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

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: SingularValueDecompositionImpl
 *
 * Decision / Branch Coverage Targets:
 * 1. Constructor:
 *    - max < eigenValues.length vs max >= eigenValues.length (Truncated SVD)
 *    - While loop: (p > 0 && eigenValues[p - 1] <= 0) - trimming non-positive eigenvalues
 *    - Bidiagonal transformer iteration (mainBidiagonal length > 1 vs = 1)
 * 2. getU():
 *    - cachedU == null vs cachedU != null (cache hit)
 *    - m >= n (tall/square branch: W = B.E.S^(-1) with loops 0..p-2 and p..m-1)
 *    - m < n (wide branch: cachedU = transformer.getU().multiply(e))
 * 3. getUT(), getS(), getV(), getVT():
 *    - Cached evaluation paths (null check & memoization)
 * 4. getV():
 *    - m >= n (tall/square branch: cachedV = transformer.getV().multiply(e))
 *    - m < n (wide branch: W = Bt.E.S^(-1) with loops 0..p-2 and p..n-1)
 * 5. getCovariance(minSingularValue):
 *    - dimension == 0 -> MathRuntimeException / IllegalArgumentException
 *    - dimension > 0 -> DefaultRealMatrixPreservingVisitor matrix construction
 *    - Filtering cutoff singular values (dimension < p vs dimension == p)
 * 6. getRank():
 *    - Threshold = max(m, n) * ulp(s[0])
 *    - Loop finding highest index where s[i] > threshold
 *    - Fallthrough returning 0 (zero matrix)
 * 7. Solver inner class:
 *    - solve(double[]), solve(RealVector), solve(RealMatrix)
 *    - isNonSingular(): true (square & full rank) vs false (singular or rectangular)
 *    - Dimension mismatch handling
 *    - getInverse() / pseudo-inverse property checks
 * 8. Defects4J Known Defect MATH-320 (testMath320A, testMath320B):
 *    - When matrix is rank-deficient (p < min(m, n)), least squares solver fails
 *      due to erroneous truncation and corrupted pseudo-inverse reconstruction.
 */
public class SingularValueDecompositionImplGeminiTest {

    private static final double EPS = 1.0e-10;

    // ------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (MATH-320 Ground Truth)
    // ------------------------------------------------------------------------

