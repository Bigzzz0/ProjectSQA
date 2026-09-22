/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.math.MathRuntimeException;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Targets org.apache.commons.math.util.MathUtils:
 * 1. addAndCheck / subAndCheck (int, long):
 *    - Sign boundary permutations (+/+, -/-, +/-)
 *    - Symmetry branches (a > b)
 *    - MIN_VALUE boundary in subAndCheck
 *    - Integer / Long MAX_VALUE and MIN_VALUE overflow/underflow detection
 * 2. mulAndCheck (int, long):
 *    - Zero multiplier, positive, negative signs
 *    - Long.MIN_VALUE boundary cases
 *    - Positive and negative overflow branches
 * 3. gcd & lcm (int):
 *    - Zero inputs, Integer.MIN_VALUE overflow branches
 *    - Knuth algorithm loop branches (powers of 2, even/odd shifts)
 *    - Negative number normalization
 * 4. binomialCoefficient, binomialCoefficientDouble, binomialCoefficientLog:
 *    - Preconditions (n < k, n < 0)
 *    - Edge branches (n == k, k == 0, k == 1, k == n - 1, k > n/2)
 *    - Tier branches (n <= 61 naive, n <= 66 gcd split, n > 66 checked mul)
 *    - Log calculation tiers (n < 67, n < 1030, n >= 1030 log sums)
 * 5. equals & compareTo (double, double[], ULPs):
 *    - NaN equality handling, infinity checks, tolerance bounds
 *    - Lexicographical ULP conversions (positive/negative floats)
 *    - Array nullability, length mismatch, element discrepancies
 * 6. round & roundUnscaled (double, float):
 *    - All BigDecimal rounding modes: CEILING, DOWN, FLOOR, HALF_DOWN, HALF_EVEN, HALF_UP, UNNECESSARY, UP
 *    - Half-even parity logic (odd/even unscaled fractions)
 *    - Inexact round exception on ROUND_UNNECESSARY
 *    - Infinite and NaN input paths
 * 7. nextAfter & scalb:
 *    - Directional shifts around 0.0 (+/- Double.MIN_VALUE)
 *    - Mantissa boundary rollover (0x000fffffffffffffL) and borrow (0L)
 *    - NaN and Infinite passthroughs
 * 8. normalizeAngle & normalizeArray:
 *    - Center shifting across periodic boundaries
 *    - Invalid sum checks (NaN, infinite, zero sum)
 *    - Array elements with NaN and Infinite values
 * 9. distance, distance1, distanceInf (double[], int[]):
 *    - Zero length, multi-dimensional Euclidean, Manhattan, and Chebyshev metrics
 * 10. pow (int, long, BigInteger):
 *     - Negative exponent guards
 *     - Binary exponentiation bit-paths
 * 11. Indicator & Sign functions:
 *     - byte, short, int, long, float, double for -1, 0, +1, NaN
 */
public class MathUtilsGeminiTest {

