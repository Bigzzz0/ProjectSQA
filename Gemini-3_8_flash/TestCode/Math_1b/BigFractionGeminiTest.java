package org.apache.commons.math3.fraction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.ZeroException;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. DEFECT REPRODUCTION TARGET (MATH-1 / MATH-995):
 *    - FractionConversionException on digit-limit / maxDenominator conversions (e.g. 0.5 with digit limits).
 *    - Validates correct convergence without false overflow exceptions when q2 reaches maxDenominator limits.
 *
 * 2. CONSTRUCTORS & FACTORIES:
 *    - BigFraction(BigInteger): normal, zero, negative.
 *    - BigFraction(BigInteger, BigInteger): null guards, zero denominator, zero numerator, gcd reduction, negative sign normalization.
 *    - BigFraction(int), BigFraction(int, int), BigFraction(long), BigFraction(long, long).
 *    - BigFraction(double): NaN, Infinity, subnormal/normalized bits, k < 0 vs k >= 0 bit shifts, positive/negative.
 *    - BigFraction(double, double, int): epsilon integer bypass, convergent loops, overflow exceptions, maxIterations exhaustion.
 *    - BigFraction(double, int): maxDenominator boundary convergence.
 *    - getReducedFraction(int, int): zero numerator identity, reduction, zero denominator guard.
 *
 * 3. ARITHMETIC OPERATIONS & BRANCHES:
 *    - add(BigInteger|int|long|BigFraction): null check, zero identity, same denominator vs different denominator.
 *    - subtract(BigInteger|int|long|BigFraction): null check, zero identity, same denominator vs different denominator.
 *    - multiply(BigInteger|int|long|BigFraction): null check, zero numerator short-circuit, general multiplication.
 *    - divide(BigInteger|int|long|BigFraction): null check, zero divisor guards (MathArithmeticException), reciprocal multiplication.
 *    - negate(), abs(): positive, negative, zero branches.
 *    - reciprocal(): inverted numerator and denominator.
 *    - pow(int), pow(long), pow(BigInteger), pow(double): negative exponents, zero exponent, positive exponents.
 *
 * 4. CONVERSIONS & NUMERIC INTERFACES:
 *    - doubleValue(): standard ratio, bit-shift branch for extreme bit lengths triggering NaN (out-of-range exponents).
 *    - floatValue(): standard ratio, bit-shift branch for extreme bit lengths triggering NaN (out-of-range exponents).
 *    - intValue(), longValue(), percentageValue().
 *    - bigDecimalValue(), bigDecimalValue(roundingMode), bigDecimalValue(scale, roundingMode), non-terminating division guard.
 *
 * 5. EQUALITY, SERIALIZATION & FIELD CONTRACTS:
 *    - equals(), hashCode(), compareTo(): reflexivity, null, non-BigFraction class, reduced equivalence.
 *    - toString(): denominator == 1, numerator == 0, standard "num / den".
 *    - getField(): BigFractionField singleton identity.
 *    - Java Serialization round-trip verification.
 */
public class BigFractionGeminiTest {

    private void assertFraction(int expectedNumerator, int expectedDenominator, BigFraction actual) {
        assertEquals("Numerator mismatch", BigInteger.valueOf(expectedNumerator), actual.getNumerator());
        assertEquals("Denominator mismatch", BigInteger.valueOf(expectedDenominator), actual.getDenominator());
    }

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED ZONE (MATH-1 / MATH-995)
    // =========================================================================

