package org.apache.commons.collections.map;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Flat3MapDeepseekTest - Comprehensive White-Box Test Suite
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: Flat3Map (flat mode up to 3 entries, then delegates to AbstractHashedMap)
 * 
 * Key Decision Branches:
 * 1. delegateMap != null vs null (flat vs delegate mode)
 * 2. key == null vs key != null in get/containsKey/put/remove
 * 3. size switch cases: 0, 1, 2, 3, default (>=4 triggers conversion)
 * 4. Drop-through logic in switch statements for sequential checks
 * 5. Hash collision handling in flat mode
 * 6. remove() compaction logic when removing non-last element
 * 7. put() overwrite vs new entry logic
 * 8. convertToMap() transition at size 4
 * 9. Iterator state management (canRemove, nextIndex)
 * 10. equals/hashCode/toString edge cases
 * 
 * Known Defect (Defects4J testCollections261):
 * - Bug: When removing a key from a Flat3Map with 3 entries, if the removed key
 *   is not the last one (e.g., key2), the compaction logic incorrectly copies
 *   the last entry (key3/value3) into the removed slot, but the size is set to 2.
 *   However, the bug manifests when the map is later queried - specifically,
 *   containsKey or get may fail to find the remaining keys because the hash
 *   values are not properly updated during compaction.
 * - The defect is triggered when removing a non-last key from a 3-entry map
 *   and then checking for the key that was moved (originally key3, now key2).
 *   The hash value for the moved entry is not updated to match its new position.
 * 
 * Test Strategy:
 * - Partition A: Core functional tests (get, put, containsKey, containsValue, remove, clear)
 * - Partition B: Boundary tests (null keys/values, empty map, size transitions)
 * - Partition C: Defect-targeted tests (remove compaction bug, hash consistency)
 * - Partition D: Exception paths (iterator misuse, invalid states)
 * - Partition E: Contract tests (equals, hashCode, clone, serialization)
 */
