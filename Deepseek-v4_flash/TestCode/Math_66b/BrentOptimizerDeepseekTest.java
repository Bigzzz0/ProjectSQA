package org.apache.commons.math.optimization.univariate;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.exception.NotStrictlyPositiveException;

/**
 * White-box test suite for BrentOptimizer targeting the known defect
 * (incorrect convergence and evaluation count) and achieving maximum
 * line/branch coverage.
 *
 * [Branch & Defect Analysis Matrix]
 * - Stopping criterion: |x - m| > tol2 - 0.5*(b-a) (branch)
 * - Parabolic interpolation vs golden section (branch on |e| > tol1)
 * - Update of a,b,v,w,x based on fu <= fx (branch)
 * - Exception paths: eps <= 0, t <= 0
 * - GoalType MINIMIZE vs MAXIMIZE (negation of fx/fu)
 * - lo < hi vs lo >= hi (swap)
 * - Golden section step when x < m vs x >= m
 * - Update by at least tol1 when |d| < tol1
 * - Defect: incorrect convergence leads to wrong optimum and low evaluation count
 */
public class BrentOptimizerDeepseekTest {

    // ---------------------------------------------------------------
    // Helper: simple quadratic function for basic tests
    // ---------------------------------------------------------------
    private static final UnivariateRealFunction QUAD = new UnivariateRealFunction() {
        public double value(double x) {
            return x * x;
        }
    };

    // ---------------------------------------------------------------
    // Helper: quintic function from defect report
    // f(x) = x^5 - 2x^3 + x
    // ---------------------------------------------------------------
    private static final UnivariateRealFunction QUINTIC = new UnivariateRealFunction() {
        public double value(double x) {
            return x * x * x * x * x - 2 * x * x * x + x;
        }
    };

    // ---------------------------------------------------------------
    // Helper: sin function from defect report
    // ---------------------------------------------------------------
    private static final UnivariateRealFunction SIN = new UnivariateRealFunction() {
        public double value(double x) {
            return Math.sin(x);
        }
    };

