package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: AbstractLeastSquaresOptimizer (extends JacobianMultivariateVectorOptimizer)
 *
 * Branch / Condition Coverage:
 * 1. computeWeightedJacobian(double[] params)
 *    - Matrix multiplication: weightMatrixSqrt * computeJacobian(params)
 *    - Dimension match and mismatch checks via underlying linear algebra / jacobian provider
 * 2. computeCost(double[] residuals)
 *    - Cost calculation: sqrt(r^T * W * r)
 *    - Non-zero, zero, and positive-definite weight matrix interactions
 * 3. getRMS() & getChiSquare()
 *    - Cost squared vs RMS = sqrt(chiSquare / targetSize)
 *    - Zero targetSize, non-zero targetSize, zero cost
 * 4. getWeightSquareRoot()
 *    - Matrix defensive copy verification
 * 5. setCost(double cost)
 *    - Setter mutation and state reflection in getChiSquare() / getRMS()
 * 6. computeCovariances(double[] params, double threshold)
 *    - Inversion of J^T * J using QRDecomposition solver with threshold
 *    - Singular matrix threshold triggering SingularMatrixException
 * 7. computeSigma(double[] params, double covarianceSingularityThreshold)
 *    - Sqrt of diagonal elements of covariance matrix
 * 8. computeResiduals(double[] objectiveValue)
 *    - Target dimension matches objectiveValue length -> computes target - objectiveValue
 *    - Target dimension mismatch -> throws DimensionMismatchException
 * 9. parseOptimizationData(OptimizationData... optData) via optimize(...)
 *    - optData containing Weight -> computes weightMatrixSqrt via squareRoot(m)
 *    - optData without Weight -> retains existing weightMatrixSqrt or stays null
 *    - Presence of other OptimizationData instances (Target, ModelFunction, etc.)
 *
 * Target Defect (Defects4J / MATH-983):
 * - squareRoot(RealMatrix m) unconditionally uses EigenDecomposition(m).
 * - When a large DiagonalMatrix is provided (e.g., from Weight(double[]) with large sample size),
 *   EigenDecomposition(m) constructs dense intermediate arrays causing OutOfMemoryError.
 * - Test Partition C specifically tests optimization with a large diagonal Weight matrix
 *   to expose this defect.
 */

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractLeastSquaresOptimizerGeminiTest {

    private static final double EPSILON = 1e-10;

    /**
     * Concrete test implementation of AbstractLeastSquaresOptimizer exposing protected methods.
     */
    private static class TestOptimizer extends AbstractLeastSquaresOptimizer {
        private PointVectorValuePair dummyPair;

        TestOptimizer() {
            super(null);
        }

        TestOptimizer(ConvergenceChecker<PointVectorValuePair> checker) {
            super(checker);
        }

        void setDummyReturn(PointVectorValuePair pair) {
            this.dummyPair = pair;
        }

        @Override
        protected PointVectorValuePair doOptimize() {
            return dummyPair != null ? dummyPair : new PointVectorValuePair(new double[]{0.0}, new double[]{0.0});
        }

        @Override
        public RealMatrix computeWeightedJacobian(double[] params) {
            return super.computeWeightedJacobian(params);
        }

        @Override
        public double computeCost(double[] residuals) {
            return super.computeCost(residuals);
        }

        @Override
        public void setCost(double cost) {
            super.setCost(cost);
        }

        @Override
        public double[] computeResiduals(double[] objectiveValue) {
            return super.computeResiduals(objectiveValue);
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCostAndChiSquareAndRMS() {
        TestOptimizer optimizer = new TestOptimizer();
        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{10.0, 20.0}),
            new Weight(new double[]{1.0, 1.0})
        );

        optimizer.setCost(5.0);
        assertEquals(25.0, optimizer.getChiSquare(), EPSILON);
        // Target size is 2, RMS = sqrt(25 / 2) = sqrt(12.5)
        assertEquals(FastMath.sqrt(12.5), optimizer.getRMS(), EPSILON);

        optimizer.setCost(0.0);
        assertEquals(0.0, optimizer.getChiSquare(), EPSILON);
        assertEquals(0.0, optimizer.getRMS(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testComputeCostWithWeights() {
        TestOptimizer optimizer = new TestOptimizer();
        // Weight matrix = diag(2.0, 3.0)
        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{0.0, 0.0}),
            new Weight(new double[]{2.0, 3.0})
        );

        double[] residuals = new double[]{2.0, 4.0};
        // Cost = sqrt( 2.0 * (2^2) + 3.0 * (4^2) ) = sqrt( 8 + 48 ) = sqrt(56)
        double expectedCost = FastMath.sqrt(56.0);
        double actualCost = optimizer.computeCost(residuals);
        assertEquals(expectedCost, actualCost, EPSILON);
    }

    @Test(timeout = 4000)
    public void testComputeResidualsValid() {
        TestOptimizer optimizer = new TestOptimizer();
        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{10.0, -5.0, 3.5}),
            new Weight(new double[]{1.0, 1.0, 1.0})
        );

        double[] objectiveValue = new double[]{8.0, -2.0, 3.5};
        double[] residuals = optimizer.computeResiduals(objectiveValue);

        assertNotNull(residuals);
        assertEquals(3, residuals.length);
        assertEquals(2.0, residuals[0], EPSILON);
        assertEquals(-3.0, residuals[1], EPSILON);
        assertEquals(0.0, residuals[2], EPSILON);
    }

    @Test(timeout = 4000)
    public void testWeightSquareRootDefensiveCopy() {
        TestOptimizer optimizer = new TestOptimizer();
        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{1.0}),
            new Weight(new double[]{4.0})
        );

        RealMatrix sqrtW1 = optimizer.getWeightSquareRoot();
        assertEquals(2.0, sqrtW1.getEntry(0, 0), EPSILON);

        // Mutate the returned matrix copy
        sqrtW1.setEntry(0, 0, 99.0);

        // Ensure internal weightMatrixSqrt remains unaltered
        RealMatrix sqrtW2 = optimizer.getWeightSquareRoot();
        assertEquals(2.0, sqrtW2.getEntry(0, 0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testComputeWeightedJacobianAndCovariancesAndSigma() {
        TestOptimizer optimizer = new TestOptimizer();

        // Model: f(x0, x1) = [x0 + 2*x1, 3*x0 + 4*x1]
        // J = [[1, 2], [3, 4]]
        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            @Override
            public double[][] value(double[] point) {
                return new double[][]{
                    {1.0, 2.0},
                    {3.0, 4.0}
                };
            }
        };

        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            @Override
            public double[] value(double[] point) {
                return new double[]{
                    point[0] + 2.0 * point[1],
                    3.0 * point[0] + 4.0 * point[1]
                };
            }
        };

        // Weight = diag(4.0, 9.0) -> Sqrt(Weight) = diag(2.0, 3.0)
        // Weighted J = diag(2, 3) * [[1, 2], [3, 4]] = [[2, 4], [9, 12]]
        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{5.0, 11.0}),
            new Weight(new double[]{4.0, 9.0}),
            new InitialGuess(new double[]{1.0, 1.0}),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian)
        );

        double[] params = new double[]{1.0, 1.0};
        RealMatrix weightedJ = optimizer.computeWeightedJacobian(params);
        assertEquals(2.0, weightedJ.getEntry(0, 0), EPSILON);
        assertEquals(4.0, weightedJ.getEntry(0, 1), EPSILON);
        assertEquals(9.0, weightedJ.getEntry(1, 0), EPSILON);
        assertEquals(12.0, weightedJ.getEntry(1, 1), EPSILON);

        // Compute Covariances: (J^T * J)^(-1)
        // J = [[2, 4], [9, 12]]
        // J^T * J = [[85, 116], [116, 160]]
        // Det = 85*160 - 116^2 = 13600 - 13456 = 144
        // Inverse = 1/144 * [[160, -116], [-116, 85]]
        double[][] cov = optimizer.computeCovariances(params, 1e-10);
        assertEquals(160.0 / 144.0, cov[0][0], EPSILON);
        assertEquals(-116.0 / 144.0, cov[0][1], EPSILON);
        assertEquals(-116.0 / 144.0, cov[1][0], EPSILON);
        assertEquals(85.0 / 144.0, cov[1][1], EPSILON);

        // Compute Sigma
        double[] sigma = optimizer.computeSigma(params, 1e-10);
        assertEquals(FastMath.sqrt(160.0 / 144.0), sigma[0], EPSILON);
        assertEquals(FastMath.sqrt(85.0 / 144.0), sigma[1], EPSILON);
    }

    @Test(timeout = 4000)
    public void testOptimizerWithConvergenceChecker() {
        ConvergenceChecker<PointVectorValuePair> checker =
            new SimplePointChecker<PointVectorValuePair>(1e-4, 1e-4);
        TestOptimizer optimizer = new TestOptimizer(checker);

        assertEquals(checker, optimizer.getConvergenceChecker());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testZeroCostAndZeroResiduals() {
        TestOptimizer optimizer = new TestOptimizer();
        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{0.0}),
            new Weight(new double[]{1.0})
        );

        double cost = optimizer.computeCost(new double[]{0.0});
        assertEquals(0.0, cost, EPSILON);

        optimizer.setCost(0.0);
        assertEquals(0.0, optimizer.getCost(), EPSILON);
        assertEquals(0.0, optimizer.getChiSquare(), EPSILON);
        assertEquals(0.0, optimizer.getRMS(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testIdentityAndDenseWeightMatrixSquareRoot() {
        TestOptimizer optimizer = new TestOptimizer();

        // Symmetric positive-definite matrix 2x2: [[4, 0], [0, 9]]
        RealMatrix weightMat = MatrixUtils.createRealMatrix(new double[][]{
            {4.0, 0.0},
            {0.0, 9.0}
        });

        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{1.0, 1.0}),
            new Weight(weightMat)
        );

        RealMatrix sqrtW = optimizer.getWeightSquareRoot();
        assertEquals(2.0, sqrtW.getEntry(0, 0), EPSILON);
        assertEquals(0.0, sqrtW.getEntry(0, 1), EPSILON);
        assertEquals(0.0, sqrtW.getEntry(1, 0), EPSILON);
        assertEquals(3.0, sqrtW.getEntry(1, 1), EPSILON);
    }

    @Test(timeout = 4000)
    public void testParseOptimizationDataWithoutWeight() {
        TestOptimizer optimizer = new TestOptimizer();
        // Optimize called with no Weight data
        PointVectorValuePair result = optimizer.optimize(
            new MaxEval(100),
            new Target(new double[]{1.0})
        );
        assertNotNull(result);
        assertNull(optimizer.getWeightSquareRoot());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-983 / Defects4J)
    // =========================================================================

    /**
     * Targets Defects4J bug where squareRoot(m) unconditionally uses
     * new EigenDecomposition(m). For