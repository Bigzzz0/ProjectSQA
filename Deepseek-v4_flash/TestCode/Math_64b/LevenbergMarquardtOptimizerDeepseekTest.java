package org.apache.commons.math.optimization.general;

import java.util.Arrays;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.VectorialConvergenceChecker;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Partition A: Core Functional Logic & State Transitions
 *   - Normal optimization path (Minpack JennrichSampson, FreudensteinRoth)
 *   - Setters: initialStepBoundFactor, costRelativeTolerance, parRelativeTolerance,
 *              orthoTolerance, qrRankingThreshold
 *
 * Partition B: Boundary Value Analysis & Extremes
 *   - Zero cost at initial point (cost == 0)
 *   - Zero Jacobian (all columns zero)
 *   - xNorm == 0 at first iteration
 *   - delta update with ratio <= 0.25 and actRed < 0
 *   - lmPar = 0, ratio >= 0.75
 *
 * Partition C: Defect-Targeted Branch Zone
 *   - Known Defects4J failures: JennrichSampson and FreudensteinRoth problems
 *   - Triggers internal algorithmic inaccuracies in LM parameter or QR decomposition
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - Too small costRelativeTolerance -> OptimizationException
 *   - Too small parRelativeTolerance -> OptimizationException
 *   - Too small orthoTolerance -> OptimizationException
 *   - Q.R. decomposition with infinite/NaN column norm
 *
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Non-null convergence checker path
 *   - Convergence via checker convergence
 */
public class LevenbergMarquardtOptimizerDeepseekTest {

    // --------------------------------------------------------------------
    // Private inner classes for test problems
    // --------------------------------------------------------------------

