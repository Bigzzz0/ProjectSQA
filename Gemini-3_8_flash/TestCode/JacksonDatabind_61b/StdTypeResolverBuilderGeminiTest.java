package com.fasterxml.jackson.databind.jsontype.impl;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Method                     | Condition / Branch Target                | Test Method
 * ----------------------------------------------------------------------------------------------------
 * init                       | idType == null (throws IAE)             | testInitNullIdTypeThrowsException
 *                            | Valid idType, customIdResolver null      | testInitValidIdTypeAndDefaults
 *                            | Valid idType, customIdResolver present   | testInitWithCustomResolver
 * ----------------------------------------------------------------------------------------------------
 * inclusion                  | includeAs == null (throws IAE)          | testInclusionNullThrowsException
 *                            | Valid includeAs values                   | testInclusionValid
 * ----------------------------------------------------------------------------------------------------
 * typeProperty               | null property name (resets to default)   | testTypePropertyNullResetsToDefault
 *                            | empty string (resets to default)         | testTypePropertyEmptyResetsToDefault
 *                            | custom property name                     | testTypePropertyCustomName
 * ----------------------------------------------------------------------------------------------------
 * defaultImpl & accessors    | defaultImpl set/get                      | testDefaultImplAndAccessors
 *                            | typeIdVisibility set/isTypeIdVisible     | testTypeIdVisibility
 * ----------------------------------------------------------------------------------------------------
 * noTypeInfoBuilder          | Id.NONE initialized                      | testNoTypeInfoBuilder
 * ----------------------------------------------------------------------------------------------------
 * buildTypeSerializer        | _idType == Id.NONE                       | testBuildTypeSerializerNoneReturnsNull
 *                            | baseType.isPrimitive() -> returns null   | testDefectPrimitiveBaseTypeSerializerReturnsNull [DEFECT #1395]
 *                            | As.WRAPPER_ARRAY                         | testBuildTypeSerializerWrapperArray
 *                            | As.PROPERTY                              | testBuildTypeSerializerProperty
 *                            | As.WRAPPER_OBJECT                        | testBuildTypeSerializerWrapperObject
 *                            | As.EXTERNAL_PROPERTY                     | testBuildTypeSerializerExternalProperty
 *                            | As.EXISTING_PROPERTY                     | testBuildTypeSerializerExistingProperty
 *                            | _includeAs == null (throws ISE)          | testBuildTypeSerializerNullInclusionThrowsISE
 * ----------------------------------------------------------------------------------------------------
 * buildTypeDeserializer      | _idType == Id.NONE                       | testBuildTypeDeserializerNoneReturnsNull
 *                            | baseType.isPrimitive() -> returns null   | testDefectPrimitiveBaseTypeDeserializerReturnsNull [DEFECT #1395]
 *                            | _defaultImpl == null                     | testBuildTypeDeserializerDefaultImplNull
 *                            | _defaultImpl == Void.class               | testBuildTypeDeserializerDefaultImplVoid
 *                            | _defaultImpl == NoClass.class            | testBuildTypeDeserializerDefaultImplNoClass
 *                            | _defaultImpl specialized subclass        | testBuildTypeDeserializerDefaultImplSpecialized
 *                            | As.WRAPPER_ARRAY                         | testBuildTypeDeserializerWrapperArray
 *                            | As.PROPERTY                              | testBuildTypeDeserializerProperty
 *                            | As.EXISTING_PROPERTY                     | testBuildTypeDeserializerExistingProperty
 *                            | As.WRAPPER_OBJECT                        | testBuildTypeDeserializerWrapperObject
 *                            | As.EXTERNAL_PROPERTY                     | testBuildTypeDeserializerExternalProperty
 *                            | _includeAs == null (throws ISE)          | testBuildTypeDeserializerNullInclusionThrowsISE
 * ----------------------------------------------------------------------------------------------------
 * idResolver                 | _customIdResolver != null                | testIdResolverUsesCustomResolver
 *                            | _idType == null (throws ISE)             | testIdResolverUninitializedThrowsISE
 *                            | Id.CLASS                                 | testIdResolverClass
 *                            | Id.MINIMAL_CLASS                         | testIdResolverMinimalClass
 *                            | Id.NAME                                  | testIdResolverName
 *                            | Id.CUSTOM without resolver (throws ISE)  | testIdResolverCustomWithoutResolverThrowsISE
 * ----------------------------------------------------------------------------------------------------
 */

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.annotation.NoClass;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class StdTypeResolverBuilderGeminiTest {

    private ObjectMapper _mapper;
    private SerializationConfig _serializationConfig;
    private DeserializationConfig _deserializationConfig;
    private TypeFactory _typeFactory;
    private JavaType _baseType;

    // Test hierarchy types
    static class SuperClass { }
    static class SubClass extends SuperClass { }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _serializationConfig = _mapper.getSerializationConfig();
        _deserializationConfig = _mapper.getDeserializationConfig();
        _typeFactory = _mapper.getTypeFactory();
        _baseType = _typeFactory.constructType(SuperClass.class);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitValidIdTypeAndDefaults() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);

        assertEquals(JsonTypeInfo.Id.CLASS.getDefaultPropertyName(), builder.getTypeProperty());
        assertFalse(builder.isTypeIdVisible());
        assertNull(builder.getDefaultImpl());
    }

    @Test(timeout = 4000)
    public void testInitWithCustomResolver() {
        TypeIdResolver customResolver = new ClassNameIdResolver(_baseType, _typeFactory);
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CUSTOM, customResolver);

        builder.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);
        TypeSerializer serializer = builder.buildTypeSerializer(_serializationConfig, _baseType, null);
        assertNotNull(serializer);
        assertEquals(customResolver, serializer.getTypeIdResolver());
    }

    @Test(timeout = 4000)
    public void testInclusionValid() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        StdTypeResolverBuilder returned = builder.inclusion(JsonTypeInfo.As.PROPERTY);

        assertSame(builder, returned);
    }

    @Test(timeout = 4000)
    public void testTypePropertyCustomName() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.typeProperty("@customTypeProp");

        assertEquals("@customTypeProp", builder.getTypeProperty());
    }

    @Test(timeout = 4000)
    public void testDefaultImplAndAccessors() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.defaultImpl(SubClass.class);

        assertEquals(SubClass.class, builder.getDefaultImpl());
    }

    @Test(timeout = 4000)
    public void testTypeIdVisibility() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        assertFalse(builder.isTypeIdVisible());

        builder.typeIdVisibility(true);
        assertTrue(builder.isTypeIdVisible());

        builder.typeIdVisibility(false);
        assertFalse(builder.isTypeIdVisible());
    }

    @Test(timeout = 4000)
    public void testNoTypeInfoBuilder() {
        StdTypeResolverBuilder builder = StdTypeResolverBuilder.noTypeInfoBuilder();
        assertNotNull(builder);

        TypeSerializer ser = builder.buildTypeSerializer(_serializationConfig, _baseType, null);
        assertNull(ser);

        TypeDeserializer deser = builder.buildTypeDeserializer(_deserializationConfig, _baseType, null);
        assertNull(deser);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Serialization Inclusions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTypePropertyNullResetsToDefault() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.typeProperty("someCustom");
        assertEquals("someCustom", builder.getTypeProperty());

        builder.typeProperty(null);
        assertEquals(JsonTypeInfo.Id.CLASS.getDefaultPropertyName(), builder.getTypeProperty());
    }

    @Test(timeout = 4000)
    public void testTypePropertyEmptyResetsToDefault() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.NAME, null);
        builder.typeProperty("");
        assertEquals(JsonTypeInfo.Id.NAME.getDefaultPropertyName(), builder.getTypeProperty());
    }

    @Test(timeout = 4000)
    public void testBuildTypeSerializerNoneReturnsNull() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.NONE, null);
        builder.inclusion(JsonTypeInfo.As.PROPERTY);

        TypeSerializer ser = builder.buildTypeSerializer(_serializationConfig, _baseType, null);
        assertNull(ser);
    }

    @Test(timeout = 4000)
    public void testBuildTypeSerializerWrapperArray() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);

        TypeSerializer ser = builder.buildTypeSerializer(_serializationConfig, _baseType, null);
        assertNotNull(ser);
        assertTrue(ser instanceof AsArrayTypeSerializer);
    }

    @Test(timeout = 4000)
    public void testBuildTypeSerializerProperty() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.PROPERTY);

        TypeSerializer ser = builder.buildTypeSerializer(_serializationConfig, _baseType, null);
        assertNotNull(ser);
        assertTrue(ser instanceof AsPropertyTypeSerializer);
        assertEquals(JsonTypeInfo.Id.CLASS.getDefaultPropertyName(), ser.getPropertyName());
    }

    @Test(timeout = 4000)
    public void testBuildTypeSerializerWrapperObject() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);

        TypeSerializer ser = builder.buildTypeSerializer(_serializationConfig, _baseType, null);
        assertNotNull(ser);
        assertTrue(ser instanceof AsWrapperTypeSerializer);
    }

    @Test(timeout = 4000)
    public void testBuildTypeSerializerExternalProperty() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.EXTERNAL_PROPERTY);

        TypeSerializer ser = builder.buildTypeSerializer(_serializationConfig, _baseType, null);
        assertNotNull(ser);
        assertTrue(ser instanceof AsExternalTypeSerializer);
    }

    @Test(timeout = 4000)
    public void testBuildTypeSerializerExistingProperty() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.EXISTING_PROPERTY);

        TypeSerializer ser = builder.buildTypeSerializer(_serializationConfig, _baseType, null);
        assertNotNull(ser);
        assertTrue(ser instanceof AsExistingPropertyTypeSerializer);
    }

    // =========================================================================
    // Partition C: Deserialization Inclusion & DefaultImpl Variants
    // =========================================================================

    @Test(timeout = 4000)
    public void testBuildTypeDeserializerNoneReturnsNull() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.NONE, null);
        builder.inclusion(JsonTypeInfo.As.PROPERTY);

        TypeDeserializer deser = builder.buildTypeDeserializer(_deserializationConfig, _baseType, null);
        assertNull(deser);
    }

    @Test(timeout = 4000)
    public void testBuildTypeDeserializerDefaultImplNull() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
        builder.defaultImpl(null);

        TypeDeserializer deser = builder.buildTypeDeserializer(_deserializationConfig, _baseType, null);
        assertNotNull(deser);
        assertTrue(deser instanceof AsArrayTypeDeserializer);
        assertNull(deser.getDefaultImpl());
    }

    @Test(timeout = 4000)
    public void testBuildTypeDeserializerDefaultImplVoid() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);
        builder.defaultImpl(Void.class);

        TypeDeserializer deser = builder.buildTypeDeserializer(_deserializationConfig, _baseType, null);
        assertNotNull(deser);
        assertTrue(deser instanceof AsWrapperTypeDeserializer);
        assertEquals(Void.class, deser.getDefaultImpl());
    }

    @Test(timeout = 4000)
    public void testBuildTypeDeserializerDefaultImplNoClass() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.EXTERNAL_PROPERTY);
        builder.defaultImpl(NoClass.class);

        TypeDeserializer deser = builder.buildTypeDeserializer(_deserializationConfig, _baseType, null);
        assertNotNull(deser);
        assertTrue(deser instanceof AsExternalTypeDeserializer);
        assertEquals(NoClass.class, deser.getDefaultImpl());
    }

    @Test(timeout = 4000)
    public void testBuildTypeDeserializerDefaultImplSpecialized() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        builder.defaultImpl(SubClass.class);

        TypeDeserializer deser = builder.buildTypeDeserializer(_deserializationConfig, _baseType, null);
        assertNotNull(deser);
        assertTrue(deser instanceof AsPropertyTypeDeserializer);
        assertEquals(SubClass.class, deser.getDefaultImpl());
    }

    @Test(timeout = 4000)
    public void testBuildTypeDeserializerExistingProperty() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.EXISTING_PROPERTY);

        TypeDeserializer deser = builder.buildTypeDeserializer(_deserializationConfig, _baseType, null);
        assertNotNull(deser);
        assertTrue(deser instanceof AsPropertyTypeDeserializer);
    }

    // =========================================================================
    // Partition D: idResolver() Branch Exploration
    // =========================================================================

    @Test(timeout = 4000)
    public void testIdResolverUsesCustomResolver() {
        TypeIdResolver custom = new ClassNameIdResolver(_baseType, _typeFactory);
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, custom);
        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);

        TypeSerializer ser = builder.buildTypeSerializer(_serializationConfig, _baseType, null);
        assertSame(custom, ser.getTypeIdResolver());
    }

    @Test(timeout = 4000)
    public void testIdResolverClass() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);

        TypeSerializer ser = builder.buildTypeSerializer(_serializationConfig, _baseType, null);
        assertTrue(ser.getTypeIdResolver() instanceof ClassNameIdResolver);
    }

    @Test(timeout = 4000)
    public void testIdResolverMinimalClass() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.MINIMAL_CLASS, null);
        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);

        TypeSerializer ser = builder.buildTypeSerializer(_serializationConfig, _baseType, null);
        assertTrue(ser.getTypeIdResolver() instanceof MinimalClassNameIdResolver);
    }

    @Test(timeout = 4000)
    public void testIdResolverName() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.NAME, null);
        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);

        Collection<NamedType> subtypes = new ArrayList<NamedType>();
        subtypes.add(new NamedType(SubClass.class, "sub"));

        TypeSerializer ser = builder.buildTypeSerializer(_serializationConfig, _baseType, subtypes);
        assertTrue(ser.getTypeIdResolver() instanceof TypeNameIdResolver);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testIdResolverCustomWithoutResolverThrowsISE() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CUSTOM, null);
        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);

        builder.buildTypeSerializer(_serializationConfig, _baseType, null);
    }

    // =========================================================================
    // Partition E: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInitNullIdTypeThrowsException() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(null, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInclusionNullThrowsException() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.inclusion(null);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testIdResolverUninitializedThrowsISE() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.inclusion(JsonTypeInfo.As.WRAPPER_ARRAY);
        builder.buildTypeSerializer(_serializationConfig, _baseType, null);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testBuildTypeSerializerNullInclusionThrowsISE() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.buildTypeSerializer(_serializationConfig, _baseType, null);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testBuildTypeDeserializerNullInclusionThrowsISE() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.buildTypeDeserializer(_deserializationConfig, _baseType, null);
    }

    // =========================================================================
    // Partition F: Defect-Targeted Branch Zone (databind#1395 / Primitive Handling)
    // =========================================================================

    /**
     * Target Defect: databind#1395 (DefaultTypingWithPrimitivesTest::testDefaultTypingWithLong).
     * TypeSerializer should NOT be built for primitive types, regardless of default typing settings.
     * Expected behavior: buildTypeSerializer returns null when baseType.isPrimitive() is true.
     */
    @Test(timeout = 4000)
    public void testDefectPrimitiveBaseTypeSerializerReturnsNull() {
        JavaType primitiveType = _typeFactory.constructType(long.class);
        assertTrue(primitiveType.isPrimitive());

        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.PROPERTY);

        TypeSerializer serializer = builder.buildTypeSerializer(_serializationConfig, primitiveType, null);
        assertNull("TypeSerializer must be null for primitive base type (databind#1395)", serializer);
    }

    /**
     * Target Defect: databind#1395 (DefaultTypingWithPrimitivesTest::testDefaultTypingWithLong).
     * TypeDeserializer should NOT be built for primitive types, regardless of default typing settings.
     * Expected behavior: buildTypeDeserializer returns null when baseType.isPrimitive() is true.
     */
    @Test(timeout = 4000)
    public void testDefectPrimitiveBaseTypeDeserializerReturnsNull() {
        JavaType primitiveType = _typeFactory.constructType(long.class);
        assertTrue(primitiveType.isPrimitive());

        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.PROPERTY);

        TypeDeserializer deserializer = builder.buildTypeDeserializer(_deserializationConfig, primitiveType, null);
        assertNull("TypeDeserializer must be null for primitive base type (databind#1395)", deserializer);
    }

    @Test(timeout = 4000)
    public void testDefectPrimitiveBaseTypeIntSerializerReturnsNull() {
        JavaType primitiveType = _typeFactory.constructType(int.class);
        assertTrue(primitiveType.isPrimitive());

        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);

        TypeSerializer serializer = builder.buildTypeSerializer(_serializationConfig, primitiveType, null);
        assertNull("TypeSerializer must be null for primitive int (databind#1395)", serializer);

        TypeDeserializer deserializer = builder.buildTypeDeserializer(_deserializationConfig, primitiveType, null);
        assertNull("TypeDeserializer must be null for primitive int (databind#1395)", deserializer);
    }
}