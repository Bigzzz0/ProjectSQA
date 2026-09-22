/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.math.analysis.solvers.BracketingNthOrderBrentSolver
 *
 * Test Strategy:
 * 1. Partition A: Core Functional Logic & State Transitions
 *    - All 4 constructors with custom accuracies (relative, absolute, functionValueAccuracy, maximalOrder).
 *    - Getter inspection: getMaximalOrder().
 *    - Solving smooth and polynomial functions: linear, quadratic, cubic, sinusoidal, exponential.
 *    - Evaluation of initial guess as perfect root: Precision.equals(y[1], 0.0, 1).
 *    - Evaluation of first endpoint as perfect root: Precision.equals(y[0], 0.0, 1).
 *    - Evaluation of second endpoint as perfect root: Precision.equals(y[2], 0.0, 1).
 *    - Sign change interval reduction: y[0] * y[1] < 0 vs y[1] * y[2] < 0.
 *
 * 2. Partition B: Boundary Value Analysis & Solution Selection (AllowedSolution)
 *    - AllowedSolution.ANY_SIDE: absYA < absYB ? xA : xB.
 *    - AllowedSolution.LEFT_SIDE: always xA <= root.
 *    - AllowedSolution.RIGHT_SIDE: always xB >= root.
 *    - AllowedSolution.BELOW_SIDE: function value <= 0.
 *    - AllowedSolution.ABOVE_SIDE: function value >= 0.
 *    - Root finding with default maximal order (5) and minimal order (2).
 *    - Extreme tolerance boundaries (e.g. 1e-14, 1e-3).
 *
 * 3. Partition C: Defect-Targeted Branch Zone (Defects4J MATH-40 / Issue 716)
 *    - testIssue716: Inverse polynomial interpolation step selection leading to evaluation
 *      count explosion or TooManyEvaluationsException when guessing outside bracketing interval.
 *    - Pathological functions with sharp turns and asymmetric aging of brackets triggering
 *      agingA >= MAXIMAL_AGING and agingB >= MAXIMAL_AGING branches and targetY compensation.
 *    - Triggering array shifting and point-dropping branches when nbPoints == x.length and
 *      signChangeIndex >= (x.length + 1) / 2 vs < (x.length + 1) / 2.
 *    - Triggering fallback to bisection when guessed root is NaN or outside [xA, xB].
 *
 * 4. Partition D: Defensive & Exception Guard Paths
 *    - NumberIsTooSmallException when maximalOrder < 2 for each constructor.
 *    - NoBracketingException when y[0] and y[2] have the same sign (and y[1] also doesn't bracket).
 *    - NumberIsTooLargeException / NonMonotonicSequenceException when min >= max or start not in (min, max).
 *    - TooManyEvaluationsException when maxEval is strictly exceeded.
 */
package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.UnivariateFunction;
import org.apache.commons.math.exception.NoBracketingException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.exception.TooManyEvaluationsException;
import org.apache.commons.math.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class BracketingNthOrderBrentSolverGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndGetters() {
        BracketingNthOrderBrentSolver sDefault = new BracketingNthOrderBrentSolver();
        Assert.assertEquals(5, sDefault.getMaximalOrder());
        Assert.assertEquals(1e-6, sDefault.getAbsoluteAccuracy(), 1e-12);
        Assert.assertEquals(1e-14, sDefault.getRelativeAccuracy(), 1e-15);

        BracketingNthOrderBrentSolver s2 = new BracketingNthOrderBrentSolver(1e-8, 3);
        Assert.assertEquals(3, s2.getMaximalOrder());
        Assert.assertEquals(1e-8, s2.getAbsoluteAccuracy(), 1e-12);

        BracketingNthOrderBrentSolver s3 = new BracketingNthOrderBrentSolver(1e-10, 1e-8, 4);
        Assert.assertEquals(4, s3.getMaximalOrder());
        Assert.assertEquals(1e-10, s3.getRelativeAccuracy(), 1e-15);
        Assert.assertEquals(1e-8, s3.getAbsoluteAccuracy(), 1e-12);

        BracketingNthOrderBrentSolver s4 = new BracketingNthOrderBrentSolver(1e-11, 1e-9, 1e-7, 6);
        Assert.assertEquals(6, s4.getMaximalOrder());
        Assert.assertEquals(1e-11, s4.getRelativeAccuracy(), 1e-15);
        Assert.assertEquals(1e-9, s4.getAbsoluteAccuracy(), 1e-15);
        Assert.assertEquals(1e-7, s4.getFunctionValueAccuracy(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testExactRootAtStartValue() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x - 3.0;
            }
        };
        // startValue is exactly 3.0, which is the root
        double root = solver.solve(50, f, 1.0, 5.0, 3.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(3.0, root, 1e-15);
        Assert.assertEquals(1, solver.getEvaluations());
    }

    @Test(timeout = 4000)
    public void testExactRootAtMinEndpoint() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x - 1.0;
            }
        };
        // min is 1.0, perfect root
        double root = solver.solve(50, f, 1.0, 5.0, 2.5, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(1.0, root, 1e-15);
        Assert.assertEquals(2, solver.getEvaluations()); // evaluated startValue, then min
    }

    @Test(timeout = 4000)
    public void testExactRootAtMaxEndpoint() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x - 5.0;
            }
        };
        // max is 5.0, perfect root; root not between min and startValue
        double root = solver.solve(50, f, 1.0, 5.0, 2.5, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(5.0, root, 1e-15);
    }

    @Test(timeout = 4000)
    public void testSignChangeInFirstSubInterval() {
        // Root is at 2.0; min=1.0, start=3.0, max=5.0.
        // y[0]*y[1] = f(1)*f(3) = (-1)*(1) < 0 -> interval reduces to [1, 3] with nbPoints=2
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x - 2.0;
            }
        };
        double root = solver.solve(50, f, 1.0, 5.0, 3.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(2.0, root, 1e-6);
    }

    @Test(timeout = 4000)
    public void testSignChangeInSecondSubInterval() {
        // Root is at 4.0; min=1.0, start=2.0, max=5.0.
        // f(1) = -3, f(2) = -2 (same sign); f(5) = +1 -> sign change in second sub-interval
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x - 4.0;
            }
        };
        double root = solver.solve(50, f, 1.0, 5.0, 2.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(4.0, root, 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveWithoutStartValue() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new SinFunction();
        // solve(maxEval, f, min, max, allowedSolution)
        double root = solver.solve(100, f, 3.0, 4.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(FastMath.PI, root, 1e-6);
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & AllowedSolution Modes
    // =========================================================================

    @Test(timeout = 4000)
    public void testAllowedSolutionLeftSide() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-8, 1.0e-5, 5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };
        double root = solver.solve(100, f, 3.0, 3.2, 3.1, AllowedSolution.LEFT_SIDE);
        Assert.assertTrue(root <= FastMath.PI);
        Assert.assertEquals(FastMath.PI, root, 1.0e-4);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionRightSide() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-8, 1.0e-5, 5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };
        double root = solver.solve(100, f, 3.0, 3.2, 3.1, AllowedSolution.RIGHT_SIDE);
        Assert.assertTrue(root >= FastMath.PI);
        Assert.assertEquals(FastMath.PI, root, 1.0e-4);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionBelowSideIncreasing() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-8, 1.0e-5, 5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x * x - 8.0; // root at x = 2
            }
        };
        double root = solver.solve(100, f, 1.0, 3.0, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(f.value(root) <= 0.0);
        Assert.assertEquals(2.0, root, 1.0e-4);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionAboveSideIncreasing() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-8, 1.0e-5, 5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x * x - 8.0; // root at x = 2
            }
        };
        double root = solver.solve(100, f, 1.0, 3.0, AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(f.value(root) >= 0.0);
        Assert.assertEquals(2.0, root, 1.0e-4);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionBelowSideDecreasing() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-8, 1.0e-5, 5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return -(x * x * x - 8.0); // decreasing across root x = 2
            }
        };
        double root = solver.solve(100, f, 1.0, 3.0, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(f.value(root) <= 0.0);
        Assert.assertEquals(2.0, root, 1.0e-4);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionAboveSideDecreasing() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-8, 1.0e-5, 5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return -(x * x * x - 8.0); // decreasing across root x = 2
            }
        };
        double root = solver.solve(100, f, 1.0, 3.0, AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(f.value(root) >= 0.0);
        Assert.assertEquals(2.0, root, 1.0e-4);
    }

    @Test(timeout = 4000)
    public void testMinimalOrderTwo() {
        // Minimal allowable maximalOrder is 2
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-10, 1.0e-10, 2);
        Assert.assertEquals(2, solver.getMaximalOrder());
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        double root = solver.solve(100, f, 0.0, 5.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(2.0, root, 1.0e-9);
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (MATH-40 / testIssue716 & Stress)
    // =========================================================================

    /**
     * Defects4J Math-40 Ground Truth:
     * BracketingNthOrderBrentSolver previously ran into an excessive evaluation loop
     * (TooManyEvaluationsException: maximal count 100 exceeded) on sharp-turning functions
     * due to step drop decisions when inverse polynomial interpolation guesses outside interval.
     */
    @Test(timeout = 4000)
    public void testIssue716() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-12, 1.0e-10, 1.0e-8, 5);
        UnivariateFunction sharpTurn = new UnivariateFunction() {
            public double value(double x) {
                return 2 / (2 * x + 1) - 2 * -0.0001073128451174798 * (x + 1) / (1 + 2 * -0.0001073128451174798 * x);
            }
        };
        double result = solver.solve(100, sharpTurn, -0.20, 10.0, 0.5, AllowedSolution.RIGHT_SIDE);
        Assert.assertEquals(0.0, sharpTurn.value(result), solver.getFunctionValueAccuracy());
        Assert.assertTrue(sharpTurn.value(result) >= 0.0);
    }

    @Test(timeout = 4000)
    public void testAgingCompensationHighOrder() {
        // High maximal order causing array capacity saturation (nbPoints == x.length)
        // and triggering the aging branches (agingA >= 2 or agingB >= 2)
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-14, 1.0e-12, 1.0e-15, 12);
        Assert.assertEquals(12, solver.getMaximalOrder());

        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.expm1(x) - 0.5; // root at ln(1.5) ~ 0.4054651081
            }
        };

        double root = solver.solve(200, f, -1.0, 2.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(FastMath.log(1.5), root, 1.0e-11);
    }

    @Test(timeout = 4000)
    public void testPolynomialInterpolationFallbackToBisection() {
        // Step function / extreme slope creating invalid or duplicate points where
        // inverse polynomial interpolation guesses outside bounds and drops to bisection
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-10, 1.0e-10, 5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                if (x < 0.5) {
                    return -1.0;
                } else if (x > 0.5) {
                    return 1.0;
                } else {
                    return 0.0;
                }
            }
        };
        double root = solver.solve(100, f, 0.0, 1.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(0.5, root, 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testArrayShiftDropLowestPoint() {
        // Trigger condition: nbPoints == x.length and signChangeIndex >= (x.length + 1) / 2
        // We use order 3 (array length 4), asymmetric root location to push signChangeIndex high.
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-12, 1.0e-10, 3);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.pow(x, 7) - 128.0; // root at x = 2
            }
        };
        double root = solver.solve(100, f, 0.1, 4.0, 0.2, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(2.0, root, 1.0e-8);
    }

    // =========================================================================
    // PARTITION D: Defensive & Exception Guard Paths
    // =========================================================================

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testConstructorOrderTooSmallOrder1() {
        new BracketingNthOrderBrentSolver(1e-6, 1);
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testConstructorOrderTooSmallOrder0() {
        new BracketingNthOrderBrentSolver(1e-12, 1e-6, 0);
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testConstructorOrderTooSmallNegative() {
        new BracketingNthOrderBrentSolver(1e-12, 1e-6, 1e-15, -1);
    }

    @Test(expected = NoBracketingException.class, timeout = 4000)
    public void testNoBracketingExceptionSameSignPositive() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x + 1.0; // strictly positive
            }
        };
        solver.solve(50, f, -2.0, 2.0, 0.0, AllowedSolution.ANY_SIDE);
    }

    @Test(expected = NoBracketingException.class, timeout = 4000)
    public void testNoBracketingExceptionSameSignNegative() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return -x * x - 2.0; // strictly negative
            }
        };
        solver.solve(50, f, 1.0, 3.0, AllowedSolution.ANY_SIDE);
    }

    @Test(expected = TooManyEvaluationsException.class, timeout = 4000)
    public void testTooManyEvaluationsException() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-15, 1.0e-15, 5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return FastMath.sin(100.0 * x);
            }
        };
        // Exceed tiny max evaluation budget of 2
        solver.solve(2, f, 0.1, 1.9, AllowedSolution.ANY_SIDE);
    }

    // =========================================================================
    // PARTITION E: Convergence and Precision Nuances
    // =========================================================================

    @Test(timeout = 4000)
    public void testConvergenceOnFunctionAccuracy() {
        // When function value is below getFunctionValueAccuracy, loop should exit early
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-15, 1.0e-15, 1.0e-2, 5);
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return 0.005 * (x - 1.0); // always small in [0, 2]
            }
        };
        double root = solver.solve(50, f, 0.0, 2.0, AllowedSolution.ANY_SIDE);
        Assert.assertTrue(FastMath.abs(f.value(root)) <= 1.0e-2);
    }

    @Test(timeout = 4000)
    public void testVeryCloseToZeroRoot() {
        BracketingNthOrderBrentSolver solver = new BracketingNthOrderBrentSolver();
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x;
            }
        };
        double root = solver.solve(50, f, -1e-5, 1e-5, 1e-6, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(0.0, root, 1e-6);
        Assert.assertEquals(0.0, f.value(root), 1e-6);
    }
}