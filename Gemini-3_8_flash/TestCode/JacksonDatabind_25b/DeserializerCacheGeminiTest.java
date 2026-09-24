package com.fasterxml.jackson.databind.deser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.NoClass;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.util.StdConverter;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: DeserializerCache
 *
 * PARTITION A: Core Functional Logic & State Transitions
 * - findValueDeserializer():
 *     - Initial miss -> factory creation -> cache addition.
 *     - Hit from cache (_findCachedDeserializer).
 *     - Enum deserializers (factory.createEnumDeserializer).
 *     - Array deserializers (factory.createArrayDeserializer).
 *     - True Map deserializers (factory.createMapDeserializer).
 *     - True Collection deserializers (factory.createCollectionDeserializer).
 *     - JsonNode tree deserializers (factory.createTreeDeserializer).
 *     - Standard Bean deserializers (factory.createBeanDeserializer).
 *     - Resolvable cyclic deserializers (Bean referencing itself).
 * - findKeyDeserializer():
 *     - Successful key deserializer resolution (e.g., String key).
 *     - Resolvable KeyDeserializer resolution trigger.
 * - hasValueDeserializerFor():
 *     - Uncached and cached query verification.
 * - Cache management:
 *     - cachedDeserializersCount() & flushCachedDeserializers() state cycle.
 *
 * PARTITION B: Boundary Value Analysis (BVA) & Extremes
 * - _findCachedDeserializer(null): Null check guard -> IllegalArgumentException.
 * - _hasCustomValueHandler(): Container types with null vs. non-null custom value/type handlers.
 * - _verifyAsClass():
 *     - src == null -> returns null.
 *     - src == noneClass -> returns null.
 *     - src == NoClass.class (isBogusClass) -> returns null.
 *     - src not Class -> throws IllegalStateException.
 *     - valid class -> returns Class<?>.
 *
 * PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
 * - testByteArrayTypeOverride890:
 *     - Target bug: TestArrayDeserialization::testByteArrayTypeOverride890
 *     - In modifyTypeByAnnotation(), type.narrowBy() used to create SimpleType for array classes
 *       instead of ArrayType, subsequently routing byte[].class to BeanDeserializerFactory and failing
 *       with: "Can not deserialize Class [B (of type array) as a Bean".
 *     - Validates property annotation type override (@JsonDeserialize(as = byte[].class)).
 *
 * PARTITION D: Exception & Defensive Guard Paths
 * - _handleUnknownValueDeserializer():
 *     - Abstract raw class -> throws JsonMappingException ("for abstract type").
 *     - Concrete raw class -> throws JsonMappingException ("for type").
 * - _handleUnknownKeyDeserializer():
 *     - Missing key deserializer -> throws JsonMappingException ("(Map) Key deserializer").
 * - modifyTypeByAnnotation():
 *     - Invalid key-type annotation on non-map container -> JsonMappingException ("Illegal key-type annotation").
 *
 * PARTITION E: Annotations, Conversions & Object Lifecycle
 * - findDeserializerFromAnnotation(): @JsonDeserialize(using = ...) resolution.
 * - findConvertingDeserializer(): @JsonDeserialize(converter = ...) wrapping StdDelegatingDeserializer.
 * - POJO Builder: @JsonDeserialize(builder = ...) calling factory.createBuilderBasedDeserializer.
 * - JsonFormat(shape = Shape.OBJECT) on CollectionLikeType bypassing regular collection deserializer.
 * - writeReplace() lifecycle & JDK serialization integrity.
 * ----------------------------------------------------------------------------------------------------
 */
public class DeserializerCacheGeminiTest {

    /*
    /**********************************************************
    /* Test Helper Dummy Classes & Annotations
    /**********************************************************
     */

    public static class SimpleBean {
        public int id;
        public String name;
    }

    public enum SampleEnum {
        VALUE_A, VALUE_B
    }

    public static class CyclicNode {
        public int id;
        public CyclicNode next;
    }

    public static class NoKeyDeserClass {
        private NoKeyDeserClass(int a, int b) { }
    }

    public static class CustomDeserClass {
        public int value;
    }

