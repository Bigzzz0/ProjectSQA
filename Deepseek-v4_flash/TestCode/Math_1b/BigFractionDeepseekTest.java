package org.apache.commons.math3.fraction;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target class: org.apache.commons.math3.fraction.BigFraction
 * Defect: The digit-limit constructor (private BigFraction(double,double,int,int))
 *         throws FractionConversionException for mundane values like 0.5.
 *         Root cause: overflow threshold set to Integer.MAX_VALUE, but intermediate
 *         numerator/denominator values (computed as longs) can exceed this limit,
 *         causing premature exception. The fix should allow larger intermediate values
 *         or switch to BigInteger arithmetic.
 *
 * Branch coverage targets:
 *  - Constructor with BigInteger/BigInteger: zero numerator, negative denominator, gcd reduction, null checks.
 *  - Constructor with double: NaN, Infinity, normal values (including subnormal).
 *  - Constructor with double,epsilon,maxIterations: exact match, epsilon convergence, overflow path.
 *  - Constructor with double,maxDenominator: exact match, maxDenominator limit.
 *  - Arithmetic methods: add/subtract/multiply/divide with various types (null, zero, negative).
 *  - pow(int/long/BigInteger/double): negative exponent, zero.
 *  - equals/hashCode: identity, reduced vs unreduced, null, other class.
 *  - compareTo: order, equality.
 *  - getters, toString, percentageValue, doubleValue, floatValue, intValue, longValue.
 *  - reduce, abs, negate, reciprocal.
 *  - Exception paths: NullArgumentException, ZeroException, MathArithmeticException, FractionConversionException.
 */
public class BigFractionDeepseekTest {

    // ---------- Partition A: Core Functional Logic & State Transitions ----------

