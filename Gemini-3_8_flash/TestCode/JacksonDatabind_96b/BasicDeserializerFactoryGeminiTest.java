package com.fasterxml.jackson.databind.deser;

/* [Branch & Defect Analysis Matrix]
 *
 * 1. DEFECT UNDER TEST (Defects4J ground truth: CreatorWithNamingStrategyTest):
 *    - Method: _addExplicitAnyCreator(DeserializationContext, BeanDescription, CreatorCollector, CreatorCandidate)
 *    - Branch: 1-parameter Creator annotated with @JsonCreator (mode DEFAULT/ANY), no explicit property name,
 *      implicit parameter name matches POJO property with getter (couldSerialize() == true), and PropertyNamingStrategy
 *      is configured (e.g., SNAKE_CASE).
 *    - Flaw: In the defective version, candidate.findImplicitParamName(0) was mistakenly assigned instead of the
 *      renamed/strategy-based property name (from candidate.paramName(0) or paramDef.getFullName()). This results in
 *      creator property registered under the unrenamed implicit name (e.g. "paramName0") instead of the renamed
 *      property ("param_name0"), triggering:
 *      "InvalidDefinitionException: Invalid definition for property `param_name0`: Could not find creator property
 *       with name 'param_name0' (known Creator properties: [paramName0])".
 *
 * 2. COVERAGE TARGETS (Partitions A - E):
 *    - Abstract Type Resolution:
 *      * mapAbstractType() normal resolution, non-subclass resolution throwing IllegalArgumentException.
 *      * _mapAbstractType2() with and without AbstractTypeResolvers.
 *    - ValueInstantiator Resolution:
 *      * findValueInstantiator() with custom @JsonValueInstantiator, standard collections (EMPTY_SET, EMPTY_LIST, EMPTY_MAP),
 *        JsonLocation, standard default instantiators, and custom ValueInstantiators module handler.
 *      * _valueInstantiatorInstance() with null, direct instance, bogus class, non-instantiator class.
 *    - Container / Collection / Map Deserializers:
 *      * createCollectionDeserializer() with Collection, List, Set, Queue, EnumSet, ArrayBlockingQueue, unmodifiable/empty.
 *      * createArrayDeserializer() with primitives (int[], byte[]), String[], Object[].
 *      * createMapDeserializer() with Map, ConcurrentMap, SortedMap, EnumMap (with enum key and non-enum key failure).
 *      * createReferenceDeserializer() for AtomicReference.
 *      * createTreeDeserializer() for JsonNode.
 *    - Fallback and Default Mappings:
 *      * _collectionFallbacks, _mapFallbacks resolution.
 *      * findDefaultDeserializer() for Object.class, String.class, CharSequence.class, Iterable.class, Map.Entry.class,
 *        TokenBuffer.class, Date, Number, and JDK classes.
 *    - Type Deserializers & Key Deserializers:
 *      * findTypeDeserializer(), createKeyDeserializer() for Enum and String-based keys.
 *      * _createEnumKeyDeserializer() with @JsonCreator factory method and normal enum resolution.
 *    - Creator Introspection & Visibility:
 *      * Creator candidates with 1-arg String/int/long/double/boolean factory methods and constructors.
 *      * Explicit Delegating (@JsonCreator(mode = DELEGATING)) creators vs explicit Properties (@JsonCreator(mode = PROPERTIES)).
 *      * Unwrapped creator parameters triggering error report (_reportUnwrappedCreatorProperty).
 */

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import org.junit.Test;
import static org.junit.Assert.*;

public class BasicDeserializerFactoryGeminiTest {

    // =========================================================================
    // Test Supporting Types & Fixtures
    // =========================================================================

    public enum SampleEnum {
        A, B, C;

        @JsonCreator
        public static SampleEnum fromString(String val) {
            if ("A_CUSTOM".equalsIgnoreCase(val)) return A;
            return valueOf(val);
        }
    }

    public enum PlainEnum {
        ONE, TWO
    }

    public static class CustomOnePropertyBean {
        private final String paramName0;

        @JsonCreator
        public CustomOnePropertyBean(String paramName0) {
            this.paramName0 = paramName0;
        }

        public String getParamName0() {
            return paramName0;
        }
    }

    public static class DelegatingBean {
        final String value;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public DelegatingBean(String v) {
            this.value = v;
        }

        public String getValue() {
            return value;
        }
    }

    public static class MultiParamCreatorBean {
        final String first;
        final int second;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public MultiParamCreatorBean(@JsonProperty("first") String first, @JsonProperty("second") int second) {
            this.first = first;
            this.second = second;
        }

