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
 * Partition A: Core Functional Logic & State Transitions
 * - Constructors: default, dimensions, double[][], double[][] with copy flag, double[] column vector.
 * - Basic accessors & dimensions: getRowDimension, getColumnDimension, getData, getDataRef, isSquare.
 * - Arithmetic operations: add, subtract, multiply, preMultiply, scalarAdd, scalarMultiply.
 * - Interface polymorphism & ClassCastException fallbacks (non-RealMatrixImpl inputs for add, subtract, multiply).
 * - Vector operations: operate, preMultiply(double[]).
 * - Matrix transformations: transpose, copy, getRow, getColumn, getRowMatrix, getColumnMatrix.
 * - Linear solver & LU decomposition: solve(double[]), solve(RealMatrix), inverse, getDeterminant,
 *   isSingular, luDecompose with row pivoting (max != col) and parity alternation, getLUMatrix, getPermutation.
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - Singular matrices (all zeros, linearly dependent rows/cols).
 * - 1x1 matrix edge cases (determinant, trace, solve, inverse).
 * - Precision & norm: getNorm across uniform and non-uniform distributions.
 * - Submatrix operations with 0-index boundaries and multi-element extractions.
 *
 * Partition C: Defect-Targeted Branch Zone (MATH-209)
 * - getSubMatrix(startRow, endRow, startColumn, endColumn) index validation bounds.
 *   Defect: endRow >= data.length or endColumn >= data[0].length triggers ArrayIndexOutOfBoundsException
 *   instead of throwing MatrixIndexException due to improper '>' check instead of '>=' check against row/col size.
 * - Explicit test asserting MatrixIndexException when endRow == data.length or endColumn == data[0].length.
 *
 * Partition D: Exception & Defensive Guard Paths
 * - Dimension checks in constructors: negative or zero row/col, jagged/empty arrays in copyIn/setSubMatrix.
 * - Matrix dimension mismatch in add, subtract, multiply, solve, operate, preMultiply.
 * - Invalid index accesses in getEntry, getRow, getColumn, getRowMatrix, getColumnMatrix, getSubMatrix.
 * - Non-square matrix exceptions for getDeterminant, getTrace, luDecompose, solve.
 * - MatrixIndexException on uninitialized or overflowing setSubMatrix calls.
 *
 * Partition E: Object Lifecycle & Contract Integrity
 * - toString() on empty/uninitialized and populated matrices.
 * - equals() contract: identity, null, different type, mismatched dimensions, unequal entries, identical entries.
 * - hashCode() consistency and compliance with equals.
 */
public class RealMatrixImplGeminiTest {

    private static final double EPSILON = 1e-11;

    // Helper dummy implementation of RealMatrix to test interface fallback branches
    private static class CustomRealMatrix implements RealMatrix {
        private final double[][] entries;

        public CustomRealMatrix(double[][] data) {
            this.entries = data;
        }

