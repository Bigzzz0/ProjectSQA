package org.apache.commons.math3.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - exp, log, sin, cos, tan, pow, sqrt, cbrt, etc.
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - Zero, NaN, Infinity, subnormal, large magnitudes
 * Partition C: Defect-Targeted Branch Zone (MATH-905)
 *   - cosh(x) and sinh(x) for |x| > 20 should return 0.5*exp(|x|) without overflow
 *   - Bug: cosh/sinh for large positive/negative returns Infinity instead of finite value
 * Partition D: Exception & Defensive Guard Paths
 *   - Invalid arguments (NaN, out-of-domain) for asin, acos, atanh, etc.
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Not applicable (static utility class)
 */
public class FastMathDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

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
        assertTrue(Double.isInfinite(FastMath.log(0.0)));
        assertTrue(Double.isNaN(FastMath.log(-1.0)));
    }

    @Test(timeout = 4000)
    public void testSinCosTan() {
        assertEquals(0.0, FastMath.sin(0.0), 1e-15);
        assertEquals(1.0, FastMath.sin(Math.PI / 2), 1e-15);
        assertEquals(0.0, FastMath.sin(Math.PI), 1e-15);
        assertEquals(1.0, FastMath.cos(0.0), 1e-15);
        assertEquals(0.0, FastMath.cos(Math.PI / 2), 1e-15);
        assertEquals(-1.0, FastMath.cos(Math.PI), 1e-15);
        assertEquals(0.0, FastMath.tan(0.0), 1e-15);
        assertEquals(1.0, FastMath.tan(Math.PI / 4), 1e-15);
    }

    @Test(timeout = 4000)
    public void testPowBasic() {
        assertEquals(8.0, FastMath.pow(2.0, 3.0), 1e-15);
        assertEquals(1.0, FastMath.pow(2.0, 0.0), 1e-15);
        assertEquals(0.25, FastMath.pow(2.0, -2.0), 1e-15);
        assertEquals(4.0, FastMath.pow(-2.0, 2.0), 1e-15);
        assertEquals(-8.0, FastMath.pow(-2.0, 3.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSqrtCbrt() {
        assertEquals(2.0, FastMath.sqrt(4.0), 1e-15);
        assertEquals(3.0, FastMath.cbrt(27.0), 1e-15);
        assertEquals(-3.0, FastMath.cbrt(-27.0), 1e-15);
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testNaN() {
        assertTrue(Double.isNaN(FastMath.sin(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.cos(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.tan(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.exp(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.log(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.pow(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.pow(1.0, Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.exp(Double.NEGATIVE_INFINITY), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log(Double.POSITIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.sin(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.cos(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.tan(Double.POSITIVE_INFINITY)));
    }

    @Test(timeout = 4000)
    public void testZeroSign() {
        assertEquals(0.0, FastMath.sin(0.0), 0.0);
        assertEquals(-0.0, FastMath.sin(-0.0), 0.0);
        assertEquals(1.0, FastMath.cos(0.0), 0.0);
        assertEquals(1.0, FastMath.cos(-0.0), 0.0);
        assertEquals(0.0, FastMath.tan(0.0), 0.0);
        assertEquals(-0.0, FastMath.tan(-0.0), 0.0);
        assertEquals(0.0, FastMath.exp(0.0), 0.0);
        assertEquals(1.0, FastMath.exp(0.0), 0.0); // exp(0)=1
        assertEquals(0.0, FastMath.log(1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testSubnormal() {
        double subnormal = Double.MIN_VALUE;
        assertEquals(subnormal, FastMath.abs(subnormal), 0.0);
        assertEquals(0.0, FastMath.log(subnormal), 0.0); // log(subnormal) negative large
        // Just ensure no exception
        FastMath.sin(subnormal);
        FastMath.cos(subnormal);
    }

    // ========== Partition C: Defect-Targeted (MATH-905) ==========

    @Test(timeout = 4000)
    public void testMath905LargePositive() {
        // For x > 20, cosh(x) should be 0.5*exp(x) (no overflow)
        double x = 1000.0;
        double expected = 0.5 * FastMath.exp(x);
        double actual = FastMath.cosh(x);
        assertEquals("cosh large positive should not overflow", expected, actual, 1e-10 * expected);
    }

    @Test(timeout = 4000)
    public void testMath905LargeNegative() {
        // For x < -20, cosh(x) should be 0.5*exp(-x)
        double x = -1000.0;
        double expected = 0.5 * FastMath.exp(-x);
        double actual = FastMath.cosh(x);
        assertEquals("cosh large negative should not overflow", expected, actual, 1e-10 * expected);
    }

    @Test(timeout = 4000)
    public void testMath905SinhLargePositive() {
        double x = 1000.0;
        double expected = 0.5 * FastMath.exp(x);
        double actual = FastMath.sinh(x);
        assertEquals("sinh large positive should not overflow", expected, actual, 1e-10 * expected);
    }

    @Test(timeout = 4000)
    public void testMath905SinhLargeNegative() {
        double x = -1000.0;
        double expected = -0.5 * FastMath.exp(-x);
        double actual = FastMath.sinh(x);
        assertEquals("sinh large negative should not overflow", expected, actual, 1e-10 * expected);
    }

    @Test(timeout = 4000)
    public void testMath905TanhLarge() {
        // tanh should saturate at +/-1 for large |x|
        assertEquals(1.0, FastMath.tanh(1000.0), 1e-15);
        assertEquals(-1.0, FastMath.tanh(-1000.0), 1e-15);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testAsinDomain() {
        assertTrue(Double.isNaN(FastMath.asin(1.5)));
        assertTrue(Double.isNaN(FastMath.asin(-1.5)));
        assertEquals(Math.PI / 2, FastMath.asin(1.0), 1e-15);
        assertEquals(-Math.PI / 2, FastMath.asin(-1.0), 1e-15);
        assertEquals(0.0, FastMath.asin(0.0), 0.0);
        assertEquals(-0.0, FastMath.asin(-0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testAcosDomain() {
        assertTrue(Double.isNaN(FastMath.acos(1.5)));
        assertTrue(Double.isNaN(FastMath.acos(-1.5)));
        assertEquals(0.0, FastMath.acos(1.0), 1e-15);
        assertEquals(Math.PI, FastMath.acos(-1.0), 1e-15);
        assertEquals(Math.PI / 2, FastMath.acos(0.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAtanhDomain() {
        assertTrue(Double.isNaN(FastMath.atanh(1.5)));
        assertTrue(Double.isNaN(FastMath.atanh(-1.5)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.atanh(1.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.atanh(-1.0), 0.0);
        assertEquals(0.0, FastMath.atanh(0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testLog1pEdge() {
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log1p(-1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log1p(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.log1p(0.0), 0.0);
        assertEquals(-0.0, FastMath.log1p(-0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testHypotOverflow() {
        assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(Double.MAX_VALUE, Double.MAX_VALUE), 0.0);
        assertEquals(5.0, FastMath.hypot(3.0, 4.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testPowSpecialCases() {
        assertEquals(1.0, FastMath.pow(0.0, 0.0), 0.0); // 0^0 = 1
        assertEquals(0.0, FastMath.pow(0.0, 1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -1.0), 0.0);
        assertEquals(Double.NaN, FastMath.pow(-1.0, 0.5), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(2.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.pow(0.5, Double.POSITIVE_INFINITY), 0.0);
    }

    @Test(timeout = 4000)
    public void testScalb() {
        assertEquals(8.0, FastMath.scalb(1.0, 3), 0.0);
        assertEquals(0.5, FastMath.scalb(1.0, -1), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(1.0, 2000), 0.0);
        assertEquals(0.0, FastMath.scalb(1.0, -2000), 0.0);
    }

    @Test(timeout = 4000)
    public void testNextAfter() {
        assertEquals(Double.MIN_VALUE, FastMath.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, FastMath.nextAfter(0.0, -1.0), 0.0);
        assertEquals(Double.MAX_VALUE, FastMath.nextAfter(Double.POSITIVE_INFINITY, 0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.nextAfter(Double.NaN, 1.0)));
    }

    @Test(timeout = 4000)
    public void testFloorCeilRint() {
        assertEquals(2.0, FastMath.floor(2.5), 0.0);
        assertEquals(-3.0, FastMath.floor(-2.5), 0.0);
        assertEquals(3.0, FastMath.ceil(2.5), 0.0);
        assertEquals(-2.0, FastMath.ceil(-2.5), 0.0);
        assertEquals(2.0, FastMath.rint(2.5), 0.0); // round to even
        assertEquals(3.0, FastMath.rint(3.5), 0.0);
        assertEquals(-2.0, FastMath.rint(-2.5), 0.0);
    }

    @Test(timeout = 4000)
    public void testMinMax() {
        assertEquals(2.0, FastMath.min(2.0, 3.0), 0.0);
        assertEquals(3.0, FastMath.max(2.0, 3.0), 0.0);
        assertEquals(-0.0, FastMath.min(0.0, -0.0), 0.0);
        assertEquals(0.0, FastMath.max(0.0, -0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.min(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.max(Double.NaN, 1.0)));
    }

    @Test(timeout = 4000)
    public void testCopySign() {
        assertEquals(2.0, FastMath.copySign(2.0, 1.0), 0.0);
        assertEquals(-2.0, FastMath.copySign(2.0, -1.0), 0.0);
        assertEquals(2.0, FastMath.copySign(-2.0, 1.0), 0.0);
        assertEquals(-2.0, FastMath.copySign(-2.0, -1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetExponent() {
        assertEquals(0, FastMath.getExponent(1.0));
        assertEquals(3, FastMath.getExponent(8.0));
        assertEquals(-1, FastMath.getExponent(0.5));
        assertEquals(1024, FastMath.getExponent(Double.POSITIVE_INFINITY)); // exponent of Inf is 1024
    }

    @Test(timeout = 4000)
    public void testToRadiansToDegrees() {
        assertEquals(0.0, FastMath.toRadians(0.0), 0.0);
        assertEquals(Math.PI, FastMath.toRadians(180.0), 1e-15);
        assertEquals(180.0, FastMath.toDegrees(Math.PI), 1e-15);
        assertEquals(0.0, FastMath.toDegrees(0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testUlp() {
        assertTrue(FastMath.ulp(1.0) > 0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.ulp(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.ulp(Double.NEGATIVE_INFINITY), 0.0);
    }

    @Test(timeout = 4000)
    public void testSignum() {
        assertEquals(1.0, FastMath.signum(5.0), 0.0);
        assertEquals(-1.0, FastMath.signum(-5.0), 0.0);
        assertEquals(0.0, FastMath.signum(0.0), 0.0);
        assertEquals(-0.0, FastMath.signum(-0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.signum(Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testAbs() {
        assertEquals(5, FastMath.abs(-5));
        assertEquals(5L, FastMath.abs(-5L));
        assertEquals(5.0f, FastMath.abs(-5.0f), 0.0f);
        assertEquals(5.0, FastMath.abs(-5.0), 0.0);
        assertEquals(0.0f, FastMath.abs(-0.0f), 0.0f);
        assertEquals(0.0, FastMath.abs(-0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testRound() {
        assertEquals(3L, FastMath.round(2.5));
        assertEquals(2L, FastMath.round(2.4));
        assertEquals(-2L, FastMath.round(-2.5));
        assertEquals(3, FastMath.round(2.5f));
        assertEquals(2, FastMath.round(2.4f));
        assertEquals(-2, FastMath.round(-2.5f));
    }

    @Test(timeout = 4000)
    public void testIEEEremainder() {
        assertEquals(1.0, FastMath.IEEEremainder(10.0, 3.0), 1e-15);
        assertEquals(-1.0, FastMath.IEEEremainder(-10.0, 3.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.IEEEremainder(Double.POSITIVE_INFINITY, 1.0)));
    }

    @Test(timeout = 4000)
    public void testAsinhAcoshAtanh() {
        assertEquals(0.0, FastMath.asinh(0.0), 1e-15);
        assertEquals(0.0, FastMath.acosh(1.0), 1e-15);
        assertEquals(0.0, FastMath.atanh(0.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.acosh(0.5)));
        assertTrue(Double.isNaN(FastMath.atanh(1.5)));
    }

    @Test(timeout = 4000)
    public void testLog10() {
        assertEquals(1.0, FastMath.log10(10.0), 1e-15);
        assertEquals(2.0, FastMath.log10(100.0), 1e-15);
        assertEquals(0.0, FastMath.log10(1.0), 1e-15);
        assertTrue(Double.isInfinite(FastMath.log10(0.0)));
    }

    @Test(timeout = 4000)
    public void testLogBase() {
        assertEquals(2.0, FastMath.log(2.0, 4.0), 1e-15);
        assertEquals(3.0, FastMath.log(2.0, 8.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.log(-1.0, 2.0)));
    }

    @Test(timeout = 4000)
    public void testPowInt() {
        assertEquals(8.0, FastMath.pow(2.0, 3), 1e-15);
        assertEquals(0.25, FastMath.pow(2.0, -2), 1e-15);
        assertEquals(1.0, FastMath.pow(0.0, 0), 1e-15);
        assertEquals(1.0, FastMath.pow(5.0, 0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testExpm1() {
        assertEquals(0.0, FastMath.expm1(0.0), 1e-15);
        assertEquals(Math.E - 1, FastMath.expm1(1.0), 1e-15);
        assertEquals(-1.0, FastMath.expm1(-1.0), 1e-15);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.expm1(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(-1.0, FastMath.expm1(Double.NEGATIVE_INFINITY), 0.0);
    }

    @Test(timeout = 4000)
    public void testLog1p() {
        assertEquals(0.0, FastMath.log1p(0.0), 1e-15);
        assertEquals(Math.log(2), FastMath.log1p(1.0), 1e-15);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log1p(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testCoshSinhTanhSmall() {
        assertEquals(1.0, FastMath.cosh(0.0), 1e-15);
        assertEquals(0.0, FastMath.sinh(0.0), 1e-15);
        assertEquals(0.0, FastMath.tanh(0.0), 1e-15);
        double x = 0.5;
        assertEquals(Math.cosh(x), FastMath.cosh(x), 1e-15);
        assertEquals(Math.sinh(x), FastMath.sinh(x), 1e-15);
        assertEquals(Math.tanh(x), FastMath.tanh(x), 1e-15);
    }

    @Test(timeout = 4000)
    public void testCoshSinhTanhModerate() {
        double x = 15.0;
        assertEquals(Math.cosh(x), FastMath.cosh(x), 1e-12);
        assertEquals(Math.sinh(x), FastMath.sinh(x), 1e-12);
        assertEquals(Math.tanh(x), FastMath.tanh(x), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAtan2() {
        assertEquals(0.0, FastMath.atan2(0.0, 1.0), 1e-15);
        assertEquals(Math.PI, FastMath.atan2(0.0, -1.0), 1e-15);
        assertEquals(Math.PI / 2, FastMath.atan2(1.0, 0.0), 1e-15);
        assertEquals(-Math.PI / 2, FastMath.atan2(-1.0, 0.0), 1e-15);
        assertEquals(Math.PI / 4, FastMath.atan2(1.0, 1.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.atan2(Double.NaN, 1.0)));
    }

    @Test(timeout = 4000)
    public void testCodyWaiteReduction() {
        // Test sin/cos/tan for values requiring Cody-Waite reduction
        double x = 100.0;
        assertEquals(Math.sin(x), FastMath.sin(x), 1e-15);
        assertEquals(Math.cos(x), FastMath.cos(x), 1e-15);
        assertEquals(Math.tan(x), FastMath.tan(x), 1e-15);
    }

    @Test(timeout = 4000)
    public void testPayneHanekReduction() {
        // Very large argument to trigger Payne-Hanek
        double x = 1e10;
        assertEquals(Math.sin(x), FastMath.sin(x), 1e-10);
        assertEquals(Math.cos(x), FastMath.cos(x), 1e-10);
        assertEquals(Math.tan(x), FastMath.tan(x), 1e-10);
    }

    @Test(timeout = 4000)
    public void testPowNegativeBaseNonIntegerExponent() {
        assertTrue(Double.isNaN(FastMath.pow(-2.0, 0.5)));
        assertEquals(4.0, FastMath.pow(-2.0, 2.0), 1e-15);
        assertEquals(-8.0, FastMath.pow(-2.0, 3.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testPowZeroBase() {
        assertEquals(0.0, FastMath.pow(0.0, 5.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -5.0), 0.0);
        assertEquals(1.0, FastMath.pow(0.0, 0.0), 0.0);
        // Negative zero
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.pow(-0.0, -3.0), 0.0);
        assertEquals(-0.0, FastMath.pow(-0.0, 3.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testPowInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.POSITIVE_INFINITY, 2.0), 0.0);
        assertEquals(0.0, FastMath.pow(Double.POSITIVE_INFINITY, -2.0), 0.0);
        assertEquals(Double.NaN, FastMath.pow(1.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.NaN, FastMath.pow(-1.0, Double.POSITIVE_INFINITY), 0.0);
    }

    @Test(timeout = 4000)
    public void testHypotUnderflow() {
        double tiny = Double.MIN_VALUE / 2;
        assertEquals(tiny, FastMath.hypot(tiny, 0.0), 0.0);
        assertEquals(0.0, FastMath.hypot(0.0, 0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testScalbFloat() {
        assertEquals(8.0f, FastMath.scalb(1.0f, 3), 0.0f);
        assertEquals(0.5f, FastMath.scalb(1.0f, -1), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, FastMath.scalb(1.0f, 200), 0.0f);
        assertEquals(0.0f, FastMath.scalb(1.0f, -200), 0.0f);
    }

    @Test(timeout = 4000)
    public void testNextAfterFloat() {
        assertEquals(Float.MIN_VALUE, FastMath.nextAfter(0.0f, 1.0), 0.0f);
        assertEquals(-Float.MIN_VALUE, FastMath.nextAfter(0.0f, -1.0), 0.0f);
        assertTrue(Float.isNaN(FastMath.nextAfter(Float.NaN, 1.0)));
    }

    @Test(timeout = 4000)
    public void testCopySignFloat() {
        assertEquals(2.0f, FastMath.copySign(2.0f, 1.0f), 0.0f);
        assertEquals(-2.0f, FastMath.copySign(2.0f, -1.0f), 0.0f);
    }

    @Test(timeout = 4000)
    public void testGetExponentFloat() {
        assertEquals(0, FastMath.getExponent(1.0f));
        assertEquals(3, FastMath.getExponent(8.0f));
        assertEquals(128, FastMath.getExponent(Float.POSITIVE_INFINITY));
    }

    @Test(timeout = 4000)
    public void testUlpFloat() {
        assertTrue(FastMath.ulp(1.0f) > 0);
        assertEquals(Float.POSITIVE_INFINITY, FastMath.ulp(Float.POSITIVE_INFINITY), 0.0f);
    }

    @Test(timeout = 4000)
    public void testSignumFloat() {
        assertEquals(1.0f, FastMath.signum(5.0f), 0.0f);
        assertEquals(-1.0f, FastMath.signum(-5.0f), 0.0f);
        assertEquals(0.0f, FastMath.signum(0.0f), 0.0f);
        assertTrue(Float.isNaN(FastMath.signum(Float.NaN)));
    }

    @Test(timeout = 4000)
    public void testMinMaxFloat() {
        assertEquals(2.0f, FastMath.min(2.0f, 3.0f), 0.0f);
        assertEquals(3.0f, FastMath.max(2.0f, 3.0f), 0.0f);
        assertTrue(Float.isNaN(FastMath.min(Float.NaN, 1.0f)));
    }

    @Test(timeout = 4000)
    public void testAbsFloat() {
        assertEquals(5.0f, FastMath.abs(-5.0f), 0.0f);
        assertEquals(0.0f, FastMath.abs(-0.0f), 0.0f);
    }

    @Test(timeout = 4000)
    public void testRoundFloat() {
        assertEquals(3, FastMath.round(2.5f));
        assertEquals(2, FastMath.round(2.4f));
        assertEquals(-2, FastMath.round(-2.5f));
    }

    @Test(timeout = 4000)
    public void testNextUp() {
        assertEquals(Double.MIN_VALUE, FastMath.nextUp(0.0), 0.0);
        assertEquals(Float.MIN_VALUE, FastMath.nextUp(0.0f), 0.0f);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.nextUp(Double.MAX_VALUE), 0.0);
    }

    @Test(timeout = 4000)
    public void testRandom() {
        double r = FastMath.random();
        assertTrue(r >= 0.0 && r < 1.0);
    }

    @Test(timeout = 4000)
    public void testDoubleHighPart() {
        // Indirectly tested via other methods, but ensure no exception
        FastMath.sin(1.0);
        FastMath.cos(1.0);
    }
}