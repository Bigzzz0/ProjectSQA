package org.apache.commons.math3.util;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Decision Branches & Boundary Conditions:
 * 1. Defect MATH-904:
 *    - In FastMath.pow(x, y), when x < 0 and |y| is an odd integer in [2^52, 2^53),
 *      the implementation prematurely assumes |y| >= 2^52 is always even due to checking
 *      against TWO_POWER_52 (2^52) rather than 2^53.
 *      Specifically, y = 2^52 + 1.0 = 4503599627370497.0 is an odd integer.
 *      FastMath.pow(-1.0, 4503599627370497.0) erroneously returns 1.0 instead of -1.0.
 *
 * 2. Exponential & Hyperbolic Functions:
 *    - exp(x): x < 0 (intVal > 746, 709 < intVal <= 746, intVal == 709, intVal <= 708);
 *              x >= 0 (intVal > 709 -> +Inf, normal cases).
 *    - expm1(x): NaN, +/-0.0, |x| >= 1.0 (positive & negative), |x| < 1.0 (positive & negative).
 *    - cosh(x), sinh(x), tanh(x): NaN, |x| > 20 (with |x| >= LOG_MAX_VALUE overflow checks),
 *      zero with sign preservation, small ranges (|x| <= 0.25, |x| < 0.5).
 *    - asinh, acosh, atanh: negative branches, piecewise polynomial zones (<0.0036, <0.036, <0.097, etc.).
 *
 * 3. Logarithmic Functions:
 *    - log(x): 0.0 (-Inf), negative or NaN, +Inf, subnormal normalization loop,
 *      Taylor series around 1.0 (0.99 < x < 1.01).
 *    - log1p(x): x == -1 (-Inf), x == +Inf, |x| > 1e-6, |x| <= 1e-6.
 *    - log10(x), log(base, x).
 *
 * 4. Trigonometric Functions & Argument Reductions:
 *    - sin(x), cos(x), tan(x): +/-0.0, NaN, +Inf, Cody-Waite reduction (x > 1.5707963...),
 *      Payne-Hanek reduction (x > 3294198.0), 4-quadrant switching, tan(x) accuracy boundary (xa > 1.5).
 *    - asin(x), acos(x), atan(x), atan2(y, x): boundary limits (+/-1.0, 0.0, large inputs, special quadrant checks).
 *
 * 5. Arithmetic, Bitwise & Floating-Point Helpers:
 *    - scalb(double, int) & scalb(float, int): normal exponent adjustments, underflow/overflow thresholds,
 *      subnormal normalization loop and round-up bit additions.
 *    - nextAfter, nextUp, ulp, copySign, getExponent, floor, ceil, rint, round, min, max, hypot, abs.
 */
public class FastMathGeminiTest {

