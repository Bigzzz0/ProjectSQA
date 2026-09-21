package org.apache.commons.math3.fraction;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive white-box test suite for {@link Fraction}.
 * Targets line/branch coverage and the known defect in the digit-limit constructor.
 *
 * [Branch & Defect Analysis Matrix]
 * - Fraction(int, int): den==0, den<0, MIN_VALUE overflow, gcd reduction, sign move.
 * - Fraction(double, double, int, int): floor overflow, epsilon exact match, loop overflow, convergence, maxIterations, q2<maxDenominator else p1/q1.
 * - addSub: null, zero numerator, d1==1, else BigInteger path.
 * - multiply: null, zero numerator, gcd reduction.
 * - divide: null, zero divisor, multiply reciprocal.
 * - getReducedFraction: den==0, numerator==0, den==MIN_VALUE & even numerator, den<0 overflow, gcd.
 * - negate: numerator==MIN_VALUE.
 * - compareTo: cross multiplication.
 * - equals: identity, instanceof, field equality.
 * - hashCode: formula.
 * - toString: den==1, num==0, else.
 * - Defect: Fraction(0.5, Integer.MAX_VALUE) should return 1/2 but throws FractionConversionException.
 */
public class FractionDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testConstructorIntInt() {
        Fraction f = new Fraction(3, 4);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorIntIntReduction() {
        Fraction f = new Fraction(6, 8);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorIntIntNegativeDenominator() {
        Fraction f = new Fraction(3, -4);
        assertEquals(-3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorIntIntNegativeBoth() {
        Fraction f = new Fraction(-3, -4);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorInt() {
        Fraction f = new Fraction(5);
        assertEquals(5, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorDouble() throws FractionConversionException {
        Fraction f = new Fraction(0.5);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleEpsilonMaxIter() throws FractionConversionException {
        Fraction f = new Fraction(0.33333333, 1e-6, 100);
        assertEquals(1, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleMaxDenominator() throws FractionConversionException {
        Fraction f = new Fraction(0.5, 100);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetters() {
        Fraction f = new Fraction(7, 11);
        assertEquals(7, f.getNumerator());
        assertEquals(11, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddFraction() {
        Fraction a = new Fraction(1, 3);
        Fraction b = new Fraction(1, 6);
        Fraction result = a.add(b);
        assertEquals(1, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddInt() {
        Fraction a = new Fraction(1, 4);
        Fraction result = a.add(2);
        assertEquals(9, result.getNumerator());
        assertEquals(4, result.getDenominator());
    }

    @Test(timeout = 4000)
    public void testSubtractFraction() {
        Fraction a = new Fraction(3, 4);
        Fraction b = new Fraction(1, 4);
        Fraction result = a.subtract(b);
        assertEquals(1, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test(timeout = 4000)
    public void testSubtractInt() {
        Fraction a = new Fraction(5, 2);
        Fraction result = a.subtract(1);
        assertEquals(3, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test(timeout = 4000)
    public void testMultiplyFraction() {
        Fraction a = new Fraction(2, 3);
        Fraction b = new Fraction(3, 4);
        Fraction result = a.multiply(b);
        assertEquals(1, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test(timeout = 4000)
    public void testMultiplyInt() {
        Fraction a = new Fraction(1, 5);
        Fraction result = a.multiply(10);
        assertEquals(2, result.getNumerator());
        assertEquals(1, result.getDenominator());
    }

    @Test(timeout = 4000)
    public void testDivideFraction() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(1, 4);
        Fraction result = a.divide(b);
        assertEquals(2, result.getNumerator());
        assertEquals(1, result.getDenominator());
    }

    @Test(timeout = 4000)
    public void testDivideInt() {
        Fraction a = new Fraction(3, 4);
        Fraction result = a.divide(3);
        assertEquals(1, result.getNumerator());
        assertEquals(4, result.getDenominator());
    }

    @Test(timeout = 4000)
    public void testNegate() {
        Fraction f = new Fraction(2, 3);
        Fraction neg = f.negate();
        assertEquals(-2, neg.getNumerator());
        assertEquals(3, neg.getDenominator());
    }

    @Test(timeout = 4000)
    public void testReciprocal() {
        Fraction f = new Fraction(3, 4);
        Fraction rec = f.reciprocal();
        assertEquals(4, rec.getNumerator());
        assertEquals(3, rec.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAbsPositive() {
        Fraction f = new Fraction(3, 4);
        assertSame(f, f.abs());
    }

    @Test(timeout = 4000)
    public void testAbsNegative() {
        Fraction f = new Fraction(-3, 4);
        Fraction abs = f.abs();
        assertEquals(3, abs.getNumerator());
        assertEquals(4, abs.getDenominator());
    }

    @Test(timeout = 4000)
    public void testCompareTo() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 3);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertEquals(0, a.compareTo(new Fraction(1, 2)));
    }

    @Test(timeout = 4000)
    public void testEquals() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(1, 2);
        Fraction c = new Fraction(2, 4);
        assertTrue(a.equals(b));
        assertTrue(a.equals(c)); // reduced form
        assertFalse(a.equals(null));
        assertFalse(a.equals("string"));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(1, 2);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test(timeout = 4000)
    public void testDoubleValue() {
        Fraction f = new Fraction(1, 4);
        assertEquals(0.25, f.doubleValue(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testFloatValue() {
        Fraction f = new Fraction(1, 3);
        assertEquals(1.0f/3.0f, f.floatValue(), 1e-7);
    }

    @Test(timeout = 4000)
    public void testIntValue() {
        Fraction f = new Fraction(7, 3);
        assertEquals(2, f.intValue());
    }

    @Test(timeout = 4000)
    public void testLongValue() {
        Fraction f = new Fraction(7, 3);
        assertEquals(2L, f.longValue());
    }

    @Test(timeout = 4000)
    public void testPercentageValue() {
        Fraction f = new Fraction(1, 4);
        assertEquals(25.0, f.percentageValue(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testToStringDenominatorOne() {
        Fraction f = new Fraction(5);
        assertEquals("5", f.toString());
    }

    @Test(timeout = 4000)
    public void testToStringNumeratorZero() {
        Fraction f = new Fraction(0, 1);
        assertEquals("0", f.toString());
    }

    @Test(timeout = 4000)
    public void testToStringNormal() {
        Fraction f = new Fraction(3, 7);
        assertEquals("3 / 7", f.toString());
    }

    @Test(timeout = 4000)
    public void testGetReducedFraction() {
        Fraction f = Fraction.getReducedFraction(6, 8);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionZeroNumerator() {
        assertSame(Fraction.ZERO, Fraction.getReducedFraction(0, 5));
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testConstructorIntIntMinValueDenominator() {
        // denominator = Integer.MIN_VALUE, numerator even -> should reduce
        Fraction f = new Fraction(2, Integer.MIN_VALUE);
        // After reduction: numerator = -1, denominator = 1073741824? Actually gcd(2, MIN_VALUE) = 2? MIN_VALUE is -2^31, gcd(2, -2^31) = 2? But sign handling: den<0, so num = -2, den = -MIN_VALUE? Wait careful.
        // The constructor: if den<0, negate both. den = Integer.MIN_VALUE, num=2 -> num = -2, den = -Integer.MIN_VALUE which overflows? Actually -Integer.MIN_VALUE = Integer.MIN_VALUE (since overflow). So it throws MathArithmeticException.
        // So we expect exception.
        try {
            new Fraction(2, Integer.MIN_VALUE);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorIntIntMinValueNumerator() {
        // numerator = Integer.MIN_VALUE, denominator negative -> overflow in negation
        try {
            new Fraction(Integer.MIN_VALUE, -1);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNegateMinValue() {
        Fraction f = new Fraction(Integer.MIN_VALUE, 1);
        try {
            f.negate();
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddSubOverflow() {
        Fraction a = new Fraction(Integer.MAX_VALUE, 1);
        Fraction b = new Fraction(1, 1);
        try {
            a.add(b);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMultiplyOverflow() {
        Fraction a = new Fraction(Integer.MAX_VALUE, 1);
        Fraction b = new Fraction(2, 1);
        try {
            a.multiply(b);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDivideByZero() {
        Fraction a = new Fraction(1, 2);
        Fraction zero = Fraction.ZERO;
        try {
            a.divide(zero);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionDenominatorMinValueEvenNumerator() {
        // denominator = Integer.MIN_VALUE, numerator even -> should reduce
        Fraction f = Fraction.getReducedFraction(2, Integer.MIN_VALUE);
        // After reduction: numerator = -1, denominator = 1073741824? Actually gcd(2, MIN_VALUE) = 2, then numerator=1, denominator= -1073741824? Wait sign handling: den<0, so numerator = -1, denominator = 1073741824.
        assertEquals(-1, f.getNumerator());
        assertEquals(1073741824, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionDenominatorMinValueOddNumerator() {
        // denominator = Integer.MIN_VALUE, numerator odd -> overflow in negation
        try {
            Fraction.getReducedFraction(1, Integer.MIN_VALUE);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionDenominatorZero() {
        try {
            Fraction.getReducedFraction(1, 0);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testDigitLimitConstructorDefect() throws FractionConversionException {
        // This test targets the known defect: Fraction(0.5, Integer.MAX_VALUE) should return 1/2
        // but in the defective version it throws FractionConversionException due to overflow.
        Fraction f = new Fraction(0.5, Integer.MAX_VALUE);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testAddNull() {
        Fraction f = new Fraction(1, 2);
        f.add((Fraction) null);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testSubtractNull() {
        Fraction f = new Fraction(1, 2);
        f.subtract((Fraction) null);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testMultiplyNull() {
        Fraction f = new Fraction(1, 2);
        f.multiply((Fraction) null);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testDivideNull() {
        Fraction f = new Fraction(1, 2);
        f.divide((Fraction) null);
    }

    @Test(timeout = 4000, expected = MathArithmeticException.class)
    public void testConstructorDenominatorZero() {
        new Fraction(1, 0);
    }

    @Test(timeout = 4000, expected = FractionConversionException.class)
    public void testConstructorDoubleNoConvergence() throws FractionConversionException {
        // Use very small epsilon and few iterations to force non-convergence
        new Fraction(Math.PI, 1e-20, 1);
    }

    @Test(timeout = 4000, expected = FractionConversionException.class)
    public void testConstructorDoubleOverflow() throws FractionConversionException {
        // Large value that causes floor overflow
        new Fraction(1e20, 1e-5, 100);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testEqualsConsistentWithHashCode() {
        Fraction f1 = new Fraction(3, 7);
        Fraction f2 = new Fraction(3, 7);
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentTypes() {
        Fraction f = new Fraction(1, 2);
        assertFalse(f.equals(new Object()));
    }

    @Test(timeout = 4000)
    public void testEqualsReflexive() {
        Fraction f = new Fraction(1, 2);
        assertTrue(f.equals(f));
    }

    @Test(timeout = 4000)
    public void testCompareToConsistentWithEquals() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 4);
        assertEquals(0, a.compareTo(b));
        assertTrue(a.equals(b));
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        Fraction f = new Fraction(3, 5);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(f);
        oos.flush();
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        Fraction deserialized = (Fraction) ois.readObject();
        assertEquals(f, deserialized);
        assertEquals(f.getNumerator(), deserialized.getNumerator());
        assertEquals(f.getDenominator(), deserialized.getDenominator());
    }
}