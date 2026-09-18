package org.apache.commons.collections4.list;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.collections4.list.SetUniqueList
 *
 * Decision / Branch Matrix & Targeted Scenarios:
 * 1. Factory method setUniqueList(List):
 *    - list == null -> IllegalArgumentException
 *    - list.isEmpty() == true -> returns empty SetUniqueList wrapping same list
 *    - list has elements & duplicates -> retains first occurrences, cleans duplicates
 *
 * 2. Constructor SetUniqueList(List, Set):
 *    - set == null -> IllegalArgumentException
 *    - list == null -> super NPE / constructor guard
 *
 * 3. asSet():
 *    - Returns unmodifiable view of internal set (verified modification throws UOE)
 *
 * 4. add(E) & add(int, E):
 *    - Unique element -> added, returns true, size increments
 *    - Duplicate element -> ignored, returns false / size unchanged
 *    - At index 0, mid, tail
 *
 * 5. addAll(Collection) & addAll(int, Collection):
 *    - All unique -> added, returns true
 *    - Mixed unique & duplicates -> only unique added, returns true
 *    - All duplicates / empty collection -> returns false, list unmodified
 *
 * 6. set(int, E):
 *    - Replacing element with a new unique element
 *    - Replacing element with itself (pos == index)
 *    - Replacing element with an element existing elsewhere (pos != -1 && pos != index)
 *
 * 7. remove(Object) & remove(int):
 *    - Removing present element -> returns true / removed element, removed from both set & list
 *    - Removing non-present element -> returns false
 *    - Out of bounds index -> IndexOutOfBoundsException
 *
 * 8. removeAll(Collection) & retainAll(Collection):
 *    - removeAll with matching and non-matching elements
 *    - retainAll branches:
 *      * setRetainAll.size() == set.size() (no elements removed -> returns false)
 *      * setRetainAll.size() == 0 (all removed -> clear() invoked, returns true)
 *      * partial retain (selective removal via iterator -> returns true)
 *
 * 9. clear(), contains(), containsAll():
 *    - Comprehensive state synchronization between internal list and internal set
 *
 * 10. Iterators (SetListIterator & SetListListIterator):
 *     - next(), previous(), remove() updates internal set
 *     - listIterator.add(E): unique vs duplicate
 *     - listIterator.set(E): must throw UnsupportedOperationException
 *
 * 11. subList(int, int) & createSetBasedOnList(Set, List):
 *     - HashSet branch -> new HashSet
 *     - Non-HashSet with public default constructor (e.g. TreeSet / LinkedHashSet) -> new instance
 *     - Non-HashSet with private / inaccessible constructor -> falls back to catch IllegalAccessException
 *     - Non-HashSet abstract / non-instantiable -> falls back to catch InstantiationException
 *     - DEFECTS4J TARGET: testSubListIsUnmodifiable asserts subList() is unmodifiable.
 *       According to the specification/javadoc, changes to subList can invalidate the parent,
 *       so subList must be unmodifiable (Defect: returns a modifiable SetUniqueList).
 * -----------------------------------------------------------------------------------------
 */
public class SetUniqueListGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactoryWithDuplicatesRetainsFirstOccurrence() {
        final List<String> rawList = new ArrayList<String>(Arrays.asList("A", "B", "A", "C", "B", "D"));
        final SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(rawList);

        assertEquals(4, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
        assertEquals("C", uniqueList.get(2));
        assertEquals("D", uniqueList.get(3));
        assertTrue(uniqueList.contains("A"));
        assertTrue(uniqueList.contains("B"));
        assertTrue(uniqueList.contains("C"));
        assertTrue(uniqueList.contains("D"));
    }

    @Test(timeout = 4000)
    public void testFactoryEmptyList() {
        final List<String> emptyList = new ArrayList<String>();
        final SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(emptyList);

        assertEquals(0, uniqueList.size());
        assertTrue(uniqueList.isEmpty());
        assertSame(emptyList, uniqueList.decorated());
    }