        public String getFirst() { return first; }
        public int getSecond() { return second; }
    }

    public static class PrimitiveCreatorsBean {
        final Object val;

        public PrimitiveCreatorsBean(int v) { this.val = v; }
        public PrimitiveCreatorsBean(long v) { this.val = v; }
        public PrimitiveCreatorsBean(double v) { this.val = v; }
        public PrimitiveCreatorsBean(boolean v) { this.val = v; }
        public PrimitiveCreatorsBean(String v) { this.val = v; }
    }

    public static class CustomInstantiator extends ValueInstantiator {
        @Override
        public String getValueTypeDesc() {
            return "CustomInstantiator";
        }

        @Override
        public boolean canCreateUsingDefault() {
            return true;
        }

        @Override
        public Object createUsingDefault(DeserializationContext ctxt) {
            return new CustomInstantiatorTarget("created-by-custom");
        }
    }

    public static class CustomInstantiatorTarget {
        public String val;
        public CustomInstantiatorTarget(String val) { this.val = val; }
    }

    public interface UnrelatedInterface {}

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J ground truth)
    // =========================================================================

    /**
     * TARGETS KNOWN DEFECT: databind#2051 / CreatorWithNamingStrategyTest#testSnakeCaseWithOneArg
     * When a 1-argument @JsonCreator uses an implicit parameter name that matches a property
     * renamed via PropertyNamingStrategy (e.g. SNAKE_CASE), BasicDeserializerFactory._addExplicitAnyCreator
     * must register the creator property using the renamed property name ("param_name0"), not the unrenamed
     * implicit parameter name ("paramName0").
     */
    @Test(timeout = 4000)
    public void testSnakeCaseNamingStrategyWithOneArgCreatorDefect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        // Inject an AnnotationIntrospector to supply implicit parameter name "paramName0"
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            private static final long serialVersionUID = 1L;

            @Override
            public String findImplicitPropertyName(AnnotatedMember member) {
                if (member instanceof AnnotatedParameter) {
                    AnnotatedParameter p = (AnnotatedParameter) member;
                    if (p.getIndex() == 0 && p.getOwner().getDeclaringClass() == CustomOnePropertyBean.class) {
                        return "paramName0";
                    }
                }
                return super.findImplicitPropertyName(member);
            }
        });

        String json = "{\"param_name0\":\"gemini_test_value\"}";
        CustomOnePropertyBean result = mapper.readValue(json, CustomOnePropertyBean.class);
        assertNotNull("Deserialized bean should not be null", result);
        assertEquals("Property should be correctly bound via renamed creator property",
                "gemini_test_value", result.getParamName0());
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & FLUENT FACTORY CONFIG
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactoryConfigFluentMethods() {
        DeserializerFactory factory = BeanDeserializerFactory.instance;
        DeserializerFactoryConfig origConfig = factory.getFactoryConfig();
        assertNotNull(origConfig);

        // Test withAdditionalDeserializers
        DeserializerFactory f1 = factory.withAdditionalDeserializers(new Deserializers.Base());
        assertNotSame(factory, f1);
        assertTrue(f1.getFactoryConfig().hasDeserializers());

        // Test withAdditionalKeyDeserializers
        DeserializerFactory f2 = factory.withAdditionalKeyDeserializers(new KeyDeserializers() {
            @Override
            public KeyDeserializer findKeyDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) {
                return null;
            }
        });
        assertNotSame(factory, f2);
        assertTrue(f2.getFactoryConfig().hasKeyDeserializers());

        // Test withDeserializerModifier
        DeserializerFactory f3 = factory.withDeserializerModifier(new BeanDeserializerModifier() {});
        assertNotSame(factory, f3);
        assertTrue(f3.getFactoryConfig().hasDeserializerModifiers());

        // Test withAbstractTypeResolver
        DeserializerFactory f4 = factory.withAbstractTypeResolver(new SimpleAbstractTypeResolver());
        assertNotSame(factory, f4);
        assertTrue(f4.getFactoryConfig().hasAbstractTypeResolvers());

        // Test withValueInstantiators
        DeserializerFactory f5 = factory.withValueInstantiators(new SimpleValueInstantiators());
        assertNotSame(factory, f5);
        assertTrue(f5.getFactoryConfig().hasValueInstantiators());
    }

    @Test(timeout = 4000)
    public void testMapAbstractTypeSuccessAndFailure() throws Exception {
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        TypeFactory tf = mapper.getTypeFactory();

        // 1. Without resolvers, mapping abstract type returns itself
        JavaType listType = tf.constructType(List.class);
        JavaType resolvedNoOp = factory.mapAbstractType(config, listType);
        assertEquals(List.class, resolvedNoOp.getRawClass());

        // 2. With resolver mapping List -> ArrayList
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(List.class, ArrayList.class);
        DeserializerFactory factoryWithResolver = factory.withAbstractTypeResolver(resolver);

        JavaType resolvedSubtype = factoryWithResolver.mapAbstractType(config, listType);
        assertEquals(ArrayList.class, resolvedSubtype.getRawClass());

        // 3. Invalid mapping: resolving to non-subtype throws IllegalArgumentException
        SimpleAbstractTypeResolver brokenResolver = new SimpleAbstractTypeResolver();
        brokenResolver.addMapping(CharSequence.class, (Class) Integer.class);
        DeserializerFactory brokenFactory = factory.withAbstractTypeResolver(brokenResolver);

        try {
            brokenFactory.mapAbstractType(config, tf.constructType(CharSequence.class));
            fail("Expected IllegalArgumentException when abstract type maps to non-subtype");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("latter is not a subtype of former"));
        }
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS & CONTAINER TYPES
    // =========================================================================

    @Test(timeout = 4000)
    public void testCollectionAndListFallbacks() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Fallback for java.util.Collection -> ArrayList
        Collection<?> col = mapper.readValue("[\"item1\"]", Collection.class);
        assertTrue(col instanceof ArrayList);

        // Fallback for java.util.List -> ArrayList
        List<?> list = mapper.readValue("[\"item2\"]", List.class);
        assertTrue(list instanceof ArrayList);

        // Fallback for java.util.Set -> HashSet
        Set<?> set = mapper.readValue("[\"item3\"]", Set.class);
        assertTrue(set instanceof HashSet);

        // Fallback for java.util.SortedSet -> TreeSet
        SortedSet<?> sortedSet = mapper.readValue("[\"item4\"]", SortedSet.class);
        assertTrue(sortedSet instanceof TreeSet);

        // Fallback for java.util.Queue -> LinkedList
        Queue<?> queue = mapper.readValue("[\"item5\"]", Queue.class);
        assertTrue(queue instanceof LinkedList);

        // Fallback for java.util.Deque -> LinkedList
        Deque<?> deque = mapper.readValue("[\"item6\"]", Deque.class);
        assertTrue(deque instanceof LinkedList);

        // Fallback for java.util.NavigableSet -> TreeSet
        NavigableSet<?> navSet = mapper.readValue("[\"item7\"]", NavigableSet.class);
        assertTrue(navSet instanceof TreeSet);
    }

    @Test(timeout = 4000)
    public void testMapFallbacks() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Fallback for java.util.Map -> LinkedHashMap
        Map<?, ?> map = mapper.readValue("{\"k\":\"v\"}", Map.class);
        assertTrue(map instanceof LinkedHashMap);

        // Fallback for java.util.concurrent.ConcurrentMap -> ConcurrentHashMap
        ConcurrentMap<?, ?> concMap = mapper.readValue("{\"k\":\"v\"}", ConcurrentMap.class);
        assertTrue(concMap instanceof ConcurrentHashMap);

        // Fallback for java.util.SortedMap -> TreeMap
        SortedMap<?, ?> sortedMap = mapper.readValue("{\"k\":\"v\"}", SortedMap.class);
        assertTrue(sortedMap instanceof TreeMap);

        // Fallback for java.util.NavigableMap -> TreeMap
        NavigableMap<?, ?> navMap = mapper.readValue("{\"k\":\"v\"}", NavigableMap.class);
        assertTrue(navMap instanceof TreeMap);

        // Fallback for java.util.concurrent.ConcurrentNavigableMap -> ConcurrentSkipListMap
        ConcurrentNavigableMap<?, ?> concNavMap = mapper.readValue("{\"k\":\"v\"}", ConcurrentNavigableMap.class);
        assertTrue(concNavMap instanceof ConcurrentSkipListMap);
    }

    @Test(timeout = 4000)
    public void testSpecialCollectionsAndInstantiators() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Databind#161: ArrayBlockingQueue with default capacity
        ArrayBlockingQueue<?> abq = mapper.readValue("[\"a\", \"b\"]", ArrayBlockingQueue.class);
        assertEquals(2, abq.size());

        // Databind#1868: Empty collections
        Set<?> emptySet = mapper.readValue("[]", Collections.EMPTY_SET.getClass());
        assertEquals(0, emptySet.size());

        List<?> emptyList = mapper.readValue("[]", Collections.EMPTY_LIST.getClass());
        assertEquals(0, emptyList.size());

        Map<?, ?> emptyMap = mapper.readValue("{}", Collections.EMPTY_MAP.getClass());
        assertEquals(0, emptyMap.size());

        // JsonLocation instantiator
        String locJson = "{\"sourceRef\":\"test\", \"charOffset\":10, \"lineNr\":2, \"columnNr\":5}";
        JsonLocation loc = mapper.readValue(locJson, JsonLocation.class);
        assertEquals(2, loc.getLineNr());
        assertEquals(5, loc.getColumnNr());
    }

    @Test(timeout = 4000)
    public void testArrayDeserializers() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Primitive int array
        int[] ints = mapper.readValue("[1, 2, 3]", int[].class);
        assertArrayEquals(new int[]{1, 2, 3}, ints);

        // Primitive byte array (from base64 or JSON ints)
        byte[] bytes = mapper.readValue("[10, 20]", byte[].class);
        assertArrayEquals(new byte[]{10, 20}, bytes);

        // String array
        String[] strings = mapper.readValue("[\"x\", \"y\"]", String[].class);
        assertArrayEquals(new String[]{"x", "y"}, strings);

        // Object array
        Object[] objects = mapper.readValue("[\"val\", 123]", Object[].class);
        assertEquals(2, objects.length);
        assertEquals("val", objects[0]);
        assertEquals(123, objects[1]);
    }

    @Test(timeout = 4000)
    public void testReferenceAndAtomicReferenceDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        AtomicReference<String> ref = mapper.readValue("\"atomic_content\"",
                new TypeReference<AtomicReference<String>>() {});
        assertNotNull(ref);
        assertEquals("atomic_content", ref.get());
    }

    @Test(timeout = 4000)
    public void testEnumSetDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumSet<PlainEnum> enumSet = mapper.readValue("[\"ONE\", \"TWO\"]",
                new TypeReference<EnumSet<PlainEnum>>() {});
        assertEquals(2, enumSet.size());
        assertTrue(enumSet.contains(PlainEnum.ONE));
        assertTrue(enumSet.contains(PlainEnum.TWO));
    }

    @Test(timeout = 4000)
    public void testEnumMapDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        EnumMap<PlainEnum, String> enumMap = mapper.readValue("{\"ONE\": \"first\"}",
                new TypeReference<EnumMap<PlainEnum, String>>() {});
        assertEquals(1, enumMap.size());
        assertEquals("first", enumMap.get(PlainEnum.ONE));
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNonConcreteCollectionWithoutTypeResolverThrowsException() throws Exception {
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        CollectionType type = mapper.getTypeFactory().constructCollectionType(
                (Class) UnrelatedInterface.class, String.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspectClassAnnotations(UnrelatedInterface.class);

        factory.createCollectionDeserializer(ctxt, type, desc);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNonConcreteMapWithoutTypeResolverThrowsException() throws Exception {
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        MapType type = mapper.getTypeFactory().constructMapType(
                (Class) UnrelatedInterface.class, String.class, String.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspectClassAnnotations(UnrelatedInterface.class);

        factory.createMapDeserializer(ctxt, type, desc);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEnumMapWithoutEnumKeyThrowsException() throws Exception {
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        MapType type = mapper.getTypeFactory().constructMapType(
                EnumMap.class, String.class, String.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspectClassAnnotations(EnumMap.class);

        factory.createMapDeserializer(ctxt, type, desc);
    }

    @Test(timeout = 4000)
    public void testValueInstantiatorInstanceGuards() throws Exception {
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();

        // 1. null definition returns null
        assertNull(factory._valueInstantiatorInstance(config, null, null));

        // 2. Instance of ValueInstantiator returns itself
        ValueInstantiator direct = new StdValueInstantiator(config, String.class);
        assertSame(direct, factory._valueInstantiatorInstance(config, null, direct));

        // 3. Bogus class (NoClass / Void) returns null
        assertNull(factory._valueInstantiatorInstance(config, null, com.fasterxml.jackson.databind.annotation.NoClass.class));

        // 4. Non-Class and non-ValueInstantiator object throws IllegalStateException
        try {
            factory._valueInstantiatorInstance(config, null, "not-a-class");
            fail("Expected IllegalStateException for non-class definition");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("AnnotationIntrospector returned key deserializer definition"));
        }

        // 5. Class not extending ValueInstantiator throws IllegalStateException
        try {
            factory._valueInstantiatorInstance(config, null, String.class);
            fail("Expected IllegalStateException for class not extending ValueInstantiator");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("expected Class<ValueInstantiator>"));
        }
    }

    // =========================================================================
    // PARTITION E: CREATOR MODES, KEY DESERIALIZERS & DEFAULT DESERIALIZERS
    // =========================================================================

    @Test(timeout = 4000)
    public void testExplicitDelegatingAndPropertyCreators() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Delegating Creator Mode
        DelegatingBean dBean = mapper.readValue("\"gemini_del\"", DelegatingBean.class);
        assertEquals("gemini_del", dBean.getValue());

        // Multi-parameter Properties Creator Mode
        MultiParamCreatorBean mBean = mapper.readValue("{\"first\":\"abc\",\"second\":42}", MultiParamCreatorBean.class);
        assertEquals("abc", mBean.getFirst());
        assertEquals(42, mBean.getSecond());
    }

    @Test(timeout = 4000)
    public void testSingleArgPrimitiveAndStringCreators() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        PrimitiveCreatorsBean fromInt = mapper.readValue("100", PrimitiveCreatorsBean.class);
        assertEquals(100, fromInt.val);

        PrimitiveCreatorsBean fromStr = mapper.readValue("\"gemini_str\"", PrimitiveCreatorsBean.class);
        assertEquals("gemini_str", fromStr.val);

        PrimitiveCreatorsBean fromBool = mapper.readValue("true", PrimitiveCreatorsBean.class);
        assertEquals(true, fromBool.val);
    }

    @Test(timeout = 4000)
    public void testKeyDeserializerResolution() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // String based key
        Map<String, String> strKeyMap = mapper.readValue("{\"key1\":\"val1\"}",
                new TypeReference<Map<String, String>>() {});
        assertEquals("val1", strKeyMap.get("key1"));

        // Enum key with @JsonCreator
        Map<SampleEnum, String> enumKeyMap = mapper.readValue("{\"A_CUSTOM\":\"valA\"}",
                new TypeReference<Map<SampleEnum, String>>() {});
        assertEquals("valA", enumKeyMap.get(SampleEnum.A));

        // Plain enum key
        Map<PlainEnum, String> plainEnumKeyMap = mapper.readValue("{\"ONE\":\"val1\"}",
                new TypeReference<Map<PlainEnum, String>>() {});
        assertEquals("val1", plainEnumKeyMap.get(PlainEnum.ONE));
    }

    @Test(timeout = 4000)
    public void testFindDefaultDeserializerPlatformTypes() throws Exception {
        BasicDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        TypeFactory tf = mapper.getTypeFactory();
        DeserializationConfig config = mapper.getDeserializationConfig();

        // Object.class -> UntypedObjectDeserializer
        JavaType objType = tf.constructType(Object.class);
        JsonDeserializer<?> deserObj = factory.findDefaultDeserializer(ctxt, objType, config.introspectClassAnnotations(Object.class));
        assertNotNull(deserObj);

        // String.class -> StringDeserializer
        JavaType strType = tf.constructType(String.class);
        JsonDeserializer<?> deserStr = factory.findDefaultDeserializer(ctxt, strType, config.introspectClassAnnotations(String.class));
        assertNotNull(deserStr);

        // Iterable.class -> CollectionDeserializer upgrade
        JavaType iterType = tf.constructType(Iterable.class);
        JsonDeserializer<?> deserIter = factory.findDefaultDeserializer(ctxt, iterType, config.introspectClassAnnotations(Iterable.class));
        assertNotNull(deserIter);

        // Map.Entry.class -> MapEntryDeserializer
        JavaType mapEntryType = tf.constructType(Map.Entry.class);
        JsonDeserializer<?> deserEntry = factory.findDefaultDeserializer(ctxt, mapEntryType, config.introspectClassAnnotations(Map.Entry.class));
        assertNotNull(deserEntry);

        // TokenBuffer.class -> TokenBufferDeserializer
        JavaType tbType = tf.constructType(TokenBuffer.class);
        JsonDeserializer<?> deserTb = factory.findDefaultDeserializer(ctxt, tbType, config.introspectClassAnnotations(TokenBuffer.class));
        assertNotNull(deserTb);

        // JDK Date type
        JavaType dateType = tf.constructType(Date.class);
        JsonDeserializer<?> deserDate = factory.findDefaultDeserializer(ctxt, dateType, config.introspectClassAnnotations(Date.class));
        assertNotNull(deserDate);
    }

    @Test(timeout = 4000)
    public void testModuleRegisteredValueInstantiators() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        SimpleValueInstantiators instantiators = new SimpleValueInstantiators();
        instantiators.addValueInstantiator(CustomInstantiatorTarget.class, new CustomInstantiator());
        module.setValueInstantiators(instantiators);
        mapper.registerModule(module);

        CustomInstantiatorTarget res = mapper.readValue("{}", CustomInstantiatorTarget.class);
        assertNotNull(res);
        assertEquals("created-by-custom", res.val);
    }
}