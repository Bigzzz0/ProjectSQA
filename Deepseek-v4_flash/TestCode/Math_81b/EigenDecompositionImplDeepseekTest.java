package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for EigenDecompositionImpl targeting maximum coverage and the known defect MATH-308.
 * 
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic: symmetric matrix decomposition, eigenvalue retrieval, eigenvector computation, solver.
 * - Partition B: Boundary values: 1x1, 2x2, 3x3 blocks, zero off-diagonals, extreme split tolerance.
 * - Partition C: Defect-targeted: matrix with very small off-diagonals causing early deflation and negative index in computeShiftIncrement.
 * - Partition D: Exception paths: non-symmetric matrix, singular matrix, dimension mismatch in solver.
 * - Partition E: Object lifecycle: getV, getD, getVT caching, getDeterminant.
 */
public class EigenDecompositionImplDeepseekTest {

    // --- Partition A: Core Functional Logic ---

    @Test(timeout = 4000)
    public void testSymmetric3x3() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, 2, 3},
            {2, 5, 6},
            {3, 6, 9}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eigenvalues = ed.getRealEigenvalues();
        assertEquals(3, eigenvalues.length);
        // eigenvalues should be sorted descending
        assertTrue(eigenvalues[0] >= eigenvalues[1]);
        assertTrue(eigenvalues[1] >= eigenvalues[2]);
        // known eigenvalues for this matrix: approx 14.933, 0.067, 0
        assertEquals(14.933, eigenvalues[0], 0.001);
        assertEquals(0.067, eigenvalues[1], 0.001);
        assertEquals(0.0, eigenvalues[2], 0.001);
        // imaginary parts should be zero
        double[] imag = ed.getImagEigenvalues();
        for (double v : imag) {
            assertEquals(0.0, v, 0.0);
        }
        // eigenvectors should be orthogonal
        RealMatrix V = ed.getV();
        RealMatrix VT = ed.getVT();
        RealMatrix D = ed.getD();
        RealMatrix product = V.multiply(D).multiply(VT);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(matrix.getEntry(i, j), product.getEntry(i, j), 1e-10);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSymmetric4x4() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {4, 1, 0, 0},
            {1, 3, 1, 0},
            {0, 1, 2, 1},
            {0, 0, 1, 1}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eigenvalues = ed.getRealEigenvalues();
        assertEquals(4, eigenvalues.length);
        // check reconstruction
        RealMatrix V = ed.getV();
        RealMatrix D = ed.getD();
        RealMatrix VT = ed.getVT();
        RealMatrix product = V.multiply(D).multiply(VT);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(matrix.getEntry(i, j), product.getEntry(i, j), 1e-10);
            }
        }
    }

    @Test(timeout = 4000)
    public void testGetDeterminant() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {2, 0, 0},
            {0, 3, 0},
            {0, 0, 5}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(30.0, ed.getDeterminant(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSolver() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, 0, 0},
            {0, 2, 0},
            {0, 0, 3}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        DecompositionSolver solver = ed.getSolver();
        double[] b = {1, 1, 1};
        double[] x = solver.solve(b);
        assertArrayEquals(new double[]{1.0, 0.5, 1.0/3.0}, x, 1e-12);
        // test RealVector version
        RealVector bv = new ArrayRealVector(b);
        RealVector xv = solver.solve(bv);
        assertArrayEquals(x, xv.toArray(), 1e-12);
        // test RealMatrix version
        RealMatrix bm = MatrixUtils.createRealMatrix(new double[][]{{1},{1},{1}});
        RealMatrix xm = solver.solve(bm);
        assertEquals(1.0, xm.getEntry(0,0), 1e-12);
        assertEquals(0.5, xm.getEntry(1,0), 1e-12);
        assertEquals(1.0/3.0, xm.getEntry(2,0), 1e-12);
    }

    // --- Partition B: Boundary Value Analysis ---

    @Test(timeout = 4000)
    public void test1x1Block() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {{7}});
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(7.0, ed.getRealEigenvalue(0), 0.0);
        assertEquals(1, ed.getRealEigenvalues().length);
    }

    @Test(timeout = 4000)
    public void test2x2Block() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {2, 1},
            {1, 2}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eigenvalues = ed.getRealEigenvalues();
        assertEquals(2, eigenvalues.length);
        // eigenvalues: 3 and 1
        assertEquals(3.0, eigenvalues[0], 1e-12);
        assertEquals(1.0, eigenvalues[1], 1e-12);
    }

    @Test(timeout = 4000)
    public void test3x3Block() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, 0, 0},
            {0, 2, 0},
            {0, 0, 3}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eigenvalues = ed.getRealEigenvalues();
        assertArrayEquals(new double[]{3.0, 2.0, 1.0}, eigenvalues, 1e-12);
    }

    @Test(timeout = 4000)
    public void testZeroOffDiagonalSplit() {
        // matrix with zero off-diagonals -> splits into 1x1 blocks
        double[] main = {1, 2, 3};
        double[] secondary = {0, 0};
        EigenDecompositionImpl ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        double[] eigenvalues = ed.getRealEigenvalues();
        assertArrayEquals(new double[]{3.0, 2.0, 1.0}, eigenvalues, 1e-12);
    }

    @Test(timeout = 4000)
    public void testExtremeSplitTolerance() {
        // large split tolerance causes many splits
        double[] main = {1, 2, 3, 4};
        double[] secondary = {1e-10, 1e-10, 1e-10};
        EigenDecompositionImpl ed = new EigenDecompositionImpl(main, secondary, 1e-5);
        double[] eigenvalues = ed.getRealEigenvalues();
        // eigenvalues should be close to diagonal values
        assertEquals(4.0, eigenvalues[0], 1e-5);
        assertEquals(3.0, eigenvalues[1], 1e-5);
        assertEquals(2.0, eigenvalues[2], 1e-5);
        assertEquals(1.0, eigenvalues[3], 1e-5);
    }

    // --- Partition C: Defect-Targeted (MATH-308) ---

    @Test(timeout = 4000)
    public void testMath308Defect() {
        // This matrix with very small off-diagonals can cause early deflation
        // leading to negative index in computeShiftIncrement (ArrayIndexOutOfBoundsException: -1)
        double[] main = {1, 2, 3, 4};
        double[] secondary = {1e-12, 1e-12, 1e-12};
        try {
            EigenDecompositionImpl ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
            double[] eigenvalues = ed.getRealEigenvalues();
            // If we reach here, the bug is not present (fixed version)
            assertNotNull(eigenvalues);
            assertEquals(4, eigenvalues.length);
        } catch (ArrayIndexOutOfBoundsException e) {
            // On defective version, this exception is thrown -> test fails
            fail("Defect MATH-308 triggered: ArrayIndexOutOfBoundsException: " + e.getMessage());
        }
    }

    // --- Partition D: Exception & Defensive Guard Paths ---

    @Test(timeout = 4000, expected = InvalidMatrixException.class)
    public void testNonSymmetricMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, 2},
            {3, 4}
        });
        new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
    }

    @Test(timeout = 4000, expected = SingularMatrixException.class)
    public void testSingularMatrixSolver() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, 0, 0},
            {0, 0, 0},
            {0, 0, 1}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        DecompositionSolver solver = ed.getSolver();
        assertFalse(solver.isNonSingular());
        solver.solve(new double[]{1, 1, 1}); // should throw
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSolverDimensionMismatch() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, 0},
            {0, 1}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        DecompositionSolver solver = ed.getSolver();
        solver.solve(new double[]{1, 2, 3}); // wrong length
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testGetEigenvalueOutOfRange() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {{1}});
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        ed.getRealEigenvalue(5); // index out of bounds
    }

    // --- Partition E: Object Lifecycle & Contract ---

    @Test(timeout = 4000)
    public void testCachedMatrices() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {2, 1},
            {1, 2}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        // getV, getD, getVT should return same instances on repeated calls
        RealMatrix v1 = ed.getV();
        RealMatrix v2 = ed.getV();
        assertSame(v1, v2);
        RealMatrix d1 = ed.getD();
        RealMatrix d2 = ed.getD();
        assertSame(d1, d2);
        RealMatrix vt1 = ed.getVT();
        RealMatrix vt2 = ed.getVT();
        assertSame(vt1, vt2);
    }

    @Test(timeout = 4000)
    public void testEigenvectorCopy() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, 0},
            {0, 2}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        RealVector ev = ed.getEigenvector(0);
        // eigenvector for eigenvalue 2 should be [0,1] or [0,-1]
        assertEquals(0.0, ev.getEntry(0), 1e-12);
        assertEquals(1.0, Math.abs(ev.getEntry(1)), 1e-12);
        // getEigenvector should return a copy
        RealVector ev2 = ed.getEigenvector(0);
        assertNotSame(ev, ev2);
    }

    @Test(timeout = 4000)
    public void testGetImagEigenvalues() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {{5}});
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] imag = ed.getImagEigenvalues();
        assertEquals(1, imag.length);
        assertEquals(0.0, imag[0], 0.0);
        assertEquals(0.0, ed.getImagEigenvalue(0), 0.0);
    }

    @Test(timeout = 4000)
    public void testSolverInverse() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {2, 1},
            {1, 2}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        DecompositionSolver solver = ed.getSolver();
        RealMatrix inverse = solver.getInverse();
        RealMatrix identity = matrix.multiply(inverse);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                double expected = (i == j) ? 1.0 : 0.0;
                assertEquals(expected, identity.getEntry(i, j), 1e-12);
            }
        }
    }
}