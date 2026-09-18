package org.jfree.data;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jfree.chart.util.SortOrder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: org.jfree.data.DefaultKeyedValues
 * Defects4J Defect Focus:
 *   - Bug: In removeValue(int index), rebuildIndex() is guarded by `if (index < this.keys.size())`.
 *          When removing the last element (or the only element), `index == this.keys.size()`,
 *          causing rebuildIndex() to be skipped. Consequently:
 *          1) this.indexMap retains the deleted key and stale index mappings.
 *          2) getItemCount() (which queries indexMap.size()) returns an incorrect stale count.
 *          3) getIndex(removedKey) returns the stale index instead of -1.
 *          4) Subsequent operations fail or cause IndexOutOfBoundsException (e.g., in getValue(index)).
 *
 * Decision / Condition Matrix Covered:
 *   - getItemCount(): 0, 1, n items, and after removals.
 *   - getValue(int): valid boundaries (0, size-1), invalid boundaries (< 0, >= size).
 *   - getKey(int): valid boundaries (0, size-1), invalid boundaries (< 0, >= size).
 *   - getIndex(Comparable): null key (IAE), key present, key absent (-1).
 *   - getKeys(): empty dataset, populated dataset, immutability of returned list clone.
 *   - getValue(Comparable): null key (IAE), unknown key (UnknownKeyException), valid key with value/null.
 *   - addValue / setValue(Comparable, double/Number): new key, overwrite existing key, null key (IAE),
 *     null value.
 *   - insertValue(int, Comparable, double/Number):
 *       * position < 0 or > getItemCount() (IAE)
 *       * key == null (IAE)
 *       * pos == position (in-place replacement)
 *       * pos >= 0 && pos != position (existing key moved)
 *       * pos < 0 (inserting new key at index 0, intermediate, or end)
 *   - removeValue(int): index 0, intermediate index, last index (triggers rebuild defect).
 *   - removeValue(Comparable): null key (IAE), absent key (no-op), present key (triggers rebuild defect).
 *   - clear(): clears keys, values, and indexMap.
 *   - sortByKeys(SortOrder): ASCENDING, DESCENDING, empty list, single item, multiple items.
 *   - sortByValues(SortOrder): ASCENDING, DESCENDING, with null values (nulls placed at end).
 *   - equals(Object): identity, non-KeyedValues, differing sizes, differing keys, null vs non-null value,
 *     differing values, exact match.
 *   - hashCode(): non-null keys, empty dataset.
 *   - clone(): deep copy verification (keys, values, indexMap independently modified).
 *   - Serialization: round-trip fidelity and state restoration.
 * ----------------------------------------------------------------------------------------------------
 */
