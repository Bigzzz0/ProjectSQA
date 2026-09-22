/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math3.optim.nonlinear.scalar.noderiv;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.util.FastMath;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer
 * Defects4J Defect Focus:
 *  - Defect (Math-6): Failure in optimizers (e.g. PowellOptimizerTest::testSumSinc) where
 *    iteration counter increment was omitted in doOptimize(). getIterations() remains 0,
 *    and iteration count contracts fail.
 *
 * Decision / Branch Matrix:
 *  1. PowellOptimizer(rel, abs, checker) & PowellOptimizer(rel, abs, lineRel, lineAbs, checker):
 *     - rel < MIN_RELATIVE_TOLERANCE (2 * ulp(1.0)) -> NumberIsTooSmallException (Branch True/False)
 *     - abs <= 0 -> NotStrictlyPositiveException (Branch True/False)
 *  2. doOptimize() -> checkParameters():
 *     - lowerBound != null || upperBound != null -> MathUnsupportedOperationException (Branch True/False)
 *  3. doOptimize() -> line search loop:
 *     - (fX2 - fVal) > delta -> delta and bigInd update (Branch True/False)
 *  4. Default convergence check:
 *     - 2 * (fX - fVal) <= (relativeThreshold * (|fX| + |fVal|) + absoluteThreshold) (Branch True/False)
 *  5. Custom ConvergenceChecker:
 *     - checker != null && checker.converged(...) (Branch True/False)
 *  6. Termination outcome:
 *     - goal == GoalType.MINIMIZE: (fVal < fX) ? current : previous (Both branches covered)
 *     - goal == GoalType.MAXIMIZE: (fVal > fX) ? current : previous (Both branches covered)
 *  7. Direction update:
 *     - fX > fX2 (Branch True/False)
 *     - t < 0.0 (Branch True/False) -> replace direction in direc matrix
 */
