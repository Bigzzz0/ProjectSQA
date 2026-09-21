package org.apache.commons.lang.math;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: org.apache.commons.lang.math.Fraction
 * DEFECT TARGET (Defects4J): testReduce() failure where Fraction.getFraction(0, k).reduce()
 *   returns 0/k instead of 0/1 (expected denominator 1, was 100).
 * 
 * BRANCH / PARTITION COVERAGE:
 * - Partition A: Factory methods (getFraction with 2 ints, 3 ints, double, String)
 *     * Denominator 0, negative denominator, sign normalization, MIN_VALUE / MAX_VALUE checks.
 *     * Whole number combination, mixed fraction parsing, double continued fraction convergence.
 * - Partition B: Arithmetic Operations (add, subtract, multiplyBy, divideBy, pow)
 *     * Identity zeros (left and right operand zero branches).
 *     * Relatively prime denominators (d1 == 1) vs shared factors (d1 > 1, BigInteger logic).
 *     * Division by zero, multiplication by zero returning ZERO constant.
 *     * Integer overflow guards (mulAndCheck, mulPosAndCheck, addAndCheck, subAndCheck).
 *     * Negative powers, even/odd power recursion, Integer.MIN_VALUE power negation handling.
 * - Partition C: Transformations & Unary Operations (reduce, invert, negate, abs)
 *     * reduce(): gcd == 1 early return vs reduction, and numerator == 0 reduction (Defect Target).
 *     * invert(): zero numerator exception, MIN_VALUE negation exception, sign flips.
 *     * negate(): MIN_VALUE negation exception, positive/negative flips.
 *     * abs(): positive branch (returns this) vs negative branch (negate).
 * - Partition D: Value extraction and Number implementation (intValue, longValue, floatValue, doubleValue)
 * - Partition E: Contracts & String representations
 *     * equals & hashCode (symmetry, reflexivity, type safety, caching).
 *     * compareTo (consistent with cross-multiplication, reflexive, non-Fraction class cast).
 *     * toString and toProperString (zeros, 1, -1, proper fractions, improper fractions, caching).
 * ----------------------------------------------------------------------------------------------------
 */
