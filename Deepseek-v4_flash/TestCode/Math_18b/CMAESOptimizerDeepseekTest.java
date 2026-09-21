package org.apache.commons.math3.optimization.direct;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.random.MersenneTwister;

/**
 * Test suite for CMAESOptimizer.
 * 
 * Branch & Defect Analysis Matrix:
 * - Partition A: Core functional logic (no-bounds optimization, default parameters)
 * - Partition B: Boundary analysis (bounds, inputSigma extremes, overflow detection)
 * - Partition C: Defect-targeted (known boundary-dependent accuracy bug)
 * - Partition D: Exception paths (invalid inputSigma, dimension mismatch, overflow)
 * - Partition E: Statistics collection and convergence checker interaction
 */
public class CMAESOptimizerDeepseekTest {

    // ------------------------------------------------------------
    // Partition A: Core functional logic
    // ------------------------------------------------------------
    
    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        CMAESOptimizer opt = new CMAESOptimizer();
        assertNotNull(opt);
        // Statistics lists should be empty initially
        assertTrue(opt.getStatisticsSigmaHistory().isEmpty());
        assertTrue(opt.getStatisticsFitnessHistory().isEmpty());
        assertTrue(opt.getStatisticsMeanHistory().isEmpty());
        assertTrue(opt.getStatisticsDHistory().isEmpty());
    }

    @Test(timeout = 4000)
    public void testOptimizeSimpleNoBounds() {
        CMAESOptimizer opt = new CMAESOptimizer(6); // small lambda for speed
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                // Sphere function shifted to (1,1)
                double sum = 0;
                for (double x : point) {
                    double d = x - 1.0;
                    sum += d * d;
                }
                return sum;
            }
        };
        double[] start = {0.5, 0.5};
        double[] lower = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        double[] upper = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        PointValuePair result = opt.optimize(10000, f, GoalType.MINIMIZE, start, lower, upper);
        // Expect optimum near (1,1)
        double[] point = result.getPoint();
        assertEquals(1.0, point[0], 0.2);
        assertEquals(1.0, point[1], 0.2);
        // Fitness should be near zero
        assertTrue("Fitness too high: " + result.getValue(), result.getValue() < 1e-2);
    }

    @Test(timeout = 4000)
    public void testOptimizeWithBounds() {
        CMAESOptimizer opt = new CMAESOptimizer(6);
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                // Identify function: f(x) = x[0]^2 + (x[1]-0.5)^2, optimum at (0,0.5) within [0,1]
                return point[0]*point[0] + (point[1]-0.5)*(point[1]-0.5);
            }
        };
        double[] start = {0.3, 0.3};
        double[] lower = {0.0, 0.0};
        double[] upper = {1.0, 1.0};
        PointValuePair result = opt.optimize(10000, f, GoalType.MINIMIZE, start, lower, upper);
        double[] point = result.getPoint();
        // Optimum expected inside bounds
        assertEquals(0.0, point[0], 0.1);
        assertEquals(0.5, point[1], 0.1);
        assertTrue("Fitness too high: " + result.getValue(), result.getValue() < 0.01);
    }

    @Test(timeout = 4000)
    public void testActiveCMATrue() {
        // Use isActiveCMA = true (default) and explicit diagonalOnly = 0
        CMAESOptimizer opt = new CMAESOptimizer(
            6, null, 1000, 0, true, 0, 0, new MersenneTwister(), false, null);
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0]*point[0] + point[1]*point[1];
            }
        };
        double[] start = {2.0, 2.0};
        double[] lower = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        double[] upper = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        PointValuePair result = opt.optimize(10000, f, GoalType.MINIMIZE, start, lower, upper);
        double[] point = result.getPoint();
        assertEquals(0.0, point[0], 0.5);
        assertEquals(0.0, point[1], 0.5);
        assertTrue("Fitness too high: " + result.getValue(), result.getValue() < 1);
    }

    // ------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // ------------------------------------------------------------
    
    @Test(timeout = 4000)
    public void testOptimizeWithBoundsTight() {
        // Very tight bounds: [0, 1e-8] – should stay near lower bound
        CMAESOptimizer opt = new CMAESOptimizer(6);
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0]; // optimum at 0
            }
        };
        double[] start = {5e-9};
        double[] lower = {0.0};
        double[] upper = {1e-8};
        PointValuePair result = opt.optimize(10000, f, GoalType.MINIMIZE, start, lower, upper);
        double[] point = result.getPoint();
        assertTrue("Point should be inside bounds", point[0] >= 0.0 && point[0] <= 1e-8);
        assertTrue("Fitness should be tiny", result.getValue() < 1e-12);
    }

    // ------------------------------------------------------------
    // Partition C: Defect-targeted (known boundary-related accuracy bug)
    // ------------------------------------------------------------
    
    @Test(timeout = 4000)
    public void testFitAccuracyDependsOnBoundary() {
        // Reproduce the known defect: expected fitness ~11.1, actual ~8.0
        // The bug manifests when boundaries are not null and the optimizer
        // fails to converge to the correct optimum. We use a simple 1D
        // quadratic with optimum at 0.5, bounds [0,10].
        CMAESOptimizer opt = new CMAESOptimizer(
            10, null, 30000, 0, true, 0, 0, new MersenneTwister(42L), false, null);
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                double x = point[0];
                return (x - 0.5) * (x - 0.5); // optimum at 0.5
            }
        };
        double[] start = {1.0};
        double[] lower = {0.0};
        double[] upper = {10.0};
        PointValuePair result = opt.optimize(50000, f, GoalType.MINIMIZE, start, lower, upper);
        double bestFitness = result.getValue();
        // The expected correct fitness is near zero; the buggy version may produce ~8.0
        // We assert that the optimizer finds a significantly better point.
        assertTrue("Optimizer should find a point with fitness near 0, but got: " + bestFitness,
                   bestFitness < 0.1);
        // Additionally, the point should be close to 0.5
        double xOpt = result.getPoint()[0];
        assertEquals("Optimal x should be near 0.5", 0.5, xOpt, 0.2);
    }

    // ------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // ------------------------------------------------------------
    
    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testInputSigmaNegative() {
        double[] inputSigma = {-0.5};
        CMAESOptimizer opt = new CMAESOptimizer(6, inputSigma);
        // Trigger checkParameters by calling optimize
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) { return point[0]; }
        };
        double[] start = {0.5};
        double[] lower = {0.0};
        double[] upper = {1.0};
        opt.optimize(100, f, GoalType.MINIMIZE, start, lower, upper);
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testInputSigmaDimensionMismatch() {
        double[] inputSigma = {1.0, 2.0}; // two elements, but start point has one dimension
        CMAESOptimizer opt = new CMAESOptimizer(6, inputSigma);
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) { return point[0]; }
        };
        double[] start = {0.5};
        double[] lower = {0.0};
        double[] upper = {1.0};
        opt.optimize(100, f, GoalType.MINIMIZE, start, lower, upper);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testBoundOverflow() {
        // Bounds difference overflows to Infinity
        double[] lower = {-Double.MAX_VALUE};
        double[] upper = {Double.MAX_VALUE}; // difference = Infinity
        CMAESOptimizer opt = new CMAESOptimizer(6);
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) { return point[0]; }
        };
        double[] start = {0.0};
        opt.optimize(100, f, GoalType.MINIMIZE, start, lower, upper);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInputSigmaOutOfRange() {
        double[] inputSigma = {2.0}; // > bound range (1-0)
        CMAESOptimizer opt = new CMAESOptimizer(6, inputSigma);
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) { return point[0]; }
        };
        double[] start = {0.5};
        double[] lower = {0.0};
        double[] upper = {1.0};
        opt.optimize(100, f, GoalType.MINIMIZE, start, lower, upper);
    }

    // ------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // ------------------------------------------------------------
    
    @Test(timeout = 4000)
    public void testStatisticsCollection() {
        CMAESOptimizer opt = new CMAESOptimizer(
            6, null, 1000, 0, true, 0, 0, new MersenneTwister(), true, null);
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0]*point[0];
            }
        };
        double[] start = {3.0};
        double[] lower = {Double.NEGATIVE_INFINITY};
        double[] upper = {Double.POSITIVE_INFINITY};
        PointValuePair result = opt.optimize(10000, f, GoalType.MINIMIZE, start, lower, upper);
        // After optimization, statistics lists should have entries if generateStatistics was true
        assertFalse("Sigma history should not be empty", opt.getStatisticsSigmaHistory().isEmpty());
        assertFalse("Fitness history should not be empty", opt.getStatisticsFitnessHistory().isEmpty());
        assertFalse("Mean history should not be empty", opt.getStatisticsMeanHistory().isEmpty());
        assertFalse("D history should not be empty", opt.getStatisticsDHistory().isEmpty());
    }

    @Test(timeout = 4000)
    public void testConvergenceChecker() {
        // Custom convergence checker that stops after 1 iteration
        CMAESOptimizer opt = new CMAESOptimizer(
            6, null, 10, 0, false, 0, 0, new MersenneTwister(), false,
            new org.apache.commons.math3.optimization.ConvergenceChecker<PointValuePair>() {
                public boolean converged(int iteration, PointValuePair previous, PointValuePair current) {
                    return true; // always stop
                }
            }
        );
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0]*point[0];
            }
        };
        double[] start = {1.0};
        double[] lower = {Double.NEGATIVE_INFINITY};
        double[] upper = {Double.POSITIVE_INFINITY};
        PointValuePair result = opt.optimize(100, f, GoalType.MINIMIZE, start, lower, upper);
        assertNotNull("Optimizer should return a result even when convergence checker stops early", result);
    }

    @Test(timeout = 4000)
    public void testFitnessFunctionDecodeEncode() {
        // Indirectly test by providing start point in encoded space and checking returned point
        CMAESOptimizer opt = new CMAESOptimizer(6);
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0]*point[0];
            }
        };
        // Start at 0.5 in original space; boundaries [0,1] -> encoded 0.5
        double[] start = {0.5};
        double[] lower = {0.0};
        double[] upper = {1.0};
        PointValuePair result = opt.optimize(10000, f, GoalType.MINIMIZE, start, lower, upper);
        // Result point should be in original space, near 0
        double x = result.getPoint()[0];
        assertTrue("Decoded point should be within bounds", x >= 0.0 && x <= 1.0);
        assertEquals("Optimum should be near 0", 0.0, x, 0.2);
    }
}