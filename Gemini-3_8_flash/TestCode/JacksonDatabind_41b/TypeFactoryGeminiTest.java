/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Defect Target:
 *  - Deprecated constructType(Type, Class<?>) and constructType(Type, JavaType) fail with
 *    IllegalArgumentException: "Unrecognized Type: [null]" (or NPE) when contextClass/contextType is null.
 *    (Defects4J ground truth: DeprecatedTypeHandling1102Test::testDeprecatedTypeResolution).
 *
 * Core Branches & Coverage Points:
 * 1. constructType with null contextClass / contextType (Defect #1102 target).
 * 2. well-known cached types: boolean, int, long, String, Object, Comparable, Enum, Class.
 * 3. rawClass(Type) resolution: Class<?> instance vs generic / JavaType reflection.
 * 4. findClass(String): primitives (int, long, float, double, boolean, byte, char, short, void),
 *    custom classloader path, context classloader path, ClassNotFoundException handling.
 * 5. constructSpecializedType:
 *    - rawBase == subclass (fast return)
 *    - rawBase == Object.class
 *    - incompatible type (IllegalArgumentException)
 *    - empty bindings shortcut
 *    - Container fast-paths:
 *        * Map-like: HashMap, LinkedHashMap, EnumMap, TreeMap
 *        * Collection-like: ArrayList, LinkedList, HashSet, TreeSet, EnumSet
 *    - Subclass with 0 type parameters
 *    - Interface refinement vs class refinement
 * 6. constructGeneralizedType:
 *    - rawBase == superClass (fast return)
 *    - valid superclass / interface traversal
 *    - incompatible class (IllegalArgumentException)
 * 7. constructFromCanonical: valid canonical strings, parametrized, nested generics, and syntax errors.
 * 8. findTypeParameters: matching supertype, non-matching supertype (empty array), deprecated variants.
 * 9. moreSpecificType: type1 == null, type2 == null, raw1 == raw2, subtype relationships, unrelated.
 * 10. Direct factory methods:
 *    - constructArrayType (Class & JavaType)
 *    - constructCollectionType / constructCollectionLikeType (custom types, upgradeFrom)
 *    - constructMapType (Properties special case vs generic Map), constructMapLikeType
 *    - constructReferenceType (AtomicReference)
 *    - constructParametricType / constructParametrizedType
 *    - constructRawCollectionType, constructRawCollectionLikeType, constructRawMapType, constructRawMapLikeType
 * 11. Type modifiers:
 *    - withModifier(null), withModifier(mod), chaining modifiers, ArrayBuilders deduplication.
 *    - classloader chaining: withClassLoader(loader), getClassLoader().
 * 12. Recursive & Self-referencing type definitions:
 *    - Generic recursive class hierarchies (e.g. Node<T extends Node<T>>).
 * 13. Complex Types: GenericArrayType, WildcardType (upper/lower bounds), TypeVariable bindings & unbound.
 * ---------------------------------------------------------------------------------------------------
 */

package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;

public class TypeFactoryGeminiTest {

    // Helper classes for reflection and generic resolution tests
    static class GenericHolder<T> {
        public T singleField;
        public T[] genericArray;
        public List<? extends Number> wildcardExtends;
        public List<? super Integer> wildcardSuper;
    }

    static class SelfReferential<T extends SelfReferential<T>> {
        public T next;
    }

    static class StringMap extends HashMap<String, Integer> {
        private static final long serialVersionUID = 1L;
    }

    static class CustomList<E> extends ArrayList<E> {
        private static final long serialVersionUID = 1L;
    }

    static class CustomComparable implements Comparable<CustomComparable> {
        @Override
        public int compareTo(CustomComparable o) {
            return 0;
        }
    }

    /*
     * =========================================================================
     * Partition C: Defect-Targeted Branch Zone (Issue #1102 / Defect Ground Truth)
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testDeprecatedTypeResolutionWithNullContextClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        // Defects4J Issue #1102: constructType(Type, (Class<?>) null) triggers
        // IllegalArgumentException: Unrecognized Type: [null] when context is null.
        @SuppressWarnings("deprecation")
        JavaType jt = tf.constructType(String.class, (Class<?>) null);
        assertNotNull("Type should not be null when contextClass is null", jt);
        assertEquals(String.class, jt.getRawClass());
    }

    @Test(timeout = 4000)
    public void testDeprecatedTypeResolutionWithNullContextType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        // Calling constructType(Type, (JavaType) null) should resolve without throwing NPE or error
        @SuppressWarnings("deprecation")
        JavaType jt = tf.constructType(Integer.class, (JavaType) null);
        assertNotNull("Type should not be null when contextType is null", jt);
        assertEquals(Integer.class, jt.getRawClass());
    }

    /*
     * =========================================================================
     * Partition A: Core Functional Logic & State Transitions
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testWellKnownCoreTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        assertEquals(boolean.class, tf.constructType(Boolean.TYPE).getRawClass());
        assertEquals(int.class, tf.constructType(Integer.TYPE).getRawClass());
        assertEquals(long.class, tf.constructType(Long.TYPE).getRawClass());
        assertEquals(String.class, tf.constructType(String.class).getRawClass());
        assertEquals(Object.class, tf.constructType(Object.class).getRawClass());
        assertEquals(Comparable.class, tf.constructType(Comparable.class).getRawClass());
        assertEquals(Enum.class, tf.constructType(Enum.class).getRawClass());
        assertEquals(Class.class, tf.constructType(Class.class).getRawClass());
    }

    @Test(timeout = 4000)
    public void testUnknownTypeAndRawClass() {
        JavaType unknown = TypeFactory.unknownType();
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());

        assertEquals(String.class, TypeFactory.rawClass(String.class));
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        assertEquals(List.class, TypeFactory.rawClass(listType));
    }

    @Test(timeout = 4000)
    public void testClearCache() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type1 = tf.constructType(CustomComparable.class);
        assertNotNull(type1);
        tf.clearCache();
        JavaType type2 = tf.constructType(CustomComparable.class);
        assertNotNull(type2);
        assertEquals(type1.getRawClass(), type2.getRawClass());
    }

    @Test(timeout = 4000)
    public void testFindClassPrimitives() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        assertEquals(int.class, tf.findClass("int"));
        assertEquals(long.class, tf.findClass("long"));
        assertEquals(float.class, tf.findClass("float"));
        assertEquals(double.class, tf.findClass("double"));
        assertEquals(boolean.class, tf.findClass("boolean"));
        assertEquals(byte.class, tf.findClass("byte"));
        assertEquals(char.class, tf.findClass("char"));
        assertEquals(short.class, tf.findClass("short"));
        assertEquals(void.class, tf.findClass("void"));
    }

    @Test(timeout = 4000)
    public void testFindClassStandardAndCustomClassLoader() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        Class<?> clazz = tf.findClass("java.lang.String");
        assertEquals(String.class, clazz);

        ClassLoader cl = getClass().getClassLoader();
        TypeFactory customTf = tf.withClassLoader(cl);
        assertEquals(cl, customTf.getClassLoader());
        Class<?> customClazz = customTf.findClass(TypeFactoryGeminiTest.class.getName());
        assertEquals(TypeFactoryGeminiTest.class, customClazz);
    }

    @Test(timeout = 4000)
    public void testConstructSpecializedTypeBasicAndFastPaths() {
        TypeFactory tf = TypeFactory.defaultInstance();

        // Identical class returns same
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        assertSame(listType, tf.constructSpecializedType(listType, List.class));

        // Object.class base
        JavaType objType = tf.constructType(Object.class);
        JavaType specializedFromObj = tf.constructSpecializedType(objType, String.class);
        assertEquals(String.class, specializedFromObj.getRawClass());

        // Target type without generics
        JavaType numType = tf.constructType(Number.class);
        JavaType integerType = tf.constructSpecializedType(numType, Integer.class);
        assertEquals(Integer.class, integerType.getRawClass());

        // Map shortcuts
        JavaType mapType = tf.constructMapType(Map.class, String.class, Integer.class);
        JavaType hashMapType = tf.constructSpecializedType(mapType, HashMap.class);
        assertEquals(HashMap.class, hashMapType.getRawClass());
        assertEquals(String.class, hashMapType.getKeyType().getRawClass());
        assertEquals(Integer.class, hashMapType.getContentType().getRawClass());

        JavaType linkedHashMapType = tf.constructSpecializedType(mapType, LinkedHashMap.class);
        assertEquals(LinkedHashMap.class, linkedHashMapType.getRawClass());

        JavaType treeMapType = tf.constructSpecializedType(mapType, TreeMap.class);
        assertEquals(TreeMap.class, treeMapType.getRawClass());

        // Collection shortcuts
        JavaType colType = tf.constructCollectionType(Collection.class, String.class);
        JavaType arrayListType = tf.constructSpecializedType(colType, ArrayList.class);
        assertEquals(ArrayList.class, arrayListType.getRawClass());
        assertEquals(String.class, arrayListType.getContentType().getRawClass());

        JavaType linkedListType = tf.constructSpecializedType(colType, LinkedList.class);
        assertEquals(LinkedList.class, linkedListType.getRawClass());

        JavaType hashSetType = tf.constructSpecializedType(colType, HashSet.class);
        assertEquals(HashSet.class, hashSetType.getRawClass());

        JavaType treeSetType = tf.constructSpecializedType(colType, TreeSet.class);
        assertEquals(TreeSet.class, treeSetType.getRawClass());

        // Subclass with 0 type parameters
        JavaType specializedStringMap = tf.constructSpecializedType(mapType, StringMap.class);
        assertEquals(StringMap.class, specializedStringMap.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructSpecializedTypeEnumSetShortcut() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType enumSetType = tf.constructCollectionType(EnumSet.class, Thread.State.class);
        // Special branch: if rawBase == EnumSet.class, return baseType
        JavaType res = tf.constructSpecializedType(enumSetType, EnumSet.class);
        assertSame(enumSetType, res);
    }

    @Test(timeout = 4000)
    public void testConstructGeneralizedType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType arrayListType = tf.constructCollectionType(ArrayList.class, String.class);

        // Same class identity
        assertSame(arrayListType, tf.constructGeneralizedType(arrayListType, ArrayList.class));

        // Generalize to List
        JavaType listType = tf.constructGeneralizedType(arrayListType, List.class);
        assertEquals(List.class, listType.getRawClass());
        assertEquals(String.class, listType.getContentType().getRawClass());

        // Generalize to Collection
        JavaType colType = tf.constructGeneralizedType(arrayListType, Collection.class);
        assertEquals(Collection.class, colType.getRawClass());
        assertEquals(String.class, colType.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testCanonicalRoundTrip() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType complexType = tf.constructMapType(HashMap.class, String.class, Integer.class);
        String canonical = complexType.toCanonical();
        JavaType parsed = tf.constructFromCanonical(canonical);
        assertEquals(complexType, parsed);
    }

    @Test(timeout = 4000)
    public void testFindTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType stringMapType = tf.constructType(StringMap.class);
        JavaType[] params = tf.findTypeParameters(stringMapType, Map.class);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(Integer.class, params[1].getRawClass());

        // Non-supertype
        JavaType[] noParams = tf.findTypeParameters(stringMapType, Collection.class);
        assertEquals(0, noParams.length);

        // Deprecated helper variants
        @SuppressWarnings("deprecation")
        JavaType[] paramsDep1 = tf.findTypeParameters(StringMap.class, Map.class);
        assertEquals(2, paramsDep1.length);
        assertEquals(String.class, paramsDep1[0].getRawClass());

        @SuppressWarnings("deprecation")
        JavaType[] paramsDep2 = tf.findTypeParameters(StringMap.class, Map.class, TypeBindings.emptyBindings());
        assertEquals(2, paramsDep2.length);
    }

    @Test(timeout = 4000)
    public void testMoreSpecificType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        JavaType arrayListType = tf.constructCollectionType(ArrayList.class, String.class);
        JavaType stringType = tf.constructType(String.class);

        assertSame(listType, tf.moreSpecificType(listType, null));
        assertSame(listType, tf.moreSpecificType(null, listType));
        assertSame(listType, tf.moreSpecificType(listType, listType));

        // Subclass is more specific
        assertSame(arrayListType, tf.moreSpecificType(listType, arrayListType));
        assertSame(arrayListType, tf.moreSpecificType(arrayListType, listType));

        // Unrelated types returns type1
        assertSame(listType, tf.moreSpecificType(listType, stringType));
    }

    /*
     * =========================================================================
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testTypeReferenceConstruct() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeReference<List<String>> typeRef = new TypeReference<List<String>>() {};
        JavaType jt = tf.constructType(typeRef);
        assertEquals(List.class, jt.getRawClass());
        assertEquals(String.class, jt.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructArrayType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ArrayType arrFromClass = tf.constructArrayType(String.class);
        assertEquals(String[].class, arrFromClass.getRawClass());
        assertEquals(String.class, arrFromClass.getContentType().getRawClass());

        ArrayType arrFromJavaType = tf.constructArrayType(tf.constructType(Integer.class));
        assertEquals(Integer[].class, arrFromJavaType.getRawClass());
        assertEquals(Integer.class, arrFromJavaType.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructMapTypeAndProperties() {
        TypeFactory tf = TypeFactory.defaultInstance();
        // Properties class special handling -> forced <String, String>
        MapType propType = tf.constructMapType(Properties.class, Object.class, Object.class);
        assertEquals(Properties.class, propType.getRawClass());
        assertEquals(String.class, propType.getKeyType().getRawClass());
        assertEquals(String.class, propType.getContentType().getRawClass());

        MapType genericPropType = (MapType) tf.constructType(Properties.class);
        assertEquals(String.class, genericPropType.getKeyType().getRawClass());
        assertEquals(String.class, genericPropType.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructRawCollectionAndMapVariants() {
        TypeFactory tf = TypeFactory.defaultInstance();

        CollectionType rawCol = tf.constructRawCollectionType(ArrayList.class);
        assertEquals(ArrayList.class, rawCol.getRawClass());
        assertEquals(Object.class, rawCol.getContentType().getRawClass());

        CollectionLikeType rawColLike = tf.constructRawCollectionLikeType(ArrayList.class);
        assertEquals(ArrayList.class, rawColLike.getRawClass());
        assertEquals(Object.class, rawColLike.getContentType().getRawClass());

        MapType rawMap = tf.constructRawMapType(HashMap.class);
        assertEquals(HashMap.class, rawMap.getRawClass());
        assertEquals(Object.class, rawMap.getKeyType().getRawClass());
        assertEquals(Object.class, rawMap.getContentType().getRawClass());

        MapLikeType rawMapLike = tf.constructRawMapLikeType(HashMap.class);
        assertEquals(HashMap.class, rawMapLike.getRawClass());
        assertEquals(Object.class, rawMapLike.getKeyType().getRawClass());
        assertEquals(Object.class, rawMapLike.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructCollectionLikeAndMapLikeUpgrade() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType stringType = tf.constructType(String.class);

        // String is not a collection, forces upgradeFrom
        CollectionLikeType colLike = tf.constructCollectionLikeType(String.class, stringType);
        assertEquals(String.class, colLike.getRawClass());
        assertEquals(String.class, colLike.getContentType().getRawClass());

        // String is not a map, forces upgradeFrom
        MapLikeType mapLike = tf.constructMapLikeType(String.class, stringType, stringType);
        assertEquals(String.class, mapLike.getRawClass());
        assertEquals(String.class, mapLike.getKeyType().getRawClass());
        assertEquals(String.class, mapLike.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructReferenceType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType refType = tf.constructReferenceType(AtomicReference.class, tf.constructType(String.class));
        assertTrue(refType.isReferenceType());
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(String.class, refType.getContentType().getRawClass());

        JavaType autoRefType = tf.constructType(AtomicReference.class);
        assertTrue(autoRefType.isReferenceType());
    }

    @Test(timeout = 4000)
    public void testUncheckedSimpleTypeAndConstructSimpleType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType unchecked = tf.uncheckedSimpleType(String.class);
        assertEquals(String.class, unchecked.getRawClass());

        JavaType simple = tf.constructSimpleType(String.class, new JavaType[0]);
        assertEquals(String.class, simple.getRawClass());

        @SuppressWarnings("deprecation")
        JavaType depSimple = tf.constructSimpleType(String.class, String.class, new JavaType[0]);
        assertEquals(String.class, depSimple.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructParametricAndParametrizedVariants() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType pt1 = tf.constructParametricType(List.class, String.class);
        assertEquals(List.class, pt1.getRawClass());
        assertEquals(String.class, pt1.getContentType().getRawClass());

        JavaType pt2 = tf.constructParametricType(Map.class, tf.constructType(String.class), tf.constructType(Integer.class));
        assertEquals(Map.class, pt2.getRawClass());
        assertEquals(String.class, pt2.getKeyType().getRawClass());

        JavaType pt3 = tf.constructParametrizedType(List.class, Collection.class, String.class);
        assertEquals(List.class, pt3.getRawClass());

        JavaType pt4 = tf.constructParametrizedType(List.class, Collection.class, tf.constructType(String.class));
        assertEquals(List.class, pt4.getRawClass());
    }

    /*
     * =========================================================================
     * Partition D: Exception & Defensive Guard Paths
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testFindClassNotFound() {
        TypeFactory tf = TypeFactory.defaultInstance();
        try {
            tf.findClass("com.nonexistent.NoSuchClassDef");
            fail("Expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertNotNull(e);
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructSpecializedTypeIncompatible() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        // Map is not a subtype of List -> IllegalArgumentException
        tf.constructSpecializedType(listType, Map.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructGeneralizedTypeIncompatible() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        // Set is not a supertype of List -> IllegalArgumentException
        tf.constructGeneralizedType(listType, Set.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructFromCanonicalMalformed() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructFromCanonical("java.util.List<missing_closing_bracket");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructTypeWithUnrecognizedNullType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        // Passing null Type directly to constructType(Type) should throw IllegalArgumentException
        tf.constructType((Type) null);
    }

    /*
     * =========================================================================
     * Partition E: Object Lifecycle, Modifiers, Generics & Complex Resolution
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testTypeModifiersLifecycle() {
        TypeFactory tf = TypeFactory.defaultInstance();

        // Null modifier is a no-op / returns copy
        TypeFactory tf2 = tf.withModifier(null);
        assertNotNull(tf2);

        // Add a modifier
        TypeModifier mod1 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeFactory tfWithMod = tf.withModifier(mod1);
        assertNotNull(tfWithMod);

        // Add duplicate modifier -> deduplicated via ArrayBuilders
        TypeFactory tfWithModDup = tfWithMod.withModifier(mod1);
        assertNotNull(tfWithModDup);

        // Add a second distinct modifier
        TypeModifier mod2 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeFactory tfWithTwoMods = tfWithMod.withModifier(mod2);
        assertNotNull(tfWithTwoMods);
    }

    @Test(timeout = 4000)
    public void testRecursiveGenericTypeResolution() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType jt = tf.constructType(SelfReferential.class);
        assertNotNull(jt);
        assertEquals(SelfReferential.class, jt.getRawClass());
    }

    @Test(timeout = 4000)
    public void testGenericFieldsResolution() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();

        Field singleField = GenericHolder.class.getField("singleField");
        JavaType singleType = tf.constructType(singleField.getGenericType(), TypeBindings.emptyBindings());
        assertNotNull(singleType);

        Field genericArray = GenericHolder.class.getField("genericArray");
        Type gat = genericArray.getGenericType();
        assertTrue(gat instanceof GenericArrayType);
        JavaType gatType = tf.constructType(gat, TypeBindings.emptyBindings());
        assertTrue(gatType.isArrayType());

        Field wildcardExtends = GenericHolder.class.getField("wildcardExtends");
        JavaType weType = tf.constructType(wildcardExtends.getGenericType(), TypeBindings.emptyBindings());
        assertEquals(List.class, weType.getRawClass());
        assertEquals(Number.class, weType.getContentType().getRawClass());

        Field wildcardSuper = GenericHolder.class.getField("wildcardSuper");
        JavaType wsType = tf.constructType(wildcardSuper.getGenericType(), TypeBindings.emptyBindings());
        assertEquals(List.class, wsType.getRawClass());
        assertEquals(Object.class, wsType.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testDeprecatedConstructTypeWithContext() {
        TypeFactory tf = TypeFactory.defaultInstance();
        @SuppressWarnings("deprecation")
        JavaType jtClass = tf.constructType(String.class, Object.class);
        assertEquals(String.class, jtClass.getRawClass());

        @SuppressWarnings("deprecation")
        JavaType jtJavaType = tf.constructType(String.class, tf.constructType(Object.class));
        assertEquals(String.class, jtJavaType.getRawClass());
    }
}