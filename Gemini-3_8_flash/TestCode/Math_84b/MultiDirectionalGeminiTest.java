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

package org.apache.commons.math.optimization.direct;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.math.optimization.direct.MultiDirectional
 *
 * Coverage & Branch Decision Analysis:
 * 1. MultiDirectional() [Default Constructor]:
 *    - khi = 2.0, gamma = 0.5
 * 2. MultiDirectional(double khi, double gamma) [Custom Constructor]:
 *    - Valid custom positive expansion and contraction factors.
 * 3. iterateSimplex(Comparator<RealPointValuePair> comparator):
 *    - Branch 1: evaluateNewSimplex(original, 1.0) -> comparator.compare(reflected, best) < 0
 *      - Branch 1a: comparator.compare(reflected, expanded) <= 0 -> simplex = reflectedSimplex (accept reflection)
 *      - Branch 1b: comparator.compare(reflected, expanded) > 0 -> accept expanded simplex
 *    - Branch 2: evaluateNewSimplex(original, gamma) -> comparator.compare(contracted, best) < 0
 *      - Branch 2a: accept contracted simplex and return.
 *      - Branch 2b: loop continues inside while(true) -> triggers convergence check or loop continuation.
 * 4. evaluateNewSimplex(RealPointValuePair[] original, double coeff, Comparator comparator):
 *    - Linear transformation computation for n-dimensional points.
 *    - Simplex evaluation and reordering via evaluateSimplex(comparator).
 *
 * Defects4J Ground Truth Defects Targeted:
 * - MATH-283 / MultiDirectionalTest::testMath283:
 *   Optimization of 1D function failure where iteration counter exceeded 100 because of improper
 *   simplex iteration loop convergence handling / iteration counting when simplex fails to improve.
 * - MultiDirectionalTest::testMinimizeMaximize:
 *   Simplex reflection/expansion/contraction behavior under both MINIMIZE and MAXIMIZE goal types
 *   for multi-extrema landscape (fourExtrema function).
 */

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.SimpleScalarValueChecker;
import org.apache.commons.math.optimization.SimpleRealPointChecker;
import org.apache.commons.math.analysis.MultivariateRealFunction;

