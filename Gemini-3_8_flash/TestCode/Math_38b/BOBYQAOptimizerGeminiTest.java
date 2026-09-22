package org.apache.commons.math.optimization.direct;

import org.apache.commons.math.analysis.MultivariateFunction;
import org.apache.commons.math.exception.MathIllegalStateException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.apache.commons.math.exception.TooManyEvaluationsException;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Class under test: BOBYQAOptimizer
 * Key Methods Tested:
 *   - BOBYQAOptimizer(int), BOBYQAOptimizer(int, double, double)
 *   - doOptimize() -> setup(), bobyqa(), bobyqb(), prelim(), trsbox(), altmov(), update()
 *
 * Specific Decision Branches & Coverage Targets:
 *   1. Setup Validation:
 *      - Dimension < MINIMUM_PROBLEM_DIMENSION (2) -> NumberIsTooSmallException
 *      - Interpolation points < n + 2 -> OutOfRangeException
 *      - Interpolation points > (n + 1) * (n + 2) / 2 -> OutOfRangeException
 *      - minDiff < 2 * initialTrustRegionRadius -> initialTrustRegionRadius scaled to minDiff / 3.0
 *   2. Optimization Goals:
 *      - GoalType.MINIMIZE vs GoalType.MAXIMIZE
 *   3. Boundary Value Handling in bobyqa():
 *      - lowerDifference >= -initialTrustRegionRadius (both >= 0 and < 0 branches)
 *      - upperDifference <= initialTrustRegionRadius (both <= 0 and > 0 branches)
 *   4. Preliminary Model Construction (prelim()):
 *      - numEval <= 2 * n + 1 (standard cases)
 *      - Defect Zone (numEval > 2 * n + 1, triggered when npt > 2 * n + 1) ->
 *        hits unhandled PathIsExploredException in prelim() at off-diagonal derivative construction.
 *   5. Trust Region Steps & Alternating Iterations:
 *      - Unconstrained and boundary-constrained steps in trsbox()
 *      - Multi-dimensional Rosenbrock, Sphere, and Ellipsoid functions
 *      - Evaluation limits (TooManyEvaluationsException)
 * =========================================================================================
 */
public class BOBYQAOptimizerGeminiTest {

    // Helper functions
    private static class SphereFunction implements MultivariateFunction {
        public double value(double[] point) {
            double sum = 0;
            for (double v : point) {
                sum += v * v;
            }
            return sum;
        }
    }

    private static class InvertedSphereFunction implements MultivariateFunction {
        public double value(double[] point) {
            double sum = 0;
            for (double v : point) {
                sum += v * v;
            }
            return -sum;
        }
    }

    private static class RosenbrockFunction implements MultivariateFunction {
        public double value(double[] x) {
            double sum = 0;
            for (int i = 0; i < x.length - 1; i++) {
                double a = x[i + 1] - x[i] * x[i];
                double b = 1.0 - x[i];
                sum += 100.0 * a * a + b * b;
            }
            return sum;
        }
    }

    private static class LinearObjectiveFunction implements MultivariateFunction {
        public double value(double[] x) {
            return 2.0 * x[0] + 3.0 * x[1];
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSphereMinimization2D() {
        // n = 2, minimum allowed npt = 2 + 2 = 4
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(4);
        MultivariateFunction function = new SphereFunction();
        double[] start = new double[] { 2.0, -1.5 };
        double[] lower = new double[] { -5.0, -5.0 };
        double[] upper = new double[] { 5.0, 5.0 };

        RealPointValuePair result = optimizer.optimize(
                500, function, GoalType.MINIMIZE, start, lower, upper
        );

        assertNotNull(result);
        assertEquals(0.0, result.getPoint()[0], 1e-3);
        assertEquals(0.0, result.getPoint()[1], 1e-3);
        assertEquals(0.0, result.getValue(), 1e-4);
    }

    @Test(timeout = 4000)
    public void testSphereMinimization3D() {
        // n = 3, npt = 2 * n + 1 = 7
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(7, 2.0, 1e-6);
        MultivariateFunction function = new SphereFunction();
        double[] start = new double[] { 1.0, -1.0, 0.5 };
        double[] lower = new double[] { -10.0, -10.0, -10.0 };
        double[] upper = new double[] { 10.0, 10.0, 10.0 };

        RealPointValuePair result = optimizer.optimize(
                1000, function, GoalType.MINIMIZE, start, lower, upper
        );

        assertNotNull(result);
        for (int i = 0; i < 3; i++) {
            assertEquals(0.0, result.getPoint()[i], 1e-3);
        }
        assertEquals(0.0, result.getValue(), 1e-4);
    }

    @Test(timeout = 4000)
    public void testSphereMaximization() {
        // Test GoalType.MAXIMIZE branch
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        MultivariateFunction function = new InvertedSphereFunction();
        double[] start = new double[] { 1.5, -2.0 };
        double[] lower = new double[] { -10.0, -10.0 };
        double[] upper = new double[] { 10.0, 10.0 };

        RealPointValuePair result = optimizer.optimize(
                500, function, GoalType.MAXIMIZE, start, lower, upper
        );

        assertNotNull(result);
        assertEquals(0.0, result.getPoint()[0], 1e-2);
        assertEquals(0.0, result.getPoint()[1], 1e-2);
        assertEquals(0.0, result.getValue(), 1e-3);
    }

    @Test(timeout = 4000)
    public void testRosenbrock2D() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5, 1.0, 1e-7);
        MultivariateFunction function = new RosenbrockFunction();
        double[] start = new double[] { -0.5, 0.5 };
        double[] lower = new double[] { -2.0, -2.0 };
        double[] upper = new double[] { 2.0, 2.0 };

