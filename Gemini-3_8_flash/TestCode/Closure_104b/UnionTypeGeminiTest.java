package com.google.javascript.rhino.jstype;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.jstype.JSType.TypePair;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================
 * Target Class: com.google.javascript.rhino.jstype.UnionType
 * Target Environment: Java 8 / JUnit 4
 *
 * Specific Defect Targeted (Defects4J Ground Truth):
 * - UnionTypeTest::testGreatestSubtypeUnionTypes5:
 *   expected:<NoObject> but was:<None>
 *   Root cause: When computing meet/greatest subtype between a union type containing an
 *   object and a primitive (e.g. (NumberObject, number) or (Date, number)) and a disjoint
 *   object type (e.g. StringObject or RegExp), meet() fails to recognize that the object
 *   parts intersect at NO_OBJECT_TYPE because this.isObject() evaluates to false due to the
 *   presence of primitive alternates, causing a premature fallback to NO_TYPE ("None").
 *
 * Branch & Condition Coverage Matrix:
 * - matchesNumberContext(): alternates with matching / non-matching types / empty alternates.
 * - matchesStringContext(): VOID_TYPE (false) vs non-void alternates (true).
 * - matchesObjectContext(): NULL_TYPE and VOID_TYPE (false) vs ObjectType (true).
 * - findPropertyType(): null/void types skipped, altPropertyType null skipped, least-supertype
 *   aggregation across multiple property matches, property not found.
 * - canAssignTo(): unknown alternate (short-circuit true), all assignable (true), one failing (false).
 * - canBeCalled(): all alternates callable (true), at least one non-callable (false).
 * - testForEquality(): first alternate null initialization, consistent equality results,
 *   divergent equality results returning UNKNOWN, empty set returning null.
 * - isNullable() / isUnknownType() / isObject(): any true, all true, none true.
 * - getLeastSupertype(): that.isUnknownType(), alternate.isUnknownType(), that.isSubtype(alternate),
 *   and fallback to JSType.getLeastSupertype().
 * - meet(): alternate.isSubtype(that), that instanceof UnionType, that.isSubtype(this),
 *   this.isObject() && that.isObject() -> NO_OBJECT_TYPE, fallback -> NO_TYPE.
 * - getRestrictedUnion(): t.isUnknownType(), !t.isSubtype(type), and excluded subtypes.
 * - getPossibleToBooleanOutcomes(): early termination on BOTH, only TRUE, only FALSE.
 * - resolveInternal(): changed == false path vs changed == true path.
 * - equals() & hashCode(): reflexive, symmetric, non-UnionType object, null, different sets.
 * ====================================================================================
 */
