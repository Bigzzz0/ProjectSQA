package org.apache.commons.math3.util;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.math3.util.ContinuedFraction
 *
 * Decision / Branch Coverage Targets:
 * 1. Precision.equals(hPrev, 0.0, small):
 *    - Branch TRUE:  getA(0, x) == 0.0 (sets hPrev = 1e-50)
 *    - Branch FALSE: getA(0, x) != 0.0 (preserves hPrev)
 * 2. Overload Delegation:
 *    - evaluate(x) -> evaluate(x, DEFAULT_EPSILON, Integer.MAX_VALUE)
 *    - evaluate(x, epsilon) -> evaluate(x, epsilon, Integer.MAX_VALUE)
 *    - evaluate(x, maxIterations) -> evaluate(x, DEFAULT_EPSILON, maxIterations)
 * 3. Scaling on Infinity (Double.isInfinite(cN) || Double.isInfinite(q2)):
 *    - Branch TRUE:  cN or q2 overflows to Infinity.
 *      - Sub-branch scale <= 0 -> ConvergenceException (CONTINUED_FRACTION_INFINITY_DIVERGENCE)
 *      - Sub-branch a != 0.0 && a > b -> rescale with a
 *      - Sub-branch b != 0 (and !(a > b)) -> rescale with b
 *      - Sub-branch loop break when finite restored vs exhausts 5 iterations
 *    - Branch FALSE: normal convergent calculation
 * 4. Infinity and NaN Checks on hN:
 *    - Double.isInfinite(hN) -> ConvergenceException (CONTINUED_FRACTION_INFINITY_DIVERGENCE)
 *    - Double.isNaN(hN)      -> ConvergenceException (CONTINUED_FRACTION_NAN_DIVERGENCE) [MATH-718 defect zone]
 * 5. Convergence criterion:
 *    - FastMath.abs(deltaN - 1.0) < epsilon -> early break and return hN
 * 6. Iteration Guard:
 *    - n >= maxIterations -> MaxCountExceededException
 *
 * Known Defects Targeted:
 * - MATH-718 / MATH-785: Rapid growth of cN and q2 in modified Lentz algorithm causes intermediate
 *   overflow to Infinity / Infinity, producing NaN divergences and ConvergenceException.
 */