    @Test(timeout = 4000)
    public void testNumeratorDenominatorSimple() {
        BigFraction f = new BigFraction(3, 5);
        assertEquals(BigInteger.valueOf(3), f.getNumerator());
        assertEquals(BigInteger.valueOf(5), f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testNumeratorDenominatorReduction() {
        // 6/8 -> reduces to 3/4
        BigFraction f = new BigFraction(6, 8);
        assertEquals(BigInteger.valueOf(3), f.getNumerator());
        assertEquals(BigInteger.valueOf(4), f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testNumeratorDenominatorNegativeDenominator() {
        // 3/-5 -> -3/5
        BigFraction f = new BigFraction(3, -5);
        assertEquals(BigInteger.valueOf(-3), f.getNumerator());
        assertEquals(BigInteger.valueOf(5), f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testNumeratorDenominatorBothNegative() {
        // -3/-5 -> 3/5
        BigFraction f = new BigFraction(-3, -5);
        assertEquals(BigInteger.valueOf(3), f.getNumerator());
        assertEquals(BigInteger.valueOf(5), f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testNumeratorDenominatorZeroNumerator() {
        BigFraction f = new BigFraction(0, 5);
        assertEquals(BigInteger.ZERO, f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testBigIntegerConstructor() {
        BigFraction f = new BigFraction(BigInteger.valueOf(7));
        assertEquals(BigInteger.valueOf(7), f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testIntConstructor() {
        BigFraction f = new BigFraction(7);
        assertEquals(BigInteger.valueOf(7), f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testLongConstructor() {
        BigFraction f = new BigFraction(7L);
        assertEquals(BigInteger.valueOf(7), f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetReducedFraction() {
        BigFraction f = BigFraction.getReducedFraction(4, 6);
        assertEquals(BigInteger.valueOf(2), f.getNumerator());
        assertEquals(BigInteger.valueOf(3), f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testGetReducedFractionZero() {
        BigFraction f = BigFraction.getReducedFraction(0, 5);
        assertSame(BigFraction.ZERO, f);
    }

    @Test(timeout = 4000)
    public void testAbsPositive() {
        BigFraction f = new BigFraction(3, 4);
        assertSame(f, f.abs());
    }

    @Test(timeout = 4000)
    public void testAbsNegative() {
        BigFraction f = new BigFraction(-3, 4);
        BigFraction abs = f.abs();
        assertEquals(BigInteger.valueOf(3), abs.getNumerator());
        assertEquals(BigInteger.valueOf(4), abs.getDenominator());
    }

    @Test(timeout = 4000)
    public void testNegate() {
        BigFraction f = new BigFraction(3, 4);
        BigFraction neg = f.negate();
        assertEquals(BigInteger.valueOf(-3), neg.getNumerator());
        assertEquals(BigInteger.valueOf(4), neg.getDenominator());
    }

    @Test(timeout = 4000)
    public void testReciprocal() {
        BigFraction f = new BigFraction(3, 4);
        BigFraction r = f.reciprocal();
        assertEquals(BigInteger.valueOf(4), r.getNumerator());
        assertEquals(BigInteger.valueOf(3), r.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddBigFractionSameDenominator() {
        BigFraction a = new BigFraction(1, 5);
        BigFraction b = new BigFraction(2, 5);
        BigFraction sum = a.add(b);
        assertEquals(BigInteger.valueOf(3), sum.getNumerator());
        assertEquals(BigInteger.valueOf(5), sum.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddBigFractionDifferentDenominator() {
        BigFraction a = new BigFraction(1, 3);
        BigFraction b = new BigFraction(1, 6);
        BigFraction sum = a.add(b);
        assertEquals(BigInteger.valueOf(1), sum.getNumerator());
        assertEquals(BigInteger.valueOf(2), sum.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddBigFractionIdentity() {
        BigFraction a = new BigFraction(2, 3);
        BigFraction sum = a.add(BigFraction.ZERO);
        assertSame(a, sum);
    }

    @Test(timeout = 4000)
    public void testAddBigInteger() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction sum = a.add(BigInteger.valueOf(3));
        assertEquals(BigInteger.valueOf(7), sum.getNumerator());
        assertEquals(BigInteger.valueOf(2), sum.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddInt() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction sum = a.add(3);
        assertEquals(BigInteger.valueOf(7), sum.getNumerator());
        assertEquals(BigInteger.valueOf(2), sum.getDenominator());
    }

    @Test(timeout = 4000)
    public void testAddLong() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction sum = a.add(3L);
        assertEquals(BigInteger.valueOf(7), sum.getNumerator());
        assertEquals(BigInteger.valueOf(2), sum.getDenominator());
    }

    @Test(timeout = 4000)
    public void testSubtractBigFractionSameDenominator() {
        BigFraction a = new BigFraction(3, 5);
        BigFraction b = new BigFraction(1, 5);
        BigFraction diff = a.subtract(b);
        assertEquals(BigInteger.valueOf(2), diff.getNumerator());
        assertEquals(BigInteger.valueOf(5), diff.getDenominator());
    }

    @Test(timeout = 4000)
    public void testSubtractBigFractionDifferentDenominator() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(1, 3);
        BigFraction diff = a.subtract(b);
        assertEquals(BigInteger.valueOf(1), diff.getNumerator());
        assertEquals(BigInteger.valueOf(6), diff.getDenominator());
    }

    @Test(timeout = 4000)
    public void testSubtractBigFractionIdentity() {
        BigFraction a = new BigFraction(2, 3);
        BigFraction diff = a.subtract(BigFraction.ZERO);
        assertSame(a, diff);
    }

    @Test(timeout = 4000)
    public void testSubtractBigInteger() {
        BigFraction a = new BigFraction(7, 2);
        BigFraction diff = a.subtract(BigInteger.valueOf(3));
        assertEquals(BigInteger.valueOf(1), diff.getNumerator());
        assertEquals(BigInteger.valueOf(2), diff.getDenominator());
    }

    @Test(timeout = 4000)
    public void testSubtractInt() {
        BigFraction a = new BigFraction(7, 2);
        BigFraction diff = a.subtract(3);
        assertEquals(BigInteger.valueOf(1), diff.getNumerator());
        assertEquals(BigInteger.valueOf(2), diff.getDenominator());
    }

    @Test(timeout = 4000)
    public void testSubtractLong() {
        BigFraction a = new BigFraction(7, 2);
        BigFraction diff = a.subtract(3L);
        assertEquals(BigInteger.valueOf(1), diff.getNumerator());
        assertEquals(BigInteger.valueOf(2), diff.getDenominator());
    }

    @Test(timeout = 4000)
    public void testMultiplyBigFraction() {
        BigFraction a = new BigFraction(2, 3);
        BigFraction b = new BigFraction(3, 4);
        BigFraction prod = a.multiply(b);
        assertEquals(BigInteger.valueOf(1), prod.getNumerator());
        assertEquals(BigInteger.valueOf(2), prod.getDenominator());
    }

    @Test(timeout = 4000)
    public void testMultiplyBigFractionZero() {
        BigFraction a = new BigFraction(2, 3);
        BigFraction prod = a.multiply(BigFraction.ZERO);
        assertSame(BigFraction.ZERO, prod);
    }

    @Test(timeout = 4000)
    public void testMultiplyBigInteger() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction prod = a.multiply(BigInteger.valueOf(3));
        assertEquals(BigInteger.valueOf(3), prod.getNumerator());
        assertEquals(BigInteger.valueOf(2), prod.getDenominator());
    }

    @Test(timeout = 4000)
    public void testMultiplyInt() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction prod = a.multiply(3);
        assertEquals(BigInteger.valueOf(3), prod.getNumerator());
        assertEquals(BigInteger.valueOf(2), prod.getDenominator());
    }

    @Test(timeout = 4000)
    public void testMultiplyLong() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction prod = a.multiply(3L);
        assertEquals(BigInteger.valueOf(3), prod.getNumerator());
        assertEquals(BigInteger.valueOf(2), prod.getDenominator());
    }

    @Test(timeout = 4000)
    public void testDivideBigFraction() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 3);
        BigFraction quot = a.divide(b);
        assertEquals(BigInteger.valueOf(3), quot.getNumerator());
        assertEquals(BigInteger.valueOf(4), quot.getDenominator());
    }

    @Test(timeout = 4000)
    public void testDivideBigInteger() {
        BigFraction a = new BigFraction(3, 4);
        BigFraction quot = a.divide(BigInteger.valueOf(2));
        assertEquals(BigInteger.valueOf(3), quot.getNumerator());
        assertEquals(BigInteger.valueOf(8), quot.getDenominator());
    }

    @Test(timeout = 4000)
    public void testDivideInt() {
        BigFraction a = new BigFraction(3, 4);
        BigFraction quot = a.divide(2);
        assertEquals(BigInteger.valueOf(3), quot.getNumerator());
        assertEquals(BigInteger.valueOf(8), quot.getDenominator());
    }

    @Test(timeout = 4000)
    public void testDivideLong() {
        BigFraction a = new BigFraction(3, 4);
        BigFraction quot = a.divide(2L);
        assertEquals(BigInteger.valueOf(3), quot.getNumerator());
        assertEquals(BigInteger.valueOf(8), quot.getDenominator());
    }

    @Test(timeout = 4000)
    public void testPowIntPositive() {
        BigFraction a = new BigFraction(2, 3);
        BigFraction p = a.pow(3);
        assertEquals(BigInteger.valueOf(8), p.getNumerator());
        assertEquals(BigInteger.valueOf(27), p.getDenominator());
    }

    @Test(timeout = 4000)
    public void testPowIntNegative() {
        BigFraction a = new BigFraction(2, 3);
        BigFraction p = a.pow(-2);
        assertEquals(BigInteger.valueOf(9), p.getNumerator());
        assertEquals(BigInteger.valueOf(4), p.getDenominator());
    }

    @Test(timeout = 4000)
    public void testPowIntZero() {
        BigFraction a = new BigFraction(2, 3);
        BigFraction p = a.pow(0);
        assertEquals(BigInteger.ONE, p.getNumerator());
        assertEquals(BigInteger.ONE, p.getDenominator());
    }

    @Test(timeout = 4000)
    public void testPowLongPositive() {
        BigFraction a = new BigFraction(2, 3);
        BigFraction p = a.pow(3L);
        assertEquals(BigInteger.valueOf(8), p.getNumerator());
        assertEquals(BigInteger.valueOf(27), p.getDenominator());
    }

    @Test(timeout = 4000)
    public void testPowLongNegative() {
        BigFraction a = new BigFraction(2, 3);
        BigFraction p = a.pow(-2L);
        assertEquals(BigInteger.valueOf(9), p.getNumerator());
        assertEquals(BigInteger.valueOf(4), p.getDenominator());
    }

    @Test(timeout = 4000)
    public void testPowBigIntegerPositive() {
        BigFraction a = new BigFraction(2, 3);
        BigFraction p = a.pow(BigInteger.valueOf(3));
        assertEquals(BigInteger.valueOf(8), p.getNumerator());
        assertEquals(BigInteger.valueOf(27), p.getDenominator());
    }

    @Test(timeout = 4000)
    public void testPowBigIntegerNegative() {
        BigFraction a = new BigFraction(2, 3);
        BigFraction p = a.pow(BigInteger.valueOf(-2));
        assertEquals(BigInteger.valueOf(9), p.getNumerator());
        assertEquals(BigInteger.valueOf(4), p.getDenominator());
    }

    @Test(timeout = 4000)
    public void testPowDouble() {
        BigFraction a = new BigFraction(1, 2);
        double d = a.pow(2.0);
        assertEquals(0.25, d, 1e-15);
    }

    @Test(timeout = 4000)
    public void testReduce() {
        BigFraction f = new BigFraction(6, 8);
        BigFraction r = f.reduce();
        assertEquals(BigInteger.valueOf(3), r.getNumerator());
        assertEquals(BigInteger.valueOf(4), r.getDenominator());
    }

    @Test(timeout = 4000)
    public void testToStringInt() {
        BigFraction f = new BigFraction(3, 1);
        assertEquals("3", f.toString());
    }

    @Test(timeout = 4000)
    public void testToStringZero() {
        BigFraction f = BigFraction.ZERO;
        assertEquals("0", f.toString());
    }

    @Test(timeout = 4000)
    public void testToStringFraction() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals("1 / 2", f.toString());
    }

    @Test(timeout = 4000)
    public void testDoubleValueNormal() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals(0.5, f.doubleValue(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDoubleValueLarge() {
        // Trigger overflow path in doubleValue
        BigFraction f = new BigFraction(BigInteger.valueOf(1).shiftLeft(2000), BigInteger.ONE);
        double d = f.doubleValue();
        assertTrue(Double.isFinite(d));
        assertTrue(d > 0);
    }

    @Test(timeout = 4000)
    public void testFloatValueNormal() {
        BigFraction f = new BigFraction(1, 2);
        assertEquals(0.5f, f.floatValue(), 1e-15f);
    }

    @Test(timeout = 4000)
    public void testFloatValueLarge() {
        BigFraction f = new BigFraction(BigInteger.valueOf(1).shiftLeft(2000), BigInteger.ONE);
        float fl = f.floatValue();
        assertTrue(Float.isFinite(fl));
        assertTrue(fl > 0);
    }

    @Test(timeout = 4000)
    public void testIntValue() {
        BigFraction f = new BigFraction(7, 3);
        assertEquals(2, f.intValue());
    }

    @Test(timeout = 4000)
    public void testLongValue() {
        BigFraction f = new BigFraction(7, 3);
        assertEquals(2L, f.longValue());
    }

    @Test(timeout = 4000)
    public void testPercentageValue() {
        BigFraction f = new BigFraction(1, 4);
        assertEquals(25.0, f.percentageValue(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testBigDecimalValue() {
        BigFraction f = new BigFraction(1, 4);
        assertEquals(new BigDecimal("0.25"), f.bigDecimalValue());
    }

    @Test(timeout = 4000)
    public void testBigDecimalValueRoundingMode() {
        BigFraction f = new BigFraction(1, 3);
        BigDecimal bd = f.bigDecimalValue(RoundingMode.DOWN);
        assertEquals(new BigDecimal("0.3333333333333333"), bd);
    }

    @Test(timeout = 4000)
    public void testBigDecimalValueScaleRoundingMode() {
        BigFraction f = new BigFraction(1, 3);
        BigDecimal bd = f.bigDecimalValue(5, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("0.33333"), bd);
    }

    @Test(timeout = 4000)
    public void testGetField() {
        assertSame(BigFractionField.getInstance(), new BigFraction(1, 2).getField());
    }

    // ---------- Partition B: Boundary Value Analysis (BVA) & Extremes ----------

    @Test(timeout = 4000)
    public void testConstructorDoubleZero() {
        BigFraction f = new BigFraction(0.0);
        assertEquals(BigInteger.ZERO, f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleSmallInteger() {
        BigFraction f = new BigFraction(3.0);
        assertEquals(BigInteger.valueOf(3), f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleNegative() {
        BigFraction f = new BigFraction(-0.5);
        assertTrue(f.compareTo(BigFraction.ZERO) < 0);
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleEpsilonMaxIterationsExact() {
        // 0.5 exact match
        BigFraction f = new BigFraction(0.5, 0.0, 100);
        assertEquals(BigInteger.ONE, f.getNumerator());
        assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleMaxDenominatorExact() {
        // 0.5 with maxDenominator = 1000 should give 1/2
        BigFraction f = new BigFraction(0.5, 1000);
        assertEquals(BigInteger.ONE, f.getNumerator());
        assertEquals(BigInteger.valueOf(2), f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleMaxDenominatorBoundary() {
        // edge: maxDenominator = 1, 0.5 should give 0/1 (since exact 1/2 not allowed)
        // The algorithm will stop at first convergent 0/1 because q2 >= maxDenominator
        BigFraction f = new BigFraction(0.5, 1);
        assertEquals(BigInteger.ZERO, f.getNumerator());
        assertEquals(BigInteger.ONE, f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleNaN() {
        try {
            new BigFraction(Double.NaN);
            fail("Expected MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleInfinity() {
        try {
            new BigFraction(Double.POSITIVE_INFINITY);
            fail("Expected MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            // expected
        }
        try {
            new BigFraction(Double.NEGATIVE_INFINITY);
            fail("Expected MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorBigIntegerNullNum() {
        try {
            new BigFraction((BigInteger) null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorBigIntegerNullDen() {
        try {
            new BigFraction(BigInteger.ONE, null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorBigIntegerZeroDen() {
        try {
            new BigFraction(BigInteger.ONE, BigInteger.ZERO);
            fail("Expected ZeroException");
        } catch (ZeroException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddNullBigFraction() {
        try {
            new BigFraction(1, 2).add((BigFraction) null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddNullBigInteger() {
        try {
            new BigFraction(1, 2).add((BigInteger) null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSubtractNullBigFraction() {
        try {
            new BigFraction(1, 2).subtract((BigFraction) null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSubtractNullBigInteger() {
        try {
            new BigFraction(1, 2).subtract((BigInteger) null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMultiplyNullBigFraction() {
        try {
            new BigFraction(1, 2).multiply((BigFraction) null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMultiplyNullBigInteger() {
        try {
            new BigFraction(1, 2).multiply((BigInteger) null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDivideNullBigFraction() {
        try {
            new BigFraction(1, 2).divide((BigFraction) null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDivideNullBigInteger() {
        try {
            new BigFraction(1, 2).divide((BigInteger) null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDivideBigIntegerZero() {
        try {
            new BigFraction(1, 2).divide(BigInteger.ZERO);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDivideBigFractionZero() {
        try {
            new BigFraction(1, 2).divide(BigFraction.ZERO);
            fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    // ---------- Partition C: Defect-Targeted Branch Zone (Known Failure) ----------

    /**
     * This test targets the known defect: the private digit-limit constructor
     * (called from BigFraction(double,double,int) and BigFraction(double,int))
     * throws FractionConversionException for simple values like 0.5.
     * On a fixed version, this test should pass; on the buggy version, it will
     * throw an exception and fail, thus revealing the defect.
     */
    @Test(timeout = 4000)
    public void testDigitLimitConstructorDefect() {
        // Test with epsilon=0.0 and maxIterations=100
        BigFraction f = new BigFraction(0.5, 0.0, 100);
        assertEquals("Numerator should be 1", BigInteger.ONE, f.getNumerator());
        assertEquals("Denominator should be 2", BigInteger.valueOf(2), f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testDigitLimitConstructorDefectMaxDenominator() {
        // Also test via the maxDenominator constructor
        BigFraction f = new BigFraction(0.5, 1000);
        assertEquals("Numerator should be 1", BigInteger.ONE, f.getNumerator());
        assertEquals("Denominator should be 2", BigInteger.valueOf(2), f.getDenominator());
    }

    @Test(timeout = 4000)
    public void testDigitLimitConstructorDefectApproximation() {
        // Test a value that requires many iterations to trigger overflow earlier
        // (exact fraction might be large but algorithm should converge without overflow)
        BigFraction f = new BigFraction(Math.PI, 1e-5, 100);
        assertTrue("Result should be finite", f.getDenominator().bitCount() > 0);
    }

    // ---------- Partition D: Exception & Defensive Guard Paths ----------

    @Test(timeout = 4000)
    public void testConstructorDoubleEpsilonMaxIterationsNonConvergent() {
        try {
            // Very small epsilon, few iterations – should converge for 0.5
            new BigFraction(0.5, 1e-20, 1);
            fail("Expected FractionConversionException for non-convergence");
        } catch (FractionConversionException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleMaxDenominatorOverflow() {
        // The known bug manifests when maxDenominator is very large
        // Test that we get a sensible fraction even with huge maxDenominator.
        // On buggy version, this will throw FractionConversionException.
        try {
            BigFraction f = new BigFraction(0.5, Integer.MAX_VALUE);
            assertNotNull(f);
        } catch (Exception e) {
            // If it's FractionConversionException, that's the defect.
            // We'll let it fail the test.
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCompareTo() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 4); // equal after reduction
        assertEquals(0, a.compareTo(b));
        BigFraction c = new BigFraction(2, 3);
        assertTrue(a.compareTo(c) < 0);
        assertTrue(c.compareTo(a) > 0);
    }

    @Test(timeout = 4000)
    public void testEqualsIdentity() {
        BigFraction a = new BigFraction(1, 2);
        assertTrue(a.equals(a));
    }

    @Test(timeout = 4000)
    public void testEqualsEquivalent() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 4);
        assertTrue(a.equals(b));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        BigFraction a = new BigFraction(1, 2);
        assertFalse(a.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        BigFraction a = new BigFraction(1, 2);
        assertFalse(a.equals("hello"));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 4);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test(timeout = 4000)
    public void testConstants() {
        assertNotNull(BigFraction.TWO);
        assertNotNull(BigFraction.ONE);
        assertNotNull(BigFraction.ZERO);
        assertNotNull(BigFraction.MINUS_ONE);
        assertNotNull(BigFraction.FOUR_FIFTHS);
        assertNotNull(BigFraction.ONE_FIFTH);
        assertNotNull(BigFraction.ONE_HALF);
        assertNotNull(BigFraction.ONE_QUARTER);
        assertNotNull(BigFraction.ONE_THIRD);
        assertNotNull(BigFraction.THREE_FIFTHS);
        assertNotNull(BigFraction.THREE_QUARTERS);
        assertNotNull(BigFraction.TWO_FIFTHS);
        assertNotNull(BigFraction.TWO_QUARTERS);
        assertNotNull(BigFraction.TWO_THIRDS);
    }

    // ---------- Partition E: Object Lifecycle & Contract Integrity ----------

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeReflexive() {
        BigFraction f = new BigFraction(3, 7);
        assertEquals(f, f);
        assertEquals(f.hashCode(), f.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeSymmetric() {
        BigFraction a = new BigFraction(1, 3);
        BigFraction b = new BigFraction(2, 6);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test(timeout = 4000)
    public void testCompareToConsistentWithEquals() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 4);
        assertEquals(0, a.compareTo(b));
        assertTrue(a.equals(b));
    }

    @Test(timeout = 4000)
    public void testAddCommutative() {
        BigFraction a = new BigFraction(1, 3);
        BigFraction b = new BigFraction(2, 5);
        assertEquals(a.add(b), b.add(a));
    }

    @Test(timeout = 4000)
    public void testMultiplyAssociative() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 3);
        BigFraction c = new BigFraction(3, 4);
        assertEquals(a.multiply(b).multiply(c), a.multiply(b.multiply(c)));
    }

    @Test(timeout = 4000)
    public void testSubtractInverse() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(3, 4);
        assertEquals(a.subtract(b), a.add(b.negate()));
    }

    @Test(timeout = 4000)
    public void testDivideByReciprocal() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(3, 4);
        assertEquals(a.divide(b), a.multiply(b.reciprocal()));
    }

    @Test(timeout = 4000)
    public void testPowNegativeExponentReciprocal() {
        BigFraction a = new BigFraction(2, 3);
        assertEquals(a.pow(-2), a.reciprocal().pow(2));
    }

    @Test(timeout = 4000)
    public void testZeroToNegativePower() {
        // 0^(-1) should throw ArithmeticException
        try {
            BigFraction.ZERO.pow(-1);
            fail("Expected MathArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }
}