    private static final double EPSILON = 1e-14;

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-904)
    // =========================================================================

    /**
     * Targets MATH-904:
     * FastMath.pow(-1.0, y) where y is an odd integer in [2^52, 2^53).
     * 2^52 = 4503599627370496.0; 2^52 + 1.0 = 4503599627370497.0 (odd integer).
     * FastMath mistakenly treats y >= 2^52 as an even integer, returning 1.0 instead of -1.0.
     */
    @Test(timeout = 4000)
    public void testMath904() {
        final double oddLargeInteger = 4503599627370497.0; // 2^52 + 1.0
        final double result = FastMath.pow(-1.0, oddLargeInteger);
        assertEquals(-1.0, result, 0.0);
    }

    /**
     * Targets negative large odd integer for MATH-904.
     */
    @Test(timeout = 4000)
    public void testMath904NegativeExponent() {
        final double oddLargeInteger = -4503599627370497.0; // -(2^52 + 1.0)
        final double result = FastMath.pow(-1.0, oddLargeInteger);
        assertEquals(-1.0, result, 0.0);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testExpAndExpm1Core() {
        assertEquals(1.0, FastMath.exp(0.0), EPSILON);
        assertEquals(Math.E, FastMath.exp(1.0), 1e-13);
        assertEquals(0.0, FastMath.expm1(0.0), 0.0);
        assertEquals(Math.E - 1.0, FastMath.expm1(1.0), 1e-13);

        // Positive and negative non-zero expm1
        assertEquals(Math.exp(0.5) - 1.0, FastMath.expm1(0.5), EPSILON);
        assertEquals(Math.exp(-0.5) - 1.0, FastMath.expm1(-0.5), EPSILON);
        assertEquals(Math.exp(2.0) - 1.0, FastMath.expm1(2.0), 1e-12);
        assertEquals(Math.exp(-2.0) - 1.0, FastMath.expm1(-2.0), 1e-14);
    }

    @Test(timeout = 4000)
    public void testLogarithmsCore() {
        assertEquals(0.0, FastMath.log(1.0), EPSILON);
        assertEquals(1.0, FastMath.log(FastMath.E), EPSILON);

        // 0.99 < x < 1.01 (LN_QUICK_COEF branch)
        assertEquals(Math.log(1.005), FastMath.log(1.005), EPSILON);
        assertEquals(Math.log(0.995), FastMath.log(0.995), EPSILON);

        // log1p
        assertEquals(0.0, FastMath.log1p(0.0), 0.0);
        assertEquals(Math.log(2.0), FastMath.log1p(1.0), EPSILON);
        assertEquals(Math.log1p(1e-7), FastMath.log1p(1e-7), 1e-18);
        assertEquals(Math.log1p(-1e-7), FastMath.log1p(-1e-7), 1e-18);

        // log10 and arbitrary base log
        assertEquals(1.0, FastMath.log10(10.0), EPSILON);
        assertEquals(2.0, FastMath.log10(100.0), EPSILON);
        assertEquals(3.0, FastMath.log(2.0, 8.0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testTrigonometricCore() {
        assertEquals(0.0, FastMath.sin(0.0), 0.0);
        assertEquals(1.0, FastMath.sin(FastMath.PI / 2.0), EPSILON);
        assertEquals(1.0, FastMath.cos(0.0), EPSILON);
        assertEquals(0.0, FastMath.cos(FastMath.PI / 2.0), EPSILON);
        assertEquals(1.0, FastMath.tan(FastMath.PI / 4.0), EPSILON);

        // Cody-Waite reduction (x > PI/2)
        assertEquals(Math.sin(2.5), FastMath.sin(2.5), EPSILON);
        assertEquals(Math.cos(2.5), FastMath.cos(2.5), EPSILON);
        assertEquals(Math.tan(2.5), FastMath.tan(2.5), EPSILON);

        // Tan accuracy boundary (xa > 1.5)
        assertEquals(Math.tan(1.55), FastMath.tan(1.55), EPSILON);

        // Inverse trigonometric
        assertEquals(FastMath.PI / 4.0, FastMath.atan(1.0), EPSILON);
        assertEquals(FastMath.PI / 2.0, FastMath.asin(1.0), EPSILON);
        assertEquals(0.0, FastMath.acos(1.0), EPSILON);
        assertEquals(FastMath.PI / 2.0, FastMath.acos(0.0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testHyperbolicCore() {
        assertEquals(1.0, FastMath.cosh(0.0), EPSILON);
        assertEquals(0.0, FastMath.sinh(0.0), 0.0);
        assertEquals(0.0, FastMath.tanh(0.0), 0.0);

        // Normal ranges (0 < |x| <= 20)
        assertEquals(Math.cosh(1.5), FastMath.cosh(1.5), EPSILON);
        assertEquals(Math.sinh(1.5), FastMath.sinh(1.5), EPSILON);
        assertEquals(Math.tanh(1.5), FastMath.tanh(1.5), EPSILON);

        assertEquals(Math.cosh(-1.5), FastMath.cosh(-1.5), EPSILON);
        assertEquals(Math.sinh(-1.5), FastMath.sinh(-1.5), EPSILON);
        assertEquals(Math.tanh(-1.5), FastMath.tanh(-1.5), EPSILON);

        // Small range sinh (|x| <= 0.25) and tanh (|x| < 0.5)
        assertEquals(Math.sinh(0.1), FastMath.sinh(0.1), EPSILON);
        assertEquals(Math.sinh(-0.1), FastMath.sinh(-0.1), EPSILON);
        assertEquals(Math.tanh(0.2), FastMath.tanh(0.2), EPSILON);
        assertEquals(Math.tanh(-0.2), FastMath.tanh(-0.2), EPSILON);

        // Inverse hyperbolic
        assertEquals(0.0, FastMath.acosh(1.0), EPSILON);
        assertEquals(Math.log(2.0 + Math.sqrt(3.0)), FastMath.acosh(2.0), EPSILON);

        // asinh polynomial intervals: >0.167, 0.097-0.167, 0.036-0.097, 0.0036-0.036, <=0.0036
        assertEquals(0.2, FastMath.asinh(Math.sinh(0.2)), EPSILON);
        assertEquals(0.12, FastMath.asinh(Math.sinh(0.12)), EPSILON);
        assertEquals(0.05, FastMath.asinh(Math.sinh(0.05)), EPSILON);
        assertEquals(0.01, FastMath.asinh(Math.sinh(0.01)), EPSILON);
        assertEquals(0.001, FastMath.asinh(Math.sinh(0.001)), EPSILON);
        assertEquals(-0.2, FastMath.asinh(Math.sinh(-0.2)), EPSILON);

        // atanh polynomial intervals: >0.15, 0.087-0.15, 0.031-0.087, 0.003-0.031, <=0.003
        assertEquals(0.2, FastMath.atanh(Math.tanh(0.2)), EPSILON);
        assertEquals(0.1, FastMath.atanh(Math.tanh(0.1)), EPSILON);
        assertEquals(0.05, FastMath.atanh(Math.tanh(0.05)), EPSILON);
        assertEquals(0.01, FastMath.atanh(Math.tanh(0.01)), EPSILON);
        assertEquals(0.001, FastMath.atanh(Math.tanh(0.001)), EPSILON);
        assertEquals(-0.2, FastMath.atanh(Math.tanh(-0.2)), EPSILON);
    }

    @Test(timeout = 4000)
    public void testPowDoubleInt() {
        assertEquals(1.0, FastMath.pow(5.5, 0), EPSILON);
        assertEquals(25.0, FastMath.pow(5.0, 2), EPSILON);
        assertEquals(125.0, FastMath.pow(5.0, 3), EPSILON);
        assertEquals(0.04, FastMath.pow(5.0, -2), EPSILON);
        assertEquals(1.0, FastMath.pow(-1.0, 2), EPSILON);
        assertEquals(-1.0, FastMath.pow(-1.0, 3), EPSILON);
    }

    @Test(timeout = 4000)
    public void testCbrtAndSqrt() {
        assertEquals(0.0, FastMath.sqrt(0.0), 0.0);
        assertEquals(3.0, FastMath.sqrt(9.0), EPSILON);
        assertEquals(0.0, FastMath.cbrt(0.0), 0.0);
        assertEquals(2.0, FastMath.cbrt(8.0), EPSILON);
        assertEquals(-2.0, FastMath.cbrt(-8.0), EPSILON);

        // Subnormal cbrt
        final double subnormal = Double.MIN_VALUE;
        final double cbrtSub = FastMath.cbrt(subnormal);
        assertTrue(cbrtSub > 0.0);
        assertEquals(Math.cbrt(subnormal), cbrtSub, Math.cbrt(subnormal) * 1e-10);
    }

    @Test(timeout = 4000)
    public void testAngleConversions() {
        assertEquals(FastMath.PI, FastMath.toRadians(180.0), EPSILON);
        assertEquals(180.0, FastMath.toDegrees(FastMath.PI), EPSILON);
        assertEquals(0.0, FastMath.toRadians(0.0), 0.0);
        assertEquals(0.0, FastMath.toDegrees(0.0), 0.0);
        assertTrue(Double.isInfinite(FastMath.toRadians(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isInfinite(FastMath.toDegrees(Double.NEGATIVE_INFINITY)));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSpecialValuesExp() {
        // Extreme negative exp
        assertEquals(0.0, FastMath.exp(-750.0), 0.0); // intVal > 746
        assertTrue(FastMath.exp(-715.0) > 0.0);        // intVal > 709 (subnormal output)
        assertTrue(FastMath.exp(-709.5) > 0.0);        // intVal == 709

        // Extreme positive exp
        assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(710.0), 0.0); // intVal > 709
    }

    @Test(timeout = 4000)
    public void testSpecialValuesLog() {
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(-0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.log(-1.0)));
        assertTrue(Double.isNaN(FastMath.log(Double.NaN)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log(Double.POSITIVE_INFINITY), 0.0);

        // Subnormal positive log
        assertEquals(Math.log(Double.MIN_VALUE), FastMath.log(Double.MIN_VALUE), 1e-10);

        // log1p extremes
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log1p(-1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log1p(Double.POSITIVE_INFINITY), 0.0);
    }

    @Test(timeout = 4000)
    public void testSpecialValuesHyperbolic() {
        // NaN inputs
        assertTrue(Double.isNaN(FastMath.cosh(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.sinh(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.tanh(Double.NaN)));

        // cosh / sinh large arguments (|x| > 20 and |x| >= LOG_MAX_VALUE)
        assertEquals(Double.POSITIVE_INFINITY, FastMath.cosh(715.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.cosh(-715.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.sinh(715.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.sinh(-715.0), 0.0);

        // cosh / sinh 20 < |x| < LOG_MAX_VALUE
        assertEquals(0.5 * Math.exp(25.0), FastMath.cosh(25.0), 1.0);
        assertEquals(0.5 * Math.exp(25.0), FastMath.cosh(-25.0), 1.0);
        assertEquals(0.5 * Math.exp(25.0), FastMath.sinh(25.0), 1.0);
        assertEquals(-0.5 * Math.exp(25.0), FastMath.sinh(-25.0), 1.0);

        // tanh extremes
        assertEquals(1.0, FastMath.tanh(25.0), 0.0);
        assertEquals(-1.0, FastMath.tanh(-25.0), 0.0);

        // Sign preservation of zero
        assertEquals(Double.doubleToLongBits(0.0), Double.doubleToLongBits(FastMath.sinh(0.0)));
        assertEquals(Double.doubleToLongBits(-0.0), Double.doubleToLongBits(FastMath.sinh(-0.0)));
        assertEquals(Double.doubleToLongBits(0.0), Double.doubleToLongBits(FastMath.tanh(0.0)));
        assertEquals(Double.doubleToLongBits(-0.0), Double.doubleToLongBits(FastMath.tanh(-0.0)));
    }

    @Test(timeout = 4000)
    public void testPayneHanekArgumentReduction() {
        // x > 3294198.0 triggers Payne-Hanek reduction
        final double hugeX = 1e8 * FastMath.PI + 0.5;
        final double sinHuge = FastMath.sin(hugeX);
        final double cosHuge = FastMath.cos(hugeX);
        final double tanHuge = FastMath.tan(hugeX);

        assertEquals(Math.sin(hugeX), sinHuge, 1e-8);
        assertEquals(Math.cos(hugeX), cosHuge, 1e-8);
        assertEquals(Math.tan(hugeX), tanHuge, 1e-8);

        // Negative huge
        assertEquals(Math.sin(-hugeX), FastMath.sin(-hugeX), 1e-8);
        assertEquals(Math.cos(-hugeX), FastMath.cos(-hugeX), 1e-8);
        assertEquals(Math.tan(-hugeX), FastMath.tan(-hugeX), 1e-8);
    }

    @Test(timeout = 4000)
    public void testTrigonometricSpecialCases() {
        // Zero sign preservation
        assertEquals(Double.doubleToLongBits(0.0), Double.doubleToLongBits(FastMath.sin(0.0)));
        assertEquals(Double.doubleToLongBits(-0.0), Double.doubleToLongBits(FastMath.sin(-0.0)));
        assertEquals(Double.doubleToLongBits(0.0), Double.doubleToLongBits(FastMath.tan(0.0)));
        assertEquals(Double.doubleToLongBits(-0.0), Double.doubleToLongBits(FastMath.tan(-0.0)));

        // Infinite / NaN inputs
        assertTrue(Double.isNaN(FastMath.sin(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.sin(Double.NEGATIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.cos(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.cos(Double.NEGATIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.tan(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.tan(Double.NEGATIVE_INFINITY)));

        assertTrue(Double.isNaN(FastMath.sin(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.cos(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.tan(Double.NaN)));

        // Large atan input > 1.633123935319537E16
        assertEquals(FastMath.PI / 2.0, FastMath.atan(2.0e16), EPSILON);
        assertEquals(-FastMath.PI / 2.0, FastMath.atan(-2.0e16), EPSILON);

        // asin / acos boundary & out-of-range
        assertTrue(Double.isNaN(FastMath.asin(1.0001)));
        assertTrue(Double.isNaN(FastMath.asin(-1.0001)));
        assertTrue(Double.isNaN(FastMath.acos(1.0001)));
        assertTrue(Double.isNaN(FastMath.acos(-1.0001)));
        assertEquals(FastMath.PI, FastMath.acos(-1.0), EPSILON);
        assertEquals(-FastMath.PI / 2.0, FastMath.asin(-1.0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testAtan2SpecialCases() {
        assertTrue(Double.isNaN(FastMath.atan2(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.atan2(1.0, Double.NaN)));

        // y == 0
        assertEquals(0.0, FastMath.atan2(0.0, 1.0), 0.0);
        assertEquals(FastMath.PI, FastMath.atan2(0.0, -1.0), EPSILON);
        assertEquals(-FastMath.PI, FastMath.atan2(-0.0, -1.0), EPSILON);
        assertEquals(0.0, FastMath.atan2(0.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(FastMath.PI, FastMath.atan2(0.0, Double.NEGATIVE_INFINITY), EPSILON);

        // Infinite y
        assertEquals(FastMath.PI / 4.0, FastMath.atan2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), EPSILON);
        assertEquals(3.0 * FastMath.PI / 4.0, FastMath.atan2(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), EPSILON);
        assertEquals(FastMath.PI / 2.0, FastMath.atan2(Double.POSITIVE_INFINITY, 10.0), EPSILON);
        assertEquals(-FastMath.PI / 4.0, FastMath.atan2(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), EPSILON);
        assertEquals(-3.0 * FastMath.PI / 4.0, FastMath.atan2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), EPSILON);
        assertEquals(-FastMath.PI / 2.0, FastMath.atan2(Double.NEGATIVE_INFINITY, 10.0), EPSILON);

        // Infinite x
        assertEquals(0.0, FastMath.atan2(1.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(-0.0, FastMath.atan2(-1.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(FastMath.PI, FastMath.atan2(1.0, Double.NEGATIVE_INFINITY), EPSILON);
        assertEquals(-FastMath.PI, FastMath.atan2(-1.0, Double.NEGATIVE_INFINITY), EPSILON);

        // x == 0
        assertEquals(FastMath.PI / 2.0, FastMath.atan2(1.0, 0.0), EPSILON);
        assertEquals(-FastMath.PI / 2.0, FastMath.atan2(-1.0, 0.0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testPowDoubleDoubleSpecialCases() {
        // y == 0.0
        assertEquals(1.0, FastMath.pow(0.0, 0.0), 0.0);
        assertEquals(1.0, FastMath.pow(Double.NaN, 0.0), 0.0);

        // x is NaN
        assertTrue(Double.isNaN(FastMath.pow(Double.NaN, 2.0)));

        // x == 0
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -2.0), 0.0);
        assertEquals(0.0, FastMath.pow(0.0, 2.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.pow(-0.0, -3.0), 0.0); // odd negative
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(-0.0, -2.0), 0.0); // even negative
        assertEquals(-0.0, FastMath.pow(-0.0, 3.0), 0.0);                      // odd positive
        assertEquals(0.0, FastMath.pow(-0.0, 2.0), 0.0);                       // even positive

        // x == +Inf
        assertEquals(0.0, FastMath.pow(Double.POSITIVE_INFINITY, -1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertTrue(Double.isNaN(FastMath.pow(Double.POSITIVE_INFINITY, Double.NaN)));

        // y == +Inf
        assertTrue(Double.isNaN(FastMath.pow(1.0, Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.pow(-1.0, Double.POSITIVE_INFINITY)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(2.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.pow(0.5, Double.POSITIVE_INFINITY), 0.0);

        // x == -Inf
        assertEquals(-0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -3.0), 0.0); // odd negative
        assertEquals(0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -2.0), 0.0);  // even negative
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 3.0), 0.0); // odd positive
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 2.0), 0.0); // even positive

        // y == -Inf
        assertTrue(Double.isNaN(FastMath.pow(1.0, Double.NEGATIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.pow(-1.0, Double.NEGATIVE_INFINITY)));
        assertEquals(0.0, FastMath.pow(2.0, Double.NEGATIVE_INFINITY), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.5, Double.NEGATIVE_INFINITY), 0.0);

        // x < 0: non-integer y -> NaN
        assertTrue(Double.isNaN(FastMath.pow(-2.0, 2.5)));

        // x < 0: integer y
        assertEquals(4.0, FastMath.pow(-2.0, 2.0), EPSILON);
        assertEquals(-8.0, FastMath.pow(-2.0, 3.0), EPSILON);

        // Split y large (y > 8e298)
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(2.0, 1e299), 0.0);
        assertEquals(0.0, FastMath.pow(2.0, -1e299), 0.0);
    }

    // =========================================================================
    // Partition D: Floating-Point Manipulation & Utility Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testScalbDouble() {
        // Fast paths
        assertEquals(8.0, FastMath.scalb(2.0, 2), EPSILON);
        assertEquals(0.5, FastMath.scalb(2.0, -2), EPSILON);

        // Extreme n (> 2097 / < -2098)
        assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(1.0, 2100), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.scalb(-1.0, 2100), 0.0);
        assertEquals(0.0, FastMath.scalb(1.0, -2100), 0.0);
        assertEquals(-0.0, FastMath.scalb(-1.0, -2100), 0.0);

        // Subnormal scaling and scale to 0
        assertEquals(0.0, FastMath.scalb(1.0, -1100), 0.0);
        assertTrue(FastMath.scalb(1.0, -1070) > 0.0); // Subnormal result

        // Subnormal input scaling up (n >= 1024, exponent == 0)
        final double subnormal = Double.MIN_VALUE;
        assertTrue(FastMath.scalb(subnormal, 1050) > 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(subnormal, 2100), 0.0);

        // Special inputs
        assertTrue(Double.isNaN(FastMath.scalb(Double.NaN, 5)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(Double.POSITIVE_INFINITY, 5), 0.0);
        assertEquals(0.0, FastMath.scalb(0.0, 5), 0.0);
    }

    @Test(timeout = 4000)
    public void testScalbFloat() {
        assertEquals(8.0f, FastMath.scalb(2.0f, 2), 1e-6f);
        assertEquals(0.5f, FastMath.scalb(2.0f, -2), 1e-6f);

        assertEquals(Float.POSITIVE_INFINITY, FastMath.scalb(1.0f, 300), 0.0f);
        assertEquals(Float.NEGATIVE_INFINITY, FastMath.scalb(-1.0f, 300), 0.0f);
        assertEquals(0.0f, FastMath.scalb(1.0f, -300), 0.0f);
        assertEquals(-0.0f, FastMath.scalb(-1.0f, -300), 0.0f);

        assertTrue(FastMath.scalb(1.0f, -135) > 0.0f); // Float subnormal result
        assertEquals(0.0f, FastMath.scalb(1.0f, -160), 0.0f);

        final float subnormalF = Float.MIN_VALUE;
        assertTrue(FastMath.scalb(subnormalF, 140) > 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, FastMath.scalb(subnormalF, 300), 0.0f);

        assertTrue(Float.isNaN(FastMath.scalb(Float.NaN, 5)));
        assertEquals(Float.POSITIVE_INFINITY, FastMath.scalb(Float.POSITIVE_INFINITY, 5), 0.0f);
        assertEquals(0.0f, FastMath.scalb(0.0f, 5), 0.0f);
    }

    @Test(timeout = 4000)
    public void testNextAfterDouble() {
        assertTrue(Double.isNaN(FastMath.nextAfter(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.nextAfter(1.0, Double.NaN)));
        assertEquals(2.0, FastMath.nextAfter(2.0, 2.0), 0.0);

        assertEquals(Double.MIN_VALUE, FastMath.nextAfter(0.0,