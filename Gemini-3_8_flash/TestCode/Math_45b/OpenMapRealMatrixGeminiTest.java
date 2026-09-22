package org.apache.commons.math.linear;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.MathIllegalArgumentException;
import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.apache.commons.math.exception.NumberIsTooLargeException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.math.linear.OpenMapRealMatrix
 * Known Defect: MATH-679 (testMath679)
 * Description : Instantiating OpenMapRealMatrix with dimensions whose product exceeds
 *               Integer.MAX_VALUE causes integer overflow in key computations.
 *               Defective version fails to check (long) row * col >= Integer.MAX_VALUE
 *               and fails to throw NumberIsTooLargeException.
 *
 * Decision / Branch Matrix:
 * 1. OpenMapRealMatrix(int, int)
 *    - Branch: valid dims (>0) vs invalid dims (<=0) -> NotStrictlyPositiveException.
 *    - Defect MATH-679: product >= Integer.MAX_VALUE -> NumberIsTooLargeException.
 * 2. OpenMapRealMatrix(OpenMapRealMatrix) & copy()
 *    - Branch: deep copy verification, independence of underlying storage map.
 * 3. getEntry(int, int)
 *    - Branch: row/col in bounds vs out of bounds (row < 0, row >= rows, col < 0, col >= cols).
 *    - Branch: entry exists vs entry absent (defaults to 0.0).
 * 4. setEntry(int, int, double)
 *    - Branch: value == 0.0 (removes key from map) vs value != 0.0 (inserts/overwrites key).
 *    - Bounds checking on indices.
 * 5. addToEntry(int, int, double)
 *    - Branch: result == 0.0 (removes key) vs result != 0.0 (inserts/updates key).
 * 6. multiplyEntry(int, int, double)
 *    - Branch: result == 0.0 (removes key) vs result != 0.0 (inserts/updates key).
 * 7. add(OpenMapRealMatrix)
 *    - Branch: dimension compatibility check (success vs MatrixDimensionMismatchException).
 *    - Branch: iterate entries and accumulate.
 * 8. subtract(RealMatrix) & subtract(OpenMapRealMatrix)
 *    - Branch: argument is OpenMapRealMatrix (try block).
 *    - Branch: argument is generic RealMatrix (catch ClassCastException -> super.subtract).
 * 9. multiply(RealMatrix) & multiply(OpenMapRealMatrix)
 *    - Branch: argument is OpenMapRealMatrix (try block).
 *    - Branch: argument is generic RealMatrix (catch ClassCastException -> BlockRealMatrix loop).
 *    - Branch in multiply(OpenMapRealMatrix): m.entries.containsKey(rightKey) [true / false].
 *    - Branch in multiply(OpenMapRealMatrix): outValue == 0.0 [true -> remove / false -> put].
 * 10. Serialization / Contracts
 *    - Full roundtrip serialization/deserialization integrity.
 */
public class OpenMapRealMatrixGeminiTest {

    private static final double EPSILON = 1e-11;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicDimensionsAndCreation() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(4, 5);
        assertEquals(4, matrix.getRowDimension());
        assertEquals(5, matrix.getColumnDimension());

        OpenMapRealMatrix created = matrix.createMatrix(2, 3);
        assertNotNull(created);
        assertEquals(2, created.getRowDimension());
        assertEquals(3, created.getColumnDimension());
        assertEquals(0.0, created.getEntry(0, 0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testGetAndSetEntry() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(3, 3);

        // Entry initially zero
        assertEquals(0.0, matrix.getEntry(1, 1), EPSILON);

        // Set non-zero
        matrix.setEntry(1, 1, 42.5);
        assertEquals(42.5, matrix.getEntry(1, 1), EPSILON);

        // Overwrite non-zero
        matrix.setEntry(1, 1, 84.0);
        assertEquals(84.0, matrix.getEntry(1, 1), EPSILON);

        // Set to 0.0 (should remove key from internal map)
        matrix.setEntry(1, 1, 0.0);
        assertEquals(0.0, matrix.getEntry(1, 1), EPSILON);

        // Setting 0.0 on already 0.0 entry
        matrix.setEntry(2, 2, 0.0);
        assertEquals(0.0, matrix.getEntry(2, 2), EPSILON);
    }

    @Test(timeout = 4000)
    public void testAddToEntry() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(3, 3);

        // Add to 0.0 resulting in non-zero
        matrix.addToEntry(0, 0, 5.5);
        assertEquals(5.5, matrix.getEntry(0, 0), EPSILON);

        // Add to non-zero resulting in another non-zero
        matrix.addToEntry(0, 0, 4.5);
        assertEquals(10.0, matrix.getEntry(0, 0), EPSILON);

        // Add to non-zero resulting in 0.0 (hits entries.remove(key) branch)
        matrix.addToEntry(0, 0, -10.0);
        assertEquals(0.0, matrix.getEntry(0, 0), EPSILON);

        // Add 0.0 to 0.0
        matrix.addToEntry(1, 1, 0.0);
        assertEquals(0.0, matrix.getEntry(1, 1), EPSILON);
    }

