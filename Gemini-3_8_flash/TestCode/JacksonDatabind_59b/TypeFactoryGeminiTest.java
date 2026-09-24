package com.fasterxml.jackson.databind.type;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.util.LRUMap;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. TARGET DEFECT: Jackson-Databind #1384 (testMapKeyRefinement1384)
 *    - Branch: TypeFactory.constructSpecializedType(JavaType, Class<?>)
 *    - Flaw: When specializing a base type into a subclass (e.g. Map -> HashMap, or Collection -> ArrayList),
 *      the newly constructed JavaType failed to propagate valueHandler and typeHandler from the baseType.
 *    - Verification: Specializing a JavaType configured with custom valueHandler and typeHandler must preserve
 *      both handlers on the resulting specialized type.
 *
 * 2. STRUCTURAL BRANCH COVERAGE TARGETS:
 *    - Partition A (Core Functional & Hierarchy Resolution):
 *      - constructType: Class, ParameterizedType, GenericArrayType, WildcardType, TypeVariable, JavaType.
 *      - Parametric short-circuits: Enum, Comparable, Class.
 *      - Direct constructors: constructArrayType, constructCollectionType, constructMapType, constructReferenceType.
 *      - Fast paths for well-known types: boolean, int, long, String, Object.
 *      - Properties special handling (MapType with String/String key/value).
 *    - Partition B (Boundary Conditions & Extremes):
 *      - rawClass erasure determination.
 *      - Empty bindings, empty type parameter arrays, recursive type structures.
 *      - Upgrades via constructCollectionLikeType and constructMapLikeType for non-standard types.
 *    - Partition C (Defect-Targeted Verification):
 *      - constructSpecializedType retaining handlers for MapType, CollectionType, and SimpleType.
 *    - Partition D (Defensive & Exception Paths):
 *      - Unknown type structures, malformed canonical strings, illegal subtype/supertype hierarchies.
 *      - Primitive lookup via findClass with all primitive keywords and ClassNotFoundException fallback.
 *      - TypeModifier returning null leading to IllegalStateException.
 *    - Partition E (Lifecycle, Cache & Mutant Configs):
 *      - withModifier, withClassLoader, withCache, clearCache.
 *      - moreSpecificType comparison matrix (nulls, equals, assignable).
 */
public class TypeFactoryGeminiTest {

    // --- Test Fixture Types ---

    enum TestEnum {
        ALPHA, BETA
    }

    static class GenericHolder<T, U extends Number> {
        public T unboundVar;
        public U boundedVar;
        public T[] genericArray;
        public List<String>[] listGenericArray;
        public List<?> wildcardList;
        public List<? extends Number> upperWildcardList;
        public List<? super Integer> lowerWildcardList;
    }

    static class RecursiveNode {
        public RecursiveNode next;
    }

    static class SelfReferential<T extends SelfReferential<T>> {
        public T self;
    }

    static class CustomList<E> extends ArrayList<E> {
        private static final long serialVersionUID = 1L;
    }

    static class CustomMap<K, V> extends HashMap<K, V> {
        private static final long serialVersionUID = 1L;
    }

    static class TripleMap<A, B, C> extends HashMap<A, B> {
        private static final long serialVersionUID = 1L;
        public C extra;
    }

    static class NonGenericSubList extends ArrayList<String> {
        private static final long serialVersionUID = 1L;
    }

    interface SingleInterface<T> {}
    static class SingleImpl<T> implements SingleInterface<T> {}

    // =========================================================================
    // Partition A: Core Functional Logic & Hierarchy Resolution
    // =========================================================================

    @Test(timeout = 4000)
    public void testWellKnownCoreSimpleTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType boolType = tf.constructType(Boolean.TYPE);
        assertTrue(boolType.isPrimitive());
        assertEquals(Boolean.TYPE, boolType.getRawClass());

        JavaType intType = tf.constructType(Integer.TYPE);
        assertTrue(intType.isPrimitive());
        assertEquals(Integer.TYPE, intType.getRawClass());

        JavaType longType = tf.constructType(Long.TYPE);
        assertTrue(longType.isPrimitive());
        assertEquals(Long.TYPE, longType.getRawClass());

