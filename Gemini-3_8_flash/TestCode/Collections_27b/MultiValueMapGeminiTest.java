package org.apache.commons.collections4.map;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.FunctorException;
import org.apache.commons.collections4.MultiMap;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: org.apache.commons.collections4.map.MultiValueMap
 *
 * Decision / Branch Points Targeted:
 * 1. multiValueMap factory methods: wrapping with default ArrayList, Class<C>, or custom Factory<C>.
 * 2. Constructor guard: collectionFactory == null throwing IllegalArgumentException.
 * 3. removeMapping(key, value):
 *    - key not found -> false
 *    - key found, value not found -> false
 *    - key found, value removed, collection not empty -> true (key preserved)
 *    - key found, value removed, collection becomes empty -> true (key removed from map)
 * 4. containsValue(value):
 *    - empty map -> false
 *    - value present in one of the collections -> true
 *    - value not present in any collection -> false
 * 5. containsValue(key, value):
 *    - key not found -> false
 *    - key found, value in collection -> true
 *    - key found, value not in collection -> false
 * 6. put(key, value):
 *    - collection not existing for key -> createCollection(1), add, put in map, return value
 *    - collection existing, element added successfully -> return value
 *    - collection existing (e.g. Set), duplicate element rejected -> return null
 * 7. putAll(Map):
 *    - argument instanceof MultiMap -> unpacks entries and calls putAll(key, coll)
 *    - argument not instanceof MultiMap -> calls put(key, value) for each entry
 * 8. putAll(key, values):
 *    - values == null -> false
 *    - values.isEmpty() -> false
 *    - key not in map -> creates collection, addAll, puts in map, returns true
 *    - key in map -> coll.addAll(values), returns result
 * 9. size(key):
 *    - key not in map -> 0
 *    - key in map -> coll.size()
 * 10. totalSize():
 *     - empty map -> 0
 *     - multiple keys and varying collection sizes -> accurate sum
 * 11. iterator(key):
 *     - key not in map -> EmptyIterator
 *     - key in map -> ValuesIterator; remove() updates or deletes key when empty
 * 12. iterator():
 *     - all-entries lazy iterator over every (key, value) pair
 *     - Entry.setValue() -> UnsupportedOperationException
 * 13. values():
 *     - cached valuesView collection
 *     - iterator(), size(), clear()
 * 14. ReflectionFactory:
 *     - successful instantiation of specified collection class
 *     - instantiation failure (e.g. abstract class / interface) -> FunctorException
 * 15. Defects4J Known Defect:
 *     - testUnsafeDeSerialization: deserialization of an unsafe class (non-Collection)
 *       in ReflectionFactory should be rejected.
 */
