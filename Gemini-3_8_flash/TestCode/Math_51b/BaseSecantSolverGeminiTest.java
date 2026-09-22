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
import org.apache.commons.math.exception.NoBracketingException;
import org.apache.commons.math.exception.NumberIsTooLargeException;
import org.apache.commons.math.exception.TooManyEvaluationsException;
import org.apache.commons.math.util.FastMath;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Decision Points & Edge Conditions:
 * 1. Initial Boundary Root Checking:
 *    - Branch: f0 == 0.0 (return x0 immediately)
 *    - Branch: f1 == 0.0 (return x1 immediately)
 * 2. Intermediate Exact Root Finding:
 *    - Branch: fx == 0.0 (return x immediately)
 * 3. Interval Updates & Inversion Tracking:
 *    - Branch: (f1 * fx < 0) -> Interval inversion (inverted = !inverted, x0 = x1, f0 = f1)
 *    - Branch: (f1 * fx >= 0) -> Method adjustments:
 *      * Method.ILLINOIS -> f0 *= 0.5
 *      * Method.PEGASUS -> f0 *= f1 / (f1 + fx)
 *      * Method.REGULA_FALSI -> no change / default fallback
 * 4. Function Value Accuracy Early Exit (|f1| <= ftol):
 *    - ANY_SIDE: returns x1 directly.
 *    - LEFT_SIDE: returns x1 if inverted, continues search if not inverted.
 *    - RIGHT_SIDE: returns x1 if !inverted, continues search if inverted.
 *    - BELOW_SIDE: returns x1 if f1 <= 0, continues search if f1 > 0.
 *    - ABOVE_SIDE: returns x1 if f1 >= 0, continues search if f1 < 0.
 * 5. Interval Convergence Threshold (|x1 - x0| < max(rtol * |x1|, atol)):
 *    - ANY_SIDE: returns x1.
 *    - LEFT_SIDE: inverted ? x1 : x0.
 *    - RIGHT_SIDE: inverted ? x0 : x1.
 *    - BELOW_SIDE: (f1 <= 0) ? x1 : x0.
 *    - ABOVE_SIDE: (f1 >= 0) ? x1 : x0.
 * 6. Constructors & Solve Overloads:
 *    - BaseSecantSolver(atol, method)
 *    - BaseSecantSolver(rtol, atol, method)
 *    - BaseSecantSolver(rtol, atol, ftol, method)
 *    - solve(maxEval, f, min, max, allowedSolution)
 *    - solve(maxEval, f, min, max, startValue, allowedSolution)
 *    - solve(maxEval, f, min, max, startValue)
 * 7. Defect Targeting (Defects4J Issue 631 / Math-50):
 *    - RegulaFalsiSolver stagnation causing TooManyEvaluationsException on f(x) = exp(x) - pi.
 */
public class BaseSecantSolverGeminiTest {

    /**
     * Concrete test implementation of BaseSecantSolver exposing all constructors
     * and methods for white-box branch coverage.
     */
    private static class TestSecantSolver extends BaseSecantSolver {
        public TestSecantSolver(final double absoluteAccuracy, final Method method) {
            super(absoluteAccuracy, method);
        }

        public TestSecantSolver(final double relativeAccuracy, final double absoluteAccuracy,
                                final Method method) {
            super(relativeAccuracy, absoluteAccuracy, method);
        }

