package org.apache.commons.math.util;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. DEFECT UNDER TEST:
 *    - Defects4J Math Defect (FastMath.max(float, float)):
 *      Line in FastMath: `return (a <= b) ? b : (Float.isNaN(a + b) ? Float.NaN : b);`
 *      When `a > b` and neither is NaN, it erroneously returns `b` instead of `a`!
 *      Targeted by: testMaxFloatDefect() where max(50.0f, -50.0f) must return 50.0f, not -50.0f.
 *
 * 2. BRANCH & EQUIVALENCE PARTITION COVERAGE:
 *    - Partition A (Core Math & Arithmetic Helpers):
 *      - min/max across int, long, float, double (including NaN propagation, -0.0 vs +0.0).
 *      - abs across int (boundary Integer.MIN_VALUE), long (Long.MIN_VALUE), float, double.
 *      - signum, toRadians, toDegrees, random.
 *    - Partition B (Floating Point Manipulation & Boundaries):
 *      - floor, ceil, rint, round: NaN, infinite, extreme magnitude (>= 2^52), negative fractional,
 *        rint half-way even/odd rounding.
 *      - ulp, nextAfter, nextUp: zero (+/-), NaN, Infinite, mantissa overflow/underflow,
 *        direction >= d vs direction < d.
 *    - Partition C (Roots & Powers):
 *      - sqrt, cbrt (zero, subnormal, infinity, NaN, positive, negative).
 *      - pow(): y == 0, x is NaN, x == 0 (+/-0, y<0, y>0, odd/even int), x == +Inf, y == +Inf (|x| <=> 1),
 *        x == -Inf, y == -Inf, x < 0 (y >= 2^52, y even/odd integer, y non-integer -> NaN),
 *        large/small y (|y| >= 8e298).
 *    - Partition D (Exponentials & Logarithms):
 *      - exp: large negative (intVal > 746 -> 0, intVal > 709, intVal == 709), large positive (> 709 -> Inf).
 *      - expm1: NaN, zero, |x| >= 1.0 (positive/negative), -1.0 < x < 1.0 (positive/negative).
 *      - log: negative/NaN, +Infinity, zero (-Infinity), subnormal normalization loop,
 *        quick expansion range (0.99 < x < 1.01).
 *      - log1p: x == -1 (-Inf), x == +Inf, |x| >= 1e-6, |x| < 1e-6.
 *      - log10: normal, subnormal, infinite, NaN.
 *    - Partition E (Trigonometric & Hyperbolic):
 *      - sin, cos, tan: zero (+/-), NaN, +Inf, Payne-Hanek reduction (> 3294198.0), Cody-Waite (> pi/2),
 *        quadrants 0..3, tan near pi/2 (|xa| > 1.5).
 *      - asin, acos: NaN, |x| > 1.0, x == +/-1.0, x == 0.0.
 *      - atan, atan2: extreme inputs (> 1.633e16), atan2 edge cases (y=0, x=0, +/-Inf, quadrants 1..4).
 *      - cosh, sinh, tanh: NaN, |x| > 20, zero, positive, negative, threshold branches (sinh 0.25, tanh 0.5).
 *      - acosh, asinh, atanh: ranges across polynomial transitions and boundaries.
 */

import org.junit.Test;
import static org.junit.Assert.*;

public class FastMathGeminiTest {

    private static final double EPSILON = 1e-12;

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Direct replication of the defect: FastMath.max(50.0f, -50.0f)
     * Buggy implementation returns -50.0f instead of 50.0f due to returning 'b' on the false branch.
     */
    @Test(timeout = 4000)
    public void testMaxFloatDefect() {
        float result = FastMath.max(50.0f, -50.0f);
        assertEquals("max(50.0f, -50.0f) must return 50.0f", 50.0f, result, 0.0f);

        float result2 = FastMath.max(10.5f, 5.2f);
        assertEquals("max(10.5f, 5.2f) must return 10.5f", 10.5f, result2, 0.0f);

        float result3 = FastMath.max(-2.0f, -8.0f);
        assertEquals("max(-2.0f, -8.0f) must return -2.0f", -2.0f, result3, 0.0f);
    }

