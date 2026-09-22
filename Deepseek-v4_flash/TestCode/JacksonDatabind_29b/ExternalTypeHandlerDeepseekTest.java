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

/**
 * White-box test suite for ExternalTypeHandler.
 * Targets all major branches, boundary conditions, and the known defect
 * related to null value handling (databind#942).
 *
 * [Branch & Defect Analysis Matrix]
 * - handleTypePropertyValue: null/not-null bean, null/not-null tokens[index]
 * - handlePropertyValue: type property vs value property, null/not-null bean, null/not-null typeId/tokens
 * - complete (bean): both present, missing typeId (scalar/non-scalar), missing tokens, both missing
 * - complete (creator): similar with creator property assignment
 * - _deserialize / _deserializeAndSet: null token handling, merged array construction
 * - Builder: addExternal, build
 * - ExtTypedProperty: hasTypePropertyName, hasDefaultType, getDefaultTypeId
 */
public class ExternalTypeHandlerDeepseekTest {

    // ----------------------------------------------------------
    // Helper stubs
    // ----------------------------------------------------------

    private static class SimpleBean {
        public Object value;
    }

    private static class SimpleSettableBeanProperty extends SettableBeanProperty {
        private final String name;
        private final JavaType type;
        public Object storedValue;

        public SimpleSettableBeanProperty(String name, JavaType type) {
            super(PropertyName.construct(name), type, null, null, null, false, null);
            this.name = name;
            this.type = type;
        }

        @Override
        public String getName() { return name; }

        @Override
        public JavaType getType() { return type; }

        @Override
        public <A> A getAnnotation(Class<A> acls) { return null; }

        @Override
        public AnnotatedMember getMember() { return null; }

        @Override
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            storedValue = deserialize(p, ctxt);
            ((SimpleBean) instance).value = storedValue;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            // For simplicity, return the token as a string or null
            if (p.currentToken() == JsonToken.VALUE_NULL) {
                return null;
            }
            return p.getText();
        }