        public RealMatrix copy() { return new CustomRealMatrix(entries); }
        public RealMatrix add(RealMatrix m) { throw new UnsupportedOperationException(); }
        public RealMatrix subtract(RealMatrix m) { throw new UnsupportedOperationException(); }
        public RealMatrix multiply(RealMatrix m) { throw new UnsupportedOperationException(); }
        public RealMatrix preMultiply(RealMatrix m) { throw new UnsupportedOperationException(); }
        public double[][] getData() { return entries; }
        public double getNorm() { return 0; }
        public RealMatrix getSubMatrix(int startRow, int endRow, int startColumn, int endColumn) { return null; }
        public RealMatrix getSubMatrix(int[] selectedRows, int[] selectedColumns) { return null; }
        public void setSubMatrix(double[][] subMatrix, int row, int column) {}
        public RealMatrix getRowMatrix(int row) { return null; }
        public RealMatrix getColumnMatrix(int column) { return null; }
        public double[] getRow(int row) { return entries[row]; }
        public double[] getColumn(int col) { return null; }
        public double getEntry(int row, int column) { return entries[row][column]; }
        public RealMatrix transpose() { return null; }
        public RealMatrix inverse() { return null; }
        public double getDeterminant() { return 0; }
        public boolean isSquare() { return entries.length == entries[0].length; }
        public boolean isSingular() { return false; }
        public int getRowDimension() { return entries.length; }
        public int getColumnDimension() { return entries[0].length; }
        public double getTrace() { return 0; }
        public double[] operate(double[] v) { return null; }
        public double[] preMultiply(double[] v) { return null; }
        public double[] solve(double[] b) { return null; }
        public RealMatrix solve(RealMatrix b) { return null; }
        public RealMatrix scalarAdd(double d) { return null; }
        public RealMatrix scalarMultiply(double d) { return null; }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndAccessors() {
        RealMatrixImpl m = new RealMatrixImpl(2, 3);
        assertEquals(2, m.getRowDimension());
        assertEquals(3, m.getColumnDimension());
        assertEquals(0.0, m.getEntry(0, 0), EPSILON);

        double[][] testData = {{1.0, 2.0}, {3.0, 4.0}};
        RealMatrixImpl m2 = new RealMatrixImpl(testData);
        assertEquals(2, m2.getRowDimension());
        assertEquals(2, m2.getColumnDimension());
        assertEquals(1.0, m2.getEntry(0, 0), EPSILON);

        // Modify original array, m2 should remain unaffected (copy verified)
        testData[0][0] = 99.0;
        assertEquals(1.0, m2.getEntry(0, 0), EPSILON);

        // Reference constructor with copyArray = false
        double[][] refData = {{5.0, 6.0}, {7.0, 8.0}};
        RealMatrixImpl mRef = new RealMatrixImpl(refData, false);
        assertSame(refData, mRef.getDataRef());

        // Vector constructor
        double[] v = {10.0, 20.0, 30.0};
        RealMatrixImpl mVec = new RealMatrixImpl(v);
        assertEquals(3, mVec.getRowDimension());
        assertEquals(1, mVec.getColumnDimension());
        assertEquals(20.0, mVec.getEntry(1, 0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testBasicArithmeticSameType() {
        double[][] d1 = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] d2 = {{5.0, 6.0}, {7.0, 8.0}};
        RealMatrixImpl m1 = new RealMatrixImpl(d1);
        RealMatrixImpl m2 = new RealMatrixImpl(d2);

        RealMatrixImpl sum = m1.add(m2);
        assertArrayEquals(new double[]{6.0, 8.0}, sum.getRow(0), EPSILON);
        assertArrayEquals(new double[]{10.0, 12.0}, sum.getRow(1), EPSILON);

        RealMatrixImpl diff = m2.subtract(m1);
        assertArrayEquals(new double[]{4.0, 4.0}, diff.getRow(0), EPSILON);
        assertArrayEquals(new double[]{4.0, 4.0}, diff.getRow(1), EPSILON);

        RealMatrixImpl prod = m1.multiply(m2);
        assertArrayEquals(new double[]{19.0, 22.0}, prod.getRow(0), EPSILON);
        assertArrayEquals(new double[]{43.0, 50.0}, prod.getRow(1), EPSILON);

        RealMatrix preProd = m2.preMultiply(m1);
        assertEquals(prod, preProd);

        RealMatrix scAdd = m1.scalarAdd(2.0);
        assertEquals(3.0, scAdd.getEntry(0, 0), EPSILON);
        assertEquals(6.0, scAdd.getEntry(1, 1), EPSILON);

        RealMatrix scMul = m1.scalarMultiply(3.0);
        assertEquals(3.0, scMul.getEntry(0, 0), EPSILON);
        assertEquals(12.0, scMul.getEntry(1, 1), EPSILON);
    }

    @Test(timeout = 4000)
    public void testArithmeticInterfaceFallback() {
        double[][] d1 = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] d2 = {{2.0, 0.0}, {1.0, 2.0}};
        RealMatrixImpl m1 = new RealMatrixImpl(d1);
        RealMatrix mCustom = new CustomRealMatrix(d2);

        RealMatrix sum = m1.add(mCustom);
        assertEquals(3.0, sum.getEntry(0, 0), EPSILON);
        assertEquals(2.0, sum.getEntry(0, 1), EPSILON);

        RealMatrix diff = m1.subtract(mCustom);
        assertEquals(-1.0, diff.getEntry(0, 0), EPSILON);
        assertEquals(2.0, diff.getEntry(0, 1), EPSILON);

        RealMatrix prod = m1.multiply(mCustom);
        assertEquals(4.0, prod.getEntry(0, 0), EPSILON);
        assertEquals(4.0, prod.getEntry(0, 1), EPSILON);
        assertEquals(10.0, prod.getEntry(1, 0), EPSILON);
        assertEquals(8.0, prod.getEntry(1, 1), EPSILON);
    }

    @Test(timeout = 4000)
    public void testVectorOperations() {
        double[][] d = {{1.0, -2.0}, {3.0, 4.0}};
        RealMatrixImpl m = new RealMatrixImpl(d);
        double[] v = {2.0, 3.0};

        double[] out = m.operate(v);
        assertEquals(-4.0, out[0], EPSILON);
        assertEquals(18.0, out[1], EPSILON);

        double[] preOut = m.preMultiply(v);
        assertEquals(11.0, preOut[0], EPSILON);
        assertEquals(8.0, preOut[1], EPSILON);
    }

    @Test(timeout = 4000)
    public void testNormTransposeAndTrace() {
        double[][] d = {{1.0, -5.0}, {3.0, 2.0}};
        RealMatrixImpl m = new RealMatrixImpl(d);

        assertEquals(7.0, m.getNorm(), EPSILON); // max col sum: |1|+3=4, |-5|+2=7
        assertEquals(3.0, m.getTrace(), EPSILON); // 1.0 + 2.0

        RealMatrix mTrans = m.transpose();
        assertEquals(1.0, mTrans.getEntry(0, 0), EPSILON);
        assertEquals(3.0, mTrans.getEntry(0, 1), EPSILON);
        assertEquals(-5.0, mTrans.getEntry(1, 0), EPSILON);
        assertEquals(2.0, mTrans.getEntry(1, 1), EPSILON);
    }

    @Test(timeout = 4000)
    public void testRowAndColumnMatrixGetters() {
        double[][] d = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}};
        RealMatrixImpl m = new RealMatrixImpl(d);

