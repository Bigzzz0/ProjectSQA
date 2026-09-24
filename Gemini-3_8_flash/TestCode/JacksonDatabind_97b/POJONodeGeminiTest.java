package com.fasterxml.jackson.databind.node;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNodeType;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.util.RawValue;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.node.POJONode
 *
 * Decision / Condition Matrix:
 * 1. getNodeType() -> Always returns JsonNodeType.POJO.
 * 2. asToken() -> Always returns JsonToken.VALUE_EMBEDDED_OBJECT.
 * 3. binaryValue() ->
 *    - Branch: _value instanceof byte[] -> returns (byte[]) _value
 *    - Branch: _value not instanceof byte[] -> super.binaryValue() (returns null)
 * 4. asText() & asText(String defaultValue) ->
 *    - Branch: _value == null -> "null" / defaultValue
 *    - Branch: _value != null -> _value.toString()
 * 5. asBoolean(boolean defaultValue) ->
 *    - Branch: _value != null && _value instanceof Boolean (true/false) -> boolean value
 *    - Branch: _value == null or non-Boolean -> defaultValue
 * 6. asInt / asLong / asDouble (defaultValue) ->
 *    - Branch: _value instanceof Number -> int/long/double value
 *    - Branch: _value == null or non-Number -> defaultValue
 * 7. serialize(JsonGenerator gen, SerializerProvider ctxt) ->
 *    - Branch: _value == null -> ctxt.defaultSerializeNull(gen)
 *    - Branch: _value instanceof JsonSerializable -> ((JsonSerializable) _value).serialize(gen, ctxt)
 *    - Branch: other -> context-aware serialization (databind#1991). Defective version delegates via
 *              gen.writeObject(_value), losing SerializerProvider contextual state (e.g. attributes).
 * 8. equals(Object o) & _pojoEquals(POJONode other) ->
 *    - Branch: o == this -> true
 *    - Branch: o == null -> false
 *    - Branch: !(o instanceof POJONode) -> false
 *    - Branch: _value == null: other._value == null (true), other._value != null (false)
 *    - Branch: _value != null: _value.equals(other._value) (true/false)
 * 9. hashCode() -> _value.hashCode(), NPE if _value == null
 * 10. toString() ->
 *    - Branch: _value instanceof byte[] -> "(binary value of %d bytes)"
 *    - Branch: _value instanceof RawValue -> "(raw value '%s')"
 *    - Branch: other -> String.valueOf(_value)
 *
 * Known Defect (Defects4J databind#1991):
 * - POJONode.serialize delegates to gen.writeObject(_value) instead of context,
 *   stripping serializer provider attributes and contextual serializers.
 */
public class POJONodeGeminiTest {

    // =========================================================================
    // Supporting Artifacts for Serialization Testing
    // =========================================================================

    public static class ContextualPojo {
        @JsonSerialize(using = ContextualAttrSerializer.class)
        public String customStr = "ignored";
    }

    public static class ContextualAttrSerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider ctxt) throws IOException {
            Object attr = ctxt.getAttribute("myAttr");
            String str = (attr == null) ? "NULL" : attr.toString();
            gen.writeString("The value is: " + str);
        }
    }

    public static class TestJsonSerializable implements JsonSerializable {
        public boolean serialized = false;

        @Override
        public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
            serialized = true;
            gen.writeString("serialized-custom-json-serializable");
        }

        @Override
        public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer)
                throws IOException {
            serialize(gen, serializers);
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicNodeMetadata() {
        POJONode node = new POJONode("test");
        assertEquals(JsonNodeType.POJO, node.getNodeType());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, node.asToken());
        assertEquals("test", node.getPojo());
    }

    @Test(timeout = 4000)
    public void testBinaryValueExtraction() throws IOException {
        byte[] rawBytes = new byte[] { 10, 20, 30, 40 };
        POJONode node = new POJONode(rawBytes);
        assertArrayEquals(rawBytes, node.binaryValue());

        POJONode nonBinaryNode = new POJONode("not a byte array");
        assertNull(nonBinaryNode.binaryValue());

        POJONode nullBinaryNode = new POJONode(null);
        assertNull(nullBinaryNode.binaryValue());
    }

    @Test(timeout = 4000)
    public void testAsTextCoercions() {
        POJONode stringNode = new POJONode("text-content");
        assertEquals("text-content", stringNode.asText());
        assertEquals("text-content", stringNode.asText("default"));

        POJONode nullNode = new POJONode(null);
        assertEquals("null", nullNode.asText());
        assertEquals("customDefault", nullNode.asText("customDefault"));

        POJONode numberNode = new POJONode(12345);
        assertEquals("12345", numberNode.asText());
        assertEquals("12345", numberNode.asText("fallback"));
    }

    @Test(timeout = 4000)
    public void testAsBooleanCoercions() {
        POJONode trueNode = new POJONode(Boolean.TRUE);
        assertTrue(trueNode.asBoolean(false));

        POJONode falseNode = new POJONode(Boolean.FALSE);
        assertFalse(falseNode.asBoolean(true));

        POJONode nonBoolNode = new POJONode("string");
        assertTrue(nonBoolNode.asBoolean(true));
        assertFalse(nonBoolNode.asBoolean(false));

        POJONode nullNode = new POJONode(null);
        assertTrue(nullNode.asBoolean(true));
        assertFalse(nullNode.asBoolean(false));
    }

    @Test(timeout = 4000)
    public void testAsNumericCoercions() {
        POJONode intNode = new POJONode(Integer.valueOf(42));
        assertEquals(42, intNode.asInt(0));
        assertEquals(42L, intNode.asLong(0L));
        assertEquals(42.0, intNode.asDouble(0.0), 0.00001);

        POJONode doubleNode = new POJONode(Double.valueOf(3.14159));
        assertEquals(3, doubleNode.asInt(0));
        assertEquals(3L, doubleNode.asLong(0L));
        assertEquals(3.14159, doubleNode.asDouble(0.0), 0.00001);

        POJONode longNode = new POJONode(Long.valueOf(9876543210L));
        assertEquals((int) 9876543210L, longNode.asInt(0));
        assertEquals(9876543210L, longNode.asLong(0L));
        assertEquals(9876543210.0, longNode.asDouble(0.0), 0.00001);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNumericBoundariesAndFallback() {
        POJONode nonNumberNode = new POJONode("NaN");
        assertEquals(101, nonNumberNode.asInt(101));
        assertEquals(202L, nonNumberNode.asLong(202L));
        assertEquals(303.5, nonNumberNode.asDouble(303.5), 0.00001);

        POJONode nullNode = new POJONode(null);
        assertEquals(-1, nullNode.asInt(-1));
        assertEquals(-2L, nullNode.asLong(-2L));
        assertEquals(-3.14, nullNode.asDouble(-3.14), 0.00001);

        POJONode maxLongNode = new POJONode(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, maxLongNode.asLong(0L));

        POJONode minIntNode = new POJONode(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, minIntNode.asInt(0));
    }

    @Test(timeout = 4000)
    public void testEmptyAndEdgeValues() {
        POJONode emptyStrNode = new POJONode("");
        assertEquals("", emptyStrNode.asText());
        assertEquals("", emptyStrNode.asText("def"));
        assertEquals("", emptyStrNode.toString());

        POJONode emptyBytesNode = new POJONode(new byte[0]);
        assertEquals(0, emptyBytesNode.binaryValue().length);
        assertEquals("(binary value of 0 bytes)", emptyBytesNode.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (databind#1991 / Ground Truth)
    // =========================================================================

    /**
     * Targets databind#1991 / testPOJONodeCustomSer failure condition.
     * When serializing a POJONode wrapping an object with contextual/attribute-dependent
     * serializer, the SerializerProvider attributes MUST be preserved.
     * Defective version calls gen.writeObject(_value), losing SerializerProvider context
     * and causing expected:<...Str":"The value is: [Hello!]"}}> but was:<...Str":"The value is: [NULL]"}}>.
     */
    @Test(timeout = 4000)
    public void testPOJONodeCustomSer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> root = new HashMap<String, Object>();
        root.put("pojo", new POJONode(new ContextualPojo()));

        String json = mapper.writer().withAttribute("myAttr", "Hello!").writeValueAsString(root);
        assertEquals("{\"pojo\":{\"customStr\":\"The value is: Hello!\"}}", json);
    }

    @Test(timeout = 4000)
    public void testSerializeNullValue() throws Exception {
        POJONode nullNode = new POJONode(null);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(nullNode);
        assertEquals("null", json);
    }

    @Test(timeout = 4000)
    public void testSerializeJsonSerializable() throws Exception {
        TestJsonSerializable pojo = new TestJsonSerializable();
        POJONode node = new POJONode(pojo);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(node);

        assertTrue(pojo.serialized);
        assertEquals("\"serialized-custom-json-serializable\"", json);
    }

    @Test(timeout = 4000)
    public void testSerializeStandardObject() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "val");
        POJONode node = new POJONode(map);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(node);
        assertEquals("{\"key\":\"val\"}", json);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testHashCodeWithNullThrowsNpe() {
        new POJONode(null).hashCode();
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsContract() {
        POJONode node1 = new POJONode("content");
        POJONode node1Same = new POJONode("content");
        POJONode node2 = new POJONode("other");
        POJONode nullNode1 = new POJONode(null);
        POJONode nullNode2 = new POJONode(null);

        // Reflexive
        assertTrue(node1.equals(node1));
        assertTrue(nullNode1.equals(nullNode1));

        // Symmetric & Equivalent
        assertTrue(node1.equals(node1Same));
        assertTrue(node1Same.equals(node1));
        assertFalse(node1.equals(node2));

        // Null-value nodes
        assertTrue(nullNode1.equals(nullNode2));
        assertFalse(nullNode1.equals(node1));
        assertFalse(node1.equals(nullNode1));

        // Against null and other types
        assertFalse(node1.equals(null));
        assertFalse(node1.equals("plain string"));
        assertFalse(node1.equals(new TextNode("content")));
    }

    @Test(timeout = 4000)
    public void testHashCodeContract() {
        POJONode node1 = new POJONode("hash-test");
        POJONode node2 = new POJONode("hash-test");
        assertEquals("hash-test".hashCode(), node1.hashCode());
        assertEquals(node1.hashCode(), node2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToStringFormatting() {
        // Binary byte[] branch
        byte[] bytes = new byte[] { 1, 2, 3 };
        POJONode binaryNode = new POJONode(bytes);
        assertEquals("(binary value of 3 bytes)", binaryNode.toString());

        // RawValue branch
        RawValue rawValue = new RawValue("[100, 200]");
        POJONode rawNode = new POJONode(rawValue);
        assertEquals("(raw value '[100, 200]')", rawNode.toString());

        // Default String.valueOf branches
        POJONode strNode = new POJONode("greeting");
        assertEquals("greeting", strNode.toString());

        POJONode intNode = new POJONode(777);
        assertEquals("777", intNode.toString());

        POJONode nullNode = new POJONode(null);
        assertEquals("null", nullNode.toString());
    }
}