public class DefaultKeyedValuesGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Regression Targets)
    // =========================================================================

    /**
     * Targets the defect where removing the last item in removeValue(int) skips
     * rebuildIndex() because index == this.keys.size().
     */
    @Test(timeout = 4000)
    public void testRemoveValueLastItemIndexMapSync() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);

        // Remove the last item (index 1)
        data.removeValue(1);

        assertEquals("Item count must be 1 after removing one of two items", 1, data.getItemCount());
        assertEquals("Removed key 'B' should no longer be indexed", -1, data.getIndex("B"));
        assertEquals("Key at index 0 must be 'A'", "A", data.getKey(0));
    }

    /**
     * Targets the defect where removing the single remaining item leaves indexMap stale.
     */
    @Test(timeout = 4000)
    public void testRemoveOnlyItemIndexMapSync() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("OnlyKey", 100.0);

        data.removeValue("OnlyKey");

        assertEquals("Dataset should be empty after removing the only element", 0, data.getItemCount());
        assertEquals("getIndex for removed key must return -1", -1, data.getIndex("OnlyKey"));
    }

    /**
     * Targets getIndex returning stale index after removing item by key.
     */
    @Test(timeout = 4000)
    public void testGetIndexAfterRemovingKey() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("K1", 10);
        data.addValue("K2", 20);
        data.addValue("K3", 30);

        data.removeValue("K3");

        assertEquals(-1, data.getIndex("K3"));
        assertEquals(2, data.getItemCount());

        // Also test removal of first and middle items
        data.removeValue("K1");
        assertEquals(-1, data.getIndex("K1"));
        assertEquals(0, data.getIndex("K2"));
        assertEquals(1, data.getItemCount());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddAndRetrieveValues() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("K1", 10.5);
        data.addValue("K2", (Number) null);
        data.addValue("K3", 30);

        assertEquals(3, data.getItemCount());
        assertEquals(Double.valueOf(10.5), data.getValue(0));
        assertNull(data.getValue(1));
        assertEquals(30, data.getValue(2));

        assertEquals("K1", data.getKey(0));
        assertEquals("K2", data.getKey(1));
        assertEquals("K3", data.getKey(2));

        assertEquals(0, data.getIndex("K1"));
        assertEquals(1, data.getIndex("K2"));
        assertEquals(2, data.getIndex("K3"));

        assertEquals(Double.valueOf(10.5), data.getValue("K1"));
        assertNull(data.getValue("K2"));
        assertEquals(30, data.getValue("K3"));
    }

    @Test(timeout = 4000)
    public void testSetValueOverwritesExisting() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.setValue("A", 1.0);
        assertEquals(1, data.getItemCount());
        assertEquals(1.0, data.getValue("A").doubleValue(), 1e-9);

        // Overwrite with double
        data.setValue("A", 2.0);
        assertEquals(1, data.getItemCount());
        assertEquals(2.0, data.getValue("A").doubleValue(), 1e-9);

        // Overwrite with Number
        data.setValue("A", (Number) null);
        assertEquals(1, data.getItemCount());
        assertNull(data.getValue("A"));
    }

    @Test(timeout = 4000)
    public void testInsertValueAtBeginningMiddleAndEnd() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.insertValue(0, "B", 2.0); // insert into empty
        assertEquals(1, data.getItemCount());
        assertEquals("B", data.getKey(0));

        data.insertValue(0, "A", 1.0); // insert at head
        assertEquals(2, data.getItemCount());
        assertEquals("A", data.getKey(0));
        assertEquals("B", data.getKey(1));

        data.insertValue(2, "D", 4.0); // insert at tail
        assertEquals(3, data.getItemCount());
        assertEquals("D", data.getKey(2));

        data.insertValue(2, "C", 3.0); // insert in middle
        assertEquals(4, data.getItemCount());
        assertEquals("A", data.getKey(0));
        assertEquals("B", data.getKey(1));
        assertEquals("C", data.getKey(2));
        assertEquals("D", data.getKey(3));

        assertEquals(0, data.getIndex("A"));
        assertEquals(1, data.getIndex("B"));
        assertEquals(2, data.getIndex("C"));
        assertEquals(3, data.getIndex("D"));
    }

    @Test(timeout = 4000)
    public void testInsertValueExistingKeySamePosition() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);

        // Replace B at position 1 with new value
        data.insertValue(1, "B", 20.0);
        assertEquals(2, data.getItemCount());
        assertEquals("B", data.getKey(1));
        assertEquals(20.0, data.getValue(1).doubleValue(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testInsertValueExistingKeyDifferentPosition() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);
        data.addValue("C", 3.0);

        // Move C from pos 2 to pos 0
        data.insertValue(0, "C", (Number) 30.0);
        assertEquals(3, data.getItemCount());
        assertEquals("C", data.getKey(0));
        assertEquals("A", data.getKey(1));
        assertEquals("B", data.getKey(2));
        assertEquals(30.0, data.getValue(0).doubleValue(), 1e-9);

        assertEquals(0, data.getIndex("C"));
        assertEquals(1, data.getIndex("A"));
        assertEquals(2, data.getIndex("B"));
    }

    @Test(timeout = 4000)
    public void testClear() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("K1", 1.0);
        data.addValue("K2", 2.0);
        assertEquals(2, data.getItemCount());

        data.clear();
        assertEquals(0, data.getItemCount());
        assertEquals(-1, data.getIndex("K1"));
        assertEquals(-1, data.getIndex("K2"));
        assertTrue(data.getKeys().isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetKeysReturnsDefensiveCopy() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("K1", 10);
        List keys = data.getKeys();
        assertEquals(1, keys.size());

        // Modifying returned list must not affect internal state
        keys.clear();
        assertEquals(1, data.getItemCount());
        assertEquals("K1", data.getKey(0));
    }

    @Test(timeout = 4000)
    public void testSortByKeysAscendingAndDescending() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("C", 3.0);
        data.addValue("A", 1.0);
        data.addValue("B", 2.0);

        data.sortByKeys(SortOrder.ASCENDING);
        assertEquals("A", data.getKey(0));
        assertEquals("B", data.getKey(1));
        assertEquals("C", data.getKey(2));
        assertEquals(1.0, data.getValue(0).doubleValue(), 1e-9);
        assertEquals(2.0, data.getValue(1).doubleValue(), 1e-9);
        assertEquals(3.0, data.getValue(2).doubleValue(), 1e-9);

        data.sortByKeys(SortOrder.DESCENDING);
        assertEquals("C", data.getKey(0));
        assertEquals("B", data.getKey(1));
        assertEquals("A", data.getKey(2));
    }

    @Test(timeout = 4000)
    public void testSortByValuesAscendingDescendingAndWithNulls() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("V_NULL", (Number) null);
        data.addValue("V_30", 30.0);
        data.addValue("V_10", 10.0);
        data.addValue("V_20", 20.0);

        // In JFreeChart, null values always sort to the end regardless of order
        data.sortByValues(SortOrder.ASCENDING);
        assertEquals("V_10", data.getKey(0));
        assertEquals("V_20", data.getKey(1));
        assertEquals("V_30", data.getKey(2));
        assertEquals("V_NULL", data.getKey(3));
        assertNull(data.getValue(3));

        data.sortByValues(SortOrder.DESCENDING);
        assertEquals("V_30", data.getKey(0));
        assertEquals("V_20", data.getKey(1));
        assertEquals("V_10", data.getKey(2));
        assertEquals("V_NULL", data.getKey(3));
        assertNull(data.getValue(3));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyCollectionBoundaries() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        assertEquals(0, data.getItemCount());
        assertEquals(-1, data.getIndex("NonExistent"));
        assertNotNull(data.getKeys());
        assertTrue(data.getKeys().isEmpty());
    }

    @Test(timeout = 4000)
    public void testRemoveNonExistentKeyIsNoOp() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("K1", 10.0);
        data.removeValue("K_MISSING");
        assertEquals(1, data.getItemCount());
        assertEquals(0, data.getIndex("K1"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetIndexNullKeyThrowsIAE() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.getIndex(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetValueNullKeyThrowsIAE() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.getValue((Comparable) null);
    }

    @Test(expected = UnknownKeyException.class, timeout = 4000)
    public void testGetValueMissingKeyThrowsUnknownKeyException() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("Present", 1.0);
        data.getValue("Absent");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValueNegativeIndexThrowsIOOB() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValueIndexTooLargeThrowsIOOB() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("A", 1.0);
        data.getValue(1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetKeyNegativeIndexThrowsIOOB() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.getKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetKeyIndexTooLargeThrowsIOOB() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.getKey(0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetValueNullKeyThrowsIAE() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.setValue(null, 1.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetValueNumberNullKeyThrowsIAE() {
        DefaultKeyedValues data = new DefaultKeyedValues();
        data.setValue(null, (Number) 1.0);
    }

    @Test(expected =