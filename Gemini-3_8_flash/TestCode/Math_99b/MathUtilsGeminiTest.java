/* [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target: org.apache.commons.math.util.MathUtils
 * Defect Ground Truth (Defects4J):
 *  1. gcd(Integer.MIN_VALUE, 0) and gcd(0, Integer.MIN_VALUE) do not throw ArithmeticException
 *     even though the result (2^31) cannot be represented in a 32-bit signed integer.
 *     Math.abs(Integer.MIN_VALUE) returns negative Integer.MIN_VALUE instead of throwing.
 *  2. lcm(Integer.MIN_VALUE, n) / lcm(n, Integer.MIN_VALUE) for power-of-two n (e.g. 1, 2)
 *     does not throw ArithmeticException even though result 2^31 overflows signed 32-bit integer.
 *
 * Decision / Condition Branch Coverage Plan:
 *  - addAndCheck(int, int) & addAndCheck(long, long):
 *      Overflow positive (> MAX), overflow negative (< MIN), boundary (MAX, MIN, 0).
 *  - subAndCheck(int, int) & subAndCheck(long, long):
 *      Positive/negative overflow, b == Long.MIN_VALUE branch (a < 0 vs a >= 0).
 *  - mulAndCheck(int, int) & mulAndCheck(long, long):
 *      Symmetry a > b, a < 0 & b < 0, a < 0 & b > 0, a > 0 & b > 0, zero combinations, overflow paths.
 *  - gcd(int, int) & lcm(int, int):
 *      Zeros (0,0), (u,0), (0,v), negatives, powers of 2 (k loop), t > 0 vs t <= 0,
 *      Integer.MIN_VALUE boundary overflows (DEFECT ZONE).
 *  - binomialCoefficient / Double / Log:
 *      n < k, n < 0, n == k, k == 0, k == 1, k == n-1, k > n/2 symmetry,
 *      n <= 61 naive, 61 < n <= 66 gcd reduction, n > 66 checked multiply overflow,
 *      binomialCoefficientDouble (n < 67, n >= 67, large overflow),
 *      binomialCoefficientLog (n < 67, n < 1030, n >= 1030).
 *  - factorial / Double / Log:
 *      n < 0, n in [0, 20], n > 20 (ArithmeticException for long),
 *      factorialDouble n < 21 vs n >= 21, factorialLog n < 21 vs n >= 21.
 *  - indicator & sign:
 *      byte, short, int, long, float, double for values < 0, == 0, > 0, and NaN.
 *  - equals(double, double), equals(double, double, eps), equals(double[], double[]):
 *      NaN comparisons, exact equals, within eps, outside eps, null arrays, mismatched lengths.
 *  - scalb, nextAfter, cosh, sinh, normalizeAngle, log:
 *      Boundary values (0, NaN, Inf, exponent shifts, mantissa increase/decrease carry/borrow).
 *  - round(double, int, roundingMethod) & round(float, int, roundingMethod):
 *      ROUND_UP, ROUND_DOWN, ROUND_CEILING, ROUND_FLOOR, ROUND_HALF_UP,
 *      ROUND_HALF_DOWN, ROUND_HALF_EVEN, ROUND_UNNECESSARY, invalid roundingMethod.
 * -----------------------------------------------------------------------------------------
 */
package org.apache.commons.math.util;

import java.math.BigDecimal;
import org.junit.Test;
import static org.junit.Assert.*;

public class MathUtilsGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Regressions)
    // =========================================================================

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGcdIntegerMinValueZeroDefect() {
        // Known bug: Math.abs(Integer.MIN_VALUE) returns Integer.MIN_VALUE instead of throwing
        MathUtils.gcd(Integer.MIN_VALUE, 0);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGcdZeroIntegerMinValueDefect() {
        // Known bug: Math.abs(Integer.MIN_VALUE) returns Integer.MIN_VALUE instead of throwing
        MathUtils.gcd(0, Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGcdIntegerMinValueBothDefect() {
        // Both Integer.MIN_VALUE should overflow signed 32-bit positive integer
        MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testLcmIntegerMinValueOneDefect() {
        // Known bug: lcm(MIN_VALUE, 1) has result 2^31 which overflows signed int
        MathUtils.lcm(Integer.MIN_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testLcmOneIntegerMinValueDefect() {
        MathUtils.lcm(1, Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testLcmIntegerMinValuePowerOfTwoDefect() {
        // Power of two results in overflow 2^31
        MathUtils.lcm(Integer.MIN_VALUE, 2);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddAndCheckInt() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(-5, MathUtils.addAndCheck(-2, -3));
        assertEquals(Integer.MAX_VALUE, MathUtils.addAndCheck(Integer.MAX_VALUE - 1, 1));
        assertEquals(Integer.MIN_VALUE, MathUtils.addAndCheck(Integer.MIN_VALUE + 1, -1));
    }

    @Test(timeout = 4000)
    public void testAddAndCheckLong() {
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
        assertEquals(5L, MathUtils.addAndCheck(3L, 2L)); // a > b branch
        assertEquals(-5L, MathUtils.addAndCheck(-2L, -3L));
        assertEquals(-1L, MathUtils.addAndCheck(-3L, 2L)); // opposite signs
        assertEquals(1L, MathUtils.addAndCheck(3L, -2L));  // opposite signs symmetry
        assertEquals(Long.MAX_VALUE, MathUtils.addAndCheck(Long.MAX_VALUE - 10L, 10L));
        assertEquals(Long.MIN_VALUE, MathUtils.addAndCheck(Long.MIN_VALUE + 10L, -10L));
    }

    @Test(timeout = 4000)
    public void testSubAndCheckInt() {
        assertEquals(-1, MathUtils.subAndCheck(2, 3));
        assertEquals(5, MathUtils.subAndCheck(2, -3));
        assertEquals(Integer.MAX_VALUE, MathUtils.subAndCheck(Integer.MAX_VALUE, 0));
        assertEquals(Integer.MIN_VALUE, MathUtils.subAndCheck(Integer.MIN_VALUE, 0));
    }

    @Test(timeout = 4000)
    public void testSubAndCheckLong() {
        assertEquals(-1L, MathUtils.subAndCheck(2L, 3L));
        assertEquals(Long.MIN_VALUE, MathUtils.subAndCheck(Long.MIN_VALUE + 1L, 1L));
        // b == Long.MIN_VALUE branch
        assertEquals(-1L, MathUtils.subAndCheck(-1L + Long.MIN_VALUE, Long.MIN_VALUE));
        assertEquals(0L, MathUtils.subAndCheck(Long.MIN_VALUE, Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testMulAndCheckInt() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(-6, MathUtils.mulAndCheck(-2, 3));
        assertEquals(6, MathUtils.mulAndCheck(-2, -3));
        assertEquals(0, MathUtils.mulAndCheck(0, 5));
        assertEquals(0, MathUtils.mulAndCheck(5, 0));
    }

    @Test(timeout = 4000)
    public void testMulAndCheckLong() {
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        assertEquals(6L, MathUtils.mulAndCheck(3L, 2L)); // a > b symmetry
        assertEquals(6L, MathUtils.mulAndCheck(-3L, -2L)); // a < 0, b < 0
        assertEquals(-6L, MathUtils.mulAndCheck(-2L, 3L)); // a < 0, b > 0
        assertEquals(0L, MathUtils.mulAndCheck(0L, 5L));
        assertEquals(0L, MathUtils.mulAndCheck(5L, 0L));
        assertEquals(0L, MathUtils.mulAndCheck(-5L, 0L)); // a < 0, b == 0
        assertEquals(0L, MathUtils.mulAndCheck(0L, -5L));
    }

    @Test(timeout = 4000)
    public void testGcdStandardCases() {
        assertEquals(1, MathUtils.gcd(1, 1));
        assertEquals(6, MathUtils.gcd(12, 18));
        assertEquals(6, MathUtils.gcd(-12, 18));
        assertEquals(6, MathUtils.gcd(12, -18));
        assertEquals(6, MathUtils.gcd(-12, -18));
        assertEquals(5, MathUtils.gcd(0, 5));
        assertEquals(5, MathUtils.gcd(5, 0));
        assertEquals(0, MathUtils.gcd(0, 0));
        assertEquals(1, MathUtils.gcd(17, 31));
        assertEquals(8, MathUtils.gcd(24, 16));
        assertEquals(3, MathUtils.gcd(9, 6));
    }

    @Test(timeout = 4000)
    public void testLcmStandardCases() {
        assertEquals(0, MathUtils.lcm(0, 5));
        assertEquals(0, MathUtils.lcm(5, 0));
        assertEquals(36, MathUtils.lcm(12, 18));
        assertEquals(36, MathUtils.lcm(-12, 18));
        assertEquals(36, MathUtils.lcm(12, -18));
        assertEquals(36, MathUtils.lcm(-12, -18));
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficient() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 3)); // k > n/2 symmetry

        // n <= 61 naive loop
        assertEquals(252L, MathUtils.binomialCoefficient(10, 5));

        // 61 < n <= 66 branch
        long coeff64 = MathUtils.binomialCoefficient(64, 3);
        assertEquals(41664L, coeff64);

        // n > 66 branch without overflow
        long coeff67 = MathUtils.binomialCoefficient(67, 2);
        assertEquals(2211L, coeff67);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientDoubleAndLog() {
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 1e-10);
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 5), 1e-10);
        assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 1), 1e-10);
        assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 4), 1e-10);
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-10);
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 3), 1e-10);

        // n >= 67 for double
        double coeff70 = MathUtils.binomialCoefficientDouble(70, 4);
        assertEquals(916895.0, coeff70, 1.0);

        // binomialCoefficientLog
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-10);
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 5), 1e-10);
        assertEquals(Math.log(5.0), MathUtils.binomialCoefficientLog(5, 1), 1e-10);
        assertEquals(Math.log(5.0), MathUtils.binomialCoefficientLog(5, 4), 1e-10);
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(5, 2), 1e-10);

        // n in [67, 1030)
        double log100 = MathUtils.binomialCoefficientLog(100, 5);
        assertEquals(Math.log(MathUtils.binomialCoefficientDouble(100, 5)), log100, 1e-7);

        // n >= 1030 (sum of logs path)
        double logLarge = MathUtils.binomialCoefficientLog(1050, 10);
        assertTrue(logLarge > 0.0);
        // k > n/2 symmetry on large log
        double logLargeSym = MathUtils.binomialCoefficientLog(1050, 1040);
        assertEquals(logLarge, logLargeSym, 1e-7);
    }

    @Test(timeout = 4000)
    public void testFactorial() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(2L, MathUtils.factorial(2));
        assertEquals(6L, MathUtils.factorial(3));
        assertEquals(24L, MathUtils.factorial(4));
        assertEquals(2432902008176640000L, MathUtils.factorial(20));

        assertEquals(1.0, MathUtils.factorialDouble(0), 1e-10);
        assertEquals(24.0, MathUtils.factorialDouble(4), 1e-10);
        assertTrue(MathUtils.factorialDouble(25) > 0);

        assertEquals(0.0, MathUtils.factorialLog(0), 1e-10);
        assertEquals(0.0, MathUtils.factorialLog(1), 1e-10);
        assertEquals(Math.log(24.0), MathUtils.factorialLog(4), 1e-10);
        assertTrue(MathUtils.factorialLog(25) > MathUtils.factorialLog(24));
    }

    @Test(timeout = 4000)
    public void testHyperbolicFunctions() {
        assertEquals(1.0, MathUtils.cosh(0.0), 1e-12);
        assertEquals(0.0, MathUtils.sinh(0.0), 1e-12);
        double x = 1.5;
        double coshVal = (Math.exp(x) + Math.exp(-x)) / 2.0;
        double sinhVal = (Math.exp(x) - Math.exp(-x)) / 2.0;
        assertEquals(coshVal, MathUtils.cosh(x), 1e-12);
        assertEquals(sinhVal, MathUtils.sinh(x), 1e-12);
        // Identity: cosh^2(x) - sinh^2(x) = 1
        assertEquals(1.0, MathUtils.cosh(x) * MathUtils.cosh(x) - MathUtils.sinh(x) * MathUtils.sinh(x), 1e-12);
    }

    @Test(timeout = 4000)
    public void testLogBase() {
        assertEquals(3.0, MathUtils.log(2.0, 8.0), 1e-12);
        assertEquals(2.0, MathUtils.log(10.0, 100.0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testNormalizeAngle() {
        double twoPi = 2.0 * Math.PI;
        assertEquals(0.0, MathUtils.normalizeAngle(twoPi, 0.0), 1e-10);
        assertEquals(Math.PI, MathUtils.normalizeAngle(3.0 * Math.PI, Math.PI), 1e-10);
        assertEquals(-Math.PI / 2.0, MathUtils.normalizeAngle(1.5 * Math.PI, 0.0), 1e-10);
    }

    @Test(timeout = 4000)
    public void testScalb() {
        assertEquals(0.0, MathUtils.scalb(0.0, 5), 0.0);
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 5)));
        assertTrue(Double.isInfinite(MathUtils.scalb(Double.POSITIVE_INFINITY, 5)));
        assertTrue(Double.isInfinite(MathUtils.scalb(Double.NEGATIVE_INFINITY, 5)));

        assertEquals(8.0, MathUtils.scalb(2.0, 2), 1e-12);
        assertEquals(0.5, MathUtils.scalb(2.0, -2), 1e-12);
        assertEquals(-8.0, MathUtils.scalb(-2.0, 2), 1e-12);
    }

    @Test(timeout = 4000)
    public void testNextAfter() {
        assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1.0)));
        assertTrue(Double.isInfinite(MathUtils.nextAfter(Double.POSITIVE_INFINITY, 1.0)));
        assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1.0), 0.0);

        double nextUp = MathUtils.nextAfter(1.0, 2.0);
        assertTrue(nextUp > 1.0);
        double nextDown = MathUtils.nextAfter(1.0, 0.0);
        assertTrue(nextDown < 1.0);

        // Test mantissa boundary carry (mantissa == 0x000fffffffffffffL)
        double nearPow2 = Double.longBitsToDouble(0x3ff0000000000000L | 0x000fffffffffffffL);
        double carried = MathUtils.nextAfter(nearPow2, 2.0);
        assertEquals(Double.longBitsToDouble(0x4000000000000000L), carried, 0.0);

        // Test mantissa borrow (mantissa == 0L)
        double pow2 = Double.longBitsToDouble(0x4000000000000000L);
        double borrowed = MathUtils.nextAfter(pow2, 0.0);
        assertEquals(nearPow2, borrowed, 0.0);
    }

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
    // Partition B: Boundary Value Analysis (BVA) & Equals/Hash Contracts
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsDouble() {
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertFalse(MathUtils.equals(1.0, 2.0));
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equals(Double.NaN, 1.0));
        assertFalse(MathUtils.equals(1.0, Double.NaN));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleWithEps() {
        assertTrue(MathUtils.equals(1.0, 1.0, 0.0));
        assertTrue(MathUtils.equals(1.0, 1.05, 0.1));
        assertTrue(MathUtils.equals(1.05, 1.0, 0.1));
        assertFalse(MathUtils.equals(1.0, 1.15, 0.1));
        assertFalse(MathUtils.equals(1.15, 1.0, 0.1));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArray() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
        assertFalse(MathUtils.equals(new double[] { 1.0 }, null));
        assertFalse(MathUtils.equals(null, new double[] { 1.0 }));
        assertFalse(MathUtils.equals(new double[] { 1.0 }, new double[] { 1.0, 2.0 }));

        assertTrue(MathUtils.equals(new double[] { 1.0, Double.NaN }, new double[] { 1.0, Double.NaN }));
        assertFalse(MathUtils.equals(new double[] { 1.0, 2.0 }, new double[] { 1.0, 3.0 }));
    }

    @Test(timeout = 4000)
    public void testHash() {
        assertEquals(Double.valueOf(1.234).hashCode(), MathUtils.hash(1.234));
        double[] arr = new double[] { 1.0, 2.0, 3.0 };
        assertEquals(java.util.Arrays.hashCode(arr), MathUtils.hash(arr));
        assertEquals(java.util.Arrays.hashCode((double[]) null), MathUtils.hash((double[]) null));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckIntOverflowPositive() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckIntOverflowNegative() {
        MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckLongOverflowPositive() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckLongOverflowNegative() {
        MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckIntOverflowPositive() {
        MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckIntOverflowNegative() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckLongOverflowPositive() {
        MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckLongOverflowNegative() {
        MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckLongMinValueMinuendOverflow() {
        MathUtils.subAndCheck(0L, Long.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckIntOverflowPositive() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckIntOverflowNegative() {
        MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckLongOverflowBothPositive() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckLongOverflowBothNegative() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, -2L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckLongOverflowOppositeSigns() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientNegativeN() {
        MathUtils.binomialCoefficient(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientKGreaterThanN() {
        MathUtils.binomialCoefficient(3, 5);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testBinomialCoefficientOverflow() {
        MathUtils.binomialCoefficient(67, 30);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientDoubleNegativeN() {
        MathUtils.binomialCoefficientDouble(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientDoubleKGreaterThanN() {
        MathUtils.binomialCoefficientDouble(3, 5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientLogNegativeN() {
        MathUtils.binomialCoefficientLog(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientLogKGreaterThanN() {
        MathUtils.binomialCoefficientLog(3, 5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactorialNegative() {
        MathUtils.factorial(-1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testFactorialOverflow() {
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
    // Partition E: Rounding Modes & Edge-Cases
    // =========================================================================

    @Test(timeout = 4000)
    public void testRoundDouble() {
        assertEquals(1.23, MathUtils.round(1.234, 2), 1e-10);
        assertEquals(1.24, MathUtils.round(1.235, 2), 1e-10);
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 2), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.round(Double.NEGATIVE_INFINITY, 2), 0.0);
        assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2)));
    }

    @Test(timeout = 4000)
    public void testRoundFloat() {
        assertEquals(1.23f, MathUtils.round(1.234f, 2), 1e-5f);
        assertEquals(1.24f, MathUtils.round(1.235f, 2), 1e-5f);
        assertEquals(Float.POSITIVE_INFINITY, MathUtils.round(Float.POSITIVE_INFINITY, 2), 0.0f);
        assertEquals(Float.NEGATIVE_INFINITY, MathUtils.round(Float.NEGATIVE_INFINITY, 2), 0.0f);
        assertTrue(Float.isNaN(MathUtils.round(Float.NaN, 2)));
    }

    @Test(timeout = 4000)
    public void testRoundFloatAllModes() {
        float pos = 1.25f;
        float neg = -1.25f;

        assertEquals(1.3f, MathUtils.round(pos, 1, BigDecimal.ROUND_UP), 1e-5f);
        assertEquals(-1.3f, MathUtils.round(neg, 1, BigDecimal.ROUND_UP), 1e-5f);

        assertEquals(1.2f, MathUtils.round(pos, 1, BigDecimal.ROUND_DOWN), 1e-5f);
        assertEquals(-1.2f, MathUtils.round(neg, 1, BigDecimal.ROUND_DOWN), 1e-5f);

        assertEquals(1.3f, MathUtils.round(pos, 1, BigDecimal.ROUND_CEILING), 1e-5f);
        assertEquals(-1.2f, MathUtils.round(neg, 1, BigDecimal.ROUND_CEILING), 1e-5f);

        assertEquals(1.2f, MathUtils.round(pos, 1, BigDecimal.ROUND_FLOOR), 1e-5f);
        assertEquals(-1.3f, MathUtils.round(neg, 1, BigDecimal.ROUND_FLOOR), 1e-5f);

        assertEquals(1.3f, MathUtils.round(pos, 1, BigDecimal.ROUND_HALF_UP), 1e-5f);
        assertEquals(1.2f, MathUtils.round(pos, 1, BigDecimal.ROUND_HALF_DOWN), 1e-5f);

        // HALF_EVEN
        assertEquals(1.2f, MathUtils.round(1.25f, 1, BigDecimal.ROUND_HALF_EVEN), 1e-5f);
        assertEquals(1.4f, MathUtils.round(1.35f, 1, BigDecimal.ROUND_HALF_EVEN), 1e-5f);

        // UNNECESSARY (exact)
        assertEquals(1.5f, MathUtils.round(1.5f, 1, BigDecimal.ROUND_UNNECESSARY), 1e-5f);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testRoundFloatUnnecessaryThrows() {
        MathUtils.round(1.25f, 1, BigDecimal.ROUND_UNNECESSARY);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRoundFloatInvalidMode() {
        MathUtils.round(1.25f, 1, 9999);
    }
}