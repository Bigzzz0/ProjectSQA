package org.apache.commons.math.optimization.univariate;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.apache.commons.math.optimization.GoalType;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Class under test: BrentOptimizer
 *
 * Targeted Decision Branches & Conditions:
 * 1. Constructor initial state: maxEvaluations = Integer.MAX_VALUE, maxIterations = 100,
 *    absoluteAccuracy = 1E-10, relativeAccuracy = 1.0e-14.
 * 2. doOptimize(): directly throws UnsupportedOperationException.
 * 3. optimize(f, goalType, min, max, startValue) vs optimize(f, goalType, min, max) with GOLDEN_SECTION default.
 * 4. localMin guards:
 *    - eps <= 0 -> NotStrictlyPositiveException(eps)
 *    - t <= 0   -> NotStrictlyPositiveException(t)
 * 5. Bounds ordering:
 *    - lo < hi  -> a = lo, b = hi
 *    - lo >= hi -> a = hi, b = lo (inverted bounds)
 * 6. Goal type handling:
 *    - GoalType.MINIMIZE (fx, fu unchanged)
 *    - GoalType.MAXIMIZE (fx = -fx, fu = -fu, returned result negated back)
 * 7. Parabolic interpolation vs Golden section fallback:
 *    - Math.abs(e) > tol1 (fit parabola attempt) vs Math.abs(e) <= tol1 (golden section step)
 *    - Parabola curvature: q > 0 (p = -p) vs q <= 0 (q = -q)
 *    - Parabola acceptability: (p > q*(a-x) && p < q*(b-x) && |p| < |0.5*q*r|)
 *    - Too close to boundary: (u - a < tol2 || b - u < tol2) -> d = (x <= m) ? tol1 : -tol1
 *    - Rejection of parabola -> golden section step: x < m vs x >= m
 * 8. Movement step clamping:
 *    - Math.abs(d) < tol1 -> d >= 0 ? u = x + tol1 : u = x - tol1
 *    - Math.abs(d) >= tol1 -> u = x + d
 * 9. Bracket and history updates:
 *    - fu <= fx:
 *      * u < x -> b = x vs u >= x -> a = x
 *      * update v = w, w = x, x = u
 *    - fu > fx:
 *      * u < x -> a = u vs u >= x -> b = u
 *      * fu <= fw || w == x -> update v = w, w = u
 *      * else if (fu <= fv || v == x || v == w) -> update v = u
 *      * else -> neither updated
 * 10. Iteration limit exceeded: count >= maximalIterationCount -> MaxIterationsExceededException.
 * 11. Termination criteria: Math.abs(x - m) <= tol2 - 0.5 * (b - a) -> setResult & return.
 * 12. Defects4J Known Defect:
 *     - BrentOptimizerTest::testSinMin: assertEquals(3 * Math.PI / 2, optimizer.optimize(f, GoalType.MINIMIZE, 4, 5), 1e-10)
 *       fails due to tolerance premature termination producing 4.71238897901431 instead of ~4.71238898038469.
 *     - MultiStartUnivariateRealOptimizerTest::testQuinticMin: expected:<-0.2719561270319131> but was:<-0.2719561299044896>.
 */