        RealMatrix rowMat = m.getRowMatrix(1);
        assertEquals(1, rowMat.getRowDimension());
        assertEquals(3, rowMat.getColumnDimension());
        assertArrayEquals(new double[]{4.0, 5.0, 6.0}, rowMat.getRow(0), EPSILON);

        RealMatrix colMat = m.getColumnMatrix(2);
        assertEquals(2, colMat.getRowDimension());
        assertEquals(1, colMat.getColumnDimension());
        assertEquals(3.0, colMat.getEntry(0, 0), EPSILON);
        assertEquals(6.0, colMat.getEntry(1, 0), EPSILON);

        assertArrayEquals(new double[]{4.0, 5.0, 6.0}, m.getRow(1), EPSILON);
        assertArrayEquals(new double[]{3.0, 6.0}, m.getColumn(2), EPSILON);
    }

    @Test(timeout = 4000)
    public void testLUSolveInversionDeterminant() {
        // Needs pivoting: row 1 has a larger leading coefficient than row 0
        double[][] d = {{1.0, 2.0}, {3.0, -1.0}};
        RealMatrixImpl m = new RealMatrixImpl(d);

        assertFalse(m.isSingular());
        assertEquals(-7.0, m.getDeterminant(), EPSILON);

        RealMatrix inv = m.inverse();
        RealMatrix identity = m.multiply(inv);
        assertEquals(1.0, identity.getEntry(0, 0), EPSILON);
        assertEquals(0.0, identity.getEntry(0, 1), EPSILON);
        assertEquals(0.0, identity.getEntry(1, 0), EPSILON);
        assertEquals(1.0, identity.getEntry(1, 1), EPSILON);

        double[] b = {5.0, 1.0};
        double[] x = m.solve(b);
        // 1*x0 + 2*x1 = 5, 3*x0 - 1*x1 = 1 => x0 = 1, x1 = 2
        assertEquals(1.0, x[0], EPSILON);
        assertEquals(2.0, x[1], EPSILON);

        RealMatrix bMatrix = new RealMatrixImpl(new double[][]{{5.0}, {1.0}});
        RealMatrix xMatrix = m.solve(bMatrix);
        assertEquals(1.0, xMatrix.getEntry(0, 0), EPSILON);
        assertEquals(2.0, xMatrix.getEntry(1, 0), EPSILON);

        // Protected internal checks
        RealMatrix lu = m.getLUMatrix();
        assertNotNull(lu);
        int[] perm = m.getPermutation();
        assertEquals(2, perm.length);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testOneByOneMatrix() {
        double[][] d = {{4.0}};
        RealMatrixImpl m = new RealMatrixImpl(d);
        assertTrue(m.isSquare());
        assertFalse(m.isSingular());
        assertEquals(4.0, m.getDeterminant(), EPSILON);
        assertEquals(4.0, m.getTrace(), EPSILON);
        assertEquals(4.0, m.getNorm(), EPSILON);

        RealMatrix inv = m.inverse();
        assertEquals(0.25, inv.getEntry(0, 0), EPSILON);

        double[] sol = m.solve(new double[]{8.0});
        assertEquals(2.0, sol[0], EPSILON);
    }

    @Test(timeout = 4000)
    public void testSingularMatrixProperties() {
        double[][] d = {{1.0, 2.0}, {2.0, 4.0}};
        RealMatrixImpl m = new RealMatrixImpl(d);
        assertTrue(m.isSingular());
        assertEquals(0.0, m.getDeterminant(), EPSILON);

        try {
            m.inverse();
            fail("Expected InvalidMatrixException on singular inversion");
        } catch (InvalidMatrixException expected) {
            // expected
        }

        try {
            m.solve(new double[]{1.0, 2.0});
            fail("Expected InvalidMatrixException on singular solve");
        } catch (InvalidMatrixException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSubMatrixExtractionAndReplacement() {
        double[][] d = {
            {1, 2, 3, 4},
            {5, 6, 7, 8},
            {9, 0, 1, 2}
        };
        RealMatrixImpl m = new RealMatrixImpl(d);

        RealMatrix sub = m.getSubMatrix(0, 1, 1, 2);
        assertEquals(2, sub.getRowDimension());
        assertEquals(2, sub.getColumnDimension());
        assertEquals(2.0, sub.getEntry(0, 0), EPSILON);
        assertEquals(3.0, sub.getEntry(0, 1), EPSILON);
        assertEquals(6.0, sub.getEntry(1, 0), EPSILON);
        assertEquals(7.0, sub.getEntry(1, 1), EPSILON);

        RealMatrix indexedSub = m.getSubMatrix(new int[]{0, 2}, new int[]{1, 3});
        assertEquals(2, indexedSub.getRowDimension());
        assertEquals(2, indexedSub.getColumnDimension());
        assertEquals(2.0, indexedSub.getEntry(0, 0), EPSILON);
        assertEquals(4.0, indexedSub.getEntry(0, 1), EPSILON);
        assertEquals(0.0, indexedSub.getEntry(1, 0), EPSILON);
        assertEquals(2.0, indexedSub.getEntry(1, 1), EPSILON);

        double[][] replacement = {{99, 98}, {97, 96}};
        m.setSubMatrix(replacement, 1, 1);
        assertEquals(99.0, m.getEntry(1, 1), EPSILON);
        assertEquals(98.0, m.getEntry(1, 2), EPSILON);
        assertEquals(97.0, m.getEntry(2, 1), EPSILON);
        assertEquals(96.0, m.getEntry(2, 2), EPSILON);
    }

    @Test(timeout = 4000)
    public void testDefaultConstructorAndDelayedSubMatrixInit() {
        RealMatrixImpl m = new RealMatrixImpl();
        double[][] sub = {{1.0, 2.0}, {3.0, 4.0}};
        m.setSubMatrix(sub, 0, 0);
        assertEquals(2, m.getRowDimension());
        assertEquals(2, m.getColumnDimension());
        assertEquals(1.0, m.getEntry(0, 0), EPSILON);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-209)
    // =========================================================================

    /**
     * Targets Commons Math issue MATH-209.
     * getSubMatrix index checks: endRow and endColumn must be strictly less than
     * rowDimension and columnDimension respectively (0-indexed).
     * If an index equals rowDimension or columnDimension, a MatrixIndexException
     * MUST be thrown, not an ArrayIndexOutOfBoundsException.
     */
    @Test(timeout = 4000)
    public void testMath209DefectBoundaryCheck() {
        RealMatrix m = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});

        try {
            m.getSubMatrix(0, 2, 0, 1);
            fail("Expected MatrixIndexException when endRow == getRowDimension()");
        } catch (MatrixIndexException e) {
            // Expected behavior
        }

        try {
            m.getSubMatrix(0, 1, 0, 2);
            fail("Expected MatrixIndexException when endColumn == getColumnDimension()");
        } catch (MatrixIndexException e) {
            // Expected behavior
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorZeroRows() {
        new RealMatrixImpl(0, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorZeroColumns() {
        new RealMatrixImpl(2, 0);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorNullArray() {
        new RealMatrixImpl((double[][]) null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorNullArrayNoCopy() {
        new RealMatrixImpl((double[][]) null, false);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorEmptyRows() {
        new RealMatrixImpl(new double[][]{});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorEmptyRowsNoCopy() {
        new RealMatrixImpl(new double[][]{}, false);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorEmptyCols() {
        new RealMatrixImpl(new double[][]{{}});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorEmptyColsNoCopy() {
        new RealMatrixImpl(new double[][]{{}}, false);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorJaggedArray() {
        new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0}});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorJaggedArrayNoCopy() {
        new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0}}, false);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddDimensionMismatchSameType() {
        RealMatrixImpl m1 = new RealMatrixImpl(2, 2);
        RealMatrixImpl m2 = new RealMatrixImpl(2, 3);
        m1.add(m2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddDimensionMismatchCustom() {
        RealMatrixImpl m1 = new RealMatrixImpl(2, 2);
        RealMatrix mCustom = new CustomRealMatrix(new double[2][3]);
        m1.add(mCustom);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSubtractDimensionMismatchSameType() {
        RealMatrixImpl m1 = new RealMatrixImpl(2, 2);
        RealMatrixImpl m2 = new RealMatrixImpl(3, 2);
        m1.subtract(m2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSubtractDimensionMismatchCustom() {
        RealMatrixImpl m1 = new RealMatrixImpl(2, 2);
        RealMatrix mCustom = new CustomRealMatrix(new double[3][2]);
        m1.subtract(mCustom);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMultiplyDimensionMismatchSameType() {
        RealMatrixImpl m1 = new RealMatrixImpl(2, 3);
        RealMatrixImpl m2 = new RealMatrixImpl(2, 3);
        m1.multiply(m2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMultiplyDimensionMismatchCustom() {
        RealMatrixImpl m1 = new RealMatrixImpl(2, 3);
        RealMatrix mCustom = new CustomRealMatrix(new double[2][3]);
        m1.multiply(mCustom);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetSubMatrixNegativeIndex() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getSubMatrix(-1, 1, 0, 1);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetSubMatrixInvertedRows() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getSubMatrix(1, 0, 0, 1);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetSubMatrixEmptySelectedRows() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getSubMatrix(new int[]{}, new int[]{0});
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetSubMatrixInvalidSelectedIndices() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getSubMatrix(new int[]{0, 5}, new int[]{0});
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testSetSubMatrixNegativeCoords() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.setSubMatrix(new double[][]{{1.0}}, -1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetSubMatrixEmptyInput() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.setSubMatrix(new double[][]{}, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetSubMatrixEmptyColsInput() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.setSubMatrix(new double[][]{{}}, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetSubMatrixJagged() {
        RealMatrixImpl m = new RealMatrixImpl(3, 3);
        m.setSubMatrix(new double[][]{{1.0, 2.0}, {3.0}}, 0, 0);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testSetSubMatrixUninitializedNonZero() {
        RealMatrixImpl m = new RealMatrixImpl();
        m.setSubMatrix(new double[][]{{1.0}}, 1, 0);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testSetSubMatrixOutOfBounds() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.setSubMatrix(new double[][]{{1.0, 2.0}, {3.0, 4.0}}, 1, 1);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetRowMatrixInvalid() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getRowMatrix(5);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetColumnMatrixInvalid() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getColumnMatrix(-1);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetRowInvalid() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getRow(2);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetColumnInvalid() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getColumn(2);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetEntryInvalid() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getEntry(2, 0);
    }

    @Test(expected = InvalidMatrixException.class, timeout = 4000)
    public void testDeterminantNonSquare() {
        RealMatrixImpl m = new RealMatrixImpl(2, 3);
        m.getDeterminant();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTraceNonSquare() {
        RealMatrixImpl m = new RealMatrixImpl(3, 2);
        m.getTrace();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testOperateDimensionMismatch() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.operate(new double[]{1.0});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPreMultiplyDimensionMismatch() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.preMultiply(new double[]{1.0, 2.0, 3.0});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveVectorDimensionMismatch() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.solve(new double[]{1.0});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveMatrixRowDimensionMismatch() {
        RealMatrixImpl m1 = new RealMatrixImpl(2, 2);
        RealMatrixImpl m2 = new RealMatrixImpl(3, 1);
        m1.solve(m2);
    }

    @Test(expected = InvalidMatrixException.class, timeout = 4000)
    public void testSolveMatrixNonSquare() {
        RealMatrixImpl m1 = new RealMatrixImpl(2, 3);
        RealMatrixImpl m2 = new RealMatrixImpl(2, 1);
        m1.solve(m2);
    }

    @Test(expected = InvalidMatrixException.class, timeout = 4000)
    public void testLuDecomposeNonSquare() {
        RealMatrixImpl m = new RealMatrixImpl(2, 3);
        m.luDecompose();
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testToString() {
        RealMatrixImpl empty = new RealMatrixImpl();
        assertEquals("RealMatrixImpl{}", empty.toString());

        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        assertEquals("RealMatrixImpl{{1.0,2.0},{3.0,4.0}}", m.toString());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        double[][] d1 = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] d2 = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] dDiff = {{1.0, 2.0}, {3.0, 5.0}};
        RealMatrixImpl m1 = new RealMatrixImpl(d1);
        RealMatrixImpl m2 = new RealMatrixImpl(d2);
        RealMatrixImpl mDiff = new RealMatrixImpl(dDiff);
        RealMatrixImpl mDimensionDiff = new RealMatrixImpl(2, 3);

        // Reflexive
        assertTrue(m1.equals(m1));

        // Symmetric & Equal
        assertTrue(m1.equals(m2));
        assertTrue(m2.equals(m1));
        assertEquals(m1.hashCode(), m2.hashCode());

        // Incompatible types
        assertFalse(m1.equals(null));
        assertFalse(m1.equals("Not a matrix"));

        // Different dimensions or values
        assertFalse(m1.equals(mDimensionDiff));
        assertFalse(m1.equals(mDiff));
    }

    @Test(timeout = 4000)
    public void testCopyContract() {
        double[][] d = {{1.0, 2.0}, {3.0, 4.0}};
        RealMatrixImpl m = new RealMatrixImpl(d);
        RealMatrix copy = m.copy();

        assertEquals(m, copy);
        assertNotSame(m.getDataRef(), ((RealMatrixImpl) copy).getDataRef());

        // Verify full independence
        double[][] copyDataRef = ((RealMatrixImpl) copy).getDataRef();
        copyDataRef[0][0] = 12345.0;
        assertFalse(m.equals(copy));
    }
}