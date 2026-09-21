package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for MathUtils.
 * Targets line/branch coverage and known defects in gcd/lcm.
 *
 * [Branch & Defect Analysis Matrix]
 * - addAndCheck(int): overflow detection (MIN_VALUE, MAX_VALUE boundaries)
 * - addAndCheck(long): symmetric reduction, negative overflow, positive overflow, opposite sign
 * - binomialCoefficient: n<k, n<0, n==k, k==0, k==1, k==n-1, symmetry, n<=61, n<=66, n>66 overflow
 * - binomialCoefficientDouble: similar branches, double overflow, floor rounding
 * - binomialCoefficientLog: n<67 exact, n<1030 double, symmetry, sum logs
 * - cosh: exp(x)+exp(-x)/2
 * - equals(double,double): NaN equality, exact equality
 * - equals(double,double,double): tolerance branches (x<y, x>y)
 * - equals(double[],double[]): null handling, length mismatch, element equality
 * - factorial: n<0, n>20 overflow, n<=20 exact
 * - factorialDouble: n<0, n<21 exact, n>=21 via factorialLog
 * - factorialLog: n<0, n<21 exact, n>=21 sum logs
 * - gcd: zero handling, negative conversion, power-of-2 extraction, overflow (2^31), Stein's algorithm
 * - hash(double): Double.hashCode
 * - hash(double[]): Arrays.hashCode
 * - indicator: byte, double, float, int, long, short (sign branches, NaN handling)
 * - lcm: zero, gcd overflow, multiplication overflow
 * - log(base,x): division by log(base)
 * - mulAndCheck(int): overflow detection
 * - mulAndCheck(long): symmetric, negative/negative, negative/positive, positive/positive, zero
 * - nextAfter: NaN, Infinity, zero, normal increase/decrease, mantissa overflow/underflow
 * - normalizeAngle: floor-based normalization
 * - round(double,int): BigDecimal rounding
 * - round(double,int,int): NumberFormatException catch (infinite, NaN)
 * - round(float,int,int): sign, factor, roundUnscaled
 * - roundUnscaled: all rounding modes (CEILING, DOWN, FLOOR, HALF_DOWN, HALF_EVEN, HALF_UP, UNNECESSARY, UP)
 * - scalb: zero, NaN, Infinity, normal exponent shift
 * - sign: byte, double, float, int, long, short (zero, positive, negative, NaN)
 * - sinh: (exp(x)-exp(-x))/2
 * - subAndCheck(int): overflow detection
 * - subAndCheck(long): MIN_VALUE special case, additive inverse
 *
 * Known defect: gcd(Integer.MIN_VALUE, Integer.MIN_VALUE) and lcm(Integer.MIN_VALUE, ...) should throw ArithmeticException.
 */
