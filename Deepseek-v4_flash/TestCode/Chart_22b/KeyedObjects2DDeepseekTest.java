package org.jfree.data;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: KeyedObjects2D - a 2D keyed data structure with row/column keys.
 * 
 * Decision branches identified:
 * 1. getObject(int,int): rowData != null, columnKey != null, index >= 0
 * 2. getObject(Comparable,Comparable): rowKey null check, columnKey null check,
 *    row index < 0, column index < 0, row >= 0 (always true after checks)
 * 3. setObject: rowKey null check, columnKey null check, rowIndex >= 0 (existing row)
 *    vs. new row creation, columnIndex < 0 (new column)
 * 4. removeObject: allNull detection loop, row removal condition
 * 5. removeColumn(Comparable): index < 0 (unknown key exception)
 * 6. equals: obj == this, obj instanceof, rowKeys equality, columnKeys equality,
 *    rowCount equality, colCount equality, per-cell value equality (null handling)
 * 7. clone: deep copy of rows, rowKeys, columnKeys
 * 
 * Boundary conditions:
 * - Empty table (0 rows, 0 columns)
 * - Single row/column
 * - Multiple rows/columns
 * - Null row/column keys (IllegalArgumentException)
 * - Unknown row/column keys (UnknownKeyException)
 * - Removing last object in a row (row removal)
 * - Removing column by key/index
 * - Removing row by key/index
 * - getObject with valid/invalid indices
 * - equals with null, different type, same object, equal/different content
 * 
 * Known defect: removeColumn(Comparable) fails with UnknownKeyException for
 * valid keys (C1, C2) after certain operations. Also removeRow(Comparable)
 * can throw ArrayIndexOutOfBoundsException for unknown keys. The removeObject
 * method fails to remove columns when all values in a column become null.
 * 
 * Test partitions:
 * A: Core functional logic (setObject, getObject, addObject)
 * B: Boundary values (empty, single, multiple, null keys)
 * C: Defect-targeted (removeColumn, removeRow, removeObject)
 * D: Exception paths (null keys, unknown keys, invalid indices)
 * E: Object lifecycle (equals, hashCode, clone, serialization)
 */
public class KeyedObjects2DDeepseekTest {

    /* ========== Partition A: Core Functional Logic & State Transitions ========== */

    @Test(timeout = 4000)
    public void testInitialState() {
        KeyedObjects2D data = new KeyedObjects2D();
        assertEquals(0, data.getRowCount());
        assertEquals(0, data.getColumnCount());
        assertTrue(data.getRowKeys().isEmpty());
        assertTrue(data.getColumnKeys().isEmpty());
    }

