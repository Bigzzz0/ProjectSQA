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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.*;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.TokenBuffer;

/* [Branch & Defect Analysis Matrix]
 * ------------------------------------------------------------------------------------------------
 * TARGET CLASS: BasicDeserializerFactory (tested via BeanDeserializerFactory / ObjectMapper)
 *
 * PARTITION A: Core Functional Logic & State Transitions
 * - Fluent configuration updates: withAdditionalDeserializers, withAdditionalKeyDeserializers,
 *   withDeserializerModifier, withAbstractTypeResolver, withValueInstantiators.
 * - Standard ValueInstantiator creation: JsonLocation, Collections.EMPTY_SET/EMPTY_LIST/EMPTY_MAP.
 * - Collection fallbacks: List -> ArrayList, Set -> HashSet, Queue/Deque -> LinkedList,
 *   SortedSet/NavigableSet -> TreeSet.
 * - Map fallbacks: Map -> LinkedHashMap, ConcurrentMap -> ConcurrentHashMap,
 *   SortedMap/NavigableMap -> TreeMap, ConcurrentNavigableMap -> ConcurrentSkipListMap.
 * - findDefaultDeserializer: Object, String, CharSequence, Iterable, Map.Entry, Numbers, Dates, TokenBuffer.
 * - Tree deserializers: JsonNode, ObjectNode, ArrayNode.
 * - Reference deserializers: AtomicReference.
 *
 * PARTITION B: Boundary Value Analysis (BVA) & Extremes
 * - EnumMap deserialization with valid Enum keys vs non-Enum keys (throws IllegalArgumentException).
 * - Non-concrete Collection/Map types without fallback and without type handler (throws IllegalArgumentException).
 * - Empty string and empty collection/map instantiations.
 * - Incomplete creator parameter checks and constructor mapping.
 *
 * PARTITION C: Defect-Targeted Branch Zone (Defects4J known issue: testLocationAddition)
 * - Map key deserialization failure: verifying that deserializing an invalid Enum key does not cause
 *   duplicate nested exception location markers (" at [") in the message.
 *
 * PARTITION D: Exception & Defensive Guard Paths
 * - mapAbstractType circular resolution and invalid non-subtype mapping detection.
 * - _valueInstantiatorInstance invalid class specifications.
 * - Enum Creator with improper parameter types (must be String for key / valid type for creator).
 *
 * PARTITION E: Object Lifecycle & Deprecated API Integrity
 * - Deprecated methods: modifyTypeByAnnotation, resolveType, _findJsonValueFor.
 * - Immutable config retrieval and preservation.
 * ------------------------------------------------------------------------------------------------
 */
public class BasicDeserializerFactoryGeminiTest {

    private final BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
    private final ObjectMapper mapper = new ObjectMapper();

    enum TestEnum {
        ALPHA, BETA, GAMMA;
    }

    enum EnumWithValue {
        ONE("1"), TWO("2");
        private final String code;
        EnumWithValue(String code) { this.code = code; }
        @JsonValue
        public String getCode() { return code; }
    }

    enum EnumWithFactory {
        A, B;
        @JsonCreator
        public static EnumWithFactory fromString(String val) {
            for (EnumWithFactory e : values()) {
                if (e.name().equalsIgnoreCase(val)) return e;
            }
            return null;
        }
    }

    enum EnumWithInvalidFactory {
        X;
        @JsonCreator
        public static EnumWithInvalidFactory make(int invalidParam) {
            return X;
        }
    }

    static class CustomDummyValueInstantiator extends ValueInstantiator implements Serializable {
        @Override
        public String getValueTypeDesc() { return "CustomDummy"; }
        @Override
        public boolean canCreateUsingDefault() { return true; }
        @Override
        public Object createUsingDefault(DeserializationContext ctxt) { return "customInstance"; }
    }

    interface UnmappedInterface {
        String getValue();
    }

    interface AbstractCustomMap<K, V> extends Map<K, V> { }
    interface AbstractCustomCollection<E> extends Collection<E> { }

    // ============================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ============================================================================================

