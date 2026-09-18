package org.jfree.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * /* [Branch & Defect Analysis Matrix]
 *
 * Targeted Decision Branches & Boundary Conditions:
 * 1. Construction:
 *    - Default constructor vs boolean sortRowKeys flag (false vs true).
 * 2. Row/Column Additions & Sorting (setValue/addValue):
 *    - Adding new row (sorted vs unsorted binarySearch insert point vs append).
 *    - Updating existing row.
 *    - Adding new column vs existing column.
 *    - Setting null values and updating existing non-null values with null / new numbers.
 * 3. Retrieval (getValue, getRowIndex, getColumnIndex, etc.):
 *    - Null keys in getRowIndex / getColumnIndex (throws IllegalArgumentException).
 *    - Unknown rowKey / columnKey in getValue(Comparable, Comparable) (throws UnknownKeyException).
 *    - Null columnKey or rowKey in getValue (throws IllegalArgumentException).
 *    - getValue(int, int) when row has no entry for column -> returns null.
 *    - Unmodifiable views of row/column keys list.
 * 4. Deletions (removeRow, removeColumn, removeValue, clear):
 *    - removeRow by index and by Comparable key.
 *    - removeColumn by index and by Comparable key.
 *    - removeColumn(Comparable key): Null key check (throws IllegalArgumentException),
 *      and UnknownKeyException when column does not exist (Defects4J Bug 1835955 / testRemoveColumnByKey).
 *    - removeValue(Comparable, Comparable):
 *      * allNull row checking (cleans up empty row).
 *      * allNull column checking (cleans up column and removes from rows).
 *      * partial nulls where row or column is retained.
 * 5. Object Contract (equals, hashCode, clone, serialization):
 *    - equals: null, self, non-KeyedValues2D type, mismatched rows/columns size,
 *      mismatched row keys, mismatched column keys, null values matching vs mismatching.
 *    - clone: independent deep copy of internal rows list, verifying mutation on clone
 *      does not affect original.
 *    - serialization: roundtrip equality.
 *
 * Known Defect (Defects4J Bug 1835955):
 * - Calling removeColumn(Comparable columnKey) where the columnKey does not exist
 *   or calling removeColumn when rows exist can throw IndexOutOfBoundsException or
 *   fail to throw UnknownKeyException if columnKey is not found in columnKeys.
 */
