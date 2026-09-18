package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * The known defect in Defects4J: When reducing a fraction with numerator = Integer.MIN_VALUE and denominator = 2,
 * the reduce() method (and getReducedFraction) fails because greatestCommonDivisor incorrectly handles
 * Math.abs(Integer.MIN_VALUE) returning a negative value, causing the gcd to be 1 and the fraction not reduced.
 * The expected reduced numerator is -1073741824, but the buggy version returns the original -2147483648.
 * 
 * Key branches and boundaries tested:
 * - getReducedFraction: denominator == 0, special case denominator == Integer.MIN_VALUE && numerator even,
 *   denominator < 0 and overflow check, gcd normalization.
 * - reduce: numerator == 0, gcd == 1, gcd > 1, overflow from Math.abs(Integer.MIN_VALUE).
 * - getFraction(int, int): denominator == 0, denominator < 0 with overflow check.
 * - getFraction(int, int, int): denominator zero/negative, numerator negative, overflow.
 * - getFraction(double): NaN, INF, overflow, convergence.
 * - getFraction(String): null, dot, space, slash, whole number.
 * - Arithmetic operations: add, subtract, multiply, divide, invert, negate, abs, pow.
 * - Object contracts: equals, hashCode, compareTo, toString, toProperString.
 * - Boundary values: 0, 1, Integer.MAX_VALUE, Integer.MIN_VALUE.
 */
public class FractionDeepseekTest {

