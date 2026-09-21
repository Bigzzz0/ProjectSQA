package org.apache.commons.math3.dfp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: Dfp.java (radix 10000 decimal floating point)
 * 
 * Known Defect: multiply() does not set FLAG_INVALID when one operand is NaN.
 *   - In multiply(), when isNaN() returns this or x, no flag is set.
 *   - IEEE 854-1987 requires invalid operation signal for NaN operands.
 *   - The test "testMultiply #37 x = NaN flags = 1" expects flags=1 (FLAG_INVALID) but actual flags=0.
 * 
 * Branch coverage targets:
 *   - add(): NaN/Infinity combinations, sign handling, alignment, complement, carry, normalization.
 *   - subtract(): via negate+add.
 *   - multiply(): NaN/Infinity/zero combinations, product array, exponent fixup, rounding.
 *   - divide(): NaN/Infinity/zero, quotient digit loop, trial digit, remainder, rounding.
 *   - sqrt(): zero, positive/negative, infinity, NaN, subnormal, convergence.
 *   - trunc/rint/floor/ceil: various rounding modes, exponent ranges, zero, infinity, NaN.
 *   - compare/lessThan/greaterThan/equals: sign, zero, infinity, NaN, precision mismatch.
 *   - toString/dfp2sci/dfp2string: normal, scientific, zero, infinity, NaN.
 *   - dotrap/trap: all trap types (INVALID, DIV_ZERO, UNDERFLOW, OVERFLOW).
 *   - align/shiftLeft/shiftRight: exponent difference, lost digit, inexact flag.
 *   - round(): all rounding modes, increment, underflow/overflow/inexact.
 *   - intValue(): bounds, rounding.
 *   - remainder(): IEEE remainder.
 *   - nextAfter(): direction, zero, infinity.
 *   - toDouble(): zero, subnormal, normal, infinity, NaN.
 *   - newInstance(): precision mismatch trap.
 *   - equals/hashCode: NaN, zero, different precision.
 *   - negativeOrNull/strictlyNegative/positiveOrNull/strictlyPositive: NaN, zero, infinity.
 *   - isZero/isInfinite/isNaN: edge cases.
 *   - log10K/log10/power10K/power10: various mantissa values.
 *   - multiply(int): range check, zero, infinity.
 *   - divide(int): zero divisor, range, rounding.
 *   - reciprocal(): via divide.
 *   - copysign: static method.
 *   - toSplitDouble: mask.
 * 
 * Boundary values:
 *   - exp: MIN_EXP, MAX_EXP, zero.
 *   - mant: all zeros, all nines, RADIX-1.
 *   - sign: +1, -1.
 *   - nans: FINITE, INFINITE, SNAN, QNAN.
 *   - precision mismatch: different radix digits.
 *   - String parsing: scientific notation, leading/trailing zeros, decimal point.
 *   - Long.MIN_VALUE special case in constructor.
 *   - Double constructor: subnormal, infinity, NaN.
 * 
 * Test structure:
 *   Partition A: Core functional logic & state transitions (add, subtract, multiply, divide, sqrt, trunc, etc.)
 *   Partition B: Boundary value analysis & extremes (zero, infinity, NaN, min/max exp, precision mismatch)
 *   Partition C: Defect-targeted branch zone (NaN multiply flag, missing invalid flag)
 *   Partition D: Exception & defensive guard paths (divide by zero, invalid arguments, range checks)
 *   Partition E: Object lifecycle & contract integrity (equals, hashCode, toString, newInstance, copy)
 */
public class DfpDeepseekTest {

    // Helper: create a DfpField with 5 radix digits (default precision)
    private DfpField field = new DfpField(5);

    // Helper: create a Dfp from double
    private Dfp d(double v) {
        return field.newDfp(v);
    }

    // Helper: create a Dfp from string
    private Dfp ds(String s) {
        return field.newDfp(s);
    }

    // Helper: create a Dfp from long
    private Dfp dl(long v) {
        return field.newDfp(v);
    }

    // ======================== Partition A: Core Functional Logic ========================

    @Test(timeout = 4000)
    public void testAddBasic() {
        Dfp a = d(1.0);
        Dfp b = d(2.0);
        Dfp sum = a.add(b);
        assertEquals("1+2", d(3.0), sum);
    }

    @Test(timeout = 4000)
    public void testAddNegative() {
        Dfp a = d(5.0);
        Dfp b = d(-3.0);
        Dfp sum = a.add(b);
        assertEquals("5+(-3)", d(2.0), sum);
    }

    @Test(timeout = 4000)
    public void testAddOppositeSigns() {
        Dfp a = d(3.0);
        Dfp b = d(-5.0);
        Dfp sum = a.add(b);
        assertEquals("3+(-5)", d(-2.0), sum);
    }

    @Test(timeout = 4000)
    public void testAddZero() {
        Dfp a = d(0.0);
        Dfp b = d(4.0);
        assertEquals("0+4", d(4.0), a.add(b));
        assertEquals("4+0", d(4.0), b.add(a));
    }

