package org.apache.commons.math.fraction;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * 
 * Partitions and boundary conditions targeted:
 * 
 * A. Core Functionality:
 *    - Fraction(int,int) constructor: sign normalization, reduction, zero denominator.
 *    - Fraction(double) and private double constructor: continued fraction convergence,
 *      epsilon/maxDenominator/maxIterations edge cases.
 *    - abs(), negate(), reciprocal().
 *    - add(), subtract(), multiply(), divide() with null and normal operands.
 *    - getReducedFraction() static.
 *    - doubleValue(), floatValue(), intValue(), longValue().
 * 
 * B. Boundary Value Analysis:
 *    - Numerator/denominator = Integer.MIN_VALUE, Integer.MAX_VALUE, 0, ±1.
 *    - double constructor: value close to integer, very large, very small, etc.
 *    - Overflow detection in arithmetic (mulAndCheck, addAndCheck, subAndCheck).
 * 
 * C. Defect-Targeted (MATH bug):
 *    - compareTo() using double leads to equality for distinct fractions
 *      when double values round to same.  We use (1999999999/2000000000) vs
 *      (2000000000/2000000001) which are mathematically f1<f2 but doubles equal.
 * 
 * D. Exception/Defensive Guards:
 *    - Denominator zero -> ArithmeticException.
 *    - Numerator/denominator Integer.MIN_VALUE in sign negation -> ArithmeticException.
 *    - Null fraction in add/sub/multiply/divide -> IllegalArgumentException.
 *    - Division by zero fraction -> ArithmeticException.
 *    - Overflow in arithmetic -> ArithmeticException.
 *    - Continued fraction convergence failure -> FractionConversionException.
 * 
 * E. Object Lifecycle:
 *    - equals() with null, different type, same/different fractions.
 *    - hashCode() consistency with equals.
 *    - compareTo() consistency with equals and natural order.
 */
