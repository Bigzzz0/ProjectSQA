package org.apache.commons.collections.list;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

/**
 * White-box test suite for SetUniqueList targeting maximal coverage and defect detection.
 * 
 * [Branch & Defect Analysis Matrix]
 * ==================================
 * 1. decorate(): null list -> IllegalArgumentException, empty list vs non-empty list with duplicates
 * 2. Constructor: null set -> IllegalArgumentException
 * 3. add(Object): branch based on size change (violates List contract), returns false if duplicate
 * 4. add(int, Object): condition set.contains(object) == false
 * 5. addAll(Collection) and addAll(int, Collection): loops with iterator, size comparison
 * 6. set(int, Object): three-way branch depending on pos (indexOf result) vs index:
 *    - pos == -1 -> normal set
 *    - pos == index -> no duplicate removal
 *    - else -> remove duplicate and old element
 * 7. remove(Object), remove(int), removeAll, retainAll, clear: delegate and sync set
 * 8. contains/containsAll: uses set only
 * 9. iterator/listIterator inner classes: next(), previous(), remove(), add() with set sync
 * 10. subList(): returns new SetUniqueList sharing same set
 * 
 * Covered boundary/edge cases:
 * - null arguments (list, set, elements)
 * - empty collections
 * - single element collections
 * - duplicate handling in addAll, set
 * - index boundaries (-1, 0, size, size-1)
 * - iterator removal after next/previous
 * - subList mutation effects on parent
 * 
 * Known defect: addAll(int, Collection) uses add(index, object) for each element
 * but index is never incremented, causing incorrect positioning. 
 * Target: testIntCollectionAddAll() reveals this bug.
 */
