package org.apache.commons.math3.optimization.univariate;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target: org.apache.commons.math3.optimization.univariate.BrentOptimizer
 *
 * 1. Constructor Guard Branches:
 *    - rel < MIN_RELATIVE_TOLERANCE (2 * FastMath.ulp(1.0)): NumberIsTooSmallException
 *    - rel == MIN_RELATIVE_TOLERANCE: Boundary valid
 *    - abs <= 0 (0.0 and negative values): NotStrictlyPositiveException
 *    - abs > 0: Valid
 *    - checker == null vs checker != null
 *
 * 2. Optimization Control Flow & Boundaries:
 *    - Interval ordering: lo < hi vs lo > hi (swapped interval)
 *    - GoalType: MINIMIZE vs MAXIMIZE
 *    - Initial point positioning: mid at minimum, mid < minimum, mid > minimum, mid == lo, mid == hi
 *    - Parabolic interpolation fit vs Golden section fallback:
 *      * abs(e) > tol1
 *      * q > 0 vs q <= 0
 *      * Parabolic conditions: p in (q*(a-x), q*(b-x)) and abs(p) < abs(0.5*q*r)
 *      * Proximity check: u - a < tol2 || b - u < tol2 (with x <= m vs x > m)
 *      * Step clipping: abs(d) < tol1 (with d >= 0 vs d < 0)
 *    - Function value updates:
 *      * fu <= fx (u < x vs u >= x)
 *      * fu > fx (u < x vs u >= x)
 *      * fu <= fw || Precision.equals(w, x)
 *      * fu <= fv || Precision.equals(v, x) || Precision.equals(v, w)
 *    - Custom ConvergenceChecker:
 *      * Early termination via checker.converged()
 *      * Checker returning false till standard Brent criterion stops
 *
 * 3. Defects4J Defect Target (testKeepInitIfBest):
 *    - If the initial guess `startValue` happens to be the global minimum/optimum,
 *      BrentOptimizer must not discard the initial point in favor of worse subsequent evaluations.
 */
public class BrentOptimizerGeminiTest {

