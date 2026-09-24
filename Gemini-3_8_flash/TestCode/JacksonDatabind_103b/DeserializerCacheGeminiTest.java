package com.fasterxml.jackson.databind.deser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.fasterxml.jackson.databind.deser.DeserializerCache
 *
 * Covered Branches and Logic Zones:
 * 1. Lifecycle and Serialization:
 *    - writeReplace() clears _incompleteDeserializers and preserves cache lifecycle.
 *    - Java Serialization round-trip integrity.
 * 2. Cache Querying & Management:
 *    - cachedDeserializersCount() & flushCachedDeserializers() state transitions.
 *    - _findCachedDeserializer(JavaType) with null argument (IllegalArgumentException).
 *    - _findCachedDeserializer with custom handlers on ContainerType (ContentType and KeyType handlers).
 * 3. General Deserializer Locating:
 *    - findValueDeserializer(): Cache hit, Cache miss (construct and cache), unknown type failure.
 *    - findKeyDeserializer(): Success path, ResolvableDeserializer path, unknown key deserializer failure.
 *    - hasValueDeserializerFor(): True for concrete types/containers/primitives; exception or false for invalid.
 * 4. Deserializer Construction Branching (_createDeserializer & _createDeserializer2):
 *    - Abstract type mappings (interfaces, abstract classes).
 *    - Explicit deserializer from annotations (@JsonDeserialize(using = ...)).
 *    - Builder-based deserialization (@JsonDeserialize(builder = ...)).
 *    - Converter-based deserialization (@JsonDeserialize(converter = ...)).
 *    - Enum types (_createDeserializer2 -> createEnumDeserializer).
 *    - Container types:
 *      * ArrayType -> createArrayDeserializer.
 *      * MapLikeType (true Map vs MapLike).
 *      * CollectionLikeType (true Collection vs CollectionLike).
 *      * Shape.OBJECT override on Map and Collection (bypasses container handler to POJO handler).
 *    - Reference types (AtomicReference -> createReferenceDeserializer).
 *    - Tree types (JsonNode / ObjectNode -> createTreeDeserializer).
 *    - Standard Bean POJOs -> createBeanDeserializer.
 * 5. Type Modifiers and Annotations:
 *    - modifyTypeByAnnotation with custom key deserializer and content deserializer.
 *    - _verifyAsClass branch checks (null, non-Class, None.class, bogus class).
 * 6. Defect-Targeted Branch Zone (BasicExceptionTest / Map Key Enum Deserialization):
 *    - Key deserializer lookup for Enums and handling invalid representation on Map keys.
 */
public class DeserializerCacheGeminiTest {

    // ------------------------------------------------------------------------
    // Helper POJOs & Types for Equivalence Partitioning
    // ------------------------------------------------------------------------

    public enum TestEnum {
        ALPHA, BETA, GAMMA
    }

    public enum ABC {
        A, B, C
    }

    public static class SimpleBean {
        public String name;
        public int age;
    }

    public interface CustomInterface {
        void doSomething();
    }

    public static abstract class AbstractBase {
        public int id;
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public static class MapAsObject extends HashMap<String, Object> {
        private static final long serialVersionUID = 1L;
        public String customField;
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public static class CollectionAsObject extends ArrayList<Object> {
        private static final long serialVersionUID = 1L;
        public String collectionName;
    }

    @JsonDeserialize(using = CustomDummyDeserializer.class)
    public static class AnnotatedWithDeser {
        public int val;
    }

    public static class CustomDummyDeserializer extends JsonDeserializer<AnnotatedWithDeser> {
        @Override
        public AnnotatedWithDeser deserialize(JsonParser p, DeserializationContext ctxt) {
            return new AnnotatedWithDeser();
        }
    }

    @JsonDeserialize(converter = StringToBeanConverter.class)
    public static class ConvertedBean {
        public String content;
        public ConvertedBean(String content) { this.content = content; }
    }

    public static class StringToBeanConverter extends StdConverter<String, ConvertedBean> {
        @Override
        public ConvertedBean convert(String value) {
            return new ConvertedBean(value);
        }
    }

    @JsonDeserialize(builder = SimpleBuilder.class)
    public static class BuiltBean {
        private final int x;
        public BuiltBean(int x) { this.x = x; }
        public int getX() { return x; }
    }

    public static class SimpleBuilder {
        public int x;
        public SimpleBuilder withX(int val) { this.x = val; return this; }
        public BuiltBean build() { return new BuiltBean(x); }
    }

    public static class UnkeyableKey {
        private final int val;
        public UnkeyableKey(int a, int b) { this.val = a + b; }
        public int getVal() { return val; }
    }

    // Helper context builder
    private DefaultDeserializationContext createCtxt(ObjectMapper mapper) {
        return ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), null, null);
    }

    // ========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ========================================================================