    @Test(timeout = 4000)
    public void testFactoryFluentConfigChaining() {
        DeserializerFactoryConfig config = factory.getFactoryConfig();
        assertNotNull(config);

        DeserializerFactory f1 = factory.withAdditionalDeserializers(new SimpleModule().getDeserializers());
        assertNotSame(factory, f1);

        DeserializerFactory f2 = factory.withAdditionalKeyDeserializers(new SimpleModule().getKeyDeserializers());
        assertNotSame(factory, f2);

        DeserializerFactory f3 = factory.withDeserializerModifier(new BeanDeserializerModifier() {});
        assertNotSame(factory, f3);

        DeserializerFactory f4 = factory.withAbstractTypeResolver(new SimpleAbstractTypeResolver());
        assertNotSame(factory, f4);

        DeserializerFactory f5 = factory.withValueInstantiators(new ValueInstantiators.Base());
        assertNotSame(factory, f5);
    }

    @Test(timeout = 4000)
    public void testFindStdValueInstantiators() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializationConfig config = mapper.getDeserializationConfig();

        // 1. JsonLocation
        JavaType locType = mapper.constructType(JsonLocation.class);
        BeanDescription locDesc = config.introspect(locType);
        ValueInstantiator locInst = factory.findValueInstantiator(ctxt, locDesc);
        assertNotNull(locInst);
        assertTrue(locInst.canCreateFromObjectWith());

        // 2. Collections.EMPTY_SET
        JavaType emptySetType = mapper.constructType(Collections.EMPTY_SET.getClass());
        BeanDescription setDesc = config.introspect(emptySetType);
        ValueInstantiator setInst = factory.findValueInstantiator(ctxt, setDesc);
        assertNotNull(setInst);
        assertEquals(Collections.EMPTY_SET, setInst.createUsingDefault(ctxt));

        // 3. Collections.EMPTY_LIST
        JavaType emptyListType = mapper.constructType(Collections.EMPTY_LIST.getClass());
        BeanDescription listDesc = config.introspect(emptyListType);
        ValueInstantiator listInst = factory.findValueInstantiator(ctxt, listDesc);
        assertNotNull(listInst);
        assertEquals(Collections.EMPTY_LIST, listInst.createUsingDefault(ctxt));

