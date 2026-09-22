package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: UnivariateRealSolverUtils
 *
 * Decision / Condition Coverage Targets:
 * 1. solve(f, x0, x1) & solve(f, x0, x1, accuracy):
 *    - setup(f): f == null (throws IllegalArgumentException) vs f != null.
 *    - Endpoints valid vs invalid interval (e.g., x0 >= x1, handled by solver).
 *    - Accuracy propagation to the underlying solver.
 *
 * 2. bracket(function, initial, lowerBound, upperBound):
 *    - Overload forwarding to bracket(..., Integer.MAX_VALUE).
 *
 * 3. bracket(function, initial, lowerBound, upperBound, maximumIterations):
 *    - Guard 1: function == null -> IllegalArgumentException.
 *    - Guard 2: maximumIterations <= 0 -> IllegalArgumentException.
 *    - Guard 3: initial < lowerBound -> IllegalArgumentException.
 *    - Guard 3: initial > upperBound -> IllegalArgumentException.
 *    - Guard 3: lowerBound >= upperBound -> IllegalArgumentException.
 *    - Boundary checks: initial == lowerBound (fails initial < lowerBound, but triggers lowerBound >= upperBound check if lower == upper).
 *    - Loop Condition: (fa * fb > 0.0) && (numIterations < maximumIterations) && ((a > lowerBound) || (b < upperBound)).
 *      * Hits a root where fa * fb < 0 (standard bracketing success).
 *      * Hits a root where fa == 0 or fb == 0 -> fa * fb == 0.0.
 *      * numIterations reaches maximumIterations without bracketing.
 *      * Interval expansion hits boundaries: a == lowerBound and b == upperBound.
 *    - Exit Condition: if (fa * fb > 0.0) in corrected version vs (fa * fb >= 0.0) in defective version.
 *
 * 4. midpoint(a, b):
 *    - Correct arithmetic: (a + b) * 0.5 across positive, negative, zero, and extreme values.
 *
 * 5. Defects4J Defect Target (MATH-280):
 *    - When bracketing lands directly on a root such that f(a) == 0.0 or f(b) == 0.0 (fa * fb == 0.0),
 *      the method contract stipulates bracketing success (f(a) * f(b) <= 0). However, the defective code
 *      checks "if (fa * fb >= 0.0)" and erroneously throws ConvergenceException.
 */
public class UnivariateRealSolverUtilsGeminiTest {

    /* =========================================================================
     * Partition A: Core Functional Logic & State Transitions
     * ========================================================================= */

    @Test(timeout = 4000)
    public void testSolveTwoParametersStandardFunction() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        double root = UnivariateRealSolverUtils.solve(f, 3.0, 4.0);
        assertEquals(Math.PI, root, 1.0e-4);
    }

    @Test(timeout = 4000)
    public void testSolveFourParametersWithAccuracy() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        double accuracy = 1.0e-7;
        double root = UnivariateRealSolverUtils.solve(f, 3.0, 4.0, accuracy);
        assertEquals(Math.PI, root, accuracy);
    }

    @Test(