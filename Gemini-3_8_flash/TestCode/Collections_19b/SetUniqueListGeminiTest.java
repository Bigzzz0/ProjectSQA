package org.apache.commons.collections.list;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
 *
 * Class Under Test: org.apache.commons.collections.list.SetUniqueList
 *
 * Target Defect (Defects4J / COLLECTIONS-444):
 *   - Method: set(int index, E object)
 *   - Problem: When setting an element to an index where the identical element already exists
 *     (pos == index), set.remove(removed) is invoked where removed == object, erroneously purging
 *     the element from the internal uniqueness tracking Set while retaining it in the decorated List.
 *     Subsequent calls to contains(object) return false, and re-adding allows duplicates.
 *
 * Decision / Branch Matrix:
 *   1. setUniqueList(List list):
 *      - list == null -> throws IllegalArgumentException
 *      - list.isEmpty() -> returns empty SetUniqueList with HashSet
 *      - list has elements (with / without duplicates) -> clears original list, retains order & uniqueness
 *   2. Constructor(List list, Set set):
 *      - set == null -> throws IllegalArgumentException
 *      - list == null -> throws IllegalArgumentException
 *      - valid list & set -> sets decorator fields
 *   3. asSet():
 *      - returns unmodifiable view, mutating throws UnsupportedOperationException
 *   4. add(E object) & add(int index, E object):
 *      - element not in set -> inserted into list and set, returns true / changes size
 *      - element already in set -> ignored, returns false / size unchanged
 *   5. addAll(Collection coll) & addAll(int index, Collection coll):
 *      - coll with all unique new elements -> inserted, returns true
 *      - coll with duplicates internal to coll or already in list -> only unique new added
 *      - coll with no new elements -> returns false
 *   6. set(int index, E object):
 *      - pos == -1 (brand new element) -> replaces element, updates set
 *      - pos == index (same element at target slot) -> COLLECTIONS-444 bug trigger
 *      - pos != -1 && pos != index (moving existing element) -> removes duplicate at pos, updates set
 *        * pos < index
 *        * pos > index
 *   7. remove(Object) & remove(int):
 *      - existing element -> removed from both list and set, returns true / removed element
 *      - non-existing element -> returns false
 *   8. removeAll(Collection coll):
 *      - elements present -> removed, returns true
 *      - elements not present -> returns false
 *   9. retainAll(Collection coll):
 *      - retain set size == current set size -> returns false
 *      - retain set size == 0 -> clear(), returns true
 *      - retain set size between 0 and current set size -> filtered via iterator, returns true
 *  10. clear(), contains(Object), containsAll(Collection):
 *      - verify synchronization between decorated list and set
 *  11. iterator() & listIterator():
 *      - SetListIterator / SetListListIterator navigation and removal
 *      - SetListListIterator.add(E): adds only if not present
 *      - SetListListIterator.set(E): throws UnsupportedOperationException
 *  12. subList(int, int) & createSetBasedOnList(Set, List):
 *      - HashSet type cloning
 *      - Non-HashSet type cloning (e.g., TreeSet, LinkedHashSet)
 *      - Inaccessible / private constructor fallback to HashSet
 *  13. Serialization:
 *      - Round-trip serialization preserves list and set state
 */