    @Test(timeout = 4000)
    public void testAddAndAddIndex() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());

        // add(E)
        assertTrue(list.add("Alpha"));
        assertFalse(list.add("Alpha")); // duplicate
        assertEquals(1, list.size());

        assertTrue(list.add("Gamma"));
        assertEquals(2, list.size());

        // add(int, E)
        list.add(1, "Beta");
        assertEquals(3, list.size());
        assertEquals("Beta", list.get(1));

        // duplicate add at index should be ignored
        list.add(0, "Beta");
        assertEquals(3, list.size());
        assertEquals("Alpha", list.get(0));
    }

    @Test(timeout = 4000)
    public void testAddAll() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("B");

        final List<String> toAdd = Arrays.asList("B", "C", "D", "A", "E");
        final boolean changed = list.addAll(toAdd);

        assertTrue(changed);
        assertEquals(5, list.size());
        assertEquals(Arrays.asList("A", "B", "C", "D", "E"), list);

        // addAll with all duplicates
        final boolean changedDuplicates = list.addAll(Arrays.asList("A", "C"));
        assertFalse(changedDuplicates);
        assertEquals(5, list.size());
    }

    @Test(timeout = 4000)
    public void testAddAllAtIndex() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "D")));

        final boolean changed = list.addAll(1, Arrays.asList("B", "C", "D"));
        assertTrue(changed);
        assertEquals(4, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals("D", list.get(3));

        final boolean changedEmpty = list.addAll(1, Collections.<String>emptyList());
        assertFalse(changedEmpty);
    }

    @Test(timeout = 4000)
    public void testSetMethod() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));

        // 1. Normal set: replaces element with a completely new one
        String removed = list.set(1, "X");
        assertEquals("B", removed);
        assertEquals(Arrays.asList("A", "X", "C"), list);
        assertFalse(list.contains("B"));
        assertTrue(list.contains("X"));

        // 2. Set replacing element with itself (pos == index)
        removed = list.set(1, "X");
        assertEquals("X", removed);
        assertEquals(Arrays.asList("A", "X", "C"), list);

        // 3. Set replacing element with an item existing elsewhere (pos != index)
        // Replacing "A" at index 0 with "C" (which is at index 2)
        removed = list.set(0, "C");
        assertEquals("A", removed);
        assertEquals(1, list.indexOf("C")); // duplicate removed
        assertEquals(2, list.size());
        assertEquals("C", list.get(0));
        assertEquals("X", list.get(1));
        assertFalse(list.contains("A"));
    }

    @Test(timeout = 4000)
    public void testRemoveObjectAndRemoveIndex() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C", "D")));

        // remove(Object)
        assertTrue(list.remove("B"));
        assertFalse(list.remove("NonExistent"));
        assertFalse(list.contains("B"));
        assertEquals(3, list.size());

        // remove(int)
        final String removed = list.remove(1); // removes "C"
        assertEquals("C", removed);
        assertFalse(list.contains("C"));
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("D", list.get(1));
    }

    @Test(timeout = 4000)
    public void testRemoveAll() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C", "D")));

        final boolean changed = list.removeAll(Arrays.asList("B", "D", "Z"));
        assertTrue(changed);
        assertEquals(2, list.size());
        assertEquals(Arrays.asList("A", "C"), list);

        final boolean changedNoMatch = list.removeAll(Arrays.asList("X", "Y"));
        assertFalse(changedNoMatch);
    }

    @Test(timeout = 4000)
    public void testRetainAllScenarios() {
        // Scenario 1: Retain all contains all current elements -> returns false
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));
        boolean changed = list.retainAll(Arrays.asList("A", "B", "C", "Extra"));
        assertFalse(changed);
        assertEquals(3, list.size());

        // Scenario 2: Retain all has no elements in common -> clear() invoked, returns true
        list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));
        changed = list.retainAll(Arrays.asList("X", "Y", "Z"));
        assertTrue(changed);
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());

        // Scenario 3: Partial retain -> selective removal via iterator, returns true
        list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C", "D")));
        changed = list.retainAll(Arrays.asList("B", "D", "F"));
        assertTrue(changed);
        assertEquals(2, list.size());
        assertEquals(Arrays.asList("B", "D"), list);
        assertTrue(list.contains("B"));
        assertTrue(list.contains("D"));
        assertFalse(list.contains("A"));
        assertFalse(list.contains("C"));
    }

    @Test(timeout = 4000)
    public void testClearAndContains() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));
        assertTrue(list.contains("A"));
        assertTrue(list.containsAll(Arrays.asList("A", "B")));
        assertFalse(list.containsAll(Arrays.asList("A", "Z")));

        list.clear();
        assertEquals(0, list.size());
        assertFalse(list.contains("A"));
        assertFalse(list.contains("B"));
        assertFalse(list.contains("C"));
    }

    @Test(timeout = 4000)
    public void testAsSetIsUnmodifiable() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B")));
        final Set<String> setView = list.asSet();

        assertEquals(2, setView.size());
        assertTrue(setView.contains("A"));
        assertTrue(setView.contains("B"));

        try {
            setView.add("C");
            fail("asSet() view should be unmodifiable");
        } catch (final UnsupportedOperationException expected) {
            // expected
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Iterators
    // =========================================================================

    @Test(timeout = 4000)
    public void testIteratorOperations() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));
        final Iterator<String> it = list.iterator();

        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        it.remove(); // removes "B"
        assertFalse(list.contains("B"));
        assertEquals(2, list.size());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());

        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (final NoSuchElementException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testListIteratorOperations() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));
        final ListIterator<String> lit = list.listIterator();

        assertTrue(lit.hasNext());
        assertFalse(lit.hasPrevious());
        assertEquals(0, lit.nextIndex());
        assertEquals(-1, lit.previousIndex());

        assertEquals("A", lit.next());
        assertEquals(1, lit.nextIndex());
        assertEquals(0, lit.previousIndex());
        assertTrue(lit.hasPrevious());
        assertEquals("A", lit.previous());

        assertEquals("A", lit.next());
        assertEquals("B", lit.next());

        // Test ListIterator.add with unique element
        lit.add("Inserted");
        assertTrue(list.contains("Inserted"));
        assertEquals(4, list.size());

        // Test ListIterator.add with duplicate element (should be ignored)
        lit.add("A");
        assertEquals(4, list.size());

        // Test ListIterator.remove
        assertEquals("C", lit.next());
        lit.remove(); // removes "C"
        assertFalse(list.contains("C"));
        assertEquals(3, list.size());

        // Test ListIterator(index)
        final ListIterator<String> litFromIndex = list.listIterator(1);
        assertEquals("Inserted", litFromIndex.next());
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testListIteratorSetThrowsUnsupportedOperationException() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B")));
        final ListIterator<String> lit = list.listIterator();
        lit.next();
        lit.set("Z"); // Must throw UnsupportedOperationException
    }

    @Test(timeout = 4000)
    public void testNullElementsSupported() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        assertTrue(list.add(null));
        assertFalse(list.add(null)); // duplicate null ignored
        assertEquals(1, list.size());
        assertTrue(list.contains(null));
        assertNull(list.get(0));

        assertTrue(list.remove(null));
        assertEquals(0, list.size());
        assertFalse(list.contains(null));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J known failure:
     * According to SetUniqueList specification/javadoc:
     * "NOTE: from 4.0, an unmodifiable list will be returned, as changes to the
     * subList can invalidate the parent list."
     *
     * Calling mutating methods on the returned subList MUST throw UnsupportedOperationException.
     */
    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSubListIsUnmodifiable() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C", "D")));
        final List<String> sub = list.subList(1, 3);
        assertEquals(2, sub.size());
        assertEquals("B", sub.get(0));
        assertEquals("C", sub.get(1));

        // In the defective version, subList returns a modifiable SetUniqueList,
        // so this modification succeeds instead of throwing UnsupportedOperationException.
        sub.add("X");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSubListRemoveIsUnmodifiable() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));
        final List<String> sub = list.subList(0, 1);
        sub.remove(0);
    }

    // =========================================================================
    // Partition D: Exception, Defensive Guards & Reflection Branches
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactoryNullListThrowsException() {
        SetUniqueList.setUniqueList(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullSetThrowsException() {
        new SetUniqueList<String>(new ArrayList<String>(), null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorNullListThrowsException() {
        new SetUniqueList<String>(null, new HashSet<String>());
    }

    // Helper classes for testing reflection branches in createSetBasedOnList
    public static class SetWithPrivateConstructor<E> extends HashSet<E> {
        private static final long serialVersionUID = 1L;
        private SetWithPrivateConstructor() {
            super();
        }
    }

    public static abstract class AbstractCustomSet<E> extends HashSet<E> {
        private static final long serialVersionUID = 1L;
    }

    @Test(timeout = 4000)
    public void testCreateSetBasedOnListBranches() {
        // Branch 1: Set is HashSet (default)
        final SetUniqueList<String> hashSetList = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B")));
        final Set<String> createdFromHashSet = hashSetList.createSetBasedOnList(new HashSet<String>(), Arrays.asList("1", "2"));
        assertEquals(HashSet.class, createdFromHashSet.getClass());
        assertEquals(2, createdFromHashSet.size());

        // Branch 2: Set is non-HashSet with public default constructor (e.g. TreeSet)
        final Set<String> treeSet = new TreeSet<String>();
        final Set<String> createdFromTreeSet = hashSetList.createSetBasedOnList(treeSet, Arrays.asList("1", "2"));
        assertEquals(TreeSet.class, createdFromTreeSet.getClass());
        assertEquals(2, createdFromTreeSet.size());

        // Branch 3: Set class throws IllegalAccessException on newInstance() -> falls back to HashSet
        final Set<String> privateSet = new SetWithPrivateConstructor<String>();
        final Set<String> createdFromPrivate = hashSetList.createSetBasedOnList(privateSet, Arrays.asList("1", "2"));
        assertEquals(HashSet.class, createdFromPrivate.getClass());
        assertEquals(2, createdFromPrivate.size());

        // Branch 4: Set class throws InstantiationException on newInstance() -> falls back to HashSet
        final Set<String> anonymousSet = new AbstractCustomSet<String>() {};
        final Set<String> createdFromAbstract = hashSetList.createSetBasedOnList(anonymousSet, Arrays.asList("1", "2"));
        assertEquals(HashSet.class, createdFromAbstract.getClass());
        assertEquals(2, createdFromAbstract.size());
    }

    @Test(timeout = 4000)
    public void testCustomSetConstructorUsage() {
        final List<Integer> backingList = new ArrayList<Integer>(Arrays.asList(3, 1, 2));
        final Set<Integer> backingSet = new TreeSet<Integer>(backingList);
        final SetUniqueList<Integer> customList = new SetUniqueList<Integer>(backingList, backingSet);

        assertEquals(3, customList.size());
        assertTrue(customList.contains(1));
        assertFalse(customList.add(2)); // duplicate in TreeSet
        assertTrue(customList.add(4));
        assertEquals(4, customList.size());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        final List<String> raw1 = new ArrayList<String>(Arrays.asList("A", "B", "C"));
        final List<String> raw2 = new ArrayList<String>(Arrays.asList("A", "B", "C"));
        final SetUniqueList<String> list1 = SetUniqueList.setUniqueList(raw1);
        final SetUniqueList<String> list2 = SetUniqueList.setUniqueList(raw2);

        assertEquals(list1, list2);
        assertEquals(list1.hashCode(), list2.hashCode());
        assertEquals(list1, Arrays.asList("A", "B", "C")); // SetUniqueList equals standard List

        list2.add("D");
        assertNotEquals(list1, list2);
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testSerializationIntegrity() throws Exception {
        final SetUniqueList<String> original = SetUniqueList.setUniqueList(new ArrayList<String>(Arrays.asList("A", "B", "C")));

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final SetUniqueList<String> deserialized = (SetUniqueList<String>) ois.readObject();

        assertEquals(original, deserialized);
        assertEquals(original.size(), deserialized.size());
        assertTrue(deserialized.contains("A"));
        // Check uniqueness constraint remains active after serialization
        assertFalse(deserialized.add("A"));
        assertTrue(deserialized.add("D"));
        assertEquals(4, deserialized.size());
    }
}