    @Test(timeout = 4000)
    public void testMultiplyEntry() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(3, 3);

        matrix.setEntry(1, 2, 6.0);
        matrix.multiplyEntry(1, 2, 2.5);
        assertEquals(15.0, matrix.getEntry(1, 2), EPSILON);

        // Multiply by 0.0 (hits entries.remove(key) branch)
        matrix.multiplyEntry(1, 2, 0.0);
        assertEquals(0.0, matrix.getEntry(1, 2), EPSILON);

        // Multiply 0.0 entry by non-zero
        matrix.multiplyEntry(2, 2, 10.0);
        assertEquals(0.0, matrix.getEntry(2, 2), EPSILON);
    }

    @Test(timeout = 4000)
    public void testCopyConstructorAndCopyMethod() {
        OpenMapRealMatrix orig = new OpenMapRealMatrix(3, 3);
        orig.setEntry(0, 1, 12.0);
        orig.setEntry(2, 0, 7.0);

        OpenMapRealMatrix copyFromCtor = new OpenMapRealMatrix(orig);
        OpenMapRealMatrix copyFromMethod = orig.copy();

        assertEquals(orig.getRowDimension(), copyFromCtor.getRowDimension());
        assertEquals(orig.getColumnDimension(), copyFromCtor.getColumnDimension());
        assertEquals(12.0, copyFromCtor.getEntry(0, 1), EPSILON);
        assertEquals(7.0, copyFromCtor.getEntry(2, 0), EPSILON);

        assertEquals(orig.getRowDimension(), copyFromMethod.getRowDimension());
        assertEquals(orig.getColumnDimension(), copyFromMethod.getColumnDimension());
        assertEquals(12.0, copyFromMethod.getEntry(0, 1), EPSILON);
        assertEquals(7.0, copyFromMethod.getEntry(2, 0), EPSILON);

        // Mutating original does not mutate copies
        orig.setEntry(0, 1, 99.0);
        assertEquals(12.0, copyFromCtor.getEntry(0, 1), EPSILON);
        assertEquals(12.0, copyFromMethod.getEntry(0, 1), EPSILON);

        // Mutating copy does not mutate original
        copyFromCtor.setEntry(2, 0, 100.0);
        assertEquals(7.0, orig.getEntry(2, 0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testAddOpenMapRealMatrix() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 3);
        m1.setEntry(0, 0, 1.0);
        m1.setEntry(0, 1, 2.0);
        m1.setEntry(1, 2, 3.0);

        OpenMapRealMatrix m2 = new OpenMapRealMatrix(2, 3);
        m2.setEntry(0, 1, 5.0);
        m2.setEntry(1, 1, 4.0);
        m2.setEntry(1, 2, -3.0); // Will cancel out to 0.0

        OpenMapRealMatrix sum = m1.add(m2);

        assertEquals(1.0, sum.getEntry(0, 0), EPSILON);
        assertEquals(7.0, sum.getEntry(0, 1), EPSILON);
        assertEquals(0.0, sum.getEntry(0, 2), EPSILON);
        assertEquals(0.0, sum.getEntry(1, 0), EPSILON);
        assertEquals(4.0, sum.getEntry(1, 1), EPSILON);
        assertEquals(0.0, sum.getEntry(1, 2), EPSILON); // Canceled out
    }

    @Test(timeout = 4000)
    public void testSubtractOpenMapRealMatrix() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 2);
        m1.setEntry(0, 0, 5.0);
        m1.setEntry(0, 1, 8.0);

        OpenMapRealMatrix m2 = new OpenMapRealMatrix(2, 2);
        m2.setEntry(0, 0, 5.0); // Will cancel out to 0.0
        m2.setEntry(1, 1, 2.0);

        OpenMapRealMatrix diff = m1.subtract(m2);

        assertEquals(0.0, diff.getEntry(0, 0), EPSILON);
        assertEquals(8.0, diff.getEntry(0, 1), EPSILON);
        assertEquals(0.0, diff.getEntry(1, 0), EPSILON);
        assertEquals(-2.0, diff.getEntry(1, 1), EPSILON);
    }

    @Test(timeout = 4000)
    public void testSubtractGenericRealMatrixCatchBranch() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 2);
        m1.setEntry(0, 0, 10.0);
        m1.setEntry(1, 1, 5.0);

        RealMatrix m2 = new BlockRealMatrix(new double[][] {
            {3.0, 1.0},
            {0.0, 5.0}
        });

        // Calls subtract(RealMatrix), triggers ClassCastException catch block
        OpenMapRealMatrix diff = m1.subtract(m2);

        assertEquals(7.0, diff.getEntry(0, 0), EPSILON);
        assertEquals(-1.0, diff.getEntry(0, 1), EPSILON);
        assertEquals(0.0, diff.getEntry(1, 0), EPSILON);
        assertEquals(0.0, diff.getEntry(1, 1), EPSILON);
    }

    @Test(timeout = 4000)
    public void testMultiplyOpenMapRealMatrix() {
        // Test standard multiplication and both branches of rightKey containment & outValue==0.0
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 3);
        m1.setEntry(0, 0, 1.0);
        m1.setEntry(0, 1, -1.0);
        m1.setEntry(1, 2, 4.0);

        OpenMapRealMatrix m2 = new OpenMapRealMatrix(3, 2);
        // Col 0 will produce outValue cancellation to 0.0: (1.0*2.0) + (-1.0*2.0) = 0.0
        m2.setEntry(0, 0, 2.0);
        m2.setEntry(1, 0, 2.0);
        // Col 1 will produce non-zero value: (1.0*3.0) + (-1.0*0.0) = 3.0
        m2.setEntry(0, 1, 3.0);
        m2.setEntry(2, 1, 5.0);

        OpenMapRealMatrix product = m1.multiply(m2);

        assertEquals(2, product.getRowDimension());
        assertEquals(2, product.getColumnDimension());
        // (0,0) canceled to 0.0
        assertEquals(0.0, product.getEntry(0, 0), EPSILON);
        // (0,1) is 1.0 * 3.0 = 3.0
        assertEquals(3.0, product.getEntry(0, 1), EPSILON);
        // (1,0) is 4.0 * m2(2,0)=0.0 = 0.0
        assertEquals(0.0, product.getEntry(1, 0), EPSILON);
        // (1,1) is 4.0 * 5.0 = 20.0
        assertEquals(20.0, product.getEntry(1, 1), EPSILON);
    }

    @Test(timeout = 4000)
    public void testMultiplyGenericRealMatrixCatchBranch() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 3);
        m1.setEntry(0, 0, 2.0);
        m1.setEntry(0, 2, 3.0);
        m1.setEntry(1, 1, 4.0);

        RealMatrix m2 = new BlockRealMatrix(new double[][] {
            {1.0, 2.0},
            {3.0, 4.0},
            {5.0, 6.0}
        });

        // Calls multiply(RealMatrix), triggers ClassCastException catch block returning BlockRealMatrix
        RealMatrix product = m1.multiply(m2);
        assertTrue(product instanceof BlockRealMatrix);
        assertEquals(2, product.getRowDimension());
        assertEquals(2, product.getColumnDimension());

        // Row 0: 2*1 + 0*3 + 3*5 = 17 ; 2*2 + 0*4 + 3*6 = 22
        assertEquals(17.0, product.getEntry(0, 0), EPSILON);
        assertEquals(22.0, product.getEntry(0, 1), EPSILON);
        // Row 1: 0*1 + 4*3 + 0*5 = 12 ; 0*2 + 4*4 + 0*6 = 16
        assertEquals(12.0, product.getEntry(1, 0), EPSILON);
        assertEquals(16.0, product.getEntry(1, 1), EPSILON);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinimalDimensions1x1() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(1, 1);
        assertEquals(1, matrix.getRowDimension());
        assertEquals(1, matrix.getColumnDimension());
        assertEquals(0.0, matrix.getEntry(0, 0), EPSILON);

        matrix.setEntry(0, 0, -99.9);
        assertEquals(-99.9, matrix.getEntry(0, 0), EPSILON);

        OpenMapRealMatrix m2 = new OpenMapRealMatrix(1, 1);
        m2.setEntry(0, 0, 2.0);

        OpenMapRealMatrix mult = matrix.multiply(m2);
        assertEquals(-199.8, mult.getEntry(0, 0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testEmptySparseMatrixOperations() {
        OpenMapRealMatrix empty1 = new OpenMapRealMatrix(3, 3);
        OpenMapRealMatrix empty2 = new OpenMapRealMatrix(3, 3);

        OpenMapRealMatrix sum = empty1.add(empty2);
        assertEquals(0.0, sum.getEntry(0, 0), EPSILON);

        OpenMapRealMatrix diff = empty1.subtract(empty2);
        assertEquals(0.0, diff.getEntry(0, 0), EPSILON);

        OpenMapRealMatrix prod = empty1.multiply(empty2);
        assertEquals(0.0, prod.getEntry(0, 0), EPSILON);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-679)
    // =========================================================================

    /**
     * Targets Commons Math issue MATH-679.
     * Dimensions whose product exceeds Integer.MAX_VALUE must throw NumberIsTooLargeException
     * to prevent integer overflow in internal 1D array indexing (computeKey).
     */
    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testMath679() {
        new OpenMapRealMatrix(3, Integer.MAX_VALUE);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorZeroRow() {
        new OpenMapRealMatrix(0, 5);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorNegativeRow() {
        new OpenMapRealMatrix(-1, 5);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorZeroColumn() {
        new OpenMapRealMatrix(5, 0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorNegativeColumn() {
        new OpenMapRealMatrix(5, -2);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testGetEntryNegativeRow() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(3, 3);
        m.getEntry(-1, 0);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testGetEntryRowTooLarge() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(3, 3);
        m.getEntry(3, 0);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testGetEntryNegativeColumn() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(3, 3);
        m.getEntry(0, -1);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testGetEntryColumnTooLarge() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(3, 3);
        m.getEntry(0, 3);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testSetEntryNegativeRow() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(3, 3);
        m.setEntry(-1, 0, 1.0);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testSetEntryRowTooLarge() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(3, 3);
        m.setEntry(3, 0, 1.0);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testSetEntryNegativeColumn() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(3, 3);
        m.setEntry(0, -1, 1.0);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testSetEntryColumnTooLarge() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(3, 3);
        m.setEntry(0, 3, 1.0);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testAddToEntryOutOfBounds() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(2, 2);
        m.addToEntry(2, 1, 1.0);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testMultiplyEntryOutOfBounds() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(2, 2);
        m.multiplyEntry(1, 2, 2.0);
    }

    @Test(timeout = 4000)
    public void testAddDimensionMismatch() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 3);
        OpenMapRealMatrix m2 = new OpenMapRealMatrix(2, 4);
        try {
            m1.add(m2);
            fail("Expected dimension mismatch exception");
        } catch (MathIllegalArgumentException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testSubtractDimensionMismatch() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(3, 2);
        OpenMapRealMatrix m2 = new OpenMapRealMatrix(2, 2);
        try {
            m1.subtract(m2);
            fail("Expected dimension mismatch exception");
        } catch (MathIllegalArgumentException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testMultiplyOpenMapDimensionMismatch() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 3);
        OpenMapRealMatrix m2 = new OpenMapRealMatrix(2, 2);
        try {
            m1.multiply(m2);
            fail("Expected dimension mismatch exception");
        } catch (DimensionMismatchException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testMultiplyGenericRealMatrixDimensionMismatch() {
        OpenMapRealMatrix m1 = new OpenMapRealMatrix(2, 3);
        RealMatrix m2 = new BlockRealMatrix(2, 2);
        try {
            m1.multiply(m2);
            fail("Expected dimension mismatch exception");
        } catch (DimensionMismatchException expected) {
            // Success
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationRoundtrip() throws IOException, ClassNotFoundException {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(5, 5);
        matrix.setEntry(0, 0, 1.23);
        matrix.setEntry(2, 3, 4.56);
        matrix.setEntry(4, 4, 7.89);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(matrix);
        oos.flush();
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        OpenMapRealMatrix deserialized = (OpenMapRealMatrix) ois.readObject();
        ois.close();

        assertEquals(matrix.getRowDimension(), deserialized.getRowDimension());
        assertEquals(matrix.getColumnDimension(), deserialized.getColumnDimension());
        assertEquals(1.23, deserialized.getEntry(0, 0), EPSILON);
        assertEquals(4.56, deserialized.getEntry(2, 3), EPSILON);
        assertEquals(7.89, deserialized.getEntry(4, 4), EPSILON);
        assertEquals(0.0, deserialized.getEntry(1, 1), EPSILON);
    }
}