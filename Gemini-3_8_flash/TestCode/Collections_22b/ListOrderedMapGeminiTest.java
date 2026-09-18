/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections4.map;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.commons.collections4.OrderedMapIterator;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.collections4.map.ListOrderedMap
 *
 * Defect Under Test (COLLECTIONS-474):
 * - Method: putAll(int index, final Map<? extends K, ? extends V> map)
 * - Root Cause: The method relies on `old == null` to determine if a key was newly inserted
 *   vs replaced. When a key exists with a null value, `old` is null, causing the index to
 *   advance unconditionally (incrementing index when it shouldn't or desynchronizing), leading
 *   to `IndexOutOfBoundsException: Index: 2, Size: 1` on subsequent insertions.
 * - Test Target: `testCOLLECTIONS_474_nullValues` reproduces this exact condition by inserting
 *   mappings where existing keys have null values into a map with size 1 at index 1.
 *
 * Branch & Coverage Matrix:
 * 1. Factory & Constructors:
 *    - listOrderedMap(Map): null map (IAE), non-empty decorated map (preserves keySet order).
 *    - ListOrderedMap(): default constructor backed by HashMap.
 *    - ListOrderedMap(Map): null map (IAE).
 * 2. OrderedMap Navigation:
 *    - firstKey(), lastKey(): empty map (NoSuchElementException), single-entry, multi-entry.
 *    - nextKey(Object), previousKey(Object): key absent, key at boundary (first/last), middle keys.
 * 3. Mutation Operations:
 *    - put(K, V): key existing vs key new.
 *    - put(int index, K, V): key new (add at index), key existing (remove & shift; pos < index vs pos >= index).
 *    - putAll(Map): empty, populated.
 *    - putAll(int, Map): normal case, replaced existing value, replaced null value (COLLECTIONS-474).
 *    - remove(Object): key existing vs absent.
 *    - remove(int): valid index, negative index, index >= size.
 *    - clear(): clears both decorated map and insertOrder list.
 *    - setValue(int, V): valid index, boundary index, out-of-bounds.
 * 4. Index-based Accessors:
 *    - get(int), getValue(int), indexOf(Object): valid, invalid, missing.
 * 5. Views & Iterators:
 *    - keySet(), keyList(), asList(): contains, remove, iterator, unmodifiable list contract.
 *    - values(), valueList(): List view operations: get, set, remove, iterator, contains.
 *    - entrySet(): contains, containsAll, remove (matching entry vs wrong value/type), equals, hashCode, toString.
 *    - ListOrderedIterator: next, remove.
 *    - ListOrderedMapEntry: getKey, getValue, setValue.
 *    - ListOrderedMapIterator: hasNext, next, hasPrevious, previous, getKey, getValue, setValue,
 *      remove, reset, toString, illegal state checks before next().
 * 6. String Representation:
 *    - toString(): empty map, populated map, recursive reference (this Map).
 * 7. Serialization:
 *    - writeObject / readObject roundtrip preserving order and decorated map.
 */
public class ListOrderedMapGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (COLLECTIONS-474)
    // =========================================================================

    /**
     * Targets Defects4J COLLECTIONS-474:
     * When putAll(int index, Map) is called where the map already contains a key
     * with a null value, `old == null` is true even though the key was replaced
     * rather than newly added. This incorrectly advanced `index`, leading to:
     * `java.lang.IndexOutOfBoundsException: Index: 2, Size: 1`.
     */
    @Test(timeout = 4000)
    public void testCOLLECTIONS_474_nullValues() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("key1", null); // size = 1, insertOrder = ["key1"]

        final Map<String, String> newMap = new LinkedHashMap<String, String>();
        newMap.put("key1", "val1");
        newMap.put("key2", "val2");

        // Should replace "key1" at index 0 and insert "key2" at index 1 without throwing IOOBE
        map.putAll(1, newMap);

        assertEquals(2, map.size());
        assertEquals("key1", map.get(0));
        assertEquals("val1", map.getValue(0));
        assertEquals("key2", map.get(1));
        assertEquals("val2", map.getValue(1));
    }

    /**
     * Additional COLLECTIONS-474 test case: multiple null keys in the target and source map.
     */
    @Test(timeout = 4000)
    public void testCOLLECTIONS_474_multipleNullValuesAtBeginning() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", null);
        map.put("b", null);

        final Map<String, String> update = new LinkedHashMap<String, String>();
        update.put("a", "newA");
        update.put("c", "newC");

        map.putAll(0, update);

        assertEquals(3, map.size());
        assertEquals("a", map.get(0));
        assertEquals("newA", map.getValue(0));
        assertEquals("c", map.get(1));
        assertEquals("newC", map.getValue(1));
        assertEquals("b", map.get(2));
        assertNull(map.getValue(2));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorAndBasicPutGet() {
        final ListOrderedMap<String, Integer> map = new ListOrderedMap<String, Integer>();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());

        assertNull(map.put("Alpha", 1));
        assertNull(map.put("Beta", 2));
        assertNull(map.put("Gamma", 3));

        assertEquals(3, map.size());
        assertEquals(Integer.valueOf(1), map.get("Alpha"));
        assertEquals(Integer.valueOf(2), map.get("Beta"));
        assertEquals(Integer.valueOf(3), map.get("Gamma"));

        // Put existing key replaces value without shifting order
        final Integer oldVal = map.put("Beta", 20);
        assertEquals(Integer.valueOf(2), oldVal);
        assertEquals(3, map.size());
        assertEquals("Beta", map.get(1));
        assertEquals(Integer.valueOf(20), map.getValue(1));
    }

    @Test(timeout = 4000)
    public void testFactoryMethodWithExistingMap() {
        final Map<String, String> base = new LinkedHashMap<String, String>();
        base.put("1", "one");
        base.put("2", "two");

        final ListOrderedMap<String, String> ordered = ListOrderedMap.listOrderedMap(base);
        assertEquals(2, ordered.size());
        assertEquals("1", ordered.firstKey());
        assertEquals("2", ordered.lastKey());
    }

    @Test(timeout = 4000)
    public void testFirstAndLastKey() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("first", "A");
        map.put("middle", "B");
        map.put("last", "C");

        assertEquals("first", map.firstKey());
        assertEquals("last", map.lastKey());
    }

    @Test(timeout = 4000)
    public void testNextAndPreviousKey() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        assertEquals("k2", map.nextKey("k1"));
        assertEquals("k3", map.nextKey("k2"));
        assertNull(map.nextKey("k3")); // last key has no next
        assertNull(map.nextKey("unknown")); // not found

        assertEquals("k2", map.previousKey("k3"));
        assertEquals("k1", map.previousKey("k2"));
        assertNull(map.previousKey("k1")); // first key has no previous
        assertNull(map.previousKey("unknown")); // not found
    }

    @Test(timeout = 4000)
    public void testRemoveByKey() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");

        assertEquals("v1", map.remove("k1"));
        assertEquals(1, map.size());
        assertNull(map.get("k1"));
        assertNull(map.remove("unknown"));
        assertEquals(-1, map.indexOf("k1"));
        assertEquals(0, map.indexOf("k2"));
    }

    @Test(timeout = 4000)
    public void testClear() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.clear();

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertEquals(-1, map.indexOf("k1"));
    }

    @Test(timeout = 4000)
    public void testPutAtIndexNewKey() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("C", "3");

        // Insert at index 1
        final String old = map.put(1, "B", "2");
        assertNull(old);
        assertEquals(3, map.size());
        assertEquals("A", map.get(0));
        assertEquals("B", map.get(1));
        assertEquals("C", map.get(2));
    }

    @Test(timeout = 4000)
    public void testPutAtIndexExistingKeyShiftForward() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");

        // Move "A" (pos 0) to index 2 (pos < index branch)
        final String old = map.put(2, "A", "newA");
        assertEquals("1", old);
        assertEquals(3, map.size());
        assertEquals("B", map.get(0));
        assertEquals("A", map.get(1));
        assertEquals("C", map.get(2));
        assertEquals("newA", map.getValue(1));
    }

    @Test(timeout = 4000)
    public void testPutAtIndexExistingKeyShiftBackward() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");

        // Move "C" (pos 2) to index 0 (pos >= index branch)
        final String old = map.put(0, "C", "newC");
        assertEquals("3", old);
        assertEquals(3, map.size());
        assertEquals("C", map.get(0));
        assertEquals("A", map.get(1));
        assertEquals("B", map.get(2));
        assertEquals("newC", map.getValue(0));
    }

    @Test(timeout = 4000)
    public void testSetValueAtIndex() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");

        final String prev = map.setValue(1, "v2-updated");
        assertEquals("v2", prev);
        assertEquals("v2-updated", map.getValue(1));
        assertEquals("k2", map.get(1));
    }

    @Test(timeout = 4000)
    public void testRemoveByIndex() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        final String removed = map.remove(1);
        assertEquals("v2", removed);
        assertEquals(2, map.size());
        assertEquals("k3", map.get(1));
        assertFalse(map.containsKey("k2"));
    }

    @Test(timeout = 4000)
    public void testPutAllMap() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k1", "v1");

        final Map<String, String> additional = new LinkedHashMap<String, String>();
        additional.put("k2", "v2");
        additional.put("k3", "v3");

        map.putAll(additional);
        assertEquals(3, map.size());
        assertEquals("k1", map.get(0));
        assertEquals("k2", map.get(1));
        assertEquals("k3", map.get(2));
    }

    @Test(timeout = 4000)
    public void testPutAllAtIndexWithNonNullReplacedValue() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");

        final Map<String, String> incoming = new LinkedHashMap<String, String>();
        incoming.put("k2", "v2-replaced");
        incoming.put("k3", "v3-new");

        map.putAll(1, incoming);

        assertEquals(3, map.size());
        assertEquals("k1", map.get(0));
        assertEquals("k2", map.get(1));
        assertEquals("v2-replaced", map.getValue(1));
        assertEquals("k3", map.get(2));
        assertEquals("v3-new", map.getValue(2));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyMapToString() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        assertEquals("{}", map.toString());
    }

    @Test(timeout = 4000)
    public void testPopulatedToString() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        assertEquals("{a=1, b=2}", map.toString());
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testSelfReferentialToString() {
        final ListOrderedMap<Object, Object> map = new ListOrderedMap<Object, Object>();
        map.put(map, map);
        assertEquals("{(this Map)=(this Map)}", map.toString());
    }

    @Test(timeout = 4000)
    public void testNullKeysAndValues() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put(null, "nullKey");
        map.put("nullVal", null);

        assertEquals(2, map.size());
        assertEquals(0, map.indexOf(null));
        assertEquals(1, map.indexOf("nullVal"));
        assertNull(map.get(0));
        assertEquals("nullKey", map.getValue(0));
        assertEquals("nullVal", map.get(1));
        assertNull(map.getValue(1));
        assertEquals("nullVal", map.nextKey(null));
        assertEquals(null, map.previousKey("nullVal"));
    }

    @Test(timeout = 4000)
    public void testIndexOfNonExistent() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k1", "v1");
        assertEquals(-1, map.indexOf("nonexistent"));
        assertEquals(-1, map.indexOf(null));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testFactoryNullMap() {
        ListOrderedMap.listOrderedMap(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorNullMap() {
        new ListOrderedMap<String, String>(null);
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testFirstKeyEmptyMapThrows() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.firstKey();
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testLastKeyEmptyMapThrows() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.lastKey();
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetIndexNegativeThrows() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k", "v");
        map.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetIndexOutOfBoundsThrows() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k", "v");
        map.get(1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValueOutOfBoundsThrows() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.getValue(0);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testSetValueOutOfBoundsThrows() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.setValue(0, "value");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testPutIndexNegativeThrows() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put(-1, "key", "val");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testPutIndexBeyondSizeThrows() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put(1, "key", "val");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testRemoveIndexOutOfBoundsThrows() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.remove(0);
    }

    // =========================================================================
    // Partition E: Views, Iterators & Lifecycle Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testKeySetAndKeyList() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("1", "one");
        map.put("2", "two");

        final Set<String> keySet = map.keySet();
        assertEquals(2, keySet.size());
        assertTrue(keySet.contains("1"));
        assertFalse(keySet.contains("unknown"));

        final Iterator<String> it = keySet.iterator();
        assertTrue(it.hasNext());
        assertEquals("1", it.next());
        assertTrue(it.hasNext());
        assertEquals("2", it.next());
        assertFalse(it.hasNext());

        final List<String> keyList = map.keyList();
        assertEquals(2, keyList.size());
        assertEquals("1", keyList.get(0));
        assertEquals("2", keyList.get(1));
        assertEquals(keyList, map.asList());

        keySet.clear();
        assertTrue(map.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testKeyListIsUnmodifiable() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("1", "one");
        map.keyList().add("two");
    }

    @Test(timeout = 4000)
    public void testValuesAndValueList() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");

        final Collection<String> values = map.values();
        final List<String> valList = map.valueList();
        assertEquals(2, values.size());
        assertTrue(values.contains("v1"));
        assertFalse(values.contains("nonexistent"));

        assertEquals("v1", valList.get(0));
        assertEquals("v2", valList.get(1));

        final String prev = valList.set(1, "v2-updated");
        assertEquals("v2", prev);
        assertEquals("v2-updated", map.get("k2"));

        final Iterator<String> it = values.iterator();
        assertTrue(it.hasNext());
        assertEquals("v1", it.next());
        assertTrue(it.hasNext());
        assertEquals("v2-updated", it.next());

        final String removedVal = valList.remove(0);
        assertEquals("v1", removedVal);
        assertEquals(1, map.size());
        assertFalse(map.containsKey("k1"));

        valList.clear();
        assertTrue(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testEntrySetViewOperations() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");

        final Set<Map.Entry<String, String>> entrySet = map.entrySet();
        assertEquals(2, entrySet.size());
        assertFalse(entrySet.isEmpty());

        final Iterator<Map.Entry<String, String>> it = entrySet.iterator();
        assertTrue(it.hasNext());
        final Map.Entry<String, String> entry1 = it.next();
        assertEquals("k1", entry1.getKey());
        assertEquals("v1", entry1.getValue());
        assertTrue(entrySet.contains(entry1));

        // Test entry.setValue
        entry1.setValue("v1-mutated");
        assertEquals("v1-mutated", map.get("k1"));
        assertEquals("v1-mutated", entry1.getValue());

        // Test containsAll
        final List<Map.Entry<String, String>> checkList = new ArrayList<Map.Entry<String, String>>();
        checkList.add(entry1);
        assertTrue(entrySet.containsAll(checkList));

        // Remove via iterator
        it.remove();
        assertEquals(1, map.size());
        assertFalse(map.containsKey("k1"));

        // Entry removal via Set.remove(Object)
        final Map.Entry<String, String> remainingEntry = entrySet.iterator().next();
        assertFalse(entrySet.remove("not-an-entry"));
        assertTrue(entrySet.remove(remainingEntry));
        assertTrue(map.isEmpty());

        // Reset and clear entrySet
        map.put("kx", "vx");
        assertNotNull(entrySet.toString());
        assertEquals(entrySet.hashCode(), entrySet.hashCode());
        assertTrue(entrySet.equals(entrySet));
        assertFalse(entrySet.equals(new Object()));
        entrySet.clear();
        assertTrue(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testMapIteratorNavigationAndMutations() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");

        final OrderedMapIterator<String, String> it = map.mapIterator();
        assertFalse(it.hasPrevious());
        assertTrue(it.hasNext());
        assertEquals("Iterator[]", it.toString());

        assertEquals("k1", it.next());
        assertEquals("k1", it.getKey());
        assertEquals("v1", it.getValue());
        assertEquals("Iterator[k1=v1]", it.toString());
        assertTrue(it.hasPrevious());

        assertEquals("k2", it.next());
        assertEquals("k2", it.getKey());
        assertEquals("v2", it.getValue());
        assertFalse(it.hasNext());

        // Test setValue through iterator
        assertEquals("v2", it.setValue("v2-new"));
        assertEquals("v2-new", map.get("k2"));

        // Navigate backwards
        assertEquals("k2", it.previous());
        assertEquals("k1", it.previous());
        assertFalse(it.hasPrevious());

        // Test remove through iterator
        assertEquals("k1", it.next());
        it.remove();
        assertEquals(1, map.size());
        assertFalse(map.containsKey("k1"));

        // Test reset
        it.reset();
        assertEquals("Iterator[]", it.toString());
        assertTrue(it.hasNext());
        assertEquals("k2", it.next());
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorGetKeyWithoutNextThrows() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k", "v");
        final OrderedMapIterator<String, String> it = map.mapIterator();
        it.getKey();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorGetValueWithoutNextThrows() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k", "v");
        final OrderedMapIterator<String, String> it = map.mapIterator();
        it.getValue();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorSetValueWithoutNextThrows() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k", "v");
        final OrderedMapIterator<String, String> it = map.mapIterator();
        it.setValue("error");
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorRemoveWithoutNextThrows() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k", "v");
        final OrderedMapIterator<String, String> it = map.mapIterator();
        it.remove();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorDoubleRemoveThrows() {
        final ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("k1", "v1");
        final OrderedMapIterator<String, String> it = map.mapIterator();
        it.next();
        it.remove();
        it.remove();
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testSerializationRoundTrip() throws Exception {
        final ListOrderedMap<String, Integer> map = new ListOrderedMap<String, Integer>();
        map.put("first", 100);
        map.put("second", 200);
        map.put("third", 300);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final ListOrderedMap<String, Integer> deserialized = (ListOrderedMap<String, Integer>) ois.readObject();
        ois.close();

        assertEquals(map.size(), deserialized.size());
        assertEquals("first", deserialized.get(0));
        assertEquals("second", deserialized.get(1));
        assertEquals("third", deserialized.get(2));
        assertEquals(Integer.valueOf(100), deserialized.getValue(0));
        assertEquals(Integer.valueOf(200), deserialized.getValue(1));
        assertEquals(Integer.valueOf(300), deserialized.getValue(2));
    }
}