    public static class CustomDeser extends JsonDeserializer<CustomDeserClass> {
        @Override
        public CustomDeserClass deserialize(JsonParser p, DeserializationContext ctxt) {
            CustomDeserClass obj = new CustomDeserClass();
            obj.value = 42;
            return obj;
        }
    }

    @JsonDeserialize(using = CustomDeser.class)
    public static class AnnotatedWithCustomDeser {
        public int value;
    }

    public static class ConvertedTarget {
        public String parsed;
        public ConvertedTarget(String p) { this.parsed = p; }
    }

    public static class StringToTargetConverter extends StdConverter<String, ConvertedTarget> {
        @Override
        public ConvertedTarget convert(String value) {
            return new ConvertedTarget(value);
        }
    }

    @JsonDeserialize(converter = StringToTargetConverter.class)
    public static class AnnotatedWithConverter {
    }

    @JsonDeserialize(builder = POJOWithBuilder.Builder.class)
    public static class POJOWithBuilder {
        public final int x;
        protected POJOWithBuilder(int x) { this.x = x; }

        @JsonPOJOBuilder(withPrefix = "with")
        public static class Builder {
            private int x;
            public Builder withX(int val) { this.x = val; return this; }
            public POJOWithBuilder build() { return new POJOWithBuilder(x); }
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public static class ObjectShapeCollection extends ArrayList<String> {
        private static final long serialVersionUID = 1L;
        public int extraField;
    }

    @JsonDeserialize(keyAs = String.class)
    public static class NonMapWithKeyAs extends ArrayList<String> {
        private static final long serialVersionUID = 1L;
    }

    public static class DummyCustomKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return key;
        }
    }

    @JsonDeserialize(keyUsing = DummyCustomKeyDeserializer.class)
    public static class MapWithCustomKeyDeser extends HashMap<String, String> {
        private static final long serialVersionUID = 1L;
    }

