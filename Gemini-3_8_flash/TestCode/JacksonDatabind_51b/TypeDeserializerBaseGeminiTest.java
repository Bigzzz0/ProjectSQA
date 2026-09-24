package com.fasterxml.jackson.databind.jsontype.impl;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.jsontype.impl.TypeDeserializerBase
 *
 * 1. Defect-Targeted Branch Zone (databind#1270 / Defects4J Ground Truth):
 *    - In `_findDeserializer(DeserializationContext, String)`:
 *      Condition: `(_baseType != null) && _baseType.getClass() == type.getClass()`
 *      Defect: When `type` resolved by a custom `TypeIdResolver` already has generic parameters
 *      (e.g., specialized type `Base<CustomPayload>`), calling
 *      `constructSpecializedType(_baseType, type.getRawClass())` unconditionally strips
 *      the generic parameter information from `type` if `_baseType` lacks it (e.g., `Base<?>`).
 *      Target Test: Verify that generic type arguments resolved via custom type ID resolver
 *      are retained rather than clobbered back to raw/Object bounds.
 *
 * 2. Core Functional Logic & State Transitions:
 *    - Constructors: primary constructor initializing fields; copy constructor `(src, property)`.
 *    - Accessors: `baseTypeName()`, `getPropertyName()`, `getTypeIdResolver()`, `getDefaultImpl()`, `toString()`.
 *    - `_findDeserializer`: caching behavior in `_deserializers` ConcurrentHashMap.
 *    - `_findDefaultImplDeserializer`: handling null `_defaultImpl`, `FAIL_ON_INVALID_SUBTYPE`,
 *      bogus classes (e.g. Void.class, NoClass.class), and synchronized lazy caching.
 *    - `_deserializeWithNativeTypeId`: null type ID path, non-null type ID path, non-String type ID conversion.
 *    - `_handleUnknownTypeId`: formatting extra description with `TypeIdResolverBase` vs generic `TypeIdResolver`.
 */
public class TypeDeserializerBaseGeminiTest {

    // Concrete test implementation of TypeDeserializerBase
    private static class ConcreteTypeDeserializer extends TypeDeserializerBase {
        private static final long serialVersionUID = 1L;

        public ConcreteTypeDeserializer(JavaType baseType, TypeIdResolver idRes,
                String typePropertyName, boolean typeIdVisible, JavaType defaultImpl) {
            super(baseType, idRes, typePropertyName, typeIdVisible, defaultImpl);
        }

        public ConcreteTypeDeserializer(ConcreteTypeDeserializer src, BeanProperty prop) {
            super(src, prop);
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) {
            return new ConcreteTypeDeserializer(this, prop);
        }

        @Override
        public JsonTypeInfo.As getTypeInclusion() {
            return JsonTypeInfo.As.PROPERTY;
        }

        @Override
        public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }
    }

    // Helper classes for Defect databind#1270 reproduction
    static class PolyPayload {
        public String name;
    }

    static class BaseContainer<T> {
        public T options;
    }

    static class CustomContainerResolver extends TypeIdResolverBase {
        @Override
        public void init(JavaType baseType) { }

        @Override
        public String idFromValue(Object value) { return "custom"; }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) { return "custom"; }

        @Override
        public JavaType typeFromId(com.fasterxml.jackson.databind.DatabindContext context, String id) {
            if ("custom".equals(id)) {
                return context.getTypeFactory().constructParametricType(BaseContainer.class, PolyPayload.class);
            }
            return null;
        }

        @Override
        public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }

        @Override
        public String getDescForKnownTypeIds() {
            return "custom";
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonTypeIdResolver(CustomContainerResolver.class)
    static class PolymorphicWrapper extends BaseContainer<PolyPayload> {
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone (databind#1270)
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testCustomTypeIdResolverGenericRetentionDefect1270() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.getTypeFactory().constructType(BaseContainer.class);
        CustomContainerResolver resolver = new CustomContainerResolver();

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, resolver, "type", false, null);

        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt instanceof com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) {
            ctxt = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) ctxt)
                    .createInstance(mapper.getDeserializationConfig(),
                            mapper.getFactory().createParser("{}"), mapper.getInjectableValues());
        }

        // Resolves "custom" which returns BaseContainer<PolyPayload>
        JsonDeserializer<Object> foundDeser = deser._findDeserializer(ctxt, "custom");
        assertNotNull("Deserializer should be found for custom type id", foundDeser);

        // When deserializing JSON containing 'options', it must deserialize 'options' as PolyPayload, NOT Map
        String json = "{\"type\":\"custom\",\"options\":{\"name\":\"defect_fixed\"}}";
        BaseContainer<?> result = mapper.readValue(json, PolymorphicWrapper.class);
        assertNotNull(result);
        assertNotNull(result.options);
        assertTrue("Options should be resolved to PolyPayload rather than LinkedHashMap",
                result.options instanceof PolyPayload);
        assertEquals("defect_fixed", ((PolyPayload) result.options).name);
    }

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testConstructionAndAccessors() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(String.class);
        JavaType defaultImpl = tf.constructType(Integer.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, tf);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idRes, "myProp", true, defaultImpl);

        assertEquals("java.lang.String", deser.baseTypeName());
        assertEquals("myProp", deser.getPropertyName());
        assertSame(idRes, deser.getTypeIdResolver());
        assertEquals(Integer.class, deser.getDefaultImpl());
        assertEquals(JsonTypeInfo.As.PROPERTY, deser.getTypeInclusion());

        String str = deser.toString();
        assertTrue(str.contains("ConcreteTypeDeserializer"));
        assertTrue(str.contains("base-type:"));
        assertTrue(str.contains("id-resolver:"));

        // Copy constructor check
        ConcreteTypeDeserializer copy = (ConcreteTypeDeserializer) deser.forProperty(null);
        assertEquals("java.lang.String", copy.baseTypeName());
        assertEquals("myProp", copy.getPropertyName());
        assertSame(idRes, copy.getTypeIdResolver());
        assertEquals(Integer.class, copy.getDefaultImpl());
    }

    @Test(timeout = 4000)
    public void testFindDeserializerCaching() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = mapper.getTypeFactory();
        JavaType baseType = tf.constructType(Object.class);
        TypeIdResolver idRes = new TypeNameIdResolver(mapper.getDeserializationConfig(),
                baseType, new java.util.HashMap<String, String>(),
                new java.util.HashMap<String, JavaType>());

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);

        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt instanceof com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) {
            ctxt = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) ctxt)
                    .createInstance(mapper.getDeserializationConfig(),
                            mapper.getFactory().createParser("{}"), mapper.getInjectableValues());
        }

        // Put a pre-cached deserializer
        JsonDeserializer<Object> dummyDeser = NullifyingDeserializer.instance;
        deser._deserializers.put("cachedType", dummyDeser);

        JsonDeserializer<Object> fetched = deser._findDeserializer(ctxt, "cachedType");
        assertSame(dummyDeser, fetched);
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Extremes
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testNullTypePropertyNameBecomesEmpty() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(String.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, tf);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idRes, null, false, null);

        assertEquals("", deser.getPropertyName());
        assertNull(deser.getDefaultImpl());
    }

    @Test(timeout = 4000)
    public void testDefaultImplNullHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = mapper.getTypeFactory();
        JavaType baseType = tf.constructType(Object.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, tf);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);

        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt instanceof com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) {
            ctxt = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) ctxt)
                    .createInstance(mapper.getDeserializationConfig(),
                            mapper.getFactory().createParser("{}"), mapper.getInjectableValues());
        }

        // When FAIL_ON_INVALID_SUBTYPE is true and defaultImpl is null => returns null
        ctxt = ctxt.setAttribute("dummy", "dummy"); // keeps ctxt alive
        assertTrue(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE));
        assertNull(deser._findDefaultImplDeserializer(ctxt));

        // When FAIL_ON_INVALID_SUBTYPE is false => returns NullifyingDeserializer.instance
        ObjectMapper lenientMapper = new ObjectMapper();
        lenientMapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        DeserializationContext lenientCtxt = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext)
                lenientMapper.getDeserializationContext()).createInstance(
                        lenientMapper.getDeserializationConfig(),
                        lenientMapper.getFactory().createParser("{}"), lenientMapper.getInjectableValues());

        JsonDeserializer<Object> nullDeser = deser._findDefaultImplDeserializer(lenientCtxt);
        assertSame(NullifyingDeserializer.instance, nullDeser);
    }

    @Test(timeout = 4000)
    public void testDefaultImplBogusClassVoid() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = mapper.getTypeFactory();
        JavaType baseType = tf.constructType(Object.class);
        JavaType voidType = tf.constructType(Void.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, tf);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, voidType);

        DeserializationContext ctxt = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext)
                mapper.getDeserializationContext()).createInstance(
                        mapper.getDeserializationConfig(),
                        mapper.getFactory().createParser("{}"), mapper.getInjectableValues());

        JsonDeserializer<Object> found = deser._findDefaultImplDeserializer(ctxt);
        assertSame(NullifyingDeserializer.instance, found);
    }

    @Test(timeout = 4000)
    public void testDefaultImplSynchronizationAndCaching() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = mapper.getTypeFactory();
        JavaType baseType = tf.constructType(Object.class);
        JavaType defaultType = tf.constructType(String.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, tf);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, defaultType);

        DeserializationContext ctxt = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext)
                mapper.getDeserializationContext()).createInstance(
                        mapper.getDeserializationConfig(),
                        mapper.getFactory().createParser("{}"), mapper.getInjectableValues());

        assertNull(deser._defaultImplDeserializer);
        JsonDeserializer<Object> d1 = deser._findDefaultImplDeserializer(ctxt);
        assertNotNull(d1);
        assertSame(d1, deser._defaultImplDeserializer);

        // Second lookup should return cached deserializer
        JsonDeserializer<Object> d2 = deser._findDefaultImplDeserializer(ctxt);
        assertSame(d1, d2);
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testDeserializeWithNativeTypeIdNullTypeIdThrowsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = mapper.getTypeFactory();
        JavaType baseType = tf.constructType(Object.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, tf);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);

        JsonParser parser = mapper.getFactory().createParser("{}");
        parser.nextToken();
        DeserializationContext ctxt = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext)
                mapper.getDeserializationContext()).createInstance(
                        mapper.getDeserializationConfig(), parser, mapper.getInjectableValues());

        try {
            deser._deserializeWithNativeTypeId(parser, ctxt, null);
            fail("Should have failed when no native type id was found and no default impl was configured");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("No (native) type id found"));
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithNativeTypeIdNonStringConversion() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = mapper.getTypeFactory();
        JavaType baseType = tf.constructType(Object.class);
        TypeIdResolver idRes = new TypeIdResolverBase() {
            @Override
            public void init(JavaType baseType) {}
            @Override
            public String idFromValue(Object value) { return "123"; }
            @Override
            public String idFromValueAndType(Object value, Class<?> suggestedType) { return "123"; }
            @Override
            public JavaType typeFromId(com.fasterxml.jackson.databind.DatabindContext context, String id) {
                if ("123".equals(id)) {
                    return TypeFactory.defaultInstance().constructType(Integer.class);
                }
                return null;
            }
            @Override
            public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
        };

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);

        JsonParser parser = mapper.getFactory().createParser("42");
        parser.nextToken();
        DeserializationContext ctxt = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext)
                mapper.getDeserializationContext()).createInstance(
                        mapper.getDeserializationConfig(), parser, mapper.getInjectableValues());

        // Pass an Integer object 123 as the typeId, should invoke String.valueOf(typeId)
        Object result = deser._deserializeWithNativeTypeId(parser, ctxt, 123);
        assertEquals(42, result);
    }

    @Test(timeout = 4000)
    public void testHandleUnknownTypeIdFormatsExtraDesc() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = mapper.getTypeFactory();
        JavaType baseType = tf.constructType(String.class);

        // 1. With TypeIdResolverBase that has known type IDs
        TypeIdResolverBase resolverWithKnown = new TypeIdResolverBase() {
            @Override public void init(JavaType baseType) {}
            @Override public String idFromValue(Object value) { return null; }
            @Override public String idFromValueAndType(Object value, Class<?> suggestedType) { return null; }
            @Override public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
            @Override public String getDescForKnownTypeIds() { return "knownA, knownB"; }
        };

        ConcreteTypeDeserializer deser1 = new ConcreteTypeDeserializer(
                baseType, resolverWithKnown, "type", false, null);

        DeserializationContext ctxt = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext)
                mapper.getDeserializationContext()).createInstance(
                        mapper.getDeserializationConfig(),
                        mapper.getFactory().createParser("{}"), mapper.getInjectableValues());

        try {
            deser1._handleUnknownTypeId(ctxt, "unknownId", resolverWithKnown, baseType);
            fail("Expected JsonMappingException for unknown type id");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("known type ids = knownA, knownB"));
        }

        // 2. With TypeIdResolverBase where getDescForKnownTypeIds() returns null
        TypeIdResolverBase resolverWithoutKnown = new TypeIdResolverBase() {
            @Override public void init(JavaType baseType) {}
            @Override public String idFromValue(Object value) { return null; }
            @Override public String idFromValueAndType(Object value, Class<?> suggestedType) { return null; }
            @Override public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
            @Override public String getDescForKnownTypeIds() { return null; }
        };

        ConcreteTypeDeserializer deser2 = new ConcreteTypeDeserializer(
                baseType, resolverWithoutKnown, "type", false, null);

        try {
            deser2._handleUnknownTypeId(ctxt, "unknownId", resolverWithoutKnown, baseType);
            fail("Expected JsonMappingException for unknown type id");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("known type ids are not statically known"));
        }

        // 3. With generic TypeIdResolver not implementing TypeIdResolverBase
        TypeIdResolver genericResolver = new TypeIdResolver() {
            @Override public void init(JavaType baseType) {}
            @Override public String idFromValue(Object value) { return null; }
            @Override public String idFromValueAndType(Object value, Class<?> suggestedType) { return null; }
            @Override public String idFromBaseType() { return null; }
            @Override public JavaType typeFromId(String id) { return null; }
            @Override public JavaType typeFromId(com.fasterxml.jackson.databind.DatabindContext context, String id) { return null; }
            @Override public String getDescForKnownTypeIds() { return null; }
            @Override public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
        };

        ConcreteTypeDeserializer deser3 = new ConcreteTypeDeserializer(
                baseType, genericResolver, "type", false, null);

        try {
            deser3._handleUnknownTypeId(ctxt, "unknownId", genericResolver, baseType);
            fail("Expected JsonMappingException for unknown type id");
        } catch (JsonMappingException e) {
            assertNotNull(e.getMessage());
        }
    }

    /*
    /**********************************************************
    /* Partition E: Object Lifecycle & Deprecated Methods
    /**********************************************************
     */

    @SuppressWarnings("deprecation")
    @Test(timeout = 4000)
    public void testDeprecatedDeserializeWithNativeTypeIdNoArg() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = mapper.getTypeFactory();
        JavaType baseType = tf.constructType(Integer.class);
        JavaType defaultType = tf.constructType(Integer.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, tf);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, defaultType);

        JsonParser parser = mapper.getFactory().createParser("123");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        DeserializationContext ctxt = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext)
                mapper.getDeserializationContext()).createInstance(
                        mapper.getDeserializationConfig(), parser, mapper.getInjectableValues());

        // Default parser has null native type id; falls back to defaultImpl deserializer
        Object result = deser._deserializeWithNativeTypeId(parser, ctxt);
        assertEquals(123, result);
    }
}