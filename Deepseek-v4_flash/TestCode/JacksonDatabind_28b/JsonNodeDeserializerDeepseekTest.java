package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.util.RawValue;

/**
 * White-box test suite for JsonNodeDeserializer and its inner classes.
 * Targets line/branch coverage and the known defect (END_OBJECT handling).
 */
public class JsonNodeDeserializerDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     * 
     * Partition A: Core functional logic & state transitions
     *   - getDeserializer for ObjectNode, ArrayNode, other
     *   - getNullValue (both overloads)
     *   - deserialize() with START_OBJECT, START_ARRAY, other tokens
     *   - ObjectDeserializer.deserialize() with START_OBJECT, FIELD_NAME, END_OBJECT (defect)
     *   - ArrayDeserializer.deserialize() with START_ARRAY, other token
     * 
     * Partition B: Boundary Value Analysis & Extremes
     *   - Empty JSON object "{}" -> END_OBJECT path
     *   - Empty JSON array "[]" -> END_ARRAY path
     *   - Null embedded object
     *   - byte[] embedded object
     *   - RawValue embedded object
     *   - JsonNode embedded object
     *   - Pojo embedded object
     *   - Number types: INT, LONG, BIG_INTEGER, FLOAT, DOUBLE, BIG_DECIMAL
     *   - Boolean true/false, null, string
     *   - Duplicate field handling (with and without FAIL_ON_READING_DUP_TREE_KEY)
     *   - Unexpected end-of-input in array
     *   - Mapping exception for invalid tokens
     * 
     * Partition C: Defect-targeted branch zone
     *   - ObjectDeserializer.deserialize() when parser is at END_OBJECT
     *     (known defect: should return empty ObjectNode, not throw)
     * 
     * Partition D: Exception & defensive guard paths
     *   - MappingException for invalid tokens in ObjectDeserializer and ArrayDeserializer
     *   - _reportProblem throws JsonMappingException
     *   - deserializeAny with unknown token
     * 
     * Partition E: Object lifecycle & contract integrity
     *   - isCachable returns true
     *   - deserializeWithType delegates to typeDeserializer
     */

    // Helper to create a DeserializationContext from a simple config
    private DeserializationContext createContext() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.getDeserializationContext();
    }

    // Helper to create a JsonParser from a JSON string
    private JsonParser createParser(String json) throws IOException {
        JsonFactory factory = new JsonFactory();
        return factory.createParser(json);
    }

    // ============================================================
    // Partition A: Core functional logic & state transitions
    // ============================================================

    @Test(timeout = 4000)
    public void testGetDeserializerForObjectNode() {
        JsonDeserializer<? extends JsonNode> deser = JsonNodeDeserializer.getDeserializer(ObjectNode.class);
        assertTrue("Should be ObjectDeserializer", deser instanceof JsonNodeDeserializer.ObjectDeserializer);
    }

    @Test(timeout = 4000)
    public void testGetDeserializerForArrayNode() {
        JsonDeserializer<? extends JsonNode> deser = JsonNodeDeserializer.getDeserializer(ArrayNode.class);
        assertTrue("Should be ArrayDeserializer", deser instanceof JsonNodeDeserializer.ArrayDeserializer);
    }

    @Test(timeout = 4000)
    public void testGetDeserializerForOther() {
        JsonDeserializer<? extends JsonNode> deser = JsonNodeDeserializer.getDeserializer(TextNode.class);
        assertTrue("Should be generic JsonNodeDeserializer", deser instanceof JsonNodeDeserializer);
    }

    @Test(timeout = 4000)
    public void testGetNullValueWithContext() {
        JsonNodeDeserializer deser = new JsonNodeDeserializer();
        DeserializationContext ctxt = createContext();
        JsonNode nullNode = deser.getNullValue(ctxt);
        assertTrue("Should be NullNode", nullNode instanceof NullNode);
    }

    @Test(timeout = 4000)
    public void testGetNullValueDeprecated() {
        JsonNodeDeserializer deser = new JsonNodeDeserializer();
        JsonNode nullNode = deser.getNullValue();
        assertTrue("Should be NullNode", nullNode instanceof NullNode);
    }

    @Test(timeout = 4000)
    public void testDeserializeStartObject() throws IOException {
        JsonNodeDeserializer deser = new JsonNodeDeserializer();
        JsonParser p = createParser("{\"a\":1}");
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = createContext();
        JsonNode result = deser.deserialize(p, ctxt);
        assertTrue("Should be ObjectNode", result instanceof ObjectNode);
        assertEquals(1, ((ObjectNode) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeStartArray() throws IOException {
        JsonNodeDeserializer deser = new JsonNodeDeserializer();
        JsonParser p = createParser("[1,2]");
        p.nextToken(); // START_ARRAY
        DeserializationContext ctxt = createContext();
        JsonNode result = deser.deserialize(p, ctxt);
        assertTrue("Should be ArrayNode", result instanceof ArrayNode);
        assertEquals(2, ((ArrayNode) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeOtherToken() throws IOException {
        JsonNodeDeserializer deser = new JsonNodeDeserializer();
        JsonParser p = createParser("\"hello\"");
        p.nextToken(); // VALUE_STRING
        DeserializationContext ctxt = createContext();
        JsonNode result = deser.deserialize(p, ctxt);
        assertTrue("Should be TextNode", result instanceof TextNode);
        assertEquals("hello", result.asText());
    }

    // ObjectDeserializer tests
    @Test(timeout = 4000)
    public void testObjectDeserializerStartObject() throws IOException {
        JsonNodeDeserializer.ObjectDeserializer deser = JsonNodeDeserializer.ObjectDeserializer.getInstance();
        JsonParser p = createParser("{\"x\":2}");
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = createContext();
        ObjectNode result = deser.deserialize(p, ctxt);
        assertEquals(1, result.size());
        assertEquals(2, result.get("x").asInt());
    }

    @Test(timeout = 4000)
    public void testObjectDeserializerFieldName() throws IOException {
        JsonNodeDeserializer.ObjectDeserializer deser = JsonNodeDeserializer.ObjectDeserializer.getInstance();
        JsonParser p = createParser("{\"y\":3}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME "y"
        DeserializationContext ctxt = createContext();
        ObjectNode result = deser.deserialize(p, ctxt);
        assertEquals(1, result.size());
        assertEquals(3, result.get("y").asInt());
    }

    // ============================================================
    // Partition C: Defect-targeted branch zone (END_OBJECT)
    // ============================================================

    @Test(timeout = 4000)
    public void testObjectDeserializerEndObject() throws IOException {
        // This targets the known defect: deserialize when parser is at END_OBJECT
        // Should return empty ObjectNode, not throw JsonMappingException
        JsonNodeDeserializer.ObjectDeserializer deser = JsonNodeDeserializer.ObjectDeserializer.getInstance();
        JsonParser p = createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = createContext();
        ObjectNode result = deser.deserialize(p, ctxt);
        assertNotNull("Should return empty ObjectNode", result);
        assertEquals(0, result.size());
    }

    // ============================================================
    // Partition D: Exception & defensive guard paths
    // ============================================================

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testObjectDeserializerInvalidToken() throws IOException {
        JsonNodeDeserializer.ObjectDeserializer deser = JsonNodeDeserializer.ObjectDeserializer.getInstance();
        JsonParser p = createParser("123");
        p.nextToken(); // VALUE_NUMBER_INT
        DeserializationContext ctxt = createContext();
        deser.deserialize(p, ctxt);
    }

    @Test(timeout = 4000)
    public void testArrayDeserializerStartArray() throws IOException {
        JsonNodeDeserializer.ArrayDeserializer deser = JsonNodeDeserializer.ArrayDeserializer.getInstance();
        JsonParser p = createParser("[1]");
        p.nextToken(); // START_ARRAY
        DeserializationContext ctxt = createContext();
        ArrayNode result = deser.deserialize(p, ctxt);
        assertEquals(1, result.size());
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testArrayDeserializerInvalidToken() throws IOException {
        JsonNodeDeserializer.ArrayDeserializer deser = JsonNodeDeserializer.ArrayDeserializer.getInstance();
        JsonParser p = createParser("123");
        p.nextToken(); // VALUE_NUMBER_INT
        DeserializationContext ctxt = createContext();
        deser.deserialize(p, ctxt);
    }

    // BaseNodeDeserializer tests
    @Test(timeout = 4000)
    public void testIsCachable() {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        assertTrue(deser.isCachable());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithType() throws IOException {
        // We can't easily mock TypeDeserializer, but we can verify it delegates
        // by using a simple implementation that returns a fixed node.
        // For simplicity, we test that it doesn't throw and returns something.
        JsonNodeDeserializer deser = new JsonNodeDeserializer();
        JsonParser p = createParser("{}");
        p.nextToken();
        DeserializationContext ctxt = createContext();
        // Use a trivial TypeDeserializer that returns a NullNode
        com.fasterxml.jackson.databind.jsontype.TypeDeserializer td = new com.fasterxml.jackson.databind.jsontype.TypeDeserializer() {
            @Override
            public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) throws IOException {
                return NullNode.getInstance();
            }
            // other methods not needed for this test
            @Override public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override public TypeDeserializer forProperty(BeanProperty prop) { return this; }
            @Override public String getTypeId() { return null; }
            @Override public Class<?> getDefaultImpl() { return null; }
            @Override public boolean hasDefaultImpl() { return false; }
        };
        Object result = deser.deserializeWithType(p, ctxt, td);
        assertTrue("Should be NullNode", result instanceof NullNode);
    }

    @Test(timeout = 4000)
    public void testReportProblem() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("{}");
        try {
            deser._reportProblem(p, "test error");
            fail("Should have thrown JsonMappingException");
        } catch (JsonMappingException e) {
            assertEquals("test error", e.getMessage());
        }
    }

    // ============================================================
    // Partition B: Boundary Value Analysis & Extremes
    // ============================================================

    @Test(timeout = 4000)
    public void testDeserializeObjectEmpty() throws IOException {
        // Empty object via deserializeObject directly
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("{}");
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = createContext();
        ObjectNode node = deser.deserializeObject(p, ctxt, ctxt.getNodeFactory());
        assertEquals(0, node.size());
    }

    @Test(timeout = 4000)
    public void testDeserializeArrayEmpty() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("[]");
        p.nextToken(); // START_ARRAY
        DeserializationContext ctxt = createContext();
        ArrayNode node = deser.deserializeArray(p, ctxt, ctxt.getNodeFactory());
        assertEquals(0, node.size());
    }

    @Test(timeout = 4000)
    public void testDeserializeArrayUnexpectedEnd() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("[");
        p.nextToken(); // START_ARRAY
        DeserializationContext ctxt = createContext();
        try {
            deser.deserializeArray(p, ctxt, ctxt.getNodeFactory());
            fail("Should have thrown JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Unexpected end-of-input"));
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeObjectDuplicateField() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("{\"a\":1,\"a\":2}");
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = createContext();
        ObjectNode node = deser.deserializeObject(p, ctxt, ctxt.getNodeFactory());
        // Last value wins by default
        assertEquals(2, node.get("a").asInt());
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testDeserializeObjectDuplicateFieldFail() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("{\"a\":1,\"a\":2}");
        p.nextToken(); // START_OBJECT
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        deser.deserializeObject(p, ctxt, ctxt.getNodeFactory());
    }

    @Test(timeout = 4000)
    public void testDeserializeAnyStartObject() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("{}");
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = createContext();
        JsonNode result = deser.deserializeAny(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof ObjectNode);
    }

    @Test(timeout = 4000)
    public void testDeserializeAnyEndObject() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = createContext();
        JsonNode result = deser.deserializeAny(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof ObjectNode);
        assertEquals(0, ((ObjectNode) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeAnyStartArray() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("[]");
        p.nextToken(); // START_ARRAY
        DeserializationContext ctxt = createContext();
        JsonNode result = deser.deserializeAny(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof ArrayNode);
    }

    @Test(timeout = 4000)
    public void testDeserializeAnyFieldName() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("{\"x\":1}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        DeserializationContext ctxt = createContext();
        JsonNode result = deser.deserializeAny(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof ObjectNode);
        assertEquals(1, ((ObjectNode) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeAnyEmbeddedObject() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        // Use a parser that returns an embedded object (e.g., from a tree)
        // We'll simulate by using a TokenBuffer
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeEmbeddedObject(new int[]{1,2});
        JsonParser p = buffer.asParser();
        p.nextToken(); // VALUE_EMBEDDED_OBJECT
        DeserializationContext ctxt = createContext();
        JsonNode result = deser.deserializeAny(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof POJONode);
    }

    @Test(timeout = 4000)
    public void testDeserializeAnyString() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("\"test\"");
        p.nextToken();
        DeserializationContext ctxt = createContext();
        JsonNode result = deser.deserializeAny(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof TextNode);
        assertEquals("test", result.asText());
    }

    @Test(timeout = 4000)
    public void testDeserializeAnyNumberInt() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("42");
        p.nextToken();
        DeserializationContext ctxt = createContext();
        JsonNode result = deser.deserializeAny(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof NumericNode);
        assertEquals(42, result.asInt());
    }

    @Test(timeout = 4000)
    public void testDeserializeAnyNumberFloat() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("3.14");
        p.nextToken();
        DeserializationContext ctxt = createContext();
        JsonNode result = deser.deserializeAny(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof NumericNode);
        assertTrue(result.isFloatingPointNumber());
    }

    @Test(timeout = 4000)
    public void testDeserializeAnyTrue() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("true");
        p.nextToken();
        DeserializationContext ctxt = createContext();
        JsonNode result = deser.deserializeAny(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof BooleanNode);
        assertTrue(result.booleanValue());
    }

    @Test(timeout = 4000)
    public void testDeserializeAnyFalse() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("false");
        p.nextToken();
        DeserializationContext ctxt = createContext();
        JsonNode result = deser.deserializeAny(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof BooleanNode);
        assertFalse(result.booleanValue());
    }

    @Test(timeout = 4000)
    public void testDeserializeAnyNull() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("null");
        p.nextToken();
        DeserializationContext ctxt = createContext();
        JsonNode result = deser.deserializeAny(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof NullNode);
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testDeserializeAnyInvalidToken() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        // Use a token that is not handled: e.g., END_ARRAY without context
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeEndArray();
        JsonParser p = buffer.asParser();
        p.nextToken(); // END_ARRAY
        DeserializationContext ctxt = createContext();
        deser.deserializeAny(p, ctxt, ctxt.getNodeFactory());
    }

    // _fromInt tests
    @Test(timeout = 4000)
    public void testFromIntWithInt() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("123");
        p.nextToken();
        DeserializationContext ctxt = createContext();
        JsonNode result = deser._fromInt(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof IntNode);
        assertEquals(123, result.asInt());
    }

    @Test(timeout = 4000)
    public void testFromIntWithLong() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("1234567890123");
        p.nextToken();
        DeserializationContext ctxt = createContext();
        JsonNode result = deser._fromInt(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof LongNode);
        assertEquals(1234567890123L, result.asLong());
    }

    @Test(timeout = 4000)
    public void testFromIntWithBigInteger() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("123456789012345678901234567890");
        p.nextToken();
        DeserializationContext ctxt = createContext();
        JsonNode result = deser._fromInt(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof BigIntegerNode);
        assertEquals(new BigInteger("123456789012345678901234567890"), result.asBigInteger());
    }

    @Test(timeout = 4000)
    public void testFromIntWithUseBigInteger() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("42");
        p.nextToken();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonNode result = deser._fromInt(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof BigIntegerNode);
    }

    @Test(timeout = 4000)
    public void testFromIntWithUseLong() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("42");
        p.nextToken();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.USE_LONG_FOR_INTS);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonNode result = deser._fromInt(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof LongNode);
    }

    // _fromFloat tests
    @Test(timeout = 4000)
    public void testFromFloatDouble() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("3.14");
        p.nextToken();
        DeserializationContext ctxt = createContext();
        JsonNode result = deser._fromFloat(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof DoubleNode);
        assertEquals(3.14, result.asDouble(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testFromFloatBigDecimal() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("3.141592653589793238462643383279");
        p.nextToken();
        DeserializationContext ctxt = createContext();
        JsonNode result = deser._fromFloat(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof DecimalNode);
    }

    @Test(timeout = 4000)
    public void testFromFloatUseBigDecimal() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("3.14");
        p.nextToken();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonNode result = deser._fromFloat(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof DecimalNode);
    }

    // _fromEmbedded tests
    @Test(timeout = 4000)
    public void testFromEmbeddedNull() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeEmbeddedObject(null);
        JsonParser p = buffer.asParser();
        p.nextToken();
        DeserializationContext ctxt = createContext();
        JsonNode result = deser._fromEmbedded(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof NullNode);
    }

    @Test(timeout = 4000)
    public void testFromEmbeddedByteArray() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        byte[] data = {1,2,3};
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeEmbeddedObject(data);
        JsonParser p = buffer.asParser();
        p.nextToken();
        DeserializationContext ctxt = createContext();
        JsonNode result = deser._fromEmbedded(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof BinaryNode);
        assertArrayEquals(data, ((BinaryNode) result).binaryValue());
    }

    @Test(timeout = 4000)
    public void testFromEmbeddedRawValue() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        RawValue rv = new RawValue("test");
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeEmbeddedObject(rv);
        JsonParser p = buffer.asParser();
        p.nextToken();
        DeserializationContext ctxt = createContext();
        JsonNode result = deser._fromEmbedded(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof ValueNode);
        // RawValueNode is internal, but we can check it's not null
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testFromEmbeddedJsonNode() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonNode embedded = TextNode.valueOf("embedded");
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeEmbeddedObject(embedded);
        JsonParser p = buffer.asParser();
        p.nextToken();
        DeserializationContext ctxt = createContext();
        JsonNode result = deser._fromEmbedded(p, ctxt, ctxt.getNodeFactory());
        assertSame(embedded, result);
    }

    @Test(timeout = 4000)
    public void testFromEmbeddedPojo() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        Object pojo = new Object();
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeEmbeddedObject(pojo);
        JsonParser p = buffer.asParser();
        p.nextToken();
        DeserializationContext ctxt = createContext();
        JsonNode result = deser._fromEmbedded(p, ctxt, ctxt.getNodeFactory());
        assertTrue(result instanceof POJONode);
        assertSame(pojo, ((POJONode) result).getPojo());
    }

    // Additional coverage for deserializeObject with various token types
    @Test(timeout = 4000)
    public void testDeserializeObjectWithAllTokenTypes() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        // Build a JSON object with all value types
        String json = "{\"obj\":{},\"arr\":[],\"emb\":null,\"str\":\"s\",\"int\":1,\"true\":true,\"false\":false,\"null\":null}";
        JsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = createContext();
        ObjectNode node = deser.deserializeObject(p, ctxt, ctxt.getNodeFactory());
        assertEquals(8, node.size());
        assertTrue(node.get("obj") instanceof ObjectNode);
        assertTrue(node.get("arr") instanceof ArrayNode);
        assertTrue(node.get("str") instanceof TextNode);
        assertTrue(node.get("int") instanceof IntNode);
        assertTrue(node.get("true") instanceof BooleanNode);
        assertTrue(node.get("false") instanceof BooleanNode);
        assertTrue(node.get("null") instanceof NullNode);
        // embedded object not present in this JSON, but we can test via token buffer
    }

    // Test deserializeArray with all token types
    @Test(timeout = 4000)
    public void testDeserializeArrayWithAllTokenTypes() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        String json = "[{},[],null,\"s\",1,true,false,null]";
        JsonParser p = createParser(json);
        p.nextToken(); // START_ARRAY
        DeserializationContext ctxt = createContext();
        ArrayNode node = deser.deserializeArray(p, ctxt, ctxt.getNodeFactory());
        assertEquals(8, node.size());
        assertTrue(node.get(0) instanceof ObjectNode);
        assertTrue(node.get(1) instanceof ArrayNode);
        assertTrue(node.get(2) instanceof NullNode);
        assertTrue(node.get(3) instanceof TextNode);
        assertTrue(node.get(4) instanceof IntNode);
        assertTrue(node.get(5) instanceof BooleanNode);
        assertTrue(node.get(6) instanceof BooleanNode);
        assertTrue(node.get(7) instanceof NullNode);
    }

    // Test that deserializeObject handles FIELD_NAME directly (when called from deserializeAny)
    @Test(timeout = 4000)
    public void testDeserializeObjectFromFieldName() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("{\"x\":1}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        DeserializationContext ctxt = createContext();
        ObjectNode node = deser.deserializeObject(p, ctxt, ctxt.getNodeFactory());
        assertEquals(1, node.size());
        assertEquals(1, node.get("x").asInt());
    }

    // Test that deserializeObject handles END_OBJECT directly (empty object)
    @Test(timeout = 4000)
    public void testDeserializeObjectFromEndObject() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = createContext();
        ObjectNode node = deser.deserializeObject(p, ctxt, ctxt.getNodeFactory());
        assertEquals(0, node.size());
    }

    // Test that deserializeObject throws on invalid token
    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testDeserializeObjectInvalidToken() throws IOException {
        BaseNodeDeserializer<?> deser = (BaseNodeDeserializer<?>) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        JsonParser p = createParser("123");
        p.nextToken(); // VALUE_NUMBER_INT
        DeserializationContext ctxt = createContext();
        deser.deserializeObject(p, ctxt, ctxt.getNodeFactory());
    }
}