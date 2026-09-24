package com.fasterxml.jackson.databind.deser.impl;

import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: JavaUtilCollectionsDeserializers
 * Method / Branch Coverage Goals:
 * 1. findForCollection(ctxt, type):
 *    - Branch 1: type == CLASS_AS_ARRAYS_LIST (Arrays.asList(...)) -> TYPE_AS_LIST delegator
 *    - Branch 2: type == CLASS_SINGLETON_LIST (Collections.singletonList(...)) -> TYPE_SINGLETON_LIST
 *    - Branch 3: type == CLASS_SINGLETON_SET (Collections.singleton(...)) -> TYPE_SINGLETON_SET
 *    - Branch 4: type == CLASS_UNMODIFIABLE_LIST (Collections.unmodifiableList(RandomAccess)) -> TYPE_UNMODIFIABLE_LIST
 *    - Branch 4b (DEFECT databind#2265): Collections.unmodifiableList(LinkedList) (non-RandomAccess)
 *    - Branch 5: type == CLASS_UNMODIFIABLE_SET (Collections.unmodifiableSet(...)) -> TYPE_UNMODIFIABLE_SET
 *    - Branch 6: None of the above -> returns null (e.g., ArrayList, HashSet, non-collection types)
 * 2. findForMap(ctxt, type):
 *    - Branch 1: type == CLASS_SINGLETON_MAP (Collections.singletonMap(...)) -> TYPE_SINGLETON_MAP
 *    - Branch 2: type == CLASS_UNMODIFIABLE_MAP (Collections.unmodifiableMap(...)) -> TYPE_UNMODIFIABLE_MAP
 *    - Branch 3: None of the above -> returns null (e.g., HashMap, TreeMap)
 * 3. JavaUtilCollectionsConverter.convert(value):
 *    - Null check: value == null -> returns null
 *    - TYPE_SINGLETON_SET: size == 1 -> success; size != 1 -> IllegalArgumentException
 *    - TYPE_SINGLETON_LIST: size == 1 -> success; size != 1 -> IllegalArgumentException
 *    - TYPE_SINGLETON_MAP: size == 1 -> success; size != 1 -> IllegalArgumentException
 *    - TYPE_UNMODIFIABLE_SET: valid set -> unmodifiable set
 *    - TYPE_UNMODIFIABLE_LIST: valid list -> unmodifiable list
 *    - TYPE_UNMODIFIABLE_MAP: valid map -> unmodifiable map
 *    - TYPE_AS_LIST / default: returns list / value as-is
 * 4. JavaUtilCollectionsConverter metadata methods:
 *    - getInputType(TypeFactory)
 *    - getOutputType(TypeFactory)
 * ----------------------------------------------------------------------------------------------------
 */
public class JavaUtilCollectionsDeserializersGeminiTest {

    private final ObjectMapper _mapper = new ObjectMapper();
    private final TypeFactory _typeFactory = TypeFactory.defaultInstance();

    // =========================================================================
    // PARTITION A: Core Functional Logic & Deserialization Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFindForCollectionArraysAsList() throws Exception {
        Class<?> targetCls = Arrays.asList("a", "b").getClass();
        JavaType type = _typeFactory.constructType(targetCls);

        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        assertNotNull("Deserializer should be found for Arrays.asList", deser);
        assertTrue(deser instanceof StdDelegatingDeserializer);

        List<?> result = _mapper.readValue("[\"x\", \"y\"]", targetCls);
        assertEquals(2, result.size());
        assertEquals("x", result.get(0));
        assertEquals("y", result.get(1));
    }

    @Test(timeout = 4000)
    public void testFindForCollectionSingletonList() throws Exception {
        Class<?> targetCls = Collections.singletonList("a").getClass();
        JavaType type = _typeFactory.constructType(targetCls);

        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        assertNotNull("Deserializer should be found for Collections.singletonList", deser);
        assertTrue(deser instanceof StdDelegatingDeserializer);

        List<?> result = _mapper.readValue("[\"hello\"]", targetCls);
        assertEquals(1, result.size());
        assertEquals("hello", result.get(0));
        assertEquals(targetCls, result.getClass());
    }

    @Test(timeout = 4000)
    public void testFindForCollectionSingletonSet() throws Exception {
        Class<?> targetCls = Collections.singleton("a").getClass();
        JavaType type = _typeFactory.constructType(targetCls);

        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        assertNotNull("Deserializer should be found for Collections.singleton", deser);
        assertTrue(deser instanceof StdDelegatingDeserializer);

        Set<?> result = _mapper.readValue("[\"item\"]", targetCls);
        assertEquals(1, result.size());
        assertTrue(result.contains("item"));
        assertEquals(targetCls, result.getClass());
    }

    @Test(timeout = 4000)
    public void testFindForCollectionUnmodifiableListRandomAccess() throws Exception {
        Class<?> targetCls = Collections.unmodifiableList(new ArrayList<>(Arrays.asList("1", "2"))).getClass();
        JavaType type = _typeFactory.constructType(targetCls);

        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        assertNotNull("Deserializer should be found for unmodifiable ArrayList", deser);

        List<?> result = _mapper.readValue("[\"1\", \"2\"]", targetCls);
        assertEquals(2, result.size());
        assertEquals("1", result.get(0));
        assertEquals("2", result.get(1));
    }

    @Test(timeout = 4000)
    public void testFindForCollectionUnmodifiableSet() throws Exception {
        Class<?> targetCls = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("a", "b"))).getClass();
        JavaType type = _typeFactory.constructType(targetCls);

        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        assertNotNull("Deserializer should be found for unmodifiable Set", deser);

        Set<?> result = _mapper.readValue("[\"a\", \"b\"]", targetCls);
        assertEquals(2, result.size());
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
    }

    @Test(timeout = 4000)
    public void testFindForMapSingletonMap() throws Exception {
        Class<?> targetCls = Collections.singletonMap("key", "val").getClass();
        JavaType type = _typeFactory.constructType(targetCls);

        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForMap(null, type);
        assertNotNull("Deserializer should be found for singleton Map", deser);
        assertTrue(deser instanceof StdDelegatingDeserializer);

        Map<?, ?> result = _mapper.readValue("{\"key\": \"val\"}", targetCls);
        assertEquals(1, result.size());
        assertEquals("val", result.get("key"));
        assertEquals(targetCls, result.getClass());
    }

    @Test(timeout = 4000)
    public void testFindForMapUnmodifiableMap() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("k1", "v1");
        Class<?> targetCls = Collections.unmodifiableMap(map).getClass();
        JavaType type = _typeFactory.constructType(targetCls);

        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForMap(null, type);
        assertNotNull("Deserializer should be found for unmodifiable Map", deser);

        Map<?, ?> result = _mapper.readValue("{\"k1\":\"v1\", \"k2\":\"v2\"}", targetCls);
        assertEquals(2, result.size());
        assertEquals("v1", result.get("k1"));
        assertEquals("v2", result.get("k2"));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis & Converter Verification
    // =========================================================================

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testConverterNullInputReturnsNull() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.singletonList("a").getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>)
                JavaUtilCollectionsDeserializers.findForCollection(null, type);
        assertNotNull(deser);
        Converter<Object, Object> conv = (Converter<Object, Object>) deser.getConverter();

        assertNull("Converting null should return null", conv.convert(null));
        assertEquals(type.findSuperType(List.class), conv.getInputType(_typeFactory));
        assertEquals(type.findSuperType(List.class), conv.getOutputType(_typeFactory));
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testConverterSingletonListValidation() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.singletonList("a").getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>)
                JavaUtilCollectionsDeserializers.findForCollection(null, type);
        Converter<Object, Object> conv = (Converter<Object, Object>) deser.getConverter();

        // Valid single element
        Object result = conv.convert(Arrays.asList("testVal"));
        assertEquals(Collections.singletonList("testVal"), result);

        // Boundary: 0 elements
        try {
            conv.convert(Collections.emptyList());
            fail("Expected IllegalArgumentException for 0 entries in singleton list");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not deserialize Singleton container from 0 entries"));
        }

        // Boundary: 2 elements
        try {
            conv.convert(Arrays.asList("e1", "e2"));
            fail("Expected IllegalArgumentException for 2 entries in singleton list");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not deserialize Singleton container from 2 entries"));
        }
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testConverterSingletonSetValidation() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.singleton("a").getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>)
                JavaUtilCollectionsDeserializers.findForCollection(null, type);
        Converter<Object, Object> conv = (Converter<Object, Object>) deser.getConverter();

        // Valid single element
        Object result = conv.convert(new HashSet<String>(Collections.singletonList("testVal")));
        assertEquals(Collections.singleton("testVal"), result);

        // Boundary: 0 elements
        try {
            conv.convert(Collections.emptySet());
            fail("Expected IllegalArgumentException for 0 entries in singleton set");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not deserialize Singleton container from 0 entries"));
        }

        // Boundary: 2 elements
        try {
            conv.convert(new HashSet<String>(Arrays.asList("e1", "e2")));
            fail("Expected IllegalArgumentException for 2 entries in singleton set");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not deserialize Singleton container from 2 entries"));
        }
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testConverterSingletonMapValidation() throws Exception {
        JavaType type = _typeFactory.constructType(Collections.singletonMap("k", "v").getClass());
        StdDelegatingDeserializer<?> deser = (StdDelegatingDeserializer<?>)
                JavaUtilCollectionsDeserializers.findForMap(null, type);
        Converter<Object, Object> conv = (Converter<Object, Object>) deser.getConverter();

        // Valid single element
        Map<String, String> singleMap = new HashMap<String, String>();
        singleMap.put("k1", "v1");
        Object result = conv.convert(singleMap);
        assertEquals(Collections.singletonMap("k1", "v1"), result);

        // Boundary: 0 elements
        try {
            conv.convert(Collections.emptyMap());
            fail("Expected IllegalArgumentException for 0 entries in singleton map");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not deserialize Singleton container from 0 entries"));
        }

        // Boundary: 2 elements
        try {
            Map<String, String> multiMap = new HashMap<String, String>();
            multiMap.put("k1", "v1");
            multiMap.put("k2", "v2");
            conv.convert(multiMap);
            fail("Expected IllegalArgumentException for 2 entries in singleton map");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not deserialize Singleton container from 2 entries"));
        }
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testConverterUnmodifiableImmutability() throws Exception {
        // Unmodifiable Set
        JavaType setType = _typeFactory.constructType(Collections.unmodifiableSet(new HashSet<String>()).getClass());
        StdDelegatingDeserializer<?> setDeser = (StdDelegatingDeserializer<?>)
                JavaUtilCollectionsDeserializers.findForCollection(null, setType);
        Converter<Object, Object> setConv = (Converter<Object, Object>) setDeser.getConverter();
        Set<String> setOut = (Set<String>) setConv.convert(new HashSet<String>(Arrays.asList("a")));
        try {
            setOut.add("b");
            fail("Set should be unmodifiable");
        } catch (UnsupportedOperationException expected) {}

        // Unmodifiable List
        JavaType listType = _typeFactory.constructType(Collections.unmodifiableList(new ArrayList<String>()).getClass());
        StdDelegatingDeserializer<?> listDeser = (StdDelegatingDeserializer<?>)
                JavaUtilCollectionsDeserializers.findForCollection(null, listType);
        Converter<Object, Object> listConv = (Converter<Object, Object>) listDeser.getConverter();
        List<String> listOut = (List<String>) listConv.convert(new ArrayList<String>(Arrays.asList("a")));
        try {
            listOut.add("b");
            fail("List should be unmodifiable");
        } catch (UnsupportedOperationException expected) {}

        // Unmodifiable Map
        JavaType mapType = _typeFactory.constructType(Collections.unmodifiableMap(new HashMap<String, String>()).getClass());
        StdDelegatingDeserializer<?> mapDeser = (StdDelegatingDeserializer<?>)
                JavaUtilCollectionsDeserializers.findForMap(null, mapType);
        Converter<Object, Object> mapConv = (Converter<Object, Object>) mapDeser.getConverter();
        Map<String, String> mapOut = (Map<String, String>) mapConv.convert(new HashMap<String, String>());
        try {
            mapOut.put("k", "v");
            fail("Map should be unmodifiable");
        } catch (UnsupportedOperationException expected) {}
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testConverterDefaultAndAsListPassthrough() throws Exception {
        JavaType type = _typeFactory.constructType(Arrays.asList("a").getClass());
        Converter<Object, Object> conv = (Converter<Object, Object>)
                JavaUtilCollectionsDeserializers.converter(JavaUtilCollectionsDeserializers.TYPE_AS_LIST, type, List.class);
        List<String> in = Arrays.asList("1", "2");
        assertSame("TYPE_AS_LIST should return value directly", in, conv.convert(in));

        Converter<Object, Object> unknownConv = (Converter<Object, Object>)
                JavaUtilCollectionsDeserializers.converter(999, type, List.class);
        assertSame("Default case should return value directly", in, unknownConv.convert(in));
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Defects4J Target Defect:
     * Collections.unmodifiableList(LinkedList) produces a java.util.Collections$UnmodifiableList
     * which does NOT implement RandomAccess.
     * When attempting to deserialize it, findForCollection must recognize this non-RandomAccess
     * unmodifiable list implementation rather than returning null.
     */
    @Test(timeout = 4000)
    public void testUnmodifiableListFromLinkedList() throws Exception {
        List<String> original = Collections.unmodifiableList(new LinkedList<String>(Arrays.asList("foo", "bar")));
        Class<?> nonRandomAccessClass = original.getClass();
        JavaType type = _typeFactory.constructType(nonRandomAccessClass);

        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        assertNotNull("Deserializer MUST be found for unmodifiable List from LinkedList (non-RandomAccess)", deser);

        List<?> result = _mapper.readValue("[\"foo\", \"bar\"]", nonRandomAccessClass);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("foo", result.get(0));
        assertEquals("bar", result.get(1));
        assertEquals(nonRandomAccessClass, result.getClass());
    }

    // =========================================================================
    // PARTITION D: Defensive Guard & Unmatched Types Path
    // =========================================================================

    @Test(timeout = 4000)
    public void testFindForCollectionReturnsNullForRegularCollections() throws Exception {
        assertNull(JavaUtilCollectionsDeserializers.findForCollection(null, _typeFactory.constructType(ArrayList.class)));
        assertNull(JavaUtilCollectionsDeserializers.findForCollection(null, _typeFactory.constructType(LinkedList.class)));
        assertNull(JavaUtilCollectionsDeserializers.findForCollection(null, _typeFactory.constructType(HashSet.class)));
        assertNull(JavaUtilCollectionsDeserializers.findForCollection(null, _typeFactory.constructType(TreeSet.class)));
        assertNull(JavaUtilCollectionsDeserializers.findForCollection(null, _typeFactory.constructType(String.class)));
    }

    @Test(timeout = 4000)
    public void testFindForMapReturnsNullForRegularMaps() throws Exception {
        assertNull(JavaUtilCollectionsDeserializers.findForMap(null, _typeFactory.constructType(HashMap.class)));
        assertNull(JavaUtilCollectionsDeserializers.findForMap(null, _typeFactory.constructType(LinkedHashMap.class)));
        assertNull(JavaUtilCollectionsDeserializers.findForMap(null, _typeFactory.constructType(TreeMap.class)));
        assertNull(JavaUtilCollectionsDeserializers.findForMap(null, _typeFactory.constructType(Integer.class)));
    }

    // =========================================================================
    // PARTITION E: End-to-End Deserializer Exception Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializationSizeMismatchThrowsException() {
        Class<?> targetListCls = Collections.singletonList("a").getClass();
        try {
            _mapper.readValue("[]", targetListCls);
            fail("Deserializing empty array to SingletonList should fail");
        } catch (Exception e) {
            assertTrue(e instanceof JsonMappingException || e.getCause() instanceof IllegalArgumentException);
        }

        try {
            _mapper.readValue("[\"1\", \"2\"]", targetListCls);
            fail("Deserializing two items to SingletonList should fail");
        } catch (Exception e) {
            assertTrue(e instanceof JsonMappingException || e.getCause() instanceof IllegalArgumentException);
        }

        Class<?> targetMapCls = Collections.singletonMap("a", "b").getClass();
        try {
            _mapper.readValue("{}", targetMapCls);
            fail("Deserializing empty object to SingletonMap should fail");
        } catch (Exception e) {
            assertTrue(e instanceof JsonMappingException || e.getCause() instanceof IllegalArgumentException);
        }
    }
}