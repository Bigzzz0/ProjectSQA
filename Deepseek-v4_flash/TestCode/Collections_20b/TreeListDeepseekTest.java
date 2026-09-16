package org.apache.commons.collections.list;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.ConcurrentModificationException;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Basic CRUD operations (add, get, set, remove, clear)
 *   - Iterator/ListIterator operations (next, previous, hasNext, hasPrevious, remove, set, add)
 *   - Bulk operations (addAll, toArray, indexOf, contains)
 *   - State management (size, modCount, empty/full states)
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Empty list operations (size=0)
 *   - Single element list (size=1)
 *   - Multiple elements with complex tree structure (size > 1, triggers AVL rotations)
 *   - Index boundaries: 0, size()-1, size() for add, invalid indices for get/set/remove
 *   - Null element handling
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Known defect: TreeList testBugCollections447 - expected:<[A]> but was:<[C]>
 *   - This bug relates to list iterator add/remove operations causing incorrect element ordering
 *   - The fix involves ensuring iterator.add() correctly updates the tree structure
 *   - Targeting scenarios: add after remove via iterator, multiple add/remove cycles
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - IndexOutOfBoundsException for invalid indices in get/set/remove/add
 *   - ConcurrentModificationException when modifying list during iteration
 *   - IllegalStateException for iterator remove/set without next/previous
 *   - NoSuchElementException for iterator next/previous at boundaries
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Collection constructor (ArrayList source)
 *   - toArray contract (array length, element ordering)
 *   - indexOf/contains consistency
 * 
 * Key branches in AVLNode:
 *   - insertOnLeft vs insertOnRight (indexRelativeToMe <= 0 vs > 0)
 *   - removeSelf with left/right child combinations
 *   - balance() for heightRightMinusLeft values -2, -1, 0, 1, 2
 *   - rotateLeft/rotateRight with offset calculations
 *   - getLeftSubTree/getRightSubTree with faedelung flags (leftIsPrevious/rightIsNext)
 */

