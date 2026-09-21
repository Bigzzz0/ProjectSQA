package org.apache.commons.math3.fraction;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: org.apache.commons.math3.fraction.Fraction
 * 
 * Decision branches covered:
 * - Fraction(int, int): den==0, den<0 with MIN_VALUE, gcd reduction, sign normalization
 * - Fraction(double, ...): integer check, overflow of a0, iteration loop, convergence, maxIterations, maxDenominator
 * - addSub: null check, numerator==0, fraction.numerator==0, d1==1 vs d1!=1, BigInteger overflow check
 * - multiply: null check, zero numerator, gcd reduction, mulAndCheck overflow
 * - divide: null check, zero numerator, reciprocal
 * - add(int), subtract(int), multiply(int), divide(int): overflow in arithmetic (defect area)
 * - abs: numerator>=0 vs negate
 * - negate: numerator==MIN_VALUE overflow
 * - reciprocal: simple constructor
 * - compareTo: cross-multiplication overflow (long)
 * - equals: identity, instanceof, field comparison
 * - hashCode: formula
 * - getReducedFraction: den==0, numerator==0, denominator MIN_VALUE with even numerator, sign flip, gcd
 * - toString: denominator==1, numerator==0, else
 * 
 * Known defect: testIntegerOverflow expects MathArithmeticException when overflow occurs
 * in add(int), subtract(int), multiply(int), or divide(int) but none is thrown.
 * We target these methods with boundary values that cause silent overflow.
 */
