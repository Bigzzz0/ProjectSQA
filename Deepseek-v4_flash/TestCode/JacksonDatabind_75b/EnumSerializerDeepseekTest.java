package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsonFormatVisitors.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.util.EnumValues;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic (serialize, getSchema, acceptJsonFormatVisitor)
 * - Partition B: BVA for _serializeAsIndex (null, true, false), WRITE_ENUMS_USING_INDEX, WRITE_ENUMS_USING_TO_STRING
 * - Partition C: Defect-targeted: property-level @JsonFormat(shape = Shape.NUMBER) should force index serialization
 * - Partition D: Exception paths in _isShapeWrittenUsingIndex (OBJECT shape)
 * - Partition E: Object lifecycle: createContextual (null property, overriding property), construct, deprecated constructor
 *
 * Targeted branches:
 *   serialize: _serializeAsIndex -> index branch; WRITE_ENUMS_USING_TO_STRING; default
 *   _serializeAsIndex: _serializeAsIndex != null vs. feature check
 *   _isShapeWrittenUsingIndex: shape == null, ANY, SCALAR, STRING, NATURAL, numeric, ARRAY, OBJECT (exception)
 *   createContextual: property null, format null, format changes
 *   getSchema: _serializeAsIndex true vs false, typeHint null vs enum type
 *   acceptJsonFormatVisitor: _serializeAsIndex true; false with/without WRITE_ENUMS_USING_TO_STRING
 */
public class EnumSerializerDeepseekTest {

    // Test enum for consistent use
    private enum Color { RED, GREEN, BLUE }

    // POJO for property-level annotation testing
    static class WrapperNumber {
        @JsonFormat(shape = Shape.NUMBER)
        public Color color;
    }

    static class WrapperString {
        @JsonFormat(shape = Shape.STRING)
        public Color color;
    }

    static class WrapperArray {
        @JsonFormat(shape = Shape.ARRAY)
        public Color color;
    }

    // ============================================================
    // Partition A: Core Functional Logic & State Transitions
    // ============================================================

