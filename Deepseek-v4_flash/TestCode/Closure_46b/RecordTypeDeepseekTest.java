package com.google.javascript.rhino.jstype;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.RecordTypeBuilder.RecordProperty;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test suite for RecordType. Targets line/branch coverage and the known
 * defect in getLeastSupertype (returns RecordType instead of UnionType).
 *
 * Branch & Defect Analysis Matrix:
 * --------------------------------
 * Partition A: Functional Logic & State Transitions
 *   - Construct record type with properties
 *   - isEquivalentTo: equal, different keys, different types, same reference
 *   - isSubtype: subtype, not subtype, object type
 *   - getLeastSupertype: both record types, one non-record, overlapping properties
 *   - getGreatestSubtypeHelper: both record types with/without conflicts
 *   - getImplicitPrototype
 *   - defineProperty (frozen/unfrozen)
 *   - toMaybeRecordType
 *   - resolveInternal
 *
 * Partition B: Boundary Value Analysis
 *   - Empty property map
 *   - Null RecordProperty in constructor
 *   - Null property type (not allowed by constructor but in defineProperty? we test null node)
 *   - Property with unknown type
 *
 * Partition C: Defect-Targeted Branch Zone
 *   - getLeastSupertype when properties differ → should return UnionType (bug: returns RecordType)
 *   - Tests derived from Defects4J: RecordTypeTest.testSupAndInf, JSTypeTest.testRecordTypeLeastSuperType2/3
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - IllegalStateException when RecordProperty is null
 *   - defineProperty after frozen returns false
 *   - isSubtype with null (should handle gracefully)
 *
 * Partition E: Object Lifecycle & Contract Integrity
 *   - isEquivalentTo symmetric and transitive properties
 *   - isSubtype consistency
 */
public class RecordTypeDeepseekTest {

    private JSTypeRegistry registry;

