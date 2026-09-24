package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.jstype.RecordTypeBuilder.RecordProperty;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: RecordType.java
 *
 * DECISION BRANCHES & COVERAGE TARGETS:
 * 1. Constructor:
 *    - declared = true vs declared = false (isSynthetic() branch).
 *    - RecordProperty == null defensive check -> throws IllegalStateException.
 *    - Normal property population and freeze state check.
 * 2. defineProperty:
 *    - isFrozen == true -> returns false (direct call on created RecordType).
 *    - !inferred vs inferred logic during construction.
 * 3. checkRecordEquivalenceHelper:
 *    - Key set size/name inequality (!otherProps.keySet().equals(keySet)) -> false.
 *    - Tolerating unknowns (tolerateUnknowns = true vs false) with matching and non-matching types.
 *    - Key types equivalence -> true.
 * 4. isSubtype (Instance method):
 *    - JSType.isSubtypeHelper(this, that) -> true (e.g., that is UNKNOWN_TYPE or ALL_TYPE).
 *    - OBJECT_TYPE.isSubtype(that) -> true (e.g., that is OBJECT_TYPE).
 *    - !that.isRecordType() -> false (e.g., primitive NUMBER_TYPE).
 *    - Delegation to static isSubtype(ObjectType, RecordType).
 * 5. isSubtype (Static method):
 *    - !typeA.hasProperty(property) -> false.
 *    - propA and propB unknown types (bypassing invariance check).
 *    - typeA.isPropertyTypeDeclared(property) == true -> !propA.isInvariant(propB) -> false / true.
 *    - typeA.isPropertyTypeDeclared(property) == false (inferred) -> !propA.isSubtype(propB) -> false / true.
 * 6. getGreatestSubtypeHelper:
 *    - that.isRecordType() == true:
 *        * Conflicting property types (!isInvariant) -> returns NO_TYPE.
 *        * Non-conflicting shared properties and disjoint properties -> merges unique properties.
 *    - that.isRecordType() == false:
 *        * thatRestrictedToObj.isEmptyType() == true -> returns NO_OBJECT_TYPE.
 *        * thatRestrictedToObj is ObjectType -> UnionTypeBuilder over reference types in registry.
 * 7. resolveInternal:
 *    - Unresolved property types (type != resolvedType) -> updates properties map.
 *    - Already resolved property types -> keeps existing.
 * 8. KNOWN DEFECT ZONE (Defects4J: RecordTypeTest::testSubtypeWithUnknowns2 / TypeCheckTest::testIssue791):
 *    - Subtyping when properties contain UnionType including UNKNOWN_TYPE (e.g., (?|undefined)).
 *    - In the buggy version, UnionType with unknown is NOT recognized as isUnknownType(), causing
 *      strict invariance failure instead of allowing unknown type covariance.
 * ----------------------------------------------------------------------------------------------------
 */
