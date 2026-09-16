package com.google.gson.internal;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: $Gson$Types - static utility class for type operations
 * 
 * Key branches and boundaries covered:
 * 
 * Partition A (Core Functional Logic):
 * - newParameterizedTypeWithOwner: null/valid owner, static/non-static raw types
 * - arrayOf: null/valid component types
 * - subtypeOf/supertypeOf: wildcard inputs, Object bounds, null bounds
 * - canonicalize: Class, ParameterizedType, GenericArrayType, WildcardType, unsupported
 * - getRawType: Class, ParameterizedType, GenericArrayType, TypeVariable, WildcardType, invalid
 * - equals: identity, Class, ParameterizedType, GenericArrayType, WildcardType, TypeVariable, mismatches
 * - getGenericSupertype: interface/class hierarchy, direct match, Object
 * - getSupertype: valid/invalid supertype
 * - getArrayComponentType: GenericArrayType vs Class array
 * - getCollectionElementType: ParameterizedType, WildcardType, non-collection
 * - getMapKeyAndValueTypes: Properties, ParameterizedType, non-map
 * - resolve: TypeVariable, Class array, GenericArrayType, ParameterizedType, WildcardType
 * - resolveTypeVariable: declared by class/parameterized type, not found
 * - indexOf: found/not found
 * - declaringClassOf: class vs non-class declaration
 * - checkNotPrimitive: primitive vs non-primitive
 * 
 * Partition B (BVA & Extremes):
 * - null types, empty type arrays, Object.class bounds
 * - hashCodeOrZero: null vs non-null
 * - typeToString: Class vs non-Class
 * - WildcardTypeImpl: lowerBounds length 0/1, upperBounds length 1, null bounds
 * - ParameterizedTypeImpl: null typeArguments, empty typeArguments
 * 
 * Partition C (Defect-Targeted - testIssue1107):
 * - The defect: ClassCastException when deserializing a collection of SmallClass
 *   from LinkedTreeMap. This occurs due to incorrect type resolution in
 *   getCollectionElementType or resolve when handling generic collections.
 *   The test creates a generic collection type and verifies the resolved
 *   element type is correct, exposing the bug where the type variable
 *   resolution fails and returns Object instead of the actual type.
 * 
 * Partition D (Exception & Defensive Guards):
 * - IllegalArgumentException for invalid raw types in getRawType
 * - IllegalArgumentException for invalid supertype in getSupertype
 * - IllegalArgumentException for invalid WildcardTypeImpl bounds
 * - NullPointerException for null type arguments
 * - UnsupportedOperationException for private constructor
 * 
 * Partition E (Lifecycle & Contract):
 * - equals/hashCode for ParameterizedTypeImpl, GenericArrayTypeImpl, WildcardTypeImpl
 * - toString for ParameterizedTypeImpl
 * - Serialization contract (Serializable implementations)
 */
public class $Gson$TypesDeepseekTest {

    /* ==================== Partition A: Core Functional Logic ==================== */

    @Test(timeout = 4000)
    public void testNewParameterizedTypeWithOwner_ValidOwner() {
        ParameterizedType type = $Gson$Types.newParameterizedTypeWithOwner(
                Map.class, HashMap.class, String.class, Integer.class);
        assertNotNull(type);
        assertEquals(Map.class, type.getOwnerType());
        assertEquals(HashMap.class, type.getRawType());
        assertArrayEquals(new Type[]{String.class, Integer.class}, type.getActualTypeArguments());
    }

    @Test(timeout = 4000)
    public void testNewParameterizedTypeWithOwner_NullOwnerStaticClass() {
        ParameterizedType type = $Gson$Types.newParameterizedTypeWithOwner(
                null, ArrayList.class, String.class);
        assertNotNull(type);
        assertNull(type.getOwnerType());
        assertEquals(ArrayList.class, type.getRawType());
        assertArrayEquals(new Type[]{String.class}, type.getActualTypeArguments());
    }

    @Test(timeout = 4000)
    public void testArrayOf_ValidComponent() {
        GenericArrayType arrayType = $Gson$Types.arrayOf(String.class);
        assertNotNull(arrayType);
        assertEquals(String.class, arrayType.getGenericComponentType());
    }