    // ==================== PART A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testGetFractionSimple() {
        Fraction f = Fraction.getFraction(3, 7);
        assertEquals(3, f.getNumerator());
        assertEquals(7, f.getDenominator());
        assertEquals(0.42857142857142855, f.doubleValue(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetFractionNegativeDenominator() {
        Fraction f = Fraction.getFraction(3, -7);
        assertEquals(-3, f.getNumerator());
        assertEquals(7, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetFractionWhole() {
        Fraction f = Fraction.getFraction(1, 2, 3);
        assertEquals(5, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetFractionWholeNegative() {
        Fraction f = Fraction.getFraction(-1, 2, 3);
        assertEquals(-5, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetFractionFromDouble() {
        Fraction f = Fraction.getFraction(0.75);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetFractionFromStringWhole() {
        Fraction f = Fraction.getFraction("5");
        assertEquals(5, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetFractionFromStringSpace() {
        Fraction f = Fraction.getFraction("1 2/3");
        assertEquals(5, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetFractionFromStringSlash() {
        Fraction f = Fraction.getFraction("4/7");
        assertEquals(4, f.getNumerator());
        assertEquals(7, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetFractionFromStringDot() {
        Fraction f = Fraction.getFraction("0.5");
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionAlreadyReduced() {
        Fraction f = Fraction.getReducedFraction(3, 7);
        assertEquals(3, f.getNumerator());
        assertEquals(7, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionReduces() {
        Fraction f = Fraction.getReducedFraction(6, 8);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionZero() {
        Fraction f = Fraction.getReducedFraction(0, 5);
        assertSame(Fraction.ZERO, f);
    }

    @Test(timeout = 4000)
    public void testReduceNoChange() {
        Fraction f = Fraction.getFraction(3, 7);
        assertSame(f, f.reduce());
    }

    @Test(timeout = 4000)
    public void testReduceReduces() {
        Fraction f = Fraction.getFraction(6, 8);
        Fraction reduced = f.reduce();
        assertEquals(3, reduced.getNumerator());
        assertEquals(4, reduced.getDenominator());
    }

    @Test(timeout = 4000)
    public void testReduceZero() {
        assertSame(Fraction.ZERO, Fraction.ZERO.reduce());
    }

    // ==================== PART B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testGetFractionMinValueDenominator() {
        // denominator = Integer.MIN_VALUE, numerator positive (even)
        Fraction f = Fraction.getFraction(2, Integer.MIN_VALUE);
        assertEquals(2, f.getNumerator());
        assertEquals(Integer.MIN_VALUE, f.getDenominator());
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testGetFractionOverflowNegateNumerator() {
        Fraction.getFraction(Integer.MIN_VALUE, -1);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testGetFractionOverflowNegateDenominator() {
        Fraction.getFraction(1, Integer.MIN_VALUE);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testGetFractionDenominatorZero() {
        Fraction.getFraction(1, 0);
    }

    @Test(timeout = 4000)
    public void testGetFractionWholeLargeValue() {
        // whole = Integer.MAX_VALUE, numerator=1, denominator=2 -> overflow
        try {
            Fraction.getFraction(Integer.MAX_VALUE, 1, 2);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetFractionDoubleVeryLarge() {
        try {
            Fraction.getFraction(Double.MAX_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testGetFractionDoubleNaN() {
        Fraction.getFraction(Double.NaN);
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testGetFractionStringInvalid() {
        Fraction.getFraction("abc");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetFractionStringNull() {
        Fraction.getFraction(null);
    }

    // ==================== PART C: Defect-Targeted Branch Zone ====================

    /**
     * Targets the known bug where getReducedFraction with numerator = Integer.MIN_VALUE
     * and denominator = 2 fails to reduce because greatestCommonDivisor incorrectly returns 1
     * when Math.abs(Integer.MIN_VALUE) is negative and <= 1.
     */
    @Test(timeout = 4000)
    public void testReducedFactoryWithMinValueNumerator() {
        Fraction f = Fraction.getReducedFraction(Integer.MIN_VALUE, 2);
        assertEquals("Expected numerator -1073741824 after reduction", -1073741824, f.getNumerator());
        assertEquals("Expected denominator 1", 1, f.getDenominator());
    }

    /**
     * Targets the same bug via the reduce() instance method.
     */
    @Test(timeout = 4000)
    public void testReduceWithMinValueNumerator() {
        Fraction f = Fraction.getFraction(Integer.MIN_VALUE, 2);
        Fraction reduced = f.reduce();
        assertEquals("Expected numerator -1073741824 after reduction", -1073741824, reduced.getNumerator());
        assertEquals("Expected denominator 1", 1, reduced.getDenominator());
    }

    /**
     * Additional test: numerator positive, denominator = Integer.MIN_VALUE (even) should reduce correctly.
     */
    @Test(timeout = 4000)
    public void testReducedFactoryWithMinValueDenominatorEven() {
        // numerator=4, denominator=MIN_VALUE; after divide by 2: numerator=2, denominator=-1073741824;
        // then negate: numerator=-2, denominator=1073741824; gcd=2 -> (-1, 536870912)
        Fraction f = Fraction.getReducedFraction(4, Integer.MIN_VALUE);
        assertEquals(-1, f.getNumerator());
        assertEquals(536870912, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testReducedFactoryWithMinValueDenominatorOdd() {
        // numerator=3, denominator=MIN_VALUE -> must throw (negate overflow because denominator==MIN_VALUE)
        try {
            Fraction.getReducedFraction(3, Integer.MIN_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    // ==================== PART D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testInvertZero() {
        Fraction.ZERO.invert();
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testInvertMinValue() {
        Fraction.getFraction(Integer.MIN_VALUE, 1).invert();
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testNegateMinValue() {
        Fraction.getFraction(Integer.MIN_VALUE, 1).negate();
    }

    @Test(timeout = 4000)
    public void testAbsPositive() {
        Fraction f = Fraction.getFraction(3, 4);
        assertSame(f, f.abs());
    }

    @Test(timeout = 4000)
    public void testAbsNegative() {
        Fraction f = Fraction.getFraction(-3, 4);
        Fraction abs = f.abs();
        assertEquals(3, abs.getNumerator());
        assertEquals(4, abs.getDenominator());
    }

    @Test(timeout = 4000)
    public void testPowZero() {
        assertSame(Fraction.ONE, Fraction.ONE.pow(0));
    }

    @Test(timeout = 4000)
    public void testPowOne() {
        Fraction f = Fraction.getFraction(2, 3);
        assertSame(f, f.pow(1));
    }

    @Test(timeout = 4000)
    public void testPowNegativeExponent() {
        Fraction f = Fraction.getFraction(2, 3);
        Fraction result = f.pow(-2);
        assertEquals(9, result.getNumerator());
        assertEquals(4, result.getDenominator());
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testPowOverflow() {
        Fraction.getFraction(Integer.MAX_VALUE, 1).pow(2);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddNull() {
        Fraction.ONE.add(null);
    }

    @Test(timeout = 4000)
    public void testAddZeroNumerator() {
        Fraction f = Fraction.getFraction(0, 1);
        assertSame(Fraction.ONE, f.add(Fraction.ONE));
    }

    @Test(timeout = 4000)
    public void testAddZeroOtherNumerator() {
        assertSame(Fraction.ONE, Fraction.ONE.add(Fraction.ZERO));
    }

    @Test(timeout = 4000)
    public void testAddSameDenominator() {
        Fraction f1 = Fraction.getFraction(1, 5);
        Fraction f2 = Fraction.getFraction(2, 5);
        Fraction result = f1.add(f2);
        assertEquals(3, result.getNumerator());
        assertEquals(5, result.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddDifferentDenominator() {
        Fraction f1 = Fraction.getFraction(1, 3);
        Fraction f2 = Fraction.getFraction(1, 6);
        Fraction result = f1.add(f2);
        assertEquals(1, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testAddOverflow() {
        Fraction f1 = Fraction.getFraction(Integer.MAX_VALUE, 1);
        Fraction f2 = Fraction.getFraction(Integer.MAX_VALUE, 1);
        f1.add(f2);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSubtractNull() {
        Fraction.ONE.subtract(null);
    }

    @Test(timeout = 4000)
    public void testSubtractSame() {
        Fraction f = Fraction.getFraction(3, 7);
        assertEquals(Fraction.ZERO, f.subtract(f));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMultiplyNull() {
        Fraction.ONE.multiplyBy(null);
    }

    @Test(timeout = 4000)
    public void testMultiplyByZero() {
        assertSame(Fraction.ZERO, Fraction.ONE.multiplyBy(Fraction.ZERO));
    }

    @Test(timeout = 4000)
    public void testMultiplyReduce() {
        Fraction f1 = Fraction.getFraction(2, 3);
        Fraction f2 = Fraction.getFraction(3, 4);
        Fraction result = f1.multiplyBy(f2);
        assertEquals(1, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDivideNull() {
        Fraction.ONE.divideBy(null);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testDivideByZero() {
        Fraction.ONE.divideBy(Fraction.ZERO);
    }

    @Test(timeout = 4000)
    public void testDivide() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(3, 4);
        Fraction result = f1.divideBy(f2);
        assertEquals(2, result.getNumerator());
        assertEquals(3, result.getDenominator());
    }

    // ==================== PART E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsSame() {
        assertTrue(Fraction.ONE.equals(Fraction.ONE));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentObject() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 2);
        assertTrue(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsNotFraction() {
        assertFalse(Fraction.ONE.equals("1"));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentNumerator() {
        assertFalse(Fraction.getFraction(1, 2).equals(Fraction.getFraction(2, 4)));
    }

    @Test(timeout = 4000)
    public void testHashCodeEqualFractions() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 2);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeDifferentFractions() {
        assertNotEquals(Fraction.ONE.hashCode(), Fraction.ZERO.hashCode());
    }

    @Test(timeout = 4000)
    public void testCompareToEqual() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 2);
        assertEquals(0, f1.compareTo(f2));
    }

    @Test(timeout = 4000)
    public void testCompareToLess() {
        Fraction f1 = Fraction.getFraction(1, 3);
        Fraction f2 = Fraction.getFraction(1, 2);
        assertTrue(f1.compareTo(f2) < 0);
    }

    @Test(timeout = 4000)
    public void testCompareToGreater() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 3);
        assertTrue(f1.compareTo(f2) > 0);
    }

    @Test(timeout = 4000)
    public void testCompareToCrossMultiplyEqual() {
        // 2/4 compareTo 1/2 => cross multiply 2*2 == 1*4 => 0
        Fraction f1 = Fraction.getFraction(2, 4);
        Fraction f2 = Fraction.getFraction(1, 2);
        assertEquals(0, f1.compareTo(f2));
    }

    @Test(timeout = 4000)
    public void testToString() {
        Fraction f = Fraction.getFraction(3, 7);
        assertEquals("3/7", f.toString());
    }

    @Test(timeout = 4000)
    public void testToProperStringImproperPositive() {
        Fraction f = Fraction.getFraction(7, 4);
        assertEquals("1 3/4", f.toProperString());
    }

    @Test(timeout = 4000)
    public void testToProperStringImproperNegative() {
        Fraction f = Fraction.getFraction(-7, 4);
        assertEquals("-1 3/4", f.toProperString());
    }

    @Test(timeout = 4000)
    public void testToProperStringWholeNumber() {
        Fraction f = Fraction.getFraction(5, 1);
        assertEquals("5", f.toProperString());
    }

    @Test(timeout = 4000)
    public void testToProperStringZero() {
        assertEquals("0", Fraction.ZERO.toProperString());
    }

    @Test(timeout = 4000)
    public void testToProperStringEqualNumeratorDenominator() {
        assertEquals("1", Fraction.ONE.toProperString());
    }

    @Test(timeout = 4000)
    public void testToProperStringProperFraction() {
        Fraction f = Fraction.getFraction(3, 7);
        assertEquals("3/7", f.toProperString());
    }

    @Test(timeout = 4000)
    public void testIntValue() {
        assertEquals(2, Fraction.getFraction(7, 3).intValue());
    }

    @Test(timeout = 4000)
    public void testLongValue() {
        assertEquals(2L, Fraction.getFraction(7, 3).longValue());
    }

    @Test(timeout = 4000)
    public void testFloatValue() {
        assertEquals(0.5f, Fraction.ONE_HALF.floatValue(), 1e-15f);
    }

    @Test(timeout = 4000)
    public void testDoubleValue() {
        assertEquals(0.75, Fraction.THREE_QUARTERS.doubleValue(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetProperNumerator() {
        assertEquals(3, Fraction.getFraction(7, 4).getProperNumerator());
    }

    @Test(timeout = 4000)
    public void testGetProperWhole() {
        assertEquals(1, Fraction.getFraction(7, 4).getProperWhole());
    }

    // Edge case: getProperNumerator with negative fraction
    @Test(timeout = 4000)
    public void testGetProperNumeratorNegative() {
        assertEquals(3, Fraction.getFraction(-7, 4).getProperNumerator());
    }

    // Edge case: getProperWhole with negative fraction
    @Test(timeout = 4000)
    public void testGetProperWholeNegative() {
        assertEquals(-1, Fraction.getFraction(-7, 4).getProperWhole());
    }

    // Additional defect-related: reduce with numerator = Integer.MIN_VALUE and denominator = even number > 0
    @Test(timeout = 4000)
    public void testReduceWithMinValueNumeratorAndEvenDenominator() {
        Fraction f = Fraction.getFraction(Integer.MIN_VALUE, 4);
        Fraction reduced = f.reduce();
        assertEquals(-536870912, reduced.getNumerator());
        assertEquals(1, reduced.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionWithMinValueNumeratorAndEvenDenominator() {
        Fraction f = Fraction.getReducedFraction(Integer.MIN_VALUE, 4);
        assertEquals(-536870912, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }
}