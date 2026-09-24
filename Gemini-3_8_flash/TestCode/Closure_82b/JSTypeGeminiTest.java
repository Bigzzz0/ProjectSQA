/* [Branch & Defect Analysis Matrix]
 *
 * -----------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.rhino.jstype.JSType
 * Ground Truth Defects:
 *   - FunctionTypeTest::testEmptyFunctionTypes (Defects4J Closure-82)
 *   - TypeCheckTest::testIssue301
 *   Root cause: LEAST_FUNCTION_TYPE was not recognized as an empty type by isEmptyType(),
 *   causing incorrect equality inference, join/meet computations, and missed warnings.
 * -----------------------------------------------------------------------------------------
 * Branch & Condition Coverage Matrix:
 *
 * 1. isEmptyType():
 *    - isNoType() = true -> returns true
 *    - isNoObjectType() = true -> returns true
 *    - isNoResolvedType() = true -> returns true
 *    - [DEFECT TARGET] LEAST_FUNCTION_TYPE.isEmptyType() must be true!
 *
 * 2. isEquivalent(JSType, JSType) & isEquivalentTo(JSType):
 *    - typeA == null, typeB == null -> true
 *    - typeA == null, typeB != null -> false
 *    - typeA != null, typeB == null -> false
 *    - jsType instanceof ProxyObjectType -> delegates to proxy.isEquivalentTo(this)
 *    - this == jsType -> true
 *    - this != jsType -> false
 *
 * 3. dereference():
 *    - autoboxesTo() != null (e.g., number value -> Number Object)
 *    - autoboxesTo() == null, restricted is Object (e.g., ObjectType -> ObjectType)
 *    - autoboxesTo() == null, restricted is not Object (e.g., null / void -> null)
 *
 * 4. testForEqualityHelper(JSType, JSType):
 *    - aType or bType is AllType / UnknownType / NoResolvedType -> UNKNOWN
 *    - aIsEmpty && bIsEmpty -> TRUE
 *    - aIsEmpty ^ bIsEmpty -> UNKNOWN
 *    - aType or bType is FunctionType:
 *        * otherType meet OBJECT_TYPE is NoType / NoObjectType -> FALSE
 *        * otherType meet OBJECT_TYPE is Object -> UNKNOWN
 *    - bType is EnumElementType or UnionType -> delegates to bType.testForEquality(aType)
 *    - default -> null
 *
 * 5. getGreatestSubtype(JSType, JSType):
 *    - this == that -> this
 *    - this or that is UnknownType (equal vs not equal)
 *    - this isSubtype that -> filterNoResolvedType(this)
 *    - that isSubtype this -> filterNoResolvedType(that)
 *    - this is UnionType -> meet
 *    - that is UnionType -> meet
 *    - this is Object && that is Object (disjoint objects) -> NO_OBJECT_TYPE
 *    - default fallback -> NO_TYPE
 *    - that is RecordType -> that.getGreatestSubtype(this)
 *
 * 6. filterNoResolvedType(JSType):
 *    - type.isNoResolvedType() -> NO_RESOLVED_TYPE
 *    - type is UnionType containing NoResolvedType -> filtered union without NoResolvedType
 *    - type is UnionType not containing NoResolvedType -> same union
 *    - other types -> returns unmodified
 *
 * 7. differsFrom(JSType):
 *    - neither is unknown -> !isEquivalentTo
 *    - one unknown, one not unknown -> true
 *    - both unknown -> false
 *
 * 8. resolve() & forceResolve():
 *    - resolved == false -> calls resolveInternal, sets resolveResult & resolved
 *    - resolved == true & resolveResult != null -> returns resolveResult
 *    - resolved == true & resolveResult == null (recursive cycle) -> UNKNOWN_TYPE
 *    - forceResolve switches ResolveMode to IMMEDIATE and restores previous mode
 *
 * 9. TypePair resolution methods:
 *    - getTypesUnderEquality, getTypesUnderInequality,
 *      getTypesUnderShallowEquality, getTypesUnderShallowInequality
 *    - UnionType delegations vs non-union branches
 *    - null/undefined special casing under shallow inequality
 * -----------------------------------------------------------------------------------------
 */

