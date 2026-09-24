/* [Branch & Defect Analysis Matrix]
 * Target: com.fasterxml.jackson.databind.type.TypeFactory
 *
 * 1. DEFECT-TARGETED ZONE (databind#810 / Defects4J ground truth):
 *    - Defect: Properties.class extends Hashtable<Object,Object>, causing type resolution
 *      to assign Object.class as key and value types. Correct specification requires
 *      Properties to resolve with String.class for both key and content types.
 *    - Target Method: _mapType(Class<?> rawClass) / constructType(Properties.class)
 *    - Target Assertion: keyType == String.class and contentType == String.class.
 *
 * 2. BRANCH & DECISION COVERAGE:
 *    - Core Type caching & short-circuits: String, Boolean.TYPE, Integer.TYPE, Long.TYPE.
 *    - Type resolution branches: Class, ParameterizedType, GenericArrayType, TypeVariable,
 *      WildcardType, JavaType passthrough, unrecognized Type check.
 *    - Specialized Type Construction (constructSpecializedType):
 *      * baseType.getRawClass() == subclass (early exit)
 *      * baseType is SimpleType -> subclass is Array, Map, Collection
 *      * incompatible subclass throws IllegalArgumentException
 *      * copying of valueHandler and typeHandler
 *      * narrowBy fallback for non-SimpleType
 *    - Parametric construction (constructParametrizedType / constructParametricType):
 *      * Array (parameterTypes.length == 1 vs != 1)
 *      * Map (parameterTypes.length == 2 vs != 2)
 *      * Collection (parameterTypes.length == 1 vs != 1)
 *      * SimpleType (parameterTarget.getTypeParameters mismatch check)
 *    - Type hierarchy & interface chains:
 *      * HashMap -> Map (_hashMapSuperInterfaceChain)
 *      * ArrayList -> List (_arrayListSuperInterfaceChain)
 *      * Arbitrary classes & interfaces (_findSuperClassChain, _findSuperInterfaceChain)
 *    - AtomicReference & Map.Entry special handling.
 *    - moreSpecificType: type1 == null, type2 == null, raw1 == raw2, raw1.isAssignableFrom(raw2).
 *    - TypeModifier mechanics: withModifier(null), withModifier(mod) chained, container vs non-container.
 *    - Canonical parsing & clearCache.
 */

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

public class TypeFactoryGeminiTest {

    // Helper generic classes for deep branch resolution
    static class CustomMap<K, V> extends HashMap<K, V> {}
    static class StringKeyMap<V> implements Map<String, V> {
        public int size() { return 0; }
        public boolean isEmpty() { return false; }
        public boolean containsKey(Object key) { return false; }
        public boolean containsValue(Object value) { return false; }
        public V get(Object key) { return null; }
        public V put(String key, V value) { return null; }
        public V remove(Object key) { return null; }
        public void putAll(Map<? extends String, ? extends V> m) {}
        public void clear() {}
        public Set<String> keySet() { return null; }
        public Collection<V> values() { return null; }
        public Set<Entry<String, V>> entrySet() { return null; }
    }
    static class BoundedContainer<T extends Number & Comparable<T>> {
        public T item;
    }
    static class RecursiveComparable<T extends Comparable<T>> {
        public T element;
    }
    static class WildcardHolder {
        public List<? extends Number> numbers;
        public List<?> objects;
    }
    static class GenericArrayHolder<T> {
        public T[] genericArray;
    }

