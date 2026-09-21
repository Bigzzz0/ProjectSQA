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
 * Target Class: org.apache.commons.lang3.math.NumberUtils
 *
 * Targeted Defects & Decision Branches:
 * 1. LANG-664 / Defect Zone:
 *    - isNumber(String): Hex numbers starting with upper-case "0X" or "-0X" must be recognized
 *      as valid numbers.
 * 2. Conversions (toInt, toLong, toFloat, toDouble, toByte, toShort):
 *    - Null, empty string, valid number strings, invalid strings triggering default fallback.
 * 3. createNumber(String):
 *    - Null, blank, "--" prefix handling (OS X bug guard), "0x" / "-0x" hex branches.
 *    - Decimal point handling with/without scientific exponent ('e', 'E').
 *    - Type qualifiers: 'l', 'L', 'f', 'F', 'd', 'D' and fallback to BigDecimal/BigInteger.
 *    - Precision preservation (Float/Double zero checking against isAllZeros).
 *    - Sign-handling and invalid formats throwing NumberFormatException.
 * 4. createFloat, createDouble, createInteger, createLong, createBigInteger, createBigDecimal:
 *    - Null strings, standard formats, blank strings in BigDecimal, hex/octal decoding.
 * 5. Array min / max functions (long, int, short, byte, double, float):
 *    - Null arrays and empty arrays (IllegalArgumentException).
 *    - Single-element arrays, multiple-element arrays with positive/negative/duplicate elements.
 *    - Double and Float NaN propagation in array min/max.
 * 6. Ternary min / max functions:
 *    - All ordering permutations (a < b < c, b < a < c, c < a, etc.) across long, int, short, byte.
 *    - Double/Float NaN and Infinity handling in ternary min/max.
 * 7. isDigits(String):
 *    - Null, empty, pure digits, signs, alphabetic and mixed unicode characters.
 * 8. isNumber(String):
 *    - Hex variations (0x, -0x, 0X, -0X, invalid hex digits, empty after prefix "0x").
 *    - Floating point forms (e.g. "1.0", ".5", "5.", "1e1", "1e+1", "1e-1", "1.1e1").
 *    - Qualifiers ('f', 'F', 'd', 'D', 'l', 'L'), multiple decimals, illegal exponent positions.
 */
public class NumberUtilsGeminiTest {

