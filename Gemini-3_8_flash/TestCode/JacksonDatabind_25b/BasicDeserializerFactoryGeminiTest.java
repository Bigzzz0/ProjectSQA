package com.fasterxml.jackson.databind.deser;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonValueInstantiator;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: BasicDeserializerFactory and BeanDeserializerFactory hierarchy.
 *
 * Decision / Branch Coverage Points:
 * 1. Configuration fluent builder methods (withAdditionalDeserializers, withAdditionalKeyDeserializers,
 *    withDeserializerModifier, withAbstractTypeResolver, withValueInstantiators).
 * 2. mapAbstractType: loop resolution, null check, cycle/non-subtype guard (prevCls == nextCls || !prevCls.isAssignableFrom(nextCls)).
 * 3. findValueInstantiator:
 *    - @JsonValueInstantiator on class
 *    - _findStdValueInstantiator (JsonLocation)
 *    - _constructDefaultValueInstantiator
 *    - post-modifiers (null instantiator sanity check)
 *    - incomplete creator parameter check (throws IllegalArgumentException)
 * 4. _valueInstantiatorInstance: null check, instance check, non-class type, bogus class, non-assignable class, reflection inst.
 * 5. Single and multi-arg constructors/factories: primitive/wrapper types (String, int, long, double, boolean),
 *    delegating creators, property-based creators, non-static inner class @JsonCreator restriction.
 * 6. createArrayDeserializer: primitive array (int[], byte[], etc.), String[], ObjectArray, custom array deserializer.
 * 7. createCollectionDeserializer: EnumSet, abstract fallbacks (List, Set, Queue, Deque, NavigableSet),
 *    ArrayBlockingQueue, StringCollection, generic Collection, non-concrete failure.
 * 8. createMapDeserializer: EnumMap (with valid/invalid key), abstract fallbacks (Map, ConcurrentMap, SortedMap,
 *    NavigableMap, ConcurrentNavigableMap), non-concrete failure.
 * 9. createEnumDeserializer: standard enum, @JsonCreator static factory, @JsonValue method, unsuitable creator method.
 * 10. createKeyDeserializer: Enum key with creator/value, string-based keys.
 * 11. findDefaultDeserializer: Object ("untyped"), String, CharSequence, AtomicReference, Iterable, Map.Entry,
 *     numbers, dates, TokenBuffer, JdkDeserializers.
 * 12. modifyTypeByAnnotation: class narrowing, key narrowing (and non-Map exception), content narrowing.
 *
 * Defect-Targeted Ground Truth:
 * - Issue #890 / testByteArrayTypeOverride890: @JsonDeserialize(as=byte[].class) on Object field
 *   failed with JsonMappingException ("Can not deserialize Class [B (of type array) as a Bean").
 */
public class BasicDeserializerFactoryGeminiTest {

    // Concrete test implementation of BasicDeserializerFactory to test life-cycle and internal mechanics
    static class TestBasicDeserializerFactory extends BasicDeserializerFactory {
        private static final long serialVersionUID = 1L;

        public TestBasicDeserializerFactory(DeserializerFactoryConfig config) {
            super(config);
        }

        @Override
        protected DeserializerFactory withConfig(DeserializerFactoryConfig config) {
            return new TestBasicDeserializerFactory(config);
        }

        @Override
        public JsonDeserializer<Object> createBeanDeserializer(DeserializationContext ctxt,
                JavaType type, BeanDescription beanDesc) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonDeserializer<Object> createBuilderBasedDeserializer(DeserializationContext ctxt,
                JavaType type, BeanDescription beanDesc, Class<?> builderClass) throws JsonMappingException {
            return null;
        }
    }

    // Helper POJOs and Enums
    static class CustomValueInstantiator extends StdValueInstantiator {
        private static final long serialVersionUID = 1L;
        public CustomValueInstantiator() {
            super(null, Object.class);
        }
    }

    @JsonValueInstantiator(CustomValueInstantiator.class)
    static class AnnotatedWithInstantiator {
        public String value;
    }

    static class CustomList<E> extends ArrayList<E> {
        private static final long serialVersionUID = 1L;
    }

    abstract static class AbstractCustomCollection<E> implements Collection<E> {
    }

    abstract static class AbstractCustomMap<K, V> implements Map<K, V> {
    }

    enum TestEnum {
        A, B, C;
    }

