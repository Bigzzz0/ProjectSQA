package org.apache.commons.math3.optimization.univariate;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: org.apache.commons.math3.optimization.univariate.BrentOptimizer
 *
 * 1. Constructor Guard Conditions:
 *    - relativeThreshold < MIN_RELATIVE_TOLERANCE (2 * ulp(1.0)) -> NumberIsTooSmallException
 *    - relativeThreshold == MIN_RELATIVE_TOLERANCE (valid boundary)
 *    - absoluteThreshold <= 0 -> NotStrictlyPositiveException (0.0, negative)
 *    - absoluteThreshold > 0 (valid boundary)
 *    - 2-arg constructor chaining to 3-arg constructor with null checker
 *
 * 2. Optimization Loop Decision Branches (doOptimize):
 *    - GoalType: MINIMIZE vs MAXIMIZE (fx / fu negation)
 *    - Interval ordering: lo < hi vs lo >= hi (boundary swapping)
 *    - Parabolic interpolation fit vs Golden Section:
 *      * FastMath.abs(e) > tol1 (fit parabola) vs <= tol1 (golden section)
 *      * q > 0 (p = -p) vs q <= 0 (q = -q)
 *      * Acceptance condition: p > q*(a-x) && p < q*(b-x) && |p| < |0.5*q*r|
 *      * Parabola evaluation safety: u - a < tol2 || b - u < tol2 (with x <= m vs x > m)
 *      * Golden section sub-branch: x < m vs x >= m
 *    - Minimum step threshold: FastMath.abs(d) < tol1 (with d >= 0 vs d < 0) vs abs(d) >= tol1
 *    - Point replacement ordering:
 *      * fu <= fx: u < x (b = x) vs u >= x (a = x)
 *      * fu > fx: u < x (a = u) vs u >= x (b = u)
 *      * fu <= fw || Precision.equals(w, x)
 *      * fu <= fv || Precision.equals(v, x) || Precision.equals(v, w)
 *    - Termination criteria:
 *      * Custom ConvergenceChecker: checker != null && checker.converged(...)
 *      * Default Brent stopping condition: |x - m| <= tol2 - 0.5*(b - a)
 *
 * 3. Private Helper Method (best):
 *    - a == null, b != null -> returns b
 *    - a != null, b == null -> returns a
 *    - a == null, b == null -> returns null
 *    - isMinim = true: a.val < b.val ? a : b
 *    - isMinim = false: a.val > b.val ? a : b
 *
 * 4. Ground Truth Defect (MATH-855):
 *    - BrentOptimizerTest::testMath855: The optimizer reports the last evaluated point
 *      (`current`) upon convergence rather than the best evaluation found so far (`best`).
 *      When using a ConvergenceChecker or at termination where the last step is worse
 *      than a previous step, `current` is returned despite `best` having a better value.
 */
public class BrentOptimizerGeminiTest {

