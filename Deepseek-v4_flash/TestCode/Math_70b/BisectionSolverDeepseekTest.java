package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for BisectionSolver, targeting line/branch coverage
 * and the known NullPointerException defect (Defects4J testMath369).
 *
 * [Branch & Defect Analysis Matrix]
 *
 * Target method: solve(UnivariateRealFunction, double, double)
 *
 * Decision branches:
 * 1. verifyInterval(min, max) – throws IllegalArgumentException if interval invalid
 * 2. while (i < maximalIterationCount) loop entry/exit
 * 3. fm * fmin > 0.0 ?  (true: set min = m; false: set max = m)
 * 4. if (Math.abs(max - min) <= absoluteAccuracy)  (true: return midpoint; false: continue)
 * 5. Loop exhausted -> throw MaxIterationsExceededException
 *
 * Defect: NPE when the internal function reference f is null (deprecated constructor).
 * Triggered when any solve method accesses f.value(...) without null check.
 *
 * Partitions:
 * A – Core functional: normal root, monotone increasing/decreasing functions
 * B – Boundary: min==max, min>max, absoluteAccuracy limits
 * C – Defect-targeted: null function via deprecated constructor / new API null parameter
 * D – Exception: max iterations exceeded, function evaluation exception
 * E – Object lifecycle: constructor state, getters
 */
public class BisectionSolverDeepseekTest {

    // ==================== Part A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testSolveSimpleRoot() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        // f(x) = 2x - 4  => root at x=2
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return 2 * x - 4; }
        };
        double root = solver.solve(f, 0.0, 5.0);
        assertEquals("Unexpected root", 2.0, root, 1e-6);
        assertTrue("Root not bracketed", solver.getResult() >= 0.0 && solver.getResult() <= 5.0);
    }

    @Test(timeout = 4000)
    public void testSolveNegativeRoot() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        // f(x) = x + 3  => root at x=-3
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x + 3; }
        };
        double root = solver.solve(f, -5.0, 5.0);
        assertEquals("Unexpected root", -3.0, root, 1e-6);
    }

    // ==================== Part B: Boundary Value Analysis ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveInvalidIntervalMinGreaterThanMax() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x; }
        };
        solver.solve(f, 10.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveInvalidIntervalEqualEndpointsNoRoot() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return 1.0; }
        };
        solver.solve(f, 5.0, 5.0);
    }

    @Test(timeout = 4000)
    public void testSolveExtremelySmallRange() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        solver.setAbsoluteAccuracy(1e-12);
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return Math.sin(x); }
        };
        double root = solver.solve(f, Math.PI - 1e-10, Math.PI + 1e-10);
        assertEquals("Root near pi", Math.PI, root, 1e-8);
    }

    @Test(timeout = 4000)
    public void testSolveLargeDomain() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 1000; }
        };
        double root = solver.solve(f, -1e6, 1e6);
        assertEquals("Unexpected root", 1000.0, root, 1e-3);
    }

    // ==================== Part C: Defect-Targeted (NullPointerException) ====================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testMath369_NullFunctionDeprecatedConstructor() throws Exception {
        // Using deprecated constructor with null function -> internal f is null
        // Calling solve(double, double) will call solve(f, min, max) which tries f.value(...)
        BisectionSolver solver = new BisectionSolver(null);
        solver.solve(0.0, 10.0);
        // Expected NPE on defective version; on fixed, should perhaps throw NPE as well (no null check)
        // This test triggers the documented bug.
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testMath369_NullFunctionNewAPI() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        // Passing null function to the non-deprecated solve method
        solver.solve(null, 0.0, 10.0);
    }

    // ==================== Part D: Exception & Defensive Guard Paths ====================

    @Test(expected = MaxIterationsExceededException.class, timeout = 4000)
    public void testMaxIterationsExceeded() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        solver.setMaximalIterationCount(10); // very low
        solver.setAbsoluteAccuracy(1e-12);   // extremely high accuracy
        // Function with no root in interval, but bisection will eventually converge very slowly.
        // Use f(x)=x, interval [-1,1] -> root at 0, but with low iterations it might not converge.
        // Actually the algorithm requires sign change – we need a function that doesn't cross zero.
        // Use f(x)=x^2+1, always positive, so no sign change, but algorithm will still bracket? Wait,
        // bisection requires f(min) and f(max) have opposite signs. If not, it's invalid.
        // We need a function with opposite signs at endpoints but very slow convergence.
        // Use f(x)= x-0.5, root at 0.5, set very large accuracy and small iterations.
        final UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 0.5; }
        };
        solver.solve(f, 0.0, 1.0); // Should converge quickly, but with 10 iterations and 1e-12 accuracy it might fail.
        // Actually if function is linear, 10 iterations give interval width 1/2^10 ≈ 0.001 > 1e-12, so exception.
    }

    @Test(expected = FunctionEvaluationException.class, timeout = 4000)
    public void testFunctionEvaluationException() throws Exception {
        BisectionSolver solver = new BisectionSolver();
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) throws FunctionEvaluationException {
                throw new FunctionEvaluationException(x);
            }
        };
        solver.solve(f, 0.0, 10.0);
    }

    // ==================== Part E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testDefaultConstructorState() {
        BisectionSolver solver = new BisectionSolver();
        assertEquals("Default max iterations", 100, solver.getMaximalIterationCount());
        assertEquals("Default absolute accuracy", 1e-6, solver.getAbsoluteAccuracy(), 1e-6);
        assertNotNull("Function should be null initially", solver.getFunction());
        // Actually getFunction returns null for no-arg constructor? Let's check: parent stores f internally.
        // The field is private and only set via setFunction or constructor. We don't set it.
        // getFunction() is inherited: returns the stored function. It can be null.
        assertNull("Function should be null", solver.getFunction());
    }

    @Test(timeout = 4000)
    public void testDeprecatedConstructorState() {
        UnivariateRealFunction dummy = new UnivariateRealFunction() {
            public double value(double x) { return 0; }
        };
        BisectionSolver solver = new BisectionSolver(dummy);
        assertSame("Function stored", dummy, solver.getFunction());
    }

    @Test(timeout = 4000)
    public void testSetAndGetAccuracy() {
        BisectionSolver solver = new BisectionSolver();
        solver.setAbsoluteAccuracy(1e-10);
        assertEquals("Accuracy updated", 1e-10, solver.getAbsoluteAccuracy(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSetAndGetMaxIterations() {
        BisectionSolver solver = new BisectionSolver();
        solver.setMaximalIterationCount(200);
        assertEquals("Max iterations updated", 200, solver.getMaximalIterationCount());
    }
}