/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math.linear;

import org.apache.commons.math.util.MathUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: EigenDecompositionImpl
 *
 * Defect Zone Covered:
 * 1. Defects4J Math-81 / MATH-308:
 *    - In computeShiftIncrement (case 0, dMin == dN2, case 5 shift):
 *      When end == 2 and pingPong == 0, np = nn = 7, accessing work[np - 8] evaluates to work[-1],
 *      throwing ArrayIndexOutOfBoundsException: -1.
 *    - Targeted by testMath308 and testDefectMath308Direct.
 *
 * Equivalence Partitions & Branch Paths Covered:
 * - Constructors:
 *   * RealMatrix constructor with symmetric matrix (1x1, 2x2, 3x3, 4x4, 5x5).
 *   * RealMatrix constructor with asymmetric matrix -> InvalidMatrixException.
 *   * Tridiagonal arrays constructor (main, secondary, splitTolerance).
 * - Dimension branches in findEigenvalues:
 *   * n = 1: process1RowBlock
 *   * n = 2: process2RowsBlock (delta >= 0, roots computed)
 *   * n = 3: process3RowsBlock (cubic roots calculation, distinct real roots)
 *   * n >= 4: default general dqd/dqds block (LDL^T, Li's initial splits, goodStep, dqds, dqd)
 * - Getters and Decomposition properties:
 *   * getV(), getVT(), getD() (verifying A * V = V * D and V * VT = I).
 *   * getRealEigenvalues(), getRealEigenvalue(i), getImagEigenvalues(), getImagEigenvalue(i).
 *   * getEigenvector(i), getDeterminant().
 * - Specialized Solver operations:
 *   * solve(double[]), solve(RealVector), solve(RealMatrix).
 *   * Dimension mismatch exceptions for solver operations.
 *   * SingularMatrixException on singular / non-invertible matrix (eigenvalue == 0).
 *   * getInverse() for regular matrices.
 * - Boundary Values & Edge Cases:
 *   * Identity matrices, diagonal matrices (secondary all zero).
 *   * Matrix with zero eigenvalues, negative eigenvalues, multiple identical eigenvalues.
 *   * Splitting behavior when secondary elements are below splitTolerance.
 */
public class EigenDecompositionImplGeminiTest {

    private static final double EPSILON = 1e-10;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions (Normal Operations)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDimension1Matrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 42.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);

        assertEquals(42.0, ed.getRealEigenvalue(0), EPSILON);
        assertEquals(0.0, ed.getImagEigenvalue(0), EPSILON);
        assertEquals(42.0, ed.getDeterminant(), EPSILON);

        RealMatrix v = ed.getV();
        assertEquals(1, v.getRowDimension());
        assertEquals(1, v.getColumnDimension());
        assertEquals(1.0, Math.abs(v.getEntry(0, 0)), EPSILON);

        RealMatrix vt = ed.getVT();
        assertEquals(1.0, Math.abs(vt.getEntry(0, 0)), EPSILON);

        RealMatrix d = ed.getD();
        assertEquals(42.0, d.getEntry(0, 0), EPSILON);

        RealVector eigenVector = ed.getEigenvector(0);
        assertEquals(1.0, Math.abs(eigenVector.getEntry(0)), EPSILON);

        // Verification: A = V * D * V^T
        RealMatrix reconstructed = v.multiply(d).multiply(vt);
        assertEquals(42.0, reconstructed.getEntry(0, 0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testDimension2Matrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);

        double[] eigenvalues = ed.getRealEigenvalues();
        assertEquals(2, eigenvalues.length);
        assertEquals(3.0, eigenvalues[0], EPSILON);
        assertEquals(1.0, eigenvalues[1], EPSILON);
        assertEquals(3.0, ed.getDeterminant(), EPSILON);

        RealMatrix v = ed.getV();
        RealMatrix vt = ed.getVT();
        RealMatrix d = ed.getD();

        RealMatrix reconstructed = v.multiply(d).multiply(vt);
        checkMatrixEquals(matrix, reconstructed, EPSILON);

        // Orthogonality: V * V^T = I
        checkMatrixEquals(MatrixUtils.createRealIdentityMatrix(2), v.multiply(vt), EPSILON);
    }

    @Test(timeout = 4000)
    public void testDimension3Matrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 6.0, 2.0, 1.0 },
            { 2.0, 3.0, 1.0 },
            { 1.0, 1.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);

        double[] eigenvalues = ed.getRealEigenvalues();
        assertEquals(3, eigenvalues.length);
        assertTrue(eigenvalues[0] >= eigenvalues[1]);
        assertTrue(eigenvalues[1] >= eigenvalues[2]);

        double det = ed.getDeterminant();
        assertEquals(eigenvalues[0] * eigenvalues[1] * eigenvalues[2], det, 1e-8);

        RealMatrix v = ed.getV();
        RealMatrix vt = ed.getVT();
        RealMatrix d = ed.getD();

        RealMatrix reconstructed = v.multiply(d).multiply(vt);
        checkMatrixEquals(matrix, reconstructed, EPSILON);
    }

    @Test(timeout = 4000)
    public void testDimension4GeneralBlock() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0, 1.0, -2.0, 2.0 },
            { 1.0, 2.0, 0.0, 1.0 },
            { -2.0, 0.0, 3.0, -2.0 },
            { 2.0, 1.0, -2.0, -1.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);

        double[] realValues = ed.getRealEigenvalues();
        double[] imagValues = ed.getImagEigenvalues();
        assertEquals(4, realValues.length);
        assertEquals(4, imagValues.length);
        for (double imag : imagValues) {
            assertEquals(0.0, imag, EPSILON);
        }

        RealMatrix v = ed.getV();
        RealMatrix vt = ed.getVT();
        RealMatrix d = ed.getD();

        RealMatrix reconstructed = v.multiply(d).multiply(vt);
        checkMatrixEquals(matrix, reconstructed, EPSILON);
    }

    @Test(timeout = 4000)
    public void testDimension5MatrixDecomposition() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 10.0, 1.0, 2.0, 3.0, 4.0 },
            { 1.0, 9.0, -1.0, 2.0, -3.0 },
            { 2.0, -1.0, 8.0, 0.0, 1.0 },
            { 3.0, 2.0, 0.0, 7.0, -2.0 },
            { 4.0, -3.0, 1.0, -2.0, 6.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);

        RealMatrix v = ed.getV();
        RealMatrix vt = ed.getVT();
        RealMatrix d = ed.getD();

        RealMatrix reconstructed = v.multiply(d).multiply(vt);
        checkMatrixEquals(matrix, reconstructed, 1e-8);
    }

    @Test(timeout = 4000)
    public void testTridiagonalDirectConstructor() {
        double[] main = new double[] { 4.0, 3.0, 2.0, 1.0 };
        double[] secondary = new double[] { 1.0, 1.0, 1.0 };

        EigenDecomposition ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        double[] realEigenvalues = ed.getRealEigenvalues();
        assertEquals(4, realEigenvalues.length);

        for (int i = 0; i < realEigenvalues.length - 1; ++i) {
            assertTrue(realEigenvalues[i] >= realEigenvalues[i + 1]);
        }

        RealMatrix v = ed.getV();
        RealMatrix vt = ed.getVT();
        RealMatrix d = ed.getD();

        RealMatrix reconstructed = v.multiply(d).multiply(vt);
        // Verify tridiagonal structure matches original
        assertEquals(4.0, reconstructed.getEntry(0, 0), 1e-8);
        assertEquals(1.0, reconstructed.getEntry(0, 1), 1e-8);
        assertEquals(0.0, reconstructed.getEntry(0, 2), 1e-8);
        assertEquals(3.0, reconstructed.getEntry(1, 1), 1e-8);
        assertEquals(1.0, reconstructed.getEntry(1, 2), 1e-8);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIdentityMatrix() {
        RealMatrix identity = MatrixUtils.createRealIdentityMatrix(4);
        EigenDecomposition ed = new EigenDecompositionImpl(identity, MathUtils.SAFE_MIN);

        double[] ev = ed.getRealEigenvalues();
        for (double lambda : ev) {
            assertEquals(1.0, lambda, EPSILON);
        }
        assertEquals(1.0, ed.getDeterminant(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testDiagonalMatrixAlreadySplit() {
        // All secondary elements zero => each row is a 1x1 block
        double[] main = new double[] { 10.0, 5.0, 2.0, 1.0 };
        double[] secondary = new double[] { 0.0, 0.0, 0.0 };

        EigenDecomposition ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        double[] ev = ed.getRealEigenvalues();
        assertEquals(10.0, ev[0], EPSILON);
        assertEquals(5.0, ev[1], EPSILON);
        assertEquals(2.0, ev[2], EPSILON);
        assertEquals(1.0, ev[3], EPSILON);
    }

    @Test(timeout = 4000)
    public void testMatrixWithZeroEigenvalueSingular() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 1.0 },
            { 1.0, 1.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(2.0, ed.getRealEigenvalue(0), EPSILON);
        assertEquals(0.0, ed.getRealEigenvalue(1), EPSILON);
        assertEquals(0.0, ed.getDeterminant(), EPSILON);

        DecompositionSolver solver = ed.getSolver();
        assertFalse(solver.isNonSingular());

        try {
            solver.getInverse();
            fail("Expected SingularMatrixException");
        } catch (SingularMatrixException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testNegativeEigenvalues() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { -3.0, 0.0 },
            { 0.0, -7.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);

        assertEquals(-3.0, ed.getRealEigenvalue(0), EPSILON);
        assertEquals(-7.0, ed.getRealEigenvalue(1), EPSILON);
        assertEquals(21.0, ed.getDeterminant(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testIndefiniteMatrixWithEigenvectors() {
        // Mixed positive and negative eigenvalues (tests mu shift calculation in findEigenVectors)
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0, 0.0 },
            { 1.0, -1.0, 1.0 },
            { 0.0, 1.0, -3.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);

        RealMatrix v = ed.getV();
        RealMatrix vt = ed.getVT();
        RealMatrix d = ed.getD();

        RealMatrix reconstructed = v.multiply(d).multiply(vt);
        checkMatrixEquals(matrix, reconstructed, 1e-8);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-308 / Defects4J Math-81)
    // =========================================================================

    /**
     * Targets Defects4J Math-81 known defect:
     * EigenDecompositionImplTest::testMath308 throwing ArrayIndexOutOfBoundsException: -1
     * in computeShiftIncrement when end == 2 and pingPong == 0.
     */
    @Test(timeout = 4000)
    public void testMath308() {
        double[] main = new double[] {
            -0.34446487008778437, -0.34446487008778437, -0.34446487008778437, -0.34446487008778437
        };
        double[] secondary = new double[] {
            -0.2974868285526042, -0.2974868285526042, -0.2974868285526042
        };

        // On defective version, this triggers ArrayIndexOutOfBoundsException: -1
        EigenDecomposition ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        double[] eigenvalues = ed.getRealEigenvalues();
        assertNotNull(eigenvalues);
        assertEquals(4, eigenvalues.length);
        for (int i = 0; i < eigenvalues.length - 1; ++i) {
            assertTrue("Eigenvalues must be sorted descending", eigenvalues[i] >= eigenvalues[i + 1]);
        }
    }

    @Test(timeout = 4000)
    public void testDefectMath308Direct() {
        // Another instance triggering the dqd/dqds loop deflation boundaries
        double[] main = new double[] { 0.0, 0.0, 0.0, 0.0 };
        double[] secondary = new double[] { 1.0, 1.0, 1.0 };

        EigenDecomposition ed = new EigenDecompositionImpl(main, secondary, MathUtils.SAFE_MIN);
        double[] eigenvalues = ed.getRealEigenvalues();
        assertEquals(4, eigenvalues.length);
        // Expected eigenvalues of this symmetric tridiagonal are ~ 1.618, 0.618, -0.618, -1.618
        assertEquals(Math.sqrt(5.0) * 0.5 + 0.5, eigenvalues[0], 0.1);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = InvalidMatrixException.class, timeout = 4000)
    public void testAsymmetricMatrixThrowsException() {
        RealMatrix asymmetric = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });
        new EigenDecompositionImpl(asymmetric, MathUtils.SAFE_MIN);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetRealEigenvalueOutOfBoundsNegative() {
        RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(2);
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        ed.getRealEigenvalue(-1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetRealEigenvalueOutOfBoundsPositive() {
        RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(2);
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        ed.getRealEigenvalue(2);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetImagEigenvalueOutOfBounds() {
        RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(2);
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        ed.getImagEigenvalue(5);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetEigenvectorOutOfBounds() {
        RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(2);
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        ed.getEigenvector(2);
    }

    // =========================================================================
    // Partition E: DecompositionSolver Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testSolverWithVectorsAndMatrices() {
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0, 1.0, -1.0 },
            { 1.0, 2.0, 0.0 },
            { -1.0, 0.0, 3.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(a, MathUtils.SAFE_MIN);
        DecompositionSolver solver = ed.getSolver();

        assertTrue(solver.isNonSingular());

        // Solve double[]
        double[] bVector = new double[] { 1.0, 2.0, 3.0 };
        double[] xVector = solver.solve(bVector);
        RealVector ax = a.operate(new ArrayRealVector(xVector));
        checkVectorEquals(new ArrayRealVector(bVector), ax, 1e-8);

        // Solve RealVector
        RealVector bRealVector = new ArrayRealVector(bVector);
        RealVector xRealVector = solver.solve(bRealVector);
        checkVectorEquals(bRealVector, a.operate(xRealVector), 1e-8);

        // Solve RealMatrix
        RealMatrix bMatrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 0.0 },
            { 2.0, -1.0 },
            { 3.0, 2.0 }
        });
        RealMatrix xMatrix = solver.solve(bMatrix);
        RealMatrix axMatrix = a.multiply(xMatrix);
        checkMatrixEquals(bMatrix, axMatrix, 1e-8);

        // Inverse check: A * A^-1 = I
        RealMatrix inverse = solver.getInverse();
        checkMatrixEquals(MatrixUtils.createRealIdentityMatrix(3), a.multiply(inverse), 1e-8);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolverDimensionMismatchDoubleArray() {
        RealMatrix a = MatrixUtils.createRealIdentityMatrix(3);
        EigenDecomposition ed = new EigenDecompositionImpl(a, MathUtils.SAFE_MIN);
        ed.getSolver().solve(new double[] { 1.0, 2.0 });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolverDimensionMismatchRealVector() {
        RealMatrix a = MatrixUtils.createRealIdentityMatrix(3);
        EigenDecomposition ed = new EigenDecompositionImpl(a, MathUtils.SAFE_MIN);
        ed.getSolver().solve(new ArrayRealVector(new double[] { 1.0, 2.0, 3.0, 4.0 }));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolverDimensionMismatchRealMatrix() {
        RealMatrix a = MatrixUtils.createRealIdentityMatrix(3);
        EigenDecomposition ed = new EigenDecompositionImpl(a, MathUtils.SAFE_MIN);
        RealMatrix b = MatrixUtils.createRealMatrix(2, 2);
        ed.getSolver().solve(b);
    }

    @Test(expected = SingularMatrixException.class, timeout = 4000)
    public void testSolverSingularSolveDoubleArrayThrows() {
        RealMatrix singular = MatrixUtils.createRealMatrix(new double[][] {
            { 0.0, 0.0 },
            { 0.0, 0.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(singular, MathUtils.SAFE_MIN);
        ed.getSolver().solve(new double[] { 1.0, 1.0 });
    }

    @Test(expected = SingularMatrixException.class, timeout = 4000)
    public void testSolverSingularSolveRealVectorThrows() {
        RealMatrix singular = MatrixUtils.createRealMatrix(new double[][] {
            { 0.0, 0.0 },
            { 0.0, 0.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(singular, MathUtils.SAFE_MIN);
        ed.getSolver().solve(new ArrayRealVector(new double[] { 1.0, 1.0 }));
    }

    @Test(expected = SingularMatrixException.class, timeout = 4000)
    public void testSolverSingularSolveRealMatrixThrows() {
        RealMatrix singular = MatrixUtils.createRealMatrix(new double[][] {
            { 0.0, 0.0 },
            { 0.0, 0.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(singular, MathUtils.SAFE_MIN);
        ed.getSolver().solve(MatrixUtils.createRealIdentityMatrix(2));
    }

    // =========================================================================
    // Partition F: Caching and Repeated Calls
    // =========================================================================

    @Test(timeout = 4000)
    public void testCachingBehavior() {
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 }
        });
        EigenDecomposition ed = new EigenDecompositionImpl(a, MathUtils.SAFE_MIN);

        RealMatrix v1 = ed.getV();
        RealMatrix v2 = ed.getV();
        assertSame("getV() should return cached instance", v1, v2);

        RealMatrix d1 = ed.getD();
        RealMatrix d2 = ed.getD();
        assertSame("getD() should return cached instance", d1, d2);

        RealMatrix vt1 = ed.getVT();
        RealMatrix vt2 = ed.getVT();
        assertSame("getVT() should return cached instance", vt1, vt2);

        double[] ev1 = ed.getRealEigenvalues();
        double[] ev2 = ed.getRealEigenvalues();
        assertNotSame("getRealEigenvalues() should return a clone", ev1, ev2);
        assertArrayEquals(ev1, ev2, EPSILON);

        double[] iev1 = ed.getImagEigenvalues();
        double[] iev2 = ed.getImagEigenvalues();
        assertNotSame("getImagEigenvalues() should return a clone", iev1, iev2);
        assertArrayEquals(iev1, iev2, EPSILON);
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private void checkMatrixEquals(RealMatrix expected, RealMatrix actual, double tolerance) {
        assertEquals("Row dimensions must match", expected.getRowDimension(), actual.getRowDimension());
        assertEquals("Column dimensions must match", expected.getColumnDimension(), actual.getColumnDimension());
        for (int r = 0; r < expected.getRowDimension(); ++r) {
            for (int c = 0; c < expected.getColumnDimension(); ++c) {
                assertEquals("Entry mismatch at (" + r + ", " + c + ")",
                        expected.getEntry(r, c), actual.getEntry(r, c), tolerance);
            }
        }
    }

    private void checkVectorEquals(RealVector expected, RealVector actual, double tolerance) {
        assertEquals("Vector dimensions must match", expected.getDimension(), actual.getDimension());
        for (int i = 0; i < expected.getDimension(); ++i) {
            assertEquals("Vector entry mismatch at " + i,
                    expected.getEntry(i), actual.getEntry(i), tolerance);
        }
    }
}