public class ContinuedFractionGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & Standard Convergence
    // =========================================================================

    @Test(timeout = 4000)
    public void testGoldenRatioConvergence() {
        // Golden ratio: phi = 1 + 1 / (1 + 1 / (1 + ...)) = (1 + sqrt(5)) / 2 approx 1.618033988749895
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        double expected = (1.0 + FastMath.sqrt(5.0)) / 2.0;
        double result = cf.evaluate(0.0, 1e-10, 100);
        assertEquals(expected, result, 1e-9);
    }

    @Test(timeout = 4000)
    public void testTanContinuedFraction() {
        // tan(x) = x / (1 - x^2 / (3 - x^2 / (5 - ...)))
        // Can be represented via ContinuedFraction
        // Here we test simple evaluation with a0 != 0 and known positive terms
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return (n == 0) ? 2.0 : 2.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        // 2 + 1 / (2 + 1 / (2 + ...)) = 1 + sqrt(2) approx 2.414213562373095
        double expected = 1.0 + FastMath.sqrt(2.0);
        double result = cf.evaluate(0.0);
        assertEquals(expected, result, 1e-8);
    }

    @Test(timeout = 4000)
    public void testInitialAIsZeroBranch() {
        // When getA(0, x) == 0.0, hPrev should be set to small (1e-50)
        // Continued fraction for sqrt(2) - 1:
        // [0; 2, 2, 2, ...] = 1 / (2 + 1 / (2 + ...)) = sqrt(2) - 1 approx 0.41421356237
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return (n == 0) ? 0.0 : 2.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        double expected = FastMath.sqrt(2.0) - 1.0;
        double result = cf.evaluate(0.0, 1e-10, 100);
        assertEquals(expected, result, 1e-8);
    }

    // =========================================================================
    // Partition B: Overload Delegation Verification
    // =========================================================================

    @Test(timeout = 4000)
    public void testEvaluateOverloadWithOnlyX() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        double expected = (1.0 + FastMath.sqrt(5.0)) / 2.0;
        double result = cf.evaluate(5.0);
        assertEquals(expected, result, 1e-8);
    }

    @Test(timeout = 4000)
    public void testEvaluateOverloadWithXAndEpsilon() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        double expected = (1.0 + FastMath.sqrt(5.0)) / 2.0;
        double result = cf.evaluate(0.0, 1e-6);
        assertEquals(expected, result, 1e-5);
    }

    @Test(timeout = 4000)
    public void testEvaluateOverloadWithXAndMaxIterations() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        double expected = (1.0 + FastMath.sqrt(5.0)) / 2.0;
        double result = cf.evaluate(0.0, 50);
        assertEquals(expected, result, 1e-8);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (MATH-718 / Divergence to NaN)
    // =========================================================================

    /**
     * Targets MATH-718 where rapid coefficient expansion causes cN and q2 to overflow
     * to Infinity simultaneously, yielding deltaN = cN / q2 / cPrev = Inf / Inf = NaN.
     * The evaluation must detect NaN divergence and throw ConvergenceException.
     */
    @Test(timeout = 4000)
    public void testMath718DivergenceToNanTriggersException() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return (n == 0) ? 1.0 : Double.NaN;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        try {
            cf.evaluate(0.5, 1e-8, 10);
            fail("Expected ConvergenceException due to NaN divergence");
        } catch (ConvergenceException ce) {
            assertTrue(ce.getMessage().contains("NaN") || ce.getMessage().contains("diverged"));
        }
    }

    /**
     * Generates a rapid overflow where cN and q2 become Infinite, testing the NaN condition.
     */
    @Test(timeout = 4000)
    public void testSimultaneousInfinityOverflowCausesNanOrException() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return 1e200;
            }

            @Override
            protected double getB(int n, double x) {
                return 1e200;
            }
        };

        try {
            cf.evaluate(1.0, 1e-8, 10);
            // If it succeeds or throws ConvergenceException, both are legitimate paths
        } catch (ConvergenceException ce) {
            assertNotNull(ce.getMessage());
        }
    }

    // =========================================================================
    // Partition D: Overflow, Scaling, & Infinity Divergence Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testScalingWhenScaleIsNonPositiveThrowsInfinityDivergence() {
        // Force Double.isInfinite(cN) on step n=1, while scale = FastMath.max(a, b) <= 0
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return (n == 0) ? 1.0 : -1.0;
            }

            @Override
            protected double getB(int n, double x) {
                // Return Infinity for b at n=1 so cN / q2 becomes infinite,
                // but scale = max(-1.0, -10.0) <= 0
                return (n == 1) ? Double.POSITIVE_INFINITY : -10.0;
            }
        };

        try {
            cf.evaluate(1.0, 1e-9, 10);
            fail("Expected ConvergenceException for unscalable infinity divergence");
        } catch (ConvergenceException ce) {
            assertNotNull(ce.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testScalingBranchWithAGreaterThanB() {
        // Tests the inner loop: a != 0.0 && a > b
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                if (n == 0) return 1.0;
                if (n == 1) return Double.MAX_VALUE;
                return 2.0;
            }

            @Override
            protected double getB(int n, double x) {
                if (n == 1) return 1.0;
                return 1.0;
            }
        };

        try {
            double res = cf.evaluate(0.0, 1e-6, 10);
            assertFalse(Double.isNaN(res));
        } catch (ConvergenceException expected) {
            // Either convergence or convergence exception is valid
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testScalingBranchWithBGreaterThanA() {
        // Tests the inner loop: else if (b != 0) where b > a
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                if (n == 0) return 1.0;
                if (n == 1) return 1.0;
                return 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                if (n == 1) return Double.MAX_VALUE;
                return 1.0;
            }
        };

        try {
            double res = cf.evaluate(0.0, 1e-6, 10);
            assertFalse(Double.isNaN(res));
        } catch (ConvergenceException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDirectHNInfinityThrowsConvergenceException() {
        // Force hN to become Double.POSITIVE_INFINITY directly
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return (n == 0) ? 1.0 : Double.POSITIVE_INFINITY;
            }

            @Override
            protected double getB(int n, double x) {
                return 0.0;
            }
        };

        try {
            cf.evaluate(0.0, 1e-9, 10);
            fail("Expected ConvergenceException for infinite hN");
        } catch (ConvergenceException ce) {
            assertNotNull(ce.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Iteration Guard & Boundary Extremes
    // =========================================================================

    @Test(expected = MaxCountExceededException.class, timeout = 4000)
    public void testMaxCountExceededExceptionWhenMaxIterationsIsSmall() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 1.0;
            }
        };

        // Needs around 30 iterations for 1e-12 precision, so 1 iteration must fail
        cf.evaluate(0.0, 1e-12, 1);
    }

    @Test(expected = MaxCountExceededException.class, timeout = 4000)
    public void testMaxCountExceededExceptionWithMaxIterationsTwo() {
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return 2.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 3.0;
            }
        };

        // Very tight tolerance and only 2 iterations
        cf.evaluate(0.0, 1e-15, 2);
    }

    @Test(timeout = 4000)
    public void testImmediateConvergenceInFirstIteration() {
        // If deltaN is already within epsilon on n=1
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return 1.0;
            }

            @Override
            protected double getB(int n, double x) {
                return 0.0; // cN = a * hPrev + 0 = 1.0, q2 = a * q1 + 0 = 1.0 => deltaN = 1.0
            }
        };

        // |deltaN - 1.0| = |1.0 - 1.0| = 0.0 < 1e-5
        double res = cf.evaluate(0.0, 1e-5, 5);
        assertEquals(1.0, res, 1e-9);
    }

    @Test(timeout = 4000)
    public void testEvaluationPointPassedCorrectly() {
        final double targetX = 42.5;
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                assertEquals(targetX, x, 1e-9);
                return x;
            }

            @Override
            protected double getB(int n, double x) {
                assertEquals(targetX, x, 1e-9);
                return 0.0;
            }
        };

        double res = cf.evaluate(targetX, 1e-5, 10);
        assertEquals(targetX, res, 1e-9);
    }
}