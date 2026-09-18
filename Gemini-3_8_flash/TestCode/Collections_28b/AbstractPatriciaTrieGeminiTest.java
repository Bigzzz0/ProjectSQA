package org.apache.commons.collections4.trie;

/*
 * [Branch & Defect Analysis Matrix]
 * ------------------------------------------------------------------------------------------------------
 * TARGET: org.apache.commons.collections4.trie.AbstractPatriciaTrie
 *
 * 1. DEFECT-TARGETED ZONE (Defects4J Ground Truth):
 *    - PrefixRangeEntrySet.EntryIterator#remove: In the original codebase, when removing an element
 *      that causes the prefix subtree root to shift or become null, accessing subtree.bitIndex causes a
 *      NullPointerException.
 *      Test: testPrefixMapClear_DefectTarget triggers clear() on prefixMap view which iteratively removes
 *      entries until subtree is recomputed.
 *
 * 2. EQUIVALENCE PARTITIONS & BOUNDARY VALUE ANALYSIS:
 *    - Root Node (lengthInBits == 0): Key "" (empty string). Stored at root. Insertion, update, lookup,
 *      higherEntry, ceilingEntry, lowerEntry, floorEntry, remove.
 *    - Bit Index Traversal & Uplink Validation: Internal vs. External node removals; left/right uplink
 *      resolution; sibling and parent pointer repointing.
 *    - RangeMap Views (SubMap, HeadMap, TailMap):
 *      * fromKey > toKey checks (IllegalArgumentException).
 *      * fromKey == null && toKey == null (IllegalArgumentException).
 *      * Out of range inserts/queries (IllegalArgumentException, inRange, inRange2).
 *      * Empty range boundaries, firstKey/lastKey NoSuchElementException.
 *    - PrefixRangeMap:
 *      * offset + length > prefix bit length (IllegalArgumentException).
 *      * offset + length == 0 returns trie itself.
 *      * Subtree queries with no match, single match (SingletonIterator), and multiple matches (EntryIterator).
 *    - Iterator & Fail-Fast ModCount: ConcurrentModificationException on trie mutation during iteration,
 *      IllegalStateException when calling getKey/getValue/setValue/remove prior to next().
 *    - Contract & Serialization Integrity: Round-trip Java serialization of PatriciaTrie.
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.collections4.OrderedMapIterator;

public class AbstractPatriciaTrieGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Known Bug)
    // =========================================================================

    /**
     * Targets the documented Defects4J defect:
     * PrefixRangeEntrySet$EntryIterator.remove() throws NullPointerException when
     * clearing or iteratively removing all entries in a prefixMap.
     */
    @Test(timeout = 4000)
    public void testPrefixMapClear_DefectTarget() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        trie.put("Alpha", "1");
        trie.put("Alphabet", "2");
        trie.put("Alligator", "3");

        final SortedMap<String, String> prefixMap = trie.prefixMap("Alph");
        assertEquals(2, prefixMap.size());
        assertTrue(prefixMap.containsKey("Alpha"));
        assertTrue(prefixMap.containsKey("Alphabet"));

        // Triggers PrefixRangeMap clear -> EntryIterator.remove() which exposes the NPE bug
        prefixMap.clear();

        assertEquals(0, prefixMap.size());
        assertTrue(prefixMap.isEmpty());
        assertEquals(1, trie.size());
        assertTrue(trie.containsKey("Alligator"));
        assertFalse(trie.containsKey("Alpha"));
        assertFalse(trie.containsKey("Alphabet"));
    }

    @Test(timeout = 4000)
    public void testPrefixMapIteratorRemoveSingleAndMultiple() {
        final PatriciaTrie<Integer> trie = new PatriciaTrie<Integer>();
        trie.put("test", 1);
        trie.put("testing", 2);
        trie.put("tester", 3);

        final SortedMap<String, Integer> pMap = trie.prefixMap("test");
        assertEquals(3, pMap.size());

        final Iterator<Map.Entry<String, Integer>> it = pMap.entrySet().iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }

        assertEquals(0, pMap.size());
        assertEquals(0, trie.size());
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testPutGetAndOverwrite() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        assertTrue(trie.isEmpty());
        assertEquals(0, trie.size());

        assertNull(trie.put("key1", "val1"));
        assertEquals(1, trie.size());
        assertEquals("val1", trie.get("key1"));

        // Overwrite existing key
        assertEquals("val1", trie.put("key1", "val1_updated"));
        assertEquals(1, trie.size());
        assertEquals("val1_updated", trie.get("key1"));
    }

    @Test(timeout = 4000)
    public void testClearLifecycle() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        trie.put("A", "alpha");
        trie.put("B", "beta");
        trie.put("", "rootVal");
        assertEquals(3, trie.size());

        trie.clear();
        assertEquals(0, trie.size());
        assertTrue(trie.isEmpty());
        assertNull(trie.get("A"));
        assertNull(trie.get(""));
        assertFalse(trie.containsKey("A"));
        assertFalse(trie.containsKey(""));
    }

    @Test(timeout = 4000)
    public void testSelectSelectKeySelectValue() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        assertNull(trie.select("anything"));
        assertNull(trie.selectKey("anything"));
        assertNull(trie.selectValue("anything"));

        trie.put("cat", "feline");
        trie.put("dog", "canine");

        final Map.Entry<String, String> selected = trie.select("c");
        assertNotNull(selected);
        assertEquals("cat", selected.getKey());
        assertEquals("feline", selected.getValue());

        assertEquals("cat", trie.selectKey("cat"));
        assertEquals("canine", trie.selectValue("dog"));
    }

    @Test(timeout = 4000)
    public void testInternalAndExternalNodeRemovals() {
        final PatriciaTrie<Integer> trie = new PatriciaTrie<Integer>();
        // Insert a rich set of keys that generate both internal and external nodes
        final String[] keys = {"bravo", "alpha", "charlie", "delta", "echo", "foxtrot", "golf", "hotel"};
        for (int i = 0; i < keys.length; i++) {
            trie.put(keys[i], i);
        }
        assertEquals(keys.length, trie.size());

        // Remove leaf/external node
        assertEquals(Integer.valueOf(1), trie.remove("alpha"));
        assertFalse(trie.containsKey("alpha"));
        assertEquals(keys.length - 1, trie.size());

        // Remove intermediate/internal nodes
        for (int i = 1; i < keys.length; i++) {
            if (trie.containsKey(keys[i])) {
                assertNotNull(trie.remove(keys[i]));
            }
        }
        // Remove bravo (last remaining)
        assertEquals(Integer.valueOf(0), trie.remove("bravo"));
        assertEquals(0, trie.size());
        assertTrue(trie.isEmpty());
    }

    @Test(timeout = 4000)
    public void testNextAndPreviousKeyNavigation() {
        final PatriciaTrie<Integer> trie = new PatriciaTrie<Integer>();
        trie.put("apple", 1);
        trie.put("banana", 2);
        trie.put("cherry", 3);

        assertEquals("banana", trie.nextKey("apple"));
        assertEquals("cherry", trie.nextKey("banana"));
        assertNull(trie.nextKey("cherry"));

        assertEquals("banana", trie.previousKey("cherry"));
        assertEquals("apple", trie.previousKey("banana"));
        assertNull(trie.previousKey("apple"));

        assertNull(trie.nextKey("nonexistent"));
        assertNull(trie.previousKey("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testHigherCeilingLowerFloorEntry() {
        final PatriciaTrie<Integer> trie = new PatriciaTrie<Integer>();
        trie.put("b", 2);
        trie.put("d", 4);
        trie.put("f", 6);

        // higherEntry
        assertEquals("d", trie.higherEntry("b").getKey());
        assertEquals("d", trie.higherEntry("c").getKey());
        assertNull(trie.higherEntry("f"));
        assertNull(trie.higherEntry("z"));

        // ceilingEntry
        assertEquals("b", trie.ceilingEntry("b").getKey());
        assertEquals("d", trie.ceilingEntry("c").getKey());
        assertNull(trie.ceilingEntry("g"));

        // lowerEntry
        assertEquals("d", trie.lowerEntry("f").getKey());
        assertEquals("d", trie.lowerEntry("e").getKey());
        assertNull(trie.lowerEntry("b"));
        assertNull(trie.lowerEntry("a"));

        // floorEntry
        assertEquals("f", trie.floorEntry("f").getKey());
        assertEquals("d", trie.floorEntry("e").getKey());
        assertNull(trie.floorEntry("a"));
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyStringAsRootKey() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();

        // Empty string is stored at root
        assertNull(trie.put("", "ROOT_VALUE"));
        assertEquals(1, trie.size());
        assertTrue(trie.containsKey(""));
        assertEquals("ROOT_VALUE", trie.get(""));

        // Replace root value
        assertEquals("ROOT_VALUE", trie.put("", "ROOT_VALUE2"));
        assertEquals("ROOT_VALUE2", trie.get(""));
        assertEquals(1, trie.size());

        // higher/ceiling/floor/lower against root
        assertNull(trie.lowerEntry(""));
        assertEquals("", trie.floorEntry("").getKey());
        assertEquals("", trie.ceilingEntry("").getKey());
        assertNull(trie.higherEntry(""));

        // Add another node after root
        trie.put("after", "AFTER_VALUE");
        assertEquals("after", trie.higherEntry("").getKey());

        // Remove root entry
        assertEquals("ROOT_VALUE2", trie.remove(""));
        assertFalse(trie.containsKey(""));
        assertEquals(1, trie.size());
        assertEquals("after", trie.get("after"));
    }

    @Test(timeout = 4000)
    public void testEmptyTrieExtremes() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();

        assertNull(trie.firstEntry());
        assertNull(trie.lastEntry());
        assertNull(trie.higherEntry("any"));
        assertNull(trie.ceilingEntry("any"));
        assertNull(trie.lowerEntry("any"));
        assertNull(trie.floorEntry("any"));

        try {
            trie.firstKey();
            fail("Expected NoSuchElementException for firstKey() on empty trie");
        } catch (final NoSuchElementException expected) {
            // Success
        }

        try {
            trie.lastKey();
            fail("Expected NoSuchElementException for lastKey() on empty trie");
        } catch (final NoSuchElementException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testComparatorContract() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        assertNotNull(trie.comparator());
        assertTrue(trie.comparator() instanceof KeyAnalyzer);
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testPutNullKeyThrowsNPE() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        trie.put(null, "val");
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNextKeyNullThrowsNPE() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        trie.nextKey(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testPreviousKeyNullThrowsNPE() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        trie.previousKey(null);
    }

    @Test(timeout = 4000)
    public void testGetAndRemoveNullKeySafelyReturnsNull() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        assertNull(trie.get(null));
        assertNull(trie.remove(null));
        assertFalse(trie.containsKey(null));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSubMapFromKeyGreaterThanToKeyThrowsIAE() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        trie.put("b", "val");
        trie.subMap("z", "a");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSubMapNullEndpointsThrowsIAE() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        trie.subMap(null, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSubMapPutOutOfRangeThrowsIAE() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        trie.put("m", "middle");
        final SortedMap<String, String> sub = trie.subMap("c", "p");
        sub.put("z", "out_of_range");
    }

    @Test(expected = ConcurrentModificationException.class, timeout = 4000)
    public void testFailFastIteratorOnPut() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        trie.put("k1", "v1");
        trie.put("k2", "v2");

        final Iterator<String> it = trie.keySet().iterator();
        assertTrue(it.hasNext());
        trie.put("k3", "v3");
        it.next(); // Must throw CME
    }

    @Test(expected = ConcurrentModificationException.class, timeout = 4000)
    public void testFailFastIteratorOnRemove() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        trie.put("k1", "v1");
        trie.put("k2", "v2");

        final Iterator<String> it = trie.keySet().iterator();
        assertTrue(it.hasNext());
        trie.remove("k1");
        it.next(); // Must throw CME
    }

    // =========================================================================
    // PARTITION E: VIEWS, ITERATORS, SUBMAPS & CONTRACT INTEGRITY
    // =========================================================================

    @Test(timeout = 4000)
    public void testKeySetEntrySetAndValuesViews() {
        final PatriciaTrie<Integer> trie = new PatriciaTrie<Integer>();
        trie.put("1", 1);
        trie.put("2", 2);
        trie.put("3", 3);

        final Set<String> keys = trie.keySet();
        assertEquals(3, keys.size());
        assertTrue(keys.contains("1"));
        assertFalse(keys.contains("4"));
        assertTrue(keys.remove("1"));
        assertFalse(trie.containsKey("1"));
        assertEquals(2, trie.size());

        final Collection<Integer> values = trie.values();
        assertEquals(2, values.size());
        assertTrue(values.contains(2));
        assertTrue(values.remove(2));
        assertFalse(trie.containsKey("2"));
        assertEquals(1, trie.size());

        final Set<Map.Entry<String, Integer>> entries = trie.entrySet();
        assertEquals(1, entries.size());
        final Map.Entry<String, Integer> remaining = entries.iterator().next();
        assertEquals("3", remaining.getKey());
        assertEquals(Integer.valueOf(3), remaining.getValue());

        assertTrue(entries.remove(remaining));
        assertTrue(trie.isEmpty());
    }

    @Test(timeout = 4000)
    public void testOrderedMapIteratorTraversal() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        trie.put("k1", "v1");
        trie.put("k2", "v2");

        final OrderedMapIterator<String, String> mapIt = trie.mapIterator();
        assertFalse(mapIt.hasPrevious());
        assertTrue(mapIt.hasNext());

        try {
            mapIt.getKey();
            fail("Expected IllegalStateException prior to next()");
        } catch (final IllegalStateException expected) {
            // Success
        }

        final String nextKey = mapIt.next();
        assertEquals("k1", nextKey);
        assertEquals("k1", mapIt.getKey());
        assertEquals("v1", mapIt.getValue());

        mapIt.setValue("v1_mod");
        assertEquals("v1_mod", trie.get("k1"));

        assertTrue(mapIt.hasNext());
        assertEquals("k2", mapIt.next());

        assertTrue(mapIt.hasPrevious());
        assertEquals("k2", mapIt.previous());
        assertEquals("k1", mapIt.previous());
        assertFalse(mapIt.hasPrevious());
    }

    @Test(timeout = 4000)
    public void testSubMapHeadMapTailMap() {
        final PatriciaTrie<Integer> trie = new PatriciaTrie<Integer>();
        trie.put("apple", 1);
        trie.put("banana", 2);
        trie.put("cherry", 3);
        trie.put("date", 4);
        trie.put("fig", 5);

        // SubMap ["banana", "date")
        final SortedMap<String, Integer> sub = trie.subMap("banana", "date");
        assertEquals(2, sub.size());
        assertEquals("banana", sub.firstKey());
        assertEquals("cherry", sub.lastKey());
        assertTrue(sub.containsKey("banana"));
        assertTrue(sub.containsKey("cherry"));
        assertFalse(sub.containsKey("apple"));
        assertFalse(sub.containsKey("date"));

        // HeadMap (< "cherry")
        final SortedMap<String, Integer> head = trie.headMap("cherry");
        assertEquals(2, head.size());
        assertEquals("apple", head.firstKey());
        assertEquals("banana", head.lastKey());

        // TailMap (>= "cherry")
        final SortedMap<String, Integer> tail = trie.tailMap("cherry");
        assertEquals(3, tail.size());
        assertEquals("cherry", tail.firstKey());
        assertEquals("fig", tail.lastKey());
    }

    @Test(timeout = 4000)
    public void testPrefixMapSubtreeBoundary() {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        trie.put("xy", "val1");
        trie.put("xyz", "val2");
        trie.put("xyza", "val3");
        trie.put("other", "val4");

        final SortedMap<String, String> prefix = trie.prefixMap("xyz");
        assertEquals(2, prefix.size());
        assertEquals("xyz", prefix.firstKey());
        assertEquals("xyza", prefix.lastKey());

        // Key not matching prefix
        assertFalse(prefix.containsKey("xy"));
        assertFalse(prefix.containsKey("other"));

        // Non-existent prefix
        final SortedMap<String, String> emptyPrefix = trie.prefixMap("none");
        assertTrue(emptyPrefix.isEmpty());
        assertEquals(0, emptyPrefix.size());
    }

    @Test(timeout = 4000)
    public void testTrieEntryDirectContract() {
        final AbstractPatriciaTrie.TrieEntry<String, String> entry =
                new AbstractPatriciaTrie.TrieEntry<String, String>("testKey", "testVal", 5);

        assertFalse(entry.isEmpty());
        assertTrue(entry.isExternalNode());
        assertFalse(entry.isInternalNode());

        final String str = entry.toString();
        assertNotNull(str);
        assertTrue(str.contains("testKey"));
        assertTrue(str.contains("testVal"));

        final AbstractPatriciaTrie.TrieEntry<String, String> rootEntry =
                new AbstractPatriciaTrie.TrieEntry<String, String>(null, null, -1);
        assertTrue(rootEntry.isEmpty());
        assertTrue(rootEntry.toString().contains("RootEntry"));
    }

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        final PatriciaTrie<String> trie = new PatriciaTrie<String>();
        trie.put("ser1", "val1");
        trie.put("ser2", "val2");
        trie.put("", "root");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(trie);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        @SuppressWarnings("unchecked")
        final PatriciaTrie<String> deserialized = (PatriciaTrie<String>) ois.readObject();
        ois.close();

        assertEquals(trie.size(), deserialized.size());
        assertEquals("val1", deserialized.get("ser1"));
        assertEquals("val2", deserialized.get("ser2"));
        assertEquals("root", deserialized.get(""));
        assertEquals(trie.keySet(), deserialized.keySet());
    }
}