public class MathUtilsDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testAddAndCheckIntNormal() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(-1, MathUtils.addAndCheck(2, -3));
        assertEquals(Integer.MAX_VALUE, MathUtils.addAndCheck(Integer.MAX_VALUE - 1, 1));
        assertEquals(Integer.MIN_VALUE, MathUtils.addAndCheck(Integer.MIN_VALUE + 1, -1));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testAddAndCheckIntOverflowPositive() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testAddAndCheckIntOverflowNegative() {
        MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
    }

    @Test(timeout = 4000)
    public void testAddAndCheckLongNormal() {
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
        assertEquals(-1L, MathUtils.addAndCheck(2L, -3L));
        assertEquals(Long.MAX_VALUE, MathUtils.addAndCheck(Long.MAX_VALUE - 1, 1));
        assertEquals(Long.MIN_VALUE, MathUtils.addAndCheck(Long.MIN_VALUE + 1, -1));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflowPositive() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflowNegative() {
        MathUtils.addAndCheck(Long.MIN_VALUE, -1);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientNormal() {
        assertEquals(1, MathUtils.binomialCoefficient(5, 0));
        assertEquals(5, MathUtils.binomialCoefficient(5, 1));
        assertEquals(10, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10, MathUtils.binomialCoefficient(5, 3));
        assertEquals(5, MathUtils.binomialCoefficient(5, 4));
        assertEquals(1, MathUtils.binomialCoefficient(5, 5));
        assertEquals(1, MathUtils.binomialCoefficient(0, 0));
        assertEquals(0, MathUtils.binomialCoefficient(0, 1)); // n<k -> exception
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBinomialCoefficientNLessThanK() {
        MathUtils.binomialCoefficient(2, 5);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBinomialCoefficientNNegative() {
        MathUtils.binomialCoefficient(-1, 0);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientLargeN() {
        // n=66, k=33 should be exact
        long result = MathUtils.binomialCoefficient(66, 33);
        assertTrue(result > 0);
        // n=67, k=30 should overflow (n>66)
        try {
            MathUtils.binomialCoefficient(67, 30);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientDouble() {
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 1e-12);
        assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 1), 1e-12);
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-12);
        assertEquals(252.0, MathUtils.binomialCoefficientDouble(10, 5), 1e-12);
        // n=1029, k=500 should not overflow double
        double d = MathUtils.binomialCoefficientDouble(1029, 500);
        assertTrue(Double.isFinite(d));
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientLog() {
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-12);
        assertEquals(Math.log(5), MathUtils.binomialCoefficientLog(5, 1), 1e-12);
        assertEquals(Math.log(10), MathUtils.binomialCoefficientLog(5, 2), 1e-12);
        // n=1000, k=500 should be computed via sum logs
        double logVal = MathUtils.binomialCoefficientLog(1000, 500);
        assertTrue(logVal > 0);
    }

    @Test(timeout = 4000)
    public void testCosh() {
        assertEquals(1.0, MathUtils.cosh(0), 1e-12);
        assertEquals(Math.cosh(1), MathUtils.cosh(1), 1e-12);
        assertEquals(Math.cosh(-1), MathUtils.cosh(-1), 1e-12);
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleDouble() {
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equals(1.0, 2.0));
        assertFalse(MathUtils.equals(1.0, Double.NaN));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleDoubleEps() {
        assertTrue(MathUtils.equals(1.0, 1.0, 0.0));
        assertTrue(MathUtils.equals(1.0, 1.0001, 0.001));
        assertFalse(MathUtils.equals(1.0, 1.1, 0.05));
        // boundary: x < y and x+eps >= y
        assertTrue(MathUtils.equals(1.0, 1.05, 0.05));
        // boundary: x > y and x <= y+eps
        assertTrue(MathUtils.equals(1.05, 1.0, 0.05));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArray() {
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {1.0, 2.0, 3.0};
        assertTrue(MathUtils.equals(a, b));
        assertFalse(MathUtils.equals(a, new double[]{1.0, 2.0}));
        assertFalse(MathUtils.equals(a, new double[]{1.0, 2.0, 4.0}));
        assertTrue(MathUtils.equals(null, null));
        assertFalse(MathUtils.equals(null, a));
        assertFalse(MathUtils.equals(a, null));
    }

    @Test(timeout = 4000)
    public void testFactorial() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(2L, MathUtils.factorial(2));
        assertEquals(6L, MathUtils.factorial(3));
        assertEquals(120L, MathUtils.factorial(5));
        assertEquals(2432902008176640000L, MathUtils.factorial(20));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFactorialNegative() {
        MathUtils.factorial(-1);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testFactorialOverflow() {
        MathUtils.factorial(21);
    }

    @Test(timeout = 4000)
    public void testFactorialDouble() {
        assertEquals(1.0, MathUtils.factorialDouble(0), 1e-12);
        assertEquals(120.0, MathUtils.factorialDouble(5), 1e-12);
        assertEquals(2.43290200817664e18, MathUtils.factorialDouble(20), 1e12);
        // n=170 should not overflow
        double d = MathUtils.factorialDouble(170);
        assertTrue(Double.isFinite(d));
        // n=171 should overflow to infinity
        double inf = MathUtils.factorialDouble(171);
        assertEquals(Double.POSITIVE_INFINITY, inf, 0.0);
    }

    @Test(timeout = 4000)
    public void testFactorialLog() {
        assertEquals(0.0, MathUtils.factorialLog(0), 1e-12);
        assertEquals(Math.log(120), MathUtils.factorialLog(5), 1e-12);
        assertTrue(MathUtils.factorialLog(100) > 0);
    }

    @Test(timeout = 4000)
    public void testGcdNormal() {
        assertEquals(6, MathUtils.gcd(12, 18));
        assertEquals(1, MathUtils.gcd(17, 13));
        assertEquals(0, MathUtils.gcd(0, 0));
        assertEquals(5, MathUtils.gcd(0, 5));
        assertEquals(5, MathUtils.gcd(5, 0));
        assertEquals(2, MathUtils.gcd(-4, 6));
        assertEquals(2, MathUtils.gcd(4, -6));
        assertEquals(2, MathUtils.gcd(-4, -6));
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testGcdBoundary() {
        // gcd(1,1) = 1
        assertEquals(1, MathUtils.gcd(1, 1));
        // gcd(Integer.MAX_VALUE, Integer.MAX_VALUE) = Integer.MAX_VALUE
        assertEquals(Integer.MAX_VALUE, MathUtils.gcd(Integer.MAX_VALUE, Integer.MAX_VALUE));
        // gcd(Integer.MIN_VALUE+1, 1) = 1
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE + 1, 1));
    }

    @Test(timeout = 4000)
    public void testHashDouble() {
        assertEquals(new Double(1.0).hashCode(), MathUtils.hash(1.0));
        assertEquals(new Double(Double.NaN).hashCode(), MathUtils.hash(Double.NaN));
    }

    @Test(timeout = 4000)
    public void testHashDoubleArray() {
        double[] arr = {1.0, 2.0};
        assertEquals(java.util.Arrays.hashCode(arr), MathUtils.hash(arr));
        assertEquals(0, MathUtils.hash(null));
    }

    @Test(timeout = 4000)
    public void testIndicator() {
        assertEquals((byte)1, MathUtils.indicator((byte)5));
        assertEquals((byte)-1, MathUtils.indicator((byte)-3));
        assertEquals((byte)1, MathUtils.indicator((byte)0));
        assertEquals(1.0, MathUtils.indicator(5.0), 0.0);
        assertEquals(-1.0, MathUtils.indicator(-3.0), 0.0);
        assertEquals(1.0, MathUtils.indicator(0.0), 0.0);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));
        assertEquals(1.0f, MathUtils.indicator(5.0f), 0.0f);
        assertEquals(-1.0f, MathUtils.indicator(-3.0f), 0.0f);
        assertEquals(1.0f, MathUtils.indicator(0.0f), 0.0f);
        assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));
        assertEquals(1, MathUtils.indicator(5));
        assertEquals(-1, MathUtils.indicator(-3));
        assertEquals(1, MathUtils.indicator(0));
        assertEquals(1L, MathUtils.indicator(5L));
        assertEquals(-1L, MathUtils.indicator(-3L));
        assertEquals(1L, MathUtils.indicator(0L));
        assertEquals((short)1, MathUtils.indicator((short)5));
        assertEquals((short)-1, MathUtils.indicator((short)-3));
        assertEquals((short)1, MathUtils.indicator((short)0));
    }

    @Test(timeout = 4000)
    public void testLcmNormal() {
        assertEquals(36, MathUtils.lcm(12, 18));
        assertEquals(0, MathUtils.lcm(0, 5));
        assertEquals(0, MathUtils.lcm(5, 0));
        assertEquals(12, MathUtils.lcm(3, 4));
    }

    @Test(timeout = 4000)
    public void testLog() {
        assertEquals(1.0, MathUtils.log(10, 10), 1e-12);
        assertEquals(2.0, MathUtils.log(2, 4), 1e-12);
        assertEquals(0.0, MathUtils.log(10, 1), 1e-12);
    }

    @Test(timeout = 4000)
    public void testMulAndCheckIntNormal() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(-6, MathUtils.mulAndCheck(2, -3));
        assertEquals(0, MathUtils.mulAndCheck(0, 100));
        assertEquals(Integer.MAX_VALUE, MathUtils.mulAndCheck(1, Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, MathUtils.mulAndCheck(1, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testMulAndCheckIntOverflowPositive() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testMulAndCheckIntOverflowNegative() {
        MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
    }

    @Test(timeout = 4000)
    public void testMulAndCheckLongNormal() {
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        assertEquals(-6L, MathUtils.mulAndCheck(2L, -3L));
        assertEquals(0L, MathUtils.mulAndCheck(0L, 100L));
        assertEquals(Long.MAX_VALUE, MathUtils.mulAndCheck(1L, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, MathUtils.mulAndCheck(1L, Long.MIN_VALUE));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowPositive() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowNegative() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
    }

    @Test(timeout = 4000)
    public void testNextAfter() {
        assertEquals(Double.NaN, MathUtils.nextAfter(Double.NaN, 1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.nextAfter(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.nextAfter(Double.NEGATIVE_INFINITY, 1.0), 0.0);
        assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1.0), 0.0);
        double d = 1.0;
        double next = MathUtils.nextAfter(d, 2.0);
        assertTrue(next > d);
        double prev = MathUtils.nextAfter(d, 0.0);
        assertTrue(prev < d);
    }

    @Test(timeout = 4000)
    public void testNormalizeAngle() {
        double a = 10 * Math.PI;
        double center = 0.0;
        double normalized = MathUtils.normalizeAngle(a, center);
        assertTrue(normalized >= -Math.PI && normalized <= Math.PI);
        assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-12);
        assertEquals(Math.PI, MathUtils.normalizeAngle(Math.PI, 0.0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleInt() {
        assertEquals(2.56, MathUtils.round(2.556, 2), 1e-12);
        assertEquals(-2.56, MathUtils.round(-2.556, 2), 1e-12);
        assertEquals(2.0, MathUtils.round(2.0, 0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleIntRoundingMethod() {
        assertEquals(2.56, MathUtils.round(2.556, 2, java.math.BigDecimal.ROUND_HALF_UP), 1e-12);
        assertEquals(2.55, MathUtils.round(2.554, 2, java.math.BigDecimal.ROUND_HALF_DOWN), 1e-12);
        assertEquals(2.56, MathUtils.round(2.555, 2, java.math.BigDecimal.ROUND_HALF_EVEN), 1e-12);
        assertEquals(2.56, MathUtils.round(2.555, 2, java.math.BigDecimal.ROUND_UP), 1e-12);
        assertEquals(2.55, MathUtils.round(2.555, 2, java.math.BigDecimal.ROUND_DOWN), 1e-12);
        // Infinity case
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 2, java.math.BigDecimal.ROUND_HALF_UP), 0.0);
        // NaN case
        assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2, java.math.BigDecimal.ROUND_HALF_UP)));
    }

    @Test(timeout = 4000)
    public void testRoundFloatInt() {
        assertEquals(2.56f, MathUtils.round(2.556f, 2), 1e-6f);
        assertEquals(-2.56f, MathUtils.round(-2.556f, 2), 1e-6f);
    }

    @Test(timeout = 4000)
    public void testRoundFloatIntRoundingMethod() {
        assertEquals(2.56f, MathUtils.round(2.556f, 2, java.math.BigDecimal.ROUND_HALF_UP), 1e-6f);
        assertEquals(2.55f, MathUtils.round(2.554f, 2, java.math.BigDecimal.ROUND_HALF_DOWN), 1e-6f);
    }

    @Test(timeout = 4000)
    public void testScalb() {
        assertEquals(0.0, MathUtils.scalb(0.0, 5), 0.0);
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 5)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.scalb(Double.POSITIVE_INFINITY, 5), 0.0);
        assertEquals(4.0, MathUtils.scalb(1.0, 2), 1e-12);
        assertEquals(0.5, MathUtils.scalb(1.0, -1), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSign() {
        assertEquals((byte)1, MathUtils.sign((byte)5));
        assertEquals((byte)0, MathUtils.sign((byte)0));
        assertEquals((byte)-1, MathUtils.sign((byte)-3));
        assertEquals(1.0, MathUtils.sign(5.0), 0.0);
        assertEquals(0.0, MathUtils.sign(0.0), 0.0);
        assertEquals(-1.0, MathUtils.sign(-3.0), 0.0);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));
        assertEquals(1.0f, MathUtils.sign(5.0f), 0.0f);
        assertEquals(0.0f, MathUtils.sign(0.0f), 0.0f);
        assertEquals(-1.0f, MathUtils.sign(-3.0f), 0.0f);
        assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));
        assertEquals(1, MathUtils.sign(5));
        assertEquals(0, MathUtils.sign(0));
        assertEquals(-1, MathUtils.sign(-3));
        assertEquals(1L, MathUtils.sign(5L));
        assertEquals(0L, MathUtils.sign(0L));
        assertEquals(-1L, MathUtils.sign(-3L));
        assertEquals((short)1, MathUtils.sign((short)5));
        assertEquals((short)0, MathUtils.sign((short)0));
        assertEquals((short)-1, MathUtils.sign((short)-3));
    }

    @Test(timeout = 4000)
    public void testSinh() {
        assertEquals(0.0, MathUtils.sinh(0), 1e-12);
        assertEquals(Math.sinh(1), MathUtils.sinh(1), 1e-12);
        assertEquals(Math.sinh(-1), MathUtils.sinh(-1), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSubAndCheckIntNormal() {
        assertEquals(2, MathUtils.subAndCheck(5, 3));
        assertEquals(-2, MathUtils.subAndCheck(3, 5));
        assertEquals(Integer.MAX_VALUE, MathUtils.subAndCheck(Integer.MAX_VALUE, 0));
        assertEquals(Integer.MIN_VALUE, MathUtils.subAndCheck(Integer.MIN_VALUE, 0));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testSubAndCheckIntOverflowPositive() {
        MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testSubAndCheckIntOverflowNegative() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    @Test(timeout = 4000)
    public void testSubAndCheckLongNormal() {
        assertEquals(2L, MathUtils.subAndCheck(5L, 3L));
        assertEquals(-2L, MathUtils.subAndCheck(3L, 5L));
        assertEquals(Long.MAX_VALUE, MathUtils.subAndCheck(Long.MAX_VALUE, 0L));
        assertEquals(Long.MIN_VALUE, MathUtils.subAndCheck(Long.MIN_VALUE, 0L));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflowPositive() {
        MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflowNegative() {
        MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Known defect: gcd(Integer.MIN_VALUE, Integer.MIN_VALUE) should throw ArithmeticException
     * because result would be 2^31 which is too large for int.
     */
    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testGcdMinValueMinValue() {
        MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    /**
     * Known defect: gcd(Integer.MIN_VALUE, 0) should throw ArithmeticException.
     */
    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testGcdMinValueZero() {
        MathUtils.gcd(Integer.MIN_VALUE, 0);
    }

    /**
     * Known defect: gcd(0, Integer.MIN_VALUE) should throw ArithmeticException.
     */
    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testGcdZeroMinValue() {
        MathUtils.gcd(0, Integer.MIN_VALUE);
    }

    /**
     * Known defect: lcm(Integer.MIN_VALUE, 1) should throw ArithmeticException
     * because result would be 2^31.
     */
    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testLcmMinValueOne() {
        MathUtils.lcm(Integer.MIN_VALUE, 1);
    }

    /**
     * Known defect: lcm(1, Integer.MIN_VALUE) should throw ArithmeticException.
     */
    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testLcmOneMinValue() {
        MathUtils.lcm(1, Integer.MIN_VALUE);
    }

    /**
     * Additional lcm defect: lcm(Integer.MIN_VALUE, 2) should throw ArithmeticException.
     */
    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testLcmMinValueTwo() {
        MathUtils.lcm(Integer.MIN_VALUE, 2);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBinomialCoefficientDoubleNLessThanK() {
        MathUtils.binomialCoefficientDouble(2, 5);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBinomialCoefficientDoubleNNegative() {
        MathUtils.binomialCoefficientDouble(-1, 0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogNLessThanK() {
        MathUtils.binomialCoefficientLog(2, 5);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBinomialCoefficientLogNNegative() {
        MathUtils.binomialCoefficientLog(-1, 0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFactorialDoubleNegative() {
        MathUtils.factorialDouble(-1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFactorialLogNegative() {
        MathUtils.factorialLog(-1);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testMulAndCheckIntOverflowMinValue() {
        MathUtils.mulAndCheck(Integer.MIN_VALUE, -1);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowMinValue() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, -1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRoundUnnecessaryException() {
        // This will trigger ROUND_UNNECESSARY via round(double,int,int)
        MathUtils.round(2.5, 0, java.math.BigDecimal.ROUND_UNNECESSARY);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRoundInvalidRoundingMethod() {
        MathUtils.round(1.0, 0, 999);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    // (No mutable state, but we can test static utility consistency)

    @Test(timeout = 4000)
    public void testConstants() {
        assertTrue(MathUtils.EPSILON > 0);
        assertTrue(MathUtils.SAFE_MIN > 0);
        assertEquals(2 * Math.PI, MathUtils.TWO_PI, 1e-12);
    }

    @Test(timeout = 4000)
    public void testPrivateConstructor() throws Exception {
        // Ensure constructor is private and cannot be instantiated
        java.lang.reflect.Constructor<MathUtils> c = MathUtils.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(c.getModifiers()));
        c.setAccessible(true);
        MathUtils instance = c.newInstance();
        assertNotNull(instance);
    }
}