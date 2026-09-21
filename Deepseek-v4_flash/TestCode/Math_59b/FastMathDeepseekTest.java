package org.apache.commons.math.util;

import static org.junit.Assert.*;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Defect: In FastMath.max(float, float), the else branch returns b instead of a.
 *   - For input (50.0f, -50.0f): a <= b is false, so else branch executed.
 *     a+b = 0.0f (not NaN), returns b = -50.0f. Correct should be a = 50.0f.
 *   - Tests will expose this by asserting the correct positive value.
 * 
 * Target branches (public methods, high-level decision branches):
 *   - max/min: NaN handling, equality, positive/negative, zero.
 *   - exp: negative vs positive, overflow, subnormal, large arguments.
 *   - log: domain errors (negative, zero, NaN, infinity), subnormal, normal.
 *   - pow: special cases (zero, infinity, NaN, negative base, integer exponent).
 *   - sin/cos/tan: quadrant reduction, large arguments (Payne-Hanek), zero, NaN.
 *   - asin/acos/atan/atan2: domain limits, NaN, infinity.
 *   - hyperbolic: large magnitudes, special values.
 *   - sqrt: NaN, negative, zero.
 *   - cbrt: subnormal, normal, zero, NaN, infinity.
 *   - rounding: floor, ceil, rint, round (edge cases at half-integers).
 *   - nextAfter: zero, max/min, NaN.
 *   - signum: NaN, positive, negative, zero.
 *   - ulp: normal, subnormal.
 *   - toRadians/toDegrees: zero, large, negative.
 *   - abs: all types.
 */

