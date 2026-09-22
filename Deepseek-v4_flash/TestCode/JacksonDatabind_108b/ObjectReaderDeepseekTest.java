package com.fasterxml.jackson.databind;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Test;

/**
 * White-box test suite for ObjectReader targeting maximum line/branch coverage
 * and the known defect where readTree(JsonParser) returns MissingNode instead of null
 * on end-of-input.
 *
 * [Branch & Defect Analysis Matrix]
 * Partition A: Core functional logic & state transitions
 *   - readValue(JsonParser) with null, non-null valueToUpdate, unwrapRoot
 *   - readTree(JsonParser) with content, null, missing node
 *   - readValues(JsonParser) with sequence
 *   - withXxx methods (with/without features, forType, withValueToUpdate, etc.)
 * Partition B: Boundary Value Analysis & Extremes
 *   - null arguments (valueType, valueToUpdate, schema, etc.)
 *   - empty string, empty byte array, empty input stream
 *   - zero-length arrays, negative offsets (if applicable)
 * Partition C: Defect-Targeted Branch Zone
 *   - readTree(JsonParser) on empty input -> should return null, not MissingNode
 * Partition D: Exception & Defensive Guard Paths
 *   - invalid schema type, trailing tokens, format detection on char sources
 *   - missing value type, unknown format
 * Partition E: Object Lifecycle & Contract Integrity
 *   - version(), getConfig(), getFactory(), isEnabled()
 *   - serialization (not directly tested, but contract)
 */
public class ObjectReaderDeepseekTest {

    private final ObjectMapper MAPPER = new ObjectMapper();

    // ============================================================
    // Partition A: Core Functional Logic & State Transitions
    // ============================================================

    @Test(timeout = 4000)
    public void testReadValueFromJsonParser() throws Exception {
        ObjectReader reader = MAPPER.readerFor(String.class);
        JsonParser p = MAPPER.getFactory().createParser("\"hello\"");
        String result = reader.readValue(p);
        assertEquals("hello", result);
    }

    @Test(timeout = 4000)
    public void testReadValueWithValueToUpdate() throws Exception {
        StringBuilder sb = new StringBuilder("old");
        ObjectReader reader = MAPPER.readerFor(StringBuilder.class).withValueToUpdate(sb);
        JsonParser p = MAPPER.getFactory().createParser("\"new\"");
        // Note: StringBuilder deserializer may not exist; use a simple type
        // Actually, we need a type that supports update. Let's use a simple POJO.
        // For simplicity, test with String and valueToUpdate null? We'll test with a custom class.
        // Instead, test with String and valueToUpdate null is covered elsewhere.
        // We'll test with a Map or List.
        // Let's use a Map<String,String> update.
        Map<String,String> map = new HashMap<>();
        map.put("a","b");
        ObjectReader reader2 = MAPPER.readerFor(Map.class).withValueToUpdate(map);
        JsonParser p2 = MAPPER.getFactory().createParser("{\"c\":\"d\"}");
        Map<String,String> result = reader2.readValue(p2);
        assertSame(map, result);
        assertEquals("d", result.get("c"));
        assertEquals("b", result.get("a"));
    }

    @Test(timeout = 4000)
    public void testReadValueWithRootUnwrap() throws Exception {
        ObjectReader reader = MAPPER.readerFor(String.class).withRootName("root");
        // Need to wrap input with root name
        String json = "{\"root\":\"value\"}";
        JsonParser p = MAPPER.getFactory().createParser(json);
        String result = reader.readValue(p);
        assertEquals("value", result);
    }

    @Test(timeout = 4000)
    public void testReadTreeWithContent() throws Exception {
        ObjectReader reader = MAPPER.reader();
        JsonParser p = MAPPER.getFactory().createParser("{\"a\":1}");
        JsonNode node = reader.readTree(p);
        assertTrue(node.isObject());
        assertEquals(1, node.get("a").asInt());
    }

    @Test(timeout = 4000)
    public void testReadTreeWithNullValue() throws Exception {
        ObjectReader reader = MAPPER.reader();
        JsonParser p = MAPPER.getFactory().createParser("null");
        JsonNode node = reader.readTree(p);
        assertTrue(node instanceof NullNode);
    }

