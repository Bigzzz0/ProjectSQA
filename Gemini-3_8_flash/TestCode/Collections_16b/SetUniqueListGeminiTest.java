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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Decision Branches & Conditions:
 * 1. decorate(List):
 *    - list == null -> IllegalArgumentException
 *    - list.isEmpty() == true -> returns SetUniqueList wrapping empty HashSet
 *    - list.isEmpty() == false -> temp copy made, list.clear(), addAll(temp) with duplicate filtering
 * 2. SetUniqueList(List, Set) constructor:
 *    - set == null -> IllegalArgumentException
 *    - list == null -> IllegalArgumentException (via super)
 * 3. add(Object):
 *    - Element already present -> sizeBefore == size() -> returns false
 *    - Element not present -> sizeBefore != size() -> returns true
 * 4. add(int, Object):
 *    - set.contains(object) == false -> inserted into list and set
 *    - set.contains(object) == true -> ignored, no modification
 * 5. addAll(Collection) & addAll(int, Collection):
 *    - Collection empty -> returns false
 *    - All elements duplicates -> returns false
 *    - Some/all unique elements -> returns true, indices correctly shifted
 * 6. set(int, Object) [DEFECT COLLECTIONS-307]:
 *    - pos == index (replacing item with itself): defective implementation removes the element from internal set.
 *    - pos != -1 && pos != index (moving existing item): removes duplicate at pos, adjusts set.
 *    - pos == -1 (replacing item with totally new item): replaces old, removes old from set, adds new.
 * 7. remove(Object) / remove(int) / removeAll(Collection) / retainAll(Collection) / clear():
 *    - Ensures both internal list and backing set maintain synchronized states across deletions.
 * 8. Iterators (SetListIterator & SetListListIterator):
 *    - next() / previous() / remove() state transitions.
 *    - listIterator.add(Object): adds if not present in set, ignores if present.
 *    - listIterator.set(Object): unconditionally throws UnsupportedOperationException.
 * 9. subList(int, int):
 *    - subList shares the backing set; unique constraint holds across sublist operations.
 */
public class SetUniqueListGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDecoratePopulatedListWithDuplicatesPreservesFirstOccurrence() {
        List source = new ArrayList();
        source.add("A");
        source.add("B");
        source.add("A");
        source.add("C");
        source.add("B");

        SetUniqueList uniqueList = SetUniqueList.decorate(source);

        assertEquals(3, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
        assertEquals("C", uniqueList.get(2));
        assertTrue(uniqueList.contains("A"));
        assertTrue(uniqueList.contains("B"));
        assertTrue(uniqueList.contains("C"));
    }

