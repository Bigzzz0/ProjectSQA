/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.rhino.jstype.RecordType
 *
 * Decision / Condition Branch Analysis:
 * 1. Constructor:
 *    - properties map with valid RecordProperty elements -> properties defined, frozen = true.
 *    - properties map with null RecordProperty value -> throws IllegalStateException.
 * 2. isEquivalentTo(JSType other):
 *    - other.isRecordType() == false -> false.
 *    - otherRecord == this -> true.
 *    - keySet sizes/elements mismatch (!otherProps.keySet().equals(keySet)) -> false.
 *    - keySet matches, but property types not equivalent -> false.
 *    - keySet matches and all property types equivalent -> true.
 * 3. getImplicitPrototype():
 *    - returns registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE).
 * 4. defineProperty(propertyName, type, inferred, propertyNode):
 *    - isFrozen == true -> returns false immediately.
 *    - during construction (isFrozen == false):
 *        - inferred == true -> does not add to properties map.
 *        - inferred == false -> adds to properties map.
 * 5. getLeastSupertype(JSType that):
 *    - that.isRecordType() == false -> delegates to super.getLeastSupertype(that).
 *    - that.isRecordType() == true -> [KNOWN DEFECT: Defective implementation computes common
 *      properties record instead of union type when structural records diverge].
 * 6. getGreatestSubtypeHelper(JSType that):
 *    - that.isRecordType() == true:
 *        - matching property with non-equivalent type -> returns NO_TYPE.
 *        - matching properties with equivalent types -> combines properties into builder.
 *        - properties unique to 'that' -> added to builder.
 *        - builds and returns unified RecordType.
 *    - that.isRecordType() == false:
 *        - that restricted to OBJECT_TYPE isEmptyType() -> returns NO_OBJECT_TYPE.
 *        - that restricted to OBJECT_TYPE is not empty -> searches reference types with property,
 *          evaluates alt.isSubtype(that) and property equivalence/unknown compatibility.
 * 7. toMaybeRecordType():
 *    - returns this.
 * 8. isSubtype(JSType that):
 *    - JSType.isSubtypeHelper(this, that) == true -> true.
 *    - OBJECT_TYPE is subtype of that -> true.
 *    - !that.isRecordType() -> false.
 *    - delegated to static RecordType.isSubtype(this, that.toMaybeRecordType()).
 * 9. static isSubtype(ObjectType typeA, RecordType typeB):
 *    - typeA lacks property of typeB -> false.
 *    - property types: propA or propB is unknown -> loop continues without check.
 *    - both known:
 *        - typeA.isPropertyTypeDeclared(property) == true:
 *            - propA.isEquivalentTo(propB) == false -> false.
 *            - propA.isEquivalentTo(propB) == true -> continue.
 *        - typeA.isPropertyTypeDeclared(property) == false:
 *            - propA.isSubtype(propB) == false -> false.
 *            - propA.isSubtype(propB) == true -> continue.
 *    - all properties matched -> returns true.
 * 10. resolveInternal(ErrorReporter t, StaticScope<JSType> scope):
 *    - property resolves to itself (unmodified) -> map untouched.
 *    - property resolves to a new type -> properties map updated with resolved type.
 */

package com.google.javascript.rhino.jstype;

