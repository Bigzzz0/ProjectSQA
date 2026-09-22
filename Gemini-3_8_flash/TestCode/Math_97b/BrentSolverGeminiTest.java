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
package org.apache.commons.math.analysis;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.math.analysis.BrentSolver
 *
 * Method Under Test: solve(double min, double max)
 *   - Decision: verifyInterval(min, max) -> min >= max throws IllegalArgumentException.
 *   - Decision: sign = yMin * yMax >= 0
 *       * Branch TRUE: endpoints have same sign or zero.
 *         [DEFECT TARGET]: In defective versions, if an endpoint is an exact or near root (|y| <= accuracy),
 *         BrentSolver prematurely throws IllegalArgumentException instead of returning the root.
 *       * Branch FALSE: endpoints bracket a root -> calls solve(min, yMin, max, yMax, min, yMin).
 *
 * Method Under Test: solve(double min, double max, double initial)
 *   - Decision: ((initial - min) * (max - initial)) < 0 -> throws IllegalArgumentException.
 *   - Decision: Math.abs(yInitial) <= functionValueAccuracy -> early return initial.
 *   - Decision: Math.abs(yMin) <= functionValueAccuracy -> early return yMin/min root.
 *   - Decision: yInitial * yMin < 0 -> bracketed in [min, initial] -> solve(min, yMin, initial, ...).
 *   - Decision: Math.abs(yMax) <= functionValueAccuracy -> early return yMax/max root.
 *   - Decision: yInitial * yMax < 0 -> bracketed in [initial, max] -> solve(initial, yInitial, max, ...).
 *   - Fallthrough: solve(min, yMin, max, yMax, initial, yInitial).
 *
 * Method Under Test: solve(x0, y0, x1, y1, x2, y2) [Internal Brent Core]
 *   - Branch: |y2| < |y1| -> swap x0,x1,x2 and y0,y1,y2.
 *   - Branch: |y1| <= functionValueAccuracy -> return x1.
 *   - Branch: |dx| <= tolerance -> return x1.
 *   - Branch: |oldDelta| < tolerance || |y0| <= |y1| -> Force bisection.
 *   - Branch: x0 == x2 (Linear interpolation) vs x0 != x2 (Inverse quadratic interpolation).
 *   - Branch: p > 0.0 -> invert p1 vs p <= 0.0 -> invert p.
 *   - Branch: 2*p >= 1.5*dx*p1 - |tol*p1| || p >= |0.5*oldDelta*p1| -> fallback bisection vs accept step.
 *   - Branch: |delta| > tolerance vs dx > 0.0 (add half tol) vs dx <= 0.0 (sub half tol).
 *   - Branch: (y1 > 0) == (y2 > 0) -> update x2 = x0.
 *   - Branch: iteration count reaches maximalIterationCount -> throws MaxIterationsExceededException.
 * ------------------------------------------------------------------------------------------------------
 */
public class BrentSolverGeminiTest {

    private static final double DEFAULT_ABSOLUTE_ACCURACY = 1E-6;

