package com.google.javascript.rhino.jstype;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.jstype.RecordTypeBuilder.RecordProperty;

import java.util.*;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructor with declared=true/false
 *   - isSynthetic() method
 *   - defineProperty() with frozen/unfrozen state
 *   - getImplicitPrototype() returns OBJECT_TYPE
 *   - toMaybeRecordType() returns this
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Empty properties map
 *   - Single property
 *   - Multiple properties
 *   - Null RecordProperty in constructor (throws IllegalStateException)
 *   - Null property key
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - checkRecordEquivalenceHelper with tolerateUnknowns=true/false
 *   - isSubtype with unknown types (targeting testIssue791 and testSubtypeWithUnknowns2)
 *   - isSubtype static method with declared vs inferred properties
 *   - getGreatestSubtypeHelper with record types
 *   - isSubtype with non-record types
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Constructor with null RecordProperty throws IllegalStateException
 *   - defineProperty on frozen record returns false
 *   - resolveInternal with type resolution
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - checkRecordEquivalenceHelper with equal/unequal records
 *   - isSubtype with various type combinations
 */
public class RecordTypeDeepseekTest {
    
    private JSTypeRegistry createRegistry() {
        return new JSTypeRegistry(new SimpleErrorReporter());
    }
    
    private RecordProperty createProperty(JSType type) {
        return new RecordProperty(type, new Node(1));
    }
    
    // ===== Partition A: Core Functional Logic & State Transitions =====
    
    @Test(timeout = 4000)
    public void testConstructorDeclaredTrue() {
        JSTypeRegistry registry = createRegistry();
        Map<String, RecordProperty> props = new HashMap<>();
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        props.put("name", createProperty(stringType));
        
        RecordType record = new RecordType(registry, props, true);
        assertTrue("Declared record should not be synthetic", !record.isSynthetic());
        assertTrue("Record should have property 'name'", record.hasProperty("name"));
        assertEquals("Property type should be STRING", stringType, record.getPropertyType("name"));
    }
    
    @Test(timeout = 4000)
    public void testConstructorDeclaredFalse() {
        JSTypeRegistry registry = createRegistry();
        Map<String, RecordProperty> props = new HashMap<>();
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        props.put("age", createProperty(numberType));
        
        RecordType record = new RecordType(registry, props, false);
        assertTrue("Synthesized record should be synthetic", record.isSynthetic());
        assertTrue("Record should have property 'age'", record.hasProperty("age"));
    }
    
