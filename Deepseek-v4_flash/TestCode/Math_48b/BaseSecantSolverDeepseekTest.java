package org.apache.commons.math.analysis.solvers;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.ConvergenceException;
import org.apache.commons.math.exception.TooManyEvaluationsException;

/**
 * Test class for BaseSecantSolver (concrete implementations: RegulaFalsiSolver,
 * IllinoisSolver, PegasusSolver). Targets line/branch coverage and the known
 * defect where RegulaFalsiSolver fails to throw ConvergenceException.
 *
 * [Branch & Defect Analysis Matrix]
 * - doSolve() branches:
 *   - f0 == 0.0 -> return x0
 *   - f1 == 0.0 -> return x1
 *   - verifyBracketing (implicitly tested via non-bracketing interval)
 *   - while(true) loop:
 *     - fx == 0.0 -> return x
 *     - f1*fx < 0 -> invert interval, update x0/f0
 *     - else -> switch on method (ILLINOIS, PEGASUS, REGULA_FALSI)
 *     - update x1/f1
 *     - |f1| <= ftol -> switch on allowed (ANY_SIDE, LEFT_SIDE, RIGHT_SIDE, BELOW_SIDE, ABOVE_SIDE)
 *     - |x1-x0| < max(rtol*|x1|, atol) -> switch on allowed (same cases)
 *   - Inverted flag affects LEFT_SIDE/RIGHT_SIDE returns.
 * - Defect: RegulaFalsiSolver should throw ConvergenceException when stagnation
 *   is detected, but instead throws TooManyEvaluationsException.
 *   Test: low maxEval on a convex function where false position stagnates.
 */
public class BaseSecantSolverDeepseekTest {

    private static final double EPS = 1e-12;

    // ---------- Helper: simple functions ----------
    private final UnivariateRealFunction fSquare = new UnivariateRealFunction() {
        public double value(double x) {
            return x * x - 2; // root at sqrt(2) ~1.4142
        }
    };

    private final UnivariateRealFunction fCube = new UnivariateRealFunction() {
        public double value(double x) {
            return x * x * x - 1; // root at 1
        }
    };

    private final UnivariateRealFunction fLinear = new UnivariateRealFunction() {
        public double value(double x) {
            return 2 * x - 4; // root at 2
        }
    };

    private final UnivariateRealFunction fNoRoot = new UnivariateRealFunction() {
        public double value(double x) {
            return x * x + 1; // always positive, no root
        }
    };

    // ---------- Partition A: Core Functional Logic & State Transitions ----------

    @Test(timeout = 4000)
    public void testIllinoisSolveBasic() {
        IllinoisSolver solver = new IllinoisSolver(1e-6);
        double root = solver.solve(100, fSquare, 0, 2);
        assertEquals(Math.sqrt(2), root, 1e-6);
    }

    @Test(timeout = 4000)
    public void testPegasusSolveBasic() {
        PegasusSolver solver = new PegasusSolver(1e-6);
        double root = solver.solve(100, fCube, 0, 2);
        assertEquals(1.0, root, 1e-6);
    }

    @Test(timeout = 4000)
    public void testRegulaFalsiSolveBasic() {
        RegulaFalsiSolver solver = new RegulaFalsiSolver(1e-6);
        double root = solver.solve(100, fLinear, 0, 4);
        assertEquals(2.0, root, 1e-6);
    }

    // Test solve with startValue
    @Test(timeout = 4000)
    public void testSolveWithStartValue() {
        IllinoisSolver solver = new IllinoisSolver(1e-6);
        double root = solver.solve(100, fSquare, 0, 2, 1.0);
        assertEquals(Math.sqrt(2), root, 1e-6);
    }

    // Test solve with AllowedSolution
    @Test(timeout = 4000)
    public void testSolveWithAllowedSolution() {
        IllinoisSolver solver = new IllinoisSolver(1e-6);
        double root = solver.solve(100, fSquare, 0, 2, AllowedSolution.ANY_SIDE);
        assertEquals(Math.sqrt(2), root, 1e-6);
    }

