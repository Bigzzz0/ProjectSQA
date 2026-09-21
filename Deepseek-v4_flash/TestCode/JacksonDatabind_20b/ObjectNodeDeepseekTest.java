package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: ObjectNode.java (Jackson databind)
 * 
 * Branches covered:
 * - deepCopy(): iteration over entries, recursive deepCopy
 * - getNodeType(), asToken(), size(): trivial returns
 * - elements(), fieldNames(), fields(): delegation to map views
 * - get(int): always returns null
 * - get(String): map lookup
 * - path(int): always MissingNode
 * - path(String): map lookup with null check -> MissingNode
 * - with(String): existing ObjectNode -> return; existing non-ObjectNode -> throw; absent -> create
 * - withArray(String): similar with ArrayNode
 * - findValue(String): recursive search, null return
 * - findValues(String, List): recursive, null list handling
 * - findValuesAsText(String, List): similar
 * - findParent(String): recursive, return this or child
 * - findParents(String, List): recursive, null list handling
 * - serialize(JsonGenerator, SerializerProvider): writeStartObject, iterate, writeFieldName, serialize child, writeEndObject
 * - serializeWithType: similar with type prefix/suffix
 * - set(String, JsonNode): null value -> nullNode, put
 * - setAll(Map): iterate, null value -> nullNode, put
 * - setAll(ObjectNode): putAll
 * - replace(String, JsonNode): null value -> nullNode, put, return old
 * - without(String): remove, return this
 * - without(Collection): keySet().removeAll
 * - put(String, JsonNode): deprecated, similar to replace
 * - remove(String): map remove
 * - remove(Collection): keySet().removeAll
 * - removeAll(): clear
 * - retain(Collection): keySet().retainAll
 * - retain(String...): delegates to retain(Collection)
 * - putArray(String): create ArrayNode, _put
 * - putObject(String): create ObjectNode, _put
 * - putPOJO(String, Object): pojoNode, _put
 * - putNull(String): nullNode, _put
 * - put(String, short/Short/int/Integer/long/Long/float/Float/double/Double/BigDecimal/String/boolean/Boolean/byte[]): null handling, _put
 * - equals(Object): identity, null, type check, _childrenEqual
 * - _childrenEqual(ObjectNode): map equals
 * - hashCode(): map hashCode
 * - toString(): build string with quotes and commas
 * - _put(String, JsonNode): put, return this
 * 
 * Boundary conditions:
 * - null field names (allowed? map allows null key)
 * - null values in set/setAll/replace/put -> converted to NullNode
 * - empty map/collection for setAll/without/retain
 * - large number of children for toString
 * - recursive structures for find methods
 * - serialization with null children
 * 
 * Defect-targeted branch:
 * - Two setAll methods (Map and ObjectNode) cause ambiguous setter for property "all"
 *   when ObjectNode is used as @JsonUnwrapped bean property.
 *   Test triggers JsonMappingException during deserialization.
 */
public class ObjectNodeDeepseekTest {

    private final JsonNodeFactory factory = JsonNodeFactory.instance;

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testDeepCopy() {
        ObjectNode node = factory.objectNode();
        node.put("a", 1);
        node.put("b", "text");
        ObjectNode copy = node.deepCopy();
        assertNotSame(node, copy);
        assertEquals(node, copy);
        // Modify original, copy unchanged
        node.put("a", 2);
        assertNotEquals(node.get("a"), copy.get("a"));
    }

    @Test(timeout = 4000)
    public void testGetNodeType() {
        ObjectNode node = factory.objectNode();
        assertEquals(JsonNodeType.OBJECT, node.getNodeType());
    }

    @Test(timeout = 4000)
    public void testAsToken() {
        ObjectNode node = factory.objectNode();
        assertEquals(JsonToken.START_OBJECT, node.asToken());
    }

    @Test(timeout = 4000)
    public void testSize() {
        ObjectNode node = factory.objectNode();
        assertEquals(0, node.size());
        node.put("x", 1);
        assertEquals(1, node.size());
        node.put("y", 2);
        assertEquals(2, node.size());
        node.remove("x");
        assertEquals(1, node.size());
    }

