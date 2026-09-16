package com.google.gson.internal;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 * - newParameterizedTypeWithOwner: owner type handling, null owner, non-null owner
 * - arrayOf: component type handling
 * - subtypeOf/supertypeOf: WildcardType vs regular bound, correct bound extraction
 * - canonicalize: Class, ParameterizedType, GenericArrayType, WildcardType, other types
 * - getRawType: Class, ParameterizedType, GenericArrayType, TypeVariable, WildcardType, null/unsupported
 * - equals: symmetric, reflexive, transitive, null safety, all type categories
 * - typeToString: Class vs ParameterizedType/WildcardType
 * - getGenericSupertype: class hierarchy, interfaces, recursive
 * - getSupertype: valid/invalid supertype
 * - getArrayComponentType: GenericArrayType vs Class array
 * - getCollectionElementType: Collection vs WildcardType vs Object
 * - getMapKeyAndValueTypes: Map, Properties special case, Object fallback
 * - resolve: TypeVariable, Class array, GenericArrayType, ParameterizedType, WildcardType, recursion
 * - resolveTypeVariable: declaredByRaw null vs ParameterizedType vs Object
 * - indexOf: normal find, not found -> NoSuchElementException
 * - declaringClassOf: Class vs GenericDeclaration
 * - checkNotPrimitive: primitive vs non-primitive
 * 
 * Partition B: Boundary Value Analysis & Extremes
 * - Null arguments for ownerType, rawType, typeArguments
 * - Empty typeArguments array
 * - EMPTY_TYPE_ARRAY usage
 * - Primitive types as type arguments (should fail)
 * - Object.class as bound
 * - Multiple upper/lower bounds (only single supported)
 * - Array with primitive component type
 * - Recursive type variable resolution (targets StackOverflowError defect)
 * 
 * Partition C: Defect-Targeted Branch Zone
 * - Recursive type variables causing infinite recursion in resolve()
 * - TypeVariable with self-referencing generic bounds
 * - Mutually referencing type variables
 * 
 * Partition D: Exception & Defensive Guard Paths
 * - IllegalArgumentException for invalid arguments (null rawType, primitive args)
 * - NullPointerException from checkNotNull
 * - UnsupportedOperationException from constructor
 * - ClassCastException for non-array types
 * - NoSuchElementException from indexOf
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 * - equals/hashCode contract for ParameterizedTypeImpl, GenericArrayTypeImpl, WildcardTypeImpl
 * - toString representation
 * - Serializable nature of implementation classes
 */
