package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: AtomicReferenceDeserializer (extends ReferenceTypeDeserializer)
 * 
 * Decision Branches & Conditions:
 * 1. Constructor: Accepts fullType, inst, typeDeser, deser; delegates to super (no validation logic)
 * 2. withResolved(): Returns new AtomicReferenceDeserializer with provided typeDeser, valueDeser
 * 3. getNullValue(): Returns new AtomicReference<Object>() - always empty, no null check
 * 4. getEmptyValue(): Returns new AtomicReference<Object>() - always empty
 * 5. referenceValue(Object): Returns AtomicReference wrapping contents (can be null)
 * 6. getReferenced(AtomicReference): Returns reference.get() - can return null if reference holds null
 * 7. updateReference(AtomicReference, Object): Calls reference.set(contents); returns reference
 * 8. supportsUpdate(): Always returns Boolean.TRUE
 * 
 * Boundary Conditions:
 * - null contents in referenceValue → AtomicReference wrapping null
 * - null reference in getReferenced → NullPointerException (requires non-null reference)
 * - null reference in updateReference → NullPointerException
 * - null contents in updateReference → allowed, sets null inside reference
 * - getNullValue() returns empty AtomicReference (contents = null)
 * - getEmptyValue() returns empty AtomicReference (contents = null)
 * 
 * Known Defect (Defects4J): testNullWithinNested fails
 * The defect involves nested structures where null values within AtomicReference 
 * cause incorrect behavior during deserialization. Specifically, when a null 
 * value is set via referenceValue() or updateReference(), the returned 
 * AtomicReference contains null, but subsequent operations may fail due to 
 * incorrect null handling in the reference type deserializer chain.
 * 
 * To expose the defect, we must test:
 * - AtomicReference containing null values after referenceValue(null)
 * - AtomicReference updated to null via updateReference()
 * - Correct retrieval of null from getReferenced() 
 * - Correct getNullValue() returning reference with null contents
 * - Nested operations with null propagation
 */
public class AtomicReferenceDeserializerDeepseekTest {

    // Create a minimal mock DeserializationContext for testing
    private static class MinimalDeserializationContext extends DeserializationContext {
        public MinimalDeserializationContext() {
            super(new DeserializerFactory() {
                @Override
                public ValueInstantiator findValueInstantiator(DeserializationConfig config, JavaType beanDesc) {
                    return null;
                }
            });
        }
        // Override abstract methods minimally
        @Override
        public Class<?> getActiveView() { return null; }
        @Override
        public Object getAttribute(Object key) { return null; }
        @Override
        public DeserializationContext setAttribute(Object key, Object value) { return this; }
        @Override
        public JsonDeserializer<Object> deserializerInstance(JavaType type, Class<?> deserClass) { return null; }
        @Override
        public JsonDeserializer<Object> keyDeserializerInstance(JavaType type, Class<?> deserClass) { return null; }
        @Override
        public int getDeserializationFeatures() { return 0; }
        @Override
        public boolean hasDeserializationFeatures(int featureMask) { return false; }
        @Override
        public boolean hasSomeOfFeatures(int featureMask) { return false; }
        @Override
        public int getParserCurrentName() { return 0; }
        @Override
        public JavaType getContextualType() { return null; }
        @Override
        public JsonParser getParser() { return null; }
        @Override
        public Object findInjectableValue(Object valueId, Object forProperty) { return null; }
        @Override
        public DeserializationContext with(Object... features) { return this; }
    }

    /*
     * Partition A: Core Functional Logic & State Transitions
     */
    
