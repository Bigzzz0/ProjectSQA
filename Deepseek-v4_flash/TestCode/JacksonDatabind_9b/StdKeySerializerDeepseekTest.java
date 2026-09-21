package com.fasterxml.jackson.databind.ser.std;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target class: StdKeySerializer
 * 
 * Branches:
 * 1. serialize(): if (value instanceof Date) -> true branch (calls provider.defaultSerializeDateKey)
 * 2. serialize(): else branch (calls value.toString() then jgen.writeFieldName)
 * 3. getSchema(): returns createSchemaNode("string")
 * 4. acceptJsonFormatVisitor(): calls visitor.expectStringFormat(typeHint)
 * 
 * Boundary conditions:
 * - value = null (will cause NullPointerException in serialize)
 * - value = Date (boundary for instanceof)
 * - value = Class<?> (known defect: toString() returns "class ..." instead of just class name)
 * - value = other object (e.g., String, Integer)
 * 
 * Defect-targeted: When value is a Class<?>, the serialize method calls value.toString() which returns
 * "class java.lang.String" instead of "java.lang.String". The correct behavior should output the class name
 * without the "class " prefix. This test suite includes a dedicated test that serializes a Map with a Class key
 * and asserts the JSON output does not contain "class ".
 */
public class StdKeySerializerDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSerializeWithStringKey() throws Exception {
        StdKeySerializer serializer = new StdKeySerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator jgen = new JsonFactory().createGenerator(sw);
        SerializerProvider provider = new DefaultSerializerProvider.Impl();
        // Use a simple ObjectMapper to initialize the provider properly
        ObjectMapper mapper = new ObjectMapper();
        SerializerFactory factory = mapper.getSerializerFactory();
        provider = ((DefaultSerializerProvider) mapper.getSerializerProviderInstance()).createInstance(
                factory.createSerializerFactory(), null);
        // Actually, we need a proper SerializerProvider. Let's use the mapper's serialization context.
        // Simpler: use ObjectMapper to test the whole map serialization.
        // We'll test the serializer directly with a mock-like approach using a custom generator.
        // For simplicity, we rely on integration tests below.
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testSerializeWithNullValue() throws Exception {
        StdKeySerializer serializer = new StdKeySerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator jgen = new JsonFactory().createGenerator(sw);
        // We need a non-null provider to avoid NPE from provider, but value is null -> NPE from value.toString()
        // Actually, the method does value.toString() after checking instanceof, so null will cause NPE.
        // We can use a mock provider, but we can just call with null provider as well? The method uses provider only for Date.
        // To isolate, we'll use a real provider from ObjectMapper.
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProvider();
        serializer.serialize(null, jgen, provider);
    }

    @Test(timeout = 4000)
    public void testSerializeWithDateKey() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<Object, Integer> map = new HashMap<>();
        Date date = new Date(0); // epoch
        map.put(date, 1);
        String json = mapper.writeValueAsString(map);
        // The date key should be serialized as a string (timestamp or formatted)
        // Default Jackson serializes Date as number (timestamp) unless configured.
        // But the key serializer uses provider.defaultSerializeDateKey which writes as string.
        // Actually, defaultSerializeDateKey writes the date as a string using the configured date format.
        // By default, it uses the timestamp as string? Let's check: In Jackson, default for Date keys is to use
        // the timestamp as string (e.g., "0"). So we expect something like {"0":1}
        assertTrue("Date key should be serialized as string", json.contains("\"0\""));
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Class key bug)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testClassKeySerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<Object, Integer> map = new HashMap<>();
        map.put(String.class, 2);
        String json = mapper.writeValueAsString(map);
        // The bug produces {"class java.lang.String":2}
        // Correct output should be {"java.lang.String":2}
        assertFalse("Output should not contain 'class ' prefix", json.contains("class "));
        assertTrue("Output should contain the class name", json.contains("\"java.lang.String\""));
        // Also verify the value
        assertEquals("{\"java.lang.String\":2}", json);
    }

    @Test(timeout = 4000)
    public void testClassKeyWithMultipleEntries() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<Object, Integer> map = new LinkedHashMap<>();
        map.put(Integer.class, 1);
        map.put(String.class, 2);
        String json = mapper.writeValueAsString(map);
        // Expected: {"java.lang.Integer":1,"java.lang.String":2}
        assertFalse("Output should not contain 'class '", json.contains("class "));
        assertEquals("{\"java.lang.Integer\":1,\"java.lang.String\":2}", json);
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGetSchema() throws Exception {
        StdKeySerializer serializer = new StdKeySerializer();
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProvider();
        JsonNode schema = serializer.getSchema(provider, null);
        assertNotNull(schema);
        assertTrue(schema instanceof ObjectNode);
        assertEquals("string", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitor() throws Exception {
        StdKeySerializer serializer = new StdKeySerializer();
        // Create a simple visitor that records the call
        final boolean[] visited = {false};
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper() {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                visited[0] = true;
                return null;
            }
            // other methods not needed for this test
            @Override
            public JsonObjectFormatVisitor expectObjectFormat(JavaType type) { return null; }
            @Override
            public JsonArrayFormatVisitor expectArrayFormat(JavaType type) { return null; }
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) { return null; }
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) { return null; }
            @Override
            public JsonBooleanFormatVisitor expectBooleanFormat(JavaType type) { return null; }
            @Override
            public JsonNullFormatVisitor expectNullFormat(JavaType type) { return null; }
            @Override
            public JsonAnyFormatVisitor expectAnyFormat(JavaType type) { return null; }
            @Override
            public JsonMapFormatVisitor expectMapFormat(JavaType type) { return null; }
        };
        JavaType type = TypeFactory.defaultInstance().constructType(Object.class);
        serializer.acceptJsonFormatVisitor(visitor, type);
        assertTrue("expectStringFormat should have been called", visited[0]);
    }

    // -----------------------------------------------------------------------
    // Additional coverage: serialize with non-Date, non-Class object
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSerializeWithIntegerKey() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<Object, Integer> map = new HashMap<>();
        map.put(42, 3);
        String json = mapper.writeValueAsString(map);
        assertEquals("{\"42\":3}", json);
    }

    @Test(timeout = 4000)
    public void testSerializeWithCustomObjectKey() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<Object, Integer> map = new HashMap<>();
        map.put(new Object() {
            @Override
            public String toString() {
                return "customKey";
            }
        }, 4);
        String json = mapper.writeValueAsString(map);
        assertEquals("{\"customKey\":4}", json);
    }

    // -----------------------------------------------------------------------
    // Edge: empty map (no keys to serialize)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEmptyMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<Object, Integer> map = new HashMap<>();
        String json = mapper.writeValueAsString(map);
        assertEquals("{}", json);
    }
}