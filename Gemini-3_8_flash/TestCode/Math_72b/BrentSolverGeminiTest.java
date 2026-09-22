package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.QuinticFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Class under test: BrentSolver (extends UnivariateRealSolverImpl)
 *
 * Targeted Decision Branches & Pathways:
 * 1. Deprecated Constructors & Methods:
 *    - BrentSolver(UnivariateRealFunction f) with solve(min, max) and solve(min, max, initial).
 *    - Default constructor BrentSolver() with solve(f, min, max) and solve(f, min, max, initial).
 * 2. Method solve(f, min, max, initial):
 *    - verifySequence(min, initial, max) failure (min >= initial or initial >= max).
 *    - |yInitial| <= functionValueAccuracy -> early return initial.
 *    - |yMin| <= functionValueAccuracy -> early return min (BUG in SUT: sets yMin instead of min).
 *    - (yInitial * yMin < 0) -> solve bracket [min, initial].
 *    - |yMax| <= functionValueAccuracy -> early return max (BUG in SUT: sets yMax instead of max).
 *    - (yInitial * yMax < 0) -> solve bracket [initial, max].
 *    - (yMin * yMax > 0) -> throws IllegalArgumentException (NON_BRACKETING_MESSAGE).
 *    - Fallback: solve(f, min, yMin, max, yMax, initial, yInitial).
 * 3. Method solve(f, min, max):
 *    - verifyInterval(min, max) failure (min >= max).
 *    - sign = yMin * yMax > 0:
 *      * |yMin| <= functionValueAccuracy -> return min.
 *      * |yMax| <= functionValueAccuracy -> return max.
 *      * Neither close to zero -> throws IllegalArgumentException (NON_BRACKETING_MESSAGE).
 *    - sign < 0:
 *      * Standard bracketing solve.
 *    - sign == 0:
 *      * yMin == 0.0 -> return min.
 *      * yMax == 0.0 -> return max.
 * 4. Method solve(f, x0, y0, x1, y1, x2, y2) [Internal Brent Algorithm Loop]:
 *    - |y2| < |y1| swap condition.
 *    - Convergence checks: |y1| <= functionValueAccuracy; |dx| <= tolerance.
 *    - Bisection forcing: |oldDelta| < tolerance OR |y0| <= |y1|.
 *    - Interpolation selection:
 *      * x0 == x2 -> Linear interpolation.
 *      * x0 != x2 -> Inverse quadratic interpolation.
 *    - Interpolation direction/bounds checks: fallback to bisection when bounds exceeded.
 *    - Delta adjustment: |delta| > tolerance vs. dx > 0.0 vs dx <= 0.0.
 *    - Bracket shift: (y1 > 0) == (y2 > 0).
 *    - Loop exhaustion: MaxIterationsExceededException.
 *
 * Known Defect (Defects4J):
 * - In solve(f, min, max, initial):
 *   Lines setting result when yMin or yMax is close to zero incorrectly use setResult(yMin, 0)
 *   and setResult(yMax, 0) instead of setResult(min, 0) and setResult(max, 0).
 *   When min is a root (e.g. pi for sin(x)), the solver returns ~1.22e-16 instead of ~3.14159.
 */
public class BrentSolverGeminiTest {

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testStandardSinZeroCrossing() throws Exception {
        UnivariateRealFunction sin = new SinFunction();
        BrentSolver solver = new BrentSolver();
        double result = solver.solve(sin, 3.0, 4.0);
        assertEquals(Math.PI, result, 1e-6);
        assertTrue(solver.getIterationCount() > 0);
    }

