package org.apache.commons.collections4.trie;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: UnmodifiableTrie<K,V> - decorator that wraps a Trie and prevents modifications.
 * 
 * Defect: The factory method unmodifiableTrie(Trie) and constructor do not properly handle
 *         the case where the delegate is already an UnmodifiableTrie. The known defect
 *         (from Defects4J) shows that decorating an already-unmodifiable trie results in
 *         a double-wrapped object, causing the toString() and equality checks to fail
 *         (e.g., "UnmodifiableTrie shall not be decorated").
 * 
 * Branch Analysis:
 * 1. Constructor null check: trie == null -> throw IllegalArgumentException
 * 2. Factory method: delegates to constructor
 * 3. All read methods delegate to underlying trie (containsKey, get, size, etc.)
 * 4. All write methods throw UnsupportedOperationException (clear, put, putAll, remove)
 * 5. View methods return unmodifiable wrappers (entrySet, keySet, values, headMap, etc.)
 * 6. mapIterator() returns unmodifiable iterator
 * 7. equals/hashCode/toString delegate to underlying trie
 * 
 * Boundary Conditions:
 * - null trie passed to constructor/factory
 * - empty trie
 * - trie with single element
 * - trie with multiple elements
 * - null keys/values
 * - boundary keys (empty string, single char, long strings)
 * - prefixMap with various prefixes
 * - headMap/tailMap/subMap boundaries
 * 
 * Defect-Targeted Tests:
 * - testDecorateFactory_AlreadyUnmodifiable: Verifies that decorating an already
 *   unmodifiable trie returns the same instance (no double wrapping)
 * - testUnmodifiableTrie_NotDoubleWrapped: Verifies toString() and equality
 *   are not affected by double wrapping
 */
public class UnmodifiableTrieDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testConstructor_NullTrie_ThrowsIllegalArgumentException() {
        try {
            new UnmodifiableTrie<String, String>(null);
            fail("Expected IllegalArgumentException for null trie");
        } catch (IllegalArgumentException e) {
            assertEquals("Trie must not be null", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testFactory_NullTrie_ThrowsIllegalArgumentException() {
        try {
            UnmodifiableTrie.unmodifiableTrie(null);
            fail("Expected IllegalArgumentException for null trie");
        } catch (IllegalArgumentException e) {
            assertEquals("Trie must not be null", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSize_EmptyTrie_ReturnsZero() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertEquals(0, trie.size());
    }

    @Test(timeout = 4000)
    public void testSize_NonEmptyTrie_ReturnsCorrectCount() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        delegate.put("key2", "value2");
        delegate.put("key3", "value3");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertEquals(3, trie.size());
    }

    @Test(timeout = 4000)
    public void testIsEmpty_EmptyTrie_ReturnsTrue() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertTrue(trie.isEmpty());
    }

    @Test(timeout = 4000)
    public void testIsEmpty_NonEmptyTrie_ReturnsFalse() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key", "value");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertFalse(trie.isEmpty());
    }

    @Test(timeout = 4000)
    public void testContainsKey_ExistingKey_ReturnsTrue() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key", "value");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertTrue(trie.containsKey("key"));
    }

