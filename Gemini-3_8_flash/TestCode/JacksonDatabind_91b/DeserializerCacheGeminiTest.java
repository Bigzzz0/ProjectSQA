package com.fasterxml.jackson.databind.deser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

/*
 * [Branch & Defect Analysis Matrix]
 * Class Under Test: com.fasterxml.jackson.databind.deser.DeserializerCache
 *
 * Targeted Decision Branches & Coverage Matrix:
 * 1. cache management:
 *    - cachedDeserializersCount(), flushCachedDeserializers()
 *    - JDK serialization via writeReplace(): clears _incompleteDeserializers
 * 2. findValueDeserializer(ctxt, factory, type):
 *    - _findCachedDeserializer hits vs misses
 *    - IllegalArgumentException when type == null in _findCachedDeserializer
 *    - _createAndCacheValueDeserializer synchronized block:
 *      * double-checked locking hit (cache hit inside sync block)
 *      * cyclic / in-progress resolution via _incompleteDeserializers
 *      * exception conversion: IllegalArgumentException -> JsonMappingException
 * 3. _hasCustomHandlers(JavaType):
 *    - container vs non-container type
 *    - content value handler != null
 *    - content type handler != null
 *    - key type value handler != null (DEFECT FIX Databind #735 / MapDeserializerCachingTest)
 * 4. _createDeserializer / _createDeserializer2 routing:
 *    - Abstract / MapLike / CollectionLike abstract type mapping
 *    - Annotated deserializer via findDeserializerFromAnnotation
 *    - Converter applied on POJO (StdDelegatingDeserializer)
 *    - Enum types -> createEnumDeserializer
 *    - Array types -> createArrayDeserializer
 *    - Map types (true map vs map-like)
 *    - Collection types (true collection vs collection-like, JsonFormat.Shape.OBJECT exception)
 *    - Reference types (ReferenceType)
 *    - Tree types (JsonNode and subtypes)
 *    - Builder-based deserializers (findPOJOBuilder)
 * 5. Unknown Deserializer Handlers:
 *    - _handleUnknownValueDeserializer (concrete vs abstract reporting)
 *    - _handleUnknownKeyDeserializer
 * 6. Defect Target:
 *    - Databind #735 / Defects4J MapDeserializerCachingTest:
 *      Caching of MapType must respect custom key deserializers (via keyType.getValueHandler).
 *      Prior caching of Map<String, String> must not lead to reusing that cached deserializer
 *      when a custom key deserializer is configured on a Map field.
 */
public class DeserializerCacheGeminiTest {

    // =========================================================================
    // Test Support Artifacts (Beans, Converters, Deserializers)
    // =========================================================================

    public static class SimpleBean {
        public String name;
        public int age;
    }

    public static class CustomKeyDeser extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return key + ":custom";
        }
    }

    public static class MapContainerBean {
        @JsonDeserialize(keyUsing = CustomKeyDeser.class)
        public Map<String, String> data;
    }

    public static class CustomStringDeserializer extends StdDeserializer<String> {
        public CustomStringDeserializer() {
            super(String.class);
        }

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) {
            return "custom:" + p.getText();
        }
    }

    @JsonDeserialize(using = CustomStringDeserializer.class)
    public static class AnnotatedTargetClass {
    }

    public static class ConvBean {
        public String text;
    }

    public static class TestConverter extends StdConverter<String, ConvBean> {
        @Override
        public ConvBean convert(String value) {
            ConvBean b = new ConvBean();
            b.text = "converted:" + value;
            return b;
        }
    }

    @JsonDeserialize(converter = TestConverter.class)
    public static class ConvertedBean {
        public String value;
    }

    public enum StatusEnum {
        ACTIVE, INACTIVE
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public static class ObjectShapedCollection extends java.util.ArrayList<String> {
        public String meta;
    }

    public static class CyclicNode {
        public CyclicNode next;
        public String value;
    }

    public static class BuilderBean {
        private final String data;

        BuilderBean(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }

        public static class Builder {
            private String data;

            public Builder withData(String data) {
                this.data = data;
                return this;
            }

            public BuilderBean build() {
                return new BuilderBean(data);
            }
        }
    }

    @JsonDeserialize(builder = BuilderBean.Builder.class)
    public static class BuilderAnnotatedBean {
    }

    public abstract static class AbstractDummy {
        public String id;
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCacheSizeAndFlush() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializerCache cache = new DeserializerCache();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();
        JavaType type = mapper.constructType(SimpleBean.class);

        assertEquals(0, cache.cachedDeserializersCount());

        JsonDeserializer<Object> deser1 = cache.findValueDeserializer(ctxt, factory, type);
        assertNotNull(deser1);
        assertTrue(cache.cachedDeserializersCount() > 0);

        JsonDeserializer<Object> deser2 = cache.findValueDeserializer(ctxt, factory, type);
        assertSame(deser1, deser2);

        cache.flushCachedDeserializers();
        assertEquals(0, cache.cachedDeserializersCount());
    }

    @Test(timeout = 4000)
    public void testHasValueDeserializerFor() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializerCache cache = new DeserializerCache();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();

        JavaType type = mapper.constructType(SimpleBean.class);
        assertTrue(cache.hasValueDeserializerFor(ctxt, factory, type));

        // Looking up again hits cache
        assertTrue(cache.hasValueDeserializerFor(ctxt, factory, type));
    }

    @Test(timeout = 4000)
    public void testFindKeyDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializerCache cache = new DeserializerCache();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();

        JavaType stringType = mapper.constructType(String.class);
        KeyDeserializer kd = cache.findKeyDeserializer(ctxt, factory, stringType);
        assertNotNull(kd);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Type Specializations
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFindCachedDeserializerNullType() {
        DeserializerCache cache = new DeserializerCache();
        cache._findCachedDeserializer(null);
    }

    @Test(timeout = 4000)
    public void testDifferentTypeHierarchies() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializerCache cache = new DeserializerCache();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();

        // 1. Enum Deserializer
        JavaType enumType = mapper.constructType(StatusEnum.class);
        JsonDeserializer<Object> enumDeser = cache.findValueDeserializer(ctxt, factory, enumType);
        assertNotNull(enumDeser);

        // 2. Array Deserializer
        JavaType arrayType = mapper.constructType(String[].class);
        JsonDeserializer<Object> arrayDeser = cache.findValueDeserializer(ctxt, factory, arrayType);
        assertNotNull(arrayDeser);

        // 3. Map Deserializer (true map)
        JavaType mapType = mapper.constructType(new TypeReference<Map<String, Integer>>() {});
        JsonDeserializer<Object> mapDeser = cache.findValueDeserializer(ctxt, factory, mapType);
        assertNotNull(mapDeser);

        // 4. Collection Deserializer (true collection)
        JavaType colType = mapper.constructType(new TypeReference<List<String>>() {});
        JsonDeserializer<Object> colDeser = cache.findValueDeserializer(ctxt, factory, colType);
        assertNotNull(colDeser);

        // 5. Tree Deserializer (JsonNode / ObjectNode)
        JavaType treeType = mapper.constructType(ObjectNode.class);
        JsonDeserializer<Object> treeDeser = cache.findValueDeserializer(ctxt, factory, treeType);
        assertNotNull(treeDeser);
    }

    @Test(timeout = 4000)
    public void testCollectionWithShapeObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializerCache cache = new DeserializerCache();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();

        JavaType type = mapper.constructType(ObjectShapedCollection.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, factory, type);
        assertNotNull(deser);
    }

    @Test(timeout = 4000)
    public void testCyclicDependencyResolution() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializerCache cache = new DeserializerCache();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();

        JavaType type = mapper.constructType(CyclicNode.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, factory, type);
        assertNotNull(deser);
    }

    @Test(timeout = 4000)
    public void testConverterAndAnnotationHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializerCache cache = new DeserializerCache();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();

        // Custom Deserializer annotation
        JavaType annotatedType = mapper.constructType(AnnotatedTargetClass.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, factory, annotatedType);
        assertNotNull(deser);
        assertTrue(deser instanceof CustomStringDeserializer);

        // Converter annotation
        JavaType convType = mapper.constructType(ConvertedBean.class);
        JsonDeserializer<Object> convDeser = cache.findValueDeserializer(ctxt, factory, convType);
        assertNotNull(convDeser);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Databind-735 / Map Custom Handlers)
    // =========================================================================

    /**
     * Dedicated defect test targeting:
     * com.fasterxml.jackson.databind.deser.jdk.MapDeserializerCachingTest::testCachedSerialize
     * "Not using custom key deserializer for input: {"data":{"1st":"onedata","2nd":"twodata"}}"
     *
     * DeserializerCache._hasCustomHandlers must return true for MapTypes with a keyType handler.
     * Otherwise, a cached plain Map<String, String> deserializer is improperly reused, ignoring
     * the custom key deserializer on the MapContainerBean.data property.
     */
    @Test(timeout = 4000)
    public void testMapKeyDeserializerNotBypassedDueToCachingDefect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Step 1: Prime cache with standard Map<String, String>
        Map<String, String> initial = mapper.readValue("{\"1st\":\"plain\"}",
                new TypeReference<Map<String, String>>() {});
        assertEquals("plain", initial.get("1st"));

        // Step 2: Deserialize container that declares custom key deserializer
        String json = "{\"data\":{\"1st\":\"onedata\",\"2nd\":\"twodata\"}}";
        MapContainerBean result = mapper.readValue(json, MapContainerBean.class);

        assertNotNull(result);
        assertNotNull(result.data);
        assertEquals(2, result.data.size());

        // The defect causes result.data to contain keys "1st", "2nd" instead of "1st:custom", "2nd:custom"
        assertTrue("Expected key '1st:custom' from custom key deserializer, but was: " + result.data.keySet(),
                result.data.containsKey("1st:custom"));
        assertTrue("Expected key '2nd:custom' from custom key deserializer, but was: " + result.data.keySet(),
                result.data.containsKey("2nd:custom"));
        assertEquals("onedata", result.data.get("1st:custom"));
        assertEquals("twodata", result.data.get("2nd:custom"));
    }

    @Test(timeout = 4000)
    public void testHasCustomHandlersDirectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializerCache cache = new DeserializerCache();

        TypeFactory tf = mapper.getTypeFactory();
        JavaType keyTypeWithHandler = tf.constructType(String.class).withValueHandler(new CustomKeyDeser());
        JavaType valType = tf.constructType(String.class);

        MapType mapTypeWithKeyHandler = tf.constructMapType(Map.class, keyTypeWithHandler, valType);

        // _findCachedDeserializer checks _hasCustomHandlers(type)
        // If custom handlers are present, it must return null rather than checking cache
        JsonDeserializer<Object> cached = cache._findCachedDeserializer(mapTypeWithKeyHandler);
        assertNull(cached);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testUnknownValueDeserializerThrows() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializerCache cache = new DeserializerCache();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType abstractType = mapper.constructType(AbstractDummy.class);

        try {
            cache._handleUnknownValueDeserializer(ctxt, abstractType);
            fail("Expected JsonMappingException for abstract type unknown deserializer");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not find a Value deserializer"));
        }
    }

    @Test(timeout = 4000)
    public void testUnknownKeyDeserializerThrows() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializerCache cache = new DeserializerCache();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(Object.class);

        try {
            cache._handleUnknownKeyDeserializer(ctxt, type);
            fail("Expected JsonMappingException for unknown key deserializer");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not find a (Map) Key deserializer"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateAndCache2ExceptionWrapping() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializerCache cache = new DeserializerCache();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        // Passing an incomplete or invalid type that triggers IllegalArgumentException
        // in factory should get wrapped in JsonMappingException
        DeserializerFactory faultyFactory = new BasicDeserializerFactory(null) {
            private static final long serialVersionUID = 1L;

            @Override
            public JsonDeserializer<?> createBeanDeserializer(DeserializationContext ctxt,
                    JavaType type, BeanDescription beanDesc) {
                throw new IllegalArgumentException("Forced illegal argument in factory");
            }

            @Override
            public DeserializerFactory withConfig(DeserializerFactoryConfig config) {
                return this;
            }
        };

        try {
            cache._createAndCache2(ctxt, faultyFactory, mapper.constructType(SimpleBean.class));
            fail("Expected JsonMappingException due to wrapped IllegalArgumentException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Forced illegal argument"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationAndWriteReplace() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);

        // Put an entry into incomplete deserializers map to test writeReplace clearing
        cache._incompleteDeserializers.put(type, null);
        assertEquals(1, cache._incompleteDeserializers.size());

        // writeReplace() should return 'this' and clear _incompleteDeserializers
        Object replaced = cache.writeReplace();
        assertSame(cache, replaced);
        assertEquals(0, cache._incompleteDeserializers.size());

        // Full JDK serialization roundtrip
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(cache);
        }

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            Object deserialized = ois.readObject();
            assertNotNull(deserialized);
            assertTrue(deserialized instanceof DeserializerCache);
            DeserializerCache restoredCache = (DeserializerCache) deserialized;
            assertEquals(0, restoredCache.cachedDeserializersCount());
        }
    }
}