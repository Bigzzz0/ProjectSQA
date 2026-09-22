package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;

/* [Branch & Defect Analysis Matrix]
 *
 * 1. DEFECT TARGETING:
 *    - binomialCoefficient(48, 22): Precision loss in intermediate floating-point calculation
 *      causes Math.round(binomialCoefficientDouble(48, 22)) to return 27385657281647L instead of
 *      the mathematically exact 27385657281648L.
 *
 * 2. BRANCH & BOUNDARY MATRIX:
 *    - addAndCheck(int, int): Normal, Integer.MAX_VALUE + 1 (overflow), Integer.MIN_VALUE - 1 (overflow).
 *    - addAndCheck(long, long):
 *        * a > b (symmetry swap)
 *        * a < 0, b < 0: Long.MIN_VALUE - b <= a vs overflow
 *        * a < 0, b >= 0: opposite signs (always safe)
 *        * a >= 0, b >= 0: a <= Long.MAX_VALUE - b vs overflow
 *    - subAndCheck(int, int): Normal, positive/negative overflow.
 *    - subAndCheck(long, long):
 *        * b == Long.MIN_VALUE with a < 0 (safe) vs a >= 0 (overflow)
 *        * b != Long.MIN_VALUE (delegates to addAndCheck(a, -b))
 *    - mulAndCheck(int, int): Normal, pos/neg overflow.
 *    - mulAndCheck(long, long):
 *        * a > b (symmetry swap)
 *        * a < 0, b < 0: a >= Long.MAX_VALUE / b vs overflow
 *        * a < 0, b > 0: Long.MIN_VALUE / b <= a vs overflow
 *        * a < 0, b == 0: ret = 0
 *        * a > 0, b > 0: a <= Long.MAX_VALUE / b vs overflow
 *        * a == 0: ret = 0
 *    - binomialCoefficient(int, int), binomialCoefficientDouble, binomialCoefficientLog:
 *        * Preconditions: n < k (IAE), n < 0 (IAE)
 *        * Special base values: n == k, k == 0, k == 1, k == n - 1
 *        * Loop calculations for n! / (k! * (n-k)!)
 *        * Result overflow -> Long.MAX_VALUE throws ArithmeticException
 *    - gcd(int, int):
 *        * u == 0 || v == 0
 *        * u > 0, v > 0 (negation branches)
 *        * Both even cast out twos (k loop)
 *        * k == 31 overflow: gcd(Integer.MIN_VALUE, Integer.MIN_VALUE)
 *        * Odd/even reduction loops (t division, reset max(u, v), replacement)
 *    - lcm(int, int): Normal and overflow checks.
 *    - equals(double, double):
 *        * Both NaN -> true, one NaN -> false, equals -> true, unequal -> false
 *    - equals(double[], double[]):
 *        * Both null, one null, mismatched lengths, identical arrays, differing elements
 *    - indicator & sign:
 *        * byte, short, int, long, float, double for zero, positive, negative, and NaN
 *    - round(double/float, int, int):
 *        * NumberFormatException branches for Double.isInfinite vs NaN
 *        * roundUnscaled methods: CEILING, DOWN, FLOOR, HALF_DOWN, HALF_EVEN, HALF_UP,
 *          UNNECESSARY (inexact throws AE), UP, and default invalid mode (IAE).
 *    - nextAfter(double, double):
 *        * NaN or Infinite returns unchanged
 *        * d == 0 moving to positive vs negative
 *        * Mantissa increase / overflow to next exponent
 *        * Mantissa decrease / underflow to previous exponent
 *    - scalb(double, int): 0, NaN, Infinite, normal scaling
 *    - normalizeAngle(double, double): 2pi periodicity wrapping
 *    - cosh, sinh, log: mathematical limits and standard domains
 */
public class MathUtilsGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testBinomialCoefficientLargeDefect() {
        // Ground truth defect from Defects4J:
        // binomialCoefficient(48, 22) expected:<27385657281648> but was:<27385657281647>
        long result = MathUtils.binomialCoefficient(48, 22);
        assertEquals(27385657281648L, result);
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & ARITHMETIC WITH OVERFLOW CHECKS
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddAndCheckInt() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(-5, MathUtils.addAndCheck(-2, -3));
        assertEquals(1, MathUtils.addAndCheck(-2, 3));
        assertEquals(Integer.MAX_VALUE, MathUtils.addAndCheck(Integer.MAX_VALUE, 0));
        assertEquals(Integer.MIN_VALUE, MathUtils.addAndCheck(Integer.MIN_VALUE, 0));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckIntPositiveOverflow() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckIntNegativeOverflow() {
        MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
    }

