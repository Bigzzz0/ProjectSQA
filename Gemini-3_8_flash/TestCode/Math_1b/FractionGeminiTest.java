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
 * ====================================================================================================
 * Target Class: org.apache.commons.math3.fraction.Fraction
 * Benchmark: Apache Commons Math 3 (Defects4J Math-1 defect zone)
 *
 * 1. DEFECT INVESTIGATION (Math-1 / testDigitLimitConstructor):
 *    - In the private constructor Fraction(double value, double epsilon, int maxDenominator, int maxIterations),
 *      when called via Fraction(double value, int maxDenominator), epsilon is set to 0.
 *    - If a continued fraction convergent has denominator q2 that overflows Integer.MAX_VALUE in the next
 *      iteration, the implementation checks:
 *          if ((FastMath.abs(p2) > overflow) || (FastMath.abs(q2) > overflow))
 *      and unconditionally throws FractionConversionException.
 *    - In maxDenominator mode, when q1 < maxDenominator, the loop should break and return the previous
 *      convergent p1/q1 (as explicitly noted in the source comment), instead of throwing an overflow exception.
 *    - Specific failing test vector: value = 2499999794.0 / 4999999587.0 (~0.4999999999) with maxDenominator = 10
 *      reproduces "Overflow trying to convert 0.5 to fraction (2,499,999,794/4,999,999,587)".
 *
 * 2. BRANCH & CONDITION COVERAGE PATHS:
 *    - Constructors:
 *      * double -> default epsilon, maxIterations.
 *      * double, epsilon, maxIterations.
 *      * double, maxDenominator.
 *      * private double, epsilon, maxDenominator, maxIterations:
 *        - FastMath.abs(a0) > overflow (throws FractionConversionException)
 *        - FastMath.abs(a0 - value) < epsilon (almost-integer short-circuit)
 *        - (FastMath.abs(p2) > overflow) || (FastMath.abs(q2) > overflow) (overflow branch)
 *        - n < maxIterations && FastMath.abs(convergent - value) > epsilon && q2 < maxDenominator (iteration condition)
 *        - n >= maxIterations (throws FractionConversionException)
 *        - q2 < maxDenominator vs q2 >= maxDenominator (final fraction selection)
 *      * int -> int / 1.
 *      * int, int:
 *        - den == 0 (MathArithmeticException)
 *        - den < 0: num == Integer.MIN_VALUE || den == Integer.MIN_VALUE (MathArithmeticException)
 *        - den < 0: sign transposition
 *        - gcd(num, den) reduction
 *    - Arithmetic & Functional Methods:
 *      * abs: numerator >= 0 vs < 0
 *      * compareTo: <, >, == with cross-multiplication long promotion
 *      * doubleValue, floatValue, intValue, longValue conversions
 *      * equals & hashCode: identity, null, non-Fraction, field equality
 *      * negate: normal vs numerator == Integer.MIN_VALUE (MathArithmeticException)
 *      * reciprocal: normal vs numerator == 0 (MathArithmeticException)
 *      * add / subtract (addSub):
 *        - null argument check (NullArgumentException)
 *        - zero identity branches (numerator == 0, fraction.numerator == 0)
 *        - d1 == 1 (direct mulAndCheck/addAndCheck/subAndCheck)
 *        - d1 != 1 (BigInteger precision, tmodd1 == 0 vs != 0, w.bitLength() > 31 overflow check)
 *      * add(int), subtract(int), multiply(int), divide(int)
 *      * multiply(Fraction): null check, zero checks, gcd checks, mulAndCheck
 *      * divide(Fraction): null check, division by zero check, reciprocal multiplication
 *      * percentageValue: 100 * doubleValue()
 *      * getReducedFraction:
 *        - den == 0 (MathArithmeticException)
 *        - num == 0 (normalize to ZERO)
 *        - den == Integer.MIN_VALUE && (num & 1) == 0 (2^k bit shift reduction)
 *        - den < 0 with MIN_VALUE overflow checks
 *        - sign flip and gcd reduction
 *      * toString: den == 1 vs num == 0 vs general "num / den"
 *      * getField: singleton FractionField
 *      * Serializable contract integrity
 * ====================================================================================================
 */
public class FractionGeminiTest {

    // ------------------------------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ------------------------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstants() {
        assertEquals(2, Fraction.TWO.getNumerator());
        assertEquals(1, Fraction.TWO.getDenominator());

        assertEquals(1, Fraction.ONE.getNumerator());
        assertEquals(1, Fraction.ONE.getDenominator());

        assertEquals(0, Fraction.ZERO.getNumerator());
        assertEquals(1, Fraction.ZERO.getDenominator());

        assertEquals(-1, Fraction.MINUS_ONE.getNumerator());
        assertEquals(1, Fraction.MINUS_ONE.getDenominator());

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

        assertEquals(2, Fraction