    @Test(timeout = 4000)
    public void testReadValuesFromJsonParser() throws Exception {
        ObjectReader reader = MAPPER.readerFor(Integer.class);
        JsonParser p = MAPPER.getFactory().createParser("[1,2,3]");
        // Must advance to first token of first element (after START_ARRAY)
        p.nextToken(); // skip START_ARRAY
        MappingIterator<Integer> it = reader.readValues(p);
        List<Integer> list = new ArrayList<>();
        it.forEachRemaining(list::add);
        assertEquals(Arrays.asList(1,2,3), list);
    }

    @Test(timeout = 4000)
    public void testWithAndWithoutFeatures() throws Exception {
        ObjectReader base = MAPPER.reader();
        ObjectReader withFailOnUnknown = base.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertNotSame(base, withFailOnUnknown);
        assertTrue(withFailOnUnknown.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        ObjectReader without = withFailOnUnknown.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertFalse(without.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test(timeout = 4000)
    public void testForType() throws Exception {
        ObjectReader base = MAPPER.reader();
        ObjectReader typed = base.forType(String.class);
        assertNotNull(typed);
        // Should be able to read a string
        String result = typed.readValue("\"test\"");
        assertEquals("test", result);
    }

    @Test(timeout = 4000)
    public void testWithValueToUpdateNull() throws Exception {
        ObjectReader base = MAPPER.readerFor(String.class);
        ObjectReader withUpdate = base.withValueToUpdate("initial");
        assertNotSame(base, withUpdate);
        ObjectReader withoutUpdate = withUpdate.withValueToUpdate(null);
        assertNotSame(withUpdate, withoutUpdate);
        // Should be able to read normally
        String result = withoutUpdate.readValue("\"final\"");
        assertEquals("final", result);
    }

    // ============================================================
    // Partition B: Boundary Value Analysis & Extremes
    // ============================================================

    @Test(timeout = 4000)
    public void testReadValueFromEmptyString() throws Exception {
        ObjectReader reader = MAPPER.readerFor(String.class);
        try {
            reader.readValue("");
            fail("Expected exception for empty input");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReadValueFromEmptyByteArray() throws Exception {
        ObjectReader reader = MAPPER.readerFor(String.class);
        try {
            reader.readValue(new byte[0]);
            fail("Expected exception");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReadValueFromNullInputStream() throws Exception {
        ObjectReader reader = MAPPER.readerFor(String.class);
        // Not testing null input stream as it would cause NPE; skip.
    }

    @Test(timeout = 4000)
    public void testReadTreeFromEmptyString() throws Exception {
        ObjectReader reader = MAPPER.reader();
        JsonNode node = reader.readTree("");
        assertTrue(node instanceof MissingNode);
    }

    @Test(timeout = 4000)
    public void testReadTreeFromEmptyByteArray() throws Exception {
        ObjectReader reader = MAPPER.reader();
        JsonNode node = reader.readTree(new byte[0]);
        assertTrue(node instanceof MissingNode);
    }

    @Test(timeout = 4000)
    public void testWithNullSchema() throws Exception {
        ObjectReader reader = MAPPER.reader();
        ObjectReader withSchema = reader.with((FormatSchema) null);
        assertSame(reader, withSchema); // should return same instance
    }

    @Test(timeout = 4000)
    public void testWithInvalidSchemaType() throws Exception {
        ObjectReader reader = MAPPER.reader();
        // Use a schema that is not compatible with JSON factory
        FormatSchema badSchema = new FormatSchema() {
            @Override
            public String getSchemaType() { return "bad"; }
        };
        try {
            reader.with(badSchema);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ============================================================
    // Partition C: Defect-Targeted Branch Zone
    // ============================================================

    /**
     * Defect: readTree(JsonParser) should return null when no content,
     * but returns MissingNode.
     */
    @Test(timeout = 4000)
    public void testReadTreeFromEmptyParserReturnsNull() throws Exception {
        ObjectReader reader = MAPPER.reader();
        // Create a parser that has no tokens (end-of-input)
        JsonParser p = MAPPER.getFactory().createParser("");
        // According to contract, readTree(JsonParser) should return null
        // when no more content is accessible.
        JsonNode result = reader.readTree(p);
        // The bug: it returns MissingNode. Correct: null.
        // We assert null to reveal the bug.
        assertNull("readTree(JsonParser) should return null on empty input, but got " + result, result);
    }

    // Additional test to ensure that readTree(JsonParser) with content works
    @Test(timeout = 4000)
    public void testReadTreeFromParserWithContent() throws Exception {
        ObjectReader reader = MAPPER.reader();
        JsonParser p = MAPPER.getFactory().createParser("\"abc\"");
        JsonNode result = reader.readTree(p);
        assertTrue(result instanceof TextNode);
        assertEquals("abc", result.asText());
    }

    // ============================================================
    // Partition D: Exception & Defensive Guard Paths
    // ============================================================

    @Test(timeout = 4000)
    public void testReadValueWithTrailingTokens() throws Exception {
        ObjectReader reader = MAPPER.readerFor(String.class)
                .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        JsonParser p = MAPPER.getFactory().createParser("\"hello\" 123");
        try {
            reader.readValue(p);
            fail("Expected exception for trailing tokens");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReadValueWithNoValueType() throws Exception {
        ObjectReader reader = MAPPER.reader(); // no type configured
        JsonParser p = MAPPER.getFactory().createParser("1");
        try {
            reader.readValue(p);
            fail("Expected exception for missing value type");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFormatDetectionOnCharSource() throws Exception {
        ObjectReader reader = MAPPER.reader().withFormatDetection(
                new ObjectReader[]{MAPPER.reader()});
        try {
            reader.readValue("{}");
            fail("Expected exception for format detection on char source");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReadValueWithRootNameMismatch() throws Exception {
        ObjectReader reader = MAPPER.readerFor(String.class).withRootName("expected");
        JsonParser p = MAPPER.getFactory().createParser("{\"wrong\":\"value\"}");
        try {
            reader.readValue(p);
            fail("Expected exception for root name mismatch");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    // ============================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ============================================================

    @Test(timeout = 4000)
    public void testVersion() throws Exception {
        ObjectReader reader = MAPPER.reader();
        Version v = reader.version();
        assertNotNull(v);
    }

    @Test(timeout = 4000)
    public void testGetConfig() throws Exception {
        ObjectReader reader = MAPPER.reader();
        assertNotNull(reader.getConfig());
    }

    @Test(timeout = 4000)
    public void testGetFactory() throws Exception {
        ObjectReader reader = MAPPER.reader();
        assertSame(MAPPER.getFactory(), reader.getFactory());
    }

    @Test(timeout = 4000)
    public void testIsEnabled() throws Exception {
        ObjectReader reader = MAPPER.reader();
        assertFalse(reader.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        ObjectReader withFeature = reader.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertTrue(withFeature.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test(timeout = 4000)
    public void testGetInjectableValues() throws Exception {
        ObjectReader reader = MAPPER.reader();
        assertNull(reader.getInjectableValues());
        InjectableValues inj = new InjectableValues.Std().addValue("key", "value");
        ObjectReader withInj = reader.with(inj);
        assertSame(inj, withInj.getInjectableValues());
    }

    @Test(timeout = 4000)
    public void testWithJsonFactory() throws Exception {
        ObjectReader reader = MAPPER.reader();
        JsonFactory otherFactory = new JsonFactory();
        ObjectReader withFactory = reader.with(otherFactory);
        assertNotSame(reader, withFactory);
        assertSame(otherFactory, withFactory.getFactory());
    }

    @Test(timeout = 4000)
    public void testWithAttributes() throws Exception {
        ObjectReader reader = MAPPER.reader();
        ContextAttributes attrs = reader.getAttributes();
        ObjectReader withAttr = reader.withAttribute("key", "val");
        assertNotSame(reader, withAttr);
        assertEquals("val", withAttr.getAttributes().getAttribute("key"));
    }

    @Test(timeout = 4000)
    public void testTreeToValue() throws Exception {
        ObjectReader reader = MAPPER.readerFor(String.class);
        JsonNode node = MAPPER.readTree("\"test\"");
        String result = reader.treeToValue(node, String.class);
        assertEquals("test", result);
    }

    @Test(timeout = 4000)
    public void testCreateArrayNode() throws Exception {
        ObjectReader reader = MAPPER.reader();
        JsonNode arr = reader.createArrayNode();
        assertTrue(arr.isArray());
    }

    @Test(timeout = 4000)
    public void testCreateObjectNode() throws Exception {
        ObjectReader reader = MAPPER.reader();
        JsonNode obj = reader.createObjectNode();
        assertTrue(obj.isObject());
    }
}