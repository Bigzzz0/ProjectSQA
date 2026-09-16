package org.apache.commons.collections.map;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: MultiValueMap
 * 
 * Key Decision Branches:
 * 1. put() method:
 *    - Branch 1: coll == null (no existing collection for key)
 *      - Sub-branch 1a: coll.size() > 0 after add -> put to map, return null
 *      - Sub-branch 1b: coll.size() == 0 after add -> don't put, return null
 *    - Branch 2: coll != null (existing collection) -> add to collection, return value if changed
 * 
 * 2. putAll(Object key, Collection values) method:
 *    - Branch 1: values == null || values.size() == 0 -> return false
 *    - Branch 2: coll == null (no existing collection)
 *      - Sub-branch 2a: coll.size() > 0 after addAll -> put to map, return false
 *      - Sub-branch 2b: coll.size() == 0 after addAll -> don't put, return false
 *    - Branch 3: coll != null -> addAll to existing collection, return result
 * 
 * 3. removeMapping() method:
 *    - Branch 1: valuesForKey == null -> return null
 *    - Branch 2: removed == false -> return null
 *    - Branch 3: valuesForKey.isEmpty() after remove -> remove key from map
 * 
 * 4. containsValue(Object value) method:
 *    - Branch 1: pairs == null -> return false
 *    - Branch 2: coll.contains(value) -> return true
 * 
 * 5. containsValue(Object key, Object value) method:
 *    - Branch 1: coll == null -> return false
 *    - Branch 2: coll.contains(value) -> return true/false
 * 
 * 6. size(Object key) method:
 *    - Branch 1: coll == null -> return 0
 *    - Branch 2: coll != null -> return coll.size()
 * 
 * 7. putAll(Map map) method:
 *    - Branch 1: map instanceof MultiMap -> use putAll(key, collection)
 *    - Branch 2: else -> use put(key, value)
 * 
 * 8. iterator(Object key) method:
 *    - Branch 1: !containsKey(key) -> return EmptyIterator
 *    - Branch 2: else -> return ValuesIterator
 * 
 * Known Defect: put() method has a bug where when a new collection is created and
 * a value is added, if the collection size > 0, it puts the collection in the map
 * but sets result = false, returning null instead of the value. This causes
 * testPutWithList and testPutWithSet to fail because they expect the value to be returned.
 * 
 * Also, putAll(Object key, Collection values) has a similar bug where when a new
 * collection is created and values are added, if the collection size > 0, it puts
 * the collection but sets result = false, returning false instead of true.
 */
