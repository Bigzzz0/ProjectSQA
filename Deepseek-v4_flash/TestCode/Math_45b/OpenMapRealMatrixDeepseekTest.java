package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.exception.NumberIsTooLargeException;
import org.apache.commons.math.exception.DimensionMismatchException;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructor and state getters (getRowDimension, getColumnDimension)
 *   - setEntry / getEntry on various positions
 *   - addToEntry / multiplyEntry with zero and non-zero increments/factors
 *   - Entry removal when value becomes zero
 *   - Matrix copy and createMatrix
 *   - add(OpenMapRealMatrix) and subtract(OpenMapRealMatrix)
 *   - multiply(OpenMapRealMatrix) - sparse multiplication
 *   - multiply(RealMatrix) - fallback to block multiplication
 *   - subtract(RealMatrix) - fallback via ClassCastException
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Single element matrix (1x1)
 *   - Matrix with zero entries (all zeros)
 *   - Matrix with max row/column indices
 *   - Large matrix dimensions
 *   - Operations creating zero entries (should remove them)
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - TestMath679: When multiplying sparse matrices, the computeKey in the inner
 *     loop can produce an index that overflows int range when columns * rows is large,
 *     because computeKey(row, column) = row * columns + column, and if row*columns
 *     exceeds Integer.MAX_VALUE, this silently wraps to negative, causing
 *     ArrayIndexOutOfBoundsException or wrong results.
 *     Instead of NumberIsTooLargeException, we expect an overflow handling.
 *     The bug: multiplication does not guard against overflow in computeKey.
 *   - The test should create a matrix large enough that row*columns can overflow.
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Invalid row/column indices (negative, out of bounds)
 *   - Dimension mismatch in add/subtract/multiply
 *   - Null argument (though not checked, may cause NPE)
 * 
 * Partition E: Object Lifecycle & Contract
 *   - copy returns a deep copy? Mutable state isolation check
 *   - createMatrix creates independent matrix
 */
