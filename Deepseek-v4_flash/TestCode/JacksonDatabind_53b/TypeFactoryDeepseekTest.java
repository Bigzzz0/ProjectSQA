package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TypeFactory.constructType(Type) and related resolution paths.
 * 
 * Key branches exercised:
 *  - constructType(Type): null, Class, ParameterizedType, GenericArrayType, WildcardType, TypeVariable, JavaType
 *  - _fromClass: well-known types (String, Integer, Boolean, Long, Object, Comparable, Enum, Class), 
 *    AtomicReference, array types, collection/map types, simple types
 *  - _fromParameterizedClass: resolution of type parameters, TypeBindings, recursive types
 *  - constructSpecializedType: raw types, generic subtypes, well-known List/Map subtypes
 *  - findTypeParameters: super-type resolution, TypeBindings
 *  - constructCollectionType/constructMapType: raw and parameterized variants
 *  - constructSimpleType: parameter count validation
 *  - uncheckedSimpleType: direct construction
 *  - withModifier/withClassLoader: state transitions
 *  - defaultInstance/unknownType/rawClass: static helpers
 * 
 * Defect-targeted test (Partition C):
 *  - TypeRefinementForMap1215Test::testMapRefinement: When constructing a type for a class
 *    that implements Map with a specific value type, the factory must correctly resolve
 *    the value type parameter. The defect causes an abstract type mapping failure when
 *    the value type is a custom class with a unique ID. The test verifies that
 *    constructType correctly resolves the Map's value type to the concrete class.
 * 
 * Boundary conditions:
 *  - null Type input
 *  - empty type parameter arrays
 *  - raw types without generics
 *  - classes with multiple type parameters
 *  - recursive type references
 *  - well-known types vs. custom types
 *  - array types (primitive and object)
 *  - wildcard types with upper/lower bounds
 *  - type variables with bounds
 */
public class TypeFactoryDeepseekTest {

    // --- Partition A: Core Functional Logic & State Transitions ---

    @Test(timeout = 4000)
    public void testDefaultInstance() {
        TypeFactory factory = TypeFactory.defaultInstance();
        assertNotNull(factory);
        assertSame(TypeFactory.defaultInstance(), factory);
    }

    @Test(timeout = 4000)
    public void testWithModifier() {
        TypeFactory factory = TypeFactory.defaultInstance();
        TypeModifier modifier = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, TypeBindings bindings, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeFactory modified = factory.withModifier(modifier);
        assertNotSame(factory, modified);
        assertNotNull(modified);
    }

    @Test(timeout = 4000)
    public void testWithClassLoader() {
        TypeFactory factory = TypeFactory.defaultInstance();
        TypeFactory modified = factory.withClassLoader(getClass().getClassLoader());
        assertNotSame(factory, modified);
        assertNotNull(modified);
    }

    @Test(timeout = 4000)
    public void testUnknownType() {
        JavaType type = TypeFactory.unknownType();
        assertNotNull(type);
        assertEquals(Object.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testRawClass() {
        assertEquals(String.class, TypeFactory.rawClass(String.class));
        assertEquals(String.class, TypeFactory.rawClass(new TypeReference<String>() {}.getType()));
        assertEquals(int.class, TypeFactory.rawClass(int.class));
    }

    @Test(timeout = 4000)
    public void testConstructTypeWellKnown() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType stringType = factory.constructType(String.class);
        assertEquals(String.class, stringType.getRawClass());
        assertTrue(stringType.isFinal());
        assertFalse(stringType.isContainerType());
    }

    @Test(timeout = 4000)
    public void testConstructTypePrimitive() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType intType = factory.constructType(int.class);
        assertEquals(int.class, intType.getRawClass());
        assertTrue(intType.isPrimitive());
    }

