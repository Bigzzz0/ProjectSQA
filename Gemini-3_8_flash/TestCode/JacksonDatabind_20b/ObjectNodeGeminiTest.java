package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Defect Target (Defects4J - TestNamingStrategyStd::testNamingWithObjectNode):
 *    - Root Cause: ObjectNode defines overloaded single-argument mutators:
 *      setAll(Map<String,? extends JsonNode>) and setAll(ObjectNode).
 *      When an ObjectMapper uses a PropertyNamingStrategy (e.g. CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES
 *      or PASCAL_CASE_TO_CAMEL_CASE), POJO setter introspection misinterprets both methods as conflicting
 *      setters for property "all", raising:
 *      JsonMappingException: Conflicting setter definitions for property "all".
 *    - Target Tests: testNamingStrategyWithObjectNodeDefect(), testNamingStrategyPascalCaseDefect().
 *
 * 2. Decision Branches Covered:
 *    - _at(JsonPointer): Matching property retrieval.
 *    - deepCopy(): Empty node, single/nested node preservation, immutability check.
 *    - size(), elements(), fields(), fieldNames(): Iterators across populated and empty states.
 *    - path(int) vs path(String): MissingNode handling vs populated node hit.
 *    - get(int) vs get(String): Null return on index vs Map lookup.
 *    - with(String) & withArray(String):
 *      * Absent property -> Instantiation of child ObjectNode / ArrayNode.
 *      * Existing matching property -> Direct cast return.
 *      * Existing non-matching property -> UnsupportedOperationException branch.
 *    - findValue, findValues, findValuesAsText, findParent, findParents:
 *      * Direct child match vs Deep recursive child match vs No match.
 *      * Accumulator parameter (foundSoFar == null vs foundSoFar != null).
 *      * Child iteration order and exclusion of children once parent matched.
 *    - Mutators (set, replace, put, putAll, setAll, without, remove, retain, removeAll):
 *      * null value conversions to NullNode.
 *      * Map and ObjectNode batch copies.
 *      * Collection-based and varargs field removal/retention.
 *    - Overloaded put primitives & wrappers:
 *      * short, int, long, float, double, boolean, BigDecimal, String, byte[], POJO.
 *      * Boxed primitives with null values (conversion to NullNode).
 *    - Serialization:
 *      * serialize(JsonGenerator, SerializerProvider) with BaseJsonNode cast traversal.
 *      * serializeWithType(JsonGenerator, SerializerProvider, TypeSerializer) with prefix/suffix.
 *    - Object Contracts:
 *      * equals(this), equals(null), equals(non-ObjectNode), equals(equivalent), equals(different).
 *      * hashCode() equality and contract compliance.
 *      * toString() empty vs single-item vs multi-item comma formatting.
 */
public class ObjectNodeGeminiTest {

    private final JsonNodeFactory nf = JsonNodeFactory.instance;

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testNodeStateAndTypeTokens() {
        ObjectNode node = new ObjectNode(nf);
        assertEquals(JsonNodeType.OBJECT, node.getNodeType());
        assertEquals(JsonToken.START_OBJECT, node.asToken());
        assertTrue(node.isContainerNode());
        assertFalse(node.isArray());
        assertTrue(node.isObject());
        assertEquals(0, node.size());
        assertNull(node.get(0));
        assertTrue(node.path(0).isMissingNode());
    }

    @Test(timeout = 4000)
    public void testDeepCopyIntegrity() {
        ObjectNode original = new ObjectNode(nf);
        original.put("str", "value");
        ObjectNode child = original.putObject("child");
        child.put("num", 42);

        ObjectNode copy = original.deepCopy();
        assertNotSame(original, copy);
        assertNotSame(original.get("child"), copy.get("child"));
        assertEquals(original, copy);

        // Mutate copy and verify original remains unchanged
        copy.put("str", "modified");
        ((ObjectNode) copy.get("child")).put("num", 99);
        assertEquals("value", original.get("str").asText());
        assertEquals(42, original.get("child").get("num").asInt());
    }

    @Test(timeout = 4000)
    public void testJsonPointerNavigation() {
        ObjectNode node = new ObjectNode(nf);
        node.put("target", "foundMe");
        JsonPointer ptr = JsonPointer.compile("/target");
        JsonNode result = node._at(ptr);
        assertNotNull(result);
        assertEquals("foundMe", result.asText());

        JsonPointer missingPtr = JsonPointer.compile("/missing");
        assertNull(node._at(missingPtr));
    }

