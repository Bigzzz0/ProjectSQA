package org.apache.commons.math3.optim.nonlinear.vector;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.NonSquareMatrixException;

/**
 * White-box test suite for Weight class.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructor with double[]: creates diagonal matrix, verifies entries
 *   - Constructor with RealMatrix: copies matrix, verifies entries
 *   - getWeight(): returns a copy, not the original reference
 * 
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - Empty double[]: zero-length diagonal matrix
 *   - Single element double[]: 1x1 matrix
 *   - Large double[]: stress test for memory (targets OutOfMemoryError defect)
 *   - Null arguments (not applicable due to NPE from MatrixUtils)
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Large sample test: triggers OutOfMemoryError in defective version
 *     due to inefficient matrix creation in Weight(double[]) constructor
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Non-square RealMatrix: throws NonSquareMatrixException
 *   - Null RealMatrix: throws NullPointerException (from copy())
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Immutability: getWeight() returns independent copy
 *   - Multiple calls to getWeight() return independent copies
 */
public class WeightDeepseekTest {

    /* ========== Partition A: Core Functional Logic & State Transitions ========== */

    @Test(timeout = 4000)
    public void testDiagonalConstructorAndGetWeight() {
        double[] weights = {1.0, 2.0, 3.0};
        Weight weight = new Weight(weights);
        RealMatrix matrix = weight.getWeight();
        
        assertEquals(3, matrix.getRowDimension());
        assertEquals(3, matrix.getColumnDimension());
        assertEquals(1.0, matrix.getEntry(0, 0), 0.0);
        assertEquals(2.0, matrix.getEntry(1, 1), 0.0);
        assertEquals(3.0, matrix.getEntry(2, 2), 0.0);
        assertEquals(0.0, matrix.getEntry(0, 1), 0.0);
        assertEquals(0.0, matrix.getEntry(1, 0), 0.0);
    }

    @Test(timeout = 4000)
    public void testRealMatrixConstructorAndGetWeight() {
        RealMatrix input = new DiagonalMatrix(new double[]{4.0, 5.0, 6.0});
        Weight weight = new Weight(input);
        RealMatrix matrix = weight.getWeight();
        
        assertEquals(3, matrix.getRowDimension());
        assertEquals(3, matrix.getColumnDimension());
        assertEquals(4.0, matrix.getEntry(0, 0), 0.0);
        assertEquals(5.0, matrix.getEntry(1, 1), 0.0);
        assertEquals(6.0, matrix.getEntry(2, 2), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetWeightReturnsCopy() {
        double[] weights = {7.0, 8.0};
        Weight weight = new Weight(weights);
        RealMatrix firstCopy = weight.getWeight();
        RealMatrix secondCopy = weight.getWeight();
        
        // Verify they are different objects
        assertNotSame(firstCopy, secondCopy);
        // Verify they have same values
        assertEquals(firstCopy.getEntry(0, 0), secondCopy.getEntry(0, 0), 0.0);
        assertEquals(firstCopy.getEntry(1, 1), secondCopy.getEntry(1, 1), 0.0);
    }

    /* ========== Partition B: Boundary Value Analysis (BVA) & Extremes ========== */

    @Test(timeout = 4000)
    public void testEmptyDiagonalArray() {
        double[] weights = {};
        Weight weight = new Weight(weights);
        RealMatrix matrix = weight.getWeight();
        
        assertEquals(0, matrix.getRowDimension());
        assertEquals(0, matrix.getColumnDimension());
    }

    @Test(timeout = 4000)
    public void testSingleElementDiagonalArray() {
        double[] weights = {42.0};
        Weight weight = new Weight(weights);
        RealMatrix matrix = weight.getWeight();
        
        assertEquals(1, matrix.getRowDimension());
        assertEquals(1, matrix.getColumnDimension());
        assertEquals(42.0, matrix.getEntry(0, 0), 0.0);
    }

    /* ========== Partition C: Defect-Targeted Branch Zone ========== */

    /**
     * Targets the known OutOfMemoryError defect in Defects4J.
     * The defective version uses MatrixUtils.createRealMatrix(dim, dim)
     * which creates a dense matrix, causing OOM for large dimensions.
     * The correct behavior should use a sparse/diagonal representation.
     * 
     * This test verifies that a large diagonal weight matrix can be created
     * without throwing OutOfMemoryError.
     */
    @Test(timeout = 4000)
    public void testLargeSample() {
        // Use a size that would trigger OOM in defective version
        // but should work in fixed version with diagonal optimization
        int size = 100000;  // 100k x 100k diagonal matrix
        double[] weights = new double[size];
        for (int i = 0; i < size; i++) {
            weights[i] = 1.0;
        }
        
        try {
            Weight weight = new Weight(weights);
            RealMatrix matrix = weight.getWeight();
            
            // Verify the matrix is diagonal (should be efficient)
            assertEquals(size, matrix.getRowDimension());
            assertEquals(size, matrix.getColumnDimension());
            assertEquals(1.0, matrix.getEntry(0, 0), 0.0);
            assertEquals(1.0, matrix.getEntry(size - 1, size - 1), 0.0);
            assertEquals(0.0, matrix.getEntry(0, 1), 0.0);
        } catch (OutOfMemoryError e) {
            fail("OutOfMemoryError thrown: defective version creates dense matrix instead of diagonal");
        }
    }

    /* ========== Partition D: Exception & Defensive Guard Paths ========== */

    @Test(expected = NonSquareMatrixException.class, timeout = 4000)
    public void testNonSquareMatrixThrowsException() {
        // Create a 2x3 non-square matrix
        RealMatrix nonSquare = org.apache.commons.math3.linear.MatrixUtils.createRealMatrix(2, 3);
        new Weight(nonSquare);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNullRealMatrixThrowsException() {
        new Weight((RealMatrix) null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNullDoubleArrayThrowsException() {
        new Weight((double[]) null);
    }

    /* ========== Partition E: Object Lifecycle & Contract Integrity ========== */

    @Test(timeout = 4000)
    public void testImmutabilityAfterGetWeight() {
        double[] weights = {10.0, 20.0};
        Weight weight = new Weight(weights);
        RealMatrix matrix = weight.getWeight();
        
        // Modify the returned copy
        matrix.setEntry(0, 0, 999.0);
        
        // Original should be unchanged
        RealMatrix newCopy = weight.getWeight();
        assertEquals(10.0, newCopy.getEntry(0, 0), 0.0);
        assertEquals(20.0, newCopy.getEntry(1, 1), 0.0);
    }

    @Test(timeout = 4000)
    public void testRealMatrixConstructorImmutability() {
        RealMatrix input = new DiagonalMatrix(new double[]{1.0, 2.0});
        Weight weight = new Weight(input);
        
        // Modify the original input
        input.setEntry(0, 0, 999.0);
        
        // Weight should have its own copy
        RealMatrix matrix = weight.getWeight();
        assertEquals(1.0, matrix.getEntry(0, 0), 0.0);
        assertEquals(2.0, matrix.getEntry(1, 1), 0.0);
    }

    @Test(timeout = 4000)
    public void testMultipleGetWeightCallsConsistency() {
        double[] weights = {5.0, 10.0, 15.0};
        Weight weight = new Weight(weights);
        
        RealMatrix first = weight.getWeight();
        RealMatrix second = weight.getWeight();
        RealMatrix third = weight.getWeight();
        
        // All copies should be equal
        assertEquals(first, second);
        assertEquals(second, third);
        assertEquals(first, third);
        
        // But different objects
        assertNotSame(first, second);
        assertNotSame(second, third);
    }
}