import com.google.common.collect.Maps;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.jstype.RecordTypeBuilder.RecordProperty;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RecordTypeGeminiTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter errorReporter;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private ObjectType objectNativeType;

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(errorReporter);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    objectNativeType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testImplicitPrototypeIsObject() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    assertNotNull(record);
    assertEquals(objectNativeType, record.getImplicitPrototype());
    assertSame(record, record.toMaybeRecordType());
  }

  @Test(timeout = 4000)
  public void testDefinePropertyFailsWhenFrozen() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("x", stringType, null)
        .build();

    // The record should be frozen after construction
    boolean defined = record.defineProperty("y", numberType, false, null);
    assertFalse("defineProperty must return false when record is frozen", defined);
    assertFalse("Property 'y' should not have been added", record.hasProperty("y"));
  }

  @Test(timeout = 4000)
  public void testEquivalenceSelfAndIdentical() {
    RecordType recordA = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("prop1", numberType, null)
        .addProperty("prop2", stringType, null)
        .build();

    RecordType recordB = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("prop1", numberType, null)
        .addProperty("prop2", stringType, null)
        .build();

    assertTrue("Record must be equivalent to itself", recordA.isEquivalentTo(recordA));
    assertTrue("Records with identical properties must be equivalent", recordA.isEquivalentTo(recordB));
    assertTrue("Equivalence must be symmetric", recordB.isEquivalentTo(recordA));
  }

  @Test(timeout = 4000)
  public void testEquivalenceMismatches() {
    RecordType recordA = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("prop1", numberType, null)
        .build();

    RecordType recordDifferentKeys = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("prop2", numberType, null)
        .build();

    RecordType recordDifferentType = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("prop1", stringType, null)
        .build();

    RecordType recordExtraKey = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("prop1", numberType, null)
        .addProperty("extra", booleanType, null)
        .build();

    assertFalse("Non-record type must not be equivalent", recordA.isEquivalentTo(numberType));
    assertFalse("Different keys must not be equivalent", recordA.isEquivalentTo(recordDifferentKeys));
    assertFalse("Different property types must not be equivalent", recordA.isEquivalentTo(recordDifferentType));
    assertFalse("Different key counts must not be equivalent", recordA.isEquivalentTo(recordExtraKey));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Structural Subtyping
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyRecordEquivalenceAndSubtyping() {
    RecordType emptyRecord1 = (RecordType) new RecordTypeBuilder(registry).build();
    RecordType emptyRecord2 = (RecordType) new RecordTypeBuilder(registry).build();

    assertTrue(emptyRecord1.isEquivalentTo(emptyRecord2));
    assertTrue(emptyRecord1.isSubtype(emptyRecord2));
    assertTrue(emptyRecord2.isSubtype(emptyRecord1));

    RecordType nonEmpty = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    // A subtype has all properties of supertype (empty record has no constraints)
    assertTrue("Non-empty record should be subtype of empty record", nonEmpty.isSubtype(emptyRecord1));
    assertFalse("Empty record should NOT be subtype of non-empty record", emptyRecord1.isSubtype(nonEmpty));
  }

  @Test(timeout = 4000)
  public void testStructuralSubtypingMatchingAndMissingProperties() {
    RecordType superRecord = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    RecordType subRecord = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .addProperty("b", stringType, null)
        .build();

    RecordType incompatible = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("b", stringType, null)
        .build();

    assertTrue("{a, b} should be a subtype of {a}", subRecord.isSubtype(superRecord));
    assertFalse("{a} should NOT be a subtype of {a, b}", superRecord.isSubtype(subRecord));
    assertFalse("{b} should NOT be a subtype of {a}", incompatible.isSubtype(superRecord));
    assertFalse("Record should NOT be subtype of non-record/non-object", subRecord.isSubtype(numberType));
  }

  @Test(timeout = 4000)
  public void testSubtypingWithDeclaredVersusInferredProperties() {
    RecordType targetRecord = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("x", objectNativeType, null)
        .build();

    // Create an ObjectType with an inferred property whose type is a subtype of objectNativeType
    ObjectType instanceObj = registry.createAnonymousObjectType();
    // Inferred property
    instanceObj.defineInferredProperty("x", registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE), null);
    assertTrue("ObjectType with inferred subtype property should be subtype of record",
        RecordType.isSubtype(instanceObj, targetRecord));

    // Create an ObjectType with a declared property whose type is a subtype but NOT equivalent
    ObjectType declaredObj = registry.createAnonymousObjectType();
    declaredObj.defineDeclaredProperty("x", registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE), null);
    assertFalse("ObjectType with declared strict-subtype property should NOT be subtype of record (requires strict equivalence)",
        RecordType.isSubtype(declaredObj, targetRecord));

    // Create an ObjectType where property type is unknown
    ObjectType unknownPropObj = registry.createAnonymousObjectType();
    unknownPropObj.defineDeclaredProperty("x", registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), null);
    assertTrue("Unknown property should satisfy subtyping check",
        RecordType.isSubtype(unknownPropObj, targetRecord));
  }

  @Test(timeout = 4000)
  public void testSubtypingTopObject() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("x", numberType, null)
        .build();

    // Top of record types is OBJECT_TYPE
    assertTrue("Record is a subtype of OBJECT_TYPE", record.isSubtype(objectNativeType));
    assertTrue("Record is a subtype of ALL_TYPE", record.isSubtype(registry.getNativeType(JSTypeNative.ALL_TYPE)));
  }

  // =========================================================================
  // Partition C: Greatest Subtype Helper
  // =========================================================================

  @Test(timeout = 4000)
  public void testGreatestSubtypeHelperWithCompatibleRecord() {
    RecordType rec1 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    RecordType rec2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("b", stringType, null)
        .build();

    JSType greatestSubtype = rec1.getGreatestSubtypeHelper(rec2);
    assertTrue(greatestSubtype.isRecordType());
    RecordType res = greatestSubtype.toMaybeRecordType();
    assertTrue(res.hasProperty("a"));
    assertTrue(res.hasProperty("b"));
    assertTrue(res.getPropertyType("a").isEquivalentTo(numberType));
    assertTrue(res.getPropertyType("b").isEquivalentTo(stringType));
  }

  @Test(timeout = 4000)
  public void testGreatestSubtypeHelperWithConflictingPropertyTypes() {
    RecordType rec1 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    RecordType rec2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", stringType, null)
        .build();

    JSType greatestSubtype = rec1.getGreatestSubtypeHelper(rec2);
    assertEquals("Conflicting property types must result in NO_TYPE",
        registry.getNativeObjectType(JSTypeNative.NO_TYPE), greatestSubtype);
  }

  @Test(timeout = 4000)
  public void testGreatestSubtypeHelperWithNonRecordType() {
    RecordType rec = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    // Greatest subtype with a primitive type (which has empty restriction to OBJECT_TYPE)
    JSType result = rec.getGreatestSubtypeHelper(numberType);
    assertEquals(registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE), result);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testConstructorWithNullRecordPropertyThrowsException() {
    Map<String, RecordProperty> propMap = new HashMap<String, RecordProperty>();
    propMap.put("invalidProp", null);
    new RecordType(registry, propMap);
  }

  @Test(timeout = 4000)
  public void testResolveInternalWithUnresolvedAndResolvedTypes() {
    NamedType unresolvableNamed = new NamedType(registry, "CustomType", null, -1, -1);
    RecordType rec = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", unresolvableNamed, null)
        .addProperty("b", numberType, null)
        .build();

    StaticScope<JSType> scope = registry.getTopScope();
    JSType resolved = rec.resolveInternal(errorReporter, scope);

    assertNotNull(resolved);
    assertTrue(resolved.isRecordType());
    // "a" should have been resolved (to UnknownType since "CustomType" is undefined)
    JSType resolvedPropA = resolved.toMaybeRecordType().getPropertyType("a");
    assertNotNull(resolvedPropA);
  }

  // =========================================================================
  // Partition E: Defect-Targeted Branch Zone (Ground Truth from Defects4J)
  // =========================================================================

  /**
   * Targets Defects4J known failure:
   * JSTypeTest::testRecordTypeLeastSuperType2
   * RecordTypeTest::testSupAndInf
   *
   * The least supertype of two distinct records with overlapping properties
   * MUST be their union type ({a: number, b: number}|{b: number, c: number}),
   * NOT an artificially stripped record type containing only common properties ({b: number}).
   */
  @Test(timeout = 4000)
  public void testRecordTypeLeastSuperTypeOverlappingPropertiesDefect() {
    RecordType rec1 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .addProperty("b", numberType, null)
        .build();

    RecordType rec2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("b", numberType, null)
        .addProperty("c", numberType, null)
        .build();

    JSType leastSupertype = rec1.getLeastSupertype(rec2);
    JSType expectedUnion = registry.createUnionType(rec1, rec2);

    assertEquals("Least supertype of distinct record types must be their UnionType",
        expectedUnion, leastSupertype);
  }

  /**
   * Targets Defects4J known failure:
   * JSTypeTest::testRecordTypeLeastSuperType3
   *
   * When two record types have completely disjoint properties, their least
   * supertype must be their union type, NOT an empty record type.
   */
  @Test(timeout = 4000)
  public void testRecordTypeLeastSuperTypeDisjointPropertiesDefect() {
    RecordType rec1 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .addProperty("b", stringType, null)
        .build();

    RecordType rec2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("d", numberType, null)
        .addProperty("e", stringType, null)
        .addProperty("f", stringType, null)
        .build();

    JSType leastSupertype = rec1.getLeastSupertype(rec2);
    JSType expectedUnion = registry.createUnionType(rec1, rec2);

    assertEquals("Least supertype of disjoint record types must be their UnionType",
        expectedUnion, leastSupertype);
  }

  @Test(timeout = 4000)
  public void testRecordTypeLeastSuperTypeWithNonRecord() {
    RecordType rec = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    JSType leastSupertype = rec.getLeastSupertype(numberType);
    JSType expectedUnion = registry.createUnionType(rec, numberType);

    assertEquals("Least supertype with non-record must delegate to union creation",
        expectedUnion, leastSupertype);
  }
}