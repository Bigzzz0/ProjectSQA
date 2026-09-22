package org.apache.commons.math.analysis.solvers;

import static org.junit.Assert.*;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 *
 * Target: org.apache.commons.math.analysis.solvers.BisectionSolver
 *
 * 1. Defect MATH-369 Analysis:
 *    - Method: solve(UnivariateRealFunction f, double min, double max, double initial)
 *    - Defect: Calls solve(min, max) instead of solve(f, min, max). When solver is instantiated
 *      via the default constructor (new BisectionSolver()), the instance field `f` is null,
 *      resulting in a java.lang.NullPointerException during evaluation.
 *
 * 2. Decision Branches & Boundary Conditions:
 *    - Constructors:
 *      * Deprecated: BisectionSolver(UnivariateRealFunction)
 *      * Default: BisectionSolver()
 *    - Interval Verification:
 *      * min < max (valid)
 *      * min >= max (verifyInterval throws IllegalArgumentException)
 *    - Iteration Logic in solve(f, min, max):
 *      * Branch fm * fmin > 0.0 (root lies in upper half, min = m)
 *      * Branch fm * fmin <= 0.0 (root lies in lower half, max = m)
 *      * Convergence check: Math.abs(max - min) <= absoluteAccuracy (termination with setResult)
 *      * Exceeded iteration count: loop finishes without convergence, throws MaxIterationsExceededException
 *    - Deprecated Solver Delegation:
 *      * solve(min, max, initial) delegates to solve(f, min, max)
 *      * solve(min, max) delegates to solve(f, min, max)
 *      * solve(f, min, max, initial) intended to solve for f within [min, max]
 *    - Accuracy Boundaries:
 *      * Default absolute accuracy (1E-6)
 *      * Custom absolute accuracy (e.g., 1E-12, 1E-2)
 *    - Functional Extremes:
 *      * Exact root at midpoint on first iteration
 *      * Exact root at upper/lower boundary
 *      * Monotonic increasing / decreasing / non-smooth step functions
 */
public class BisectionSolverGeminiTest {

    // Simple continuous linear function: f(x) = x - 1
    private final UnivariateRealFunction linearFunc = new UnivariateRealFunction() {
        public double value(double x) {
            return x - 1.0;
        }
    };

    // Quadratic function: f(x) = x^2 - 4 (roots at -2 and 2)
    private final UnivariateRealFunction quadFunc = new UnivariateRealFunction() {
        public double value(double x) {
            return (x * x) - 4.0;
        }
    };

