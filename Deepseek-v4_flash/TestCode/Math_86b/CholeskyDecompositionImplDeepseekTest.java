package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box JUnit 4 test suite for CholeskyDecompositionImpl.
 * Targets maximum line/branch coverage and the known defect where
 * NotPositiveDefiniteMatrixException is not thrown for matrices that
 * become non-positive definite during the decomposition (e.g., symmetric
 * but not positive definite with all initial diagonal > threshold).
 *
 * [Branch & Defect Analysis Matrix]
 * 1. Non-square matrix: rowCount != colCount -> NonSquareMatrixException
 * 2. Non-symmetric matrix: |a_ij - a_ji| > relativeSymmetryThreshold *
 *    max(|a_ij|, |a_ji|) -> NotSymmetricMatrixException
 * 3. Initial diagonal positivity: a_ii < absolutePositivityThreshold ->
 *    NotPositiveDefiniteMatrixException (covers zero/negative/small positive)
 * 4. Defect: after updating submatrix during Cholesky, a diagonal may
 *    become negative -> should throw NotPositiveDefiniteMatrixException
 *    but current code does not check. This is triggered by e.g. [[1,2],[2,1]].
 * 5. Normal positive definite matrix: decomposition succeeds, L and LT cached,
 *    determinant computed, solver works.
 * 6. Solver paths: solve(double[]), solve(RealVector), solve(RealMatrix),
 *    getInverse(), isNonSingular().
 * 7. Threshold boundaries: default values, custom thresholds.
 * 8. Edge: zero matrix? (diagonal 0 < threshold -> exception)
 * 9. Edge: identity matrix positive definite.
 * 10. Caching: getL and getLT return the same object after first call.
 */
public class CholeskyDecompositionImplDeepseekTest {