package com.google.javascript.rhino.jstype;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.jstype.JSTypeRegistry.ResolveMode;
import com.google.common.base.Predicate;

public class JSTypeGeminiTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter reporter;

  // Concrete test dummy for exercising non-overridden JSType baseline behavior
  private static class DummyJSType extends JSType {
    private static final long serialVersionUID = 1L;
    private final String displayName;
    private BooleanLiteralSet booleanOutcomes = BooleanLiteralSet.BOTH;

    DummyJSType(JSTypeRegistry registry, String displayName) {
      super(registry);
      this.displayName = displayName;
    }

    void setBooleanOutcomes(BooleanLiteralSet outcomes) {
      this.booleanOutcomes = outcomes;
    }

    @Override
    public String getDisplayName() {
      return displayName;
    }

    @Override
    public BooleanLiteralSet getPossibleToBooleanOutcomes() {
      return booleanOutcomes;
    }

    @Override
    public boolean isSubtype(JSType that) {
      return this == that;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return null;
    }

    @Override
    JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
      return this;
    }

    @Override
    public String toString() {
      return displayName == null ? "DummyJSType" : displayName;
    }
  }

  // Type that simulates re-entrant resolve cycle where resolveResult is null while resolved is true
  private static class CyclicResolveType extends JSType {
    private static final long serialVersionUID = 1L;

    CyclicResolveType(JSTypeRegistry registry) {
      super(registry);
    }

    @Override
    public BooleanLiteralSet getPossibleToBooleanOutcomes() {
      return BooleanLiteralSet.BOTH;
    }

    @Override
    public boolean isSubtype(JSType that) {
      return this == that;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return null;
    }

    @Override
    JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
      // Re-entrant call while resolution is currently in-flight
      return this.resolve(t, scope);
    }
  }

  @Before
  public void setUp() {
    reporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(reporter);
  }

  // =========================================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Closure-82 / Issue 301)
  // =========================================================================================

  /**
   * Targets Defect: FunctionTypeTest::testEmptyFunctionTypes / TypeCheckTest::testIssue301.
   * LEAST_FUNCTION_TYPE represents an uninhabited/empty function type.
   * JSType.isEmptyType() must return true for LEAST_FUNCTION_TYPE.
   */
  @Test(timeout = 4000)
  public void testDefectLeastFunctionTypeIsEmptyType() {
    JSType leastFunctionType = registry.getNativeType(JSTypeNative.LEAST_FUNCTION_TYPE);
    assertNotNull("LEAST_FUNCTION_TYPE should exist in registry", leastFunctionType);
    assertTrue("LEAST_FUNCTION_TYPE must evaluate to emptyType", leastFunctionType.isEmptyType());
  }

  /**
   * Targets Defect: Equality comparison between empty types (such as LEAST_FUNCTION_TYPE and NO_TYPE).
   * When both types are empty, testForEquality must evaluate to TRUE.
   */
  @Test(timeout = 4000)
  public void testDefectEqualityBetweenLeastFunctionAndNoType() {
    JSType leastFunction = registry.getNativeType(JSTypeNative.LEAST_FUNCTION_TYPE);
    JSType noType = registry.getNativeType(JSTypeNative.NO_TYPE);

    // If leastFunction is recognized as an empty type, two empty types equal TRUE
    assertEquals(
        "Comparison between two empty types must yield TRUE",
        TernaryValue.TRUE,
        leastFunction.testForEquality(noType));
  }

  // =========================================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================================

  @Test(timeout = 4000)
  public void testDefaultTypePredicatesOnBaseJSType() {
    DummyJSType dummy = new DummyJSType(registry, "Dummy");

    assertFalse(dummy.isNoType());
    assertFalse(dummy.isNoResolvedType());
    assertFalse(dummy.isNoObjectType());
    assertFalse(dummy.isEmptyType());
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
    assertFalse(dummy.canBeCalled());
    assertNull(dummy.autoboxesTo());
    assertNull(dummy.unboxesTo());
    assertNull(dummy.toObjectType());
    assertNull(dummy.getJSDocInfo());

    // Context predicates default to false
    assertFalse(dummy.matchesNumberContext());
    assertFalse(dummy.matchesStringContext());
    assertFalse(dummy.matchesObjectContext());
    assertFalse(dummy.matchesInt32Context());
    assertFalse(dummy.matchesUint32Context());

    dummy.forgiveUnknownNames(); // Should be a no-op without exception
  }

  @Test(timeout = 4000)
  public void testEmptyTypePredicateCombinations() {
    JSType noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    JSType noObjectType = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    JSType noResolvedType = registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE);

    assertTrue("NO_TYPE is an empty type", noType.isEmptyType());
    assertTrue("NO_OBJECT_TYPE is an empty type", noObjectType.isEmptyType());
    assertTrue("NO_RESOLVED_TYPE is an empty type", noResolvedType.isEmptyType());
    assertTrue(noType.isNoType());
    assertTrue(noObjectType.isNoObjectType());
    assertTrue(noResolvedType.isNoResolvedType());
  }

  @Test(timeout = 4000)
  public void testDisplayNameHandling() {
    DummyJSType namedDummy = new DummyJSType(registry, "ValidName");
    assertTrue(namedDummy.hasDisplayName());
    assertEquals("ValidName", namedDummy.getDisplayName());

    DummyJSType emptyNamedDummy = new DummyJSType(registry, "");
    assertFalse(emptyNamedDummy.hasDisplayName());

    DummyJSType nullNamedDummy = new DummyJSType(registry, null);
    assertFalse(nullNamedDummy.hasDisplayName());
    assertNull(nullNamedDummy.getDisplayName());
  }

  @Test(timeout = 4000)
  public void testIsStringAndIsNumber() {
    JSType stringVal = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType stringObj = registry.getNativeType(JSTypeNative.STRING_OBJECT_TYPE);
    JSType numberVal = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType numberObj = registry.getNativeType(JSTypeNative.NUMBER_OBJECT_TYPE);
    JSType booleanVal = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);

    assertTrue("string value is string", stringVal.isString());
    assertTrue("String object is string", stringObj.isString());
    assertFalse("number value is not string", numberVal.isString());

    assertTrue("number value is number", numberVal.isNumber());
    assertTrue("Number object is number", numberObj.isNumber());
    assertFalse("boolean value is not number", booleanVal.isNumber());
  }

  @Test(timeout = 4000)
  public void testDereference() {
    JSType numberVal = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    ObjectType derefNumber = numberVal.dereference();
    assertNotNull("Number value autoboxes when dereferenced", derefNumber);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_OBJECT_TYPE), derefNumber);

    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    ObjectType derefObject = objectType.dereference();
    assertEquals("Object type dereferences to itself", objectType, derefObject);

    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    assertNull("Null cannot dereference to an object", nullType.dereference());

    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    assertNull("Void cannot dereference to an object", voidType.dereference());
  }

  @Test(timeout = 4000)
  public void testFindPropertyType() {
    JSType numberVal = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType toStringProp = numberVal.findPropertyType("toString");
    assertNotNull("Number value property access should find toString on Number.prototype", toStringProp);

    JSType nonExistentProp = numberVal.findPropertyType("nonExistentPropXYZ");
    assertNull("Non-existent property returns null", nonExistentProp);

    DummyJSType dummy = new DummyJSType(registry, "Dummy");
    assertNull("Unautoboxable type returns null property type", dummy.findPropertyType("anyProp"));
  }

  @Test(timeout = 4000)
  public void testCanAssignTo() {
    JSType numberVal = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    JSType stringVal = registry.getNativeType(JSTypeNative.STRING_TYPE);

    assertTrue("Number can assign to Number", numberVal.canAssignTo(numberVal));
    assertTrue("Number can assign to AllType (*)", numberVal.canAssignTo(allType));
    assertFalse("Number cannot assign to String", numberVal.canAssignTo(stringVal));
  }

  @Test(timeout = 4000)
  public void testIsNullable() {
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    JSType numberVal = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType unionWithNull = registry.createUnionType(numberVal, nullType);

    assertTrue("NullType is nullable", nullType.isNullable());
    assertTrue("Union containing Null is nullable", unionWithNull.isNullable());
    assertFalse("NumberType is not nullable", numberVal.isNullable());
  }

  @Test(timeout = 4000)
  public void testDiffersFrom() {
    JSType numA = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType strB = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType unknown1 = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    JSType unknown2 = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);

    assertFalse("Identical types do not differ", numA.differsFrom(numA));
    assertTrue("Different concrete types differ", numA.differsFrom(strB));

    assertTrue("Unknown differs from concrete type (LHS unknown)", unknown1.differsFrom(numA));
    assertTrue("Concrete type differs from unknown (RHS unknown)", numA.differsFrom(unknown1));
    assertFalse("Two unknown types do not meaningfully differ", unknown1.differsFrom(unknown2));
  }

  @Test(timeout = 4000)
  public void testGetRestrictedTypeGivenToBooleanOutcome() {
    DummyJSType dummy = new DummyJSType(registry, "Dummy");

    dummy.setBooleanOutcomes(BooleanLiteralSet.TRUE);
    assertSame("Restricting on true matches literals", dummy, dummy.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals("Restricting on false fails and yields NO_TYPE",
        registry.getNativeType(JSTypeNative.NO_TYPE), dummy.getRestrictedTypeGivenToBooleanOutcome(false));

    dummy.setBooleanOutcomes(BooleanLiteralSet.FALSE);
    assertSame("Restricting on false matches literals", dummy, dummy.getRestrictedTypeGivenToBooleanOutcome(false));
    assertEquals("Restricting on true fails and yields NO_TYPE",
        registry.getNativeType(JSTypeNative.NO_TYPE), dummy.getRestrictedTypeGivenToBooleanOutcome(true));
  }

  // =========================================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================================

  @Test(timeout = 4000)
  public void testIsEquivalentNullBoundaryChecks() {
    DummyJSType dummy = new DummyJSType(registry, "Dummy");

    assertTrue("null is equivalent to null", JSType.isEquivalent(null, null));
    assertFalse("type is not equivalent to null", JSType.isEquivalent(dummy, null));
    assertFalse("null is not equivalent to type", JSType.isEquivalent(null, dummy));
    assertTrue("type is equivalent to itself", JSType.isEquivalent(dummy, dummy));
  }

  @Test(timeout = 4000)
  public void testProxyObjectTypeEquivalenceDelegation() {
    DummyJSType target = new DummyJSType(registry, "Target");
    ProxyObjectType proxy = new ProxyObjectType(registry, target);

    assertTrue("Direct type equivalence with ProxyObjectType invokes delegation", target.isEquivalentTo(proxy));
    assertTrue("ProxyObjectType equivalence with target invokes delegation", proxy.isEquivalentTo(target));
  }

  @Test(timeout = 4000)
  public void testTestForEqualityHelperBranches() {
    JSType allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    JSType noResolvedType = registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE);
    JSType noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    JSType numberVal = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType functionInst = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);

    // 1. AllType / UnknownType / NoResolvedType return UNKNOWN
    assertEquals(TernaryValue.UNKNOWN, allType.testForEquality(numberVal));
    assertEquals(TernaryValue.UNKNOWN, numberVal.testForEquality(allType));
    assertEquals(TernaryValue.UNKNOWN, unknownType.testForEquality(numberVal));
    assertEquals(TernaryValue.UNKNOWN, noResolvedType.testForEquality(numberVal));

    // 2. Empty types
    assertEquals(TernaryValue.TRUE, noType.testForEquality(noType));
    assertEquals(TernaryValue.UNKNOWN, noType.testForEquality(numberVal));
    assertEquals(TernaryValue.UNKNOWN, numberVal.testForEquality(noType));

    // 3. Function vs incompatible type (Number) -> FALSE
    assertEquals(TernaryValue.FALSE, functionInst.testForEquality(numberVal));
    assertEquals(TernaryValue.FALSE, numberVal.testForEquality(functionInst));

    // Function vs compatible ObjectType -> UNKNOWN
    assertEquals(TernaryValue.UNKNOWN, functionInst.testForEquality(objectType));

    // 4. Default baseline dummy vs dummy returns null helper result
    DummyJSType dummy1 = new DummyJSType(registry, "D1");
    DummyJSType dummy2 = new DummyJSType(registry, "D2");
    assertNull("Base testForEqualityHelper without match returns null", dummy1.testForEquality(dummy2));
  }

  @Test(timeout = 4000)
  public void testCanTestForEqualityWith() {
    JSType allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    JSType numberVal = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType functionInst = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);

    assertTrue("Can test equality when result is UNKNOWN", allType.canTestForEqualityWith(numberVal));
    assertFalse("Cannot test equality when result is FALSE", functionInst.canTestForEqualityWith(numberVal));
  }

  @Test(timeout = 4000)
  public void testCanTestForShallowEqualityWith() {
    JSType numberVal = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    JSType stringVal = registry.getNativeType(JSTypeNative.STRING_TYPE);

    assertTrue("Subtype allows shallow equality test", numberVal.canTestForShallowEqualityWith(allType));
    assertTrue("Supertype allows shallow equality test", allType.canTestForShallowEqualityWith(numberVal));
    assertFalse("Disjoint types cannot test shallow equality", numberVal.canTestForShallowEqualityWith(stringVal));
  }

  @Test(timeout = 4000)
  public void testGetLeastSupertypeBranches() {
    JSType numberVal = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringVal = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType booleanVal = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);

    // Equivalent branch
    assertSame(numberVal, numberVal.getLeastSupertype(numberVal));

    // Non-equivalent branch creates union
    JSType numOrStr = numberVal.getLeastSupertype(stringVal);
    assertTrue(numOrStr.isUnionType());

    // That is UnionType branch delegation
    JSType unionRight = registry.createUnionType(stringVal, booleanVal);
    JSType joinResult = numberVal.getLeastSupertype(unionRight);
    assertTrue(joinResult.isUnionType());
    assertTrue(joinResult.isSubtype(registry.createUnionType(numberVal, stringVal, booleanVal)));
  }

  @Test(timeout = 4000)
  public void testGetGreatestSubtypeBranches() {
    JSType numberVal = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringVal = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    JSType allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    JSType dateType = registry.getNativeType(JSTypeNative.DATE_TYPE);
    JSType regexpType = registry.getNativeType(JSTypeNative.REGEXP_TYPE);
    JSType noObjectType = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    JSType noType = registry.getNativeType(JSTypeNative.NO_TYPE);

    // Equivalent branch
    assertSame(numberVal, numberVal.getGreatestSubtype(numberVal));

    // Unknown branches
    assertSame(unknownType, unknownType.getGreatestSubtype(unknownType));
    assertSame(unknownType, numberVal.getGreatestSubtype(unknownType));
    assertSame(unknownType, unknownType.getGreatestSubtype(numberVal));

    // Subtype branches
    assertEquals(numberVal, numberVal.getGreatestSubtype(allType));
    assertEquals(numberVal, allType.getGreatestSubtype(numberVal));

    // Union meet branches
    JSType numOrStr = registry.createUnionType(numberVal, stringVal);
    assertEquals(numberVal, numOrStr.getGreatestSubtype(numberVal));
    assertEquals(numberVal, numberVal.getGreatestSubtype(numOrStr));

    // Two disjoint ObjectTypes yield NO_OBJECT_TYPE
    assertEquals(noObjectType, dateType.getGreatestSubtype(regexpType));

    // Default disjoint primitive types yield NO_TYPE
    assertEquals(noType, numberVal.getGreatestSubtype(stringVal));

    // RecordType on RHS branch delegation
    RecordType recordType = registry.createRecordTypeBuilder().build();
    JSType meetWithRecord = numberVal.getGreatestSubtype(recordType);
    assertNotNull(meetWithRecord);
  }

  @Test(timeout = 4000)
  public void testFilterNoResolvedType() {
    JSType noResolved = registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE);
    JSType numberVal = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringVal = registry.getNativeType(JSTypeNative.STRING_TYPE);

    // 1. Base NoResolvedType
    assertEquals(noResolved, JSType.filterNoResolvedType(noResolved));

    // 2. UnionType without NoResolvedType
    JSType cleanUnion = registry.createUnionType(numberVal, stringVal);
    assertSame(cleanUnion, JSType.filterNoResolvedType(cleanUnion));

    // 3. UnionType with NoResolvedType needs filtering
    UnionTypeBuilder builder = new UnionTypeBuilder(registry);
    builder.addAlternate(numberVal);
    builder.addAlternate(noResolved);
    UnionType dirtyUnion = (UnionType) builder.build();

    JSType filtered = JSType.filterNoResolvedType(dirtyUnion);
    assertFalse("Filtered union must not equal dirty union containing unresolved",
        filtered.isEquivalentTo(dirtyUnion));
    assertEquals(numberVal, filtered);

    // 4. Default non-union type returns unmodified
    assertSame(numberVal, JSType.filterNoResolvedType(numberVal));
  }

  // =========================================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================================

  @Test(timeout = 4000)
  public void testResolutionLifecycleAndCycleHandling() {
    DummyJSType dummy = new DummyJSType(registry, "Dummy");
    assertFalse(dummy.isResolved());

    // First resolve
    JSType resolved = dummy.resolve(reporter, null);
    assertSame(dummy, resolved);
    assertTrue(dummy.isResolved());

    // Subsequent resolve returns cached resolveResult
    assertSame(dummy, dummy.resolve(reporter, null));

    // clearResolved resets state
    dummy.clearResolved();
    assertFalse(dummy.isResolved());

    // Cyclic recursive resolve handling: returns UNKNOWN_TYPE when resolveResult is null during cycle
    CyclicResolveType cyclicType = new CyclicResolveType(registry);
    JSType cyclicResolved = cyclicType.resolve(reporter, null);
    assertEquals("Cyclic resolution must fall back to UNKNOWN_TYPE",
        registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), cyclicResolved);
  }

  @Test(timeout = 4000)
  public void testForceResolveModeRestoration() {
    DummyJSType dummy = new DummyJSType(registry, "Dummy");
    registry.setResolveMode(ResolveMode.LAZY);
    assertEquals(ResolveMode.LAZY, registry.getResolveMode());

    JSType resolved = dummy.forceResolve(reporter, null);
    assertSame(dummy, resolved);
    assertEquals("ResolveMode must be restored to previous mode after forceResolve",
        ResolveMode.LAZY, registry.getResolveMode());
  }

  @Test(timeout = 4000)
  public void testSafeResolveNullSafety() {
    assertNull("safeResolve with null returns null", JSType.safeResolve(null, reporter, null));

    DummyJSType dummy = new DummyJSType(registry, "Dummy");
    assertSame("safeResolve with valid type resolves successfully",
        dummy, JSType.safeResolve(dummy, reporter, null));
  }

  @Test(timeout = 4000)
  public void testSetValidator() {
    DummyJSType dummy = new DummyJSType(registry, "Dummy");

    Predicate<JSType> truePredicate = new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        return input != null;
      }
    };
    assertTrue(dummy.setValidator(truePredicate));

    Predicate<JSType> falsePredicate = new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        return false;
      }
    };
    assertFalse(dummy.setValidator(falsePredicate));
  }

  // =========================================================================================
  // Partition E: Object Lifecycle, Contract Integrity & Pairs
  // =========================================================================================

  @Test(timeout = 4000)
  public void testAlphaComparatorContract() {
    DummyJSType typeA = new DummyJSType(registry, "Alpha");
    DummyJSType typeB = new DummyJSType(registry, "Beta");

    assertTrue(JSType.ALPHA.compare(typeA, typeB) < 0);
    assertTrue(JSType.ALPHA.compare(typeB, typeA) > 0);
    assertEquals(0, JSType.ALPHA.compare(typeA, typeA));
  }

  @Test(timeout = 4000)
  public void testEqualsAndHashCodeContract() {
    DummyJSType dummy1 = new DummyJSType(registry, "Dummy1");
    DummyJSType dummy2 = new DummyJSType(registry, "Dummy2");

    assertTrue("Reflexive equals", dummy1.equals(dummy1));
    assertFalse("Symmetric distinction", dummy1.equals(dummy2));
    assertFalse("Non-JSType comparison", dummy1.equals("StringObject"));
    assertFalse("Null comparison", dummy1.equals(null));

    assertEquals("Identity hashCode check", System.identityHashCode(dummy1), dummy1.hashCode());
    assertEquals("{" + dummy1.hashCode() + "}", dummy1.toDebugHashCodeString());
  }

  @Test(timeout = 4000)
  public void testTypePairUnderEqualityAndInequality() {
    JSType func = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    JSType num = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);

    // Equality: FALSE outcome produces (null, null)
    JSType.TypePair pairEqFalse = func.getTypesUnderEquality(num);
    assertNull(pairEqFalse.typeA);
    assertNull(pairEqFalse.typeB);

    // Equality: UNKNOWN outcome produces (this, that)
    JSType.TypePair pairEqUnknown = func.getTypesUnderEquality(func);
    assertSame(func, pairEqUnknown.typeA);
    assertSame(func, pairEqUnknown.typeB);

    // Inequality: TRUE outcome produces (NO_TYPE, NO_TYPE)
    JSType.TypePair pairIneqTrue = noType.getTypesUnderInequality(noType);
    assertEquals(noType, pairIneqTrue.typeA);
    assertEquals(noType, pairIneqTrue.typeB);

    // Inequality: FALSE / UNKNOWN produces (this, that)
    JSType.TypePair pairIneqFalse = func.getTypesUnderInequality(num);
    assertSame(func, pairIneqFalse.typeA);
    assertSame(num, pairIneqFalse.typeB);

    // Shallow Inequality: null vs null or void vs void produces (null, null)
    JSType.TypePair pairNull = nullType.getTypesUnderShallowInequality(nullType);
    assertNull(pairNull.typeA);
    assertNull(pairNull.typeB);

    JSType.TypePair pairVoid = voidType.getTypesUnderShallowInequality(voidType);
    assertNull(pairVoid.typeA);
    assertNull(pairVoid.typeB);

    JSType.TypePair pairOther = num.getTypesUnderShallowInequality(num);
    assertSame(num, pairOther.typeA);
    assertSame(num, pairOther.typeB);

    // Shallow Equality
    JSType.TypePair pairShallowEq = num.getTypesUnderShallowEquality(num);
    assertEquals(num, pairShallowEq.typeA);
    assertEquals(num, pairShallowEq.typeB);
  }

  @Test(timeout = 4000)
  public void testTypePairUnionDelegation() {
    JSType num = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType str = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType union = registry.createUnionType(num, str);

    // Verify union reversal delegation
    JSType.TypePair pairEq = num.getTypesUnderEquality(union);
    assertNotNull(pairEq);

    JSType.TypePair pairIneq = num.getTypesUnderInequality(union);
    assertNotNull(pairIneq);

    JSType.TypePair pairShallowIneq = num.getTypesUnderShallowInequality(union);
    assertNotNull(pairShallowIneq);
  }

  @Test(timeout = 4000)
  public void testStaticIsSubtypeHelperBranches() {
    JSType num = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType str = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType bool = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    JSType all = registry.getNativeType(JSTypeNative.ALL_TYPE);
    JSType unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    JSType unionNumStr = registry.createUnionType(num, str);

    assertTrue("thatType is UnknownType -> true", JSType.isSubtype(num, unknown));
    assertTrue("thisType equivalent thatType -> true", JSType.isSubtype(num, num));
    assertTrue("thatType is AllType -> true", JSType.isSubtype(num, all));
    assertTrue("thatType is UnionType containing subtype -> true", JSType.isSubtype(num, unionNumStr));
    assertFalse("thatType is UnionType not containing subtype -> false", JSType.isSubtype(bool, unionNumStr));

    // NamedType branch check
    NamedType namedNumber = new NamedType(registry, "NamedNumber", "test.js", 1, 0);
    namedNumber.setResolvedTypeInternal(num);
    assertTrue("NamedType resolving to Number accepts Number subtype", JSType.isSubtype(num, namedNumber));
    assertFalse("NamedType resolving to Number rejects String", JSType.isSubtype(str, namedNumber));

    assertFalse("Default fallback returns false", JSType.isSubtype(num, str));
  }

  @Test(timeout = 4000)
  public void testConstantsIntegrity() {
    assertEquals("Unknown class name", JSType.UNKNOWN_NAME);
    assertEquals("Not declared as a constructor", JSType.NOT_A_CLASS);
    assertEquals("Not declared as a type name", JSType.NOT_A_TYPE);
    assertEquals("Named type with empty name component", JSType.EMPTY_TYPE_COMPONENT);
    assertEquals(1, JSType.ENUMDECL);
    assertEquals(0, JSType.NOT_ENUMDECL);
  }
}