public class MultiValueMapGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPutAndGetSingleKeyMultipleValues() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        assertNull(map.getCollection("key1"));
        assertEquals(0, map.size("key1"));

        final Object putResult1 = map.put("key1", "val1");
        assertEquals("val1", putResult1);
        assertEquals(1, map.size("key1"));
        assertEquals(1, map.totalSize());

        final Object putResult2 = map.put("key1", "val2");
        assertEquals("val2", putResult2);
        assertEquals(2, map.size("key1"));
        assertEquals(2, map.totalSize());

        final Collection<String> coll = map.getCollection("key1");
        assertNotNull(coll);
        assertEquals(2, coll.size());
        assertTrue(coll.contains("val1"));
        assertTrue(coll.contains("val2"));
    }

    @Test(timeout = 4000)
    public void testRemoveMappingTransitions() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "val1");
        map.put("key1", "val2");

        // Key not present
        assertFalse(map.removeMapping("nonExistingKey", "val1"));

        // Key present, value not present
        assertFalse(map.removeMapping("key1", "nonExistingVal"));

        // Value removed, collection still has items -> key retained
        assertTrue(map.removeMapping("key1", "val1"));
        assertEquals(1, map.size("key1"));
        assertTrue(map.containsKey("key1"));

        // Last value removed -> collection empty -> key deleted
        assertTrue(map.removeMapping("key1", "val2"));
        assertEquals(0, map.size("key1"));
        assertFalse(map.containsKey("key1"));
        assertNull(map.getCollection("key1"));
    }

    @Test(timeout = 4000)
    public void testContainsValueVariants() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        assertFalse(map.containsValue("val1"));
        assertFalse(map.containsValue("key1", "val1"));

        map.put("key1", "val1");
        map.put("key2", "val2");

        assertTrue(map.containsValue("val1"));
        assertTrue(map.containsValue("val2"));
        assertFalse(map.containsValue("val3"));

        assertTrue(map.containsValue("key1", "val1"));
        assertFalse(map.containsValue("key1", "val2"));
        assertFalse(map.containsValue("key3", "val1"));
    }

    @Test(timeout = 4000)
    public void testPutAllWithCollection() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();

        // Null and empty values collection
        assertFalse(map.putAll("k1", null));
        assertFalse(map.putAll("k1", Collections.<String>emptyList()));
        assertFalse(map.containsKey("k1"));

        // Put into new key
        final boolean addedFirst = map.putAll("k1", Arrays.asList("a", "b", "c"));
        assertTrue(addedFirst);
        assertEquals(3, map.size("k1"));

        // Put into existing key
        final boolean addedMore = map.putAll("k1", Arrays.asList("d", "e"));
        assertTrue(addedMore);
        assertEquals(5, map.size("k1"));
        assertEquals(5, map.totalSize());
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testPutAllWithStandardMapAndMultiMap() {
        final MultiValueMap<String, String> target = new MultiValueMap<String, String>();

        // 1. Put standard Map
        final Map<String, String> standardMap = new HashMap<String, String>();
        standardMap.put("k1", "v1");
        standardMap.put("k2", "v2");
        target.putAll(standardMap);

        assertEquals(1, target.size("k1"));
        assertEquals(1, target.size("k2"));

        // 2. Put MultiMap
        final MultiValueMap<String, String> sourceMultiMap = new MultiValueMap<String, String>();
        sourceMultiMap.put("k1", "v1_extra");
        sourceMultiMap.put("k3", "v3");

        target.putAll(sourceMultiMap);

        assertEquals(2, target.size("k1"));
        assertEquals(1, target.size("k2"));
        assertEquals(1, target.size("k3"));
        assertEquals(4, target.totalSize());
    }

    @Test(timeout = 4000)
    public void testClearMethod() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        assertEquals(2, map.totalSize());

        map.clear();
        assertEquals(0, map.totalSize());
        assertTrue(map.isEmpty());
        assertNull(map.getCollection("k1"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullKeysAndNullValues() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();

        // Allow null key and null value
        map.put(null, null);
        assertTrue(map.containsKey(null));
        assertTrue(map.containsValue(null));
        assertTrue(map.containsValue(null, null));
        assertEquals(1, map.size(null));
        assertEquals(1, map.totalSize());

        map.put(null, "nonNullVal");
        assertEquals(2, map.size(null));

        assertTrue(map.removeMapping(null, null));
        assertEquals(1, map.size(null));
        assertFalse(map.containsValue(null, null));

        assertTrue(map.removeMapping(null, "nonNullVal"));
        assertFalse(map.containsKey(null));
        assertEquals(0, map.totalSize());
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testPutWithSetCollectionRejectingDuplicates() {
        // MultiValueMap configured with a Set collection
        final MultiValueMap<String, String> map = MultiValueMap.multiValueMap(
                new HashMap<String, Collection<String>>(),
                (Class<HashSet<String>>) (Class<?>) HashSet.class
        );

        final Object firstPut = map.put("setKey", "dup");
        assertEquals("dup", firstPut);
        assertEquals(1, map.size("setKey"));

        // Adding duplicate value to Set returns false internally, put() returns null
        final Object secondPut = map.put("setKey", "dup");
        assertNull(secondPut);
        assertEquals(1, map.size("setKey"));
    }

    @Test(timeout = 4000)
    public void testValuesCollectionContractAndCaching() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");

        final Collection<Object> values1 = map.values();
        final Collection<Object> values2 = map.values();
        assertSame("valuesView should be cached", values1, values2);

        assertEquals(3, values1.size());
        assertTrue(values1.contains("v1"));
        assertTrue(values1.contains("v2"));
        assertTrue(values1.contains("v3"));

        // Iterate through values
        int count = 0;
        for (final Object val : values1) {
            assertNotNull(val);
            count++;
        }
        assertEquals(3, count);

        // Values.clear clears the map
        values1.clear();
        assertEquals(0, map.totalSize());
        assertTrue(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testKeyIteratorAndValuesIteratorRemoval() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();

        // Non-existent key yields EmptyIterator
        final Iterator<String> emptyIt = map.iterator("nonExistent");
        assertNotNull(emptyIt);
        assertFalse(emptyIt.hasNext());

        map.put("k1", "v1");
        map.put("k1", "v2");

        final Iterator<String> it = map.iterator("k1");
        assertTrue(it.hasNext());
        assertEquals("v1", it.next());
        it.remove(); // removes "v1", "v2" remains
        assertEquals(1, map.size("k1"));
        assertTrue(map.containsKey("k1"));

        assertTrue(it.hasNext());
        assertEquals("v2", it.next());
        it.remove(); // removes "v2", collection becomes empty -> key removed
        assertEquals(0, map.size("k1"));
        assertFalse(map.containsKey("k1"));
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testMapEntryIteratorFlattened() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();

        // Empty map iterator
        final Iterator<Map.Entry<String, String>> emptyIt = map.iterator();
        assertNotNull(emptyIt);
        assertFalse(emptyIt.hasNext());

        map.put("A", "1");
        map.put("A", "2");
        map.put("B", "3");

        final Iterator<Map.Entry<String, String>> it = map.iterator();
        int entriesCount = 0;
        while (it.hasNext()) {
            final Map.Entry<String, String> entry = it.next();
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
            entriesCount++;

            // Entry.setValue is unsupported
            try {
                entry.setValue("forbidden");
                fail("Entry.setValue should throw UnsupportedOperationException");
            } catch (final UnsupportedOperationException expected) {
                // Expected
            }
        }
        assertEquals(3, entriesCount);
    }

    @Test(timeout = 4000)
    public void testEntrySetDelegation() {
        final MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("A", "1");
        map.put("A", "2");

        final Set<Map.Entry<String, Object>> entries = map.entrySet();
        assertEquals(1, entries.size());
        final Map.Entry<String, Object> entry = entries.iterator().next();
        assertEquals("A", entry.getKey());
        assertTrue(entry.getValue() instanceof Collection);
        assertEquals(2, ((Collection<?>) entry.getValue()).size());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets known defect: testUnsafeDeSerialization.
     * When MultiValueMap is deserialized with an unsafe (non-Collection) class
     * for its collectionFactory, it must reject deserialization (e.g. by throwing
     * UnsupportedOperationException). In the defective version, unsafe classes are
     * accepted without validation.
     */
    @Test(timeout = 4000)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testUnsafeDeSerialization() throws Exception {
        final MultiValueMap<String, String> map =
                MultiValueMap.multiValueMap(new HashMap<String, Collection<String>>(), (Class) String.class);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        try {
            ois.readObject();
            fail("unsafe clazz accepted when de-serializing MultiValueMap");
        } catch (final UnsupportedOperationException expected) {
            // Expected in safe/patched version
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullFactoryGuard() {
        MultiValueMap.multiValueMap(new HashMap<String, Collection<String>>(), (Factory<Collection<String>>) null);
    }

    @Test(timeout = 4000)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testReflectionFactoryInstantiationFailure() {
        // Interface cannot be instantiated via newInstance() -> FunctorException
        final MultiValueMap<String, String> map =
                MultiValueMap.multiValueMap(new HashMap<String, Collection<String>>(), (Class) Collection.class);
        try {
            map.put("key", "value");
            fail("Expected FunctorException when creating collection with interface class");
        } catch (final FunctorException expected) {
            assertTrue(expected.getMessage().contains("Cannot instantiate class"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactoryCreationWithCustomFactory() {
        final Factory<LinkedList<String>> linkedListFactory = new Factory<LinkedList<String>>() {
            @Override
            public LinkedList<String> create() {
                return new LinkedList<String>();
            }
        };

        final MultiValueMap<String, String> map = MultiValueMap.multiValueMap(
                new HashMap<String, Collection<String>>(),
                linkedListFactory
        );

        map.put("K", "V");
        final Collection<String> coll = map.getCollection("K");
        assertNotNull(coll);
        assertTrue("Collection must be created by custom LinkedList factory", coll instanceof LinkedList);
    }

    @Test(timeout = 4000)
    public void testFactoryCreationWithArrayListClass() {
        final MultiValueMap<String, String> map = MultiValueMap.multiValueMap(
                new HashMap<String, ArrayList<String>>(),
                ArrayList.class
        );

        map.put("K", "V");
        final Collection<String> coll = map.getCollection("K");
        assertNotNull(coll);
        assertTrue(coll instanceof ArrayList);
    }

    @Test(timeout = 4000)
    public void testFactoryCreationWithBaseMap() {
        final Map<String, Object> baseMap = new HashMap<String, Object>();
        final MultiValueMap<String, String> map = MultiValueMap.multiValueMap(baseMap);

        map.put("K", "V");
        assertEquals(1, map.totalSize());
        assertEquals("V", map.getCollection("K").iterator().next());
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testSafeSerializationRoundTrip() throws Exception {
        final MultiValueMap<String, String> original = new MultiValueMap<String, String>();
        original.put("k1", "v1");
        original.put("k1", "v2");
        original.put("k2", "v3");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final MultiValueMap<String, String> deserialized = (MultiValueMap<String, String>) ois.readObject();

        assertNotNull(deserialized);
        assertEquals(original.totalSize(), deserialized.totalSize());
        assertEquals(2, deserialized.size("k1"));
        assertEquals(1, deserialized.size("k2"));
        assertTrue(deserialized.containsValue("k1", "v1"));
        assertTrue(deserialized.containsValue("k1", "v2"));
        assertTrue(deserialized.containsValue("k2", "v3"));
    }
}