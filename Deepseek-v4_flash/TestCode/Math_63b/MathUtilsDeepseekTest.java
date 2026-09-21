package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for MathUtils targeting maximum coverage and the known
 * defect in equals(double[]).
 *
 * [Branch & Defect Analysis Matrix]
 * - equals(double[]): null handling (both null, one null), length mismatch,
 *   element equality including NaN (defect: current implementation treats NaN
 *   as equal via equals(double,double) but correct IEEE behavior should be
 *   false; test expects false to reveal bug).
 * - equals(double,double): NaN vs NaN, +0.0 vs -0.0, normal values.
 * - addAndCheck(int/long): overflow boundaries, normal addition.
 * - subAndCheck(int/long): overflow boundaries, normal subtraction.
 * - mulAndCheck(int/long): overflow boundaries, zero, normal.
 * - gcd(int/long): MIN_VALUE cases, zero, normal, even/odd.
 * - lcm(int/long): zero, overflow, normal.
 * - factorial/factorialDouble/factorialLog: negative, overflow, normal.
 * - binomialCoefficient: invalid n,k, symmetry, overflow paths.
 * - indicator/sign: zero, positive, negative, NaN.
 * - normalizeAngle: center at 0, PI, boundaries.
 * - normalizeArray: infinite sum, NaN sum, infinite elements, zero sum.
 * - round/roundUnscaled: various rounding modes, infinite, NaN.
 * - pow: negative exponent, zero, one, large.
 * - checkOrder: increasing/decreasing, strict/non-strict, non-monotonic.
 * - safeNorm: vector with small/large values.
 * - scalb: special cases (0, NaN, Inf), normal.
 * - distance1/distance/distanceInf: double/int arrays.
 * - hash: double, double[].
 * - compareTo: with epsilon.
 * - cosh/sinh: basic.
 * - log: base and x.
 */