    @Test(timeout = 4000)
    public void testQuinticFunctionStandardSolve() throws Exception {
        UnivariateRealFunction quintic = new QuinticFunction();
        BrentSolver solver = new BrentSolver();
        // Roots are at 0, +-0.5, +-1.
        double result = solver.solve(quintic, 0.1, 0.8);
        assertEquals(0.5, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testInitialGuessCloseToZeroReturnsInitial() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 2.0;
            }
        };
        BrentSolver solver = new BrentSolver();
        solver.setFunctionValueAccuracy(1e-4);
        double result = solver.solve(f, 0.0, 5.0, 2.00001);
        assertEquals(2.00001, result, 1e-9);
        assertEquals(0, solver.getIterationCount());
    }

    @Test(timeout = 4000)
    public void testSolveWithInitialBracketingMinAndInitial() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.5;
            }
        };
        BrentSolver solver = new BrentSolver();
        // min=1.0 (f(1.0)=-0.5), initial=2.0 (f(2.0)=0.5), max=3.0 (f(3.0)=1.5)
        // yInitial * yMin < 0 -> interval [1.0, 2.0]
        double result = solver.solve(f, 1.0, 3.0, 2.0);
        assertEquals(1.5, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveWithInitialBracketingInitialAndMax() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 2.5;
            }
        };
        BrentSolver solver = new BrentSolver();
        // min=1.0 (f(1.0)=-1.5), initial=2.0 (f(2.0)=-0.5), max=3.0 (f(3.0)=0.5)
        // yInitial * yMax < 0 -> interval [2.0, 3.0]
        double result = solver.solve(f, 1.0, 3.0, 2.0);
        assertEquals(2.5, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testDeprecatedConstructorAndSolve() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        @SuppressWarnings("deprecation")
        BrentSolver solver = new BrentSolver(f);
        @SuppressWarnings("deprecation")
        double result = solver.solve(3.0, 4.0);
        assertEquals(Math.PI, result, 1e-6);

        @SuppressWarnings("deprecation")
        double resultWithInitial = solver.solve(3.0, 4.0, 3.2);
        assertEquals(Math.PI, resultWithInitial, 1e-6);
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testExactZeroAtMinEndpointTwoArgs() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x == 2.0 ? 0.0 : x - 2.0;
            }
        };
        BrentSolver solver = new BrentSolver();
        // yMin == 0.0
        double result = solver.solve(f, 2.0, 5.0);
        assertEquals(2.0, result, 1e-12);
        assertEquals(0, solver.getIterationCount());
    }

    @Test(timeout = 4000)
    public void testExactZeroAtMaxEndpointTwoArgs() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x == 5.0 ? 0.0 : x - 5.0;
            }
        };
        BrentSolver solver = new BrentSolver();
        // yMin != 0, yMax == 0.0
        double result = solver.solve(f, 1.0, 5.0);
        assertEquals(5.0, result, 1e-12);
        assertEquals(0, solver.getIterationCount());
    }

    @Test(timeout = 4000)
    public void testNearZeroAtMinEndpointSameSignTwoArgs() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 1.0) * (x - 1.0) + 1e-12; // always positive, near zero at 1.0
            }
        };
        BrentSolver solver = new BrentSolver();
        solver.setFunctionValueAccuracy(1e-8);
        // yMin and yMax both positive, but |yMin| <= functionValueAccuracy
        double result = solver.solve(f, 1.0, 3.0);
        assertEquals(1.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testNearZeroAtMaxEndpointSameSignTwoArgs() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x - 3.0) * (x - 3.0) + 1e-12; // always positive, near zero at 3.0
            }
        };
        BrentSolver solver = new BrentSolver();
        solver.setFunctionValueAccuracy(1e-8);
        // yMin and yMax both positive, but |yMax| <= functionValueAccuracy
        double result = solver.solve(f, 1.0, 3.0);
        assertEquals(3.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testInverseQuadraticInterpolationBranch() throws Exception {
        // Function where x0 != x2 in the 7-arg solve loop
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return (x + 3.0) * (x - 1.0) * (x - 2.0);
            }
        };
        BrentSolver solver = new BrentSolver();
        // Bracketing [-4.0, 0.0] with initial guess -2.0
        // Root is at -3.0
        double result = solver.solve(f, -4.0, 0.0, -2.0);
        assertEquals(-3.0, result, 1e-6);
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    /**
     * TARGET DEFECT:
     * In BrentSolver.java:
     * line 106: setResult(yMin, 0); instead of setResult(min, 0);
     * line 118: setResult(yMax, 0); instead of setResult(max, 0);
     *
     * In the defective implementation:
     * solve(sin, Math.PI, 4.0, 3.5) evaluated sin(Math.PI) which was ~1.22e-16 (<= accuracy),
     * and instead of returning Math.PI, it returned yMin (~1.22e-16).
     */
    @Test(timeout = 4000)
    public void testRootAtMinEndpointWithInitial() throws Exception {
        UnivariateRealFunction sin = new SinFunction();
        BrentSolver solver = new BrentSolver();
        // Math.PI is a root of SinFunction, sin(Math.PI) is ~ 1.2246467991473532E-16
        // functionValueAccuracy defaults to 1e-15, which is > 1.22e-16
        // Thus |yMin| <= functionValueAccuracy triggers!
        // Correct behavior: returns Math.PI
        // Defective behavior: returns 1.2246467991473532E-16
        double result = solver.solve(sin, Math.PI, 4.0, 3.5);
        assertEquals(Math.PI, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testRootAtMaxEndpointWithInitial() throws Exception {
        UnivariateRealFunction sin = new SinFunction();
        BrentSolver solver = new BrentSolver();
        // Math.PI is at max endpoint; min = 2.0, max = Math.PI, initial = 2.5
        // Both yInitial (sin(2.5)>0) and yMin (sin(2.0)>0) are positive.
        // |yMax| = |sin(Math.PI)| <= 1e-15
        // Correct behavior: returns Math.PI
        // Defective behavior: returns 1.2246467991473532E-16
        double result = solver.solve(sin, 2.0, Math.PI, 2.5);
        assertEquals(Math.PI, result, 1e-6);
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidIntervalTwoArgsMinGreaterThanMax() throws Exception {
        UnivariateRealFunction sin = new SinFunction();
        BrentSolver solver = new BrentSolver();
        solver.solve(sin, 4.0, 2.0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidIntervalTwoArgsMinEqualsMax() throws Exception {
        UnivariateRealFunction sin = new SinFunction();
        BrentSolver solver = new BrentSolver();
        solver.solve(sin, 2.0, 2.0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNonBracketingTwoArgsThrowsException() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0; // always >= 1.0
            }
        };
        BrentSolver solver = new BrentSolver();
        solver.solve(f, 1.0, 3.0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidSequenceThreeArgsMinGreaterInitial() throws Exception {
        UnivariateRealFunction sin = new SinFunction();
        BrentSolver solver = new BrentSolver();
        solver.solve(sin, 3.0, 4.0, 2.5);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidSequenceThreeArgsInitialGreaterMax() throws Exception {
        UnivariateRealFunction sin = new SinFunction();
        BrentSolver solver = new BrentSolver();
        solver.solve(sin, 3.0, 4.0, 4.5);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNonBracketingThreeArgsThrowsException() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 2.0; // always positive
            }
        };
        BrentSolver solver = new BrentSolver();
        solver.solve(f, 1.0, 3.0, 2.0);
    }

    @Test(timeout = 4000, expected = MaxIterationsExceededException.class)
    public void testMaxIterationsExceeded() throws Exception {
        UnivariateRealFunction f = new QuinticFunction();
        BrentSolver solver = new BrentSolver();
        solver.setMaximalIterationCount(1);
        solver.setAbsoluteAccuracy(1e-15);
        solver.setRelativeAccuracy(1e-15);
        // Should throw MaxIterationsExceededException
        solver.solve(f, 0.2, 0.8);
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSolverDefaultConfiguration() {
        BrentSolver solver = new BrentSolver();
        assertEquals(100, solver.getMaximalIterationCount());
        assertEquals(1e-6, solver.getAbsoluteAccuracy(), 1e-12);
        assertEquals(1e-14, solver.getRelativeAccuracy(), 1e-16);
        assertEquals(1e-15, solver.getFunctionValueAccuracy(), 1e-17);
    }

    @Test(timeout = 4000)
    public void testAccuracyAdjustmentAndStateReset() throws Exception {
        BrentSolver solver = new BrentSolver();
        solver.setAbsoluteAccuracy(1e-10);
        solver.setRelativeAccuracy(1e-10);
        solver.setFunctionValueAccuracy(1e-10);
        assertEquals(1e-10, solver.getAbsoluteAccuracy(), 1e-15);
        assertEquals(1e-10, solver.getRelativeAccuracy(), 1e-15);
        assertEquals(1e-10, solver.getFunctionValueAccuracy(), 1e-15);

        UnivariateRealFunction sin = new SinFunction();
        double res = solver.solve(sin, 3.0, 3.5);
        assertEquals(Math.PI, res, 1e-10);
        assertEquals(res, solver.getResult(), 1e-15);
        assertTrue(solver.getIterationCount() > 0);
    }
}