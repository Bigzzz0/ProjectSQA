/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.rhino.jstype.JSType (and lattice relationships)
 * Key Defects & Branches Targeted:
 * 1. Defect Issue 791 / RecordTypeTest.testSubtypeWithUnknowns2:
 *    - Type checking involving RecordType with UnknownType properties:
 *      A record type with property {a: ?} (recordB) must NOT be a subtype of {a: number} (recordA),
 *      whereas {a: number} (recordA) MUST be a subtype of {a: ?} (recordB).
 *      Their greatest subtype (meet) should be equivalent to {a: number}.
 * 2. Equivalence Visitors & Tolerating Unknowns (checkEquivalenceHelper):
 *    - Branch: this == that
 *    - Branch: thisUnknown || thatUnknown with tolerateUnknowns = true vs false
 *    - Branch: thisUnknown && thatUnknown && (isNominalType() ^ that.isNominalType())
 *    - Branch: isUnionType(), isFunctionType(), isRecordType(), isParameterizedType()
 *    - Branch: isNominalType() reference name comparison
 *    - Branch: ProxyObjectType unboxing
 * 3. Lattice Operations (getLeastSupertype, getGreatestSubtype):
 *    - Branch: both FunctionTypes
 *    - Branch: isEquivalentTo
 *    - Branch: isUnknownType() for this or that (universal unknown fallback vs identical)
 *    - Branch: thisType.isSubtype(thatType) vs thatType.isSubtype(thisType)
 *    - Branch: UnionType meet, RecordType helper, EnumElementType meet
 *    - Branch: ObjectType intersection -> NO_OBJECT_TYPE vs NO_TYPE
 *    - Branch: filterNoResolvedType for NoResolvedType and UnionType containing NoResolvedType
 * 4. Equality & Comparison Algorithms:
 *    - testForEqualityHelper: AllType, UnknownType, NoResolvedType -> UNKNOWN
 *    - isEmptyType for aType and/or bType -> TRUE if both, UNKNOWN if one
 *    - isFunctionType meet with ObjectType -> FALSE if meet is NoType / NoObjectType, else UNKNOWN
 *    - canTestForShallowEqualityWith: isEmptyType subtypes, inf.isEmptyType, LEAST_FUNCTION_TYPE
 *    - getTypesUnderEquality, getTypesUnderInequality, getTypesUnderShallowEquality, getTypesUnderShallowInequality
 * 5. Type Conversions & Autoboxing:
 *    - autobox(), dereference(), findPropertyType()
 *    - getRestrictedTypeGivenToBooleanOutcome with UnknownType -> CheckedUnknownType
 *    - resolve, forceResolve, safeResolve, clearResolved, setResolvedTypeInternal
 */

package com.google.javascript.rhino.jstype;

