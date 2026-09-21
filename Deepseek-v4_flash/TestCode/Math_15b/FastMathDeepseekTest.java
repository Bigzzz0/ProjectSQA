package org.apache.commons.math3.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for FastMath targeting the MATH-904 defect and maximizing coverage.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic (sin, cos, tan, asin, acos, atan, atan2, exp, log, pow, sqrt, cbrt, hyperbolic)
 * - Partition B: Boundary Value Analysis (NaN, Infinity, zero, negative zero, subnormal, large/small values)
 * - Partition C: Defect-targeted branch (MATH-904: pow(-1, odd integer >= 2^52) returns 1.0 instead of -1.0)
 * - Partition D: Exception/defensive paths (illegal arguments, out-of-range)
 * - Partition E: Object lifecycle (not applicable, static methods)
 *
 * Key branches targeted:
 * - pow: negative base with large integer exponent (y >= TWO_POWER_52) bypasses parity check
 * - sin/cos/tan: argument reduction (CodyWaite, PayneHanek), quadrant handling, sign of zero
 * - exp: overflow/underflow, subnormal output, negative input
 * - log: special cases (0, negative, NaN, infinity), subnormal normalization, polynomial expansion near 1
 * - asin/acos: domain boundaries, sign of zero
 * - atan2: zero, infinity, sign handling
 * - hyperbolic: large magnitude, overflow avoidance (MATH-905)
 */
