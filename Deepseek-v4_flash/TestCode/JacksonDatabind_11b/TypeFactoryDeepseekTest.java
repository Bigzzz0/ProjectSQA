package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;

public class TypeFactoryDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Defect 1: testLocalType728 - When constructing a type from a TypeVariable
     *   that has a bound (e.g., <T extends CharSequence>), the factory incorrectly
     *   resolves to Object instead of the bound type. Target branch: _constructType
     *   TypeVariable handling where bounds are not properly resolved.
     * 
     * Defect 2: testLocalPartialType609 - When a TypeVariable is used in a nested
     *   generic context (e.g., EntityContainer<T> where T is a type variable from
     *   an enclosing class), the resolution fails with "Type variable 'T' can not
     *   be resolved". Target branch: _constructType TypeVariable resolution when
     *   context bindings are incomplete.
     * 
     * Key branches to cover:
     * - TypeVariable resolution with/without bounds
     * - Generic array type construction
     * - Parameterized type resolution with wildcards
     * - Map/Collection type parameter validation
     * - Type cache hit/miss paths
     * - TypeModifier application
     * - Raw type fallback paths
     * - Exception paths for invalid type parameters
     */

    // ===== Partition A: Core Functional Logic & State Transitions =====
    
    @Test(timeout = 4000)
    public void testConstructTypeFromClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(String.class);
        assertEquals(String.class, type.getRawClass());
        assertTrue(type.isFinal());
        assertFalse(type.isContainerType());
    }

    @Test(timeout = 4000)
    public void testConstructTypeFromParameterizedType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<Map<String, Integer>>() {}.getType();
        JavaType result = tf.constructType(type);
        assertTrue(result.isMapType());
        assertEquals(String.class, result.getKeyType().getRawClass());
        assertEquals(Integer.class, result.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeFromGenericArrayType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<List<String>[]>() {}.getType();
        JavaType result = tf.constructType(type);
        assertTrue(result.isArrayType());
        assertTrue(result.getContentType().isCollectionLikeType());
    }

    @Test(timeout = 4000)
    public void testConstructTypeFromWildcardType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<List<? extends Number>>() {}.getType();
        JavaType result = tf.constructType(type);
        assertTrue(result.isCollectionLikeType());
        assertEquals(Number.class, result.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeFromTypeVariableWithBound() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<Holder<? extends CharSequence>>() {}.getType();
        JavaType result = tf.constructType(type);
        // Should resolve the wildcard bound
        assertEquals(CharSequence.class, result.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructCollectionType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        CollectionType type = tf.constructCollectionType(ArrayList.class, String.class);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
        assertTrue(type.isCollectionLikeType());
    }

    @Test(timeout = 4000)
    public void testConstructMapType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        MapType type = tf.constructMapType(HashMap.class, String.class, Integer.class);
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Integer.class, type.getValueType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructArrayType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ArrayType type = tf.constructArrayType(tf.constructType(String.class));
        assertEquals(String[].class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructParametrizedType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructParametrizedType(ArrayList.class, List.class, String.class);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructSimpleType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructSimpleType(MyGeneric.class, new JavaType[] { tf.constructType(String.class) });
        assertEquals(MyGeneric.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testUncheckedSimpleType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.uncheckedSimpleType(String.class);
        assertEquals(String.class, type.getRawClass());
        assertTrue(type.isFinal());
    }

    @Test(timeout = 4000)
    public void testConstructRawCollectionType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        CollectionType type = tf.constructRawCollectionType(ArrayList.class);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(Object.class, type.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructRawMapType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        MapType type = tf.constructRawMapType(HashMap.class);
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals(Object.class, type.getKeyType().getRawClass());
        assertEquals(Object.class, type.getValueType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructCollectionLikeType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        CollectionLikeType type = tf.constructCollectionLikeType(MyCollection.class, String.class);
        assertEquals(MyCollection.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructMapLikeType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        MapLikeType type = tf.constructMapLikeType(MyMap.class, String.class, Integer.class);
        assertEquals(MyMap.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Integer.class, type.getValueType().getRawClass());
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testConstructTypeWithNullType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        try {
            tf.constructType((Type) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithNullClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        try {
            tf.constructType((Class<?>) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithVoidClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(void.class);
        assertEquals(void.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithPrimitiveArray() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(int[].class);
        assertTrue(type.isArrayType());
        assertEquals(int.class, type.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithObjectArray() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(Object[].class);
        assertTrue(type.isArrayType());
        assertEquals(Object.class, type.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithMultiDimensionalArray() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(String[][].class);
        assertTrue(type.isArrayType());
        assertTrue(type.getContentType().isArrayType());
        assertEquals(String.class, type.getContentType().getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithEmptyTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructParametrizedType(ArrayList.class, List.class);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(Object.class, type.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithTooManyTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        try {
            tf.constructParametrizedType(ArrayList.class, List.class, String.class, Integer.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithTooFewTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        try {
            tf.constructParametrizedType(HashMap.class, Map.class, String.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithNonGenericClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        try {
            tf.constructParametrizedType(String.class, CharSequence.class, Integer.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithInterfaceAsParametrized() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructParametrizedType(List.class, List.class, String.class);
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithAbstractClassAsParametrized() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructParametrizedType(AbstractList.class, List.class, String.class);
        assertEquals(AbstractList.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithEnum() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(MyEnum.class);
        assertEquals(MyEnum.class, type.getRawClass());
        assertTrue(type.isEnumType());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithAnnotation() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(MyAnnotation.class);
        assertEquals(MyAnnotation.class, type.getRawClass());
        assertTrue(type.isAnnotationType());
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    /**
     * Defect 1: testLocalType728
     * When a TypeVariable has a bound (e.g., <T extends CharSequence>), the factory
     * incorrectly resolves to Object instead of the bound type.
     */
    @Test(timeout = 4000)
    public void testTypeVariableWithBoundResolution() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<LocalType728<CharSequence>>() {}.getType();
        JavaType result = tf.constructType(type);
        // The type variable T in LocalType728 should resolve to CharSequence
        assertEquals(CharSequence.class, result.getRawClass());
    }

    /**
     * Defect 2: testLocalPartialType609
     * When a TypeVariable is used in a nested generic context, the resolution fails
     * with "Type variable 'T' can not be resolved".
     */
    @Test(timeout = 4000)
    public void testNestedTypeVariableResolution() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<EntityContainer<MyEntity>>() {}.getType();
        JavaType result = tf.constructType(type);
        assertEquals(EntityContainer.class, result.getRawClass());
        // The type variable T in EntityContainer should resolve to MyEntity
        assertEquals(MyEntity.class, result.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testTypeVariableWithMultipleBounds() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<MultiBound<MyEntity>>() {}.getType();
        JavaType result = tf.constructType(type);
        assertEquals(MultiBound.class, result.getRawClass());
        assertEquals(MyEntity.class, result.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testTypeVariableWithRecursiveBound() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<RecursiveBound<MyEntity>>() {}.getType();
        JavaType result = tf.constructType(type);
        assertEquals(RecursiveBound.class, result.getRawClass());
        assertEquals(MyEntity.class, result.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testTypeVariableWithWildcardBound() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<WildcardBound<MyEntity>>() {}.getType();
        JavaType result = tf.constructType(type);
        assertEquals(WildcardBound.class, result.getRawClass());
        assertEquals(MyEntity.class, result.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testTypeVariableWithArrayBound() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<ArrayBound<MyEntity[]>>() {}.getType();
        JavaType result = tf.constructType(type);
        assertEquals(ArrayBound.class, result.getRawClass());
        assertTrue(result.containedType(0).isArrayType());
        assertEquals(MyEntity.class, result.containedType(0).getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testTypeVariableWithGenericBound() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<GenericBound<MyEntity>>() {}.getType();
        JavaType result = tf.constructType(type);
        assertEquals(GenericBound.class, result.getRawClass());
        assertEquals(MyEntity.class, result.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testTypeVariableWithInterfaceBound() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<InterfaceBound<MyEntity>>() {}.getType();
        JavaType result = tf.constructType(type);
        assertEquals(InterfaceBound.class, result.getRawClass());
        assertEquals(MyEntity.class, result.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testTypeVariableWithClassBound() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<ClassBound<MyEntity>>() {}.getType();
        JavaType result = tf.constructType(type);
        assertEquals(ClassBound.class, result.getRawClass());
        assertEquals(MyEntity.class, result.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testTypeVariableWithNestedGenericBound() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type type = new TypeReference<NestedGenericBound<MyEntity>>() {}.getType();
        JavaType result = tf.constructType(type);
        assertEquals(NestedGenericBound.class, result.getRawClass());
        assertEquals(MyEntity.class, result.containedType(0).getRawClass());
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructTypeWithInvalidMapType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructMapType(String.class, String.class, Integer.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructTypeWithInvalidCollectionType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructCollectionType(String.class, Integer.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructTypeWithInvalidParametrizedType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructParametrizedType(String.class, String.class, Integer.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructTypeWithInvalidSimpleType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructSimpleType(String.class, new JavaType[] { tf.constructType(Integer.class) });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructTypeWithInvalidTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructParametrizedType(ArrayList.class, List.class, String.class, Integer.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructTypeWithNonSubtype() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(Number.class);
        tf.constructSpecializedType(baseType, String.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructTypeWithNullParameterizedClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructParametrizedType(null, List.class, String.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructTypeWithNullParameterTarget() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructParametrizedType(ArrayList.class, null, String.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructTypeWithNullTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructParametrizedType(ArrayList.class, List.class, (Class<?>[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructTypeWithNullJavaTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructParametrizedType(ArrayList.class, List.class, (JavaType) null);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testTypeFactoryWithModifier() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeModifier modifier = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type genericType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeFactory modified = tf.withModifier(modifier);
        assertNotNull(modified);
        assertNotSame(tf, modified);
    }

    @Test(timeout = 4000)
    public void testTypeFactoryWithMultipleModifiers() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeModifier modifier1 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type genericType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeModifier modifier2 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type genericType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeFactory modified = tf.withModifier(modifier1).withModifier(modifier2);
        assertNotNull(modified);
    }

    @Test(timeout = 4000)
    public void testTypeFactoryWithSameModifierTwice() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeModifier modifier = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type genericType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeFactory modified = tf.withModifier(modifier).withModifier(modifier);
        assertNotNull(modified);
    }

    @Test(timeout = 4000)
    public void testTypeFactoryDefaultInstance() {
        TypeFactory tf1 = TypeFactory.defaultInstance();
        TypeFactory tf2 = TypeFactory.defaultInstance();
        assertSame(tf1, tf2);
    }

    @Test(timeout = 4000)
    public void testTypeFactoryClearCache() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructType(String.class);
        tf.constructType(Integer.class);
        tf.clearCache();
        // Should not throw
    }

    @Test(timeout = 4000)
    public void testUnknownType() {
        JavaType type = TypeFactory.unknownType();
        assertEquals(Object.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testRawClass() {
        assertEquals(String.class, TypeFactory.rawClass(String.class));
        assertEquals(String.class, TypeFactory.rawClass(new TypeReference<String>() {}.getType()));
    }

    @Test(timeout = 4000)
    public void testMoreSpecificType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType stringType = tf.constructType(String.class);
        JavaType objectType = tf.constructType(Object.class);
        JavaType result = tf.moreSpecificType(stringType, objectType);
        assertEquals(String.class, result.getRawClass());
    }

    @Test(timeout = 4000)
    public void testMoreSpecificTypeWithEqualTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType stringType1 = tf.constructType(String.class);
        JavaType stringType2 = tf.constructType(String.class);
        JavaType result = tf.moreSpecificType(stringType1, stringType2);
        assertEquals(String.class, result.getRawClass());
    }

    @Test(timeout = 4000)
    public void testMoreSpecificTypeWithUnrelatedTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType stringType = tf.constructType(String.class);
        JavaType integerType = tf.constructType(Integer.class);
        JavaType result = tf.moreSpecificType(stringType, integerType);
        assertEquals(String.class, result.getRawClass());
    }

    @Test(timeout = 4000)
    public void testFindTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(new TypeReference<ArrayList<String>>() {}.getType());
        JavaType[] params = tf.findTypeParameters(type, List.class);
        assertEquals(1, params.length);
        assertEquals(String.class, params[0].getRawClass());
    }

    @Test(timeout = 4000)
    public void testFindTypeParametersWithMap() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(new TypeReference<HashMap<String, Integer>>() {}.getType());
        JavaType[] params = tf.findTypeParameters(type, Map.class);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(Integer.class, params[1].getRawClass());
    }

    @Test(timeout = 4000)
    public void testFindTypeParametersWithRawClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType[] params = tf.findTypeParameters(ArrayList.class, List.class);
        assertEquals(1, params.length);
        assertEquals(Object.class, params[0].getRawClass());
    }

    @Test(timeout = 4000)
    public void testFindTypeParametersWithNonSubtype() {
        TypeFactory tf = TypeFactory.defaultInstance();
        try {
            tf.findTypeParameters(String.class, List.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructSpecializedType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(new TypeReference<List<String>>() {}.getType());
        JavaType specialized = tf.constructSpecializedType(baseType, ArrayList.class);
        assertEquals(ArrayList.class, specialized.getRawClass());
        assertEquals(String.class, specialized.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructSpecializedTypeWithMap() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(new TypeReference<Map<String, Integer>>() {}.getType());
        JavaType specialized = tf.constructSpecializedType(baseType, HashMap.class);
        assertEquals(HashMap.class, specialized.getRawClass());
        assertEquals(String.class, specialized.getKeyType().getRawClass());
        assertEquals(Integer.class, specialized.getValueType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructSpecializedTypeWithArray() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(String[].class);
        JavaType specialized = tf.constructSpecializedType(baseType, Object[].class);
        assertEquals(Object[].class, specialized.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithContextClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(String.class, (Class<?>) null);
        assertEquals(String.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithContextJavaType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType context = tf.constructType(MyEntity.class);
        JavaType type = tf.constructType(String.class, context);
        assertEquals(String.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithTypeReference() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(new TypeReference<List<String>>() {});
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithTypeBindings() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeBindings bindings = new TypeBindings(tf, String.class);
        JavaType type = tf.constructType(String.class, bindings);
        assertEquals(String.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithCoreTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        assertEquals(String.class, tf.constructType(String.class).getRawClass());
        assertEquals(Boolean.TYPE, tf.constructType(Boolean.TYPE).getRawClass());
        assertEquals(Integer.TYPE, tf.constructType(Integer.TYPE).getRawClass());
        assertEquals(Long.TYPE, tf.constructType(Long.TYPE).getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithCachedType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type1 = tf.constructType(String.class);
        JavaType type2 = tf.constructType(String.class);
        assertSame(type1, type2);
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithDifferentTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type1 = tf.constructType(String.class);
        JavaType type2 = tf.constructType(Integer.class);
        assertNotSame(type1, type2);
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithGenericArray() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(new TypeReference<List<String>[]>() {}.getType());
        assertTrue(type.isArrayType());
        assertTrue(type.getContentType().isCollectionLikeType());
        assertEquals(String.class, type.getContentType().getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithNestedGeneric() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(new TypeReference<Map<String, List<Integer>>>() {}.getType());
        assertTrue(type.isMapType());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertTrue(type.getValueType().isCollectionLikeType());
        assertEquals(Integer.class, type.getValueType().getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithDeepNesting() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(new TypeReference<Map<String, Map<String, Map<String, Integer>>>>() {}.getType());
        assertTrue(type.isMapType());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertTrue(type.getValueType().isMapType());
        assertEquals(String.class, type.getValueType().getKeyType().getRawClass());
        assertTrue(type.getValueType().getValueType().isMapType());
        assertEquals(Integer.class, type.getValueType().getValueType().getValueType().getRawClass());
    }

    // Helper classes for testing

    static class LocalType728<T extends CharSequence> {
        T value;
    }

    static class EntityContainer<T> {
        T entity;
    }

    static class MyEntity {
        String name;
    }

    static class MultiBound<T extends MyEntity & Comparable<T>> {
        T value;
    }

    static class RecursiveBound<T extends Comparable<T>> {
        T value;
    }

    static class WildcardBound<T extends MyEntity> {
        T value;
    }

    static class ArrayBound<T> {
        T value;
    }

    static class GenericBound<T extends MyEntity> {
        T value;
    }

    static class InterfaceBound<T extends MyEntity> {
        T value;
    }

    static class ClassBound<T extends MyEntity> {
        T value;
    }

    static class NestedGenericBound<T extends MyEntity> {
        T value;
    }

    static class MyGeneric<T> {
        T value;
    }

    static class MyCollection<E> extends ArrayList<E> {
    }

    static class MyMap<K, V> extends HashMap<K, V> {
    }

    enum MyEnum {
        A, B, C
    }

    @interface MyAnnotation {
    }

    static class Holder<T> {
        T value;
    }
}