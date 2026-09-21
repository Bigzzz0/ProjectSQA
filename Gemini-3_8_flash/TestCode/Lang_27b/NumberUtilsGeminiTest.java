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

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * /* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target: org.apache.commons.lang3.math.NumberUtils
 *
 * 1. DEFECT UNDER TEST (Defects4J Lang-27 / LANG-638):
 *    - In createNumber(String str), the exponent position calculation:
 *      int expPos = str.indexOf('e') + str.indexOf('E') + 1;
 *      When both 'e' and 'E' are present (e.g., "1eE"), expPos exceeds the string length
 *      (e.g., 1 + 2 + 1 = 4 for length 3). Later, mant = str.substring(0, expPos) triggers:
 *      java.lang.StringIndexOutOfBoundsException: String index out of range: 4.
 *      The expected behavior for malformed scientific numbers like "1eE" is NumberFormatException,
 *      not an unhandled StringIndexOutOfBoundsException.
 *
 * 2. BRANCH & PARTITION COVERAGE MATRIX:
 *    - toInt(String, int) / toInt(String): null, empty, valid positive/negative, invalid formats.
 *    - toLong(String, long) / toLong(String): null, empty, valid, invalid formats.
 *    - toFloat(String, float) / toFloat(String): null, empty, valid, invalid formats.
 *    - toDouble(String, double) / toDouble(String): null, empty, valid, invalid formats.
 *    - toByte(String, byte) / toByte(String): null, empty, valid, overflow/invalid formats.
 *    - toShort(String, short) / toShort(String): null, empty, valid, overflow/invalid formats.
 *    - createNumber(String):
 *        * null, blank string (empty, spaces) -> throws NumberFormatException.
 *        * starts with "--" -> returns null.
 *        * starts with "0x", "-0x", "0X", "-0X" -> hexadecimal integers.
 *        * decimal point positions (decPos > -1, expPos > -1, expPos < decPos).
 *        * type specifiers ('l', 'L', 'f', 'F', 'd', 'D') with valid and overflow fallbacks.
 *        * precision checks for Float/Double (Float/Double.isInfinite() and 0.0 vs allZeros).
 *        * BigInteger and BigDecimal fallback paths.
 *        * scientific notation with positive/negative exponents.
 *    - createFloat, createDouble, createInteger, createLong, createBigInteger, createBigDecimal.
 *    - Array min/max operations for long[], int[], short[], byte[], double[], float[]:
 *        * null array -> IllegalArgumentException
 *        * empty array -> IllegalArgumentException
 *        * single element, multiple elements with min/max at start/middle/end
 *        * NaN handling: min/max on double[] and float[] returning NaN when present.
 *    - 3-parameter min/max for long, int, short, byte, double, float:
 *        * permutations of (a < b < c), (b < a < c), (c < a < b), etc.
 *        * double and float NaN propagation.
 *    - isDigits(String): null, empty, non-digit chars, digit-only strings.
 *    - isNumber(String):
 *        * null, empty -> false.
 *        * hex prefix ("0x", "-0x", valid and invalid hex digits).
 *        * leading sign ('-').
 *        * single decimal, double decimal, decimal in exponent.
 *        * exponent without digits, double exponents, exponent followed by +/- and digits.
 *        * type qualifiers ('f', 'F', 'd', 'D', 'l', 'L') at valid and invalid positions.
 * =========================================================================
 */