    private static final double MIN_REL = 2 * FastMath.ulp(1.0);

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardMinimizationParabola() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return (x - 3.0) * (x - 3.0) + 5.0;
            }
        };

        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, 5.0, 1.0);
        assertNotNull(result);
        assertEquals(3.0, result.getPoint(), 1e-6);
        assertEquals(5.0, result.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testStandardMaximizationCubic() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return -2.0 * x * x + 4.0 * x + 1.0; // Max at x = 1.0, f(1.0) = 3.0
            }
        };

        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MAXIMIZE, -2.0, 4.0, 0.0);
        assertNotNull(result);
        assertEquals(1.0, result.getPoint(), 1e-6);
        assertEquals(3.0, result.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testCustomConvergenceCheckerEarlyExit() {
        final int maxIterAllowed = 3;
        ConvergenceChecker<UnivariatePointValuePair> checker = new ConvergenceChecker<UnivariatePointValuePair>() {
            @Override
            public boolean converged(int iteration, UnivariatePointValuePair previous, UnivariatePointValuePair current) {
                return iteration >= maxIterAllowed;
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14, checker);
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return FastMath.cos(x);
            }
        };

        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 0.0, FastMath.PI * 2, 2.0);
        assertNotNull(result);
        assertTrue(optimizer.getEvaluations() <= maxIterAllowed + 5);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testInvertedIntervalLoGreaterThanHi() {
        // lo > hi exercises the (lo < hi) else branch: a = hi; b = lo;
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return (x - 2.0) * (x - 2.0);
            }
        };

        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 10.0, -10.0, 0.0);
        assertNotNull(result);
        assertEquals(2.0, result.getPoint(), 1e-6);
        assertEquals(0.0, result.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testConstructorBoundaryExactMinRelativeTolerance() {
        BrentOptimizer optimizer = new BrentOptimizer(MIN_REL, 1e-10);
        assertNotNull(optimizer);
    }

    @Test(timeout = 4000)
    public void testStartValueNearBoundaries() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return x * x;
            }
        };

        // Start value close to lower bound
        UnivariatePointValuePair resLow = optimizer.optimize(100, f, GoalType.MINIMIZE, -5.0, 5.0, -4.99);
        assertEquals(0.0, resLow.getPoint(), 1e-6);

        // Start value close to upper bound
        UnivariatePointValuePair resHigh = optimizer.optimize(100, f, GoalType.MINIMIZE, -5.0, 5.0, 4.99);
        assertEquals(0.0, resHigh.getPoint(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testFlatFunctionForcesGoldenSectionSteps() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-10);
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return 42.0; // Completely flat, tests equality logic in updating v, w, x
            }
        };

        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -2.0, 2.0, 0.5);
        assertNotNull(result);
        assertEquals(42.0, result.getValue(), 1e-12);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J known issue where the initial guess is already the optimal point.
     * The optimizer must report the best point encountered (the initial guess),
     * rather than discarding it for a worse sub-step evaluated later.
     */
    @Test(timeout = 4000)
    public void testKeepInitIfBestMinimization() {
        final double optimalX = 4.0;
        final double optimalY = 2.0;
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return (x - optimalX) * (x - optimalX) + optimalY;
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, 3.0, 5.0, optimalX);

        assertEquals("Best point not reported when init is optimal (Minimization)", optimalX, result.getPoint(), 0.0);
        assertEquals("Best value not reported when init is optimal (Minimization)", optimalY, result.getValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testKeepInitIfBestMaximization() {
        final double optimalX = 1.5;
        final double optimalY = 10.0;
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return -((x - optimalX) * (x - optimalX)) + optimalY;
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MAXIMIZE, 0.0, 3.0, optimalX);

        assertEquals("Best point not reported when init is optimal (Maximization)", optimalX, result.getPoint(), 0.0);
        assertEquals("Best value not reported when init is optimal (Maximization)", optimalY, result.getValue(), 0.0);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testConstructorRelativeThresholdTooSmall() {
        new BrentOptimizer(MIN_REL - 1e-20, 1e-8);
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testConstructorRelativeThresholdZero() {
        new BrentOptimizer(0.0, 1e-8);
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testConstructorRelativeThresholdNegative() {
        new BrentOptimizer(-1.0, 1e-8);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorAbsoluteThresholdZero() {
        new BrentOptimizer(1e-5, 0.0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorAbsoluteThresholdNegative() {
        new BrentOptimizer(1e-5, -1e-8);
    }

    @Test(expected = TooManyEvaluationsException.class, timeout = 4000)
    public void testMaxEvaluationsExceeded() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return FastMath.sin(x);
            }
        };

        // Allowed only 2 evaluations: will fail to converge within budget
        optimizer.optimize(2, f, GoalType.MINIMIZE, 0.0, 10.0, 5.0);
    }

    // =========================================================================
    // Partition E: Internal Branching & Path Coverage Stressors
    // =========================================================================

    @Test(timeout = 4000)
    public void testParabolicStepProximityBranch() {
        // Asymmetric non-linear function designed to trigger parabolic fit near boundaries
        BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-10);
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return FastMath.exp(-x) + FastMath.pow(x, 4);
            }
        };

        UnivariatePointValuePair result = optimizer.optimize(150, f, GoalType.MINIMIZE, 0.0, 2.0, 0.1);
        assertNotNull(result);
        assertTrue(result.getPoint() > 0.0 && result.getPoint() < 2.0);
    }

    @Test(timeout = 4000)
    public void testConvexFunctionWithNegativeMinimum() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-9, 1e-12);
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return (x + 7.5) * (x + 7.5) - 100.0;
            }
        };

        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -10.0, -5.0, -6.0);
        assertNotNull(result);
        assertEquals(-7.5, result.getPoint(), 1e-6);
        assertEquals(-100.0, result.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testDefaultConvergenceCheckerIsProperlyInvokedWhenPresent() {
        final boolean[] checkerInvoked = new boolean[] { false };
        ConvergenceChecker<UnivariatePointValuePair> checker = new ConvergenceChecker<UnivariatePointValuePair>() {
            @Override
            public boolean converged(int iteration, UnivariatePointValuePair previous, UnivariatePointValuePair current) {
                checkerInvoked[0] = true;
                return false; // let Brent stop naturally
            }
        };

        BrentOptimizer optimizer = new BrentOptimizer(1e-8, 1e-10, checker);
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return x * x;
            }
        };

        optimizer.optimize(100, f, GoalType.MINIMIZE, -1.0, 1.0, 0.5);
        assertTrue("Checker should have been invoked during optimization", checkerInvoked[0]);
    }

    @Test(timeout = 4000)
    public void testEqualEvaluationPointsInBestSelection() {
        // Symmetric function around 0 where x and -x yield exact same value
        BrentOptimizer optimizer = new BrentOptimizer(1e-6, 1e-8);
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return FastMath.abs(x);
            }
        };

        UnivariatePointValuePair result = optimizer.optimize(100, f, GoalType.MINIMIZE, -1.0, 1.0, 0.2);
        assertNotNull(result);
        assertEquals(0.0, result.getPoint(), 1e-4);
        assertEquals(0.0, result.getValue(), 1e-4);
    }
}