public class FractionGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J ground truth)
    // =========================================================================

    /**
     * Targets Defects4J bug: Fraction.getFraction(0, 100).reduce() fails to reduce
     * to 0/1, resulting in denominator 100 instead of 1.
     */
    @Test(timeout = 4000)
    public void testReduceZeroNumeratorDefect() {
        Fraction f = Fraction.getFraction(0, 100);
        Fraction reduced = f.reduce();
        assertEquals(0, reduced.getNumerator());
        assertEquals(1, reduced.getDenominator());
    }

    // =========================================================================
    // Partition A: Core Factory Methods & Construction Logic
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetFractionTwoIntsNormal() {
        Fraction f = Fraction.getFraction(3, 4);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());

        Fraction negDenom = Fraction.getFraction(3, -4);
        assertEquals(-3, negDenom.getNumerator());
        assertEquals(4, negDenom.getDenominator());

        Fraction bothNeg = Fraction.getFraction(-3, -4);
        assertEquals(3, bothNeg.getNumerator());
        assertEquals(4, bothNeg.getDenominator());
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetFractionTwoIntsZeroDenominator() {
        Fraction.getFraction(1, 0);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetFractionTwoIntsNumeratorMinValNegDenom() {
        Fraction.getFraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetFractionTwoIntsDenomMinVal() {
        Fraction.getFraction(1, Integer.MIN_VALUE);
    }

    @Test(timeout = 4000)
    public void testGetFractionThreeIntsNormal() {
        Fraction f1 = Fraction.getFraction(1, 2, 3);
        assertEquals(5, f1.getNumerator());
        assertEquals(3, f1.getDenominator());

        Fraction f2 = Fraction.getFraction(-1, 2, 3);
        assertEquals(-5, f2.getNumerator());
        assertEquals(3, f2.getDenominator());

        Fraction f3 = Fraction.getFraction(0, 0, 5);
        assertEquals(0, f3.getNumerator());
        assertEquals(5, f3.getDenominator());
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetFractionThreeIntsZeroDenominator() {
        Fraction.getFraction(1, 2, 0);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetFractionThreeIntsNegativeDenominator() {
        Fraction.getFraction(1, 2, -3);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetFractionThreeIntsNegativeNumerator() {
        Fraction.getFraction(1, -2, 3);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetFractionThreeIntsPositiveOverflow() {
        Fraction.getFraction(Integer.MAX_VALUE, 1, 2);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetFractionThreeIntsNegativeOverflow() {
        Fraction.getFraction(Integer.MIN_VALUE, 1, 2);
    }

    @Test(timeout = 4000)
    public void testGetReducedFraction() {
        Fraction f = Fraction.getReducedFraction(0, 5);
        assertSame(Fraction.ZERO, f);

        Fraction f2 = Fraction.getReducedFraction(2, 4);
        assertEquals(1, f2.getNumerator());
        assertEquals(2, f2.getDenominator());

        Fraction f3 = Fraction.getReducedFraction(-2, -4);
        assertEquals(1, f3.getNumerator());
        assertEquals(2, f3.getDenominator());

        // Even numerator and denominator Integer.MIN_VALUE
        Fraction f4 = Fraction.getReducedFraction(2, Integer.MIN_VALUE);
        assertEquals(-1, f4.getNumerator());
        assertEquals(1073741824, f4.getDenominator());
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetReducedFractionZeroDenominator() {
        Fraction.getReducedFraction(1, 0);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetReducedFractionDenomMinValOddNumerator() {
        Fraction.getReducedFraction(1, Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetReducedFractionNumeratorMinValNegDenom() {
        Fraction.getReducedFraction(Integer.MIN_VALUE, -1);
    }

    @Test(timeout = 4000)
    public void testGetFractionDouble() {
        Fraction f1 = Fraction.getFraction(0.5);
        assertEquals(1, f1.getNumerator());
        assertEquals(2, f1.getDenominator());

        Fraction f2 = Fraction.getFraction(-0.75);
        assertEquals(-3, f2.getNumerator());
        assertEquals(4, f2.getDenominator());

        Fraction f3 = Fraction.getFraction(0.0);
        assertEquals(0, f3.getNumerator());
        assertEquals(1, f3.getDenominator());

        Fraction f4 = Fraction.getFraction(2.0);
        assertEquals(2, f4.getNumerator());
        assertEquals(1, f4.getDenominator());
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetFractionDoubleNaN() {
        Fraction.getFraction(Double.NaN);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetFractionDoubleTooLarge() {
        Fraction.getFraction((double) Integer.MAX_VALUE + 1000.0);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testGetFractionDoubleUnconverged() {
        // Very fine non-converging continued fraction within 25 iterations
        Fraction.getFraction(0.1234567890123456789);
    }

    @Test(timeout = 4000)
    public void testGetFractionString() {
        Fraction f1 = Fraction.getFraction("3/4");
        assertEquals(3, f1.getNumerator());
        assertEquals(4, f1.getDenominator());

        Fraction f2 = Fraction.getFraction("1 1/2");
        assertEquals(3, f2.getNumerator());
        assertEquals(2, f2.getDenominator());

        Fraction f3 = Fraction.getFraction("-1 1/2");
        assertEquals(-3, f3.getNumerator());
        assertEquals(2, f3.getDenominator());

        Fraction f4 = Fraction.getFraction("5");
        assertEquals(5, f4.getNumerator());
        assertEquals(1, f4.getDenominator());

        Fraction f5 = Fraction.getFraction("0.5");
        assertEquals(1, f5.getNumerator());
        assertEquals(2, f5.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetFractionStringNull() {
        Fraction.getFraction((String) null);
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testGetFractionStringInvalidMixed() {
        Fraction.getFraction("1 2");
    }

    // =========================================================================
    // Partition B: Accessors, Number Conversions & String Representations
    // =========================================================================

    @Test(timeout = 4000)
    public void testAccessorsAndNumberConversions() {
        Fraction improper = Fraction.getFraction(7, 4);
        assertEquals(7, improper.getNumerator());
        assertEquals(4, improper.getDenominator());
        assertEquals(3, improper.getProperNumerator());
        assertEquals(1, improper.getProperWhole());
        assertEquals(1, improper.intValue());
        assertEquals(1L, improper.longValue());
        assertEquals(1.75f, improper.floatValue(), 0.0001f);
        assertEquals(1.75d, improper.doubleValue(), 0.0001d);

        Fraction negImproper = Fraction.getFraction(-7, 4);
        assertEquals(-7, negImproper.getNumerator());
        assertEquals(4, negImproper.getDenominator());
        assertEquals(3, negImproper.getProperNumerator());
        assertEquals(-1, negImproper.getProperWhole());
    }

    @Test(timeout = 4000)
    public void testToStringAndToProperString() {
        Fraction f1 = Fraction.getFraction(3, 4);
        assertEquals("3/4", f1.toString());
        assertEquals("3/4", f1.toProperString());
        // Second call verifies caching
        assertEquals("3/4", f1.toString());
        assertEquals("3/4", f1.toProperString());

        Fraction f2 = Fraction.ZERO;
        assertEquals("0/1", f2.toString());
        assertEquals("0", f2.toProperString());

        Fraction f3 = Fraction.ONE;
        assertEquals("1/1", f3.toString());
        assertEquals("1", f3.toProperString());

        Fraction f4 = Fraction.getFraction(-1, 1);
        assertEquals("-1/1", f4.toString());
        assertEquals("-1", f4.toProperString());

        Fraction f5 = Fraction.getFraction(7, 4);
        assertEquals("7/4", f5.toString());
        assertEquals("1 3/4", f5.toProperString());

        Fraction f6 = Fraction.getFraction(-7, 4);
        assertEquals("-7/4", f6.toString());
        assertEquals("-1 3/4", f6.toProperString());

        Fraction f7 = Fraction.getFraction(8, 4);
        assertEquals("8/4", f7.toString());
        assertEquals("2", f7.toProperString());

        Fraction f8 = Fraction.getFraction(-8, 4);
        assertEquals("-8/4", f8.toString());
        assertEquals("-2", f8.toProperString());
    }

    // =========================================================================
    // Partition C: Transformations (reduce, invert, negate, abs, pow)
    // =========================================================================

    @Test(timeout = 4000)
    public void testReduceNormal() {
        Fraction f = Fraction.getFraction(2, 4);
        Fraction reduced = f.reduce();
        assertEquals(1, reduced.getNumerator());
        assertEquals(2, reduced.getDenominator());

        Fraction irreducible = Fraction.getFraction(1, 3);
        assertSame(irreducible, irreducible.reduce());
    }

    @Test(timeout = 4000)
    public void testInvert() {
        Fraction f1 = Fraction.getFraction(3, 4);
        Fraction inv1 = f1.invert();
        assertEquals(4, inv1.getNumerator());
        assertEquals(3, inv1.getDenominator());

        Fraction f2 = Fraction.getFraction(-3, 4);
        Fraction inv2 = f2.invert();
        assertEquals(-4, inv2.getNumerator());
        assertEquals(3, inv2.getDenominator());
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testInvertZero() {
        Fraction.ZERO.invert();
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testInvertMinNumerator() {
        Fraction.getFraction(Integer.MIN_VALUE, 1).invert();
    }

    @Test(timeout = 4000)
    public void testNegate() {
        Fraction f1 = Fraction.getFraction(3, 4);
        Fraction neg1 = f1.negate();
        assertEquals(-3, neg1.getNumerator());
        assertEquals(4, neg1.getDenominator());

        Fraction f2 = Fraction.getFraction(-3, 4);
        Fraction neg2 = f2.negate();
        assertEquals(3, neg2.getNumerator());
        assertEquals(4, neg2.getDenominator());
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testNegateMinNumerator() {
        Fraction.getFraction(Integer.MIN_VALUE, 1).negate();
    }

    @Test(timeout = 4000)
    public void testAbs() {
        Fraction pos = Fraction.getFraction(3, 4);
        assertSame(pos, pos.abs());

        Fraction neg = Fraction.getFraction(-3, 4);
        Fraction absNeg = neg.abs();
        assertEquals(3, absNeg.getNumerator());
        assertEquals(4, absNeg.getDenominator());
    }

    @Test(timeout = 4000)
    public void testPow() {
        Fraction f = Fraction.getFraction(2, 3);
        assertSame(f, f.pow(1));
        assertSame(Fraction.ONE, f.pow(0));

        Fraction pow2 = f.pow(2);
        assertEquals(4, pow2.getNumerator());
        assertEquals(9, pow2.getDenominator());

        Fraction pow3 = f.pow(3);
        assertEquals(8, pow3.getNumerator());
        assertEquals(27, pow3.getDenominator());

        Fraction powNeg1 = f.pow(-1);
        assertEquals(3, powNeg1.getNumerator());
        assertEquals(2, powNeg1.getDenominator());

        Fraction powNeg2 = f.pow(-2);
        assertEquals(9, powNeg2.getNumerator());
        assertEquals(4, powNeg2.getDenominator());
    }

    @Test(timeout = 4000)
    public void testPowMinInteger() {
        Fraction one = Fraction.ONE;
        Fraction res = one.pow(Integer.MIN_VALUE);
        assertEquals(1, res.getNumerator());
        assertEquals(1, res.getDenominator());
    }

    // =========================================================================
    // Partition D: Arithmetic Operations (add, subtract, multiplyBy, divideBy)
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddAndSubtractIdentity() {
        Fraction f = Fraction.getFraction(3, 5);
        assertSame(f, f.add(Fraction.ZERO));
        assertSame(f, Fraction.ZERO.add(f));

        assertSame(f, f.subtract(Fraction.ZERO));
        Fraction subFromZero = Fraction.ZERO.subtract(f);
        assertEquals(-3, subFromZero.getNumerator());
        assertEquals(5, subFromZero.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddAndSubtractCoprimeDenominators() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 3);

        Fraction sum = f1.add(f2);
        assertEquals(5, sum.getNumerator());
        assertEquals(6, sum.getDenominator());

        Fraction diff = f1.subtract(f2);
        assertEquals(1, diff.getNumerator());
        assertEquals(6, diff.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddAndSubtractSharedFactorDenominators() {
        Fraction f1 = Fraction.getFraction(1, 4);
        Fraction f2 = Fraction.getFraction(3, 4);

        Fraction sum = f1.add(f2);
        assertEquals(1, sum.getNumerator());
        assertEquals(1, sum.getDenominator());

        Fraction diff = f1.subtract(f2);
        assertEquals(-1, diff.getNumerator());
        assertEquals(2, diff.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddNull() {
        Fraction.ONE.add(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSubtractNull() {
        Fraction.ONE.subtract(null);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testAddOverflow() {
        Fraction f1 = Fraction.getFraction(Integer.MAX_VALUE - 1, 1);
        Fraction f2 = Fraction.getFraction(2, 1);
        f1.add(f2);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testSubtractOverflow() {
        Fraction f1 = Fraction.getFraction(Integer.MIN_VALUE + 1, 1);
        Fraction f2 = Fraction.getFraction(2, 1);
        f1.subtract(f2);
    }

    @Test(timeout = 4000)
    public void testMultiplyBy() {
        Fraction f1 = Fraction.getFraction(2, 3);
        Fraction f2 = Fraction.getFraction(3, 4);
        Fraction result = f1.multiplyBy(f2);
        assertEquals(1, result.getNumerator());
        assertEquals(2, result.getDenominator());

        assertSame(Fraction.ZERO, f1.multiplyBy(Fraction.ZERO));
        assertSame(Fraction.ZERO, Fraction.ZERO.multiplyBy(f1));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMultiplyByNull() {
        Fraction.ONE.multiplyBy(null);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testMultiplyByOverflow() {
        Fraction f1 = Fraction.getFraction(Integer.MAX_VALUE, 2);
        Fraction f2 = Fraction.getFraction(3, 1);
        f1.multiplyBy(f2);
    }

    @Test(timeout = 4000)
    public void testDivideBy() {
        Fraction f1 = Fraction.getFraction(2, 3);
        Fraction f2 = Fraction.getFraction(4, 5);
        Fraction result = f1.divideBy(f2);
        assertEquals(5, result.getNumerator());
        assertEquals(6, result.getDenominator());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDivideByNull() {
        Fraction.ONE.divideBy(null);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testDivideByZero() {
        Fraction.ONE.divideBy(Fraction.ZERO);
    }

    // =========================================================================
    // Partition E: Object Contracts (equals, hashCode, compareTo)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        Fraction f1 = Fraction.getFraction(2, 4);
        Fraction f2 = Fraction.getFraction(2, 4);
        Fraction f3 = Fraction.getFraction(1, 2);

        assertTrue(f1.equals(f1));
        assertTrue(f1.equals(f2));
        assertFalse(f1.equals(f3));
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("2/4"));

        assertEquals(f1.hashCode(), f2.hashCode());
        // Verify caching idempotency
        assertEquals(f1.hashCode(), f1.hashCode());
    }

    @Test(timeout = 4000)
    public void testCompareTo() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(2, 4);
        Fraction f3 = Fraction.getFraction(1, 3);
        Fraction f4 = Fraction.getFraction(3, 4);

        assertEquals(0, f1.compareTo(f1));
        assertEquals(0, f1.compareTo(f2));
        assertEquals(1, f1.compareTo(f3));
        assertEquals(-1, f1.compareTo(f4));
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testCompareToNull() {
        Fraction.ONE.compareTo(null);
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testCompareToNonFraction() {
        Fraction.ONE.compareTo("1");
    }

    // =========================================================================
    // Partition F: Static Constants Integrity Check
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstants() {
        assertEquals(0, Fraction.ZERO.getNumerator());
        assertEquals(1, Fraction.ZERO.getDenominator());

        assertEquals(1, Fraction.ONE.getNumerator());
        assertEquals(1, Fraction.ONE.getDenominator());

        assertEquals(1, Fraction.ONE_HALF.getNumerator());
        assertEquals(2, Fraction.ONE_HALF.getDenominator());

        assertEquals(1, Fraction.ONE_THIRD.getNumerator());
        assertEquals(3, Fraction.ONE_THIRD.getDenominator());

        assertEquals(2, Fraction.TWO_THIRDS.getNumerator());
        assertEquals(3, Fraction.TWO_THIRDS.getDenominator());

        assertEquals(1, Fraction.ONE_QUARTER.getNumerator());
        assertEquals(4, Fraction.ONE_QUARTER.getDenominator());

        assertEquals(2, Fraction.TWO_QUARTERS.getNumerator());
        assertEquals(4, Fraction.TWO_QUARTERS.getDenominator());

        assertEquals(3, Fraction