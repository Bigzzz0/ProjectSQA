package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Defect Targeting (Apache Commons Math Math-13 / PolynomialFitterTest#testLargeSample):
 *    - In the defective version, AbstractLeastSquaresOptimizer#squareRoot(RealMatrix) did not check
 *      whether the weight matrix was an instance of DiagonalMatrix. It instead always instantiated an
 *      EigenDecomposition on the matrix. When dealing with large datasets (e.g. 40,000+ points),
 *      this caused a full N x N matrix allocation, throwing OutOfMemoryError: Java heap space.
 *    - The test `testLargeSampleWeightSquareRootDefect` verifies that a large DiagonalMatrix is handled
 *      in O(N) memory and time without triggering OutOfMemoryError.
 *
 * 2. Partition A: Core Functional Logic & State Transitions:
 *    - `squareRoot(RealMatrix)`: Branch `m instanceof DiagonalMatrix` vs Branch `else` (dense RealMatrix).
 *    - `computeWeightedJacobian(double[])`: Matrix multiplication of W^(1/2) * J.
 *    - `computeCost(double[])`: Weighted dot product of residuals: sqrt(r^T * W * r).
 *    - `getChiSquare()` & `setCost(double)`: Cost squaring and retrieval.
 *    - `getRMS()`: Root mean square calculation: sqrt(chiSquare / targetSize).
 *    - `getWeightSquareRoot()`: Defensive copy verification.
 *    - `computeCovariances(double[], double)`: QR decomposition of J^T * J and inversion.
 *    - `computeSigma(double[], double)`: Parameter standard deviations from diagonal covariances.
 *
 * 3. Partition B: Boundary Value Analysis (BVA) & Extremes:
 *    - Residuals and objective values of zero length.
 *    - Zero cost and zero RMS calculation.
 *    - Negative cost values handling.
 *    - Empty optimization data arrays in `optimize(OptimizationData...)`.
 *
 * 4. Partition C: Defensive & Exception Guard Paths:
 *    - `computeResiduals(double[])`: DimensionMismatchException when objectiveValue.length != target.length.
 *    - `computeCovariances(double[], double)`: SingularMatrixException on rank-deficient/singular J^T * J.
 *    - `computeSigma(double[], double)`: SingularMatrixException propagation on singular problems.
 *
 * 5. Partition D: Lifecycle & Convergence Checker:
 *    - Null vs non-null ConvergenceChecker constructor injection.
 *    - State persistence across multiple optimize() calls when Weight is omitted in subsequent calls.
 */
public class AbstractLeastSquaresOptimizerGeminiTest {

    /**
     * Concrete test implementation of AbstractLeastSquaresOptimizer.
     */
    private static class ConcreteLeastSquaresOptimizer extends AbstractLeastSquaresOptimizer {
        private double[][] customJacobian;

        ConcreteLeastSquaresOptimizer() {
            super(null);
        }

        ConcreteLeastSquaresOptimizer(ConvergenceChecker<PointVectorValuePair> checker) {
            super(checker);
        }

        void setCustomJacobian(double[][] jacobian) {
            this.customJacobian = jacobian;
        }

        @Override
        protected double[][] computeJacobian(double[] params) {
            if (customJacobian != null) {
                return customJacobian;
            }
            return super.computeJacobian(params);
        }

        @Override
        protected PointVectorValuePair doOptimize() {
            return new PointVectorValuePair(new double[0], new double[0]);
        }
    }

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Math-13 Ground Truth)
    // =========================================================================

    /**
     * Targets Math-13 / PolynomialFitterTest#testLargeSample defect.
     * When processing large datasets, passing a large DiagonalMatrix as Weight must NOT
     * trigger an EigenDecomposition (which attempts to allocate an N x N dense matrix
     * resulting in OutOfMemoryError).
     */
    @Test(timeout = 4000)
    public void testLargeSampleWeightSquareRootDefect() {
        final int sampleSize = 40000;
        final double[] weights = new double[sampleSize];
        weights[0] = 4.0;
        weights[1] = 9.0;
        weights[sampleSize - 1] = 16.0;

        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.optimize(
            new Target(new double[sampleSize]),
            new Weight(weights),
            new MaxEval(100)
        );

        RealMatrix sqrtW = optimizer.getWeightSquareRoot();
        assertNotNull(sqrtW);
        assertTrue("Weight square root matrix must remain a DiagonalMatrix", sqrtW instanceof DiagonalMatrix);
        assertEquals(sampleSize, sqrtW.getRowDimension());
        assertEquals(sampleSize, sqrtW.getColumnDimension());
        assertEquals(2.0, sqrtW.getEntry(0, 0), 1e-10);
        assertEquals(3.0, sqrtW.getEntry(1, 1), 1e-10);
        assertEquals(4.0, sqrtW.getEntry(sampleSize - 1, sampleSize - 1), 1e-10);
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testSquareRootWithDenseNonDiagonalWeightMatrix() {
        // Symmetric positive-definite matrix:
        // [ 5.0, 4.0 ]
        // [ 4.0, 5.0 ]
        RealMatrix denseWeight = new Array2DRowRealMatrix(new double[][] {
            { 5.0, 4.0 },
            { 4.0, 5.0 }
        });

        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.optimize(
            new Target(new double[] { 1.0, 2.0 }),
            new Weight(denseWeight),
            new MaxEval(100)
        );

        RealMatrix sqrtW = optimizer.getWeightSquareRoot();
        assertNotNull(sqrtW);
        assertFalse(sqrtW instanceof DiagonalMatrix);

        // Verification: W^(1/2) * W^(1/2) == W
        RealMatrix reconstructed = sqrtW.multiply(sqrtW);
        assertEquals(5.0, reconstructed.getEntry(0, 0), 1e-8);
        assertEquals(4.0, reconstructed.getEntry(0, 1), 1e-8);
        assertEquals(4.0, reconstructed.getEntry(1, 0), 1e-8);
        assertEquals(5.0, reconstructed.getEntry(1, 1), 1e-8);
    }

    @Test(timeout = 4000)
    public void testComputeWeightedJacobian() {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.optimize(
            new Target(new double[] { 0.0, 0.0 }),
            new Weight(new double[] { 4.0, 9.0 }),
            new MaxEval(10)
        );

        // Sqrt weights will be diag(2.0, 3.0)
        // Set Jacobian to:
        // [ 1.0, 2.0 ]
        // [ 3.0, 4.0 ]
        optimizer.setCustomJacobian(new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        });

        RealMatrix weightedJ = optimizer.computeWeightedJacobian(new double[] { 0.5, 0.5 });
        assertNotNull(weightedJ);
        // [ 2.0, 0.0 ] * [ 1.0, 2.0 ] = [ 2.0,  4.0 ]
        // [ 0.0, 3.0 ]   [ 3.0, 4.0 ]   [ 9.0, 12.0 ]
        assertEquals(2.0, weightedJ.getEntry(0, 0), 1e-10);
        assertEquals(4.0, weightedJ.getEntry(0, 1), 1e-10);
        assertEquals(9.0, weightedJ.getEntry(1, 0), 1e-10);
        assertEquals(12.0, weightedJ.getEntry(1, 1), 1e-10);
    }

    @Test(timeout = 4000)
    public void testComputeCost() {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.optimize(
            new Target(new double[] { 0.0, 0.0 }),
            new Weight(new double[] { 3.0, 2.0 }),
            new MaxEval(10)
        );

        // Residuals r = [2.0, -1.0]
        // r^T * W * r = 2.0 * 3.0 * 2.0 + (-1.0) * 2.0 * (-1.0) = 12.0 + 2.0 = 14.0
        // cost = sqrt(14.0)
        double cost = optimizer.computeCost(new double[] { 2.0, -1.0 });
        assertEquals(FastMath.sqrt(14.0), cost, 1e-10);
    }

    @Test(timeout = 4000)
    public void testCostAndChiSquareAndRMS() {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.optimize(
            new Target(new double[] { 1.0, 2.0, 3.0, 4.0 }),
            new Weight(new double[] { 1.0, 1.0, 1.0, 1.0 }),
            new MaxEval(10)
        );

        optimizer.setCost(6.0);
        assertEquals(6.0, FastMath.sqrt(optimizer.getChiSquare()), 1e-10);
        assertEquals(36.0, optimizer.getChiSquare(), 1e-10);

        // Target size = 4
        // RMS = sqrt(chiSquare / targetSize) = sqrt(36.0 / 4) = 3.0
        assertEquals(3.0, optimizer.getRMS(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetWeightSquareRootImmutability() {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.optimize(
            new Target(new double[] { 0.0 }),
            new Weight(new double[] { 4.0 }),
            new MaxEval(10)
        );

        RealMatrix sqrtW = optimizer.getWeightSquareRoot();
        assertEquals(2.0, sqrtW.getEntry(0, 0), 1e-10);

        // Modify returned copy
        sqrtW.setEntry(0, 0, 999.0);

        // Internal matrix must remain untouched
        RealMatrix sqrtW2 = optimizer.getWeightSquareRoot();
        assertEquals(2.0, sqrtW2.getEntry(0, 0), 1e-10);
    }

    @Test(timeout = 4000)
    public void testComputeCovariancesAndSigma() {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.optimize(
            new Target(new double[] { 0.0, 0.0, 0.0 }),
            new Weight(new double[] { 1.0, 1.0, 1.0 }),
            new MaxEval(10)
        );

        // 3 observations, 2 parameters
        // J = [ 1, 0 ]
        //     [ 0, 1 ]
        //     [ 1, 1 ]
        // J^T * J = [ 2, 1 ]
        //           [ 1, 2 ]
        // (J^T * J)^(-1) = (1/3) * [  2, -1 ] = [  2/3, -1/3 ]
        //                          [ -1,  2 ]   [ -1/3,  2/3 ]
        optimizer.setCustomJacobian(new double[][] {
            { 1.0, 0.0 },
            { 0.0, 1.0 },
            { 1.0, 1.0 }
        });

        double[] params = new double[] { 0.0, 0.0 };
        double[][] cov = optimizer.computeCovariances(params, 1e-10);

        assertEquals(2, cov.length);
        assertEquals(2, cov[0].length);
        assertEquals(2.0 / 3.0, cov[0][0], 1e-10);
        assertEquals(-1.0 / 3.0, cov[0][1], 1e-10);
        assertEquals(-1.0 / 3.0, cov[1][0], 1e-10);
        assertEquals(2.0 / 3.0, cov[1][1], 1e-10);

        double[] sigma = optimizer.computeSigma(params, 1e-10);
        assertEquals(2, sigma.length);
        assertEquals(FastMath.sqrt(2.0 / 3.0), sigma[0], 1e-10);
        assertEquals(FastMath.sqrt(2.0 / 3.0), sigma[1], 1e-10);
    }

    @Test(timeout = 4000)
    public void testComputeResidualsNormal() {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.optimize(
            new Target(new double[] { 10.0, 20.0, 30.0 }),
            new Weight(new double[] { 1.0, 1.0, 1.0 }),
            new MaxEval(10)
        );

        double[] objectiveValue = new double[] { 8.0, 25.0, 30.0 };
        double[] residuals = optimizer.computeResiduals(objectiveValue);

        assertNotNull(residuals);
        assertEquals(3, residuals.length);
        assertEquals(2.0, residuals[0], 1e-10);
        assertEquals(-5.0, residuals[1], 1e-10);
        assertEquals(0.0, residuals[2], 1e-10);
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES
    // =========================================================================

    @Test(timeout = 4000)
    public void testZeroCostAndZeroResiduals() {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.optimize(
            new Target(new double[] { 5.0, 5.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new MaxEval(10)
        );

        double[] zeroResiduals = optimizer.computeResiduals(new double[] { 5.0, 5.0 });
        assertEquals(0.0, zeroResiduals[0], 1e-10);
        assertEquals(0.0, zeroResiduals[1], 1e-10);

        double cost = optimizer.computeCost(zeroResiduals);
        assertEquals(0.0, cost, 1e-10);

        optimizer.setCost(0.0);
        assertEquals(0.0, optimizer.getChiSquare(), 1e-10);
        assertEquals(0.0, optimizer.getRMS(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testNegativeCostSquareRMS() {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.optimize(
            new Target(new double[] { 1.0 }),
            new Weight(new double[] { 1.0 }),
            new MaxEval(10)
        );

        optimizer.setCost(-4.0);
        assertEquals(16.0, optimizer.getChiSquare(), 1e-10);
        assertEquals(4.0, optimizer.getRMS(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testOptimizationDataWithoutWeightKeepsPreviousState() {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.optimize(
            new Target(new double[] { 1.0 }),
            new Weight(new double[] { 9.0 }),
            new MaxEval(10)
        );

        RealMatrix sqrtW1 = optimizer.getWeightSquareRoot();
        assertEquals(3.0, sqrtW1.getEntry(0, 0), 1e-10);

        // Optimize again without specifying Weight
        optimizer.optimize(
            new Target(new double[] { 1.0 }),
            new MaxEval(20)
        );

        RealMatrix sqrtW2 = optimizer.getWeightSquareRoot();
        assertEquals(3.0, sqrtW2.getEntry(0, 0), 1e-10);
    }

    // =========================================================================
    // PARTITION D: DEFENSIVE & EXCEPTION GUARD PATHS
    // =========================================================================

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testComputeResidualsDimensionMismatch() {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.optimize(
            new Target(new double[] { 1.0, 2.0, 3.0 }),
            new Weight(new double[] { 1.0, 1.0, 1.0 }),
            new MaxEval(10)
        );

        // Target length is 3, but objectiveValue length is 2
        optimizer.computeResiduals(new double[] { 1.0, 2.0 });
    }

    @Test(expected = SingularMatrixException.class, timeout = 4000)
    public void testComputeCovariancesSingularMatrixThrowsException() {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.optimize(
            new Target(new double[] { 0.0, 0.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new MaxEval(10)
        );

        // Linearly dependent columns: rank 1 instead of 2
        // J = [ 1, 2 ]
        //     [ 2, 4 ]
        optimizer.setCustomJacobian(new double[][] {
            { 1.0, 2.0 },
            { 2.0, 4.0 }
        });

        optimizer.computeCovariances(new double[] { 0.0, 0.0 }, 1e-14);
    }

    @Test(expected = SingularMatrixException.class, timeout = 4000)
    public void testComputeSigmaSingularMatrixThrowsException() {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer();
        optimizer.optimize(
            new Target(new double[] { 0.0, 0.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new MaxEval(10)
        );

        // Completely zero Jacobian
        optimizer.setCustomJacobian(new double[][] {
            { 0.0, 0.0 },
            { 0.0, 0.0 }
        });

        optimizer.computeSigma(new double[] { 0.0, 0.0 }, 1e-14);
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE & CONVERGENCE CHECKER
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorWithConvergenceChecker() {
        ConvergenceChecker<PointVectorValuePair> checker =
            new SimplePointChecker<PointVectorValuePair>(1e-4, 1e-4);
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer(checker);

        assertSame(checker, optimizer.getConvergenceChecker());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullConvergenceChecker() {
        ConcreteLeastSquaresOptimizer optimizer = new ConcreteLeastSquaresOptimizer(null);
        assertNull(optimizer.getConvergenceChecker());
    }
}