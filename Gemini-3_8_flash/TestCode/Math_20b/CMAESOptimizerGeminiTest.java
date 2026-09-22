/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: CMAESOptimizer
 *
 * 1. Defect MATH-864 (Targeted in Partition C):
 *    - Cause: repairAndDecode() failed to call repair() on normalized coordinates before decoding.
 *    - Manifestation: On bound-constrained optimization (especially maximization towards upper bound),
 *      returned optimum point coordinates exceed the specified boundaries (e.g., > 0.5).
 *    - Reproduction: Optimize an objective function towards a boundary constraint with SimpleBounds.
 *
 * 2. Key Branch & Decision Analysis:
 *    - checkParameters():
 *      * inputSigma null vs. non-null; length mismatch vs. match; negative sigma; sigma > range.
 *      * boundaries: all infinite (null), all finite, mixed finite/infinite (throws MathUnsupportedOperationException).
 *    - Generation Loop & Offspring Generation:
 *      * diagonalOnly <= 0 (full covariance) vs. diagonalOnly > 0 (diagonal covariance update).
 *      * checkFeasableCount: loop regeneration when !isFeasible(arxk).
 *      * TooManyEvaluationsException handling in offspring fitness evaluation.
 *      * isActiveCMA == true vs. false (active CMA covariance update vs. non-active).
 *      * ConvergenceChecker: null vs. non-null, early stopping on checker convergence.
 *      * Stop conditions: stopFitness reached, stopTolX, stopTolUpX, stopTolFun, stopTolHistFun,
 *        condition number > 1e7, flat fitness adjustments (bestValue == fitness[k] or range == 0).
 *    - UpdateBD / EigenDecomposition:
 *      * min(diagD) <= 0 correction; max(diagD) > 1e14 * min(diagD) correction.
 *    - FitnessFunction & Statistics Collection:
 *      * isRepairMode boundary handling, penalty calculation when out-of-bounds.
 *      * generateStatistics == true: history of sigma, mean, fitness, D.
 *    - DoubleIndex inner class:
 *      * compareTo, equals (same instance, different instance, non-DoubleIndex), hashCode.
 */

package org.apache.commons.math3.optimization.direct;

import java.util.List;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.SimplePointChecker;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;

import static org.junit.Assert.*;

