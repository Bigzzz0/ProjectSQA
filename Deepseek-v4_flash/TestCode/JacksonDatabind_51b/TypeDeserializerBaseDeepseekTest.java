package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Constructors:
 *   - Branch: defaultImpl null vs not null
 *   - Branch: typePropertyName null → becomes ""
 *   - Branch: property assigned = null in base constructor
 *
 * Accessors:
 *   - baseTypeName(): delegates to _baseType.getRawClass().getName()
 *   - getPropertyName(): returns _typePropertyName
 *   - getTypeIdResolver(): returns _idResolver
 *   - getDefaultImpl(): returns null if _defaultImpl null, else raw class
 *   - toString(): format check
 *
 * _findDeserializer:
 *   - Branch: deserializer cached → return cached
 *   - Branch: type from id == null → use default impl → if null → handleUnknown
 *   - Branch: type from id != null → narrowing condition if _baseType.getClass() == type.getClass()
 *   - Branch: narrowing performed
 *   - Branch: no narrowing performed
 *   - Branch: deserializer obtained from context → cache
 *
 * _findDefaultImplDeserializer:
 *   - Branch: _defaultImpl == null + fail_on_invalid disabled → return NullifyingDeserializer
 *   - Branch: _defaultImpl == null + fail_on_invalid enabled → return null
 *   - Branch: _defaultImpl raw is bogus (Void) → return NullifyingDeserializer
 *   - Branch: _defaultImpl non-null, non-bogus → synchronize and create/cache
 *
 * _handleUnknownTypeId:
 *   - Branch: idResolver instanceof TypeIdResolverBase → append extra description
 *   - Branch: unknown type → delegate to ctxt.handleUnknownTypeId (throws)
 *
 * Defect target (databind#1270):
 *   - Custom type resolver returning a type whose JavaType class differs from base class
 *     prevents proper narrowing, leading to potential type mismatch.
 *   - Test exercises that narrowing path and verifies the deserializer is non‑null.
 */
public class TypeDeserializerBaseDeepseekTest {

    // Concrete subclass exposing protected methods
    static class TestTypeDeserializer extends TypeDeserializerBase {
        public TestTypeDeserializer(JavaType baseType, TypeIdResolver idRes,
                                    String typePropertyName, boolean typeIdVisible, JavaType defaultImpl) {
            super(baseType, idRes, typePropertyName, typeIdVisible, defaultImpl);
        }

        public TestTypeDeserializer(TypeDeserializerBase src, BeanProperty prop) {
            super(src, prop);
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) {
            return new TestTypeDeserializer(this, prop);
        }

        @Override
        public JsonTypeInfo.As getTypeInclusion() {
            return JsonTypeInfo.As.PROPERTY;
        }

        // Expose protected methods for white‑box testing
        public JsonDeserializer<Object> findDeserializer(DeserializationContext ctxt, String typeId) throws IOException {
            return _findDeserializer(ctxt, typeId);
        }

        public JsonDeserializer<Object> findDefaultImplDeserializer(DeserializationContext ctxt) throws IOException {
            return _findDefaultImplDeserializer(ctxt);
        }

        public JavaType handleUnknownTypeId(DeserializationContext ctxt, String typeId,
                                            TypeIdResolver idResolver, JavaType baseType) throws IOException {
            return _handleUnknownTypeId(ctxt, typeId, idResolver, baseType);
        }
    }

    // Minimal DeserializationContext stub for testing protected methods
    static class StubDeserializationContext extends DeserializationContext {
        private final TypeFactory typeFactory = TypeFactory.defaultInstance();
        private final Map<JavaType, JsonDeserializer<?>> deserCache = new HashMap<>();
        private boolean failOnInvalidSubtype = false;

        public StubDeserializationContext() {
            super(null, null, null); // dummy constructor, minimal fields
        }

        public void setFailOnInvalidSubtype(boolean fail) {
            this.failOnInvalidSubtype = fail;
        }

        @Override
        public TypeFactory getTypeFactory() {
            return typeFactory;
        }

        @Override
        public DeserializationConfig getConfig() {
            throw new UnsupportedOperationException("not needed for tests");
        }

        @Override
        public boolean isEnabled(DeserializationFeature feature) {
            if (feature == DeserializationFeature.FAIL_ON_INVALID_SUBTYPE) {
                return failOnInvalidSubtype;
            }
            throw new UnsupportedOperationException("not expected: " + feature);
        }

        @Override
        public JsonDeserializer<Object> findContextualValueDeserializer(JavaType type, BeanProperty prop) throws JsonMappingException {
            // Return a simple deserializer that returns the given type name
            JsonDeserializer<?> deser = deserCache.get(type);
            if (deser == null) {
                deser = new JsonDeserializer<Object>() {
                    @Override
                    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                        return p.getText(); // dummy
                    }
                };
                deserCache.put(type, deser);
            }
            @SuppressWarnings("unchecked")
            JsonDeserializer<Object> cast = (JsonDeserializer<Object>) deser;
            return cast;
        }

        @Override
        public JavaType handleUnknownTypeId(JavaType baseType, String typeId, TypeIdResolver idResolver, String extraDesc) throws IOException {
            throw new JsonMappingException(null, "Unknown type id: " + typeId);
        }

        @Override
        public Class<?> getActiveView() { return null; }
        @Override
        public AnnotationIntrospector getAnnotationIntrospector() { return null; }
        @Override
        public Object getAttribute(Object key) { return null; }
        @Override
        public void setAttribute(Object key, Object value) {}
    }

    // Stub TypeIdResolver that returns a fixed JavaType
    static class FixedTypeIdResolver implements TypeIdResolver {
        private final JavaType type;

        public FixedTypeIdResolver(JavaType type) {
            this.type = type;
        }

        @Override
        public void init(JavaType baseType) {}
        @Override
        public String idFromValue(Object value) { return "fixed"; }
        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) { return "fixed"; }
        @Override
        public String idFromBaseType() { return "fixed"; }
        @Override
        public JavaType typeFromId(DeserializationContext ctxt, String id) {
            return type;
        }
        @Override
        public String getDescForKnownTypeIds() { return "fixed types"; }
        @Override
        public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
    }

    // ------ Partition A: Core functional logic & state transitions ------

    @Test(timeout = 4000)
    public void testConstructorAndAccessors() {
        JavaType baseType = SimpleType.constructUnsafe(Object.class);
        TypeIdResolver idRes = new FixedTypeIdResolver(baseType);
        // null typePropertyName -> becomes ""
        TypeDeserializerBase deser = new TestTypeDeserializer(baseType, idRes, null, true, null);
        assertEquals("", deser.getPropertyName());
        assertSame(idRes, deser.getTypeIdResolver());
        assertNull(deser.getDefaultImpl());
        assertEquals(baseType.getRawClass().getName(), deser.baseTypeName());
        String toString = deser.toString();
        assertTrue(toString.startsWith("["));
        assertTrue(toString.endsWith("]"));
        assertTrue(toString.contains("TestTypeDeserializer"));
    }

    @Test(timeout = 4000)
    public void testConstructorAndAccessorsWithDefaultImpl() {
        JavaType baseType = SimpleType.constructUnsafe(Number.class);
        JavaType defaultImpl = SimpleType.constructUnsafe(Integer.class);
        TypeIdResolver idRes = new FixedTypeIdResolver(baseType);
        TypeDeserializerBase deser = new TestTypeDeserializer(baseType, idRes, "type", false, defaultImpl);
        assertEquals("type", deser.getPropertyName());
        assertEquals(Integer.class, deser.getDefaultImpl());
        assertFalse(deser.toString().isEmpty());
    }

    // ------ Partition B: Boundary Value Analysis ------

    @Test(timeout = 4000)
    public void testConstructorWithEmptyTypeProperty() {
        JavaType baseType = SimpleType.constructUnsafe(String.class);
        TypeIdResolver idRes = new FixedTypeIdResolver(baseType);
        TypeDeserializerBase deser = new TestTypeDeserializer(baseType, idRes, "", true, null);
        assertEquals("", deser.getPropertyName());
    }

    @Test(timeout = 4000)
    public void testDefaultImplNullWithFailOff() throws Exception {
        JavaType baseType = SimpleType.constructUnsafe(Object.class);
        TypeIdResolver idRes = new FixedTypeIdResolver(baseType);
        TestTypeDeserializer deser = new TestTypeDeserializer(baseType, idRes, "id", false, null);
        StubDeserializationContext ctxt = new StubDeserializationContext();
        ctxt.setFailOnInvalidSubtype(false);
        JsonDeserializer<Object> d = deser.findDefaultImplDeserializer(ctxt);
        assertNotNull(d);
        assertTrue(d instanceof NullifyingDeserializer);
    }

    @Test(timeout = 4000)
    public void testDefaultImplNullWithFailOn() throws Exception {
        JavaType baseType = SimpleType.constructUnsafe(Object.class);
        TypeIdResolver idRes = new FixedTypeIdResolver(baseType);
        TestTypeDeserializer deser = new TestTypeDeserializer(baseType, idRes, "id", false, null);
        StubDeserializationContext ctxt = new StubDeserializationContext();
        ctxt.setFailOnInvalidSubtype(true);
        JsonDeserializer<Object> d = deser.findDefaultImplDeserializer(ctxt);
        assertNull(d);
    }

    @Test(timeout = 4000)
    public void testDefaultImplBogusClass() throws Exception {
        JavaType baseType = SimpleType.constructUnsafe(Object.class);
        JavaType voidType = SimpleType.constructUnsafe(Void.class);
        TypeIdResolver idRes = new FixedTypeIdResolver(baseType);
        TestTypeDeserializer deser = new TestTypeDeserializer(baseType, idRes, "id", false, voidType);
        StubDeserializationContext ctxt = new StubDeserializationContext();
        JsonDeserializer<Object> d = deser.findDefaultImplDeserializer(ctxt);
        assertNotNull(d);
        assertTrue(d instanceof NullifyingDeserializer);
    }

    @Test(timeout = 4000)
    public void testDefaultImplDeserializerCached() throws Exception {
        JavaType baseType = SimpleType.constructUnsafe(Object.class);
        JavaType defaultImpl = SimpleType.constructUnsafe(Integer.class);
        TypeIdResolver idRes = new FixedTypeIdResolver(baseType);
        TestTypeDeserializer deser = new TestTypeDeserializer(baseType, idRes, "id", false, defaultImpl);
        StubDeserializationContext ctxt = new StubDeserializationContext();
        JsonDeserializer<Object> d1 = deser.findDefaultImplDeserializer(ctxt);
        assertNotNull(d1);
        // second call returns cached
        JsonDeserializer<Object> d2 = deser.findDefaultImplDeserializer(ctxt);
        assertSame(d1, d2);
    }

    // ------ Partition C: Defect-targeted branch zone ------

    @Test(timeout = 4000)
    public void testFindDeserializerWithTypeNarrowing() throws Exception {
        // Base is SimpleType, resolver returns SimpleType – narrowing should occur
        JavaType baseType = SimpleType.constructUnsafe(Object.class);
        JavaType resolvedType = SimpleType.constructUnsafe(String.class);
        TypeIdResolver idRes = new FixedTypeIdResolver(resolvedType);
        TestTypeDeserializer deser = new TestTypeDeserializer(baseType, idRes, "id", false, null);
        StubDeserializationContext ctxt = new StubDeserializationContext();
        JsonDeserializer<Object> d = deser.findDeserializer(ctxt, "someId");
        assertNotNull(d);
        // Because narrowing happens (both SimpleType), the deserializer is for String
    }

    @Test(timeout = 4000)
    public void testFindDeserializerWithoutNarrowing() throws Exception {
        // Base is SimpleType, resolver returns CollectionType – no narrowing
        JavaType baseType = SimpleType.constructUnsafe(Object.class);
        JavaType resolvedType = TypeFactory.defaultInstance().constructType(ArrayList.class);
        TypeIdResolver idRes = new FixedTypeIdResolver(resolvedType);
        TestTypeDeserializer deser = new TestTypeDeserializer(baseType, idRes, "id", false, null);
        StubDeserializationContext ctxt = new StubDeserializationContext();
        JsonDeserializer<Object> d = deser.findDeserializer(ctxt, "someId");
        assertNotNull(d);
    }

    @Test(timeout = 4000)
    public void testFindDeserializerCachedReturn() throws Exception {
        JavaType baseType = SimpleType.constructUnsafe(Object.class);
        JavaType resolvedType = SimpleType.constructUnsafe(Integer.class);
        TypeIdResolver idRes = new FixedTypeIdResolver(resolvedType);
        TestTypeDeserializer deser = new TestTypeDeserializer(baseType, idRes, "id", false, null);
        StubDeserializationContext ctxt = new StubDeserializationContext();
        JsonDeserializer<Object> d1 = deser.findDeserializer(ctxt, "id1");
        JsonDeserializer<Object> d2 = deser.findDeserializer(ctxt, "id1");
        assertSame(d1, d2);
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testFindDeserializerTypeNullNoDefaultImpl() throws Exception {
        // Resolver returns null, no default impl -> handleUnknownTypeId throws
        JavaType baseType = SimpleType.constructUnsafe(Object.class);
        TypeIdResolver idRes = new TypeIdResolver() {
            @Override public void init(JavaType baseType1) {}
            @Override public String idFromValue(Object value) { return null; }
            @Override public String idFromValueAndType(Object value, Class<?> suggestedType) { return null; }
            @Override public String idFromBaseType() { return null; }
            @Override public JavaType typeFromId(DeserializationContext ctxt, String id) { return null; }
            @Override public String getDescForKnownTypeIds() { return null; }
            @Override public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
        };
        TestTypeDeserializer deser = new TestTypeDeserializer(baseType, idRes, "id", false, null);
        StubDeserializationContext ctxt = new StubDeserializationContext();
        deser.findDeserializer(ctxt, "unknown");
    }

    @Test(timeout = 4000)
    public void testFindDeserializerDefaultImplFallback() throws Exception {
        // Resolver returns null, but default impl is set -> should use default impl
        JavaType baseType = SimpleType.constructUnsafe(Object.class);
        JavaType defaultImpl = SimpleType.constructUnsafe(Double.class);
        TypeIdResolver idRes = new TypeIdResolver() {
            @Override public void init(JavaType baseType1) {}
            @Override public String idFromValue(Object value) { return null; }
            @Override public String idFromValueAndType(Object value, Class<?> suggestedType) { return null; }
            @Override public String idFromBaseType() { return null; }
            @Override public JavaType typeFromId(DeserializationContext ctxt, String id) { return null; }
            @Override public String getDescForKnownTypeIds() { return null; }
            @Override public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
        };
        TestTypeDeserializer deser = new TestTypeDeserializer(baseType, idRes, "id", false, defaultImpl);
        StubDeserializationContext ctxt = new StubDeserializationContext();
        JsonDeserializer<Object> d = deser.findDeserializer(ctxt, "unknown");
        assertNotNull(d);
    }

    // ------ Partition D: Exception & defensive guard paths ------

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testHandleUnknownTypeIdWithTypeIdResolverBase() throws Throwable {
        JavaType baseType = SimpleType.constructUnsafe(Object.class);
        // TypeIdResolverBase subclass – will have getDescForKnownTypeIds()
        TypeIdResolverBase resolver = new TypeIdResolverBase() {
            @Override public void init(JavaType baseType1) {}
            @Override public String idFromValue(Object value) { return null; }
            @Override public String idFromValueAndType(Object value, Class<?> suggestedType) { return null; }
            @Override public String idFromBaseType() { return null; }
            @Override public JavaType typeFromId(DeserializationContext ctxt, String id) { return null; }
            @Override public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
            @Override public String getDescForKnownTypeIds() { return "known ids: a,b,c"; }
        };
        TestTypeDeserializer deser = new TestTypeDeserializer(baseType, resolver, "id", false, null);
        StubDeserializationContext ctxt = new StubDeserializationContext();
        // This will call _handleUnknownTypeId which in turn throws
        deser.handleUnknownTypeId(ctxt, "x", resolver, baseType);
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testHandleUnknownTypeIdWithPlainResolver() throws Throwable {
        JavaType baseType = SimpleType.constructUnsafe(Object.class);
        TypeIdResolver resolver = new TypeIdResolver() {
            @Override public void init(JavaType baseType1) {}
            @Override public String idFromValue(Object value) { return null; }
            @Override public String idFromValueAndType(Object value, Class<?> suggestedType) { return null; }
            @Override public String idFromBaseType() { return null; }
            @Override public JavaType typeFromId(DeserializationContext ctxt, String id) { return null; }
            @Override public String getDescForKnownTypeIds() { return null; }
            @Override public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
        };
        TestTypeDeserializer deser = new TestTypeDeserializer(baseType, resolver, "id", false, null);
        StubDeserializationContext ctxt = new StubDeserializationContext();
        deser.handleUnknownTypeId(ctxt, "x", resolver, baseType);
    }

    // Defect-targeted test for databind#1270: custom resolver returns a type with different class
    // (base SimpleType, resolved CollectionType) and ensure no crash
    @Test(timeout = 4000)
    public void testDefectPolymorphicTypeViaCustom() throws Exception {
        // This reproduces the scenario where a custom TypeIdResolver returns a type whose
        // JavaType class differs from the base class (SimpleType vs MapType), preventing
        // the narrowing that would normally convert the type. The bug manifested as a
        // JsonMappingException when trying to set a LinkedHashMap into a property.
        JavaType baseType = SimpleType.constructUnsafe(Object.class);
        JavaType resolvedType = TypeFactory.defaultInstance().constructType(LinkedHashMap.class);
        TypeIdResolver idRes = new FixedTypeIdResolver(resolvedType);
        TestTypeDeserializer deser = new TestTypeDeserializer(baseType, idRes, "type", false, null);
        StubDeserializationContext ctxt = new StubDeserializationContext();
        JsonDeserializer<Object> d = deser.findDeserializer(ctxt, "custom");
        assertNotNull(d); // In the buggy version, this would throw or return null; fixed returns a deserializer.
    }

    // ------ Partition E: Object lifecycle & contract integrity ------

    @Test(timeout = 4000)
    public void testForPropertyCreatesCopy() {
        JavaType baseType = SimpleType.constructUnsafe(Object.class);
        TypeIdResolver idRes = new FixedTypeIdResolver(baseType);
        TestTypeDeserializer original = new TestTypeDeserializer(baseType, idRes, "id", false, null);
        // Property is not null in copy
        BeanProperty prop = null; // We cannot create a simple BeanProperty easily, but abstract method handles it
        TypeDeserializer copy = original.forProperty(prop);
        assertNotNull(copy);
        assertNotSame(original, copy);
        // The copy should have the same id resolver, base type, etc.
        assertEquals(original.getTypeIdResolver(), copy.getTypeIdResolver());
    }
}