public class RecordTypeGeminiTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType unknownType;
  private JSType voidType;
  private ObjectType objectType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testRecordTypeCreationAndBasicGetters() {
    Map<String, RecordProperty> props = new HashMap<String, RecordProperty>();
    props.put("a", new RecordProperty(numberType, null));
    props.put("b", new RecordProperty(stringType, null));

    RecordType record = new RecordType(registry, props);

    assertFalse("Declared record type must not be synthetic", record.isSynthetic());
    assertTrue("Record type must have property 'a'", record.hasProperty("a"));
    assertTrue("Record type must have property 'b'", record.hasProperty("b"));
    assertEquals(numberType, record.getPropertyType("a"));
    assertEquals(stringType, record.getPropertyType("b"));
    assertEquals(objectType, record.getImplicitPrototype());
    assertSame("toMaybeRecordType must return this", record, record.toMaybeRecordType());
  }

  @Test(timeout = 4000)
  public void testSyntheticRecordTypeFlag() {
    Map<String, RecordProperty> props = new HashMap<String, RecordProperty>();
    props.put("x", new RecordProperty(booleanType, null));

    RecordType synthRecord = new RecordType(registry, props, false);
    assertTrue("Synthetic record must return true for isSynthetic", synthRecord.isSynthetic());

    RecordType declaredRecord = new RecordType(registry, props, true);
    assertFalse("Declared record must return false for isSynthetic", declaredRecord.isSynthetic());
  }

  @Test(timeout = 4000)
  public void testSubtypeStructuralCovariance() {
    // Subtype has {a: number, b: string}, Supertype has {a: number}
    RecordType sub = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .addProperty("b", stringType, null)
        .build();

    RecordType sup = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    assertTrue("Record with superset of properties is a subtype", sub.isSubtype(sup));
    assertFalse("Record with fewer properties cannot be a subtype", sup.isSubtype(sub));
  }

  @Test(timeout = 4000)
  public void testSubtypePropertyTypeMismatch() {
    RecordType rec1 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    RecordType rec2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", stringType, null)
        .build();

    assertFalse("Different property types must fail subtyping", rec1.isSubtype(rec2));
    assertFalse("Different property types must fail subtyping symmetrically", rec2.isSubtype(rec1));
  }

  @Test(timeout = 4000)
  public void testSubtypeWithInferredPropertyOnObjectType() {
    // Non-record ObjectType with an inferred property
    ObjectType inferredObj = new PrototypeObjectType(registry, "InferredHolder", objectType);
    inferredObj.defineProperty("prop", numberType, true, null); // inferred = true

    RecordType targetRecord = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("prop", numberType, null)
        .build();

    assertTrue("Inferred property with matching subtype should satisfy record",
        RecordType.isSubtype(inferredObj, targetRecord));

    RecordType targetMismatch = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("prop", stringType, null)
        .build();

    assertFalse("Inferred property not subtyping record property should fail",
        RecordType.isSubtype(inferredObj, targetMismatch));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyRecordTypeSubtyping() {
    RecordType emptyRec1 = (RecordType) new RecordTypeBuilder(registry).build();
    RecordType emptyRec2 = (RecordType) new RecordTypeBuilder(registry).build();

    assertTrue("Empty record is subtype of empty record", emptyRec1.isSubtype(emptyRec2));
    assertTrue("Empty record checkEquivalenceHelper with empty record is true",
        emptyRec1.checkRecordEquivalenceHelper(emptyRec2, false));

    RecordType nonPropRec = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    assertTrue("Any record is subtype of empty record", nonPropRec.isSubtype(emptyRec1));
    assertFalse("Empty record is not subtype of non-empty record", emptyRec1.isSubtype(nonPropRec));
  }

  @Test(timeout = 4000)
  public void testFrozenRecordRejectsDefineProperty() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    // After construction, RecordType is frozen
    boolean result = record.defineProperty("b", stringType, false, null);
    assertFalse("RecordType must reject defineProperty once frozen", result);
    assertFalse("Property 'b' must not be added to frozen record", record.hasProperty("b"));
  }

  @Test(timeout = 4000)
  public void testSubtypeWithDirectUnknownProperty() {
    RecordType recUnknown = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", unknownType, null)
        .build();
    RecordType recNumber = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    assertTrue("{a: ?} is subtype of {a: number}", recUnknown.isSubtype(recNumber));
    assertTrue("{a: number} is subtype of {a: ?}", recNumber.isSubtype(recUnknown));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Issue 791 / Defects4J)
  // =========================================================================

  /**
   * Targets Defects4J RecordTypeTest::testSubtypeWithUnknowns2 & TypeCheckTest::testIssue791.
   * When record properties have union types containing UnknownType and VoidType (e.g. (?|undefined)),
   * subtyping between {a: (?|undefined)} and {a: (number|undefined)} or {a: (string|undefined)}
   * must hold true.
   */
  @Test(timeout = 4000)
  public void testSubtypeWithUnknowns2() {
    JSType unknownOrVoid = registry.createUnionType(unknownType, voidType);
    JSType numberOrVoid = registry.createUnionType(numberType, voidType);

    RecordType type1 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", unknownOrVoid, null)
        .build();
    RecordType type2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberOrVoid, null)
        .build();

    assertTrue("Record with property (?|void) must be subtype of (number|void)",
        type1.isSubtype(type2));
    assertTrue("Record with property (number|void) must be subtype of (?|void)",
        type2.isSubtype(type1));
  }

  @Test(timeout = 4000)
  public void testSubtypeWithUnknownsStringOrVoid() {
    JSType unknownOrVoid = registry.createUnionType(unknownType, voidType);
    JSType stringOrVoid = registry.createUnionType(stringType, voidType);

    RecordType type1 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", unknownOrVoid, null)
        .build();
    RecordType type2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", stringOrVoid, null)
        .build();

    assertTrue("Record with property (?|void) must be subtype of (string|void)",
        type1.isSubtype(type2));
    assertTrue("Record with property (string|void) must be subtype of (?|void)",
        type2.isSubtype(type1));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testConstructorThrowsWhenRecordPropertyIsNull() {
    Map<String, RecordProperty> mapWithNull = new HashMap<String, RecordProperty>();
    mapWithNull.put("invalid", null);

    new RecordType(registry, mapWithNull);
  }

  @Test(timeout = 4000)
  public void testIsSubtypeNonRecordAndUniversalSupertypes() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    // Primitive target
    assertFalse("Record cannot be subtype of primitive number", record.isSubtype(numberType));

    // Universal supertypes
    assertTrue("Record must be subtype of UNKNOWN_TYPE",
        record.isSubtype(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE)));
    assertTrue("Record must be subtype of ALL_TYPE",
        record.isSubtype(registry.getNativeType(JSTypeNative.ALL_TYPE)));
    assertTrue("Record must be subtype of OBJECT_TYPE",
        record.isSubtype(objectType));
  }

  @Test(timeout = 4000)
  public void testStaticIsSubtypeMissingPropertyOnTypeA() {
    RecordType typeA = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("p1", numberType, null)
        .build();
    RecordType typeB = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("p1", numberType, null)
        .addProperty("p2", stringType, null)
        .build();

    assertFalse("typeA missing property p2 cannot be subtype of typeB",
        RecordType.isSubtype(typeA, typeB));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testCheckRecordEquivalenceHelperBranchCoverage() {
    RecordType recA = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("k1", numberType, null)
        .build();
    RecordType recB = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("k2", numberType, null)
        .build();
    RecordType recA2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("k1", numberType, null)
        .build();
    RecordType recAString = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("k1", stringType, null)
        .build();
    RecordType recAUnknown = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("k1", unknownType, null)
        .build();

    // 1. Key set mismatch
    assertFalse("Equivalence must fail when property names differ",
        recA.checkRecordEquivalenceHelper(recB, false));

    // 2. Identical keys and types
    assertTrue("Equivalence must hold for identical records",
        recA.checkRecordEquivalenceHelper(recA2, false));

    // 3. Same key, different types, no unknowns tolerated
    assertFalse("Equivalence must fail for different property types",
        recA.checkRecordEquivalenceHelper(recAString, false));

    // 4. Same key, unknown type with tolerateUnknowns = false
    assertFalse("Equivalence with unknown fails when unknowns not tolerated",
        recA.checkRecordEquivalenceHelper(recAUnknown, false));

    // 5. Same key, unknown type with tolerateUnknowns = true
    assertTrue("Equivalence with unknown succeeds when unknowns are tolerated",
        recA.checkRecordEquivalenceHelper(recAUnknown, true));
  }

  @Test(timeout = 4000)
  public void testGetGreatestSubtypeHelperWithRecordTypes() {
    RecordType rec1 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .addProperty("b", stringType, null)
        .build();

    RecordType rec2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("b", stringType, null)
        .addProperty("c", booleanType, null)
        .build();

    // Compatible overlapping properties
    JSType greatest = rec1.getGreatestSubtypeHelper(rec2);
    assertTrue("Greatest subtype must be a RecordType", greatest.isRecordType());
    RecordType resultRec = greatest.toMaybeRecordType();
    assertTrue(resultRec.hasProperty("a"));
    assertTrue(resultRec.hasProperty("b"));
    assertTrue(resultRec.hasProperty("c"));
    assertTrue("Merged greatest subtype must be synthetic", resultRec.isSynthetic());

    // Conflicting property types -> returns NO_TYPE
    RecordType recConflict = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", stringType, null) // 'a' is string vs number in rec1
        .build();

    JSType conflictGreatest = rec1.getGreatestSubtypeHelper(recConflict);
    assertEquals("Conflicting property types must result in NO_TYPE",
        registry.getNativeObjectType(JSTypeNative.NO_TYPE), conflictGreatest);
  }

  @Test(timeout = 4000)
  public void testGetGreatestSubtypeHelperWithNonRecordTypes() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("prop", numberType, null)
        .build();

    // 1. Primitive type (thatRestrictedToObj is empty)
    JSType nonObjResult = record.getGreatestSubtypeHelper(numberType);
    assertEquals(registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE), nonObjResult);

    // 2. ObjectType (thatRestrictedToObj is non-empty)
    JSType objResult = record.getGreatestSubtypeHelper(objectType);
    assertNotNull("Greatest subtype with ObjectType should return a non-null type", objResult);
  }

  @Test(timeout = 4000)
  public void testResolveInternalUpdatesProperties() {
    NamedType unresType = new NamedType(registry, "DeferredType", "source.js", 1, 1);
    registry.declareType("DeferredType", numberType);

    Map<String, RecordProperty> map = new HashMap<String, RecordProperty>();
    map.put("key", new RecordProperty(unresType, null));
    RecordType rt = new RecordType(registry, map);

    ErrorReporter reporter = new SimpleErrorReporter();
    JSType resolved = rt.resolve(reporter, null);

    assertNotNull("Resolved RecordType must not be null", resolved);
    assertTrue("Resolved type must still be RecordType", resolved.isRecordType());
    assertEquals("Property type must be resolved to numberType",
        numberType, resolved.toMaybeRecordType().getPropertyType("key"));
  }
}