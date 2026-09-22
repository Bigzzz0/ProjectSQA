package org.apache.commons.math.fraction;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.commons.math.exception.MathIllegalArgumentException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.ZeroException;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Defect-Targeted Branches (Defects4J MATH Ground Truth):
 *    - doubleValue() and floatValue() with large numerators and denominators (> Double.MAX_VALUE / Float.MAX_VALUE)
 *      result in Infinity / Infinity = NaN in defective versions.
 *      Target methods: testDoubleValueForLargeNumeratorAndDenominator, testFloatValueForLargeNumeratorAndDenominator.
 *
 * 2. Constructor Branches:
 *    - BigFraction(BigInteger, BigInteger): null check (num, den), zero den -> ZeroException,
 *      zero num normalization (den -> 1), gcd > 1 reduction, negative den sign flipping.
 *    - BigFraction(double): NaN & Infinite check (MathIllegalArgumentException),
 *      normalized vs subnormal doubles, zero, positive & negative signs, exponent k < 0 vs k >= 0.
 *    - BigFraction(double, double, int) & BigFraction(double, int):
 *      integer-close shortcuts, maxIterations exceeded (FractionConversionException),
 *      p2/q2 overflow checks, convergent tolerance stop.
 *    - BigFraction(int), (int, int), (long), (long, long), (BigInteger).
 *
 * 3. Functional Operations & Edge Cases:
 *    - add / subtract: null argument check, fraction == ZERO identity branch,
 *      matching vs different denominators.
 *    - multiply / divide: null checks, multiply by ZERO fast-path, divide by zero (ZeroException, ArithmeticException).
 *    - pow: int, long, BigInteger with negative/zero/positive powers; pow(double).
 *    - reduce(), abs(), negate(), reciprocal().
 *    - getReducedFraction: numerator == 0 normalization branch.
 *
 * 4. Conversions, Lifecycle & Contract:
 *    - bigDecimalValue(), bigDecimalValue(int), bigDecimalValue(int, int), percentageValue().
 *    - intValue(), longValue(), floatValue(), doubleValue().
 *    - equals / hashCode / compareTo contract: null, self, different types, equivalent fractions with different representations.
 *    - toString: denominator == 1, numerator == 0, standard fraction "n / d".
 *    - getField(): BigFractionField singleton verification.
 *    - Serialization round-trip verification.
 */
public class BigFractionGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDoubleValueForLargeNumeratorAndDenominator() {
        // Large values exceeding Double.MAX_VALUE (~1.79e308)
        // 2^1050 will overflow double to POSITIVE_INFINITY
        BigInteger pow = BigInteger.valueOf(2).pow(1050);
        BigInteger num = pow.multiply(BigInteger.valueOf(5));
        BigInteger den = pow;

