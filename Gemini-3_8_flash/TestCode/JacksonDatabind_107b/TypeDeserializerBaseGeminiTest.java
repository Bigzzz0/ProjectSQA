package com.fasterxml.jackson.databind.jsontype.impl;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.fasterxml.jackson.databind.jsontype.impl.TypeDeserializerBase
 *
 * 1. Defect-Targeted Zone (Jackson databind #2221):
 *    - Branch: _handleUnknownTypeId returning null via DeserializationProblemHandler.
 *    - Bug: In defective versions, _findDeserializer() returns null when actual == null,
 *      causing subsequent callers (e.g. _deserializeWithNativeTypeId) to throw NullPointerException
 *      on deser.deserialize(jp, ctxt) instead of gracefully handling/skipping with NullifyingDeserializer.
 *
 * 2. Equivalence Partitioning & Boundary Value Analysis (BVA):
 *    - Constructors:
 *      * Initial constructor: non-null vs null typePropertyName (testing ClassUtil.nonNullString fallback).
 *      * Copy constructor: verifies state propagation including _defaultImplDeserializer and property binding.
 *    - Accessors:
 *      * baseTypeName(), getPropertyName(), getTypeIdResolver(), getDefaultImpl(), baseType(), toString().
 *      * defaultImpl null vs non-null vs bogus class (Void.class).
 *    - _findDeserializer(DeserializationContext, String):
 *      * Cache hit (deserializer already in _deserializers).
 *      * Cache miss -> _idResolver resolves to non-null JavaType.
 *        - Base type class match & !type.hasGenericTypes(): specialization via constructSpecializedType.
 *        - Base type class match & type.hasGenericTypes(): specialization bypassed.
 *        - Base type null or mismatched type class: specialization bypassed.
 *      * Cache miss -> _idResolver returns null:
 *        - defaultImpl present -> _findDefaultImplDeserializer() used and cached.
 *        - defaultImpl null + FAIL_ON_INVALID_SUBTYPE disabled -> NullifyingDeserializer returned.
 *        - defaultImpl null + FAIL_ON_INVALID_SUBTYPE enabled -> invokes _handleUnknownTypeId.
 *    - _findDefaultImplDeserializer(DeserializationContext):
 *      * defaultImpl == null (FAIL_ON_INVALID_SUBTYPE enabled -> null, disabled -> NullifyingDeserializer).
 *      * defaultImpl != null (bogus class Void.class -> NullifyingDeserializer).
 *      * defaultImpl != null (valid class -> contextual deserializer fetched and cached in _defaultImplDeserializer).
 *    - Native Type ID Deserialization:
 *      * _deserializeWithNativeTypeId(JsonParser, DeserializationContext) [deprecated].
 *      * _deserializeWithNativeTypeId(JsonParser, DeserializationContext, Object typeId):
 *        - typeId == null & defaultImpl == null -> reports input mismatch / throws JsonMappingException.
 *        - typeId == null & defaultImpl != null -> deserializes with default implementation.
 *        - typeId != null (String vs non-String/Integer) -> finds deserializer and deserializes.
 *    - Diagnostic Helpers:
 *      * _handleUnknownTypeId: known type IDs present vs null; property present vs null.
 *      * _handleMissingTypeId: delegate to context.
 */

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class TypeDeserializerBaseGeminiTest {

    // Concrete test harness subclass exposing TypeDeserializerBase methods
    static class ConcreteTypeDeserializer extends TypeDeserializerBase {
        private static final long serialVersionUID = 1L;

        public ConcreteTypeDeserializer(JavaType baseType, TypeIdResolver idRes,
                String typePropertyName, boolean typeIdVisible, JavaType defaultImpl) {
            super(baseType, idRes, typePropertyName, typeIdVisible, defaultImpl);
        }

        public ConcreteTypeDeserializer(TypeDeserializerBase src, BeanProperty prop) {
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
    }

    // Controllable TypeIdResolver stub
    static class StubTypeIdResolver extends TypeIdResolverBase {
        private final Map<String, JavaType> _typeMap = new HashMap<String, JavaType>();
        private String _knownTypeIdsDesc = null;

        public StubTypeIdResolver(JavaType baseType, TypeFactory typeFactory) {
            super(baseType, typeFactory);
        }

        public void register(String id, JavaType type) {
            _typeMap.put(id, type);
        }

        public void setKnownTypeIdsDesc(String desc) {
            _knownTypeIdsDesc = desc;
        }

        @Override
        public String idFromValue(Object value) {
            return null;
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            return null;
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) {
            return _typeMap.get(id);
        }

        @Override
        public String getDescForKnownTypeIds() {
            return _knownTypeIdsDesc;
        }
    }

    private DefaultDeserializationContext createCtxt(ObjectMapper mapper, String json) throws Exception {
        JsonParser p = mapper.getFactory().createParser(json);
        return ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), p, null);
    }

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testConstructionAndAccessors() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(Number.class);
        JavaType defaultType = mapper.constructType(Integer.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idRes, "myType", true, defaultType);

        assertEquals("java.lang.Number", deser.baseTypeName());
        assertEquals("myType", deser.getPropertyName());
        assertSame(idRes, deser.getTypeIdResolver());
        assertEquals(Integer.class, deser.getDefaultImpl());
        assertSame(baseType, deser.baseType());
        assertTrue(deser.toString().contains("java.lang.Number"));
        assertTrue(deser.toString().contains("StubTypeIdResolver"));
    }

    @Test(timeout = 4000)
    public void testForPropertyCopyConstructor() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(Number.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());
        ConcreteTypeDeserializer original = new ConcreteTypeDeserializer(
                baseType, idRes, "@type", false, null);

        BeanProperty.Std prop = new BeanProperty.Std(
                PropertyName.construct("testProp"), baseType, null, null, PropertyMetadata.STD_OPTIONAL);

        TypeDeserializer copy = original.forProperty(prop);
        assertNotSame(original, copy);
        assertTrue(copy instanceof ConcreteTypeDeserializer);
        ConcreteTypeDeserializer concreteCopy = (ConcreteTypeDeserializer) copy;
        assertEquals(original.getPropertyName(), concreteCopy.getPropertyName());
        assertSame(original.getTypeIdResolver(), concreteCopy.getTypeIdResolver());
        assertSame(original.baseType(), concreteCopy.baseType());
    }

    @Test(timeout = 4000)
    public void testFindDeserializerResolvedDirectlyAndCached() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper, "123");

        JavaType baseType = mapper.constructType(Number.class);
        JavaType intType = mapper.constructType(Integer.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());
        idRes.register("int", intType);

        ConcreteTypeDeserializer deserBase = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);

        JsonDeserializer<Object> deser1 = deserBase._findDeserializer(ctxt, "int");
        assertNotNull("Deserializer must be resolved for mapped id", deser1);

        // Verify cache hit branch on second invocation
        JsonDeserializer<Object> deser2 = deserBase._findDeserializer(ctxt, "int");
        assertSame("Subsequent resolution must return cached instance", deser1, deser2);
    }

    @Test(timeout = 4000)
    public void testFindDeserializerTypeSpecialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper, "[]");

        JavaType listOfStrings = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        JavaType rawArrayList = mapper.getTypeFactory().constructRawCollectionType(java.util.ArrayList.class);

        StubTypeIdResolver idRes = new StubTypeIdResolver(listOfStrings, mapper.getTypeFactory());
        idRes.register("list", rawArrayList);

        ConcreteTypeDeserializer deserBase = new ConcreteTypeDeserializer(
                listOfStrings, idRes, "type", false, null);

        JsonDeserializer<Object> deser = deserBase._findDeserializer(ctxt, "list");
        assertNotNull(deser);
    }

    @Test(timeout = 4000)
    public void testFindDefaultImplDeserializerCaching() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper, "\"hello\"");

        JavaType baseType = mapper.constructType(CharSequence.class);
        JavaType defaultType = mapper.constructType(String.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());

        ConcreteTypeDeserializer deserBase = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, defaultType);

        JsonDeserializer<Object> d1 = deserBase._findDefaultImplDeserializer(ctxt);
        assertNotNull(d1);
        JsonDeserializer<Object> d2 = deserBase._findDefaultImplDeserializer(ctxt);
        assertSame("Default impl deserializer must be cached in _defaultImplDeserializer", d1, d2);
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Extremes
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testNullTypePropertyNameNormalizesToEmptyString() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(Object.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idRes, null, false, null);

        assertEquals("Null property name should default to empty string", "", deser.getPropertyName());
    }

    @Test(timeout = 4000)
    public void testNullDefaultImplAccessor() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(Object.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);

        assertNull("Default impl class must be null when _defaultImpl is null", deser.getDefaultImpl());
    }

    @Test(timeout = 4000)
    public void testBogusClassDefaultImplYieldsNullifyingDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper, "null");

        JavaType baseType = mapper.constructType(Object.class);
        JavaType voidType = mapper.constructType(Void.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, voidType);

        JsonDeserializer<Object> defaultDeser = deser._findDefaultImplDeserializer(ctxt);
        assertSame(NullifyingDeserializer.instance, defaultDeser);
    }

    @Test(timeout = 4000)
    public void testNullDefaultImplWithFailOnInvalidSubtypeDisabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        DefaultDeserializationContext ctxt = createCtxt(mapper, "123");

        JavaType baseType = mapper.constructType(Number.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);

        JsonDeserializer<Object> defaultDeser = deser._findDefaultImplDeserializer(ctxt);
        assertSame("When FAIL_ON_INVALID_SUBTYPE is disabled and defaultImpl is null, should return NullifyingDeserializer",
                NullifyingDeserializer.instance, defaultDeser);
    }

    @Test(timeout = 4000)
    public void testNullDefaultImplWithFailOnInvalidSubtypeEnabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        DefaultDeserializationContext ctxt = createCtxt(mapper, "123");

        JavaType baseType = mapper.constructType(Number.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);

        JsonDeserializer<Object> defaultDeser = deser._findDefaultImplDeserializer(ctxt);
        assertNull("When FAIL_ON_INVALID_SUBTYPE is enabled and defaultImpl is null, should return null", defaultDeser);
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone (databind#2221)
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testUnknownTypeIdHandledAsNullDoesNotReturnNull_Defect2221() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public JavaType handleUnknownTypeId(DeserializationContext ctxt,
                    JavaType baseType, String subTypeId, TypeIdResolver idResolver,
                    String failureMsg) throws IOException {
                return null; // Problem handler deliberately ignores unknown type
            }
        });
        DefaultDeserializationContext ctxt = createCtxt(mapper, "{\"val\": 1}");

        JavaType baseType = mapper.constructType(Object.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());
        ConcreteTypeDeserializer deserBase = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);

        JsonDeserializer<Object> deser = deserBase._findDeserializer(ctxt, "unknown.UnmappedType");

        // Jackson databind #2221: must return NullifyingDeserializer instead of null
        assertNotNull("Deserializer must NOT be null when unknown type id is handled as null by problem handler (databind#2221)", deser);
        assertTrue("Expected NullifyingDeserializer to safely skip token, but got: " + deser.getClass().getName(),
                deser instanceof NullifyingDeserializer);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithNativeTypeIdUnknownTypeIdHandledAsNull_Defect2221() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public JavaType handleUnknownTypeId(DeserializationContext ctxt,
                    JavaType baseType, String subTypeId, TypeIdResolver idResolver,
                    String failureMsg) throws IOException {
                return null;
            }
        });
        JsonParser p = mapper.getFactory().createParser("{\"dummy\": \"test\"}");
        p.nextToken();
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), p, null);

        JavaType baseType = mapper.constructType(Object.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());
        ConcreteTypeDeserializer deserBase = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);

        // In databind #2221, this threw a NullPointerException because _findDeserializer returned null
        Object result = deserBase._deserializeWithNativeTypeId(p, ctxt, "unknown.UnmappedType");
        assertNull("Deserialization should safely return null without throwing NullPointerException", result);
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testUnknownTypeIdWithoutProblemHandlerThrowsJsonMappingException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper, "{}");

        JavaType baseType = mapper.constructType(Number.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());
        idRes.setKnownTypeIdsDesc("int, float");

        BeanProperty.Std prop = new BeanProperty.Std(
                PropertyName.construct("numProp"), baseType, null, null, PropertyMetadata.STD_REQUIRED);

        ConcreteTypeDeserializer deserBase = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);
        ConcreteTypeDeserializer propDeser = (ConcreteTypeDeserializer) deserBase.forProperty(prop);

        try {
            propDeser._findDeserializer(ctxt, "unknownType");
            fail("Expected JsonMappingException for unresolved type id");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("unknownType"));
            assertTrue(msg.contains("known type ids = int, float"));
            assertTrue(msg.contains("for POJO property 'numProp'"));
        }
    }

    @Test(timeout = 4000)
    public void testUnknownTypeIdWithoutKnownIdsDesc() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper, "{}");

        JavaType baseType = mapper.constructType(Number.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());
        idRes.setKnownTypeIdsDesc(null);

        ConcreteTypeDeserializer deserBase = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);

        try {
            deserBase._findDeserializer(ctxt, "missingType");
            fail("Expected JsonMappingException for unresolved type id");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("type ids are not statically known"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleMissingTypeIdThrowsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createCtxt(mapper, "{}");

        JavaType baseType = mapper.constructType(Number.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());

        ConcreteTypeDeserializer deserBase = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);

        try {
            deserBase._handleMissingTypeId(ctxt, "custom missing explanation");
            fail("Expected JsonMappingException from handleMissingTypeId");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("custom missing explanation"));
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithNativeTypeIdNullTypeIdNoDefaultImplThrowsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("{}");
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), p, null);

        JavaType baseType = mapper.constructType(Number.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());

        ConcreteTypeDeserializer deserBase = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);

        try {
            deserBase._deserializeWithNativeTypeId(p, ctxt, null);
            fail("Expected exception when typeId is null and no default implementation exists");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("No (native) type id found"));
        }
    }

    /*
    /**********************************************************
    /* Partition E: Object Lifecycle & Native Deserializer Paths
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testDeserializeWithNativeTypeIdNullTypeIdUsingDefaultImpl() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("42");
        p.nextToken();
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), p, null);

        JavaType baseType = mapper.constructType(Number.class);
        JavaType defaultType = mapper.constructType(Integer.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());

        ConcreteTypeDeserializer deserBase = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, defaultType);

        Object value = deserBase._deserializeWithNativeTypeId(p, ctxt, null);
        assertEquals(42, value);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithNativeTypeIdNonStringId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.getFactory().createParser("999");
        p.nextToken();
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), p, null);

        JavaType baseType = mapper.constructType(Number.class);
        JavaType intType = mapper.constructType(Integer.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());
        idRes.register("100", intType); // String representation of Integer 100

        ConcreteTypeDeserializer deserBase = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);

        Object value = deserBase._deserializeWithNativeTypeId(p, ctxt, Integer.valueOf(100));
        assertEquals(999, value);
    }

    @SuppressWarnings("deprecation")
    @Test(timeout = 4000)
    public void testDeserializeWithNativeTypeIdDeprecatedOverload() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser originalParser = mapper.getFactory().createParser("321");
        originalParser.nextToken();

        // Custom parser delegate with native type id support
        JsonParser nativeParser = new JsonParserDelegate(originalParser) {
            @Override
            public boolean canReadObjectId() { return false; }

            @Override
            public boolean canReadTypeId() { return true; }

            @Override
            public Object getTypeId() {
                return "int";
            }
        };

        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), nativeParser, null);

        JavaType baseType = mapper.constructType(Number.class);
        JavaType intType = mapper.constructType(Integer.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());
        idRes.register("int", intType);

        ConcreteTypeDeserializer deserBase = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);

        Object value = deserBase._deserializeWithNativeTypeId(nativeParser, ctxt);
        assertEquals(321, value);
    }

    @Test(timeout = 4000)
    public void testUnknownTypeIdHandledByProblemHandlerResolvingToValidType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        final JavaType stringType = mapper.constructType(String.class);

        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public JavaType handleUnknownTypeId(DeserializationContext ctxt,
                    JavaType baseType, String subTypeId, TypeIdResolver idResolver,
                    String failureMsg) throws IOException {
                return stringType; // Handled by substituting String type
            }
        });
        DefaultDeserializationContext ctxt = createCtxt(mapper, "\"fallback\"");

        JavaType baseType = mapper.constructType(CharSequence.class);
        StubTypeIdResolver idRes = new StubTypeIdResolver(baseType, mapper.getTypeFactory());

        ConcreteTypeDeserializer deserBase = new ConcreteTypeDeserializer(
                baseType, idRes, "type", false, null);

        JsonDeserializer<Object> deser = deserBase._findDeserializer(ctxt, "nonExistent");
        assertNotNull(deser);
        // Verify caching of recovered deserializer
        JsonDeserializer<Object> cached = deserBase._findDeserializer(ctxt, "nonExistent");
        assertSame(deser, cached);
    }
}