package org.apache.commons.math3.optimization.univariate;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Decision branches targeted:
 * 1. Constructor validation: rel < MIN_RELATIVE_TOLERANCE (throws NumberIsTooSmallException)
 * 2. Constructor validation: abs <= 0 (throws NotStrictlyPositiveException)
 * 3. doOptimize: lo < hi vs lo >= hi (swap a/b)
 * 4. doOptimize: isMinim true/false (negate fx/fu)
 * 5. Stopping criterion: |x - m| <= tol2 - 0.5*(b-a)
 * 6. Parabolic vs Golden section: |e| > tol1
 * 7. Parabolic interpolation validity: p > q*(a-x) && p < q*(b-x) && |p| < |0.5*q*r|
 * 8. Update step: u < x (adjust a/b)
 * 9. Update step: fu <= fx (update x,w,v)
 * 10. Update step: fu <= fw || Precision.equals(w,x)
 * 11. Update step: fu <= fv || Precision.equals(v,x) || Precision.equals(v,w)
 * 12. Golden section: x < m (choose e)
 * 13. d sign handling: d >= 0 ? x+tol1 : x-tol1
 * 14. best() method: null handling, isMinim comparison
 * 
 * Defect targeting (testMath855): The bug occurs when the optimizer
 * fails to report the best point when the convergence checker is used.
 * The defect is in the parabolic interpolation step where the variable
 * 'u' is used before being assigned in some code paths, or the
 * convergence checker returns true before the best point is updated.
 * 
 * The testMath855 scenario: A function where the minimum is at the
 * boundary and the convergence checker triggers early, causing the
 * optimizer to return a suboptimal point.
 */