    enum EnumWithCreator {
        ALPHA, BETA;

        @JsonCreator
        public static EnumWithCreator fromString(String val) {
            if ("a".equalsIgnoreCase(val)) return ALPHA;
            return BETA;
        }
    }

    enum EnumWithJsonValue {
        FIRST("1"), SECOND("2");
        private final String code;
        EnumWithJsonValue(String c) { this.code = c; }

        @JsonValue
        public String getCode() { return code; }
    }

    enum EnumWithBadCreator {
        X, Y;

        @JsonCreator
        public static EnumWithBadCreator badFactory(int x, int y) {
            return X;
        }
    }

    enum EnumWithBadKeyCreator {
        FOO, BAR;

        @JsonCreator
        public static EnumWithBadKeyCreator make(Integer i) {
            return FOO;
        }
    }

    static class SingleArgCtors {
        String s;
        int i;
        long l;
        double d;
        boolean b;

        public SingleArgCtors(String s) { this.s = s; }
        public SingleArgCtors(int i) { this.i = i; }
        public SingleArgCtors(long l) { this.l = l; }
        public SingleArgCtors(double d) { this.d = d; }
        public SingleArgCtors(boolean b) { this.b = b; }
    }

    static class SingleArgFactories {
        final Object val;
        private SingleArgFactories(Object v) { this.val = v; }

        public static SingleArgFactories create(String s) { return new SingleArgFactories(s); }
        public static SingleArgFactories create(int i) { return new SingleArgFactories(i); }
        public static SingleArgFactories create(long l) { return new SingleArgFactories(l); }
        public static SingleArgFactories create(double d) { return new SingleArgFactories(d); }
        public static SingleArgFactories create(boolean b) { return new SingleArgFactories(b); }
    }

    static class UnnamedMultiParamCreator {
        @JsonCreator
        public UnnamedMultiParamCreator(int a, String b) {}
    }

    public class NonStaticInner {
        @JsonCreator
        public NonStaticInner(String a, int b) {}
    }

    static class ByteArrayWrapper890 {
        @JsonDeserialize(as = byte[].class)
        public Object value;
    }

    static class ByteArrayWrapperWithSetter890 {
        private Object value;

        @JsonDeserialize(as = byte[].class)
        public void setValue(Object val) {
            this.value = val;
        }

        public Object getValue() {
            return value;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactoryConfigImmutabilityAndChaining() {
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        BasicDeserializerFactory factory = new TestBasicDeserializerFactory(config);
        assertSame(config, factory.getFactoryConfig());

        DeserializerFactory f1 = factory.withAdditionalDeserializers(new Deserializers.Base());
        assertNotSame(factory, f1);

        DeserializerFactory f2 = factory.withAdditionalKeyDeserializers(new SimpleKeyDeserializers());
        assertNotSame(factory, f2);

        DeserializerFactory f3 = factory.withDeserializerModifier(new BeanDeserializerModifier());
        assertNotSame(factory, f3);

        DeserializerFactory f4 = factory.withAbstractTypeResolver(new SimpleAbstractTypeResolver());
        assertNotSame(factory, f4);

        DeserializerFactory f5 = factory.withValueInstantiators(new SimpleValueInstantiators());
        assertNotSame(factory, f5);
    }

    @Test(timeout = 4000)
    public void testFindDefaultDeserializerPrimitivesAndJdkTypes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        // Object ("untyped")
        JavaType objType = mapper.constructType(Object.class);
        JsonDeserializer<?> deser = factory.findDefaultDeserializer(ctxt, objType, mapper.getDeserializationConfig().introspect(objType));
        assertNotNull(deser);

        // String and CharSequence
        JavaType strType = mapper.constructType(String.class);
        assertNotNull(factory.findDefaultDeserializer(ctxt, strType, mapper.getDeserializationConfig().introspect(strType)));

        JavaType charSeqType = mapper.constructType(CharSequence.class);
        assertNotNull(factory.findDefaultDeserializer(ctxt, charSeqType, mapper.getDeserializationConfig().introspect(charSeqType)));

        // AtomicReference
        JavaType refType = mapper.getTypeFactory().constructReferenceType(AtomicReference.class, strType);
        assertNotNull(factory.findDefaultDeserializer(ctxt, refType, mapper.getDeserializationConfig().introspect(refType)));

        // Iterable
        JavaType iterType = mapper.constructType(Iterable.class);
        assertNotNull(factory.findDefaultDeserializer(ctxt, iterType, mapper.getDeserializationConfig().introspect(iterType)));

        // Map.Entry
        JavaType entryType = mapper.getTypeFactory().constructMapLikeType(Map.Entry.class, strType, objType);
        assertNotNull(factory.findDefaultDeserializer(ctxt, entryType, mapper.getDeserializationConfig().introspect(entryType)));

        // TokenBuffer
        JavaType tbType = mapper.constructType(TokenBuffer.class);
        assertNotNull(factory.findDefaultDeserializer(ctxt, tbType, mapper.getDeserializationConfig().introspect(tbType)));

        // Numbers & Dates
        JavaType intType = mapper.constructType(int.class);
        assertNotNull(factory.findDefaultDeserializer(ctxt, intType, mapper.getDeserializationConfig().introspect(intType)));

        JavaType dateType = mapper.constructType(Date.class);
        assertNotNull(factory.findDefaultDeserializer(ctxt, dateType, mapper.getDeserializationConfig().introspect(dateType)));

        JavaType uuidType = mapper.constructType(UUID.class);
        assertNotNull(factory.findDefaultDeserializer(ctxt, uuidType, mapper.getDeserializationConfig().introspect(uuidType)));
    }