        RealPointValuePair result = optimizer.optimize(
                1000, function, GoalType.MINIMIZE, start, lower, upper
        );

        assertNotNull(result);
        assertEquals(1.0, result.getPoint()[0], 1e-2);
        assertEquals(1.0, result.getPoint()[1], 1e-2);
        assertEquals(0.0, result.getValue(), 1e-3);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSmallBoundDifferenceRadiusReduction() {
        // minDiff < 2 * initialTrustRegionRadius triggers radius reduction: initialTrustRegionRadius = minDiff / 3.0
        double initialRadius = 10.0;
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5, initialRadius, 1e-8);
        MultivariateFunction function = new SphereFunction();

        // Upper - Lower = 1.0, which is < 2 * 10.0
        double[] lower = new double[] { -0.5, -0.5 };
        double[] upper = new double[] { 0.5, 0.5 };
        double[] start = new double[] { 0.1, -0.1 };

        RealPointValuePair result = optimizer.optimize(
                500, function, GoalType.MINIMIZE, start, lower, upper
        );

        assertNotNull(result);
        assertEquals(0.0, result.getValue(), 1e-3);
    }

    @Test(timeout = 4000)
    public void testStartPointDirectlyOnLowerBound() {
        // Tests lowerDifference >= 0 branch in bobyqa()
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5, 1.0, 1e-6);
        MultivariateFunction function = new LinearObjectiveFunction();
        double[] lower = new double[] { 1.0, 2.0 };
        double[] upper = new double[] { 10.0, 10.0 };
        double[] start = new double[] { 1.0, 2.0 }; // Exactly on lower bounds

        RealPointValuePair result = optimizer.optimize(
                500, function, GoalType.MINIMIZE, start, lower, upper
        );

