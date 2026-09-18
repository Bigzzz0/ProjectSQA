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
package org.apache.commons.lang3.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.lang3.math.NumberUtils
 *
 * Decision / Condition Matrix:
 * 1. createNumber(String):
 *    - null string -> null
 *    - blank/empty strings -> NumberFormatException
 *    - "--..." prefix -> Defect Zone: defective implementation returns null instead of throwing NumberFormatException
 *    - Hex integer prefixes: "0x", "-0x", "0X", "-0X" with length <= 8 hex digits -> Integer; > 8 -> Long
 *    - Exponent before decimal point ("1e2.3") -> NumberFormatException
 *    - Exponent position out of bounds -> NumberFormatException
 *    - Type qualifiers: 'l', 'L', 'f', 'F', 'd', 'D', and default non-digit/non-decimal -> NFE
 *    - Overflow cascades: Integer -> Long -> BigInteger; Float -> Double -> BigDecimal
 *    - Precision zero-checks with isAllZeros(mant/exp)
 * 2. isNumber(String):
 *    - Null / empty checks
 *    - Hex prefix handling ('0x', '-0x') with valid/invalid hex digits
 *    - Exponent ('e', 'E') state machine (repeated 'e', missing digit after sign, trailing sign)
 *    - Decimal point handling (multiple dots, dots in exponent)
 *    - Qualifiers at string end ('f', 'F', 'd', 'D', 'l', 'L')
 * 3. Array min/max operations for long, int, short, byte, double, float:
 *    - Null array -> IllegalArgumentException
 *    - Empty array -> IllegalArgumentException
 *    - Element comparisons (first, middle, last is min/max)
 *    - Floating point NaN propagation (NaN in first, middle, last position)
 * 4. Three-parameter min/max methods:
 *    - Permutations of (a < b < c), (b < a < c), (c < a < b), etc.
 *    - Double and Float NaN and Infinity handling
 * 5. String to Primitive Converters (toInt, toLong, toFloat, toDouble, toByte, toShort):
 *    - null input -> default value
 *    - valid numeric -> parsed value
 *    - invalid numeric -> default value
 */
public class NumberUtilsGeminiTest {

    private static final double DELTA = 1e-9;
    private static final float FLOAT_DELTA = 1e-5f;

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAndConstants() {
        NumberUtils instance = new NumberUtils();
        assertNotNull(instance);

        assertEquals(Long.valueOf(0L), NumberUtils.LONG_ZERO);
        assertEquals(Long.valueOf(1L), NumberUtils.LONG_ONE);
        assertEquals(Long.valueOf(-1L), NumberUtils.LONG_MINUS_ONE);

        assertEquals(Integer.valueOf(0), NumberUtils.INTEGER_ZERO);
        assertEquals(Integer.valueOf(1), NumberUtils.INTEGER_ONE);
        assertEquals(Integer.valueOf(-1), NumberUtils.INTEGER_MINUS_ONE);

        assertEquals(Short.valueOf((short) 0), NumberUtils.SHORT_ZERO);
        assertEquals(Short.valueOf((short) 1), NumberUtils.SHORT_ONE);
        assertEquals(Short.valueOf((short) -1), NumberUtils.SHORT_MINUS_ONE);

        assertEquals(Byte.valueOf((byte) 0), NumberUtils.BYTE_ZERO);
        assertEquals(Byte.valueOf((byte) 1), NumberUtils.BYTE_ONE);
        assertEquals(Byte.valueOf((byte) -1), NumberUtils.BYTE_MINUS_ONE);

        assertEquals(Double.valueOf(0.0d), NumberUtils.DOUBLE_ZERO);
        assertEquals(Double.valueOf(1.0d), NumberUtils.DOUBLE_ONE);
        assertEquals(Double.valueOf(-1.0d), NumberUtils.DOUBLE_MINUS_ONE);

        assertEquals(Float.valueOf(0.0f), NumberUtils.FLOAT_ZERO);
        assertEquals(Float.valueOf(1.0f), NumberUtils.FLOAT_ONE);
        assertEquals(Float.valueOf(-1.0f), NumberUtils.FLOAT_MINUS_ONE);
    }