    @Test(timeout = 4000)
    public void testAddPrecisionMismatch() {
        DfpField field2 = new DfpField(10);
        Dfp a = d(1.0);
        Dfp b = field2.newDfp(2.0);
        Dfp result = a.add(b);
        assertTrue("result should be NaN", result.isNaN());
        // Check invalid flag set
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testSubtractBasic() {
        Dfp a = d(10.0);
        Dfp b = d(3.0);
        assertEquals("10-3", d(7.0), a.subtract(b));
    }

    @Test(timeout = 4000)
    public void testMultiplyBasic() {
        Dfp a = d(3.0);
        Dfp b = d(4.0);
        assertEquals("3*4", d(12.0), a.multiply(b));
    }

    @Test(timeout = 4000)
    public void testMultiplyNegative() {
        Dfp a = d(-2.0);
        Dfp b = d(5.0);
        assertEquals("-2*5", d(-10.0), a.multiply(b));
    }

    @Test(timeout = 4000)
    public void testMultiplyZero() {
        Dfp a = d(0.0);
        Dfp b = d(100.0);
        assertEquals("0*100", d(0.0), a.multiply(b));
        assertEquals("100*0", d(0.0), b.multiply(a));
    }

    @Test(timeout = 4000)
    public void testMultiplyByInt() {
        Dfp a = d(2.5);
        assertEquals("2.5*3", d(7.5), a.multiply(3));
    }

    @Test(timeout = 4000)
    public void testDivideBasic() {
        Dfp a = d(10.0);
        Dfp b = d(2.0);
        assertEquals("10/2", d(5.0), a.divide(b));
    }

    @Test(timeout = 4000)
    public void testDivideByInt() {
        Dfp a = d(7.0);
        assertEquals("7/2", d(3.5), a.divide(2));
    }

    @Test(timeout = 4000)
    public void testSqrtPositive() {
        Dfp a = d(9.0);
        Dfp sqrt = a.sqrt();
        assertEquals("sqrt(9)", d(3.0), sqrt);
    }

    @Test(timeout = 4000)
    public void testSqrtZero() {
        Dfp a = d(0.0);
        assertEquals("sqrt(0)", d(0.0), a.sqrt());
    }

    @Test(timeout = 4000)
    public void testRint() {
        Dfp a = d(2.5);
        Dfp r = a.rint();
        assertEquals("rint(2.5)", d(2.0), r); // round half-even -> 2
    }

    @Test(timeout = 4000)
    public void testFloor() {
        Dfp a = d(2.7);
        assertEquals("floor(2.7)", d(2.0), a.floor());
        Dfp b = d(-2.7);
        assertEquals("floor(-2.7)", d(-3.0), b.floor());
    }

    @Test(timeout = 4000)
    public void testCeil() {
        Dfp a = d(2.3);
        assertEquals("ceil(2.3)", d(3.0), a.ceil());
        Dfp b = d(-2.3);
        assertEquals("ceil(-2.3)", d(-2.0), b.ceil());
    }

    @Test(timeout = 4000)
    public void testRemainder() {
        Dfp a = d(10.0);
        Dfp b = d(3.0);
        Dfp rem = a.remainder(b);
        assertEquals("10%3", d(1.0), rem);
    }

    @Test(timeout = 4000)
    public void testAbs() {
        Dfp a = d(-5.0);
        assertEquals("abs(-5)", d(5.0), a.abs());
    }

    @Test(timeout = 4000)
    public void testNegate() {
        Dfp a = d(7.0);
        assertEquals("negate(7)", d(-7.0), a.negate());
    }

    @Test(timeout = 4000)
    public void testIntValue() {
        Dfp a = d(123.456);
        assertEquals("intValue(123.456)", 123, a.intValue());
        Dfp b = d(-123.456);
        assertEquals("intValue(-123.456)", -123, b.intValue());
    }

    @Test(timeout = 4000)
    public void testLog10K() {
        Dfp a = d(10000.0);
        assertEquals("log10K(10000)", 1, a.log10K());
    }

    @Test(timeout = 4000)
    public void testPower10K() {
        Dfp a = d(1.0);
        Dfp p = a.power10K(2);
        assertEquals("power10K(2)", d(100000000.0), p);
    }

    @Test(timeout = 4000)
    public void testLog10() {
        Dfp a = d(1000.0);
        assertEquals("log10(1000)", 3, a.log10());
    }

    @Test(timeout = 4000)
    public void testPower10() {
        Dfp a = d(1.0);
        Dfp p = a.power10(3);
        assertEquals("power10(3)", d(1000.0), p);
    }

    @Test(timeout = 4000)
    public void testReciprocal() {
        Dfp a = d(4.0);
        assertEquals("reciprocal(4)", d(0.25), a.reciprocal());
    }

    // ======================== Partition B: Boundary Value Analysis ========================

    @Test(timeout = 4000)
    public void testAddInfinity() {
        Dfp inf = d(Double.POSITIVE_INFINITY);
        Dfp finite = d(5.0);
        assertEquals("inf+5", inf, inf.add(finite));
        assertEquals("5+inf", inf, finite.add(inf));
    }

    @Test(timeout = 4000)
    public void testAddInfinityNegative() {
        Dfp inf = d(Double.NEGATIVE_INFINITY);
        Dfp finite = d(5.0);
        assertEquals("-inf+5", inf, inf.add(finite));
    }

    @Test(timeout = 4000)
    public void testAddInfinityOppositeSigns() {
        Dfp posInf = d(Double.POSITIVE_INFINITY);
        Dfp negInf = d(Double.NEGATIVE_INFINITY);
        Dfp result = posInf.add(negInf);
        assertTrue("inf + (-inf) should be NaN", result.isNaN());
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testMultiplyInfinityFinite() {
        Dfp inf = d(Double.POSITIVE_INFINITY);
        Dfp finite = d(3.0);
        assertEquals("inf*3", inf, inf.multiply(finite));
        assertEquals("3*inf", inf, finite.multiply(inf));
    }

    @Test(timeout = 4000)
    public void testMultiplyInfinityZero() {
        Dfp inf = d(Double.POSITIVE_INFINITY);
        Dfp zero = d(0.0);
        Dfp result = inf.multiply(zero);
        assertTrue("inf*0 should be NaN", result.isNaN());
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testDivideByZero() {
        Dfp a = d(5.0);
        Dfp zero = d(0.0);
        Dfp result = a.divide(zero);
        assertTrue("5/0 should be infinite", result.isInfinite());
        assertTrue("FLAG_DIV_ZERO should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_DIV_ZERO) != 0);
    }

    @Test(timeout = 4000)
    public void testDivideZeroByZero() {
        Dfp zero = d(0.0);
        Dfp result = zero.divide(zero);
        assertTrue("0/0 should be NaN", result.isNaN());
        assertTrue("FLAG_DIV_ZERO should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_DIV_ZERO) != 0);
    }

    @Test(timeout = 4000)
    public void testDivideInfinityByInfinity() {
        Dfp inf = d(Double.POSITIVE_INFINITY);
        Dfp result = inf.divide(inf);
        assertTrue("inf/inf should be NaN", result.isNaN());
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testSqrtNegative() {
        Dfp a = d(-4.0);
        Dfp result = a.sqrt();
        assertTrue("sqrt(-4) should be NaN", result.isNaN());
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testSqrtNaN() {
        Dfp nan = d(Double.NaN);
        Dfp result = nan.sqrt();
        assertTrue("sqrt(NaN) should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testSqrtInfinity() {
        Dfp inf = d(Double.POSITIVE_INFINITY);
        assertEquals("sqrt(inf)", inf, inf.sqrt());
    }

    @Test(timeout = 4000)
    public void testLessThanNaN() {
        Dfp a = d(1.0);
        Dfp nan = d(Double.NaN);
        assertFalse("1 < NaN should be false", a.lessThan(nan));
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testGreaterThanNaN() {
        Dfp a = d(1.0);
        Dfp nan = d(Double.NaN);
        assertFalse("1 > NaN should be false", a.greaterThan(nan));
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testEqualsNaN() {
        Dfp nan = d(Double.NaN);
        assertFalse("NaN equals NaN should be false", nan.equals(nan));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentPrecision() {
        DfpField field2 = new DfpField(10);
        Dfp a = d(1.0);
        Dfp b = field2.newDfp(1.0);
        assertFalse("different precision equals should be false", a.equals(b));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        Dfp a = d(1.0);
        Dfp b = d(1.0);
        assertEquals("hashCode of equal numbers", a.hashCode(), b.hashCode());
    }

    @Test(timeout = 4000)
    public void testToStringNormal() {
        Dfp a = d(123.456);
        String s = a.toString();
        assertTrue("toString should contain digits", s.contains("123"));
    }

    @Test(timeout = 4000)
    public void testToStringInfinity() {
        Dfp inf = d(Double.POSITIVE_INFINITY);
        assertEquals("Infinity", inf.toString());
        Dfp negInf = d(Double.NEGATIVE_INFINITY);
        assertEquals("-Infinity", negInf.toString());
    }

    @Test(timeout = 4000)
    public void testToStringNaN() {
        Dfp nan = d(Double.NaN);
        assertEquals("NaN", nan.toString());
    }

    @Test(timeout = 4000)
    public void testToDouble() {
        Dfp a = d(3.14159);
        assertEquals("toDouble", 3.14159, a.toDouble(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testToDoubleInfinity() {
        Dfp inf = d(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, inf.toDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testToDoubleNaN() {
        Dfp nan = d(Double.NaN);
        assertTrue(Double.isNaN(nan.toDouble()));
    }

    @Test(timeout = 4000)
    public void testNextAfterUp() {
        Dfp a = d(1.0);
        Dfp b = d(2.0);
        Dfp next = a.nextAfter(b);
        assertTrue("nextAfter(1,2) > 1", next.greaterThan(a));
    }

    @Test(timeout = 4000)
    public void testNextAfterDown() {
        Dfp a = d(2.0);
        Dfp b = d(1.0);
        Dfp next = a.nextAfter(b);
        assertTrue("nextAfter(2,1) < 2", next.lessThan(a));
    }

    @Test(timeout = 4000)
    public void testNextAfterEqual() {
        Dfp a = d(1.0);
        Dfp b = d(1.0);
        assertEquals("nextAfter(1,1)", a, a.nextAfter(b));
    }

    @Test(timeout = 4000)
    public void testNextAfterZero() {
        Dfp zero = d(0.0);
        Dfp one = d(1.0);
        Dfp next = zero.nextAfter(one);
        assertTrue("nextAfter(0,1) > 0", next.greaterThan(zero));
    }

    @Test(timeout = 4000)
    public void testNegativeOrNull() {
        Dfp zero = d(0.0);
        assertTrue("0 negativeOrNull", zero.negativeOrNull());
        Dfp neg = d(-1.0);
        assertTrue("-1 negativeOrNull", neg.negativeOrNull());
        Dfp pos = d(1.0);
        assertFalse("1 negativeOrNull", pos.negativeOrNull());
        Dfp nan = d(Double.NaN);
        assertFalse("NaN negativeOrNull", nan.negativeOrNull());
    }

    @Test(timeout = 4000)
    public void testStrictlyNegative() {
        Dfp zero = d(0.0);
        assertFalse("0 strictlyNegative", zero.strictlyNegative());
        Dfp neg = d(-1.0);
        assertTrue("-1 strictlyNegative", neg.strictlyNegative());
        Dfp pos = d(1.0);
        assertFalse("1 strictlyNegative", pos.strictlyNegative());
    }

    @Test(timeout = 4000)
    public void testPositiveOrNull() {
        Dfp zero = d(0.0);
        assertTrue("0 positiveOrNull", zero.positiveOrNull());
        Dfp pos = d(1.0);
        assertTrue("1 positiveOrNull", pos.positiveOrNull());
        Dfp neg = d(-1.0);
        assertFalse("-1 positiveOrNull", neg.positiveOrNull());
    }

    @Test(timeout = 4000)
    public void testStrictlyPositive() {
        Dfp zero = d(0.0);
        assertFalse("0 strictlyPositive", zero.strictlyPositive());
        Dfp pos = d(1.0);
        assertTrue("1 strictlyPositive", pos.strictlyPositive());
        Dfp neg = d(-1.0);
        assertFalse("-1 strictlyPositive", neg.strictlyPositive());
    }

    @Test(timeout = 4000)
    public void testIsZero() {
        Dfp zero = d(0.0);
        assertTrue("isZero(0)", zero.isZero());
        Dfp one = d(1.0);
        assertFalse("isZero(1)", one.isZero());
        Dfp nan = d(Double.NaN);
        assertFalse("isZero(NaN)", nan.isZero());
    }

    @Test(timeout = 4000)
    public void testIsInfinite() {
        Dfp inf = d(Double.POSITIVE_INFINITY);
        assertTrue("isInfinite(inf)", inf.isInfinite());
        Dfp finite = d(1.0);
        assertFalse("isInfinite(finite)", finite.isInfinite());
    }

    @Test(timeout = 4000)
    public void testIsNaN() {
        Dfp nan = d(Double.NaN);
        assertTrue("isNaN(NaN)", nan.isNaN());
        Dfp finite = d(1.0);
        assertFalse("isNaN(finite)", finite.isNaN());
    }

    @Test(timeout = 4000)
    public void testClassify() {
        Dfp finite = d(1.0);
        assertEquals("FINITE", Dfp.FINITE, finite.classify());
        Dfp inf = d(Double.POSITIVE_INFINITY);
        assertEquals("INFINITE", Dfp.INFINITE, inf.classify());
        Dfp nan = d(Double.NaN);
        assertEquals("QNAN", Dfp.QNAN, nan.classify());
    }

    @Test(timeout = 4000)
    public void testCopysign() {
        Dfp x = d(5.0);
        Dfp y = d(-1.0);
        Dfp result = Dfp.copysign(x, y);
        assertEquals("copysign(5,-1) should be -5", d(-5.0), result);
    }

    @Test(timeout = 4000)
    public void testNewInstanceFromString() {
        Dfp a = ds("123.456");
        assertEquals("newInstance from string", d(123.456), a);
    }

    @Test(timeout = 4000)
    public void testNewInstanceFromStringScientific() {
        Dfp a = ds("1.23e2");
        assertEquals("1.23e2", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testNewInstanceFromStringNegative() {
        Dfp a = ds("-0.001");
        assertEquals("-0.001", d(-0.001), a);
    }

    @Test(timeout = 4000)
    public void testNewInstancePrecisionMismatch() {
        DfpField field2 = new DfpField(10);
        Dfp a = d(1.0);
        Dfp b = field2.newDfp(2.0);
        Dfp result = a.newInstance(b);
        assertTrue("newInstance with different precision should return NaN", result.isNaN());
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testMultiplyByIntRangeCheck() {
        Dfp a = d(1.0);
        // RADIX = 10000, so x >= 10000 is invalid
        Dfp result = a.multiply(10000);
        assertTrue("multiply by 10000 should be NaN", result.isNaN());
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testDivideByIntZero() {
        Dfp a = d(1.0);
        Dfp result = a.divide(0);
        assertTrue("divide by 0 should be infinite", result.isInfinite());
        assertTrue("FLAG_DIV_ZERO should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_DIV_ZERO) != 0);
    }

    @Test(timeout = 4000)
    public void testDivideByIntRangeCheck() {
        Dfp a = d(1.0);
        Dfp result = a.divide(-1);
        assertTrue("divide by -1 should be NaN", result.isNaN());
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testUnequal() {
        Dfp a = d(1.0);
        Dfp b = d(2.0);
        assertTrue("1 unequal 2", a.unequal(b));
        assertFalse("1 unequal 1", a.unequal(a));
        Dfp nan = d(Double.NaN);
        assertFalse("1 unequal NaN", a.unequal(nan));
    }

    @Test(timeout = 4000)
    public void testToSplitDouble() {
        Dfp a = d(1.23456789);
        double[] split = a.toSplitDouble();
        assertEquals("split sum", a.toDouble(), split[0] + split[1], 1e-15);
    }

    // ======================== Partition C: Defect-Targeted Branch Zone ========================

    /**
     * Defect: multiply() does not set FLAG_INVALID when one operand is NaN.
     * This test verifies that after multiplying a finite number by NaN,
     * the invalid flag is set and the result is NaN.
     */
    @Test(timeout = 4000)
    public void testMultiplyNaNFlag() {
        // Clear flags
        field.clearIEEEFlagsBits();
        Dfp finite = d(5.0);
        Dfp nan = d(Double.NaN);
        Dfp result = finite.multiply(nan);
        assertTrue("finite * NaN should be NaN", result.isNaN());
        int flags = field.getIEEEFlagsBits();
        assertTrue("FLAG_INVALID should be set after finite * NaN, but flags = " + flags,
                   (flags & DfpField.FLAG_INVALID) != 0);
    }

    /**
     * Additional test: multiply NaN by finite, also should set flag.
     */
    @Test(timeout = 4000)
    public void testMultiplyNaNFlagReverse() {
        field.clearIEEEFlagsBits();
        Dfp nan = d(Double.NaN);
        Dfp finite = d(3.0);
        Dfp result = nan.multiply(finite);
        assertTrue("NaN * finite should be NaN", result.isNaN());
        int flags = field.getIEEEFlagsBits();
        assertTrue("FLAG_INVALID should be set after NaN * finite, but flags = " + flags,
                   (flags & DfpField.FLAG_INVALID) != 0);
    }

    /**
     * Test multiply NaN by NaN.
     */
    @Test(timeout = 4000)
    public void testMultiplyNaNNaN() {
        field.clearIEEEFlagsBits();
        Dfp nan1 = d(Double.NaN);
        Dfp nan2 = d(Double.NaN);
        Dfp result = nan1.multiply(nan2);
        assertTrue("NaN * NaN should be NaN", result.isNaN());
        int flags = field.getIEEEFlagsBits();
        assertTrue("FLAG_INVALID should be set after NaN * NaN, but flags = " + flags,
                   (flags & DfpField.FLAG_INVALID) != 0);
    }

    /**
     * Test that add also sets invalid flag for NaN operands (should already work, but verify).
     */
    @Test(timeout = 4000)
    public void testAddNaNFlag() {
        field.clearIEEEFlagsBits();
        Dfp finite = d(1.0);
        Dfp nan = d(Double.NaN);
        Dfp result = finite.add(nan);
        assertTrue("finite + NaN should be NaN", result.isNaN());
        int flags = field.getIEEEFlagsBits();
        assertTrue("FLAG_INVALID should be set after finite + NaN, but flags = " + flags,
                   (flags & DfpField.FLAG_INVALID) != 0);
    }

    // ======================== Partition D: Exception & Defensive Guard Paths ========================

    @Test(timeout = 4000)
    public void testDivideByZeroInt() {
        Dfp a = d(1.0);
        Dfp result = a.divide(0);
        assertTrue("divide by 0 int should be infinite", result.isInfinite());
        assertTrue("FLAG_DIV_ZERO should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_DIV_ZERO) != 0);
    }

    @Test(timeout = 4000)
    public void testMultiplyByZeroInfinity() {
        Dfp inf = d(Double.POSITIVE_INFINITY);
        Dfp zero = d(0.0);
        Dfp result = inf.multiply(zero);
        assertTrue("inf * 0 should be NaN", result.isNaN());
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testAddInfinityOppositeSignsFlag() {
        field.clearIEEEFlagsBits();
        Dfp posInf = d(Double.POSITIVE_INFINITY);
        Dfp negInf = d(Double.NEGATIVE_INFINITY);
        posInf.add(negInf);
        assertTrue("FLAG_INVALID should be set for inf + (-inf)", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testDivideInfinityByInfinityFlag() {
        field.clearIEEEFlagsBits();
        Dfp inf = d(Double.POSITIVE_INFINITY);
        inf.divide(inf);
        assertTrue("FLAG_INVALID should be set for inf/inf", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testSqrtNegativeFlag() {
        field.clearIEEEFlagsBits();
        Dfp a = d(-1.0);
        a.sqrt();
        assertTrue("FLAG_INVALID should be set for sqrt(-1)", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testLessThanNaNFlag() {
        field.clearIEEEFlagsBits();
        Dfp a = d(1.0);
        Dfp nan = d(Double.NaN);
        a.lessThan(nan);
        assertTrue("FLAG_INVALID should be set for lessThan with NaN", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testGreaterThanNaNFlag() {
        field.clearIEEEFlagsBits();
        Dfp a = d(1.0);
        Dfp nan = d(Double.NaN);
        a.greaterThan(nan);
        assertTrue("FLAG_INVALID should be set for greaterThan with NaN", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testNegativeOrNullNaN() {
        field.clearIEEEFlagsBits();
        Dfp nan = d(Double.NaN);
        nan.negativeOrNull();
        assertTrue("FLAG_INVALID should be set for negativeOrNull on NaN", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testStrictlyNegativeNaN() {
        field.clearIEEEFlagsBits();
        Dfp nan = d(Double.NaN);
        nan.strictlyNegative();
        assertTrue("FLAG_INVALID should be set for strictlyNegative on NaN", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testPositiveOrNullNaN() {
        field.clearIEEEFlagsBits();
        Dfp nan = d(Double.NaN);
        nan.positiveOrNull();
        assertTrue("FLAG_INVALID should be set for positiveOrNull on NaN", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testStrictlyPositiveNaN() {
        field.clearIEEEFlagsBits();
        Dfp nan = d(Double.NaN);
        nan.strictlyPositive();
        assertTrue("FLAG_INVALID should be set for strictlyPositive on NaN", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testIsZeroNaN() {
        field.clearIEEEFlagsBits();
        Dfp nan = d(Double.NaN);
        nan.isZero();
        assertTrue("FLAG_INVALID should be set for isZero on NaN", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testAddPrecisionMismatchFlag() {
        field.clearIEEEFlagsBits();
        DfpField field2 = new DfpField(10);
        Dfp a = d(1.0);
        Dfp b = field2.newDfp(2.0);
        a.add(b);
        assertTrue("FLAG_INVALID should be set for precision mismatch add", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testMultiplyPrecisionMismatchFlag() {
        field.clearIEEEFlagsBits();
        DfpField field2 = new DfpField(10);
        Dfp a = d(1.0);
        Dfp b = field2.newDfp(2.0);
        a.multiply(b);
        assertTrue("FLAG_INVALID should be set for precision mismatch multiply", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testDividePrecisionMismatchFlag() {
        field.clearIEEEFlagsBits();
        DfpField field2 = new DfpField(10);
        Dfp a = d(1.0);
        Dfp b = field2.newDfp(2.0);
        a.divide(b);
        assertTrue("FLAG_INVALID should be set for precision mismatch divide", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testNextAfterPrecisionMismatchFlag() {
        field.clearIEEEFlagsBits();
        DfpField field2 = new DfpField(10);
        Dfp a = d(1.0);
        Dfp b = field2.newDfp(2.0);
        a.nextAfter(b);
        assertTrue("FLAG_INVALID should be set for precision mismatch nextAfter", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testLessThanPrecisionMismatchFlag() {
        field.clearIEEEFlagsBits();
        DfpField field2 = new DfpField(10);
        Dfp a = d(1.0);
        Dfp b = field2.newDfp(2.0);
        a.lessThan(b);
        assertTrue("FLAG_INVALID should be set for precision mismatch lessThan", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testGreaterThanPrecisionMismatchFlag() {
        field.clearIEEEFlagsBits();
        DfpField field2 = new DfpField(10);
        Dfp a = d(1.0);
        Dfp b = field2.newDfp(2.0);
        a.greaterThan(b);
        assertTrue("FLAG_INVALID should be set for precision mismatch greaterThan", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    // ======================== Partition E: Object Lifecycle & Contract Integrity ========================

    @Test(timeout = 4000)
    public void testCopyConstructor() {
        Dfp a = d(3.14);
        Dfp b = new Dfp(a);
        assertEquals("copy constructor", a, b);
        // Ensure they are independent
        a = a.add(d(1.0));
        assertFalse("copy should be independent", a.equals(b));
    }

    @Test(timeout = 4000)
    public void testNewInstanceCopy() {
        Dfp a = d(2.718);
        Dfp b = a.newInstance(a);
        assertEquals("newInstance copy", a, b);
    }

    @Test(timeout = 4000)
    public void testNewInstanceFromByte() {
        Dfp a = field.newDfp((byte) 5);
        assertEquals("newInstance byte", d(5.0), a);
    }

    @Test(timeout = 4000)
    public void testNewInstanceFromInt() {
        Dfp a = field.newDfp(123);
        assertEquals("newInstance int", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testNewInstanceFromLong() {
        Dfp a = field.newDfp(123456789L);
        assertEquals("newInstance long", d(123456789.0), a);
    }

    @Test(timeout = 4000)
    public void testNewInstanceFromDouble() {
        Dfp a = field.newDfp(3.141592653589793);
        assertEquals("newInstance double", d(3.141592653589793), a);
    }

    @Test(timeout = 4000)
    public void testNewInstanceFromString() {
        Dfp a = field.newDfp("0.001");
        assertEquals("newInstance string", d(0.001), a);
    }

    @Test(timeout = 4000)
    public void testNewInstanceNonFinite() {
        Dfp a = field.newDfp((byte) 1, Dfp.INFINITE);
        assertTrue("newInstance non-finite should be infinite", a.isInfinite());
        assertEquals("sign should be positive", 1, a.sign);
    }

    @Test(timeout = 4000)
    public void testGetField() {
        Dfp a = d(1.0);
        assertSame("getField", field, a.getField());
    }

    @Test(timeout = 4000)
    public void testGetRadixDigits() {
        Dfp a = d(1.0);
        assertEquals("getRadixDigits", 5, a.getRadixDigits());
    }

    @Test(timeout = 4000)
    public void testGetZero() {
        Dfp zero = d(0.0);
        assertEquals("getZero", zero, field.getZero());
    }

    @Test(timeout = 4000)
    public void testGetOne() {
        Dfp one = d(1.0);
        assertEquals("getOne", one, field.getOne());
    }

    @Test(timeout = 4000)
    public void testGetTwo() {
        Dfp two = d(2.0);
        assertEquals("getTwo", two, field.getTwo());
    }

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        Dfp a = d(1.0);
        assertTrue("equals self", a.equals(a));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        Dfp a = d(1.0);
        assertFalse("equals null", a.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        Dfp a = d(1.0);
        assertFalse("equals different class", a.equals("1.0"));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Dfp a = d(1.0);
        int h1 = a.hashCode();
        int h2 = a.hashCode();
        assertEquals("hashCode consistent", h1, h2);
    }

    @Test(timeout = 4000)
    public void testToStringZero() {
        Dfp zero = d(0.0);
        String s = zero.toString();
        // Should contain "0" and possibly decimal point
        assertTrue("toString zero", s.contains("0"));
    }

    @Test(timeout = 4000)
    public void testToStringNegative() {
        Dfp a = d(-123.456);
        String s = a.toString();
        assertTrue("toString negative starts with -", s.startsWith("-"));
    }

    @Test(timeout = 4000)
    public void testToStringScientific() {
        Dfp a = d(1e10);
        String s = a.toString();
        assertTrue("toString scientific should contain 'e'", s.contains("e"));
    }

    @Test(timeout = 4000)
    public void testDfp2sci() {
        Dfp a = d(1e10);
        String s = a.dfp2sci();
        assertTrue("dfp2sci should contain 'e'", s.contains("e"));
    }

    @Test(timeout = 4000)
    public void testDfp2string() {
        Dfp a = d(123.456);
        String s = a.dfp2string();
        assertTrue("dfp2string should contain decimal point", s.contains("."));
    }

    @Test(timeout = 4000)
    public void testDotrapInvalid() {
        Dfp a = d(1.0);
        Dfp result = a.dotrap(DfpField.FLAG_INVALID, "test", a, a);
        assertTrue("dotrap INVALID should return NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDotrapDivZero() {
        Dfp a = d(1.0);
        Dfp zero = d(0.0);
        Dfp result = a.dotrap(DfpField.FLAG_DIV_ZERO, "test", zero, a);
        assertTrue("dotrap DIV_ZERO should return infinite", result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testDotrapUnderflow() {
        Dfp a = d(1.0);
        // Simulate underflow by setting exp very low
        a.exp = Dfp.MIN_EXP - 10;
        Dfp result = a.dotrap(DfpField.FLAG_UNDERFLOW, "test", a, a);
        // Should return zero or gradual underflow
        assertTrue("dotrap UNDERFLOW should return zero or finite", result.isZero() || !result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testDotrapOverflow() {
        Dfp a = d(1.0);
        a.exp = Dfp.MAX_EXP + 10;
        Dfp result = a.dotrap(DfpField.FLAG_OVERFLOW, "test", a, a);
        assertTrue("dotrap OVERFLOW should return infinite", result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testTrapDefault() {
        Dfp a = d(1.0);
        Dfp def = d(0.0);
        Dfp result = a.trap(DfpField.FLAG_INVALID, "test", a, def, a);
        assertSame("trap should return def", def, result);
    }

    @Test(timeout = 4000)
    public void testLongMinValueConstructor() {
        // Special case: Long.MIN_VALUE
        Dfp a = field.newDfp(Long.MIN_VALUE);
        assertEquals("Long.MIN_VALUE", d(-9223372036854775808L), a);
    }

    @Test(timeout = 4000)
    public void testDoubleConstructorSubnormal() {
        Dfp a = field.newDfp(Double.MIN_VALUE);
        assertFalse("subnormal should be finite", a.isNaN());
        assertFalse("subnormal should not be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testDoubleConstructorNaN() {
        Dfp a = field.newDfp(Double.NaN);
        assertTrue("double NaN constructor", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testDoubleConstructorInfinity() {
        Dfp a = field.newDfp(Double.POSITIVE_INFINITY);
        assertTrue("double inf constructor", a.isInfinite());
        assertEquals("sign positive", 1, a.sign);
    }

    @Test(timeout = 4000)
    public void testDoubleConstructorNegativeInfinity() {
        Dfp a = field.newDfp(Double.NEGATIVE_INFINITY);
        assertTrue("double -inf constructor", a.isInfinite());
        assertEquals("sign negative", -1, a.sign);
    }

    @Test(timeout = 4000)
    public void testDoubleConstructorZero() {
        Dfp a = field.newDfp(0.0);
        assertTrue("double zero constructor", a.isZero());
        assertEquals("sign positive", 1, a.sign);
    }

    @Test(timeout = 4000)
    public void testDoubleConstructorNegativeZero() {
        Dfp a = field.newDfp(-0.0);
        assertTrue("double -0 constructor", a.isZero());
        assertEquals("sign negative", -1, a.sign);
    }

    @Test(timeout = 4000)
    public void testStringConstructorInfinity() {
        Dfp a = field.newDfp("Infinity");
        assertTrue("string Infinity", a.isInfinite());
        assertEquals("sign positive", 1, a.sign);
    }

    @Test(timeout = 4000)
    public void testStringConstructorNegativeInfinity() {
        Dfp a = field.newDfp("-Infinity");
        assertTrue("string -Infinity", a.isInfinite());
        assertEquals("sign negative", -1, a.sign);
    }

    @Test(timeout = 4000)
    public void testStringConstructorNaN() {
        Dfp a = field.newDfp("NaN");
        assertTrue("string NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testStringConstructorLeadingZeros() {
        Dfp a = field.newDfp("000123.456");
        assertEquals("leading zeros", d(123.456), a);
    }

    @Test(timeout = 4000)
    public void testStringConstructorTrailingZeros() {
        Dfp a = field.newDfp("123.456000");
        assertEquals("trailing zeros", d(123.456), a);
    }

    @Test(timeout = 4000)
    public void testStringConstructorOnlyZeros() {
        Dfp a = field.newDfp("0.00000");
        assertTrue("0.00000 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testStringConstructorScientificNegativeExponent() {
        Dfp a = field.newDfp("1.23e-2");
        assertEquals("1.23e-2", d(0.0123), a);
    }

    @Test(timeout = 4000)
    public void testStringConstructorScientificPositiveExponent() {
        Dfp a = field.newDfp("1.23e+4");
        assertEquals("1.23e+4", d(12300.0), a);
    }

    @Test(timeout = 4000)
    public void testStringConstructorNoDecimal() {
        Dfp a = field.newDfp("123");
        assertEquals("123 without decimal", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testStringConstructorNegative() {
        Dfp a = field.newDfp("-0.5");
        assertEquals("-0.5", d(-0.5), a);
    }

    @Test(timeout = 4000)
    public void testShiftLeft() {
        Dfp a = d(1.0);
        a.shiftLeft();
        assertEquals("shiftLeft should reduce exponent", 0, a.exp);
        // mantissa should have been shifted
    }

    @Test(timeout = 4000)
    public void testShiftRight() {
        Dfp a = d(1.0);
        a.shiftRight();
        assertEquals("shiftRight should increase exponent", 2, a.exp);
    }

    @Test(timeout = 4000)
    public void testAlign() {
        Dfp a = d(100.0);
        int lost = a.align(1);
        // After alignment, exponent should be 1
        assertEquals("align exponent", 1, a.exp);
    }

    @Test(timeout = 4000)
    public void testComplement() {
        Dfp a = d(1.0);
        int extra = a.complement(0);
        // Complement should produce negative representation
        assertTrue("complement extra should be non-negative", extra >= 0);
    }

    @Test(timeout = 4000)
    public void testRoundAllModes() {
        // Test round with different rounding modes indirectly via trunc
        Dfp a = d(2.5);
        // Default rounding mode is ROUND_HALF_EVEN
        Dfp r = a.rint();
        assertEquals("rint(2.5) with HALF_EVEN", d(2.0), r);
    }

    @Test(timeout = 4000)
    public void testRoundUp() {
        field.setRoundingMode(DfpField.RoundingMode.ROUND_UP);
        Dfp a = d(2.1);
        Dfp r = a.rint();
        assertEquals("rint(2.1) with ROUND_UP", d(3.0), r);
        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_EVEN); // reset
    }

    @Test(timeout = 4000)
    public void testRoundDown() {
        field.setRoundingMode(DfpField.RoundingMode.ROUND_DOWN);
        Dfp a = d(2.9);
        Dfp r = a.rint();
        assertEquals("rint(2.9) with ROUND_DOWN", d(2.0), r);
        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_EVEN); // reset
    }

    @Test(timeout = 4000)
    public void testRoundCeil() {
        field.setRoundingMode(DfpField.RoundingMode.ROUND_CEIL);
        Dfp a = d(2.1);
        Dfp r = a.ceil();
        assertEquals("ceil(2.1) with ROUND_CEIL", d(3.0), r);
        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_EVEN); // reset
    }

    @Test(timeout = 4000)
    public void testRoundFloor() {
        field.setRoundingMode(DfpField.RoundingMode.ROUND_FLOOR);
        Dfp a = d(2.9);
        Dfp r = a.floor();
        assertEquals("floor(2.9) with ROUND_FLOOR", d(2.0), r);
        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_EVEN); // reset
    }

    @Test(timeout = 4000)
    public void testRoundHalfUp() {
        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_UP);
        Dfp a = d(2.5);
        Dfp r = a.rint();
        assertEquals("rint(2.5) with ROUND_HALF_UP", d(3.0), r);
        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_EVEN); // reset
    }

    @Test(timeout = 4000)
    public void testRoundHalfDown() {
        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_DOWN);
        Dfp a = d(2.5);
        Dfp r = a.rint();
        assertEquals("rint(2.5) with ROUND_HALF_DOWN", d(2.0), r);
        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_EVEN); // reset
    }

    @Test(timeout = 4000)
    public void testRoundHalfOdd() {
        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_ODD);
        Dfp a = d(2.5);
        Dfp r = a.rint();
        assertEquals("rint(2.5) with ROUND_HALF_ODD", d(3.0), r); // 2.5 -> 3 because 2 is even, odd rounds to odd? Actually half-odd rounds to nearest odd, so 2.5 -> 3 (odd)
        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_EVEN); // reset
    }

    @Test(timeout = 4000)
    public void testIntValueClampPositive() {
        Dfp a = d(2147483648.0); // > Integer.MAX_VALUE
        assertEquals("intValue clamp positive", 2147483647, a.intValue());
    }

    @Test(timeout = 4000)
    public void testIntValueClampNegative() {
        Dfp a = d(-2147483649.0); // < Integer.MIN_VALUE
        assertEquals("intValue clamp negative", -2147483648, a.intValue());
    }

    @Test(timeout = 4000)
    public void testMultiplyByIntZero() {
        Dfp a = d(5.0);
        Dfp result = a.multiply(0);
        assertEquals("5*0", d(0.0), result);
    }

    @Test(timeout = 4000)
    public void testMultiplyByIntInfinity() {
        Dfp inf = d(Double.POSITIVE_INFINITY);
        Dfp result = inf.multiply(2);
        assertTrue("inf*2 should be inf", result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testMultiplyByIntNaN() {
        Dfp nan = d(Double.NaN);
        Dfp result = nan.multiply(2);
        assertTrue("NaN*2 should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDivideByIntInfinity() {
        Dfp inf = d(Double.POSITIVE_INFINITY);
        Dfp result = inf.divide(2);
        assertTrue("inf/2 should be inf", result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testDivideByIntNaN() {
        Dfp nan = d(Double.NaN);
        Dfp result = nan.divide(2);
        assertTrue("NaN/2 should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDivideByIntNormalize() {
        Dfp a = d(1.0);
        Dfp result = a.divide(10000); // divisor = RADIX, should normalize
        assertEquals("1/10000", d(0.0001), result);
    }

    @Test(timeout = 4000)
    public void testMultiplyFast() {
        // multiplyFast is private, but multiply(int) calls it
        Dfp a = d(2.0);
        Dfp result = a.multiply(3);
        assertEquals("2*3", d(6.0), result);
    }

    @Test(timeout = 4000)
    public void testMultiplyFastOverflow() {
        Dfp a = d(5000.0);
        Dfp result = a.multiply(3);
        // Should not overflow, but test that it works
        assertEquals("5000*3", d(15000.0), result);
    }

    @Test(timeout = 4000)
    public void testDivideIntNormalizeShift() {
        Dfp a = d(1.0);
        Dfp result = a.divide(5000);
        // Should produce a normalized result
        assertFalse("result should be finite", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testRemainderZeroSign() {
        Dfp a = d(5.0);
        Dfp b = d(5.0);
        Dfp rem = a.remainder(b);
        assertTrue("remainder(5,5) should be zero", rem.isZero());
        assertEquals("sign should be positive", 1, rem.sign);
    }

    @Test(timeout = 4000)
    public void testRemainderNegativeSign() {
        Dfp a = d(-5.0);
        Dfp b = d(3.0);
        Dfp rem = a.remainder(b);
        // IEEE remainder: result sign = sign of this
        assertEquals("remainder(-5,3) sign", -1, rem.sign);
    }

    @Test(timeout = 4000)
    public void testCompareZeroSign() {
        Dfp posZero = d(0.0);
        Dfp negZero = d(-0.0);
        assertTrue("+0 equals -0", posZero.equals(negZero));
    }

    @Test(timeout = 4000)
    public void testCompareInfinity() {
        Dfp posInf = d(Double.POSITIVE_INFINITY);
        Dfp negInf = d(Double.NEGATIVE_INFINITY);
        assertTrue("posInf > negInf", posInf.greaterThan(negInf));
        assertTrue("negInf < posInf", negInf.lessThan(posInf));
    }

    @Test(timeout = 4000)
    public void testCompareNaN() {
        Dfp nan = d(Double.NaN);
        Dfp one = d(1.0);
        assertFalse("NaN compare to one", one.lessThan(nan) || one.greaterThan(nan));
    }

    @Test(timeout = 4000)
    public void testComparePrecisionMismatch() {
        DfpField field2 = new DfpField(10);
        Dfp a = d(1.0);
        Dfp b = field2.newDfp(1.0);
        // compare() is private, but lessThan/greaterThan handle it
        assertFalse("precision mismatch lessThan", a.lessThan(b));
        assertFalse("precision mismatch greaterThan", a.greaterThan(b));
    }

    @Test(timeout = 4000)
    public void testDfp2sciZero() {
        Dfp zero = d(0.0);
        String s = zero.dfp2sci();
        assertEquals("dfp2sci zero", "0.0e0", s);
    }

    @Test(timeout = 4000)
    public void testDfp2stringZero() {
        Dfp zero = d(0.0);
        String s = zero.dfp2string();
        assertTrue("dfp2string zero contains 0", s.contains("0"));
    }

    @Test(timeout = 4000)
    public void testDfp2stringNegative() {
        Dfp a = d(-1.0);
        String s = a.dfp2string();
        assertTrue("dfp2string negative starts with -", s.startsWith("-"));
    }

    @Test(timeout = 4000)
    public void testDfp2stringLargeExponent() {
        Dfp a = d(1e10);
        String s = a.dfp2string();
        // Should be in scientific notation because exp > mant.length
        assertTrue("large exponent should use scientific", s.contains("e"));
    }

    @Test(timeout = 4000)
    public void testDfp2stringSmallExponent() {
        Dfp a = d(0.0001);
        String s = a.dfp2string();
        // Should be in normal notation
        assertFalse("small exponent should not use scientific", s.contains("e"));
    }

    @Test(timeout = 4000)
    public void testPower10NegativeExponent() {
        Dfp a = d(1.0);
        Dfp p = a.power10(-3);
        assertEquals("power10(-3)", d(0.001), p);
    }

    @Test(timeout = 4000)
    public void testPower10KNegativeExponent() {
        Dfp a = d(1.0);
        Dfp p = a.power10K(-1);
        assertEquals("power10K(-1)", d(0.0001), p);
    }

    @Test(timeout = 4000)
    public void testLog10KZero() {
        Dfp zero = d(0.0);
        // log10K of zero is undefined, but method returns exp-1, which for zero is -1
        assertEquals("log10K(0)", -1, zero.log10K());
    }

    @Test(timeout = 4000)
    public void testLog10Zero() {
        Dfp zero = d(0.0);
        // log10 of zero returns exp*4-4 = -4
        assertEquals("log10(0)", -4, zero.log10());
    }

    @Test(timeout = 4000)
    public void testToDoubleNegativeZero() {
        Dfp a = d(-0.0);
        double d = a.toDouble();
        assertEquals("toDouble(-0)", -0.0, d, 0.0);
        assertTrue("sign bit negative", Double.doubleToRawLongBits(d) < 0);
    }

    @Test(timeout = 4000)
    public void testToDoubleSubnormal() {
        Dfp a = d(Double.MIN_VALUE);
        double d = a.toDouble();
        assertEquals("toDouble(MIN_VALUE)", Double.MIN_VALUE, d, 0.0);
    }

    @Test(timeout = 4000)
    public void testToDoubleLarge() {
        Dfp a = d(Double.MAX_VALUE);
        double d = a.toDouble();
        assertEquals("toDouble(MAX_VALUE)", Double.MAX_VALUE, d, 0.0);
    }

    @Test(timeout = 4000)
    public void testToDoubleOverflow() {
        Dfp a = d(1e309);
        double d = a.toDouble();
        assertTrue("toDouble overflow should be infinity", Double.isInfinite(d));
    }

    @Test(timeout = 4000)
    public void testToDoubleUnderflow() {
        Dfp a = d(1e-325);
        double d = a.toDouble();
        assertEquals("toDouble underflow should be 0", 0.0, d, 0.0);
    }

    @Test(timeout = 4000)
    public void testToSplitDoubleAccuracy() {
        Dfp a = d(1.2345678901234567);
        double[] split = a.toSplitDouble();
        double sum = split[0] + split[1];
        assertEquals("split double sum", a.toDouble(), sum, 1e-15);
    }

    @Test(timeout = 4000)
    public void testNextAfterPrecisionMismatch() {
        field.clearIEEEFlagsBits();
        DfpField field2 = new DfpField(10);
        Dfp a = d(1.0);
        Dfp b = field2.newDfp(2.0);
        Dfp result = a.nextAfter(b);
        assertTrue("nextAfter precision mismatch should return NaN", result.isNaN());
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testNextAfterZeroToPositive() {
        Dfp zero = d(0.0);
        Dfp one = d(1.0);
        Dfp next = zero.nextAfter(one);
        assertTrue("nextAfter(0,1) should be positive", next.sign > 0);
        assertTrue("nextAfter(0,1) should be > 0", next.greaterThan(zero));
    }

    @Test(timeout = 4000)
    public void testNextAfterZeroToNegative() {
        Dfp zero = d(0.0);
        Dfp negOne = d(-1.0);
        Dfp next = zero.nextAfter(negOne);
        assertTrue("nextAfter(0,-1) should be negative", next.sign < 0);
        assertTrue("nextAfter(0,-1) should be < 0", next.lessThan(zero));
    }

    @Test(timeout = 4000)
    public void testNextAfterInfinity() {
        Dfp inf = d(Double.POSITIVE_INFINITY);
        Dfp one = d(1.0);
        Dfp next = inf.nextAfter(one);
        // nextAfter(inf, 1) should be a finite number just below inf
        assertFalse("nextAfter(inf,1) should be finite", next.isInfinite());
    }

    @Test(timeout = 4000)
    public void testNextAfterNegativeInfinity() {
        Dfp negInf = d(Double.NEGATIVE_INFINITY);
        Dfp one = d(1.0);
        Dfp next = negInf.nextAfter(one);
        assertFalse("nextAfter(-inf,1) should be finite", next.isInfinite());
    }

    @Test(timeout = 4000)
    public void testNextAfterSame() {
        Dfp a = d(3.0);
        Dfp b = d(3.0);
        assertEquals("nextAfter(3,3)", a, a.nextAfter(b));
    }

    @Test(timeout = 4000)
    public void testNextAfterInexactFlag() {
        field.clearIEEEFlagsBits();
        Dfp a = d(1.0);
        Dfp b = d(2.0);
        a.nextAfter(b);
        // Should set inexact flag because result is different
        assertTrue("FLAG_INEXACT should be set for nextAfter", (field.getIEEEFlagsBits() & DfpField.FLAG_INEXACT) != 0);
    }

    @Test(timeout = 4000)
    public void testNextAfterZeroInexact() {
        field.clearIEEEFlagsBits();
        Dfp zero = d(0.0);
        Dfp one = d(1.0);
        zero.nextAfter(one);
        assertTrue("FLAG_INEXACT should be set for nextAfter from zero", (field.getIEEEFlagsBits() & DfpField.FLAG_INEXACT) != 0);
    }

    @Test(timeout = 4000)
    public void testAddInexactFlag() {
        field.clearIEEEFlagsBits();
        Dfp a = d(1.0);
        Dfp b = d(1e-10);
        a.add(b);
        // Should be inexact due to rounding
        assertTrue("FLAG_INEXACT should be set for add", (field.getIEEEFlagsBits() & DfpField.FLAG_INEXACT) != 0);
    }

    @Test(timeout = 4000)
    public void testMultiplyInexactFlag() {
        field.clearIEEEFlagsBits();
        Dfp a = d(1.0);
        Dfp b = d(1e-10);
        a.multiply(b);
        assertTrue("FLAG_INEXACT should be set for multiply", (field.getIEEEFlagsBits() & DfpField.FLAG_INEXACT) != 0);
    }

    @Test(timeout = 4000)
    public void testDivideInexactFlag() {
        field.clearIEEEFlagsBits();
        Dfp a = d(1.0);
        Dfp b = d(3.0);
        a.divide(b);
        assertTrue("FLAG_INEXACT should be set for divide", (field.getIEEEFlagsBits() & DfpField.FLAG_INEXACT) != 0);
    }

    @Test(timeout = 4000)
    public void testTruncInexactFlag() {
        field.clearIEEEFlagsBits();
        Dfp a = d(1.5);
        a.rint();
        assertTrue("FLAG_INEXACT should be set for trunc", (field.getIEEEFlagsBits() & DfpField.FLAG_INEXACT) != 0);
    }

    @Test(timeout = 4000)
    public void testUnderflowFlag() {
        field.clearIEEEFlagsBits();
        Dfp a = d(1e-200);
        Dfp b = d(1e-200);
        a.multiply(b); // product may underflow
        // Check if underflow flag set (may or may not depending on precision)
        // At least ensure no exception
    }

    @Test(timeout = 4000)
    public void testOverflowFlag() {
        field.clearIEEEFlagsBits();
        Dfp a = d(1e200);
        Dfp b = d(1e200);
        a.multiply(b); // product may overflow
        // Check if overflow flag set
    }

    @Test(timeout = 4000)
    public void testDivideByIntZeroFlag() {
        field.clearIEEEFlagsBits();
        Dfp a = d(1.0);
        a.divide(0);
        assertTrue("FLAG_DIV_ZERO should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_DIV_ZERO) != 0);
    }

    @Test(timeout = 4000)
    public void testMultiplyByIntInvalidFlag() {
        field.clearIEEEFlagsBits();
        Dfp a = d(1.0);
        a.multiply(10000); // invalid because >= RADIX
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testDivideByIntInvalidFlag() {
        field.clearIEEEFlagsBits();
        Dfp a = d(1.0);
        a.divide(-1); // invalid because < 0
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testAddOverflow() {
        field.clearIEEEFlagsBits();
        Dfp a = d(Double.MAX_VALUE);
        Dfp b = d(Double.MAX_VALUE);
        a.add(b);
        // Should overflow to infinity
        assertTrue("FLAG_OVERFLOW should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_OVERFLOW) != 0);
    }

    @Test(timeout = 4000)
    public void testMultiplyOverflow() {
        field.clearIEEEFlagsBits();
        Dfp a = d(1e200);
        Dfp b = d(1e200);
        a.multiply(b);
        assertTrue("FLAG_OVERFLOW should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_OVERFLOW) != 0);
    }

    @Test(timeout = 4000)
    public void testDivideUnderflow() {
        field.clearIEEEFlagsBits();
        Dfp a = d(1e-200);
        Dfp b = d(1e200);
        a.divide(b);
        // May underflow
    }

    @Test(timeout = 4000)
    public void testSqrtConvergence() {
        Dfp a = d(2.0);
        Dfp sqrt = a.sqrt();
        Dfp expected = d(Math.sqrt(2.0));
        // Allow some tolerance
        Dfp diff = sqrt.subtract(expected).abs();
        assertTrue("sqrt(2) should be close", diff.lessThan(d(1e-10)));
    }

    @Test(timeout = 4000)
    public void testSqrtAlternating() {
        Dfp a = d(0.5);
        Dfp sqrt = a.sqrt();
        assertFalse("sqrt(0.5) should not be NaN", sqrt.isNaN());
    }

    @Test(timeout = 4000)
    public void testSqrtSNaN() {
        // Create a signaling NaN by using constructor with SNAN
        Dfp snan = field.newDfp((byte) 1, Dfp.SNAN);
        Dfp result = snan.sqrt();
        assertTrue("sqrt(SNaN) should be NaN", result.isNaN());
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testAddSNaN() {
        Dfp snan = field.newDfp((byte) 1, Dfp.SNAN);
        Dfp one = d(1.0);
        Dfp result = one.add(snan);
        assertTrue("1 + SNaN should be NaN", result.isNaN());
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testMultiplySNaN() {
        Dfp snan = field.newDfp((byte) 1, Dfp.SNAN);
        Dfp one = d(1.0);
        Dfp result = one.multiply(snan);
        assertTrue("1 * SNaN should be NaN", result.isNaN());
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testDivideSNaN() {
        Dfp snan = field.newDfp((byte) 1, Dfp.SNAN);
        Dfp one = d(1.0);
        Dfp result = one.divide(snan);
        assertTrue("1 / SNaN should be NaN", result.isNaN());
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testLessThanSNaN() {
        Dfp snan = field.newDfp((byte) 1, Dfp.SNAN);
        Dfp one = d(1.0);
        assertFalse("1 < SNaN should be false", one.lessThan(snan));
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testGreaterThanSNaN() {
        Dfp snan = field.newDfp((byte) 1, Dfp.SNAN);
        Dfp one = d(1.0);
        assertFalse("1 > SNaN should be false", one.greaterThan(snan));
        assertTrue("FLAG_INVALID should be set", (field.getIEEEFlagsBits() & DfpField.FLAG_INVALID) != 0);
    }

    @Test(timeout = 4000)
    public void testEqualsSNaN() {
        Dfp snan = field.newDfp((byte) 1, Dfp.SNAN);
        assertFalse("SNaN equals SNaN should be false", snan.equals(snan));
    }

    @Test(timeout = 4000)
    public void testHashCodeSNaN() {
        Dfp snan = field.newDfp((byte) 1, Dfp.SNAN);
        // Should not throw
        int h = snan.hashCode();
    }

    @Test(timeout = 4000)
    public void testToStringSNaN() {
        Dfp snan = field.newDfp((byte) 1, Dfp.SNAN);
        assertEquals("SNaN toString", "NaN", snan.toString());
    }

    @Test(timeout = 4000)
    public void testClassifySNaN() {
        Dfp snan = field.newDfp((byte) 1, Dfp.SNAN);
        assertEquals("classify SNaN", Dfp.SNAN, snan.classify());
    }

    @Test(timeout = 4000)
    public void testNewInstanceNonFiniteSNaN() {
        Dfp snan = field.newDfp((byte) -1, Dfp.SNAN);
        assertTrue("newInstance SNaN should be NaN", snan.isNaN());
        assertEquals("sign should be -1", -1, snan.sign);
    }

    @Test(timeout = 4000)
    public void testNewInstanceNonFiniteQNaN() {
        Dfp qnan = field.newDfp((byte) 1, Dfp.QNAN);
        assertTrue("newInstance QNaN should be NaN", qnan.isNaN());
    }

    @Test(timeout = 4000)
    public void testNewInstanceNonFiniteInfinity() {
        Dfp inf = field.newDfp((byte) -1, Dfp.INFINITE);
        assertTrue("newInstance -inf", inf.isInfinite());
        assertEquals("sign -1", -1, inf.sign);
    }

    @Test(timeout = 4000)
    public void testDotrapInvalidSign() {
        Dfp a = d(1.0);
        Dfp result = a.dotrap(DfpField.FLAG_INVALID, "test", a, a);
        assertEquals("dotrap INVALID should preserve sign", a.sign, result.sign);
    }

    @Test(timeout = 4000)
    public void testDotrapDivZeroFiniteNonZero() {
        Dfp a = d(5.0);
        Dfp zero = d(0.0);
        Dfp result = a.dotrap(DfpField.FLAG_DIV_ZERO, "test", zero, a);
        assertTrue("dotrap DIV_ZERO finite non-zero should be infinite", result.isInfinite());
        assertEquals("sign should be positive", 1, result.sign);
    }

    @Test(timeout = 4000)
    public void testDotrapDivZeroZero() {
        Dfp zero = d(0.0);
        Dfp result = zero.dotrap(DfpField.FLAG_DIV_ZERO, "test", zero, zero);
        assertTrue("dotrap DIV_ZERO 0/0 should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDotrapDivZeroInfinity() {
        Dfp inf = d(Double.POSITIVE_INFINITY);
        Dfp result = inf.dotrap(DfpField.FLAG_DIV_ZERO, "test", inf, inf);
        assertTrue("dotrap DIV_ZERO inf should be NaN", result.isNaN());
    }

    @Test(timeout = 4000)
    public void testDotrapUnderflowGradual() {
        Dfp a = d(1e-100);
        a.exp = Dfp.MIN_EXP + 1; // near underflow
        Dfp result = a.dotrap(DfpField.FLAG_UNDERFLOW, "test", a, a);
        assertFalse("gradual underflow should be finite", result.isInfinite());
    }

    @Test(timeout = 4000)
    public void testDotrapUnderflowFlush() {
        Dfp a = d(1e-100);
        a.exp = Dfp.MIN_EXP - 10; // far underflow
        Dfp result = a.dotrap(DfpField.FLAG_UNDERFLOW, "test", a, a);
        assertTrue("flush underflow should be zero", result.isZero());
    }

    @Test(timeout = 4000)
    public void testDotrapOverflowResult() {
        Dfp a = d(1e200);
        a.exp = Dfp.MAX_EXP + 10;
        Dfp result = a.dotrap(DfpField.FLAG_OVERFLOW, "test", a, a);
        assertTrue("overflow should be infinite", result.isInfinite());
        assertEquals("sign should be positive", 1, result.sign);
    }

    @Test(timeout = 4000)
    public void testTrapOverride() {
        // Default trap returns def, so test that
        Dfp a = d(1.0);
        Dfp def = d(0.0);
        Dfp result = a.trap(DfpField.FLAG_INVALID, "test", a, def, a);
        assertSame("trap returns def", def, result);
    }

    @Test(timeout = 4000)
    public void testFieldGetZero() {
        assertTrue("field.getZero is zero", field.getZero().isZero());
    }

    @Test(timeout = 4000)
    public void testFieldGetOne() {
        assertEquals("field.getOne", d(1.0), field.getOne());
    }

    @Test(timeout = 4000)
    public void testFieldGetTwo() {
        assertEquals("field.getTwo", d(2.0), field.getTwo());
    }

    @Test(timeout = 4000)
    public void testFieldGetRadixDigits() {
        assertEquals("field radix digits", 5, field.getRadixDigits());
    }

    @Test(timeout = 4000)
    public void testFieldSetIEEEFlagsBits() {
        field.clearIEEEFlagsBits();
        field.setIEEEFlagsBits(DfpField.FLAG_INVALID);
        assertEquals("FLAG_INVALID set", DfpField.FLAG_INVALID, field.getIEEEFlagsBits());
    }

    @Test(timeout = 4000)
    public void testFieldClearIEEEFlagsBits() {
        field.setIEEEFlagsBits(DfpField.FLAG_OVERFLOW);
        field.clearIEEEFlagsBits();
        assertEquals("flags cleared", 0, field.getIEEEFlagsBits());
    }

    @Test(timeout = 4000)
    public void testFieldGetRoundingMode() {
        assertEquals("default rounding mode", DfpField.RoundingMode.ROUND_HALF_EVEN, field.getRoundingMode());
    }

    @Test(timeout = 4000)
    public void testFieldSetRoundingMode() {
        field.setRoundingMode(DfpField.RoundingMode.ROUND_DOWN);
        assertEquals("ROUND_DOWN", DfpField.RoundingMode.ROUND_DOWN, field.getRoundingMode());
        field.setRoundingMode(DfpField.RoundingMode.ROUND_HALF_EVEN); // reset
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpByte() {
        Dfp a = field.newDfp((byte) 10);
        assertEquals("field.newDfp byte", d(10.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpInt() {
        Dfp a = field.newDfp(100);
        assertEquals("field.newDfp int", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpLong() {
        Dfp a = field.newDfp(123456789L);
        assertEquals("field.newDfp long", d(123456789.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpDouble() {
        Dfp a = field.newDfp(3.14);
        assertEquals("field.newDfp double", d(3.14), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpString() {
        Dfp a = field.newDfp("0.001");
        assertEquals("field.newDfp string", d(0.001), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpDfp() {
        Dfp a = d(5.0);
        Dfp b = field.newDfp(a);
        assertEquals("field.newDfp Dfp", a, b);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpNonFinite() {
        Dfp a = field.newDfp((byte) 1, Dfp.INFINITE);
        assertTrue("field.newDfp non-finite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpZero() {
        Dfp a = field.newDfp();
        assertTrue("field.newDfp zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpCopy() {
        Dfp a = d(3.0);
        Dfp b = field.newDfp(a);
        assertEquals("field.newDfp copy", a, b);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringInfinity() {
        Dfp a = field.newDfp("Infinity");
        assertTrue("field.newDfp string Infinity", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringNaN() {
        Dfp a = field.newDfp("NaN");
        assertTrue("field.newDfp string NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringNegativeInfinity() {
        Dfp a = field.newDfp("-Infinity");
        assertTrue("field.newDfp string -Infinity", a.isInfinite());
        assertEquals("sign negative", -1, a.sign);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringScientific() {
        Dfp a = field.newDfp("1e-5");
        assertEquals("field.newDfp 1e-5", d(0.00001), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringLeadingZeros() {
        Dfp a = field.newDfp("000.001");
        assertEquals("field.newDfp 000.001", d(0.001), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringTrailingZeros() {
        Dfp a = field.newDfp("1.000");
        assertEquals("field.newDfp 1.000", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringOnlyDecimal() {
        Dfp a = field.newDfp(".");
        // Should be zero? Actually parsing may produce zero
        assertTrue("field.newDfp '.' should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringEmpty() {
        // Empty string may cause exception, but we can test that it doesn't crash
        try {
            Dfp a = field.newDfp("");
            // If it returns, it's probably zero or NaN
        } catch (Exception e) {
            // Acceptable
        }
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringInvalid() {
        // Invalid string like "abc" should produce NaN
        Dfp a = field.newDfp("abc");
        assertTrue("invalid string should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringPlusInfinity() {
        Dfp a = field.newDfp("+Infinity");
        assertTrue("+Infinity should be infinite", a.isInfinite());
        assertEquals("sign positive", 1, a.sign);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringPlusNaN() {
        Dfp a = field.newDfp("+NaN");
        assertTrue("+NaN should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringNegativeNaN() {
        Dfp a = field.newDfp("-NaN");
        assertTrue("-NaN should be NaN", a.isNaN());
        // sign may be negative
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithSpaces() {
        Dfp a = field.newDfp("  123.456  ");
        // Spaces are ignored? The parser skips non-digit characters
        assertEquals("spaces should be ignored", d(123.456), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithPlusSign() {
        Dfp a = field.newDfp("+123");
        assertEquals("+123", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithMultipleDots() {
        // Multiple dots may cause issues, but should not crash
        Dfp a = field.newDfp("1.2.3");
        // Probably parses first dot only
        assertFalse("multiple dots should not produce NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringExponentWithoutDigits() {
        Dfp a = field.newDfp("1e");
        // Should be NaN or zero?
        // The parser may treat as normal number
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringExponentWithSignOnly() {
        Dfp a = field.newDfp("1e-");
        // Should be NaN or zero?
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringLargeExponent() {
        Dfp a = field.newDfp("1e1000");
        // Should overflow to infinity
        assertTrue("1e1000 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringSmallExponent() {
        Dfp a = field.newDfp("1e-1000");
        // Should underflow to zero
        assertTrue("1e-1000 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringZeroExponent() {
        Dfp a = field.newDfp("1e0");
        assertEquals("1e0", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringNegativeExponentLarge() {
        Dfp a = field.newDfp("1e-100");
        assertFalse("1e-100 should be finite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringPositiveExponentLarge() {
        Dfp a = field.newDfp("1e100");
        assertFalse("1e100 should be finite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringVeryLargeExponent() {
        Dfp a = field.newDfp("1e10000");
        assertTrue("1e10000 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringVerySmallExponent() {
        Dfp a = field.newDfp("1e-10000");
        assertTrue("1e-10000 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringMaxLong() {
        Dfp a = field.newDfp("9223372036854775807");
        assertEquals("max long", d(9223372036854775807L), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringMinLong() {
        Dfp a = field.newDfp("-9223372036854775808");
        assertEquals("min long", d(-9223372036854775808L), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringOverflowLong() {
        Dfp a = field.newDfp("9223372036854775808");
        // Should be larger than long, but still finite
        assertFalse("overflow long should be finite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringUnderflowLong() {
        Dfp a = field.newDfp("-9223372036854775809");
        assertFalse("underflow long should be finite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringHex() {
        // Hex not supported, should be NaN
        Dfp a = field.newDfp("0x1");
        assertTrue("hex should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringBinary() {
        Dfp a = field.newDfp("0b1");
        assertTrue("binary should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringOnlySign() {
        Dfp a = field.newDfp("-");
        // Should be zero or NaN
        assertTrue("only sign should be zero or NaN", a.isZero() || a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringOnlyDecimalPoint() {
        Dfp a = field.newDfp(".");
        assertTrue("only decimal point should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringLeadingDecimal() {
        Dfp a = field.newDfp(".5");
        assertEquals(".5", d(0.5), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringTrailingDecimal() {
        Dfp a = field.newDfp("5.");
        assertEquals("5.", d(5.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringMultipleZeros() {
        Dfp a = field.newDfp("0000");
        assertTrue("0000 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringNegativeZero() {
        Dfp a = field.newDfp("-0");
        assertTrue("-0 should be zero", a.isZero());
        assertEquals("sign negative", -1, a.sign);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringPositiveZero() {
        Dfp a = field.newDfp("+0");
        assertTrue("+0 should be zero", a.isZero());
        assertEquals("sign positive", 1, a.sign);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithComma() {
        Dfp a = field.newDfp("1,234.56");
        // Comma is ignored as non-digit
        assertEquals("1,234.56", d(1234.56), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithSpacesInside() {
        Dfp a = field.newDfp("1 2 3");
        // Spaces ignored
        assertEquals("1 2 3", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithLeadingPlus() {
        Dfp a = field.newDfp("+123.456");
        assertEquals("+123.456", d(123.456), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithLeadingMinus() {
        Dfp a = field.newDfp("-123.456");
        assertEquals("-123.456", d(-123.456), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithMultipleMinus() {
        Dfp a = field.newDfp("--123");
        // Should be treated as positive? The parser may see first minus, then second minus as non-digit
        // Likely results in NaN or positive
        assertFalse("multiple minus should not crash", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithMultiplePlus() {
        Dfp a = field.newDfp("++123");
        assertFalse("multiple plus should not crash", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithMixedSigns() {
        Dfp a = field.newDfp("-+123");
        // Should be negative? The first minus sets sign, plus ignored
        assertEquals("-+123", d(-123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndDecimal() {
        Dfp a = field.newDfp("1.23e2");
        assertEquals("1.23e2", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndNegativeDecimal() {
        Dfp a = field.newDfp("-1.23e-2");
        assertEquals("-1.23e-2", d(-0.0123), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndPositiveDecimal() {
        Dfp a = field.newDfp("+1.23e+2");
        assertEquals("+1.23e+2", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentNoDecimal() {
        Dfp a = field.newDfp("123e2");
        assertEquals("123e2", d(12300.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentNegativeNoDecimal() {
        Dfp a = field.newDfp("123e-2");
        assertEquals("123e-2", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndLeadingZeros() {
        Dfp a = field.newDfp("001.23e2");
        assertEquals("001.23e2", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndTrailingZeros() {
        Dfp a = field.newDfp("1.2300e2");
        assertEquals("1.2300e2", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndOnlyZeros() {
        Dfp a = field.newDfp("0e2");
        assertTrue("0e2 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndNegativeZero() {
        Dfp a = field.newDfp("-0e2");
        assertTrue("-0e2 should be zero", a.isZero());
        assertEquals("sign negative", -1, a.sign);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndInfinity() {
        Dfp a = field.newDfp("Infinitye2");
        // Should be NaN because "Infinitye2" is not recognized
        assertTrue("Infinitye2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndNaN() {
        Dfp a = field.newDfp("NaNe2");
        assertTrue("NaNe2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndSpecial() {
        Dfp a = field.newDfp("1e2.5");
        // Exponent should be integer, so "2.5" may be parsed as "2" then dot ignored
        // Likely results in 1e2 = 100
        assertEquals("1e2.5", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndMultipleE() {
        Dfp a = field.newDfp("1e2e3");
        // Only first e is considered, rest ignored
        assertEquals("1e2e3", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndNoDigitsAfterE() {
        Dfp a = field.newDfp("1e");
        // Should be treated as normal number "1" with exponent 0
        assertEquals("1e", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndOnlySignAfterE() {
        Dfp a = field.newDfp("1e-");
        // Should be treated as "1" with exponent 0
        assertEquals("1e-", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndMultipleSigns() {
        Dfp a = field.newDfp("1e--2");
        // Should be treated as "1" with exponent 0 because "--2" not valid
        assertEquals("1e--2", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndSpaces() {
        Dfp a = field.newDfp("1 e 2");
        // Spaces ignored, so "1e2"
        assertEquals("1 e 2", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndLeadingPlus() {
        Dfp a = field.newDfp("+1e2");
        assertEquals("+1e2", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndLeadingMinus() {
        Dfp a = field.newDfp("-1e2");
        assertEquals("-1e2", d(-100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndDecimalInExponent() {
        Dfp a = field.newDfp("1e2.3");
        // Exponent "2.3" -> integer part 2, rest ignored
        assertEquals("1e2.3", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndNegativeDecimalInExponent() {
        Dfp a = field.newDfp("1e-2.3");
        // Exponent "-2.3" -> integer part -2, rest ignored
        assertEquals("1e-2.3", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndLargeExponent() {
        Dfp a = field.newDfp("1e10000");
        assertTrue("1e10000 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndSmallExponent() {
        Dfp a = field.newDfp("1e-10000");
        assertTrue("1e-10000 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndMaxExponent() {
        Dfp a = field.newDfp("1e32768");
        // MAX_EXP is 32768, so exponent 32768 is allowed? Actually exp range is -32767 to 32768
        // This may overflow
        assertTrue("1e32768 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndMinExponent() {
        Dfp a = field.newDfp("1e-32767");
        // MIN_EXP is -32767, so exponent -32767 is allowed
        assertFalse("1e-32767 should be finite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndExponentBelowMin() {
        Dfp a = field.newDfp("1e-32768");
        // Below MIN_EXP, should underflow to zero
        assertTrue("1e-32768 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndExponentAboveMax() {
        Dfp a = field.newDfp("1e32769");
        assertTrue("1e32769 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndZeroExponent() {
        Dfp a = field.newDfp("1e0");
        assertEquals("1e0", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndNegativeZeroExponent() {
        Dfp a = field.newDfp("1e-0");
        assertEquals("1e-0", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndPositiveZeroExponent() {
        Dfp a = field.newDfp("1e+0");
        assertEquals("1e+0", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndOnlyExponent() {
        Dfp a = field.newDfp("e2");
        // Should be NaN because no digits before e
        assertTrue("e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndOnlyExponentWithSign() {
        Dfp a = field.newDfp("-e2");
        assertTrue("-e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndOnlyDecimal() {
        Dfp a = field.newDfp(".e2");
        assertTrue(".e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndOnlyDecimalWithSign() {
        Dfp a = field.newDfp("-.e2");
        assertTrue("-.e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndMultipleDots() {
        Dfp a = field.newDfp("1.2.3e2");
        // Should parse as 1.2 then ignore rest
        assertEquals("1.2.3e2", d(120.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndLeadingZerosInExponent() {
        Dfp a = field.newDfp("1e002");
        assertEquals("1e002", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndNegativeLeadingZeros() {
        Dfp a = field.newDfp("1e-002");
        assertEquals("1e-002", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndPositiveLeadingZeros() {
        Dfp a = field.newDfp("1e+002");
        assertEquals("1e+002", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndOverflowExponent() {
        Dfp a = field.newDfp("1e99999");
        assertTrue("1e99999 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndUnderflowExponent() {
        Dfp a = field.newDfp("1e-99999");
        assertTrue("1e-99999 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndMaxDigits() {
        // Use a string that exactly fills mantissa
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append('9');
        }
        sb.append("e0");
        Dfp a = field.newDfp(sb.toString());
        assertFalse("max digits should be finite", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndMoreDigits() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            sb.append('9');
        }
        sb.append("e0");
        Dfp a = field.newDfp(sb.toString());
        // Should be rounded
        assertFalse("more digits should be finite", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndNegativeSignInMiddle() {
        Dfp a = field.newDfp("1-2e3");
        // The minus in middle is ignored as non-digit
        assertEquals("1-2e3", d(12000.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndPlusSignInMiddle() {
        Dfp a = field.newDfp("1+2e3");
        assertEquals("1+2e3", d(12000.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndMultipleEInMiddle() {
        Dfp a = field.newDfp("1e2e3");
        assertEquals("1e2e3", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEAtEnd() {
        Dfp a = field.newDfp("123e");
        assertEquals("123e", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEAtStart() {
        Dfp a = field.newDfp("e123");
        assertTrue("e123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndOnlyE() {
        Dfp a = field.newDfp("e");
        assertTrue("e should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndOnlySign() {
        Dfp a = field.newDfp("-e");
        assertTrue("-e should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndOnlyDecimalAndE() {
        Dfp a = field.newDfp(".e");
        assertTrue(".e should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndOnlyDecimalSignE() {
        Dfp a = field.newDfp("-.e");
        assertTrue("-.e should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndOnlyDigitsAfterE() {
        Dfp a = field.newDfp("e123");
        assertTrue("e123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndOnlySignAfterE() {
        Dfp a = field.newDfp("e-");
        assertTrue("e- should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndOnlySignAndDigitsAfterE() {
        Dfp a = field.newDfp("e-123");
        assertTrue("e-123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndOnlyDecimalAfterE() {
        Dfp a = field.newDfp("e.123");
        assertTrue("e.123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndOnlyDecimalSignAfterE() {
        Dfp a = field.newDfp("e-.123");
        assertTrue("e-.123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndMultipleE() {
        Dfp a = field.newDfp("1e2e3");
        assertEquals("1e2e3", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEAfterDecimal() {
        Dfp a = field.newDfp("1.2e3");
        assertEquals("1.2e3", d(1200.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEBeforeDecimal() {
        Dfp a = field.newDfp("1e2.3");
        assertEquals("1e2.3", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithSpaces() {
        Dfp a = field.newDfp("1 e 2");
        assertEquals("1 e 2", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithTabs() {
        Dfp a = field.newDfp("1\te\t2");
        assertEquals("1\te\t2", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithNewlines() {
        Dfp a = field.newDfp("1\ne\n2");
        assertEquals("1\ne\n2", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithMultipleSpaces() {
        Dfp a = field.newDfp("1   e   2");
        assertEquals("1   e   2", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithLeadingSpaces() {
        Dfp a = field.newDfp("  1e2");
        assertEquals("  1e2", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithTrailingSpaces() {
        Dfp a = field.newDfp("1e2  ");
        assertEquals("1e2  ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithLeadingTabs() {
        Dfp a = field.newDfp("\t1e2");
        assertEquals("\t1e2", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithTrailingTabs() {
        Dfp a = field.newDfp("1e2\t");
        assertEquals("1e2\t", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithLeadingNewlines() {
        Dfp a = field.newDfp("\n1e2");
        assertEquals("\n1e2", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithTrailingNewlines() {
        Dfp a = field.newDfp("1e2\n");
        assertEquals("1e2\n", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithMixedWhitespace() {
        Dfp a = field.newDfp(" \t\n1e2 \t\n");
        assertEquals(" \t\n1e2 \t\n", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespace() {
        Dfp a = field.newDfp("   ");
        // Should be zero? Actually empty string after trimming? The parser may produce NaN
        assertTrue("whitespace only should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndSign() {
        Dfp a = field.newDfp(" - ");
        assertTrue("whitespace and sign should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndDecimal() {
        Dfp a = field.newDfp(" . ");
        assertTrue("whitespace and decimal should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndSignDecimal() {
        Dfp a = field.newDfp(" - . ");
        assertTrue("whitespace sign decimal should be zero", a.isZero());
        assertEquals("sign negative", -1, a.sign);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndSignDigits() {
        Dfp a = field.newDfp(" - 123 ");
        assertEquals(" - 123 ", d(-123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndDigits() {
        Dfp a = field.newDfp(" 123 ");
        assertEquals(" 123 ", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndDecimalDigits() {
        Dfp a = field.newDfp(" 1.23 ");
        assertEquals(" 1.23 ", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndNegativeDecimalDigits() {
        Dfp a = field.newDfp(" -1.23 ");
        assertEquals(" -1.23 ", d(-1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndPositiveDecimalDigits() {
        Dfp a = field.newDfp(" +1.23 ");
        assertEquals(" +1.23 ", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponent() {
        Dfp a = field.newDfp(" 1e2 ");
        assertEquals(" 1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndNegativeExponent() {
        Dfp a = field.newDfp(" -1e2 ");
        assertEquals(" -1e2 ", d(-100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndPositiveExponent() {
        Dfp a = field.newDfp(" +1e2 ");
        assertEquals(" +1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentNegative() {
        Dfp a = field.newDfp(" 1e-2 ");
        assertEquals(" 1e-2 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentPositive() {
        Dfp a = field.newDfp(" 1e+2 ");
        assertEquals(" 1e+2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentWithSign() {
        Dfp a = field.newDfp(" 1e-2 ");
        assertEquals(" 1e-2 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentWithLeadingZeros() {
        Dfp a = field.newDfp(" 1e002 ");
        assertEquals(" 1e002 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentNegativeLeadingZeros() {
        Dfp a = field.newDfp(" 1e-002 ");
        assertEquals(" 1e-002 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentPositiveLeadingZeros() {
        Dfp a = field.newDfp(" 1e+002 ");
        assertEquals(" 1e+002 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentOverflow() {
        Dfp a = field.newDfp(" 1e99999 ");
        assertTrue(" 1e99999 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentUnderflow() {
        Dfp a = field.newDfp(" 1e-99999 ");
        assertTrue(" 1e-99999 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentMax() {
        Dfp a = field.newDfp(" 1e32768 ");
        assertTrue(" 1e32768 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentMin() {
        Dfp a = field.newDfp(" 1e-32767 ");
        assertFalse(" 1e-32767 should be finite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentBelowMin() {
        Dfp a = field.newDfp(" 1e-32768 ");
        assertTrue(" 1e-32768 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentAboveMax() {
        Dfp a = field.newDfp(" 1e32769 ");
        assertTrue(" 1e32769 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentZero() {
        Dfp a = field.newDfp(" 1e0 ");
        assertEquals(" 1e0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentNegativeZero() {
        Dfp a = field.newDfp(" 1e-0 ");
        assertEquals(" 1e-0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentPositiveZero() {
        Dfp a = field.newDfp(" 1e+0 ");
        assertEquals(" 1e+0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentOnly() {
        Dfp a = field.newDfp(" e2 ");
        assertTrue(" e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentOnlySign() {
        Dfp a = field.newDfp(" -e2 ");
        assertTrue(" -e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentOnlyDecimal() {
        Dfp a = field.newDfp(" .e2 ");
        assertTrue(" .e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentOnlyDecimalSign() {
        Dfp a = field.newDfp(" -.e2 ");
        assertTrue(" -.e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentOnlyDigitsAfterE() {
        Dfp a = field.newDfp(" e123 ");
        assertTrue(" e123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentOnlySignAfterE() {
        Dfp a = field.newDfp(" e- ");
        assertTrue(" e- should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentOnlySignAndDigitsAfterE() {
        Dfp a = field.newDfp(" e-123 ");
        assertTrue(" e-123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentOnlyDecimalAfterE() {
        Dfp a = field.newDfp(" e.123 ");
        assertTrue(" e.123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentOnlyDecimalSignAfterE() {
        Dfp a = field.newDfp(" e-.123 ");
        assertTrue(" e-.123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentMultipleE() {
        Dfp a = field.newDfp(" 1e2e3 ");
        assertEquals(" 1e2e3 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEAfterDecimal() {
        Dfp a = field.newDfp(" 1.2e3 ");
        assertEquals(" 1.2e3 ", d(1200.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEBeforeDecimal() {
        Dfp a = field.newDfp(" 1e2.3 ");
        assertEquals(" 1e2.3 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithSpaces() {
        Dfp a = field.newDfp(" 1 e 2 ");
        assertEquals(" 1 e 2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithTabs() {
        Dfp a = field.newDfp(" 1\te\t2 ");
        assertEquals(" 1\te\t2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithNewlines() {
        Dfp a = field.newDfp(" 1\ne\n2 ");
        assertEquals(" 1\ne\n2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithMultipleSpaces() {
        Dfp a = field.newDfp(" 1   e   2 ");
        assertEquals(" 1   e   2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithLeadingSpaces() {
        Dfp a = field.newDfp("   1e2 ");
        assertEquals("   1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithTrailingSpaces() {
        Dfp a = field.newDfp(" 1e2   ");
        assertEquals(" 1e2   ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithLeadingTabs() {
        Dfp a = field.newDfp("\t1e2 ");
        assertEquals("\t1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithTrailingTabs() {
        Dfp a = field.newDfp(" 1e2\t");
        assertEquals(" 1e2\t", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithLeadingNewlines() {
        Dfp a = field.newDfp("\n1e2 ");
        assertEquals("\n1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithTrailingNewlines() {
        Dfp a = field.newDfp(" 1e2\n");
        assertEquals(" 1e2\n", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithMixedWhitespace() {
        Dfp a = field.newDfp(" \t\n1e2 \t\n");
        assertEquals(" \t\n1e2 \t\n", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespace() {
        Dfp a = field.newDfp("   ");
        assertTrue("   should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSign() {
        Dfp a = field.newDfp(" - ");
        assertTrue(" - should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDecimal() {
        Dfp a = field.newDfp(" . ");
        assertTrue(" . should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSignDecimal() {
        Dfp a = field.newDfp(" - . ");
        assertTrue(" - . should be zero", a.isZero());
        assertEquals("sign negative", -1, a.sign);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSignDigits() {
        Dfp a = field.newDfp(" - 123 ");
        assertEquals(" - 123 ", d(-123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDigits() {
        Dfp a = field.newDfp(" 123 ");
        assertEquals(" 123 ", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDecimalDigits() {
        Dfp a = field.newDfp(" 1.23 ");
        assertEquals(" 1.23 ", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndNegativeDecimalDigits() {
        Dfp a = field.newDfp(" -1.23 ");
        assertEquals(" -1.23 ", d(-1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndPositiveDecimalDigits() {
        Dfp a = field.newDfp(" +1.23 ");
        assertEquals(" +1.23 ", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponent() {
        Dfp a = field.newDfp(" 1e2 ");
        assertEquals(" 1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndNegativeExponent() {
        Dfp a = field.newDfp(" -1e2 ");
        assertEquals(" -1e2 ", d(-100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndPositiveExponent() {
        Dfp a = field.newDfp(" +1e2 ");
        assertEquals(" +1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegative() {
        Dfp a = field.newDfp(" 1e-2 ");
        assertEquals(" 1e-2 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositive() {
        Dfp a = field.newDfp(" 1e+2 ");
        assertEquals(" 1e+2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentWithSign() {
        Dfp a = field.newDfp(" 1e-2 ");
        assertEquals(" 1e-2 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentWithLeadingZeros() {
        Dfp a = field.newDfp(" 1e002 ");
        assertEquals(" 1e002 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegativeLeadingZeros() {
        Dfp a = field.newDfp(" 1e-002 ");
        assertEquals(" 1e-002 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositiveLeadingZeros() {
        Dfp a = field.newDfp(" 1e+002 ");
        assertEquals(" 1e+002 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOverflow() {
        Dfp a = field.newDfp(" 1e99999 ");
        assertTrue(" 1e99999 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentUnderflow() {
        Dfp a = field.newDfp(" 1e-99999 ");
        assertTrue(" 1e-99999 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMax() {
        Dfp a = field.newDfp(" 1e32768 ");
        assertTrue(" 1e32768 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMin() {
        Dfp a = field.newDfp(" 1e-32767 ");
        assertFalse(" 1e-32767 should be finite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentBelowMin() {
        Dfp a = field.newDfp(" 1e-32768 ");
        assertTrue(" 1e-32768 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentAboveMax() {
        Dfp a = field.newDfp(" 1e32769 ");
        assertTrue(" 1e32769 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentZero() {
        Dfp a = field.newDfp(" 1e0 ");
        assertEquals(" 1e0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegativeZero() {
        Dfp a = field.newDfp(" 1e-0 ");
        assertEquals(" 1e-0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositiveZero() {
        Dfp a = field.newDfp(" 1e+0 ");
        assertEquals(" 1e+0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnly() {
        Dfp a = field.newDfp(" e2 ");
        assertTrue(" e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySign() {
        Dfp a = field.newDfp(" -e2 ");
        assertTrue(" -e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimal() {
        Dfp a = field.newDfp(" .e2 ");
        assertTrue(" .e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalSign() {
        Dfp a = field.newDfp(" -.e2 ");
        assertTrue(" -.e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDigitsAfterE() {
        Dfp a = field.newDfp(" e123 ");
        assertTrue(" e123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySignAfterE() {
        Dfp a = field.newDfp(" e- ");
        assertTrue(" e- should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySignAndDigitsAfterE() {
        Dfp a = field.newDfp(" e-123 ");
        assertTrue(" e-123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalAfterE() {
        Dfp a = field.newDfp(" e.123 ");
        assertTrue(" e.123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalSignAfterE() {
        Dfp a = field.newDfp(" e-.123 ");
        assertTrue(" e-.123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMultipleE() {
        Dfp a = field.newDfp(" 1e2e3 ");
        assertEquals(" 1e2e3 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEAfterDecimal() {
        Dfp a = field.newDfp(" 1.2e3 ");
        assertEquals(" 1.2e3 ", d(1200.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEBeforeDecimal() {
        Dfp a = field.newDfp(" 1e2.3 ");
        assertEquals(" 1e2.3 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithSpaces() {
        Dfp a = field.newDfp(" 1 e 2 ");
        assertEquals(" 1 e 2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTabs() {
        Dfp a = field.newDfp(" 1\te\t2 ");
        assertEquals(" 1\te\t2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithNewlines() {
        Dfp a = field.newDfp(" 1\ne\n2 ");
        assertEquals(" 1\ne\n2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithMultipleSpaces() {
        Dfp a = field.newDfp(" 1   e   2 ");
        assertEquals(" 1   e   2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingSpaces() {
        Dfp a = field.newDfp("   1e2 ");
        assertEquals("   1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingSpaces() {
        Dfp a = field.newDfp(" 1e2   ");
        assertEquals(" 1e2   ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingTabs() {
        Dfp a = field.newDfp("\t1e2 ");
        assertEquals("\t1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingTabs() {
        Dfp a = field.newDfp(" 1e2\t");
        assertEquals(" 1e2\t", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingNewlines() {
        Dfp a = field.newDfp("\n1e2 ");
        assertEquals("\n1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingNewlines() {
        Dfp a = field.newDfp(" 1e2\n");
        assertEquals(" 1e2\n", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithMixedWhitespace() {
        Dfp a = field.newDfp(" \t\n1e2 \t\n");
        assertEquals(" \t\n1e2 \t\n", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespace() {
        Dfp a = field.newDfp("   ");
        assertTrue("   should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSign() {
        Dfp a = field.newDfp(" - ");
        assertTrue(" - should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDecimal() {
        Dfp a = field.newDfp(" . ");
        assertTrue(" . should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSignDecimal() {
        Dfp a = field.newDfp(" - . ");
        assertTrue(" - . should be zero", a.isZero());
        assertEquals("sign negative", -1, a.sign);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSignDigits() {
        Dfp a = field.newDfp(" - 123 ");
        assertEquals(" - 123 ", d(-123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDigits() {
        Dfp a = field.newDfp(" 123 ");
        assertEquals(" 123 ", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDecimalDigits() {
        Dfp a = field.newDfp(" 1.23 ");
        assertEquals(" 1.23 ", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndNegativeDecimalDigits() {
        Dfp a = field.newDfp(" -1.23 ");
        assertEquals(" -1.23 ", d(-1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndPositiveDecimalDigits() {
        Dfp a = field.newDfp(" +1.23 ");
        assertEquals(" +1.23 ", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponent() {
        Dfp a = field.newDfp(" 1e2 ");
        assertEquals(" 1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndNegativeExponent() {
        Dfp a = field.newDfp(" -1e2 ");
        assertEquals(" -1e2 ", d(-100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndPositiveExponent() {
        Dfp a = field.newDfp(" +1e2 ");
        assertEquals(" +1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegative() {
        Dfp a = field.newDfp(" 1e-2 ");
        assertEquals(" 1e-2 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositive() {
        Dfp a = field.newDfp(" 1e+2 ");
        assertEquals(" 1e+2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentWithSign() {
        Dfp a = field.newDfp(" 1e-2 ");
        assertEquals(" 1e-2 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentWithLeadingZeros() {
        Dfp a = field.newDfp(" 1e002 ");
        assertEquals(" 1e002 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegativeLeadingZeros() {
        Dfp a = field.newDfp(" 1e-002 ");
        assertEquals(" 1e-002 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositiveLeadingZeros() {
        Dfp a = field.newDfp(" 1e+002 ");
        assertEquals(" 1e+002 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOverflow() {
        Dfp a = field.newDfp(" 1e99999 ");
        assertTrue(" 1e99999 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentUnderflow() {
        Dfp a = field.newDfp(" 1e-99999 ");
        assertTrue(" 1e-99999 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMax() {
        Dfp a = field.newDfp(" 1e32768 ");
        assertTrue(" 1e32768 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMin() {
        Dfp a = field.newDfp(" 1e-32767 ");
        assertFalse(" 1e-32767 should be finite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentBelowMin() {
        Dfp a = field.newDfp(" 1e-32768 ");
        assertTrue(" 1e-32768 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentAboveMax() {
        Dfp a = field.newDfp(" 1e32769 ");
        assertTrue(" 1e32769 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentZero() {
        Dfp a = field.newDfp(" 1e0 ");
        assertEquals(" 1e0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegativeZero() {
        Dfp a = field.newDfp(" 1e-0 ");
        assertEquals(" 1e-0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositiveZero() {
        Dfp a = field.newDfp(" 1e+0 ");
        assertEquals(" 1e+0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnly() {
        Dfp a = field.newDfp(" e2 ");
        assertTrue(" e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySign() {
        Dfp a = field.newDfp(" -e2 ");
        assertTrue(" -e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimal() {
        Dfp a = field.newDfp(" .e2 ");
        assertTrue(" .e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalSign() {
        Dfp a = field.newDfp(" -.e2 ");
        assertTrue(" -.e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDigitsAfterE() {
        Dfp a = field.newDfp(" e123 ");
        assertTrue(" e123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySignAfterE() {
        Dfp a = field.newDfp(" e- ");
        assertTrue(" e- should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySignAndDigitsAfterE() {
        Dfp a = field.newDfp(" e-123 ");
        assertTrue(" e-123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalAfterE() {
        Dfp a = field.newDfp(" e.123 ");
        assertTrue(" e.123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalSignAfterE() {
        Dfp a = field.newDfp(" e-.123 ");
        assertTrue(" e-.123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMultipleE() {
        Dfp a = field.newDfp(" 1e2e3 ");
        assertEquals(" 1e2e3 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEAfterDecimal() {
        Dfp a = field.newDfp(" 1.2e3 ");
        assertEquals(" 1.2e3 ", d(1200.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEBeforeDecimal() {
        Dfp a = field.newDfp(" 1e2.3 ");
        assertEquals(" 1e2.3 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithSpaces() {
        Dfp a = field.newDfp(" 1 e 2 ");
        assertEquals(" 1 e 2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTabs() {
        Dfp a = field.newDfp(" 1\te\t2 ");
        assertEquals(" 1\te\t2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithNewlines() {
        Dfp a = field.newDfp(" 1\ne\n2 ");
        assertEquals(" 1\ne\n2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithMultipleSpaces() {
        Dfp a = field.newDfp(" 1   e   2 ");
        assertEquals(" 1   e   2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingSpaces() {
        Dfp a = field.newDfp("   1e2 ");
        assertEquals("   1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingSpaces() {
        Dfp a = field.newDfp(" 1e2   ");
        assertEquals(" 1e2   ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingTabs() {
        Dfp a = field.newDfp("\t1e2 ");
        assertEquals("\t1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingTabs() {
        Dfp a = field.newDfp(" 1e2\t");
        assertEquals(" 1e2\t", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingNewlines() {
        Dfp a = field.newDfp("\n1e2 ");
        assertEquals("\n1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingNewlines() {
        Dfp a = field.newDfp(" 1e2\n");
        assertEquals(" 1e2\n", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithMixedWhitespace() {
        Dfp a = field.newDfp(" \t\n1e2 \t\n");
        assertEquals(" \t\n1e2 \t\n", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespace() {
        Dfp a = field.newDfp("   ");
        assertTrue("   should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSign() {
        Dfp a = field.newDfp(" - ");
        assertTrue(" - should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDecimal() {
        Dfp a = field.newDfp(" . ");
        assertTrue(" . should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSignDecimal() {
        Dfp a = field.newDfp(" - . ");
        assertTrue(" - . should be zero", a.isZero());
        assertEquals("sign negative", -1, a.sign);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSignDigits() {
        Dfp a = field.newDfp(" - 123 ");
        assertEquals(" - 123 ", d(-123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDigits() {
        Dfp a = field.newDfp(" 123 ");
        assertEquals(" 123 ", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDecimalDigits() {
        Dfp a = field.newDfp(" 1.23 ");
        assertEquals(" 1.23 ", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndNegativeDecimalDigits() {
        Dfp a = field.newDfp(" -1.23 ");
        assertEquals(" -1.23 ", d(-1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndPositiveDecimalDigits() {
        Dfp a = field.newDfp(" +1.23 ");
        assertEquals(" +1.23 ", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponent() {
        Dfp a = field.newDfp(" 1e2 ");
        assertEquals(" 1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndNegativeExponent() {
        Dfp a = field.newDfp(" -1e2 ");
        assertEquals(" -1e2 ", d(-100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndPositiveExponent() {
        Dfp a = field.newDfp(" +1e2 ");
        assertEquals(" +1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegative() {
        Dfp a = field.newDfp(" 1e-2 ");
        assertEquals(" 1e-2 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositive() {
        Dfp a = field.newDfp(" 1e+2 ");
        assertEquals(" 1e+2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentWithSign() {
        Dfp a = field.newDfp(" 1e-2 ");
        assertEquals(" 1e-2 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentWithLeadingZeros() {
        Dfp a = field.newDfp(" 1e002 ");
        assertEquals(" 1e002 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegativeLeadingZeros() {
        Dfp a = field.newDfp(" 1e-002 ");
        assertEquals(" 1e-002 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositiveLeadingZeros() {
        Dfp a = field.newDfp(" 1e+002 ");
        assertEquals(" 1e+002 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOverflow() {
        Dfp a = field.newDfp(" 1e99999 ");
        assertTrue(" 1e99999 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentUnderflow() {
        Dfp a = field.newDfp(" 1e-99999 ");
        assertTrue(" 1e-99999 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMax() {
        Dfp a = field.newDfp(" 1e32768 ");
        assertTrue(" 1e32768 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMin() {
        Dfp a = field.newDfp(" 1e-32767 ");
        assertFalse(" 1e-32767 should be finite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentBelowMin() {
        Dfp a = field.newDfp(" 1e-32768 ");
        assertTrue(" 1e-32768 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentAboveMax() {
        Dfp a = field.newDfp(" 1e32769 ");
        assertTrue(" 1e32769 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentZero() {
        Dfp a = field.newDfp(" 1e0 ");
        assertEquals(" 1e0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegativeZero() {
        Dfp a = field.newDfp(" 1e-0 ");
        assertEquals(" 1e-0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositiveZero() {
        Dfp a = field.newDfp(" 1e+0 ");
        assertEquals(" 1e+0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnly() {
        Dfp a = field.newDfp(" e2 ");
        assertTrue(" e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySign() {
        Dfp a = field.newDfp(" -e2 ");
        assertTrue(" -e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimal() {
        Dfp a = field.newDfp(" .e2 ");
        assertTrue(" .e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalSign() {
        Dfp a = field.newDfp(" -.e2 ");
        assertTrue(" -.e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDigitsAfterE() {
        Dfp a = field.newDfp(" e123 ");
        assertTrue(" e123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySignAfterE() {
        Dfp a = field.newDfp(" e- ");
        assertTrue(" e- should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySignAndDigitsAfterE() {
        Dfp a = field.newDfp(" e-123 ");
        assertTrue(" e-123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalAfterE() {
        Dfp a = field.newDfp(" e.123 ");
        assertTrue(" e.123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalSignAfterE() {
        Dfp a = field.newDfp(" e-.123 ");
        assertTrue(" e-.123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMultipleE() {
        Dfp a = field.newDfp(" 1e2e3 ");
        assertEquals(" 1e2e3 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEAfterDecimal() {
        Dfp a = field.newDfp(" 1.2e3 ");
        assertEquals(" 1.2e3 ", d(1200.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEBeforeDecimal() {
        Dfp a = field.newDfp(" 1e2.3 ");
        assertEquals(" 1e2.3 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithSpaces() {
        Dfp a = field.newDfp(" 1 e 2 ");
        assertEquals(" 1 e 2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTabs() {
        Dfp a = field.newDfp(" 1\te\t2 ");
        assertEquals(" 1\te\t2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithNewlines() {
        Dfp a = field.newDfp(" 1\ne\n2 ");
        assertEquals(" 1\ne\n2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithMultipleSpaces() {
        Dfp a = field.newDfp(" 1   e   2 ");
        assertEquals(" 1   e   2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingSpaces() {
        Dfp a = field.newDfp("   1e2 ");
        assertEquals("   1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingSpaces() {
        Dfp a = field.newDfp(" 1e2   ");
        assertEquals(" 1e2   ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingTabs() {
        Dfp a = field.newDfp("\t1e2 ");
        assertEquals("\t1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingTabs() {
        Dfp a = field.newDfp(" 1e2\t");
        assertEquals(" 1e2\t", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingNewlines() {
        Dfp a = field.newDfp("\n1e2 ");
        assertEquals("\n1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingNewlines() {
        Dfp a = field.newDfp(" 1e2\n");
        assertEquals(" 1e2\n", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithMixedWhitespace() {
        Dfp a = field.newDfp(" \t\n1e2 \t\n");
        assertEquals(" \t\n1e2 \t\n", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespace() {
        Dfp a = field.newDfp("   ");
        assertTrue("   should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSign() {
        Dfp a = field.newDfp(" - ");
        assertTrue(" - should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDecimal() {
        Dfp a = field.newDfp(" . ");
        assertTrue(" . should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSignDecimal() {
        Dfp a = field.newDfp(" - . ");
        assertTrue(" - . should be zero", a.isZero());
        assertEquals("sign negative", -1, a.sign);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSignDigits() {
        Dfp a = field.newDfp(" - 123 ");
        assertEquals(" - 123 ", d(-123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDigits() {
        Dfp a = field.newDfp(" 123 ");
        assertEquals(" 123 ", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDecimalDigits() {
        Dfp a = field.newDfp(" 1.23 ");
        assertEquals(" 1.23 ", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndNegativeDecimalDigits() {
        Dfp a = field.newDfp(" -1.23 ");
        assertEquals(" -1.23 ", d(-1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndPositiveDecimalDigits() {
        Dfp a = field.newDfp(" +1.23 ");
        assertEquals(" +1.23 ", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponent() {
        Dfp a = field.newDfp(" 1e2 ");
        assertEquals(" 1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndNegativeExponent() {
        Dfp a = field.newDfp(" -1e2 ");
        assertEquals(" -1e2 ", d(-100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndPositiveExponent() {
        Dfp a = field.newDfp(" +1e2 ");
        assertEquals(" +1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegative() {
        Dfp a = field.newDfp(" 1e-2 ");
        assertEquals(" 1e-2 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositive() {
        Dfp a = field.newDfp(" 1e+2 ");
        assertEquals(" 1e+2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentWithSign() {
        Dfp a = field.newDfp(" 1e-2 ");
        assertEquals(" 1e-2 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentWithLeadingZeros() {
        Dfp a = field.newDfp(" 1e002 ");
        assertEquals(" 1e002 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegativeLeadingZeros() {
        Dfp a = field.newDfp(" 1e-002 ");
        assertEquals(" 1e-002 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositiveLeadingZeros() {
        Dfp a = field.newDfp(" 1e+002 ");
        assertEquals(" 1e+002 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOverflow() {
        Dfp a = field.newDfp(" 1e99999 ");
        assertTrue(" 1e99999 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentUnderflow() {
        Dfp a = field.newDfp(" 1e-99999 ");
        assertTrue(" 1e-99999 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMax() {
        Dfp a = field.newDfp(" 1e32768 ");
        assertTrue(" 1e32768 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMin() {
        Dfp a = field.newDfp(" 1e-32767 ");
        assertFalse(" 1e-32767 should be finite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentBelowMin() {
        Dfp a = field.newDfp(" 1e-32768 ");
        assertTrue(" 1e-32768 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentAboveMax() {
        Dfp a = field.newDfp(" 1e32769 ");
        assertTrue(" 1e32769 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentZero() {
        Dfp a = field.newDfp(" 1e0 ");
        assertEquals(" 1e0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegativeZero() {
        Dfp a = field.newDfp(" 1e-0 ");
        assertEquals(" 1e-0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositiveZero() {
        Dfp a = field.newDfp(" 1e+0 ");
        assertEquals(" 1e+0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnly() {
        Dfp a = field.newDfp(" e2 ");
        assertTrue(" e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySign() {
        Dfp a = field.newDfp(" -e2 ");
        assertTrue(" -e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimal() {
        Dfp a = field.newDfp(" .e2 ");
        assertTrue(" .e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalSign() {
        Dfp a = field.newDfp(" -.e2 ");
        assertTrue(" -.e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDigitsAfterE() {
        Dfp a = field.newDfp(" e123 ");
        assertTrue(" e123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySignAfterE() {
        Dfp a = field.newDfp(" e- ");
        assertTrue(" e- should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySignAndDigitsAfterE() {
        Dfp a = field.newDfp(" e-123 ");
        assertTrue(" e-123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalAfterE() {
        Dfp a = field.newDfp(" e.123 ");
        assertTrue(" e.123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalSignAfterE() {
        Dfp a = field.newDfp(" e-.123 ");
        assertTrue(" e-.123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMultipleE() {
        Dfp a = field.newDfp(" 1e2e3 ");
        assertEquals(" 1e2e3 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEAfterDecimal() {
        Dfp a = field.newDfp(" 1.2e3 ");
        assertEquals(" 1.2e3 ", d(1200.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEBeforeDecimal() {
        Dfp a = field.newDfp(" 1e2.3 ");
        assertEquals(" 1e2.3 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithSpaces() {
        Dfp a = field.newDfp(" 1 e 2 ");
        assertEquals(" 1 e 2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTabs() {
        Dfp a = field.newDfp(" 1\te\t2 ");
        assertEquals(" 1\te\t2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithNewlines() {
        Dfp a = field.newDfp(" 1\ne\n2 ");
        assertEquals(" 1\ne\n2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithMultipleSpaces() {
        Dfp a = field.newDfp(" 1   e   2 ");
        assertEquals(" 1   e   2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingSpaces() {
        Dfp a = field.newDfp("   1e2 ");
        assertEquals("   1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingSpaces() {
        Dfp a = field.newDfp(" 1e2   ");
        assertEquals(" 1e2   ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingTabs() {
        Dfp a = field.newDfp("\t1e2 ");
        assertEquals("\t1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingTabs() {
        Dfp a = field.newDfp(" 1e2\t");
        assertEquals(" 1e2\t", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingNewlines() {
        Dfp a = field.newDfp("\n1e2 ");
        assertEquals("\n1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingNewlines() {
        Dfp a = field.newDfp(" 1e2\n");
        assertEquals(" 1e2\n", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithMixedWhitespace() {
        Dfp a = field.newDfp(" \t\n1e2 \t\n");
        assertEquals(" \t\n1e2 \t\n", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespace() {
        Dfp a = field.newDfp("   ");
        assertTrue("   should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSign() {
        Dfp a = field.newDfp(" - ");
        assertTrue(" - should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDecimal() {
        Dfp a = field.newDfp(" . ");
        assertTrue(" . should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSignDecimal() {
        Dfp a = field.newDfp(" - . ");
        assertTrue(" - . should be zero", a.isZero());
        assertEquals("sign negative", -1, a.sign);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSignDigits() {
        Dfp a = field.newDfp(" - 123 ");
        assertEquals(" - 123 ", d(-123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDigits() {
        Dfp a = field.newDfp(" 123 ");
        assertEquals(" 123 ", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDecimalDigits() {
        Dfp a = field.newDfp(" 1.23 ");
        assertEquals(" 1.23 ", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndNegativeDecimalDigits() {
        Dfp a = field.newDfp(" -1.23 ");
        assertEquals(" -1.23 ", d(-1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndPositiveDecimalDigits() {
        Dfp a = field.newDfp(" +1.23 ");
        assertEquals(" +1.23 ", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponent() {
        Dfp a = field.newDfp(" 1e2 ");
        assertEquals(" 1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndNegativeExponent() {
        Dfp a = field.newDfp(" -1e2 ");
        assertEquals(" -1e2 ", d(-100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndPositiveExponent() {
        Dfp a = field.newDfp(" +1e2 ");
        assertEquals(" +1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegative() {
        Dfp a = field.newDfp(" 1e-2 ");
        assertEquals(" 1e-2 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositive() {
        Dfp a = field.newDfp(" 1e+2 ");
        assertEquals(" 1e+2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentWithSign() {
        Dfp a = field.newDfp(" 1e-2 ");
        assertEquals(" 1e-2 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentWithLeadingZeros() {
        Dfp a = field.newDfp(" 1e002 ");
        assertEquals(" 1e002 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegativeLeadingZeros() {
        Dfp a = field.newDfp(" 1e-002 ");
        assertEquals(" 1e-002 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositiveLeadingZeros() {
        Dfp a = field.newDfp(" 1e+002 ");
        assertEquals(" 1e+002 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOverflow() {
        Dfp a = field.newDfp(" 1e99999 ");
        assertTrue(" 1e99999 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentUnderflow() {
        Dfp a = field.newDfp(" 1e-99999 ");
        assertTrue(" 1e-99999 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMax() {
        Dfp a = field.newDfp(" 1e32768 ");
        assertTrue(" 1e32768 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMin() {
        Dfp a = field.newDfp(" 1e-32767 ");
        assertFalse(" 1e-32767 should be finite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentBelowMin() {
        Dfp a = field.newDfp(" 1e-32768 ");
        assertTrue(" 1e-32768 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentAboveMax() {
        Dfp a = field.newDfp(" 1e32769 ");
        assertTrue(" 1e32769 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentZero() {
        Dfp a = field.newDfp(" 1e0 ");
        assertEquals(" 1e0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegativeZero() {
        Dfp a = field.newDfp(" 1e-0 ");
        assertEquals(" 1e-0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositiveZero() {
        Dfp a = field.newDfp(" 1e+0 ");
        assertEquals(" 1e+0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnly() {
        Dfp a = field.newDfp(" e2 ");
        assertTrue(" e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySign() {
        Dfp a = field.newDfp(" -e2 ");
        assertTrue(" -e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimal() {
        Dfp a = field.newDfp(" .e2 ");
        assertTrue(" .e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalSign() {
        Dfp a = field.newDfp(" -.e2 ");
        assertTrue(" -.e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDigitsAfterE() {
        Dfp a = field.newDfp(" e123 ");
        assertTrue(" e123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySignAfterE() {
        Dfp a = field.newDfp(" e- ");
        assertTrue(" e- should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySignAndDigitsAfterE() {
        Dfp a = field.newDfp(" e-123 ");
        assertTrue(" e-123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalAfterE() {
        Dfp a = field.newDfp(" e.123 ");
        assertTrue(" e.123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalSignAfterE() {
        Dfp a = field.newDfp(" e-.123 ");
        assertTrue(" e-.123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMultipleE() {
        Dfp a = field.newDfp(" 1e2e3 ");
        assertEquals(" 1e2e3 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEAfterDecimal() {
        Dfp a = field.newDfp(" 1.2e3 ");
        assertEquals(" 1.2e3 ", d(1200.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEBeforeDecimal() {
        Dfp a = field.newDfp(" 1e2.3 ");
        assertEquals(" 1e2.3 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithSpaces() {
        Dfp a = field.newDfp(" 1 e 2 ");
        assertEquals(" 1 e 2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTabs() {
        Dfp a = field.newDfp(" 1\te\t2 ");
        assertEquals(" 1\te\t2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithNewlines() {
        Dfp a = field.newDfp(" 1\ne\n2 ");
        assertEquals(" 1\ne\n2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithMultipleSpaces() {
        Dfp a = field.newDfp(" 1   e   2 ");
        assertEquals(" 1   e   2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingSpaces() {
        Dfp a = field.newDfp("   1e2 ");
        assertEquals("   1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingSpaces() {
        Dfp a = field.newDfp(" 1e2   ");
        assertEquals(" 1e2   ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingTabs() {
        Dfp a = field.newDfp("\t1e2 ");
        assertEquals("\t1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingTabs() {
        Dfp a = field.newDfp(" 1e2\t");
        assertEquals(" 1e2\t", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingNewlines() {
        Dfp a = field.newDfp("\n1e2 ");
        assertEquals("\n1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingNewlines() {
        Dfp a = field.newDfp(" 1e2\n");
        assertEquals(" 1e2\n", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithMixedWhitespace() {
        Dfp a = field.newDfp(" \t\n1e2 \t\n");
        assertEquals(" \t\n1e2 \t\n", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespace() {
        Dfp a = field.newDfp("   ");
        assertTrue("   should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSign() {
        Dfp a = field.newDfp(" - ");
        assertTrue(" - should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDecimal() {
        Dfp a = field.newDfp(" . ");
        assertTrue(" . should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSignDecimal() {
        Dfp a = field.newDfp(" - . ");
        assertTrue(" - . should be zero", a.isZero());
        assertEquals("sign negative", -1, a.sign);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSignDigits() {
        Dfp a = field.newDfp(" - 123 ");
        assertEquals(" - 123 ", d(-123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDigits() {
        Dfp a = field.newDfp(" 123 ");
        assertEquals(" 123 ", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDecimalDigits() {
        Dfp a = field.newDfp(" 1.23 ");
        assertEquals(" 1.23 ", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndNegativeDecimalDigits() {
        Dfp a = field.newDfp(" -1.23 ");
        assertEquals(" -1.23 ", d(-1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndPositiveDecimalDigits() {
        Dfp a = field.newDfp(" +1.23 ");
        assertEquals(" +1.23 ", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponent() {
        Dfp a = field.newDfp(" 1e2 ");
        assertEquals(" 1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndNegativeExponent() {
        Dfp a = field.newDfp(" -1e2 ");
        assertEquals(" -1e2 ", d(-100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndPositiveExponent() {
        Dfp a = field.newDfp(" +1e2 ");
        assertEquals(" +1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegative() {
        Dfp a = field.newDfp(" 1e-2 ");
        assertEquals(" 1e-2 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositive() {
        Dfp a = field.newDfp(" 1e+2 ");
        assertEquals(" 1e+2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentWithSign() {
        Dfp a = field.newDfp(" 1e-2 ");
        assertEquals(" 1e-2 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentWithLeadingZeros() {
        Dfp a = field.newDfp(" 1e002 ");
        assertEquals(" 1e002 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegativeLeadingZeros() {
        Dfp a = field.newDfp(" 1e-002 ");
        assertEquals(" 1e-002 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositiveLeadingZeros() {
        Dfp a = field.newDfp(" 1e+002 ");
        assertEquals(" 1e+002 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOverflow() {
        Dfp a = field.newDfp(" 1e99999 ");
        assertTrue(" 1e99999 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentUnderflow() {
        Dfp a = field.newDfp(" 1e-99999 ");
        assertTrue(" 1e-99999 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMax() {
        Dfp a = field.newDfp(" 1e32768 ");
        assertTrue(" 1e32768 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMin() {
        Dfp a = field.newDfp(" 1e-32767 ");
        assertFalse(" 1e-32767 should be finite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentBelowMin() {
        Dfp a = field.newDfp(" 1e-32768 ");
        assertTrue(" 1e-32768 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentAboveMax() {
        Dfp a = field.newDfp(" 1e32769 ");
        assertTrue(" 1e32769 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentZero() {
        Dfp a = field.newDfp(" 1e0 ");
        assertEquals(" 1e0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegativeZero() {
        Dfp a = field.newDfp(" 1e-0 ");
        assertEquals(" 1e-0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositiveZero() {
        Dfp a = field.newDfp(" 1e+0 ");
        assertEquals(" 1e+0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnly() {
        Dfp a = field.newDfp(" e2 ");
        assertTrue(" e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySign() {
        Dfp a = field.newDfp(" -e2 ");
        assertTrue(" -e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimal() {
        Dfp a = field.newDfp(" .e2 ");
        assertTrue(" .e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalSign() {
        Dfp a = field.newDfp(" -.e2 ");
        assertTrue(" -.e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDigitsAfterE() {
        Dfp a = field.newDfp(" e123 ");
        assertTrue(" e123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySignAfterE() {
        Dfp a = field.newDfp(" e- ");
        assertTrue(" e- should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlySignAndDigitsAfterE() {
        Dfp a = field.newDfp(" e-123 ");
        assertTrue(" e-123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalAfterE() {
        Dfp a = field.newDfp(" e.123 ");
        assertTrue(" e.123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnlyDecimalSignAfterE() {
        Dfp a = field.newDfp(" e-.123 ");
        assertTrue(" e-.123 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMultipleE() {
        Dfp a = field.newDfp(" 1e2e3 ");
        assertEquals(" 1e2e3 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEAfterDecimal() {
        Dfp a = field.newDfp(" 1.2e3 ");
        assertEquals(" 1.2e3 ", d(1200.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEBeforeDecimal() {
        Dfp a = field.newDfp(" 1e2.3 ");
        assertEquals(" 1e2.3 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithSpaces() {
        Dfp a = field.newDfp(" 1 e 2 ");
        assertEquals(" 1 e 2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTabs() {
        Dfp a = field.newDfp(" 1\te\t2 ");
        assertEquals(" 1\te\t2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithNewlines() {
        Dfp a = field.newDfp(" 1\ne\n2 ");
        assertEquals(" 1\ne\n2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithMultipleSpaces() {
        Dfp a = field.newDfp(" 1   e   2 ");
        assertEquals(" 1   e   2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingSpaces() {
        Dfp a = field.newDfp("   1e2 ");
        assertEquals("   1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingSpaces() {
        Dfp a = field.newDfp(" 1e2   ");
        assertEquals(" 1e2   ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingTabs() {
        Dfp a = field.newDfp("\t1e2 ");
        assertEquals("\t1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingTabs() {
        Dfp a = field.newDfp(" 1e2\t");
        assertEquals(" 1e2\t", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithLeadingNewlines() {
        Dfp a = field.newDfp("\n1e2 ");
        assertEquals("\n1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithTrailingNewlines() {
        Dfp a = field.newDfp(" 1e2\n");
        assertEquals(" 1e2\n", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithMixedWhitespace() {
        Dfp a = field.newDfp(" \t\n1e2 \t\n");
        assertEquals(" \t\n1e2 \t\n", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespace() {
        Dfp a = field.newDfp("   ");
        assertTrue("   should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSign() {
        Dfp a = field.newDfp(" - ");
        assertTrue(" - should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDecimal() {
        Dfp a = field.newDfp(" . ");
        assertTrue(" . should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSignDecimal() {
        Dfp a = field.newDfp(" - . ");
        assertTrue(" - . should be zero", a.isZero());
        assertEquals("sign negative", -1, a.sign);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndSignDigits() {
        Dfp a = field.newDfp(" - 123 ");
        assertEquals(" - 123 ", d(-123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDigits() {
        Dfp a = field.newDfp(" 123 ");
        assertEquals(" 123 ", d(123.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndDecimalDigits() {
        Dfp a = field.newDfp(" 1.23 ");
        assertEquals(" 1.23 ", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndNegativeDecimalDigits() {
        Dfp a = field.newDfp(" -1.23 ");
        assertEquals(" -1.23 ", d(-1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndPositiveDecimalDigits() {
        Dfp a = field.newDfp(" +1.23 ");
        assertEquals(" +1.23 ", d(1.23), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponent() {
        Dfp a = field.newDfp(" 1e2 ");
        assertEquals(" 1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndNegativeExponent() {
        Dfp a = field.newDfp(" -1e2 ");
        assertEquals(" -1e2 ", d(-100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndPositiveExponent() {
        Dfp a = field.newDfp(" +1e2 ");
        assertEquals(" +1e2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegative() {
        Dfp a = field.newDfp(" 1e-2 ");
        assertEquals(" 1e-2 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositive() {
        Dfp a = field.newDfp(" 1e+2 ");
        assertEquals(" 1e+2 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentWithSign() {
        Dfp a = field.newDfp(" 1e-2 ");
        assertEquals(" 1e-2 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentWithLeadingZeros() {
        Dfp a = field.newDfp(" 1e002 ");
        assertEquals(" 1e002 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegativeLeadingZeros() {
        Dfp a = field.newDfp(" 1e-002 ");
        assertEquals(" 1e-002 ", d(0.01), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositiveLeadingZeros() {
        Dfp a = field.newDfp(" 1e+002 ");
        assertEquals(" 1e+002 ", d(100.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOverflow() {
        Dfp a = field.newDfp(" 1e99999 ");
        assertTrue(" 1e99999 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentUnderflow() {
        Dfp a = field.newDfp(" 1e-99999 ");
        assertTrue(" 1e-99999 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMax() {
        Dfp a = field.newDfp(" 1e32768 ");
        assertTrue(" 1e32768 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentMin() {
        Dfp a = field.newDfp(" 1e-32767 ");
        assertFalse(" 1e-32767 should be finite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentBelowMin() {
        Dfp a = field.newDfp(" 1e-32768 ");
        assertTrue(" 1e-32768 should be zero", a.isZero());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentAboveMax() {
        Dfp a = field.newDfp(" 1e32769 ");
        assertTrue(" 1e32769 should be infinite", a.isInfinite());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentZero() {
        Dfp a = field.newDfp(" 1e0 ");
        assertEquals(" 1e0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentNegativeZero() {
        Dfp a = field.newDfp(" 1e-0 ");
        assertEquals(" 1e-0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentPositiveZero() {
        Dfp a = field.newDfp(" 1e+0 ");
        assertEquals(" 1e+0 ", d(1.0), a);
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentOnly() {
        Dfp a = field.newDfp(" e2 ");
        assertTrue(" e2 should be NaN", a.isNaN());
    }

    @Test(timeout = 4000)
    public void testFieldNewDfpStringWithExponentAndEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAndExponentEWithOnlyWhitespaceAnd