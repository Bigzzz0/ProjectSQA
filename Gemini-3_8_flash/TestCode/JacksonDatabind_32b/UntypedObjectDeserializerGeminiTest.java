package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * Targeted Class: com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer (and .Vanilla)
 *
 * Defect Target (from Defects4J databind #989):
 * - Description: "Can not deserialize instance of java.lang.Object out of END_OBJECT token"
 * - Root Cause: Parser already positioned at JsonTokenId.ID_END_OBJECT for an empty JSON object
 *   was unhandled in deserialize() switches of UntypedObjectDeserializer and Vanilla, falling
 *   through to default and throwing JsonMappingException.
 * - Targeted Tests: testDefect989DeserializeWhenParserAtEndObject,
 *                   testDefect989VanillaDeserializeWhenParserAtEndObject,
 *                   testNestedUntyped989Defects4J
 *
 * Logical Partitions:
 * Partition A: Core Functional Logic & State Transitions
 * - Standard primitive mappings: String, Boolean (true/false), Integer, Long, Double, Null.
 * - Array mappings: 0 elements, 1 element, 2 elements, 3+ elements.
 * - Object mappings: 0 entries, 1 entry, 2 entries, 3+ entries.
 * - ObjectBuffer chunk boundary overflow (> 20 elements) for mapArray and mapArrayToArray.
 *
 * Partition B: Boundary Value Analysis (BVA) & Config Features
 * - DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY: empty (NO_OBJECTS), 1-item, multi-item chunked.
 * - DeserializationFeature.USE_BIG_INTEGER_FOR_INTS: coercion of integral numbers.
 * - DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS: coercion of floating numbers.
 * - POJONode / ID_EMBEDDED_OBJECT handling.
 *
 * Partition C: Defect-Targeted Branch Zone (Issue #989)
 * - Handling of parser positioned on END_OBJECT directly or via nested deserialization.
 *
 * Partition D: Exception & Defensive Guard Paths
 * - Unexpected tokens for UntypedObjectDeserializer and Vanilla deserialize() (e.g., ID_END_ARRAY).
 * - mapObject() invoked with unexpected token (e.g. NUMBER_INT).
 * - deserializeWithType() unexpected tokens.
 *
 * Partition E: Object Lifecycle & Contract Integrity & Custom Deserializer Overrides
 * - Constructors: default, (JavaType, JavaType), base copy constructor, and _withResolved().
 * - isCachable() contract.
 * - resolve() logic: std vs custom List/Map JavaType overrides, _clearIfStdImpl, secondary contextualization.
 * - createContextual(): Vanilla substitution vs self-return when customized or subclassed.
 * - Custom overrides for Map, List, String, and Number in deserialize and deserializeWithType.
 */
public class UntypedObjectDeserializerGeminiTest {

    // =========================================================================
    // Test Helpers and Mock-Free Stubs
    // =========================================================================

    private DefaultDeserializationContext createDeserializationContext(ObjectMapper mapper, JsonParser p) {
        return ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), p, mapper.getInjectableValues());
    }

    private static class SubUntypedDeserializer extends UntypedObjectDeserializer {
        private static final long serialVersionUID = 1L;

        public SubUntypedDeserializer() {
            super(null, null);
        }

        @Override
        public Object mapObject(JsonParser p, DeserializationContext ctxt) throws IOException {
            return super.mapObject(p, ctxt);
        }

        @Override
        public Object mapArray(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return super.mapArray(jp, ctxt);
        }

        @Override
        public Object[] mapArrayToArray(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return super.mapArrayToArray(jp, ctxt);
        }

        @Override
        public JsonDeserializer<?> _withResolved(JsonDeserializer<?> mapDeser, JsonDeserializer<?> listDeser,
                                                JsonDeserializer<?> stringDeser, JsonDeserializer<?> numberDeser) {
            return super._withResolved(mapDeser, listDeser, stringDeser, numberDeser);
        }
    }

    private static class CustomStringDeser extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return "CUSTOM_STRING:" + p.getText();
        }
    }

    private static class CustomNumberDeser extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return 88888;
        }
    }

    private static class CustomMapDeser extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("custom_map", Boolean.TRUE);
            return map;
        }
    }

    private static class CustomListDeser extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            List<Object> list = new ArrayList<Object>();
            list.add("custom_list");
            return list;
        }
    }

    private static final TypeDeserializer DUMMY_TYPE_DESERIALIZER = new TypeDeserializer() {
        @Override
        public TypeDeserializer forProperty(BeanProperty prop) { return this; }
        @Override
        public JsonTypeInfo.As getTypeInclusionType() { return JsonTypeInfo.As.PROPERTY; }
        @Override
        public String getPropertyName() { return "@type"; }
        @Override
        public TypeIdResolver getTypeIdResolver() { return null; }
        @Override
        public Class<?> getDefaultImpl() { return Object.class; }
        @Override
        public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) { return "TYPED_OBJECT"; }
        @Override
        public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) { return "TYPED_ARRAY"; }
        @Override
        public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) { return "TYPED_SCALAR"; }
        @Override
        public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) { return "TYPED_ANY"; }
    };

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testVanillaScalars() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        assertEquals("hello", mapper.readValue("\"hello\"", Object.class));
        assertEquals(Boolean.TRUE, mapper.readValue("true", Object.class));
        assertEquals(Boolean.FALSE, mapper.readValue("false", Object.class));
        assertNull(mapper.readValue("null", Object.class));
        assertEquals(123, mapper.readValue("123", Object.class));
        assertEquals(12.34, ((Double) mapper.readValue("12.34", Object.class)).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testVanillaArraySizes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // 0 elements
        Object empty = mapper.readValue("[]", Object.class);
        assertTrue(empty instanceof List);
        assertEquals(0, ((List<?>) empty).size());

        // 1 element
        Object one = mapper.readValue("[\"a\"]", Object.class);
        assertTrue(one instanceof List);
        assertEquals(1, ((List<?>) one).size());
        assertEquals("a", ((List<?>) one).get(0));

        // 2 elements
        Object two = mapper.readValue("[\"a\", \"b\"]", Object.class);
        assertTrue(two instanceof List);
        assertEquals(2, ((List<?>) two).size());
        assertEquals("b", ((List<?>) two).get(1));

        // 3 elements
        Object three = mapper.readValue("[\"a\", \"b\", \"c\"]", Object.class);
        assertTrue(three instanceof List);
        assertEquals(3, ((List<?>) three).size());

        // Chunk boundary overflow (> 20 items to trigger ObjectBuffer chunk rollover)
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 30; i++) {
            if (i > 0) sb.append(",");
            sb.append(i);
        }
        sb.append("]");
        Object large = mapper.readValue(sb.toString(), Object.class);
        assertTrue(large instanceof List);
        List<?> largeList = (List<?>) large;
        assertEquals(30, largeList.size());
        assertEquals(0, largeList.get(0));
        assertEquals(29, largeList.get(29));
    }

    @Test(timeout = 4000)
    public void testVanillaObjectSizes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // 0 entries
        Object empty = mapper.readValue("{}", Object.class);
        assertTrue(empty instanceof Map);
        assertEquals(0, ((Map<?, ?>) empty).size());

        // 1 entry
        Object one = mapper.readValue("{\"k1\": \"v1\"}", Object.class);
        assertTrue(one instanceof Map);
        assertEquals(1, ((Map<?, ?>) one).size());
        assertEquals("v1", ((Map<?, ?>) one).get("k1"));

        // 2 entries
        Object two = mapper.readValue("{\"k1\": \"v1\", \"k2\": \"v2\"}", Object.class);
        assertTrue(two instanceof Map);
        assertEquals(2, ((Map<?, ?>) two).size());
        assertEquals("v2", ((Map<?, ?>) two).get("k2"));

        // 3 entries
        Object three = mapper.readValue("{\"k1\": \"v1\", \"k2\": \"v2\", \"k3\": \"v3\"}", Object.class);
        assertTrue(three instanceof Map);
        assertEquals(3, ((Map<?, ?>) three).size());
        assertEquals("v3", ((Map<?, ?>) three).get("k3"));

        // Many entries
        Object many = mapper.readValue("{\"k1\": 1, \"k2\": 2, \"k3\": 3, \"k4\": 4, \"k5\": 5}", Object.class);
        assertTrue(many instanceof Map);
        assertEquals(5, ((Map<?, ?>) many).size());
    }

    @Test(timeout = 4000)
    public void testNonVanillaSubclassDirectMapArrayAndObject() throws Exception {
        SubUntypedDeserializer deser = new SubUntypedDeserializer();
        ObjectMapper mapper = new ObjectMapper();

        // Test non-vanilla mapArray: 0, 1, 2, 3, and 30 items
        String[] testArrays = new String[] {
                "[]",
                "[\"x\"]",
                "[\"x\", \"y\"]",
                "[\"x\", \"y\", \"z\"]"
        };
        for (String json : testArrays) {
            JsonParser p = mapper.getFactory().createParser(json);
            p.nextToken(); // START_ARRAY
            DefaultDeserializationContext dc = createDeserializationContext(mapper, p);
            Object res = deser.mapArray(p, dc);
            assertTrue(res instanceof List);
        }

        // Chunk rollover in non-vanilla mapArray
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 30; i++) {
            if (i > 0) sb.append(",");
            sb.append(i);
        }
        sb.append("]");
        JsonParser pLarge = mapper.getFactory().createParser(sb.toString());
        pLarge.nextToken();
        DefaultDeserializationContext dcLarge = createDeserializationContext(mapper, pLarge);
        List<?> largeList = (List<?>) deser.mapArray(pLarge, dcLarge);
        assertEquals(30, largeList.size());

        // Test non-vanilla mapObject: 0, 1, 2, 3, 5 entries
        String[] testObjects = new String[] {
                "{}",
                "{\"a\": 1}",
                "{\"a\": 1, \"b\": 2}",
                "{\"a\": 1, \"b\": 2, \"c\": 3}",
                "{\"a\": 1, \"b\": 2, \"c\": 3, \"d\": 4, \"e\": 5}"
        };
        for (String json : testObjects) {
            JsonParser p = mapper.getFactory().createParser(json);
            p.nextToken(); // START_OBJECT
            DefaultDeserializationContext dc = createDeserializationContext(mapper, p);
            Object res = deser.mapObject(p, dc);
            assertTrue(res instanceof Map);
        }
    }

    @Test(timeout = 4000)
    public void testNonVanillaMapObjectStartingAtFieldName() throws Exception {
        SubUntypedDeserializer deser = new SubUntypedDeserializer();
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("{\"foo\":\"bar\"}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        DefaultDeserializationContext dc = createDeserializationContext(mapper, p);

        Object res = deser.mapObject(p, dc);
        assertTrue(res instanceof Map);
        assertEquals("bar", ((Map<?, ?>) res).get("foo"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Config Features
    // =========================================================================

    @Test(timeout = 4000)
    public void testUseJavaArrayForJsonArrayInVanilla() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);

        // Empty array returns NO_OBJECTS
        Object empty = mapper.readValue("[]", Object.class);
        assertTrue(empty instanceof Object[]);
        assertSame(UntypedObjectDeserializer.NO_OBJECTS, empty);

        // 1 element
        Object one = mapper.readValue("[\"val\"]", Object.class);
        assertTrue(one instanceof Object[]);
        Object[] oneArr = (Object[]) one;
        assertEquals(1, oneArr.length);
        assertEquals("val", oneArr[0]);

        // Large array (>20 elements) triggering chunk rollover in mapArrayToArray
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 30; i++) {
            if (i > 0) sb.append(",");
            sb.append(i);
        }
        sb.append("]");
        Object large = mapper.readValue(sb.toString(), Object.class);
        assertTrue(large instanceof Object[]);
        Object[] largeArr = (Object[]) large;
        assertEquals(30, largeArr.length);
        assertEquals(29, largeArr[29]);
    }

    @Test(timeout = 4000)
    public void testUseJavaArrayForJsonArrayInNonVanilla() throws Exception {
        SubUntypedDeserializer deser = new SubUntypedDeserializer();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);

        // Empty array via deser.deserialize()
        JsonParser pEmpty = mapper.getFactory().createParser("[]");
        pEmpty.nextToken();
        DefaultDeserializationContext dcEmpty = createDeserializationContext(mapper, pEmpty);
        Object emptyRes = deser.deserialize(pEmpty, dcEmpty);
        assertTrue(emptyRes instanceof Object[]);
        assertSame(UntypedObjectDeserializer.NO_OBJECTS, emptyRes);

        // Non-empty array via mapArrayToArray directly with chunk rollover
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 30; i++) {
            if (i > 0) sb.append(",");
            sb.append(i);
        }
        sb.append("]");
        JsonParser pLarge = mapper.getFactory().createParser(sb.toString());
        pLarge.nextToken(); // START_ARRAY
        DefaultDeserializationContext dcLarge = createDeserializationContext(mapper, pLarge);
        Object[] largeArr = deser.mapArrayToArray(pLarge, dcLarge);
        assertEquals(30, largeArr.length);
        assertEquals(0, largeArr[0]);
    }

    @Test(timeout = 4000)
    public void testCoercionFeaturesBigIntegerAndBigDecimal() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        // Vanilla deserialize
        Object bi = mapper.readValue("12345678901234567890", Object.class);
        assertTrue("Expected BigInteger but got " + bi.getClass(), bi instanceof BigInteger);

        Object bd = mapper.readValue("12.3456789", Object.class);
        assertTrue("Expected BigDecimal but got " + bd.getClass(), bd instanceof BigDecimal);

        // Subclass non-vanilla deserialize
        SubUntypedDeserializer deser = new SubUntypedDeserializer();
        JsonParser pInt = mapper.getFactory().createParser("9999");
        pInt.nextToken();
        DefaultDeserializationContext dcInt = createDeserializationContext(mapper, pInt);
        Object biNonVanilla = deser.deserialize(pInt, dcInt);
        assertTrue(biNonVanilla instanceof BigInteger);

        JsonParser pFloat = mapper.getFactory().createParser("99.99");
        pFloat.nextToken();
        DefaultDeserializationContext dcFloat = createDeserializationContext(mapper, pFloat);
        Object bdNonVanilla = deser.deserialize(pFloat, dcFloat);
        assertTrue(bdNonVanilla instanceof BigDecimal);
    }

    @Test(timeout = 4000)
    public void testEmbeddedObjectHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Date embeddedDate = new Date();
        POJONode node = new POJONode(embeddedDate);
        JsonParser p = node.traverse();
        p.nextToken(); // VALUE_EMBEDDED_OBJECT
        DefaultDeserializationContext dc = createDeserializationContext(mapper, p);

        // Test Vanilla
        Object vanillaResult = UntypedObjectDeserializer.Vanilla.std.deserialize(p, dc);
        assertSame(embeddedDate, vanillaResult);

        // Test Non-Vanilla
        JsonParser p2 = node.traverse();
        p2.nextToken();
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer(null, null);
        Object nonVanillaResult = deser.deserialize(p2, dc);
        assertSame(embeddedDate, nonVanillaResult);

        // Test deserializeWithType embedded
        JsonParser p3 = node.traverse();
        p3.nextToken();
        Object typedResult = UntypedObjectDeserializer.Vanilla.std.deserializeWithType(p3, dc, DUMMY_TYPE_DESERIALIZER);
        assertSame(embeddedDate, typedResult);

        JsonParser p4 = node.traverse();
        p4.nextToken();
        Object typedResultNonVanilla = deser.deserializeWithType(p4, dc, DUMMY_TYPE_DESERIALIZER);
        assertSame(embeddedDate, typedResultNonVanilla);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Jackson Databind #989)
    // =========================================================================

    /**
     * Ground Truth Test from Defects4J:
     * com.fasterxml.jackson.databind.deser.TestUntypedDeserialization::testNestedUntyped989
     * Triggers the failure:
     * com.fasterxml.jackson.databind.JsonMappingException: Can not deserialize instance of java.lang.Object out of END_OBJECT token
     */
    @Test(timeout = 4000)
    public void testNestedUntyped989Defects4J() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        final String JSON = "{\"controls\":{\"reconnect\":{}}}";
        Map<String, Object> map = mapper.readValue(JSON, new TypeReference<Map<String, Object>>() { });
        assertNotNull(map);
        assertTrue(map.containsKey("controls"));

        Object controls = map.get("controls");
        assertTrue(controls instanceof Map);
        Map<?, ?> controlsMap = (Map<?, ?>) controls;
        assertTrue(controlsMap.containsKey("reconnect"));

        Object reconnect = controlsMap.get("reconnect");
        assertTrue(reconnect instanceof Map);
        assertTrue(((Map<?, ?>) reconnect).isEmpty());
    }

    @Test(timeout = 4000)
    public void testDefect989DeserializeWhenParserAtEndObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT (caller advanced to first token of empty object)
        assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());

        DefaultDeserializationContext dc = createDeserializationContext(mapper, p);
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer(null, null);

        Object result = deser.deserialize(p, dc);
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    @Test(timeout = 4000)
    public void testDefect989VanillaDeserializeWhenParserAtEndObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());

        DefaultDeserializationContext dc = createDeserializationContext(mapper, p);
        Object result = UntypedObjectDeserializer.Vanilla.std.deserialize(p, dc);
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testDeserializeInvalidTokenVanilla() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("[]");
        p.nextToken(); // START_ARRAY
        p.nextToken(); // END_ARRAY
        DefaultDeserializationContext dc = createDeserializationContext(mapper, p);
        UntypedObjectDeserializer.Vanilla.std.deserialize(p, dc);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testDeserializeInvalidTokenNonVanilla() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("[]");
        p.nextToken(); // START_ARRAY
        p.nextToken(); // END_ARRAY
        DefaultDeserializationContext dc = createDeserializationContext(mapper, p);
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer(null, null);
        deser.deserialize(p, dc);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testDeserializeWithTypeInvalidTokenVanilla() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("[]");
        p.nextToken(); // START_ARRAY
        p.nextToken(); // END_ARRAY
        DefaultDeserializationContext dc = createDeserializationContext(mapper, p);
        UntypedObjectDeserializer.Vanilla.std.deserializeWithType(p, dc, DUMMY_TYPE_DESERIALIZER);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testDeserializeWithTypeInvalidTokenNonVanilla() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("[]");
        p.nextToken(); // START_ARRAY
        p.nextToken(); // END_ARRAY
        DefaultDeserializationContext dc = createDeserializationContext(mapper, p);
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer(null, null);
        deser.deserializeWithType(p, dc, DUMMY_TYPE_DESERIALIZER);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testMapObjectUnexpectedTokenThrows() throws Exception {
        SubUntypedDeserializer deser = new SubUntypedDeserializer();
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("123");
        p.nextToken(); // VALUE_NUMBER_INT
        DefaultDeserializationContext dc = createDeserializationContext(mapper, p);
        deser.mapObject(p, dc);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity & Custom Deserializers
    // =========================================================================

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testConstructorsAndLifecycle() {
        UntypedObjectDeserializer d1 = new UntypedObjectDeserializer();
        assertNotNull(d1);
        assertTrue(d1.isCachable());
        assertSame(Object.class, d1.handledType());

        assertNotNull(UntypedObjectDeserializer.instance);
        assertTrue(UntypedObjectDeserializer.instance.isCachable());
    }

    @Test(timeout = 4000)
    public void testResolveAndContextualWithStandardTypes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext dc = createDeserializationContext(mapper, null);

        UntypedObjectDeserializer deser = new UntypedObjectDeserializer(null, null);
        deser.resolve(dc);

        JsonDeserializer<?> contextual = deser.createContextual(dc, null);
        assertSame(UntypedObjectDeserializer.Vanilla.std, contextual);
    }

    @Test(timeout = 4000)
    public void testResolveWithCustomCollectionAndMapTypes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext dc = createDeserializationContext(mapper, null);

        TypeFactory tf = mapper.getTypeFactory();
        JavaType listType = tf.constructCollectionType(LinkedList.class, Object.class);
        JavaType mapType = tf.constructMapType(TreeMap.class, String.class, Object.class);

        UntypedObjectDeserializer customTypesDeser = new UntypedObjectDeserializer(listType, mapType);
        customTypesDeser.resolve(dc);

        // When custom list/map types are configured, createContextual should return this, not Vanilla
        JsonDeserializer<?> contextual = customTypesDeser.createContextual(dc, null);
        assertSame(customTypesDeser, contextual);
    }

    @Test(timeout = 4000)
    public void testSubclassCreateContextualReturnsSelf() throws Exception {
        SubUntypedDeserializer subDeser = new SubUntypedDeserializer();
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext dc = createDeserializationContext(mapper, null);
        subDeser.resolve(dc);

        JsonDeserializer<?> contextual = subDeser.createContextual(dc, null);
        assertSame(subDeser, contextual);
    }

    @Test(timeout = 4000)
    public void testCustomDeserializerOverrides() throws Exception {
        UntypedObjectDeserializer base = new UntypedObjectDeserializer(null, null);
        CustomMapDeser mapDeser = new CustomMapDeser();
        CustomListDeser listDeser = new CustomListDeser();
        CustomStringDeser stringDeser = new CustomStringDeser();
        CustomNumberDeser numberDeser = new CustomNumberDeser();

        UntypedObjectDeserializer overridden = new UntypedObjectDeserializer(
                base, mapDeser, listDeser, stringDeser, numberDeser
        );

        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext dc = createDeserializationContext(mapper, null);

        // Test ID_START_OBJECT delegation
        JsonParser pObj = mapper.getFactory().createParser("{\"foo\":\"bar\"}");
        pObj.nextToken();
        Object objRes = overridden.deserialize(pObj, dc);
        assertTrue(objRes instanceof Map);
        assertEquals(Boolean.TRUE, ((Map<?, ?>) objRes).get("custom_map"));

        // Test ID_START_ARRAY delegation
        JsonParser pArr = mapper.getFactory().createParser("[\"hello\"]");
        pArr.nextToken();
        Object arrRes = overridden.deserialize(pArr, dc);
        assertTrue(arrRes instanceof List);
        assertEquals("custom_list", ((List<?>) arrRes).get(0));

        // Test ID_STRING delegation
        JsonParser pStr = mapper.getFactory().createParser("\"simple_str\"");
        pStr.nextToken();
        Object strRes = overridden.deserialize(pStr, dc);
        assertEquals("CUSTOM_STRING:simple_str", strRes);

        // Test ID_NUMBER_INT delegation
        JsonParser pInt = mapper.getFactory().createParser("42");
        pInt.nextToken();
        Object intRes = overridden.deserialize(pInt, dc);
        assertEquals(88888, intRes);

        // Test ID_NUMBER_FLOAT delegation
        JsonParser pFloat = mapper.getFactory().createParser("42.42");
        pFloat.nextToken();
        Object floatRes = overridden.deserialize(pFloat, dc);
        assertEquals(88888, floatRes);

        // Test deserializeWithType delegations for custom deserializers
        JsonParser pStrTyped = mapper.getFactory().createParser("\"typed_str\"");
        pStrTyped.nextToken();
        assertEquals("CUSTOM_STRING:typed_str", overridden.deserializeWithType(pStrTyped, dc, DUMMY_TYPE_DESERIALIZER));

        JsonParser pIntTyped = mapper.getFactory().createParser("100");
        pIntTyped.nextToken();
        assertEquals(88888, overridden.deserializeWithType(pIntTyped, dc, DUMMY_TYPE_DESERIALIZER));

        JsonParser pFloatTyped = mapper.getFactory().createParser("100.5");
        pFloatTyped.nextToken();
        assertEquals(88888, overridden.deserializeWithType(pFloatTyped, dc, DUMMY_TYPE_DESERIALIZER));
    }

    @Test(timeout = 4000)
    public void testWithResolvedProtectedMethod() {
        SubUntypedDeserializer deser = new SubUntypedDeserializer();
        CustomMapDeser mapDeser = new CustomMapDeser();
        CustomListDeser listDeser = new CustomListDeser();
        CustomStringDeser stringDeser = new CustomStringDeser();
        CustomNumberDeser numberDeser = new CustomNumberDeser();

        JsonDeserializer<?> resolved = deser._withResolved(mapDeser, listDeser, stringDeser, numberDeser);
        assertNotNull(resolved);
        assertTrue(resolved instanceof UntypedObjectDeserializer);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeBranchesVanillaAndNonVanilla() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext dc = createDeserializationContext(mapper, null);
        UntypedObjectDeserializer nonVanilla = new UntypedObjectDeserializer(null, null);

        // Types that forward to typeDeserializer: ID_START_ARRAY, ID_START_OBJECT, ID_FIELD_NAME
        JsonParser pArray = mapper.getFactory().createParser("[]");
        pArray.nextToken();
        assertEquals("TYPED_ANY", UntypedObjectDeserializer.Vanilla.std.deserializeWithType(pArray, dc, DUMMY_TYPE_DESERIALIZER));

        JsonParser pObj = mapper.getFactory().createParser("{}");
        pObj.nextToken();
        assertEquals("TYPED_ANY", nonVanilla.deserializeWithType(pObj, dc, DUMMY_TYPE_DESERIALIZER));

        // Scalar types in Vanilla deserializeWithType
        JsonParser pStr = mapper.getFactory().createParser("\"abc\"");
        pStr.nextToken();
        assertEquals("abc", UntypedObjectDeserializer.Vanilla.std.deserializeWithType(pStr, dc, DUMMY_TYPE_DESERIALIZER));

        JsonParser pInt = mapper.getFactory().createParser("10");
        pInt.nextToken();
        assertEquals(10, UntypedObjectDeserializer.Vanilla.std.deserializeWithType(pInt, dc, DUMMY_TYPE_DESERIALIZER));

        JsonParser pFloat = mapper.getFactory().createParser("10.5");
        pFloat.nextToken();
        assertEquals(10.5, ((Double) UntypedObjectDeserializer.Vanilla.std.deserializeWithType(pFloat, dc, DUMMY_TYPE_DESERIALIZER)).doubleValue(), 0.001);

        JsonParser pTrue = mapper.getFactory().createParser("true");
        pTrue.nextToken();
        assertEquals(Boolean.TRUE, UntypedObjectDeserializer.Vanilla.std.deserializeWithType(pTrue, dc, DUMMY_TYPE_DESERIALIZER));

        JsonParser pFalse = mapper.getFactory().createParser("false");
        pFalse.nextToken();
        assertEquals(Boolean.FALSE, UntypedObjectDeserializer.Vanilla.std.deserializeWithType(pFalse, dc, DUMMY_TYPE_DESERIALIZER));

        JsonParser pNull = mapper.getFactory().createParser("null");
        pNull.nextToken();
        assertNull(UntypedObjectDeserializer.Vanilla.std.deserializeWithType(pNull, dc, DUMMY_TYPE_DESERIALIZER));

        // Scalar types in Non-Vanilla deserializeWithType
        JsonParser pStr2 = mapper.getFactory().createParser("\"def\"");
        pStr2.nextToken();
        assertEquals("def", nonVanilla.deserializeWithType(pStr2, dc, DUMMY_TYPE_DESERIALIZER));

        JsonParser pInt2 = mapper.getFactory().createParser("20");
        pInt2.nextToken();
        assertEquals(20, nonVanilla.deserializeWithType(pInt2, dc, DUMMY_TYPE_DESERIALIZER));

        JsonParser pFloat2 = mapper.getFactory().createParser("20.5");
        pFloat2.nextToken();
        assertEquals(20.5, ((Double) nonVanilla.deserializeWithType(pFloat2, dc, DUMMY_TYPE_DESERIALIZER)).doubleValue(), 0.001);

        JsonParser pTrue2 = mapper.getFactory().createParser("true");
        pTrue2.nextToken();
        assertEquals(Boolean.TRUE, nonVanilla.deserializeWithType(pTrue2, dc, DUMMY_TYPE_DESERIALIZER));

        JsonParser pFalse2 = mapper.getFactory().createParser("false");
        pFalse2.nextToken();
        assertEquals(Boolean.FALSE, nonVanilla.deserializeWithType(pFalse2, dc, DUMMY_TYPE_DESERIALIZER));

        JsonParser pNull2 = mapper.getFactory().createParser("null");
        pNull2.nextToken();
        assertNull(nonVanilla.deserializeWithType(pNull2, dc, DUMMY_TYPE_DESERIALIZER));
    }
}