package org.apache.commons.collections.map;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.collections.map.MultiValueMap
 * Defect Reference: Defects4J Collections - InvalidClassException during deserialization
 *
 * Branch & Condition Analysis:
 * 1. Constructor & Decorator variants:
 *    - Default constructor: HashMap + ArrayList reflection factory.
 *    - decorate(Map): default ArrayList reflection factory.
 *    - decorate(Map, Class): custom collection class instantiation.
 *    - decorate(Map, Factory): custom collection factory.
 *    - Guard: collectionFactory == null -> throws IllegalArgumentException.
 * 2. put(K, V) & putAll(K, Collection):
 *    - Collection does not exist for key -> create via factory, add value/collection, store in map if size > 0.
 *    - Collection exists for key -> add/addAll to existing collection.
 *    - Set-backed collection where add/addAll returns false -> put returns null, putAll returns false.
 *    - putAll with null or empty collection -> early exit returning false without modification.
 * 3. putAll(Map):
 *    - Parameter instanceof MultiMap -> unpacks entry collection, delegates to putAll(key, coll).
 *    - Parameter !instanceof MultiMap -> treats as regular map, delegates to put(key, value).
 * 4. removeMapping(K, V):
 *    - Key not found (coll == null) -> returns null.
 *    - Value not in collection -> returns null.
 *    - Value removed, collection still non-empty -> returns value, key preserved.
 *    - Value removed, collection becomes empty -> returns value, key pruned from map.
 * 5. containsValue(V) & containsValue(K, V):
 *    - Key-specific check: key missing vs key present (value present/absent).
 *    - Global check: iterate through all entry sets, match early return true vs full traversal returning false.
 * 6. iterator(K) & values().iterator() (Values & ValuesIterator):
 *    - iterator(K) on missing key -> EmptyIterator.INSTANCE.
 *    - iterator(K) on present key -> ValuesIterator; remove() prunes key if last value removed.
 *    - values() view: size() delegates to totalSize(), clear() clears base map.
 *    - values().iterator() returns IteratorChain over all keys.
 * 7. ReflectionFactory:
 *    - Valid instantiable class (e.g., ArrayList, HashSet).
 *    - Uninstantiable class (e.g., interface/abstract class or private constructor) -> FunctorException.
 * 8. Serialization (Defect Targeted):
 *    - MultiValueMap must implement Serializable and allow round-trip serialization of both empty
 *      and fully populated maps without throwing InvalidClassException or NotSerializableException.
 * -----------------------------------------------------------------------------------------
 */

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.MultiMap;
import org.junit.Test;

public class MultiValueMapGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicPutAndGetOperations() {
        MultiValueMap map = new MultiValueMap();
        assertNull(map.getCollection("key1"));
        assertEquals(0, map.size("key1"));
        assertEquals(0, map.totalSize());

        Object result1 = map.put("key1", "val1");
        assertEquals("val1", result1);
        assertEquals(1, map.size("key1"));
        assertEquals(1, map.totalSize());

        Object result2 = map.put("key1", "val2");
        assertEquals("val2", result2);
        assertEquals(2, map.size("key1"));
        assertEquals(2, map.totalSize());