public class SetUniqueListGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J COLLECTIONS-444)
    // =========================================================================

    /**
     * Directly tests COLLECTIONS-444:
     * When replacing an element at index with itself (or an equal element), the internal
     * set must NOT remove the element.
     */
    @Test(timeout = 4000)
    public void testSetCollections444() {
        final SetUniqueList<Integer> list = new SetUniqueList<Integer>(new ArrayList<Integer>(), new HashSet<Integer>());
        final Integer val1 = new Integer(1);
        final Integer val2 = new Integer(2);
        list.add(val1);
        list.add(val2);

        // Replacing index 0 with val1 (identical element)
        final Integer old = list.set(0, val1);
        assertEquals("Previous object returned should be val1", val1, old);
        assertEquals("List size should remain 2", 2, list.size());
        assertEquals("Element at 0 should be val1", val1, list.get(0));
        assertEquals("Element at 1 should be val2", val2, list.get(1));

        // The critical failure in COLLECTIONS-444: val1 was removed from the internal set!
        assertTrue("Internal set must still contain val1", list.contains(val1));
        assertTrue("Internal set must still contain val2", list.contains(val2));

        // Verification of state integrity: attempting to add val1 again should fail
        assertFalse("Adding val1 again must return false because it already exists", list.add(val1));
        assertEquals("List size must still be 2", 2, list.size());
    }

    /**
     * Tests set(index, object) when the object already exists in the list at a different index (pos < index).
     */
    @Test(timeout = 4000)
    public void testSetExistingElementPosLessThanIndex() {
        final List<String> backing = new ArrayList<String>();
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(backing);
        list.add("A");
        list.add("B");
        list.add("C");

        // "A" is currently at index 0. We set index 2 ("C") to "A".
        final String removed = list.set(2, "A");
        assertEquals("C", removed);
        assertEquals(2, list.size());
        assertEquals("B", list.get(0));
        assertEquals("A", list.get(1));
        assertTrue(list.contains("A"));
        assertTrue(list.contains("B"));
        assertFalse(list.contains("C"));
    }

    /**
     * Tests set(index, object) when the object already exists in the list at a different index (pos > index).
     */
    @Test(timeout = 4000)
    public void testSetExistingElementPosGreaterThanIndex() {
        final List<String> backing = new ArrayList<String>();
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(backing);
        list.add("A");
        list.add("B");
        list.add("C");

        // "C" is currently at index 2. We set index 0 ("A") to "C".
        final String removed = list.set(0, "C");
        assertEquals("A", removed);
        assertEquals(2, list.size());
        assertEquals("C", list.get(0));
        assertEquals("B", list.get(1));
        assertTrue(list.contains("C"));
        assertTrue(list.contains("B"));
        assertFalse(list.contains("A"));
    }

    /**
     * Tests set(index, object) when the object is completely new to the list.
     */
    @Test(timeout = 4000)
    public void testSetCompletelyNewElement() {
        final List<String> backing = new ArrayList<String>();
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(backing);
        list.add("A");
        list.add("B");

        final String removed = list.set(1, "X");
        assertEquals("B", removed);
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("X", list.get(1));
        assertTrue(list.contains("A"));
        assertTrue(list.contains("X"));
        assertFalse(list.contains("B"));
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactoryMethodWithPopulatedListContainingDuplicates() {
        final List<String> source = new ArrayList<String>(Arrays.asList("alpha", "beta", "alpha", "gamma", "beta"));
        final SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(source);

        assertEquals(3, uniqueList.size());
        assertEquals("alpha", uniqueList.get(0));
        assertEquals("beta", uniqueList.get(1));
        assertEquals("gamma", uniqueList.get(2));
        assertTrue(uniqueList.contains("alpha"));
        assertTrue(uniqueList.contains("beta"));
        assertTrue(uniqueList.contains("gamma"));
    }

    @Test(timeout = 4000)
    public void testFactoryMethodWithEmptyList() {
        final List<String> source = new ArrayList<String>();
        final SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(source);

        assertEquals(0, uniqueList.size());
        assertTrue(uniqueList.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAddAndAddAtIndex() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());

        assertTrue(list.add("first"));
        assertFalse("Duplicate add must return false", list.add("first"));
        assertEquals(1, list.size());

        // add at index
        list.add(0, "second");
        assertEquals(2, list.size());
        assertEquals("second", list.get(0));
        assertEquals("first", list.get(1));

        // add duplicate at index (should be ignored)
        list.add(1, "first");
        assertEquals(2, list.size());
        assertEquals("second", list.get(0));
        assertEquals("first", list.get(1));
    }

    @Test(timeout = 4000)
    public void testAddAllAppend() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("one");

        final List<String> toAdd = Arrays.asList("two", "one", "three", "two");
        final boolean changed = list.addAll(toAdd);

        assertTrue("Collection should have changed", changed);
        assertEquals(3, list.size());
        assertEquals("one", list.get(0));
        assertEquals("two", list.get(1));
        assertEquals("three", list.get(2));

        final boolean changedAgain = list.addAll(Arrays.asList("one", "three"));
        assertFalse("Collection should not change when adding all duplicates", changedAgain);
        assertEquals(3, list.size());
    }

    @Test(timeout = 4000)
    public void testAddAllAtIndex() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("A");
        list.add("D");

        final List<String> toInsert = Arrays.asList("B", "A", "C");
        final boolean changed = list.addAll(1, toInsert);

        assertTrue(changed);
        assertEquals(4, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals("D", list.get(3));
    }

    @Test(timeout = 4000)
    public void testRemoveObjectAndRemoveIndex() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("X");
        list.add("Y");
        list.add("Z");

        // remove by index
        final String removedIndex = list.remove(1);
        assertEquals("Y", removedIndex);
        assertEquals(2, list.size());
        assertFalse(list.contains("Y"));

        // remove by object
        assertTrue(list.remove("X"));
        assertEquals(1, list.size());
        assertFalse(list.contains("X"));
        assertEquals("Z", list.get(0));

        // remove non-existent object
        assertFalse(list.remove("NOT_EXISTENT"));
        assertEquals(1, list.size());
    }

    @Test(timeout = 4000)
    public void testRemoveAll() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.addAll(Arrays.asList("1", "2", "3", "4"));

        final boolean changed = list.removeAll(Arrays.asList("2", "4", "5"));
        assertTrue(changed);
        assertEquals(2, list.size());
        assertEquals("1", list.get(0));
        assertEquals("3", list.get(1));
        assertFalse(list.contains("2"));
        assertFalse(list.contains("4"));

        final boolean changedNoMatch = list.removeAll(Arrays.asList("99", "100"));
        assertFalse(changedNoMatch);
        assertEquals(2, list.size());
    }

    @Test(timeout = 4000)
    public void testRetainAllScenarios() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.addAll(Arrays.asList("red", "green", "blue"));

        // Scenario 1: Retain set equals current set size -> returns false
        assertFalse(list.retainAll(Arrays.asList("red", "green", "blue", "yellow")));
        assertEquals(3, list.size());

        // Scenario 2: Retain subset -> modifies list, returns true
        assertTrue(list.retainAll(Arrays.asList("red", "blue")));
        assertEquals(2, list.size());
        assertEquals("red", list.get(0));
        assertEquals("blue", list.get(1));
        assertFalse(list.contains("green"));

        // Scenario 3: Retain empty / disjoint collection -> clears list, returns true
        assertTrue(list.retainAll(Collections.singletonList("purple")));
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        assertFalse(list.contains("red"));
    }

    @Test(timeout = 4000)
    public void testClearAndContainsAll() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.addAll(Arrays.asList("apple", "banana"));

        assertTrue(list.contains("apple"));
        assertTrue(list.containsAll(Arrays.asList("apple", "banana")));
        assertFalse(list.containsAll(Arrays.asList("apple", "orange")));

        list.clear();
        assertEquals(0, list.size());
        assertFalse(list.contains("apple"));
        assertFalse(list.contains("banana"));
    }

    @Test(timeout = 4000)
    public void testAsSetUnmodifiable() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("test");
        final Set<String> setView = list.asSet();

        assertEquals(1, setView.size());
        assertTrue(setView.contains("test"));

        try {
            setView.add("forbidden");
            fail("Mutating asSet() view should throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException expected) {
            // Expected behavior
        }
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS & ITERATOR EDGE CASES
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetListIteratorIterationAndRemoval() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.addAll(Arrays.asList("A", "B", "C"));

        final Iterator<String> it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        it.remove();

        assertEquals(2, list.size());
        assertFalse(list.contains("A"));
        assertEquals("B", list.get(0));

        assertEquals("B", it.next());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testSetListListIteratorFullLifecycle() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.addAll(Arrays.asList("one", "two", "three"));

        final ListIterator<String> lit = list.listIterator();
        assertTrue(lit.hasNext());
        assertFalse(lit.hasPrevious());

        assertEquals("one", lit.next());
        assertEquals("one", lit.previous());
        assertEquals("one", lit.next());

        // add non-duplicate via ListIterator
        lit.add("inserted");
        assertTrue(list.contains("inserted"));
        assertEquals("two", lit.next());

        // add duplicate via ListIterator (should be ignored)
        lit.add("one");
        assertFalse(list.get(lit.nextIndex() - 1).equals("one") && list.indexOf("one") != 0);

        // remove via ListIterator
        lit.remove();
        assertFalse(list.contains("two"));

        // listIterator at index
        final ListIterator<String> litIndex = list.listIterator(1);
        assertEquals("inserted", litIndex.next());
    }

    @Test(timeout = 4000)
    public void testSetListListIteratorSetThrowsUnsupported() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.add("element");

        final ListIterator<String> lit = list.listIterator();
        lit.next();
        try {
            lit.set("newElement");
            fail("ListIterator.set() must throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException expected) {
            assertEquals("ListIterator does not support set", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSubListBehavior() {
        final SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<String>());
        list.addAll(Arrays.asList("A", "B", "C", "D", "E"));

        final List<String> sub = list.subList(1, 4);
        assertEquals(3, sub.size());
        assertEquals("B", sub.get(0));
        assertEquals("C", sub.get(1));
        assertEquals("D", sub.get(2));

        assertTrue(sub instanceof SetUniqueList);
        final SetUniqueList<String> uniqueSub = (SetUniqueList<String>) sub;
        assertTrue(uniqueSub.contains("B"));
        assertFalse(uniqueSub.contains("A"));

        // Modifying subList affects parent list
        uniqueSub.remove("C");
        assertEquals(4, list.size());
        assertFalse(list.contains("C"));
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactoryMethodNullListThrows() {
        SetUniqueList.setUniqueList(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullSetThrows() {
        new SetUniqueList<String>(new ArrayList<String>(), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullListThrows() {
        new SetUniqueList<String>(null, new HashSet<String>());
    }

    @Test(timeout = 4000)
    public void testCreateSetBasedOnListWithVariousSetTypes() {
        // HashSet branch
        final SetUniqueList<Integer> hashSetList = new SetUniqueList<Integer>(new ArrayList<Integer>(), new HashSet<Integer>());
        final Set<Integer> createdSet1 = hashSetList.createSetBasedOnList(hashSetList.set, Arrays.asList(1, 2, 3));
        assertEquals(HashSet.class, createdSet1.getClass());
        assertEquals(3, createdSet1.size());

        // TreeSet branch (non-HashSet)
        final SetUniqueList<Integer> treeSetList = new SetUniqueList<Integer>(new ArrayList<Integer>(), new TreeSet<Integer>());
        final Set<Integer> createdSet2 = treeSetList.createSetBasedOnList(treeSetList.set, Arrays.asList(4, 5, 6));
        assertEquals(TreeSet.class, createdSet2.getClass());
        assertEquals(3, createdSet2.size());

        // Custom set with inaccessible constructor to hit catch blocks
        final Set<Integer> uninstantiableSet = new InaccessibleSet<Integer>();
        final SetUniqueList<Integer> customList = new SetUniqueList<Integer>(new ArrayList<Integer>(), uninstantiableSet);
        final Set<Integer> fallbackSet = customList.createSetBasedOnList(uninstantiableSet, Arrays.asList(7, 8));
        assertEquals(HashSet.class, fallbackSet.getClass());
        assertEquals(2, fallbackSet.size());
    }

    // Helper class with private constructor to test InstantiationException/IllegalAccessException branch
    public static class InaccessibleSet<E> extends HashSet<E> {
        private static final long serialVersionUID = 1L;

        private InaccessibleSet() {
            super();
        }
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE, INTEGRITY & SERIALIZATION
    // =========================================================================

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testSerializationRoundTrip() throws Exception {
        final List<String> backing = new ArrayList<String>();
        final SetUniqueList<String> original = SetUniqueList.setUniqueList(backing);
        original.add("alpha");
        original.add("beta");
        original.add("gamma");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final SetUniqueList<String> deserialized = (SetUniqueList<String>) ois.readObject();
        ois.close();

        assertEquals(original.size(), deserialized.size());
        assertEquals(original, deserialized);
        assertTrue(deserialized.contains("alpha"));
        assertTrue(deserialized.contains("beta"));
        assertTrue(deserialized.contains("gamma"));

        // Verify uniqueness check still works post-deserialization
        assertFalse(deserialized.add("alpha"));
        assertEquals(3, deserialized.size());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        final SetUniqueList<String> list1 = SetUniqueList.setUniqueList(new ArrayList<String>());
        list1.addAll(Arrays.asList("1", "2", "3"));

        final SetUniqueList<String> list2 = SetUniqueList.setUniqueList(new ArrayList<String>());
        list2.addAll(Arrays.asList("1", "2", "3"));

        final List<String> standardList = Arrays.asList("1", "2", "3");

        assertEquals(list1, list2);
        assertEquals(list1, standardList);
        assertEquals(list1.hashCode(), standardList.hashCode());

        list2.remove("2");
        assertFalse(list1.equals(list2));
    }
}