public class FastMathDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testExp() {
        assertEquals(1.0, FastMath.exp(0.0), 0.0);
        assertEquals(Math.E, FastMath.exp(1.0), 1e-15);
        assertEquals(1.0 / Math.E, FastMath.exp(-1.0), 1e-15);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(710.0), 0.0);
        assertEquals(0.0, FastMath.exp(-750.0), 0.0);
        assertTrue(Double.isNaN(FastMath.exp(Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testLog() {
        assertEquals(0.0, FastMath.log(1.0), 0.0);
        assertEquals(1.0, FastMath.log(Math.E), 1e-15);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.log(-1.0)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log(Double.POSITIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.log(Double.NaN)));
        // Subnormal
        double subnormal = Double.MIN_NORMAL / 2;
        double expected = Math.log(subnormal);
        assertEquals(expected, FastMath.log(subnormal), 1e-15);
    }

    @Test(timeout = 4000)
    public void testPow() {
        assertEquals(1.0, FastMath.pow(2.0, 0.0), 0.0);
        assertEquals(8.0, FastMath.pow(2.0, 3.0), 0.0);
        assertEquals(0.25, FastMath.pow(2.0, -2.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -1.0), 0.0);
        assertEquals(0.0, FastMath.pow(0.0, 1.0), 0.0);
        assertTrue(Double.isNaN(FastMath.pow(0.0, 0.0))); // 0^0 is undefined
        assertTrue(Double.isNaN(FastMath.pow(-2.0, 0.5))); // negative base non-integer
        assertEquals(4.0, FastMath.pow(-2.0, 2.0), 0.0); // integer exponent
        assertEquals(-8.0, FastMath.pow(-2.0, 3.0), 0.0); // odd integer
        assertEquals(0.0, FastMath.pow(0.5, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(2.0, Double.POSITIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.pow(1.0, Double.POSITIVE_INFINITY))); // 1^inf
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.5, Double.NEGATIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.pow(2.0, Double.NEGATIVE_INFINITY), 0.0);
    }

    @Test(timeout = 4000)
    public void testSinCosTan() {
        assertEquals(0.0, FastMath.sin(0.0), 0.0);
        assertEquals(1.0, FastMath.sin(Math.PI / 2), 1e-15);
        assertEquals(0.0, FastMath.sin(Math.PI), 1e-15);
        assertEquals(-1.0, FastMath.sin(3 * Math.PI / 2), 1e-15);
        assertTrue(Double.isNaN(FastMath.sin(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.sin(Double.NaN)));

        assertEquals(1.0, FastMath.cos(0.0), 0.0);
        assertEquals(0.0, FastMath.cos(Math.PI / 2), 1e-15);
        assertEquals(-1.0, FastMath.cos(Math.PI), 1e-15);
        assertTrue(Double.isNaN(FastMath.cos(Double.NaN)));

        assertEquals(0.0, FastMath.tan(0.0), 0.0);
        assertEquals(1.0, FastMath.tan(Math.PI / 4), 1e-15);
        assertTrue(Double.isNaN(FastMath.tan(Math.PI / 2))); // near pole
        assertTrue(Double.isNaN(FastMath.tan(Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testAsinAcosAtan() {
        assertEquals(0.0, FastMath.asin(0.0), 0.0);
        assertEquals(Math.PI / 2, FastMath.asin(1.0), 1e-15);
        assertEquals(-Math.PI / 2, FastMath.asin(-1.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.asin(2.0)));
        assertTrue(Double.isNaN(FastMath.asin(Double.NaN)));

        assertEquals(0.0, FastMath.acos(1.0), 0.0);
        assertEquals(Math.PI, FastMath.acos(-1.0), 1e-15);
        assertEquals(Math.PI / 2, FastMath.acos(0.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.acos(2.0)));

        assertEquals(0.0, FastMath.atan(0.0), 0.0);
        assertEquals(Math.PI / 4, FastMath.atan(1.0), 1e-15);
        assertEquals(-Math.PI / 4, FastMath.atan(-1.0), 1e-15);
        assertEquals(Math.PI / 2, FastMath.atan(Double.POSITIVE_INFINITY), 1e-15);
        assertEquals(-Math.PI / 2, FastMath.atan(Double.NEGATIVE_INFINITY), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSqrtCbrt() {
        assertEquals(2.0, FastMath.sqrt(4.0), 0.0);
        assertEquals(0.0, FastMath.sqrt(0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.sqrt(-1.0)));
        assertTrue(Double.isNaN(FastMath.sqrt(Double.NaN)));

        assertEquals(3.0, FastMath.cbrt(27.0), 0.0);
        assertEquals(-2.0, FastMath.cbrt(-8.0), 1e-15);
        assertEquals(0.0, FastMath.cbrt(0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.cbrt(Double.NaN)));
        // Subnormal input
        double sub = Double.MIN_VALUE;
        double expected = Math.cbrt(sub);
        assertEquals(expected, FastMath.cbrt(sub), 1e-15);
    }

    @Test(timeout = 4000)
    public void testHyperbolic() {
        assertEquals(1.0, FastMath.cosh(0.0), 0.0);
        assertEquals((Math.E + 1.0 / Math.E) / 2, FastMath.cosh(1.0), 1e-15);
        assertEquals(0.0, FastMath.sinh(0.0), 0.0);
        assertEquals((Math.E - 1.0 / Math.E) / 2, FastMath.sinh(1.0), 1e-15);
        assertEquals(0.0, FastMath.tanh(0.0), 0.0);
        assertEquals((Math.E * Math.E - 1) / (Math.E * Math.E + 1), FastMath.tanh(1.0), 1e-15);
        assertEquals(1.0, FastMath.tanh(20.0), 1e-15);
        assertEquals(-1.0, FastMath.tanh(-20.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.sinh(Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testAbs() {
        assertEquals(5, FastMath.abs(-5));
        assertEquals(5L, FastMath.abs(-5L));
        assertEquals(5.0f, FastMath.abs(-5.0f), 0.0f);
        assertEquals(5.0, FastMath.abs(-5.0), 0.0);
        assertEquals(0.0, FastMath.abs(0.0), 0.0);
        assertEquals(Double.NaN, FastMath.abs(Double.NaN), 0.0);
    }

    // ========== Partition B: Boundary Values & Special Cases ==========

    @Test(timeout = 4000)
    public void testSignum() {
        assertEquals(-1.0, FastMath.signum(-5.0), 0.0);
        assertEquals(1.0, FastMath.signum(5.0), 0.0);
        assertEquals(0.0, FastMath.signum(0.0), 0.0);
        assertEquals(-0.0, FastMath.signum(-0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.signum(Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testNextAfter() {
        assertEquals(0.0, FastMath.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, FastMath.nextAfter(0.0, -1.0), 0.0);
        assertEquals(Double.MAX_VALUE, FastMath.nextAfter(Double.MAX_VALUE, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.nextAfter(Double.MAX_VALUE, Double.POSITIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.nextAfter(Double.NaN, 1.0)));
    }

    @Test(timeout = 4000)
    public void testFloorCeilRintRound() {
        assertEquals(3.0, FastMath.floor(3.7), 0.0);
        assertEquals(-4.0, FastMath.floor(-3.7), 0.0);
        assertEquals(4.0, FastMath.ceil(3.7), 0.0);
        assertEquals(-3.0, FastMath.ceil(-3.7), 0.0);
        assertEquals(4.0, FastMath.rint(3.5), 0.0);
        assertEquals(2.0, FastMath.rint(2.5), 0.0); // round to even
        assertEquals(3L, FastMath.round(3.4));
        assertEquals(4L, FastMath.round(3.6));
        assertEquals(-3L, FastMath.round(-3.4));
        assertEquals(-4L, FastMath.round(-3.6));
        assertTrue(Double.isNaN(FastMath.floor(Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testToRadiansToDegrees() {
        assertEquals(0.0, FastMath.toRadians(0.0), 0.0);
        assertEquals(Math.PI / 2, FastMath.toRadians(90.0), 1e-15);
        assertEquals(90.0, FastMath.toDegrees(Math.PI / 2), 1e-15);
        assertEquals(180.0, FastMath.toDegrees(Math.PI), 1e-15);
    }

    @Test(timeout = 4000)
    public void testUlip() {
        assertEquals(Double.MIN_VALUE, FastMath.ulp(0.0), 0.0);
        assertTrue(FastMath.ulp(1.0) > 0.0);
        assertTrue(FastMath.ulp(Double.MAX_VALUE) > 0.0);
        assertTrue(Double.isNaN(FastMath.ulp(Double.NaN)));
    }

    // ========== Partition C: Defect-Targeted max(float) ==========

    @Test(timeout = 4000)
    public void testMaxFloat() {
        // Directly exposes the defect: max(50.0f, -50.0f) should be 50.0f
        assertEquals("max(50.0f, -50.0f)", 50.0f, FastMath.max(50.0f, -50.0f), 0.0f);
        // Additional edge cases
        assertEquals(50.0f, FastMath.max(-50.0f, 50.0f), 0.0f);
        assertEquals(0.0f, FastMath.max(0.0f, 0.0f), 0.0f);
        assertEquals(Float.NaN, FastMath.max(Float.NaN, 1.0f), 0.0f);
        assertEquals(Float.NaN, FastMath.max(1.0f, Float.NaN), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, FastMath.max(Float.POSITIVE_INFINITY, 1.0f), 0.0f);
        assertEquals(1.0f, FastMath.max(Float.NEGATIVE_INFINITY, 1.0f), 0.0f);
        assertEquals(0.0f, FastMath.max(-0.0f, 0.0f), 0.0f);
    }

    @Test(timeout = 4000)
    public void testMinFloat() {
        // min is correct but test for completeness
        assertEquals(-50.0f, FastMath.min(50.0f, -50.0f), 0.0f);
        assertEquals(-50.0f, FastMath.min(-50.0f, 50.0f), 0.0f);
        assertEquals(0.0f, FastMath.min(0.0f, 0.0f), 0.0f);
        assertEquals(Float.NaN, FastMath.min(Float.NaN, 1.0f), 0.0f);
        assertEquals(Float.NEGATIVE_INFINITY, FastMath.min(Float.NEGATIVE_INFINITY, 1.0f), 0.0f);
    }

    @Test(timeout = 4000)
    public void testMaxDouble() {
        // Ensure double version is correct
        assertEquals(50.0, FastMath.max(50.0, -50.0), 0.0);
        assertEquals(-50.0, FastMath.max(-50.0, -50.0), 0.0);
        assertEquals(Double.NaN, FastMath.max(Double.NaN, 1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.max(Double.POSITIVE_INFINITY, 1.0), 0.0);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testLogDomainErrors() {
        assertTrue(Double.isNaN(FastMath.log(-0.5)));
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log(Double.POSITIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.log(Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testPowSpecialCases() {
        assertEquals(1.0, FastMath.pow(0.0, 0.0), 0.0); // note: Math.pow(0,0)=1.0, but FastMath returns NaN? Actually code returns 1.0 if y==0.0 first. So 0.0^0.0 = 1.0. 
        // But we follow the spec: if y==0 return 1.0
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -1.0), 0.0);
        assertEquals(0.0, FastMath.pow(0.0, 1.0), 0.0);
        assertTrue(Double.isNaN(FastMath.pow(-2.0, 0.5)));
        assertEquals(4.0, FastMath.pow(-2.0, 2.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testAtan2() {
        assertEquals(0.0, FastMath.atan2(0.0, 1.0), 0.0);
        assertEquals(Math.PI, FastMath.atan2(0.0, -1.0), 1e-15);
        assertEquals(Math.PI / 2, FastMath.atan2(1.0, 0.0), 1e-15);
        assertEquals(-Math.PI / 4, FastMath.atan2(-1.0, 1.0), 1e-15);
        assertEquals(3 * Math.PI / 4, FastMath.atan2(1.0, -1.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.atan2(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.atan2(1.0, Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testAsinDomain() {
        assertTrue(Double.isNaN(FastMath.asin(1.5)));
        assertTrue(Double.isNaN(FastMath.asin(-1.5)));
        assertEquals(Math.PI / 2, FastMath.asin(1.0), 1e-15);
        assertEquals(-Math.PI / 2, FastMath.asin(-1.0), 1e-15);
    }

    // ========== Additional coverage for previously untested methods ==========

    @Test(timeout = 4000)
    public void testLog1p() {
        assertEquals(0.0, FastMath.log1p(0.0), 0.0);
        assertEquals(Math.log(2.0), FastMath.log1p(1.0), 1e-15);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log1p(-1.0), 0.0);
        assertTrue(Double.isNaN(FastMath.log1p(-2.0)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log1p(Double.POSITIVE_INFINITY), 0.0);
    }

    @Test(timeout = 4000)
    public void testExpm1() {
        assertEquals(0.0, FastMath.expm1(0.0), 0.0);
        assertEquals(Math.E - 1.0, FastMath.expm1(1.0), 1e-15);
        assertEquals(1.0 / Math.E - 1.0, FastMath.expm1(-1.0), 1e-15);
        assertEquals(-1.0, FastMath.expm1(Double.NEGATIVE_INFINITY), 1e-15);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.expm1(Double.POSITIVE_INFINITY), 0.0);
    }

    @Test(timeout = 4000)
    public void testLog10() {
        assertEquals(0.0, FastMath.log10(1.0), 0.0);
        assertEquals(1.0, FastMath.log10(10.0), 1e-15);
        assertEquals(2.0, FastMath.log10(100.0), 1e-15);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log10(0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.log10(-1.0)));
    }

    @Test(timeout = 4000)
    public void testAcoshAsinhAtanh() {
        assertEquals(0.0, FastMath.acosh(1.0), 0.0);
        assertTrue(Double.isNaN(FastMath.acosh(0.5)));
        assertEquals(0.0, FastMath.asinh(0.0), 0.0);
        assertEquals(0.881373587019543, FastMath.asinh(1.0), 1e-15);
        assertEquals(0.0, FastMath.atanh(0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.atanh(1.0), 1e-15);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.atanh(-1.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.atanh(2.0)));
    }

    @Test(timeout = 4000)
    public void testRandom() {
        double r = FastMath.random();
        assertTrue(r >= 0.0 && r < 1.0);
    }

    @Test(timeout = 4000)
    public void testNextUp() {
        assertEquals(0.0, FastMath.nextUp(0.0), 0.0);
        assertEquals(Double.MIN_VALUE, FastMath.nextUp(0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.nextUp(Double.NaN)));
    }
}