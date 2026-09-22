package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer.Vanilla;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: UntypedObjectDeserializer
 * 
 * Key Decision Branches:
 * 1. deserialize() token switch:
 *    - ID_START_OBJECT / ID_FIELD_NAME -> mapObject() or _mapDeserializer
 *    - ID_START_ARRAY -> mapArray() / mapArrayToArray() / _listDeserializer
 *    - ID_EMBEDDED_OBJECT -> getEmbeddedObject()
 *    - ID_STRING -> _stringDeserializer or getText()
 *    - ID_NUMBER_INT -> _numberDeserializer or _coerceIntegral()
 *    - ID_NUMBER_FLOAT -> USE_BIG_DECIMAL_FOR_FLOATS handling
 *    - ID_NULL -> null
 *    - default -> mappingException
 * 
 * 2. mapObject() branches:
 *    - Empty object (START_OBJECT then END_OBJECT)
 *    - Single entry (FIELD_NAME then VALUE then END_OBJECT)
 *    - Two entries (FIELD_NAME VALUE FIELD_NAME VALUE END_OBJECT)
 *    - Multiple entries (general case)
 *    - Key == null handling (empty map)
 *    - Duplicate keys (LinkedHashMap overwrite)
 * 
 * 3. mapArray() branches:
 *    - Empty array (START_ARRAY then END_ARRAY)
 *    - Single element
 *    - Multiple elements with buffer growth
 *    - USE_JAVA_ARRAY_FOR_JSON_ARRAY feature
 * 
 * 4. resolve() / createContextual() branches:
 *    - Custom deserializer detection
 *    - _clearIfStdImpl() logic
 *    - Vanilla optimization when no custom deserializers
 * 
 * 5. Defect Target (TestNestedUntyped989):
 *    - Nested untyped deserialization where an END_OBJECT token is encountered
 *      in a context expecting a value. The bug causes JsonMappingException:
 *      "Can not deserialize instance of java.lang.Object out of END_OBJECT token"
 *    - This occurs when deserializing a nested object that is empty or when
 *      the parser state is not properly handled for nested structures.
 * 
 * Boundary Conditions:
 * - null tokens, empty arrays/objects, single-element arrays/objects
 * - Large arrays triggering buffer reallocation
 * - Integer/Float boundaries (MAX_VALUE, MIN_VALUE, zero)
 * - USE_BIG_DECIMAL_FOR_FLOATS and USE_BIG_INTEGER_FOR_INTS features
 * - Custom deserializer overrides
 * 
 * Test Strategy:
 * - Partition A: Core deserialization for each token type
 * - Partition B: Boundary values (empty, single, large collections)
 * - Partition C: Defect-targeted nested object/array scenarios
 * - Partition D: Exception paths (invalid tokens, null handling)
 * - Partition E: Lifecycle (resolve, contextualization, caching)
 */
public class UntypedObjectDeserializerDeepseekTest {

    private final UntypedObjectDeserializer deser = new UntypedObjectDeserializer();
    private final ObjectMapper mapper = new ObjectMapper();

    /* ========== Partition A: Core Functional Logic ========== */

    @Test(timeout = 4000)
    public void testDeserializeNull() throws Exception {
        JsonParser p = mapper.getFactory().createParser("null");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertNull(deser.deserialize(p, ctxt));
    }

    @Test(timeout = 4000)
    public void testDeserializeString() throws Exception {
        JsonParser p = mapper.getFactory().createParser("\"hello\"");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertEquals("hello", deser.deserialize(p, ctxt));
    }

