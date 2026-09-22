package org.apache.commons.math3.fraction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * =====================================================================================================
 * Method / Area                 Branch / Condition Tested                          Targeted Defect / Edge
 * -----------------------------------------------------------------------------------------------------
 * percentageValue()             numerator * 100 integer overflow                   MATH-835
 * Fraction(double, ...)         FastMath.abs(a0 - value) < epsilon                 Exact integer shortcut
 * Fraction(double, ...)         a0 > Integer.MAX_VALUE                             FractionConversionException
 * Fraction(double, ...)         Convergents (p2, q2) > Integer.MAX_VALUE           FractionConversionException
 * Fraction(double, ...)         n >= maxIterations without convergence             FractionConversionException
 * Fraction(double, int)         q2 < maxDenominator vs q2 >= maxDenominator        Continued fraction stop
 * Fraction(int, int)            den == 0                                           Zero denominator exception
 * Fraction(int, int)            den < 0 with num/den == Integer.MIN_VALUE          Overflow guard exception
 * Fraction(int, int)            Sign normalization & GCD reduction                 Reduced canonical form
 * abs()                         numerator >= 0 vs numerator < 0                    Identity vs negate()
 * compareTo(Fraction)           Cross-multiplication comparison (<, ==, >)         Long arithmetic precision
 * equals(Object) / hashCode()   this == other, !(other instanceof Fraction), diff  Contract consistency
 * negate()                      numerator == Integer.MIN_VALUE                     MathArithmeticException
 * reciprocal()                  numerator == 0                                     Zero denominator
 * add / subtract(Fraction)      fraction == null                                   NullArgumentException
 * add / subtract(Fraction)      numerator == 0 or fraction.numerator == 0          Additive identities
 * add / subtract(Fraction)      gcd(d1, d2) == 1 vs gcd(d1, d2) > 1                Knuth algorithm paths
 * add / subtract(Fraction)      BigInteger w.bitLength() > 31                      Numerator overflow guard
 * multiply / divide(Fraction)   null, zero multiplicand/divisor                    Guard paths & identities
 * multiply / divide(Fraction)   Integer arithmetic overflow                        mulAndCheck guards
 * getReducedFraction(int, int)  den == 0, num == 0                                 Zero handling
 * getReducedFraction(int, int)  den == MIN_VALUE & (num & 1) == 0 vs odd num       Sub-power-of-2 branch
 * getReducedFraction(int, int)  num/den == Integer.MIN_VALUE overflow              Overflow guard
 * toString()                    den == 1 ("n"), num == 0 ("0"), general ("n / d")  Formatting branches
 * =====================================================================================================
 */
public class FractionGeminiTest {

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testBasicConstructorAndAccessors() {
        Fraction f = new Fraction(3, 4);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
        assertEquals(0.75, f.doubleValue(), 1.0e-10);
        assertEquals(0.75f, f.floatValue(), 1.0e-5f);
        assertEquals(0, f.intValue());
        assertEquals(0L, f.longValue());
    }

    @Test(timeout = 4000)
    public void testIntegerConstructor() {
        Fraction f = new Fraction(7);
        assertEquals(7, f.getNumerator());
        assertEquals(1, f.getDenominator());
        assertEquals(7.0, f.doubleValue(), 1.0e-10);
        assertEquals(7, f.intValue());
        assertEquals(7L, f.longValue());
    }

    @Test(timeout = 4000)
    public void testStaticConstants() {
        assertEquals(0, Fraction.ZERO.getNumerator());
        assertEquals(1, Fraction.ZERO.getDenominator());

        assertEquals(1, Fraction.ONE.getNumerator());
        assertEquals(1, Fraction.ONE.getDenominator());

        assertEquals(-1, Fraction.MINUS_ONE.getNumerator());
        assertEquals(1, Fraction.MINUS_ONE.getDenominator());

        assertEquals(2, Fraction.TWO.getNumerator());
        assertEquals(1, Fraction.TWO.getDenominator());

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

        assertEquals(3, Fraction.THREE_QUARTERS.getNumerator());
        assertEquals(4, Fraction.THREE_QUARTERS.getDenominator());

        assertEquals(1, Fraction.ONE_FIFTH.getNumerator());
        assertEquals(5, Fraction.ONE_FIFTH.getDenominator());

        assertEquals(2