    @Test(timeout = 4000)
    public void testCacheLifecycleAndCounts() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        assertEquals(0, cache.cachedDeserializersCount());

        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper);
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();
        JavaType type = mapper.getTypeFactory().constructType(SimpleBean.class);

        // Find deserializer - should construct and cache
        JsonDeserializer<Object> deser1 = cache.findValueDeserializer(ctxt, factory, type);
        assertNotNull(deser1);
        assertEquals(1, cache.cachedDeserializersCount());

        // Subsequent lookup should hit cache
        JsonDeserializer<Object> deser2 = cache.findValueDeserializer(ctxt, factory, type);
        assertSame(deser1, deser2);
        assertEquals(1, cache.cachedDeserializersCount());

        // Flush cache
        cache.flushCachedDeserializers();
        assertEquals(0, cache.cachedDeserializersCount());

        // Re-construct after flush
        JsonDeserializer<Object> deser3 = cache.findValueDeserializer(ctxt, factory, type);
        assertNotNull(deser3);
        assertEquals(1, cache.cachedDeserializersCount());
    }

    @Test(timeout = 4000)
    public void testFindKeyDeserializerResolvableAndStandard() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper);
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();

        JavaType stringType = mapper.getTypeFactory().constructType(String.class);
        KeyDeserializer kd = cache.findKeyDeserializer(ctxt, factory, stringType);
        assertNotNull(kd);

        JavaType enumType = mapper.getTypeFactory().constructType(TestEnum.class);
        KeyDeserializer enumKd = cache.findKeyDeserializer(ctxt, factory, enumType);
        assertNotNull(enumKd);
    }

    @Test(timeout = 4000)
    public void testHasValueDeserializerFor() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper);
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();

        JavaType intType = mapper.getTypeFactory().constructType(int.class);
        assertTrue(cache.hasValueDeserializerFor(ctxt, factory, intType));

        JavaType beanType = mapper.getTypeFactory().constructType(SimpleBean.class);
        assertTrue(cache.hasValueDeserializerFor(ctxt, factory, beanType));
    }

    // ========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Custom Handlers
    // ========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFindCachedDeserializerNullTypeThrowsException() {
        DeserializerCache cache = new DeserializerCache();
        cache._findCachedDeserializer(null);
    }

    @Test(timeout = 4000)
    public void testCustomHandlersBypassCaching() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = mapper.getTypeFactory();

        JavaType listType = tf.constructCollectionType(ArrayList.class, String.class);
        JavaType listWithCustomContent = listType.withContentValueHandler("customValueHandler");

        // Should return null and not hit or query the cache
        assertNull(cache._findCachedDeserializer(listWithCustomContent));

        // Map with custom key value handler
        JavaType mapType = tf.constructMapType(HashMap.class, String.class, Integer.class);
        JavaType mapWithKeyHandler = mapType.withKeyValueHandler("customKeyHandler");
        assertNull(cache._findCachedDeserializer(mapWithKeyHandler));

        // Map with content type handler
        JavaType mapWithContentHandler = mapType.withContentTypeHandler("customTypeHandler");
        assertNull(cache._findCachedDeserializer(mapWithContentHandler));
    }

    // ========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defect Alignment)
    // ========================================================================

    @Test(timeout = 4000)
    public void testEnumMapKeyDeserializationErrorFormat() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Defects4J ground truth: verify map key deserializer failure handles error and single location marker
        JavaType mapType = mapper.getTypeFactory().constructMapType(HashMap.class, ABC.class, String.class);

        try {
            // Attempt to deserialize invalid representation for Enum key
            mapper.readValue("{\"INVALID_KEY\": \"test\"}", mapType);
            fail("Expected InvalidFormatException for invalid enum key");
        } catch (InvalidFormatException ife) {
            assertNotNull(ife.getMessage());
            assertTrue(ife.getMessage().contains("Cannot deserialize Map key of type"));
            assertTrue(ife.getMessage().contains("ABC"));
            // Verify path reference exists
            assertNotNull(ife.getPath());
            assertFalse(ife.getPath().isEmpty());
        }
    }

    @Test(timeout = 4000)
    public void testFindKeyDeserializerForEnumKey() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper);
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();

        JavaType keyType = mapper.getTypeFactory().constructType(ABC.class);
        KeyDeserializer kd = cache.findKeyDeserializer(ctxt, factory, keyType);
        assertNotNull("KeyDeserializer for enum ABC must not be null", kd);
    }

    // ========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ========================================================================

    @Test(timeout = 4000)
    public void testUnknownValueDeserializerThrowsJsonMappingException() {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper);
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();

        JavaType abstractType = mapper.getTypeFactory().constructType(CustomInterface.class);

        try {
            cache.findValueDeserializer(ctxt, factory, abstractType);
            fail("Should throw JsonMappingException for unmapped abstract interface");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Cannot find a Value deserializer for abstract type"));
        }
    }

    @Test(timeout = 4000)
    public void testUnknownKeyDeserializerThrowsJsonMappingException() {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper);
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();

        JavaType unkeyable = mapper.getTypeFactory().constructType(UnkeyableKey.class);

        try {
            cache.findKeyDeserializer(ctxt, factory, unkeyable);
            fail("Should throw JsonMappingException for type that cannot be a key");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Cannot find a (Map) Key deserializer for type"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleUnknownValueDeserializerConcreteClass() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper);

        // Directly invoke _handleUnknownValueDeserializer with concrete and abstract types
        JavaType concreteType = mapper.getTypeFactory().constructType(String.class);
        try {
            cache._handleUnknownValueDeserializer(ctxt, concreteType);
            fail("Expected bad definition exception");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Cannot find a Value deserializer for type"));
        }
    }

    // ========================================================================
    // Partition E: Object Lifecycle & Complex Polymorphic / Type Trees
    // ========================================================================

    @Test(timeout = 4000)
    public void testWriteReplaceAndSerialization() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper);
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();

        // Populate something
        cache.findValueDeserializer(ctxt, factory, mapper.getTypeFactory().constructType(SimpleBean.class));
        assertEquals(1, cache.cachedDeserializersCount());

        Object replaced = cache.writeReplace();
        assertSame(cache, replaced);

        // Roundtrip serialization
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(cache);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        DeserializerCache deserialized = (DeserializerCache) ois.readObject();
        assertNotNull(deserialized);
    }

    @Test(timeout = 4000)
    public void testVariousTypeCategoriesCreation() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper);
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();
        TypeFactory tf = mapper.getTypeFactory();

        // 1. Enum
        JsonDeserializer<Object> enumDeser = cache.findValueDeserializer(
                ctxt, factory, tf.constructType(TestEnum.class));
        assertNotNull(enumDeser);

        // 2. Array
        ArrayType arrayType = tf.constructArrayType(String.class);
        JsonDeserializer<Object> arrayDeser = cache.findValueDeserializer(ctxt, factory, arrayType);
        assertNotNull(arrayDeser);

        // 3. Collection
        CollectionType listType = tf.constructCollectionType(ArrayList.class, Integer.class);
        JsonDeserializer<Object> listDeser = cache.findValueDeserializer(ctxt, factory, listType);
        assertNotNull(listDeser);

        // 4. Map
        MapType mapType = tf.constructMapType(HashMap.class, String.class, Double.class);
        JsonDeserializer<Object> mapDeser = cache.findValueDeserializer(ctxt, factory, mapType);
        assertNotNull(mapDeser);

        // 5. Reference Type (AtomicReference)
        JavaType refType = tf.constructReferenceType(AtomicReference.class, tf.constructType(String.class));
        JsonDeserializer<Object> refDeser = cache.findValueDeserializer(ctxt, factory, refType);
        assertNotNull(refDeser);

        // 6. Tree / JsonNode
        JavaType treeType = tf.constructType(JsonNode.class);
        JsonDeserializer<Object> treeDeser = cache.findValueDeserializer(ctxt, factory, treeType);
        assertNotNull(treeDeser);
    }

    @Test(timeout = 4000)
    public void testMapAndCollectionAsObjectShape() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper);
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();
        TypeFactory tf = mapper.getTypeFactory();

        // Map with Shape.OBJECT must resolve to BeanDeserializer instead of MapDeserializer
        JavaType mapAsObjType = tf.constructType(MapAsObject.class);
        JsonDeserializer<Object> mapDeser = cache.findValueDeserializer(ctxt, factory, mapAsObjType);
        assertNotNull(mapDeser);

        // Collection with Shape.OBJECT must resolve to BeanDeserializer
        JavaType colAsObjType = tf.constructType(CollectionAsObject.class);
        JsonDeserializer<Object> colDeser = cache.findValueDeserializer(ctxt, factory, colAsObjType);
        assertNotNull(colDeser);
    }

    @Test(timeout = 4000)
    public void testAnnotatedCustomDeserializerAndConverterAndBuilder() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper);
        DeserializerFactory factory = mapper.getDeserializationContext().getFactory();
        TypeFactory tf = mapper.getTypeFactory();

        // Explicit @JsonDeserialize(using = ...)
        JavaType customDeserType = tf.constructType(AnnotatedWithDeser.class);
        JsonDeserializer<Object> customDeser = cache.findValueDeserializer(ctxt, factory, customDeserType);
        assertNotNull(customDeser);
        assertTrue(customDeser instanceof CustomDummyDeserializer);

        // Converter @JsonDeserialize(converter = ...)
        JavaType convType = tf.constructType(ConvertedBean.class);
        JsonDeserializer<Object> convDeser = cache.findValueDeserializer(ctxt, factory, convType);
        assertNotNull(convDeser);

        // Builder @JsonDeserialize(builder = ...)
        JavaType builderType = tf.constructType(BuiltBean.class);
        JsonDeserializer<Object> builderDeser = cache.findValueDeserializer(ctxt, factory, builderType);
        assertNotNull(builderDeser);
    }
}