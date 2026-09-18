package org.apache.commons.collections.set;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Class Under Test: org.apache.commons.collections.set.ListOrderedSet<E>
 * Targeted Defect: COLLECTIONS-426 / testRetainAllCollections426 (junit.framework.AssertionFailedError)
 * ----------------------------------------------------------------------------------------------------
 * Branch / Condition Coverage Map:
 * 1. Factory listOrderedSet(Set, List):
 *    - set == null (throws IAE)
 *    - list == null (throws IAE)
 *    - set.size() > 0 || list.size() > 0 (throws IAE)
 *    - empty set and empty list (valid instantiation)
 * 2. Factory listOrderedSet(Set):
 *    - set == null (throws IAE)
 *    - non-empty set (populates order via new ArrayList(set))
 * 3. Factory listOrderedSet(List):
 *    - list == null (throws IAE)
 *    - list with duplicates (duplicates purged via retainAll on set)
 * 4. Constructors:
 *    - Default constructor ListOrderedSet()
 *    - Protected ListOrderedSet(Set, List) with list == null (throws IAE)
 * 5. add(E) & add(int, E):
 *    - Object not present: returns true / added to both collection and setOrder at index
 *    - Object already present: returns false / no-op, preserving original insertion order
 * 6. addAll(Collection) & addAll(int, Collection):
 *    - All new elements (changed == true)
 *    - Mixed new and existing elements (only new inserted, changed == true)
 *    - No new elements (all present, changed == false)
 *    - Empty collection (changed == false)
 * 7. remove(Object) & remove(int):
 *    - remove(Object) present: removes from both, returns true
 *    - remove(Object) absent: returns false
 *    - remove(int): removes from listOrder and decorated set, shifts left, returns removed element
 *    - remove(int) out-of-bounds: throws IndexOutOfBoundsException
 * 8. removeAll(Collection):
 *    - Subset matches (result == true)
 *    - No match (result == false)
 * 9. retainAll(Collection) [DEFECT ZONE - COLLECTIONS-426]:
 *    - result == false branch (no modifications to collection, returns false, list intact)
 *    - collection.size() == 0 branch (all removed, setOrder cleared)
 *    - collection.size() > 0 branch (partial removal, iterator filters setOrder)
 *    - setOrder synchronization with collection state
 * 10. Iterators (OrderedSetIterator):
 *    - Forward traversal (hasNext, next)
 *    - Reverse traversal (hasPrevious, previous)
 *    - Removal via iterator (removes from both setOrder and underlying collection)
 * 11. List Views & Projections:
 *    - asList() unmodifiable wrapper verification
 *    - toArray() and toArray(T[]) matching insertion order
 *    - indexOf(Object) present vs absent (-1)
 *    - get(int) valid vs IndexOutOfBoundsException
 *    - toString() representation matching setOrder
 * 12. Serialization & Lifecycle:
 *    - Round-trip serialization ensuring state and order preservation
 * ====================================================================================================
 */

import org.apache.commons.collections.OrderedIterator;
import org.junit.Test;

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
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.*;