public class DefaultKeyedValues2DGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddAndRetrieveBasic() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.addValue(10.0, "R1", "C1");
        data.addValue(20.0, "R1", "C2");
        data.addValue(30.0, "R2", "C1");

        assertEquals(2, data.getRowCount());
        assertEquals(2, data.getColumnCount());

        assertEquals(10.0, data.getValue("R1", "C1"));
        assertEquals(20.0, data.getValue("R1", "C2"));
        assertEquals(30.0, data.getValue("R2", "C1"));
        assertNull(data.getValue("R2", "C2"));

        assertEquals(10.0, data.getValue(0, 0));
        assertEquals(20.0, data.getValue(0, 1));
        assertEquals(30.0, data.getValue(1, 0));
        assertNull(data.getValue(1, 1));
    }

    @Test(timeout = 4000)
    public void testSetValueOverwrites() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.setValue(100, "R1", "C1");
        assertEquals(100, data.getValue("R1", "C1"));

        data.setValue(200, "R1", "C1");
        assertEquals(200, data.getValue("R1", "C1"));
        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
    }

    @Test(timeout = 4000)
    public void testSortedRowKeys() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D(true);
        data.setValue(1.0, "B", "C1");
        data.setValue(2.0, "A", "C1");
        data.setValue(3.0, "C", "C1");

        assertEquals("A", data.getRowKey(0));
        assertEquals("B", data.getRowKey(1));
        assertEquals("C", data.getRowKey(2));

        assertEquals(0, data.getRowIndex("A"));
        assertEquals(1, data.getRowIndex("B"));
        assertEquals(2, data.getRowIndex("C"));
        assertTrue(data.getRowIndex("NonExistent") < 0);
    }

    @Test(timeout = 4000)
    public void testUnsortedRowKeys() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D(false);
        data.setValue(1.0, "B", "C1");
        data.setValue(2.0, "A", "C1");

        assertEquals("B", data.getRowKey(0));
        assertEquals("A", data.getRowKey(1));
        assertEquals(0, data.getRowIndex("B"));
        assertEquals(1, data.getRowIndex("A"));
        assertEquals(-1, data.getRowIndex("NonExistent"));
    }

    @Test(timeout = 4000)
    public void testGetRowAndColumnKeys() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.setValue(1, "R1", "C1");
        data.setValue(2, "R2", "C2");

        List rKeys = data.getRowKeys();
        List cKeys = data.getColumnKeys();

        assertEquals(2, rKeys.size());
        assertEquals("R1", rKeys.get(0));
        assertEquals("R2", rKeys.get(1));

        assertEquals(2, cKeys.size());
        assertEquals("C1", cKeys.get(0));
        assertEquals("C2", cKeys.get(1));

        try {
            rKeys.add("R3");
            fail("Row keys list should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }

        try {
            cKeys.add("C3");
            fail("Column keys list should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testClear() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.setValue(1, "R1", "C1");
        data.setValue(2, "R2", "C2");
        assertEquals(2, data.getRowCount());
        assertEquals(2, data.getColumnCount());

        data.clear();
        assertEquals(0, data.getRowCount());
        assertEquals(0, data.getColumnCount());
        assertEquals(0, data.getRowKeys().size());
        assertEquals(0, data.getColumnKeys().size());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Removal State Logic
    // =========================================================================

    @Test(timeout = 4000)
    public void testRemoveRowByIndexAndKey() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.setValue(1, "R1", "C1");
        data.setValue(2, "R2", "C1");
        data.setValue(3, "R3", "C1");

        data.removeRow(1); // removes R2
        assertEquals(2, data.getRowCount());
        assertEquals("R1", data.getRowKey(0));
        assertEquals("R3", data.getRowKey(1));

        data.removeRow("R1"); // removes R1
        assertEquals(1, data.getRowCount());
        assertEquals("R3", data.getRowKey(0));
    }

    @Test(timeout = 4000)
    public void testRemoveColumnByIndex() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.setValue(1, "R1", "C1");
        data.setValue(2, "R1", "C2");

        data.removeColumn(0); // removes C1
        assertEquals(1, data.getColumnCount());
        assertEquals("C2", data.getColumnKey(0));
        assertEquals(2, data.getValue("R1", "C2"));
    }

    @Test(timeout = 4000)
    public void testRemoveValueRemovesRowAndColumnWhenAllNull() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.setValue(10, "R1", "C1");

        // Single cell present: removing it should clear both row and column
        data.removeValue("R1", "C1");

        assertEquals(0, data.getRowCount());
        assertEquals(0, data.getColumnCount());
    }

    @Test(timeout = 4000)
    public void testRemoveValueRetainsPartialEntries() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.setValue(10, "R1", "C1");
        data.setValue(20, "R1", "C2");
        data.setValue(30, "R2", "C1");

        // Remove R1, C1: R1 still has C2 (20), C1 still has R2 (30)
        data.removeValue("R1", "C1");

        assertEquals(2, data.getRowCount());
        assertEquals(2, data.getColumnCount());
        assertNull(data.getValue("R1", "C1"));
        assertEquals(20, data.getValue("R1", "C2"));
        assertEquals(30, data.getValue("R2", "C1"));
    }

    @Test(timeout = 4000)
    public void testRemoveValueWhenEntireColumnBecomesNull() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.setValue(10, "R1", "C1");
        data.setValue(20, "R1", "C2");

        // R1 has C1 and C2. Remove C2: C2 should be pruned completely, R1 retained.
        data.removeValue("R1", "C2");

        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("C1", data.getColumnKey(0));
        assertEquals(10, data.getValue("R1", "C1"));
    }

    @Test(timeout = 4000)
    public void testRemoveValueWhenEntireRowBecomesNull() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.setValue(10, "R1", "C1");
        data.setValue(20, "R2", "C1");

        // C1 present in R1 and R2. Remove R2, C1: R2 pruned completely, C1 retained.
        data.removeValue("R2", "C1");

        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("R1", data.getRowKey(0));
        assertEquals(10, data.getValue("R1", "C1"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug 1835955)
    // =========================================================================

    /**
     * Targets Bug 1835955 / testRemoveColumnByKey.
     * When removeColumn(Comparable) is invoked with a key not in the dataset,
     * the specification requires throwing UnknownKeyException.
     * Additionally, removing an existing column when multiple rows exist must not throw IndexOutOfBoundsException.
     */
    @Test(timeout = 4000, expected = UnknownKeyException.class)
    public void testDefectRemoveNonExistentColumnThrowsUnknownKeyException() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.setValue(1.0, "R1", "C1");
        // Unknown column key removal must throw UnknownKeyException
        data.removeColumn("UnknownCol");
    }

    @Test(timeout = 4000)
    public void testDefectRemoveExistingColumnDoesNotThrowOutOfBounds() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.setValue(1.0, "R1", "C1");
        data.setValue(2.0, "R2", "C1");
        data.setValue(3.0, "R3", "C2");

        // Removing C2 should not throw IndexOutOfBoundsException even if row doesn't contain C2
        data.removeColumn("C2");
        assertEquals(1, data.getColumnCount());
        assertEquals(0, data.getColumnIndex("C1"));
        assertEquals(-1, data.getColumnIndex("C2"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetRowIndexNullKey() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.getRowIndex(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetColumnIndexNullKey() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.getColumnIndex(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetValueNullRowKey() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.getValue(null, "C1");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetValueNullColumnKey() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.getValue("R1", null);
    }

    @Test(timeout = 4000, expected = UnknownKeyException.class)
    public void testGetValueUnknownColumnKey() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.setValue(1, "R1", "C1");
        data.getValue("R1", "NoSuchColumn");
    }

    @Test(timeout = 4000, expected = UnknownKeyException.class)
    public void testGetValueUnknownRowKey() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.setValue(1, "R1", "C1");
        data.getValue("NoSuchRow", "C1");
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetValueInvalidRowIndex() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.getValue(0, 0);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetRowKeyNegativeIndex() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.getRowKey(-1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetColumnKeyNegativeIndex() {
        DefaultKeyedValues2D data = new DefaultKeyedValues2D();
        data.getColumnKey(-1);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        DefaultKeyedValues2D d1 = new DefaultKeyedValues2D();
        DefaultKeyedValues2D d2 = new DefaultKeyedValues2D();

        // Reflexive
        assertTrue(d1.equals(d1));
        assertEquals(d1.hashCode(), d1.hashCode());

        // Null & Class comparison
        assertFalse(d1.equals(null));
        assertFalse(d1.equals("SomeString"));

        // Both empty
        assertTrue(d1.equals(d2));
        assertEquals(d1.hashCode(), d2.hashCode());

        // Different row keys
        d1.setValue(1.0, "R1", "C1");
        assertFalse(d1.equals(d2));
        d2.setValue(1.0, "R2", "C1");
        assertFalse(d1.equals(d2));

        // Matching structure and values
        d2.clear();
        d2.setValue(1.0, "R1", "C1");
        assertTrue(d1.equals(d2));
        assertEquals(d1.hashCode(), d2.hashCode());

        // Different column keys
        DefaultKeyedValues2D d3 = new DefaultKeyedValues2D();
        d3.setValue(1.0, "R1", "C2");
        assertFalse(d1.equals(d3));

        // Different values (null vs non-null)
        DefaultKeyedValues2D d4 = new DefaultKeyedValues2D();
        d4.setValue(null, "R1", "C1");
        assertFalse(d1.equals(d4));
        assertFalse(d4.equals(d1));

        // Both having null values
        DefaultKeyedValues2D d5 = new DefaultKeyedValues2D();
        d5.setValue(null, "R1", "C1");
        assertTrue(d4.equals(d5));
        assertEquals(d4.hashCode(), d5.hashCode());

        // Different non-null value
        DefaultKeyedValues2D d6 = new DefaultKeyedValues2D();
        d6.setValue(99.0, "R1", "C1");
        assertFalse(d1.equals(d6));
    }

    @Test(timeout = 4000)
    public void testCloneIndependence() throws Exception {
        DefaultKeyedValues2D original = new DefaultKeyedValues2D();
        original.setValue(10, "R1", "C1");
        original.setValue(20, "R2", "C2");

        DefaultKeyedValues2D copy = (DefaultKeyedValues2D) original.clone();
        assertNotSame(original, copy);
        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());

        // Mutate clone and assert original is isolated
        copy.setValue(99, "R1", "C1");
        assertEquals(10, original.getValue("R1", "C1"));
        assertEquals(99, copy.getValue("R1", "C1"));
        assertFalse(original.equals(copy));

        // Add to clone
        copy.setValue(100, "R3", "C3");
        assertEquals(2, original.getRowCount());
        assertEquals(3, copy.getRowCount());
    }

    @Test(timeout = 4000)
    public void testSerializationRoundtrip() throws Exception {
        DefaultKeyedValues2D original = new DefaultKeyedValues2D(true);
        original.setValue(123.45, "RowA", "ColA");
        original.setValue(null, "RowB", "ColB");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DefaultKeyedValues2D deserialized = (DefaultKeyedValues2D) ois.readObject();

        assertNotSame(original, deserialized);
        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());
        assertEquals(123.45, deserialized.getValue("RowA", "ColA"));
        assertNull(deserialized.getValue("RowB", "ColB"));
    }
}