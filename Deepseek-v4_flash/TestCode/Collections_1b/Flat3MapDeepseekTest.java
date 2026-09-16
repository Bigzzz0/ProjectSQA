package org.apache.commons.collections.map;

import static org.junit.Assert.*;

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

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.ResettableIterator;
import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Primary defect targeted:
 *  - D1: FlatMapIterator.setValue() and EntrySetIterator.setValue() use switch
 *    fall-through. For nextIndex==2 or nextIndex==3, the assignment writes to
 *    earlier slots as well, corrupting previously mapped values.
 *    Tests assert that setValue on the second and third entries preserves the
 *    first/second values respectively.
 *
 * Additional coverage:
 *  - Flat mode size 0,1,2,3 and delegate mode transition at size 4.
 *  - Null keys/values placed in each flat slot.
 *  - put/replace/remove/contains/get branches for null and non-null keys.
 *  - MapIterator, EntrySet, KeySet and Values view behaviour and iterator state guards.
 *  - equals/hashCode/toString/clone/serialization contracts.
 *  - Hash collision branch (same hashCode, unequal keys).
 */
public class Flat3MapDeepseekTest {

    private static Flat3Map mapOf(Object... entries) {
        Flat3Map map = new Flat3Map();
        for (int i = 0; i < entries.length; i += 2) {
            map.put(entries[i], entries[i + 1]);
        }
        return map;
    }