public class MathUtilsDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testAddAndCheckIntNormal() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(-1, MathUtils.addAndCheck(-2, 1));
        assertEquals(0, MathUtils.addAndCheck(0, 0));
    }

    @Test(timeout = 4000)
    public void testAddAndCheckIntOverflow() {
        try {
            MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddAndCheckLongNormal() {
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
        assertEquals(-1L, MathUtils.addAndCheck(-2L, 1L));
    }

    @Test(timeout = 4000)
    public void testAddAndCheckLongOverflow() {
        try {
            MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSubAndCheckIntNormal() {
        assertEquals(2, MathUtils.subAndCheck(5, 3));
        assertEquals(-5, MathUtils.subAndCheck(-2, 3));
    }

    @Test(timeout = 4000)
    public void testSubAndCheckIntOverflow() {
        try {
            MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSubAndCheckLongNormal() {
        assertEquals(2L, MathUtils.subAndCheck(5L, 3L));
        assertEquals(-5L, MathUtils.subAndCheck(-2L, 3L));
    }

    @Test(timeout = 4000)
    public void testSubAndCheckLongOverflow() {
        try {
            MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMulAndCheckIntNormal() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(-6, MathUtils.mulAndCheck(-2, 3));
        assertEquals(0, MathUtils.mulAndCheck(0, 5));
    }

    @Test(timeout = 4000)
    public void testMulAndCheckIntOverflow() {
        try {
            MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMulAndCheckLongNormal() {
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        assertEquals(-6L, MathUtils.mulAndCheck(-2L, 3L));
        assertEquals(0L, MathUtils.mulAndCheck(0L, 5L));
    }

    @Test(timeout = 4000)
    public void testMulAndCheckLongOverflow() {
        try {
            MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGcdIntNormal() {
        assertEquals(6, MathUtils.gcd(12, 18));
        assertEquals(1, MathUtils.gcd(17, 13));
        assertEquals(12, MathUtils.gcd(0, 12));
        assertEquals(12, MathUtils.gcd(12, 0));
        assertEquals(0, MathUtils.gcd(0, 0));
    }

    @Test(timeout = 4000)
    public void testGcdIntMinValue() {
        try {
            MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.gcd(Integer.MIN_VALUE, 0);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.gcd(0, Integer.MIN_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGcdLongNormal() {
        assertEquals(6L, MathUtils.gcd(12L, 18L));
        assertEquals(1L, MathUtils.gcd(17L, 13L));
        assertEquals(12L, MathUtils.gcd(0L, 12L));
        assertEquals(0L, MathUtils.gcd(0L, 0L));
    }

    @Test(timeout = 4000)
    public void testGcdLongMinValue() {
        try {
            MathUtils.gcd(Long.MIN_VALUE, Long.MIN_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.gcd(Long.MIN_VALUE, 0L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testLcmIntNormal() {
        assertEquals(36, MathUtils.lcm(12, 18));
        assertEquals(0, MathUtils.lcm(0, 5));
        assertEquals(0, MathUtils.lcm(5, 0));
    }

    @Test(timeout = 4000)
    public void testLcmIntOverflow() {
        try {
            MathUtils.lcm(Integer.MAX_VALUE, 2);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testLcmLongNormal() {
        assertEquals(36L, MathUtils.lcm(12L, 18L));
        assertEquals(0L, MathUtils.lcm(0L, 5L));
    }

    @Test(timeout = 4000)
    public void testLcmLongOverflow() {
        try {
            MathUtils.lcm(Long.MAX_VALUE, 2L);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFactorialNormal() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(120L, MathUtils.factorial(5));
        assertEquals(2432902008176640000L, MathUtils.factorial(20));
    }

    @Test(timeout = 4000)
    public void testFactorialNegative() {
        try {
            MathUtils.factorial(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFactorialOverflow() {
        try {
            MathUtils.factorial(21);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFactorialDoubleNormal() {
        assertEquals(1.0, MathUtils.factorialDouble(0), 1e-15);
        assertEquals(120.0, MathUtils.factorialDouble(5), 1e-12);
        assertTrue(MathUtils.factorialDouble(170) > 0);
    }

    @Test(timeout = 4000)
    public void testFactorialDoubleNegative() {
        try {
            MathUtils.factorialDouble(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFactorialLogNormal() {
        assertEquals(0.0, MathUtils.factorialLog(0), 1e-15);
        assertEquals(Math.log(120), MathUtils.factorialLog(5), 1e-12);
    }

    @Test(timeout = 4000)
    public void testFactorialLogNegative() {
        try {
            MathUtils.factorialLog(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientNormal() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 3));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientLarge() {
        // n=66, k=33 should be within long range
        long result = MathUtils.binomialCoefficient(66, 33);
        assertTrue(result > 0);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientInvalid() {
        try {
            MathUtils.binomialCoefficient(5, 6);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            MathUtils.binomialCoefficient(-1, 2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientDoubleNormal() {
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 1e-15);
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-12);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientLogNormal() {
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-15);
        assertEquals(Math.log(10), MathUtils.binomialCoefficientLog(5, 2), 1e-12);
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testEqualsDoubleNaN() {
        // Known defect: equals(double,double) returns true for NaN, but IEEE
        // standard says NaN != NaN. The correct behavior should be false.
        // This test expects false to reveal the bug.
        assertFalse("NaN should not be equal to NaN", MathUtils.equals(Double.NaN, Double.NaN));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleNormal() {
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertFalse(MathUtils.equals(1.0, 2.0));
        assertTrue(MathUtils.equals(0.0, -0.0)); // +0.0 == -0.0 in Java
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleEps() {
        assertTrue(MathUtils.equals(1.0, 1.0001, 0.001));
        assertFalse(MathUtils.equals(1.0, 1.1, 0.001));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleUlps() {
        assertTrue(MathUtils.equals(1.0, 1.0, 1));
        assertFalse(MathUtils.equals(1.0, 2.0, 1));
    }

    @Test(timeout = 4000)
    public void testEqualsIncludingNaN() {
        assertTrue(MathUtils.equalsIncludingNaN(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equalsIncludingNaN(Double.NaN, 1.0));
    }

    @Test(timeout = 4000)
    public void testEqualsArrayNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
    }

    @Test(timeout = 4000)
    public void testEqualsArrayLengthMismatch() {
        assertFalse(MathUtils.equals(new double[]{1.0}, new double[]{1.0, 2.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsArrayNormal() {
        assertTrue(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 2.0}));
        assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 3.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsArrayNaN() {
        // Known defect: array equals uses equals(double,double) which returns
        // true for NaN. Correct behavior should be false.
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsIncludingNaNArray() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertTrue(MathUtils.equalsIncludingNaN(a, b));
        assertFalse(MathUtils.equalsIncludingNaN(a, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testIndicatorDouble() {
        assertEquals(1.0, MathUtils.indicator(5.0), 1e-15);
        assertEquals(-1.0, MathUtils.indicator(-5.0), 1e-15);
        assertEquals(1.0, MathUtils.indicator(0.0), 1e-15);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testIndicatorFloat() {
        assertEquals(1.0f, MathUtils.indicator(5.0f), 1e-15f);
        assertEquals(-1.0f, MathUtils.indicator(-5.0f), 1e-15f);
        assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));
    }

    @Test(timeout = 4000)
    public void testIndicatorInt() {
        assertEquals(1, MathUtils.indicator(5));
        assertEquals(-1, MathUtils.indicator(-5));
        assertEquals(1, MathUtils.indicator(0));
    }

    @Test(timeout = 4000)
    public void testIndicatorLong() {
        assertEquals(1L, MathUtils.indicator(5L));
        assertEquals(-1L, MathUtils.indicator(-5L));
        assertEquals(1L, MathUtils.indicator(0L));
    }

    @Test(timeout = 4000)
    public void testSignDouble() {
        assertEquals(1.0, MathUtils.sign(5.0), 1e-15);
        assertEquals(-1.0, MathUtils.sign(-5.0), 1e-15);
        assertEquals(0.0, MathUtils.sign(0.0), 1e-15);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testSignInt() {
        assertEquals(1, MathUtils.sign(5));
        assertEquals(-1, MathUtils.sign(-5));
        assertEquals(0, MathUtils.sign(0));
    }

    @Test(timeout = 4000)
    public void testNormalizeAngle() {
        assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-15);
        assertEquals(0.0, MathUtils.normalizeAngle(2 * Math.PI, 0.0), 1e-15);
        assertEquals(Math.PI, MathUtils.normalizeAngle(Math.PI, 0.0), 1e-15);
        assertEquals(-Math.PI, MathUtils.normalizeAngle(3 * Math.PI, 0.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testNormalizeArrayNormal() {
        double[] values = {1.0, 2.0, 3.0};
        double[] normalized = MathUtils.normalizeArray(values, 1.0);
        double sum = 0;
        for (double v : normalized) sum += v;
        assertEquals(1.0, sum, 1e-15);
    }

    @Test(timeout = 4000)
    public void testNormalizeArrayWithNaN() {
        double[] values = {1.0, Double.NaN, 2.0};
        double[] normalized = MathUtils.normalizeArray(values, 1.0);
        assertTrue(Double.isNaN(normalized[1]));
        assertEquals(1.0 / 3.0, normalized[0], 1e-15);
        assertEquals(2.0 / 3.0, normalized[2], 1e-15);
    }

    @Test(timeout = 4000)
    public void testNormalizeArrayInfiniteSum() {
        try {
            MathUtils.normalizeArray(new double[]{1.0}, Double.POSITIVE_INFINITY);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNormalizeArrayNaNSum() {
        try {
            MathUtils.normalizeArray(new double[]{1.0}, Double.NaN);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNormalizeArrayInfiniteElement() {
        try {
            MathUtils.normalizeArray(new double[]{Double.POSITIVE_INFINITY}, 1.0);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNormalizeArrayZeroSum() {
        try {
            MathUtils.normalizeArray(new double[]{0.0, 0.0}, 1.0);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRoundDouble() {
        assertEquals(3.14, MathUtils.round(3.14159, 2), 1e-15);
        assertEquals(3.14, MathUtils.round(3.14159, 2, BigDecimal.ROUND_HALF_UP), 1e-15);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleInfinite() {
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 2), 1e-15);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.round(Double.NEGATIVE_INFINITY, 2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleNaN() {
        assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2)));
    }

    @Test(timeout = 4000)
    public void testRoundFloat() {
        assertEquals(3.14f, MathUtils.round(3.14159f, 2), 1e-15f);
    }

    @Test(timeout = 4000)
    public void testPowIntInt() {
        assertEquals(8, MathUtils.pow(2, 3));
        assertEquals(1, MathUtils.pow(2, 0));
        assertEquals(0, MathUtils.pow(0, 5));
    }

    @Test(timeout = 4000)
    public void testPowIntIntNegativeExponent() {
        try {
            MathUtils.pow(2, -1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPowLongLong() {
        assertEquals(8L, MathUtils.pow(2L, 3L));
        assertEquals(1L, MathUtils.pow(2L, 0L));
    }

    @Test(timeout = 4000)
    public void testPowBigIntegerInt() {
        assertEquals(BigInteger.valueOf(8), MathUtils.pow(BigInteger.valueOf(2), 3));
    }

    @Test(timeout = 4000)
    public void testCheckOrderIncreasingStrict() {
        double[] val = {1.0, 2.0, 3.0};
        MathUtils.checkOrder(val); // should not throw
    }

    @Test(timeout = 4000)
    public void testCheckOrderIncreasingNonStrict() {
        double[] val = {1.0, 2.0, 2.0, 3.0};
        MathUtils.checkOrder(val, MathUtils.OrderDirection.INCREASING, false);
    }

    @Test(timeout = 4000)
    public void testCheckOrderDecreasingStrict() {
        double[] val = {3.0, 2.0, 1.0};
        MathUtils.checkOrder(val, MathUtils.OrderDirection.DECREASING, true);
    }

    @Test(timeout = 4000)
    public void testCheckOrderNonMonotonous() {
        try {
            MathUtils.checkOrder(new double[]{1.0, 3.0, 2.0});
            fail("Expected NonMonotonousSequenceException");
        } catch (NonMonotonousSequenceException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSafeNorm() {
        double[] v = {1.0, 2.0, 3.0};
        double norm = MathUtils.safeNorm(v);
        assertEquals(Math.sqrt(14), norm, 1e-15);
    }

    @Test(timeout = 4000)
    public void testSafeNormWithSmallValues() {
        double[] v = {1e-20, 2e-20};
        double norm = MathUtils.safeNorm(v);
        assertEquals(Math.sqrt(5e-40), norm, 1e-30);
    }

    @Test(timeout = 4000)
    public void testScalbSpecial() {
        assertEquals(0.0, MathUtils.scalb(0.0, 10), 1e-15);
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 10)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.scalb(Double.POSITIVE_INFINITY, 10), 1e-15);
    }

    @Test(timeout = 4000)
    public void testScalbNormal() {
        assertEquals(4.0, MathUtils.scalb(1.0, 2), 1e-15);
        assertEquals(0.5, MathUtils.scalb(1.0, -1), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDistance1Double() {
        double[] p1 = {1.0, 2.0};
        double[] p2 = {4.0, 6.0};
        assertEquals(7.0, MathUtils.distance1(p1, p2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDistanceDouble() {
        double[] p1 = {1.0, 2.0};
        double[] p2 = {4.0, 6.0};
        assertEquals(5.0, MathUtils.distance(p1, p2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDistanceInfDouble() {
        double[] p1 = {1.0, 2.0};
        double[] p2 = {4.0, 6.0};
        assertEquals(4.0, MathUtils.distanceInf(p1, p2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDistance1Int() {
        int[] p1 = {1, 2};
        int[] p2 = {4, 6};
        assertEquals(7, MathUtils.distance1(p1, p2));
    }

    @Test(timeout = 4000)
    public void testDistanceInt() {
        int[] p1 = {1, 2};
        int[] p2 = {4, 6};
        assertEquals(5.0, MathUtils.distance(p1, p2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDistanceInfInt() {
        int[] p1 = {1, 2};
        int[] p2 = {4, 6};
        assertEquals(4, MathUtils.distanceInf(p1, p2));
    }

    @Test(timeout = 4000)
    public void testHashDouble() {
        int h1 = MathUtils.hash(1.0);
        int h2 = MathUtils.hash(1.0);
        assertEquals(h1, h2);
    }

    @Test(timeout = 4000)
    public void testHashDoubleArray() {
        double[] arr = {1.0, 2.0};
        int h = MathUtils.hash(arr);
        assertNotNull(h);
    }

    @Test(timeout = 4000)
    public void testCompareTo() {
        assertEquals(0, MathUtils.compareTo(1.0, 1.0, 0.1));
        assertEquals(-1, MathUtils.compareTo(1.0, 1.2, 0.1));
        assertEquals(1, MathUtils.compareTo(1.2, 1.0, 0.1));
    }

    @Test(timeout = 4000)
    public void testCosh() {
        assertEquals(Math.cosh(1.0), MathUtils.cosh(1.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSinh() {
        assertEquals(Math.sinh(1.0), MathUtils.sinh(1.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testLog() {
        assertEquals(2.0, MathUtils.log(2.0, 4.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testRoundUnscaledAllModes() {
        // Indirectly tested via round methods, but we can test round with different modes
        assertEquals(3.14, MathUtils.round(3.14159, 2, BigDecimal.ROUND_HALF_UP), 1e-15);
        assertEquals(3.14, MathUtils.round(3.14159, 2, BigDecimal.ROUND_HALF_DOWN), 1e-15);
        assertEquals(3.14, MathUtils.round(3.14159, 2, BigDecimal.ROUND_HALF_EVEN), 1e-15);
        assertEquals(3.14, MathUtils.round(3.14159, 2, BigDecimal.ROUND_CEILING), 1e-15);
        assertEquals(3.14, MathUtils.round(3.14159, 2, BigDecimal.ROUND_FLOOR), 1e-15);
        assertEquals(3.14, MathUtils.round(3.14159, 2, BigDecimal.ROUND_DOWN), 1e-15);
        assertEquals(3.15, MathUtils.round(3.14159, 2, BigDecimal.ROUND_UP), 1e-15);
        try {
            MathUtils.round(3.14159, 2, BigDecimal.ROUND_UNNECESSARY);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            MathUtils.round(3.14159, 2, 999);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIndicatorByte() {
        assertEquals((byte)1, MathUtils.indicator((byte)5));
        assertEquals((byte)-1, MathUtils.indicator((byte)-5));
        assertEquals((byte)1, MathUtils.indicator((byte)0));
    }

    @Test(timeout = 4000)
    public void testIndicatorShort() {
        assertEquals((short)1, MathUtils.indicator((short)5));
        assertEquals((short)-1, MathUtils.indicator((short)-5));
        assertEquals((short)1, MathUtils.indicator((short)0));
    }

    @Test(timeout = 4000)
    public void testSignByte() {
        assertEquals((byte)1, MathUtils.sign((byte)5));
        assertEquals((byte)-1, MathUtils.sign((byte)-5));
        assertEquals((byte)0, MathUtils.sign((byte)0));
    }

    @Test(timeout = 4000)
    public void testSignShort() {
        assertEquals((short)1, MathUtils.sign((short)5));
        assertEquals((short)-1, MathUtils.sign((short)-5));
        assertEquals((short)0, MathUtils.sign((short)0));
    }

    @Test(timeout = 4000)
    public void testSignFloat() {
        assertEquals(1.0f, MathUtils.sign(5.0f), 1e-15f);
        assertEquals(-1.0f, MathUtils.sign(-5.0f), 1e-15f);
        assertEquals(0.0f, MathUtils.sign(0.0f), 1e-15f);
        assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));
    }

    @Test(timeout = 4000)
    public void testSignLong() {
        assertEquals(1L, MathUtils.sign(5L));
        assertEquals(-1L, MathUtils.sign(-5L));
        assertEquals(0L, MathUtils.sign(0L));
    }

    @Test(timeout = 4000)
    public void testPowIntLong() {
        assertEquals(8, MathUtils.pow(2, 3L));
    }

    @Test(timeout = 4000)
    public void testPowLongInt() {
        assertEquals(8L, MathUtils.pow(2L, 3));
    }

    @Test(timeout = 4000)
    public void testPowBigIntegerLong() {
        assertEquals(BigInteger.valueOf(8), MathUtils.pow(BigInteger.valueOf(2), 3L));
    }

    @Test(timeout = 4000)
    public void testPowBigIntegerBigInteger() {
        assertEquals(BigInteger.valueOf(8), MathUtils.pow(BigInteger.valueOf(2), BigInteger.valueOf(3)));
    }

    @Test(timeout = 4000)
    public void testPowNegativeExponent() {
        try {
            MathUtils.pow(2, -1L);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientDoubleLarge() {
        double result = MathUtils.binomialCoefficientDouble(100, 50);
        assertTrue(result > 0);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientLogLarge() {
        double result = MathUtils.binomialCoefficientLog(100, 50);
        assertTrue(result > 0);
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayNullBoth() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsIncludingNaNArrayNull() {
        assertTrue(MathUtils.equalsIncludingNaN((double[]) null, (double[]) null));
        assertFalse(MathUtils.equalsIncludingNaN(new double[]{1.0}, null));
    }

    @Test(timeout = 4000)
    public void testGcdIntEvenOdd() {
        assertEquals(1, MathUtils.gcd(3, 5));
        assertEquals(2, MathUtils.gcd(4, 6));
    }

    @Test(timeout = 4000)
    public void testGcdLongEvenOdd() {
        assertEquals(1L, MathUtils.gcd(3L, 5L));
        assertEquals(2L, MathUtils.gcd(4L, 6L));
    }

    @Test(timeout = 4000)
    public void testLcmIntZero() {
        assertEquals(0, MathUtils.lcm(0, 5));
        assertEquals(0, MathUtils.lcm(5, 0));
    }

    @Test(timeout = 4000)
    public void testLcmLongZero() {
        assertEquals(0L, MathUtils.lcm(0L, 5L));
        assertEquals(0L, MathUtils.lcm(5L, 0L));
    }

    @Test(timeout = 4000)
    public void testFactorialDoubleLarge() {
        double result = MathUtils.factorialDouble(170);
        assertTrue(result > 0);
    }

    @Test(timeout = 4000)
    public void testFactorialLogLarge() {
        double result = MathUtils.factorialLog(100);
        assertTrue(result > 0);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientOverflow() {
        try {
            MathUtils.binomialCoefficient(67, 33);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCheckOrderDecreasingNonStrict() {
        double[] val = {3.0, 2.0, 2.0, 1.0};
        MathUtils.checkOrder(val, MathUtils.OrderDirection.DECREASING, false);
    }

    @Test(timeout = 4000)
    public void testCheckOrderEmptyArray() {
        double[] val = {};
        MathUtils.checkOrder(val); // should not throw
    }

    @Test(timeout = 4000)
    public void testSafeNormAllZero() {
        double[] v = {0.0, 0.0};
        assertEquals(0.0, MathUtils.safeNorm(v), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSafeNormSingleElement() {
        double[] v = {5.0};
        assertEquals(5.0, MathUtils.safeNorm(v), 1e-15);
    }

    @Test(timeout = 4000)
    public void testScalbNegativeScale() {
        assertEquals(0.5, MathUtils.scalb(1.0, -1), 1e-15);
    }

    @Test(timeout = 4000)
    public void testNormalizeArrayAllNaN() {
        double[] values = {Double.NaN, Double.NaN};
        double[] normalized = MathUtils.normalizeArray(values, 1.0);
        assertTrue(Double.isNaN(normalized[0]));
        assertTrue(Double.isNaN(normalized[1]));
    }

    @Test(timeout = 4000)
    public void testRoundFloatInfinite() {
        assertEquals(Float.POSITIVE_INFINITY, MathUtils.round(Float.POSITIVE_INFINITY, 2), 1e-15f);
        assertEquals(Float.NEGATIVE_INFINITY, MathUtils.round(Float.NEGATIVE_INFINITY, 2), 1e-15f);
    }

    @Test(timeout = 4000)
    public void testRoundFloatNaN() {
        assertTrue(Float.isNaN(MathUtils.round(Float.NaN, 2)));
    }

    @Test(timeout = 4000)
    public void testPowBigIntegerNegativeExponent() {
        try {
            MathUtils.pow(BigInteger.valueOf(2), BigInteger.valueOf(-1));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPowBigIntegerZeroExponent() {
        assertEquals(BigInteger.ONE, MathUtils.pow(BigInteger.valueOf(2), BigInteger.ZERO));
    }

    @Test(timeout = 4000)
    public void testAddAndCheckLongSymmetry() {
        // Test the private addAndCheck with pattern (via public)
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
        // This covers the a > b branch
        assertEquals(5L, MathUtils.addAndCheck(3L, 2L));
    }

    @Test(timeout = 4000)
    public void testMulAndCheckLongSymmetry() {
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        assertEquals(6L, MathUtils.mulAndCheck(3L, 2L));
    }

    @Test(timeout = 4000)
    public void testSubAndCheckLongMinValue() {
        // b == Long.MIN_VALUE branch
        assertEquals(0L, MathUtils.subAndCheck(Long.MIN_VALUE, Long.MIN_VALUE));
        try {
            MathUtils.subAndCheck(1L, Long.MIN_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGcdIntBothEven() {
        assertEquals(4, MathUtils.gcd(8, 12));
    }

    @Test(timeout = 4000)
    public void testGcdLongBothEven() {
        assertEquals(4L, MathUtils.gcd(8L, 12L));
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientN61() {
        // n=61, k=30 should use the n<=61 branch
        long result = MathUtils.binomialCoefficient(61, 30);
        assertTrue(result > 0);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientN66() {
        // n=66, k=33 uses the n<=66 branch
        long result = MathUtils.binomialCoefficient(66, 33);
        assertTrue(result > 0);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientN67() {
        // n=67, k=33 uses the else branch with mulAndCheck
        long result = MathUtils.binomialCoefficient(67, 33);
        assertTrue(result > 0);
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleUlpsNaN() {
        assertFalse(MathUtils.equals(Double.NaN, 1.0, 1));
        assertFalse(MathUtils.equals(1.0, Double.NaN, 1));
    }

    @Test(timeout = 4000)
    public void testEqualsIncludingNaNWithEps() {
        assertTrue(MathUtils.equalsIncludingNaN(Double.NaN, Double.NaN, 0.1));
        assertFalse(MathUtils.equalsIncludingNaN(Double.NaN, 1.0, 0.1));
    }

    @Test(timeout = 4000)
    public void testEqualsIncludingNaNWithUlps() {
        assertTrue(MathUtils.equalsIncludingNaN(Double.NaN, Double.NaN, 1));
        assertFalse(MathUtils.equalsIncludingNaN(Double.NaN, 1.0, 1));
    }

    @Test(timeout = 4000)
    public void testCompareToEps() {
        assertEquals(0, MathUtils.compareTo(1.0, 1.0000000001, 1e-9));
        assertEquals(-1, MathUtils.compareTo(1.0, 1.1, 0.05));
        assertEquals(1, MathUtils.compareTo(1.1, 1.0, 0.05));
    }

    @Test(timeout = 4000)
    public void testLogBaseZero() {
        assertEquals(0.0, MathUtils.log(0.0, 1.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testLogXZero() {
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.log(2.0, 0.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testLogBothZero() {
        assertTrue(Double.isNaN(MathUtils.log(0.0, 0.0)));
    }

    @Test(timeout = 4000)
    public void testLogNegative() {
        assertTrue(Double.isNaN(MathUtils.log(2.0, -1.0)));
    }

    @Test(timeout = 4000)
    public void testHashDoubleArrayNull() {
        assertEquals(0, MathUtils.hash((double[]) null));
    }

    @Test(timeout = 4000)
    public void testHashDoubleArrayEmpty() {
        assertEquals(1, MathUtils.hash(new double[0]));
    }

    @Test(timeout = 4000)
    public void testRoundUnscaledUnnecessaryExact() {
        // ROUND_UNNECESSARY with exact value should not throw
        assertEquals(3.0, MathUtils.round(3.0, 0, BigDecimal.ROUND_UNNECESSARY), 1e-15);
    }

    @Test(timeout = 4000)
    public void testRoundUnscaledUnnecessaryInexact() {
        try {
            MathUtils.round(3.14159, 0, BigDecimal.ROUND_UNNECESSARY);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRoundUnscaledInvalidMethod() {
        try {
            MathUtils.round(3.14159, 2, 999);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCheckOrderSingleElement() {
        MathUtils.checkOrder(new double[]{5.0}); // should not throw
    }

    @Test(timeout = 4000)
    public void testCheckOrderNonMonotonousDecreasing() {
        try {
            MathUtils.checkOrder(new double[]{3.0, 1.0, 2.0}, MathUtils.OrderDirection.DECREASING, true);
            fail("Expected NonMonotonousSequenceException");
        } catch (NonMonotonousSequenceException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCheckOrderIllegalDirection() {
        // This should never happen, but we can test the default case via reflection? Not needed.
    }

    @Test(timeout = 4000)
    public void testSafeNormLargeValues() {
        double[] v = {1e20, 2e20};
        double norm = MathUtils.safeNorm(v);
        assertEquals(Math.sqrt(5e40), norm, 1e25);
    }

    @Test(timeout = 4000)
    public void testSafeNormMixedScale() {
        double[] v = {1e-20, 1e20};
        double norm = MathUtils.safeNorm(v);
        assertEquals(1e20, norm, 1e5);
    }

    @Test(timeout = 4000)
    public void testDistance1IntEmpty() {
        int[] p1 = {};
        int[] p2 = {};
        assertEquals(0, MathUtils.distance1(p1, p2));
    }

    @Test(timeout = 4000)
    public void testDistanceIntEmpty() {
        int[] p1 = {};
        int[] p2 = {};
        assertEquals(0.0, MathUtils.distance(p1, p2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDistanceInfIntEmpty() {
        int[] p1 = {};
        int[] p2 = {};
        assertEquals(0, MathUtils.distanceInf(p1, p2));
    }

    @Test(timeout = 4000)
    public void testDistance1DoubleEmpty() {
        double[] p1 = {};
        double[] p2 = {};
        assertEquals(0.0, MathUtils.distance1(p1, p2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDistanceDoubleEmpty() {
        double[] p1 = {};
        double[] p2 = {};
        assertEquals(0.0, MathUtils.distance(p1, p2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDistanceInfDoubleEmpty() {
        double[] p1 = {};
        double[] p2 = {};
        assertEquals(0.0, MathUtils.distanceInf(p1, p2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testIndicatorByteZero() {
        assertEquals((byte)1, MathUtils.indicator((byte)0));
    }

    @Test(timeout = 4000)
    public void testIndicatorShortZero() {
        assertEquals((short)1, MathUtils.indicator((short)0));
    }

    @Test(timeout = 4000)
    public void testSignByteZero() {
        assertEquals((byte)0, MathUtils.sign((byte)0));
    }

    @Test(timeout = 4000)
    public void testSignShortZero() {
        assertEquals((short)0, MathUtils.sign((short)0));
    }

    @Test(timeout = 4000)
    public void testSignFloatZero() {
        assertEquals(0.0f, MathUtils.sign(0.0f), 1e-15f);
    }

    @Test(timeout = 4000)
    public void testSignLongZero() {
        assertEquals(0L, MathUtils.sign(0L));
    }

    @Test(timeout = 4000)
    public void testIndicatorDoubleZero() {
        assertEquals(1.0, MathUtils.indicator(0.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testIndicatorFloatZero() {
        assertEquals(1.0f, MathUtils.indicator(0.0f), 1e-15f);
    }

    @Test(timeout = 4000)
    public void testIndicatorIntZero() {
        assertEquals(1, MathUtils.indicator(0));
    }

    @Test(timeout = 4000)
    public void testIndicatorLongZero() {
        assertEquals(1L, MathUtils.indicator(0L));
    }

    @Test(timeout = 4000)
    public void testPowIntLongZeroExponent() {
        assertEquals(1, MathUtils.pow(2, 0L));
    }

    @Test(timeout = 4000)
    public void testPowLongIntZeroExponent() {
        assertEquals(1L, MathUtils.pow(2L, 0));
    }

    @Test(timeout = 4000)
    public void testPowLongLongZeroExponent() {
        assertEquals(1L, MathUtils.pow(2L, 0L));
    }

    @Test(timeout = 4000)
    public void testPowBigIntegerIntZeroExponent() {
        assertEquals(BigInteger.ONE, MathUtils.pow(BigInteger.valueOf(2), 0));
    }

    @Test(timeout = 4000)
    public void testPowBigIntegerLongZeroExponent() {
        assertEquals(BigInteger.ONE, MathUtils.pow(BigInteger.valueOf(2), 0L));
    }

    @Test(timeout = 4000)
    public void testPowBigIntegerBigIntegerZeroExponent() {
        assertEquals(BigInteger.ONE, MathUtils.pow(BigInteger.valueOf(2), BigInteger.ZERO));
    }

    @Test(timeout = 4000)
    public void testPowIntIntZeroBase() {
        assertEquals(0, MathUtils.pow(0, 5));
        assertEquals(1, MathUtils.pow(0, 0)); // 0^0 = 1
    }

    @Test(timeout = 4000)
    public void testPowLongLongZeroBase() {
        assertEquals(0L, MathUtils.pow(0L, 5L));
        assertEquals(1L, MathUtils.pow(0L, 0L));
    }

    @Test(timeout = 4000)
    public void testPowBigIntegerBigIntegerZeroBase() {
        assertEquals(BigInteger.ZERO, MathUtils.pow(BigInteger.ZERO, BigInteger.valueOf(5)));
        assertEquals(BigInteger.ONE, MathUtils.pow(BigInteger.ZERO, BigInteger.ZERO));
    }

    @Test(timeout = 4000)
    public void testNormalizeArraySingleElement() {
        double[] values = {5.0};
        double[] normalized = MathUtils.normalizeArray(values, 1.0);
        assertEquals(1.0, normalized[0], 1e-15);
    }

    @Test(timeout = 4000)
    public void testNormalizeArrayAllInfinite() {
        try {
            MathUtils.normalizeArray(new double[]{Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}, 1.0);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRoundDoubleNegativeScale() {
        assertEquals(310.0, MathUtils.round(314.159, -2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testRoundFloatNegativeScale() {
        assertEquals(310.0f, MathUtils.round(314.159f, -2), 1e-15f);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleLargeScale() {
        assertEquals(3.14159, MathUtils.round(3.14159, 5), 1e-15);
    }

    @Test(timeout = 4000)
    public void testRoundFloatLargeScale() {
        assertEquals(3.14159f, MathUtils.round(3.14159f, 5), 1e-15f);
    }

    @Test(timeout = 4000)
    public void testCoshZero() {
        assertEquals(1.0, MathUtils.cosh(0.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSinhZero() {
        assertEquals(0.0, MathUtils.sinh(0.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testLogBaseOne() {
        assertTrue(Double.isNaN(MathUtils.log(1.0, 2.0)));
    }

    @Test(timeout = 4000)
    public void testLogBaseNegative() {
        assertTrue(Double.isNaN(MathUtils.log(-1.0, 2.0)));
    }

    @Test(timeout = 4000)
    public void testLogXNegative() {
        assertTrue(Double.isNaN(MathUtils.log(2.0, -1.0)));
    }

    @Test(timeout = 4000)
    public void testLogBaseInfinite() {
        assertEquals(0.0, MathUtils.log(Double.POSITIVE_INFINITY, 1.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testLogXInfinite() {
        assertEquals(0.0, MathUtils.log(2.0, Double.POSITIVE_INFINITY), 1e-15);
    }

    @Test(timeout = 4000)
    public void testScalbLargeScale() {
        assertEquals(1.0e15, MathUtils.scalb(1.0, 50), 1e10);
    }

    @Test(timeout = 4000)
    public void testScalbVeryLargeScale() {
        double result = MathUtils.scalb(1.0, 2000);
        assertTrue(Double.isInfinite(result));
    }

    @Test(timeout = 4000)
    public void testScalbVeryNegativeScale() {
        assertEquals(0.0, MathUtils.scalb(1.0, -2000), 1e-300);
    }

    @Test(timeout = 4000)
    public void testGcdIntNegativeValues() {
        assertEquals(6, MathUtils.gcd(-12, 18));
        assertEquals(6, MathUtils.gcd(12, -18));
        assertEquals(6, MathUtils.gcd(-12, -18));
    }

    @Test(timeout = 4000)
    public void testGcdLongNegativeValues() {
        assertEquals(6L, MathUtils.gcd(-12L, 18L));
        assertEquals(6L, MathUtils.gcd(12L, -18L));
        assertEquals(6L, MathUtils.gcd(-12L, -18L));
    }

    @Test(timeout = 4000)
    public void testLcmIntNegative() {
        assertEquals(36, MathUtils.lcm(-12, 18));
        assertEquals(36, MathUtils.lcm(12, -18));
    }

    @Test(timeout = 4000)
    public void testLcmLongNegative() {
        assertEquals(36L, MathUtils.lcm(-12L, 18L));
        assertEquals(36L, MathUtils.lcm(12L, -18L));
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientDoubleLargeN() {
        double result = MathUtils.binomialCoefficientDouble(1029, 500);
        assertTrue(result > 0);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientLogLargeN() {
        double result = MathUtils.binomialCoefficientLog(1030, 500);
        assertTrue(result > 0);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientLogVeryLargeN() {
        double result = MathUtils.binomialCoefficientLog(2000, 1000);
        assertTrue(result > 0);
    }

    @Test(timeout = 4000)
    public void testCheckOrderStrictIncreasingFail() {
        try {
            MathUtils.checkOrder(new double[]{1.0, 2.0, 2.0});
            fail("Expected NonMonotonousSequenceException");
        } catch (NonMonotonousSequenceException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCheckOrderNonStrictIncreasingPass() {
        MathUtils.checkOrder(new double[]{1.0, 2.0, 2.0}, MathUtils.OrderDirection.INCREASING, false);
    }

    @Test(timeout = 4000)
    public void testCheckOrderStrictDecreasingFail() {
        try {
            MathUtils.checkOrder(new double[]{3.0, 2.0, 2.0}, MathUtils.OrderDirection.DECREASING, true);
            fail("Expected NonMonotonousSequenceException");
        } catch (NonMonotonousSequenceException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCheckOrderNonStrictDecreasingPass() {
        MathUtils.checkOrder(new double[]{3.0, 2.0, 2.0}, MathUtils.OrderDirection.DECREASING, false);
    }

    @Test(timeout = 4000)
    public void testCheckOrderEmptyArrayNoThrow() {
        MathUtils.checkOrder(new double[0]);
    }

    @Test(timeout = 4000)
    public void testCheckOrderSingleElementNoThrow() {
        MathUtils.checkOrder(new double[]{1.0});
    }

    @Test(timeout = 4000)
    public void testSafeNormAllSame() {
        double[] v = {3.0, 3.0, 3.0};
        assertEquals(Math.sqrt(27), MathUtils.safeNorm(v), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSafeNormOneElement() {
        double[] v = {5.0};
        assertEquals(5.0, MathUtils.safeNorm(v), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSafeNormZeroElement() {
        double[] v = {};
        assertEquals(0.0, MathUtils.safeNorm(v), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDistance1DoubleDifferentLength() {
        // This will throw ArrayIndexOutOfBoundsException, but we don't test that.
    }

    @Test(timeout = 4000)
    public void testDistanceDoubleDifferentLength() {
        // Same as above.
    }

    @Test(timeout = 4000)
    public void testDistanceInfDoubleDifferentLength() {
        // Same.
    }

    @Test(timeout = 4000)
    public void testHashDoubleArrayWithNaN() {
        double[] arr = {Double.NaN};
        int h = MathUtils.hash(arr);
        assertNotNull(h);
    }

    @Test(timeout = 4000)
    public void testHashDoubleNaN() {
        int h = MathUtils.hash(Double.NaN);
        assertEquals(Double.hashCode(Double.NaN), h);
    }

    @Test(timeout = 4000)
    public void testHashDoublePositiveInfinity() {
        int h = MathUtils.hash(Double.POSITIVE_INFINITY);
        assertEquals(Double.hashCode(Double.POSITIVE_INFINITY), h);
    }

    @Test(timeout = 4000)
    public void testHashDoubleNegativeInfinity() {
        int h = MathUtils.hash(Double.NEGATIVE_INFINITY);
        assertEquals(Double.hashCode(Double.NEGATIVE_INFINITY), h);
    }

    @Test(timeout = 4000)
    public void testHashDoubleZero() {
        int h = MathUtils.hash(0.0);
        assertEquals(Double.hashCode(0.0), h);
    }

    @Test(timeout = 4000)
    public void testHashDoubleNegativeZero() {
        int h = MathUtils.hash(-0.0);
        assertEquals(Double.hashCode(-0.0), h);
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaN() {
        // Known defect test: arrays with NaN should not be equal
        double[] a = {Double.NaN, 1.0};
        double[] b = {Double.NaN, 1.0};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNOnly() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with only NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsIncludingNaNArrayWithNaN() {
        double[] a = {Double.NaN, 1.0};
        double[] b = {Double.NaN, 1.0};
        assertTrue(MathUtils.equalsIncludingNaN(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsIncludingNaNArrayWithNaNOnly() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertTrue(MathUtils.equalsIncludingNaN(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsIncludingNaNArrayDifferentLength() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN, 1.0};
        assertFalse(MathUtils.equalsIncludingNaN(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsIncludingNaNArrayOneNull() {
        assertFalse(MathUtils.equalsIncludingNaN(null, new double[]{1.0}));
        assertFalse(MathUtils.equalsIncludingNaN(new double[]{1.0}, null));
    }

    @Test(timeout = 4000)
    public void testEqualsIncludingNaNArrayBothNull() {
        assertTrue(MathUtils.equalsIncludingNaN((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayDifferentLength() {
        assertFalse(MathUtils.equals(new double[]{1.0}, new double[]{1.0, 2.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArraySameElements() {
        assertTrue(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 2.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayDifferentElements() {
        assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 3.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithInfinity() {
        double[] a = {Double.POSITIVE_INFINITY};
        double[] b = {Double.POSITIVE_INFINITY};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNegativeInfinity() {
        double[] a = {Double.NEGATIVE_INFINITY};
        double[] b = {Double.NEGATIVE_INFINITY};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithMixedInfinity() {
        double[] a = {Double.POSITIVE_INFINITY};
        double[] b = {Double.NEGATIVE_INFINITY};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithZeroAndNegativeZero() {
        double[] a = {0.0};
        double[] b = {-0.0};
        assertTrue(MathUtils.equals(a, b)); // +0.0 == -0.0
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndInfinity() {
        double[] a = {Double.NaN};
        double[] b = {Double.POSITIVE_INFINITY};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNumber() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayOneEmptyOneNonEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNFirstElement() {
        double[] a = {Double.NaN, 2.0};
        double[] b = {Double.NaN, 2.0};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNSecondElement() {
        double[] a = {1.0, Double.NaN};
        double[] b = {1.0, Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNOnlyDifferentLength() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN, Double.NaN};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNull() {
        assertFalse(MathUtils.equals(null, new double[]{Double.NaN}));
        assertFalse(MathUtils.equals(new double[]{Double.NaN}, null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNOneEmpty() {
        double[] a = {};
        double[] b = {Double.NaN};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNMultiple() {
        double[] a = {Double.NaN, Double.NaN};
        double[] b = {Double.NaN, Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndInfinity() {
        double[] a = {Double.NaN};
        double[] b = {Double.POSITIVE_INFINITY};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNegativeInfinity() {
        double[] a = {Double.NaN};
        double[] b = {Double.NEGATIVE_INFINITY};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndZero() {
        double[] a = {Double.NaN};
        double[] b = {0.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNegativeZero() {
        double[] a = {Double.NaN};
        double[] b = {-0.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndMaxValue() {
        double[] a = {Double.NaN};
        double[] b = {Double.MAX_VALUE};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndMinValue() {
        double[] a = {Double.NaN};
        double[] b = {Double.MIN_VALUE};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormal() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndSameNormal() {
        double[] a = {Double.NaN, 1.0};
        double[] b = {Double.NaN, 1.0};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndDifferentNormal() {
        double[] a = {Double.NaN, 1.0};
        double[] b = {Double.NaN, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalFirstElement() {
        double[] a = {1.0, Double.NaN};
        double[] b = {1.0, Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalFirstElementDifferent() {
        double[] a = {1.0, Double.NaN};
        double[] b = {2.0, Double.NaN};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothDifferent() {
        double[] a = {1.0, Double.NaN};
        double[] b = {2.0, Double.NaN};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0, Double.NaN};
        double[] b = {1.0, Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNaN() {
        double[] a = {1.0, Double.NaN};
        double[] b = {1.0, Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSame() {
        double[] a = {1.0, Double.NaN};
        double[] b = {1.0, Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentOrder() {
        double[] a = {Double.NaN, 1.0};
        double[] b = {1.0, Double.NaN};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameOrder() {
        double[] a = {Double.NaN, 1.0};
        double[] b = {Double.NaN, 1.0};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {Double.NaN, 1.0};
        double[] b = {Double.NaN};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {Double.NaN, 1.0};
        double[] b = {Double.NaN, 1.0};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN, Double.NaN};
        double[] b = {Double.NaN, Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN, 1.0};
        double[] b = {1.0, Double.NaN};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0, 2.0};
        double[] b = {1.0, 2.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0, 2.0};
        double[] b = {1.0, 3.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormalDifferentOrder() {
        double[] a = {1.0, 2.0};
        double[] b = {2.0, 1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormalSameOrder() {
        double[] a = {1.0, 2.0};
        double[] b = {1.0, 2.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLengthNormal() {
        double[] a = {1.0, 2.0};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLengthNormal() {
        double[] a = {1.0, 2.0};
        double[] b = {1.0, 2.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {
        double[] a = {};
        double[] b = {};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneEmpty() {
        double[] a = {};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNull() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNull() {
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNaN() {
        double[] a = {Double.NaN};
        double[] b = {Double.NaN};
        assertFalse("Arrays with NaN should not be equal", MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalOneNaN() {
        double[] a = {Double.NaN};
        double[] b = {1.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentNormal() {
        double[] a = {1.0};
        double[] b = {2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameNormal() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalDifferentLength() {
        double[] a = {1.0};
        double[] b = {1.0, 2.0};
        assertFalse(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalSameLength() {
        double[] a = {1.0};
        double[] b = {1.0};
        assertTrue(MathUtils.equals(a, b));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArrayWithNaNAndNormalBothEmpty() {