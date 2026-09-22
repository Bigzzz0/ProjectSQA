package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. DEFECT UNDER TEST (Math-94):
 *    - Method: MathUtils.gcd(int u, int v)
 *    - Defect: Line 374 uses `if (u * v == 0)` to check if either argument is zero.
 *              When u and v are both non-zero large integers whose 32-bit product overflows to 0
 *              (e.g., u = 3 * 2^20 = 3145728, v = 9 * 2^15 = 294912, where u * v = 27 * 2^35 = 0 mod 2^32),
 *              the method mistakenly takes the early exit branch and returns (|u| + |v|) = 3440640
 *              instead of computing the true GCD = 98304.
 *
 * 2. BRANCH & CONDITION COVERAGE TARGETS:
 *    - MathUtils private constructor (via reflection for 100% line coverage)
 *    - addAndCheck(int, int): normal addition, positive overflow (> MAX_VALUE), negative overflow (< MIN_VALUE)
 *    - addAndCheck(long, long): a > b symmetry branch, a < 0 & b < 0 (both negative, overflow vs valid),
 *      a < 0 & b >= 0 (opposite sign safe), a >= 0 & b >= 0 (positive overflow vs valid)
 *    - subAndCheck(int, int): normal, positive overflow, negative overflow
 *    - subAndCheck(long, long): b == Long.MIN_VALUE with a < 0 vs a >= 0, normal delegation to addAndCheck
 *    - mulAndCheck(int, int): normal, positive overflow, negative overflow
 *    - mulAndCheck(long, long): a > b symmetry, a < 0 & b < 0 (overflow vs valid), a < 0 & b > 0 (overflow vs valid),
 *      a < 0 & b == 0, a > 0 & b > 0 (overflow vs valid), a == 0
 *    - gcd(int, int): zero inputs, positive inputs, negative inputs, u > 0, v > 0, power-of-two extraction loop,
 *      k == 31 overflow check (gcd(MIN_VALUE, MIN_VALUE)), Stein loop t > 0 and t <= 0 updates
 *    - lcm(int, int): normal, negative values, overflow in mulAndCheck
 *    - binomialCoefficient(int, int): n < k, n < 0, n == k, k == 0, k == 1, k == n - 1, overflow check (result == Long.MAX_VALUE)
 *    - binomialCoefficientDouble(int, int) & binomialCoefficientLog(int, int): bounds, edge cases, loops
 *    - factorial, factorialDouble, factorialLog: n < 0, n = 0, n = 1, small n, overflow on n = 21 (result == Long.MAX_VALUE),
 *      Double.POSITIVE_INFINITY on n > 170
 *    - cosh(double), sinh(double): negative, zero, positive
 *    - equals(double, double): both NaN, one NaN, equal, unequal
 *    - equals(double[], double[]): both null, one null, different lengths, identical, mismatched elements, NaN elements
 *    - hash(double), hash(double[]): null array, empty array, non-empty, values
 *    - indicator & sign for all 6 primitives (byte, double, float, int, long, short): negative, zero, positive, NaN (for floating-point)
 *    - log(double, double): positive base & arg, negative args, base 0, arg 0
 *    - nextAfter(double, double): NaN, infinite, d == 0 with direction < 0 vs >= 0, mantissa increase (mantissa max vs normal),
 *      mantissa decrease (mantissa 0 vs normal)
 *    - scalb(double, int): 0, NaN, infinite, positive scale, negative scale
 *    - normalizeAngle(double, double): intervals around center
 *    - round(double, int, int): normal, infinite, NaN (via NumberFormatException)
 *    - round(float, int, int) / roundUnscaled: all 8 BigDecimal rounding modes (CEILING, FLOOR, UP, DOWN,
 *      HALF_UP, HALF_DOWN, HALF_EVEN, UNNECESSARY with exact vs inexact), default invalid mode
 */