    @Test(timeout = 4000)
    public void testToInt() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(456, NumberUtils.toInt("456", 10));
        assertEquals(10, NumberUtils.toInt("invalid", 10));
        assertEquals(10, NumberUtils.toInt(null, 10));
    }

    @Test(timeout = 4000)
    public void testToLong() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(0L, NumberUtils.toLong("xyz"));
        assertEquals(1234567890123L, NumberUtils.toLong("1234567890123"));
        assertEquals(55L, NumberUtils.toLong("55", 99L));
        assertEquals(99L, NumberUtils.toLong("abc", 99L));
        assertEquals(99L, NumberUtils.toLong(null, 99L));
    }

    @Test(timeout = 4000)
    public void testToFloat() {
        assertEquals(0.0f, NumberUtils.toFloat(null), FLOAT_DELTA);
        assertEquals(0.0f, NumberUtils.toFloat(""), FLOAT_DELTA);
        assertEquals(0.0f, NumberUtils.toFloat("bad"), FLOAT_DELTA);
        assertEquals(3.14f, NumberUtils.toFloat("3.14"), FLOAT_DELTA);
        assertEquals(2.5f, NumberUtils.toFloat("2.5", 1.1f), FLOAT_DELTA);
        assertEquals(1.1f, NumberUtils.toFloat("bad", 1.1f), FLOAT_DELTA);
        assertEquals(1.1f, NumberUtils.toFloat(null, 1.1f), FLOAT_DELTA);
    }

    @Test(timeout = 4000)
    public void testToDouble() {
        assertEquals(0.0d, NumberUtils.toDouble(null), DELTA);
        assertEquals(0.0d, NumberUtils.toDouble(""), DELTA);
        assertEquals(0.0d, NumberUtils.toDouble("bad"), DELTA);
        assertEquals(3.14159d, NumberUtils.toDouble("3.14159"), DELTA);
        assertEquals(2.718d, NumberUtils.toDouble("2.718", 1.414d), DELTA);
        assertEquals(1.414d, NumberUtils.toDouble("bad", 1.414d), DELTA);
        assertEquals(1.414d, NumberUtils.toDouble(null, 1.414d), DELTA);
    }

    @Test(timeout = 4000)
    public void testToByte() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 0, NumberUtils.toByte("bad"));
        assertEquals((byte) 12, NumberUtils.toByte("12"));
        assertEquals((byte) 7, NumberUtils.toByte("7", (byte) 3));
        assertEquals((byte) 3, NumberUtils.toByte("bad", (byte) 3));
        assertEquals((byte) 3, NumberUtils.toByte(null, (byte) 3));
    }

    @Test(timeout = 4000)
    public void testToShort() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 0, NumberUtils.toShort("bad"));
        assertEquals((short) 1234, NumberUtils.toShort("1234"));
        assertEquals((short) 42, NumberUtils.toShort("42", (short) 9));
        assertEquals((short) 9, NumberUtils.toShort("bad", (short) 9));
        assertEquals((short) 9, NumberUtils.toShort(null, (short) 9));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinMaxArrayLong() {
        long[] arr = {5L, -2L, 10L, 0L};
        assertEquals(-2L, NumberUtils.min(arr));
        assertEquals(10L, NumberUtils.max(arr));

        long[] single = {42L};
        assertEquals(42L, NumberUtils.min(single));
        assertEquals(42L, NumberUtils.max(single));

        long[] reversed = {10L, 5L, 1L, -10L};
        assertEquals(-10L, NumberUtils.min(reversed));
        assertEquals(10L, NumberUtils.max(reversed));
    }

    @Test(timeout = 4000)
    public void testMinMaxArrayInt() {
        int[] arr = {5, -2, 10, 0};
        assertEquals(-2, NumberUtils.min(arr));
        assertEquals(10, NumberUtils.max(arr));

        int[] single = {42};
        assertEquals(42, NumberUtils.min(single));
        assertEquals(42, NumberUtils.max(single));
    }

    @Test(timeout = 4000)
    public void testMinMaxArrayShort() {
        short[] arr = {(short) 5, (short) -2, (short) 10, (short) 0};
        assertEquals((short) -2, NumberUtils.min(arr));
        assertEquals((short) 10, NumberUtils.max(arr));

        short[] single = {(short) 42};
        assertEquals((short) 42, NumberUtils.min(single));
        assertEquals((short) 42, NumberUtils.max(single));
    }

    @Test(timeout = 4000)
    public void testMinMaxArrayByte() {
        byte[] arr = {(byte) 5, (byte) -2, (byte) 10, (byte) 0};
        assertEquals((byte) -2, NumberUtils.min(arr));
        assertEquals((byte) 10, NumberUtils.max(arr));

        byte[] single = {(byte) 42};
        assertEquals((byte) 42, NumberUtils.min(single));
        assertEquals((byte) 42, NumberUtils.max(single));
    }

    @Test(timeout = 4000)
    public void testMinMaxArrayDouble() {
        double[] arr = {5.0d, -2.5d, 10.2d, 0.0d};
        assertEquals(-2.5d, NumberUtils.min(arr), DELTA);
        assertEquals(10.2d, NumberUtils.max(arr), DELTA);

        double[] nanArr = {1.0d, Double.NaN, 2.0d};
        assertTrue(Double.isNaN(NumberUtils.min(nanArr)));
        assertTrue(Double.isNaN(NumberUtils.max(nanArr)));

        double[] nanFirst = {Double.NaN, 1.0d, 2.0d};
        assertTrue(Double.isNaN(NumberUtils.min(nanFirst)));
        assertTrue(Double.isNaN(NumberUtils.max(nanFirst)));
    }

    @Test(timeout = 4000)
    public void testMinMaxArrayFloat() {
        float[] arr = {5.0f, -2.5f, 10.2f, 0.0f};
        assertEquals(-2.5f, NumberUtils.min(arr), FLOAT_DELTA);
        assertEquals(10.2f, NumberUtils.max(arr), FLOAT_DELTA);

        float[] nanArr = {1.0f, Float.NaN, 2.0f};
        assertTrue(Float.isNaN(NumberUtils.min(nanArr)));
        assertTrue(Float.isNaN(NumberUtils.max(nanArr)));

        float[] nanFirst = {Float.NaN, 1.0f, 2.0f};
        assertTrue(Float.isNaN(NumberUtils.min(nanFirst)));
        assertTrue(Float.isNaN(NumberUtils.max(nanFirst)));
    }

    @Test(timeout = 4000)
    public void testThreeParamMinMaxPrimitives() {
        // long
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(2L, 1L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(1L, 3L, 2L));
        assertEquals(3L, NumberUtils.max(3L, 1L, 2L));

        // int
        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(2, 1, 3));
        assertEquals(1, NumberUtils.min(3, 2, 1));
        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(3, NumberUtils.max(1, 3, 2));
        assertEquals(3, NumberUtils.max(3, 1, 2));

        // short
        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 2, (short) 1, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 2, (short) 1));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 3, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 1, (short) 2));

        // byte
        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 2, (byte) 1, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 2, (byte) 1));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 3, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 1, (byte) 2));

        // double
        assertEquals(1.0d, NumberUtils.min(1.0d, 2.0d, 3.0d), DELTA);
        assertEquals(1.0d, NumberUtils.min(2.0d, 1.0d, 3.0d), DELTA);
        assertEquals(1.0d, NumberUtils.min(3.0d, 2.0d, 1.0d), DELTA);
        assertEquals(3.0d, NumberUtils.max(1.0d, 2.0d, 3.0d), DELTA);
        assertEquals(3.0d, NumberUtils.max(1.0d, 3.0d, 2.0d), DELTA);
        assertEquals(3.0d, NumberUtils.max(3.0d, 1.0d, 2.0d), DELTA);
        assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, 1.0d, 2.0d)));
        assertTrue(Double.isNaN(NumberUtils.max(1.0d, Double.NaN, 2.0d)));

        // float
        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), FLOAT_DELTA);
        assertEquals(1.0f, NumberUtils.min(2.0f, 1.0f, 3.0f), FLOAT_DELTA);
        assertEquals(1.0f, NumberUtils.min(3.0f, 2.0f, 1.0f), FLOAT_DELTA);
        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), FLOAT_DELTA);
        assertEquals(3.0f, NumberUtils.max(1.0f, 3.0f, 2.0f), FLOAT_DELTA);
        assertEquals(3.0f, NumberUtils.max(3.0f, 1.0f, 2.0f), FLOAT_DELTA);
        assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, 1.0f, 2.0f)));
        assertTrue(Float.isNaN(NumberUtils.max(1.0f, Float.NaN, 2.0f)));
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberDefectNegativeNegativePrefix() {
        // Known Defect: In defective versions, inputs starting with "--" return null
        // instead of throwing NumberFormatException.
        NumberUtils.createNumber("--1234");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberDefectNegativeNegativeOnly() {
        // Another defective boundary pattern returning null instead of throwing NumberFormatException
        NumberUtils.createNumber("--");
    }

    // =========================================================================
    // PARTITION D: Comprehensive createNumber & Type Creator Branch Tests
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumberValidHex() {
        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("0x12"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createNumber("0X12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-0x12"));
        assertEquals(Integer.valueOf(-0x12), NumberUtils.createNumber("-0X12"));

        // Long hex (more than 8 digits)
        assertEquals(Long.valueOf(0x123456789L), NumberUtils.createNumber("0x123456789"));
        assertEquals(Long.valueOf(-0x123456789L), NumberUtils.createNumber("-0x123456789"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberNormalDecimalsAndScientific() {
        assertNull(NumberUtils.createNumber(null));
        assertEquals(Integer.valueOf(12345), NumberUtils.createNumber("12345"));
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createNumber("1234567890123"));
        assertEquals(new BigInteger("123456789012345678901234567890"),
                NumberUtils.createNumber("123456789012345678901234567890"));

        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf(1.23F), NumberUtils.createNumber("1.23F"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf(1.23D), NumberUtils.createNumber("1.23D"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(-123L), NumberUtils.createNumber("-123L"));

        // BigInteger via L suffix overflow
        assertEquals(new BigInteger("123456789012345678901234567890"),
                NumberUtils.createNumber("123456789012345678901234567890L"));

        // Untyped float / double / BigDecimal
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23"));
        // Float overflow to Double
        Number largeFloat = NumberUtils.createNumber("3.4028236e+38");
        assertTrue(largeFloat instanceof Double);

        // Double overflow to BigDecimal
        Number largeDouble = NumberUtils.createNumber("1.7976931348623157e+309");
        assertTrue(largeDouble instanceof BigDecimal);

        // all zeros check
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));
        assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0.0d"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("   ");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberEmpty() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberExponentBeforeDecimal() {
        NumberUtils.createNumber("1e2.3");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidFloatQualifier() {
        NumberUtils.createNumber("1.2.3f");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidLongQualifierDec() {
        NumberUtils.createNumber("1.2L");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidLongQualifierExp() {
        NumberUtils.createNumber("1e2L");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidUnknownSuffix() {
        NumberUtils.createNumber("12345q");
    }

    @Test(timeout = 4000)
    public void testSpecificTypeCreators() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));

        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(1.5d), NumberUtils.createDouble("1.5"));

        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        assertEquals(Integer.valueOf(012), NumberUtils.createInteger("012")); // Octal
        assertEquals(Integer.valueOf(0x12), NumberUtils.createInteger("0x12")); // Hex

        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(123456789L), NumberUtils.createLong("123456789"));

        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("9876543210"), NumberUtils.createBigInteger("9876543210"));

        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("123.456"), NumberUtils.createBigDecimal("123.456"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("  ");
    }

    // =========================================================================
    // PARTITION E: Validation Utilities (isDigits, isNumber) & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("12a3"));
        assertFalse(NumberUtils.isDigits("-123"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertTrue(NumberUtils.isDigits("0"));
        assertTrue(NumberUtils.isDigits("1234567890"));
    }

    @Test(timeout = 4000)
    public void testIsNumber() {
        // null and empty
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));

        // hex numbers
        assertTrue(NumberUtils.isNumber("0x1234"));
        assertTrue(NumberUtils.isNumber("-0x1234"));
        assertTrue(NumberUtils.isNumber("0Xabcdef"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertFalse(NumberUtils.isNumber("0x12g3"));

        // regular decimals
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber(".45"));
        assertTrue(NumberUtils.isNumber("123."));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("1.2.3"));

        // exponents
        assertTrue(NumberUtils.isNumber("1e3"));
        assertTrue(NumberUtils.isNumber("1E3"));
        assertTrue(NumberUtils.isNumber("1.5e-3"));
        assertTrue(NumberUtils.isNumber("1.5e+3"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e-"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("1e3e3"));
        assertFalse(NumberUtils.isNumber("e3"));
        assertFalse(NumberUtils.isNumber("1.2e3.4"));

        // type qualifiers
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertTrue(NumberUtils.isNumber("123.45f"));
        assertTrue(NumberUtils.isNumber("123.45F"));
        assertTrue(NumberUtils.isNumber("123.45d"));
        assertTrue(NumberUtils.isNumber("123.45D"));
        assertFalse(NumberUtils.isNumber("123.45L"));
        assertFalse(NumberUtils.isNumber("123e4L"));
        assertFalse(NumberUtils.isNumber("f"));
        assertFalse(NumberUtils.isNumber("d"));
        assertFalse(NumberUtils.isNumber("L"));
        assertFalse(NumberUtils.isNumber("123z"));
        assertFalse(NumberUtils.isNumber("--123"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinLongNull() {
        NumberUtils.min((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinLongEmpty() {
        NumberUtils.min(new long[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxLongNull() {
        NumberUtils.max((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxLongEmpty() {
        NumberUtils.max(new long[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinIntNull() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinIntEmpty() {
        NumberUtils.min(new int[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxIntNull() {
        NumberUtils.max((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxIntEmpty() {
        NumberUtils.max(new int[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinShortNull() {
        NumberUtils.min((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinShortEmpty() {
        NumberUtils.min(new short[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxShortNull() {
        NumberUtils.max((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxShortEmpty() {
        NumberUtils.max(new short[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinByteNull() {
        NumberUtils.min((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinByteEmpty() {
        NumberUtils.min(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxByteNull() {
        NumberUtils.max((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxByteEmpty() {
        NumberUtils.max(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinDoubleNull() {
        NumberUtils.min((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinDoubleEmpty() {
        NumberUtils.min(new double[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxDoubleNull() {
        NumberUtils.max((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxDoubleEmpty() {
        NumberUtils.max(new double[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinFloatNull() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinFloatEmpty() {
        NumberUtils.min(new float[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxFloatNull() {
        NumberUtils.max((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxFloatEmpty() {
        NumberUtils.max(new float[0]);
    }
}