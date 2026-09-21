package org.apache.commons.math3.optimization.direct;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * White-box test suite for CMAESOptimizer.
 * Targets line/branch coverage and the known defect where
 * boundary range overflow should throw NumberIsTooLargeException
 * but instead throws MathIllegalStateException.
 *
 * [Branch & Defect Analysis Matrix]
 * - checkParameters: finite/infinite bounds combinations, inputSigma validation
 * - initializeCMA: lambda auto-computation, sigma initialization, termination criteria
 * - doOptimize: generation loop, fitness evaluation, sorting, update paths/covariance
 * - FitnessFunction: encode/decode/repair/penalty, isFeasible
 * - updateEvolutionPaths: hsig flag computation
 * - updateCovariance: active vs non-active CMA, negccov clamping
 * - updateBD: eigen decomposition, symmetry enforcement, zero eigenvalue handling
 * - Boundary overflow: encode diff = upper - lower may overflow to Infinity
 */
public class CMAESOptimizerDeepseekTest {

    // -------------------- Partition A: Core Functional Logic --------------------
    @Test(timeout = 4000)
    public void testSimpleSphereMinimization() {
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double v : point) sum += v * v;
                return sum;
            }
        };
        double[] start = {1.0, 1.0};
        double[] lower = {-10, -10};
        double[] upper = {10, 10};
        CMAESOptimizer optimizer = new CMAESOptimizer();
        PointValuePair result = optimizer.optimize(1000, sphere, GoalType.MINIMIZE, start, lower, upper);
        assertNotNull(result);
        double[] point = result.getPoint();
        double value = result.getValue();
        // Expect near zero
        assertTrue("Point should be near zero", Math.abs(point[0]) < 1e-2);
        assertTrue("Point should be near zero", Math.abs(point[1]) < 1e-2);
        assertTrue("Value should be near zero", value < 1e-4);
    }

    @Test(timeout = 4000)
    public void testMaximization() {
        MultivariateFunction negSphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double v : point) sum += v * v;
                return -sum;
            }
        };
        double[] start = {0.5, -0.5};
        double[] lower = {-5, -5};
        double[] upper = {5, 5};
        CMAESOptimizer optimizer = new CMAESOptimizer(10, new double[]{0.2, 0.2});
        PointValuePair result = optimizer.optimize(500, negSphere, GoalType.MAXIMIZE, start, lower, upper);
        assertNotNull(result);
        // Maximizing -sum => should go to zero
        assertTrue("Value should be near zero", Math.abs(result.getValue()) < 1e-3);
    }

    @Test(timeout = 4000)
    public void testNoBounds() {
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double v : point) sum += v * v;
                return sum;
            }
        };
        double[] start = {2.0, -2.0};
        CMAESOptimizer optimizer = new CMAESOptimizer();
        PointValuePair result = optimizer.optimize(1000, sphere, GoalType.MINIMIZE, start);
        assertNotNull(result);
        assertTrue("Value should be near zero", result.getValue() < 1e-3);
    }

    // -------------------- Partition B: Boundary Value Analysis --------------------
    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testInputSigmaDimensionMismatch() {
        double[] start = {1.0, 2.0};
        double[] sigma = {0.3}; // wrong length
        new CMAESOptimizer(10, sigma).optimize(100, new MultivariateFunction() {
            @Override
            public double value(double[] point) { return 0; }
        }, GoalType.MINIMIZE, start);
    }

    @Test(timeout = 4000, expected = NotPositiveException.class)
    public void testInputSigmaNegative() {
        double[] start = {1.0};
        double[] sigma = {-0.1};
        new CMAESOptimizer(10, sigma).optimize(100, new MultivariateFunction() {
            @Override
            public double value(double[] point) { return 0; }
        }, GoalType.MINIMIZE, start);
    }

    @Test(timeout = 4000, expected = OutOfRangeException.class)
    public void testInputSigmaOutOfRange() {
        double[] start = {0.5};
        double[] lower = {0};
        double[] upper = {1};
        double[] sigma = {2.0}; // > upper-lower = 1
        new CMAESOptimizer(10, sigma).optimize(100, new MultivariateFunction() {
            @Override
            public double value(double[] point) { return 0; }
        }, GoalType.MINIMIZE, start, lower, upper);
    }

    @Test(timeout = 4000, expected = MathUnsupportedOperationException.class)
    public void testMixedBoundsInfiniteFinite() {
        double[] start = {0.0};
        double[] lower = {Double.NEGATIVE_INFINITY};
        double[] upper = {1.0}; // one infinite, one finite
        new CMAESOptimizer().optimize(100, new MultivariateFunction() {
            @Override
            public double value(double[] point) { return 0; }
        }, GoalType.MINIMIZE, start, lower, upper);
    }

    // -------------------- Partition C: Defect-Targeted Branch Zone --------------------
    @Test(timeout = 4000, expected = NumberIsTooLargeException.class)
    public void testBoundaryRangeTooLarge() {
        // This test targets the known defect: overflow in encode diff
        double[] start = {0.0};
        double[] lower = {-Double.MAX_VALUE / 2};
        double[] upper = {Double.MAX_VALUE / 2}; // diff = Double.MAX_VALUE, may overflow to Infinity
        // Actually to guarantee overflow, use values that cause diff = Infinity
        double[] lowerOverflow = {-1e308};
        double[] upperOverflow = {1e308};
        new CMAESOptimizer().optimize(100, new MultivariateFunction() {
            @Override
            public double value(double[] point) { return 0; }
        }, GoalType.MINIMIZE, start, lowerOverflow, upperOverflow);
    }

    // -------------------- Partition D: Exception & Defensive Guard Paths --------------------
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullStartPoint() {
        new CMAESOptimizer().optimize(100, new MultivariateFunction() {
            @Override
            public double value(double[] point) { return 0; }
        }, GoalType.MINIMIZE, null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullFunction() {
        double[] start = {0.0};
        new CMAESOptimizer().optimize(100, null, GoalType.MINIMIZE, start);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullGoalType() {
        double[] start = {0.0};
        new CMAESOptimizer().optimize(100, new MultivariateFunction() {
            @Override
            public double value(double[] point) { return 0; }
        }, null, start);
    }

    @Test(timeout = 4000)
    public void testTooManyEvaluations() {
        // Use a function that is expensive or set low max evaluations
        MultivariateFunction slow = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                // Simulate many evaluations by just returning a value
                return point[0] * point[0];
            }
        };
        double[] start = {100.0};
        double[] lower = {-1000};
        double[] upper = {1000};
        CMAESOptimizer optimizer = new CMAESOptimizer(10, null, 10, 0, true, 0, 0, new MersenneTwister(), false);
        // With very few iterations, it should still return a result (not throw)
        PointValuePair result = optimizer.optimize(10, slow, GoalType.MINIMIZE, start, lower, upper);
        assertNotNull(result);
    }

    // -------------------- Partition E: Object Lifecycle & Contract Integrity --------------------
    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        CMAESOptimizer opt = new CMAESOptimizer();
        assertNotNull(opt);
    }

    @Test(timeout = 4000)
    public void testLambdaConstructor() {
        CMAESOptimizer opt = new CMAESOptimizer(20);
        assertNotNull(opt);
    }

    @Test(timeout = 4000)
    public void testStatisticsHistory() {
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double v : point) sum += v * v;
                return sum;
            }
        };
        double[] start = {1.0, 1.0};
        double[] lower = {-5, -5};
        double[] upper = {5, 5};
        // Enable statistics generation
        CMAESOptimizer optimizer = new CMAESOptimizer(10, null, 100, 0, true, 0, 0, new MersenneTwister(), true);
        optimizer.optimize(100, sphere, GoalType.MINIMIZE, start, lower, upper);
        assertNotNull(optimizer.getStatisticsSigmaHistory());
        assertNotNull(optimizer.getStatisticsMeanHistory());
        assertNotNull(optimizer.getStatisticsFitnessHistory());
        assertNotNull(optimizer.getStatisticsDHistory());
        assertTrue(optimizer.getStatisticsSigmaHistory().size() > 0);
    }

    @Test(timeout = 4000)
    public void testDiagonalOnlyMode() {
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double v : point) sum += v * v;
                return sum;
            }
        };
        double[] start = {1.0, 1.0, 1.0};
        double[] lower = {-10, -10, -10};
        double[] upper = {10, 10, 10};
        // diagonalOnly = 1 (always diagonal)
        CMAESOptimizer optimizer = new CMAESOptimizer(10, null, 100, 0, true, 1, 0, new MersenneTwister(), false);
        PointValuePair result = optimizer.optimize(100, sphere, GoalType.MINIMIZE, start, lower, upper);
        assertNotNull(result);
        assertTrue("Value should be small", result.getValue() < 1.0);
    }

    @Test(timeout = 4000)
    public void testActiveCMAOff() {
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double v : point) sum += v * v;
                return sum;
            }
        };
        double[] start = {0.5, -0.5};
        double[] lower = {-2, -2};
        double[] upper = {2, 2};
        CMAESOptimizer optimizer = new CMAESOptimizer(10, null, 200, 0, false, 0, 0, new MersenneTwister(), false);
        PointValuePair result = optimizer.optimize(200, sphere, GoalType.MINIMIZE, start, lower, upper);
        assertNotNull(result);
        assertTrue("Value should be small", result.getValue() < 1.0);
    }

    @Test(timeout = 4000)
    public void testCheckFeasableCount() {
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double v : point) sum += v * v;
                return sum;
            }
        };
        double[] start = {0.0};
        double[] lower = {-1};
        double[] upper = {1};
        // checkFeasableCount = 5
        CMAESOptimizer optimizer = new CMAESOptimizer(10, null, 100, 0, true, 0, 5, new MersenneTwister(), false);
        PointValuePair result = optimizer.optimize(100, sphere, GoalType.MINIMIZE, start, lower, upper);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testStopFitness() {
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double v : point) sum += v * v;
                return sum;
            }
        };
        double[] start = {10.0};
        double[] lower = {-100};
        double[] upper = {100};
        // stopFitness = 1e-6 (minimize, so stop when value < 1e-6)
        CMAESOptimizer optimizer = new CMAESOptimizer(10, null, 1000, 1e-6, true, 0, 0, new MersenneTwister(), false);
        PointValuePair result = optimizer.optimize(1000, sphere, GoalType.MINIMIZE, start, lower, upper);
        assertTrue("Value should be <= 1e-6", result.getValue() <= 1e-6);
    }

    @Test(timeout = 4000)
    public void testFitnessFunctionEncodeDecode() {
        // Indirectly test FitnessFunction via optimization with bounds
        MultivariateFunction func = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0];
            }
        };
        double[] start = {0.5};
        double[] lower = {0};
        double[] upper = {1};
        CMAESOptimizer optimizer = new CMAESOptimizer(10, null, 100, 0, true, 0, 0, new MersenneTwister(), false);
        PointValuePair result = optimizer.optimize(100, func, GoalType.MINIMIZE, start, lower, upper);
        // Should converge to lower bound (0)
        assertTrue("Point should be near 0", Math.abs(result.getPoint()[0]) < 0.1);
    }

    @Test(timeout = 4000)
    public void testRepairMode() {
        // Test that repair works when point is out of bounds
        MultivariateFunction func = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0];
            }
        };
        double[] start = {0.5};
        double[] lower = {0};
        double[] upper = {1};
        CMAESOptimizer optimizer = new CMAESOptimizer(10, null, 100, 0, true, 0, 0, new MersenneTwister(), false);
        PointValuePair result = optimizer.optimize(100, func, GoalType.MINIMIZE, start, lower, upper);
        // The result should be within bounds
        assertTrue("Point should be >= lower", result.getPoint()[0] >= lower[0] - 1e-10);
        assertTrue("Point should be <= upper", result.getPoint()[0] <= upper[0] + 1e-10);
    }

    @Test(timeout = 4000)
    public void testSortedIndices() {
        // Indirectly test via optimization (sortedIndices is private)
        // We can call a public method that uses it: doOptimize
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double v : point) sum += v * v;
                return sum;
            }
        };
        double[] start = {1.0, 2.0};
        double[] lower = {-10, -10};
        double[] upper = {10, 10};
        CMAESOptimizer optimizer = new CMAESOptimizer(10, null, 100, 0, true, 0, 0, new MersenneTwister(), false);
        PointValuePair result = optimizer.optimize(100, sphere, GoalType.MINIMIZE, start, lower, upper);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testConvergenceChecker() {
        // Use a custom convergence checker that stops early
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double v : point) sum += v * v;
                return sum;
            }
        };
        double[] start = {1.0};
        double[] lower = {-5};
        double[] upper = {5};
        CMAESOptimizer optimizer = new CMAESOptimizer(10, null, 1000, 0, true, 0, 0, new MersenneTwister(), false,
                new org.apache.commons.math3.optimization.SimpleValueChecker(1e-8, 1e-8));
        PointValuePair result = optimizer.optimize(1000, sphere, GoalType.MINIMIZE, start, lower, upper);
        assertNotNull(result);
        assertTrue("Value should be small", result.getValue() < 1e-4);
    }
}