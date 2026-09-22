package com.fasterxml.jackson.databind.deser.impl;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;

public class ExternalTypeHandlerDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: ExternalTypeHandler (package-private class, tested via public API)
     * Known defect: In `complete(JsonParser, DeserializationContext, Object)` when
     * handling natural types (scalar values) with missing type id, the code path
     * `TypeDeserializer.deserializeIfNatural` may return null for valid scalar
     * values (e.g., String "foo") due to incorrect handling of the buffered token
     * position, causing the property to be set to null instead of the actual value.
     * 
     * Branches targeted:
     * 1. handlePropertyValue: type property name match vs non-match
     * 2. handlePropertyValue: canDeserialize true/false (bean null vs non-null)
     * 3. complete: typeId null vs non-null
     * 4. complete: tokens null vs non-null
     * 5. complete: firstToken scalar vs non-scalar
     * 6. complete: deserializeIfNatural result null vs non-null
     * 7. complete: hasDefaultType true/false
     * 8. _deserialize: VALUE_NULL vs non-null token
     * 9. Builder.addExternal: property name vs type property name
     * 10. ExtTypedProperty methods: hasTypePropertyName, hasDefaultType, getDefaultTypeId
     * 
     * Boundary conditions:
     * - Empty property list
     * - Single property with/without type id
     * - Null bean with buffered tokens
     * - Scalar values (String, int) as natural types
     * - Missing both type and property
     * - Missing type but present property (and vice versa)
     * - Default type id handling
     * - Creator property assignment
     */

    // Test helper to create a minimal ExternalTypeHandler with one property
    private static class TestContext {
        final SettableBeanProperty property;
        final TypeDeserializer typeDeserializer;
        final ExternalTypeHandler.Builder builder;
        final ExternalTypeHandler handler;
        final DeserializationContext ctxt;
        final JsonParser parser;

        TestContext() throws IOException {
            // Create a simple bean property
            property = new SettableBeanProperty("value", 
                com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class),
                null, null) {
                @Override
                public SettableBeanProperty withValueDeserializer(JsonDeserializer<Object> deser) {
                    return this;
                }
                @Override
                public SettableBeanProperty withName(String newName) {
                    return this;
                }
                @Override
                public void set(Object bean, Object value) throws IOException {
                    // Simple setter for test
                    ((TestBean) bean).value = (String) value;
                }
                @Override
                public Object get(Object bean) throws IOException {
                    return ((TestBean) bean).value;
                }
            };

            // Create a TypeDeserializer that handles natural types
            typeDeserializer = new TypeDeserializer() {
                @Override
                public TypeDeserializer forProperty(SettableBeanProperty prop) {
                    return this;
                }
                @Override
                public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) {
                    return null;
                }
                @Override
                public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) {
                    return null;
                }
                @Override
                public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) {
                    return null;
                }
                @Override
                public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) {
                    return null;
                }
                @Override
                public Class<?> getDefaultImpl() {
                    return null;
                }
                @Override
                public String getPropertyName() {
                    return "typeId";
                }
                @Override
                public TypeIdResolver getTypeIdResolver() {
                    return new TypeIdResolver() {
                        @Override
                        public void init(JavaType baseType) {}
                        @Override
                        public String idFromValue(Object value) { return "test"; }
                        @Override
                        public String idFromValueAndType(Object value, Class<?> suggestedType) { return "test"; }
                        @Override
                        public String idFromBaseType() { return "test"; }
                        @Override
                        public JavaType typeFromId(DeserializationContext ctxt, String id) { return null; }
                        @Override
                        public String getMechanism() { return null; }
                    };
                }
            };

            builder = new ExternalTypeHandler.Builder();
            builder.addExternal(property, typeDeserializer);
            handler = builder.build();

            // Create a simple parser for testing
            String json = "{\"typeId\":\"foo\",\"value\":\"bar\"}";
            parser = new com.fasterxml.jackson.core.json.JsonFactory().createParser(json);
            parser.nextToken(); // START_OBJECT
            parser.nextToken(); // FIELD_NAME typeId

            // Create a minimal DeserializationContext (not used in most paths)
            ctxt = null;
        }

        static class TestBean {
            public String value;
        }
    }

    // Test the known defect: natural type handling in complete() with scalar value
    @Test(timeout = 4000)
    public void testCompleteWithNaturalTypeScalarValue() throws Exception {
        TestContext ctx = new TestContext();
        ExternalTypeHandler handler = ctx.handler;
        TestBean bean = new TestBean();

        // Simulate: handle property value for "value" property (buffers token)
        // First, advance parser to the value token
        ctx.parser.nextToken(); // FIELD_NAME "value"
        ctx.parser.nextToken(); // VALUE_STRING "bar"
        
        // Manually set up the handler state to simulate the defect scenario
        // We need to access private fields via reflection or use the handler methods
        // Since we can't easily set private fields, we'll use the public API
        // The defect occurs when complete() is called with a buffered scalar token
        // and no type id, expecting natural type deserialization
        
        // Use reflection to set up the state
        java.lang.reflect.Field tokensField = ExternalTypeHandler.class.getDeclaredField("_tokens");
        tokensField.setAccessible(true);
        TokenBuffer[] tokens = (TokenBuffer[]) tokensField.get(handler);
        
        java.lang.reflect.Field typeIdsField = ExternalTypeHandler.class.getDeclaredField("_typeIds");
        typeIdsField.setAccessible(true);
        String[] typeIds = (String[]) typeIdsField.get(handler);
        
        // Create a TokenBuffer with a scalar value "foo"
        TokenBuffer buffer = new TokenBuffer(ctx.parser, ctx.ctxt);
        buffer.writeString("foo");
        tokens[0] = buffer;
        typeIds[0] = null; // No type id - this triggers natural type handling
        
        // Call complete() - this should set bean.value to "foo" via natural type
        Object result = handler.complete(ctx.parser, ctx.ctxt, bean);
        
        // The defect: expected "foo" but got null
        assertEquals("Natural type should be deserialized", "foo", bean.value);
        assertSame("Should return the same bean", bean, result);
    }

    // Test handlePropertyValue with type property name
    @Test(timeout = 4000)
    public void testHandlePropertyValueWithTypeProperty() throws Exception {
        TestContext ctx = new TestContext();
        ExternalTypeHandler handler = ctx.handler;
        TestBean bean = new TestBean();

        // Simulate JSON: {"typeId":"foo","value":"bar"}
        ctx.parser.nextToken(); // FIELD_NAME "typeId"
        ctx.parser.nextToken(); // VALUE_STRING "foo"
        
        boolean handled = handler.handlePropertyValue(ctx.parser, ctx.ctxt, "typeId", bean);
        assertTrue("Type property should be handled", handled);
        
        // Now handle the value property
        ctx.parser.nextToken(); // FIELD_NAME "value"
        ctx.parser.nextToken(); // VALUE_STRING "bar"
        handled = handler.handlePropertyValue(ctx.parser, ctx.ctxt, "value", bean);
        assertTrue("Value property should be handled", handled);
        
        // Complete should deserialize and set the value
        Object result = handler.complete(ctx.parser, ctx.ctxt, bean);
        assertSame("Should return the same bean", bean, result);
        assertEquals("Value should be set", "bar", bean.value);
    }

    // Test handlePropertyValue with non-type property (buffering)
    @Test(timeout = 4000)
    public void testHandlePropertyValueBuffersValue() throws Exception {
        TestContext ctx = new TestContext();
        ExternalTypeHandler handler = ctx.handler;
        TestBean bean = new TestBean();

        // Simulate: handle value property first (no type id yet)
        ctx.parser.nextToken(); // FIELD_NAME "value"
        ctx.parser.nextToken(); // VALUE_STRING "bar"
        
        boolean handled = handler.handlePropertyValue(ctx.parser, ctx.ctxt, "value", bean);
        assertTrue("Value property should be handled", handled);
        
        // Now handle type property
        ctx.parser.nextToken(); // FIELD_NAME "typeId"
        ctx.parser.nextToken(); // VALUE_STRING "foo"
        handled = handler.handlePropertyValue(ctx.parser, ctx.ctxt, "typeId", bean);
        assertTrue("Type property should be handled", handled);
        
        // Complete should deserialize and set the value
        Object result = handler.complete(ctx.parser, ctx.ctxt, bean);
        assertSame("Should return the same bean", bean, result);
        assertEquals("Value should be set", "bar", bean.value);
    }

    // Test handlePropertyValue with null bean (should buffer but not deserialize)
    @Test(timeout = 4000)
    public void testHandlePropertyValueNullBean() throws Exception {
        TestContext ctx = new TestContext();
        ExternalTypeHandler handler = ctx.handler;

        // Simulate: handle type property with null bean
        ctx.parser.nextToken(); // FIELD_NAME "typeId"
        ctx.parser.nextToken(); // VALUE_STRING "foo"
        
        boolean handled = handler.handlePropertyValue(ctx.parser, ctx.ctxt, "typeId", null);
        assertTrue("Type property should be handled", handled);
        
        // Handle value property
        ctx.parser.nextToken(); // FIELD_NAME "value"
        ctx.parser.nextToken(); // VALUE_STRING "bar"
        handled = handler.handlePropertyValue(ctx.parser, ctx.ctxt, "value", null);
        assertTrue("Value property should be handled", handled);
        
        // Complete with null bean should not throw
        Object result = handler.complete(ctx.parser, ctx.ctxt, null);
        assertNull("Result should be null", result);
    }

    // Test handleTypePropertyValue method
    @Test(timeout = 4000)
    public void testHandleTypePropertyValue() throws Exception {
        TestContext ctx = new TestContext();
        ExternalTypeHandler handler = ctx.handler;
        TestBean bean = new TestBean();

        // Simulate: handle type property value
        ctx.parser.nextToken(); // FIELD_NAME "typeId"
        ctx.parser.nextToken(); // VALUE_STRING "foo"
        
        boolean handled = handler.handleTypePropertyValue(ctx.parser, ctx.ctxt, "typeId", bean);
        assertTrue("Type property should be handled", handled);
        
        // Handle value property
        ctx.parser.nextToken(); // FIELD_NAME "value"
        ctx.parser.nextToken(); // VALUE_STRING "bar"
        handled = handler.handlePropertyValue(ctx.parser, ctx.ctxt, "value", bean);
        assertTrue("Value property should be handled", handled);
        
        // Complete should deserialize
        Object result = handler.complete(ctx.parser, ctx.ctxt, bean);
        assertSame("Should return the same bean", bean, result);
        assertEquals("Value should be set", "bar", bean.value);
    }

    // Test handleTypePropertyValue with unknown property
    @Test(timeout = 4000)
    public void testHandleTypePropertyValueUnknown() throws Exception {
        TestContext ctx = new TestContext();
        ExternalTypeHandler handler = ctx.handler;
        TestBean bean = new TestBean();

        boolean handled = handler.handleTypePropertyValue(ctx.parser, ctx.ctxt, "unknown", bean);
        assertFalse("Unknown property should not be handled", handled);
    }

    // Test complete with missing type id but present property (should report error)
    @Test(timeout = 4000, expected = com.fasterxml.jackson.databind.JsonMappingException.class)
    public void testCompleteMissingTypeId() throws Exception {
        TestContext ctx = new TestContext();
        ExternalTypeHandler handler = ctx.handler;
        TestBean bean = new TestBean();

        // Handle value property only (no type id)
        ctx.parser.nextToken(); // FIELD_NAME "value"
        ctx.parser.nextToken(); // VALUE_STRING "bar"
        handler.handlePropertyValue(ctx.parser, ctx.ctxt, "value", bean);
        
        // Complete should throw because type id is missing
        handler.complete(ctx.parser, ctx.ctxt, bean);
    }

    // Test complete with missing property but present type id (should report error)
    @Test(timeout = 4000, expected = com.fasterxml.jackson.databind.JsonMappingException.class)
    public void testCompleteMissingProperty() throws Exception {
        TestContext ctx = new TestContext();
        ExternalTypeHandler handler = ctx.handler;
        TestBean bean = new TestBean();

        // Handle type property only (no value)
        ctx.parser.nextToken(); // FIELD_NAME "typeId"
        ctx.parser.nextToken(); // VALUE_STRING "foo"
        handler.handlePropertyValue(ctx.parser, ctx.ctxt, "typeId", bean);
        
        // Complete should throw because property is missing
        handler.complete(ctx.parser, ctx.ctxt, bean);
    }

    // Test complete with both missing (should be skipped)
    @Test(timeout = 4000)
    public void testCompleteBothMissing() throws Exception {
        TestContext ctx = new TestContext();
        ExternalTypeHandler handler = ctx.handler;
        TestBean bean = new TestBean();

        // No properties handled, complete should just return bean
        Object result = handler.complete(ctx.parser, ctx.ctxt, bean);
        assertSame("Should return the same bean", bean, result);
    }

    // Test _deserialize with null token (should return null)
    @Test(timeout = 4000)
    public void testDeserializeNullToken() throws Exception {
        TestContext ctx = new TestContext();
        ExternalTypeHandler handler = ctx.handler;
        TestBean bean = new TestBean();

        // Use reflection to set up null token
        java.lang.reflect.Field tokensField = ExternalTypeHandler.class.getDeclaredField("_tokens");
        tokensField.setAccessible(true);
        TokenBuffer[] tokens = (TokenBuffer[]) tokensField.get(handler);
        tokens[0] = null;
        
        // Call _deserialize via complete (should handle null token gracefully)
        // Since token is null, complete should report missing property
        try {
            handler.complete(ctx.parser, ctx.ctxt, bean);
            fail("Should throw JsonMappingException");
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            // Expected
        }
    }

    // Test Builder.addExternal with both property names
    @Test(timeout = 4000)
    public void testBuilderAddExternal() throws Exception {
        TestContext ctx = new TestContext();
        ExternalTypeHandler.Builder builder = ctx.builder;
        
        // The builder should have registered both property name and type property name
        ExternalTypeHandler handler = builder.build();
        
        // Verify the handler works with both names
        TestBean bean = new TestBean();
        
        // Simulate handling type property
        ctx.parser.nextToken(); // FIELD_NAME "typeId"
        ctx.parser.nextToken(); // VALUE_STRING "foo"
        boolean handled = handler.handlePropertyValue(ctx.parser, ctx.ctxt, "typeId", bean);
        assertTrue("Type property should be handled", handled);
        
        // Handle value property
        ctx.parser.nextToken(); // FIELD_NAME "value"
        ctx.parser.nextToken(); // VALUE_STRING "bar"
        handled = handler.handlePropertyValue(ctx.parser, ctx.ctxt, "value", bean);
        assertTrue("Value property should be handled", handled);
        
        Object result = handler.complete(ctx.parser, ctx.ctxt, bean);
        assertSame("Should return the same bean", bean, result);
        assertEquals("Value should be set", "bar", bean.value);
    }

    // Test start() method creates a new instance
    @Test(timeout = 4000)
    public void testStartCreatesNewInstance() throws Exception {
        TestContext ctx = new TestContext();
        ExternalTypeHandler handler = ctx.handler;
        
        ExternalTypeHandler newHandler = handler.start();
        assertNotSame("Should create a new instance", handler, newHandler);
    }

    // Test complete with creator properties (PropertyValueBuffer path)
    @Test(timeout = 4000)
    public void testCompleteWithCreatorProperties() throws Exception {
        TestContext ctx = new TestContext();
        ExternalTypeHandler handler = ctx.handler;
        TestBean bean = new TestBean();

        // Set up buffered tokens
        java.lang.reflect.Field tokensField = ExternalTypeHandler.class.getDeclaredField("_tokens");
        tokensField.setAccessible(true);
        TokenBuffer[] tokens = (TokenBuffer[]) tokensField.get(handler);
        
        java.lang.reflect.Field typeIdsField = ExternalTypeHandler.class.getDeclaredField("_typeIds");
        typeIdsField.setAccessible(true);
        String[] typeIds = (String[]) typeIdsField.get(handler);
        
        // Create a token buffer with a value
        TokenBuffer buffer = new TokenBuffer(ctx.parser, ctx.ctxt);
        buffer.writeString("bar");
        tokens[0] = buffer;
        typeIds[0] = "foo";
        
        // Create a PropertyValueBuffer (simplified)
        PropertyValueBuffer pvb = new PropertyValueBuffer(ctx.parser, ctx.ctxt, 1, null);
        
        // Call complete with creator
        Object result = handler.complete(ctx.parser, ctx.ctxt, pvb, null);
        // Since we don't have a real creator, this may throw, but we just verify no crash
        // The actual behavior depends on the creator implementation
    }

    // Test ExtTypedProperty methods indirectly
    @Test(timeout = 4000)
    public void testExtTypedPropertyMethods() throws Exception {
        TestContext ctx = new TestContext();
        ExternalTypeHandler handler = ctx.handler;
        
        // Access the ExtTypedProperty via reflection
        java.lang.reflect.Field propsField = ExternalTypeHandler.class.getDeclaredField("_properties");
        propsField.setAccessible(true);
        Object[] props = (Object[]) propsField.get(handler);
        
        // Since ExtTypedProperty is private, we can only test through the handler
        assertNotNull("Properties array should not be null", props);
        assertEquals("Should have one property", 1, props.length);
    }

    // Test handlePropertyValue with non-matching property name
    @Test(timeout = 4000)
    public void testHandlePropertyValueNonMatching() throws Exception {
        TestContext ctx = new TestContext();
        ExternalTypeHandler handler = ctx.handler;
        TestBean bean = new TestBean();

        boolean handled = handler.handlePropertyValue(ctx.parser, ctx.ctxt, "unknown", bean);
        assertFalse("Unknown property should not be handled", handled);
    }

    // Test complete with default type (when type id is missing but default exists)
    @Test(timeout = 4000)
    public void testCompleteWithDefaultType() throws Exception {
        // Create a handler with a type deserializer that has a default impl
        TestContext ctx = new TestContext();
        
        // Override the type deserializer to have a default impl
        TypeDeserializer tdWithDefault = new TypeDeserializer() {
            @Override
            public TypeDeserializer forProperty(SettableBeanProperty prop) { return this; }
            @Override
            public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Class<?> getDefaultImpl() { return String.class; }
            @Override
            public String getPropertyName() { return "typeId"; }
            @Override
            public TypeIdResolver getTypeIdResolver() {
                return new TypeIdResolver() {
                    @Override public void init(JavaType baseType) {}
                    @Override public String idFromValue(Object value) { return "test"; }
                    @Override public String idFromValueAndType(Object value, Class<?> suggestedType) { return "test"; }
                    @Override public String idFromBaseType() { return "test"; }
                    @Override public JavaType typeFromId(DeserializationContext ctxt, String id) { return null; }
                    @Override public String getMechanism() { return null; }
                };
            }
        };
        
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(ctx.property, tdWithDefault);
        ExternalTypeHandler handler = builder.build();
        
        TestBean bean = new TestBean();
        
        // Handle value property only (no type id)
        ctx.parser.nextToken(); // FIELD_NAME "value"
        ctx.parser.nextToken(); // VALUE_STRING "bar"
        handler.handlePropertyValue(ctx.parser, ctx.ctxt, "value", bean);
        
        // Complete should use default type
        Object result = handler.complete(ctx.parser, ctx.ctxt, bean);
        assertSame("Should return the same bean", bean, result);
    }
}