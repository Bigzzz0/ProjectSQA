package org.apache.commons.collections.list;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Branches & Boundary Conditions:
 * - SetUniqueList decoration (empty list, non-empty list with duplicates)
 * - add(Object): sizeBefore vs size() comparison, when element already in set
 * - add(int, Object): set.contains(object) == false path, index at 0, size(), middle
 * - addAll(Collection): iterates and uses add(index, object) with index increment logic
 * - addAll(int, Collection): index increment after successful insertion, duplicate handling
 * - set(int, Object): pos == -1, pos == index, pos != index (swap/remove logic), removed tracking
 * - remove(Object): super.remove(object) then set.remove(object), when object not present
 * - remove(int): super.remove(index) then set.remove(result)
 * - removeAll(Collection): super.removeAll(coll) then set.removeAll(coll)
 * - retainAll(Collection): super.retainAll(coll) then set.retainAll(coll)
 * - clear(): both list and set cleared
 * - contains(Object): delegated to set.contains(object)
 * - containsAll(Collection): delegated to set.containsAll(coll)
 * - iterator().remove(): set.remove(last) after super.remove()
 * - listIterator().add(): when set.contains(object) == false, add to both
 * - subList(): returns new SetUniqueList wrapping subList and same set
 * - asSet(): returns unmodifiable set view
 * - Boundary: null list in decorate(), null set in constructor
 * - Boundary: empty collection arguments for addAll, removeAll, retainAll
 * - Boundary: index out of bounds for add(int, Object), remove(int), set(int, Object)
 * - Defect Target (testCollections304): set() causing duplicate or incorrect element count
 *   after swap-remove logic; expected 3 but got 4 indicates improper removal
 */
