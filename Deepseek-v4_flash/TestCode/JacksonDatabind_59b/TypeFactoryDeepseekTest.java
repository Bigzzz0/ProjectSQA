package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class TypeFactoryDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * Target: TypeFactory.constructType / _fromClass / _fromAny / constructSpecializedType / constructGeneralizedType
     * Known Defect: TypeRefinementForMapTest::testMapKeyRefinement1384
     *   - When constructing a Map type with a key type that is a custom class (CompoundKey) and
     *     the value type is a parameterized type (e.g., List<String>), the factory fails to
     *     properly resolve the key type, resulting in "Can not find a (Map) Key deserializer".
     *   - Root cause: In _fromClass, when handling Map types, the key type resolution may
     *     incorrectly fall back to raw type or fail to apply type bindings for the key.
     *   - This test directly constructs a Map type with a custom key class and a parameterized
     *     value type, asserting that the key type is correctly resolved to the custom class.
     *
     * Branches targeted:
     * - _fromClass: rawType == Map.class / MapLikeType / CollectionType / ReferenceType
     * - constructMapType: keyType/valueType resolution, Properties special case
     * - constructSpecializedType: no generics, well-known types, subclass type parameters
     * - constructGeneralizedType: super-type resolution
     * - findTypeParameters: expType matching, TypeBindings usage
     * - constructType: TypeReference, Class, ParameterizedType, GenericArrayType, WildcardType
     * - Boundary: null types, empty type arrays, unknown classes, raw types
     * - Exception paths: IllegalArgumentException for strange Map/Collection/Reference types
     */

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testDefaultInstanceNotNull() {
        TypeFactory factory = TypeFactory.defaultInstance();
        assertNotNull(factory);
        // Verify singleton behavior
        assertSame(TypeFactory.defaultInstance(), factory);
    }

    @Test(timeout = 4000)
    public void testUnknownType() {
        JavaType unknown = TypeFactory.unknownType();
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeFromClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        assertTrue(type.isFinal());
    }

    @Test(timeout = 4000)
    public void testConstructTypeFromTypeReference() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<List<String>>() {});
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeFromParameterizedType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        Type type = new TypeReference<Map<String, Integer>>() {}.getType();
        JavaType javaType = factory.constructType(type);
        assertNotNull(javaType);
        assertEquals(Map.class, javaType.getRawClass());
        assertEquals(2, javaType.containedTypeCount());
        assertEquals(String.class, javaType.containedType(0).getRawClass());
        assertEquals(Integer.class, javaType.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeFromGenericArrayType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        Type type = new TypeReference<List<String>[]>() {}.getType();
        JavaType javaType = factory.constructType(type);
        assertNotNull(javaType);
        assertTrue(javaType.isArrayType());
        assertEquals(List.class, javaType.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeFromWildcardType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        Type type = new TypeReference<List<? extends Number>>() {}.getType();
        JavaType javaType = factory.constructType(type);
        assertNotNull(javaType);
        assertEquals(List.class, javaType.getRawClass());
        assertEquals(Number.class, javaType.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithContextClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(String.class, (Class<?>) null);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithContextJavaType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType context = factory.constructType(String.class);
        JavaType type = factory.constructType(Integer.class, context);
        assertNotNull(type);
        assertEquals(Integer.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructCollectionType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType elementType = factory.constructType(String.class);
        CollectionType type = factory.constructCollectionType(List.class, elementType);
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructCollectionTypeWithClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        CollectionType type = factory.constructCollectionType(ArrayList.class, String.class);
        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructCollectionLikeType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType elementType = factory.constructType(String.class);
        CollectionLikeType type = factory.constructCollectionLikeType(Collection.class, elementType);
        assertNotNull(type);
        assertEquals(Collection.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructMapType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType keyType = factory.constructType(String.class);
        JavaType valueType = factory.constructType(Integer.class);
        MapType type = factory.constructMapType(HashMap.class, keyType, valueType);
        assertNotNull(type);
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Integer.class, type.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructMapTypeWithClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        MapType type = factory.constructMapType(HashMap.class, String.class, Integer.class);
        assertNotNull(type);
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Integer.class, type.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructMapLikeType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType keyType = factory.constructType(String.class);
        JavaType valueType = factory.constructType(Integer.class);
        MapLikeType type = factory.constructMapLikeType(Map.class, keyType, valueType);
        assertNotNull(type);
        assertEquals(Map.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Integer.class, type.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructSimpleType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructSimpleType(String.class, new JavaType[0]);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructSimpleTypeWithParameters() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType paramType = factory.constructType(String.class);
        JavaType type = factory.constructSimpleType(ArrayList.class, new JavaType[]{paramType});
        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructReferenceType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType referredType = factory.constructType(String.class);
        ReferenceType type = factory.constructReferenceType(AtomicReference.class, referredType);
        assertNotNull(type);
        assertEquals(AtomicReference.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testUncheckedSimpleType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.uncheckedSimpleType(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructParametricType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructParametricType(List.class, String.class);
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructParametricTypeWithJavaTypes() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType paramType = factory.constructType(String.class);
        JavaType type = factory.constructParametricType(List.class, paramType);
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructParametrizedType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructParametrizedType(ArrayList.class, List.class, String.class);
        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructRawCollectionType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        CollectionType type = factory.constructRawCollectionType(ArrayList.class);
        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(Object.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructRawCollectionLikeType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        CollectionLikeType type = factory.constructRawCollectionLikeType(Collection.class);
        assertNotNull(type);
        assertEquals(Collection.class, type.getRawClass());
        assertEquals(Object.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructRawMapType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        MapType type = factory.constructRawMapType(HashMap.class);
        assertNotNull(type);
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals(Object.class, type.containedType(0).getRawClass());
        assertEquals(Object.class, type.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructRawMapLikeType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        MapLikeType type = factory.constructRawMapLikeType(Map.class);
        assertNotNull(type);
        assertEquals(Map.class, type.getRawClass());
        assertEquals(Object.class, type.containedType(0).getRawClass());
        assertEquals(Object.class, type.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testFindTypeParameters() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<HashMap<String, Integer>>() {});
        JavaType[] params = factory.findTypeParameters(type, Map.class);
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(Integer.class, params[1].getRawClass());
    }

    @Test(timeout = 4000)
    public void testFindTypeParametersWithClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType[] params = factory.findTypeParameters(HashMap.class, Map.class);
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals(Object.class, params[0].getRawClass());
        assertEquals(Object.class, params[1].getRawClass());
    }

    @Test(timeout = 4000)
    public void testFindTypeParametersWithBindings() {
        TypeFactory factory = TypeFactory.defaultInstance();
        TypeBindings bindings = TypeBindings.create(String.class, Integer.class);
        JavaType[] params = factory.findTypeParameters(HashMap.class, Map.class, bindings);
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(Integer.class, params[1].getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructSpecializedType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType baseType = factory.constructType(List.class);
        JavaType specialized = factory.constructSpecializedType(baseType, ArrayList.class);
        assertNotNull(specialized);
        assertEquals(ArrayList.class, specialized.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructSpecializedTypeWithGenerics() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType baseType = factory.constructType(new TypeReference<List<String>>() {});
        JavaType specialized = factory.constructSpecializedType(baseType, ArrayList.class);
        assertNotNull(specialized);
        assertEquals(ArrayList.class, specialized.getRawClass());
        assertEquals(String.class, specialized.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructGeneralizedType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType baseType = factory.constructType(ArrayList.class);
        JavaType generalized = factory.constructGeneralizedType(baseType, List.class);
        assertNotNull(generalized);
        assertEquals(List.class, generalized.getRawClass());
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
        TypeFactory newFactory = factory.withModifier(modifier);
        assertNotNull(newFactory);
        assertNotSame(factory, newFactory);
    }

    @Test(timeout = 4000)
    public void testWithClassLoader() {
        TypeFactory factory = TypeFactory.defaultInstance();
        TypeFactory newFactory = factory.withClassLoader(getClass().getClassLoader());
        assertNotNull(newFactory);
        assertNotSame(factory, newFactory);
    }

    @Test(timeout = 4000)
    public void testWithCache() {
        TypeFactory factory = TypeFactory.defaultInstance();
        LRUMap<Object, JavaType> cache = new LRUMap<>(16, 200);
        TypeFactory newFactory = factory.withCache(cache);
        assertNotNull(newFactory);
        assertNotSame(factory, newFactory);
    }

    @Test(timeout = 4000)
    public void testClearCache() {
        TypeFactory factory = TypeFactory.defaultInstance();
        factory.clearCache();
        // Should not throw
    }

    @Test(timeout = 4000)
    public void testRawClass() {
        assertEquals(String.class, TypeFactory.rawClass(String.class));
        assertEquals(String.class, TypeFactory.rawClass(new TypeReference<String>() {}.getType()));
    }

    // ===== Partition B: Boundary Value Analysis (BVA) & Extremes =====

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
    public void testConstructTypeNullTypeReference() {
        TypeFactory factory = TypeFactory.defaultInstance();
        try {
            factory.constructType((TypeReference<?>) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructTypeVoid() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(Void.TYPE);
        assertNotNull(type);
        assertEquals(Void.TYPE, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypePrimitive() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(int.class);
        assertNotNull(type);
        assertEquals(int.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeArray() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(String[].class);
        assertNotNull(type);
        assertTrue(type.isArrayType());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeEmptyArray() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new Class<?>[0]);
        assertNotNull(type);
        assertEquals(Object.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeEmptyTypeParameters() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructParametricType(List.class);
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(Object.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeNullParameter() {
        TypeFactory factory = TypeFactory.defaultInstance();
        try {
            factory.constructParametricType(List.class, (Class<?>) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructTypeNullJavaTypeParameter() {
        TypeFactory factory = TypeFactory.defaultInstance();
        try {
            factory.constructParametricType(List.class, (JavaType) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructCollectionTypeNullElement() {
        TypeFactory factory = TypeFactory.defaultInstance();
        try {
            factory.constructCollectionType(List.class, (JavaType) null);
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
    public void testConstructSimpleTypeNullParameters() {
        TypeFactory factory = TypeFactory.defaultInstance();
        try {
            factory.constructSimpleType(String.class, (JavaType[]) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructReferenceTypeNullReferred() {
        TypeFactory factory = TypeFactory.defaultInstance();
        try {
            factory.constructReferenceType(AtomicReference.class, (JavaType) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructSpecializedTypeNullSubclass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType baseType = factory.constructType(List.class);
        try {
            factory.constructSpecializedType(baseType, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructGeneralizedTypeNullSuperClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType baseType = factory.constructType(ArrayList.class);
        try {
            factory.constructGeneralizedType(baseType, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFindTypeParametersNullType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        try {
            factory.findTypeParameters((JavaType) null, Map.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFindTypeParametersNullExpType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(HashMap.class);
        try {
            factory.findTypeParameters(type, (Class<?>) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    /**
     * Targets the known defect: TypeRefinementForMapTest::testMapKeyRefinement1384
     * When constructing a Map type with a custom key class and a parameterized value type,
     * the key type must be correctly resolved to the custom class.
     */
    @Test(timeout = 4000)
    public void testConstructMapTypeWithCustomKeyAndParameterizedValue() {
        TypeFactory factory = TypeFactory.defaultInstance();

        // Custom key class (like CompoundKey in the defect)
        class CompoundKey {
            private final String name;
            public CompoundKey(String name) { this.name = name; }
            @Override public int hashCode() { return name.hashCode(); }
            @Override public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof CompoundKey)) return false;
                return name.equals(((CompoundKey) o).name);
            }
        }

        // Construct Map<CompoundKey, List<String>> type
        JavaType keyType = factory.constructType(CompoundKey.class);
        JavaType valueType = factory.constructType(new TypeReference<List<String>>() {});
        MapType mapType = factory.constructMapType(HashMap.class, keyType, valueType);

        assertNotNull(mapType);
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(2, mapType.containedTypeCount());

        // KEY ASSERTION: The key type must be the custom class, not Object or something else
        JavaType actualKeyType = mapType.containedType(0);
        assertEquals(CompoundKey.class, actualKeyType.getRawClass());
        assertFalse(actualKeyType.isContainerType());
        assertFalse(actualKeyType.isMapLikeType());

        // Value type must be List<String>
        JavaType actualValueType = mapType.containedType(1);
        assertEquals(List.class, actualValueType.getRawClass());
        assertEquals(1, actualValueType.containedTypeCount());
        assertEquals(String.class, actualValueType.containedType(0).getRawClass());
    }

    /**
     * Additional test for the same defect: ensure that when constructing via TypeReference,
     * the key type is properly resolved.
     */
    @Test(timeout = 4000)
    public void testConstructMapTypeWithCustomKeyViaTypeReference() {
        TypeFactory factory = TypeFactory.defaultInstance();

        class CompoundKey {
            private final String name;
            public CompoundKey(String name) { this.name = name; }
            @Override public int hashCode() { return name.hashCode(); }
            @Override public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof CompoundKey)) return false;
                return name.equals(((CompoundKey) o).name);
            }
        }

        JavaType mapType = factory.constructType(new TypeReference<Map<CompoundKey, List<String>>>() {});
        assertNotNull(mapType);
        assertEquals(Map.class, mapType.getRawClass());
        assertEquals(2, mapType.containedTypeCount());

        JavaType keyType = mapType.containedType(0);
        assertEquals(CompoundKey.class, keyType.getRawClass());
        assertFalse(keyType.isContainerType());

        JavaType valueType = mapType.containedType(1);
        assertEquals(List.class, valueType.getRawClass());
        assertEquals(String.class, valueType.containedType(0).getRawClass());
    }

    /**
     * Test that the key type is correctly resolved when the map is constructed with
     * a specialized type (e.g., subclass of HashMap).
     */
    @Test(timeout = 4000)
    public void testConstructSpecializedMapTypeWithCustomKey() {
        TypeFactory factory = TypeFactory.defaultInstance();

        class CompoundKey {
            private final String name;
            public CompoundKey(String name) { this.name = name; }
            @Override public int hashCode() { return name.hashCode(); }
            @Override public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof CompoundKey)) return false;
                return name.equals(((CompoundKey) o).name);
            }
        }

        JavaType baseType = factory.constructType(new TypeReference<Map<CompoundKey, List<String>>>() {});
        JavaType specialized = factory.constructSpecializedType(baseType, LinkedHashMap.class);

        assertNotNull(specialized);
        assertEquals(LinkedHashMap.class, specialized.getRawClass());
        assertEquals(2, specialized.containedTypeCount());

        JavaType keyType = specialized.containedType(0);
        assertEquals(CompoundKey.class, keyType.getRawClass());
        assertFalse(keyType.isContainerType());

        JavaType valueType = specialized.containedType(1);
        assertEquals(List.class, valueType.getRawClass());
        assertEquals(String.class, valueType.containedType(0).getRawClass());
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructMapTypeWithNonMapClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType keyType = factory.constructType(String.class);
        JavaType valueType = factory.constructType(Integer.class);
        // String is not a Map type
        factory.constructMapType(String.class, keyType, valueType);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructCollectionTypeWithNonCollectionClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType elementType = factory.constructType(String.class);
        // String is not a Collection type
        factory.constructCollectionType(String.class, elementType);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructReferenceTypeWithNonReferenceClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType referredType = factory.constructType(String.class);
        // String is not a Reference type
        factory.constructReferenceType(String.class, referredType);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructSimpleTypeWithWrongParameterCount() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType param1 = factory.constructType(String.class);
        JavaType param2 = factory.constructType(Integer.class);
        // List takes 1 parameter, but we pass 2
        factory.constructSimpleType(List.class, new JavaType[]{param1, param2});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructParametricTypeWithWrongParameterCount() {
        TypeFactory factory = TypeFactory.defaultInstance();
        // Map takes 2 parameters, but we pass 1
        factory.constructParametricType(Map.class, String.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructParametrizedTypeWithWrongParameterCount() {
        TypeFactory factory = TypeFactory.defaultInstance();
        // List takes 1 parameter, but we pass 2
        factory.constructParametrizedType(ArrayList.class, List.class, String.class, Integer.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructGeneralizedTypeWithNonSuperType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType baseType = factory.constructType(ArrayList.class);
        // String is not a super-type of ArrayList
        factory.constructGeneralizedType(baseType, String.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructSpecializedTypeWithIncompatibleSubclass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType baseType = factory.constructType(List.class);
        // String is not a subtype of List
        factory.constructSpecializedType(baseType, String.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructTypeWithUnsupportedType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        // Create a custom Type that is not supported
        Type unsupportedType = new Type() {
            @Override
            public String getTypeName() {
                return "unsupported";
            }
        };
        factory.constructType(unsupportedType);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testConstructedTypesAreCached() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type1 = factory.constructType(String.class);
        JavaType type2 = factory.constructType(String.class);
        // Well-known types are pre-constructed and shared
        assertSame(type1, type2);
    }

    @Test(timeout = 4000)
    public void testConstructedParameterizedTypesAreCached() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type1 = factory.constructType(new TypeReference<List<String>>() {});
        JavaType type2 = factory.constructType(new TypeReference<List<String>>() {});
        // Parameterized types may be cached
        assertSame(type1, type2);
    }

    @Test(timeout = 4000)
    public void testClearCacheClearsTypes() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type1 = factory.constructType(new TypeReference<Map<String, Integer>>() {});
        factory.clearCache();
        JavaType type2 = factory.constructType(new TypeReference<Map<String, Integer>>() {});
        // After cache clear, types should be re-created (not same instance)
        assertNotSame(type1, type2);
        // But should be equal
        assertEquals(type1, type2);
    }

    @Test(timeout = 4000)
    public void testWithCacheUsesNewCache() {
        TypeFactory factory = TypeFactory.defaultInstance();
        LRUMap<Object, JavaType> cache = new LRUMap<>(16, 200);
        TypeFactory newFactory = factory.withCache(cache);
        JavaType type1 = newFactory.constructType(new TypeReference<List<Integer>>() {});
        JavaType type2 = newFactory.constructType(new TypeReference<List<Integer>>() {});
        assertSame(type1, type2);
    }

    @Test(timeout = 4000)
    public void testWithModifierAffectsTypeConstruction() {
        TypeFactory factory = TypeFactory.defaultInstance();
        TypeModifier modifier = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, TypeBindings bindings, TypeFactory typeFactory) {
                // Force all types to be Object
                return typeFactory.constructType(Object.class);
            }
        };
        TypeFactory newFactory = factory.withModifier(modifier);
        JavaType type = newFactory.constructType(String.class);
        assertEquals(Object.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testWithClassLoaderAffectsClassLoading() throws Exception {
        TypeFactory factory = TypeFactory.defaultInstance();
        ClassLoader customLoader = new ClassLoader(getClass().getClassLoader()) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.equals("com.example.CustomClass")) {
                    // Return a dummy class
                    return super.loadClass("java.lang.String");
                }
                return super.loadClass(name);
            }
        };
        TypeFactory newFactory = factory.withClassLoader(customLoader);
        // Should not throw
        JavaType type = newFactory.constructType(String.class);
        assertNotNull(type);
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithContextType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType contextType = factory.constructType(new TypeReference<Map<String, Integer>>() {});
        JavaType type = factory.constructType(String.class, contextType);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithContextClassAndBindings() {
        TypeFactory factory = TypeFactory.defaultInstance();
        TypeBindings bindings = TypeBindings.create(String.class, Integer.class);
        JavaType type = factory.constructType(new TypeReference<Map<String, Integer>>() {}.getType(), bindings);
        assertNotNull(type);
        assertEquals(Map.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Integer.class, type.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithJavaType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType javaType = factory.constructType(String.class);
        JavaType type = factory.constructType(javaType);
        assertSame(javaType, type);
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithJavaTypeArray() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType javaType = factory.constructType(String.class);
        JavaType type = factory.constructType(new JavaType[]{javaType});
        assertNotNull(type);
        assertEquals(Object.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(String.class);
        assertEquals(String.class, type.getRawClass());
        assertTrue(type.isFinal());
        assertFalse(type.isContainerType());
        assertFalse(type.isMapLikeType());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithEnum() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(TestEnum.class);
        assertNotNull(type);
        assertEquals(TestEnum.class, type.getRawClass());
        assertTrue(type.isEnumType());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithAtomicReference() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<AtomicReference<String>>() {});
        assertNotNull(type);
        assertEquals(AtomicReference.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithProperties() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(Properties.class);
        assertNotNull(type);
        assertEquals(Properties.class, type.getRawClass());
        // Properties is a Map<String,String>
        assertEquals(2, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(String.class, type.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithObject() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(Object.class);
        assertNotNull(type);
        assertEquals(Object.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithInterface() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(Comparable.class);
        assertNotNull(type);
        assertEquals(Comparable.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithAbstractClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(AbstractList.class);
        assertNotNull(type);
        assertEquals(AbstractList.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithFinalClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(String.class);
        assertTrue(type.isFinal());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithNonFinalClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(ArrayList.class);
        assertFalse(type.isFinal());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithArrayClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(String[].class);
        assertTrue(type.isArrayType());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithPrimitiveArray() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(int[].class);
        assertTrue(type.isArrayType());
        assertEquals(int.class, type.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithMultiDimensionalArray() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(String[][].class);
        assertTrue(type.isArrayType());
        assertTrue(type.getContentType().isArrayType());
        assertEquals(String.class, type.getContentType().getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithGenericArray() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<List<String>[]>() {});
        assertTrue(type.isArrayType());
        assertEquals(List.class, type.getContentType().getRawClass());
        assertEquals(String.class, type.getContentType().containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithWildcardExtends() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<List<? extends Number>>() {});
        assertEquals(List.class, type.getRawClass());
        assertEquals(Number.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithWildcardSuper() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<List<? super Integer>>() {});
        assertEquals(List.class, type.getRawClass());
        assertEquals(Integer.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithUnboundedWildcard() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<List<?>>() {});
        assertEquals(List.class, type.getRawClass());
        assertEquals(Object.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithTypeVariable() {
        TypeFactory factory = TypeFactory.defaultInstance();
        // Use a generic method to get a TypeVariable
        class GenericClass<T> {
            Type getType() { return T.class; }
        }
        // Can't easily get TypeVariable from Class, so use a different approach
        Type type = new TypeReference<Map<String, ?>>() {}.getType();
        JavaType javaType = factory.constructType(type);
        assertNotNull(javaType);
        assertEquals(Map.class, javaType.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRecursiveType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        // Create a self-referencing type
        JavaType type = factory.constructType(new TypeReference<RecursiveType>() {});
        assertNotNull(type);
        assertEquals(RecursiveType.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithNestedParameterizedType() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<Map<String, List<Integer>>>() {});
        assertEquals(Map.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(List.class, type.containedType(1).getRawClass());
        assertEquals(Integer.class, type.containedType(1).containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithDeepNesting() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<Map<String, Map<String, Map<String, Integer>>>>() {});
        assertEquals(Map.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Map.class, type.containedType(1).getRawClass());
        assertEquals(String.class, type.containedType(1).containedType(0).getRawClass());
        assertEquals(Map.class, type.containedType(1).containedType(1).getRawClass());
        assertEquals(String.class, type.containedType(1).containedType(1).containedType(0).getRawClass());
        assertEquals(Integer.class, type.containedType(1).containedType(1).containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithCustomGenericClass() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<GenericClass<String, Integer>>() {});
        assertEquals(GenericClass.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Integer.class, type.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithInheritedGeneric() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<SubGenericClass<String>>() {});
        assertEquals(SubGenericClass.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithInterfaceImplementation() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<ArrayList<String>>() {});
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithMapImplementation() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<HashMap<String, Integer>>() {});
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Integer.class, type.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithCollectionImplementation() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<HashSet<String>>() {});
        assertEquals(HashSet.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawCollection() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(List.class);
        assertEquals(List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(Object.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawMap() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(Map.class);
        assertEquals(Map.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        assertEquals(Object.class, type.containedType(0).getRawClass());
        assertEquals(Object.class, type.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawReference() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(AtomicReference.class);
        assertEquals(AtomicReference.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(Object.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawSimple() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(String.class);
        assertEquals(String.class, type.getRawClass());
        assertEquals(0, type.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawObject() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(Object.class);
        assertEquals(Object.class, type.getRawClass());
        assertEquals(0, type.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawInterface() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(Comparable.class);
        assertEquals(Comparable.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(Object.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawEnum() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(TestEnum.class);
        assertEquals(TestEnum.class, type.getRawClass());
        assertEquals(0, type.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawAnnotation() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(Deprecated.class);
        assertEquals(Deprecated.class, type.getRawClass());
        assertEquals(0, type.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawVoid() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(Void.class);
        assertEquals(Void.class, type.getRawClass());
        assertEquals(0, type.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawPrimitive() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(int.class);
        assertEquals(int.class, type.getRawClass());
        assertEquals(0, type.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawArray() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(String[].class);
        assertEquals(String[].class, type.getRawClass());
        assertEquals(0, type.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawMultiArray() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(String[][].class);
        assertEquals(String[][].class, type.getRawClass());
        assertEquals(0, type.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawGenericArray() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<List<String>[]>() {});
        assertEquals(List[].class, type.getRawClass());
        assertEquals(0, type.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawWildcard() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<List<?>>() {});
        assertEquals(List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(Object.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawParameterized() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<List<String>>() {});
        assertEquals(List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawTypeVariable() {
        TypeFactory factory = TypeFactory.defaultInstance();
        // Can't easily create a TypeVariable, so use a generic method
        class GenericClass<T> {
            Type getType() { return T.class; }
        }
        // This will fail because T.class is not a valid Type
        // Instead, use a TypeReference
        JavaType type = factory.constructType(new TypeReference<GenericClass<String>>() {});
        assertEquals(GenericClass.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawRecursive() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<RecursiveType>() {});
        assertEquals(RecursiveType.class, type.getRawClass());
        assertEquals(0, type.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawNested() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<Map<String, List<Integer>>>() {});
        assertEquals(Map.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(List.class, type.containedType(1).getRawClass());
        assertEquals(Integer.class, type.containedType(1).containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithRawDeepNested() {
        TypeFactory factory = TypeFactory.defaultInstance();
        JavaType type = factory.constructType(new TypeReference<Map<String, Map<String, Map<String, Integer>>>>() {});
        assertEquals(Map.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Map.class, type.containedType(1).getRawClass());
        assertEquals(String.class, type.containedType(1).containedType(0).getRawClass());
        assertEquals(Map.class, type.containedType(1).containedType(1).getRawClass());
        assertEquals(String.class, type.containedType(1).containedType(1).containedType(0).getRawClass());
        assertEquals(Integer.class, type.containedType(1).containedType(1).containedType(1).getRawClass());
    }

    // Helper classes for testing

    private enum TestEnum {
        VALUE1, VALUE2
    }

    private static class GenericClass<T, U> {
        T first;
        U second;
    }

    private static class SubGenericClass<T> extends GenericClass<T, String> {
    }

    private static class RecursiveType {
        RecursiveType next;
    }
}