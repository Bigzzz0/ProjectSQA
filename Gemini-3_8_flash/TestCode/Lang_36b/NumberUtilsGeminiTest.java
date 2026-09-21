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

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.lang3.math.NumberUtils
 *
 * 1. Defect LANG-521:
 *    - NumberUtils.createNumber("2.") throws NumberFormatException instead of parsing floating-point number.
 *    - NumberUtils.isNumber("2.") returns false instead of true because the trailing decimal point was not
 *      considered a valid terminus without following digits or exponents.
 *
 * 2. Coverage Branches & Partitions:
 *    - Partition A (Functional Logic & State):
 *      * Primitive conversions: toInt, toLong, toFloat, toDouble, toByte, toShort (with and without defaults).
 *      * Number factory methods: createInteger (dec, hex, octal), createLong, createBigInteger, createBigDecimal, createFloat, createDouble.
 *      * min/max across 3-parameter overloads (long, int, short, byte, float, double).
 *      * min/max across array overloads (long[], int[], short[], byte[], float[], double[]).
 *      * isDigits / isNumber across canonical numbers, scientific notations, hex representations.
 *    - Partition B (Boundary Value Analysis):
 *      * Extreme values: Long.MIN_VALUE, Long.MAX_VALUE, Integer.MIN_VALUE, Float/Double MIN/MAX.
 *      * Empty strings, blank strings, null arguments.
 *      * Decimal point / Exponent boundaries in createNumber (expPos < decPos, multiple exponents, etc.).
 *      * Arrays with NaN values (IEEE 754 specifics in float/double min/max).
 *      * Array length boundaries (empty array, single element, negative values).
 *    - Partition C (Defect-Targeted Zone - LANG-521):
 *      * isNumber("2.") and createNumber("2.")
 *      * Negative variants ("-2.", "-.1", ".5")
 *    - Partition D (Exception & Defensive Guard Paths):
 *      * IllegalArgumentException on empty or null arrays for all min/max methods.
 *      * NumberFormatException on illegal type specifiers, multiple decimal points, malformed exponents.
 *    - Partition E (Lifecycle & Constant Integrity):
 *      * Public no-arg constructor execution.
 *      * Public static final constants verification.
 */
public class NumberUtilsGeminiTest {