public class SetUniqueListDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====
    
    @Test(timeout = 4000)
    public void testDecorateWithEmptyList() {
        List<String> list = new ArrayList<String>();
        SetUniqueList sul = SetUniqueList.decorate(list);
        assertTrue(sul.isEmpty());
        assertEquals(0, sul.size());
        assertTrue(sul.asSet().isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testDecorateWithNonEmptyNoDuplicates() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("B");
        SetUniqueList sul = SetUniqueList.decorate(list);
        assertEquals(2, sul.size());
        assertEquals("A", sul.get(0));
        assertEquals("B", sul.get(1));
    }
    
    @Test(timeout = 4000)
    public void testDecorateRemovesDuplicates() {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add("B");
        list.add("A");  // duplicate
        list.add("C");
        SetUniqueList sul = SetUniqueList.decorate(list);
        assertEquals(3, sul.size());
        assertEquals("A", sul.get(0));
        assertEquals("B", sul.get(1));
        assertEquals("C", sul.get(2));
        // First occurrence of A kept at index 0
    }
    
    @Test(timeout = 4000)
    public void testAddUniqueElement() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        assertTrue(sul.add("A"));
        assertEquals(1, sul.size());
        assertTrue(sul.add("B"));
        assertEquals(2, sul.size());
    }
    
    @Test(timeout = 4000)
    public void testAddDuplicateElementReturnsFalse() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        assertTrue(sul.add("A"));
        assertFalse(sul.add("A"));  // duplicate, should return false
        assertEquals(1, sul.size());
    }
    
    @Test(timeout = 4000)
    public void testAddAtIndexUnique() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add(0, "A");
        assertEquals(1, sul.size());
        assertEquals("A", sul.get(0));
    }
    
    @Test(timeout = 4000)
    public void testAddAtIndexDuplicate() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        sul.add(1, "A");  // duplicate, should not be added
        assertEquals(2, sul.size());
        assertEquals("A", sul.get(0));  // unchanged order
        assertEquals("B", sul.get(1));
    }
    
    @Test(timeout = 4000)
    public void testAddAllEndUnique() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        Collection<String> coll = Arrays.asList("A", "B", "C");
        assertTrue(sul.addAll(coll));
        assertEquals(3, sul.size());
        assertEquals("A", sul.get(0));
        assertEquals("B", sul.get(1));
        assertEquals("C", sul.get(2));
    }
    
    @Test(timeout = 4000)
    public void testAddAllEndWithDuplicatesInCollection() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        Collection<String> coll = Arrays.asList("A", "A", "B", "C");
        assertTrue(sul.addAll(coll));
        assertEquals(3, sul.size());
        assertEquals("A", sul.get(0));  // first A kept
        assertEquals("B", sul.get(1));
        assertEquals("C", sul.get(2));
    }

    @Test(timeout = 4000)
    public void testAddAllEndWithExistingElements() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        Collection<String> coll = Arrays.asList("A", "B");
        assertTrue(sul.addAll(coll));  // only B added
        assertEquals(2, sul.size());
        assertEquals("A", sul.get(0));
        assertEquals("B", sul.get(1));
    }
    
    @Test(timeout = 4000)
    public void testSetNormalUniqueObject() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        Object old = sul.set(0, "C");  // C not in list
        assertEquals("A", old);
        assertEquals("C", sul.get(0));
        assertEquals("B", sul.get(1));
        assertTrue(sul.contains("C"));
        assertFalse(sul.contains("A"));
        assertEquals(2, sul.size());
    }
    
    @Test(timeout = 4000)
    public void testSetObjectAlreadyAtSameIndex() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        Object old = sul.set(0, "A");  // same object at same index
        assertEquals("A", old);
        assertEquals(2, sul.size());
        assertEquals("A", sul.get(0));
        assertEquals("B", sul.get(1));
    }
    
    @Test(timeout = 4000)
    public void testSetObjectAlreadyInListDifferentIndex() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        sul.add("C");
        // set index 0 to "B" (already at index 1)
        Object old = sul.set(0, "B");
        assertEquals("A", old);  // old value at index 0 is "A"
        assertEquals("B", sul.get(0));  // B moved to index 0
        assertEquals("C", sul.get(1));  // C shifted left
        assertEquals(2, sul.size());    // duplicate B removed
        assertFalse(sul.contains("A"));
    }
    
    @Test(timeout = 4000)
    public void testRemoveObject() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        assertTrue(sul.remove("A"));
        assertEquals(1, sul.size());
        assertFalse(sul.contains("A"));
        assertEquals("B", sul.get(0));
    }
    
    @Test(timeout = 4000)
    public void testRemoveNonExistentObject() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        assertFalse(sul.remove("B"));
        assertEquals(1, sul.size());
    }
    
    @Test(timeout = 4000)
    public void testRemoveByIndex() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        Object removed = sul.remove(1);
        assertEquals("B", removed);
        assertEquals(1, sul.size());
        assertFalse(sul.contains("B"));
        assertEquals("A", sul.get(0));
    }
    
    @Test(timeout = 4000)
    public void testRemoveAll() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        sul.add("C");
        Collection<String> coll = Arrays.asList("A", "C");
        assertTrue(sul.removeAll(coll));
        assertEquals(1, sul.size());
        assertEquals("B", sul.get(0));
    }
    
    @Test(timeout = 4000)
    public void testRetainAll() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        sul.add("C");
        Collection<String> coll = Arrays.asList("A", "C");
        assertTrue(sul.retainAll(coll));
        assertEquals(2, sul.size());
        assertEquals("A", sul.get(0));
        assertEquals("C", sul.get(1));
    }
    
    @Test(timeout = 4000)
    public void testClear() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        sul.clear();
        assertTrue(sul.isEmpty());
        assertEquals(0, sul.size());
        assertFalse(sul.contains("A"));
    }
    
    @Test(timeout = 4000)
    public void testContains() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        assertTrue(sul.contains("A"));
        assertFalse(sul.contains("B"));
    }
    
    @Test(timeout = 4000)
    public void testContainsAll() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        assertTrue(sul.containsAll(Arrays.asList("A", "B")));
        assertFalse(sul.containsAll(Arrays.asList("A", "C")));
    }
    
    @Test(timeout = 4000)
    public void testAsSetView() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        Set<String> setView = sul.asSet();
        assertTrue(setView.contains("A"));
        assertTrue(setView.contains("B"));
        assertEquals(2, setView.size());
        // Should be unmodifiable
        try {
            setView.add("C");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testIterator() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        Iterator<String> it = sul.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        it.remove();  // should remove A from both list and set
        assertEquals(1, sul.size());
        assertFalse(sul.contains("A"));
        assertTrue(sul.contains("B"));
    }
    
    @Test(timeout = 4000)
    public void testListIterator() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        ListIterator<String> lit = sul.listIterator();
        assertTrue(lit.hasNext());
        assertEquals("A", lit.next());
        assertTrue(lit.hasPrevious());
        assertEquals("A", lit.previous());
    }
    
    @Test(timeout = 4000)
    public void testListIteratorAtIndex() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        sul.add("C");
        ListIterator<String> lit = sul.listIterator(1);
        assertEquals("B", lit.next());
    }
    
    @Test(timeout = 4000)
    public void testListIteratorAdd() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        ListIterator<String> lit = sul.listIterator();
        lit.add("B");  // should add B
        assertEquals(2, sul.size());
        assertEquals("B", sul.get(0));  // inserted at current position (before A)
        assertEquals("A", sul.get(1));
    }
    
    @Test(timeout = 4000)
    public void testListIteratorAddDuplicate() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        ListIterator<String> lit = sul.listIterator();
        lit.next();  // position at A
        lit.add("A");  // duplicate, should not be added
        assertEquals(2, sul.size());
        assertEquals("A", sul.get(0));
        assertEquals("B", sul.get(1));
    }
    
    @Test(timeout = 4000)
    public void testListIteratorSetUnsupported() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        ListIterator<String> lit = sul.listIterator();
        lit.next();
        try {
            lit.set("B");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testSubList() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        sul.add("C");
        List<String> sub = sul.subList(0, 2);
        assertEquals(2, sub.size());
        assertEquals("A", sub.get(0));
        assertEquals("B", sub.get(1));
    }
    
    @Test(timeout = 4000)
    public void testSubListSharedSet() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        sul.add("C");
        List<String> sub = sul.subList(0, 2);
        // Modifications to subList affect parent and vice versa via shared set
        sub.add("D");  // D not in list, should be added
        assertEquals(4, sul.size());
        assertTrue(sul.contains("D"));
    }
    
    @Test(timeout = 4000)
    public void testSubListAddDuplicateViaSet() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        sul.add("C");
        List<String> sub = sul.subList(0, 2);
        sub.add("A");  // duplicate globally, should not be added
        assertEquals(3, sul.size());
    }

    // ===== Partition B: Boundary Value Analysis (BVA) & Extremes =====
    
    @Test(timeout = 4000)
    public void testDecorateWithNullList() {
        try {
            SetUniqueList.decorate(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("List must not be null", e.getMessage());
        }
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithNullSet() {
        try {
            new SetUniqueList(new ArrayList(), null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Set must not be null", e.getMessage());
        }
    }
    
    @Test(timeout = 4000)
    public void testAddNullElement() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        assertTrue(sul.add(null));
        assertEquals(1, sul.size());
        assertTrue(sul.contains(null));
    }
    
    @Test(timeout = 4000)
    public void testAddDuplicateNullElement() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add(null);
        assertFalse(sul.add(null));
        assertEquals(1, sul.size());
    }
    
    @Test(timeout = 4000)
    public void testAddAllEmptyCollection() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        assertFalse(sul.addAll(new ArrayList<String>()));
        assertTrue(sul.isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testContainsNull() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add(null);
        assertTrue(sul.contains(null));
        assertFalse(sul.contains("A"));
    }
    
    @Test(timeout = 4000)
    public void testRemoveNull() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add(null);
        assertTrue(sul.remove(null));
        assertTrue(sul.isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testLargeNumberOfElements() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<Integer>());
        for (int i = 0; i < 100; i++) {
            assertTrue(sul.add(Integer.valueOf(i)));
        }
        assertEquals(100, sul.size());
        // Add duplicates
        for (int i = 0; i < 100; i++) {
            assertFalse(sul.add(Integer.valueOf(i)));
        }
        assertEquals(100, sul.size());
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    
    /**
     * Targets the known defect from Defects4J:
     * testIntCollectionAddAll fails because addAll(int, Collection) does not 
     * update the index after each insertion, causing subsequent elements to 
     * overwrite positions instead of shifting.
     * Expected behavior: First new element at index 0, second at index 1, etc.
     * Buggy behavior: All added elements end up at index 0, overwriting each other.
     */
    @Test(timeout = 4000)
    public void testIntCollectionAddAll() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<Integer>());
        // Add initial list with elements that will be replaced/moved
        sul.add(Integer.valueOf(1));
        
        // Create collection of new integers to add at beginning
        Collection<Integer> coll = new ArrayList<Integer>();
        coll.add(Integer.valueOf(2));
        coll.add(Integer.valueOf(3));
        
        // AddAll at index 0 (before element 1)
        // Expected result: [2, 3, 1]
        // Buggy result (due to missing index increment): [3, 1] or [2, 1]
        sul.addAll(0, coll);
        
        assertEquals(3, sul.size());
        // The first new element (2) should be at index 0
        assertEquals("First new element should be at index 0", 
                     Integer.valueOf(2), sul.get(0));
        // The second new element (3) should be at index 1
        assertEquals("Second new element should be at index 1", 
                     Integer.valueOf(3), sul.get(1));
        // The original element (1) should be at the end
        assertEquals("Original element should be at index 2", 
                     Integer.valueOf(1), sul.get(2));
    }
    
    @Test(timeout = 4000)
    public void testAddAllMiddleWithExistingDuplicatesInColl() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<Integer>());
        sul.add(Integer.valueOf(1));
        sul.add(Integer.valueOf(4));
        
        Collection<Integer> coll = new ArrayList<Integer>();
        coll.add(Integer.valueOf(2));
        coll.add(Integer.valueOf(3));
        coll.add(Integer.valueOf(2));  // duplicate in collection
        
        sul.addAll(1, coll);
        
        assertEquals(4, sul.size());
        assertEquals(Integer.valueOf(1), sul.get(0));
        assertEquals(Integer.valueOf(2), sul.get(1));
        assertEquals(Integer.valueOf(3), sul.get(2));
        assertEquals(Integer.valueOf(4), sul.get(3));
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====
    
    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testAddAtIndexNegative() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add(-1, "A");
    }
    
    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testAddAtIndexTooLarge() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add(1, "A");
    }
    
    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testSetNegativeIndex() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.set(-1, "B");
    }
    
    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testSetIndexTooLarge() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.set(2, "B");
    }
    
    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetNegativeIndex() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.get(-1);
    }
    
    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetIndexTooLarge() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.get(1);
    }
    
    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testRemoveByIndexNegative() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.remove(-1);
    }
    
    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testRemoveByIndexTooLarge() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.remove(1);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    
    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        SetUniqueList sul1 = SetUniqueList.decorate(new ArrayList<String>());
        sul1.add("A");
        sul1.add("B");
        
        SetUniqueList sul2 = SetUniqueList.decorate(new ArrayList<String>());
        sul2.add("A");
        sul2.add("B");
        
        assertEquals(sul1, sul2);
        assertEquals(sul1.hashCode(), sul2.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testToString() {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        String str = sul.toString();
        assertTrue(str.contains("A"));
        assertTrue(str.contains("B"));
    }
    
    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        SetUniqueList sul = SetUniqueList.decorate(new ArrayList<String>());
        sul.add("A");
        sul.add("B");
        
        // Simulate serialization
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(sul);
        oos.close();
        
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        SetUniqueList deserialized = (SetUniqueList) ois.readObject();
        ois.close();
        
        assertEquals(sul.size(), deserialized.size());
        assertEquals(sul.get(0), deserialized.get(0));
        assertEquals(sul.get(1), deserialized.get(1));
        assertEquals(sul.asSet(), deserialized.asSet());
    }
}