public class PowellOptimizerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testOptimizeQuadraticMinimization() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-9, 1e-9);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                double x = point[0] - 2.0;
                double y = point[1] - 3.0;
                return x * x + y * y;
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new ObjectiveFunction(func),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 })
        );

        assertNotNull(optimum);
        assertEquals(2.0, optimum.getPoint()[0], 1e-4);
        assertEquals(3.0, optimum.getPoint()[1], 1e-4);
        assertEquals(0.0, optimum.getValue(), 1e-7);
    }

    @Test(timeout = 4000)
    public void testOptimizeQuadraticMaximization() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-9, 1e-9);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                double x = point[0] - 1.0;
                double y = point[1] + 2.0;
                return -(x * x + y * y);
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new ObjectiveFunction(func),
            GoalType.MAXIMIZE,
            new InitialGuess(new double[] { 5.0, -5.0 })
        );

        assertNotNull(optimum);
        assertEquals(1.0, optimum.getPoint()[0], 1e-4);
        assertEquals(-2.0, optimum.getPoint()[1], 1e-4);
        assertEquals(0.0, optimum.getValue(), 1e-7);
    }

    @Test(timeout = 4000)
    public void testMinimizeReturnsPreviousWhenAlreadyAtOptimum() {
        // When start point is exactly at optimum, fVal < fX is false -> returns previous
        PowellOptimizer optimizer = new PowellOptimizer(1e-5, 1e-5);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            new ObjectiveFunction(func),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 })
        );

        assertNotNull(optimum);
        assertEquals(0.0, optimum.getPoint()[0], 1e-6);
        assertEquals(0.0, optimum.getPoint()[1], 1e-6);
        assertEquals(0.0, optimum.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testMaximizeReturnsPreviousWhenAlreadyAtOptimum() {
        // When start point is exactly at maximum, fVal > fX is false -> returns previous
        PowellOptimizer optimizer = new PowellOptimizer(1e-5, 1e-5);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return -(point[0] * point[0] + point[1] * point[1]);
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            new ObjectiveFunction(func),
            GoalType.MAXIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 })
        );

        assertNotNull(optimum);
        assertEquals(0.0, optimum.getPoint()[0], 1e-6);
        assertEquals(0.0, optimum.getPoint()[1], 1e-6);
        assertEquals(0.0, optimum.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testUserDefinedConvergenceCheckerEarlyStop() {
        final boolean[] checkerCalled = new boolean[] { false };
        ConvergenceChecker<PointValuePair> customChecker = new ConvergenceChecker<PointValuePair>() {
            public boolean converged(int iteration, PointValuePair previous, PointValuePair current) {
                checkerCalled[0] = true;
                // Force early termination on iteration 1
                return iteration >= 1;
            }
        };

        // Strict thresholds so default stopping criterion does not fire on iteration 1
        PowellOptimizer optimizer = new PowellOptimizer(1e-15, 1e-15, customChecker);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 10.0) * (point[0] - 10.0);
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(500),
            new ObjectiveFunction(func),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0 })
        );

        assertNotNull(optimum);
        assertTrue("Custom convergence checker must have been invoked", checkerCalled[0]);
    }

    @Test(timeout = 4000)
    public void testAllConstructorsCoverage() {
        // Constructor 1: (rel, abs)
        PowellOptimizer opt1 = new PowellOptimizer(1e-6, 1e-6);
        assertNotNull(opt1);

        // Constructor 2: (rel, abs, checker)
        PowellOptimizer opt2 = new PowellOptimizer(1e-6, 1e-6, null);
        assertNotNull(opt2);

        // Constructor 3: (rel, abs, lineRel, lineAbs)
        PowellOptimizer opt3 = new PowellOptimizer(1e-6, 1e-6, 1e-4, 1e-4);
        assertNotNull(opt3);

        // Constructor 4: (rel, abs, lineRel, lineAbs, checker)
        PowellOptimizer opt4 = new PowellOptimizer(1e-6, 1e-6, 1e-4, 1e-4, null);
        assertNotNull(opt4);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testRelAtExactMinimumToleranceAllowed() {
        double minRel = 2 * FastMath.ulp(1d);
        // Should succeed without exception
        PowellOptimizer optimizer = new PowellOptimizer(minRel, 1e-8);
        assertNotNull(optimizer);
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testRelJustBelowMinimumToleranceThrows() {
        double minRel = 2 * FastMath.ulp(1d);
        double belowMin = Math.nextDown(minRel);
        new PowellOptimizer(belowMin, 1e-8);
    }

    @Test(timeout = 4000)
    public void testAbsAtDoubleMinValueAllowed() {
        // Smallest strictly positive double
        PowellOptimizer optimizer = new PowellOptimizer(1e-6, Double.MIN_VALUE);
        assertNotNull(optimizer);
    }

    @Test(timeout = 4000)
    public void testOneDimensionalOptimization() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-8);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 42.0) * (point[0] - 42.0);
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(500),
            new ObjectiveFunction(func),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0 })
        );

        assertNotNull(optimum);
        assertEquals(42.0, optimum.getPoint()[0], 1e-4);
        assertEquals(0.0, optimum.getValue(), 1e-7);
    }

    @Test(timeout = 4000)
    public void testHighDimensionalOptimization() {
        int dim = 6;
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-8);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                double sum = 0;
                for (int i = 0; i < point.length; i++) {
                    double diff = point[i] - (i + 1);
                    sum += diff * diff;
                }
                return sum;
            }
        };

        double[] init = new double[dim];
        PointValuePair optimum = optimizer.optimize(
            new MaxEval(5000),
            new ObjectiveFunction(func),
            GoalType.MINIMIZE,
            new InitialGuess(init)
        );

        assertNotNull(optimum);
        for (int i = 0; i < dim; i++) {
            assertEquals((double) (i + 1), optimum.getPoint()[i], 1e-3);
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Math-6 Iterations Defect)
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetIterationsIncrementsAfterOptimization() {
        // Defect check: PowellOptimizer in buggy revision did not increment iteration count,
        // causing optimizer.getIterations() to remain 0 after optimization.
        PowellOptimizer optimizer = new PowellOptimizer(1e-6, 1e-6);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 3.0) * (point[0] - 3.0) + (point[1] + 4.0) * (point[1] + 4.0);
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new ObjectiveFunction(func),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 })
        );

        assertNotNull(optimum);
        assertTrue("Optimizer iteration count must be strictly positive after optimization",
                   optimizer.getIterations() > 0);
    }

    @Test(timeout = 4000)
    public void testSumSincFunctionOptimizationAndIterations() {
        // Mirrors the exact scenario from Defects4J PowellOptimizerTest::testSumSinc
        final MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                double sum = 0;
                for (double v : point) {
                    double sinc = (v == 0) ? 1.0 : FastMath.sin(v) / v;
                    sum += sinc;
                }
                return -sum; // Minimum is at (0, 0), value = -2.0
            }
        };

        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-8);
        PointValuePair optimum = optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(100),
            new ObjectiveFunction(func),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 3.0, 3.0 })
        );

        assertNotNull(optimum);
        assertEquals(-2.0, optimum.getValue(), 1e-5);
        assertTrue("Iterations must be counted and greater than 0", optimizer.getIterations() > 0);
    }

    @Test(timeout = 4000)
    public void testRosenbrockValleyExercisesDirectionUpdate() {
        // Rosenbrock function: f(x, y) = 100*(y - x^2)^2 + (1 - x)^2
        // Optimizing this banana-shaped valley forces t < 0.0, triggering direction vector substitution
        PowellOptimizer optimizer = new PowellOptimizer(1e-9, 1e-9);
        MultivariateFunction rosenbrock = new MultivariateFunction() {
            public double value(double[] x) {
                double a = x[1] - x[0] * x[0];
                double b = 1.0 - x[0];
                return 100.0 * a * a + b * b;
            }
        };

        PointValuePair optimum = optimizer.optimize(
            new MaxEval(10000),
            new MaxIter(1000),
            new ObjectiveFunction(rosenbrock),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { -1.2, 1.0 })
        );

        assertNotNull(optimum);
        assertEquals(1.0, optimum.getPoint()[0], 1e-2);
        assertEquals(1.0, optimum.getPoint()[1], 1e-2);
        assertEquals(0.0, optimum.getValue(), 1e-4);
        assertTrue(optimizer.getIterations() > 1);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testConstructorThrowsWhenRelTooSmall3Args() {
        new PowellOptimizer(1e-20, 1.0, null);
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testConstructorThrowsWhenRelNegative3Args() {
        new PowellOptimizer(-1.0, 1.0, null);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorThrowsWhenAbsZero3Args() {
        new PowellOptimizer(1e-6, 0.0, null);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorThrowsWhenAbsNegative3Args() {
        new PowellOptimizer(1e-6, -1.0, null);
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testConstructorThrowsWhenRelTooSmall5Args() {
        new PowellOptimizer(0.0, 1.0, 1e-4, 1e-4, null);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorThrowsWhenAbsNegative5Args() {
        new PowellOptimizer(1e-6, -0.5, 1e-4, 1e-4, null);
    }

    @Test(expected = MathUnsupportedOperationException.class, timeout = 4000)
    public void testBoundsUnsupportedThrowsException() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-6, 1e-6);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        // Passing SimpleBounds triggers checkParameters() throwing MathUnsupportedOperationException
        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(func),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 2.0 }),
            new SimpleBounds(new double[] { 0.0 }, new double[] { 5.0 })
        );
    }

    @Test(expected = TooManyEvaluationsException.class, timeout = 4000)
    public void testExceedingMaxEvaluationsThrows() {
        PowellOptimizer optimizer = new PowellOptimizer(1e-8, 1e-8);
        MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] point) {
                return (point[0] - 100.0) * (point[0] - 100.0) + (point[1] - 100.0) * (point[1] - 100.0);
            }
        };

        // Very restrictive evaluation budget
        optimizer.optimize(
            new MaxEval(5),
            new ObjectiveFunction(func),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 })
        );
    }
}