        JavaType stringType = tf.constructType(String.class);
        assertFalse(stringType.isPrimitive());
        assertEquals(String.class, stringType.getRawClass());

        JavaType objType = tf.constructType(Object.class);
        assertEquals(Object.class, objType.getRawClass());
    }

    @Test(timeout = 4000)
    public void testDirectFactoryMethodsForContainers() {
        TypeFactory tf = TypeFactory.defaultInstance();

        ArrayType arrayTypeFromClass = tf.constructArrayType(String.class);
        assertEquals(String[].class, arrayTypeFromClass.getRawClass());
        assertEquals(String.class, arrayTypeFromClass.getContentType().getRawClass());

        ArrayType arrayTypeFromJavaType = tf.constructArrayType(tf.constructType(Integer.class));
        assertEquals(Integer[].class, arrayTypeFromJavaType.getRawClass());

        CollectionType colFromClasses = tf.constructCollectionType(List.class, String.class);
        assertEquals(List.class, colFromClasses.getRawClass());
        assertEquals(String.class, colFromClasses.getContentType().getRawClass());

        MapType mapFromClasses = tf.constructMapType(Map.class, String.class, Integer.class);
        assertEquals(Map.class, mapFromClasses.getRawClass());
        assertEquals(String.class, mapFromClasses.getKeyType().getRawClass());
        assertEquals(Integer.class, mapFromClasses.getContentType().getRawClass());

        // Special handling of java.util.Properties
        MapType propType = tf.constructMapType(Properties.class, Object.class, Object.class);
        assertEquals(Properties.class, propType.getRawClass());
        assertEquals(String.class, propType.getKeyType().getRawClass());
        assertEquals(String.class, propType.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testParametricShortCircuits() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType enumType = tf.constructType(new TypeReference<Enum<TestEnum>>() {});
        assertEquals(Enum.class, enumType.getRawClass());

        JavaType compType = tf.constructType(new TypeReference<Comparable<String>>() {});
        assertEquals(Comparable.class, compType.getRawClass());

        JavaType classType = tf.constructType(new TypeReference<Class<String>>() {});
        assertEquals(Class.class, classType.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructFromReflectionFields() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();

        Field arrayField = GenericHolder.class.getField("genericArray");
        JavaType arrayType = tf.constructType(arrayField.getGenericType());
        assertTrue(arrayType.isArrayType());

        Field listArrayField = GenericHolder.class.getField("listGenericArray");
        JavaType listArrayType = tf.constructType(listArrayField.getGenericType());
        assertTrue(listArrayType.isArrayType());
        assertTrue(listArrayType.getContentType().isCollectionLikeType());

        Field wildcardField = GenericHolder.class.getField("wildcardList");
        JavaType wildcardType = tf.constructType(wildcardField.getGenericType());
        assertEquals(List.class, wildcardType.getRawClass());

        Field upperWildcardField = GenericHolder.class.getField("upperWildcardList");
        JavaType upperWildcardType = tf.constructType(upperWildcardField.getGenericType());
        assertEquals(Number.class, upperWildcardType.getContentType().getRawClass());

        Field lowerWildcardField = GenericHolder.class.getField("lowerWildcardList");
        JavaType lowerWildcardType = tf.constructType(lowerWildcardField.getGenericType());
        assertEquals(Object.class, lowerWildcardType.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testAtomicReferenceAndWellKnownClassResolution() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType refType = tf.constructType(new TypeReference<AtomicReference<String>>() {});
        assertTrue(refType.isReferenceType());
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(String.class, refType.getContentType().getRawClass());

        JavaType directRef = tf.constructReferenceType(AtomicReference.class, tf.constructType(Long.class));
        assertTrue(directRef.isReferenceType());
        assertEquals(Long.class, directRef.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testCanonicalStringParsing() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType parsed = tf.constructFromCanonical("java.util.Map<java.lang.String,java.lang.Integer>");
        assertTrue(parsed.isMapLikeType());
        assertEquals(Map.class, parsed.getRawClass());
        assertEquals(String.class, parsed.getKeyType().getRawClass());
        assertEquals(Integer.class, parsed.getContentType().getRawClass());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testRawClassResolution() {
        Class<?> raw1 = TypeFactory.rawClass(String.class);
        assertEquals(String.class, raw1);

        Type paramType = new TypeReference<List<Double>>() {}.getType();
        Class<?> raw2 = TypeFactory.rawClass(paramType);
        assertEquals(List.class, raw2);
    }

    @Test(timeout = 4000)
    public void testUnknownType() {
        JavaType unknown1 = TypeFactory.unknownType();
        JavaType unknown2 = TypeFactory.defaultInstance()._unknownType();
        assertSame(unknown1, unknown2);
        assertEquals(Object.class, unknown1.getRawClass());
    }

    @Test(timeout = 4000)
    public void testCollectionAndMapLikeTypeUpgrade() {
        TypeFactory tf = TypeFactory.defaultInstance();

        CollectionLikeType colLike = tf.constructCollectionLikeType(String.class, Integer.class);
        assertEquals(String.class, colLike.getRawClass());
        assertEquals(Integer.class, colLike.getContentType().getRawClass());

        MapLikeType mapLike = tf.constructMapLikeType(String.class, Integer.class, Boolean.class);
        assertEquals(String.class, mapLike.getRawClass());
        assertEquals(Integer.class, mapLike.getKeyType().getRawClass());
        assertEquals(Boolean.class, mapLike.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testRawVariants() {
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
    }

    @Test(timeout = 4000)
    public void testFindTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType subMapType = tf.constructType(new TypeReference<HashMap<String, Long>>() {});
        JavaType[] params = tf.findTypeParameters(subMapType, Map.class);
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(Long.class, params[1].getRawClass());

        JavaType[] unmatched = tf.findTypeParameters(subMapType, List.class);
        assertNotNull(unmatched);
        assertEquals(0, unmatched.length);

        @SuppressWarnings("deprecation")
        JavaType[] deprecatedFind = tf.findTypeParameters(HashMap.class, Map.class);
        assertEquals(2, deprecatedFind.length);

        @SuppressWarnings("deprecation")
        JavaType[] deprecatedWithBindings = tf.findTypeParameters(HashMap.class, Map.class, TypeBindings.emptyBindings());
        assertEquals(2, deprecatedWithBindings.length);
    }

    @Test(timeout = 4000)
    public void testRecursiveTypesCycleResolution() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType recursiveType = tf.constructType(RecursiveNode.class);
        assertNotNull(recursiveType);
        assertEquals(RecursiveNode.class, recursiveType.getRawClass());

        JavaType selfRefType = tf.constructType(SelfReferential.class);
        assertNotNull(selfRefType);
        assertEquals(SelfReferential.class, selfRefType.getRawClass());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (databind#1384)
    // =========================================================================

    @Test(timeout = 4000)
    public void testMapKeyRefinement1384() {
        TypeFactory tf = TypeFactory.defaultInstance();

        // Construct a parameterized base map with explicit value and type handlers
        JavaType baseType = tf.constructMapType(Map.class, String.class, Integer.class)
                .withValueHandler("BASE_MAP_VALUE_HANDLER")
                .withTypeHandler("BASE_MAP_TYPE_HANDLER");

        // Specialize into concrete HashMap
        JavaType specialized = tf.constructSpecializedType(baseType, HashMap.class);

        // Ground Truth Check: defect #1384 caused handlers to be lost during subtype specialization
        assertNotNull("Specialized type must not be null", specialized);
        assertEquals("Subclass should be HashMap", HashMap.class, specialized.getRawClass());
        assertEquals("Value handler must be retained across specialization",
                "BASE_MAP_VALUE_HANDLER", specialized.getValueHandler());
        assertEquals("Type handler must be retained across specialization",
                "BASE_MAP_TYPE_HANDLER", specialized.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testCollectionSpecializationPreservesHandlers1384() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType baseType = tf.constructCollectionType(List.class, String.class)
                .withValueHandler("LIST_VAL_HANDLER")
                .withTypeHandler("LIST_TYPE_HANDLER");

        JavaType specialized = tf.constructSpecializedType(baseType, ArrayList.class);

        assertNotNull(specialized);
        assertEquals(ArrayList.class, specialized.getRawClass());
        assertEquals("Value handler must be retained across collection specialization",
                "LIST_VAL_HANDLER", specialized.getValueHandler());
        assertEquals("Type handler must be retained across collection specialization",
                "LIST_TYPE_HANDLER", specialized.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testObjectSpecializationPreservesHandlers1384() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType baseObj = tf.constructType(Object.class)
                .withValueHandler("OBJ_VAL_HANDLER")
                .withTypeHandler("OBJ_TYPE_HANDLER");

        JavaType specialized = tf.constructSpecializedType(baseObj, String.class);

        assertNotNull(specialized);
        assertEquals(String.class, specialized.getRawClass());
        assertEquals("Value handler must be retained across object specialization",
                "OBJ_VAL_HANDLER", specialized.getValueHandler());
        assertEquals("Type handler must be retained across object specialization",
                "OBJ_TYPE_HANDLER", specialized.getTypeHandler());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testFindClassPrimitivesAndFallbacks() throws Exception {
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

        assertEquals(String.class, tf.findClass("java.lang.String"));
    }

    @Test(expected = ClassNotFoundException.class, timeout = 4000)
    public void testFindClassNonExistent() throws Exception {
        TypeFactory.defaultInstance().findClass("com.fasterxml.jackson.nonexistent.BogusClass");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructFromCanonicalMalformed() {
        TypeFactory.defaultInstance().constructFromCanonical("java.util.List<invalid bracket");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructSpecializedTypeNotSubtype() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        tf.constructSpecializedType(listType, String.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructGeneralizedTypeNotSuperType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(List.class, String.class);
        tf.constructGeneralizedType(listType, Integer.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructTypeWithNullType() {
        TypeFactory.defaultInstance().constructType((Type) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructTypeWithUnrecognizedType() {
        Type dummyType = new Type() {
            @Override
            public String toString() {
                return "MockCustomType";
            }
        };
        TypeFactory.defaultInstance().constructType(dummyType);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testModifierReturningNullThrowsException() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeModifier nullMod = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return null;
            }
        };
        TypeFactory tfWithMod = tf.withModifier(nullMod);
        tfWithMod.constructType(String.class);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSpecializationShortcutBranches() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType listBase = tf.constructCollectionType(List.class, String.class);
        assertSame(listBase, tf.constructSpecializedType(listBase, List.class));

        JavaType arrayList = tf.constructSpecializedType(listBase, ArrayList.class);
        assertEquals(ArrayList.class, arrayList.getRawClass());

        JavaType linkedList = tf.constructSpecializedType(listBase, LinkedList.class);
        assertEquals(LinkedList.class, linkedList.getRawClass());

        JavaType setBase = tf.constructCollectionType(Set.class, String.class);
        JavaType hashSet = tf.constructSpecializedType(setBase, HashSet.class);
        assertEquals(HashSet.class, hashSet.getRawClass());

        JavaType treeSet = tf.constructSpecializedType(setBase, TreeSet.class);
        assertEquals(TreeSet.class, treeSet.getRawClass());

        JavaType mapBase = tf.constructMapType(Map.class, String.class, Integer.class);
        JavaType linkedHashMap = tf.constructSpecializedType(mapBase, LinkedHashMap.class);
        assertEquals(LinkedHashMap.class, linkedHashMap.getRawClass());

        JavaType treeMap = tf.constructSpecializedType(mapBase, TreeMap.class);
        assertEquals(TreeMap.class, treeMap.getRawClass());

        JavaType nonGeneric = tf.constructSpecializedType(listBase, NonGenericSubList.class);
        assertEquals(NonGenericSubList.class, nonGeneric.getRawClass());

        JavaType enumSetBase = tf.constructCollectionType(EnumSet.class, TestEnum.class);
        assertSame(enumSetBase, tf.constructSpecializedType(enumSetBase, EnumSet.class));
    }

    @Test(timeout = 4000)
    public void testSpecializationInterfaceAndComplexBindings() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType singleBase = tf.constructParametricType(SingleInterface.class, String.class);
        JavaType singleSpec = tf.constructSpecializedType(singleBase, SingleImpl.class);
        assertEquals(SingleImpl.class, singleSpec.getRawClass());
        assertEquals(String.class, singleSpec.containedType(0).getRawClass());

        JavaType mapBase = tf.constructMapType(Map.class, String.class, Integer.class);
        JavaType tripleSpec = tf.constructSpecializedType(mapBase, TripleMap.class);
        assertEquals(TripleMap.class, tripleSpec.getRawClass());
    }

    @Test(timeout = 4000)
    public void testGeneralizedTypeResolution() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType arrayListType = tf.constructCollectionType(ArrayList.class, String.class);
        assertSame(arrayListType, tf.constructGeneralizedType(arrayListType, ArrayList.class));

        JavaType listType = tf.constructGeneralizedType(arrayListType, List.class);
        assertEquals(List.class, listType.getRawClass());
        assertEquals(String.class, listType.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testMoreSpecificTypeMatrix() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType strType = tf.constructType(String.class);
        JavaType objType = tf.constructType(Object.class);

        assertSame(strType, tf.moreSpecificType(null, strType));
        assertSame(strType, tf.moreSpecificType(strType, null));
        assertSame(strType, tf.moreSpecificType(strType, strType));
        assertSame(strType, tf.moreSpecificType(strType, objType));
        assertSame(strType, tf.moreSpecificType(objType, strType));

        JavaType intType = tf.constructType(Integer.class);
        assertSame(strType, tf.moreSpecificType(strType, intType));
    }

    @Test(timeout = 4000)
    public void testMutantFactoryAndModifiers() {
        TypeFactory tf = TypeFactory.defaultInstance();

        ClassLoader cl = getClass().getClassLoader();
        TypeFactory tfWithCl = tf.withClassLoader(cl);
        assertSame(cl, tfWithCl.getClassLoader());

        LRUMap<Object, JavaType> cache = new LRUMap<Object, JavaType>(10, 50);
        TypeFactory tfWithCache = tf.withCache(cache);
        assertNotNull(tfWithCache);
        tfWithCache.clearCache();

        TypeModifier mod1 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeModifier mod2 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };

        TypeFactory tf1 = tf.withModifier(mod1);
        TypeFactory tf2 = tf1.withModifier(mod2);
        TypeFactory tfClearedMods = tf2.withModifier(null);
        assertNotNull(tfClearedMods);
    }

    @Test(timeout = 4000)
    public void testDeprecatedAndConvenienceMethods() {
        TypeFactory tf = TypeFactory.defaultInstance();

        @SuppressWarnings("deprecation")
        JavaType fromClassContext = tf.constructType(String.class, String.class);
        assertEquals(String.class, fromClassContext.getRawClass());

        @SuppressWarnings("deprecation")
        JavaType fromJavaTypeContext = tf.constructType(String.class, fromClassContext);
        assertEquals(String.class, fromJavaTypeContext.getRawClass());

        @SuppressWarnings("deprecation")
        JavaType unchecked = tf.uncheckedSimpleType(String.class);
        assertEquals(String.class, unchecked.getRawClass());

        @SuppressWarnings("deprecation")
        JavaType simple3Args = tf.constructSimpleType(String.class, String.class, new JavaType[0]);
        assertEquals(String.class, simple3Args.getRawClass());

        JavaType parametricFromClasses = tf.constructParametricType(List.class, String.class);
        assertEquals(List.class, parametricFromClasses.getRawClass());

        JavaType parametricFromTypes = tf.constructParametricType(List.class, tf.constructType(String.class));
        assertEquals(List.class, parametricFromTypes.getRawClass());

        @SuppressWarnings("deprecation")
        JavaType parametrizedLegacy1 = tf.constructParametrizedType(ArrayList.class, List.class, String.class);
        assertEquals(ArrayList.class, parametrizedLegacy1.getRawClass());

        @SuppressWarnings("deprecation")
        JavaType parametrizedLegacy2 = tf.constructParametrizedType(ArrayList.class, List.class, tf.constructType(String.class));
        assertEquals(ArrayList.class, parametrizedLegacy2.getRawClass());

        JavaType existingJavaType = tf.constructType(fromClassContext);
        assertSame(fromClassContext, existingJavaType);
    }
}