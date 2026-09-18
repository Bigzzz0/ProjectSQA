package org.apache.commons.collections.map;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.collections.MapIterator;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: Flat3Map
 * Key State Transitions:
 *   - Flat mode: sizes 0, 1, 2, 3 (direct fields: key1..3, value1..3, hash1..3)
 *   - Switch to Delegate mode: size transitions 3 -> 4 (convertToMap -> delegateMap)
 *   - Switch back to Flat mode: clear() on delegateMap resets delegateMap to null and size to 0
 *
 * Targeted Decision Branches & Boundaries:
 *   1. get(Object) & containsKey(Object):
 *      - null key lookup across sizes 0, 1, 2, 3 (each slot matching vs missing)
 *      - non-null key lookup across sizes 0, 1, 2, 3 (hash code collision vs mismatch, equality vs inequality)
 *      - delegated mode lookups
 *   2. containsValue(Object):
 *      - null value search across sizes 0, 1, 2, 3
 *      - non-null value search across sizes 0, 1, 2, 3
 *      - delegated mode value search
 *   3. put(Object, Object):
 *      - overwrite existing entry (null key and non-null key at slots 1, 2, 3)
 *      - append new entry: size 0 -> 1 -> 2 -> 3 -> 4 (delegate transition)
 *      - delegated mode put
 *   4. putAll(Map):
 *      - empty map (no-op)
 *      - map size < 4 while flat vs map size >= 4 triggering instant delegate conversion
 *      - putAll while already in delegate mode
 *   5. remove(Object):
 *      - empty map removal
 *      - null key removal at sizes 1, 2, 3
 *      - non-null key removal at sizes 1, 2, 3
 *      - shift logic validation (ensuring slot 3 shifts to slot 1 or 2 correctly)
 *      - delegate mode removal
 *   6. clear():
 *      - flat mode clear (zeroing all field refs)
 *      - delegate mode clear (delegating and nullifying delegateMap)
 *   7. Iterators & Views:
 *      - MapIterator (FlatMapIterator): hasNext, next, getKey, getValue, setValue, remove, reset, toString
 *      - EntrySet, KeySet, Values: size, clear, contains, remove, iterator
 *      - Empty views when size == 0 (EmptyIterator, EmptyMapIterator)
 *      - EntrySetIterator equality, hashCode, toString
 *   8. Contracts & Serialization:
 *      - equals and hashCode across flat (sizes 0..3) and delegate modes
 *      - clone() shallow copy integrity for both flat and delegated maps
 *      - custom serialization (writeObject / readObject) for flat (<=3) and delegated (>3)
 *      - toString() with self-referencing map entries ("(this Map)")
 *
 * CRITICAL DEFECT REPRODUCTION (COLLECTIONS-261):
 *   - Flat3Map.remove(Object key) when size == 2 or size == 3:
 *     Bug: In size == 2, removing key1 incorrectly assigned `old = value2` instead of `value1`.
 *     Bug: In size == 3, removing key2 or key1 incorrectly assigned `old = value3` instead of `value2` or `value1`.
 *     Target test: testCollections261 asserts map.remove(key1) returns value1 when size == 2.
 * ====================================================================================================
 */
public class Flat3MapGeminiTest {

    // ========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ========================================================================

    @Test(timeout = 4000)
    public void testPutGetAndTransitionsUpToDelegate() {
        Flat3Map map = new Flat3Map();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());

        // Size 0 -> 1
        assertNull(map.put("k1", "v1"));
        assertFalse(map.isEmpty());
        assertEquals(1, map.size());
        assertEquals("v1", map.get("k1"));