    @Test(timeout = 4000)
    public void testContainsKey_NonExistingKey_ReturnsFalse() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key", "value");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertFalse(trie.containsKey("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testContainsKey_NullKey_ReturnsFalse() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key", "value");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertFalse(trie.containsKey(null));
    }

    @Test(timeout = 4000)
    public void testContainsValue_ExistingValue_ReturnsTrue() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key", "value");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertTrue(trie.containsValue("value"));
    }

    @Test(timeout = 4000)
    public void testContainsValue_NonExistingValue_ReturnsFalse() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key", "value");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertFalse(trie.containsValue("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGet_ExistingKey_ReturnsValue() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key", "value");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertEquals("value", trie.get("key"));
    }

    @Test(timeout = 4000)
    public void testGet_NonExistingKey_ReturnsNull() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key", "value");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertNull(trie.get("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGet_NullKey_ReturnsNull() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key", "value");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertNull(trie.get(null));
    }

    @Test(timeout = 4000)
    public void testFirstKey_NonEmptyTrie_ReturnsFirstKey() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        delegate.put("banana", "2");
        delegate.put("cherry", "3");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertEquals("apple", trie.firstKey());
    }

    @Test(timeout = 4000)
    public void testLastKey_NonEmptyTrie_ReturnsLastKey() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        delegate.put("banana", "2");
        delegate.put("cherry", "3");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertEquals("cherry", trie.lastKey());
    }

    @Test(timeout = 4000)
    public void testNextKey_ExistingKey_ReturnsNextKey() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        delegate.put("banana", "2");
        delegate.put("cherry", "3");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertEquals("banana", trie.nextKey("apple"));
    }

    @Test(timeout = 4000)
    public void testNextKey_LastKey_ReturnsNull() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        delegate.put("banana", "2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertNull(trie.nextKey("banana"));
    }

    @Test(timeout = 4000)
    public void testPreviousKey_ExistingKey_ReturnsPreviousKey() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        delegate.put("banana", "2");
        delegate.put("cherry", "3");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertEquals("apple", trie.previousKey("banana"));
    }

    @Test(timeout = 4000)
    public void testPreviousKey_FirstKey_ReturnsNull() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        delegate.put("banana", "2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertNull(trie.previousKey("apple"));
    }

    @Test(timeout = 4000)
    public void testComparator_NaturalOrdering_ReturnsNull() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertNull(trie.comparator());
    }

    @Test(timeout = 4000)
    public void testComparator_CustomComparator_ReturnsComparator() {
        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.compareTo(o1); // reverse order
            }
        };
        Trie<String, String> delegate = new PatriciaTrie<String, String>(comparator);
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertSame(comparator, trie.comparator());
    }

    // ==================== Partition B: Boundary Value Analysis (BVA) & Extremes ====================

    @Test(timeout = 4000)
    public void testGet_EmptyStringKey_ReturnsValue() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("", "empty");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertEquals("empty", trie.get(""));
    }

    @Test(timeout = 4000)
    public void testGet_SingleCharacterKey_ReturnsValue() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("a", "single");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertEquals("single", trie.get("a"));
    }

    @Test(timeout = 4000)
    public void testGet_LongStringKey_ReturnsValue() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        String longKey = "averylongkeythatgoesonandonandonandonandonandonandon";
        delegate.put(longKey, "long");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertEquals("long", trie.get(longKey));
    }

    @Test(timeout = 4000)
    public void testContainsValue_NullValue_ReturnsTrue() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key", null);
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertTrue(trie.containsValue(null));
    }

    @Test(timeout = 4000)
    public void testEntrySet_EmptyTrie_ReturnsEmptySet() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        Set<Map.Entry<String, String>> entries = trie.entrySet();
        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }

    @Test(timeout = 4000)
    public void testEntrySet_NonEmptyTrie_ReturnsUnmodifiableSet() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        delegate.put("key2", "value2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        Set<Map.Entry<String, String>> entries = trie.entrySet();
        assertEquals(2, entries.size());
        try {
            entries.clear();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testKeySet_EmptyTrie_ReturnsEmptySet() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        Set<String> keys = trie.keySet();
        assertNotNull(keys);
        assertTrue(keys.isEmpty());
    }

    @Test(timeout = 4000)
    public void testKeySet_NonEmptyTrie_ReturnsUnmodifiableSet() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        delegate.put("key2", "value2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        Set<String> keys = trie.keySet();
        assertEquals(2, keys.size());
        try {
            keys.add("newkey");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testValues_EmptyTrie_ReturnsEmptyCollection() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        Collection<String> values = trie.values();
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    @Test(timeout = 4000)
    public void testValues_NonEmptyTrie_ReturnsUnmodifiableCollection() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        delegate.put("key2", "value2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        Collection<String> values = trie.values();
        assertEquals(2, values.size());
        try {
            values.clear();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testHeadMap_WithBoundaryKey_ReturnsUnmodifiableSortedMap() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        delegate.put("banana", "2");
        delegate.put("cherry", "3");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        SortedMap<String, String> headMap = trie.headMap("banana");
        assertNotNull(headMap);
        assertEquals(1, headMap.size());
        assertTrue(headMap.containsKey("apple"));
        try {
            headMap.put("newkey", "newvalue");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testTailMap_WithBoundaryKey_ReturnsUnmodifiableSortedMap() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        delegate.put("banana", "2");
        delegate.put("cherry", "3");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        SortedMap<String, String> tailMap = trie.tailMap("banana");
        assertNotNull(tailMap);
        assertEquals(2, tailMap.size());
        assertTrue(tailMap.containsKey("banana"));
        assertTrue(tailMap.containsKey("cherry"));
        try {
            tailMap.put("newkey", "newvalue");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSubMap_WithBoundaryKeys_ReturnsUnmodifiableSortedMap() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        delegate.put("banana", "2");
        delegate.put("cherry", "3");
        delegate.put("date", "4");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        SortedMap<String, String> subMap = trie.subMap("banana", "date");
        assertNotNull(subMap);
        assertEquals(2, subMap.size());
        assertTrue(subMap.containsKey("banana"));
        assertTrue(subMap.containsKey("cherry"));
        try {
            subMap.put("newkey", "newvalue");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPrefixMap_WithPrefix_ReturnsUnmodifiableSortedMap() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        delegate.put("appetizer", "2");
        delegate.put("banana", "3");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        SortedMap<String, String> prefixMap = trie.prefixMap("app");
        assertNotNull(prefixMap);
        assertEquals(2, prefixMap.size());
        assertTrue(prefixMap.containsKey("apple"));
        assertTrue(prefixMap.containsKey("appetizer"));
        try {
            prefixMap.put("newkey", "newvalue");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPrefixMap_EmptyPrefix_ReturnsAllEntries() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        delegate.put("banana", "2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        SortedMap<String, String> prefixMap = trie.prefixMap("");
        assertEquals(2, prefixMap.size());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testDecorateFactory_AlreadyUnmodifiable_ReturnsSameInstance() {
        // This test targets the known defect: decorating an already-unmodifiable trie
        // should return the same instance, not a double-wrapped one
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        delegate.put("key2", "value2");
        
        UnmodifiableTrie<String, String> first = UnmodifiableTrie.unmodifiableTrie(delegate);
        UnmodifiableTrie<String, String> second = UnmodifiableTrie.unmodifiableTrie(first);
        
        // The defect causes double wrapping, so the toString() would show nested "UnmodifiableTrie"
        // The correct behavior is to return the same instance
        assertSame("Should return the same instance when decorating an unmodifiable trie", 
                   first, second);
    }

    @Test(timeout = 4000)
    public void testUnmodifiableTrie_NotDoubleWrapped_ToStringIsCorrect() {
        // This test targets the known defect from TrieUtilsTest
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        
        UnmodifiableTrie<String, String> trie = UnmodifiableTrie.unmodifiableTrie(delegate);
        UnmodifiableTrie<String, String> doubleWrapped = UnmodifiableTrie.unmodifiableTrie(trie);
        
        // The toString should not show double wrapping
        String toString = doubleWrapped.toString();
        assertFalse("toString should not contain nested UnmodifiableTrie markers", 
                    toString.contains("UnmodifiableTrie[UnmodifiableTrie"));
        assertTrue("toString should contain the delegate's content", 
                   toString.contains("key1"));
    }

    @Test(timeout = 4000)
    public void testUnmodifiableTrie_NotDoubleWrapped_EqualsIsCorrect() {
        // This test targets the known defect from UnmodifiableTrieTest
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        delegate.put("key2", "value2");
        
        UnmodifiableTrie<String, String> trie1 = UnmodifiableTrie.unmodifiableTrie(delegate);
        UnmodifiableTrie<String, String> trie2 = UnmodifiableTrie.unmodifiableTrie(trie1);
        
        // Both should be equal to the original delegate
        assertEquals("Double-wrapped trie should equal the original delegate", 
                     delegate, trie2);
        assertEquals("Double-wrapped trie should equal the single-wrapped trie", 
                     trie1, trie2);
    }

    @Test(timeout = 4000)
    public void testUnmodifiableTrie_NotDoubleWrapped_HashCodeIsCorrect() {
        // Hash code should be consistent with the delegate
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        
        UnmodifiableTrie<String, String> trie = UnmodifiableTrie.unmodifiableTrie(delegate);
        UnmodifiableTrie<String, String> doubleWrapped = UnmodifiableTrie.unmodifiableTrie(trie);
        
        assertEquals("Hash code should match the delegate", 
                     delegate.hashCode(), doubleWrapped.hashCode());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testClear_ThrowsUnsupportedOperationException() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key", "value");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        try {
            trie.clear();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPut_ThrowsUnsupportedOperationException() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        try {
            trie.put("key", "value");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPutAll_ThrowsUnsupportedOperationException() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        try {
            trie.putAll(map);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRemove_ThrowsUnsupportedOperationException() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key", "value");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        try {
            trie.remove("key");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMapIterator_ReturnsUnmodifiableIterator() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        delegate.put("key2", "value2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        OrderedMapIterator<String, String> it = trie.mapIterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        it.next();
        try {
            it.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMapIterator_EmptyTrie_HasNoNext() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        OrderedMapIterator<String, String> it = trie.mapIterator();
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEquals_SameInstance_ReturnsTrue() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key", "value");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertTrue(trie.equals(trie));
    }

    @Test(timeout = 4000)
    public void testEquals_EqualDelegates_ReturnsTrue() {
        Trie<String, String> delegate1 = new PatriciaTrie<String, String>();
        delegate1.put("key", "value");
        Trie<String, String> delegate2 = new PatriciaTrie<String, String>();
        delegate2.put("key", "value");
        UnmodifiableTrie<String, String> trie1 = new UnmodifiableTrie<String, String>(delegate1);
        UnmodifiableTrie<String, String> trie2 = new UnmodifiableTrie<String, String>(delegate2);
        assertTrue(trie1.equals(trie2));
    }

    @Test(timeout = 4000)
    public void testEquals_DifferentDelegates_ReturnsFalse() {
        Trie<String, String> delegate1 = new PatriciaTrie<String, String>();
        delegate1.put("key1", "value1");
        Trie<String, String> delegate2 = new PatriciaTrie<String, String>();
        delegate2.put("key2", "value2");
        UnmodifiableTrie<String, String> trie1 = new UnmodifiableTrie<String, String>(delegate1);
        UnmodifiableTrie<String, String> trie2 = new UnmodifiableTrie<String, String>(delegate2);
        assertFalse(trie1.equals(trie2));
    }

    @Test(timeout = 4000)
    public void testEquals_NullObject_ReturnsFalse() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key", "value");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertFalse(trie.equals(null));
    }

    @Test(timeout = 4000)
    public void testEquals_DifferentType_ReturnsFalse() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key", "value");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertFalse(trie.equals("not a trie"));
    }

    @Test(timeout = 4000)
    public void testHashCode_MatchesDelegate() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        delegate.put("key2", "value2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertEquals(delegate.hashCode(), trie.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCode_EmptyTrie_MatchesDelegate() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertEquals(delegate.hashCode(), trie.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString_MatchesDelegate() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        delegate.put("key2", "value2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertEquals(delegate.toString(), trie.toString());
    }

    @Test(timeout = 4000)
    public void testToString_EmptyTrie_MatchesDelegate() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertEquals(delegate.toString(), trie.toString());
    }

    @Test(timeout = 4000)
    public void testSerializable_ImplementsInterface() {
        // Verify the class implements Serializable
        assertTrue("UnmodifiableTrie should implement Serializable", 
                   Serializable.class.isAssignableFrom(UnmodifiableTrie.class));
    }

    @Test(timeout = 4000)
    public void testUnmodifiable_ImplementsInterface() {
        // Verify the class implements Unmodifiable
        assertTrue("UnmodifiableTrie should implement Unmodifiable", 
                   org.apache.commons.collections4.Unmodifiable.class.isAssignableFrom(UnmodifiableTrie.class));
    }

    @Test(timeout = 4000)
    public void testTrie_ImplementsInterface() {
        // Verify the class implements Trie
        assertTrue("UnmodifiableTrie should implement Trie", 
                   Trie.class.isAssignableFrom(UnmodifiableTrie.class));
    }

    @Test(timeout = 4000)
    public void testMapIterator_AfterNext_GetKeyAndValue() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        delegate.put("key2", "value2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        OrderedMapIterator<String, String> it = trie.mapIterator();
        assertTrue(it.hasNext());
        String key = it.next();
        assertNotNull(key);
        assertNotNull(it.getValue());
        assertTrue(it.hasNext());
        it.next();
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testMapIterator_SetValue_ThrowsUnsupportedOperationException() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        OrderedMapIterator<String, String> it = trie.mapIterator();
        it.next();
        try {
            it.setValue("newValue");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEntrySet_ContainsCorrectEntries() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        delegate.put("key2", "value2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        Set<Map.Entry<String, String>> entries = trie.entrySet();
        boolean foundKey1 = false;
        boolean foundKey2 = false;
        for (Map.Entry<String, String> entry : entries) {
            if ("key1".equals(entry.getKey()) && "value1".equals(entry.getValue())) {
                foundKey1 = true;
            }
            if ("key2".equals(entry.getKey()) && "value2".equals(entry.getValue())) {
                foundKey2 = true;
            }
        }
        assertTrue("Should contain key1=value1", foundKey1);
        assertTrue("Should contain key2=value2", foundKey2);
    }

    @Test(timeout = 4000)
    public void testKeySet_ContainsCorrectKeys() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        delegate.put("key2", "value2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        Set<String> keys = trie.keySet();
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
        assertEquals(2, keys.size());
    }

    @Test(timeout = 4000)
    public void testValues_ContainsCorrectValues() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("key1", "value1");
        delegate.put("key2", "value2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        Collection<String> values = trie.values();
        assertTrue(values.contains("value1"));
        assertTrue(values.contains("value2"));
        assertEquals(2, values.size());
    }

    @Test(timeout = 4000)
    public void testHeadMap_EmptyResult_ReturnsEmptyMap() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        SortedMap<String, String> headMap = trie.headMap("apple");
        assertNotNull(headMap);
        assertTrue(headMap.isEmpty());
    }

    @Test(timeout = 4000)
    public void testTailMap_EmptyResult_ReturnsEmptyMap() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        SortedMap<String, String> tailMap = trie.tailMap("zebra");
        assertNotNull(tailMap);
        assertTrue(tailMap.isEmpty());
    }

    @Test(timeout = 4000)
    public void testSubMap_EmptyResult_ReturnsEmptyMap() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        delegate.put("banana", "2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        SortedMap<String, String> subMap = trie.subMap("cherry", "date");
        assertNotNull(subMap);
        assertTrue(subMap.isEmpty());
    }

    @Test(timeout = 4000)
    public void testPrefixMap_NoMatch_ReturnsEmptyMap() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        delegate.put("banana", "2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        SortedMap<String, String> prefixMap = trie.prefixMap("xyz");
        assertNotNull(prefixMap);
        assertTrue(prefixMap.isEmpty());
    }

    @Test(timeout = 4000)
    public void testNextKey_NonExistentKey_ReturnsNull() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        delegate.put("banana", "2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertNull(trie.nextKey("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testPreviousKey_NonExistentKey_ReturnsNull() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        delegate.put("apple", "1");
        delegate.put("banana", "2");
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        assertNull(trie.previousKey("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testFirstKey_EmptyTrie_ThrowsNoSuchElementException() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        try {
            trie.firstKey();
            fail("Expected NoSuchElementException");
        } catch (java.util.NoSuchElementException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testLastKey_EmptyTrie_ThrowsNoSuchElementException() {
        Trie<String, String> delegate = new PatriciaTrie<String, String>();
        UnmodifiableTrie<String, String> trie = new UnmodifiableTrie<String, String>(delegate);
        try {
            trie.lastKey();
            fail("Expected NoSuchElementException");
        } catch (java.util.NoSuchElementException e) {
            // expected
        }
    }
}