package org.apache.commons.collections.list;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target Class: org.apache.commons.collections.list.SetUniqueList
 *
 * Partition A: Core Functional Logic & State Transitions
 * - decorate(List): empty list branch vs non-empty list with duplicate purging.
 * - add(Object) / add(int, Object): unique additions vs duplicate rejections.
 * - addAll(Collection) / addAll(int, Collection): sizeBefore comparison, index increment logic.
 * - remove(Object) / remove(int): dual removal from list & backing set.
 * - removeAll(Collection) / retainAll(Collection) / clear(): set/list consistency.
 * - contains(Object) / containsAll(Collection): backing set delegation.
 * - asSet(): returns UnmodifiableSet view backed by internal set.
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - Null element handling (add, contains, remove with null values).
 * - Empty collections passed to addAll, removeAll, retainAll, containsAll.
 * - List boundary insertions (index 0, middle, size()).
 * - Swapping element with itself via set(index, object) where pos == index.
 *
 * Partition C: Defect-Targeted Branch Zone (COLLECTIONS-304)
 * - set(int, Object):
 *   Bug: When replacing an existing element with a new element (pos == -1),
 *        the code returns early without adding the new element to the set,
 *        and without removing the old element from the set.
 *   Consequence: Subsequent add(newObject) falsely succeeds, producing duplicates
 *        in SetUniqueList (size becomes 4 instead of 3).
 *   Target Test: testCollections304() exposes this defect.
 *
 * Partition D: Exception & Defensive Guard Paths
 * - decorate(null) -> IllegalArgumentException("List must not be null")
 * - SetUniqueList(list, null) -> IllegalArgumentException("Set must not be null")
 * - SetUniqueList(null, set) -> IllegalArgumentException (from super decorator)
 * - ListIterator.set(Object) -> UnsupportedOperationException
 * - asSet().add(...) -> UnsupportedOperationException
 *
 * Partition E: Object Lifecycle & Contract Integrity
 * - SetListIterator and SetListListIterator: next(), previous(), remove(), add()
 * - subList(fromIndex, toIndex): uniqueness enforcement in sublists.
 * - Full serialization / deserialization roundtrip.
 * =========================================================================
 */
public class SetUniqueListGeminiTest {

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDecorateEmptyList() {
        List base = new ArrayList();
        SetUniqueList uniqueList = SetUniqueList.decorate(base);
        assertTrue(uniqueList.isEmpty());
        assertEquals(0, uniqueList.size());
    }

    @Test(timeout = 4000)
    public void testDecorateNonEmptyListWithDuplicates() {
        List base = new ArrayList();
        base.add("A");
        base.add("B");
        base.add("A");
        base.add("C");
        base.add("B");

        SetUniqueList uniqueList = SetUniqueList.decorate(base);
        assertEquals(3, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
        assertEquals("C", uniqueList.get(2));
    }

    @Test(timeout = 4000)
    public void testAddReturnsTrueWhenUniqueAndFalseWhenDuplicate() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        assertTrue(list.add("First"));
        assertEquals(1, list.size());

        assertFalse(list.add("First"));
        assertEquals(1, list.size());
        assertTrue(list.contains("First"));
    }

    @Test(timeout = 4000)
    public void testAddAtIndexIgnoresDuplicates() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add(0, "A");
        list.add(1, "B");
        list.add(1, "C"); // ["A", "C", "B"]

        assertEquals(3, list.size());
        assertEquals("C", list.get(1));