        // Size 1 -> 2
        assertNull(map.put("k2", "v2"));
        assertEquals(2, map.size());
        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));

        // Size 2 -> 3
        assertNull(map.put("k3", "v3"));
        assertEquals(3, map.size());
        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));
        assertEquals("v3", map.get("k3"));

        // Overwrite in flat mode (slot 2)
        assertEquals("v2", map.put("k2", "v2_updated"));
        assertEquals(3, map.size());
        assertEquals("v2_updated", map.get("k2"));

        // Size 3 -> 4 (Triggers convertToMap() into delegate mode)
        assertNull(map.put("k4", "v4"));
        assertEquals(4, map.size());
        assertEquals("v1", map.get("k1"));
        assertEquals("v2_updated", map.get("k2"));
        assertEquals("v3", map.get("k3"));
        assertEquals("v4", map.get("k4"));

        // Delegate mode overwrite
        assertEquals("v4", map.put("k4", "v4_new"));
        assertEquals("v4_new", map.get("k4"));
    }

    @Test(timeout = 4000)
    public void testClearResetsDelegateModeToFlat() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        map.put("k4", "v4"); // now in delegate mode
        assertEquals(4, map.size());

        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get("k1"));

        // Ensure it is back in flat mode
        map.put("a", "1");
        map.put("b", "2");
        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
    }

    @Test(timeout = 4000)
    public void testPutAllScenarios() {
        // 1. putAll into empty map with small map (<4)
        Flat3Map map = new Flat3Map();
        Map<String, String> small = new HashMap<String, String>();
        small.put("A", "1");
        small.put("B", "2");
        map.putAll(small);
        assertEquals(2, map.size());
        assertEquals("1", map.get("A"));
        assertEquals("2", map.get("B"));

        // 2. putAll empty map (no-op)
        map.putAll(new HashMap<String, String>());
        assertEquals(2, map.size());

        // 3. putAll with size >= 4 (direct conversion to delegateMap)
        Flat3Map mapLarge = new Flat3Map();
        Map<String, String> large = new HashMap<String, String>();
        large.put("1", "A");
        large.put("2", "B");
        large.put("3", "C");
        large.put("4", "D");
        mapLarge.putAll(large);
        assertEquals(4, mapLarge.size());
        assertEquals("A", mapLarge.get("1"));
        assertEquals("D", mapLarge.get("4"));

        // 4. putAll into already-delegated map
        Map<String, String> extra = new HashMap<String, String>();
        extra.put("5", "E");
        mapLarge.putAll(extra);
        assertEquals(5, mapLarge.size());
        assertEquals("E", mapLarge.get("5"));
    }

    @Test(timeout = 4000)
    public void testContainsKeyAndContainsValue() {
        Flat3Map map = new Flat3Map();
        // size 0
        assertFalse(map.containsKey("k1"));
        assertFalse(map.containsValue("v1"));

        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");

        // size 3 flat lookups
        assertTrue(map.containsKey("k1"));
        assertTrue(map.containsKey("k2"));
        assertTrue(map.containsKey("k3"));
        assertFalse(map.containsKey("missing"));

        assertTrue(map.containsValue("v1"));
        assertTrue(map.containsValue("v2"));
        assertTrue(map.containsValue("v3"));
        assertFalse(map.containsValue("missing"));

        // transition to delegate mode
        map.put("k4", "v4");
        assertTrue(map.containsKey("k4"));
        assertFalse(map.containsKey("missing"));
        assertTrue(map.containsValue("v4"));
        assertFalse(map.containsValue("missing"));
    }

    // ========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ========================================================================

    @Test(timeout = 4000)
    public void testNullKeyHandlingFlatAndDelegate() {
        Flat3Map map = new Flat3Map();

        // Null key at slot 1
        assertNull(map.put(null, "nullVal1"));
        assertEquals(1, map.size());
        assertTrue(map.containsKey(null));
        assertEquals("nullVal1", map.get(null));

        // Overwrite null key at slot 1
        assertEquals("nullVal1", map.put(null, "nullVal1_updated"));
        assertEquals("nullVal1_updated", map.get(null));

        // Null key with size 2
        map.clear();
        map.put("k1", "v1");
        map.put(null, "nullVal2");
        assertEquals(2, map.size());
        assertTrue(map.containsKey(null));
        assertEquals("nullVal2", map.get(null));
        assertEquals("nullVal2", map.put(null, "nullVal2_updated"));

        // Null key with size 3
        map.put("k3", "v3");
        assertEquals(3, map.size());
        assertTrue(map.containsKey(null));
        assertEquals("nullVal2_updated", map.get(null));

        // Delegate mode with null key
        map.put("k4", "v4");
        assertTrue(map.containsKey(null));
        assertEquals("nullVal2_updated", map.get(null));
    }

    @Test(timeout = 4000)
    public void testNullValueHandlingFlatAndDelegate() {
        Flat3Map map = new Flat3Map();
        assertNull(map.put("k1", null));
        assertEquals(1, map.size());
        assertTrue(map.containsValue(null));
        assertNull(map.get("k1"));

        map.put("k2", "v2");
        map.put("k3", null);
        assertEquals(3, map.size());
        assertTrue(map.containsValue(null));
        assertTrue(map.containsValue("v2"));

        map.put("k4", "v4"); // Delegate mode
        assertTrue(map.containsValue(null));
        assertTrue(map.containsValue("v4"));
    }

    @Test(timeout = 4000)
    public void testHashCollisionBranchesInFlatMode() {
        // Two distinct objects with identical hashCode to exercise hash match but !equals
        class CollisionKey {
            private final String id;
            CollisionKey(String id) { this.id = id; }
            @Override
            public int hashCode() { return 42; }
            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof CollisionKey)) return false;
                return this.id.equals(((CollisionKey) obj).id);
            }
        }

        CollisionKey k1 = new CollisionKey("1");
        CollisionKey k2 = new CollisionKey("2");
        CollisionKey k3 = new CollisionKey("3");
        CollisionKey k4Mismatch = new CollisionKey("4");

        Flat3Map map = new Flat3Map();
        map.put(k1, "v1");
        map.put(k2, "v2");
        map.put(k3, "v3");

        assertEquals("v1", map.get(k1));
        assertEquals("v2", map.get(k2));
        assertEquals("v3", map.get(k3));
        assertNull(map.get(k4Mismatch));
        assertFalse(map.containsKey(k4Mismatch));

        // Overwrite under collision
        assertEquals("v2", map.put(k2, "v2_updated"));
        assertEquals("v2_updated", map.get(k2));
    }

    // ========================================================================
    // Partition C: Defect-Targeted Branch Zone (COLLECTIONS-261)
    // ========================================================================

    /**
     * Defects4J Target: COLLECTIONS-261
     * Map.remove(key) must return the value mapped to key1 when size == 2.
     * In defective Flat3Map, it returned value2 instead of value1!
     */
    @Test(timeout = 4000)
    public void testCollections261() {
        Flat3Map map = new Flat3Map();
        Integer one = new Integer(1);
        Integer zero = new Integer(0);
        map.put(one, one);
        map.put(zero, zero);
        assertEquals(one, map.remove(one));
    }

    @Test(timeout = 4000)
    public void testRemoveReturnValueIntegritySize3() {
        // When size == 3, removing key1 or key2 must return value1 or value2, not value3!
        Flat3Map map = new Flat3Map();
        map.put("A", "valA");
        map.put("B", "valB");
        map.put("C", "valC");

        // Remove slot 1 (A)
        assertEquals("valA", map.remove("A"));
        assertEquals(2, map.size());
        assertFalse(map.containsKey("A"));
        assertTrue(map.containsKey("B"));
        assertTrue(map.containsKey("C"));

        // Reset and remove slot 2 (B)
        map.clear();
        map.put("A", "valA");
        map.put("B", "valB");
        map.put("C", "valC");
        assertEquals("valB", map.remove("B"));
        assertEquals(2, map.size());
        assertTrue(map.containsKey("A"));
        assertFalse(map.containsKey("B"));
        assertTrue(map.containsKey("C"));

        // Reset and remove slot 3 (C)
        map.clear();
        map.put("A", "valA");
        map.put("B", "valB");
        map.put("C", "valC");
        assertEquals("valC", map.remove("C"));
        assertEquals(2, map.size());
        assertTrue(map.containsKey("A"));
        assertTrue(map.containsKey("B"));
        assertFalse(map.containsKey("C"));
    }

    @Test(timeout = 4000)
    public void testRemoveNullKeyReturnValues() {
        // Size 1 with null
        Flat3Map map = new Flat3Map();
        map.put(null, "vNull");
        assertEquals("vNull", map.remove(null));
        assertEquals(0, map.size());

        // Size 2 with null at key1
        map.clear();
        map.put(null, "vNull");
        map.put("k2", "v2");
        assertEquals("vNull", map.remove(null));
        assertEquals(1, map.size());
        assertEquals("v2", map.get("k2"));

        // Size 2 with null at key2
        map.clear();
        map.put("k1", "v1");
        map.put(null, "vNull");
        assertEquals("vNull", map.remove(null));
        assertEquals(1, map.size());
        assertEquals("v1", map.get("k1"));

        // Size 3 with null at key1
        map.clear();
        map.put(null, "vNull");
        map.put("k2", "v2");
        map.put("k3", "v3");
        assertEquals("vNull", map.remove(null));
        assertEquals(2, map.size());
        assertEquals("v2", map.get("k2"));
        assertEquals("v3", map.get("k3"));

        // Size 3 with null at key2
        map.clear();
        map.put("k1", "v1");
        map.put(null, "vNull");
        map.put("k3", "v3");
        assertEquals("vNull", map.remove(null));
        assertEquals(2, map.size());
        assertEquals("v1", map.get("k1"));
        assertEquals("v3", map.get("k3"));

        // Size 3 with null at key3
        map.clear();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put(null, "vNull");
        assertEquals("vNull", map.remove(null));
        assertEquals(2, map.size());
        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));
    }

    @Test(timeout = 4000)
    public void testRemoveNonExistentAndDelegateMode() {
        Flat3Map map = new Flat3Map();
        assertNull(map.remove("missing")); // size 0

        map.put("k1", "v1");
        map.put("k2", "v2");
        assertNull(map.remove("missing"));
        assertNull(map.remove(null));

        map.put("k3", "v3");
        assertNull(map.remove("missing"));
        assertNull(map.remove(null));

        map.put("k4", "v4"); // Delegate mode
        assertEquals("v3", map.remove("k3"));
        assertEquals(3, map.size());
        assertNull(map.remove("k3"));
    }

    // ========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorWithNullMapThrowsNPE() {
        new Flat3Map((Map) null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testPutAllNullMapThrowsNPE() {
        Flat3Map map = new Flat3Map();
        map.putAll(null);
    }

    @Test(timeout = 4000)
    public void testMapIteratorExceptionsAndLifecycle() {
        Flat3Map map = new Flat3Map();
        MapIterator it = map.mapIterator();
        assertFalse(it.hasNext());

        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {}

        try {
            it.getKey();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {}

        try {
            it.getValue();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {}

        try {
            it.setValue("val");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {}

        try {
            it.remove();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {}

        // Populate 3 entries
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");

        it = map.mapIterator();
        assertEquals("Iterator[]", it.toString());
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertEquals("Iterator[A=1]", it.toString());
        assertEquals("A", it.getKey());
        assertEquals("1", it.getValue());
        assertEquals("1", it.setValue("1_updated"));
        assertEquals("1_updated", it.getValue());

        it.remove();
        assertEquals(2, map.size());
        try {
            it.remove(); // Cannot remove twice
            fail("Expected IllegalStateException on consecutive remove");
        } catch (IllegalStateException expected) {}

        it.reset();
        assertTrue(it.hasNext());
        assertEquals("Iterator[]", it.toString());

        // Delegate map iterator
        map.put("D", "4");
        map.put("E", "5");
        MapIterator delegateIt = map.mapIterator();
        assertTrue(delegateIt.hasNext());
        assertNotNull(delegateIt.next());
    }

    @Test(timeout = 4000)
    public void testEntrySetIteratorLifecycle() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");

        Set entrySet = map.entrySet();
        assertEquals(2, entrySet.size());
        Iterator it = entrySet.iterator();

        assertTrue(it.hasNext());
        Map.Entry entry = (Map.Entry) it.next();
        assertEquals("k1", entry.getKey());
        assertEquals("v1", entry.getValue());
        assertEquals("k1=v1", entry.toString());
        assertEquals("v1", entry.setValue("v1_mod"));
        assertEquals("v1_mod", entry.getValue());

        // Entry equality and hashCode
        Map.Entry dummyEntry = new AbstractMap.SimpleEntry("k1", "v1_mod");
        assertTrue(entry.equals(dummyEntry));
        assertFalse(entry.equals("not an entry"));
        assertEquals(dummyEntry.hashCode(), entry.hashCode());

        it.remove();
        assertEquals(1, map.size());
        assertFalse(entry.equals(dummyEntry)); // canRemove is false after remove
        assertEquals(0, entry.hashCode());
        assertEquals("", entry.toString());

        try {
            it.remove();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {}

        // EntrySet clear and remove
        map.put("k3", "v3");
        assertTrue(entrySet.remove(new AbstractMap.SimpleEntry("k3", "v3")));
        assertFalse(entrySet.remove("not an entry"));
        assertFalse(entrySet.remove(new AbstractMap.SimpleEntry("missing", "val")));
        entrySet.clear();
        assertEquals(0, map.size());
    }

    @Test(timeout = 4000)
    public void testKeySetAndValuesViews() {
        Flat3Map map = new Flat3Map();
        map.put("k1", "v1");
        map.put("k2", "v2");

        Set keys = map.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("k1"));
        assertFalse(keys.contains("missing"));
        assertTrue(keys.remove("k1"));
        assertFalse(keys.remove("missing"));
        assertEquals(1, map.size());

        Iterator keyIt = keys.iterator();
        assertTrue(keyIt.hasNext());
        assertEquals("k2", keyIt.next());
        keyIt.remove();
        assertEquals(0, map.size());

        // Values view
        map.put("a", "alpha");
        map.put("b", "beta");
        Collection values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains("alpha"));
        assertFalse(values.contains("gamma"));

        Iterator valIt = values.iterator();
        assertTrue(valIt.hasNext());
        Object val = valIt.next();
        assertTrue("alpha".equals(val) || "beta".equals(val));
        values.clear();
        assertEquals(0, map.size());

        // Delegate mode views
        map.put("1", "A");
        map.put("2", "B");
        map.put("3", "C");
        map.put("4", "D");
        assertEquals(4, map.keySet().size());
        assertEquals(4, map.values().size());
        assertEquals(4, map.entrySet().size());
    }

    // ========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        Flat3Map map1 = new Flat3Map();
        Flat3Map map2 = new Flat3Map();

        assertTrue(map1.equals(map1));
        assertTrue(map1.equals(map2));
        assertEquals(map1.hashCode(), map2.hashCode());

        map1.put("k1", "v1");
        assertFalse(map1.equals(map2));
        assertFalse(map1.equals("Not a map"));

        map2.put("k1", "v1");
        assertTrue(map1.equals(map2));
        assertEquals(map1.hashCode(), map2.hashCode());

        // Size 2
        map1.put("k2", null);
        map2.put("k2", "nonNull");
        assertFalse(map1.equals(map2));

        map2.put("k2", null);
        assertTrue(map1.equals(map2));
        assertEquals(map1.hashCode(), map2.hashCode());

        // Size 3
        map1.put("k3", "v3");
        map2.put("k3", "v3_diff");
        assertFalse(map1.equals(map2));

        map2.put("k3", "v3");
        assertTrue(map1.equals(map2));
        assertEquals(map1.hashCode(), map2.hashCode());

        // Delegate mode equals
        map1.put("k4", "v4");
        map2.put("k4", "v4");
        assertTrue(map1.equals(map2));
        assertEquals(map1.hashCode(), map2.hashCode());
    }

    @Test(timeout = 4000)
    public void testCloneFlatAndDelegate() {
        Flat3Map map = new Flat3Map();
        map.put("1", "A");
        map.put("2", "B");

        Flat3Map clonedFlat = (Flat3Map) map.clone();
        assertEquals(map.size(), clonedFlat.size());
        assertEquals(map.get("1"), clonedFlat.get("1"));
        assertEquals(map.get("2"), clonedFlat.get("2"));

        // Mutate original, clone remains isolated
        map.put("3", "C");
        assertEquals(3, map.size());
        assertEquals(2, clonedFlat.size());

        // Clone in delegate mode
        map.put("4", "D"); // delegate mode
        Flat3Map clonedDelegate = (Flat3Map) map.clone();
        assertEquals(4, clonedDelegate.size());
        assertEquals("D", clonedDelegate.get("4"));
    }

    @Test(timeout = 4000)
    public void testSerializationFlatAndDelegate() throws Exception {
        // Flat mode round trip
        Flat3Map flatMap = new Flat3Map();
        flatMap.put("A", "1");
        flatMap.put("B", "2");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(flatMap);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Flat3Map deserializedFlat = (Flat3Map) ois.readObject();
        ois.close();

        assertEquals(flatMap.size(), deserializedFlat.size());
        assertEquals("1", deserializedFlat.get("A"));
        assertEquals("2", deserializedFlat.get("B"));

        // Delegate mode round trip (>3 elements)
        flatMap.put("C", "3");
        flatMap.put("D", "4");

        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.writeObject(flatMap);
        oos.close();

        bais = new ByteArrayInputStream(baos.toByteArray());
        ois = new ObjectInputStream(bais);
        Flat3Map deserializedDelegate = (Flat3Map) ois.readObject();
        ois.close();

        assertEquals(4, deserializedDelegate.size());
        assertEquals("4", deserializedDelegate.get("D"));
    }

    @Test(timeout = 4000)
    public void testToStringAndSelfReference() {
        Flat3Map map = new Flat3Map();
        assertEquals("{}", map.toString());

        map.put("k1", "v1");
        assertEquals("{k1=v1}", map.toString());

        // Self reference detection
        map.clear();
        map.put(map, map);
        assertEquals("{(this Map)=(this Map)}", map.toString());

        // Delegate mode toString
        map.clear();
        map.put("1", "A");
        map.put("2", "B");
        map.put("3", "C");
        map.put("4", "D");
        assertTrue(map.toString().startsWith("{"));
        assertTrue(map.toString().endsWith("}"));
    }

    @Test(timeout = 4000)
    public void testConstructorWithExistingMap() {
        Map<String, String> init = new HashMap<String, String>();
        init.put("X", "1");
        init.put("Y", "2");
        Flat3Map map = new Flat3Map(init);
        assertEquals(2, map.size());
        assertEquals("1", map.get("X"));
        assertEquals("2", map.get("Y"));
    }
}