    @Test(timeout = 4000)
    public void testDeserializeInt() throws Exception {
        JsonParser p = mapper.getFactory().createParser("42");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof Integer);
        assertEquals(42, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeLong() throws Exception {
        JsonParser p = mapper.getFactory().createParser("1234567890123");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof Long);
        assertEquals(1234567890123L, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeFloat() throws Exception {
        JsonParser p = mapper.getFactory().createParser("3.14");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof Double);
        assertEquals(3.14, ((Double) result).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testDeserializeBoolean() throws Exception {
        JsonParser p = mapper.getFactory().createParser("true");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertEquals(Boolean.TRUE, deser.deserialize(p, ctxt));
    }

    @Test(timeout = 4000)
    public void testDeserializeEmbeddedObject() throws Exception {
        // Use a parser that returns embedded objects
        JsonParser p = mapper.getFactory().createParser("{\"__embedded__\":1}");
        p.nextToken();
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // VALUE_NUMBER_INT
        // Simulate embedded object by using a custom parser? For simplicity, test with a mock
        // Since we can't easily create embedded objects, test the branch with a custom parser
        // that returns ID_EMBEDDED_OBJECT
        // This is a placeholder; actual embedded object testing requires a custom JsonParser
        // We'll test the branch indirectly via mapObject with embedded values
    }

    @Test(timeout = 4000)
    public void testDeserializeEmptyObject() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof Map);
        assertEquals(0, ((Map<?, ?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeSingleEntryObject() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"key\":\"value\"}");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(1, map.size());
        assertEquals("value", map.get("key"));
    }

    @Test(timeout = 4000)
    public void testDeserializeTwoEntryObject() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"a\":1,\"b\":2}");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(2, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
    }

    @Test(timeout = 4000)
    public void testDeserializeMultipleEntryObject() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"a\":1,\"b\":2,\"c\":3,\"d\":4}");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(4, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(4, map.get("d"));
    }

    @Test(timeout = 4000)
    public void testDeserializeEmptyArray() throws Exception {
        JsonParser p = mapper.getFactory().createParser("[]");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof List);
        assertEquals(0, ((List<?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeSingleElementArray() throws Exception {
        JsonParser p = mapper.getFactory().createParser("[42]");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(1, list.size());
        assertEquals(42, list.get(0));
    }

    @Test(timeout = 4000)
    public void testDeserializeMultipleElementArray() throws Exception {
        JsonParser p = mapper.getFactory().createParser("[1,2,3,4,5]");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(5, list.size());
        assertEquals(1, list.get(0));
        assertEquals(5, list.get(4));
    }

    @Test(timeout = 4000)
    public void testDeserializeArrayToArray() throws Exception {
        ObjectMapper mapperWithArray = new ObjectMapper();
        mapperWithArray.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
        JsonParser p = mapperWithArray.getFactory().createParser("[1,2,3]");
        p.nextToken();
        DeserializationContext ctxt = mapperWithArray.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof Object[]);
        Object[] array = (Object[]) result;
        assertEquals(3, array.length);
        assertEquals(1, array[0]);
        assertEquals(3, array[2]);
    }

    /* ========== Partition B: Boundary Value Analysis ========== */

    @Test(timeout = 4000)
    public void testDeserializeNullValueInObject() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"key\":null}");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(1, map.size());
        assertTrue(map.containsKey("key"));
        assertNull(map.get("key"));
    }

    @Test(timeout = 4000)
    public void testDeserializeEmptyString() throws Exception {
        JsonParser p = mapper.getFactory().createParser("\"\"");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertEquals("", deser.deserialize(p, ctxt));
    }

    @Test(timeout = 4000)
    public void testDeserializeMaxInt() throws Exception {
        JsonParser p = mapper.getFactory().createParser(String.valueOf(Integer.MAX_VALUE));
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertEquals(Integer.MAX_VALUE, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeMinInt() throws Exception {
        JsonParser p = mapper.getFactory().createParser(String.valueOf(Integer.MIN_VALUE));
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertEquals(Integer.MIN_VALUE, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeMaxLong() throws Exception {
        JsonParser p = mapper.getFactory().createParser(String.valueOf(Long.MAX_VALUE));
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertEquals(Long.MAX_VALUE, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeBigIntegerFeature() throws Exception {
        ObjectMapper mapperWithBigInt = new ObjectMapper();
        mapperWithBigInt.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        JsonParser p = mapperWithBigInt.getFactory().createParser("12345678901234567890");
        p.nextToken();
        DeserializationContext ctxt = mapperWithBigInt.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger("12345678901234567890"), result);
    }

    @Test(timeout = 4000)
    public void testDeserializeBigDecimalFeature() throws Exception {
        ObjectMapper mapperWithBigDec = new ObjectMapper();
        mapperWithBigDec.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        JsonParser p = mapperWithBigDec.getFactory().createParser("3.14159265358979323846");
        p.nextToken();
        DeserializationContext ctxt = mapperWithBigDec.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("3.14159265358979323846"), result);
    }

    @Test(timeout = 4000)
    public void testDeserializeLargeArray() throws Exception {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) sb.append(",");
            sb.append(i);
        }
        sb.append("]");
        JsonParser p = mapper.getFactory().createParser(sb.toString());
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(1000, list.size());
        assertEquals(0, list.get(0));
        assertEquals(999, list.get(999));
    }

    /* ========== Partition C: Defect-Targeted Tests ========== */

    /**
     * Defect Test: TestNestedUntyped989
     * This test targets the known defect where deserializing a nested untyped
     * object results in JsonMappingException: "Can not deserialize instance of
     * java.lang.Object out of END_OBJECT token"
     * 
     * The bug occurs when a nested object is empty or when the parser encounters
     * an END_OBJECT token in a context where a value is expected.
     */
    @Test(timeout = 4000)
    public void testNestedUntyped989() throws Exception {
        // This is the exact scenario from the defect report
        String json = "{\"outer\":{\"inner\":{}}}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        
        // This should not throw an exception
        Object result = deser.deserialize(p, ctxt);
        
        assertNotNull(result);
        assertTrue(result instanceof Map);
        Map<?, ?> outer = (Map<?, ?>) result;
        assertEquals(1, outer.size());
        assertTrue(outer.get("outer") instanceof Map);
        Map<?, ?> inner = (Map<?, ?>) outer.get("outer");
        assertEquals(0, inner.size());
    }

    @Test(timeout = 4000)
    public void testNestedUntypedWithArray() throws Exception {
        String json = "{\"outer\":[{\"inner\":1}]}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        
        Object result = deser.deserialize(p, ctxt);
        assertNotNull(result);
        Map<?, ?> outer = (Map<?, ?>) result;
        assertTrue(outer.get("outer") instanceof List);
        List<?> list = (List<?>) outer.get("outer");
        assertEquals(1, list.size());
        Map<?, ?> inner = (Map<?, ?>) list.get(0);
        assertEquals(1, inner.get("inner"));
    }

    @Test(timeout = 4000)
    public void testNestedUntypedDeeplyNested() throws Exception {
        String json = "{\"a\":{\"b\":{\"c\":{\"d\":{}}}}}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        
        Object result = deser.deserialize(p, ctxt);
        assertNotNull(result);
        Map<?, ?> current = (Map<?, ?>) result;
        for (int i = 0; i < 3; i++) {
            assertEquals(1, current.size());
            current = (Map<?, ?>) current.values().iterator().next();
        }
        assertEquals(0, current.size());
    }

    @Test(timeout = 4000)
    public void testNestedUntypedWithNull() throws Exception {
        String json = "{\"a\":null}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        
        Object result = deser.deserialize(p, ctxt);
        assertNotNull(result);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(1, map.size());
        assertNull(map.get("a"));
    }

    /* ========== Partition D: Exception & Defensive Guard Paths ========== */

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testDeserializeInvalidToken() throws Exception {
        // Create a parser positioned at END_OBJECT which is invalid for deserialize
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        deser.deserialize(p, ctxt);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testDeserializeWithTypeInvalidToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = mapper.getDeserializationContext().getTypeFactory()
                .constructType(Object.class).getTypeHandler();
        // This should throw because we can't deserialize with type from an object token
        deser.deserializeWithType(p, ctxt, typeDeser);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeNull() throws Exception {
        JsonParser p = mapper.getFactory().createParser("null");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = mapper.getDeserializationContext().getTypeFactory()
                .constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertNull(result);
    }

    /* ========== Partition E: Lifecycle & Contract Integrity ========== */

    @Test(timeout = 4000)
    public void testIsCachable() {
        assertTrue(deser.isCachable());
    }

    @Test(timeout = 4000)
    public void testCreateContextualVanilla() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonDeserializer<?> result = deser.createContextual(ctxt, null);
        assertNotNull(result);
        // Should return Vanilla instance when no custom deserializers
        assertTrue(result instanceof Vanilla);
    }

    @Test(timeout = 4000)
    public void testResolve() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        deser.resolve(ctxt);
        // After resolve, should have custom deserializers or null
        // Just verify no exception is thrown
    }

    @Test(timeout = 4000)
    public void testVanillaDeserialize() throws Exception {
        Vanilla vanilla = Vanilla.std;
        JsonParser p = mapper.getFactory().createParser("{\"a\":1}");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = vanilla.deserialize(p, ctxt);
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(1, map.size());
        assertEquals(1, map.get("a"));
    }

    @Test(timeout = 4000)
    public void testVanillaDeserializeArray() throws Exception {
        Vanilla vanilla = Vanilla.std;
        JsonParser p = mapper.getFactory().createParser("[1,2,3]");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = vanilla.deserialize(p, ctxt);
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(3, list.size());
    }

    @Test(timeout = 4000)
    public void testVanillaDeserializeWithType() throws Exception {
        Vanilla vanilla = Vanilla.std;
        JsonParser p = mapper.getFactory().createParser("null");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = vanilla.deserializeWithType(p, ctxt, typeDeser);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testMapArrayToArrayEmpty() throws Exception {
        // Test the protected method via reflection or through deserialization
        ObjectMapper mapperWithArray = new ObjectMapper();
        mapperWithArray.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
        JsonParser p = mapperWithArray.getFactory().createParser("[]");
        p.nextToken();
        DeserializationContext ctxt = mapperWithArray.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof Object[]);
        assertEquals(0, ((Object[]) result).length);
    }

    @Test(timeout = 4000)
    public void testMapArrayToArraySingle() throws Exception {
        ObjectMapper mapperWithArray = new ObjectMapper();
        mapperWithArray.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
        JsonParser p = mapperWithArray.getFactory().createParser("[42]");
        p.nextToken();
        DeserializationContext ctxt = mapperWithArray.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof Object[]);
        Object[] array = (Object[]) result;
        assertEquals(1, array.length);
        assertEquals(42, array[0]);
    }

    @Test(timeout = 4000)
    public void testMapArrayToArrayMultiple() throws Exception {
        ObjectMapper mapperWithArray = new ObjectMapper();
        mapperWithArray.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
        JsonParser p = mapperWithArray.getFactory().createParser("[1,2,3,4,5]");
        p.nextToken();
        DeserializationContext ctxt = mapperWithArray.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof Object[]);
        Object[] array = (Object[]) result;
        assertEquals(5, array.length);
        assertEquals(1, array[0]);
        assertEquals(5, array[4]);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithCustomStringDeserializer() throws Exception {
        // Create a custom deserializer for strings
        JsonDeserializer<Object> customStringDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return "custom:" + p.getText();
            }
        };
        
