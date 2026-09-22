package org.apache.commons.math3.linear;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.apache.commons.math3.linear.RectangularCholeskyDecomposition
 * Tested Branches & Decision Points:
 * 1. Pivot search loop: swap[r] initialized to r; loop i from r+1 to order.
 *    - Comparison: c[ii][ii] > c[isi][isi] (both true and false outcomes).
 *    - DEFECT (MATH-789): Reading index[swap[i]] instead of index[swap[r]].
 * 2. Pivot swap decision:
 *    - swap[r] != r (true -> elements in index array swapped; false -> already in place).
 * 3. Diagonal element threshold check:
 *    - c[ir][ir] < small:
 *      * Sub-branch r == 0: Triggers initial NonPositiveDefiniteMatrixException.
 *      * Sub-branch r > 0: Loop i from r to order.
 *        - c[index[i]][index[i]] < -small: Triggers NonPositiveDefiniteMatrixException for indefinite matrix.
 *        - c[index[i]][index[i]] >= -small: Matrix is positive semidefinite with rank r. Loop terminates.
 *    - c[ir][ir] >= small: Normal transformation path; computes sqrt and updates trailing submatrix.
 * 4. Submatrix update nested loops:
 *    - i from r+1 to order.
 *    - j from r+1 to i (covers 0 iterations when i = r+1 and >=1 iterations when i > r+1).
 * 5. Loop continuation:
 *    - ++r < order (true -> continue decomposition, false -> full rank reached).
 * 6. Getters:
 *    - getRootMatrix() dimension and contents (order x rank).
 *    - getRank() verification.
 * ====================================================================================================
 */
public class RectangularCholeskyDecompositionGeminiTest {

    // ================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ================================================================================================

    @Test(timeout = 4000)
    public void testOrderOnePositiveMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0 }
        });
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);

        assertEquals(1, rcd.getRank());
        RealMatrix root = rcd.getRootMatrix();
        assertEquals(1, root.getRowDimension());
        assertEquals(1, root.getColumnDimension());
        assertEquals(2.0, root.getEntry(0, 0), 1.0e-12);

        RealMatrix rebuilt = root.multiply(root.transpose());
        assertEquals(0.0, matrix.subtract(rebuilt).getNorm(), 1.0e-12);
    }

    @Test(timeout = 4000)
    public void testIdentityMatrixFullRank() {
        RealMatrix identity = MatrixUtils.createRealIdentityMatrix(3);
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(identity, 1.0e-10);

        assertEquals(3, rcd.getRank());
        RealMatrix root = rcd.getRootMatrix();
        assertEquals(3, root.getRowDimension());
        assertEquals(3, root.getColumnDimension());

        RealMatrix rebuilt = root.multiply(root.transpose());
        assertEquals(0.0, identity.subtract(rebuilt).getNorm(), 1.0e-12);
    }

    @Test(timeout = 4000)
    public void testDiagonalMatrixDecreasingPivots() {
        // Naturally sorted diagonal: swap[r] == r for all steps
        RealMatrix matrix = MatrixUtils.createRealDiagonalMatrix(new double[] { 9.0, 4.0, 1.0 });
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);

        assertEquals(3, rcd.getRank());
        RealMatrix root = rcd.getRootMatrix();
        RealMatrix rebuilt = root.multiply(root.transpose());
        assertEquals(0.0, matrix.subtract(rebuilt).getNorm(), 1.0e-12);
    }

    @Test(timeout = 4000)
    public void testDiagonalMatrixIncreasingPivots() {
        // Reverse sorted diagonal: swap[r] != r branch is exercised
        RealMatrix matrix = MatrixUtils.createRealDiagonalMatrix(new double[] { 1.0, 4.0, 9.0 });
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);

        assertEquals(3, rcd.getRank());
        RealMatrix root = rcd.getRootMatrix();
        RealMatrix rebuilt = root.multiply(root.transpose());
        assertEquals(0.0, matrix.subtract(rebuilt).getNorm(), 1.0e-12);
    }

    @Test(timeout = 4000)
    public void testRankDeficientPositiveSemidefiniteRank1Of2() {
        // [1 1]
        // [1 1]
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 1.0 },
            { 1.0, 1.0 }
        });
        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);

        assertEquals(1, rcd.getRank());
        RealMatrix root = rcd.getRootMatrix();
        assertEquals(2, root.getRowDimension());
        assertEquals(1, root.getColumnDimension());

        RealMatrix rebuilt = root.multiply(root.transpose());
        assertEquals(0.0, matrix.subtract(rebuilt).getNorm(), 1.0e-12);
    }

    @Test(timeout = 4000)
    public void testRankDeficientPositiveSemidefiniteRank2Of3() {
        // B = [[2, 1], [1, 2], [3, 3]] -> M = B * B^T of order 3, rank 2
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.0 },
            { 1.0, 2.0 },
            { 3.0, 3.0 }
        });
        RealMatrix matrix = b.multiply(b.transpose());

        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);
        assertEquals(2, rcd.getRank());

        RealMatrix root = rcd.getRootMatrix();
        assertEquals(3, root.getRowDimension());
        assertEquals(2, root.getColumnDimension());

        RealMatrix rebuilt = root.multiply(root.transpose());
        assertEquals(0.0, matrix.subtract(rebuilt).getNorm(), 1.0e-10);
    }

    @Test(timeout = 4000)
    public void testMultiDimensionalInnerLoopExecution() {
        // 4x4 matrix to exercise inner update loops (j from r+1 to i) with multiple passes
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0, 1.0, 2.0 },
            { 1.0, 3.0, 1.0 },
            { 2.0, 1.0, 5.0 },
            { 1.0, 1.0, 1.0 }
        });
        RealMatrix matrix = b.multiply(b.transpose());

        RectangularCholeskyDecomposition rcd = new RectangularCholeskyDecomposition(matrix, 1.0e-10);
        assertEquals(3, rcd.getRank());

        RealMatrix root = rcd.getRootMatrix();
        assertEquals(4, root.getRowDimension());
        assertEquals(3, root.getColumnDimension());

        RealMatrix rebuilt = root.multiply(root.transpose());
        assertEquals(0.0, matrix.subtract(rebuilt).getNorm(), 1.0e-10);
    }

    // ================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================================================

    @Test(timeout = 40