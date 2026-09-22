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

package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.ConvergenceException;
import org.apache.commons.math.exception.NoBracketingException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.NumberIsTooLargeException;
import org.apache.commons.math.util.FastMath;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: BaseSecantSolver
 * Defect Under Analysis: RegulaFalsiSolverTest::testIssue631 (MATH-631)
 *
 * Branch & Condition Coverage Matrix:
 * 1. Initial Root Check:
 *    - f(x0) == 0.0 -> returns x0 immediately (both inverted / non-inverted).
 *    - f(x1) == 0.0 -> returns x1 immediately.
 * 2. Intermediate Exact Root Check:
 *    - fx == 0.0 -> returns x immediately regardless of AllowedSolution.
 * 3. Update Bounds Branching (f1 * fx < 0):
 *    - True (Sign change): x0 = x1; f0 = f1; inverted = !inverted.
 *    - False (Same sign):
 *      - Method.ILLINOIS -> f0 *= 0.5
 *      - Method.PEGASUS -> f0 *= f1 / (f1 + fx)
 *      - Method.REGULA_FALSI -> defect zone: failure to detect stagnation -> infinite loop / TooManyEvaluationsException
 * 4. Tolerance Threshold 1: Function Value Accuracy (FastMath.abs(f1) <= ftol):
 *    - ANY_SIDE -> returns x1
 *    - LEFT_SIDE -> returns x1 if inverted; continues loop if not inverted
 *    - RIGHT_SIDE -> returns x1 if !inverted; continues loop if inverted
 *    - BELOW_SIDE -> returns x1 if f1 <= 0; continues loop if f1 > 0
 *    - ABOVE_SIDE -> returns x1 if f1 >= 0; continues loop if f1 < 0
 * 5. Tolerance Threshold 2: Interval Accuracy (FastMath.abs(x1 - x0) < max(rtol * |x1|, atol)):
 *    - ANY_SIDE -> returns x1
 *    - LEFT_SIDE -> returns inverted ? x1 : x0
 *    - RIGHT_SIDE -> returns inverted ? x0 : x1
 *    - BELOW_SIDE -> returns (f1 <= 0) ? x1 : x0
 *    - ABOVE_SIDE -> returns (f1 >= 0) ? x1 : x0
 * 6. Constructors & Overloaded Methods:
 *    - 2-arg (absoluteAccuracy, method)
 *    - 3-arg (relativeAccuracy, absoluteAccuracy, method)
 *    - 4-arg (relativeAccuracy, absoluteAccuracy, functionValueAccuracy, method)
 *    - solve(maxEval, f, min, max, allowedSolution)
 *    - solve(maxEval, f, min, max, startValue, allowedSolution)
 *    - solve(maxEval, f, min, max, startValue)
 */
public class BaseSecantSolverGeminiTest {

    /** Concrete test harness exposing BaseSecantSolver constructors. */
    private static class ConcreteSecantSolver extends BaseSecantSolver {
        public ConcreteSecantSolver(final double absoluteAccuracy, final Method method) {
            super(absoluteAccuracy, method);
        }

        public ConcreteSecantSolver(final double relativeAccuracy, final double absoluteAccuracy, final Method method) {
            super(relativeAccuracy, absoluteAccuracy, method);
        }

        public ConcreteSecantSolver(final double relativeAccuracy, final double absoluteAccuracy,
                                    final double functionValueAccuracy, final Method method) {
            super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy, method);
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSolveExactInitialBounds() {
        // Test f0 == 0.0 branch
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * (x - 4.0);
            }
        };

        double rootMin = solver.solve(100, f, 0.0, 3.0, AllowedSolution.ANY_SIDE);
        assertEquals(0.0, rootMin, 1e-12);

