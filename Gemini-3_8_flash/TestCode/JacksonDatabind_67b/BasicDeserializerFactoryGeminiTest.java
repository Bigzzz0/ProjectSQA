package com.fasterxml.jackson.databind.deser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.NoClass;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.impl.CreatorCollector;
import com.fasterxml.jackson.databind.deser.std.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.EnumResolver;
import com.fasterxml.jackson.databind.util.TokenBuffer;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------------------
 * Target Class: BasicDeserializerFactory (Abstract Deserializer Factory base)
 *
 * 1. DEFECT-TARGETED ZONE (Ground Truth: Defects4J / Jackson-databind #700+ / CustomEnumKeyDeserializer):
 *    - Branch: `BasicDeserializerFactory.createKeyDeserializer(...)`
 *      Issue: For Enum types (`if (type.isEnumType())`), the method prematurely returns `_createEnumKeyDeserializer(...)`,
 *             completely bypassing the post-processing loop for `_factoryConfig.deserializerModifiers()`.
 *      Target: `BeanDeserializerModifier.modifyKeyDeserializer(...)` must be invoked and allowed to customize Enum key deserializers.
 *
 * 2. CORE FUNCTIONAL & BRANCH COVERAGE ZONES:
 *    - Config Lifecycle: Fluent `withConfig`, `withAdditionalDeserializers`, `withAdditionalKeyDeserializers`,
 *      `withDeserializerModifier`, `withAbstractTypeResolver`, `withValueInstantiators`.
 *    - Fallbacks & Mappings: `_collectionFallbacks` (Collection, List, Set, SortedSet, Queue, Deque, NavigableSet)
 *      and `_mapFallbacks` (Map, ConcurrentMap, SortedMap, NavigableMap, ConcurrentNavigableMap).
 *    - Default Deserializers: Object (UntypedObjectDeserializer with list/map remapping), String, CharSequence,
 *      Iterable, Map.Entry, Numbers/Dates, TokenBuffer, JdkDeserializers (UUID, URL, URI, Pattern, Locale, StackTraceElement).
 *    - ValueInstantiator Resolution: JsonLocationInstantiator, @JsonValueInstantiator, broken instantiator reporting,
 *      incomplete creators validation.
 *    - Array / Collection / Map / Tree / Reference Deserializer Factories: Primitive array vs String[] vs Object[],
 *      EnumSet, ArrayBlockingQueue, StringCollection, EnumMap, AtomicReference, etc.
 *    - Creator Introspection & Visibility: Single-arg detection (String, int, long, double, boolean),
 *      delegating creators with @JacksonInject, non-static inner class rejection, missing creator property names.
 *    - Defensive/Exception Guards: Abstract collection/map without fallbacks or type handlers, invalid abstract type
 *      cycles/subtyping, bogus class instantiation, unsuitable enum creators.
 * ---------------------------------------------------------------------------------------------------------------------
 */
public class BasicDeserializerFactoryGeminiTest {

    private ObjectMapper mapper;
    private DeserializationConfig config;
    private DefaultDeserializationContext ctxt;
    private BeanDeserializerFactory factory;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        config = mapper.getDeserializationConfig();
        ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(config, null, mapper.getInjectableValues());
        factory = BeanDeserializerFactory.instance;
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactoryConfigFluentImmutability() {
        DeserializerFactoryConfig originalConfig = factory.getFactoryConfig();
        assertNotNull(originalConfig);

        Deserializers additionalDesers = new SimpleDeserializers();
        DeserializerFactory f1 = factory.withAdditionalDeserializers(additionalDesers);
        assertNotSame(factory, f1);
        assertTrue(f1.getFactoryConfig().hasDeserializers());

        KeyDeserializers additionalKeyDesers = new SimpleKeyDeserializers();
        DeserializerFactory f2 = factory.withAdditionalKeyDeserializers(additionalKeyDesers);
        assertNotSame(factory, f2);
        assertTrue(f2.getFactoryConfig().hasKeyDeserializers());

        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {};
        DeserializerFactory f3 = factory.withDeserializerModifier(modifier);
        assertNotSame(factory, f3);
        assertTrue(f3.getFactoryConfig().hasDeserializerModifiers());

        AbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        DeserializerFactory f4 = factory.withAbstractTypeResolver(resolver);
        assertNotSame(factory, f4);
        assertTrue(f4.getFactoryConfig().hasAbstractTypeResolvers());

        ValueInstantiators instantiators = new SimpleValueInstantiators();
        DeserializerFactory f5 = factory.withValueInstantiators(instantiators);
        assertNotSame(factory, f5);
        assertTrue(f5.getFactoryConfig().hasValueInstantiators());
    }

    @Test(timeout = 4000)
    public void testCollectionFallbacksMapping() {
        assertEquals(ArrayList.class, BasicDeserializerFactory._collectionFallbacks.get(Collection.class.getName()));
        assertEquals(ArrayList.class, BasicDeserializerFactory._collectionFallbacks.get(List.class.getName()));
        assertEquals(HashSet.class, BasicDeserializerFactory._collectionFallbacks.get(Set.class.getName()));
        assertEquals(TreeSet.class, BasicDeserializerFactory._collectionFallbacks.get(SortedSet.class.getName()));
        assertEquals(LinkedList.class, BasicDeserializerFactory._collectionFallbacks.get(Queue.class.getName()));
        assertEquals(LinkedList.class, BasicDeserializerFactory._collectionFallbacks.get("java.util.Deque"));
        assertEquals(TreeSet.class, BasicDeserializerFactory._collectionFallbacks.get("java.util.NavigableSet"));

        JavaType listType = mapper.constructType(List.class);
        CollectionType mappedList = factory._mapAbstractCollectionType(listType, config);
        assertNotNull(mappedList);
        assertEquals(ArrayList.class, mappedList.getRawClass());

        JavaType setType = mapper.constructType(Set.class);
        CollectionType mappedSet = factory._mapAbstractCollectionType(setType, config);
        assertNotNull(mappedSet);
        assertEquals(HashSet.class, mappedSet.getRawClass());
    }

    @Test(timeout = 4000)
    public void testMapFallbacksMapping() {
        assertEquals(LinkedHashMap.class, BasicDeserializerFactory._mapFallbacks.get(Map.class.getName()));
        assertEquals(ConcurrentHashMap.class, BasicDeserializerFactory._mapFallbacks.get(ConcurrentMap.class.getName()));
        assertEquals(TreeMap.class, BasicDeserializerFactory._mapFallbacks.get(SortedMap.class.getName()));
        assertEquals(TreeMap.class, BasicDeserializerFactory._mapFallbacks.get(NavigableMap.class.getName()));
        assertEquals(ConcurrentSkipListMap.class, BasicDeserializerFactory._mapFallbacks.get(ConcurrentNavigableMap.class.getName()));
    }

    @Test(timeout = 4000)
    public void testFindDefaultDeserializersJdkAndPrimitiveTypes() throws Exception {
        JavaType objType = mapper.constructType(Object.class);
        JsonDeserializer<?> objDeser = factory.findDefaultDeserializer(ctxt, objType, config.introspect(objType));
        assertTrue(objDeser instanceof UntypedObjectDeserializer);

        JavaType strType = mapper.constructType(String.class);
        JsonDeserializer<?> strDeser = factory.findDefaultDeserializer(ctxt, strType, config.introspect(strType));
        assertSame(StringDeserializer.instance, strDeser);

        JavaType charSeqType = mapper.constructType(CharSequence.class);
        JsonDeserializer<?> charSeqDeser = factory.findDefaultDeserializer(ctxt, charSeqType, config.introspect(charSeqType));
        assertSame(StringDeserializer.instance, charSeqDeser);

        JavaType iterType = mapper.constructType(Iterable.class);
        JsonDeserializer<?> iterDeser = factory.findDefaultDeserializer(ctxt, iterType, config.introspect(iterType));
        assertNotNull(iterDeser);

        JavaType entryType = mapper.getTypeFactory().constructMapLikeType(Map.Entry.class, String.class, Integer.class);
        JsonDeserializer<?> entryDeser = factory.findDefaultDeserializer(ctxt, entryType, config.introspect(entryType));
        assertTrue(entryDeser instanceof MapEntryDeserializer);

        JavaType tokenType = mapper.constructType(TokenBuffer.class);
        JsonDeserializer<?> tokenDeser = factory.findDefaultDeserializer(ctxt, tokenType, config.introspect(tokenType));
        assertTrue(tokenDeser instanceof TokenBufferDeserializer);

        Class<?>[] jdkTypes = new Class<?>[] {
            int.class, Integer.class, long.class, Long.class, double.class, Double.class,
            boolean.class, Boolean.class, Date.class, Calendar.class, UUID.class,
            java.net.URL.class, java.net.URI.class, java.util.regex.Pattern.class,
            Locale.class, StackTraceElement.class
        };

        for (Class<?> cls : jdkTypes) {
            JavaType t = mapper.constructType(cls);
            JsonDeserializer<?> d = factory.findDefaultDeserializer(ctxt, t, config.introspect(t));
            assertNotNull("Default deserializer should exist for: " + cls.getName(), d);
        }
    }

    @Test(timeout = 4000)
    public void testArrayDeserializerVariants() throws Exception {
        ArrayType intArrayType = mapper.getTypeFactory().constructArrayType(int.class);
        JsonDeserializer<?> intArrDeser = factory.createArrayDeserializer(ctxt, intArrayType, config.introspect(intArrayType));
        assertNotNull(intArrDeser);

        ArrayType strArrayType = mapper.getTypeFactory().constructArrayType(String.class);
        JsonDeserializer<?> strArrDeser = factory.createArrayDeserializer(ctxt, strArrayType, config.introspect(strArrayType));
        assertSame(StringArrayDeserializer.instance, strArrDeser);

        ArrayType objArrayType = mapper.getTypeFactory().constructArrayType(Object.class);
        JsonDeserializer<?> objArrDeser = factory.createArrayDeserializer(ctxt, objArrayType, config.introspect(objArrayType));
        assertTrue(objArrDeser instanceof ObjectArrayDeserializer);
    }

    @Test(timeout = 4000)
    public void testCollectionDeserializerVariants() throws Exception {
        CollectionType enumSetType = mapper.getTypeFactory().constructCollectionType(EnumSet.class, TestSimpleEnum.class);
        JsonDeserializer<?> enumSetDeser = factory.createCollectionDeserializer(ctxt, enumSetType, config.introspect(enumSetType));
        assertTrue(enumSetDeser instanceof EnumSetDeserializer);

        CollectionType strListType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, String.class);
        JsonDeserializer<?> strListDeser = factory.createCollectionDeserializer(ctxt, strListType, config.introspect(strListType));
        assertTrue(strListDeser instanceof StringCollectionDeserializer);

        CollectionType intListType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Integer.class);
        JsonDeserializer<?> intListDeser = factory.createCollectionDeserializer(ctxt, intListType, config.introspect(intListType));
        assertTrue(intListDeser instanceof CollectionDeserializer);

        CollectionType abqType = mapper.getTypeFactory().constructCollectionType(ArrayBlockingQueue.class, String.class);
        JsonDeserializer<?> abqDeser = factory.createCollectionDeserializer(ctxt, abqType, config.introspect(abqType));
        assertTrue(abqDeser instanceof ArrayBlockingQueueDeserializer);
    }

    @Test(timeout = 4000)
    public void testMapDeserializerVariants() throws Exception {
        MapType enumMapType = mapper.getTypeFactory().constructMapType(EnumMap.class, TestSimpleEnum.class, String.class);
        JsonDeserializer<?> enumMapDeser = factory.createMapDeserializer(ctxt, enumMapType, config.introspect(enumMapType));
        assertTrue(enumMapDeser instanceof EnumMapDeserializer);

        MapType mapType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        JsonDeserializer<?> mapDeser = factory.createMapDeserializer(ctxt, mapType, config.introspect(mapType));
        assertTrue(mapDeser instanceof MapDeserializer);
    }

    @Test(timeout = 4000)
    public void testEnumDeserializersAndResolvers() throws Exception {
        JavaType simpleEnumType = mapper.constructType(TestSimpleEnum.class);
        JsonDeserializer<?> simpleEnumDeser = factory.createEnumDeserializer(ctxt, simpleEnumType, config.introspect(simpleEnumType));
        assertTrue(simpleEnumDeser instanceof EnumDeserializer);

        JavaType zeroArgEnumType = mapper.constructType(TestZeroArgEnum.class);
        JsonDeserializer<?> zeroArgDeser = factory.createEnumDeserializer(ctxt, zeroArgEnumType, config.introspect(zeroArgEnumType));
        assertNotNull(zeroArgDeser);

        JavaType oneArgEnumType = mapper.constructType(TestOneArgEnum.class);
        JsonDeserializer<?> oneArgDeser = factory.createEnumDeserializer(ctxt, oneArgEnumType, config.introspect(oneArgEnumType));
        assertNotNull(oneArgDeser);

        JavaType jsonValueEnumType = mapper.constructType(TestJsonValueEnum.class);
        JsonDeserializer<?> jsonValueDeser = factory.createEnumDeserializer(ctxt, jsonValueEnumType, config.introspect(jsonValueEnumType));
        assertNotNull(jsonValueDeser);

        EnumResolver enumResolver = factory.constructEnumResolver(TestSimpleEnum.class, config, null);
        assertNotNull(enumResolver);
        assertEquals(TestSimpleEnum.class, enumResolver.getEnumClass());
    }

    @Test(timeout = 4000)
    public void testTreeAndReferenceDeserializers() throws Exception {
        JavaType jsonNodeType = mapper.constructType(JsonNode.class);
        JsonDeserializer<?> nodeDeser = factory.createTreeDeserializer(config, jsonNodeType, config.introspect(jsonNodeType));
        assertTrue(nodeDeser instanceof JsonNodeDeserializer);

        JavaType objNodeType = mapper.constructType(ObjectNode.class);
        JsonDeserializer<?> objNodeDeser = factory.createTreeDeserializer(config, objNodeType, config.introspect(objNodeType));
        assertNotNull(objNodeDeser);

        JavaType arrNodeType = mapper.constructType(ArrayNode.class);
        JsonDeserializer<?> arrNodeDeser = factory.createTreeDeserializer(config, arrNodeType, config.introspect(arrNodeType));
        assertNotNull(arrNodeDeser);

        JavaType refType = ReferenceType.upgradeFrom(mapper.constructType(AtomicReference.class), mapper.constructType(String.class));
        JsonDeserializer<?> refDeser = factory.createReferenceDeserializer(ctxt, (ReferenceType) refType, config.introspect(refType));
        assertTrue(refDeser instanceof AtomicReferenceDeserializer);
    }

    @Test(timeout = 4000)
    public void testFindStdValueInstantiatorForJsonLocation() throws Exception {
        JavaType locType = mapper.constructType(JsonLocation.class);
        BeanDescription beanDesc = config.introspect(locType);
        ValueInstantiator inst = factory.findValueInstantiator(ctxt, beanDesc);
        assertNotNull(inst);
        assertTrue(inst instanceof JsonLocationInstantiator);
    }

    @Test(timeout = 4000)
    public void testDelegatingAndInjectableCreator() throws Exception {
        JavaType type = mapper.constructType(DelegatingInjectableBean.class);
        BeanDescription beanDesc = config.introspect(type);
        ValueInstantiator inst = factory.findValueInstantiator(ctxt, beanDesc);
        assertNotNull(inst);
        assertTrue(inst.canCreateUsingDelegate());

        JavaType factoryType = mapper.constructType(DelegatingInjectableFactoryBean.class);
        BeanDescription factoryBeanDesc = config.introspect(factoryType);
        ValueInstantiator factoryInst = factory.findValueInstantiator(ctxt, factoryBeanDesc);
        assertNotNull(factoryInst);
        assertTrue(factoryInst.canCreateUsingDelegate());
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testValueInstantiatorInstanceBoundaryConditions() throws Exception {
        AnnotatedClass ac = config.introspectClassAnnotations(String.class).getClassInfo();

        assertNull(factory._valueInstantiatorInstance(config, ac, null));

        ValueInstantiator customInst = new ValueInstantiator.Base(String.class);
        assertSame(customInst, factory._valueInstantiatorInstance(config, ac, customInst));

        assertNull(factory._valueInstantiatorInstance(config, ac, NoClass.class));

        ValueInstantiator fromClass = factory._valueInstantiatorInstance(config, ac, CustomConcreteValueInstantiator.class);
        assertNotNull(fromClass);
        assertTrue(fromClass instanceof CustomConcreteValueInstantiator);
    }

    @Test(timeout = 4000)
    public void testDeprecatedHelperMethodsContracts() throws Exception {
        BeanDescription beanDesc = config.introspect(mapper.constructType(String.class));
        JavaType baseType = mapper.constructType(String.class);

        JavaType modified = factory.modifyTypeByAnnotation(ctxt, beanDesc.getClassInfo(), baseType);
        assertEquals(baseType, modified);

        JavaType resolved = factory.resolveType(ctxt, beanDesc, baseType, null);
        assertEquals(baseType, resolved);

        assertNull(factory._findJsonValueFor(config, null));

        AnnotatedMethod jv = factory._findJsonValueFor(config, mapper.constructType(TestJsonValueEnum.class));
        assertNotNull(jv);

        assertNull(factory._findParamName(null, null));
        assertNull(factory._findImplicitParamName(null, null));
        assertNull(factory._findExplicitParamName(null, null));
        assertFalse(factory._hasExplicitParamName(null, null));
    }

    @Test(timeout = 4000)
    public void testAbstractTypeResolverEmptyMapping() throws Exception {
        JavaType type = mapper.constructType(CharSequence.class);
        JavaType mapped = factory.mapAbstractType(config, type);
        assertSame(type, mapped);
    }

    @Test(timeout = 4000)
    public void testRemappedTypeWithoutAndWithResolvers() throws Exception {
        assertNull(factory._findRemappedType(config, List.class));

        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(List.class, LinkedList.class);
        resolver.addMapping(Map.class, TreeMap.class);
        DeserializerFactory fWithResolver = factory.withAbstractTypeResolver(resolver);

        JavaType remappedList = fWithResolver._findRemappedType(config, List.class);
        assertNotNull(remappedList);
        assertEquals(LinkedList.class, remappedList.getRawClass());

        JavaType remappedMap = fWithResolver._findRemappedType(config, Map.class);
        assertNotNull(remappedMap);
        assertEquals(TreeMap.class, remappedMap.getRawClass());
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGET DEFECT: BasicDeserializerFactory.createKeyDeserializer() prematurely returns
     * _createEnumKeyDeserializer() for Enum types without running deserializer modifiers!
     *
     * As observed in Defects4J:
     *   TestCustomEnumKeyDeserializer::testCustomEnumValueAndKeyViaModifier
     *   --> InvalidFormatException: Can not deserialize Map key of type ... from String "REPlaceMENTS":
     *       not one of values excepted for Enum class...
     */
    @Test(timeout = 4000)
    public void testCustomEnumValueAndKeyViaModifier() throws Exception {
        ObjectMapper testMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        final AtomicBoolean modifierInvoked = new AtomicBoolean(false);

        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public KeyDeserializer modifyKeyDeserializer(DeserializationConfig cfg, JavaType type, final KeyDeserializer defaultKeyDeserializer) {
                if (type.getRawClass() == DefectKeyEnum.class) {
                    modifierInvoked.set(true);
                    return new KeyDeserializer() {
                        @Override
                        public Object deserializeKey(String key, DeserializationContext context) throws IOException {
                            if ("REPlaceMENTS".equals(key)) {
                                return DefectKeyEnum.REPLACEMENTS;
                            }
                            return defaultKeyDeserializer.deserializeKey(key, context);
                        }
                    };
                }
                return defaultKeyDeserializer;
            }
        });
        testMapper.registerModule(module);

        JavaType mapType = testMapper.getTypeFactory().constructMapType(HashMap.class, DefectKeyEnum.class, String.class);
        Map<DefectKeyEnum, String> result = testMapper.readValue("{\"REPlaceMENTS\": \"passed\"}", mapType);

        assertTrue("Defect verification: BeanDeserializerModifier.modifyKeyDeserializer MUST be called for Enum key types!",
                modifierInvoked.get());
        assertNotNull(result);
        assertEquals("passed", result.get(DefectKeyEnum.REPLACEMENTS));
    }

    @Test(timeout = 4000)
    public void testEnumKeyDeserializerModifierDirectInvocation() throws Exception {
        final KeyDeserializer mockKeyDeser = new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext context) {
                return DefectKeyEnum.ROOT_DIRECTORY;
            }
        };

        DeserializerFactory factoryWithMod = factory.withDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public KeyDeserializer modifyKeyDeserializer(DeserializationConfig cfg, JavaType type, KeyDeserializer defaultKeyDeserializer) {
                if (type.getRawClass() == DefectKeyEnum.class) {
                    return mockKeyDeser;
                }
                return defaultKeyDeserializer;
            }
        });

        JavaType enumType = mapper.constructType(DefectKeyEnum.class);
        KeyDeserializer actualKeyDeser = factoryWithMod.createKeyDeserializer(ctxt, enumType);
        assertSame("modifyKeyDeserializer must post-process and return the modified KeyDeserializer for Enums",
                mockKeyDeser, actualKeyDeser);
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMapAbstractTypeCycleOrNonSubtypeResolutionThrows() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(Number.class, Object.class); // Object is not a subtype of Number
        DeserializerFactory f = factory.withAbstractTypeResolver(resolver);
        f.mapAbstractType(config, mapper.constructType(Number.class));
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testValueInstantiatorInstanceNonClassThrows() throws Exception {
        AnnotatedClass ac = config.introspectClassAnnotations(String.class).getClassInfo();
        factory._valueInstantiatorInstance(config, ac, 12345);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testValueInstantiatorInstanceNotAssignableThrows() throws Exception {
        AnnotatedClass ac = config.introspectClassAnnotations(String.class).getClassInfo();
        factory._valueInstantiatorInstance(config, ac, String.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEnumMapWithoutEnumKeyThrows() throws Exception {
        MapType invalidEnumMapType = mapper.getTypeFactory().constructMapType(EnumMap.class, String.class, String.class);
        factory.createMapDeserializer(ctxt, invalidEnumMapType, config.introspect(invalidEnumMapType));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNonConcreteCollectionWithoutFallbackThrows() throws Exception {
        CollectionType unmappedCollType = mapper.getTypeFactory().constructCollectionType(CustomNonFallbackCollection.class, String.class);
        factory.createCollectionDeserializer(ctxt, unmappedCollType, config.introspectClassAnnotations(CustomNonFallbackCollection.class));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNonConcreteMapWithoutFallbackThrows() throws Exception {
        MapType unmappedMapType = mapper.getTypeFactory().constructMapType(CustomNonFallbackMap.class, String.class, String.class);
        factory.createMapDeserializer(ctxt, unmappedMapType, config.introspectClassAnnotations(CustomNonFallbackMap.class));
    }

    @Test(timeout = 4000)
    public void testNonStaticInnerClassCreatorThrows() throws Exception {
        BeanDescription desc = config.introspect(mapper.constructType(NonStaticOuter.NonStaticInner.class));
        try {
            factory.findValueInstantiator(ctxt, desc);
            fail("Expected IllegalArgumentException for non-static inner class with @JsonCreator");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Non-static inner classes like"));
        }
    }

    @Test(timeout = 4000)
    public void testMultiArgConstructorMissingNameThrows() throws Exception {
        BeanDescription desc = config.introspect(mapper.constructType(BadMultiArgCtorBean.class));
        try {
            factory.findValueInstantiator(ctxt, desc);
            fail("Expected IllegalArgumentException for multi-arg constructor missing parameter names");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("has no property name annotation"));
        }
    }

    @Test(timeout = 4000)
    public void testMultiArgFactoryMissingNameThrows() throws Exception {
        BeanDescription desc = config.introspect(mapper.constructType(BadMultiArgFactoryBean.class));
        try {
            factory.findValueInstantiator(ctxt, desc);
            fail("Expected IllegalArgumentException for multi-arg factory method missing parameter names");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("has no property name annotation"));
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEnumKeyDeserializerUnsuitableFactoryArgCountThrows() throws Exception {
        JavaType type = mapper.constructType(BadEnumCreatorArgCount.class);
        factory.createKeyDeserializer(ctxt, type);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEnumKeyDeserializerUnsuitableFactoryArgTypeThrows() throws Exception {
        JavaType type = mapper.constructType(BadEnumCreatorArgType.class);
        factory.createKeyDeserializer(ctxt, type);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testBrokenRegisteredValueInstantiatorsReportsException() throws Exception {
        DeserializerFactory brokenFactory = factory.withValueInstantiators(new ValueInstantiators.Base() {
            @Override
            public ValueInstantiator findValueInstantiator(DeserializationConfig cfg, BeanDescription beanDesc, ValueInstantiator defaultInstantiator) {
                return null;
            }
        });
        JavaType type = mapper.constructType(SimpleBean.class);
        brokenFactory.findValueInstantiator(ctxt, config.introspect(type));
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle, Introspection & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testModifiersInvokedAcrossAllContainerAndReferenceTypes() throws Exception {
        final AtomicBoolean arrayMod = new AtomicBoolean(false);
        final AtomicBoolean collMod = new AtomicBoolean(false);
        final AtomicBoolean mapMod = new AtomicBoolean(false);
        final AtomicBoolean refMod = new AtomicBoolean(false);
        final AtomicBoolean enumMod = new AtomicBoolean(false);

        DeserializerFactory f = factory.withDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyArrayDeserializer(DeserializationConfig cfg, ArrayType type, BeanDescription beanDesc, JsonDeserializer<?> deser) {
                arrayMod.set(true);
                return deser;
            }
            @Override
            public JsonDeserializer<?> modifyCollectionDeserializer(DeserializationConfig cfg, CollectionType type, BeanDescription beanDesc, JsonDeserializer<?> deser) {
                collMod.set(true);
                return deser;
            }
            @Override
            public JsonDeserializer<?> modifyMapDeserializer(DeserializationConfig cfg, MapType type, BeanDescription beanDesc, JsonDeserializer<?> deser) {
                mapMod.set(true);
                return deser;
            }
            @Override
            public JsonDeserializer<?> modifyReferenceDeserializer(DeserializationConfig cfg, ReferenceType type, BeanDescription beanDesc, JsonDeserializer<?> deser) {
                refMod.set(true);
                return deser;
            }
            @Override
            public JsonDeserializer<?> modifyEnumDeserializer(DeserializationConfig cfg, JavaType type, BeanDescription beanDesc, JsonDeserializer<?> deser) {
                enumMod.set(true);
                return deser;
            }
        });

        ArrayType arrType = mapper.getTypeFactory().constructArrayType(String.class);
        f.createArrayDeserializer(ctxt, arrType, config.introspect(arrType));
        assertTrue(arrayMod.get());

        CollectionType collType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, String.class);
        f.createCollectionDeserializer(ctxt, collType, config.introspect(collType));
        assertTrue(collMod.get());

        MapType mapType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
        f.createMapDeserializer(ctxt, mapType, config.introspect(mapType));
        assertTrue(mapMod.get());

        JavaType refType = ReferenceType.upgradeFrom(mapper.constructType(AtomicReference.class), mapper.constructType(String.class));
        f.createReferenceDeserializer(ctxt, (ReferenceType) refType, config.introspect(refType));
        assertTrue(refMod.get());

        JavaType enumType = mapper.constructType(TestSimpleEnum.class);
        f.createEnumDeserializer(ctxt, enumType, config.introspect(enumType));
        assertTrue(enumMod.get());
    }

    @Test(timeout = 4000)
    public void testFindPropertyTypeDeserializerResolution() throws Exception {
        JavaType polyType = mapper.constructType(PolymorphicTestBase.class);
        TypeDeserializer td = factory.findPropertyTypeDeserializer(config, polyType, null);
        assertNotNull("TypeDeserializer should be found for @JsonTypeInfo annotated base class", td);

        JavaType plainType = mapper.constructType(SimpleBean.class);
        TypeDeserializer tdPlain = factory.findPropertyTypeDeserializer(config, plainType, null);
        assertNull("TypeDeserializer should be null when no polymorphic annotations present", tdPlain);
    }

    @Test(timeout = 4000)
    public void testCustomDeserializersAndKeyDeserializersRegistration() throws Exception {
        SimpleDeserializers customDesers = new SimpleDeserializers();
        final JsonDeserializer<TestSimpleEnum> dummyEnumDeser = new StdScalarDeserializer<TestSimpleEnum>(TestSimpleEnum.class) {
            @Override
            public TestSimpleEnum deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext context) {
                return TestSimpleEnum.ONE;
            }
        };
        customDesers.addDeserializer(TestSimpleEnum.class, dummyEnumDeser);
        DeserializerFactory fDesers = factory.withAdditionalDeserializers(customDesers);

        JavaType enumType = mapper.constructType(TestSimpleEnum.class);
        JsonDeserializer<?> resolvedEnumDeser = fDesers.createEnumDeserializer(ctxt, enumType, config.introspect(enumType));
        assertSame(dummyEnumDeser, resolvedEnumDeser);

        SimpleKeyDeserializers customKeyDesers = new SimpleKeyDeserializers();
        final KeyDeserializer dummyStringKeyDeser = new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext context) {
                return "CUSTOM:" + key;
            }
        };
        customKeyDesers.addDeserializer(String.class, dummyStringKeyDeser);
        DeserializerFactory fKeyDesers = factory.withAdditionalKeyDeserializers(customKeyDesers);

        JavaType stringType = mapper.constructType(String.class);
        KeyDeserializer resolvedKeyDeser = fKeyDesers.createKeyDeserializer(ctxt, stringType);
        assertSame(dummyStringKeyDeser, resolvedKeyDeser);
    }

    // =========================================================================
    // Test Supporting Types & Fixtures
    // =========================================================================

    public enum DefectKeyEnum {
        ROOT_DIRECTORY,
        REPLACEMENTS,
        LICENSE_STRING
    }

    public enum TestSimpleEnum {
        ONE, TWO
    }

    public enum TestZeroArgEnum {
        A, B;
        @JsonCreator
        public static TestZeroArgEnum makeDefault() {
            return A;
        }
    }

    public enum TestOneArgEnum {
        X, Y;
        @JsonCreator
        public static TestOneArgEnum fromString(String v) {
            return "x".equalsIgnoreCase(v) ? X : Y;
        }
    }

    public enum TestJsonValueEnum {
        VALUE_1;
        @JsonValue
        public String toValue() {
            return "val_1";
        }
    }

    public enum BadEnumCreatorArgCount {
        A, B;
        @JsonCreator
        public static BadEnumCreatorArgCount create(String a, String b) {
            return A;
        }
    }

    public enum BadEnumCreatorArgType {
        A, B;
        @JsonCreator
        public static BadEnumCreatorArgType create(int val) {
            return A;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    public static abstract class PolymorphicTestBase {}

    public static class SimpleBean {
        public String name;
    }

    public static class CustomConcreteValueInstantiator extends ValueInstantiator.Base {
        public CustomConcreteValueInstantiator() {
            super(String.class);
        }
    }

    public interface CustomNonFallbackCollection<E> extends Collection<E> {}

    public interface CustomNonFallbackMap<K, V> extends Map<K, V> {}

    public static class NonStaticOuter {
        public class NonStaticInner {
            @JsonCreator
            public NonStaticInner(@JsonProperty("val") int val) {}
        }
    }

    public static class BadMultiArgCtorBean {
        @JsonCreator
        public BadMultiArgCtorBean(int a, int b) {}
    }

    public static class BadMultiArgFactoryBean {
        @JsonCreator
        public static BadMultiArgFactoryBean create(int a, int b) {
            return new BadMultiArgFactoryBean();
        }
    }

    public static class DelegatingInjectableBean {
        public final String value;
        public final int injected;

        @JsonCreator
        public DelegatingInjectableBean(String value, @JacksonInject int injected) {
            this.value = value;
            this.injected = injected;
        }
    }

    public static class DelegatingInjectableFactoryBean {
        @JsonCreator
        public static DelegatingInjectableFactoryBean create(String value, @JacksonInject int injected) {
            return new DelegatingInjectableFactoryBean();
        }
    }
}