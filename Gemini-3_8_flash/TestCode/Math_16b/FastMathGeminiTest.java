package org.apache.commons.math3.util;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.math3.util.FastMath
 *
 * 1. Defect MATH-905 (Defects4J ground truth):
 *    - In cosh(x) and sinh(x), inputs near log(Double.MAX_VALUE) and log(2 * Double.MAX_VALUE)
 *      (~709.78 to ~710.47) prematurely overflow to Infinity because 0.5 * exp(x) evaluates
 *      exp(x) first (which overflows for x > 709.7827), even though the true hyperbolic result
 *      is representable within Double.MAX_VALUE (~1.79e308).
 *    - Targeted by testMath905LargePositive and testMath905LargeNegative.
 *
 * 2. Coverage Focus Areas:
 *    - exp / expm1: negative subnormals (intVal > 709, intVal > 746, intVal == 709),
 *      ranges [-1, 1], > 709 (Infinity), 0.0, NaN.
 *    - log / log1p / log10 / log(base, x): subnormals, 0.99 < x < 1.01 (Remez poly),
 *      x <= 0, +Infinity, NaN.
 *    - pow(double, double) and pow(double, int): all IEEE-754 edge branches (+/-0, +/-Inf, NaN,
 *      odd/even integer powers, fractional powers of negatives).
 *    - sin / cos / tan / asin / acos / atan / atan2: Payne-Hanek reduction (x > 3294198),
 *      Cody-Waite reduction, quadrant flips, negative zero, extreme values.
 *    - sinh / cosh / tanh / asinh / acosh / atanh: magnitude thresholds (> 20, < -20,
 *      intermediate polynomial branches for asinh/atanh).
 *    - cbrt / sqrt / hypot: subnormals, scale-down Newton steps, large scale differences.
 *    - scalb / nextAfter / nextUp / getExponent / copySign: float and double variants,
 *      underflow, overflow, subnormals.
 *    - min / max / abs / floor / ceil / rint / round: sign bit preservation (-0.0 vs +0.0),
 *      boundary 2^52, NaN handling.
 */

import org.junit.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.Assert.*;

public class FastMathGeminiTest {