    // =========================================================================
    // Partition E: Object Lifecycle & Constant Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        NumberUtils nu = new NumberUtils();
        assertNotNull(nu);
    }

    @Test(timeout = 4000)
    public void testStaticConstants() {
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
    // Partition A: Core Functional Logic & Primitive Conversions
    // =========================================================================

    @Test(timeout = 4000)
    public void testToInt() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(0, NumberUtils.toInt("invalid"));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(42, NumberUtils.toInt(null, 42));
        assertEquals(42, NumberUtils.toInt("invalid", 42));
        assertEquals(-7, NumberUtils.toInt("-7", 42));
    }

    @Test(timeout = 4000)
    public void testToLong() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(0L, NumberUtils.toLong("invalid"));
        assertEquals(1234567890123L, NumberUtils.toLong("1234567890123"));
        assertEquals(99L, NumberUtils.toLong(null, 99L));
        assertEquals(99L, NumberUtils.toLong("invalid", 99L));
        assertEquals(-123L, NumberUtils.toLong("-123", 99L));
    }

    @Test(timeout = 4000)
    public void testToFloat() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat("invalid"), 0.0001f);
        assertEquals(12.34f, NumberUtils.toFloat("12.34"), 0.0001f);
        assertEquals(4.5f, NumberUtils.toFloat(null, 4.5f), 0.0001f);
        assertEquals(4.5f, NumberUtils.toFloat("invalid", 4.5f), 0.0001f);
        assertEquals(-2.5f, NumberUtils.toFloat("-2.5", 4.5f), 0.0001f);
    }

    @Test(timeout = 4000)
    public void testToDouble() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.00001d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.00001d);
        assertEquals(0.0d, NumberUtils.toDouble("invalid"), 0.00001d);
        assertEquals(123.456d, NumberUtils.toDouble("123.456"), 0.00001d);
        assertEquals(7.89d, NumberUtils.toDouble(null, 7.89d), 0.00001d);
        assertEquals(7.89d, NumberUtils.toDouble("invalid", 7.89d), 0.00001d);
        assertEquals(-99.1d, NumberUtils.toDouble("-99.1", 7.89d), 0.00001d);
    }

    @Test(timeout = 4000)
    public void testToByte() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 0, NumberUtils.toByte("invalid"));
        assertEquals((byte) 12, NumberUtils.toByte("12"));
        assertEquals((byte) 5, NumberUtils.toByte(null, (byte) 5));
        assertEquals((byte) 5, NumberUtils.toByte("invalid", (byte) 5));
        assertEquals((byte) -8, NumberUtils.toByte("-8", (byte) 5));
    }

    @Test(timeout = 4000)
    public void testToShort() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 0, NumberUtils.toShort("invalid"));
        assertEquals((short) 1234, NumberUtils.toShort("1234"));
        assertEquals((short) 15, NumberUtils.toShort(null, (short) 15));
        assertEquals((short) 15, NumberUtils.toShort("invalid", (short) 15));
        assertEquals((short) -300, NumberUtils.toShort("-300", (short) 15));
    }

    // =========================================================================
    // Partition A & B: Explicit Factory Methods
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(12.34f), NumberUtils.createFloat("12.34"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateFloatInvalid() {
        NumberUtils.createFloat("abc");
    }

    @Test(timeout = 4000)
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(1234.5678d), NumberUtils.createDouble("1234.5678"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateDoubleInvalid() {
        NumberUtils.createDouble("not-a-double");
    }

    @Test(timeout = 4000)
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        assertEquals(Integer.valueOf(16), NumberUtils.createInteger("0x10"));
        assertEquals(Integer.valueOf(-16), NumberUtils.createInteger("-0x10"));
        assertEquals(Integer.valueOf(8), NumberUtils.createInteger("010")); // Octal
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateIntegerInvalid() {
        NumberUtils.createInteger("bad");
    }

    @Test(timeout = 4000)
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createLong("1234567890123"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateLongInvalid() {
        NumberUtils.createLong("badLong");
    }

    @Test(timeout = 4000)
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("98765432109876543210"), NumberUtils.createBigInteger("98765432109876543210"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigIntegerInvalid() {
        NumberUtils.createBigInteger("invalidBigInt");
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("12345678901234567890.123456789"),
                NumberUtils.createBigDecimal("12345678901234567890.123456789"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("   ");
    }

    // =========================================================================
    // Partition A & B: createNumber Branch Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumberBasic() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--123")); // Specially guarded for OS X Java issue
        assertEquals(Integer.valueOf(16), NumberUtils.createNumber("0x10"));
        assertEquals(Integer.valueOf(-16), NumberUtils.createNumber("-0x10"));
        assertEquals(Integer.valueOf(12345), NumberUtils.createNumber("12345"));
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createNumber("1234567890123"));
        assertEquals(new BigInteger("123456789012345678901234567890"),
                NumberUtils.createNumber("123456789012345678901234567890"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithQualifiers() {
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(new BigInteger("123456789012345678901234567890"),
                NumberUtils.createNumber("123456789012345678901234567890L"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23f"));
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23F"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23d"));
        assertEquals(Double.valueOf(1.23d), NumberUtils.createNumber("1.23D"));

        // Qualifier fallback when Float/Double overflows or underflows precision
        // Smallest Float precision underflow falls to Double/BigDecimal
        Number numFUnderflow = NumberUtils.createNumber("1e-46f");
        assertNotNull(numFUnderflow);

        // Extreme exponents
        assertEquals(Double.valueOf(1.0e100), NumberUtils.createNumber("1.0e100d"));
        assertEquals(new BigDecimal("1.0e500"), NumberUtils.createNumber("1.0e500d"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberFloatingPointAutoType() {
        assertEquals(Float.valueOf(1.234f), NumberUtils.createNumber("1.234"));
        assertEquals(Double.valueOf(1.23456789012345d), NumberUtils.createNumber("1.23456789012345"));
        assertEquals(new BigDecimal("1.234567890123456789012345678901234567890"),
                NumberUtils.createNumber("1.234567890123456789012345678901234567890"));

        // Scientific notation auto types
        assertEquals(Float.valueOf(1.0e10f), NumberUtils.createNumber("1.0e10"));
        assertEquals(Double.valueOf(1.0e50d), NumberUtils.createNumber("1.0e50"));
        assertEquals(new BigDecimal("1.0e500"), NumberUtils.createNumber("1.0e500"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("  ");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidExponentDecimalPlacement() {
        NumberUtils.createNumber("1e2.3");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberLongWithDecimal() {
        NumberUtils.createNumber("12.34L");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberLongWithExponent() {
        NumberUtils.createNumber("12e3L");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidQualifier() {
        NumberUtils.createNumber("123z");
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (Defects4J LANG-521)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectLang521IsNumberTrailingDecimal() {
        // Under defective code, trailing '.' returned false due to unexpected end-of-string state
        assertTrue("isNumber(\"2.\") should be valid float/double literal", NumberUtils.isNumber("2."));
        assertTrue("isNumber(\".2\") should be valid float/double literal", NumberUtils.isNumber(".2"));
        assertTrue("isNumber(\"-2.\") should be valid negative float/double literal", NumberUtils.isNumber("-2."));
    }

    @Test(timeout = 4000)
    public void testDefectLang521CreateNumberTrailingDecimal() {
        // Under defective code, createNumber("2.") throws NumberFormatException
        Number result = NumberUtils.createNumber("2.");
        assertNotNull("createNumber(\"2.\") should not be null", result);
        assertEquals("createNumber(\"2.\") should equal 2.0f", Float.valueOf(2.0f), result);
    }

    // =========================================================================
    // Partition A & B: isDigits and isNumber Verification
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("  "));
        assertFalse(NumberUtils.isDigits("12a34"));
        assertFalse(NumberUtils.isDigits("-123")); // Sign is not a digit
        assertFalse(NumberUtils.isDigits("12.34")); // Dot is not a digit
        assertTrue(NumberUtils.isDigits("0"));
        assertTrue(NumberUtils.isDigits("1234567890"));
    }

    @Test(timeout = 4000)
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber(" "));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertFalse(NumberUtils.isNumber("0xGHI"));
        assertTrue(NumberUtils.isNumber("0x123ABC"));
        assertTrue(NumberUtils.isNumber("-0xABC123"));

        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("0"));
        assertTrue(NumberUtils.isNumber("12.34"));
        assertTrue(NumberUtils.isNumber("-12.34"));
        assertTrue(NumberUtils.isNumber(".5"));
        assertTrue(NumberUtils.isNumber("-.5"));

        // Exponents
        assertTrue(NumberUtils.isNumber("1e5"));
        assertTrue(NumberUtils.isNumber("1.5e-5"));
        assertTrue(NumberUtils.isNumber("1.5E+5"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e-"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1.2e3.4"));

        // Type qualifiers
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertTrue(NumberUtils.isNumber("1.2f"));
        assertTrue(NumberUtils.isNumber("1.2F"));
        assertTrue(NumberUtils.isNumber("1.2d"));
        assertTrue(NumberUtils.isNumber("1.2D"));
        assertFalse(NumberUtils.isNumber("123L2"));
        assertFalse(NumberUtils.isNumber("1.2e3L")); // Long cannot have exponent
        assertFalse(NumberUtils.isNumber("123abc"));
    }

    // =========================================================================
    // Partition A & B: min and max Overloads (3 parameters)
    // =========================================================================

    @Test(timeout = 4000)
    public void testMin3ParamLong() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(2L, 1L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));
        assertEquals(-5L, NumberUtils.min(0L, -5L, 5L));
    }

    @Test(timeout = 4000)
    public void testMax3ParamLong() {
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(2L, 3L, 1L));
        assertEquals(3L, NumberUtils.max(3L, 2L, 1L));
        assertEquals(5L, NumberUtils.max(0L, -5L, 5L));
    }

    @Test(timeout = 4000)
    public void testMin3ParamInt() {
        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(2, 1, 3));
        assertEquals(1, NumberUtils.min(3, 2, 1));
        assertEquals(-10, NumberUtils.min(-10, 0, 10));
    }

    @Test(timeout = 4000)
    public void testMax3ParamInt() {
        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(3, NumberUtils.max(2, 3, 1));
        assertEquals(3, NumberUtils.max(3, 2, 1));
        assertEquals(10, NumberUtils.max(-10, 0, 10));
    }

    @Test(timeout = 4000)
    public void testMin3ParamShort() {
        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 2, (short) 1, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 2, (short) 1));
    }

    @Test(timeout = 4000)
    public void testMax3ParamShort() {
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 2, (short) 3, (short) 1));
        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 2, (short) 1));
    }

    @Test(timeout = 4000)
    public void testMin3ParamByte() {
        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 2, (byte) 1, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 2, (byte) 1));
    }

    @Test(timeout = 4000)
    public void testMax3ParamByte() {
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 2, (byte) 3, (byte) 1));
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 2, (byte) 1));
    }

    @Test(timeout = 4000)
    public void testMin3ParamDouble() {
        assertEquals(1.1d, NumberUtils.min(1.1d, 2.2d, 3.3d), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(2.2d, 1.1d, 3.3d), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(3.3d, 2.2d, 1.1d), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, 2.2d, 3.3d)));
        assertTrue(Double.isNaN(NumberUtils.min(1.1d, Double.NaN, 3.3d)));
        assertTrue(Double.isNaN(NumberUtils.min(1.1d, 2.2d, Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testMax3ParamDouble() {
        assertEquals(3.3d, NumberUtils.max(1.1d, 2.2d, 3.3d), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(2.2d, 3.3d, 1.1d), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(3.3d, 2.2d, 1.1d), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.max(Double.NaN, 2.2d, 3.3d)));
        assertTrue(Double.isNaN(NumberUtils.max(1.1d, Double.NaN, 3.3d)));
        assertTrue(Double.isNaN(NumberUtils.max(1.1d, 2.2d, Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testMin3ParamFloat() {
        assertEquals(1.1f, NumberUtils.min(1.1f, 2.2f, 3.3f), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(2.2f, 1.1f, 3.3f), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(3.3f, 2.2f, 1.1f), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, 2.2f, 3.3f)));
        assertTrue(Float.isNaN(NumberUtils.min(1.1f, Float.NaN, 3.3f)));
        assertTrue(Float.isNaN(NumberUtils.min(1.1f, 2.2f, Float.NaN)));
    }

    @Test(timeout = 4000)
    public void testMax3ParamFloat() {
        assertEquals(3.3f, NumberUtils.max(1.1f, 2.2f, 3.3f), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(2.2f, 3.3f, 1.1f), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(3.3f, 2.2f, 1.1f), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.max(Float.NaN, 2.2f, 3.3f)));
        assertTrue(Float.isNaN(NumberUtils.max(1.1f, Float.NaN, 3.3f)));
        assertTrue(Float.isNaN(NumberUtils.max(1.1f, 2.2f, Float.NaN)));
    }

    // =========================================================================
    // Partition A & B: min and max Overloads (Array versions)
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinMaxLongArray() {
        long[] arr = new long[]{5L, -2L, 10L, -7L, 3L};
        assertEquals(-7L, NumberUtils.min(arr));
        assertEquals(10L, NumberUtils.max(arr));

        long[] single = new long[]{42L};
        assertEquals(42L, NumberUtils.min(single));
        assertEquals(42L, NumberUtils.max(single));
    }

    @Test(timeout = 4000)
    public void testMinMaxIntArray() {
        int[] arr = new int[]{5, -2, 10, -7, 3};
        assertEquals(-7, NumberUtils.min(arr));
        assertEquals(10, NumberUtils.max(arr));

        int[] single = new int[]{42};
        assertEquals(42, NumberUtils.min(single));
        assertEquals(42, NumberUtils.max(single));
    }

    @Test(timeout = 4000)
    public void testMinMaxShortArray() {
        short[] arr = new short[]{5, -2, 10, -7, 3};
        assertEquals((short) -7, NumberUtils.min(arr));
        assertEquals((short) 10, NumberUtils.max(arr));

        short[] single = new short[]{42};
        assertEquals((short) 42, NumberUtils.min(single));
        assertEquals((short) 42, NumberUtils.max(single));
    }

    @Test(timeout = 4000)
    public void testMinMaxByteArray() {
        byte[] arr = new byte[]{5, -2, 10, -7, 3};
        assertEquals((byte) -7, NumberUtils.min(arr));
        assertEquals((byte) 10, NumberUtils.max(arr));

        byte[] single = new byte[]{42};
        assertEquals((byte) 42, NumberUtils.min(single));
        assertEquals((byte) 42, NumberUtils.max(single));
    }

    @Test(timeout = 4000)
    public void testMinMaxDoubleArray() {
        double[] arr = new double[]{5.5d, -2.2d, 10.1d, -7.7d, 3.3d};
        assertEquals(-7.7d, NumberUtils.min(arr), 0.0001d);
        assertEquals(10.1d, NumberUtils.max(arr), 0.0001d);

        double[] nanArr = new double[]{1.0d, Double.NaN, 2.0d};
        assertTrue(Double.isNaN(NumberUtils.min(nanArr)));
        assertTrue(Double.isNaN(NumberUtils.max(nanArr)));

        double[] single = new double[]{4.2d};
        assertEquals(4.2d, NumberUtils.min(single), 0.0001d);