    @Test(timeout = 4000)
    public void testReferenceValueWithNonNull() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(
                null, null, null, null);
        String content = "testString";
        AtomicReference<Object> ref = deser.referenceValue(content);
        assertNotNull("Reference must not be null", ref);
        assertEquals("Contents must match input", content, ref.get());
    }

    @Test(timeout = 4000)
    public void testGetReferencedWithNonNull() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(
                null, null, null, null);
        String content = "testData";
        AtomicReference<Object> ref = new AtomicReference<Object>(content);
        Object result = deser.getReferenced(ref);
        assertEquals("Must retrieve correct content", content, result);
    }

    @Test(timeout = 4000)
    public void testUpdateReferenceWithNonNull() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(
                null, null, null, null);
        AtomicReference<Object> ref = new AtomicReference<Object>("initial");
        String newContent = "updated";
        deser.updateReference(ref, newContent);
        assertEquals("Reference must be updated", newContent, ref.get());
    }

    @Test(timeout = 4000)
    public void testSupportsUpdate() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(
                null, null, null, null);
        assertTrue("Must support update", deser.supportsUpdate(null));
    }

    /*
     * Partition B: Boundary Value Analysis & Extremes
     */
    
    @Test(timeout = 4000)
    public void testReferenceValueWithNullContent() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(
                null, null, null, null);
        AtomicReference<Object> ref = deser.referenceValue(null);
        assertNotNull("Reference must not be null even with null content", ref);
        assertNull("Contents must be null", ref.get());
    }

    @Test(timeout = 4000)
    public void testGetReferencedWithNullContained() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(
                null, null, null, null);
        AtomicReference<Object> ref = new AtomicReference<Object>(null);
        Object result = deser.getReferenced(ref);
        assertNull("Must retrieve null content", result);
    }

    @Test(timeout = 4000)
    public void testUpdateReferenceWithNullContent() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(
                null, null, null, null);
        AtomicReference<Object> ref = new AtomicReference<Object>("initial");
        deser.updateReference(ref, null);
        assertNull("Reference must contain null after update", ref.get());
    }

    @Test(timeout = 4000)
    public void testGetNullValue() throws Exception {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(
                null, null, null, null);
        DeserializationContext ctxt = new MinimalDeserializationContext();
        AtomicReference<Object> ref = deser.getNullValue(ctxt);
        assertNotNull("Null value must return non-null reference", ref);
        assertNull("Null value reference must contain null", ref.get());
    }

    @Test(timeout = 4000)
    public void testGetEmptyValue() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(
                null, null, null, null);
        DeserializationContext ctxt = new MinimalDeserializationContext();
        AtomicReference<Object> ref = (AtomicReference<Object>) deser.getEmptyValue(ctxt);
        assertNotNull("Empty value must return non-null reference", ref);
        assertNull("Empty value reference must contain null", ref.get());
    }

    /*
     * Partition C: Defect-Targeted Branch Zone - Targets null within nested AtomicReference
     * This specifically targets the known defect where null values within nested structures fail.
     */
    
    @Test(timeout = 4000)
    public void testNullWithinNestedAtomicReference() {
        // Simulates nested null scenario: create reference via referenceValue(null) 
        // then update it to another value, then getReferenced to verify null handling
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(
                null, null, null, null);
        
        // Step 1: Create reference with null content (simulates nested null)
        AtomicReference<Object> outerRef = deser.referenceValue(null);
        assertNotNull("Outer reference must not be null", outerRef);
        assertNull("Initially must contain null", outerRef.get());
        
        // Step 2: Update reference with a non-null value (simulates transition)
        String nestedContent = "nestedValue";
        deser.updateReference(outerRef, nestedContent);
        assertEquals("After update must contain new value", nestedContent, outerRef.get());
        
        // Step 3: Retrieve referenced value
        Object retrieved = deser.getReferenced(outerRef);
        assertEquals("Retrieved value must match", nestedContent, retrieved);
        
        // Step 4: Update back to null (this is where the bug may manifest)
        deser.updateReference(outerRef, null);
        assertNull("After null update must contain null", outerRef.get());
        
        // Step 5: Retrieve null value - this is the critical operation
        Object nullRetrieved = deser.getReferenced(outerRef);
        assertNull("Must retrieve null after null update", nullRetrieved);
        
        // Step 6: Chain with getNullValue to test full null cycle
        AtomicReference<Object> fromNull = deser.referenceValue(deser.getReferenced(outerRef));
        assertNotNull("Reference from null must not be null", fromNull);
        assertNull("Must wrap null value correctly", fromNull.get());
    }

    @Test(timeout = 4000)
    public void testDefectRevealingNestedNullCycle() throws Exception {
        // This test directly targets the defect described in testNullWithinNested
        // It exercises the full lifecycle with null propagation across multiple operations
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(
                null, null, null, null);
        DeserializationContext ctxt = new MinimalDeserializationContext();
        
        // Start with empty reference (getNullValue)
        AtomicReference<Object> ref1 = deser.getNullValue(ctxt);
        assertNotNull("Null value reference", ref1);
        assertNull("Empty reference content", ref1.get());
        
        // Create a new reference wrapping null explicitly
        AtomicReference<Object> ref2 = deser.referenceValue(null);
        assertNotNull("Reference from null", ref2);
        assertNull("Wrapped null content", ref2.get());
        
        // Update reference with null again (no-op but exercises branch)
        deser.updateReference(ref2, null);
        assertNull("After double null update", ref2.get());
        
        // Retrieve null from reference - this is the critical path
        Object retrievedNull = deser.getReferenced(ref2);
        assertNull("Must retrieve null", retrievedNull);
        
        // Create another reference using the retrieved null value
        AtomicReference<Object> ref3 = deser.referenceValue(retrievedNull);
        assertNotNull("Reference from retrieved null", ref3);
        assertNull("Nested null preservation", ref3.get());
        
        // Verify getEmptyValue also produces null-containing reference
        AtomicReference<Object> emptyRef = (AtomicReference<Object>) deser.getEmptyValue(ctxt);
        assertNotNull("Empty value reference", emptyRef);
        assertNull("Empty reference content", emptyRef.get());
    }

    /*
     * Partition D: Exception & Defensive Guard Paths
     */
    
    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testGetReferencedWithNullReference() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(
                null, null, null, null);
        // Passing null reference should throw NullPointerException
        deser.getReferenced(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testUpdateReferenceWithNullReference() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(
                null, null, null, null);
        // Passing null reference should throw NullPointerException
        deser.updateReference(null, "test");
    }

    /*
     * Partition E: Object Lifecycle & Contract Integrity
     */
    
    @Test(timeout = 4000)
    public void testWithResolvedNonNull() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(
                null, null, null, null);
        TypeDeserializer typeDeser = null; // Cannot mock, but test construction
        JsonDeserializer<?> valueDeser = null;
        AtomicReferenceDeserializer resolved = deser.withResolved(typeDeser, valueDeser);
        assertNotNull("Resolved deserializer must not be null", resolved);
        assertTrue("Resolved must be correct type", resolved instanceof AtomicReferenceDeserializer);
    }

    @Test(timeout = 4000)
    public void testWithResolvedPreservesType() {
        // Test that withResolved returns correct subtype
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(
                null, null, null, null);
        AtomicReferenceDeserializer resolved = deser.withResolved(null, null);
        assertNotNull(resolved);
        // Ensure it's not the same instance (should be a new instance)
        assertNotSame("Should create new instance", deser, resolved);
    }
}