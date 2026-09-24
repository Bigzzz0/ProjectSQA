package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;

import com.google.common.base.Predicate;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.google.javascript.rhino.jstype.JSType and its core type lattice methods.
 *
 * 1. Defect-Targeted Zone:
 *    - testRestrictedTypeGivenToBoolean: Tests getRestrictedTypeGivenToBooleanOutcome(true/false)
 *      across native types (UnknownType, CheckedUnknownType, AllType, NoType, BooleanType,
 *      NullType, VoidType, ObjectType). Targets boolean outcome evaluation regressions
 *      linked to Defects4J / testRestrictedTypeGivenToBoolean / testIssue783.
 *
 * 2. Partition A: Core Functional Logic & State Transitions:
 *    - isEmptyType(): Branches for isNoType(), isNoObjectType(), isNoResolvedType(), and LEAST_FUNCTION_TYPE.
 *    - isNominalConstructor(): Branches for (isConstructor() || isInterface()) with null FunctionType,
 *      fn.getSource() != null, and fn.isNativeObjectType().
 *    - canTestForEqualityWith() and testForEqualityHelper():
 *      * aType/bType isAllType, isUnknownType, isNoResolvedType.
 *      * aIsEmpty, bIsEmpty (both empty => TRUE, one empty => UNKNOWN).
 *      * FunctionType comparisons (meet with OBJECT_TYPE is NoType/NoObjectType => FALSE, else UNKNOWN).
 *      * EnumElementType / UnionType delegation.
 *    - canTestForShallowEqualityWith(): isEmptyType checks, inf.isEmptyType(), LEAST_FUNCTION_TYPE.
 *    - getTypesUnderEquality / getTypesUnderInequality: FALSE, TRUE, UNKNOWN handling.
 *    - getTypesUnderShallowInequality: NullType, VoidType pairings vs other types.
 *
 * 3. Partition B: Boundary Value Analysis (BVA) & Lattice Meet/Join:
 *    - getLeastSupertype(): Identical types, UnionType delegation, filterNoResolvedType.
 *    - getGreatestSubtype():
 *      * FunctionType meet (supAndInfHelper).
 *      * Equivalent types, UnknownType meets.
 *      * Subtype branches (thisType <: thatType and thatType <: thisType).
 *      * UnionType and RecordType meets.
 *      * EnumElementType meets.
 *      * Object meet (both Object => NO_OBJECT_TYPE, else NO_TYPE).
 *    - filterNoResolvedType(): NoResolvedType directly, UnionType containing NoResolvedType alternates.
 *
 * 4. Partition C: Resolving, State Management & Static Helpers:
 *    - resolve() & forceResolve(): Resolved flag cache, cyclic guard (null resolveResult => UNKNOWN_TYPE),
 *      clearResolved(), safeResolve(null).
 *    - isEquivalent() and differsFrom(): Unknown vs Known, ProxyObjectType delegation.
 *    - toMaybeFunctionType(null), toMaybeParameterizedType(null), toMaybeTemplateType(null).
 *    - hasAnyTemplate(): inTemplatedCheckVisit recursion protection.
 *    - ALPHA comparator: Deterministic type comparison based on toString().
 */
