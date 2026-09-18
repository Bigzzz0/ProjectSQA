/*
 *  Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.commons.collections.map;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.ResettableIterator;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Decision Branches & Conditions:
 * 1. Flat mode vs Delegate mode transitions (0 -> 1 -> 2 -> 3 -> delegate mode at 4; clear resets back to flat).
 * 2. Flat mode key/value lookup (get, containsKey, containsValue) with null and non-null keys/values across sizes 0, 1, 2, 3.
 * 3. Fall-through in removal: removing key1, key2, or key3 for both null and non-null keys when size is 1, 2, or 3.
 * 4. putAll branching: empty map, size < 4 while flat, size >= 4 forcing transition, and putAll while already in delegate mode.
 * 5. Iterators (FlatMapIterator, EntrySetIterator, KeySetIterator, ValuesIterator):
 *    - hasNext, next, getKey, getValue, setValue, remove, reset, toString, equals, hashCode.
 *    - Invalid state handling: calling remove/getKey/getValue/setValue before next() throws IllegalStateException.
 *    - Calling next() past end throws NoSuchElementException.
 * 6. Object Integrity & Contract: equals (reflexive, symmetric with nulls, mismatch keys/values, delegate vs flat),
 *    hashCode calculation, serialization/deserialization (both <=3 items and >3 items), and clone().
 * 7. Self-referencing keys/values in toString(): verifies "(this Map)" rendering.
 *
 * Targeted Known Defect (Defects4J):
 * - FlatMapIterator.setValue(value) and EntrySetIterator.setValue(value) missing break statements inside
 *   their switch(nextIndex) blocks. When setting value at nextIndex == 2 or 3, execution falls through
 *   and overwrites earlier values (value1, value2) unexpectedly.
 */
