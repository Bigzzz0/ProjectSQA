package org.apache.commons.math.analysis.solvers;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * 
 * Partition A – Core Functional Logic:
 *   - solve(f, x0, x1): delegates to default solver; test with simple function (root at 0)
 *   - solve(f, x0, x1, absoluteAccuracy): same with accuracy; note accuracy must be positive?
 *   - bracket(function, initial, lowerBound, upperBound): tests normal bracketing with sign change
 *   - bracket(..., maximumIterations): same with explicit iterations
 *   - midpoint(a, b): simple computation
 * 
 * Partition B – Boundary Value Analysis & Extremes:
 *   - null function (IllegalArgumentException)
 *   - invalid initial (outside [lowerBound, upperBound])
 *   - lowerBound >= upperBound
 *   - maximumIterations <= 0
 *   - initial == lowerBound or initial == upperBound (should throw? Actually initial must be strictly between? javadoc says "initial is not between lowerBound and upperBound" => throws if not in [lowerBound, upperBound] inclusive? Check: condition `initial < lowerBound || initial > upperBound` => inclusive allowed? If initial == lowerBound, initial is not less than lowerBound, so allowed. But the algorithm requires a < initial < b, so if initial == lowerBound, then a = max(initial-1, lowerBound) = lowerBound, so a == initial, which violates a < initial. So the bracket method itself may still work? Let's test boundary cases. For safety, test initial just inside.
 *   - large bounds (e.g., Double.MAX_VALUE, -Double.MAX_VALUE) – may cause overflow? But test with moderate.
 * 
 * Partition C – Defect-Targeted Branch Zone:
 *   - Condition: fa * fb == 0.0 (one endpoint is a root). The bug is in the final check `fa * fb >= 0.0` which throws ConvergenceException when product is zero. Correct behavior should accept it as bracketing success.
 *   - Test scenario: function with root at a or b (e.g., f(a) = 0, f(b) positive or negative, product = 0). Expect bracket to return array with a and b.
 *   - Also test scenario where a and b both have same sign but product is zero? Actually if one is zero, product is zero. The loop condition `fa*fb > 0.0` stops when product <= 0, so it exits. Then the final check fails if product == 0. So our test should reveal the ConvergenceException on defective version.
 * 
 * Partition D – Exception & Defensive Guard Paths:
 *   - null function for bracket and solve
 *   - maximumIterations <= 0
 *   - initial out of bounds (lowerBound/upperBound)
 *   - lowerBound >= upperBound
 *   - Convergence when brackets cannot be found (e.g., function always positive, interval touches bounds)
 * 
 * Partition E – Object Lifecycle & Contract: Not applicable (static utilities only).
 */
public class UnivariateRealSolverUtilsDeepseekTest {

    // ================= Partition A: Core Functional Logic =================
    
    @Test(timeout = 4000)
    public void testSolveSimpleFunction() throws Exception {
        // f(x) = x - 1, root at 1
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.0;
            }
        };
        double result = UnivariateRealSolverUtils.solve(f, 0.0, 2.0);
        assertEquals(1.0, result, 1e-6);
    }

    @Test(timeout = 4000)
    public void testSolveWithAbsoluteAccuracy() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0; // roots at -2 and 2
            }
        };
        double result = UnivariateRealSolverUtils.solve(f, 0.0, 5.0, 1e-10);
        assertEquals(2.0, result, 1e-8);
    }

    @Test(timeout = 4000)
    public void testBracketNormalSignChange() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0; // root at sqrt(2) ≈ 1.414
            }
        };
        double[] bracket = UnivariateRealSolverUtils.bracket(f, 1.0, 0.0, 2.0);
        assertNotNull(bracket);
        assertEquals(2, bracket.length);
        assertTrue(bracket[0] < bracket[1]);
        // verify signs are opposite or one zero (should be opposite)
        double fa = f.value(bracket[0]);
        double fb = f.value(bracket[1]);
        assertTrue("f(a)*f(b) <= 0 expected, but got " + (fa*fb), fa * fb <= 0.0);
    }

    @Test(timeout = 4000)
    public void testBracketWithExplicitIterations() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 5.0; // root at 5
            }
        };
        double[] bracket = UnivariateRealSolverUtils.bracket(f, 4.0, 0.0, 10.0, 100);
        assertNotNull(bracket);
        assertTrue(bracket[0] <= 5.0 && 5.0 <= bracket[1]);
    }

    @Test(timeout = 4000)
    public void testMidpoint() {
        double mid = UnivariateRealSolverUtils.midpoint(2.0, 4.0);
        assertEquals(3.0, mid, 0.0);
        mid = UnivariateRealSolverUtils.midpoint(-1.0, 3.0);
        assertEquals(1.0, mid, 0.0);
        mid = UnivariateRealSolverUtils.midpoint(0.0, 0.0);
        assertEquals(0.0, mid, 0.0);
    }

    // ================= Partition B: Boundary Values =================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveNullFunction() throws Exception {
        UnivariateRealSolverUtils.solve(null, 0.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveNullFunctionWithAccuracy() throws Exception {
        UnivariateRealSolverUtils.solve(null, 0.0, 1.0, 1e-6);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBracketNullFunction() throws Exception {
        UnivariateRealSolverUtils.bracket(null, 0.5, 0.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBracketNullFunctionWithIterations() throws Exception {
        UnivariateRealSolverUtils.bracket(null, 0.5, 0.0, 1.0, 100);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBracketInitialBelowLowerBound() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x; }
        };
        UnivariateRealSolverUtils.bracket(f, -1.0, 0.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBracketInitialAboveUpperBound() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x; }
        };
        UnivariateRealSolverUtils.bracket(f, 2.0, 0.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBracketLowerBoundEqualsUpperBound() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x; }
        };
        UnivariateRealSolverUtils.bracket(f, 0.5, 1.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBracketLowerBoundGreaterThanUpperBound() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x; }
        };
        UnivariateRealSolverUtils.bracket(f, 0.5, 2.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBracketInvalidMaxIterations() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x; }
        };
        UnivariateRealSolverUtils.bracket(f, 0.5, 0.0, 1.0, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBracketNegativeMaxIterations() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x; }
        };
        UnivariateRealSolverUtils.bracket(f, 0.5, 0.0, 1.0, -5);
    }

    // ================= Partition C: Defect-Targeted: f(a) * f(b) == 0 =================
    
    @Test(timeout = 4000)
    public void testBracketWhenRootAtLeftEndpoint() throws Exception {
        // f(x) = x - 0; root at 0 exactly. Bracket starts from initial = 1, lowerBound=0, upperBound=2.
        // After first iteration: a = max(1-1, 0) = 0, b = min(1+1, 2) = 2. f(a)=0, f(b)=2. Product=0.
        // Loop condition fa*fb > 0.0 is false (0>0 is false), so loop exits.
        // Then final check: fa*fb >= 0.0 -> true (0>=0) throws ConvergenceException.
        // Expected correct behavior: should succeed and return [0,2].
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x; // root at 0
            }
        };
        double[] bracket = UnivariateRealSolverUtils.bracket(f, 1.0, 0.0, 2.0);
        assertNotNull(bracket);
        assertEquals("a should be lowerBound", 0.0, bracket[0], 0.0);
        assertEquals("b should be 2.0", 2.0, bracket[1], 0.0);
    }

    @Test(timeout = 4000)
    public void testBracketWhenRootAtRightEndpoint() throws Exception {
        // f(x) = x - 2; root at 2. initial = 1, lowerBound=0, upperBound=2.
        // After first iteration: a=0, b=2. f(a)=-2, f(b)=0. Product=0.
        // Same as above.
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 2.0;
            }
        };
        double[] bracket = UnivariateRealSolverUtils.bracket(f, 1.0, 0.0, 2.0);
        assertNotNull(bracket);
        assertEquals("a should be 0.0", 0.0, bracket[0], 0.0);
        assertEquals("b should be upperBound", 2.0, bracket[1], 0.0);
    }

    @Test(timeout = 4000)
    public void testBracketWhenRootInsideButOneEndpointReachesRoot() throws Exception {
        // Another defect case: initial such that a or b hits root during expansion.
        // f(x) = x - 5; root at 5. initial=4, lower=0, upper=10.
        // a = max(4-1=3,0)=3, b = min(4+1=5,10)=5. f(a)=-2, f(b)=0. Product=0.
        // Should succeed.
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 5.0;
            }
        };
        double[] bracket = UnivariateRealSolverUtils.bracket(f, 4.0, 0.0, 10.0);
        assertNotNull(bracket);
        assertTrue("b should be 5.0", Math.abs(bracket[1] - 5.0) < 1e-12);
    }

    // ================= Partition D: Exception Paths =================

    @Test(expected = ConvergenceException.class, timeout = 4000)
    public void testBracketConvergenceFailureNoSignChange() throws Exception {
        // Function always positive: f(x) = x^2 + 1. No root.
        // Initial=0, lower=-10, upper=10. Expand until bounds reached, product always >0.
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };
        UnivariateRealSolverUtils.bracket(f, 0.0, -10.0, 10.0);
    }

    @Test(expected = ConvergenceException.class, timeout = 4000)
    public void testBracketConvergenceFailureTightBounds() throws Exception {
        // Function f(x) = x, root at 0. initial=1, lower=0.5, upper=1.5.
        // a = max(0, 0.5) = 0.5, b = min(2, 1.5) = 1.5.
        // f(a)=0.5, f(b)=1.5, product > 0. No further expansion, both bounds reached => ConvergenceException.
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        UnivariateRealSolverUtils.bracket(f, 1.0, 0.5, 1.5);
    }

    // ================= Additional coverage tests =================

    @Test(timeout = 4000)
    public void testBracketWithInitialAtLowerBound() throws Exception {
        // initial == lowerBound is allowed (since not < lowerBound). But a < initial fails (a == initial).
        // Let's test: function with root at 1, initial = 0, lower = 0, upper = 2.
        // a = max(0-1=-1,0)=0, b = min(0+1=1,2)=1. f(a)=f(0) = -1 (if f(x)=x-1), f(b)=0. product=0.
        // This should succeed (defect case actually) but note that initial == lowerBound means a = initial, violating a<initial. But algorithm still works? It will return a=0, b=1. That's fine.
        // We'll test it.
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.0;
            }
        };
        double[] bracket = UnivariateRealSolverUtils.bracket(f, 0.0, 0.0, 2.0);
        assertNotNull(bracket);
        assertEquals(0.0, bracket[0], 0.0);
        assertEquals(1.0, bracket[1], 0.0);
    }

    @Test(timeout = 4000)
    public void testBracketWithInitialAtUpperBound() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.0;
            }
        };
        double[] bracket = UnivariateRealSolverUtils.bracket(f, 2.0, 0.0, 2.0);
        assertNotNull(bracket);
        assertEquals(1.0, bracket[0], 0.0);
        assertEquals(2.0, bracket[1], 0.0);
    }

    @Test(timeout = 4000)
    public void testBracketMaximumIterationsExactlySatisfied() throws Exception {
        // Function with root at 0, initial=1, lower=0, upper=10, maxIterations=1.
        // After first iteration: a=0, b=2. f(a)=0, product=0 => success.
        // But if f(a)!=0, product >0 and maxIterations reached -> ConvergenceException.
        // We'll test a case where success occurs with maxIterations=1.
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 0.0; // root at 0
            }
        };
        double[] bracket = UnivariateRealSolverUtils.bracket(f, 1.0, 0.0, 10.0, 1);
        assertNotNull(bracket);
        assertEquals(0.0, bracket[0], 0.0);
        assertEquals(2.0, bracket[1], 0.0);
    }

    @Test(expected = ConvergenceException.class, timeout = 4000)
    public void testBracketExceedsMaximumIterations() throws Exception {
        // Function no sign change, maxIterations=1, only one iteration performed, no convergence.
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };
        UnivariateRealSolverUtils.bracket(f, 0.0, -10.0, 10.0, 1);
    }
}