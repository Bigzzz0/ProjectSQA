package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.util.LRUMap;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.fasterxml.jackson.databind.type.TypeFactory
 * 
 * Key Defect Targeted:
 * - Defects4J: TestTypeFactory::testCanonicalNames throwing NullPointerException
 *   Triggered when parsing or round-tripping canonical representations of types
 *   (e.g., primitives, arrays, specialized collections/maps, and Calendar).
 * 
 * Decision Branch Zones Covered:
 * 1. Primitive & Well-known Type Resolution:
 *    - All 9 primitives: int, long, float, double, boolean, byte, char, short, void.
 *    - Core well-known types: String, Object, Comparable, Class, Enum.
 * 2. Specialized & Generalized Type Traversal:
 *    - Identity fast-path (rawBase == subclass / superClass).
 *    - Untyped base (rawBase == Object.class).
 *    - Incompatible hierarchy (throw IllegalArgumentException).
 *    - Pre-defined collection shortcuts: ArrayList, LinkedList, HashSet, TreeSet, EnumSet.
 *    - Pre-defined map shortcuts: HashMap, LinkedHashMap, EnumMap, TreeMap.
 *    - Subclass with 0 type parameters vs generic parameters placeholder traversal.
 * 3. Array & Generic Array Traversal:
 *    - Element type array construction via Class and JavaType.
 *    - Reflection GenericArrayType parsing.
 * 4. Canonical String Conversion & Parsing (TypeParser hook):
 *    - Primitive arrays ("int[]"), Object arrays ("java.lang.String[]").
 *    - Deeply nested parameterizations.
 * 5. TypeModifier & ClassLoader Mutant Factories:
 *    - withModifier (null clears, non-null chains, duplicate prevention).
 *    - TypeModifier returning null (IllegalStateException).
 *    - withClassLoader and two-phase ClassLoader lookup.
 * 6. Edge Cases & Exception Guards:
 *    - Deprecated helper variants (e.g., constructType with context).
 *    - Recursive self-referential generics.
 */
public class TypeFactoryGeminiTest {

    // --- Test Fixtures & Helper Classes ---

    static class CustomMapLike { }
    static class CustomCollectionLike { }

    static class GenericHolder<T> {
        public T single;
        public T[] array;
        public List<T> list;
        public List<? extends Number> wildcardExtends;
        public List<? super Integer> wildcardSuper;
    }

    static class SelfReferential<T extends SelfReferential<T>> {
        public T next;
    }

    static class StringListSubclass extends ArrayList<String> {
        private static final long serialVersionUID = 1L;
    }

    static class TwoParamHolder<K, V> {
        public K key;
        public V value;
    }

    static class SubTwoParamHolder<X> extends TwoParamHolder<X, Integer> {
    }

