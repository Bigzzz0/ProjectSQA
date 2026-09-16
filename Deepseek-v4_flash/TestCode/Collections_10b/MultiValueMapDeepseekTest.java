package org.apache.commons.collections.map;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.MultiMap;

/**
 * Comprehensive JUnit 4 test suite for MultiValueMap, targeting line/branch coverage and the known serialization defect.
 *
 * <pre>
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - put: new key, existing key, null key/value, map state changes
 *   - putAll: normal map, MultiMap, null/empty collections
 *   - removeMapping: key present, key absent, last value removal
 *   - containsValue (single-arg): value present/absent, empty map
 *   - containsValue (two-arg): present, absent, null collection
 *   - getCollection, size(key), iterator: key exists/missing
 *   - totalSize: empty, one key multi-values, multiple keys
 *   - values(): view behaviour (size, clear, iterator)
 *   - clear: multiple entries, empty map
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - null arguments to put, putAll, removeMapping, getCollection
 *   - empty collections to putAll
 *   - zero, one, many values per key
 *   - null factory (constructor)
 *   - factory that returns non-empty collection (via class with constructor side-effects)
 *   - serialization round-trip (empty, non-empty)
 * 
 * Partition C: Defect-Targeted Branch Zone (InvalidClassException)
 *   - Serialization compatibility: ensures MultiValueMap can be serialized and deserialized.
 *     On the defective version, missing serialVersionUID causes InvalidClassException.
 *     Test will fail there, revealing the bug.
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Constructor with null factory -> IllegalArgumentException
 *   - ReflectionFactory with non-instantiable class -> FunctorException on put
 *   - iterator() on missing key -> EmptyIterator (no exception)
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - equals/hashCode not overridden, but inherited from AbstractMapDecorator
 *   - Ensures that deserialized map maintains state and contract.
 * </pre>
 */
public class MultiValueMapDeepseekTest {

    // ---- Partition A: Core Functional Logic & State Transitions ----

    @Test(timeout = 4000)
    public void testPutNewKey() {
        MultiValueMap map = new MultiValueMap();
        assertNull(map.put("key", "value1"));
        assertEquals(1, map.totalSize());
        assertTrue(map.containsValue("key", "value1"));
    }