    // Test solve with startValue and AllowedSolution
    @Test(timeout = 4000)
    public void testSolveWithStartValueAndAllowed() {
        PegasusSolver solver = new PegasusSolver(1e-6);
        double root = solver.solve(100, fCube, 0, 2, 1.5, AllowedSolution.ANY_SIDE);
        assertEquals(1.0, root, 1e-6);
    }

    // ---------- Partition B: Boundary Value Analysis & Extremes ----------

    // Exact root at left bound
    @Test(timeout = 4000)
    public void testRootAtLeftBound() {
        IllinoisSolver solver = new IllinoisSolver(1e-6);
        double root = solver.solve(100, fLinear, 2, 4); // f(2)=0
        assertEquals(2.0, root, 0.0);
    }

    // Exact root at right bound
    @Test(timeout = 4000)
    public void testRootAtRightBound() {
        PegasusSolver solver = new PegasusSolver(1e-6);
        double root = solver.solve(100, fLinear, 0, 2); // f(2)=0
        assertEquals(2.0, root, 0.0);
    }

    // Exact root at approximation (fx == 0)
    @Test(timeout = 4000)
    public void testExactRootDuringIteration() {
        // Use a function where the secant step lands exactly on root
        // f(x)=x-1, interval [0,2] -> secant step will hit 1 quickly
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1;
            }
        };
        RegulaFalsiSolver solver = new RegulaFalsiSolver(1e-12);
        double root = solver.solve(100, f, 0, 2);
        assertEquals(1.0, root, 0.0);
    }

    // Non-bracketing interval -> should throw IllegalArgumentException (via verifyBracketing)
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNonBracketingInterval() {
        IllinoisSolver solver = new IllinoisSolver(1e-6);
        solver.solve(100, fNoRoot, 0, 2);
    }

    // ---------- Partition C: Defect-Targeted Branch Zone ----------
    // Known defect: RegulaFalsiSolver should throw ConvergenceException when
    // stagnation is detected, but instead throws TooManyEvaluationsException.
    // Use a convex function (f(x)=x^2-2) with low maxEval to trigger stagnation.
    @Test(expected = ConvergenceException.class, timeout = 4000)
    public void testRegulaFalsiStagnationThrowsConvergenceException() {
        RegulaFalsiSolver solver = new RegulaFalsiSolver(1e-6);
        // Use a small maxEval to force early detection of stagnation.
        // The buggy version will throw TooManyEvaluationsException instead.
        solver.solve(50, fSquare, 0, 2);
    }

    // Also test that Illinois and Pegasus do not throw ConvergenceException
    // (they should converge normally)
    @Test(timeout = 4000)
    public void testIllinoisNoStagnation() {
        IllinoisSolver solver = new IllinoisSolver(1e-6);
        double root = solver.solve(50, fSquare, 0, 2);
        assertEquals(Math.sqrt(2), root, 1e-6);
    }

    @Test(timeout = 4000)
    public void testPegasusNoStagnation() {
        PegasusSolver solver = new PegasusSolver(1e-6);
        double root = solver.solve(50, fSquare, 0, 2);
        assertEquals(Math.sqrt(2), root, 1e-6);
    }

    // ---------- Partition D: Exception & Defensive Guard Paths ----------

    // Test that TooManyEvaluationsException is thrown when maxEval is too low
    // (for methods that don't stagnate, but still need many evaluations)
    @Test(expected = TooManyEvaluationsException.class, timeout = 4000)
    public void testTooManyEvaluations() {
        IllinoisSolver solver = new IllinoisSolver(1e-12);
        // Very low maxEval, high accuracy -> will exceed evaluations
        solver.solve(2, fSquare, 0, 2);
    }

    // Test that ConvergenceException can be thrown for other reasons? Not directly.
    // But we can test that the solver throws appropriate exception for invalid parameters.
    // (Already tested non-bracketing)

    // ---------- Partition E: Object Lifecycle & Contract Integrity ----------
    // (No serialization/clone in this abstract class; constructors are tested implicitly)

    // ---------- Additional branch coverage: AllowedSolution variations ----------

    @Test(timeout = 4000)
    public void testAllowedSolutionLeftSide() {
        IllinoisSolver solver = new IllinoisSolver(1e-6);
        // Use a function where root is exactly at left side? Not needed.
        // Just ensure no exception.
        double root = solver.solve(100, fSquare, 0, 2, AllowedSolution.LEFT_SIDE);
        assertTrue(root >= 0 && root <= 2);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionRightSide() {
        PegasusSolver solver = new PegasusSolver(1e-6);
        double root = solver.solve(100, fSquare, 0, 2, AllowedSolution.RIGHT_SIDE);
        assertTrue(root >= 0 && root <= 2);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionBelowSide() {
        RegulaFalsiSolver solver = new RegulaFalsiSolver(1e-6);
        double root = solver.solve(100, fSquare, 0, 2, AllowedSolution.BELOW_SIDE);
        assertTrue(root >= 0 && root <= 2);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionAboveSide() {
        IllinoisSolver solver = new IllinoisSolver(1e-6);
        double root = solver.solve(100, fSquare, 0, 2, AllowedSolution.ABOVE_SIDE);
        assertTrue(root >= 0 && root <= 2);
    }

    // Test that when function value accuracy is met, allowed solution is respected.
    // We'll use a function that converges to a point where f is very small.
    @Test(timeout = 4000)
    public void testFunctionValueAccuracyWithAllowed() {
        // Use a high function value accuracy so that the solver stops early.
        IllinoisSolver solver = new IllinoisSolver(1e-6, 1e-6, 1e-3, Method.ILLINOIS);
        // f(x)=x^2-2, root ~1.414, f(1.414) ~0. So ftol=1e-3 will be satisfied.
        double root = solver.solve(100, fSquare, 0, 2, AllowedSolution.ANY_SIDE);
        assertEquals(Math.sqrt(2), root, 1e-2); // loose tolerance due to ftol
    }

    // Test the case where interval is within accuracies (relative/absolute)
    @Test(timeout = 4000)
    public void testIntervalAccuracy() {
        // Use a very loose absolute accuracy so that interval shrinks quickly.
        IllinoisSolver solver = new IllinoisSolver(1e-2);
        double root = solver.solve(100, fSquare, 0, 2);
        assertEquals(Math.sqrt(2), root, 1e-2);
    }

    // Test inverted interval scenario (when f1*fx < 0)
    // This is covered by normal solve, but we can force inversion by choosing
    // a function where the secant step flips sign.
    @Test(timeout = 4000)
    public void testInvertedInterval() {
        // Use a function where the root is near the right bound.
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 10; // root at 10
            }
        };
        IllinoisSolver solver = new IllinoisSolver(1e-6);
        double root = solver.solve(100, f, 0, 20);
        assertEquals(10.0, root, 1e-6);
    }

    // Test that the method enum default case is not reachable (MathInternalError)
    // We cannot test this without modifying the enum, so skip.

    // Test that the allowed solution default case is not reachable (MathInternalError)
    // Skip.

    // ---------- Additional: test with different constructors ----------
    @Test(timeout = 4000)
    public void testConstructorWithRelativeAccuracy() {
        IllinoisSolver solver = new IllinoisSolver(1e-4, 1e-6, Method.ILLINOIS);
        double root = solver.solve(100, fSquare, 0, 2);
        assertEquals(Math.sqrt(2), root, 1e-4);
    }

    @Test(timeout = 4000)
    public void testConstructorWithAllAccuracies() {
        PegasusSolver solver = new PegasusSolver(1e-4, 1e-6, 1e-8, Method.PEGASUS);
        double root = solver.solve(100, fCube, 0, 2);
        assertEquals(1.0, root, 1e-4);
    }

    // ---------- Helper inner class to expose protected constructor? Not needed. ----------
}