public class FractionDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testConstructorIntIntNormal() {
        Fraction f = new Fraction(3, 4);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
        assertEquals(0.75, f.doubleValue(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConstructorIntIntReduction() {
        Fraction f = new Fraction(6, 8);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorIntIntNegativeDenominator() {
        Fraction f = new Fraction(5, -3);
        assertEquals(-5, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorIntIntBothNegative() {
        Fraction f = new Fraction(-4, -6);
        assertEquals(2, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorIntIntZeroNumerator() {
        Fraction f = new Fraction(0, 5);
        assertEquals(0, f.getNumerator());
        assertEquals(1, f.getDenominator()); // reduced to 0/1
    }

    @Test(timeout = 4000)
    public void testConstructorIntIntMinValueDenominator() {
        // den = Integer.MIN_VALUE, num positive -> should throw
        try {
            new Fraction(1, Integer.MIN_VALUE);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorIntIntMinValueNumerator() {
        try {
            new Fraction(Integer.MIN_VALUE, -1);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorInt() {
        Fraction f = new Fraction(7);
        assertEquals(7, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testDoubleValue() {
        Fraction f = new Fraction(1, 3);
        assertEquals(1.0/3.0, f.doubleValue(), 1e-15);
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
    public void testNegate() {
        Fraction f = new Fraction(3, 4);
        Fraction neg = f.negate();
        assertEquals(-3, neg.getNumerator());
        assertEquals(4, neg.getDenominator());
    }

    @Test(timeout = 4000)
    public void testNegateOverflow() {
        try {
            new Fraction(Integer.MIN_VALUE, 1).negate();
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReciprocal() {
        Fraction f = new Fraction(3, 4);
        Fraction rec = f.reciprocal();
        assertEquals(4, rec.getNumerator());
        assertEquals(3, rec.getDenominator());
    }

    @Test(timeout = 4000)
    public void testCompareToEqual() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 4);
        assertEquals(0, a.compareTo(b));
    }

    @Test(timeout = 4000)
    public void testCompareToLess() {
        Fraction a = new Fraction(1, 3);
        Fraction b = new Fraction(1, 2);
        assertTrue(a.compareTo(b) < 0);
    }

    @Test(timeout = 4000)
    public void testCompareToGreater() {
        Fraction a = new Fraction(2, 3);
        Fraction b = new Fraction(1, 2);
        assertTrue(a.compareTo(b) > 0);
    }

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        Fraction f = new Fraction(1, 2);
        assertTrue(f.equals(f));
    }

    @Test(timeout = 4000)
    public void testEqualsEqualFractions() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 4);
        assertTrue(a.equals(b));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferent() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(3, 4);
        assertFalse(a.equals(b));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        Fraction f = new Fraction(1, 2);
        assertFalse(f.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsNonFraction() {
        Fraction f = new Fraction(1, 2);
        assertFalse(f.equals("string"));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 4);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test(timeout = 4000)
    public void testToStringDenominatorOne() {
        Fraction f = new Fraction(5);
        assertEquals("5", f.toString());
    }

    @Test(timeout = 4000)
    public void testToStringNumeratorZero() {
        Fraction f = new Fraction(0, 3);
        assertEquals("0", f.toString());
    }

    @Test(timeout = 4000)
    public void testToStringNormal() {
        Fraction f = new Fraction(3, 4);
        assertEquals("3 / 4", f.toString());
    }

    @Test(timeout = 4000)
    public void testGetField() {
        assertNotNull(new Fraction(1, 2).getField());
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testConstructorDoubleExactInteger() {
        Fraction f = new Fraction(3.0);
        assertEquals(3, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleApproximate() {
        Fraction f = new Fraction(0.3333333333, 1e-5, 100);
        assertEquals(1, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleMaxDenominator() {
        Fraction f = new Fraction(Math.PI, 1000);
        assertTrue(f.getDenominator() <= 1000);
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleOverflowA0() {
        // value large enough that a0 > Integer.MAX_VALUE
        try {
            new Fraction(1e20, 1e-5, 100);
            fail("Expected FractionConversionException");
        } catch (FractionConversionException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleMaxIterationsExceeded() {
        try {
            new Fraction(0.123456789, 1e-20, 5); // small epsilon, few iterations
            fail("Expected FractionConversionException");
        } catch (FractionConversionException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleP2Q2Overflow() {
        // This is tricky; we need a value that causes p2 or q2 to exceed overflow.
        // Use a large value with small epsilon to force many iterations.
        try {
            new Fraction(1e10, 1e-10, 100);
        } catch (FractionConversionException e) {
            // acceptable
        }
    }

    @Test(timeout = 4000)
    public void testAddSubD1Equals1() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(1, 3);
        Fraction sum = a.add(b);
        assertEquals(5, sum.getNumerator());
        assertEquals(6, sum.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddSubD1Not1() {
        Fraction a = new Fraction(1, 6);
        Fraction b = new Fraction(1, 4);
        Fraction sum = a.add(b);
        assertEquals(5, sum.getNumerator());
        assertEquals(12, sum.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddSubWithZeroNumerator() {
        Fraction zero = new Fraction(0, 1);
        Fraction f = new Fraction(3, 4);
        assertSame(f, zero.add(f));
        assertSame(f.negate(), zero.subtract(f));
    }

    @Test(timeout = 4000)
    public void testAddSubWithFractionZeroNumerator() {
        Fraction f = new Fraction(3, 4);
        Fraction zero = new Fraction(0, 1);
        assertSame(f, f.add(zero));
        assertSame(f, f.subtract(zero));
    }

    @Test(timeout = 4000)
    public void testAddSubNull() {
        try {
            new Fraction(1, 2).add(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddSubOverflowInMulAndCheck() {
        // Use large values to trigger overflow in mulAndCheck
        Fraction a = new Fraction(Integer.MAX_VALUE, 1);
        Fraction b = new Fraction(2, 1);
        try {
            a.add(b);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddSubBigIntegerOverflow() {
        // Use fractions that cause BigInteger result > 31 bits
        Fraction a = new Fraction(46340, 1); // sqrt of Integer.MAX_VALUE approx
        Fraction b = new Fraction(46341, 1);
        try {
            a.multiply(b); // this will overflow in multiply, but we need addSub with d1!=1
            // Actually, to trigger BigInteger overflow in addSub, we need large numerators and denominators
            // Example: a = 1/1000000, b = 1/1000000, but d1=1? No.
            // Better: use denominators that are not coprime and large numerators.
            // Let's use a = 1000000/1, b = 1000000/1, but d1=1 -> uses mulAndCheck, not BigInteger.
            // To force d1!=1, use denominators with common factor.
            // a = 1000000/2, b = 1000000/4 -> d1=2, then uvp and upv become large.
            Fraction a2 = new Fraction(1000000, 2);
            Fraction b2 = new Fraction(1000000, 4);
            try {
                a2.add(b2);
                fail("Expected MathArithmeticException");
            } catch (MathArithmeticException e) {
                // expected
            }
        } catch (Exception e) {
            // other exceptions possible
        }
    }

    @Test(timeout = 4000)
    public void testMultiplyNormal() {
        Fraction a = new Fraction(2, 3);
        Fraction b = new Fraction(3, 4);
        Fraction prod = a.multiply(b);
        assertEquals(1, prod.getNumerator());
        assertEquals(2, prod.getDenominator());
    }

    @Test(timeout = 4000)
    public void testMultiplyByZero() {
        Fraction a = new Fraction(5, 7);
        Fraction zero = Fraction.ZERO;
        assertSame(Fraction.ZERO, a.multiply(zero));
    }

    @Test(timeout = 4000)
    public void testMultiplyNull() {
        try {
            new Fraction(1, 2).multiply(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMultiplyOverflow() {
        Fraction a = new Fraction(46340, 1);
        Fraction b = new Fraction(46341, 1);
        try {
            a.multiply(b);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDivideNormal() {
        Fraction a = new Fraction(3, 4);
        Fraction b = new Fraction(1, 2);
        Fraction quot = a.divide(b);
        assertEquals(3, quot.getNumerator());
        assertEquals(2, quot.getDenominator());
    }

    @Test(timeout = 4000)
    public void testDivideByZero() {
        try {
            new Fraction(1, 2).divide(Fraction.ZERO);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDivideNull() {
        try {
            new Fraction(1, 2).divide(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPercentageValue() {
        Fraction f = new Fraction(1, 4);
        assertEquals(25.0, f.percentageValue(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionNormal() {
        Fraction f = Fraction.getReducedFraction(6, 8);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionZeroDenominator() {
        try {
            Fraction.getReducedFraction(1, 0);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionZeroNumerator() {
        assertSame(Fraction.ZERO, Fraction.getReducedFraction(0, 5));
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionMinValueDenominatorEvenNumerator() {
        // denominator = Integer.MIN_VALUE, numerator even -> should reduce
        Fraction f = Fraction.getReducedFraction(2, Integer.MIN_VALUE);
        // After reduction: numerator=1, denominator=1073741824 (MIN_VALUE/2)
        assertEquals(1, f.getNumerator());
        assertEquals(Integer.MIN_VALUE / 2, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionMinValueDenominatorOddNumerator() {
        try {
            Fraction.getReducedFraction(1, Integer.MIN_VALUE);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionNegativeDenominator() {
        Fraction f = Fraction.getReducedFraction(3, -6);
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========
    // Known defect: overflow in add(int), subtract(int), multiply(int), divide(int)
    // These methods do not check for overflow and can produce incorrect results silently.

    @Test(timeout = 4000)
    public void testAddIntegerOverflow() {
        // numerator + i * denominator overflows
        Fraction f = new Fraction(Integer.MAX_VALUE, 1);
        try {
            f.add(1);
            fail("Expected MathArithmeticException for overflow in add(int)");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSubtractIntegerOverflow() {
        Fraction f = new Fraction(Integer.MIN_VALUE, 1);
        try {
            f.subtract(1);
            fail("Expected MathArithmeticException for overflow in subtract(int)");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMultiplyIntegerOverflow() {
        Fraction f = new Fraction(Integer.MAX_VALUE, 1);
        try {
            f.multiply(2);
            fail("Expected MathArithmeticException for overflow in multiply(int)");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDivideIntegerOverflow() {
        // denominator * i overflows
        Fraction f = new Fraction(1, Integer.MAX_VALUE);
        try {
            f.divide(2);
            fail("Expected MathArithmeticException for overflow in divide(int)");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    // Additional edge: add(int) with negative i that could cause underflow
    @Test(timeout = 4000)
    public void testAddIntegerUnderflow() {
        Fraction f = new Fraction(Integer.MIN_VALUE, 1);
        try {
            f.add(-1);
            fail("Expected MathArithmeticException for underflow in add(int)");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testConstructorDenominatorZero() {
        try {
            new Fraction(1, 0);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddSubFractionNull() {
        try {
            new Fraction(1, 2).add(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMultiplyFractionNull() {
        try {
            new Fraction(1, 2).multiply(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDivideFractionNull() {
        try {
            new Fraction(1, 2).divide(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 4);
        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());
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
        Fraction f = new Fraction(3, 7);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(f);
        oos.close();
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        Fraction deserialized = (Fraction) ois.readObject();
        assertEquals(f.getNumerator(), deserialized.getNumerator());
        assertEquals(f.getDenominator(), deserialized.getDenominator());
    }

    // Additional coverage for addSub with d1!=1 and BigInteger path
    @Test(timeout = 4000)
    public void testAddSubBigIntegerPath() {
        // Denominators with common factor, large numerators to force BigInteger usage
        Fraction a = new Fraction(100000, 6);
        Fraction b = new Fraction(200000, 4);
        Fraction sum = a.add(b);
        // Expected: (100000/6) + (200000/4) = (100000*4 + 200000*6)/(24) = (400000+1200000)/24 = 1600000/24 = 200000/3
        assertEquals(200000, sum.getNumerator());
        assertEquals(3, sum.getDenominator());
    }

    @Test(timeout = 4000)
    public void testSubtractBigIntegerPath() {
        Fraction a = new Fraction(100000, 6);
        Fraction b = new Fraction(200000, 4);
        Fraction diff = a.subtract(b);
        // (100000/6) - (200000/4) = (400000 - 1200000)/24 = -800000/24 = -100000/3
        assertEquals(-100000, diff.getNumerator());
        assertEquals(3, diff.getDenominator());
    }

    // Edge: multiply with gcd reduction that prevents overflow
    @Test(timeout = 4000)
    public void testMultiplyWithGcdReduction() {
        Fraction a = new Fraction(100, 200);
        Fraction b = new Fraction(300, 400);
        Fraction prod = a.multiply(b);
        assertEquals(3, prod.getNumerator());
        assertEquals(8, prod.getDenominator());
    }

    // Edge: divide with reciprocal
    @Test(timeout = 4000)
    public void testDivideByFraction() {
        Fraction a = new Fraction(3, 4);
        Fraction b = new Fraction(2, 5);
        Fraction quot = a.divide(b);
        assertEquals(15, quot.getNumerator());
        assertEquals(8, quot.getDenominator());
    }

    // Edge: add(int) with zero denominator? Not possible.
    // Edge: subtract(int) with negative i
    @Test(timeout = 4000)
    public void testSubtractIntegerNegative() {
        Fraction f = new Fraction(5, 1);
        Fraction result = f.subtract(-3);
        assertEquals(8, result.getNumerator());
        assertEquals(1, result.getDenominator());
    }

    // Edge: multiply(int) with zero
    @Test(timeout = 4000)
    public void testMultiplyIntegerZero() {
        Fraction f = new Fraction(5, 7);
        Fraction result = f.multiply(0);
        assertEquals(0, result.getNumerator());
        assertEquals(1, result.getDenominator());
    }

    // Edge: divide(int) with zero
    @Test(timeout = 4000)
    public void testDivideIntegerZero() {
        try {
            new Fraction(1, 2).divide(0);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    // Edge: add(int) with large denominator
    @Test(timeout = 4000)
    public void testAddIntegerLargeDenominator() {
        Fraction f = new Fraction(1, Integer.MAX_VALUE);
        // i * denominator may overflow
        try {
            f.add(2);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    // Edge: subtract(int) with large denominator
    @Test(timeout = 4000)
    public void testSubtractIntegerLargeDenominator() {
        Fraction f = new Fraction(1, Integer.MAX_VALUE);
        try {
            f.subtract(2);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    // Edge: multiply(int) with large numerator
    @Test(timeout = 4000)
    public void testMultiplyIntegerLargeNumerator() {
        Fraction f = new Fraction(Integer.MAX_VALUE, 1);
        try {
            f.multiply(2);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    // Edge: divide(int) with large denominator
    @Test(timeout = 4000)
    public void testDivideIntegerLargeDenominator() {
        Fraction f = new Fraction(1, Integer.MAX_VALUE);
        try {
            f.divide(2);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    // Additional coverage for double constructor with maxDenominator
    @Test(timeout = 4000)
    public void testConstructorDoubleMaxDenominatorExact() {
        Fraction f = new Fraction(0.5, 10);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleMaxDenominatorApprox() {
        Fraction f = new Fraction(0.3333, 100);
        assertTrue(f.getDenominator() <= 100);
    }

    // Edge: getReducedFraction with negative numerator and denominator
    @Test(timeout = 4000)
    public void testGetReducedFractionNegativeBoth() {
        Fraction f = Fraction.getReducedFraction(-4, -6);
        assertEquals(2, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    // Edge: getReducedFraction with numerator MIN_VALUE and denominator -1
    @Test(timeout = 4000)
    public void testGetReducedFractionMinValueNumeratorNegativeDenominator() {
        try {
            Fraction.getReducedFraction(Integer.MIN_VALUE, -1);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    // Edge: toString with negative numerator
    @Test(timeout = 4000)
    public void testToStringNegative() {
        Fraction f = new Fraction(-3, 4);
        assertEquals("-3 / 4", f.toString());
    }

    // Edge: compareTo with overflow in cross-multiplication (should use long)
    @Test(timeout = 4000)
    public void testCompareToLargeValues() {
        Fraction a = new Fraction(Integer.MAX_VALUE, 1);
        Fraction b = new Fraction(Integer.MAX_VALUE - 1, 1);
        assertTrue(a.compareTo(b) > 0);
    }

    // Edge: equals with different types
    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        Fraction f = new Fraction(1, 2);
        assertFalse(f.equals(new Object()));
    }

    // Edge: hashCode consistency for reduced fractions
    @Test(timeout = 4000)
    public void testHashCodeReduced() {
        Fraction a = new Fraction(2, 4);
        Fraction b = new Fraction(1, 2);
        assertEquals(a.hashCode(), b.hashCode());
    }
}