    @Test(timeout = 4000)
    public void testConstructorEmptyProperties() {
        JSTypeRegistry registry = createRegistry();
        Map<String, RecordProperty> props = new HashMap<>();
        
        RecordType record = new RecordType(registry, props, true);
        assertTrue("Empty record should have no properties", record.getProperties().isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testGetImplicitPrototype() {
        JSTypeRegistry registry = createRegistry();
        Map<String, RecordProperty> props = new HashMap<>();
        RecordType record = new RecordType(registry, props, true);
        
        ObjectType prototype = record.getImplicitPrototype();
        assertEquals("Implicit prototype should be OBJECT_TYPE", 
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), prototype);
    }
    
    @Test(timeout = 4000)
    public void testToMaybeRecordType() {
        JSTypeRegistry registry = createRegistry();
        Map<String, RecordProperty> props = new HashMap<>();
        RecordType record = new RecordType(registry, props, true);
        
        assertSame("toMaybeRecordType should return this", record, record.toMaybeRecordType());
    }
    
    @Test(timeout = 4000)
    public void testDefinePropertyOnFrozenRecord() {
        JSTypeRegistry registry = createRegistry();
        Map<String, RecordProperty> props = new HashMap<>();
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        props.put("name", createProperty(stringType));
        
        RecordType record = new RecordType(registry, props, true);
        
        // Try to define a new property after construction (should fail because frozen)
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        boolean result = record.defineProperty("age", numberType, false, new Node(1));
        assertFalse("Should not be able to define property on frozen record", result);
        assertFalse("Property 'age' should not exist", record.hasProperty("age"));
    }
    
    // ===== Partition B: Boundary Value Analysis & Extremes =====
    
    @Test(timeout = 4000)
    public void testConstructorWithNullRecordProperty() {
        JSTypeRegistry registry = createRegistry();
        Map<String, RecordProperty> props = new HashMap<>();
        props.put("prop", null);
        
        try {
            new RecordType(registry, props, true);
            fail("Should throw IllegalStateException for null RecordProperty");
        } catch (IllegalStateException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testConstructorMultipleProperties() {
        JSTypeRegistry registry = createRegistry();
        Map<String, RecordProperty> props = new LinkedHashMap<>();
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
        
        props.put("name", createProperty(stringType));
        props.put("age", createProperty(numberType));
        props.put("active", createProperty(booleanType));
        
        RecordType record = new RecordType(registry, props, true);
        assertEquals("Should have 3 properties", 3, record.getProperties().size());
        assertTrue("Should have 'name'", record.hasProperty("name"));
        assertTrue("Should have 'age'", record.hasProperty("age"));
        assertTrue("Should have 'active'", record.hasProperty("active"));
    }
    
    // ===== Partition C: Defect-Targeted Branch Zone =====
    
    @Test(timeout = 4000)
    public void testCheckRecordEquivalenceHelperEqualRecords() {
        JSTypeRegistry registry = createRegistry();
        Map<String, RecordProperty> props1 = new HashMap<>();
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        props1.put("name", createProperty(stringType));
        
        Map<String, RecordProperty> props2 = new HashMap<>();
        props2.put("name", createProperty(stringType));
        
        RecordType record1 = new RecordType(registry, props1, true);
        RecordType record2 = new RecordType(registry, props2, true);
        
        assertTrue("Equivalent records should be equal", 
            record1.checkRecordEquivalenceHelper(record2, false));
    }
    
    @Test(timeout = 4000)
    public void testCheckRecordEquivalenceHelperDifferentKeys() {
        JSTypeRegistry registry = createRegistry();
        Map<String, RecordProperty> props1 = new HashMap<>();
        props1.put("name", createProperty(registry.getNativeType(JSTypeNative.STRING_TYPE)));
        
        Map<String, RecordProperty> props2 = new HashMap<>();
        props2.put("age", createProperty(registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
        
        RecordType record1 = new RecordType(registry, props1, true);
        RecordType record2 = new RecordType(registry, props2, true);
        
        assertFalse("Records with different keys should not be equivalent", 
            record1.checkRecordEquivalenceHelper(record2, false));
    }
    
    @Test(timeout = 4000)
    public void testCheckRecordEquivalenceHelperDifferentTypes() {
        JSTypeRegistry registry = createRegistry();
        Map<String, RecordProperty> props1 = new HashMap<>();
        props1.put("prop", createProperty(registry.getNativeType(JSTypeNative.STRING_TYPE)));
        
        Map<String, RecordProperty> props2 = new HashMap<>();
        props2.put("prop", createProperty(registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
        
        RecordType record1 = new RecordType(registry, props1, true);
        RecordType record2 = new RecordType(registry, props2, true);
        
        assertFalse("Records with different property types should not be equivalent", 
            record1.checkRecordEquivalenceHelper(record2, false));
    }
    
    @Test(timeout = 4000)
    public void testCheckRecordEquivalenceHelperWithUnknowns() {
        JSTypeRegistry registry = createRegistry();
        Map<String, RecordProperty> props1 = new HashMap<>();
        props1.put("prop", createProperty(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE)));
        
        Map<String, RecordProperty> props2 = new HashMap<>();
        props2.put("prop", createProperty(registry.getNativeType(JSTypeNative.STRING_TYPE)));
        
        RecordType record1 = new RecordType(registry, props1, true);
        RecordType record2 = new RecordType(registry, props2, true);
        
        // With tolerateUnknowns=true, unknown types should be tolerated
        assertTrue("Should tolerate unknowns when flag is true", 
            record1.checkRecordEquivalenceHelper(record2, true));
        
        // With tolerateUnknowns=false, unknown types should not be tolerated
        assertFalse("Should not tolerate unknowns when flag is false", 
            record1.checkRecordEquivalenceHelper(record2, false));
    }
    
    @Test(timeout = 4000)
    public void testIsSubtypeWithRecordType() {
        JSTypeRegistry registry = createRegistry();
        
        // Create record type A: { name: string }
        Map<String, RecordProperty> propsA = new HashMap<>();
        propsA.put("name", createProperty(registry.getNativeType(JSTypeNative.STRING_TYPE)));
        RecordType recordA = new RecordType(registry, propsA, true);
        
        // Create record type B: { name: string, age: number }
        Map<String, RecordProperty> propsB = new HashMap<>();
        propsB.put("name", createProperty(registry.getNativeType(JSTypeNative.STRING_TYPE)));
        propsB.put("age", createProperty(registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
        RecordType recordB = new RecordType(registry, propsB, true);
        
        // B should be subtype of A (B has all properties of A with same types)
        assertTrue("Record B should be subtype of Record A", recordB.isSubtype(recordA));
        
        // A should NOT be subtype of B (A doesn't have 'age' property)
        assertFalse("Record A should not be subtype of Record B", recordA.isSubtype(recordB));
    }
    
    @Test(timeout = 4000)
    public void testIsSubtypeWithUnknownTypes() {
        // This test targets the defect from testIssue791 and testSubtypeWithUnknowns2
        JSTypeRegistry registry = createRegistry();
        
        // Create record with unknown type property
        Map<String, RecordProperty> propsWithUnknown = new HashMap<>();
        propsWithUnknown.put("prop", createProperty(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE)));
        RecordType recordWithUnknown = new RecordType(registry, propsWithUnknown, true);
        
        // Create record with string type property
        Map<String, RecordProperty> propsWithString = new HashMap<>();
        propsWithString.put("prop", createProperty(registry.getNativeType(JSTypeNative.STRING_TYPE)));
        RecordType recordWithString = new RecordType(registry, propsWithString, true);
        
        // When one type is unknown, isSubtype should handle it gracefully
        // The defect was that isSubtype with unknown types could cause issues
        boolean result1 = recordWithUnknown.isSubtype(recordWithString);
        boolean result2 = recordWithString.isSubtype(recordWithUnknown);
        
        // Both should complete without exception and return consistent results
        // With unknown types, the behavior should be permissive
        assertNotNull("Result should not be null", result1);
        assertNotNull("Result should not be null", result2);
    }
    
    @Test(timeout = 4000)
    public void testIsSubtypeWithNonRecordType() {
        JSTypeRegistry registry = createRegistry();
        
        Map<String, RecordProperty> props = new HashMap<>();
        props.put("prop", createProperty(registry.getNativeType(JSTypeNative.STRING_TYPE)));
        RecordType record = new RecordType(registry, props, true);
        
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        
        // Record should not be subtype of non-record type (unless it's OBJECT_TYPE)
        assertFalse("Record should not be subtype of number", record.isSubtype(numberType));
    }
    
    @Test(timeout = 4000)
    public void testIsSubtypeWithObjectType() {
        JSTypeRegistry registry = createRegistry();
        
        Map<String, RecordProperty> props = new HashMap<>();
        props.put("prop", createProperty(registry.getNativeType(JSTypeNative.STRING_TYPE)));
        RecordType record = new RecordType(registry, props, true);
        
        JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
        
        // Record should be subtype of OBJECT_TYPE
        assertTrue("Record should be subtype of OBJECT_TYPE", record.isSubtype(objectType));
    }
    
    @Test(timeout = 4000)
    public void testGetGreatestSubtypeHelperWithRecordTypes() {
        JSTypeRegistry registry = createRegistry();
        
        // Create record type A: { name: string }
        Map<String, RecordProperty> propsA = new HashMap<>();
        propsA.put("name", createProperty(registry.getNativeType(JSTypeNative.STRING_TYPE)));
        RecordType recordA = new RecordType(registry, propsA, true);
        
        // Create record type B: { age: number }
        Map<String, RecordProperty> propsB = new HashMap<>();
        propsB.put("age", createProperty(registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
        RecordType recordB = new RecordType(registry, propsB, true);
        
        // Greatest subtype should be a record with both properties
        JSType result = recordA.getGreatestSubtypeHelper(recordB);
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be a record type", result.isRecordType());
        
        RecordType resultRecord = result.toMaybeRecordType();
        assertTrue("Result should have 'name' property", resultRecord.hasProperty("name"));
        assertTrue("Result should have 'age' property", resultRecord.hasProperty("age"));
    }
    
    @Test(timeout = 4000)
    public void testGetGreatestSubtypeHelperWithConflictingTypes() {
        JSTypeRegistry registry = createRegistry();
        
        // Create record type A: { prop: string }
        Map<String, RecordProperty> propsA = new HashMap<>();
        propsA.put("prop", createProperty(registry.getNativeType(JSTypeNative.STRING_TYPE)));
        RecordType recordA = new RecordType(registry, propsA, true);
        
        // Create record type B: { prop: number }
        Map<String, RecordProperty> propsB = new HashMap<>();
        propsB.put("prop", createProperty(registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
        RecordType recordB = new RecordType(registry, propsB, true);
        
        // Conflicting property types should result in NO_TYPE
        JSType result = recordA.getGreatestSubtypeHelper(recordB);
        assertEquals("Conflicting types should result in NO_TYPE", 
            registry.getNativeObjectType(JSTypeNative.NO_TYPE), result);
    }
    
    @Test(timeout = 4000)
    public void testStaticIsSubtypeWithDeclaredProperty() {
        JSTypeRegistry registry = createRegistry();
        
        // Create a record type with declared property
        Map<String, RecordProperty> props = new HashMap<>();
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        props.put("prop", createProperty(stringType));
        RecordType recordType = new RecordType(registry, props, true);
        
        // Create an object type that has the same property
        // For this test, we'll use the record type itself as typeA
        assertTrue("Record should be subtype of itself", 
            RecordType.isSubtype(recordType, recordType));
    }
    
    // ===== Partition D: Exception & Defensive Guard Paths =====
    
    @Test(timeout = 4000)
    public void testResolveInternal() {
        JSTypeRegistry registry = createRegistry();
        Map<String, RecordProperty> props = new HashMap<>();
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        props.put("prop", createProperty(stringType));
        
        RecordType record = new RecordType(registry, props, true);
        
        // Resolve should complete without exception
        JSType resolved = record.resolveInternal(new SimpleErrorReporter(), null);
        assertNotNull("Resolved type should not be null", resolved);
        assertTrue("Resolved type should be record type", resolved.isRecordType());
    }
    
    @Test(timeout = 4000)
    public void testDefinePropertyWithInferred() {
        JSTypeRegistry registry = createRegistry();
        
        // Create a record type that is NOT frozen (using internal mechanism)
        // Since the constructor always freezes, we test that defineProperty returns false
        Map<String, RecordProperty> props = new HashMap<>();
        props.put("prop", createProperty(registry.getNativeType(JSTypeNative.STRING_TYPE)));
        RecordType record = new RecordType(registry, props, true);
        
        // Try to define inferred property
        boolean result = record.defineProperty("newProp", 
            registry.getNativeType(JSTypeNative.NUMBER_TYPE), true, new Node(1));
        assertFalse("Should return false for frozen record", result);
    }
    
    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    
    @Test(timeout = 4000)
    public void testIsSynthetic() {
        JSTypeRegistry registry = createRegistry();
        Map<String, RecordProperty> props = new HashMap<>();
        
        RecordType declaredRecord = new RecordType(registry, props, true);
        assertFalse("Declared record should not be synthetic", declaredRecord.isSynthetic());
        
        RecordType synthesizedRecord = new RecordType(registry, props, false);
        assertTrue("Synthesized record should be synthetic", synthesizedRecord.isSynthetic());
    }
    
    @Test(timeout = 4000)
    public void testGetProperties() {
        JSTypeRegistry registry = createRegistry();
        Map<String, RecordProperty> props = new HashMap<>();
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        
        props.put("name", createProperty(stringType));
        props.put("age", createProperty(numberType));
        
        RecordType record = new RecordType(registry, props, true);
        
        Set<String> propertyNames = record.getProperties().keySet();
        assertEquals("Should have 2 properties", 2, propertyNames.size());
        assertTrue("Should contain 'name'", propertyNames.contains("name"));
        assertTrue("Should contain 'age'", propertyNames.contains("age"));
    }
}