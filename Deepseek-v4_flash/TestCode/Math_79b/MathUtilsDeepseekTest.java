package org.apache.commons.math.util;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for MathUtils.
 * Targets line/branch coverage and known null-pointer defects.
 *
 * [Branch & Defect Analysis Matrix]
 * - hash(double[]): missing null check -> NPE
 * - normalizeArray(double[], double): missing null check -> NPE
 * - distance1, distance, distanceInf (double[] and int[]): missing null check -> NPE
 * - equals(double[], double[]): null-safe (already correct)
 * - binomialCoefficient: boundary conditions (n<k, n<0, overflow)
 * - factorial: n<0, n>20 overflow
 * - gcd: MIN_VALUE cases, zero, even/odd combinations
 * - addAndCheck, subAndCheck, mulAndCheck: overflow boundaries
 * - pow: negative exponent
 * - round: NaN, Infinity, scale, rounding methods
 * - nextAfter: special values (NaN, Infinity, zero)
 * - scalb: special values
 * - normalizeAngle: center and angle boundaries
 * - sign/indicator: zero, positive, negative, NaN
 * - compareTo, equals(double,double,double), equals(double,double,int)
 * - cosh, sinh: basic values
 * - log: base and x boundaries
 * - lcm: zero, overflow
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
        assertEquals(Long.MAX_VALUE, MathUtils.addAndCheck(Long.MAX_VALUE - 1, 1));
        assertEquals(Long.MIN_VALUE, MathUtils.addAndCheck(Long.MIN_VALUE + 1, -1));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflowPositive() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflowNegative() {
        MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(timeout = 4000)
    public void testSubAndCheckIntNormal() {
        assertEquals(2, MathUtils.subAndCheck(5, 3));
        assertEquals(-5, MathUtils.subAndCheck(2, 7));
        assertEquals(Integer.MIN_VALUE + 1, MathUtils.subAndCheck(Integer.MIN_VALUE, -1));
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
        assertEquals(Long.MIN_VALUE + 1, MathUtils.subAndCheck(Long.MIN_VALUE, -1L));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflowPositive() {
        MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflowNegative() {
        MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
    }

    @Test(timeout = 4000)
    public void testMulAndCheckIntNormal() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(-6, MathUtils.mulAndCheck(2, -3));
        assertEquals(0, MathUtils.mulAndCheck(0, 100));
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
        assertEquals(0L, MathUtils.mulAndCheck(0L, Long.MAX_VALUE));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowPositive() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflowNegative() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testFactorialBoundaries() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
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
    public void testFactorialDoubleBoundaries() {
        assertEquals(1.0, MathUtils.factorialDouble(0), 0.0);
        assertEquals(120.0, MathUtils.factorialDouble(5), 0.0);
        assertTrue(Double.isInfinite(MathUtils.factorialDouble(171)));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFactorialDoubleNegative() {
        MathUtils.factorialDouble(-1);
    }

    @Test(timeout = 4000)
    public void testFactorialLogBoundaries() {
        assertEquals(0.0, MathUtils.factorialLog(0), 0.0);
        assertEquals(Math.log(120), MathUtils.factorialLog(5), 1e-12);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFactorialLogNegative() {
        MathUtils.factorialLog(-1);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientBoundaries() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 3));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        assertEquals(66L, MathUtils.binomialCoefficient(66, 33)); // large but within long
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBinomialCoefficientNLessThanK() {
        MathUtils.binomialCoefficient(2, 5);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBinomialCoefficientNNegative() {
        MathUtils.binomialCoefficient(-1, 0);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testBinomialCoefficientOverflow() {
        MathUtils.binomialCoefficient(67, 34); // result > Long.MAX_VALUE
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientDoubleBoundaries() {
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 0.0);
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 0.0);
        assertTrue(MathUtils.binomialCoefficientDouble(1029, 500) > 0);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientLogBoundaries() {
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 0.0);
        assertEquals(Math.log(5), MathUtils.binomialCoefficientLog(5, 1), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGcdBoundaries() {
        assertEquals(0, MathUtils.gcd(0, 0));
        assertEquals(5, MathUtils.gcd(10, 5));
        assertEquals(1, MathUtils.gcd(1, 1));
        assertEquals(1, MathUtils.gcd(Integer.MAX_VALUE, 1));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE + 1, 1));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testGcdMinValue() {
        MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testGcdMinValueAndZero() {
        MathUtils.gcd(Integer.MIN_VALUE, 0);
    }

    @Test(timeout = 4000)
    public void testLcmBoundaries() {
        assertEquals(0, MathUtils.lcm(0, 5));
        assertEquals(10, MathUtils.lcm(2, 5));
        assertEquals(12, MathUtils.lcm(4, 6));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testLcmOverflow() {
        MathUtils.lcm(Integer.MAX_VALUE, 2);
    }

    // ========== Partition C: Defect-Targeted Branch Zone (NullPointerException) ==========

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testHashDoubleArrayNull() {
        MathUtils.hash((double[]) null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testNormalizeArrayNull() {
        MathUtils.normalizeArray(null, 1.0);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testDistance1DoubleNull() {
        MathUtils.distance1(null, new double[]{1.0});
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testDistance1IntNull() {
        MathUtils.distance1(null, new int[]{1});
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testDistanceDoubleNull() {
        MathUtils.distance(null, new double[]{1.0});
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testDistanceIntNull() {
        MathUtils.distance(null, new int[]{1});
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testDistanceInfDoubleNull() {
        MathUtils.distanceInf(null, new double[]{1.0});
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testDistanceInfIntNull() {
        MathUtils.distanceInf(null, new int[]{1});
    }

    // Additional null tests for other methods that may not handle null
    @Test(timeout = 4000)
    public void testEqualsDoubleArrayNull() {
        assertTrue(MathUtils.equals(null, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPowIntNegativeExponent() {
        MathUtils.pow(2, -1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPowLongNegativeExponent() {
        MathUtils.pow(2L, -1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPowBigIntNegativeExponent() {
        MathUtils.pow(java.math.BigInteger.ONE, -1);
    }

    @Test(timeout = 4000)
    public void testPowIntZeroExponent() {
        assertEquals(1, MathUtils.pow(2, 0));
        assertEquals(1, MathUtils.pow(0, 0)); // 0^0 = 1
    }

    @Test(timeout = 4000)
    public void testNormalizeArrayInvalidSum() {
        double[] input = {1.0, 2.0};
        try {
            MathUtils.normalizeArray(input, Double.POSITIVE_INFINITY);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            MathUtils.normalizeArray(input, Double.NaN);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testNormalizeArrayInfiniteElement() {
        MathUtils.normalizeArray(new double[]{1.0, Double.POSITIVE_INFINITY}, 1.0);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testNormalizeArraySumZero() {
        MathUtils.normalizeArray(new double[]{0.0, 0.0}, 1.0);
    }

    @Test(timeout = 4000)
    public void testNormalizeArrayNormal() {
        double[] input = {1.0, 2.0, 3.0};
        double[] result = MathUtils.normalizeArray(input, 6.0);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, result, 1e-12);
    }

    @Test(timeout = 4000)
    public void testNormalizeArrayWithNaN() {
        double[] input = {1.0, Double.NaN, 2.0};
        double[] result = MathUtils.normalizeArray(input, 3.0);
        assertEquals(1.0, result[0], 1e-12);
        assertTrue(Double.isNaN(result[1]));
        assertEquals(2.0, result[2], 1e-12);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleBoundaries() {
        assertEquals(3.14, MathUtils.round(3.14159, 2), 0.0);
        assertEquals(3.14, MathUtils.round(3.14159, 2, java.math.BigDecimal.ROUND_HALF_UP), 0.0);
        assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 2), 0.0);
    }

    @Test(timeout = 4000)
    public void testRoundFloatBoundaries() {
        assertEquals(3.14f, MathUtils.round(3.14159f, 2), 0.0f);
        assertTrue(Float.isNaN(MathUtils.round(Float.NaN, 2)));
        assertEquals(Float.POSITIVE_INFINITY, MathUtils.round(Float.POSITIVE_INFINITY, 2), 0.0f);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRoundUnscaledInvalidRoundingMethod() {
        // This will be called via round(float, int, int) with invalid method
        MathUtils.round(1.0f, 2, 999);
    }

    @Test(timeout = 4000)
    public void testNextAfterSpecialCases() {
        assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1.0)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.nextAfter(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testScalbSpecialCases() {
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 5)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.scalb(Double.POSITIVE_INFINITY, 5), 0.0);
        assertEquals(0.0, MathUtils.scalb(0.0, 5), 0.0);
        assertEquals(4.0, MathUtils.scalb(1.0, 2), 0.0);
    }

    @Test(timeout = 4000)
    public void testNormalizeAngle() {
        assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-12);
        assertEquals(Math.PI, MathUtils.normalizeAngle(Math.PI, 0.0), 1e-12);
        assertEquals(0.0, MathUtils.normalizeAngle(2 * Math.PI, 0.0), 1e-12);
        assertEquals(-Math.PI, MathUtils.normalizeAngle(3 * Math.PI, 0.0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSignAndIndicator() {
        assertEquals(1, MathUtils.sign(5));
        assertEquals(0, MathUtils.sign(0));
        assertEquals(-1, MathUtils.sign(-5));
        assertEquals(1.0, MathUtils.sign(5.0), 0.0);
        assertEquals(0.0, MathUtils.sign(0.0), 0.0);
        assertEquals(-1.0, MathUtils.sign(-5.0), 0.0);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));

        assertEquals(1, MathUtils.indicator(5));
        assertEquals(-1, MathUtils.indicator(-5));
        assertEquals(1.0, MathUtils.indicator(5.0), 0.0);
        assertEquals(-1.0, MathUtils.indicator(-5.0), 0.0);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testCompareTo() {
        assertEquals(0, MathUtils.compareTo(1.0, 1.0, 0.0));
        assertEquals(-1, MathUtils.compareTo(1.0, 2.0, 0.0));
        assertEquals(1, MathUtils.compareTo(2.0, 1.0, 0.0));
        assertEquals(0, MathUtils.compareTo(1.0, 1.1, 0.2));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleDouble() {
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertFalse(MathUtils.equals(1.0, 2.0));
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equals(1.0, Double.NaN));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleDoubleEps() {
        assertTrue(MathUtils.equals(1.0, 1.05, 0.1));
        assertFalse(MathUtils.equals(1.0, 1.2, 0.1));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleDoubleUlps() {
        assertTrue(MathUtils.equals(1.0, 1.0, 1));
        assertTrue(MathUtils.equals(1.0, 1.0 + Double.MIN_VALUE, 2));
        assertFalse(MathUtils.equals(1.0, 2.0, 1000));
    }

    @Test(timeout = 4000)
    public void testCosh() {
        assertEquals(1.0, MathUtils.cosh(0.0), 1e-12);
        assertTrue(MathUtils.cosh(1.0) > 0);
    }

    @Test(timeout = 4000)
    public void testSinh() {
        assertEquals(0.0, MathUtils.sinh(0.0), 1e-12);
        assertTrue(MathUtils.sinh(1.0) > 0);
    }

    @Test(timeout = 4000)
    public void testLog() {
        assertEquals(1.0, MathUtils.log(10.0, 10.0), 1e-12);
        assertEquals(2.0, MathUtils.log(2.0, 4.0), 1e-12);
        assertTrue(Double.isNaN(MathUtils.log(-1.0, 1.0)));
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    // (No objects to test, but we can test array equality and hash consistency)

    @Test(timeout = 4000)
    public void testHashDoubleArrayConsistency() {
        double[] arr = {1.0, 2.0, 3.0};
        int h1 = MathUtils.hash(arr);
        int h2 = MathUtils.hash(arr);
        assertEquals(h1, h2);
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArray() {
        double[] a = {1.0, 2.0};
        double[] b = {1.0, 2.0};
        double[] c = {1.0, 3.0};
        assertTrue(MathUtils.equals(a, b));
        assertFalse(MathUtils.equals(a, c));
        assertFalse(MathUtils.equals(a, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testDistanceMethods() {
        double[] p1 = {0.0, 0.0};
        double[] p2 = {3.0, 4.0};
        assertEquals(7.0, MathUtils.distance1(p1, p2), 1e-12);
        assertEquals(5.0, MathUtils.distance(p1, p2), 1e-12);
        assertEquals(4.0, MathUtils.distanceInf(p1, p2), 1e-12);

        int[] ip1 = {0, 0};
        int[] ip2 = {3, 4};
        assertEquals(7, MathUtils.distance1(ip1, ip2));
        assertEquals(5.0, MathUtils.distance(ip1, ip2), 1e-12);
        assertEquals(4, MathUtils.distanceInf(ip1, ip2));
    }
}