    /**
     * Targets Defects4J Math-1 / MATH-995:
     * Overflow trying to convert double (e.g. 0.5) to fraction in maxDenominator constructor.
     */
    @Test(timeout = 4000)
    public void testDigitLimitConstructorDefectMath1() {
        assertFraction(1, 2, new BigFraction(0.5, 2));
        assertFraction(1, 2, new BigFraction(0.5, 9));
        assertFraction(1, 2, new BigFraction(0.5, 10));
        assertFraction(1, 2, new BigFraction(0.5, 99));
        assertFraction(1, 2, new BigFraction(0.5, 100));
        assertFraction(1, 2, new BigFraction(0.5, 1000));
        assertFraction(1, 2, new BigFraction(0.5, Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testDigitLimitConstructorStandardCases() {
        assertFraction(2, 5, new BigFraction(0.4, 9));
        assertFraction(2, 5, new BigFraction(0.4, 99));
        assertFraction(2, 5, new BigFraction(0.4, 999));

        assertFraction(3, 5, new BigFraction(0.6152, 9));
        assertFraction(8, 13, new BigFraction(0.6152, 99));
        assertFraction(510, 829, new BigFraction(0.6152, 999));
        assertFraction(769, 1250, new BigFraction(0.6152, 9999));

        assertFraction(548816, 22449, new BigFraction(24.447222, 22956));
        assertFraction(284680, 11645, new BigFraction(24.447222, 11999));
        assertFraction(52039, 2128, new BigFraction(24.447222, 2500));
        assertFraction(43282, 1770, new BigFraction(24.447222, 2000));
        assertFraction(2445, 100, new BigFraction(24.447222, 100));
        assertFraction(245, 10, new BigFraction(24.447222, 10));
    }

    // =========================================================================
    // PARTITION A: CORE CONSTRUCTORS & CONSTANTS
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstants() {
        assertFraction(2, 1, BigFraction.TWO);
        assertFraction(1, 1, BigFraction.ONE);
        assertFraction(0, 1, BigFraction.ZERO);
        assertFraction(-1, 1, BigFraction.MINUS_ONE);
        assertFraction(4, 5, BigFraction.FOUR_FIFTHS);
        assertFraction(1, 5, BigFraction.ONE_FIFTH);
        assertFraction(1, 2, BigFraction.ONE_HALF);
        assertFraction(1, 4, BigFraction.ONE_QUARTER);
        assertFraction(1, 3, BigFraction.ONE_THIRD);
        assertFraction(3, 5, BigFraction.THREE_FIFTHS);
        assertFraction(3, 4, BigFraction.THREE_QUARTERS);
        assertFraction(2, 5, BigFraction.TWO_FIFTHS);
        assertFraction(1, 2, BigFraction.TWO_QUARTERS);
        assertFraction(2, 3, BigFraction.TWO_THIRDS);
    }

    @Test(timeout = 4000)
    public void testIntegerAndLongConstructors() {
        BigFraction f1 = new BigFraction(5);
        assertFraction(5, 1, f1);

        BigFraction f2 = new BigFraction(-6, 8);
        assertFraction(-3, 4, f2);

        BigFraction f3 = new BigFraction(6, -8);
        assertFraction(-3, 4, f3);

        BigFraction f4 = new BigFraction(-6, -8);
        assertFraction(3, 4, f4);

        BigFraction f5 = new BigFraction(1234567890123L);
        assertEquals(BigInteger.valueOf(1234567890123L), f5.getNumerator());
        assertEquals(BigInteger.ONE, f5.getDenominator());

        BigFraction f6 = new BigFraction(20000000000L, 50000000000L);
        assertFraction(2, 5, f6);
    }

    @Test(timeout = 4000)
    public void testBigIntegerConstructors() {
        BigFraction f1 = new BigFraction(BigInteger.valueOf(10));
        assertFraction(10, 1, f1);

        BigFraction f2 = new BigFraction(BigInteger.ZERO, BigInteger.valueOf(5));
        assertFraction(0, 1, f2);

        BigFraction f3 = new BigFraction(BigInteger.valueOf(15), BigInteger.valueOf(-25));
        assertFraction(-3, 5, f3);

        BigFraction f4 = new BigFraction(BigInteger.valueOf(-15), BigInteger.valueOf(-25));
        assertFraction(3, 5, f4);
    }

    @Test(timeout = 4000)
    public void testDoubleConstructorExact() {
        BigFraction f1 = new BigFraction(0.0);
        assertFraction(0, 1, f1);

        BigFraction f2 = new BigFraction(0.5);
        assertFraction(1, 2, f2);

        BigFraction f3 = new BigFraction(-0.75);
        assertFraction(-3, 4, f3);

        BigFraction f4 = new BigFraction(4.0);
        assertFraction(4, 1, f4);

        BigFraction f5 = new BigFraction(Double.MIN_VALUE);
        assertEquals(BigInteger.ONE, f5.getNumerator());
        assertEquals(BigInteger.ZERO.flipBit(1074), f5.getDenominator());
    }

    @Test(timeout = 4000)
    public void testDoubleConstructorWithEpsilonAndIterations() {
        BigFraction f1 = new BigFraction(0.3333333333333333, 1e-10, 100);
        assertFraction(1, 3, f1);

        // Almost integer path
        BigFraction f2 = new BigFraction(5.0000000001, 1e-5, 10);
        assertFraction(5, 1, f2);
    }

    @Test(timeout = 4000)
    public void testGetReducedFraction() {
        BigFraction f1 = BigFraction.getReducedFraction(0, 10);
        assertSame(BigFraction.ZERO, f1);

        BigFraction f2 = BigFraction.getReducedFraction(6, -8);
        assertFraction(-3, 4, f2);

        BigFraction f3 = BigFraction.getReducedFraction(-6, -8);
        assertFraction(3, 4, f3);
    }

    // =========================================================================
    // PARTITION B: ARITHMETIC OPERATIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testAbs() {
        BigFraction pos = new BigFraction(3, 4);
        assertSame(pos, pos.abs());

        BigFraction neg = new BigFraction(-3, 4);
        assertFraction(3, 4, neg.abs());

        assertSame(BigFraction.ZERO, BigFraction.ZERO.abs());
    }

    @Test(timeout = 4000)
    public void testAddOperations() {
        BigFraction f = new BigFraction(1, 3);

        assertFraction(4, 3, f.add(1));
        assertFraction(7, 3, f.add(2L));
        assertFraction(10, 3, f.add(BigInteger.valueOf(3)));

        // BigFraction additions
        assertSame(f, f.add(BigFraction.ZERO));

        // Same denominator
        BigFraction fSameDen = f.add(new BigFraction(4, 3));
        assertFraction(5, 3, fSameDen);

        // Different denominator
        BigFraction fDiffDen = f.add(new BigFraction(1, 2));
        assertFraction(5, 6, fDiffDen);
    }

    @Test(timeout = 4000)
    public void testSubtractOperations() {
        BigFraction f = new BigFraction(5, 6);

        assertFraction(-1, 6, f.subtract(1));
        assertFraction(-7, 6, f.subtract(2L));
        assertFraction(-13, 6, f.subtract(BigInteger.valueOf(3)));

        assertSame(f, f.subtract(BigFraction.ZERO));

        // Same denominator
        BigFraction fSameDen = f.subtract(new BigFraction(1, 6));
        assertFraction(2, 3, fSameDen);

        // Different denominator
        BigFraction fDiffDen = f.subtract(new BigFraction(1, 2));
        assertFraction(1, 3, fDiffDen);
    }

    @Test(timeout = 4000)
    public void testMultiplyOperations() {
        BigFraction f = new BigFraction(2, 3);

        assertFraction(4, 3, f.multiply(2));
        assertFraction(2, 1, f.multiply(3L));
        assertFraction(8, 3, f.multiply(BigInteger.valueOf(4)));

        assertFraction(1, 3, f.multiply(new BigFraction(1, 2)));

        assertSame(BigFraction.ZERO, f.multiply(BigFraction.ZERO));
        assertSame(BigFraction.ZERO, BigFraction.ZERO.multiply(f));
    }

    @Test(timeout = 4000)
    public void testDivideOperations() {
        BigFraction f = new BigFraction(3, 4);

        assertFraction(3, 8, f.divide(2));
        assertFraction(1, 4, f.divide(3L));
        assertFraction(3, 20, f.divide(BigInteger.valueOf(5)));
        assertFraction(9, 8, f.divide(new BigFraction(2, 3)));
    }

    @Test(timeout = 4000)
    public void testNegateAndReciprocal() {
        BigFraction f = new BigFraction(3, 5);
        assertFraction(-3, 5, f.negate());
        assertFraction(3, 5, f.negate().negate());

        assertFraction(5, 3, f.reciprocal());
        assertFraction(-5, 3, f.negate().reciprocal());
    }

    @Test(timeout = 4000)
    public void testPow() {
        BigFraction f = new BigFraction(2, 3);

        // int pow
        assertFraction(4, 9, f.pow(2));
        assertFraction(1, 1, f.pow(0));
        assertFraction(9, 4, f.pow(-2));

        // long pow
        assertFraction(8, 27, f.pow(3L));
        assertFraction(1, 1, f.pow(0L));
        assertFraction(27, 8, f.pow(-3L));

        // BigInteger pow
        assertFraction(16, 81, f.pow(BigInteger.valueOf(4)));
        assertFraction(1, 1, f.pow(BigInteger.ZERO));
        assertFraction(81, 16, f.pow(BigInteger.valueOf(-4)));

        // double pow
        assertEquals(0.5, new BigFraction(1, 4).pow(0.5), 1e-10);
    }

    @Test(timeout = 4000)
    public void testReduce() {
        BigFraction f = new BigFraction(4, 6); // Reduced in constructor to 2/3
        BigFraction reduced = f.reduce();
        assertFraction(2, 3, reduced);
    }

    // =========================================================================
    // PARTITION C: VALUE CONVERSIONS & EXTREME BOUNDARIES
    // =========================================================================

    @Test(timeout = 4000)
    public void testGettersAndPrimitives() {
        BigFraction f = new BigFraction(-11, 4);

        assertEquals(BigInteger.valueOf(-11), f.getNumerator());
        assertEquals(-11, f.getNumeratorAsInt());
        assertEquals(-11L, f.getNumeratorAsLong());

        assertEquals(BigInteger.valueOf(4), f.getDenominator());
        assertEquals(4, f.getDenominatorAsInt());
        assertEquals(4L, f.getDenominatorAsLong());

        assertEquals(-2, f.intValue());
        assertEquals(-2L, f.longValue());
        assertEquals(-2.75f, f.floatValue(), 1e-6f);
        assertEquals(-2.75, f.doubleValue(), 1e-10);
        assertEquals(-275.0, f.percentageValue(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testDoubleValueAndFloatValueOutOfRangeExponents() {
        // Shift branch when numerator and/or denominator exceed standard double capacity
        BigInteger hugeNum = BigInteger.valueOf(2).pow(1050);
        BigInteger hugeDen = BigInteger.valueOf(2).pow(1049);
        BigFraction large = new BigFraction(hugeNum, hugeDen);
        assertEquals(2.0, large.doubleValue(), 1e-10);

        // Float shift branch when exponents exceed Float.MAX_VALUE capacity
        BigInteger floatNum = BigInteger.valueOf(2).pow(150);
        BigInteger floatDen = BigInteger.valueOf(2).pow(149);
        BigFraction largeFloat = new BigFraction(floatNum, floatDen);
        assertEquals(2.0f, largeFloat.floatValue(), 1e-6f);
    }

    @Test(timeout = 4000)
    public void testBigDecimalConversions() {
        BigFraction fExact = new BigFraction(1, 2);
        assertEquals(new BigDecimal("0.5"), fExact.bigDecimalValue());

        BigFraction fThird = new BigFraction(1, 3);
        assertEquals(new BigDecimal("0.33"), fThird.bigDecimalValue(2, BigDecimal.ROUND_HALF_UP));
        assertEquals(new BigDecimal("0.33333"), fThird.bigDecimalValue(5, BigDecimal.ROUND_HALF_UP));
        assertEquals(new BigDecimal("0"), fThird.bigDecimalValue(BigDecimal.ROUND_DOWN));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testBigDecimalValueNonTerminatingThrows() {
        new BigFraction(1, 3).bigDecimalValue();
    }

    // =========================================================================
    // PARTITION D: CONTRACT INTEGRITY (equals, compareTo, toString, Field)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        BigFraction f1 = new BigFraction(2, 4);
        BigFraction f2 = new BigFraction(1, 2);
        BigFraction f3 = new BigFraction(2, 3);

        assertTrue(f1.equals(f1));
        assertTrue(f1.equals(f2));
        assertFalse(f1.equals(f3));
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("NotAFraction"));

        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test(timeout = 4000)
    public void testCompareTo() {
        BigFraction f1 = new BigFraction(1, 3);
        BigFraction f2 = new BigFraction(2, 6);
        BigFraction f3 = new BigFraction(1, 2);

        assertEquals(0, f1.compareTo(f2));
        assertTrue(f1.compareTo(f3) < 0);
        assertTrue(f3.compareTo(f1) > 0);
    }

    @Test(timeout = 4000)
    public void testToString() {
        assertEquals("0", new BigFraction(0, 5).toString());
        assertEquals("4", new BigFraction(4, 1).toString());
        assertEquals("-3 / 5", new BigFraction(-3, 5).toString());
    }

    @Test(timeout = 4000)
    public void testGetField() {
        BigFraction f = new BigFraction(1, 2);
        assertNotNull(f.getField());
        assertSame(BigFractionField.getInstance(), f.getField());
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        BigFraction original = new BigFraction(-1234567, 891011);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        BigFraction deserialized = (BigFraction) ois.readObject();
        ois.close();

        assertEquals(original, deserialized);
        assertEquals(original.getNumerator(), deserialized.getNumerator());
        assertEquals(original.getDenominator(), deserialized.getDenominator());
    }

    // =========================================================================
    // PARTITION E: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(expected = ZeroException.class, timeout = 4000)
    public void testConstructorZeroDenominatorInt() {
        new BigFraction(1, 0);
    }

    @Test(expected = ZeroException.class, timeout = 4000)
    public void testConstructorZeroDenominatorLong() {
        new BigFraction(1L, 0L);
    }

    @Test(expected = ZeroException.class, timeout = 4000)
    public void testConstructorZeroDenominatorBigInteger() {
        new BigFraction(BigInteger.ONE, BigInteger.ZERO);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testConstructorNullNumerator() {
        new BigFraction(null, BigInteger.ONE);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testConstructorNullDenominator() {
        new BigFraction(BigInteger.ONE, null);
    }

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testDoubleConstructorNaN() {
        new BigFraction(Double.NaN);
    }

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testDoubleConstructorPositiveInfinity() {
        new BigFraction(Double.POSITIVE_INFINITY);
    }

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testDoubleConstructorNegativeInfinity() {
        new BigFraction(Double.NEGATIVE_INFINITY);
    }

    @Test(expected = FractionConversionException.class, timeout = 4000)
    public void testDoubleConstructorOverflowA0() {
        new BigFraction(Double.MAX_VALUE, 1.0, 10);
    }

    @Test(expected = FractionConversionException.class, timeout = 4000)
    public void testDoubleConstructorMaxIterationsExhausted() {
        new BigFraction(0.6152, 1e-15, 1);
    }

    @Test(expected = ZeroException.class, timeout = 4000)
    public void testGetReducedFractionZeroDenominator() {
        BigFraction.getReducedFraction(5, 0);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testAddNullBigInteger() {
        BigFraction.ONE.add((BigInteger) null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testAddNullBigFraction() {
        BigFraction.ONE.add((BigFraction) null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testSubtractNullBigInteger() {
        BigFraction.ONE.subtract((BigInteger) null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testSubtractNullBigFraction() {
        BigFraction.ONE.subtract((BigFraction) null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testMultiplyNullBigInteger() {
        BigFraction.ONE.multiply((BigInteger) null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testMultiplyNullBigFraction() {
        BigFraction.ONE.multiply((BigFraction) null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testDivideNullBigInteger() {
        BigFraction.ONE.divide((BigInteger) null);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testDivideZeroBigInteger() {
        BigFraction.ONE.divide(BigInteger.ZERO);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testDivideZeroInt() {
        BigFraction.ONE.divide(0);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testDivideZeroLong() {
        BigFraction.ONE.divide(0L);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testDivideNullBigFraction() {
        BigFraction.ONE.divide((BigFraction) null);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testDivideZeroBigFraction() {
        BigFraction.ONE.divide(BigFraction.ZERO);
    }
}