public class BrentOptimizerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorDefaultState() {
        BrentOptimizer optimizer = new BrentOptimizer();
        assertEquals(Integer.MAX_VALUE, optimizer.getMaxEvaluations());
        assertEquals(100, optimizer.getMaximalIterationCount());
        assertEquals(1E-10, optimizer.getAbsoluteAccuracy(), 0.0);
        assertEquals(1.0e-14, optimizer.getRelativeAccuracy(), 0.0);
    }

    @Test(timeout = 4000)
    public void testMinimizeSimpleParabola() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        // f(x) = (x - 3)^2 + 2, minimum at x = 3, f(3) = 2
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 3.0) * (x - 3.0) + 2.0;
            }
        };
        double result = optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 5.0);
        assertEquals(3.0, result, 1e-6);
        assertEquals(2.0, optimizer.getFunctionValue(), 1e-6);
        assertEquals(result, optimizer.getResult(), 1e-9);
        assertTrue(optimizer.getIterationCount() > 0);
        assertTrue(optimizer.getEvaluations() > 0);
    }

    @Test(timeout = 4000)
    public void testMaximizeSimpleParabola() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        // f(x) = -(x - 4)^2 + 10, maximum at x = 4, f(4) = 10
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return -((x - 4.0) * (x - 4.0)) + 10.0;
            }
        };
        double result = optimizer.optimize(f, GoalType.MAXIMIZE, 1.0, 6.0);
        assertEquals(4.0, result, 1e-6);
        assertEquals(10.0, optimizer.getFunctionValue(), 1e-6);
        assertEquals(result, optimizer.getResult(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testOptimizeWithCustomStartValue() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.5) * (x - 1.5);
            }
        };
        // startValue explicitly set near 2.0
        double result = optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 3.0, 2.0);
        assertEquals(1.5, result, 1e-6);
        assertEquals(0.0, optimizer.getFunctionValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testInvertedIntervalBounds() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 2.0) * (x - 2.0);
            }
        };
        // lo > hi (min=5.0, max=0.0) -> tests the else branch (a = hi, b = lo)
        double result = optimizer.optimize(f, GoalType.MINIMIZE, 5.0, 0.0);
        assertEquals(2.0, result, 1e-6);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Algorithmic Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testGoldenSectionFallbackWhenCurvatureZero() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        // Constant function: curvature is 0, p and q will force fallback to golden section
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 7.0;
            }
        };
        double result = optimizer.optimize(f, GoalType.MINIMIZE, -2.0, 2.0, 0.0);
        assertTrue(result >= -2.0 && result <= 2.0);
        assertEquals(7.0, optimizer.getFunctionValue(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testStrictMonotonicFunctionBoundaryMinimum() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        // Strictly increasing: minimum is at lower bound
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 2.5 * x + 1.0;
            }
        };
        double result = optimizer.optimize(f, GoalType.MINIMIZE, 1.0, 5.0);
        assertEquals(1.0, result, 1e-4);
    }

    @Test(timeout = 4000)
    public void testStrictMonotonicFunctionBoundaryMaximum() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        // Strictly increasing: maximum is at upper bound
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 2.5 * x + 1.0;
            }
        };
        double result = optimizer.optimize(f, GoalType.MAXIMIZE, 1.0, 5.0);
        assertEquals(5.0, result, 1e-4);
    }

    @Test(timeout = 4000)
    public void testAsymmetricPolynomialBranches() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        // Quartic polynomial exercising multiple assignment branches for v, w, x
        // f(x) = (x + 1)^2 * (x - 2)^2
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.pow(x + 1.0, 2.0) * Math.pow(x - 2.0, 2.0);
            }
        };
        double result = optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 3.0, 0.5);
        assertEquals(2.0, result, 1e-5);
    }

    @Test(timeout = 4000)
    public void testConvexSharpCurvature() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        // Sharp curvature exercising step clamping (Math.abs(d) < tol1)
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.abs(x - 1.23456789);
            }
        };
        double result = optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 2.0);
        assertEquals(1.23456789, result, 1e-4);
    }

    @Test(timeout = 4000)
    public void testMaximizationHistoryAssignmentBranches() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        // Multi-extrema function to exercise fu > fx branches under MAXIMIZE
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return -Math.cos(x) - 0.1 * Math.sin(3.0 * x);
            }
        };
        double result = optimizer.optimize(f, GoalType.MAXIMIZE, 1.0, 5.0, 2.5);
        assertEquals(Math.PI, result, 1e-2);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect observed in BrentOptimizerTest::testSinMin:
     * Due to premature convergence or accuracy tolerances in BrentOptimizer,
     * the optimizer returns 4.71238897901431 instead of 3*pi/2 (4.71238898038469),
     * causing an AssertionFailedError when asserted with delta 1e-10.
     */
    @Test(timeout = 4000)
    public void testSinMinDefect() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        double result = optimizer.optimize(f, GoalType.MINIMIZE, 4.0, 5.0);
        // Ground truth expected exact convergence: 3 * Math.PI / 2 within 1e-10
        assertEquals(3.0 * Math.PI / 2.0, result, 1e-10);
    }

    /**
     * Targets the defect observed in MultiStartUnivariateRealOptimizerTest::testQuinticMin:
     * Quintic function: (x - 1)(x - 0.5)x(x + 0.5)(x + 1) has local minimum around -0.2719561270319131.
     * The defective implementation produces -0.2719561299044896.
     */
    @Test(timeout = 4000)
    public void testQuinticMinDefect() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 0.5) * x * (x + 0.5) * (x + 1.0);
            }
        };
        double result = optimizer.optimize(f, GoalType.MINIMIZE, -0.3, -0.2);
        assertEquals(-0.2719561270319131, result, 1e-10);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testDoOptimizeThrowsUnsupportedOperationException() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.doOptimize();
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testRelativeAccuracyNotStrictlyPositiveThrows() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setRelativeAccuracy(0.0);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x;
            }
        };
        optimizer.optimize(f, GoalType.MINIMIZE, -1.0, 1.0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testRelativeAccuracyNegativeThrows() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setRelativeAccuracy(-1.0e-10);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x;
            }
        };
        optimizer.optimize(f, GoalType.MINIMIZE, -1.0, 1.0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testAbsoluteAccuracyNotStrictlyPositiveThrows() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(0.0);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x;
            }
        };
        optimizer.optimize(f, GoalType.MINIMIZE, -1.0, 1.0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testAbsoluteAccuracyNegativeThrows() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(-1.0e-5);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x;
            }
        };
        optimizer.optimize(f, GoalType.MINIMIZE, -1.0, 1.0);
    }

    @Test(expected = MaxIterationsExceededException.class, timeout = 4000)
    public void testMaximalIterationCountExceededThrows() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setMaximalIterationCount(1);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 10.0);
    }

    @Test(timeout = 4000)
    public void testFunctionEvaluationExceptionPropagated() {
        BrentOptimizer optimizer = new BrentOptimizer();
        final String errMsg = "Simulated evaluation failure";
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) throws FunctionEvaluationException {
                throw new FunctionEvaluationException(x, errMsg);
            }
        };
        try {
            optimizer.optimize(f, GoalType.MINIMIZE, -1.0, 1.0);
            fail("Expected FunctionEvaluationException was not thrown");
        } catch (FunctionEvaluationException fee) {
            assertTrue(fee.getMessage().contains(errMsg));
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getClass().getName());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testResultClearedBeforeOptimization() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 1.0);
            }
        };
        optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 2.0);
        assertEquals(1.0, optimizer.getResult(), 1e-6);

        // Reset accuracies to illegal values to cause early abort and verify clearResult
        optimizer.setAbsoluteAccuracy(-1.0);
        try {
            optimizer.optimize(f, GoalType.MINIMIZE, 0.0, 2.0);
            fail("Expected NotStrictlyPositiveException");
        } catch (NotStrictlyPositiveException expected) {
            // Success
        }

        try {
            optimizer.getResult();
            fail("Expected IllegalStateException since result was cleared");
        } catch (IllegalStateException expected) {
            // Verified that clearResult() wiped prior computed optimum
        }
    }

    @Test(timeout = 4000)
    public void testResetAccuraciesToDefaults() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer();
        optimizer.setAbsoluteAccuracy(1e-4);
        optimizer.setRelativeAccuracy(1e-4);
        assertEquals(1e-4, optimizer.getAbsoluteAccuracy(), 0.0);
        assertEquals(1e-4, optimizer.getRelativeAccuracy(), 0.0);

        optimizer.resetAbsoluteAccuracy();
        optimizer.resetRelativeAccuracy();
        // AbstractUnivariateRealOptimizer default accuracies
        assertEquals(1.0e-10, optimizer.getAbsoluteAccuracy(), 1e-15);
        assertEquals(1.0e-14, optimizer.getRelativeAccuracy(), 1e-15);
    }
}