public class Flat3MapDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================
    
    @Test(timeout = 4000)
    public void testPutAndGetSingleEntry() {
        Flat3Map map = new Flat3Map();
        assertNull(map.put("key1", "value1"));
        assertEquals("value1", map.get("key1"));
        assertEquals(1, map.size());
        assertFalse(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testPutAndGetMultipleEntries() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        assertEquals(3, map.size());
        assertEquals("1", map.get("A"));
        assertEquals("2", map.get("B"));
        assertEquals("3", map.get("C"));
    }

    @Test(timeout = 4000)
    public void testPutOverwriteExistingKey() {
        Flat3Map map = new Flat3Map();
        map.put("key", "oldValue");
        assertEquals("oldValue", map.put("key", "newValue"));
        assertEquals("newValue", map.get("key"));
        assertEquals(1, map.size());
    }

    @Test(timeout = 4000)
    public void testPutOverwriteInThreeEntryMap() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        assertEquals("2", map.put("B", "updated"));
        assertEquals("updated", map.get("B"));
        assertEquals(3, map.size());
    }

    @Test(timeout = 4000)
    public void testContainsKey() {
        Flat3Map map = new Flat3Map();
        map.put("key", "value");
        assertTrue(map.containsKey("key"));
        assertFalse(map.containsKey("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testContainsValue() {
        Flat3Map map = new Flat3Map();
        map.put("key", "value");
        assertTrue(map.containsValue("value"));
        assertFalse(map.containsValue("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testRemoveSingleEntry() {
        Flat3Map map = new Flat3Map();
        map.put("key", "value");
        assertEquals("value", map.remove("key"));
        assertNull(map.get("key"));
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testRemoveLastEntryFromThree() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        assertEquals("3", map.remove("C"));
        assertEquals(2, map.size());
        assertEquals("1", map.get("A"));
        assertEquals("2", map.get("B"));
        assertNull(map.get("C"));
    }

    @Test(timeout = 4000)
    public void testClearFlatMap() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get("A"));
        assertNull(map.get("B"));
    }

    @Test(timeout = 4000)
    public void testDelegateModeTransition() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4"); // triggers conversion to delegate mode
        assertEquals(4, map.size());
        assertEquals("1", map.get("A"));
        assertEquals("2", map.get("B"));
        assertEquals("3", map.get("C"));
        assertEquals("4", map.get("D"));
    }

    @Test(timeout = 4000)
    public void testDelegateModePutAll() {
        Flat3Map map = new Flat3Map();
        Map<String, String> source = new HashMap<String, String>();
        source.put("A", "1");
        source.put("B", "2");
        source.put("C", "3");
        source.put("D", "4");
        map.putAll(source);
        assertEquals(4, map.size());
        assertEquals("1", map.get("A"));
        assertEquals("4", map.get("D"));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testNullKeyOperations() {
        Flat3Map map = new Flat3Map();
        assertNull(map.put(null, "nullValue"));
        assertEquals("nullValue", map.get(null));
        assertTrue(map.containsKey(null));
        assertTrue(map.containsValue("nullValue"));
        assertEquals(1, map.size());
    }

    @Test(timeout = 4000)
    public void testNullValueOperations() {
        Flat3Map map = new Flat3Map();
        assertNull(map.put("key", null));
        assertNull(map.get("key"));
        assertTrue(map.containsKey("key"));
        assertTrue(map.containsValue(null));
        assertEquals(1, map.size());
    }

    @Test(timeout = 4000)
    public void testNullKeyAndNullValue() {
        Flat3Map map = new Flat3Map();
        assertNull(map.put(null, null));
        assertNull(map.get(null));
        assertTrue(map.containsKey(null));
        assertTrue(map.containsValue(null));
        assertEquals(1, map.size());
    }

    @Test(timeout = 4000)
    public void testMultipleNullKeys() {
        Flat3Map map = new Flat3Map();
        map.put(null, "first");
        map.put("A", "1");
        map.put(null, "second"); // overwrite
        assertEquals("second", map.get(null));
        assertEquals(2, map.size());
    }

    @Test(timeout = 4000)
    public void testEmptyMapOperations() {
        Flat3Map map = new Flat3Map();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get("anything"));
        assertFalse(map.containsKey("anything"));
        assertFalse(map.containsValue("anything"));
        assertNull(map.remove("anything"));
    }

    @Test(timeout = 4000)
    public void testSizeTransitions() {
        Flat3Map map = new Flat3Map();
        assertEquals(0, map.size());
        
        map.put("A", "1");
        assertEquals(1, map.size());
        
        map.put("B", "2");
        assertEquals(2, map.size());
        
        map.put("C", "3");
        assertEquals(3, map.size());
        
        map.put("D", "4");
        assertEquals(4, map.size());
    }

    @Test(timeout = 4000)
    public void testPutAllEmptyMap() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        Map<String, String> empty = new HashMap<String, String>();
        map.putAll(empty);
        assertEquals(1, map.size());
        assertEquals("1", map.get("A"));
    }

    @Test(timeout = 4000)
    public void testPutAllWithNullKey() {
        Flat3Map map = new Flat3Map();
        Map<String, String> source = new HashMap<String, String>();
        source.put(null, "nullValue");
        source.put("A", "1");
        map.putAll(source);
        assertEquals(2, map.size());
        assertEquals("nullValue", map.get(null));
        assertEquals("1", map.get("A"));
    }

    // ==================== Partition C: Defect-Targeted Tests ====================

    /**
     * Directly targets the known Defects4J bug (testCollections261).
     * 
     * Bug scenario: Remove a non-last key from a 3-entry map, then verify
     * that the remaining keys are still accessible. The compaction logic
     * in remove() has a bug where the hash value is not properly updated
     * when moving the last entry to fill the gap.
     * 
     * Specifically, when removing key2 from a 3-entry map:
     * - key3/value3 are moved to position 2
     * - But hash2 is set to hash3 (the hash of key3), which is correct
     * - However, the bug is that after compaction, the map's internal state
     *   may have inconsistent hash values when the moved key's hash differs
     *   from the original key2's hash
     */
    @Test(timeout = 4000)
    public void testRemoveNonLastKeyFromThreeEntries_Bug261() {
        Flat3Map map = new Flat3Map();
        
        // Use keys with different hash codes to expose the bug
        String key1 = "first";
        String key2 = "second"; 
        String key3 = "third";
        
        map.put(key1, "value1");
        map.put(key2, "value2");
        map.put(key3, "value3");
        
        // Remove the middle key (key2) - this triggers compaction
        assertEquals("value2", map.remove(key2));
        
        // After removal, size should be 2
        assertEquals(2, map.size());
        
        // The remaining keys should still be accessible
        // key1 should still be at position 1
        assertEquals("value1", map.get(key1));
        assertTrue(map.containsKey(key1));
        
        // key3 should have been moved to position 2
        // This is where the bug manifests - hash may not match
        assertEquals("value3", map.get(key3));
        assertTrue(map.containsKey(key3));
        
        // The removed key should not be found
        assertNull(map.get(key2));
        assertFalse(map.containsKey(key2));
    }

    /**
     * Additional test for the same bug with null keys involved.
     */
    @Test(timeout = 4000)
    public void testRemoveNonLastNullKeyFromThreeEntries() {
        Flat3Map map = new Flat3Map();
        
        map.put(null, "nullValue");
        map.put("A", "1");
        map.put("B", "2");
        
        // Remove the first entry (null key) - triggers compaction
        assertEquals("nullValue", map.remove(null));
        
        assertEquals(2, map.size());
        assertEquals("1", map.get("A"));
        assertEquals("2", map.get("B"));
        assertNull(map.get(null));
        assertFalse(map.containsKey(null));
    }

    /**
     * Test remove compaction when removing first entry from 3-entry map
     * with non-null keys.
     */
    @Test(timeout = 4000)
    public void testRemoveFirstKeyFromThreeEntries() {
        Flat3Map map = new Flat3Map();
        
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        
        // Remove first key - triggers compaction moving B->1, C->2
        assertEquals("1", map.remove("A"));
        
        assertEquals(2, map.size());
        assertEquals("2", map.get("B"));
        assertEquals("3", map.get("C"));
        assertNull(map.get("A"));
    }

    /**
     * Test remove compaction when removing middle entry from 3-entry map
     * with null values.
     */
    @Test(timeout = 4000)
    public void testRemoveMiddleKeyWithNullValues() {
        Flat3Map map = new Flat3Map();
        
        map.put("A", null);
        map.put("B", null);
        map.put("C", null);
        
        assertEquals(null, map.remove("B"));
        
        assertEquals(2, map.size());
        assertNull(map.get("A"));
        assertNull(map.get("C"));
        assertNull(map.get("B"));
    }

    /**
     * Test that after remove compaction, containsValue still works correctly.
     */
    @Test(timeout = 4000)
    public void testContainsValueAfterRemoveCompaction() {
        Flat3Map map = new Flat3Map();
        
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        
        map.remove("B");
        
        assertTrue(map.containsValue("1"));
        assertTrue(map.containsValue("3"));
        assertFalse(map.containsValue("2"));
    }

    // ==================== Partition D: Exception & Defensive Paths ====================

    @Test(timeout = 4000)
    public void testMapIterator() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        
        MapIterator it = map.mapIterator();
        assertTrue(it.hasNext());
        Object key1 = it.next();
        assertEquals("1", map.get(key1));
        
        assertTrue(it.hasNext());
        Object key2 = it.next();
        assertEquals("2", map.get(key2));
        
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testMapIteratorRemove() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        
        MapIterator it = map.mapIterator();
        it.next();
        it.remove();
        
        assertEquals(1, map.size());
        assertFalse(map.containsKey("A"));
        assertTrue(map.containsKey("B"));
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorRemoveWithoutNext() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        
        MapIterator it = map.mapIterator();
        it.remove(); // should throw IllegalStateException
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testMapIteratorNextBeyondEnd() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        
        MapIterator it = map.mapIterator();
        it.next();
        it.next(); // should throw NoSuchElementException
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorGetKeyWithoutNext() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        
        MapIterator it = map.mapIterator();
        it.getKey(); // should throw IllegalStateException
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorGetValueWithoutNext() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        
        MapIterator it = map.mapIterator();
        it.getValue(); // should throw IllegalStateException
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorSetValueWithoutNext() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        
        MapIterator it = map.mapIterator();
        it.setValue("newValue"); // should throw IllegalStateException
    }

    @Test(timeout = 4000)
    public void testMapIteratorSetValue() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        
        MapIterator it = map.mapIterator();
        it.next();
        assertEquals("1", it.setValue("updated"));
        assertEquals("updated", map.get("A"));
    }

    @Test(timeout = 4000)
    public void testEntrySetIterator() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        
        Set entrySet = map.entrySet();
        assertEquals(2, entrySet.size());
        
        Iterator it = entrySet.iterator();
        assertTrue(it.hasNext());
        Map.Entry entry1 = (Map.Entry) it.next();
        assertNotNull(entry1.getKey());
        assertNotNull(entry1.getValue());
        
        assertTrue(it.hasNext());
        Map.Entry entry2 = (Map.Entry) it.next();
        assertNotNull(entry2.getKey());
        assertNotNull(entry2.getValue());
        
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testEntrySetRemove() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        
        Set entrySet = map.entrySet();
        assertTrue(entrySet.remove(new java.util.AbstractMap.SimpleEntry("A", "1")));
        assertEquals(1, map.size());
        assertFalse(map.containsKey("A"));
    }

    @Test(timeout = 4000)
    public void testKeySet() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        
        Set keySet = map.keySet();
        assertEquals(2, keySet.size());
        assertTrue(keySet.contains("A"));
        assertTrue(keySet.contains("B"));
        assertFalse(keySet.contains("C"));
    }

    @Test(timeout = 4000)
    public void testKeySetRemove() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        
        Set keySet = map.keySet();
        assertTrue(keySet.remove("A"));
        assertEquals(1, map.size());
        assertFalse(map.containsKey("A"));
    }

    @Test(timeout = 4000)
    public void testValues() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        
        Collection values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains("1"));
        assertTrue(values.contains("2"));
        assertFalse(values.contains("3"));
    }

    // ==================== Partition E: Object Contract Tests ====================

    @Test(timeout = 4000)
    public void testEquals() {
        Flat3Map map1 = new Flat3Map();
        map1.put("A", "1");
        map1.put("B", "2");
        
        Flat3Map map2 = new Flat3Map();
        map2.put("A", "1");
        map2.put("B", "2");
        
        assertTrue(map1.equals(map2));
        assertTrue(map2.equals(map1));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentOrder() {
        Flat3Map map1 = new Flat3Map();
        map1.put("A", "1");
        map1.put("B", "2");
        
        Flat3Map map2 = new Flat3Map();
        map2.put("B", "2");
        map2.put("A", "1");
        
        assertTrue(map1.equals(map2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullValues() {
        Flat3Map map1 = new Flat3Map();
        map1.put("A", null);
        
        Flat3Map map2 = new Flat3Map();
        map2.put("A", null);
        
        assertTrue(map1.equals(map2));
    }

    @Test(timeout = 4000)
    public void testNotEquals() {
        Flat3Map map1 = new Flat3Map();
        map1.put("A", "1");
        
        Flat3Map map2 = new Flat3Map();
        map2.put("A", "2");
        
        assertFalse(map1.equals(map2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentSize() {
        Flat3Map map1 = new Flat3Map();
        map1.put("A", "1");
        
        Flat3Map map2 = new Flat3Map();
        map2.put("A", "1");
        map2.put("B", "2");
        
        assertFalse(map1.equals(map2));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        Flat3Map map1 = new Flat3Map();
        map1.put("A", "1");
        map1.put("B", "2");
        
        Flat3Map map2 = new Flat3Map();
        map2.put("A", "1");
        map2.put("B", "2");
        
        assertEquals(map1.hashCode(), map2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeWithNullKey() {
        Flat3Map map = new Flat3Map();
        map.put(null, "value");
        
        // hash code should be 0 ^ value.hashCode() for null key
        int expected = 0 ^ "value".hashCode();
        assertEquals(expected, map.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeWithNullValue() {
        Flat3Map map = new Flat3Map();
        map.put("key", null);
        
        // hash code should be key.hashCode() ^ 0 for null value
        int expected = "key".hashCode() ^ 0;
        assertEquals(expected, map.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        
        String str = map.toString();
        assertTrue(str.contains("A"));
        assertTrue(str.contains("1"));
        assertTrue(str.startsWith("{"));
        assertTrue(str.endsWith("}"));
    }

    @Test(timeout = 4000)
    public void testToStringEmptyMap() {
        Flat3Map map = new Flat3Map();
        assertEquals("{}", map.toString());
    }

    @Test(timeout = 4000)
    public void testClone() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        
        Flat3Map cloned = (Flat3Map) map.clone();
        assertEquals(map.size(), cloned.size());
        assertEquals(map.get("A"), cloned.get("A"));
        assertEquals(map.get("B"), cloned.get("B"));
        
        // Verify independence
        cloned.put("C", "3");
        assertEquals(2, map.size());
        assertEquals(3, cloned.size());
    }

    @Test(timeout = 4000)
    public void testCloneWithDelegateMode() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4");
        
        Flat3Map cloned = (Flat3Map) map.clone();
        assertEquals(4, cloned.size());
        assertEquals("1", cloned.get("A"));
        assertEquals("4", cloned.get("D"));
    }

    @Test(timeout = 4000)
    public void testSelfReferentialToString() {
        Flat3Map map = new Flat3Map();
        map.put("key", map);
        
        String str = map.toString();
        assertTrue(str.contains("(this Map)"));
    }

    @Test(timeout = 4000)
    public void testConstructorWithMap() {
        Map<String, String> source = new HashMap<String, String>();
        source.put("A", "1");
        source.put("B", "2");
        
        Flat3Map map = new Flat3Map(source);
        assertEquals(2, map.size());
        assertEquals("1", map.get("A"));
        assertEquals("2", map.get("B"));
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorWithNullMap() {
        new Flat3Map(null);
    }

    @Test(timeout = 4000)
    public void testPutAllWithSmallMap() {
        Flat3Map map = new Flat3Map();
        map.put("existing", "value");
        
        Map<String, String> source = new HashMap<String, String>();
        source.put("A", "1");
        source.put("B", "2");
        
        map.putAll(source);
        assertEquals(3, map.size());
        assertEquals("value", map.get("existing"));
        assertEquals("1", map.get("A"));
        assertEquals("2", map.get("B"));
    }

    @Test(timeout = 4000)
    public void testRemoveFromEmptyMap() {
        Flat3Map map = new Flat3Map();
        assertNull(map.remove("anything"));
    }

    @Test(timeout = 4000)
    public void testRemoveNonExistentKey() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        assertNull(map.remove("B"));
        assertEquals(1, map.size());
    }

    @Test(timeout = 4000)
    public void testClearDelegateMode() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4");
        
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get("A"));
        
        // After clear, should be back in flat mode
        map.put("X", "Y");
        assertEquals(1, map.size());
        assertEquals("Y", map.get("X"));
    }

    @Test(timeout = 4000)
    public void testEntrySetEquals() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        
        Set entrySet = map.entrySet();
        Iterator it = entrySet.iterator();
        Map.Entry entry = (Map.Entry) it.next();
        
        // Entry should equal itself
        assertTrue(entry.equals(entry));
        
        // Entry should equal a corresponding SimpleEntry
        assertTrue(entry.equals(new java.util.AbstractMap.SimpleEntry(entry.getKey(), entry.getValue())));
    }

    @Test(timeout = 4000)
    public void testEntrySetHashCode() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        
        Set entrySet = map.entrySet();
        Iterator it = entrySet.iterator();
        Map.Entry entry = (Map.Entry) it.next();
        
        int expected = ("A".hashCode()) ^ ("1".hashCode());
        assertEquals(expected, entry.hashCode());
    }
}