    /**
     * Targets Defects4J MATH-320: 3x3 rank-deficient matrix (rank 2).
     * Solves Ax = b where b is in the column space of A.
     * The defective implementation fails to eliminate the zero/noise singular value,
     * corrupting the least squares solution and producing a non-zero residual norm (0.09336767546650937).
     */
    @Test(timeout = 4000)
    public void testMath320A() {
        RealMatrix rm = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0, 3.0 },
            { 2.0, 3.0, 4.0 },
            { 3.0, 5.0, 7.0 }
        });
        RealVector b = new ArrayRealVector(new double[] { 1.0, 2.0, 3.0 });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(rm);
        RealVector x = svd.getSolver().solve(b);
        RealVector residual = rm.operate(x).subtract(b);
        assertEquals(0.0, residual.getNorm(), EPS);
    }

    /**
     * Targets Defects4J MATH-320: 2x2 rank-deficient matrix (rank 1).
     * Solves Ax = b where b is in the column space of A.
     * The defective implementation drops secondary bidiagonal entries when p < n,
     * resulting in a residual norm of 3.2 instead of 0.0.
     */
    @Test(timeout = 4000)
    public void testMath320B() {
        RealMatrix rm = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 2.0, 4.0 }
        });
        RealVector b = new ArrayRealVector(new double[] { 2.0, 4.0 });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(rm);
        RealVector x = svd.getSolver().solve(b);
        RealVector residual = rm.operate(x).subtract(b);
        assertEquals(0.0, residual.getNorm(), EPS);
    }

    // ------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSquareFullRankDecomposition() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);

        RealMatrix u = svd.getU();
        RealMatrix s = svd.getS();
        RealMatrix v = svd.getV();
        RealMatrix vt = svd.getVT();

        assertEquals(2, u.getRowDimension());
        assertEquals(2, u.getColumnDimension());
        assertEquals(2, s.getRowDimension());
        assertEquals(2, s.getColumnDimension());
        assertEquals(2, v.getRowDimension());
        assertEquals(2, v.getColumnDimension());

        // A = U * S * V^T
        RealMatrix reconstructed = u.multiply(s).multiply(vt);
        double diff = matrix.subtract(reconstructed).getNorm();
        assertEquals(0.0, diff, EPS);

        // Orthogonality: U^T * U = I, V^T * V = I
        RealMatrix identity2 = MatrixUtils.createRealIdentityMatrix(2);
        assertEquals(0.0, svd.getUT().multiply(u).subtract(identity2).getNorm(), EPS);
        assertEquals(0.0, vt.multiply(v).subtract(identity2).getNorm(), EPS);

        assertEquals(2, svd.getRank());
        assertTrue(svd.getSolver().isNonSingular());
        assertEquals(svd.getSingularValues()[0], svd.getNorm(), EPS);
        assertEquals(svd.getSingularValues()[0] / svd.getSingularValues()[1], svd.getConditionNumber(), EPS);
    }

    @Test(timeout = 4000)
    public void testTallMatrixDecomposition() {
        // m > n (4x2): triggers m >= n branch in getU() and getV()
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 0.0 },
            { 0.0, 2.0 },
            { 3.0, 0.0 },
            { 0.0, 4.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);

        RealMatrix u = svd.getU();
        RealMatrix s = svd.getS();
        RealMatrix vt = svd.getVT();

        RealMatrix reconstructed = u.multiply(s).multiply(vt);
        assertEquals(0.0, matrix.subtract(reconstructed).getNorm(), EPS);

        assertEquals(4, u.getRowDimension());
        assertEquals(2, u.getColumnDimension());
        assertEquals(2, s.getRowDimension());
        assertEquals(2, s.getColumnDimension());
        assertEquals(2, svd.getV().getRowDimension());
        assertEquals(2, svd.getV().getColumnDimension());

        // Rectangular matrix cannot be non-singular in Solver definition
        assertFalse(svd.getSolver().isNonSingular());
        assertEquals(2, svd.getRank());
    }

    @Test(timeout = 4000)
    public void testWideMatrixDecomposition() {
        // m < n (2x4): triggers m < n branch in getU() and getV()
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 0.0, 3.0, 0.0 },
            { 0.0, 2.0, 0.0, 4.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);

        RealMatrix u = svd.getU();
        RealMatrix s = svd.getS();
        RealMatrix vt = svd.getVT();

        RealMatrix reconstructed = u.multiply(s).multiply(vt);
        assertEquals(0.0, matrix.subtract(reconstructed).getNorm(), EPS);

        assertEquals(2, u.getRowDimension());
        assertEquals(2, u.getColumnDimension());
        assertEquals(2, s.getRowDimension());
        assertEquals(2, s.getColumnDimension());
        assertEquals(4, svd.getV().getRowDimension());
        assertEquals(2, svd.getV().getColumnDimension());

        assertFalse(svd.getSolver().isNonSingular());
        assertEquals(2, svd.getRank());
    }

    @Test(timeout = 4000)
    public void testCachingIntegrity() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);

        RealMatrix u1 = svd.getU();
        RealMatrix u2 = svd.getU();
        assertSame("getU() should return cached instance", u1, u2);

        RealMatrix ut1 = svd.getUT();
        RealMatrix ut2 = svd.getUT();
        assertSame("getUT() should return cached instance", ut1, ut2);

        RealMatrix s1 = svd.getS();
        RealMatrix s2 = svd.getS();
        assertSame("getS() should return cached instance", s1, s2);

        RealMatrix v1 = svd.getV();
        RealMatrix v2 = svd.getV();
        assertSame("getV() should return cached instance", v1, v2);

        RealMatrix vt1 = svd.getVT();
        RealMatrix vt2 = svd.getVT();
        assertSame("getVT() should return cached instance", vt1, vt2);

        double[] sv1 = svd.getSingularValues();
        double[] sv2 = svd.getSingularValues();
        assertNotSame("getSingularValues() must return a defensive clone", sv1, sv2);
        assertArrayEquals(sv1, sv2, EPS);
    }

    // ------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Coverage Fillers
    // ------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testTruncatedSVDConstructor() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0, 3.0 },
            { 4.0, 5.0, 6.0 },
            { 7.0, 8.0, 9.0 }
        });
        // max = 1: compute only 1 singular value
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix, 1);
        double[] sv = svd.getSingularValues();
        assertEquals(1, sv.length);
        assertEquals(1, svd.getS().getRowDimension());
        assertEquals(1, svd.getS().getColumnDimension());
        assertEquals(1, svd.getU().getColumnDimension());
        assertEquals(1, svd.getV().getColumnDimension());
    }

    @Test(timeout = 4000)
    public void testTallRankDeficientPZeroFillLoops() {
        // 3x2 matrix with rank 1 -> p = 1, m = 3 > p, exercises loop for (int i = p; i < m; ++i) in getU()
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 2.0, 4.0 },
            { 3.0, 6.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        RealMatrix u = svd.getU();
        assertEquals(3, u.getRowDimension());
    }

    @Test(timeout = 4000)
    public void testWideRankDeficientPZeroFillLoops() {
        // 2x3 matrix with rank 1 -> p = 1, n = 3 > p, exercises loop for (int i = p; i < n; ++i) in getV()
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0, 3.0 },
            { 2.0, 4.0, 6.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        RealMatrix v = svd.getV();
        assertEquals(3, v.getRowDimension());
    }

    @Test(timeout = 4000)
    public void testCovarianceValidAllSingularValues() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 5.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        double minSV = svd.getSingularValues()[1] - 1.0e-6;
        RealMatrix cov = svd.getCovariance(minSV);
        assertNotNull(cov);
        assertEquals(2, cov.getRowDimension());
        assertEquals(2, cov.getColumnDimension());
    }

    @Test(timeout = 4000)
    public void testCovarianceTruncatedDimension() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            { 10.0, 0.0 },
            { 0.0, 1.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        // cutoff between 1st and 2nd singular value -> dimension = 1 < p
        double minSV = 5.0;
        RealMatrix cov = svd.getCovariance(minSV);
        assertNotNull(cov);
        assertEquals(2, cov.getRowDimension());
        assertEquals(2, cov.getColumnDimension());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCovarianceTooHighMinSingularValue() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        double tooLarge = svd.getSingularValues()[0] + 10.0;
        svd.getCovariance(tooLarge);
    }

    @Test(timeout = 4000)
    public void testZeroMatrixRank() {
        RealMatrix zero = new Array2DRowRealMatrix(new double[][] {
            { 0.0, 0.0 },
            { 0.0, 0.0 }
        });
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(zero);
        assertEquals(0, svd.getRank());
        assertEquals(0, svd.getSingularValues().length);
    }

    // ------------------------------------------------------------------------
    // Partition D: Solver Operations & Defensive Exception Paths
    // ------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSolverSolveDoubleArray() {
        RealMatrix a = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 0.0 },
            { 0.0, 2.0 }
        });
        DecompositionSolver solver = new SingularValueDecompositionImpl(a).getSolver();
        double[] b = new double[] { 2.0, 4.0 };
        double[] x = solver.solve(b);
        assertArrayEquals(new double[] { 2.0, 2.0 }, x, EPS);
    }

    @Test(timeout = 4000)
    public void testSolverSolveRealVector() {
        RealMatrix a = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 0.0 },
            { 0.0, 2.0 }
        });
        DecompositionSolver solver = new SingularValueDecompositionImpl(a).getSolver();
        RealVector b = new ArrayRealVector(new double[] { 2.0, 4.0 });
        RealVector x = solver.solve(b);
        assertEquals(2.0, x.getEntry(0), EPS);
        assertEquals(2.0, x.getEntry(1), EPS);
    }

    @Test(timeout = 4000)
    public void testSolverSolveRealMatrix() {
        RealMatrix a = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 0.0 },
            { 0.0, 2.0 }
        });
        DecompositionSolver solver = new SingularValueDecompositionImpl(a).getSolver();
        RealMatrix b = new Array2DRowRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 4.0, 2.0 }
        });
        RealMatrix x = solver.solve(b);
        assertEquals(2.0, x.getEntry(0, 0), EPS);
        assertEquals(1.0, x.getEntry(0, 1), EPS);
        assertEquals(2.0, x.getEntry(1, 0), EPS);
        assertEquals(1.0, x.getEntry(1, 1), EPS);
    }

    @Test(timeout = 4000)
    public void testSolverGetInverseSquare() {
        RealMatrix a = new Array2DRowRealMatrix(new double[][] {
            { 2.0, 0.0 },
            { 0.0, 4.0 }
        });
        DecompositionSolver solver = new SingularValueDecompositionImpl(a).getSolver();
        RealMatrix inv = solver.getInverse();
        assertEquals(0.5, inv.getEntry(0, 0), EPS);
        assertEquals(0.25, inv.getEntry(1, 1), EPS);
        assertEquals(0.0, inv.getEntry(0, 1), EPS);
        assertEquals(0.0, inv.getEntry(1, 0), EPS);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolverSolveDimensionMismatchVector() {
        RealMatrix a = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });
        DecompositionSolver solver = new SingularValueDecompositionImpl(a).getSolver();
        solver.solve(new ArrayRealVector(new double[] { 1.0, 2.0, 3.0 }));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolverSolveDimensionMismatchArray() {
        RealMatrix a = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });
        DecompositionSolver solver = new SingularValueDecompositionImpl(a).getSolver();
        solver.solve(new double[] { 1.0, 2.0, 3.0 });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolverSolveDimensionMismatchMatrix() {
        RealMatrix a = new Array2DRowRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });
        DecompositionSolver solver = new SingularValueDecompositionImpl(a).getSolver();
        solver.solve(new Array2DRowRealMatrix(new double[3][2]));
    }
}