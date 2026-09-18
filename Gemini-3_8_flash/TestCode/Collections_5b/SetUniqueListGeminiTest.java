/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections.list;

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
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * White-Box Test Suite for {@link SetUniqueList}.
 *
 * /* [Branch & Defect Analysis Matrix]
 * **********************************************************************************************
 * Method / Branch Targeted                  | Specific Input / Condition                | Expected Behavior
 * ------------------------------------------+-------------------------------------------+---------------------------------------
 * decorate(List) (null list)                | list = null                               | IllegalArgumentException
 * decorate(List) (empty list)               | list = new ArrayList()                    | Returns empty SetUniqueList
 * decorate(List) (duplicate elements)       | ["A", "B", "A", "C"]                      | Preserves order & uniqueness ["A","B","C"]
 * SetUniqueList(List, Set) (null set)       | set = null                                | IllegalArgumentException
 * SetUniqueList(List, Set) (null list)      | list = null                               | IllegalArgumentException
 * asSet()                                   | modification attempt                      | UnsupportedOperationException
 * add(Object) (unique vs duplicate)         | "A" (new) vs "A" (existing)               | returns true, then returns false
 * add(int, Object) (unique vs duplicate)    | index 0, new element vs existing element  | inserts when unique, ignores duplicate
 * addAll(Collection) (all duplicates)       | coll with already existing elements       | returns false, size unchanged
 * addAll(int, Collection) [DEFECT TARGET]   | addAll(0, [2, 3]) on list containing [1]  | Elements inserted at index 0: [2, 3, 1]
 * set(int, Object) (new element)            | set(0, "X") where "X" not in list         | Old returned, "X" added, old removed
 * set(int, Object) (same pos)               | set(0, "A") where "A" is at index 0       | Old ("A") returned, list unchanged
 * set(int, Object) (pos != index, duplicate)| set(0, "C") where "C" is at index 2       | Duplication resolved, order updated
 * remove(Object)                            | object present vs absent                  | Returns true / false, set updated
 * remove(int)                               | valid index                               | Returns removed element, set updated
 * removeAll(Collection)                     | matching subcollection                    | Returns true, set & list updated
 * retainAll(Collection)                     | partial matching collection               | Returns true, retains only matched
 * clear()                                   | populated list                            | Both list and internal set become empty
 * contains(Object) & containsAll(Coll)      | present / absent elements                 | Accurately reflects set membership
 * iterator() remove()                       | iterator.next() then remove()             | Removes from both list and backing set
 * listIterator() navigation & add()         | next(), previous(), add() duplicate/uniq  | Respects uniqueness, updates set
 * listIterator() set()                      | listIterator.set("any")                   | UnsupportedOperationException
 * subList(int, int)                         | subList(1, 3)                             | Returns SetUniqueList view
 * serialization roundtrip                   | populated SetUniqueList                   | Deserializes with intact uniqueness & order
 * **********************************************************************************************
 */
public class SetUniqueListGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDecorateAndInitialState() {
        List<String> rawList = new ArrayList<>();
        rawList.add("A");
        rawList.add("B");
        rawList.add("A");
        rawList.add("C");

        SetUniqueList uniqueList = SetUniqueList.decorate(rawList);

        assertEquals(3, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
        assertEquals("C", uniqueList.get(2));
        assertTrue(uniqueList.contains("A"));
        assertTrue(uniqueList.contains("B"));
        assertTrue(uniqueList.contains("C"));
        assertFalse(uniqueList.contains("NonExistent"));
    }

    @Test(timeout = 4000)
    public void testAdd() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        assertTrue(list.add("First"));
        assertEquals(1, list.size());
        assertTrue(list.contains("First"));

