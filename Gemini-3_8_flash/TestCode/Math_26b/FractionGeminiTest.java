package org.apache.commons.math3.fraction;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NullArgumentException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================
 * 1. DEFECT-TARGETED ZONE (Defects4J Ground Truth):
 *    - FractionTest::testIntegerOverflow: Continued fraction evaluation
 *      for certain double values (e.g., 0.75000000001455192) causes long
 *      overflow in (a1 * p1) + p0 and (a1 * q1) + q0. The checks (p2 > overflow)
 *      failed to prevent or catch wrapped negative values when q2 >= maxDenominator,
 *      resulting in missed FractionConversionException and test failure.
 *
 * 2. BRANCH & BOUNDARY MATRIX:
 *    - Fraction(double, double, int, int):
 *      * a0 > overflow branch -> FractionConversionException.
 *      * FastMath.abs(a0 - value) < epsilon branch -> integer direct return.
 *      * (p2 > overflow || q2 > overflow) branch -> FractionConversionException.
 *      * n >= maxIterations branch -> FractionConversionException.
 *      * q2 < maxDenominator vs. q2 >= maxDenominator convergent selection.
 *    - Fraction(int, int):
 *      * den == 0 -> MathArithmeticException.
 *      * den < 0 with num or den == Integer.MIN_VALUE -> MathArithmeticException.
 *      * den < 0 normal reduction and sign flipping.
 *      * gcd reduction where d > 1.
 *    - abs(), negate():
 *      * num == Integer.MIN_VALUE -> MathArithmeticException on negate().
 *      * num >= 0 returns this instance.
 *    - compareTo(Fraction):
 *      * Less than (-1), greater than (+1), equal (0).
 *      * Wide 64-bit cross-multiplication verification.
 *    - add(Fraction), subtract(Fraction) (Knuth 4.5.1 via addSub):
 *      * fraction == null -> NullArgumentException.
 *      * num == 0 identity handling for add and subtract (negate path).
 *      * d1 == 1 coprime denominator path + add/sub/mul overflow guards.
 *      * d1 > 1 common factor denominator path:
 *        - tmodd1 == 0 vs. tmodd1 != 0 branches.
 *        - w.bitLength() > 31 numerator overflow branch.
 *        - mulAndCheck denominator overflow branch.
 *    - multiply(Fraction), divide(Fraction):
 *      * fraction == null -> NullArgumentException.
 *      * fraction.numerator == 0 -> divide by zero MathArithmeticException.
 *      * zero factors -> returns Fraction.ZERO.
 *      * mulAndCheck overflow handling.
 *    - getReducedFraction(int, int):
 *      * den == 0 -> MathArithmeticException.
 *      * num == 0 -> ZERO normalization.
 *      * den == Integer.MIN_VALUE and even num -> power-of-two reduction.
 *      * den == Integer.MIN_VALUE and odd num -> MathArithmeticException.
 *      * num == Integer.MIN_VALUE and den < 0 -> MathArithmeticException.
 *    - toString():
 *      * den == 1 ("num") vs. num == 0 ("0") vs. standard ("num / den").
 *    - equals(Object) & hashCode():
 *      * Identity, null, foreign type, different num/den, equivalent value.
 *    - Java Serialization round-trip integrity.
 * =========================================================================
 */
