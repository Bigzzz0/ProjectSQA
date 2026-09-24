package com.fasterxml.jackson.databind.ser.std;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.ser.std.EnumSerializer
 * 
 * Branches & Conditions Analyzed:
 * 1. Constructor:
 *    - Deprecated single-arg constructor: EnumSerializer(EnumValues) -> sets _serializeAsIndex to null.
 *    - Two-arg constructor: EnumSerializer(EnumValues, Boolean) -> initializes values and serializeAsIndex flag.
 * 2. Static construct():
 *    - Uses EnumValues.constructFromName and _isShapeWrittenUsingIndex with fromClass=true.
 * 3. createContextual():
 *    - property == null -> returns this.
 *    - property != null, findFormatOverrides returns null -> returns this.
 *    - property != null, findFormatOverrides returns non-null:
 *        * serializeAsIndex != _serializeAsIndex -> creates new EnumSerializer with updated flag.
 *        * serializeAsIndex == _serializeAsIndex -> returns this.
 * 4. serialize():
 *    - _serializeAsIndex(serializers) is true -> writes ordinal (integer).
 *    - WRITE_ENUMS_USING_TO_STRING enabled -> writes en.toString().
 *    - default -> writes _values.serializedValueFor(en).
 * 5. getSchema():
 *    - _serializeAsIndex(provider) is true -> returns integer schema.
 *    - _serializeAsIndex(provider) is false:
 *        * typeHint is null -> returns string schema without enum array.
 *        * typeHint is not enum -> returns string schema without enum array.
 *        * typeHint is enum -> returns string schema with enum array containing names.
 * 6. acceptJsonFormatVisitor():
 *    - _serializeAsIndex(serializers) is true -> visitIntFormat.
 *    - _serializeAsIndex(serializers) is false:
 *        * visitor.expectStringFormat returns null -> graceful no-op.
 *        * visitor.expectStringFormat returns non-null:
 *            - WRITE_ENUMS_USING_TO_STRING enabled -> fills enumTypes with en.toString().
 *            - otherwise -> fills enumTypes with serialized names.
 * 7. _serializeAsIndex():
 *    - _serializeAsIndex != null -> returns boolean value.
 *    - _serializeAsIndex == null -> checks WRITE_ENUMS_USING_INDEX on serializers.
 * 8. _isShapeWrittenUsingIndex():
 *    - format == null or shape == null -> returns null.
 *    - shape ANY or SCALAR -> returns null.
 *    - shape STRING or NATURAL -> returns Boolean.FALSE.
 *    - shape isNumeric() or ARRAY -> returns Boolean.TRUE.
 *    - shape unsupported (e.g., OBJECT, BOOLEAN):
 *        * fromClass == true -> IllegalArgumentException mentioning "class annotation".
 *        * fromClass == false -> IllegalArgumentException mentioning "property annotation".
 *
 * Ground Truth Defect Targeted:
 * - EnumFormatShapeTest::testEnumPropertyAsNumber:
 *   Ensures property-level @JsonFormat(shape=JsonFormat.Shape.NUMBER) serializes enum as ordinal index,
 *   preventing regression where it serializes as string name.
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.EnumValues;

public class EnumSerializerGeminiTest {

    public enum TestColor {
        RED,
        YELLOW,
        GREEN;

        @Override
        public String toString() {
            return "color:" + name().toLowerCase();
        }
    }

    static class PojoWithEnumPropertyNumber {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public TestColor color = TestColor.GREEN;
    }

    static class PojoWithEnumPropertyNumberInt {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
        public TestColor color = TestColor.YELLOW;
    }

    static class PojoWithEnumPropertyArray {
        @JsonFormat(shape = JsonFormat.Shape.ARRAY)
        public TestColor color = TestColor.RED;
    }

    static class PojoWithEnumPropertyString {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public TestColor color = TestColor.GREEN;
    }

    static class PojoWithEnumPropertyNatural {
        @JsonFormat(shape = JsonFormat.Shape.NATURAL)
        public TestColor color = TestColor.YELLOW;
    }

    static class PojoWithDefaultEnum {
        public TestColor color = TestColor.RED;
    }

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testConstructAndGetEnumValues() {
        ObjectMapper mapper = new ObjectMapper();
        BeanDescription desc = mapper.getSerializationConfig().introspect(mapper.constructType(TestColor.class));
        EnumSerializer ser = EnumSerializer.construct(TestColor.class, mapper.getSerializationConfig(), desc, null);
        assertNotNull(ser);
        assertNull(ser._serializeAsIndex);

        EnumValues values = ser.getEnumValues();
        assertNotNull(values);
        assertEquals(TestColor.class, values.getEnumClass());
    }