    @Test(timeout = 4000)
    public void testSetObjectNewRowAndColumn() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "R1", "C1");
        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("Value1", data.getObject(0, 0));
        assertEquals("Value1", data.getObject("R1", "C1"));
        assertEquals("R1", data.getRowKey(0));
        assertEquals("C1", data.getColumnKey(0));
        assertEquals(0, data.getRowIndex("R1"));
        assertEquals(0, data.getColumnIndex("C1"));
    }

    @Test(timeout = 4000)
    public void testSetObjectExistingRowNewColumn() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R1", "C2");
        assertEquals(1, data.getRowCount());
        assertEquals(2, data.getColumnCount());
        assertEquals("V1", data.getObject("R1", "C1"));
        assertEquals("V2", data.getObject("R1", "C2"));
    }

    @Test(timeout = 4000)
    public void testSetObjectNewRowExistingColumn() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R2", "C1");
        assertEquals(2, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("V1", data.getObject("R1", "C1"));
        assertEquals("V2", data.getObject("R2", "C1"));
    }

    @Test(timeout = 4000)
    public void testSetObjectUpdateExistingValue() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R1", "C1");
        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("V2", data.getObject("R1", "C1"));
    }

    @Test(timeout = 4000)
    public void testAddObject() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("Value", "R1", "C1");
        assertEquals("Value", data.getObject("R1", "C1"));
    }

    @Test(timeout = 4000)
    public void testGetObjectByIndexValid() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("A", "R1", "C1");
        data.setObject("B", "R1", "C2");
        data.setObject("C", "R2", "C1");
        data.setObject("D", "R2", "C2");
        assertEquals("A", data.getObject(0, 0));
        assertEquals("B", data.getObject(0, 1));
        assertEquals("C", data.getObject(1, 0));
        assertEquals("D", data.getObject(1, 1));
    }

    @Test(timeout = 4000)
    public void testGetObjectByIndexNullValue() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject(null, "R1", "C1");
        assertNull(data.getObject(0, 0));
    }

    @Test(timeout = 4000)
    public void testGetObjectByKeyValid() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("X", "R1", "C1");
        data.setObject("Y", "R2", "C2");
        assertEquals("X", data.getObject("R1", "C1"));
        assertEquals("Y", data.getObject("R2", "C2"));
    }

    @Test(timeout = 4000)
    public void testGetObjectByKeyNullValue() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject(null, "R1", "C1");
        assertNull(data.getObject("R1", "C1"));
    }

    @Test(timeout = 4000)
    public void testGetRowKeysUnmodifiable() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V", "R1", "C1");
        try {
            data.getRowKeys().add("R2");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetColumnKeysUnmodifiable() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V", "R1", "C1");
        try {
            data.getColumnKeys().add("C2");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    /* ========== Partition B: Boundary Value Analysis & Extremes ========== */

    @Test(timeout = 4000)
    public void testEmptyTableGetRowCount() {
        KeyedObjects2D data = new KeyedObjects2D();
        assertEquals(0, data.getRowCount());
    }

    @Test(timeout = 4000)
    public void testEmptyTableGetColumnCount() {
        KeyedObjects2D data = new KeyedObjects2D();
        assertEquals(0, data.getColumnCount());
    }

    @Test(timeout = 4000)
    public void testSingleRowSingleColumn() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Only", "R1", "C1");
        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("Only", data.getObject(0, 0));
    }

    @Test(timeout = 4000)
    public void testManyRowsAndColumns() {
        KeyedObjects2D data = new KeyedObjects2D();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                data.setObject("V" + i + "-" + j, "R" + i, "C" + j);
            }
        }
        assertEquals(10, data.getRowCount());
        assertEquals(10, data.getColumnCount());
        assertEquals("V5-5", data.getObject("R5", "C5"));
    }

    @Test(timeout = 4000)
    public void testGetRowIndexNonExistent() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V", "R1", "C1");
        assertEquals(-1, data.getRowIndex("R2"));
    }

    @Test(timeout = 4000)
    public void testGetColumnIndexNonExistent() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V", "R1", "C1");
        assertEquals(-1, data.getColumnIndex("C2"));
    }

    @Test(timeout = 4000)
    public void testGetRowKeyValid() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V", "R1", "C1");
        assertEquals("R1", data.getRowKey(0));
    }

    @Test(timeout = 4000)
    public void testGetColumnKeyValid() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V", "R1", "C1");
        assertEquals("C1", data.getColumnKey(0));
    }

    @Test(timeout = 4000)
    public void testGetObjectByIndexOutOfBounds() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V", "R1", "C1");
        try {
            data.getObject(1, 0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    /* ========== Partition C: Defect-Targeted Branch Zone ========== */

    @Test(timeout = 4000)
    public void testRemoveColumnByKeyDefect() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R1", "C2");
        data.setObject("V3", "R2", "C1");
        data.setObject("V4", "R2", "C2");
        
        // This should work but fails in defective version
        data.removeColumn("C2");
        
        assertEquals(2, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("V1", data.getObject("R1", "C1"));
        assertEquals("V3", data.getObject("R2", "C1"));
        assertNull(data.getObject("R1", "C2"));
        assertNull(data.getObject("R2", "C2"));
    }

    @Test(timeout = 4000)
    public void testRemoveColumnByIndexDefect() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R1", "C2");
        data.setObject("V3", "R2", "C1");
        data.setObject("V4", "R2", "C2");
        
        // This should work but fails in defective version
        data.removeColumn(1);
        
        assertEquals(2, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("V1", data.getObject("R1", "C1"));
        assertEquals("V3", data.getObject("R2", "C1"));
    }

    @Test(timeout = 4000)
    public void testRemoveRowByKeyDefect() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R2", "C1");
        
        // This should work but fails in defective version
        data.removeRow("R2");
        
        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("V1", data.getObject("R1", "C1"));
    }

    @Test(timeout = 4000)
    public void testRemoveObjectDefect() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R1", "C2");
        data.setObject("V3", "R2", "C1");
        data.setObject("V4", "R2", "C2");
        
        // Remove one value, column C2 should still exist because R2/C2 remains
        data.removeObject("R1", "C2");
        
        assertEquals(2, data.getRowCount());
        assertEquals(2, data.getColumnCount());
        assertNull(data.getObject("R1", "C2"));
        assertEquals("V4", data.getObject("R2", "C2"));
    }

    @Test(timeout = 4000)
    public void testRemoveObjectRemovesRowWhenAllNull() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R1", "C2");
        
        data.removeObject("R1", "C1");
        data.removeObject("R1", "C2");
        
        assertEquals(0, data.getRowCount());
        assertEquals(2, data.getColumnCount()); // columns remain
    }

    @Test(timeout = 4000)
    public void testGetValueByKeyAfterColumnRemoval() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R1", "C2");
        data.setObject("V3", "R2", "C1");
        data.setObject("V4", "R2", "C2");
        
        data.removeColumn("C1");
        
        // This should work but fails in defective version
        assertEquals("V2", data.getObject("R1", "C2"));
        assertEquals("V4", data.getObject("R2", "C2"));
    }

    @Test(timeout = 4000)
    public void testSetObjectAfterColumnRemoval() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R1", "C2");
        
        data.removeColumn("C1");
        
        // This should work but fails in defective version
        data.setObject("V3", "R1", "C2");
        
        assertEquals("V3", data.getObject("R1", "C2"));
    }

    /* ========== Partition D: Exception & Defensive Guard Paths ========== */

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetObjectNullRowKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V", null, "C1");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetObjectNullColumnKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V", "R1", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddObjectNullRowKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("V", null, "C1");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddObjectNullColumnKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("V", "R1", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetObjectByKeyNullRowKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.getObject(null, "C1");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetObjectByKeyNullColumnKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.getObject("R1", null);
    }

    @Test(expected = UnknownKeyException.class, timeout = 4000)
    public void testGetObjectByKeyUnknownRow() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V", "R1", "C1");
        data.getObject("R2", "C1");
    }

    @Test(expected = UnknownKeyException.class, timeout = 4000)
    public void testGetObjectByKeyUnknownColumn() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V", "R1", "C1");
        data.getObject("R1", "C2");
    }

    @Test(expected = UnknownKeyException.class, timeout = 4000)
    public void testRemoveColumnUnknownKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V", "R1", "C1");
        data.removeColumn("C2");
    }

    @Test(timeout = 4000)
    public void testRemoveRowByIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R2", "C1");
        data.removeRow(1);
        assertEquals(1, data.getRowCount());
        assertEquals("V1", data.getObject("R1", "C1"));
    }

    @Test(timeout = 4000)
    public void testRemoveRowByKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R2", "C1");
        data.removeRow("R1");
        assertEquals(1, data.getRowCount());
        assertEquals("V2", data.getObject("R2", "C1"));
    }

    @Test(timeout = 4000)
    public void testRemoveColumnByIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R1", "C2");
        data.removeColumn(0);
        assertEquals(1, data.getColumnCount());
        assertEquals("V2", data.getObject("R1", "C2"));
    }

    /* ========== Partition E: Object Lifecycle & Contract Integrity ========== */

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V", "R1", "C1");
        assertTrue(data.equals(data));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        KeyedObjects2D data = new KeyedObjects2D();
        assertFalse(data.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        KeyedObjects2D data = new KeyedObjects2D();
        assertFalse(data.equals("String"));
    }

    @Test(timeout = 4000)
    public void testEqualsEmptyTables() {
        KeyedObjects2D data1 = new KeyedObjects2D();
        KeyedObjects2D data2 = new KeyedObjects2D();
        assertTrue(data1.equals(data2));
    }

    @Test(timeout = 4000)
    public void testEqualsSameContent() {
        KeyedObjects2D data1 = new KeyedObjects2D();
        KeyedObjects2D data2 = new KeyedObjects2D();
        data1.setObject("V1", "R1", "C1");
        data1.setObject("V2", "R2", "C2");
        data2.setObject("V1", "R1", "C1");
        data2.setObject("V2", "R2", "C2");
        assertTrue(data1.equals(data2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentValues() {
        KeyedObjects2D data1 = new KeyedObjects2D();
        KeyedObjects2D data2 = new KeyedObjects2D();
        data1.setObject("V1", "R1", "C1");
        data2.setObject("V2", "R1", "C1");
        assertFalse(data1.equals(data2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentRowKeys() {
        KeyedObjects2D data1 = new KeyedObjects2D();
        KeyedObjects2D data2 = new KeyedObjects2D();
        data1.setObject("V", "R1", "C1");
        data2.setObject("V", "R2", "C1");
        assertFalse(data1.equals(data2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentColumnKeys() {
        KeyedObjects2D data1 = new KeyedObjects2D();
        KeyedObjects2D data2 = new KeyedObjects2D();
        data1.setObject("V", "R1", "C1");
        data2.setObject("V", "R1", "C2");
        assertFalse(data1.equals(data2));
    }

    @Test(timeout = 4000)
    public void testEqualsNullValues() {
        KeyedObjects2D data1 = new KeyedObjects2D();
        KeyedObjects2D data2 = new KeyedObjects2D();
        data1.setObject(null, "R1", "C1");
        data2.setObject(null, "R1", "C1");
        assertTrue(data1.equals(data2));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        KeyedObjects2D data1 = new KeyedObjects2D();
        KeyedObjects2D data2 = new KeyedObjects2D();
        data1.setObject("V1", "R1", "C1");
        data1.setObject("V2", "R2", "C2");
        data2.setObject("V1", "R1", "C1");
        data2.setObject("V2", "R2", "C2");
        assertEquals(data1.hashCode(), data2.hashCode());
    }

    @Test(timeout = 4000)
    public void testClone() throws CloneNotSupportedException {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R2", "C2");
        
        KeyedObjects2D clone = (KeyedObjects2D) data.clone();
        
        assertNotSame(data, clone);
        assertEquals(data, clone);
        assertEquals(data.getRowCount(), clone.getRowCount());
        assertEquals(data.getColumnCount(), clone.getColumnCount());
        assertEquals("V1", clone.getObject("R1", "C1"));
        assertEquals("V2", clone.getObject("R2", "C2"));
    }

    @Test(timeout = 4000)
    public void testCloneIndependent() throws CloneNotSupportedException {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        
        KeyedObjects2D clone = (KeyedObjects2D) data.clone();
        clone.setObject("V2", "R1", "C1");
        
        assertEquals("V1", data.getObject("R1", "C1"));
        assertEquals("V2", clone.getObject("R1", "C1"));
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R2", "C2");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(data);
        oos.flush();
        
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        KeyedObjects2D deserialized = (KeyedObjects2D) ois.readObject();
        
        assertEquals(data, deserialized);
        assertEquals("V1", deserialized.getObject("R1", "C1"));
        assertEquals("V2", deserialized.getObject("R2", "C2"));
    }

    @Test(timeout = 4000)
    public void testRemoveObjectNullValue() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R1", "C2");
        data.setObject("V3", "R2", "C1");
        data.setObject("V4", "R2", "C2");
        
        data.removeObject("R1", "C1");
        
        assertNull(data.getObject("R1", "C1"));
        assertEquals("V2", data.getObject("R1", "C2"));
        assertEquals("V3", data.getObject("R2", "C1"));
        assertEquals("V4", data.getObject("R2", "C2"));
    }

    @Test(timeout = 4000)
    public void testRemoveObjectLastInRow() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R2", "C1");
        
        data.removeObject("R1", "C1");
        
        assertEquals(1, data.getRowCount());
        assertEquals("V2", data.getObject("R2", "C1"));
    }

    @Test(timeout = 4000)
    public void testRemoveColumnByKeyWithMultipleRows() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R1", "C2");
        data.setObject("V3", "R2", "C1");
        data.setObject("V4", "R2", "C2");
        data.setObject("V5", "R3", "C1");
        data.setObject("V6", "R3", "C2");
        
        data.removeColumn("C1");
        
        assertEquals(3, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("V2", data.getObject("R1", "C2"));
        assertEquals("V4", data.getObject("R2", "C2"));
        assertEquals("V6", data.getObject("R3", "C2"));
    }

    @Test(timeout = 4000)
    public void testRemoveColumnByIndexWithMultipleRows() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R1", "C2");
        data.setObject("V3", "R2", "C1");
        data.setObject("V4", "R2", "C2");
        data.setObject("V5", "R3", "C1");
        data.setObject("V6", "R3", "C2");
        
        data.removeColumn(0);
        
        assertEquals(3, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("V2", data.getObject("R1", "C2"));
        assertEquals("V4", data.getObject("R2", "C2"));
        assertEquals("V6", data.getObject("R3", "C2"));
    }

    @Test(timeout = 4000)
    public void testRemoveRowByKeyWithMultipleColumns() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R1", "C2");
        data.setObject("V3", "R2", "C1");
        data.setObject("V4", "R2", "C2");
        
        data.removeRow("R1");
        
        assertEquals(1, data.getRowCount());
        assertEquals(2, data.getColumnCount());
        assertEquals("V3", data.getObject("R2", "C1"));
        assertEquals("V4", data.getObject("R2", "C2"));
    }

    @Test(timeout = 4000)
    public void testRemoveRowByIndexWithMultipleColumns() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R1", "C2");
        data.setObject("V3", "R2", "C1");
        data.setObject("V4", "R2", "C2");
        
        data.removeRow(0);
        
        assertEquals(1, data.getRowCount());
        assertEquals(2, data.getColumnCount());
        assertEquals("V3", data.getObject("R2", "C1"));
        assertEquals("V4", data.getObject("R2", "C2"));
    }
}