public class FractionDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testConstructorBasic() {
        Fraction f = new Fraction(3, 5);
        assertEquals(3, f.getNumerator());
        assertEquals(5, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorReduces() {
        Fraction f = new Fraction(4, 6);
        assertEquals(2, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorNegativeSignNormalization() {
        Fraction f = new Fraction(4, -6);
        assertEquals(-2, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleSimple() throws FractionConversionException {
        Fraction f = new Fraction(0.75);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleWithEpsilon() throws FractionConversionException {
        Fraction f = new Fraction(0.333333333, 1e-6, 100);
        // approximate 1/3 within epsilon
        double ratio = (double) f.getNumerator() / f.getDenominator();
        assertTrue(Math.abs(ratio - 0.333333333) < 1e-6);
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleWithMaxDenominator() throws FractionConversionException {
        Fraction f = new Fraction(0.333333333, 1000);
        assertTrue(f.getDenominator() <= 1000);
    }

    @Test(timeout = 4000)
    public void testAbsPositive() {
        Fraction f = new Fraction(3, 7);
        assertSame(f, f.abs());
    }

    @Test(timeout = 4000)
    public void testAbsNegative() {
        Fraction f = new Fraction(-3, 7);
        Fraction abs = f.abs();
        assertEquals(3, abs.getNumerator());
        assertEquals(7, abs.getDenominator());
    }

    @Test(timeout = 4000)
    public void testNegate() {
        Fraction f = new Fraction(3, 7);
        Fraction neg = f.negate();
        assertEquals(-3, neg.getNumerator());
        assertEquals(7, neg.getDenominator());
    }

    @Test(timeout = 4000)
    public void testReciprocal() {
        Fraction f = new Fraction(5, 9);
        Fraction recip = f.reciprocal();
        assertEquals(9, recip.getNumerator());
        assertEquals(5, recip.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddSimple() {
        Fraction a = new Fraction(1, 4);
        Fraction b = new Fraction(1, 6);
        Fraction sum = a.add(b);
        assertEquals(5, sum.getNumerator());
        assertEquals(12, sum.getDenominator());
    }

    @Test(timeout = 4000)
    public void testSubtractSimple() {
        Fraction a = new Fraction(3, 4);
        Fraction b = new Fraction(1, 8);
        Fraction diff = a.subtract(b);
        assertEquals(5, diff.getNumerator());
        assertEquals(8, diff.getDenominator());
    }

    @Test(timeout = 4000)
    public void testMultiplySimple() {
        Fraction a = new Fraction(2, 3);
        Fraction b = new Fraction(3, 5);
        Fraction prod = a.multiply(b);
        assertEquals(2, prod.getNumerator());
        assertEquals(5, prod.getDenominator());
    }

    @Test(timeout = 4000)
    public void testDivideSimple() {
        Fraction a = new Fraction(2, 3);
        Fraction b = new Fraction(4, 5);
        Fraction quot = a.divide(b);
        assertEquals(5, quot.getNumerator());
        assertEquals(6, quot.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddIdentityZero() {
        Fraction a = new Fraction(5, 7);
        Fraction zero = Fraction.ZERO;
        assertSame(a, a.add(zero));
        assertSame(a, zero.add(a));
    }

    @Test(timeout = 4000)
    public void testMultiplyByZero() {
        Fraction a = new Fraction(5, 7);
        Fraction zero = Fraction.ZERO;
        assertSame(Fraction.ZERO, a.multiply(zero));
        assertSame(Fraction.ZERO, zero.multiply(a));
    }

    @Test(timeout = 4000)
    public void testDoubleValue() {
        Fraction f = new Fraction(3, 8);
        assertEquals(0.375, f.doubleValue(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testFloatValue() {
        Fraction f = new Fraction(1, 3);
        assertTrue(f.floatValue() > 0.33333333f && f.floatValue() < 0.33333334f);
    }

    @Test(timeout = 4000)
    public void testIntValue() {
        Fraction f = new Fraction(7, 3);
        assertEquals(2, f.intValue());
    }

    @Test(timeout = 4000)
    public void testLongValue() {
        Fraction f = new Fraction(22, 7);
        assertEquals(3L, f.longValue());
    }

    @Test(timeout = 4000)
    public void testStaticConstants() {
        assertEquals(2, Fraction.TWO.getNumerator());
        assertEquals(1, Fraction.TWO.getDenominator());
        assertEquals(1, Fraction.ONE.getNumerator());
        assertEquals(0, Fraction.ZERO.getNumerator());
        assertEquals(-1, Fraction.MINUS_ONE.getNumerator());
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testConstructorWithMaxDenominator() throws FractionConversionException {
        // value very close to integer, should return integer fraction
        Fraction f = new Fraction(2.999999999, 1000);
        assertEquals(3, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorWithMinNumeratorAndDenominator() {
        // ensure no overflow when denominator negative and numerator is MIN_VALUE
        try {
            new Fraction(Integer.MIN_VALUE, -1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithDenominatorMinValue() {
        // denominator Integer.MIN_VALUE handled in getReducedFraction
        try {
            new Fraction(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNegateWithMinNumerator() {
        Fraction f = new Fraction(Integer.MIN_VALUE, 1);
        try {
            f.negate();
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReciprocalWithMinValue() {
        // reciprocal of Integer.MIN_VALUE/1 = 1/Integer.MIN_VALUE, but denominator becomes MIN_VALUE which triggers exception in constructor
        Fraction f = new Fraction(1, 1);
        // not a problem, but test a case that would cause overflow when negating?
        // Actually reciprocal calls new Fraction(denominator, numerator).
        // If numerator is MIN_VALUE and denominator is 1, reciprocal gives 1/MIN_VALUE, but constructor handles negative denominator.
        // Denominator negative, numerator positive, it will try to negate: num becomes -1? Wait: denominator MIN_VALUE is <0, and numerator != MIN_VALUE, so it negates numerator (-1) and denominator (MIN_VALUE -> -MIN_VALUE overflow?)
        // Actually denominator = Integer.MIN_VALUE, is negative, but denominator==Integer.MIN_VALUE is caught before negation: throws ArithmeticException.
        // So test it:
        Fraction g = new Fraction(1, Integer.MIN_VALUE); // should throw
        // but constructor doesn't allow that because den <0 and den==MIN_VALUE => throw.
        // So we can't even create it.
        // Instead, test reciprocal of a fraction that has denominator = 1 and numerator = MIN_VALUE+1? That denominator is 1, not MIN_VALUE.
        // The reciprocal of Fraction(1, Integer.MIN_VALUE) would be Fraction(Integer.MIN_VALUE, 1), which is valid.
        // But to create Fraction(1, Integer.MIN_VALUE) we need den = MIN_VALUE, which throws.
        // So test reciprocal of Fraction(Integer.MIN_VALUE, 1) -> new Fraction(1, Integer.MIN_VALUE) -> throws.
        Fraction f = new Fraction(Integer.MIN_VALUE, 1);
        try {
            f.reciprocal();
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddOverflow() {
        Fraction a = new Fraction(Integer.MAX_VALUE, 1);
        Fraction b = new Fraction(1, 1);
        try {
            a.add(b);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMultiplyOverflow() {
        Fraction a = new Fraction(Integer.MAX_VALUE / 2 + 1, 1);
        Fraction b = new Fraction(2, 1);
        try {
            a.multiply(b);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDivideOverflowViaMultiply() {
        Fraction a = new Fraction(Integer.MAX_VALUE / 2 + 1, 1);
        Fraction b = new Fraction(1, 2);
        try {
            a.divide(b);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionZeroDenominator() {
        try {
            Fraction.getReducedFraction(1, 0);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionZeroNumerator() {
        assertSame(Fraction.ZERO, Fraction.getReducedFraction(0, 5));
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionWithMinValue() {
        // allow 2^k/-2^31 as valid when k>0 -> numerator even
        Fraction f = Fraction.getReducedFraction(2, Integer.MIN_VALUE);
        // After reduction: 1/ -1073741824, but sign moves to numerator: -1/1073741824
        assertEquals(-1, f.getNumerator());
        assertEquals(1073741824, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionWithMinValueOddNumerator() {
        // odd numerator cannot be halved
        try {
            Fraction.getReducedFraction(1, Integer.MIN_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    // ========== Partition C: Defect-Targeted (compareTo bug) ==========

    @Test(timeout = 4000)
    public void testCompareToRevealsBug() {
        // These two fractions are very close but distinct, f1 < f2 mathematically.
        // Due to double rounding, their double values are equal, causing buggy
        // compareTo to return 0 instead of -1.
        Fraction f1 = new Fraction(1999999999, 2000000000);
        Fraction f2 = new Fraction(2000000000, 2000000001);
        assertTrue("compareTo should be < 0, but buggy version returns 0", f1.compareTo(f2) < 0);
        assertTrue(f2.compareTo(f1) > 0);
        assertFalse(f1.equals(f2));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddNull() {
        new Fraction(1, 2).add(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSubtractNull() {
        new Fraction(1, 2).subtract(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMultiplyNull() {
        new Fraction(1, 2).multiply(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDivideNull() {
        new Fraction(1, 2).divide(null);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testDivideByZero() {
        new Fraction(1, 2).divide(Fraction.ZERO);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testConstructorDenominatorZero() {
        new Fraction(1, 0);
    }

    @Test(timeout = 4000, expected = FractionConversionException.class)
    public void testDoubleConstructorConvergenceFailure() throws FractionConversionException {
        // Use a very small epsilon and max iterations to force failure
        new Fraction(3.141592653589793, 1e-20, 5);
    }

    @Test(timeout = 4000, expected = FractionConversionException.class)
    public void testDoubleConstructorOverflowInContinuedFraction() throws FractionConversionException {
        // value that causes a0 to exceed Integer.MAX_VALUE
        new Fraction(1.0e10, 0.01, Integer.MAX_VALUE, 10);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testEqualsReflexive() {
        Fraction f = new Fraction(2, 3);
        assertTrue(f.equals(f));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        Fraction f = new Fraction(2, 3);
        assertFalse(f.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        Fraction f = new Fraction(2, 3);
        assertFalse(f.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsSame() {
        Fraction a = new Fraction(2, 3);
        Fraction b = new Fraction(2, 3);
        assertTrue(a.equals(b));
    }

    @Test(timeout = 4000)
    public void testEqualsReducedForm() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 4);
        assertTrue(a.equals(b));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferent() {
        Fraction a = new Fraction(2, 3);
        Fraction b = new Fraction(3, 4);
        assertFalse(a.equals(b));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Fraction f = new Fraction(5, 7);
        int h1 = f.hashCode();
        int h2 = f.hashCode();
        assertEquals(h1, h2);
    }

    @Test(timeout = 4000)
    public void testHashCodeEquality() {
        Fraction a = new Fraction(1, 3);
        Fraction b = new Fraction(2, 6);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test(timeout = 4000)
    public void testCompareToConsistentWithEquals() {
        Fraction a = new Fraction(1, 3);
        Fraction b = new Fraction(2, 6);
        assertEquals(0, a.compareTo(b));  // equals returns true
        assertTrue(a.equals(b));
    }

    @Test(timeout = 4000)
    public void testCompareToNaturalOrdering() {
        Fraction small = new Fraction(1, 4);
        Fraction medium = new Fraction(1, 3);
        Fraction large = new Fraction(1, 2);
        assertTrue(small.compareTo(medium) < 0);
        assertTrue(medium.compareTo(large) < 0);
        assertTrue(small.compareTo(large) < 0);
        assertTrue(large.compareTo(medium) > 0);
        assertTrue(medium.compareTo(small) > 0);
    }

    @Test(timeout = 4000)
    public void testCompareToWithNegative() {
        Fraction neg = new Fraction(-1, 3);
        Fraction zero = Fraction.ZERO;
        assertTrue(neg.compareTo(zero) < 0);
        assertTrue(zero.compareTo(neg) > 0);
    }
}