    private static final float FLOAT_DELTA = 0.0001f;
    private static final double DOUBLE_DELTA = 0.000001d;

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth: LANG-664)
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsNumberLang664HexUpperCase() {
        // LANG-664: isNumber should return true for valid hexadecimal numbers with '0X' prefix
        assertTrue("isNumber(String) LANG-664 failed: '0X1234' must be recognized as valid",
                NumberUtils.isNumber("0X1234"));
        assertTrue("isNumber(String) LANG-664 failed: '0Xabc' must be recognized as valid",
                NumberUtils.isNumber("0Xabc"));
        assertTrue("isNumber(String) LANG-664 failed: '0XABCDEF' must be recognized as valid",
                NumberUtils.isNumber("0XABCDEF"));
        assertTrue("isNumber(String) LANG-664 failed: '-0X1234' must be recognized as valid",
                NumberUtils.isNumber("-0X1234"));
        assertTrue("isNumber(String) LANG-664 failed: '-0Xabc' must be recognized as valid",
                NumberUtils.isNumber("-0Xabc"));
        assertTrue("isNumber(String) LANG-664 failed: '-0XABCDEF' must be recognized as valid",
                NumberUtils.isNumber("-0XABCDEF"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Conversions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        assertNotNull(new NumberUtils());
    }

    @Test(timeout = 4000)
    public void testConstants() {
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

        assertEquals(0.0d, NumberUtils.DOUBLE_ZERO, DOUBLE_DELTA);
        assertEquals(1.0d, NumberUtils.DOUBLE_ONE, DOUBLE_DELTA);
        assertEquals(-1.0d, NumberUtils.DOUBLE_MINUS_ONE, DOUBLE_DELTA);

        assertEquals(0.0f, NumberUtils.FLOAT_ZERO, FLOAT_DELTA);
        assertEquals(1.0f, NumberUtils.FLOAT_ONE, FLOAT_DELTA);
        assertEquals(-1.0f, NumberUtils.FLOAT_MINUS_ONE, FLOAT_DELTA);
    }

    @Test(timeout = 4000)
    public void testToInt() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(-456, NumberUtils.toInt("-456"));

        assertEquals(5, NumberUtils.toInt(null, 5));
        assertEquals(5, NumberUtils.toInt("invalid", 5));
        assertEquals(123, NumberUtils.toInt("123", 5));
    }

    @Test(timeout = 4000)
    public void testToLong() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(0L, NumberUtils.toLong("xyz"));
        assertEquals(123456789012L, NumberUtils.toLong("123456789012"));
        assertEquals(-9876543210L, NumberUtils.toLong("-9876543210"));

        assertEquals(99L, NumberUtils.toLong(null, 99L));
        assertEquals(99L, NumberUtils.toLong("error", 99L));
        assertEquals(10L, NumberUtils.toLong("10", 99L));
    }

    @Test(timeout = 4000)
    public void testToFloat() {
        assertEquals(0.0f, NumberUtils.toFloat(null), FLOAT_DELTA);
        assertEquals(0.0f, NumberUtils.toFloat(""), FLOAT_DELTA);
        assertEquals(0.0f, NumberUtils.toFloat("fail"), FLOAT_DELTA);
        assertEquals(3.1415f, NumberUtils.toFloat("3.1415"), FLOAT_DELTA);
        assertEquals(-2.718f, NumberUtils.toFloat("-2.718"), FLOAT_DELTA);

        assertEquals(1.23f, NumberUtils.toFloat(null, 1.23f), FLOAT_DELTA);
        assertEquals(1.23f, NumberUtils.toFloat("invalid", 1.23f), FLOAT_DELTA);
        assertEquals(4.56f, NumberUtils.toFloat("4.56", 1.23f), FLOAT_DELTA);
    }

    @Test(timeout = 4000)
    public void testToDouble() {
        assertEquals(0.0d, NumberUtils.toDouble(null), DOUBLE_DELTA);
        assertEquals(0.0d, NumberUtils.toDouble(""), DOUBLE_DELTA);
        assertEquals(0.0d, NumberUtils.toDouble("fail"), DOUBLE_DELTA);
        assertEquals(3.1415926535d, NumberUtils.toDouble("3.1415926535"), DOUBLE_DELTA);
        assertEquals(-2.7182818284d, NumberUtils.toDouble("-2.7182818284"), DOUBLE_DELTA);

        assertEquals(9.87d, NumberUtils.toDouble(null, 9.87d), DOUBLE_DELTA);
        assertEquals(9.87d, NumberUtils.toDouble("bad", 9.87d), DOUBLE_DELTA);
        assertEquals(1.23d, NumberUtils.toDouble("1.23", 9.87d), DOUBLE_DELTA);
    }

    @Test(timeout = 4000)
    public void testToByte() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 0, NumberUtils.toByte("bad"));
        assertEquals((byte) 127, NumberUtils.toByte("127"));
        assertEquals((byte) -128, NumberUtils.toByte("-128"));

        assertEquals((byte) 7, NumberUtils.toByte(null, (byte) 7));
        assertEquals((byte) 7, NumberUtils.toByte("bad", (byte) 7));
        assertEquals((byte) 42, NumberUtils.toByte("42", (byte) 7));
    }

    @Test(timeout = 4000)
    public void testToShort() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 0, NumberUtils.toShort("bad"));
        assertEquals((short) 32767, NumberUtils.toShort("32767"));
        assertEquals((short) -32768, NumberUtils.toShort("-32768"));

        assertEquals((short) 12, NumberUtils.toShort(null, (short) 12));
        assertEquals((short) 12, NumberUtils.toShort("bad", (short) 12));
        assertEquals((short) 1234, NumberUtils.toShort("1234", (short) 12));
    }

    // =========================================================================
    // Partition B: Specific Type Creation Methods (createFloat, createDouble, etc.)
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(12.34f), NumberUtils.createFloat("12.34"));
    }

    @Test(timeout = 4000)
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(1234.5678d), NumberUtils.createDouble("1234.5678"));
    }

    @Test(timeout = 4000)
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        assertEquals(Integer.valueOf(0x7F), NumberUtils.createInteger("0x7F"));
        assertEquals(Integer.valueOf(010), NumberUtils.createInteger("010"));
    }

    @Test(timeout = 4000)
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createLong("1234567890123"));
    }

    @Test(timeout = 4000)
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("123456789012345678901234567890"),
                NumberUtils.createBigInteger("123456789012345678901234567890"));
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("123456.789012"), NumberUtils.createBigDecimal("123456.789012"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("  ");
    }

    // =========================================================================
    // Partition B & C: createNumber Systematic Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumberNullAndSpecialCases() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--123"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("   ");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberEmpty() {
        NumberUtils.createNumber("");
    }

    @Test(timeout = 4000)
    public void testCreateNumberHex() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xff"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberIntegerTypes() {
        assertEquals(Integer.valueOf(12345), NumberUtils.createNumber("12345"));
        assertEquals(Integer.valueOf(-12345), NumberUtils.createNumber("-12345"));
        assertEquals(Long.valueOf(2147483648L), NumberUtils.createNumber("2147483648"));
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithLongQualifier() {
        assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345l"));
        assertEquals(Long.valueOf(12345L), NumberUtils.createNumber("12345L"));
        assertEquals(Long.valueOf(-12345L), NumberUtils.createNumber("-12345L"));
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808L"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberWithLongQualifierInvalidDec() {
        NumberUtils.createNumber("123.45L");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberWithLongQualifierInvalidExp() {
        NumberUtils.createNumber("123e4L");
    }

    @Test(timeout = 4000)
    public void testCreateNumberFloatingPointNoQualifier() {
        assertEquals(Float.valueOf("1.234"), NumberUtils.createNumber("1.234"));
        assertEquals(Double.valueOf("1.7976931348623157e+308"), NumberUtils.createNumber("1.7976931348623157e+308"));
        assertEquals(new BigDecimal("1.7976931348623157e+309"), NumberUtils.createNumber("1.7976931348623157e+309"));
        assertEquals(Float.valueOf("0.0"), NumberUtils.createNumber("0.0"));
        assertEquals(Float.valueOf("0.00"), NumberUtils.createNumber(".00"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithFloatQualifier() {
        assertEquals(Float.valueOf("1.23f"), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf("1.23F"), NumberUtils.createNumber("1.23F"));
        assertEquals(Float.valueOf("0.0f"), NumberUtils.createNumber("0.0f"));
        // If too large for float, falls through to Double or BigDecimal
        Number bigF = NumberUtils.createNumber("3.4028236e+39f");
        assertTrue(bigF instanceof Double || bigF instanceof BigDecimal);
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithDoubleQualifier() {
        assertEquals(Double.valueOf("1.23d"), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf("1.23D"), NumberUtils.createNumber("1.23D"));
        assertEquals(Double.valueOf("0.0d"), NumberUtils.createNumber("0.0d"));
        // Overflow double falls through to BigDecimal
        Number bigD = NumberUtils.createNumber("1.7976931348623159e+309d");
        assertTrue(bigD instanceof BigDecimal);
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidExponentPositionDec() {
        NumberUtils.createNumber("1.2e");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidExponentPositionNoDec() {
        NumberUtils.createNumber("12e");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidExponentOrder() {
        // expPos < decPos
        NumberUtils.createNumber("1e2.3");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidTrailingChar() {
        NumberUtils.createNumber("12345X");
    }

    // =========================================================================
    // Partition A & B: min & max on Arrays
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinLongArray() {
        assertEquals(1L, NumberUtils.min(new long[]{1L}));
        assertEquals(-10L, NumberUtils.min(new long[]{5L, -10L, 20L, 0L}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinLongArrayNull() {
        NumberUtils.min((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinLongArrayEmpty() {
        NumberUtils.min(new long[]{});
    }

    @Test(timeout = 4000)
    public void testMinIntArray() {
        assertEquals(1, NumberUtils.min(new int[]{1}));
        assertEquals(-20, NumberUtils.min(new int[]{10, 5, -20, 30}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinIntArrayNull() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinIntArrayEmpty() {
        NumberUtils.min(new int[]{});
    }

    @Test(timeout = 4000)
    public void testMinShortArray() {
        assertEquals((short) 1, NumberUtils.min(new short[]{(short) 1}));
        assertEquals((short) -5, NumberUtils.min(new short[]{(short) 3, (short) -5, (short) 10}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinShortArrayNull() {
        NumberUtils.min((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinShortArrayEmpty() {
        NumberUtils.min(new short[]{});
    }

    @Test(timeout = 4000)
    public void testMinByteArray() {
        assertEquals((byte) 5, NumberUtils.min(new byte[]{(byte) 5}));
        assertEquals((byte) -12, NumberUtils.min(new byte[]{(byte) 8, (byte) -12, (byte) 3}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinByteArrayNull() {
        NumberUtils.min((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinByteArrayEmpty() {
        NumberUtils.min(new byte[]{});
    }

    @Test(timeout = 4000)
    public void testMinDoubleArray() {
        assertEquals(1.1d, NumberUtils.min(new double[]{1.1d}), DOUBLE_DELTA);
        assertEquals(-2.5d, NumberUtils.min(new double[]{3.2d, -2.5d, 4.0d}), DOUBLE_DELTA);
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.0d, Double.NaN, 2.0d})));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinDoubleArrayNull() {
        NumberUtils.min((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinDoubleArrayEmpty() {
        NumberUtils.min(new double[]{});
    }

    @Test(timeout = 4000)
    public void testMinFloatArray() {
        assertEquals(2.0f, NumberUtils.min(new float[]{2.0f}), FLOAT_DELTA);
        assertEquals(-1.5f, NumberUtils.min(new float[]{5.5f, 0.0f, -1.5f}), FLOAT_DELTA);
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.0f, Float.NaN, 2.0f})));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinFloatArrayNull() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinFloatArrayEmpty() {
        NumberUtils.min(new float[]{});
    }

    @Test(timeout = 4000)
    public void testMaxLongArray() {
        assertEquals(10L, NumberUtils.max(new long[]{10L}));
        assertEquals(30L, NumberUtils.max(new long[]{5L, 30L, -10L, 0L}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxLongArrayNull() {
        NumberUtils.max((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxLongArrayEmpty() {
        NumberUtils.max(new long[]{});
    }

    @Test(timeout = 4000)
    public void testMaxIntArray() {
        assertEquals(7, NumberUtils.max(new int[]{7}));
        assertEquals(50, NumberUtils.max(new int[]{10, 50, -20, 30}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxIntArrayNull() {
        NumberUtils.max((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxIntArrayEmpty() {
        NumberUtils.max(new int[]{});
    }

    @Test(timeout = 4000)
    public void testMaxShortArray() {
        assertEquals((short) 9, NumberUtils.max(new short[]{(short) 9}));
        assertEquals((short) 20, NumberUtils.max(new short[]{(short) 3, (short) 20, (short) -5}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxShortArrayNull() {
        NumberUtils.max((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxShortArrayEmpty() {
        NumberUtils.max(new short[]{});
    }

    @Test(timeout = 4000)
    public void testMaxByteArray() {
        assertEquals((byte) 4, NumberUtils.max(new byte[]{(byte) 4}));
        assertEquals((byte) 15, NumberUtils.max(new byte[]{(byte) 8, (byte) 15, (byte) -12}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxByteArrayNull() {
        NumberUtils.max((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxByteArrayEmpty() {
        NumberUtils.max(new byte[]{});
    }

    @Test(timeout = 4000)
    public void testMaxDoubleArray() {
        assertEquals(1.1d, NumberUtils.max(new double[]{1.1d}), DOUBLE_DELTA);
        assertEquals(9.9d, NumberUtils.max(new double[]{3.2d, 9.9d, 4.0d}), DOUBLE_DELTA);
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0d, Double.NaN, 2.0d})));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxDoubleArrayNull() {
        NumberUtils.max((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxDoubleArrayEmpty() {
        NumberUtils.max(new double[]{});
    }

    @Test(timeout = 4000)
    public void testMaxFloatArray() {
        assertEquals(3.3f, NumberUtils.max(new float[]{3.3f}), FLOAT_DELTA);
        assertEquals(7.7f, NumberUtils.max(new float[]{1.1f, 7.7f, -1.5f}), FLOAT_DELTA);
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN, 2.0f})));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxFloatArrayNull() {
        NumberUtils.max((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxFloatArrayEmpty() {
        NumberUtils.max(new float[]{});
    }

    // =========================================================================
    // Partition A: 3-Parameter min & max Methods
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinLongThreeArgs() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(2L, 1L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));
        assertEquals(1L, NumberUtils.min(1L, 1L, 1L));
    }

    @Test(timeout = 4000)
    public void testMinIntThreeArgs() {
        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(2, 1, 3));
        assertEquals(1, NumberUtils.min(3, 2, 1));
        assertEquals(2, NumberUtils.min(2, 2, 3));
    }

    @Test(timeout = 4000)
    public void testMinShortThreeArgs() {
        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 2, (short) 1, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 2, (short) 1));
    }

    @Test(timeout = 4000)
    public void testMinByteThreeArgs() {
        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 2, (byte) 1, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 2, (byte) 1));
    }

    @Test(timeout = 4000)
    public void testMinDoubleThreeArgs() {
        assertEquals(1.0d, NumberUtils.min(1.0d, 2.0d, 3.0d), DOUBLE_DELTA);
        assertEquals(1.0d, NumberUtils.min(2.0d, 1.0d, 3.0d), DOUBLE_DELTA);
        assertEquals(1.0d, NumberUtils.min(3.0d, 2.0d, 1.0d), DOUBLE_DELTA);
        assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, 2.0d, 3.0d)));
        assertTrue(Double.isNaN(NumberUtils.min(1.0d, Double.NaN, 3.0d)));
        assertTrue(Double.isNaN(NumberUtils.min(1.0d, 2.0d, Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testMinFloatThreeArgs() {
        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), FLOAT_DELTA);
        assertEquals(1.0f, NumberUtils.min(2.0f, 1.0f, 3.0f), FLOAT_DELTA);
        assertEquals(1.0f, NumberUtils.min(3.0f, 2.0f, 1.0f), FLOAT_DELTA);
        assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, 2.0f, 3.0f)));
        assertTrue(Float.isNaN(NumberUtils.min(1.0f, Float.NaN, 3.0f)));
        assertTrue(Float.isNaN(NumberUtils.min(1.0f, 2.0f, Float.NaN)));
    }

    @Test(timeout = 4000)
    public void testMaxLongThreeArgs() {
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(1L, 3L, 2L));
        assertEquals(3L, NumberUtils.max(3L, 2L, 1L));
        assertEquals(2L, NumberUtils.max(2L, 2L, 1L));
    }

    @Test(timeout = 4000)
    public void testMaxIntThreeArgs() {
        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(3, NumberUtils.max(1, 3, 2));
        assertEquals(3, NumberUtils.max(3, 2, 1));
        assertEquals(2, NumberUtils.max(2, 2, 1));
    }

    @Test(timeout = 4000)
    public void testMaxShortThreeArgs() {
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 3, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 2, (short) 1));
    }

    @Test(timeout = 4000)
    public void testMaxByteThreeArgs() {
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 3, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 2, (byte) 1));
    }

    @Test(timeout = 4000)
    public void testMaxDoubleThreeArgs() {
        assertEquals(3.0d, NumberUtils.max(1.0d, 2.0d, 3.0d), DOUBLE_DELTA);
        assertEquals(3.0d, NumberUtils.max(1.0d, 3.0d, 2.0d), DOUBLE_DELTA);
        assertEquals(3.0d, NumberUtils.max(3.0d, 2.0d, 1.0d), DOUBLE_DELTA);
        assertTrue(Double.isNaN(NumberUtils.max(Double.NaN, 2.0d, 3.0d)));
        assertTrue(Double.isNaN(NumberUtils.max(1.0d, Double.NaN, 3.0d)));
        assertTrue(Double.isNaN(NumberUtils.max(1.0d, 2.0d, Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testMaxFloatThreeArgs() {
        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), FLOAT_DELTA);
        assertEquals(3.0f, NumberUtils.max(1.0f, 3.0f, 2.0f), FLOAT_DELTA);
        assertEquals(3.0f, NumberUtils.max(3.0f, 2.0f, 1.0f), FLOAT_DELTA);
        assertTrue(Float.isNaN(NumberUtils.max(Float.NaN, 2.0f, 3.0f)));
        assertTrue(Float.isNaN(NumberUtils.max(1.0f, Float.NaN, 3.0f)));
        assertTrue(Float.isNaN(NumberUtils.max(1.0f, 2.0f, Float.NaN)));
    }

    // =========================================================================
    // Partition A & B: isDigits
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("  "));
        assertFalse(NumberUtils.isDigits("123a45"));
        assertFalse(NumberUtils.isDigits("-123"));
        assertFalse(NumberUtils.isDigits("12.3"));

        assertTrue(NumberUtils.isDigits("0"));
        assertTrue(NumberUtils.isDigits("1234567890"));
    }

    // =========================================================================
    // Partition A, B, & D: isNumber Decision Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsNumberBaseCases() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("   "));
        assertFalse(NumberUtils.isNumber("-"));
        assertFalse(NumberUtils.isNumber("+"));
    }

    @Test(timeout = 4000)
    public void testIsNumberHex() {
        assertTrue(NumberUtils.isNumber("0x1234"));
        assertTrue(NumberUtils.isNumber("0xabcdef"));
        assertTrue(NumberUtils.isNumber("0xABCDEF"));
        assertTrue(NumberUtils.isNumber("-0x1234"));
        assertTrue(NumberUtils.isNumber("-0xabcdef"));
        assertTrue(NumberUtils.isNumber("-0xABCDEF"));

        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertFalse(NumberUtils.isNumber("0x123g"));
        assertFalse(NumberUtils.isNumber("-0x123g"));
    }

    @Test(timeout = 4000)
    public void testIsNumberDecimalAndScientific() {
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("12.34"));
        assertTrue(NumberUtils.isNumber("-12.34"));
        assertTrue(NumberUtils.isNumber(".34"));
        assertTrue(NumberUtils.isNumber("12."));

        assertTrue(NumberUtils.isNumber("1e2"));
        assertTrue(NumberUtils.isNumber("1E2"));
        assertTrue(NumberUtils.isNumber("1.2e3"));
        assertTrue(NumberUtils.isNumber("1.2e+3"));
        assertTrue(NumberUtils.isNumber("1.2e-3"));
        assertTrue(NumberUtils.isNumber("-1.2e-3"));

        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("e3"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("1e-"));
        assertFalse(NumberUtils.isNumber("1.2e3.4"));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("-."));
    }

    @Test(timeout = 4000)
    public void testIsNumberTypeQualifiers() {
        assertTrue(NumberUtils.isNumber("123f"));
        assertTrue(NumberUtils.isNumber("123F"));
        assertTrue(NumberUtils.isNumber("123.4f"));
        assertTrue(NumberUtils.isNumber("123.4F"));
        assertTrue(NumberUtils.isNumber("1.2e3f"));

        assertTrue(NumberUtils.isNumber("123d"));
        assertTrue(NumberUtils.isNumber("123D"));
        assertTrue(NumberUtils.isNumber("123.4d"));
        assertTrue(NumberUtils.isNumber("123.4D"));
        assertTrue(NumberUtils.isNumber("1.2e3d"));

        assertTrue(NumberUtils.isNumber("123l"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("-123L"));

        // 'l' or 'L' with decimal point or exponent is not allowed in Java literals
        assertFalse(NumberUtils.isNumber("123.4L"));
        assertFalse(NumberUtils.isNumber("1.2e3L"));
        assertFalse(NumberUtils.isNumber("123e4L"));

        // Illegal characters as qualifiers
        assertFalse(NumberUtils.isNumber("123a"));
        assertFalse(NumberUtils.isNumber("123z"));
        assertFalse(NumberUtils.isNumber("f"));
        assertFalse(NumberUtils.isNumber("d"));
        assertFalse(NumberUtils.isNumber("l"));
    }
}