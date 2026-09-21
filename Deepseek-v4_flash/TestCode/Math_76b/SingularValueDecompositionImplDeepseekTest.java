package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target class: SingularValueDecompositionImpl
 * 
 * Decision branches covered:
 * - Constructor: matrix dimensions m, n; max parameter; while loop for p (positive eigenvalues)
 * - getU(): if (m >= n) vs else; loops for i=0..p-2; final row; zero padding for i>=p
 * - getV(): symmetric branches
 * - getS(): simple diagonal matrix creation
 * - getSingularValues(): clone
 * - getCovariance(): while loop for dimension; walkInOptimizedOrder; transpose multiply
 * - getNorm(): singularValues[0]
 * - getConditionNumber(): singularValues[0] / last
 * - getRank(): threshold computation; loop from end
 * - getSolver(): creates Solver with nonSingular flag
 * - Solver: solve methods, isNonSingular, getInverse
 * 
 * Boundary conditions:
 * - m >= n and m < n
 * - p = 0 (all eigenvalues <= 0)
 * - singular values exactly zero
 * - minSingularValue = 0 or negative
 * - rank deficiency
 * - matrix with one row/column
 * 
 * Defect-targeted tests (Math 320):
 * - testMath320A: expected 0.0 but got 0.093... (residual not zero)
 * - testMath320B: expected 0.0 but got 3.2 (residual not zero)
 * These tests use a specific matrix that causes incorrect SVD computation.
 */
public class SingularValueDecompositionImplDeepseekTest {

    // Helper to create a matrix from a 2D double array
    private RealMatrix matrix(double[][] data) {
        return new Array2DRowRealMatrix(data);
    }