        // Reject duplicate
        assertFalse(list.add("First"));
        assertEquals(1, list.size());
    }

    @Test(timeout = 4000)
    public void testAddAtIndex() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("B");
        list.add(0, "A"); // Unique, insert at 0

        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));

        // Attempt to insert duplicate at index 0 - should be ignored
        list.add(0, "B");
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
    }

    @Test(timeout = 4000)
    public void testAddAll() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("A");

        boolean changed = list.addAll(Arrays.asList("B", "C", "A", "D"));
        assertTrue(changed);
        assertEquals(4, list.size());
        assertEquals(Arrays.asList("A", "B", "C", "D"), list);

        // Add collection where all exist
        boolean changedAgain = list.addAll(Arrays.asList("A", "B"));
        assertFalse(changedAgain);
        assertEquals(4, list.size());
    }

    @Test(timeout = 4000)
    public void testSetNormalReplacement() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("A");
        list.add("B");
        list.add("C");

        Object old = list.set(1, "X");
        assertEquals("B", old);
        assertEquals(3, list.size());
        assertEquals(Arrays.asList("A", "X", "C"), list);
        assertFalse(list.contains("B"));
        assertTrue(list.contains("X"));
    }

    @Test(timeout = 4000)
    public void testSetSameElementAtSameIndex() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("A");
        list.add("B");

        Object old = list.set(1, "B");
        assertEquals("B", old);
        assertEquals(2, list.size());
        assertEquals("B", list.get(1));
    }

    @Test(timeout = 4000)
    public void testSetDuplicateElementAtDifferentIndex() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("A");
        list.add("B");
        list.add("C");

        // Set index 0 to "C", which was previously at index 2
        Object old = list.set(0, "C");
        assertEquals("A", old);
        // "A" is removed, "C" moved to index 0, old index of "C" removed
        assertEquals(2, list.size());
        assertEquals("C", list.get(0));
        assertEquals("B", list.get(1));
        assertFalse(list.contains("A"));
        assertTrue(list.contains("B"));
        assertTrue(list.contains("C"));
    }

    @Test(timeout = 4000)
    public void testRemoveByObject() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("A");
        list.add("B");

        assertTrue(list.remove("A"));
        assertFalse(list.contains("A"));
        assertEquals(1, list.size());

        assertFalse(list.remove("NotPresent"));
        assertEquals(1, list.size());
    }

    @Test(timeout = 4000)
    public void testRemoveByIndex() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("A");
        list.add("B");

        Object removed = list.remove(0);
        assertEquals("A", removed);
        assertFalse(list.contains("A"));
        assertEquals(1, list.size());
        assertEquals("B", list.get(0));
    }

    @Test(timeout = 4000)
    public void testRemoveAllAndRetainAll() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");

        assertTrue(list.removeAll(Arrays.asList("A", "C", "Z")));
        assertEquals(2, list.size());
        assertEquals(Arrays.asList("B", "D"), list);
        assertFalse(list.contains("A"));
        assertFalse(list.contains("C"));

        assertFalse(list.removeAll(Collections.singletonList("NotFound")));

        assertTrue(list.retainAll(Collections.singletonList("B")));
        assertEquals(1, list.size());
        assertEquals("B", list.get(0));
        assertFalse(list.contains("D"));

        assertFalse(list.retainAll(Collections.singletonList("B")));
    }

    @Test(timeout = 4000)
    public void testClear() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("A");
        list.add("B");
        list.clear();

        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        assertFalse(list.contains("A"));
        assertFalse(list.contains("B"));
    }

    @Test(timeout = 4000)
    public void testContainsAll() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("A");
        list.add("B");

        assertTrue(list.containsAll(Arrays.asList("A", "B")));
        assertTrue(list.containsAll(Collections.singletonList("A")));
        assertFalse(list.containsAll(Arrays.asList("A", "C")));
    }

    @Test(timeout = 4000)
    public void testAsSet() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("A");
        list.add("B");

        Set setView = list.asSet();
        assertEquals(2, setView.size());
        assertTrue(setView.contains("A"));
        assertTrue(setView.contains("B"));

        try {
            setView.add("C");
            fail("asSet() should return an unmodifiable set");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSubList() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("A");
        list.add("B");
        list.add("C");

        List sub = list.subList(1, 3);
        assertTrue(sub instanceof SetUniqueList);
        assertEquals(2, sub.size());
        assertEquals("B", sub.get(0));
        assertEquals("C", sub.get(1));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testDecorateEmptyList() {
        List empty = new ArrayList();
        SetUniqueList unique = SetUniqueList.decorate(empty);
        assertTrue(unique.isEmpty());
        assertEquals(0, unique.size());
    }

    @Test(timeout = 4000)
    public void testAddNullElement() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        assertTrue(list.add(null));
        assertTrue(list.contains(null));
        assertEquals(1, list.size());

        // Duplicate null
        assertFalse(list.add(null));
        assertEquals(1, list.size());

        assertTrue(list.remove(null));
        assertEquals(0, list.size());
        assertFalse(list.contains(null));
    }

    @Test(timeout = 4000)
    public void testAddAllWithEmptyCollection() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("A");
        assertFalse(list.addAll(Collections.emptyList()));
        assertEquals(1, list.size());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // Defects4J Known Defect:
    // org.apache.commons.collections.list.TestSetUniqueList::testIntCollectionAddAll
    // --> junit.framework.AssertionFailedError: First new element should be at index 0 expected:<2> but was:<1>
    // =========================================================================

    @Test(timeout = 4000)
    public void testIntCollectionAddAll() {
        // Target defect where addAll(int index, Collection coll) ignored index parameter
        // and appended to the end of the list.
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add(new Integer(1));

        List<Integer> toAdd = Arrays.asList(new Integer(2), new Integer(3));
        boolean result = list.addAll(0, toAdd);

        assertTrue("List should have changed", result);
        assertEquals("Size should be 3", 3, list.size());
        assertEquals("First new element should be at index 0", new Integer(2), list.get(0));
        assertEquals("Second new element should be at index 1", new Integer(3), list.get(1));
        assertEquals("Original element should have shifted to index 2", new Integer(1), list.get(2));
    }

    @Test(timeout = 4000)
    public void testAddAllAtIndexWithDuplicates() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("A");
        list.add("D");

        // Insert at index 1: "B", duplicate "A", "C"
        boolean result = list.addAll(1, Arrays.asList("B", "A", "C"));
        assertTrue(result);
        assertEquals(4, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals("D", list.get(3));
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
    public void testRemoveInvalidIndexThrowsException() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.remove(0);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testSetInvalidIndexThrowsException() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.set(0, "A");
    }

    // =========================================================================
    // Partition E: Iterators & Object Lifecycle Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testIteratorOperations() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("A");
        list.add("B");
        list.add("C");

        Iterator it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        it.remove(); // removes "B"

        assertFalse(list.contains("B"));
        assertEquals(2, list.size());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());

        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testListIteratorForwardBackwardAndAdd() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("A");
        list.add("C");

        ListIterator it = list.listIterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());

        // Add unique element via ListIterator
        it.add("B");
        assertTrue(list.contains("B"));

        // Attempt to add duplicate via ListIterator
        it.add("A"); // should be ignored
        assertEquals(3, list.size());

        assertTrue(it.hasPrevious());
        assertEquals("B", it.previous());
        assertEquals("A", it.previous());
        assertFalse(it.hasPrevious());

        // Test listIterator(index)
        ListIterator itIndex = list.listIterator(1);
        assertEquals("B", itIndex.next());

        // Test remove via ListIterator
        itIndex.remove();
        assertFalse(list.contains("B"));
        assertEquals(2, list.size());
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testListIteratorSetThrowsUnsupportedOperationException() {
        SetUniqueList list = SetUniqueList.decorate(new ArrayList<>());
        list.add("A");
        ListIterator it = list.listIterator();
        it.next();
        it.set("Z");
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        SetUniqueList original = SetUniqueList.decorate(new ArrayList<>());
        original.add("One");
        original.add("Two");
        original.add("Three");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SetUniqueList deserialized = (SetUniqueList) ois.readObject();

        assertEquals(original, deserialized);
        assertEquals(original.size(), deserialized.size());
        assertTrue(deserialized.contains("One"));
        assertTrue(deserialized.contains("Two"));
        assertTrue(deserialized.contains("Three"));

        // Ensure uniqueness behavior persists after deserialization
        assertFalse(deserialized.add("One"));
        assertTrue(deserialized.add("Four"));
        assertEquals(4, deserialized.size());
    }
}