    /*
     * =========================================================================
     * Partition C: Defect-Targeted Branch Zone (databind#810)
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testPropertiesResolutionKeyAndContentType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType propType = tf.constructType(Properties.class);

        assertTrue("Properties must be resolved as a MapType", propType.isMapLikeType());
        assertTrue("Properties must be MapType instance", propType instanceof MapType);
        
        // GROUND TRUTH DEFECT TRIGGER:
        // Properties extends Hashtable<Object, Object>. Without explicit special casing,
        // Jackson resolves key and content to Object.class instead of String.class.
        assertSame("Properties keyType must be String.class",
                String.class, propType.getKeyType().getRawClass());
        assertSame("Properties contentType must be String.class",
                propType.getContentType().getRawClass(), String.class);
    }

    /*
     * =========================================================================
     * Partition A: Core Functional Logic & State Transitions
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testCoreSingletonAndPrimitives() {
        TypeFactory tf = TypeFactory.defaultInstance();
        assertNotNull(tf);
        assertSame(tf, TypeFactory.defaultInstance());

        JavaType stringType = tf.constructType(String.class);
        assertSame(TypeFactory.CORE_TYPE_STRING, stringType);
        assertEquals(String.class, stringType.getRawClass());

        JavaType boolType = tf.constructType(Boolean.TYPE);
        assertSame(TypeFactory.CORE_TYPE_BOOL, boolType);

        JavaType intType = tf.constructType(Integer.TYPE);
        assertSame(TypeFactory.CORE_TYPE_INT, intType);

        JavaType longType = tf.constructType(Long.TYPE);
        assertSame(TypeFactory.CORE_TYPE_LONG, longType);

        JavaType unknown = TypeFactory.unknownType();
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());

        Class<?> raw = TypeFactory.rawClass(new TypeReference<List<String>>() {}.getType());
        assertEquals(List.class, raw);
        assertEquals(String.class, TypeFactory.rawClass(String.class));
    }

    @Test(timeout = 4000)
    public void testConstructSpecializedTypeBasicAndHandlers() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseMap = tf.constructType(Map.class);
        JavaType specialized = tf.constructSpecializedType(baseMap, HashMap.class);
        assertEquals(HashMap.class, specialized.getRawClass());
        assertTrue(specialized instanceof MapType);

        // Same class optimization branch
        JavaType same = tf.constructSpecializedType(baseMap, Map.class);
        assertSame(baseMap, same);

        // Handler propagation branch
        JavaType baseWithHandlers = tf.constructType(Object.class)
                .withValueHandler("VAL_HANDLER")
                .withTypeHandler("TYPE_HANDLER");
        JavaType specList = tf.constructSpecializedType(baseWithHandlers, ArrayList.class);
        assertEquals(ArrayList.class, specList.getRawClass());
        assertEquals("VAL_HANDLER", specList.getValueHandler());
        assertEquals("TYPE_HANDLER", specList.getTypeHandler());

        // Array specialization
        JavaType specArray = tf.constructSpecializedType(baseWithHandlers, Object[].class);
        assertTrue(specArray.isArrayType());
        assertEquals("VAL_HANDLER", specArray.getValueHandler());
        assertEquals("TYPE_HANDLER", specArray.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testHierarchicTypeCachingForHashMapAndArrayList() {
        TypeFactory tf = TypeFactory.defaultInstance();
        
        // Exercises _hashMapSuperInterfaceChain
        JavaType mapType = tf.constructType(HashMap.class);
        assertNotNull(mapType);
        assertEquals(HashMap.class, mapType.getRawClass());
        
        // Second call hits _cachedHashMapType
        JavaType mapType2 = tf.constructType(HashMap.class);
        assertSame(mapType, mapType2);

        // Exercises _arrayListSuperInterfaceChain
        JavaType listType = tf.constructType(ArrayList.class);
        assertNotNull(listType);
        assertEquals(ArrayList.class, listType.getRawClass());

        // Second call hits _cachedArrayListType
        JavaType listType2 = tf.constructType(ArrayList.class);
        assertSame(listType, listType2);
    }

    @Test(timeout = 4000)
    public void testAtomicReferenceAndMapEntryResolution() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType refType = tf.constructType(new TypeReference<AtomicReference<Long>>() {});
        assertTrue(refType.isReferenceType());
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(Long.class, refType.getContentType().getRawClass());

        JavaType rawRef = tf.constructType(AtomicReference.class);
        assertTrue(rawRef.isReferenceType());
        assertEquals(Object.class, rawRef.getContentType().getRawClass());

        JavaType entryType = tf.constructType(new TypeReference<Map.Entry<String, Integer>>() {});
        assertEquals(Map.Entry.class, entryType.getRawClass());
        assertEquals(String.class, entryType.containedType(0).getRawClass());
        assertEquals(Integer.class, entryType.containedType(1).getRawClass());

        JavaType rawEntry = tf.constructType(Map.Entry.class);
        assertEquals(Map.Entry.class, rawEntry.getRawClass());
        assertEquals(Object.class, rawEntry.containedType(0).getRawClass());
        assertEquals(Object.class, rawEntry.containedType(1).getRawClass());
    }

    @Test(timeout = 4000)
    public void testMoreSpecificTypeBranches() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType objType = tf.constructType(Object.class);
        JavaType strType = tf.constructType(String.class);
        JavaType numType = tf.constructType(Number.class);

        // Null checks
        assertSame(strType, tf.moreSpecificType(null, strType));
        assertSame(strType, tf.moreSpecificType(strType, null));

        // Same raw class
        JavaType strType2 = tf.constructType(String.class);
        assertSame(strType, tf.moreSpecificType(strType, strType2));

        // Subclass relationship
        assertSame(strType, tf.moreSpecificType(objType, strType));
        assertSame(strType, tf.moreSpecificType(strType, objType));

        // Unrelated types -> primary returned
        assertSame(strType, tf.moreSpecificType(strType, numType));
    }

    @Test(timeout = 4000)
    public void testFindTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();

        // Direct parameter source match branch
        JavaType compType = tf.constructParametrizedType(ArrayList.class, List.class, String.class);
        JavaType[] params = tf.findTypeParameters(compType, List.class);
        assertNotNull(params);
        assertEquals(1, params.length);
        assertEquals(String.class, params[0].getRawClass());

        // Parameter source with 0 parameters returns null
        JavaType nonGen = tf.constructType(String.class);
        JavaType[] emptyParams = tf.findTypeParameters(nonGen, String.class);
        assertNull(emptyParams);

        // Subclass inheritance chain resolution
        JavaType[] mapParams = tf.findTypeParameters(StringKeyMap.class, Map.class);
        assertNotNull(mapParams);
        assertEquals(2, mapParams.length);
        assertEquals(String.class, mapParams[0].getRawClass());
        assertEquals(Object.class, mapParams[1].getRawClass());
    }

    /*
     * =========================================================================
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testConstructParametrizedTypeValidations() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType strType = tf.constructType(String.class);
        JavaType intType = tf.constructType(Integer.class);

        // Map parameterization: requires exactly 2 parameters
        JavaType mapType = tf.constructParametrizedType(HashMap.class, Map.class, strType, intType);
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(strType, mapType.getKeyType());
        assertEquals(intType, mapType.getContentType());

        // Collection parameterization: requires exactly 1 parameter
        JavaType listType = tf.constructParametrizedType(ArrayList.class, List.class, strType);
        assertEquals(ArrayList.class, listType.getRawClass());
        assertEquals(strType, listType.getContentType());

        // Array parameterization: requires exactly 1 parameter
        JavaType arrType = tf.constructParametrizedType(String[].class, String[].class, strType);
        assertTrue(arrType.isArrayType());
        assertEquals(strType, arrType.getContentType());

        // Deprecated variant check
        @SuppressWarnings("deprecation")
        JavaType depType = tf.constructParametricType(ArrayList.class, String.class);
        assertEquals(ArrayList.class, depType.getRawClass());
        assertEquals(String.class, depType.getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructParametrizedTypeArrayInvalidCount() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType strType = tf.constructType(String.class);
        tf.constructParametrizedType(String[].class, String[].class, strType, strType);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructParametrizedTypeMapInvalidCount() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType strType = tf.constructType(String.class);
        tf.constructParametrizedType(HashMap.class, Map.class, strType);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructParametrizedTypeCollectionInvalidCount() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType strType = tf.constructType(String.class);
        tf.constructParametrizedType(ArrayList.class, Collection.class, strType, strType);
    }

    @Test(timeout = 4000)
    public void testConstructRawContainerVariants() {
        TypeFactory tf = TypeFactory.defaultInstance();

        CollectionType rawColl = tf.constructRawCollectionType(ArrayList.class);
        assertEquals(ArrayList.class, rawColl.getRawClass());
        assertEquals(Object.class, rawColl.getContentType().getRawClass());

        CollectionLikeType rawCollLike = tf.constructRawCollectionLikeType(ArrayList.class);
        assertEquals(ArrayList.class, rawCollLike.getRawClass());
        assertEquals(Object.class, rawCollLike.getContentType().getRawClass());

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
    public void testConstructFromCanonicalAndRoundTrip() {
        TypeFactory tf = TypeFactory.defaultInstance();
        String canonical = "java.util.Map<java.lang.String,java.lang.Integer>";
        JavaType parsed = tf.constructFromCanonical(canonical);

        assertTrue(parsed.isMapLikeType());
        assertEquals(String.class, parsed.getKeyType().getRawClass());
        assertEquals(Integer.class, parsed.getContentType().getRawClass());
        assertEquals(canonical, parsed.toCanonical());
    }

    /*
     * =========================================================================
     * Partition D: Exception & Defensive Guard Paths
     * =========================================================================
     */

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructTypeNullThrowsException() {
        TypeFactory.defaultInstance().constructType((Type) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSpecializedTypeIncompatibleSubclassThrowsException() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructType(List.class);
        // List is not assignable to HashMap
        tf.constructSpecializedType(listType, HashMap.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFindTypeParametersNotSubtypeThrows() {
        TypeFactory tf = TypeFactory.defaultInstance();
        // String does not implement Map
        tf.findTypeParameters(String.class, Map.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructSimpleTypeArityMismatchThrows() {
        TypeFactory tf = TypeFactory.defaultInstance();
        // Map expects 2 parameters; providing 1 must throw IllegalArgumentException
        tf.constructSimpleType(Map.class, Map.class, new JavaType[] { tf.constructType(String.class) });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructFromCanonicalMalformed() {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructFromCanonical("java.util.List<invalid.class.Name?>");
    }

    /*
     * =========================================================================
     * Partition E: Object Lifecycle, Reflection Resolution & Modifiers
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testTypeModifierPipeline() {
        TypeFactory base = TypeFactory.defaultInstance();
        // withModifier(null) returns new equivalent instance
        TypeFactory mod0 = base.withModifier(null);
        assertNotNull(mod0);

        final JavaType overrideType = base.constructType(Double.class);
        TypeModifier mod = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                if (type.getRawClass() == Float.class) {
                    return overrideType;
                }
                return type;
            }
        };

        TypeFactory customTf = base.withModifier(mod);
        JavaType resolvedFloat = customTf.constructType(Float.class);
        // Modified to Double
        assertSame(overrideType, resolvedFloat);

        // Container types should not be modified directly by simple modifier
        JavaType listFloat = customTf.constructCollectionType(ArrayList.class, Float.class);
        assertTrue(listFloat.isCollectionLikeType());
        // Component float was modified to Double
        assertEquals(Double.class, listFloat.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testClearCache() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t1 = tf.constructType(CustomMap.class);
        assertNotNull(t1);
        tf.clearCache();
        JavaType t2 = tf.constructType(CustomMap.class);
        assertNotNull(t2);
        // After clearing cache, new instance is constructed with identical structure
        assertEquals(t1, t2);
    }

    @Test(timeout = 4000)
    public void testGenericArrayTypeAndTypeVariables() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();

        Field arrayField = GenericArrayHolder.class.getField("genericArray");
        JavaType arrayType = tf.constructType(arrayField.getGenericType());
        assertTrue(arrayType.isArrayType());
        assertEquals(Object.class, arrayType.getContentType().getRawClass());

        Field numField = BoundedContainer.class.getField("item");
        JavaType boundType = tf.constructType(numField.getGenericType());
        // Bound is Number & Comparable<T>; first bound is Number
        assertEquals(Number.class, boundType.getRawClass());

        Field recField = RecursiveComparable.class.getField("element");
        JavaType recType = tf.constructType(recField.getGenericType());
        assertEquals(Comparable.class, recType.getRawClass());
    }

    @Test(timeout = 4000)
    public void testWildcardResolution() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();

        Field boundedWildcard = WildcardHolder.class.getField("numbers");
        JavaType numList = tf.constructType(boundedWildcard.getGenericType());
        assertEquals(List.class, numList.getRawClass());
        assertEquals(Number.class, numList.getContentType().getRawClass());

        Field unboundedWildcard = WildcardHolder.class.getField("objects");
        JavaType objList = tf.constructType(unboundedWildcard.getGenericType());
        assertEquals(List.class, objList.getRawClass());
        assertEquals(Object.class, objList.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testDirectFactoryConstructors() {
        TypeFactory tf = TypeFactory.defaultInstance();

        JavaType simpleString = tf.uncheckedSimpleType(String.class);
        assertEquals(String.class, simpleString.getRawClass());

        JavaType arrFromClass = tf.constructArrayType(Integer.class);
        assertTrue(arrFromClass.isArrayType());
        assertEquals(Integer.class, arrFromClass.getContentType().getRawClass());

        JavaType arrFromType = tf.constructArrayType(tf.constructType(Long.class));
        assertTrue(arrFromType.isArrayType());
        assertEquals(Long.class, arrFromType.getContentType().getRawClass());

        MapLikeType mlType = tf.constructMapLikeType(Map.class, String.class, Integer.class);
        assertEquals(Map.class, mlType.getRawClass());
        assertEquals(String.class, mlType.getKeyType().getRawClass());
        assertEquals(Integer.class, mlType.getContentType().getRawClass());

        CollectionLikeType clType = tf.constructCollectionLikeType(List.class, String.class);
        assertEquals(List.class, clType.getRawClass());
        assertEquals(String.class, clType.getContentType().getRawClass());
    }
}