        assertNotNull(result);
        assertEquals(1.0, result.getPoint()[0], 1e-3);
        assertEquals(2.0, result.getPoint()[1], 1e-3);
        assertEquals(8.0, result.getValue(), 1e-3);
    }

    @Test(timeout = 4000)
    public void testStartPointDirectlyOnUpperBound() {
        // Tests upperDifference <= 0 branch in bobyqa()
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5, 1.0, 1e-6);
        MultivariateFunction function = new LinearObjectiveFunction();
        double[] lower = new double[] { -10.0, -10.0 };
        double[] upper = new double[] { 2.0, 3.0 };
        double[] start = new double[] { 2.0, 3.0 }; // Exactly on upper bounds

        RealPointValuePair result = optimizer.optimize(
                500, function, GoalType.MAXIMIZE, start, lower, upper
        );

        assertNotNull(result);
        assertEquals(2.0, result.getPoint()[0], 1e-3);
        assertEquals(3.0, result.getPoint()[1], 1e-3);
        assertEquals(13.0, result.getValue(), 1e-3);
    }

    @Test(timeout = 4000)
    public void testActiveBoundConstraintAtOptimum() {
        // Optimum lies strictly outside bounds so bounds must become active
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        MultivariateFunction function = new SphereFunction();
        double[] start = new double[] { 3.0, 3.0 };
        double[] lower = new double[] { 2.0, 2.0 };
        double[] upper = new double[] { 5.0, 5.0 };

        RealPointValuePair result = optimizer.optimize(
                500, function, GoalType.MINIMIZE, start, lower, upper
        );

        assertNotNull(result);
        assertEquals(2.0, result.getPoint()[0], 1e-3);
        assertEquals(2.0, result.getPoint()[1], 1e-3);
        assertEquals(8.0, result.getValue(), 1e-2);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Defect)
    // =========================================================================

    /**
     * Targets known defect: BOBYQAOptimizer contains an unremoved debugging guard
     * (PathIsExploredException) when numberOfInterpolationPoints exceeds 2 * n + 1.
     * For n = 2, max points allowed by setup is (2 + 1) * (2 + 2) / 2 = 6.
     * Choosing npt = 6 (which is > 2 * 2 + 1 = 5) triggers prelim's else branch
     * when computing the off-diagonal second derivative terms.
     */
    @Test(timeout = 4000)
    public void testConstrainedRosenWithMoreInterpolationPoints() {
        // Dimension n = 2.
        // Valid range for npt: [n + 2, (n + 1)*(n + 2)/2] = [4, 6].
        // 6 exceeds 2 * n + 1 = 5, exposing the defect if PathIsExploredException is present.
        int npt = 6;
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(npt);
        MultivariateFunction function = new RosenbrockFunction();

        double[] start = new double[] { -1.2, 1.0 };
        double[] lower = new double[] { -2.0, -1.0 };
        double[] upper = new double[] { 2.0, 3.0 };

        RealPointValuePair result = optimizer.optimize(
                1000, function, GoalType.MINIMIZE, start, lower, upper
        );

        assertNotNull(result);
        assertEquals(1.0, result.getPoint()[0], 1e-2);
        assertEquals(1.0, result.getPoint()[1], 1e-2);
        assertEquals(0.0, result.getValue(), 1e-3);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = NumberIsTooSmallException.class)
    public void testDimensionTooSmall() {
        // Dimension n = 1 is less than MINIMUM_PROBLEM_DIMENSION = 2
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(3);
        MultivariateFunction function = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        optimizer.optimize(
                100, function, GoalType.MINIMIZE,
                new double[] { 1.0 },
                new double[] { -5.0 },
                new double[] { 5.0 }
        );
    }

    @Test(timeout = 4000, expected = OutOfRangeException.class)
    public void testInterpolationPointsTooSmall() {
        // Dimension n = 2. Minimum npt is n + 2 = 4. Passing 3 must throw OutOfRangeException.
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(3);
        MultivariateFunction function = new SphereFunction();

        optimizer.optimize(
                100, function, GoalType.MINIMIZE,
                new double[] { 1.0, 1.0 },
                new double[] { -5.0, -5.0 },
                new double[] { 5.0, 5.0 }
        );
    }

    @Test(timeout = 4000, expected = OutOfRangeException.class)
    public void testInterpolationPointsTooLarge() {
        // Dimension n = 2. Maximum npt is (n + 1)*(n + 2)/2 = 6. Passing 7 must throw OutOfRangeException.
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(7);
        MultivariateFunction function = new SphereFunction();

        optimizer.optimize(
                100, function, GoalType.MINIMIZE,
                new double[] { 1.0, 1.0 },
                new double[] { -5.0, -5.0 },
                new double[] { 5.0, 5.0 }
        );
    }

    @Test(timeout = 4000, expected = TooManyEvaluationsException.class)
    public void testMaxEvaluationsExceeded() {
        // Restricting max evaluations to a very low number (e.g. 5) must trigger TooManyEvaluationsException
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(4);
        MultivariateFunction function = new SphereFunction();

        optimizer.optimize(
                3, function, GoalType.MINIMIZE,
                new double[] { 2.0, 2.0 },
                new double[] { -5.0, -5.0 },
                new double[] { 5.0, 5.0 }
        );
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstantsAndDefaults() {
        assertEquals(2, BOBYQAOptimizer.MINIMUM_PROBLEM_DIMENSION);
        assertEquals(10.0, BOBYQAOptimizer.DEFAULT_INITIAL_RADIUS, 1e-12);
        assertEquals(1E-8, BOBYQAOptimizer.DEFAULT_STOPPING_RADIUS, 1e-12);

        // Verify constructor with default radii vs explicit radii
        BOBYQAOptimizer optDefault = new BOBYQAOptimizer(4);
        assertNotNull(optDefault);

        BOBYQAOptimizer optExplicit = new BOBYQAOptimizer(4, 5.0, 1e-5);
        assertNotNull(optExplicit);
    }

    @Test(timeout = 4000)
    public void testHigherDimensionOptimization() {
        // Dimension n = 4, npt = 2 * n + 1 = 9
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(9);
        MultivariateFunction function = new SphereFunction();
        double[] start = new double[] { 1.0, -1.0, 2.0, -2.0 };
        double[] lower = new double[] { -5.0, -5.0, -5.0, -5.0 };
        double[] upper = new double[] { 5.0, 5.0, 5.0, 5.0 };

        RealPointValuePair result = optimizer.optimize(
                1500, function, GoalType.MINIMIZE, start, lower, upper
        );

        assertNotNull(result);
        for (int i = 0; i < 4; i++) {
            assertEquals(0.0, result.getPoint()[i], 1e-2);
        }
        assertEquals(0.0, result.getValue(), 1e-3);
    }
}