    public static class ContentCustomDeser extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) {
            return "customContent";
        }
    }

    @JsonDeserialize(contentUsing = ContentCustomDeser.class)
    public static class ListWithCustomContentDeser extends ArrayList<String> {
        private static final long serialVersionUID = 1L;
    }

    // Reproduction structure for Defects4J known issue [databind#890]
    public static class ByteArrayWrapper890 {
        @JsonDeserialize(as = byte[].class)
        public Object value;
    }

    private ObjectMapper newObjectMapper() {
        return new ObjectMapper();
    }

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testFindValueDeserializerBasicAndCaching() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = ctxt.getFactory();

        JavaType type = mapper.constructType(SimpleBean.class);

        assertEquals(0, cache.cachedDeserializersCount());
        JsonDeserializer<Object> deser1 = cache.findValueDeserializer(ctxt, factory, type);
        assertNotNull(deser1);
        assertEquals(1, cache.cachedDeserializersCount());

        // Second lookup must be served from cache
        JsonDeserializer<Object> deser2 = cache.findValueDeserializer(ctxt, factory, type);
        assertSame(deser1, deser2);
        assertEquals(1, cache.cachedDeserializersCount());

        // Verify flush
        cache.flushCachedDeserializers();
        assertEquals(0, cache.cachedDeserializersCount());
    }

    @Test(timeout = 4000)
    public void testFindValueDeserializerVarietyTypes() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = ctxt.getFactory();

        // Enum deserializer
        JavaType enumType = mapper.constructType(SampleEnum.class);
        JsonDeserializer<Object> enumDeser = cache.findValueDeserializer(ctxt, factory, enumType);
        assertNotNull(enumDeser);

        // Array deserializer
        JavaType arrayType = mapper.constructType(String[].class);
        JsonDeserializer<Object> arrayDeser = cache.findValueDeserializer(ctxt, factory, arrayType);
        assertNotNull(arrayDeser);

        // True Map deserializer
        JavaType mapType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
        JsonDeserializer<Object> mapDeser = cache.findValueDeserializer(ctxt, factory, mapType);
        assertNotNull(mapDeser);

        // True Collection deserializer
        JavaType colType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, String.class);
        JsonDeserializer<Object> colDeser = cache.findValueDeserializer(ctxt, factory, colType);
        assertNotNull(colDeser);

        // JsonNode Tree deserializer
        JavaType nodeType = mapper.constructType(JsonNode.class);
        JsonDeserializer<Object> nodeDeser = cache.findValueDeserializer(ctxt, factory, nodeType);
        assertNotNull(nodeDeser);
    }

    @Test(timeout = 4000)
    public void testCyclicDependencyResolution() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = ctxt.getFactory();

        JavaType type = mapper.constructType(CyclicNode.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, factory, type);
        assertNotNull(deser);
    }

    @Test(timeout = 4000)
    public void testHasValueDeserializerFor() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = ctxt.getFactory();

        JavaType type = mapper.constructType(SimpleBean.class);
        // Initially not in cache
        assertTrue(cache.hasValueDeserializerFor(ctxt, factory, type));
        // Second call with cached deserializer
        assertTrue(cache.hasValueDeserializerFor(ctxt, factory, type));
    }

    @Test(timeout = 4000)
    public void testFindKeyDeserializerSuccess() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = ctxt.getFactory();

        JavaType strType = mapper.constructType(String.class);
        KeyDeserializer kd = cache.findKeyDeserializer(ctxt, factory, strType);
        assertNotNull(kd);
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Extremes
    /**********************************************************
     */

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFindCachedDeserializerNullType() {
        DeserializerCache cache = new DeserializerCache();
        cache._findCachedDeserializer(null);
    }

    @Test(timeout = 4000)
    public void testHasCustomValueHandlerBypass() {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();

        JavaType ctWithValue = mapper.getTypeFactory().constructType(String.class).withValueHandler("dummyHandler");
        ArrayType arrayWithValueHandler = mapper.getTypeFactory().constructArrayType(ctWithValue);
        assertNull(cache._findCachedDeserializer(arrayWithValueHandler));

        JavaType ctWithType = mapper.getTypeFactory().constructType(String.class).withTypeHandler("dummyHandler");
        ArrayType arrayWithTypeHandler = mapper.getTypeFactory().constructArrayType(ctWithType);
        assertNull(cache._findCachedDeserializer(arrayWithTypeHandler));

        JavaType normalArray = mapper.getTypeFactory().constructArrayType(String.class);
        assertNull(cache._findCachedDeserializer(normalArray));
    }

    @Test(timeout = 4000)
    public void testVerifyAsClass() {
        DeserializerCache cache = new DeserializerCache();

        assertNull(cache._verifyAsClass(null, "testMethod", JsonDeserializer.None.class));
        assertNull(cache._verifyAsClass(JsonDeserializer.None.class, "testMethod", JsonDeserializer.None.class));
        assertNull(cache._verifyAsClass(NoClass.class, "testMethod", JsonDeserializer.None.class));

        Class<?> valid = cache._verifyAsClass(String.class, "testMethod", JsonDeserializer.None.class);
        assertEquals(String.class, valid);

        try {
            cache._verifyAsClass("NotAClassInstance", "testMethod", JsonDeserializer.None.class);
            fail("Expected IllegalStateException for non-class input");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("expected type JsonSerializer or Class<JsonSerializer>"));
        }
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone (Defects4J #890)
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testByteArrayTypeOverride890() throws Exception {
        // Targets known defect where byte[].class type override was mishandled as a Bean
        // instead of an ArrayType, leading to: "Can not deserialize Class [B (of type array) as a Bean"
        ObjectMapper mapper = newObjectMapper();
        String json = "{\"value\":\"AQID\"}";
        ByteArrayWrapper890 result = mapper.readValue(json, ByteArrayWrapper890.class);

        assertNotNull(result);
        assertNotNull(result.value);
        assertTrue("Expected byte[] but got: " + result.value.getClass().getName(),
                result.value instanceof byte[]);
        byte[] bytes = (byte[]) result.value;
        assertArrayEquals(new byte[] { 1, 2, 3 }, bytes);
    }

    @Test(timeout = 4000)
    public void testByteArrayTypeOverrideDirectCacheResolution() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = ctxt.getFactory();

        JavaType type = mapper.constructType(ByteArrayWrapper890.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, factory, type);
        assertNotNull(deser);
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testHandleUnknownValueDeserializer() {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();

        // Abstract type path
        JavaType abstractType = mapper.constructType(CharSequence.class);
        try {
            cache._handleUnknownValueDeserializer(abstractType);
            fail("Expected JsonMappingException for abstract type");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not find a Value deserializer for abstract type"));
        }

        // Concrete type path
        JavaType concreteType = mapper.constructType(String.class);
        try {
            cache._handleUnknownValueDeserializer(concreteType);
            fail("Expected JsonMappingException for concrete type");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not find a Value deserializer for type"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleUnknownKeyDeserializer() {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();

        JavaType type = mapper.constructType(NoKeyDeserClass.class);
        try {
            cache._handleUnknownKeyDeserializer(type);
            fail("Expected JsonMappingException for unknown key deserializer");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not find a (Map) Key deserializer for type"));
        }
    }

    @Test(timeout = 4000)
    public void testFindKeyDeserializerFailure() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = ctxt.getFactory();

        JavaType type = mapper.constructType(NoKeyDeserClass.class);
        try {
            cache.findKeyDeserializer(ctxt, factory, type);
            fail("Expected JsonMappingException for type without KeyDeserializer");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("Can not find a (Map) Key deserializer"));
        }
    }

    @Test(timeout = 4000)
    public void testModifyTypeByAnnotationIllegalKeyTypeOnNonMap() {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = ctxt.getFactory();

        JavaType invalidType = mapper.constructType(NonMapWithKeyAs.class);
        try {
            cache.findValueDeserializer(ctxt, factory, invalidType);
            fail("Expected JsonMappingException for illegal key-type annotation on Collection");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal key-type annotation: type"));
        }
    }

    /*
    /**********************************************************
    /* Partition E: Object Lifecycle, Annotations & Contracts
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testAnnotatedCustomDeserializer() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = ctxt.getFactory();

        JavaType type = mapper.constructType(AnnotatedWithCustomDeser.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, factory, type);
        assertNotNull(deser);
        assertTrue(deser instanceof CustomDeser);
    }

    @Test(timeout = 4000)
    public void testAnnotatedConverter() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = ctxt.getFactory();

        JavaType type = mapper.constructType(AnnotatedWithConverter.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, factory, type);
        assertNotNull(deser);
        assertTrue(deser instanceof StdDelegatingDeserializer);
    }

    @Test(timeout = 4000)
    public void testPOJOBuilderDeserializer() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = ctxt.getFactory();

        JavaType type = mapper.constructType(POJOWithBuilder.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, factory, type);
        assertNotNull(deser);
    }

    @Test(timeout = 4000)
    public void testCollectionShapeObjectBranch() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = ctxt.getFactory();

        JavaType type = mapper.constructType(ObjectShapeCollection.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, factory, type);
        assertNotNull(deser);
        // JsonFormat.Shape.OBJECT should bypass collection deserializer and use bean deserializer
        assertFalse(deser instanceof com.fasterxml.jackson.databind.deser.std.CollectionDeserializer);
    }

    @Test(timeout = 4000)
    public void testMapWithCustomKeyDeserializerAnnotation() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = ctxt.getFactory();

        JavaType type = mapper.constructType(MapWithCustomKeyDeser.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, factory, type);
        assertNotNull(deser);
    }

    @Test(timeout = 4000)
    public void testListWithCustomContentDeserializerAnnotation() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = ctxt.getFactory();

        JavaType type = mapper.constructType(ListWithCustomContentDeser.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, factory, type);
        assertNotNull(deser);
    }

    @Test(timeout = 4000)
    public void testWriteReplaceAndSerialization() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = newObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerFactory factory = ctxt.getFactory();

        cache.findValueDeserializer(ctxt, factory, mapper.constructType(SimpleBean.class));
        assertTrue(cache.cachedDeserializersCount() > 0);

        Object writeReplaced = cache.writeReplace();
        assertSame(cache, writeReplaced);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(cache);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        assertNotNull(deserialized);
        assertTrue(deserialized instanceof DeserializerCache);
    }
}