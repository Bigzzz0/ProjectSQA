package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.jstype.JSTypeRegistry.ResolveMode;
import org.junit.Before;
import org.junit.Test;

/**
 * /* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target: com.google.javascript.rhino.jstype.JSType
 * Primary Defect Target:
 *   - SemanticReverseAbstractInterpreterTest::testEqCondition4 (expected:<None> but was:<undefined>)
 *   - Root cause in JSType.java:
 *       testForEquality(JSType that):
 *         if (that instanceof UnionType) {
 *           ...
 *           for (...) { ... }
 *           // Defect: missing `return result;` causing execution to fall through
 *           // to `return null;` when all alternates yield identical equality outcomes
 *         }
 *         return null;
 *       This returns null instead of TernaryValue.FALSE or TernaryValue.TRUE, which triggers
 *       NPE in canTestForEqualityWith() and corrupts FlowScope type inference under equality/inequality.
 *
 * Branch & Equivalence Partitions:
 *   - Partition A: Core Functional Logic & State Transitions
 *       * getLeastSupertype / getGreatestSubtype (lattice meet/join: Empty, All, Unknown, Subtype, Union, Object)
 *       * dereference() & autoboxing / restricted non-null or undefined
 *       * differsFrom() matrix (neither unknown, one unknown, both unknown)
 *       * matchesNumberContext, matchesStringContext, matchesObjectContext, matchesInt32/Uint32
 *       * canAssignTo() & isSubtype() helper (unknown, equivalent, all, union alternates, named type)
 *       * findPropertyType() with autoboxed and unboxable types
 *   - Partition B: Boundary Value Analysis (BVA) & Extremes
 *       * isEquivalent(null, null), isEquivalent(null, type), isEquivalent(type, null)
 *       * safeResolve(null, ...), resolve() caching and cycle resolution (resolveResult == null fallback)
 *       * ALPHA Comparator on identical, differing, and reverse ordered types
 *   - Partition C: Defect-Targeted Branch Zone
 *       * testForEquality() with UnionType alternates returning uniform FALSE
 *       * testForEquality() with UnionType alternates returning uniform TRUE
 *       * testForEquality() with UnionType alternates returning mixed -> UNKNOWN
 *       * canTestForEqualityWith() against UnionType ensuring no NullPointerException
 *       * getTypesUnderEquality / getTypesUnderInequality with UnionType and single types
 *       * getTypesUnderShallowInequality with null/undefined vs other types
 *       * getRestrictedTypeGivenToBooleanOutcome with boolean outcomes (true/false) for null, void, object
 *   - Partition D: Exception & Defensive Guard Paths
 *       * resolve() loopback safeguard returning UNKNOWN_TYPE
 *       * forceResolve() mode preservation
 *       * getTypesUnderEquality / getTypesUnderInequality switch exhaustive check
 *   - Partition E: Object Lifecycle & Contract Integrity
 *       * equals() contract: identity, proxy delegation, non-JSType object
 *       * hashCode() contract: identity hashCode & toDebugHashCodeString()
 *       * Dummy subclass testing default polymorphic fallback implementations
 * =========================================================================
 */
