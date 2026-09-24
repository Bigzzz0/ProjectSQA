package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.jstype.RecordTypeBuilder.RecordProperty;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.rhino.jstype.RecordType
 * Known Defect Reference: Defects4J Closure / Issue 725 (TypeCheckTest::testIssue725)
 * ---------------------------------------------------------------------------------------------------------------------
 * METHOD                          | TARGET DECISION / BRANCH / EDGE CASE                                 | TEST METHOD(S)
 * ---------------------------------------------------------------------------------------------------------------------
 * <init>                          | Map with null RecordProperty -> throws IllegalStateException         | testConstructor_nullPropertyThrowsException
 *                                 | Empty properties map -> empty frozen RecordType                      | testConstructor_emptyPropertiesMap
 *                                 | Multiple declared properties populated and type frozen               | testConstructor_validPropertiesFreezesType
 * ---------------------------------------------------------------------------------------------------------------------
 * isEquivalentTo                  | !other.isRecordType() -> false                                       | testIsEquivalentTo_nonRecordTypesReturnFalse
 *                                 | this == otherRecord -> true                                          | testIsEquivalentTo_sameInstanceReturnsTrue
 *                                 | Different key sets (size/names) -> false                             | testIsEquivalentTo_differentKeySets
 *                                 | Same keys, different property types -> false                         | testIsEquivalentTo_differentPropertyTypes
 *                                 | Same keys, equivalent property types -> true                         | testIsEquivalentTo_equivalentPropertiesReturnTrue
 * ---------------------------------------------------------------------------------------------------------------------
 * getImplicitPrototype            | Returns ObjectType (OBJECT_TYPE native)                              | testGetImplicitPrototype_returnsNativeObjectType
 * ---------------------------------------------------------------------------------------------------------------------
 * defineProperty                  | isFrozen == true -> returns false (mutation disallowed)              | testDefineProperty_frozenRejectsNewProperties
 *                                 | Attempt inferred vs declared defineProperty while frozen             | testDefineProperty_cannotMutateViaInferredOrDeclared
 * ---------------------------------------------------------------------------------------------------------------------
 * getGreatestSubtypeHelper        | that.isRecordType() == true:                                         |
 *                                 |   - Conflicting property types -> returns NO_TYPE                    | testGetGreatestSubtypeHelper_recordWithConflictReturnsNoType
 *                                 |   - Merges disjoint/shared compatible properties into builder        | testGetGreatestSubtypeHelper_recordsMergedCompatible
 *                                 | that.isRecordType() == false:                                        |
 *                                 |   - that is non-object (e.g. NUMBER_TYPE) -> returns NO_OBJECT_TYPE   | testGetGreatestSubtypeHelper_nonObjectTypeReturnsNoObjectType
 *                                 |   - that is ObjectType with reference types having matching property | testGetGreatestSubtypeHelper_objectTypeWithMatchingReferences
 *                                 |   - that is ObjectType without matching reference property types     | testGetGreatestSubtypeHelper_objectTypeWithoutMatchingReferences
 * ---------------------------------------------------------------------------------------------------------------------
 * toMaybeRecordType               | Returns this                                                         | testToMaybeRecordType_returnsSameInstance
 * ---------------------------------------------------------------------------------------------------------------------
 * isSubtype                       | JSType.isSubtypeHelper(this, that) returns true                      | testIsSubtype_subtypeHelperShortCircuit
 *                                 | OBJECT_TYPE.isSubtype(that) returns true                             | testIsSubtype_topLevelObjectTypeIsSubtype
 *                                 | !that.isRecordType() returns false                                   | testIsSubtype_nonRecordTypeReturnsFalse
 *                                 | that is record type -> delegates to RecordType.isSubtype(A, B)       | testIsSubtype_recordTypeDelegation
 * ---------------------------------------------------------------------------------------------------------------------
 * isSubtype(ObjectType, RecordType)| typeA missing property of typeB -> false                            | testStaticIsSubtype_missingPropertyReturnsFalse
 *                                 | Unknown types in propA or propB bypass equivalence checks            | testStaticIsSubtype_unknownPropertiesBypassEquivalence
 *                                 | typeA property declared and not equivalent -> false                  | testStaticIsSubtype_declaredPropertyNonEquivalentReturnsFalse
 *                                 | typeA property declared and equivalent -> true                       | testStaticIsSubtype_declaredPropertyEquivalentReturnsTrue
 *                                 | typeA property inferred and not subtype -> false                     | testStaticIsSubtype_inferredPropertyNonSubtypeReturnsFalse
 *                                 | typeA property inferred and is subtype -> true                       | testStaticIsSubtype_inferredPropertySubtypeReturnsTrue
 * ---------------------------------------------------------------------------------------------------------------------
 * resolveInternal                 | Properties contain unresolved NamedType -> replaced with resolved    | testResolveInternal_resolvesNamedTypes
 *                                 | Properties already resolved -> identity preserved                    | testResolveInternal_alreadyResolvedPreserved
 * ---------------------------------------------------------------------------------------------------------------------
 * Defect Zone (Closure Issue 725) | Record type rejects undeclared properties, prevents definition of    | testIssue725_recordTypeRejectsUndeclaredPropertyAndCannotBeExtended
 *                                 | new properties, and enforces strict property containment             | testIssue725_subtypingEnforcesStructuralClosure
 */
