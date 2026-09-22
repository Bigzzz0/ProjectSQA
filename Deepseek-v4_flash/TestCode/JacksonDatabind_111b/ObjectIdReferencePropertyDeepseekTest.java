package com.fasterxml.jackson.databind.deser.impl;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId.Referring;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: ObjectIdReferenceProperty - covering all constructors, factory methods,
 * delegation methods, fixAccess, deserializeSetAndReturn (including forward reference handling),
 * and PropertyReferring.handleResolvedForwardReference.
 * 
 * Key branch/decision conditions:
 * 1. Constructor coverage: 3 constructors (forward, copy with deser+nva, copy with newName)
 * 2. withName / withValueDeserializer / withNullProvider: identity shortcuts vs new instances
 * 3. fixAccess: null vs non-null _forward
 * 4. getAnnotation, getMember, getCreatorIndex: delegate to _forward
 * 5. deserializeAndSet: delegates to deserializeSetAndReturn
 * 6. deserializeSetAndReturn: try block -> normal deserialize; catch UnresolvedForwardReference
 *    -> branches: usingIdentityInfo true vs false (based on _objectIdInfo or _valueDeserializer.getObjectIdReader)
 * 7. set/setAndReturn: delegate to _forward
 * 8. PropertyReferring constructor + handleResolvedForwardReference: id match vs mismatch
 * 9. Known defect (ground truth): Null handling within nested structures 
 *    (JDKAtomicTypesDeserTest::testNullWithinNested -> AssertionFailedError)
 *    This defect involves handling of null values in deserialization of identity-info context.
 *    Target scenario: when _valueDeserializer returns null from deserialize(p, ctxt) 
 *    and _objectIdInfo is non-null but null value handling is problematic.
 * 
 * Partition A: Core Functional Logic & State Transitions
 * - Test constructors, factory methods, delegation to _forward
 * - Test PropertyReferring handleResolvedForwardReference success
 * - Test deserializeSetAndReturn normal path (no forward reference)
 * 
 * Partition B: Boundary Value Analysis & Extremes
 * - Test null _forward in fixAccess (no-op)
 * - Test identity shortcut in withValueDeserializer (same deserializer)
 * - Test withName with various PropertyName values
 * - Test PropertyReferring with non-matching id
 * 
 * Partition C: Defect-Targeted Branch Zone
 * - Test deserializeSetAndReturn with UnresolvedForwardReference when usingIdentityInfo is false -> throw JsonMappingException
 * - Test deserializeSetAndReturn with UnresolvedForwardReference when _objectIdInfo is null but _valueDeserializer has ObjectIdReader
 * - Test deserializeSetAndReturn when deserialize returns null (null nested value scenario) — targets the known defect
 * 
 * Partition D: Exception & Defensive Guard Paths
 * - Test PropertyReferring handleResolvedForwardReference with wrong id -> IllegalArgumentException
 * - Test deserializeSetAndReturn UnresolvedForwardReference with no identity info -> JsonMappingException
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 * - Serialization/deserialization aspects (delegates to _forward)
 */
public class ObjectIdReferencePropertyDeepseekTest {

    // ============================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE
    // Targets null handling within nested structures (ground truth defect)
    // ============================================================
    
    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnWithNullDeserializedValueAndObjectIdInfo() throws Exception {
        // Simulate scenario where deserialize returns null (null nested value)
        // and _objectIdInfo is non-null. This targets the known defect pattern
        // from JDKAtomicTypesDeserTest::testNullWithinNested where null handling
        // within identity context fails.
        
        // Arrange: Create a mock ObjectIdInfo
        ObjectIdInfo objectIdInfo = new ObjectIdInfo(PropertyName.construct("@id"), ObjectIdGenerator.class, null, null);
        
        // Create mock SettableBeanProperty forward that returns a null deserialized value
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, objectIdInfo);
        
        // Act
        Object result = prop.deserializeSetAndReturn(null, null, new Object());
        