public class JSTypeGeminiTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter errorReporter;

  private JSType allType;
  private JSType unknownType;
  private JSType checkedUnknownType;
  private JSType noType;
  private JSType noObjectType;
  private JSType numberType;
  private JSType numberObjectType;
  private JSType stringType;
  private JSType stringObjectType;
  private JSType booleanType;
  private JSType booleanObjectType;
  private JSType nullType;
  private JSType voidType;
  private JSType objectType;

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(errorReporter);

    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    checkedUnknownType = registry.getNativeType(JSTypeNative.CHECKED_UNKNOWN_TYPE);
    noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    noObjectType = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    numberObjectType = registry.getNativeType(JSTypeNative.NUMBER_OBJECT_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    stringObjectType = registry.getNativeType(JSTypeNative.STRING_OBJECT_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    booleanObjectType = registry.getNativeType(JSTypeNative.BOOLEAN_OBJECT_TYPE);
    nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testPredicateMethodsForPrimitiveTypes() {
    assertTrue(numberType.isNumberValueType());
    assertTrue(numberType.isNumber());
    assertFalse(numberType.isString());
    assertFalse(numberType.isEmptyType());

    assertTrue(stringType.isStringValueType());
    assertTrue(stringType.isString());
    assertFalse(stringType.isNumber());

    assertTrue(noType.isNoType());
    assertTrue(noType.isEmptyType());
    assertTrue(noObjectType.isNoObjectType());
    assertTrue(noObjectType.isEmptyType());

    assertTrue(nullType.isNullType());
    assertTrue(nullType.isNullable());
    assertFalse(numberType.isNullable());

    assertTrue(voidType.isVoidType());
    assertTrue(allType.isAllType());
    assertTrue(unknownType.isUnknownType());
    assertTrue(checkedUnknownType.isCheckedUnknownType());
    assertTrue(objectType.isObject());
  }

  @Test(timeout = 4000)
  public void testContextMatching() {
    assertTrue(numberType.matchesNumberContext());
    assertTrue(numberType.matchesInt32Context());
    assertTrue(numberType.matchesUint32Context());
    assertFalse(nullType.matchesNumberContext());

    assertTrue(stringType.matchesStringContext());
    assertFalse(nullType.matchesStringContext());

    assertTrue(objectType.matchesObjectContext());
    assertFalse(nullType.matchesObjectContext());
  }

  @Test(timeout = 4000)
  public void testDiffersFromMatrix() {
    // Both not unknown
    assertFalse(numberType.differsFrom(numberType));
    assertTrue(numberType.differsFrom(stringType));

    // Exactly one unknown
    assertTrue(numberType.differsFrom(unknownType));
    assertTrue(unknownType.differsFrom(stringType));

    // Both unknown
    assertFalse(unknownType.differsFrom(unknownType));
  }

  @Test(timeout = 4000)
  public void testDereferenceAndAutoboxing() {
    ObjectType derefNumber = numberType.dereference();
    assertNotNull(derefNumber);
    assertTrue(derefNumber.isSubtype(objectType));

    ObjectType derefObject = objectType.dereference();
    assertEquals(objectType, derefObject);

    ObjectType derefNull = nullType.dereference();
    assertNull(derefNull);

    ObjectType derefVoid = voidType.dereference();
    assertNull(derefVoid);
  }

  @Test(timeout = 4000)
  public void testFindPropertyType() {
    JSType prop = stringType.findPropertyType("length");
    assertNotNull(prop);
    assertTrue(prop.isNumber());

    assertNull(nullType.findPropertyType("length"));
    assertNull(voidType.findPropertyType("toString"));
  }

  @Test(timeout = 4000)
  public void testLeastSupertypeLattice() {
    // number \/ all = all
    assertEquals(allType, numberType.getLeastSupertype(allType));
    // number \/ noType = number
    assertEquals(numberType, numberType.getLeastSupertype(noType));
    // number \/ number = number
    assertEquals(numberType, numberType.getLeastSupertype(numberType));

    // number \/ string = (number, string)
    JSType join = numberType.getLeastSupertype(stringType);
    assertTrue(join.isUnionType());
    assertTrue(numberType.isSubtype(join));
    assertTrue(stringType.isSubtype(join));

    // Union delegation: (number, string) \/ boolean
    JSType tripleUnion = join.getLeastSupertype(booleanType);
    assertTrue(tripleUnion.isUnionType());
    assertTrue(booleanType.isSubtype(tripleUnion));
  }

  @Test(timeout = 4000)
  public void testGreatestSubtypeLattice() {
    // number /\ all = number
    assertEquals(numberType, numberType.getGreatestSubtype(allType));
    // number /\ noType = noType
    assertEquals(noType, numberType.getGreatestSubtype(noType));
    // number /\ unknown = unknown
    assertEquals(unknownType, numberType.getGreatestSubtype(unknownType));
    // unknown /\ unknown = unknown (equivalent branch)
    assertEquals(unknownType, unknownType.getGreatestSubtype(unknownType));

    // number /\ string = noType
    assertEquals(noType, numberType.getGreatestSubtype(stringType));

    // objectType /\ numberObjectType = numberObjectType (subtype branch)
    assertEquals(numberObjectType, numberObjectType.getGreatestSubtype(objectType));
    assertEquals(numberObjectType, objectType.getGreatestSubtype(numberObjectType));

    // Two disjoint object types: Date /\ RegExp = noObjectType
    JSType dateType = registry.getNativeType(JSTypeNative.DATE_TYPE);
    JSType regExpType = registry.getNativeType(JSTypeNative.REGEXP_TYPE);
    assertEquals(noObjectType, dateType.getGreatestSubtype(regExpType));
  }

  @Test(timeout = 4000)
  public void testCanAssignTo() {
    assertTrue(numberType.canAssignTo(numberType));
    assertTrue(numberType.canAssignTo(allType));
    assertFalse(numberType.canAssignTo(stringType));
    assertTrue(noType.canAssignTo(numberType));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsEquivalentStaticMethodNullHandling() {
    assertTrue(JSType.isEquivalent(null, null));
    assertFalse(JSType.isEquivalent(numberType, null));
    assertFalse(JSType.isEquivalent(null, numberType));
    assertTrue(JSType.isEquivalent(numberType, numberType));
    assertFalse(JSType.isEquivalent(numberType, stringType));
  }

  @Test(timeout = 4000)
  public void testAlphaComparatorTotalOrdering() {
    assertEquals(0, JSType.ALPHA.compare(numberType, numberType));
    int cmp1 = JSType.ALPHA.compare(numberType, stringType);
    int cmp2 = JSType.ALPHA.compare(stringType, numberType);
    assertTrue((cmp1 < 0 && cmp2 > 0) || (cmp1 > 0 && cmp2 < 0) || (cmp1 == 0 && cmp2 == 0));
  }

  @Test(timeout = 4000)
  public void testSafeResolveWithNull() {
    assertNull(JSType.safeResolve(null, errorReporter, null));
    JSType resolved = JSType.safeResolve(numberType, errorReporter, null);
    assertEquals(numberType, resolved);
  }

  @Test(timeout = 4000)
  public void testResolveCycleFallbackToUnknown() {
    DummyJSType dummy = new DummyJSType(registry);
    dummy.setResolvedManually(true, null);
    // When resolved is true but resolveResult is null, resolve() returns UNKNOWN_TYPE
    JSType result = dummy.resolve(errorReporter, null);
    assertEquals(unknownType, result);
  }

  @Test(timeout = 4000)
  public void testClearResolved() {
    DummyJSType dummy = new DummyJSType(registry);
    dummy.resolve(errorReporter, null);
    assertTrue(dummy.isResolved());
    dummy.clearResolved();
    assertFalse(dummy.isResolved());
  }

  @Test(timeout = 4000)
  public void testForceResolveModePreservation() {
    registry.setResolveMode(ResolveMode.LAZY);
    DummyJSType dummy = new DummyJSType(registry);
    JSType res = dummy.forceResolve(errorReporter, null);
    assertEquals(dummy, res);
    assertEquals(ResolveMode.LAZY, registry.getResolveMode());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone
  // (SemanticReverseAbstractInterpreterTest::testEqCondition4 defect & testForEquality)
  // =========================================================================

  /**
   * CRITICAL DEFECT TEST:
   * In JSType.java, testForEquality(JSType that) checks `if (that instanceof UnionType)`.
   * It loops over the union alternates, computing `result = test`, but misses `return result;`
   * at the end of the if-block, falling through to `return null;`.
   *
   * When null is compared to (number | string), both alternates are FALSE.
   * Expected: TernaryValue.FALSE.
   * Defect: Returns null -> throws NullPointerException or causes assertion failure.
   */
  @Test(timeout = 4000)
  public void testDefectUnionTypeTestForEqualityReturnsFalse() {
    JSType union = registry.createUnionType(numberType, stringType);

    // null == (number | string) must be deterministically FALSE
    TernaryValue result = nullType.testForEquality(union);
    assertNotNull("Defect triggered: testForEquality returned null instead of TernaryValue.FALSE", result);
    assertEquals(TernaryValue.FALSE, result);

    // canTestForEqualityWith must not throw NPE and should return false (since it's FALSE, not UNKNOWN)
    assertFalse(nullType.canTestForEqualityWith(union));
  }

  /**
   * CRITICAL DEFECT TEST:
   * When void (undefined) is compared to (number | string), both alternates are FALSE.
   * Expected: TernaryValue.FALSE.
   * Defect: Returns null instead of TernaryValue.FALSE.
   */
  @Test(timeout = 4000)
  public void testDefectVoidTypeTestForEqualityWithUnionReturnsFalse() {
    JSType union = registry.createUnionType(numberType, stringType);

    TernaryValue result = voidType.testForEquality(union);
    assertNotNull("Defect triggered: testForEquality returned null instead of TernaryValue.FALSE", result);
    assertEquals(TernaryValue.FALSE, result);
  }

  /**
   * CRITICAL DEFECT TEST:
   * When void is compared to (null | void), both alternates evaluate to TRUE.
   * Expected: TernaryValue.TRUE.
   * Defect: Returns null instead of TernaryValue.TRUE.
   */
  @Test(timeout = 4000)
  public void testDefectVoidTypeTestForEqualityWithUnionReturnsTrue() {
    JSType union = registry.createUnionType(nullType, voidType);

    TernaryValue result = voidType.testForEquality(union);
    assertNotNull("Defect triggered: testForEquality returned null instead of TernaryValue.TRUE", result);
    assertEquals(TernaryValue.TRUE, result);
  }

  @Test(timeout = 4000)
  public void testTestForEqualitySpecialTypes() {
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(allType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(noType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(unknownType));
  }

  @Test(timeout = 4000)
  public void testTestForEqualityMixedUnionReturnsUnknown() {
    // null == (null | number) -> null==null is TRUE, null==number is FALSE -> UNKNOWN
    JSType union = registry.createUnionType(nullType, numberType);
    assertEquals(TernaryValue.UNKNOWN, nullType.testForEquality(union));
    assertTrue(nullType.canTestForEqualityWith(union));
  }

  @Test(timeout = 4000)
  public void testGetTypesUnderEqualityAndInequality() {
    // Equality: void == null -> TRUE -> pair(void, null)
    JSType.TypePair eqPair = voidType.getTypesUnderEquality(nullType);
    assertEquals(voidType, eqPair.typeA);
    assertEquals(nullType, eqPair.typeB);

    // Inequality: void != null -> TRUE -> impossible -> pair(null, null)
    JSType.TypePair ineqPair = voidType.getTypesUnderInequality(nullType);
    assertNull(ineqPair.typeA);
    assertNull(ineqPair.typeB);

    // Inequality: number != string -> FALSE -> pair(number, string)
    JSType.TypePair numStrIneq = numberType.getTypesUnderInequality(stringType);
    assertEquals(numberType, numStrIneq.typeA);
    assertEquals(stringType, numStrIneq.typeB);
  }

  @Test(timeout = 4000)
  public void testGetTypesUnderShallowEqualityAndInequality() {
    // Shallow Equality: greatest subtype of number and string is noType
    JSType.TypePair shallowEq = numberType.getTypesUnderShallowEquality(stringType);
    assertEquals(noType, shallowEq.typeA);
    assertEquals(noType, shallowEq.typeB);

    // Shallow Inequality: null !== null -> pair(null, null)
    JSType.TypePair nullNullIneq = nullType.getTypesUnderShallowInequality(nullType);
    assertNull(nullNullIneq.typeA);
    assertNull(nullNullIneq.typeB);

    // Shallow Inequality: void !== void -> pair(null, null)
    JSType.TypePair voidVoidIneq = voidType.getTypesUnderShallowInequality(voidType);
    assertNull(voidVoidIneq.typeA);
    assertNull(voidVoidIneq.typeB);

    // Shallow Inequality: void !== null -> pair(void, null)
    JSType.TypePair voidNullIneq = voidType.getTypesUnderShallowInequality(nullType);
    assertEquals(voidType, voidNullIneq.typeA);
    assertEquals(nullType, voidNullIneq.typeB);
  }

  @Test(timeout = 4000)
  public void testRestrictedTypeGivenToBooleanOutcome() {
    // void (undefined) ToBoolean is always {false}
    assertEquals(noType, voidType.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(voidType, voidType.getRestrictedTypeGivenToBooleanOutcome(false));

    // null ToBoolean is always {false}
    assertEquals(noType, nullType.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(nullType, nullType.getRestrictedTypeGivenToBooleanOutcome(false));

    // Object ToBoolean is always {true}
    assertEquals(objectType, objectType.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(noType, objectType.getRestrictedTypeGivenToBooleanOutcome(false));

    // Number ToBoolean can be true or false
    assertEquals(numberType, numberType.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(numberType, numberType.getRestrictedTypeGivenToBooleanOutcome(false));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsSubtypeHelperMethodBranches() {
    // 1. that is unknown -> true
    assertTrue(JSType.isSubtype(numberType, unknownType));

    // 2. equivalent -> true
    assertTrue(JSType.isSubtype(numberType, numberType));

    // 3. that is all -> true
    assertTrue(JSType.isSubtype(numberType, allType));

    // 4. that is union containing subtype -> true
    JSType numOrStr = registry.createUnionType(numberType, stringType);
    assertTrue(JSType.isSubtype(numberType, numOrStr));

    // 5. that is unrelated -> false
    assertFalse(JSType.isSubtype(numberType, stringType));
  }

  @Test(timeout = 4000)
  public void testCanTestForShallowEqualityWith() {
    assertTrue(numberType.canTestForShallowEqualityWith(numberType));
    assertTrue(numberObjectType.canTestForShallowEqualityWith(objectType));
    assertFalse(numberType.canTestForShallowEqualityWith(stringType));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testEqualsAndHashCodeContract() {
    assertEquals(numberType, numberType);
    assertNotEquals(numberType, stringType);
    assertFalse(numberType.equals("NotAJSType"));
    assertFalse(numberType.equals(null));

    assertEquals(System.identityHashCode(numberType), numberType.hashCode());
    assertEquals("{" + numberType.hashCode() + "}", numberType.toDebugHashCodeString());
  }

  @Test(timeout = 4000)
  public void testDummySubclassDefaultBehaviors() {
    DummyJSType dummy = new DummyJSType(registry);

    assertNull(dummy.getJSDocInfo());
    dummy.forgiveUnknownNames(); // Should be a no-op

    assertFalse(dummy.isNoType());
    assertFalse(dummy.isNoObjectType());
    assertFalse(dummy.isNumberObjectType());
    assertFalse(dummy.isNumberValueType());
    assertFalse(dummy.isFunctionPrototypeType());
    assertFalse(dummy.isStringObjectType());
    assertFalse(dummy.isTheObjectType());
    assertFalse(dummy.isStringValueType());
    assertFalse(dummy.isArrayType());
    assertFalse(dummy.isBooleanObjectType());
    assertFalse(dummy.isBooleanValueType());
    assertFalse(dummy.isRegexpType());
    assertFalse(dummy.isDateType());
    assertFalse(dummy.isNullType());
    assertFalse(dummy.isVoidType());
    assertFalse(dummy.isAllType());
    assertFalse(dummy.isUnknownType());
    assertFalse(dummy.isCheckedUnknownType());
    assertFalse(dummy.isUnionType());
    assertFalse(dummy.isFunctionType());
    assertFalse(dummy.isEnumElementType());
    assertFalse(dummy.isEnumType());
    assertFalse(dummy.isNamedType());
    assertFalse(dummy.isRecordType());
    assertFalse(dummy.isTemplateType());
    assertFalse(dummy.isObject());
    assertFalse(dummy.isConstructor());
    assertFalse(dummy.isNominalType());
    assertFalse(dummy.isInstanceType());
    assertFalse(dummy.isInterface());
    assertFalse(dummy.isOrdinaryFunction());

    assertFalse(dummy.matchesInt32Context());
    assertFalse(dummy.matchesUint32Context());
    assertFalse(dummy.matchesNumberContext());
    assertFalse(dummy.matchesStringContext());
    assertFalse(dummy.matchesObjectContext());

    assertFalse(dummy.canBeCalled());
    assertNull(dummy.autoboxesTo());
    assertNull(dummy.unboxesTo());
    assertNull(dummy.toObjectType());
    assertEquals(dummy, dummy.restrictByNotNullOrUndefined());
    assertNull(dummy.testForEquality(numberType));
  }

  // =========================================================================
  // Test Fixture Helpers
  // =========================================================================

  private static class DummyJSType extends JSType {
    private static final long serialVersionUID = 1L;

    DummyJSType(JSTypeRegistry registry) {
      super(registry);
    }

    void setResolvedManually(boolean resolved, JSType result) {
      if (resolved) {
        setResolvedTypeInternal(result);
      } else {
        clearResolved();
      }
    }

    @Override
    public BooleanLiteralSet getPossibleToBooleanOutcomes() {
      return BooleanLiteralSet.BOTH;
    }

    @Override
    public boolean isSubtype(JSType that) {
      return JSType.isSubtype(this, that);
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return null;
    }

    @Override
    JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
      return this;
    }
  }
}