        @Override
        public void set(Object instance, Object value) throws IOException {
            ((SimpleBean) instance).value = value;
        }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) { return this; }

        @Override
        public SettableBeanProperty withName(String newName) { return this; }

        @Override
        public SettableBeanProperty withName(PropertyName newName) { return this; }

        @Override
        public SettableBeanProperty withNullProvider(NullValueProvider nva) { return this; }
    }

    private static class SimpleTypeDeserializer extends TypeDeserializer {
        private final String typePropertyName;
        private final Class<?> defaultImpl;

        public SimpleTypeDeserializer(String typePropertyName, Class<?> defaultImpl) {
            this.typePropertyName = typePropertyName;
            this.defaultImpl = defaultImpl;
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) { return this; }

        @Override
        public String getPropertyName() { return typePropertyName; }

        @Override
        public TypeIdResolver getTypeIdResolver() {
            return new TypeIdResolver() {
                @Override
                public String idFromValue(Object value) { return "testType"; }
                @Override
                public String idFromValueAndType(Object value, Class<?> type) { return "testType"; }
                @Override
                public String idFromBaseType() { return "testType"; }
                @Override
                public JavaType typeFromId(DeserializationContext ctxt, String id) { return null; }
                @Override
                public String idFromValueAndType(Object value, Class<?> type, DeserializationConfig config) { return "testType"; }
                @Override
                public String idFromBaseType(DeserializationConfig config) { return "testType"; }
                @Override
                public JavaType typeFromId(DeserializationConfig config, String id) { return null; }
                @Override
                public String getDescForKnownTypeIds() { return ""; }
                @Override
                public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
            };
        }

        @Override
        public Class<?> getDefaultImpl() { return defaultImpl; }

        @Override
        public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }
    }

    // ----------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ----------------------------------------------------------

    @Test(timeout = 4000)
    public void testStartReturnsNewInstance() {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler original = builder.build();
        ExternalTypeHandler copy = original.start();
        assertNotNull(copy);
        assertNotSame(original, copy);
    }

    @Test(timeout = 4000)
    public void testHandleTypePropertyValueWithBeanAndTokens() throws IOException {
        // Setup: one property, type property name "type"
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", null);
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build();

        // Simulate: first handle type property (sets typeId), then value property (buffers tokens)
        // We'll manually set tokens via handlePropertyValue for value property
        TokenBuffer valueBuf = new TokenBuffer(null);
        valueBuf.writeNull();
        JsonParser valueParser = valueBuf.asParser();
        valueParser.nextToken(); // VALUE_NULL

        // Handle value property first (buffers token)
        assertTrue(handler.handlePropertyValue(valueParser, null, "value", null));

        // Now handle type property
        TokenBuffer typeBuf = new TokenBuffer(null);
        typeBuf.writeString("myType");
        JsonParser typeParser = typeBuf.asParser();
        typeParser.nextToken(); // VALUE_STRING

        SimpleBean bean = new SimpleBean();
        assertTrue(handler.handleTypePropertyValue(typeParser, null, "@type", bean));

        // After handling type, it should have deserialized and set the value (null)
        assertNull(bean.value);
    }

    @Test(timeout = 4000)
    public void testHandleTypePropertyValueWithoutBean() throws IOException {
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", null);
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build();

        // Buffer value first
        TokenBuffer valueBuf = new TokenBuffer(null);
        valueBuf.writeString("someValue");
        JsonParser valueParser = valueBuf.asParser();
        valueParser.nextToken();
        handler.handlePropertyValue(valueParser, null, "value", null);

        // Now handle type property with bean = null
        TokenBuffer typeBuf = new TokenBuffer(null);
        typeBuf.writeString("myType");
        JsonParser typeParser = typeBuf.asParser();
        typeParser.nextToken();
        assertTrue(handler.handleTypePropertyValue(typeParser, null, "@type", null));
        // No exception expected; typeId stored internally
    }

    @Test(timeout = 4000)
    public void testHandlePropertyValueTypeProperty() throws IOException {
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", null);
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build();

        TokenBuffer typeBuf = new TokenBuffer(null);
        typeBuf.writeString("myType");
        JsonParser typeParser = typeBuf.asParser();
        typeParser.nextToken();

        // handlePropertyValue with type property name
        assertTrue(handler.handlePropertyValue(typeParser, null, "@type", null));
    }

    @Test(timeout = 4000)
    public void testHandlePropertyValueValueProperty() throws IOException {
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", null);
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build();

        TokenBuffer valueBuf = new TokenBuffer(null);
        valueBuf.writeString("someValue");
        JsonParser valueParser = valueBuf.asParser();
        valueParser.nextToken();

        assertTrue(handler.handlePropertyValue(valueParser, null, "value", null));
    }

    @Test(timeout = 4000)
    public void testHandlePropertyValueUnknownProperty() throws IOException {
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", null);
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build();

        TokenBuffer buf = new TokenBuffer(null);
        buf.writeString("irrelevant");
        JsonParser parser = buf.asParser();
        parser.nextToken();

        assertFalse(handler.handlePropertyValue(parser, null, "unknown", null));
    }

    // ----------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // ----------------------------------------------------------

    @Test(timeout = 4000)
    public void testCompleteWithMissingBoth() throws IOException {
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", null);
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build();

        SimpleBean bean = new SimpleBean();
        // No typeId, no tokens -> should skip and return bean unchanged
        Object result = handler.complete(null, null, bean);
        assertSame(bean, result);
        assertNull(bean.value);
    }

    @Test(timeout = 4000)
    public void testCompleteWithMissingTokens() throws IOException {
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", null);
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build();

        // Set typeId via handlePropertyValue (type property)
        TokenBuffer typeBuf = new TokenBuffer(null);
        typeBuf.writeString("myType");
        JsonParser typeParser = typeBuf.asParser();
        typeParser.nextToken();
        handler.handlePropertyValue(typeParser, null, "@type", null);

        SimpleBean bean = new SimpleBean();
        try {
            handler.complete(null, null, bean);
            fail("Expected JsonMappingException for missing tokens");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Missing property"));
        }
    }

    @Test(timeout = 4000)
    public void testCompleteWithMissingTypeIdAndScalarValue() throws IOException {
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", String.class); // default impl
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build();

        // Buffer a scalar value (string)
        TokenBuffer valueBuf = new TokenBuffer(null);
        valueBuf.writeString("someString");
        JsonParser valueParser = valueBuf.asParser();
        valueParser.nextToken();
        handler.handlePropertyValue(valueParser, null, "value", null);

        SimpleBean bean = new SimpleBean();
        Object result = handler.complete(null, null, bean);
        assertSame(bean, result);
        // Since default type is String, it should have deserialized the string as natural type?
        // Our simple deserializer returns the text, so bean.value should be "someString"
        assertEquals("someString", bean.value);
    }

    @Test(timeout = 4000)
    public void testCompleteWithMissingTypeIdAndNonScalarValue() throws IOException {
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", null); // no default
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build();

        // Buffer a non-scalar value (object)
        TokenBuffer valueBuf = new TokenBuffer(null);
        valueBuf.writeStartObject();
        valueBuf.writeStringField("a", "b");
        valueBuf.writeEndObject();
        JsonParser valueParser = valueBuf.asParser();
        valueParser.nextToken(); // START_OBJECT
        handler.handlePropertyValue(valueParser, null, "value", null);

        SimpleBean bean = new SimpleBean();
        try {
            handler.complete(null, null, bean);
            fail("Expected JsonMappingException for missing type id");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Missing external type id property"));
        }
    }

    // ----------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (null handling)
    // ----------------------------------------------------------

    @Test(timeout = 4000)
    public void testNullValueWithTypeId() throws IOException {
        // This test targets the known defect: null value with external type id
        // Buggy version throws "Can not deserialize instance of ... out of VALUE_STRING token"
        // Correct behavior: should set property to null

        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", null);
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build();

        // Step 1: buffer null value
        TokenBuffer valueBuf = new TokenBuffer(null);
        valueBuf.writeNull();
        JsonParser valueParser = valueBuf.asParser();
        valueParser.nextToken(); // VALUE_NULL
        handler.handlePropertyValue(valueParser, null, "value", null);

        // Step 2: provide type id
        TokenBuffer typeBuf = new TokenBuffer(null);
        typeBuf.writeString("myType");
        JsonParser typeParser = typeBuf.asParser();
        typeParser.nextToken(); // VALUE_STRING

        SimpleBean bean = new SimpleBean();
        // This call should trigger _deserializeAndSet with null token
        handler.handleTypePropertyValue(typeParser, null, "@type", bean);

        // Expect no exception and property set to null
        assertNull("Property should be null", bean.value);
    }

    @Test(timeout = 4000)
    public void testNullValueWithoutTypeIdWithDefault() throws IOException {
        // Null value, missing type id, but default type exists
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", String.class);
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build();

        TokenBuffer valueBuf = new TokenBuffer(null);
        valueBuf.writeNull();
        JsonParser valueParser = valueBuf.asParser();
        valueParser.nextToken();
        handler.handlePropertyValue(valueParser, null, "value", null);

        SimpleBean bean = new SimpleBean();
        Object result = handler.complete(null, null, bean);
        assertSame(bean, result);
        // With default type String, null should be set as null (our deserializer returns null for null token)
        assertNull(bean.value);
    }

    // ----------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // ----------------------------------------------------------

    @Test(timeout = 4000)
    public void testCompleteWithMissingTypeIdAndNoDefault() throws IOException {
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", null);
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build();

        TokenBuffer valueBuf = new TokenBuffer(null);
        valueBuf.writeString("someValue");
        JsonParser valueParser = valueBuf.asParser();
        valueParser.nextToken();
        handler.handlePropertyValue(valueParser, null, "value", null);

        SimpleBean bean = new SimpleBean();
        try {
            handler.complete(null, null, bean);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Missing external type id property"));
        }
    }

    @Test(timeout = 4000)
    public void testCompleteWithMissingTokensAndTypeId() throws IOException {
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", null);
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build();

        // Set typeId only
        TokenBuffer typeBuf = new TokenBuffer(null);
        typeBuf.writeString("myType");
        JsonParser typeParser = typeBuf.asParser();
        typeParser.nextToken();
        handler.handlePropertyValue(typeParser, null, "@type", null);

        SimpleBean bean = new SimpleBean();
        try {
            handler.complete(null, null, bean);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Missing property"));
        }
    }

    // ----------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // ----------------------------------------------------------

    @Test(timeout = 4000)
    public void testBuilderAddExternalAndBuild() {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("x", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", null);
        builder.addExternal(prop, typeDeser);
        ExternalTypeHandler handler = builder.build();
        assertNotNull(handler);
    }

    @Test(timeout = 4000)
    public void testExtTypedPropertyHasTypePropertyName() {
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", null);
        ExternalTypeHandler.ExtTypedProperty extProp = new ExternalTypeHandler.ExtTypedProperty(prop, typeDeser);
        assertTrue(extProp.hasTypePropertyName("@type"));
        assertFalse(extProp.hasTypePropertyName("value"));
    }

    @Test(timeout = 4000)
    public void testExtTypedPropertyHasDefaultType() {
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeserWithDefault = new SimpleTypeDeserializer("@type", String.class);
        ExternalTypeHandler.ExtTypedProperty extProp = new ExternalTypeHandler.ExtTypedProperty(prop, typeDeserWithDefault);
        assertTrue(extProp.hasDefaultType());

        SimpleTypeDeserializer typeDeserNoDefault = new SimpleTypeDeserializer("@type", null);
        ExternalTypeHandler.ExtTypedProperty extProp2 = new ExternalTypeHandler.ExtTypedProperty(prop, typeDeserNoDefault);
        assertFalse(extProp2.hasDefaultType());
    }

    @Test(timeout = 4000)
    public void testExtTypedPropertyGetDefaultTypeId() {
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", String.class);
        ExternalTypeHandler.ExtTypedProperty extProp = new ExternalTypeHandler.ExtTypedProperty(prop, typeDeser);
        String defaultId = extProp.getDefaultTypeId();
        assertNotNull(defaultId);
        assertEquals("testType", defaultId);
    }

    @Test(timeout = 4000)
    public void testExtTypedPropertyGetDefaultTypeIdNullImpl() {
        SimpleSettableBeanProperty prop = new SimpleSettableBeanProperty("value", null);
        SimpleTypeDeserializer typeDeser = new SimpleTypeDeserializer("@type", null);
        ExternalTypeHandler.ExtTypedProperty extProp = new ExternalTypeHandler.ExtTypedProperty(prop, typeDeser);
        assertNull(extProp.getDefaultTypeId());
    }
}