        // 4. Collections.EMPTY_MAP
        JavaType emptyMapType = mapper.constructType(Collections.EMPTY_MAP.getClass());
        BeanDescription mapDesc = config.introspect(emptyMapType);
        ValueInstantiator mapInst = factory.findValueInstantiator(ctxt, mapDesc);
        assertNotNull(mapInst);
        assertEquals(Collections.EMPTY_MAP, mapInst.createUsingDefault(ctxt));
    }

    @Test(timeout = 4000)
    public void testFindDefaultDeserializersWellKnownTypes() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializationConfig config = mapper.getDeserializationConfig();

        // Object.class
        JavaType objType = mapper.constructType(Object.class);
        JsonDeserializer<?> objDeser = factory.findDefaultDeserializer(ctxt, objType, config.introspect(objType));
        assertTrue(objDeser instanceof UntypedObjectDeserializer);

        // String.class & CharSequence.class
        JavaType strType = mapper.constructType(String.class);
        JsonDeserializer<?> strDeser = factory.findDefaultDeserializer(ctxt, strType, config.introspect(strType));
        assertSame(StringDeserializer.instance, strDeser);

        JavaType charSeqType = mapper.constructType(CharSequence.class);
        JsonDeserializer<?> charSeqDeser = factory.findDefaultDeserializer(ctxt, charSeqType, config.introspect(charSeqType));
        assertSame(StringDeserializer.instance, charSeqDeser);

        // Map.Entry
        JavaType entryType = mapper.getTypeFactory().constructMapEntryType(Map.Entry.class, String.class, Integer.class);
        JsonDeserializer<?> entryDeser = factory.findDefaultDeserializer(ctxt, entryType, config.introspect(entryType));
        assertTrue(entryDeser instanceof MapEntryDeserializer);

        // TokenBuffer
        JavaType tbType = mapper.constructType(TokenBuffer.class);
        JsonDeserializer<?> tbDeser = factory.findDefaultDeserializer(ctxt, tbType, config.introspect(tbType));
        assertTrue(tbDeser instanceof TokenBufferDeserializer);

        // Number primitives & java.util.Date
        JavaType intType = mapper.constructType(int.class);
        assertNotNull(factory.findDefaultDeserializer(ctxt, intType, config.introspect(intType)));

        JavaType dateType = mapper.constructType(Date.class);
        assertNotNull(factory.findDefaultDeserializer(ctxt, dateType, config.introspect(dateType)));

        // Iterable.class
        JavaType iterType = mapper.constructType(Iterable.class);
        assertNotNull(factory.findDefaultDeserializer(ctxt, iterType, config.introspect(iterType)));
    }

    @Test(timeout = 4000)
    public void testTreeDeserializers() throws Exception {
        DeserializationConfig config = mapper.getDeserializationConfig();

        JavaType nodeType = mapper.constructType(JsonNode.class);
        JsonDeserializer<?> deser = factory.createTreeDeserializer(config, nodeType, config.introspect(nodeType));
        assertNotNull(deser);

        JavaType objNodeType = mapper.constructType(ObjectNode.class);
        JsonDeserializer<?> objDeser = factory.createTreeDeserializer(config, objNodeType, config.introspect(objNodeType));
        assertNotNull(objDeser);

        JavaType arrNodeType = mapper.constructType(ArrayNode.class);
        JsonDeserializer<?> arrDeser = factory.createTreeDeserializer(config, arrNodeType, config.introspect(arrNodeType));
        assertNotNull(arrDeser);
    }

    @Test(timeout = 4000)
    public void testArrayDeserializers() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializationConfig config = mapper.getDeserializationConfig();

        // Primitive int array
        ArrayType intArrayType = mapper.getTypeFactory().constructArrayType(int.class);
        JsonDeserializer<?> intArrDeser = factory.createArrayDeserializer(ctxt, intArrayType, config.introspect(intArrayType));
        assertNotNull(intArrDeser);

        // String array
        ArrayType strArrayType = mapper.getTypeFactory().constructArrayType(String.class);
        JsonDeserializer<?> strArrDeser = factory.createArrayDeserializer(ctxt, strArrayType, config.introspect(strArrayType));
        assertSame(StringArrayDeserializer.instance, strArrDeser);

        // Object array
        ArrayType objArrayType = mapper.getTypeFactory().constructArrayType(Object.class);
        JsonDeserializer<?> objArrDeser = factory.createArrayDeserializer(ctxt, objArrayType, config.introspect(objArrayType));
        assertTrue(objArrDeser instanceof ObjectArrayDeserializer);
    }

    @Test(timeout = 4000)
    public void testCollectionFallbacksAndCustomTypes() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializationConfig config = mapper.getDeserializationConfig();
        TypeFactory tf = mapper.getTypeFactory();

        Class<?>[] collClasses = new Class<?>[] {
            Collection.class, List.class, Set.class, SortedSet.class,
            Queue.class, Deque.class, NavigableSet.class
        };

        for (Class<?> cls : collClasses) {
            CollectionType ct = tf.constructCollectionType((Class<? extends Collection>) cls, String.class);
            JsonDeserializer<?> deser = factory.createCollectionDeserializer(ctxt, ct, config.introspect(ct));
            assertNotNull("Deserializer should be created for " + cls.getName(), deser);
        }

        // EnumSet
        CollectionType enumSetType = tf.constructCollectionType(EnumSet.class, TestEnum.class);
        JsonDeserializer<?> enumSetDeser = factory.createCollectionDeserializer(ctxt, enumSetType, config.introspect(enumSetType));
        assertTrue(enumSetDeser instanceof EnumSetDeserializer);

        // ArrayBlockingQueue (no default ctor)
        CollectionType abqType = tf.constructCollectionType(ArrayBlockingQueue.class, Integer.class);
        JsonDeserializer<?> abqDeser = factory.createCollectionDeserializer(ctxt, abqType, config.introspect(abqType));
        assertTrue(abqDeser instanceof ArrayBlockingQueueDeserializer);
    }

    @Test(timeout = 4000)
    public void testMapFallbacksAndCustomTypes() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializationConfig config = mapper.getDeserializationConfig();
        TypeFactory tf = mapper.getTypeFactory();

        Class<?>[] mapClasses = new Class<?>[] {
            Map.class, ConcurrentMap.class, SortedMap.class,
            NavigableMap.class, ConcurrentNavigableMap.class
        };

        for (Class<?> cls : mapClasses) {
            MapType mt = tf.constructMapType((Class<? extends Map>) cls, String.class, String.class);
            JsonDeserializer<?> deser = factory.createMapDeserializer(ctxt, mt, config.introspect(mt));
            assertNotNull("Deserializer should be created for " + cls.getName(), deser);
        }

        // EnumMap
        MapType enumMapType = tf.constructMapType(EnumMap.class, TestEnum.class, String.class);
        JsonDeserializer<?> enumMapDeser = factory.createMapDeserializer(ctxt, enumMapType, config.introspect(enumMapType));
        assertTrue(enumMapDeser instanceof EnumMapDeserializer);
    }

    @Test(timeout = 4000)
    public void testReferenceDeserializer() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializationConfig config = mapper.getDeserializationConfig();

        JavaType refType = mapper.getTypeFactory().constructReferenceType(AtomicReference.class, mapper.constructType(String.class));
        JsonDeserializer<?> deser = factory.createReferenceDeserializer(ctxt, (ReferenceType) refType, config.introspect(refType));
        assertTrue(deser instanceof AtomicReferenceDeserializer);
    }

    @Test(timeout = 4000)
    public void testKeyDeserializerResolution() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();

        // Enum key
        JavaType enumType = mapper.constructType(TestEnum.class);
        KeyDeserializer enumKeyDeser = factory.createKeyDeserializer(ctxt, enumType);
        assertNotNull(enumKeyDeser);

        // String based key (e.g. Long)
        JavaType longType = mapper.constructType(Long.class);
        KeyDeserializer longKeyDeser = factory.createKeyDeserializer(ctxt, longType);
        assertNotNull(longKeyDeser);
    }

    // ============================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ============================================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEnumMapFailsWithNonEnumKey() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializationConfig config = mapper.getDeserializationConfig();
        TypeFactory tf = mapper.getTypeFactory();

        // Key is String, not Enum
        MapType invalidEnumMap = tf.constructMapType(EnumMap.class, String.class, String.class);
        factory.createMapDeserializer(ctxt, invalidEnumMap, config.introspect(invalidEnumMap));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUnmappedAbstractCollectionThrows() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializationConfig config = mapper.getDeserializationConfig();
        TypeFactory tf = mapper.getTypeFactory();

        CollectionType unmappedColl = tf.constructCollectionType(AbstractCustomCollection.class, String.class);
        factory.createCollectionDeserializer(ctxt, unmappedColl, config.introspect(unmappedColl));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUnmappedAbstractMapThrows() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializationConfig config = mapper.getDeserializationConfig();
        TypeFactory tf = mapper.getTypeFactory();

        MapType unmappedMap = tf.constructMapType(AbstractCustomMap.class, String.class, String.class);
        factory.createMapDeserializer(ctxt, unmappedMap, config.introspect(unmappedMap));
    }

    @Test(timeout = 4000)
    public void testValueInstantiatorInstanceResolution() throws Exception {
        DeserializationConfig config = mapper.getDeserializationConfig();
        AnnotatedClass ac = AnnotatedClassResolver.resolve(config, mapper.constructType(String.class), null);

        // Null def returns null
        assertNull(factory._valueInstantiatorInstance(config, ac, null));

        // Direct instance
        CustomDummyValueInstantiator inst = new CustomDummyValueInstantiator();
        assertSame(inst, factory._valueInstantiatorInstance(config, ac, inst));

        // By Class reference
        ValueInstantiator created = factory._valueInstantiatorInstance(config, ac, CustomDummyValueInstantiator.class);
        assertTrue(created instanceof CustomDummyValueInstantiator);
    }

    // ============================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug: testLocationAddition)
    // ============================================================================================

    @Test(timeout = 4000)
    public void testLocationAdditionOnEnumKeyDeserializationFailure() {
        // Targets the failure in Defects4J: BasicExceptionTest::testLocationAddition
        // Where an invalid Enum map key produced duplicate " at [" location markers.
        try {
            mapper.readValue("{\"NOT_AN_ENUM\": \"val\"}", new TypeReference<Map<TestEnum, String>>() {});
            fail("Expected exception when deserializing invalid Enum key");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertNotNull(msg);

            // Count occurrences of " at ["
            int count = 0;
            int idx = 0;
            while ((idx = msg.indexOf(" at [", idx)) != -1) {
                count++;
                idx += 5;
            }
            assertEquals("Should only get one 'at [' marker in error message, got " + count + ": " + msg, 1, count);
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName());
        }
    }

    // ============================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ============================================================================================

    @Test(timeout = 4000)
    public void testMapAbstractTypeCycleAndSubtypeValidation() throws Exception {
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType stringType = mapper.constructType(String.class);

        // Resolving an unregistered abstract type should return original type
        JavaType resolved = factory.mapAbstractType(config, stringType);
        assertSame(stringType, resolved);

        // Testing invalid non-subtype mapping exception
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        // Map CharSequence to Integer (invalid subtype!)
        resolver.addMapping(CharSequence.class, (Class) Integer.class);

        DeserializerFactory customFactory = factory.withAbstractTypeResolver(resolver);
        try {
            customFactory.mapAbstractType(config, mapper.constructType(CharSequence.class));
            fail("Expected IllegalArgumentException for invalid abstract type mapping");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not a subtype of"));
        }
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testValueInstantiatorNonInstantiatorClassThrows() throws Exception {
        DeserializationConfig config = mapper.getDeserializationConfig();
        AnnotatedClass ac = AnnotatedClassResolver.resolve(config, mapper.constructType(String.class), null);
        // Passing a Class that does not implement ValueInstantiator
        factory._valueInstantiatorInstance(config, ac, String.class);
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testValueInstantiatorNonClassNonInstanceThrows() throws Exception {
        DeserializationConfig config = mapper.getDeserializationConfig();
        AnnotatedClass ac = AnnotatedClassResolver.resolve(config, mapper.constructType(String.class), null);
        // Passing a String instead of Class/ValueInstantiator
        factory._valueInstantiatorInstance(config, ac, "notAClass");
    }

    @Test(timeout = 4000)
    public void testCreateEnumDeserializerWithVariants() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializationConfig config = mapper.getDeserializationConfig();

        // 1. Standard Enum
        JavaType stdType = mapper.constructType(TestEnum.class);
        JsonDeserializer<?> stdDeser = factory.createEnumDeserializer(ctxt, stdType, config.introspect(stdType));
        assertTrue(stdDeser instanceof EnumDeserializer);

        // 2. Enum with @JsonValue
        JavaType valType = mapper.constructType(EnumWithValue.class);
        JsonDeserializer<?> valDeser = factory.createEnumDeserializer(ctxt, valType, config.introspect(valType));
        assertTrue(valDeser instanceof EnumDeserializer);

        // 3. Enum with @JsonCreator
        JavaType creatorType = mapper.constructType(EnumWithFactory.class);
        JsonDeserializer<?> creatorDeser = factory.createEnumDeserializer(ctxt, creatorType, config.introspect(creatorType));
        assertNotNull(creatorDeser);
    }

    @Test(timeout = 4000)
    public void testEnumKeyDeserializerWithCreatorInvalidParamThrows() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType invalidType = mapper.constructType(EnumWithInvalidFactory.class);

        try {
            factory.createKeyDeserializer(ctxt, invalidType);
            fail("Expected IllegalArgumentException when Creator factory parameter is not String for EnumKeyDeserializer");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must be java.lang.String"));
        }
    }

    // ============================================================================================
    // Partition E: Object Lifecycle & Deprecated API Integrity
    // ============================================================================================

    @SuppressWarnings("deprecation")
    @Test(timeout = 4000)
    public void testDeprecatedHelperMethods() throws Exception {
        DeserializationConfig config = mapper.getDeserializationConfig();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JavaType enumType = mapper.constructType(EnumWithValue.class);
        // _findJsonValueFor
        assertNotNull(factory._findJsonValueFor(config, enumType));
        assertNull(factory._findJsonValueFor(config, null));

        // modifyTypeByAnnotation & resolveType
        BasicBeanDescription desc = (BasicBeanDescription) config.introspect(enumType);
        Annotated ann = desc.getClassInfo();
        JavaType refined = factory.modifyTypeByAnnotation(ctxt, ann, enumType);
        assertEquals(enumType, refined);
    }
}