public class MultiDirectionalGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorAndSphereOptimization()
            throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(200);
        optimizer.setMaxEvaluations(1000);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-8, 1.0e-12));

        MultivariateRealFunction sphere = new MultivariateRealFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        RealPointValuePair result = optimizer.optimize(sphere, GoalType.MINIMIZE, new double[] { 2.5, -1.8 });
        assertNotNull(result);
        assertEquals(0.0, result.getPoint()[0], 1.0e-3);
        assertEquals(0.0, result.getPoint()[1], 1.0e-3);
        assertEquals(0.0, result.getValue(), 1.0e-5);
        assertTrue(optimizer.getIterations() > 0);
        assertTrue(optimizer.getEvaluations() > 0);
    }

    @Test(timeout = 4000)
    public void testCustomCoefficientsExpansionAcceptance()
            throws FunctionEvaluationException, OptimizationException {
        // khi = 3.0, gamma = 0.25: triggers expansion acceptance path
        MultiDirectional optimizer = new MultiDirectional(3.0, 0.25);
        optimizer.setMaxIterations(200);
        optimizer.setMaxEvaluations(1000);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-7, 1.0e-10));

        MultivariateRealFunction parabolic = new MultivariateRealFunction() {
            public double value(double[] point) {
                double dx = point[0] - 2.0;
                double dy = point[1] + 3.0;
                return dx * dx + dy * dy;
            }
        };

        RealPointValuePair result = optimizer.optimize(parabolic, GoalType.MINIMIZE, new double[] { 0.0, 0.0 });
        assertEquals(2.0, result.getPoint()[0], 1.0e-3);
        assertEquals(-3.0, result.getPoint()[1], 1.0e-3);
        assertEquals(0.0, result.getValue(), 1.0e-5);
    }

    @Test(timeout = 4000)
    public void testRosenbrockValley()
            throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(500);
        optimizer.setMaxEvaluations(2500);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-6, 1.0e-10));

        MultivariateRealFunction rosenbrock = new MultivariateRealFunction() {
            public double value(double[] x) {
                double a = x[1] - x[0] * x[0];
                double b = 1.0 - x[0];
                return 100.0 * a * a + b * b;
            }
        };

        RealPointValuePair optimum = optimizer.optimize(rosenbrock, GoalType.MINIMIZE, new double[] { -1.2, 1.0 });
        assertEquals(1.0, optimum.getPoint()[0], 1.0e-2);
        assertEquals(1.0, optimum.getPoint()[1], 1.0e-2);
        assertEquals(0.0, optimum.getValue(), 1.0e-3);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testOneDimensionalOptimization()
            throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(150);
        optimizer.setMaxEvaluations(500);
        optimizer.setConvergenceChecker(new SimpleRealPointChecker(1.0e-8, 1.0e-10));

        MultivariateRealFunction quad1D = new MultivariateRealFunction() {
            public double value(double[] x) {
                double diff = x[0] - 42.0;
                return diff * diff;
            }
        };

        RealPointValuePair optimum = optimizer.optimize(quad1D, GoalType.MINIMIZE, new double[] { 0.0 });
        assertEquals(42.0, optimum.getPoint()[0], 1.0e-3);
        assertEquals(0.0, optimum.getValue(), 1.0e-5);
    }

    @Test(timeout = 4000)
    public void testHighDimensionalSphere()
            throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional(1.8, 0.4);
        optimizer.setMaxIterations(400);
        optimizer.setMaxEvaluations(3000);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-6, 1.0e-9));

        final int dim = 5;
        MultivariateRealFunction sphere5D = new MultivariateRealFunction() {
            public double value(double[] point) {
                double sum = 0.0;
                for (double v : point) {
                    sum += v * v;
                }
                return sum;
            }
        };

        double[] start = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0 };
        RealPointValuePair optimum = optimizer.optimize(sphere5D, GoalType.MINIMIZE, start);
        for (int i = 0; i < dim; ++i) {
            assertEquals(0.0, optimum.getPoint()[i], 1.0e-2);
        }
        assertEquals(0.0, optimum.getValue(), 1.0e-3);
    }

    @Test(timeout = 4000)
    public void testCustomSimplexConfiguration()
            throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(200);
        optimizer.setMaxEvaluations(1000);

        // Custom start configuration steps
        optimizer.setStartConfiguration(new double[] { 0.5, 0.5 });
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-8, 1.0e-12));

        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] x) {
                return (x[0] - 5.0) * (x[0] - 5.0) + (x[1] - 3.0) * (x[1] - 3.0);
            }
        };

        RealPointValuePair optimum = optimizer.optimize(f, GoalType.MINIMIZE, new double[] { 0.0, 0.0 });
        assertEquals(5.0, optimum.getPoint()[0], 1.0e-3);
        assertEquals(3.0, optimum.getPoint()[1], 1.0e-3);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J MATH-283:
     * Optimization of 1D function must not exceed 100 iterations.
     * In defective versions, iterations are not counted/converged correctly in iterateSimplex.
     */
    @Test(timeout = 4000)
    public void testMath283() throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(100);
        optimizer.setMaxEvaluations(1000);
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-11, 1.0e-30));

        final MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] x) {
                final double d = x[0] - 1.0;
                return d * d;
            }
        };

        RealPointValuePair optimum = optimizer.optimize(f, GoalType.MINIMIZE, new double[] { 10.0 });
        assertEquals(1.0, optimum.getPoint()[0], 1.0e-5);
        assertEquals(0.0, optimum.getValue(), 1.0e-10);
        assertTrue("Optimizer iterations (" + optimizer.getIterations() + ") should be <= 50",
                optimizer.getIterations() <= 50);
        assertTrue("Optimizer evaluations (" + optimizer.getEvaluations() + ") should be <= 100",
                optimizer.getEvaluations() <= 100);
    }

    /**
     * Targets Defects4J MultiDirectionalTest::testMinimizeMaximize:
     * Tests optimization on a multi-modal landscape under both MINIMIZE and MAXIMIZE goal types.
     */
    @Test(timeout = 4000)
    public void testMinimizeMaximize() throws FunctionEvaluationException, OptimizationException {
        MultivariateRealFunction fourExtrema = new MultivariateRealFunction() {
            public double value(double[] variables) {
                final double x = variables[0];
                final double y = variables[1];
                return ((x == 0) || (y == 0)) ? 0 :
                        Math.atan(x) * Math.atan(x + 2) * Math.atan(y) * Math.atan(y) / (x * y);
            }
        };

        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-11, 1.0e-30));
        optimizer.setMaxIterations(200);
        optimizer.setStartConfiguration(new double[] { 0.2, 0.2 });
        RealPointValuePair optimum;

        // Minimizing branch
        optimum = optimizer.optimize(fourExtrema, GoalType.MINIMIZE, new double[] { -0.5, 0.5 });
        assertEquals(-0.2719561299878, optimum.getValue(), 1.0e-4);
        assertEquals(-0.44334269937599835, optimum.getPoint()[0], 1.0e-2);
        assertEquals(0.5664082857640795, optimum.getPoint()[1], 1.0e-2);

        // Maximizing branch
        optimum = optimizer.optimize(fourExtrema, GoalType.MAXIMIZE, new double[] { 0.2, 0.2 });
        assertEquals(0.4990051026838, optimum.getValue(), 1.0e-4);
        assertEquals(0.932524733252112, optimum.getPoint()[0], 1.0e-2);
        assertEquals(-0.767192729553464, optimum.getPoint()[1], 1.0e-2);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testMaxIterationsExceeded() throws FunctionEvaluationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(1);
        optimizer.setMaxEvaluations(1000);

        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                return (point[0] - 100.0) * (point[0] - 100.0);
            }
        };

        try {
            optimizer.optimize(f, GoalType.MINIMIZE, new double[] { 0.0 });
            fail("Expected OptimizationException due to maximal iterations exceeded");
        } catch (OptimizationException e) {
            assertTrue(e.getMessage().length() > 0);
        }
    }

    @Test(timeout = 4000)
    public void testMaxEvaluationsExceeded() throws FunctionEvaluationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(500);
        optimizer.setMaxEvaluations(5);

        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                return point[0] * point[0] + point[1] * point[1];
            }
        };

        try {
            optimizer.optimize(f, GoalType.MINIMIZE, new double[] { 5.0, 5.0 });
            fail("Expected OptimizationException due to maximal evaluations exceeded");
        } catch (OptimizationException e) {
            assertTrue(e.getMessage().length() > 0);
        }
    }

    @Test(timeout = 4000)
    public void testFunctionEvaluationExceptionPropagation() throws OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        optimizer.setMaxIterations(100);
        optimizer.setMaxEvaluations(1000);

        MultivariateRealFunction failingFunction = new MultivariateRealFunction() {
            public double value(double[] point) throws FunctionEvaluationException {
                throw new FunctionEvaluationException(point, "Simulated evaluation failure");
            }
        };

        try {
            optimizer.optimize(failingFunction, GoalType.MINIMIZE, new double[] { 1.0 });
            fail("Expected FunctionEvaluationException to propagate");
        } catch (FunctionEvaluationException e) {
            assertEquals("Simulated evaluation failure", e.getMessage());
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDimensionMismatchInStartConfiguration()
            throws FunctionEvaluationException, OptimizationException {
        MultiDirectional optimizer = new MultiDirectional();
        // 2D start configuration steps
        optimizer.setStartConfiguration(new double[] { 0.1, 0.1 });

        MultivariateRealFunction f = new MultivariateRealFunction() {
            public double value(double[] point) {
                return point[0] * point[0];
            }
        };

        // 1D start point causes dimension mismatch with 2D configuration
        optimizer.optimize(f, GoalType.MINIMIZE, new double[] { 1.0 });
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Parameter Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCoefficientsIntegrityAndConvergenceCheckerState() {
        MultiDirectional opt1 = new MultiDirectional();
        assertEquals(Integer.MAX_VALUE, opt1.getMaxIterations());
        assertEquals(Integer.MAX_VALUE, opt1.getMaxEvaluations());

        opt1.setMaxIterations(77);
        assertEquals(77, opt1.getMaxIterations());

        opt1.setMaxEvaluations(88);
        assertEquals(88, opt1.getMaxEvaluations());

        SimpleScalarValueChecker checker = new SimpleScalarValueChecker(1.0e-3, 1.0e-6);
        opt1.setConvergenceChecker(checker);
        assertSame(checker, opt1.getConvergenceChecker());

        MultiDirectional opt2 = new MultiDirectional(2.5, 0.35);
        assertNotNull(opt2);
    }
}