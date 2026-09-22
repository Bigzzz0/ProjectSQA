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
package org.apache.commons.math.fraction;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------------------
 * Target Defect:
 * - org.apache.commons.math.fraction.FractionTest::testCompareTo (Defects4J MATH failure)
 *   Root Cause: compareTo converts fractions directly to doubleValue(). For two distinct fractions whose
 *   difference is smaller than double precision (53 bits mantissa), both double conversions produce identical
 *   double values, returning 0 instead of -1 / +1.
 *   Targeted in: testCompareToPrecisionDefect() using pi approximations 1068966896/340262731 and 411557987/131002976.
 *
 * Targeted Decision Branches & Boundaries:
 * - Fraction(double, double, int, int):
 *     1. a0 > overflow -> FractionConversionException
 *     2. |a0 - value| < epsilon -> direct integer path
 *     3. p2 > overflow || q2 > overflow -> FractionConversionException
 *     4. convergent loop condition: n < maxIterations && |convergent - value| > epsilon && q2 < maxDenominator
 *     5. n >= maxIterations -> FractionConversionException
 *     6. q2 < maxDenominator branch (true: take p2, q2; false: take p1, q1)
 * - Fraction(int, int):
 *     1. den == 0 -> ArithmeticException
 *     2. den < 0 with num == MIN_VALUE or den == MIN_VALUE -> ArithmeticException
 *     3. den < 0 sign flip -> num = -num, den = -den
 *     4. gcd reduction (gcd > 1)
 * - abs(), negate():
 *     1. num >= 0 vs num < 0
 *     2. negate() with num == Integer.MIN_VALUE -> ArithmeticException
 * - reciprocal():
 *     1. normal reciprocal, handling sign
 * - compareTo(Fraction):
 *     1. < 0, == 0, > 0 paths
 *     2. Extreme values close to double-precision threshold
 * - equals(Object) & hashCode():
 *     1. this == other (reflexive)
 *     2. other == null
 *     3. other not instance of Fraction (ClassCastException path)
 *     4. matching num & den, differing num, differing den
 *     5. hashCode contract
 * - add(Fraction) & subtract(Fraction) & addSub():
 *     1. null parameter -> IllegalArgumentException
 *     2. this.numerator == 0 (addition identity / negation for sub)
 *     3. fraction.numerator == 0
 *     4. gcd(den1, den2) == 1 (coprime path) with mulAndCheck/addAndCheck/subAndCheck
 *     5. gcd(den1, den2) != 1 (common factor path using BigInteger)
 *     6. w.bitLength() > 31 -> ArithmeticException
 *     7. Arithmetic overflow in denominator multiplication -> ArithmeticException
 * - multiply(Fraction):
 *     1. null parameter -> IllegalArgumentException
 *     2. numerator == 0 or fraction.numerator == 0 -> ZERO
 *     3. normal multiplication reduced by gcd
 *     4. overflow in mulAndCheck -> ArithmeticException
 * - divide(Fraction):
 *     1. null parameter -> IllegalArgumentException
 *     2. fraction.numerator == 0 -> ArithmeticException
 *     3. normal division via reciprocal
 * - getReducedFraction(int, int):
 *     1. den == 0 -> ArithmeticException
 *     2. num == 0 -> ZERO
 *     3. den == MIN_VALUE && (num & 1) == 0 -> reduction by 2
 *     4. den < 0 with num == MIN_VALUE or den == MIN_VALUE -> ArithmeticException
 *     5. normal negative denominator inversion and gcd reduction
 */
public class FractionGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstants() {
        assertEquals(0, Fraction.ZERO.getNumerator());
        assertEquals(1, Fraction.ZERO.getDenominator());

        assertEquals(1, Fraction.ONE.getNumerator());
        assertEquals(1, Fraction.ONE.getDenominator());

        assertEquals(2, Fraction.TWO.getNumerator());
        assertEquals(1, Fraction.TWO.getDenominator());

