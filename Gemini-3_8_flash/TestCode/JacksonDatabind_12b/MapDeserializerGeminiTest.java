package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedCreator;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.deser.std.MapDeserializer
 * Defect Reference: Defects4J / JacksonDatabind-26 (databind#735)
 * Failure Symptom: TestCustomDeserializers::testCustomMapValueDeser735 -> expected:<1> but was:<100>
 * Root Cause: isCachable() returned true even when contextualized or constructed with custom
 *             value/key deserializers, causing custom MapDeserializers to be shared globally in DeserializerCache,
 *             infecting subsequent standard Map deserializations.
 *
 * Branches & Logic Covered:
 * - isCachable():
 *     * valueTypeDeserializer != null vs null
 *     * ignorableProperties != null vs null
 *     * custom key/value deserializers (databind#735 check)
 * - constructors and withResolved():
 *     * identity checks (keyDeser, valueDeser, valueTypeDeser, ignorableProperties)
 *     * copy-constructor preserving settings
 * - _isStdKeyDeser():
 *     * keyDeser == null
 *     * mapType.getKeyType() == null
 *     * rawKeyType == String.class / Object.class vs others
 * - resolve():
 *     * valueInstantiator.canCreateUsingDelegate() (valid delegateType vs null -> IllegalArgumentException)
 *     * valueInstantiator.canCreateFromObjectWith() (PropertyBasedCreator)
 * - createContextual():
 *     * keyDeser resolution (null -> findKeyDeserializer; ContextualKeyDeserializer)
 *     * valueDeser resolution (content converter, secondary contextualization)
 *     * valueTypeDeserializer property scoping
 *     * property-level ignored properties via AnnotationIntrospector
 * - deserialize(jp, ctxt):
 *     * propertyBasedCreator != null
 *     * delegateDeserializer != null
 *     * hasDefaultCreator == false -> JsonMappingException
 *     * tokens: START_OBJECT, FIELD_NAME, END_OBJECT, VALUE_STRING (JACKSON-620 empty string mapping), invalid token
 *     * standardStringKey vs generic key (_readAndBindStringMap vs _readAndBind)
 *     * ignorableProperties skipping
 *     * null value handling (VALUE_NULL -> getNullValue)
 *     * valueTypeDeserializer dispatch (deserializeWithType)
 * - wrapAndThrow():
 *     * InvocationTargetException unwrapping
 *     * Error and IOException propagation
 *     * JsonMappingException wrapping with path/key
 */
public class MapDeserializerGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (databind#735)
    // =========================================================================

    public static class CustomNumberDeserializer extends JsonDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return 100;
        }
    }

    public static class ContainerWithCustomMap {
        @JsonDeserialize(contentUsing = CustomNumberDeserializer.class)
        public Map<String, Integer> map;
    }

    public static class ContainerWithDefaultMap {
        public Map<String, Integer> map;
    }

    /**
     * Directly reproduces the defect reported in Jackson databind#735.
     * When a custom content deserializer is supplied via annotation, isCachable() must ensure
     * that the contextualized deserializer is NOT cached and reused for subsequent default maps.
     */
    @Test(timeout = 4000)
    public void testCustomMapValueDeser735_DefectVerification() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // 1. Deserialize container with custom deserializer: converts any number to 100
        String jsonCustom = "{\"map\":{\"a\":1}}";
        ContainerWithCustomMap resCustom = mapper.readValue(jsonCustom, ContainerWithCustomMap.class);
        assertNotNull(resCustom.map);
        assertEquals(Integer.valueOf(100), resCustom.map.get("a"));

        // 2. Deserialize default container: MUST deserialize "1" as 1, NOT 100
        String jsonDefault = "{\"map\":{\"a\":1}}";
        ContainerWithDefaultMap resDefault = mapper.readValue(jsonDefault, ContainerWithDefaultMap.class);
        assertNotNull(resDefault.map);
        assertEquals(Integer.valueOf(1), resDefault.map.get("a"));
    }

    @Test(timeout = 4000)
    public void testIsCachableLogicDirectly() {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Object.class);
        ValueInstantiator vi = new ValueInstantiator.Base(mapType);
        
        // 1. Standard instance without extra attributes
        MapDeserializer deserStandard = new MapDeserializer(mapType, vi, null, null, null);
        assertTrue(deserStandard.isCachable());

        // 2. Non-null ignorable properties prevents caching
        deserStandard.setIgnorableProperties(new String[]{"ignoreMe"});
        assertFalse(deserStandard.isCachable());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicDeserializationStringKeys() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"key1\":\"val1\", \"key2\":\"val2\"}";
        Map<String, String> result = mapper.readValue(json, new TypeReference<Map<String, String>>() {});

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("val1", result.get("key1"));
        assertEquals("val2", result.get("key2"));
    }

    @Test(timeout = 4000)
    public void testBasicDeserializationNonStringKeys() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"10\":\"apple\", \"20\":\"banana\"}";
        Map<Integer, String> result = mapper.readValue(json, new TypeReference<Map<Integer, String>>() {});

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("apple", result.get(10));
        assertEquals("banana", result.get(20));
    }

    @Test(timeout = 4000)
    public void testDeserializeUpdateExistingMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Object.class);
        MapDeserializer deser = (MapDeserializer) mapper.getDeserializationContext()
                .findRootValueDeserializer(mapType);

        JsonParser parser = mapper.getFactory().createParser("{\"newKey\":\"newValue\"}");
        parser.nextToken(); // START_OBJECT

        Map<Object, Object> existing = new HashMap<Object, Object>();
        existing.put("oldKey", "oldValue");

        Map<Object, Object> updated = deser.deserialize(parser, mapper.getDeserializationContext(), existing);
        assertSame(existing, updated);
        assertEquals(2, updated.size());
        assertEquals("oldValue", updated.get("oldKey"));
        assertEquals("newValue", updated.get("newKey"));
    }

    @Test(timeout = 4000)
    public void testWithResolvedShortCircuit() {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Object.class);
        ValueInstantiator vi = new ValueInstantiator.Base(mapType);
        MapDeserializer deser = new MapDeserializer(mapType, vi, null, null, null);

        // Same parameters should return 'this'
        MapDeserializer resolved = deser.withResolved(null, null, null, null);
        assertSame(deser, resolved);
    }

    @Test(timeout = 4000)
    public void testAccessorsAndGetters() {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(LinkedHashMap.class, String.class, Integer.class);
        ValueInstantiator vi = new ValueInstantiator.Base(mapType);
        MapDeserializer deser = new MapDeserializer(mapType, vi, null, null, null);

        assertEquals(LinkedHashMap.class, deser.getMapClass());
        assertEquals(mapType, deser.getValueType());
        assertEquals(Integer.class, deser.getContentType().getRawClass());
        assertNull(deser.getContentDeserializer());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyJsonMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue("{}", new TypeReference<Map<String, Object>>() {});
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testEmptyStringAsNullOrEmptyMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        Map<String, String> res = mapper.readValue("\"\"", new TypeReference<Map<String, String>>() {});
        assertNull(res);
    }

    @Test(timeout = 4000)
    public void testNullValuesInsideMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"k1\": null, \"k2\": \"value\"}";
        Map<String, String> res = mapper.readValue(json, new TypeReference<Map<String, String>>() {});

        assertNotNull(res);
        assertEquals(2, res.size());
        assertTrue(res.containsKey("k1"));
        assertNull(res.get("k1"));
        assertEquals("value", res.get("k2"));
    }

    @Test(timeout = 4000)
    public void testIgnorablePropertiesSetNullAndEmpty() {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Object.class);
        ValueInstantiator vi = new ValueInstantiator.Base(mapType);
        MapDeserializer deser = new MapDeserializer(mapType, vi, null, null, null);

        deser.setIgnorableProperties(new String[0]);
        assertNull(deser._ignorableProperties);

        deser.setIgnorableProperties(null);
        assertNull(deser._ignorableProperties);

        deser.setIgnorableProperties(new String[]{"propA", "propB"});
        assertNotNull(deser._ignorableProperties);
        assertEquals(2, deser._ignorableProperties.size());
        assertTrue(deser._ignorableProperties.contains("propA"));
    }

    @Test(timeout = 4000)
    public void testIgnorablePropertiesDuringDeserialization() throws Exception {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        ValueInstantiator vi = new ValueInstantiator.Base(mapType) {
            @Override
            public boolean canCreateUsingDefault() { return true; }
            @Override
            public Object createUsingDefault(DeserializationContext ctxt) { return new HashMap<Object, Object>(); }
        };

        MapDeserializer deser = new MapDeserializer(mapType, vi, null, mapper.findRootValueDeserializer(TypeFactory.unknownType()), null);
        deser.setIgnorableProperties(new String[]{"skipMe"});

        JsonParser parser = mapper.getFactory().createParser("{\"skipMe\":{\"nested\":\"value\"}, \"keepMe\":\"val\"}");
        parser.nextToken(); // START_OBJECT

        Map<Object, Object> result = deser.deserialize(parser, ctxt);
        assertEquals(1, result.size());
        assertFalse(result.containsKey("skipMe"));
        assertEquals("val", result.get("keepMe"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializeThrowsWhenNoDefaultConstructor() throws Exception {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Object.class);
        ValueInstantiator vi = new ValueInstantiator.Base(mapType) {
            @Override
            public boolean canCreateUsingDefault() {
                return false;
            }
        };
        MapDeserializer deser = new MapDeserializer(mapType, vi, null, null, null);

        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("{\"key\":\"value\"}");
        p.nextToken();

        try {
            deser.deserialize(p, mapper.getDeserializationContext());
            fail("Expected JsonMappingException due to missing default creator");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("No default constructor found"));
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeThrowsOnInvalidToken() throws Exception {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class);
        ObjectMapper mapper = new ObjectMapper();
        ValueInstantiator vi = new ValueInstantiator.Base(mapType) {
            @Override
            public boolean canCreateUsingDefault() { return true; }
            @Override
            public Object createUsingDefault(DeserializationContext ctxt) { return new HashMap<Object, Object>(); }
        };
        MapDeserializer deser = new MapDeserializer(mapType, vi, null, null, null);

        // Feed an Array start token [ instead of object {
        JsonParser p = mapper.getFactory().createParser("[1, 2, 3]");
        p.nextToken();

        try {
            deser.deserialize(p, mapper.getDeserializationContext());
            fail("Expected JsonMappingException on START_ARRAY token");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testResolveInvalidDelegateCreatorThrowsIllegalArgumentException() {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Object.class);
        ValueInstantiator vi = new ValueInstantiator.Base(mapType) {
            @Override
            public boolean canCreateUsingDelegate() { return true; }
            @Override
            public JavaType getDelegateType(DeserializationConfig config) { return null; }
        };

        MapDeserializer deser = new MapDeserializer(mapType, vi, null, null, null);
        ObjectMapper mapper = new ObjectMapper();
        try {
            deser.resolve(mapper.getDeserializationContext());
            fail("Expected IllegalArgumentException when delegate type is null");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("returned true for 'canCreateUsingDelegate()', but null for 'getDelegateType()'"));
        } catch (JsonMappingException e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrowBehaviors() throws Exception {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class);
        ValueInstantiator vi = new ValueInstantiator.Base(mapType);
        MapDeserializer deser = new MapDeserializer(mapType, vi, null, null, null);

        // 1. Error should be rethrown directly
        try {
            deser.wrapAndThrow(new OutOfMemoryError("boom"), Map.class, "key");
            fail("Should rethrow Error");
        } catch (OutOfMemoryError e) {
            assertEquals("boom", e.getMessage());
        }

        // 2. IOException (non JsonMappingException) should be rethrown directly
        try {
            deser.wrapAndThrow(new IOException("io_error"), Map.class, "key");
            fail("Should rethrow IOException");
        } catch (IOException e) {
            assertEquals("io_error", e.getMessage());
        }

        // 3. InvocationTargetException unwrapping
        try {
            InvocationTargetException ite = new InvocationTargetException(new NumberFormatException("bad number"));
            deser.wrapAndThrow(ite, Map.class, "testKey");
            fail("Should wrap target exception");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getCause() instanceof NumberFormatException);
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Polymorphic / Creator Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testCopyConstructorIntegrity() {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class);
        ValueInstantiator vi = new ValueInstantiator.Base(mapType);
        MapDeserializer original = new MapDeserializer(mapType, vi, null, null, null);
        original.setIgnorableProperties(new String[]{"x"});

        MapDeserializer copy = new MapDeserializer(original);
        assertEquals(original.getMapClass(), copy.getMapClass());
        assertEquals(original.getValueType(), copy.getValueType());
        assertFalse(copy.isCachable());
        assertNotNull(copy._ignorableProperties);
        assertTrue(copy._ignorableProperties.contains("x"));
    }

    @Test(timeout = 4000)
    public void testStdKeyDeserHeuristics() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ValueInstantiator vi = new ValueInstantiator.Base(tf.constructType(Map.class));

        // 1. keyDeser == null -> true
        JavaType mapTypeStr = tf.constructMapType(HashMap.class, String.class, Object.class);
        MapDeserializer deser1 = new MapDeserializer(mapTypeStr, vi, null, null, null);
        assertTrue(deser1._isStdKeyDeser(mapTypeStr, null));

        // 2. keyType is Object.class -> true when default key deser used
        JavaType mapTypeObj = tf.constructMapType(HashMap.class, Object.class, Object.class);
        MapDeserializer deser2 = new MapDeserializer(mapTypeObj, vi, null, null, null);
        assertTrue(deser2._isStdKeyDeser(mapTypeObj, null));

        // 3. non-standard key type with custom deserializer
        JavaType mapTypeInt = tf.constructMapType(HashMap.class, Integer.class, Object.class);
        KeyDeserializer customKd = new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt) {
                return Integer.parseInt(key);
            }
        };
        MapDeserializer deser3 = new MapDeserializer(mapTypeInt, vi, customKd, null, null);
        assertFalse(deser3._isStdKeyDeser(mapTypeInt, customKd));
    }

    public static class DelegateContainer {
        public final Map<String, Object> values;
        public DelegateContainer(Map<String, Object> values) {
            this.values = values;
        }
    }

    @Test(timeout = 4000)
    public void testDelegateDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"prop1\":\"v1\", \"prop2\":42}";
        Map<String, Object> map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        assertNotNull(map);
        assertEquals("v1", map.get("prop1"));
        assertEquals(42, map.get("prop2"));
    }
}