    private static final double EPSILON = 1e-15;

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-905)
    // =========================================================================

    /**
     * Targets MATH-905: cosh(x) and sinh(x) for large positive arguments
     * between ln(Double.MAX_VALUE) and ln(2 * Double.MAX_VALUE).
     */
    @Test(timeout = 4000)
    public void testMath905LargePositive() {
        final double start = StrictMath.log(Double.MAX_VALUE);
        final double endT = FastMath.sqrt(2) * StrictMath.sqrt(Double.MAX_VALUE);
        final double end = 2 * StrictMath.log(endT);

        final double step = (end - start) / 1024.0;
        for (double x = start; x < end; x += step) {
            assertEquals(0.0,
                    (FastMath.cosh(x) - StrictMath.cosh(x)) / StrictMath.cosh(x),
                    1e-15);
            assertEquals(0.0,
                    (FastMath.sinh(x) - StrictMath.sinh(x)) / StrictMath.sinh(x),
                    1e-15);
        }
    }

    /**
     * Targets MATH-905: cosh(x) and sinh(x) for large negative arguments
     * between -ln(Double.MAX_VALUE) and -ln(2 * Double.MAX_VALUE).
     */
    @Test(timeout = 4000)
    public void testMath905LargeNegative() {
        final double start = -StrictMath.log(Double.MAX_VALUE);
        final double endT = FastMath.sqrt(2) * StrictMath.sqrt(Double.MAX_VALUE);
        final double end = -2 * StrictMath.log(endT);

        final double step = (end - start) / 1024.0;
        for (double x = start; x > end; x += step) {
            assertEquals(0.0,
                    (FastMath.cosh(x) - StrictMath.cosh(x)) / StrictMath.cosh(x),
                    1e-15);
            assertEquals(0.0,
                    (FastMath.sinh(x) - StrictMath.sinh(x)) / StrictMath.sinh(x),
                    1e-15);
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Hyperbolic Functions
    // =========================================================================

    @Test(timeout = 4000)
    public void testHyperbolicCore() {
        assertEquals(1.0, FastMath.cosh(0.0), EPSILON);
        assertEquals(0.0, FastMath.sinh(0.0), EPSILON);
        assertEquals(0.0, FastMath.tanh(0.0), EPSILON);

        assertEquals(StrictMath.cosh(1.5), FastMath.cosh(1.5), 1e-14);
        assertEquals(StrictMath.sinh(1.5), FastMath.sinh(1.5), 1e-14);
        assertEquals(StrictMath.tanh(1.5), FastMath.tanh(1.5), 1e-14);

        // Symmetry
        assertEquals(FastMath.cosh(1.2), FastMath.cosh(-1.2), EPSILON);
        assertEquals(-FastMath.sinh(1.2), FastMath.sinh(-1.2), EPSILON);
        assertEquals(-FastMath.tanh(1.2), FastMath.tanh(-1.2), EPSILON);

        // Magnitude > 20
        assertEquals(0.5 * FastMath.exp(25.0), FastMath.cosh(25.0), 1e-10);
        assertEquals(0.5 * FastMath.exp(25.0), FastMath.sinh(25.0), 1e-10);
        assertEquals(-0.5 * FastMath.exp(25.0), FastMath.sinh(-25.0), 1e-10);
        assertEquals(1.0, FastMath.tanh(25.0), EPSILON);
        assertEquals(-1.0, FastMath.tanh(-25.0), EPSILON);

        // Intermediate ranges
        assertTrue(FastMath.sinh(0.1) > 0.0);
        assertTrue(FastMath.sinh(-0.1) < 0.0);
        assertTrue(FastMath.tanh(0.4) > 0.0);
        assertTrue(FastMath.tanh(-0.4) < 0.0);
    }

    @Test(timeout = 4000)
    public void testInverseHyperbolic() {
        assertEquals(0.0, FastMath.acosh(1.0), EPSILON);
        assertEquals(StrictMath.log(2.0 + Math.sqrt(3.0)), FastMath.acosh(2.0), 1e-14);

        // asinh branches based on magnitude
        assertEquals(0.0, FastMath.asinh(0.0), EPSILON);
        assertEquals(-FastMath.asinh(0.001), FastMath.asinh(-0.001), EPSILON);
        assertEquals(-FastMath.asinh(0.02), FastMath.asinh(-0.02), EPSILON);
        assertEquals(-FastMath.asinh(0.05), FastMath.asinh(-0.05), EPSILON);
        assertEquals(-FastMath.asinh(0.12), FastMath.asinh(-0.12), EPSILON);
        assertEquals(-FastMath.asinh(2.0), FastMath.asinh(-2.0), EPSILON);

        // atanh branches
        assertEquals(0.0, FastMath.atanh(0.0), EPSILON);
        assertEquals(-FastMath.atanh(0.001), FastMath.atanh(-0.001), EPSILON);
        assertEquals(-FastMath.atanh(0.02), FastMath.atanh(-0.02), EPSILON);
        assertEquals(-FastMath.atanh(0.05), FastMath.atanh(-0.05), EPSILON);
        assertEquals(-FastMath.atanh(0.10), FastMath.atanh(-0.10), EPSILON);
        assertEquals(-FastMath.atanh(0.5), FastMath.atanh(-0.5), EPSILON);
    }

    // =========================================================================
    // Partition A & B: Exponential and Logarithmic Functions
    // =========================================================================

    @Test(timeout = 4000)
    public void testExpAndExpm1() {
        assertEquals(1.0, FastMath.exp(0.0), EPSILON);
        assertEquals(FastMath.E, FastMath.exp(1.0), 1e-14);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(750.0), 0.0);
        assertEquals(0.0, FastMath.exp(-750.0), 0.0);

        // Negative subnormal branches in exp
        assertTrue(FastMath.exp(-715.0) > 0.0);
        assertTrue(FastMath.exp(-709.5) > 0.0);

        // expm1
        assertEquals(0.0, FastMath.expm1(0.0), 0.0);
        assertEquals(-0.0, FastMath.expm1(-0.0), 0.0);
        assertEquals(FastMath.E - 1.0, FastMath.expm1(1.0), 1e-14);
        assertEquals(FastMath.exp(-2.0) - 1.0, FastMath.expm1(-2.0), 1e-14);
        assertEquals(FastMath.exp(2.0) - 1.0, FastMath.expm1(2.0), 1e-14);

        // Small expm1 around 0
        assertEquals(0.5e-5, FastMath.expm1(0.5e-5), 1e-10);
        assertEquals(-0.5e-5, FastMath.expm1(-0.5e-5), 1e-10);
    }

    @Test(timeout = 4000)
    public void testLogarithms() {
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(-0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.log(-1.0)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.log(1.0), EPSILON);

        // Near 1.0 (Remez polynomial quick branch)
        assertEquals(Math.log(1.005), FastMath.log(1.005), 1e-15);
        assertEquals(Math.log(0.995), FastMath.log(0.995), 1e-15);

        // Subnormal log input
        double subnormal = Double.longBitsToDouble(0x0000000000000004L);
        assertEquals(StrictMath.log(subnormal), FastMath.log(subnormal), 1e-14);

        // log10
        assertEquals(1.0, FastMath.log10(10.0), EPSILON);
        assertEquals(2.0, FastMath.log10(100.0), EPSILON);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log10(0.0), 0.0);

        // log1p
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log1p(-1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log1p(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.log1p(0.0), EPSILON);
        assertEquals(StrictMath.log1p(1e-8), FastMath.log1p(1e-8), 1e-18);
        assertEquals(StrictMath.log1p(-1e-8), FastMath.log1p(-1e-8), 1e-18);
        assertEquals(StrictMath.log1p(2.0), FastMath.log1p(2.0), 1e-14);

        // Custom base log
        assertEquals(3.0, FastMath.log(2.0, 8.0), 1e-14);
        assertEquals(0.0, FastMath.log(0.0, 5.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(5.0, 0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.log(0.0, 0.0)));
    }

    @Test(timeout = 4000)
    public void testPowDoubleDouble() {
        assertEquals(1.0, FastMath.pow(123.45, 0.0), EPSILON);
        assertEquals(1.0, FastMath.pow(0.0, 0.0), EPSILON);
        assertEquals(1.0, FastMath.pow(Double.NaN, 0.0), EPSILON);

        assertTrue(Double.isNaN(FastMath.pow(Double.NaN, 2.0)));
        assertTrue(Double.isNaN(FastMath.pow(2.0, Double.NaN)));

        // x == 0
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -2.0), 0.0);
        assertEquals(0.0, FastMath.pow(0.0, 2.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.pow(-0.0, -3.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(-0.0, -2.0), 0.0);
        assertEquals(-0.0, FastMath.pow(-0.0, 3.0), 0.0);
        assertEquals(0.0, FastMath.pow(-0.0, 2.0), 0.0);

        // x == Infinity
        assertEquals(0.0, FastMath.pow(Double.POSITIVE_INFINITY, -1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals(-0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -3.0), 0.0);
        assertEquals(0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -2.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 3.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 2.0), 0.0);

        // y == Infinity
        assertTrue(Double.isNaN(FastMath.pow(1.0, Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.pow(-1.0, Double.POSITIVE_INFINITY)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(2.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.pow(0.5, Double.POSITIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.pow(1.0, Double.NEGATIVE_INFINITY)));
        assertEquals(0.0, FastMath.pow(2.0, Double.NEGATIVE_INFINITY), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.5, Double.NEGATIVE_INFINITY), 0.0);

        // x < 0
        assertEquals(4.0, FastMath.pow(-2.0, 2.0), EPSILON);
        assertEquals(-8.0, FastMath.pow(-2.0, 3.0), EPSILON);
        assertTrue(Double.isNaN(FastMath.pow(-2.0, 2.5)));
        assertEquals(1.0, FastMath.pow(-1.0, 4503599627370496.0), EPSILON); // >= TWO_POWER_52

        // Large y split path
        assertEquals(0.0, FastMath.pow(0.999999, 1e300), 0.0);

        // Standard pow
        assertEquals(8.0, FastMath.pow(2.0, 3.0), EPSILON);
        assertEquals(0.125, FastMath.pow(2.0, -3.0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testPowDoubleInt() {
        assertEquals(1.0, FastMath.pow(5.5, 0), EPSILON);
        assertEquals(25.0, FastMath.pow(5.0, 2), EPSILON);
        assertEquals(125.0, FastMath.pow(5.0, 3), EPSILON);
        assertEquals(0.04, FastMath.pow(5.0, -2), EPSILON);
        assertEquals(-8.0, FastMath.pow(-2.0, 3), EPSILON);
        assertEquals(16.0, FastMath.pow(-2.0, 4), EPSILON);
    }

    // =========================================================================
    // Partition A & B: Trigonometric Functions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTrigonometricCore() {
        assertEquals(0.0, FastMath.sin(0.0), 0.0);
        assertEquals(-0.0, FastMath.sin(-0.0), 0.0);
        assertEquals(1.0, FastMath.cos(0.0), EPSILON);
        assertEquals(0.0, FastMath.tan(0.0), 0.0);
        assertEquals(-0.0, FastMath.tan(-0.0), 0.0);

        assertTrue(Double.isNaN(FastMath.sin(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.sin(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.cos(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.cos(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.tan(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.tan(Double.POSITIVE_INFINITY)));

        // Cody-Waite reduction range (1.57 < x <= 3294198.0)
        assertEquals(StrictMath.sin(3.0), FastMath.sin(3.0), 1e-14);
        assertEquals(StrictMath.cos(3.0), FastMath.cos(3.0), 1e-14);
        assertEquals(StrictMath.tan(3.0), FastMath.tan(3.0), 1e-14);

        assertEquals(StrictMath.sin(100.0), FastMath.sin(100.0), 1e-14);
        assertEquals(StrictMath.cos(100.0), FastMath.cos(100.0), 1e-14);
        assertEquals(StrictMath.tan(100.0), FastMath.tan(100.0), 1e-14);

        // Payne-Hanek reduction range (x > 3294198.0)
        double hugeX = 1e7;
        assertEquals(StrictMath.sin(hugeX), FastMath.sin(hugeX), 1e-12);
        assertEquals(StrictMath.cos(hugeX), FastMath.cos(hugeX), 1e-12);
        assertEquals(StrictMath.tan(hugeX), FastMath.tan(hugeX), 1e-12);

        // Negative quadrant flips
        assertEquals(-FastMath.sin(2.5), FastMath.sin(-2.5), EPSILON);
        assertEquals(FastMath.cos(2.5), FastMath.cos(-2.5), EPSILON);
        assertEquals(-FastMath.tan(2.5), FastMath.tan(-2.5), EPSILON);

        // tan boundary around 1.5
        assertEquals(StrictMath.tan(1.52), FastMath.tan(1.52), 1e-13);
    }

    @Test(timeout = 4000)
    public void testInverseTrigonometric() {
        assertEquals(0.0, FastMath.asin(0.0), 0.0);
        assertEquals(-0.0, FastMath.asin(-0.0), 0.0);
        assertEquals(FastMath.PI / 2.0, FastMath.asin(1.0), EPSILON);
        assertEquals(-FastMath.PI / 2.0, FastMath.asin(-1.0), EPSILON);
        assertTrue(Double.isNaN(FastMath.asin(1.01)));
        assertTrue(Double.isNaN(FastMath.asin(-1.01)));
        assertTrue(Double.isNaN(FastMath.asin(Double.NaN)));

        assertEquals(FastMath.PI / 2.0, FastMath.acos(0.0), EPSILON);
        assertEquals(0.0, FastMath.acos(1.0), EPSILON);
        assertEquals(FastMath.PI, FastMath.acos(-1.0), EPSILON);
        assertTrue(Double.isNaN(FastMath.acos(1.01)));
        assertTrue(Double.isNaN(FastMath.acos(-1.01)));
        assertTrue(Double.isNaN(FastMath.acos(Double.NaN)));

        // atan
        assertEquals(0.0, FastMath.atan(0.0), 0.0);
        assertEquals(-0.0, FastMath.atan(-0.0), 0.0);
        assertEquals(FastMath.PI / 4.0, FastMath.atan(1.0), EPSILON);
        assertEquals(-FastMath.PI / 4.0, FastMath.atan(-1.0), EPSILON);
        assertEquals(FastMath.PI / 2.0, FastMath.atan(2e16), 1e-14);
        assertEquals(-FastMath.PI / 2.0, FastMath.atan(-2e16), 1e-14);

        // atan2
        assertTrue(Double.isNaN(FastMath.atan2(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.atan2(1.0, Double.NaN)));
        assertEquals(0.0, FastMath.atan2(0.0, 1.0), 0.0);
        assertEquals(-0.0, FastMath.atan2(-0.0, 1.0), 0.0);
        assertEquals(FastMath.PI, FastMath.atan2(0.0, -1.0), EPSILON);
        assertEquals(-FastMath.PI, FastMath.atan2(-0.0, -1.0), EPSILON);

        // atan2 with Infinities
        assertEquals(FastMath.PI / 4.0, FastMath.atan2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), EPSILON);
        assertEquals(3.0 * FastMath.PI / 4.0, FastMath.atan2(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), EPSILON);
        assertEquals(-FastMath.PI / 4.0, FastMath.atan2(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), EPSILON);
        assertEquals(-3.0 * FastMath.PI / 4.0, FastMath.atan2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), EPSILON);

        assertEquals(FastMath.PI / 2.0, FastMath.atan2(Double.POSITIVE_INFINITY, 5.0), EPSILON);
        assertEquals(-FastMath.PI / 2.0, FastMath.atan2(Double.NEGATIVE_INFINITY, 5.0), EPSILON);
        assertEquals(0.0, FastMath.atan2(5.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(-0.0, FastMath.atan2(-5.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(FastMath.PI, FastMath.atan2(5.0, Double.NEGATIVE_INFINITY), EPSILON);
        assertEquals(-FastMath.PI, FastMath.atan2(-5.0, Double.NEGATIVE_INFINITY), EPSILON);

        // atan2 with x == 0
        assertEquals(FastMath.PI / 2.0, FastMath.atan2(5.0, 0.0), EPSILON);
        assertEquals(-FastMath.PI / 2.0, FastMath.atan2(-5.0, 0.0), EPSILON);
    }

    // =========================================================================
    // Partition A & B: Roots, Conversion & Rounding
    // =========================================================================

    @Test(timeout = 4000)
    public void testRoots() {
        assertEquals(3.0, FastMath.sqrt(9.0), EPSILON);
        assertEquals(2.0, FastMath.cbrt(8.0), EPSILON);
        assertEquals(-2.0, FastMath.cbrt(-8.0), EPSILON);
        assertEquals(0.0, FastMath.cbrt(0.0), 0.0);
        assertEquals(-0.0, FastMath.cbrt(-0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.cbrt(Double.NaN)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.cbrt(Double.POSITIVE_INFINITY), 0.0);

        // cbrt subnormal branch
        double subnormal = Double.longBitsToDouble(0x0000000000000008L);
        assertEquals(StrictMath.cbrt(subnormal), FastMath.cbrt(subnormal), 1e-14);

        // hypot
        assertEquals(5.0, FastMath.hypot(3.0, 4.0), EPSILON);
        assertEquals(5.0, FastMath.hypot(-3.0, -4.0), EPSILON);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(1.0, Double.NEGATIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.hypot(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.hypot(1.0, Double.NaN)));

        // Negligible side branches (expX > expY + 27 and vice versa)
        assertEquals(1e100, FastMath.hypot(1e100, 1.0), 1e-10);
        assertEquals(1e100, FastMath.hypot(1.0, 1e100), 1e-10);
    }

    @Test(timeout = 4000)
    public void testRoundingAndFloorCeil() {
        // floor
        assertEquals(2.0, FastMath.floor(2.9), EPSILON);
        assertEquals(-3.0, FastMath.floor(-2.1), EPSILON);
        assertEquals(0.0, FastMath.floor(0.0), 0.0);
        assertEquals(-0.0, FastMath.floor(-0.5), 0.0);
        assertTrue(Double.isNaN(FastMath.floor(Double.NaN)));
        assertEquals(1e18, FastMath.floor(1e18), 0.0);

        // ceil
        assertEquals(3.0, FastMath.ceil(2.1), EPSILON);
        assertEquals(-2.0, FastMath.ceil(-2.9), EPSILON);
        assertEquals(-0.0, FastMath.ceil(-0.0), 0.0);
        assertEquals(0.0, FastMath.ceil(-0.9), 0.0);
        assertTrue(Double.isNaN(FastMath.ceil(Double.NaN)));

        // rint
        assertEquals(2.0, FastMath.rint(2.4), EPSILON);
        assertEquals(3.0, FastMath.rint(2.6), EPSILON);
        assertEquals(2.0, FastMath.rint(2.5), EPSILON); // round to even
        assertEquals(4.0, FastMath.rint(3.5), EPSILON); // round to even
        assertEquals(-0.0, FastMath.rint(-0.4), 0.0);

        // round
        assertEquals(3L, FastMath.round(2.6));
        assertEquals(2L, FastMath.round(2.4));
        assertEquals(-2L, FastMath.round(-2.4));
        assertEquals(3, FastMath.round(2.6f));
        assertEquals(2, FastMath.round(2.4f));
    }

    @Test(timeout = 4000)
    public void testAngleConversion() {
        assertEquals(0.0, FastMath.toRadians(0.0), 0.0);
        assertEquals(-0.0, FastMath.toRadians(-0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.toRadians(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(FastMath.PI, FastMath.toRadians(180.0), 1e-14);

        assertEquals(0.0, FastMath.toDegrees(0.0), 0.0);
        assertEquals(-0.0, FastMath.toDegrees(-0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.toDegrees(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(180.0, FastMath.toDegrees(FastMath.PI), 1e-14);
    }

    // =========================================================================
    // Partition A & B: Bit Manipulation, Scalb, NextAfter, Signum, Min, Max, Abs
    // =========================================================================

    @Test(timeout = 4000)
    public void testScalbDouble() {
        assertEquals(8.0, FastMath.scalb(2.0, 2), EPSILON);
        assertEquals(0.5, FastMath.scalb(2.0, -2), EPSILON);
        assertEquals(0.0, FastMath.scalb(0.0, 10), 0.0);
        assertTrue(Double.isNaN(FastMath.scalb(Double.NaN, 5)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(Double.POSITIVE_INFINITY, 5), 0.0);

        // Large scaling powers
        assertEquals(0.0, FastMath.scalb(1.0, -2100), 0.0);
        assertEquals(-0.0, FastMath.scalb(-1.0, -2100), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(1.0, 2100), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-1.0, 2100), 0.0);

        // Subnormal transitions
        double subnormal = Double.MIN_VALUE;
        assertTrue(FastMath.scalb(subnormal, 1030) > 0.0);
        assertTrue(FastMath.scalb(1.0, -1030) > 0.0);
    }

    @Test(timeout = 4000)
    public void testScalbFloat() {
        assertEquals(8.0f, FastMath.scalb(2.0f, 2), 1e-6f);
        assertEquals(0.5f, FastMath.scalb(2.0f, -2), 1e-6f);
        assertEquals(0.0f, FastMath.scalb(0.0f, 10), 0.0f);
        assertTrue(Float.isNaN(FastMath.scalb(Float.NaN, 5)));
        assertEquals(Float.POSITIVE_INFINITY, FastMath.scalb(Float.POSITIVE_INFINITY, 5), 0.0f);

        // Large scaling powers
        assertEquals(0.0f, FastMath.scalb(1.0f, -300), 0.0f);
        assertEquals(-0.0f, FastMath.scalb(-1.0f, -300), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, FastMath.scalb(1.0f, 300), 0.0f);
        assertEquals(Float.NEGATIVE_INFINITY, FastMath.scalb(-1.0f, 300), 0.0f);

        // Subnormal transitions
        float subnormal = Float.MIN_VALUE;
        assertTrue(FastMath.scalb(subnormal, 130) > 0.0f);
        assertTrue(FastMath.scalb(1.0f, -130) > 0.0f);
    }

    @Test(timeout = 4000)
    public void testNextAfterAndNextUp() {
        assertEquals(1.0, FastMath.nextAfter(1.0, 1.0), 0.0);
        assertTrue(Double.isNaN(FastMath.nextAfter(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.nextAfter(1.0, Double.NaN)));

        assertEquals(Double.MIN_VALUE, FastMath.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, FastMath.nextAfter(0.0, -1.0), 0.0);
        assertEquals(Double.MAX_VALUE, FastMath.nextAfter(Double.POSITIVE_INFINITY, 0.0), 0.0);
        assertEquals(-Double.MAX_VALUE, FastMath.nextAfter(Double.NEGATIVE_INFINITY, 0.0), 0.0);

        assertTrue(FastMath.nextUp(1.0) > 1.0);
        assertTrue(FastMath.nextUp(-1.0) > -1.0);

        // Float variants
        assertEquals(1.0f, FastMath.nextAfter(1.0f, 1.0), 0.0f);
        assertTrue(Float.isNaN(FastMath.nextAfter(Float.NaN, 1.0)));
        assertTrue(Float.isNaN(FastMath.nextAfter(1.0f, Double.NaN)));
        assertEquals(Float.MIN_VALUE, FastMath.nextAfter(0.0f, 1.0), 0.0f);
        assertEquals(-Float.MIN_VALUE, FastMath.nextAfter(0.0f, -1.0), 0.0f);
        assertEquals(Float.MAX_VALUE, FastMath.nextAfter(Float.POSITIVE_INFINITY, 0.0), 0.0f);
        assertEquals(-Float.MAX_VALUE, FastMath.nextAfter(Float.NEGATIVE_INFINITY, 0.0), 0.0f);

        assertTrue(FastMath.nextUp(1.0f) > 1.0f);
        assertTrue(FastMath.nextUp(-1.0f) > -1.0f);
    }

    @Test(timeout = 4000)
    public void testSignumAndUlpAndGetExponent() {
        assertEquals(1.0, FastMath.signum(5.0), EPSILON);
        assertEquals(-1.0, FastMath.signum(-5.0), EPSILON);
        assertEquals(0.0, FastMath.signum(0.0), 0.0);
        assertEquals(-0.0, FastMath.signum(-0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.signum(Double.NaN)));

        assertEquals(1.0f, FastMath.signum(5.0f), 1e-6f);
        assertEquals(-1.0f, FastMath.signum(-5.0f), 1e-6f);
        assertEquals(0.0f, FastMath.signum(0.0f), 0.0f);
        assertEquals(-0.0f, FastMath.signum(-0.0f), 0.0f);
        assertTrue(Float.isNaN(FastMath.signum(Float.NaN)));

        // ulp
        assertEquals(Double.POSITIVE_INFINITY, FastMath.ulp(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Float.POSITIVE_INFINITY, FastMath.ulp(Float.POSITIVE_INFINITY), 0.0f);
        assertTrue(FastMath.ulp(1.0) > 0.0);
        assertTrue(FastMath.ulp(1.0f) > 0.0f);

        // getExponent
        assertEquals(0, FastMath.getExponent(1.0));
        assertEquals(3, FastMath.getExponent(8.0));
        assertEquals(0, FastMath.getExponent(1.0f));
        assertEquals(3, FastMath.getExponent(8.0f));
    }

    @Test(timeout = 4000)
    public void testMinMaxAbsAndCopySign() {
        // min / max int
        assertEquals(3, FastMath.min(3, 5));
        assertEquals(3, FastMath.min(5, 3));
        assertEquals(5, FastMath.max(3, 5));
        assertEquals(5, FastMath.max(5, 3));

        // min / max long
        assertEquals(3L, FastMath.min(3L, 5L));
        assertEquals(3L, FastMath.min(5L, 3L));
        assertEquals(5L, FastMath.max(3L, 5L));
        assertEquals(5L, FastMath.max(5L, 3L));

        // min / max float with -0.0 and NaN
        assertEquals(-0.0f, FastMath.min(-0.0f, 0.0f), 0.0f);
        assertEquals(0.0f, FastMath.max(-0.0f, 0.0f), 0.0f);
        assertTrue(Float.isNaN(FastMath.min(Float.NaN, 1.0f)));
        assertTrue(Float.isNaN(FastMath.max(Float.NaN, 1.0f)));
        assertEquals(1.0f, FastMath.min(1.0f, 2.0f), 1e-6f);
        assertEquals(2.0f, FastMath.max(1.0f, 2.0f), 1e-6f);

        // min / max double with -0.0 and NaN
        assertEquals(-0.0, FastMath.min(-0.0, 0.0), 0.0);
        assertEquals(0.0, FastMath.max(-0.0, 0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.min(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.max(Double.NaN, 1.0)));
        assertEquals(1.0, FastMath.min(1.0, 2.0), EPSILON);
        assertEquals(2.0, FastMath.max(1.0, 2.0), EPSILON);

        // abs
        assertEquals(5, FastMath.abs(-5));
        assertEquals(5, FastMath.abs(5));
        assertEquals(5L, FastMath.abs(-5L));
        assertEquals(5L, FastMath.abs(5L));
        assertEquals(5.0f, FastMath.abs(-5.0f), 1e-6f);
        assertEquals(0.0f, FastMath.abs(-0.0f), 0.0f);
        assertEquals(5.0, FastMath.abs(-5.0), EPSILON);
        assertEquals(0.0, FastMath.abs(-0.0), 0.0);

        // copySign
        assertEquals(2.0, FastMath.copySign(2.0, 1.0), 0.0);
        assertEquals(-2.0, FastMath.copySign(2.0, -1.0), 0.0);
        assertEquals(2.0, FastMath.copySign(-2.0, 1.0), 0.0);
        assertEquals(-2.0, FastMath.copySign(-2.0, -1.0), 0.0);
        assertEquals(2.0, FastMath.copySign(2.0, Double.NaN), 0.0);

        assertEquals(2.0f, FastMath.copySign(2.0f, 1.0f), 0.0f);
        assertEquals(-2.0f, FastMath.copySign(2.0f, -1.0f), 0.0f);
        assertEquals(2.0f, FastMath.copySign(-2.0f, 1.0f), 0.0f);
        assertEquals(-2.0f, FastMath.copySign(-2.0f, -1.0f), 0.0f);
        assertEquals(2.0f, FastMath.copySign(2.0f, Float.NaN), 0.0f);
    }

    // =========================================================================
    // Partition D & E: Lifecycle, Delegations & Utility
    // =========================================================================

    @Test(timeout = 4000)
    public void testStrictMathDelegations() {
        assertEquals(StrictMath.IEEEremainder(7.0, 3.0), FastMath.IEEEremainder(7.0, 3.0), EPSILON);
        double r = FastMath.random();
        assertTrue(r >= 0.0 && r < 1.0);
    }

    @Test(timeout = 4000)
    public void testPrivateConstructorViaReflection() throws Exception {
        Constructor<FastMath> constructor = FastMath.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        FastMath instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test(timeout = 4000)
    public void testMainMethodExecution() {
        // FastMath.main prints array tables for verification and exercises table loader
        FastMath.main(new String[0]);
    }
}