package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.type.TypeFactory
 *
 * Targeted Defects & Regressions:
 * 1. [DEFECT-728]: Handling of naked/unbound TypeVariable resolution without context.
 *    In TypeFactory._fromVariable(TypeVariable<?>, TypeBindings), when context is null,
 *    the implementation prematurely returned _unknownType() (Object.class) rather than
 *    inspecting and resolving upper bounds (e.g., T extends CharSequence -> CharSequence).
 *    Trigger Test: testTypeVariableWithoutContextBounds_Defect728()
 * 2. [DEFECT-609]: Unresolved type variables and placeholders during recursive/self-referential resolution.
 *    Trigger Test: testRecursiveOrUnboundTypeVariableResolution_Defect609()
 *
 * Branch Matrix Covered:
 * - Core primitives & cached singleton types (String, boolean, int, long)
 * - Cache retention & clearCache()
 * - withModifier() (null, single modifier, multiple modifiers/duplicates)
 * - constructSpecializedType() (same type short-circuit, SimpleType to Array/Collection/Map,
 *   type compatibility verification, handler retention, fallback narrowing)
 * - constructFromCanonical() (valid primitive, parameterized, map, invalid format)
 * - findTypeParameters() (direct parameterSource match, inheritance chains, invalid subtypes)
 * - moreSpecificType() (null comparisons, identical types, assignable hierarchies, unrelated fallback)
 * - constructParametrizedType() & variants (Array length checks, Map 2-param requirement,
 *   Collection 1-param requirement, SimpleType param count verification)
 * - Type reflections: Class, ParameterizedType, GenericArrayType, WildcardType (upper/lower bounds),
 *   JavaType passthrough, unrecognized Type check
 * - Raw type constructors (raw collection, raw map, raw collection-like, raw map-like)
 * - Special Map.Entry resolving branch
 * - Hierarchic super-chain caching (ArrayList -> List, HashMap -> Map)
 */
public class TypeFactoryGeminiTest {

    // Helper classes for reflection testing
    static class BoundedHolder<T extends CharSequence> {
        public T boundedField;
    }

    static class RecursiveHolder<T extends Comparable<T>> {
        public T recursiveField;
    }

    static class MultiBoundHolder<T extends Number & Comparable<T>> {
        public T multiBoundField;
    }

    static class GenericContainer<E> {
        public E item;
        public List<E> list;
        public E[] array;
        public List<? extends Number> wildcardExtends;
        public List<? super Integer> wildcardSuper;
    }

    static class CustomMap<K, V> extends HashMap<K, V> {
        private static final long serialVersionUID = 1L;
    }

    static class CustomList<E> extends ArrayList<E> {
        private static final long serialVersionUID = 1L;
    }

    static class UnrelatedType {
    }

