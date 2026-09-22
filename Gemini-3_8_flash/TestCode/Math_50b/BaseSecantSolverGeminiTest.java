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
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.TooManyEvaluationsException;
import org.apache.commons.math.util.FastMath;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: BaseSecantSolver (and concrete subclasses: RegulaFalsiSolver, IllinoisSolver, PegasusSolver)
 *
 * Decision / Condition Matrix:
 * 1. Initial Root Check:
 *    - f(x0) == 0.0 -> returns x0 immediately (Branch Covered)
 *    - f(x1) == 0.0 -> returns x1 immediately (Branch Covered)
 * 2. Exact Secant Hit:
 *    - f(x) == 0.0 -> returns x immediately (Branch Covered)
 * 3. Bound Update:
 *    - f(x1) * f(x) < 0 -> invert bounds, inverted = !inverted (Branch Covered)
 *    - f(x1) * f(x) >= 0:
 *      - Method.ILLINOIS -> f0 *= 0.5 (Branch Covered)
 *      - Method.PEGASUS -> f0 *= f1 / (f1 + fx) (Branch Covered)
 *      - Method.REGULA_FALSI -> x == x1 adjustments (Branch Covered)
 * 4. ftol Stopping Criterion (|f1| <= ftol):
 *    - ANY_SIDE -> returns x1
 *    - LEFT_SIDE -> returns x1 if inverted, else continue
 *    - RIGHT_SIDE -> returns x1 if !inverted, else continue
 *    - BELOW_SIDE -> returns x1 if f1 <= 0, else continue
 *    - ABOVE_SIDE -> returns x1 if f1 >= 0, else continue
 * 5. Interval Accuracy Stopping Criterion (|x1 - x0| < max(rtol * |x1|, atol)):
 *    - ANY_SIDE -> returns x1
 *    - LEFT_SIDE -> returns inverted ? x1 : x0
 *    - RIGHT_SIDE -> returns inverted ? x0 : x1
 *    - BELOW_SIDE -> returns (f1 <= 0) ? x1 : x0
 *    - ABOVE_SIDE -> returns (f1 >= 0) ? x1 : x0
 * 6. Defects4J Known Failure (testIssue631):
 *    - RegulaFalsi stalling with slow convergence exceeding evaluation quota,
 *      asserting expected TooManyEvaluationsException.
 */
public class BaseSecantSolverGeminiTest {

    /**
     * Concrete test implementation of BaseSecantSolver to test protected constructors.
     */
    private static class DummySecantSolver extends BaseSecantSolver {
        DummySecantSolver(final double absoluteAccuracy, final Method method) {
            super(absoluteAccuracy, method);
        }

        DummySecantSolver(final double relativeAccuracy, final double absoluteAccuracy, final Method method) {
            super(relativeAccuracy, absoluteAccuracy, method);
        }

        DummySecantSolver(final double relativeAccuracy, final double absoluteAccuracy,
                          final double functionValueAccuracy, final Method method) {
            super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy, method);
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testProtectedConstructorsAndSolveOverloads() {
        // Constructor 1: absoluteAccuracy, method
        DummySecantSolver solver1 = new DummySecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        assertEquals(1e-6, solver1.getAbsoluteAccuracy(), 1e-15);

        // Constructor 2: relativeAccuracy, absoluteAccuracy, method
        DummySecantSolver solver2 = new DummySecantSolver(1e-12, 1e-6, BaseSecantSolver.Method.PEGASUS);
        assertEquals(1e-12, solver2.getRelativeAccuracy(), 1e-15);
        assertEquals(1e-6, solver2.getAbsoluteAccuracy(), 1e-15);

        // Constructor 3: relativeAccuracy, absoluteAccuracy, functionValueAccuracy, method
        DummySecantSolver solver3 = new DummySecantSolver(1e-12, 1e-6, 1e-10, BaseSecantSolver.Method.REGULA_FALSI);
        assertEquals(1e-12, solver3.getRelativeAccuracy(), 1e-15);
        assertEquals(1e-6, solver3.getAbsoluteAccuracy(), 1e-15);
        assertEquals(1e-10, solver3.getFunctionValueAccuracy(), 1e-15);

        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };

        // solve(maxEval, f, min, max, allowedSolution)
        double root1 = solver1.solve(100, f, 1.0, 3.0, AllowedSolution.ANY_SIDE);
        assertEquals(2.0, root1, 1e-5);

        // solve(maxEval, f, min, max, startValue, allowedSolution)
        double root2 = solver2.solve(100, f, 1.0, 3.0, 1.5, AllowedSolution.ANY_SIDE);
        assertEquals(2.0, root2, 1e-5);

        // solve(maxEval, f, min, max, startValue)
        double root3 = solver3.solve(100, f, 1.0, 3.0, 2.5);
        assertEquals(2.0, root3, 1e-5);
    }

    @Test(timeout = 4000)
    public void testIllinoisMethodConvergence() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };
        IllinoisSolver solver = new IllinoisSolver(1e-10, 1e-10);
        double root = solver.solve(100, f, 3.0, 4.0);
        assertEquals(FastMath.PI, root, 1e-9);
    }

    @Test(timeout = 4000)
    public void testPegasusMethodConvergence() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.exp(x) - 3.0;
            }
        };
        PegasusSolver solver = new PegasusSolver(1e-10, 1e-10);
        double root = solver.solve(100, f, 0.0, 2.0);
        assertEquals(FastMath.log(3.0), root, 1e-9);
    }

    @Test(timeout = 4000)
    public void testRegulaFalsiMethodConvergence() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - 27.0;
            }
        };
        RegulaFalsiSolver solver = new RegulaFalsiSolver(1e-8, 1e-8);
        double root = solver.solve(100, f, 2.0, 4.0);
        assertEquals(3.0, root, 1e-7);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Immediate Root Detection
    // =========================================================================

    @Test(timeout = 4000)
    public void testRootAtMinBoundary() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 2.0) * (x - 5.0);
            }
        };
        PegasusSolver solver = new PegasusSolver(1e-6);
        // Root is exactly at min = 2.0
        double root = solver.solve(50, f, 2.0, 4.0, AllowedSolution.RIGHT_SIDE);
        assertEquals(2.0, root, 1e-15);
    }

    @Test(timeout = 4000)
    public void testRootAtMaxBoundary() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 2.0) * (x - 5.0);
            }
        };
        IllinoisSolver solver = new IllinoisSolver(1e-6);
        // Root is exactly at max = 5.0
        double root = solver.solve(50, f, 3.0, 5.0, AllowedSolution.LEFT_SIDE);
        assertEquals(5.0, root, 1e-15);
    }

    @Test(timeout = 4000)
    public void testExactRootHitInSecantStep() {
        // Linear function where secant step hits the exact zero on the very first approximation
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 2.0 * x - 6.0;
            }
        };
        RegulaFalsiSolver solver = new RegulaFalsiSolver(1e-8);
        double root = solver.solve(50, f, 1.0, 5.0, AllowedSolution.BELOW_SIDE);
        assertEquals(3.0, root, 1e-15);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-631)
    // =========================================================================

    /**
     * Targets Defects4J known issue (MATH-631).
     * On defective versions of RegulaFalsiSolver/BaseSecantSolver, this specific
     * equation and bound setup causes slow convergence that triggers TooManyEvaluationsException.
     */
    @Test(expected = TooManyEvaluationsException.class, timeout = 4000)
    public void testIssue631() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.exp(x) - FastMath.pow(FastMath.PI, 3.0);
            }
        };

        final UnivariateRealSolver solver = new RegulaFalsiSolver();
        solver.solve(3624, f, 1.0, 10.0);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NoBracketingException.class, timeout = 4000)
    public void testNoBracketingExceptionSameSignPositive() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };
        IllinoisSolver solver = new IllinoisSolver();
        solver.solve(100, f, 1.0, 2.0);
    }

    @Test(expected = NoBracketingException.class, timeout = 4000)
    public void testNoBracketingExceptionSameSignNegative() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return -(x * x + 1.0);
            }
        };
        PegasusSolver solver = new PegasusSolver();
        solver.solve(100, f, -3.0, -1.0);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testMinGreaterThanMax() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        RegulaFalsiSolver solver = new RegulaFalsiSolver();
        solver.solve(100, f, 2.0, 1.0);
    }

    @Test(expected = TooManyEvaluationsException.class, timeout = 4000)
    public void testTooFewAllowedEvaluations() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };
        IllinoisSolver solver = new IllinoisSolver();
        // 2 evaluations are consumed immediately by min and max
        solver.solve(2, f, 3.0, 4.0);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testNullFunctionThrowsException() {
        PegasusSolver solver = new PegasusSolver();
        solver.solve(100, null, 1.0, 2.0);
    }

    // =========================================================================
    // Partition E: AllowedSolution Systematic Branch Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testAllowedSolutionLeftSideIncreasingFunction() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };
        PegasusSolver solver = new PegasusSolver(1e-6, 1e-6);
        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.LEFT_SIDE);
        assertTrue("Root should be <= exact root for LEFT_SIDE", root <= FastMath.sqrt(2.0));
        assertEquals(FastMath.sqrt(2.0), root, 1e-5);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionRightSideIncreasingFunction() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };
        PegasusSolver solver = new PegasusSolver(1e-6, 1e-6);
        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.RIGHT_SIDE);
        assertTrue("Root should be >= exact root for RIGHT_SIDE", root >= FastMath.sqrt(2.0));
        assertEquals(FastMath.sqrt(2.0), root, 1e-5);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionBelowSideIncreasingFunction() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };
        IllinoisSolver solver = new IllinoisSolver(1e-6, 1e-6);
        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.BELOW_SIDE);
        assertTrue("Function value must be <= 0 for BELOW_SIDE", f.value(root) <= 0.0);
        assertEquals(FastMath.sqrt(2.0), root, 1e-5);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionAboveSideIncreasingFunction() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };
        IllinoisSolver solver = new IllinoisSolver(1e-6, 1e-6);
        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.ABOVE_SIDE);
        assertTrue("Function value must be >= 0 for ABOVE_SIDE", f.value(root) >= 0.0);
        assertEquals(FastMath.sqrt(2.0), root, 1e-5);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionDecreasingFunction() {
        // Strictly decreasing function: root at x = 3.0
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return 3.0 - x;
            }
        };
        DummySecantSolver solver = new DummySecantSolver(1e-6, 1e-6, BaseSecantSolver.Method.PEGASUS);

        double rootBelow = solver.solve(100, f, 1.0, 5.0, AllowedSolution.BELOW_SIDE);
        assertTrue(f.value(rootBelow) <= 0.0);

        double rootAbove = solver.solve(100, f, 1.0, 5.0, AllowedSolution.ABOVE_SIDE);
        assertTrue(f.value(rootAbove) >= 0.0);

        double rootLeft = solver.solve(100, f, 1.0, 5.0, AllowedSolution.LEFT_SIDE);
        assertTrue(rootLeft <= 3.0);

        double rootRight = solver.solve(100, f, 1.0, 5.0, AllowedSolution.RIGHT_SIDE);
        assertTrue(rootRight >= 3.0);
    }

    @Test(timeout = 4000)
    public void testFunctionToleranceEarlyStopping() {
        // Using large ftol to force early exit at |f1| <= ftol branch
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - 8.0;
            }
        };
        DummySecantSolver solver = new DummySecantSolver(1e-14, 1e-14, 0.5, BaseSecantSolver.Method.ILLINOIS);
        double root = solver.solve(100, f, 1.0, 3.0, AllowedSolution.ANY_SIDE);
        assertTrue(FastMath.abs(f.value(root)) <= 0.5);
    }
}