public class RecordTypeGeminiTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter errorReporter;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType unknownType;
  private JSType allType;
  private ObjectType objectType;

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(errorReporter);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
  }

  // ===================================================================================================================
  // Helper Methods
  // ===================================================================================================================

  private RecordType createRecord(Map<String, JSType> propMap) {
    Map<String, RecordProperty> recordProps = new LinkedHashMap<String, RecordProperty>();
    for (Map.Entry<String, JSType> entry : propMap.entrySet()) {
      recordProps.put(entry.getKey(), new RecordProperty(entry.getValue(), null));
    }
    return new RecordType(registry, recordProps);
  }

  private RecordType createSinglePropRecord(String name, JSType type) {
    Map<String, RecordProperty> recordProps = new HashMap<String, RecordProperty>();
    recordProps.put(name, new RecordProperty(type, null));
    return new RecordType(registry, recordProps);
  }

  // ===================================================================================================================
  // Partition A: Core Functional Logic & State Transitions
  // ===================================================================================================================

  @Test(timeout = 4000)
  public void testConstructor_validPropertiesFreezesType() {
    Map<String, RecordProperty> map = new LinkedHashMap<String, RecordProperty>();
    Node propNode = Node.newString("age");
    map.put("age", new RecordProperty(numberType, propNode));
    map.put("name", new RecordProperty(stringType, null));

    RecordType record = new RecordType(registry, map);

    assertTrue(record.isRecordType());
    assertTrue(record.hasProperty("age"));
    assertTrue(record.hasProperty("name"));
    assertFalse(record.hasProperty("address"));

    assertEquals(numberType, record.getPropertyType("age"));
    assertEquals(stringType, record.getPropertyType("name"));
    assertTrue(record.isPropertyTypeDeclared("age"));
    assertTrue(record.isPropertyTypeDeclared("name"));
    assertFalse(record.isPropertyTypeInferred("age"));
    assertSame(propNode, record.getPropertyNode("age"));
  }

  @Test(timeout = 4000)
  public void testGetImplicitPrototype_returnsNativeObjectType() {
    RecordType record = createSinglePropRecord("x", numberType);
    ObjectType proto = record.getImplicitPrototype();
    assertNotNull(proto);
    assertSame(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), proto);
  }

  @Test(timeout = 4000)
  public void testToMaybeRecordType_returnsSameInstance() {
    RecordType record = createSinglePropRecord("x", numberType);
    assertSame(record, record.toMaybeRecordType());
  }

  @Test(timeout = 4000)
  public void testIsEquivalentTo_sameInstanceReturnsTrue() {
    RecordType record = createSinglePropRecord("x", numberType);
    assertTrue(record.isEquivalentTo(record));
  }

  @Test(timeout = 4000)
  public void testIsEquivalentTo_equivalentPropertiesReturnTrue() {
    Map<String, JSType> props1 = new LinkedHashMap<String, JSType>();
    props1.put("a", numberType);
    props1.put("b", stringType);

    Map<String, JSType> props2 = new LinkedHashMap<String, JSType>();
    props2.put("b", stringType);
    props2.put("a", numberType);

    RecordType rt1 = createRecord(props1);
    RecordType rt2 = createRecord(props2);

    assertTrue(rt1.isEquivalentTo(rt2));
    assertTrue(rt2.isEquivalentTo(rt1));
  }

  @Test(timeout = 4000)
  public void testIsEquivalentTo_nonRecordTypesReturnFalse() {
    RecordType record = createSinglePropRecord("x", numberType);
    assertFalse(record.isEquivalentTo(numberType));
    assertFalse(record.isEquivalentTo(objectType));
    assertFalse(record.isEquivalentTo(allType));
    assertFalse(record.isEquivalentTo(null));
  }

  // ===================================================================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // ===================================================================================================================

  @Test(timeout = 4000)
  public void testConstructor_emptyPropertiesMap() {
    RecordType emptyRecord = new RecordType(registry, Collections.<String, RecordProperty>emptyMap());
    assertTrue(emptyRecord.isRecordType());
    assertFalse(emptyRecord.hasProperty("any"));
    assertSame(emptyRecord, emptyRecord.toMaybeRecordType());
    assertTrue(emptyRecord.isEquivalentTo(emptyRecord));

    RecordType emptyRecord2 = new RecordType(registry, Collections.<String, RecordProperty>emptyMap());
    assertTrue(emptyRecord.isEquivalentTo(emptyRecord2));
  }

  @Test(timeout = 4000)
  public void testIsEquivalentTo_differentKeySets() {
    Map<String, JSType> map1 = new HashMap<String, JSType>();
    map1.put("a", numberType);

    Map<String, JSType> map2 = new HashMap<String, JSType>();
    map2.put("a", numberType);
    map2.put("b", stringType);

    Map<String, JSType> map3 = new HashMap<String, JSType>();
    map3.put("b", numberType);

    RecordType rt1 = createRecord(map1);
    RecordType rt2 = createRecord(map2);
    RecordType rt3 = createRecord(map3);

    assertFalse(rt1.isEquivalentTo(rt2));
    assertFalse(rt2.isEquivalentTo(rt1));
    assertFalse(rt1.isEquivalentTo(rt3));
  }

  @Test(timeout = 4000)
  public void testIsEquivalentTo_differentPropertyTypes() {
    RecordType rt1 = createSinglePropRecord("x", numberType);
    RecordType rt2 = createSinglePropRecord("x", stringType);

    assertFalse(rt1.isEquivalentTo(rt2));
    assertFalse(rt2.isEquivalentTo(rt1));
  }

  @Test(timeout = 4000)
  public void testDefineProperty_frozenRejectsNewProperties() {
    RecordType record = createSinglePropRecord("x", numberType);

    boolean added = record.defineProperty("y", stringType, false, null);
    assertFalse(added);
    assertFalse(record.hasProperty("y"));
  }

  @Test(timeout = 4000)
  public void testDefineProperty_cannotMutateViaInferredOrDeclared() {
    RecordType record = createSinglePropRecord("x", numberType);

    assertFalse(record.defineDeclaredProperty("y", stringType, null));
    assertFalse(record.defineInferredProperty("z", booleanType, null));
    assertFalse(record.defineProperty("x", stringType, true, null));
    assertEquals(numberType, record.getPropertyType("x"));
  }

  // ===================================================================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Issue 725)
  // ===================================================================================================================

  /**
   * Targets Defects4J Closure Issue 725:
   * Record types are closed structural shapes. Undeclared properties must not be accepted or added,
   * defineProperty must return false, and subtyping must ensure that an object missing a property
   * or expecting undeclared extension is rejected.
   */
  @Test(timeout = 4000)
  public void testIssue725_recordTypeRejectsUndeclaredPropertyAndCannotBeExtended() {
    // Simulates {name: string} from testIssue725
    RecordType personRecord = createSinglePropRecord("name", stringType);

    // 1. Verify property "bar" does not exist and cannot be defined (x.bar = 3)
    assertFalse(personRecord.hasProperty("bar"));
    assertEquals(unknownType, personRecord.getPropertyType("bar"));
    assertFalse(personRecord.isPropertyTypeDeclared("bar"));
    assertFalse(personRecord.isPropertyTypeInferred("bar"));

    // 2. Attempting to define property on frozen record type MUST fail
    boolean definedDeclared = personRecord.defineDeclaredProperty("bar", numberType, null);
    assertFalse("RecordType should not allow declaring new properties after creation", definedDeclared);

    boolean definedInferred = personRecord.defineInferredProperty("bar", numberType, null);
    assertFalse("RecordType should not allow inferring new properties after creation", definedInferred);

    assertFalse(personRecord.hasProperty("bar"));
  }

  @Test(timeout = 4000)
  public void testIssue725_subtypingEnforcesStructuralClosure() {
    RecordType baseShape = createSinglePropRecord("name", stringType);

    Map<String, JSType> extendedMap = new LinkedHashMap<String, JSType>();
    extendedMap.put("name", stringType);
    extendedMap.put("bar", numberType);
    RecordType extendedShape = createRecord(extendedMap);

    // {name: string, bar: number} is a subtype of {name: string}
    assertTrue(extendedShape.isSubtype(baseShape));

    // {name: string} is NOT a subtype of {name: string, bar: number}
    assertFalse(baseShape.isSubtype(extendedShape));
  }

  // ===================================================================================================================
  // Partition D: Exception & Defensive Guard Paths
  // ===================================================================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testConstructor_nullPropertyThrowsException() {
    Map<String, RecordProperty> map = new HashMap<String, RecordProperty>();
    map.put("invalid", null);
    new RecordType(registry, map);
  }

  // ===================================================================================================================
  // Partition E: Subtyping & Greatest Subtype (Branch & Logic Completeness)
  // ===================================================================================================================

  @Test(timeout = 4000)
  public void testIsSubtype_subtypeHelperShortCircuit() {
    RecordType record = createSinglePropRecord("x", numberType);
    assertTrue(record.isSubtype(record));
    assertTrue(record.isSubtype(unknownType));
    assertTrue(record.isSubtype(allType));
  }

  @Test(timeout = 4000)
  public void testIsSubtype_topLevelObjectTypeIsSubtype() {
    RecordType record = createSinglePropRecord("x", numberType);
    assertTrue(record.isSubtype(objectType));
  }

  @Test(timeout = 4000)
  public void testIsSubtype_nonRecordTypeReturnsFalse() {
    RecordType record = createSinglePropRecord("x", numberType);
    assertFalse(record.isSubtype(numberType));
    assertFalse(record.isSubtype(stringType));

    ObjectType arrayType = registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);
    assertFalse(record.isSubtype(arrayType));
  }

  @Test(timeout = 4000)
  public void testIsSubtype_recordTypeDelegation() {
    Map<String, JSType> props1 = new LinkedHashMap<String, JSType>();
    props1.put("a", numberType);
    props1.put("b", stringType);

    RecordType wideRecord = createRecord(props1);
    RecordType narrowRecordA = createSinglePropRecord("a", numberType);
    RecordType narrowRecordB = createSinglePropRecord("b", stringType);
    RecordType differentTypeRecord = createSinglePropRecord("a", stringType);

    assertTrue(wideRecord.isSubtype(narrowRecordA));
    assertTrue(wideRecord.isSubtype(narrowRecordB));
    assertFalse(narrowRecordA.isSubtype(wideRecord));
    assertFalse(wideRecord.isSubtype(differentTypeRecord));
  }

  @Test(timeout = 4000)
  public void testStaticIsSubtype_missingPropertyReturnsFalse() {
    RecordType rtA = createSinglePropRecord("a", numberType);
    RecordType rtB = createSinglePropRecord("b", numberType);

    assertFalse(RecordType.isSubtype(rtA, rtB));
  }

  @Test(timeout = 4000)
  public void testStaticIsSubtype_unknownPropertiesBypassEquivalence() {
    RecordType rtUnknownA = createSinglePropRecord("a", unknownType);
    RecordType rtNumberA = createSinglePropRecord("a", numberType);

    assertTrue(RecordType.isSubtype(rtUnknownA, rtNumberA));
    assertTrue(RecordType.isSubtype(rtNumberA, rtUnknownA));
  }

  @Test(timeout = 4000)
  public void testStaticIsSubtype_declaredPropertyEquivalentReturnsTrue() {
    RecordType rt1 = createSinglePropRecord("a", numberType);
    RecordType rt2 = createSinglePropRecord("a", numberType);

    assertTrue(RecordType.isSubtype(rt1, rt2));
  }

  @Test(timeout = 4000)
  public void testStaticIsSubtype_declaredPropertyNonEquivalentReturnsFalse() {
    RecordType rt1 = createSinglePropRecord("a", numberType);
    RecordType rt2 = createSinglePropRecord("a", stringType);

    assertFalse(RecordType.isSubtype(rt1, rt2));
  }

  @Test(timeout = 4000)
  public void testStaticIsSubtype_inferredPropertySubtypeReturnsTrue() {
    // Custom ObjectType with inferred property
    PrototypeObjectType customObj = new PrototypeObjectType(registry, "Custom", objectType);
    // propA is NUMBER_TYPE (inferred)
    customObj.defineInferredProperty("val", numberType, null);
    assertTrue(customObj.isPropertyTypeInferred("val"));

    // propB is (NUMBER_TYPE | STRING_TYPE)
    JSType unionType = registry.createUnionType(numberType, stringType);
    RecordType rtTarget = createSinglePropRecord("val", unionType);

    // Inferred property type is a subtype of target property type -> should pass
    assertTrue(RecordType.isSubtype(customObj, rtTarget));
  }

  @Test(timeout = 4000)
  public void testStaticIsSubtype_inferredPropertyNonSubtypeReturnsFalse() {
    PrototypeObjectType customObj = new PrototypeObjectType(registry, "Custom", objectType);
    customObj.defineInferredProperty("val", stringType, null);

    RecordType rtTarget = createSinglePropRecord("val", numberType);

    // STRING_TYPE is not a subtype of NUMBER_TYPE -> should fail
    assertFalse(RecordType.isSubtype(customObj, rtTarget));
  }

  @Test(timeout = 4000)
  public void testGetGreatestSubtypeHelper_recordWithConflictReturnsNoType() {
    RecordType rt1 = createSinglePropRecord("conflict", numberType);
    RecordType rt2 = createSinglePropRecord("conflict", stringType);

    JSType greatestSubtype = rt1.getGreatestSubtypeHelper(rt2);
    assertSame(registry.getNativeObjectType(JSTypeNative.NO_TYPE), greatestSubtype);
  }

  @Test(timeout = 4000)
  public void testGetGreatestSubtypeHelper_recordsMergedCompatible() {
    Map<String, JSType> props1 = new LinkedHashMap<String, JSType>();
    props1.put("a", numberType);
    props1.put("shared", booleanType);
    RecordType rt1 = createRecord(props1);

    Map<String, JSType> props2 = new LinkedHashMap<String, JSType>();
    props2.put("b", stringType);
    props2.put("shared", booleanType);
    RecordType rt2 = createRecord(props2);

    JSType greatestSubtype = rt1.getGreatestSubtypeHelper(rt2);
    assertTrue(greatestSubtype.isRecordType());

    RecordType merged = greatestSubtype.toMaybeRecordType();
    assertTrue(merged.hasProperty("a"));
    assertTrue(merged.hasProperty("b"));
    assertTrue(merged.hasProperty("shared"));
    assertEquals(numberType, merged.getPropertyType("a"));
    assertEquals(stringType, merged.getPropertyType("b"));
    assertEquals(booleanType, merged.getPropertyType("shared"));
  }

  @Test(timeout = 4000)
  public void testGetGreatestSubtypeHelper_nonObjectTypeReturnsNoObjectType() {
    RecordType rt = createSinglePropRecord("a", numberType);

    JSType result = rt.getGreatestSubtypeHelper(numberType);
    assertSame(registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE), result);
  }

  @Test(timeout = 4000)
  public void testGetGreatestSubtypeHelper_objectTypeWithMatchingReferences() {
    // Record type has "length" which is defined on Array native reference type
    RecordType rt = createSinglePropRecord("length", numberType);

    JSType result = rt.getGreatestSubtypeHelper(objectType);
    assertNotNull(result);
    assertFalse(result.isEmptyType());
  }

  @Test(timeout = 4000)
  public void testGetGreatestSubtypeHelper_objectTypeWithoutMatchingReferences() {
    // Record type with a property that no reference type has
    RecordType rt = createSinglePropRecord("nonExistentUniqueProp12345", numberType);

    JSType result = rt.getGreatestSubtypeHelper(objectType);
    assertSame(registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE), result);
  }

  // ===================================================================================================================
  // Partition F: Type Resolution Logic
  // ===================================================================================================================

  @Test(timeout = 4000)
  public void testResolveInternal_alreadyResolvedPreserved() {
    RecordType rt = createSinglePropRecord("a", numberType);
    JSType resolved = rt.resolve(errorReporter, null);
    assertSame(rt, resolved);
    assertEquals(numberType, rt.getPropertyType("a"));
  }

  @Test(timeout = 4000)
  public void testResolveInternal_resolvesNamedTypes() {
    NamedType namedType = registry.createNamedType("CustomNamedType", "test.js", 1, 1);

    Map<String, RecordProperty> map = new HashMap<String, RecordProperty>();
    map.put("ref", new RecordProperty(namedType, null));
    RecordType rt = new RecordType(registry, map);

    assertEquals(namedType, rt.getPropertyType("ref"));

    JSType resolved = rt.resolve(errorReporter, null);
    assertNotNull(resolved);
    assertTrue(resolved.isRecordType());

    // After resolution, unresolved named type is replaced with the resolved type representation (unknownType)
    JSType refTypeAfterResolve = rt.getPropertyType("ref");
    assertNotSame(namedType, refTypeAfterResolve);
    assertTrue(refTypeAfterResolve.isUnknownType());
  }
}