    // ---------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testMinimizeQuadratic() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        double result = optimizer.optimize(QUAD, GoalType.MINIMIZE, -1.0, 1.0, 0.5);
        assertEquals(0.0, result, 1e-8);
        assertTrue(optimizer.getEvaluations() > 0);
    }

    @Test(timeout = 4000)
    public void testMaximizeQuadratic() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        // Maximize -x^2 by minimizing x^2 with MAXIMIZE goal
        double result = optimizer.optimize(QUAD, GoalType.MAXIMIZE, -1.0, 1.0, 0.0);
        assertEquals(0.0, result, 1e-8);
    }

    @Test(timeout = 4000)
    public void testMinimizeWithStartValueAtBoundary() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        double result = optimizer.optimize(QUAD, GoalType.MINIMIZE, -1.0, 1.0, -1.0);
        assertEquals(0.0, result, 1e-8);
    }

    @Test(timeout = 4000)
    public void testMinimizeWithStartValueOutsideInterval() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        double result = optimizer.optimize(QUAD, GoalType.MINIMIZE, -1.0, 1.0, 2.0);
        assertEquals(0.0, result, 1e-8);
    }

    @Test(timeout = 4000)
    public void testMinimizeWithLoGreaterThanHi() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        double result = optimizer.optimize(QUAD, GoalType.MINIMIZE, 1.0, -1.0, 0.0);
        assertEquals(0.0, result, 1e-8);
    }

    // ---------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testTightTolerance() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-12);
        optimizer.setRelativeAccuracy(1e-14);
        double result = optimizer.optimize(QUAD, GoalType.MINIMIZE, -1.0, 1.0, 0.5);
        assertEquals(0.0, result, 1e-10);
    }

    @Test(timeout = 4000)
    public void testLargeInterval() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        double result = optimizer.optimize(QUAD, GoalType.MINIMIZE, -1000.0, 1000.0, 0.0);
        assertEquals(0.0, result, 1e-8);
    }

    @Test(timeout = 4000)
    public void testZeroInterval() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        double result = optimizer.optimize(QUAD, GoalType.MINIMIZE, 0.0, 0.0, 0.0);
        assertEquals(0.0, result, 1e-8);
    }

    // ---------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (known failures)
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSinMin() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        double result = optimizer.optimize(SIN, GoalType.MINIMIZE, 3.0, 5.0, 4.0);
        // Expected from defect report: 4.71238898038469
        // Bug gives: 4.71238897901431 (difference ~1.37e-9)
        assertEquals(4.71238898038469, result, 1e-12);
    }

    @Test(timeout = 4000)
    public void testQuinticMin() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        double result = optimizer.optimize(QUINTIC, GoalType.MINIMIZE, -1.0, 1.0, 0.0);
        // Expected from defect report: -0.2719561270319131
        // Bug gives: -0.2719561299044896 (difference ~2.87e-9)
        assertEquals(-0.2719561270319131, result, 1e-12);
    }

    @Test(timeout = 4000)
    public void testQuinticMinStatistics() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        optimizer.setMaximalIterationCount(10000);
        optimizer.optimize(QUINTIC, GoalType.MINIMIZE, -1.0, 1.0, 0.0);
        int evals = optimizer.getEvaluations();
        // Bug causes very low evaluation count (~18) instead of ~1880
        // With correct implementation, should be > 1000 for tight tolerance
        assertTrue("Evaluation count too low, defect likely present", evals > 1000);
    }

    // ---------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // ---------------------------------------------------------------

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testNonPositiveRelativeAccuracy() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setRelativeAccuracy(0.0);  // eps <= 0 triggers exception
        optimizer.optimize(QUAD, GoalType.MINIMIZE, -1.0, 1.0, 0.5);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testNonPositiveAbsoluteAccuracy() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(0.0);  // t <= 0 triggers exception
        optimizer.optimize(QUAD, GoalType.MINIMIZE, -1.0, 1.0, 0.5);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testNegativeRelativeAccuracy() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setRelativeAccuracy(-1.0);
        optimizer.optimize(QUAD, GoalType.MINIMIZE, -1.0, 1.0, 0.5);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testNegativeAbsoluteAccuracy() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(-1.0);
        optimizer.optimize(QUAD, GoalType.MINIMIZE, -1.0, 1.0, 0.5);
    }

    @Test(expected = MaxIterationsExceededException.class, timeout = 4000)
    public void testMaxIterationsExceeded() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setMaximalIterationCount(1);  // Force early termination
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        optimizer.optimize(QUAD, GoalType.MINIMIZE, -1.0, 1.0, 0.5);
    }

    // ---------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDefaultSettings() {
        BrentOptimizer optimizer = new BrentOptimizer();
        assertEquals(Integer.MAX_VALUE, optimizer.getMaxEvaluations());
        assertEquals(100, optimizer.getMaximalIterationCount());
        assertEquals(1E-10, optimizer.getAbsoluteAccuracy(), 1e-15);
        assertEquals(1.0e-14, optimizer.getRelativeAccuracy(), 1e-20);
    }

    @Test(timeout = 4000)
    public void testClearResult() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.optimize(QUAD, GoalType.MINIMIZE, -1.0, 1.0, 0.5);
        assertTrue(optimizer.getEvaluations() > 0);
        // After clearResult (called at start of optimize), evaluations reset
        // We can't directly call clearResult, but we can verify that a second call resets
        int firstEvals = optimizer.getEvaluations();
        optimizer.optimize(QUAD, GoalType.MINIMIZE, -1.0, 1.0, 0.5);
        int secondEvals = optimizer.getEvaluations();
        assertTrue(secondEvals <= firstEvals);  // Should be reset
    }

    // ---------------------------------------------------------------
    // Additional branch coverage: golden section vs parabolic
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGoldenSectionBranch() throws MaxIterationsExceededException, FunctionEvaluationException {
        // Use a function where parabolic interpolation is unlikely to succeed
        // e.g., a linear function (no curvature) – but Brent still works
        UnivariateRealFunction linear = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        double result = optimizer.optimize(linear, GoalType.MINIMIZE, -1.0, 1.0, 0.0);
        // Minimum of linear on [-1,1] is at -1
        assertEquals(-1.0, result, 1e-8);
    }

    @Test(timeout = 4000)
    public void testParabolicInterpolationBranch() throws MaxIterationsExceededException, FunctionEvaluationException {
        // Use a function with strong curvature to encourage parabolic step
        UnivariateRealFunction cubic = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 0.3) * (x - 0.3) * (x - 0.3) + 1;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        double result = optimizer.optimize(cubic, GoalType.MINIMIZE, -1.0, 1.0, 0.0);
        assertEquals(0.3, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testUpdateByAtLeastTol1() throws MaxIterationsExceededException, FunctionEvaluationException {
        // Force a situation where |d| < tol1 by using very tight tolerance and a flat function
        UnivariateRealFunction flat = new UnivariateRealFunction() {
            public double value(double x) {
                return 1.0;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        double result = optimizer.optimize(flat, GoalType.MINIMIZE, -1.0, 1.0, 0.0);
        // Any point is optimal; should converge quickly
        assertTrue(result >= -1.0 && result <= 1.0);
    }

    @Test(timeout = 4000)
    public void testFuLessThanFxBranch() throws MaxIterationsExceededException, FunctionEvaluationException {
        // Use a function where the new point is better than current
        UnivariateRealFunction simple = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 0.5) * (x - 0.5);
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        double result = optimizer.optimize(simple, GoalType.MINIMIZE, 0.0, 1.0, 0.0);
        assertEquals(0.5, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testFuGreaterThanFxBranch() throws MaxIterationsExceededException, FunctionEvaluationException {
        // Use a function where the new point is worse, but still updates v,w
        UnivariateRealFunction bump = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x) + 0.1 * x;
            }
        };
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        double result = optimizer.optimize(bump, GoalType.MINIMIZE, 0.0, 5.0, 2.0);
        // Just ensure it runs without exception
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testMaximizeWithSin() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        double result = optimizer.optimize(SIN, GoalType.MAXIMIZE, 0.0, 2.0 * Math.PI, 1.0);
        // Maximum of sin is at pi/2 ~ 1.5708
        assertEquals(Math.PI / 2, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testOptimizeWithoutStartValue() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        double result = optimizer.optimize(QUAD, GoalType.MINIMIZE, -1.0, 1.0);
        assertEquals(0.0, result, 1e-8);
    }

    @Test(timeout = 4000)
    public void testEvaluationsCount() throws MaxIterationsExceededException, FunctionEvaluationException {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-10);
        optimizer.setRelativeAccuracy(1e-14);
        optimizer.optimize(QUAD, GoalType.MINIMIZE, -1.0, 1.0, 0.5);
        int evals = optimizer.getEvaluations();
        assertTrue("Evaluations should be positive", evals > 0);
        assertTrue("Evaluations should not exceed max", evals <= optimizer.getMaxEvaluations());
    }
}