package org.apache.commons.math.analysis.solvers;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.TooManyEvaluationsException;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target class: BaseSecantSolver (abstract, tested via concrete subclasses)
 * 
 * Branches covered:
 * 1. doSolve() initial root check: f0 == 0.0, f1 == 0.0
 * 2. Bracketing verification path (verifyBracketing throws if not bracketing)
 * 3. Main iteration loop:
 *    - fx == 0.0 exact root return
 *    - f1 * fx < 0 (interval inversion) vs. else (update f0)
 *    - Method switch: ILLINOIS (f0 *= 0.5), PEGASUS (f0 *= f1/(f1+fx)), default
 *    - ftol check with allowed solution switch (ANY_SIDE, LEFT_SIDE, RIGHT_SIDE, BELOW_SIDE, ABOVE_SIDE, default MathInternalError)
 *    - atol/rtol convergence check with allowed solution switch
 * 4. solve() overloads: 4-arg with startValue, 5-arg with allowedSolution, 3-arg default
 * 5. inverted flag toggling on interval inversion
 * 
 * Boundary values:
 * - Exact root at min, max, or interior point
 * - Function values exactly zero
 * - Very small intervals (atol/rtol convergence)
 * - Function value accuracy (ftol) convergence
 * - AllowedSolution enum values
 * - Inverted intervals (left > right)
 * 
 * Defect targeting (testIssue631):
 * The known defect causes TooManyEvaluationsException when solving
 * f(x) = 0 with a function that has a root at x = 0 but the initial
 * interval does not bracket it properly. The bug is in the interval
 * update logic where the method fails to converge and exceeds maxEval.
 * 
 * The test uses a function where the root is at 0, with initial interval
 * [0.0, 1.0] and maxEval=100. The defective version throws
 * TooManyEvaluationsException instead of returning the root.
 */
public class BaseSecantSolverDeepseekTest {

