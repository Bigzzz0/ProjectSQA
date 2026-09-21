package org.apache.commons.lang3.reflect;

import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * 1. DEFECT-TARGETED BRANCHES (Defects4J Ground Truth):
 *    - TypeUtils.getTypeArguments(Class<?> cls, Class<?> toClass, Map<TypeVariable<?>, Type> subtypeVarAssigns):
 *      Faulty check: `if (cls.getTypeParameters().length > 0 || toClass.equals(cls))` prematurely returns an empty
 *      typeVarAssigns map whenever `cls` declares any type parameters, even if `toClass` has not been reached yet!
 *    - TypeUtilsTest::testGetTypeArguments: expects size 2 when resolving TheOther.class to This.class, but got 0.
 *    - TypeUtilsTest::testIsAssignable: expects Thing.class to be assignable to This<String, String>, but failed.
 *
 * 2. CORE DECISION BRANCHES & CONDITION COMBINATORICS:
 *    - isAssignable(Type, Type):
 *      * toType is null vs Class<?> vs ParameterizedType vs GenericArrayType vs WildcardType vs TypeVariable<?> vs Unhandled Type.
 *    - isAssignable(Type, Class<?>):
 *      * type == null (primitive vs non-primitive toClass).
 *      * toClass == null, toClass.equals(type).
 *      * type instanceof Class<?>, ParameterizedType, TypeVariable<?>, GenericArrayType, WildcardType.
 *    - isAssignable(Type, ParameterizedType):
 *      * type == null, toParameterizedType == null, exact match.
 *      * fromTypeVarAssigns == null vs empty vs populated.
 *      * Type argument exact match, wildcard assignable, and mismatch.
 *    - isAssignable(Type, GenericArrayType):
 *      * type == null, toGenericArrayType == null, exact match.
 *      * type instanceof Class<?> (array vs non-array), GenericArrayType, WildcardType, TypeVariable<?>, ParameterizedType.
 *    - isAssignable(Type, WildcardType):
 *      * upper and lower bound satisfaction, Wildcard-to-Wildcard, Non-Wildcard-to-Wildcard.
 *    - isAssignable(Type, TypeVariable<?>):
 *      * type == null, toTypeVariable == null, exact match, bounded descendant, incompatible types.
 *    - getTypeArguments(Type, Class<?>):
 *      * Class, ParameterizedType (with/without owner type), GenericArrayType, WildcardType, TypeVariable.
 *    - determineTypeArguments(Class<?>, ParameterizedType):
 *      * compatibility checks, exact class equality, multi-step hierarchy traversal.
 *    - normalizeUpperBounds:
 *      * bounds length < 2, bounds with subtype redundancy, bounds with independent types.
 *    - getRawType(Type, Type):
 *      * Class, ParameterizedType, TypeVariable (with assigning type context), GenericArrayType, WildcardType.
 *    - isArrayType / getArrayComponentType:
 *      * Class arrays, GenericArrayTypes, non-arrays, primitives, nulls.
 *    - typesSatisfyVariables:
 *      * Bound satisfaction, failure on mismatched types, missing variable assignment exception.
 */
public class TypeUtilsGeminiTest {

    // =========================================================================
    // Reflection Test Fixtures
    // =========================================================================

    public interface This<K, V> {}
    public static class That<K, V> implements This<K, V> {}
    public static class TheOther<B> extends That<String, String> {}
    public static class Thing extends TheOther<String> {}

    public This<String, String> dis;

    public static class Outer<A> {
        public class Inner<B> {}
    }

    public Outer<String>.Inner<Integer> innerField;

    public static class TestFixture<T extends Number, U extends Comparable<U>> {
        public List<String> stringList;
        public List<Integer> intList;
        public List<Number> numberList;
        public List<? extends Number> wildcardExtendsNumberList;
        public List<? super Integer> wildcardSuperIntList;
        public List<?> wildcardList;
        public List<String>[] stringListArray;
        public T[] tArray;
        public String[] stringArray;
        public int[] intArray;
        public T tVar;
        public U uVar;
        public Map<String, Integer> mapStringInt;
    }

