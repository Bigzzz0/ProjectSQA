package org.apache.commons.math.optimization.direct;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.analysis.MultivariateFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.exception.OutOfRangeException;

/**
 * Advanced white-box test suite for BOBYQAOptimizer targeting the known defect
 * (PathIsExploredException in prelim) and maximizing branch/line coverage.
 *
 * <pre>
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional optimization (Rosenbrock, simple quadratic)
 * - Partition B: Boundary values (dimension=2, npt=n+2, npt=(n+1)(n+2)/2, radius extremes)
 * - Partition C: Defect-targeted branch (constrained Rosen with more interpolation points)
 * - Partition D: Exception paths (invalid dimension, invalid npt, too small bounds)
 * - Partition E: Object lifecycle (constructor, getters, state after optimize)
 * </pre>
 */
public class BOBYQAOptimizerDeepseekTest {

    // --- Helper: Rosenbrock function (minimization) ---
    private static final MultivariateFunction ROSENBROCK = new MultivariateFunction() {
        @Override
        public double value(double[] point) {
            double x1 = point[0];
            double x2 = point[1];
            return 100 * (x2 - x1 * x1) * (x2 - x1 * x1) + (1 - x1) * (1 - x1);
        }
    };

    // --- Helper: simple quadratic (x1^2 + x2^2) ---
    private static final MultivariateFunction QUADRATIC = new MultivariateFunction() {
        @Override
        public double value(double[] point) {
            return point[0] * point[0] + point[1] * point[1];
        }
    };

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testSimpleQuadraticMinimize() {
        double[] lower = {-10, -10};
        double[] upper = {10, 10};
        double[] start = {-5, 5};
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5, 5.0, 1e-8);
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        RealPointValuePair result = optimizer.optimize(1000, QUADRATIC, GoalType.MINIMIZE);
        double[] point = result.getPoint();
        assertEquals(0.0, point[0], 1e-3);
        assertEquals(0.0, point[1], 1e-3);
        assertEquals(0.0, result.getValue(), 1e-3);
    }

    @Test(timeout = 4000)
    public void testRosenbrockMinimize() {
        double[] lower = {-10, -10};
        double[] upper = {10, 10};
        double[] start = {-5, 5};
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5, 5.0, 1e-8);
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        RealPointValuePair result = optimizer.optimize(2000, ROSENBROCK, GoalType.MINIMIZE);
        double[] point = result.getPoint();
        assertEquals(1.0, point[0], 1e-2);
        assertEquals(1.0, point[1], 1e-2);
        assertEquals(0.0, result.getValue(), 1e-2);
    }

    @Test(timeout = 4000)
    public void testMaximizeQuadratic() {
        double[] lower = {-5, -5};
        double[] upper = {5, 5};
        double[] start = {0, 0};
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5, 5.0, 1e-8);
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        // Maximize -x^2 - y^2 => maximum at (0,0) value 0
        RealPointValuePair result = optimizer.optimize(1000, QUADRATIC, GoalType.MAXIMIZE);
        assertEquals(0.0, result.getValue(), 1e-3);
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testMinimalDimension() {
        // dimension = 2 (minimum allowed)
        double[] lower = {-1, -1};
        double[] upper = {1, 1};
        double[] start = {0, 0};
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(4, 1.0, 1e-8);
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        RealPointValuePair result = optimizer.optimize(500, QUADRATIC, GoalType.MINIMIZE);
        assertEquals(0.0, result.getValue(), 1e-3);
    }

    @Test(timeout = 4000)
    public void testMinimalNumberOfInterpolationPoints() {
        // npt = n+2 = 4
        double[] lower = {-10, -10};
        double[] upper = {10, 10};
        double[] start = {-5, 5};
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(4, 5.0, 1e-8);
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        RealPointValuePair result = optimizer.optimize(2000, ROSENBROCK, GoalType.MINIMIZE);
        assertEquals(1.0, result.getPoint()[0], 1e-1);
        assertEquals(1.0, result.getPoint()[1], 1e-1);
    }

    @Test(timeout = 4000)
    public void testMaximalNumberOfInterpolationPoints() {
        // npt = (n+1)(n+2)/2 = 6 for n=2
        double[] lower = {-10, -10};
        double[] upper = {10, 10};
        double[] start = {-5, 5};
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(6, 5.0, 1e-8);
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        RealPointValuePair result = optimizer.optimize(2000, ROSENBROCK, GoalType.MINIMIZE);
        assertEquals(1.0, result.getPoint()[0], 1e-1);
        assertEquals(1.0, result.getPoint()[1], 1e-1);
    }

    @Test(timeout = 4000)
    public void testTightBounds() {
        // Bounds very close: difference < 2*initialRadius => radius adjusted
        double[] lower = {0, 0};
        double[] upper = {0.1, 0.1};
        double[] start = {0.05, 0.05};
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(4, 0.5, 1e-8);
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        RealPointValuePair result = optimizer.optimize(500, QUADRATIC, GoalType.MINIMIZE);
        assertEquals(0.0, result.getValue(), 1e-3);
    }

    // ========== Partition C: Defect-Targeted Branch ==========

    /**
     * This test directly targets the known defect:
     * PathIsExploredException thrown in prelim at line 1752.
     * The scenario is a constrained Rosenbrock with more interpolation points
     * (npt > 2n+1). The defective version throws an exception; the fixed version
     * should complete successfully.
     */
    @Test(timeout = 4000)
    public void testConstrainedRosenWithMoreInterpolationPoints() {
        double[] lower = {-10, -10};
        double[] upper = {10, 10};
        double[] start = {-5, 5};
        // Use npt = 6 (max allowed for n=2) to trigger the problematic path
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(6, 5.0, 1e-8);
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        // Should not throw PathIsExploredException
        RealPointValuePair result = optimizer.optimize(2000, ROSENBROCK, GoalType.MINIMIZE);
        double[] point = result.getPoint();
        assertEquals(1.0, point[0], 1e-1);
        assertEquals(1.0, point[1], 1e-1);
        assertEquals(0.0, result.getValue(), 1e-1);
    }

    // Additional defect-targeted: use npt=5 (also > 2n+1) with different bounds
    @Test(timeout = 4000)
    public void testConstrainedRosenWithFivePoints() {
        double[] lower = {-5, -5};
        double[] upper = {5, 5};
        double[] start = {0, 0};
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5, 2.0, 1e-8);
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        RealPointValuePair result = optimizer.optimize(2000, ROSENBROCK, GoalType.MINIMIZE);
        assertEquals(1.0, result.getPoint()[0], 1e-1);
        assertEquals(1.0, result.getPoint()[1], 1e-1);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testDimensionTooSmall() {
        double[] lower = {0};
        double[] upper = {1};
        double[] start = {0.5};
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(3, 1.0, 1e-8);
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        optimizer.optimize(100, QUADRATIC, GoalType.MINIMIZE);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testNumberOfInterpolationPointsTooSmall() {
        double[] lower = {-1, -1};
        double[] upper = {1, 1};
        double[] start = {0, 0};
        // npt = 3 < n+2 = 4
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(3, 1.0, 1e-8);
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        optimizer.optimize(100, QUADRATIC, GoalType.MINIMIZE);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testNumberOfInterpolationPointsTooLarge() {
        double[] lower = {-1, -1};
        double[] upper = {1, 1};
        double[] start = {0, 0};
        // npt = 7 > (n+1)(n+2)/2 = 6
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(7, 1.0, 1e-8);
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        optimizer.optimize(100, QUADRATIC, GoalType.MINIMIZE);
    }

    @Test(timeout = 4000)
    public void testBoundDifferenceTooSmall() {
        // Bounds difference < 2*initialRadius => radius adjusted internally
        double[] lower = {0, 0};
        double[] upper = {0.5, 0.5}; // diff = 0.5 < 2*1.0 = 2.0
        double[] start = {0.25, 0.25};
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(4, 1.0, 1e-8);
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        RealPointValuePair result = optimizer.optimize(500, QUADRATIC, GoalType.MINIMIZE);
        assertEquals(0.0, result.getValue(), 1e-3);
    }

    // ========== Partition E: Object Lifecycle & Contract ==========

    @Test(timeout = 4000)
    public void testConstructorDefaults() {
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        // Should use default radii
        double[] lower = {-10, -10};
        double[] upper = {10, 10};
        double[] start = {0, 0};
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        RealPointValuePair result = optimizer.optimize(500, QUADRATIC, GoalType.MINIMIZE);
        assertEquals(0.0, result.getValue(), 1e-3);
    }

    @Test(timeout = 4000)
    public void testRepeatedOptimize() {
        double[] lower = {-10, -10};
        double[] upper = {10, 10};
        double[] start = {-5, 5};
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5, 5.0, 1e-8);
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        RealPointValuePair result1 = optimizer.optimize(1000, ROSENBROCK, GoalType.MINIMIZE);
        // Second call with same settings
        optimizer.setStartPoint(start);
        RealPointValuePair result2 = optimizer.optimize(1000, ROSENBROCK, GoalType.MINIMIZE);
        assertEquals(result1.getValue(), result2.getValue(), 1e-3);
    }

    @Test(timeout = 4000)
    public void testStartPointAtBound() {
        double[] lower = {0, 0};
        double[] upper = {10, 10};
        double[] start = {0, 0}; // at lower bound
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(4, 2.0, 1e-8);
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        RealPointValuePair result = optimizer.optimize(500, QUADRATIC, GoalType.MINIMIZE);
        assertEquals(0.0, result.getValue(), 1e-3);
    }

    @Test(timeout = 4000)
    public void testStartPointAtUpperBound() {
        double[] lower = {-10, -10};
        double[] upper = {0, 0};
        double[] start = {0, 0}; // at upper bound
        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(4, 2.0, 1e-8);
        optimizer.setLowerBound(lower);
        optimizer.setUpperBound(upper);
        optimizer.setStartPoint(start);
        RealPointValuePair result = optimizer.optimize(500, QUADRATIC, GoalType.MINIMIZE);
        assertEquals(0.0, result.getValue(), 1e-3);
    }
}