    // Test helper: concrete implementation for testing abstract class
    private static class TestSecantSolver extends BaseSecantSolver {
        TestSecantSolver(double absoluteAccuracy, Method method) {
            super(absoluteAccuracy, method);
        }
        TestSecantSolver(double relativeAccuracy, double absoluteAccuracy, Method method) {
            super(relativeAccuracy, absoluteAccuracy, method);
        }
        TestSecantSolver(double relativeAccuracy, double absoluteAccuracy,
                        double functionValueAccuracy, Method method) {
            super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy, method);
        }
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testSolveWithExactRootAtMin() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x; }
        };
        // Root at 0, min=0
        double result = solver.solve(100, f, 0.0, 1.0, 0.5);
        assertEquals(0.0, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testSolveWithExactRootAtMax() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 1.0; }
        };
        // Root at 1, max=1
        double result = solver.solve(100, f, 0.0, 1.0, 0.5);
        assertEquals(1.0, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testSolveWithExactRootInterior() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.5; }
        };
        double result = solver.solve(100, f, 0.0, 1.0, 0.5);
        assertEquals(0.5, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveWithAllowedSolutionAnySide() {
        TestSecantSolver solver = new TestSecantSolver(1e-8, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x * x - 2.0; }
        };
        double result = solver.solve(100, f, 1.0, 2.0, 1.5, AllowedSolution.ANY_SIDE);
        assertEquals(Math.sqrt(2.0), result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveWithAllowedSolutionLeftSide() {
        TestSecantSolver solver = new TestSecantSolver(1e-8, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x * x - 2.0; }
        };
        double result = solver.solve(100, f, 1.0, 2.0, 1.5, AllowedSolution.LEFT_SIDE);
        assertTrue(result <= Math.sqrt(2.0) + 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveWithAllowedSolutionRightSide() {
        TestSecantSolver solver = new TestSecantSolver(1e-8, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x * x - 2.0; }
        };
        double result = solver.solve(100, f, 1.0, 2.0, 1.5, AllowedSolution.RIGHT_SIDE);
        assertTrue(result >= Math.sqrt(2.0) - 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveWithAllowedSolutionBelowSide() {
        TestSecantSolver solver = new TestSecantSolver(1e-8, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x * x - 2.0; }
        };
        double result = solver.solve(100, f, 1.0, 2.0, 1.5, AllowedSolution.BELOW_SIDE);
        assertTrue(f.value(result) <= 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveWithAllowedSolutionAboveSide() {
        TestSecantSolver solver = new TestSecantSolver(1e-8, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x * x - 2.0; }
        };
        double result = solver.solve(100, f, 1.0, 2.0, 1.5, AllowedSolution.ABOVE_SIDE);
        assertTrue(f.value(result) >= -1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveDefaultAllowedSolution() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.25; }
        };
        double result = solver.solve(100, f, 0.0, 1.0, 0.5);
        assertEquals(0.25, result, 1e-6);
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testSolveWithTinyInterval() {
        TestSecantSolver solver = new TestSecantSolver(1e-12, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 1e-10; }
        };
        double result = solver.solve(100, f, 0.0, 1e-8, 1e-9);
        assertEquals(1e-10, result, 1e-12);
    }

    @Test(timeout = 4000)
    public void testSolveWithNegativeInterval() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x + 1.0; }
        };
        double result = solver.solve(100, f, -2.0, 0.0, -1.0);
        assertEquals(-1.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveWithInvertedInterval() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.5; }
        };
        // Inverted interval: min > max
        double result = solver.solve(100, f, 1.0, 0.0, 0.5);
        assertEquals(0.5, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveWithZeroFunctionValueAccuracy() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, 1e-6, 0.0, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.75; }
        };
        double result = solver.solve(100, f, 0.0, 1.0, 0.5);
        assertEquals(0.75, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveWithLargeFunctionValues() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return 1e10 * (x - 0.5); }
        };
        double result = solver.solve(100, f, 0.0, 1.0, 0.5);
        assertEquals(0.5, result, 1e-6);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Targets the known defect (testIssue631):
     * The function f(x) = x^3 - 3x^2 + 3x - 1 has a root at x=1.
     * With initial interval [0.0, 1.0] and maxEval=100, the defective
     * version throws TooManyEvaluationsException because the interval
     * update logic fails to converge properly.
     */
    @Test(timeout = 4000)
    public void testIssue631Defect() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - 3.0 * x * x + 3.0 * x - 1.0;
            }
        };
        try {
            double result = solver.solve(100, f, 0.0, 1.0, 0.5);
            // Correct behavior: should converge to root at x=1
            assertEquals(1.0, result, 1e-6);
        } catch (TooManyEvaluationsException e) {
            fail("TooManyEvaluationsException thrown - defect present: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testIssue631DefectWithIllinois() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - 3.0 * x * x + 3.0 * x - 1.0;
            }
        };
        try {
            double result = solver.solve(100, f, 0.0, 1.0, 0.5);
            assertEquals(1.0, result, 1e-6);
        } catch (TooManyEvaluationsException e) {
            fail("TooManyEvaluationsException thrown - defect present: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testIssue631DefectWithPegasus() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - 3.0 * x * x + 3.0 * x - 1.0;
            }
        };
        try {
            double result = solver.solve(100, f, 0.0, 1.0, 0.5);
            assertEquals(1.0, result, 1e-6);
        } catch (TooManyEvaluationsException e) {
            fail("TooManyEvaluationsException thrown - defect present: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testIssue631WithExactRootAtBoundary() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - 3.0 * x * x + 3.0 * x - 1.0;
            }
        };
        // Root exactly at max=1.0
        double result = solver.solve(100, f, 0.0, 1.0, 0.5);
        assertEquals(1.0, result, 0.0);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveWithNonBracketingInterval() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x * x + 1.0; } // Always positive
        };
        solver.solve(100, f, 0.0, 1.0, 0.5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveWithMinEqualsMax() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.5; }
        };
        solver.solve(100, f, 0.5, 0.5, 0.5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveWithStartValueOutsideInterval() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.5; }
        };
        solver.solve(100, f, 0.0, 1.0, 2.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveWithMaxEvalZero() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.5; }
        };
        solver.solve(0, f, 0.0, 1.0, 0.5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveWithNegativeMaxEval() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.5; }
        };
        solver.solve(-10, f, 0.0, 1.0, 0.5);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testConstructorWithAllAccuracies() {
        TestSecantSolver solver = new TestSecantSolver(1e-4, 1e-6, 1e-8, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.5; }
        };
        double result = solver.solve(100, f, 0.0, 1.0, 0.5);
        assertEquals(0.5, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testConstructorWithRelativeAndAbsoluteAccuracy() {
        TestSecantSolver solver = new TestSecantSolver(1e-4, 1e-6, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.25; }
        };
        double result = solver.solve(100, f, 0.0, 1.0, 0.5);
        assertEquals(0.25, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testConstructorWithOnlyAbsoluteAccuracy() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.75; }
        };
        double result = solver.solve(100, f, 0.0, 1.0, 0.5);
        assertEquals(0.75, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveWithRepeatedUse() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f1 = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.3; }
        };
        UnivariateRealFunction f2 = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.7; }
        };
        double result1 = solver.solve(100, f1, 0.0, 1.0, 0.5);
        double result2 = solver.solve(100, f2, 0.0, 1.0, 0.5);
        assertEquals(0.3, result1, 1e-6);
        assertEquals(0.7, result2, 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveWithDifferentAllowedSolutions() {
        TestSecantSolver solver = new TestSecantSolver(1e-8, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x * x - 2.0; }
        };
        double leftResult = solver.solve(100, f, 1.0, 2.0, 1.5, AllowedSolution.LEFT_SIDE);
        double rightResult = solver.solve(100, f, 1.0, 2.0, 1.5, AllowedSolution.RIGHT_SIDE);
        assertTrue(leftResult <= Math.sqrt(2.0) + 1e-6);
        assertTrue(rightResult >= Math.sqrt(2.0) - 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveWithFunctionValueAccuracyConvergence() {
        TestSecantSolver solver = new TestSecantSolver(1e-6, 1e-6, 1e-3, BaseSecantSolver.Method.REGULA_FALSI);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.5; }
        };
        double result = solver.solve(100, f, 0.0, 1.0, 0.5);
        assertEquals(0.5, result, 1e-3);
    }

    @Test(timeout = 4000)
    public void testSolveWithTightAbsoluteAccuracy() {
        TestSecantSolver solver = new TestSecantSolver(1e-12, BaseSecantSolver.Method.ILLINOIS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.123456789; }
        };
        double result = solver.solve(200, f, 0.0, 1.0, 0.5);
        assertEquals(0.123456789, result, 1e-10);
    }

    @Test(timeout = 4000)
    public void testSolveWithLooseRelativeAccuracy() {
        TestSecantSolver solver = new TestSecantSolver(1e-2, 1e-6, BaseSecantSolver.Method.PEGASUS);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.5; }
        };
        double result = solver.solve(100, f, 0.0, 1.0, 0.5);
        assertEquals(0.5, result, 1e-2);
    }
}