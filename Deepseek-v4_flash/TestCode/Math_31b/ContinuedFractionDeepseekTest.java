package org.apache.commons.math3.util;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MaxCountExceededException;

/**
 * Advanced white-box test suite for ContinuedFraction.
 * Targets line/branch coverage and the known NaN divergence defect.
 */
public class ContinuedFractionDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Branches covered:
     * - hPrev zero check (Precision.equals)
     * - While loop entry/exit (n < maxIterations)
     * - Infinite cN/q2 detection and scaling loop
     *   - scale <= 0 -> ConvergenceException
     *   - a != 0 && a > b branch
     *   - b != 0 branch
     *   - scaling loop break on finite values
     * - deltaN computation and convergence check
     * - hN infinite/NaN checks -> ConvergenceException
     * - Max iterations exceeded -> MaxCountExceededException
     * - Normal convergence return
     *
     * Defect targeting:
     * - Known NaN divergence for x=0.5 (e.g., zero coefficients)
     * - Also tests infinite divergence and scaling edge cases
     */

    // ---------------------------------------------------------------
    // Helper: concrete ContinuedFraction implementations
    // ---------------------------------------------------------------

    /** Convergent fraction: 1/(1+1/(1+...)) = (sqrt(5)-1)/2 ≈ 0.618 */
    private static class GoldenRatioCF extends ContinuedFraction {
        @Override
        protected double getA(int n, double x) {
            return 1.0;
        }
        @Override
        protected double getB(int n, double x) {
            return 1.0;
        }
    }

    /** Zero coefficients – triggers NaN divergence */
    private static class ZeroCF extends ContinuedFraction {
        @Override
        protected double getA(int n, double x) {
            return 0.0;
        }
        @Override
        protected double getB(int n, double x) {
            return 0.0;
        }
    }

    /** Coefficients that cause infinite cN/q2 and require scaling */
    private static class ScalingCF extends ContinuedFraction {
        @Override
        protected double getA(int n, double x) {
            // a0 = 1, a1 = 1e100, a2 = 1e-100, etc.
            if (n == 0) return 1.0;
            if (n == 1) return 1e100;
            return 1.0;
        }
        @Override
        protected double getB(int n, double x) {
            if (n == 0) return 1.0;
            if (n == 1) return 1e100;
            return 1.0;
        }
    }

    /** Slowly converging fraction to trigger max iterations */
    private static class SlowCF extends ContinuedFraction {
        @Override
        protected double getA(int n, double x) {
            return 1.0;
        }
        @Override
        protected double getB(int n, double x) {
            // b_n = n+1, so convergence is slow
            return n + 1.0;
        }
    }

    /** Fraction with zero initial hPrev */
    private static class ZeroInitialCF extends ContinuedFraction {
        @Override
        protected double getA(int n, double x) {
            return (n == 0) ? 0.0 : 1.0;
        }
        @Override
        protected double getB(int n, double x) {
            return 1.0;
        }
    }

    // ---------------------------------------------------------------
    // Partition A: Core functional logic & state transitions
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEvaluateDefaultEpsilon() {
        ContinuedFraction cf = new GoldenRatioCF();
        double result = cf.evaluate(1.0);
        // Expected: (sqrt(5)-1)/2 ≈ 0.6180339887498949
        assertEquals(0.6180339887498949, result, 1e-9);
    }

    @Test(timeout = 4000)
    public void testEvaluateWithEpsilon() {
        ContinuedFraction cf = new GoldenRatioCF();
        double result = cf.evaluate(1.0, 1e-12);
        assertEquals(0.6180339887498949, result, 1e-12);
    }

    @Test(timeout = 4000)
    public void testEvaluateWithMaxIterations() {
        ContinuedFraction cf = new GoldenRatioCF();
        double result = cf.evaluate(1.0, 100);
        assertEquals(0.6180339887498949, result, 1e-9);
    }

    @Test(timeout = 4000)
    public void testEvaluateWithEpsilonAndMaxIterations() {
        ContinuedFraction cf = new GoldenRatioCF();
        double result = cf.evaluate(1.0, 1e-12, 100);
        assertEquals(0.6180339887498949, result, 1e-12);
    }

    // ---------------------------------------------------------------
    // Partition B: Boundary value analysis & extremes
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEvaluateZeroInitial() {
        ContinuedFraction cf = new ZeroInitialCF();
        double result = cf.evaluate(1.0);
        // Should converge to same golden ratio (since a0=0 is set to small)
        assertEquals(0.6180339887498949, result, 1e-9);
    }

    @Test(timeout = 4000)
    public void testEvaluateNegativeX() {
        ContinuedFraction cf = new GoldenRatioCF();
        double result = cf.evaluate(-1.0);
        // For x negative, coefficients are constant, so result should be same
        assertEquals(0.6180339887498949, result, 1e-9);
    }

    // ---------------------------------------------------------------
    // Partition C: Defect-targeted branch zone (NaN divergence)
    // ---------------------------------------------------------------

    @Test(timeout = 4000, expected = ConvergenceException.class)
    public void testEvaluateNaNDivergence() {
        // Zero coefficients cause cN/q2 = 0/0 -> NaN -> ConvergenceException
        ContinuedFraction cf = new ZeroCF();
        cf.evaluate(0.5);
    }

    @Test(timeout = 4000, expected = ConvergenceException.class)
    public void testEvaluateNaNDivergenceWithEpsilon() {
        ContinuedFraction cf = new ZeroCF();
        cf.evaluate(0.5, 1e-9);
    }

    @Test(timeout = 4000, expected = ConvergenceException.class)
    public void testEvaluateNaNDivergenceWithMaxIterations() {
        ContinuedFraction cf = new ZeroCF();
        cf.evaluate(0.5, 100);
    }

    @Test(timeout = 4000, expected = ConvergenceException.class)
    public void testEvaluateNaNDivergenceFull() {
        ContinuedFraction cf = new ZeroCF();
        cf.evaluate(0.5, 1e-9, 100);
    }

    // ---------------------------------------------------------------
    // Partition D: Exception & defensive guard paths
    // ---------------------------------------------------------------

    @Test(timeout = 4000, expected = MaxCountExceededException.class)
    public void testEvaluateMaxIterationsExceeded() {
        // SlowCF converges very slowly; with maxIterations=5 it should fail
        ContinuedFraction cf = new SlowCF();
        cf.evaluate(1.0, 1e-9, 5);
    }

    @Test(timeout = 4000, expected = ConvergenceException.class)
    public void testEvaluateInfinityDivergence() {
        // ScalingCF with a1 and b1 huge may cause infinite values
        ContinuedFraction cf = new ScalingCF();
        cf.evaluate(1.0, 1e-9, 100);
    }

    @Test(timeout = 4000)
    public void testEvaluateScalingSuccess() {
        // Use a fraction that requires scaling but eventually converges
        // We'll create a custom one: a0=1, a1=1e200, b1=1e200, then normal
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                if (n == 0) return 1.0;
                if (n == 1) return 1e200;
                return 1.0;
            }
            @Override
            protected double getB(int n, double x) {
                if (n == 0) return 1.0;
                if (n == 1) return 1e200;
                return 1.0;
            }
        };
        // Should converge to golden ratio after scaling
        double result = cf.evaluate(1.0, 1e-9, 1000);
        assertEquals(0.6180339887498949, result, 1e-9);
    }

    @Test(timeout = 4000, expected = ConvergenceException.class)
    public void testEvaluateScaleZero() {
        // When scale <= 0, should throw ConvergenceException
        ContinuedFraction cf = new ContinuedFraction() {
            @Override
            protected double getA(int n, double x) {
                return 0.0;
            }
            @Override
            protected double getB(int n, double x) {
                return -1.0; // negative b makes scale negative? Actually scale = max(a,b) = 0, so scale <= 0
            }
        };
        cf.evaluate(1.0, 1e-9, 100);
    }

    // ---------------------------------------------------------------
    // Partition E: Object lifecycle & contract integrity
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEvaluateMultipleCalls() {
        ContinuedFraction cf = new GoldenRatioCF();
        double r1 = cf.evaluate(1.0);
        double r2 = cf.evaluate(1.0);
        assertEquals(r1, r2, 0.0);
    }

    @Test(timeout = 4000)
    public void testEvaluateDifferentX() {
        ContinuedFraction cf = new GoldenRatioCF();
        double r1 = cf.evaluate(2.0);
        double r2 = cf.evaluate(3.0);
        // Since coefficients are constant, result should be same
        assertEquals(r1, r2, 1e-9);
    }

    // Additional branch coverage: deltaN exactly 1.0 (convergence immediate)
    @Test(timeout = 4000)
    public void testEvaluateImmediateConvergence() {
        // Create a fraction where deltaN = 1 after first iteration
        ContinuedFraction cf = new ContinuedFraction() {
            private int callCount = 0;
            @Override
            protected double getA(int n, double x) {
                if (n == 0) return 1.0;
                if (n == 1) return 0.0; // a1=0 makes cN = 0*hPrev + b*p0 = b*p0, q2 = 0*q1 + b*dPrev = b*dPrev
                return 0.0;
            }
            @Override
            protected double getB(int n, double x) {
                if (n == 0) return 1.0;
                if (n == 1) return 1.0;
                return 0.0;
            }
        };
        double result = cf.evaluate(1.0, 1e-9, 100);
        // Expected: hN = 1.0 (since after first iteration, deltaN = 1)
        assertEquals(1.0, result, 1e-9);
    }
}