    static class IdentityTypeModifier extends TypeModifier {
        @Override
        public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
            return type;
        }
    }

    static class NullReturningTypeModifier extends TypeModifier {
        @Override
        public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
            return null;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultInstanceAndSingletonContract() {
        TypeFactory tf1 = TypeFactory.defaultInstance();
        TypeFactory tf2 = TypeFactory.defaultInstance();
        assertNotNull(tf1);
        assertSame(tf1, tf2);

        JavaType unknown = TypeFactory.unknownType();
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());
    }

    @Test(timeout = 4000)
    public void testWellKnownCoreSimpleTypes() {
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
    public void testContainerConstructorsViaClassAndJavaType() {
        TypeFactory tf = TypeFactory.defaultInstance();

        CollectionType colType1 = tf.constructCollectionType(ArrayList.class, String.class);
        assertEquals(ArrayList.class, colType1.getRawClass());
        assertEquals(String.class, colType1.getContentType().getRawClass());

        JavaType intType = tf.constructType(Integer.class);
        CollectionType colType2 = tf.constructCollectionType(LinkedList.class, intType);
        assertEquals(LinkedList.class, colType2.getRawClass());
        assertEquals(Integer.class, colType2.getContentType().getRawClass());

        MapType mapType1 = tf.constructMapType(HashMap.class, String.class, Integer.class);
        assertEquals(HashMap.class, mapType1.getRawClass());
        assertEquals(String.class, mapType1.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType1.getContentType().getRawClass());

        MapType mapType2 = tf.constructMapType(TreeMap.class, intType, colType1);
        assertEquals(TreeMap.class, mapType2.getRawClass());
        assertEquals(Integer.class, mapType2.getKeyType().getRawClass());
        assertEquals(ArrayList.class, mapType2.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testMapLikeAndCollectionLikeTypeUpgrades() {
        TypeFactory tf = TypeFactory.defaultInstance();

        CollectionLikeType colLike = tf.constructCollectionLikeType(CustomCollectionLike.class, String.class);
        assertNotNull(colLike);
        assertEquals(CustomCollectionLike.class, colLike.getRawClass());
        assertEquals(String.class, colLike.getContentType().getRawClass());

        MapLikeType mapLike = tf.constructMapLikeType(CustomMapLike.class, String.class, Long.class);
        assertNotNull(mapLike);
        assertEquals(CustomMapLike.class, mapLike.getRawClass());
        assertEquals(String.class, mapLike.getKeyType().getRawClass());
        assertEquals(Long.class, mapLike.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testPropertiesSpecialHandling() {
        TypeFactory tf = TypeFactory.defaultInstance();
        MapType propType = tf.constructMapType(Properties.class, Object.class, Object.class);
        assertEquals(Properties.class, propType.getRawClass());
        assertEquals(String.class, propType.getKeyType().getRawClass());
        assertEquals(String.class, propType.getContentType().getRawClass());

        JavaType directProp = tf.constructType(Properties.class);
        assertTrue(directProp instanceof MapType);
        assertEquals(String.class, ((MapType) directProp).getKeyType().getRawClass());
        assertEquals(String.class, ((MapType) directProp).getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testArrayConstructors() {
        TypeFactory tf = TypeFactory.defaultInstance();

        ArrayType arr1 = tf.constructArrayType(String.class);
        assertEquals(String[].class, arr1.getRawClass());
        assertEquals(String.class, arr1.getContentType().getRawClass());

        ArrayType arr2 = tf.constructArrayType(tf.constructType(Integer.TYPE));
        assertEquals(int[].class, arr2.getRawClass());
        assertEquals(Integer.TYPE, arr2.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testSpecializedTypeShortcuts() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseMap = tf.constructMapType(Map.class, String.class, Integer.class);

        // Identity
        assertSame(baseMap, tf.constructSpecializedType(baseMap, Map.class));

        // Well-known Map subtypes
        JavaType specialized = tf.constructSpecializedType(baseMap, HashMap.class);
        assertEquals(HashMap.class, specialized.getRawClass());
        assertEquals(String.class, specialized.getKeyType().getRawClass());
        assertEquals(Integer.class, specialized.getContentType().getRawClass());

        specialized = tf.constructSpecializedType(baseMap, LinkedHashMap.class);
        assertEquals(LinkedHashMap.class, specialized.getRawClass());

        specialized = tf.constructSpecializedType(baseMap, TreeMap.class);
        assertEquals(TreeMap.class, specialized.getRawClass());

        // Well-known Collection subtypes
        JavaType baseCol = tf.constructCollectionType(Collection.class, String.class);
        assertEquals(ArrayList.class, tf.constructSpecializedType(baseCol, ArrayList.class).getRawClass());
        assertEquals(LinkedList.class, tf.constructSpecializedType(baseCol, LinkedList.class).getRawClass());
        assertEquals(HashSet.class, tf.constructSpecializedType(baseCol, HashSet.class).getRawClass());
        assertEquals(TreeSet.class, tf.constructSpecializedType(baseCol, TreeSet.class).getRawClass());

        // EnumSet shortcut
        JavaType enumSetBase = tf.constructCollectionType(EnumSet.class, ElementType.class);
        assertSame(enumSetBase, tf.constructSpecializedType(enumSetBase, EnumSet.class));
    }

    @Test(timeout = 4000)
    public void testGeneralizedTypeResolution() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType subType = tf.constructCollectionType(ArrayList.class, String.class);

        // Identity
        assertSame(subType, tf.constructGeneralizedType(subType, ArrayList.class));

        // Generalized to interface
        JavaType listType = tf.constructGeneralizedType(subType, List.class);
        assertEquals(List.class, listType.getRawClass());
        assertEquals(String.class, listType.getContentType().getRawClass());

        JavaType colType = tf.constructGeneralizedType(subType, Collection.class);
        assertEquals(Collection.class, colType.getRawClass());
        assertEquals(String.class, colType.getContentType().getRawClass());

        JavaType objType = tf.constructGeneralizedType(subType, Object.class);
        assertEquals(Object.class, objType.getRawClass());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFindPrimitiveLookup() throws ClassNotFoundException {
        TypeFactory tf = TypeFactory.defaultInstance();

        assertSame(Integer.TYPE, tf.findClass("int"));
        assertSame(Long.TYPE, tf.findClass("long"));
        assertSame(Float.TYPE, tf.findClass("float"));
        assertSame(Double.TYPE, tf.findClass("double"));
        assertSame(Boolean.TYPE, tf.findClass("boolean"));
        assertSame(Byte.TYPE, tf.findClass("byte"));
        assertSame(Character.TYPE, tf.findClass("char"));
        assertSame(Short.TYPE, tf.findClass("short"));
        assertSame(Void.TYPE, tf.findClass("void"));
    }

    @Test(timeout = 4000)
    public void testRawClassHelper() {
        assertSame(String.class, TypeFactory.rawClass(String.class));
        assertSame(ArrayList.class, TypeFactory.rawClass(new TypeReference<ArrayList<Integer>>() {}.getType()));
    }

    @Test(timeout = 4000)
    public void testMoreSpecificType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType stringType = tf.constructType(String.class);
        JavaType objectType = tf.constructType(Object.class);

        // Null checks
        assertSame(stringType, tf.moreSpecificType(stringType, null));
        assertSame(stringType, tf.moreSpecificType(null, stringType));
        assertNull(tf.moreSpecificType(null, null));

        // Equal types
        assertSame(stringType, tf.moreSpecificType(stringType, stringType));

        // Inheritance hierarchy: String is more specific than Object
        assertSame(stringType, tf.moreSpecificType(objectType, stringType));
        assertSame(stringType, tf.moreSpecificType(stringType, objectType));

        // Unrelated types returns type1
        JavaType intType = tf.constructType(Integer.class);
        assertSame(stringType, tf.moreSpecificType(stringType, intType));
    }

    @Test(timeout = 4000)
    public void testFindTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType subType = tf.constructType(StringListSubclass.class);
        JavaType[] params = tf.findTypeParameters(subType, List.class);
        assertNotNull(params);
        assertEquals(1, params.length);
        assertEquals(String.class, params[0].getRawClass());

        // Target interface not implemented
        JavaType[] emptyParams = tf.findTypeParameters(subType, Map.class);
        assertNotNull(emptyParams);
        assertEquals(0, emptyParams.length);

        // Deprecated helper methods
        JavaType[] depParams1 = tf.findTypeParameters(StringListSubclass.class, List.class);
        assertEquals(1, depParams1.length);
        assertEquals(String.class, depParams1[0].getRawClass());

        JavaType[] depParams2 = tf.findTypeParameters(StringListSubclass.class, List.class, TypeBindings.emptyBindings());
        assertEquals(1, depParams2.length);
        assertEquals(String.class, depParams2[0].getRawClass());
    }

    @Test(timeout = 4000)
    public void testRawCollectionsAndMapsConstructors() {
        TypeFactory tf = TypeFactory.defaultInstance();

        CollectionType rawCol = tf.constructRawCollectionType(List.class);
        assertEquals(Object.class, rawCol.getContentType().getRawClass());

        CollectionLikeType rawColLike = tf.constructRawCollectionLikeType(CustomCollectionLike.class);
        assertEquals(Object.class, rawColLike.getContentType().getRawClass());

        MapType rawMap = tf.constructRawMapType(Map.class);
        assertEquals(Object.class, rawMap.getKeyType().getRawClass());
        assertEquals(Object.class, rawMap.getContentType().getRawClass());

        MapLikeType rawMapLike = tf.constructRawMapLikeType(CustomMapLike.class);
        assertEquals(Object.class, rawMapLike.getKeyType().getRawClass());
        assertEquals(Object.class, rawMapLike.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testReferenceTypeConstruction() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType refType = tf.constructType(new TypeReference<AtomicReference<String>>() {});
        assertTrue(refType.isReferenceType());
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(String.class, refType.getContentType().getRawClass());

        JavaType directRef = tf.constructReferenceType(AtomicReference.class, tf.constructType(Long.class));
        assertTrue(directRef.isReferenceType());
        assertEquals(Long.class, directRef.getContentType().getRawClass());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Jackson Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target: Defect in TestTypeFactory::testCanonicalNames throwing NullPointerException.
     * Validates round-trip canonical names for Calendar, primitives, arrays, and parameterized maps/lists.
     */
    @Test(timeout = 4000)
    public void testCanonicalNames() {
        TypeFactory tf = TypeFactory.defaultInstance();

        // 1. Calendar test case
        JavaType t = tf.constructType(java.util.Calendar.class);
        String can = t.toCanonical();
        assertEquals("java.util.Calendar", can);
        assertEquals(t, tf.constructFromCanonical(can));
        assertEquals(t, tf.constructFromCanonical(Calendar.class.getName()));
        assertEquals(t, tf.constructFromCanonical("   " + Calendar.class.getName() + "   "));

        // 2. Generic Collection
        t = tf.constructCollectionType(ArrayList.class, String.class);
        assertEquals("java.util.ArrayList<java.lang.String>", t.toCanonical());
        assertEquals(t, tf.constructFromCanonical("java.util.ArrayList<java.lang.String>"));

        // 3. Generic Map
        t = tf.constructMapType(HashMap.class, String.class, Integer.class);
        assertEquals("java.util.HashMap<java.lang.String,java.lang.Integer>", t.toCanonical());
        assertEquals(t, tf.constructFromCanonical("java.util.HashMap<java.lang.String,java.lang.Integer>"));

        // 4. Primitive and Object Arrays
        t = tf.constructArrayType(String.class);
        assertEquals("java.lang.String[]", t.toCanonical());
        assertEquals(t, tf.constructFromCanonical("java.lang.String[]"));

        t = tf.constructArrayType(Integer.TYPE);
        assertEquals("int[]", t.toCanonical());
        assertEquals(t, tf.constructFromCanonical("int[]"));

        t = tf.constructArrayType(Boolean.TYPE);
        assertEquals("boolean[]", t.toCanonical());
        assertEquals(t, tf.constructFromCanonical("boolean[]"));

        t = tf.constructArrayType(Long.TYPE);
        assertEquals("long[]", t.toCanonical());
        assertEquals(t, tf.constructFromCanonical("long[]"));
    }

    @Test(timeout = 4000)
    public void testComplexNestedCanonicalNames() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType nestedType = tf.constructCollectionType(ArrayList.class,
                tf.constructMapType(HashMap.class, String.class, Integer.class));
        String can = nestedType.toCanonical();
        JavaType parsed = tf.constructFromCanonical(can);
        assertEquals(nestedType, parsed);
        assertEquals(ArrayList.class, parsed.getRawClass());
        assertEquals(HashMap.class, parsed.getContentType().getRawClass());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructSpecializedTypeWithIncompatibleSubclass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        tf.constructSpecializedType(listType, Set.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructGeneralizedTypeWithIncompatibleSuperclass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        tf.constructGeneralizedType(listType, Map.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructFromCanonicalMalformed() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructFromCanonical("java.util.List<java.lang.String");
    }

    @Test(expected = ClassNotFoundException.class, timeout = 4000)
    public void testFindClassNotFound() throws ClassNotFoundException {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.findClass("com.fasterxml.jackson.bogus.NonExistentClass");
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testTypeModifierReturningNullThrowsException() {
        TypeFactory tf = TypeFactory.defaultInstance().withModifier(new NullReturningTypeModifier());
        tf.constructType(String.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructSpecializedTypeParameterMismatch() {
        TypeFactory tf = TypeFactory.defaultInstance();
        // Base is MultiParam<String, Integer>
        JavaType baseType = tf.constructParametricType(TwoParamHolder.class, String.class, Integer.class);
        // SubTwoParamHolder only binds X to String, but value is Integer.
        // Try specializing TwoParamHolder<String, String> into SubTwoParamHolder (which has Integer)
        JavaType incompatibleBase = tf.constructParametricType(TwoParamHolder.class, String.class, String.class);
        tf.constructSpecializedType(incompatibleBase, SubTwoParamHolder.class);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Reflection, Generics & Cache Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testMutantFactoriesConfiguration() {
        TypeFactory base = TypeFactory.defaultInstance();
        ClassLoader cl = getClass().getClassLoader();

        TypeFactory withCl = base.withClassLoader(cl);
        assertNotSame(base, withCl);
        assertSame(cl, withCl.getClassLoader());

        LRUMap<Object, JavaType> customCache = new LRUMap<Object, JavaType>(10, 50);
        TypeFactory withCache = base.withCache(customCache);
        assertNotSame(base, withCache);

        TypeModifier mod1 = new IdentityTypeModifier();
        TypeFactory withMod = base.withModifier(mod1);
        assertNotSame(base, withMod);

        // Chaining modifiers
        TypeModifier mod2 = new IdentityTypeModifier();
        TypeFactory withTwoMods = withMod.withModifier(mod2);
        assertNotSame(withMod, withTwoMods);

        // Modifier reset (null argument)
        TypeFactory withoutMods = withMod.withModifier(null);
        assertNotNull(withoutMods);

        // Cache clear invocation
        base.clearCache();
    }

    @Test(timeout = 4000)
    public void testReflectionTypeResolution() throws NoSuchFieldException {
        TypeFactory tf = TypeFactory.defaultInstance();

        Field singleField = GenericHolder.class.getField("single");
        JavaType singleType = tf.constructType(singleField.getGenericType());
        assertEquals(Object.class, singleType.getRawClass());

        Field arrayField = GenericHolder.class.getField("array");
        JavaType arrayType = tf.constructType(arrayField.getGenericType());
        assertTrue(arrayType.isArrayType());
        assertEquals(Object.class, arrayType.getContentType().getRawClass());

        Field wildcardExtField = GenericHolder.class.getField("wildcardExtends");
        JavaType wildcardExtType = tf.construct