        public TestSecantSolver(final double relativeAccuracy, final double absoluteAccuracy,
                                final double functionValueAccuracy, final Method method) {
            super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy, method);
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSolveWithMethodIllinois() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - x - 2.0; // root around 1.5213797
            }
        };

        final BaseSecantSolver solver = new TestSecantSolver(1e-14, 1e-10, BaseSecantSolver.Method.ILLINOIS);
        final double root = solver.solve(100, f, 1.0, 3.0);
        assertEquals(1.5213797068, root, 1e-7);
        assertTrue(solver.getEvaluations() > 0);
    }

    @Test(timeout = 4000)
    public void testSolveWithMethodPegasus() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - x - 2.0;
            }
        };

        final BaseSecantSolver solver = new TestSecantSolver(1e-14, 1e-10, BaseSecantSolver.Method.PEGASUS);
        final double root = solver.solve(100, f, 1.0, 3.0, 2.0);
        assertEquals(1.5213797068, root, 1e-7);
    }

    @Test(timeout = 4000)
    public void testSolveWithMethodRegulaFalsi() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 2.0 * x - 4.0;
            }
        };

        final BaseSecantSolver solver = new TestSecantSolver(1e-10, BaseSecantSolver.Method.REGULA_FALSI);
        final double root = solver.solve(100, f, 0.0, 5.0, AllowedSolution.ANY_SIDE);
        assertEquals(2.0, root, 1e-10);
    }

    @Test(timeout = 4000)
    public void testAllConstructorsCoverage() {
        final BaseSecantSolver solver1 = new TestSecantSolver(1e-8, BaseSecantSolver.Method.ILLINOIS);
        assertEquals(1e-8, solver1.getAbsoluteAccuracy(), 1e-15);

        final BaseSecantSolver solver2 = new TestSecantSolver(1e-9, 1e-8, BaseSecantSolver.Method.PEGASUS);
        assertEquals(1e-9, solver2.getRelativeAccuracy(), 1e-15);
        assertEquals(1e-8, solver2.getAbsoluteAccuracy(), 1e-15);

        final BaseSecantSolver solver3 = new TestSecantSolver(1e-9, 1e-8, 1e-7, BaseSecantSolver.Method.REGULA_FALSI);
        assertEquals(1e-9, solver3.getRelativeAccuracy(), 1e-15);
        assertEquals(1e-8, solver3.getAbsoluteAccuracy(), 1e-15);
        assertEquals(1e-7, solver3.getFunctionValueAccuracy(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSolveOverloadsDelegation() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 3.5;
            }
        };

        final BaseSecantSolver solver = new TestSecantSolver(1e-8, BaseSecantSolver.Method.ILLINOIS);

        // 1. solve(int, f, min, max, AllowedSolution)
        double r1 = solver.solve(100, f, 1.0, 5.0, AllowedSolution.ANY_SIDE);
        assertEquals(3.5, r1, 1e-7);

        // 2. solve(int, f, min, max, startValue, AllowedSolution)
        double r2 = solver.solve(100, f, 1.0, 5.0, 2.0, AllowedSolution.ANY_SIDE);
        assertEquals(3.5, r2, 1e-7);

        // 3. solve(int, f, min, max, startValue)
        double r3 = solver.solve(100, f, 1.0, 5.0, 2.5);
        assertEquals(3.5, r3, 1e-7);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Exact Hit Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialMinIsExactRoot() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 2.0;
            }
        };

        final BaseSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        // x0 = 2.0 is the exact root -> f0 == 0.0
        final double root = solver.solve(100, f, 2.0, 6.0);
        assertEquals(2.0, root, 1e-15);
        assertEquals(1, solver.getEvaluations());
    }

    @Test(timeout = 4000)
    public void testInitialMaxIsExactRoot() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 6.0;
            }
        };

        final BaseSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.PEGASUS);
        // x1 = 6.0 is the exact root -> f1 == 0.0
        final double root = solver.solve(100, f, 2.0, 6.0);
        assertEquals(6.0, root, 1e-15);
        assertEquals(2, solver.getEvaluations());
    }

    @Test(timeout = 4000)
    public void testIntermediateApproximationIsExactRoot() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 2.0 * x - 6.0;
            }
        };

        final BaseSecantSolver solver = new TestSecantSolver(1e-10, BaseSecantSolver.Method.ILLINOIS);
        // For linear function, first secant calculation hits exact root x = 3.0 -> fx == 0.0
        final double root = solver.solve(100, f, 1.0, 5.0);
        assertEquals(3.0, root, 1e-15);
        assertEquals(3, solver.getEvaluations());
    }

    // =========================================================================
    // Partition C: AllowedSolution Modes & Stopping Criteria Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testAllowedSolutionLeftSide() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0; // root sqrt(2) ~ 1.41421356
            }
        };

        final BaseSecantSolver solver = new TestSecantSolver(1e-14, 1e-8, BaseSecantSolver.Method.ILLINOIS);
        final double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.LEFT_SIDE);
        assertTrue("LEFT_SIDE solution must be <= exact root", root <= FastMath.sqrt(2.0));
        assertEquals(FastMath.sqrt(2.0), root, 1e-7);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionRightSide() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };

        final BaseSecantSolver solver = new TestSecantSolver(1e-14, 1e-8, BaseSecantSolver.Method.ILLINOIS);
        final double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.RIGHT_SIDE);
        assertTrue("RIGHT_SIDE solution must be >= exact root", root >= FastMath.sqrt(2.0));
        assertEquals(FastMath.sqrt(2.0), root, 1e-7);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionBelowSide() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };

        final BaseSecantSolver solver = new TestSecantSolver(1e-14, 1e-8, BaseSecantSolver.Method.PEGASUS);
        final double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.BELOW_SIDE);
        assertTrue("BELOW_SIDE solution must satisfy f(root) <= 0", f.value(root) <= 0.0);
        assertEquals(FastMath.sqrt(2.0), root, 1e-7);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionAboveSide() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };

        final BaseSecantSolver solver = new TestSecantSolver(1e-14, 1e-8, BaseSecantSolver.Method.PEGASUS);
        final double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.ABOVE_SIDE);
        assertTrue("ABOVE_SIDE solution must satisfy f(root) >= 0", f.value(root) >= 0.0);
        assertEquals(FastMath.sqrt(2.0), root, 1e-7);
    }

    @Test(timeout = 4000)
    public void testFastMathAbsF1LessThanFtolBranch() {
        // Force |f1| <= ftol exit by using loose function value accuracy
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };

        // ftol = 0.5, atol = 1e-15 -> triggers |f1| <= ftol condition first
        final BaseSecantSolver solverAny = new TestSecantSolver(1e-15, 1e-15, 0.5, BaseSecantSolver.Method.ILLINOIS);
        final double rootAny = solverAny.solve(100, f, 1.0, 3.0, AllowedSolution.ANY_SIDE);
        assertTrue(FastMath.abs(f.value(rootAny)) <= 0.5);

        final BaseSecantSolver solverBelow = new TestSecantSolver(1e-15, 1e-15, 0.5, BaseSecantSolver.Method.ILLINOIS);
        final double rootBelow = solverBelow.solve(100, f, 1.0, 3.0, AllowedSolution.BELOW_SIDE);
        assertTrue(f.value(rootBelow) <= 0.0);

        final BaseSecantSolver solverAbove = new TestSecantSolver(1e-15, 1e-15, 0.5, BaseSecantSolver.Method.ILLINOIS);
        final double rootAbove = solverAbove.solve(100, f, 1.0, 3.0, AllowedSolution.ABOVE_SIDE);
        assertTrue(f.value(rootAbove) >= 0.0);
    }

    @Test(timeout = 4000)
    public void testIntervalInversionLeftAndRightSide() {
        // Decreasing function: f(1) = 2, f(3) = -2 (f0 > 0, f1 < 0)
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return -x * x * x + 8.0; // root at 2.0
            }
        };

        final BaseSecantSolver solverLeft = new TestSecantSolver(1e-14, 1e-8, BaseSecantSolver.Method.ILLINOIS);
        final double rootLeft = solverLeft.solve(100, f, 1.0, 3.0, AllowedSolution.LEFT_SIDE);
        assertTrue(rootLeft <= 2.0);

        final BaseSecantSolver solverRight = new TestSecantSolver(1e-14, 1e-8, BaseSecantSolver.Method.ILLINOIS);
        final double rootRight = solverRight.solve(100, f, 1.0, 3.0, AllowedSolution.RIGHT_SIDE);
        assertTrue(rootRight >= 2.0);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NoBracketingException.class, timeout = 4000)
    public void testNoBracketingThrowsException() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0; // Always strictly positive
            }
        };

        final BaseSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        solver.solve(50, f, 1.0, 5.0);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testMinGreaterThanMaxThrowsException() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };

        final BaseSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.PEGASUS);
        solver.solve(50, f, 5.0, 1.0);
    }

    @Test(expected = TooManyEvaluationsException.class, timeout = 4000)
    public void testMaxEvaluationsExceededThrowsException() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };

        final BaseSecantSolver solver = new TestSecantSolver(1e-15, 1e-15, BaseSecantSolver.Method.ILLINOIS);
        // Exceed count with very low maxEval limit
        solver.solve(2, f, 3.0, 4.0);
    }

    // =========================================================================
    // Partition E: Defect-Targeted Zone (Defects4J Issue 631)
    // =========================================================================

    /**
     * Dedicated test targeting Defect 631 / Math-50:
     * In the standard Regula Falsi algorithm, one bound may remain stagnant
     * while the other slowly creeps towards the root, resulting in linear convergence
     * that exhausts evaluations (TooManyEvaluationsException).
     */
    @Test(timeout = 4000)
    public void testIssue631() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.exp(x) - FastMath.PI;
            }
        };

        final RegulaFalsiSolver solver = new RegulaFalsiSolver();
        final double root = solver.solve(3624, f, 1.0, 10.0);
        assertEquals(FastMath.log(FastMath.PI), root, 1e-6);
    }
}