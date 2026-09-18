package org.jfree.data;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: DefaultKeyedValues (Defects4J)
 * 
 * Known Defects:
 * 1. testRemoveValue: removeValue(Comparable) fails when removing a key that 
 *    exists at index 0 (or when indexMap is not properly rebuilt after removal).
 *    - Branch: removeValue(Comparable) -> getIndex(key) >= 0 -> removeValue(index)
 *    - Defect: After removing at index 0, indexMap may not be updated correctly 
 *      for remaining keys, causing getIndex() to return stale/incorrect values.
 * 2. testGetIndex2: getIndex() returns 0 for a non-existent key instead of -1.
 *    - Branch: getIndex(key) -> indexMap.get(key) == null -> return -1
 *    - Defect: indexMap may contain stale entries after removeValue() operations, 
 *      causing getIndex() to return an incorrect index for a removed key.
 * 3. testRemoveColumnByKey (in 2D dataset) and testBug1835955: 
 *    IndexOutOfBoundsException when removing from an empty or improperly 
 *    maintained structure.
 * 
 * Test Strategy:
 * - Partition A: Core functional logic (add, set, get, insert, sort).
 * - Partition B: Boundary values (null keys, empty list, index boundaries).
 * - Partition C: Defect-targeted tests for removeValue and getIndex.
 * - Partition D: Exception paths (null key, invalid position).
 * - Partition E: equals, hashCode, clone.
 * 
 * The critical test is testRemoveValueAndGetIndexConsistency() which directly 
 * targets the defect: after removing a key at index 0, getIndex() for a 
 * non-existent key must return -1, and getIndex() for remaining keys must 
 * return correct indices.
 */