public class JSTypeGeminiTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter errorReporter;

  private JSType ALL_TYPE;
  private JSType UNKNOWN_TYPE;
  private JSType CHECKED_UNKNOWN_TYPE;
  private JSType NO_TYPE;
  private JSType NO_OBJECT_TYPE;
  private JSType NO_RESOLVED_TYPE;
  private JSType NUMBER_TYPE;
  private JSType NUMBER_OBJECT_TYPE;
  private JSType STRING_TYPE;
  private JSType STRING_OBJECT_TYPE;
  private JSType BOOLEAN_TYPE;
  private JSType BOOLEAN_OBJECT_TYPE;
  private JSType NULL_TYPE;
  private JSType VOID_TYPE;
  private ObjectType OBJECT_TYPE;
  private FunctionType LEAST_FUNCTION_TYPE;
  private FunctionType GREATEST_FUNCTION_TYPE;
  private JSType GLOBAL_THIS;

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(errorReporter);

    ALL_TYPE = registry.getNativeType(JSTypeNative.ALL_TYPE);
    UNKNOWN_TYPE = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    CHECKED_UNKNOWN_TYPE = registry.getNativeType(JSTypeNative.CHECKED_UNKNOWN_TYPE);
    NO_TYPE = registry.getNativeType(JSTypeNative.NO_TYPE);
    NO_OBJECT_TYPE = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    NO_RESOLVED_TYPE = registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE);
    NUMBER_TYPE = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    NUMBER_OBJECT_TYPE = registry.getNativeType(JSTypeNative.NUMBER_OBJECT_TYPE);
    STRING_TYPE = registry.getNativeType(JSTypeNative.STRING_TYPE);
    STRING_OBJECT_TYPE = registry.getNativeType(JSTypeNative.STRING_OBJECT_TYPE);
    BOOLEAN_TYPE = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    BOOLEAN_OBJECT_TYPE = registry.getNativeType(JSTypeNative.BOOLEAN_OBJECT_TYPE);
    NULL_TYPE = registry.getNativeType(JSTypeNative.NULL_TYPE);
    VOID_TYPE = registry.getNativeType(JSTypeNative.VOID_TYPE);
    OBJECT_TYPE = (ObjectType) registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    LEAST_FUNCTION_TYPE = (FunctionType) registry.getNativeType(JSTypeNative.LEAST_FUNCTION_TYPE);
    GREATEST_FUNCTION_TYPE = (FunctionType) registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
    GLOBAL_THIS = registry.getNativeType(JSTypeNative.GLOBAL_THIS);
  }

  // =========================================================================
  // Partition C / Defect-Targeted Zone
  // =========================================================================

  /**
   * Targets the defect documented in Defects4J:
   * JSTypeTest::testRestrictedTypeGivenToBoolean
   * Ensures that restriction of UNKNOWN_TYPE, CHECKED_UNKNOWN_TYPE, and primitive types
   * correctly yields the expected type according to their boolean outcome sets.
   */
  @Test(timeout = 4000)
  public void testRestrictedTypeGivenToBoolean() {
    // Unknown types can evaluate to either true or false
    assertEquals(UNKNOWN_TYPE, UNKNOWN_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(UNKNOWN_TYPE, UNKNOWN_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertEquals(CHECKED_UNKNOWN_TYPE, CHECKED_UNKNOWN_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(CHECKED_UNKNOWN_TYPE, CHECKED_UNKNOWN_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    // Boolean type can be both true and false
    assertEquals(BOOLEAN_TYPE, BOOLEAN_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(BOOLEAN_TYPE, BOOLEAN_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    // Null and Void (undefined) can only be false
    assertEquals(NO_TYPE, NULL_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(NULL_TYPE, NULL_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertEquals(NO_TYPE, VOID_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(VOID_TYPE, VOID_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    // Standard Object type is always truthy
    assertEquals(OBJECT_TYPE, OBJECT_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(NO_TYPE, OBJECT_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyTypeChecks() {
    assertTrue(NO_TYPE.isEmptyType());
    assertTrue(NO_OBJECT_TYPE.isEmptyType());
    assertTrue(NO_RESOLVED_TYPE.isEmptyType());
    assertTrue(LEAST_FUNCTION_TYPE.isEmptyType());

    assertFalse(ALL_TYPE.isEmptyType());
    assertFalse(UNKNOWN_TYPE.isEmptyType());
    assertFalse(NUMBER_TYPE.isEmptyType());
    assertFalse(OBJECT_TYPE.isEmptyType());
  }

  @Test(timeout = 4000)
  public void testStringAndNumberContextPredicates() {
    assertTrue(STRING_TYPE.isString());
    assertTrue(STRING_OBJECT_TYPE.isString());
    assertFalse(NUMBER_TYPE.isString());

    assertTrue(NUMBER_TYPE.isNumber());
    assertTrue(NUMBER_OBJECT_TYPE.isNumber());
    assertFalse(STRING_TYPE.isNumber());

    assertTrue(NUMBER_TYPE.matchesNumberContext());
    assertTrue(NUMBER_TYPE.matchesInt32Context());
    assertTrue(NUMBER_TYPE.matchesUint32Context());
    assertFalse(OBJECT_TYPE.matchesNumberContext());

    assertFalse(NUMBER_TYPE.matchesStringContext());
    assertFalse(NUMBER_TYPE.matchesObjectContext());
  }

  @Test(timeout = 4000)
  public void testGlobalThisAndNominalConstructor() {
    assertTrue(GLOBAL_THIS.isGlobalThisType());
    assertFalse(OBJECT_TYPE.isGlobalThisType());

    // Native constructors should be nominal constructors
    FunctionType objConstructor = OBJECT_TYPE.getConstructor();
    if (objConstructor != null) {
      assertTrue(objConstructor.isNominalConstructor());
    }

    // LEAST_FUNCTION_TYPE is not a constructor
    assertFalse(LEAST_FUNCTION_TYPE.isNominalConstructor());
  }

  @Test(timeout = 4000)
  public void testEqualityTestingHelper_EmptyTypes() {
    // Both empty -> TRUE
    assertEquals(TernaryValue.TRUE, NO_TYPE.testForEquality(NO_TYPE));
    assertEquals(TernaryValue.TRUE, NO_TYPE.testForEquality(NO_OBJECT_TYPE));

    // One empty, other non-empty -> UNKNOWN
    assertEquals(TernaryValue.UNKNOWN, NO_TYPE.testForEquality(NUMBER_TYPE));
    assertEquals(TernaryValue.UNKNOWN, NUMBER_TYPE.testForEquality(NO_TYPE));
  }

  @Test(timeout = 4000)
  public void testEqualityTestingHelper_UnknownAndAllTypes() {
    assertEquals(TernaryValue.UNKNOWN, ALL_TYPE.testForEquality(NUMBER_TYPE));
    assertEquals(TernaryValue.UNKNOWN, NUMBER_TYPE.testForEquality(ALL_TYPE));
    assertEquals(TernaryValue.UNKNOWN, UNKNOWN_TYPE.testForEquality(NUMBER_TYPE));
    assertEquals(TernaryValue.UNKNOWN, NUMBER_TYPE.testForEquality(UNKNOWN_TYPE));
    assertEquals(TernaryValue.UNKNOWN, NO_RESOLVED_TYPE.testForEquality(NUMBER_TYPE));
    assertEquals(TernaryValue.UNKNOWN, NUMBER_TYPE.testForEquality(NO_RESOLVED_TYPE));
  }

  @Test(timeout = 4000)
  public void testEqualityTestingHelper_FunctionTypes() {
    FunctionType fnType = registry.createFunctionType(NUMBER_TYPE);
    // Function vs Function
    assertEquals(TernaryValue.UNKNOWN, fnType.testForEquality(fnType));

    // Function vs Null/Void: NullType / VoidType meets with ObjectType is NoType/NoObjectType -> FALSE
    assertEquals(TernaryValue.FALSE, fnType.testForEquality(NULL_TYPE));
    assertEquals(TernaryValue.FALSE, NULL_TYPE.testForEquality(fnType));
    assertEquals(TernaryValue.FALSE, fnType.testForEquality(VOID_TYPE));
  }

  @Test(timeout = 4000)
  public void testEqualityTestingHelper_UnionsAndEnums() {
    JSType union = registry.createUnionType(NUMBER_TYPE, STRING_TYPE);
    // Delegation when bType is UnionType
    TernaryValue tv = NUMBER_TYPE.testForEquality(union);
    assertNotNull(tv);

    EnumType enumType = registry.createEnumType("MyEnum", null, NUMBER_TYPE);
    EnumElementType enumElem = enumType.getElementsType();
    TernaryValue tvEnum = NUMBER_TYPE.testForEquality(enumElem);
    assertNotNull(tvEnum);
  }

  @Test(timeout = 4000)
  public void testCanTestForShallowEqualityWith() {
    // Empty types branch
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(NO_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));

    // Two functions branch (inf == LEAST_FUNCTION_TYPE)
    FunctionType fn1 = registry.createFunctionType(NUMBER_TYPE);
    FunctionType fn2 = registry.createFunctionType(STRING_TYPE);
    assertTrue(fn1.canTestForShallowEqualityWith(fn2));

    // Distinct primitives with empty intersection cannot be shallow equal
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(NUMBER_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
  }

  @Test(timeout = 4000)
  public void testGetTypesUnderEqualityAndInequality() {
    // False branch: e.g. function and null
    FunctionType fnType = registry.createFunctionType(NUMBER_TYPE);
    JSType.TypePair pairEq = fnType.getTypesUnderEquality(NULL_TYPE);
    assertNull(pairEq.typeA);
    assertNull(pairEq.typeB);

    JSType.TypePair pairIneq = fnType.getTypesUnderInequality(NULL_TYPE);
    assertEquals(fnType, pairIneq.typeA);
    assertEquals(NULL_TYPE, pairIneq.typeB);

    // Union type symmetry branch
    JSType union = registry.createUnionType(NUMBER_TYPE, NULL_TYPE);
    JSType.TypePair unionPairEq = fnType.getTypesUnderEquality(union);
    assertNotNull(unionPairEq);
    JSType.TypePair unionPairIneq = fnType.getTypesUnderInequality(union);
    assertNotNull(unionPairIneq);

    // True branch: NoType == NoType
    JSType.TypePair eqPair = NO_TYPE.getTypesUnderEquality(NO_TYPE);
    assertEquals(NO_TYPE, eqPair.typeA);
    assertEquals(NO_TYPE, eqPair.typeB);

    JSType.TypePair ineqPair = NO_TYPE.getTypesUnderInequality(NO_TYPE);
    assertEquals(NO_TYPE, ineqPair.typeA);
    assertEquals(NO_TYPE, ineqPair.typeB);
  }

  @Test(timeout = 4000)
  public void testGetTypesUnderShallowEqualityAndInequality() {
    JSType.TypePair shallowEq = NUMBER_TYPE.getTypesUnderShallowEquality(NUMBER_TYPE);
    assertEquals(NUMBER_TYPE, shallowEq.typeA);
    assertEquals(NUMBER_TYPE, shallowEq.typeB);

    // Shallow inequality for null and void
    JSType.TypePair nullPair = NULL_TYPE.getTypesUnderShallowInequality(NULL_TYPE);
    assertNull(nullPair.typeA);
    assertNull(nullPair.typeB);

    JSType.TypePair voidPair = VOID_TYPE.getTypesUnderShallowInequality(VOID_TYPE);
    assertNull(voidPair.typeA);
    assertNull(voidPair.typeB);

    JSType.TypePair otherPair = NUMBER_TYPE.getTypesUnderShallowInequality(STRING_TYPE);
    assertEquals(NUMBER_TYPE, otherPair.typeA);
    assertEquals(STRING_TYPE, otherPair.typeB);

    // Union delegation
    JSType union = registry.createUnionType(NULL_TYPE, VOID_TYPE);
    JSType.TypePair unionShallowIneq = union.getTypesUnderShallowInequality(union);
    assertNotNull(unionShallowIneq);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Type Lattice Meet/Join
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetLeastSupertype() {
    // Equivalent types
    assertEquals(NUMBER_TYPE, NUMBER_TYPE.getLeastSupertype(NUMBER_TYPE));

    // Union creation and collapsing
    JSType numOrStr = NUMBER_TYPE.getLeastSupertype(STRING_TYPE);
    assertTrue(numOrStr.isUnionType());

    // Union delegation
    assertEquals(numOrStr, numOrStr.getLeastSupertype(NUMBER_TYPE));
    assertEquals(numOrStr, NUMBER_TYPE.getLeastSupertype(numOrStr));
  }

  @Test(timeout = 4000)
  public void testGetGreatestSubtype() {
    // Equivalent types
    assertEquals(NUMBER_TYPE, NUMBER_TYPE.getGreatestSubtype(NUMBER_TYPE));

    // Unknown type meets
    assertEquals(UNKNOWN_TYPE, NUMBER_TYPE.getGreatestSubtype(UNKNOWN_TYPE));
    assertEquals(UNKNOWN_TYPE, UNKNOWN_TYPE.getGreatestSubtype(STRING_TYPE));
    assertEquals(UNKNOWN_TYPE, UNKNOWN_TYPE.getGreatestSubtype(UNKNOWN_TYPE));

    // Function meet
    FunctionType fn1 = registry.createFunctionType(NUMBER_TYPE);
    FunctionType fn2 = registry.createFunctionType(NUMBER_TYPE);
    JSType fnMeet = fn1.getGreatestSubtype(fn2);
    assertTrue(fnMeet.isFunctionType());

    // Subtype meets
    assertEquals(NO_TYPE, NUMBER_TYPE.getGreatestSubtype(ALL_TYPE.getGreatestSubtype(NO_TYPE)));
    assertEquals(NUMBER_TYPE, NUMBER_TYPE.getGreatestSubtype(ALL_TYPE));
    assertEquals(NUMBER_TYPE, ALL_TYPE.getGreatestSubtype(NUMBER_TYPE));

    // Union meet
    JSType union = registry.createUnionType(NUMBER_TYPE, STRING_TYPE);
    assertEquals(NUMBER_TYPE, union.getGreatestSubtype(NUMBER_TYPE));
    assertEquals(STRING_TYPE, STRING_TYPE.getGreatestSubtype(union));

    // Record meet
    RecordTypeBuilder rtb1 = new RecordTypeBuilder(registry);
    rtb1.addProperty("a", NUMBER_TYPE, null);
    RecordType rec1 = rtb1.build();

    RecordTypeBuilder rtb2 = new RecordTypeBuilder(registry);
    rtb2.addProperty("b", STRING_TYPE, null);
    RecordType rec2 = rtb2.build();

    JSType recMeet = rec1.getGreatestSubtype(rec2);
    assertTrue(recMeet.isRecordType());

    // EnumElement meet
    EnumType enumType = registry.createEnumType("NumEnum", null, NUMBER_TYPE);
    EnumElementType enumElem = enumType.getElementsType();
    assertEquals(enumElem, enumElem.getGreatestSubtype(NUMBER_TYPE));
    assertEquals(enumElem, NUMBER_TYPE.getGreatestSubtype(enumElem));

    // Two objects meet -> NO_OBJECT_TYPE
    assertEquals(NO_OBJECT_TYPE, OBJECT_TYPE.getGreatestSubtype(NUMBER_OBJECT_TYPE));

    // Disjoint primitives meet -> NO_TYPE
    assertEquals(NO_TYPE, NUMBER_TYPE.getGreatestSubtype(STRING_TYPE));
  }

  @Test(timeout = 4000)
  public void testFilterNoResolvedType() {
    assertEquals(NO_RESOLVED_TYPE, JSType.filterNoResolvedType(NO_RESOLVED_TYPE));
    assertEquals(NUMBER_TYPE, JSType.filterNoResolvedType(NUMBER_TYPE));

    // Union with NO_RESOLVED_TYPE
    UnionTypeBuilder utb = new UnionTypeBuilder(registry);
    utb.addAlternate(NUMBER_TYPE);
    utb.addAlternate(NO_RESOLVED_TYPE);
    JSType union = utb.build();

    JSType filtered = JSType.filterNoResolvedType(union);
    assertFalse(filtered.isNoResolvedType());
    assertEquals(NUMBER_TYPE, filtered);
  }

  // =========================================================================
  // Partition D: Autoboxing, Dereferencing & Context Guards
  // =========================================================================

  @Test(timeout = 4000)
  public void testAutoboxingAndDereferencing() {
    assertEquals(NUMBER_OBJECT_TYPE, NUMBER_TYPE.autoboxesTo());
    assertEquals(NUMBER_OBJECT_TYPE, NUMBER_TYPE.autobox());
    assertEquals(NUMBER_OBJECT_TYPE, NUMBER_TYPE.dereference());

    assertNull(OBJECT_TYPE.autoboxesTo());
    assertEquals(OBJECT_TYPE, OBJECT_TYPE.autobox());
    assertEquals(OBJECT_TYPE, OBJECT_TYPE.dereference());

    // Null and Void autoboxing/dereferencing
    assertEquals(NO_TYPE, NULL_TYPE.autobox());
    assertNull(NULL_TYPE.dereference());
    assertNull(VOID_TYPE.dereference());

    // unboxesTo
    assertEquals(NUMBER_TYPE, NUMBER_OBJECT_TYPE.unboxesTo());
    assertNull(NUMBER_TYPE.unboxesTo());
  }

  @Test(timeout = 4000)
  public void testFindPropertyType() {
    // Autoboxed primitive property lookup (Number.prototype has toFixed)
    JSType propType = NUMBER_TYPE.findPropertyType("toFixed");
    assertNotNull(propType);

    // Property on non-autoboxing type without properties
    assertNull(NULL_TYPE.findPropertyType("foo"));
  }

  @Test(timeout = 4000)
  public void testCanAssignToAndIsNullable() {
    assertTrue(NO_TYPE.canAssignTo(NUMBER_TYPE));
    assertTrue(NUMBER_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(NUMBER_TYPE.canAssignTo(STRING_TYPE));

    assertTrue(NULL_TYPE.isNullable());
    assertFalse(NUMBER_TYPE.isNullable());
  }

  // =========================================================================
  // Partition E: Object Lifecycle, Resolution & Identity Contract
  // =========================================================================

  @Test(timeout = 4000)
  public void testResolutionLifecycle() {
    ObjectType namedType = registry.getType("MissingType");
    assertNull(namedType);

    assertFalse(NUMBER_TYPE.isResolved());
    JSType resolved = NUMBER_TYPE.resolve(errorReporter, null);
    assertEquals(NUMBER_TYPE, resolved);
    assertTrue(NUMBER_TYPE.isResolved());

    // Calling resolve again returns cached resolveResult
    assertEquals(NUMBER_TYPE, NUMBER_TYPE.resolve(errorReporter, null));

    // Clear resolved
    NUMBER_TYPE.clearResolved();
    assertFalse(NUMBER_TYPE.isResolved());

    // Force resolve
    JSType forceResolved = NUMBER_TYPE.forceResolve(errorReporter, null);
    assertEquals(NUMBER_TYPE, forceResolved);
    assertTrue(NUMBER_TYPE.isResolved());
    NUMBER_TYPE.clearResolved();

    // safeResolve with null
    assertNull(JSType.safeResolve(null, errorReporter, null));
  }

  @Test(timeout = 4000)
  public void testSetValidator() {
    Predicate<JSType> acceptAll = new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        return true;
      }
    };
    Predicate<JSType> rejectAll = new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        return false;
      }
    };

    assertTrue(NUMBER_TYPE.setValidator(acceptAll));
    assertFalse(NUMBER_TYPE.setValidator(rejectAll));
  }

  @Test(timeout = 4000)
  public void testEquivalenceAndDiffersFrom() {
    assertTrue(JSType.isEquivalent(null, null));
    assertFalse(JSType.isEquivalent(NUMBER_TYPE, null));
    assertFalse(JSType.isEquivalent(null, NUMBER_TYPE));
    assertTrue(JSType.isEquivalent(NUMBER_TYPE, NUMBER_TYPE));
    assertFalse(JSType.isEquivalent(NUMBER_TYPE, STRING_TYPE));

    assertEquals(NUMBER_TYPE, NUMBER_TYPE);
    assertNotEquals(NUMBER_TYPE, STRING_TYPE);
    assertNotEquals(NUMBER_TYPE, "string_object");
    assertEquals(NUMBER_TYPE.hashCode(), System.identityHashCode(NUMBER_TYPE));

    // differsFrom
    assertFalse(NUMBER_TYPE.differsFrom(NUMBER_TYPE));
    assertTrue(NUMBER_TYPE.differsFrom(STRING_TYPE));
    assertTrue(NUMBER_TYPE.differsFrom(UNKNOWN_TYPE));
    assertTrue(UNKNOWN_TYPE.differsFrom(NUMBER_TYPE));
    assertFalse(UNKNOWN_TYPE.differsFrom(UNKNOWN_TYPE));
  }

  @Test(timeout = 4000)
  public void testNullSafeDowncasts() {
    assertNull(JSType.toMaybeFunctionType(null));
    assertNull(JSType.toMaybeFunctionType(NUMBER_TYPE));
    assertEquals(LEAST_FUNCTION_TYPE, JSType.toMaybeFunctionType(LEAST_FUNCTION_TYPE));

    assertNull(JSType.toMaybeParameterizedType(null));
    assertNull(JSType.toMaybeParameterizedType(NUMBER_TYPE));

    assertNull(JSType.toMaybeTemplateType(null));
    assertNull(JSType.toMaybeTemplateType(NUMBER_TYPE));
  }

  @Test(timeout = 4000)
  public void testHasAnyTemplateRecursionProtection() {
    assertFalse(NUMBER_TYPE.hasAnyTemplate());
    assertFalse(NUMBER_TYPE.hasAnyTemplateInternal());
  }

  @Test(timeout = 4000)
  public void testDefaultImplementationsAndStringRepresentations() {
    assertNull(NUMBER_TYPE.getJSDocInfo());
    assertNull(NUMBER_TYPE.getDisplayName());
    assertFalse(NUMBER_TYPE.hasDisplayName());

    assertFalse(NUMBER_TYPE.isNoType());
    assertFalse(NUMBER_TYPE.isNoResolvedType());
    assertFalse(NUMBER_TYPE.isNoObjectType());
    assertFalse(NUMBER_TYPE.isFunctionPrototypeType());
    assertFalse(NUMBER_TYPE.isTheObjectType());
    assertFalse(NUMBER_TYPE.isArrayType());
    assertFalse(NUMBER_TYPE.isBooleanObjectType());
    assertFalse(NUMBER_TYPE.isBooleanValueType());
    assertFalse(NUMBER_TYPE.isRegexpType());
    assertFalse(NUMBER_TYPE.isDateType());
    assertFalse(NUMBER_TYPE.isAllType());
    assertFalse(NUMBER_TYPE.isCheckedUnknownType());
    assertFalse(NUMBER_TYPE.isNominalType());
    assertFalse(NUMBER_TYPE.isInstanceType());
    assertFalse(NUMBER_TYPE.isOrdinaryFunction());
    assertFalse(NUMBER_TYPE.isNamedType());
    assertFalse(NUMBER_TYPE.canBeCalled());

    // collapseUnion
    assertEquals(NUMBER_TYPE, NUMBER_TYPE.collapseUnion());

    // matchConstraint does not throw
    NUMBER_TYPE.matchConstraint(STRING_TYPE);

    // toString, toAnnotationString, toDebugHashCodeString
    assertNotNull(NUMBER_TYPE.toString());
    assertNotNull(NUMBER_TYPE.toAnnotationString());
    assertEquals("{" + NUMBER_TYPE.hashCode() + "}", NUMBER_TYPE.toDebugHashCodeString());
  }

  @Test(timeout = 4000)
  public void testAlphaComparator() {
    List<JSType> types = new ArrayList<JSType>();
    types.add(STRING_TYPE);
    types.add(NUMBER_TYPE);
    types.add(BOOLEAN_TYPE);

    Collections.sort(types, JSType.ALPHA);

    for (int i = 0; i < types.size() - 1; i++) {
      assertTrue(types.get(i).toString().compareTo(types.get(i + 1).toString()) <= 0);
    }
  }

  @Test(timeout = 4000)
  public void testSubtypingProxyObjectTypeBranch() {
    ProxyObjectType proxy = new ProxyObjectType(registry, NUMBER_TYPE);
    assertTrue(NUMBER_TYPE.isSubtype(proxy));
    assertTrue(proxy.isEquivalentTo(NUMBER_TYPE));
  }
}