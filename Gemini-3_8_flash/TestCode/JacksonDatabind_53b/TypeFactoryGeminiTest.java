package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: TypeFactory
 *
 * Targeted Decision Branches & Boundaries:
 * 1. Singleton & Factory Instance lifecycle: defaultInstance(), withModifier(), withClassLoader(), clearCache().
 * 2. Type conversions: rawClass(), constructType() overloads (Type, TypeBindings, TypeReference, Context).
 * 3. Container & Specialized construction:
 *    - constructArrayType (Class vs JavaType)
 *    - constructCollectionType / constructRawCollectionType
 *    - constructCollectionLikeType / constructRawCollectionLikeType
 *    - constructMapType / constructRawMapType (Properties handling vs generic Map)
 *    - constructMapLikeType / constructRawMapLikeType
 *    - constructSimpleType, constructReferenceType, uncheckedSimpleType
 *    - constructParametricType / constructParametrizedType
 * 4. Reflection / Type Resolution Internals:
 *    - _findPrimitive: all 9 primitive keywords ("int", "long", "float", "double", "boolean", "byte", "char", "short", "void")
 *    - findClass: primitives vs FQCN, ContextClassLoader vs default ClassLoader, failure paths (ClassNotFoundException).
 *    - moreSpecificType: type1 null, type2 null, same raw class, assignable/subclass branch, unrelated.
 *    - findTypeParameters: matching super type, missing/unmatched super type.
 *    - constructGeneralizedType: matching raw type, super type lookup, illegal argument branch.
 *    - constructSpecializedType:
 *      * identical raw types (identity optimization)
 *      * rawBase == Object.class
 *      * non-assignable types (IllegalArgumentException)
 *      * empty bindings shortcut
 *      * well-known container shortcuts: Map (HashMap, LinkedHashMap, EnumMap, TreeMap) and Collection (ArrayList, LinkedList, HashSet, TreeSet, EnumSet)
 *      * typeParamCount == 0 shortcut
 *      * databind#1215 / databind#1173 refinement branch: baseType.isInterface() vs class refinement, key/value type preservation.
 * 5. TypeModifier pipeline: modifyType returning valid types, modifier returning null (IllegalStateException).
 * 6. Parser integration: constructFromCanonical valid and malformed.
 * 7. Recursive / Cyclic Types: ClassStack cycle detection & ResolvedRecursiveType.
 *
 * Known Defect (Defects4J / databind#1215):
 * - SubMap<V> extends HashMap<ConcreteKey, V> specializing Map<InterfaceKey, V>.
 *   Specialized Map type fails to resolve specialized key type if subclass changes parameter count or binds key.
 */
public class TypeFactoryGeminiTest {

    // Interfaces and stub classes for testing hierarchy & defect reproduction
    private interface HasUniqueId {
        String getId();
    }

    private static class UniqueId implements HasUniqueId {
        @Override
        public String getId() { return "uid"; }
    }

    private static class SpecializedKeyMap<V> extends HashMap<UniqueId, V> {
        private static final long serialVersionUID = 1L;
    }

    private static class CustomList<T> extends ArrayList<T> {
        private static final long serialVersionUID = 1L;
    }

    private static class StringCustomList extends ArrayList<String> {
        private static final long serialVersionUID = 1L;
    }

    private static class RecursiveNode {
        public RecursiveNode next;
    }

    private static class GenericHolder<T> {
        public T value;
        public List<T> list;
        public T[] array;
        public List<? extends Number> wildcardList;
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (databind#1215 / TypeRefinement)
    // =========================================================================

    /**
     * Targets databind#1215: Refinement of Map types when specializing from a base interface
     * or class with generic bounds (e.g., Map<HasUniqueId, V>) into a subclass that specifies
     * or narrows a type parameter (SpecializedKeyMap<V> extends HashMap<UniqueId, V>).
     * The specialized type must accurately reflect the narrowed key type (UniqueId).
     */
    @Test(timeout = 4000)
    public void testMapRefinementDefect1215() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructMapType(Map.class, HasUniqueId.class, Object.class);

        // Specialize Map<HasUniqueId, Object> to SpecializedKeyMap<Object> where key is bound to UniqueId
        JavaType specialized = tf.constructSpecializedType(baseType, SpecializedKeyMap.class);

        assertNotNull(specialized);
        assertEquals(SpecializedKeyMap.class, specialized.getRawClass());
        assertTrue("Specialized type must be a Map-like type", specialized.isMapLikeType());
        // Verify key type refinement: must resolve to UniqueId, not retain HasUniqueId interface
        assertEquals(UniqueId.class, specialized.getKeyType().getRawClass());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSingletonAndConfigurationLifecycle() {
        TypeFactory tf = TypeFactory.defaultInstance();
        assertNotNull(tf);
        assertSame(tf, TypeFactory.defaultInstance());

        // Cache clearing
        tf.clearCache();

        // ClassLoader configuration
        ClassLoader cl = getClass().getClassLoader();
        TypeFactory withCl = tf.withClassLoader(cl);
        assertNotSame(tf, withCl);
        assertSame(cl, withCl.getClassLoader());
        assertNull(tf.getClassLoader());

        // TypeModifier configuration
        TypeModifier mod1 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeFactory withMod = tf.withModifier(mod1);
        assertNotSame(tf, withMod);

        // Modifier null check idempotency
        TypeFactory withNullMod = withMod.withModifier(null);
        assertNotNull(withNullMod);
    }

    @Test(timeout = 4000)
    public void testConstructTypeFromVariousSources() {
        TypeFactory tf = TypeFactory.defaultInstance();

        // 1. Primitive and Well-known types
        JavaType intType = tf.constructType(int.class);
        assertEquals(int.class, intType.getRawClass());
        assertTrue(intType.isPrimitive());

        JavaType strType = tf.constructType(String.class);
        assertEquals(String.class, strType.getRawClass());

        JavaType objType = tf.constructType(Object.class);
        assertEquals(Object.class, objType.getRawClass());

        // 2. TypeReference
        JavaType listRefType = tf.constructType(new TypeReference<List<String>>() {});
        assertTrue(listRefType.isCollectionLikeType());
        assertEquals(String.class, listRefType.getContentType().getRawClass());

        // 3. rawClass helper
        assertEquals(String.class, TypeFactory.rawClass(String.class));
        assertEquals(List.class, TypeFactory.rawClass(listRefType));

        // 4. unknownType
        JavaType unk = TypeFactory.unknownType();
        assertNotNull(unk);
        assertEquals(Object.class, unk.getRawClass());
    }

    @Test(timeout = 4000)
    public void testDirectFactoryMethodsForContainers() {
        TypeFactory tf = TypeFactory.defaultInstance();

        // Array types
        ArrayType arr1 = tf.constructArrayType(String.class);
        assertEquals(String[].class, arr1.getRawClass());
        assertEquals(String.class, arr1.getContentType().getRawClass());

        ArrayType arr2 = tf.constructArrayType(arr1);
        assertEquals(String[][].class, arr2.getRawClass());

        // Collection types
        CollectionType listType = tf.constructCollectionType(ArrayList.class, Integer.class);
        assertEquals(ArrayList.class, listType.getRawClass());
        assertEquals(Integer.class, listType.getContentType().getRawClass());

        CollectionLikeType colLike = tf.constructCollectionLikeType(Set.class, Double.class);
        assertEquals(Set.class, colLike.getRawClass());
        assertEquals(Double.class, colLike.getContentType().getRawClass());

        // Raw variants
        CollectionType rawCol = tf.constructRawCollectionType(ArrayList.class);
        assertEquals(Object.class, rawCol.getContentType().getRawClass());

        CollectionLikeType rawColLike = tf.constructRawCollectionLikeType(List.class);
        assertEquals(Object.class, rawColLike.getContentType().getRawClass());

        // Map types
        MapType mapType = tf.constructMapType(HashMap.class, String.class, Long.class);
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Long.class, mapType.getContentType().getRawClass());

        // Properties special case
        MapType propType = tf.constructMapType(Properties.class, Object.class, Object.class);
        assertEquals(String.class, propType.getKeyType().getRawClass());
        assertEquals(String.class, propType.getContentType().getRawClass());

        // Raw Map variants
        MapType rawMap = tf.constructRawMapType(HashMap.class);
        assertEquals(Object.class, rawMap.getKeyType().getRawClass());
        assertEquals(Object.class, rawMap.getContentType().getRawClass());

        MapLikeType rawMapLike = tf.constructRawMapLikeType(Map.class);
        assertEquals(Object.class, rawMapLike.getKeyType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testReferenceAndSimpleTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType refType = tf.constructReferenceType(AtomicReference.class, tf.constructType(String.class));
        assertTrue(refType.isReferenceType());
        assertEquals(String.class, refType.getContentType().getRawClass());

        JavaType unchecked = tf.uncheckedSimpleType(Integer.class);
        assertNotNull(unchecked);
        assertEquals(Integer.class, unchecked.getRawClass());

        JavaType simpleParam = tf.constructSimpleType(ArrayList.class, new JavaType[] { tf.constructType(String.class) });
        assertEquals(ArrayList.class, simpleParam.getRawClass());
        assertEquals(1, simpleParam.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testParametricTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();

        // Classes parameterization
        JavaType type1 = tf.constructParametricType(Map.class, String.class, Integer.class);
        assertTrue(type1.isMapLikeType());
        assertEquals(String.class, type1.getKeyType().getRawClass());
        assertEquals(Integer.class, type1.getContentType().getRawClass());

        // JavaType parameterization
        JavaType type2 = tf.constructParametricType(List.class, tf.constructType(Boolean.class));
        assertTrue(type2.isCollectionLikeType());
        assertEquals(Boolean.class, type2.getContentType().getRawClass());

        // Deprecated aliases for backwards compatibility
        @SuppressWarnings("deprecation")
        JavaType dep1 = tf.constructParametrizedType(List.class, Collection.class, String.class);
        assertEquals(List.class, dep1.getRawClass());

        @SuppressWarnings("deprecation")
        JavaType dep2 = tf.constructParametrizedType(List.class, Collection.class, tf.constructType(String.class));
        assertEquals(List.class, dep2.getRawClass());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Low-Level Methods
    // =========================================================================

    @Test(timeout = 4000)
    public void testFindPrimitiveAllBranches() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();

        assertEquals(Integer.TYPE, tf.findClass("int"));
        assertEquals(Long.TYPE, tf.findClass("long"));
        assertEquals(Float.TYPE, tf.findClass("float"));
        assertEquals(Double.TYPE, tf.findClass("double"));
        assertEquals(Boolean.TYPE, tf.findClass("boolean"));
        assertEquals(Byte.TYPE, tf.findClass("byte"));
        assertEquals(Character.TYPE, tf.findClass("char"));
        assertEquals(Short.TYPE, tf.findClass("short"));
        assertEquals(Void.TYPE, tf.findClass("void"));

        // Non-primitive dot-less class name that is not a primitive
        try {
            tf.findClass("nonExistentPrimitive");
            fail("Expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFindClassWithClassLoaderAndContext() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        Class<?> clazz = tf.findClass("java.lang.String");
        assertEquals(String.class, clazz);

        TypeFactory tfWithCl = tf.withClassLoader(String.class.getClassLoader());
        Class<?> clazz2 = tfWithCl.findClass("java.util.ArrayList");
        assertEquals(ArrayList.class, clazz2);
    }

    @Test(timeout = 4000)
    public void testMoreSpecificType() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType obj = tf.constructType(Object.class);
        JavaType num = tf.constructType(Number.class);
        JavaType integer = tf.constructType(Integer.class);

        // Null boundaries
        assertSame(num, tf.moreSpecificType(null, num));
        assertSame(num, tf.moreSpecificType(num, null));

        // Same raw class
        assertSame(num, tf.moreSpecificType(num, num));

        // Hierarchy narrowing
        assertSame(integer, tf.moreSpecificType(num, integer));
        assertSame(integer, tf.moreSpecificType(integer, num));

        // Unrelated types returns type1
        JavaType str = tf.constructType(String.class);
        assertSame(num, tf.moreSpecificType(num, str));
    }

    @Test(timeout = 4000)
    public void testFindTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType mapType = tf.constructMapType(HashMap.class, String.class, Integer.class);
        JavaType[] params = tf.findTypeParameters(mapType, Map.class);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(Integer.class, params[1].getRawClass());

        // No match returns NO_TYPES
        JavaType[] noParams = tf.findTypeParameters(mapType, Collection.class);
        assertEquals(0, noParams.length);

        // Deprecated helper overloads
        @SuppressWarnings("deprecation")
        JavaType[] depParams = tf.findTypeParameters(HashMap.class, Map.class);
        assertNotNull(depParams);
    }

    @Test(timeout = 4000)
    public void testConstructCanonicalNames() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType type = tf.constructMapType(HashMap.class, String.class, Integer.class);
        String canonical = type.toCanonical();
        assertNotNull(canonical);

        JavaType parsed = tf.constructFromCanonical(canonical);
        assertEquals(type, parsed);
    }

    @Test(timeout = 4000)
    public void testConstructSpecializedTypeShortcuts() {
        TypeFactory tf = TypeFactory.defaultInstance();

        // 1. Identical class
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        assertSame(listType, tf.constructSpecializedType(listType, List.class));

        // 2. Base is Object
        JavaType objType = tf.constructType(Object.class);
        JavaType fromObj = tf.constructSpecializedType(objType, String.class);
        assertEquals(String.class, fromObj.getRawClass());

        // 3. Container shortcuts: Map subtypes
        JavaType baseMap = tf.constructMapType(Map.class, String.class, Integer.class);
        assertEquals(HashMap.class, tf.constructSpecializedType(baseMap, HashMap.class).getRawClass());
        assertEquals(LinkedHashMap.class, tf.constructSpecializedType(baseMap, LinkedHashMap.class).getRawClass());
        assertEquals(TreeMap.class, tf.constructSpecializedType(baseMap, TreeMap.class).getRawClass());

        // 4. Container shortcuts: Collection subtypes
        JavaType baseCol = tf.constructCollectionType(Collection.class, String.class);
        assertEquals(ArrayList.class, tf.constructSpecializedType(baseCol, ArrayList.class).getRawClass());
        assertEquals(LinkedList.class, tf.constructSpecializedType(baseCol, LinkedList.class).getRawClass());
        assertEquals(HashSet.class, tf.constructSpecializedType(baseCol, HashSet.class).getRawClass());
        assertEquals(TreeSet.class, tf.constructSpecializedType(baseCol, TreeSet.class).getRawClass());

        // 5. EnumSet shortcut
        JavaType enumSetType = tf.constructCollectionType(EnumSet.class, Thread.State.class);
        JavaType specEnumSet = tf.constructSpecializedType(enumSetType, EnumSet.class);
        assertSame(enumSetType, specEnumSet);

        // 6. Subclass with 0 type parameters
        JavaType customList = tf.constructSpecializedType(baseCol, StringCustomList.class);
        assertEquals(StringCustomList.class, customList.getRawClass());

        // 7. Base type without bindings
        JavaType rawBaseCol = tf.constructRawCollectionType(List.class);
        JavaType specFromRaw = tf.constructSpecializedType(rawBaseCol, ArrayList.class);
        assertEquals(ArrayList.class, specFromRaw.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructGeneralizedType() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType subList = tf.constructCollectionType(ArrayList.class, String.class);

        // Same type identity
        assertSame(subList, tf.constructGeneralizedType(subList, ArrayList.class));

        // Generalize to Collection interface
        JavaType generalized = tf.constructGeneralizedType(subList, Collection.class);
        assertEquals(Collection.class, generalized.getRawClass());
        assertEquals(String.class, generalized.getContentType().getRawClass());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSpecializedNonSubclassThrowsException() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType strType = tf.constructType(String.class);
        tf.constructSpecializedType(strType, Integer.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGeneralizedNonSuperclassThrowsException() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType strType = tf.constructType(String.class);
        tf.constructGeneralizedType(strType, Integer.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFromAnyNullThrowsException() {
        TypeFactory.defaultInstance().constructType((Type) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMalformedCanonicalThrowsException() {
        TypeFactory.defaultInstance().constructFromCanonical("com.not.existing.MissingClass<Foo");
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testTypeModifierReturningNullThrowsException() {
        TypeModifier badModifier = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return null; // Contract violation: cannot return null
            }
        };
        TypeFactory tf = TypeFactory.defaultInstance().withModifier(badModifier);
        tf.constructType(String.class);
    }

    // =========================================================================
    // Partition E: Introspection, Reflection Types & Cycle Resolution
    // =========================================================================

    @Test(timeout = 4000)
    public void testGenericIntrospectionAndTypeVariables() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();

        Field listField = GenericHolder.class.getField("list");
        JavaType listType = tf.constructType(listField.getGenericType());
        assertTrue(listType.isCollectionLikeType());

        Field arrayField = GenericHolder.class.getField("array");
        JavaType arrayType = tf.constructType(arrayField.getGenericType());
        assertTrue(arrayType.isArrayType());

        Field wildcardField = GenericHolder.class.getField("wildcardList");
        JavaType wildcardListType = tf.constructType(wildcardField.getGenericType());
        assertTrue(wildcardListType.isCollectionLikeType());
        assertEquals(Number.class, wildcardListType.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testRecursiveTypesCycleHandling() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType nodeType = tf.constructType(RecursiveNode.class);
        assertNotNull(nodeType);
        assertEquals(RecursiveNode.class, nodeType.getRawClass());

        // Field traversal should resolve self-referential type without infinite recursion
        JavaType nextFieldType = tf.constructType(RecursiveNode.class.getFields()[0].getGenericType());
        assertEquals(RecursiveNode.class, nextFieldType.getRawClass());
    }

    @Test(timeout = 4000)
    public void testWellKnownParametricTypesOptimization() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();

        // Method returning Enum<E>
        Method enumMethod = Enum.class.getMethod("valueOf", Class.class, String.class);
        JavaType enumRet = tf.constructType(enumMethod.getGenericReturnType());
        assertEquals(Enum.class, enumRet.getRawClass());

        // Method using Comparable<T>
        Type compGeneric = String.class.getGenericInterfaces()[0]; // Comparable<String>
        JavaType compType = tf.constructType(compGeneric);
        assertEquals(Comparable.class, compType.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructTypeWithContext() {
        TypeFactory tf = TypeFactory.defaultInstance();

        // Deprecated context overloads
        @SuppressWarnings("deprecation")
        JavaType contextClassType = tf.constructType(String.class, ArrayList.class);
        assertEquals(String.class, contextClassType.getRawClass());

        @SuppressWarnings("deprecation")
        JavaType contextJavaType = tf.constructType(String.class, tf.constructType(ArrayList.class));
        assertEquals(String.class, contextJavaType.getRawClass());
    }
}