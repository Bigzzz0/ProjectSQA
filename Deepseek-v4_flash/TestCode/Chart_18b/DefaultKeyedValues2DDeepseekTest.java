package org.jfree.data;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: DefaultKeyedValues2D
 * 
 * Partitions:
 * A. Core Functional Logic & State Transitions
 *    - addValue/setValue/getValue (by index and by key)
 *    - getRowCount, getColumnCount, getRowKey, getColumnKey
 *    - getRowIndex, getColumnIndex, getRowKeys, getColumnKeys
 *    - removeValue, removeRow, removeColumn, clear
 * 
 * B. Boundary Value Analysis & Extremes
 *    - null keys (rowKey, columnKey)
 *    - empty data structure
 *    - out-of-range indices (negative, >= size)
 *    - duplicate keys, missing keys
 * 
 * C. Defect-Targeted Branch Zone
 *    - Bug 1835955: removeColumn(Comparable) throws IndexOutOfBoundsException
 *      when column key is not present in all rows.
 *    - Bug in removeValue: row/column removal logic after setting null.
 *    - Bug in getIndex (indirectly via DefaultKeyedValues): getIndex returning 0
 *      for non-existent key (affects getValue by key? but guarded by columnKeys check)
 * 
 * D. Exception & Defensive Guard Paths
 *    - IllegalArgumentException for null keys in getRowIndex, getColumnIndex,
 *      getValue(Comparable, Comparable), setValue, removeValue, removeRow(Comparable),
 *      removeColumn(Comparable)
 *    - UnknownKeyException for unrecognised row/column keys in getValue(Comparable, Comparable)
 *    - IndexOutOfBoundsException for invalid indices in getValue(int,int), getRowKey, getColumnKey
 * 
 * E. Object Lifecycle & Contract Integrity
 *    - equals, hashCode, clone
 * 
 * Known Defects (from Defects4J):
 * 1. removeColumn(Comparable) throws IndexOutOfBoundsException when column key
 *    is not present in some rows (bug 1835955).
 * 2. removeValue may incorrectly remove rows/columns or throw exceptions.
 * 3. getIndex in DefaultKeyedValues may return 0 for non-existent key (affects
 *    internal logic but not directly exposed here).
 * 
 * Tests are designed to reveal these defects on the buggy version.
 */
