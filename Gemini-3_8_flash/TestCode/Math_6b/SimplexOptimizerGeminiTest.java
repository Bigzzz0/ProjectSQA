/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer
 *
 * Decision / Condition Coverage Targets:
 * 1. SimplexOptimizer(ConvergenceChecker) vs SimplexOptimizer(double, double) constructors.
 * 2. parseOptimizationData(OptimizationData...):
 *    - data instanceof AbstractSimplex (true / false / multiple data items).
 *    - Reusing previous simplex vs updating with new simplex.
 * 3. checkParameters():
 *    - simplex == null -> throws NullArgumentException.
 *    - getLowerBound() != null -> throws MathUnsupportedOperationException.
 *    - getUpperBound() != null -> throws MathUnsupportedOperationException.
 * 4. doOptimize():
 *    - isMinim: GoalType.MINIMIZE vs GoalType.MAXIMIZE (both branches of comparator).
 *    - iteration == 0 (convergence check bypassed).
 *    - iteration > 0 with converged == false (continues iteration loop).
 *    - iteration > 0 with converged == true (returns simplex.getPoint(0)).
 *    - comparator returning negative, zero, positive.
 *
 * Defects4J Bug Target (MATH-6):
 * - Iteration counter not incremented on the optimizer base class during doOptimize loop.
 * - getIterations() fails to reflect iterations executed (> 0) upon convergence.
 */