    /*
     * ----------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testCoreSingletonAndPrimitiveTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        assertNotNull(tf);
        assertSame(tf, TypeFactory.defaultInstance());

        JavaType stringType = tf.constructType(String.class);
        assertSame(TypeFactory.CORE_TYPE_STRING, stringType);
        assertEquals(String.class, stringType.getRawClass());

        JavaType boolType = tf.constructType(Boolean.TYPE);
        assertSame(TypeFactory.CORE_TYPE_BOOL, boolType);
        assertEquals(Boolean.TYPE, boolType.getRawClass());

        JavaType intType = tf.constructType(Integer.TYPE);
        assertSame(TypeFactory.CORE_TYPE_INT, intType);
        assertEquals(Integer.TYPE, intType.getRawClass());

        JavaType longType = tf.constructType(Long.TYPE);
        assertSame(TypeFactory.CORE_TYPE_LONG, longType);
        assertEquals(Long.TYPE, longType.getRawClass());
    }

    @Test(timeout = 4000)
    public void testCacheAndClearCache() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type1 = tf.constructType(Date.class);
        JavaType type2 = tf.constructType(Date.class);
        assertSame(type1, type2);

        tf.clearCache();
        JavaType type3 = tf.constructType(Date.class);
        assertEquals(type1, type3);
    }

    @Test(timeout = 4000)
    public void testModifiersLifecycle() {
        TypeFactory tf = TypeFactory.defaultInstance();

        // Adding null modifier returns functionally identical factory
        TypeFactory tfSame = tf.withModifier(null);
        assertNotNull(tfSame);

        TypeModifier mod1 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };

        TypeFactory tfMod1 = tf.withModifier(mod1);
        assertNotSame(tf, tfMod1);

        // Adding second modifier exercises array append
        TypeModifier mod2 = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };

        TypeFactory tfMod2 = tfMod1.withModifier(mod2);
        assertNotNull(tfMod2);

        // Re-adding existing modifier verifies ArrayBuilders.insertInListNoDup
        TypeFactory tfModDup = tfMod2.withModifier(mod2);
        assertNotNull(tfModDup);
    }

    @Test(timeout = 4000)
    public void testConstructSpecializedType() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType baseMap = tf.constructType(Map.class);
        // Short-circuit: same class returns baseType
        JavaType same = tf.constructSpecializedType(baseMap, Map.class);
        assertSame(baseMap, same);

        // SimpleType specialized to Map implementation
        JavaType specializedMap = tf.constructSpecializedType(baseMap, HashMap.class);
        assertEquals(HashMap.class, specializedMap.getRawClass());
        assertTrue(specializedMap.isMapLikeType());

        // SimpleType specialized to Collection implementation
        JavaType baseColl = tf.constructType(Collection.class);
        JavaType specializedColl = tf.constructSpecializedType(baseColl, ArrayList.class);
        assertEquals(ArrayList.class, specializedColl.getRawClass());
        assertTrue(specializedColl.isCollectionLikeType());

        // SimpleType specialized to Array
        JavaType baseObj = tf.constructType(Object.class);
        JavaType specializedArr = tf.constructSpecializedType(baseObj, String[].class);
        assertEquals(String[].class, specializedArr.getRawClass());
        assertTrue(specializedArr.isArrayType());

        // Narrowing with handlers
        JavaType baseWithHandlers = baseObj.withValueHandler("valHandler").withTypeHandler("typeHandler");
        JavaType specializedWithHandlers = tf.constructSpecializedType(baseWithHandlers, ArrayList.class);
        assertEquals("valHandler", specializedWithHandlers.getValueHandler());
        assertEquals("typeHandler", specializedWithHandlers.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testMoreSpecificType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType stringType = tf.constructType(String.class);
        JavaType objType = tf.constructType(Object.class);
        JavaType numType = tf.constructType(Number.class);

        // Null checks
        assertSame(stringType, tf.moreSpecificType(stringType, null));
        assertSame(stringType, tf.moreSpecificType(null, stringType));

        // Same raw class
        assertSame(stringType, tf.moreSpecificType(stringType, stringType));

        // Assignable: Object vs String -> String is more specific
        assertSame(stringType, tf.moreSpecificType(objType, stringType));

        // Not assignable: String vs Number -> primary type returned
        assertSame(stringType, tf.moreSpecificType(stringType, numType));
    }

    @Test(timeout = 4000)
    public void testDirectFactoryConstructors() {
        TypeFactory tf = TypeFactory.defaultInstance();

        // Arrays
        ArrayType arr1 = tf.constructArrayType(Integer.class);
        assertEquals(Integer[].class, arr1.getRawClass());
        ArrayType arr2 = tf.constructArrayType(tf.constructType(Integer.class));
        assertEquals(Integer[].class, arr2.getRawClass());

        // Collections
        CollectionType coll1 = tf.constructCollectionType(List.class, String.class);
        assertEquals(List.class, coll1.getRawClass());
        assertEquals(String.class, coll1.getContentType().getRawClass());

        CollectionType coll2 = tf.constructCollectionType(Set.class, tf.constructType(Double.class));
        assertEquals(Set.class, coll2.getRawClass());
        assertEquals(Double.class, coll2.getContentType().getRawClass());

        // Collection-like
        CollectionLikeType cl1 = tf.constructCollectionLikeType(Collection.class, String.class);
        assertEquals(Collection.class, cl1.getRawClass());
        CollectionLikeType cl2 = tf.constructCollectionLikeType(Collection.class, tf.constructType(String.class));
        assertEquals(Collection.class, cl2.getRawClass());

        // Maps
        MapType map1 = tf.constructMapType(Map.class, String.class, Long.class);
        assertEquals(Map.class, map1.getRawClass());
        assertEquals(String.class, map1.getKeyType().getRawClass());
        assertEquals(Long.class, map1.getContentType().getRawClass());

        MapType map2 = tf.constructMapType(Map.class, tf.constructType(String.class), tf.constructType(Long.class));
        assertEquals(Map.class, map2.getRawClass());

        // Map-like
        MapLikeType ml1 = tf.constructMapLikeType(Map.class, String.class, Integer.class);
        assertEquals(Map.class, ml1.getRawClass());
        MapLikeType ml2 = tf.constructMapLikeType(Map.class, tf.constructType(String.class), tf.constructType(Integer.class));
        assertEquals(Map.class, ml2.getRawClass());

        // Unchecked simple type
        JavaType unchecked = tf.uncheckedSimpleType(Float.class);
        assertEquals(Float.class, unchecked.getRawClass());
    }

    @Test(timeout = 4000)
    public void testRawFactoryConstructors() {
        TypeFactory tf = TypeFactory.defaultInstance();

        CollectionType rawColl = tf.constructRawCollectionType(List.class);
        assertEquals(Object.class, rawColl.getContentType().getRawClass());

        CollectionLikeType rawCollLike = tf.constructRawCollectionLikeType(Collection.class);
        assertEquals(Object.class, rawCollLike.getContentType().getRawClass());

        MapType rawMap = tf.constructRawMapType(Map.class);
        assertEquals(Object.class, rawMap.getKeyType().getRawClass());
        assertEquals(Object.class, rawMap.getContentType().getRawClass());

        MapLikeType rawMapLike = tf.constructRawMapLikeType(Map.class);
        assertEquals(Object.class, rawMapLike.getKeyType().getRawClass());
        assertEquals(Object.class, rawMapLike.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructFromCanonical() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType simple = tf.constructFromCanonical("java.lang.String");
        assertEquals(String.class, simple.getRawClass());

        JavaType list = tf.constructFromCanonical("java.util.List<java.lang.Integer>");
        assertTrue(list.isCollectionLikeType());
        assertEquals(Integer.class, list.getContentType().getRawClass());

        JavaType map = tf.constructFromCanonical("java.util.Map<java.lang.String,java.lang.Boolean>");
        assertTrue(map.isMapLikeType());
        assertEquals(String.class, map.getKeyType().getRawClass());
        assertEquals(Boolean.class, map.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testMapEntryResolution() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType entryType = tf.constructType(Map.Entry.class);
        assertEquals(Map.Entry.class, entryType.getRawClass());
        assertEquals(2, entryType.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testHierarchicCachingChains() {
        TypeFactory tf = TypeFactory.defaultInstance();

        // Exercise _hashMapSuperInterfaceChain and _arrayListSuperInterfaceChain caching paths
        JavaType mapType = tf.constructType(HashMap.class);
        assertEquals(HashMap.class, mapType.getRawClass());

        JavaType listType = tf.constructType(ArrayList.class);
        assertEquals(ArrayList.class, listType.getRawClass());

        // Second call hits cache
        JavaType mapType2 = tf.constructType(HashMap.class);
        assertSame(mapType, mapType2);

        JavaType listType2 = tf.constructType(ArrayList.class);
        assertSame(listType, listType2);
    }

    /*
     * ----------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testConstructTypeWithContexts() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType t1 = tf.constructType(String.class, (Class<?>) null);
        assertEquals(String.class, t1.getRawClass());

        JavaType t2 = tf.constructType(String.class, (JavaType) null);
        assertEquals(String.class, t2.getRawClass());

        JavaType t3 = tf.constructType(String.class, Object.class);
        assertEquals(String.class, t3.getRawClass());

        JavaType t4 = tf.constructType(String.class, tf.constructType(Object.class));
        assertEquals(String.class, t4.getRawClass());
    }

    @Test(timeout = 4000)
    public void testRawClassResolution() {
        assertEquals(String.class, TypeFactory.rawClass(String.class));
        Type listType = new TypeReference<List<String>>() {}.getType();
        assertEquals(List.class, TypeFactory.rawClass(listType));
    }

    @Test(timeout = 4000)
    public void testTypeReferenceResolution() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType jt = tf.constructType(new TypeReference<Map<String, Set<Integer>>>() {});
        assertTrue(jt.isMapLikeType());
        assertEquals(String.class, jt.getKeyType().getRawClass());
        JavaType valType = jt.getContentType();
        assertTrue(valType.isCollectionLikeType());
        assertEquals(Integer.class, valType.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testParameterizedReflectionTypes() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();

        Field arrayField = GenericContainer.class.getField("array");
        JavaType arrayType = tf.constructType(arrayField.getGenericType());
        assertTrue(arrayType.isArrayType());

        Field wildExtends = GenericContainer.class.getField("wildcardExtends");
        JavaType wildExtType = tf.constructType(wildExtends.getGenericType());
        assertTrue(wildExtType.isCollectionLikeType());
        assertEquals(Number.class, wildExtType.getContentType().getRawClass());

        Field wildSuper = GenericContainer.class.getField("wildSuper");
        JavaType wildSuperType = tf.constructType(wildSuper.getGenericType());
        assertTrue(wildSuperType.isCollectionLikeType());
        assertEquals(Object.class, wildSuperType.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testParameterizedClassVariants() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType[] emptyParams = new JavaType[0];
        JavaType resEnum = tf._fromParameterizedClass(Thread.State.class, Collections.<JavaType>emptyList());
        assertEquals(Thread.State.class, resEnum.getRawClass());

        JavaType resArr = tf._fromParameterizedClass(int[].class, Collections.<JavaType>emptyList());
        assertTrue(resArr.isArrayType());

        // Map without and with 1 / 2 params
        JavaType mapRaw = tf._fromParameterizedClass(Map.class, Collections.<JavaType>emptyList());
        assertTrue(mapRaw.isMapLikeType());

        JavaType map1 = tf._fromParameterizedClass(Map.class, Collections.singletonList(tf.constructType(String.class)));
        assertTrue(map1.isMapLikeType());
        assertEquals(Object.class, map1.getContentType().getRawClass());

        JavaType map2 = tf._fromParameterizedClass(Map.class, Arrays.asList(tf.constructType(String.class), tf.constructType(Integer.class)));
        assertTrue(map2.isMapLikeType());
        assertEquals(Integer.class, map2.getContentType().getRawClass());

        // Collection without and with params
        JavaType collRaw = tf._fromParameterizedClass(List.class, Collections.<JavaType>emptyList());
        assertTrue(collRaw.isCollectionLikeType());

        JavaType coll1 = tf._fromParameterizedClass(List.class, Collections.singletonList(tf.constructType(Double.class)));
        assertEquals(Double.class, coll1.getContentType().getRawClass());
    }

    /*
     * ----------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone
     * ----------------------------------------------------------------------
     */