public class Flat3MapGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Missing breaks in setValue)
    // =========================================================================

    @Test(timeout = 4000)
    public void testMapIteratorSetValue2() {
        Flat3Map map = new Flat3Map();
        map.put("key1", "val1");
        map.put("key2", "val2");

        MapIterator it = map.mapIterator();
        assertEquals("key1", it.next());
        assertEquals("key2", it.next());

        // Target bug: switch (nextIndex) { case 2: ... case 1: ... } falls through to case 1
        it.setValue("newVal2");

        assertEquals("newVal2", map.get("key2"));
        assertEquals("val1", map.get("key1")); // Exposes defect if case 2 falls through to case 1
    }

    @Test(timeout = 4000)
    public void testMapIteratorSetValue3() {
        Flat3Map map = new Flat3Map();
        map.put("key1", "val1");
        map.put("key2", "val2");
        map.put("key3", "val3");

        MapIterator it = map.mapIterator();
        assertEquals("key1", it.next());
        assertEquals("key2", it.next());
        assertEquals("key3", it.next());

        // Target bug: switch (nextIndex) { case 3: ... case 2: ... case 1: ... }
        it.setValue("newVal3");

        assertEquals("newVal3", map.get("key3"));
        assertEquals("val2", map.get("key2")); // Exposes defect if case 3 falls through
        assertEquals("val1", map.get("key1"));
    }

    @Test(timeout = 4000)
    public void testEntryIteratorSetValue2() {
        Flat3Map map = new Flat3Map();
        map.put("key1", "val1");
        map.put("key2", "val2");

        Iterator it = map.entrySet().iterator();
        Map.Entry entry1 = (Map.Entry) it.next();
        Map.Entry entry2 = (Map.Entry) it.next();

        entry2.setValue("newVal2");

        assertEquals("newVal2", map.get("key2"));
        assertEquals("val1", map.get("key1")); // Exposes defect if entry iterator falls through
    }

    @Test(timeout = 4000)
    public void testEntryIteratorSetValue3() {
        Flat3Map map = new Flat3Map();
        map.put("key1", "val1");
        map.put("key2", "val2");
        map.put("key3", "val3");

        Iterator it = map.entrySet().iterator();
        it.next();
        it.next();
        Map.Entry entry3 = (Map.Entry) it.next();

        entry3.setValue("newVal3");

        assertEquals("newVal3", map.get("key3"));
        assertEquals("val2", map.get("key2")); // Exposes defect if entry iterator falls through
        assertEquals("val1", map.get("key1"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFlatToDelegateTransitionAndClear() {
        Flat3Map map = new Flat3Map();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());

        assertNull(map.put("A", "1"));
        assertEquals(1, map.size());
        assertFalse(map.isEmpty());

        assertNull(map.put("B", "2"));
        assertEquals(2, map.size());

        assertNull(map.put("C", "3"));
        assertEquals(3, map.size());

        // 4th entry triggers transition to delegate mode
        assertNull(map.put("D", "4"));
        assertEquals(4, map.size());
        assertEquals("1", map.get("A"));
        assertEquals("2", map.get("B"));
        assertEquals("3", map.get("C"));
        assertEquals("4", map.get("D"));

        // Modifying in delegate mode
        assertEquals("1", map.put("A", "1-updated"));
        assertEquals("1-updated", map.get("A"));
        assertEquals(4, map.size());

        // Clearing should reset back to flat mode
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get("A"));

        // Put up to 1 item to verify flat mode is active again
        map.put("X", "10");
        assertEquals(1, map.size());
        assertEquals("10", map.get("X"));
    }

    @Test(timeout = 4000)
    public void testPutOverwriteExistingKeysFlatMode() {
        Flat3Map map = new Flat3Map();
        assertNull(map.put("k1", "v1"));
        assertEquals("v1", map.put("k1", "v1-new"));
        assertEquals("v1-new", map.get("k1"));
        assertEquals(1, map.size());

        assertNull(map.put("k2", "v2"));
        assertEquals("v2", map.put("k2", "v2-new"));
        assertEquals("v2-new", map.get("k2"));
        assertEquals(2, map.size());

        assertNull(map.put("k3", "v3"));
        assertEquals("v3", map.put("k3", "v3-new"));
        assertEquals("v3-new", map.get("k3"));
        assertEquals(3, map.size());

        // Overwrite key1 when size is 3
        assertEquals("v1-new", map.put("k1", "v1-final"));
        assertEquals("v1-final", map.get("k1"));
        assertEquals(3, map.size());

        // Overwrite key2 when size is 3
        assertEquals("v2-new", map.put("k2", "v2-final"));
        assertEquals("v2-final", map.get("k2"));
        assertEquals(3, map.size());
    }

    @Test(timeout = 4000)
    public void testRemoveNonDelegateAllSizes() {
        // Size 1 removal
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        assertEquals("v1", map.remove("k1"));
        assertEquals(0, map.size());
        assertNull(map.get("k1"));

        // Size 2 removal of key1 (tests shifting key2 into key1)
        map.clear();
        map.put("k1", "v1");
        map.put("k2", "v2");
        assertEquals("v1", map.remove("k1"));
        assertEquals(1, map.size());
        assertEquals("v2", map.get("k2"));
        assertNull(map.get("k1"));

        // Size 2 removal of key2
        map.clear();
        map.put("k1", "v1");
        map.put("k2", "v2");
        assertEquals("v2", map.remove("k2"));
        assertEquals(1, map.size());
        assertEquals("v1", map.get("k1"));
        assertNull(map.get("k2"));

        // Size 3 removal of key3
        map.clear();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        assertEquals("v3", map.remove("k3"));
        assertEquals(2, map.size());
        assertNull(map.get("k3"));
        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));

        // Size 3 removal of key2 (tests shifting key3 into key2)
        map.clear();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        assertEquals("v2", map.remove("k2"));
        assertEquals(2, map.size());
        assertNull(map.get("k2"));
        assertEquals("v1", map.get("k1"));
        assertEquals("v3", map.get("k3"));

        // Size 3 removal of key1 (tests shifting key3 into key1)
        map.clear();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        assertEquals("v1", map.remove("k1"));
        assertEquals(2, map.size());
        assertNull(map.get("k1"));
        assertEquals("v2", map.get("k2"));
        assertEquals("v3", map.get("k3"));
    }

    @Test(timeout = 4000)
    public void testRemoveNonExistentKeys() {
        Flat3Map map = new Flat3Map();
        assertNull(map.remove("absent"));

        map.put("a", "1");
        assertNull(map.remove("b"));

        map.put("b", "2");
        assertNull(map.remove("c"));

        map.put("c", "3");
        assertNull(map.remove("d"));

        // Delegate mode removal
        map.put("d", "4");
        assertNull(map.remove("e"));
        assertEquals("4", map.remove("d"));
        assertEquals(3, map.size());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Null Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullKeyOperations() {
        Flat3Map map = new Flat3Map();
        assertFalse(map.containsKey(null));
        assertNull(map.get(null));
        assertNull(map.remove(null));

        // Insert null key at size 0 -> key1
        map.put(null, "null-val1");
        assertEquals(1, map.size());
        assertTrue(map.containsKey(null));
        assertEquals("null-val1", map.get(null));

        // Overwrite null key at size 1
        assertEquals("null-val1", map.put(null, "null-val1-updated"));
        assertEquals("null-val1-updated", map.get(null));

        // Insert null key at size 1 (key1 != null) -> key2
        map.clear();
        map.put("k1", "v1");
        map.put(null, "null-val2");
        assertEquals(2, map.size());
        assertTrue(map.containsKey(null));
        assertEquals("null-val2", map.get(null));

        // Overwrite null key at size 2 (key2 is null)
        assertEquals("null-val2", map.put(null, "null-val2-updated"));
        assertEquals("null-val2-updated", map.get(null));

        // Overwrite null key at size 2 (key1 is null)
        map.clear();
        map.put(null, "null-val1");
        map.put("k2", "v2");
        assertEquals("null-val1", map.put(null, "null-val1-again"));

        // Insert null key at size 2 (key1, key2 != null) -> key3
        map.clear();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put(null, "null-val3");
        assertEquals(3, map.size());
        assertTrue(map.containsKey(null));
        assertEquals("null-val3", map.get(null));

        // Overwrite null key at size 3 (key3 is null)
        assertEquals("null-val3", map.put(null, "null-val3-updated"));
        assertEquals("null-val3-updated", map.get(null));

        // Overwrite null key at size 3 (key2 is null)
        map.clear();
        map.put("k1", "v1");
        map.put(null, "null-val2");
        map.put("k3", "v3");
        assertEquals("null-val2", map.put(null, "null-val2-overwritten"));

        // Overwrite null key at size 3 (key1 is null)
        map.clear();
        map.put(null, "null-val1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        assertEquals("null-val1", map.put(null, "null-val1-overwritten"));

        // Remove null key at size 3 when key3 is null
        map.clear();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put(null, "null-val3");
        assertEquals("null-val3", map.remove(null));
        assertEquals(2, map.size());
        assertFalse(map.containsKey(null));

        // Remove null key at size 3 when key2 is null
        map.clear();
        map.put("k1", "v1");
        map.put(null, "null-val2");
        map.put("k3", "v3");
        assertEquals("null-val2", map.remove(null));
        assertEquals(2, map.size());
        assertFalse(map.containsKey(null));

        // Remove null key at size 3 when key1 is null
        map.clear();
        map.put(null, "null-val1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        assertEquals("null-val1", map.remove(null));
        assertEquals(2, map.size());
        assertFalse(map.containsKey(null));

        // Remove null key at size 2 when key2 is null
        map.clear();
        map.put("k1", "v1");
        map.put(null, "null-val2");
        assertEquals("null-val2", map.remove(null));
        assertEquals(1, map.size());
        assertFalse(map.containsKey(null));

        // Remove null key at size 2 when key1 is null
        map.clear();
        map.put(null, "null-val1");
        map.put("k2", "v2");
        assertEquals("null-val1", map.remove(null));
        assertEquals(1, map.size());
        assertFalse(map.containsKey(null));

        // Remove null key at size 1 when key1 is null
        map.clear();
        map.put(null, "null-val1");
        assertEquals("null-val1", map.remove(null));
        assertEquals(0, map.size());
        assertFalse(map.containsKey(null));
    }

    @Test(timeout = 4000)
    public void testNullValuesHandling() {
        Flat3Map map = new Flat3Map();
        assertFalse(map.containsValue(null));

        map.put("k1", null);
        assertTrue(map.containsValue(null));
        assertNull(map.get("k1"));
        assertTrue(map.containsKey("k1"));

        map.put("k2", "v2");
        assertTrue(map.containsValue(null));
        assertTrue(map.containsValue("v2"));
        assertFalse(map.containsValue("v3"));

        map.put("k3", "v3");
        assertTrue(map.containsValue(null));
        assertTrue(map.containsValue("v2"));
        assertTrue(map.containsValue("v3"));

        // Put null at size 3 (value3 is null)
        map.put("k3", null);
        assertTrue(map.containsValue(null));

        // Transition to delegate with null values
        map.put("k4", "v4");
        assertTrue(map.containsValue(null));
        assertTrue(map.containsValue("v4"));
    }

    @Test(timeout = 4000)
    public void testPutAllBranches() {
        Flat3Map map = new Flat3Map();

        // 1. putAll empty map
        Map empty = new HashMap();
        map.putAll(empty);
        assertEquals(0, map.size());

        // 2. putAll small map (< 4 entries) into empty flat map
        Map small = new HashMap();
        small.put("A", "1");
        small.put("B", "2");
        map.putAll(small);
        assertEquals(2, map.size());
        assertEquals("1", map.get("A"));
        assertEquals("2", map.get("B"));

        // 3. putAll large map (>= 4 entries) directly converts to delegate map
        Flat3Map map2 = new Flat3Map();
        Map large = new HashMap();
        large.put("1", "one");
        large.put("2", "two");
        large.put("3", "three");
        large.put("4", "four");
        map2.putAll(large);
        assertEquals(4, map2.size());
        assertEquals("one", map2.get("1"));

        // 4. putAll when already in delegate mode
        map2.putAll(small);
        assertEquals(6, map2.size());
        assertEquals("1", map2.get("A"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorWithNullMapThrowsNPE() {
        new Flat3Map(null);
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testMapIteratorNoSuchElementException() {
        Flat3Map map = new Flat3Map();
        map.put("a", "b");
        MapIterator it = map.mapIterator();
        it.next();
        it.next(); // throws
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorRemoveBeforeNextThrows() {
        Flat3Map map = new Flat3Map();
        map.put("a", "b");
        MapIterator it = map.mapIterator();
        it.remove();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorDoubleRemoveThrows() {
        Flat3Map map = new Flat3Map();
        map.put("a", "b");
        MapIterator it = map.mapIterator();
        it.next();
        it.remove();
        it.remove(); // throws
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorGetKeyBeforeNextThrows() {
        Flat3Map map = new Flat3Map();
        map.put("a", "b");
        MapIterator it = map.mapIterator();
        it.getKey();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorGetValueBeforeNextThrows() {
        Flat3Map map = new Flat3Map();
        map.put("a", "b");
        MapIterator it = map.mapIterator();
        it.getValue();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorSetValueBeforeNextThrows() {
        Flat3Map map = new Flat3Map();
        map.put("a", "b");
        MapIterator it = map.mapIterator();
        it.setValue("x");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testEntrySetIteratorNoSuchElementException() {
        Flat3Map map = new Flat3Map();
        map.put("k", "v");
        Iterator it = map.entrySet().iterator();
        it.next();
        it.next(); // throws
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testEntrySetIteratorRemoveBeforeNextThrows() {
        Flat3Map map = new Flat3Map();
        map.put("k", "v");
        Iterator it = map.entrySet().iterator();
        it.remove();
    }

    // =========================================================================
    // Partition E: Iterators, Views, Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testFlatMapIteratorFullLifecycle() {
        Flat3Map map = new Flat3Map();
        // Empty map iterator
        MapIterator emptyIt = map.mapIterator();
        assertFalse(emptyIt.hasNext());

        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        Flat3Map.FlatMapIterator it = (Flat3Map.FlatMapIterator) map.mapIterator();
        assertEquals("Iterator[]", it.toString());

        assertTrue(it.hasNext());
        assertEquals("k1", it.next());
        assertEquals("k1", it.getKey());
        assertEquals("v1", it.getValue());
        assertEquals("Iterator[k1=v1]", it.toString());

        assertEquals("v1", it.setValue("v1-alt"));
        assertEquals("v1-alt", map.get("k1"));

        assertEquals("k2", it.next());
        assertEquals("k3", it.next());
        assertFalse(it.hasNext());

        // Test remove while iterating
        it.remove();
        assertEquals(2, map.size());
        assertNull(map.get("k3"));

        // Reset
        it.reset();
        assertTrue(it.hasNext());
        assertEquals("k1", it.next());

        // Delegate mode mapIterator
        map.put("k3", "v3");
        map.put("k4", "v4");
        MapIterator delegateIt = map.mapIterator();
        assertNotNull(delegateIt);
        assertTrue(delegateIt.hasNext());
    }

    @Test(timeout = 4000)
    public void testEntrySetViewAndIterator() {
        Flat3Map map = new Flat3Map();
        Set entrySet = map.entrySet();
        assertEquals(0, entrySet.size());
        assertFalse(entrySet.iterator().hasNext());

        map.put("k1", "v1");
        map.put("k2", "v2");
        assertEquals(2, entrySet.size());

        // EntrySet remove non-Entry
        assertFalse(entrySet.remove("randomObject"));

        // EntrySet remove valid Entry
        Map testMap = new HashMap();
        testMap.put("k1", "v1");
        Map.Entry entryToRemove = (Map.Entry) testMap.entrySet().iterator().next();
        assertTrue(entrySet.remove(entryToRemove));
        assertEquals(1, map.size());
        assertFalse(map.containsKey("k1"));

        // Remove entry not present
        assertFalse(entrySet.remove(entryToRemove));

        // Clear via entrySet
        entrySet.clear();
        assertEquals(0, map.size());

        // Test EntrySetIterator contract: equals, hashCode, toString
        map.put("k1", "v1");
        Iterator it = map.entrySet().iterator();
        Flat3Map.EntrySetIterator entry = (Flat3Map.EntrySetIterator) it.next();

        assertEquals("k1", entry.getKey());
        assertEquals("v1", entry.getValue());
        assertEquals("k1=v1", entry.toString());

        // equals with same Map.Entry
        Map helper = new HashMap();
        helper.put("k1", "v1");
        Map.Entry helperEntry = (Map.Entry) helper.entrySet().iterator().next();
        assertTrue(entry.equals(helperEntry));
        assertEquals(helperEntry.hashCode(), entry.hashCode());

        assertFalse(entry.equals("not an entry"));
        helper.put("k1", "differentVal");
        assertFalse(entry.equals(helper.entrySet().iterator().next()));

        // Delegate mode entrySet
        map.put("k2", "v2");
        map.put("k3", "v3");
        map.put("k4", "v4");
        assertEquals(4, map.entrySet().size());
        Iterator delegateEntryIt = map.entrySet().iterator();
        assertTrue(delegateEntryIt.hasNext());
    }

    @Test(timeout = 4000)
    public void testKeySetAndValuesViews() {
        Flat3Map map = new Flat3Map();
        Set keySet = map.keySet();
        Collection values = map.values();

        assertEquals(0, keySet.size());
        assertEquals(0, values.size());
        assertFalse(keySet.iterator().hasNext());
        assertFalse(values.iterator().hasNext());

        map.put("k1", "v1");
        map.put("k2", "v2");

        assertTrue(keySet.contains("k1"));
        assertFalse(keySet.contains("absent"));
        assertTrue(values.contains("v1"));
        assertFalse(values.contains("absent"));

        // Remove from keySet
        assertTrue(keySet.remove("k1"));
        assertFalse(keySet.remove("k1"));
        assertEquals(1, map.size());
        assertFalse(map.containsKey("k1"));

        // KeySet iterator next() returns key
        Iterator keyIt = map.keySet().iterator();
        assertEquals("k2", keyIt.next());

        // Values iterator next() returns value
        Iterator valIt = map.values().iterator();
        assertEquals("v2", valIt.next());

        // Clear via views
        keySet.clear();
        assertEquals(0, map.size());

        map.put("k1", "v1");
        values.clear();
        assertEquals(0, map.size());

        // Views in delegate mode
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        assertEquals(4, map.keySet().size());
        assertEquals(4, map.values().size());
        assertTrue(map.keySet().contains("a"));
        assertTrue(map.values().contains("1"));
        assertNotNull(map.keySet().iterator().next());
        assertNotNull(map.values().iterator().next());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        Flat3Map map1 = new Flat3Map();
        Flat3Map map2 = new Flat3Map();

        // Equals self
        assertTrue(map1.equals(map1));
        assertFalse(map1.equals(null));
        assertFalse(map1.equals("aString"));

        // Both empty
        assertTrue(map1.equals(map2));
        assertEquals(map1.hashCode(), map2.hashCode());

        // Size mismatch
        map1.put("k1", "v1");
        assertFalse(map1.equals(map2));

        // Different keys
        map2.put("diffKey", "v1");
        assertFalse(map1.equals(map2));

        // Same keys, different values
        map2.clear();
        map2.put("k1", "diffVal");
        assertFalse(map1.equals(map2));

        // Matches size 1
        map2.put("k1", "v1");
        assertTrue(map1.equals(map2));
        assertEquals(map1.hashCode(), map2.hashCode());

        // Matches size 2
        map1.put("k2", "v2");
        map2.put("k2", "v2");
        assertTrue(map1.equals(map2));
        assertEquals(map1.hashCode(), map2.hashCode());

        // Matches size 3
        map1.put("k3", null);
        map2.put("k3", null);
        assertTrue(map1.equals(map2));
        assertEquals(map1.hashCode(), map2.hashCode());

        // One has non-null, other has null for value
        map2.put("k3", "notNull");
        assertFalse(map1.equals(map2));

        // Equals in delegate mode
        map1.put("k4", "v4");
        map2.put("k3", null);
        map2.put("k4", "v4");
        assertTrue(map1.equals(map2));
        assertEquals(map1.hashCode(), map2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToStringSpecialCases() {
        Flat3Map map = new Flat3Map();
        assertEquals("{}", map.toString());

        map.put("k1", "v1");
        assertEquals("{k1=v1}", map.toString());

        map.put("k2", "v2");
        assertEquals("{k2=v2,k1=v1}", map.toString());

        map.put("k3", "v3");
        assertEquals("{k3=v3,k2=v2,k1=v1}", map.toString());

        // Self-referencing map as key and value
        Flat3Map selfMap = new Flat3Map();
        selfMap.put(selfMap, selfMap);
        assertEquals("{(this Map)=(this Map)}", selfMap.toString());

        // Delegate mode toString
        map.put("k4", "v4");
        assertTrue(map.toString().startsWith("{"));
        assertTrue(map.toString().endsWith("}"));
    }

    @Test(timeout = 4000)
    public void testClone() {
        // Clone flat mode
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");

        Flat3Map clone1 = (Flat3Map) map.clone();
        assertEquals(map.size(), clone1.size());
        assertEquals("v1", clone1.get("k1"));
        assertEquals("v2", clone1.get("k2"));

        // Modifying clone should not modify parent
        clone1.put("k3", "v3");
        assertEquals(2, map.size());
        assertEquals(3, clone1.size());

        // Clone delegate mode
        map.put("k3", "v3");
        map.put("k4", "v4");
        Flat3Map clone2 = (Flat3Map) map.clone();
        assertEquals(4, clone2.size());
        assertEquals("v4", clone2.get("k4"));

        clone2.put("k5", "v5");
        assertEquals(4, map.size());
        assertEquals(5, clone2.size());
    }

    @Test(timeout = 4000)
    public void testSerializationFlatAndDelegate() throws Exception {
        // 1. Serialize flat mode (<= 3 items)
        Flat3Map flatMap = new Flat3Map();
        flatMap.put("1", "one");
        flatMap.put("2", "two");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(flatMap);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Flat3Map deserializedFlat = (Flat3Map) ois.readObject();
        ois.close();

        assertEquals(flatMap.size(), deserializedFlat.size());
        assertEquals("one", deserializedFlat.get("1"));
        assertEquals("two", deserializedFlat.get("2"));

        // 2. Serialize delegate mode (> 3 items)
        Flat3Map delegateMap = new Flat3Map();
        delegateMap.put("1", "one");
        delegateMap.put("2", "two");
        delegateMap.put("3", "three");
        delegateMap.put("4", "four");

        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.writeObject(delegateMap);
        oos.close();

        ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Flat3Map deserializedDelegate = (Flat3Map) ois.readObject();
        ois.close();

        assertEquals(delegateMap.size(), deserializedDelegate.size());
        assertEquals("four", deserializedDelegate.get("4"));
    }
}