    @Test(timeout = 4000)
    public void testIteratorsAndFields() {
        ObjectNode node = new ObjectNode(nf);
        node.put("k1", "v1").put("k2", "v2");

        // Elements iterator
        Iterator<JsonNode> elemIt = node.elements();
        assertTrue(elemIt.hasNext());
        assertEquals("v1", elemIt.next().asText());
        assertEquals("v2", elemIt.next().asText());
        assertFalse(elemIt.hasNext());

        // Field names iterator
        Iterator<String> nameIt = node.fieldNames();
        assertTrue(nameIt.hasNext());
        assertEquals("k1", nameIt.next());
        assertEquals("k2", nameIt.next());
        assertFalse(nameIt.hasNext());

        // Fields (entries) iterator
        Iterator<Map.Entry<String, JsonNode>> fieldsIt = node.fields();
        assertTrue(fieldsIt.hasNext());
        Map.Entry<String, JsonNode> e1 = fieldsIt.next();
        assertEquals("k1", e1.getKey());
        assertEquals("v1", e1.getValue().asText());
        Map.Entry<String, JsonNode> e2 = fieldsIt.next();
        assertEquals("k2", e2.getKey());
        assertEquals("v2", e2.getValue().asText());
        assertFalse(fieldsIt.hasNext());
    }

    @Test(timeout = 4000)
    public void testWithAndWithArraySuccessTransitions() {
        ObjectNode node = new ObjectNode(nf);

        // with on non-existing creates ObjectNode
        ObjectNode childObj = node.with("subObj");
        assertNotNull(childObj);
        assertSame(childObj, node.get("subObj"));
        // with on existing ObjectNode returns it
        assertSame(childObj, node.with("subObj"));

        // withArray on non-existing creates ArrayNode
        ArrayNode childArr = node.withArray("subArr");
        assertNotNull(childArr);
        assertSame(childArr, node.get("subArr"));
        // withArray on existing ArrayNode returns it
        assertSame(childArr, node.withArray("subArr"));
    }

    @Test(timeout = 4000)
    public void testFindValueAndFindParentDeepTraversal() {
        ObjectNode root = new ObjectNode(nf);
        ObjectNode branch = root.putObject("branch");
        branch.put("leaf", "leafValue");
        branch.put("common", "branchCommon");
        root.put("common", "rootCommon");

        // findValue: root direct match takes precedence
        assertEquals("rootCommon", root.findValue("common").asText());
        // findValue: deep match
        assertEquals("leafValue", root.findValue("leaf").asText());
        // findValue: not found
        assertNull(root.findValue("absent"));

        // findParent: direct child parent is root
        assertSame(root, root.findParent("common"));
        // findParent: nested child parent is branch
        assertSame(branch, root.findParent("leaf"));
        // findParent: not found
        assertNull(root.findParent("absent"));
    }

    @Test(timeout = 4000)
    public void testFindValuesAndFindParentsAccumulators() {
        ObjectNode root = new ObjectNode(nf);
        root.put("key", "val1");
        ObjectNode child = root.putObject("child");
        child.put("other", "val2");
        ObjectNode grandChild = child.putObject("grandChild");
        grandChild.put("other", "val3");

        // findValues with null vs existing list
        List<JsonNode> valuesNull = root.findValues("other", null);
        assertNotNull(valuesNull);
        assertEquals(2, valuesNull.size());

        List<JsonNode> preAllocatedValues = new ArrayList<JsonNode>();
        List<JsonNode> valuesPopulated = root.findValues("other", preAllocatedValues);
        assertSame(preAllocatedValues, valuesPopulated);
        assertEquals(2, valuesPopulated.size());

        // findValuesAsText with null vs existing list
        List<String> textNull = root.findValuesAsText("other", null);
        assertNotNull(textNull);
        assertEquals(2, textNull.size());
        assertEquals("val2", textNull.get(0));
        assertEquals("val3", textNull.get(1));

        List<String> preAllocatedText = new ArrayList<String>();
        List<String> textPopulated = root.findValuesAsText("other", preAllocatedText);
        assertSame(preAllocatedText, textPopulated);
        assertEquals(2, textPopulated.size());

        // findParents with null vs existing list
        List<JsonNode> parentsNull = root.findParents("other", null);
        assertNotNull(parentsNull);
        assertEquals(2, parentsNull.size());
        assertSame(child, parentsNull.get(0));
        assertSame(grandChild, parentsNull.get(1));

        List<JsonNode> preAllocatedParents = new ArrayList<JsonNode>();
        List<JsonNode> parentsPopulated = root.findParents("other", preAllocatedParents);
        assertSame(preAllocatedParents, parentsPopulated);
        assertEquals(2, parentsPopulated.size());
    }

