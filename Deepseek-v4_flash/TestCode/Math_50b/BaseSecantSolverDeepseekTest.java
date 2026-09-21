package org.apache.commons.math.analysis.solvers;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.TooManyEvaluationsException;
import org.apache.commons.math.exception.MathInternalError;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: BaseSecantSolver (abstract) - concrete subclasses RegulaFalsiSolver,
 * IllinoisSolver, PegasusSolver.
 * 
 * Branches covered:
 * - doSolve() initial root check (f0==0, f1==0)
 * - verifyBracketing path (successful bracket)
 * - main iteration loop:
 *   - fx==0 exact root return
 *   - f1*fx < 0 interval inversion (inverted flag toggle)
 *   - else branch with method switch:
 *     - ILLINOIS: f0 *= 0.5
 *     - PEGASUS: f0 *= f1/(f1+fx)
 *     - REGULA_FALSI: special handling when x==x1 (stagnation)
 *     - default: MathInternalError (unreachable)
 * - convergence check (|x1-x0| < max(rtol*|x1|, atol)):
 *   - ALLOWED_SIDE switch:
 *     - ANY_SIDE, LEFT_SIDE, RIGHT_SIDE, BELOW_SIDE, ABOVE_SIDE
 *     - default: MathInternalError
 * - ftol check (|f1| <= ftol) with allowed side handling
 * - inverted flag usage in LEFT_SIDE/RIGHT_SIDE returns
 * 
 * Defect targeting (testIssue631):
 * - RegulaFalsiSolver with function f(x)=x^3 + x^2 + x + 1 (no real root)
 *   on interval [-2, 2] with maxEval=100 should throw TooManyEvaluationsException.
 *   Defective version may loop indefinitely or return wrong value.
 * 
 * Boundary values:
 * - Exact root at bounds (f0==0, f1==0)
 * - Zero function value at approximation (fx==0)
 * - Interval inversion (f1*fx < 0)
 * - Stagnation in REGULA_FALSI (x==x1)
 * - Convergence with all allowed sides
 * - ftol tolerance with all allowed sides
 * - Inverted flag with LEFT_SIDE/RIGHT_SIDE
 */
public class BaseSecantSolverDeepseekTest {

    // Helper: simple linear function f(x)=x-2, root at 2
    private static class LinearFunction implements UnivariateRealFunction {
        @Override
        public double value(double x) {
            return x - 2.0;
        }
    }

    // Helper: quadratic with roots at -1 and 1
    private static class QuadraticFunction implements UnivariateRealFunction {
        @Override
        public double value(double x) {
            return x * x - 1.0;
        }
    }

    // Helper: cubic with root at 0
    private static class CubicFunction implements UnivariateRealFunction {
        @Override
        public double value(double x) {
            return x * x * x;
        }
    }

    // Helper: function with no real root (for defect test)
    private static class NoRealRootFunction implements UnivariateRealFunction {
        @Override
        public double value(double x) {
            return x * x + 1.0; // always positive
        }
    }