    @Test(timeout = 4000)
    public void testSerializationWithExplicitIndexTrue() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestColor.class);
        EnumSerializer ser = new EnumSerializer(values, Boolean.TRUE);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        ser.serialize(TestColor.GREEN, gen, provider);
        gen.flush();

        assertEquals(String.valueOf(TestColor.GREEN.ordinal()), sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializationWithExplicitIndexFalse() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestColor.class);
        EnumSerializer ser = new EnumSerializer(values, Boolean.FALSE);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        ser.serialize(TestColor.GREEN, gen, provider);
        gen.flush();

        assertEquals("\"GREEN\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializationUsingToStringFeature() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestColor.class);
        EnumSerializer ser = new EnumSerializer(values, null);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        ser.serialize(TestColor.RED, gen, provider);
        gen.flush();

        assertEquals("\"color:red\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializationUsingIndexFeature() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestColor.class);
        EnumSerializer ser = new EnumSerializer(values, null);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        ser.serialize(TestColor.YELLOW, gen, provider);
        gen.flush();

        assertEquals("1", sw.toString());
    }

    @Test(timeout = 4000)
    public void testGetSchemaWhenSerializeAsIndex() {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestColor.class);
        EnumSerializer ser = new EnumSerializer(values, Boolean.TRUE);

        JsonNode schema = ser.getSchema(mapper.getSerializerProviderInstance(), TestColor.class);
        assertNotNull(schema);
        assertEquals("integer", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testGetSchemaWhenSerializeAsString() {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestColor.class);
        EnumSerializer ser = new EnumSerializer(values, Boolean.FALSE);

        JsonNode schema = ser.getSchema(mapper.getSerializerProviderInstance(), TestColor.class);
        assertNotNull(schema);
        assertEquals("string", schema.get("type").asText());

        JsonNode enumNode = schema.get("enum");
        assertNotNull(enumNode);
        assertTrue(enumNode.isArray());
        ArrayNode arr = (ArrayNode) enumNode;
        assertEquals(3, arr.size());
        assertEquals("RED", arr.get(0).asText());
        assertEquals("YELLOW", arr.get(1).asText());
        assertEquals("GREEN", arr.get(2).asText());
    }

    @Test(timeout = 4000)
    public void testGetSchemaWithNullOrNonEnumTypeHint() {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestColor.class);
        EnumSerializer ser = new EnumSerializer(values, Boolean.FALSE);

        JsonNode schemaNullType = ser.getSchema(mapper.getSerializerProviderInstance(), null);
        assertEquals("string", schemaNullType.get("type").asText());
        assertNull(schemaNullType.get("enum"));

        JsonNode schemaNonEnum = ser.getSchema(mapper.getSerializerProviderInstance(), String.class);
        assertEquals("string", schemaNonEnum.get("type").asText());
        assertNull(schemaNonEnum.get("enum"));
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Shape Rules
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testIsShapeWrittenUsingIndexAllShapes() {
        assertNull(EnumSerializer._isShapeWrittenUsingIndex(TestColor.class, null, true));
        assertNull(EnumSerializer._isShapeWrittenUsingIndex(TestColor.class, JsonFormat.Value.empty(), true));
        assertNull(EnumSerializer._isShapeWrittenUsingIndex(TestColor.class, JsonFormat.Value.forShape(JsonFormat.Shape.ANY), true));
        assertNull(EnumSerializer._isShapeWrittenUsingIndex(TestColor.class, JsonFormat.Value.forShape(JsonFormat.Shape.SCALAR), false));

        assertEquals(Boolean.FALSE, EnumSerializer._isShapeWrittenUsingIndex(TestColor.class, JsonFormat.Value.forShape(JsonFormat.Shape.STRING), true));
        assertEquals(Boolean.FALSE, EnumSerializer._isShapeWrittenUsingIndex(TestColor.class, JsonFormat.Value.forShape(JsonFormat.Shape.NATURAL), false));

        assertEquals(Boolean.TRUE, EnumSerializer._isShapeWrittenUsingIndex(TestColor.class, JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER), true));
        assertEquals(Boolean.TRUE, EnumSerializer._isShapeWrittenUsingIndex(TestColor.class, JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER_INT), false));
        assertEquals(Boolean.TRUE, EnumSerializer._isShapeWrittenUsingIndex(TestColor.class, JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER_FLOAT), true));
        assertEquals(Boolean.TRUE, EnumSerializer._isShapeWrittenUsingIndex(TestColor.class, JsonFormat.Value.forShape(JsonFormat.Shape.ARRAY), false));
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithNullProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestColor.class);
        EnumSerializer ser = new EnumSerializer(values, null);

        assertSame(ser, ser.createContextual(mapper.getSerializerProviderInstance(), null));
    }

    @Test(timeout = 4000)
    public void testDeprecatedConstructor() {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestColor.class);
        @SuppressWarnings("deprecation")
        EnumSerializer ser = new EnumSerializer(values);
        assertNull(ser._serializeAsIndex);
        assertEquals(values, ser.getEnumValues());
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testEnumPropertyAsNumber() throws Exception {
        // Targets known defect: com.fasterxml.jackson.databind.struct.EnumFormatShapeTest::testEnumPropertyAsNumber
        // expected: <{"color":2}> but was: <{"color":"GREEN"}>
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new PojoWithEnumPropertyNumber());
        assertEquals("{\"color\":2}", json);
    }

    @Test(timeout = 4000)
    public void testEnumPropertyAsNumberInt() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new PojoWithEnumPropertyNumberInt());
        assertEquals("{\"color\":1}", json);
    }

    @Test(timeout = 4000)
    public void testEnumPropertyAsArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new PojoWithEnumPropertyArray());
        assertEquals("{\"color\":0}", json);
    }

    @Test(timeout = 4000)
    public void testEnumPropertyOverridesGlobalIndexConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);

        // Class annotation or property annotation specifying STRING should override global WRITE_ENUMS_USING_INDEX
        String jsonString = mapper.writeValueAsString(new PojoWithEnumPropertyString());
        assertEquals("{\"color\":\"GREEN\"}", jsonString);

        String jsonNatural = mapper.writeValueAsString(new PojoWithEnumPropertyNatural());
        assertEquals("{\"color\":\"YELLOW\"}", jsonNatural);

        // Default property respects global WRITE_ENUMS_USING_INDEX
        String jsonDefault = mapper.writeValueAsString(new PojoWithDefaultEnum());
        assertEquals("{\"color\":0}", jsonDefault);
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testUnsupportedShapeFromClassThrowsException() {
        try {
            EnumSerializer._isShapeWrittenUsingIndex(TestColor.class,
                    JsonFormat.Value.forShape(JsonFormat.Shape.OBJECT), true);
            fail("Expected IllegalArgumentException for Shape.OBJECT on class");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unsupported serialization shape (OBJECT)"));
            assertTrue(e.getMessage().contains("class annotation"));
        }
    }

    @Test(timeout = 4000)
    public void testUnsupportedShapeFromPropertyThrowsException() {
        try {
            EnumSerializer._isShapeWrittenUsingIndex(TestColor.class,
                    JsonFormat.Value.forShape(JsonFormat.Shape.OBJECT), false);
            fail("Expected IllegalArgumentException for Shape.OBJECT on property");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unsupported serialization shape (OBJECT)"));
            assertTrue(e.getMessage().contains("property annotation"));
        }
    }

    @Test(timeout = 4000)
    public void testUnsupportedBooleanShapeThrowsException() {
        try {
            EnumSerializer._isShapeWrittenUsingIndex(TestColor.class,
                    JsonFormat.Value.forShape(JsonFormat.Shape.BOOLEAN), false);
            fail("Expected IllegalArgumentException for Shape.BOOLEAN");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unsupported serialization shape (BOOLEAN)"));
        }
    }

    /*
    /**********************************************************
    /* Partition E: Visitor & Schema Verification
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorIndexFormat() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestColor.class);
        EnumSerializer ser = new EnumSerializer(values, Boolean.TRUE);

        final List<JsonParser.NumberType> intFormats = new ArrayList<JsonParser.NumberType>();
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        intFormats.add(type);
                    }
                };
            }
        };

        ser.acceptJsonFormatVisitor(visitor, mapper.constructType(TestColor.class));
        assertEquals(1, intFormats.size());
        assertEquals(JsonParser.NumberType.INT, intFormats.get(0));
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorStringFormatNames() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestColor.class);
        EnumSerializer ser = new EnumSerializer(values, Boolean.FALSE);

        final Set<String> enumsSeen = new LinkedHashSet<String>();
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor.Base() {
                    @Override
                    public void enumTypes(Set<String> enums) {
                        enumsSeen.addAll(enums);
                    }
                };
            }
        };

        ser.acceptJsonFormatVisitor(visitor, mapper.constructType(TestColor.class));
        assertEquals(3, enumsSeen.size());
        assertTrue(enumsSeen.contains("RED"));
        assertTrue(enumsSeen.contains("YELLOW"));
        assertTrue(enumsSeen.contains("GREEN"));
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorStringFormatToString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestColor.class);
        EnumSerializer ser = new EnumSerializer(values, Boolean.FALSE);

        final Set<String> enumsSeen = new LinkedHashSet<String>();
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor.Base() {
                    @Override
                    public void enumTypes(Set<String> enums) {
                        enumsSeen.addAll(enums);
                    }
                };
            }
        };

        ser.acceptJsonFormatVisitor(visitor, mapper.constructType(TestColor.class));
        assertEquals(3, enumsSeen.size());
        assertTrue(enumsSeen.contains("color:red"));
        assertTrue(enumsSeen.contains("color:yellow"));
        assertTrue(enumsSeen.contains("color:green"));
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorNullStringVisitorGraceful() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestColor.class);
        EnumSerializer ser = new EnumSerializer(values, Boolean.FALSE);

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return null;
            }
        };

        ser.acceptJsonFormatVisitor(visitor, mapper.constructType(TestColor.class));
    }
}