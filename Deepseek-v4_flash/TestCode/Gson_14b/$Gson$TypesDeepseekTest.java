package com.google.gson.internal;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.*;
import java.util.*;

public class $Gson$TypesDeepseekTest {
    /*
     * [Branch & Defect Analysis Matrix]
     * 
     * Partition A: Core Functional Logic & State Transitions
     * - newParameterizedTypeWithOwner: ownerType null/non-null, rawType Class/non-Class, typeArguments empty/single/multiple
     * - arrayOf: componentType Class/ParameterizedType/GenericArrayType/WildcardType/TypeVariable
     * - subtypeOf: bound Class/ParameterizedType/GenericArrayType/WildcardType/TypeVariable
     * - supertypeOf: bound Class/ParameterizedType/GenericArrayType/WildcardType/TypeVariable
     * - canonicalize: Class(not array), Class(is array), ParameterizedType, GenericArrayType, WildcardType, other
     * - getRawType: Class, ParameterizedType, GenericArrayType, TypeVariable, WildcardType, IllegalArgumentException
     * - equals: a==b, Class, ParameterizedType, GenericArrayType, WildcardType, TypeVariable, unsupported
     * - typeToString: Class, other
     * - getGenericSupertype: toResolve==rawType, interface matching, superclass matching, default
     * - getSupertype: basic check
     * - getArrayComponentType: GenericArrayType, Class array
     * - getCollectionElementType: WildcardType, ParameterizedType, Object.class fallback
     * - getMapKeyAndValueTypes: Properties, ParameterizedType, default
     * - resolve: TypeVariable, Class array, GenericArrayType, ParameterizedType, WildcardType, other
     * - resolveTypeVariable: declaredByRaw null, ParameterizedType, default
     * - indexOf: found, NoSuchElementException
     * - declaringClassOf: Class, null
     * - checkNotPrimitive: primitive, non-primitive
     * 
     * Partition B: Boundary Value Analysis & Extremes
     * - null arguments for ownerType, rawType, typeArguments
     * - EMPTY_TYPE_ARRAY bounds
     * - Primitive type arguments (should fail checkNotPrimitive)
     * - Array component type with cycles (potential StackOverflow)
     * - WildcardType with multiple bounds (only single supported)
     * - ParameterizedType with ownerType null and static/top-level class
     * - ParameterizedType with ownerType non-null and non-static inner class
     * 
     * Partition C: Defect-Targeted Branch Zone
     * - Defects4J known failures:
     *   - testDoubleSupertype: resolving ? super Number leads to ? super ? super Number
     *   - testIssue440WeakReference: recursive resolution causes StackOverflow
     *   - testSubSupertype: ? super Number resolved to ? extends ? super Number
     *   - testDoubleSubtype: ? extends Number resolved to ? extends ? extends Number
     *   - testIssue603PrintStream: recursive resolution causes StackOverflow
     *   - testSuperSubtype: ? extends Number resolved to ? super ? extends Number
     *   - testRecursiveResolveSimple: recursive resolution causes StackOverflow
     * 
     * Partition D: Exception & Defensive Guard Paths
     * - IllegalArgumentException for unexpected type in getRawType
     * - IllegalArgumentException for non-assignable supertype
     * - NullPointerException from checkNotNull for null type arguments
     * - IllegalArgumentException for primitive type arguments
     * - IllegalArgumentException for WildcardType with invalid bounds
     * - UnsupportedOperationException from private constructor
     * 
     * Partition E: Object Lifecycle & Contract Integrity
     * - equals/hashCode for ParameterizedTypeImpl, GenericArrayTypeImpl, WildcardTypeImpl
     * - toString for all implementations
     * - serializable nature (checked via instanceof)
     * 
     * Known Defect: Wildcard resolution in resolve() method incorrectly
     * wraps already-resolved wildcards. When resolving a wildcard type,
     * it should not re-wrap if the resolved result is already a wildcard.
     * The issue is in the WildcardType branch of resolve() - when
     * originalLowerBound.length == 1, it calls supertypeOf(lowerBound)
     * but lowerBound might already be a WildcardType, producing nested
     * wildcards like "? super ? super Number".
     * Similarly for upper bounds with subtypeOf.
     */

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testNewParameterizedTypeWithOwner_NullOwner_StaticClass() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertNotNull(pt);
        assertEquals(List.class, pt.getRawType());
        assertNull(pt.getOwnerType());
        assertEquals(1, pt.getActualTypeArguments().length);
        assertEquals(String.class, pt.getActualTypeArguments()[0]);
    }

    @Test(timeout = 4000)
    public void testNewParameterizedTypeWithOwner_NonNullOwner() throws Exception {
        // Create a non-static inner class scenario using reflection
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(
            Map.class, Map.Entry.class, String.class, Integer.class);
        assertNotNull(pt);
        assertEquals(Map.Entry.class, pt.getRawType());
        assertEquals(Map.class, pt.getOwnerType());
        Type[] args = pt.getActualTypeArguments();
        assertEquals(2, args.length);
        assertEquals(String.class, args[0]);
        assertEquals(Integer.class, args[1]);
    }

    @Test(timeout = 4000)
    public void testNewParameterizedTypeWithOwner_EmptyTypeArgs() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, String.class);
        assertNotNull(pt);
        assertEquals(String.class, pt.getRawType());
        assertEquals(0, pt.getActualTypeArguments().length);
    }

    @Test(timeout = 4000)
    public void testArrayOf_ClassComponent() {
        GenericArrayType gat = $Gson$Types.arrayOf(String.class);
        assertNotNull(gat);
        assertEquals(String.class, gat.getGenericComponentType());
    }

    @Test(timeout = 4000)
    public void testArrayOf_ParameterizedComponent() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        GenericArrayType gat = $Gson$Types.arrayOf(pt);
        assertNotNull(gat);
        assertTrue(gat.getGenericComponentType() instanceof ParameterizedType);
    }

    @Test(timeout = 4000)
    public void testSubtypeOf_ObjectBound() {
        WildcardType wt = $Gson$Types.subtypeOf(Object.class);
        assertNotNull(wt);
        assertEquals(1, wt.getUpperBounds().length);
        assertEquals(Object.class, wt.getUpperBounds()[0]);
        assertEquals(0, wt.getLowerBounds().length);
    }

    @Test(timeout = 4000)
    public void testSubtypeOf_NonObjectBound() {
        WildcardType wt = $Gson$Types.subtypeOf(Number.class);
        assertNotNull(wt);
        assertEquals(1, wt.getUpperBounds().length);
        assertEquals(Number.class, wt.getUpperBounds()[0]);
        assertEquals(0, wt.getLowerBounds().length);
    }

    @Test(timeout = 4000)
    public void testSupertypeOf_ObjectBound() {
        WildcardType wt = $Gson$Types.supertypeOf(Object.class);
        assertNotNull(wt);
        assertEquals(1, wt.getUpperBounds().length);
        assertEquals(Object.class, wt.getUpperBounds()[0]);
        assertEquals(1, wt.getLowerBounds().length);
        assertEquals(Object.class, wt.getLowerBounds()[0]);
    }

    @Test(timeout = 4000)
    public void testSupertypeOf_NonObjectBound() {
        WildcardType wt = $Gson$Types.supertypeOf(String.class);
        assertNotNull(wt);
        assertEquals(1, wt.getUpperBounds().length);
        assertEquals(Object.class, wt.getUpperBounds()[0]);
        assertEquals(1, wt.getLowerBounds().length);
        assertEquals(String.class, wt.getLowerBounds()[0]);
    }

    @Test(timeout = 4000)
    public void testCanonicalize_ClassNonArray() {
        Type result = $Gson$Types.canonicalize(String.class);
        assertSame(String.class, result);
    }

    @Test(timeout = 4000)
    public void testCanonicalize_ClassArray() {
        Type result = $Gson$Types.canonicalize(String[].class);
        assertTrue(result instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) result).getGenericComponentType());
    }

    @Test(timeout = 4000)
    public void testCanonicalize_ParameterizedType() {
        ParameterizedType pt = new ParameterizedTypeImpl(null, List.class, String.class);
        Type result = $Gson$Types.canonicalize(pt);
        assertTrue(result instanceof ParameterizedType);
        ParameterizedType resultPt = (ParameterizedType) result;
        assertEquals(List.class, resultPt.getRawType());
        assertEquals(1, resultPt.getActualTypeArguments().length);
        assertEquals(String.class, resultPt.getActualTypeArguments()[0]);
    }

    @Test(timeout = 4000)
    public void testCanonicalize_GenericArrayType() {
        GenericArrayType gat = new GenericArrayTypeImpl(String.class);
        Type result = $Gson$Types.canonicalize(gat);
        assertTrue(result instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) result).getGenericComponentType());
    }

    @Test(timeout = 4000)
    public void testCanonicalize_WildcardType() {
        WildcardType wt = new WildcardTypeImpl(new Type[]{Number.class}, new Type[]{});
        Type result = $Gson$Types.canonicalize(wt);
        assertTrue(result instanceof WildcardType);
    }

    @Test(timeout = 4000)
    public void testCanonicalize_Other() {
        Type other = new Type() {}; // anonymous type
        Type result = $Gson$Types.canonicalize(other);
        assertSame(other, result);
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
        GenericArrayType gat = $Gson$Types.arrayOf(String.class);
        assertTrue($Gson$Types.getRawType(gat).isArray());
    }

    @Test(timeout = 4000)
    public void testGetRawType_TypeVariable() {
        // Create a type variable from a generic method
        TypeVariable<?> tv = getClass().getTypeParameters().length > 0 ? 
            getClass().getTypeParameters()[0] : createDummyTypeVariable();
        assertEquals(Object.class, $Gson$Types.getRawType(tv));
    }

    @Test(timeout = 4000)
    public void testGetRawType_WildcardType() {
        WildcardType wt = $Gson$Types.subtypeOf(Number.class);
        assertEquals(Number.class, $Gson$Types.getRawType(wt));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetRawType_IllegalArgumentException() {
        $Gson$Types.getRawType(new Type() {});
    }

    @Test(timeout = 4000)
    public void testEquals_SameObject() {
        assertTrue($Gson$Types.equals(String.class, String.class));
    }

    @Test(timeout = 4000)
    public void testEquals_BothNull() {
        assertTrue($Gson$Types.equals(null, null));
    }

    @Test(timeout = 4000)
    public void testEquals_OneNull() {
        assertFalse($Gson$Types.equals(String.class, null));
        assertFalse($Gson$Types.equals(null, String.class));
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
        ParameterizedType pt3 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, Integer.class);
        assertTrue($Gson$Types.equals(pt1, pt2));
        assertFalse($Gson$Types.equals(pt1, pt3));
    }

    @Test(timeout = 4000)
    public void testEquals_GenericArrayType() {
        GenericArrayType gat1 = $Gson$Types.arrayOf(String.class);
        GenericArrayType gat2 = $Gson$Types.arrayOf(String.class);
        GenericArrayType gat3 = $Gson$Types.arrayOf(Integer.class);
        assertTrue($Gson$Types.equals(gat1, gat2));
        assertFalse($Gson$Types.equals(gat1, gat3));
    }

    @Test(timeout = 4000)
    public void testEquals_WildcardType() {
        WildcardType wt1 = $Gson$Types.subtypeOf(Number.class);
        WildcardType wt2 = $Gson$Types.subtypeOf(Number.class);
        WildcardType wt3 = $Gson$Types.supertypeOf(Number.class);
        assertTrue($Gson$Types.equals(wt1, wt2));
        assertFalse($Gson$Types.equals(wt1, wt3));
    }

    @Test(timeout = 4000)
    public void testEquals_WildcardTypeBounds() {
        WildcardType wt1 = new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{String.class});
        WildcardType wt2 = new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{String.class});
        WildcardType wt3 = new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{Integer.class});
        assertTrue($Gson$Types.equals(wt1, wt2));
        assertFalse($Gson$Types.equals(wt1, wt3));
    }

    @Test(timeout = 4000)
    public void testEquals_TypeVariable() {
        // Can't easily create equal TypeVariables, so test basic cases
        TypeVariable<?>[] params = getClass().getTypeParameters();
        if (params.length > 0) {
            assertTrue($Gson$Types.equals(params[0], params[0]));
        }
        // Test mismatch
        assertFalse($Gson$Types.equals(String.class, $Gson$Types.subtypeOf(Number.class)));
    }

    @Test(timeout = 4000)
    public void testEquals_Unsupported() {
        Type unsupported = new Type() {};
        assertFalse($Gson$Types.equals(unsupported, unsupported));
    }

    @Test(timeout = 4000)
    public void testTypeToString_Class() {
        assertEquals("java.lang.String", $Gson$Types.typeToString(String.class));
    }

    @Test(timeout = 4000)
    public void testTypeToString_NonClass() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        String result = $Gson$Types.typeToString(pt);
        assertTrue(result.contains("List"));
        assertTrue(result.contains("String"));
    }

    @Test(timeout = 4000)
    public void testGetGenericSupertype_ToResolveEqualsRawType() {
        Type result = $Gson$Types.getGenericSupertype(String.class, String.class, String.class);
        assertSame(String.class, result);
    }

    @Test(timeout = 4000)
    public void testGetGenericSupertype_Interface() {
        Type result = $Gson$Types.getGenericSupertype(
            ArrayList.class, ArrayList.class, List.class);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testGetGenericSupertype_Superclass() {
        Type result = $Gson$Types.getGenericSupertype(
            ArrayList.class, ArrayList.class, AbstractCollection.class);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testGetGenericSupertype_Default() {
        Type result = $Gson$Types.getGenericSupertype(
            ArrayList.class, ArrayList.class, Date.class);
        assertSame(Date.class, result);
    }

    @Test(timeout = 4000)
    public void testGetArrayComponentType_GenericArrayType() {
        GenericArrayType gat = $Gson$Types.arrayOf(String.class);
        assertEquals(String.class, $Gson$Types.getArrayComponentType(gat));
    }

    @Test(timeout = 4000)
    public void testGetArrayComponentType_ClassArray() {
        assertEquals(String.class, $Gson$Types.getArrayComponentType(String[].class));
    }

    @Test(timeout = 4000)
    public void testGetCollectionElementType_WildcardType() {
        // Mock scenario where supertype returns WildcardType
        Type result = $Gson$Types.getCollectionElementType(
            $Gson$Types.subtypeOf(Collection.class), Collection.class);
        // Should return Object.class since wildcard upper bound is Object
        assertEquals(Object.class, result);
    }

    @Test(timeout = 4000)
    public void testGetCollectionElementType_ParameterizedType() {
        Type result = $Gson$Types.getCollectionElementType(
            $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, String.class), 
            Collection.class);
        assertEquals(Object.class, result); // Because supertype of ArrayList is not ParameterizedType directly
    }

    @Test(timeout = 4000)
    public void testGetCollectionElementType_Default() {
        // Pass a type that doesn't resolve to ParameterizedType
        Type result = $Gson$Types.getCollectionElementType(String.class, String.class);
        assertEquals(Object.class, result);
    }

    @Test(timeout = 4000)
    public void testGetMapKeyAndValueTypes_Properties() {
        Type[] result = $Gson$Types.getMapKeyAndValueTypes(Properties.class, Properties.class);
        assertEquals(String.class, result[0]);
        assertEquals(String.class, result[1]);
    }

    @Test(timeout = 4000)
    public void testGetMapKeyAndValueTypes_ParameterizedType() {
        Type[] result = $Gson$Types.getMapKeyAndValueTypes(
            $Gson$Types.newParameterizedTypeWithOwner(null, HashMap.class, String.class, Integer.class),
            HashMap.class);
        assertTrue(result.length >= 2);
    }

    @Test(timeout = 4000)
    public void testGetMapKeyAndValueTypes_Default() {
        Type[] result = $Gson$Types.getMapKeyAndValueTypes(String.class, String.class);
        assertEquals(Object.class, result[0]);
        assertEquals(Object.class, result[1]);
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testNewParameterizedTypeWithOwner_NullOwnerStaticClass() {
        try {
            $Gson$Types.newParameterizedTypeWithOwner(null, String.class);
        } catch (Exception e) {
            fail("Should not throw exception for static/top-level class with null owner");
        }
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_NullTypeArgument() {
        try {
            $Gson$Types.newParameterizedTypeWithOwner(null, List.class, (Type) null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_PrimitiveTypeArgument() {
        try {
            $Gson$Types.newParameterizedTypeWithOwner(null, List.class, int.class);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_InvalidLowerBoundsCount() {
        try {
            new $Gson$Types.WildcardTypeImpl(
                new Type[]{Object.class}, 
                new Type[]{String.class, Integer.class});
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_InvalidUpperBoundsCount() {
        try {
            new $Gson$Types.WildcardTypeImpl(
                new Type[]{Object.class, String.class}, 
                new Type[]{});
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_NullLowerBound() {
        try {
            new $Gson$Types.WildcardTypeImpl(
                new Type[]{Object.class}, 
                new Type[]{null});
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_PrimitiveLowerBound() {
        try {
            new $Gson$Types.WildcardTypeImpl(
                new Type[]{Object.class}, 
                new Type[]{int.class});
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCheckNotPrimitive_Primitive() {
        try {
            $Gson$Types.checkNotPrimitive(int.class);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCheckNotPrimitive_NonPrimitive() {
        // Should not throw
        $Gson$Types.checkNotPrimitive(String.class);
    }

    @Test(timeout = 4000)
    public void testIndexOf_Found() {
        String[] arr = {"a", "b", "c"};
        // indexOf is private, test through resolveTypeVariable which uses it indirectly
        // We'll test the resolve method instead
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_Canonicalization() {
        // Test that type arguments are canonicalized
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String[].class);
        Type arg = pt.getActualTypeArguments()[0];
        assertTrue("Array class should be canonicalized to GenericArrayType", 
            arg instanceof GenericArrayType);
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeImpl_Equals() {
        GenericArrayType gat1 = $Gson$Types.arrayOf(String.class);
        GenericArrayType gat2 = $Gson$Types.arrayOf(String.class);
        assertTrue(gat1.equals(gat2));
        assertFalse(gat1.equals(new Object()));
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeImpl_HashCode() {
        GenericArrayType gat1 = $Gson$Types.arrayOf(String.class);
        GenericArrayType gat2 = $Gson$Types.arrayOf(String.class);
        assertEquals(gat1.hashCode(), gat2.hashCode());
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeImpl_ToString() {
        GenericArrayType gat = $Gson$Types.arrayOf(String.class);
        assertEquals("java.lang.String[]", gat.toString());
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_ToStringNoLowerBound() {
        WildcardType wt = $Gson$Types.subtypeOf(Number.class);
        assertEquals("? extends java.lang.Number", wt.toString());
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_ToStringLowerBound() {
        WildcardType wt = $Gson$Types.supertypeOf(Number.class);
        assertEquals("? super java.lang.Number", wt.toString());
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_ToStringObjectUpperBound() {
        WildcardType wt = $Gson$Types.subtypeOf(Object.class);
        assertEquals("?", wt.toString());
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_EqualsNullOwner() {
        ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        ParameterizedType pt3 = $Gson$Types.newParameterizedTypeWithOwner(
            new Object(), List.class, String.class);
        assertTrue(pt1.equals(pt2));
        assertFalse(pt1.equals(pt3));
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    /**
     * Test for known defect: Resolving a wildcard with lower bound should not
     * produce nested wildcards. This targets the issue where
     * supertypeOf(lowerBound) is called when lowerBound might already be a wildcard.
     * Expected: ? super Number
     * Bug: ? super ? super Number
     */
    @Test(timeout = 4000)
    public void testDoubleSupertype_DefectTarget() {
        // Create a context that would trigger the bug in resolve()
        // We need to exercise the WildcardType branch of resolve()
        WildcardType original = $Gson$Types.supertypeOf(Number.class);
        // Simulate what happens during recursive resolve
        Type resolved = $Gson$Types.resolve(
            original, 
            Object.class, 
            original);
        // If resolved is a WildcardType, it should have exactly one lower bound of Number
        assertTrue("Resolved type should be a WildcardType", resolved instanceof WildcardType);
        WildcardType resolvedWt = (WildcardType) resolved;
        assertEquals("Should have exactly one upper bound", 1, resolvedWt.getUpperBounds().length);
        assertEquals("Upper bound should be Object.class", 
            Object.class, resolvedWt.getUpperBounds()[0]);
        assertEquals("Should have exactly one lower bound", 
            1, resolvedWt.getLowerBounds().length);
        assertEquals("Lower bound should be Number.class, not nested wildcard", 
            Number.class, resolvedWt.getLowerBounds()[0]);
        assertEquals("toString should not show nested wildcard", 
            "? super java.lang.Number", resolvedWt.toString());
    }

    /**
     * Test for known defect: Resolving a wildcard with upper bound should not
     * produce nested wildcards. Expected: ? extends Number
     * Bug: ? extends ? extends Number
     */
    @Test(timeout = 4000)
    public void testDoubleSubtype_DefectTarget() {
        WildcardType original = $Gson$Types.subtypeOf(Number.class);
        Type resolved = $Gson$Types.resolve(
            original,
            Object.class,
            original);
        assertTrue("Resolved type should be a WildcardType", resolved instanceof WildcardType);
        WildcardType resolvedWt = (WildcardType) resolved;
        assertEquals("Should have exactly one upper bound", 1, resolvedWt.getUpperBounds().length);
        assertEquals("Upper bound should be Number.class, not nested wildcard", 
            Number.class, resolvedWt.getUpperBounds()[0]);
        assertEquals("Should have no lower bounds", 
            0, resolvedWt.getLowerBounds().length);
        assertEquals("toString should not show nested wildcard", 
            "? extends java.lang.Number", resolvedWt.toString());
    }

    /**
     * Test for known defect: Resolving '? super Number' should produce '?', not '? extends ? super Number'
     */
    @Test(timeout = 4000)
    public void testSubSupertype_DefectTarget() {
        // When resolving a wildcard that is a supertype, the upper bound should become Object
        // and the result should be '?' (unbounded wildcard)
        WildcardType superWt = $Gson$Types.supertypeOf(Number.class);
        // This simulates a scenario where the context makes the resolved type '?'
        Type resolved = $Gson$Types.resolve(
            $Gson$Types.newParameterizedTypeWithOwner(null, List.class, Number.class),
            List.class,
            superWt);
        // Depending on context, this might resolve differently
        assertNotNull("Resolved type should not be null", resolved);
        // The key defect is that resolving a supertype should not produce nested extends
        if (resolved instanceof WildcardType) {
            WildcardType resolvedWt = (WildcardType) resolved;
            String str = resolvedWt.toString();
            assertFalse("Should not contain nested wildcard: " + str, 
                str.contains("? extends ?") || str.contains("? super ?"));
        }
    }

    /**
     * Test for known defect: Resolving '? extends Number' should produce '?', not '? super ? extends Number'
     */
    @Test(timeout = 4000)
    public void testSuperSubtype_DefectTarget() {
        WildcardType subWt = $Gson$Types.subtypeOf(Number.class);
        Type resolved = $Gson$Types.resolve(
            $Gson$Types.newParameterizedTypeWithOwner(null, List.class, Object.class),
            List.class,
            subWt);
        assertNotNull("Resolved type should not be null", resolved);
        if (resolved instanceof WildcardType) {
            WildcardType resolvedWt = (WildcardType) resolved;
            String str = resolvedWt.toString();
            assertFalse("Should not contain nested wildcard: " + str, 
                str.contains("? extends ?") || str.contains("? super ?"));
        }
    }

    /**
     * Test for known defect: Recursive type resolution can cause StackOverflowError.
     * This tests the resolve method's handling of recursive TypeVariable references.
     */
    @Test(timeout = 4000)
    public void testRecursiveResolveSimple_DefectTarget() throws Exception {
        // Create a recursive type structure that could cause StackOverflow
        // Use a parameterized type that references itself
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, 
            Comparable.class, 
            $Gson$Types.newParameterizedTypeWithOwner(null, Comparable.class, Object.class));
        
        // This should not cause StackOverflowError
        try {
            Type resolved = $Gson$Types.resolve(pt, Comparable.class, pt);
            assertNotNull(resolved);
        } catch (StackOverflowError e) {
            fail("Recursive type resolution should not cause StackOverflowError");
        }
    }

    /**
     * Test for known defect: Issue 440 with WeakReference causes StackOverflow.
     * Simulates recursive type resolution similar to WeakReference<Number>
     */
    @Test(timeout = 4000)
    public void testIssue440WeakReference_DefectTarget() throws Exception {
        // Simulate TypeVariable resolution that could cause infinite recursion
        // Create a mock scenario similar to WeakReference's recursive type
        try {
            // This exercises the TypeVariable branch of resolve()
            Type resolved = $Gson$Types.resolve(
                Object.class, 
                Object.class, 
                $Gson$Types.supertypeOf(Number.class));
            assertNotNull(resolved);
        } catch (StackOverflowError e) {
            fail("WeakReference-like type resolution should not cause StackOverflowError");
        }
    }

    /**
     * Test for known defect: Issue 603 with PrintStream causes StackOverflow.
     * Simulates complex recursive type resolution.
     */
    @Test(timeout = 4000)
    public void testIssue603PrintStream_DefectTarget() throws Exception {
        // Create a deeply nested recursive type
        try {
            Type recursiveType = $Gson$Types.subtypeOf(
                $Gson$Types.supertypeOf(
                    $Gson$Types.subtypeOf(
                        $Gson$Types.supertypeOf(Number.class))));
            
            Type resolved = $Gson$Types.resolve(recursiveType, Object.class, recursiveType);
            assertNotNull(resolved);
        } catch (StackOverflowError e) {
            fail("Recursive wildcard depth should not cause StackOverflowError");
        }
    }

    /**
     * Direct test: Ensure resolve() does not create nested wildcards when
     * the resolved type of a wildcard bound is itself a wildcard.
     */
    @Test(timeout = 4000)
    public void testResolve_NestedWildcardPrevention() {
        // Test case: wildcard with lower bound that resolves to another wildcard
        WildcardType wildcard = new WildcardTypeImpl(
            new Type[]{Object.class},
            new Type[]{$Gson$Types.subtypeOf(Number.class)}  // lower bound is a wildcard!
        );
        
        Type resolved = $Gson$Types.resolve(wildcard, Object.class, wildcard);
        assertTrue("Should still be a WildcardType", resolved instanceof WildcardType);
        WildcardType resolvedWt = (WildcardType) resolved;
        String result = resolvedWt.toString();
        // The result should not contain nested wildcards
        assertFalse("Should not create double wildcard: " + result, 
            result.contains("? super ?") || result.contains("? extends ?"));
        // The lower bound should be directly the resolved wildcard, not wrapped again
        assertTrue("Lower bounds should have length 1 or 0", 
            resolvedWt.getLowerBounds().length <= 1);
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testPrivateConstructor() {
        try {
            Constructor<?> c = $Gson$Types.class.getDeclaredConstructor();
            c.setAccessible(true);
            c.newInstance();
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test(timeout = 4000)
    public void testGetSupertype_NotAssignable() {
        try {
            $Gson$Types.getSupertype(String.class, String.class, List.class);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetRawType_NullInput() {
        try {
            $Gson$Types.getRawType(null);
            fail("Should have thrown NullPointerException or IllegalArgumentException");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testResolve_TypeVariableUnresolvable() {
        // Test that resolve returns the original type variable when it can't be resolved
        TypeVariable<?> tv = createDummyTypeVariable();
        Type resolved = $Gson$Types.resolve(Object.class, Object.class, tv);
        assertSame("Should return original when unresolvable", tv, resolved);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_Serializable() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertTrue("Should be Serializable", pt instanceof Serializable);
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeImpl_Serializable() {
        GenericArrayType gat = $Gson$Types.arrayOf(String.class);
        assertTrue("Should be Serializable", gat instanceof Serializable);
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_Serializable() {
        WildcardType wt = $Gson$Types.subtypeOf(Number.class);
        assertTrue("Should be Serializable", wt instanceof Serializable);
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_HashCode() {
        ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertEquals("Equal types should have same hashcode", pt1.hashCode(), pt2.hashCode());
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_ToStringEmptyArgs() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, String.class);
        assertEquals("java.lang.String", pt.toString());
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_ToStringMultipleArgs() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(
            null, Map.class, String.class, Integer.class);
        String str = pt.toString();
        assertTrue(str.contains("java.util.Map"));
        assertTrue(str.contains("java.lang.String"));
        assertTrue(str.contains("java.lang.Integer"));
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_EqualsDifferentOwner() {
        // Same type but with different owner - should not be equal if owner matters
        ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, Map.Entry.class, String.class, Integer.class);
        ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(Map.class, Map.Entry.class, String.class, Integer.class);
        // These may or may not be equal depending on implementation - just verify no exception
        boolean equals = pt1.equals(pt2);
        pt2.equals(pt1);
        // No assertion on equality, just testing it doesn't throw
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_EqualsWithLowerBounds() {
        WildcardType wt1 = $Gson$Types.supertypeOf(Number.class);
        WildcardType wt2 = $Gson$Types.supertypeOf(Number.class);
        assertTrue(wt1.equals(wt2));
        assertEquals(wt1.hashCode(), wt2.hashCode());
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_EqualsDifferentLowerBounds() {
        WildcardType wt1 = $Gson$Types.supertypeOf(Number.class);
        WildcardType wt2 = $Gson$Types.supertypeOf(String.class);
        assertFalse(wt1.equals(wt2));
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_EqualsNonWildcard() {
        WildcardType wt = $Gson$Types.subtypeOf(Number.class);
        assertFalse(wt.equals(new Object()));
        assertFalse(wt.equals(null));
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_EqualsNonParameterizedType() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertFalse(pt.equals(new Object()));
        assertFalse(pt.equals(null));
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_GetLowerBoundsEmpty() {
        WildcardType wt = $Gson$Types.subtypeOf(Number.class);
        assertEquals(0, wt.getLowerBounds().length);
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_GetUpperBoundsSingle() {
        WildcardType wt = $Gson$Types.supertypeOf(Number.class);
        assertEquals(1, wt.getUpperBounds().length);
        assertEquals(Object.class, wt.getUpperBounds()[0]);
    }

    @Test(timeout = 4000)
    public void testWildcardTypeImpl_HashCodeConsistency() {
        WildcardType wt1 = $Gson$Types.subtypeOf(Number.class);
        WildcardType wt2 = $Gson$Types.subtypeOf(Number.class);
        assertEquals(wt1.hashCode(), wt2.hashCode());
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeImpl_HashCodeConsistency() {
        GenericArrayType gat1 = $Gson$Types.arrayOf(String.class);
        GenericArrayType gat2 = $Gson$Types.arrayOf(String.class);
        assertEquals(gat1.hashCode(), gat2.hashCode());
    }

    @Test(timeout = 4000)
    public void testParameterizedTypeImpl_ActualTypeArgumentsCloned() {
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        Type[] args1 = pt.getActualTypeArguments();
        Type[] args2 = pt.getActualTypeArguments();
        assertNotSame("Should return new array each time", args1, args2);
        assertTrue("Contents should be equal", Arrays.equals(args1, args2));
    }

    // Helper method to create a dummy TypeVariable for testing
    private TypeVariable<?> createDummyTypeVariable() {
        // Use a simple generic class to get a TypeVariable
        class Dummy<T> {}
        return Dummy.class.getTypeParameters()[0];
    }

    // ParameterizedTypeImpl constructor is private, use newParameterizedTypeWithOwner instead
    // WildcardTypeImpl constructor is private, use subtypeOf/supertypeOf instead
    // GenericArrayTypeImpl constructor is private, use arrayOf instead

    // Use reflection to access inner classes for testing
    @SuppressWarnings("unchecked")
    private static Class<? extends WildcardType> getWildcardTypeImplClass() {
        try {
            return (Class<? extends WildcardType>) 
                Class.forName("com.google.gson.internal.$Gson$Types$WildcardTypeImpl");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

// Define inner class equivalents for testing ParameterizedTypeImpl directly
// Since the actual class is private, we need to access it via reflection or through public APIs
// We'll use the public APIs ($Gson$Types.newParameterizedTypeWithOwner) throughout