public class TreeListDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========
    
    @Test(timeout = 4000)
    public void testEmptyListSize() {
        TreeList<String> list = new TreeList<String>();
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAddAndGet() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        list.add("C");
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
    }

    @Test(timeout = 4000)
    public void testAddAtIndex() {
        TreeList<String> list = new TreeList<String>();
        list.add(0, "B");
        list.add(0, "A");
        list.add(2, "C");
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
    }

    @Test(timeout = 4000)
    public void testSet() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        list.add("C");
        String old = list.set(1, "X");
        assertEquals("B", old);
        assertEquals("X", list.get(1));
        assertEquals(3, list.size());
    }

    @Test(timeout = 4000)
    public void testRemove() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        list.add("C");
        String removed = list.remove(1);
        assertEquals("B", removed);
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("C", list.get(1));
    }

    @Test(timeout = 4000)
    public void testClear() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        list.clear();
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @Test(timeout = 4000)
    public void testIndexOf() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        list.add("A");
        assertEquals(0, list.indexOf("A"));
        assertEquals(1, list.indexOf("B"));
        assertEquals(-1, list.indexOf("C"));
    }

    @Test(timeout = 4000)
    public void testContains() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        assertTrue(list.contains("A"));
        assertFalse(list.contains("B"));
    }

    @Test(timeout = 4000)
    public void testToArray() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        Object[] arr = list.toArray();
        assertEquals(2, arr.length);
        assertEquals("A", arr[0]);
        assertEquals("B", arr[1]);
    }

    @Test(timeout = 4000)
    public void testCollectionConstructor() {
        List<String> source = Arrays.asList("A", "B", "C");
        TreeList<String> list = new TreeList<String>(source);
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testGetSingleElement() {
        TreeList<String> list = new TreeList<String>();
        list.add("X");
        assertEquals("X", list.get(0));
    }

    @Test(timeout = 4000)
    public void testRemoveFromSingleElement() {
        TreeList<String> list = new TreeList<String>();
        list.add("X");
        assertEquals("X", list.remove(0));
        assertEquals(0, list.size());
    }

    @Test(timeout = 4000)
    public void testAddAtEndMultiple() {
        TreeList<Integer> list = new TreeList<Integer>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }
        assertEquals(10, list.size());
        for (int i = 0; i < 10; i++) {
            assertEquals(Integer.valueOf(i), list.get(i));
        }
    }

    @Test(timeout = 4000)
    public void testAddAtIndexMultiple() {
        TreeList<Integer> list = new TreeList<Integer>();
        list.add(0, 2);
        list.add(0, 1);
        list.add(0, 0);
        list.add(3, 3);
        assertEquals(4, list.size());
        for (int i = 0; i < 4; i++) {
            assertEquals(Integer.valueOf(i), list.get(i));
        }
    }

    @Test(timeout = 4000)
    public void testRemoveFromBeginningAndEnd() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        assertEquals("A", list.remove(0));
        assertEquals("D", list.remove(2));
        assertEquals(2, list.size());
        assertEquals("B", list.get(0));
        assertEquals("C", list.get(1));
    }

    @Test(timeout = 4000)
    public void testAddAtIndexZeroOnEmptyList() {
        TreeList<String> list = new TreeList<String>();
        list.add(0, "First");
        assertEquals(1, list.size());
        assertEquals("First", list.get(0));
    }

    @Test(timeout = 4000)
    public void testNullElementHandling() {
        TreeList<String> list = new TreeList<String>();
        list.add(null);
        list.add("B");
        assertEquals(2, list.size());
        assertNull(list.get(0));
        assertEquals("B", list.get(1));
        assertEquals(0, list.indexOf(null));
        assertTrue(list.contains(null));
        assertEquals(-1, list.indexOf("A"));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========
    
    @Test(timeout = 4000)
    public void testBugCollections447() {
        // Known defect: iterator.add() followed by operations causes incorrect ordering
        // Expected: [A] but was: [C]
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        list.add("C");
        
        // This sequence should preserve ordering
        ListIterator<String> it = list.listIterator();
        it.next(); // A
        it.next(); // B
        it.remove(); // remove B
        it.add("X"); // add X at position after A
        it.next(); // should be C
        it.remove(); // remove C
        
        // Now list should be [A, X]
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("X", list.get(1));
    }

    @Test(timeout = 4000)
    public void testBugCollections447Variant() {
        // Variant: add after remove via iterator
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        
        ListIterator<String> it = list.listIterator();
        it.next(); // A
        it.remove(); // remove A
        it.add("C"); // add C at start
        
        // Should be [C, B]
        assertEquals(2, list.size());
        assertEquals("C", list.get(0));
        assertEquals("B", list.get(1));
    }

    @Test(timeout = 4000)
    public void testBugCollections447MultipleOperations() {
        // Multiple add/remove cycles via iterator
        TreeList<String> list = new TreeList<String>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        
        ListIterator<String> it = list.listIterator();
        it.next(); // 1
        it.remove(); // remove 1
        it.add("A"); // add A at start
        it.next(); // 2
        it.add("B"); // add B after 2
        
        // Expected: [A, 2, B, 3, 4]
        assertEquals(5, list.size());
        assertEquals("A", list.get(0));
        assertEquals("2", list.get(1));
        assertEquals("B", list.get(2));
        assertEquals("3", list.get(3));
        assertEquals("4", list.get(4));
    }

    @Test(timeout = 4000)
    public void testRemoveViaIteratorFollowedByGet() {
        // After removing via iterator, subsequent get should reflect correct state
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        list.add("C");
        
        ListIterator<String> it = list.listIterator();
        it.next(); // A
        it.next(); // B
        it.remove(); // remove B
        
        // List should be [A, C]
        assertEquals("A", list.get(0));
        assertEquals("C", list.get(1));
        
        it.add("X"); // add X at index 1
        // List should be [A, X, C]
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("X", list.get(1));
        assertEquals("C", list.get(2));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetWithNegativeIndex() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetWithIndexEqualToSize() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.get(1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetFromEmptyList() {
        TreeList<String> list = new TreeList<String>();
        list.get(0);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testAddWithNegativeIndex() {
        TreeList<String> list = new TreeList<String>();
        list.add(-1, "A");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testAddWithIndexGreaterThanSize() {
        TreeList<String> list = new TreeList<String>();
        list.add(1, "A");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testRemoveWithNegativeIndex() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.remove(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testRemoveWithIndexEqualToSize() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.remove(1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testRemoveFromEmptyList() {
        TreeList<String> list = new TreeList<String>();
        list.remove(0);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testSetWithNegativeIndex() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.set(-1, "B");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testSetWithIndexEqualToSize() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.set(1, "B");
    }

    @Test(expected = ConcurrentModificationException.class, timeout = 4000)
    public void testConcurrentModificationNext() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        
        ListIterator<String> it = list.listIterator();
        list.add("C"); // modify list externally
        it.next(); // should throw ConcurrentModificationException
    }

    @Test(expected = ConcurrentModificationException.class, timeout = 4000)
    public void testConcurrentModificationPrevious() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        
        ListIterator<String> it = list.listIterator(1);
        list.remove(0); // modify list externally
        it.previous(); // should throw ConcurrentModificationException
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testIteratorRemoveWithoutNext() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        
        ListIterator<String> it = list.listIterator();
        it.remove(); // should throw IllegalStateException
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testIteratorSetWithoutNext() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        
        ListIterator<String> it = list.listIterator();
        it.set("B"); // should throw IllegalStateException
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testIteratorNextBeyondEnd() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        
        ListIterator<String> it = list.listIterator();
        it.next(); // A
        it.next(); // should throw NoSuchElementException
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testIteratorPreviousBeforeStart() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        
        ListIterator<String> it = list.listIterator();
        it.previous(); // should throw NoSuchElementException
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testListIteratorWithNegativeIndex() {
        TreeList<String> list = new TreeList<String>();
        list.listIterator(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testListIteratorWithIndexGreaterThanSize() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.listIterator(2);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testIteratorHasNextHasPrevious() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        
        ListIterator<String> it = list.listIterator();
        assertTrue(it.hasNext());
        assertFalse(it.hasPrevious());
        assertEquals(0, it.nextIndex());
        assertEquals(-1, it.previousIndex());
        
        it.next();
        assertTrue(it.hasNext());
        assertTrue(it.hasPrevious());
        assertEquals(1, it.nextIndex());
        assertEquals(0, it.previousIndex());
        
        it.next();
        assertFalse(it.hasNext());
        assertTrue(it.hasPrevious());
        assertEquals(2, it.nextIndex());
        assertEquals(1, it.previousIndex());
    }

    @Test(timeout = 4000)
    public void testIteratorPreviousAfterNext() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        list.add("C");
        
        ListIterator<String> it = list.listIterator();
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        assertEquals("B", it.previous());
        assertEquals("A", it.previous());
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        assertEquals("C", it.next());
    }

    @Test(timeout = 4000)
    public void testIteratorSetWithNext() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        
        ListIterator<String> it = list.listIterator();
        it.next(); // A
        it.set("X");
        assertEquals("X", list.get(0));
        assertEquals("B", list.get(1));
    }

    @Test(timeout = 4000)
    public void testIteratorSetWithPrevious() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        
        ListIterator<String> it = list.listIterator(1);
        it.previous(); // A
        it.set("X");
        assertEquals("X", list.get(0));
        assertEquals("B", list.get(1));
    }

    @Test(timeout = 4000)
    public void testComplexAddRemoveSequence() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        
        list.remove(1); // remove B
        list.add(2, "E"); // add E at index 2
        list.remove(0); // remove A
        list.set(0, "F"); // set index 0 to F
        
        assertEquals(3, list.size());
        assertEquals("F", list.get(0));
        assertEquals("C", list.get(1));
        assertEquals("E", list.get(2));
    }

    @Test(timeout = 4000)
    public void testLargeListOrdering() {
        TreeList<Integer> list = new TreeList<Integer>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        
        for (int i = 0; i < 100; i++) {
            assertEquals(Integer.valueOf(i), list.get(i));
        }
        
        // Remove from various positions
        for (int i = 0; i < 50; i++) {
            list.remove(0);
        }
        
        assertEquals(50, list.size());
        for (int i = 0; i < 50; i++) {
            assertEquals(Integer.valueOf(i + 50), list.get(i));
        }
    }

    @Test(timeout = 4000)
    public void testToArrayWithMultipleElements() {
        TreeList<Integer> list = new TreeList<Integer>();
        list.add(5);
        list.add(3);
        list.add(1);
        list.add(4);
        list.add(2);
        
        Object[] arr = list.toArray();
        assertArrayEquals(new Object[]{5, 3, 1, 4, 2}, arr);
    }

    @Test(timeout = 4000)
    public void testAddAllWithCollection() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.addAll(Arrays.asList("B", "C", "D"));
        assertEquals(4, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals("D", list.get(3));
    }

    @Test(timeout = 4000)
    public void testIndexAfterModifications() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("A");
        list.add("B");
        
        assertEquals(0, list.indexOf("A"));
        list.remove(0);
        assertEquals(2, list.indexOf("A")); // should find the second occurrence
    }

    @Test(timeout = 4000)
    public void testIteratorAddThenNext() {
        TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("C");
        
        ListIterator<String> it = list.listIterator(1);
        it.previous(); // A
        it.add("B"); // add B after A, list becomes [A, B, C]
        
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        
        // Next should return C (the element that was before the add)
        assertEquals("C", it.next());
    }
}