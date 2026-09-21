package org.apache.commons.math.analysis.solvers;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;

/* [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - solve(f, min, max) with bracketing endpoints (sign < 0)
 *   - solve(f, min, max, initial) with various initial guesses
 *   - solve(f, min, yMin, max, yMax, x2, y2) private method via public entry points
 *   - clearResult() and setResult() state management
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Endpoints where function value is exactly zero (yMin == 0.0, yMax == 0.0)
 *   - Endpoints where function value is within functionValueAccuracy (1E-6)
 *   - Initial guess equal to min or max
 *   - Very small intervals, large intervals
 *   - Tolerance boundaries (relativeAccuracy * |x1| vs absoluteAccuracy)
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - KNOWN DEFECT: testRootEndpoints - solve(f, min, max) when sign == 0 (yMin == 0.0)
 *     The bug: when yMin == 0.0, the method returns min correctly, but the test expects
 *     the root to be found at max (pi) for sin(x) on [3, 4]. The defect is that when
 *     yMin == 0.0, the method returns min immediately without checking if max is also a root.
 *     Actually, looking at the code: if sign == 0, it checks yMin == 0.0 first, returns min.
 *     The test expects the root at max (pi ≈ 3.14159) when solving sin(x)=0 on [3,4].
 *     sin(3) ≈ 0.141 > 0, sin(4) ≈ -0.757 < 0, so sign < 0, not 0. The defect is actually
 *     in the solve(f, min, max) method: when sign > 0 and |yMin| <= functionValueAccuracy,
 *     it sets result to min but returns min. But the test expects the root at max.
 *     Wait - the test is sin(x) on [3,4], sin(3)=0.141, sin(4)=-0.757, so sign < 0.
 *     The test expects 3.141592653589793 (pi). The actual result is 1.2246467991473532E-16
 *     which is essentially 0. So the solver finds 0 instead of pi. This is because the
 *     initial guess handling in solve(f, min, max, initial) when initial is not provided
 *     defaults to using min as initial guess. The bug is in the private solve method's
 *     convergence check: when y1 is very small but not exactly zero, it should converge
 *     to the correct root, but the tolerance check or bisection logic may cause it to
 *     converge to the wrong root (0 instead of pi).
 *     Actually, looking more carefully: the test calls solve(min, max) which calls
 *     solve(f, min, max). For sin(x) on [3,4], sign < 0, so it calls
 *     solve(f, min, yMin, max, yMax, min, yMin). The initial guess is min=3.
 *     The private solve method should find the root near pi=3.14159.
 *     The bug is that when the function value at the initial guess is very small
 *     (but not within functionValueAccuracy), the algorithm may converge to 0
 *     instead of the actual root. This is a known Defects4J bug where the solver
 *     doesn't properly handle the case when the initial guess is close to but not
 *     exactly at the root, and the function has another root at 0.
 *     Actually, the real defect: in solve(f, min, max), when sign < 0, it calls
 *     solve(f, min, yMin, max, yMax, min, yMin). But the private solve method
 *     has a bug: when x0 == x2 (both are min), the linear interpolation formula
 *     may produce incorrect results. The fix should ensure proper bracketing.
 *     The test expects pi, but gets ~0 (1.22e-16). This is because the solver
 *     converges to 0 (another root of sin(x)) instead of pi.
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Non-bracketing interval (sign > 0) throws IllegalArgumentException
 *   - Invalid sequence (initial not between min and max) throws IllegalArgumentException
 *   - Max iterations exceeded
 *   - Function evaluation exceptions
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Constructor with and without function
 *   - Deprecated methods still functional
 *   - Solver state after multiple calls
 */
public class BrentSolverDeepseekTest {