public class SetUniqueListDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========
    
    @Test(timeout = 4000)
    public void testAddMethodReturnsTrueWhenElementAdded() {
        List<String> baseList = new ArrayList<String>();
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        assertTrue("Should return true when adding new element", uniqueList.add("A"));
        assertEquals(1, uniqueList.size());
        assertTrue(uniqueList.contains("A"));
    }
    
    @Test(timeout = 4000)
    public void testAddMethodReturnsFalseWhenDuplicateAdded() {
        List<String> baseList = new ArrayList<String>();
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        uniqueList.add("A");
        assertFalse("Should return false when adding duplicate", uniqueList.add("A"));
        assertEquals(1, uniqueList.size());
    }
    
    @Test(timeout = 4000)
    public void testAddAtIndexWhenElementUnique() {
        List<String> baseList = new ArrayList<String>();
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        uniqueList.add(0, "B");
        assertEquals(1, uniqueList.size());
        assertEquals("B", uniqueList.get(0));
    }
    
    @Test(timeout = 4000)
    public void testAddAtIndexWhenElementDuplicateDoesNotModify() {
        List<String> baseList = new ArrayList<String>();
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        uniqueList.add("A");
        uniqueList.add(0, "A"); // duplicate, should not be added
        assertEquals(1, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
    }
    
    @Test(timeout = 4000)
    public void testAddAllWithNoDuplicates() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        Collection<String> coll = Arrays.asList("C", "D");
        assertTrue("Should return true when collection changed", uniqueList.addAll(coll));
        assertEquals(4, uniqueList.size());
        assertTrue(uniqueList.contains("C"));
        assertTrue(uniqueList.contains("D"));
    }
    
    @Test(timeout = 4000)
    public void testAddAllWithAllDuplicates() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        Collection<String> coll = Arrays.asList("A", "B");
        assertFalse("Should return false when no change", uniqueList.addAll(coll));
        assertEquals(2, uniqueList.size());
    }
    
    @Test(timeout = 4000)
    public void testAddAllAtIndexWithMixedDuplicates() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "C"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        Collection<String> coll = Arrays.asList("A", "B", "C");
        assertTrue("Should return true when collection changes", uniqueList.addAll(1, coll));
        assertEquals(3, uniqueList.size()); // A (existing), B (newly added), C (existing, but not duplicate in insert)
        // Expected: ultimate order depends on logic: 
        // Starting: [A, C]; insert at index 1: 
        //   A already exists -> skip, size unchanged, index stays 1
        //   B not exists -> add at 1 -> [A, B, C], size becomes 3, index inc to 2
        //   C already exists -> skip, size unchanged
        assertEquals("A", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
        assertEquals("C", uniqueList.get(2));
    }
    
    @Test(timeout = 4000)
    public void testAddAllAtIndexEmptyCollection() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        Collection<String> emptyColl = new ArrayList<String>();
        assertFalse("Should return false for empty collection", uniqueList.addAll(0, emptyColl));
        assertEquals(1, uniqueList.size());
    }
    
    @Test(timeout = 4000)
    public void testSetMethodNormalNoDuplicate() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B", "C"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        String old = (String) uniqueList.set(1, "D");
        assertEquals("B", old);
        assertEquals(3, uniqueList.size());
        assertEquals("D", uniqueList.get(1));
        assertTrue(uniqueList.contains("D"));
        assertFalse(uniqueList.contains("B"));
    }
    
    @Test(timeout = 4000)
    public void testSetMethodWithExistingElementAtDifferentIndex() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B", "C", "D"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        // set index 0 to "C" which already exists at index 2
        String old = (String) uniqueList.set(0, "C");
        assertEquals("A", old);
        assertEquals(3, uniqueList.size()); // since duplicate removal occurred
        // Expected: after set, list becomes [C, B, D] (A removed, C moved? Actually:
        // set(0, "C"): position of "C" is 2, index=0, pos != index
        // super.set(0, "C") -> list becomes [C, B, C, D]
        // removed (old) = "A"; pos=2, index=0 => super.remove(2) -> remove C at index 2 => list [C, B, D]
        // set.remove("A") removes A from set
        assertEquals("C", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
        assertEquals("D", uniqueList.get(2));
        // Check set contains correct elements
        assertTrue(uniqueList.contains("C"));
        assertTrue(uniqueList.contains("B"));
        assertTrue(uniqueList.contains("D"));
        assertFalse(uniqueList.contains("A"));
    }
    
    @Test(timeout = 4000)
    public void testSetMethodWithSameIndex() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B", "C"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        // set "B" at index 1, which is its current position
        String old = (String) uniqueList.set(1, "B");
        assertEquals("B", old);
        assertEquals(3, uniqueList.size());
        assertEquals("B", uniqueList.get(1));
    }
    
    @Test(timeout = 4000)
    public void testRemoveObjectRemovesFromSet() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B", "C"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        assertTrue("Should return true when object removed", uniqueList.remove("B"));
        assertEquals(2, uniqueList.size());
        assertFalse(uniqueList.contains("B"));
    }
    
    @Test(timeout = 4000)
    public void testRemoveObjectNotPresent() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        assertFalse("Should return false when object not present", uniqueList.remove("C"));
        assertEquals(2, uniqueList.size());
    }
    
    @Test(timeout = 4000)
    public void testRemoveIntReturnsCorrectElement() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("X", "Y", "Z"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        String removed = (String) uniqueList.remove(1);
        assertEquals("Y", removed);
        assertEquals(2, uniqueList.size());
        assertFalse(uniqueList.contains("Y"));
        assertEquals("X", uniqueList.get(0));
        assertEquals("Z", uniqueList.get(1));
    }
    
    @Test(timeout = 4000)
    public void testRemoveAllRemovesFromSet() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B", "C", "D"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        Collection<String> coll = Arrays.asList("A", "C");
        assertTrue(uniqueList.removeAll(coll));
        assertEquals(2, uniqueList.size());
        assertTrue(uniqueList.contains("B"));
        assertTrue(uniqueList.contains("D"));
        assertFalse(uniqueList.contains("A"));
        assertFalse(uniqueList.contains("C"));
    }
    
    @Test(timeout = 4000)
    public void testRetainAll() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B", "C", "D"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        Collection<String> coll = Arrays.asList("A", "C");
        assertTrue(uniqueList.retainAll(coll));
        assertEquals(2, uniqueList.size());
        assertTrue(uniqueList.contains("A"));
        assertTrue(uniqueList.contains("C"));
        assertFalse(uniqueList.contains("B"));
        assertFalse(uniqueList.contains("D"));
    }
    
    @Test(timeout = 4000)
    public void testClear() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B", "C"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        uniqueList.clear();
        assertEquals(0, uniqueList.size());
        assertFalse(uniqueList.contains("A"));
    }
    
    @Test(timeout = 4000)
    public void testContainsDelegatesToSet() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        assertTrue(uniqueList.contains("A"));
        assertFalse(uniqueList.contains("C"));
    }
    
    @Test(timeout = 4000)
    public void testContainsAllDelegatesToSet() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B", "C"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        assertTrue(uniqueList.containsAll(Arrays.asList("A", "C")));
        assertFalse(uniqueList.containsAll(Arrays.asList("A", "D")));
    }
    
    @Test(timeout = 4000)
    public void testAsSetReturnsUnmodifiable() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        Set<String> setView = uniqueList.asSet();
        assertNotNull(setView);
        assertTrue(setView.contains("A"));
        assertTrue(setView.contains("B"));
        try {
            setView.add("C");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testIteratorAndRemove() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B", "C"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        java.util.Iterator<String> it = uniqueList.iterator();
        assertTrue(it.hasNext());
        String first = it.next();
        assertEquals("A", first);
        it.remove();
        assertEquals(2, uniqueList.size());
        assertFalse(uniqueList.contains("A"));
        // ensure set and list consistent
        assertTrue(uniqueList.contains("B"));
        assertTrue(uniqueList.contains("C"));
    }
    
    @Test(timeout = 4000)
    public void testListIteratorAdd() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "C"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        java.util.ListIterator<String> lit = uniqueList.listIterator();
        lit.next(); // A
        lit.add("B"); // insert B between A and C
        assertEquals(3, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
        assertEquals("C", uniqueList.get(2));
        assertTrue(uniqueList.contains("B"));
    }
    
    @Test(timeout = 4000)
    public void testListIteratorAddDuplicateIgnored() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        java.util.ListIterator<String> lit = uniqueList.listIterator();
        lit.next(); // A
        lit.add("A"); // duplicate, should be ignored
        assertEquals(2, uniqueList.size());
    }
    
    @Test(timeout = 4000)
    public void testListIteratorSetThrows() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        java.util.ListIterator<String> lit = uniqueList.listIterator();
        lit.next();
        try {
            lit.set("C");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testSubListMaintainsUniqueness() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B", "C", "D"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        List<String> sub = uniqueList.subList(1, 3);
        assertEquals(2, sub.size());
        assertEquals("B", sub.get(0));
        assertEquals("C", sub.get(1));
        // SubList is a SetUniqueList that shares the set
        assertFalse(sub.contains("A"));
        assertFalse(sub.contains("D"));
    }
    
    // ========== Partition B: Boundary Value Analysis & Extremes ==========
    
    @Test(timeout = 4000)
    public void testDecorateWithEmptyList() {
        List<String> empty = new ArrayList<String>();
        SetUniqueList uniqueList = SetUniqueList.decorate(empty);
        assertNotNull(uniqueList);
        assertEquals(0, uniqueList.size());
    }
    
    @Test(timeout = 4000)
    public void testDecorateWithDuplicateElements() {
        List<String> listWithDups = new ArrayList<String>(Arrays.asList("A", "B", "A", "C", "B"));
        SetUniqueList uniqueList = SetUniqueList.decorate(listWithDups);
        assertEquals(3, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
        assertEquals("B", uniqueList.get(1));
        assertEquals("C", uniqueList.get(2));
    }
    
    @Test(timeout = 4000)
    public void testDecorateWithNullListThrows() {
        try {
            SetUniqueList.decorate(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithNullSetThrows() {
        try {
            List<String> list = new ArrayList<String>();
            new SetUniqueList(list, null) {};
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testAddAllWithNullCollection() {
        List<String> baseList = new ArrayList<String>();
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        try {
            uniqueList.addAll((Collection) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testAddAtIndexWithIndexZero() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        uniqueList.add(0, "C");
        assertEquals(3, uniqueList.size());
        assertEquals("C", uniqueList.get(0));
    }
    
    @Test(timeout = 4000)
    public void testAddAtIndexOutOfBounds() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        try {
            uniqueList.add(5, "B");
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testRemoveIntIndexOutOfBounds() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        try {
            uniqueList.remove(5);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testSetIntObjectIndexOutOfBounds() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        try {
            uniqueList.set(5, "B");
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testLargeNumberOfElements() {
        List<Integer> baseList = new ArrayList<Integer>();
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        for (int i = 0; i < 1000; i++) {
            assertTrue(uniqueList.add(Integer.valueOf(i)));
        }
        assertEquals(1000, uniqueList.size());
        // Add half duplicates
        for (int i = 0; i < 500; i++) {
            assertFalse(uniqueList.add(Integer.valueOf(i)));
        }
        assertEquals(1000, uniqueList.size());
    }
    
    // ========== Partition C: Defect-Targeted Branch Zone ==========
    
    /**
     * Directly targets the known Defects4J defect (testCollections304):
     * Expected size 3 but got 4 after a series of set operations.
     * This test reproduces a scenario where set() with duplicate removal
     * fails to properly remove an element from the internal set,
     * causing the list to report incorrect size.
     */
    @Test(timeout = 4000)
    public void testCollections304DefectScenario() {
        // This scenario specifically triggers the bug:
        // Create list with [A, B, C]
        List<String> list = new ArrayList<String>(Arrays.asList("A", "B", "C"));
        SetUniqueList uniqueList = SetUniqueList.decorate(list);
        
        // Set index 0 to "B" which exists at index 1
        // Bug: improper handling of removed element from set may cause size inconsistency
        String old = (String) uniqueList.set(0, "B");
        
        // After correct behavior: old = "A", list should become [B, C] (size 2)
        // Or depending on implementation: [B, C] or [B, A, C]? Let's analyze:
        // set(0, "B"): pos of "B" = 1, index=0, pos != index
        // super.set(0, "B") -> list becomes [B, B, C], removed = "A"
        // super.remove(pos=1) -> removes B at index 1 -> list becomes [B, C]
        // set.remove("A") -> removes A from set
        // Final list: [B, C], size = 2
        // But the defect caused incorrect size (expected 3 but was 4)
        // Here we test that size is correct and elements are consistent
        assertEquals("List size should be correct", 2, uniqueList.size());
        assertFalse("A should be removed", uniqueList.contains("A"));
        assertTrue("B should be present", uniqueList.contains("B"));
        assertTrue("C should be present", uniqueList.contains("C"));
    }
    
    @Test(timeout = 4000)
    public void testSetMethodCascadingRemovalDefect() {
        // More complex case that exercises set() with multiple occurrences
        List<String> list = new ArrayList<String>(Arrays.asList("X", "Y", "X", "Z"));
        SetUniqueList uniqueList = SetUniqueList.decorate(list);
        // After decoration: [X, Y, Z] (duplicates removed)
        assertEquals(3, uniqueList.size());
        
        // Attempt to set index 2 to "Y" which is at index 1
        String old = (String) uniqueList.set(2, "Y");
        // Expected behavior:
        // pos of Y = 1, index = 2, remove at pos 1 -> list becomes [X, Z] but also remove X? Wait:
        // super.set(2, "Y") -> list: [X, Y, Y], old = "Z"
        // super.remove(1) -> remove Y at index 1 -> [X, Y]
        // set.remove("Z") -> remove Z from set
        // Final: [X, Y], size = 2
        assertTrue("Y should be present", uniqueList.contains("Y"));
        assertTrue("X should be present", uniqueList.contains("X"));
        assertFalse("Z should be removed", uniqueList.contains("Z"));
        assertEquals("Size should be 2 after operation", 2, uniqueList.size());
    }
    
    // ========== Partition D: Exception & Defensive Guard Paths ==========
    
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testListIteratorIndexOutOfBounds() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        uniqueList.listIterator(5);
    }
    
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testSubListInvalidIndices() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        uniqueList.subList(2, 1);
    }
    
    @Test(timeout = 4000)
    public void testListIteratorRemoveConsistency() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B", "C"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        java.util.ListIterator<String> lit = uniqueList.listIterator();
        lit.next(); // A
        lit.next(); // B
        lit.remove(); // remove B
        assertEquals(2, uniqueList.size());
        assertFalse(uniqueList.contains("B"));
        assertTrue(uniqueList.contains("A"));
        assertTrue(uniqueList.contains("C"));
    }
    
    @Test(timeout = 4000)
    public void testListIteratorPrevious() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B", "C"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        java.util.ListIterator<String> lit = uniqueList.listIterator();
        lit.next(); // A
        lit.next(); // B
        Object prev = lit.previous(); // back to B? Actually previous returns B then cursor before B
        assertEquals("B", prev);
        // Now previous again returns A
        prev = lit.previous();
        assertEquals("A", prev);
    }
    
    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    
    @Test(timeout = 4000)
    public void testDecoratePreservesInitialOrder() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("C", "A", "B", "A", "C"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        assertEquals(3, uniqueList.size());
        // First occurrence preserved: C, A, B
        assertEquals("C", uniqueList.get(0));
        assertEquals("A", uniqueList.get(1));
        assertEquals("B", uniqueList.get(2));
    }
    
    @Test(timeout = 4000)
    public void testSetAndAddInteraction() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("1", "2", "3"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        uniqueList.set(2, "4");
        assertEquals(3, uniqueList.size());
        uniqueList.add("5");
        assertEquals(4, uniqueList.size());
        assertFalse(uniqueList.contains("3"));
        assertTrue(uniqueList.contains("4"));
        assertTrue(uniqueList.contains("5"));
    }
    
    @Test(timeout = 4000)
    public void testRetainAllEmptyCollection() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B", "C"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        Collection<String> empty = new ArrayList<String>();
        assertTrue(uniqueList.retainAll(empty));
        assertEquals(0, uniqueList.size());
    }
    
    @Test(timeout = 4000)
    public void testRemoveAllEmptyCollection() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", "B"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        Collection<String> empty = new ArrayList<String>();
        assertFalse(uniqueList.removeAll(empty));
        assertEquals(2, uniqueList.size());
    }
    
    @Test(timeout = 4000)
    public void testSerializationContract() {
        // Validate that the class has serialVersionUID
        // This is a compile-time check; we can verify the field exists
        try {
            java.lang.reflect.Field serialField = SetUniqueList.class.getDeclaredField("serialVersionUID");
            assertNotNull("serialVersionUID field must exist", serialField);
            assertEquals(java.lang.reflect.Modifier.PRIVATE, serialField.getModifiers() & java.lang.reflect.Modifier.PRIVATE);
            assertEquals(java.lang.reflect.Modifier.STATIC, serialField.getModifiers() & java.lang.reflect.Modifier.STATIC);
        } catch (NoSuchFieldException e) {
            fail("serialVersionUID field missing");
        }
    }
    
    @Test(timeout = 4000)
    public void testSetWithNullElement() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("A", null, "B"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        // After decoration, null is present
        assertTrue(uniqueList.contains(null));
        assertEquals(3, uniqueList.size());
        // Set null at index 2 to "C"
        String old = (String) uniqueList.set(2, "C");
        assertEquals("B", old);
        assertEquals(3, uniqueList.size());
    }
    
    @Test(timeout = 4000)
    public void testListIteratorRemoveThenAdd() {
        List<String> baseList = new ArrayList<String>(Arrays.asList("X", "Y", "Z"));
        SetUniqueList uniqueList = SetUniqueList.decorate(baseList);
        java.util.ListIterator<String> lit = uniqueList.listIterator();
        lit.next(); // X
        lit.remove(); // remove X
        lit.add("W"); // add W
        // Now list should be [W, Y, Z] (since X removed, W added at position 0)
        assertEquals(3, uniqueList.size());
        assertEquals("W", uniqueList.get(0));
        assertEquals("Y", uniqueList.get(1));
        assertEquals("Z", uniqueList.get(2));
    }
}