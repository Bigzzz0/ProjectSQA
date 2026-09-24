package com.google.javascript.rhino.jstype;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.rhino.jstype.UnionType
 *
 * Branch & Decision Coverage Map:
 * 1. matchesNumberContext / matchesStringContext / matchesObjectContext:
 *    - Alternates matching vs not matching; short-circuit on true; all false.
 * 2. findPropertyType(propertyName):
 *    - Skip NullType and VoidType alternates.
 *    - Alternate missing property (returns null).
 *    - Alternate containing property (first match vs least supertype merge).
 * 3. canAssignTo(that):
 *    - UnknownType alternate triggering immediate return true.
 *    - All alternates assignable vs at least one alternate not assignable.
 * 4. canBeCalled():
 *    - Short-circuit on non-callable alternate; all callable alternates.
 * 5. autobox() & restrictByNotNullOrUndefined():
 *    - Rebuilding union after transforming alternates; empty, single, or multiple results.
 * 6. testForEquality(that):
 *    - Single alternate equality; multiple alternates matching same ternary outcome;
 *      differing outcomes returning UNKNOWN.
 * 7. isNullable, isUnknownType, isStruct, isDict, isObject:
 *    - Any match returns true (or false for isObject when non-object encountered).
 * 8. getLeastSupertype(that):
 *    - Non-unknown, non-union 'that' where 'that.isSubtype(alternate)' returns 'this'.
 *    - Fallback to getLeastSupertype(this, that).
 * 9. meet(that):
 *    - Alternates subtype of 'that'; 'that' is UnionType vs non-union; result is NoType / NoObjectType.
 * 10. checkUnionEquivalenceHelper(that, tolerateUnknowns):
 *     - Size mismatch with tolerateUnknowns false vs true; missing alternate branches.
 * 11. contains(type) & hasProperty(pname):
 *     - Match found vs no match.
 * 12. getRestrictedUnion(type):
 *     - Alternates matching isUnknownType or !isSubtype(type) kept; others dropped.
 * 13. toStringHelper:
 *     - Multi-type alpha ordering formatting "(type1|type2)".
 * 14. isSubtype(that):
 *     - that.isUnknownType() -> true; that.isAllType() -> true.
 *     - All elements subtype of 'that' -> true; one element not subtype -> false.
 *     - Target defect: RecordType subtyping and least supertypes involving RecordType and unknown types.
 * 15. getRestrictedTypeGivenToBooleanOutcome & getPossibleToBooleanOutcomes:
 *     - Accumulating BooleanLiteralSet; break early on BooleanLiteralSet.BOTH.
 * 16. getTypesUnderEquality / getTypesUnderInequality / getTypesUnderShallowInequality:
 *     - Accumulating pair branches with null and non-null types.
 * 17. collapseUnion():
 *     - Unknown type alternate -> returns UNKNOWN_TYPE.
 *     - Non-object value followed by another value / object -> returns ALL_TYPE.
 *     - Multiple objects collapsed to common super object.
 * 18. resolveInternal:
 *     - Type resolution where alternate changes vs unchanged; circularly defined types.
 */

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class UnionTypeGeminiTest {

  private JSTypeRegistry registry;
  private JSType NUMBER;
  private JSType STRING;
  private JSType BOOLEAN;
  private JSType NULL;
  private JSType VOID;
  private JSType OBJECT;
  private JSType UNKNOWN_TYPE;
  private JSType ALL_TYPE;
  private JSType NO_TYPE;
  private JSType NO_OBJECT;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    NUMBER = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    STRING = registry.getNativeType(JSTypeNative.STRING_TYPE);
    BOOLEAN = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    NULL = registry.getNativeType(JSTypeNative.NULL_TYPE);
    VOID = registry.getNativeType(JSTypeNative.VOID_TYPE);
    OBJECT = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    UNKNOWN_TYPE = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    ALL_TYPE = registry.getNativeType(JSTypeNative.ALL_TYPE);
    NO_TYPE = registry.getNativeType(JSTypeNative.NO_TYPE);
    NO_OBJECT = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
  }

  private UnionType createUnion(JSType... types) {
    JSType union = registry.createUnionType(types);
    return union.toMaybeUnionType();
  }

  // =========================================================================
  // Partition A: Core Functional Logic & Context Matches
  // =========================================================================

  @Test(timeout = 4000)
  public void testContextMatching() {
    UnionType numOrStr = createUnion(NUMBER, STRING);
    assertTrue(numOrStr.matchesNumberContext());
    assertTrue(numOrStr.matchesStringContext());
    assertTrue(numOrStr.matchesObjectContext());

    UnionType nullOrVoid = createUnion(NULL, VOID);
    assertFalse(nullOrVoid.matchesNumberContext());
    assertFalse(nullOrVoid.matchesStringContext());
    assertFalse(nullOrVoid.matchesObjectContext());
  }

  @Test(timeout = 4000)
  public void testFindPropertyType() {
    ObjectType objA = registry.createAnonymousObjectType();
    objA.defineDeclaredProperty("propA", NUMBER, null);
    ObjectType objB = registry.createAnonymousObjectType();
    objB.defineDeclaredProperty("propA", STRING, null);

    UnionType union = createUnion(objA, objB, NULL, VOID);
    JSType propType = union.findPropertyType("propA");
    assertNotNull(propType);
    assertTrue(propType.isUnionType());
    assertTrue(propType.toMaybeUnionType().contains(NUMBER));
    assertTrue(propType.toMaybeUnionType().contains(STRING));

    assertNull(union.findPropertyType("nonExistent"));
  }

  @Test(timeout = 4000)
  public void testCanAssignTo() {
    UnionType numOrStr = createUnion(NUMBER, STRING);
    assertFalse(numOrStr.canAssignTo(NUMBER));
    assertTrue(numOrStr.canAssignTo(ALL_TYPE));

    UnionType withUnknown = new UnionType(registry, ImmutableList.of(NUMBER, UNKNOWN_TYPE));
    assertTrue(withUnknown.canAssignTo(STRING));
  }

  @Test(timeout = 4000)
  public void testCanBeCalled() {
    FunctionType funcA = registry.createFunctionType(NUMBER);
    FunctionType funcB = registry.createFunctionType(STRING);

    UnionType callableUnion = createUnion(funcA, funcB);
    assertTrue(callableUnion.canBeCalled());

    UnionType mixedUnion = createUnion(funcA, NUMBER);
    assertFalse(mixedUnion.canBeCalled());
  }

  @Test(timeout = 4000)
  public void testAutoboxAndRestrictNotNullOrUndefined() {
    UnionType primitiveUnion = createUnion(NUMBER, STRING, NULL, VOID);
    JSType autoboxed = primitiveUnion.autobox();
    assertTrue(autboxed.isUnionType());
    for (JSType alt : autoboxed.toMaybeUnionType().getAlternates()) {
      assertTrue(alt.isObject() || alt.isNullType() || alt.isVoidType());
    }

    JSType restricted = primitiveUnion.restrictByNotNullOrUndefined();
    assertTrue(restricted.isUnionType());
    assertFalse(restricted.toMaybeUnionType().contains(NULL));
    assertFalse(restricted.toMaybeUnionType().contains(VOID));
  }

  @Test(timeout = 4000)
  public void testTestForEquality() {
    UnionType numOrStr = createUnion(NUMBER, STRING);
    assertEquals(TernaryValue.UNKNOWN, numOrStr.testForEquality(NUMBER));

    UnionType nullUnion = createUnion(NULL, NULL);
    assertEquals(TernaryValue.TRUE, nullUnion.testForEquality(NULL));
  }

  // =========================================================================
  // Partition B: Type Predicates & Introspection
  // =========================================================================

  @Test(timeout = 4000)
  public void testTypePredicates() {
    UnionType nullable = createUnion(STRING, NULL);
    assertTrue(nullable.isNullable());

    UnionType notNullable = createUnion(STRING, NUMBER);
    assertFalse(notNullable.isNullable());

    UnionType withUnknown = new UnionType(registry, ImmutableList.of(NUMBER, UNKNOWN_TYPE));
    assertTrue(withUnknown.isUnknownType());

    ObjectType structObj = registry.createObjectType("StructObj", null, null);
    structObj.setStruct();
    ObjectType dictObj = registry.createObjectType("DictObj", null, null);
    dictObj.setDict();

    UnionType structUnion = createUnion(structObj, STRING);
    assertTrue(structUnion.isStruct());
    assertFalse(structUnion.isDict());

    UnionType dictUnion = createUnion(dictObj, STRING);
    assertTrue(dictUnion.isDict());
    assertFalse(dictUnion.isStruct());

    UnionType allObjects = createUnion(structObj, dictObj);
    assertTrue(allObjects.isObject());

    UnionType mixedObjects = createUnion(structObj, NUMBER);
    assertFalse(mixedObjects.isObject());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (RecordType & Unknown Interactions)
  // =========================================================================

  /**
   * Targets Defects4J RecordType / UnionType least-supertype & subtyping defects
   * (e.g. RecordTypeTest::testSubtypeWithUnknowns2 and TypeCheckTest::testIssue791).
   */
  @Test(timeout = 4000)
  public void testDefectRecordTypeLeastSupertypeAndSubtypingWithUnknowns() {
    RecordType recordA = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", NUMBER, null)
        .build();
    RecordType recordB = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("b", NUMBER, null)
        .build();

    JSType leastSuper = recordA.getLeastSupertype(recordB);
    assertTrue(leastSuper.isUnionType());
    UnionType unionRecord = leastSuper.toMaybeUnionType();
    assertTrue(unionRecord.contains(recordA));
    assertTrue(unionRecord.contains(recordB));

    // Test union least-supertype when 'that' is an alternate subtype
    JSType superOfUnionAndA = unionRecord.getLeastSupertype(recordA);
    assertEquals(unionRecord, superOfUnionAndA);

    // Verify subtyping under unknown type property
    RecordType recordWithUnknown = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", UNKNOWN_TYPE, null)
        .build();
    assertTrue(recordA.isSubtype(recordWithUnknown));

    UnionType unionWithUnknownRecord = createUnion(recordWithUnknown, recordB);
    assertTrue(recordA.isSubtype(unionWithUnknownRecord.getLeastSupertype(recordA)));
  }

  @Test(timeout = 4000)
  public void testSubtypingBranches() {
    UnionType numOrStr = createUnion(NUMBER, STRING);
    assertTrue(numOrStr.isSubtype(UNKNOWN_TYPE));
    assertTrue(numOrStr.isSubtype(ALL_TYPE));

    UnionType numOnly = createUnion(NUMBER, NUMBER);
    assertTrue(numOnly.isSubtype(NUMBER));
    assertFalse(numOrStr.isSubtype(NUMBER));
  }

  @Test(timeout = 4000)
  public void testMeetOperation() {
    UnionType numOrStr = createUnion(NUMBER, STRING);
    JSType meetWithNum = numOrStr.meet(NUMBER);
    assertEquals(NUMBER, meetWithNum);

    UnionType strOrBool = createUnion(STRING, BOOLEAN);
    JSType meetUnions = numOrStr.meet(strOrBool);
    assertEquals(STRING, meetUnions);

    ObjectType objA = registry.createAnonymousObjectType();
    ObjectType objB = registry.createAnonymousObjectType();
    UnionType unionObjs = createUnion(objA, objB);
    JSType meetNoMatchObj = unionObjs.meet(NUMBER);
    assertEquals(NO_TYPE, meetNoMatchObj);

    ObjectType objC = registry.createAnonymousObjectType();
    JSType meetDisjointObjs = createUnion(objA).meet(objC);
    assertTrue(meetDisjointObjs.isSubtype(OBJECT));
  }

  // =========================================================================
  // Partition D: Restriction, Boolean Outcomes & Type Pairs
  // =========================================================================

  @Test(timeout = 4000)
  public void testRestrictedUnion() {
    UnionType numOrStr = createUnion(NUMBER, STRING);
    JSType restricted = numOrStr.getRestrictedUnion(NUMBER);
    assertEquals(STRING, restricted);

    UnionType withUnknown = new UnionType(registry, ImmutableList.of(UNKNOWN_TYPE, NUMBER));
    JSType keptUnknown = withUnknown.getRestrictedUnion(NUMBER);
    assertTrue(keptUnknown.isUnknownType());
  }

  @Test(timeout = 4000)
  public void testBooleanOutcomes() {
    UnionType alwaysTrue = createUnion(STRING, OBJECT);
    assertEquals(BooleanLiteralSet.BOTH, alwaysTrue.getPossibleToBooleanOutcomes());

    JSType trueOutcome = alwaysTrue.getRestrictedTypeGivenToBooleanOutcome(true);
    assertNotNull(trueOutcome);

    JSType falseOutcome = alwaysTrue.getRestrictedTypeGivenToBooleanOutcome(false);
    assertNotNull(falseOutcome);
  }

  @Test(timeout = 4000)
  public void testTypesUnderEqualityAndInequality() {
    UnionType numOrStr = createUnion(NUMBER, STRING);
    TypePair eqPair = numOrStr.getTypesUnderEquality(NUMBER);
    assertNotNull(eqPair.typeA);
    assertNotNull(eqPair.typeB);

    TypePair ineqPair = numOrStr.getTypesUnderInequality(NUMBER);
    assertNotNull(ineqPair.typeA);
    assertNotNull(ineqPair.typeB);

    TypePair shallowIneqPair = numOrStr.getTypesUnderShallowInequality(NUMBER);
    assertNotNull(shallowIneqPair.typeA);
    assertNotNull(shallowIneqPair.typeB);
  }

  @Test(timeout = 4000)
  public void testCollapseUnion() {
    UnionType withUnknown = new UnionType(registry, ImmutableList.of(NUMBER, UNKNOWN_TYPE));
    assertEquals(UNKNOWN_TYPE, withUnknown.collapseUnion());

    UnionType numAndStr = createUnion(NUMBER, STRING);
    assertEquals(ALL_TYPE, numAndStr.collapseUnion());

    ObjectType objA = registry.createAnonymousObjectType();
    UnionType numAndObj = createUnion(NUMBER, objA);
    assertEquals(ALL_TYPE, numAndObj.collapseUnion());

    ObjectType objB = registry.createAnonymousObjectType();
    UnionType objsOnly = createUnion(objA, objB);
    JSType collapsed = objsOnly.collapseUnion();
    assertNotNull(collapsed);
    assertTrue(collapsed.isSubtype(OBJECT));
  }

  // =========================================================================
  // Partition E: Object Contract, Equivalence & Lifecycle
  // =========================================================================

  @Test(timeout = 4000)
  public void testCheckUnionEquivalenceHelper() {
    UnionType u1 = createUnion(NUMBER, STRING);
    UnionType u2 = createUnion(STRING, NUMBER);
    UnionType u3 = createUnion(NUMBER, BOOLEAN);

    assertTrue(u1.checkUnionEquivalenceHelper(u2, false));
    assertFalse(u1.checkUnionEquivalenceHelper(u3, false));
    assertEquals(u1.hashCode(), u2.hashCode());

    UnionType u4 = createUnion(NUMBER, STRING, BOOLEAN);
    assertFalse(u1.checkUnionEquivalenceHelper(u4, false));
  }

  @Test(timeout = 4000)
  public void testToStringAndDebugHashCode() {
    UnionType u = createUnion(NUMBER, STRING);
    String str = u.toStringHelper(false);
    assertTrue(str.startsWith("(") && str.endsWith(")"));
    assertTrue(str.contains("number") && str.contains("string"));

    String debugStr = u.toDebugHashCodeString();
    assertTrue(debugStr.startsWith("{(") && debugStr.endsWith(")}"));
  }

  @Test(timeout = 4000)
  public void testVisitorPattern() {
    UnionType u = createUnion(NUMBER, STRING);
    String visited = u.visit(new Visitor<String>() {
      @Override public String caseNoType() { return "no"; }
      @Override public String caseEnumElementType(EnumElementType type) { return "enum"; }
      @Override public String caseAllType() { return "all"; }
      @Override public String caseBooleanType() { return "bool"; }
      @Override public String caseNoObjectType() { return "noObj"; }
      @Override public String caseFunctionType(FunctionType type) { return "func"; }
      @Override public String caseObjectType(ObjectType type) { return "obj"; }
      @Override public String caseUnknownType() { return "unknown"; }
      @Override public String caseNullType() { return "null"; }
      @Override public String caseNamedType(NamedType type) { return "named"; }
      @Override public String caseNumberType() { return "num"; }
      @Override public String caseStringType() { return "str"; }
      @Override public String caseVoidType() { return "void"; }
      @Override public String caseUnionType(UnionType type) { return "union:" + type.alternates.size(); }
      @Override public String caseTemplateType(TemplateType templateType) { return "template"; }
    });
    assertEquals("union:2", visited);
  }

  @Test(timeout = 4000)
  public void testSetValidatorAndMatchConstraint() {
    UnionType u = createUnion(NUMBER, STRING);
    boolean validatorResult = u.setValidator(new Predicate<JSType>() {
      @Override public boolean apply(JSType input) { return true; }
    });
    assertTrue(validatorResult);

    u.matchConstraint(OBJECT);
    assertFalse(u.hasAnyTemplateInternal());
  }

  @Test(timeout = 4000)
  public void testHasProperty() {
    ObjectType obj = registry.createAnonymousObjectType();
    obj.defineDeclaredProperty("foo", NUMBER, null);

    UnionType u = createUnion(obj, STRING);
    assertTrue(u.hasProperty("foo"));
    assertFalse(u.hasProperty("bar"));
  }

  @Test(timeout = 4000)
  public void testResolveInternalNoChange() {
    UnionType u = createUnion(NUMBER, STRING);
    JSType resolved = u.resolveInternal(null, null);
    assertSame(u, resolved);
  }
}