    @Test(timeout = 4000)
    public void testArrayOf_GenericComponent() {
        ParameterizedType listType = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        GenericArrayType arrayType = $Gson$Types.arrayOf(listType);
        assertNotNull(arrayType);
        assertEquals(listType, arrayType.getGenericComponentType());
    }

    @Test(timeout = 4000)
    public void testSubtypeOf_ClassBound() {
        WildcardType wildcard = $Gson$Types.subtypeOf(CharSequence.class);
        assertNotNull(wildcard);
        assertArrayEquals(new Type[]{CharSequence.class}, wildcard.getUpperBounds());
        assertArrayEquals(new Type[]{}, wildcard.getLowerBounds());
    }

    @Test(timeout = 4000)
    public void testSubtypeOf_ObjectBound() {
        WildcardType wildcard = $Gson$Types.subtypeOf(Object.class);
        assertNotNull(wildcard);
        assertArrayEquals(new Type[]{Object.class}, wildcard.getUpperBounds());
        assertArrayEquals(new Type[]{}, wildcard.getLowerBounds());
    }

    @Test(timeout = 4000)
    public void testSubtypeOf_WildcardBound() {
        WildcardType original = $Gson$Types.subtypeOf(Number.class);
        WildcardType wildcard = $Gson$Types.subtypeOf(original);
        assertNotNull(wildcard);
        assertArrayEquals(new Type[]{Number.class}, wildcard.getUpperBounds());
        assertArrayEquals(new Type[]{}, wildcard.getLowerBounds());
    }

    @Test(timeout = 4000)
    public void testSupertypeOf_ClassBound() {
        WildcardType wildcard = $Gson$Types.supertypeOf(String.class);
        assertNotNull(wildcard);
        assertArrayEquals(new Type[]{Object.class}, wildcard.getUpperBounds());
        assertArrayEquals(new Type[]{String.class}, wildcard.getLowerBounds());
    }

    @Test(timeout = 4000)
    public void testSupertypeOf_WildcardBound() {
        WildcardType original = $Gson$Types.supertypeOf(Number.class);
        WildcardType wildcard = $Gson$Types.supertypeOf(original);
        assertNotNull(wildcard);
        assertArrayEquals(new Type[]{Object.class}, wildcard.getUpperBounds());
        assertArrayEquals(new Type[]{Number.class}, wildcard.getLowerBounds());
    }

    @Test(timeout = 4000)
    public void testCanonicalize_Class() {
        Type canonical = $Gson$Types.canonicalize(String.class);
        assertEquals(String.class, canonical);
    }

    @Test(timeout = 4000)
    public void testCanonicalize_ParameterizedType() {
        ParameterizedType original = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        Type canonical = $Gson$Types.canonicalize(original);
        assertTrue(canonical instanceof ParameterizedType);
        ParameterizedType paramType = (ParameterizedType) canonical;
        assertEquals(List.class, paramType.getRawType());
        assertArrayEquals(new Type[]{String.class}, paramType.getActualTypeArguments());
    }

