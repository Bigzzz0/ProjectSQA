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
 *
 * Target Class: EigenDecompositionImpl
 * Defect Under Analysis: Defects4J Math-81 / Mathpbx02 regression where dqds shift computation
 *                        in computeShiftIncrement under case 4 / case 5 produces an incorrect
 *                        shift value leading to erroneous largest eigenvalue (20654.745... vs
 *                        expected 16828.208...).
 *
 * Key Branch Coverage Zones:
 * 1. Constructor:
 *    - RealMatrix symmetric vs asymmetric (throws InvalidMatrixException).
 *    - Tridiagonal (double[] main, double[] secondary) constructor.
 * 2. Splitting & Block Sizes:
 *    - Dimension 1 block (process1RowBlock).
 *    - Dimension 2 block (process2RowsBlock).
 *    - Dimension 3 block (process3RowsBlock).
 *    - General block (n >= 4, dqd / dqds, flipIfWarranted, goodStep, computeShiftIncrement).
 *    - Splitting points with splitTolerance > 0 and secondary elements <= max.
 *    - Zero off-diagonal elements in general block (sumOffDiag == 0).
 * 3. Matrix Properties & Decompositions:
 *    - Cached getters: getV(), getD(), getVT() (verifying cache hits on secondary calls).
 *    - getDeterminant(), getEigenvector(i), getRealEigenvalues(), getImagEigenvalues().
 *    - Solver: solve(double[]), solve(RealVector), solve(RealMatrix), getInverse(), isNonSingular().
 *    - Singular matrix handling (SingularMatrixException).
 *    - Dimension mismatch handling in Solver (IllegalArgumentException).
 * 4. Boundaries and Extremes:
 *    - Dimension 1 (1x1), Dimension 2 (2x2), Dimension 3 (3x3), Dimension >= 4.
 *    - Negative eigenvalues, zero eigenvalues, indefinite and positive definite matrices.
 *    - Bounds on getRealEigenvalue / getImagEigenvalue / getEigenvector indices.
 */
public class EigenDecompositionImplGeminiTest {

    /* =========================================================================
     * Partition C: Defect-Targeted Branch Zone (Defects4J Known Defect)
     * ========================================================================= */

    /**
     * Regression test for known defect testMathpbx02.
     * Triggers the dqds step logic where shift computation failed in computeShiftIncrement,
     * causing realEigenvalues[0] to be computed as ~20654.745 instead of ~16828.208.
     */
    @Test(timeout = 4000)
    public void testMathpbx02() {
        final double[] d = new double[] {
            11075.257404285885, 8740.090124208034, 4614.492582767073,
            1438.3079944061864, 187.6418858810799, 4.417277884814981
        };
        final double[] e = new double[] {
            7459.790906202425, 4744.029809939527, 2197.8860368305884,
            502.8277259169498, 29.58572111586561
        };
        final EigenDecomposition ed = new EigenDecompositionImpl(d, e, 0);
        final double[] realEigenvalues = ed.getRealEigenvalues();
        assertEquals(16828.208208485466, realEigenvalues[0], 1.0e-10);
    }

    /* =========================================================================
     * Partition A: Core Functional Logic & State Transitions
     * ========================================================================= */

    @Test(timeout = 4000)
    public void test1x1Matrix() {
        final double[] main = new double[] { 42.0 };
        final double[] secondary = new double[0];
        final EigenDecomposition ed = new EigenDecompositionImpl(main, secondary, 0.0);

        final double[] eigenvalues = ed.getRealEigenvalues();
        assertEquals(1, eigenvalues.length);
        assertEquals(42.0, eigenvalues[0], 1.0e-12);
        assertEquals(42.0, ed.getRealEigenvalue(0), 1.0e-12);
        assertEquals(0.0, ed.getImagEigenvalue(0), 1.0e-12);
        assertEquals(42.0, ed.getDeterminant(), 1.0e-12);

        final RealMatrix v = ed.getV();
        assertEquals(1, v.getRowDimension());
        assertEquals(1, v.getColumnDimension());
        assertEquals(1.0, Math.abs(v.getEntry(0, 0)), 1.0e-12);

        final RealMatrix d = ed.getD();
        assertEquals(42.0, d.getEntry(0, 0), 1.0e-12);

        final RealMatrix vt = ed.getVT();
        assertEquals(1.0, Math.abs(vt.getEntry(0, 0)), 1.0e-12);

        final RealVector ev = ed.getEigenvector(0);
        assertEquals(1.0, Math.abs(ev.getEntry(0)), 1.0e-12);
    }

    @Test(timeout = 4000)
    public void test2x2Matrix() {
        // Matrix: [ [2, 1], [1, 2] ] -> Eigenvalues: 3.0 and 1.0
        final double[] main = new double[] { 2.0, 2.0 };
        final double[] secondary = new double[] { 1.0 };
        final EigenDecomposition ed = new EigenDecompositionImpl(main, secondary, 0.0);

        final double[] eigenvalues = ed.getRealEigenvalues();
        assertEquals(2, eigenvalues.length);
        assertEquals(3.0, eigenvalues[0], 1.0e-12);
        assertEquals(1.0, eigenvalues[1], 1.0e-12);
        assertEquals(3.0, ed.getDeterminant(), 1.0e-12);

        final RealMatrix v = ed.getV();
        final RealMatrix vt = ed.getVT();
        final RealMatrix identity = v.multiply(vt);
        assertEquals(1.0, identity.getEntry(0, 0), 1.0e-12);
        assertEquals(0.0, identity.getEntry(0, 1), 1.0e-12);
        assertEquals(0.0, identity.getEntry(1, 0), 1.0e-12);
        assertEquals(1.0, identity.getEntry(1, 1), 1.0e-12);

        // Verification of A * v = lambda * v
        final RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 }
        });
        for (int i = 0; i < 2; ++i) {
            final RealVector vi = ed.getEigenvector(i);
            final RealVector lambdaVi = vi.mapMultiply(eigenvalues[i]);
            final RealVector aVi = a.operate(vi);
            assertEquals(0.0, aVi.subtract(lambdaVi).getNorm(), 1.0e-10);
        }
    }

    @Test(timeout = 4000)
    public void test3x3Matrix() {
        // 3x3 symmetric tridiagonal matrix
        final double[] main = new double[] { 4.0, 3.0, 2.0 };
        final double[] secondary = new double[] { 1.0, 1.0 };
        final EigenDecomposition ed = new EigenDecompositionImpl(main, secondary, 0.0);

        final double[] eigenvalues = ed.getRealEigenvalues();
        assertEquals(3, eigenvalues.length);
        assertTrue(eigenvalues[0] >= eigenvalues[1]);
        assertTrue(