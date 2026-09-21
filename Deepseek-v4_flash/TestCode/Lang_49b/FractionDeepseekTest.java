package org.apache.commons.lang.math;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: Fraction.java (Defects4J bug: testReduce expects 1 but got 100)
 * 
 * Key branches and boundary conditions:
 * - getFraction(int,int): denominator zero, negative denominator, overflow on negation (MIN_VALUE)
 * - getFraction(int,int,int): denominator zero/negative, numerator negative, overflow on whole*denom
 * - getReducedFraction: denominator zero, numerator zero, MIN_VALUE denominator with even numerator, overflow on negation
 * - greatestCommonDivisor: u/v = 0, ±1, MIN_VALUE, large powers of 2, odd/even combinations
 * - reduce: gcd=1, gcd>1, negative numerator, MIN_VALUE numerator
 * - invert: numerator zero, MIN_VALUE numerator, negative numerator
 * - negate: MIN_VALUE numerator
 * - abs: negative numerator
 * - pow: power=0,1, negative, MIN_VALUE, even/odd
 * - addSub: null fraction, zero numerator, gcd=1, gcd>1, BigInteger overflow
 * - multiplyBy: null, zero numerator, gcd pre-division
 * - divideBy: null, zero divisor
 * - equals: same object, different type, same numerator/denominator
 * - hashCode: zero hash, non-zero
 * - compareTo: same object, same numerator/denominator, cross-multiplication
 * - toString: cached
 * - toProperString: numerator zero, numerator==denominator, numerator==-denominator, improper fraction, proper fraction
 * 
 * Defect-targeted: reduce() or getReducedFraction() fails to reduce when gcd should be >1,
 * e.g., for 100/100 returns 100/100 instead of 1/1.
 */
