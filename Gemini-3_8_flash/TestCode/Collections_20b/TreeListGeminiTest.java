/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.collections.list.TreeList and inner classes (AVLNode, TreeListIterator)
 *
 * Specific Defect Targeted:
 * - COLLECTIONS-447 / Defects4J: TreeListIterator.remove() stale node reference bug.
 *   When remove() is invoked, the cached 'next' AVL node is not cleared (next = null missing),
 *   causing subsequent calls to previous() to traverse stale node pointers and return
 *   an incorrect element (e.g. expected <[A]> but was <[C]>).
 *
 * Structural & Branch Coverage Targets:
 * 1. TreeList Constructors: default empty list, collection-copying constructor (valid & null).
 * 2. TreeList Operations: get, set, add(index, E), remove(index), clear, size, indexOf, contains, toArray.
 * 3. AVL Node Traversal & Balancing:
 *    - Insert on Left / Insert on Right
 *    - Rotations: Left-Left (LL), Right-Right (RR), Left-Right (LR), Right-Left (RL)
 *    - Removals: Leaf removal, single-child (left-only, right-only), two-child deletions
 *    - RemoveSelf: heightRightMinusLeft() > 0 vs <= 0; leftMax / rightMin extraction
 * 4. TreeListIterator:
 *    - hasNext, next, hasPrevious, previous
 *    - Boundary start at 0, middle, and list.size() (testing next == null recovery in previous())
 *    - remove() following next() vs remove() following previous() (nextIndex == currentIndex vs nextIndex != currentIndex)
 *    - set() and add() through iterator
 *    - ConcurrentModificationException guards on structural modifications
 *    - IllegalStateException guards on invalid remove() / set() invocations
 *    - NoSuchElementException guards when cursor exceeds bounds
 */