    // -------------------------------------------------------------------------
    // Partition A: Core Arithmetic & Checked Operations
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAddAndCheckInt() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(-5, MathUtils.addAndCheck(-2, -3));
        assertEquals(1, MathUtils.addAndCheck(-2, 3));
        assertEquals(-1, MathUtils.addAndCheck(2, -3));
        assertEquals(Integer.MAX_VALUE, MathUtils.addAndCheck(Integer.MAX_VALUE - 1, 1));
        assertEquals(Integer.MIN_VALUE, MathUtils.addAndCheck(Integer.MIN_VALUE + 1, -1));
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
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
        // a > b symmetry branch
        assertEquals(5L, MathUtils.addAndCheck(3L, 2L));
        // a < 0, b >= 0 branch
        assertEquals(1L, MathUtils.addAndCheck(-2L, 3L));
        // a >= 0, b >= 0 branch
        assertEquals(Long.MAX_VALUE, MathUtils.addAndCheck(Long.MAX_VALUE - 1L, 1L));
        // a < 0, b < 0 branch
        assertEquals(Long.MIN_VALUE, MathUtils.addAndCheck(Long.MIN_VALUE + 1L, -1L));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckLongPositiveOverflow() {
        MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddAndCheckLongNegativeOverflow() {
        MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(timeout = 4000)
    public void testSubAndCheckInt() {
        assertEquals(-1, MathUtils.subAndCheck(2, 3));
        assertEquals(1, MathUtils.subAndCheck(3, 2));
        assertEquals(Integer.MIN_VALUE, MathUtils.subAndCheck(Integer.MIN_VALUE + 1, 1));
        assertEquals(Integer.MAX_VALUE, MathUtils.subAndCheck(Integer.MAX_VALUE - 1, -1));
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
        assertEquals(1L, MathUtils.subAndCheck(3L, 2L));
        assertEquals(-1L, MathUtils.subAndCheck(2L, 3L));
        // b == Long.MIN_VALUE with a < 0
        assertEquals(0L, MathUtils.subAndCheck(Long.MIN_VALUE, Long.MIN_VALUE));
        assertEquals(1L, MathUtils.subAndCheck(Long.MIN_VALUE + 1L, Long.MIN_VALUE));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckLongMinValSubtrahendPositiveA() {
        MathUtils.subAndCheck(0L, Long.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckLongPositiveOverflow() {
        MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubAndCheckLongNegativeOverflow() {
        MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
    }

    @Test(timeout = 4000)
    public void testMulAndCheckInt() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(-6, MathUtils.mulAndCheck(-2, 3));
        assertEquals(0, MathUtils.mulAndCheck(0, 100));
        assertEquals(Integer.MAX_VALUE, MathUtils.mulAndCheck(Integer.MAX_VALUE, 1));
        assertEquals(Integer.MIN_VALUE, MathUtils.mulAndCheck(Integer.MIN_VALUE, 1));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckIntPositiveOverflow() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckIntNegativeOverflow() {
        MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
    }

    @Test(timeout = 4000)
    public void testMulAndCheckLong() {
        assertEquals(0L, MathUtils.mulAndCheck(0L, 5L));
        assertEquals(0L, MathUtils.mulAndCheck(5L, 0L));
        assertEquals(0L, MathUtils.mulAndCheck(0L, -5L));
        assertEquals(0L, MathUtils.mulAndCheck(-5L, 0L));
        // a > b symmetry
        assertEquals(6L, MathUtils.mulAndCheck(3L, 2L));
        // a < 0, b < 0
        assertEquals(6L, MathUtils.mulAndCheck(-2L, -3L));
        // a < 0, b > 0
        assertEquals(-6L, MathUtils.mulAndCheck(-2L, 3L));
        // a > 0, b > 0
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        assertEquals(Long.MAX_VALUE, MathUtils.mulAndCheck(Long.MAX_VALUE, 1L));
        assertEquals(Long.MIN_VALUE, MathUtils.mulAndCheck(Long.MIN_VALUE, 1L));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckLongNegativeNegativeOverflow() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckLongNegativePositiveOverflow() {
        MathUtils.mulAndCheck(Long.MIN_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMulAndCheckLongPositivePositiveOverflow() {
        MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
    }

    // -------------------------------------------------------------------------
    // Partition B: GCD & LCM
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGcd() {
        assertEquals(0, MathUtils.gcd(0, 0));
        assertEquals(5, MathUtils.gcd(5, 0));
        assertEquals(5, MathUtils.gcd(0, 5));
        assertEquals(5, MathUtils.gcd(-5, 0));
        assertEquals(5, MathUtils.gcd(0, -5));
        assertEquals(6, MathUtils.gcd(12, 18));
        assertEquals(6, MathUtils.gcd(-12, 18));
        assertEquals(6, MathUtils.gcd(12, -18));
        assertEquals(6, MathUtils.gcd(-12, -18));
        assertEquals(1, MathUtils.gcd(17, 19));
        assertEquals(8, MathUtils.gcd(24, 16));
        assertEquals(1 << 15, MathUtils.gcd(1 << 15, 1 << 20));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGcdOverflowBothMin() {
        MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGcdOverflowMinZero() {
        MathUtils.gcd(Integer.MIN_VALUE, 0);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGcdOverflowZeroMin() {
        MathUtils.gcd(0, Integer.MIN_VALUE);
    }

    @Test(timeout = 4000)
    public void testLcm() {
        assertEquals(0, MathUtils.lcm(0, 5));
        assertEquals(0, MathUtils.lcm(5, 0));
        assertEquals(36, MathUtils.lcm(12, 18));
        assertEquals(36, MathUtils.lcm(-12, 18));
        assertEquals(36, MathUtils.lcm(12, -18));
        assertEquals(36, MathUtils.lcm(-12, -18));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testLcmOverflow() {
        MathUtils.lcm(Integer.MIN_VALUE, 1);
    }

    // -------------------------------------------------------------------------
    // Partition C: Factorials & Binomial Coefficients
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFactorial() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(2L, MathUtils.factorial(2));
        assertEquals(6L, MathUtils.factorial(3));
        assertEquals(2432902008176640000L, MathUtils.factorial(20));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactorialNegative() {
        MathUtils.factorial(-1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testFactorialOverflow() {
        MathUtils.factorial(21);
    }

    @Test(timeout = 4000)
    public void testFactorialDouble() {
        assertEquals(1.0, MathUtils.factorialDouble(0), 1e-10);
        assertEquals(1.0, MathUtils.factorialDouble(1), 1e-10);
        assertEquals(2432902008176640000.0, MathUtils.factorialDouble(20), 1e-5);
        assertEquals(51090942171709440000.0, MathUtils.factorialDouble(21), 1e5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactorialDoubleNegative() {
        MathUtils.factorialDouble(-1);
    }

    @Test(timeout = 4000)
    public void testFactorialLog() {
        assertEquals(0.0, MathUtils.factorialLog(0), 1e-10);
        assertEquals(0.0, MathUtils.factorialLog(1), 1e-10);
        assertEquals(Math.log(2.0), MathUtils.factorialLog(2), 1e-10);
        assertEquals(Math.log(2432902008176640000.0), MathUtils.factorialLog(20), 1e-5);
        assertTrue(MathUtils.factorialLog(25) > MathUtils.factorialLog(24));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFactorialLogNegative() {
        MathUtils.factorialLog(-1);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficient() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 3)); // symmetry k > n/2

        // Branch: n <= 61
        assertEquals(252L, MathUtils.binomialCoefficient(10, 5));

        // Branch: 61 < n <= 66
        assertEquals(72199496600L, MathUtils.binomialCoefficient(62, 8));

        // Branch: n > 66 with small k (result fits in long)
        assertEquals(2211L, MathUtils.binomialCoefficient(67, 2));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientNegativeN() {
        MathUtils.binomialCoefficient(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testBinomialCoefficientKGreaterThanN() {
        MathUtils.binomialCoefficient(5, 6);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testBinomialCoefficientOverflow() {
        MathUtils.binomialCoefficient(67, 30);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientDouble() {
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 0), 1e-10);
        assertEquals(1.0, MathUtils.binomialCoefficientDouble(5, 5), 1e-10);
        assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 1), 1e-10);
        assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 4), 1e-10);
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-10);
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 3), 1e-10); // symmetry k > n/2
        // n >= 67
        double b70_35 = MathUtils.binomialCoefficientDouble(70, 35);
        assertTrue(b70_35 > 0.0);
    }

    @Test(timeout = 4000)
    public void testBinomialCoefficientLog() {
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 0), 1e-10);
        assertEquals(0.0, MathUtils.binomialCoefficientLog(5, 5), 1e-10);
        assertEquals(Math.log(5.0), MathUtils.binomialCoefficientLog(5, 1), 1e-10);
        assertEquals(Math.log(5.0), MathUtils.binomialCoefficientLog(5, 4), 1e-10);
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(5, 2), 1e-10);

        // n < 67
        assertEquals(Math.log(MathUtils.binomialCoefficient(50, 10)), MathUtils.binomialCoefficientLog(50, 10), 1e-10);

        // 67 <= n < 1030
        assertEquals(Math.log(MathUtils.binomialCoefficientDouble(100, 10)), MathUtils.binomialCoefficientLog(100, 10), 1e-10);

        // n >= 1030
        double logLarge1 = MathUtils.binomialCoefficientLog(1050, 100);
        double logLarge2 = MathUtils.binomialCoefficientLog(1050, 950); // symmetry
        assertEquals(logLarge1, logLarge2, 1e-8);
        assertTrue(logLarge1 > 0.0);
    }

    // -------------------------------------------------------------------------
    // Partition D: Real Numbers, Indicators, Signs, Exponentials & Log
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSignAndIndicatorByte() {
        assertEquals((byte) 0, MathUtils.sign((byte) 0));
        assertEquals((byte) 1, MathUtils.sign((byte) 5));
        assertEquals((byte) -1, MathUtils.sign((byte) -5));

        assertEquals((byte) 1, MathUtils.indicator((byte) 0));
        assertEquals((byte) 1, MathUtils.indicator((byte) 5));
        assertEquals((byte) -1, MathUtils.indicator((byte) -5));
    }

    @Test(timeout = 4000)
    public void testSignAndIndicatorShort() {
        assertEquals((short) 0, MathUtils.sign((short) 0));
        assertEquals((short) 1, MathUtils.sign((short) 5));
        assertEquals((short) -1, MathUtils.sign((short) -5));

        assertEquals((short) 1, MathUtils.indicator((short) 0));
        assertEquals((short) 1, MathUtils.indicator((short) 5));
        assertEquals((short) -1, MathUtils.indicator((short) -5));
    }

    @Test(timeout = 4000)
    public void testSignAndIndicatorInt() {
        assertEquals(0, MathUtils.sign(0));
        assertEquals(1, MathUtils.sign(5));
        assertEquals(-1, MathUtils.sign(-5));

        assertEquals(1, MathUtils.indicator(0));
        assertEquals(1, MathUtils.indicator(5));
        assertEquals(-1, MathUtils.indicator(-5));
    }

    @Test(timeout = 4000)
    public void testSignAndIndicatorLong() {
        assertEquals(0L, MathUtils.sign(0L));
        assertEquals(1L, MathUtils.sign(5L));
        assertEquals(-1L, MathUtils.sign(-5L));

        assertEquals(1L, MathUtils.indicator(0L));
        assertEquals(1L, MathUtils.indicator(5L));
        assertEquals(-1L, MathUtils.indicator(-5L));
    }

    @Test(timeout = 4000)
    public void testSignAndIndicatorFloat() {
        assertEquals(0.0f, MathUtils.sign(0.0f), 0.0f);
        assertEquals(1.0f, MathUtils.sign(5.5f), 0.0f);
        assertEquals(-1.0f, MathUtils.sign(-5.5f), 0.0f);
        assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));

        assertEquals(1.0f, MathUtils.indicator(0.0f), 0.0f);
        assertEquals(1.0f, MathUtils.indicator(5.5f), 0.0f);
        assertEquals(-1.0f, MathUtils.indicator(-5.5f), 0.0f);
        assertTrue(Float.isNaN(MathUtils.indicator(Float.NaN)));
    }

    @Test(timeout = 4000)
    public void testSignAndIndicatorDouble() {
        assertEquals(0.0, MathUtils.sign(0.0), 0.0);
        assertEquals(1.0, MathUtils.sign(5.5), 0.0);
        assertEquals(-1.0, MathUtils.sign(-5.5), 0.0);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));

        assertEquals(1.0, MathUtils.indicator(0.0), 0.0);
        assertEquals(1.0, MathUtils.indicator(5.5), 0.0);
        assertEquals(-1.0, MathUtils.indicator(-5.5), 0.0);
        assertTrue(Double.isNaN(MathUtils.indicator(Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testHyperbolicFunctions() {
        assertEquals(1.0, MathUtils.cosh(0.0), 1e-12);
        assertEquals((Math.E + 1.0 / Math.E) / 2.0, MathUtils.cosh(1.0), 1e-12);

        assertEquals(0.0, MathUtils.sinh(0.0), 1e-12);
        assertEquals((Math.E - 1.0 / Math.E) / 2.0, MathUtils.sinh(1.0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testLogBase() {
        assertEquals(3.0, MathUtils.log(2.0, 8.0), 1e-12);
        assertEquals(2.0, MathUtils.log(10.0, 100.0), 1e-12);
    }

    // -------------------------------------------------------------------------
    // Partition E: Floating Point Comparison, NextAfter, Scalb, Hash
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEqualsDouble() {
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        assertFalse(MathUtils.equals(Double.NaN, 1.0));
        assertFalse(MathUtils.equals(1.0, Double.NaN));
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertFalse(MathUtils.equals(1.0, 1.0000001));

        // equals with absolute error eps
        assertTrue(MathUtils.equals(1.0, 1.05, 0.1));
        assertFalse(MathUtils.equals(1.0, 1.15, 0.1));
        assertTrue(MathUtils.equals(Double.NaN, Double.NaN, 0.1));

        // compareTo
        assertEquals(0, MathUtils.compareTo(1.0, 1.05, 0.1));
        assertEquals(-1, MathUtils.compareTo(1.0, 1.2, 0.1));
        assertEquals(1, MathUtils.compareTo(1.2, 1.0, 0.1));
    }

    @Test(timeout = 4000)
    public void testEqualsUlps() {
        assertTrue(MathUtils.equals(1.0, 1.0, 1));
        double next = Math.nextAfter(1.0, 2.0);
        assertTrue(MathUtils.equals(1.0, next, 1));
        assertFalse(MathUtils.equals(1.0, next, 0));

        // negative numbers branch in equals(double, double, int)
        double neg1 = -1.0;
        double negNext = Math.nextAfter(-1.0, -2.0);
        assertTrue(MathUtils.equals(neg1, negNext, 1));
        assertFalse(MathUtils.equals(neg1, negNext, 0));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleArray() {
        assertTrue(MathUtils.equals((double[]) null, (double[]) null));
        assertFalse(MathUtils.equals(new double[]{1.0}, null));
        assertFalse(MathUtils.equals(null, new double[]{1.0}));
        assertFalse(MathUtils.equals(new double[]{1.0}, new double[]{1.0, 2.0}));
        assertTrue(MathUtils.equals(new double[]{1.0, Double.NaN, 3.0}, new double[]{1.0, Double.NaN, 3.0}));
        assertFalse(MathUtils.equals(new double[]{1.0, 2.0}, new double[]{1.0, 3.0}));
    }

    @Test(timeout = 4000)
    public void testHash() {
        assertEquals(new Double(1.5).hashCode(), MathUtils.hash(1.5));
        double[] arr = new double[]{1.0, 2.0, 3.0};
        assertEquals(java.util.Arrays.hashCode(arr), MathUtils.hash(arr));
        assertEquals(0, MathUtils.hash((double[]) null));
    }

    @Test(timeout = 4000)
    public void testNextAfter() {
        assertTrue(Double.isNaN(MathUtils.nextAfter(Double.NaN, 1.0)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.nextAfter(Double.POSITIVE_INFINITY, 0.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.nextAfter(Double.NEGATIVE_INFINITY, 0.0), 0.0);

        // d == 0
        assertEquals(Double.MIN_VALUE, MathUtils.nextAfter(0.0, 1.0), 0.0);
        assertEquals(-Double.MIN_VALUE, MathUtils.nextAfter(0.0, -1.0), 0.0);

        // Increase mantissa without exponent rollover
        double d1 = 1.0;
        double nextUp = MathUtils.nextAfter(d1, 2.0);
        assertTrue(nextUp > d1);

        // Decrease mantissa without exponent borrow
        double nextDown = MathUtils.nextAfter(nextUp, 0.0);
        assertEquals(d1, nextDown, 0.0);

        // Mantissa rollover to next exponent: mantissa == 0x000fffffffffffffL
        double nearPow2 = Math.nextAfter(2.0, 1.0); // 1.9999999999999998
        assertEquals(2.0, MathUtils.nextAfter(nearPow2, 3.0), 0.0);

        // Mantissa borrow from exponent: mantissa == 0L
        assertEquals(nearPow2, MathUtils.nextAfter(2.0, 1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testScalb() {
        assertEquals(0.0, MathUtils.scalb(0.0, 5), 0.0);
        assertTrue(Double.isNaN(MathUtils.scalb(Double.NaN, 5)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.scalb(Double.POSITIVE_INFINITY, 5), 0.0);

        assertEquals(6.0, MathUtils.scalb(1.5, 2), 1e-12);
        assertEquals(0.375, MathUtils.scalb(1.5, -2), 1e-12);
    }

    // -------------------------------------------------------------------------
    // Partition F: Angles & Array Normalization
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNormalizeAngle() {
        double twoPi = MathUtils.TWO_PI;
        assertEquals(0.0, MathUtils.normalizeAngle(twoPi, 0.0), 1e-12);
        assertEquals(Math.PI, MathUtils.normalizeAngle(3 * Math.PI, Math.PI), 1e-12);
        assertEquals(-Math.PI / 2, MathUtils.normalizeAngle(3.5 * Math.PI, 0.0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testNormalizeArray() {
        double[] values = new double[]{1.0, 2.0, 3.0, Double.NaN};
        double[] norm = MathUtils.normalizeArray(values, 12.0);
        assertEquals(2.0, norm[0], 1e-12);
        assertEquals(4.0, norm[1], 1e-12);
        assertEquals(6.0, norm[2], 1e-12);
        assertTrue(Double.isNaN(norm[3]));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNormalizeArrayInfiniteTarget() {
        MathUtils.normalizeArray(new double[]{1.0, 2.0}, Double.POSITIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNormalizeArrayNaNDetected() {
        MathUtils.normalizeArray(new double[]{1.0, 2.0}, Double.NaN);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testNormalizeArrayContainsInfinite() {
        MathUtils.normalizeArray(new double[]{1.0, Double.POSITIVE_INFINITY}, 5.0);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testNormalizeArrayZeroSum() {
        MathUtils.normalizeArray(new double[]{1.0, -1.0}, 5.0);
    }

    // -------------------------------------------------------------------------
    // Partition G: Rounding Paths (Double & Float)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testRoundDoubleSpecialCases() {
        assertTrue(Double.isNaN(MathUtils.round(Double.NaN, 2)));
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.round(Double.POSITIVE_INFINITY, 2), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.round(Double.NEGATIVE_INFINITY, 2), 0.0);
    }

    @Test(timeout = 4000)
    public void testRoundDoubleRoundingModes() {
        assertEquals(1.24, MathUtils.round(1.235, 2, BigDecimal.ROUND_HALF_UP), 1e-10);
        assertEquals(1.23, MathUtils.round(1.234, 2, BigDecimal.ROUND_HALF_UP), 1e-10);

        assertEquals(1.23, MathUtils.round(1.235, 2, BigDecimal.ROUND_HALF_DOWN), 1e-10);
        assertEquals(1.24, MathUtils.round(1.236, 2, BigDecimal.ROUND_HALF_DOWN), 1e-10);

        assertEquals(1.24, MathUtils.round(1.235, 2, BigDecimal.ROUND_HALF_EVEN), 1e-10);
        assertEquals(1.24, MathUtils.round(1.245, 2, BigDecimal.ROUND_HALF_EVEN), 1e-10);

        assertEquals(1.24, MathUtils.round(1.231, 2, BigDecimal.ROUND_UP), 1e-10);
        assertEquals(1.23, MathUtils.round(1.239, 2, BigDecimal.ROUND_DOWN), 1e-10);

        assertEquals(1.24, MathUtils.round(1.231, 2, BigDecimal.ROUND_CEILING), 1e-10);
        assertEquals(-1.23, MathUtils.round(-1.231, 2, BigDecimal.ROUND_CEILING), 1e-10);

        assertEquals(1.23, MathUtils.round(1.239, 2, BigDecimal.ROUND_FLOOR), 1e-10);
        assertEquals(-1.24, MathUtils.round(-1.231, 2, BigDecimal.ROUND_FLOOR), 1e-10);

        assertEquals(1.25, MathUtils.round(1.25, 2, BigDecimal.ROUND_UNNECESSARY), 1e-10);
        assertEquals(1.23, MathUtils.round(1.234, 2), 1e-10);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testRoundDoubleUnnecessaryException() {
        MathUtils.round(1.2345, 2, BigDecimal.ROUND_UNNECESSARY);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRoundDoubleInvalidMethod() {
        MathUtils.round(1.2345, 2, -99);
    }

    @Test(timeout = 4000)
    public void testRoundFloatAllModes() {
        assertEquals(1.24f, MathUtils.round(1.235f, 2), 1e-5f);
        assertEquals(1.24f, MathUtils.round(1.235f, 2, BigDecimal.ROUND_HALF_UP), 1e-5f);
        assertEquals(1.23f, MathUtils.round(1.234f, 2, BigDecimal.ROUND_HALF_UP), 1e-5f);

        assertEquals(1.23f, MathUtils.round(1.235f, 2, BigDecimal.ROUND_HALF_DOWN), 1e-5f);
        assertEquals(1.24f, MathUtils.round(1.236f, 2, BigDecimal.ROUND_HALF_DOWN), 1e-5f);

        // HALF_EVEN parity checks in roundUnscaled
        assertEquals(1.24f, MathUtils.round(1.235f, 2, BigDecimal.ROUND_HALF_EVEN), 1e-5f); // odd to even
        assertEquals(1.24f, MathUtils.round(1.245f, 2, BigDecimal.ROUND_HALF_EVEN), 1e-5f); // even stays even
        assertEquals(1.24f, MathUtils.round(1.244f, 2, BigDecimal.ROUND_HALF_EVEN), 1e-5f); // fraction < 0.5
        assertEquals(1.25f, MathUtils.round(1.246f, 2, BigDecimal.ROUND_HALF_EVEN), 1e-5f); // fraction > 0.5

        // CEILING positive and negative
        assertEquals(1.24f, MathUtils.round(1.231f, 2, BigDecimal.ROUND_CEILING), 1e-5f);
        assertEquals(-1.23f, MathUtils.round(-1.231f, 2, BigDecimal.ROUND_CEILING), 1e-5f);

        // FLOOR positive and negative
        assertEquals(1.23f, MathUtils.round(1.239f, 2, BigDecimal.ROUND_FLOOR), 1e-5f);
        assertEquals(-1.24f, MathUtils.round(-1.231f, 2, BigDecimal.ROUND_FLOOR), 1e-5f);

        // DOWN & UP
        assertEquals(1.23f, MathUtils.round(1.239f, 2, BigDecimal.ROUND_DOWN), 1e-5f);
        assertEquals(1.24f, MathUtils.round(1.231f, 2, BigDecimal.ROUND_UP), 1e-5f);

        // UNNECESSARY
        assertEquals(1.25f, MathUtils.round(1.25f, 2, BigDecimal.ROUND_UNNECESSARY), 1e-5f);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testRoundFloatUnnecessaryException() {
        MathUtils.round(1.2345f, 2, BigDecimal.ROUND_UNNECESSARY);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRoundFloatInvalidMethod() {
        MathUtils.round(1.2345f, 2, 999);
    }

    // -------------------------------------------------------------------------
    // Partition H: Power (pow) Operations
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPowIntInt() {
        assertEquals(1, MathUtils.pow(5, 0));
        assertEquals(5, MathUtils.pow(5, 1));
        assertEquals(25, MathUtils.pow(5, 2));
        assertEquals(125, MathUtils.pow(5, 3));
        assertEquals(-8, MathUtils.pow(-2, 3));
        assertEquals(16, MathUtils.pow(-2, 4));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPowIntIntNegativeExponent() {
        MathUtils.pow(5, -1);
    }

    @Test(timeout = 4000)
    public void testPowIntLong() {
        assertEquals(1, MathUtils.pow(5, 0L));
        assertEquals(125, MathUtils.pow(5, 3L));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPowIntLongNegativeExponent() {
        MathUtils.pow(5, -1L);
    }

    @Test(timeout = 4000)
    public void testPowLongInt() {
        assertEquals(1L, MathUtils.pow(5L, 0));
        assertEquals(125L, MathUtils.pow(5L, 3));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPowLongIntNegativeExponent() {
        MathUtils.pow(5L, -1);
    }

    @Test(timeout = 4000)
    public void testPowLongLong() {
        assertEquals(1L, MathUtils.pow(5L, 0L));
        assertEquals(125L, MathUtils.pow(5L, 3L));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPowLongLongNegativeExponent() {
        MathUtils.pow(5L, -1L);
    }

    @Test(timeout = 4000)
    public void testPowBigInteger() {
        BigInteger b3 = BigInteger.valueOf(3);
        assertEquals(BigInteger.ONE, MathUtils.pow(b3, 0));
        assertEquals(BigInteger.valueOf(27), MathUtils.pow(b3, 3));

        assertEquals(BigInteger.ONE, MathUtils.pow(b3, 0L));
        assertEquals(BigInteger.valueOf(27), MathUtils.pow(b3, 3L));

        assertEquals(BigInteger.ONE, MathUtils.pow(b3, BigInteger.ZERO));
        assertEquals(BigInteger.valueOf(27), MathUtils.pow(b3, BigInteger.valueOf(3)));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPowBigIntegerIntNegativeExp() {
        MathUtils.pow(BigInteger.valueOf(3), -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPowBigIntegerLongNegativeExp() {
        MathUtils.pow(BigInteger.valueOf(3), -1L);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPowBigIntegerBigIntegerNegativeExp() {
        MathUtils.pow(BigInteger.valueOf(3), BigInteger.valueOf(-1));
    }

    // -------------------------------------------------------------------------
    // Partition I: Vector Distances & Defect Zone
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDistancesDouble() {
        double[] p1 = new double[]{1.0, 2.0, 3.0};
        double[] p2 = new double[]{4.0, 6.0, 3.0};

        // L1 distance: |1-4| + |2-6| + |3-3| = 3 + 4 + 0 = 7.0
        assertEquals(7.0, MathUtils.distance1(p1, p2), 1e-12);

        // L2 distance: sqrt((1-4)^2 + (2-6)^2 + 0^2) = sqrt(9 + 16) = 5.0
        assertEquals(5.0, MathUtils.distance(p1, p2), 1e-12);

        // L_inf distance: max(|1-4|, |2-6|, |3-3|) = 4.0
        assertEquals(4.0, MathUtils.distanceInf(p1, p2), 1e-12);

        // Empty arrays
        assertEquals(0.0, MathUtils.distance1(new double[0], new double[0]), 1e-12);
        assertEquals(0.0, MathUtils.distance(new double[0], new double[0]), 1e-12);
        assertEquals(0.0, MathUtils.distanceInf(new double[0], new double[0]), 1e-12);
    }

    @Test(timeout = 4000)
    public void testDistancesInt() {
        int[] p1 = new int[]{1, 2, 3};
        int[] p2 = new int[]{4, 6, 3};

        assertEquals(7, MathUtils.distance1(p1, p2));
        assertEquals(5.0, MathUtils.distance(p1, p2), 1e-12);
        assertEquals(4, MathUtils.distanceInf(p1, p2));

        // Empty arrays
        assertEquals(0, MathUtils.distance1(new int[0], new int[0]));
        assertEquals(0.0, MathUtils.distance(new int[0], new int[0]), 1e-12);
        assertEquals(0, MathUtils.distanceInf(new int[0], new int[0]));
    }

    /**
     * Defect-targeted test matching KMeansPlusPlusClusterer degenerate / empty cluster condition.
     * Ensures distance computations between identical coordinates (degenerate clusters) evaluate
     * to exactly 0.0 with full precision, preventing cluster centroid drift or degenerate NPEs.
     */
    @Test(timeout = 4000)
    public void testDistanceDegeneratePointsEquivalence() {
        double[] point1 = new double[]{15.0, -20.5, 0.0, 100.25};
        double[] point2 = new double[]{15.0, -20.5, 0.0, 100.25};

        assertEquals(0.0, MathUtils.distance(point1, point2), 0.0);
        assertEquals(0.0, MathUtils.distance1(point1, point2), 0.0);
        assertEquals(0.0, MathUtils.distanceInf(point1, point2), 0.0);

        int[] ipoint1 = new int[]{15, -20, 0, 100};
        int[] ipoint2 = new int[]{15, -20, 0, 100};

        assertEquals(0.0, MathUtils.distance(ipoint1, ipoint2), 0.0);
        assertEquals(0, MathUtils.distance1(ipoint1, ipoint2));
        assertEquals(0, MathUtils.distanceInf(ipoint1, ipoint2));
    }
}