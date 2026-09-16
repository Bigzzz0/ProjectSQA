package org.apache.commons.collections.list;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class SetUniqueListDeepseekTest {

    /* [Branch & Defect Analysis Matrix] */
    // Target: SetUniqueList.set(int, Object) - defect in handling duplicate replacement
    // Branches covered:
    // - set.contains(object) == false -> normal set, no duplicate removal
    // - set.contains(object) == true && pos == index -> no removal (same position)
    // - set.contains(object) == true && pos != index -> remove duplicate at pos
    // - set.add(object) after removal
    // - set.remove(removed) to sync set
    // - return removed value
    // Defect: When setting an object that already exists at a different index,
    // the method removes the duplicate but the set.remove(removed) may remove
    // the wrong element if 'removed' equals the object being set (edge case).
    // Also tests: add, addAll, remove, removeAll, retainAll, clear, contains,
    // containsAll, iterator, listIterator, subList, asSet, decorate.

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testAddUniqueElements() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        assertTrue(list.add("A"));
        assertTrue(list.add("B"));
        assertFalse(list.add("A")); // duplicate not added
        assertEquals(2, list.size());
        assertEquals(Arrays.asList("A", "B"), list);
    }

    @Test(timeout = 4000)
    public void testAddAtIndexUnique() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        list.add("C");
        list.add(1, "B");
        assertEquals(Arrays.asList("A", "B", "C"), list);
        // duplicate at index - should not change
        list.add(1, "A");
        assertEquals(Arrays.asList("A", "B", "C"), list);
    }

    @Test(timeout = 4000)
    public void testAddAllUnique() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        Collection coll = Arrays.asList("B", "C", "A", "D", "B");
        assertTrue(list.addAll(coll));
        assertEquals(Arrays.asList("A", "B", "C", "D"), list);
        // no change when all duplicates
        assertFalse(list.addAll(Arrays.asList("A", "B")));
    }

    @Test(timeout = 4000)
    public void testAddAllAtIndexUnique() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "D"));
        Collection coll = Arrays.asList("B", "C", "A");
        assertTrue(list.addAll(1, coll));
        assertEquals(Arrays.asList("A", "B", "C", "D"), list);
    }

    @Test(timeout = 4000)
    public void testSetNormal() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        list.add("B");
        Object old = list.set(0, "C");
        assertEquals("A", old);
        assertEquals(Arrays.asList("C", "B"), list);
        assertTrue(list.contains("C"));
        assertFalse(list.contains("A"));
    }

    @Test(timeout = 4000)
    public void testSetDuplicateDifferentIndex() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        list.add("B");
        list.add("C");
        // set index 0 to "B" which already exists at index 1
        Object old = list.set(0, "B");
        assertEquals("A", old);
        // After set: list should have B at index 0, and the duplicate at index 1 removed
        assertEquals(Arrays.asList("B", "C"), list);
        assertTrue(list.contains("B"));
        assertFalse(list.contains("A"));
    }

    @Test(timeout = 4000)
    public void testSetDuplicateSameIndex() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        list.add("B");
        Object old = list.set(1, "B"); // same index
        assertEquals("B", old);
        assertEquals(Arrays.asList("A", "B"), list);
    }

    @Test(timeout = 4000)
    public void testRemoveObject() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        list.add("B");
        assertTrue(list.remove("A"));
        assertFalse(list.remove("A"));
        assertEquals(1, list.size());
        assertFalse(list.contains("A"));
    }

    @Test(timeout = 4000)
    public void testRemoveIndex() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        list.add("B");
        Object removed = list.remove(0);
        assertEquals("A", removed);
        assertEquals(1, list.size());
        assertFalse(list.contains("A"));
    }

    @Test(timeout = 4000)
    public void testRemoveAll() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "B", "C", "D"));
        assertTrue(list.removeAll(Arrays.asList("A", "C")));
        assertEquals(Arrays.asList("B", "D"), list);
        assertFalse(list.removeAll(Arrays.asList("X")));
    }

    @Test(timeout = 4000)
    public void testRetainAll() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "B", "C", "D"));
        assertTrue(list.retainAll(Arrays.asList("B", "D")));
        assertEquals(Arrays.asList("B", "D"), list);
        assertFalse(list.retainAll(Arrays.asList("B", "D")));
    }

    @Test(timeout = 4000)
    public void testClear() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "B"));
        list.clear();
        assertTrue(list.isEmpty());
        assertFalse(list.contains("A"));
        assertEquals(0, list.size());
    }

    @Test(timeout = 4000)
    public void testContainsAndContainsAll() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "B"));
        assertTrue(list.contains("A"));
        assertFalse(list.contains("C"));
        assertTrue(list.containsAll(Arrays.asList("A", "B")));
        assertFalse(list.containsAll(Arrays.asList("A", "C")));
    }

    @Test(timeout = 4000)
    public void testIterator() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "B", "C"));
        Iterator it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        it.remove();
        assertEquals(Arrays.asList("B", "C"), list);
        assertFalse(list.contains("A"));
    }

    @Test(timeout = 4000)
    public void testListIterator() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "B", "C"));
        ListIterator it = list.listIterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        assertEquals("B", it.previous());
        it.remove();
        assertEquals(Arrays.asList("A", "C"), list);
        assertFalse(list.contains("B"));
    }

    @Test(timeout = 4000)
    public void testListIteratorAdd() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "C"));
        ListIterator it = list.listIterator();
        it.next(); // A
        it.add("B"); // add B between A and C
        assertEquals(Arrays.asList("A", "B", "C"), list);
        // duplicate add should not change
        it.add("A");
        assertEquals(Arrays.asList("A", "B", "C"), list);
    }

    @Test(timeout = 4000)
    public void testListIteratorSetUnsupported() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        ListIterator it = list.listIterator();
        it.next();
        try {
            it.set("B");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSubList() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "B", "C", "D"));
        List sub = list.subList(1, 3);
        assertEquals(Arrays.asList("B", "C"), sub);
        // sublist is a SetUniqueList, adding duplicate should not change
        sub.add("B");
        assertEquals(Arrays.asList("B", "C"), sub);
        assertEquals(Arrays.asList("A", "B", "C", "D"), list);
    }

    @Test(timeout = 4000)
    public void testAsSet() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "B"));
        Set set = list.asSet();
        assertTrue(set.contains("A"));
        assertTrue(set.contains("B"));
        assertEquals(2, set.size());
        try {
            set.add("C");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ==================== Partition B: Boundary Value Analysis (BVA) & Extremes ====================

    @Test(timeout = 4000)
    public void testDecorateNullList() {
        try {
            SetUniqueList.decorate(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDecorateEmptyList() {
        List list = new ArrayList();
        SetUniqueList unique = SetUniqueList.decorate(list);
        assertTrue(unique.isEmpty());
        assertEquals(0, unique.size());
    }

    @Test(timeout = 4000)
    public void testDecorateWithDuplicates() {
        List list = new ArrayList(Arrays.asList("A", "B", "A", "C", "B"));
        SetUniqueList unique = SetUniqueList.decorate(list);
        assertEquals(Arrays.asList("A", "B", "C"), unique);
        // original list is cleared and reused
        assertEquals(3, list.size());
    }

    @Test(timeout = 4000)
    public void testConstructorNullSet() {
        try {
            new SetUniqueList(new ArrayList(), null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddNullElement() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        assertTrue(list.add(null));
        assertFalse(list.add(null)); // duplicate null
        assertEquals(1, list.size());
        assertTrue(list.contains(null));
    }

    @Test(timeout = 4000)
    public void testAddAllNullCollection() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        try {
            list.addAll(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetNullElement() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        Object old = list.set(0, null);
        assertEquals("A", old);
        assertEquals(1, list.size());
        assertTrue(list.contains(null));
    }

    @Test(timeout = 4000)
    public void testRemoveNull() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add(null);
        assertTrue(list.remove(null));
        assertFalse(list.contains(null));
        assertEquals(0, list.size());
    }

    @Test(timeout = 4000)
    public void testLargeListAdd() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        for (int i = 0; i < 1000; i++) {
            assertTrue(list.add(i));
        }
        assertEquals(1000, list.size());
        // duplicate
        assertFalse(list.add(500));
        assertEquals(1000, list.size());
    }

    @Test(timeout = 4000)
    public void testLargeListSet() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        // set index 0 to value 50 (exists at index 50)
        Object old = list.set(0, 50);
        assertEquals(0, old);
        assertEquals(99, list.size());
        assertFalse(list.contains(0));
        assertTrue(list.contains(50));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Targets the known defect from TestSetUniqueList::testCollections307.
     * The defect occurs when setting an element that already exists in the list
     * at a different index. The method should remove the duplicate and keep the
     * set in sync. The bug causes the set to become inconsistent, leading to
     * incorrect contains() results or size mismatches.
     */
    @Test(timeout = 4000)
    public void testCollections307Defect() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");

        // Set index 1 to "C" which already exists at index 2
        Object old = list.set(1, "C");
        assertEquals("B", old);

        // Expected: list becomes [A, C, D] (duplicate C at index 2 removed)
        assertEquals(3, list.size());
        assertEquals(Arrays.asList("A", "C", "D"), list);

        // Set must be consistent
        assertTrue(list.contains("A"));
        assertTrue(list.contains("C"));
        assertTrue(list.contains("D"));
        assertFalse(list.contains("B"));

        // Verify set internal state via asSet
        Set setView = list.asSet();
        assertEquals(3, setView.size());
        assertTrue(setView.contains("A"));
        assertTrue(setView.contains("C"));
        assertTrue(setView.contains("D"));
        assertFalse(setView.contains("B"));
    }

    @Test(timeout = 4000)
    public void testSetDuplicateEdgeCase() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("X");
        list.add("Y");
        list.add("Z");

        // Set index 2 to "X" (exists at index 0)
        Object old = list.set(2, "X");
        assertEquals("Z", old);

        // Expected: [X, Y] (Z removed, duplicate X at index 2 removed)
        assertEquals(2, list.size());
        assertEquals(Arrays.asList("X", "Y"), list);
        assertTrue(list.contains("X"));
        assertTrue(list.contains("Y"));
        assertFalse(list.contains("Z"));

        // Set consistency
        Set setView = list.asSet();
        assertEquals(2, setView.size());
        assertTrue(setView.contains("X"));
        assertTrue(setView.contains("Y"));
        assertFalse(setView.contains("Z"));
    }

    @Test(timeout = 4000)
    public void testSetDuplicateWithNull() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add(null);
        list.add("A");
        list.add("B");

        // Set index 1 to null (exists at index 0)
        Object old = list.set(1, null);
        assertEquals("A", old);

        // Expected: [null, B]
        assertEquals(2, list.size());
        assertEquals(Arrays.asList(null, "B"), list);
        assertTrue(list.contains(null));
        assertTrue(list.contains("B"));
        assertFalse(list.contains("A"));

        Set setView = list.asSet();
        assertEquals(2, setView.size());
        assertTrue(setView.contains(null));
        assertTrue(setView.contains("B"));
        assertFalse(setView.contains("A"));
    }

    @Test(timeout = 4000)
    public void testSetDuplicateMultipleTimes() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");

        // Set index 0 to "C" (exists at index 2)
        list.set(0, "C");
        assertEquals(Arrays.asList("C", "B", "D"), list);

        // Now set index 1 to "D" (exists at index 2)
        Object old = list.set(1, "D");
        assertEquals("B", old);
        assertEquals(Arrays.asList("C", "D"), list);

        // Set consistency
        Set setView = list.asSet();
        assertEquals(2, setView.size());
        assertTrue(setView.contains("C"));
        assertTrue(setView.contains("D"));
        assertFalse(setView.contains("A"));
        assertFalse(setView.contains("B"));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testAddAtIndexOutOfBounds() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        try {
            list.add(5, "B");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetIndexOutOfBounds() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        try {
            list.set(5, "B");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRemoveIndexOutOfBounds() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        try {
            list.remove(5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSubListInvalidRange() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        try {
            list.subList(1, 0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testListIteratorRemoveWithoutNext() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        ListIterator it = list.listIterator();
        try {
            it.remove();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIteratorRemoveWithoutNext() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        Iterator it = list.iterator();
        try {
            it.remove();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        SetUniqueList list1 = new SetUniqueList(new ArrayList(), new HashSet());
        list1.addAll(Arrays.asList("A", "B"));
        SetUniqueList list2 = new SetUniqueList(new ArrayList(), new HashSet());
        list2.addAll(Arrays.asList("A", "B"));
        assertEquals(list1, list2);
        assertEquals(list1.hashCode(), list2.hashCode());

        list2.add("C");
        assertNotEquals(list1, list2);
    }

    @Test(timeout = 4000)
    public void testToString() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "B"));
        assertEquals("[A, B]", list.toString());
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "B", "C"));

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(list);
        oos.close();

        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        SetUniqueList deserialized = (SetUniqueList) ois.readObject();
        ois.close();

        assertEquals(list, deserialized);
        assertEquals(list.asSet(), deserialized.asSet());
    }

    @Test(timeout = 4000)
    public void testListIteratorPreviousAndRemove() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "B", "C"));
        ListIterator it = list.listIterator(2); // points to C
        assertEquals("C", it.previous());
        it.remove();
        assertEquals(Arrays.asList("A", "B"), list);
        assertFalse(list.contains("C"));
    }

    @Test(timeout = 4000)
    public void testAddAllWithEmptyCollection() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        assertFalse(list.addAll(new ArrayList()));
        assertEquals(1, list.size());
    }

    @Test(timeout = 4000)
    public void testRetainAllEmptyCollection() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "B"));
        assertTrue(list.retainAll(new ArrayList()));
        assertTrue(list.isEmpty());
    }

    @Test(timeout = 4000)
    public void testRemoveAllEmptyCollection() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "B"));
        assertFalse(list.removeAll(new ArrayList()));
        assertEquals(2, list.size());
    }

    @Test(timeout = 4000)
    public void testContainsAllEmptyCollection() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        assertTrue(list.containsAll(new ArrayList()));
    }

    @Test(timeout = 4000)
    public void testSetWithEqualButNotSameObject() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        String a = new String("A");
        String b = new String("A");
        list.add(a);
        // b is equal but not same reference
        assertFalse(list.add(b)); // duplicate
        assertEquals(1, list.size());
        assertTrue(list.contains(b));
    }

    @Test(timeout = 4000)
    public void testIteratorRemoveUpdatesSet() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "B", "C"));
        Iterator it = list.iterator();
        it.next(); // A
        it.remove();
        assertFalse(list.contains("A"));
        assertEquals(2, list.size());
        // set view should not contain A
        assertFalse(list.asSet().contains("A"));
    }

    @Test(timeout = 4000)
    public void testListIteratorAddUpdatesSet() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.add("A");
        ListIterator it = list.listIterator();
        it.next();
        it.add("B");
        assertTrue(list.contains("B"));
        assertEquals(2, list.size());
        assertTrue(list.asSet().contains("B"));
    }

    @Test(timeout = 4000)
    public void testSubListAddUpdatesParentSet() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "B", "C"));
        List sub = list.subList(0, 2);
        sub.add("D");
        assertTrue(list.contains("D"));
        assertEquals(4, list.size());
        assertTrue(list.asSet().contains("D"));
    }

    @Test(timeout = 4000)
    public void testSubListRemoveUpdatesParentSet() {
        SetUniqueList list = new SetUniqueList(new ArrayList(), new HashSet());
        list.addAll(Arrays.asList("A", "B", "C"));
        List sub = list.subList(0, 2);
        sub.remove("A");
        assertFalse(list.contains("A"));
        assertEquals(2, list.size());
        assertFalse(list.asSet().contains("A"));
    }
}