package org.apache.commons.collections.list;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class TreeListGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyListState() {
        final TreeList<String> list = new TreeList<String>();
        assertEquals("Size of new list should be 0", 0, list.size());
        assertTrue("New list should be empty", list.isEmpty());
        assertEquals("IndexOf on empty list should return -1", -1, list.indexOf("A"));
        assertFalse("Contains on empty list should return false", list.contains("A"));
        assertArrayEquals("toArray on empty list should return empty array", new Object[0], list.toArray());
    }

    @Test(timeout = 4000)
    public void testSequentialAddAndGet() {
        final TreeList<Integer> list = new TreeList<Integer>();
        for (int i = 0; i < 10; i++) {
            list.add(i, Integer.valueOf(i * 10));
        }

        assertEquals("Size should be 10", 10, list.size());
        for (int i = 0; i < 10; i++) {
            assertEquals("Retrieved value should match inserted value",
                    Integer.valueOf(i * 10), list.get(i));
        }
    }

    @Test(timeout = 4000)
    public void testAddAtArbitraryIndices() {
        final TreeList<String> list = new TreeList<String>();
        list.add(0, "A"); // [A]
        list.add(1, "C"); // [A, C]
        list.add(1, "B"); // [A, B, C]
        list.add(0, "START"); // [START, A, B, C]
        list.add(4, "END"); // [START, A, B, C, END]

        assertEquals(5, list.size());
        assertEquals("START", list.get(0));
        assertEquals("A", list.get(1));
        assertEquals("B", list.get(2));
        assertEquals("C", list.get(3));
        assertEquals("END", list.get(4));
    }

    @Test(timeout = 4000)
    public void testSetElement() {
        final TreeList<String> list = new TreeList<String>();
        list.add(0, "First");
        list.add(1, "Second");

        final String old = list.set(1, "Updated");
        assertEquals("Should return previous value", "Second", old);
        assertEquals("Should store new value", "Updated", list.get(1));
        assertEquals("Size should remain unchanged", 2, list.size());
    }

    @Test(timeout = 4000)
    public void testRemoveByIndex() {
        final TreeList<String> list = new TreeList<String>();
        list.add(0, "A");
        list.add(1, "B");
        list.add(2, "C");

        final String removed = list.remove(1);
        assertEquals("Removed element should be B", "B", removed);
        assertEquals("Size should decrease", 2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("C", list.get(1));
    }

    @Test(timeout = 4000)
    public void testClear() {
        final TreeList<String> list = new TreeList<String>();
        list.add(0, "A");
        list.add(1, "B");
        list.clear();

        assertEquals("Size should be 0 after clear", 0, list.size());
        assertTrue("List should be empty after clear", list.isEmpty());
        assertEquals(-1, list.indexOf("A"));
    }

    @Test(timeout = 4000)
    public void testCollectionConstructor() {
        final Collection<String> source = Arrays.asList("Alpha", "Beta", "Gamma");
        final TreeList<String> list = new TreeList<String>(source);

        assertEquals(3, list.size());
        assertEquals("Alpha", list.get(0));
        assertEquals("Beta", list.get(1));
        assertEquals("Gamma", list.get(2));
    }

    @Test(timeout = 4000)
    public void testContainsAndIndexOf() {
        final TreeList<String> list = new TreeList<String>();
        list.add(0, "One");
        list.add(1, "Two");
        list.add(2, "Three");
        list.add(3, "Two");

        assertTrue(list.contains("One"));
        assertTrue(list.contains("Two"));
        assertFalse(list.contains("Four"));

        assertEquals(0, list.indexOf("One"));
        assertEquals(1, list.indexOf("Two")); // must return first occurrence
        assertEquals(2, list.indexOf("Three"));
        assertEquals(-1, list.indexOf("NonExistent"));
    }

    @Test(timeout = 4000)
    public void testToArray() {
        final TreeList<Integer> list = new TreeList<Integer>();
        list.add(0, Integer.valueOf(1));
        list.add(1, Integer.valueOf(2));
        list.add(2, Integer.valueOf(3));

        final Object[] array = list.toArray();
        assertNotNull(array);
        assertEquals(3, array.length);
        assertEquals(Integer.valueOf(1), array[0]);
        assertEquals(Integer.valueOf(2), array[1]);
        assertEquals(Integer.valueOf(3), array[2]);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & AVL Balancing Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullElementHandling() {
        final TreeList<String> list = new TreeList<String>();
        list.add(0, "A");
        list.add(1, null);
        list.add(2, "B");

        assertEquals(3, list.size());
        assertNull(list.get(1));
        assertTrue("List should contain null", list.contains(null));
        assertEquals("IndexOf null should be 1", 1, list.indexOf(null));

        final String removed = list.remove(1);
        assertNull(removed);
        assertFalse("List should no longer contain null", list.contains(null));
        assertEquals(-1, list.indexOf(null));
    }

    @Test(timeout = 4000)
    public void testAVLRotationsLL() {
        // Left-Left case triggers rotateRight
        final TreeList<Integer> list = new TreeList<Integer>();
        list.add(0, Integer.valueOf(30));
        list.add(0, Integer.valueOf(20));
        list.add(0, Integer.valueOf(10)); // Triggers rotation

        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(10), list.get(0));
        assertEquals(Integer.valueOf(20), list.get(1));
        assertEquals(Integer.valueOf(30), list.get(2));
    }

    @Test(timeout = 4000)
    public void testAVLRotationsRR() {
        // Right-Right case triggers rotateLeft
        final TreeList<Integer> list = new TreeList<Integer>();
        list.add(0, Integer.valueOf(10));
        list.add(1, Integer.valueOf(20));
        list.add(2, Integer.valueOf(30)); // Triggers rotation

        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(10), list.get(0));
        assertEquals(Integer.valueOf(20), list.get(1));
        assertEquals(Integer.valueOf(30), list.get(2));
    }

    @Test(timeout = 4000)
    public void testAVLRotationsLR() {
        // Left-Right case triggers rotateLeft then rotateRight
        final TreeList<Integer> list = new TreeList<Integer>();
        list.add(0, Integer.valueOf(30));
        list.add(0, Integer.valueOf(10));
        list.add(1, Integer.valueOf(20)); // Triggers LR rotation

        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(10), list.get(0));
        assertEquals(Integer.valueOf(20), list.get(1));
        assertEquals(Integer.valueOf(30), list.get(2));
    }

    @Test(timeout = 4000)
    public void testAVLRotationsRL() {
        // Right-Left case triggers rotateRight then rotateLeft
        final TreeList<Integer> list = new TreeList<Integer>();
        list.add(0, Integer.valueOf(10));
        list.add(1, Integer.valueOf(30));
        list.add(1, Integer.valueOf(20)); // Triggers RL rotation

        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(10), list.get(0));
        assertEquals(Integer.valueOf(20), list.get(1));
        assertEquals(Integer.valueOf(30), list.get(2));
    }

    @Test(timeout = 4000)
    public void testExtensiveAVLInsertionsAndDeletions() {
        final TreeList<Integer> list = new TreeList<Integer>();
        final int count = 64;

        for (int i = 0; i < count; i++) {
            list.add(i, Integer.valueOf(i));
        }
        assertEquals(count, list.size());

        // Interleaved removals: removing from head, middle, and tail
        while (list.size() > 0) {
            int mid = list.size() / 2;
            list.remove(mid);
            if (list.size() > 0) {
                list.remove(0);
            }
            if (list.size() > 0) {
                list.remove(list.size() - 1);
            }
        }
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @Test(timeout = 4000)
    public void testRemoveRootWithTwoSubtreesDifferentHeights() {
        // Build tree and remove root to exercise both heightRightMinusLeft() > 0 and <= 0
        final TreeList<Integer> list = new TreeList<Integer>();
        for (int i = 0; i < 7; i++) {
            list.add(i, Integer.valueOf(i));
        }

        // Tree structure balanced around index 3 (element 3)
        // Remove root or inner nodes to test deletion branches
        list.remove(3); // Remove middle
        assertEquals(6, list.size());
        list.remove(0); // Remove min
        assertEquals(5, list.size());
        list.remove(list.size() - 1); // Remove max
        assertEquals(4, list.size());

        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(4), list.get(2));
        assertEquals(Integer.valueOf(5), list.get(3));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (COLLECTIONS-447 / Defects4J)
    // =========================================================================

    /**
     * Targets known defect COLLECTIONS-447:
     * When remove() is called on TreeListIterator, it fails to set 'next = null',
     * retaining a stale node reference that corrupts subsequent previous() calls.
     */
    @Test(timeout = 4000)
    public void testBugCollections447() {
        final List<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");

        final ListIterator<String> it = list.listIterator();
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        it.remove();
        assertEquals("C", it.next());
        it.remove();
        assertEquals("A", it.previous());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testConstructorNullCollectionThrowsException() {
        new TreeList<String>((Collection<String>) null);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetNegativeIndexThrowsException() {
        final TreeList<String> list = new TreeList<String>();
        list.get(-1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetIndexEqualToSizeThrowsException() {
        final TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.get(1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testAddNegativeIndexThrowsException() {
        final TreeList<String> list = new TreeList<String>();
        list.add(-1, "X");
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testAddIndexGreaterThanSizeThrowsException() {
        final TreeList<String> list = new TreeList<String>();
        list.add(1, "X");
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testSetNegativeIndexThrowsException() {
        final TreeList<String> list = new TreeList<String>();
        list.set(-1, "X");
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testSetIndexEqualToSizeThrowsException() {
        final TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.set(1, "X");
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testRemoveNegativeIndexThrowsException() {
        final TreeList<String> list = new TreeList<String>();
        list.remove(-1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testRemoveIndexEqualToSizeThrowsException() {
        final TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.remove(1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testListIteratorNegativeIndexThrowsException() {
        final TreeList<String> list = new TreeList<String>();
        list.listIterator(-1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testListIteratorIndexGreaterThanSizeThrowsException() {
        final TreeList<String> list = new TreeList<String>();
        list.listIterator(1);
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testIteratorNextExhaustedThrowsException() {
        final TreeList<String> list = new TreeList<String>();
        final Iterator<String> it = list.iterator();
        it.next();
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testIteratorPreviousAtStartThrowsException() {
        final TreeList<String> list = new TreeList<String>();
        list.add("A");
        final ListIterator<String> it = list.listIterator();
        it.previous();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testIteratorRemoveBeforeNextThrowsException() {
        final TreeList<String> list = new TreeList<String>();
        list.add("A");
        final ListIterator<String> it = list.listIterator();
        it.remove();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testIteratorDoubleRemoveThrowsException() {
        final TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        final ListIterator<String> it = list.listIterator();
        it.next();
        it.remove();
        it.remove();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testIteratorSetBeforeNextThrowsException() {
        final TreeList<String> list = new TreeList<String>();
        list.add("A");
        final ListIterator<String> it = list.listIterator();
        it.set("B");
    }

    @Test(timeout = 4000, expected = ConcurrentModificationException.class)
    public void testIteratorConcurrentModificationOnNext() {
        final TreeList<String> list = new TreeList<String>();
        list.add("A");
        final Iterator<String> it = list.iterator();
        list.add("B");
        it.next();
    }

    @Test(timeout = 4000, expected = ConcurrentModificationException.class)
    public void testIteratorConcurrentModificationOnPrevious() {
        final TreeList<String> list = new TreeList<String>();
        list.add("A");
        final ListIterator<String> it = list.listIterator(1);
        list.add("B");
        it.previous();
    }

    @Test(timeout = 4000, expected = ConcurrentModificationException.class)
    public void testIteratorConcurrentModificationOnRemove() {
        final TreeList<String> list = new TreeList<String>();
        list.add("A");
        final ListIterator<String> it = list.listIterator();
        it.next();
        list.add("B");
        it.remove();
    }

    @Test(timeout = 4000, expected = ConcurrentModificationException.class)
    public void testIteratorConcurrentModificationOnSet() {
        final TreeList<String> list = new TreeList<String>();
        list.add("A");
        final ListIterator<String> it = list.listIterator();
        it.next();
        list.add("B");
        it.set("C");
    }

    @Test(timeout = 4000, expected = ConcurrentModificationException.class)
    public void testIteratorConcurrentModificationOnAdd() {
        final TreeList<String> list = new TreeList<String>();
        list.add("A");
        final ListIterator<String> it = list.listIterator();
        list.add("B");
        it.add("C");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & ListIterator Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testIteratorBidirectionalTraversal() {
        final TreeList<String> list = new TreeList<String>();
        list.add("1");
        list.add("2");
        list.add("3");

        final ListIterator<String> it = list.listIterator();
        assertEquals(0, it.nextIndex());
        assertEquals(-1, it.previousIndex());
        assertTrue(it.hasNext());
        assertFalse(it.hasPrevious());

        assertEquals("1", it.next());
        assertEquals(1, it.nextIndex());
        assertEquals(0, it.previousIndex());
        assertTrue(it.hasPrevious());

        assertEquals("2", it.next());
        assertEquals("3", it.next());
        assertFalse(it.hasNext());

        // Traverse backwards
        assertEquals("3", it.previous());
        assertEquals("2", it.previous());
        assertEquals("1", it.previous());
        assertTrue(it.hasNext());
        assertFalse(it.hasPrevious());
    }

    @Test(timeout = 4000)
    public void testIteratorStartingAtEnd() {
        final TreeList<String> list = new TreeList<String>();
        list.add("First");
        list.add("Second");

        final ListIterator<String> it = list.listIterator(list.size());
        assertFalse(it.hasNext());
        assertTrue(it.hasPrevious());
        assertEquals(2, it.nextIndex());
        assertEquals(1, it.previousIndex());

        // Exercises branch where next == null in previous()
        assertEquals("Second", it.previous());
        assertEquals("First", it.previous());
    }

    @Test(timeout = 4000)
    public void testIteratorAddOperation() {
        final TreeList<String> list = new TreeList<String>();
        final ListIterator<String> it = list.listIterator();
        it.add("A");
        it.add("B");

        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));

        // Adding between elements
        final ListIterator<String> it2 = list.listIterator(1);
        it2.add("Middle");
        assertEquals(3, list.size());
        assertEquals("Middle", list.get(1));
    }

    @Test(timeout = 4000)
    public void testIteratorSetOperation() {
        final TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");

        final ListIterator<String> it = list.listIterator();
        it.next(); // 'A'
        it.set("A_Modified");
        assertEquals("A_Modified", list.get(0));

        it.next(); // 'B'
        it.previous(); // 'B'
        it.set("B_Modified");
        assertEquals("B_Modified", list.get(1));
    }

    @Test(timeout = 4000)
    public void testIteratorRemoveAfterPrevious() {
        // Exercises the branch: nextIndex == currentIndex in remove()
        final TreeList<String> list = new TreeList<String>();
        list.add("A");
        list.add("B");
        list.add("C");

        final ListIterator<String> it = list.listIterator();
        it.next(); // A
        it.next(); // B
        assertEquals("B", it.previous());
        it.remove(); // removes B

        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("C", list.get(1));
        assertEquals("C", it.next());
    }

    @Test(timeout = 4000)
    public void testListEqualsAndHashCodeContract() {
        final TreeList<String> list1 = new TreeList<String>();
        list1.add("X");
        list1.add("Y");

        final List<String> list2 = new ArrayList<String>();
        list2.add("X");
        list2.add("Y");

        assertEquals("TreeList must equal another List with same elements", list1, list2);
        assertEquals("TreeList must have identical hashCode to equal List", list1.hashCode(), list2.hashCode());
    }
}