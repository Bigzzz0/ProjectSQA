package org.apache.commons.math3.optim.nonlinear.scalar.noderiv;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.ConvergenceChecker;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Decision branches targeted:
 * 1. GoalType: MINIMIZE vs MAXIMIZE (comparator direction)
 * 2. Convergence: iteration == 0 vs > 0 (initialization path)
 * 3. Convergence: all points converged vs any diverged (loop exit)
 * 4. Simplex provided vs null (NullArgumentException)
 * 5. Bounds provided vs null (MathUnsupportedOperationException)
 * 6. Simplex re-initialization across multiple optimize calls
 * 7. Objective function evaluation count accuracy (via base class)
 * 
 * Boundary conditions:
 * - Zero tolerance values
 * - Very tight tolerance to force convergence after few iterations
 * - Function with known optimum at origin
 * 
 * Defect targeting: The known bug likely causes incorrect convergence detection or
 * improper simplex ordering when using the comparator. Tests verify that
 * optimization produces correct results for both Nelder-Mead and MultiDirectional
 * simplex variants under both MINIMIZE and MAXIMIZE goals.
 */
public class SimplexOptimizerDeepseekTest {

    // ======== Partition A: Core Functional Logic & State Transitions ========

    @Test(timeout = 4000)
    public void testMinimizeSimpleQuadratic() {
        // Simple 2D quadratic: f(x,y) = x^2 + y^2
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-10);
        optimizer.optimize(
            new MaxEval(1000),
            new ObjectiveFunction(f),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] {3.0, -4.0}),
            new NelderMeadSimplex(2)
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new ObjectiveFunction(f),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] {3.0, -4.0}),
            new NelderMeadSimplex(2)
        );

        assertEquals(0.0, result.getValue(), 1e-5);
        assertEquals(0.0, result.getPoint()[0], 1e-5);
        assertEquals(0.0, result.getPoint()[1], 1e-5);
    }

    @Test(timeout = 4000)
    public void testMaximizeSimpleQuadratic() {
        // Maximize f(x,y) = -(x^2 + y^2)  => optimum at (0,0)
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                return -(point[0] * point[0] + point[1] * point[1]);
            }
        };

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-10);
        PointValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new ObjectiveFunction(f),
            GoalType.MAXIMIZE,
            new InitialGuess(new double[] {1.0, 1.0}),
            new NelderMeadSimplex(2)
        );

        assertEquals(0.0, result.getValue(), 1e-5);
        assertEquals(0.0, result.getPoint()[0], 1e-5);
        assertEquals(0.0, result.getPoint()[1], 1e-5);
    }

    @Test(timeout = 4000)
    public void testMinimizeWithMultiDirectionalSimplex() {
        // Use MultiDirectional simplex (not just NelderMead)
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                int sum = 0;
                for (int i = 0; i < point.length; i++) {
                    sum += (i+1) * point[i] * point[i];
                }
                return sum;
            }
        };

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-8, 1e-8);
        PointValuePair result = optimizer.optimize(
            new MaxEval(2000),
            new ObjectiveFunction(f),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] {2.0, -1.0, 0.5}),
            new MultiDirectionalSimplex(3)
        );

        assertEquals(0.0, result.getValue(), 1e-4);
        // Point should be near zero
        for (double x : result.getPoint()) {
            assertEquals(0.0, x, 1e-4);
        }
    }

    @Test(timeout = 4000)
    public void testMaximizeWithMultiDirectionalSimplex() {
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                // Rosenbrock in reverse: maximize -Rosenbrock
                double x = point[0];
                double y = point[1];
                return -(100 * (y - x*x)*(y - x*x) + (1 - x)*(1 - x));
            }
        };

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-8, 1e-8);
        PointValuePair result = optimizer.optimize(
            new MaxEval(5000),
            new ObjectiveFunction(f),
            GoalType.MAXIMIZE,
            new InitialGuess(new double[] {-1.0, 1.0}),
            new MultiDirectionalSimplex(2)
        );

        // Maximum at (1,1) with value 0
        assertEquals(0.0, result.getValue(), 1e-3);
        assertEquals(1.0, result.getPoint()[0], 1e-2);
        assertEquals(1.0, result.getPoint()[1], 1e-2);
    }

    // ======== Partition B: Boundary Value Analysis & Extremes ========

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testNullSimplex() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-10);
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };
        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(f),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] {1.0})
            // No AbstractSimplex provided -> null
        );
    }

    @Test(expected = MathUnsupportedOperationException.class, timeout = 4000)
    public void testBoundsThrowsException() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-10);
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };
        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(f),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] {0.5}),
            new NelderMeadSimplex(1),
            new SimpleBounds(new double[] {-1.0}, new double[] {1.0})
        );
    }

    @Test(timeout = 4000)
    public void testZeroToleranceConvergence() {
        // With zero tolerance, convergence might never happen but we use maxEval
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + 1;
            }
        };
        SimplexOptimizer optimizer = new SimplexOptimizer(0.0, 0.0);
        PointValuePair result = optimizer.optimize(
            new MaxEval(500),
            new ObjectiveFunction(f),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] {10.0}),
            new NelderMeadSimplex(1)
        );
        // Should converge to minimum near 1.0
        assertEquals(1.0, result.getValue(), 1e-4);
        assertEquals(0.0, result.getPoint()[0], 1e-4);
    }

    // ======== Partition C: Defect-Targeted Branch Zone ========

    @Test(timeout = 4000)
    public void testOptimizeAfterConvergenceReusesSimplex() {
        // Call optimize twice with different start points; verify second call does not reuse previous state incorrectly
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-10);
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        // First optimization from (3,4)
        PointValuePair result1 = optimizer.optimize(
            new MaxEval(1000),
            new ObjectiveFunction(f),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] {3.0, 4.0}),
            new NelderMeadSimplex(2)
        );
        assertEquals(0.0, result1.getValue(), 1e-6);

        // Second optimization from different start (10,-10) should still converge
        PointValuePair result2 = optimizer.optimize(
            new MaxEval(1000),
            new ObjectiveFunction(f),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] {10.0, -10.0}),
            new NelderMeadSimplex(2)
        );
        assertEquals(0.0, result2.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testConvergenceCheckerUsesAllPoints() {
        // Ensure that convergence check over all simplex points does not cause premature exit
        // Use a function where the simplex might have points with similar but not identical values
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                // Linear function: gradient constant, simplex will shrink
                return point[0] + point[1];
            }
        };
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-5, 1e-5);
        PointValuePair result = optimizer.optimize(
            new MaxEval(2000),
            new ObjectiveFunction(f),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] {1.0, -1.0}),
            new NelderMeadSimplex(2)
        );
        // For linear function, minimum is unbounded, but optimizer should run to maxEval or convergence
        // We just check it doesn't throw exception
        assertNotNull(result);
    }

    // ======== Partition D: Exception & Defensive Guard Paths ========

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testNullInitialGuess() {
        // This is handled by base class, but we test that optimizer propagates
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-10);
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0];
            }
        };
        optimizer.optimize(
            new MaxEval(10),
            new ObjectiveFunction(f),
            GoalType.MINIMIZE,
            new InitialGuess(null), // null
            new NelderMeadSimplex(1)
        );
    }

    @Test(timeout = 4000)
    public void testHighDimensionSimplex() {
        // 5D Rosenbrock derivative: ensure optimizer handles larger dimensions
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] x) {
                double sum = 0;
                for (int i = 0; i < x.length - 1; i++) {
                    sum += 100 * (x[i+1] - x[i]*x[i]) * (x[i+1] - x[i]*x[i]) + (1 - x[i])*(1 - x[i]);
                }
                return sum;
            }
        };

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-6, 1e-6);
        PointValuePair result = optimizer.optimize(
            new MaxEval(10000),
            new ObjectiveFunction(f),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] {-1.0, 1.0, -1.0, 1.0, -1.0}),
            new NelderMeadSimplex(5)
        );

        // Accept approximate convergence to optimum near 0
        assertEquals(0.0, result.getValue(), 0.5);
        for (int i = 0; i < 5; i++) {
            // Each coordinate should be near 1.0 for Rosenbrock
            assertEquals(1.0, result.getPoint()[i], 0.5);
        }
    }

    // ======== Partition E: Object Lifecycle & Contract Integrity ========

    @Test(timeout = 4000)
    public void testComparatorOrderForMinimize() {
        // Indirectly test that the comparator inside doOptimize orders correctly for minimization
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0];
            }
        };

        SimplexOptimizer optimizer = new SimplexOptimizer(new SimpleValueChecker(1e-10, 1e-10));
        // Use a simplex that will evaluate points in known order
        PointValuePair result = optimizer.optimize(
            new MaxEval(200),
            new ObjectiveFunction(f),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] {5.0}),
            new AbstractSimplex(1) {
                @Override
                public void iterate(MultivariateFunction evalFunc, Comparator<PointValuePair> comparator) {
                    // Manually set simplex points to test comparator
                    PointValuePair[] points = getPoints();
                    points[0] = new PointValuePair(new double[] {1.0}, 1.0);
                    points[1] = new PointValuePair(new double[] {2.0}, 2.0);
                    // Re-sort according to comparator (should order by value ascending for minimize)
                    java.util.Arrays.sort(points, comparator);
                    // Now points[0] should be (1.0,1.0) and points[1] should be (2.0,2.0)
                    setPoints(points);
                }
            }
        );

        // After iterate, best point should be the one with smallest value
        assertEquals(1.0, result.getValue(), 0.0);
        assertEquals(1.0, result.getPoint()[0], 0.0);
    }

    @Test(timeout = 4000)
    public void testComparatorOrderForMaximize() {
        // Indirectly test that the comparator inside doOptimize orders correctly for maximization
        MultivariateFunction f = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0];
            }
        };

        SimplexOptimizer optimizer = new SimplexOptimizer(new SimpleValueChecker(1e-10, 1e-10));
        PointValuePair result = optimizer.optimize(
            new MaxEval(200),
            new ObjectiveFunction(f),
            GoalType.MAXIMIZE,
            new InitialGuess(new double[] {5.0}),
            new AbstractSimplex(1) {
                @Override
                public void iterate(MultivariateFunction evalFunc, Comparator<PointValuePair> comparator) {
                    PointValuePair[] points = getPoints();
                    points[0] = new PointValuePair(new double[] {1.0}, 1.0);
                    points[1] = new PointValuePair(new double[] {2.0}, 2.0);
                    java.util.Arrays.sort(points, comparator);
                    // For maximize, comparator orders descending values, so points[0] should be (2.0,2.0)
                    setPoints(points);
                }
            }
        );

        assertEquals(2.0, result.getValue(), 0.0);
        assertEquals(2.0, result.getPoint()[0], 0.0);
    }
}