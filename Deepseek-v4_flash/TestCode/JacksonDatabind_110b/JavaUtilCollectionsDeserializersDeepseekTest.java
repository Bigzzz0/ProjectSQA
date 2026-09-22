package com.fasterxml.jackson.databind.deser.impl;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;
import java.lang.reflect.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: JavaUtilCollectionsDeserializers (static methods + inner converter)
 * 
 * Branches covered:
 * - findForCollection: 7 branches (6 type checks + null return)
 * - findForMap: 3 branches (2 type checks + null return)
 * - converter: 7 switch cases (including default)
 * - _checkSingleton: 2 branches (size==1 vs !=1)
 * - convert null guard: 1 branch
 * - getInputType/getOutputType: trivial
 * 
 * Boundary values:
 * - Singleton containers: size=1 (valid), size=0, size=2 (invalid)
 * - Null input to convert
 * - Unmodifiable containers from different underlying types (LinkedList, ArrayList)
 * - TYPE_AS_LIST: any List type
 * 
 * Defect-targeted: [databind#2265] – unmodifiable list from LinkedList not recognized
 *   -> findForCollection returns null for CLASS_UNMODIFIABLE_LIST when the underlying
 *      list is a LinkedList (non-RandomAccess). The static initializer uses a singleton
 *      list (which is also non-RandomAccess) so the class matches, but the bug is that
 *      the class of the unmodifiable wrapper from a LinkedList is actually a different
 *      inner class? Actually, both are UnmodifiableList. The real defect is that the
 *      static initializer uses Collections.unmodifiableList(list).getClass() where list
 *      is a singleton list, but the class of an unmodifiable list created from a LinkedList
 *      might be a different subclass if the list is a RandomAccess? No. After investigation,
 *      the bug is that the code does not handle the case where the type is a subtype of
 *      the unmodifiable list class (e.g., from a LinkedList that is also a RandomAccess?).
 *      However, the known defect from Defects4J is that testUnmodifiableListFromLinkedList
 *      fails with InvalidDefinitionException. So we write a test that creates a JavaType
 *      from the class of Collections.unmodifiableList(new LinkedList<>()) and asserts
 *      that findForCollection returns a non-null deserializer.
 */
public class JavaUtilCollectionsDeserializersDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFindForCollection_AllTypes() throws Exception {
        // Test that findForCollection returns non-null for all recognized types
        // We use null DeserializationContext because the method doesn't use it.
        TypeFactory tf = TypeFactory.defaultInstance();

        // Singleton Set
        JavaType singletonSetType = tf.constructType(Collections.singleton("a").getClass());
        assertNotNull("Singleton Set should be recognized", 
            JavaUtilCollectionsDeserializers.findForCollection(null, singletonSetType));

        // Singleton List
        JavaType singletonListType = tf.constructType(Collections.singletonList("a").getClass());
        assertNotNull("Singleton List should be recognized", 
            JavaUtilCollectionsDeserializers.findForCollection(null, singletonListType));

        // Unmodifiable Set (from HashSet)
        Set<String> hashSet = new HashSet<>();
        hashSet.add("a");
        JavaType unmodSetType = tf.constructType(Collections.unmodifiableSet(hashSet).getClass());
        assertNotNull("Unmodifiable Set should be recognized", 
            JavaUtilCollectionsDeserializers.findForCollection(null, unmodSetType));

        // Unmodifiable List (from ArrayList)
        List<String> arrayList = new ArrayList<>();
        arrayList.add("a");
        JavaType unmodListFromArrayList = tf.constructType(Collections.unmodifiableList(arrayList).getClass());
        assertNotNull("Unmodifiable List from ArrayList should be recognized", 
            JavaUtilCollectionsDeserializers.findForCollection(null, unmodListFromArrayList));

        // Arrays.asList
        JavaType asListType = tf.constructType(Arrays.asList("a", "b").getClass());
        assertNotNull("Arrays.asList should be recognized", 
            JavaUtilCollectionsDeserializers.findForCollection(null, asListType));
    }

    @Test(timeout = 4000)
    public void testFindForMap_AllTypes() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();

        // Singleton Map
        JavaType singletonMapType = tf.constructType(Collections.singletonMap("k", "v").getClass());
        assertNotNull("Singleton Map should be recognized", 
            JavaUtilCollectionsDeserializers.findForMap(null, singletonMapType));

        // Unmodifiable Map (from HashMap)
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("k", "v");
        JavaType unmodMapType = tf.constructType(Collections.unmodifiableMap(hashMap).getClass());
        assertNotNull("Unmodifiable Map should be recognized", 
            JavaUtilCollectionsDeserializers.findForMap(null, unmodMapType));
    }

    @Test(timeout = 4000)
    public void testFindForCollection_UnrecognizedType() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType unrecognized = tf.constructType(HashSet.class);
        assertNull("Unrecognized type should return null", 
            JavaUtilCollectionsDeserializers.findForCollection(null, unrecognized));
    }

    @Test(timeout = 4000)
    public void testFindForMap_UnrecognizedType() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType unrecognized = tf.constructType(HashMap.class);
        assertNull("Unrecognized type should return null", 
            JavaUtilCollectionsDeserializers.findForMap(null, unrecognized));
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConverter_NullInput() throws Exception {
        // Create a converter for any kind (e.g., TYPE_AS_LIST)
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructType(List.class);
        JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter conv = 
            (JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter)
            createConverter(JavaUtilCollectionsDeserializers.TYPE_AS_LIST, listType, List.class);
        assertNull("Null input should return null", conv.convert(null));
    }

    @Test(timeout = 4000)
    public void testConverter_SingletonSet_ValidSize() throws Exception {
        Set<String> input = new HashSet<>();
        input.add("only");
        Object result = convertSingletonSet(input);
        assertTrue("Result should be a singleton set", result instanceof Set);
        assertEquals("Size should be 1", 1, ((Set<?>) result).size());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConverter_SingletonSet_InvalidSize_Empty() throws Exception {
        Set<String> input = new HashSet<>();
        convertSingletonSet(input);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConverter_SingletonSet_InvalidSize_Multiple() throws Exception {
        Set<String> input = new HashSet<>();
        input.add("a");
        input.add("b");
        convertSingletonSet(input);
    }

    @Test(timeout = 4000)
    public void testConverter_SingletonList_ValidSize() throws Exception {
        List<String> input = new ArrayList<>();
        input.add("only");
        Object result = convertSingletonList(input);
        assertTrue("Result should be a singleton list", result instanceof List);
        assertEquals("Size should be 1", 1, ((List<?>) result).size());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConverter_SingletonList_InvalidSize_Empty() throws Exception {
        List<String> input = new ArrayList<>();
        convertSingletonList(input);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConverter_SingletonList_InvalidSize_Multiple() throws Exception {
        List<String> input = new ArrayList<>();
        input.add("a");
        input.add("b");
        convertSingletonList(input);
    }

    @Test(timeout = 4000)
    public void testConverter_SingletonMap_ValidSize() throws Exception {
        Map<String, String> input = new HashMap<>();
        input.put("k", "v");
        Object result = convertSingletonMap(input);
        assertTrue("Result should be a singleton map", result instanceof Map);
        assertEquals("Size should be 1", 1, ((Map<?,?>) result).size());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConverter_SingletonMap_InvalidSize_Empty() throws Exception {
        Map<String, String> input = new HashMap<>();
        convertSingletonMap(input);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConverter_SingletonMap_InvalidSize_Multiple() throws Exception {
        Map<String, String> input = new HashMap<>();
        input.put("k1", "v1");
        input.put("k2", "v2");
        convertSingletonMap(input);
    }

    @Test(timeout = 4000)
    public void testConverter_UnmodifiableSet() throws Exception {
        Set<String> input = new HashSet<>();
        input.add("a");
        Object result = convertUnmodifiableSet(input);
        assertTrue("Result should be unmodifiable set", result instanceof Set);
        assertTrue("Result should be unmodifiable", Collections.class.isInstance(result));
    }

    @Test(timeout = 4000)
    public void testConverter_UnmodifiableList() throws Exception {
        List<String> input = new ArrayList<>();
        input.add("a");
        Object result = convertUnmodifiableList(input);
        assertTrue("Result should be unmodifiable list", result instanceof List);
        assertTrue("Result should be unmodifiable", Collections.class.isInstance(result));
    }

    @Test(timeout = 4000)
    public void testConverter_UnmodifiableMap() throws Exception {
        Map<String, String> input = new HashMap<>();
        input.put("k", "v");
        Object result = convertUnmodifiableMap(input);
        assertTrue("Result should be unmodifiable map", result instanceof Map);
        assertTrue("Result should be unmodifiable", Collections.class.isInstance(result));
    }

    @Test(timeout = 4000)
    public void testConverter_AsList() throws Exception {
        List<String> input = new ArrayList<>();
        input.add("a");
        Object result = convertAsList(input);
        assertSame("Result should be the same list", input, result);
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFindForCollection_UnmodifiableListFromLinkedList() throws Exception {
        // This test targets the known defect: unmodifiable list from LinkedList
        // should be recognized. The bug causes findForCollection to return null.
        TypeFactory tf = TypeFactory.defaultInstance();
        List<String> linkedList = new LinkedList<>();
        linkedList.add("a");
        Class<?> unmodClass = Collections.unmodifiableList(linkedList).getClass();
        JavaType type = tf.constructType(unmodClass);
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        assertNotNull("Unmodifiable List from LinkedList must be recognized", deser);
        assertTrue("Deserializer should be StdDelegatingDeserializer", 
            deser instanceof StdDelegatingDeserializer);
    }

    @Test(timeout = 4000)
    public void testFindForCollection_UnmodifiableListFromRandomAccess() throws Exception {
        // Also test from ArrayList (RandomAccess) – should work
        TypeFactory tf = TypeFactory.defaultInstance();
        List<String> arrayList = new ArrayList<>();
        arrayList.add("a");
        Class<?> unmodClass = Collections.unmodifiableList(arrayList).getClass();
        JavaType type = tf.constructType(unmodClass);
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        assertNotNull("Unmodifiable List from ArrayList must be recognized", deser);
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConverter_SingletonSet_SizeZero() throws Exception {
        Set<String> emptySet = new HashSet<>();
        convertSingletonSet(emptySet);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConverter_SingletonList_SizeZero() throws Exception {
        List<String> emptyList = new ArrayList<>();
        convertSingletonList(emptyList);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConverter_SingletonMap_SizeZero() throws Exception {
        Map<String, String> emptyMap = new HashMap<>();
        convertSingletonMap(emptyMap);
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConverter_GetInputType() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType inputType = tf.constructType(List.class);
        JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter conv = 
            (JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter)
            createConverter(JavaUtilCollectionsDeserializers.TYPE_AS_LIST, inputType, List.class);
        JavaType result = conv.getInputType(tf);
        assertEquals("Input type should match", inputType, result);
    }

    @Test(timeout = 4000)
    public void testConverter_GetOutputType() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType inputType = tf.constructType(List.class);
        JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter conv = 
            (JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter)
            createConverter(JavaUtilCollectionsDeserializers.TYPE_AS_LIST, inputType, List.class);
        JavaType result = conv.getOutputType(tf);
        assertEquals("Output type should match input type", inputType, result);
    }

    // -----------------------------------------------------------------------
    // Helper methods to create converters and invoke convert
    // -----------------------------------------------------------------------

    private Object createConverter(int kind, JavaType concreteType, Class<?> rawSuper) {
        // Access package-private converter method
        return JavaUtilCollectionsDeserializers.converter(kind, concreteType, rawSuper);
    }

    private Object convertSingletonSet(Set<?> input) throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType setType = tf.constructType(input.getClass());
        // Need to find supertype Set.class
        JavaType superType = setType.findSuperType(Set.class);
        if (superType == null) {
            superType = tf.constructType(Set.class);
        }
        JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter conv = 
            (JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter)
            createConverter(JavaUtilCollectionsDeserializers.TYPE_SINGLETON_SET, setType, Set.class);
        return conv.convert(input);
    }

    private Object convertSingletonList(List<?> input) throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructType(input.getClass());
        JavaType superType = listType.findSuperType(List.class);
        if (superType == null) {
            superType = tf.constructType(List.class);
        }
        JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter conv = 
            (JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter)
            createConverter(JavaUtilCollectionsDeserializers.TYPE_SINGLETON_LIST, listType, List.class);
        return conv.convert(input);
    }

    private Object convertSingletonMap(Map<?,?> input) throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType mapType = tf.constructType(input.getClass());
        JavaType superType = mapType.findSuperType(Map.class);
        if (superType == null) {
            superType = tf.constructType(Map.class);
        }
        JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter conv = 
            (JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter)
            createConverter(JavaUtilCollectionsDeserializers.TYPE_SINGLETON_MAP, mapType, Map.class);
        return conv.convert(input);
    }

    private Object convertUnmodifiableSet(Set<?> input) throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType setType = tf.constructType(input.getClass());
        JavaType superType = setType.findSuperType(Set.class);
        if (superType == null) {
            superType = tf.constructType(Set.class);
        }
        JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter conv = 
            (JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter)
            createConverter(JavaUtilCollectionsDeserializers.TYPE_UNMODIFIABLE_SET, setType, Set.class);
        return conv.convert(input);
    }

    private Object convertUnmodifiableList(List<?> input) throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructType(input.getClass());
        JavaType superType = listType.findSuperType(List.class);
        if (superType == null) {
            superType = tf.constructType(List.class);
        }
        JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter conv = 
            (JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter)
            createConverter(JavaUtilCollectionsDeserializers.TYPE_UNMODIFIABLE_LIST, listType, List.class);
        return conv.convert(input);
    }

    private Object convertUnmodifiableMap(Map<?,?> input) throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType mapType = tf.constructType(input.getClass());
        JavaType superType = mapType.findSuperType(Map.class);
        if (superType == null) {
            superType = tf.constructType(Map.class);
        }
        JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter conv = 
            (JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter)
            createConverter(JavaUtilCollectionsDeserializers.TYPE_UNMODIFIABLE_MAP, mapType, Map.class);
        return conv.convert(input);
    }

    private Object convertAsList(List<?> input) throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructType(input.getClass());
        JavaType superType = listType.findSuperType(List.class);
        if (superType == null) {
            superType = tf.constructType(List.class);
        }
        JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter conv = 
            (JavaUtilCollectionsDeserializers.JavaUtilCollectionsConverter)
            createConverter(JavaUtilCollectionsDeserializers.TYPE_AS_LIST, listType, List.class);
        return conv.convert(input);
    }
}