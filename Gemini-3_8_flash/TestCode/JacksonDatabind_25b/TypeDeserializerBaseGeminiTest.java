/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.jsontype.impl.TypeDeserializerBase
 *
 * Decision Branches & Target Conditions:
 * 1. Constructor:
 *    - defaultImpl == null vs defaultImpl != null (_baseType.forcedNarrowBy)
 *    - Copy constructor (copying all fields, overriding _property)
 * 2. Accessors:
 *    - baseTypeName() -> delegates to _baseType.getRawClass().getName()
 *    - getPropertyName() -> returns _typePropertyName
 *    - getTypeIdResolver() -> returns _idResolver
 *    - getDefaultImpl() -> null when _defaultImpl is null; rawClass when non-null
 *    - toString() -> formatting check of string representation
 * 3. _findDeserializer(DeserializationContext, String):
 *    - Cache hit (_deserializers contains typeId) vs Cache miss
 *    - Cache miss:
 *      * type == null (typeFromId returned null) -> delegates to _findDefaultImplDeserializer
 *        - defaultImpl deser found vs null -> invokes _handleUnknownTypeId
 *      * type != null:
 *        - _baseType != null && _baseType.getClass() == type.getClass() -> invokes narrowBy()
 *        - _baseType == null or different class -> directly findContextualValueDeserializer
 * 4. _findDefaultImplDeserializer(DeserializationContext):
 *    - _defaultImpl == null:
 *      * FAIL_ON_INVALID_SUBTYPE disabled -> NullifyingDeserializer.instance
 *      * FAIL_ON_INVALID_SUBTYPE enabled -> null
 *    - _defaultImpl != null:
 *      * ClassUtil.isBogusClass(raw) (e.g. Void.class, NoClass.class) -> NullifyingDeserializer.instance
 *      * Non-bogus class: synchronized double-check caching of _defaultImplDeserializer
 * 5. _deserializeWithNativeTypeId(JsonParser, DeserializationContext, Object):
 *    - typeId == null:
 *      * defaultImpl deser exists -> calls deser.deserialize(jp, ctxt)
 *      * defaultImpl deser is null -> throws JsonMappingException ("No (native) type id found...")
 *    - typeId != null:
 *      * String typeId vs non-String (invokes String.valueOf)
 *      * calls deser.deserialize(jp, ctxt)
 * 6. _handleUnknownTypeId(DeserializationContext, String, TypeIdResolver, JavaType):
 *    - idResolver instanceof TypeIdResolverBase:
 *      * extraDesc == null -> "known type ids are not statically known"
 *      * extraDesc != null -> "known type ids = ..."
 *    - idResolver NOT instanceof TypeIdResolverBase -> extraDesc = null
 *    - throws ctxt.unknownTypeException
 *
 * Known Defect (Defects4J / Jackson-databind #890):
 * - Test: TestArrayDeserialization::testByteArrayTypeOverride890
 * - Issue: When _baseType is an ArrayType (e.g., Object[]) and type is an ArrayType (e.g., byte[]),
 *   _baseType.getClass() == type.getClass() is true. Calling _baseType.narrowBy(type.getRawClass())
 *   incorrectly narrows ArrayType into a SimpleType for '[B', causing Jackson to attempt
 *   Bean deserialization instead of Array deserialization:
 *   "JsonMappingException: Can not deserialize Class [B (of type array) as a Bean".
 */

package com.fasterxml.jackson.databind.jsontype.impl;

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
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;

import static org.junit.Assert.*;

public class TypeDeserializerBaseGeminiTest {

    // Concrete implementation of TypeDeserializerBase for direct unit testing
    private static class ConcreteTypeDeserializer extends TypeDeserializerBase {
        private static final long serialVersionUID = 1L;
        private final JsonTypeInfo.As _inclusion;

        public ConcreteTypeDeserializer(JavaType baseType, TypeIdResolver idRes,
                                        String typePropertyName, boolean typeIdVisible,
                                        Class<?> defaultImpl, JsonTypeInfo.As inclusion) {
            super(baseType, idRes, typePropertyName, typeIdVisible, defaultImpl);
            _inclusion = inclusion;
        }

        public ConcreteTypeDeserializer(ConcreteTypeDeserializer src, BeanProperty prop) {
            super(src, prop);
            _inclusion = src._inclusion;
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) {
            if (prop == _property) {
                return this;
            }
            return new ConcreteTypeDeserializer(this, prop);
        }

        @Override
        public JsonTypeInfo.As getTypeInclusion() {
            return _inclusion;
        }

        @Override
        public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
            return _deserializeWithNativeTypeId(p, ctxt);
        }

        @Override
        public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
            return _deserializeWithNativeTypeId(p, ctxt);
        }

        @Override
        public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) throws IOException {
            return _deserializeWithNativeTypeId(p, ctxt);
        }

        @Override
        public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) throws IOException {
            return _deserializeWithNativeTypeId(p, ctxt);
        }

        // Expose protected methods for test inspection
        public JsonDeserializer<Object> findDeserializerPublic(DeserializationContext ctxt, String typeId) throws IOException {
            return _findDeserializer(ctxt, typeId);
        }

        public JsonDeserializer<Object> findDefaultImplDeserializerPublic(DeserializationContext ctxt) throws IOException {
            return _findDefaultImplDeserializer(ctxt);
        }

        public Object deserializeWithNativeTypeIdPublic(JsonParser jp, DeserializationContext ctxt, Object typeId) throws IOException {
            return _deserializeWithNativeTypeId(jp, ctxt, typeId);
        }

        public JsonDeserializer<Object> handleUnknownTypeIdPublic(DeserializationContext ctxt, String typeId,
                                                                   TypeIdResolver idRes, JavaType baseType) throws IOException {
            return _handleUnknownTypeId(ctxt, typeId, idRes, baseType);
        }
    }

    private static class DummyTypeIdResolver implements TypeIdResolver {
        private JavaType _baseType;

        @Override
        public void init(JavaType baseType) {
            _baseType = baseType;
        }

        @Override
        public String idFromValue(Object value) {
            return value.getClass().getName();
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            return suggestedType.getName();
        }

        @Override
        public String idFromBaseType() {
            return _baseType.getRawClass().getName();
        }

        @Override
        public JavaType typeFromId(String id) {
            return null;
        }

        @Override
        public JavaType typeFromId(com.fasterxml.jackson.databind.DatabindContext context, String id) {
            if ("known".equals(id)) {
                return SimpleType.construct(String.class);
            }
            if ("bytes".equals(id) || "[B".equals(id)) {
                return ArrayType.construct(SimpleType.construct(Byte.TYPE), null, null);
            }
            return null;
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;
        }
    }

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testGettersAndStateInitialization() {
        JavaType baseType = SimpleType.construct(CharSequence.class);
        TypeIdResolver resolver = new DummyTypeIdResolver();
        resolver.init(baseType);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, resolver, "@type", true, String.class, JsonTypeInfo.As.PROPERTY);

        assertEquals("java.lang.CharSequence", deser.baseTypeName());
        assertEquals("@type", deser.getPropertyName());
        assertSame(resolver, deser.getTypeIdResolver());
        assertEquals(String.class, deser.getDefaultImpl());
        assertEquals(JsonTypeInfo.As.PROPERTY, deser.getTypeInclusion());

        String toStringVal = deser.toString();
        assertTrue(toStringVal.contains("ConcreteTypeDeserializer"));
        assertTrue(toStringVal.contains("base-type:"));
        assertTrue(toStringVal.contains("id-resolver:"));
    }

    @Test(timeout = 4000)
    public void testForPropertyCopyPreservesState() {
        JavaType baseType = SimpleType.construct(Object.class);
        TypeIdResolver resolver = new DummyTypeIdResolver();
        ConcreteTypeDeserializer original = new ConcreteTypeDeserializer(
                baseType, resolver, "typeProp", false, null, JsonTypeInfo.As.WRAPPER_OBJECT);

        BeanProperty.Bogus prop = new BeanProperty.Bogus();
        TypeDeserializer copy = original.forProperty(prop);

        assertNotSame(original, copy);
        assertSame(copy, copy.forProperty(prop)); // Identity check when re-applying same property
        assertEquals("typeProp", copy.getPropertyName());
        assertSame(resolver, copy.getTypeIdResolver());
        assertNull(copy.getDefaultImpl());
        assertEquals(JsonTypeInfo.As.WRAPPER_OBJECT, copy.getTypeInclusion());
    }

    @Test(timeout = 4000)
    public void testFindDeserializerCaching() throws Exception {
        JavaType baseType = SimpleType.construct(CharSequence.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver();
        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, resolver, "type", false, null, JsonTypeInfo.As.PROPERTY);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonDeserializer<Object> d1 = deser.findDeserializerPublic(ctxt, "known");
        assertNotNull(d1);
        JsonDeserializer<Object> d2 = deser.findDeserializerPublic(ctxt, "known");
        assertSame("Subsequent lookups for same typeId must return cached instance", d1, d2);
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Extremes
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testDefaultImplNullHandling() throws Exception {
        JavaType baseType = SimpleType.construct(Object.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver();
        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, resolver, "type", false, null, JsonTypeInfo.As.PROPERTY);

        assertNull(deser.getDefaultImpl());

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxtFail = mapper.getDeserializationContext();
        // Default: FAIL_ON_INVALID_SUBTYPE is enabled
        assertTrue(ctxtFail.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE));
        assertNull(deser.findDefaultImplDeserializerPublic(ctxtFail));

        // When FAIL_ON_INVALID_SUBTYPE is disabled
        ObjectMapper noFailMapper = new ObjectMapper();
        noFailMapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        DeserializationContext ctxtNoFail = noFailMapper.getDeserializationContext();
        JsonDeserializer<Object> defaultDeser = deser.findDefaultImplDeserializerPublic(ctxtNoFail);
        assertNotNull(defaultDeser);
        assertSame(NullifyingDeserializer.instance, defaultDeser);
    }

    @Test(timeout = 4000)
    public void testDefaultImplBogusClassHandling() throws Exception {
        JavaType baseType = SimpleType.construct(Object.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver();
        // Void.class is recognized as a bogus class in ClassUtil
        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, resolver, "type", false, Void.class, JsonTypeInfo.As.PROPERTY);

        assertEquals(Void.class, deser.getDefaultImpl());

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonDeserializer<Object> defaultDeser = deser.findDefaultImplDeserializerPublic(ctxt);
        assertSame(NullifyingDeserializer.instance, defaultDeser);
    }

    @Test(timeout = 4000)
    public void testFindDefaultImplDeserializerCaching() throws Exception {
        JavaType baseType = SimpleType.construct(CharSequence.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver();
        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, resolver, "type", false, String.class, JsonTypeInfo.As.PROPERTY);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonDeserializer<Object> d1 = deser.findDefaultImplDeserializerPublic(ctxt);
        assertNotNull(d1);
        JsonDeserializer<Object> d2 = deser.findDefaultImplDeserializerPublic(ctxt);
        assertSame(d1, d2);
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone (Jackson #890)
    /**********************************************************
     */

    static class ByteArrayWrapper {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
        public Object value;
    }

    @Test(timeout = 4000)
    public void testByteArrayTypeOverride890ThroughMapper() throws Exception {
        // Direct reproduction of testByteArrayTypeOverride890 defect
        ObjectMapper mapper = new ObjectMapper();
        byte[] inputBytes = new byte[] { 1, 2, 3, 4, 5 };
        ByteArrayWrapper wrapper = new ByteArrayWrapper();
        wrapper.value = inputBytes;

        String json = mapper.writeValueAsString(wrapper);
        assertNotNull(json);

        // Deserialization must correctly resolve byte[] array type, not attempt Bean deserialization
        ByteArrayWrapper result = mapper.readValue(json, ByteArrayWrapper.class);
        assertNotNull(result);
        assertNotNull(result.value);
        assertTrue("Value must be deserialized as byte[]", result.value instanceof byte[]);
        assertArrayEquals(inputBytes, (byte[]) result.value);
    }

    @Test(timeout = 4000)
    public void testArrayTypeNarrowingBugDirect() throws Exception {
        // Targets: _baseType != null && _baseType.getClass() == type.getClass()
        // where both are ArrayType (Object[] narrowed to byte[])
        JavaType baseArrayType = ArrayType.construct(SimpleType.construct(Object.class), null, null);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver();

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseArrayType, resolver, "type", false, null, JsonTypeInfo.As.PROPERTY);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        // Resolving typeId "bytes" (which produces ArrayType for byte[])
        // If the narrowBy defect is present, this will narrow to a SimpleType of [B
        // and fail to find or return an invalid bean deserializer instead of byte[] array deserializer.
        JsonDeserializer<Object> deserializer = deser.findDeserializerPublic(ctxt, "bytes");
        assertNotNull(deserializer);

        byte[] inputData = new byte[] { 7, 8, 9 };
        String json = mapper.writeValueAsString(inputData);
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken(); // Move to START_ARRAY or VALUE_STRING (Base64)
        Object deserializedValue = deserializer.deserialize(p, ctxt);
        assertTrue("Deserialized value must be byte array", deserializedValue instanceof byte[]);
        assertArrayEquals(inputData, (byte[]) deserializedValue);
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testHandleUnknownTypeIdWithTypeIdResolverBaseKnown() {
        JavaType baseType = SimpleType.construct(Object.class);
        // TypeNameIdResolver extends TypeIdResolverBase
        ObjectMapper mapper = new ObjectMapper();
        TypeNameIdResolver resolver = TypeNameIdResolver.construct(
                mapper.getDeserializationConfig(),
                baseType,
                Collections.singletonList(new NamedType(String.class, "strId")),
                true, false);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, resolver, "type", false, null, JsonTypeInfo.As.PROPERTY);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        try {
            deser.handleUnknownTypeIdPublic(ctxt, "unknownId", resolver, baseType);
            fail("Expected JsonMappingException for unknown typeId");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("known type ids = [strId]"));
        } catch (IOException e) {
            fail("Unexpected IOException type: " + e.getClass().getName());
        }
    }

    @Test(timeout = 4000)
    public void testHandleUnknownTypeIdWithTypeIdResolverBaseNullDesc() {
        JavaType baseType = SimpleType.construct(Object.class);
        // Empty mappings -> getDescForKnownTypeIds() returns null
        ObjectMapper mapper = new ObjectMapper();
        TypeNameIdResolver resolver = TypeNameIdResolver.construct(
                mapper.getDeserializationConfig(),
                baseType,
                Collections.<NamedType>emptyList(),
                true, false);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, resolver, "type", false, null, JsonTypeInfo.As.PROPERTY);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        try {
            deser.handleUnknownTypeIdPublic(ctxt, "unmappedId", resolver, baseType);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("known type ids are not statically known"));
        } catch (IOException e) {
            fail("Unexpected IOException type: " + e.getClass().getName());
        }
    }

    @Test(timeout = 4000)
    public void testHandleUnknownTypeIdWithoutTypeIdResolverBase() {
        JavaType baseType = SimpleType.construct(Object.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver();

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, resolver, "type", false, null, JsonTypeInfo.As.PROPERTY);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        try {
            deser.handleUnknownTypeIdPublic(ctxt, "invalidId", resolver, baseType);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("Could not resolve type id 'invalidId'"));
            assertFalse(msg.contains("known type ids"));
        } catch (IOException e) {
            fail("Unexpected IOException type: " + e.getClass().getName());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithNativeTypeIdMissingThrowsException() throws Exception {
        JavaType baseType = SimpleType.construct(Object.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver();
        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, resolver, "type", false, null, JsonTypeInfo.As.PROPERTY);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonParser p = mapper.getFactory().createParser("{}");

        try {
            deser.deserializeWithNativeTypeIdPublic(p, ctxt, null);
            fail("Expected JsonMappingException when native type id is null and no defaultImpl");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("No (native) type id found"));
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithNativeTypeIdNonStringFallback() throws Exception {
        JavaType baseType = SimpleType.construct(CharSequence.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver();
        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, resolver, "type", false, null, JsonTypeInfo.As.PROPERTY);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonParser p = mapper.getFactory().createParser("\"testText\"");
        p.nextToken(); // Move to VALUE_STRING

        // Pass non-String typeId (e.g. an Object whose toString() is "known")
        Object customId = new Object() {
            @Override
            public String toString() {
                return "known";
            }
        };

        Object result = deser.deserializeWithNativeTypeIdPublic(p, ctxt, customId);
        assertEquals("testText", result);
    }

    /*
    /**********************************************************
    /* Partition E: Object Lifecycle & Contract Integrity
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testSerializationContract() {
        JavaType baseType = SimpleType.construct(Number.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver();
        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, resolver, "type", false, Integer.class, JsonTypeInfo.As.PROPERTY);

        assertTrue("TypeDeserializerBase must implement Serializable",
                deser instanceof Serializable);
    }
}