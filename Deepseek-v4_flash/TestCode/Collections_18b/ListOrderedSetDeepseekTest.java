package org.apache.commons.collections.set;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Targeted defect: retrainAll() method incorrectly handles case when collection contains elements 
 * that exist in the set after removing other elements. Bug triggered when:
 * - collection.removeAll() removes elements from decorated set
 * - Then subsequent iteration over setOrder removes elements not in collection
 * - If the original set had elements that are still in collection, but setOrder still contains 
 *   removed elements, the iteration may skip elements due to concurrent modification
 * 
 * Partition A: Core Functional Logic & State Transitions
 * - Construction via factory methods and constructors (empty, with set, with set+list, with list)
 * - add(), addAll(), remove(), removeAll(), retainAll() operations
 * - List-like operations: get(), indexOf(), add(index, E), addAll(index, Collection), remove(index)
 * - asList(), iterator(), clear(), toString(), toArray()
 * 
 * Partition B: Boundary Value Analysis & Extremes
 * - Empty collections, null arguments (where validated), duplicate elements
 * - Index boundaries: 0, size-1, size (out of bounds)
 * - Multiple operations in sequence to test state consistency
 * 
 * Partition C: Defect-Targeted Branch Zone
 * - retainAll() with overlapping and non-overlapping collections
 * - retainAll() where collection.contains() returns false for elements in setOrder after remove
 * - Multiple retainAll() calls to trigger edge cases in iteration
 * 
 * Partition D: Exception & Defensive Guard Paths
 * - Null arguments to factory methods
 * - Non-empty set/list to listOrderedSet(set, list)
 * - IndexOutOfBoundsException for add(index) and remove(index)
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 * - equals/hashCode consistency (inherited from AbstractSetDecorator)
 * - Iterator behavior and remove() operation
 * - Set uniqueness guarantees
 */