public class DefaultKeyedValues2DDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testAddValueAndGetValueByIndex() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R1", "C2");
        d.addValue(3.0, "R2", "C1");
        assertEquals(2, d.getRowCount());
        assertEquals(2, d.getColumnCount());
        assertEquals(1.0, d.getValue(0, 0));
        assertEquals(2.0, d.getValue(0, 1));
        assertEquals(3.0, d.getValue(1, 0));
        assertNull(d.getValue(1, 1)); // no value for R2,C2
    }

    @Test(timeout = 4000)
    public void testSetValueUpdatesExisting() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.setValue(10.0, "R1", "C1");
        d.setValue(20.0, "R1", "C1");
        assertEquals(20.0, d.getValue(0, 0));
    }

    @Test(timeout = 4000)
    public void testGetValueByKey() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(5.0, "Row", "Col");
        assertEquals(5.0, d.getValue("Row", "Col"));
    }

    @Test(timeout = 4000)
    public void testGetRowKeyAndColumnKey() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "A", "X");
        d.addValue(2.0, "B", "Y");
        assertEquals("A", d.getRowKey(0));
        assertEquals("B", d.getRowKey(1));
        assertEquals("X", d.getColumnKey(0));
        assertEquals("Y", d.getColumnKey(1));
    }

    @Test(timeout = 4000)
    public void testGetRowIndexAndColumnIndex() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R2", "C2");
        assertEquals(0, d.getRowIndex("R1"));
        assertEquals(1, d.getRowIndex("R2"));
        assertEquals(0, d.getColumnIndex("C1"));
        assertEquals(1, d.getColumnIndex("C2"));
    }

    @Test(timeout = 4000)
    public void testGetRowKeysAndColumnKeys() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R2", "C2");
        assertTrue(d.getRowKeys().contains("R1"));
        assertTrue(d.getRowKeys().contains("R2"));
        assertTrue(d.getColumnKeys().contains("C1"));
        assertTrue(d.getColumnKeys().contains("C2"));
    }

    @Test(timeout = 4000)
    public void testRemoveValue() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R1", "C2");
        d.removeValue("R1", "C1");
        assertNull(d.getValue("R1", "C1"));
        assertEquals(2.0, d.getValue("R1", "C2"));
        // row still has one non-null value, so row should remain
        assertEquals(1, d.getRowCount());
        assertEquals(2, d.getColumnCount());
    }

    @Test(timeout = 4000)
    public void testRemoveValueRemovesRowWhenAllNull() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.removeValue("R1", "C1");
        assertEquals(0, d.getRowCount());
        assertEquals(0, d.getColumnCount());
    }

    @Test(timeout = 4000)
    public void testRemoveValueRemovesColumnWhenAllNull() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R2", "C1");
        d.removeValue("R1", "C1");
        // column C1 still has value in R2, so column remains
        assertEquals(2, d.getRowCount());
        assertEquals(1, d.getColumnCount());
        d.removeValue("R2", "C1");
        // now column should be removed
        assertEquals(0, d.getRowCount());
        assertEquals(0, d.getColumnCount());
    }

    @Test(timeout = 4000)
    public void testRemoveRowByIndex() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R2", "C1");
        d.removeRow(0);
        assertEquals(1, d.getRowCount());
        assertEquals("R2", d.getRowKey(0));
    }

    @Test(timeout = 4000)
    public void testRemoveRowByKey() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R2", "C1");
        d.removeRow("R1");
        assertEquals(1, d.getRowCount());
        assertEquals("R2", d.getRowKey(0));
    }

    @Test(timeout = 4000)
    public void testRemoveColumnByIndex() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R1", "C2");
        d.removeColumn(0);
        assertEquals(1, d.getColumnCount());
        assertEquals("C2", d.getColumnKey(0));
    }

    @Test(timeout = 4000)
    public void testClear() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.clear();
        assertEquals(0, d.getRowCount());
        assertEquals(0, d.getColumnCount());
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetRowIndexNullKey() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.getRowIndex(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetColumnIndexNullKey() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.getColumnIndex(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetValueByKeyNullRowKey() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.getValue(null, "C1");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetValueByKeyNullColumnKey() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.getValue("R1", null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetValueNullRowKey() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.setValue(1.0, null, "C1");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetValueNullColumnKey() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.setValue(1.0, "R1", null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRemoveValueNullRowKey() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.removeValue(null, "C1");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRemoveValueNullColumnKey() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.removeValue("R1", null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRemoveRowByKeyNull() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.removeRow((Comparable) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRemoveColumnByKeyNull() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.removeColumn((Comparable) null);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetValueNegativeRow() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.getValue(-1, 0);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetValueNegativeColumn() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.getValue(0, -1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetRowKeyNegative() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.getRowKey(-1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetColumnKeyNegative() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.getColumnKey(-1);
    }

    @Test(timeout = 4000, expected = UnknownKeyException.class)
    public void testGetValueByKeyUnknownRow() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.getValue("R2", "C1");
    }

    @Test(timeout = 4000, expected = UnknownKeyException.class)
    public void testGetValueByKeyUnknownColumn() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.getValue("R1", "C2");
    }

    @Test(timeout = 4000)
    public void testGetValueByKeyReturnsNullForMissingColumnInRow() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R2", "C2");
        // R1 has no value for C2, but C2 is a defined column
        assertNull(d.getValue("R1", "C2"));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Targets bug 1835955: removeColumn(Comparable) throws
     * IndexOutOfBoundsException when the column key is not present in all rows.
     * This test adds two rows with different columns, then removes one column.
     * The buggy version will throw IndexOutOfBoundsException.
     */
    @Test(timeout = 4000)
    public void testRemoveColumnByKeyWhenColumnNotInAllRows() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R2", "C2"); // R2 does not have C1
        // This should not throw any exception
        d.removeColumn("C1");
        assertEquals(1, d.getColumnCount());
        assertEquals("C2", d.getColumnKey(0));
        assertEquals(2, d.getRowCount());
        // R1 should have no values now (C1 removed)
        assertNull(d.getValue("R1", "C1")); // C1 no longer exists
    }

    /**
     * Another variant: remove a column that exists only in some rows,
     * with multiple rows and columns.
     */
    @Test(timeout = 4000)
    public void testRemoveColumnByKeyMultipleRows() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R1", "C2");
        d.addValue(3.0, "R2", "C2");
        d.addValue(4.0, "R3", "C1");
        d.addValue(5.0, "R3", "C3");
        // Remove C1 - R2 does not have C1
        d.removeColumn("C1");
        assertEquals(2, d.getColumnCount());
        assertTrue(d.getColumnKeys().contains("C2"));
        assertTrue(d.getColumnKeys().contains("C3"));
        assertEquals(3, d.getRowCount());
    }

    /**
     * Test removeColumn(Comparable) when column key does not exist at all.
     * The buggy version might throw IndexOutOfBoundsException or
     * UnknownKeyException? Actually the method does not check existence,
     * so it will iterate over rows and call removeValue on each.
     * If the column key is not in any row, rowData.removeValue(columnKey)
     * may throw if it tries to remove a non-existent key.
     * The correct behavior should be to do nothing or throw UnknownKeyException.
     * The current implementation does not throw, so we expect no exception.
     */
    @Test(timeout = 4000)
    public void testRemoveColumnByKeyNonExistent() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        // Removing a column that was never added
        d.removeColumn("C2");
        // Should have no effect
        assertEquals(1, d.getColumnCount());
        assertEquals("C1", d.getColumnKey(0));
    }

    /**
     * Test removeValue when the row becomes empty and column also becomes empty.
     * This exercises the row/column removal logic.
     */
    @Test(timeout = 4000)
    public void testRemoveValueRemovesBothRowAndColumn() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.removeValue("R1", "C1");
        assertEquals(0, d.getRowCount());
        assertEquals(0, d.getColumnCount());
    }

    /**
     * Test removeValue when only the row becomes empty but column still has values.
     */
    @Test(timeout = 4000)
    public void testRemoveValueRemovesRowOnly() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R2", "C1");
        d.removeValue("R1", "C1");
        assertEquals(1, d.getRowCount());
        assertEquals("R2", d.getRowKey(0));
        assertEquals(1, d.getColumnCount());
        assertEquals("C1", d.getColumnKey(0));
    }

    /**
     * Test removeValue when only the column becomes empty but row still has values.
     */
    @Test(timeout = 4000)
    public void testRemoveValueRemovesColumnOnly() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R1", "C2");
        d.removeValue("R1", "C1");
        assertEquals(1, d.getRowCount());
        assertEquals("R1", d.getRowKey(0));
        assertEquals(1, d.getColumnCount());
        assertEquals("C2", d.getColumnKey(0));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testRemoveRowByIndexOutOfBounds() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.removeRow(0);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testRemoveColumnByIndexOutOfBounds() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.removeColumn(0);
    }

    @Test(timeout = 4000, expected = UnknownKeyException.class)
    public void testRemoveRowByKeyUnknown() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.removeRow("NonExistent");
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        assertTrue(d.equals(d));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        assertFalse(d.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        assertFalse(d.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsSymmetric() {
        DefaultKeyedValues2D d1 = new DefaultKeyedValues2D();
        DefaultKeyedValues2D d2 = new DefaultKeyedValues2D();
        d1.addValue(1.0, "R1", "C1");
        d2.addValue(1.0, "R1", "C1");
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentRowKeys() {
        DefaultKeyedValues2D d1 = new DefaultKeyedValues2D();
        DefaultKeyedValues2D d2 = new DefaultKeyedValues2D();
        d1.addValue(1.0, "R1", "C1");
        d2.addValue(1.0, "R2", "C1");
        assertFalse(d1.equals(d2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentColumnKeys() {
        DefaultKeyedValues2D d1 = new DefaultKeyedValues2D();
        DefaultKeyedValues2D d2 = new DefaultKeyedValues2D();
        d1.addValue(1.0, "R1", "C1");
        d2.addValue(1.0, "R1", "C2");
        assertFalse(d1.equals(d2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentValues() {
        DefaultKeyedValues2D d1 = new DefaultKeyedValues2D();
        DefaultKeyedValues2D d2 = new DefaultKeyedValues2D();
        d1.addValue(1.0, "R1", "C1");
        d2.addValue(2.0, "R1", "C1");
        assertFalse(d1.equals(d2));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        int hash1 = d.hashCode();
        int hash2 = d.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testClone() throws CloneNotSupportedException {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        DefaultKeyedValues2D clone = (DefaultKeyedValues2D) d.clone();
        assertNotSame(d, clone);
        assertEquals(d, clone);
        // Modify original, clone should be unaffected
        d.addValue(2.0, "R1", "C2");
        assertFalse(d.equals(clone));
    }

    @Test(timeout = 4000)
    public void testCloneDeepCopy() throws CloneNotSupportedException {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D();
        d.addValue(1.0, "R1", "C1");
        DefaultKeyedValues2D clone = (DefaultKeyedValues2D) d.clone();
        // Modify original's row data
        d.setValue(99.0, "R1", "C1");
        assertEquals(1.0, clone.getValue("R1", "C1"));
    }

    @Test(timeout = 4000)
    public void testSortRowKeys() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D(true);
        d.addValue(1.0, "Z", "C1");
        d.addValue(2.0, "A", "C1");
        assertEquals("A", d.getRowKey(0));
        assertEquals("Z", d.getRowKey(1));
    }

    @Test(timeout = 4000)
    public void testGetRowIndexWithSort() {
        DefaultKeyedValues2D d = new DefaultKeyedValues2D(true);
        d.addValue(1.0, "B", "C1");
        d.addValue(2.0, "A", "C1");
        assertEquals(0, d.getRowIndex("A"));
        assertEquals(1, d.getRowIndex("B"));
        // Non-existent key should return negative insertion point
        int idx = d.getRowIndex("C");
        assertTrue(idx < 0);
    }
}