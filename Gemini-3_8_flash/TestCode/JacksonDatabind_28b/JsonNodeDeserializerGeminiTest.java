package com.fasterxml.jackson.databind.deser.std;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.util.RawValue;
import com.fasterxml.jackson.databind.util.TokenBuffer;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets:
 * - JsonNodeDeserializer & BaseNodeDeserializer (all subclasses and internal helpers)
 * - Defect Target (Defects4J Issue #941):
 *     ObjectDeserializer.deserialize(JsonParser, DeserializationContext) handling when
 *     the parser is positioned at END_OBJECT (e.g., empty JSON Object `{}` where the caller
 *     already advanced the parser or mapped out of END_OBJECT). In defective versions,
 *     ObjectDeserializer throws JsonMappingException ("Can not deserialize instance of
 *     ObjectNode out of END_OBJECT token").
 * - Branch Coverage:
 *     1. getDeserializer factory method for ObjectNode, ArrayNode, JsonNode, and specialized subclasses.
 *     2. getNullValue() and getNullValue(DeserializationContext) returning NullNode.
 *     3. isCachable() contract returning true.
 *     4. deserialize(p, ctxt) token routing (ID_START_OBJECT, ID_START_ARRAY, ID_STRING, ID_NUMBER_INT, ID_TRUE, etc.).
 *     5. ObjectDeserializer: START_OBJECT, FIELD_NAME, and END_OBJECT tokens, and invalid non-object tokens.
 *     6. ArrayDeserializer: isExpectedStartArrayToken true and false branch.
 *     7. deserializeObject: empty object, single/multiple fields, nested objects, nested arrays, duplicates
 *        with FAIL_ON_READING_DUP_TREE_KEY enabled/disabled, unexpected non-field tokens.
 *     8. deserializeArray: empty array, scalar types, nested arrays/objects, unexpected EOF.
 *     9. deserializeAny: ID_NUMBER_FLOAT, ID_EMBEDDED_OBJECT, ID_FALSE, ID_NULL, and invalid tokens.
 *    10. _fromInt: INT vs LONG vs BIG_INTEGER under default, USE_BIG_INTEGER_FOR_INTS, and USE_LONG_FOR_INTS.
 *    11. _fromFloat: double vs BigDecimal under default and USE_BIG_DECIMAL_FOR_FLOATS.
 *    12. _fromEmbedded: null, byte[], RawValue, JsonNode, and arbitrary POJO.
 *    13. deserializeWithType: invocation of TypeDeserializer.deserializeTypedFromAny.
 */
public class JsonNodeDeserializerGeminiTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory factory = new JsonFactory();

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Issue #941)
    // =========================================================================

    /**
     * Direct replication of Defects4J Issue #941:
     * When deserializing ObjectNode and the parser is already positioned at END_OBJECT
     * (e.g., after consuming START_OBJECT or advancing into an empty object),
     * ObjectDeserializer must succeed and return an empty ObjectNode rather than throwing
     * JsonMappingException: "Can not deserialize instance of ObjectNode out of END_OBJECT token".
     */
    @Test(timeout = 4000)
    public void testIssue941EmptyObjectNodeFromEndObject() throws Exception {
        JsonParser p = factory.createParser("{}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());

        ObjectNode node = mapper.readValue(p, ObjectNode.class);
        assertNotNull("ObjectNode should not be null", node);
        assertEquals("ObjectNode should be empty", 0, node.size());
        p.close();
    }

    /**
     * Variant of Issue #941: directly invoking ObjectDeserializer when parser points to END_OBJECT.
     */
    @Test(timeout = 4000)
    public void testObjectDeserializerDirectlyAtEndObject() throws Exception {
        JsonParser p = factory.createParser("{}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());

        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonDeserializer<? extends JsonNode> deser = JsonNodeDeserializer.getDeserializer(ObjectNode.class);

        JsonNode result = deser.deserialize(p, mapper.getDeserializationConfig().createDeserializationContext(p));
        assertNotNull(result);
        assertTrue(result instanceof ObjectNode);
        assertEquals(0, result.size());
        p.close();
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & FACTORY ROUTING
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetDeserializerFactoryResolution() {
        JsonDeserializer<?> objDeser = JsonNodeDeserializer.getDeserializer(ObjectNode.class);
        assertTrue(objDeser instanceof JsonNodeDeserializer.ObjectDeserializer);
        assertSame(objDeser, JsonNodeDeserializer.ObjectDeserializer.getInstance());

        JsonDeserializer<?> arrDeser = JsonNodeDeserializer.getDeserializer(ArrayNode.class);
        assertTrue(arrDeser instanceof JsonNodeDeserializer.ArrayDeserializer);
        assertSame(arrDeser, JsonNodeDeserializer.ArrayDeserializer.getInstance());

        JsonDeserializer<?> genericDeser1 = JsonNodeDeserializer.getDeserializer(JsonNode.class);
        assertTrue(genericDeser1 instanceof JsonNodeDeserializer);

        JsonDeserializer<?> genericDeser2 = JsonNodeDeserializer.getDeserializer(TextNode.class);
        assertSame(genericDeser1, genericDeser2);

        JsonDeserializer<?> genericDeser3 = JsonNodeDeserializer.getDeserializer(ValueNode.class);
        assertSame(genericDeser1, genericDeser3);
    }

    @Test(timeout = 4000)
    public void testNullValueAndCachability() {
        JsonNodeDeserializer deser = new JsonNodeDeserializer();
        assertTrue(deser.isCachable());

        JsonNode nullVal1 = deser.getNullValue();
        assertNotNull(nullVal1);
        assertTrue(nullVal1 instanceof NullNode);
        assertSame(NullNode.getInstance(), nullVal1);

        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonNode nullVal2 = deser.getNullValue(ctxt);
        assertSame(NullNode.getInstance(), nullVal2);
    }

    @Test(timeout = 4000)
    public void testDeserializeGenericObjectAndArray() throws Exception {
        JsonNode nodeObj = mapper.readTree("{\"key\":\"value\",\"num\":42}");
        assertTrue(nodeObj instanceof ObjectNode);
        assertEquals("value", nodeObj.get("key").asText());
        assertEquals(42, nodeObj.get("num").asInt());

        JsonNode nodeArr = mapper.readTree("[\"item\",123,true,null]");
        assertTrue(nodeArr instanceof ArrayNode);
        assertEquals(4, nodeArr.size());
        assertEquals("item", nodeArr.get(0).asText());
        assertEquals(123, nodeArr.get(1).asInt());
        assertTrue(nodeArr.get(2).asBoolean());
        assertTrue(nodeArr.get(3).isNull());
    }

    @Test(timeout = 4000)
    public void testObjectDeserializerFromStartObjectAndFieldName() throws Exception {
        // Starting from START_OBJECT
        JsonParser p1 = factory.createParser("{\"a\":1}");
        ObjectNode node1 = mapper.readValue(p1, ObjectNode.class);
        assertEquals(1, node1.get("a").asInt());
        p1.close();

        // Starting already at FIELD_NAME
        JsonParser p2 = factory.createParser("{\"b\":2}");
        assertEquals(JsonToken.START_OBJECT, p2.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p2.nextToken());
        ObjectNode node2 = mapper.readValue(p2, ObjectNode.class);
        assertEquals(2, node2.get("b").asInt());
        p2.close();
    }

    @Test(timeout = 4000)
    public void testArrayDeserializerFromStartArray() throws Exception {
        JsonParser p = factory.createParser("[10, 20]");
        ArrayNode arr = mapper.readValue(p, ArrayNode.class);
        assertNotNull(arr);
        assertEquals(2, arr.size());
        assertEquals(10, arr.get(0).asInt());
        assertEquals(20, arr.get(1).asInt());
        p.close();
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & NUMBER COERCIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testIntegerCoercionToBigInteger() throws Exception {
        ObjectMapper bigIntMapper = new ObjectMapper();
        bigIntMapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);

        JsonNode node = bigIntMapper.readTree("123");
        assertTrue("Expected BigIntegerNode, got: " + node.getClass().getName(), node instanceof BigIntegerNode);
        assertEquals(new BigInteger("123"), node.bigIntegerValue());

        JsonNode objNode = bigIntMapper.readTree("{\"val\":123}");
        assertTrue(objNode.get("val") instanceof BigIntegerNode);

        JsonNode arrNode = bigIntMapper.readTree("[123]");
        assertTrue(arrNode.get(0) instanceof BigIntegerNode);
    }

    @Test(timeout = 4000)
    public void testIntegerCoercionToLong() throws Exception {
        ObjectMapper longMapper = new ObjectMapper();
        longMapper.enable(DeserializationFeature.USE_LONG_FOR_INTS);

        JsonNode node = longMapper.readTree("123");
        assertTrue("Expected LongNode, got: " + node.getClass().getName(), node instanceof LongNode);
        assertEquals(123L, node.longValue());

        JsonNode objNode = longMapper.readTree("{\"val\":123}");
        assertTrue(objNode.get("val") instanceof LongNode);

        JsonNode arrNode = longMapper.readTree("[123]");
        assertTrue(arrNode.get(0) instanceof LongNode);
    }

    @Test(timeout = 4000)
    public void testDefaultIntegerAndLongBoundaries() throws Exception {
        JsonNode intNode = mapper.readTree(String.valueOf(Integer.MAX_VALUE));
        assertTrue(intNode instanceof IntNode);
        assertEquals(Integer.MAX_VALUE, intNode.asInt());

        long bigLong = ((long) Integer.MAX_VALUE) + 1000L;
        JsonNode longNode = mapper.readTree(String.valueOf(bigLong));
        assertTrue(longNode instanceof LongNode);
        assertEquals(bigLong, longNode.asLong());

        BigInteger massive = new BigInteger("9999999999999999999999999999999999");
        JsonNode bigNode = mapper.readTree(massive.toString());
        assertTrue(bigNode instanceof BigIntegerNode);
        assertEquals(massive, bigNode.bigIntegerValue());
    }

    @Test(timeout = 4000)
    public void testFloatCoercionToBigDecimal() throws Exception {
        ObjectMapper bigDecMapper = new ObjectMapper();
        bigDecMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        JsonNode node = bigDecMapper.readTree("123.456");
        assertTrue("Expected DecimalNode, got: " + node.getClass().getName(), node instanceof DecimalNode);
        assertEquals(new BigDecimal("123.456"), node.decimalValue());

        JsonNode defaultNode = mapper.readTree("123.456");
        assertTrue("Expected DoubleNode by default, got: " + defaultNode.getClass().getName(), defaultNode instanceof DoubleNode);
        assertEquals(123.456, defaultNode.doubleValue(), 0.00001);
    }

    @Test(timeout = 4000)
    public void testScalarTypesInDeserializeAny() throws Exception {
        assertTrue(mapper.readTree("true") instanceof BooleanNode);
        assertEquals(true, mapper.readTree("true").asBoolean());

        assertTrue(mapper.readTree("false") instanceof BooleanNode);
        assertEquals(false, mapper.readTree("false").asBoolean());

        assertTrue(mapper.readTree("null") instanceof NullNode);
        assertTrue(mapper.readTree("\"hello world\"") instanceof TextNode);
        assertEquals("hello world", mapper.readTree("\"hello world\"").asText());
    }

    // =========================================================================
    // PARTITION C (CONT.): DUPLICATE KEYS & EMBEDDED OBJECTS
    // =========================================================================

    @Test(timeout = 4000)
    public void testDuplicateFieldLastWinsByDefault() throws Exception {
        String json = "{\"x\": 1, \"x\": 2}";
        JsonNode node = mapper.readTree(json);
        assertTrue(node instanceof ObjectNode);
        assertEquals(2, node.get("x").asInt());
    }

    @Test(timeout = 4000)
    public void testDuplicateFieldThrowsWhenFeatureEnabled() throws Exception {
        ObjectMapper strictMapper = new ObjectMapper();
        strictMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);

        try {
            strictMapper.readTree("{\"dup\":10, \"dup\":20}");
            fail("Expected JsonMappingException for duplicate key");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Duplicate field 'dup'"));
        }
    }

    @Test(timeout = 4000)
    public void testEmbeddedObjectsHandling() throws Exception {
        // TokenBuffer allows injecting non-standard/embedded objects into JsonParser
        TokenBuffer buffer = new TokenBuffer(mapper, false);

        // 1. null embedded object
        buffer.writeStartObject();
        buffer.writeFieldName("nullObj");
        buffer.writeEmbeddedObject(null);

        // 2. byte[] embedded object
        byte[] binaryData = new byte[] { 1, 2, 3, 4 };
        buffer.writeFieldName("bin");
        buffer.writeEmbeddedObject(binaryData);

        // 3. RawValue embedded object
        RawValue raw = new RawValue("{\"raw\":true}");
        buffer.writeFieldName("rawVal");
        buffer.writeEmbeddedObject(raw);

        // 4. JsonNode embedded object
        TextNode embeddedNode = TextNode.valueOf("embeddedText");
        buffer.writeFieldName("nodeVal");
        buffer.writeEmbeddedObject(embeddedNode);

        // 5. Arbitrary POJO
        Object pojo = new java.util.Date(0L);
        buffer.writeFieldName("pojoVal");
        buffer.writeEmbeddedObject(pojo);

        buffer.writeEndObject();

        JsonParser parser = buffer.asParser();
        JsonNode result = mapper.readTree(parser);
        assertTrue(result instanceof ObjectNode);

        assertTrue(result.get("nullObj") instanceof NullNode);
        assertTrue(result.get("bin") instanceof BinaryNode);
        assertArrayEquals(binaryData, ((BinaryNode) result.get("bin")).binaryValue());
        assertEquals("embeddedText", result.get("nodeVal").asText());
        assertTrue(result.get("pojoVal") instanceof POJONode);
        assertSame(pojo, ((POJONode) result.get("pojoVal")).getPojo());
        parser.close();
        buffer.close();
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(timeout = 4000)
    public void testObjectDeserializerUnexpectedTokenThrows() throws Exception {
        JsonParser p = factory.createParser("[1, 2]");
        try {
            mapper.readValue(p, ObjectNode.class);
            fail("Expected JsonMappingException when reading ObjectNode from array input");
        } catch (JsonMappingException e) {
            // Success
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testArrayDeserializerUnexpectedTokenThrows() throws Exception {
        JsonParser p = factory.createParser("{\"a\":1}");
        try {
            mapper.readValue(p, ArrayNode.class);
            fail("Expected JsonMappingException when reading ArrayNode from object input");
        } catch (JsonMappingException e) {
            // Success
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeArrayUnexpectedEndOfInput() throws Exception {
        // Incomplete array: START_ARRAY without closing END_ARRAY
        JsonParser p = factory.createParser("[ 1, 2");
        try {
            mapper.readValue(p, ArrayNode.class);
            fail("Expected JsonMappingException for unexpected end-of-input");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Unexpected end-of-input"));
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeObjectUnexpectedTokenInsteadOfField() throws Exception {
        // Parsing token that is neither FIELD_NAME nor END_OBJECT
        JsonParser p = factory.createParser("{ 123 : 456 }");
        try {
            mapper.readTree(p);
            fail("Expected JsonMappingException for numeric token where field name is expected");
        } catch (Exception e) {
            // Expecting parsing/mapping failure
            assertTrue(e instanceof JsonMappingException || e instanceof com.fasterxml.jackson.core.JsonParseException);
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeAnyInvalidToken() throws Exception {
        // Position parser at END_OBJECT then attempt deserializeAny
        JsonParser p = factory.createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT

        // Call deserializeAny indirectly via BaseNodeDeserializer on unhandled token
        JsonNodeDeserializer deser = new JsonNodeDeserializer();
        DeserializationContext ctxt = mapper.getDeserializationConfig().createDeserializationContext(p);

        // When p is at END_ARRAY or closed, deserializeAny should throw
        JsonParser pArr = factory.createParser("[]");
        pArr.nextToken(); // START_ARRAY
        pArr.nextToken(); // END_ARRAY
        try {
            deser.deserializeAny(pArr, ctxt, JsonNodeFactory.instance);
            fail("Expected mappingException for END_ARRAY in deserializeAny");
        } catch (JsonMappingException e) {
            // Pass
        } finally {
            p.close();
            pArr.close();
        }
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE & TYPE DESERIALIZATION
    // =========================================================================

    static class Wrapper {
        public Object node;
    }

    @Test(timeout = 4000)
    public void testDeserializeWithType() throws Exception {
        ObjectMapper typedMapper = new ObjectMapper();
        typedMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);

        String json = "{\"node\":[\"com.fasterxml.jackson.databind.node.ObjectNode\",{\"k\":\"v\"}]}";
        Wrapper wrapper = typedMapper.readValue(json, Wrapper);
        assertNotNull(wrapper);
        assertTrue(wrapper.node instanceof ObjectNode);
        assertEquals("v", ((ObjectNode) wrapper.node).get("k").asText());
    }

    @Test(timeout = 4000)
    public void testNestedStructureCoverage() throws Exception {
        String complex = "{\"nestedObj\":{\"a\":true,\"b\":false,\"c\":null},\"nestedArr\":[[1,2],\"text\",1.5]}";
        JsonNode tree = mapper.readTree(complex);
        assertNotNull(tree);
        assertTrue(tree.isObject());
        assertTrue(tree.get("nestedObj").get("a").asBoolean());
        assertFalse(tree.get("nestedObj").get("b").asBoolean());
        assertTrue(tree.get("nestedObj").get("c").isNull());

        ArrayNode arr = (ArrayNode) tree.get("nestedArr");
        assertTrue(arr.get(0).isArray());
        assertEquals(2, arr.get(0).size());
        assertEquals("text", arr.get(1).asText());
        assertEquals(1.5, arr.get(2).asDouble(), 0.001);
    }
}