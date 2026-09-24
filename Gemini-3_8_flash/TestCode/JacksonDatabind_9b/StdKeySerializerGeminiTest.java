package com.fasterxml.jackson.databind.ser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.ser.std.StdKeySerializer
 * 
 * Branches & Conditions Targeted:
 * 1. Branch: `value instanceof Date` -> TRUE:
 *    - Delegates to `provider.defaultSerializeDateKey((Date) value, jgen)` and early returns.
 * 2. Branch: `value instanceof Date` -> FALSE:
 *    - Standard Object Key: uses `value.toString()`.
 *    - Empty string key: boundary testing of empty values.
 *    - Numeric/Boolean/Enum types: converts through `toString()`.
 * 3. Defect-Targeted Branch Zone (Defects4J ground truth: TestMapSerialization::testClassKey):
 *    - When `value instanceof Class<?>`:
 *      - Defective behavior: Invokes `value.toString()`, resulting in `"class java.lang.String"` or `"interface java.util.Map"`.
 *      - Fixed/Expected behavior: Must output `((Class<?>) value).getName()` -> `"java.lang.String"` without the `"class "` prefix.
 * 4. Defensive & Exception Paths:
 *    - `value == null` -> Expects NullPointerException (no null guard before `value.toString()`).
 *    - `visitor == null` in `acceptJsonFormatVisitor` -> Expects NullPointerException.
 * 5. Schema & Visitor Contracts:
 *    - `getSchema` -> Verifies string type node schema generation.
 *    - `acceptJsonFormatVisitor` -> Verifies string format visitor dispatch.
 *    - `handledType` -> Verifies `Object.class` handling contract.
 */
public class StdKeySerializerGeminiTest {

    private final JsonFactory jsonFactory = new JsonFactory();

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testHandledTypeIsObject() {
        StdKeySerializer keySerializer = new StdKeySerializer();
        assertEquals(Object.class, keySerializer.handledType());
    }