    public static class TestFixtureSub extends TestFixture<Integer, String> {}

    public static class DependentFixture<T, S extends T> {}

    public static class GenericCollectionHolder {
        public Iterable<String> iterableString;
    }

    public static class CustomType implements Type {
        @Override
        public String getTypeName() {
            return "CustomType";
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect where getTypeArguments(TheOther.class, This.class) returns 0 instead of 2.
     * In the defective implementation, `if (cls.getTypeParameters().length > 0)` triggered prematurely
     * and aborted hierarchy traversal before reaching This.class.
     */
    @Test(timeout = 4000)
    public void testDefectGetTypeArgumentsInheritanceHierarchy() {
        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.getTypeArguments(TheOther.class, This.class);
        assertNotNull("Type arguments map should not be null", typeArgs);
        assertEquals("Should extract both type arguments K and V from This<String, String>", 2, typeArgs.size());

        for (Type value : typeArgs.values()) {
            assertEquals("All type arguments must resolve to String.class", String.class, value);
        }
    }

    /**
     * Targets the defect where Thing.class is reported as not assignable to This<String, String>.
     */
    @Test(timeout = 4000)
    public void testDefectIsAssignableInheritanceHierarchy() throws NoSuchFieldException {
        Type toType = TypeUtilsGeminiTest.class.getField("dis").getGenericType();
        assertTrue("Thing.class must be assignable to This<String, String>",
                TypeUtils.isAssignable(Thing.class, toType));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsAssignableClassToClass() {
        assertTrue(TypeUtils.isAssignable(String.class, Object.class));
        assertTrue(TypeUtils.isAssignable(Integer.class, Number.class));
        assertFalse(TypeUtils.isAssignable(Number.class, Integer.class));

        // Autoboxing / Unboxing via ClassUtils
        assertTrue(TypeUtils.isAssignable(int.class, Integer.class));
        assertTrue(TypeUtils.isAssignable(Integer.class, int.class));
        assertTrue(TypeUtils.isAssignable(int.class, long.class));
    }

    @Test(timeout = 4000)
    public void testIsAssignableParameterizedTypeToClass() throws Exception {
        Type stringListType = TestFixture.class.getField("stringList").getGenericType();
        assertTrue(TypeUtils.isAssignable(stringListType, List.class));
        assertTrue(TypeUtils.isAssignable(stringListType, Collection.class));
        assertTrue(TypeUtils.isAssignable(stringListType, Object.class));
        assertFalse(TypeUtils.isAssignable(stringListType, Map.class));
    }

    @Test(timeout = 4000)
    public void testIsAssignableTypeVariableToClass() {
        TypeVariable<?> tVar = TestFixture.class.getTypeParameters()[0];
        assertTrue(TypeUtils.isAssignable(tVar, Number.class));
        assertTrue(TypeUtils.isAssignable(tVar, Object.class));
        assertFalse(TypeUtils.isAssignable(tVar, Integer.class));
    }

    @Test(timeout = 4000)
    public void testIsAssignableGenericArrayTypeToClass() throws Exception {
        Type stringListArrayType = TestFixture.class.getField("stringListArray").getGenericType();
        assertTrue(TypeUtils.isAssignable(stringListArrayType, Object.class));
        assertTrue(TypeUtils.isAssignable(stringListArrayType, List[].class));
        assertTrue(TypeUtils.isAssignable(stringListArrayType, Collection[].class));
        assertFalse(TypeUtils.isAssignable(stringListArrayType, List.class));
        assertFalse(TypeUtils.isAssignable(stringListArrayType, String[].class));
    }

    @Test(timeout = 4000)
    public void testIsAssignableWildcardToClass() throws Exception {
        ParameterizedType pt = (ParameterizedType) TestFixture.class.getField("wildcardExtendsNumberList").getGenericType();
        WildcardType wildcardType = (WildcardType) pt.getActualTypeArguments()[0];
        assertFalse("Wildcard types are not assignable to a class directly",
                TypeUtils.isAssignable(wildcardType, Object.class));
    }

    @Test(timeout = 4000)
    public void testIsAssignableParameterizedTypeToParameterizedType() throws Exception {
        Type stringListType = TestFixture.class.getField("stringList").getGenericType();
        Type intListType = TestFixture.class.getField("intList").getGenericType();
        Type wildcardExtendsNumberListType = TestFixture.class.getField("wildcardExtendsNumberList").getGenericType();
        Type wildcardSuperIntListType = TestFixture.class.getField("wildcardSuperIntList").getGenericType();
        Type numberListType = TestFixture.class.getField("numberList").getGenericType();

        // Exact match
        assertTrue(TypeUtils.isAssignable(stringListType, stringListType));
        assertFalse(TypeUtils.isAssignable(stringListType, intListType));

        // Raw type to parameterized type
        assertTrue(TypeUtils.isAssignable(List.class, stringListType));

        // Incompatible raw types
        assertFalse(TypeUtils.isAssignable(Map.class, stringListType));

        // Wildcard bounds checking
        assertTrue(TypeUtils.isAssignable(intListType, wildcardExtendsNumberListType));
        assertFalse(TypeUtils.isAssignable(stringListType, wildcardExtendsNumberListType));

        assertTrue(TypeUtils.isAssignable(intListType, wildcardSuperIntListType));
        assertTrue(TypeUtils.isAssignable(numberListType, wildcardSuperIntListType));
    }

    @Test(timeout = 4000)
    public void testIsAssignableGenericArrayTypeToGenericArrayType() throws Exception {
        Type stringListArrayType = TestFixture.class.getField("stringListArray").getGenericType();
        Type tArrayType = TestFixture.class.getField("tArray").getGenericType();

        assertTrue(TypeUtils.isAssignable(stringListArrayType, stringListArrayType));
        assertTrue(TypeUtils.isAssignable(tArrayType, tArrayType));

        // Class array to GenericArrayType
        assertTrue(TypeUtils.isAssignable(Number[].class, tArrayType));
        assertFalse(TypeUtils.isAssignable(String[].class, tArrayType));
        assertFalse(TypeUtils.isAssignable(String.class, tArrayType));

        // ParameterizedType to GenericArrayType is always false
        Type stringListType = TestFixture.class.getField("stringList").getGenericType();
        assertFalse(TypeUtils.isAssignable(stringListType, stringListArrayType));
    }

    @Test(timeout = 4000)
    public void testIsAssignableWildcardType() throws Exception {
        ParameterizedType ptExtends = (ParameterizedType) TestFixture.class.getField("wildcardExtendsNumberList").getGenericType();
        WildcardType wcExtNumber = (WildcardType) ptExtends.getActualTypeArguments()[0];

        ParameterizedType ptSuper = (ParameterizedType) TestFixture.class.getField("wildcardSuperIntList").getGenericType();
        WildcardType wcSuperInt = (WildcardType) ptSuper.getActualTypeArguments()[0];

        ParameterizedType ptAll = (ParameterizedType) TestFixture.class.getField("wildcardList").getGenericType();
        WildcardType wcAll = (WildcardType) ptAll.getActualTypeArguments()[0];

        // Self-equality
        assertTrue(TypeUtils.isAssignable(wcExtNumber, wcExtNumber));
        assertTrue(TypeUtils.isAssignable(wcSuperInt, wcSuperInt));

        // Wildcard to Wildcard
        assertTrue(TypeUtils.isAssignable(wcExtNumber, wcAll));
        assertFalse(TypeUtils.isAssignable(wcAll, wcExtNumber));

        // Class to Wildcard
        assertTrue(TypeUtils.isAssignable(Integer.class, wcExtNumber));
        assertFalse(TypeUtils.isAssignable(String.class, wcExtNumber));

        assertTrue(TypeUtils.isAssignable(Number.class, wcSuperInt));
        assertTrue(TypeUtils.isAssignable(Object.class, wcSuperInt));
        assertFalse(TypeUtils.isAssignable(Double.class, wcSuperInt));
    }

    @Test(timeout = 4000)
    public void testIsAssignableTypeVariable() {
        TypeVariable<?> tVar = TestFixture.class.getTypeParameters()[0];
        TypeVariable<?> uVar = TestFixture.class.getTypeParameters()[1];

        assertTrue(TypeUtils.isAssignable(tVar, tVar));
        assertFalse(TypeUtils.isAssignable(tVar, uVar));

        // Class, ParameterizedType, etc. are not assignable to a type variable
        assertFalse(TypeUtils.isAssignable(Number.class, tVar));
        assertFalse(TypeUtils.isAssignable(Integer.class, tVar));
    }

    @Test(timeout = 4000)
    public void testGetTypeArgumentsOwnerHierarchy() throws Exception {
        Field field = TypeUtilsGeminiTest.class.getField("innerField");
        ParameterizedType pt = (ParameterizedType) field.getGenericType();

        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.getTypeArguments(pt);
        assertNotNull(typeArgs);
        assertEquals(2, typeArgs.size());

        TypeVariable<?> varA = Outer.class.getTypeParameters()[0];
        TypeVariable<?> varB = Outer.Inner.class.getTypeParameters()[0];

        assertEquals(String.class, typeArgs.get(varA));
        assertEquals(Integer.class, typeArgs.get(varB));
    }

    @Test(timeout = 4000)
    public void testDetermineTypeArguments() throws Exception {
        ParameterizedType iterablePt = (ParameterizedType) GenericCollectionHolder.class.getField("iterableString").getGenericType();

        Map<TypeVariable<?>, Type> assigns = TypeUtils.determineTypeArguments(ArrayList.class, iterablePt);
        assertNotNull(assigns);
        TypeVariable<?> arrayListVar = ArrayList.class.getTypeParameters()[0];
        assertEquals(String.class, assigns.get(arrayListVar));

        // Exact match
        Map<TypeVariable<?>, Type> directAssigns = TypeUtils.determineTypeArguments(Iterable.class, iterablePt);
        assertNotNull(directAssigns);
        TypeVariable<?> iterableVar = Iterable.class.getTypeParameters()[0];
        assertEquals(String.class, directAssigns.get(iterableVar));

        // Incompatible types
        assertNull(TypeUtils.determineTypeArguments(String.class, iterablePt));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsAssignableNullHandling() throws Exception {
        // null target
        assertTrue(TypeUtils.isAssignable(null, (Type) null));
        assertTrue(TypeUtils.isAssignable(null, (Class<?>) null));
        assertFalse(TypeUtils.isAssignable(String.class, (Type) null));
        assertFalse(TypeUtils.isAssignable(String.class, (Class<?>) null));

        // null subject
        assertTrue(TypeUtils.isAssignable(null, String.class));
        assertTrue(TypeUtils.isAssignable(null, Object.class));
        assertFalse(TypeUtils.isAssignable(null, int.class));
        assertFalse(TypeUtils.isAssignable(null, boolean.class));

        Type stringListType = TestFixture.class.getField("stringList").getGenericType();
        assertTrue(TypeUtils.isAssignable(null, stringListType));
        assertFalse(TypeUtils.isAssignable(stringListType, (ParameterizedType) null));

        Type stringListArrayType = TestFixture.class.getField("stringListArray").getGenericType();
        assertTrue(TypeUtils.isAssignable(null, stringListArrayType));
        assertFalse(TypeUtils.isAssignable(stringListArrayType, (GenericArrayType) null));

        ParameterizedType pt = (ParameterizedType) TestFixture.class.getField("wildcardExtendsNumberList").getGenericType();
        WildcardType wildcardType = (WildcardType) pt.getActualTypeArguments()[0];
        assertTrue(TypeUtils.isAssignable(null, wildcardType));
        assertFalse(TypeUtils.isAssignable(wildcardType, (WildcardType) null));

        TypeVariable<?> tVar = TestFixture.class.getTypeParameters()[0];
        assertTrue(TypeUtils.isAssignable(null, tVar));
        assertFalse(TypeUtils.isAssignable(tVar, (TypeVariable<?>) null));
    }

    @Test(timeout = 4000)
    public void testNormalizeUpperBounds() {
        assertNull(TypeUtils.normalizeUpperBounds(null));
        Type[] empty = new Type[0];
        assertSame(empty, TypeUtils.normalizeUpperBounds(empty));

        Type[] single = new Type[] { Number.class };
        assertSame(single, TypeUtils.normalizeUpperBounds(single));

        // Subtype redundancy: List is subtype of Collection
        Type[] redundant = new Type[] { Collection.class, List.class };
        Type[] normalized = TypeUtils.normalizeUpperBounds(redundant);
        assertEquals(1, normalized.length);
        assertEquals(List.class, normalized[0]);

        // Independent types: Number and Comparable
        Type[] independent = new Type[] { Number.class, Comparable.class };
        Type[] normIndependent = TypeUtils.normalizeUpperBounds(independent);
        assertEquals(2, normIndependent.length);
    }

    @Test(timeout = 4000)
    public void testGetImplicitBounds() throws Exception {
        TypeVariable<?> tVar = TestFixture.class.getTypeParameters()[0];
        Type[] bounds = TypeUtils.getImplicitBounds(tVar);
        assertEquals(1, bounds.length);
        assertEquals(Number.class, bounds[0]);

        class RawClass<X> {}
        TypeVariable<?> xVar = RawClass.class.getTypeParameters()[0];
        Type[] defaultBounds = TypeUtils.getImplicitBounds(xVar);
        assertEquals(1, defaultBounds.length);
        assertEquals(Object.class, defaultBounds[0]);

        ParameterizedType ptExtends = (ParameterizedType) TestFixture.class.getField("wildcardExtendsNumberList").getGenericType();
        WildcardType wcExt = (WildcardType) ptExtends.getActualTypeArguments()[0];
        Type[] upperBounds = TypeUtils.getImplicitUpperBounds(wcExt);
        assertEquals(1, upperBounds.length);
        assertEquals(Number.class, upperBounds[0]);

        Type[] lowerBounds = TypeUtils.getImplicitLowerBounds(wcExt);
        assertEquals(1, lowerBounds.length);
        assertNull(lowerBounds[0]);

        ParameterizedType ptSuper = (ParameterizedType) TestFixture.class.getField("wildcardSuperIntList").getGenericType();
        WildcardType wcSuper = (WildcardType) ptSuper.getActualTypeArguments()[0];
        Type[] lowerBoundsSuper = TypeUtils.getImplicitLowerBounds(wcSuper);
        assertEquals(1, lowerBoundsSuper.length);
        assertEquals(Integer.class, lowerBoundsSuper[0]);
    }

    @Test(timeout = 4000)
    public void testIsArrayTypeAndGetArrayComponentType() throws Exception {
        Type stringListArrayType = TestFixture.class.getField("stringListArray").getGenericType();
        Type stringListType = TestFixture.class.getField("stringList").getGenericType();

        assertTrue(TypeUtils.isArrayType(String[].class));
        assertTrue(TypeUtils.isArrayType(int[].class));
        assertTrue(TypeUtils.isArrayType(stringListArrayType));
        assertFalse(TypeUtils.isArrayType(String.class));
        assertFalse(TypeUtils.isArrayType(stringListType));
        assertFalse(TypeUtils.isArrayType(null));

        assertEquals(String.class, TypeUtils.getArrayComponentType(String[].class));
        assertEquals(int.class, TypeUtils.getArrayComponentType(int[].class));
        assertEquals(stringListType, TypeUtils.getArrayComponentType(stringListArrayType));
        assertNull(TypeUtils.getArrayComponentType(String.class));
        assertNull(TypeUtils.getArrayComponentType(null));
    }

    @Test(timeout = 4000)
    public void testIsInstance() throws Exception {
        assertFalse(TypeUtils.isInstance(null, null));
        assertFalse(TypeUtils.isInstance("hello", null));

        // null value against primitive vs non-primitive
        assertFalse(TypeUtils.isInstance(null, int.class));
        assertTrue(TypeUtils.isInstance(null, Integer.class));
        assertTrue(TypeUtils.isInstance(null, String.class));

        Type stringListType = TestFixture.class.getField("stringList").getGenericType();
        assertTrue(TypeUtils.isInstance(null, stringListType));

        assertTrue(TypeUtils.isInstance("hello", String.class));
        assertTrue(TypeUtils.isInstance("hello", Object.class));
        assertFalse(TypeUtils.isInstance("hello", Number.class));

        assertTrue(TypeUtils.isInstance(new ArrayList<String>(), List.class));
        assertTrue(TypeUtils.isInstance(new ArrayList<String>(), stringListType));
    }

    @Test(timeout = 4000)
    public void testTypesSatisfyVariables() {
        TypeVariable<?> tVar = TestFixture.class.getTypeParameters()[0];
        Map<TypeVariable<?>, Type> validMap = new HashMap<TypeVariable<?>, Type>();
        validMap.put(tVar, Integer.class);
        assertTrue(TypeUtils.typesSatisfyVariables(validMap));

        Map<TypeVariable<?>, Type> invalidMap = new HashMap<TypeVariable<?>, Type>();
        invalidMap.put(tVar, String.class);
        assertFalse(TypeUtils.typesSatisfyVariables(invalidMap));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testIsAssignableUnhandledTargetTypeThrowsException() {
        TypeUtils.isAssignable(String.class, new CustomType());
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testIsAssignableUnhandledSubjectTypeToClassThrowsException() {
        TypeUtils.isAssignable(new CustomType(), String.class);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testIsAssignableUnhandledSubjectTypeToParameterizedThrowsException() throws Exception {
        Type stringListType = TestFixture.class.getField("stringList").getGenericType();
        TypeUtils.isAssignable(new CustomType(), stringListType);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testIsAssignableUnhandledSubjectTypeToGenericArrayThrowsException() throws Exception {
        Type stringListArrayType = TestFixture.class.getField("stringListArray").getGenericType();
        TypeUtils.isAssignable(new CustomType(), stringListArrayType);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testIsAssignableUnhandledSubjectTypeToTypeVarThrowsException() {
        TypeVariable<?> tVar = TestFixture.class.getTypeParameters()[0];
        TypeUtils.isAssignable(new CustomType(), tVar);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetRawTypeUnknownTypeThrowsException() {
        TypeUtils.getRawType(new CustomType(), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTypesSatisfyVariablesMissingAssignmentThrowsException() {
        TypeVariable<?> sVar = DependentFixture.class.getTypeParameters()[1];
        Map<TypeVariable<?>, Type> assigns = Collections.<TypeVariable<?>, Type>singletonMap(sVar, String.class);
        // DependentFixture has <T, S extends T>. Mapping S without T throws IllegalArgumentException
        TypeUtils.typesSatisfyVariables(assigns);
    }

    @Test(timeout = 4000)
    public void testGetRawTypeResolutions() throws Exception {
        assertEquals(String.class, TypeUtils.getRawType(String.class, null));

        Type stringListType = TestFixture.class.getField("stringList").getGenericType();
        assertEquals(List.class, TypeUtils.getRawType(stringListType, null));

        Type stringListArrayType = TestFixture.class.getField("stringListArray").getGenericType();
        assertEquals(List[].class, TypeUtils.getRawType(stringListArrayType, null));

        ParameterizedType pt = (ParameterizedType) TestFixture.class.getField("wildcardExtendsNumberList").getGenericType();
        WildcardType wildcardType = (WildcardType) pt.getActualTypeArguments()[0];
        assertNull(TypeUtils.getRawType(wildcardType, null));

        TypeVariable<?> tVar = TestFixture.class.getTypeParameters()[0];
        assertNull(TypeUtils.getRawType(tVar, null));
        assertNull(TypeUtils.getRawType(tVar, String.class));

        // Subclass providing concrete type parameters
        assertEquals(Integer.class, TypeUtils.getRawType(tVar, TestFixtureSub.class));
    }

    @Test(timeout = 4000)
    public void testGetTypeArgumentsClassPrimitiveWidening() {
        Map<TypeVariable<?>, Type> args = TypeUtils.getTypeArguments(int.class, long.class);
        assertNotNull(args);
        assertTrue(args.isEmpty());

        assertNull(TypeUtils.getTypeArguments(String.class, List.class));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorInstantiation() {
        // Permit JavaBean usage as documented in TypeUtils
        TypeUtils instance = new TypeUtils();
        assertNotNull(instance);
    }
}