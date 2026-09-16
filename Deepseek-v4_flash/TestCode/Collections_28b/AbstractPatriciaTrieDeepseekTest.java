package org.apache.commons.collections4.trie;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: AbstractPatriciaTrie (via PatriciaTrie<String, String>)
 * 
 * Known Defect: testPrefixMapClear -> NullPointerException
 *   - Likely cause: clearing a prefix map or the underlying trie leaves internal
 *     pointers (e.g., prefixStart) in an inconsistent state, causing NPE on subsequent
 *     iteration or size() calls.
 * 
 * Branches targeted:
 *   - put(): null key, zero-length key, existing key, new key, bitIndex cases (valid, null, equal)
 *   - getEntry(): null key, missing key, existing key
 *   - containsKey(): null key, missing, existing
 *   - remove(): null key, missing, external node, internal node
 *   - clear(): empty trie, non-empty trie
 *   - firstEntry()/lastEntry(): empty, single, multiple
 *   - nextEntry()/previousEntry(): boundaries, internal nodes
 *   - higherEntry()/lowerEntry()/ceilingEntry()/floorEntry(): various key positions
 *   - prefixMap(): empty prefix, full prefix, partial prefix, clear after prefix
 *   - subMap/headMap/tailMap: range boundaries, inclusive/exclusive
 *   - entrySet/keySet/values: iteration, remove, clear
 *   - mapIterator: forward/backward iteration
 *   - select/selectKey/selectValue: XOR metric
 *   - serialization: readObject/writeObject (via ObjectStream)
 * 
 * Boundary values: null, empty string, single character, multi-character, keys with common prefixes.
 * 
 * Test structure:
 *   A: Core functional logic & state transitions
 *   B: Boundary value analysis & extremes
 *   C: Defect-targeted branch zone (testPrefixMapClear)
 *   D: Exception & defensive guard paths
 *   E: Object lifecycle & contract integrity
 */
public class AbstractPatriciaTrieDeepseekTest {

    // Helper to create a trie with String keys
    private PatriciaTrie<String, String> createTrie() {
        return new PatriciaTrie<String, String>(new StringKeyAnalyzer());
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testPutAndGet() {
        PatriciaTrie<String, String> trie = createTrie();
        assertNull(trie.put("key1", "value1"));
        assertEquals("value1", trie.get("key1"));
        assertEquals("value1", trie.put("key1", "value2")); // replace
        assertEquals("value2", trie.get("key1"));
        assertEquals(1, trie.size());
    }

    @Test(timeout = 4000)
    public void testPutMultipleKeys() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");
        assertEquals(3, trie.size());
        assertEquals("1", trie.get("a"));
        assertEquals("2", trie.get("b"));
        assertEquals("3", trie.get("c"));
    }