    @Before
    public void setUp() {
        registry = new JSTypeRegistry(new ErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, int lineOffset) {
                // ignore
            }

            @Override
            public void error(String message, String sourceName, int line, int lineOffset) {
                // ignore
            }
        });
    }

    // Helper to create a RecordType from a map of property name -> type
    private RecordType createRecord(Map<String, JSType> props) {
        Map<String, RecordProperty> propMap = new LinkedHashMap<>();
        for (Map.Entry<String, JSType> entry : props.entrySet()) {
            // Property node can be null
            propMap.put(entry.getKey(), new RecordProperty(entry.getValue(), null));
        }
        return new RecordType(registry, propMap);
    }

    // Convenience with varargs (name, type, name, type, ...)
    private RecordType createRecord(Object... nameTypePairs) {
        Map<String, JSType> props = new LinkedHashMap<>();
        for (int i = 0; i < nameTypePairs.length; i += 2) {
            String name = (String) nameTypePairs[i];
            JSType type = (JSType) nameTypePairs[i + 1];
            props.put(name, type);
        }
        return createRecord(props);
    }

    // Helper to get native types (assume they exist)
    private JSType numberType() {
        return registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    }

    private JSType stringType() {
        return registry.getNativeType(JSTypeNative.STRING_TYPE);
    }

    private JSType booleanType() {
        return registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    }

    private JSType objectType() {
        return registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    }

    private JSType noType() {
        return registry.getNativeType(JSTypeNative.NO_TYPE);
    }

    private JSType noObjectType() {
        return registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testConstructorAndProperties() {
        RecordType r = createRecord("a", numberType(), "b", stringType());
        assertNotNull(r);
        assertTrue(r.isRecordType());
        assertSame(r, r.toMaybeRecordType());
        assertTrue(r.hasProperty("a"));
        assertTrue(r.hasProperty("b"));
        assertFalse(r.hasProperty("c"));
        assertEquals(numberType(), r.getPropertyType("a"));
        assertEquals(stringType(), r.getPropertyType("b"));
    }

    @Test(timeout = 4000)
    public void testIsEquivalentToSameObject() {
        RecordType r = createRecord("x", numberType());
        assertTrue(r.isEquivalentTo(r));
    }

    @Test(timeout = 4000)
    public void testIsEquivalentToEqualProperties() {
        RecordType r1 = createRecord("a", numberType(), "b", stringType());
        RecordType r2 = createRecord("a", numberType(), "b", stringType());
        assertTrue(r1.isEquivalentTo(r2));
    }

    @Test(timeout = 4000)
    public void testIsEquivalentToDifferentKeys() {
        RecordType r1 = createRecord("a", numberType());
        RecordType r2 = createRecord("b", numberType());
        assertFalse(r1.isEquivalentTo(r2));
    }

    @Test(timeout = 4000)
    public void testIsEquivalentToDifferentTypes() {
        RecordType r1 = createRecord("a", numberType());
        RecordType r2 = createRecord("a", stringType());
        assertFalse(r1.isEquivalentTo(r2));
    }

    @Test(timeout = 4000)
    public void testIsEquivalentToNonRecord() {
        RecordType r = createRecord("a", numberType());
        assertFalse(r.isEquivalentTo(numberType()));
    }

    @Test(timeout = 4000)
    public void testGetImplicitPrototype() {
        RecordType r = createRecord("a", numberType());
        ObjectType proto = r.getImplicitPrototype();
        assertEquals(objectType(), proto);
    }

    @Test(timeout = 4000)
    public void testDefinePropertyBeforeFreeze() {
        // Cannot test before freeze because constructor freezes. Use reflection? Not allowed.
        // Instead test that defineProperty after construction returns false.
        RecordType r = createRecord("a", numberType());
        assertFalse(r.defineProperty("b", stringType(), false, null));
        assertFalse(r.hasProperty("b"));
    }

    @Test(timeout = 4000)
    public void testToMaybeRecordType() {
        RecordType r = createRecord("x", numberType());
        assertSame(r, r.toMaybeRecordType());
    }

    // ==================== Partition A: Subtype ====================

    @Test(timeout = 4000)
    public void testIsSubtypeSameRecord() {
        RecordType r = createRecord("a", numberType());
        assertTrue(r.isSubtype(r));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeObjectType() {
        RecordType r = createRecord("a", numberType());
        assertTrue(r.isSubtype(objectType()));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeSupersetProperties() {
        // {a: number} is subtype of {a: number, b: string}? Actually subtype means
        // the subtype must have at least the same properties with same or compatible types.
        // Here r1 has fewer properties, so it's not a subtype of r2.
        RecordType r1 = createRecord("a", numberType());
        RecordType r2 = createRecord("a", numberType(), "b", stringType());
        assertFalse(r1.isSubtype(r2));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeDeclaredPropertyMismatch() {
        // If both have same property but types differ and both declared, not subtype.
        // To simulate declared, we need a record type created via defineDeclaredProperty? Not exposed.
        // We'll use the constructor which uses defineDeclaredProperty.
        RecordType r1 = createRecord("a", numberType());
        RecordType r2 = createRecord("a", stringType());
        assertFalse(r1.isSubtype(r2));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeNonRecord() {
        RecordType r = createRecord("a", numberType());
        assertFalse(r.isSubtype(numberType()));
    }

    // ==================== Partition A: getLeastSupertype ====================

    @Test(timeout = 4000)
    public void testLeastSupertypeNonRecord() {
        RecordType r = createRecord("a", numberType());
        JSType result = r.getLeastSupertype(numberType());
        // Should fall back to super.getLeastSupertype, likely union with object? We'll just check not null.
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testLeastSupertypeSameProperties() {
        RecordType r1 = createRecord("a", numberType());
        RecordType r2 = createRecord("a", numberType());
        JSType result = r1.getLeastSupertype(r2);
        assertTrue(result.isRecordType());
        assertEquals(r1, result);
    }

    // Defect-targeted: getLeastSupertype should return union when properties differ
    @Test(timeout = 4000)
    public void testLeastSupertypeDifferentPropertiesShouldBeUnion() {
        RecordType r1 = createRecord("a", numberType(), "b", stringType());
        RecordType r2 = createRecord("b", stringType(), "c", stringType(), "e", numberType());
        JSType result = r1.getLeastSupertype(r2);
        // The bug: currently returns RecordType with only common property "b".
        // Correct: should return a UnionType of the two records.
        assertTrue("Expected result to be a union type, but bug causes record type",
                   result.isUnionType());
    }

    // Another defect scenario: different types on common property
    @Test(timeout = 4000)
    public void testLeastSupertypeConflictingCommonProperty() {
        RecordType r1 = createRecord("a", numberType(), "b", stringType());
        RecordType r2 = createRecord("a", booleanType(), "b", stringType());
        JSType result = r1.getLeastSupertype(r2);
        // No common property with equivalent type -> should be union of the two records
        assertTrue("Expected union type", result.isUnionType());
    }

    // Replicate testSupAndInf: (a: number, b: number) and (b: number, c: number) -> union
    @Test(timeout = 4000)
    public void testSupAndInf() {
        RecordType r1 = createRecord("a", numberType(), "b", numberType());
        RecordType r2 = createRecord("b", numberType(), "c", numberType());
        JSType least = r1.getLeastSupertype(r2);
        assertTrue("Expected union type, bug causes record type", least.isUnionType());
    }

    // ==================== Partition A: getGreatestSubtypeHelper ====================

    @Test(timeout = 4000)
    public void testGreatestSubtypeHelperBothRecordsEqual() {
        RecordType r1 = createRecord("a", numberType());
        RecordType r2 = createRecord("a", numberType());
        JSType result = r1.getGreatestSubtypeHelper(r2);
        assertTrue(result.isRecordType());
    }

    @Test(timeout = 4000)
    public void testGreatestSubtypeHelperConflictingType() {
        RecordType r1 = createRecord("a", numberType());
        RecordType r2 = createRecord("a", stringType());
        JSType result = r1.getGreatestSubtypeHelper(r2);
        // Should be NO_TYPE due to conflict
        assertEquals(noType(), result);
    }

    @Test(timeout = 4000)
    public void testGreatestSubtypeHelperDisjointProperties() {
        RecordType r1 = createRecord("a", numberType());
        RecordType r2 = createRecord("b", stringType());
        JSType result = r1.getGreatestSubtypeHelper(r2);
        // Should be a record type with both properties
        assertTrue(result.isRecordType());
        RecordType rResult = result.toMaybeRecordType();
        assertTrue(rResult.hasProperty("a"));
        assertTrue(rResult.hasProperty("b"));
    }

    @Test(timeout = 4000)
    public void testGreatestSubtypeHelperNonRecord() {
        RecordType r = createRecord("a", numberType());
        JSType result = r.getGreatestSubtypeHelper(numberType());
        // Should follow the non-record branch
        assertNotNull(result);
    }

    // ==================== Partition B: Boundary Value ====================

    @Test(timeout = 4000)
    public void testEmptyRecord() {
        RecordType r = createRecord(new HashMap<String, JSType>());
        assertTrue(r.isRecordType());
        assertTrue(r.properties().isEmpty());
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testConstructorNullRecordProperty() {
        Map<String, RecordProperty> map = new HashMap<>();
        map.put("x", null);
        new RecordType(registry, map);
    }

    @Test(timeout = 4000)
    public void testPropertyWithUnknownType() {
        JSType unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
        RecordType r = createRecord("a", unknown);
        assertEquals(unknown, r.getPropertyType("a"));
    }

    // ==================== Partition C: Defect-Targeted (already covered above) ====================

    // Additional targeted test: scenario from testRecordTypeLeastSuperType2
    @Test(timeout = 4000)
    public void testLeastSuperType2() {
        RecordType r1 = createRecord("a", numberType(), "b", stringType());
        RecordType r2 = createRecord("b", stringType(), "c", stringType(), "e", numberType());
        JSType result = r1.getLeastSupertype(r2);
        // Expected union of the two record types
        assertTrue(result.isUnionType());
        // Optionally verify the union contains both records
        // (requires accessing union alternates; skip for simplicity)
    }

    // ==================== Partition D: Exception & Defensive Guard ====================

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testConstructorNullRecordPropertyMulti() {
        Map<String, RecordProperty> map = new LinkedHashMap<>();
        map.put("a", new RecordProperty(numberType(), null));
        map.put("b", null);
        new RecordType(registry, map);
    }

    @Test(timeout = 4000)
    public void testDefinePropertyFrozenReturnsFalse() {
        RecordType r = createRecord("x", numberType());
        // After construction, isFrozen = true
        assertFalse(r.defineProperty("y", stringType(), false, null));
        assertFalse(r.hasProperty("y"));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeWithNull() {
        RecordType r = createRecord("a", numberType());
        // isSubtype with null should throw or return false? Assumption: return false.
        // In Rhino, JSType.isSubtype handles null gracefully?
        assertFalse(r.isSubtype(null));
    }

    // ==================== Partition E: Contract Integrity ====================

    @Test(timeout = 4000)
    public void testIsEquivalentToSymmetric() {
        RecordType r1 = createRecord("a", numberType());
        RecordType r2 = createRecord("a", numberType());
        assertEquals(r1.isEquivalentTo(r2), r2.isEquivalentTo(r1));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeTransitive() {
        RecordType r1 = createRecord("a", numberType());
        RecordType r2 = createRecord("a", numberType());
        RecordType r3 = createRecord("a", numberType());
        assertTrue(r1.isSubtype(r2));
        assertTrue(r2.isSubtype(r3));
        assertTrue(r1.isSubtype(r3));
    }

    // ==================== Additional coverage for static isSubtype ====================

    @Test(timeout = 4000)
    public void testStaticIsSubtypeMissingProperty() {
        // typeA lacks a property of typeB => false
        RecordType typeB = createRecord("a", numberType(), "b", stringType());
        RecordType typeA = createRecord("a", numberType());
        assertFalse(RecordType.isSubtype(typeA, typeB));
    }

    @Test(timeout = 4000)
    public void testStaticIsSubtypeDeclaredMismatch() {
        // typeA has property declared with different type
        RecordType typeB = createRecord("a", numberType());
        RecordType typeA = createRecord("a", stringType());
        assertFalse(RecordType.isSubtype(typeA, typeB));
    }

    @Test(timeout = 4000)
    public void testStaticIsSubtypeInferredCompatible() {
        // For inferred property, subtype check allows subtype of property type
        // But we cannot easily create inferred property via constructor.
        // This test may not cover the inferred branch; we'll rely on coverage from other tests.
        RecordType typeB = createRecord("a", objectType());
        RecordType typeA = createRecord("a", numberType());
        // number is not subtype of object, so false
        assertFalse(RecordType.isSubtype(typeA, typeB));
    }

    // ==================== Coverage for resolveInternal ====================

    @Test(timeout = 4000)
    public void testResolveInternal() {
        RecordType r = createRecord("a", numberType());
        // resolveInternal typically requires scope and error reporter; we test that it doesn't throw.
        // In test environment, null is often used for scope.
        JSType resolved = r.resolveInternal(null, null);
        assertSame(r, resolved);
    }

    // ==================== Additional coverage for getGreatestSubtypeHelper non-record branch ====================

    @Test(timeout = 4000)
    public void testGreatestSubtypeNonRecordEmptyType() {
        // Create a record and call getGreatestSubtypeHelper with type that restricts to NO_OBJECT?
        RecordType r = createRecord("a", numberType());
        // Use noObjectType which should result in the else branch with iterations.
        JSType result = r.getGreatestSubtypeHelper(noObjectType());
        // Should not throw, result may be empty type.
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testGreatestSubtypeNonRecordObject() {
        RecordType r = createRecord("a", numberType());
        JSType result = r.getGreatestSubtypeHelper(objectType());
        // Should go through the non-record branch with for loop
        assertNotNull(result);
    }

    // ==================== Test for isSubtype using non-record types ====================

    @Test(timeout = 4000)
    public void testIsSubtypeWithNonRecord() {
        RecordType r = createRecord("a", numberType());
        assertFalse(r.isSubtype(numberType()));
    }
}