    @Test(timeout = 4000)
    public void testAddUniqueAndDuplicateElements() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());

        assertTrue(list.add("First"));
        assertEquals(1, list.size());
        assertTrue(list.contains("First"));

        // Reject duplicate
        assertFalse(list.add("First"));
        assertEquals(1, list.size());

        assertTrue(list.add("Second"));
        assertEquals(2, list.size());
    }

    @Test(timeout = 4000)
    public void testAddAtIndexUniqueAndDuplicate() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("C");

        // Insert unique at index 1
        list.add(1, "B");
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));

        // Attempt to insert duplicate at index 0
        list.add(0, "C");
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
    }

    @Test(timeout = 4000)
    public void testAddAllAtEndAndAtIndex() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");

        // Add collection containing both unique and duplicate items
        Collection toAdd = Arrays.asList("B", "C", "D", "A", "E");
        boolean changed = list.addAll(toAdd);

        assertTrue(changed);
        assertEquals(5, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals("D", list.get(3));
        assertEquals("E", list.get(4));

        // AddAll at index
        Collection insertMiddle = Arrays.asList("X", "C", "Y");
        boolean middleChanged = list.addAll(2, insertMiddle);

        assertTrue(middleChanged);
        assertEquals(7, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("X", list.get(2));
        assertEquals("Y", list.get(3));
        assertEquals("C", list.get(4));
    }

    @Test(timeout = 4000)
    public void testRemoveByObjectAndIndex() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");
        list.add("C");

        // Remove by Object (present)
        assertTrue(list.remove("B"));
        assertEquals(2, list.size());
        assertFalse(list.contains("B"));

        // Remove by Object (absent)
        assertFalse(list.remove("NonExistent"));
        assertEquals(2, list.size());

        // Remove by index
        Object removed = list.remove(0);
        assertEquals("A", removed);
        assertEquals(1, list.size());
        assertFalse(list.contains("A"));
        assertEquals("C", list.get(0));
    }

    @Test(timeout = 4000)
    public void testRemoveAllAndRetainAll() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");

        // removeAll
        assertTrue(list.removeAll(Arrays.asList("2", "4", "99")));
        assertEquals(2, list.size());
        assertTrue(list.contains("1"));
        assertTrue(list.contains("3"));
        assertFalse(list.contains("2"));
        assertFalse(list.contains("4"));

        // retainAll
        list.add("5");
        list.add("6");
        assertTrue(list.retainAll(Arrays.asList("1", "6", "100")));
        assertEquals(2, list.size());
        assertTrue(list.contains("1"));
        assertTrue(list.contains("6"));
        assertFalse(list.contains("3"));
        assertFalse(list.contains("5"));

        // retainAll when no elements removed
        assertFalse(list.retainAll(Arrays.asList("1", "6")));
    }

    @Test(timeout = 4000)
    public void testClearAndContainsAll() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("Alpha");
        list.add("Beta");

        assertTrue(list.containsAll(Arrays.asList("Alpha", "Beta")));
        assertFalse(list.containsAll(Arrays.asList("Alpha", "Gamma")));

        list.clear();
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        assertFalse(list.contains("Alpha"));
        assertFalse(list.contains("Beta"));
    }

    @Test(timeout = 4000)
    public void testAsSetUnmodifiableView() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("One");
        list.add("Two");

        Set setView = list.asSet();
        assertEquals(2, setView.size());
        assertTrue(setView.contains("One"));
        assertTrue(setView.contains("Two"));

        try {
            setView.add("Three");
            fail("Expected UnsupportedOperationException on unmodifiable set view");
        } catch (UnsupportedOperationException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testSubListOperations() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("0");
        list.add("1");
        list.add("2");
        list.add("3");

        List sub = list.subList(1, 3);
        assertEquals(2, sub.size());
        assertEquals("1", sub.get(0));
        assertEquals("2", sub.get(1));

        // Sublist is an instance of SetUniqueList
        assertTrue(sub instanceof SetUniqueList);

        // Attempt to add duplicate already in parent list
        assertFalse(sub.add("0"));
        assertEquals(2, sub.size());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testDecorateEmptyList() {
        List emptySource = new ArrayList();
        SetUniqueList list = SetUniqueList.decorate(emptySource);
        assertNotNull(list);
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAddAllWithEmptyCollectionReturnsFalse() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("Existing");

        assertFalse(list.addAll(Collections.emptyList()));
        assertFalse(list.addAll(0, Collections.emptyList()));
        assertEquals(1, list.size());
    }

    @Test(timeout = 4000)
    public void testAddAllWithAllDuplicatesReturnsFalse() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("Item1");
        list.add("Item2");

        boolean changed = list.addAll(Arrays.asList("Item1", "Item2"));
        assertFalse(changed);
        assertEquals(2, list.size());

        boolean changedIndex = list.addAll(1, Arrays.asList("Item1", "Item2"));
        assertFalse(changedIndex);
        assertEquals(2, list.size());
    }

    @Test(timeout = 4000)
    public void testSetReplacingWithDifferentNewElement() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("Original1");
        list.add("Original2");

        Object previous = list.set(1, "BrandNew");
        assertEquals("Original2", previous);
        assertEquals(2, list.size());
        assertEquals("BrandNew", list.get(1));
        assertFalse(list.contains("Original2"));
        assertTrue(list.contains("BrandNew"));
    }

    @Test(timeout = 4000)
    public void testSetMovingExistingElementForwardAndBackward() {
        // pos > index: move "C" (index 2) to index 0
        SetUniqueList list1 = SetUniqueList.decorate(new ArrayList());
        list1.add("A");
        list1.add("B");
        list1.add("C");

        Object prev1 = list1.set(0, "C");
        assertEquals("A", prev1);
        assertEquals(2, list1.size());
        assertEquals("C", list1.get(0));
        assertEquals("B", list1.get(1));
        assertFalse(list1.contains("A"));
        assertTrue(list1.contains("C"));

        // pos < index: move "A" (index 0) to index 1
        SetUniqueList list2 = SetUniqueList.decorate(new ArrayList());
        list2.add("A");
        list2.add("B");
        list2.add("C");

        Object prev2 = list2.set(2, "A");
        assertEquals("C", prev2);
        assertEquals(2, list2.size());
        assertFalse(list2.contains("C"));
        assertTrue(list2.contains("A"));
    }

    @Test(timeout = 4000)
    public void testNullElementHandling() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());

        // Supports adding null element once
        assertTrue(list.add(null));
        assertEquals(1, list.size());
        assertTrue(list.contains(null));

        // Rejects second null element
        assertFalse(list.add(null));
        assertEquals(1, list.size());

        // Remove null element
        assertTrue(list.remove(null));
        assertEquals(0, list.size());
        assertFalse(list.contains(null));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (COLLECTIONS-307)
    // =========================================================================

    /**
     * Targets Commons-Collections Defect COLLECTIONS-307:
     * When SetUniqueList.set(index, object) is invoked where object is already at 'index'
     * (i.e. pos == index), the implementation erroneously removed the object from the
     * internal backing Set via `set.remove(removed)`.
     */
    @Test(timeout = 4000)
    public void testCollections307() {
        List list = new ArrayList();
        SetUniqueList uniqueList = SetUniqueList.decorate(list);

        Integer one = new Integer(1);
        Integer two = new Integer(2);
        Integer three = new Integer(3);

        uniqueList.add(one);
        uniqueList.add(two);
        uniqueList.add(three);

        // Setting element at index 0 to the exact same element (pos == index == 0)
        Object old = uniqueList.set(0, one);

        assertEquals("Previous element returned by set() must be equal", one, old);
        assertEquals("Size must remain 3 after setting identical element", 3, uniqueList.size());
        assertTrue("Backing set must NOT lose element 1 after set(0, 1)", uniqueList.contains(one));
        assertTrue("Backing set must retain element 2", uniqueList.contains(two));
        assertTrue("Backing set must retain element 3", uniqueList.contains(three));
        assertEquals(one, uniqueList.get(0));
        assertEquals(two, uniqueList.get(1));
        assertEquals(three, uniqueList.get(2));
    }

    @Test(timeout = 4000)
    public void testCollections307SecondaryIndex() {
        SetUniqueList uniqueList = SetUniqueList.decorate(new ArrayList());
        uniqueList.add("A");
        uniqueList.add("B");

        // Re-set at index 1 with "B"
        uniqueList.set(1, "B");

        assertEquals(2, uniqueList.size());
        assertTrue("SetUniqueList must continue to contain 'B'", uniqueList.contains("B"));
        assertTrue("SetUniqueList must continue to contain 'A'", uniqueList.contains("A"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

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

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetOutOfBoundsNegativeThrowsException() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testAddIndexOutOfBoundsThrowsException() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add(2, "Invalid");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testSetIndexOutOfBoundsThrowsException() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.set(0, "Invalid");
    }

    @Test(timeout = 4000)
    public void testListIteratorSetThrowsUnsupportedOperationException() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("Alpha");
        ListIterator it = list.listIterator();
        it.next();

        try {
            it.set("Beta");
            fail("ListIterator.set() must throw UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // Expected specification violation documented in SetUniqueList
        }
    }

    // =========================================================================
    // Partition E: Iterators & Object Lifecycle Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetListIteratorTraversalAndRemoval() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");
        list.add("C");

        Iterator it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertTrue(it.hasNext());
        assertEquals("B", it.next());

        it.remove(); // removes "B"
        assertEquals(2, list.size());
        assertFalse(list.contains("B"));
        assertEquals("A", list.get(0));
        assertEquals("C", list.get(1));

        assertTrue(it.hasNext());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());

        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testSetListListIteratorBidirectionalAndAdd() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("First");
        list.add("Third");

        ListIterator lit = list.listIterator();
        assertTrue(lit.hasNext());
        assertEquals("First", lit.next());

        // Add unique element via listIterator
        lit.add("Second");
        assertEquals(3, list.size());
        assertTrue(list.contains("Second"));

        // Attempt to add duplicate element via listIterator (must be ignored)
        lit.add("First");
        assertEquals(3, list.size());

        // Test previous()
        assertTrue(lit.hasPrevious());
        assertEquals("Second", lit.previous());

        // Remove via list iterator
        lit.remove();
        assertEquals(2, list.size());
        assertFalse(list.contains("Second"));
    }

    @Test(timeout = 4000)
    public void testSetListListIteratorWithIndex() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList());
        list.add("A");
        list.add("B");
        list.add("C");

        ListIterator lit = list.listIterator(2);
        assertTrue(lit.hasNext());
        assertEquals("C", lit.next());
        assertFalse(lit.hasNext());
        assertEquals("C", lit.previous());
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTripIntegrity() throws Exception {
        SetUniqueList original = SetUniqueList.decorate(new ArrayList());
        original.add("Entry1");
        original.add("Entry2");
        original.add("Entry3");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SetUniqueList deserialized = (SetUniqueList) ois.readObject();
        ois.close();

        assertEquals(original.size(), deserialized.size());
        assertEquals("Entry1", deserialized.get(0));
        assertEquals("Entry2", deserialized.get(1));
        assertEquals("Entry3", deserialized.get(2));
        assertTrue(deserialized.contains("Entry1"));
        assertTrue(deserialized.contains("Entry2"));
        assertTrue(deserialized.contains("Entry3"));

        // Confirm unique constraint remains operational after deserialization
        assertFalse(deserialized.add("Entry1"));
        assertEquals(3, deserialized.size());
    }
}