public class ListOrderedSetDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testConstructorEmpty() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
    }

    @Test(timeout = 4000)
    public void testConstructorWithSet() {
        Set<String> original = new HashSet<String>();
        original.add("A");
        original.add("B");
        ListOrderedSet<String> set = new ListOrderedSet<String>(original);
        assertEquals(2, set.size());
        assertTrue(set.contains("A"));
        assertTrue(set.contains("B"));
    }

    @Test(timeout = 4000)
    public void testListOrderedSetFactoryWithSetAndList() {
        Set<String> set = new HashSet<String>();
        List<String> list = new ArrayList<String>();
        ListOrderedSet<String> orderedSet = ListOrderedSet.listOrderedSet(set, list);
        assertTrue(orderedSet.isEmpty());
    }

    @Test(timeout = 4000)
    public void testListOrderedSetFactoryWithSet() {
        Set<String> original = new HashSet<String>();
        original.add("X");
        ListOrderedSet<String> set = ListOrderedSet.listOrderedSet(original);
        assertEquals(1, set.size());
        assertEquals("X", set.get(0));
    }

    @Test(timeout = 4000)
    public void testListOrderedSetFactoryWithList() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("B");
        list.add("A"); // duplicate
        ListOrderedSet<String> set = ListOrderedSet.listOrderedSet(list);
        assertEquals(2, set.size()); // duplicates removed
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
        assertEquals(2, list.size()); // list was modified
    }

    @Test(timeout = 4000)
    public void testAddAndGet() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        assertTrue(set.add("first"));
        assertTrue(set.add("second"));
        assertFalse(set.add("first")); // duplicate
        assertEquals("first", set.get(0));
        assertEquals("second", set.get(1));
        assertEquals(2, set.size());
    }

    @Test(timeout = 4000)
    public void testIndexOf() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");
        assertEquals(0, set.indexOf("A"));
        assertEquals(1, set.indexOf("B"));
        assertEquals(2, set.indexOf("C"));
        assertEquals(-1, set.indexOf("D"));
    }

    @Test(timeout = 4000)
    public void testAddAtIndex() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("C");
        set.add(1, "B"); // insert between A and C
        assertEquals(3, set.size());
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
        assertEquals("C", set.get(2));
    }

    @Test(timeout = 4000)
    public void testAddAtIndexDuplicate() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add(1, "A"); // duplicate, should not be added
        assertEquals(2, set.size());
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
    }

    @Test(timeout = 4000)
    public void testAddAll() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        List<String> toAdd = new ArrayList<String>();
        toAdd.add("A");
        toAdd.add("B");
        toAdd.add("A"); // duplicate in collection
        assertTrue(set.addAll(toAdd));
        assertEquals(2, set.size());
        assertTrue(set.contains("A"));
        assertTrue(set.contains("B"));
    }

    @Test(timeout = 4000)
    public void testAddAllAtIndex() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("D");
        List<String> toAdd = new ArrayList<String>();
        toAdd.add("B");
        toAdd.add("C");
        assertTrue(set.addAll(1, toAdd));
        assertEquals(4, set.size());
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
        assertEquals("C", set.get(2));
        assertEquals("D", set.get(3));
    }

    @Test(timeout = 4000)
    public void testRemoveObject() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");
        assertTrue(set.remove("B"));
        assertEquals(2, set.size());
        assertEquals("A", set.get(0));
        assertEquals("C", set.get(1));
        assertFalse(set.remove("X"));
    }

    @Test(timeout = 4000)
    public void testRemoveByIndex() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");
        Object removed = set.remove(1);
        assertEquals("B", removed);
        assertEquals(2, set.size());
        assertEquals("A", set.get(0));
        assertEquals("C", set.get(1));
    }

    @Test(timeout = 4000)
    public void testRemoveAll() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");
        List<String> toRemove = new ArrayList<String>();
        toRemove.add("A");
        toRemove.add("C");
        assertTrue(set.removeAll(toRemove));
        assertEquals(1, set.size());
        assertEquals("B", set.get(0));
    }

    @Test(timeout = 4000)
    public void testClear() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.clear();
        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
    }

    @Test(timeout = 4000)
    public void testAsList() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        List<String> listView = set.asList();
        assertEquals(2, listView.size());
        assertEquals("A", listView.get(0));
        assertEquals("B", listView.get(1));
    }

    @Test(timeout = 4000)
    public void testIterator() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");
        java.util.Iterator<String> it = set.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        it.remove();
        assertEquals(2, set.size());
        assertFalse(set.contains("B"));
        assertEquals("C", it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testToArray() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        Object[] arr = set.toArray();
        assertEquals(2, arr.length);
        assertEquals("A", arr[0]);
        assertEquals("B", arr[1]);
    }

    @Test(timeout = 4000)
    public void testToArrayWithParam() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        String[] arr = set.toArray(new String[0]);
        assertEquals(2, arr.length);
        assertEquals("A", arr[0]);
        assertEquals("B", arr[1]);
    }

    @Test(timeout = 4000)
    public void testToString() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        assertEquals("[A, B]", set.toString());
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testEmptySetOperations() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        assertEquals(-1, set.indexOf("anything"));
        assertNull(set.asList());
        assertTrue(set.toArray().length == 0);
    }

    @Test(timeout = 4000)
    public void testSingleElement() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("only");
        assertEquals(0, set.indexOf("only"));
        assertEquals("only", set.get(0));
        assertEquals(1, set.toArray().length);
    }

    @Test(timeout = 4000)
    public void testAddAtIndexBoundaryStart() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("B");
        set.add(0, "A");
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
    }

    @Test(timeout = 4000)
    public void testAddAtIndexBoundaryEnd() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add(1, "B"); // add at end
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
    }

    @Test(timeout = 4000)
    public void testRemoveByIndexBoundary() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("first");
        set.add("last");
        assertEquals("first", set.remove(0));
        assertEquals(1, set.size());
        assertEquals("last", set.remove(0));
        assertTrue(set.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAddAllAtIndexBoundaryStart() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("B");
        List<String> toAdd = new ArrayList<String>();
        toAdd.add("A");
        assertTrue(set.addAll(0, toAdd));
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
    }

    @Test(timeout = 4000)
    public void testNullElementAdd() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        assertTrue(set.add(null));
        assertTrue(set.contains(null));
        assertEquals(0, set.indexOf(null));
        assertFalse(set.add(null)); // duplicate null
        assertEquals(1, set.size());
    }

    @Test(timeout = 4000)
    public void testDuplicateAddSequence() {
        ListOrderedSet<Integer> set = new ListOrderedSet<Integer>();
        assertTrue(set.add(1));
        assertTrue(set.add(2));
        assertTrue(set.add(3));
        assertFalse(set.add(2)); // duplicate
        assertEquals(3, set.size());
        assertEquals(Integer.valueOf(1), set.get(0));
        assertEquals(Integer.valueOf(2), set.get(1));
        assertEquals(Integer.valueOf(3), set.get(2));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testRetainAllWithEmptyCollection() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");
        Collection<String> emptyColl = new ArrayList<String>();
        boolean changed = set.retainAll(emptyColl);
        assertTrue(changed);
        assertTrue(set.isEmpty());
    }

    @Test(timeout = 4000)
    public void testRetainAllWithFullRetention() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        Collection<String> retainColl = new ArrayList<String>();
        retainColl.add("A");
        retainColl.add("B");
        retainColl.add("C"); // extra element
        boolean changed = set.retainAll(retainColl);
        assertFalse(changed);
        assertEquals(2, set.size());
    }

    @Test(timeout = 4000)
    public void testRetainAllWithPartialRetention() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");
        Collection<String> retainColl = new ArrayList<String>();
        retainColl.add("A");
        retainColl.add("C");
        boolean changed = set.retainAll(retainColl);
        assertTrue(changed);
        assertEquals(2, set.size());
        assertTrue(set.contains("A"));
        assertTrue(set.contains("C"));
        assertEquals("A", set.get(0));
        assertEquals("C", set.get(1));
    }

    @Test(timeout = 4000)
    public void testRetainAllWithAllRemoved() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        Collection<String> retainColl = new ArrayList<String>();
        retainColl.add("X");
        retainColl.add("Y");
        boolean changed = set.retainAll(retainColl);
        assertTrue(changed);
        assertTrue(set.isEmpty());
    }

    @Test(timeout = 4000)
    public void testRetainAllWithDuplicatesInCollection() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        Collection<String> retainColl = new ArrayList<String>();
        retainColl.add("A");
        retainColl.add("A");
        retainColl.add("B");
        boolean changed = set.retainAll(retainColl);
        assertFalse(changed); // no change
        assertEquals(2, set.size());
    }

    @Test(timeout = 4000)
    public void testRetainAllBugTrigger() {
        // This test targets the known defect where retainAll causes inconsistent state
        // when the collection contains elements that exist after removing others
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");
        set.add("D");
        
        // First retain removes B and D
        List<String> retainFirst = new ArrayList<String>();
        retainFirst.add("A");
        retainFirst.add("C");
        boolean changed1 = set.retainAll(retainFirst);
        assertTrue(changed1);
        assertEquals(2, set.size());
        assertEquals("A", set.get(0));
        assertEquals("C", set.get(1));
        
        // Now retain again with different set - bug would cause order corruption
        List<String> retainSecond = new ArrayList<String>();
        retainSecond.add("A");
        retainSecond.add("B");
        boolean changed2 = set.retainAll(retainSecond);
        assertTrue(changed2);
        assertEquals(1, set.size());
        assertEquals("A", set.get(0));
    }

    @Test(timeout = 4000)
    public void testRetainAllOrderPreservationAfterMultipleRemovals() {
        // Another variant targeting the retainAll defect
        ListOrderedSet<Integer> set = new ListOrderedSet<Integer>();
        for (int i = 0; i < 5; i++) {
            set.add(i);
        }
        
        // Retain only odd numbers
        List<Integer> odds = new ArrayList<Integer>();
        odds.add(1);
        odds.add(3);
        boolean changed = set.retainAll(odds);
        assertTrue(changed);
        assertEquals(2, set.size());
        assertEquals(Integer.valueOf(1), set.get(0));
        assertEquals(Integer.valueOf(3), set.get(1));
        
        // Verify order is preserved after subsequent add
        set.add(4);
        assertEquals(3, set.size());
        assertEquals(Integer.valueOf(1), set.get(0));
        assertEquals(Integer.valueOf(3), set.get(1));
        assertEquals(Integer.valueOf(4), set.get(2));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testListOrderedSetNullSet() {
        ListOrderedSet.listOrderedSet(null, new ArrayList<String>());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testListOrderedSetNullList() {
        ListOrderedSet.listOrderedSet(new HashSet<String>(), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testListOrderedSetNonNullSet() {
        Set<String> nonEmpty = new HashSet<String>();
        nonEmpty.add("X");
        ListOrderedSet.listOrderedSet(nonEmpty, new ArrayList<String>());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testListOrderedSetNonNullList() {
        List<String> nonEmpty = new ArrayList<String>();
        nonEmpty.add("X");
        ListOrderedSet.listOrderedSet(new HashSet<String>(), nonEmpty);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testListOrderedSetNullListOnly() {
        ListOrderedSet.listOrderedSet((List<String>) null);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetNegativeIndex() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetIndexOutOfBounds() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.get(5);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testAddAtIndexNegative() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add(-1, "X");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testAddAtIndexTooLarge() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add(5, "X"); // index > size
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testRemoveByIndexNegative() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.remove(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testRemoveByIndexTooLarge() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.remove(5);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testSetUniquenessContract() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        assertTrue(set.add("A"));
        assertTrue(set.add("B"));
        assertFalse(set.add("A")); // violates Set contract if returns true
        assertEquals(2, set.size());
    }

    @Test(timeout = 4000)
    public void testIteratorRemoveUpdatesBothStructures() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");
        java.util.Iterator<String> it = set.iterator();
        it.next(); // A
        it.next(); // B
        it.remove(); // removes B
        assertFalse(set.contains("B"));
        assertEquals(2, set.size());
        assertEquals("A", set.get(0));
        assertEquals("C", set.get(1));
    }

    @Test(timeout = 4000)
    public void testAsListUnmodifiable() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        List<String> listView = set.asList();
        try {
            listView.add("B");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMultipleAddAllAtIndexPreservesOrder() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        List<String> firstBatch = new ArrayList<String>();
        firstBatch.add("A");
        firstBatch.add("B");
        set.addAll(firstBatch);
        
        List<String> secondBatch = new ArrayList<String>();
        secondBatch.add("C");
        secondBatch.add("D");
        set.addAll(2, secondBatch);
        
        assertEquals(4, set.size());
        assertEquals("A", set.get(0));
        assertEquals("B", set.get(1));
        assertEquals("C", set.get(2));
        assertEquals("D", set.get(3));
    }

    @Test(timeout = 4000)
    public void testAddAllAtIndexWithOverlap() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        set.add("C");
        
        List<String> toAdd = new ArrayList<String>();
        toAdd.add("A"); // already exists
        toAdd.add("D"); // new
        assertTrue(set.addAll(1, toAdd));
        
        assertEquals(4, set.size());
        assertEquals("A", set.get(0));
        assertEquals("D", set.get(1));
        assertEquals("B", set.get(2));
        assertEquals("C", set.get(3));
    }

    @Test(timeout = 4000)
    public void testAddAllNoChange() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        List<String> toAdd = new ArrayList<String>();
        toAdd.add("A"); // all already present
        assertFalse(set.addAll(toAdd));
        assertEquals(1, set.size());
    }

    @Test(timeout = 4000)
    public void testRemoveAllWithEmptyCollection() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        assertFalse(set.removeAll(new ArrayList<String>()));
        assertEquals(2, set.size());
    }

    @Test(timeout = 4000)
    public void testRetainAllWithSameCollection() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        assertFalse(set.retainAll(set)); // retain with itself
        assertEquals(2, set.size());
    }

    @Test(timeout = 4000)
    public void testOrderedIteratorPrevious() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add("A");
        set.add("B");
        OrderedIterator<String> it = set.iterator();
        assertEquals("A", it.next());
        assertTrue(it.hasPrevious());
        assertEquals("A", it.previous());
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        assertFalse(it.hasNext());
    }
}