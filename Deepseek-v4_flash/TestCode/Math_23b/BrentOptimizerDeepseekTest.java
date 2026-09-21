package org.apache.commons.math3.optimization.univariate;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.GoalType;

/*
 * [Branch & Defect Analysis Matrix]
 * ================================
 * Target: BrentOptimizer.doOptimize() and constructors.
 *
 * Defect targeted:
 *   Brent's original termination returns best(current, previous).  If the initial
 *   guess is the best point and two worse points have been evaluated, the initial
 *   point is no longer current or previous, so it is silently lost.  The test
 *   testKeepInitIfBest() forces convergence after the second evaluation and
 *   asserts that the initial best point is still returned.
 *
 * Branch coverage highlights:
 *   - Constructor validation: rel < MIN_RELATIVE_TOLERANCE, abs <= 0.
 *   - doOptimize: MINIMIZE vs MAXIMIZE.
 *   - Interval order: lo < hi and lo > hi.
 *   - Default stopping criterion: stop == true and false.
 *   - Golden-section vs parabolic interpolation path.
 *   - User-supplied ConvergenceChecker: null, converged on first call, converged
 *     on later call, never converged.
 *   - Update rules: fu <= fx, u < x, fu <= fw, Precision.equals(w,x),
 *     fu <= fv, Precision.equals(v,x), Precision.equals(v,w).
 */
public class BrentOptimizerDeepseekTest {

