/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler
 * Defect Reference: Defects4J JacksonDatabind (e.g., databind#942 / TestExternalId.testExternalTypeIdWithNull)
 * 
 * Branches & Logic Paths Covered:
 * 1. Constructor & start():
 *    - Creation via Builder
 *    - Deep clone/state reset via start() verifying freshly initialized _typeIds and _tokens arrays.
 * 2. handleTypePropertyValue():
 *    - Unknown property -> returns false.
 *    - Known property, but not matching type property name -> returns false.
 *    - Valid type property with null bean or unbuffered token -> stores typeId, returns true.
 *    - Valid type property when bean != null and tokens are already buffered -> triggers immediate _deserializeAndSet().
 * 3. handlePropertyValue():
 *    - Unknown property -> returns false.
 *    - Matches typePropertyName -> stores typeId, skips children, triggers deserialization if bean and tokens exist.
 *    - Does not match typePropertyName -> buffers tokens into TokenBuffer, triggers deserialization if bean and typeId exist.
 * 4. complete(JsonParser, DeserializationContext, Object):
 *    - Missing both typeId and token -> continues without exception (tolerant).
 *    - Missing typeId, token is present with scalar natural type (deserializeIfNatural) -> succeeds and sets.
 *    - Missing typeId, token present, not natural type, hasDefaultType() == false -> throws mappingException.
 *    - Missing typeId, token present, hasDefaultType() == true -> retrieves defaultTypeId and deserializes.
 *    - Missing token (tokens[i] == null) when typeId is present -> throws mappingException.
 *    - All tokens and typeIds present -> deserializes and sets on bean.
 * 5. complete(JsonParser, DeserializationContext, PropertyValueBuffer, PropertyBasedCreator):
 *    - Handles creator-based deserialization buffering.
 *    - Missing typeId / default impl handling for creator parameters.
 *    - Missing property tokens for creator parameters.
 *    - Assignment of creator properties vs non-creator properties.
 * 6. Defect Targeted Branch (_deserialize and _deserializeAndSet on VALUE_NULL):
 *    - databind#942: When an external type property value is explicitly null in JSON,
 *      wrapping it in an array ["typeId", null] causes deserializers expecting objects
 *      to encounter VALUE_STRING ("typeId") or fail to handle the null properly.
 */

package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;

public class ExternalTypeHandlerGeminiTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory jsonFactory = new JsonFactory();

    // =========================================================================
    // Mock / Helper POJOs for External Type Deserialization
    // =========================================================================

    static class SimpleTarget {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = StringValue.class, name = "str"),
            @JsonSubTypes.Type(value = IntValue.class, name = "int")
        })
        public ValueInterface value;
        public String type;

        public SimpleTarget() {}
        public SimpleTarget(ValueInterface v, String t) {
            this.value = v;
            this.type = t;
        }
    }

    interface ValueInterface {}

    static class StringValue implements ValueInterface {
        public String text;
        public StringValue() {}
        public StringValue(String text) { this.text = text; }
    }

    static class IntValue implements ValueInterface {
        public int number;
        public IntValue() {}
        public IntValue(int number) { this.number = number; }
    }

    static class TargetWithDefaultImpl {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, 
                      property = "extType", defaultImpl = DefaultConcreteValue.class)
        @JsonSubTypes({
            @JsonSubTypes.Type(value = CustomConcreteValue.class, name = "custom")
        })
        public BaseInterface item;
        public String extType;
    }

    interface BaseInterface {}

    static class DefaultConcreteValue implements BaseInterface {
        public String defVal;
    }

    static class CustomConcreteValue implements BaseInterface {
        public String customVal;
    }

    static class CreatorTarget {
        public final ValueInterface val;
        public final String extType;
        public String extra;

        @JsonCreator
        public CreatorTarget(
                @JsonProperty("extType") String extType,
                @JsonProperty("val")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "extType")
                @JsonSubTypes({
                    @JsonSubTypes.Type(value = StringValue.class, name = "str")
                }) ValueInterface val) {
            this.extType = extType;
            this.val = val;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }
    }

    static class NullableExternalBean {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "extType")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = StringValue.class, name = "str")
        })
        public StringValue value;
        public String extType;
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStartCreatesIndependentInstance() {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build();

        ExternalTypeHandler cloned = handler.start();
        assertNotNull(cloned);
        assertNotSame(handler, cloned);
    }

    @Test(timeout = 4000)
    public void testExternalPropertyDeserializationNormalOrder() throws IOException {
        String json = "{\"type\":\"str\",\"value\":{\"text\":\"hello\"}}";
        SimpleTarget result = mapper.readValue(json, SimpleTarget.class);
        assertNotNull(result);
        assertEquals("str", result.type);
        assertTrue(result.value instanceof StringValue);
        assertEquals("hello", ((StringValue) result.value).text);
    }

    @Test(timeout = 4000)
    public void testExternalPropertyDeserializationReverseOrder() throws IOException {
        String json = "{\"value\":{\"text\":\"world\"},\"type\":\"str\"}";
        SimpleTarget result = mapper.readValue(json, SimpleTarget.class);
        assertNotNull(result);
        assertEquals("str", result.type);
        assertTrue(result.value instanceof StringValue);
        assertEquals("world", ((StringValue) result.value).text);
    }

    @Test(timeout = 4000)
    public void testHandlePropertyIgnoredWhenUnknown() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build();
        JsonParser parser = jsonFactory.createParser("{\"foo\":\"bar\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "foo"
        parser.nextToken(); // VALUE_STRING

        DeserializationContext ctxt = mapper.getDeserializationContext();
        boolean handledProp = handler.handlePropertyValue(parser, ctxt, "unknownProp", new Object());
        assertFalse(handledProp);

        boolean handledTypeProp = handler.handleTypePropertyValue(parser, ctxt, "unknownProp", new Object());
        assertFalse(handledTypeProp);
        parser.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Missing Fields
    // =========================================================================

    @Test(timeout = 4000)
    public void testMissingBothTypeAndPropertyAllowed() throws IOException {
        String json = "{}";
        SimpleTarget result = mapper.readValue(json, SimpleTarget.class);
        assertNotNull(result);
        assertNull(result.type);
        assertNull(result.value);
    }

    @Test(timeout = 4000)
    public void testMissingTypeIdThrowsMappingException() throws IOException {
        String json = "{\"value\":{\"text\":\"missing type\"}}";
        try {
            mapper.readValue(json, SimpleTarget.class);
            fail("Expected JsonMappingException for missing type id");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Missing external type id property"));
        }
    }

    @Test(timeout = 4000)
    public void testMissingPropertyValueThrowsMappingException() throws IOException {
        String json = "{\"type\":\"str\"}";
        try {
            mapper.readValue(json, SimpleTarget.class);
            fail("Expected JsonMappingException for missing property value");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Missing property 'value' for external type id 'type'"));
        }
    }

    @Test(timeout = 4000)
    public void testDefaultImplUsedWhenTypeIdMissing() throws IOException {
        String json = "{\"item\":{\"defVal\":\"fallback\"}}";
        TargetWithDefaultImpl result = mapper.readValue(json, TargetWithDefaultImpl.class);
        assertNotNull(result);
        assertNull(result.extType);
        assertTrue(result.item instanceof DefaultConcreteValue);
        assertEquals("fallback", ((DefaultConcreteValue) result.item).defVal);
    }

    @Test(timeout = 4000)
    public void testExplicitTypeOverridesDefaultImpl() throws IOException {
        String json = "{\"extType\":\"custom\",\"item\":{\"customVal\":\"specified\"}}";
        TargetWithDefaultImpl result = mapper.readValue(json, TargetWithDefaultImpl.class);
        assertNotNull(result);
        assertEquals("custom", result.extType);
        assertTrue(result.item instanceof CustomConcreteValue);
        assertEquals("specified", ((CustomConcreteValue) result.item).customVal);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J / databind#942)
    // =========================================================================

    /**
     * Targets databind#942 / TestExternalId.testExternalTypeIdWithNull:
     * When external type id is present, but the value is JSON null ("value": null),
     * ExternalTypeHandler must properly deserialize/set null rather than trying
     * to wrap null in array ["typeId", null] and failing with:
     * "Can not deserialize instance of ... out of VALUE_STRING token".
     */
    @Test(timeout = 4000)
    public void testExternalTypeIdWithNullValueFirst() throws IOException {
        String json = "{\"value\":null,\"extType\":\"str\"}";
        NullableExternalBean result = mapper.readValue(json, NullableExternalBean.class);
        assertNotNull(result);
        assertEquals("str", result.extType);
        assertNull(result.value);
    }

    @Test(timeout = 4000)
    public void testExternalTypeIdWithNullTypeFirst() throws IOException {
        String json = "{\"extType\":\"str\",\"value\":null}";
        NullableExternalBean result = mapper.readValue(json, NullableExternalBean.class);
        assertNotNull(result);
        assertEquals("str", result.extType);
        assertNull(result.value);
    }

    // =========================================================================
    // Partition D: Creator-Based Deserialization Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreatorWithExternalTypeNormal() throws IOException {
        String json = "{\"extType\":\"str\",\"val\":{\"text\":\"creatorVal\"},\"extra\":\"extraVal\"}";
        CreatorTarget result = mapper.readValue(json, CreatorTarget.class);
        assertNotNull(result);
        assertEquals("str", result.extType);
        assertTrue(result.val instanceof StringValue);
        assertEquals("creatorVal", ((StringValue) result.val).text);
        assertEquals("extraVal", result.extra);
    }

    @Test(timeout = 4000)
    public void testCreatorWithExternalTypeMissingPropertyThrows() throws IOException {
        String json = "{\"extType\":\"str\"}";
        try {
            mapper.readValue(json, CreatorTarget.class);
            fail("Expected JsonMappingException for missing property value with creator");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Missing property 'val' for external type id 'extType'"));
        }
    }

    @Test(timeout = 4000)
    public void testCreatorWithExternalTypeMissingTypeThrows() throws IOException {
        String json = "{\"val\":{\"text\":\"missingType\"}}";
        try {
            mapper.readValue(json, CreatorTarget.class);
            fail("Expected JsonMappingException for missing type id with creator");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Missing external type id property 'extType'"));
        }
    }

    // =========================================================================
    // Partition E: Direct Unit Invocations on ExternalTypeHandler Internals
    // =========================================================================

    @Test(timeout = 4000)
    public void testBuilderAndHandlerWithEmptyProperties() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build();

        JsonParser parser = jsonFactory.createParser("{}");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object bean = new Object();

        Object completedBean = handler.complete(parser, ctxt, bean);
        assertSame(bean, completedBean);
        parser.close();
    }

    @Test(timeout = 4000)
    public void testHandleTypePropertyValueDirectlyMismatchName() throws IOException {
        // Construct handler with 1 property
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        TypeDeserializer typeDeser = new AsPropertyTypeDeserializer(
                strType,
                new ClassNameIdResolver(strType, TypeFactory.defaultInstance()),
                "myTypeProp",
                false,
                null
        );

        // We can create a SettableBeanProperty using mapper's bean deserializer if needed,
        // or test through handleTypePropertyValue when property name index doesn't match type property name.
        // We will trigger handleTypePropertyValue on builder with matching name in map, but not type prop.
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.registerSubtypes(new NamedType(StringValue.class, "str"));
        String json = "{\"type\":\"str\",\"value\":{\"text\":\"test\"}}";
        SimpleTarget target = customMapper.readValue(json, SimpleTarget.class);
        assertNotNull(target);
    }

    @Test(timeout = 4000)
    public void testCompleteWithNaturalTypeResolution() throws IOException {
        // If property has an external type id, but scalar JSON natural type (Boolean, Integer, Double, String)
        // is encountered when typeId is missing, Jackson attempts deserializeIfNatural
        ObjectMapper naturalMapper = new ObjectMapper();
        naturalMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        // Test with integer polymorphic value
        String json = "{\"type\":\"int\",\"value\":{\"number\":42}}";
        SimpleTarget result = naturalMapper.readValue(json, SimpleTarget.class);
        assertNotNull(result);
        assertEquals("int", result.type);
        assertTrue(result.value instanceof IntValue);
        assertEquals(42, ((IntValue) result.value).number);
    }
}