    @Test(timeout = 4000)
    public void testElements() {
        ObjectNode node = factory.objectNode();
        node.put("a", 1);
        node.put("b", 2);
        Iterator<JsonNode> it = node.elements();
        assertTrue(it.hasNext());
        assertEquals(1, it.next().intValue());
        assertTrue(it.hasNext());
        assertEquals(2, it.next().intValue());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testGetByIndex() {
        ObjectNode node = factory.objectNode();
        assertNull(node.get(0));
        assertNull(node.get(-1));
    }

    @Test(timeout = 4000)
    public void testGetByFieldName() {
        ObjectNode node = factory.objectNode();
        node.put("key", "value");
        assertEquals("value", node.get("key").asText());
        assertNull(node.get("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testFieldNames() {
        ObjectNode node = factory.objectNode();
        node.put("x", 1);
        node.put("y", 2);
        Iterator<String> it = node.fieldNames();
        Set<String> names = new HashSet<>();
        while (it.hasNext()) names.add(it.next());
        assertEquals(new HashSet<>(Arrays.asList("x", "y")), names);
    }

    @Test(timeout = 4000)
    public void testPathByIndex() {
        ObjectNode node = factory.objectNode();
        assertTrue(node.path(0) instanceof MissingNode);
        assertTrue(node.path(-1) instanceof MissingNode);
    }

    @Test(timeout = 4000)
    public void testPathByFieldName() {
        ObjectNode node = factory.objectNode();
        node.put("present", "here");
        assertEquals("here", node.path("present").asText());
        assertTrue(node.path("absent") instanceof MissingNode);
    }

    @Test(timeout = 4000)
    public void testFields() {
        ObjectNode node = factory.objectNode();
        node.put("a", 1);
        node.put("b", 2);
        Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        Map<String, JsonNode> map = new HashMap<>();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> e = it.next();
            map.put(e.getKey(), e.getValue());
        }
        assertEquals(2, map.size());
        assertEquals(1, map.get("a").intValue());
        assertEquals(2, map.get("b").intValue());
    }

    @Test(timeout = 4000)
    public void testWithExistingObjectNode() {
        ObjectNode node = factory.objectNode();
        ObjectNode child = node.putObject("child");
        assertSame(child, node.with("child"));
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testWithNonObjectNode() {
        ObjectNode node = factory.objectNode();
        node.put("child", 123);
        node.with("child");
    }

    @Test(timeout = 4000)
    public void testWithNewProperty() {
        ObjectNode node = factory.objectNode();
        ObjectNode created = node.with("newProp");
        assertNotNull(created);
        assertTrue(node.get("newProp") instanceof ObjectNode);
        assertSame(created, node.get("newProp"));
    }

    @Test(timeout = 4000)
    public void testWithArrayExisting() {
        ObjectNode node = factory.objectNode();
        ArrayNode arr = node.putArray("arr");
        assertSame(arr, node.withArray("arr"));
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testWithArrayNonArray() {
        ObjectNode node = factory.objectNode();
        node.put("arr", "notarray");
        node.withArray("arr");
    }

    @Test(timeout = 4000)
    public void testWithArrayNew() {
        ObjectNode node = factory.objectNode();
        ArrayNode created = node.withArray("newArr");
        assertNotNull(created);
        assertTrue(node.get("newArr") instanceof ArrayNode);
    }

    @Test(timeout = 4000)
    public void testFindValueFoundDirect() {
        ObjectNode node = factory.objectNode();
        node.put("target", "found");
        assertEquals("found", node.findValue("target").asText());
    }

    @Test(timeout = 4000)
    public void testFindValueFoundNested() {
        ObjectNode node = factory.objectNode();
        ObjectNode child = node.putObject("child");
        child.put("target", "nested");
        assertEquals("nested", node.findValue("target").asText());
    }

    @Test(timeout = 4000)
    public void testFindValueNotFound() {
        ObjectNode node = factory.objectNode();
        assertNull(node.findValue("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testFindValuesWithNullList() {
        ObjectNode node = factory.objectNode();
        node.put("a", 1);
        List<JsonNode> result = node.findValues("a", null);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).intValue());
    }

    @Test(timeout = 4000)
    public void testFindValuesWithExistingList() {
        ObjectNode node = factory.objectNode();
        node.put("a", 1);
        List<JsonNode> list = new ArrayList<>();
        List<JsonNode> result = node.findValues("a", list);
        assertSame(list, result);
        assertEquals(1, list.size());
    }

    @Test(timeout = 4000)
    public void testFindValuesNested() {
        ObjectNode node = factory.objectNode();
        ObjectNode child = node.putObject("child");
        child.put("a", 2);
        List<JsonNode> result = node.findValues("a", null);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).intValue());
    }

    @Test(timeout = 4000)
    public void testFindValuesAsText() {
        ObjectNode node = factory.objectNode();
        node.put("a", 123);
        List<String> result = node.findValuesAsText("a", null);
        assertEquals(1, result.size());
        assertEquals("123", result.get(0));
    }

    @Test(timeout = 4000)
    public void testFindParentDirect() {
        ObjectNode node = factory.objectNode();
        node.put("x", 1);
        assertSame(node, node.findParent("x"));
    }

    @Test(timeout = 4000)
    public void testFindParentNested() {
        ObjectNode node = factory.objectNode();
        ObjectNode child = node.putObject("child");
        child.put("x", 2);
        assertSame(child, node.findParent("x"));
    }

    @Test(timeout = 4000)
    public void testFindParentNotFound() {
        ObjectNode node = factory.objectNode();
        assertNull(node.findParent("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testFindParents() {
        ObjectNode node = factory.objectNode();
        node.put("x", 1);
        ObjectNode child = node.putObject("child");
        child.put("x", 2);
        List<JsonNode> result = node.findParents("x", null);
        assertEquals(2, result.size());
        assertTrue(result.contains(node));
        assertTrue(result.contains(child));
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testSetNullValue() {
        ObjectNode node = factory.objectNode();
        node.set("key", null);
        assertTrue(node.get("key") instanceof NullNode);
    }

    @Test(timeout = 4000)
    public void testSetAllMapWithNullValues() {
        ObjectNode node = factory.objectNode();
        Map<String, JsonNode> map = new HashMap<>();
        map.put("a", null);
        map.put("b", factory.textNode("text"));
        node.setAll(map);
        assertTrue(node.get("a") instanceof NullNode);
        assertEquals("text", node.get("b").asText());
    }

    @Test(timeout = 4000)
    public void testSetAllObjectNode() {
        ObjectNode node = factory.objectNode();
        ObjectNode other = factory.objectNode();
        other.put("x", 1);
        other.put("y", 2);
        node.setAll(other);
        assertEquals(2, node.size());
        assertEquals(1, node.get("x").intValue());
        assertEquals(2, node.get("y").intValue());
    }

    @Test(timeout = 4000)
    public void testReplaceNullValue() {
        ObjectNode node = factory.objectNode();
        node.put("key", "old");
        JsonNode old = node.replace("key", null);
        assertEquals("old", old.asText());
        assertTrue(node.get("key") instanceof NullNode);
    }

    @Test(timeout = 4000)
    public void testReplaceNonExistent() {
        ObjectNode node = factory.objectNode();
        JsonNode old = node.replace("newKey", factory.textNode("val"));
        assertNull(old);
        assertEquals("val", node.get("newKey").asText());
    }

    @Test(timeout = 4000)
    public void testWithoutFieldName() {
        ObjectNode node = factory.objectNode();
        node.put("a", 1);
        node.put("b", 2);
        node.without("a");
        assertEquals(1, node.size());
        assertNull(node.get("a"));
    }

    @Test(timeout = 4000)
    public void testWithoutCollection() {
        ObjectNode node = factory.objectNode();
        node.put("a", 1);
        node.put("b", 2);
        node.put("c", 3);
        node.without(Arrays.asList("a", "c"));
        assertEquals(1, node.size());
        assertNotNull(node.get("b"));
    }

    @Test(timeout = 4000)
    public void testRemoveField() {
        ObjectNode node = factory.objectNode();
        node.put("a", 1);
        assertEquals(1, node.remove("a").intValue());
        assertNull(node.get("a"));
    }

    @Test(timeout = 4000)
    public void testRemoveCollection() {
        ObjectNode node = factory.objectNode();
        node.put("a", 1);
        node.put("b", 2);
        node.remove(Arrays.asList("a"));
        assertEquals(1, node.size());
        assertNotNull(node.get("b"));
    }

    @Test(timeout = 4000)
    public void testRemoveAll() {
        ObjectNode node = factory.objectNode();
        node.put("a", 1);
        node.put("b", 2);
        node.removeAll();
        assertEquals(0, node.size());
    }

    @Test(timeout = 4000)
    public void testRetainCollection() {
        ObjectNode node = factory.objectNode();
        node.put("a", 1);
        node.put("b", 2);
        node.put("c", 3);
        node.retain(Arrays.asList("a", "c"));
        assertEquals(2, node.size());
        assertNotNull(node.get("a"));
        assertNotNull(node.get("c"));
        assertNull(node.get("b"));
    }

    @Test(timeout = 4000)
    public void testRetainVarargs() {
        ObjectNode node = factory.objectNode();
        node.put("x", 1);
        node.put("y", 2);
        node.put("z", 3);
        node.retain("x", "z");
        assertEquals(2, node.size());
        assertNotNull(node.get("x"));
        assertNotNull(node.get("z"));
        assertNull(node.get("y"));
    }

    @Test(timeout = 4000)
    public void testPutArray() {
        ObjectNode node = factory.objectNode();
        ArrayNode arr = node.putArray("arr");
        assertNotNull(arr);
        assertTrue(node.get("arr") instanceof ArrayNode);
    }

    @Test(timeout = 4000)
    public void testPutObject() {
        ObjectNode node = factory.objectNode();
        ObjectNode child = node.putObject("obj");
        assertNotNull(child);
        assertTrue(node.get("obj") instanceof ObjectNode);
    }

    @Test(timeout = 4000)
    public void testPutPOJO() {
        ObjectNode node = factory.objectNode();
        Object pojo = new Object();
        node.putPOJO("pojo", pojo);
        assertNotNull(node.get("pojo"));
    }

    @Test(timeout = 4000)
    public void testPutNull() {
        ObjectNode node = factory.objectNode();
        node.putNull("nullField");
        assertTrue(node.get("nullField") instanceof NullNode);
    }

    @Test(timeout = 4000)
    public void testPutShort() {
        ObjectNode node = factory.objectNode();
        node.put("s", (short) 42);
        assertEquals(42, node.get("s").shortValue());
    }

    @Test(timeout = 4000)
    public void testPutShortBoxedNull() {
        ObjectNode node = factory.objectNode();
        node.put("s", (Short) null);
        assertTrue(node.get("s") instanceof NullNode);
    }

    @Test(timeout = 4000)
    public void testPutInt() {
        ObjectNode node = factory.objectNode();
        node.put("i", 123);
        assertEquals(123, node.get("i").intValue());
    }

    @Test(timeout = 4000)
    public void testPutIntegerNull() {
        ObjectNode node = factory.objectNode();
        node.put("i", (Integer) null);
        assertTrue(node.get("i") instanceof NullNode);
    }

    @Test(timeout = 4000)
    public void testPutLong() {
        ObjectNode node = factory.objectNode();
        node.put("l", 10000000000L);
        assertEquals(10000000000L, node.get("l").longValue());
    }

    @Test(timeout = 4000)
    public void testPutLongBoxedNull() {
        ObjectNode node = factory.objectNode();
        node.put("l", (Long) null);
        assertTrue(node.get("l") instanceof NullNode);
    }

    @Test(timeout = 4000)
    public void testPutFloat() {
        ObjectNode node = factory.objectNode();
        node.put("f", 3.14f);
        assertEquals(3.14f, node.get("f").floatValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testPutFloatBoxedNull() {
        ObjectNode node = factory.objectNode();
        node.put("f", (Float) null);
        assertTrue(node.get("f") instanceof NullNode);
    }

    @Test(timeout = 4000)
    public void testPutDouble() {
        ObjectNode node = factory.objectNode();
        node.put("d", 2.718);
        assertEquals(2.718, node.get("d").doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testPutDoubleBoxedNull() {
        ObjectNode node = factory.objectNode();
        node.put("d", (Double) null);
        assertTrue(node.get("d") instanceof NullNode);
    }

    @Test(timeout = 4000)
    public void testPutBigDecimal() {
        ObjectNode node = factory.objectNode();
        node.put("bd", new BigDecimal("123.456"));
        assertEquals(new BigDecimal("123.456"), node.get("bd").decimalValue());
    }

    @Test(timeout = 4000)
    public void testPutBigDecimalNull() {
        ObjectNode node = factory.objectNode();
        node.put("bd", (BigDecimal) null);
        assertTrue(node.get("bd") instanceof NullNode);
    }

    @Test(timeout = 4000)
    public void testPutString() {
        ObjectNode node = factory.objectNode();
        node.put("str", "hello");
        assertEquals("hello", node.get("str").asText());
    }

    @Test(timeout = 4000)
    public void testPutStringNull() {
        ObjectNode node = factory.objectNode();
        node.put("str", (String) null);
        assertTrue(node.get("str") instanceof NullNode);
    }

    @Test(timeout = 4000)
    public void testPutBoolean() {
        ObjectNode node = factory.objectNode();
        node.put("b", true);
        assertTrue(node.get("b").booleanValue());
    }

    @Test(timeout = 4000)
    public void testPutBooleanBoxedNull() {
        ObjectNode node = factory.objectNode();
        node.put("b", (Boolean) null);
        assertTrue(node.get("b") instanceof NullNode);
    }

    @Test(timeout = 4000)
    public void testPutBinary() {
        ObjectNode node = factory.objectNode();
        byte[] data = {1, 2, 3};
        node.put("bin", data);
        assertArrayEquals(data, node.get("bin").binaryValue());
    }

    @Test(timeout = 4000)
    public void testPutBinaryNull() {
        ObjectNode node = factory.objectNode();
        node.put("bin", (byte[]) null);
        assertTrue(node.get("bin") instanceof NullNode);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testNamingWithObjectNode() throws IOException {
        // This test reproduces the known defect: conflicting setter definitions for property "all"
        // due to two setAll methods in ObjectNode.
        // Using @JsonUnwrapped on an ObjectNode property triggers introspection of ObjectNode as a bean,
        // and when a JSON property named "all" is encountered, Jackson finds two setAll methods (Map and ObjectNode)
        // causing a JsonMappingException.
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"all\":\"value\"}";
        // MyBean has an @JsonUnwrapped ObjectNode property
        mapper.readValue(json, MyBean.class);
    }

    // Helper class for defect test
    static class MyBean {
        public com.fasterxml.jackson.annotation.JsonUnwrapped
        public ObjectNode props;
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testWithOnNonObjectNode() {
        ObjectNode node = factory.objectNode();
        node.put("prop", 123);
        node.with("prop");
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testWithArrayOnNonArrayNode() {
        ObjectNode node = factory.objectNode();
        node.put("prop", "string");
        node.withArray("prop");
    }

    @Test(timeout = 4000)
    public void testSerializeWithNullChild() throws IOException {
        // Ensure serialization handles null children (should not happen normally)
        ObjectNode node = factory.objectNode();
        node.set("nullChild", null); // becomes NullNode
        // Use a custom JsonGenerator that records output
        // We'll just use ObjectMapper to serialize to string
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(node);
        assertTrue(json.contains("\"nullChild\":null"));
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        ObjectNode node = factory.objectNode();
        assertTrue(node.equals(node));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        ObjectNode node = factory.objectNode();
        assertFalse(node.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        ObjectNode node = factory.objectNode();
        assertFalse(node.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsEqualNodes() {
        ObjectNode node1 = factory.objectNode();
        node1.put("a", 1);
        node1.put("b", "two");
        ObjectNode node2 = factory.objectNode();
        node2.put("a", 1);
        node2.put("b", "two");
        assertTrue(node1.equals(node2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentValues() {
        ObjectNode node1 = factory.objectNode();
        node1.put("a", 1);
        ObjectNode node2 = factory.objectNode();
        node2.put("a", 2);
        assertFalse(node1.equals(node2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentKeys() {
        ObjectNode node1 = factory.objectNode();
        node1.put("a", 1);
        ObjectNode node2 = factory.objectNode();
        node2.put("b", 1);
        assertFalse(node1.equals(node2));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        ObjectNode node = factory.objectNode();
        node.put("x", 1);
        int hash1 = node.hashCode();
        int hash2 = node.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testHashCodeEqualNodes() {
        ObjectNode node1 = factory.objectNode();
        node1.put("a", 1);
        ObjectNode node2 = factory.objectNode();
        node2.put("a", 1);
        assertEquals(node1.hashCode(), node2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToStringEmpty() {
        ObjectNode node = factory.objectNode();
        assertEquals("{}", node.toString());
    }

    @Test(timeout = 4000)
    public void testToStringSingle() {
        ObjectNode node = factory.objectNode();
        node.put("key", "value");
        assertEquals("{\"key\":\"value\"}", node.toString());
    }

    @Test(timeout = 4000)
    public void testToStringMultiple() {
        ObjectNode node = factory.objectNode();
        node.put("a", 1);
        node.put("b", "two");
        String s = node.toString();
        assertTrue(s.startsWith("{"));
        assertTrue(s.endsWith("}"));
        assertTrue(s.contains("\"a\":1"));
        assertTrue(s.contains("\"b\":\"two\""));
    }

    @Test(timeout = 4000)
    public void testDeepCopyEquality() {
        ObjectNode node = factory.objectNode();
        node.put("a", 1);
        node.putObject("obj").put("inner", "value");
        ObjectNode copy = node.deepCopy();
        assertEquals(node, copy);
        // Modify original
        node.put("a", 2);
        assertNotEquals(node, copy);
    }

    @Test(timeout = 4000)
    public void testPutAllDeprecated() {
        // Test deprecated putAll methods still work
        ObjectNode node = factory.objectNode();
        Map<String, JsonNode> map = new HashMap<>();
        map.put("x", factory.textNode("y"));
        node.putAll(map);
        assertEquals("y", node.get("x").asText());
    }

    @Test(timeout = 4000)
    public void testPutAllObjectNodeDeprecated() {
        ObjectNode node = factory.objectNode();
        ObjectNode other = factory.objectNode();
        other.put("a", 1);
        node.putAll(other);
        assertEquals(1, node.get("a").intValue());
    }
}