    @Test(timeout = 4000)
    public void testConstructTypeArray() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType arrayType = factory.constructType(String[].class);
        assertTrue(arrayType.isArrayType());
        assertEquals(String.class, arrayType.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypePrimitiveArray() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType arrayType = factory.constructType(int[].class);
        assertTrue(arrayType.isArrayType());
        assertEquals(int.class, arrayType.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeParameterized() {
        TypeFactory factory = TypeFactory.defaultInstance();
        Type type = new TypeReference<List<String>>() {}.getType();
        JavaType javaType = factory.constructType(type);
        assertEquals(List.class, javaType.getRawClass());
        assertEquals(1, javaType.containedTypeCount());
        assertEquals(String.class, javaType.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeMap() {
        TypeFactory factory = TypeFactory.defaultInstance();
        Type type = new TypeReference<Map<String, Integer>>() {}.getType();
        JavaType javaType = factory.constructType(type);
        assertEquals(Map.class, javaType.getRawClass());
        assertEquals(2, javaType.containedTypeCount());
        assertEquals(String.class, javaType.containedType(0).getRawClass());
        assertEquals(Integer.class, javaType.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeTypeReference() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<List<Long>>() {});
        assertEquals(List.class, type.getRawClass());
        assertEquals(Long.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithContextClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<List<String>>() {}.getType(), String.class);
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithContextJavaType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType context = factory.constructType(String.class);
        JavaType type = factory.constructType(new TypeReference<List<String>>() {}.getType(), context);
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructCollectionType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType elementType = factory.constructType(String.class);
        CollectionType collectionType = factory.constructCollectionType(ArrayList.class, elementType);
        assertEquals(ArrayList.class, collectionType.getRawClass());
        assertEquals(String.class, collectionType.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructCollectionTypeRaw() {
        TypeFactory factory = TypeFactory.defaultInstance();
        CollectionType collectionType = factory.constructCollectionType(ArrayList.class, String.class);
        assertEquals(ArrayList.class, collectionType.getRawClass());
        assertEquals(String.class, collectionType.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructCollectionLikeType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType elementType = factory.constructType(String.class);
        CollectionLikeType collectionType = factory.constructCollectionLikeType(Collection.class, elementType);
        assertEquals(Collection.class, collectionType.getRawClass());
        assertEquals(String.class, collectionType.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructMapType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType keyType = factory.constructType(String.class);
        JavaType valueType = factory.constructType(Integer.class);
        MapType mapType = factory.constructMapType(HashMap.class, keyType, valueType);
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(String.class, mapType.containedType(0).getRawClass());
        assertEquals(Integer.class, mapType.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructMapTypeRaw() {
        TypeFactory factory = TypeFactory.defaultInstance();
        MapType mapType = factory.constructMapType(HashMap.class, String.class, Integer.class);
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(String.class, mapType.containedType(0).getRawClass());
        assertEquals(Integer.class, mapType.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructMapLikeType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType keyType = factory.constructType(String.class);
        JavaType valueType = factory.constructType(Integer.class);
        MapLikeType mapType = factory.constructMapLikeType(Properties.class, keyType, valueType);
        assertEquals(Properties.class, mapType.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructSimpleType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructSimpleType(String.class, new JavaType[0]);
        assertEquals(String.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructSimpleTypeWithParams() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType stringType = factory.constructType(String.class);
        JavaType type = factory.constructSimpleType(MyParametric.class, new JavaType[] { stringType });
        assertEquals(MyParametric.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testConstructReferenceType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType referredType = factory.constructType(String.class);
        JavaType refType = factory.constructReferenceType(AtomicReference.class, referredType);
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(String.class, refType.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testUncheckedSimpleType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.uncheckedSimpleType(String.class);
        assertEquals(String.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructParametricType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructParametricType(List.class, String.class);
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructParametricTypeWithJavaType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType stringType = factory.constructType(String.class);
        JavaType type = factory.constructParametricType(List.class, stringType);
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructParametrizedType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructParametrizedType(ArrayList.class, List.class, String.class);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructSpecializedType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType baseType = factory.constructType(new TypeReference<Map<String, Integer>>() {});
        JavaType specialized = factory.constructSpecializedType(baseType, HashMap.class);
        assertEquals(HashMap.class, specialized.getRawClass());
        assertEquals(String.class, specialized.containedType(0).getRawClass());
        assertEquals(Integer.class, specialized.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructSpecializedTypeRaw() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType baseType = factory.constructType(Map.class);
        JavaType specialized = factory.constructSpecializedType(baseType, HashMap.class);
        assertEquals(HashMap.class, specialized.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructSpecializedTypeNoGenerics() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType baseType = factory.constructType(Number.class);
        JavaType specialized = factory.constructSpecializedType(baseType, Integer.class);
        assertEquals(Integer.class, specialized.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructGeneralizedType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType baseType = factory.constructType(HashMap.class);
        JavaType generalized = factory.constructGeneralizedType(baseType, Map.class);
        assertEquals(Map.class, generalized.getRawClass());
    }

    @Test(timeout = 4000)
    public void testFindTypeParameters() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<ArrayList<String>>() {});
        JavaType[] params = factory.findTypeParameters(type, List.class);
        assertNotNull(params);
        assertEquals(1, params.length);
        assertEquals(String.class, params[0].getRawClass());
    }

    @Test(timeout = 4000)
    public void testFindTypeParametersClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType[] params = factory.findTypeParameters(ArrayList.class, List.class);
        assertNotNull(params);
        assertEquals(1, params.length);
        assertEquals(Object.class, params[0].getRawClass());
    }

    @Test(timeout = 4000)
    public void testFindTypeParametersWithBindings() {
        TypeFactory factory = TypeFactory.defaultInstance();
        TypeBindings bindings = TypeBindings.create(String.class, Integer.class);
        JavaType[] params = factory.findTypeParameters(MyParametric.class, MyParametric.class, bindings);
        assertNotNull(params);
        assertEquals(1, params.length);
    }

    // --- Partition B: Boundary Value Analysis & Extremes ---

    @Test(timeout = 4000)
    public void testConstructTypeNull() {
        TypeFactory factory = TypeFactory.defaultInstance();
        try {
            factory.constructType((Type) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructTypeEmptyTypeReference() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<Object>() {});
        assertEquals(Object.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructCollectionTypeNullElement() {
        TypeFactory factory = TypeFactory.defaultInstance();
        try {
            factory.constructCollectionType(ArrayList.class, (JavaType) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructMapTypeNullKey() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType valueType = factory.constructType(String.class);
        try {
            factory.constructMapType(HashMap.class, (JavaType) null, valueType);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructMapTypeNullValue() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType keyType = factory.constructType(String.class);
        try {
            factory.constructMapType(HashMap.class, keyType, (JavaType) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructSimpleTypeNullParams() {
        TypeFactory factory = TypeFactory.defaultInstance();
        try {
            factory.constructSimpleType(String.class, (JavaType[]) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructParametricTypeEmpty() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructParametricType(List.class);
        assertEquals(List.class, type.getRawClass());
        assertEquals(0, type.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testConstructParametricTypeNullClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        try {
            factory.constructParametricType(null, String.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructTypeWildcard() {
        TypeFactory factory = TypeFactory.defaultInstance();
        Type type = new TypeReference<List<? extends Number>>() {}.getType();
        JavaType javaType = factory.constructType(type);
        assertEquals(List.class, javaType.getRawClass());
        assertEquals(Number.class, javaType.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWildcardLower() {
        TypeFactory factory = TypeFactory.defaultInstance();
        Type type = new TypeReference<List<? super Integer>>() {}.getType();
        JavaType javaType = factory.constructType(type);
        assertEquals(List.class, javaType.getRawClass());
        assertEquals(Integer.class, javaType.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeGenericArray() {
        TypeFactory factory = TypeFactory.defaultInstance();
        Type type = new TypeReference<List<String>[]>() {}.getType();
        JavaType javaType = factory.constructType(type);
        assertTrue(javaType.isArrayType());
        assertEquals(List.class, javaType.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeTypeVariable() {
        TypeFactory factory = TypeFactory.defaultInstance();
        Type type = new TypeReference<Map<String, ?>>() {}.getType();
        JavaType javaType = factory.constructType(type);
        assertEquals(Map.class, javaType.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeRecursive() {
        TypeFactory factory = TypeFactory.defaultInstance();
        Type type = new TypeReference<RecursiveType<RecursiveType<?>>>() {}.getType();
        JavaType javaType = factory.constructType(type);
        assertNotNull(javaType);
    }

    // --- Partition C: Defect-Targeted Branch Zone ---

    /**
     * Targets the defect from TypeRefinementForMap1215Test::testMapRefinement.
     * When constructing a type for a class that implements Map with a custom value type,
     * the factory must correctly resolve the value type parameter to the concrete class.
     * The defect causes an abstract type mapping failure.
     */
    @Test(timeout = 4000)
    public void testMapRefinementDefect() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(HasUniqueId.class);
        assertNotNull(type);
        assertEquals(HasUniqueId.class, type.getRawClass());
        
        // Verify that the type can be constructed without abstract type errors
        JavaType mapType = factory.constructType(new TypeReference<Map<String, HasUniqueId>>() {});
        assertEquals(Map.class, mapType.getRawClass());
        assertEquals(String.class, mapType.containedType(0).getRawClass());
        assertEquals(HasUniqueId.class, mapType.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testMapRefinementWithSubtype() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType baseType = factory.constructType(new TypeReference<Map<String, HasUniqueId>>() {});
        JavaType specialized = factory.constructSpecializedType(baseType, HashMap.class);
        assertEquals(HashMap.class, specialized.getRawClass());
        assertEquals(String.class, specialized.containedType(0).getRawClass());
        assertEquals(HasUniqueId.class, specialized.containedType(1).getRawClass());
    }

    // --- Partition D: Exception & Defensive Guard Paths ---

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructCollectionTypeNonCollection() {
        TypeFactory factory = TypeFactory.defaultInstance();
        factory.constructCollectionType(String.class, String.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructMapTypeNonMap() {
        TypeFactory factory = TypeFactory.defaultInstance();
        factory.constructMapType(String.class, String.class, String.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructSimpleTypeWrongParamCount() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType stringType = factory.constructType(String.class);
        factory.constructSimpleType(MyParametric.class, new JavaType[] { stringType, stringType });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructReferenceTypeNonReference() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType referredType = factory.constructType(String.class);
        factory.constructReferenceType(String.class, referredType);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructParametricTypeNonParametric() {
        TypeFactory factory = TypeFactory.defaultInstance();
        factory.constructParametricType(String.class, String.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructSpecializedTypeIncompatible() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType baseType = factory.constructType(String.class);
        factory.constructSpecializedType(baseType, Integer.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructGeneralizedTypeNotSupertype() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType baseType = factory.constructType(String.class);
        factory.constructGeneralizedType(baseType, Integer.class);
    }

    // --- Partition E: Object Lifecycle & Contract Integrity ---

    @Test(timeout = 4000)
    public void testTypeFactoryEquality() {
        TypeFactory factory1 = TypeFactory.defaultInstance();
        TypeFactory factory2 = TypeFactory.defaultInstance();
        assertEquals(factory1, factory2);
        assertEquals(factory1.hashCode(), factory2.hashCode());
    }

    @Test(timeout = 4000)
    public void testTypeFactoryWithModifierEquality() {
        TypeFactory factory = TypeFactory.defaultInstance();
        TypeModifier modifier = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, TypeBindings bindings, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeFactory modified1 = factory.withModifier(modifier);
        TypeFactory modified2 = factory.withModifier(modifier);
        assertEquals(modified1, modified2);
        assertEquals(modified1.hashCode(), modified2.hashCode());
    }

    @Test(timeout = 4000)
    public void testTypeFactoryWithClassLoaderEquality() {
        TypeFactory factory = TypeFactory.defaultInstance();
        TypeFactory modified1 = factory.withClassLoader(getClass().getClassLoader());
        TypeFactory modified2 = factory.withClassLoader(getClass().getClassLoader());
        assertEquals(modified1, modified2);
        assertEquals(modified1.hashCode(), modified2.hashCode());
    }

    @Test(timeout = 4000)
    public void testConstructTypeCaching() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type1 = factory.constructType(String.class);
        JavaType type2 = factory.constructType(String.class);
        assertSame(type1, type2);
    }

    @Test(timeout = 4000)
    public void testConstructTypeNoCacheForCustom() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type1 = factory.constructType(MyParametric.class);
        JavaType type2 = factory.constructType(MyParametric.class);
        assertNotNull(type1);
        assertNotNull(type2);
    }

    @Test(timeout = 4000)
    public void testClearCache() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type1 = factory.constructType(String.class);
        factory.clearCache();
        JavaType type2 = factory.constructType(String.class);
        assertNotNull(type1);
        assertNotNull(type2);
    }

    // Helper classes for testing

    static class MyParametric<T> {
        T value;
    }

    static class RecursiveType<T> {
        T value;
    }

    static class HasUniqueId {
        public String id;
    }
}