public class MultiValueMapDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====
    
    @Test(timeout = 4000)
    public void testPutNewKey() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        Object value = "value1";
        
        // When putting a value for a new key, the bug causes null to be returned
        // instead of the value. This test reveals the defect.
        Object result = map.put(key, value);
        
        // The expected correct behavior is that the value should be returned
        // because the map changed (a new mapping was added)
        assertEquals("put should return the value when map changes", value, result);
        
        // Verify the value was actually stored
        assertTrue("Map should contain the key", map.containsKey(key));
        Collection coll = map.getCollection(key);
        assertNotNull("Collection should exist for key", coll);
        assertTrue("Collection should contain the value", coll.contains(value));
    }
    
    @Test(timeout = 4000)
    public void testPutExistingKey() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        Object value1 = "value1";
        Object value2 = "value2";
        
        map.put(key, value1);
        Object result = map.put(key, value2);
        
        // When adding to an existing collection, the value should be returned
        assertEquals("put should return the value when adding to existing collection", value2, result);
        
        Collection coll = map.getCollection(key);
        assertEquals("Collection should have 2 values", 2, coll.size());
        assertTrue("Collection should contain value1", coll.contains(value1));
        assertTrue("Collection should contain value2", coll.contains(value2));
    }
    
    @Test(timeout = 4000)
    public void testPutDuplicateValue() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        Object value = "value1";
        
        map.put(key, value);
        Object result = map.put(key, value);
        
        // For ArrayList-based collections, duplicates are allowed
        assertEquals("put should return the value even for duplicates", value, result);
        
        Collection coll = map.getCollection(key);
        assertEquals("Collection should have 2 identical values", 2, coll.size());
    }
    
    @Test(timeout = 4000)
    public void testGetCollection() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        Object value = "value1";
        
        assertNull("getCollection should return null for non-existent key", map.getCollection(key));
        
        map.put(key, value);
        Collection coll = map.getCollection(key);
        assertNotNull("getCollection should return collection for existing key", coll);
        assertTrue("Collection should contain the value", coll.contains(value));
    }
    
    @Test(timeout = 4000)
    public void testSizeForKey() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        
        assertEquals("size should be 0 for non-existent key", 0, map.size(key));
        
        map.put(key, "value1");
        assertEquals("size should be 1 after one put", 1, map.size(key));
        
        map.put(key, "value2");
        assertEquals("size should be 2 after two puts", 2, map.size(key));
    }
    
    @Test(timeout = 4000)
    public void testTotalSize() {
        MultiValueMap map = new MultiValueMap();
        
        assertEquals("totalSize should be 0 for empty map", 0, map.totalSize());
        
        map.put("key1", "value1");
        assertEquals("totalSize should be 1", 1, map.totalSize());
        
        map.put("key1", "value2");
        assertEquals("totalSize should be 2", 2, map.totalSize());
        
        map.put("key2", "value3");
        assertEquals("totalSize should be 3", 3, map.totalSize());
    }
    
    @Test(timeout = 4000)
    public void testClear() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key2", "value2");
        
        assertEquals("Map should have 2 keys before clear", 2, map.size());
        
        map.clear();
        assertTrue("Map should be empty after clear", map.isEmpty());
        assertEquals("totalSize should be 0 after clear", 0, map.totalSize());
    }
    
    @Test(timeout = 4000)
    public void testContainsValue() {
        MultiValueMap map = new MultiValueMap();
        
        assertFalse("containsValue should return false for empty map", map.containsValue("value1"));
        
        map.put("key1", "value1");
        assertTrue("containsValue should return true for existing value", map.containsValue("value1"));
        assertFalse("containsValue should return false for non-existing value", map.containsValue("value2"));
    }
    
    @Test(timeout = 4000)
    public void testContainsValueForKey() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        
        assertFalse("containsValue(key, value) should return false for non-existent key", 
                     map.containsValue(key, "value1"));
        
        map.put(key, "value1");
        assertTrue("containsValue(key, value) should return true for existing key-value", 
                    map.containsValue(key, "value1"));
        assertFalse("containsValue(key, value) should return false for non-existing value", 
                     map.containsValue(key, "value2"));
    }
    
    // ===== Partition B: Boundary Value Analysis (BVA) & Extremes =====
    
    @Test(timeout = 4000)
    public void testPutNullKey() {
        MultiValueMap map = new MultiValueMap();
        
        // MultiValueMap should handle null keys (delegates to underlying HashMap)
        Object result = map.put(null, "value1");
        // The bug also affects null keys
        assertEquals("put should return the value for null key", "value1", result);
        
        assertTrue("Map should contain null key", map.containsKey(null));
        Collection coll = map.getCollection(null);
        assertNotNull("Collection should exist for null key", coll);
        assertTrue("Collection should contain the value", coll.contains("value1"));
    }
    
    @Test(timeout = 4000)
    public void testPutNullValue() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        
        Object result = map.put(key, null);
        // The bug also affects null values
        assertEquals("put should return null value", null, result);
        
        Collection coll = map.getCollection(key);
        assertNotNull("Collection should exist", coll);
        assertTrue("Collection should contain null", coll.contains(null));
    }
    
    @Test(timeout = 4000)
    public void testPutAllWithNullCollection() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        
        boolean result = map.putAll(key, null);
        assertFalse("putAll with null collection should return false", result);
        assertFalse("Map should not contain the key", map.containsKey(key));
    }
    
    @Test(timeout = 4000)
    public void testPutAllWithEmptyCollection() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        
        boolean result = map.putAll(key, new ArrayList());
        assertFalse("putAll with empty collection should return false", result);
        assertFalse("Map should not contain the key", map.containsKey(key));
    }
    
    @Test(timeout = 4000)
    public void testPutAllNewKey() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        Collection values = new ArrayList();
        values.add("value1");
        values.add("value2");
        
        // This test reveals the bug in putAll: when a new collection is created
        // and values are added, if the collection size > 0, it puts the collection
        // but returns false instead of true
        boolean result = map.putAll(key, values);
        
        // The expected correct behavior is that true should be returned
        // because the map changed
        assertTrue("putAll should return true when map changes", result);
        
        Collection coll = map.getCollection(key);
        assertNotNull("Collection should exist", coll);
        assertEquals("Collection should have 2 values", 2, coll.size());
    }
    
    @Test(timeout = 4000)
    public void testPutAllExistingKey() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        map.put(key, "value1");
        
        Collection values = new ArrayList();
        values.add("value2");
        values.add("value3");
        
        boolean result = map.putAll(key, values);
        assertTrue("putAll should return true when adding to existing collection", result);
        
        Collection coll = map.getCollection(key);
        assertEquals("Collection should have 3 values", 3, coll.size());
    }
    
    @Test(timeout = 4000)
    public void testRemoveMapping() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        Object value = "value1";
        
        // Remove from non-existent key
        Object result = map.removeMapping(key, value);
        assertNull("removeMapping should return null for non-existent key", result);
        
        map.put(key, value);
        map.put(key, "value2");
        
        // Remove existing value
        result = map.removeMapping(key, value);
        assertEquals("removeMapping should return the removed value", value, result);
        
        Collection coll = map.getCollection(key);
        assertEquals("Collection should have 1 value remaining", 1, coll.size());
        assertFalse("Collection should not contain removed value", coll.contains(value));
    }
    
    @Test(timeout = 4000)
    public void testRemoveMappingLastValue() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        Object value = "value1";
        
        map.put(key, value);
        
        // Remove the only value
        Object result = map.removeMapping(key, value);
        assertEquals("removeMapping should return the removed value", value, result);
        
        // Key should be removed from map
        assertFalse("Key should be removed from map", map.containsKey(key));
        assertNull("getCollection should return null for removed key", map.getCollection(key));
    }
    
    @Test(timeout = 4000)
    public void testRemoveMappingNonExistentValue() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        map.put(key, "value1");
        
        Object result = map.removeMapping(key, "value2");
        assertNull("removeMapping should return null for non-existent value", result);
        
        assertTrue("Key should still exist", map.containsKey(key));
        assertEquals("Collection should still have 1 value", 1, map.size(key));
    }
    
    // ===== Partition C: Defect-Targeted Branch Zone =====
    
    @Test(timeout = 4000)
    public void testPutWithList() {
        // This test directly targets the known defect from Defects4J
        MultiValueMap map = new MultiValueMap();
        String key = "testKey";
        String value = "a";
        
        Object result = map.put(key, value);
        
        // The defect causes this to return null instead of "a"
        // This assertion will fail on the defective version, revealing the bug
        assertEquals("put should return the added value", value, result);
        
        // Additional verification
        assertTrue("Map should contain the key", map.containsKey(key));
        Collection coll = map.getCollection(key);
        assertNotNull("Collection should exist", coll);
        assertEquals("Collection should have 1 element", 1, coll.size());
        assertTrue("Collection should contain the value", coll.contains(value));
    }
    
    @Test(timeout = 4000)
    public void testPutWithSet() {
        // This test directly targets the known defect from Defects4J
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), HashSet.class);
        String key = "testKey";
        String value = "a";
        
        Object result = map.put(key, value);
        
        // The defect causes this to return null instead of "a"
        // This assertion will fail on the defective version, revealing the bug
        assertEquals("put should return the added value", value, result);
        
        // Additional verification
        assertTrue("Map should contain the key", map.containsKey(key));
        Collection coll = map.getCollection(key);
        assertNotNull("Collection should exist", coll);
        assertEquals("Collection should have 1 element", 1, coll.size());
        assertTrue("Collection should contain the value", coll.contains(value));
    }
    
    @Test(timeout = 4000)
    public void testPutAll_KeyCollection() {
        // This test directly targets the known defect from Defects4J
        MultiValueMap map = new MultiValueMap();
        String key = "testKey";
        Collection values = new ArrayList();
        values.add("a");
        values.add("b");
        
        boolean result = map.putAll(key, values);
        
        // The defect causes this to return false instead of true
        // This assertion will fail on the defective version, revealing the bug
        assertTrue("putAll should return true when map changes", result);
        
        // Additional verification
        assertTrue("Map should contain the key", map.containsKey(key));
        Collection coll = map.getCollection(key);
        assertNotNull("Collection should exist", coll);
        assertEquals("Collection should have 2 elements", 2, coll.size());
    }
    
    @Test(timeout = 4000)
    public void testPutAllWithSet() {
        // Test putAll with a Set-based collection
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), HashSet.class);
        String key = "testKey";
        Collection values = new ArrayList();
        values.add("a");
        values.add("b");
        values.add("a"); // duplicate, should be ignored by Set
        
        boolean result = map.putAll(key, values);
        
        // The defect causes this to return false instead of true
        assertTrue("putAll should return true when map changes", result);
        
        Collection coll = map.getCollection(key);
        assertNotNull("Collection should exist", coll);
        assertEquals("Set should have 2 unique elements", 2, coll.size());
    }
    
    @Test(timeout = 4000)
    public void testPutMultipleValuesRevealBug() {
        // This test demonstrates the cumulative effect of the bug
        MultiValueMap map = new MultiValueMap();
        
        // First put - should return "a" but bug returns null
        Object result1 = map.put("key1", "a");
        assertEquals("First put should return 'a'", "a", result1);
        
        // Second put to same key - should return "b" (existing collection path works)
        Object result2 = map.put("key1", "b");
        assertEquals("Second put should return 'b'", "b", result2);
        
        // Third put to new key - should return "c" but bug returns null
        Object result3 = map.put("key2", "c");
        assertEquals("Third put should return 'c'", "c", result3);
        
        // Verify total state
        assertEquals("totalSize should be 3", 3, map.totalSize());
        assertEquals("Map should have 2 keys", 2, map.size());
    }
    
    // ===== Partition D: Exception & Defensive Guard Paths =====
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorWithNullFactory() {
        // The protected constructor should throw IllegalArgumentException for null factory
        new MultiValueMap(new HashMap(), null);
    }
    
    @Test(timeout = 4000)
    public void testDecorateWithNullMap() {
        // decorate should handle null map (will throw NPE from AbstractMapDecorator)
        try {
            MultiValueMap.decorate(null);
            fail("Should throw NullPointerException for null map");
        } catch (NullPointerException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testIteratorForNonExistentKey() {
        MultiValueMap map = new MultiValueMap();
        Iterator it = map.iterator("nonExistentKey");
        assertNotNull("Iterator should not be null", it);
        assertFalse("Iterator should be empty", it.hasNext());
    }
    
    @Test(timeout = 4000)
    public void testIteratorForExistingKey() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        map.put(key, "value1");
        map.put(key, "value2");
        
        Iterator it = map.iterator(key);
        assertNotNull("Iterator should not be null", it);
        assertTrue("Iterator should have next", it.hasNext());
        assertEquals("First value should be value1", "value1", it.next());
        assertTrue("Iterator should have next", it.hasNext());
        assertEquals("Second value should be value2", "value2", it.next());
        assertFalse("Iterator should not have more elements", it.hasNext());
    }
    
    @Test(timeout = 4000)
    public void testIteratorRemove() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        map.put(key, "value1");
        map.put(key, "value2");
        
        Iterator it = map.iterator(key);
        it.next(); // value1
        it.remove(); // remove value1
        
        Collection coll = map.getCollection(key);
        assertEquals("Collection should have 1 value", 1, coll.size());
        assertFalse("Collection should not contain removed value", coll.contains("value1"));
        assertTrue("Collection should contain remaining value", coll.contains("value2"));
    }
    
    @Test(timeout = 4000)
    public void testIteratorRemoveLastValue() {
        MultiValueMap map = new MultiValueMap();
        Object key = "key1";
        map.put(key, "value1");
        
        Iterator it = map.iterator(key);
        it.next(); // value1
        it.remove(); // remove the only value
        
        // Key should be removed from map
        assertFalse("Key should be removed from map", map.containsKey(key));
    }
    
    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    
    @Test(timeout = 4000)
    public void testValuesCollection() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key1", "value2");
        map.put("key2", "value3");
        
        Collection values = map.values();
        assertNotNull("values() should not return null", values);
        assertEquals("values collection should have 3 elements", 3, values.size());
        assertTrue("values should contain value1", values.contains("value1"));
        assertTrue("values should contain value2", values.contains("value2"));
        assertTrue("values should contain value3", values.contains("value3"));
    }
    
    @Test(timeout = 4000)
    public void testValuesCollectionClear() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key2", "value2");
        
        Collection values = map.values();
        values.clear();
        
        assertTrue("Map should be empty after values clear", map.isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testPutAllWithNormalMap() {
        MultiValueMap map = new MultiValueMap();
        Map normalMap = new HashMap();
        normalMap.put("key1", "value1");
        normalMap.put("key2", "value2");
        
        map.putAll(normalMap);
        
        assertEquals("Map should have 2 keys", 2, map.size());
        assertTrue("Map should contain key1", map.containsKey("key1"));
        assertTrue("Map should contain key2", map.containsKey("key2"));
        
        Collection coll1 = map.getCollection("key1");
        assertEquals("Collection for key1 should have 1 value", 1, coll1.size());
        assertTrue("Collection for key1 should contain value1", coll1.contains("value1"));
    }
    
    @Test(timeout = 4000)
    public void testPutAllWithMultiMap() {
        MultiValueMap map = new MultiValueMap();
        MultiValueMap sourceMap = new MultiValueMap();
        sourceMap.put("key1", "value1");
        sourceMap.put("key1", "value2");
        sourceMap.put("key2", "value3");
        
        map.putAll(sourceMap);
        
        assertEquals("Map should have 2 keys", 2, map.size());
        
        Collection coll1 = map.getCollection("key1");
        assertEquals("Collection for key1 should have 2 values", 2, coll1.size());
        assertTrue("Collection for key1 should contain value1", coll1.contains("value1"));
        assertTrue("Collection for key1 should contain value2", coll1.contains("value2"));
        
        Collection coll2 = map.getCollection("key2");
        assertEquals("Collection for key2 should have 1 value", 1, coll2.size());
        assertTrue("Collection for key2 should contain value3", coll2.contains("value3"));
    }
    
    @Test(timeout = 4000)
    public void testDecorateWithCollectionClass() {
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), ArrayList.class);
        
        Object result = map.put("key1", "value1");
        // This also reveals the bug
        assertEquals("put should return the value", "value1", result);
        
        Collection coll = map.getCollection("key1");
        assertTrue("Collection should be ArrayList", coll instanceof ArrayList);
    }
    
    @Test(timeout = 4000)
    public void testDecorateWithFactory() {
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), new Factory() {
            public Object create() {
                return new ArrayList();
            }
        });
        
        Object result = map.put("key1", "value1");
        // This also reveals the bug
        assertEquals("put should return the value", "value1", result);
        
        Collection coll = map.getCollection("key1");
        assertNotNull("Collection should exist", coll);
        assertTrue("Collection should contain value", coll.contains("value1"));
    }
    
    @Test(timeout = 4000)
    public void testEmptyMapBehavior() {
        MultiValueMap map = new MultiValueMap();
        
        assertTrue("New map should be empty", map.isEmpty());
        assertEquals("New map should have size 0", 0, map.size());
        assertEquals("totalSize should be 0", 0, map.totalSize());
        assertFalse("containsValue should return false", map.containsValue("anything"));
        assertNull("getCollection should return null", map.getCollection("anyKey"));
        assertEquals("size(key) should return 0", 0, map.size("anyKey"));
    }
    
    @Test(timeout = 4000)
    public void testMultipleKeysWithMultipleValues() {
        MultiValueMap map = new MultiValueMap();
        
        map.put("key1", "a");
        map.put("key1", "b");
        map.put("key2", "c");
        map.put("key2", "d");
        map.put("key2", "e");
        map.put("key3", "f");
        
        assertEquals("Map should have 3 keys", 3, map.size());
        assertEquals("totalSize should be 6", 6, map.totalSize());
        assertEquals("key1 should have 2 values", 2, map.size("key1"));
        assertEquals("key2 should have 3 values", 3, map.size("key2"));
        assertEquals("key3 should have 1 value", 1, map.size("key3"));
    }
}