    // Helper: function with root at 0 but steep
    private static class SteepFunction implements UnivariateRealFunction {
        @Override
        public double value(double x) {
            return Math.tanh(x); // root at 0
        }
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testRegulaFalsiBasicRoot() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testIllinoisBasicRoot() {
        BaseSecantSolver solver = new IllinoisSolver();
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testPegasusBasicRoot() {
        BaseSecantSolver solver = new PegasusSolver();
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testQuadraticRoots() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        double result = solver.solve(100, new QuadraticFunction(), 0.5, 3.0);
        assertEquals(1.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testCubicRoot() {
        BaseSecantSolver solver = new IllinoisSolver();
        double result = solver.solve(100, new CubicFunction(), -1.0, 1.0);
        assertEquals(0.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testStartValueProvided() {
        BaseSecantSolver solver = new PegasusSolver();
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0, 3.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionAnySide() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        double result = solver.solve(100, new QuadraticFunction(), 0.5, 3.0,
                                     AllowedSolution.ANY_SIDE);
        assertEquals(1.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionLeftSide() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        double result = solver.solve(100, new QuadraticFunction(), 0.5, 3.0,
                                     AllowedSolution.LEFT_SIDE);
        assertTrue(result <= 1.0 + 1e-6);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionRightSide() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        double result = solver.solve(100, new QuadraticFunction(), 0.5, 3.0,
                                     AllowedSolution.RIGHT_SIDE);
        assertTrue(result >= 1.0 - 1e-6);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionBelowSide() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        double result = solver.solve(100, new QuadraticFunction(), 0.5, 3.0,
                                     AllowedSolution.BELOW_SIDE);
        assertTrue(result * result - 1.0 <= 1e-6);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionAboveSide() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        double result = solver.solve(100, new QuadraticFunction(), 0.5, 3.0,
                                     AllowedSolution.ABOVE_SIDE);
        assertTrue(result * result - 1.0 >= -1e-6);
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testExactRootAtMinBound() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        double result = solver.solve(100, new LinearFunction(), 2.0, 5.0);
        assertEquals(2.0, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testExactRootAtMaxBound() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        double result = solver.solve(100, new LinearFunction(), 0.0, 2.0);
        assertEquals(2.0, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testTinyInterval() {
        BaseSecantSolver solver = new RegulaFalsiSolver(1e-12, 1e-12, 1e-12);
        double result = solver.solve(100, new LinearFunction(), 1.999999, 2.000001);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testLargeInterval() {
        BaseSecantSolver solver = new IllinoisSolver();
        double result = solver.solve(1000, new LinearFunction(), -1e6, 1e6);
        assertEquals(2.0, result, 1e-3);
    }

    @Test(timeout = 4000)
    public void testZeroFunctionValueAccuracy() {
        BaseSecantSolver solver = new PegasusSolver(1e-15, 1e-15, 1e-15);
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0);
        assertEquals(2.0, result, 1e-10);
    }

    @Test(timeout = 4000)
    public void testNegativeInterval() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        double result = solver.solve(100, new LinearFunction(), -5.0, 0.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testInvertedInterval() {
        BaseSecantSolver solver = new IllinoisSolver();
        double result = solver.solve(100, new LinearFunction(), 5.0, 0.0);
        assertEquals(2.0, result, 1e-6);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Defect test for issue631: RegulaFalsiSolver should throw
     * TooManyEvaluationsException when no root exists and maxEval is exceeded.
     * The defective version may loop indefinitely or return a wrong value.
     */
    @Test(timeout = 4000, expected = TooManyEvaluationsException.class)
    public void testIssue631_RegulaFalsiNoRoot() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        // Function x^2+1 has no real root; interval [-2,2] brackets no root.
        // Should throw TooManyEvaluationsException after maxEval=100 evaluations.
        solver.solve(100, new NoRealRootFunction(), -2.0, 2.0);
        fail("Expected TooManyEvaluationsException");
    }

    @Test(timeout = 4000)
    public void testRegulaFalsiStagnationHandling() {
        // Function with root but tricky convergence for Regula Falsi
        BaseSecantSolver solver = new RegulaFalsiSolver();
        double result = solver.solve(1000, new SteepFunction(), -1.0, 1.0);
        assertEquals(0.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testIllinoisNoRoot() {
        BaseSecantSolver solver = new IllinoisSolver();
        try {
            solver.solve(100, new NoRealRootFunction(), -2.0, 2.0);
            fail("Expected TooManyEvaluationsException");
        } catch (TooManyEvaluationsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPegasusNoRoot() {
        BaseSecantSolver solver = new PegasusSolver();
        try {
            solver.solve(100, new NoRealRootFunction(), -2.0, 2.0);
            fail("Expected TooManyEvaluationsException");
        } catch (TooManyEvaluationsException e) {
            // expected
        }
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNonBracketingInterval() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        // Function x-2, interval [0,1] does not bracket root (both negative)
        solver.solve(100, new LinearFunction(), 0.0, 1.0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMinEqualsMax() {
        BaseSecantSolver solver = new IllinoisSolver();
        solver.solve(100, new LinearFunction(), 2.0, 2.0);
    }

    @Test(timeout = 4000)
    public void testMaxEvaluationsZero() {
        BaseSecantSolver solver = new PegasusSolver();
        try {
            solver.solve(0, new LinearFunction(), 0.0, 5.0);
            fail("Expected TooManyEvaluationsException");
        } catch (TooManyEvaluationsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNullFunction() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        try {
            solver.solve(100, null, 0.0, 5.0);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testDefaultConstructorAccuracy() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        assertEquals(1e-6, solver.getAbsoluteAccuracy(), 1e-15);
        assertEquals(1e-14, solver.getRelativeAccuracy(), 1e-15);
        assertEquals(1e-15, solver.getFunctionValueAccuracy(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConstructorWithAbsoluteAccuracy() {
        BaseSecantSolver solver = new IllinoisSolver(1e-8);
        assertEquals(1e-8, solver.getAbsoluteAccuracy(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConstructorWithRelativeAndAbsolute() {
        BaseSecantSolver solver = new PegasusSolver(1e-10, 1e-8);
        assertEquals(1e-10, solver.getRelativeAccuracy(), 1e-15);
        assertEquals(1e-8, solver.getAbsoluteAccuracy(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConstructorWithAllAccuracies() {
        BaseSecantSolver solver = new RegulaFalsiSolver(1e-10, 1e-8, 1e-12);
        assertEquals(1e-10, solver.getRelativeAccuracy(), 1e-15);
        assertEquals(1e-8, solver.getAbsoluteAccuracy(), 1e-15);
        assertEquals(1e-12, solver.getFunctionValueAccuracy(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSolveWithStartValueAndAllowedSolution() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0, 3.0,
                                     AllowedSolution.LEFT_SIDE);
        assertTrue(result <= 2.0 + 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveWithAllowedSolutionOverload() {
        BaseSecantSolver solver = new IllinoisSolver();
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0,
                                     AllowedSolution.RIGHT_SIDE);
        assertTrue(result >= 2.0 - 1e-6);
    }

    @Test(timeout = 4000)
    public void testRepeatedSolveCalls() {
        BaseSecantSolver solver = new PegasusSolver();
        double result1 = solver.solve(100, new LinearFunction(), 0.0, 5.0);
        double result2 = solver.solve(100, new LinearFunction(), 0.0, 5.0);
        assertEquals(result1, result2, 0.0);
    }

    @Test(timeout = 4000)
    public void testInvertedFlagWithLeftSide() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        // Inverted interval: max < min
        double result = solver.solve(100, new LinearFunction(), 5.0, 0.0,
                                     AllowedSolution.LEFT_SIDE);
        assertTrue(result <= 2.0 + 1e-6);
    }

    @Test(timeout = 4000)
    public void testInvertedFlagWithRightSide() {
        BaseSecantSolver solver = new IllinoisSolver();
        double result = solver.solve(100, new LinearFunction(), 5.0, 0.0,
                                     AllowedSolution.RIGHT_SIDE);
        assertTrue(result >= 2.0 - 1e-6);
    }

    @Test(timeout = 4000)
    public void testBelowSideWithNegativeFunction() {
        BaseSecantSolver solver = new PegasusSolver();
        // Function x-2, at root 2, f(2)=0 which is not below
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0,
                                     AllowedSolution.BELOW_SIDE);
        assertTrue(result * result - 2.0 * result + 1.0 <= 1e-6);
    }

    @Test(timeout = 4000)
    public void testAboveSideWithPositiveFunction() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0,
                                     AllowedSolution.ABOVE_SIDE);
        assertTrue(result * result - 2.0 * result + 1.0 >= -1e-6);
    }

    @Test(timeout = 4000)
    public void testFunctionValueAccuracyTolerance() {
        // Very tight function value accuracy should still converge
        BaseSecantSolver solver = new IllinoisSolver(1e-12, 1e-12, 1e-12);
        double result = solver.solve(1000, new LinearFunction(), 0.0, 5.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testRelativeAccuracyDominates() {
        // Large relative accuracy should cause early convergence
        BaseSecantSolver solver = new RegulaFalsiSolver(1e-2, 1e-15, 1e-15);
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0);
        assertTrue(Math.abs(result - 2.0) < 0.1);
    }

    @Test(timeout = 4000)
    public void testAbsoluteAccuracyDominates() {
        BaseSecantSolver solver = new PegasusSolver(1e-15, 1e-2, 1e-15);
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0);
        assertTrue(Math.abs(result - 2.0) < 0.1);
    }

    @Test(timeout = 4000)
    public void testMaxEvaluationsExactlyEnough() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        // Should converge within 100 evaluations for linear function
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testMaxEvaluationsTooFew() {
        BaseSecantSolver solver = new IllinoisSolver();
        try {
            solver.solve(1, new LinearFunction(), 0.0, 5.0);
            fail("Expected TooManyEvaluationsException");
        } catch (TooManyEvaluationsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSolveWithStartValueAtRoot() {
        BaseSecantSolver solver = new PegasusSolver();
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0, 2.0);
        assertEquals(2.0, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testSolveWithStartValueOutsideInterval() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0, 10.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testMultipleAllowedSolutions() {
        BaseSecantSolver solver = new IllinoisSolver();
        double resultAny = solver.solve(100, new QuadraticFunction(), 0.5, 3.0,
                                        AllowedSolution.ANY_SIDE);
        double resultLeft = solver.solve(100, new QuadraticFunction(), 0.5, 3.0,
                                         AllowedSolution.LEFT_SIDE);
        double resultRight = solver.solve(100, new QuadraticFunction(), 0.5, 3.0,
                                          AllowedSolution.RIGHT_SIDE);
        assertEquals(1.0, resultAny, 1e-6);
        assertTrue(resultLeft <= 1.0 + 1e-6);
        assertTrue(resultRight >= 1.0 - 1e-6);
    }

    @Test(timeout = 4000)
    public void testConvergenceWithTightTolerance() {
        BaseSecantSolver solver = new PegasusSolver(1e-15, 1e-15, 1e-15);
        double result = solver.solve(1000, new LinearFunction(), 0.0, 5.0);
        assertEquals(2.0, result, 1e-12);
    }

    @Test(timeout = 4000)
    public void testFunctionWithMultipleRoots() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        // sin(x) has roots at 0, pi, 2pi, etc.
        UnivariateRealFunction sinFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Math.sin(x);
            }
        };
        double result = solver.solve(100, sinFunc, 3.0, 4.0);
        assertEquals(Math.PI, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testFunctionWithDiscontinuity() {
        BaseSecantSolver solver = new IllinoisSolver();
        // 1/x has a discontinuity at 0, but root at infinity (not applicable)
        // Use a function with a pole: tan(x) has root at 0
        UnivariateRealFunction tanFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Math.tan(x);
            }
        };
        double result = solver.solve(100, tanFunc, -0.5, 0.5);
        assertEquals(0.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testVeryFlatFunction() {
        BaseSecantSolver solver = new PegasusSolver();
        // f(x) = x^3, very flat near root
        double result = solver.solve(1000, new CubicFunction(), -1.0, 1.0);
        assertEquals(0.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testVerySteepFunction() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        // f(x) = 1000*(x-2), steep
        UnivariateRealFunction steep = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return 1000.0 * (x - 2.0);
            }
        };
        double result = solver.solve(100, steep, 0.0, 5.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testNegativeFunctionValues() {
        BaseSecantSolver solver = new IllinoisSolver();
        // f(x) = -(x-2), root at 2 but negative slope
        UnivariateRealFunction negFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return -(x - 2.0);
            }
        };
        double result = solver.solve(100, negFunc, 0.0, 5.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testAllowedSideWithInvertedInterval() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        // Inverted interval with BELOW_SIDE
        double result = solver.solve(100, new LinearFunction(), 5.0, 0.0,
                                     AllowedSolution.BELOW_SIDE);
        assertTrue(result * result - 2.0 * result + 1.0 <= 1e-6);
    }

    @Test(timeout = 4000)
    public void testAllowedSideWithInvertedIntervalAbove() {
        BaseSecantSolver solver = new PegasusSolver();
        double result = solver.solve(100, new LinearFunction(), 5.0, 0.0,
                                     AllowedSolution.ABOVE_SIDE);
        assertTrue(result * result - 2.0 * result + 1.0 >= -1e-6);
    }

    @Test(timeout = 4000)
    public void testMaxEvaluationsBoundary() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        // Use a function that requires many evaluations
        UnivariateRealFunction hardFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Math.sin(1.0 / x);
            }
        };
        try {
            solver.solve(1000, hardFunc, 0.1, 1.0);
            // May or may not converge, but should not hang
        } catch (TooManyEvaluationsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRepeatedUseWithDifferentFunctions() {
        BaseSecantSolver solver = new IllinoisSolver();
        double result1 = solver.solve(100, new LinearFunction(), 0.0, 5.0);
        double result2 = solver.solve(100, new QuadraticFunction(), 0.5, 3.0);
        assertEquals(2.0, result1, 1e-6);
        assertEquals(1.0, result2, 1e-6);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionChangesBetweenCalls() {
        BaseSecantSolver solver = new PegasusSolver();
        double result1 = solver.solve(100, new LinearFunction(), 0.0, 5.0,
                                      AllowedSolution.LEFT_SIDE);
        double result2 = solver.solve(100, new LinearFunction(), 0.0, 5.0,
                                      AllowedSolution.RIGHT_SIDE);
        assertTrue(result1 <= 2.0 + 1e-6);
        assertTrue(result2 >= 2.0 - 1e-6);
    }

    @Test(timeout = 4000)
    public void testFunctionValueAccuracyExactRoot() {
        BaseSecantSolver solver = new RegulaFalsiSolver(1e-15, 1e-15, 1e-15);
        // Function with exact root at 2.0
        double result = solver.solve(100, new LinearFunction(), 2.0, 5.0);
        assertEquals(2.0, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testRelativeAccuracyZero() {
        BaseSecantSolver solver = new IllinoisSolver(0.0, 1e-6, 1e-15);
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testAbsoluteAccuracyZero() {
        BaseSecantSolver solver = new PegasusSolver(1e-6, 0.0, 1e-15);
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testBothAccuraciesZero() {
        BaseSecantSolver solver = new RegulaFalsiSolver(0.0, 0.0, 1e-15);
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testNegativeMaxEvaluations() {
        BaseSecantSolver solver = new IllinoisSolver();
        try {
            solver.solve(-1, new LinearFunction(), 0.0, 5.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNaNFunctionValue() {
        BaseSecantSolver solver = new PegasusSolver();
        UnivariateRealFunction nanFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.NaN;
            }
        };
        try {
            solver.solve(100, nanFunc, 0.0, 5.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testInfiniteFunctionValue() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        UnivariateRealFunction infFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.POSITIVE_INFINITY;
            }
        };
        try {
            solver.solve(100, infFunc, 0.0, 5.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMinGreaterThanMax() {
        BaseSecantSolver solver = new IllinoisSolver();
        double result = solver.solve(100, new LinearFunction(), 5.0, 0.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testStartValueAtMin() {
        BaseSecantSolver solver = new PegasusSolver();
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0, 0.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testStartValueAtMax() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        double result = solver.solve(100, new LinearFunction(), 0.0, 5.0, 5.0);
        assertEquals(2.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionNull() {
        BaseSecantSolver solver = new IllinoisSolver();
        try {
            solver.solve(100, new LinearFunction(), 0.0, 5.0, (AllowedSolution) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMethodEnumValues() {
        // Verify all enum constants exist
        assertNotNull(BaseSecantSolver.Method.REGULA_FALSI);
        assertNotNull(BaseSecantSolver.Method.ILLINOIS);
        assertNotNull(BaseSecantSolver.Method.PEGASUS);
        assertEquals(3, BaseSecantSolver.Method.values().length);
    }

    @Test(timeout = 4000)
    public void testMethodEnumToString() {
        assertEquals("REGULA_FALSI", BaseSecantSolver.Method.REGULA_FALSI.toString());
        assertEquals("ILLINOIS", BaseSecantSolver.Method.ILLINOIS.toString());
        assertEquals("PEGASUS", BaseSecantSolver.Method.PEGASUS.toString());
    }

    @Test(timeout = 4000)
    public void testMethodEnumValueOf() {
        assertEquals(BaseSecantSolver.Method.REGULA_FALSI,
                     BaseSecantSolver.Method.valueOf("REGULA_FALSI"));
        assertEquals(BaseSecantSolver.Method.ILLINOIS,
                     BaseSecantSolver.Method.valueOf("ILLINOIS"));
        assertEquals(BaseSecantSolver.Method.PEGASUS,
                     BaseSecantSolver.Method.valueOf("PEGASUS"));
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionEnumValues() {
        assertNotNull(AllowedSolution.ANY_SIDE);
        assertNotNull(AllowedSolution.LEFT_SIDE);
        assertNotNull(AllowedSolution.RIGHT_SIDE);
        assertNotNull(AllowedSolution.BELOW_SIDE);
        assertNotNull(AllowedSolution.ABOVE_SIDE);
        assertEquals(5, AllowedSolution.values().length);
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionEnumValueOf() {
        assertEquals(AllowedSolution.ANY_SIDE, AllowedSolution.valueOf("ANY_SIDE"));
        assertEquals(AllowedSolution.LEFT_SIDE, AllowedSolution.valueOf("LEFT_SIDE"));
        assertEquals(AllowedSolution.RIGHT_SIDE, AllowedSolution.valueOf("RIGHT_SIDE"));
        assertEquals(AllowedSolution.BELOW_SIDE, AllowedSolution.valueOf("BELOW_SIDE"));
        assertEquals(AllowedSolution.ABOVE_SIDE, AllowedSolution.valueOf("ABOVE_SIDE"));
    }

    @Test(timeout = 4000)
    public void testAllowedSolutionEnumToString() {
        assertEquals("ANY_SIDE", AllowedSolution.ANY_SIDE.toString());
        assertEquals("LEFT_SIDE", AllowedSolution.LEFT_SIDE.toString());
        assertEquals("RIGHT_SIDE", AllowedSolution.RIGHT_SIDE.toString());
        assertEquals("BELOW_SIDE", AllowedSolution.BELOW_SIDE.toString());
        assertEquals("ABOVE_SIDE", AllowedSolution.ABOVE_SIDE.toString());
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethods() {
        // Test all three methods with same function
        UnivariateRealFunction func = new LinearFunction();
        double expected = 2.0;

        BaseSecantSolver regula = new RegulaFalsiSolver();
        assertEquals(expected, regula.solve(100, func, 0.0, 5.0), 1e-6);

        BaseSecantSolver illinois = new IllinoisSolver();
        assertEquals(expected, illinois.solve(100, func, 0.0, 5.0), 1e-6);

        BaseSecantSolver pegasus = new PegasusSolver();
        assertEquals(expected, pegasus.solve(100, func, 0.0, 5.0), 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndAllowedSolutions() {
        UnivariateRealFunction func = new QuadraticFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.5, 3.0, allowed);
                assertTrue("Method: " + method + ", allowed: " + allowed,
                           Math.abs(result - 1.0) < 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndInvertedIntervals() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            double result = solver.solve(100, func, 5.0, 0.0);
            assertEquals("Method: " + method, 2.0, result, 1e-6);
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValues() {
        UnivariateRealFunction func = new LinearFunction();
        double[] startValues = {-10.0, 0.0, 2.0, 5.0, 10.0};
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (double start : startValues) {
                double result = solver.solve(100, func, 0.0, 5.0, start);
                assertEquals("Method: " + method + ", start: " + start,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndTolerances() {
        UnivariateRealFunction func = new LinearFunction();
        double[] tolerances = {1e-3, 1e-6, 1e-9, 1e-12};
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            for (double tol : tolerances) {
                BaseSecantSolver solver;
                switch (method) {
                    case REGULA_FALSI:
                        solver = new RegulaFalsiSolver(tol, tol, tol);
                        break;
                    case ILLINOIS:
                        solver = new IllinoisSolver(tol, tol, tol);
                        break;
                    case PEGASUS:
                        solver = new PegasusSolver(tol, tol, tol);
                        break;
                    default:
                        throw new AssertionError("Unknown method: " + method);
                }
                double result = solver.solve(1000, func, 0.0, 5.0);
                assertEquals("Method: " + method + ", tol: " + tol,
                             2.0, result, tol * 10);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNoRoot() {
        UnivariateRealFunction func = new NoRealRootFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, func, -2.0, 2.0);
                fail("Expected TooManyEvaluationsException for method: " + method);
            } catch (TooManyEvaluationsException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtBound() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            double result = solver.solve(100, func, 2.0, 5.0);
            assertEquals("Method: " + method, 2.0, result, 0.0);
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtMaxBound() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            double result = solver.solve(100, func, 0.0, 2.0);
            assertEquals("Method: " + method, 2.0, result, 0.0);
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtStartValue() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            double result = solver.solve(100, func, 0.0, 5.0, 2.0);
            assertEquals("Method: " + method, 2.0, result, 0.0);
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndFunctionValueAccuracy() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-6, 1e-3);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-6, 1e-3);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-6, 1e-3);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            double result = solver.solve(100, func, 0.0, 5.0);
            assertEquals("Method: " + method, 2.0, result, 1e-3);
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndRelativeAccuracy() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-3, 1e-6, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-3, 1e-6, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-3, 1e-6, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            double result = solver.solve(100, func, 0.0, 5.0);
            assertEquals("Method: " + method, 2.0, result, 1e-3);
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndAbsoluteAccuracy() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-3, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-3, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-3, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            double result = solver.solve(100, func, 0.0, 5.0);
            assertEquals("Method: " + method, 2.0, result, 1e-3);
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluations() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            double result = solver.solve(10, func, 0.0, 5.0);
            assertEquals("Method: " + method, 2.0, result, 1e-6);
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsTooLow() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(1, func, 0.0, 5.0);
                fail("Expected TooManyEvaluationsException for method: " + method);
            } catch (TooManyEvaluationsException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNonBracketing() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, func, 0.0, 1.0);
                fail("Expected IllegalArgumentException for method: " + method);
            } catch (IllegalArgumentException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullFunction() {
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, null, 0.0, 5.0);
                fail("Expected NullPointerException for method: " + method);
            } catch (NullPointerException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMinEqualsMax() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, func, 2.0, 2.0);
                fail("Expected IllegalArgumentException for method: " + method);
            } catch (IllegalArgumentException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNaNFunction() {
        UnivariateRealFunction nanFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.NaN;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, nanFunc, 0.0, 5.0);
                fail("Expected IllegalArgumentException for method: " + method);
            } catch (IllegalArgumentException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndInfiniteFunction() {
        UnivariateRealFunction infFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.POSITIVE_INFINITY;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, infFunc, 0.0, 5.0);
                fail("Expected IllegalArgumentException for method: " + method);
            } catch (IllegalArgumentException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNegativeMaxEvaluations() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(-1, func, 0.0, 5.0);
                fail("Expected IllegalArgumentException for method: " + method);
            } catch (IllegalArgumentException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullAllowedSolution() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, func, 0.0, 5.0, (AllowedSolution) null);
                fail("Expected NullPointerException for method: " + method);
            } catch (NullPointerException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueNullAllowedSolution() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, func, 0.0, 5.0, 2.0, null);
                fail("Expected NullPointerException for method: " + method);
            } catch (NullPointerException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueOutsideInterval() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            double result = solver.solve(100, func, 0.0, 5.0, 10.0);
            assertEquals("Method: " + method, 2.0, result, 1e-6);
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMin() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            double result = solver.solve(100, func, 0.0, 5.0, 0.0);
            assertEquals("Method: " + method, 2.0, result, 1e-6);
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMax() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            double result = solver.solve(100, func, 0.0, 5.0, 5.0);
            assertEquals("Method: " + method, 2.0, result, 1e-6);
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtRoot() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            double result = solver.solve(100, func, 0.0, 5.0, 2.0);
            assertEquals("Method: " + method, 2.0, result, 0.0);
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndAllowedSolutions() {
        UnivariateRealFunction func = new QuadraticFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.5, 3.0, allowed);
                assertTrue("Method: " + method + ", allowed: " + allowed,
                           Math.abs(result - 1.0) < 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndInvertedIntervals() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            double result = solver.solve(100, func, 5.0, 0.0);
            assertEquals("Method: " + method, 2.0, result, 1e-6);
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndInvertedIntervalsWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 5.0, 0.0, allowed);
                assertTrue("Method: " + method + ", allowed: " + allowed,
                           Math.abs(result - 2.0) < 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertTrue("Method: " + method + ", allowed: " + allowed,
                           Math.abs(result - 2.0) < 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueWithAllowedInverted() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 5.0, 0.0, 3.0, allowed);
                assertTrue("Method: " + method + ", allowed: " + allowed,
                           Math.abs(result - 2.0) < 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndTolerancesWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        double[] tolerances = {1e-3, 1e-6, 1e-9};
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            for (double tol : tolerances) {
                BaseSecantSolver solver;
                switch (method) {
                    case REGULA_FALSI:
                        solver = new RegulaFalsiSolver(tol, tol, tol);
                        break;
                    case ILLINOIS:
                        solver = new IllinoisSolver(tol, tol, tol);
                        break;
                    case PEGASUS:
                        solver = new PegasusSolver(tol, tol, tol);
                        break;
                    default:
                        throw new AssertionError("Unknown method: " + method);
                }
                for (AllowedSolution allowed : AllowedSolution.values()) {
                    double result = solver.solve(1000, func, 0.0, 5.0, allowed);
                    assertTrue("Method: " + method + ", tol: " + tol + ", allowed: " + allowed,
                               Math.abs(result - 2.0) < tol * 10);
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNoRootWithAllowed() {
        UnivariateRealFunction func = new NoRealRootFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, -2.0, 2.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtBoundWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 2.0, 5.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtMaxBoundWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtStartValueWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndFunctionValueAccuracyWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-6, 1e-3);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-6, 1e-3);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-6, 1e-3);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndRelativeAccuracyWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-3, 1e-6, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-3, 1e-6, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-3, 1e-6, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndAbsoluteAccuracyWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-3, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-3, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-3, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(10, func, 0.0, 5.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsTooLowWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(1, func, 0.0, 5.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNonBracketingWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 0.0, 1.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullFunctionWithAllowed() {
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, null, 0.0, 5.0, allowed);
                    fail("Expected NullPointerException for method: " + method + ", allowed: " + allowed);
                } catch (NullPointerException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMinEqualsMaxWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 2.0, 2.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNaNFunctionWithAllowed() {
        UnivariateRealFunction nanFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.NaN;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, nanFunc, 0.0, 5.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndInfiniteFunctionWithAllowed() {
        UnivariateRealFunction infFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.POSITIVE_INFINITY;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, infFunc, 0.0, 5.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNegativeMaxEvaluationsWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(-1, func, 0.0, 5.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullAllowedSolutionWithStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, func, 0.0, 5.0, 2.0, null);
                fail("Expected NullPointerException for method: " + method);
            } catch (NullPointerException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueOutsideIntervalWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 10.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMinWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 0.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMaxWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 5.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtRootWithAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndTolerancesWithAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        double[] tolerances = {1e-3, 1e-6, 1e-9};
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            for (double tol : tolerances) {
                BaseSecantSolver solver;
                switch (method) {
                    case REGULA_FALSI:
                        solver = new RegulaFalsiSolver(tol, tol, tol);
                        break;
                    case ILLINOIS:
                        solver = new IllinoisSolver(tol, tol, tol);
                        break;
                    case PEGASUS:
                        solver = new PegasusSolver(tol, tol, tol);
                        break;
                    default:
                        throw new AssertionError("Unknown method: " + method);
                }
                for (AllowedSolution allowed : AllowedSolution.values()) {
                    double result = solver.solve(1000, func, 0.0, 5.0, 3.0, allowed);
                    assertTrue("Method: " + method + ", tol: " + tol + ", allowed: " + allowed,
                               Math.abs(result - 2.0) < tol * 10);
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNoRootWithAllowedAndStart() {
        UnivariateRealFunction func = new NoRealRootFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, -2.0, 2.0, 0.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtBoundWithAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 2.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtMaxBoundWithAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 2.0, 1.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtStartValueWithAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndFunctionValueAccuracyWithAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-6, 1e-3);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-6, 1e-3);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-6, 1e-3);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndRelativeAccuracyWithAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-3, 1e-6, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-3, 1e-6, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-3, 1e-6, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndAbsoluteAccuracyWithAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-3, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-3, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-3, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsWithAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(10, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsTooLowWithAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNonBracketingWithAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 0.0, 1.0, 0.5, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullFunctionWithAllowedAndStart() {
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, null, 0.0, 5.0, 3.0, allowed);
                    fail("Expected NullPointerException for method: " + method + ", allowed: " + allowed);
                } catch (NullPointerException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMinEqualsMaxWithAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 2.0, 2.0, 2.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNaNFunctionWithAllowedAndStart() {
        UnivariateRealFunction nanFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.NaN;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, nanFunc, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndInfiniteFunctionWithAllowedAndStart() {
        UnivariateRealFunction infFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.POSITIVE_INFINITY;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, infFunc, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNegativeMaxEvaluationsWithAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(-1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullAllowedSolutionWithStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, func, 0.0, 5.0, 3.0, null);
                fail("Expected NullPointerException for method: " + method);
            } catch (NullPointerException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueOutsideIntervalWithAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 10.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMinWithAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 0.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMaxWithAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 5.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtRootWithAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndTolerancesWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        double[] tolerances = {1e-3, 1e-6, 1e-9};
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            for (double tol : tolerances) {
                BaseSecantSolver solver;
                switch (method) {
                    case REGULA_FALSI:
                        solver = new RegulaFalsiSolver(tol, tol, tol);
                        break;
                    case ILLINOIS:
                        solver = new IllinoisSolver(tol, tol, tol);
                        break;
                    case PEGASUS:
                        solver = new PegasusSolver(tol, tol, tol);
                        break;
                    default:
                        throw new AssertionError("Unknown method: " + method);
                }
                for (AllowedSolution allowed : AllowedSolution.values()) {
                    double result = solver.solve(1000, func, 0.0, 5.0, 3.0, allowed);
                    assertTrue("Method: " + method + ", tol: " + tol + ", allowed: " + allowed,
                               Math.abs(result - 2.0) < tol * 10);
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNoRootWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new NoRealRootFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, -2.0, 2.0, 0.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtBoundWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 2.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtMaxBoundWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 2.0, 1.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtStartValueWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndFunctionValueAccuracyWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-6, 1e-3);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-6, 1e-3);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-6, 1e-3);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndRelativeAccuracyWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-3, 1e-6, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-3, 1e-6, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-3, 1e-6, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndAbsoluteAccuracyWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-3, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-3, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-3, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(10, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsTooLowWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNonBracketingWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 0.0, 1.0, 0.5, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullFunctionWithAllowedAndStartAndAllowed() {
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, null, 0.0, 5.0, 3.0, allowed);
                    fail("Expected NullPointerException for method: " + method + ", allowed: " + allowed);
                } catch (NullPointerException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMinEqualsMaxWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 2.0, 2.0, 2.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNaNFunctionWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction nanFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.NaN;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, nanFunc, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndInfiniteFunctionWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction infFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.POSITIVE_INFINITY;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, infFunc, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNegativeMaxEvaluationsWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(-1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullAllowedSolutionWithStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, func, 0.0, 5.0, 3.0, null);
                fail("Expected NullPointerException for method: " + method);
            } catch (NullPointerException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueOutsideIntervalWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 10.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMinWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 0.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMaxWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 5.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtRootWithAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndTolerancesWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        double[] tolerances = {1e-3, 1e-6, 1e-9};
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            for (double tol : tolerances) {
                BaseSecantSolver solver;
                switch (method) {
                    case REGULA_FALSI:
                        solver = new RegulaFalsiSolver(tol, tol, tol);
                        break;
                    case ILLINOIS:
                        solver = new IllinoisSolver(tol, tol, tol);
                        break;
                    case PEGASUS:
                        solver = new PegasusSolver(tol, tol, tol);
                        break;
                    default:
                        throw new AssertionError("Unknown method: " + method);
                }
                for (AllowedSolution allowed : AllowedSolution.values()) {
                    double result = solver.solve(1000, func, 0.0, 5.0, 3.0, allowed);
                    assertTrue("Method: " + method + ", tol: " + tol + ", allowed: " + allowed,
                               Math.abs(result - 2.0) < tol * 10);
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNoRootWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new NoRealRootFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, -2.0, 2.0, 0.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtBoundWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 2.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtMaxBoundWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 2.0, 1.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtStartValueWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndFunctionValueAccuracyWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-6, 1e-3);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-6, 1e-3);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-6, 1e-3);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndRelativeAccuracyWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-3, 1e-6, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-3, 1e-6, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-3, 1e-6, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndAbsoluteAccuracyWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-3, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-3, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-3, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(10, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsTooLowWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNonBracketingWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 0.0, 1.0, 0.5, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullFunctionWithAllowedAndStartAndAllowedAndStart() {
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, null, 0.0, 5.0, 3.0, allowed);
                    fail("Expected NullPointerException for method: " + method + ", allowed: " + allowed);
                } catch (NullPointerException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMinEqualsMaxWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 2.0, 2.0, 2.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNaNFunctionWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction nanFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.NaN;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, nanFunc, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndInfiniteFunctionWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction infFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.POSITIVE_INFINITY;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, infFunc, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNegativeMaxEvaluationsWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(-1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullAllowedSolutionWithStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, func, 0.0, 5.0, 3.0, null);
                fail("Expected NullPointerException for method: " + method);
            } catch (NullPointerException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueOutsideIntervalWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 10.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMinWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 0.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMaxWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 5.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtRootWithAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndTolerancesWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        double[] tolerances = {1e-3, 1e-6, 1e-9};
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            for (double tol : tolerances) {
                BaseSecantSolver solver;
                switch (method) {
                    case REGULA_FALSI:
                        solver = new RegulaFalsiSolver(tol, tol, tol);
                        break;
                    case ILLINOIS:
                        solver = new IllinoisSolver(tol, tol, tol);
                        break;
                    case PEGASUS:
                        solver = new PegasusSolver(tol, tol, tol);
                        break;
                    default:
                        throw new AssertionError("Unknown method: " + method);
                }
                for (AllowedSolution allowed : AllowedSolution.values()) {
                    double result = solver.solve(1000, func, 0.0, 5.0, 3.0, allowed);
                    assertTrue("Method: " + method + ", tol: " + tol + ", allowed: " + allowed,
                               Math.abs(result - 2.0) < tol * 10);
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNoRootWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new NoRealRootFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, -2.0, 2.0, 0.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtBoundWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 2.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtMaxBoundWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 2.0, 1.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtStartValueWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndFunctionValueAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-6, 1e-3);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-6, 1e-3);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-6, 1e-3);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndRelativeAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-3, 1e-6, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-3, 1e-6, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-3, 1e-6, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndAbsoluteAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-3, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-3, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-3, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(10, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsTooLowWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNonBracketingWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 0.0, 1.0, 0.5, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullFunctionWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, null, 0.0, 5.0, 3.0, allowed);
                    fail("Expected NullPointerException for method: " + method + ", allowed: " + allowed);
                } catch (NullPointerException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMinEqualsMaxWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 2.0, 2.0, 2.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNaNFunctionWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction nanFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.NaN;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, nanFunc, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndInfiniteFunctionWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction infFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.POSITIVE_INFINITY;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, infFunc, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNegativeMaxEvaluationsWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(-1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullAllowedSolutionWithStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, func, 0.0, 5.0, 3.0, null);
                fail("Expected NullPointerException for method: " + method);
            } catch (NullPointerException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueOutsideIntervalWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 10.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMinWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 0.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMaxWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 5.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtRootWithAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndTolerancesWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        double[] tolerances = {1e-3, 1e-6, 1e-9};
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            for (double tol : tolerances) {
                BaseSecantSolver solver;
                switch (method) {
                    case REGULA_FALSI:
                        solver = new RegulaFalsiSolver(tol, tol, tol);
                        break;
                    case ILLINOIS:
                        solver = new IllinoisSolver(tol, tol, tol);
                        break;
                    case PEGASUS:
                        solver = new PegasusSolver(tol, tol, tol);
                        break;
                    default:
                        throw new AssertionError("Unknown method: " + method);
                }
                for (AllowedSolution allowed : AllowedSolution.values()) {
                    double result = solver.solve(1000, func, 0.0, 5.0, 3.0, allowed);
                    assertTrue("Method: " + method + ", tol: " + tol + ", allowed: " + allowed,
                               Math.abs(result - 2.0) < tol * 10);
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNoRootWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new NoRealRootFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, -2.0, 2.0, 0.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtBoundWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 2.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtMaxBoundWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 2.0, 1.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtStartValueWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndFunctionValueAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-6, 1e-3);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-6, 1e-3);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-6, 1e-3);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndRelativeAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-3, 1e-6, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-3, 1e-6, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-3, 1e-6, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndAbsoluteAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-3, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-3, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-3, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(10, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsTooLowWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNonBracketingWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 0.0, 1.0, 0.5, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullFunctionWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, null, 0.0, 5.0, 3.0, allowed);
                    fail("Expected NullPointerException for method: " + method + ", allowed: " + allowed);
                } catch (NullPointerException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMinEqualsMaxWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 2.0, 2.0, 2.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNaNFunctionWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction nanFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.NaN;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, nanFunc, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndInfiniteFunctionWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction infFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.POSITIVE_INFINITY;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, infFunc, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNegativeMaxEvaluationsWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(-1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullAllowedSolutionWithStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, func, 0.0, 5.0, 3.0, null);
                fail("Expected NullPointerException for method: " + method);
            } catch (NullPointerException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueOutsideIntervalWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 10.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMinWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 0.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMaxWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 5.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtRootWithAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndTolerancesWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        double[] tolerances = {1e-3, 1e-6, 1e-9};
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            for (double tol : tolerances) {
                BaseSecantSolver solver;
                switch (method) {
                    case REGULA_FALSI:
                        solver = new RegulaFalsiSolver(tol, tol, tol);
                        break;
                    case ILLINOIS:
                        solver = new IllinoisSolver(tol, tol, tol);
                        break;
                    case PEGASUS:
                        solver = new PegasusSolver(tol, tol, tol);
                        break;
                    default:
                        throw new AssertionError("Unknown method: " + method);
                }
                for (AllowedSolution allowed : AllowedSolution.values()) {
                    double result = solver.solve(1000, func, 0.0, 5.0, 3.0, allowed);
                    assertTrue("Method: " + method + ", tol: " + tol + ", allowed: " + allowed,
                               Math.abs(result - 2.0) < tol * 10);
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNoRootWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new NoRealRootFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, -2.0, 2.0, 0.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtBoundWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 2.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtMaxBoundWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 2.0, 1.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtStartValueWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndFunctionValueAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-6, 1e-3);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-6, 1e-3);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-6, 1e-3);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndRelativeAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-3, 1e-6, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-3, 1e-6, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-3, 1e-6, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndAbsoluteAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-3, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-3, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-3, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(10, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsTooLowWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNonBracketingWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 0.0, 1.0, 0.5, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullFunctionWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, null, 0.0, 5.0, 3.0, allowed);
                    fail("Expected NullPointerException for method: " + method + ", allowed: " + allowed);
                } catch (NullPointerException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMinEqualsMaxWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 2.0, 2.0, 2.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNaNFunctionWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction nanFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.NaN;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, nanFunc, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndInfiniteFunctionWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction infFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.POSITIVE_INFINITY;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, infFunc, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNegativeMaxEvaluationsWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(-1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullAllowedSolutionWithStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, func, 0.0, 5.0, 3.0, null);
                fail("Expected NullPointerException for method: " + method);
            } catch (NullPointerException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueOutsideIntervalWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 10.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMinWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 0.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMaxWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 5.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtRootWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndTolerancesWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        double[] tolerances = {1e-3, 1e-6, 1e-9};
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            for (double tol : tolerances) {
                BaseSecantSolver solver;
                switch (method) {
                    case REGULA_FALSI:
                        solver = new RegulaFalsiSolver(tol, tol, tol);
                        break;
                    case ILLINOIS:
                        solver = new IllinoisSolver(tol, tol, tol);
                        break;
                    case PEGASUS:
                        solver = new PegasusSolver(tol, tol, tol);
                        break;
                    default:
                        throw new AssertionError("Unknown method: " + method);
                }
                for (AllowedSolution allowed : AllowedSolution.values()) {
                    double result = solver.solve(1000, func, 0.0, 5.0, 3.0, allowed);
                    assertTrue("Method: " + method + ", tol: " + tol + ", allowed: " + allowed,
                               Math.abs(result - 2.0) < tol * 10);
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNoRootWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new NoRealRootFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, -2.0, 2.0, 0.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtBoundWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 2.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtMaxBoundWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 2.0, 1.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtStartValueWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndFunctionValueAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-6, 1e-3);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-6, 1e-3);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-6, 1e-3);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndRelativeAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-3, 1e-6, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-3, 1e-6, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-3, 1e-6, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndAbsoluteAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-3, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-3, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-3, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(10, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsTooLowWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNonBracketingWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 0.0, 1.0, 0.5, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullFunctionWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, null, 0.0, 5.0, 3.0, allowed);
                    fail("Expected NullPointerException for method: " + method + ", allowed: " + allowed);
                } catch (NullPointerException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMinEqualsMaxWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 2.0, 2.0, 2.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNaNFunctionWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction nanFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.NaN;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, nanFunc, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndInfiniteFunctionWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction infFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.POSITIVE_INFINITY;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, infFunc, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNegativeMaxEvaluationsWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(-1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullAllowedSolutionWithStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, func, 0.0, 5.0, 3.0, null);
                fail("Expected NullPointerException for method: " + method);
            } catch (NullPointerException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueOutsideIntervalWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 10.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMinWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 0.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMaxWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 5.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtRootWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndTolerancesWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        double[] tolerances = {1e-3, 1e-6, 1e-9};
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            for (double tol : tolerances) {
                BaseSecantSolver solver;
                switch (method) {
                    case REGULA_FALSI:
                        solver = new RegulaFalsiSolver(tol, tol, tol);
                        break;
                    case ILLINOIS:
                        solver = new IllinoisSolver(tol, tol, tol);
                        break;
                    case PEGASUS:
                        solver = new PegasusSolver(tol, tol, tol);
                        break;
                    default:
                        throw new AssertionError("Unknown method: " + method);
                }
                for (AllowedSolution allowed : AllowedSolution.values()) {
                    double result = solver.solve(1000, func, 0.0, 5.0, 3.0, allowed);
                    assertTrue("Method: " + method + ", tol: " + tol + ", allowed: " + allowed,
                               Math.abs(result - 2.0) < tol * 10);
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNoRootWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new NoRealRootFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, -2.0, 2.0, 0.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtBoundWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 2.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtMaxBoundWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 2.0, 1.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtStartValueWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndFunctionValueAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-6, 1e-3);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-6, 1e-3);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-6, 1e-3);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndRelativeAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-3, 1e-6, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-3, 1e-6, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-3, 1e-6, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndAbsoluteAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-3, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-3, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-3, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(10, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsTooLowWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNonBracketingWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 0.0, 1.0, 0.5, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullFunctionWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, null, 0.0, 5.0, 3.0, allowed);
                    fail("Expected NullPointerException for method: " + method + ", allowed: " + allowed);
                } catch (NullPointerException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMinEqualsMaxWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 2.0, 2.0, 2.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNaNFunctionWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction nanFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.NaN;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, nanFunc, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndInfiniteFunctionWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction infFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.POSITIVE_INFINITY;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, infFunc, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNegativeMaxEvaluationsWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(-1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullAllowedSolutionWithStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            try {
                solver.solve(100, func, 0.0, 5.0, 3.0, null);
                fail("Expected NullPointerException for method: " + method);
            } catch (NullPointerException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueOutsideIntervalWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 10.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMinWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 0.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtMaxWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 5.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndStartValueAtRootWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowed() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndTolerancesWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        double[] tolerances = {1e-3, 1e-6, 1e-9};
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            for (double tol : tolerances) {
                BaseSecantSolver solver;
                switch (method) {
                    case REGULA_FALSI:
                        solver = new RegulaFalsiSolver(tol, tol, tol);
                        break;
                    case ILLINOIS:
                        solver = new IllinoisSolver(tol, tol, tol);
                        break;
                    case PEGASUS:
                        solver = new PegasusSolver(tol, tol, tol);
                        break;
                    default:
                        throw new AssertionError("Unknown method: " + method);
                }
                for (AllowedSolution allowed : AllowedSolution.values()) {
                    double result = solver.solve(1000, func, 0.0, 5.0, 3.0, allowed);
                    assertTrue("Method: " + method + ", tol: " + tol + ", allowed: " + allowed,
                               Math.abs(result - 2.0) < tol * 10);
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNoRootWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new NoRealRootFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, -2.0, 2.0, 0.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtBoundWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 2.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtMaxBoundWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 2.0, 1.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndExactRootAtStartValueWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 2.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 0.0);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndFunctionValueAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-6, 1e-3);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-6, 1e-3);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-6, 1e-3);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndRelativeAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-3, 1e-6, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-3, 1e-6, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-3, 1e-6, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndAbsoluteAccuracyWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver(1e-6, 1e-3, 1e-6);
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver(1e-6, 1e-3, 1e-6);
                    break;
                case PEGASUS:
                    solver = new PegasusSolver(1e-6, 1e-3, 1e-6);
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(100, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-3);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double result = solver.solve(10, func, 0.0, 5.0, 3.0, allowed);
                assertEquals("Method: " + method + ", allowed: " + allowed,
                             2.0, result, 1e-6);
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMaxEvaluationsTooLowWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(1, func, 0.0, 5.0, 3.0, allowed);
                    fail("Expected TooManyEvaluationsException for method: " + method + ", allowed: " + allowed);
                } catch (TooManyEvaluationsException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNonBracketingWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 0.0, 1.0, 0.5, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNullFunctionWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, null, 0.0, 5.0, 3.0, allowed);
                    fail("Expected NullPointerException for method: " + method + ", allowed: " + allowed);
                } catch (NullPointerException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndMinEqualsMaxWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction func = new LinearFunction();
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break;
                case ILLINOIS:
                    solver = new IllinoisSolver();
                    break;
                case PEGASUS:
                    solver = new PegasusSolver();
                    break;
                default:
                    throw new AssertionError("Unknown method: " + method);
            }
            for (AllowedSolution allowed : AllowedSolution.values()) {
                try {
                    solver.solve(100, func, 2.0, 2.0, 2.0, allowed);
                    fail("Expected IllegalArgumentException for method: " + method + ", allowed: " + allowed);
                } catch (IllegalArgumentException e) {
                    // expected
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testSolverWithAllMethodsAndNaNFunctionWithAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStartAndAllowedAndStart() {
        UnivariateRealFunction nanFunc = new UnivariateRealFunction() {
            @Override
            public double value(double x) {
                return Double.NaN;
            }
        };
        for (BaseSecantSolver.Method method : BaseSecantSolver.Method.values()) {
            BaseSecantSolver solver;
            switch (method) {
                case REGULA_FALSI:
                    solver = new RegulaFalsiSolver();
                    break