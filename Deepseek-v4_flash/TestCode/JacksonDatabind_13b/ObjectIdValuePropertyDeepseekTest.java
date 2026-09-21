package com.fasterxml.jackson.databind.deser.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.annotation.Annotation;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.ObjectIdReader;

public class ObjectIdValuePropertyDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: ObjectIdValueProperty
     * 
     * Constructors:
     *  - public ObjectIdValueProperty(ObjectIdReader, PropertyMetadata) -> delegates to super, sets _objectIdReader
     *  - protected ObjectIdValueProperty(ObjectIdValueProperty, JsonDeserializer) -> copy constructor with deser
     *  - protected ObjectIdValueProperty(ObjectIdValueProperty, PropertyName) -> copy constructor with name
     *  - protected ObjectIdValueProperty(ObjectIdValueProperty, String) -> delegates to PropertyName constructor
     * 
     * Methods:
     *  - withName(PropertyName) -> returns new instance with new name
     *  - withValueDeserializer(JsonDeserializer) -> returns new instance with new deser
     *  - getAnnotation(Class) -> always returns null
     *  - getMember() -> always returns null
     *  - deserializeAndSet(JsonParser, DeserializationContext, Object) -> delegates to deserializeSetAndReturn
     *  - deserializeSetAndReturn(JsonParser, DeserializationContext, Object) -> 
     *      * calls _valueDeserializer.deserialize(jp, ctxt)
     *      * calls ctxt.findObjectId(id, generator, resolver)
     *      * calls roid.bindItem(instance)
     *      * if idProperty != null -> returns idProp.setAndReturn(instance, id)
     *      * else returns instance
     *  - set(Object, Object) -> delegates to setAndReturn
     *  - setAndReturn(Object, Object) ->
     *      * if idProperty == null -> throws UnsupportedOperationException
     *      * else returns idProp.setAndReturn(instance, value)
     * 
     * Known Defect: testNullObjectId - NPE when deserializing null id
     *   - When _valueDeserializer.deserialize() returns null, ctxt.findObjectId(null, ...) 
     *     may throw NPE in some implementations.
     *   - The fix should handle null id gracefully (e.g., return instance without binding).
     * 
     * Branch Coverage Targets:
     *  - deserializeSetAndReturn: idProp == null vs != null
     *  - setAndReturn: idProp == null vs != null
     *  - withName/withValueDeserializer: return new instance
     *  - getAnnotation/getMember: always null
     * 
     * Boundary Conditions:
     *  - null deserializer, null metadata, null objectIdReader
     *  - null instance, null value in set methods
     *  - null id returned from deserializer
     *  - idProperty present vs absent
     */
    
    // Test helper to create a minimal ObjectIdReader
    private ObjectIdReader createObjectIdReader(JsonDeserializer<?> deser, SettableBeanProperty idProp) {
        // Use reflection or mock-like approach - but since we can't mock, we need real objects
        // For simplicity, we'll use a custom ObjectIdReader subclass or use the builder
        // Since ObjectIdReader has protected constructors, we'll use the builder pattern
        // Actually, ObjectIdReader has a static factory method construct()
        // Let's use that if available, otherwise we'll create a minimal subclass
        try {
            // Use reflection to create ObjectIdReader with minimal setup
            java.lang.reflect.Constructor<ObjectIdReader> ctor = 
                ObjectIdReader.class.getDeclaredConstructor(PropertyName.class, 
                    com.fasterxml.jackson.databind.JavaType.class, 
                    JsonDeserializer.class, 
                    com.fasterxml.jackson.databind.util.Resolver.class,
                    SettableBeanProperty.class,
                    com.fasterxml.jackson.databind.introspect.AnnotatedMember.class);
            ctor.setAccessible(true);
            return ctor.newInstance(
                new PropertyName("id"),
                com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(Object.class),
                deser,
                null,
                idProp,
                null
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    // Minimal JsonDeserializer for testing
    private static class TestDeserializer extends JsonDeserializer<Object> {
        private final Object value;
        private final boolean throwException;
        
        TestDeserializer(Object value) { this(value, false); }
        TestDeserializer(Object value, boolean throwException) { 
            this.value = value; 
            this.throwException = throwException;
        }
        
        @Override
        public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            if (throwException) {
                throw new IOException("test exception");
            }
            return value;
        }
    }
    
    // Minimal SettableBeanProperty for testing
    private static class TestSettableBeanProperty extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;
        private Object lastSetValue;
        private Object lastSetAndReturnValue;
        
        TestSettableBeanProperty(PropertyName name) {
            super(name, null, null, null);
        }
        
        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return this;
        }
        
        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return this;
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
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            // not used
        }
        
        @Override
        public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            return null;
        }
        
        @Override
        public void set(Object instance, Object value) throws IOException {
            lastSetValue = value;
        }
        
        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException {
            lastSetAndReturnValue = value;
            return value;
        }
    }
    
    // Minimal DeserializationContext for testing
    private static class TestDeserializationContext extends DeserializationContext {
        private final Object idResult;
        private final boolean throwOnFindObjectId;
        
        TestDeserializationContext(Object idResult, boolean throwOnFindObjectId) {
            super(null, null, null, null);
            this.idResult = idResult;
            this.throwOnFindObjectId = throwOnFindObjectId;
        }
        
        @Override
        public ReadableObjectId findObjectId(Object id, com.fasterxml.jackson.annotation.ObjectIdGenerator<?> gen, 
                com.fasterxml.jackson.annotation.ObjectIdResolver resolver) {
            if (throwOnFindObjectId) {
                throw new NullPointerException("NPE from findObjectId");
            }
            return new ReadableObjectId(id);
        }
    }
    
    // ========== Partition A: Core Functional Logic & State Transitions ==========
    
    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnWithIdProperty() throws Exception {
        TestSettableBeanProperty idProp = new TestSettableBeanProperty(new PropertyName("id"));
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), idProp);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        Object instance = new Object();
        TestDeserializationContext ctxt = new TestDeserializationContext("testId", false);
        
        Object result = prop.deserializeSetAndReturn(null, ctxt, instance);
        
        assertEquals("testId", idProp.lastSetAndReturnValue);
        assertEquals(instance, result);
    }
    
    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnWithoutIdProperty() throws Exception {
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        Object instance = new Object();
        TestDeserializationContext ctxt = new TestDeserializationContext("testId", false);
        
        Object result = prop.deserializeSetAndReturn(null, ctxt, instance);
        
        assertEquals(instance, result);
    }
    
    @Test(timeout = 4000)
    public void testDeserializeAndSetDelegates() throws Exception {
        TestSettableBeanProperty idProp = new TestSettableBeanProperty(new PropertyName("id"));
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), idProp);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        Object instance = new Object();
        TestDeserializationContext ctxt = new TestDeserializationContext("testId", false);
        
        prop.deserializeAndSet(null, ctxt, instance);
        
        assertEquals("testId", idProp.lastSetAndReturnValue);
    }
    
    @Test(timeout = 4000)
    public void testSetAndReturnWithIdProperty() throws Exception {
        TestSettableBeanProperty idProp = new TestSettableBeanProperty(new PropertyName("id"));
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), idProp);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        Object instance = new Object();
        Object result = prop.setAndReturn(instance, "value");
        
        assertEquals("value", idProp.lastSetAndReturnValue);
        assertEquals("value", result);
    }
    
    @Test(timeout = 4000)
    public void testSetDelegatesToSetAndReturn() throws Exception {
        TestSettableBeanProperty idProp = new TestSettableBeanProperty(new PropertyName("id"));
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), idProp);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        Object instance = new Object();
        prop.set(instance, "value");
        
        assertEquals("value", idProp.lastSetAndReturnValue);
    }
    
    // ========== Partition B: Boundary Value Analysis (BVA) & Extremes ==========
    
    @Test(timeout = 4000)
    public void testWithNameReturnsNewInstance() {
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        ObjectIdValueProperty renamed = prop.withName(new PropertyName("newName"));
        
        assertNotSame(prop, renamed);
        assertNotNull(renamed);
    }
    
    @Test(timeout = 4000)
    public void testWithValueDeserializerReturnsNewInstance() {
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        ObjectIdValueProperty newProp = prop.withValueDeserializer(new TestDeserializer("newId"));
        
        assertNotSame(prop, newProp);
        assertNotNull(newProp);
    }
    
    @Test(timeout = 4000)
    public void testGetAnnotationReturnsNull() {
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        assertNull(prop.getAnnotation(Deprecated.class));
    }
    
    @Test(timeout = 4000)
    public void testGetMemberReturnsNull() {
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        assertNull(prop.getMember());
    }
    
    @Test(timeout = 4000)
    public void testNullDeserializerValue() throws Exception {
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer(null), null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        Object instance = new Object();
        TestDeserializationContext ctxt = new TestDeserializationContext(null, false);
        
        Object result = prop.deserializeSetAndReturn(null, ctxt, instance);
        
        assertEquals(instance, result);
    }
    
    // ========== Partition C: Defect-Targeted Branch Zone ==========
    
    @Test(timeout = 4000)
    public void testNullObjectIdDoesNotThrowNPE() throws Exception {
        // This test targets the known defect: NPE when deserializing null id
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer(null), null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        Object instance = new Object();
        TestDeserializationContext ctxt = new TestDeserializationContext(null, false);
        
        // Should not throw NPE - the defect is that findObjectId(null, ...) throws NPE
        try {
            Object result = prop.deserializeSetAndReturn(null, ctxt, instance);
            // If we get here, no exception was thrown - but the defect would cause NPE
            // The correct behavior is to handle null gracefully
            assertEquals(instance, result);
        } catch (NullPointerException e) {
            fail("NPE should not be thrown for null object id: " + e.getMessage());
        }
    }
    
    @Test(timeout = 4000)
    public void testSetAndReturnWithoutIdPropertyThrows() {
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        try {
            prop.setAndReturn(new Object(), "value");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }
    
    // ========== Partition D: Exception & Defensive Guard Paths ==========
    
    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnWithDeserializerException() throws Exception {
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer(null, true), null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        Object instance = new Object();
        TestDeserializationContext ctxt = new TestDeserializationContext(null, false);
        
        try {
            prop.deserializeSetAndReturn(null, ctxt, instance);
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals("test exception", e.getMessage());
        }
    }
    
    @Test(timeout = 4000)
    public void testSetWithoutIdPropertyThrows() {
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        try {
            prop.set(new Object(), "value");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }
    
    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    
    @Test(timeout = 4000)
    public void testCopyConstructorWithDeserializer() throws Exception {
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        JsonDeserializer<?> newDeser = new TestDeserializer("newId");
        ObjectIdValueProperty copy = new ObjectIdValueProperty(prop, newDeser);
        
        assertNotNull(copy);
        assertNotSame(prop, copy);
    }
    
    @Test(timeout = 4000)
    public void testCopyConstructorWithPropertyName() throws Exception {
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        ObjectIdValueProperty copy = new ObjectIdValueProperty(prop, new PropertyName("newName"));
        
        assertNotNull(copy);
        assertNotSame(prop, copy);
    }
    
    @Test(timeout = 4000)
    public void testCopyConstructorWithStringName() throws Exception {
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        ObjectIdValueProperty copy = new ObjectIdValueProperty(prop, "newName");
        
        assertNotNull(copy);
        assertNotSame(prop, copy);
    }
    
    @Test(timeout = 4000)
    public void testWithNamePreservesDeserializer() throws Exception {
        TestDeserializer deser = new TestDeserializer("testId");
        ObjectIdReader reader = createObjectIdReader(deser, null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        ObjectIdValueProperty renamed = prop.withName(new PropertyName("newName"));
        
        assertNotNull(renamed);
        // Verify the deserializer is preserved by checking behavior
        try {
            renamed.deserializeSetAndReturn(null, new TestDeserializationContext("testId", false), new Object());
        } catch (Exception e) {
            fail("Should not throw: " + e.getMessage());
        }
    }
    
    @Test(timeout = 4000)
    public void testWithValueDeserializerPreservesName() throws Exception {
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), null);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        ObjectIdValueProperty newProp = prop.withValueDeserializer(new TestDeserializer("newId"));
        
        assertNotNull(newProp);
        // Verify the name is preserved by checking the property name
        assertEquals(new PropertyName("id"), newProp.getFullName());
    }
    
    @Test(timeout = 4000)
    public void testMultipleDeserializeCalls() throws Exception {
        TestSettableBeanProperty idProp = new TestSettableBeanProperty(new PropertyName("id"));
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), idProp);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        Object instance = new Object();
        TestDeserializationContext ctxt = new TestDeserializationContext("testId", false);
        
        prop.deserializeSetAndReturn(null, ctxt, instance);
        prop.deserializeSetAndReturn(null, ctxt, instance);
        
        assertEquals("testId", idProp.lastSetAndReturnValue);
    }
    
    @Test(timeout = 4000)
    public void testSetAndReturnMultipleTimes() throws Exception {
        TestSettableBeanProperty idProp = new TestSettableBeanProperty(new PropertyName("id"));
        ObjectIdReader reader = createObjectIdReader(new TestDeserializer("testId"), idProp);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        
        Object instance = new Object();
        prop.setAndReturn(instance, "value1");
        prop.setAndReturn(instance, "value2");
        
        assertEquals("value2", idProp.lastSetAndReturnValue);
    }
}