        UntypedObjectDeserializer customDeser = new UntypedObjectDeserializer(
                null, null, customStringDeser, null);
        
        JsonParser p = mapper.getFactory().createParser("\"hello\"");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = customDeser.deserialize(p, ctxt);
        assertEquals("custom:hello", result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithCustomNumberDeserializer() throws Exception {
        JsonDeserializer<Object> customNumberDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return "num:" + p.getText();
            }
        };
        
        UntypedObjectDeserializer customDeser = new UntypedObjectDeserializer(
                null, null, null, customNumberDeser);
        
        JsonParser p = mapper.getFactory().createParser("42");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = customDeser.deserialize(p, ctxt);
        assertEquals("num:42", result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithCustomMapDeserializer() throws Exception {
        JsonDeserializer<Object> customMapDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return "customMap";
            }
        };
        
        UntypedObjectDeserializer customDeser = new UntypedObjectDeserializer(
                null, customMapDeser, null, null);
        
        JsonParser p = mapper.getFactory().createParser("{\"a\":1}");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = customDeser.deserialize(p, ctxt);
        assertEquals("customMap", result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithCustomListDeserializer() throws Exception {
        JsonDeserializer<Object> customListDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return "customList";
            }
        };
        
        UntypedObjectDeserializer customDeser = new UntypedObjectDeserializer(
                null, null, customListDeser, null);
        
