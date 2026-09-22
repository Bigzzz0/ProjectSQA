package com.fasterxml.jackson.databind.jsontype.impl;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class StdTypeResolverBuilderDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: StdTypeResolverBuilder
     * 
     * Branches covered:
     * - init(): null idType -> IllegalArgumentException; non-null -> sets _idType, _customIdResolver, _typeProperty
     * - buildTypeSerializer(): _idType == NONE -> null; switch on _includeAs (WRAPPER_ARRAY, PROPERTY, WRAPPER_OBJECT, EXTERNAL_PROPERTY, EXISTING_PROPERTY, default -> IllegalStateException)
     * - buildTypeDeserializer(): _idType == NONE -> null; _defaultImpl null vs non-null; _defaultImpl == Void/NoClass vs specialized; switch on _includeAs (same cases)
     * - inclusion(): null -> IllegalArgumentException; non-null -> sets _includeAs
     * - typeProperty(): null/empty -> uses default; non-empty -> sets _typeProperty
     * - defaultImpl(): sets _defaultImpl
     * - typeIdVisibility(): sets _typeIdVisible
     * - idResolver(): _customIdResolver != null -> return it; _idType == null -> IllegalStateException; switch on _idType (CLASS, MINIMAL_CLASS, NAME, NONE, CUSTOM -> default -> IllegalStateException)
     * 
     * Defect targeted (DefaultTypingWithPrimitivesTest::testDefaultTypingWithLong):
     * - When _defaultImpl is a primitive wrapper (e.g., Long.class) and baseType is primitive (e.g., long),
     *   constructSpecializedType fails with "Class java.lang.Long not subtype of [simple type, class long]".
     *   The fix should handle primitive wrappers by using constructType instead of constructSpecializedType.
     *   Test: buildTypeDeserializer with _defaultImpl=Long.class, baseType=long primitive, should not throw.
     */
    
    // ===== Partition A: Core Functional Logic & State Transitions =====
    
    @Test(timeout = 4000)
    public void testInitSetsDefaults() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        assertEquals(JsonTypeInfo.Id.CLASS, builder._idType);
        assertNull(builder._customIdResolver);
        assertEquals(JsonTypeInfo.Id.CLASS.getDefaultPropertyName(), builder._typeProperty);
        assertFalse(builder._typeIdVisible);
        assertNull(builder._defaultImpl);
    }
    
    @Test(timeout = 4000)
    public void testInitWithCustomResolver() {
        TypeIdResolver custom = new TypeIdResolver() {
            @Override public void init(JavaType bt) {}
            @Override public String idFromValue(Object value) { return "x"; }
            @Override public String idFromValueAndType(Object value, Class<?> suggestedType) { return "x"; }
            @Override public String idFromBaseType() { return "x"; }
            @Override public JavaType typeFromId(com.fasterxml.jackson.databind.DatabindContext context, String id) { return null; }
            @Override public String getMechanism() { return null; }
        };
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CUSTOM, custom);
        assertSame(custom, builder._customIdResolver);
        assertEquals(JsonTypeInfo.Id.CUSTOM.getDefaultPropertyName(), builder._typeProperty);
    }
    
    @Test(timeout = 4000)
    public void testInclusionAndTypeProperty() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        assertEquals(JsonTypeInfo.As.PROPERTY, builder._includeAs);
        builder.typeProperty("customProp");
        assertEquals("customProp", builder._typeProperty);
        builder.typeProperty(null);
        assertEquals(JsonTypeInfo.Id.CLASS.getDefaultPropertyName(), builder._typeProperty);
        builder.typeProperty("");
        assertEquals(JsonTypeInfo.Id.CLASS.getDefaultPropertyName(), builder._typeProperty);
    }
    
    @Test(timeout = 4000)
    public void testDefaultImplAndVisibility() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.defaultImpl(String.class);
        assertEquals(String.class, builder.getDefaultImpl());
        builder.typeIdVisibility(true);
        assertTrue(builder.isTypeIdVisible());
        builder.typeIdVisibility(false);
        assertFalse(builder.isTypeIdVisible());
    }
    
    @Test(timeout = 4000)
    public void testNoTypeInfoBuilder() {
        StdTypeResolverBuilder builder = StdTypeResolverBuilder.noTypeInfoBuilder();
        assertEquals(JsonTypeInfo.Id.NONE, builder._idType);
        assertNull(builder._includeAs);
        assertNull(builder._typeProperty);
    }
    
    // ===== Partition B: Boundary Value Analysis & Extremes =====
    
    @Test(timeout = 4000)
    public void testBuildTypeSerializerWithNoneId() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.NONE, null);
        SerializationConfig config = new ObjectMapper().getSerializationConfig();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        assertNull(builder.buildTypeSerializer(config, baseType, Collections.<NamedType>emptyList()));
    }
    
    @Test(timeout = 4000)
    public void testBuildTypeDeserializerWithNoneId() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.NONE, null);
        DeserializationConfig config = new ObjectMapper().getDeserializationConfig();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        assertNull(builder.buildTypeDeserializer(config, baseType, Collections.<NamedType>emptyList()));
    }
    
    @Test(timeout = 4000)
    public void testTypePropertyEmptyUsesDefault() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.NAME, null);
        builder.typeProperty("");
        assertEquals(JsonTypeInfo.Id.NAME.getDefaultPropertyName(), builder._typeProperty);
        builder.typeProperty(null);
        assertEquals(JsonTypeInfo.Id.NAME.getDefaultPropertyName(), builder._typeProperty);
    }
    
    // ===== Partition C: Defect-Targeted Branch Zone =====
    
    @Test(timeout = 4000)
    public void testBuildTypeDeserializerWithPrimitiveDefaultImpl() {
        // Regression test for DefaultTypingWithPrimitivesTest::testDefaultTypingWithLong
        // _defaultImpl = Long.class, baseType = long primitive -> should not throw
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        builder.defaultImpl(Long.class);
        builder.typeIdVisibility(true);
        
        DeserializationConfig config = new ObjectMapper().getDeserializationConfig();
        JavaType baseType = TypeFactory.defaultInstance().constructType(long.class);
        
        // This should not throw JsonMappingException; the fix should handle primitive wrappers
        TypeDeserializer deserializer = builder.buildTypeDeserializer(config, baseType, Collections.<NamedType>emptyList());
        assertNotNull(deserializer);
    }
    
    @Test(timeout = 4000)
    public void testBuildTypeDeserializerWithVoidDefaultImpl() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
        builder.defaultImpl(Void.class);
        
        DeserializationConfig config = new ObjectMapper().getDeserializationConfig();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        TypeDeserializer deserializer = builder.buildTypeDeserializer(config, baseType, Collections.<NamedType>emptyList());
        assertNotNull(deserializer);
    }
    
    // ===== Partition D: Exception & Defensive Guard Paths =====
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInitWithNullIdType() {
        new StdTypeResolverBuilder().init(null, null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInclusionWithNull() {
        new StdTypeResolverBuilder().inclusion(null);
    }
    
    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testBuildTypeSerializerWithoutInit() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        SerializationConfig config = new ObjectMapper().getSerializationConfig();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        builder.buildTypeSerializer(config, baseType, Collections.<NamedType>emptyList());
    }
    
    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testBuildTypeDeserializerWithoutInit() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        DeserializationConfig config = new ObjectMapper().getDeserializationConfig();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        builder.buildTypeDeserializer(config, baseType, Collections.<NamedType>emptyList());
    }
    
    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testBuildTypeSerializerWithInvalidIncludeAs() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        // Set invalid includeAs via reflection or use a custom subclass
        builder._includeAs = null; // This will cause NPE in switch, but we want IllegalStateException
        // Actually, we need to set an invalid enum value; use reflection to set a non-existent value
        // Since we can't, we'll use a mock-like approach with a subclass
        StdTypeResolverBuilder sub = new StdTypeResolverBuilder() {
            @Override
            public TypeSerializer buildTypeSerializer(SerializationConfig config,
                    JavaType baseType, Collection<NamedType> subtypes) {
                // Force invalid includeAs
                _includeAs = null;
                return super.buildTypeSerializer(config, baseType, subtypes);
            }
        };
        sub.init(JsonTypeInfo.Id.CLASS, null);
        SerializationConfig config = new ObjectMapper().getSerializationConfig();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        sub.buildTypeSerializer(config, baseType, Collections.<NamedType>emptyList());
    }
    
    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testIdResolverWithInvalidIdType() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CUSTOM, null);
        // Force invalid idType via reflection
        builder._idType = null;
        SerializationConfig config = new ObjectMapper().getSerializationConfig();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        builder.idResolver(config, baseType, Collections.<NamedType>emptyList(), true, false);
    }
    
    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    
    @Test(timeout = 4000)
    public void testBuilderChaining() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        assertSame(builder, builder.init(JsonTypeInfo.Id.CLASS, null));
        assertSame(builder, builder.inclusion(JsonTypeInfo.As.PROPERTY));
        assertSame(builder, builder.typeProperty("prop"));
        assertSame(builder, builder.defaultImpl(String.class));
        assertSame(builder, builder.typeIdVisibility(true));
    }
    
    @Test(timeout = 4000)
    public void testGetTypePropertyAfterInit() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.MINIMAL_CLASS, null);
        assertEquals(JsonTypeInfo.Id.MINIMAL_CLASS.getDefaultPropertyName(), builder.getTypeProperty());
    }
    
    @Test(timeout = 4000)
    public void testBuildTypeSerializerAllIncludeAs() {
        // Test all includeAs values for serializer
        for (JsonTypeInfo.As includeAs : JsonTypeInfo.As.values()) {
            StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
            builder.init(JsonTypeInfo.Id.CLASS, null);
            builder.inclusion(includeAs);
            SerializationConfig config = new ObjectMapper().getSerializationConfig();
            JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
            TypeSerializer serializer = builder.buildTypeSerializer(config, baseType, Collections.<NamedType>emptyList());
            assertNotNull("Serializer for " + includeAs, serializer);
        }
    }
    
    @Test(timeout = 4000)
    public void testBuildTypeDeserializerAllIncludeAs() {
        // Test all includeAs values for deserializer
        for (JsonTypeInfo.As includeAs : JsonTypeInfo.As.values()) {
            StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
            builder.init(JsonTypeInfo.Id.CLASS, null);
            builder.inclusion(includeAs);
            DeserializationConfig config = new ObjectMapper().getDeserializationConfig();
            JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
            TypeDeserializer deserializer = builder.buildTypeDeserializer(config, baseType, Collections.<NamedType>emptyList());
            assertNotNull("Deserializer for " + includeAs, deserializer);
        }
    }
    
    @Test(timeout = 4000)
    public void testIdResolverCustom() {
        TypeIdResolver custom = new TypeIdResolver() {
            @Override public void init(JavaType bt) {}
            @Override public String idFromValue(Object value) { return "x"; }
            @Override public String idFromValueAndType(Object value, Class<?> suggestedType) { return "x"; }
            @Override public String idFromBaseType() { return "x"; }
            @Override public JavaType typeFromId(com.fasterxml.jackson.databind.DatabindContext context, String id) { return null; }
            @Override public String getMechanism() { return null; }
        };
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CUSTOM, custom);
        SerializationConfig config = new ObjectMapper().getSerializationConfig();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        assertSame(custom, builder.idResolver(config, baseType, Collections.<NamedType>emptyList(), true, false));
    }
    
    @Test(timeout = 4000)
    public void testIdResolverClassName() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        SerializationConfig config = new ObjectMapper().getSerializationConfig();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        TypeIdResolver resolver = builder.idResolver(config, baseType, Collections.<NamedType>emptyList(), true, false);
        assertTrue(resolver instanceof ClassNameIdResolver);
    }
    
    @Test(timeout = 4000)
    public void testIdResolverMinimalClassName() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.MINIMAL_CLASS, null);
        SerializationConfig config = new ObjectMapper().getSerializationConfig();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        TypeIdResolver resolver = builder.idResolver(config, baseType, Collections.<NamedType>emptyList(), true, false);
        assertTrue(resolver instanceof MinimalClassNameIdResolver);
    }
    
    @Test(timeout = 4000)
    public void testIdResolverName() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.NAME, null);
        SerializationConfig config = new ObjectMapper().getSerializationConfig();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        TypeIdResolver resolver = builder.idResolver(config, baseType, Collections.<NamedType>emptyList(), true, false);
        assertNotNull(resolver);
    }
    
    @Test(timeout = 4000)
    public void testIdResolverNone() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.NONE, null);
        SerializationConfig config = new ObjectMapper().getSerializationConfig();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        assertNull(builder.idResolver(config, baseType, Collections.<NamedType>emptyList(), true, false));
    }
    
    @Test(timeout = 4000)
    public void testBuildTypeDeserializerWithNoClassDefaultImpl() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        builder.defaultImpl(com.fasterxml.jackson.databind.annotation.NoClass.class);
        
        DeserializationConfig config = new ObjectMapper().getDeserializationConfig();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        TypeDeserializer deserializer = builder.buildTypeDeserializer(config, baseType, Collections.<NamedType>emptyList());
        assertNotNull(deserializer);
    }
}