public class FastMathDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testSinBasic() {
        assertEquals(0.0, FastMath.sin(0.0), 1e-15);
        assertEquals(1.0, FastMath.sin(Math.PI / 2), 1e-15);
        assertEquals(0.0, FastMath.sin(Math.PI), 1e-15);
        assertEquals(-1.0, FastMath.sin(3 * Math.PI / 2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testCosBasic() {
        assertEquals(1.0, FastMath.cos(0.0), 1e-15);
        assertEquals(0.0, FastMath.cos(Math.PI / 2), 1e-15);
        assertEquals(-1.0, FastMath.cos(Math.PI), 1e-15);
        assertEquals(0.0, FastMath.cos(3 * Math.PI / 2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testTanBasic() {
        assertEquals(0.0, FastMath.tan(0.0), 1e-15);
        assertEquals(1.0, FastMath.tan(Math.PI / 4), 1e-15);
        assertEquals(0.0, FastMath.tan(Math.PI), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAsinBasic() {
        assertEquals(0.0, FastMath.asin(0.0), 1e-15);
        assertEquals(Math.PI / 2, FastMath.asin(1.0), 1e-15);
        assertEquals(-Math.PI / 2, FastMath.asin(-1.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAcosBasic() {
        assertEquals(0.0, FastMath.acos(1.0), 1e-15);
        assertEquals(Math.PI / 2, FastMath.acos(0.0), 1e-15);
        assertEquals(Math.PI, FastMath.acos(-1.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAtanBasic() {
        assertEquals(0.0, FastMath.atan(0.0), 1e-15);
        assertEquals(Math.PI / 4, FastMath.atan(1.0), 1e-15);
        assertEquals(-Math.PI / 4, FastMath.atan(-1.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAtan2Basic() {
        assertEquals(0.0, FastMath.atan2(0.0, 1.0), 1e-15);
        assertEquals(Math.PI / 2, FastMath.atan2(1.0, 0.0), 1e-15);
        assertEquals(Math.PI, FastMath.atan2(0.0, -1.0), 1e-15);
        assertEquals(-Math.PI / 2, FastMath.atan2(-1.0, 0.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testExpBasic() {
        assertEquals(1.0, FastMath.exp(0.0), 1e-15);
        assertEquals(Math.E, FastMath.exp(1.0), 1e-15);
        assertEquals(1.0 / Math.E, FastMath.exp(-1.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testLogBasic() {
        assertEquals(0.0, FastMath.log(1.0), 1e-15);
        assertEquals(1.0, FastMath.log(Math.E), 1e-15);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testPowBasic() {
        assertEquals(1.0, FastMath.pow(2.0, 0.0), 1e-15);
        assertEquals(8.0, FastMath.pow(2.0, 3.0), 1e-15);
        assertEquals(0.25, FastMath.pow(2.0, -2.0), 1e-15);
        assertEquals(-8.0, FastMath.pow(-2.0, 3.0), 1e-15);
        assertEquals(8.0, FastMath.pow(-2.0, 4.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSqrtBasic() {
        assertEquals(0.0, FastMath.sqrt(0.0), 1e-15);
        assertEquals(2.0, FastMath.sqrt(4.0), 1e-15);
        assertEquals(Double.NaN, FastMath.sqrt(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testCbrtBasic() {
        assertEquals(0.0, FastMath.cbrt(0.0), 1e-15);
        assertEquals(3.0, FastMath.cbrt(27.0), 1e-15);
        assertEquals(-3.0, FastMath.cbrt(-27.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testHyperbolic() {
        assertEquals(0.0, FastMath.sinh(0.0), 1e-15);
        assertEquals(0.0, FastMath.cosh(0.0), 1e-15);
        assertEquals(0.0, FastMath.tanh(0.0), 1e-15);
        assertEquals(1.0, FastMath.tanh(100.0), 1e-15);
        assertEquals(-1.0, FastMath.tanh(-100.0), 1e-15);
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testSinBoundaries() {
        // Negative zero
        assertEquals(-0.0, FastMath.sin(-0.0), 0.0);
        // Infinity
        assertEquals(Double.NaN, FastMath.sin(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.NaN, FastMath.sin(Double.NEGATIVE_INFINITY), 0.0);
        // NaN
        assertEquals(Double.NaN, FastMath.sin(Double.NaN), 0.0);
        // Large argument requiring PayneHanek reduction
        double large = 1e20;
        double expected = Math.sin(large);
        assertEquals(expected, FastMath.sin(large), 1e-12);
    }

    @Test(timeout = 4000)
    public void testCosBoundaries() {
        assertEquals(Double.NaN, FastMath.cos(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.NaN, FastMath.cos(Double.NaN), 0.0);
        // Large argument
        double large = 1e20;
        double expected = Math.cos(large);
        assertEquals(expected, FastMath.cos(large), 1e-12);
    }

    @Test(timeout = 4000)
    public void testTanBoundaries() {
        assertEquals(-0.0, FastMath.tan(-0.0), 0.0);
        assertEquals(Double.NaN, FastMath.tan(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.NaN, FastMath.tan(Double.NaN), 0.0);
        // Near PI/2
        double nearPi2 = Math.PI / 2 - 1e-12;
        assertTrue(FastMath.tan(nearPi2) > 1e12);
    }

    @Test(timeout = 4000)
    public void testAsinBoundaries() {
        assertEquals(Double.NaN, FastMath.asin(1.1), 0.0);
        assertEquals(Double.NaN, FastMath.asin(-1.1), 0.0);
        assertEquals(Double.NaN, FastMath.asin(Double.NaN), 0.0);
        // Negative zero
        assertEquals(-0.0, FastMath.asin(-0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testAcosBoundaries() {
        assertEquals(Double.NaN, FastMath.acos(1.1), 0.0);
        assertEquals(Double.NaN, FastMath.acos(-1.1), 0.0);
        assertEquals(Double.NaN, FastMath.acos(Double.NaN), 0.0);
        assertEquals(Math.PI / 2, FastMath.acos(0.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAtanBoundaries() {
        assertEquals(-0.0, FastMath.atan(-0.0), 0.0);
        assertEquals(Math.PI / 2, FastMath.atan(Double.POSITIVE_INFINITY), 1e-15);
        assertEquals(-Math.PI / 2, FastMath.atan(Double.NEGATIVE_INFINITY), 1e-15);
        assertEquals(Double.NaN, FastMath.atan(Double.NaN), 0.0);
    }

    @Test(timeout = 4000)
    public void testAtan2Boundaries() {
        // Zero cases
        assertEquals(0.0, FastMath.atan2(0.0, 1.0), 1e-15);
        assertEquals(Math.PI, FastMath.atan2(0.0, -1.0), 1e-15);
        assertEquals(-0.0, FastMath.atan2(-0.0, 1.0), 0.0);
        assertEquals(-Math.PI, FastMath.atan2(-0.0, -1.0), 1e-15);
        // Infinity cases
        assertEquals(Math.PI / 4, FastMath.atan2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), 1e-15);
        assertEquals(3 * Math.PI / 4, FastMath.atan2(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), 1e-15);
        assertEquals(-Math.PI / 4, FastMath.atan2(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), 1e-15);
        assertEquals(-3 * Math.PI / 4, FastMath.atan2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), 1e-15);
        // NaN
        assertEquals(Double.NaN, FastMath.atan2(Double.NaN, 1.0), 0.0);
        assertEquals(Double.NaN, FastMath.atan2(1.0, Double.NaN), 0.0);
    }

    @Test(timeout = 4000)
    public void testExpBoundaries() {
        // Overflow
        assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(1000.0), 0.0);
        // Underflow
        assertEquals(0.0, FastMath.exp(-1000.0), 0.0);
        // Subnormal output (exp(-750) is subnormal)
        double sub = FastMath.exp(-750.0);
        assertTrue(sub > 0 && sub < Double.MIN_NORMAL);
        // NaN
        assertEquals(Double.NaN, FastMath.exp(Double.NaN), 0.0);
    }

    @Test(timeout = 4000)
    public void testLogBoundaries() {
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(-0.0), 0.0);
        assertEquals(Double.NaN, FastMath.log(-1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.NaN, FastMath.log(Double.NaN), 0.0);
        // Subnormal input
        double sub = Double.MIN_VALUE;
        double expected = Math.log(sub);
        assertEquals(expected, FastMath.log(sub), 1e-15);
    }

    @Test(timeout = 4000)
    public void testPowBoundaries() {
        // x = 0
        assertEquals(0.0, FastMath.pow(0.0, 1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -1.0), 0.0);
        assertEquals(Double.NaN, FastMath.pow(0.0, 0.0), 0.0);
        // x = -0.0
        assertEquals(-0.0, FastMath.pow(-0.0, 1.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.pow(-0.0, -1.0), 0.0);
        // x = Infinity
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.POSITIVE_INFINITY, 2.0), 0.0);
        assertEquals(0.0, FastMath.pow(Double.POSITIVE_INFINITY, -2.0), 0.0);
        // x = -Infinity
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 2.0), 0.0);
        assertEquals(0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -1.0), 0.0);
        // y = Infinity
        assertEquals(Double.NaN, FastMath.pow(-1.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.pow(0.5, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(2.0, Double.POSITIVE_INFINITY), 0.0);
        // y = -Infinity
        assertEquals(Double.NaN, FastMath.pow(-1.0, Double.NEGATIVE_INFINITY), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.5, Double.NEGATIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.pow(2.0, Double.NEGATIVE_INFINITY), 0.0);
        // NaN
        assertEquals(Double.NaN, FastMath.pow(Double.NaN, 1.0), 0.0);
        assertEquals(Double.NaN, FastMath.pow(1.0, Double.NaN), 0.0);
    }

    @Test(timeout = 4000)
    public void testCbrtBoundaries() {
        assertEquals(-0.0, FastMath.cbrt(-0.0), 0.0);
        assertEquals(Double.NaN, FastMath.cbrt(Double.NaN), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.cbrt(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.cbrt(Double.NEGATIVE_INFINITY), 0.0);
        // Subnormal
        double sub = Double.MIN_VALUE;
        double expected = Math.cbrt(sub);
        assertEquals(expected, FastMath.cbrt(sub), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSinhCoshTanhBoundaries() {
        // Overflow avoidance (MATH-905)
        double large = 800.0;
        assertFalse(Double.isInfinite(FastMath.sinh(large)));
        assertFalse(Double.isInfinite(FastMath.cosh(large)));
        // Negative large
        assertEquals(-FastMath.sinh(large), FastMath.sinh(-large), 1e-12);
        // tanh saturates
        assertEquals(1.0, FastMath.tanh(30.0), 1e-15);
        assertEquals(-1.0, FastMath.tanh(-30.0), 1e-15);
        // NaN
        assertEquals(Double.NaN, FastMath.sinh(Double.NaN), 0.0);
        assertEquals(Double.NaN, FastMath.cosh(Double.NaN), 0.0);
        assertEquals(Double.NaN, FastMath.tanh(Double.NaN), 0.0);
    }

    // ========== Partition C: Defect-Targeted Branch (MATH-904) ==========

    @Test(timeout = 4000)
    public void testMath904() {
        // pow(-1.0, odd integer >= 2^52) should return -1.0, but bug returns 1.0
        double oddLarge = 4503599627370497.0; // 2^52 + 1, exactly representable odd integer
        double result = FastMath.pow(-1.0, oddLarge);
        assertEquals("MATH-904: pow(-1, odd large integer) should be -1.0", -1.0, result, 0.0);
    }

    // Additional test for even large integer (should be 1.0)
    @Test(timeout = 4000)
    public void testPowEvenLarge() {
        double evenLarge = 4503599627370496.0; // 2^52, even
        assertEquals(1.0, FastMath.pow(-1.0, evenLarge), 0.0);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testLog1p() {
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log1p(-1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log1p(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.log1p(0.0), 1e-15);
        // Small value
        double small = 1e-10;
        assertEquals(small - small*small/2, FastMath.log1p(small), 1e-20);
    }

    @Test(timeout = 4000)
    public void testExpm1() {
        assertEquals(0.0, FastMath.expm1(0.0), 1e-15);
        assertEquals(Math.E - 1, FastMath.expm1(1.0), 1e-15);
        assertEquals(-0.6321205588285577, FastMath.expm1(-1.0), 1e-15);
        // Large negative
        assertEquals(-1.0, FastMath.expm1(-1000.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSignum() {
        assertEquals(1.0, FastMath.signum(5.0), 0.0);
        assertEquals(-1.0, FastMath.signum(-5.0), 0.0);
        assertEquals(0.0, FastMath.signum(0.0), 0.0);
        assertEquals(-0.0, FastMath.signum(-0.0), 0.0);
        assertEquals(Double.NaN, FastMath.signum(Double.NaN), 0.0);
    }

    @Test(timeout = 4000)
    public void testFloorCeilRint() {
        assertEquals(2.0, FastMath.floor(2.5), 0.0);
        assertEquals(-3.0, FastMath.floor(-2.5), 0.0);
        assertEquals(3.0, FastMath.ceil(2.5), 0.0);
        assertEquals(-2.0, FastMath.ceil(-2.5), 0.0);
        assertEquals(2.0, FastMath.rint(2.5), 0.0);
        assertEquals(3.0, FastMath.rint(3.5), 0.0);
        assertEquals(-2.0, FastMath.rint(-2.5), 0.0);
        // Preserve sign of zero
        assertEquals(-0.0, FastMath.ceil(-0.0), 0.0);
        assertEquals(-0.0, FastMath.floor(-0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testRound() {
        assertEquals(3L, FastMath.round(2.5));
        assertEquals(2L, FastMath.round(2.4));
        assertEquals(-2L, FastMath.round(-2.5));
        assertEquals(-3L, FastMath.round(-2.6));
    }

    @Test(timeout = 4000)
    public void testMinMax() {
        assertEquals(2.0, FastMath.min(2.0, 3.0), 0.0);
        assertEquals(3.0, FastMath.max(2.0, 3.0), 0.0);
        // NaN handling
        assertEquals(Double.NaN, FastMath.min(Double.NaN, 1.0), 0.0);
        assertEquals(Double.NaN, FastMath.max(Double.NaN, 1.0), 0.0);
        // Signed zero
        assertEquals(-0.0, FastMath.min(0.0, -0.0), 0.0);
        assertEquals(0.0, FastMath.max(0.0, -0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testAbs() {
        assertEquals(5, FastMath.abs(-5));
        assertEquals(5L, FastMath.abs(-5L));
        assertEquals(5.0f, FastMath.abs(-5.0f), 0.0f);
        assertEquals(5.0, FastMath.abs(-5.0), 0.0);
        // Negative zero
        assertEquals(0.0f, FastMath.abs(-0.0f), 0.0f);
        assertEquals(0.0, FastMath.abs(-0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testCopySign() {
        assertEquals(-1.0, FastMath.copySign(1.0, -2.0), 0.0);
        assertEquals(1.0, FastMath.copySign(-1.0, 2.0), 0.0);
        // NaN sign treated as positive
        assertEquals(1.0, FastMath.copySign(1.0, Double.NaN), 0.0);
    }

    @Test(timeout = 4000)
    public void testScalb() {
        assertEquals(8.0, FastMath.scalb(2.0, 2), 0.0);
        assertEquals(0.5, FastMath.scalb(2.0, -2), 0.0);
        // Subnormal result
        double sub = FastMath.scalb(1.0, -1074);
        assertTrue(sub > 0 && sub < Double.MIN_NORMAL);
        // Overflow
        assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(1.0, 1024), 0.0);
        // Underflow
        assertEquals(0.0, FastMath.scalb(1.0, -1075), 0.0);
    }

    @Test(timeout = 4000)
    public void testNextAfter() {
        assertEquals(2.0, FastMath.nextAfter(1.0, 3.0), 0.0);
        assertEquals(0.9999999999999999, FastMath.nextAfter(1.0, 0.0), 0.0);
        // Special cases
        assertEquals(Double.NaN, FastMath.nextAfter(Double.NaN, 1.0), 0.0);
        assertEquals(Double.MAX_VALUE, FastMath.nextAfter(Double.POSITIVE_INFINITY, 0.0), 0.0);
        assertEquals(Double.MIN_VALUE, FastMath.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, FastMath.nextAfter(0.0, -1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testHypot() {
        assertEquals(5.0, FastMath.hypot(3.0, 4.0), 1e-15);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals(Double.NaN, FastMath.hypot(Double.NaN, 1.0), 0.0);
        // Avoid overflow
        double large = 1e300;
        double expected = Math.hypot(large, large);
        assertEquals(expected, FastMath.hypot(large, large), 1e-12);
    }

    @Test(timeout = 4000)
    public void testToRadiansToDegrees() {
        assertEquals(0.0, FastMath.toRadians(0.0), 0.0);
        assertEquals(Math.PI, FastMath.toRadians(180.0), 1e-15);
        assertEquals(180.0, FastMath.toDegrees(Math.PI), 1e-15);
        // Preserve sign of zero
        assertEquals(-0.0, FastMath.toRadians(-0.0), 0.0);
        assertEquals(-0.0, FastMath.toDegrees(-0.0), 0.0);
        // Infinity
        assertEquals(Double.POSITIVE_INFINITY, FastMath.toRadians(Double.POSITIVE_INFINITY), 0.0);
    }

    @Test(timeout = 4000)
    public void testUlp() {
        assertEquals(Double.MIN_VALUE, FastMath.ulp(1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.ulp(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.ulp(Double.NEGATIVE_INFINITY), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetExponent() {
        assertEquals(0, FastMath.getExponent(1.0));
        assertEquals(3, FastMath.getExponent(8.0));
        assertEquals(-3, FastMath.getExponent(0.125));
        assertEquals(1024, FastMath.getExponent(Double.POSITIVE_INFINITY));
        assertEquals(1024, FastMath.getExponent(Double.NaN));
    }

    @Test(timeout = 4000)
    public void testIEEEremainder() {
        assertEquals(1.0, FastMath.IEEEremainder(5.0, 2.0), 1e-15);
        assertEquals(-1.0, FastMath.IEEEremainder(5.0, 3.0), 1e-15);
        assertEquals(Double.NaN, FastMath.IEEEremainder(Double.POSITIVE_INFINITY, 1.0), 0.0);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    // Not applicable for static utility class; no state to test.

    // Additional coverage for internal methods (indirectly tested via public methods)
    @Test(timeout = 4000)
    public void testPowIntExponent() {
        assertEquals(1.0, FastMath.pow(2.0, 0), 0.0);
        assertEquals(0.125, FastMath.pow(2.0, -3), 0.0);
        assertEquals(-8.0, FastMath.pow(-2.0, 3), 0.0);
        assertEquals(8.0, FastMath.pow(-2.0, 4), 0.0);
        // Large exponent
        assertEquals(1.0, FastMath.pow(1.0, 1000000), 0.0);
    }

    @Test(timeout = 4000)
    public void testLog10() {
        assertEquals(0.0, FastMath.log10(1.0), 1e-15);
        assertEquals(1.0, FastMath.log10(10.0), 1e-15);
        assertEquals(Double.NaN, FastMath.log10(-1.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log10(0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testLogBase() {
        assertEquals(1.0, FastMath.log(2.0, 2.0), 1e-15);
        assertEquals(2.0, FastMath.log(2.0, 4.0), 1e-15);
        assertEquals(Double.NaN, FastMath.log(-1.0, 2.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testAsinhAcoshAtanh() {
        assertEquals(0.0, FastMath.asinh(0.0), 1e-15);
        assertEquals(0.0, FastMath.acosh(1.0), 1e-15);
        assertEquals(0.0, FastMath.atanh(0.0), 1e-15);
        // Domain boundaries
        assertEquals(Double.NaN, FastMath.acosh(0.5), 0.0);
        assertEquals(Double.NaN, FastMath.atanh(1.1), 0.0);
        assertEquals(Double.NaN, FastMath.atanh(-1.1), 0.0);
    }
}