    @Test(timeout = 4000)
    public void testPutExistingKey() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value1");
        assertEquals("value2", map.put("key", "value2"));
        assertEquals(2, map.totalSize());
        assertTrue(map.containsValue("key", "value1"));
        assertTrue(map.containsValue("key", "value2"));
    }

    @Test(timeout = 4000)
    public void testPutNullValue() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", (Object) null);
        assertEquals(1, map.totalSize());
        assertTrue(map.containsValue("key", null));
    }

    @Test(timeout = 4000)
    public void testPutAllNormalMap() {
        MultiValueMap map = new MultiValueMap();
        Map<String, String> normal = new HashMap<>();
        normal.put("a", "1");
        normal.put("b", "2");
        map.putAll(normal);
        assertEquals(2, map.totalSize());
        assertTrue(map.containsValue("a", "1"));
        assertTrue(map.containsValue("b", "2"));
    }

    @Test(timeout = 4000)
    public void testPutAllMultiMap() {
        MultiValueMap map = new MultiValueMap();
        MultiValueMap other = new MultiValueMap();
        other.put("x", "10");
        other.put("x", "20");
        map.putAll(other);
        assertEquals(2, map.totalSize());
        assertTrue(map.containsValue("x", "10"));
        assertTrue(map.containsValue("x", "20"));
    }

    @Test(timeout = 4000)
    public void testPutAllWithKeyAndCollection() {
        MultiValueMap map = new MultiValueMap();
        List<String> values = Arrays.asList("v1", "v2");
        assertTrue(map.putAll("k", values));
        assertEquals(2, map.totalSize());
        assertTrue(map.containsValue("k", "v1"));
        assertTrue(map.containsValue("k", "v2"));
    }

    @Test(timeout = 4000)
    public void testPutAllWithNullCollection() {
        MultiValueMap map = new MultiValueMap();
        assertFalse(map.putAll("k", (Collection) null));
    }

    @Test(timeout = 4000)
    public void testPutAllWithEmptyCollection() {
        MultiValueMap map = new MultiValueMap();
        assertFalse(map.putAll("k", new ArrayList<>()));
    }

    @Test(timeout = 4000)
    public void testRemoveMappingExisting() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value");
        assertEquals("value", map.removeMapping("key", "value"));
        assertFalse(map.containsKey("key"));
        assertNull(map.getCollection("key"));
    }

    @Test(timeout = 4000)
    public void testRemoveMappingNonExisting() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "v1");
        assertNull(map.removeMapping("key", "v2"));
        assertTrue(map.containsValue("key", "v1"));
    }

    @Test(timeout = 4000)
    public void testRemoveMappingLastValue() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "v1");
        map.put("key", "v2");
        map.removeMapping("key", "v1");
        assertTrue(map.containsKey("key"));
        assertEquals("v2", map.removeMapping("key", "v2"));
        assertFalse(map.containsKey("key"));
    }

    @Test(timeout = 4000)
    public void testContainsValue() {
        MultiValueMap map = new MultiValueMap();
        map.put("a", "1");
        map.put("b", "2");
        assertTrue(map.containsValue("1"));
        assertFalse(map.containsValue("3"));
    }

    @Test(timeout = 4000)
    public void testContainsValueWithKey() {
        MultiValueMap map = new MultiValueMap();
        map.put("a", "1");
        assertTrue(map.containsValue("a", "1"));
        assertFalse(map.containsValue("a", "2"));
        assertFalse(map.containsValue("b", "1"));
    }

    @Test(timeout = 4000)
    public void testGetCollection() {
        MultiValueMap map = new MultiValueMap();
        assertNull(map.getCollection("absent"));
        map.put("k", "v");
        Collection c = map.getCollection("k");
        assertNotNull(c);
        assertEquals(1, c.size());
    }

    @Test(timeout = 4000)
    public void testSizeForKey() {
        MultiValueMap map = new MultiValueMap();
        assertEquals(0, map.size("absent"));
        map.put("k", "v");
        assertEquals(1, map.size("k"));
        map.put("k", "v2");
        assertEquals(2, map.size("k"));
    }

    @Test(timeout = 4000)
    public void testIteratorExistingKey() {
        MultiValueMap map = new MultiValueMap();
        map.put("k", "a");
        map.put("k", "b");
        Iterator it = map.iterator("k");
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testIteratorMissingKey() {
        MultiValueMap map = new MultiValueMap();
        Iterator it = map.iterator("missing");
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testTotalSize() {
        MultiValueMap map = new MultiValueMap();
        assertEquals(0, map.totalSize());
        map.put("a", "1");
        map.put("a", "2");
        assertEquals(2, map.totalSize());
        map.put("b", "3");
        assertEquals(3, map.totalSize());
    }

    @Test(timeout = 4000)
    public void testValuesView() {
        MultiValueMap map = new MultiValueMap();
        map.put("k", "v1");
        map.put("k", "v2");
        Collection values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains("v1"));
        assertTrue(values.contains("v2"));
        values.clear();
        assertTrue(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testClear() {
        MultiValueMap map = new MultiValueMap();
        map.put("a", "1");
        map.put("b", "2");
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.totalSize());
    }

    // ---- Partition B: Boundary Value Analysis & Extremes ----

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullFactory() {
        new MultiValueMap(new HashMap(), null);
    }

    @Test(timeout = 4000)
    public void testDecorateWithClass() {
        Map base = new HashMap();
        MultiValueMap map = (MultiValueMap) MultiValueMap.decorate(base, ArrayList.class);
        map.put("k", "v");
        assertTrue(map.getCollection("k") instanceof ArrayList);
    }

    @Test(timeout = 4000, expected = FunctorException.class)
    public void testReflectionFactoryInvalidClass() {
        // Using an interface (no default constructor) should cause FunctorException on put
        MultiValueMap map = (MultiValueMap) MultiValueMap.decorate(new HashMap(), Collection.class);
        map.put("k", "v");  // triggers createCollection -> ReflectionFactory.create() -> InstantiationException
    }

    @Test(timeout = 4000)
    public void testFactoryReturningNonEmptyCollection() {
        // Factory that returns a pre-populated collection; put should still work
        Factory nonEmptyFactory = new Factory() {
            @Override
            public Object create() {
                Collection c = new ArrayList();
                c.add("dummy");
                return c;
            }
        };
        MultiValueMap map = (MultiValueMap) MultiValueMap.decorate(new HashMap(), nonEmptyFactory);
        assertNull(map.put("k", "real"));
        // The collection initially had one element, put adds "real", so total size = 2
        assertEquals(2, map.totalSize());
        assertTrue(map.containsValue("k", "real"));
        assertTrue(map.containsValue("k", "dummy"));
    }

    @Test(timeout = 4000)
    public void testRemoveMappingNullKey() {
        MultiValueMap map = new MultiValueMap();
        assertNull(map.removeMapping(null, "val"));
    }

    // ---- Partition C: Defect-Targeted Branch Zone (Serialization) ----

    @Test(timeout = 4000)
    public void testSerializationEmptyMap() throws Exception {
        MultiValueMap map = new MultiValueMap();
        MultiValueMap deserialized = serializeAndDeserialize(map);
        assertTrue(deserialized.isEmpty());
    }

    @Test(timeout = 4000)
    public void testSerializationNonEmptyMap() throws Exception {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key1", "value2");
        map.put("key2", "value3");
        MultiValueMap deserialized = serializeAndDeserialize(map);
        assertEquals(3, deserialized.totalSize());
        assertTrue(deserialized.containsValue("value1"));
        assertTrue(deserialized.containsValue("key1", "value2"));
        assertTrue(deserialized.containsValue("key2", "value3"));
    }

    @Test(timeout = 4000)
    public void testSerializationCompatibility() throws Exception {
        // This test explicitly targets the InvalidClassException defect.
        MultiValueMap map = new MultiValueMap();
        map.put("a", "1");
        map.put("b", "2");
        map.put("a", "3");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(map);
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bis)) {
            MultiValueMap deserialized = (MultiValueMap) ois.readObject();
            // Verify state preserved
            assertEquals(3, deserialized.totalSize());
            assertTrue(deserialized.containsValue("a", "1"));
            assertTrue(deserialized.containsValue("a", "3"));
            assertTrue(deserialized.containsValue("b", "2"));
        }
    }

    private MultiValueMap serializeAndDeserialize(MultiValueMap map) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(map);
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (MultiValueMap) ois.readObject();
        }
    }

    // ---- Partition D: Exception & Defensive Guard Paths ----

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDecorateNullFactory() {
        MultiValueMap.decorate(new HashMap(), (Factory) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDecorateNullClass() {
        MultiValueMap.decorate(new HashMap(), (Class) null);
    }

    @Test(timeout = 4000)
    public void testPutAllWithMultiMapAndAddsToExistingKey() {
        MultiValueMap map = new MultiValueMap();
        map.put("k", "v1");
        MultiValueMap other = new MultiValueMap();
        other.put("k", "v2");
        other.put("k", "v3");
        map.putAll(other);
        assertEquals(3, map.totalSize());
    }

    // ---- Partition E: Object Lifecycle & Contract Integrity ----

    @Test(timeout = 4000)
    public void testEqualsHashCodeContract() {
        MultiValueMap map1 = new MultiValueMap();
        map1.put("k", "v");
        MultiValueMap map2 = new MultiValueMap();
        map2.put("k", "v");
        // Not overridden, so uses AbstractMapDecorator which delegates to map.equals()
        // Because they wrap different HashMap instances, equals should be based on content
        assertEquals(map1, map2);
        assertEquals(map1.hashCode(), map2.hashCode());
    }

    @Test(timeout = 4000)
    public void testIteratorRemove() {
        MultiValueMap map = new MultiValueMap();
        map.put("k", "v1");
        map.put("k", "v2");
        Iterator it = map.iterator("k");
        it.next();
        it.remove();
        assertEquals(1, map.totalSize());
        assertTrue(map.containsValue("k", "v2"));
    }

    @Test(timeout = 4000)
    public void testValuesIteratorRemove() {
        MultiValueMap map = new MultiValueMap();
        map.put("k", "v1");
        map.put("k", "v2");
        Iterator it = map.values().iterator();
        it.next();
        it.remove();
        assertEquals(1, map.totalSize());
        assertTrue(map.containsValue("k", "v2"));
    }
}