public class FractionGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testIntegerOverflow() {
        checkOverflow(0.75000000001455192);
        checkOverflow(0.68262797000000004);
        checkOverflow(0.9999999999);
        checkOverflow(0.8413447461);
        checkOverflow(0.999999999999);
    }

    private void checkOverflow(double a) {
        try {
            new Fraction(a, 1.0e-12, 1000);
            fail("an exception should have been thrown");
        } catch (FractionConversionException ex) {
            // expected defect-revealing path
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstantsAndBasicAccessors() {
        assertEquals(0, Fraction.ZERO.getNumerator());
        assertEquals(1, Fraction.ZERO.getDenominator());

        assertEquals(1, Fraction.ONE.getNumerator());
        assertEquals(1, Fraction.ONE.getDenominator());

        assertEquals(2, Fraction.TWO.getNumerator());
        assertEquals(1, Fraction.TWO.getDenominator());

        assertEquals(-1, Fraction.MINUS_ONE.getNumerator());
        assertEquals(1, Fraction.MINUS_ONE.getDenominator());

        assertEquals(new Fraction(1, 2), Fraction.ONE_HALF);
        assertEquals(new Fraction(1, 3), Fraction.ONE_THIRD);
        assertEquals(new Fraction(2, 3), Fraction.TWO_THIRDS);
        assertEquals(new Fraction(1, 4), Fraction.ONE_QUARTER);
        assertEquals(new Fraction(2, 4), Fraction.TWO_QUARTERS);
        assertEquals(new Fraction(3, 4), Fraction.THREE_QUARTERS);
        assertEquals(new Fraction(1, 5), Fraction.ONE_FIFTH);
        assertEquals(new Fraction(2, 5), Fraction.TWO_FIFTHS);
        assertEquals(new Fraction(3, 5), Fraction.THREE_FIFTHS);
        assertEquals(new Fraction(4, 5), Fraction.FOUR_FIFTHS);
    }

    @Test(timeout = 4000)
    public void testSingleIntConstructor() {
        Fraction f = new Fraction(42);
        assertEquals(42, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testDoubleConstructorNormal() {
        Fraction f = new Fraction(0.5);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());

        Fraction neg = new Fraction(-0.75);
        assertEquals(-3, neg.getNumerator());
        assertEquals(4, neg.getDenominator());
    }

    @Test(timeout = 4000)
    public void testDoubleConstructorMaxDenominatorBranch() {
        // q2 < maxDenominator branch
        Fraction f1 = new Fraction(0.4, 9);
        assertEquals(2, f1.getNumerator());
        assertEquals(5, f1.getDenominator());

        // q2 >= maxDenominator branch
        Fraction f2 = new Fraction(0.6152, 2);
        assertTrue(f2.getDenominator() <= 2);
    }

    @Test(timeout = 4000)
    public void testPrimitiveConversions() {
        Fraction f = new Fraction(7, 2); // 3.5
        assertEquals(3.5, f.doubleValue(), 1.0e-10);
        assertEquals(3.5f, f.floatValue(), 1.0e-5f);
        assertEquals(3, f.intValue());
        assertEquals(3L, f.longValue());
        assertEquals(350.0, f.percentageValue(), 1.0e-10);
    }

    @Test(timeout = 4000)
    public void testAbsAndNegate() {
        Fraction pos = new Fraction(3, 4);
        Fraction neg = new Fraction(-3, 4);

        assertSame(pos, pos.abs());
        assertEquals(pos, neg.abs());

        assertEquals(neg, pos.negate());
        assertEquals(pos, neg.negate());
    }

    @Test(timeout = 4000)
    public void testReciprocal() {
        Fraction f = new Fraction(3, 4);
        Fraction rec = f.reciprocal();
        assertEquals(4, rec.getNumerator());
        assertEquals(3, rec.getDenominator());

        Fraction neg = new Fraction(-5, 7);
        Fraction negRec = neg.reciprocal();
        assertEquals(-7, negRec.getNumerator());
        assertEquals(5, negRec.getDenominator());
    }

    @Test(timeout = 4000)
    public void testArithmeticAddSubtractPrimitiveInt() {
        Fraction f = new Fraction(1, 3);
        Fraction sum = f.add(2);
        assertEquals(7, sum.getNumerator());
        assertEquals(3, sum.getDenominator());

        Fraction diff = f.subtract(2);
        assertEquals(-5, diff.getNumerator());
        assertEquals(3, diff.getDenominator());
    }

    @Test(timeout = 4000)
    public void testArithmeticMultiplyDividePrimitiveInt() {
        Fraction f = new Fraction(2, 5);
        Fraction prod = f.multiply(3);
        assertEquals(6, prod.getNumerator());
        assertEquals(5, prod.getDenominator());

        Fraction quot = f.divide(2);
        assertEquals(1, quot.getNumerator());
        assertEquals(5, quot.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddSubFractionBranches() {
        // Zero identities
        Fraction f = new Fraction(2, 7);
        assertEquals(f, Fraction.ZERO.add(f));
        assertEquals(f.negate(), Fraction.ZERO.subtract(f));
        assertEquals(f, f.add(Fraction.ZERO));
        assertEquals(f, f.subtract(Fraction.ZERO));

        // d1 == 1 path (coprime denominators 3 and 5)
        Fraction f1 = new Fraction(1, 3);
        Fraction f2 = new Fraction(1, 5);
        assertEquals(new Fraction(8, 15), f1.add(f2));
        assertEquals(new Fraction(2, 15), f1.subtract(f2));

        // d1 > 1 path, tmodd1 == 0 (6 and 6)
        Fraction a = new Fraction(1, 6);
        Fraction b = new Fraction(5, 6);
        assertEquals(Fraction.ONE, a.add(b));

        // d1 > 1 path, tmodd1 != 0 (6 and 4)
        Fraction c = new Fraction(1, 6);
        Fraction d = new Fraction(1, 4);
        assertEquals(new Fraction(5, 12), c.add(d));
        assertEquals(new Fraction(-1, 12), c.subtract(d));
    }

    @Test(timeout = 4000)
    public void testMultiplyDivideFractionBranches() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(3, 4);

        assertEquals(new Fraction(1, 2), f1.multiply(f2));
        assertEquals(new Fraction(8, 9), f1.divide(f2));

        // Zero factor
        assertEquals(Fraction.ZERO, f1.multiply(Fraction.ZERO));
        assertEquals(Fraction.ZERO, Fraction.ZERO.multiply(f1));
    }

    @Test(timeout = 4000)
    public void testCompareTo() {
        Fraction small = new Fraction(-1, 2);
        Fraction mid = new Fraction(1, 3);
        Fraction midEquivalent = new Fraction(2, 6);
        Fraction large = new Fraction(3, 4);

        assertTrue(small.compareTo(mid) < 0);
        assertTrue(large.compareTo(mid) > 0);
        assertEquals(0, mid.compareTo(midEquivalent));
    }

    @Test(timeout = 4000)
    public void testToString() {
        assertEquals("0", new Fraction(0, 5).toString());
        assertEquals("7", new Fraction(7, 1).toString());
        assertEquals("-7", new Fraction(-7, 1).toString());
        assertEquals("3 / 4", new Fraction(3, 4).toString());
        assertEquals("-3 / 4", new Fraction(-3, 4).toString());
    }

    @Test(timeout = 4000)
    public void testGetField() {
        assertNotNull(Fraction.ONE.getField());
        assertSame(FractionField.getInstance(), Fraction.ONE.getField());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testAlmostIntegerConstructor() {
        // FastMath.abs(a0 - value) < epsilon
        Fraction f = new Fraction(4.000000000001, 1.0e-5, 10);
        assertEquals(4, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorReductionSignNormalizations() {
        // Both negative -> positive
        Fraction f1 = new Fraction(-3, -4);
        assertEquals(3, f1.getNumerator());
        assertEquals(4, f1.getDenominator());

        // Negative denominator -> negative numerator
        Fraction f2 = new Fraction(3, -4);
        assertEquals(-3, f2.getNumerator());
        assertEquals(4, f2.getDenominator());

        // Reduction with gcd
        Fraction f3 = new Fraction(12, 16);
        assertEquals(3, f3.getNumerator());
        assertEquals(4, f3.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionSpecialBoundaries() {
        assertEquals(Fraction.ZERO, Fraction.getReducedFraction(0, 100));

        // 2^k / -2^31 reduction branch
        Fraction f = Fraction.getReducedFraction(2, Integer.MIN_VALUE);
        assertEquals(-1, f.getNumerator());
        assertEquals(1073741824, f.getDenominator());

        // Standard negative denominator
        Fraction f2 = Fraction.getReducedFraction(3, -6);
        assertEquals(-1, f2.getNumerator());
        assertEquals(2, f2.getDenominator());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testConstructorZeroDenominator() {
        new Fraction(1, 0);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testConstructorIntegerMinValueDenominator() {
        new Fraction(1, Integer.MIN_VALUE);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testConstructorIntegerMinValueNumeratorNegativeDenominator() {
        new Fraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = FractionConversionException.class, timeout = 4000)
    public void testDoubleConstructorInitialOverflow() {
        new Fraction(3.0e10, 1.0e-5, 100);
    }

    @Test(expected = FractionConversionException.class, timeout = 4000)
    public void testDoubleConstructorMaxIterationsReached() {
        // Irrationals like PI cannot converge in 2 iterations with strict epsilon
        new Fraction(FastMath.PI, 1.0e-15, 2);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testNegateIntegerMinValueNumerator() {
        new Fraction(Integer.MIN_VALUE, 1).negate();
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testAbsIntegerMinValueNumerator() {
        new Fraction(Integer.MIN_VALUE, 1).abs();
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testReciprocalZero() {
        Fraction.ZERO.reciprocal();
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testAddNullThrows() {
        Fraction.ONE.add(null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testSubtractNullThrows() {
        Fraction.ONE.subtract(null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testMultiplyNullThrows() {
        Fraction.ONE.multiply(null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testDivideNullThrows() {
        Fraction.ONE.divide(null);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testDivideByZeroFractionThrows() {
        Fraction.ONE.divide(Fraction.ZERO);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testDivideByZeroPrimitiveIntThrows() {
        Fraction.ONE.divide(0);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testAddOverflowD1EqualsOne() {
        // d1 == 1 coprime: Integer.MAX_VALUE - 1 with 2 and 1 with 3
        new Fraction(Integer.MAX_VALUE - 1, 2).add(new Fraction(1, 3));
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testAddOverflowNumeratorBitLengthExceeded() {
        // Triggers w.bitLength() > 31 in Knuth addSub
        Fraction f1 = new Fraction(Integer.MAX_VALUE - 1, 2);
        Fraction f2 = new Fraction(Integer.MAX_VALUE - 2, 4);
        f1.add(f2);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testAddOverflowDenominatorMulAndCheck() {
        // Common factor d1 = 2, but resulting denominator exceeds Integer.MAX_VALUE
        Fraction f1 = new Fraction(1, 100000);
        Fraction f2 = new Fraction(1, 100002);
        f1.add(f2);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testMultiplyOverflow() {
        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(2, 1);
        f1.multiply(f2);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testGetReducedFractionZeroDenominatorThrows() {
        Fraction.getReducedFraction(1, 0);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testGetReducedFractionMinValueOddNumeratorThrows() {
        Fraction.getReducedFraction(1, Integer.MIN_VALUE);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testGetReducedFractionMinValueNumeratorNegativeDenominatorThrows() {
        Fraction.getReducedFraction(Integer.MIN_VALUE, -1);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Fraction f1 = new Fraction(1, 3);
        Fraction f2 = new Fraction(2, 6);
        Fraction f3 = new Fraction(1, 4);
        Fraction f4 = new Fraction(2, 3);

        // Reflexive
        assertTrue(f1.equals(f1));

        // Symmetric & reduced equality
        assertTrue(f1.equals(f2));
        assertTrue(f2.equals(f1));
        assertEquals(f1.hashCode(), f2.hashCode());

        // Non-equality
        assertFalse(f1.equals(f3)); // different denominator
        assertFalse(f1.equals(f4)); // different numerator
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("StringLiteral"));
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        Fraction original = new Fraction(355, 113);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Fraction deserialized = (Fraction) ois.readObject();
        ois.close();

        assertEquals(original, deserialized);
        assertEquals(original.getNumerator(), deserialized.getNumerator());
        assertEquals(original.getDenominator(), deserialized.getDenominator());
    }
}