    @Test(timeout = 4000)
    public void testCreateArrayDeserializers() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("[]"), null);
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        // Primitive arrays: int[], boolean[], byte[], double[]
        Class<?>[] primClasses = new Class<?>[] { int[].class, boolean[].class, byte[].class, double[].class };
        for (Class<?> clz : primClasses) {
            ArrayType arrayType = mapper.getTypeFactory().constructArrayType(clz.getComponentType());
            JsonDeserializer<?> deser = factory.createArrayDeserializer(ctxt, arrayType,
                    mapper.getDeserializationConfig().introspect(arrayType));
            assertNotNull("Deserializer for " + clz.getName() + " should not be null", deser);
        }

        // String[]
        ArrayType strArrayType = mapper.getTypeFactory().constructArrayType(String.class);
        JsonDeserializer<?> strDeser = factory.createArrayDeserializer(ctxt, strArrayType,
                mapper.getDeserializationConfig().introspect(strArrayType));
        assertNotNull(strDeser);

        // Object[]
        ArrayType objArrayType = mapper.getTypeFactory().constructArrayType(Object.class);
        JsonDeserializer<?> objDeser = factory.createArrayDeserializer(ctxt, objArrayType,
                mapper.getDeserializationConfig().introspect(objArrayType));
        assertNotNull(objDeser);
    }

    @Test(timeout = 4000)
    public void testCollectionFallbacksAndSpecialCollections() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("[]"), null);
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        // Abstract interfaces fallbacks: Collection, List, Set, SortedSet, Queue, Deque, NavigableSet
        Class<?>[] collInterfaces = new Class<?>[] {
            Collection.class, List.class, Set.class, SortedSet.class, Queue.class, Deque.class, NavigableSet.class
        };
        for (Class<?> iface : collInterfaces) {
            CollectionType type = mapper.getTypeFactory().constructCollectionType((Class<? extends Collection>) iface, String.class);
            JsonDeserializer<?> deser = factory.createCollectionDeserializer(ctxt, type,
                    mapper.getDeserializationConfig().introspect(type));
            assertNotNull("Should create fallback collection deserializer for " + iface.getName(), deser);
        }

        // EnumSet
        CollectionType enumSetType = mapper.getTypeFactory().constructCollectionType(EnumSet.class, TestEnum.class);
        JsonDeserializer<?> enumSetDeser = factory.createCollectionDeserializer(ctxt, enumSetType,
                mapper.getDeserializationConfig().introspect(enumSetType));
        assertNotNull(enumSetDeser);

        // ArrayBlockingQueue (non-default creator)
        CollectionType abqType = mapper.getTypeFactory().constructCollectionType(ArrayBlockingQueue.class, Integer.class);
        JsonDeserializer<?> abqDeser = factory.createCollectionDeserializer(ctxt, abqType,
                mapper.getDeserializationConfig().introspect(abqType));
        assertNotNull(abqDeser);
    }

    @Test(timeout = 4000)
    public void testMapFallbacksAndEnumMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        // Map fallbacks: Map, ConcurrentMap, SortedMap, NavigableMap, ConcurrentNavigableMap
        Class<?>[] mapInterfaces = new Class<?>[] {
            Map.class, ConcurrentMap.class, SortedMap.class, NavigableMap.class, ConcurrentNavigableMap.class
        };
        for (Class<?> iface : mapInterfaces) {
            MapType type = mapper.getTypeFactory().constructMapType((Class<? extends Map>) iface, String.class, Object.class);
            JsonDeserializer<?> deser = factory.createMapDeserializer(ctxt, type,
                    mapper.getDeserializationConfig().introspect(type));
            assertNotNull("Should create fallback map deserializer for " + iface.getName(), deser);
        }

        // EnumMap
        MapType enumMapType = mapper.getTypeFactory().constructMapType(EnumMap.class, TestEnum.class, String.class);
        JsonDeserializer<?> enumMapDeser = factory.createMapDeserializer(ctxt, enumMapType,
                mapper.getDeserializationConfig().introspect(enumMapType));
        assertNotNull(enumMapDeser);
    }

    @Test(timeout = 4000)
    public void testEnumDeserializersAndKeyDeserializers() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("\"A\""), null);
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        // Standard Enum
        JavaType enumType = mapper.constructType(TestEnum.class);
        assertNotNull(factory.createEnumDeserializer(ctxt, enumType, mapper.getDeserializationConfig().introspect(enumType)));
        assertNotNull(factory.createKeyDeserializer(ctxt, enumType));

        // Enum with @JsonCreator
        JavaType creatorEnumType = mapper.constructType(EnumWithCreator.class);
        assertNotNull(factory.createEnumDeserializer(ctxt, creatorEnumType, mapper.getDeserializationConfig().introspect(creatorEnumType)));
        assertNotNull(factory.createKeyDeserializer(ctxt, creatorEnumType));

        // Enum with @JsonValue
        JavaType jsonValEnumType = mapper.constructType(EnumWithJsonValue.class);
        assertNotNull(factory.createEnumDeserializer(ctxt, jsonValEnumType, mapper.getDeserializationConfig().introspect(jsonValEnumType)));
        assertNotNull(factory.createKeyDeserializer(ctxt, jsonValEnumType));
    }

    @Test(timeout = 4000)
    public void testTreeDeserializers() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        JavaType nodeType = mapper.constructType(JsonNode.class);
        assertNotNull(factory.createTreeDeserializer(config, nodeType, config.introspect(nodeType)));

        JavaType objNodeType = mapper.constructType(ObjectNode.class);
        assertNotNull(factory.createTreeDeserializer(config, objNodeType, config.introspect(objNodeType)));

        JavaType arrNodeType = mapper.constructType(ArrayNode.class);
        assertNotNull(factory.createTreeDeserializer(config, arrNodeType, config.introspect(arrNodeType)));
    }

    @Test(timeout = 4000)
    public void testValueInstantiatorResolution() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        // JsonLocation standard instantiator
        JavaType locType = mapper.constructType(JsonLocation.class);
        ValueInstantiator locInst = factory.findValueInstantiator(ctxt, mapper.getDeserializationConfig().introspect(locType));
        assertNotNull(locInst);

        // Annotated with @JsonValueInstantiator
        JavaType annType = mapper.constructType(AnnotatedWithInstantiator.class);
        ValueInstantiator annInst = factory.findValueInstantiator(ctxt, mapper.getDeserializationConfig().introspect(annType));
        assertNotNull(annInst);
        assertTrue(annInst instanceof CustomValueInstantiator);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFindJsonValueForNull() {
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;
        assertNull(factory._findJsonValueFor(new ObjectMapper().getDeserializationConfig(), null));
    }

    @Test(timeout = 4000)
    public void testValueInstantiatorInstanceWithBogusAndNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        // null definition
        assertNull(factory._valueInstantiatorInstance(config, null, null));

        // direct ValueInstantiator instance
        ValueInstantiator inst = new CustomValueInstantiator();
        assertSame(inst, factory._valueInstantiatorInstance(config, null, inst));

        // bogus class (JsonValueInstantiator.None)
        assertNull(factory._valueInstantiatorInstance(config, null, com.fasterxml.jackson.annotation.JsonValueInstantiator.class));
    }

    @Test(timeout = 4000)
    public void testMapAbstractTypeIdentityWhenNoMapping() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        JavaType type = mapper.constructType(CharSequence.class);
        JavaType mapped = factory.mapAbstractType(config, type);
        assertSame(type, mapped);
    }

    @Test(timeout = 4000)
    public void testMapAbstractCollectionTypeUnknown() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        JavaType type = mapper.constructType(AbstractCustomCollection.class);
        assertNull(factory._mapAbstractCollectionType(type, config));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Issue #890: byte[] override)
    // =========================================================================

    @Test(timeout = 4000)
    public void testByteArrayTypeOverride890() throws Exception {
        // Targets known defect where @JsonDeserialize(as=byte[].class) failed with:
        // "Can not deserialize Class [B (of type array) as a Bean"
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayWrapper890 w = mapper.readValue("{\"value\":\"AQID\"}", ByteArrayWrapper890.class);
        assertNotNull(w);
        assertNotNull(w.value);
        assertTrue("value should be deserialized as byte[]", w.value instanceof byte[]);
        assertArrayEquals(new byte[] { 1, 2, 3 }, (byte[]) w.value);
    }

    @Test(timeout = 4000)
    public void testByteArrayTypeOverrideWithSetter890() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayWrapperWithSetter890 w = mapper.readValue("{\"value\":\"BAUG\"}", ByteArrayWrapperWithSetter890.class);
        assertNotNull(w);
        assertNotNull(w.getValue());
        assertTrue("value should be byte[]", w.getValue() instanceof byte[]);
        assertArrayEquals(new byte[] { 4, 5, 6 }, (byte[]) w.getValue());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testMapAbstractTypeCycleOrInvalidSubtype() {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        // Invalid resolution: CharSequence -> Integer (Integer is not a subtype of CharSequence)
        resolver.addMapping(CharSequence.class, Integer.class);

        BasicDeserializerFactory factory = (BasicDeserializerFactory) BeanDeserializerFactory.instance
                .withAbstractTypeResolver(resolver);
        ObjectMapper mapper = new ObjectMapper();

        try {
            factory.mapAbstractType(mapper.getDeserializationConfig(), mapper.constructType(CharSequence.class));
            fail("Expected IllegalArgumentException for invalid abstract type resolution");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not a subtype of former"));
        } catch (JsonMappingException e) {
            fail("Unexpected JsonMappingException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testValueInstantiatorInstanceWithInvalidTypes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        // instDef is not a Class or ValueInstantiator (e.g., a String)
        try {
            factory._valueInstantiatorInstance(config, null, "invalidString");
            fail("Expected IllegalStateException for non-class, non-ValueInstantiator definition");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("expected type KeyDeserializer or Class<KeyDeserializer>"));
        }

        // instDef is a Class not implementing ValueInstantiator
        try {
            factory._valueInstantiatorInstance(config, null, String.class);
            fail("Expected IllegalStateException for Class not implementing ValueInstantiator");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("expected Class<ValueInstantiator>"));
        }
    }

    @Test(timeout = 4000)
    public void testEnumMapWithoutEnumKeyThrows() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        MapType badEnumMap = mapper.getTypeFactory().constructMapType(EnumMap.class, String.class, String.class);
        try {
            factory.createMapDeserializer(ctxt, badEnumMap, mapper.getDeserializationConfig().introspect(badEnumMap));
            fail("Expected IllegalArgumentException when key of EnumMap is not Enum");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("generic (key) type not available"));
        } catch (JsonMappingException e) {
            fail("Unexpected JsonMappingException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNonConcreteCollectionWithoutTypeHandlerThrows() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("[]"), null);
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        CollectionType customColl = mapper.getTypeFactory().constructCollectionType(AbstractCustomCollection.class, String.class);
        try {
            factory.createCollectionDeserializer(ctxt, customColl, mapper.getDeserializationConfig().introspect(customColl));
            fail("Expected IllegalArgumentException for non-concrete collection without fallback");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not find a deserializer for non-concrete Collection"));
        } catch (JsonMappingException e) {
            fail("Unexpected JsonMappingException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNonConcreteMapWithoutTypeHandlerThrows() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        MapType customMap = mapper.getTypeFactory().constructMapType(AbstractCustomMap.class, String.class, String.class);
        try {
            factory.createMapDeserializer(ctxt, customMap, mapper.getDeserializationConfig().introspect(customMap));
            fail("Expected IllegalArgumentException for non-concrete map without fallback");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not find a deserializer for non-concrete Map"));
        } catch (JsonMappingException e) {
            fail("Unexpected JsonMappingException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testEnumWithBadCreatorThrows() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("\"X\""), null);
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        JavaType enumType = mapper.constructType(EnumWithBadCreator.class);
        try {
            factory.createEnumDeserializer(ctxt, enumType, mapper.getDeserializationConfig().introspect(enumType));
            fail("Expected IllegalArgumentException for Enum with multi-argument creator");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unsuitable method"));
        } catch (JsonMappingException e) {
            fail("Unexpected JsonMappingException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testEnumWithBadKeyCreatorThrows() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("\"FOO\""), null);
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        JavaType enumType = mapper.constructType(EnumWithBadKeyCreator.class);
        try {
            factory.createKeyDeserializer(ctxt, enumType);
            fail("Expected IllegalArgumentException for Enum key creator expecting non-String parameter");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must be java.lang.String"));
        } catch (JsonMappingException e) {
            fail("Unexpected JsonMappingException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testBrokenValueInstantiatorsReturningNull() throws Exception {
        SimpleValueInstantiators brokenInstantiators = new SimpleValueInstantiators() {
            private static final long serialVersionUID = 1L;
            @Override
            public ValueInstantiator findValueInstantiator(DeserializationConfig config,
                    BeanDescription beanDesc, ValueInstantiator defaultInstantiator) {
                return null; // Violates contract
            }
        };

        BasicDeserializerFactory factory = (BasicDeserializerFactory) BeanDeserializerFactory.instance
                .withValueInstantiators(brokenInstantiators);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);

        JavaType type = mapper.constructType(SingleArgCtors.class);
        try {
            factory.findValueInstantiator(ctxt, mapper.getDeserializationConfig().introspect(type));
            fail("Expected JsonMappingException for broken ValueInstantiators returning null");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("returned null ValueInstantiator"));
        }
    }

    @Test(timeout = 4000)
    public void testUnnamedMultiParamCreatorThrows() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"a\":1,\"b\":\"str\"}", UnnamedMultiParamCreator.class);
            fail("Expected IllegalArgumentException / JsonMappingException for multi-parameter creator without names");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("has no property name annotation"));
        } catch (JsonMappingException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertTrue(e.getCause().getMessage().contains("has no property name annotation"));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test(timeout = 4000)
    public void testNonStaticInnerClassCreatorThrows() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"a\":\"str\",\"b\":1}", NonStaticInner.class);
            fail("Expected IllegalArgumentException for @JsonCreator on non-static inner class constructor");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("can not use @JsonCreator for constructors"));
        } catch (JsonMappingException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertTrue(e.getCause().getMessage().contains("can not use @JsonCreator for constructors"));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCustomSubclassContract() {
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        TestBasicDeserializerFactory factory = new TestBasicDeserializerFactory(config);

        DeserializerFactory withAdded = factory.withAdditionalDeserializers(new Deserializers.Base());
        assertNotNull(withAdded);
        assertTrue(withAdded instanceof TestBasicDeserializerFactory);
        assertNotSame(factory, withAdded);
        assertTrue(((TestBasicDeserializerFactory) withAdded).getFactoryConfig().hasDeserializers());
    }

    @Test(timeout = 4000)
    public void testSingleArgConstructorsAndFactoriesDetection() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;

        JavaType ctorType = mapper.constructType(SingleArgCtors.class);
        ValueInstantiator vi1 = factory.findValueInstantiator(ctxt, mapper.getDeserializationConfig().introspect(ctorType));
        assertNotNull(vi1);
        assertTrue(vi1.canCreateFromString());
        assertTrue(vi1.canCreateFromInt());
        assertTrue(vi1.canCreateFromDouble());
        assertTrue(vi1.canCreateFromBoolean());

        JavaType factoryType = mapper.constructType(SingleArgFactories.class);
        ValueInstantiator vi2 = factory.findValueInstantiator(ctxt, mapper.getDeserializationConfig().introspect(factoryType));
        assertNotNull(vi2);
        assertTrue(vi2.canCreateFromString());
        assertTrue(vi2.canCreateFromInt());
        assertTrue(vi2.canCreateFromDouble());
        assertTrue(vi2.canCreateFromBoolean());
    }
}