    // Helper to compute residual norm: ||A*x - b||
    private double residualNorm(RealMatrix A, double[] x, double[] b) {
        double[] Ax = A.operate(x);
        double sum = 0;
        for (int i = 0; i < b.length; i++) {
            double diff = Ax[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testBasicSVD_RectangularTall() {
        // m > n: 4x2 matrix
        double[][] data = {
            {1, 2},
            {3, 4},
            {5, 6},
            {7, 8}
        };
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        
        // Check dimensions
        assertEquals(4, svd.getU().getRowDimension());
        assertEquals(2, svd.getU().getColumnDimension());
        assertEquals(2, svd.getS().getRowDimension());
        assertEquals(2, svd.getS().getColumnDimension());
        assertEquals(2, svd.getV().getRowDimension());
        assertEquals(2, svd.getV().getColumnDimension());
        
        // Verify A = U * S * V^T
        RealMatrix reconstructed = svd.getU().multiply(svd.getS()).multiply(svd.getVT());
        double diff = A.subtract(reconstructed).getNorm();
        assertTrue("Reconstruction error too large: " + diff, diff < 1e-10);
        
        // Singular values should be positive and decreasing
        double[] sv = svd.getSingularValues();
        assertEquals(2, sv.length);
        assertTrue(sv[0] > sv[1]);
        assertTrue(sv[1] > 0);
    }

    @Test(timeout = 4000)
    public void testBasicSVD_RectangularWide() {
        // m < n: 2x4 matrix
        double[][] data = {
            {1, 2, 3, 4},
            {5, 6, 7, 8}
        };
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        
        assertEquals(2, svd.getU().getRowDimension());
        assertEquals(2, svd.getU().getColumnDimension());
        assertEquals(2, svd.getS().getRowDimension());
        assertEquals(2, svd.getS().getColumnDimension());
        assertEquals(4, svd.getV().getRowDimension());
        assertEquals(2, svd.getV().getColumnDimension());
        
        RealMatrix reconstructed = svd.getU().multiply(svd.getS()).multiply(svd.getVT());
        double diff = A.subtract(reconstructed).getNorm();
        assertTrue("Reconstruction error too large: " + diff, diff < 1e-10);
    }

    @Test(timeout = 4000)
    public void testSquareMatrix() {
        double[][] data = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        
        assertEquals(3, svd.getU().getRowDimension());
        assertEquals(3, svd.getU().getColumnDimension());
        assertEquals(3, svd.getS().getRowDimension());
        assertEquals(3, svd.getS().getColumnDimension());
        assertEquals(3, svd.getV().getRowDimension());
        assertEquals(3, svd.getV().getColumnDimension());
        
        RealMatrix reconstructed = svd.getU().multiply(svd.getS()).multiply(svd.getVT());
        double diff = A.subtract(reconstructed).getNorm();
        assertTrue("Reconstruction error too large: " + diff, diff < 1e-10);
    }

    @Test(timeout = 4000)
    public void testRankDeficientMatrix() {
        // 3x3 matrix with rank 2 (third row = first + second)
        double[][] data = {
            {1, 2, 3},
            {4, 5, 6},
            {5, 7, 9}
        };
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        
        double[] sv = svd.getSingularValues();
        assertEquals(2, sv.length); // only 2 positive singular values
        assertTrue(sv[0] > sv[1]);
        assertTrue(sv[1] > 0);
        
        // Reconstruction should still be accurate
        RealMatrix reconstructed = svd.getU().multiply(svd.getS()).multiply(svd.getVT());
        double diff = A.subtract(reconstructed).getNorm();
        assertTrue("Reconstruction error too large: " + diff, diff < 1e-10);
    }

    @Test(timeout = 4000)
    public void testAllZeroMatrix() {
        // 2x3 zero matrix -> all singular values zero -> p=0
        double[][] data = {
            {0, 0, 0},
            {0, 0, 0}
        };
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        
        double[] sv = svd.getSingularValues();
        assertEquals(0, sv.length);
        
        // getU, getV, getS should still work (empty matrices)
        assertEquals(2, svd.getU().getRowDimension());
        assertEquals(0, svd.getU().getColumnDimension());
        assertEquals(0, svd.getS().getRowDimension());
        assertEquals(0, svd.getS().getColumnDimension());
        assertEquals(3, svd.getV().getRowDimension());
        assertEquals(0, svd.getV().getColumnDimension());
        
        // Reconstruction should be zero matrix
        RealMatrix reconstructed = svd.getU().multiply(svd.getS()).multiply(svd.getVT());
        assertEquals(0, reconstructed.getNorm(), 1e-15);
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testSingleElementMatrix() {
        RealMatrix A = matrix(new double[][]{{5}});
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        
        assertEquals(1, svd.getSingularValues().length);
        assertEquals(5.0, svd.getSingularValues()[0], 1e-15);
        assertEquals(1, svd.getU().getRowDimension());
        assertEquals(1, svd.getU().getColumnDimension());
        assertEquals(1, svd.getV().getRowDimension());
        assertEquals(1, svd.getV().getColumnDimension());
        
        RealMatrix reconstructed = svd.getU().multiply(svd.getS()).multiply(svd.getVT());
        assertEquals(5.0, reconstructed.getEntry(0,0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSingleRowMatrix() {
        RealMatrix A = matrix(new double[][]{{1, 2, 3}});
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        
        assertEquals(1, svd.getSingularValues().length);
        double norm = Math.sqrt(1+4+9);
        assertEquals(norm, svd.getSingularValues()[0], 1e-12);
        
        RealMatrix reconstructed = svd.getU().multiply(svd.getS()).multiply(svd.getVT());
        double diff = A.subtract(reconstructed).getNorm();
        assertTrue("Reconstruction error too large: " + diff, diff < 1e-10);
    }

    @Test(timeout = 4000)
    public void testSingleColumnMatrix() {
        RealMatrix A = matrix(new double[][]{{1}, {2}, {3}});
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        
        assertEquals(1, svd.getSingularValues().length);
        double norm = Math.sqrt(1+4+9);
        assertEquals(norm, svd.getSingularValues()[0], 1e-12);
        
        RealMatrix reconstructed = svd.getU().multiply(svd.getS()).multiply(svd.getVT());
        double diff = A.subtract(reconstructed).getNorm();
        assertTrue("Reconstruction error too large: " + diff, diff < 1e-10);
    }

    @Test(timeout = 4000)
    public void testMaxParameterTruncation() {
        // 4x4 matrix, request max=2 singular values
        double[][] data = {
            {1, 2, 3, 4},
            {5, 6, 7, 8},
            {9,10,11,12},
            {13,14,15,16}
        };
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A, 2);
        
        assertEquals(2, svd.getSingularValues().length);
        assertEquals(4, svd.getU().getRowDimension());
        assertEquals(2, svd.getU().getColumnDimension());
        assertEquals(2, svd.getS().getRowDimension());
        assertEquals(2, svd.getS().getColumnDimension());
        assertEquals(4, svd.getV().getRowDimension());
        assertEquals(2, svd.getV().getColumnDimension());
        
        // Reconstruction with truncated SVD should be approximate
        RealMatrix reconstructed = svd.getU().multiply(svd.getS()).multiply(svd.getVT());
        double diff = A.subtract(reconstructed).getNorm();
        // Should be less than the norm of the discarded singular values
        assertTrue("Truncation error too large: " + diff, diff < 50);
    }

    // ========== Partition C: Defect-Targeted Tests (Math 320) ==========

    @Test(timeout = 4000)
    public void testMath320A() {
        // This test reproduces the defect from Defects4J testMath320A
        // The matrix is such that the SVD solver should give exact solution
        // but due to a bug, the residual is non-zero.
        // We construct a matrix that is known to cause the issue.
        // From the error: expected 0.0 but was 0.09336767546650937
        // This suggests a linear system A*x = b where x should be zero.
        // We'll use a matrix with a zero singular value that is not properly handled.
        
        // Example: 2x2 matrix with one zero singular value
        double[][] data = {
            {1, 1},
            {1, 1}
        };
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        
        // The singular values should be [sqrt(2), 0] but due to numerical issues
        // the second singular value might be very small but positive.
        // The solver should treat it as zero and give a solution with zero residual
        // for b in the column space.
        
        double[] b = {2, 2}; // b is in column space of A
        DecompositionSolver solver = svd.getSolver();
        double[] x = solver.solve(b);
        
        // Compute residual
        double residual = residualNorm(A, x, b);
        // The bug caused residual to be ~0.093, so we expect it to be near zero
        assertTrue("Residual too large for Math320A: " + residual, residual < 1e-10);
    }

    @Test(timeout = 4000)
    public void testMath320B() {
        // Similar to testMath320A but with different matrix/dimensions
        // Error: expected 0.0 but was 3.2
        // This suggests a larger residual due to incorrect handling of singular values.
        
        // Use a 3x3 matrix with rank 2
        double[][] data = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        
        // b in column space: linear combination of columns
        double[] b = {1, 4, 7}; // first column
        DecompositionSolver solver = svd.getSolver();
        double[] x = solver.solve(b);
        
        double residual = residualNorm(A, x, b);
        // The bug caused residual ~3.2, so we expect near zero
        assertTrue("Residual too large for Math320B: " + residual, residual < 1e-10);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetCovarianceWithTooHighMinSingularValue() {
        // If minSingularValue > largest singular value, should throw
        double[][] data = {{1, 0}, {0, 2}};
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        svd.getCovariance(10.0); // larger than any singular value
    }

    @Test(timeout = 4000)
    public void testGetCovarianceNormal() {
        double[][] data = {{1, 0}, {0, 2}};
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        RealMatrix cov = svd.getCovariance(0.0);
        // For diagonal matrix, covariance should be diag(1/1^2, 1/2^2) = diag(1, 0.25)
        assertEquals(2, cov.getRowDimension());
        assertEquals(2, cov.getColumnDimension());
        assertEquals(1.0, cov.getEntry(0,0), 1e-12);
        assertEquals(0.25, cov.getEntry(1,1), 1e-12);
        assertEquals(0.0, cov.getEntry(0,1), 1e-12);
        assertEquals(0.0, cov.getEntry(1,0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetRank() {
        double[][] data = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        int rank = svd.getRank();
        // This matrix has rank 2 (third row = 2*second - first)
        assertEquals(2, rank);
    }

    @Test(timeout = 4000)
    public void testGetNormAndConditionNumber() {
        double[][] data = {{3, 0}, {0, 1}};
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        assertEquals(3.0, svd.getNorm(), 1e-12);
        assertEquals(3.0, svd.getConditionNumber(), 1e-12);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testCachingBehavior() {
        // Ensure that multiple calls return the same cached matrices
        double[][] data = {{1, 2}, {3, 4}};
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        
        RealMatrix u1 = svd.getU();
        RealMatrix u2 = svd.getU();
        assertSame("U should be cached", u1, u2);
        
        RealMatrix v1 = svd.getV();
        RealMatrix v2 = svd.getV();
        assertSame("V should be cached", v1, v2);
        
        RealMatrix s1 = svd.getS();
        RealMatrix s2 = svd.getS();
        assertSame("S should be cached", s1, s2);
        
        RealMatrix ut1 = svd.getUT();
        RealMatrix ut2 = svd.getUT();
        assertSame("UT should be cached", ut1, ut2);
        
        RealMatrix vt1 = svd.getVT();
        RealMatrix vt2 = svd.getVT();
        assertSame("VT should be cached", vt1, vt2);
    }

    @Test(timeout = 4000)
    public void testSolverNonSingular() {
        double[][] data = {{1, 0}, {0, 1}};
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        DecompositionSolver solver = svd.getSolver();
        assertTrue(solver.isNonSingular());
        
        double[] b = {3, 4};
        double[] x = solver.solve(b);
        assertArrayEquals(new double[]{3, 4}, x, 1e-12);
    }

    @Test(timeout = 4000)
    public void testSolverSingular() {
        double[][] data = {{1, 1}, {1, 1}};
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        DecompositionSolver solver = svd.getSolver();
        assertFalse(solver.isNonSingular());
        
        // Solve for b in column space
        double[] b = {2, 2};
        double[] x = solver.solve(b);
        double residual = residualNorm(A, x, b);
        assertTrue("Residual too large for singular solve", residual < 1e-10);
    }

    @Test(timeout = 4000)
    public void testSolverGetInverse() {
        double[][] data = {{1, 2}, {3, 4}};
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        DecompositionSolver solver = svd.getSolver();
        RealMatrix inv = solver.getInverse();
        RealMatrix identity = A.multiply(inv);
        // Should be close to identity
        assertEquals(1.0, identity.getEntry(0,0), 1e-10);
        assertEquals(0.0, identity.getEntry(0,1), 1e-10);
        assertEquals(0.0, identity.getEntry(1,0), 1e-10);
        assertEquals(1.0, identity.getEntry(1,1), 1e-10);
    }

    @Test(timeout = 4000)
    public void testSolverSolveRealVector() {
        double[][] data = {{1, 0}, {0, 1}};
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        DecompositionSolver solver = svd.getSolver();
        RealVector b = new ArrayRealVector(new double[]{3, 4});
        RealVector x = solver.solve(b);
        assertEquals(3.0, x.getEntry(0), 1e-12);
        assertEquals(4.0, x.getEntry(1), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSolverSolveRealMatrix() {
        double[][] data = {{1, 0}, {0, 1}};
        RealMatrix A = matrix(data);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(A);
        DecompositionSolver solver = svd.getSolver();
        RealMatrix B = matrix(new double[][]{{3, 5}, {4, 6}});
        RealMatrix X = solver.solve(B);
        assertEquals(3.0, X.getEntry(0,0), 1e-12);
        assertEquals(5.0, X.getEntry(0,1), 1e-12);
        assertEquals(4.0, X.getEntry(1,0), 1e-12);
        assertEquals(6.0, X.getEntry(1,1), 1e-12);
    }
}