package org.apache.commons.math3.optim.nonlinear.scalar.noderiv;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SimplexOptimizerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinimizeQuadraticNelderMead() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-9, 1e-12);
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return (point[0] - 2.0) * (point[0] - 2.0) + (point[1] + 3.0) * (point[1] + 3.0);
            }
        };

        PointValuePair optimum = optimizer.optimize(
                new MaxEval(300),
                new ObjectiveFunction(sphere),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{0.0, 0.0}),
                new NelderMeadSimplex(new double[]{0.5, 0.5})
        );

        assertNotNull(optimum);
        assertEquals(2.0, optimum.getPoint()[0], 1e-4);
        assertEquals(-3.0, optimum.getPoint()[1], 1e-4);
        assertEquals(0.0, optimum.getValue(), 1e-6);
        assertTrue(optimizer.getEvaluations() > 0);
    }

    @Test(timeout = 4000)
    public void testMaximizeInvertedParaboloidMultiDirectional() {
        SimplexOptimizer optimizer = new SimplexOptimizer(new SimpleValueChecker(1e-8, 1e-10));
        MultivariateFunction invertedParaboloid = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return 10.0 - (point[0] - 1.0) * (point[0] - 1.0) - (point[1] - 2.0) * (point[1] - 2.0);
            }
        };

        PointValuePair optimum = optimizer.optimize(
                new MaxEval(400),
                new ObjectiveFunction(invertedParaboloid),
                GoalType.MAXIMIZE,
                new InitialGuess(new double[]{0.0, 0.0}),
                new MultiDirectionalSimplex(new double[]{0.2, 0.2})
        );

        assertNotNull(optimum);
        assertEquals(1.0, optimum.getPoint()[0], 1e-3);
        assertEquals(2.0, optimum.getPoint()[1], 1e-3);
        assertEquals(10.0, optimum.getValue(), 1e-4);
        assertTrue(optimizer.getEvaluations() > 0);
    }

    @Test(timeout = 4000)
    public void testSimplexReuseAcrossOptimizations() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-8, 1e-8);
        MultivariateFunction func = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        // First optimization configures the simplex
        PointValuePair opt1 = optimizer.optimize(
                new MaxEval(100),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{2.0}),
                new NelderMeadSimplex(1)
        );
        assertEquals(0.0, opt1.getPoint()[0], 1e-3);

        // Second optimization omits NelderMeadSimplex in arguments to test simplex persistence/reuse
        PointValuePair opt2 = optimizer.optimize(
                new MaxEval(100),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{-3.0})
        );
        assertEquals(0.0, opt2.getPoint()[0], 1e-3);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Guard Conditions
    // =========================================================================

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testCheckParametersThrowsNullSimplex() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-6, 1e-6);
        MultivariateFunction func = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0];
            }
        };

        // Optimize without passing any AbstractSimplex instance
        optimizer.optimize(
                new MaxEval(50),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{1.0})
        );
    }

    @Test(expected = MathUnsupportedOperationException.class, timeout = 4000)
    public void testCheckParametersThrowsWhenSimpleBoundsPassed() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-6, 1e-6);
        MultivariateFunction func = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0];
            }
        };

        optimizer.optimize(
                new MaxEval(50),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{1.0}),
                new NelderMeadSimplex(1),
                new SimpleBounds(new double[]{0.0}, new double[]{2.0})
        );
    }

    @Test(expected = MathUnsupportedOperationException.class, timeout = 4000)
    public void testCheckParametersThrowsWhenOnlyLowerBoundSpecified() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-6, 1e-6);
        MultivariateFunction func = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0];
            }
        };

        optimizer.optimize(
                new MaxEval(50),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{1.0}),
                new NelderMeadSimplex(1),
                new SimpleBounds(new double[]{0.0}, new double[]{Double.POSITIVE_INFINITY})
        );
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-6 Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testIterationCountUpdatedNelderMead() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-8, 1e-12);
        MultivariateFunction func = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        optimizer.optimize(
                new MaxEval(200),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{5.0, 5.0}),
                new NelderMeadSimplex(new double[]{0.5, 0.5})
        );

        // Targets MATH-6: BaseOptimizer iterations counter must be incremented during search
        assertTrue("Optimizer iteration count must be strictly positive after convergence",
                optimizer.getIterations() > 0);
    }

    @Test(timeout = 4000)
    public void testIterationCountUpdatedMultiDirectional() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-8, 1e-12);
        MultivariateFunction func = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        optimizer.optimize(
                new MaxEval(100),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{3.0}),
                new MultiDirectionalSimplex(new double[]{0.5})
        );

        // Targets MATH-6: MultiDirectional optimization must increment iterations
        assertTrue("Optimizer iteration count must be strictly positive after convergence",
                optimizer.getIterations() > 0);
    }

    // =========================================================================
    // Partition D: Custom Checker & Extreme/Corner Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testImmediateConvergenceOnFirstIteration() {
        // A convergence checker that immediately marks as converged at iteration 1
        ConvergenceChecker<PointValuePair> immediateChecker = new ConvergenceChecker<PointValuePair>() {
            @Override
            public boolean converged(int iteration, PointValuePair previous, PointValuePair current) {
                return iteration >= 1;
            }
        };

        SimplexOptimizer optimizer = new SimplexOptimizer(immediateChecker);
        MultivariateFunction func = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        PointValuePair result = optimizer.optimize(
                new MaxEval(50),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{10.0}),
                new NelderMeadSimplex(1)
        );

        assertNotNull(result);
        assertTrue(optimizer.getEvaluations() > 0);
    }

    @Test(timeout = 4000)
    public void testPointCheckerWithIdenticalValuesComparatorPath() {
        // Tests comparator branch when points return identical function values (Double.compare(v1, v2) == 0)
        SimplexOptimizer optimizer = new SimplexOptimizer(new SimplePointChecker<PointValuePair>(1e-4, 1e-4));
        MultivariateFunction constantFunc = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return 42.0;
            }
        };

        PointValuePair res = optimizer.optimize(
                new MaxEval(50),
                new ObjectiveFunction(constantFunc),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{1.0, 2.0}),
                new MultiDirectionalSimplex(new double[]{0.1, 0.1})
        );

        assertNotNull(res);
        assertEquals(42.0, res.getValue(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testParseOptimizationDataWithMultipleAbstractSimplex() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-6, 1e-6);
        MultivariateFunction func = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        NelderMeadSimplex simplex1 = new NelderMeadSimplex(1);
        MultiDirectionalSimplex simplex2 = new MultiDirectionalSimplex(1);

        // When multiple simplices are passed, the first instance is registered due to 'break' in parseOptimizationData
        PointValuePair res = optimizer.optimize(
                new MaxEval(100),
                new ObjectiveFunction(func),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{2.0}),
                simplex1,
                simplex2
        );

        assertNotNull(res);
        assertEquals(0.0, res.getPoint()[0], 1e-3);
    }
}