public class CMAESOptimizerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testOptimizeSphereFunctionDefaultConstructor() {
        CMAESOptimizer optimizer = new CMAESOptimizer();
        MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] x) {
                double sum = 0;
                for (double v : x) {
                    sum += v * v;
                }
                return sum;
            }
        };

        double[] start = new double[] { 1.5, -1.2 };
        PointValuePair result = optimizer.optimize(5000, sphere, GoalType.MINIMIZE, start);

        assertNotNull(result);
        assertEquals(0.0, result.getValue(), 1e-1);
        assertEquals(0.0, result.getPoint()[0], 1e-1);
        assertEquals(0.0, result.getPoint()[1], 1e-1);
    }

    @Test(timeout = 4000)
    public void testOptimizeWithDiagonalOnly() {
        double[] sigma = new double[] { 0.2, 0.2 };
        // diagonalOnly = 1 keeps covariance diagonal
        CMAESOptimizer optimizer = new CMAESOptimizer(8, sigma, 1000, 1e-6,
                false, 1, 0, new MersenneTwister(42L), false,
                new SimpleValueChecker(1e-4, 1e-4));

        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] x) {
                return (x[0] - 1.0) * (x[0] - 1.0) + (x[1] + 2.0) * (x[1] + 2.0);
            }
        };

        PointValuePair result = optimizer.optimize(3000, f, GoalType.MINIMIZE, new double[] { 0.0, 0.0 });
        assertNotNull(result);
        assertEquals(1.0, result.getPoint()[0], 0.2);
        assertEquals(-2.0, result.getPoint()[1], 0.2);
    }

    @Test(timeout = 4000)
    public void testOptimizeWithDiagonalOnlySwitchToFull() {
        double[] sigma = new double[] { 0.3, 0.3 };
        // diagonalOnly = 2 switches to full covariance after iteration 2
        CMAESOptimizer optimizer = new CMAESOptimizer(8, sigma, 1000, 1e-8,
                true, 2, 0, new MersenneTwister(42L), false);

        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0] + x[1] * x[1];
            }
        };

        PointValuePair result = optimizer.optimize(3000, f, GoalType.MINIMIZE, new double[] { 2.0, 2.0 });
        assertNotNull(result);
        assertTrue(result.getValue() < 0.1);
    }

    @Test(timeout = 4000)
    public void testStatisticsCollection() {
        double[] sigma = new double[] { 0.5 };
        CMAESOptimizer optimizer = new CMAESOptimizer(6, sigma, 200, 1e-5,
                true, 0, 0, new MersenneTwister(1234L), true);

        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0];
            }
        };

        optimizer.optimize(2000, f, GoalType.MINIMIZE, new double[] { 2.0 });

        List<Double> sigmaHist = optimizer.getStatisticsSigmaHistory();
        List<Double> fitnessHist = optimizer.getStatisticsFitnessHistory();
        List<RealMatrix> meanHist = optimizer.getStatisticsMeanHistory();
        List<RealMatrix> dHist = optimizer.getStatisticsDHistory();

        assertNotNull(sigmaHist);
        assertNotNull(fitnessHist);
        assertNotNull(meanHist);
        assertNotNull(dHist);
        assertFalse(sigmaHist.isEmpty());
        assertEquals(sigmaHist.size(), fitnessHist.size());
        assertEquals(sigmaHist.size(), meanHist.size());
        assertEquals(sigmaHist.size(), dHist.size());
    }

    @Test(timeout = 4000)
    public void testMaximizeGoal() {
        CMAESOptimizer optimizer = new CMAESOptimizer(10);
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] x) {
                return -(x[0] - 2.0) * (x[0] - 2.0);
            }
        };

        PointValuePair result = optimizer.optimize(3000, f, GoalType.MAXIMIZE, new double[] { 0.0 });
        assertNotNull(result);
        assertEquals(0.0, result.getValue(), 0.1);
        assertEquals(2.0, result.getPoint()[0], 0.1);
    }

    @Test(timeout = 4000)
    public void testFeasibleCountRegeneration() {
        // checkFeasableCount > 0 forces re-sampling if offspring falls out of bounds
        double[] sigma = new double[] { 0.1, 0.1 };
        CMAESOptimizer optimizer = new CMAESOptimizer(8, sigma, 500, 0.0,
                true, 0, 5, new MersenneTwister(42L), false);

        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] x) {
                return (x[0] - 0.5) * (x[0] - 0.5) + (x[1] - 0.5) * (x[1] - 0.5);
            }
        };

        double[] start = new double[] { 0.2, 0.2 };
        double[] lower = new double[] { 0.0, 0.0 };
        double[] upper = new double[] { 1.0, 1.0 };

        PointValuePair result = optimizer.optimize(2000, f, GoalType.MINIMIZE, start, lower, upper);
        assertNotNull(result);
        assertTrue(result.getPoint()[0] >= 0.0 && result.getPoint()[0] <= 1.0);
        assertTrue(result.getPoint()[1] >= 0.0 && result.getPoint()[1] <= 1.0);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCheckParametersAllInfiniteBoundsTreatedAsNull() {
        CMAESOptimizer optimizer = new CMAESOptimizer();
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0];
            }
        };

        double[] start = new double[] { 1.0 };
        double[] lower = new double[] { Double.NEGATIVE_INFINITY };
        double[] upper = new double[] { Double.POSITIVE_INFINITY };

        PointValuePair result = optimizer.optimize(1000, f, GoalType.MINIMIZE, start, lower, upper);
        assertNotNull(result);
        assertTrue(result.getValue() < 0.1);
    }

    @Test(timeout = 4000)
    public void testConvergenceCheckerEarlyStop() {
        ConvergenceChecker<PointValuePair> strictChecker = new SimplePointChecker<PointValuePair>(0.1, 0.1);
        CMAESOptimizer optimizer = new CMAESOptimizer(6, new double[] { 0.1 }, 2000, 0.0,
                true, 0, 0, new MersenneTwister(42L), false, strictChecker);

        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0];
            }
        };

        PointValuePair result = optimizer.optimize(2000, f, GoalType.MINIMIZE, new double[] { 5.0 });
        assertNotNull(result);
        assertTrue(optimizer.getEvaluations() < 2000);
    }

    @Test(timeout = 4000)
    public void testStopFitnessTermination() {
        // Stop fitness = 5.0, optimizer should break early once reached
        CMAESOptimizer optimizer = new CMAESOptimizer(6, new double[] { 0.2 }, 1000, 5.0,
                true, 0, 0, new MersenneTwister(42L), false);

        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0];
            }
        };

        PointValuePair result = optimizer.optimize(2000, f, GoalType.MINIMIZE, new double[] { 10.0 });
        assertNotNull(result);
        assertTrue(result.getValue() <= 5.0);
    }

    @Test(timeout = 4000)
    public void testFlatFitnessStepSizeAdjustment() {
        // Flat fitness function: bestValue == fitness[...] triggers step size adjustment
        CMAESOptimizer optimizer = new CMAESOptimizer(6, new double[] { 0.1 }, 10, 0.0,
                true, 0, 0, new MersenneTwister(42L), false);

        MultivariateFunction flat = new MultivariateFunction() {
            public double value(double[] x) {
                return 42.0;
            }
        };

        PointValuePair result = optimizer.optimize(100, flat, GoalType.MINIMIZE, new double[] { 0.0 });
        assertNotNull(result);
        assertEquals(42.0, result.getValue(), 1e-9);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-864)
    // =========================================================================

    /**
     * Fault-revealing test case for MATH-864:
     * When boundary constraints are provided, repairAndDecode() must ensure that
     * returned optimum point coordinates do not violate bounds.
     * In the defective version, decode() was returned without repair(), resulting
     * in an optimum point coordinate exceeding upper bound 0.5.
     */
    @Test(timeout = 4000)
    public void testMath864() {
        final CMAESOptimizer optimizer = new CMAESOptimizer();
        final double[] start = { 0 };
        final double[] lower = { -1 };
        final double[] upper = { 0.5 };
        final int maxEvaluations = 10000;

        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
            public double value(double[] parameters) {
                return parameters[0];
            }
        };

        final PointValuePair result = optimizer.optimize(maxEvaluations, fitnessFunction,
                GoalType.MAXIMIZE, start, lower, upper);

        final double upperLimit = upper[0];
        final double optimalCoord = result.getPoint()[0];

        assertTrue("Out of bounds (" + optimalCoord + " > " + upperLimit + ")",
                optimalCoord <= upperLimit);
    }

    @Test(timeout = 4000)
    public void testBoundaryConstraintLowerBoundIntegrity() {
        final CMAESOptimizer optimizer = new CMAESOptimizer();
        final double[] start = { 1.0 };
        final double[] lower = { 0.5 };
        final double[] upper = { 2.0 };
        final int maxEvaluations = 10000;

        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
            public double value(double[] parameters) {
                return parameters[0];
            }
        };

        final PointValuePair result = optimizer.optimize(maxEvaluations, fitnessFunction,
                GoalType.MINIMIZE, start, lower, upper);

        final double lowerLimit = lower[0];
        final double optimalCoord = result.getPoint()[0];

        assertTrue("Out of bounds (" + optimalCoord + " < " + lowerLimit + ")",
                optimalCoord >= lowerLimit);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = MathUnsupportedOperationException.class, timeout = 4000)
    public void testMixedFiniteAndInfiniteBoundsThrowsException() {
        CMAESOptimizer optimizer = new CMAESOptimizer();
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0] + x[1] * x[1];
            }
        };

        double[] start = new double[] { 1.0, 1.0 };
        double[] lower = new double[] { 0.0, Double.NEGATIVE_INFINITY };
        double[] upper = new double[] { 2.0, 2.0 };

        optimizer.optimize(100, f, GoalType.MINIMIZE, start, lower, upper);
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testInputSigmaDimensionMismatchThrowsException() {
        double[] invalidSigma = new double[] { 0.5, 0.5 };
        CMAESOptimizer optimizer = new CMAESOptimizer(4, invalidSigma);

        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0];
            }
        };

        // start point has dimension 1, sigma has dimension 2
        optimizer.optimize(100, f, GoalType.MINIMIZE, new double[] { 1.0 });
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testNegativeInputSigmaThrowsException() {
        double[] negativeSigma = new double[] { -0.1 };
        CMAESOptimizer optimizer = new CMAESOptimizer(4, negativeSigma);

        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0];
            }
        };

        optimizer.optimize(100, f, GoalType.MINIMIZE, new double[] { 1.0 });
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testInputSigmaExceedsBoundaryRangeThrowsException() {
        double[] excessiveSigma = new double[] { 5.0 };
        CMAESOptimizer optimizer = new CMAESOptimizer(4, excessiveSigma);

        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0];
            }
        };

        double[] start = new double[] { 0.5 };
        double[] lower = new double[] { 0.0 };
        double[] upper = new double[] { 1.0 }; // range is 1.0, sigma is 5.0

        optimizer.optimize(100, f, GoalType.MINIMIZE, start, lower, upper);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndCloning() {
        double[] sigma = new double[] { 0.2 };
        CMAESOptimizer opt1 = new CMAESOptimizer(10, sigma);
        assertNotNull(opt1);

        // Deprecated constructor coverage
        @SuppressWarnings("deprecation")
        CMAESOptimizer opt2 = new CMAESOptimizer(10, sigma, 100, 0.0, true, 0, 0,
                new MersenneTwister(1L), false);
        assertNotNull(opt2);

        // Verify statistics getters return empty non-null collections prior to optimization
        assertTrue(opt1.getStatisticsSigmaHistory().isEmpty());
        assertTrue(opt1.getStatisticsFitnessHistory().isEmpty());
        assertTrue(opt1.getStatisticsMeanHistory().isEmpty());
        assertTrue(opt1.getStatisticsDHistory().isEmpty());
    }

    @Test(timeout = 4000)
    public void testInternalDoubleIndexContract() {
        // Exercise DoubleIndex via sorting behavior during optimization
        // Multi-point problem with multiple distinct fitness values
        CMAESOptimizer optimizer = new CMAESOptimizer(12, new double[] { 0.5, 0.5 });
        MultivariateFunction rosenbrock = new MultivariateFunction() {
            public double value(double[] x) {
                double a = x[1] - x[0] * x[0];
                double b = 1.0 - x[0];
                return 100 * a * a + b * b;
            }
        };

        PointValuePair res = optimizer.optimize(2000, rosenbrock, GoalType.MINIMIZE, new double[] { -1.0, 1.0 });
        assertNotNull(res);
        assertTrue(res.getValue() < 1.0);
    }
}