    @Test(timeout = 4000)
    public void testSerializeStringKey() throws IOException {
        StdKeySerializer keySerializer = new StdKeySerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        gen.writeStartObject();
        keySerializer.serialize("testKey", gen, null);
        gen.writeString("testValue");
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"testKey\":\"testValue\"}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeIntegerKey() throws IOException {
        StdKeySerializer keySerializer = new StdKeySerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        gen.writeStartObject();
        keySerializer.serialize(12345, gen, null);
        gen.writeNumber(99);
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"12345\":99}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeBooleanKey() throws IOException {
        StdKeySerializer keySerializer = new StdKeySerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        gen.writeStartObject();
        keySerializer.serialize(Boolean.TRUE, gen, null);
        gen.writeBoolean(false);
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"true\":false}", sw.toString());
    }

    private enum TestEnum {
        ALPHA, BETA
    }

    @Test(timeout = 4000)
    public void testSerializeEnumKey() throws IOException {
        StdKeySerializer keySerializer = new StdKeySerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        gen.writeStartObject();
        keySerializer.serialize(TestEnum.ALPHA, gen, null);
        gen.writeString("enumVal");
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"ALPHA\":\"enumVal\"}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeCustomObjectKey() throws IOException {
        StdKeySerializer keySerializer = new StdKeySerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        Object customObj = new Object() {
            @Override
            public String toString() {
                return "custom_id_42";
            }
        };

        gen.writeStartObject();
        keySerializer.serialize(customObj, gen, null);
        gen.writeString("payload");
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"custom_id_42\":\"payload\"}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeDateKeyBranch() throws IOException {
        StdKeySerializer keySerializer = new StdKeySerializer();
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider provider = ((DefaultSerializerProvider) mapper.getSerializerProvider())
                .createInstance(mapper.getSerializationConfig(), mapper.getSerializerFactory());

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);

        gen.writeStartObject();
        Date date = new Date(100000000L);
        keySerializer.serialize(date, gen, provider);
        gen.writeString("dateVal");
        gen.writeEndObject();
        gen.close();

        String json = sw.toString();
        assertTrue("JSON output should contain the value string", json.contains("dateVal"));
        assertTrue("JSON output should contain the formatted or timestamp date key", json.contains("100000000") || json.contains("1970"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializeEmptyStringKey() throws IOException {
        StdKeySerializer keySerializer = new StdKeySerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        gen.writeStartObject();
        keySerializer.serialize("", gen, null);
        gen.writeString("emptyKeyVal");
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"\":\"emptyKeyVal\"}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializePrimitiveClassKey() throws IOException {
        StdKeySerializer keySerializer = new StdKeySerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        gen.writeStartObject();
        keySerializer.serialize(int.class, gen, null);
        gen.writeNumber(100);
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"int\":100}", sw.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Direct test revealing the defect:
     * When serializing a Class object as a key, StdKeySerializer must serialize
     * the class name (e.g. "java.lang.String") without the "class " prefix
     * produced by Class.toString().
     */
    @Test(timeout = 4000)
    public void testDirectClassKeySerializationDefect() throws IOException {
        StdKeySerializer keySerializer = new StdKeySerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        gen.writeStartObject();
        keySerializer.serialize(String.class, gen, null);
        gen.writeNumber(2);
        gen.writeEndObject();
        gen.close();

        // On defective version: outputs '{"class java.lang.String":2}'
        // Expected behavior: outputs '{"java.lang.String":2}'
        assertEquals("{\"java.lang.String\":2}", sw.toString());
    }

    /**
     * Direct test targeting interface Class key serialization:
     * Interface toString() produces "interface java.util.Map", but key should be "java.util.Map".
     */
    @Test(timeout = 4000)
    public void testDirectInterfaceKeySerializationDefect() throws IOException {
        StdKeySerializer keySerializer = new StdKeySerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        gen.writeStartObject();
        keySerializer.serialize(Map.class, gen, null);
        gen.writeNumber(2);
        gen.writeEndObject();
        gen.close();

        // On defective version: outputs '{"interface java.util.Map":2}'
        // Expected behavior: outputs '{"java.util.Map":2}'
        assertEquals("{\"java.util.Map\":2}", sw.toString());
    }

    /**
     * End-to-end Map serialization reproducing TestMapSerialization::testClassKey defect.
     */
    @Test(timeout = 4000)
    public void testMapSerializationWithClassKeyDefect() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<Class<?>, Integer> map = new LinkedHashMap<Class<?>, Integer>();
        map.put(String.class, 2);
        String json = mapper.writeValueAsString(map);

        assertEquals("{\"java.lang.String\":2}", json);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testSerializeNullValueThrowsNpe() throws IOException {
        StdKeySerializer keySerializer = new StdKeySerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        keySerializer.serialize(null, gen, null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testAcceptJsonFormatVisitorNullVisitorThrowsNpe() throws Exception {
        StdKeySerializer keySerializer = new StdKeySerializer();
        keySerializer.acceptJsonFormatVisitor(null, null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Schema Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetSchemaReturnsStringType() throws JsonMappingException {
        StdKeySerializer keySerializer = new StdKeySerializer();
        JsonNode schemaNode = keySerializer.getSchema(null, null);

        assertNotNull("Schema node should not be null", schemaNode);
        assertTrue("Schema node should be an ObjectNode", schemaNode.isObject());
        assertEquals("string", schemaNode.path("type").asText());
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorDispatchesExpectStringFormat() throws JsonMappingException {
        StdKeySerializer keySerializer = new StdKeySerializer();
        final boolean[] visited = new boolean[1];
        final JavaType[] capturedType = new JavaType[1];

        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) throws JsonMappingException {
                visited[0] = true;
                capturedType[0] = type;
                return null;
            }
        };

        JavaType expectedType = TypeFactory.defaultInstance().constructType(String.class);
        keySerializer.acceptJsonFormatVisitor(visitor, expectedType);

        assertTrue("expectStringFormat should have been invoked", visited[0]);
        assertEquals("Captured JavaType should match the type passed in", expectedType, capturedType[0]);
    }
}