public class DefaultKeyedValuesDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testAddAndGetValue() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("A", 1.0);
        dkv.addValue("B", 2.5);
        assertEquals(2, dkv.getItemCount());
        assertEquals(1.0, dkv.getValue(0).doubleValue(), 0.0);
        assertEquals(2.5, dkv.getValue(1).doubleValue(), 0.0);
        assertEquals("A", dkv.getKey(0));
        assertEquals("B", dkv.getKey(1));
    }

    @Test(timeout = 4000)
    public void testSetValueUpdatesExisting() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.setValue("X", 10);
        dkv.setValue("X", 20);
        assertEquals(1, dkv.getItemCount());
        assertEquals(20, dkv.getValue("X").intValue());
    }

    @Test(timeout = 4000)
    public void testInsertValueAtPosition() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("A", 1);
        dkv.addValue("C", 3);
        dkv.insertValue(1, "B", 2);
        assertEquals(3, dkv.getItemCount());
        assertEquals("A", dkv.getKey(0));
        assertEquals("B", dkv.getKey(1));
        assertEquals("C", dkv.getKey(2));
        assertEquals(2, dkv.getValue(1).intValue());
    }

    @Test(timeout = 4000)
    public void testInsertValueMovesExistingKey() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("A", 1);
        dkv.addValue("B", 2);
        dkv.addValue("C", 3);
        dkv.insertValue(0, "C", 30);
        assertEquals(3, dkv.getItemCount());
        assertEquals("C", dkv.getKey(0));
        assertEquals(30, dkv.getValue(0).intValue());
        assertEquals("A", dkv.getKey(1));
        assertEquals("B", dkv.getKey(2));
    }

    @Test(timeout = 4000)
    public void testSortByKeysAscending() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("C", 3);
        dkv.addValue("A", 1);
        dkv.addValue("B", 2);
        dkv.sortByKeys(SortOrder.ASCENDING);
        assertEquals("A", dkv.getKey(0));
        assertEquals("B", dkv.getKey(1));
        assertEquals("C", dkv.getKey(2));
    }

    @Test(timeout = 4000)
    public void testSortByValuesDescending() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("A", 1);
        dkv.addValue("B", 3);
        dkv.addValue("C", 2);
        dkv.sortByValues(SortOrder.DESCENDING);
        assertEquals("B", dkv.getKey(0));
        assertEquals("C", dkv.getKey(1));
        assertEquals("A", dkv.getKey(2));
    }

    @Test(timeout = 4000)
    public void testClear() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("A", 1);
        dkv.addValue("B", 2);
        dkv.clear();
        assertEquals(0, dkv.getItemCount());
        assertEquals(-1, dkv.getIndex("A"));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testEmptyCollection() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        assertEquals(0, dkv.getItemCount());
        assertEquals(-1, dkv.getIndex("anything"));
        assertEquals(0, dkv.getKeys().size());
    }

    @Test(timeout = 4000)
    public void testNullKeyInGetIndexThrows() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        try {
            dkv.getIndex(null);
            fail("Expected IllegalArgumentException for null key");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetValueByKeyNotFound() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("A", 1);
        try {
            dkv.getValue("B");
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testInsertValueOutOfBounds() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("A", 1);
        try {
            dkv.insertValue(2, "B", 2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRemoveValueByIndexBoundary() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("A", 1);
        dkv.removeValue(0);
        assertEquals(0, dkv.getItemCount());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * CRITICAL TEST: Directly targets the defect where removeValue(Comparable)
     * leaves the indexMap in an inconsistent state, causing getIndex() to return
     * 0 for a non-existent key (instead of -1) and incorrect indices for
     * remaining keys.
     */
    @Test(timeout = 4000)
    public void testRemoveValueAndGetIndexConsistency() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("A", 1);
        dkv.addValue("B", 2);
        dkv.addValue("C", 3);

        // Remove key "A" (index 0)
        dkv.removeValue("A");

        // After removal, getIndex for non-existent key must be -1
        assertEquals("getIndex for removed key should be -1", -1, dkv.getIndex("A"));
        // Remaining keys must have correct indices
        assertEquals("Index of B should be 0", 0, dkv.getIndex("B"));
        assertEquals("Index of C should be 1", 1, dkv.getIndex("C"));
    }

    /**
     * Additional defect-targeted test: removeValue(Comparable) when the key
     * is at index 0 and then add a new key to ensure indexMap is rebuilt.
     */
    @Test(timeout = 4000)
    public void testRemoveValueThenAddNewKey() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("A", 1);
        dkv.addValue("B", 2);
        dkv.removeValue("A");
        dkv.addValue("D", 4);
        assertEquals(2, dkv.getItemCount());
        assertEquals("B", dkv.getKey(0));
        assertEquals("D", dkv.getKey(1));
        assertEquals(0, dkv.getIndex("B"));
        assertEquals(1, dkv.getIndex("D"));
        assertEquals(-1, dkv.getIndex("A"));
    }

    /**
     * Test that removeValue(Comparable) works correctly when removing
     * the last element.
     */
    @Test(timeout = 4000)
    public void testRemoveLastValue() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("A", 1);
        dkv.addValue("B", 2);
        dkv.removeValue("B");
        assertEquals(1, dkv.getItemCount());
        assertEquals(0, dkv.getIndex("A"));
        assertEquals(-1, dkv.getIndex("B"));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testRemoveValueByIndexOutOfBounds() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("A", 1);
        try {
            dkv.removeValue(1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRemoveValueByKeyNotFound() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("A", 1);
        try {
            dkv.removeValue("B");
            fail("Expected UnknownKeyException");
        } catch (UnknownKeyException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetValueByIndexOutOfBounds() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("A", 1);
        try {
            dkv.getValue(1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEquals() {
        DefaultKeyedValues dkv1 = new DefaultKeyedValues();
        dkv1.addValue("A", 1);
        dkv1.addValue("B", 2);
        DefaultKeyedValues dkv2 = new DefaultKeyedValues();
        dkv2.addValue("A", 1);
        dkv2.addValue("B", 2);
        assertTrue(dkv1.equals(dkv2));
        assertTrue(dkv2.equals(dkv1));
        assertEquals(dkv1.hashCode(), dkv2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentValues() {
        DefaultKeyedValues dkv1 = new DefaultKeyedValues();
        dkv1.addValue("A", 1);
        DefaultKeyedValues dkv2 = new DefaultKeyedValues();
        dkv2.addValue("A", 2);
        assertFalse(dkv1.equals(dkv2));
    }

    @Test(timeout = 4000)
    public void testClone() throws CloneNotSupportedException {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("A", 1);
        dkv.addValue("B", 2);
        DefaultKeyedValues clone = (DefaultKeyedValues) dkv.clone();
        assertNotSame(dkv, clone);
        assertEquals(dkv, clone);
        // Modify original, clone should be unaffected
        dkv.addValue("C", 3);
        assertFalse(dkv.equals(clone));
        assertEquals(2, clone.getItemCount());
    }

    @Test(timeout = 4000)
    public void testGetKeysReturnsCopy() {
        DefaultKeyedValues dkv = new DefaultKeyedValues();
        dkv.addValue("A", 1);
        List keys = dkv.getKeys();
        keys.add("B"); // Should not affect internal state
        assertEquals(1, dkv.getItemCount());
        assertEquals("A", dkv.getKey(0));
    }
}