        assertEquals(-1, Fraction.MINUS_ONE.getNumerator());
        assertEquals(1, Fraction.MINUS_ONE.getDenominator());
    }

    @Test(timeout = 4000)
    public void testBasicConstructorAndReductions() {
        Fraction f = new Fraction(4, 6);
        assertEquals(2, f.getNumerator());
        assertEquals(3, f.getDenominator());

        Fraction fNegDen = new Fraction(2, -3);
        assertEquals(-2, fNegDen.getNumerator());
        assertEquals(3, fNegDen.getDenominator());

        Fraction fBothNeg = new Fraction(-2, -4);
        assertEquals(1, fBothNeg.getNumerator());
        assertEquals(2, fBothNeg.getDenominator());
    }

    @Test(timeout = 4000)
    public void testNumberConversions() {
        Fraction f = new Fraction(3, 2);
        assertEquals(1.5, f.doubleValue(), 1e-15);
        assertEquals(1.5f, f.floatValue(), 1e-7f);
        assertEquals(1, f.intValue());
        assertEquals(1L, f.longValue());

        Fraction neg = new Fraction(-7, 2);
        assertEquals(-3.5, neg.doubleValue(), 1e-15);
        assertEquals(-3, neg.intValue());
        assertEquals(-3L, neg.longValue());
    }

    @Test(timeout = 4000)
    public void testAbs() {
        Fraction pos = new Fraction(3, 4);
        assertSame(pos, pos.abs());

        Fraction neg = new Fraction(-3, 4);
        Fraction absNeg = neg.abs();
        assertEquals(3, absNeg.getNumerator());
        assertEquals(4, absNeg.getDenominator());

        Fraction zero = Fraction.ZERO.abs();
        assertEquals(0, zero.getNumerator());
        assertEquals(1, zero.getDenominator());
    }

    @Test(timeout = 4000)
    public void testReciprocal() {
        Fraction f = new Fraction(3, 4);
        Fraction r = f.reciprocal();
        assertEquals(4, r.getNumerator());
        assertEquals(3, r.getDenominator());

        Fraction neg = new Fraction(-2, 5);
        Fraction rNeg = neg.reciprocal();
        assertEquals(-5, rNeg.getNumerator());
        assertEquals(2, rNeg.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddAndSubtractNormal() {
        // Coprime denominators (d1 == 1)
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 3);
        Fraction sum = f1.add(f2);
        assertEquals(5, sum.getNumerator());
        assertEquals(6, sum.getDenominator());

        Fraction diff = f1.subtract(f2);
        assertEquals(1, diff.getNumerator());
        assertEquals(6, diff.getDenominator());

        // Denominators sharing common factors (d1 > 1)
        Fraction f3 = new Fraction(1, 6);
        Fraction f4 = new Fraction(1, 4);
        Fraction sumCommon = f3.add(f4);
        assertEquals(5, sumCommon.getNumerator());
        assertEquals(12, sumCommon.getDenominator());

        Fraction diffCommon = f3.subtract(f4);
        assertEquals(-1, diffCommon.getNumerator());
        assertEquals(12, diffCommon.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddAndSubtractIdentity() {
        Fraction f = new Fraction(3, 5);

        // 0 + f = f
        assertEquals(f, Fraction.ZERO.add(f));
        // f + 0 = f
        assertEquals(f, f.add(Fraction.ZERO));

        // 0 - f = -f
        Fraction negF = Fraction.ZERO.subtract(f);
        assertEquals(-3, negF.getNumerator());
        assertEquals(5, negF.getDenominator());

        // f - 0 = f
        assertEquals(f, f.subtract(Fraction.ZERO));
    }

    @Test(timeout = 4000)
    public void testMultiplyAndDivideNormal() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(3, 4);
        Fraction prod = f1.multiply(f2);
        assertEquals(1, prod.getNumerator());
        assertEquals(2, prod.getDenominator());

        Fraction quot = f1.divide(f2);
        assertEquals(8, quot.getNumerator());
        assertEquals(9, quot.getDenominator());

        // Multiply by ZERO
        assertEquals(Fraction.ZERO, f1.multiply(Fraction.ZERO));
        assertEquals(Fraction.ZERO, Fraction.ZERO.multiply(f1));
    }

    @Test(timeout = 4000)
    public void testDoubleConstructorsNormal() throws FractionConversionException {
        // Fast integer path (|a0 - value| < epsilon)
        Fraction intVal = new Fraction(5.0);
        assertEquals(5, intVal.getNumerator());
        assertEquals(1, intVal.getDenominator());

        Fraction negIntVal = new Fraction(-4.0);
        assertEquals(-4, negIntVal.getNumerator());
        assertEquals(1, negIntVal.getDenominator());

        // Standard fraction from double
        Fraction half = new Fraction(0.5);
        assertEquals(1, half.getNumerator());
        assertEquals(2, half.getDenominator());

        // With maxDenominator constraint
        Fraction approx = new Fraction(0.6152, 10);
        assertEquals(3, approx.getNumerator());
        assertEquals(5, approx.getDenominator());

        // With epsilon and maxIterations
        Fraction custom = new Fraction(0.33333333, 1e-4, 10);
        assertEquals(1, custom.getNumerator());
        assertEquals(3, custom.getDenominator());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetReducedFractionBoundaries() {
        assertEquals(Fraction.ZERO, Fraction.getReducedFraction(0, 10));
        assertEquals(Fraction.ZERO, Fraction.getReducedFraction(0, -5));

        // Normal reduction
        Fraction f = Fraction.getReducedFraction(6, 8);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());

        // Negative denominator
        Fraction fNeg = Fraction.getReducedFraction(3, -4);
        assertEquals(-3, fNeg.getNumerator());
        assertEquals(4, fNeg.getDenominator());

        // Even numerator and Integer.MIN_VALUE denominator: (2^k / -2^31) path
        Fraction fMinEven = Fraction.getReducedFraction(2, Integer.MIN_VALUE);
        assertEquals(-1, fMinEven.getNumerator());
        assertEquals(1073741824, fMinEven.getDenominator());

        Fraction fMinNegEven = Fraction.getReducedFraction(-4, Integer.MIN_VALUE);
        assertEquals(1, fMinNegEven.getNumerator());
        assertEquals(536870912, fMinNegEven.getDenominator());
    }

    @Test(timeout = 4000)
    public void testCompareToStandardBranches() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(2, 3);
        Fraction f3 = new Fraction(1, 2);

        assertTrue(f1.compareTo(f2) < 0);
        assertTrue(f2.compareTo(f1) > 0);
        assertEquals(0, f1.compareTo(f3));
        assertEquals(0, f1.compareTo(f1));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Defect)
    // =========================================================================

    /**
     * Targets the precision defect in compareTo(Fraction).
     * Two fractions with distinct rational values whose double conversions are identical
     * due to 53-bit IEEE 754 precision limits must NOT be considered equal by compareTo.
     *
     * Expected failure on defective version:
     * junit.framework.AssertionFailedError: expected:<-1> but was:<0>
     */
    @Test(timeout = 4000)
    public void testCompareToPrecisionDefect() {
        Fraction pi1 = new Fraction(1068966896, 340262731);
        Fraction pi2 = new Fraction(411557987, 131002976);

        // pi1 * 131002976 = 140037845722383488
        // pi2 * 340262731 = 140037845722383507
        // Mathematically: pi1 < pi2, so pi1.compareTo(pi2) should be -1 and pi2.compareTo(pi1) should be 1
        assertEquals(-1, pi1.compareTo(pi2));
        assertEquals(1, pi2.compareTo(pi1));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testZeroDenominatorInConstructor() {
        new Fraction(1, 0);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testNegateOverflowInConstructorMinNumerator() {
        new Fraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testNegateOverflowInConstructorMinDenominator() {
        new Fraction(1, Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testNegateArithmeticException() {
        // Valid creation: num = MIN_VALUE, den = 1
        Fraction f = new Fraction(Integer.MIN_VALUE, 1);
        f.negate();
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetReducedFractionZeroDenominator() {
        Fraction.getReducedFraction(5, 0);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetReducedFractionMinDenominatorOddNumerator() {
        // Odd numerator cannot be divided by 2 when denominator is MIN_VALUE
        Fraction.getReducedFraction(1, Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetReducedFractionMinNumeratorNegDenominator() {
        Fraction.getReducedFraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddNull() {
        Fraction.ONE.add(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSubtractNull() {
        Fraction.ONE.subtract(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMultiplyNull() {
        Fraction.ONE.multiply(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDivideNull() {
        Fraction.ONE.divide(null);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testDivideByZeroFraction() {
        Fraction.ONE.divide(Fraction.ZERO);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddOverflowCoprimeDenominators() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE - 1, 1);
        Fraction f2 = new Fraction(2, 1);
        f1.add(f2);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddOverflowCommonDenominator() {
        // Denominators share gcd(2, 4) = 2.
        // BigInteger w bit length exceeds 31: 3 * Integer.MAX_VALUE > Integer.MAX_VALUE
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 2);
        Fraction f2 = new Fraction(Integer.MAX_VALUE, 4);
        f1.add(f2);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMultiplyOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(2, 1);
        f1.multiply(f2);
    }

    @Test(expected = FractionConversionException.class, timeout = 4000)
    public void testDoubleConstructorOverflowInitialValue() throws FractionConversionException {
        new Fraction((double) Integer.MAX_VALUE + 100.0);
    }

    @Test(expected = FractionConversionException.class, timeout = 4000)
    public void testDoubleConstructorExceedMaxIterations() throws FractionConversionException {
        // An irrational / non-terminating fraction with 0 epsilon and only 2 iterations
        new Fraction(0.123456789012345, 1e-20, 2);
    }

    @Test(expected = FractionConversionException.class, timeout = 4000)
    public void testDoubleConstructorOverflowIntermediate() throws FractionConversionException {
        // Continued fraction generates denominator/numerator exceeding Integer.MAX_VALUE
        new Fraction(1.0e-11, 1.0e-20, 100);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(2, 4); // Reduces to 1/2
        Fraction f3 = new Fraction(1, 3);
        Fraction f4 = new Fraction(2, 2);

        // Reflexive
        assertTrue(f1.equals(f1));

        // Symmetric
        assertTrue(f1.equals(f2));
        assertTrue(f2.equals(f1));

        // Equal objects must have equal hashCodes
        assertEquals(f1.hashCode(), f2.hashCode());

        // Inequivalent
        assertFalse(f1.equals(f3));
        assertFalse(f1.equals(f4));
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("NotAFraction"));
    }
}