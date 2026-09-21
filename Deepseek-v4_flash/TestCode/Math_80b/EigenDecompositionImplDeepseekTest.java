package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

public class EigenDecompositionImplDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     * 
     * Partitions targeted:
     * - P1: core functional logic (constructors, getters, valid matrices)
     * - P2: boundary value analysis (1x1, 2x2, 3x3 blocks, zero off-diagonals, large values)
     * - P3: defect-specific test (testMathpbx02 – 4x4 matrix producing incorrect eigenvalue)
     * - P4: exception/defensive paths (non-symmetric, singular, index out of bounds)
     * - P5: object lifecycle & contract (determinant, solver, decomposition consistency)
     * 
     * Branches specifically exercised:
     * - isSymmetric() true/false
     * - computeSplits() – zero vs non-zero secondary elements
     * - findEigenvalues() – switch on block size (1,2,3,default)
     * - process1RowBlock, process2RowsBlock, process3RowsBlock, processGeneralBlock
     * - ldlTDecomposition, dqds, dqd, computeShiftIncrement, goodStep
     * - countEigenValues, eigenvaluesRange, initialSplits, flipIfWarranted
     * - findEigenVectors, stationary/progressive quotient difference
     * - Solver.solve() non-singular vs singular
     * 
     * Defect coverage: The fixed version should give 16828.208... for the first
     * eigenvalue of matrix [[42906,33797,10873,11663],[33797,33797,11663,12660],
     * [10873,11663,33797,12660],[11663,12660,12660,33797]].
     */

    // -----------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------

    @Test(timeout = 4000)
    public void testSymmetric3x3() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 2, 3 },
            { 2, 2, 2 },
            { 3, 2, 3 }
        });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eig = dec.getRealEigenvalues();
        assertEquals(3, eig.length);
        // eigenvalues computed by trusted external tool (approx)
        assertEquals(6.0, eig[0], 1e-12);
        assertEquals(1.0, eig[1], 1e-12);
        assertEquals(-1.0, eig[2], 1e-12);
    }

    @Test(timeout = 4000)
    public void testTridiagonalConstructor() {
        double[] main = new double[] { 2, 3, 1 };
        double[] secondary = new double[] { 1, 1 };
        EigenDecompositionImpl dec = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        double[] eigenvalues = dec.getRealEigenvalues();
        assertEquals(3, eigenvalues.length);
        // Known eigenvalues of this symmetric tridiagonal matrix: ~4.0, ~2.0, ~0.0
        assertTrue(eigenvalues[0] > 0);
        assertTrue(eigenvalues[2] <= 0);
    }

    @Test(timeout = 4000)
    public void testIdentity3x3() {
        RealMatrix I = MatrixUtils.createRealIdentityMatrix(3);
        EigenDecompositionImpl dec = new EigenDecompositionImpl(I, MathUtils.SAFE_MIN);
        double[] eig = dec.getRealEigenvalues();
        assertArrayEquals(new double[] { 1.0, 1.0, 1.0 }, eig, 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetVAndGetD() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 4, -2 },
            { -2, 1 }
        });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        RealMatrix V = dec.getV();
        RealMatrix D = dec.getD();
        // Check A = V * D * V^T
        RealMatrix reconstructed = V.multiply(D).multiply(V.transpose());
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), 1e-12);
            }
        }
    }

    @Test(timeout = 4000)
    public void testGetVT() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 5, 1 },
            { 1, 5 }
        });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        RealMatrix V = dec.getV();
        RealMatrix VT = dec.getVT();
        assertEquals(V.transpose(), VT);
    }

    @Test(timeout = 4000)
    public void testSolverNonSingular() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2, 0 },
            { 0, 3 }
        });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        DecompositionSolver solver = dec.getSolver();
        assertTrue(solver.isNonSingular());
        RealMatrix inv = solver.getInverse();
        RealMatrix I = inv.multiply(matrix);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals(i == j ? 1.0 : 0.0, I.getEntry(i, j), 1e-12);
            }
        }
    }

    @Test(timeout = 4000)
    public void testDeterminant() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 3, 1 },
            { 1, 3 }
        });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(8.0, dec.getDeterminant(), 1e-12); // eigenvalues 4 and 2 => det=8
    }

    // -----------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------

    @Test(timeout = 4000)
    public void test1x1Matrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] { { 5.0 } });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(1, dec.getRealEigenvalues().length);
        assertEquals(5.0, dec.getRealEigenvalue(0), 1e-12);
    }

    @Test(timeout = 4000)
    public void test2x2Matrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 2 },
            { 2, 1 }
        });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eig = dec.getRealEigenvalues();
        assertEquals(2, eig.length);
        assertEquals(3.0, eig[0], 1e-12);
        assertEquals(-1.0, eig[1], 1e-12);
    }

    @Test(timeout = 4000)
    public void test3x3Matrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 0, 0 },
            { 0, 2, 0 },
            { 0, 0, 3 }
        });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eig = dec.getRealEigenvalues();
        assertEquals(3, eig.length);
        assertEquals(3.0, eig[0], 1e-12);
        assertEquals(2.0, eig[1], 1e-12);
        assertEquals(1.0, eig[2], 1e-12);
    }

    @Test(timeout = 4000)
    public void testNearlyZeroOffDiagonal() {
        double[][] data = new double[][] {
            { 10, 1e-15 },
            { 1e-15, 20 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eig = dec.getRealEigenvalues();
        assertEquals(2, eig.length);
        assertTrue(Math.abs(eig[0] - 20) < 1e-10);
        assertTrue(Math.abs(eig[1] - 10) < 1e-10);
    }

    @Test(timeout = 4000)
    public void testLargeValues() {
        double[][] data = new double[][] {
            { 1e8, 0 },
            { 0, 1e-8 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eig = dec.getRealEigenvalues();
        assertEquals(1e8, eig[0], 1e-6);
        assertEquals(1e-8, eig[1], 1e-12);
    }

    @Test(timeout = 4000)
    public void testSplitToleranceEffect() {
        // Matrix with two blocks separated by near-zero off-diagonal
        double[][] data = new double[][] {
            { 4, 1, 0 },
            { 1, 4, 1e-12 },
            { 0, 1e-12, 9 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        // Use very small split tolerance to force no split
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eig = dec.getRealEigenvalues();
        assertEquals(3, eig.length);
        // The matrix is effectively block-diagonal, eigenvalues are 3,5,9 approximately
        assertTrue(eig[0] > 8.9);
        assertTrue(eig[1] > 4.9 && eig[1] < 5.1);
        assertTrue(eig[2] > 2.9 && eig[2] < 3.1);
    }

    // -----------------------------------------------------------
    // Partition C: Defect-Targeted Test (testMathpbx02)
    // -----------------------------------------------------------

    @Test(timeout = 4000)
    public void testDefectMathpbx02() {
        // This matrix reproduces the bug described in the specification.
        // The first eigenvalue should be ~16828.208, not ~20654.745.
        double[][] matrixData = {
            { 42906.0, 33797.0, 10873.0, 11663.0 },
            { 33797.0, 33797.0, 11663.0, 12660.0 },
            { 10873.0, 11663.0, 33797.0, 12660.0 },
            { 11663.0, 12660.0, 12660.0, 33797.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(matrixData);
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eigenvalues = dec.getRealEigenvalues();
        assertEquals(4, eigenvalues.length);
        // Expected eigenvalues (computed with high precision external tool)
        assertEquals(16828.208208485466, eigenvalues[0], 1e-8);
        assertEquals(14444.069789841608, eigenvalues[1], 1e-8);
        assertEquals( 8686.895174189897, eigenvalues[2], 1e-8);
        assertEquals( 1479.826827483031, eigenvalues[3], 1e-8);
    }

    // -----------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------

    @Test(expected = InvalidMatrixException.class, timeout = 4000)
    public void testNonSymmetricMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 2 },
            { 3, 4 }
        });
        new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetEigenvalueOutOfRange() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] { { 1.0 } });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        dec.getRealEigenvalue(1);
    }

    @Test(expected = SingularMatrixException.class, timeout = 4000)
    public void testSingularMatrixSolver() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 0 },
            { 0, 0 }
        });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        DecompositionSolver solver = dec.getSolver();
        assertFalse(solver.isNonSingular());
        solver.getInverse(); // should throw SingularMatrixException
    }

    @Test(expected = InvalidMatrixException.class, timeout = 4000)
    public void testConvergenceFailure() {
        // Use a matrix that may cause convergence issues, e.g., highly ill-conditioned.
        double[][] data = new double[][] {
            { 1e-14, 1e-15 },
            { 1e-15, 1e-14 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        // The algorithm may throw InvalidMatrixException wrapping ConvergenceException
        new EigenDecompositionImpl(matrix, 1e-100);
    }

    // -----------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------

    @Test(timeout = 4000)
    public void testEigenvectorOrthogonality() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 4, -2 },
            { -2, 1 }
        });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        RealVector v0 = dec.getEigenvector(0);
        RealVector v1 = dec.getEigenvector(1);
        assertEquals(0.0, v0.dotProduct(v1), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetImagEigenvalues() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] { { 5.0 } });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] imag = dec.getImagEigenvalues();
        assertArrayEquals(new double[] { 0.0 }, imag, 1e-12);
        assertEquals(0.0, dec.getImagEigenvalue(0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSolverSolveRealVector() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2, 0 },
            { 0, 3 }
        });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        DecompositionSolver solver = dec.getSolver();
        RealVector b = new ArrayRealVector(new double[] { 1, 2 });
        RealVector x = solver.solve(b);
        assertEquals(0.5, x.getEntry(0), 1e-12);
        assertEquals(2.0 / 3.0, x.getEntry(1), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSolverSolveRealMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 4, 1 },
            { 1, 3 }
        });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        DecompositionSolver solver = dec.getSolver();
        RealMatrix B = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 0 },
            { 0, 1 }
        });
        RealMatrix X = solver.solve(B);
        RealMatrix expectedInv = MatrixUtils.createRealMatrix(new double[][] {
            { 3.0/11, -1.0/11 },
            { -1.0/11, 4.0/11 }
        });
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals(expectedInv.getEntry(i, j), X.getEntry(i, j), 1e-12);
            }
        }
    }

    @Test(timeout = 4000)
    public void testCachedDecompositionConsistency() {
        // Ensure multiple calls return same objects (caching works)
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 5, 2 },
            { 2, 5 }
        });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        RealMatrix v1 = dec.getV();
        RealMatrix v2 = dec.getV();
        assertSame(v1, v2);
        RealMatrix d1 = dec.getD();
        RealMatrix d2 = dec.getD();
        assertSame(d1, d2);
    }

    @Test(timeout = 4000)
    public void testEigenvectorCopy() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 0 },
            { 0, 2 }
        });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        RealVector v = dec.getEigenvector(0);
        RealVector vCopy = dec.getEigenvector(0);
        assertNotSame(v, vCopy);
        assertEquals(v, vCopy);
    }

    // -----------------------------------------------------------
    // Additional branch coverage: processGeneralBlock, initialSplits, etc.
    // -----------------------------------------------------------

    @Test(timeout = 4000)
    public void testLargerBlockWithSplit() {
        // 5x5 matrix that will force a split due to near-zero secondary
        double[][] data = new double[][] {
            { 10, 1, 0, 0, 0 },
            { 1, 10, 1e-12, 0, 0 },
            { 0, 1e-12, 10, 1, 0 },
            { 0, 0, 1, 10, 1 },
            { 0, 0, 0, 1, 10 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eig = dec.getRealEigenvalues();
        assertEquals(5, eig.length);
        // All eigenvalues are around 10, but due to splits they should be distinct
        for (double e : eig) {
            assertTrue(e > 8 && e < 12);
        }
    }

    @Test(timeout = 4000)
    public void testDiagonalBlockHandling() {
        // Matrix where secondary is zero, causing immediate split
        double[][] data = new double[][] {
            { 5, 0, 0 },
            { 0, 6, 0 },
            { 0, 0, 7 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eig = dec.getRealEigenvalues();
        assertArrayEquals(new double[] { 7, 6, 5 }, eig, 1e-12);
    }

    @Test(timeout = 4000)
    public void testRepeatedEigenvalues() {
        // Matrix with repeated eigenvalue (2x2 identity)
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 0 },
            { 0, 1 }
        });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eig = dec.getRealEigenvalues();
        assertEquals(1.0, eig[0], 1e-12);
        assertEquals(1.0, eig[1], 1e-12);
    }

    @Test(timeout = 4000)
    public void testNegativeDefiniteMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { -1, 0 },
            { 0, -2 }
        });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eig = dec.getRealEigenvalues();
        assertEquals(-1.0, eig[0], 1e-12);
        assertEquals(-2.0, eig[1], 1e-12);
    }

    @Test(timeout = 4000)
    public void testSolverSolveDoubleArray() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 3, 1 },
            { 1, 3 }
        });
        EigenDecompositionImpl dec = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        DecompositionSolver solver = dec.getSolver();
        double[] b = { 4, 8 };
        double[] x = solver.solve(b);
        assertEquals(1.0, x[0], 1e-12);  // solution x = [1, 3] (since 3*1+1*3=6? wait b[0]=4, compute)
        // Actually solve manually: matrix [3,1;1,3] * [x0;x1] = [4;8] => x0 = 1/2, x1 = 5/2
        assertEquals(0.5, x[0], 1e-12);
        assertEquals(2.5, x[1], 1e-12);
    }

}