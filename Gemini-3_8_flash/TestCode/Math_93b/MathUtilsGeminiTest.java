package org.apache.commons.math.util;

import java.math.BigDecimal;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.math.util.MathUtils
 *
 * 1. Defect-Targeted Zone:
 *    - factorialDouble(17): In defective versions, factorialDouble(n) computes
 *      floor(exp(factorialLog(n)) + 0.5) for all n >= 0, incurring floating-point
 *      imprecision where 17! returns 3.55687428096001E14 instead of exact 3.55687428096E14.
 *      Direct assertion with 0.0 delta targets and reveals this exact defect.
 *
 * 2. Arithmetic & Overflow Branch Matrix:
 *    - addAndCheck(int, int) & addAndCheck(long, long):
 *      Positive + Positive overflow, Negative + Negative overflow, Mixed signs (safe),
 *      Symmetric delegation (a > b vs a <= b), Long.MIN_VALUE / Long.MAX_VALUE boundaries.
 *    - subAndCheck(int, int) & subAndCheck(long, long):
 *      b == Long.MIN_VALUE (a < 0 vs a >= 0 branches), positive/negative underflow/overflow.
 *    - mulAndCheck(int, int) & mulAndCheck(long, long):
 *      Symmetric case a > b, a < 0 & b < 0, a < 0 & b > 0, b == 0, a > 0 & b > 0, a == 0.
 *      Multiplication by -1, 0, 1, Long.MIN_VALUE, Long.MAX_VALUE.
 *    - gcd(int, int) & lcm(int, int):
 *      u == 0, v == 0, gcd with negative inputs, even/odd binary reductions,
 *      gcd with Integer.MIN_VALUE (overflow when k == 31).
 *
 * 3. Combinatorics:
 *    - binomialCoefficient(n, k), binomialCoefficientDouble(n, k), binomialCoefficientLog(n, k):
 *      n < k (IAE), n < 0 (IAE), n == k, k == 0, k == 1, k == n - 1, standard loop,
 *      large n exceeding Long.MAX_VALUE (ArithmeticException).
 *    - factorial(n), factorialDouble(n), factorialLog(n):
 *      n < 0 (IAE), n == 0, n == 1, n == 20, n > 20 (ArithmeticException for long).
 *
 * 4. Floating Point, Comparisons & Indicators:
 *    - equals(double, double): NaN vs NaN (true), NaN vs val (false), -0.0 vs +0.0, equal vals.
 *    - equals(double[], double[]): both null, one null, mismatched lengths, element-wise equals.
 *    - indicator & sign methods:
 *      byte, short, int, long, float, double (testing >0, ==0, <0, NaN for float/double).
 *    - nextAfter(double, double):
 *      d is NaN, Infinite, 0.0 (+/- direction), mantissa max rollover, mantissa min rollover.
 *    - scalb(double, int):
 *      d == 0, NaN, Infinite, normal exponent shift.
 *    - normalizeAngle(double, double):
 *      interval wrapping around center, positive and negative offsets.
 *
 * 5. Rounding Algorithms:
 *    - round(double, int, method) & round(float, int, method):
 *      ROUND_CEILING, ROUND_DOWN, ROUND_FLOOR, ROUND_HALF_DOWN, ROUND_HALF_EVEN,
 *      ROUND_HALF_UP, ROUND_UNNECESSARY, ROUND_UP, invalid roundingMethod (IAE).
 *      Inexact rounding under ROUND_UNNECESSARY (ArithmeticException).
 *      Infinite and NaN handling in Double rounding.
 */
public class MathUtilsGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (GROUND TRUTH DEFECT)
    // =========================================================================

    /**
     * Defects4J Ground Truth Defect Test:
     * factorialDouble(17) must equal exact 355687428096000.0 (3.55687428096E14).
     * In defective versions, computing via log/exp yields 3.55687428096001E14.
     */
    @Test(timeout = 4000)
    public void testFactorialDoubleDefectAt17() {
        assertEquals("17! exact double representation failed",
                355687428096000.0, MathUtils.factorialDouble(17), 0.0);
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & ARITHMETIC OPERATIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddAndCheckInt() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(-5, MathUtils.addAndCheck(-2, -3));
        assertEquals(1, MathUtils.addAndCheck(3, -2));
        assertEquals(Integer.MAX_VALUE, MathUtils.addAndCheck(Integer.MAX_VALUE - 1, 1));
        assertEquals(Integer.MIN_VALUE, MathUtils.addAndCheck(Integer.MIN_VALUE + 1, -1));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckIntOverflowPositive() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckIntOverflowNegative() {
        MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
    }

    @Test(timeout = 4000)
    public void testAddAndCheckLong() {
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
        assertEquals(5L, MathUtils.addAndCheck(3L, 2L)); // triggers a > b symmetric branch
        assertEquals(-5L, MathUtils.addAndCheck(-3L, -2L));
        assertEquals(-1L, MathUtils.addAndCheck(-3L, 2L)); // a < 0, b >= 0
        assertEquals(Long.MAX_VALUE, MathUtils.addAndCheck(Long.MAX_VALUE - 10L, 10L));
        assertEquals(Long.MIN_VALUE, MathUtils.addAndCheck(Long.MIN_VALUE + 10L, -10L));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckLongOverflowPositive() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckLongOverflowNegative() {
        MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(timeout = 4000)
    public void testSubAndCheckInt() {
        assertEquals(-1, MathUtils.subAndCheck(2, 3));
        assertEquals(1, MathUtils.subAndCheck(3, 2));
        assertEquals(Integer.MAX_VALUE, MathUtils.subAndCheck(Integer.MAX_VALUE - 1, -1));
        assertEquals(Integer.MIN_VALUE, MathUtils.subAndCheck(Integer.MIN_VALUE + 1, 1));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckIntOverflowPositive() {
        MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckIntOverflowNegative() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    @Test(timeout = 4000)
    public void testSubAndCheckLong() {
        assertEquals(1L, MathUtils.subAndCheck(3L, 2L));
        assertEquals(-1L, MathUtils.subAndCheck(2L, 3L));
        // b == Long.MIN_VALUE, a < 0
        assertEquals(0L, MathUtils.subAndCheck(Long.MIN_VALUE, Long.MIN_VALUE));
        assertEquals(1L, MathUtils.subAndCheck(Long.MIN_VALUE + 1L, Long.MIN_VALUE));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckLongOverflowBMinVal() {
        // b == Long.MIN_VALUE, a >= 0 -> overflow
        MathUtils.subAndCheck(0L, Long.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckLongOverflowPositive() {
        MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
    }

    @Test(timeout = 4000)
    public void testMulAndCheckInt() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(-6, MathUtils.mulAndCheck(-2, 3));
        assertEquals(0, MathUtils.mulAndCheck(0, 100));
        assertEquals(Integer.MAX_VALUE, MathUtils.mulAndCheck(Integer.MAX_VALUE, 1));
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
    public void testMulAndCheckLongBranches() {
        assertEquals(12L, MathUtils.mulAndCheck(3L, 4L));
        assertEquals(12L, MathUtils.mulAndCheck(4L, 3L)); // a > b branch
        assertEquals(0L, MathUtils.mulAndCheck(0L, 5L));
        assertEquals(0L, MathUtils.mulAndCheck(5L, 0L));
        assertEquals(0L, MathUtils.mulAndCheck(0L, 0L));
        assertEquals(6L, MathUtils.mulAndCheck(-2L, -3L)); // a < 0, b < 0
        assertEquals(-6L, MathUtils.mulAndCheck(-2L, 3L)); // a < 0, b > 0
        assertEquals(0L, MathUtils.mulAndCheck(-2L, 0L)); // a < 0, b == 0
        assertEquals(Long.MAX_VALUE, MathUtils.mulAndCheck(Long.MAX_VALUE, 1L));
        assertEquals(Long.MIN_VALUE, MathUtils.mulAndCheck(Long.MIN_VALUE, 1L));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckLongOverflowNegNeg() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckLongOverflowNegPos() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckLongOverflowPosPos() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    @Test(timeout = 4000)
    public void testGcdAndLcm() {
        assertEquals(6, MathUtils.gcd(12, 18));
        assertEquals(6, MathUtils.gcd(-12, 18));
        assertEquals(6, MathUtils.gcd(12, -18));
        assertEquals(6, MathUtils.gcd(-12, -18));
        assertEquals(5, MathUtils.gcd(0, 5));
        assertEquals(5, MathUtils.gcd(5, 0));
        assertEquals(0, MathUtils.gcd(0, 0));
        assertEquals(36, MathUtils.lcm(12, 18));
        assertEquals(36, MathUtils.lcm(-12, 18));
        assertEquals(0, MathUtils.lcm(0, 5));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGcdOverflow() {
        MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testLcmOverflow() {
        MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (COMBINATORICS & FACTORIALS)
    // =========================================================================

    @Test(timeout = 4000)
    public void testBinomialCoefficientBranches() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 3));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientNLessThanK() {
        MathUtils.binomialCoefficient(3, 5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientNegativeN() {
        MathUtils.binomialCoefficient(-1, 0);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testBinomialCoefficientOverflow() {
        MathUtils.binomialCoefficient(67, 33);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientDoubleAndLog() {
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 1e-10);
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 5), 1e-10);
        assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 1), 1e-10);
        assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 4), 1e-10);
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-10);

        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-10);
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 5), 1e-10);
        assertEquals(Math.log(5), MathUtils.binomialCoefficientLog(5, 1), 1e-10);
        assertEquals(Math.log(5), MathUtils.binomialCoefficientLog(5, 4), 1e-10);
        assertEquals(Math.log(10), MathUtils.binomialCoefficientLog(5, 2), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientLogNLessThanK() {
        MathUtils.binomialCoefficientLog(2, 4);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientLogNegativeN() {
        MathUtils.binomialCoefficientLog(-2, 0);
    }

    @Test(timeout = 4000)
    public void testFactorial() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(2L, MathUtils.factorial(2));
        assertEquals(6L, MathUtils.factorial(3));
        assertEquals(2432902008176640000L, MathUtils.factorial(20));

        assertEquals(1.0, MathUtils.factorialDouble(0), 1e-10);
        assertEquals(1.0, MathUtils.factorialDouble(1), 1e-10);
        assertEquals(2.0, MathUtils.factorialDouble(2), 1e-10);

        assertEquals(0.0, MathUtils.factorialLog(0), 1e-10);
        assertEquals(0.0, MathUtils.factorialLog(1), 1e-10);
        assertEquals(Math.log(2.0), MathUtils.factorialLog(2), 1e-10);
        assertEquals(Math.log(6.0), MathUtils.factorialLog(3), 1e-10);
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
    // PARTITION D: FLOATING POINT OPERATIONS, INDICATORS, SIGNS, EQUALS
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsDouble() {
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equals(Double.NaN, 1.0));
        assertFalse(MathUtils.equals(1.0, Double.NaN));
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertFalse(MathUtils.equals(1.0, 2.0));
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
    public void testIndicator() {
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
    public void testHyperbolicFunctionsAndLog() {
        assertEquals(1.0, MathUtils.cosh(0.0), 1e-10);
        assertEquals(Math.cosh(1.5), MathUtils.cosh(1.5), 1e-10);

        assertEquals(0.0, MathUtils.sinh(0.0), 1e-10);
        assertEquals(Math.sinh(1.5), MathUtils.sinh(1.5), 1e-10);

        assertEquals(3.0, MathUtils.log(2.0, 8.0), 1e-10);
        assertEquals(2.0, MathUtils.log(10.0, 100.0), 1e-10);
    }

    @Test(timeout = 4000)
    public void testHash() {
        assertEquals(Double.valueOf(1.23).hashCode(), MathUtils.hash(1.23));
        double[] arr = new double[]{1.0, 2.0, 3.0};
        assertEquals(java.util.Arrays.hashCode(arr), MathUtils.hash(arr));
        assertEquals(0, MathUtils.hash((double[]) null));
    }

    @Test(timeout = 4000)
    public void testNormalizeAngle() {
        assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-10);
        assertEquals(0.0, MathUtils.normalizeAngle(2 * Math.PI, 0.0), 1e-10);
        assertEquals(Math.PI, MathUtils.normalizeAngle(3 * Math.PI, 0.0), 1e-10);
        assertEquals(Math.PI / 2, MathUtils.normalizeAngle(5 * Math.PI / 2, 0.0), 1e-10);
    }

    @Test(timeout = 4000)
    public void testScalb() {
        assertEquals(0.0, MathUtils.scalb(0.0, 5), 0.0);
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 5)));
        assertTrue(Double.isInfinite(MathUtils.scalb(Double.POSITIVE_INFINITY, 5)));
        assertEquals(8.0, MathUtils.scalb(1.0, 3), 1e-10);
        assertEquals(0.25, MathUtils.scalb(1.0, -2), 1e-10);
    }

    @Test(timeout = 4000)
    public void testNextAfter() {
        assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1.0)));
        assertTrue(Double.isInfinite(MathUtils.nextAfter(Double.POSITIVE_INFINITY, 1.0)));
        assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1.0), 0.0);

        // Increase mantissa: d * (direction - d) >= 0
        // positive d, direction > d
        double d1 = 1.0;
        double next1 = MathUtils.nextAfter(d1, 2.0);
        assertTrue(next1 > d1);

        // mantissa rollover to exponent increase
        // 0x1.fffffffffffffP0
        double maxMantissa = Double.longBitsToDouble(0x3fefffffffffffffL);
        double afterRollover = MathUtils.nextAfter(maxMantissa, 2.0);
        assertEquals(2.0, afterRollover, 0.0);

        // Decrease mantissa: d * (direction - d) < 0
        // positive d, direction < d
        double next2 = MathUtils.nextAfter(d1, 0.0);
        assertTrue(next2 < d1);

        // mantissa 0 rollover to exponent decrease
        double powerOfTwo = 2.0;
        double beforePowerOfTwo = MathUtils.nextAfter(powerOfTwo, 1.0);
        assertEquals(maxMantissa, beforePowerOfTwo, 0.0);
    }

    // =========================================================================
    // PARTITION E: ROUNDING MODES & EXCEPTIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testRoundDoubleScale() {
        assertEquals(1.23, MathUtils.round(1.2345, 2), 0.0);
        assertEquals(1.24, MathUtils.round(1.2355, 2), 0.0);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleSpecialValues() {
        assertTrue(Double.isInfinite(MathUtils.round(Double.POSITIVE_INFINITY, 2, BigDecimal.ROUND_UP)));
        assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2, BigDecimal.ROUND_UP)));
    }

    @Test(timeout = 4000)
    public void testRoundFloatModes() {
        // ROUND_CEILING
        assertEquals(2.0f, MathUtils.round(1.1f, 0, BigDecimal.ROUND_CEILING), 0.0f);
        assertEquals(-1.0f, MathUtils.round(-1.9f, 0, BigDecimal.ROUND_CEILING), 0.0f);

        // ROUND_DOWN
        assertEquals(1.0f, MathUtils.round(1.9f, 0, BigDecimal.ROUND_DOWN), 0.0f);
        assertEquals(-1.0f, MathUtils.round(-1.9f, 0, BigDecimal.ROUND_DOWN), 0.0f);

        // ROUND_FLOOR
        assertEquals(1.0f, MathUtils.round(1.9f, 0, BigDecimal.ROUND_FLOOR), 0.0f);
        assertEquals(-2.0f, MathUtils.round(-1.1f, 0, BigDecimal.ROUND_FLOOR), 0.0f);

        // ROUND_HALF_DOWN
        assertEquals(1.0f, MathUtils.round(1.5f, 0, BigDecimal.ROUND_HALF_DOWN), 0.0f);
        assertEquals(2.0f, MathUtils.round(1.51f, 0, BigDecimal.ROUND_HALF_DOWN), 0.0f);

        // ROUND_HALF_EVEN
        assertEquals(2.0f, MathUtils.round(1.5f, 0, BigDecimal.ROUND_HALF_EVEN), 0.0f); // odd -> round to even (2)
        assertEquals(2.0f, MathUtils.round(2.5f, 0, BigDecimal.ROUND_HALF_EVEN), 0.0f); // even -> keep even (2)
        assertEquals(2.0f, MathUtils.round(2.4f, 0, BigDecimal.ROUND_HALF_EVEN), 0.0f);
        assertEquals(3.0f, MathUtils.round(2.6f, 0, BigDecimal.ROUND_HALF_EVEN), 0.0f);

        // ROUND_HALF_UP
        assertEquals(2.0f, MathUtils.round(1.5f, 0, BigDecimal.ROUND_HALF_UP), 0.0f);
        assertEquals(1.0f, MathUtils.round(1.49f, 0, BigDecimal.ROUND_HALF_UP), 0.0f);
        assertEquals(1.23f, MathUtils.round(1.234f, 2), 0.0f);

        // ROUND_UP
        assertEquals(2.0f, MathUtils.round(1.1f, 0, BigDecimal.ROUND_UP), 0.0f);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testRoundUnnecessaryInexact() {
        MathUtils.round(1.234f, 2, BigDecimal.ROUND_UNNECESSARY);
    }

    @Test(timeout = 4000)
    public void testRoundUnnecessaryExact() {
        assertEquals(1.25f, MathUtils.round(1.25f, 2, BigDecimal.ROUND_UNNECESSARY), 0.0f);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRoundInvalidRoundingMode() {
        MathUtils.round(1.234f, 2, 9999);
    }
}