        // Test f1 == 0.0 branch
        double rootMax = solver.solve(100, f, 1.0, 4.0, AllowedSolution.ANY_SIDE);
        assertEquals(4.0, rootMax, 1e-12);
    }

    @Test(timeout = 4000)
    public void testSolveIntermediateExactZero() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        // Function where secant step hits root 2.0 exactly on symmetric interval [-2.0, 6.0]
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 2.0 * x - 4.0;
            }
        };

        double root = solver.solve(100, f, 0.0, 5.0, AllowedSolution.ANY_SIDE);
        assertEquals(2.0, root, 1e-12);
    }

    @Test(timeout = 4000)
    public void testMethodPegasusConvergence() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-10, 1e-10, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };

        double root = solver.solve(100, f, 3.0, 4.0, 3.5, AllowedSolution.ANY_SIDE);
        assertEquals(FastMath.PI, root, 1e-9);
    }

    @Test(timeout = 4000)
    public void testMethodIllinoisConvergence() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-10, 1e-10, 1e-14, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - 2.0 * x - 5.0;
            }
        };

        // Root is approx 2.0945514815423265
        double root = solver.solve(100, f, 1.0, 3.0);
        assertEquals(2.0945514815423265, root, 1e-6);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionSides() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-14, 1e-8, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0; // root at sqrt(2)
            }
        };
        double sqrt2 = FastMath.sqrt(2.0);

        double rootLeft = solver.solve(100, f, 1.0, 2.0, AllowedSolution.LEFT_SIDE);
        assertTrue(rootLeft <= sqrt2);

        double rootRight = solver.solve(100, f, 1.0, 2.0, AllowedSolution.RIGHT_SIDE);
        assertTrue(rootRight >= sqrt2);

        double rootBelow = solver.solve(100, f, 1.0, 2.0, AllowedSolution.BELOW_SIDE);
        assertTrue(f.value(rootBelow) <= 0.0);

        double rootAbove = solver.solve(100, f, 1.0, 2.0, AllowedSolution.ABOVE_SIDE);
        assertTrue(f.value(rootAbove) >= 0.0);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFunctionValueAccuracyTerminationBranches() {
        // Loose ftol forces loop exit via FastMath.abs(f1) <= ftol
        final double ftol = 0.5;
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-14, 1e-14, ftol, BaseSecantSolver.Method.ILLINOIS);

        UnivariateRealFunction fAscending = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 2.0;
            }
        };

        double rootAny = solver.solve(100, fAscending, 1.0, 3.0, AllowedSolution.ANY_SIDE);
        assertTrue(FastMath.abs(fAscending.value(rootAny)) <= ftol);

        double rootBelow = solver.solve(100, fAscending, 1.0, 3.0, AllowedSolution.BELOW_SIDE);
        assertTrue(fAscending.value(rootBelow) <= 0.0);

        double rootAbove = solver.solve(100, fAscending, 1.0, 3.0, AllowedSolution.ABOVE_SIDE);
        assertTrue(fAscending.value(rootAbove) >= 0.0);

        double rootLeft = solver.solve(100, fAscending, 1.0, 3.0, AllowedSolution.LEFT_SIDE);
        assertTrue(rootLeft <= 2.0);

        double rootRight = solver.solve(100, fAscending, 1.0, 3.0, AllowedSolution.RIGHT_SIDE);
        assertTrue(rootRight >= 2.0);
    }

    @Test(timeout = 4000)
    public void testIntervalToleranceAllowedSolutionsInverted() {
        // High ftol precision so termination relies purely on interval convergence
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-10, 1e-4, 1e-15, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction fDescending = new UnivariateRealFunction() {
            public double value(double x) {
                return -x + 3.0; // root at 3.0, negative slope causes inverted interval paths
            }
        };

        double rootLeft = solver.solve(100, fDescending, 2.0, 4.0, AllowedSolution.LEFT_SIDE);
        assertTrue(rootLeft <= 3.0);

        double rootRight = solver.solve(100, fDescending, 2.0, 4.0, AllowedSolution.RIGHT_SIDE);
        assertTrue(rootRight >= 3.0);

        double rootBelow = solver.solve(100, fDescending, 2.0, 4.0, AllowedSolution.BELOW_SIDE);
        assertTrue(fDescending.value(rootBelow) <= 0.0);

        double rootAbove = solver.solve(100, fDescending, 2.0, 4.0, AllowedSolution.ABOVE_SIDE);
        assertTrue(fDescending.value(rootAbove) >= 0.0);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-631)
    // =========================================================================

    /**
     * Targets Defects4J known issue MATH-631:
     * Regula Falsi solver gets stuck on flat/exponential curves when x approaches x1.
     * The defective version continues cycling until TooManyEvaluationsException,
     * whereas the intended behavior expects a ConvergenceException upon stagnation.
     */
    @Test(expected = ConvergenceException.class, timeout = 4000)
    public void testIssue631RegulaFalsiConvergenceFailure() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.exp(x) - FastMath.pow(FastMath.PI, 3.0);
            }
        };

        final UnivariateRealSolver solver = new RegulaFalsiSolver();
        // Evaluating this equation triggers the stagnation condition in Regula Falsi
        solver.solve(3624, f, 1.0, 10.0);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NoBracketingException.class, timeout = 4000)
    public void testNoBracketingExceptionThrown() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0; // Strictly positive, cannot bracket 0
            }
        };

        solver.solve(100, f, 1.0, 5.0, AllowedSolution.ANY_SIDE);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testMinGreaterThanMaxThrowsException() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };

        solver.solve(100, f, 5.0, 1.0, AllowedSolution.ANY_SIDE);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testNullFunctionThrowsException() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        solver.solve(100, null, 1.0, 5.0, AllowedSolution.ANY_SIDE);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorParameterPassing() {
        double relAcc = 1e-8;
        double absAcc = 1e-6;
        double fAcc = 1e-12;

        BaseSecantSolver solver1 = new ConcreteSecantSolver(absAcc, BaseSecantSolver.Method.ILLINOIS);
        assertEquals(absAcc, solver1.getAbsoluteAccuracy(), 1e-15);
        assertEquals(1e-14, solver1.getRelativeAccuracy(), 1e-15);

        BaseSecantSolver solver2 = new ConcreteSecantSolver(relAcc, absAcc, BaseSecantSolver.Method.PEGASUS);
        assertEquals(relAcc, solver2.getRelativeAccuracy(), 1e-15);
        assertEquals(absAcc, solver2.getAbsoluteAccuracy(), 1e-15);

        BaseSecantSolver solver3 = new ConcreteSecantSolver(relAcc, absAcc, fAcc, BaseSecantSolver.Method.REGULA_FALSI);
        assertEquals(relAcc, solver3.getRelativeAccuracy(), 1e-15);
        assertEquals(absAcc, solver3.getAbsoluteAccuracy(), 1e-15);
        assertEquals(fAcc, solver3.getFunctionValueAccuracy(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDefaultAllowedSolutionIsAnySide() {
        BaseSecantSolver solver = new ConcreteSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 3.5;
            }
        };

        // Calling solve without explicit AllowedSolution should default to ANY_SIDE
        double root = solver.solve(50, f, 2.0, 5.0, 3.0);
        assertEquals(3.5, root, 1e-5);
    }
}