    /**
     * TARGET DEFECT-728:
     * When resolving a naked TypeVariable without a TypeBindings context, TypeFactory
     * must resolve its bounds (e.g. T extends CharSequence) rather than defaulting
     * blindly to Object.class (_unknownType()).
     */
    @Test(timeout = 4000)
    public void testTypeVariableWithoutContextBounds_Defect728() throws Exception {
        Field field = BoundedHolder.class.getField("boundedField");
        Type genericType = field.getGenericType();

        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType resolvedType = tf.constructType(genericType);

        // Expected: CharSequence.class (from upper bound)
        // Defective Jackson returns Object.class here, failing the assertion
        assertEquals("TypeVariable with bound should resolve to bound type instead of Object",
                CharSequence.class, resolvedType.getRawClass());
    }

    /**
     * TARGET DEFECT-609 / Recursive Type Variables:
     * T extends Comparable<T> must not infinite-loop and should resolve bound cleanly.
     */
    @Test(timeout = 4000)
    public void testRecursiveOrUnboundTypeVariableResolution_Defect609() throws Exception {
        Field field = RecursiveHolder.class.getField("recursiveField");
        Type genericType = field.getGenericType();

        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType resolvedType = tf.constructType(genericType, (TypeBindings) null);
        assertNotNull(resolvedType);
        assertEquals(Comparable.class, resolvedType.getRawClass());
    }