public class NumberUtilsGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Lang-27 Ground Truth)
    // =========================================================================

    /**
     * Target Defect: LANG-638 / Defects4J Lang-27
     * Strings containing both 'e' and 'E' like "1eE" cause expPos = 1 + 2 + 1 = 4,
     * triggering StringIndexOutOfBoundsException instead of NumberFormatException.
     */
    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testDefectLang27CreateNumberBothLowerAndUpperE() {
        NumberUtils.createNumber("1eE");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testDefectLang27CreateNumberBothLowerAndUpperEWithDigits() {
        NumberUtils.createNumber("1eE1");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testDefectLang27CreateNumberBothLowerAndUpperEWithDecimal() {
        NumberUtils.createNumber("1.0eE");
    }

    // =========================================================================
    // PARTITION A: CONSTRUCTOR & CONSTANTS INTEGRITY
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAndConstants() {
        NumberUtils utils = new NumberUtils();
        assertNotNull(utils);

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

    // =========================================================================
    // PARTITION B: TYPE CONVERTERS (toXxx with and without default values)
    // =========================================================================

    @Test(timeout = 4000)
    public void testToInt() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(5, NumberUtils.toInt(null, 5));
        assertEquals(5, NumberUtils.toInt("invalid", 5));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(-123, NumberUtils.toInt("-123", 0));
    }

    @Test(timeout = 4000)
    public void testToLong() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(99L, NumberUtils.toLong(null, 99L));
        assertEquals(99L, NumberUtils.toLong("notLong", 99L));
        assertEquals(1234567890123L, NumberUtils.toLong("1234567890123"));
        assertEquals(-1234567890123L, NumberUtils.toLong("-1234567890123", 0L));
    }

    @Test(timeout = 4000)
    public void testToFloat() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0001f);
        assertEquals(3.14f, NumberUtils.toFloat(null, 3.14f), 0.0001f);
        assertEquals(3.14f, NumberUtils.toFloat("invalidFloat", 3.14f), 0.0001f);
        assertEquals(1.25f, NumberUtils.toFloat("1.25"), 0.0001f);
        assertEquals(-1.25f, NumberUtils.toFloat("-1.25", 0.0f), 0.0001f);
    }

    @Test(timeout = 4000)
    public void testToDouble() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0001d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0001d);
        assertEquals(2.718d, NumberUtils.toDouble(null, 2.718d), 0.0001d);
        assertEquals(2.718d, NumberUtils.toDouble("invalidDouble", 2.718d), 0.0001d);
        assertEquals(123.456d, NumberUtils.toDouble("123.456"), 0.0001d);
        assertEquals(-123.456d, NumberUtils.toDouble("-123.456", 0.0d), 0.0001d);
    }

    @Test(timeout = 4000)
    public void testToByte() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 7, NumberUtils.toByte(null, (byte) 7));
        assertEquals((byte) 7, NumberUtils.toByte("999", (byte) 7)); // Byte overflow
        assertEquals((byte) 127, NumberUtils.toByte("127"));
        assertEquals((byte) -128, NumberUtils.toByte("-128", (byte) 0));
    }

    @Test(timeout = 4000)
    public void testToShort() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 42, NumberUtils.toShort(null, (short) 42));
        assertEquals((short) 42, NumberUtils.toShort("invalidShort", (short) 42));
        assertEquals((short) 32767, NumberUtils.toShort("32767"));
        assertEquals((short) -32768, NumberUtils.toShort("-32768", (short) 0));
    }

    // =========================================================================
    // PARTITION D: createXxx INDIVIDUAL CONVERTERS
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(3.14f), NumberUtils.createFloat("3.14"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateFloatInvalid() {
        NumberUtils.createFloat("abc");
    }

    @Test(timeout = 4000)
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(3.14159), NumberUtils.createDouble("3.14159"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateDoubleInvalid() {
        NumberUtils.createDouble("xyz");
    }

    @Test(timeout = 4000)
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(1234), NumberUtils.createInteger("1234"));
        assertEquals(Integer.valueOf(0xFF), NumberUtils.createInteger("0xFF"));
        assertEquals(Integer.valueOf(077), NumberUtils.createInteger("077"));
        assertEquals(Integer.valueOf(-10), NumberUtils.createInteger("-10"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateIntegerInvalid() {
        NumberUtils.createInteger("invalidInt");
    }

    @Test(timeout = 4000)
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createLong("1234567890123"));
        assertEquals(Long.valueOf(-1234567890123L), NumberUtils.createLong("-1234567890123"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateLongInvalid() {
        NumberUtils.createLong("invalidLong");
    }

    @Test(timeout = 4000)
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("123456789012345678901234567890"),
                NumberUtils.createBigInteger("123456789012345678901234567890"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigIntegerInvalid() {
        NumberUtils.createBigInteger("invalidBigInt");
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("1234567890.1234567890"),
                NumberUtils.createBigDecimal("1234567890.1234567890"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("   ");
    }

    // =========================================================================
    // PARTITION E: createNumber COMPREHENSIVE BRANCHING
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumberBasicAndEdgeCases() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--123")); // Special OS X guard returns null

        // Hexadecimal notation
        assertEquals(Integer.valueOf(0x1A), NumberUtils.createNumber("0x1A"));
        assertEquals(Integer.valueOf(-0x1A), NumberUtils.createNumber("-0x1A"));

        // Normal Integer, Long, BigInteger
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(2147483648L), NumberUtils.createNumber("2147483648")); // > Integer.MAX_VALUE
        assertEquals(new BigInteger("9223372036854775808"),
                NumberUtils.createNumber("9223372036854775808")); // > Long.MAX_VALUE

        // Trailing Long qualifier ('l', 'L')
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(-123L), NumberUtils.createNumber("-123L"));
        assertEquals(new BigInteger("9223372036854775808"),
                NumberUtils.createNumber("9223372036854775808L")); // Overflow Long -> BigInteger

        // Trailing Float qualifier ('f', 'F')
        assertEquals(Float.valueOf(123.45f), NumberUtils.createNumber("123.45f"));
        assertEquals(Float.valueOf(123.45f), NumberUtils.createNumber("123.45F"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));

        // Float overflow or loss of precision fallthrough to Double / BigDecimal
        Number bigFloatNumber = NumberUtils.createNumber("3.4028236E39f"); // Exceeds Float.MAX_VALUE
        assertTrue(bigFloatNumber instanceof Double || bigFloatNumber instanceof BigDecimal);

        // Trailing Double qualifier ('d', 'D')
        assertEquals(Double.valueOf(123.45d), NumberUtils.createNumber("123.45d"));
        assertEquals(Double.valueOf(123.45d), NumberUtils.createNumber("123.45D"));
        assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0.0d"));

        // Untyped floating point (Float -> Double -> BigDecimal)
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23"));
        assertEquals(Double.valueOf(1.7976931348623157E308),
                NumberUtils.createNumber("1.7976931348623157E308"));
        // Large scientific notation falling to BigDecimal
        assertEquals(new BigDecimal("1.7976931348623159E309"),
                NumberUtils.createNumber("1.7976931348623159E309"));

        // All zeros with exponent
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0e0"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0e0f"));
        assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0.0e0d"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("   ");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberExpBeforeDec() {
        // expPos < decPos
        NumberUtils.createNumber("1e2.3");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberLongWithDecimal() {
        NumberUtils.createNumber("123.45L");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberLongWithExponent() {
        NumberUtils.createNumber("123e4L");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidLastChar() {
        NumberUtils.createNumber("123z");
    }

    // =========================================================================
    // PARTITION F: ARRAY MIN / MAX OPERATIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinLongArray() {
        assertEquals(1L, NumberUtils.min(new long[]{1L}));
        assertEquals(2L, NumberUtils.min(new long[]{10L, 2L, 5L}));
        assertEquals(-7L, NumberUtils.min(new long[]{5L, -7L, 3L}));
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
        assertEquals(5, NumberUtils.min(new int[]{5}));
        assertEquals(1, NumberUtils.min(new int[]{8, 3, 1, 9}));
        assertEquals(-10, NumberUtils.min(new int[]{-10, 0, 10}));
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
        assertEquals((short) 10, NumberUtils.min(new short[]{10}));
        assertEquals((short) -2, NumberUtils.min(new short[]{5, -2, 4}));
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
        assertEquals((byte) 5, NumberUtils.min(new byte[]{5}));
        assertEquals((byte) -8, NumberUtils.min(new byte[]{1, -8, 12}));
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
        assertEquals(1.5d, NumberUtils.min(new double[]{1.5d}), 0.0001d);
        assertEquals(-2.5d, NumberUtils.min(new double[]{3.0d, -2.5d, 4.0d}), 0.0001d);
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
        assertEquals(1.5f, NumberUtils.min(new float[]{1.5f}), 0.0001f);
        assertEquals(-2.5f, NumberUtils.min(new float[]{3.0f, -2.5f, 4.0f}), 0.0001f);
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
        assertEquals(1L, NumberUtils.max(new long[]{1L}));
        assertEquals(10L, NumberUtils.max(new long[]{2L, 10L, 5L}));
        assertEquals(-1L, NumberUtils.max(new long[]{-5L, -1L, -7L}));
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
        assertEquals(5, NumberUtils.max(new int[]{5}));
        assertEquals(9, NumberUtils.max(new int[]{8, 3, 1, 9}));
        assertEquals(0, NumberUtils.max(new int[]{-10, 0, -2}));
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
        assertEquals((short) 10, NumberUtils.max(new short[]{10}));
        assertEquals((short) 5, NumberUtils.max(new short[]{-2, 5, 4}));
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
        assertEquals((byte) 5, NumberUtils.max(new byte[]{5}));
        assertEquals((byte) 12, NumberUtils.max(new byte[]{1, -8, 12}));
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
        assertEquals(1.5d, NumberUtils.max(new double[]{1.5d}), 0.0001d);
        assertEquals(4.0d, NumberUtils.max(new double[]{3.0d, -2.5d, 4.0d}), 0.0001d);
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
        assertEquals(1.5f, NumberUtils.max(new float[]{1.5f}), 0.0001f);
        assertEquals(4.0f, NumberUtils.max(new float[]{3.0f, -2.5f, 4.0f}), 0.0001f);
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
    // PARTITION G: 3-PARAMETER MIN / MAX
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinThreePrimitives() {
        // long
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(2L, 1L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));

        // int
        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(2, 1, 3));
        assertEquals(1, NumberUtils.min(3, 2, 1));

        // short
        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 2, (short) 1, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 2, (short) 1));

        // byte
        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 2, (byte) 1, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 2, (byte) 1));

        // double
        assertEquals(1.0d, NumberUtils.min(1.0d, 2.0d, 3.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(2.0d, 1.0d, 3.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(3.0d, 2.0d, 1.0d), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.min(1.0d, Double.NaN, 3.0d)));

        // float
        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(2.0f, 1.0f, 3.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(3.0f, 2.0f, 1.0f), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.min(1.0f, Float.NaN, 3.0f)));
    }

    @Test(timeout = 4000)
    public void testMaxThreePrimitives() {
        // long
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(1L, 3L, 2L));
        assertEquals(3L, NumberUtils.max(3L, 2L, 1L));

        // int
        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(3, NumberUtils.max(1, 3, 2));
        assertEquals(3, NumberUtils.max(3, 2, 1));

        // short
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 3, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 2, (short) 1));

        // byte
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 3, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 2, (byte) 1));

        // double
        assertEquals(3.0d, NumberUtils.max(1.0d, 2.0d, 3.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(1.0d, 3.0d, 2.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(3.0d, 2.0d, 1.0d), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.max(1.0d, Double.NaN, 3.0d)));

        // float
        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(1.0f, 3.0f, 2.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(3.0f, 2.0f, 1.0f), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.max(1.0f, Float.NaN, 3.0f)));
    }

    // =========================================================================
    // PARTITION H: STRING VALIDATORS (isDigits and isNumber)
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("  "));
        assertFalse(NumberUtils.isDigits("12a34"));
        assertFalse(NumberUtils.isDigits("-1234"));
        assertFalse(NumberUtils.isDigits("12.34"));
        assertTrue(NumberUtils.isDigits("0"));
        assertTrue(NumberUtils.isDigits("1234567890"));
    }

    @Test(timeout = 4000)
    public void testIsNumberHex() {
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertTrue(NumberUtils.isNumber("0x1234ABC"));
        assertTrue(NumberUtils.isNumber("-0x1234abc"));
        assertFalse(NumberUtils.isNumber("0x1234G")); // invalid hex char
        assertFalse(NumberUtils.isNumber("-0x1234G"));
    }

    @Test(timeout = 4000)
    public void testIsNumberStandardAndScientific() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("   "));
        assertFalse(NumberUtils.isNumber("-"));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("1.2.3")); // multiple decimals
        assertFalse(NumberUtils.isNumber("1e2e3")); // multiple exponents
        assertFalse(NumberUtils.isNumber("e1")); // exponent without mantissa
        assertFalse(NumberUtils.isNumber("1e")); // ends with exponent
        assertFalse(NumberUtils.isNumber("1e+")); // ends with sign in exponent
        assertFalse(NumberUtils.isNumber("1e-")); // ends with sign in exponent
        assertFalse(NumberUtils.isNumber("1e+a")); // non-digit after sign

        assertTrue(NumberUtils.isNumber("0"));
        assertTrue(NumberUtils.isNumber("-10"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber("-123.45"));
        assertTrue(NumberUtils.isNumber(".45"));
        assertTrue(NumberUtils.isNumber("45."));
        assertTrue(NumberUtils.isNumber("123e4"));
        assertTrue(NumberUtils.isNumber("123e+4"));
        assertTrue(NumberUtils.isNumber("123e-4"));
        assertTrue(NumberUtils.isNumber("123.45e-4"));
        assertFalse(NumberUtils.isNumber("123.45e-4.5")); // decimal in exponent
    }

    @Test(timeout = 4000)
    public void testIsNumberTypeQualifiers() {
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertTrue(NumberUtils.isNumber("-123L"));
        assertFalse(NumberUtils.isNumber("123.45L")); // L with decimal
        assertFalse(NumberUtils.isNumber("123e4L")); // L with exponent
        assertFalse(NumberUtils.isNumber("L"));

        assertTrue(NumberUtils.isNumber("123d"));
        assertTrue(NumberUtils.isNumber("123D"));
        assertTrue(NumberUtils.isNumber("123.45d"));
        assertTrue(NumberUtils.isNumber("123.45D"));
        assertTrue(NumberUtils.isNumber("123e4d"));

        assertTrue(NumberUtils.isNumber("123f"));
        assertTrue(NumberUtils.isNumber("123F"));
        assertTrue(NumberUtils.isNumber("123.45f"));
        assertTrue(NumberUtils.isNumber("123.45F"));
        assertTrue(NumberUtils.isNumber("123e4f"));

        assertFalse(NumberUtils.isNumber("123z")); // illegal qualifier
        assertFalse(NumberUtils.isNumber("d"));
        assertFalse(NumberUtils.isNumber(".d"));
    }
}