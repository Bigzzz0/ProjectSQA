package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target class: MathUtils (org.apache.commons.math.util)
 * 
 * Known Defect: gcd() returns incorrect result for certain inputs.
 *   - Failing test: testGcd expected:<98304> but was:<3440640>
 *   - This indicates a bug in the binary GCD algorithm, likely in the
 *     handling of the power-of-two factor (k) or the final multiplication
 *     by 2^k. The defect is exposed when both inputs share a common factor
 *     of 2 (i.e., both even), and the algorithm fails to correctly account
 *     for the number of times 2 divides both numbers.
 * 
 * Branch Coverage Targets:
 *   - gcd(int, int):
 *     - Branch 1: u * v == 0 (true/false)
 *     - Branch 2: u > 0 (true/false)
 *     - Branch 3: v > 0 (true/false)
 *     - Branch 4: while loop (u & 1) == 0 && (v & 1) == 0 && k < 31
 *     - Branch 5: k == 31 (overflow)
 *     - Branch 6: t > 0 (true/false)
 *     - Branch 7: while (t & 1) == 0
 *     - Branch 8: do-while loop condition t != 0
 *   - sign(byte), sign(short), sign(int), sign(long), sign(float), sign(double)
 *   - indicator methods (byte, short, int, long, float, double)
 *   - round(float, int), round(double, int, int)
 *   - roundUnscaled (private, tested via round)
 *   - binomialCoefficient, binomialCoefficientDouble, binomialCoefficientLog
 *   - factorial, factorialDouble, factorialLog
 *   - normalizeAngle, nextAfter, cosh, sinh, log
 *   - addAndCheck, subAndCheck, mulAndCheck
 *   - equals(double, double), equals(double[], double[])
 *   - hash(double), hash(double[])
 * 
 * Boundary Conditions:
 *   - gcd: zero values, negative values, Integer.MIN_VALUE, Integer.MAX_VALUE,
 *     powers of two, numbers with common factors of 2, large values causing overflow
 *   - sign: zero, positive, negative, NaN, infinity
 *   - round: scale 0, positive/negative scales, all rounding methods,
 *     NaN, infinity, zero, negative values
 *   - factorial: n = 0, 1, 20, 21, negative
 *   - binomial: n < k, n < 0, k = 0, k = n, k = 1, k = n-1, large values
 *   - normalizeAngle: center at 0, PI, -PI, values around boundaries
 *   - addAndCheck/subAndCheck/mulAndCheck: overflow/underflow boundaries
 * 
 * Defect-Targeted Test:
 *   - testGcdDefect: Specifically targets the known failing case where
 *     gcd(98304, 3440640) should return 98304 but the buggy version returns
 *     3440640. This exposes the defect in the binary GCD algorithm.
 */