    // =========================================================================
    // PARTITION A: BASIC ARITHMETIC & MIN/MAX/ABS
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinMaxInt() {
        assertEquals(3, FastMath.min(3, 7));
        assertEquals(3, FastMath.min(7, 3));
        assertEquals(-5, FastMath.min(-5, -2));
        assertEquals(-5, FastMath.min(-2, -5));
        assertEquals(Integer.MIN_VALUE, FastMath.min(Integer.MIN_VALUE, Integer.MAX_VALUE));

        assertEquals(7, FastMath.max(3, 7));
        assertEquals(7, FastMath.max(7, 3));
        assertEquals(-2, FastMath.max(-5, -2));
        assertEquals(-2, FastMath.max(-2, -5));
        assertEquals(Integer.MAX_VALUE, FastMath.max(Integer.MIN_VALUE, Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testMinMaxLong() {
        assertEquals(3L, FastMath.min(3L, 7L));
        assertEquals(3L, FastMath.min(7L, 3L));
        assertEquals(-5L, FastMath.min(-5L, -2L));
        assertEquals(Long.MIN_VALUE, FastMath.min(Long.MIN_VALUE, Long.MAX_VALUE));

        assertEquals(7L, FastMath.max(3L, 7L));
        assertEquals(7L, FastMath.max(7L, 3L));
        assertEquals(-2L, FastMath.max(-5L, -2L));
        assertEquals(Long.MAX_VALUE, FastMath.max(Long.MIN_VALUE, Long.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testMinMaxFloat() {
        assertEquals(2.0f, FastMath.min(2.0f, 5.0f), 0.0f);
        assertEquals(2.0f, FastMath.min(5.0f, 2.0f), 0.0f);
        assertEquals(5.0f, FastMath.max(2.0f, 5.0f), 0.0f);

        // NaN handling
        assertTrue(Float.isNaN(FastMath.min(Float.NaN, 2.0f)));
        assertTrue(Float.isNaN(FastMath.min(2.0f, Float.NaN)));
        assertTrue(Float.isNaN(FastMath.max(Float.NaN, 2.0f)));
        assertTrue(Float.isNaN(FastMath.max(2.0f, Float.NaN)));
    }

    @Test(timeout = 4000)
    public void testMinMaxDouble() {
        assertEquals(2.0, FastMath.min(2.0, 5.0), 0.0);
        assertEquals(2.0, FastMath.min(5.0, 2.0), 0.0);
        assertEquals(5.0, FastMath.max(2.0, 5.0), 0.0);
        assertEquals(5.0, FastMath.max(5.0, 2.0), 0.0);

        // NaN handling
        assertTrue(Double.isNaN(FastMath.min(Double.NaN, 2.0)));
        assertTrue(Double.isNaN(FastMath.min(2.0, Double.NaN)));
        assertTrue(Double.isNaN(FastMath.max(Double.NaN, 2.0)));
        assertTrue(Double.isNaN(FastMath.max(2.0, Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testAbs() {
        assertEquals(10, FastMath.abs(10));
        assertEquals(10, FastMath.abs(-10));
        assertEquals(Integer.MIN_VALUE, FastMath.abs(Integer.MIN_VALUE));

        assertEquals(10L, FastMath.abs(10L));
        assertEquals(10L, FastMath.abs(-10L));
        assertEquals(Long.MIN_VALUE, FastMath.abs(Long.MIN_VALUE));

        assertEquals(10.5f, FastMath.abs(10.5f), 0.0f);
        assertEquals(10.5f, FastMath.abs(-10.5f), 0.0f);

        assertEquals(10.5, FastMath.abs(10.5), 0.0);
        assertEquals(10.5, FastMath.abs(-10.5), 0.0);
    }

    @Test(timeout = 4000)
    public void testSignum() {
        assertEquals(1.0, FastMath.signum(123.45), 0.0);
        assertEquals(-1.0, FastMath.signum(-123.45), 0.0);
        assertEquals(0.0, FastMath.signum(0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.signum(Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testAngleConversionsAndRandom() {
        assertEquals(Math.PI, FastMath.toRadians(180.0), EPSILON);
        assertEquals(180.0, FastMath.toDegrees(Math.PI), EPSILON);
        double r = FastMath.random();
        assertTrue(r >= 0.0 && r < 1.0);
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (FLOOR, CEIL, RINT, ROUND, ULP, NEXTAFTER)
    // =========================================================================

    @Test(timeout = 4000)
    public void testFloorCeilRint() {
        assertTrue(Double.isNaN(FastMath.floor(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.ceil(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.rint(Double.NaN)));

        // Large magnitude boundary >= 4503599627370496.0 (2^52)
        double large = 4503599627370496.0 * 2.0;
        assertEquals(large, FastMath.floor(large), 0.0);
        assertEquals(-large, FastMath.floor(-large), 0.0);
        assertEquals(large, FastMath.ceil(large), 0.0);

        // Standard values
        assertEquals(3.0, FastMath.floor(3.7), 0.0);
        assertEquals(-4.0, FastMath.floor(-3.2), 0.0);
        assertEquals(0.0, FastMath.floor(0.5), 0.0);

        assertEquals(4.0, FastMath.ceil(3.2), 0.0);
        assertEquals(-3.0, FastMath.ceil(-3.7), 0.0);
        assertEquals(3.0, FastMath.ceil(3.0), 0.0);
        assertEquals(-0.0, FastMath.ceil(-0.5), 0.0);

        // rint round to nearest and half-way round to even
        assertEquals(4.0, FastMath.rint(3.7), 0.0);
        assertEquals(3.0, FastMath.rint(3.2), 0.0);
        assertEquals(4.0, FastMath.rint(3.5), 0.0); // 3.5 -> 4.0 (even)
        assertEquals(4.0, FastMath.rint(4.5), 0.0); // 4.5 -> 4.0 (even)
        assertEquals(-4.0, FastMath.rint(-3.5), 0.0);
        assertEquals(-4.0, FastMath.rint(-4.5), 0.0);
    }

    @Test(timeout = 4000)
    public void testRound() {
        assertEquals(4L, FastMath.round(3.5));
        assertEquals(-3L, FastMath.round(-3.5));
        assertEquals(0L, FastMath.round(0.0));

        assertEquals(4, FastMath.round(3.5f));
        assertEquals(-3, FastMath.round(-3.5f));
    }

    @Test(timeout = 4000)
    public void testUlp() {
        assertEquals(Math.ulp(1.0), FastMath.ulp(1.0), 0.0);
        assertEquals(Math.ulp(0.0), FastMath.ulp(0.0), 0.0);
        assertEquals(Math.ulp(-123.456), FastMath.ulp(-123.456), 0.0);
    }

    @Test(timeout = 4000)
    public void testNextAfterAndNextUp() {
        assertTrue(Double.isNaN(FastMath.nextAfter(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.nextAfter(1.0, Double.NaN)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.nextAfter(Double.POSITIVE_INFINITY, 0.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.nextAfter(Double.NEGATIVE_INFINITY, 0.0), 0.0);

        assertEquals(Double.MIN_VALUE, FastMath.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, FastMath.nextAfter(0.0, -1.0), 0.0);

        // Direction >= d
        double up = FastMath.nextAfter(1.0, 2.0);
        assertTrue(up > 1.0);
        assertEquals(up, FastMath.nextUp(1.0), 0.0);

        // Direction < d
        double down = FastMath.nextAfter(1.0, 0.0);
        assertTrue(down < 1.0);

        // Mantissa boundary transitions (mantissa all 1s and mantissa 0)
        double maxBeforePow2 = FastMath.nextAfter(2.0, 1.0);
        assertEquals(2.0, FastMath.nextAfter(maxBeforePow2, 3.0), 0.0);

        double minAfterPow2 = FastMath.nextAfter(2.0, 3.0);
        assertEquals(2.0, FastMath.nextAfter(minAfterPow2, 1.0), 0.0);
    }

    // =========================================================================
    // PARTITION D: EXPONENTIAL, LOGARITHM, POWER, ROOTS
    // =========================================================================

    @Test(timeout = 4000)
    public void testSqrtAndCbrt() {
        assertEquals(3.0, FastMath.sqrt(9.0), EPSILON);
        assertTrue(Double.isNaN(FastMath.sqrt(-1.0)));
        assertEquals(0.0, FastMath.sqrt(0.0), 0.0);

        assertEquals(0.0, FastMath.cbrt(0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.cbrt(Double.NaN)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.cbrt(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(3.0, FastMath.cbrt(27.0), EPSILON);
        assertEquals(-3.0, FastMath.cbrt(-27.0), EPSILON);

        // Subnormal cbrt
        double subnormal = Double.MIN_VALUE * 4.0;
        double cbrtSub = FastMath.cbrt(subnormal);
        assertTrue(cbrtSub > 0.0);
    }

    @Test(timeout = 4000)
    public void testExp() {
        assertEquals(1.0, FastMath.exp(0.0), EPSILON);
        assertEquals(FastMath.E, FastMath.exp(1.0), EPSILON);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(750.0), 0.0);
        assertEquals(0.0, FastMath.exp(-750.0), 0.0);

        // Subnormal output ranges: intVal > 709 and intVal == 709
        assertTrue(FastMath.exp(-715.0) >= 0.0);
        assertTrue(FastMath.exp(-709.5) > 0.0);
    }

    @Test(timeout = 4000)
    public void testExpm1() {
        assertEquals(0.0, FastMath.expm1(0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.expm1(Double.NaN)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.expm1(Double.POSITIVE_INFINITY), 0.0);

        // Outside [-1, 1]
        assertTrue(FastMath.expm1(2.0) > 0.0);
        assertTrue(FastMath.expm1(-2.0) < 0.0);

        // Inside [-1, 1]
        double pos = FastMath.expm1(0.05);
        double neg = FastMath.expm1(-0.05);
        assertEquals(Math.expm1(0.05), pos, EPSILON);
        assertEquals(Math.expm1(-0.05), neg, EPSILON);
    }

    @Test(timeout = 4000)
    public void testLogAndLog10() {
        assertTrue(Double.isNaN(FastMath.log(-1.0)));
        assertTrue(Double.isNaN(FastMath.log(Double.NaN)));
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.log(1.0), EPSILON);

        // Quick coef range (0.99 < x < 1.01)
        assertEquals(Math.log(1.005), FastMath.log(1.005), EPSILON);
        assertEquals(Math.log(0.995), FastMath.log(0.995), EPSILON);

        // Normal and subnormal
        assertEquals(1.0, FastMath.log(FastMath.E), EPSILON);
        double subnormalLog = FastMath.log(Double.MIN_VALUE);
        assertTrue(subnormalLog < 0.0);

        // Log10
        assertEquals(2.0, FastMath.log10(100.0), EPSILON);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log10(0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testLog1p() {
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log1p(-1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log1p(Double.POSITIVE_INFINITY), 0.0);

        // |x| < 1e-6 (Taylor branch)
        assertEquals(1e-8, FastMath.log1p(1e-8), 1e-15);
        assertEquals(-1e-8, FastMath.log1p(-1e-8), 1e-15);

        // |x| >= 1e-6
        assertEquals(Math.log1p(0.5), FastMath.log1p(0.5), EPSILON);
        assertEquals(Math.log1p(-0.5), FastMath.log1p(-0.5), EPSILON);
    }

    @Test(timeout = 4000)
    public void testPow() {
        assertEquals(1.0, FastMath.pow(12.34, 0.0), 0.0);
        assertEquals(1.0, FastMath.pow(0.0, 0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.pow(Double.NaN, 2.0)));
        assertTrue(Double.isNaN(FastMath.pow(2.0, Double.NaN)));

        // x == 0.0 cases
        assertEquals(0.0, FastMath.pow(0.0, 2.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -2.0), 0.0);
        assertEquals(-0.0, FastMath.pow(-0.0, 3.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.pow(-0.0, -3.0), 0.0);

        // x == +Inf
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.POSITIVE_INFINITY, 2.0), 0.0);
        assertEquals(0.0, FastMath.pow(Double.POSITIVE_INFINITY, -2.0), 0.0);

        // y == +Inf
        assertTrue(Double.isNaN(FastMath.pow(1.0, Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.pow(-1.0, Double.POSITIVE_INFINITY)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(2.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.pow(0.5, Double.POSITIVE_INFINITY), 0.0);

        // x == -Inf
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 2.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 3.0), 0.0);
        assertEquals(0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -2.0), 0.0);
        assertEquals(-0.0, FastMath.pow(Double.NEGATIVE_INFINITY, -3.0), 0.0);

        // y == -Inf
        assertTrue(Double.isNaN(FastMath.pow(1.0, Double.NEGATIVE_INFINITY)));
        assertEquals(0.0, FastMath.pow(2.0, Double.NEGATIVE_INFINITY), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.5, Double.NEGATIVE_INFINITY), 0.0);

        // x < 0 cases
        assertTrue(Double.isNaN(FastMath.pow(-2.0, 2.5))); // non-integer y
        assertEquals(4.0, FastMath.pow(-2.0, 2.0), EPSILON); // even int
        assertEquals(-8.0, FastMath.pow(-2.0, 3.0), EPSILON); // odd int
        assertEquals(1.0, FastMath.pow(-1.0, 1e16), EPSILON); // huge even int

        // Large y (|y| >= 8e298)
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(1.1, 9e298), 0.0);
        assertEquals(0.0, FastMath.pow(0.9, 9e298), 0.0);

        // Standard
        assertEquals(8.0, FastMath.pow(2.0, 3.0), EPSILON);
        assertEquals(0.125, FastMath.pow(2.0, -3.0), EPSILON);
    }

    // =========================================================================
    // PARTITION E: TRIGONOMETRIC & HYPERBOLIC FUNCTIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testSinCosTanSpecialValues() {
        // Zero
        assertEquals(0.0, FastMath.sin(0.0), 0.0);
        assertEquals(-0.0, FastMath.sin(-0.0), 0.0);
        assertEquals(1.0, FastMath.cos(0.0), 0.0);
        assertEquals(1.0, FastMath.cos(-0.0), 0.0);
        assertEquals(0.0, FastMath.tan(0.0), 0.0);
        assertEquals(-0.0, FastMath.tan(-0.0), 0.0);

        // NaN & Infinities
        assertTrue(Double.isNaN(FastMath.sin(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.sin(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.cos(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.cos(Double.NEGATIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.tan(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.tan(Double.POSITIVE_INFINITY)));
    }

    @Test(timeout = 4000)
    public void testSinCosTanQuadrantsAndReductions() {
        // Quadrant 0, 1, 2, 3
        assertEquals(Math.sin(0.5), FastMath.sin(0.5), EPSILON);
        assertEquals(Math.sin(2.0), FastMath.sin(2.0), EPSILON); // Cody-Waite reduction
        assertEquals(Math.sin(3.5), FastMath.sin(3.5), EPSILON);
        assertEquals(Math.sin(5.0), FastMath.sin(5.0), EPSILON);

        assertEquals(Math.cos(0.5), FastMath.cos(0.5), EPSILON);
        assertEquals(Math.cos(2.0), FastMath.cos(2.0), EPSILON);
        assertEquals(Math.cos(3.5), FastMath.cos(3.5), EPSILON);
        assertEquals(Math.cos(5.0), FastMath.cos(5.0), EPSILON);

        assertEquals(Math.tan(0.5), FastMath.tan(0.5), EPSILON);
        assertEquals(Math.tan(1.55), FastMath.tan(1.55), EPSILON); // near pi/2
        assertEquals(Math.tan(2.5), FastMath.tan(2.5), EPSILON);

        // Negative angles
        assertEquals(Math.sin(-2.0), FastMath.sin(-2.0), EPSILON);
        assertEquals(Math.cos(-2.0), FastMath.cos(-2.0), EPSILON);
        assertEquals(Math.tan(-2.0), FastMath.tan(-2.0), EPSILON);

        // Payne-Hanek reduction (> 3294198.0)
        double largeAngle = 4000000.0;
        assertEquals(Math.sin(largeAngle), FastMath.sin(largeAngle), 1e-9);
        assertEquals(Math.cos(largeAngle), FastMath.cos(largeAngle), 1e-9);
        assertEquals(Math.tan(largeAngle), FastMath.tan(largeAngle), 1e-9);
    }

    @Test(timeout = 4000)
    public void testAsinAcos() {
        assertTrue(Double.isNaN(FastMath.asin(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.asin(1.5)));
        assertTrue(Double.isNaN(FastMath.asin(-1.5)));
        assertEquals(Math.PI / 2.0, FastMath.asin(1.0), EPSILON);
        assertEquals(-Math.PI / 2.0, FastMath.asin(-1.0), EPSILON);
        assertEquals(0.0, FastMath.asin(0.0), 0.0);
        assertEquals(Math.asin(0.5), FastMath.asin(0.5), EPSILON);

        assertTrue(Double.isNaN(FastMath.acos(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.acos(1.5)));
        assertTrue(Double.isNaN(FastMath.acos(-1.5)));
        assertEquals(0.0, FastMath.acos(1.0), 0.0);
        assertEquals(Math.PI, FastMath.acos(-1.0), EPSILON);
        assertEquals(Math.PI / 2.0, FastMath.acos(0.0), EPSILON);
        assertEquals(Math.acos(0.5), FastMath.acos(0.5), EPSILON);
    }

    @Test(timeout = 4000)
    public void testAtanAndAtan2() {
        // atan
        assertEquals(0.0, FastMath.atan(0.0), 0.0);
        assertEquals(Math.atan(0.5), FastMath.atan(0.5), EPSILON);
        assertEquals(Math.atan(2.0), FastMath.atan(2.0), EPSILON);
        assertEquals(Math.atan(-2.0), FastMath.atan(-2.0), EPSILON);
        // Very large input branch (> 1.633e16)
        assertEquals(Math.PI / 2.0, FastMath.atan(1e18), EPSILON);
        assertEquals(-Math.PI / 2.0, FastMath.atan(-1e18), EPSILON);

        // atan2
        assertTrue(Double.isNaN(FastMath.atan2(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.atan2(1.0, Double.NaN)));

        // y == 0.0
        assertEquals(0.0, FastMath.atan2(0.0, 1.0), 0.0);
        assertEquals(Math.PI, FastMath.atan2(0.0, -1.0), EPSILON);
        assertEquals(-Math.PI, FastMath.atan2(-0.0, -1.0), EPSILON);
        assertEquals(0.0, FastMath.atan2(0.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Math.PI, FastMath.atan2(0.0, Double.NEGATIVE_INFINITY), EPSILON);

        // y == +/- Inf
        assertEquals(Math.PI / 4.0, FastMath.atan2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), EPSILON);
        assertEquals(3.0 * Math.PI / 4.0, FastMath.atan2(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), EPSILON);
        assertEquals(Math.PI / 2.0, FastMath.atan2(Double.POSITIVE_INFINITY, 1.0), EPSILON);

        assertEquals(-Math.PI / 4.0, FastMath.atan2(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), EPSILON);
        assertEquals(-3.0 * Math.PI / 4.0, FastMath.atan2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), EPSILON);
        assertEquals(-Math.PI / 2.0, FastMath.atan2(Double.NEGATIVE_INFINITY, 1.0), EPSILON);

        // x == +/- Inf
        assertEquals(0.0, FastMath.atan2(1.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(-0.0, FastMath.atan2(-1.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Math.PI, FastMath.atan2(1.0, Double.NEGATIVE_INFINITY), EPSILON);
        assertEquals(-Math.PI, FastMath.atan2(-1.0, Double.NEGATIVE_INFINITY), EPSILON);

        // x == 0
        assertEquals(Math.PI / 2.0, FastMath.atan2(1.0, 0.0), EPSILON);
        assertEquals(-Math.PI / 2.0, FastMath.atan2(-1.0, 0.0), EPSILON);

        // Large values (|x| > 8e298)
        assertEquals(Math.atan2(1e300, 1e300), FastMath.atan2(1e300, 1e300), EPSILON);

        // Regular 4 quadrants
        assertEquals(Math.atan2(1.0, 2.0), FastMath.atan2(1.0, 2.0), EPSILON);
        assertEquals(Math.atan2(1.0, -2.0), FastMath.atan2(1.0, -2.0), EPSILON);
        assertEquals(Math.atan2(-1.0, -2.0), FastMath.atan2(-1.0, -2.0), EPSILON);
        assertEquals(Math.atan2(-1.0, 2.0), FastMath.atan2(-1.0, 2.0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testHyperbolicCoshSinhTanh() {
        // cosh
        assertTrue(Double.isNaN(FastMath.cosh(Double.NaN)));
        assertEquals(1.0, FastMath.cosh(0.0), EPSILON);
        assertTrue(FastMath.cosh(25.0) > 1e10); // > 20
        assertTrue(FastMath.cosh(-25.0) > 1e10); // < -20
        assertEquals(Math.cosh(1.5), FastMath.cosh(1.5), EPSILON);
        assertEquals(Math.cosh(-1.5), FastMath.cosh(-1.5), EPSILON);

        // sinh
        assertTrue(Double.isNaN(FastMath.sinh(Double.NaN)));
        assertEquals(0.0, FastMath.sinh(0.0), 0.0);
        assertTrue(FastMath.sinh(25.0) > 1e10);
        assertTrue(FastMath.sinh(-25.0) < -1e10);
        assertEquals(Math.sinh(1.5), FastMath.sinh(1.5), EPSILON); // x > 0.25
        assertEquals(Math.sinh(-1.5), FastMath.sinh(-1.5), EPSILON);
        assertEquals(Math.sinh(0.1), FastMath.sinh(0.1), EPSILON); // x <= 0.25
        assertEquals(Math.sinh(-0.1), FastMath.sinh(-0.1), EPSILON);

        // tanh
        assertTrue(Double.isNaN(FastMath.tanh(Double.NaN)));
        assertEquals(0.0, FastMath.tanh(0.0), 0.0);
        assertEquals(1.0, FastMath.tanh(25.0), 0.0);
        assertEquals(-1.0, FastMath.tanh(-25.0), 0.0);
        assertEquals(Math.tanh(1.5), FastMath.tanh(1.5), EPSILON); // x >= 0.5
        assertEquals(Math.tanh(-1.5), FastMath.tanh(-1.5), EPSILON);
        assertEquals(Math.tanh(0.2), FastMath.tanh(0.2), EPSILON); // x < 0.5
        assertEquals(Math.tanh(-0.2), FastMath.tanh(-0.2), EPSILON);
    }

    @Test(timeout = 4000)
    public void testInverseHyperbolic() {
        // acosh
        assertEquals(0.0, FastMath.acosh(1.0), EPSILON);
        assertTrue(FastMath.acosh(3.0) > 0.0);

        // asinh polynomial partitions: > 0.167, > 0.097, > 0.036, > 0.0036, <= 0.0036
        assertEquals(0.0, FastMath.asinh(0.0), 0.0);
        assertTrue(FastMath.asinh(0.5) > 0.0);
        assertTrue(FastMath.asinh(0.12) > 0.0);
        assertTrue(FastMath.asinh(0.05) > 0.0);
        assertTrue(FastMath.asinh(0.01) > 0.0);
        assertTrue(FastMath.asinh(0.001) > 0.0);
        assertEquals(-FastMath.asinh(0.5), FastMath.asinh(-0.5), EPSILON);

        // atanh polynomial partitions: > 0.15, > 0.087, > 0.031, > 0.003, <= 0.003
        assertEquals(0.0, FastMath.atanh(0.0), 0.0);
        assertTrue(FastMath.atanh(0.5) > 0.0);
        assertTrue(FastMath.atanh(0.1) > 0.0);
        assertTrue(FastMath.atanh(0.05) > 0.0);
        assertTrue(FastMath.atanh(0.01) > 0.0);
        assertTrue(FastMath.atanh(0.001) > 0.0);
        assertEquals(-FastMath.atanh(0.5), FastMath.atanh(-0.5), EPSILON);
    }
}