package org.apache.commons.collections4.trie;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.collections4.trie.UnmodifiableTrie
 * Defect Reference: Defects4J / Collections-Decorators (COLLECTIONS-525 / Trie decoration)
 * 
 * Branches & Conditions Targeted:
 * 1. Factory Method (unmodifiableTrie):
 *    - Branch: trie == null -> throws IllegalArgumentException
 *    - Branch: trie instanceof Unmodifiable -> return trie (Defect: Re-wrapping existing Unmodifiable instance)
 *    - Branch: trie not unmodifiable -> return new UnmodifiableTrie<K, V>(trie)
 * 2. Constructor:
 *    - Branch: trie == null -> throws IllegalArgumentException
 *    - Branch: trie != null -> assigns delegate
 * 3. Mutating Operations (UnsupportedOperationException defensive guards):
 *    - clear(), put(K, V), putAll(Map), remove(Object) -> all must throw UOE unconditionally
 * 4. Views & Delegations:
 *    - entrySet(), keySet(), values() -> return unmodifiable decorated collections
 *    - headMap(), subMap(), tailMap(), prefixMap() -> return unmodifiable decorated sorted maps
 *    - mapIterator() -> returns UnmodifiableOrderedMapIterator
 *    - comparator(), firstKey(), lastKey(), nextKey(), previousKey() -> delegated properly
 *    - containsKey(), containsValue(), get(), isEmpty(), size() -> delegated properly
 * 5. Lifecycle & Contracts:
 *    - equals(), hashCode(), toString() -> delegated accurately
 *    - Java Serialization -> roundtrip preserves state and unmodifiable contract
 * -----------------------------------------------------------------------------------------
 */

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.Unmodifiable;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UnmodifiableTrieGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCoreDelegationReads() {
        final Trie<String, String> base = new PatriciaTrie<String>();
        base.put("alpha", "A");
        base.put("bravo", "B");
        base.put("charlie", "C");

        final Trie<String, String> unmod = UnmodifiableTrie.unmodifiableTrie(base);

        assertEquals(3, unmod.size());
        assertFalse(unmod.isEmpty());
        assertTrue(unmod.containsKey("alpha"));
        assertFalse(unmod.containsKey("delta"));
        assertTrue(unmod.containsValue("B"));
        assertFalse(unmod.containsValue("Z"));
        assertEquals("A", unmod.get("alpha"));
        assertNull(unmod.get("missing"));

        assertEquals("alpha", unmod.firstKey());
        assertEquals("charlie", unmod.lastKey());
        assertEquals("bravo", unmod.nextKey("alpha"));
        assertEquals("bravo", unmod.previousKey("charlie"));
        assertNull(unmod.previousKey("alpha"));
        assertNull(unmod.nextKey("charlie"));
        assertNull(unmod.comparator());
    }

    @Test(timeout = 4000)
    public void testMapIteratorTraversal() {
        final Trie<String, Integer> base = new PatriciaTrie<Integer>();
        base.put("one", 1);
        base.put("two", 2);

        final Trie<String, Integer> unmod = UnmodifiableTrie.unmodifiableTrie(base);
        final OrderedMapIterator<String, Integer> it = unmod.mapIterator();

        assertNotNull(it);
        assertTrue(it.hasNext());
        final String firstKey = it.next();
        assertNotNull(firstKey);
        assertNotNull(it.getValue());
        assertTrue(it.hasPrevious());
        assertEquals(firstKey, it.previous());
    }

    @Test(timeout = 4000)
    public void testSubMapAndPrefixMapViews() {
        final Trie<String, String> base = new PatriciaTrie<String>();
        base.put("apple", "1");
        base.put("application", "2");
        base.put("banana", "3");

        final Trie<String, String> unmod = UnmodifiableTrie.unmodifiableTrie(base);

        final SortedMap<String, String> prefix = unmod.prefixMap("app");
        assertEquals(2, prefix.size());
        assertTrue(prefix.containsKey("apple"));
        assertTrue(prefix.containsKey("application"));

        final SortedMap<String, String> head = unmod.headMap("banana");
        assertEquals(2, head.size());

        final SortedMap<String, String> tail = unmod.tailMap("banana");
        assertEquals(1, tail.size());
        assertTrue(tail.containsKey("banana"));

        final SortedMap<String, String> sub = unmod.subMap("apple", "banana");
        assertEquals(2, sub.size());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyTrieOperations() {
        final Trie<String, String> base = new PatriciaTrie<String>();
        final Trie<String, String> unmod = new UnmodifiableTrie<String, String>(base);

        assertEquals(0, unmod.size());
        assertTrue(unmod.isEmpty());
        assertFalse(unmod.containsKey(""));
        assertFalse(unmod.containsValue(""));
        assertNull(unmod.get(""));
        assertTrue(unmod.keySet().isEmpty());
        assertTrue(unmod.values().isEmpty());
        assertTrue(unmod.entrySet().isEmpty());
    }

    @Test(timeout = 4000)
    public void testViewsAreUnmodifiable() {
        final Trie<String, String> base = new PatriciaTrie<String>();
        base.put("k1", "v1");
        final Trie<String, String> unmod = UnmodifiableTrie.unmodifiableTrie(base);

        final Set<String> keys = unmod.keySet();
        try {
            keys.clear();
            fail("keySet() must be unmodifiable");
        } catch (final UnsupportedOperationException ignored) {}

        final Set<Map.Entry<String, String>> entries = unmod.entrySet();
        try {
            entries.clear();
            fail("entrySet() must be unmodifiable");
        } catch (final UnsupportedOperationException ignored) {}

        try {
            unmod.values().clear();
            fail("values() must be unmodifiable");
        } catch (final UnsupportedOperationException ignored) {}

        try {
            unmod.prefixMap("k").clear();
            fail("prefixMap() view must be unmodifiable");
        } catch (final UnsupportedOperationException ignored) {}

        try {
            unmod.headMap("k2").clear();
            fail("headMap() view must be unmodifiable");
        } catch (final UnsupportedOperationException ignored) {}

        try {
            unmod.tailMap("k1").clear();
            fail("tailMap() view must be unmodifiable");
        } catch (final UnsupportedOperationException ignored) {}

        try {
            unmod.subMap("k0", "k2").clear();
            fail("subMap() view must be unmodifiable");
        } catch (final UnsupportedOperationException ignored) {}
    }

    @Test(timeout = 4000)
    public void testMapIteratorIsUnmodifiable() {
        final Trie<String, String> base = new PatriciaTrie<String>();
        base.put("k1", "v1");
        final Trie<String, String> unmod = UnmodifiableTrie.unmodifiableTrie(base);
        final OrderedMapIterator<String, String> it = unmod.mapIterator();
        it.next();

        try {
            it.remove();
            fail("OrderedMapIterator must not support remove()");
        } catch (final UnsupportedOperationException ignored) {}

        try {
            it.setValue("newVal");
            fail("OrderedMapIterator must not support setValue()");
        } catch (final UnsupportedOperationException ignored) {}
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defect Verification)
    // =========================================================================

    /**
     * Targets Defects4J flaw where UnmodifiableTrie.unmodifiableTrie() failed to check
     * if the given trie is already an Unmodifiable instance and re-wrapped it instead
     * of returning it directly (violating unmodifiable decorator identity contract).
     */
    @Test(timeout = 4000)
    public void testDecorateFactoryDoesNotDoubleWrap() {
        final Trie<String, String> base = new PatriciaTrie<String>();
        base.put("key", "val");

        final Trie<String, String> unmod1 = UnmodifiableTrie.unmodifiableTrie(base);
        assertTrue(unmod1 instanceof Unmodifiable);

        final Trie<String, String> unmod2 = UnmodifiableTrie.unmodifiableTrie(unmod1);
        assertSame("UnmodifiableTrie shall not be decorated twice", unmod1, unmod2);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFactoryNullTrieThrowsIllegalArgumentException() {
        UnmodifiableTrie.unmodifiableTrie(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullTrieThrowsIllegalArgumentException() {
        new UnmodifiableTrie<String, String>(null);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testClearThrowsUnsupportedOperationException() {
        final Trie<String, String> unmod = UnmodifiableTrie.unmodifiableTrie(new PatriciaTrie<String>());
        unmod.clear();
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testPutThrowsUnsupportedOperationException() {
        final Trie<String, String> unmod = UnmodifiableTrie.unmodifiableTrie(new PatriciaTrie<String>());
        unmod.put("k", "v");
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testPutAllThrowsUnsupportedOperationException() {
        final Trie<String, String> unmod = UnmodifiableTrie.unmodifiableTrie(new PatriciaTrie<String>());
        unmod.putAll(Collections.singletonMap("k", "v"));
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testRemoveThrowsUnsupportedOperationException() {
        final Trie<String, String> base = new PatriciaTrie<String>();
        base.put("k", "v");
        final Trie<String, String> unmod = UnmodifiableTrie.unmodifiableTrie(base);
        unmod.remove("k");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeAndToStringContract() {
        final Trie<String, String> base1 = new PatriciaTrie<String>();
        base1.put("key1", "value1");

        final Trie<String, String> base2 = new PatriciaTrie<String>();
        base2.put("key1", "value1");

        final Trie<String, String> unmod1 = UnmodifiableTrie.unmodifiableTrie(base1);
        final Trie<String, String> unmod2 = UnmodifiableTrie.unmodifiableTrie(base2);

        assertEquals(unmod1, unmod1);
        assertEquals(unmod1, unmod2);
        assertEquals(unmod1, base1);
        assertEquals(base1, unmod1);

        assertEquals(base1.hashCode(), unmod1.hashCode());
        assertEquals(unmod1.hashCode(), unmod2.hashCode());
        assertEquals(base1.toString(), unmod1.toString());

        assertFalse(unmod1.equals(null));
        assertFalse(unmod1.equals("Some other type"));

        final Map<String, String> normalMap = new HashMap<String, String>();
        normalMap.put("key1", "value1");
        assertEquals(unmod1, normalMap);
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testSerializationIntegrity() throws Exception {
        final Trie<String, String> base = new PatriciaTrie<String>();
        base.put("serKey", "serValue");
        final Trie<String, String> unmod = UnmodifiableTrie.unmodifiableTrie(base);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(unmod);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final Trie<String, String> deserialized = (Trie<String, String>) ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertEquals(unmod, deserialized);
        assertEquals("serValue", deserialized.get("serKey"));
        try {
            deserialized.put("newKey", "newValue");
            fail("Deserialized trie must remain unmodifiable");
        } catch (final UnsupportedOperationException ignored) {}
    }
}