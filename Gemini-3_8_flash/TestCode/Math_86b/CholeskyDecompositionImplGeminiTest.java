package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/* [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Target Class: org.apache.commons.math.linear.CholeskyDecompositionImpl
 *
 * 1. Constructor Branches & Invariants:
 *    - matrix.isSquare() == false -> throws NonSquareMatrixException
 *    - matrix.isSquare() == true
 *    - lTData[i][i] < absolutePositivityThreshold -> throws NotPositiveDefiniteMatrixException
 *    - |lIJ - lJI| > maxDelta (relativeSymmetryThreshold check) -> throws NotSymmetricMatrixException
 *    - |lIJ - lJI| <= maxDelta -> matrix symmetric, sets lower off-diagonal element to 0
 *    - Transformation loop:
 *      * Diagonal update: ltI[i] = Math.sqrt(ltI[i])
 *      * Defect Zone (MATH-274): When a matrix has positive initial diagonal elements but is
 *        NOT positive-definite, ltQ[p] -= ltI[q] * ltI[p] leads to non-positive diagonal entries.
 *        The implementation MUST fail with NotPositiveDefiniteMatrixException.
 *
 * 2. Accessors & Caching:
 *    - getL(): Computes transpose of LT, caches result; subsequent calls return cached reference.
 *    - getLT(): Creates RealMatrix from lTData, caches result; subsequent calls return cached reference.
 *    - getDeterminant(): Product of squares of diagonal elements of L^T.
 *    - getSolver(): Returns Solver instance with internal lTData reference.
 *
 * 3. Solver Branches & Invariants:
 *    - isNonSingular(): Always returns true.
 *    - solve(double[] b):
 *      * b.length != m -> throws IllegalArgumentException
 *      * Forward and backward substitution
 *    - solve(RealVector b):
 *      * Instance of RealVectorImpl -> delegates directly to solve((RealVectorImpl) b)
 *      * Non-RealVectorImpl -> throws ClassCastException, falls back to manual resolution
 *      * Dimension mismatch in fallback -> throws IllegalArgumentException
 *    - solve(RealVectorImpl b):
 *      * Returns RealVectorImpl using solve(b.getDataRef())
 *    - solve(RealMatrix b):
 *      * b.getRowDimension() != m -> throws IllegalArgumentException
 *      * Matrix forward and backward substitution across all columns
 *    - getInverse(): Solves system with identity matrix of dimension m.
 * =========================================================================================
 */
public class CholeskyDecompositionImplGeminiTest {