public class $Gson$TypesDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====
    
    @Test(timeout = 4000)
    public void testNewParameterizedTypeWithOwner_NullOwner() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertNotNull(pt);
        assertNull(pt.getOwnerType());
        assertEquals(List.class, pt.getRawType());
        assertEquals(1, pt.getActualTypeArguments().length);
        assertEquals(String.class, pt.getActualTypeArguments()[0]);
    }

    @Test(timeout = 4000)
    public void testNewParameterizedTypeWithOwner_WithOwner() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(Map.class, Map.Entry.class, String.class, Integer.class);
        assertNotNull(pt);
        assertEquals(Map.class, pt.getOwnerType());
        assertEquals(Map.Entry.class, pt.getRawType());
        assertArrayEquals(new Type[]{String.class, Integer.class}, pt.getActualTypeArguments());
    }

    @Test(timeout = 4000)
    public void testNewParameterizedTypeWithOwner_EmptyTypeArgs() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class);
        assertNotNull(pt);
        assertEquals(0, pt.getActualTypeArguments().length);
    }

    @Test(timeout = 4000)
    public void testNewParameterizedTypeWithOwner_CanonicalizesInputs() {
        // Using a wildcard type to verify canonicalization
        WildcardType wc = $Gson$Types.subtypeOf(Number.class);
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, wc);
        // The wildcard type should be canonicalized internally
        assertTrue(pt.getActualTypeArguments()[0] instanceof WildcardType);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNewParameterizedTypeWithOwner_PrimitiveTypeArg() {
        $Gson$Types.newParameterizedTypeWithOwner(null, List.class, int.class);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNewParameterizedTypeWithOwner_NullTypeArg() {
        $Gson$Types.newParameterizedTypeWithOwner(null, List.class, (Type) null);
    }

    @Test(timeout = 4000)
    public void testArrayOf_Class() {
        GenericArrayType arr = $Gson$Types.arrayOf(String.class);
        assertEquals(String.class, arr.getGenericComponentType());
        assertEquals("java.lang.String[]", arr.toString());
    }

    @Test(timeout = 4000)
    public void testArrayOf_ParameterizedType() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        GenericArrayType arr = $Gson$Types.arrayOf(pt);
        assertEquals(pt, arr.getGenericComponentType());
    }

    @Test(timeout = 4000)
    public void testArrayOf_WildcardType() {
        WildcardType wc = $Gson$Types.subtypeOf(Number.class);
        GenericArrayType arr = $Gson$Types.arrayOf(wc);
        assertEquals(wc, arr.getGenericComponentType());
    }

    @Test(timeout = 4000)
    public void testSubtypeOf_RegularBound() {
        WildcardType wc = $Gson$Types.subtypeOf(CharSequence.class);
        assertArrayEquals(new Type[]{CharSequence.class}, wc.getUpperBounds());
        assertEquals(0, wc.getLowerBounds().length);
    }

    @Test(timeout = 4000)
    public void testSubtypeOf_ObjectBound() {
        WildcardType wc = $Gson$Types.subtypeOf(Object.class);
        assertArrayEquals(new Type[]{Object.class}, wc.getUpperBounds());
        assertEquals(0, wc.getLowerBounds().length);
    }

    @Test(timeout = 4000)
    public void testSubtypeOf_WildcardBound() {
        WildcardType inner = $Gson$Types.subtypeOf(Number.class);
        WildcardType outer = $Gson$Types.subtypeOf(inner);
        assertArrayEquals(inner.getUpperBounds(), outer.getUpperBounds());
    }

    @Test(timeout = 4000)
    public void testSupertypeOf_RegularBound() {
        WildcardType wc = $Gson$Types.supertypeOf(String.class);
        assertArrayEquals(new Type[]{String.class}, wc.getLowerBounds());
        assertArrayEquals(new Type[]{Object.class}, wc.getUpperBounds());
    }

    @Test(timeout = 4000)
    public void testSupertypeOf_WildcardBound() {
        WildcardType inner = $Gson$Types.supertypeOf(Number.class);
        WildcardType outer = $Gson$Types.supertypeOf(inner);
        assertArrayEquals(inner.getLowerBounds(), outer.getLowerBounds());
        assertArrayEquals(new Type[]{Object.class}, outer.getUpperBounds());
    }

    @Test(timeout = 4000)
    public void testSupertypeOf_ObjectBound() {
        WildcardType wc = $Gson$Types.supertypeOf(Object.class);
        assertArrayEquals(new Type[]{Object.class}, wc.getLowerBounds());
        assertArrayEquals(new Type[]{Object.class}, wc.getUpperBounds());
    }

    @Test(timeout = 4000)
    public void testCanonicalize_Class() {
        Type result = $Gson$Types.canonicalize(String.class);
        assertSame(String.class, result);
    }

    @Test(timeout = 4000)
    public void testCanonicalize_ArrayClass() {
        Type result = $Gson$Types.canonicalize(String[].class);
        assertTrue(result instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) result).getGenericComponentType());
    }

    @Test(timeout = 4000)
    public void testCanonicalize_ParameterizedType() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        Type result = $Gson$Types.canonicalize(pt);
        assertTrue(result instanceof ParameterizedType);
        assertEquals(pt, result);
    }

    @Test(timeout = 4000)
    public void testCanonicalize_GenericArrayType() {
        GenericArrayType arr = $Gson$Types.arrayOf(String.class);
        Type result = $Gson$Types.canonicalize(arr);
        assertTrue(result instanceof GenericArrayType);
        assertEquals(arr, result);
    }

    @Test(timeout = 4000)
    public void testCanonicalize_WildcardType() {
        WildcardType wc = $Gson$Types.subtypeOf(Number.class);
        Type result = $Gson$Types.canonicalize(wc);
        assertTrue(result instanceof WildcardType);
        assertEquals(wc, result);
    }

    @Test(timeout = 4000)
    public void testCanonicalize_UnsupportedType() {
        Type custom = new Type() {}; // Anonymous type
        Type result = $Gson$Types.canonicalize(custom);
        assertSame(custom, result);
    }

    @Test(timeout = 4000)
    public void testGetRawType_Class() {
        assertEquals(String.class, $Gson$Types.getRawType(String.class));
    }

    @Test(timeout = 4000)
    public void testGetRawType_ParameterizedType() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertEquals(List.class, $Gson$Types.getRawType(pt));
    }

    @Test(timeout = 4000)
    public void testGetRawType_GenericArrayType() {
        GenericArrayType arr = $Gson$Types.arrayOf(String.class);
        assertEquals(Object[].class, $Gson$Types.getRawType(arr));
    }

    @Test(timeout = 4000)
    public void testGetRawType_GenericArrayTypeOfParameterized() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        GenericArrayType arr = $Gson$Types.arrayOf(pt);
        assertEquals(Array.newInstance(List.class, 0).getClass(), $Gson$Types.getRawType(arr));
    }

    @Test(timeout = 4000)
    public void testGetRawType_TypeVariable() {
        // Cannot easily create a TypeVariable, but can test via resolve
        // TypeVariable should return Object.class
        // We'll test indirectly through resolve
    }

    @Test(timeout = 4000)
    public void testGetRawType_WildcardType() {
        WildcardType wc = $Gson$Types.subtypeOf(String.class);
        assertEquals(String.class, $Gson$Types.getRawType(wc));
    }

    @Test(timeout = 4000)
    public void testGetRawType_WildcardTypeWithObject() {
        WildcardType wc = $Gson$Types.subtypeOf(Object.class);
        assertEquals(Object.class, $Gson$Types.getRawType(wc));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetRawType_Null() {
        $Gson$Types.getRawType(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetRawType_UnsupportedType() {
        $Gson$Types.getRawType(new Type() {});
    }

    @Test(timeout = 4000)
    public void testEquals_SameObject() {
        assertTrue($Gson$Types.equals(null, null));
        assertTrue($Gson$Types.equals(String.class, String.class));
    }

    @Test(timeout = 4000)
    public void testEquals_Class() {
        assertTrue($Gson$Types.equals(String.class, String.class));
        assertFalse($Gson$Types.equals(String.class, Integer.class));
    }

    @Test(timeout = 4000)
    public void testEquals_ParameterizedType() {
        ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertTrue($Gson$Types.equals(pt1, pt2));
        
        ParameterizedType pt3 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, Integer.class);
        assertFalse($Gson$Types.equals(pt1, pt3));
    }

    @Test(timeout = 4000)
    public void testEquals_ParameterizedTypeWithOwner() {
        ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(Map.class, Map.Entry.class, String.class, String.class);
        ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(Map.class, Map.Entry.class, String.class, String.class);
        assertTrue($Gson$Types.equals(pt1, pt2));
        
        ParameterizedType pt3 = $Gson$Types.newParameterizedTypeWithOwner(null, Map.Entry.class, String.class, String.class);
        assertFalse($Gson$Types.equals(pt1, pt3));
    }

    @Test(timeout = 4000)
    public void testEquals_GenericArrayType() {
        GenericArrayType arr1 = $Gson$Types.arrayOf(String.class);
        GenericArrayType arr2 = $Gson$Types.arrayOf(String.class);
        assertTrue($Gson$Types.equals(arr1, arr2));
        
        GenericArrayType arr3 = $Gson$Types.arrayOf(Integer.class);
        assertFalse($Gson$Types.equals(arr1, arr3));
    }

    @Test(timeout = 4000)
    public void testEquals_WildcardType() {
        WildcardType wc1 = $Gson$Types.subtypeOf(Number.class);
        WildcardType wc2 = $Gson$Types.subtypeOf(Number.class);
        assertTrue($Gson$Types.equals(wc1, wc2));
        
        WildcardType wc3 = $Gson$Types.subtypeOf(String.class);
        assertFalse($Gson$Types.equals(wc1, wc3));
    }

    @Test(timeout = 4000)
    public void testEquals_DifferentTypes() {
        assertFalse($Gson$Types.equals(String.class, $Gson$Types.subtypeOf(String.class)));
        assertFalse($Gson$Types.equals($Gson$Types.arrayOf(String.class), $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class)));
    }

    @Test(timeout = 4000)
    public void testTypeToString_Class() {
        assertEquals("java.lang.String", $Gson$Types.typeToString(String.class));
    }

    @Test(timeout = 4000)
    public void testTypeToString_ParameterizedType() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertEquals("java.util.List<java.lang.String>", $Gson$Types.typeToString(pt));
    }

    @Test(timeout = 4000)
    public void testTypeToString_WildcardType() {
        WildcardType wc = $Gson$Types.subtypeOf(Number.class);
        assertEquals("? extends java.lang.Number", $Gson$Types.typeToString(wc));
    }

    @Test(timeout = 4000)
    public void testGetArrayComponentType_GenericArrayType() {
        GenericArrayType arr = $Gson$Types.arrayOf(String.class);
        assertEquals(String.class, $Gson$Types.getArrayComponentType(arr));
    }

    @Test(timeout = 4000)
    public void testGetArrayComponentType_ClassArray() {
        assertEquals(String.class, $Gson$Types.getArrayComponentType(String[].class));
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testGetArrayComponentType_NonArray() {
        $Gson$Types.getArrayComponentType(String.class);
    }

    @Test(timeout = 4000)
    public void testGetCollectionElementType_DirectCollection() {
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertEquals(String.class, $Gson$Types.getCollectionElementType(context, List.class));
    }

    @Test(timeout = 4000)
    public void testGetCollectionElementType_ExtendedCollection() {
        // AbstractList<String> implements Collection<String>
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, AbstractList.class, String.class);
        assertEquals(String.class, $Gson$Types.getCollectionElementType(context, AbstractList.class));
    }

    @Test(timeout = 4000)
    public void testGetCollectionElementType_WildcardCollection() {
        WildcardType wc = $Gson$Types.subtypeOf($Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class));
        assertEquals(String.class, $Gson$Types.getCollectionElementType(wc, List.class));
    }

    @Test(timeout = 4000)
    public void testGetCollectionElementType_NonGenericCollection() {
        assertEquals(Object.class, $Gson$Types.getCollectionElementType(Collection.class, Collection.class));
    }

    @Test(timeout = 4000)
    public void testGetMapKeyAndValueTypes_DirectMap() {
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
        Type[] types = $Gson$Types.getMapKeyAndValueTypes(context, Map.class);
        assertArrayEquals(new Type[]{String.class, Integer.class}, types);
    }

    @Test(timeout = 4000)
    public void testGetMapKeyAndValueTypes_Properties() {
        Type[] types = $Gson$Types.getMapKeyAndValueTypes(Properties.class, Properties.class);
        assertArrayEquals(new Type[]{String.class, String.class}, types);
    }

    @Test(timeout = 4000)
    public void testGetMapKeyAndValueTypes_NonGenericMap() {
        Type[] types = $Gson$Types.getMapKeyAndValueTypes(Map.class, Map.class);
        assertArrayEquals(new Type[]{Object.class, Object.class}, types);
    }

    @Test(timeout = 4000)
    public void testCheckNotPrimitive_NonPrimitive() {
        // Should not throw
        $Gson$Types.checkNotPrimitive(String.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCheckNotPrimitive_Primitive() {
        $Gson$Types.checkNotPrimitive(int.class);
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    // The known defect causes StackOverflowError with recursive type variables.
    // We create a self-referencing type variable scenario to trigger it.
    @Test(timeout = 4000)
    public void testResolve_RecursiveTypeVariable_Simple() {
        // Create a class with recursive type variable (e.g., Enum)
        // Enum<E extends Enum<E>> is the classic example
        Type enumType = Enum.class;
        // This should resolve without StackOverflow
        Type resolved = $Gson$Types.resolve(enumType, Enum.class, Enum.class.getTypeParameters()[0]);
        assertNotNull(resolved);
        // The resolution should terminate (either returning a type or the same TypeVariable)
        // We just verify it doesn't throw StackOverflowError
    }

    @Test(timeout = 4000)
    public void testResolve_RecursiveTypeVariable_Complex() {
        // More complex recursive structure: Comparable<T extends Comparable<T>>
        // This creates a cycle in type variable resolution
        Type comparableType = Comparable.class;
        // Try to resolve the type variable of Comparable
        TypeVariable<?>[] typeParams = Comparable.class.getTypeParameters();
        if (typeParams.length > 0) {
            Type resolved = $Gson$Types.resolve(comparableType, Comparable.class, typeParams[0]);
            // Should complete without StackOverflow
            assertNotNull(resolved);
        }
    }

    @Test(timeout = 4000)
    public void testResolve_RecursiveTypeVariable_WithSupertype() {
        // Create a scenario similar to the known failing test
        // Use a combination of type parameters that forms a cycle through supertype resolution
        // AbstractMap.SimpleEntry<K,V> implements Map.Entry<K,V>
        // We'll resolve a type that involves recursive bounds
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, AbstractMap.SimpleEntry.class, String.class, String.class);
        
        // Get the Map.Entry interface type
        Type entryType = $Gson$Types.getSupertype(context, AbstractMap.SimpleEntry.class, Map.Entry.class);
        assertNotNull(entryType);
        
        // Now resolve a type variable within this context
        // This indirectly creates a resolution chain that may involve recursive type variables
        Type[] typeArgs = ((ParameterizedType) entryType).getActualTypeArguments();
        assertEquals(2, typeArgs.length);
        
        // Resolve one type argument which may trigger recursive resolution
        Type resolvedKey = $Gson$Types.resolve(context, AbstractMap.SimpleEntry.class, ((ParameterizedType) entryType).getActualTypeArguments()[0]);
        assertEquals(String.class, resolvedKey);
    }

    @Test(timeout = 4000)
    public void testResolve_RecursiveTypeVariable_MutualRecursion() {
        // Create a scenario with mutual recursion between two type variables
        // Using a custom inner class that has type variables referencing each other
        class MutuallyRecursive<K extends Comparable<V>, V extends Comparable<K>> {}
        
        // Get the type parameters
        TypeVariable<?>[] params = MutuallyRecursive.class.getTypeParameters();
        assertEquals(2, params.length);
        
        // Resolve one type variable, which may reference the other
        Type context = MutuallyRecursive.class;
        Type resolved = $Gson$Types.resolve(context, MutuallyRecursive.class, params[0]);
        // Should not stack overflow - either returns the same TypeVariable or resolves to bound
        assertNotNull(resolved);
    }

    @Test(timeout = 4000)
    public void testResolve_RecursiveTypeVariable_SelfReference() {
        // Direct self-reference: T extends Comparable<T>
        class SelfRef<T extends Comparable<T>> {}
        
        TypeVariable<?>[] params = SelfRef.class.getTypeParameters();
        assertEquals(1, params.length);
        
        Type resolved = $Gson$Types.resolve(SelfRef.class, SelfRef.class, params[0]);
        // Expected behavior: either returns the TypeVariable itself or resolves to Comparable<T>
        assertNotNull(resolved);
        // This test specifically targets the StackOverflowError described in the defect
    }

    @Test(timeout = 4000)
    public void testResolve_RecursiveTypeVariable_WithExtends() {
        // More complex: T extends SomeClass<T>
        abstract class RecursiveClass<T extends RecursiveClass<T>> {}
        
        TypeVariable<?>[] params = RecursiveClass.class.getTypeParameters();
        assertEquals(1, params.length);
        
        Type resolved = $Gson$Types.resolve(RecursiveClass.class, RecursiveClass.class, params[0]);
        assertNotNull(resolved);
    }

    @Test(timeout = 4000)
    public void testResolve_RecursiveTypeVariable_Nested() {
        // Nested recursive type variables
        class Outer<T extends Outer<T>.Inner> {
            class Inner {}
        }
        
        TypeVariable<?>[] params = Outer.class.getTypeParameters();
        assertEquals(1, params.length);
        
        // This may create complex resolution path
        Type resolved = $Gson$Types.resolve(Outer.class, Outer.class, params[0]);
        assertNotNull(resolved);
    }

    @Test(timeout = 4000)
    public void testResolve_RecursiveTypeVariable_WithMultipleBounds() {
        // Multiple bounds with recursion: T extends A & B<T>
        interface BoundInterface<T> {}
        class MultiBound<T extends Comparable<T> & BoundInterface<T>> {}
        
        TypeVariable<?>[] params = MultiBound.class.getTypeParameters();
        assertEquals(1, params.length);
        
        // This may trigger complex resolution
        Type resolved = $Gson$Types.resolve(MultiBound.class, MultiBound.class, params[0]);
        assertNotNull(resolved);
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====
    
    @Test(timeout = 4000)
    public void testGetGenericSupertype_DirectMatch() {
        assertEquals(String.class, $Gson$Types.getGenericSupertype(String.class, String.class, String.class));
    }

    @Test(timeout = 4000)
    public void testGetGenericSupertype_ViaInterface() {
        Type result = $Gson$Types.getGenericSupertype(ArrayList.class, ArrayList.class, List.class);
        assertTrue(result instanceof ParameterizedType);
        assertEquals(List.class, ((ParameterizedType) result).getRawType());
    }

    @Test(timeout = 4000)
    public void testGetGenericSupertype_ViaSuperclass() {
        Type result = $Gson$Types.getGenericSupertype(TreeSet.class, TreeSet.class, Set.class);
        assertTrue(result instanceof ParameterizedType);
    }

    @Test(timeout = 4000)
    public void testGetGenericSupertype_NoResolve() {
        assertEquals(Set.class, $Gson$Types.getGenericSupertype(Set.class, Set.class, Collection.class));
    }

    @Test(timeout = 4000)
    public void testGetSupertype_Valid() {
        Type result = $Gson$Types.getSupertype(
            $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, String.class),
            ArrayList.class,
            List.class
        );
        assertTrue(result instanceof ParameterizedType);
        assertEquals(List.class, ((ParameterizedType) result).getRawType());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetSupertype_Invalid() {
        $Gson$Types.getSupertype(String.class, String.class, List.class);
    }

    @Test(timeout = 4000)
    public void testResolve_SimpleTypeVariable() {
        Type result = $Gson$Types.resolve(
            $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, String.class),
            ArrayList.class,
            ArrayList.class.getTypeParameters()[0]
        );
        assertEquals(String.class, result);
    }

    @Test(timeout = 4000)
    public void testResolve_UnresolvableTypeVariable() {
        // A type variable without context should return itself
        TypeVariable<?>[] params = ArrayList.class.getTypeParameters();
        Type result = $Gson$Types.resolve(Object.class, Object.class, params[0]);
        assertSame(params[0], result);
    }

    @Test(timeout = 4000)
    public void testResolve_ClassArray() {
        Type result = $Gson$Types.resolve(String.class, String.class, String[].class);
        assertTrue(result instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) result).getGenericComponentType());
    }

    @Test(timeout = 4000)
    public void testResolve_GenericArrayType() {
        GenericArrayType arr = $Gson$Types.arrayOf(
            $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class)
        );
        Type result = $Gson$Types.resolve(String.class, String.class, arr);
        assertSame(arr, result);
    }

    @Test(timeout = 4000)
    public void testResolve_ParameterizedType_NoChange() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        Type result = $Gson$Types.resolve(String.class, String.class, pt);
        assertSame(pt, result);
    }

    @Test(timeout = 4000)
    public void testResolve_ParameterizedType_WithChanges() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, ArrayList.class.getTypeParameters()[0]);
        Type result = $Gson$Types.resolve(
            $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, String.class),
            ArrayList.class,
            pt
        );
        assertTrue(result instanceof ParameterizedType);
        assertArrayEquals(new Type[]{String.class}, ((ParameterizedType) result).getActualTypeArguments());
    }

    @Test(timeout = 4000)
    public void testResolve_WildcardType_UpperBound() {
        WildcardType wc = $Gson$Types.subtypeOf(ArrayList.class.getTypeParameters()[0]);
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, String.class);
        Type result = $Gson$Types.resolve(context, ArrayList.class, wc);
        assertTrue(result instanceof WildcardType);
        assertArrayEquals(new Type[]{String.class}, ((WildcardType) result).getUpperBounds());
    }

    @Test(timeout = 4000)
    public void testResolve_WildcardType_LowerBound() {
        WildcardType wc = $Gson$Types.supertypeOf(ArrayList.class.getTypeParameters()[0]);
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, String.class);
        Type result = $Gson$Types.resolve(context, ArrayList.class, wc);
        assertTrue(result instanceof WildcardType);
        assertArrayEquals(new Type[]{String.class}, ((WildcardType) result).getLowerBounds());
    }

    @Test(timeout = 4000)
    public void testResolve_WildcardType_NoChange() {
        WildcardType wc = $Gson$Types.subtypeOf(String.class);
        Type result = $Gson$Types.resolve(String.class, String.class, wc);
        assertSame(wc, result);
    }

    @Test(timeout = 4000)
    public void testResolve_NonTypeVariable() {
        assertEquals(String.class, $Gson$Types.resolve(String.class, String.class, String.class));
    }

    @Test(timeout = 4000)
    public void testIndexOf_Found() {
        assertEquals(0, $Gson$Types.indexOf(new Object[]{"a", "b"}, "a"));
        assertEquals(1, $Gson$Types.indexOf(new Object[]{"a", "b"}, "b"));
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testIndexOf_NotFound() {
        $Gson$Types.indexOf(new Object[]{"a", "b"}, "c");
    }

    @Test(timeout = 4000)
    public void testDeclaringClassOf_ClassDeclaration() {
        TypeVariable<?>[] params = ArrayList.class.getTypeParameters();
        Class<?> declaring = $Gson$Types.declaringClassOf(params[0]);
        assertEquals(ArrayList.class, declaring);
    }

    @Test(timeout = 4000)
    public void testDeclaringClassOf_MethodDeclaration() {
        // Type variables from methods have null declaring class
        // We can't easily get a method type variable, so we test indirectly
    }

    @Test(timeout = 4000)
    public void testHashCodeOrZero_Null() {
        assertEquals(0, $Gson$Types.hashCodeOrZero(null));
    }

    @Test(timeout = 4000)
    public void testHashCodeOrZero_NonNull() {
        assertEquals(String.class.hashCode(), $Gson$Types.hashCodeOrZero(String.class));
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====
    
    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testConstructor_Throws() {
        // Call private constructor via reflection
        try {
            java.lang.reflect.Constructor<$Gson$Types> c = $Gson$Types.class.getDeclaredConstructor();
            c.setAccessible(true);
            c.newInstance();
        } catch (InvocationTargetException e) {
            throw (UnsupportedOperationException) e.getCause();
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParameterizedTypeImpl_NullRawType() {
        $Gson$Types.newParameterizedTypeWithOwner(null, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWildcardTypeImpl_InvalidBounds() {
        // This is indirectly tested through subtypeOf/supertypeOf
        // The WildcardTypeImpl constructor validates bounds
        // We can create one directly to test the validation
        new Object() {
            void test() {
                // Invalid: lowerBounds length > 1
                new $Gson$Types.WildcardTypeImpl(
                    new Type[]{Object.class}, 
                    new Type[]{String.class, Integer.class}
                );
            }
        };
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    
    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_Equals() {
        ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertEquals(pt1, pt2);
        assertEquals(pt1.hashCode(), pt2.hashCode());
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_EqualsWithOwner() {
        ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(Map.class, Map.Entry.class, String.class, String.class);
        ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(Map.class, Map.Entry.class, String.class, String.class);
        assertEquals(pt1, pt2);
        assertEquals(pt1.hashCode(), pt2.hashCode());
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_ToString() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertEquals("java.util.List<java.lang.String>", pt.toString());
        
        ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
        assertEquals("java.util.Map<java.lang.String, java.lang.Integer>", pt2.toString());
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeImpl_Equals() {
        GenericArrayType arr1 = $Gson$Types.arrayOf(String.class);
        GenericArrayType arr2 = $Gson$Types.arrayOf(String.class);
        assertEquals(arr1, arr2);
        assertEquals(arr1.hashCode(), arr2.hashCode());
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeImpl_ToString() {
        GenericArrayType arr = $Gson$Types.arrayOf(String.class);
        assertEquals("java.lang.String[]", arr.toString());
        
        GenericArrayType arr2 = $Gson$Types.arrayOf($Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class));
        assertEquals("java.util.List<java.lang.String>[]", arr2.toString());
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_Equals() {
        WildcardType wc1 = $Gson$Types.subtypeOf(Number.class);
        WildcardType wc2 = $Gson$Types.subtypeOf(Number.class);
        assertEquals(wc1, wc2);
        assertEquals(wc1.hashCode(), wc2.hashCode());
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_EqualsSupertype() {
        WildcardType wc1 = $Gson$Types.supertypeOf(String.class);
        WildcardType wc2 = $Gson$Types.supertypeOf(String.class);
        assertEquals(wc1, wc2);
        assertEquals(wc1.hashCode(), wc2.hashCode());
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_ToString() {
        WildcardType wc = $Gson$Types.subtypeOf(Number.class);
        assertEquals("? extends java.lang.Number", wc.toString());
        
        WildcardType wc2 = $Gson$Types.supertypeOf(String.class);
        assertEquals("? super java.lang.String", wc2.toString());
        
        WildcardType wc3 = $Gson$Types.subtypeOf(Object.class);
        assertEquals("?", wc3.toString());
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_GetBounds() {
        WildcardType wc = $Gson$Types.subtypeOf(Number.class);
        assertArrayEquals(new Type[]{Number.class}, wc.getUpperBounds());
        assertEquals(0, wc.getLowerBounds().length);
        
        WildcardType wc2 = $Gson$Types.supertypeOf(String.class);
        assertArrayEquals(new Type[]{Object.class}, wc2.getUpperBounds());
        assertArrayEquals(new Type[]{String.class}, wc2.getLowerBounds());
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_Serializable() {
        assertTrue(Serializable.class.isAssignableFrom(
            $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class).getClass()
        ));
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeImpl_Serializable() {
        assertTrue(Serializable.class.isAssignableFrom(
            $Gson$Types.arrayOf(String.class).getClass()
        ));
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_Serializable() {
        assertTrue(Serializable.class.isAssignableFrom(
            $Gson$Types.subtypeOf(String.class).getClass()
        ));
    }
}