    @Test(timeout = 4000)
    public void testSerializeDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // default: use name()
        assertEquals("\"GREEN\"", mapper.writeValueAsString(Color.GREEN));
    }

    @Test(timeout = 4000)
    public void testSerializeUsingIndex() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        assertEquals("1", mapper.writeValueAsString(Color.GREEN));
    }

    @Test(timeout = 4000)
    public void testSerializeUsingToString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        // Color.toString() returns "GREEN" as well, but we can use a custom enum with different toString
        assertEquals("\"GREEN\"", mapper.writeValueAsString(Color.GREEN));
    }

    @Test(timeout = 4000)
    public void testSerializeUsingIndexAndToString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        // index takes precedence over toString
        assertEquals("1", mapper.writeValueAsString(Color.GREEN));
    }

    // ============================================================
    // Partition B: BVA for _serializeAsIndex helper
    // ============================================================

    @Test(timeout = 4000)
    public void testSerializeAsIndexWhenNullAndFeatureDisabled() throws Exception {
        // _serializeAsIndex is null; feature disabled => should use name
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("\"GREEN\"", mapper.writeValueAsString(Color.GREEN));
    }

    @Test(timeout = 4000)
    public void testSerializeAsIndexWhenNullAndFeatureEnabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        assertEquals("1", mapper.writeValueAsString(Color.GREEN));
    }

    @Test(timeout = 4000)
    public void testSerializeAsIndexWhenTrue() throws Exception {
        // Construct EnumSerializer with _serializeAsIndex = true directly (via public constructor)
        EnumValues v = EnumValues.constructFromName(new ObjectMapper().getSerializationConfig(), Color.class);
        EnumSerializer ser = new EnumSerializer(v, Boolean.TRUE);
        // Use a simple generator
        com.fasterxml.jackson.core.util.DefaultPrettyPrinter pp = new com.fasterxml.jackson.core.util.DefaultPrettyPrinter();
        java.io.StringWriter sw = new java.io.StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        gen.writeStartObject();
        gen.writeFieldName("color");
        ser.serialize(Color.GREEN, gen, new ObjectMapper().getSerializerProviderInstance());
        gen.writeEndObject();
        gen.close();
        assertEquals("{\"color\":1}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeAsIndexWhenFalse() throws Exception {
        EnumValues v = EnumValues.constructFromName(new ObjectMapper().getSerializationConfig(), Color.class);
        EnumSerializer ser = new EnumSerializer(v, Boolean.FALSE);
        java.io.StringWriter sw = new java.io.StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        gen.writeStartObject();
        gen.writeFieldName("color");
        ser.serialize(Color.GREEN, gen, new ObjectMapper().getSerializerProviderInstance());
        gen.writeEndObject();
        gen.close();
        // Should use name because WRITE_ENUMS_USING_INDEX is disabled by default
        assertEquals("{\"color\":\"GREEN\"}", sw.toString());
    }

    // ============================================================
    // Partition C: Defect-Targeted Branch Zone
    // ============================================================

    @Test(timeout = 4000)
    public void testEnumPropertyAsNumber() throws Exception {
        // This test targets the known defect: @JsonFormat(shape = Shape.NUMBER) on property should serialize as index
        ObjectMapper mapper = new ObjectMapper();
        WrapperNumber w = new WrapperNumber();
        w.color = Color.GREEN;
        String json = mapper.writeValueAsString(w);
        // Expected: {"color":1}  (GREEN ordinal = 1)
        assertEquals("{\"color\":1}", json);
    }

    @Test(timeout = 4000)
    public void testEnumPropertyAsString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        WrapperString w = new WrapperString();
        w.color = Color.GREEN;
        String json = mapper.writeValueAsString(w);
        assertEquals("{\"color\":\"GREEN\"}", json);
    }

    @Test(timeout = 4000)
    public void testEnumPropertyAsArray() throws Exception {
        // ARRAY shape implies index serialization (since 2.6)
        ObjectMapper mapper = new ObjectMapper();
        WrapperArray w = new WrapperArray();
        w.color = Color.GREEN;
        String json = mapper.writeValueAsString(w);
        assertEquals("{\"color\":1}", json);
    }

    // ============================================================
    // Partition D: Exception & Defensive Guard Paths
    // ============================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsShapeWrittenUsingIndexWithObjectShape() throws Exception {
        // _isShapeWrittenUsingIndex should throw for OBJECT shape
        // Use reflection to invoke private static method? Better to trigger via contextual serialization.
        // We can create a fake format with OBJECT shape.
        // But easiest: directly call the static method via reflection in a helper.
        // Since it's package-private, we can call from same package.
        try {
            EnumSerializer._isShapeWrittenUsingIndex(Color.class,
                    JsonFormat.Value.forShape(Shape.OBJECT), true);
        } catch (IllegalArgumentException e) {
            // Expected
            throw e;
        }
        fail("Should have thrown IllegalArgumentException");
    }

    @Test(timeout = 4000)
    public void testIsShapeWrittenUsingIndexWithNullShape() throws Exception {
        // null shape => return null
        Boolean result = EnumSerializer._isShapeWrittenUsingIndex(Color.class, null, true);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testIsShapeWrittenUsingIndexWithAnyShape() throws Exception {
        // ANY => return null (dynamic)
        Boolean result = EnumSerializer._isShapeWrittenUsingIndex(Color.class,
                JsonFormat.Value.forShape(Shape.ANY), true);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testIsShapeWrittenUsingIndexWithScalarShape() throws Exception {
        // SCALAR => null (dynamic)
        Boolean result = EnumSerializer._isShapeWrittenUsingIndex(Color.class,
                JsonFormat.Value.forShape(Shape.SCALAR), true);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testIsShapeWrittenUsingIndexWithStringShape() throws Exception {
        // STRING => Boolean.FALSE
        Boolean result = EnumSerializer._isShapeWrittenUsingIndex(Color.class,
                JsonFormat.Value.forShape(Shape.STRING), true);
        assertEquals(Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testIsShapeWrittenUsingIndexWithNaturalShape() throws Exception {
        // NATURAL => Boolean.FALSE
        Boolean result = EnumSerializer._isShapeWrittenUsingIndex(Color.class,
                JsonFormat.Value.forShape(Shape.NATURAL), true);
        assertEquals(Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testIsShapeWrittenUsingIndexWithNumericShape() throws Exception {
        // numeric => Boolean.TRUE (e.g., NUMBER_INT)
        Boolean result = EnumSerializer._isShapeWrittenUsingIndex(Color.class,
                JsonFormat.Value.forShape(Shape.NUMBER_INT), true);
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testIsShapeWrittenUsingIndexWithArrayShape() throws Exception {
        // ARRAY => Boolean.TRUE
        Boolean result = EnumSerializer._isShapeWrittenUsingIndex(Color.class,
                JsonFormat.Value.forShape(Shape.ARRAY), true);
        assertEquals(Boolean.TRUE, result);
    }

    // ============================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ============================================================

    @Test(timeout = 4000)
    public void testConstruct() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(config.constructType(Color.class));
        // Use a format that does not force index (null)
        EnumSerializer ser = EnumSerializer.construct(Color.class, config, beanDesc, null);
        assertNotNull(ser);
        assertEquals(Color.class, ser.handledType());
    }

    @Test(timeout = 4000)
    public void testConstructWithStringFormat() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(config.constructType(Color.class));
        JsonFormat.Value format = JsonFormat.Value.forShape(Shape.STRING);
        EnumSerializer ser = EnumSerializer.construct(Color.class, config, beanDesc, format);
        assertNotNull(ser);
        // _serializeAsIndex should be Boolean.FALSE
        // We can test serialization behavior
        java.io.StringWriter sw = new java.io.StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        gen.writeStartObject();
        gen.writeFieldName("color");
        ser.serialize(Color.GREEN, gen, new ObjectMapper().getSerializerProviderInstance());
        gen.writeEndObject();
        gen.close();
        assertEquals("{\"color\":\"GREEN\"}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testDeprecatedConstructor() throws Exception {
        EnumValues v = EnumValues.constructFromName(new ObjectMapper().getSerializationConfig(), Color.class);
        EnumSerializer ser = new EnumSerializer(v);
        assertNotNull(ser);
        // _serializeAsIndex should be null
        assertNull(ser._serializeAsIndex); // package-private access
    }

    @Test(timeout = 4000)
    public void testCreateContextualNullProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues v = EnumValues.constructFromName(mapper.getSerializationConfig(), Color.class);
        EnumSerializer ser = new EnumSerializer(v, null);
        // createContextual with null property should return this
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<?> result = ser.createContextual(provider, null);
        assertSame(ser, result);
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithPropertyOverride() throws Exception {
        // This test checks that property-level annotation changes _serializeAsIndex
        ObjectMapper mapper = new ObjectMapper();
        // We need to get a BeanProperty with annotation. Simpler: use existing infrastructure.
        // Let's use the WrapperNumber class to create a property context.
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JavaType type = provider.constructType(WrapperNumber.class);
        BeanDescription beanDesc = provider.getConfig().introspect(type);
        // Get the "color" property
        List<BeanPropertyDefinition> props = beanDesc.findProperties();
        BeanPropertyDefinition colorProp = null;
        for (BeanPropertyDefinition p : props) {
            if (p.getName().equals("color")) {
                colorProp = p;
                break;
            }
        }
        assertNotNull("color property not found", colorProp);
        BeanProperty property = colorProp.getPrimaryMember();
        assertNotNull("Primary member not found", property);
        // Create a default serializer with null _serializeAsIndex
        EnumValues v = EnumValues.constructFromName(mapper.getSerializationConfig(), Color.class);
        EnumSerializer baseSer = new EnumSerializer(v, null);
        JsonSerializer<?> contextual = baseSer.createContextual(provider, property);
        assertNotNull(contextual);
        // Should be a new instance (different from baseSer) because property has NUMBER shape
        assertNotSame(baseSer, contextual);
        // Test that the new serializer serializes as index
        java.io.StringWriter sw = new java.io.StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        gen.writeStartObject();
        gen.writeFieldName("color");
        ((EnumSerializer)contextual).serialize(Color.GREEN, gen, provider);
        gen.writeEndObject();
        gen.close();
        assertEquals("{\"color\":1}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testGetSchemaWhenSerializeAsIndexTrue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues v = EnumValues.constructFromName(mapper.getSerializationConfig(), Color.class);
        EnumSerializer ser = new EnumSerializer(v, Boolean.TRUE);
        JsonNode schema = ser.getSchema(mapper.getSerializerProviderInstance(), null);
        assertTrue(schema.isObject());
        assertEquals("integer", schema.get("type").asText());
        assertTrue(schema.get("required").asBoolean());
    }

    @Test(timeout = 4000)
    public void testGetSchemaWhenSerializeAsIndexFalseWithEnumTypeHint() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues v = EnumValues.constructFromName(mapper.getSerializationConfig(), Color.class);
        EnumSerializer ser = new EnumSerializer(v, Boolean.FALSE);
        // Provide type hint as enum type
        JavaType enumType = mapper.getTypeFactory().constructType(Color.class);
        JsonNode schema = ser.getSchema(mapper.getSerializerProviderInstance(), enumType.getRawClass());
        assertTrue(schema.isObject());
        assertEquals("string", schema.get("type").asText());
        assertTrue(schema.has("enum"));
        ArrayNode enumValues = (ArrayNode) schema.get("enum");
        assertEquals(3, enumValues.size());
        assertEquals("RED", enumValues.get(0).asText());
        assertEquals("GREEN", enumValues.get(1).asText());
        assertEquals("BLUE", enumValues.get(2).asText());
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorWithIndex() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues v = EnumValues.constructFromName(mapper.getSerializationConfig(), Color.class);
        EnumSerializer ser = new EnumSerializer(v, Boolean.TRUE);
        // Create a visitor that records calls
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                fail("Should not be called when serializeAsIndex is true");
                return null;
            }
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                return new JsonIntegerFormatVisitor.Base();
            }
        };
        ser.acceptJsonFormatVisitor(visitor, null);
        // No exception means pass
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorWithoutIndexAndWithoutToString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues v = EnumValues.constructFromName(mapper.getSerializationConfig(), Color.class);
        EnumSerializer ser = new EnumSerializer(v, Boolean.FALSE);
        final List<String> capturedEnums = new ArrayList<String>();
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor.Base() {
                    @Override
                    public void enumTypes(Set<String> enums) {
                        capturedEnums.addAll(enums);
                    }
                };
            }
        };
        ser.acceptJsonFormatVisitor(visitor, mapper.constructType(Color.class));
        assertEquals(3, capturedEnums.size());
        assertTrue(capturedEnums.contains("RED"));
        assertTrue(capturedEnums.contains("GREEN"));
        assertTrue(capturedEnums.contains("BLUE"));
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorWithToString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        EnumValues v = EnumValues.constructFromName(mapper.getSerializationConfig(), Color.class);
        EnumSerializer ser = new EnumSerializer(v, Boolean.FALSE);
        final List<String> capturedEnums = new ArrayList<String>();
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor.Base() {
                    @Override
                    public void enumTypes(Set<String> enums) {
                        capturedEnums.addAll(enums);
                    }
                };
            }
        };
        ser.acceptJsonFormatVisitor(visitor, mapper.constructType(Color.class));
        // toString() for Color returns the same as name() by default
        assertEquals(3, capturedEnums.size());
        assertTrue(capturedEnums.contains("RED"));
        assertTrue(capturedEnums.contains("GREEN"));
        assertTrue(capturedEnums.contains("BLUE"));
    }

    // ============================================================
    // Additional edge case: null enum value
    // ============================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testSerializeNullEnum() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValueAsString((Color) null);
    }
}