    private static final double EPSILON = 1.0e-11;

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDecomposition3x3Standard() {
        // A symmetric positive-definite 3x3 matrix
        // L = [[2, 0, 0], [6, 1, 0], [-8, 5, 3]]
        // A = L * L^T =
        // [  4,  12, -16 ]
        // [ 12,  37, -43 ]
        // [-16, -43,  98 ]
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {  4.0,  12.0, -16.0 },
            { 12.0,  37.0, -43.0 },
            { -16.0, -43.0,  98.0 }
        });

        CholeskyDecomposition cholesky = new CholeskyDecompositionImpl(matrix);

        RealMatrix l = cholesky.getL();
        RealMatrix lt = cholesky.getLT();

        // Verify L is lower triangular
        assertEquals(2.0, l.getEntry(0, 0), EPSILON);
        assertEquals(0.0, l.getEntry(0, 1), EPSILON);
        assertEquals(0.0, l.getEntry(0, 2), EPSILON);
        assertEquals(6.0, l.getEntry(1, 0), EPSILON);
        assertEquals(1.0, l.getEntry(1, 1), EPSILON);
        assertEquals(0.0, l.getEntry(1, 2), EPSILON);
        assertEquals(-8.0, l.getEntry(2, 0), EPSILON);
        assertEquals(5.0, l.getEntry(2, 1), EPSILON);
        assertEquals(3.0, l.getEntry(2, 2), EPSILON);

        // Verify A = L * L^T
        RealMatrix reconstructed = l.multiply(lt);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), EPSILON);
            }
        }

        // Verify Determinant: det(L)^2 = (2 * 1 * 3)^2 = 36.0
        assertEquals(36.0, cholesky.getDeterminant(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testGetLCachingAndGetLTCaching() {
        RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(3);
        CholeskyDecomposition cholesky = new CholeskyDecompositionImpl(matrix);

        RealMatrix l1 = cholesky.getL();
        RealMatrix l2 = cholesky.getL();
        assertSame("Subsequent calls to getL() should return cached reference", l1, l2);

        RealMatrix lt1 = cholesky.getLT();
        RealMatrix lt2 = cholesky.getLT();
        assertSame("Subsequent calls to getLT() should return cached reference", lt1, lt2);
    }

    @Test(timeout = 4000)
    public void testSolverSystemDoubleArray() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {  4.0,  12.0, -16.0 },
            { 12.0,  37.0, -43.0 },
            { -16.0, -43.0,  98.0 }
        });
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();

        // Target: x = [1.0, 2.0, 3.0]^T -> b = A * x = [-20.0, -43.0, 192.0]^T
        double[] b = new double[] { -20.0, -43.0, 192.0 };
        double[] x = solver.solve(b);

        assertEquals(3, x.length);
        assertEquals(1.0, x[0], EPSILON);
        assertEquals(2.0, x[1], EPSILON);
        assertEquals(3.0, x[2], EPSILON);

        // Ensure original array b is untouched
        assertEquals(-20.0, b[0], EPSILON);
        assertEquals(-43.0, b[1], EPSILON);
        assertEquals(192.0, b[2], EPSILON);
    }

    @Test(timeout = 4000)
    public void testSolverSystemRealVectorImpl() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {  4.0,  12.0, -16.0 },
            { 12.0,  37.0, -43.0 },
            { -16.0, -43.0,  98.0 }
        });
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();

        RealVectorImpl b = new RealVectorImpl(new double[] { -20.0, -43.0, 192.0 });
        RealVector x = solver.solve(b);

        assertTrue(x instanceof RealVectorImpl);
        assertEquals(1.0, x.getEntry(0), EPSILON);
        assertEquals(2.0, x.getEntry(1), EPSILON);
        assertEquals(3.0, x.getEntry(2), EPSILON);
    }

    @Test(timeout = 4000)
    public void testSolverSystemRealMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {  4.0,  12.0, -16.0 },
            { 12.0,  37.0, -43.0 },
            { -16.0, -43.0,  98.0 }
        });
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();

        // Solve A * X = [b1 | b2]
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
            { -20.0,  -4.0 },
            { -43.0,  10.0 },
            { 192.0,  61.0 }
        });
        RealMatrix x = solver.solve(b);

        assertEquals(3, x.getRowDimension());
        assertEquals(2, x.getColumnDimension());

        // Col 0: [1, 2, 3]^T
        assertEquals(1.0, x.getEntry(0, 0), EPSILON);
        assertEquals(2.0, x.getEntry(1, 0), EPSILON);
        assertEquals(3.0, x.getEntry(2, 0), EPSILON);

        // Verify A * X == B
        RealMatrix ax = matrix.multiply(x);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals(b.getEntry(i, j), ax.getEntry(i, j), EPSILON);
            }
        }
    }

    @Test(timeout = 4000)
    public void testGetInverse() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {  4.0,  12.0, -16.0 },
            { 12.0,  37.0, -43.0 },
            { -16.0, -43.0,  98.0 }
        });
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();
        RealMatrix inverse = solver.getInverse();

        RealMatrix identity = matrix.multiply(inverse);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(i == j ? 1.0 : 0.0, identity.getEntry(i, j), EPSILON);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void test1x1Matrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] { { 9.0 } });
        CholeskyDecomposition decomp = new CholeskyDecompositionImpl(matrix);

        assertEquals(3.0, decomp.getL().getEntry(0, 0), EPSILON);
        assertEquals(3.0, decomp.getLT().getEntry(0, 0), EPSILON);
        assertEquals(9.0, decomp.getDeterminant(), EPSILON);

        DecompositionSolver solver = decomp.getSolver();
        double[] x = solver.solve(new double[] { 27.0 });
        assertEquals(3.0, x[0], EPSILON);

        RealMatrix inv = solver.getInverse();
        assertEquals(1.0 / 9.0, inv.getEntry(0, 0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testCustomThresholdsAcceptSlightAsymmetry() {
        // Off-diagonal difference is 0.005, relative error w.r.t 1.0 is 0.005
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 2.0, 1.005 },
            { 1.0, 2.0 }
        });
        // Default threshold 1.0e-15 would reject it; custom threshold 0.01 must accept it
        CholeskyDecomposition decomp = new CholeskyDecompositionImpl(matrix, 0.01, 1.0e-10);
        assertNotNull(decomp.getL());
    }

    @Test(timeout = 4000, expected = NotPositiveDefiniteMatrixException.class)
    public void testCustomPositivityThresholdRejectsSmallDiagonal() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0e-4, 0.0 },
            { 0.0, 1.0e-4 }
        });
        // absolutePositivityThreshold = 1.0e-3 > 1.0e-4 -> must trigger NotPositiveDefiniteMatrixException
        new CholeskyDecompositionImpl(matrix, 1.0e-15, 1.0e-3);
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (MATH-274 / Indefinite Matrices)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000, expected = NotPositiveDefiniteMatrixException.class)
    public void testMath274() {
        // Targets known defect MATH-274:
        // Matrix has positive initial diagonal elements (1.0, 1.0) and is symmetric,
        // but determinant = 1 - 4 = -3 < 0 (indefinite matrix).
        // Cholesky transformation must detect non-positive pivot and throw NotPositiveDefiniteMatrixException.
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 2.0, 1.0 }
        });
        new CholeskyDecompositionImpl(matrix);
    }

    @Test(timeout = 4000, expected = NotPositiveDefiniteMatrixException.class)
    public void testNotPositiveDefiniteGroundTruth() {
        // Ground truth regression test from Defects4J suite
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 14.0, 11.0, 13.0, 15.0, 24.0 },
            { 11.0, 34.0, 13.0,  8.0, 25.0 },
            { 13.0, 13.0, 14.0, 15.0, 21.0 },
            { 15.0,  8.0, 15.0, 18.0, 23.0 },
            { 24.0, 25.0, 21.0, 23.0, 45.0 }
        });
        new CholeskyDecompositionImpl(matrix);
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000, expected = NonSquareMatrixException.class)
    public void testNonSquareMatrixThrowsException() {
        RealMatrix nonSquare = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 0.0, 0.0 },
            { 0.0, 1.0, 0.0 }
        });
        new CholeskyDecompositionImpl(nonSquare);
    }

    @Test(timeout = 4000, expected = NotSymmetricMatrixException.class)
    public void testAsymmetricMatrixThrowsException() {
        RealMatrix asymmetric = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 0.0, 1.0 }
        });
        new CholeskyDecompositionImpl(asymmetric);
    }

    @Test(timeout = 4000, expected = NotPositiveDefiniteMatrixException.class)
    public void testInitialNegativeDiagonalThrowsException() {
        RealMatrix negativeDiag = MatrixUtils.createRealMatrix(new double[][] {
            { -2.0, 0.0 },
            {  0.0, 1.0 }
        });
        new CholeskyDecompositionImpl(negativeDiag);
    }

    @Test(timeout = 4000, expected = NotPositiveDefiniteMatrixException.class)
    public void testInitialZeroDiagonalThrowsException() {
        RealMatrix zeroDiag = MatrixUtils.createRealMatrix(new double[][] {
            { 0.0, 0.0 },
            { 0.0, 1.0 }
        });
        new CholeskyDecompositionImpl(zeroDiag);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSolverVectorDimensionMismatchDoubleArray() {
        RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(3);
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();
        solver.solve(new double[] { 1.0, 2.0 });
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSolverVectorDimensionMismatchRealVectorImpl() {
        RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(3);
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();
        solver.solve(new RealVectorImpl(new double[] { 1.0, 2.0 }));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSolverMatrixDimensionMismatchRealMatrix() {
        RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(3);
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();
        solver.solve(MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        }));
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle, Fallback Branches & Invariant Verification
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSolverIsNonSingularAlwaysTrue() {
        RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(2);
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();
        assertTrue(solver.isNonSingular());
    }

    @Test(timeout = 4000)
    public void testSolverSolveGenericRealVectorViaClassCastFallback() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {  4.0,  12.0, -16.0 },
            { 12.0,  37.0, -43.0 },
            { -16.0, -43.0,  98.0 }
        });
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();

        final double[] data = new double[] { -20.0, -43.0, 192.0 };
        RealVector customVector = (RealVector) Proxy.newProxyInstance(
            RealVector.class.getClassLoader(),
            new Class<?>[] { RealVector.class },
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
                    if ("getDimension".equals(method.getName())) {
                        return data.length;
                    }
                    if ("getData".equals(method.getName())) {
                        return data.clone();
                    }
                    return null;
                }
            }
        );

        // Forces solve(RealVector) into the catch (ClassCastException cce) block
        RealVector result = solver.solve(customVector);

        assertNotNull(result);
        assertEquals(3, result.getDimension());
        assertEquals(1.0, result.getEntry(0), EPSILON);
        assertEquals(2.0, result.getEntry(1), EPSILON);
        assertEquals(3.0, result.getEntry(2), EPSILON);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSolverGenericRealVectorDimensionMismatchInFallback() {
        RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(3);
        DecompositionSolver solver = new CholeskyDecompositionImpl(matrix).getSolver();

        final double[] wrongData = new double[] { 1.0, 2.0 };
        RealVector customVector = (RealVector) Proxy.newProxyInstance(
            RealVector.class.getClassLoader(),
            new Class<?>[] { RealVector.class },
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
                    if ("getDimension".equals(method.getName())) {
                        return wrongData.length;
                    }
                    if ("getData".equals(method.getName())) {
                        return wrongData.clone();
                    }
                    return null;
                }
            }
        );

        solver.solve(customVector);
    }
}