        Collection coll = map.getCollection("key1");
        assertNotNull(coll);
        assertEquals(2, coll.size());
        assertTrue(coll.contains("val1"));
        assertTrue(coll.contains("val2"));
    }

    @Test(timeout = 4000)
    public void testRemoveMappingPartialAndExhaustive() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "valA");
        map.put("key1", "valB");
        map.put("key2", "valC");

        // Key not present
        assertNull(map.removeMapping("missingKey", "valA"));

        // Value not present under existing key
        assertNull(map.removeMapping("key1", "valNonExistent"));
        assertEquals(2, map.size("key1"));

        // Remove one of two values
        Object removed1 = map.removeMapping("key1", "valA");
        assertEquals("valA", removed1);
        assertEquals(1, map.size("key1"));
        assertTrue(map.containsKey("key1"));

        // Remove final remaining value under key1 -> key must be removed
        Object removed2 = map.removeMapping("key1", "valB");
        assertEquals("valB", removed2);
        assertFalse(map.containsKey("key1"));
        assertNull(map.getCollection("key1"));
        assertEquals(0, map.size("key1"));
        assertEquals(1, map.totalSize());
    }

    @Test(timeout = 4000)
    public void testPutAllCollectionNormalAndExistingKey() {
        MultiValueMap map = new MultiValueMap();
        List<String> values = Arrays.asList("A", "B", "C");

        boolean changed1 = map.putAll("k1", values);
        assertTrue(changed1);
        assertEquals(3, map.size("k1"));

        List<String> moreValues = Arrays.asList("D", "E");
        boolean changed2 = map.putAll("k1", moreValues);
        assertTrue(changed2);
        assertEquals(5, map.size("k1"));
        assertEquals(5, map.totalSize());
    }

    @Test(timeout = 4000)
    public void testPutAllMapStandardAndMultiMap() {
        // 1. Regular Map putAll
        MultiValueMap target = new MultiValueMap();
        Map<String, String> standardMap = new HashMap<String, String>();
        standardMap.put("k1", "v1");
        standardMap.put("k2", "v2");

        target.putAll(standardMap);
        assertEquals(1, target.size("k1"));
        assertEquals(1, target.size("k2"));
        assertEquals(2, target.totalSize());

        // 2. MultiMap putAll
        MultiValueMap sourceMulti = new MultiValueMap();
        sourceMulti.put("k1", "v1_extra");
        sourceMulti.put("k3", "v3");

        target.putAll(sourceMulti);
        assertEquals(2, target.size("k1")); // v1 and v1_extra
        assertEquals(1, target.size("k2"));
        assertEquals(1, target.size("k3"));
        assertEquals(4, target.totalSize());
    }

    @Test(timeout = 4000)
    public void testContainsValueGlobalAndPerKey() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");
        map.put("k2", "v2");

        // Global containsValue
        assertTrue(map.containsValue("v1"));
        assertTrue(map.containsValue("v2"));
        assertFalse(map.containsValue("v3"));

        // Per-key containsValue
        assertTrue(map.containsValue("k1", "v1"));
        assertFalse(map.containsValue("k1", "v2"));
        assertFalse(map.containsValue("missingKey", "v1"));
    }

    @Test(timeout = 4000)
    public void testValuesViewCollectionAndIterator() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");

        Collection values = map.values();
        assertNotNull(values);
        assertSame(values, map.values()); // Cached view
        assertEquals(3, values.size());

        List<Object> collected = new ArrayList<Object>();
        for (Object val : values) {
            collected.add(val);
        }
        assertEquals(3, collected.size());
        assertTrue(collected.contains("v1"));
        assertTrue(collected.contains("v2"));
        assertTrue(collected.contains("v3"));

        values.clear();
        assertEquals(0, map.totalSize());
        assertTrue(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testKeyIteratorAndRemoval() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");
        map.put("k1", "v2");

        // Missing key returns empty iterator
        Iterator emptyIt = map.iterator("missing");
        assertFalse(emptyIt.hasNext());

        // Existing key iterator
        Iterator it = map.iterator("k1");
        assertTrue(it.hasNext());
        Object valFirst = it.next();
        it.remove(); // removes first value
        assertEquals(1, map.size("k1"));
        assertTrue(map.containsKey("k1"));

        assertTrue(it.hasNext());
        Object valSecond = it.next();
        it.remove(); // removes second value; collection empty -> key pruned
        assertFalse(map.containsKey("k1"));
        assertEquals(0, map.size("k1"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullKeysAndNullValues() {
        MultiValueMap map = new MultiValueMap();
        // Null key and null value
        map.put(null, null);
        assertEquals(1, map.size(null));
        assertTrue(map.containsKey(null));
        assertTrue(map.containsValue(null));
        assertTrue(map.containsValue(null, null));

        // Add non-null value to null key
        map.put(null, "nonNull");
        assertEquals(2, map.size(null));

        // Remove mapping with null key/value
        Object removed = map.removeMapping(null, null);
        assertNull(removed);
        assertEquals(1, map.size(null));
        assertTrue(map.containsValue(null, "nonNull"));
    }

    @Test(timeout = 4000)
    public void testPutAllWithNullAndEmptyCollection() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");

        // Null collection
        boolean changedNull = map.putAll("k1", null);
        assertFalse(changedNull);
        assertEquals(1, map.size("k1"));

        // Empty collection
        boolean changedEmpty = map.putAll("k1", Collections.emptyList());
        assertFalse(changedEmpty);
        assertEquals(1, map.size("k1"));

        // Empty collection on non-existent key
        boolean changedNewEmpty = map.putAll("k2", Collections.emptyList());
        assertFalse(changedNewEmpty);
        assertFalse(map.containsKey("k2"));
    }

    @Test(timeout = 4000)
    public void testEmptyMapBoundaries() {
        MultiValueMap map = new MultiValueMap();
        assertEquals(0, map.totalSize());
        assertFalse(map.containsValue("any"));
        assertFalse(map.containsValue("anyKey", "anyVal"));
        assertNull(map.removeMapping("anyKey", "anyVal"));
        assertEquals(0, map.size("anyKey"));
        assertFalse(map.iterator("anyKey").hasNext());

        Collection values = map.values();
        assertEquals(0, values.size());
        assertFalse(values.iterator().hasNext());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Serialization Integrity)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyMapSerializationDefectTarget() throws Exception {
        MultiValueMap emptyMap = new MultiValueMap();
        assertTrue("MultiValueMap must be Serializable", emptyMap instanceof Serializable);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(emptyMap);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof MultiValueMap);
        MultiValueMap resultMap = (MultiValueMap) deserialized;
        assertEquals(0, resultMap.totalSize());
        assertTrue(resultMap.isEmpty());
    }

    @Test(timeout = 4000)
    public void testFullMapSerializationDefectTarget() throws Exception {
        MultiValueMap populatedMap = MultiValueMap.decorate(new HashMap<Object, Object>(), ArrayList.class);
        populatedMap.put("Key1", "Val1");
        populatedMap.put("Key1", "Val2");
        populatedMap.put("Key2", "Val3");

        assertTrue("MultiValueMap must be Serializable", populatedMap instanceof Serializable);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(populatedMap);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof MultiValueMap);
        MultiValueMap resultMap = (MultiValueMap) deserialized;
        assertEquals(3, resultMap.totalSize());
        assertEquals(2, resultMap.size("Key1"));
        assertEquals(1, resultMap.size("Key2"));
        assertTrue(resultMap.containsValue("Key1", "Val1"));
        assertTrue(resultMap.containsValue("Key1", "Val2"));
        assertTrue(resultMap.containsValue("Key2", "Val3"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorNullFactoryThrowsException() {
        try {
            new MultiValueMap(new HashMap<Object, Object>(), null);
            fail("Expected IllegalArgumentException for null collectionFactory");
        } catch (IllegalArgumentException ex) {
            assertEquals("The factory must not be null", ex.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testUninstantiableCollectionClassThrowsFunctorException() {
        // AbstractCollection cannot be instantiated by reflection
        MultiValueMap map = MultiValueMap.decorate(new HashMap<Object, Object>(), AbstractCollection.class);
        try {
            map.put("key", "val");
            fail("Expected FunctorException when creating collection from abstract class");
        } catch (FunctorException expected) {
            assertNotNull(expected.getCause());
        }
    }

    // =========================================================================
    // Partition E: Factory Variants, Custom Collection Types & Iterator Dynamics
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetBackedCollectionBehavior() {
        // Decorate using HashSet: duplicates should be rejected
        MultiValueMap setMap = MultiValueMap.decorate(new HashMap<Object, Object>(), HashSet.class);

        Object added1 = setMap.put("key", "item1");
        assertEquals("item1", added1);
        assertEquals(1, setMap.size("key"));

        // Set rejection of duplicate value
        Object addedDuplicate = setMap.put("key", "item1");
        assertNull("Adding duplicate to set backed multi-map should return null", addedDuplicate);
        assertEquals(1, setMap.size("key"));

        // putAll with duplicates
        boolean putAllDuplicates = setMap.putAll("key", Arrays.asList("item1"));
        assertFalse(putAllDuplicates);

        boolean putAllNew = setMap.putAll("key", Arrays.asList("item1", "item2"));
        assertTrue(putAllNew);
        assertEquals(2, setMap.size("key"));
    }

    @Test(timeout = 4000)
    public void testCustomFactoryDecoration() {
        Factory treeSetFactory = new Factory() {
            public Object create() {
                return new TreeSet<Object>();
            }
        };

        MultiValueMap customMap = MultiValueMap.decorate(new HashMap<Object, Object>(), treeSetFactory);
        customMap.put("nums", 30);
        customMap.put("nums", 10);
        customMap.put("nums", 20);

        Collection coll = customMap.getCollection("nums");
        assertTrue(coll instanceof TreeSet);
        Iterator it = coll.iterator();
        assertEquals(10, it.next());
        assertEquals(20, it.next());
        assertEquals(30, it.next());
    }

    @Test(timeout = 4000)
    public void testClearClearsUnderlyingDecoratedMap() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");
        map.put("k2", "v2");
        assertEquals(2, map.size());
        assertEquals(2, map.totalSize());

        map.clear();
        assertEquals(0, map.size());
        assertEquals(0, map.totalSize());
        assertNull(map.getCollection("k1"));
        assertNull(map.getCollection("k2"));
    }

    @Test(timeout = 4000)
    public void testValuesIteratorRemoveAllPrunesKeys() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");
        map.put("k2", "v2");

        Collection values = map.values();
        Iterator it = values.iterator();

        while (it.hasNext()) {
            it.next();
            it.remove();
        }

        assertEquals(0, map.totalSize());
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }
}