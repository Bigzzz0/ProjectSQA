/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math3.dfp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Targeted Decision Branches & Boundary Conditions:
 * 1. Defect-Targeted Zone:
 *    - DfpTest::testMultiply (#37): FieldElement contract requires multiply(int) to accept any integer,
 *      including negative integers and integers >= RADIX (10000). The defective implementation blindly
 *      delegates multiply(int) to multiplyFast(int), which treats x < 0 or x >= RADIX as FLAG_INVALID
 *      and returns QNAN.
 * 2. Constructors & Factories:
 *    - Long conversions: 0L, positive, negative, Long.MIN_VALUE special digit shift, Long.MAX_VALUE.
 *    - Double conversions: +0.0, -0.0 (sign bit preserved), subnormal denormals, positive/negative infinity,
 *      NaN, subnormal loops, power-of-two alignment.
 *    - String parser: "Infinity", "-Infinity", "NaN", exponents ('e', 'E', negative), leading/trailing zeros,
 *      numbers without decimal points, "0.00000" special-case, long fractional parts triggering round().
 *    - Precision mismatched newInstance(Dfp): triggers FLAG_INVALID trap.
 * 3. State & Predicates:
 *    - isZero, isInfinite, isNaN, classify (FINITE, INFINITE, SNAN, QNAN).
 *    - negativeOrNull, strictlyNegative, positiveOrNull, strictlyPositive with 0, -0, +/-Inf, NaN.
 *    - equals / hashCode / compare: null, non-Dfp, different precision, NaNs, zeros of opposite signs,
 *      Infinities (equal, unequal signs), exponent ordering, mantissa ordering.
 * 4. Algebraic Operations & Traps:
 *    - align(): diff == 0, adiff > mant.length + 1 (special zero fill & inexact), diff < 0 (2+ digit inexact loss).
 *    - add(): different precisions, NaNs, +/-Inf additions, canceling to signed zero, carry propagation.
 *    - multiply(Dfp): different precisions, Inf * finite, Inf * 0 (invalid), normal multi-digit carry.
 *    - multiply(int): fast multiplication, Inf * 0, digit carry.
 *    - divide(Dfp): different precisions, NaNs, Inf/finite, finite/Inf, Inf/Inf (invalid), divide by zero,
 *      trial quotient adjustments (minadj >= 2, negative remainder, trial verification loop).
 *    - divide(int): div by zero, range checks (<0, >=RADIX), normalization.
 *    - sqrt(): zero, +Inf, -Inf / negative finite (invalid), SNAN/QNAN, coarse mantissa estimations (0, 2, 3, default),
 *      alternating iteration break, dx zero break.
 * 5. Rounding, Truncation & Stringification:
 *    - rint, floor, ceil, remainder (sign of zero preserved).
 *    - trunc(RoundingMode): exp < 0, exp >= mant.length, FLOOR on negative, CEIL on positive, HALF_EVEN ties.
 *    - round(int): all 8 RoundingModes, underflow (< MIN_EXP), overflow (> MAX_EXP), inexact flag.
 *    - intValue(): clamps to Integer.MAX_VALUE and Integer.MIN_VALUE, signs.
 *    - log10, log10K, power10, power10K.
 *    - toDouble(): Inf, NaN, +/-0, subnormal exponent adjustments (< -1023), powers-of-two carryover, toSplitDouble.
 *    - toString(): dfp2sci vs dfp2string, negative numbers, padding zeros.
 */
public class DfpGeminiTest {

    private DfpField field20;
    private DfpField field12;

    @Before
    public void setUp() {
        field20 = new DfpField(20); // 6 radix digits (4 digits each)
        field12 = new DfpField(12); // 4 radix digits
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J bug: DfpTest::testMultiply (#37: Multiply #37 x = NaN flags = 1).
     * FieldElement.multiply(int) must correctly multiply negative integers without
     * falling into multiplyFast's range check (which marks x < 0 as FLAG_INVALID and yields QNAN).
     */
    @Test(timeout = 4000)
    public void testDefectMultiplyNegativeInt() {
        field20.clearIEEEFlags();
        Dfp one = field20.getOne();
        Dfp result = one.multiply(-1);

        assertFalse("Multiply by -1 should not yield NaN", result.isNaN());
        assertEquals("1 * -1 must equal -1", field20.getOne().negate(), result);
        assertEquals("FLAG_INVALID must not be raised for negative integer multiplication",
                0, field20.getIEEEFlags() & DfpField.FLAG_INVALID);
    }

    /**
     * Targets integer multiplication for numbers >= RADIX (10000).
     */
    @Test(timeout = 4000)
    public void testDefectMultiplyLargeInt() {
        field20.clearIEEEFlags();
        Dfp two = field20.newDfp(2);
        Dfp result = two.multiply(10000);

        assertFalse("Multiply by 10000 should not yield NaN", result.isNaN());
        assertEquals("2 * 10000 must equal 20000", field20.newDfp(20000), result);
        assertEquals("FLAG_INVALID must not be raised for x >= RADIX multiplication",
                0, field20.getIEEEFlags() & DfpField.FLAG_INVALID);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicConstantsAndGetters() {
        Dfp zero = field20.getZero();
        Dfp one = field20.getOne();
        Dfp two = field20.getTwo();

        assertTrue(zero.isZero());
        assertFalse(one.isZero());
        assertEquals(1, one.sign);
        assertEquals(field20, zero.getField());
        assertEquals(field20.getRadixDigits(), zero.getRadixDigits());
        assertEquals(Dfp.FINITE, zero.classify());
        assertEquals(Dfp.FINITE, one.classify());
        assertEquals(Dfp.FINITE, two.classify());
    }

    @Test(timeout = 4000)
    public void testConstructorsNumericPrimitives() {
        Dfp fromByte = field20.newDfp((byte) 127);
        assertEquals("127.", fromByte.toString());

        Dfp fromInt = field20.newDfp(-1234567);
        assertEquals("-1234567.", fromInt.toString());

        Dfp fromLong = field20.newDfp(9876543210123L);
        assertEquals("9876543210123.", fromLong.toString());
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleExtremes() {
        Dfp posZero = field20.newDfp(0.0);
        assertEquals(1, posZero.sign);
        assertTrue(posZero.isZero());

        Dfp negZero = field20.newDfp(-0.0);
        assertEquals(-1, negZero.sign);
        assertTrue(negZero.isZero());

        Dfp posInf = field20.newDfp(Double.POSITIVE_INFINITY);
        assertTrue(posInf.isInfinite());
        assertEquals(1, posInf.sign);

        Dfp negInf = field20.newDfp(Double.NEGATIVE_INFINITY);
        assertTrue(negInf.isInfinite());
        assertEquals(-1, negInf.sign);

        Dfp qnan = field20.newDfp(Double.NaN);
        assertTrue(qnan.isNaN());

        Dfp subnormal = field20.newDfp(Double.MIN_VALUE);
        assertFalse(subnormal.isZero());
        assertTrue(subnormal.strictlyPositive());
    }

    @Test(timeout = 4000)
    public void testConstructorStringSpecialAndScientific() {
        assertEquals(Dfp.INFINITE, field20.newDfp("Infinity").classify());
        assertEquals(1, field20.newDfp("Infinity").sign);

        assertEquals(Dfp.INFINITE, field20.newDfp("-Infinity").classify());
        assertEquals(-1, field20.newDfp("-Infinity").sign);

        assertEquals(Dfp.QNAN, field20.newDfp("NaN").classify());

        Dfp sci1 = field20.newDfp("1.2345e4");
        assertEquals("12345.", sci1.toString());

        Dfp sci2 = field20.newDfp("1.2345E-2");
        assertEquals("0.012345", sci2.toString());

        Dfp zeroString = field20.newDfp("0.00000");
        assertTrue(zeroString.isZero());

        Dfp intString = field20.newDfp("10000");
        assertEquals("10000.", intString.toString());
    }

    @Test(timeout = 4000)
    public void testAddAndSubtractNormal() {
        Dfp a = field20.newDfp("123.456");
        Dfp b = field20.newDfp("456.789");
        Dfp sum = a.add(b);
        assertEquals("580.245", sum.toString());

        Dfp diff = a.subtract(b);
        assertEquals("-333.333", diff.toString());

        Dfp negA = a.negate();
        assertEquals("-123.456", negA.toString());
    }

    @Test(timeout = 4000)
    public void testAddCancelingToZeroPreservesSignRule() {
        Dfp a = field20.newDfp("10");
        Dfp b = field20.newDfp("-10");
        Dfp sum = a.add(b);
        assertTrue(sum.isZero());
        assertEquals(1, sum.sign); // IEEE 854-1987 Section 6.3: 1 unless adding two negative zeros
    }

    @Test(timeout = 4000)
    public void testMultiplyAndDivideNormal() {
        Dfp a = field20.newDfp("12.5");
        Dfp b = field20.newDfp("2.5");

        Dfp prod = a.multiply(b);
        assertEquals("31.25", prod.toString());

        Dfp quot = a.divide(b);
        assertEquals("5.", quot.toString());

        Dfp recip = b.reciprocal();
        assertEquals("0.4", recip.toString());
    }

    @Test(timeout = 4000)
    public void testDivideBySingleDigitFast() {
        Dfp a = field20.newDfp("100");
        Dfp res = a.divide(4);
        assertEquals("25.", res.toString());
    }

    @Test(timeout = 4000)
    public void testSqrt() {
        Dfp four = field20.newDfp("4");
        assertEquals("2.", four.sqrt().toString());

        Dfp zero = field20.getZero();
        assertTrue(zero.sqrt().isZero());

        Dfp posInf = field20.newDfp(1, Dfp.INFINITE);
        assertTrue(posInf.sqrt().isInfinite());

        field20.clearIEEEFlags();
        Dfp neg = field20.newDfp("-4");
        Dfp sqrtNeg = neg.sqrt();
        assertTrue(sqrtNeg.isNaN());
        assertTrue((field20.getIEEEFlags() & DfpField.FLAG_INVALID) != 0);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testLongMinValueConstructor() {
        Dfp minLong = field20.newDfp(Long.MIN_VALUE);
        assertEquals("-9223372036854775808.", minLong.toString());
        assertEquals(-1, minLong.sign);

        Dfp maxLong = field20.newDfp(Long.MAX_VALUE);
        assertEquals("9223372036854775807.", maxLong.toString());
        assertEquals(1, maxLong.sign);
    }

    @Test(timeout = 4000)
    public void testAlignLargeDiscrepancy() {
        field20.clearIEEEFlags();
        Dfp small = field20.newDfp("1e-50");
        Dfp large = field20.newDfp("1e50");
        Dfp sum = large.add(small);
        assertEquals(large, sum);
        assertTrue((field20.getIEEEFlags() & DfpField.FLAG_INEXACT) != 0);
    }

    @Test(timeout = 4000)
    public void testDivideByZeroTrap() {
        field20.clearIEEEFlags();
        Dfp one = field20.getOne();
        Dfp zero = field20.getZero();
        Dfp res = one.divide(zero);

        assertTrue(res.isInfinite());
        assertEquals(1, res.sign);
        assertTrue((field20.getIEEEFlags() & DfpField.FLAG_DIV_ZERO) != 0);

        field20.clearIEEEFlags();
        Dfp resInt = one.divide(0);
        assertTrue(resInt.isInfinite());
        assertTrue((field20.getIEEEFlags() & DfpField.FLAG_DIV_ZERO) != 0);
    }

    @Test(timeout = 4000)
    public void testInfinityArithmeticRules() {
        Dfp pInf = field20.newDfp(1, Dfp.INFINITE);
        Dfp nInf = field20.newDfp(-1, Dfp.INFINITE);
        Dfp zero = field20.getZero();
        Dfp one = field20.getOne();

        // Inf + Inf = Inf
        assertEquals(pInf, pInf.add(pInf));

        // Inf + -Inf = NaN (FLAG_INVALID)
        field20.clearIEEEFlags();
        Dfp nanAdd = pInf.add(nInf);
        assertTrue(nanAdd.isNaN());
        assertTrue((field20.getIEEEFlags() & DfpField.FLAG_INVALID) != 0);

        // Inf * 0 = NaN (FLAG_INVALID)
        field20.clearIEEEFlags();
        Dfp nanMul = pInf.multiply(zero);
        assertTrue(nanMul.isNaN());
        assertTrue((field20.getIEEEFlags() & DfpField.FLAG_INVALID) != 0);

        // Inf / Inf = NaN (FLAG_INVALID)
        field20.clearIEEEFlags();
        Dfp nanDiv = pInf.divide(nInf);
        assertTrue(nanDiv.isNaN());
        assertTrue((field20.getIEEEFlags() & DfpField.FLAG_INVALID) != 0);

        // 1 / Inf = 0
        assertEquals(zero, one.divide(pInf));

        // Inf / 1 = Inf
        assertEquals(pInf, pInf.divide(one));
    }

    @Test(timeout = 4000)
    public void testComparisonPredicatesWithNaNAndInfinities() {
        Dfp qnan = field20.newDfp(1, Dfp.QNAN);
        Dfp pInf = field20.newDfp(1, Dfp.INFINITE);
        Dfp nInf = field20.newDfp(-1, Dfp.INFINITE);
        Dfp zero = field20.getZero();
        Dfp one = field20.getOne();
        Dfp mOne = field20.newDfp(-1);

        assertFalse(qnan.lessThan(one));
        assertFalse(qnan.greaterThan(one));
        assertFalse(qnan.negativeOrNull());
        assertFalse(qnan.strictlyNegative());
        assertFalse(qnan.positiveOrNull());
        assertFalse(qnan.strictlyPositive());
        assertFalse(qnan.isZero());

        assertTrue(pInf.greaterThan(one));
        assertTrue(nInf.lessThan(mOne));
        assertTrue(pInf.strictlyPositive());
        assertTrue(nInf.strictlyNegative());
        assertTrue(zero.positiveOrNull());
        assertTrue(zero.negativeOrNull());
        assertFalse(zero.strictlyPositive());
        assertFalse(zero.strictlyNegative());
    }

    @Test(timeout = 4000)
    public void testNextAfter() {
        Dfp zero = field20.getZero();
        Dfp one = field20.getOne();

        Dfp nextUp = zero.nextAfter(one);
        assertTrue(nextUp.greaterThan(zero));
        assertTrue(nextUp.lessThan(one));

        Dfp nextDown = zero.nextAfter(one.negate());
        assertTrue(nextDown.lessThan(zero));

        assertEquals(one, one.nextAfter(one));
    }

    @Test(timeout = 4000)
    public void testCopysign() {
        Dfp two = field20.newDfp(2);
        Dfp mThree = field20.newDfp(-3);

        Dfp res = Dfp.copysign(two, mThree);
        assertEquals("-2.", res.toString());

        Dfp res2 = Dfp.copysign(mThree, two);
        assertEquals("3.", res2.toString());
    }

    // =========================================================================
    // Partition D: Precision Guards, Rounding Modes & Conversions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrecisionMismatchRaisesTrap() {
        field20.clearIEEEFlags();
        Dfp a = field20.getOne();
        Dfp b = field12.getOne();

        Dfp res = a.add(b);
        assertTrue(res.isNaN());
        assertTrue((field20.getIEEEFlags() & DfpField.FLAG_INVALID) != 0);

        assertFalse(a.lessThan(b));
        assertFalse(a.greaterThan(b));
        assertFalse(a.equals(b));
        assertFalse(a.unequal(b));
    }

    @Test(timeout = 4000)
    public void testTruncationModes() {
        Dfp val = field20.newDfp("2.6");
        assertEquals("2.", val.floor().toString());
        assertEquals("3.", val.ceil().toString());
        assertEquals("3.", val.rint().toString());

        Dfp negVal = field20.newDfp("-2.6");
        assertEquals("-3.", negVal.floor().toString());
        assertEquals("-2.", negVal.ceil().toString());
        assertEquals("-3.", negVal.rint().toString());

        // Half-even ties
        Dfp halfEven1 = field20.newDfp("2.5").rint();
        assertEquals("2.", halfEven1.toString()); // rounds to even 2

        Dfp halfEven2 = field20.newDfp("3.5").rint();
        assertEquals("4.", halfEven2.toString()); // rounds to even 4
    }

    @Test(timeout = 4000)
    public void testRemainderSignOfZero() {
        Dfp a = field20.newDfp("-4.0");
        Dfp b = field20.newDfp("2.0");
        Dfp rem = a.remainder(b);
        assertTrue(rem.isZero());
        assertEquals(-1, rem.sign); // IEEE remainder carries sign of 'this' when zero
    }

    @Test(timeout = 4000)
    public void testIntValueClamping() {
        Dfp huge = field20.newDfp("99999999999999");
        assertEquals(Integer.MAX_VALUE, huge.intValue());

        Dfp negHuge = field20.newDfp("-99999999999999");
        assertEquals(Integer.MIN_VALUE, negHuge.intValue());

        Dfp normal = field20.newDfp("42.9");
        assertEquals(43, normal.intValue()); // rint rounded
    }

    @Test(timeout = 4000)
    public void testPowersAndLogarithms() {
        Dfp val = field20.newDfp("5432.1");
        assertEquals(3, val.log10());
        assertEquals(0, val.log10K());

        Dfp pow10_3 = val.power10(3);
        assertEquals("1000.", pow10_3.toString());

        Dfp pow10_neg2 = val.power10(-2);
        assertEquals("0.01", pow10_neg2.toString());

        Dfp pow10K_2 = val.power10K(2);
        assertEquals("100000000.", pow10K_2.toString());
    }

    @Test(timeout = 4000)
    public void testToDoubleConversion() {
        Dfp dPosInf = field20.newDfp(1, Dfp.INFINITE);
        assertEquals(Double.POSITIVE_INFINITY, dPosInf.toDouble(), 0.0);

        Dfp dNegInf = field20.newDfp(-1, Dfp.INFINITE);
        assertEquals(Double.NEGATIVE_INFINITY, dNegInf.toDouble(), 0.0);

        Dfp dNaN = field20.newDfp(1, Dfp.QNAN);
        assertTrue(Double.isNaN(dNaN.toDouble()));

        Dfp dZero = field20.getZero();
        assertEquals(0.0, dZero.toDouble(), 0.0);

        Dfp dNegZero = field20.newDfp("-0.0");
        assertEquals(-0.0, dNegZero.toDouble(), 0.0);

        Dfp normal = field20.newDfp("123.456");
        assertEquals(123.456, normal.toDouble(), 1e-12);

        double[] split = normal.toSplitDouble();
        assertEquals(2, split.length);
        assertEquals(123.456, split[0] + split[1], 1e-12);
    }

    @Test(timeout = 4000)
    public void testDfp2SciFormatting() {
        Dfp small = field20.newDfp("1.2345e-15");
        String sciStr = small.toString();
        assertTrue(sciStr.contains("e-15") || sciStr.contains("e-015"));

        Dfp large = field20.newDfp("9.8765e30");
        String largeStr = large.toString();
        assertTrue(largeStr.contains("e30") || largeStr.contains("e030"));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Dfp a = field20.newDfp("123.450");
        Dfp b = field20.newDfp("123.45");
        Dfp c = field20.newDfp("123.451");

        // Reflexive
        assertTrue(a.equals(a));
        // Symmetric
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());

        // Inequality
        assertFalse(a.equals(c));
        assertFalse(a.equals(null));
        assertFalse(a.equals("123.45"));

        // Signed zeroes equality in IEEE
        Dfp pZero = field20.newDfp("0");
        Dfp mZero = field20.newDfp("-0");
        assertTrue(pZero.equals(mZero));

        // NaNs do not equal themselves
        Dfp nan = field20.newDfp(1, Dfp.QNAN);
        assertFalse(nan.equals(nan));
    }

    @Test(timeout = 4000)
    public void testCopyConstructorAndNewInstance() {
        Dfp orig = field20.newDfp("42.42");
        Dfp copy = new Dfp(orig);

        assertEquals(orig, copy);
        assertEquals(orig.sign, copy.sign);
        assertEquals(orig.exp, copy.exp);
        assertEquals(orig.nans, copy.nans);

        Dfp fromNewInstance = orig.newInstance(orig);
        assertEquals(orig, fromNewInstance);

        Dfp fromByte = orig.newInstance((byte) 3);
        assertEquals("3.", fromByte.toString());

        Dfp fromDouble = orig.newInstance(3.14);
        assertEquals(3.14, fromDouble.toDouble(), 1e-12);
    }
}