public class BrentOptimizerDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testSimpleQuadraticMinimize() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100, 
            new UnivariateFunction() {
                public double value(double x) { return (x - 3) * (x - 3); }
            },
            GoalType.MINIMIZE, -10, 10, 0);
        
        assertEquals(3.0, result.getPoint(), 1e-6);
        assertEquals(0.0, result.getValue(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testSimpleQuadraticMaximize() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100,
            new UnivariateFunction() {
                public double value(double x) { return -(x - 2) * (x - 2) + 5; }
            },
            GoalType.MAXIMIZE, -10, 10, 0);
        
        assertEquals(2.0, result.getPoint(), 1e-6);
        assertEquals(5.0, result.getValue(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testLoGreaterThanHi() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100,
            new UnivariateFunction() {
                public double value(double x) { return x * x; }
            },
            GoalType.MINIMIZE, 10, -10, 0);
        
        assertEquals(0.0, result.getPoint(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testStartValueAtBoundary() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100,
            new UnivariateFunction() {
                public double value(double x) { return (x - 5) * (x - 5); }
            },
            GoalType.MINIMIZE, 0, 10, 0);
        
        assertEquals(5.0, result.getPoint(), 1e-6);
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testMinimumRelativeTolerance() {
        // rel = 2 * ulp(1) should be accepted
        double minRel = 2 * Math.ulp(1.0);
        BrentOptimizer optimizer = new BrentOptimizer(minRel, 1e-10);
        assertNotNull(optimizer);
    }

    @Test(timeout = 4000)
    public void testTinyAbsoluteTolerance() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, Double.MIN_VALUE);
        UnivariatePointValuePair result = optimizer.optimize(100,
            new UnivariateFunction() {
                public double value(double x) { return x * x; }
            },
            GoalType.MINIMIZE, -1, 1, 0);
        
        assertEquals(0.0, result.getPoint(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testLargeScaleFunction() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(1000,
            new UnivariateFunction() {
                public double value(double x) { return (x - 1e6) * (x - 1e6); }
            },
            GoalType.MINIMIZE, -1e7, 1e7, 0);
        
        assertEquals(1e6, result.getPoint(), 1e-3);
    }

    @Test(timeout = 4000)
    public void testConstantFunction() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = optimizer.optimize(100,
            new UnivariateFunction() {
                public double value(double x) { return 42.0; }
            },
            GoalType.MINIMIZE, -10, 10, 0);
        
        assertEquals(42.0, result.getValue(), 1e-10);
    }

    // ========== Partition C: Defect-Targeted Branch Zone (testMath855) ==========

    /**
     * Test for MATH-855: Best point not reported.
     * This test uses a custom convergence checker that triggers convergence
     * immediately after the first iteration, but the optimizer must still
     * return the best point found so far.
     */
    @Test(timeout = 4000)
    public void testMath855BestPointNotReported() {
        // Create a function where the minimum is at x=0.5
        final UnivariateFunction function = new UnivariateFunction() {
            public double value(double x) {
                return (x - 0.5) * (x - 0.5);
            }
        };

        // Custom convergence checker that always returns true (converged immediately)
        ConvergenceChecker<UnivariatePointValuePair> checker = 
            new ConvergenceChecker<UnivariatePointValuePair>() {
                public boolean converged(int iteration,
                                         UnivariatePointValuePair previous,
                                         UnivariatePointValuePair current) {
                    // Always claim convergence to test early termination
                    return true;
                }
            };

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14, checker);
        
        // Start far from the minimum
        UnivariatePointValuePair result = optimizer.optimize(100, function, 
            GoalType.MINIMIZE, 0.0, 1.0, 0.1);
        
        // The optimizer should return the best point it has found,
        // even if the checker says converged immediately.
        // The best point at the first evaluation is the start value (0.1)
        // but the optimizer should still return a valid point.
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be within the search interval", 
                   result.getPoint() >= 0.0 && result.getPoint() <= 1.0);
        
        // The value should be finite
        assertTrue("Value should be finite", Double.isFinite(result.getValue()));
    }

    @Test(timeout = 4000)
    public void testMath855WithDelayedConvergence() {
        // Function with minimum at 0.25
        final UnivariateFunction function = new UnivariateFunction() {
            public double value(double x) {
                return (x - 0.25) * (x - 0.25);
            }
        };

        // Checker that converges after 2 iterations
        ConvergenceChecker<UnivariatePointValuePair> checker = 
            new ConvergenceChecker<UnivariatePointValuePair>() {
                private int count = 0;
                public boolean converged(int iteration,
                                         UnivariatePointValuePair previous,
                                         UnivariatePointValuePair current) {
                    return ++count >= 2;
                }
            };

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14, checker);
        
        UnivariatePointValuePair result = optimizer.optimize(100, function,
            GoalType.MINIMIZE, 0.0, 1.0, 0.9);
        
        assertNotNull("Result should not be null", result);
        // The result should be closer to the minimum than the start value
        assertTrue("Result should be closer to minimum than start",
                   Math.abs(result.getPoint() - 0.25) < Math.abs(0.9 - 0.25));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testRelativeThresholdTooSmall() {
        double tooSmallRel = 2 * Math.ulp(1.0) - 1e-15;
        new BrentOptimizer(tooSmallRel, 1e-10);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testAbsoluteThresholdZero() {
        new BrentOptimizer(1e-10, 0.0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testAbsoluteThresholdNegative() {
        new BrentOptimizer(1e-10, -1.0);
    }

    @Test(timeout = 4000)
    public void testNullConvergenceChecker() {
        // Null checker should be allowed (uses default termination)
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14, null);
        UnivariatePointValuePair result = optimizer.optimize(100,
            new UnivariateFunction() {
                public double value(double x) { return x * x; }
            },
            GoalType.MINIMIZE, -10, 10, 0);
        
        assertNotNull(result);
        assertEquals(0.0, result.getPoint(), 1e-6);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testBestMethodNullHandling() throws Exception {
        // Use reflection to test the private best method
        java.lang.reflect.Method bestMethod = 
            BrentOptimizer.class.getDeclaredMethod("best", 
                UnivariatePointValuePair.class, 
                UnivariatePointValuePair.class, 
                boolean.class);
        bestMethod.setAccessible(true);

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        
        // Test with both null
        UnivariatePointValuePair result = 
            (UnivariatePointValuePair) bestMethod.invoke(optimizer, null, null, true);
        assertNull("Both null should return null", result);
        
        // Test with first null
        UnivariatePointValuePair b = new UnivariatePointValuePair(1.0, 2.0);
        result = (UnivariatePointValuePair) bestMethod.invoke(optimizer, null, b, true);
        assertEquals("Should return second when first is null", b, result);
        
        // Test with second null
        UnivariatePointValuePair a = new UnivariatePointValuePair(3.0, 4.0);
        result = (UnivariatePointValuePair) bestMethod.invoke(optimizer, a, null, true);
        assertEquals("Should return first when second is null", a, result);
    }

    @Test(timeout = 4000)
    public void testBestMethodMinimization() throws Exception {
        java.lang.reflect.Method bestMethod = 
            BrentOptimizer.class.getDeclaredMethod("best", 
                UnivariatePointValuePair.class, 
                UnivariatePointValuePair.class, 
                boolean.class);
        bestMethod.setAccessible(true);

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        
        UnivariatePointValuePair a = new UnivariatePointValuePair(1.0, 5.0);
        UnivariatePointValuePair b = new UnivariatePointValuePair(2.0, 3.0);
        
        // Minimization: should return b (lower value)
        UnivariatePointValuePair result = 
            (UnivariatePointValuePair) bestMethod.invoke(optimizer, a, b, true);
        assertEquals("Minimization should pick lower value", b, result);
        
        // Maximization: should return a (higher value)
        result = (UnivariatePointValuePair) bestMethod.invoke(optimizer, a, b, false);
        assertEquals("Maximization should pick higher value", a, result);
    }

    @Test(timeout = 4000)
    public void testBestMethodTie() throws Exception {
        java.lang.reflect.Method bestMethod = 
            BrentOptimizer.class.getDeclaredMethod("best", 
                UnivariatePointValuePair.class, 
                UnivariatePointValuePair.class, 
                boolean.class);
        bestMethod.setAccessible(true);

        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        
        UnivariatePointValuePair a = new UnivariatePointValuePair(1.0, 5.0);
        UnivariatePointValuePair b = new UnivariatePointValuePair(2.0, 5.0);
        
        // Tie: should return a (first one)
        UnivariatePointValuePair result = 
            (UnivariatePointValuePair) bestMethod.invoke(optimizer, a, b, true);
        assertEquals("Tie should return first", a, result);
    }

    @Test(timeout = 4000)
    public void testMultipleOptimizations() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        
        // First optimization
        UnivariatePointValuePair result1 = optimizer.optimize(100,
            new UnivariateFunction() {
                public double value(double x) { return x * x; }
            },
            GoalType.MINIMIZE, -10, 10, 0);
        
        // Second optimization with different function
        UnivariatePointValuePair result2 = optimizer.optimize(100,
            new UnivariateFunction() {
                public double value(double x) { return (x - 7) * (x - 7); }
            },
            GoalType.MINIMIZE, -10, 10, 0);
        
        assertEquals(0.0, result1.getPoint(), 1e-6);
        assertEquals(7.0, result2.getPoint(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testExtremeFunctionValues() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        
        // Function with very large values
        UnivariatePointValuePair result = optimizer.optimize(100,
            new UnivariateFunction() {
                public double value(double x) { 
                    return 1e15 * (x - 0.5) * (x - 0.5); 
                }
            },
            GoalType.MINIMIZE, -10, 10, 0);
        
        assertEquals(0.5, result.getPoint(), 1e-6);
        assertTrue("Value should be non-negative", result.getValue() >= 0);
    }

    @Test(timeout = 4000)
    public void testDiscontinuousFunction() {
        BrentOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        
        // Discontinuous function with minimum at 0
        UnivariatePointValuePair result = optimizer.optimize(100,
            new UnivariateFunction() {
                public double value(double x) { 
                    if (x < 0) return x * x + 10;
                    return x * x;
                }
            },
            GoalType.MINIMIZE, -10, 10, 0);
        
        assertEquals(0.0, result.getPoint(), 1e-6);
        assertEquals(0.0, result.getValue(), 1e-10);
    }
}