    @Test(timeout = 4000)
    public void testAddAndCheckLong() {
        // a > b symmetry
        assertEquals(10L, MathUtils.addAndCheck(7L, 3L));
        assertEquals(10L, MathUtils.addAndCheck(3L, 7L));

        // opposite sign
        assertEquals(-2L, MathUtils.addAndCheck(-5L, 3L));
        assertEquals(2L, MathUtils.addAndCheck(5L, -3L));

        // both negative safe
        assertEquals(-10L, MathUtils.addAndCheck(-7L, -3L));
        assertEquals(Long.MIN_VALUE, MathUtils.addAndCheck(Long.MIN_VALUE + 5, -5L));

        // both positive safe
        assertEquals(Long.MAX_VALUE, MathUtils.addAndCheck(Long.MAX_VALUE - 5, 5L));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckLongPositiveOverflow() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckLongPositiveOverflowSymmetric() {
        MathUtils.addAndCheck(1L, Long.MAX_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckLongNegativeOverflow() {
        MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckLongNegativeOverflowSymmetric() {
        MathUtils.addAndCheck(-1L, Long.MIN_VALUE);
    }

    @Test(timeout = 4000)
    public void testSubAndCheckInt() {
        assertEquals(-1, MathUtils.subAndCheck(2, 3));
        assertEquals(5, MathUtils.subAndCheck(2, -3));
        assertEquals(Integer.MAX_VALUE, MathUtils.subAndCheck(Integer.MAX_VALUE, 0));
        assertEquals(Integer.MIN_VALUE, MathUtils.subAndCheck(Integer.MIN_VALUE, 0));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckIntPositiveOverflow() {
        MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckIntNegativeOverflow() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    @Test(timeout = 4000)
    public void testSubAndCheckLong() {
        assertEquals(-1L, MathUtils.subAndCheck(2L, 3L));
        assertEquals(5L, MathUtils.subAndCheck(2L, -3L));
        // b == Long.MIN_VALUE and a < 0 branch
        assertEquals(0L, MathUtils.subAndCheck(Long.MIN_VALUE, Long.MIN_VALUE));
        assertEquals(Long.MAX_VALUE, MathUtils.subAndCheck(-1L, Long.MIN_VALUE));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckLongMinBoundaryOverflow() {
        // b == Long.MIN_VALUE and a >= 0
        MathUtils.subAndCheck(0L, Long.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckLongPositiveOverflow() {
        MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
    }

    @Test(timeout = 4000)
    public void testMulAndCheckInt() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(-6, MathUtils.mulAndCheck(2, -3));
        assertEquals(0, MathUtils.mulAndCheck(0, 5));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckIntOverflowPositive() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckIntOverflowNegative() {
        MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
    }

    @Test(timeout = 4000)
    public void testMulAndCheckLong() {
        // Symmetry a > b
        assertEquals(12L, MathUtils.mulAndCheck(4L, 3L));
        assertEquals(12L, MathUtils.mulAndCheck(3L, 4L));

        // a < 0, b < 0
        assertEquals(12L, MathUtils.mulAndCheck(-3L, -4L));

        // a < 0, b > 0
        assertEquals(-12L, MathUtils.mulAndCheck(-3L, 4L));

        // a < 0, b == 0
        assertEquals(0L, MathUtils.mulAndCheck(-5L, 0L));

        // a > 0, b > 0
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));

        // a == 0
        assertEquals(0L, MathUtils.mulAndCheck(0L, 10L));
        assertEquals(0L, MathUtils.mulAndCheck(0L, 0L));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckLongOverflowNegativeBoth() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckLongOverflowMixedSigns() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckLongOverflowPositiveBoth() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    // =========================================================================
    // PARTITION B: BINOMIAL COEFFICIENT & FACTORIAL TESTS
    // =========================================================================

    @Test(timeout = 4000)
    public void testBinomialCoefficientSpecialCases() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 3));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientNegativeN() {
        MathUtils.binomialCoefficient(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientKLargerThanN() {
        MathUtils.binomialCoefficient(4, 5);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testBinomialCoefficientOverflow() {
        // n=67, k=33 exceeds Long.MAX_VALUE
        MathUtils.binomialCoefficient(67, 33);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientDoubleAndLog() {
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 5), 1e-10);
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 1e-10);
        assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 1), 1e-10);
        assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 4), 1e-10);
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-10);

        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 5), 1e-10);
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-10);
        assertEquals(Math.log(5.0), MathUtils.binomialCoefficientLog(5, 1), 1e-10);
        assertEquals(Math.log(5.0), MathUtils.binomialCoefficientLog(5, 4), 1e-10);
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(5, 2), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientDoubleKGreaterThanN() {
        MathUtils.binomialCoefficientDouble(2, 3);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientDoubleNegativeN() {
        MathUtils.binomialCoefficientDouble(-2, 0);
    }

    @Test(timeout = 4000)
    public void testFactorial() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(2L, MathUtils.factorial(2));
        assertEquals(6L, MathUtils.factorial(3));
        assertEquals(2432902008176640000L, MathUtils.factorial(20));

        assertEquals(1.0, MathUtils.factorialDouble(0), 1e-10);
        assertEquals(24.0, MathUtils.factorialDouble(4), 1e-10);
        assertTrue(MathUtils.factorialDouble(21) > 2432902008176640000.0);
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.factorialDouble(171), 1e-10);

        assertEquals(0.0, MathUtils.factorialLog(0), 1e-10);
        assertEquals(0.0, MathUtils.factorialLog(1), 1e-10);
        assertEquals(Math.log(6.0), MathUtils.factorialLog(3), 1e-10);
        assertTrue(MathUtils.factorialLog(25) > MathUtils.factorialLog(20));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactorialNegative() {
        MathUtils.factorial(-1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testFactorialTooLarge() {
        MathUtils.factorial(21);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactorialDoubleNegative() {
        MathUtils.factorialDouble(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactorialLogNegative() {
        MathUtils.factorialLog(-1);
    }

    // =========================================================================
    // PARTITION C: GCD & LCM
    // =========================================================================

    @Test(timeout = 4000)
    public void testGcd() {
        assertEquals(6, MathUtils.gcd(30, 24));
        assertEquals(6, MathUtils.gcd(-30, 24));
        assertEquals(6, MathUtils.gcd(30, -24));
        assertEquals(6, MathUtils.gcd(-30, -24));
        assertEquals(5, MathUtils.gcd(0, 5));
        assertEquals(5, MathUtils.gcd(5, 0));
        assertEquals(1, MathUtils.gcd(17, 13));
        assertEquals(16, MathUtils.gcd(32, 48));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGcdOverflow() {
        // gcd of MIN_VALUE and MIN_VALUE overflows since 2^31 cannot fit in positive int
        MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Test(timeout = 4000)
    public void testLcm() {
        assertEquals(12, MathUtils.lcm(4, 6));
        assertEquals(12, MathUtils.lcm(-4, 6));
        assertEquals(12, MathUtils.lcm(4, -6));
        assertEquals(0, MathUtils.lcm(0, 5));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testLcmOverflow() {
        MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
    }

    // =========================================================================
    // PARTITION D: EQUALITY & HASHING
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsDouble() {
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equals(Double.NaN, 1.0));
        assertFalse(MathUtils.equals(1.0, Double.NaN));
        assertTrue(MathUtils.equals(1.5, 1.5));
        assertFalse(MathUtils.equals(1.5, 1.6));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArray() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
        assertFalse(MathUtils.equals(new double[]{1.0}, new double[]{1.0, 2.0}));
        assertTrue(MathUtils.equals(new double[]{1.0, Double.NaN}, new double[]{1.0, Double.NaN}));
        assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 3.0}));
    }

    @Test(timeout = 4000)
    public void testHash() {
        assertEquals(new Double(42.5).hashCode(), MathUtils.hash(42.5));
        assertEquals(java.util.Arrays.hashCode(new double[]{1.0, 2.0}), MathUtils.hash(new double[]{1.0, 2.0}));
        assertEquals(0, MathUtils.hash((double[]) null));
    }

    // =========================================================================
    // PARTITION E: INDICATORS & SIGNS
    // =========================================================================

    @Test(timeout = 4000)
    public void testIndicator() {
        assertEquals((byte) 1, MathUtils.indicator((byte) 0));
        assertEquals((byte) 1, MathUtils.indicator((byte) 5));
        assertEquals((byte) -1, MathUtils.indicator((byte) -5));

        assertEquals((short) 1, MathUtils.indicator((short) 0));
        assertEquals((short) 1, MathUtils.indicator((short) 5));
        assertEquals((short) -1, MathUtils.indicator((short) -5));

        assertEquals(1, MathUtils.indicator(0));
        assertEquals(1, MathUtils.indicator(5));
        assertEquals(-1, MathUtils.indicator(-5));

        assertEquals(1L, MathUtils.indicator(0L));
        assertEquals(1L, MathUtils.indicator(5L));
        assertEquals(-1L, MathUtils.indicator(-5L));

        assertEquals(1.0f, MathUtils.indicator(0.0f), 0.0f);
        assertEquals(1.0f, MathUtils.indicator(5.0f), 0.0f);
        assertEquals(-1.0f, MathUtils.indicator(-5.0f), 0.0f);
        assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));

        assertEquals(1.0, MathUtils.indicator(0.0), 0.0);
        assertEquals(1.0, MathUtils.indicator(5.0), 0.0);
        assertEquals(-1.0, MathUtils.indicator(-5.0), 0.0);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testSign() {
        assertEquals((byte) 0, MathUtils.sign((byte) 0));
        assertEquals((byte) 1, MathUtils.sign((byte) 5));
        assertEquals((byte) -1, MathUtils.sign((byte) -5));

        assertEquals((short) 0, MathUtils.sign((short) 0));
        assertEquals((short) 1, MathUtils.sign((short) 5));
        assertEquals((short) -1, MathUtils.sign((short) -5));

        assertEquals(0, MathUtils.sign(0));
        assertEquals(1, MathUtils.sign(5));
        assertEquals(-1, MathUtils.sign(-5));

        assertEquals(0L, MathUtils.sign(0L));
        assertEquals(1L, MathUtils.sign(5L));
        assertEquals(-1L, MathUtils.sign(-5L));

        assertEquals(0.0f, MathUtils.sign(0.0f), 0.0f);
        assertEquals(1.0f, MathUtils.sign(5.0f), 0.0f);
        assertEquals(-1.0f, MathUtils.sign(-5.0f), 0.0f);
        assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));

        assertEquals(0.0, MathUtils.sign(0.0), 0.0);
        assertEquals(1.0, MathUtils.sign(5.0), 0.0);
        assertEquals(-1.0, MathUtils.sign(-5.0), 0.0);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));
    }

    // =========================================================================
    // PARTITION F: ROUNDING & NORMALIZATION
    // =========================================================================

    @Test(timeout = 4000)
    public void testRoundDouble() {
        assertEquals(1.23, MathUtils.round(1.234, 2), 1e-10);
        assertEquals(1.24, MathUtils.round(1.235, 2), 1e-10);
        assertEquals(1.23, MathUtils.round(1.235, 2, BigDecimal.ROUND_DOWN), 1e-10);
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 2), 0.0);
        assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2)));
    }

    @Test(timeout = 4000)
    public void testRoundFloatAllModes() {
        // ROUND_CEILING
        assertEquals(2.0f, MathUtils.round(1.2f, 0, BigDecimal.ROUND_CEILING), 0.0f);
        assertEquals(-1.0f, MathUtils.round(-1.2f, 0, BigDecimal.ROUND_CEILING), 0.0f);

        // ROUND_DOWN
        assertEquals(1.0f, MathUtils.round(1.8f, 0, BigDecimal.ROUND_DOWN), 0.0f);
        assertEquals(-1.0f, MathUtils.round(-1.8f, 0, BigDecimal.ROUND_DOWN), 0.0f);

        // ROUND_FLOOR
        assertEquals(1.0f, MathUtils.round(1.8f, 0, BigDecimal.ROUND_FLOOR), 0.0f);
        assertEquals(-2.0f, MathUtils.round(-1.2f, 0, BigDecimal.ROUND_FLOOR), 0.0f);

        // ROUND_HALF_DOWN
        assertEquals(2.0f, MathUtils.round(1.51f, 0, BigDecimal.ROUND_HALF_DOWN), 0.0f);
        assertEquals(1.0f, MathUtils.round(1.50f, 0, BigDecimal.ROUND_HALF_DOWN), 0.0f);

        // ROUND_HALF_EVEN
        assertEquals(2.0f, MathUtils.round(1.51f, 0, BigDecimal.ROUND_HALF_EVEN), 0.0f);
        assertEquals(1.0f, MathUtils.round(1.49f, 0, BigDecimal.ROUND_HALF_EVEN), 0.0f);
        assertEquals(2.0f, MathUtils.round(2.5f, 0, BigDecimal.ROUND_HALF_EVEN), 0.0f);
        assertEquals(2.0f, MathUtils.round(1.5f, 0, BigDecimal.ROUND_HALF_EVEN), 0.0f);

        // ROUND_HALF_UP
        assertEquals(2.0f, MathUtils.round(1.5f, 0, BigDecimal.ROUND_HALF_UP), 0.0f);
        assertEquals(1.0f, MathUtils.round(1.4f, 0, BigDecimal.ROUND_HALF_UP), 0.0f);
        assertEquals(1.24f, MathUtils.round(1.235f, 2), 1e-5f);

        // ROUND_UP
        assertEquals(2.0f, MathUtils.round(1.1f, 0, BigDecimal.ROUND_UP), 0.0f);

        // ROUND_UNNECESSARY
        assertEquals(1.0f, MathUtils.round(1.0f, 0, BigDecimal.ROUND_UNNECESSARY), 0.0f);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testRoundFloatUnnecessaryInexact() {
        MathUtils.round(1.5f, 0, BigDecimal.ROUND_UNNECESSARY);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRoundFloatInvalidMethod() {
        MathUtils.round(1.0f, 0, 9999);
    }

    @Test(timeout = 4000)
    public void testNormalizeAngle() {
        assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-10);
        assertEquals(Math.PI, MathUtils.normalizeAngle(3 * Math.PI, Math.PI), 1e-10);
        assertEquals(-Math.PI / 2, MathUtils.normalizeAngle(3.5 * Math.PI, 0.0), 1e-10);
    }

    // =========================================================================
    // PARTITION G: NEXTAFTER, SCALB, LOG, COSH, SINH
    // =========================================================================

    @Test(timeout = 4000)
    public void testNextAfterSpecialCases() {
        assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1.0)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.nextAfter(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.nextAfter(Double.NEGATIVE_INFINITY, 1.0), 0.0);

        assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testNextAfterDirectionTransitions() {
        // Increase mantissa normal
        double d1 = 1.0;
        double nextUp = MathUtils.nextAfter(d1, 2.0);
        assertTrue(nextUp > d1);

        // Increase mantissa overflow to next exponent (mantissa == 0x000fffffffffffffL)
        double dMaxMantissa = Double.longBitsToDouble(0x3ff0000000000000L | 0x000fffffffffffffL);
        double nextExp = MathUtils.nextAfter(dMaxMantissa, 3.0);
        assertEquals(2.0, nextExp, 0.0);

        // Decrease mantissa underflow to previous exponent (mantissa == 0L)
        double dPowerOfTwo = 2.0;
        double nextDown = MathUtils.nextAfter(dPowerOfTwo, 1.0);
        assertEquals(dMaxMantissa, nextDown, 0.0);

        // Decrease mantissa normal
        double dMid = 1.5;
        double nextDownMid = MathUtils.nextAfter(dMid, 1.0);
        assertTrue(nextDownMid < dMid);
    }

    @Test(timeout = 4000)
    public void testScalb() {
        assertEquals(0.0, MathUtils.scalb(0.0, 5), 0.0);
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 5)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.scalb(Double.POSITIVE_INFINITY, 5), 0.0);
        assertEquals(8.0, MathUtils.scalb(1.0, 3), 1e-10);
        assertEquals(0.25, MathUtils.scalb(1.0, -2), 1e-10);
    }

    @Test(timeout = 4000)
    public void testLog() {
        assertEquals(2.0, MathUtils.log(10.0, 100.0), 1e-10);
        assertEquals(3.0, MathUtils.log(2.0, 8.0), 1e-10);
        assertTrue(Double.isNaN(MathUtils.log(-2.0, 8.0)));
        assertTrue(Double.isNaN(MathUtils.log(2.0, -8.0)));
    }

    @Test(timeout = 4000)
    public void testHyperbolic() {
        assertEquals(1.0, MathUtils.cosh(0.0), 1e-10);
        assertEquals(0.0, MathUtils.sinh(0.0), 1e-10);
        assertEquals(MathUtils.cosh(1.5), MathUtils.cosh(-1.5), 1e-10);
        assertEquals(MathUtils.sinh(1.5), -MathUtils.sinh(-1.5), 1e-10);
    }

    @Test(timeout = 4000)
    public void testConstants() {
        assertTrue(MathUtils.EPSILON > 0.0);
        assertTrue(MathUtils.SAFE_MIN > 0.0);
        assertEquals(1.0 - MathUtils.EPSILON < 1.0, true);
    }
}