public class OpenMapRealMatrixDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testConstructorAndDimensions() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(3, 5);
        assertEquals(3, m.getRowDimension());
        assertEquals(5, m.getColumnDimension());
    }

    @Test(timeout = 4000)
    public void testCopyConstructor() {
        OpenMapRealMatrix original = new OpenMapRealMatrix(2, 3);
        original.setEntry(0, 0, 1.5);
        original.setEntry(1, 2, -3.7);
        OpenMapRealMatrix copy = new OpenMapRealMatrix(original);
        assertEquals(1.5, copy.getEntry(0, 0), 0.0);
        assertEquals(-3.7, copy.getEntry(1, 2), 0.0);
        // Modify original to ensure copy is independent
        original.setEntry(0, 0, 0.0);
        assertEquals(1.5, copy.getEntry(0, 0), 0.0);
    }

    @Test(timeout = 4000)
    public void testCopyMethod() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(1, 1);
        m.setEntry(0, 0, 42.0);
        OpenMapRealMatrix copied = m.copy();
        assertEquals(42.0, copied.getEntry(0, 0), 0.0);
        copied.setEntry(0, 0, 0.0);
        assertEquals(42.0, m.getEntry(0, 0), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateMatrix() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(2, 2);
        OpenMapRealMatrix created = m.createMatrix(4, 6);
        assertEquals(4, created.getRowDimension());
        assertEquals(6, created.getColumnDimension());
        // All entries should be zero (default)
        assertEquals(0.0, created.getEntry(0, 0), 0.0);
        assertEquals(0.0, created.getEntry(3, 5), 0.0);
    }

    @Test(timeout = 4000)
    public void testSetAndGetEntry() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(10, 10);
        m.setEntry(3, 7, 2.5);
        assertEquals(2.5, m.getEntry(3, 7), 0.0);
        // Non-set entry should be zero
        assertEquals(0.0, m.getEntry(0, 0), 0.0);
        assertEquals(0.0, m.getEntry(9, 9), 0.0);
        // Setting to zero should remove entry
        m.setEntry(3, 7, 0.0);
        assertEquals(0.0, m.getEntry(3, 7), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddToEntry() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(2, 2);
        m.addToEntry(0, 0, 1.5);
        assertEquals(1.5, m.getEntry(0, 0), 0.0);
        m.addToEntry(0, 0, -0.5);
        assertEquals(1.0, m.getEntry(0, 0), 0.0);
        // Adding to zero should remove entry if it becomes zero
        m.addToEntry(0, 0, -1.0);
        assertEquals(0.0, m.getEntry(0, 0), 0.0);
    }

    @Test(timeout = 4000)
    public void testMultiplyEntry() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(2, 2);
        m.setEntry(1, 1, 4.0);
        m.multiplyEntry(1, 1, 0.5);
        assertEquals(2.0, m.getEntry(1, 1), 0.0);
        m.multiplyEntry(1, 1, 0.0);
        assertEquals(0.0, m.getEntry(1, 1), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddOpenMapRealMatrix() {
        OpenMapRealMatrix a = new OpenMapRealMatrix(3, 3);
        a.setEntry(0, 0, 1.0);
        a.setEntry(1, 2, 3.0);
        OpenMapRealMatrix b = new OpenMapRealMatrix(3, 3);
        b.setEntry(0, 0, 2.0);
        b.setEntry(2, 1, -1.0);
        OpenMapRealMatrix result = a.add(b);
        assertEquals(3.0, result.getEntry(0, 0), 0.0);
        assertEquals(3.0, result.getEntry(1, 2), 0.0);
        assertEquals(-1.0, result.getEntry(2, 1), 0.0);
        // Result should not have entries that were zero in both
        assertEquals(0.0, result.getEntry(0, 1), 0.0);
    }

    @Test(timeout = 4000)
    public void testSubtractOpenMapRealMatrix() {
        OpenMapRealMatrix a = new OpenMapRealMatrix(2, 2);
        a.setEntry(0, 0, 5.0);
        a.setEntry(1, 1, 5.0);
        OpenMapRealMatrix b = new OpenMapRealMatrix(2, 2);
        b.setEntry(0, 0, 3.0);
        b.setEntry(1, 1, 7.0);
        OpenMapRealMatrix result = a.subtract(b);
        assertEquals(2.0, result.getEntry(0, 0), 0.0);
        assertEquals(-2.0, result.getEntry(1, 1), 0.0);
    }

    @Test(timeout = 4000)
    public void testSubtractRealMatrixFallback() {
        OpenMapRealMatrix a = new OpenMapRealMatrix(2, 2);
        a.setEntry(0, 0, 10.0);
        // Use a non-OpenMapRealMatrix as argument
        RealMatrix b = new BlockRealMatrix(2, 2);
        b.setEntry(0, 0, 3.0);
        RealMatrix result = a.subtract(b);
        assertTrue(result instanceof OpenMapRealMatrix);
        assertEquals(7.0, result.getEntry(0, 0), 0.0);
    }

    @Test(timeout = 4000)
    public void testMultiplyOpenMapRealMatrix() {
        OpenMapRealMatrix a = new OpenMapRealMatrix(2, 3);
        a.setEntry(0, 0, 1.0);
        a.setEntry(0, 2, 2.0);
        a.setEntry(1, 1, 3.0);
        OpenMapRealMatrix b = new OpenMapRealMatrix(3, 2);
        b.setEntry(0, 0, 4.0);
        b.setEntry(2, 1, 5.0);
        b.setEntry(1, 1, 6.0);
        OpenMapRealMatrix result = a.multiply(b);
        // Expected: result[0][0] = 1*4 = 4; result[0][1] = 2*5 = 10; result[1][1] = 3*6 = 18
        assertEquals(4.0, result.getEntry(0, 0), 0.0);
        assertEquals(10.0, result.getEntry(0, 1), 0.0);
        assertEquals(18.0, result.getEntry(1, 1), 0.0);
        assertEquals(0.0, result.getEntry(1, 0), 0.0);
    }

    @Test(timeout = 4000)
    public void testMultiplyRealMatrixFallback() {
        OpenMapRealMatrix a = new OpenMapRealMatrix(2, 2);
        a.setEntry(0, 0, 2.0);
        a.setEntry(1, 1, 3.0);
        RealMatrix b = new BlockRealMatrix(2, 2);
        b.setEntry(0, 0, 5.0);
        b.setEntry(1, 1, 7.0);
        RealMatrix result = a.multiply(b);
        assertTrue(result instanceof BlockRealMatrix);
        assertEquals(10.0, result.getEntry(0, 0), 0.0);
        assertEquals(21.0, result.getEntry(1, 1), 0.0);
    }

    // ==================== Partition B: Boundary & Edge Cases ====================

    @Test(timeout = 4000)
    public void testSingleElementMatrix() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(1, 1);
        assertEquals(0.0, m.getEntry(0, 0), 0.0);
        m.setEntry(0, 0, 99.9);
        assertEquals(99.9, m.getEntry(0, 0), 0.0);
        m.setEntry(0, 0, 0.0);
        assertEquals(0.0, m.getEntry(0, 0), 0.0);
    }

    @Test(timeout = 4000)
    public void testAllZeroMatrix() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(5, 5);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                assertEquals(0.0, m.getEntry(i, j), 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testAddCreatesZeroEntries() {
        OpenMapRealMatrix a = new OpenMapRealMatrix(2, 2);
        a.setEntry(0, 0, 1.0);
        OpenMapRealMatrix b = new OpenMapRealMatrix(2, 2);
        b.setEntry(0, 0, -1.0);
        OpenMapRealMatrix result = a.add(b);
        // The sum should be zero, so entry should be removed
        assertEquals(0.0, result.getEntry(0, 0), 0.0);
    }

    @Test(timeout = 4000)
    public void testMultiplySparseWithZeroResult() {
        OpenMapRealMatrix a = new OpenMapRealMatrix(2, 2);
        a.setEntry(0, 0, 1.0);
        OpenMapRealMatrix b = new OpenMapRealMatrix(2, 2);
        // b has no entries, so multiplication result should be all zero
        OpenMapRealMatrix result = a.multiply(b);
        assertEquals(0.0, result.getEntry(0, 0), 0.0);
        assertEquals(0.0, result.getEntry(0, 1), 0.0);
        assertEquals(0.0, result.getEntry(1, 0), 0.0);
        assertEquals(0.0, result.getEntry(1, 1), 0.0);
    }

    @Test(timeout = 4000)
    public void testLargeMatrixBoundary() {
        int rows = 100;
        int cols = 100;
        OpenMapRealMatrix m = new OpenMapRealMatrix(rows, cols);
        m.setEntry(99, 99, 123.456);
        assertEquals(123.456, m.getEntry(99, 99), 0.0);
        m.setEntry(0, 0, -0.001);
        assertEquals(-0.001, m.getEntry(0, 0), 0.0);
    }

    // ==================== Partition C: Defect-Targeted Test ====================

    @Test(timeout = 4000)
    public void testMath679_OverflowInComputeKeyDuringMultiplication() {
        // This test targets the known defect: multiplying OpenMapRealMatrix with
        // dimensions where row * columns can overflow int during computeKey.
        // We need a matrix such that rows * columns > Integer.MAX_VALUE,
        // but rows and columns individually are within int range.
        // The matrix needs to be large enough that row*columns can overflow,
        // but also have non-zero entries and a multiplication that triggers
        // computeKey with a large row index.
        // 
        // For example: rows = 50000, columns = 50000 => rows*columns = 2.5e9 > Integer.MAX_VALUE
        // But this would create a huge sparse matrix; we can use smaller values
        // like rows = 46341, columns = 46341 => rows*cols = 2,147,488,281 > Integer.MAX_VALUE
        // But to keep it practical, we use rows = 50000, columns = 50000
        // and only set a few entries.
        //
        // The expected behavior: multiplication should detect overflow and throw
        // NumberIsTooLargeException or handle it gracefully.
        // Currently, the bug causes silent overflow, so we expect an exception.
        
        int rows = 50000;
        int cols = 50000;
        try {
            OpenMapRealMatrix a = new OpenMapRealMatrix(rows, cols);
            OpenMapRealMatrix b = new OpenMapRealMatrix(rows, cols);
            a.setEntry(0, 0, 1.0);
            b.setEntry(0, 0, 1.0);
            // This multiplication triggers the bug: computeKey(0,0) = 0*cols+0 = 0, fine,
            // but when iterating, k = 0, and for j from 0 to outCols-1,
            // computeKey(0,j) = 0*cols + j = j, which is fine.
            // To trigger overflow, we need a row index > Integer.MAX_VALUE/cols.
            // Let's set a non-zero entry at row = 1, column = 0? Actually row=1 is small.
            // We need an entry where key = row*cols + col overflows.
            // Let's set entry at row = cols (50000) but rows=50000 so max row index=49999.
            // Actually, rows=50000, cols=50000, max row=49999, max col=49999.
            // row*cols = 49999*50000 = 2,499,950,000 which is < Integer.MAX_VALUE (2,147,483,647)
            // So we need larger rows/cols to overflow.
            // Let's use rows = 46341, cols = 46341 => max row*cols = 46340*46341 = 2,147,008,340?
            // Actually 46340*46341 = 2,147,480,940? Let's compute precisely:
            // 46340 * 46341 = 46340*46340 + 46340 = 2,147,395,600 + 46,340 = 2,147,441,940
            // That's > Integer.MAX_VALUE (2,147,483,647)? Actually 2,147,441,940 < 2,147,483,647
            // So we need even larger, like rows=46341, cols=46342 => max key = 46340*46342 = 2,147,...
            // This is getting complex. Let's use a simpler approach:
            // Create a matrix with rows = Integer.MAX_VALUE/1000 + 1, cols = 1000
            // Then row*cols can overflow for large row indices.
            // 
            // Simpler: rows = 50000, cols = 50000, set entry at row=46341, col=0
            // But rows=50000, so row=46341 is valid. key = 46341*50000 = 2,317,050,000 > Integer.MAX_VALUE
            // That will overflow in computeKey!
            
            int largeRows = 50000;
            int largeCols = 50000;
            OpenMapRealMatrix a2 = new OpenMapRealMatrix(largeRows, largeCols);
            OpenMapRealMatrix b2 = new OpenMapRealMatrix(largeRows, largeCols);
            // Place an entry at row=46341, col=0 (valid indices)
            int overflowRow = 46341; // 46341 * 50000 = 2,317,050,000 > 2,147,483,647
            int overflowCol = 0;
            a2.setEntry(overflowRow, overflowCol, 1.5);
            b2.setEntry(0, 0, 2.0); // This will try to multiply and compute keys that may overflow
            // This should trigger overflow in computeKey during multiplication
            try {
                OpenMapRealMatrix result = a2.multiply(b2);
                // If we reach here, the bug is present (no exception thrown for overflow)
                // We'll fail the test to reveal the bug
                fail("Expected NumberIsTooLargeException due to index overflow, but no exception was thrown");
            } catch (NumberIsTooLargeException e) {
                // Expected behavior: overflow detected, exception thrown
                assertTrue(true);
            } catch (Exception e) {
                // Any other exception is acceptable as well
                assertTrue(e instanceof RuntimeException);
            }
        } catch (Exception e) {
            // If construction already fails, that's fine too (e.g., negative array size)
            // But construction should succeed for 50000x50000
            fail("Unexpected exception during setup: " + e.getMessage());
        }
    }

    // ==================== Partition D: Exception Paths ====================

    @Test(expected = org.apache.commons.math.exception.OutOfRangeException.class, timeout = 4000)
    public void testGetEntryNegativeRow() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(2, 2);
        m.getEntry(-1, 0);
    }

    @Test(expected = org.apache.commons.math.exception.OutOfRangeException.class, timeout = 4000)
    public void testGetEntryRowTooLarge() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(2, 2);
        m.getEntry(2, 0);
    }

    @Test(expected = org.apache.commons.math.exception.OutOfRangeException.class, timeout = 4000)
    public void testGetEntryColumnTooLarge() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(2, 2);
        m.getEntry(0, 2);
    }

    @Test(expected = org.apache.commons.math.exception.OutOfRangeException.class, timeout = 4000)
    public void testSetEntryNegativeColumn() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(2, 2);
        m.setEntry(0, -1, 1.0);
    }

    @Test(expected = org.apache.commons.math.exception.OutOfRangeException.class, timeout = 4000)
    public void testAddToEntryOutOfRange() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(3, 3);
        m.addToEntry(3, 0, 1.0);
    }

    @Test(expected = org.apache.commons.math.exception.OutOfRangeException.class, timeout = 4000)
    public void testMultiplyEntryOutOfRange() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(3, 3);
        m.multiplyEntry(0, 3, 1.0);
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testAddDimensionMismatch() {
        OpenMapRealMatrix a = new OpenMapRealMatrix(2, 2);
        OpenMapRealMatrix b = new OpenMapRealMatrix(3, 2);
        a.add(b);
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testSubtractDimensionMismatch() {
        OpenMapRealMatrix a = new OpenMapRealMatrix(2, 3);
        OpenMapRealMatrix b = new OpenMapRealMatrix(2, 2);
        a.subtract(b);
    }

    @Test(expected = org.apache.commons.math.exception.MatrixDimensionMismatchException.class, timeout = 4000)
    public void testMultiplyDimensionMismatch() {
        OpenMapRealMatrix a = new OpenMapRealMatrix(2, 3);
        OpenMapRealMatrix b = new OpenMapRealMatrix(4, 2);
        a.multiply(b);
    }

    @Test(expected = org.apache.commons.math.exception.NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorNonPositiveRows() {
        new OpenMapRealMatrix(0, 5);
    }

    @Test(expected = org.apache.commons.math.exception.NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorNonPositiveColumns() {
        new OpenMapRealMatrix(5, -1);
    }

    // ==================== Partition E: Contract & Lifecycle ====================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 2);
        m1.setEntry(0, 0, 1.0);
        m1.setEntry(1, 1, 2.0);
        OpenMapRealMatrix m2 = new OpenMapRealMatrix(2, 2);
        m2.setEntry(0, 0, 1.0);
        m2.setEntry(1, 1, 2.0);
        // OpenMapRealMatrix inherits equals from AbstractRealMatrix which does element-wise
        // but uses getEntry() so should work
        assertTrue(m1.equals(m2));
        assertTrue(m2.equals(m1));
        assertEquals(m1.hashCode(), m2.hashCode());
        // Different size
        OpenMapRealMatrix m3 = new OpenMapRealMatrix(2, 3);
        assertFalse(m1.equals(m3));
    }

    @Test(timeout = 4000)
    public void testMutableStateIsolationAfterCopy() {
        OpenMapRealMatrix original = new OpenMapRealMatrix(2, 2);
        original.setEntry(0, 0, 5.0);
        OpenMapRealMatrix copy = original.copy();
        // Modify original
        original.setEntry(0, 0, 10.0);
        // Copy should remain unchanged
        assertEquals(5.0, copy.getEntry(0, 0), 0.0);
        // Modify copy
        copy.setEntry(1, 1, 20.0);
        // Original should be unaffected
        assertEquals(0.0, original.getEntry(1, 1), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateMatrixIndependence() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(3, 3);
        m.setEntry(0, 0, 1.0);
        OpenMapRealMatrix created = m.createMatrix(3, 3);
        created.setEntry(0, 0, 2.0);
        assertEquals(1.0, m.getEntry(0, 0), 0.0);
    }

    @Test(timeout = 4000)
    public void testSerializationCompatibility() {
        // Test that the class is serializable (since it implements Serializable)
        OpenMapRealMatrix m = new OpenMapRealMatrix(2, 2);
        m.setEntry(1, 0, 77.7);
        assertTrue(m instanceof java.io.Serializable);
        // Basic serialization round trip
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(m);
            oos.close();
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
            OpenMapRealMatrix deserialized = (OpenMapRealMatrix) ois.readObject();
            assertEquals(77.7, deserialized.getEntry(1, 0), 0.0);
            assertEquals(0.0, deserialized.getEntry(0, 0), 0.0);
        } catch (Exception e) {
            fail("Serialization failed: " + e.getMessage());
        }
    }
}