        // Attempt to insert duplicate "A" at index 1
        list.add(1, "A");
        assertEquals(3, list.size());
        assertEquals("C", list.get(1));
    }

    @Test(timeout = 4000)
    public void testAddAllToEnd() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");

        List toAdd = Arrays.asList("B", "A", "C", "B");
        boolean modified = list.addAll(toAdd);

        assertTrue(modified);
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));

        // AddAll with all duplicates
        assertFalse(list.addAll(Arrays.asList("A", "C")));
        assertEquals(3, list.size());
    }

    @Test(timeout = 4000)
    public void testAddAllAtIndexWithInterleavedDuplicates() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("D");

        // Insert at index 1: "B" (new), "A" (duplicate), "C" (new)
        List toInsert = Arrays.asList("B", "A", "C");
        boolean modified = list.addAll(1, toInsert);

        assertTrue(modified);
        assertEquals(4, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals("D", list.get(3));
    }

    @Test(timeout = 4000)
    public void testRemoveByObject() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");

        assertTrue(list.remove("A"));
        assertEquals(1, list.size());
        assertFalse(list.contains("A"));
        assertFalse(list.remove("NonExistent"));
    }

    @Test(timeout = 4000)
    public void testRemoveByIndex() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");

        Object removed = list.remove(0);
        assertEquals("A", removed);
        assertEquals(1, list.size());
        assertFalse(list.contains("A"));
        assertTrue(list.contains("B"));
    }

    @Test(timeout = 4000)
    public void testRemoveAllAndRetainAll() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.addAll(Arrays.asList("A", "B", "C", "D"));

        boolean removed = list.removeAll(Arrays.asList("B", "X"));
        assertTrue(removed);
        assertEquals(3, list.size());
        assertFalse(list.contains("B"));

        boolean retained = list.retainAll(Arrays.asList("A", "D", "Z"));
        assertTrue(retained);
        assertEquals(2, list.size());
        assertTrue(list.contains("A"));
        assertTrue(list.contains("D"));
        assertFalse(list.contains("C"));
    }

    @Test(timeout = 4000)
    public void testClear() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.addAll(Arrays.asList("A", "B", "C"));
        list.clear();

        assertEquals(0, list.size());
        assertFalse(list.contains("A"));
        assertFalse(list.contains("B"));
        assertFalse(list.contains("C"));
    }

    @Test(timeout = 4000)
    public void testContainsAndContainsAll() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.addAll(Arrays.asList("X", "Y", "Z"));

        assertTrue(list.contains("X"));
        assertFalse(list.contains("W"));
        assertTrue(list.containsAll(Arrays.asList("X", "Z")));
        assertFalse(list.containsAll(Arrays.asList("X", "W")));
    }

    @Test(timeout = 4000)
    public void testAsSetView() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("One");
        list.add("Two");

        Set setView = list.asSet();
        assertEquals(2, setView.size());
        assertTrue(setView.contains("One"));
        assertTrue(setView.contains("Two"));

        try {
            setView.add("Three");
            fail("asSet() must return an unmodifiable set");
        } catch (UnsupportedOperationException expected) {
            // Success: view is unmodifiable
        }
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNullElementHandling() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        assertTrue(list.add(null));
        assertEquals(1, list.size());
        assertTrue(list.contains(null));
        assertNull(list.get(0));

        // Duplicate null rejected
        assertFalse(list.add(null));
        assertEquals(1, list.size());

        // Remove null
        assertTrue(list.remove(null));
        assertFalse(list.contains(null));
        assertEquals(0, list.size());
    }

    @Test(timeout = 4000)
    public void testAddAllEmptyCollectionReturnsFalse() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        assertFalse(list.addAll(new ArrayList()));
        assertFalse(list.addAll(0, new ArrayList()));
        assertEquals(1, list.size());
    }

    @Test(timeout = 4000)
    public void testSetSelfSwapBranch() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");

        // Swap element with itself at the same position (pos == index)
        Object old = list.set(1, "B");
        assertEquals("B", old);
        assertEquals(2, list.size());
        assertEquals("B", list.get(1));
        assertTrue(list.contains("B"));
    }

    @Test(timeout = 4000)
    public void testSetReorderBranches() {
        // Test pos != -1 and pos > index
        SetUniqueList list1 = SetUniqueList.decorate(new ArrayList());
        list1.add("A");
        list1.add("B");
        list1.add("C");
        // Replace "A" at index 0 with "C" (which is at index 2)
        Object removed1 = list1.set(0, "C");
        assertEquals("A", removed1);
        assertEquals(2, list1.size());
        assertEquals("C", list1.get(0));
        assertEquals("B", list1.get(1));
        assertFalse(list1.contains("A"));

        // Test pos != -1 and pos < index
        SetUniqueList list2 = SetUniqueList.decorate(new ArrayList());
        list2.add("A");
        list2.add("B");
        list2.add("C");
        // Replace "C" at index 2 with "A" (which is at index 0)
        Object removed2 = list2.set(2, "A");
        assertEquals("C", removed2);
        assertEquals(2, list2.size());
        assertEquals("B", list2.get(0));
        assertEquals("A", list2.get(1));
        assertFalse(list2.contains("C"));
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J COLLECTIONS-304)
    // -------------------------------------------------------------------------

    /**
     * Targets COLLECTIONS-304:
     * When set(int, Object) replaces an element with a brand new element (pos == -1),
     * the new element must be added to the backing set, and the replaced element
     * must be removed from the backing set.
     * In the buggy implementation, set.add(newObject) was omitted, allowing duplicate
     * addition and causing size to be 4 instead of 3.
     */
    @Test(timeout = 4000)
    public void testCollections304() {
        List list = new ArrayList();
        SetUniqueList setList = SetUniqueList.decorate(list);
        String s1 = "A";
        String s2 = "B";
        String s3 = "C";
        String s4 = "D";

        setList.add(s1);
        setList.add(s2);
        setList.add(s3);
        assertEquals(3, setList.size());

        // Replace "A" at 0 with "D" (pos == -1)
        setList.set(0, s4);

        // In buggy version, s4 was never added to set, so add(s4) wrongly succeeds
        setList.add(s4);

        assertEquals(3, setList.size());
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDecorateNullListThrowsException() {
        SetUniqueList.decorate(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullSetThrowsException() {
        new SetUniqueList(new ArrayList(), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullListThrowsException() {
        new SetUniqueList(null, new HashSet());
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testListIteratorSetThrowsUnsupportedOperationException() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        ListIterator it = list.listIterator();
        it.next();
        it.set("B");
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity (Iterators, SubList, Serialization)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testIteratorRemoveMaintainsSetConsistency() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");

        Iterator it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        it.remove();

        assertEquals(1, list.size());
        assertFalse(list.contains("A"));
        assertTrue(list.contains("B"));
    }

    @Test(timeout = 4000)
    public void testListIteratorNavigationAndModification() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");

        ListIterator it = list.listIterator(1);
        assertTrue(it.hasPrevious());
        assertEquals(1, it.nextIndex());
        assertEquals(0, it.previousIndex());
        assertEquals("A", it.previous());

        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        assertFalse(it.hasNext());

        // Remove via ListIterator
        it.remove();
        assertEquals(1, list.size());
        assertFalse(list.contains("B"));

        // Add unique via ListIterator
        it.add("C");
        assertEquals(2, list.size());
        assertTrue(list.contains("C"));

        // Add duplicate via ListIterator (should be ignored)
        it.add("A");
        assertEquals(2, list.size());
    }

    @Test(timeout = 4000)
    public void testSubListEnforcesUniqueness() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");

        List sub = list.subList(1, 3); // ["B", "C"]
        assertTrue(sub instanceof SetUniqueList);
        assertEquals(2, sub.size());

        // Adding duplicate of parent element into sublist should be rejected
        assertFalse(sub.add("A"));
        assertEquals(2, sub.size());

        // Adding unique element into sublist
        assertTrue(sub.add("E"));
        assertTrue(list.contains("E"));
    }

    @Test(timeout = 4000)
    public void testSerializationRoundtrip() throws Exception {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("Alpha");
        list.add("Beta");
        list.add("Gamma");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(list);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SetUniqueList deserialized = (SetUniqueList) ois.readObject();

        assertEquals(list, deserialized);
        assertEquals(3, deserialized.size());
        assertTrue(deserialized.contains("Beta"));

        // Verify deserialized instance still enforces uniqueness
        assertFalse(deserialized.add("Alpha"));
        assertTrue(deserialized.add("Delta"));
        assertEquals(4, deserialized.size());
    }
}