    // Sine-like function: f(x) = sin(x)
    private final UnivariateRealFunction sinFunc = new UnivariateRealFunction() {
        public double value(double x) {
            return Math.sin(x);
        }
    };

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-369)
    // =========================================================================

    /**
     * Targets Defects4J bug MATH-369:
     * When using the default constructor BisectionSolver(), passing the function
     * to solve(f, min, max, initial) causes a NullPointerException in defective
     * versions because it erroneously calls solve(min, max) instead of solve(f, min, max).
     */
    @Test(timeout = 4000)
    public void testMath369() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.PI * x - 1.0;
            }
        };

        // If bug MATH-369 is present, this throws NullPointerException
        double min = 0.0;
        double max = 1.0;
        double initial = 0.5;
        double expected = 1.0 / Math.PI;

        double result = solver.solve(f, min, max, initial);
        assertEquals(expected, result, solver.getAbsoluteAccuracy());
    }

    /**
     * Targets MATH-369 with different boundary functions using the 4-arg solve method.
     */
    @Test(timeout = 4000)
    public void testMath369Quadratic() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        double result = solver.solve(quadFunc, 0.0, 5.0, 2.5);
        assertEquals(2.0, result, solver.getAbsoluteAccuracy());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSolveWithDefaultConstructorTwoArgs() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        double root = solver.solve(linearFunc, 0.0, 2.0);
        assertEquals(1.0, root, solver.getAbsoluteAccuracy());
        assertEquals(1.0, solver.getResult(), solver.getAbsoluteAccuracy());
        assertTrue(solver.getIterationCount() > 0);
    }

    @Test(timeout = 4000)
    public void testSolveWithDeprecatedConstructor() throws Exception {
        @SuppressWarnings("deprecation")
        BisectionSolver solver = new BisectionSolver(linearFunc);

        double root = solver.solve(0.0, 2.0);
        assertEquals(1.0, root, solver.getAbsoluteAccuracy());
        assertEquals(1.0, solver.getResult(), solver.getAbsoluteAccuracy());
    }

    @Test(timeout = 4000)
    public void testSolveWithDeprecatedConstructorAndInitial() throws Exception {
        @SuppressWarnings("deprecation")
        BisectionSolver solver = new BisectionSolver(linearFunc);

        double root = solver.solve(0.0, 2.0, 0.5);
        assertEquals(1.0, root, solver.getAbsoluteAccuracy());
    }

    @Test(timeout = 4000)
    public void testBranchRootInUpperInterval() throws Exception {
        // f(x) = x - 3.5 in [0, 4]
        // midpoint initially = 2.0. f(min) = -3.5, f(m) = -1.5 -> fm * fmin > 0
        // Forces branch: min = m (upper bracket)
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 3.5;
            }
        };

        BisectionSolver solver = new BisectionSolver();
        double root = solver.solve(f, 0.0, 4.0);
        assertEquals(3.5, root, solver.getAbsoluteAccuracy());
    }

    @Test(timeout = 4000)
    public void testBranchRootInLowerInterval() throws Exception {
        // f(x) = x - 0.5 in [0, 4]
        // midpoint initially = 2.0. f(min) = -0.5, f(m) = 1.5 -> fm * fmin <= 0
        // Forces branch: max = m (lower bracket)
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 0.5;
            }
        };

        BisectionSolver solver = new BisectionSolver();
        double root = solver.solve(f, 0.0, 4.0);
        assertEquals(0.5, root, solver.getAbsoluteAccuracy());
    }

    @Test(timeout = 4000)
    public void testExactZeroAtMidpoint() throws Exception {
        // f(x) = x in [-1, 1], midpoint is 0.0
        // fm = 0.0, so fm * fmin = 0 <= 0 -> max = m
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };

        BisectionSolver solver = new BisectionSolver();
        double root = solver.solve(f, -1.0, 1.0);
        assertEquals(0.0, root, solver.getAbsoluteAccuracy());
    }

    @Test(timeout = 4000)
    public void testClearResultBetweenInvocations() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        double root1 = solver.solve(quadFunc, 0.0, 3.0);
        assertEquals(2.0, root1, solver.getAbsoluteAccuracy());

        double root2 = solver.solve(quadFunc, -3.0, 0.0);
        assertEquals(-2.0, root2, solver.getAbsoluteAccuracy());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIntervalAlreadyWithinTolerance() throws Exception {
        // If max - min <= absoluteAccuracy from the start
        BisectionSolver solver = new BisectionSolver();
        solver.setAbsoluteAccuracy(1E-3);

        double min = 1.0000;
        double max = 1.0005; // |max - min| = 0.0005 < 1E-3
        double root = solver.solve(linearFunc, min, max);

        assertEquals(1.00025, root, 1E-6);
        assertEquals(0, solver.getIterationCount());
    }

    @Test(timeout = 4000)
    public void testHighPrecisionAccuracy() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        solver.setAbsoluteAccuracy(1E-12);
        solver.setMaximalIterationCount(100);

        double root = solver.solve(sinFunc, 3.0, 4.0);
        assertEquals(Math.PI, root, 1E-11);
    }

    @Test(timeout = 4000)
    public void testNegativeIntervalDomain() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x + 100.0;
            }
        };
        double root = solver.solve(f, -150.0, -50.0);
        assertEquals(-100.0, root, solver.getAbsoluteAccuracy());
    }

    @Test(timeout = 4000)
    public void testExtremeMagnitudeInterval() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.0E7;
            }
        };
        double root = solver.solve(f, 0.0, 2.0E7);
        assertEquals(1.0E7, root, 1.0);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinGreaterThanMaxThrowsException() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        solver.solve(linearFunc, 2.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinEqualsMaxThrowsException() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        solver.solve(linearFunc, 1.0, 1.0);
    }

    @Test(expected = MaxIterationsExceededException.class, timeout = 4000)
    public void testExceedMaxIterationsThrowsException() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        // Constrain iterations so it cannot converge on [0, 1000] with tolerance 1E-6
        solver.setMaximalIterationCount(2);
        solver.setAbsoluteAccuracy(1E-12);
        solver.solve(linearFunc, 0.0, 1000.0);
    }

    @Test(timeout = 4000)
    public void testFunctionEvaluationExceptionPropagates() {
        final String errorMsg = "Evaluator failure simulation";
        UnivariateRealFunction faultFunc = new UnivariateRealFunction() {
            public double value(double x) throws FunctionEvaluationException {
                throw new FunctionEvaluationException(x, errorMsg);
            }
        };

        BisectionSolver solver = new BisectionSolver();
        try {
            solver.solve(faultFunc, 0.0, 2.0);
            fail("Expected FunctionEvaluationException to be thrown");
        } catch (FunctionEvaluationException fee) {
            assertTrue(fee.getMessage().contains(errorMsg));
        } catch (Exception e) {
            fail("Unexpected exception thrown: " + e);
        }
    }

    @Test(timeout = 4000)
    public void testDeprecatedSolveWithoutFunctionThrowsNpeWhenFIsNull() throws Exception {
        // When constructed without a function, deprecated solve(min, max) passes null `f`
        BisectionSolver solver = new BisectionSolver();
        try {
            solver.solve(0.0, 2.0);
            fail("Expected NullPointerException when default f is null");
        } catch (NullPointerException expected) {
            // expected behavior for deprecated method without default f
            assertNotNull(expected);
        }
    }
}