    // =========================================================================
    // Partition A: Core Functional Logic & Standard Operational Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testSolveQuinticFunctionStandard() throws Exception {
        // f(x) = (x - 1)(x - 2)(x - 3)(x + 2)(x + 3)
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 2.0) * (x - 3.0) * (x + 2.0) * (x + 3.0);
            }
        };
        BrentSolver solver = new BrentSolver(f);

        // Root at x = 1.0 within interval [0.5, 1.5]
        double result = solver.solve(0.5, 1.5);
        assertEquals(1.0, result, DEFAULT_ABSOLUTE_ACCURACY);
        assertTrue(solver.getIterationCount() > 0);

        // Root at x = -2.0 within interval [-2.5, -1.5]
        result = solver.solve(-2.5, -1.5);
        assertEquals(-2.0, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    @Test(timeout = 4000)
    public void testSolveSinFunctionWithInitialGuess() throws Exception {
        UnivariateRealFunction sin = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        BrentSolver solver = new BrentSolver(sin);

        // Root at Pi (~3.14159265) bracketed by [3.0, 4.0] with initial guess 3.1
        double result = solver.solve(3.0, 4.0, 3.1);
        assertEquals(Math.PI, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    @Test(timeout = 4000)
    public void testSolveInitialGuessIsExactRoot() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 2.5;
            }
        };
        BrentSolver solver = new BrentSolver(f);

        double result = solver.solve(1.0, 4.0, 2.5);
        assertEquals(2.5, result, 1E-15);
        assertEquals(0, solver.getIterationCount());
    }

    @Test(timeout = 4000)
    public void testSolveInitialBracketWithMin() throws Exception {
        // Root at 0.5, interval [0.0, 2.0], initial guess = 1.0
        // f(0.0) = -0.5, f(1.0) = 0.5 -> initial and min bracket the root
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 0.5;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(0.0, 2.0, 1.0);
        assertEquals(0.5, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    @Test(timeout = 4000)
    public void testSolveInitialBracketWithMax() throws Exception {
        // Root at 1.5, interval [0.0, 2.0], initial guess = 1.0
        // f(1.0) = -0.5, f(2.0) = 0.5 -> initial and max bracket the root
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.5;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(0.0, 2.0, 1.0);
        assertEquals(1.5, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    @Test(timeout = 4000)
    public void testSolveLinearInterpolationPath() throws Exception {
        // Strictly linear function ensures linear interpolation branch is triggered
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 3.0 * x - 7.5;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(1.0, 4.0);
        assertEquals(2.5, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    @Test(timeout = 4000)
    public void testSolveInverseQuadraticInterpolationPath() throws Exception {
        // Non-linear function with varying curvature forcing inverse quadratic interpolation steps
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.exp(x) - 3.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(0.0, 2.0);
        assertEquals(Math.log(3.0), result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Numerical Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSolveNearZeroIntervalTolerance() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1E-8;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        solver.setAbsoluteAccuracy(1E-9);
        double result = solver.solve(-1.0, 1.0);
        assertEquals(1E-8, result, 1E-9);
    }

    @Test(timeout = 4000)
    public void testSolveLargeBoundaries() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 100000.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(0.0, 1000000.0, 50000.0);
        assertEquals(100000.0, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    @Test(timeout = 4000)
    public void testSolveNegativeEndpoints() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x + 50.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(-100.0, -10.0);
        assertEquals(-50.0, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGET DEFECT: When one of the endpoints is a root (f(max) == 0 or f(min) == 0),
     * yMin * yMax can be >= 0 (often 0.0 or slightly positive due to rounding).
     * The defective solve(min, max) throws IllegalArgumentException instead of returning the root.
     */
    @Test(timeout = 4000)
    public void testRootAtMaxEndpoint() throws Exception {
        UnivariateRealFunction sin = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        BrentSolver solver = new BrentSolver(sin);

        // Target defect: Math.sin(Math.PI) is approx 1.22e-16 > 0, Math.sin(3.0) > 0.
        // Product is >= 0, but Math.PI is a root within functionValueAccuracy.
        double result = solver.solve(3.0, Math.PI);
        assertEquals(Math.PI, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    @Test(timeout = 4000)
    public void testRootAtMinEndpoint() throws Exception {
        UnivariateRealFunction sin = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        BrentSolver solver = new BrentSolver(sin);

        // Target defect: Root exactly at min endpoint (0.0)
        double result = solver.solve(0.0, 1.0);
        assertEquals(0.0, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    @Test(timeout = 4000)
    public void testRootAtEndpointWithInitialGuess() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 2.0 * x - 4.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);

        // Root is at min endpoint x = 2.0
        double result = solver.solve(2.0, 5.0, 3.5);
        assertEquals(2.0, result, DEFAULT_ABSOLUTE_ACCURACY);

        // Root is at max endpoint x = 2.0
        result = solver.solve(0.0, 2.0, 1.0);
        assertEquals(2.0, result, DEFAULT_ABSOLUTE_ACCURACY);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveEndpointsDoNotBracket() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0; // Strictly positive everywhere
            }
        };
        BrentSolver solver = new BrentSolver(f);
        solver.solve(1.0, 5.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveInvalidIntervalMinGreaterThanMax() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        solver.solve(5.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveInvalidIntervalMinEqualsMax() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        solver.solve(2.0, 2.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveInitialGuessOutOfRangeLow() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        // Initial 0.5 is outside [1.0, 3.0]
        solver.solve(1.0, 3.0, 0.5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveInitialGuessOutOfRangeHigh() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        // Initial 3.5 is outside [1.0, 3.0]
        solver.solve(1.0, 3.0, 3.5);
    }

    @Test(expected = MaxIterationsExceededException.class, timeout = 4000)
    public void testMaxIterationsExceeded() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.pow(x - 1.0, 9);
            }
        };
        BrentSolver solver = new BrentSolver(f);
        solver.setMaximalIterationCount(2); // Artificially low limit
        solver.solve(0.0, 2.0);
    }

    @Test(expected = FunctionEvaluationException.class, timeout = 4000)
    public void testFunctionEvaluationExceptionPropagated() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) throws FunctionEvaluationException {
                throw new FunctionEvaluationException(x, "Simulated evaluation failure");
            }
        };
        BrentSolver solver = new BrentSolver(f);
        solver.solve(-1.0, 1.0);
    }

    // =========================================================================
    // Partition E: Solver Configuration & State Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSolverDefaultConfigurations() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        BrentSolver solver = new BrentSolver(f);

        assertEquals(100, solver.getMaximalIterationCount());
        assertEquals(1E-6, solver.getAbsoluteAccuracy(), 1E-12);
        assertEquals(1E-14, solver.getRelativeAccuracy(), 1E-16);
    }

    @Test(timeout = 4000)
    public void testResultResetBetweenRuns() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);

        double root1 = solver.solve(0.0, 2.0);
        assertEquals(1.0, root1, DEFAULT_ABSOLUTE_ACCURACY);
        int it1 = solver.getIterationCount();
        assertTrue(it1 > 0);

        // Run second solve on same instance with initial guess
        double root2 = solver.solve(0.5, 1.5, 1.0);
        assertEquals(1.0, root2, DEFAULT_ABSOLUTE_ACCURACY);
        assertEquals(0, solver.getIterationCount());
    }
}