    @Test(timeout = 4000)
    public void testCanonicalize_GenericArrayType() {
        GenericArrayType original = $Gson$Types.arrayOf(String.class);
        Type canonical = $Gson$Types.canonicalize(original);
        assertTrue(canonical instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) canonical).getGenericComponentType());
    }

    @Test(timeout = 4000)
    public void testCanonicalize_WildcardType() {
        WildcardType original = $Gson$Types.subtypeOf(Number.class);
        Type canonical = $Gson$Types.canonicalize(original);
        assertTrue(canonical instanceof WildcardType);
        assertArrayEquals(new Type[]{Number.class}, ((WildcardType) canonical).getUpperBounds());
    }

    @Test(timeout = 4000)
    public void testGetRawType_Class() {
        assertEquals(String.class, $Gson$Types.getRawType(String.class));
    }

    @Test(timeout = 4000)
    public void testGetRawType_ParameterizedType() {
        ParameterizedType type = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        assertEquals(List.class, $Gson$Types.getRawType(type));
    }

    @Test(timeout = 4000)
    public void testGetRawType_GenericArrayType() {
        GenericArrayType arrayType = $Gson$Types.arrayOf(String.class);
        assertEquals(String[].class, $Gson$Types.getRawType(arrayType));
    }

    @Test(timeout = 4000)
    public void testGetRawType_TypeVariable() {
        TypeVariable<?> tv = new TypeVariable<Class<?>>() {
            @Override public Type[] getBounds() { return new Type[]{Object.class}; }
            @Override public GenericDeclaration getGenericDeclaration() { return String.class; }
            @Override public String getName() { return "T"; }
            @Override public String getTypeName() { return "T"; }
        };
        assertEquals(Object.class, $Gson$Types.getRawType(tv));
    }

    @Test(timeout = 4000)
    public void testGetRawType_WildcardType() {
        WildcardType wildcard = $Gson$Types.subtypeOf(Number.class);
        assertEquals(Number.class, $Gson$Types.getRawType(wildcard));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetRawType_InvalidType() {
        $Gson$Types.getRawType(new Type() {});
    }

    @Test(timeout = 4000)
    public void testEquals_Identity() {
        Type type = String.class;
        assertTrue($Gson$Types.equals(type, type));
    }

    @Test(timeout = 4000)
    public void testEquals_Class() {
        assertTrue($Gson$Types.equals(String.class, String.class));
        assertFalse($Gson$Types.equals(String.class, Integer.class));
    }

    @Test(timeout = 4000)
    public void testEquals_ParameterizedType() {
        ParameterizedType type1 = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        ParameterizedType type2 = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        ParameterizedType type3 = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, Integer.class);
        assertTrue($Gson$Types.equals(type1, type2));
        assertFalse($Gson$Types.equals(type1, type3));
        assertFalse($Gson$Types.equals(type1, String.class));
    }

    @Test(timeout = 4000)
    public void testEquals_GenericArrayType() {
        GenericArrayType type1 = $Gson$Types.arrayOf(String.class);
        GenericArrayType type2 = $Gson$Types.arrayOf(String.class);
        GenericArrayType type3 = $Gson$Types.arrayOf(Integer.class);
        assertTrue($Gson$Types.equals(type1, type2));
        assertFalse($Gson$Types.equals(type1, type3));
        assertFalse($Gson$Types.equals(type1, String.class));
    }

    @Test(timeout = 4000)
    public void testEquals_WildcardType() {
        WildcardType type1 = $Gson$Types.subtypeOf(Number.class);
        WildcardType type2 = $Gson$Types.subtypeOf(Number.class);
        WildcardType type3 = $Gson$Types.subtypeOf(Integer.class);
        assertTrue($Gson$Types.equals(type1, type2));
        assertFalse($Gson$Types.equals(type1, type3));
        assertFalse($Gson$Types.equals(type1, String.class));
    }

    @Test(timeout = 4000)
    public void testEquals_TypeVariable() {
        TypeVariable<?> tv1 = new TypeVariable<Class<?>>() {
            @Override public Type[] getBounds() { return new Type[]{Object.class}; }
            @Override public GenericDeclaration getGenericDeclaration() { return String.class; }
            @Override public String getName() { return "T"; }
            @Override public String getTypeName() { return "T"; }
        };
        TypeVariable<?> tv2 = new TypeVariable<Class<?>>() {
            @Override public Type[] getBounds() { return new Type[]{Object.class}; }
            @Override public GenericDeclaration getGenericDeclaration() { return String.class; }
            @Override public String getName() { return "T"; }
            @Override public String getTypeName() { return "T"; }
        };
        TypeVariable<?> tv3 = new TypeVariable<Class<?>>() {
            @Override public Type[] getBounds() { return new Type[]{Object.class}; }
            @Override public GenericDeclaration getGenericDeclaration() { return String.class; }
            @Override public String getName() { return "U"; }
            @Override public String getTypeName() { return "U"; }
        };
        assertTrue($Gson$Types.equals(tv1, tv2));
        assertFalse($Gson$Types.equals(tv1, tv3));
        assertFalse($Gson$Types.equals(tv1, String.class));
    }

    @Test(timeout = 4000)
    public void testGetGenericSupertype_DirectMatch() {
        Type result = $Gson$Types.getGenericSupertype(
                ArrayList.class, ArrayList.class, ArrayList.class);
        assertEquals(ArrayList.class, result);
    }

    @Test(timeout = 4000)
    public void testGetGenericSupertype_Interface() {
        Type result = $Gson$Types.getGenericSupertype(
                ArrayList.class, ArrayList.class, List.class);
        assertTrue(result instanceof ParameterizedType);
        assertEquals(List.class, ((ParameterizedType) result).getRawType());
    }

    @Test(timeout = 4000)
    public void testGetGenericSupertype_ClassHierarchy() {
        Type result = $Gson$Types.getGenericSupertype(
                ArrayList.class, ArrayList.class, AbstractCollection.class);
        assertTrue(result instanceof ParameterizedType);
        assertEquals(AbstractCollection.class, ((ParameterizedType) result).getRawType());
    }

    @Test(timeout = 4000)
    public void testGetGenericSupertype_Object() {
        Type result = $Gson$Types.getGenericSupertype(
                ArrayList.class, ArrayList.class, Object.class);
        assertEquals(Object.class, result);
    }

    @Test(timeout = 4000)
    public void testGetSupertype_Valid() {
        Type result = $Gson$Types.getSupertype(
                ArrayList.class, ArrayList.class, List.class);
        assertTrue(result instanceof ParameterizedType);
        assertEquals(List.class, ((ParameterizedType) result).getRawType());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetSupertype_Invalid() {
        $Gson$Types.getSupertype(ArrayList.class, ArrayList.class, String.class);
    }

    @Test(timeout = 4000)
    public void testGetArrayComponentType_GenericArray() {
        GenericArrayType arrayType = $Gson$Types.arrayOf(String.class);
        assertEquals(String.class, $Gson$Types.getArrayComponentType(arrayType));
    }

    @Test(timeout = 4000)
    public void testGetArrayComponentType_ClassArray() {
        assertEquals(String.class, $Gson$Types.getArrayComponentType(String[].class));
    }

    @Test(timeout = 4000)
    public void testGetCollectionElementType_ParameterizedType() {
        ParameterizedType listType = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        assertEquals(String.class, $Gson$Types.getCollectionElementType(listType, List.class));
    }

    @Test(timeout = 4000)
    public void testGetCollectionElementType_WildcardType() {
        WildcardType wildcard = $Gson$Types.subtypeOf(String.class);
        ParameterizedType listType = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, wildcard);
        assertEquals(String.class, $Gson$Types.getCollectionElementType(listType, List.class));
    }

    @Test(timeout = 4000)
    public void testGetCollectionElementType_NonCollection() {
        assertEquals(Object.class, $Gson$Types.getCollectionElementType(String.class, String.class));
    }

    @Test(timeout = 4000)
    public void testGetMapKeyAndValueTypes_Properties() {
        Type[] result = $Gson$Types.getMapKeyAndValueTypes(Properties.class, Properties.class);
        assertArrayEquals(new Type[]{String.class, String.class}, result);
    }

    @Test(timeout = 4000)
    public void testGetMapKeyAndValueTypes_ParameterizedType() {
        ParameterizedType mapType = $Gson$Types.newParameterizedTypeWithOwner(
                null, Map.class, String.class, Integer.class);
        Type[] result = $Gson$Types.getMapKeyAndValueTypes(mapType, Map.class);
        assertArrayEquals(new Type[]{String.class, Integer.class}, result);
    }

    @Test(timeout = 4000)
    public void testGetMapKeyAndValueTypes_NonMap() {
        Type[] result = $Gson$Types.getMapKeyAndValueTypes(String.class, String.class);
        assertArrayEquals(new Type[]{Object.class, Object.class}, result);
    }

    @Test(timeout = 4000)
    public void testResolve_TypeVariable() {
        TypeVariable<?> tv = new TypeVariable<Class<?>>() {
            @Override public Type[] getBounds() { return new Type[]{Object.class}; }
            @Override public GenericDeclaration getGenericDeclaration() { return String.class; }
            @Override public String getName() { return "T"; }
            @Override public String getTypeName() { return "T"; }
        };
        Type result = $Gson$Types.resolve(String.class, String.class, tv);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testResolve_ClassArray() {
        Type result = $Gson$Types.resolve(String.class, String.class, String[].class);
        assertEquals(String[].class, result);
    }

    @Test(timeout = 4000)
    public void testResolve_GenericArrayType() {
        GenericArrayType arrayType = $Gson$Types.arrayOf(String.class);
        Type result = $Gson$Types.resolve(String.class, String.class, arrayType);
        assertTrue(result instanceof GenericArrayType);
    }

    @Test(timeout = 4000)
    public void testResolve_ParameterizedType() {
        ParameterizedType listType = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        Type result = $Gson$Types.resolve(String.class, String.class, listType);
        assertTrue(result instanceof ParameterizedType);
    }

    @Test(timeout = 4000)
    public void testResolve_WildcardType() {
        WildcardType wildcard = $Gson$Types.subtypeOf(Number.class);
        Type result = $Gson$Types.resolve(String.class, String.class, wildcard);
        assertTrue(result instanceof WildcardType);
    }

    @Test(timeout = 4000)
    public void testResolveTypeVariable_DeclaredByClass() {
        TypeVariable<?> tv = new TypeVariable<Class<?>>() {
            @Override public Type[] getBounds() { return new Type[]{Object.class}; }
            @Override public GenericDeclaration getGenericDeclaration() { return ArrayList.class; }
            @Override public String getName() { return "E"; }
            @Override public String getTypeName() { return "E"; }
        };
        Type result = $Gson$Types.resolveTypeVariable(
                ArrayList.class, ArrayList.class, tv);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testResolveTypeVariable_DeclaredByParameterizedType() {
        TypeVariable<?> tv = new TypeVariable<Class<?>>() {
            @Override public Type[] getBounds() { return new Type[]{Object.class}; }
            @Override public GenericDeclaration getGenericDeclaration() { return List.class; }
            @Override public String getName() { return "E"; }
            @Override public String getTypeName() { return "E"; }
        };
        ParameterizedType listType = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        Type result = $Gson$Types.resolveTypeVariable(listType, List.class, tv);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testResolveTypeVariable_NotDeclaredByClass() {
        TypeVariable<?> tv = new TypeVariable<Class<?>>() {
            @Override public Type[] getBounds() { return new Type[]{Object.class}; }
            @Override public GenericDeclaration getGenericDeclaration() { return String.class; }
            @Override public String getName() { return "T"; }
            @Override public String getTypeName() { return "T"; }
        };
        Type result = $Gson$Types.resolveTypeVariable(
                ArrayList.class, ArrayList.class, tv);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testIndexOf_Found() {
        Object[] array = {"a", "b", "c"};
        assertEquals(1, $Gson$Types.indexOf(array, "b"));
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testIndexOf_NotFound() {
        Object[] array = {"a", "b", "c"};
        $Gson$Types.indexOf(array, "d");
    }

    @Test(timeout = 4000)
    public void testDeclaringClassOf_ClassDeclaration() {
        TypeVariable<?> tv = new TypeVariable<Class<?>>() {
            @Override public Type[] getBounds() { return new Type[]{Object.class}; }
            @Override public GenericDeclaration getGenericDeclaration() { return ArrayList.class; }
            @Override public String getName() { return "E"; }
            @Override public String getTypeName() { return "E"; }
        };
        assertEquals(ArrayList.class, $Gson$Types.declaringClassOf(tv));
    }

    @Test(timeout = 4000)
    public void testDeclaringClassOf_NonClassDeclaration() {
        TypeVariable<?> tv = new TypeVariable<Class<?>>() {
            @Override public Type[] getBounds() { return new Type[]{Object.class}; }
            @Override public GenericDeclaration getGenericDeclaration() { return new GenericDeclaration() {
                @Override public TypeVariable<?>[] getTypeParameters() { return new TypeVariable[0]; }
                @Override public String toGenericString() { return "generic"; }
            }; }
            @Override public String getName() { return "T"; }
            @Override public String getTypeName() { return "T"; }
        };
        assertNull($Gson$Types.declaringClassOf(tv));
    }

    @Test(timeout = 4000)
    public void testCheckNotPrimitive_NonPrimitive() {
        $Gson$Types.checkNotPrimitive(String.class);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCheckNotPrimitive_Primitive() {
        $Gson$Types.checkNotPrimitive(int.class);
    }

    /* ==================== Partition B: BVA & Extremes ==================== */

    @Test(timeout = 4000)
    public void testHashCodeOrZero_Null() {
        assertEquals(0, $Gson$Types.hashCodeOrZero(null));
    }

    @Test(timeout = 4000)
    public void testHashCodeOrZero_NonNull() {
        assertEquals(String.class.hashCode(), $Gson$Types.hashCodeOrZero(String.class));
    }

    @Test(timeout = 4000)
    public void testTypeToString_Class() {
        assertEquals("java.lang.String", $Gson$Types.typeToString(String.class));
    }

    @Test(timeout = 4000)
    public void testTypeToString_NonClass() {
        ParameterizedType listType = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        assertNotNull($Gson$Types.typeToString(listType));
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_NoLowerBound() {
        WildcardType wildcard = $Gson$Types.subtypeOf(Number.class);
        assertArrayEquals(new Type[]{Number.class}, wildcard.getUpperBounds());
        assertArrayEquals(new Type[]{}, wildcard.getLowerBounds());
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_WithLowerBound() {
        WildcardType wildcard = $Gson$Types.supertypeOf(String.class);
        assertArrayEquals(new Type[]{Object.class}, wildcard.getUpperBounds());
        assertArrayEquals(new Type[]{String.class}, wildcard.getLowerBounds());
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_Equals() {
        WildcardType w1 = $Gson$Types.subtypeOf(Number.class);
        WildcardType w2 = $Gson$Types.subtypeOf(Number.class);
        assertEquals(w1, w2);
        assertEquals(w1.hashCode(), w2.hashCode());
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_NotEquals() {
        WildcardType w1 = $Gson$Types.subtypeOf(Number.class);
        WildcardType w2 = $Gson$Types.subtypeOf(Integer.class);
        assertNotEquals(w1, w2);
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_Equals() {
        ParameterizedType p1 = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        ParameterizedType p2 = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_NotEquals() {
        ParameterizedType p1 = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        ParameterizedType p2 = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, Integer.class);
        assertNotEquals(p1, p2);
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_ToString() {
        ParameterizedType p = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        assertNotNull(p.toString());
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeImpl_Equals() {
        GenericArrayType g1 = $Gson$Types.arrayOf(String.class);
        GenericArrayType g2 = $Gson$Types.arrayOf(String.class);
        assertEquals(g1, g2);
        assertEquals(g1.hashCode(), g2.hashCode());
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeImpl_NotEquals() {
        GenericArrayType g1 = $Gson$Types.arrayOf(String.class);
        GenericArrayType g2 = $Gson$Types.arrayOf(Integer.class);
        assertNotEquals(g1, g2);
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeImpl_HashCode() {
        GenericArrayType g = $Gson$Types.arrayOf(String.class);
        assertNotEquals(0, g.hashCode());
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_EmptyTypeArguments() {
        ParameterizedType p = $Gson$Types.newParameterizedTypeWithOwner(
                null, ArrayList.class);
        assertArrayEquals(new Type[]{}, p.getActualTypeArguments());
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_NullTypeArgument() {
        try {
            $Gson$Types.newParameterizedTypeWithOwner(null, List.class, (Type) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
        }
    }

    /* ==================== Partition C: Defect-Targeted (testIssue1107) ==================== */

    @Test(timeout = 4000)
    public void testIssue1107_CollectionElementTypeResolution() {
        // This test targets the defect where deserializing a collection of SmallClass
        // causes ClassCastException because the element type resolves incorrectly.
        // The bug occurs when resolving generic types in collections, where the
        // type variable resolution fails and returns Object instead of the actual type.
        
        // Create a generic type: List<SmallClass>
        ParameterizedType listType = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, SmallClass.class);
        
        // Verify the element type is correctly resolved to SmallClass
        Type elementType = $Gson$Types.getCollectionElementType(listType, List.class);
        assertEquals(SmallClass.class, elementType);
        
        // Also test with a wildcard type: List<? extends SmallClass>
        WildcardType wildcard = $Gson$Types.subtypeOf(SmallClass.class);
        ParameterizedType wildcardListType = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, wildcard);
        Type wildcardElementType = $Gson$Types.getCollectionElementType(wildcardListType, List.class);
        assertEquals(SmallClass.class, wildcardElementType);
        
        // Test with a generic subclass: class MyList extends ArrayList<SmallClass>
        ParameterizedType myListType = $Gson$Types.newParameterizedTypeWithOwner(
                null, MyList.class);
        Type resolvedElementType = $Gson$Types.getCollectionElementType(myListType, MyList.class);
        assertEquals(SmallClass.class, resolvedElementType);
        
        // Test with a generic type variable resolution
        TypeVariable<?> tv = new TypeVariable<Class<?>>() {
            @Override public Type[] getBounds() { return new Type[]{SmallClass.class}; }
            @Override public GenericDeclaration getGenericDeclaration() { return MyList.class; }
            @Override public String getName() { return "T"; }
            @Override public String getTypeName() { return "T"; }
        };
        Type resolved = $Gson$Types.resolve(MyList.class, MyList.class, tv);
        assertEquals(SmallClass.class, resolved);
    }

    @Test(timeout = 4000)
    public void testIssue1107_MapValueTypeResolution() {
        // Test map value type resolution which is related to the defect
        ParameterizedType mapType = $Gson$Types.newParameterizedTypeWithOwner(
                null, Map.class, String.class, SmallClass.class);
        Type[] keyValueTypes = $Gson$Types.getMapKeyAndValueTypes(mapType, Map.class);
        assertEquals(String.class, keyValueTypes[0]);
        assertEquals(SmallClass.class, keyValueTypes[1]);
    }

    @Test(timeout = 4000)
    public void testIssue1107_GenericSupertypeResolution() {
        // Test generic supertype resolution with SmallClass
        Type supertype = $Gson$Types.getGenericSupertype(
                MyList.class, MyList.class, List.class);
        assertTrue(supertype instanceof ParameterizedType);
        ParameterizedType paramType = (ParameterizedType) supertype;
        assertEquals(List.class, paramType.getRawType());
        assertEquals(SmallClass.class, paramType.getActualTypeArguments()[0]);
    }

    /* ==================== Partition D: Exception & Defensive Guards ==================== */

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testPrivateConstructor() {
        new $Gson$Types();
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWildcardTypeImpl_InvalidLowerBounds() {
        // Cannot directly instantiate WildcardTypeImpl, but we can test via reflection
        try {
            Class<?> clazz = Class.forName("com.google.gson.internal.$Gson$Types$WildcardTypeImpl");
            Constructor<?> constructor = clazz.getDeclaredConstructor(Type[].class, Type[].class);
            constructor.setAccessible(true);
            constructor.newInstance(new Type[]{Object.class}, new Type[]{String.class, Integer.class});
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWildcardTypeImpl_InvalidUpperBounds() {
        try {
            Class<?> clazz = Class.forName("com.google.gson.internal.$Gson$Types$WildcardTypeImpl");
            Constructor<?> constructor = clazz.getDeclaredConstructor(Type[].class, Type[].class);
            constructor.setAccessible(true);
            constructor.newInstance(new Type[]{String.class, Integer.class}, new Type[]{});
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testWildcardTypeImpl_NullLowerBound() {
        try {
            Class<?> clazz = Class.forName("com.google.gson.internal.$Gson$Types$WildcardTypeImpl");
            Constructor<?> constructor = clazz.getDeclaredConstructor(Type[].class, Type[].class);
            constructor.setAccessible(true);
            constructor.newInstance(new Type[]{Object.class}, new Type[]{null});
        } catch (Exception e) {
            throw new NullPointerException(e.getMessage());
        }
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testWildcardTypeImpl_NullUpperBound() {
        try {
            Class<?> clazz = Class.forName("com.google.gson.internal.$Gson$Types$WildcardTypeImpl");
            Constructor<?> constructor = clazz.getDeclaredConstructor(Type[].class, Type[].class);
            constructor.setAccessible(true);
            constructor.newInstance(new Type[]{null}, new Type[]{});
        } catch (Exception e) {
            throw new NullPointerException(e.getMessage());
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParameterizedTypeImpl_InvalidOwner() {
        // Non-static inner class with null owner should fail
        try {
            Class<?> clazz = Class.forName("com.google.gson.internal.$Gson$Types$ParameterizedTypeImpl");
            Constructor<?> constructor = clazz.getDeclaredConstructor(Type.class, Type.class, Type[].class);
            constructor.setAccessible(true);
            constructor.newInstance(null, NonStaticInner.class, new Type[]{String.class});
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testParameterizedTypeImpl_NullTypeArgument() {
        try {
            Class<?> clazz = Class.forName("com.google.gson.internal.$Gson$Types$ParameterizedTypeImpl");
            Constructor<?> constructor = clazz.getDeclaredConstructor(Type.class, Type.class, Type[].class);
            constructor.setAccessible(true);
            constructor.newInstance(null, List.class, new Type[]{null});
        } catch (Exception e) {
            throw new NullPointerException(e.getMessage());
        }
    }

    /* ==================== Partition E: Object Lifecycle & Contract Integrity ==================== */

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_Serializable() {
        ParameterizedType p = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        assertTrue(p instanceof Serializable);
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeImpl_Serializable() {
        GenericArrayType g = $Gson$Types.arrayOf(String.class);
        assertTrue(g instanceof Serializable);
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_Serializable() {
        WildcardType w = $Gson$Types.subtypeOf(Number.class);
        assertTrue(w instanceof Serializable);
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_GetOwnerType() {
        ParameterizedType p = $Gson$Types.newParameterizedTypeWithOwner(
                Map.class, HashMap.class, String.class, Integer.class);
        assertEquals(Map.class, p.getOwnerType());
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_GetRawType() {
        ParameterizedType p = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        assertEquals(List.class, p.getRawType());
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_GetActualTypeArguments() {
        ParameterizedType p = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class, Integer.class);
        Type[] args = p.getActualTypeArguments();
        assertEquals(2, args.length);
        assertEquals(String.class, args[0]);
        assertEquals(Integer.class, args[1]);
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeImpl_GetGenericComponentType() {
        GenericArrayType g = $Gson$Types.arrayOf(String.class);
        assertEquals(String.class, g.getGenericComponentType());
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_GetUpperBounds() {
        WildcardType w = $Gson$Types.subtypeOf(Number.class);
        Type[] upperBounds = w.getUpperBounds();
        assertEquals(1, upperBounds.length);
        assertEquals(Number.class, upperBounds[0]);
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_GetLowerBounds() {
        WildcardType w = $Gson$Types.supertypeOf(String.class);
        Type[] lowerBounds = w.getLowerBounds();
        assertEquals(1, lowerBounds.length);
        assertEquals(String.class, lowerBounds[0]);
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_GetLowerBounds_Empty() {
        WildcardType w = $Gson$Types.subtypeOf(Number.class);
        Type[] lowerBounds = w.getLowerBounds();
        assertEquals(0, lowerBounds.length);
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_HashCode() {
        ParameterizedType p1 = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        ParameterizedType p2 = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeImpl_HashCode() {
        GenericArrayType g1 = $Gson$Types.arrayOf(String.class);
        GenericArrayType g2 = $Gson$Types.arrayOf(String.class);
        assertEquals(g1.hashCode(), g2.hashCode());
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_HashCode() {
        WildcardType w1 = $Gson$Types.subtypeOf(Number.class);
        WildcardType w2 = $Gson$Types.subtypeOf(Number.class);
        assertEquals(w1.hashCode(), w2.hashCode());
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_NotEqualsNull() {
        ParameterizedType p = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        assertNotEquals(p, null);
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeImpl_NotEqualsNull() {
        GenericArrayType g = $Gson$Types.arrayOf(String.class);
        assertNotEquals(g, null);
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_NotEqualsNull() {
        WildcardType w = $Gson$Types.subtypeOf(Number.class);
        assertNotEquals(w, null);
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_NotEqualsDifferentType() {
        ParameterizedType p = $Gson$Types.newParameterizedTypeWithOwner(
                null, List.class, String.class);
        assertNotEquals(p, "not a parameterized type");
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeImpl_NotEqualsDifferentType() {
        GenericArrayType g = $Gson$Types.arrayOf(String.class);
        assertNotEquals(g, "not a generic array type");
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_NotEqualsDifferentType() {
        WildcardType w = $Gson$Types.subtypeOf(Number.class);
        assertNotEquals(w, "not a wildcard type");
    }

    /* Helper classes for testing */

    public static class SmallClass {
        public String value;
    }

    public static class MyList extends ArrayList<SmallClass> {
        private static final long serialVersionUID = 1L;
    }

    public class NonStaticInner {
    }
}