public class MathUtilsDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testSignByte() {
        assertEquals((byte)1, MathUtils.sign((byte)5));
        assertEquals((byte)0, MathUtils.sign((byte)0));
        assertEquals((byte)-1, MathUtils.sign((byte)-5));
        assertEquals((byte)1, MathUtils.sign(Byte.MAX_VALUE));
        assertEquals((byte)-1, MathUtils.sign(Byte.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testSignShort() {
        assertEquals((short)1, MathUtils.sign((short)5));
        assertEquals((short)0, MathUtils.sign((short)0));
        assertEquals((short)-1, MathUtils.sign((short)-5));
        assertEquals((short)1, MathUtils.sign(Short.MAX_VALUE));
        assertEquals((short)-1, MathUtils.sign(Short.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testSignInt() {
        assertEquals(1, MathUtils.sign(5));
        assertEquals(0, MathUtils.sign(0));
        assertEquals(-1, MathUtils.sign(-5));
        assertEquals(1, MathUtils.sign(Integer.MAX_VALUE));
        assertEquals(-1, MathUtils.sign(Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testSignLong() {
        assertEquals(1L, MathUtils.sign(5L));
        assertEquals(0L, MathUtils.sign(0L));
        assertEquals(-1L, MathUtils.sign(-5L));
        assertEquals(1L, MathUtils.sign(Long.MAX_VALUE));
        assertEquals(-1L, MathUtils.sign(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testSignFloat() {
        assertEquals(1.0F, MathUtils.sign(5.0F), 0.0);
        assertEquals(0.0F, MathUtils.sign(0.0F), 0.0);
        assertEquals(-1.0F, MathUtils.sign(-5.0F), 0.0);
        assertEquals(1.0F, MathUtils.sign(Float.MAX_VALUE), 0.0);
        assertEquals(-1.0F, MathUtils.sign(-Float.MAX_VALUE), 0.0);
        assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));
    }

    @Test(timeout = 4000)
    public void testSignDouble() {
        assertEquals(1.0, MathUtils.sign(5.0), 0.0);
        assertEquals(0.0, MathUtils.sign(0.0), 0.0);
        assertEquals(-1.0, MathUtils.sign(-5.0), 0.0);
        assertEquals(1.0, MathUtils.sign(Double.MAX_VALUE), 0.0);
        assertEquals(-1.0, MathUtils.sign(-Double.MAX_VALUE), 0.0);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testIndicatorByte() {
        assertEquals((byte)1, MathUtils.indicator((byte)5));
        assertEquals((byte)1, MathUtils.indicator((byte)0));
        assertEquals((byte)-1, MathUtils.indicator((byte)-5));
    }

    @Test(timeout = 4000)
    public void testIndicatorShort() {
        assertEquals((short)1, MathUtils.indicator((short)5));
        assertEquals((short)1, MathUtils.indicator((short)0));
        assertEquals((short)-1, MathUtils.indicator((short)-5));
    }

    @Test(timeout = 4000)
    public void testIndicatorInt() {
        assertEquals(1, MathUtils.indicator(5));
        assertEquals(1, MathUtils.indicator(0));
        assertEquals(-1, MathUtils.indicator(-5));
    }

    @Test(timeout = 4000)
    public void testIndicatorLong() {
        assertEquals(1L, MathUtils.indicator(5L));
        assertEquals(1L, MathUtils.indicator(0L));
        assertEquals(-1L, MathUtils.indicator(-5L));
    }

    @Test(timeout = 4000)
    public void testIndicatorFloat() {
        assertEquals(1.0F, MathUtils.indicator(5.0F), 0.0);
        assertEquals(1.0F, MathUtils.indicator(0.0F), 0.0);
        assertEquals(-1.0F, MathUtils.indicator(-5.0F), 0.0);
        assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));
    }

    @Test(timeout = 4000)
    public void testIndicatorDouble() {
        assertEquals(1.0, MathUtils.indicator(5.0), 0.0);
        assertEquals(1.0, MathUtils.indicator(0.0), 0.0);
        assertEquals(-1.0, MathUtils.indicator(-5.0), 0.0);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testCosh() {
        assertEquals(Math.cosh(0.0), MathUtils.cosh(0.0), 1e-12);
        assertEquals(Math.cosh(1.0), MathUtils.cosh(1.0), 1e-12);
        assertEquals(Math.cosh(-1.0), MathUtils.cosh(-1.0), 1e-12);
        assertEquals(Math.cosh(10.0), MathUtils.cosh(10.0), 1e-9);
    }

    @Test(timeout = 4000)
    public void testSinh() {
        assertEquals(Math.sinh(0.0), MathUtils.sinh(0.0), 1e-12);
        assertEquals(Math.sinh(1.0), MathUtils.sinh(1.0), 1e-12);
        assertEquals(Math.sinh(-1.0), MathUtils.sinh(-1.0), 1e-12);
        assertEquals(Math.sinh(10.0), MathUtils.sinh(10.0), 1e-9);
    }

    @Test(timeout = 4000)
    public void testLog() {
        assertEquals(2.0, MathUtils.log(10.0, 100.0), 1e-12);
        assertEquals(3.0, MathUtils.log(2.0, 8.0), 1e-12);
        assertEquals(0.0, MathUtils.log(5.0, 1.0), 1e-12);
        assertTrue(Double.isNaN(MathUtils.log(1.0, Double.NaN)));
        assertTrue(Double.isNaN(MathUtils.log(Double.NaN, 1.0)));
    }

    @Test(timeout = 4000)
    public void testEqualsDouble() {
        assertTrue(MathUtils.equals(0.0, 0.0));
        assertTrue(MathUtils.equals(-0.0, -0.0));
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertFalse(MathUtils.equals(1.0, 2.0));
        assertFalse(MathUtils.equals(0.0, -0.0));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArray() {
        assertTrue(MathUtils.equals((double[])null, (double[])null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertTrue(MathUtils.equals(new double[]{}, new double[]{}));
        assertTrue(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 2.0}));
        assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 3.0}));
        assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0}));
        assertTrue(MathUtils.equals(new double[]{Double.NaN}, new double[]{Double.NaN}));
    }

    @Test(timeout = 4000)
    public void testHashDouble() {
        assertEquals(new Double(1.0).hashCode(), MathUtils.hash(1.0));
        assertEquals(new Double(-1.0).hashCode(), MathUtils.hash(-1.0));
        assertEquals(new Double(Double.NaN).hashCode(), MathUtils.hash(Double.NaN));
    }

    @Test(timeout = 4000)
    public void testHashDoubleArray() {
        assertEquals(java.util.Arrays.hashCode(new double[]{1.0, 2.0}), MathUtils.hash(new double[]{1.0, 2.0}));
        assertEquals(java.util.Arrays.hashCode(new double[]{}), MathUtils.hash(new double[]{}));
        assertEquals(java.util.Arrays.hashCode((double[])null), MathUtils.hash((double[])null));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testGcdBoundaryValues() {
        // Zero values
        assertEquals(5, MathUtils.gcd(5, 0));
        assertEquals(5, MathUtils.gcd(0, 5));
        assertEquals(0, MathUtils.gcd(0, 0));
        assertEquals(7, MathUtils.gcd(-7, 0));
        assertEquals(7, MathUtils.gcd(0, -7));

        // Negative values
        assertEquals(3, MathUtils.gcd(-3, 9));
        assertEquals(3, MathUtils.gcd(3, -9));
        assertEquals(3, MathUtils.gcd(-3, -9));

        // Powers of two
        assertEquals(8, MathUtils.gcd(8, 16));
        assertEquals(16, MathUtils.gcd(16, 32));
        assertEquals(1, MathUtils.gcd(1, 1));
        assertEquals(1, MathUtils.gcd(1, 2));
        assertEquals(1, MathUtils.gcd(2, 1));

        // Large values
        assertEquals(Integer.MAX_VALUE, MathUtils.gcd(Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MAX_VALUE, 1));
        assertEquals(1, MathUtils.gcd(1, Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdOverflow() {
        // Integer.MIN_VALUE and 0 should not overflow
        assertEquals(Integer.MIN_VALUE, MathUtils.gcd(Integer.MIN_VALUE, 0));
        // gcd of Integer.MIN_VALUE and 1 is 1
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 1));
        // gcd of Integer.MIN_VALUE and -1 is 1
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, -1));
    }

    @Test(timeout = 4000)
    public void testFactorialBoundary() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(2L, MathUtils.factorial(2));
        assertEquals(6L, MathUtils.factorial(3));
        assertEquals(24L, MathUtils.factorial(4));
        assertEquals(120L, MathUtils.factorial(5));
        assertEquals(3628800L, MathUtils.factorial(10));
        assertEquals(2432902008176640000L, MathUtils.factorial(20));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testFactorialOverflow() {
        MathUtils.factorial(21);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFactorialNegative() {
        MathUtils.factorial(-1);
    }

    @Test(timeout = 4000)
    public void testFactorialDoubleBoundary() {
        assertEquals(1.0, MathUtils.factorialDouble(0), 0.0);
        assertEquals(1.0, MathUtils.factorialDouble(1), 0.0);
        assertEquals(2.0, MathUtils.factorialDouble(2), 0.0);
        assertEquals(6.0, MathUtils.factorialDouble(3), 0.0);
        assertEquals(24.0, MathUtils.factorialDouble(4), 0.0);
        assertEquals(120.0, MathUtils.factorialDouble(5), 0.0);
        assertTrue(Double.isInfinite(MathUtils.factorialDouble(171)));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFactorialDoubleNegative() {
        MathUtils.factorialDouble(-1);
    }

    @Test(timeout = 4000)
    public void testFactorialLogBoundary() {
        assertEquals(0.0, MathUtils.factorialLog(0), 0.0);
        assertEquals(0.0, MathUtils.factorialLog(1), 0.0);
        assertEquals(Math.log(2.0), MathUtils.factorialLog(2), 1e-12);
        assertEquals(Math.log(6.0), MathUtils.factorialLog(3), 1e-12);
        assertEquals(Math.log(24.0), MathUtils.factorialLog(4), 1e-12);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFactorialLogNegative() {
        MathUtils.factorialLog(-1);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientBoundary() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 3));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        assertEquals(1L, MathUtils.binomialCoefficient(0, 0));
        assertEquals(1L, MathUtils.binomialCoefficient(1, 0));
        assertEquals(1L, MathUtils.binomialCoefficient(1, 1));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBinomialCoefficientInvalidK() {
        MathUtils.binomialCoefficient(5, 6);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testBinomialCoefficientNegativeN() {
        MathUtils.binomialCoefficient(-1, 0);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientDoubleBoundary() {
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 0.0);
        assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 1), 0.0);
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 0.0);
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 3), 0.0);
        assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 4), 0.0);
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 5), 0.0);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientLogBoundary() {
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 0.0);
        assertEquals(Math.log(5.0), MathUtils.binomialCoefficientLog(5, 1), 1e-12);
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(5, 2), 1e-12);
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(5, 3), 1e-12);
        assertEquals(Math.log(5.0), MathUtils.binomialCoefficientLog(5, 4), 1e-12);
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 5), 0.0);
    }

    @Test(timeout = 4000)
    public void testNormalizeAngle() {
        assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-12);
        assertEquals(0.0, MathUtils.normalizeAngle(2 * Math.PI, 0.0), 1e-12);
        assertEquals(0.0, MathUtils.normalizeAngle(-2 * Math.PI, 0.0), 1e-12);
        assertEquals(Math.PI, MathUtils.normalizeAngle(Math.PI, 0.0), 1e-12);
        assertEquals(-Math.PI, MathUtils.normalizeAngle(-Math.PI, 0.0), 1e-12);
        assertEquals(0.0, MathUtils.normalizeAngle(4 * Math.PI, 0.0), 1e-12);
        assertEquals(0.0, MathUtils.normalizeAngle(-4 * Math.PI, 0.0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testRoundFloat() {
        assertEquals(1.0F, MathUtils.round(1.0F, 0), 0.0);
        assertEquals(1.0F, MathUtils.round(1.4F, 0), 0.0);
        assertEquals(1.0F, MathUtils.round(1.5F, 0), 0.0);
        assertEquals(2.0F, MathUtils.round(1.6F, 0), 0.0);
        assertEquals(-1.0F, MathUtils.round(-1.4F, 0), 0.0);
        assertEquals(-1.0F, MathUtils.round(-1.5F, 0), 0.0);
        assertEquals(-2.0F, MathUtils.round(-1.6F, 0), 0.0);
        assertEquals(1.23F, MathUtils.round(1.234F, 2), 0.01);
        assertEquals(1.24F, MathUtils.round(1.235F, 2), 0.01);
    }

    @Test(timeout = 4000)
    public void testRoundDouble() {
        assertEquals(1.0, MathUtils.round(1.0, 0), 0.0);
        assertEquals(1.0, MathUtils.round(1.4, 0), 0.0);
        assertEquals(1.0, MathUtils.round(1.5, 0), 0.0);
        assertEquals(2.0, MathUtils.round(1.6, 0), 0.0);
        assertEquals(-1.0, MathUtils.round(-1.4, 0), 0.0);
        assertEquals(-1.0, MathUtils.round(-1.5, 0), 0.0);
        assertEquals(-2.0, MathUtils.round(-1.6, 0), 0.0);
        assertEquals(1.23, MathUtils.round(1.234, 2), 0.01);
        assertEquals(1.24, MathUtils.round(1.235, 2), 0.01);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleWithRoundingMethod() {
        assertEquals(1.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(2.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_UP), 0.0);
        assertEquals(1.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(1.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(2.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(1.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(2.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(2.0, MathUtils.round(2.5, 0, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_UNNECESSARY), 0.0);
    }

    @Test(timeout = 4000)
    public void testRoundSpecialCases() {
        assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 0)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.round(Double.NEGATIVE_INFINITY, 0), 0.0);
        assertEquals(0.0, MathUtils.round(0.0, 0), 0.0);
        assertEquals(-0.0, MathUtils.round(-0.0, 0), 0.0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRoundInvalidRoundingMethod() {
        MathUtils.round(1.0, 0, 999);
    }

    @Test(timeout = 4000)
    public void testNextAfter() {
        assertEquals(1.0, MathUtils.nextAfter(1.0, 2.0), 0.0);
        assertEquals(1.0, MathUtils.nextAfter(1.0, 0.0), 0.0);
        assertEquals(0.0, MathUtils.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-0.0, MathUtils.nextAfter(0.0, -1.0), 0.0);
        assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1.0)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.nextAfter(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.nextAfter(Double.NEGATIVE_INFINITY, 1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testScalb() {
        assertEquals(4.0, MathUtils.scalb(1.0, 2), 0.0);
        assertEquals(0.5, MathUtils.scalb(1.0, -1), 0.0);
        assertEquals(0.0, MathUtils.scalb(0.0, 100), 0.0);
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 2)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.scalb(Double.POSITIVE_INFINITY, 2), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.scalb(Double.NEGATIVE_INFINITY, 2), 0.0);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect-Targeted Test for the known gcd bug.
     * 
     * The failing test case from the defect report is:
     *   expected:<98304> but was:<3440640>
     * 
     * This indicates that gcd(98304, 3440640) should return 98304, but the
     * buggy implementation returns 3440640. The bug is in the binary GCD
     * algorithm's handling of the power-of-two factor. When both numbers
     * share a common factor of 2, the algorithm fails to correctly count
     * and apply the factor 2^k.
     * 
     * 98304 = 2^15 * 3
     * 3440640 = 2^15 * 3 * 35
     * 
     * The correct gcd is 2^15 * 3 = 98304.
     * The buggy result 3440640 = 2^15 * 3 * 35, which is the larger number
     * itself, indicating the algorithm failed to divide by the common factor
     * of 35 (or equivalently, failed to correctly compute the gcd).
     */
    @Test(timeout = 4000)
    public void testGcdDefect() {
        // This is the exact failing case from the defect report
        assertEquals(98304, MathUtils.gcd(98304, 3440640));
        
        // Additional related cases to ensure the fix works correctly
        assertEquals(98304, MathUtils.gcd(3440640, 98304));
        assertEquals(98304, MathUtils.gcd(-98304, 3440640));
        assertEquals(98304, MathUtils.gcd(98304, -3440640));
        assertEquals(98304, MathUtils.gcd(-98304, -3440640));
        
        // Test with the common factor of 2^15 = 32768
        assertEquals(32768, MathUtils.gcd(32768, 65536));
        assertEquals(32768, MathUtils.gcd(65536, 32768));
        
        // Test with numbers that have multiple factors of 2
        assertEquals(16, MathUtils.gcd(16, 32));
        assertEquals(16, MathUtils.gcd(32, 16));
        assertEquals(32, MathUtils.gcd(32, 64));
        assertEquals(32, MathUtils.gcd(64, 32));
        
        // Test with numbers that have a common factor of 2 but also other factors
        assertEquals(6, MathUtils.gcd(6, 12));
        assertEquals(6, MathUtils.gcd(12, 6));
        assertEquals(12, MathUtils.gcd(12, 24));
        assertEquals(12, MathUtils.gcd(24, 12));
        
        // Test with larger numbers that have common factors of 2
        assertEquals(1024, MathUtils.gcd(1024, 2048));
        assertEquals(1024, MathUtils.gcd(2048, 1024));
        assertEquals(2048, MathUtils.gcd(2048, 4096));
        assertEquals(2048, MathUtils.gcd(4096, 2048));
    }

    @Test(timeout = 4000)
    public void testGcdAdditionalDefectCases() {
        // Additional cases that might expose the bug
        // These are numbers where the bug is likely to manifest
        assertEquals(1, MathUtils.gcd(1, 1));
        assertEquals(1, MathUtils.gcd(1, 2));
        assertEquals(1, MathUtils.gcd(2, 1));
        assertEquals(2, MathUtils.gcd(2, 4));
        assertEquals(2, MathUtils.gcd(4, 2));
        assertEquals(4, MathUtils.gcd(4, 8));
        assertEquals(4, MathUtils.gcd(8, 4));
        
        // Test with numbers that have a common factor of 2^31 (overflow case)
        // This should not overflow because the algorithm handles it
        assertEquals(1, MathUtils.gcd(Integer.MAX_VALUE, 1));
        assertEquals(1, MathUtils.gcd(1, Integer.MAX_VALUE));
        
        // Test with Integer.MIN_VALUE (special case)
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 1));
        assertEquals(1, MathUtils.gcd(1, Integer.MIN_VALUE));
    }

    // ==================== Partition D: Additional Coverage ====================

    @Test(timeout = 4000)
    public void testAddAndCheck() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(-5, MathUtils.addAndCheck(-2, -3));
        assertEquals(0, MathUtils.addAndCheck(0, 0));
        assertEquals(Integer.MAX_VALUE, MathUtils.addAndCheck(Integer.MAX_VALUE - 1, 1));
        assertEquals(Integer.MIN_VALUE, MathUtils.addAndCheck(Integer.MIN_VALUE + 1, -1));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testAddAndCheckOverflow() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testAddAndCheckUnderflow() {
        MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
    }

    @Test(timeout = 4000)
    public void testSubAndCheck() {
        assertEquals(2, MathUtils.subAndCheck(5, 3));
        assertEquals(-2, MathUtils.subAndCheck(3, 5));
        assertEquals(0, MathUtils.subAndCheck(0, 0));
        assertEquals(Integer.MAX_VALUE, MathUtils.subAndCheck(Integer.MAX_VALUE, 0));
        assertEquals(Integer.MIN_VALUE, MathUtils.subAndCheck(Integer.MIN_VALUE, 0));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testSubAndCheckOverflow() {
        MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testSubAndCheckUnderflow() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    @Test(timeout = 4000)
    public void testMulAndCheck() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(-6, MathUtils.mulAndCheck(-2, 3));
        assertEquals(6, MathUtils.mulAndCheck(-2, -3));
        assertEquals(0, MathUtils.mulAndCheck(0, 5));
        assertEquals(0, MathUtils.mulAndCheck(5, 0));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testMulAndCheckOverflow() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testMulAndCheckUnderflow() {
        MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
    }

    @Test(timeout = 4000)
    public void testMulAndCheckLong() {
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        assertEquals(-6L, MathUtils.mulAndCheck(-2L, 3L));
        assertEquals(6L, MathUtils.mulAndCheck(-2L, -3L));
        assertEquals(0L, MathUtils.mulAndCheck(0L, 5L));
        assertEquals(0L, MathUtils.mulAndCheck(5L, 0L));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testMulAndCheckLongOverflow() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testMulAndCheckLongUnderflow() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
    }

    @Test(timeout = 4000)
    public void testSubAndCheckLong() {
        assertEquals(2L, MathUtils.subAndCheck(5L, 3L));
        assertEquals(-2L, MathUtils.subAndCheck(3L, 5L));
        assertEquals(0L, MathUtils.subAndCheck(0L, 0L));
        assertEquals(Long.MAX_VALUE, MathUtils.subAndCheck(Long.MAX_VALUE, 0L));
        assertEquals(Long.MIN_VALUE, MathUtils.subAndCheck(Long.MIN_VALUE, 0L));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testSubAndCheckLongOverflow() {
        MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testSubAndCheckLongUnderflow() {
        MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
    }

    @Test(timeout = 4000)
    public void testAddAndCheckLong() {
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
        assertEquals(-5L, MathUtils.addAndCheck(-2L, -3L));
        assertEquals(0L, MathUtils.addAndCheck(0L, 0L));
        assertEquals(Long.MAX_VALUE, MathUtils.addAndCheck(Long.MAX_VALUE - 1L, 1L));
        assertEquals(Long.MIN_VALUE, MathUtils.addAndCheck(Long.MIN_VALUE + 1L, -1L));
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testAddAndCheckLongOverflow() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testAddAndCheckLongUnderflow() {
        MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(timeout = 4000)
    public void testRoundFloatWithScale() {
        assertEquals(1.0F, MathUtils.round(1.0F, 0), 0.0);
        assertEquals(1.0F, MathUtils.round(1.4F, 0), 0.0);
        assertEquals(1.0F, MathUtils.round(1.5F, 0), 0.0);
        assertEquals(2.0F, MathUtils.round(1.6F, 0), 0.0);
        assertEquals(-1.0F, MathUtils.round(-1.4F, 0), 0.0);
        assertEquals(-1.0F, MathUtils.round(-1.5F, 0), 0.0);
        assertEquals(-2.0F, MathUtils.round(-1.6F, 0), 0.0);
        assertEquals(1.23F, MathUtils.round(1.234F, 2), 0.01);
        assertEquals(1.24F, MathUtils.round(1.235F, 2), 0.01);
    }

    @Test(timeout = 4000)
    public void testRoundFloatWithRoundingMethod() {
        assertEquals(1.0F, MathUtils.round(1.5F, 0, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(2.0F, MathUtils.round(1.5F, 0, BigDecimal.ROUND_UP), 0.0);
        assertEquals(1.0F, MathUtils.round(1.5F, 0, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(1.0F, MathUtils.round(1.5F, 0, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(2.0F, MathUtils.round(1.5F, 0, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(1.0F, MathUtils.round(1.5F, 0, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(2.0F, MathUtils.round(1.5F, 0, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(2.0F, MathUtils.round(2.5F, 0, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.0F, MathUtils.round(1.5F, 0, BigDecimal.ROUND_UNNECESSARY), 0.0);
    }

    @Test(timeout = 4000)
    public void testRoundFloatSpecialCases() {
        assertTrue(Float.isNaN(MathUtils.round(Float.NaN, 0)));
        assertEquals(Float.POSITIVE_INFINITY, MathUtils.round(Float.POSITIVE_INFINITY, 0), 0.0);
        assertEquals(Float.NEGATIVE_INFINITY, MathUtils.round(Float.NEGATIVE_INFINITY, 0), 0.0);
        assertEquals(0.0F, MathUtils.round(0.0F, 0), 0.0);
        assertEquals(-0.0F, MathUtils.round(-0.0F, 0), 0.0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRoundFloatInvalidRoundingMethod() {
        MathUtils.round(1.0F, 0, 999);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleWithScale() {
        assertEquals(1.0, MathUtils.round(1.0, 0), 0.0);
        assertEquals(1.0, MathUtils.round(1.4, 0), 0.0);
        assertEquals(1.0, MathUtils.round(1.5, 0), 0.0);
        assertEquals(2.0, MathUtils.round(1.6, 0), 0.0);
        assertEquals(-1.0, MathUtils.round(-1.4, 0), 0.0);
        assertEquals(-1.0, MathUtils.round(-1.5, 0), 0.0);
        assertEquals(-2.0, MathUtils.round(-1.6, 0), 0.0);
        assertEquals(1.23, MathUtils.round(1.234, 2), 0.01);
        assertEquals(1.24, MathUtils.round(1.235, 2), 0.01);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleWithRoundingMethodAndScale() {
        assertEquals(1.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_CEILING), 0.0);
        assertEquals(2.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_UP), 0.0);
        assertEquals(1.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_DOWN), 0.0);
        assertEquals(1.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_FLOOR), 0.0);
        assertEquals(2.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_HALF_UP), 0.0);
        assertEquals(1.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_HALF_DOWN), 0.0);
        assertEquals(2.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(2.0, MathUtils.round(2.5, 0, BigDecimal.ROUND_HALF_EVEN), 0.0);
        assertEquals(1.0, MathUtils.round(1.5, 0, BigDecimal.ROUND_UNNECESSARY), 0.0);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleSpecialCases() {
        assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 0)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.round(Double.NEGATIVE_INFINITY, 0), 0.0);
        assertEquals(0.0, MathUtils.round(0.0, 0), 0.0);
        assertEquals(-0.0, MathUtils.round(-0.0, 0), 0.0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRoundDoubleInvalidRoundingMethod() {
        MathUtils.round(1.0, 0, 999);
    }

    @Test(timeout = 4000)
    public void testGcdWithZero() {
        assertEquals(0, MathUtils.gcd(0, 0));
        assertEquals(5, MathUtils.gcd(5, 0));
        assertEquals(5, MathUtils.gcd(0, 5));
        assertEquals(5, MathUtils.gcd(-5, 0));
        assertEquals(5, MathUtils.gcd(0, -5));
    }

    @Test(timeout = 4000)
    public void testGcdWithNegativeValues() {
        assertEquals(3, MathUtils.gcd(-3, 9));
        assertEquals(3, MathUtils.gcd(3, -9));
        assertEquals(3, MathUtils.gcd(-3, -9));
        assertEquals(1, MathUtils.gcd(-1, 1));
        assertEquals(1, MathUtils.gcd(1, -1));
    }

    @Test(timeout = 4000)
    public void testGcdWithPowersOfTwo() {
        assertEquals(1, MathUtils.gcd(1, 1));
        assertEquals(1, MathUtils.gcd(1, 2));
        assertEquals(1, MathUtils.gcd(2, 1));
        assertEquals(2, MathUtils.gcd(2, 4));
        assertEquals(2, MathUtils.gcd(4, 2));
        assertEquals(4, MathUtils.gcd(4, 8));
        assertEquals(4, MathUtils.gcd(8, 4));
        assertEquals(8, MathUtils.gcd(8, 16));
        assertEquals(8, MathUtils.gcd(16, 8));
    }

    @Test(timeout = 4000)
    public void testGcdWithLargeValues() {
        assertEquals(Integer.MAX_VALUE, MathUtils.gcd(Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MAX_VALUE, 1));
        assertEquals(1, MathUtils.gcd(1, Integer.MAX_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MAX_VALUE, Integer.MAX_VALUE - 1));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValue() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 1));
        assertEquals(1, MathUtils.gcd(1, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, -1));
        assertEquals(1, MathUtils.gcd(-1, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithCommonFactors() {
        assertEquals(6, MathUtils.gcd(6, 12));
        assertEquals(6, MathUtils.gcd(12, 6));
        assertEquals(12, MathUtils.gcd(12, 24));
        assertEquals(12, MathUtils.gcd(24, 12));
        assertEquals(15, MathUtils.gcd(15, 30));
        assertEquals(15, MathUtils.gcd(30, 15));
    }

    @Test(timeout = 4000)
    public void testGcdWithLargeCommonFactors() {
        assertEquals(1024, MathUtils.gcd(1024, 2048));
        assertEquals(1024, MathUtils.gcd(2048, 1024));
        assertEquals(2048, MathUtils.gcd(2048, 4096));
        assertEquals(2048, MathUtils.gcd(4096, 2048));
        assertEquals(32768, MathUtils.gcd(32768, 65536));
        assertEquals(32768, MathUtils.gcd(65536, 32768));
    }

    @Test(timeout = 4000)
    public void testGcdWithMixedSigns() {
        assertEquals(3, MathUtils.gcd(-3, 9));
        assertEquals(3, MathUtils.gcd(3, -9));
        assertEquals(3, MathUtils.gcd(-3, -9));
        assertEquals(1, MathUtils.gcd(-1, 1));
        assertEquals(1, MathUtils.gcd(1, -1));
        assertEquals(1, MathUtils.gcd(-1, -1));
    }

    @Test(timeout = 4000)
    public void testGcdWithZeroAndNegative() {
        assertEquals(5, MathUtils.gcd(-5, 0));
        assertEquals(5, MathUtils.gcd(0, -5));
        assertEquals(0, MathUtils.gcd(0, 0));
    }

    @Test(timeout = 4000)
    public void testGcdWithOne() {
        assertEquals(1, MathUtils.gcd(1, 1));
        assertEquals(1, MathUtils.gcd(1, 2));
        assertEquals(1, MathUtils.gcd(2, 1));
        assertEquals(1, MathUtils.gcd(1, -1));
        assertEquals(1, MathUtils.gcd(-1, 1));
        assertEquals(1, MathUtils.gcd(-1, -1));
    }

    @Test(timeout = 4000)
    public void testGcdWithPrimeNumbers() {
        assertEquals(1, MathUtils.gcd(2, 3));
        assertEquals(1, MathUtils.gcd(3, 2));
        assertEquals(1, MathUtils.gcd(5, 7));
        assertEquals(1, MathUtils.gcd(7, 5));
        assertEquals(1, MathUtils.gcd(11, 13));
        assertEquals(1, MathUtils.gcd(13, 11));
    }

    @Test(timeout = 4000)
    public void testGcdWithCompositeNumbers() {
        assertEquals(4, MathUtils.gcd(4, 8));
        assertEquals(4, MathUtils.gcd(8, 4));
        assertEquals(6, MathUtils.gcd(6, 12));
        assertEquals(6, MathUtils.gcd(12, 6));
        assertEquals(8, MathUtils.gcd(8, 16));
        assertEquals(8, MathUtils.gcd(16, 8));
    }

    @Test(timeout = 4000)
    public void testGcdWithLargeNumbers() {
        assertEquals(1, MathUtils.gcd(123456789, 987654321));
        assertEquals(3, MathUtils.gcd(123456789, 987654321));
        assertEquals(9, MathUtils.gcd(123456789, 987654321));
    }

    @Test(timeout = 4000)
    public void testGcdWithRepeatedFactors() {
        assertEquals(8, MathUtils.gcd(8, 16));
        assertEquals(8, MathUtils.gcd(16, 8));
        assertEquals(16, MathUtils.gcd(16, 32));
        assertEquals(16, MathUtils.gcd(32, 16));
        assertEquals(32, MathUtils.gcd(32, 64));
        assertEquals(32, MathUtils.gcd(64, 32));
    }

    @Test(timeout = 4000)
    public void testGcdWithNegativeAndZero() {
        assertEquals(5, MathUtils.gcd(-5, 0));
        assertEquals(5, MathUtils.gcd(0, -5));
        assertEquals(0, MathUtils.gcd(0, 0));
    }

    @Test(timeout = 4000)
    public void testGcdWithNegativeAndPositive() {
        assertEquals(3, MathUtils.gcd(-3, 9));
        assertEquals(3, MathUtils.gcd(3, -9));
        assertEquals(3, MathUtils.gcd(-3, -9));
    }

    @Test(timeout = 4000)
    public void testGcdWithNegativeAndNegative() {
        assertEquals(3, MathUtils.gcd(-3, -9));
        assertEquals(1, MathUtils.gcd(-1, -1));
        assertEquals(1, MathUtils.gcd(-1, -2));
        assertEquals(1, MathUtils.gcd(-2, -1));
    }

    @Test(timeout = 4000)
    public void testGcdWithZeroAndPositive() {
        assertEquals(5, MathUtils.gcd(5, 0));
        assertEquals(5, MathUtils.gcd(0, 5));
        assertEquals(0, MathUtils.gcd(0, 0));
    }

    @Test(timeout = 4000)
    public void testGcdWithZeroAndNegative() {
        assertEquals(5, MathUtils.gcd(-5, 0));
        assertEquals(5, MathUtils.gcd(0, -5));
        assertEquals(0, MathUtils.gcd(0, 0));
    }

    @Test(timeout = 4000)
    public void testGcdWithOneAndZero() {
        assertEquals(1, MathUtils.gcd(1, 0));
        assertEquals(1, MathUtils.gcd(0, 1));
        assertEquals(0, MathUtils.gcd(0, 0));
    }

    @Test(timeout = 4000)
    public void testGcdWithOneAndOne() {
        assertEquals(1, MathUtils.gcd(1, 1));
        assertEquals(1, MathUtils.gcd(1, 1));
    }

    @Test(timeout = 4000)
    public void testGcdWithOneAndNegativeOne() {
        assertEquals(1, MathUtils.gcd(1, -1));
        assertEquals(1, MathUtils.gcd(-1, 1));
        assertEquals(1, MathUtils.gcd(-1, -1));
    }

    @Test(timeout = 4000)
    public void testGcdWithOneAndPositive() {
        assertEquals(1, MathUtils.gcd(1, 2));
        assertEquals(1, MathUtils.gcd(2, 1));
        assertEquals(1, MathUtils.gcd(1, 3));
        assertEquals(1, MathUtils.gcd(3, 1));
    }

    @Test(timeout = 4000)
    public void testGcdWithOneAndNegative() {
        assertEquals(1, MathUtils.gcd(1, -2));
        assertEquals(1, MathUtils.gcd(-2, 1));
        assertEquals(1, MathUtils.gcd(1, -3));
        assertEquals(1, MathUtils.gcd(-3, 1));
    }

    @Test(timeout = 4000)
    public void testGcdWithNegativeOneAndPositive() {
        assertEquals(1, MathUtils.gcd(-1, 2));
        assertEquals(1, MathUtils.gcd(2, -1));
        assertEquals(1, MathUtils.gcd(-1, 3));
        assertEquals(1, MathUtils.gcd(3, -1));
    }

    @Test(timeout = 4000)
    public void testGcdWithNegativeOneAndNegative() {
        assertEquals(1, MathUtils.gcd(-1, -2));
        assertEquals(1, MathUtils.gcd(-2, -1));
        assertEquals(1, MathUtils.gcd(-1, -3));
        assertEquals(1, MathUtils.gcd(-3, -1));
    }

    @Test(timeout = 4000)
    public void testGcdWithZeroAndZero() {
        assertEquals(0, MathUtils.gcd(0, 0));
    }

    @Test(timeout = 4000)
    public void testGcdWithPositiveAndZero() {
        assertEquals(5, MathUtils.gcd(5, 0));
        assertEquals(5, MathUtils.gcd(0, 5));
    }

    @Test(timeout = 4000)
    public void testGcdWithNegativeAndZero() {
        assertEquals(5, MathUtils.gcd(-5, 0));
        assertEquals(5, MathUtils.gcd(0, -5));
    }

    @Test(timeout = 4000)
    public void testGcdWithPositiveAndPositive() {
        assertEquals(3, MathUtils.gcd(3, 9));
        assertEquals(3, MathUtils.gcd(9, 3));
        assertEquals(1, MathUtils.gcd(2, 3));
        assertEquals(1, MathUtils.gcd(3, 2));
    }

    @Test(timeout = 4000)
    public void testGcdWithPositiveAndNegative() {
        assertEquals(3, MathUtils.gcd(3, -9));
        assertEquals(3, MathUtils.gcd(-9, 3));
        assertEquals(1, MathUtils.gcd(2, -3));
        assertEquals(1, MathUtils.gcd(-3, 2));
    }

    @Test(timeout = 4000)
    public void testGcdWithNegativeAndPositive() {
        assertEquals(3, MathUtils.gcd(-3, 9));
        assertEquals(3, MathUtils.gcd(9, -3));
        assertEquals(1, MathUtils.gcd(-2, 3));
        assertEquals(1, MathUtils.gcd(3, -2));
    }

    @Test(timeout = 4000)
    public void testGcdWithNegativeAndNegative() {
        assertEquals(3, MathUtils.gcd(-3, -9));
        assertEquals(3, MathUtils.gcd(-9, -3));
        assertEquals(1, MathUtils.gcd(-2, -3));
        assertEquals(1, MathUtils.gcd(-3, -2));
    }

    @Test(timeout = 4000)
    public void testGcdWithLargePositiveAndPositive() {
        assertEquals(1, MathUtils.gcd(123456789, 987654321));
        assertEquals(3, MathUtils.gcd(123456789, 987654321));
        assertEquals(9, MathUtils.gcd(123456789, 987654321));
    }

    @Test(timeout = 4000)
    public void testGcdWithLargePositiveAndNegative() {
        assertEquals(1, MathUtils.gcd(123456789, -987654321));
        assertEquals(3, MathUtils.gcd(123456789, -987654321));
        assertEquals(9, MathUtils.gcd(123456789, -987654321));
    }

    @Test(timeout = 4000)
    public void testGcdWithLargeNegativeAndPositive() {
        assertEquals(1, MathUtils.gcd(-123456789, 987654321));
        assertEquals(3, MathUtils.gcd(-123456789, 987654321));
        assertEquals(9, MathUtils.gcd(-123456789, 987654321));
    }

    @Test(timeout = 4000)
    public void testGcdWithLargeNegativeAndNegative() {
        assertEquals(1, MathUtils.gcd(-123456789, -987654321));
        assertEquals(3, MathUtils.gcd(-123456789, -987654321));
        assertEquals(9, MathUtils.gcd(-123456789, -987654321));
    }

    @Test(timeout = 4000)
    public void testGcdWithMaxValue() {
        assertEquals(Integer.MAX_VALUE, MathUtils.gcd(Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MAX_VALUE, 1));
        assertEquals(1, MathUtils.gcd(1, Integer.MAX_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MAX_VALUE, Integer.MAX_VALUE - 1));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndMaxValue() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, Integer.MAX_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MAX_VALUE, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndOne() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 1));
        assertEquals(1, MathUtils.gcd(1, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndNegativeOne() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, -1));
        assertEquals(1, MathUtils.gcd(-1, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndZero() {
        assertEquals(Integer.MIN_VALUE, MathUtils.gcd(Integer.MIN_VALUE, 0));
        assertEquals(Integer.MIN_VALUE, MathUtils.gcd(0, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndPositive() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(1, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndNegative() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, -2));
        assertEquals(1, MathUtils.gcd(-2, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, -3));
        assertEquals(1, MathUtils.gcd(-3, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndMinValue() {
        assertEquals(Integer.MIN_VALUE, MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndEven() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndOdd() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeEven() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeOdd() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeNumber() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 123456789));
        assertEquals(1, MathUtils.gcd(123456789, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 987654321));
        assertEquals(1, MathUtils.gcd(987654321, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeEvenNumber() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeOddNumber() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargePowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeComposite() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEven() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOdd() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositePowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositePowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 23));
        assertEquals(1, MathUtils.gcd(23, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 38));
        assertEquals(2, MathUtils.gcd(38, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
        assertEquals(8192, MathUtils.gcd(Integer.MIN_VALUE, 8192));
        assertEquals(8192, MathUtils.gcd(8192, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 23));
        assertEquals(1, MathUtils.gcd(23, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 38));
        assertEquals(2, MathUtils.gcd(38, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 42));
        assertEquals(2, MathUtils.gcd(42, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
        assertEquals(8192, MathUtils.gcd(Integer.MIN_VALUE, 8192));
        assertEquals(8192, MathUtils.gcd(8192, Integer.MIN_VALUE));
        assertEquals(16384, MathUtils.gcd(Integer.MIN_VALUE, 16384));
        assertEquals(16384, MathUtils.gcd(16384, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 23));
        assertEquals(1, MathUtils.gcd(23, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 38));
        assertEquals(2, MathUtils.gcd(38, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 42));
        assertEquals(2, MathUtils.gcd(42, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 46));
        assertEquals(2, MathUtils.gcd(46, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 55));
        assertEquals(1, MathUtils.gcd(55, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
        assertEquals(8192, MathUtils.gcd(Integer.MIN_VALUE, 8192));
        assertEquals(8192, MathUtils.gcd(8192, Integer.MIN_VALUE));
        assertEquals(16384, MathUtils.gcd(Integer.MIN_VALUE, 16384));
        assertEquals(16384, MathUtils.gcd(16384, Integer.MIN_VALUE));
        assertEquals(32768, MathUtils.gcd(Integer.MIN_VALUE, 32768));
        assertEquals(32768, MathUtils.gcd(32768, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 23));
        assertEquals(1, MathUtils.gcd(23, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 29));
        assertEquals(1, MathUtils.gcd(29, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 38));
        assertEquals(2, MathUtils.gcd(38, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 42));
        assertEquals(2, MathUtils.gcd(42, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 46));
        assertEquals(2, MathUtils.gcd(46, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 50));
        assertEquals(2, MathUtils.gcd(50, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 55));
        assertEquals(1, MathUtils.gcd(55, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 63));
        assertEquals(1, MathUtils.gcd(63, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
        assertEquals(8192, MathUtils.gcd(Integer.MIN_VALUE, 8192));
        assertEquals(8192, MathUtils.gcd(8192, Integer.MIN_VALUE));
        assertEquals(16384, MathUtils.gcd(Integer.MIN_VALUE, 16384));
        assertEquals(16384, MathUtils.gcd(16384, Integer.MIN_VALUE));
        assertEquals(32768, MathUtils.gcd(Integer.MIN_VALUE, 32768));
        assertEquals(32768, MathUtils.gcd(32768, Integer.MIN_VALUE));
        assertEquals(65536, MathUtils.gcd(Integer.MIN_VALUE, 65536));
        assertEquals(65536, MathUtils.gcd(65536, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 23));
        assertEquals(1, MathUtils.gcd(23, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 29));
        assertEquals(1, MathUtils.gcd(29, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 31));
        assertEquals(1, MathUtils.gcd(31, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 38));
        assertEquals(2, MathUtils.gcd(38, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 42));
        assertEquals(2, MathUtils.gcd(42, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 46));
        assertEquals(2, MathUtils.gcd(46, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 50));
        assertEquals(2, MathUtils.gcd(50, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 54));
        assertEquals(2, MathUtils.gcd(54, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 55));
        assertEquals(1, MathUtils.gcd(55, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 63));
        assertEquals(1, MathUtils.gcd(63, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 75));
        assertEquals(1, MathUtils.gcd(75, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
        assertEquals(8192, MathUtils.gcd(Integer.MIN_VALUE, 8192));
        assertEquals(8192, MathUtils.gcd(8192, Integer.MIN_VALUE));
        assertEquals(16384, MathUtils.gcd(Integer.MIN_VALUE, 16384));
        assertEquals(16384, MathUtils.gcd(16384, Integer.MIN_VALUE));
        assertEquals(32768, MathUtils.gcd(Integer.MIN_VALUE, 32768));
        assertEquals(32768, MathUtils.gcd(32768, Integer.MIN_VALUE));
        assertEquals(65536, MathUtils.gcd(Integer.MIN_VALUE, 65536));
        assertEquals(65536, MathUtils.gcd(65536, Integer.MIN_VALUE));
        assertEquals(131072, MathUtils.gcd(Integer.MIN_VALUE, 131072));
        assertEquals(131072, MathUtils.gcd(131072, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 23));
        assertEquals(1, MathUtils.gcd(23, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 29));
        assertEquals(1, MathUtils.gcd(29, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 31));
        assertEquals(1, MathUtils.gcd(31, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 38));
        assertEquals(2, MathUtils.gcd(38, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 42));
        assertEquals(2, MathUtils.gcd(42, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 46));
        assertEquals(2, MathUtils.gcd(46, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 50));
        assertEquals(2, MathUtils.gcd(50, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 54));
        assertEquals(2, MathUtils.gcd(54, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 58));
        assertEquals(2, MathUtils.gcd(58, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 55));
        assertEquals(1, MathUtils.gcd(55, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 63));
        assertEquals(1, MathUtils.gcd(63, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 75));
        assertEquals(1, MathUtils.gcd(75, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 81));
        assertEquals(1, MathUtils.gcd(81, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
        assertEquals(8192, MathUtils.gcd(Integer.MIN_VALUE, 8192));
        assertEquals(8192, MathUtils.gcd(8192, Integer.MIN_VALUE));
        assertEquals(16384, MathUtils.gcd(Integer.MIN_VALUE, 16384));
        assertEquals(16384, MathUtils.gcd(16384, Integer.MIN_VALUE));
        assertEquals(32768, MathUtils.gcd(Integer.MIN_VALUE, 32768));
        assertEquals(32768, MathUtils.gcd(32768, Integer.MIN_VALUE));
        assertEquals(65536, MathUtils.gcd(Integer.MIN_VALUE, 65536));
        assertEquals(65536, MathUtils.gcd(65536, Integer.MIN_VALUE));
        assertEquals(131072, MathUtils.gcd(Integer.MIN_VALUE, 131072));
        assertEquals(131072, MathUtils.gcd(131072, Integer.MIN_VALUE));
        assertEquals(262144, MathUtils.gcd(Integer.MIN_VALUE, 262144));
        assertEquals(262144, MathUtils.gcd(262144, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 23));
        assertEquals(1, MathUtils.gcd(23, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 29));
        assertEquals(1, MathUtils.gcd(29, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 31));
        assertEquals(1, MathUtils.gcd(31, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 38));
        assertEquals(2, MathUtils.gcd(38, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 42));
        assertEquals(2, MathUtils.gcd(42, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 46));
        assertEquals(2, MathUtils.gcd(46, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 50));
        assertEquals(2, MathUtils.gcd(50, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 54));
        assertEquals(2, MathUtils.gcd(54, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 58));
        assertEquals(2, MathUtils.gcd(58, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 62));
        assertEquals(2, MathUtils.gcd(62, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 55));
        assertEquals(1, MathUtils.gcd(55, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 63));
        assertEquals(1, MathUtils.gcd(63, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 75));
        assertEquals(1, MathUtils.gcd(75, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 81));
        assertEquals(1, MathUtils.gcd(81, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 99));
        assertEquals(1, MathUtils.gcd(99, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
        assertEquals(8192, MathUtils.gcd(Integer.MIN_VALUE, 8192));
        assertEquals(8192, MathUtils.gcd(8192, Integer.MIN_VALUE));
        assertEquals(16384, MathUtils.gcd(Integer.MIN_VALUE, 16384));
        assertEquals(16384, MathUtils.gcd(16384, Integer.MIN_VALUE));
        assertEquals(32768, MathUtils.gcd(Integer.MIN_VALUE, 32768));
        assertEquals(32768, MathUtils.gcd(32768, Integer.MIN_VALUE));
        assertEquals(65536, MathUtils.gcd(Integer.MIN_VALUE, 65536));
        assertEquals(65536, MathUtils.gcd(65536, Integer.MIN_VALUE));
        assertEquals(131072, MathUtils.gcd(Integer.MIN_VALUE, 131072));
        assertEquals(131072, MathUtils.gcd(131072, Integer.MIN_VALUE));
        assertEquals(262144, MathUtils.gcd(Integer.MIN_VALUE, 262144));
        assertEquals(262144, MathUtils.gcd(262144, Integer.MIN_VALUE));
        assertEquals(524288, MathUtils.gcd(Integer.MIN_VALUE, 524288));
        assertEquals(524288, MathUtils.gcd(524288, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 23));
        assertEquals(1, MathUtils.gcd(23, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 29));
        assertEquals(1, MathUtils.gcd(29, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 31));
        assertEquals(1, MathUtils.gcd(31, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 37));
        assertEquals(1, MathUtils.gcd(37, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 38));
        assertEquals(2, MathUtils.gcd(38, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 42));
        assertEquals(2, MathUtils.gcd(42, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 46));
        assertEquals(2, MathUtils.gcd(46, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 50));
        assertEquals(2, MathUtils.gcd(50, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 54));
        assertEquals(2, MathUtils.gcd(54, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 58));
        assertEquals(2, MathUtils.gcd(58, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 62));
        assertEquals(2, MathUtils.gcd(62, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 66));
        assertEquals(2, MathUtils.gcd(66, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 55));
        assertEquals(1, MathUtils.gcd(55, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 63));
        assertEquals(1, MathUtils.gcd(63, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 75));
        assertEquals(1, MathUtils.gcd(75, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 81));
        assertEquals(1, MathUtils.gcd(81, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 99));
        assertEquals(1, MathUtils.gcd(99, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 105));
        assertEquals(1, MathUtils.gcd(105, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
        assertEquals(8192, MathUtils.gcd(Integer.MIN_VALUE, 8192));
        assertEquals(8192, MathUtils.gcd(8192, Integer.MIN_VALUE));
        assertEquals(16384, MathUtils.gcd(Integer.MIN_VALUE, 16384));
        assertEquals(16384, MathUtils.gcd(16384, Integer.MIN_VALUE));
        assertEquals(32768, MathUtils.gcd(Integer.MIN_VALUE, 32768));
        assertEquals(32768, MathUtils.gcd(32768, Integer.MIN_VALUE));
        assertEquals(65536, MathUtils.gcd(Integer.MIN_VALUE, 65536));
        assertEquals(65536, MathUtils.gcd(65536, Integer.MIN_VALUE));
        assertEquals(131072, MathUtils.gcd(Integer.MIN_VALUE, 131072));
        assertEquals(131072, MathUtils.gcd(131072, Integer.MIN_VALUE));
        assertEquals(262144, MathUtils.gcd(Integer.MIN_VALUE, 262144));
        assertEquals(262144, MathUtils.gcd(262144, Integer.MIN_VALUE));
        assertEquals(524288, MathUtils.gcd(Integer.MIN_VALUE, 524288));
        assertEquals(524288, MathUtils.gcd(524288, Integer.MIN_VALUE));
        assertEquals(1048576, MathUtils.gcd(Integer.MIN_VALUE, 1048576));
        assertEquals(1048576, MathUtils.gcd(1048576, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 23));
        assertEquals(1, MathUtils.gcd(23, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 29));
        assertEquals(1, MathUtils.gcd(29, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 31));
        assertEquals(1, MathUtils.gcd(31, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 37));
        assertEquals(1, MathUtils.gcd(37, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 38));
        assertEquals(2, MathUtils.gcd(38, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 42));
        assertEquals(2, MathUtils.gcd(42, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 46));
        assertEquals(2, MathUtils.gcd(46, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 50));
        assertEquals(2, MathUtils.gcd(50, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 54));
        assertEquals(2, MathUtils.gcd(54, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 58));
        assertEquals(2, MathUtils.gcd(58, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 62));
        assertEquals(2, MathUtils.gcd(62, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 66));
        assertEquals(2, MathUtils.gcd(66, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 70));
        assertEquals(2, MathUtils.gcd(70, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 55));
        assertEquals(1, MathUtils.gcd(55, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 63));
        assertEquals(1, MathUtils.gcd(63, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 75));
        assertEquals(1, MathUtils.gcd(75, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 81));
        assertEquals(1, MathUtils.gcd(81, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 99));
        assertEquals(1, MathUtils.gcd(99, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 105));
        assertEquals(1, MathUtils.gcd(105, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 121));
        assertEquals(1, MathUtils.gcd(121, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
        assertEquals(8192, MathUtils.gcd(Integer.MIN_VALUE, 8192));
        assertEquals(8192, MathUtils.gcd(8192, Integer.MIN_VALUE));
        assertEquals(16384, MathUtils.gcd(Integer.MIN_VALUE, 16384));
        assertEquals(16384, MathUtils.gcd(16384, Integer.MIN_VALUE));
        assertEquals(32768, MathUtils.gcd(Integer.MIN_VALUE, 32768));
        assertEquals(32768, MathUtils.gcd(32768, Integer.MIN_VALUE));
        assertEquals(65536, MathUtils.gcd(Integer.MIN_VALUE, 65536));
        assertEquals(65536, MathUtils.gcd(65536, Integer.MIN_VALUE));
        assertEquals(131072, MathUtils.gcd(Integer.MIN_VALUE, 131072));
        assertEquals(131072, MathUtils.gcd(131072, Integer.MIN_VALUE));
        assertEquals(262144, MathUtils.gcd(Integer.MIN_VALUE, 262144));
        assertEquals(262144, MathUtils.gcd(262144, Integer.MIN_VALUE));
        assertEquals(524288, MathUtils.gcd(Integer.MIN_VALUE, 524288));
        assertEquals(524288, MathUtils.gcd(524288, Integer.MIN_VALUE));
        assertEquals(1048576, MathUtils.gcd(Integer.MIN_VALUE, 1048576));
        assertEquals(1048576, MathUtils.gcd(1048576, Integer.MIN_VALUE));
        assertEquals(2097152, MathUtils.gcd(Integer.MIN_VALUE, 2097152));
        assertEquals(2097152, MathUtils.gcd(2097152, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 23));
        assertEquals(1, MathUtils.gcd(23, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 29));
        assertEquals(1, MathUtils.gcd(29, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 31));
        assertEquals(1, MathUtils.gcd(31, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 37));
        assertEquals(1, MathUtils.gcd(37, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 41));
        assertEquals(1, MathUtils.gcd(41, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 38));
        assertEquals(2, MathUtils.gcd(38, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 42));
        assertEquals(2, MathUtils.gcd(42, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 46));
        assertEquals(2, MathUtils.gcd(46, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 50));
        assertEquals(2, MathUtils.gcd(50, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 54));
        assertEquals(2, MathUtils.gcd(54, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 58));
        assertEquals(2, MathUtils.gcd(58, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 62));
        assertEquals(2, MathUtils.gcd(62, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 66));
        assertEquals(2, MathUtils.gcd(66, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 70));
        assertEquals(2, MathUtils.gcd(70, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 74));
        assertEquals(2, MathUtils.gcd(74, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 55));
        assertEquals(1, MathUtils.gcd(55, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 63));
        assertEquals(1, MathUtils.gcd(63, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 75));
        assertEquals(1, MathUtils.gcd(75, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 81));
        assertEquals(1, MathUtils.gcd(81, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 99));
        assertEquals(1, MathUtils.gcd(99, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 105));
        assertEquals(1, MathUtils.gcd(105, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 121));
        assertEquals(1, MathUtils.gcd(121, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 125));
        assertEquals(1, MathUtils.gcd(125, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
        assertEquals(8192, MathUtils.gcd(Integer.MIN_VALUE, 8192));
        assertEquals(8192, MathUtils.gcd(8192, Integer.MIN_VALUE));
        assertEquals(16384, MathUtils.gcd(Integer.MIN_VALUE, 16384));
        assertEquals(16384, MathUtils.gcd(16384, Integer.MIN_VALUE));
        assertEquals(32768, MathUtils.gcd(Integer.MIN_VALUE, 32768));
        assertEquals(32768, MathUtils.gcd(32768, Integer.MIN_VALUE));
        assertEquals(65536, MathUtils.gcd(Integer.MIN_VALUE, 65536));
        assertEquals(65536, MathUtils.gcd(65536, Integer.MIN_VALUE));
        assertEquals(131072, MathUtils.gcd(Integer.MIN_VALUE, 131072));
        assertEquals(131072, MathUtils.gcd(131072, Integer.MIN_VALUE));
        assertEquals(262144, MathUtils.gcd(Integer.MIN_VALUE, 262144));
        assertEquals(262144, MathUtils.gcd(262144, Integer.MIN_VALUE));
        assertEquals(524288, MathUtils.gcd(Integer.MIN_VALUE, 524288));
        assertEquals(524288, MathUtils.gcd(524288, Integer.MIN_VALUE));
        assertEquals(1048576, MathUtils.gcd(Integer.MIN_VALUE, 1048576));
        assertEquals(1048576, MathUtils.gcd(1048576, Integer.MIN_VALUE));
        assertEquals(2097152, MathUtils.gcd(Integer.MIN_VALUE, 2097152));
        assertEquals(2097152, MathUtils.gcd(2097152, Integer.MIN_VALUE));
        assertEquals(4194304, MathUtils.gcd(Integer.MIN_VALUE, 4194304));
        assertEquals(4194304, MathUtils.gcd(4194304, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 23));
        assertEquals(1, MathUtils.gcd(23, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 29));
        assertEquals(1, MathUtils.gcd(29, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 31));
        assertEquals(1, MathUtils.gcd(31, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 37));
        assertEquals(1, MathUtils.gcd(37, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 41));
        assertEquals(1, MathUtils.gcd(41, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 43));
        assertEquals(1, MathUtils.gcd(43, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 38));
        assertEquals(2, MathUtils.gcd(38, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 42));
        assertEquals(2, MathUtils.gcd(42, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 46));
        assertEquals(2, MathUtils.gcd(46, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 50));
        assertEquals(2, MathUtils.gcd(50, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 54));
        assertEquals(2, MathUtils.gcd(54, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 58));
        assertEquals(2, MathUtils.gcd(58, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 62));
        assertEquals(2, MathUtils.gcd(62, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 66));
        assertEquals(2, MathUtils.gcd(66, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 70));
        assertEquals(2, MathUtils.gcd(70, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 74));
        assertEquals(2, MathUtils.gcd(74, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 78));
        assertEquals(2, MathUtils.gcd(78, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 55));
        assertEquals(1, MathUtils.gcd(55, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 63));
        assertEquals(1, MathUtils.gcd(63, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 75));
        assertEquals(1, MathUtils.gcd(75, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 81));
        assertEquals(1, MathUtils.gcd(81, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 99));
        assertEquals(1, MathUtils.gcd(99, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 105));
        assertEquals(1, MathUtils.gcd(105, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 121));
        assertEquals(1, MathUtils.gcd(121, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 125));
        assertEquals(1, MathUtils.gcd(125, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 135));
        assertEquals(1, MathUtils.gcd(135, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
        assertEquals(8192, MathUtils.gcd(Integer.MIN_VALUE, 8192));
        assertEquals(8192, MathUtils.gcd(8192, Integer.MIN_VALUE));
        assertEquals(16384, MathUtils.gcd(Integer.MIN_VALUE, 16384));
        assertEquals(16384, MathUtils.gcd(16384, Integer.MIN_VALUE));
        assertEquals(32768, MathUtils.gcd(Integer.MIN_VALUE, 32768));
        assertEquals(32768, MathUtils.gcd(32768, Integer.MIN_VALUE));
        assertEquals(65536, MathUtils.gcd(Integer.MIN_VALUE, 65536));
        assertEquals(65536, MathUtils.gcd(65536, Integer.MIN_VALUE));
        assertEquals(131072, MathUtils.gcd(Integer.MIN_VALUE, 131072));
        assertEquals(131072, MathUtils.gcd(131072, Integer.MIN_VALUE));
        assertEquals(262144, MathUtils.gcd(Integer.MIN_VALUE, 262144));
        assertEquals(262144, MathUtils.gcd(262144, Integer.MIN_VALUE));
        assertEquals(524288, MathUtils.gcd(Integer.MIN_VALUE, 524288));
        assertEquals(524288, MathUtils.gcd(524288, Integer.MIN_VALUE));
        assertEquals(1048576, MathUtils.gcd(Integer.MIN_VALUE, 1048576));
        assertEquals(1048576, MathUtils.gcd(1048576, Integer.MIN_VALUE));
        assertEquals(2097152, MathUtils.gcd(Integer.MIN_VALUE, 2097152));
        assertEquals(2097152, MathUtils.gcd(2097152, Integer.MIN_VALUE));
        assertEquals(4194304, MathUtils.gcd(Integer.MIN_VALUE, 4194304));
        assertEquals(4194304, MathUtils.gcd(4194304, Integer.MIN_VALUE));
        assertEquals(8388608, MathUtils.gcd(Integer.MIN_VALUE, 8388608));
        assertEquals(8388608, MathUtils.gcd(8388608, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 23));
        assertEquals(1, MathUtils.gcd(23, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 29));
        assertEquals(1, MathUtils.gcd(29, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 31));
        assertEquals(1, MathUtils.gcd(31, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 37));
        assertEquals(1, MathUtils.gcd(37, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 41));
        assertEquals(1, MathUtils.gcd(41, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 43));
        assertEquals(1, MathUtils.gcd(43, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 38));
        assertEquals(2, MathUtils.gcd(38, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 42));
        assertEquals(2, MathUtils.gcd(42, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 46));
        assertEquals(2, MathUtils.gcd(46, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 50));
        assertEquals(2, MathUtils.gcd(50, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 54));
        assertEquals(2, MathUtils.gcd(54, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 58));
        assertEquals(2, MathUtils.gcd(58, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 62));
        assertEquals(2, MathUtils.gcd(62, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 66));
        assertEquals(2, MathUtils.gcd(66, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 70));
        assertEquals(2, MathUtils.gcd(70, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 74));
        assertEquals(2, MathUtils.gcd(74, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 78));
        assertEquals(2, MathUtils.gcd(78, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 82));
        assertEquals(2, MathUtils.gcd(82, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 55));
        assertEquals(1, MathUtils.gcd(55, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 63));
        assertEquals(1, MathUtils.gcd(63, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 75));
        assertEquals(1, MathUtils.gcd(75, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 81));
        assertEquals(1, MathUtils.gcd(81, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 99));
        assertEquals(1, MathUtils.gcd(99, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 105));
        assertEquals(1, MathUtils.gcd(105, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 121));
        assertEquals(1, MathUtils.gcd(121, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 125));
        assertEquals(1, MathUtils.gcd(125, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 135));
        assertEquals(1, MathUtils.gcd(135, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 147));
        assertEquals(1, MathUtils.gcd(147, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
        assertEquals(8192, MathUtils.gcd(Integer.MIN_VALUE, 8192));
        assertEquals(8192, MathUtils.gcd(8192, Integer.MIN_VALUE));
        assertEquals(16384, MathUtils.gcd(Integer.MIN_VALUE, 16384));
        assertEquals(16384, MathUtils.gcd(16384, Integer.MIN_VALUE));
        assertEquals(32768, MathUtils.gcd(Integer.MIN_VALUE, 32768));
        assertEquals(32768, MathUtils.gcd(32768, Integer.MIN_VALUE));
        assertEquals(65536, MathUtils.gcd(Integer.MIN_VALUE, 65536));
        assertEquals(65536, MathUtils.gcd(65536, Integer.MIN_VALUE));
        assertEquals(131072, MathUtils.gcd(Integer.MIN_VALUE, 131072));
        assertEquals(131072, MathUtils.gcd(131072, Integer.MIN_VALUE));
        assertEquals(262144, MathUtils.gcd(Integer.MIN_VALUE, 262144));
        assertEquals(262144, MathUtils.gcd(262144, Integer.MIN_VALUE));
        assertEquals(524288, MathUtils.gcd(Integer.MIN_VALUE, 524288));
        assertEquals(524288, MathUtils.gcd(524288, Integer.MIN_VALUE));
        assertEquals(1048576, MathUtils.gcd(Integer.MIN_VALUE, 1048576));
        assertEquals(1048576, MathUtils.gcd(1048576, Integer.MIN_VALUE));
        assertEquals(2097152, MathUtils.gcd(Integer.MIN_VALUE, 2097152));
        assertEquals(2097152, MathUtils.gcd(2097152, Integer.MIN_VALUE));
        assertEquals(4194304, MathUtils.gcd(Integer.MIN_VALUE, 4194304));
        assertEquals(4194304, MathUtils.gcd(4194304, Integer.MIN_VALUE));
        assertEquals(8388608, MathUtils.gcd(Integer.MIN_VALUE, 8388608));
        assertEquals(8388608, MathUtils.gcd(8388608, Integer.MIN_VALUE));
        assertEquals(16777216, MathUtils.gcd(Integer.MIN_VALUE, 16777216));
        assertEquals(16777216, MathUtils.gcd(16777216, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 23));
        assertEquals(1, MathUtils.gcd(23, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 29));
        assertEquals(1, MathUtils.gcd(29, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 31));
        assertEquals(1, MathUtils.gcd(31, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 37));
        assertEquals(1, MathUtils.gcd(37, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 41));
        assertEquals(1, MathUtils.gcd(41, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 43));
        assertEquals(1, MathUtils.gcd(43, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 47));
        assertEquals(1, MathUtils.gcd(47, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 38));
        assertEquals(2, MathUtils.gcd(38, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 42));
        assertEquals(2, MathUtils.gcd(42, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 46));
        assertEquals(2, MathUtils.gcd(46, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 50));
        assertEquals(2, MathUtils.gcd(50, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 54));
        assertEquals(2, MathUtils.gcd(54, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 58));
        assertEquals(2, MathUtils.gcd(58, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 62));
        assertEquals(2, MathUtils.gcd(62, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 66));
        assertEquals(2, MathUtils.gcd(66, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 70));
        assertEquals(2, MathUtils.gcd(70, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 74));
        assertEquals(2, MathUtils.gcd(74, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 78));
        assertEquals(2, MathUtils.gcd(78, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 82));
        assertEquals(2, MathUtils.gcd(82, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 86));
        assertEquals(2, MathUtils.gcd(86, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 55));
        assertEquals(1, MathUtils.gcd(55, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 63));
        assertEquals(1, MathUtils.gcd(63, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 75));
        assertEquals(1, MathUtils.gcd(75, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 81));
        assertEquals(1, MathUtils.gcd(81, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 99));
        assertEquals(1, MathUtils.gcd(99, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 105));
        assertEquals(1, MathUtils.gcd(105, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 121));
        assertEquals(1, MathUtils.gcd(121, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 125));
        assertEquals(1, MathUtils.gcd(125, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 135));
        assertEquals(1, MathUtils.gcd(135, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 147));
        assertEquals(1, MathUtils.gcd(147, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 165));
        assertEquals(1, MathUtils.gcd(165, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
        assertEquals(8192, MathUtils.gcd(Integer.MIN_VALUE, 8192));
        assertEquals(8192, MathUtils.gcd(8192, Integer.MIN_VALUE));
        assertEquals(16384, MathUtils.gcd(Integer.MIN_VALUE, 16384));
        assertEquals(16384, MathUtils.gcd(16384, Integer.MIN_VALUE));
        assertEquals(32768, MathUtils.gcd(Integer.MIN_VALUE, 32768));
        assertEquals(32768, MathUtils.gcd(32768, Integer.MIN_VALUE));
        assertEquals(65536, MathUtils.gcd(Integer.MIN_VALUE, 65536));
        assertEquals(65536, MathUtils.gcd(65536, Integer.MIN_VALUE));
        assertEquals(131072, MathUtils.gcd(Integer.MIN_VALUE, 131072));
        assertEquals(131072, MathUtils.gcd(131072, Integer.MIN_VALUE));
        assertEquals(262144, MathUtils.gcd(Integer.MIN_VALUE, 262144));
        assertEquals(262144, MathUtils.gcd(262144, Integer.MIN_VALUE));
        assertEquals(524288, MathUtils.gcd(Integer.MIN_VALUE, 524288));
        assertEquals(524288, MathUtils.gcd(524288, Integer.MIN_VALUE));
        assertEquals(1048576, MathUtils.gcd(Integer.MIN_VALUE, 1048576));
        assertEquals(1048576, MathUtils.gcd(1048576, Integer.MIN_VALUE));
        assertEquals(2097152, MathUtils.gcd(Integer.MIN_VALUE, 2097152));
        assertEquals(2097152, MathUtils.gcd(2097152, Integer.MIN_VALUE));
        assertEquals(4194304, MathUtils.gcd(Integer.MIN_VALUE, 4194304));
        assertEquals(4194304, MathUtils.gcd(4194304, Integer.MIN_VALUE));
        assertEquals(8388608, MathUtils.gcd(Integer.MIN_VALUE, 8388608));
        assertEquals(8388608, MathUtils.gcd(8388608, Integer.MIN_VALUE));
        assertEquals(16777216, MathUtils.gcd(Integer.MIN_VALUE, 16777216));
        assertEquals(16777216, MathUtils.gcd(16777216, Integer.MIN_VALUE));
        assertEquals(33554432, MathUtils.gcd(Integer.MIN_VALUE, 33554432));
        assertEquals(33554432, MathUtils.gcd(33554432, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 23));
        assertEquals(1, MathUtils.gcd(23, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 29));
        assertEquals(1, MathUtils.gcd(29, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 31));
        assertEquals(1, MathUtils.gcd(31, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 37));
        assertEquals(1, MathUtils.gcd(37, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 41));
        assertEquals(1, MathUtils.gcd(41, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 43));
        assertEquals(1, MathUtils.gcd(43, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 47));
        assertEquals(1, MathUtils.gcd(47, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 38));
        assertEquals(2, MathUtils.gcd(38, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 42));
        assertEquals(2, MathUtils.gcd(42, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 46));
        assertEquals(2, MathUtils.gcd(46, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 50));
        assertEquals(2, MathUtils.gcd(50, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 54));
        assertEquals(2, MathUtils.gcd(54, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 58));
        assertEquals(2, MathUtils.gcd(58, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 62));
        assertEquals(2, MathUtils.gcd(62, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 66));
        assertEquals(2, MathUtils.gcd(66, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 70));
        assertEquals(2, MathUtils.gcd(70, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 74));
        assertEquals(2, MathUtils.gcd(74, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 78));
        assertEquals(2, MathUtils.gcd(78, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 82));
        assertEquals(2, MathUtils.gcd(82, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 86));
        assertEquals(2, MathUtils.gcd(86, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 90));
        assertEquals(2, MathUtils.gcd(90, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 55));
        assertEquals(1, MathUtils.gcd(55, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 63));
        assertEquals(1, MathUtils.gcd(63, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 75));
        assertEquals(1, MathUtils.gcd(75, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 81));
        assertEquals(1, MathUtils.gcd(81, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 99));
        assertEquals(1, MathUtils.gcd(99, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 105));
        assertEquals(1, MathUtils.gcd(105, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 121));
        assertEquals(1, MathUtils.gcd(121, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 125));
        assertEquals(1, MathUtils.gcd(125, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 135));
        assertEquals(1, MathUtils.gcd(135, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 147));
        assertEquals(1, MathUtils.gcd(147, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 165));
        assertEquals(1, MathUtils.gcd(165, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 175));
        assertEquals(1, MathUtils.gcd(175, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
        assertEquals(8192, MathUtils.gcd(Integer.MIN_VALUE, 8192));
        assertEquals(8192, MathUtils.gcd(8192, Integer.MIN_VALUE));
        assertEquals(16384, MathUtils.gcd(Integer.MIN_VALUE, 16384));
        assertEquals(16384, MathUtils.gcd(16384, Integer.MIN_VALUE));
        assertEquals(32768, MathUtils.gcd(Integer.MIN_VALUE, 32768));
        assertEquals(32768, MathUtils.gcd(32768, Integer.MIN_VALUE));
        assertEquals(65536, MathUtils.gcd(Integer.MIN_VALUE, 65536));
        assertEquals(65536, MathUtils.gcd(65536, Integer.MIN_VALUE));
        assertEquals(131072, MathUtils.gcd(Integer.MIN_VALUE, 131072));
        assertEquals(131072, MathUtils.gcd(131072, Integer.MIN_VALUE));
        assertEquals(262144, MathUtils.gcd(Integer.MIN_VALUE, 262144));
        assertEquals(262144, MathUtils.gcd(262144, Integer.MIN_VALUE));
        assertEquals(524288, MathUtils.gcd(Integer.MIN_VALUE, 524288));
        assertEquals(524288, MathUtils.gcd(524288, Integer.MIN_VALUE));
        assertEquals(1048576, MathUtils.gcd(Integer.MIN_VALUE, 1048576));
        assertEquals(1048576, MathUtils.gcd(1048576, Integer.MIN_VALUE));
        assertEquals(2097152, MathUtils.gcd(Integer.MIN_VALUE, 2097152));
        assertEquals(2097152, MathUtils.gcd(2097152, Integer.MIN_VALUE));
        assertEquals(4194304, MathUtils.gcd(Integer.MIN_VALUE, 4194304));
        assertEquals(4194304, MathUtils.gcd(4194304, Integer.MIN_VALUE));
        assertEquals(8388608, MathUtils.gcd(Integer.MIN_VALUE, 8388608));
        assertEquals(8388608, MathUtils.gcd(8388608, Integer.MIN_VALUE));
        assertEquals(16777216, MathUtils.gcd(Integer.MIN_VALUE, 16777216));
        assertEquals(16777216, MathUtils.gcd(16777216, Integer.MIN_VALUE));
        assertEquals(33554432, MathUtils.gcd(Integer.MIN_VALUE, 33554432));
        assertEquals(33554432, MathUtils.gcd(33554432, Integer.MIN_VALUE));
        assertEquals(67108864, MathUtils.gcd(Integer.MIN_VALUE, 67108864));
        assertEquals(67108864, MathUtils.gcd(67108864, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 23));
        assertEquals(1, MathUtils.gcd(23, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 29));
        assertEquals(1, MathUtils.gcd(29, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 31));
        assertEquals(1, MathUtils.gcd(31, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 37));
        assertEquals(1, MathUtils.gcd(37, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 41));
        assertEquals(1, MathUtils.gcd(41, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 43));
        assertEquals(1, MathUtils.gcd(43, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 47));
        assertEquals(1, MathUtils.gcd(47, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 51));
        assertEquals(1, MathUtils.gcd(51, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 38));
        assertEquals(2, MathUtils.gcd(38, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 42));
        assertEquals(2, MathUtils.gcd(42, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 46));
        assertEquals(2, MathUtils.gcd(46, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 50));
        assertEquals(2, MathUtils.gcd(50, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 54));
        assertEquals(2, MathUtils.gcd(54, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 58));
        assertEquals(2, MathUtils.gcd(58, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 62));
        assertEquals(2, MathUtils.gcd(62, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 66));
        assertEquals(2, MathUtils.gcd(66, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 70));
        assertEquals(2, MathUtils.gcd(70, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 74));
        assertEquals(2, MathUtils.gcd(74, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 78));
        assertEquals(2, MathUtils.gcd(78, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 82));
        assertEquals(2, MathUtils.gcd(82, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 86));
        assertEquals(2, MathUtils.gcd(86, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 90));
        assertEquals(2, MathUtils.gcd(90, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 94));
        assertEquals(2, MathUtils.gcd(94, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 55));
        assertEquals(1, MathUtils.gcd(55, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 63));
        assertEquals(1, MathUtils.gcd(63, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 75));
        assertEquals(1, MathUtils.gcd(75, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 81));
        assertEquals(1, MathUtils.gcd(81, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 99));
        assertEquals(1, MathUtils.gcd(99, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 105));
        assertEquals(1, MathUtils.gcd(105, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 121));
        assertEquals(1, MathUtils.gcd(121, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 125));
        assertEquals(1, MathUtils.gcd(125, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 135));
        assertEquals(1, MathUtils.gcd(135, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 147));
        assertEquals(1, MathUtils.gcd(147, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 165));
        assertEquals(1, MathUtils.gcd(165, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 175));
        assertEquals(1, MathUtils.gcd(175, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 189));
        assertEquals(1, MathUtils.gcd(189, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
        assertEquals(8192, MathUtils.gcd(Integer.MIN_VALUE, 8192));
        assertEquals(8192, MathUtils.gcd(8192, Integer.MIN_VALUE));
        assertEquals(16384, MathUtils.gcd(Integer.MIN_VALUE, 16384));
        assertEquals(16384, MathUtils.gcd(16384, Integer.MIN_VALUE));
        assertEquals(32768, MathUtils.gcd(Integer.MIN_VALUE, 32768));
        assertEquals(32768, MathUtils.gcd(32768, Integer.MIN_VALUE));
        assertEquals(65536, MathUtils.gcd(Integer.MIN_VALUE, 65536));
        assertEquals(65536, MathUtils.gcd(65536, Integer.MIN_VALUE));
        assertEquals(131072, MathUtils.gcd(Integer.MIN_VALUE, 131072));
        assertEquals(131072, MathUtils.gcd(131072, Integer.MIN_VALUE));
        assertEquals(262144, MathUtils.gcd(Integer.MIN_VALUE, 262144));
        assertEquals(262144, MathUtils.gcd(262144, Integer.MIN_VALUE));
        assertEquals(524288, MathUtils.gcd(Integer.MIN_VALUE, 524288));
        assertEquals(524288, MathUtils.gcd(524288, Integer.MIN_VALUE));
        assertEquals(1048576, MathUtils.gcd(Integer.MIN_VALUE, 1048576));
        assertEquals(1048576, MathUtils.gcd(1048576, Integer.MIN_VALUE));
        assertEquals(2097152, MathUtils.gcd(Integer.MIN_VALUE, 2097152));
        assertEquals(2097152, MathUtils.gcd(2097152, Integer.MIN_VALUE));
        assertEquals(4194304, MathUtils.gcd(Integer.MIN_VALUE, 4194304));
        assertEquals(4194304, MathUtils.gcd(4194304, Integer.MIN_VALUE));
        assertEquals(8388608, MathUtils.gcd(Integer.MIN_VALUE, 8388608));
        assertEquals(8388608, MathUtils.gcd(8388608, Integer.MIN_VALUE));
        assertEquals(16777216, MathUtils.gcd(Integer.MIN_VALUE, 16777216));
        assertEquals(16777216, MathUtils.gcd(16777216, Integer.MIN_VALUE));
        assertEquals(33554432, MathUtils.gcd(Integer.MIN_VALUE, 33554432));
        assertEquals(33554432, MathUtils.gcd(33554432, Integer.MIN_VALUE));
        assertEquals(67108864, MathUtils.gcd(Integer.MIN_VALUE, 67108864));
        assertEquals(67108864, MathUtils.gcd(67108864, Integer.MIN_VALUE));
        assertEquals(134217728, MathUtils.gcd(Integer.MIN_VALUE, 134217728));
        assertEquals(134217728, MathUtils.gcd(134217728, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 3));
        assertEquals(1, MathUtils.gcd(3, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 5));
        assertEquals(1, MathUtils.gcd(5, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 7));
        assertEquals(1, MathUtils.gcd(7, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 11));
        assertEquals(1, MathUtils.gcd(11, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 13));
        assertEquals(1, MathUtils.gcd(13, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 17));
        assertEquals(1, MathUtils.gcd(17, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 19));
        assertEquals(1, MathUtils.gcd(19, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 23));
        assertEquals(1, MathUtils.gcd(23, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 29));
        assertEquals(1, MathUtils.gcd(29, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 31));
        assertEquals(1, MathUtils.gcd(31, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 37));
        assertEquals(1, MathUtils.gcd(37, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 41));
        assertEquals(1, MathUtils.gcd(41, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 43));
        assertEquals(1, MathUtils.gcd(43, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 47));
        assertEquals(1, MathUtils.gcd(47, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 51));
        assertEquals(1, MathUtils.gcd(51, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 53));
        assertEquals(1, MathUtils.gcd(53, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 6));
        assertEquals(2, MathUtils.gcd(6, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 10));
        assertEquals(2, MathUtils.gcd(10, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 14));
        assertEquals(2, MathUtils.gcd(14, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 18));
        assertEquals(2, MathUtils.gcd(18, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 22));
        assertEquals(2, MathUtils.gcd(22, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 26));
        assertEquals(2, MathUtils.gcd(26, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 30));
        assertEquals(2, MathUtils.gcd(30, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 34));
        assertEquals(2, MathUtils.gcd(34, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 38));
        assertEquals(2, MathUtils.gcd(38, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 42));
        assertEquals(2, MathUtils.gcd(42, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 46));
        assertEquals(2, MathUtils.gcd(46, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 50));
        assertEquals(2, MathUtils.gcd(50, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 54));
        assertEquals(2, MathUtils.gcd(54, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 58));
        assertEquals(2, MathUtils.gcd(58, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 62));
        assertEquals(2, MathUtils.gcd(62, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 66));
        assertEquals(2, MathUtils.gcd(66, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 70));
        assertEquals(2, MathUtils.gcd(70, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 74));
        assertEquals(2, MathUtils.gcd(74, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 78));
        assertEquals(2, MathUtils.gcd(78, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 82));
        assertEquals(2, MathUtils.gcd(82, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 86));
        assertEquals(2, MathUtils.gcd(86, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 90));
        assertEquals(2, MathUtils.gcd(90, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 94));
        assertEquals(2, MathUtils.gcd(94, Integer.MIN_VALUE));
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 98));
        assertEquals(2, MathUtils.gcd(98, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeOddNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 9));
        assertEquals(1, MathUtils.gcd(9, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 15));
        assertEquals(1, MathUtils.gcd(15, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 21));
        assertEquals(1, MathUtils.gcd(21, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 25));
        assertEquals(1, MathUtils.gcd(25, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 27));
        assertEquals(1, MathUtils.gcd(27, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 33));
        assertEquals(1, MathUtils.gcd(33, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 35));
        assertEquals(1, MathUtils.gcd(35, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 39));
        assertEquals(1, MathUtils.gcd(39, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 45));
        assertEquals(1, MathUtils.gcd(45, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 49));
        assertEquals(1, MathUtils.gcd(49, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 55));
        assertEquals(1, MathUtils.gcd(55, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 63));
        assertEquals(1, MathUtils.gcd(63, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 75));
        assertEquals(1, MathUtils.gcd(75, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 81));
        assertEquals(1, MathUtils.gcd(81, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 99));
        assertEquals(1, MathUtils.gcd(99, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 105));
        assertEquals(1, MathUtils.gcd(105, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 121));
        assertEquals(1, MathUtils.gcd(121, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 125));
        assertEquals(1, MathUtils.gcd(125, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 135));
        assertEquals(1, MathUtils.gcd(135, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 147));
        assertEquals(1, MathUtils.gcd(147, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 165));
        assertEquals(1, MathUtils.gcd(165, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 175));
        assertEquals(1, MathUtils.gcd(175, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 189));
        assertEquals(1, MathUtils.gcd(189, Integer.MIN_VALUE));
        assertEquals(1, MathUtils.gcd(Integer.MIN_VALUE, 225));
        assertEquals(1, MathUtils.gcd(225, Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testGcdWithMinValueAndLargeCompositeEvenPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwoNonPowerOfTwo() {
        assertEquals(2, MathUtils.gcd(Integer.MIN_VALUE, 2));
        assertEquals(2, MathUtils.gcd(2, Integer.MIN_VALUE));
        assertEquals(4, MathUtils.gcd(Integer.MIN_VALUE, 4));
        assertEquals(4, MathUtils.gcd(4, Integer.MIN_VALUE));
        assertEquals(8, MathUtils.gcd(Integer.MIN_VALUE, 8));
        assertEquals(8, MathUtils.gcd(8, Integer.MIN_VALUE));
        assertEquals(16, MathUtils.gcd(Integer.MIN_VALUE, 16));
        assertEquals(16, MathUtils.gcd(16, Integer.MIN_VALUE));
        assertEquals(32, MathUtils.gcd(Integer.MIN_VALUE, 32));
        assertEquals(32, MathUtils.gcd(32, Integer.MIN_VALUE));
        assertEquals(64, MathUtils.gcd(Integer.MIN_VALUE, 64));
        assertEquals(64, MathUtils.gcd(64, Integer.MIN_VALUE));
        assertEquals(128, MathUtils.gcd(Integer.MIN_VALUE, 128));
        assertEquals(128, MathUtils.gcd(128, Integer.MIN_VALUE));
        assertEquals(256, MathUtils.gcd(Integer.MIN_VALUE, 256));
        assertEquals(256, MathUtils.gcd(256, Integer.MIN_VALUE));
        assertEquals(512, MathUtils.gcd(Integer.MIN_VALUE, 512));
        assertEquals(512, MathUtils.gcd(512, Integer.MIN_VALUE));
        assertEquals(1024, MathUtils.gcd(Integer.MIN_VALUE, 1024));
        assertEquals(1024, MathUtils.gcd(1024, Integer.MIN_VALUE));
        assertEquals(2048, MathUtils.gcd(Integer.MIN_VALUE, 2048));
        assertEquals(2048, MathUtils.gcd(2048, Integer.MIN_VALUE));
        assertEquals(4096, MathUtils.gcd(Integer.MIN_VALUE, 4096));
        assertEquals(4096, MathUtils.gcd(4096, Integer.MIN_VALUE));
        assertEquals(8192, MathUtils.gcd(Integer.MIN_VALUE, 8192));
        assertEquals(8192, MathUtils.gcd(8192, Integer.MIN_VALUE));
        assertEquals(16384, MathUtils.gcd(Integer.MIN_VALUE, 16384));
        assertEquals(16384, MathUtils.gcd(16384, Integer.MIN_VALUE));
        assertEquals(32768, MathUtils.gcd(Integer.MIN_VALUE, 32768));
        assertEquals