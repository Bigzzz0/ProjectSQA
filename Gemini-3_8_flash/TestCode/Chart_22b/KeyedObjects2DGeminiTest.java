package org.jfree.data;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: org.jfree.data.KeyedObjects2D
 *
 * Decision / Branch Points Targeted:
 * 1. Constructor: initial state (empty lists for rowKeys, columnKeys, rows).
 * 2. getRowCount() & getColumnCount(): size queries on empty, single, and multiple entries.
 * 3. getObject(int row, int column):
 *    - rowData == null vs rowData != null
 *    - columnKey == null vs columnKey != null
 *    - rowData.getIndex(columnKey) >= 0 vs < 0
 * 4. getRowKey(int) & getColumnKey(int): valid indices vs out-of-bounds (IndexOutOfBoundsException).
 * 5. getRowIndex(Comparable) & getColumnIndex(Comparable): key found vs key absent vs null key.
 * 6. getRowKeys() & getColumnKeys(): unmodifiable list verification and content checks.
 * 7. getObject(Comparable, Comparable):
 *    - rowKey == null -> IllegalArgumentException
 *    - columnKey == null -> IllegalArgumentException
 *    - rowKey absent -> UnknownKeyException
 *    - columnKey absent -> UnknownKeyException
 *    - rowKey and columnKey both present -> returns exact object (including null)
 * 8. setObject / addObject(Object, Comparable, Comparable):
 *    - null rowKey or null columnKey -> IllegalArgumentException
 *    - rowKey present vs absent (creates new KeyedObjects)
 *    - columnKey present vs absent (adds to columnKeys)
 *    - updating existing cell value
 * 9. removeObject(Comparable, Comparable):
 *    - setting cell to null
 *    - row becoming entirely null -> row removed from rowKeys and rows
 *    - column becoming entirely null -> column removal defect trigger
 * 10. removeRow(int) & removeRow(Comparable):
 *    - valid row removal
 *    - Defect Target: removeRow(Comparable) with unknown key throws IndexOutOfBoundsException
 *      instead of expected UnknownKeyException.
 * 11. removeColumn(int) & removeColumn(Comparable):
 *    - valid column removal across uniform rows
 *    - Defect Target: sparse matrix removeColumn(Comparable) where a row doesn't contain the columnKey
 *      throws UnknownKeyException during row iteration.
 * 12. equals(Object):
 *    - this == obj (reflexive)
 *    - !(obj instanceof KeyedObjects2D)
 *    - rowKeys mismatch, columnKeys mismatch, rowCount mismatch, colCount mismatch
 *    - cell comparison: (v1 == null, v2 != null), (v1 == null, v2 == null), (v1 != null, v1.equals(v2)), (v1 != null, !v1.equals(v2))
 * 13. hashCode(): consistency with equals.
 * 14. clone(): deep copy verification for rows and shallow isolation of key lists.
 * 15. Serialization: serialization and deserialization equivalence.
 * -----------------------------------------------------------------------------------------
 */
public class KeyedObjects2DGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialEmptyState() {
        KeyedObjects2D data = new KeyedObjects2D();
        assertEquals(0, data.getRowCount());
        assertEquals(0, data.getColumnCount());
        assertTrue(data.getRowKeys().isEmpty());
        assertTrue(data.getColumnKeys().isEmpty());
    }

    @Test(timeout = 4000)
    public void testSetAndGetObjectByKeys() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Value1", "R1", "C1");

        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("Value1", data.getObject("R1", "C1"));
    }

    @Test(timeout = 4000)
    public void testAddObjectDelegation() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("ValueA", "RowA", "ColA");

        assertEquals("ValueA", data.getObject("RowA", "ColA"));
        assertEquals("ValueA", data.getObject(0, 0));
    }

    @Test(timeout = 4000)
    public void testUpdateExistingObject() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Initial", "R1", "C1");
        data.setObject("Updated", "R1", "C1");

        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("Updated", data.getObject("R1", "C1"));
    }

    @Test(timeout = 4000)
    public void testGetRowAndColumnKeysAndIndices() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R2", "C2");

        assertEquals(0, data.getRowIndex("R1"));
        assertEquals(1, data.getRowIndex("R2"));
        assertEquals(-1, data.getRowIndex("NonExistentRow"));

        assertEquals(0, data.getColumnIndex("C1"));
        assertEquals(1, data.getColumnIndex("C2"));
        assertEquals(-1, data.getColumnIndex("NonExistentCol"));

        assertEquals("R1", data.getRowKey(0));
        assertEquals("R2", data.getRowKey(1));
        assertEquals("C1", data.getColumnKey(0));
        assertEquals("C2", data.getColumnKey(1));

        List rowKeys = data.getRowKeys();
        List colKeys = data.getColumnKeys();
        assertEquals(2, rowKeys.size());
        assertEquals(2, colKeys.size());
        assertEquals("R1", rowKeys.get(0));
        assertEquals("C2", colKeys.get(1));
    }

    @Test(timeout = 4000)
    public void testGetObjectByIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R1", "C2");
        data.setObject("V3", "R2", "C1");

        assertEquals("V1", data.getObject(0, 0));
        assertEquals("V2", data.getObject(0, 1));
        assertEquals("V3", data.getObject(1, 0));
        assertNull(data.getObject(1, 1)); // R2, C2 was not populated
    }

    @Test(timeout = 4000)
    public void testRemoveRowByIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R2", "C1");

        assertEquals(2, data.getRowCount());
        data.removeRow(0);

        assertEquals(1, data.getRowCount());
        assertEquals("R2", data.getRowKey(0));
        assertEquals("V2", data.getObject(0, 0));
    }

    @Test(timeout = 4000)
    public void testRemoveRowByValidKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.setObject("V2", "R2", "C1");

        data.removeRow("R1");
        assertEquals(1, data.getRowCount());
        assertEquals("R2", data.getRowKey(0));
    }

    @Test(timeout = 4000)
    public void testRemoveColumnByIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V11", "R1", "C1");
        data.setObject("V12", "R1", "C2");

        assertEquals(2, data.getColumnCount());
        data.removeColumn(0);

        assertEquals(1, data.getColumnCount());
        assertEquals("C2", data.getColumnKey(0));
        assertEquals("V12", data.getObject(0, 0));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Defensive Guards
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetObjectNullRowKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.getObject(null, "C1");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetObjectNullColumnKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.getObject("R1", null);
    }

    @Test(expected = UnknownKeyException.class, timeout = 4000)
    public void testGetObjectUnknownRowKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Val", "R1", "C1");
        data.getObject("UnknownR", "C1");
    }

    @Test(expected = UnknownKeyException.class, timeout = 4000)
    public void testGetObjectUnknownColumnKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Val", "R1", "C1");
        data.getObject("R1", "UnknownC");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetObjectNullRowKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Val", null, "C1");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetObjectNullColumnKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Val", "R1", null);
    }

    @Test(expected = UnknownKeyException.class, timeout = 4000)
    public void testRemoveColumnUnknownKeyThrowsException() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Val", "R1", "C1");
        data.removeColumn("UnknownC");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetRowKeyOutOfBounds() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.getRowKey(0);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetColumnKeyOutOfBounds() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.getColumnKey(0);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetObjectIndexOutOfBounds() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.getObject(0, 0);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testRowKeysListImmutability() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Val", "R1", "C1");
        data.getRowKeys().add("R2");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testColumnKeysListImmutability() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Val", "R1", "C1");
        data.getColumnKeys().add("C2");
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Regression Tests)
    // =========================================================================

    /**
     * Defect 1: removeRow(Comparable) with an unknown key.
     * When rowKey is not recognized, getRowIndex(rowKey) returns -1.
     * Javadoc specifies: @throws UnknownKeyException if rowKey is not recognised.
     * In defective code, it calls removeRow(-1), which throws ArrayIndexOutOfBoundsException.
     */
    @Test(expected = UnknownKeyException.class, timeout = 4000)
    public void testRemoveRowByUnknownKeyDefect() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("V1", "R1", "C1");
        data.removeRow("NonExistentRow");
    }

    /**
     * Defect 2: removeColumn(Comparable) on a sparse 2D table.
     * When R1 only contains C1, and R2 only contains C2:
     * Calling removeColumn("C2") iterates through all rows.
     * In row 1 (which only has C1), rowData.removeValue("C2") is called,
     * which throws UnknownKeyException: The key (C2) is not recognised.
     * The table should successfully remove C2 from all rows containing it without throwing.
     */
    @Test(timeout = 4000)
    public void testRemoveColumnSparseDefect() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("A", "R1", "C1");
        data.setObject("B", "R2", "C2");

        // Defective version will throw UnknownKeyException here
        data.removeColumn("C2");

        assertEquals(1, data.getColumnCount());
        assertEquals("C1", data.getColumnKey(0));
        assertEquals(2, data.getRowCount());
    }

    /**
     * Defect 3: removeColumn(int) triggers sparse matrix removal failure.
     */
    @Test(timeout = 4000)
    public void testRemoveColumnByIndexSparseDefect() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("A", "R1", "C1");
        data.setObject("B", "R2", "C2");

        // C2 is at index 1
        data.removeColumn(1);

        assertEquals(1, data.getColumnCount());
        assertEquals("C1", data.getColumnKey(0));
    }

    /**
     * Defect 4: removeObject(rowKey, colKey) contract.
     * "If all the objects in the specified row and/or column are now null,
     * the row and/or column is removed from the table."
     * In defective code, step 2 (checking if column is empty) is entirely omitted.
     */
    @Test(timeout = 4000)
    public void testRemoveObjectColumnCleanedUpDefect() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Val1", "R1", "C1");
        data.setObject("Val2", "R2", "C1");

        // Remove Val1 from R1, C1. Row R1 is now empty, so row R1 should be removed.
        data.removeObject("R1", "C1");
        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());

        // Remove Val2 from R2, C1. Both row R2 and column C1 are now completely empty.
        data.removeObject("R2", "C1");
        assertEquals(0, data.getRowCount());
        // In the defective version, columnKeys still contains C1, so getColumnCount() == 1 instead of 0
        assertEquals(0, data.getColumnCount());
    }

    // =========================================================================
    // Partition D: Object Lifecycle & Contract Integrity (equals, hashCode, clone, serial)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        KeyedObjects2D d1 = new KeyedObjects2D();
        KeyedObjects2D d2 = new KeyedObjects2D();

        // Reflexive
        assertTrue(d1.equals(d1));
        // Symmetric empty
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));
        assertEquals(d1.hashCode(), d2.hashCode());

        // Null and different class
        assertFalse(d1.equals(null));
        assertFalse(d1.equals("NotAKeyedObjects2D"));

        // Content equality
        d1.setObject("V1", "R1", "C1");
        assertFalse(d1.equals(d2));

        d2.setObject("V1", "R1", "C1");
        assertTrue(d1.equals(d2));
        assertEquals(d1.hashCode(), d2.hashCode());

        // Different values
        d2.setObject("V2", "R1", "C1");
        assertFalse(d1.equals(d2));

        // One null vs non-null value in cell
        d2.setObject(null, "R1", "C1");
        assertFalse(d1.equals(d2));
        assertFalse(d2.equals(d1));

        // Both null values in cell
        d1.setObject(null, "R1", "C1");
        assertTrue(d1.equals(d2));
        assertEquals(d1.hashCode(), d2.hashCode());

        // Different row keys
        KeyedObjects2D d3 = new KeyedObjects2D();
        d3.setObject("V", "RowX", "C1");
        KeyedObjects2D d4 = new KeyedObjects2D();
        d4.setObject("V", "RowY", "C1");
        assertFalse(d3.equals(d4));

        // Different column keys
        KeyedObjects2D d5 = new KeyedObjects2D();
        d5.setObject("V", "R1", "ColX");
        KeyedObjects2D d6 = new KeyedObjects2D();
        d6.setObject("V", "R1", "ColY");
        assertFalse(d5.equals(d6));
    }

    @Test(timeout = 4000)
    public void testCloneIndependence() throws CloneNotSupportedException {
        KeyedObjects2D original = new KeyedObjects2D();
        original.setObject("ValA", "R1", "C1");
        original.setObject("ValB", "R2", "C2");

        KeyedObjects2D cloned = (KeyedObjects2D) original.clone();
        assertNotSame(original, cloned);
        assertEquals(original, cloned);

        // Modify clone and ensure original is unaffected
        cloned.setObject("ValModified", "R1", "C1");
        assertEquals("ValA", original.getObject("R1", "C1"));
        assertEquals("ValModified", cloned.getObject("R1", "C1"));
        assertFalse(original.equals(cloned));

        // Add to clone
        cloned.setObject("ValNew", "R3", "C3");
        assertEquals(2, original.getRowCount());
        assertEquals(3, cloned.getRowCount());
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        KeyedObjects2D original = new KeyedObjects2D();
        original.setObject("Test1", "R1", "C1");
        original.setObject("Test2", "R1", "C2");
        original.setObject(null, "R2", "C1");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(original);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        KeyedObjects2D deserialized = (KeyedObjects2D) in.readObject();
        in.close();

        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());
        assertEquals("Test1", deserialized.getObject("R1", "C1"));
        assertNull(deserialized.getObject("R2", "C1"));
    }
}