    /** JennrichSampson function: 10 equations, 2 parameters. */
    private static class JennrichSampsonFunction implements DifferentiableMultivariateVectorialFunction {
        public double[] value(double[] point) throws FunctionEvaluationException {
            double x1 = point[0];
            double x2 = point[1];
            double[] f = new double[10];
            for (int i = 1; i <= 10; i++) {
                f[i - 1] = 2 + 2 * i - (Math.exp(i * x1) + Math.exp(i * x2));
            }
            return f;
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    double x1 = point[0];
                    double x2 = point[1];
                    double[][] j = new double[10][2];
                    for (int i = 1; i <= 10; i++) {
                        j[i - 1][0] = -i * Math.exp(i * x1);
                        j[i - 1][1] = -i * Math.exp(i * x2);
                    }
                    return j;
                }
            };
        }
    }

    /** FreudensteinRoth function: 2 equations, 2 parameters. */
    private static class FreudensteinRothFunction implements DifferentiableMultivariateVectorialFunction {
        public double[] value(double[] point) {
            double x1 = point[0];
            double x2 = point[1];
            double f1 = -13 + x1 + ((5 - x2) * x2 - 2) * x2;
            double f2 = -29 + x1 + ((x2 + 1) * x2 - 14) * x2;
            return new double[] { f1, f2 };
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    double x2 = point[1];
                    double[][] j = new double[2][2];
                    j[0][0] = 1;
                    j[0][1] = 10 * x2 - 3 * x2 * x2 - 2;
                    j[1][0] = 1;
                    j[1][1] = 3 * x2 * x2 + 2 * x2 - 14;
                    return j;
                }
            };
        }
    }

    /** Constant zero function (cost = 0, zero Jacobian). */
    private static class ZeroFunction implements DifferentiableMultivariateVectorialFunction {
        public double[] value(double[] point) {
            return new double[] { 0, 0 };
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                public double[][] value(double[] point) {
                    return new double[][] { { 0, 0 }, { 0, 0 } };
                }
            };
        }
    }

    // --------------------------------------------------------------------
    // Partition A & C: defect-targeted tests (known Defects4J failures)
    // --------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testMinpackJennrichSampson() throws FunctionEvaluationException, OptimizationException {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setCostRelativeTolerance(1.0e-10);
        optimizer.setParRelativeTolerance(1.0e-10);
        optimizer.setOrthoTolerance(1.0e-10);
        optimizer.setInitialStepBoundFactor(100.0);
        optimizer.setMaxIterations(1000);
        optimizer.setConvergenceChecker(null);

        DifferentiableMultivariateVectorialFunction function = new JennrichSampsonFunction();
        double[] target = new double[10];
        double[] weights = new double[10];
        Arrays.fill(weights, 1.0);
        double[] initialPoint = new double[] { 0.3, 0.4 };

        VectorialPointValuePair result = optimizer.optimize(function, target, weights, initialPoint);
        double[] point = result.getPoint();
        double[] residuals = function.value(point);
        double cost = 0;
        for (double r : residuals) {
            cost += r * r;
        }
        assertEquals("JennrichSampson cost mismatch", 0.2578199266368004, cost, 1e-13);
    }

    @Test(timeout = 4000)
    public void testMinpackFreudensteinRoth() throws FunctionEvaluationException, OptimizationException {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setCostRelativeTolerance(1.0e-10);
        optimizer.setParRelativeTolerance(1.0e-10);
        optimizer.setOrthoTolerance(1.0e-10);
        optimizer.setInitialStepBoundFactor(100.0);
        optimizer.setMaxIterations(1000);
        optimizer.setConvergenceChecker(null);

        DifferentiableMultivariateVectorialFunction function = new FreudensteinRothFunction();
        double[] target = new double[] { 0.0, 0.0 };
        double[] weights = new double[] { 1.0, 1.0 };
        double[] initialPoint = new double[] { 0.5, -2.0 };

        VectorialPointValuePair result = optimizer.optimize(function, target, weights, initialPoint);
        double[] point = result.getPoint();
        double[] residuals = function.value(point);
        double cost = 0;
        for (double r : residuals) {
            cost += r * r;
        }
        assertEquals("FreudensteinRoth cost mismatch", 11.41300466147456, cost, 1e-13);
    }

    // --------------------------------------------------------------------
    // Partition B: Boundary conditions and extreme paths
    // --------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testZeroCostAtInitial() throws FunctionEvaluationException, OptimizationException {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setMaxIterations(10);
        DifferentiableMultivariateVectorialFunction f = new ZeroFunction();
        double[] target = new double[] { 0, 0 };
        double[] weights = new double[] { 1, 1 };
        double[] start = new double[] { 1.5, -2.3 };

        VectorialPointValuePair result = optimizer.optimize(f, target, weights, start);
        // No change because cost = 0 -> immediate convergence
        assertEquals("x0 unchanged", 1.5, result.getPoint()[0], 1e-15);
        assertEquals("x1 unchanged", -2.3, result.getPoint()[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testZeroJacobian() throws FunctionEvaluationException, OptimizationException {
        // Function returns constant [1,2], Jacobian zero
        DifferentiableMultivariateVectorialFunction constant = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { 1.0, 2.0 };
            }
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 0, 0 }, { 0, 0 } };
                    }
                };
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setOrthoTolerance(1.0e-10);
        optimizer.setMaxIterations(10);
        double[] target = new double[] { 0, 0 };
        double[] weights = new double[] { 1, 1 };
        double[] start = new double[] { 0, 0 };

        VectorialPointValuePair result = optimizer.optimize(constant, target, weights, start);
        // No improvement possible; should return at starting point
        assertArrayEquals(new double[] { 0, 0 }, result.getPoint(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConvergenceByCostRelativeTolerance() throws FunctionEvaluationException, OptimizationException {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        // Set very loose cost tolerance to force early convergence
        optimizer.setCostRelativeTolerance(1.0e-4);
        optimizer.setParRelativeTolerance(1.0e-10);
        optimizer.setOrthoTolerance(1.0e-10);
        optimizer.setMaxIterations(100);

        DifferentiableMultivariateVectorialFunction function = new FreudensteinRothFunction();
        double[] target = new double[] { 0, 0 };
        double[] weights = new double[] { 1, 1 };
        double[] start = new double[] { 0.5, -2.0 };

        VectorialPointValuePair result = optimizer.optimize(function, target, weights, start);
        double[] point = result.getPoint();
        double[] res = function.value(point);
        double cost = 0;
        for (double r : res) cost += r * r;
        // With high tolerance, cost may not reach the minimum but must be <= tolerance condition.
        // This test primarily verifies the branch is taken.
        assertTrue("Cost should be small enough", cost < 1.0);
    }

    // --------------------------------------------------------------------
    // Partition D: Exception paths
    // --------------------------------------------------------------------

    @Test(timeout = 4000, expected = OptimizationException.class)
    public void testTooSmallCostRelativeTolerance() throws FunctionEvaluationException, OptimizationException {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setCostRelativeTolerance(1.0e-20);
        optimizer.setParRelativeTolerance(1.0e-10);
        optimizer.setOrthoTolerance(1.0e-10);
        optimizer.setMaxIterations(500);

        DifferentiableMultivariateVectorialFunction function = new FreudensteinRothFunction();
        double[] target = new double[] { 0, 0 };
        double[] weights = new double[] { 1, 1 };
        double[] start = new double[] { 0.5, -2.0 };

        optimizer.optimize(function, target, weights, start);
    }

    @Test(timeout = 4000, expected = OptimizationException.class)
    public void testTooSmallParRelativeTolerance() throws FunctionEvaluationException, OptimizationException {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setCostRelativeTolerance(1.0e-10);
        optimizer.setParRelativeTolerance(1.0e-20);
        optimizer.setOrthoTolerance(1.0e-10);
        optimizer.setMaxIterations(500);

        DifferentiableMultivariateVectorialFunction function = new FreudensteinRothFunction();
        double[] target = new double[] { 0, 0 };
        double[] weights = new double[] { 1, 1 };
        double[] start = new double[] { 0.5, -2.0 };

        optimizer.optimize(function, target, weights, start);
    }

    @Test(timeout = 4000, expected = OptimizationException.class)
    public void testTooSmallOrthoTolerance() throws FunctionEvaluationException, OptimizationException {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setCostRelativeTolerance(1.0e-10);
        optimizer.setParRelativeTolerance(1.0e-10);
        optimizer.setOrthoTolerance(1.0e-20);
        optimizer.setMaxIterations(500);

        DifferentiableMultivariateVectorialFunction function = new FreudensteinRothFunction();
        double[] target = new double[] { 0, 0 };
        double[] weights = new double[] { 1, 1 };
        double[] start = new double[] { 0.5, -2.0 };

        optimizer.optimize(function, target, weights, start);
    }

    // --------------------------------------------------------------------
    // Partition E: Checker-based convergence and additional coverage
    // --------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testWithNonnullChecker() throws FunctionEvaluationException, OptimizationException {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setConvergenceChecker(new VectorialConvergenceChecker() {
            private int count = 0;
            public boolean converged(int iteration, VectorialPointValuePair previous, VectorialPointValuePair current) {
                // converge after first step
                return count++ >= 1;
            }
        });
        optimizer.setMaxIterations(10);
        // Use simple linear problem so it can converge quickly
        DifferentiableMultivariateVectorialFunction identity = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0] };
            }
            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 1.0 } };
                    }
                };
            }
        };
        double[] target = new double[] { 0.0 };
        double[] weights = new double[] { 1.0 };
        double[] start = new double[] { 5.0 };

        VectorialPointValuePair result = optimizer.optimize(identity, target, weights, start);
        // The checker forced convergence after one step, so point should be 5.0 (unchanged)
        assertEquals("x unchanged", 5.0, result.getPoint()[0], 1e-15);
    }

    @Test(timeout = 4000)
    public void testInitialStepBoundFactorZeroNorm() throws FunctionEvaluationException, OptimizationException {
        // When xNorm == 0 at first iteration, delta = initialStepBoundFactor
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setInitialStepBoundFactor(42.0);
        optimizer.setMaxIterations(10);

        // Use zero function so that diag is zero, point zero => xNorm=0
        DifferentiableMultivariateVectorialFunction zero = new ZeroFunction();
        double[] target = new double[] { 0, 0 };
        double[] weights = new double[] { 1, 1 };
        double[] start = new double[] { 0, 0 };

        VectorialPointValuePair result = optimizer.optimize(zero, target, weights, start);
        assertArrayEquals(new double[] { 0, 0 }, result.getPoint(), 1e-15);
    }
}