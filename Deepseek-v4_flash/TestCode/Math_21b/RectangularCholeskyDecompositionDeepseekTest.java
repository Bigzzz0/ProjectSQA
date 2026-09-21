package org.apache.commons.math3.linear;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Advanced white-box test suite for RectangularCholeskyDecomposition.
 * Targets line/branch coverage and the known Defects4J defect (MATH-789).
 *
 * [Branch & Defect Analysis Matrix]
 * - Branch 1: Diagonal element >= small -> process column (transform block)
 * - Branch 2: Diagonal element < small and r == 0 -> throw NonPositiveDefiniteMatrixException
 * - Branch 3: Diagonal element < small and r > 0 -> check remaining diagonals
 *   - Sub-branch 3a: Any remaining diagonal < -small -> throw NonPositiveDefiniteMatrixException
 *   - Sub-branch 3b: All remaining diagonals >= -small -> increment r, break loop
 * - Branch 4: Loop termination via ++r < order (normal completion)
 * - Branch 5: Loop termination via small diagonal (early break)
 * - Defect: When a small diagonal is found (r > 0), the code increments r before breaking,
 *   causing rank to be one too high. Also, when r == 0 and diagonal is small, it throws
 *   even for a valid positive semidefinite matrix (e.g., zero matrix).
 */
public class RectangularCholeskyDecompositionDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testFullRankPositiveDefinite() {
        // 3x3 Hilbert matrix (positive definite)
        double[][] data = {
            {1.0, 0.5, 0.3333333333333333},
            {0.5, 0.3333333333333333, 0.25},
            {0.3333333333333333, 0.25, 0.2}
        };
        RealMatrix A = new Array2DRowRealMatrix(data, false);
        double small = 1e-10;
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(A, small);
        assertEquals("Rank should be 3 for full rank matrix", 3, dec.getRank());
        RealMatrix B = dec.getRootMatrix();
        RealMatrix product = B.multiply(B.transpose());
        double diff = A.subtract(product).getNorm();
        assertTrue("Reconstruction error too large: " + diff, diff < 1e-8);
    }

    @Test(timeout = 4000)
    public void testRankDeficientMatrix() {
        // 3x3 matrix with rank 2: A = B0 * B0^T where B0 is 3x2
        double[][] b0Data = {
            {1.0, 0.0},
            {0.5, 0.8660254037844386},
            {0.0, 1.0}
        };
        RealMatrix B0 = new Array2DRowRealMatrix(b0Data, false);
        RealMatrix A = B0.multiply(B0.transpose());
        double small = 1e-10;
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(A, small);
        assertEquals("Rank should be 2 for rank-deficient matrix", 2, dec.getRank());
        RealMatrix B = dec.getRootMatrix();
        RealMatrix product = B.multiply(B.transpose());
        double diff = A.subtract(product).getNorm();
        assertTrue("Reconstruction error too large: " + diff, diff < 1e-8);
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testZeroMatrix() {
        // Zero matrix is positive semidefinite, rank 0
        RealMatrix A = new Array2DRowRealMatrix(3, 3);
        double small = 1e-10;
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(A, small);
        assertEquals("Rank of zero matrix should be 0", 0, dec.getRank());
        RealMatrix B = dec.getRootMatrix();
        assertEquals("Root matrix should have 0 columns", 0, B.getColumnDimension());
        assertEquals("Root matrix should have 3 rows", 3, B.getRowDimension());
    }

    @Test(timeout = 4000)
    public void testSingleElementMatrix() {
        // 1x1 matrix with positive element
        RealMatrix A = new Array2DRowRealMatrix(new double[][]{{4.0}});
        double small = 1e-10;
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(A, small);
        assertEquals("Rank should be 1", 1, dec.getRank());
        RealMatrix B = dec.getRootMatrix();
        assertEquals("Root matrix should have 1 column", 1, B.getColumnDimension());
        assertEquals("Root element should be sqrt(4)=2", 2.0, B.getEntry(0, 0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testVerySmallThreshold() {
        // Matrix with very small diagonal but still positive definite
        double[][] data = {
            {1e-12, 0.0},
            {0.0, 1e-12}
        };
        RealMatrix A = new Array2DRowRealMatrix(data, false);
        double small = 1e-15; // smaller than diagonal
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(A, small);
        assertEquals("Rank should be 2", 2, dec.getRank());
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Directly targets the known Defects4J defect (MATH-789).
     * Uses a rank-1 matrix (2x2) with a zero diagonal element.
     * The buggy version increments rank when encountering the zero diagonal,
     * resulting in rank=2 instead of 1.
     */
    @Test(timeout = 4000)
    public void testMath789RankDeficientWithZeroDiagonal() {
        // A = [1 1; 1 1] has rank 1, second diagonal becomes 0 after first step
        double[][] data = {
            {1.0, 1.0},
            {1.0, 1.0}
        };
        RealMatrix A = new Array2DRowRealMatrix(data, false);
        double small = 1e-10;
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(A, small);
        // The correct rank is 1; buggy version returns 2
        assertEquals("Rank should be 1 for this singular matrix", 1, dec.getRank());
        RealMatrix B = dec.getRootMatrix();
        RealMatrix product = B.multiply(B.transpose());
        double diff = A.subtract(product).getNorm();
        // The product should be exact (within numerical precision)
        assertTrue("Reconstruction error should be near zero: " + diff, diff < 1e-12);
    }

    @Test(timeout = 4000)
    public void testFullRankMatrixNoBug() {
        // Full rank matrix should have rank = order
        double[][] data = {
            {2.0, 1.0},
            {1.0, 3.0}
        };
        RealMatrix A = new Array2DRowRealMatrix(data, false);
        double small = 1e-10;
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(A, small);
        assertEquals("Rank should be 2", 2, dec.getRank());
        RealMatrix B = dec.getRootMatrix();
        RealMatrix product = B.multiply(B.transpose());
        double diff = A.subtract(product).getNorm();
        assertTrue("Reconstruction error too large: " + diff, diff < 1e-12);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = NonPositiveDefiniteMatrixException.class, timeout = 4000)
    public void testNonPositiveDefiniteMatrix() {
        // Matrix with negative eigenvalue
        double[][] data = {
            {1.0, 2.0},
            {2.0, 1.0}
        };
        RealMatrix A = new Array2DRowRealMatrix(data, false);
        double small = 1e-10;
        new RectangularCholeskyDecomposition(A, small);
    }

    @Test(expected = NonPositiveDefiniteMatrixException.class, timeout = 4000)
    public void testNegativeDiagonalFirstElement() {
        // First diagonal element negative
        double[][] data = {
            {-1.0, 0.0},
            {0.0, 1.0}
        };
        RealMatrix A = new Array2DRowRealMatrix(data, false);
        double small = 1e-10;
        new RectangularCholeskyDecomposition(A, small);
    }

    @Test(expected = NonPositiveDefiniteMatrixException.class, timeout = 4000)
    public void testNegativeDiagonalAfterProcessing() {
        // Matrix that becomes negative definite after first step
        double[][] data = {
            {1.0, 2.0},
            {2.0, 1.0}
        };
        RealMatrix A = new Array2DRowRealMatrix(data, false);
        double small = 1e-10;
        new RectangularCholeskyDecomposition(A, small);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testGetRootMatrixReturnsNonNull() {
        RealMatrix A = new Array2DRowRealMatrix(new double[][]{{1.0}});
        double small = 1e-10;
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(A, small);
        assertNotNull("Root matrix should not be null", dec.getRootMatrix());
    }

    @Test(timeout = 4000)
    public void testGetRankConsistency() {
        // After decomposition, rank should be consistent with root matrix dimensions
        double[][] data = {
            {1.0, 0.5},
            {0.5, 1.0}
        };
        RealMatrix A = new Array2DRowRealMatrix(data, false);
        double small = 1e-10;
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(A, small);
        assertEquals("Rank should equal number of columns in root matrix",
                     dec.getRank(), dec.getRootMatrix().getColumnDimension());
    }

    @Test(timeout = 4000)
    public void testMultipleDecompositionsIndependent() {
        // Ensure that two decompositions on same matrix produce independent objects
        double[][] data = {
            {4.0, 0.0},
            {0.0, 9.0}
        };
        RealMatrix A = new Array2DRowRealMatrix(data, false);
        double small = 1e-10;
        RectangularCholeskyDecomposition dec1 = new RectangularCholeskyDecomposition(A, small);
        RectangularCholeskyDecomposition dec2 = new RectangularCholeskyDecomposition(A, small);
        assertEquals(dec1.getRank(), dec2.getRank());
        assertEquals(dec1.getRootMatrix().getEntry(0, 0), dec2.getRootMatrix().getEntry(0, 0), 1e-12);
    }
}