    @Test(timeout = 4000)
    public void testKeepInitIfBest() {
        // The initial guess is the global minimum.  The early convergence checker
        // stops the algorithm after two worse points have been evaluated.
        final UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x;
            }
        };

        final ConvergenceChecker<UnivariatePointValuePair> checker =
            new ConvergenceChecker<UnivariatePointValuePair>() {
                public boolean converged(int iteration,
                                         UnivariatePointValuePair previous,
                                         UnivariatePointValuePair current) {
                    return iteration >= 1;
                }
            };

        final BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-10, checker);
        final UnivariatePointValuePair result =
            optimizer.optimize(100, f, GoalType.MINIMIZE, -1, 1, 0);

        // Correct behavior: the best point encountered (the initial guess) is returned.
        assertEquals(0.0, result.getPoint(), 1e-12);
        assertEquals(0.0, result.getValue(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testMinimizeQuadratic() {
        final UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                final double d = x - 2;
                return d * d + 1;
            }
        };

        final BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-10);
        final UnivariatePointValuePair result =
            optimizer.optimize(1000, f, GoalType.MINIMIZE, -10, 10, 0);

        assertEquals(2.0, result.getPoint(), 1e-8);
        assertEquals(1.0, result.getValue(), 1e-8);
    }

    @Test(timeout = 4000)
    public void testMaximizeQuadratic() {
        final UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                final double d = x - 2;
                return -(d * d) + 1;
            }
        };

        final BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-10);
        final UnivariatePointValuePair result =
            optimizer.optimize(1000, f, GoalType.MAXIMIZE, -10, 10, 0);

        assertEquals(2.0, result.getPoint(), 1e-8);
        assertEquals(1.0, result.getValue(), 1e-8);
    }

    @Test(timeout = 4000)
    public void testMinimizeWithStartRight() {
        // Start on the right side of the minimum; first evaluated point is better.
        final UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                final double d = x - 2;
                return d * d + 1;
            }
        };

        final BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-10);
        final UnivariatePointValuePair result =
            optimizer.optimize(1000, f, GoalType.MINIMIZE, -10, 10, 8);

        assertEquals(2.0, result.getPoint(), 1e-8);
        assertEquals(1.0, result.getValue(), 1e-8);
    }

    @Test(timeout = 4000)
    public void testMinimizeWithStartLeft() {
        // Start on the left side of the minimum; first evaluated point is better.
        final UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                final double d = x - 2;
                return d * d + 1;
            }
        };

        final BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-10);
        final UnivariatePointValuePair result =
            optimizer.optimize(1000, f, GoalType.MINIMIZE, -10, 10, -8);

        assertEquals(2.0, result.getPoint(), 1e-8);
        assertEquals(1.0, result.getValue(), 1e-8);
    }

    @Test(timeout = 4000)
    public void testReversedInterval() {
        // doOptimize explicitly handles lo > hi by swapping the interval.
        final UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x;
            }
        };

        final BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-10);
        final UnivariatePointValuePair result =
            optimizer.optimize(1000, f, GoalType.MINIMIZE, 1, -1, 0);

        assertEquals(0.0, result.getPoint(), 1e-8);
        assertEquals(0.0, result.getValue(), 1e-8);
    }

    @Test(timeout = 4000)
    public void testStopImmediately() {
        // The default stopping criterion is satisfied immediately.
        final UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x;
            }
        };

        final BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-10);
        final UnivariatePointValuePair result =
            optimizer.optimize(100, f, GoalType.MINIMIZE, -1e-12, 1e-12, 0);

        assertEquals(0.0, result.getPoint(), 0.0);
        assertEquals(0.0, result.getValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCheckerConvergesImmediately() {
        // Convergence checker stops after the first evaluated point; the initial best is kept.
        final UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x;
            }
        };

        final ConvergenceChecker<UnivariatePointValuePair> checker =
            new ConvergenceChecker<UnivariatePointValuePair>() {
                public boolean converged(int iteration,
                                         UnivariatePointValuePair previous,
                                         UnivariatePointValuePair current) {
                    return true;
                }
            };

        final BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-10, checker);
        final UnivariatePointValuePair result =
            optimizer.optimize(100, f, GoalType.MINIMIZE, -1, 1, 0);

        assertEquals(0.0, result.getPoint(), 1e-12);
        assertEquals(0.0, result.getValue(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testCheckerNeverConverges() {
        // Checker never converges; default Brent termination must still work.
        final UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x;
            }
        };

        final ConvergenceChecker<UnivariatePointValuePair> checker =
            new ConvergenceChecker<UnivariatePointValuePair>() {
                public boolean converged(int iteration,
                                         UnivariatePointValuePair previous,
                                         UnivariatePointValuePair current) {
                    return false;
                }
            };

        final BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-10, checker);
        final UnivariatePointValuePair result =
            optimizer.optimize(1000, f, GoalType.MINIMIZE, -1, 1, 0.5);

        assertEquals(0.0, result.getPoint(), 1e-8);
        assertEquals(0.0, result.getValue(), 1e-8);
    }

    @Test(timeout = 4000)
    public void testConstructorRejectsSmallRelativeThreshold() {
        // rel must be >= 2 * Math.ulp(1d).
        try {
            new BrentOptimizer(0, 1e-10);
            fail("Expected NumberIsTooSmallException");
        } catch (NumberIsTooSmallException expected) {
            // Expected.
        }
    }

    @Test(timeout = 4000)
    public void testConstructorRejectsNegativeRelativeThreshold() {
        try {
            new BrentOptimizer(-1, 1e-10);
            fail("Expected NumberIsTooSmallException");
        } catch (NumberIsTooSmallException expected) {
            // Expected.
        }
    }

    @Test(timeout = 4000)
    public void testConstructorRejectsZeroAbsoluteThreshold() {
        try {
            new BrentOptimizer(1e-10, 0);
            fail("Expected NotStrictlyPositiveException");
        } catch (NotStrictlyPositiveException expected) {
            // Expected.
        }
    }

    @Test(timeout = 4000)
    public void testConstructorRejectsNegativeAbsoluteThreshold() {
        try {
            new BrentOptimizer(1e-10, -1);
            fail("Expected NotStrictlyPositiveException");
        } catch (NotStrictlyPositiveException expected) {
            // Expected.
        }
    }

    @Test(timeout = 4000)
    public void testConstructorAllowsBoundaryRelativeThreshold() {
        final double minRel = 2 * Math.ulp(1d);
        final BrentOptimizer optimizer = new BrentOptimizer(minRel, 1e-10);
        assertNotNull(optimizer);
    }

    @Test(timeout = 4000)
    public void testNullCheckerIsAllowed() {
        final BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-10, null);
        assertNull(optimizer.getConvergenceChecker());
    }

    @Test(timeout = 4000)
    public void testCheckerIsStored() {
        final ConvergenceChecker<UnivariatePointValuePair> checker =
            new ConvergenceChecker<UnivariatePointValuePair>() {
                public boolean converged(int iteration,
                                         UnivariatePointValuePair previous,
                                         UnivariatePointValuePair current) {
                    return true;
                }
            };

        final BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-10, checker);
        assertSame(checker, optimizer.getConvergenceChecker());
    }
}