    // ---------- Partition A: Core Functional Logic & State ----------
    @Test(timeout = 4000)
    public void testPositiveDefiniteMatrix() {
        double[][] data = {{4, 2}, {2, 3}}; // positive definite
        RealMatrix m = new Array2DRowRealMatrix(data);
        CholeskyDecompositionImpl cd = new CholeskyDecompositionImpl(m);
        RealMatrix L = cd.getL();
        RealMatrix LT = cd.getLT();
        // Check that L is lower triangular with positive diagonal
        assertEquals(2, L.getRowDimension());
        assertEquals(2, L.getColumnDimension());
        assertEquals(1.0, L.getEntry(0,0) * L.getEntry(0,0), 1e-14); // L[0][0]=sqrt(4)=2
        assertEquals(1.0, L.getEntry(1,1) * L.getEntry(1,1), 1e-14); // sqrt(3-1)=...
        assertEquals(0.0, L.getEntry(0,1), 1e-15);
        // Verify A = L * LT
        RealMatrix product = L.multiply(LT);
        assertEquals(4.0, product.getEntry(0,0), 1e-14);
        assertEquals(2.0, product.getEntry(0,1), 1e-14);
        assertEquals(2.0, product.getEntry(1,0), 1e-14);
        assertEquals(3.0, product.getEntry(1,1), 1e-14);
        // Determinant
        double det = cd.getDeterminant();
        // det = product of squares of diagonal of L (since L*LT) = (2^2)*(1^2) = 4? Actually L[0][0]=2, L[1][1]=sqrt(1)=1, so det = (2*2)*(1*1)=4? But original determinant = 4*3-2*2=8. Hmm, the determinant of A is product of squares of L diagonal? Yes for Cholesky, det(A) = (∏ L_ii)^2. So det = (2 * 1)^2 = 4. That's wrong? Wait, compute L: L = [[2,0],[1,?]]. After decomposition: L[0][0]=2, L[1][0]=1, L[1][1]=sqrt(3-1)=sqrt(2)≈1.4142. So product of diagonals = 2*1.4142=2.828, squared = 8. Good. So our test must use correct L. Let's compute properly: after decomposition, L is:
        // L = [[2,0],[1, sqrt(2)]]; determinant = (2 * sqrt(2))^2 = 8.
        assertEquals(8.0, det, 1e-12);
        // Solver
        double[] b = {1, 1};
        DecompositionSolver solver = cd.getSolver();
        double[] x = solver.solve(b);
        // A * x = b => x = A^{-1}*b. Check by multiplication.
        RealMatrix A = new Array2DRowRealMatrix(data);
        RealMatrix xMatrix = new Array2DRowRealMatrix(new double[][]{x});
        RealMatrix bComputed = A.multiply(xMatrix.transpose()).transpose();
        assertEquals(1.0, bComputed.getEntry(0,0), 1e-12);
        assertEquals(1.0, bComputed.getEntry(0,1), 1e-12);
        // isNonSingular always true for positive definite
        assertTrue(solver.isNonSingular());
        // getInverse
        RealMatrix inv = solver.getInverse();
        RealMatrix I = inv.multiply(A);
        assertEquals(1.0, I.getEntry(0,0), 1e-12);
        assertEquals(0.0, I.getEntry(0,1), 1e-12);
        assertEquals(0.0, I.getEntry(1,0), 1e-12);
        assertEquals(1.0, I.getEntry(1,1), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetLTAndGetLAreCached() {
        double[][] data = {{9}};
        RealMatrix m = new Array2DRowRealMatrix(data);
        CholeskyDecompositionImpl cd = new CholeskyDecompositionImpl(m);
        RealMatrix lt1 = cd.getLT();
        RealMatrix lt2 = cd.getLT();
        assertSame("LT should be cached", lt1, lt2);
        RealMatrix l1 = cd.getL();
        RealMatrix l2 = cd.getL();
        assertSame("L should be cached", l1, l2);
        // L is transpose of LT for 1x1
        assertEquals(lt1.getEntry(0,0), l1.getEntry(0,0), 0.0);
    }

    @Test(timeout = 4000)
    public void testSingleElementMatrix() {
        double[][] data = {{5.0}};
        RealMatrix m = new Array2DRowRealMatrix(data);
        CholeskyDecompositionImpl cd = new CholeskyDecompositionImpl(m);
        assertEquals(Math.sqrt(5), cd.getL().getEntry(0,0), 1e-15);
        assertEquals(5.0, cd.getDeterminant(), 1e-15);
    }

    // ---------- Partition B: Boundary Value Analysis ----------
    @Test(timeout = 4000)
    public void testNonSquareMatrix() {
        double[][] data = {{1,2,3},{4,5,6}};
        RealMatrix m = new Array2DRowRealMatrix(data);
        try {
            new CholeskyDecompositionImpl(m);
            fail("Expected NonSquareMatrixException");
        } catch (NonSquareMatrixException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNotSymmetricMatrix() {
        double[][] data = {{1,2},{3,4}}; // a12=2, a21=3 => diff=1, max=3, threshold 1e-15 => |diff| >> max*threshold => exception
        RealMatrix m = new Array2DRowRealMatrix(data);
        try {
            new CholeskyDecompositionImpl(m);
            fail("Expected NotSymmetricMatrixException");
        } catch (NotSymmetricMatrixException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDiagonalBelowPositivityThreshold() {
        double[][] data = {{1e-12}}; // 1e-12 < 1e-10 -> throw
        RealMatrix m = new Array2DRowRealMatrix(data);
        try {
            new CholeskyDecompositionImpl(m);
            fail("Expected NotPositiveDefiniteMatrixException");
        } catch (NotPositiveDefiniteMatrixException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDiagonalEqualToThreshold() {
        double[][] data = {{1e-10}}; // exactly threshold? condition is <, so equal should pass
        RealMatrix m = new Array2DRowRealMatrix(data);
        CholeskyDecompositionImpl cd = new CholeskyDecompositionImpl(m);
        assertEquals(Math.sqrt(1e-10), cd.getL().getEntry(0,0), 1e-20);
    }

    @Test(timeout = 4000)
    public void testDiagonalJustAboveThreshold() {
        double[][] data = {{1e-9}}; // > threshold
        RealMatrix m = new Array2DRowRealMatrix(data);
        CholeskyDecompositionImpl cd = new CholeskyDecompositionImpl(m);
        assertEquals(Math.sqrt(1e-9), cd.getL().getEntry(0,0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testRelativeSymmetryThresholdAllowsNearSymmetric() {
        double delta = 1e-14; // very small difference
        double max = 1.0;
        double[][] data = {{1, 1.0}, {1+delta, 2}};
        RealMatrix m = new Array2DRowRealMatrix(data);
        // default relativeSymmetryThreshold = 1e-15
        // maxDelta = 1e-15 * max(1.0, 1+delta) ≈ 1e-15, delta=1e-14 > maxDelta => exception
        try {
            new CholeskyDecompositionImpl(m);
            fail("Expected NotSymmetricMatrixException");
        } catch (NotSymmetricMatrixException e) {
            // expected
        }
    }

    // With custom thresholds, allow larger difference
    @Test(timeout = 4000)
    public void testCustomRelativeSymmetryThreshold() {
        double delta = 1e-12;
        double[][] data = {{1, 1.0}, {1+delta, 2}};
        RealMatrix m = new Array2DRowRealMatrix(data);
        // use threshold = 1e-11, maxDelta = 1e-11*1=1e-11, delta=1e-12 <= 1e-11, so symmetric check passes
        CholeskyDecompositionImpl cd = new CholeskyDecompositionImpl(m, 1e-11, 1e-10);
        // But the matrix might not be positive definite? Check: eigenvalues? Actually 2x2 symmetric with a=1,b=1,c=2. Det=2-1=1>0, positive definite. So it should work.
        // Let's just ensure no exception.
        assertNotNull(cd.getL());
    }

    // ---------- Partition C: Defect-Targeted Branch Zone ----------
    // This test targets the known defect: a symmetric matrix that passes the initial
    // diagonal check but becomes non-positive during the Cholesky algorithm.
    @Test(timeout = 4000)
    public void testMatrixNotPositiveDefiniteButPassesInitialCheck() {
        // Matrix [[1, 2], [2, 1]] is symmetric, eigenvalues 3 and -1, not positive definite.
        // Initial diagonals are 1 and 1, both > 1e-10, so initial check passes.
        // After first step: L[0][0]=1, L[0][1]=2, submatrix updated: new (1,1) = 1 - 2*2 = -3.
        // Next, diagonal is negative -> should throw NotPositiveDefiniteMatrixException.
        double[][] data = {{1, 2}, {2, 1}};
        RealMatrix m = new Array2DRowRealMatrix(data);
        try {
            new CholeskyDecompositionImpl(m);
            fail("Expected NotPositiveDefiniteMatrixException for non-positive definite matrix that initially has positive diagonals");
        } catch (NotPositiveDefiniteMatrixException e) {
            // expected -- this is the defect revealer
        }
    }

    // Additional similar case with larger matrix
    @Test(timeout = 4000)
    public void testMatrixNotPositiveDefinite3x3() {
        double[][] data = {
            {1, 0.5, 0.5},
            {0.5, 1, 0.5},
            {0.5, 0.5, 1}
        };
        // This matrix is positive definite? Let's check eigenvalues: All ones? Actually it's a correlation matrix with off-diagonals=0.5, eigenvalues: 2, 0.5, 0.5? Wait compute? Actually all ones on diag, so trace=3. Off diag=0.5. The Cholesky will likely succeed. Not defect.
        // Instead use a known indefinite matrix: e.g., [[1, 0.9], [0.9, 1]] is indefinite if determinant <0? 1*1-0.81=0.19>0, positive definite. Need eigenvalue negative. Use [[2,3],[3,2]]? det=4-9=-5 <0, indefinite. But initial diagonals 2,2 > threshold. So should throw.
        double[][] data2 = {{2,3},{3,2}};
        RealMatrix m = new Array2DRowRealMatrix(data2);
        try {
            new CholeskyDecompositionImpl(m);
            fail("Expected NotPositiveDefiniteMatrixException");
        } catch (NotPositiveDefiniteMatrixException e) {
            // expected
        }
    }

    // ---------- Partition D: Exception & Defensive Guard Paths ----------
    @Test(timeout = 4000)
    public void testSolveDoubleVectorDimensionMismatch() {
        double[][] data = {{9}};
        RealMatrix m = new Array2DRowRealMatrix(data);
        CholeskyDecompositionImpl cd = new CholeskyDecompositionImpl(m);
        DecompositionSolver solver = cd.getSolver();
        double[] b = {1,2}; // length 2 != 1
        try {
            solver.solve(b);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSolveRealVectorDimensionMismatch() {
        double[][] data = {{4,2},{2,3}};
        RealMatrix m = new Array2DRowRealMatrix(data);
        CholeskyDecompositionImpl cd = new CholeskyDecompositionImpl(m);
        DecompositionSolver solver = cd.getSolver();
        RealVector b = new RealVectorImpl(new double[]{1,2,3}); // length 3 != 2
        try {
            solver.solve(b);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSolveRealMatrixDimensionMismatch() {
        double[][] data = {{9}};
        RealMatrix m = new Array2DRowRealMatrix(data);
        CholeskyDecompositionImpl cd = new CholeskyDecompositionImpl(m);
        DecompositionSolver solver = cd.getSolver();
        RealMatrix b = new Array2DRowRealMatrix(new double[][]{{1,2}}); // rows=1, columns=2; column dimension doesn't cause mismatch, row dimension matches (1). Actually row dimension of b = 1 matches m dimension. So it's okay. To cause mismatch, use b with row dimension != m order.
        RealMatrix b2 = new Array2DRowRealMatrix(new double[][]{{1},{2}}); // 2 rows, 1 col -> row dimension 2 != 1
        try {
            solver.solve(b2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithRealVectorImpl() {
        double[][] data = {{4,2},{2,3}};
        RealMatrix m = new Array2DRowRealMatrix(data);
        CholeskyDecompositionImpl cd = new CholeskyDecompositionImpl(m);
        DecompositionSolver solver = cd.getSolver();
        RealVectorImpl b = new RealVectorImpl(new double[]{1,1});
        RealVectorImpl x = solver.solve(b);
        RealMatrix A = new Array2DRowRealMatrix(data);
        RealMatrix bCheck = A.multiply(new Array2DRowRealMatrix(x.getData(), 2));
        assertEquals(1.0, bCheck.getEntry(0,0), 1e-12);
        assertEquals(1.0, bCheck.getEntry(1,0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testIsNonSingularAlwaysTrue() {
        double[][] data = {{9}};
        RealMatrix m = new Array2DRowRealMatrix(data);
        CholeskyDecompositionImpl cd = new CholeskyDecompositionImpl(m);
        assertTrue(cd.getSolver().isNonSingular());
    }

    // ---------- Partition E: Object Lifecycle & Contract Integrity ----------
    // No custom equals/hashCode; but we can test consistency of results.
    @Test(timeout = 4000)
    public void testDeterminantForIdentity() {
        double[][] data = {{1,0},{0,1}};
        RealMatrix m = new Array2DRowRealMatrix(data);
        CholeskyDecompositionImpl cd = new CholeskyDecompositionImpl(m);
        assertEquals(1.0, cd.getDeterminant(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDeterminantAfterSolverInverse() {
        double[][] data = {{4,2},{2,3}};
        RealMatrix m = new Array2DRowRealMatrix(data);
        CholeskyDecompositionImpl cd = new CholeskyDecompositionImpl(m);
        double det = cd.getDeterminant();
        // Inverse should have determinant 1/det
        RealMatrix inv = cd.getSolver().getInverse();
        double invDet = new LUDecompositionImpl(inv).getDeterminant(); // use LU to compute det
        assertEquals(1.0, det * invDet, 1e-12);
    }

    // Additional edge: empty matrix? Not possible since order >=1.
}