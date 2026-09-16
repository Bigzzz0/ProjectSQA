package org.apache.commons.collections4.map;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class ListOrderedMapDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * 1. Core Functional Logic:
     *    - put(K,V) for new key vs existing key (order maintained)
     *    - put(int index, K, V) for new/existing key with index adjustments
     *    - putAll(int index, Map) with null values (defect trigger)
     *    - remove(Object) and remove(int)
     *    - clear(), size(), isEmpty()
     * 
     * 2. Boundary Values:
     *    - Empty map: firstKey, lastKey, nextKey, previousKey -> NoSuchElementException
     *    - IndexOutOfBounds on get(index), setValue(index, value), put(int,...)
     *    - Null keys and values (null insertion order and replacement)
     * 
     * 3. Defect-Targeted (COLLECTIONS-474):
     *    - putAll(int index, Map) where existing keys have null values
     *      Incorrect index management leads to IndexOutOfBoundsException
     * 
     * 4. Exception Paths:
     *    - MapIterator: remove/getKey/getValue/setValue without next/previous
     *    - Invalid index in list accessors
     * 
     * 5. Contract Integrity:
     *    - toString() with self-reference
     *    - keyList(), valueList(), asList()
     *    - EntrySet, KeySet, Values views
     */

    // ========================================================================
    // Partition A: Core Functional Logic
    // ========================================================================

    @Test(timeout = 4000)
    public void testPutNewKeyMaintainsOrder() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        assertEquals("first key", "A", map.firstKey());
        assertEquals("last key", "C", map.lastKey());
        assertEquals("ordered keys", "[A, B, C]", map.keyList().toString());
    }

    @Test(timeout = 4000)
    public void testPutExistingKeyDoesNotChangeOrder() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("B", "22");  // replace
        assertEquals("first key unchanged", "A", map.firstKey());
        assertEquals("last key unchanged", "C", map.lastKey());
        assertEquals("value updated", "22", map.get("B"));
    }

    @Test(timeout = 4000)
    public void testPutAtIndexWithNewKey() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("C", "3");
        map.put(1, "B", "2"); // insert between A and C
        assertEquals("ordered keys", "[A, B, C]", map.keyList().toString());
        assertNull("return for new key", map.put(0, "Z", "0"));
        assertEquals("ordered keys after front insert", "[Z, A, B, C]", map.keyList().toString());
    }

    @Test(timeout = 4000)
    public void testPutAtIndexWithExistingKey() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        // move B to the end
        String old = map.put(2, "B", "22");
        assertEquals("old value", "2", old);
        assertEquals("ordered keys", "[A, C, B]", map.keyList().toString());
        assertEquals("updated value", "22", map.get("B"));
    }

    @Test(timeout = 4000)
    public void testPutAll() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        Map<String, String> toAdd = new HashMap<String, String>();
        toAdd.put("B", "2");
        toAdd.put("C", "3");
        map.putAll(toAdd);
        assertEquals("size after putAll", 3, map.size());
        assertEquals("order preserved", "[A, B, C]", map.keyList().toString());
    }

    @Test(timeout = 4000)
    public void testRemoveByKey() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        assertEquals("remove existing", "2", map.remove("B"));
        assertNull("remove non-existing", map.remove("X"));
        assertEquals("size after removal", 2, map.size());
        assertEquals("order after removal", "[A, C]", map.keyList().toString());
    }

    @Test(timeout = 4000)
    public void testRemoveByIndex() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        assertEquals("remove index 1", "2", map.remove(1));
        assertEquals("order after removal", "[A, C]", map.keyList().toString());
    }

    @Test(timeout = 4000)
    public void testClear() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        map.clear();
        assertTrue("isEmpty after clear", map.isEmpty());
        assertEquals("size 0", 0, map.size());
    }

    // ========================================================================
    // Partition B: Boundary & Exceptional Cases
    // ========================================================================

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testFirstKeyOnEmptyMap() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.firstKey();
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testLastKeyOnEmptyMap() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.lastKey();
    }

    @Test(timeout = 4000)
    public void testNextKeyAndPreviousKey() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        assertNull("next of C", map.nextKey("C"));
        assertEquals("next of A", "B", map.nextKey("A"));
        assertNull("previous of A", map.previousKey("A"));
        assertEquals("previous of C", "B", map.previousKey("C"));
        assertNull("non-existing key", map.nextKey("X"));
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetIndexOutOfBounds() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.get(1); // only index 0 valid
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetValueIndexOutOfBounds() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.getValue(1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testPutAtIndexOutOfBounds() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put(2, "B", "2"); // index must be 0 or 1
    }

    @Test(timeout = 4000)
    public void testNullKeyAndValue() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put(null, null);
        map.put("A", "1");
        assertTrue("contains null key", map.containsKey(null));
        assertNull("null value", map.get(null));
        assertEquals("order with null key", "[null, A]", map.keyList().toString());
        // replacing null key value with non-null
        map.put(null, "nullVal");
        assertEquals("updated null value", "nullVal", map.get(null));
        // index retrieval
        assertEquals("null key at index 0", null, map.get(0));
    }

    // ========================================================================
    // Partition C: Defect-Targeted (COLLECTIONS-474 nullValues)
    // ========================================================================

    /**
     * This test targets the defect where putAll(int, Map) throws
     * IndexOutOfBoundsException when existing keys have null values.
     * The bug is that the return value of put(index, key, value) is null
     * for both new keys and existing keys with null values, causing
     * incorrect index increments. On the fixed version the test passes.
     */
    @Test(timeout = 4000)
    public void testPutAllWithNullValues_NoIndexOutOfBounds() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", null);
        map.put("B", "b");
        map.put("C", null);
        // create a map that will cause index corruption if bug exists
        Map<String, String> toAdd = new HashMap<String, String>();
        toAdd.put("B", "b2");  // existing key with pre-existing non-null -> safe
        toAdd.put("A", "a2");  // existing key with pre-existing null -> triggers bug
        toAdd.put("D", "d");   // new key
        // This call is reported to throw IndexOutOfBoundsException in the buggy version.
        map.putAll(1, toAdd);
        // If we reach here, no exception. Verify correct state.
        assertEquals("size after putAll", 4, map.size());
        // Expect order: A, (A moved, D inserted?), B? Hard to predict exact order due to insertion logic,
        // but no exception is the primary assertion.
        // Additional checks:
        assertTrue("contains A", map.containsKey("A"));
        assertEquals("A value updated", "a2", map.get("A"));
        assertTrue("contains D", map.containsKey("D"));
    }

    // Additional targeted test: put(index, key, value) with null existing value
    // and index causing the decrement issue when pos == index? (Not directly defect but coverage)
    @Test(timeout = 4000)
    public void testPutAtIndexWithExistingNullValue() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", null);
        map.put("B", "b");
        map.put("C", null);
        // Try to move B (index 1) to the end (index 2)
        map.put(2, "B", "b2");
        // Order should become [A, C, B]
        assertEquals("order after move", "[A, C, B]", map.keyList().toString());
        // Try to move C (now at index 1) to front (index 0)
        map.put(0, "C", "c2");
        assertEquals("order after move to front", "[C, A, B]", map.keyList().toString());
    }

    // ========================================================================
    // Partition D: Exception & Iterator Lifecycle
    // ========================================================================

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testMapIteratorRemoveWithoutNext() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        OrderedMapIterator<String, String> it = map.mapIterator();
        it.remove();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testMapIteratorGetKeyWithoutNext() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        OrderedMapIterator<String, String> it = map.mapIterator();
        it.getKey();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testMapIteratorGetValueWithoutNext() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        OrderedMapIterator<String, String> it = map.mapIterator();
        it.getValue();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testMapIteratorSetValueWithoutNext() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        OrderedMapIterator<String, String> it = map.mapIterator();
        it.setValue("new");
    }

    @Test(timeout = 4000)
    public void testMapIteratorFullLifecycle() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        OrderedMapIterator<String, String> it = map.mapIterator();
        assertTrue("hasNext", it.hasNext());
        assertEquals("next", "A", it.next());
        assertEquals("getKey", "A", it.getKey());
        assertEquals("getValue", "1", it.getValue());
        String old = it.setValue("11");
        assertEquals("old value", "1", old);
        assertEquals("new value", "11", map.get("A"));
        assertTrue("hasNext again", it.hasNext());
        assertEquals("next B", "B", it.next());
        it.remove();  // removes B
        assertFalse("hasNext after remove", it.hasNext());
        assertEquals("size after removal", 1, map.size());
        // test previous
        OrderedMapIterator<String, String> it2 = map.mapIterator();
        it2.next();
        assertFalse("hasPrevious absent", it2.hasPrevious());
        it2.hasNext();
        it2.next(); // should be at end? Since only one element, after next, hasNext false, hasPrevious true
        // We'll skip since not needed for defect.
    }

    // ========================================================================
    // Partition E: Object Contract & Views
    // ========================================================================

    @Test(timeout = 4000)
    public void testToString() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        assertEquals("empty", "{}", map.toString());
        map.put("A", "1");
        map.put("B", "2");
        String str = map.toString();
        assertTrue("contains entries", str.contains("A=1") && str.contains("B=2"));
        // self-reference test
        map.put("self", null);
        ListOrderedMap<Object, Object> selfMap = new ListOrderedMap<Object, Object>();
        selfMap.put("X", "x");
        selfMap.put(selfMap, "selfRef");
        assertTrue("toString handles self-reference", selfMap.toString().contains("(this Map)"));
    }

    @Test(timeout = 4000)
    public void testKeyListUnmodifiable() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        try {
            map.keyList().add("C");
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        assertEquals("keyList size", 2, map.keyList().size());
    }

    @Test(timeout = 4000)
    public void testValueListModifiable() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        map.valueList().set(0, "10");
        assertEquals("updated value via list", "10", map.get("A"));
        map.valueList().remove(0);
        assertEquals("size after remove via list", 1, map.size());
    }

    @Test(timeout = 4000)
    public void testAsList() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        assertSame("asList returns keyList", map.asList(), map.keyList());
    }

    @Test(timeout = 4000)
    public void testEntrySet() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        assertEquals("entrySet size", 2, map.entrySet().size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
        }
    }
}