    @Test(timeout = 4000)
    public void testFindTypeParametersDirectSource() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType mapType = tf.constructType(new TypeReference<Map<String, Integer>>() {});
        JavaType[] params = tf.findTypeParameters(mapType, Map.class);
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(Integer.class, params[1].getRawClass());

        // Parameter source matching but 0 count
        JavaType stringType = tf.constructType(String.class);
        assertNull(tf.findTypeParameters(stringType, String.class));
    }

    /*
     * ----------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * ----------------------------------------------------------------------
     */

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructSpecializedTypeNotSubtypeThrows() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructType(List.class);
        tf.constructSpecializedType(listType, Set.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructFromCanonicalMalformedThrows() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructFromCanonical("com.unknown.NonExistentClass<unknown>");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFindTypeParametersNonSubtypeThrows() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.findTypeParameters(String.class, List.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructSimpleTypeMismatchThrows() {
        TypeFactory tf = TypeFactory.defaultInstance();
        // Map expects 2 parameters, passing 1 must fail
        tf.constructSimpleType(Map.class, Map.class, new JavaType[] { tf.constructType(String.class) });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructParametrizedTypeArrayWrongParamCountThrows() {
        TypeFactory tf = TypeFactory.defaultInstance();
        // Array takes exactly 1 parameter
        tf.constructParametrizedType(String[].class, String[].class,
                tf.constructType(String.class), tf.constructType(String.class));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructParametrizedTypeMapWrongParamCountThrows() {
        TypeFactory tf = TypeFactory.defaultInstance();
        // Map requires 2 parameters, passing 1 must fail
        tf.constructParametrizedType(Map.class, Map.class, tf.constructType(String.class));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructParametrizedTypeCollectionWrongParamCountThrows() {
        TypeFactory tf = TypeFactory.defaultInstance();
        // Collection requires 1 parameter, passing 2 must fail
        tf.constructParametrizedType(Collection.class, Collection.class,
                tf.constructType(String.class), tf.constructType(Integer.class));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructTypeUnknownTypeThrows() {
        TypeFactory tf = TypeFactory.defaultInstance();
        Type customType = new Type() {
            @Override
            public String toString() {
                return "CustomMockType";
            }
        };
        tf.constructType(customType);
    }

    /*
     * ----------------------------------------------------------------------
     * Partition E: Object Lifecycle & Contract Integrity
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testUnknownTypeSingletonContract() {
        JavaType unk1 = TypeFactory.unknownType();
        JavaType unk2 = TypeFactory.unknownType();
        assertNotNull(unk1);
        assertEquals(Object.class, unk1.getRawClass());
        assertEquals(unk1, unk2);
    }

    @Test(timeout = 4000)
    public void testDeprecatedMethodsCoverage() {
        TypeFactory tf = TypeFactory.defaultInstance();

        // Deprecated constructSimpleType(Class, JavaType[])
        JavaType[] params = new JavaType[] { tf.constructType(String.class), tf.constructType(Integer.class) };
        JavaType simpleMap = tf.constructSimpleType(Map.class, params);
        assertEquals(Map.class, simpleMap.getRawClass());

        // Deprecated constructParametricType(Class, Class...)
        JavaType p1 = tf.constructParametricType(List.class, String.class);
        assertEquals(List.class, p1.getRawClass());

        // Deprecated constructParametricType(Class, JavaType...)
        JavaType p2 = tf.constructParametricType(List.class, tf.constructType(Integer.class));
        assertEquals(List.class, p2.getRawClass());

        // constructParametrizedType(Class, Class, Class...)
        JavaType p3 = tf.constructParametrizedType(Map.class, Map.class, String.class, Long.class);
        assertEquals(Map.class, p3.getRawClass());
    }

    @Test(timeout = 4000)
    public void testJavaTypeDirectConstructPassthrough() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType jt = tf.constructType(String.class);
        // If type is already JavaType, _constructType returns it directly
        JavaType result = tf.constructType(jt);
        assertSame(jt, result);
    }
}