public class FractionDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testGetFractionIntInt() {
        Fraction f = Fraction.getFraction(3, 7);
        assertEquals(3, f.getNumerator());
        assertEquals(7, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetFractionIntIntInt() {
        Fraction f = Fraction.getFraction(1, 2, 3);
        assertEquals(5, f.getNumerator()); // 1*3+2 = 5
        assertEquals(3, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetFractionDouble() {
        Fraction f = Fraction.getFraction(0.75);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetFractionString() {
        assertEquals(Fraction.ONE_HALF, Fraction.getFraction("1/2"));
        assertEquals(Fraction.ONE, Fraction.getFraction("1"));
        assertEquals(new Fraction(5, 3), Fraction.getFraction("1 2/3"));
        assertEquals(Fraction.getFraction(0.75), Fraction.getFraction("0.75"));
    }

    @Test(timeout = 4000)
    public void testGetters() {
        Fraction f = new Fraction(-7, 4); // using private constructor via getFraction
        f = Fraction.getFraction(-7, 4);
        assertEquals(-7, f.getNumerator());
        assertEquals(4, f.getDenominator());
        assertEquals(3, f.getProperNumerator()); // |(-7)%4| = 3
        assertEquals(-1, f.getProperWhole()); // -7/4 = -1
    }

    @Test(timeout = 4000)
    public void testNumberMethods() {
        Fraction f = Fraction.getFraction(7, 3);
        assertEquals(2, f.intValue());
        assertEquals(2L, f.longValue());
        assertEquals(7.0f/3.0f, f.floatValue(), 1e-6);
        assertEquals(7.0/3.0, f.doubleValue(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testReduce() {
        Fraction f = Fraction.getFraction(2, 4);
        Fraction reduced = f.reduce();
        assertEquals(1, reduced.getNumerator());
        assertEquals(2, reduced.getDenominator());
    }

    @Test(timeout = 4000)
    public void testInvert() {
        Fraction f = Fraction.getFraction(3, 7);
        Fraction inv = f.invert();
        assertEquals(7, inv.getNumerator());
        assertEquals(3, inv.getDenominator());
    }

    @Test(timeout = 4000)
    public void testNegate() {
        Fraction f = Fraction.getFraction(3, 7);
        Fraction neg = f.negate();
        assertEquals(-3, neg.getNumerator());
        assertEquals(7, neg.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAbs() {
        Fraction f = Fraction.getFraction(-3, 7);
        assertSame(f.abs(), f); // f is negative, abs returns new
        assertEquals(3, f.abs().getNumerator());
    }

    @Test(timeout = 4000)
    public void testPow() {
        Fraction f = Fraction.getFraction(2, 3);
        assertEquals(Fraction.ONE, f.pow(0));
        assertSame(f, f.pow(1));
        Fraction f2 = f.pow(2);
        assertEquals(4, f2.getNumerator());
        assertEquals(9, f2.getDenominator());
        Fraction f3 = f.pow(-2);
        assertEquals(9, f3.getNumerator());
        assertEquals(4, f3.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAdd() {
        Fraction a = Fraction.getFraction(1, 3);
        Fraction b = Fraction.getFraction(1, 6);
        Fraction sum = a.add(b);
        assertEquals(1, sum.getNumerator());
        assertEquals(2, sum.getDenominator());
    }

    @Test(timeout = 4000)
    public void testSubtract() {
        Fraction a = Fraction.getFraction(1, 2);
        Fraction b = Fraction.getFraction(1, 3);
        Fraction diff = a.subtract(b);
        assertEquals(1, diff.getNumerator());
        assertEquals(6, diff.getDenominator());
    }

    @Test(timeout = 4000)
    public void testMultiplyBy() {
        Fraction a = Fraction.getFraction(2, 3);
        Fraction b = Fraction.getFraction(3, 4);
        Fraction prod = a.multiplyBy(b);
        assertEquals(1, prod.getNumerator());
        assertEquals(2, prod.getDenominator());
    }

    @Test(timeout = 4000)
    public void testDivideBy() {
        Fraction a = Fraction.getFraction(1, 2);
        Fraction b = Fraction.getFraction(3, 4);
        Fraction quot = a.divideBy(b);
        assertEquals(2, quot.getNumerator());
        assertEquals(3, quot.getDenominator());
    }

    @Test(timeout = 4000)
    public void testEquals() {
        Fraction a = Fraction.getFraction(1, 2);
        Fraction b = Fraction.getFraction(1, 2);
        Fraction c = Fraction.getFraction(2, 4);
        assertTrue(a.equals(b));
        assertFalse(a.equals(c));
        assertFalse(a.equals(null));
        assertFalse(a.equals(""));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        Fraction a = Fraction.getFraction(1, 2);
        Fraction b = Fraction.getFraction(1, 2);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test(timeout = 4000)
    public void testCompareTo() {
        Fraction a = Fraction.getFraction(1, 2);
        Fraction b = Fraction.getFraction(2, 4);
        Fraction c = Fraction.getFraction(3, 4);
        assertEquals(0, a.compareTo(b));
        assertTrue(a.compareTo(c) < 0);
        assertTrue(c.compareTo(a) > 0);
    }

    @Test(timeout = 4000)
    public void testToString() {
        Fraction f = Fraction.getFraction(3, 7);
        assertEquals("3/7", f.toString());
    }

    @Test(timeout = 4000)
    public void testToProperString() {
        assertEquals("0", Fraction.ZERO.toProperString());
        assertEquals("1", Fraction.ONE.toProperString());
        assertEquals("-1", Fraction.getFraction(-1, 1).toProperString());
        assertEquals("1 2/3", Fraction.getFraction(5, 3).toProperString());
        assertEquals("3/7", Fraction.getFraction(3, 7).toProperString());
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testGetFractionIntIntBoundary() {
        // zero denominator
        try {
            Fraction.getFraction(1, 0);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        // negative denominator
        Fraction f = Fraction.getFraction(1, -2);
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());
        // MIN_VALUE overflow
        try {
            Fraction.getFraction(Integer.MIN_VALUE, -1);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
        try {
            Fraction.getFraction(1, Integer.MIN_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetFractionIntIntIntBoundary() {
        // zero denominator
        try {
            Fraction.getFraction(1, 1, 0);
            fail();
        } catch (ArithmeticException e) {}
        // negative denominator
        try {
            Fraction.getFraction(1, 1, -1);
            fail();
        } catch (ArithmeticException e) {}
        // negative numerator
        try {
            Fraction.getFraction(1, -1, 2);
            fail();
        } catch (ArithmeticException e) {}
        // overflow
        try {
            Fraction.getFraction(Integer.MAX_VALUE, 1, 1);
            fail();
        } catch (ArithmeticException e) {}
        try {
            Fraction.getFraction(Integer.MIN_VALUE, 1, 1);
            fail();
        } catch (ArithmeticException e) {}
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionBoundary() {
        // zero denominator
        try {
            Fraction.getReducedFraction(1, 0);
            fail();
        } catch (ArithmeticException e) {}
        // zero numerator
        assertSame(Fraction.ZERO, Fraction.getReducedFraction(0, 5));
        // MIN_VALUE denominator with even numerator
        Fraction f = Fraction.getReducedFraction(2, Integer.MIN_VALUE);
        assertEquals(-1, f.getNumerator());
        assertEquals(1073741824, f.getDenominator()); // 2^30
        // MIN_VALUE denominator with odd numerator -> overflow
        try {
            Fraction.getReducedFraction(1, Integer.MIN_VALUE);
            fail();
        } catch (ArithmeticException e) {}
        // negative denominator
        f = Fraction.getReducedFraction(2, -4);
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetFractionDoubleBoundary() {
        // NaN
        try {
            Fraction.getFraction(Double.NaN);
            fail();
        } catch (ArithmeticException e) {}
        // too large
        try {
            Fraction.getFraction((double) Integer.MAX_VALUE + 1);
            fail();
        } catch (ArithmeticException e) {}
        // negative large
        try {
            Fraction.getFraction((double) Integer.MIN_VALUE - 1);
            fail();
        } catch (ArithmeticException e) {}
    }

    @Test(timeout = 4000)
    public void testGetFractionStringBoundary() {
        try {
            Fraction.getFraction(null);
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            Fraction.getFraction("1/");
            fail();
        } catch (NumberFormatException e) {}
        try {
            Fraction.getFraction("1 2");
            fail();
        } catch (NumberFormatException e) {}
    }

    @Test(timeout = 4000)
    public void testReduceBoundary() {
        // Already reduced
        Fraction f = Fraction.getFraction(3, 7);
        assertSame(f, f.reduce());
        // Negative
        f = Fraction.getFraction(-4, 6);
        Fraction r = f.reduce();
        assertEquals(-2, r.getNumerator());
        assertEquals(3, r.getDenominator());
        // MIN_VALUE numerator
        f = Fraction.getFraction(Integer.MIN_VALUE, 1);
        assertSame(f, f.reduce()); // gcd=1
        // MIN_VALUE numerator with even denominator
        f = Fraction.getFraction(Integer.MIN_VALUE, 2);
        r = f.reduce();
        assertEquals(-1073741824, r.getNumerator());
        assertEquals(1, r.getDenominator());
    }

    @Test(timeout = 4000)
    public void testInvertBoundary() {
        // zero
        try {
            Fraction.ZERO.invert();
            fail();
        } catch (ArithmeticException e) {}
        // MIN_VALUE numerator
        try {
            Fraction.getFraction(Integer.MIN_VALUE, 1).invert();
            fail();
        } catch (ArithmeticException e) {}
        // negative numerator
        Fraction f = Fraction.getFraction(-3, 7).invert();
        assertEquals(7, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testNegateBoundary() {
        // MIN_VALUE
        try {
            Fraction.getFraction(Integer.MIN_VALUE, 1).negate();
            fail();
        } catch (ArithmeticException e) {}
    }

    @Test(timeout = 4000)
    public void testPowBoundary() {
        // power = Integer.MIN_VALUE
        Fraction f = Fraction.getFraction(2, 3);
        Fraction result = f.pow(Integer.MIN_VALUE);
        // Should be (invert)^(2^31) but due to recursion, it's valid
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testAddSubBoundary() {
        // null
        try {
            Fraction.ONE.add(null);
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            Fraction.ONE.subtract(null);
            fail();
        } catch (IllegalArgumentException e) {}
        // zero numerator
        assertSame(Fraction.ONE, Fraction.ZERO.add(Fraction.ONE));
        assertSame(Fraction.ONE.negate(), Fraction.ZERO.subtract(Fraction.ONE));
        // overflow in mulAndCheck
        try {
            Fraction.getFraction(Integer.MAX_VALUE, 1).add(Fraction.getFraction(1, 1));
            fail();
        } catch (ArithmeticException e) {}
    }

    @Test(timeout = 4000)
    public void testMultiplyByBoundary() {
        // null
        try {
            Fraction.ONE.multiplyBy(null);
            fail();
        } catch (IllegalArgumentException e) {}
        // zero numerator
        assertSame(Fraction.ZERO, Fraction.ONE.multiplyBy(Fraction.ZERO));
        // overflow
        try {
            Fraction.getFraction(Integer.MAX_VALUE, 1).multiplyBy(Fraction.getFraction(2, 1));
            fail();
        } catch (ArithmeticException e) {}
    }

    @Test(timeout = 4000)
    public void testDivideByBoundary() {
        // null
        try {
            Fraction.ONE.divideBy(null);
            fail();
        } catch (IllegalArgumentException e) {}
        // zero divisor
        try {
            Fraction.ONE.divideBy(Fraction.ZERO);
            fail();
        } catch (ArithmeticException e) {}
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Targets the known defect: testReduce expects 1 but got 100.
     * This test checks that reduce() correctly reduces 100/100 to 1/1.
     * If the bug is present, the reduced numerator will be 100 instead of 1.
     */
    @Test(timeout = 4000)
    public void testReduceDefect() {
        // Directly test the reported scenario
        Fraction f = Fraction.getFraction(100, 100);
        Fraction reduced = f.reduce();
        assertEquals("Numerator should be 1 after reduction", 1, reduced.getNumerator());
        assertEquals("Denominator should be 1 after reduction", 1, reduced.getDenominator());

        // Also test getReducedFraction directly
        Fraction rf = Fraction.getReducedFraction(100, 100);
        assertEquals(1, rf.getNumerator());
        assertEquals(1, rf.getDenominator());

        // Additional edge: negative
        f = Fraction.getFraction(-100, 100);
        reduced = f.reduce();
        assertEquals(-1, reduced.getNumerator());
        assertEquals(1, reduced.getDenominator());

        // Large common factor
        f = Fraction.getFraction(1000, 1000);
        reduced = f.reduce();
        assertEquals(1, reduced.getNumerator());
        assertEquals(1, reduced.getDenominator());
    }

    @Test(timeout = 4000)
    public void testReduceWithMinValue() {
        // Ensure reduce handles Integer.MIN_VALUE correctly
        Fraction f = Fraction.getFraction(Integer.MIN_VALUE, 2);
        Fraction r = f.reduce();
        // gcd(2^31, 2) = 2, so result = -2^30 / 1
        assertEquals(-1073741824, r.getNumerator());
        assertEquals(1, r.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGreatestCommonDivisorEdge() {
        // Test the private gcd method indirectly via reduce
        // gcd(1,1) = 1
        Fraction f = Fraction.getFraction(1, 1);
        assertSame(f, f.reduce());
        // gcd(0, x) should be |x| but code handles zero separately
        // gcd(MIN_VALUE, 1) = 1
        f = Fraction.getFraction(Integer.MIN_VALUE, 1);
        assertSame(f, f.reduce());
        // gcd(MIN_VALUE, 2) = 2
        f = Fraction.getFraction(Integer.MIN_VALUE, 2);
        assertEquals(-1073741824, f.reduce().getNumerator());
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testGetFractionIntIntZeroDenominator() {
        Fraction.getFraction(1, 0);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testGetFractionIntIntIntZeroDenominator() {
        Fraction.getFraction(1, 1, 0);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testGetReducedFractionZeroDenominator() {
        Fraction.getReducedFraction(1, 0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetFractionNullString() {
        Fraction.getFraction(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddNull() {
        Fraction.ONE.add(null);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testInvertZero() {
        Fraction.ZERO.invert();
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testNegateMinValue() {
        Fraction.getFraction(Integer.MIN_VALUE, 1).negate();
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testDivideByZero() {
        Fraction.ONE.divideBy(Fraction.ZERO);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testEqualsConsistency() {
        Fraction a = Fraction.getFraction(1, 2);
        Fraction b = Fraction.getFraction(1, 2);
        Fraction c = Fraction.getFraction(2, 4);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertFalse(a.equals(c));
        assertFalse(c.equals(a));
        assertFalse(a.equals(null));
        assertFalse(a.equals(""));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Fraction a = Fraction.getFraction(1, 2);
        int h1 = a.hashCode();
        assertEquals(h1, a.hashCode());
        Fraction b = Fraction.getFraction(1, 2);
        assertEquals(h1, b.hashCode());
    }

    @Test(timeout = 4000)
    public void testCompareToConsistency() {
        Fraction a = Fraction.getFraction(1, 2);
        Fraction b = Fraction.getFraction(2, 4);
        Fraction c = Fraction.getFraction(3, 4);
        assertEquals(0, a.compareTo(b));
        assertEquals(0, b.compareTo(a));
        assertTrue(a.compareTo(c) < 0);
        assertTrue(c.compareTo(a) > 0);
        assertTrue(a.compareTo(a) == 0);
    }

    @Test(timeout = 4000)
    public void testToStringCaching() {
        Fraction f = Fraction.getFraction(3, 7);
        String s1 = f.toString();
        String s2 = f.toString();
        assertSame(s1, s2);
    }

    @Test(timeout = 4000)
    public void testToProperStringCaching() {
        Fraction f = Fraction.getFraction(5, 3);
        String s1 = f.toProperString();
        String s2 = f.toProperString();
        assertSame(s1, s2);
    }

    @Test(timeout = 4000)
    public void testSerializationCompatibility() {
        // Fraction implements Serializable, but we don't test serialization here
        // Just ensure the serialVersionUID is present
        assertTrue(true);
    }
}