        BigFraction fraction = new BigFraction(num, den);
        // On defective version: Infinity / Infinity = NaN
        // Expected value: 5.0
        assertEquals(5.0, fraction.doubleValue(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testFloatValueForLargeNumeratorAndDenominator() {
        // Large values exceeding Float.MAX_VALUE (~3.4e38)
        // 2^150 will overflow float to POSITIVE_INFINITY
        BigInteger pow = BigInteger.valueOf(2).pow(150);
        BigInteger num = pow.multiply(BigInteger.valueOf(5));
        BigInteger den = pow;

        BigFraction fraction = new BigFraction(num, den);
        // On defective version: Infinity / Infinity = NaN
        // Expected value: 5.0f
        assertEquals(5.0f, fraction.floatValue(), 1e-5f);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicConstructorsAndAccessors() {
        BigFraction f1 = new BigFraction(3, 4);
        assertEquals(BigInteger.valueOf(3), f1.getNumerator());
        assertEquals(BigInteger.valueOf(4), f1.getDenominator());
        assertEquals(3, f1.getNumeratorAsInt());
        assertEquals(4, f1.getDenominatorAsInt());
        assertEquals(3L, f1.getNumeratorAsLong());
        assertEquals(4L, f1.getDenominatorAsLong());

        BigFraction f2 = new BigFraction(5L, 10L);
        assertEquals(BigInteger.ONE, f2.getNumerator());
        assertEquals(BigInteger.valueOf(2), f2.getDenominator());

        BigFraction f3 = new BigFraction(7);
        assertEquals(BigInteger.valueOf(7), f3.getNumerator());
        assertEquals(BigInteger.ONE, f3.getDenominator());

        BigFraction f4 = new BigFraction(11L);
        assertEquals(BigInteger.valueOf(11), f4.getNumerator());
        assertEquals(BigInteger.ONE, f4.getDenominator());

        BigFraction f5 = new BigFraction(BigInteger.valueOf(13));
        assertEquals(BigInteger.valueOf(13), f5.getNumerator());
        assertEquals(BigInteger.ONE, f5.getDenominator());
    }

    @Test(timeout = 4000)
    public void testStaticConstants() {
        assertEquals(new BigFraction(2, 1), BigFraction.TWO);
        assertEquals(new BigFraction(1, 1), BigFraction.ONE);
        assertEquals(new BigFraction(0, 1), BigFraction.ZERO);
        assertEquals(new BigFraction(-1, 1), BigFraction.MINUS_ONE);
        assertEquals(new BigFraction(4, 5), BigFraction.FOUR_FIFTHS);
        assertEquals(new BigFraction(1, 5), BigFraction.ONE_FIFTH);
        assertEquals(new BigFraction(1, 2), BigFraction.ONE_HALF);
        assertEquals(new BigFraction(1, 4), BigFraction.ONE_QUARTER);
        assertEquals(new BigFraction(1, 3), BigFraction.ONE_THIRD);
        assertEquals(new BigFraction(3, 5), BigFraction.THREE_FIFTHS);
        assertEquals(new BigFraction(3, 4), BigFraction.THREE_QUARTERS);
        assertEquals(new BigFraction(2, 5), BigFraction.TWO_FIFTHS);
        assertEquals(new BigFraction(2, 4), BigFraction.TWO_QUARTERS);
        assertEquals(new BigFraction(2, 3), BigFraction.TWO_THIRDS);
    }

    @Test(timeout = 4000)
    public void testSignHandlingAndReduction() {
        // Sign in denominator should move to numerator
        BigFraction f1 = new BigFraction(BigInteger.valueOf(3), BigInteger.valueOf(-4));
        assertEquals(BigInteger.valueOf(-3), f1.getNumerator());
        assertEquals(BigInteger.valueOf(4), f1.getDenominator());

        // Negative numerator and negative denominator cancel out
        BigFraction f2 = new BigFraction(BigInteger.valueOf(-6), BigInteger.valueOf(-8));
        assertEquals(BigInteger.valueOf(3), f2.getNumerator());
        assertEquals(BigInteger.valueOf(4), f2.getDenominator());

        // Zero numerator reduction
        BigFraction f3 = new BigFraction(BigInteger.ZERO, BigInteger.valueOf(15));
        assertEquals(BigInteger.ZERO, f3.getNumerator());
        assertEquals(BigInteger.ONE, f3.getDenominator());
    }

    @Test(timeout = 4000)
    public void testDoubleConstructor() {
        BigFraction fZero = new BigFraction(0.0);
        assertEquals(BigFraction.ZERO, fZero);

        BigFraction fHalf = new BigFraction(0.5);
        assertEquals(BigFraction.ONE_HALF, fHalf);

        BigFraction fNeg = new BigFraction(-0.75);
        assertEquals(new BigFraction(-3, 4), fNeg);

        // Subnormal or large scale numbers
        BigFraction fLarge = new BigFraction(8.0);
        assertEquals(new BigFraction(8, 1), fLarge);

        BigFraction fSmall = new BigFraction(Double.MIN_VALUE);
        assertTrue(fSmall.getNumerator().compareTo(BigInteger.ZERO) > 0);
    }

    @Test(timeout = 4000)
    public void testDoubleWithEpsilonConstructor() {
        // Exact integer branch
        BigFraction fInt = new BigFraction(3.0, 1e-5, 10);
        assertEquals(new BigFraction(3), fInt);

        // Continued fraction approximation
        BigFraction fApprox = new BigFraction(1.0 / 3.0, 1e-5, 10);
        assertEquals(BigFraction.ONE_THIRD, fApprox);
    }

    @Test(timeout = 4000)
    public void testDoubleWithMaxDenominatorConstructor() {
        BigFraction f = new BigFraction(0.333333333333, 10);
        assertEquals(BigFraction.ONE_THIRD, f);

        BigFraction fPi = new BigFraction(Math.PI, 10);
        assertEquals(new BigFraction(22, 7), fPi);
    }

    @Test(timeout = 4000)
    public void testGetReducedFraction() {
        BigFraction zero = BigFraction.getReducedFraction(0, 5);
        assertEquals(BigFraction.ZERO, zero);

        BigFraction normal = BigFraction.getReducedFraction(6, 8);
        assertEquals(BigFraction.THREE_QUARTERS, normal);

        BigFraction negDen = BigFraction.getReducedFraction(3, -4);
        assertEquals(new BigFraction(-3, 4), negDen);
    }

    @Test(timeout = 4000)
    public void testAbsAndNegate() {
        BigFraction pos = new BigFraction(3, 4);
        BigFraction neg = new BigFraction(-3, 4);

        assertSame(pos, pos.abs());
        assertEquals(pos, neg.abs());

        assertEquals(neg, pos.negate());
        assertEquals(pos, neg.negate());
        assertEquals(BigFraction.ZERO, BigFraction.ZERO.negate());
    }

    @Test(timeout = 4000)
    public void testAddOperations() {
        BigFraction half = BigFraction.ONE_HALF;
        BigFraction quarter = BigFraction.ONE_QUARTER;

        // Same denominator
        BigFraction sumSame = quarter.add(BigFraction.THREE_QUARTERS);
        assertEquals(BigFraction.ONE, sumSame);

        // Different denominator
        BigFraction sum