public class UnionTypeGeminiTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter errorReporter;

  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType nullType;
  private JSType voidType;
  private JSType unknownType;
  private JSType allType;
  private JSType noType;
  private JSType noObjectType;

  private ObjectType objectType;
  private ObjectType dateType;
  private ObjectType regExpType;
  private ObjectType arrayType;
  private ObjectType numberObjectType;
  private ObjectType stringObjectType;

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(errorReporter);

    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    noObjectType = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);

    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    dateType = registry.getNativeObjectType(JSTypeNative.DATE_TYPE);
    regExpType = registry.getNativeObjectType(JSTypeNative.REGEXP_TYPE);
    arrayType = registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);
    numberObjectType = registry.getNativeObjectType(JSTypeNative.NUMBER_OBJECT_TYPE);
    stringObjectType = registry.getNativeObjectType(JSTypeNative.STRING_OBJECT_TYPE);
  }

  private UnionType createUnion(JSType... types) {
    return new UnionType(registry, ImmutableSet.copyOf(types));
  }

  // ==========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // ==========================================================================

  /**
   * Targets defects4j failure:
   * com.google.javascript.rhino.jstype.UnionTypeTest::testGreatestSubtypeUnionTypes5
   * Failure: expected:<NoObject> but was:<None>
   *
   * A union with an object alternate and a primitive alternate when met with a disjoint
   * object type must evaluate their greatest subtype to NoObject, not None.
   */
  @Test(timeout = 4000)
  public void testGreatestSubtypeUnionTypes5() {
    JSType unionType = registry.createUnionType(numberObjectType, numberType);
    JSType result = unionType.getGreatestSubtype(stringObjectType);
    assertEquals("Greatest subtype of (Number, number) and String should be NoObject",
        noObjectType, result);
  }

  @Test(timeout = 4000)
  public void testGreatestSubtypeDisjointObjectAndPrimitiveAlternates() {
    JSType unionType = registry.createUnionType(dateType, numberType);
    JSType result = unionType.getGreatestSubtype(regExpType);
    assertEquals("Greatest subtype of (Date, number) and RegExp should be NoObject",
        noObjectType, result);
  }

  @Test(timeout = 4000)
  public void testGreatestSubtypeTwoUnionsWithDisjointObjectsAndPrimitives() {
    JSType u1 = registry.createUnionType(dateType, numberType);
    JSType u2 = registry.createUnionType(regExpType, stringType);
    JSType result = u1.getGreatestSubtype(u2);
    assertEquals("Greatest subtype of (Date, number) and (RegExp, string) should be NoObject",
        noObjectType, result);
  }

  // ==========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // ==========================================================================

  @Test(timeout = 4000)
  public void testGetAlternatesAndContains() {
    UnionType union = createUnion(numberType, stringType);
    assertTrue(union.isUnionType());
    assertTrue(union.contains(numberType));
    assertTrue(union.contains(stringType));
    assertFalse(union.contains(booleanType));

    Set<JSType> alternates = new HashSet<JSType>();
    for (JSType alt : union.getAlternates()) {
      alternates.add(alt);
    }
    assertEquals(2, alternates.size());
    assertTrue(alternates.contains(numberType));
    assertTrue(alternates.contains(stringType));
  }

  @Test(timeout = 4000)
  public void testMatchesContextPredicates() {
    UnionType numOrStr = createUnion(numberType, stringType);
    assertTrue(numOrStr.matchesNumberContext());
    assertTrue(numOrStr.matchesStringContext());
    assertTrue(numOrStr.matchesObjectContext());

    UnionType voidOrNull = createUnion(voidType, nullType);
    assertFalse(voidOrNull.matchesObjectContext());

    UnionType onlyVoid = createUnion(voidType);
    assertFalse(onlyVoid.matchesStringContext());
  }

  @Test(timeout = 4000)
  public void testCanAssignTo() {
    UnionType numOrStr = createUnion(numberType, stringType);
    assertTrue(numOrStr.canAssignTo(allType));
    assertFalse(numOrStr.canAssignTo(numberType));

    // When an alternate is unknown type, canAssignTo must short-circuit to true
    UnionType unknownOrNum = createUnion(unknownType, numberType);
    assertTrue(unknownOrNum.canAssignTo(stringType));
  }

  @Test(timeout = 4000)
  public void testCanBeCalled() {
    FunctionType fn1 = registry.createFunctionType(numberType);
    FunctionType fn2 = registry.createFunctionType(stringType);

    UnionType fns = createUnion(fn1, fn2);
    assertTrue(fns.canBeCalled());

    UnionType fnOrNum = createUnion(fn1, numberType);
    assertFalse(fnOrNum.canBeCalled());
  }

  @Test(timeout = 4000)
  public void testRestrictByNotNullOrUndefined() {
    UnionType union = createUnion(numberType, nullType, voidType);
    JSType restricted = union.restrictByNotNullOrUndefined();
    assertEquals(numberType, restricted);

    UnionType objectUnion = createUnion(objectType, nullType);
    assertEquals(objectType, objectUnion.restrictByNotNullOrUndefined());
  }

  @Test(timeout = 4000)
  public void testTestForEquality() {
    UnionType numOrStr = createUnion(numberType, stringType);
    // Both number and string test FALSE against null
    assertEquals(TernaryValue.FALSE, numOrStr.testForEquality(nullType));

    // Null is TRUE with null, string is FALSE with null -> divergence yields UNKNOWN
    UnionType nullOrStr = createUnion(nullType, stringType);
    assertEquals(TernaryValue.UNKNOWN, nullOrStr.testForEquality(nullType));
  }

  @Test(timeout = 4000)
  public void testIsNullableAndIsUnknownTypeAndIsObject() {
    UnionType nullable = createUnion(numberType, nullType);
    assertTrue(nullable.isNullable());
    assertFalse(nullable.isUnknownType());
    assertFalse(nullable.isObject());

    UnionType unknownUnion = createUnion(unknownType, numberType);
    assertFalse(unknownUnion.isNullable());
    assertTrue(unknownUnion.isUnknownType());
    assertFalse(unknownUnion.isObject());

    UnionType objects = createUnion(dateType, regExpType);
    assertFalse(objects.isNullable());
    assertFalse(objects.isUnknownType());
    assertTrue(objects.isObject());
  }

  @Test(timeout = 4000)
  public void testGetLeastSupertype() {
    UnionType numOrDate = createUnion(numberType, dateType);

    // That is unknown type path
    assertEquals(unknownType, numOrDate.getLeastSupertype(unknownType));

    // That is subtype of an alternate in union -> returns this
    JSType leastWithDate = numOrDate.getLeastSupertype(dateType);
    assertSame(numOrDate, leastWithDate);

    // Alternate is unknown type -> skipped in loop
    UnionType unkOrStr = createUnion(unknownType, stringType);
    JSType leastUnk = unkOrStr.getLeastSupertype(numberType);
    assertNotNull(leastUnk);
  }

  @Test(timeout = 4000)
  public void testMeetBranches() {
    UnionType numOrStr = createUnion(numberType, stringType);

    // 1. Alternate is subtype of that -> result != null
    JSType meet1 = numOrStr.meet(numberType);
    assertEquals(numberType, meet1);

    // 2. that instanceof UnionType with matching subtype
    UnionType numOrBool = createUnion(numberType, booleanType);
    JSType meet2 = numOrStr.meet(numOrBool);
    assertEquals(numberType, meet2);

    // 3. that is subtype of this (non-union)
    UnionType objOrNum = createUnion(objectType, numberType);
    JSType meet3 = objOrNum.meet(dateType);
    assertEquals(dateType, meet3);

    // 4. this.isObject() && that.isObject() -> NO_OBJECT_TYPE
    UnionType dateOrArray = createUnion(dateType, arrayType);
    JSType meet4 = dateOrArray.meet(regExpType);
    assertEquals(noObjectType, meet4);

    // 5. Fallback -> NO_TYPE
    JSType meet5 = numOrStr.meet(booleanType);
    assertEquals(noType, meet5);
  }

  @Test(timeout = 4000)
  public void testFindPropertyTypeBranches() {
    ObjectType obj1 = registry.createAnonymousObjectType();
    obj1.defineDeclaredProperty("prop", numberType, null);

    ObjectType obj2 = registry.createAnonymousObjectType();
    obj2.defineDeclaredProperty("prop", stringType, null);

    ObjectType objWithoutProp = registry.createAnonymousObjectType();

    // Contains nullType, voidType, obj1, objWithoutProp, obj2
    UnionType union = createUnion(nullType, voidType, obj1, objWithoutProp, obj2);

    // Property found across alternates and merged using least supertype
    JSType propType = union.findPropertyType("prop");
    assertNotNull(propType);
    assertTrue(propType.isUnionType());
    assertTrue(propType.toMaybeUnionType().contains(numberType));
    assertTrue(propType.toMaybeUnionType().contains(stringType));

    // Property does not exist in any alternate -> null
    assertNull(union.findPropertyType("nonExistent"));
  }

  @Test(timeout = 4000)
  public void testGetRestrictedUnion() {
    UnionType union = createUnion(unknownType, numberType, stringType);
    JSType restricted = union.getRestrictedUnion(numberType);

    // Number type should be filtered out, unknown and string remain
    assertTrue(restricted.isUnionType());
    UnionType restrictedUnion = restricted.toMaybeUnionType();
    assertTrue(restrictedUnion.contains(unknownType));
    assertTrue(restrictedUnion.contains(stringType));
    assertFalse(restrictedUnion.contains(numberType));
  }

  @Test(timeout = 4000)
  public void testToStringFormatting() {
    UnionType union = createUnion(numberType, stringType);
    assertEquals("(number|string)", union.toString());
  }

  @Test(timeout = 4000)
  public void testIsSubtype() {
    UnionType numOrStr = createUnion(numberType, stringType);
    assertTrue(numOrStr.isSubtype(allType));
    assertFalse(numOrStr.isSubtype(numberType));
  }

  @Test(timeout = 4000)
  public void testGetRestrictedTypeGivenToBooleanOutcome() {
    UnionType union = createUnion(nullType, stringType);

    // True outcome filters out null (falsy)
    JSType trueOutcome = union.getRestrictedTypeGivenToBooleanOutcome(true);
    assertEquals(stringType, trueOutcome);

    // False outcome retains falsy possibilities
    JSType falseOutcome = union.getRestrictedTypeGivenToBooleanOutcome(false);
    assertNotNull(falseOutcome);
  }

  @Test(timeout = 4000)
  public void testGetPossibleToBooleanOutcomes() {
    // 1. Both TRUE and FALSE early break
    UnionType both = createUnion(nullType, objectType);
    assertEquals(BooleanLiteralSet.BOTH, both.getPossibleToBooleanOutcomes());

    // 2. Only FALSE
    UnionType onlyFalse = createUnion(nullType, voidType);
    assertEquals(BooleanLiteralSet.FALSE, onlyFalse.getPossibleToBooleanOutcomes());

    // 3. Only TRUE
    UnionType onlyTrue = createUnion(objectType);
    assertEquals(BooleanLiteralSet.TRUE, onlyTrue.getPossibleToBooleanOutcomes());
  }

  @Test(timeout = 4000)
  public void testTypePairsUnderEqualityAndInequality() {
    UnionType union = createUnion(numberType, stringType);

    TypePair eqPair = union.getTypesUnderEquality(numberType);
    assertNotNull(eqPair);
    assertNotNull(eqPair.typeA);
    assertNotNull(eqPair.typeB);

    TypePair ineqPair = union.getTypesUnderInequality(numberType);
    assertNotNull(ineqPair);
    assertNotNull(ineqPair.typeA);
    assertNotNull(ineqPair.typeB);

    TypePair shallowIneqPair = union.getTypesUnderShallowInequality(numberType);
    assertNotNull(shallowIneqPair);
    assertNotNull(shallowIneqPair.typeA);
    assertNotNull(shallowIneqPair.typeB);
  }

  @Test(timeout = 4000)
  public void testForgiveUnknownNames() {
    UnionType union = createUnion(numberType, stringType);
    union.forgiveUnknownNames();
  }

  @Test(timeout = 4000)
  public void testVisitorPatternDispatch() {
    UnionType union = createUnion(numberType, stringType);

    InvocationHandler handler = new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) {
        if ("caseUnionType".equals(method.getName())) {
          return "dispatched_union";
        }
        return null;
      }
    };

    @SuppressWarnings("unchecked")
    Visitor<String> visitor = (Visitor<String>) Proxy.newProxyInstance(
        Visitor.class.getClassLoader(),
        new Class<?>[] { Visitor.class },
        handler);

    String result = union.visit(visitor);
    assertEquals("dispatched_union", result);
  }

  // ==========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // ==========================================================================

  @Test(timeout = 4000)
  public void testEmptyAlternatesBoundary() {
    UnionType emptyUnion = new UnionType(registry, Collections.<JSType>emptySet());

    assertFalse(emptyUnion.matchesNumberContext());
    assertFalse(emptyUnion.matchesStringContext());
    assertFalse(emptyUnion.matchesObjectContext());
    assertTrue(emptyUnion.canAssignTo(numberType));
    assertTrue(emptyUnion.canBeCalled());
    assertNull(emptyUnion.testForEquality(numberType));
    assertFalse(emptyUnion.isNullable());
    assertFalse(emptyUnion.isUnknownType());
    assertTrue(emptyUnion.isObject());
    assertNull(emptyUnion.findPropertyType("any"));
    assertEquals("()", emptyUnion.toString());
    assertTrue(emptyUnion.isSubtype(numberType));
    assertEquals(BooleanLiteralSet.EMPTY, emptyUnion.getPossibleToBooleanOutcomes());
  }

  @Test(timeout = 4000)
  public void testSingleAlternateBoundary() {
    UnionType single = createUnion(numberType);
    assertTrue(single.contains(numberType));
    assertEquals("(number)", single.toString());
    assertTrue(single.matchesNumberContext());
    assertEquals(numberType, single.restrictByNotNullOrUndefined());
  }

  // ==========================================================================
  // Partition D: Scope Resolution & State Mutation Guard Paths
  // ==========================================================================

  @Test(timeout = 4000)
  public void testResolveInternalUnchangedPath() {
    UnionType union = createUnion(numberType, stringType);
    JSType resolved = union.resolve(errorReporter, null);
    assertSame(union, resolved);
  }

  @Test(timeout = 4000)
  public void testResolveInternalChangedPath() {
    NamedType namedType = new NamedType(registry, "MissingType", "test.js", 1, 1);
    UnionType union = createUnion(namedType, numberType);

    JSType resolved = union.resolve(errorReporter, null);
    assertSame(union, resolved);
  }

  // ==========================================================================
  // Partition E: Object Lifecycle & Contract Integrity (equals, hashCode)
  // ==========================================================================

  @Test(timeout = 4000)
  public void testEqualsAndHashCodeContract() {
    UnionType u1 = createUnion(numberType, stringType);
    UnionType u2 = createUnion(stringType, numberType);
    UnionType u3 = createUnion(numberType, booleanType);

    // Reflexive
    assertEquals(u1, u1);

    // Symmetric & order-independent
    assertEquals(u1, u2);
    assertEquals(u2, u1);
    assertEquals(u1.hashCode(), u2.hashCode());

    // Inequality
    assertFalse(u1.equals(u3));
    assertFalse(u3.equals(u1));
    assertFalse(u1.equals(null));
    assertFalse(u1.equals("NotAUnionType"));
    assertFalse(u1.equals(numberType));
  }
}