    private static final double EPSILON = 1e-10;
    private static final double FUNCTION_VALUE_ACCURACY = 1e-6;

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testSolveBracketingIntervalLinear() throws Exception {
        // f(x) = x - 2, root at x = 2
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 2.0; }
        };
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, 0.0, 5.0);
        assertEquals(2.0, result, EPSILON);
        assertTrue(solver.getFunctionValue() <= FUNCTION_VALUE_ACCURACY);
    }

    @Test(timeout = 4000)
    public void testSolveBracketingIntervalQuadratic() throws Exception {
        // f(x) = (x-3)*(x+1), roots at x = -1 and x = 3
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return (x - 3.0) * (x + 1.0); }
        };
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, 2.0, 4.0);
        assertEquals(3.0, result, EPSILON);
    }

    @Test(timeout = 4000)
    public void testSolveWithInitialGuess() throws Exception {
        // f(x) = x^2 - 4, roots at x = -2 and x = 2
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x * x - 4.0; }
        };
        BrentSolver solver = new BrentSolver();
        // Initial guess near the positive root
        double result = solver.solve(f, 0.0, 5.0, 3.0);
        assertEquals(2.0, result, EPSILON);
    }

    @Test(timeout = 4000)
    public void testSolveWithInitialGuessNearRoot() throws Exception {
        // f(x) = sin(x), root at pi ≈ 3.14159
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return Math.sin(x); }
        };
        BrentSolver solver = new BrentSolver();
        // Initial guess very close to pi
        double result = solver.solve(f, 3.0, 4.0, 3.14);
        assertEquals(Math.PI, result, 1e-4);
    }

    @Test(timeout = 4000)
    public void testSolveMultipleCallsStateReset() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 5.0; }
        };
        BrentSolver solver = new BrentSolver();
        
        double result1 = solver.solve(f, 0.0, 10.0);
        assertEquals(5.0, result1, EPSILON);
        
        // Second call with different function
        UnivariateRealFunction f2 = new UnivariateRealFunction() {
            public double value(double x) { return x + 3.0; }
        };
        double result2 = solver.solve(f2, -5.0, 0.0);
        assertEquals(-3.0, result2, EPSILON);
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testSolveEndpointIsExactRootMin() throws Exception {
        // f(x) = x, root at x = 0, min = 0 is exact root
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x; }
        };
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, 0.0, 5.0);
        assertEquals(0.0, result, EPSILON);
    }

    @Test(timeout = 4000)
    public void testSolveEndpointIsExactRootMax() throws Exception {
        // f(x) = x - 5, root at x = 5, max = 5 is exact root
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 5.0; }
        };
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, 0.0, 5.0);
        assertEquals(5.0, result, EPSILON);
    }

    @Test(timeout = 4000)
    public void testSolveEndpointWithinAccuracyMin() throws Exception {
        // f(x) = x - 1e-7, value at min=0 is -1e-7, within functionValueAccuracy
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 1e-7; }
        };
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, 0.0, 1.0);
        // Should return min since |yMin| <= 1e-6
        assertEquals(0.0, result, EPSILON);
    }

    @Test(timeout = 4000)
    public void testSolveEndpointWithinAccuracyMax() throws Exception {
        // f(x) = x - 1 + 1e-7, value at max=1 is 1e-7, within functionValueAccuracy
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 1.0 + 1e-7; }
        };
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, 0.0, 1.0);
        // Should return max since |yMax| <= 1e-6
        assertEquals(1.0, result, EPSILON);
    }

    @Test(timeout = 4000)
    public void testSolveVerySmallInterval() throws Exception {
        // f(x) = x - 1e-10, root at 1e-10, very small interval
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 1e-10; }
        };
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, 0.0, 1e-5);
        assertEquals(1e-10, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveLargeInterval() throws Exception {
        // f(x) = x - 1000, root at 1000
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 1000.0; }
        };
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, -10000.0, 10000.0);
        assertEquals(1000.0, result, 1e-3);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Targets the known Defects4J defect: testRootEndpoints
     * For sin(x) on [3, 4], the solver should find the root at pi (≈3.14159),
     * but the defective version returns ~0 (1.22e-16).
     * 
     * The bug occurs in solve(f, min, max) when sign < 0, it calls
     * solve(f, min, yMin, max, yMax, min, yMin). The private solve method
     * has a convergence issue where it can converge to a different root (0)
     * instead of the bracketed root (pi).
     */
    @Test(timeout = 4000)
    public void testSolveSinOnThreeToFour() throws Exception {
        UnivariateRealFunction sin = new UnivariateRealFunction() {
            public double value(double x) { return Math.sin(x); }
        };
        BrentSolver solver = new BrentSolver();
        // sin(3) ≈ 0.141 > 0, sin(4) ≈ -0.757 < 0, so bracketing
        double result = solver.solve(sin, 3.0, 4.0);
        // Should find pi, not 0
        assertEquals(Math.PI, result, 1e-5);
        // The defective version returns ~0 (1.22e-16), which is NOT pi
        assertTrue("Solver should find root near pi, not near 0", 
                   Math.abs(result - Math.PI) < 1e-5);
    }

    /**
     * Additional test targeting the same defect with different function
     * where the solver might converge to the wrong root.
     */
    @Test(timeout = 4000)
    public void testSolveCosOnOneToTwo() throws Exception {
        // cos(x) has root at pi/2 ≈ 1.5708
        UnivariateRealFunction cos = new UnivariateRealFunction() {
            public double value(double x) { return Math.cos(x); }
        };
        BrentSolver solver = new BrentSolver();
        // cos(1) ≈ 0.54 > 0, cos(2) ≈ -0.416 < 0, bracketing
        double result = solver.solve(cos, 1.0, 2.0);
        assertEquals(Math.PI / 2.0, result, 1e-5);
    }

    /**
     * Test where initial guess is at one endpoint, which is the path
     * taken by solve(f, min, max) when sign < 0.
     */
    @Test(timeout = 4000)
    public void testSolveWithInitialAtEndpoint() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return (x - 2.0) * (x + 1.0); }
        };
        BrentSolver solver = new BrentSolver();
        // solve(f, min, max) will use min as initial guess
        double result = solver.solve(f, 1.0, 3.0);
        assertEquals(2.0, result, EPSILON);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveNonBracketingInterval() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x * x + 1.0; } // always positive
        };
        BrentSolver solver = new BrentSolver();
        solver.solve(f, -1.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveInvalidSequence() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 2.0; }
        };
        BrentSolver solver = new BrentSolver();
        // initial = 5 is not between min=0 and max=3
        solver.solve(f, 0.0, 3.0, 5.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveMinGreaterThanMax() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 2.0; }
        };
        BrentSolver solver = new BrentSolver();
        solver.solve(f, 5.0, 0.0);
    }

    @Test(timeout = 4000)
    public void testSolveNonBracketingButEndpointWithinAccuracy() throws Exception {
        // f(x) = x - 1e-7, on [0, 10], both endpoints positive, but min is within accuracy
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 1e-7; }
        };
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, 0.0, 10.0);
        assertEquals(0.0, result, EPSILON);
    }

    @Test(timeout = 4000)
    public void testSolveNonBracketingMaxWithinAccuracy() throws Exception {
        // f(x) = x - 10 + 1e-7, on [0, 10], max value within accuracy
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 10.0 + 1e-7; }
        };
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, 0.0, 10.0);
        assertEquals(10.0, result, EPSILON);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveNonBracketingNeitherEndpointClose() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x * x + 1.0; }
        };
        BrentSolver solver = new BrentSolver();
        solver.solve(f, -10.0, 10.0);
    }

    @Test(timeout = 4000)
    public void testSolveWithInitialGuessInvalidSequence() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 2.0; }
        };
        BrentSolver solver = new BrentSolver();
        try {
            solver.solve(f, 0.0, 5.0, -1.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testConstructorWithFunction() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 1.0; }
        };
        BrentSolver solver = new BrentSolver(f);
        assertNotNull(solver);
        // Deprecated method should still work
        double result = solver.solve(0.0, 2.0);
        assertEquals(1.0, result, EPSILON);
    }

    @Test(timeout = 4000)
    public void testConstructorDefault() throws Exception {
        BrentSolver solver = new BrentSolver();
        assertNotNull(solver);
        assertEquals(100, solver.getMaximalIterationCount());
        assertEquals(1E-6, solver.getFunctionValueAccuracy(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testDeprecatedSolveWithInitial() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 3.0; }
        };
        BrentSolver solver = new BrentSolver(f);
        double result = solver.solve(0.0, 5.0, 2.0);
        assertEquals(3.0, result, EPSILON);
    }

    @Test(timeout = 4000)
    public void testResultStateAfterSolve() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 4.0; }
        };
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(f, 0.0, 10.0);
        assertEquals(4.0, solver.getResult(), EPSILON);
        assertTrue(solver.getFunctionValue() <= FUNCTION_VALUE_ACCURACY);
        assertTrue(solver.getIterationCount() >= 0);
    }

    @Test(timeout = 4000)
    public void testClearResult() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 4.0; }
        };
        BrentSolver solver = new BrentSolver();
        solver.solve(f, 0.0, 10.0);
        assertFalse(Double.isNaN(solver.getResult()));
        
        // After clearResult (called internally by solve), state is reset
        // We can verify by calling solve again with different function
        UnivariateRealFunction f2 = new UnivariateRealFunction() {
            public double value(double x) { return x + 2.0; }
        };
        double result = solver.solve(f2, -5.0, 0.0);
        assertEquals(-2.0, result, EPSILON);
    }

    @Test(timeout = 4000)
    public void testMaxIterationsExceeded() throws Exception {
        // Use a function that requires many iterations with tight tolerance
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { 
                // Function with very flat region near root
                return Math.pow(x - 1000.0, 3); 
            }
        };
        BrentSolver solver = new BrentSolver();
        // Set very low max iterations to force exception
        solver.setMaximalIterationCount(5);
        try {
            solver.solve(f, 0.0, 2000.0);
            fail("Expected MaxIterationsExceededException");
        } catch (MaxIterationsExceededException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFunctionEvaluationException() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            private int count = 0;
            public double value(double x) throws FunctionEvaluationException {
                count++;
                if (count > 3) {
                    throw new FunctionEvaluationException(x, "Test exception");
                }
                return x - 5.0;
            }
        };
        BrentSolver solver = new BrentSolver();
        try {
            solver.solve(f, 0.0, 10.0);
            fail("Expected FunctionEvaluationException");
        } catch (FunctionEvaluationException e) {
            // expected
        }
    }
}