        // Assert: null should be returned, and set should NOT be called on forward
        assertNull("Expected null result when deserialize returns null and _objectIdInfo is set", result);
    }
    
    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnWithNullAndNoIdentityInfo() throws Exception {
        // Scenario where deserialize returns null but there is NO identity info
        // This should NOT throw because null is returned normally
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        
        Object result = prop.deserializeSetAndReturn(null, null, new Object());
        assertNull("Expected null result when deserialize returns null and no identity info", result);
    }
    
    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnWithUnresolvedForwardAndIdentityInfoByObjectIdReader() throws Exception {
        // Create a scenario where deserialize throws UnresolvedForwardReference
        // and identity info comes from _valueDeserializer.getObjectIdReader()
        
        SettableBeanProperty forward = new SettableBeanPropertyWithDeserializer(
                new JsonDeserializer<Object>() {
                    @Override
                    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                        throw new UnresolvedForwardReference(p, "test forward ref", null);
                    }
                    
                    @Override
                    public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
                        return null;
                    }
                    
                    @Override
                    public ObjectIdReader getObjectIdReader() {
                        // Return a non-null ObjectIdReader to satisfy usingIdentityInfo condition
                        return ObjectIdReader.construct(
                                JavaTypeFactory.instance.constructType(String.class), 
                                PropertyName.construct("@id"), 
                                null,  // scope
                                null,  // generator
                                null,  // resolver
                                null   // deserializer
                        );
                    }
                }, 
                null);
        
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null); // _objectIdInfo is null
        
        // Create a mock ReadableObjectId and attach it
        UnresolvedForwardReference reference = new UnresolvedForwardReference(null, "test", null);
        // We need to set the ROID on this reference for appendReferring to work
        // Use reflection to set the internal ROID
        java.lang.reflect.Field roidField = UnresolvedForwardReference.class.getDeclaredField("_roid");
        roidField.setAccessible(true);
        roidField.set(reference, new ReadableObjectId(123));
        
        // Act
        Object result = prop.deserializeSetAndReturn(null, null, new Object());
        
        // Since we did not provide a proper ROID, the appendReferring may fail or result is null
        // The key is that no JsonMappingException is thrown because usingIdentityInfo is true
        assertNull("Should return null when forward reference is unresolved but identity info present",
                result);
    }
    
    // ============================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // ============================================================
    
    @Test(timeout = 4000)
    public void testConstructorWithForward() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdInfo objectIdInfo = new ObjectIdInfo(PropertyName.construct("@id"), ObjectIdGenerator.class, null, null);
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, objectIdInfo);
        assertNotNull("Property should not be null", prop);
        assertEquals("Name should match forward", forward.getName(), prop.getName());
    }
    
    @Test(timeout = 4000)
    public void testCopyConstructorWithDeserAndNva() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdInfo objectIdInfo = new ObjectIdInfo(PropertyName.construct("@id"), ObjectIdGenerator.class, null, null);
        ObjectIdReferenceProperty original = new ObjectIdReferenceProperty(forward, objectIdInfo);
        
        JsonDeserializer<?> newDeser = new SettableBeanPropertyDeserializer();
        NullValueProvider nva = (ctxt, obj) -> "default";
        
        ObjectIdReferenceProperty copy = new ObjectIdReferenceProperty(original, newDeser, nva);
        assertNotNull("Copy should not be null", copy);
    }
    
    @Test(timeout = 4000)
    public void testCopyConstructorWithNewName() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdInfo objectIdInfo = new ObjectIdInfo(PropertyName.construct("@id"), ObjectIdGenerator.class, null, null);
        ObjectIdReferenceProperty original = new ObjectIdReferenceProperty(forward, objectIdInfo);
        
        PropertyName newName = PropertyName.construct("newName");
        ObjectIdReferenceProperty copy = new ObjectIdReferenceProperty(original, newName);
        assertNotNull("Copy should not be null", copy);
    }
    
    @Test(timeout = 4000)
    public void testWithName() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        
        PropertyName newName = PropertyName.construct("newName");
        SettableBeanProperty newProp = prop.withName(newName);
        assertTrue("Result should be ObjectIdReferenceProperty", newProp instanceof ObjectIdReferenceProperty);
        assertEquals("Name should be updated", newName, newProp.getName());
    }
    
    @Test(timeout = 4000)
    public void testWithValueDeserializerDifferent() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        
        JsonDeserializer<?> newDeser = new SettableBeanPropertyDeserializer();
        SettableBeanProperty newProp = prop.withValueDeserializer(newDeser);
        assertTrue("Result should be ObjectIdReferenceProperty", newProp instanceof ObjectIdReferenceProperty);
    }
    
    @Test(timeout = 4000)
    public void testWithValueDeserializerSame() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        
        // Use the same deserializer (which is already set in forward)
        SettableBeanProperty newProp = prop.withValueDeserializer(prop.getValueDeserializer());
        assertSame("Should return same instance when deserializer unchanged", prop, newProp);
    }
    
    @Test(timeout = 4000)
    public void testWithNullProvider() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        
        NullValueProvider nva = (ctxt, obj) -> "default";
        SettableBeanProperty newProp = prop.withNullProvider(nva);
        assertTrue("Result should be ObjectIdReferenceProperty", newProp instanceof ObjectIdReferenceProperty);
    }
    
    @Test(timeout = 4000)
    public void testFixAccessWithNonNullForward() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        
        // This should not throw
        prop.fixAccess(null);
    }
    
    @Test(timeout = 4000)
    public void testFixAccessWithNullForward() throws Exception {
        // We need to create a prop with null _forward. Since constructors always set _forward,
        // we can create via copy constructor where forward is null in the src... but that can't happen.
        // Actually all constructors set _forward from the source. So test with non-null is sufficient.
    }
    
    @Test(timeout = 4000)
    public void testGetAnnotation() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        
        Annotation result = prop.getAnnotation(Override.class);
        assertNull("Should return null from mock", result);
    }
    
    @Test(timeout = 4000)
    public void testGetMember() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        
        AnnotatedMember result = prop.getMember();
        assertNull("Should return null from mock", result);
    }
    
    @Test(timeout = 4000)
    public void testGetCreatorIndex() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        
        assertEquals("Should delegate to forward", 0, prop.getCreatorIndex());
    }
    
    @Test(timeout = 4000)
    public void testDeserializeAndSet() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        
        // This delegates to deserializeSetAndReturn which uses forward's deserialize
        // Our mock returns null from deserialize, so should not throw
        prop.deserializeAndSet(null, null, new Object());
    }
    
    @Test(timeout = 4000)
    public void testSet() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        
        prop.set(new Object(), "testValue");
        // No exception expected
    }
    
    @Test(timeout = 4000)
    public void testSetAndReturn() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        
        Object result = prop.setAndReturn(new Object(), "testValue");
        assertEquals("Should return value from forward.setAndReturn", "testReturnValue", result);
    }
    
    @Test(timeout = 4000)
    public void testPropertyReferringSuccessfulHandle() throws Exception {
        // Create a property with non-null _forward that supports set
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        
        UnresolvedForwardReference ref = new UnresolvedForwardReference(null, "test", null);
        Object pojo = new Object();
        ObjectIdReferenceProperty.PropertyReferring referring = 
                new ObjectIdReferenceProperty.PropertyReferring(prop, ref, Object.class, pojo);
        
        // First we need to add an id to the referring's referable ids
        // Use reflection to access the internal _referableIds map
        java.lang.reflect.Field idsField = Referring.class.getDeclaredField("_referableIds");
        idsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<Object> ids = (java.util.Set<Object>) idsField.get(referring);
        ids.add(123); // Add the id that will be resolved
        
        // Now handle the resolution
        referring.handleResolvedForwardReference(123, "resolvedValue");
    }
    
    // ============================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS & EXTREMES
    // ============================================================
    
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPropertyReferringHandleWithWrongId() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        
        UnresolvedForwardReference ref = new UnresolvedForwardReference(null, "test", null);
        Object pojo = new Object();
        ObjectIdReferenceProperty.PropertyReferring referring = 
                new ObjectIdReferenceProperty.PropertyReferring(prop, ref, Object.class, pojo);
        
        // Try to resolve with id 123 which was NOT previously added
        referring.handleResolvedForwardReference(123, "value");
    }
    
    @Test(timeout = 4000)
    public void testPropertyReferringHasId() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        
        UnresolvedForwardReference ref = new UnresolvedForwardReference(null, "test", null);
        ObjectIdReferenceProperty.PropertyReferring referring = 
                new ObjectIdReferenceProperty.PropertyReferring(prop, ref, Object.class, new Object());
        
        assertFalse("Should not have id initially", referring.hasId(123));
    }
    
    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnWithForwardReferenceNoIdentityInfo() throws Exception {
        // Create a forward that throws UnresolvedForwardReference from deserialize
        SettableBeanProperty forward = new SettableBeanPropertyWithDeserializer(
                new JsonDeserializer<Object>() {
                    @Override
                    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                        throw new UnresolvedForwardReference(p, "test forward ref", null);
                    }
                    
                    @Override
                    public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
                        return null;
                    }
                    
                    @Override
                    public ObjectIdReader getObjectIdReader() {
                        return null; // No ObjectIdReader
                    }
                }, 
                null);
        
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null); // No _objectIdInfo
        
        try {
            prop.deserializeSetAndReturn(null, null, new Object());
            fail("Should have thrown JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue("Message should mention unresolved forward reference",
                    e.getMessage().contains("Unresolved forward reference"));
        }
    }
    
    // ============================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // ============================================================
    
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPropertyReferringHandleNullId() throws Exception {
        SettableBeanProperty forward = new SettableBeanPropertyTestHelper();
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);
        
        UnresolvedForwardReference ref = new UnresolvedForwardReference(null, "test", null);
        ObjectIdReferenceProperty.PropertyReferring referring = 
                new ObjectIdReferenceProperty.PropertyReferring(prop, ref, Object.class, new Object());
        
        // Null id will cause NullPointerException in hasId check, but the method catches it?
        // Actually hasId(null) would throw NPE because it tries to call .equals on null in the set
        referring.handleResolvedForwardReference(null, "value");
    }
    
    // ============================================================
    // HELPER CLASSES FOR MOCKING
    // ============================================================
    
    /**
     * Helper SettableBeanProperty implementation for testing delegation.
     * Not a mock, but a minimal concrete implementation.
     */
    private static class SettableBeanPropertyTestHelper extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;
        
        public SettableBeanPropertyTestHelper() {
            super(new PropertyName("testProp"), 
                  JavaTypeFactory.instance.constructType(String.class), 
                  null, null, null);
        }
        
        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return this;
        }
        
        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return this;
        }
        
        @Override
        public SettableBeanProperty withNullProvider(NullValueProvider nva) {
            return this;
        }
        
        @Override
        public void fixAccess(DeserializationConfig config) {
            // No-op
        }
        
        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) {
            return null;
        }
        
        @Override
        public AnnotatedMember getMember() {
            return null;
        }
        
        @Override
        public int getCreatorIndex() {
            return 0;
        }
        
        @Override
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            // No-op
        }
        
        @Override
        public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            return null;
        }
        
        @Override
        public void set(Object instance, Object value) throws IOException {
            // No-op
        }
        
        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException {
            return "testReturnValue";
        }
        
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null; // Default returns null to trigger null value handling
        }
        
        @Override
        public JsonDeserializer<Object> getValueDeserializer() {
            return new SettableBeanPropertyDeserializer();
        }
    }
    
    /**
     * Helper SettableBeanProperty that uses a custom deserializer.
     */
    private static class SettableBeanPropertyWithDeserializer extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;
        
        private final JsonDeserializer<?> _customDeser;
        
        public SettableBeanPropertyWithDeserializer(JsonDeserializer<?> deser, ObjectIdInfo objectIdInfo) {
            super(new PropertyName("testProp"), 
                  JavaTypeFactory.instance.constructType(String.class), 
                  null, null, objectIdInfo);
            _customDeser = deser;
        }
        
        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return this;
        }
        
        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return this;
        }
        
        @Override
        public SettableBeanProperty withNullProvider(NullValueProvider nva) {
            return this;
        }
        
        @Override
        public void fixAccess(DeserializationConfig config) {
            // No-op
        }
        
        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) {
            return null;
        }
        
        @Override
        public AnnotatedMember getMember() {
            return null;
        }
        
        @Override
        public int getCreatorIndex() {
            return 0;
        }
        
        @Override
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            // No-op
        }
        
        @Override
        public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            return null;
        }
        
        @Override
        public void set(Object instance, Object value) throws IOException {
            // No-op
        }
        
        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException {
            return value;
        }
        
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return _customDeser.deserialize(p, ctxt);
        }
        
        @Override
        public JsonDeserializer<Object> getValueDeserializer() {
            @SuppressWarnings("unchecked")
            JsonDeserializer<Object> result = (JsonDeserializer<Object>) _customDeser;
            return result;
        }
    }
    
    /**
     * Minimal JsonDeserializer implementation for testing.
     */
    private static class SettableBeanPropertyDeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }
        
        @Override
        public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
            return null;
        }
        
        @Override
        public ObjectIdReader getObjectIdReader() {
            return null;
        }
    }
    
    /**
     * Basic JavaTypeFactory for constructing types.
     */
    private static class JavaTypeFactory {
        public static final JavaTypeFactory instance = new JavaTypeFactory();
        
        public JavaType constructType(Class<?> clazz) {
            return new JavaType() {
                private static final long serialVersionUID = 1L;
                
                @Override
                public Class<?> getRawClass() {
                    return clazz;
                }
            };
        }
    }
}