public class MathUtilsGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Math-94)
    // =========================================================================

    /**
     * Targets the defect where `u * v == 0` causes integer multiplication overflow to 0
     * when u and v have enough factors of 2 (product is a multiple of 2^32).
     * u = 3 * (1 << 20) = 3145728
     * v = 9 * (1 << 15) = 294912
     * Expected GCD is 3 * (1 << 15) = 98304.
     * The buggy code returns |u| + |v| = 3440640.
     */
    @Test(timeout = 4000)
    public void testGcdIntegerOverflowBug() {
        int u = 3 * (1 << 20);
        int v = 9 * (1 << 15);
        int expected = 3 * (1 << 15); // 98304
        int actual = MathUtils.gcd(u, v);
        assertEquals(expected, actual);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddAndCheckInt() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(-5, MathUtils.addAndCheck(-2, -3));
        assertEquals(1, MathUtils.addAndCheck(-2, 3));
        assertEquals(Integer.MAX_VALUE, MathUtils.addAndCheck(Integer.MAX_VALUE, 0));
        assertEquals(Integer.MIN_VALUE, MathUtils.addAndCheck(Integer.MIN_VALUE, 0));
    }

    @Test(timeout = 4000)
    public void testAddAndCheckLong() {
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
        assertEquals(5L, MathUtils.addAndCheck(3L, 2L)); // a > b branch
        assertEquals(-5L, MathUtils.addAndCheck(-2L, -3L));
        assertEquals(-5L, MathUtils.addAndCheck(-3L, -2L)); // a > b branch with negative numbers
        assertEquals(1L, MathUtils.addAndCheck(-2L, 3L));
        assertEquals(Long.MAX_VALUE, MathUtils.addAndCheck(Long.MAX_VALUE - 10L, 10L));
        assertEquals(Long.MIN_VALUE, MathUtils.addAndCheck(Long.MIN_VALUE + 10L, -10L));
    }

    @Test(timeout = 4000)
    public void testSubAndCheckInt() {
        assertEquals(1, MathUtils.subAndCheck(3, 2));
        assertEquals(-1, MathUtils.subAndCheck(2, 3));
        assertEquals(Integer.MAX_VALUE, MathUtils.subAndCheck(Integer.MAX_VALUE, 0));
        assertEquals(Integer.MIN_VALUE, MathUtils.subAndCheck(Integer.MIN_VALUE, 0));
    }

    @Test(timeout = 4000)
    public void testSubAndCheckLong() {
        assertEquals(1L, MathUtils.subAndCheck(3L, 2L));
        assertEquals(-1L, MathUtils.subAndCheck(2L, 3L));
        // b == Long.MIN_VALUE and a < 0 branch
        assertEquals(Long.MAX_VALUE, MathUtils.subAndCheck(-1L, Long.MIN_VALUE));
        assertEquals(0L, MathUtils.subAndCheck(Long.MIN_VALUE, Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testMulAndCheckInt() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(-6, MathUtils.mulAndCheck(-2, 3));
        assertEquals(6, MathUtils.mulAndCheck(-2, -3));
        assertEquals(0, MathUtils.mulAndCheck(0, 100));
        assertEquals(0, MathUtils.mulAndCheck(100, 0));
    }

    @Test(timeout = 4000)
    public void testMulAndCheckLong() {
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        assertEquals(6L, MathUtils.mulAndCheck(3L, 2L)); // a > b
        assertEquals(-6L, MathUtils.mulAndCheck(-2L, 3L));
        assertEquals(6L, MathUtils.mulAndCheck(-2L, -3L));
        assertEquals(0L, MathUtils.mulAndCheck(-2L, 0L));
        assertEquals(0L, MathUtils.mulAndCheck(0L, -2L));
        assertEquals(0L, MathUtils.mulAndCheck(0L, 0L));
        assertEquals(0L, MathUtils.mulAndCheck(0L, 5L));
        assertEquals(Long.MAX_VALUE, MathUtils.mulAndCheck(Long.MAX_VALUE, 1L));
        assertEquals(Long.MIN_VALUE, MathUtils.mulAndCheck(Long.MIN_VALUE, 1L));
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficient() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 3));

        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 1e-10);
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-10);

        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-10);
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 5), 1e-10);
        assertEquals(Math.log(5.0), MathUtils.binomialCoefficientLog(5, 1), 1e-10);
        assertEquals(Math.log(5.0), MathUtils.binomialCoefficientLog(5, 4), 1e-10);
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(5, 2), 1e-10);
    }

    @Test(timeout = 4000)
    public void testFactorials() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(2L, MathUtils.factorial(2));
        assertEquals(6L, MathUtils.factorial(3));
        assertEquals(24L, MathUtils.factorial(4));
        assertEquals(120L, MathUtils.factorial(5));
        assertEquals(2432902008176640000L, MathUtils.factorial(20));

        assertEquals(1.0, MathUtils.factorialDouble(0), 1e-10);
        assertEquals(120.0, MathUtils.factorialDouble(5), 1e-10);
        assertTrue(Double.isInfinite(MathUtils.factorialDouble(171)));

        assertEquals(0.0, MathUtils.factorialLog(0), 1e-10);
        assertEquals(0.0, MathUtils.factorialLog(1), 1e-10);
        assertEquals(Math.log(120.0), MathUtils.factorialLog(5), 1e-10);
    }

    @Test(timeout = 4000)
    public void testHyperbolicFunctions() {
        assertEquals(1.0, MathUtils.cosh(0.0), 1e-15);
        assertEquals(0.0, MathUtils.sinh(0.0), 1e-15);

        double x = 1.5;
        double expectedCosh = (Math.exp(x) + Math.exp(-x)) / 2.0;
        double expectedSinh = (Math.exp(x) - Math.exp(-x)) / 2.0;
        assertEquals(expectedCosh, MathUtils.cosh(x), 1e-15);
        assertEquals(expectedSinh, MathUtils.sinh(x), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGcdAndLcm() {
        assertEquals(6, MathUtils.gcd(54, 24));
        assertEquals(6, MathUtils.gcd(-54, 24));
        assertEquals(6, MathUtils.gcd(54, -24));
        assertEquals(6, MathUtils.gcd(-54, -24));

        assertEquals(5, MathUtils.gcd(0, 5));
        assertEquals(5, MathUtils.gcd(5, 0));
        assertEquals(0, MathUtils.gcd(0, 0));

        assertEquals(12, MathUtils.lcm(4, 6));
        assertEquals(12, MathUtils.lcm(-4, 6));
        assertEquals(12, MathUtils.lcm(4, -6));
        assertEquals(12, MathUtils.lcm(-4, -6));
        assertEquals(0, MathUtils.lcm(0, 5));
    }

    @Test(timeout = 4000)
    public void testScalb() {
        assertEquals(0.0, MathUtils.scalb(0.0, 5), 0.0);
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 5)));
        assertTrue(Double.isInfinite(MathUtils.scalb(Double.POSITIVE_INFINITY, 5)));
        assertTrue(Double.isInfinite(MathUtils.scalb(Double.NEGATIVE_INFINITY, 5)));

        assertEquals(6.0, MathUtils.scalb(1.5, 2), 1e-15);
        assertEquals(1.5, MathUtils.scalb(6.0, -2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testNormalizeAngle() {
        assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-15);
        assertEquals(Math.PI, MathUtils.normalizeAngle(3.0 * Math.PI, Math.PI), 1e-15);
        assertEquals(-Math.PI / 2.0, MathUtils.normalizeAngle(3.5 * Math.PI, 0.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testLog() {
        assertEquals(3.0, MathUtils.log(2.0, 8.0), 1e-15);
        assertEquals(2.0, MathUtils.log(10.0, 100.0), 1e-15);
        assertTrue(Double.isNaN(MathUtils.log(-2.0, 8.0)));
        assertTrue(Double.isNaN(MathUtils.log(2.0, -8.0)));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsDouble() {
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equals(Double.NaN, 1.0));
        assertFalse(MathUtils.equals(1.0, Double.NaN));
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertFalse(MathUtils.equals(1.0, 1.0 + 1e-10));
        assertTrue(MathUtils.equals(0.0, -0.0));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArray() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
        assertFalse(MathUtils.equals(new double[]{1.0}, new double[]{1.0, 2.0}));
        assertTrue(MathUtils.equals(new double[]{}, new double[]{}));
        assertTrue(MathUtils.equals(new double[]{1.0, Double.NaN, 3.0}, new double[]{1.0, Double.NaN, 3.0}));
        assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 3.0}));
    }

    @Test(timeout = 4000)
    public void testHash() {
        assertEquals(new Double(42.5).hashCode(), MathUtils.hash(42.5));
        assertEquals(0, MathUtils.hash((double[]) null));
        double[] arr = new double[]{1.0, 2.0, Double.NaN};
        assertEquals(java.util.Arrays.hashCode(arr), MathUtils.hash(arr));
    }

    @Test(timeout = 4000)
    public void testIndicators() {
        assertEquals((byte) 1, MathUtils.indicator((byte) 5));
        assertEquals((byte) 1, MathUtils.indicator((byte) 0));
        assertEquals((byte) -1, MathUtils.indicator((byte) -5));

        assertEquals(1.0, MathUtils.indicator(5.0), 0.0);
        assertEquals(1.0, MathUtils.indicator(0.0), 0.0);
        assertEquals(-1.0, MathUtils.indicator(-5.0), 0.0);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));

        assertEquals(1.0f, MathUtils.indicator(5.0f), 0.0f);
        assertEquals(1.0f, MathUtils.indicator(0.0f), 0.0f);
        assertEquals(-1.0f, MathUtils.indicator(-5.0f), 0.0f);
        assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));

        assertEquals(1, MathUtils.indicator(5));
        assertEquals(1, MathUtils.indicator(0));
        assertEquals(-1, MathUtils.indicator(-5));

        assertEquals(1L, MathUtils.indicator(5L));
        assertEquals(1L, MathUtils.indicator(0L));
        assertEquals(-1L, MathUtils.indicator(-5L));

        assertEquals((short) 1, MathUtils.indicator((short) 5));
        assertEquals((short) 1, MathUtils.indicator((short) 0));
        assertEquals((short) -1, MathUtils.indicator((short) -5));
    }

    @Test(timeout = 4000)
    public void testSign() {
        assertEquals((byte) 1, MathUtils.sign((byte) 5));
        assertEquals((byte) 0, MathUtils.sign((byte) 0));
        assertEquals((byte) -1, MathUtils.sign((byte) -5));

        assertEquals(1.0, MathUtils.sign(5.0), 0.0);
        assertEquals(0.0, MathUtils.sign(0.0), 0.0);
        assertEquals(-1.0, MathUtils.sign(-5.0), 0.0);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));

        assertEquals(1.0f, MathUtils.sign(5.0f), 0.0f);
        assertEquals(0.0f, MathUtils.sign(0.0f), 0.0f);
        assertEquals(-1.0f, MathUtils.sign(-5.0f), 0.0f);
        assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));

        assertEquals(1, MathUtils.sign(5));
        assertEquals(0, MathUtils.sign(0));
        assertEquals(-1, MathUtils.sign(-5));

        assertEquals(1L, MathUtils.sign(5L));
        assertEquals(0L, MathUtils.sign(0L));
        assertEquals(-1L, MathUtils.sign(-5L));

        assertEquals((short) 1, MathUtils.sign((short) 5));
        assertEquals((short) 0, MathUtils.sign((short) 0));
        assertEquals((short) -1, MathUtils.sign((short) -5));
    }

    @Test(timeout = 4000)
    public void testNextAfter() {
        assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1.0)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.nextAfter(Double.POSITIVE_INFINITY, 0.0), 0.0);
        assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1.0), 0.0);
        assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1.0), 0.0);

        // Increase mantissa: mantissa == 0x000fffffffffffffL
        double dMaxMantissa = Double.longBitsToDouble(0x3ff0000000000000L | 0x000fffffffffffffL);
        double nextOver = MathUtils.nextAfter(dMaxMantissa, 2.0);
        assertTrue(nextOver > dMaxMantissa);

        // Increase mantissa normal
        double dNormal = 1.0;
        double nextUp = MathUtils.nextAfter(dNormal, 2.0);
        assertTrue(nextUp > dNormal);

        // Decrease mantissa: mantissa == 0L
        double dZeroMantissa = Double.longBitsToDouble(0x3ff0000000000000L); // 1.0
        double nextDownExp = MathUtils.nextAfter(dZeroMantissa, 0.0);
        assertTrue(nextDownExp < dZeroMantissa);

        // Decrease mantissa normal
        double dNormalDesc = 1.5;
        double nextDown = MathUtils.nextAfter(dNormalDesc, 0.0);
        assertTrue(nextDown < dNormalDesc);
    }

    @Test(timeout = 4000)
    public void testRoundDouble() {
        assertEquals(1.23, MathUtils.round(1.2345, 2), 1e-15);
        assertEquals(1.24, MathUtils.round(1.2355, 2), 1e-15);
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 2), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.round(Double.NEGATIVE_INFINITY, 2), 0.0);
        assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2)));
    }

    @Test(timeout = 4000)
    public void testRoundFloatAllModes() {
        // ROUND_HALF_UP (default float round)
        assertEquals(1.23f, MathUtils.round(1.234f, 2), 1e-6f);
        assertEquals(1.24f, MathUtils.round(1.235f, 2), 1e-6f);

        // ROUND_CEILING: positive vs negative sign
        assertEquals(1.3f, MathUtils.round(1.21f, 1, BigDecimal.ROUND_CEILING), 1e-6f);
        assertEquals(-1.2f, MathUtils.round(-1.29f, 1, BigDecimal.ROUND_CEILING), 1e-6f);

        // ROUND_FLOOR: positive vs negative sign
        assertEquals(1.2f, MathUtils.round(1.29f, 1, BigDecimal.ROUND_FLOOR), 1e-6f);
        assertEquals(-1.3f, MathUtils.round(-1.21f, 1, BigDecimal.ROUND_FLOOR), 1e-6f);

        // ROUND_DOWN
        assertEquals(1.2f, MathUtils.round(1.29f, 1, BigDecimal.ROUND_DOWN), 1e-6f);
        assertEquals(-1.2f, MathUtils.round(-1.29f, 1, BigDecimal.ROUND_DOWN), 1e-6f);

        // ROUND_UP
        assertEquals(1.3f, MathUtils.round(1.21f, 1, BigDecimal.ROUND_UP), 1e-6f);
        assertEquals(-1.3f, MathUtils.round(-1.21f, 1, BigDecimal.ROUND_UP), 1e-6f);

        // ROUND_HALF_DOWN
        assertEquals(1.2f, MathUtils.round(1.25f, 1, BigDecimal.ROUND_HALF_DOWN), 1e-6f);
        assertEquals(1.3f, MathUtils.round(1.26f, 1, BigDecimal.ROUND_HALF_DOWN), 1e-6f);

        // ROUND_HALF_EVEN: even vs odd
        assertEquals(1.2f, MathUtils.round(1.25f, 1, BigDecimal.ROUND_HALF_EVEN), 1e-6f);
        assertEquals(1.4f, MathUtils.round(1.35f, 1, BigDecimal.ROUND_HALF_EVEN), 1e-6f);
        assertEquals(1.2f, MathUtils.round(1.24f, 1, BigDecimal.ROUND_HALF_EVEN), 1e-6f);
        assertEquals(1.3f, MathUtils.round(1.26f, 1, BigDecimal.ROUND_HALF_EVEN), 1e-6f);

        // ROUND_UNNECESSARY
        assertEquals(1.2f, MathUtils.round(1.2f, 1, BigDecimal.ROUND_UNNECESSARY), 1e-6f);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckIntPositiveOverflow() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckIntNegativeOverflow() {
        MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckLongPositiveOverflow() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckLongNegativeOverflow() {
        MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckIntPositiveOverflow() {
        MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckIntNegativeOverflow() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckLongPositiveOverflow() {
        MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckLongMinValSubtractedFromNonNegative() {
        // b == Long.MIN_VALUE and a >= 0
        MathUtils.subAndCheck(0L, Long.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckIntPositiveOverflow() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckIntNegativeOverflow() {
        MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckLongPositiveBothPositiveOverflow() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckLongPositiveBothNegativeOverflow() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckLongNegativeMixedSignOverflow() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientNegativeN() {
        MathUtils.binomialCoefficient(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientKGreaterThanN() {
        MathUtils.binomialCoefficient(2, 3);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientLogNegativeN() {
        MathUtils.binomialCoefficientLog(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientLogKGreaterThanN() {
        MathUtils.binomialCoefficientLog(2, 3);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testBinomialCoefficientOverflow() {
        MathUtils.binomialCoefficient(67, 30);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactorialNegative() {
        MathUtils.factorial(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactorialDoubleNegative() {
        MathUtils.factorialDouble(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactorialLogNegative() {
        MathUtils.factorialLog(-1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testFactorialOverflow() {
        MathUtils.factorial(21);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGcdOverflow2Pow31() {
        MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testLcmOverflow() {
        MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testRoundFloatUnnecessaryInexact() {
        MathUtils.round(1.234f, 1, BigDecimal.ROUND_UNNECESSARY);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRoundFloatInvalidMethod() {
        MathUtils.round(1.234f, 1, -999);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrivateConstructor() throws Exception {
        Constructor<MathUtils> constructor = MathUtils.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        MathUtils instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test(timeout = 4000)
    public void testPublicConstants() {
        assertEquals(0x1.0p-53, MathUtils.EPSILON, 0.0);
        assertEquals(0x1.0p-1022, MathUtils.SAFE_MIN, 0.0);
    }
}