    private static final double MIN_REL_TOL = 2 * FastMath.ulp(1d);

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardMinimizationParabolic() {
        // Standard convex quadratic function: min at x = 2.0 with value = 0.0
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 2.0) * (x - 2.0);
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 5.0, 1.0);

        assertEquals(2.0, result.getPoint(), 1e-6);
        assertEquals(0.0, result.getValue(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testStandardMaximization() {
        // Concave quadratic function: max at x = 3.0 with value = 7.0
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return -(x - 3.0) * (x - 3.0) + 7.0;
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MAXIMIZE, -2.0, 8.0, 0.0);

        assertEquals(3.0, result.getPoint(), 1e-6);
        assertEquals(7.0, result.getValue(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testQuinticFunctionMultimodal() {
        // f(x) = x * (x^2 - 1) * (x^2 - 0.25)
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * (x * x - 1.0) * (x * x - 0.25);
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        // Search local minimum around -0.27
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -0.3, -0.1, -0.2);

        assertEquals(-0.27195613, result.getPoint(), 1e-5);
    }

    @Test(timeout = 4000)
    public void testCustomConvergenceCheckerPath() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 1.0) + 4.0;
            }
        };

        // Checker that converges after 5 iterations
        ConvergenceChecker<UnivariatePointValuePair> checker = new ConvergenceChecker<UnivariatePointValuePair>() {
            public boolean converged(int iteration, UnivariatePointValuePair previous, UnivariatePointValuePair current) {
                return iteration >= 5;
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-10, checker);
        UnivariatePointValuePair result = optimizer.optimize(50, f, GoalType.MINIMIZE, 0.0, 2.0, 0.5);

        assertNotNull(result);
        assertTrue(result.getValue() >= 4.0);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorWithExactMinRelativeTolerance() {
        // Valid edge case: rel == MIN_RELATIVE_TOLERANCE
        BrentOptimizer optimizer = new BrentOptimizer(MIN_REL_TOL, 1e-10);
        assertNotNull(optimizer);
    }

    @Test(timeout = 4000)
    public void testInvertedIntervalBounds() {
        // min > max triggers the else branch (a = hi, b = lo)
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 1.5) * (x - 1.5);
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 4.0, -1.0, 0.0);

        assertEquals(1.5, result.getPoint(), 1e-6);
        assertEquals(0.0, result.getValue(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testMinimumNearLowerBoundary() {
        // Triggers parabolic step near lower boundary (u - a < tol2)
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 0.0001) * (x - 0.0001);
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-10);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 1.0, 0.5);

        assertEquals(0.0001, result.getPoint(), 1e-4);
    }

    @Test(timeout = 4000)
    public void testMinimumNearUpperBoundary() {
        // Triggers parabolic step near upper boundary (b - u < tol2)
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return (x - 0.9999) * (x - 0.9999);
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-10);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 1.0, 0.5);

        assertEquals(0.9999, result.getPoint(), 1e-4);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-855)
    // =========================================================================

    /**
     * Targets Defects4J MATH-855:
     * BrentOptimizer must report the point with the lowest objective value found,
     * not simply the last evaluated point (`current`) when stopping or converging.
     */
    @Test(timeout = 4000)
    public void testMath855() {
        final UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };

        final ConvergenceChecker<UnivariatePointValuePair> checker
            = new SimpleUnivariateValueChecker(1e-5, 1e-14);

        final BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14, checker);
        final UnivariatePointValuePair result
            = optimizer.optimize(100, f, GoalType.MINIMIZE, -100.0, 100.0, 0.0);

        assertTrue("Best point not reported", result.getValue() <= -0.99999999);
    }

    @Test(timeout = 4000)
    public void testKeepInitIfBetterMinimization() {
        // Initial point x = 0 is the exact global minimum
        final UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x == 0.0 ? -10.0 : x * x;
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -5.0, 5.0, 0.0);

        assertEquals("Best point must be reported even if initial point was best", -10.0, result.getValue(), 1e-6);
        assertEquals(0.0, result.getPoint(), 1e-6);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testRelativeToleranceBelowMinimumThrows() {
        new BrentOptimizer(MIN_REL_TOL - 1e-18, 1e-10);
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testRelativeToleranceZeroThrows() {
        new BrentOptimizer(0.0, 1e-10);
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testRelativeToleranceNegativeThrows() {
        new BrentOptimizer(-1e-5, 1e-10);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testAbsoluteToleranceZeroThrows() {
        new BrentOptimizer(1e-5, 0.0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testAbsoluteToleranceNegativeThrows() {
        new BrentOptimizer(1e-5, -1.0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorWithCheckerThrowsOnInvalidAbs() {
        ConvergenceChecker<UnivariatePointValuePair> checker = new SimpleUnivariateValueChecker(1e-5, 1e-10);
        new BrentOptimizer(1e-5, -1e-10, checker);
    }

    // =========================================================================
    // Partition E: Object Contract & Private Method Branch Completeness
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrivateBestMethodAllBranchesViaReflection() throws Exception {
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        Method bestMethod = BrentOptimizer.class.getDeclaredMethod(
            "best",
            UnivariatePointValuePair.class,
            UnivariatePointValuePair.class,
            boolean.class
        );
        bestMethod.setAccessible(true);

        UnivariatePointValuePair pLow = new UnivariatePointValuePair(1.0, 10.0);
        UnivariatePointValuePair pHigh = new UnivariatePointValuePair(2.0, 20.0);

        // a == null -> returns b
        assertSame(pHigh, bestMethod.invoke(optimizer, null, pHigh, true));
        assertSame(pHigh, bestMethod.invoke(optimizer, null, pHigh, false));

        // b == null -> returns a
        assertSame(pLow, bestMethod.invoke(optimizer, pLow, null, true));
        assertSame(pLow, bestMethod.invoke(optimizer, pLow, null, false));

        // both null -> returns null
        assertNull(bestMethod.invoke(optimizer, null, null, true));
        assertNull(bestMethod.invoke(optimizer, null, null, false));

        // isMinim = true: lower value wins
        assertSame(pLow, bestMethod.invoke(optimizer, pLow, pHigh, true));
        assertSame(pLow, bestMethod.invoke(optimizer, pHigh, pLow, true));

        // isMinim = false: higher value wins
        assertSame(pHigh, bestMethod.invoke(optimizer, pLow, pHigh, false));
        assertSame(pHigh, bestMethod.invoke(optimizer, pHigh, pLow, false));
    }
}