public class ListOrderedSetGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorAndBasicAdd() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());

        assertTrue(set.add("Alpha"));
        assertTrue(set.add("Beta"));
        assertTrue(set.add("Gamma"));

        // Set semantics: duplicate additions must return false and not modify sequence
        assertFalse(set.add("Alpha"));
        assertFalse(set.add("Beta"));

        assertEquals(3, set.size());
        assertEquals("Alpha", set.get(0));
        assertEquals("Beta", set.get(1));
        assertEquals("Gamma", set.get(2));
    }

    @Test(timeout = 4000)
    public void testIndexedAddAndOrderPreservation() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("Alpha");
        set.add("Gamma");

        // Insert at middle index
        set.add(1, "Beta");
        assertEquals(3, set.size());
        assertEquals("Alpha", set.get(0));
        assertEquals("Beta", set.get(1));
        assertEquals("Gamma", set.get(2));

        // Attempt to insert duplicate at different index: must be ignored
        set.add(0, "Beta");
        assertEquals(3, set.size());
        assertEquals("Alpha", set.get(0));
        assertEquals("Beta", set.get(1));
        assertEquals("Gamma", set.get(2));

        // Insert at beginning
        set.add(0, "First");
        assertEquals(4, set.size());
        assertEquals("First", set.get(0));
        assertEquals("Alpha", set.get(1));

        // Insert at end index
        set.add(4, "Last");
        assertEquals(5, set.size());
        assertEquals("Last", set.get(4));
    }

    @Test(timeout = 4000)
    public void testAddAllSequentialAndIndexed() {
        ListOrderedSet<Integer> set = new ListOrderedSet<Integer>();
        assertTrue(set.addAll(Arrays.asList(10, 20, 30)));
        assertEquals(3, set.size());

        // Partial overlap addAll
        assertTrue(set.addAll(Arrays.asList(20, 30, 40, 50)));
        assertEquals(5, set.size());
        assertEquals(Integer.valueOf(10), set.get(0));
        assertEquals(Integer.valueOf(20), set.get(1));
        assertEquals(Integer.valueOf(30), set.get(2));
        assertEquals(Integer.valueOf(40), set.get(3));
        assertEquals(Integer.valueOf(50), set.get(4));

        // Complete duplicate addAll: must return false
        assertFalse(set.addAll(Arrays.asList(10, 40, 50)));

        // Indexed addAll with mixed elements
        assertTrue(set.addAll(2, Arrays.asList(99, 20, 100))); // 20 ignored, 99 and 100 inserted at index 2
        assertEquals(7, set.size());
        assertEquals(Integer.valueOf(10), set.get(0));
        assertEquals(Integer.valueOf(20), set.get(1));
        assertEquals(Integer.valueOf(99), set.get(2));
        assertEquals(Integer.valueOf(100), set.get(3));
        assertEquals(Integer.valueOf(30), set.get(4));

        // Indexed addAll with no new elements: must return false
        assertFalse(set.addAll(1, Arrays.asList(10, 20, 99)));
    }

    @Test(timeout = 4000)
    public void testRemoveByObjectAndByIndex() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.addAll(Arrays.asList("A", "B", "C", "D", "E"));

        // Remove by object
        assertTrue(set.remove("C"));
        assertFalse(set.remove("NonExistent"));
        assertEquals(4, set.size());
        assertEquals(-1, set.indexOf("C"));
        assertFalse(set.contains("C"));
        assertEquals("D", set.get(2));

        // Remove by index
        Object removed = set.remove(1); // removes "B"
        assertEquals("B", removed);
        assertEquals(3, set.size());
        assertEquals("A", set.get(0));
        assertEquals("D", set.get(1));
        assertEquals("E", set.get(2));
        assertFalse(set.contains("B"));

        // Remove head and tail by index
        assertEquals("A", set.remove(0));
        assertEquals("E", set.remove(1));
        assertEquals(1, set.size());
        assertEquals("D", set.get(0));
    }

    @Test(timeout = 4000)
    public void testRemoveAll() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.addAll(Arrays.asList("One", "Two", "Three", "Four"));

        // Remove some existing and non-existing
        assertTrue(set.removeAll(Arrays.asList("Two", "Four", "Five")));
        assertEquals(2, set.size());
        assertEquals("One", set.get(0));
        assertEquals("Three", set.get(1));

        // Remove none
        assertFalse(set.removeAll(Arrays.asList("Ten", "Twenty")));
        assertEquals(2, set.size());
    }

    @Test(timeout = 4000)
    public void testClearAndIsEmpty() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.addAll(Arrays.asList("X", "Y", "Z"));
        assertFalse(set.isEmpty());

        set.clear();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
        assertEquals(-1, set.indexOf("X"));
        assertFalse(set.iterator().hasNext());
    }

    @Test(timeout = 4000)
    public void testAsListAndViews() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.addAll(Arrays.asList("First", "Second", "Third"));

        List<String> listView = set.asList();
        assertEquals(3, listView.size());
        assertEquals("First", listView.get(0));
        assertEquals("Second", listView.get(1));
        assertEquals("Third", listView.get(2));

        // Verify unmodifiable contract on asList()
        try {
            listView.add("Illegal");
            fail("Expected UnsupportedOperationException when mutating asList()");
        } catch (UnsupportedOperationException expected) {
            // Success
        }

        try {
            listView.remove(0);
            fail("Expected UnsupportedOperationException when mutating asList()");
        } catch (UnsupportedOperationException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testToArrayProjections() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.addAll(Arrays.asList("Apple", "Banana", "Cherry"));

        Object[] objArray = set.toArray();
        assertArrayEquals(new Object[]{"Apple", "Banana", "Cherry"}, objArray);

        String[] smallArray = new String[1];
        String[] resultSmall = set.toArray(smallArray);
        assertNotSame(smallArray, resultSmall);
        assertArrayEquals(new String[]{"Apple", "Banana", "Cherry"}, resultSmall);

        String[] bigArray = new String[5];
        bigArray[3] = "Sentinel";
        String[] resultBig = set.toArray(bigArray);
        assertSame(bigArray, resultBig);
        assertEquals("Apple", resultBig[0]);
        assertEquals("Banana", resultBig[1]);
        assertEquals("Cherry", resultBig[2]);
        assertNull(resultBig[3]); // Array contract sets index equal to size to null
    }

    @Test(timeout = 4000)
    public void testOrderedSetIteratorBidirectionalAndRemove() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.addAll(Arrays.asList("1", "2", "3"));

        OrderedIterator<String> it = set.iterator();
        assertFalse(it.hasPrevious());
        assertTrue(it.hasNext());

        assertEquals("1", it.next());
        assertTrue(it.hasPrevious());
        assertEquals("1", it.previous());
        assertEquals("1", it.next());

        assertEquals("2", it.next());
        // Remove "2" via iterator
        it.remove();
        assertEquals(2, set.size());
        assertFalse(set.contains("2"));
        assertEquals("3", it.next());
        assertFalse(it.hasNext());

        // Traverse backwards after removal
        assertTrue(it.hasPrevious());
        assertEquals("3", it.previous());
        assertTrue(it.hasPrevious());
        assertEquals("1", it.previous());
        assertFalse(it.hasPrevious());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptySetBehaviors() {
        ListOrderedSet<Object> emptySet = new ListOrderedSet<Object>();
        assertEquals(0, emptySet.size());
        assertTrue(emptySet.isEmpty());
        assertEquals(-1, emptySet.indexOf("NonExistent"));
        assertFalse(emptySet.contains("NonExistent"));
        assertFalse(emptySet.remove("NonExistent"));
        assertFalse(emptySet.removeAll(Collections.emptyList()));
        assertFalse(emptySet.retainAll(Collections.emptyList()));
        assertEquals(0, emptySet.toArray().length);
        assertEquals("[]", emptySet.toString());
    }

    @Test(timeout = 4000)
    public void testNullElementHandling() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        assertTrue(set.add(null));
        assertEquals(1, set.size());
        assertTrue(set.contains(null));
        assertNull(set.get(0));
        assertEquals(0, set.indexOf(null));

        // Re-adding null must fail
        assertFalse(set.add(null));
        assertEquals(1, set.size());

        set.add("AfterNull");
        assertEquals(2, set.size());
        assertEquals("AfterNull", set.get(1));

        assertTrue(set.remove(null));
        assertEquals(1, set.size());
        assertFalse(set.contains(null));
        assertEquals("AfterNull", set.get(0));
    }

    @Test(timeout = 4000)
    public void testFactoryDecorateListWithDuplicates() {
        List<String> listWithDups = new ArrayList<String>();
        listWithDups.add("A");
        listWithDups.add("B");
        listWithDups.add("A");
        listWithDups.add("C");
        listWithDups.add("B");

        ListOrderedSet<String> set = ListOrderedSet.listOrderedSet(listWithDups);
        assertEquals(3, set.size());
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
        assertEquals("C", set.get(2));
        assertEquals(0, set.indexOf("A"));
        assertEquals(1, set.indexOf("B"));
        assertEquals(2, set.indexOf("C"));
    }

    @Test(timeout = 4000)
    public void testFactoryDecorateSet() {
        Set<Integer> sourceSet = new HashSet<Integer>();
        sourceSet.add(100);
        sourceSet.add(200);

        ListOrderedSet<Integer> set = ListOrderedSet.listOrderedSet(sourceSet);
        assertEquals(2, set.size());
        assertTrue(set.contains(100));
        assertTrue(set.contains(200));
    }

    @Test(timeout = 4000)
    public void testFactoryDecorateEmptySetAndList() {
        Set<String> emptySet = new HashSet<String>();
        List<String> emptyList = new ArrayList<String>();

        ListOrderedSet<String> orderedSet = ListOrderedSet.listOrderedSet(emptySet, emptyList);
        assertTrue(orderedSet.isEmpty());
        orderedSet.add("Test");
        assertEquals(1, orderedSet.size());
        assertEquals("Test", orderedSet.get(0));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Targeting COLLECTIONS-426)
    // =========================================================================

    /**
     * Direct test targeting Defects4J defect:
     * org.apache.commons.collections.set.ListOrderedSetTest::testRetainAllCollections426
     * Verifies that retainAll properly synchronizes both the underlying collection
     * and setOrder list across all partial, total, and no-op retention conditions.
     */
    @Test(timeout = 4000)
    public void testRetainAllCollections426() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");
        set.add("D");

        List<String> retainList = new ArrayList<String>();
        retainList.add("B");
        retainList.add("D");
        retainList.add("E"); // Extra element not in target set

        boolean modified = set.retainAll(retainList);
        assertTrue("retainAll should return true when elements are removed", modified);
        assertEquals("Size must reflect only retained elements", 2, set.size());
        assertEquals("asList view must have matching size", 2, set.asList().size());
        assertEquals("toArray must have matching length", 2, set.toArray().length);

        // Crucial: Retained order must strictly preserve initial insertion order
        assertEquals("Element at index 0 must be 'B'", "B", set.get(0));
        assertEquals("Element at index 1 must be 'D'", "D", set.get(1));

        assertFalse(set.contains("A"));
        assertTrue(set.contains("B"));
        assertFalse(set.contains("C"));
        assertTrue(set.contains("D"));
        assertFalse(set.contains("E"));
    }

    @Test(timeout = 4000)
    public void testRetainAllClearAllElements() {
        ListOrderedSet<Integer> set = new ListOrderedSet<Integer>();
        set.addAll(Arrays.asList(1, 2, 3));

        boolean modified = set.retainAll(Collections.singletonList(99)); // Disjoint collection
        assertTrue(modified);
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
        assertEquals(0, set.asList().size());
        assertEquals(0, set.toArray().length);
    }

    @Test(timeout = 4000)
    public void testRetainAllNoOpWhenSupersetRetained() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.addAll(Arrays.asList("X", "Y"));

        List<String> retainList = Arrays.asList("X", "Y", "Z");
        boolean modified = set.retainAll(retainList);
        assertFalse("retainAll must return false when no elements were removed", modified);
        assertEquals(2, set.size());
        assertEquals("X", set.get(0));
        assertEquals("Y", set.get(1));
    }

    @Test(timeout = 4000)
    public void testRetainAllWithEmptyCollection() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.addAll(Arrays.asList("M", "N"));

        boolean modified = set.retainAll(Collections.emptyList());
        assertTrue(modified);
        assertEquals(0, set.size());
        assertEquals(0, set.asList().size());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactoryListOrderedSetNullSetThrows() {
        ListOrderedSet.listOrderedSet((Set<Object>) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactoryListOrderedSetNullListThrows() {
        ListOrderedSet.listOrderedSet((List<Object>) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactoryListOrderedSetSetAndListNullSetThrows() {
        ListOrderedSet.listOrderedSet(null, new ArrayList<Object>());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactoryListOrderedSetSetAndListNullListThrows() {
        ListOrderedSet.listOrderedSet(new HashSet<Object>(), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactoryListOrderedSetNonEmptySetThrows() {
        Set<String> set = new HashSet<String>();
        set.add("Present");
        ListOrderedSet.listOrderedSet(set, new ArrayList<String>());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactoryListOrderedSetNonEmptyListThrows() {
        List<String> list = new ArrayList<String>();
        list.add("Present");
        ListOrderedSet.listOrderedSet(new HashSet<String>(), list);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetNegativeIndexThrows() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("Element");
        set.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetIndexOutOfBoundsThrows() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("Element");
        set.get(1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testRemoveIndexOutOfBoundsThrows() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("Element");
        set.remove(1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testAddIndexNegativeThrows() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add(-1, "Illegal");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testAddIndexBeyondSizeThrows() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add(1, "Illegal");
    }

    @Test(timeout = 4000)
    public void testProtectedConstructorNullListThrows() {
        try {
            new ExposedListOrderedSet<String>(new HashSet<String>(), null);
            fail("Expected IllegalArgumentException on null list in constructor");
        } catch (IllegalArgumentException expected) {
            // Success
        }
    }

    // Helper subclass to access protected constructors
    private static class ExposedListOrderedSet<E> extends ListOrderedSet<E> {
        private static final long serialVersionUID = 1L;

        ExposedListOrderedSet(Set<E> set, List<E> list) {
            super(set, list);
        }

        ExposedListOrderedSet(Set<E> set) {
            super(set);
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testToStringMatchesListRepresentation() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("Red");
        set.add("Green");
        set.add("Blue");

        assertEquals("[Red, Green, Blue]", set.toString());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        ListOrderedSet<Integer> set1 = new ListOrderedSet<Integer>();
        set1.addAll(Arrays.asList(1, 2, 3));

        ListOrderedSet<Integer> set2 = new ListOrderedSet<Integer>();
        set2.addAll(Arrays.asList(1, 2, 3));

        Set<Integer> standardSet = new HashSet<Integer>(Arrays.asList(3, 2, 1));

        // Set equality is order-independent per Set interface contract
        assertEquals(set1, set2);
        assertEquals(set1, standardSet);
        assertEquals(standardSet, set1);
        assertEquals(set1.hashCode(), standardSet.hashCode());
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testSerializationRoundTrip() throws Exception {
        ListOrderedSet<String> original = new ListOrderedSet<String>();
        original.add("Item1");
        original.add("Item2");
        original.add("Item3");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ListOrderedSet<String> deserialized = (ListOrderedSet<String>) ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertEquals(3, deserialized.size());
        assertEquals("Item1", deserialized.get(0));
        assertEquals("Item2", deserialized.get(1));
        assertEquals("Item3", deserialized.get(2));
        assertEquals(original, deserialized);
    }

    @Test(timeout = 4000)
    public void testIteratorExceptionUnderflowAndDoubleRemove() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        OrderedIterator<String> it = set.iterator();

        try {
            it.previous();
            fail("Expected NoSuchElementException when calling previous() at start");
        } catch (NoSuchElementException expected) {
            // Success
        }

        assertEquals("A", it.next());
        it.remove();

        try {
            it.remove();
            fail("Expected IllegalStateException on consecutive remove() calls");
        } catch (IllegalStateException expected) {
            // Success
        }

        try {
            it.next();
            fail("Expected NoSuchElementException when calling next() at end");
        } catch (NoSuchElementException expected) {
            // Success
        }
    }
}