    @Test(timeout = 4000)
    public void testRetainAndWithoutOperations() {
        ObjectNode node = new ObjectNode(nf);
        node.put("a", 1).put("b", 2).put("c", 3).put("d", 4);

        // without single
        assertSame(node, node.without("a"));
        assertNull(node.get("a"));

        // without collection
        assertSame(node, node.without(Arrays.asList("b", "missing")));
        assertNull(node.get("b"));
        assertEquals(2, node.size());

        // retain varargs
        assertSame(node, node.retain("c", "nonexistent"));
        assertEquals(1, node.size());
        assertTrue(node.has("c"));
        assertFalse(node.has("d"));

        // retain collection
        node.put("e", 5);
        assertSame(node, node.retain(Collections.singletonList("e")));
        assertEquals(1, node.size());
        assertTrue(node.has("e"));
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Extremes
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testNullArgumentConversionsToNullNode() {
        ObjectNode node = new ObjectNode(nf);

        // Null value setters must store NullNode instance
        node.set("n1", null);
        assertTrue(node.get("n1").isNull());

        node.replace("n2", null);
        assertTrue(node.get("n2").isNull());

        node.put("n3", (JsonNode) null);
        assertTrue(node.get("n3").isNull());

        node.putNull("n4");
        assertTrue(node.get("n4").isNull());

        // Boxed primitive puts with null values
        node.put("nShort", (Short) null);
        node.put("nInt", (Integer) null);
        node.put("nLong", (Long) null);
        node.put("nFloat", (Float) null);
        node.put("nDouble", (Double) null);
        node.put("nBigDecimal", (BigDecimal) null);
        node.put("nString", (String) null);
        node.put("nBoolean", (Boolean) null);
        node.put("nBytes", (byte[]) null);

        assertTrue(node.get("nShort").isNull());
        assertTrue(node.get("nInt").isNull());
        assertTrue(node.get("nLong").isNull());
        assertTrue(node.get("nFloat").isNull());
        assertTrue(node.get("nDouble").isNull());
        assertTrue(node.get("nBigDecimal").isNull());
        assertTrue(node.get("nString").isNull());
        assertTrue(node.get("nBoolean").isNull());
        assertTrue(node.get("nBytes").isNull());
    }

    @Test(timeout = 4000)
    public void testNumericBoundaryValues() {
        ObjectNode node = new ObjectNode(nf);

        node.put("shortMin", Short.MIN_VALUE);
        node.put("shortMax", Short.MAX_VALUE);
        node.put("intMin", Integer.MIN_VALUE);
        node.put("intMax", Integer.MAX_VALUE);
        node.put("longMin", Long.MIN_VALUE);
        node.put("longMax", Long.MAX_VALUE);
        node.put("floatMin", Float.MIN_VALUE);
        node.put("floatMax", Float.MAX_VALUE);
        node.put("doubleMin", Double.MIN_VALUE);
        node.put("doubleMax", Double.MAX_VALUE);

        assertEquals(Short.MIN_VALUE, node.get("shortMin").shortValue());
        assertEquals(Short.MAX_VALUE, node.get("shortMax").shortValue());
        assertEquals(Integer.MIN_VALUE, node.get("intMin").intValue());
        assertEquals(Integer.MAX_VALUE, node.get("intMax").intValue());
        assertEquals(Long.MIN_VALUE, node.get("longMin").longValue());
        assertEquals(Long.MAX_VALUE, node.get("longMax").longValue());
        assertEquals(Float.MIN_VALUE, node.get("floatMin").floatValue(), 0.00001f);
        assertEquals(Float.MAX_VALUE, node.get("floatMax").floatValue(), 0.00001f);
        assertEquals(Double.MIN_VALUE, node.get("doubleMin").doubleValue(), 0.00001d);
        assertEquals(Double.MAX_VALUE, node.get("doubleMax").doubleValue(), 0.00001d);
    }

    @Test(timeout = 4000)
    public void testAllPrimitiveAndBoxedPutOverloads() {
        ObjectNode node = new ObjectNode(nf);

        node.put("s_prim", (short) 1);
        node.put("s_box", Short.valueOf((short) 2));
        node.put("i_prim", 10);
        node.put("i_box", Integer.valueOf(20));
        node.put("l_prim", 100L);
        node.put("l_box", Long.valueOf(200L));
        node.put("f_prim", 1.5f);
        node.put("f_box", Float.valueOf(2.5f));
        node.put("d_prim", 10.5d);
        node.put("d_box", Double.valueOf(20.5d));
        node.put("bd", new BigDecimal("123.456"));
        node.put("str", "testString");
        node.put("b_prim", true);
        node.put("b_box", Boolean.FALSE);
        node.put("bytes", new byte[]{1, 2, 3});
        node.putPOJO("pojo", "plainStringPOJO");

        assertEquals(1, node.get("s_prim").shortValue());
        assertEquals(2, node.get("s_box").shortValue());
        assertEquals(10, node.get("i_prim").intValue());
        assertEquals(20, node.get("i_box").intValue());
        assertEquals(100L, node.get("l_prim").longValue());
        assertEquals(200L, node.get("l_box").longValue());
        assertEquals(1.5f, node.get("f_prim").floatValue(), 0.001f);
        assertEquals(2.5f, node.get("f_box").floatValue(), 0.001f);
        assertEquals(10.5d, node.get("d_prim").doubleValue(), 0.001d);
        assertEquals(20.5d, node.get("d_box").doubleValue(), 0.001d);
        assertEquals(new BigDecimal("123.456"), node.get("bd").decimalValue());
        assertEquals("testString", node.get("str").textValue());
        assertTrue(node.get("b_prim").booleanValue());
        assertFalse(node.get("b_box").booleanValue());
        assertArrayEquals(new byte[]{1, 2, 3}, node.get("bytes").binaryValue());
        assertEquals("plainStringPOJO", ((POJONode) node.get("pojo")).getPojo());
    }

    @Test(timeout = 4000)
    public void testBatchSetAndPutOperationsWithNulls() {
        ObjectNode node = new ObjectNode(nf);
        Map<String, JsonNode> map = new HashMap<String, JsonNode>();
        map.put("valid", nf.textNode("ok"));
        map.put("nullEntry", null);

        // setAll(Map) handles null entries as NullNode
        node.setAll(map);
        assertEquals("ok", node.get("valid").textValue());
        assertTrue(node.get("nullEntry").isNull());

        // setAll(ObjectNode)
        ObjectNode other = new ObjectNode(nf);
        other.put("added", 999);
        node.setAll(other);
        assertEquals(999, node.get("added").intValue());

        // Deprecated putAll delegations
        ObjectNode deprecatedTest = new ObjectNode(nf);
        deprecatedTest.putAll(map);
        assertEquals(2, deprecatedTest.size());
        deprecatedTest.putAll(other);
        assertEquals(3, deprecatedTest.size());
    }

    @Test(timeout = 4000)
    public void testRemoveMutatorVariations() {
        ObjectNode node = new ObjectNode(nf);
        node.put("k1", 1).put("k2", 2).put("k3", 3);

        // remove single field
        JsonNode removed = node.remove("k1");
        assertNotNull(removed);
        assertEquals(1, removed.intValue());
        assertNull(node.remove("nonexistent"));

        // remove collection
        assertSame(node, node.remove(Arrays.asList("k2", "nonexistent")));
        assertFalse(node.has("k2"));
        assertEquals(1, node.size());

        // removeAll
        assertSame(node, node.removeAll());
        assertEquals(0, node.size());
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone
    /**********************************************************
     */

    /**
     * Targets Defects4J Ground Truth:
     * com.fasterxml.jackson.databind.introspect.TestNamingStrategyStd::testNamingWithObjectNode
     * Failure: Conflicting setter definitions for property "all":
     * com.fasterxml.jackson.databind.node.ObjectNode#setAll(1 params) vs ObjectNode#setAll(1 params)
     */
    @Test(timeout = 4000)
    public void testNamingStrategyWithObjectNodeDefect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        ObjectNode result = mapper.readValue("{\"foo_bar\":\"abc\"}", ObjectNode.class);
        assertNotNull("ObjectNode should successfully deserialize with snake_case naming strategy", result);
        assertEquals(1, result.size());
        assertEquals("abc", result.get("foo_bar").asText());
    }

    @Test(timeout = 4000)
    public void testNamingStrategyPascalCaseDefect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.PASCAL_CASE_TO_CAMEL_CASE);
        ObjectNode result = mapper.readValue("{\"FooBar\":\"xyz\"}", ObjectNode.class);
        assertNotNull("ObjectNode should successfully deserialize with PascalCase naming strategy", result);
        assertEquals(1, result.size());
        assertEquals("xyz", result.get("FooBar").asText());
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testWithThrowsWhenPropertyIsNotAnObjectNode() {
        ObjectNode node = new ObjectNode(nf);
        node.put("scalar", "stringValue");
        try {
            node.with("scalar");
            fail("Expected UnsupportedOperationException when 'with' encounters a non-ObjectNode child");
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage().contains("is not of type ObjectNode"));
        }
    }

    @Test(timeout = 4000)
    public void testWithArrayThrowsWhenPropertyIsNotAnArrayNode() {
        ObjectNode node = new ObjectNode(nf);
        node.put("scalar", 12345);
        try {
            node.withArray("scalar");
            fail("Expected UnsupportedOperationException when 'withArray' encounters a non-ArrayNode child");
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage().contains("is not of type ArrayNode"));
        }
    }

    @Test(timeout = 4000)
    public void testPathAndGetWithMissingProperties() {
        ObjectNode node = new ObjectNode(nf);
        assertNull(node.get("missing"));
        JsonNode pathNode = node.path("missing");
        assertNotNull(pathNode);
        assertTrue(pathNode.isMissingNode());
        assertSame(MissingNode.getInstance(), pathNode);

        // Int-indexed path on ObjectNode always returns MissingNode
        assertSame(MissingNode.getInstance(), node.path(10));
    }

    /*
    /**********************************************************
    /* Partition E: Object Lifecycle, Serialization & Contract Integrity
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContracts() {
        ObjectNode n1 = new ObjectNode(nf);
        ObjectNode n2 = new ObjectNode(nf);

        // Reflexive
        assertTrue(n1.equals(n1));

        // Symmetric empty
        assertTrue(n1.equals(n2));
        assertTrue(n2.equals(n1));
        assertEquals(n1.hashCode(), n2.hashCode());

        // Null and non-ObjectNode
        assertFalse(n1.equals(null));
        assertFalse(n1.equals("NotAnObjectNode"));

        // Content equality
        n1.put("k", "v");
        assertFalse(n1.equals(n2));
        n2.put("k", "v");
        assertTrue(n1.equals(n2));
        assertEquals(n1.hashCode(), n2.hashCode());

        // Sub-node difference
        n2.put("k", "different");
        assertFalse(n1.equals(n2));
        assertFalse(n1._childrenEqual(n2));
    }

    @Test(timeout = 4000)
    public void testToStringRepresentation() {
        ObjectNode node = new ObjectNode(nf);
        assertEquals("{}", node.toString());

        node.put("a", 1);
        assertEquals("{\"a\":1}", node.toString());

        node.put("b", "two");
        // Multi-entry checks comma branch logic (count > 0)
        assertEquals("{\"a\":1,\"b\":\"two\"}", node.toString());
    }

    @Test(timeout = 4000)
    public void testSerializationStandard() throws IOException {
        ObjectNode node = new ObjectNode(nf);
        node.put("name", "gemini").put("count", 3);

        TokenBuffer tb = new TokenBuffer(null, false);
        node.serialize(tb, null);
        tb.close();

        JsonNode deserialized = new ObjectMapper().readTree(tb.asParser());
        assertEquals(node, deserialized);
    }

    @Test(timeout = 4000)
    public void testSerializationWithType() throws IOException {
        ObjectNode node = new ObjectNode(nf);
        node.put("id", 101);

        TokenBuffer tb = new TokenBuffer(null, false);
        TypeSerializer dummyTypeSer = new TypeSerializer() {
            @Override
            public TypeSerializer forProperty(BeanProperty prop) { return this; }
            @Override
            public JsonTypeInfo.As getTypeInclusion() { return JsonTypeInfo.As.WRAPPER_OBJECT; }
            @Override
            public String getPropertyName() { return "@type"; }
            @Override
            public TypeIdResolver getTypeIdResolver() { return null; }
            @Override
            public void writeTypePrefixForObject(Object value, JsonGenerator jgen) throws IOException {
                jgen.writeStartObject();
                jgen.writeStringField("@type", "Custom