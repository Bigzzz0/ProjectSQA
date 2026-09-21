package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.util.FastMath;

/**
 * Advanced white-box test suite for LevenbergMarquardtOptimizer.
 * Targets line/branch coverage and the known Defects4J defect in getIterations().
 */
public class LevenbergMarquardtOptimizerDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     *
     * Partitions:
     * A: Core functional logic – normal convergence via cost/parameter/orthogonality tolerances.
     * B: Boundary values – zero initial step bound, extreme tolerances, rank-deficient Jacobian.
     * C: Defect-targeted – verify getIterations() returns positive count after optimization.
     * D: Exception paths – ConvergenceException, MathUnsupportedOperationException (bounds).
     * E: Object lifecycle – constructor defaults, custom checker, state after optimize.
     *
     * Branches covered:
     * - doOptimize(): outer loop, inner loop, convergence checks (orthogonality, cost, delta),
     *   ratio computation, step bound update, successful/failed iteration, termination tests.
     * - qrDecomposition(): column selection, rank detection, Householder transforms.
     * - determineLMParameter(): Gauss-Newton direction, parl/paru bounds, Newton correction.
     * - determineLMDirection(): Givens rotations, singular system handling.
     * - checkParameters(): bounds null vs non-null.
     * - Constructor variants: default, with checker, with tolerances.
     */

    // Helper: linear model y = p0 * x + p1
    private static class LinearModel implements ModelFunction {
        private final double[] x;
        private final double[] y;
        LinearModel(double[] x, double[] y) { this.x = x; this.y = y; }
        public double[] value(double[] params) {
            double[] residuals = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                residuals[i] = y[i] - (params[0] * x[i] + params[1]);
            }
            return residuals;
        }
    }

    private static class LinearJacobian implements ModelFunctionJacobian {
        private final double[] x;
        LinearJacobian(double[] x) { this.x = x; }
        public RealMatrix value(double[] params) {
            double[][] jac = new double[x.length][2];
            for (int i = 0; i < x.length; i++) {
                jac[i][0] = -x[i];
                jac[i][1] = -1.0;
            }
            return new Array2DRowRealMatrix(jac);
        }
    }

    // Helper: simple quadratic model for rank-deficient test
    private static class QuadraticModel implements ModelFunction {
        public double[] value(double[] params) {
            // f(x) = (p0)^2 + (p1)^2, but only one observation
            return new double[] { params[0]*params[0] + params[1]*params[1] };
        }
    }

    private static class QuadraticJacobian implements ModelFunctionJacobian {
        public RealMatrix value(double[] params) {
            double[][] jac = new double[1][2];
            jac[0][0] = 2 * params[0];
            jac[0][1] = 2 * params[1];
            return new Array2DRowRealMatrix(jac);
        }
    }

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testSimpleLinearFit() {
        double[] x = {1.0, 2.0, 3.0, 4.0};
        double[] y = {3.0, 5.0, 7.0, 9.0}; // y = 2*x + 1
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        PointVectorValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new Target(y),
            new Weight(new double[]{1,1,1,1}),
            new InitialGuess(new double[]{0,0}),
            new ModelFunction(new LinearModel(x, y)),
            new ModelFunctionJacobian(new LinearJacobian(x))
        );
        double[] params = result.getPoint();
        assertEquals("p0", 2.0, params[0], 1e-6);
        assertEquals("p1", 1.0, params[1], 1e-6);
        assertTrue("Iterations > 0", optimizer.getIterations() > 0);
    }

    @Test(timeout = 4000)
    public void testConvergenceViaOrthogonality() {
        // Use a problem that converges quickly due to orthogonality
        double[] x = {0.0, 1.0};
        double[] y = {1.0, 2.0}; // y = x + 1
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(
            1e-10, 1e-10, 1e-10);
        PointVectorValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new Target(y),
            new Weight(new double[]{1,1}),
            new InitialGuess(new double[]{0.5, 0.5}),
            new ModelFunction(new LinearModel(x, y)),
            new ModelFunctionJacobian(new LinearJacobian(x))
        );
        double[] params = result.getPoint();
        assertEquals(1.0, params[0], 1e-6);
        assertEquals(1.0, params[1], 1e-6);
    }

    @Test(timeout = 4000)
    public void testConvergenceViaCostTolerance() {
        // Very loose cost tolerance should stop early
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(
            100, 1e-2, 1e-10, 1e-10, Precision.SAFE_MIN);
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 5.0};
        PointVectorValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new Target(y),
            new Weight(new double[]{1,1}),
            new InitialGuess(new double[]{0,0}),
            new ModelFunction(new LinearModel(x, y)),
            new ModelFunctionJacobian(new LinearJacobian(x))
        );
        // Should have converged (cost small enough)
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testConvergenceViaDeltaTolerance() {
        // Set parRelativeTolerance large so delta condition triggers
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(
            100, 1e-10, 1e-2, 1e-10, Precision.SAFE_MIN);
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 5.0};
        PointVectorValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new Target(y),
            new Weight(new double[]{1,1}),
            new InitialGuess(new double[]{0,0}),
            new ModelFunction(new LinearModel(x, y)),
            new ModelFunctionJacobian(new LinearJacobian(x))
        );
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testCustomConvergenceChecker() {
        // Use a checker that stops after 2 iterations
        ConvergenceChecker<PointVectorValuePair> checker = new ConvergenceChecker<PointVectorValuePair>() {
            private int count = 0;
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                return ++count >= 2;
            }
        };
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(checker);
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {2.0, 4.0, 6.0}; // y = 2*x
        PointVectorValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new Target(y),
            new Weight(new double[]{1,1,1}),
            new InitialGuess(new double[]{0,0}),
            new ModelFunction(new LinearModel(x, y)),
            new ModelFunctionJacobian(new LinearJacobian(x))
        );
        // Should have stopped after 2 iterations
        assertTrue("Iterations <= 2", optimizer.getIterations() <= 2);
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testZeroInitialStepBound() {
        // initialStepBoundFactor = 0 should still work (delta = 0 initially)
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(
            0, 1e-10, 1e-10, 1e-10, Precision.SAFE_MIN);
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 5.0};
        PointVectorValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new Target(y),
            new Weight(new double[]{1,1}),
            new InitialGuess(new double[]{0,0}),
            new ModelFunction(new LinearModel(x, y)),
            new ModelFunctionJacobian(new LinearJacobian(x))
        );
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testRankDeficientJacobian() {
        // Over-determined system with rank-deficient Jacobian (only one observation)
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        PointVectorValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new Target(new double[]{0.0}),
            new Weight(new double[]{1.0}),
            new InitialGuess(new double[]{1.0, 1.0}),
            new ModelFunction(new QuadraticModel()),
            new ModelFunctionJacobian(new QuadraticJacobian())
        );
        // Should converge to a point where cost is minimal (p0=0, p1=0)
        double[] params = result.getPoint();
        assertEquals(0.0, params[0], 1e-6);
        assertEquals(0.0, params[1], 1e-6);
    }

    @Test(timeout = 4000)
    public void testExtremeTolerances() {
        // Very tight tolerances should still converge
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(
            100, 1e-20, 1e-20, 1e-20, Precision.SAFE_MIN);
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 5.0};
        PointVectorValuePair result = optimizer.optimize(
            new MaxEval(10000),
            new MaxIter(1000),
            new Target(y),
            new Weight(new double[]{1,1}),
            new InitialGuess(new double[]{0,0}),
            new ModelFunction(new LinearModel(x, y)),
            new ModelFunctionJacobian(new LinearJacobian(x))
        );
        assertNotNull(result);
    }

    // ========== Partition C: Defect-Targeted (getIterations) ==========

    @Test(timeout = 4000)
    public void testGetIterationsAfterOptimization() {
        // This test directly targets the known defect where getIterations() may return 0
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {2.0, 4.0, 6.0, 8.0, 10.0}; // y = 2*x
        optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new Target(y),
            new Weight(new double[]{1,1,1,1,1}),
            new InitialGuess(new double[]{0,0}),
            new ModelFunction(new LinearModel(x, y)),
            new ModelFunctionJacobian(new LinearJacobian(x))
        );
        int iterations = optimizer.getIterations();
        assertTrue("Iterations should be > 0, but got " + iterations, iterations > 0);
    }

    @Test(timeout = 4000)
    public void testGetIterationsAfterEarlyConvergence() {
        // Use a checker that stops immediately (converged on first iteration)
        ConvergenceChecker<PointVectorValuePair> checker = new ConvergenceChecker<PointVectorValuePair>() {
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                return true; // always converged
            }
        };
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(checker);
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 5.0};
        optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new Target(y),
            new Weight(new double[]{1,1}),
            new InitialGuess(new double[]{0,0}),
            new ModelFunction(new LinearModel(x, y)),
            new ModelFunctionJacobian(new LinearJacobian(x))
        );
        int iterations = optimizer.getIterations();
        assertTrue("Iterations should be >= 1, but got " + iterations, iterations >= 1);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = MathUnsupportedOperationException.class, timeout = 4000)
    public void testBoundsThrowException() {
        // Bounds are not supported; should throw MathUnsupportedOperationException
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 5.0};
        optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new Target(y),
            new Weight(new double[]{1,1}),
            new InitialGuess(new double[]{0,0}),
            new ModelFunction(new LinearModel(x, y)),
            new ModelFunctionJacobian(new LinearJacobian(x)),
            new org.apache.commons.math3.optim.SimpleBounds(new double[]{-10,-10}, new double[]{10,10})
        );
    }

    @Test(expected = ConvergenceException.class, timeout = 4000)
    public void testTooSmallCostTolerance() {
        // Set costRelativeTolerance extremely small to trigger ConvergenceException
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(
            100, 1e-30, 1e-10, 1e-10, Precision.SAFE_MIN);
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 5.0};
        optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new Target(y),
            new Weight(new double[]{1,1}),
            new InitialGuess(new double[]{0,0}),
            new ModelFunction(new LinearModel(x, y)),
            new ModelFunctionJacobian(new LinearJacobian(x))
        );
    }

    @Test(expected = ConvergenceException.class, timeout = 4000)
    public void testTooSmallParameterTolerance() {
        // Set parRelativeTolerance extremely small to trigger ConvergenceException
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(
            100, 1e-10, 1e-30, 1e-10, Precision.SAFE_MIN);
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 5.0};
        optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new Target(y),
            new Weight(new double[]{1,1}),
            new InitialGuess(new double[]{0,0}),
            new ModelFunction(new LinearModel(x, y)),
            new ModelFunctionJacobian(new LinearJacobian(x))
        );
    }

    @Test(expected = ConvergenceException.class, timeout = 4000)
    public void testTooSmallOrthoTolerance() {
        // Set orthoTolerance extremely small to trigger ConvergenceException
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(
            100, 1e-10, 1e-10, 1e-30, Precision.SAFE_MIN);
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 5.0};
        optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new Target(y),
            new Weight(new double[]{1,1}),
            new InitialGuess(new double[]{0,0}),
            new ModelFunction(new LinearModel(x, y)),
            new ModelFunctionJacobian(new LinearJacobian(x))
        );
    }

    // ========== Partition E: Object Lifecycle & Contract ==========

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        assertNotNull(optimizer);
        // Should be able to optimize a simple problem
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 5.0};
        PointVectorValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new Target(y),
            new Weight(new double[]{1,1}),
            new InitialGuess(new double[]{0,0}),
            new ModelFunction(new LinearModel(x, y)),
            new ModelFunctionJacobian(new LinearJacobian(x))
        );
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testConstructorWithChecker() {
        ConvergenceChecker<PointVectorValuePair> checker = new SimpleVectorValueChecker(1e-10, 1e-10);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(checker);
        assertNotNull(optimizer);
    }

    @Test(timeout = 4000)
    public void testConstructorWithAllParams() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(
            50, 1e-8, 1e-8, 1e-8, 1e-12);
        assertNotNull(optimizer);
    }

    @Test(timeout = 4000)
    public void testStateAfterOptimize() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 5.0};
        optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new Target(y),
            new Weight(new double[]{1,1}),
            new InitialGuess(new double[]{0,0}),
            new ModelFunction(new LinearModel(x, y)),
            new ModelFunctionJacobian(new LinearJacobian(x))
        );
        // After optimization, internal fields should be set
        assertNotNull(optimizer.getConvergenceChecker());
        assertTrue(optimizer.getEvaluations() > 0);
        assertTrue(optimizer.getIterations() > 0);
    }
}