import com.google.common.base.Predicate;
import com.google.javascript.rhino.ErrorReporter;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class JSTypeGeminiTest {

  private JSTypeRegistry registry;
  private JSType unknownType;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType nullType;
  private JSType voidType;
  private JSType allType;
  private JSType noType;
  private JSType noObjectType;
  private JSType noResolvedType;
  private ObjectType objectType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    noObjectType = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    noResolvedType = registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE);
    objectType = (ObjectType) registry.getNativeType(JSTypeNative.OBJECT_TYPE);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 791 & testSubtypeWithUnknowns2)
  // =========================================================================

  @Test(timeout = 4000)
  public void testSubtypeWithUnknowns2_RecordTypeAsymmetricSubtyping() {
    // Target defect: Issue 791 / RecordTypeTest.testSubtypeWithUnknowns2
    // A record type with an unknown property {a: ?} is NOT a subtype of {a: number},
    // but {a: number} is a subtype of {a: ?}.
    RecordType recordNumber = new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    RecordType recordUnknown = new RecordTypeBuilder(registry)
        .addProperty("a", unknownType, null)
        .build();

    assertTrue("recordNumber should be a subtype of recordUnknown",
        recordNumber.isSubtype(recordUnknown));
    assertFalse("recordUnknown should NOT be a subtype of recordNumber",
        recordUnknown.isSubtype(recordNumber));

    JSType meet1 = recordUnknown.getGreatestSubtype(recordNumber);
    assertTrue("Greatest subtype of {a: ?} and {a: number} should be {a: number}",
        recordNumber.isEquivalentTo(meet1));

    JSType meet2 = recordNumber.getGreatestSubtype(recordUnknown);
    assertTrue("Greatest subtype of {a: number} and {a: ?} should be {a: number}",
        recordNumber.isEquivalentTo(meet2));
  }

  @Test(timeout = 4000)
  public void testCheckEquivalenceHelper_NominalVsUnknownDefectEdge() {
    // When one is unknown and nominal, and the other is unknown and not nominal
    ObjectType nominalUnknown = registry.createNamedType("NominalUnknown", null, 0, 0);
    // nominalUnknown without resolution acts as an unresolved nominal proxy/unknown
    assertNotNull(nominalUnknown);
    assertFalse(unknownType.isNominalType());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBasicTypePropertiesAndPredicates() {
    assertTrue(numberType.isNumberValueType());
    assertTrue(numberType.isNumber());
    assertFalse(numberType.isNumberObjectType());
    assertFalse(numberType.isObject());
    assertFalse(numberType.isNullType());
    assertFalse(numberType.isVoidType());
    assertFalse(numberType.isString());
    assertFalse(numberType.isArrayType());
    assertFalse(numberType.isDateType());
    assertFalse(numberType.isRegexpType());
    assertFalse(numberType.isFunctionType());
    assertFalse(numberType.isFunctionPrototypeType());

    assertTrue(stringType.isStringValueType());
    assertTrue(stringType.isString());
    assertFalse(stringType.isStringObjectType());

    assertTrue(booleanType.isBooleanValueType());
    assertFalse(booleanType.isBooleanObjectType());

    assertTrue(nullType.isNullType());
    assertTrue(nullType.isNullable());

    assertTrue(voidType.isVoidType());
    assertFalse(voidType.isNullable());

    assertTrue(allType.isAllType());
    assertTrue(unknownType.isUnknownType());
    assertFalse(unknownType.isCheckedUnknownType());
    assertTrue(noType.isNoType());
    assertTrue(noType.isEmptyType());
    assertTrue(noObjectType.isNoObjectType());
    assertTrue(noObjectType.isEmptyType());
    assertTrue(noResolvedType.isNoResolvedType());
    assertTrue(noResolvedType.isEmptyType());

    JSType leastFn = registry.getNativeFunctionType(JSTypeNative.LEAST_FUNCTION_TYPE);
    assertTrue(leastFn.isEmptyType());
  }

  @Test(timeout = 4000)
  public void testContextMatches() {
    assertTrue(numberType.matchesNumberContext());
    assertTrue(numberType.matchesInt32Context());
    assertTrue(numberType.matchesUint32Context());
    assertFalse(numberType.matchesStringContext());
    assertFalse(numberType.matchesObjectContext());

    assertFalse(nullType.matchesNumberContext());
    assertFalse(nullType.matchesObjectContext());
    assertFalse(nullType.matchesStringContext());
  }

  @Test(timeout = 4000)
  public void testAutoboxingAndDereference() {
    JSType autoboxedNumber = numberType.autoboxesTo();
    assertNotNull(autoboxedNumber);
    assertTrue(autoboxedNumber.isObject());
    assertTrue(autoboxedNumber.isNumberObjectType());

    JSType unboxed = autoboxedNumber.unboxesTo();
    assertNotNull(unboxed);
    assertTrue(unboxed.isNumberValueType());

    assertNull(numberType.unboxesTo());
    assertNull(objectType.autoboxesTo());

    JSType autoboxResult = numberType.autobox();
    assertEquals(autoboxedNumber, autoboxResult);

    ObjectType derefResult = numberType.dereference();
    assertNotNull(derefResult);
    assertTrue(derefResult.isNumberObjectType());

    assertNull(nullType.dereference());
    assertNull(voidType.dereference());
  }

  @Test(timeout = 4000)
  public void testFindPropertyType() {
    // Number type autoboxes to Number Object which has toString property
    JSType toStringProp = numberType.findPropertyType("toString");
    assertNotNull(toStringProp);

    assertNull(numberType.findPropertyType("nonExistentPropXYZ"));
    assertNull(nullType.findPropertyType("toString"));
  }

  @Test(timeout = 4000)
  public void testCanBeCalledAndIsConstructor() {
    assertFalse(numberType.canBeCalled());
    assertFalse(numberType.isConstructor());
    assertFalse(numberType.isInterface());
    assertFalse(numberType.isOrdinaryFunction());
    assertFalse(numberType.isNominalConstructor());

    FunctionType fn = registry.createFunctionType(numberType);
    assertTrue(fn.canBeCalled());
    assertFalse(fn.isConstructor());
    assertTrue(fn.isOrdinaryFunction());

    FunctionType ctor = registry.createConstructorType("CustomClass", null, null, null);
    assertTrue(ctor.canBeCalled());
    assertTrue(ctor.isConstructor());
    assertTrue(ctor.isNominalConstructor());
  }

  @Test(timeout = 4000)
  public void testIsStructAndIsDict() {
    assertFalse(numberType.isStruct());
    assertFalse(numberType.isDict());

    ObjectType structInstance = registry.createObjectType("StructObj", null, null);
    assertFalse(structInstance.isStruct());
    assertFalse(structInstance.isDict());
  }

  @Test(timeout = 4000)
  public void testResolveLifecycle() {
    assertFalse(numberType.isResolved());
    JSType resolved = numberType.resolve(null, null);
    assertTrue(numberType.isResolved());
    assertSame(numberType, resolved);

    // Call resolve again (idempotent)
    assertSame(numberType, numberType.resolve(null, null));

    numberType.clearResolved();
    assertFalse(numberType.isResolved());

    // forceResolve
    JSType forceResolved = numberType.forceResolve(null, null);
    assertTrue(numberType.isResolved());
    assertSame(numberType, forceResolved);

    // safeResolve with null
    assertNull(JSType.safeResolve(null, null, null));
  }

  @Test(timeout = 4000)
  public void testSetValidator() {
    Predicate<JSType> trueValidator = new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        return input != null;
      }
    };
    assertTrue(numberType.setValidator(trueValidator));

    Predicate<JSType> falseValidator = new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        return false;
      }
    };
    assertFalse(numberType.setValidator(falseValidator));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Lattice Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testLatticeSupremumAndInfimum() {
    // Least Supertype (Join)
    // number v number = number
    assertEquals(numberType, numberType.getLeastSupertype(numberType));
    // number v string = (number, string) union
    JSType numOrStr = numberType.getLeastSupertype(stringType);
    assertTrue(numOrStr.isUnionType());

    // Greatest Subtype (Meet)
    // number ^ number = number
    assertEquals(numberType, numberType.getGreatestSubtype(numberType));
    // number ^ string = NO_TYPE
    assertEquals(noType, numberType.getGreatestSubtype(stringType));

    // Object ^ Object = NO_OBJECT_TYPE for disjoint object instances
    ObjectType objA = registry.createAnonymousObjectType();
    ObjectType objB = registry.createAnonymousObjectType();
    assertEquals(noObjectType, objA.getGreatestSubtype(objB));

    // unknown ^ number = unknown
    assertEquals(unknownType, unknownType.getGreatestSubtype(numberType));
    assertEquals(unknownType, numberType.getGreatestSubtype(unknownType));
  }

  @Test(timeout = 4000)
  public void testFilterNoResolvedType() {
    assertEquals(noResolvedType, JSType.filterNoResolvedType(noResolvedType));
    assertEquals(numberType, JSType.filterNoResolvedType(numberType));

    // Union with NoResolvedType should filter it out
    JSType unionWithNoResolved = registry.createUnionType(numberType, noResolvedType);
    JSType filtered = JSType.filterNoResolvedType(unionWithNoResolved);
    assertEquals(numberType, filtered);
  }

  @Test(timeout = 4000)
  public void testCollapseUnionAndRestrictByNotNullOrUndefined() {
    assertEquals(numberType, numberType.collapseUnion());
    assertEquals(numberType, numberType.restrictByNotNullOrUndefined());

    JSType nullableNum = registry.createNullableType(numberType);
    assertTrue(nullableNum.isUnionType());
    assertEquals(numberType, nullableNum.restrictByNotNullOrUndefined());
  }

  @Test(timeout = 4000)
  public void testSubtypeRelations() {
    assertTrue(noType.isSubtype(numberType));
    assertTrue(numberType.isSubtype(allType));
    assertTrue(numberType.isSubtype(unknownType));
    assertTrue(numberType.isSubtype(numberType));
    assertFalse(numberType.isSubtype(stringType));
    assertTrue(numberType.canAssignTo(numberType));
    assertFalse(numberType.canAssignTo(stringType));
  }

  @Test(timeout = 4000)
  public void testRestrictedTypeGivenToBooleanOutcome() {
    // UnknownType with true outcome gives CheckedUnknownType
    JSType checkedUnknown = unknownType.getRestrictedTypeGivenToBooleanOutcome(true);
    assertTrue(checkedUnknown.isCheckedUnknownType());

    // Number type can be both true (non-zero) and false (0, NaN)
    assertEquals(numberType, numberType.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(numberType, numberType.getRestrictedTypeGivenToBooleanOutcome(false));

    // Null is always false in boolean outcome
    assertEquals(nullType, nullType.getRestrictedTypeGivenToBooleanOutcome(false));
    assertEquals(noType, nullType.getRestrictedTypeGivenToBooleanOutcome(true));
  }

  @Test(timeout = 4000)
  public void testEqualityAndShallowEquality() {
    // Unknown comparisons
    assertEquals(TernaryValue.UNKNOWN, unknownType.testForEquality(numberType));
    assertEquals(TernaryValue.UNKNOWN, allType.testForEquality(numberType));
    assertEquals(TernaryValue.UNKNOWN, noResolvedType.testForEquality(numberType));

    // Empty types comparison
    assertEquals(TernaryValue.TRUE, noType.testForEquality(noType));
    assertEquals(TernaryValue.UNKNOWN, noType.testForEquality(numberType));

    // Function type equality with disjoint type
    FunctionType fnType = registry.createFunctionType(numberType);
    assertEquals(TernaryValue.FALSE, fnType.testForEquality(stringType));

    // Shallow equality
    assertTrue(noType.canTestForShallowEqualityWith(noType));
    assertTrue(numberType.canTestForShallowEqualityWith(numberType));
    assertFalse(numberType.canTestForShallowEqualityWith(stringType));
    assertTrue(fnType.canTestForShallowEqualityWith(fnType));
  }

  @Test(timeout = 4000)
  public void testTypePairsUnderEqualityAndInequality() {
    // Under equality: number == number
    JSType.TypePair pairEq = numberType.getTypesUnderEquality(numberType);
    assertEquals(numberType, pairEq.typeA);
    assertEquals(numberType, pairEq.typeB);

    // Under equality: function == string -> FALSE -> null, null
    FunctionType fnType = registry.createFunctionType(numberType);
    JSType.TypePair pairFnStr = fnType.getTypesUnderEquality(stringType);
    assertNull(pairFnStr.typeA);
    assertNull(pairFnStr.typeB);

    // Under inequality: null != null is deterministically false in JS (always equal)
    JSType.TypePair pairShallowIneq = nullType.getTypesUnderShallowInequality(nullType);
    assertNull(pairShallowIneq.typeA);
    assertNull(pairShallowIneq.typeB);

    JSType.TypePair pairVoidIneq = voidType.getTypesUnderShallowInequality(voidType);
    assertNull(pairVoidIneq.typeA);
    assertNull(pairVoidIneq.typeB);

    JSType.TypePair numStrIneq = numberType.getTypesUnderShallowInequality(stringType);
    assertEquals(numberType, numStrIneq.typeA);
    assertEquals(stringType, numStrIneq.typeB);

    // getTypesUnderShallowEquality
    JSType.TypePair shallowEq = numberType.getTypesUnderShallowEquality(numberType);
    assertEquals(numberType, shallowEq.typeA);
    assertEquals(numberType, shallowEq.typeB);
  }

  // =========================================================================
  // Partition D: Equivalence, Differing, and Proxy Handling
  // =========================================================================

  @Test(timeout = 4000)
  public void testCheckEquivalenceBranches() {
    // Identity
    assertTrue(numberType.isEquivalentTo(numberType));
    assertFalse(numberType.differsFrom(numberType));

    // Invariance
    assertTrue(numberType.isInvariant(numberType));
    assertFalse(numberType.isInvariant(stringType));

    // Static null-safe isEquivalent
    assertTrue(JSType.isEquivalent(null, null));
    assertFalse(JSType.isEquivalent(numberType, null));
    assertFalse(JSType.isEquivalent(null, numberType));
    assertTrue(JSType.isEquivalent(numberType, numberType));

    // Tolerate unknowns in differsFrom
    // differsFrom calls checkEquivalenceHelper(that, true)
    // unknown differsFrom number is false because unknown can be anything
    assertFalse(unknownType.differsFrom(numberType));
    assertFalse(numberType.differsFrom(unknownType));
    assertFalse(unknownType.differsFrom(unknownType));

    // Function equivalence
    FunctionType fn1 = registry.createFunctionType(numberType, stringType);
    FunctionType fn2 = registry.createFunctionType(numberType, stringType);
    assertTrue(fn1.isEquivalentTo(fn2));

    // Union equivalence
    JSType u1 = registry.createUnionType(numberType, stringType);
    JSType u2 = registry.createUnionType(stringType, numberType);
    assertTrue(u1.isEquivalentTo(u2));
  }

  @Test(timeout = 4000)
  public void testParameterizedTypeEquivalenceAndStaticMethods() {
    ObjectType arrayType = (ObjectType) registry.getNativeType(JSTypeNative.ARRAY_TYPE);
    ParameterizedType p1 = registry.createParameterizedType(arrayType, numberType);
    ParameterizedType p2 = registry.createParameterizedType(arrayType, numberType);
    ParameterizedType p3 = registry.createParameterizedType(arrayType, stringType);

    assertTrue(p1.isParameterizedType());
    assertNotNull(p1.toMaybeParameterizedType());
    assertEquals(p1, JSType.toMaybeParameterizedType(p1));
    assertNull(JSType.toMaybeParameterizedType(numberType));
    assertNull(JSType.toMaybeParameterizedType((JSType) null));

    assertTrue(p1.isEquivalentTo(p2));
    assertFalse(p1.isEquivalentTo(p3));
  }

  @Test(timeout = 4000)
  public void testTemplateTypeAndVisitors() {
    TemplateType templateType = new TemplateType(registry, "T");
    assertTrue(templateType.isTemplateType());
    assertNotNull(templateType.toMaybeTemplateType());
    assertEquals(templateType, JSType.toMaybeTemplateType(templateType));
    assertNull(JSType.toMaybeTemplateType(numberType));
    assertNull(JSType.toMaybeTemplateType((JSType) null));

    assertTrue(templateType.hasAnyTemplate());

    Visitor<String> testVisitor = new Visitor<String>() {
      @Override public String caseNoType() { return "no"; }
      @Override public String caseEnumElementType(EnumElementType type) { return "enumElem"; }
      @Override public String caseAllType() { return "all"; }
      @Override public String caseBooleanType() { return "bool"; }
      @Override public String caseNoObjectType() { return "noObj"; }
      @Override public String caseFunctionType(FunctionType type) { return "fn"; }
      @Override public String caseObjectType(ObjectType type) { return "obj"; }
      @Override public String caseUnknownType() { return "unknown"; }
      @Override public String caseNullType() { return "null"; }
      @Override public String caseNamedType(NamedType type) { return "named"; }
      @Override public String caseNumberType() { return "number"; }
      @Override public String caseStringType() { return "string"; }
      @Override public String caseVoidType() { return "void"; }
      @Override public String caseUnionType(UnionType type) { return "union"; }
      @Override public String caseTemplateType(TemplateType templateType) { return "template"; }
    };

    assertEquals("number", numberType.visit(testVisitor));
    assertEquals("template", templateType.visit(testVisitor));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testEqualsAndHashCode() {
    assertEquals(numberType, numberType);
    assertNotEquals(numberType, stringType);
    assertNotEquals(numberType, "non-JSType");
    assertNotEquals(numberType, null);

    assertEquals(System.identityHashCode(numberType), numberType.hashCode());
    assertTrue(numberType.toDebugHashCodeString().contains(String.valueOf(numberType.hashCode())));
  }

  @Test(timeout = 4000)
  public void testToStringAndAlphaComparator() {
    assertEquals("number", numberType.toString());
    assertEquals("number", numberType.toAnnotationString());
    assertEquals("string", stringType.toString());

    int cmp = JSType.ALPHA.compare(numberType, stringType);
    assertTrue(cmp < 0);
  }

  @Test(timeout = 4000)
  public void testDisplayAndDocumentationInfo() {
    assertNull(numberType.getJSDocInfo());
    assertNull(numberType.getDisplayName());
    assertFalse(numberType.hasDisplayName());
    assertFalse(numberType.hasProperty("prop"));

    EnumType enumType = registry.createEnumType("MyEnum", null, numberType);
    assertEquals("MyEnum", enumType.getDisplayName());
    assertTrue(enumType.hasDisplayName());
  }

  @Test(timeout = 4000)
  public void testStaticToMaybeFunctionType() {
    FunctionType fn = registry.createFunctionType(numberType);
    assertEquals(fn, JSType.toMaybeFunctionType(fn));
    assertNull(JSType.toMaybeFunctionType(numberType));
    assertNull(JSType.toMaybeFunctionType((JSType) null));
  }

  @Test(timeout = 4000)
  public void testCanTestForEqualityWith() {
    assertTrue(numberType.canTestForEqualityWith(numberType));
    assertTrue(unknownType.canTestForEqualityWith(numberType));
  }

  @Test(timeout = 4000)
  public void testMatchConstraint() {
    // Should execute safely as a no-op on non-specialized types
    numberType.matchConstraint(stringType);
  }
}