    private static Flat3Map roundTrip(Flat3Map map) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(map);
        out.close();
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (Flat3Map) in.readObject();
    }

    @Test(timeout = 4000)
    public void testEmptyMapContract() {
        Flat3Map map = new Flat3Map();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get("missing"));
        assertNull(map.get(null));
        assertFalse(map.containsKey("missing"));
        assertFalse(map.containsKey(null));
        assertFalse(map.containsValue("missing"));
        assertFalse(map.containsValue(null));
        assertEquals("{}", map.toString());
        assertEquals(0, map.hashCode());
        assertEquals(new Flat3Map(), map);
        assertFalse(map.equals(null));
        assertFalse(map.equals("not a map"));
        assertFalse(map.entrySet().iterator().hasNext());
        assertFalse(map.keySet().iterator().hasNext());
        assertFalse(map.values().iterator().hasNext());
        assertFalse(map.mapIterator().hasNext());
    }

    @Test(timeout = 4000)
    public void testPutGetSizesUpToThree() {
        Flat3Map map = new Flat3Map();
        assertNull(map.put("a", "1"));
        assertEquals(1, map.size());
        assertFalse(map.isEmpty());

        assertNull(map.put("b", "2"));
        assertEquals(2, map.size());

        assertNull(map.put("c", "3"));
        assertEquals(3, map.size());

        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertEquals("3", map.get("c"));
        assertNull(map.get("missing"));
        assertTrue(map.containsKey("c"));
        assertTrue(map.containsValue("2"));
    }

    @Test(timeout = 4000)
    public void testNullKeyGetFromAllSlots() {
        Flat3Map map3 = new Flat3Map();
        map3.put("a", "A");
        map3.put("b", "B");
        map3.put(null, "C");
        assertEquals("C", map3.get(null));
        assertTrue(map3.containsKey(null));
        assertTrue(map3.containsValue("C"));

        Flat3Map map2 = new Flat3Map();
        map2.put("a", "A");
        map2.put(null, "B");
        map2.put("c", "C");
        assertEquals("B", map2.get(null));

        Flat3Map map1 = new Flat3Map();
        map1.put(null, "A");
        map1.put("b", "B");
        map1.put("c", "C");
        assertEquals("A", map1.get(null));
    }

    @Test(timeout = 4000)
    public void testPutReplaceExistingKeysAllSlots() {
        Flat3Map map = new Flat3Map();
        assertNull(map.put("a", "1"));
        assertNull(map.put("b", "2"));
        assertNull(map.put("c", "3"));

        assertEquals("1", map.put("a", "X"));
        assertEquals("2", map.put("b", "Y"));
        assertEquals("3", map.put("c", "Z"));

        assertEquals("X", map.get("a"));
        assertEquals("Y", map.get("b"));
        assertEquals("Z", map.get("c"));
        assertEquals(3, map.size());
    }

    @Test(timeout = 4000)
    public void testPutReplaceNullKey() {
        Flat3Map map = new Flat3Map();
        assertNull(map.put(null, "v1"));
        assertNull(map.put("a", "A"));
        assertNull(map.put("b", "B"));

        assertEquals("v1", map.put(null, "v2"));
        assertEquals("v2", map.get(null));
        assertEquals(3, map.size());

        Flat3Map map2 = new Flat3Map();
        map2.put("a", "A");
        map2.put(null, "v1");
        assertEquals("v1", map2.put(null, "v2"));
        assertEquals("v2", map2.get(null));
        assertEquals(2, map2.size());
    }

    @Test(timeout = 4000)
    public void testFourthPutConvertsToDelegateMode() {
        Flat3Map map = new Flat3Map();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        assertNull(map.put("d", "4"));

        assertEquals(4, map.size());
        assertFalse(map.isEmpty());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertEquals("3", map.get("c"));
        assertEquals("4", map.get("d"));
        assertTrue(map.containsKey("d"));
        assertTrue(map.containsValue("4"));
        assertEquals(4, map.entrySet().size());
        assertEquals(4, map.keySet().size());
        assertEquals(4, map.values().size());
    }

    @Test(timeout = 4000)
    public void testPutAllEmptyDoesNotChangeState() {
        Flat3Map map = mapOf("a", "1");
        map.putAll(new HashMap());
        assertEquals(1, map.size());
        assertEquals("1", map.get("a"));
    }

    @Test(timeout = 4000)
    public void testPutAllSmallCopiesEntries() {
        Flat3Map source = new Flat3Map();
        source.put("a", "1");
        source.put("b", "2");

        Flat3Map target = new Flat3Map();
        target.putAll(source);

        assertEquals(2, target.size());
        assertEquals("1", target.get("a"));
        assertEquals("2", target.get("b"));
    }

    @Test(timeout = 4000)
    public void testPutAllLargeConvertsToDelegate() {
        Flat3Map source = new Flat3Map();
        source.put("a", "1");
        source.put("b", "2");
        source.put("c", "3");
        source.put("d", "4");
        source.put("e", "5");

        Flat3Map target = new Flat3Map();
        target.putAll(source);

        assertEquals(5, target.size());
        assertEquals("5", target.get("e"));
    }

    @Test(timeout = 4000)
    public void testConstructorWithMapCopiesEntries() {
        Map source = new HashMap();
        source.put("a", "1");
        source.put("b", "2");
        source.put("c", "3");
        source.put("d", "4");

        Flat3Map map = new Flat3Map(source);
        assertEquals(4, map.size());
        assertEquals("4", map.get("d"));
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorWithNullMapThrowsNullPointerException() {
        new Flat3Map(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testPutAllNullThrowsNullPointerException() {
        new Flat3Map().putAll(null);
    }

    @Test(timeout = 4000)
    public void testRemoveFromEmptyReturnsNull() {
        Flat3Map map = new Flat3Map();
        assertNull(map.remove("a"));
        assertNull(map.remove(null));
    }

    @Test(timeout = 4000)
    public void testRemoveLastEntryInThreeSlotMap() {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3");
        assertEquals("3", map.remove("c"));
        assertEquals(2, map.size());
        assertFalse(map.containsKey("c"));
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
    }

    @Test(timeout = 4000)
    public void testRemoveMiddleEntryInThreeSlotMapState() {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3");
        map.remove("b");
        assertEquals(2, map.size());
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("c"));
    }

    @Test(timeout = 4000)
    public void testRemoveFirstEntryInThreeSlotMapState() {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3");
        map.remove("a");
        assertEquals(2, map.size());
        assertFalse(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
    }

    @Test(timeout = 4000)
    public void testRemoveNullKeyFromLastSlot() {
        Flat3Map map = new Flat3Map();
        map.put("a", "A");
        map.put("b", "B");
        map.put(null, "C");
        assertEquals("C", map.remove(null));
        assertEquals(2, map.size());
        assertFalse(map.containsKey(null));
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
    }

    @Test(timeout = 4000)
    public void testRemoveNullKeyFromMiddleSlotState() {
        Flat3Map map = new Flat3Map();
        map.put("a", "A");
        map.put(null, "B");
        map.put("c", "C");
        map.remove(null);
        assertEquals(2, map.size());
        assertFalse(map.containsKey(null));
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("c"));
    }

    @Test(timeout = 4000)
    public void testRemoveNullKeyFromFirstSlotState() {
        Flat3Map map = new Flat3Map();
        map.put(null, "A");
        map.put("b", "B");
        map.put("c", "C");
        map.remove(null);
        assertEquals(2, map.size());
        assertFalse(map.containsKey(null));
        assertTrue(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
    }

    @Test(timeout = 4000)
    public void testRemoveSecondEntryInTwoSlotMap() {
        Flat3Map map = mapOf("a", "1", "b", "2");
        assertEquals("2", map.remove("b"));
        assertEquals(1, map.size());
        assertTrue(map.containsKey("a"));
        assertFalse(map.containsKey("b"));
    }

    @Test(timeout = 4000)
    public void testRemoveFirstEntryInTwoSlotMapState() {
        Flat3Map map = mapOf("a", "1", "b", "2");
        map.remove("a");
        assertEquals(1, map.size());
        assertFalse(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
    }

    @Test(timeout = 4000)
    public void testRemoveSoleEntryReturnsValue() {
        Flat3Map map = mapOf("a", "1");
        assertEquals("1", map.remove("a"));
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testRemoveSoleNullKeyReturnsValue() {
        Flat3Map map = new Flat3Map();
        map.put(null, "nullVal");
        assertEquals("nullVal", map.remove(null));
        assertTrue(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testRemoveMissingKeyReturnsNull() {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3");
        assertNull(map.remove("zzz"));
        assertNull(map.remove(null));
        assertEquals(3, map.size());
    }

    @Test(timeout = 4000)
    public void testClearFlatMode() {
        Flat3Map map = mapOf("a", "1", "b", "2");
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get("a"));
        assertEquals("{}", map.toString());
        map.put("x", "X");
        assertEquals("X", map.get("x"));
    }

    @Test(timeout = 4000)
    public void testClearDelegateModeReturnsToFlatMode() {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3", "d", "4");
        assertEquals(4, map.size());
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        map.put("only", "value");
        assertEquals(1, map.size());
        assertEquals("value", map.get("only"));
    }

    /* ---------- Defect-targeted iterator setValue tests ---------- */

    @Test(timeout = 4000)
    public void testMapIteratorSetValueOnSecondEntryDoesNotModifyFirst() {
        Flat3Map map = mapOf("a", "1", "b", "2");
        MapIterator it = map.mapIterator();
        it.next();
        it.next();
        assertEquals("2", it.setValue("X"));
        assertEquals("1", map.get("a"));
        assertEquals("X", map.get("b"));
    }

    @Test(timeout = 4000)
    public void testMapIteratorSetValueOnThirdEntryDoesNotModifyEarlier() {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3");
        MapIterator it = map.mapIterator();
        it.next();
        it.next();
        it.next();
        assertEquals("3", it.setValue("X"));
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertEquals("X", map.get("c"));
    }

    @Test(timeout = 4000)
    public void testEntrySetIteratorSetValueOnSecondEntryDoesNotModifyFirst() {
        Flat3Map map = mapOf("a", "1", "b", "2");
        Iterator it = map.entrySet().iterator();
        it.next();
        Map.Entry entry = (Map.Entry) it.next();
        assertEquals("2", entry.setValue("X"));
        assertEquals("1", map.get("a"));
        assertEquals("X", map.get("b"));
    }

    @Test(timeout = 4000)
    public void testEntrySetIteratorSetValueOnThirdEntryDoesNotModifyEarlier() {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3");
        Iterator it = map.entrySet().iterator();
        it.next();
        it.next();
        Map.Entry entry = (Map.Entry) it.next();
        assertEquals("3", entry.setValue("X"));
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertEquals("X", map.get("c"));
    }

    @Test(timeout = 4000)
    public void testMapIteratorSetValueReturnsOldValue() {
        Flat3Map map = mapOf("a", "1", "b", "2");
        MapIterator it = map.mapIterator();
        it.next();
        assertEquals("1", it.setValue("A"));
        assertEquals("A", map.get("a"));
        assertEquals("2", map.get("b"));
    }

    @Test(timeout = 4000)
    public void testEntrySetIteratorSetValueReturnsOldValue() {
        Flat3Map map = mapOf("a", "1", "b", "2");
        Iterator it = map.entrySet().iterator();
        Map.Entry entry = (Map.Entry) it.next();
        assertEquals("1", entry.setValue("A"));
        assertEquals("A", map.get("a"));
        assertEquals("2", map.get("b"));
    }

    /* ---------- MapIterator state and lifecycle ---------- */

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testMapIteratorNextPastEndThrowsNoSuchElementException() {
        MapIterator it = mapOf("a", "1").mapIterator();
        it.next();
        it.next();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorGetKeyBeforeNextThrowsIllegalStateException() {
        mapOf("a", "1").mapIterator().getKey();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorGetValueBeforeNextThrowsIllegalStateException() {
        mapOf("a", "1").mapIterator().getValue();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorSetValueBeforeNextThrowsIllegalStateException() {
        mapOf("a", "1").mapIterator().setValue("x");
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testMapIteratorRemoveBeforeNextThrowsIllegalStateException() {
        mapOf("a", "1").mapIterator().remove();
    }

    @Test(timeout = 4000)
    public void testMapIteratorRemoveCurrentEntry() {
        Flat3Map map = mapOf("a", "1", "b", "2");
        MapIterator it = map.mapIterator();
        it.next();
        it.remove();
        assertEquals(1, map.size());
        assertFalse(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertEquals("b", it.next());
    }

    @Test(timeout = 4000)
    public void testMapIteratorReset() {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3");
        MapIterator it = map.mapIterator();
        it.next();
        it.next();
        ((ResettableIterator) it).reset();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("1", it.getValue());
    }

    /* ---------- EntrySet / KeySet / Values views ---------- */

    @Test(timeout = 4000)
    public void testEntrySetIteratorTraversalAndEntryMethods() {
        Flat3Map map = mapOf("a", "1", "b", "2");
        Iterator it = map.entrySet().iterator();
        assertTrue(it.hasNext());

        Map.Entry e1 = (Map.Entry) it.next();
        assertEquals("a", e1.getKey());
        assertEquals("1", e1.getValue());
        e1.setValue("A");
        assertEquals("A", map.get("a"));

        Map.Entry e2 = (Map.Entry) it.next();
        assertEquals("b", e2.getKey());
        assertEquals("2", e2.getValue());
        assertTrue(e2.equals(e2));
        assertFalse(e2.equals(null));
        assertFalse(e2.equals("not entry"));
        assertFalse(e1.equals(e2));
        assertTrue(e1.hashCode() != 0);
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testEntrySetIteratorRemoveCurrentEntry() {
        Flat3Map map = mapOf("a", "1", "b", "2");
        Iterator it = map.entrySet().iterator();
        it.next();
        it.remove();
        assertEquals(1, map.size());
        assertFalse(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testEntrySetNextPastEndThrowsNoSuchElementException() {
        Iterator it = mapOf("a", "1").entrySet().iterator();
        it.next();
        it.next();
    }

    @Test(timeout = 4000)
    public void testEntrySetRemoveInvalidObjectReturnsFalse() {
        Flat3Map map = mapOf("a", "1");
        assertFalse(map.entrySet().remove("notAnEntry"));
        assertEquals(1, map.size());
    }

    @Test(timeout = 4000)
    public void testEntrySetRemoveExistingEntryReturnsTrue() {
        Flat3Map map = mapOf("a", "1", "b", "2");
        Map.Entry entry = new java.util.AbstractMap.SimpleEntry("a", "1");
        assertTrue(map.entrySet().remove(entry));
        assertEquals(1, map.size());
        assertFalse(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
    }

    @Test(timeout = 4000)
    public void testEntrySetClearClearsMap() {
        Flat3Map map = mapOf("a", "1", "b", "2");
        map.entrySet().clear();
        assertTrue(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testKeySetContainsRemoveAndIterator() {
        Flat3Map map = mapOf("a", "1", "b", "2");
        Set keys = map.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("a"));
        assertFalse(keys.contains("zz"));

        Iterator it = keys.iterator();
        assertEquals("a", it.next());
        it.remove();
        assertEquals(1, map.size());
        assertFalse(map.containsKey("a"));
        assertTrue(map.containsKey("b"));

        assertTrue(keys.remove("b"));
        assertTrue(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testKeySetRemoveMissingReturnsFalse() {
        Flat3Map map = mapOf("a", "1");
        assertFalse(map.keySet().remove("zz"));
    }

    @Test(timeout = 4000)
    public void testValuesContainsAndIteratorRemove() {
        Flat3Map map = mapOf("a", "1", "b", "2");
        Collection values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains("1"));
        assertFalse(values.contains("zz"));

        Iterator it = values.iterator();
        assertEquals("1", it.next());
        it.remove();
        assertEquals(1, map.size());
        assertFalse(map.containsKey("a"));
        assertTrue(map.containsKey("b"));

        values.clear();
        assertTrue(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testViewsInDelegateMode() {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3", "d", "4");
        assertEquals(4, map.entrySet().size());
        assertEquals(4, map.keySet().size());
        assertEquals(4, map.values().size());

        Iterator it = map.entrySet().iterator();
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(4, count);
    }

    /* ---------- Hash collision branch ---------- */

    private static class CollidingKey {
        private final String id;

        CollidingKey(String id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return 42;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CollidingKey)) {
                return false;
            }
            return id.equals(((CollidingKey) obj).id);
        }
    }

    @Test(timeout = 4000)
    public void testHashCollisionDoesNotMatchWrongKey() {
        Flat3Map map = new Flat3Map();
        CollidingKey a = new CollidingKey("a");
        CollidingKey b = new CollidingKey("b");

        assertNull(map.put(a, "A"));
        assertNull(map.get(b));
        assertFalse(map.containsKey(b));

        assertNull(map.put(b, "B"));
        assertEquals(2, map.size());
        assertEquals("A", map.get(a));
        assertEquals("B", map.get(b));

        assertEquals("A", map.put(a, "A2"));
        assertEquals("A2", map.get(a));
        assertEquals("B", map.get(b));
    }

    /* ---------- equals / hashCode / toString / clone / serialization ---------- */

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeAgainstHashMap() {
        Flat3Map map = mapOf("a", "1", "b", "2");
        Map other = new HashMap();
        other.put("a", "1");
        other.put("b", "2");

        assertTrue(map.equals(other));
        assertTrue(other.equals(map));
        assertEquals(other.hashCode(), map.hashCode());

        other.put("c", "3");
        assertFalse(map.equals(other));
        assertFalse(map.equals(mapOf("a", "1", "b", "WRONG")));
    }

    @Test(timeout = 4000)
    public void testEqualsDelegateModeAgainstHashMap() {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3", "d", "4");
        Map other = new HashMap();
        other.put("a", "1");
        other.put("b", "2");
        other.put("c", "3");
        other.put("d", "4");

        assertTrue(map.equals(other));
        assertTrue(other.equals(map));
        assertEquals(other.hashCode(), map.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsDetectsKeyAndValueDifferences() {
        Flat3Map map = mapOf("a", "1", "b", "2");
        assertFalse(map.equals(mapOf("a", "1", "b", "3")));
        assertFalse(map.equals(mapOf("a", "1", "c", "2")));
        assertFalse(map.equals(mapOf("a", "1")));

        Flat3Map bigger = mapOf("a", "1", "b", "2", "c", "3");
        assertFalse(map.equals(bigger));
    }

    @Test(timeout = 4000)
    public void testHashCodeWithNullKeyAndValue() {
        Flat3Map map = new Flat3Map();
        map.put(null, "v");
        map.put("a", null);

        Map other = new HashMap();
        other.put(null, "v");
        other.put("a", null);

        assertEquals(other.hashCode(), map.hashCode());
        assertTrue(map.equals(other));
        assertTrue(other.equals(map));
    }

    @Test(timeout = 4000)
    public void testToStringFlatAndSelfReference() {
        assertEquals("{}", new Flat3Map().toString());
        assertEquals("{a=1}", mapOf("a", "1").toString());
        assertEquals("{b=2,a=1}", mapOf("a", "1", "b", "2").toString());
        assertEquals("{c=3,b=2,a=1}", mapOf("a", "1", "b", "2", "c", "3").toString());

        Flat3Map selfValue = new Flat3Map();
        selfValue.put("k", selfValue);
        assertTrue(selfValue.toString().contains("(this Map)"));

        Flat3Map selfKey = new Flat3Map();
        selfKey.put(selfKey, "v");
        assertTrue(selfKey.toString().contains("(this Map)"));
    }

    @Test(timeout = 4000)
    public void testToStringDelegateMode() {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3", "d", "4");
        String s = map.toString();
        assertTrue(s.startsWith("{"));
        assertTrue(s.endsWith("}"));
        assertTrue(s.contains("a=1"));
    }

    @Test(timeout = 4000)
    public void testCloneFlatModeIsShallowIndependent() {
        Flat3Map original = mapOf("a", "1", "b", "2");
        Flat3Map clone = (Flat3Map) original.clone();
        assertEquals(original, clone);
        assertNotSame(original, clone);

        clone.put("a", "X");
        assertEquals("1", original.get("a"));
        assertEquals("X", clone.get("a"));
    }

    @Test(timeout = 4000)
    public void testCloneDelegateModeIsIndependent() {
        Flat3Map original = mapOf("a", "1", "b", "2", "c", "3", "d", "4");
        Flat3Map clone = (Flat3Map) original.clone();
        assertNotSame(original, clone);
        assertEquals(4, clone.size());
        assertEquals("1", clone.get("a"));
        assertEquals("4", clone.get("d"));

        clone.put("e", "5");
        assertEquals(4, original.size());
        assertEquals(5, clone.size());
    }

    @Test(timeout = 4000)
    public void testSerializationFlatMode() throws Exception {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3");
        Flat3Map copy = roundTrip(map);
        assertEquals(map, copy);
        assertEquals(map.size(), copy.size());
        assertEquals("2", copy.get("b"));
    }

    @Test(timeout = 4000)
    public void testSerializationDelegateMode() throws Exception {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3", "d", "4");
        Flat3Map copy = roundTrip(map);
        assertEquals(4, copy.size());
        assertEquals("1", copy.get("a"));
        assertEquals("4", copy.get("d"));
    }

    /* ---------- Additional value / delegate coverage ---------- */

    @Test(timeout = 4000)
    public void testContainsNullValueInAllSlots() {
        Flat3Map map = new Flat3Map();
        map.put("a", null);
        assertTrue(map.containsValue(null));
        map.put("b", "B");
        map.put("c", "C");
        assertTrue(map.containsValue(null));

        Flat3Map map2 = new Flat3Map();
        map2.put("a", "A");
        map2.put("b", null);
        assertTrue(map2.containsValue(null));

        Flat3Map map3 = new Flat3Map();
        map3.put("a", "A");
        map3.put("b", "B");
        map3.put("c", null);
        assertTrue(map3.containsValue(null));
    }

    @Test(timeout = 4000)
    public void testContainsValueNonNullInAllSlots() {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3");
        assertTrue(map.containsValue("1"));
        assertTrue(map.containsValue("2"));
        assertTrue(map.containsValue("3"));
        assertFalse(map.containsValue("4"));
    }

    @Test(timeout = 4000)
    public void testPutInDelegateModeReplacesAndReturnsOld() {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3", "d", "4");
        assertEquals("1", map.put("a", "X"));
        assertEquals("X", map.get("a"));
        assertNull(map.put("e", "5"));
        assertEquals(5, map.size());
        assertEquals("5", map.get("e"));
    }

    @Test(timeout = 4000)
    public void testRemoveInDelegateMode() {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3", "d", "4");
        assertEquals("1", map.remove("a"));
        assertEquals(3, map.size());
        assertFalse(map.containsKey("a"));
    }

    @Test(timeout = 4000)
    public void testPutAllInDelegateModeMerges() {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3", "d", "4");
        Map extra = new HashMap();
        extra.put("e", "5");
        extra.put("f", "6");
        map.putAll(extra);
        assertEquals(6, map.size());
        assertEquals("5", map.get("e"));
        assertEquals("6", map.get("f"));
    }

    @Test(timeout = 4000)
    public void testMapIteratorIteratesDelegateMode() {
        Flat3Map map = mapOf("a", "1", "b", "2", "c", "3", "d", "4");
        MapIterator it = map.mapIterator();
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(4, count);
    }
}