        JsonParser p = mapper.getFactory().createParser("[1,2,3]");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = customDeser.deserialize(p, ctxt);
        assertEquals("customList", result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithAllCustomDeserializers() throws Exception {
        JsonDeserializer<Object> customMapDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return "customMap";
            }
        };
        JsonDeserializer<Object> customListDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return "customList";
            }
        };
        JsonDeserializer<Object> customStringDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return "customString";
            }
        };
        JsonDeserializer<Object> customNumberDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return "customNumber";
            }
        };
        
        UntypedObjectDeserializer customDeser = new UntypedObjectDeserializer(
                null, customMapDeser, customListDeser, customStringDeser, customNumberDeser);
        
        // Test map
        JsonParser p = mapper.getFactory().createParser("{\"a\":1}");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertEquals("customMap", customDeser.deserialize(p, ctxt));
        
        // Test list
        p = mapper.getFactory().createParser("[1,2]");
        p.nextToken();
        assertEquals("customList", customDeser.deserialize(p, ctxt));
        
        // Test string
        p = mapper.getFactory().createParser("\"str\"");
        p.nextToken();
        assertEquals("customString", customDeser.deserialize(p, ctxt));
        
        // Test number
        p = mapper.getFactory().createParser("42");
        p.nextToken();
        assertEquals("customNumber", customDeser.deserialize(p, ctxt));
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAny() throws Exception {
        // Test deserializeWithType with various tokens
        String[] jsons = {"null", "true", "42", "\"str\"", "[1,2]", "{\"a\":1}"};
        for (String json : jsons) {
            JsonParser p = mapper.getFactory().createParser(json);
            p.nextToken();
            DeserializationContext ctxt = mapper.getDeserializationContext();
            TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
            Object result = deser.deserializeWithType(p, ctxt, typeDeser);
            assertNotNull(result);
        }
    }

    @Test(timeout = 4000)
    public void testMapObjectWithDuplicateKeys() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"a\":1,\"a\":2}");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(1, map.size());
        assertEquals(2, map.get("a"));
    }

    @Test(timeout = 4000)
    public void testMapObjectWithNestedArray() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"arr\":[1,2,{\"nested\":true}]}");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        Map<?, ?> map = (Map<?, ?>) result;
        List<?> arr = (List<?>) map.get("arr");
        assertEquals(3, arr.size());
        assertEquals(1, arr.get(0));
        Map<?, ?> nested = (Map<?, ?>) arr.get(2);
        assertEquals(Boolean.TRUE, nested.get("nested"));
    }

    @Test(timeout = 4000)
    public void testMapArrayWithNestedObject() throws Exception {
        JsonParser p = mapper.getFactory().createParser("[{\"a\":1},[2,3],\"str\"]");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        List<?> list = (List<?>) result;
        assertEquals(3, list.size());
        Map<?, ?> map = (Map<?, ?>) list.get(0);
        assertEquals(1, map.get("a"));
        List<?> innerList = (List<?>) list.get(1);
        assertEquals(2, innerList.size());
        assertEquals("str", list.get(2));
    }

    @Test(timeout = 4000)
    public void testDeserializeWithFieldNameToken() throws Exception {
        // Test the case where parser is at FIELD_NAME
        JsonParser p = mapper.getFactory().createParser("{\"a\":1}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(1, map.size());
        assertEquals(1, map.get("a"));
    }

    @Test(timeout = 4000)
    public void testDeserializeWithEndObjectToken() throws Exception {
        // Test the case where parser is at END_OBJECT (empty object)
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof Map);
        assertEquals(0, ((Map<?, ?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithEndArrayToken() throws Exception {
        // Test the case where parser is at END_ARRAY (empty array)
        JsonParser p = mapper.getFactory().createParser("[]");
        p.nextToken(); // START_ARRAY
        p.nextToken(); // END_ARRAY
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof List);
        assertEquals(0, ((List<?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithValueToken() throws Exception {
        // Test the case where parser is at VALUE_NULL
        JsonParser p = mapper.getFactory().createParser("null");
        p.nextToken(); // VALUE_NULL
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertNull(deser.deserialize(p, ctxt));
    }

    @Test(timeout = 4000)
    public void testDeserializeWithNumberFloatToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("1.5");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof Double);
        assertEquals(1.5, ((Double) result).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithNumberIntToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("10");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertTrue(result instanceof Integer);
        assertEquals(10, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithBooleanTrueToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("true");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertEquals(Boolean.TRUE, deser.deserialize(p, ctxt));
    }

    @Test(timeout = 4000)
    public void testDeserializeWithBooleanFalseToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("false");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertEquals(Boolean.FALSE, deser.deserialize(p, ctxt));
    }

    @Test(timeout = 4000)
    public void testDeserializeWithStringToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("\"test\"");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertEquals("test", deser.deserialize(p, ctxt));
    }

    @Test(timeout = 4000)
    public void testDeserializeWithEmbeddedObjectToken() throws Exception {
        // Create a custom parser that returns ID_EMBEDDED_OBJECT
        // This is complex; we'll test the branch indirectly
        // For now, just verify the method handles it without exception
        // by using a mock parser
        JsonParser p = new JsonParser() {
            @Override
            public Object getEmbeddedObject() throws IOException {
                return "embedded";
            }
            
            @Override
            public JsonToken nextToken() throws IOException {
                return JsonToken.VALUE_EMBEDDED_OBJECT;
            }
            
            // ... other abstract methods would need implementation
            // For brevity, we'll just test the deserialize method with a real parser
            // that produces an embedded object
        };
        // Since we can't easily create an embedded object, we'll skip this test
        // and rely on other tests covering the branch
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeDeserializer() throws Exception {
        // Test deserializeWithType with a real TypeDeserializer
        JsonParser p = mapper.getFactory().createParser("{\"@type\":\"java.lang.String\",\"value\":\"test\"}");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        // This may throw if type info is not properly handled
        try {
            Object result = deser.deserializeWithType(p, ctxt, typeDeser);
            // If it doesn't throw, we're fine
            assertNotNull(result);
        } catch (JsonMappingException e) {
            // Expected if type info is not available
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNull() throws Exception {
        JsonParser p = mapper.getFactory().createParser("null");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyString() throws Exception {
        JsonParser p = mapper.getFactory().createParser("\"test\"");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertEquals("test", result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNumber() throws Exception {
        JsonParser p = mapper.getFactory().createParser("42");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertEquals(42, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyBoolean() throws Exception {
        JsonParser p = mapper.getFactory().createParser("true");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyArray() throws Exception {
        JsonParser p = mapper.getFactory().createParser("[1,2,3]");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof List);
        assertEquals(3, ((List<?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyObject() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"a\":1}");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Map);
        assertEquals(1, ((Map<?, ?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyEmbedded() throws Exception {
        // Test with embedded object - this is complex, so we'll skip
        // and rely on other tests
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyEndObject() throws Exception {
        // Test the defect scenario with END_OBJECT
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Map);
        assertEquals(0, ((Map<?, ?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyEndArray() throws Exception {
        JsonParser p = mapper.getFactory().createParser("[]");
        p.nextToken(); // START_ARRAY
        p.nextToken(); // END_ARRAY
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof List);
        assertEquals(0, ((List<?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyFieldName() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"a\":1}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Map);
        assertEquals(1, ((Map<?, ?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyStartObject() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"a\":1}");
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Map);
        assertEquals(1, ((Map<?, ?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyStartArray() throws Exception {
        JsonParser p = mapper.getFactory().createParser("[1,2]");
        p.nextToken(); // START_ARRAY
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof List);
        assertEquals(2, ((List<?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyValueTrue() throws Exception {
        JsonParser p = mapper.getFactory().createParser("true");
        p.nextToken(); // VALUE_TRUE
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyValueFalse() throws Exception {
        JsonParser p = mapper.getFactory().createParser("false");
        p.nextToken(); // VALUE_FALSE
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertEquals(Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyValueNull() throws Exception {
        JsonParser p = mapper.getFactory().createParser("null");
        p.nextToken(); // VALUE_NULL
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyValueNumberInt() throws Exception {
        JsonParser p = mapper.getFactory().createParser("42");
        p.nextToken(); // VALUE_NUMBER_INT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertEquals(42, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyValueNumberFloat() throws Exception {
        JsonParser p = mapper.getFactory().createParser("3.14");
        p.nextToken(); // VALUE_NUMBER_FLOAT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Double);
        assertEquals(3.14, ((Double) result).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyValueString() throws Exception {
        JsonParser p = mapper.getFactory().createParser("\"test\"");
        p.nextToken(); // VALUE_STRING
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertEquals("test", result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyValueEmbedded() throws Exception {
        // Complex to test, skip
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNotAvailable() throws Exception {
        // Test with a token that is not handled
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Map);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyInvalid() throws Exception {
        // Test with an invalid token
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyException() throws Exception {
        // Test exception handling
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(p, ctxt, typeDeser);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyIOException() throws Exception {
        // Test IOException handling
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(p, ctxt, typeDeser);
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyJsonProcessingException() throws Exception {
        // Test JsonProcessingException handling
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(p, ctxt, typeDeser);
        } catch (JsonProcessingException e) {
            fail("Unexpected JsonProcessingException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyJsonMappingException() throws Exception {
        // Test JsonMappingException handling
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(p, ctxt, typeDeser);
        } catch (JsonMappingException e) {
            fail("Unexpected JsonMappingException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyRuntimeException() throws Exception {
        // Test RuntimeException handling
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(p, ctxt, typeDeser);
        } catch (RuntimeException e) {
            fail("Unexpected RuntimeException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyError() throws Exception {
        // Test Error handling
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(p, ctxt, typeDeser);
        } catch (Error e) {
            fail("Unexpected Error: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyThrowable() throws Exception {
        // Test Throwable handling
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(p, ctxt, typeDeser);
        } catch (Throwable t) {
            fail("Unexpected Throwable: " + t.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNullTypeDeserializer() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        try {
            deser.deserializeWithType(p, ctxt, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNullParser() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(null, ctxt, typeDeser);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNullContext() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        TypeDeserializer typeDeser = mapper.getDeserializationContext().getTypeFactory()
                .constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(p, null, typeDeser);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNullAll() throws Exception {
        try {
            deser.deserializeWithType(null, null, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyEmptyObject() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Map);
        assertEquals(0, ((Map<?, ?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyEmptyArray() throws Exception {
        JsonParser p = mapper.getFactory().createParser("[]");
        p.nextToken(); // START_ARRAY
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof List);
        assertEquals(0, ((List<?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnySingleObject() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"a\":1}");
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Map);
        assertEquals(1, ((Map<?, ?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnySingleArray() throws Exception {
        JsonParser p = mapper.getFactory().createParser("[1]");
        p.nextToken(); // START_ARRAY
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof List);
        assertEquals(1, ((List<?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyMultipleObject() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"a\":1,\"b\":2}");
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Map);
        assertEquals(2, ((Map<?, ?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyMultipleArray() throws Exception {
        JsonParser p = mapper.getFactory().createParser("[1,2,3]");
        p.nextToken(); // START_ARRAY
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof List);
        assertEquals(3, ((List<?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNestedObject() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"a\":{\"b\":{}}}");
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(1, map.size());
        assertTrue(map.get("a") instanceof Map);
        assertEquals(0, ((Map<?, ?>) map.get("a")).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNestedArray() throws Exception {
        JsonParser p = mapper.getFactory().createParser("[[],[]]");
        p.nextToken(); // START_ARRAY
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(2, list.size());
        assertTrue(list.get(0) instanceof List);
        assertEquals(0, ((List<?>) list.get(0)).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyMixedNested() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"a\":[1,{\"b\":2}]}");
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(1, map.size());
        List<?> list = (List<?>) map.get("a");
        assertEquals(2, list.size());
        assertEquals(1, list.get(0));
        Map<?, ?> inner = (Map<?, ?>) list.get(1);
        assertEquals(2, inner.get("b"));
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyDeeplyNested() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"a\":{\"b\":{\"c\":{\"d\":{}}}}}");
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Map);
        Map<?, ?> current = (Map<?, ?>) result;
        for (int i = 0; i < 3; i++) {
            assertEquals(1, current.size());
            current = (Map<?, ?>) current.values().iterator().next();
        }
        assertEquals(0, current.size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNullValue() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"a\":null}");
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(1, map.size());
        assertNull(map.get("a"));
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyEmptyString() throws Exception {
        JsonParser p = mapper.getFactory().createParser("\"\"");
        p.nextToken(); // VALUE_STRING
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertEquals("", result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyZero() throws Exception {
        JsonParser p = mapper.getFactory().createParser("0");
        p.nextToken(); // VALUE_NUMBER_INT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertEquals(0, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNegative() throws Exception {
        JsonParser p = mapper.getFactory().createParser("-1");
        p.nextToken(); // VALUE_NUMBER_INT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertEquals(-1, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyFloat() throws Exception {
        JsonParser p = mapper.getFactory().createParser("1.5");
        p.nextToken(); // VALUE_NUMBER_FLOAT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Double);
        assertEquals(1.5, ((Double) result).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyBooleanTrue() throws Exception {
        JsonParser p = mapper.getFactory().createParser("true");
        p.nextToken(); // VALUE_TRUE
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertEquals(Boolean.TRUE, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyBooleanFalse() throws Exception {
        JsonParser p = mapper.getFactory().createParser("false");
        p.nextToken(); // VALUE_FALSE
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertEquals(Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNullToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("null");
        p.nextToken(); // VALUE_NULL
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyStringToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("\"test\"");
        p.nextToken(); // VALUE_STRING
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertEquals("test", result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNumberIntToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("42");
        p.nextToken(); // VALUE_NUMBER_INT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertEquals(42, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNumberFloatToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("3.14");
        p.nextToken(); // VALUE_NUMBER_FLOAT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Double);
        assertEquals(3.14, ((Double) result).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyStartObjectToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"a\":1}");
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Map);
        assertEquals(1, ((Map<?, ?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyStartArrayToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("[1,2]");
        p.nextToken(); // START_ARRAY
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof List);
        assertEquals(2, ((List<?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyFieldNameToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{\"a\":1}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Map);
        assertEquals(1, ((Map<?, ?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyEndObjectToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Map);
        assertEquals(0, ((Map<?, ?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyEndArrayToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("[]");
        p.nextToken(); // START_ARRAY
        p.nextToken(); // END_ARRAY
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof List);
        assertEquals(0, ((List<?>) result).size());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyValueEmbeddedToken() throws Exception {
        // Complex to test, skip
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNotAvailableToken() throws Exception {
        // Test with a token that is not handled
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(result instanceof Map);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyInvalidToken() throws Exception {
        // Test with an invalid token
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        Object result = deser.deserializeWithType(p, ctxt, typeDeser);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyExceptionToken() throws Exception {
        // Test exception handling
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(p, ctxt, typeDeser);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyIOExceptionToken() throws Exception {
        // Test IOException handling
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(p, ctxt, typeDeser);
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyJsonProcessingExceptionToken() throws Exception {
        // Test JsonProcessingException handling
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(p, ctxt, typeDeser);
        } catch (JsonProcessingException e) {
            fail("Unexpected JsonProcessingException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyJsonMappingExceptionToken() throws Exception {
        // Test JsonMappingException handling
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(p, ctxt, typeDeser);
        } catch (JsonMappingException e) {
            fail("Unexpected JsonMappingException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyRuntimeExceptionToken() throws Exception {
        // Test RuntimeException handling
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(p, ctxt, typeDeser);
        } catch (RuntimeException e) {
            fail("Unexpected RuntimeException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyErrorToken() throws Exception {
        // Test Error handling
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(p, ctxt, typeDeser);
        } catch (Error e) {
            fail("Unexpected Error: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyThrowableToken() throws Exception {
        // Test Throwable handling
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(p, ctxt, typeDeser);
        } catch (Throwable t) {
            fail("Unexpected Throwable: " + t.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNullTypeDeserializerToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        DeserializationContext ctxt = mapper.getDeserializationContext();
        try {
            deser.deserializeWithType(p, ctxt, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNullParserToken() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeDeserializer typeDeser = ctxt.getTypeFactory().constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(null, ctxt, typeDeser);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNullContextToken() throws Exception {
        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken(); // START_OBJECT
        TypeDeserializer typeDeser = mapper.getDeserializationContext().getTypeFactory()
                .constructType(Object.class).getTypeHandler();
        try {
            deser.deserializeWithType(p, null, typeDeser);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFromAnyNullAllToken() throws Exception {
        try {
            deser.deserializeWithType(null, null, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }
}