    @Test(timeout = 4000)
    public void testPutWithCommonPrefix() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("abc", "v1");
        trie.put("abd", "v2");
        trie.put("ab", "v3");
        assertEquals(3, trie.size());
        assertEquals("v1", trie.get("abc"));
        assertEquals("v2", trie.get("abd"));
        assertEquals("v3", trie.get("ab"));
    }

    @Test(timeout = 4000)
    public void testContainsKey() {
        PatriciaTrie<String, String> trie = createTrie();
        assertFalse(trie.containsKey("key"));
        trie.put("key", "val");
        assertTrue(trie.containsKey("key"));
        assertFalse(trie.containsKey("other"));
        assertFalse(trie.containsKey(null)); // null returns false
    }

    @Test(timeout = 4000)
    public void testRemove() {
        PatriciaTrie<String, String> trie = createTrie();
        assertNull(trie.remove("nonexistent"));
        trie.put("key", "val");
        assertEquals("val", trie.remove("key"));
        assertNull(trie.get("key"));
        assertEquals(0, trie.size());
    }

    @Test(timeout = 4000)
    public void testRemoveInternalNode() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");
        // Remove a key that is an internal node (e.g., "b" if it splits)
        assertEquals("2", trie.remove("b"));
        assertEquals(2, trie.size());
        assertNull(trie.get("b"));
        assertEquals("1", trie.get("a"));
        assertEquals("3", trie.get("c"));
    }

    @Test(timeout = 4000)
    public void testClear() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("a", "1");
        trie.put("b", "2");
        trie.clear();
        assertEquals(0, trie.size());
        assertTrue(trie.isEmpty());
        assertNull(trie.get("a"));
        assertNull(trie.get("b"));
    }

    @Test(timeout = 4000)
    public void testSizeAndIsEmpty() {
        PatriciaTrie<String, String> trie = createTrie();
        assertTrue(trie.isEmpty());
        assertEquals(0, trie.size());
        trie.put("k", "v");
        assertFalse(trie.isEmpty());
        assertEquals(1, trie.size());
    }

    @Test(timeout = 4000)
    public void testFirstKeyLastKey() {
        PatriciaTrie<String, String> trie = createTrie();
        try {
            trie.firstKey();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) { /* expected */ }
        try {
            trie.lastKey();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) { /* expected */ }

        trie.put("b", "2");
        trie.put("a", "1");
        trie.put("c", "3");
        assertEquals("a", trie.firstKey());
        assertEquals("c", trie.lastKey());
    }

    @Test(timeout = 4000)
    public void testNextKeyPreviousKey() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");

        assertEquals("b", trie.nextKey("a"));
        assertEquals("c", trie.nextKey("b"));
        assertNull(trie.nextKey("c"));

        assertNull(trie.previousKey("a"));
        assertEquals("a", trie.previousKey("b"));
        assertEquals("b", trie.previousKey("c"));

        // key not in trie
        assertNull(trie.nextKey("x"));
        assertNull(trie.previousKey("x"));

        // null key throws NPE
        try {
            trie.nextKey(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) { /* expected */ }
        try {
            trie.previousKey(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) { /* expected */ }
    }

    @Test(timeout = 4000)
    public void testSelect() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("H", "valueH");   // 1001000
        trie.put("L", "valueL");   // 1001100
        // D = 1000100, XOR distance: D^L = 0001000, D^H = 0001100 -> L is closer
        Map.Entry<String, String> entry = trie.select("D");
        assertNotNull(entry);
        assertEquals("L", entry.getKey());
        assertEquals("valueL", entry.getValue());

        assertEquals("L", trie.selectKey("D"));
        assertEquals("valueL", trie.selectValue("D"));

        // key not in trie, but closest
        entry = trie.select("Z");
        assertNotNull(entry);
        // Should be "L" or "H"? Actually Z is 1011010, XOR with H: 0010010, with L: 0010110 -> H closer
        assertEquals("H", entry.getKey());
    }

    @Test(timeout = 4000)
    public void testHigherLowerCeilingFloor() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("a", "1");
        trie.put("c", "3");
        trie.put("e", "5");

        // higherEntry
        Map.Entry<String, String> higher = trie.higherEntry("b");
        assertNotNull(higher);
        assertEquals("c", higher.getKey());

        // lowerEntry
        Map.Entry<String, String> lower = trie.lowerEntry("b");
        assertNotNull(lower);
        assertEquals("a", lower.getKey());

        // ceilingEntry (>=)
        Map.Entry<String, String> ceil = trie.ceilingEntry("b");
        assertNotNull(ceil);
        assertEquals("c", ceil.getKey());
        ceil = trie.ceilingEntry("c");
        assertNotNull(ceil);
        assertEquals("c", ceil.getKey());

        // floorEntry (<=)
        Map.Entry<String, String> floor = trie.floorEntry("b");
        assertNotNull(floor);
        assertEquals("a", floor.getKey());
        floor = trie.floorEntry("c");
        assertNotNull(floor);
        assertEquals("c", floor.getKey());

        // edge cases
        assertNull(trie.higherEntry("e"));
        assertNull(trie.lowerEntry("a"));
        assertNull(trie.ceilingEntry("z"));
        assertNull(trie.floorEntry("0"));
    }

    @Test(timeout = 4000)
    public void testEntrySet() {
        PatriciaTrie<String, String> trie = createTrie();
        Set<Map.Entry<String, String>> entrySet = trie.entrySet();
        assertTrue(entrySet.isEmpty());

        trie.put("k1", "v1");
        trie.put("k2", "v2");
        assertEquals(2, entrySet.size());
        assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>("k1", "v1")));
        assertFalse(entrySet.contains(new AbstractMap.SimpleEntry<>("k3", "v3")));

        // remove via entrySet
        entrySet.remove(new AbstractMap.SimpleEntry<>("k1", "v1"));
        assertEquals(1, trie.size());
        assertNull(trie.get("k1"));

        // clear via entrySet
        entrySet.clear();
        assertTrue(trie.isEmpty());
    }

    @Test(timeout = 4000)
    public void testKeySet() {
        PatriciaTrie<String, String> trie = createTrie();
        Set<String> keySet = trie.keySet();
        assertTrue(keySet.isEmpty());

        trie.put("a", "1");
        trie.put("b", "2");
        assertEquals(2, keySet.size());
        assertTrue(keySet.contains("a"));
        assertFalse(keySet.contains("c"));

        keySet.remove("a");
        assertEquals(1, trie.size());
        assertNull(trie.get("a"));

        keySet.clear();
        assertTrue(trie.isEmpty());
    }

    @Test(timeout = 4000)
    public void testValues() {
        PatriciaTrie<String, String> trie = createTrie();
        Collection<String> values = trie.values();
        assertTrue(values.isEmpty());

        trie.put("a", "1");
        trie.put("b", "2");
        assertEquals(2, values.size());
        assertTrue(values.contains("1"));
        assertFalse(values.contains("3"));

        values.remove("1");
        assertEquals(1, trie.size());
        assertNull(trie.get("a"));

        values.clear();
        assertTrue(trie.isEmpty());
    }

    @Test(timeout = 4000)
    public void testMapIterator() {
        PatriciaTrie<String, String> trie = createTrie();
        OrderedMapIterator<String, String> it = trie.mapIterator();
        assertFalse(it.hasPrevious());
        assertFalse(it.hasNext());

        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");

        it = trie.mapIterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("a", it.getKey());
        assertEquals("1", it.getValue());
        it.setValue("11");
        assertEquals("11", trie.get("a"));

        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertTrue(it.hasPrevious());
        assertEquals("b", it.getKey());
        assertEquals("2", it.getValue());

        assertEquals("a", it.previous());
        assertEquals("a", it.getKey());
        assertEquals("11", it.getValue());

        // remove via iterator
        it.remove();
        assertEquals(2, trie.size());
        assertNull(trie.get("a"));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testPutNullKey() {
        PatriciaTrie<String, String> trie = createTrie();
        try {
            trie.put(null, "value");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) { /* expected */ }
    }

    @Test(timeout = 4000)
    public void testPutEmptyStringKey() {
        PatriciaTrie<String, String> trie = createTrie();
        assertNull(trie.put("", "empty"));
        assertEquals("empty", trie.get(""));
        assertEquals(1, trie.size());
        // Replace empty key
        assertEquals("empty", trie.put("", "new"));
        assertEquals("new", trie.get(""));
    }

    @Test(timeout = 4000)
    public void testGetNullKey() {
        PatriciaTrie<String, String> trie = createTrie();
        assertNull(trie.get(null));
    }

    @Test(timeout = 4000)
    public void testRemoveNullKey() {
        PatriciaTrie<String, String> trie = createTrie();
        assertNull(trie.remove(null));
    }

    @Test(timeout = 4000)
    public void testContainsKeyNull() {
        PatriciaTrie<String, String> trie = createTrie();
        assertFalse(trie.containsKey(null));
    }

    @Test(timeout = 4000)
    public void testLargeNumberOfEntries() {
        PatriciaTrie<String, String> trie = createTrie();
        for (int i = 0; i < 1000; i++) {
            trie.put(String.format("%04d", i), "v" + i);
        }
        assertEquals(1000, trie.size());
        assertEquals("v42", trie.get("0042"));
    }

    @Test(timeout = 4000)
    public void testKeysWithDifferentLengths() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("a", "1");
        trie.put("ab", "2");
        trie.put("abc", "3");
        assertEquals(3, trie.size());
        assertEquals("1", trie.get("a"));
        assertEquals("2", trie.get("ab"));
        assertEquals("3", trie.get("abc"));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testPrefixMapClear() {
        // This test targets the known defect: NPE when clearing a prefix map
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("key1", "value1");
        trie.put("key2", "value2");
        trie.put("key3", "value3");

        SortedMap<String, String> prefixMap = trie.prefixMap("key");
        assertEquals(3, prefixMap.size());

        // Clear the prefix map (should remove all entries with prefix "key")
        prefixMap.clear();
        assertEquals(0, trie.size());
        assertTrue(trie.isEmpty());

        // After clear, operations on the trie should not throw NPE
        assertNull(trie.get("key1"));
        assertNull(trie.get("key2"));
        assertNull(trie.get("key3"));

        // Re-add and verify
        trie.put("newKey", "newValue");
        assertEquals(1, trie.size());
        assertEquals("newValue", trie.get("newKey"));
    }

    @Test(timeout = 4000)
    public void testPrefixMapClearWithMultiplePrefixes() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("abc", "1");
        trie.put("abd", "2");
        trie.put("ab", "3");
        trie.put("ac", "4");

        SortedMap<String, String> prefixMap = trie.prefixMap("ab");
        assertEquals(3, prefixMap.size());

        prefixMap.clear();
        assertEquals(1, trie.size()); // only "ac" remains
        assertNull(trie.get("abc"));
        assertNull(trie.get("abd"));
        assertNull(trie.get("ab"));
        assertEquals("4", trie.get("ac"));
    }

    @Test(timeout = 4000)
    public void testPrefixMapClearThenIterate() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("test1", "v1");
        trie.put("test2", "v2");
        SortedMap<String, String> prefixMap = trie.prefixMap("test");
        prefixMap.clear();
        // Iterating over the prefix map after clear should not throw NPE
        for (Map.Entry<String, String> e : prefixMap.entrySet()) {
            fail("Should be empty");
        }
        assertTrue(prefixMap.isEmpty());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testPrefixMapNullKey() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.prefixMap(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSubMapInvalidRange() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.subMap("z", "a"); // from > to
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSubMapOutOfRangeKey() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("a", "1");
        SortedMap<String, String> sub = trie.subMap("a", "b");
        sub.put("c", "2"); // key out of range
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testFirstKeyOnEmpty() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.firstKey();
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testLastKeyOnEmpty() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.lastKey();
    }

    @Test(timeout = 4000, expected = ConcurrentModificationException.class)
    public void testConcurrentModificationOnIterator() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("a", "1");
        trie.put("b", "2");
        Iterator<String> it = trie.keySet().iterator();
        trie.put("c", "3"); // structural modification
        it.next(); // should throw ConcurrentModificationException
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testIteratorRemoveWithoutNext() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("a", "1");
        Iterator<String> it = trie.keySet().iterator();
        it.remove(); // no next called
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        PatriciaTrie<String, String> trie1 = createTrie();
        PatriciaTrie<String, String> trie2 = createTrie();
        assertEquals(trie1, trie2);
        assertEquals(trie1.hashCode(), trie2.hashCode());

        trie1.put("k", "v");
        assertNotEquals(trie1, trie2);
        trie2.put("k", "v");
        assertEquals(trie1, trie2);
    }

    @Test(timeout = 4000)
    public void testToString() {
        PatriciaTrie<String, String> trie = createTrie();
        assertNotNull(trie.toString());
        trie.put("a", "1");
        assertTrue(trie.toString().contains("a"));
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("key1", "value1");
        trie.put("key2", "value2");

        // Serialize to byte array
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(trie);
        oos.close();

        // Deserialize
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        PatriciaTrie<String, String> deserialized = (PatriciaTrie<String, String>) ois.readObject();
        ois.close();

        assertEquals(trie.size(), deserialized.size());
        assertEquals(trie.get("key1"), deserialized.get("key1"));
        assertEquals(trie.get("key2"), deserialized.get("key2"));
    }

    @Test(timeout = 4000)
    public void testClone() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("a", "1");
        PatriciaTrie<String, String> cloned = (PatriciaTrie<String, String>) trie.clone();
        assertEquals(trie, cloned);
        cloned.put("b", "2");
        assertNotEquals(trie.size(), cloned.size());
    }

    // ==================== Additional Coverage for Range Maps ====================

    @Test(timeout = 4000)
    public void testSubMap() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");
        trie.put("d", "4");

        SortedMap<String, String> sub = trie.subMap("b", "d");
        assertEquals(2, sub.size());
        assertEquals("2", sub.get("b"));
        assertEquals("3", sub.get("c"));
        assertNull(sub.get("a"));
        assertNull(sub.get("d"));

        // firstKey and lastKey
        assertEquals("b", sub.firstKey());
        assertEquals("c", sub.lastKey());

        // remove from submap
        sub.remove("b");
        assertEquals(3, trie.size());
        assertNull(trie.get("b"));
    }

    @Test(timeout = 4000)
    public void testHeadMap() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");

        SortedMap<String, String> head = trie.headMap("c");
        assertEquals(2, head.size());
        assertTrue(head.containsKey("a"));
        assertTrue(head.containsKey("b"));
        assertFalse(head.containsKey("c"));
    }

    @Test(timeout = 4000)
    public void testTailMap() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");

        SortedMap<String, String> tail = trie.tailMap("b");
        assertEquals(2, tail.size());
        assertTrue(tail.containsKey("b"));
        assertTrue(tail.containsKey("c"));
        assertFalse(tail.containsKey("a"));
    }

    @Test(timeout = 4000)
    public void testPrefixMapWithEmptyPrefix() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("a", "1");
        trie.put("b", "2");
        // prefixMap with empty string returns the whole trie
        SortedMap<String, String> prefixMap = trie.prefixMap("");
        assertEquals(trie, prefixMap);
    }

    @Test(timeout = 4000)
    public void testPrefixMapWithFullMatch() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("test", "value");
        SortedMap<String, String> prefixMap = trie.prefixMap("test");
        assertEquals(1, prefixMap.size());
        assertEquals("value", prefixMap.get("test"));
    }

    @Test(timeout = 4000)
    public void testPrefixMapNoMatch() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("abc", "1");
        SortedMap<String, String> prefixMap = trie.prefixMap("xyz");
        assertTrue(prefixMap.isEmpty());
    }

    @Test(timeout = 4000)
    public void testRangeEntrySetIterator() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("a", "1");
        trie.put("b", "2");
        trie.put("c", "3");
        SortedMap<String, String> sub = trie.subMap("a", "c");
        Iterator<Map.Entry<String, String>> it = sub.entrySet().iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next().getKey());
        assertTrue(it.hasNext());
        assertEquals("b", it.next().getKey());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testPrefixRangeEntrySetSingleton() {
        PatriciaTrie<String, String> trie = createTrie();
        trie.put("key", "value");
        SortedMap<String, String> prefixMap = trie.prefixMap("key");
        assertEquals(1, prefixMap.size());
        Iterator<Map.Entry<String, String>> it = prefixMap.entrySet().iterator();
        assertTrue(it.hasNext());
        Map.Entry<String, String> entry = it.next();
        assertEquals("key", entry.getKey());
        assertEquals("value", entry.getValue());
        assertFalse(it.hasNext());
        it.remove();
        assertTrue(trie.isEmpty());
    }
}