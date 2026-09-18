package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targets & Branch Zones:
 * 1. Partition A: Core Functional Logic & State Transitions
 *    - Conversions with defaults: toInt, toLong, toFloat, toDouble, toByte, toShort
 *    - Standard number creation: createFloat, createDouble, createInteger, createLong, createBigInteger, createBigDecimal
 *    - 3-parameter min/max scalar methods across all primitive types
 *    - Array min/max across all primitive types
 *    - Digit and general number checking: isDigits, isNumber
 *
 * 2. Partition B: Boundary Value Analysis (BVA) & Extremes
 *    - Null and blank strings across all parsing methods
 *    - Hexadecimal representations with prefixes ("0x", "0X", "-0x", "-0X", "#", "-#")
 *    - Octal representations with leading zeros
 *    - Positive and negative limits: Integer.MIN/MAX, Long.MIN/MAX, Double/Float extremes
 *    - Double.NaN and Float.NaN behavior in array/scalar min/max routines
 *
 * 3. Partition C: Defect-Targeted Branch Zone (LANG-693 / Defects4J)
 *    - Precision preservation in createNumber: "testStringCreateNumberEnsureNoPrecisionLoss"
 *    - Values near boundary of Float/Double representations without explicit type qualifiers
 *      must not truncate precision to Float when Double or BigDecimal is required.
 *
 * 4. Partition D: Exception & Defensive Guard Paths
 *    - Empty or null array validations in min/max methods (IllegalArgumentException)
 *    - Malformed numbers in createNumber and createBigDecimal ("--1", multiple decimals, exponents)
 *    - Unparseable suffixes and malformed hex/octal strings
 *
 * 5. Partition E: Object Lifecycle & Constant Integrity
 *    - Instantiation of public NumberUtils constructor
 *    - Verification of cached constants (LONG_ZERO, INTEGER_ONE, etc.)
 */
public class NumberUtilsGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Directly targets LANG-693 / Defects4J defect:
     * When createNumber parses floating point strings without type specifiers,
     * it must not choose Float/Double if precision is lost.
     */
    @Test(timeout = 4000)
    public void testStringCreateNumberEnsureNoPrecisionLoss() {
        final String shouldBeFloat = "1.23";
        final String shouldBeDouble = "3.40282354e+38";
        final String shouldBeBigDecimal = "1.797693134862315759e+308";

        final Number numFloat = NumberUtils.createNumber(shouldBeFloat);
        assertTrue("Expected Float for " + shouldBeFloat + " but was " + (numFloat == null ? "null" : numFloat.getClass().getName()),
                numFloat instanceof Float);

        final Number numDouble = NumberUtils.createNumber(shouldBeDouble);
        assertTrue("Expected Double for " + shouldBeDouble + " but was " + (numDouble == null ? "null" : numDouble.getClass().getName()),
                numDouble instanceof Double);

        final Number numBigDecimal = NumberUtils.createNumber(shouldBeBigDecimal);
        assertTrue("Expected BigDecimal for " + shouldBeBigDecimal + " but was " + (numBigDecimal == null ? "null" : numBigDecimal.getClass().getName()),
                numBigDecimal instanceof BigDecimal);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testToInt() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(5, NumberUtils.toInt("abc", 5));
        assertEquals(42, NumberUtils.toInt("42", 5));
    }

    @Test(timeout = 4000)
    public void testToLong() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(12345678901L, NumberUtils.toLong("12345678901"));
        assertEquals(99L, NumberUtils.toLong("invalid", 99L));
        assertEquals(100L, NumberUtils.toLong("100", 99L));
    }

    @Test(timeout = 4000)
    public void testToFloat() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0f);
        assertEquals(1.25f, NumberUtils.toFloat("1.25"), 0.0f);
        assertEquals(3.14f, NumberUtils.toFloat("invalid", 3.14f), 0.0f);
        assertEquals(2.71f, NumberUtils.toFloat("2.71", 3.14f), 0.0f);
    }

    @Test(timeout = 4000)
    public void testToDouble() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0d);
        assertEquals(1.2345d, NumberUtils.toDouble("1.2345"), 0.0d);
        assertEquals(9.87d, NumberUtils.toDouble("invalid", 9.87d), 0.0d);
        assertEquals(5.55d, NumberUtils.toDouble("5.55", 9.87d), 0.0d);
    }

    @Test(timeout = 4000)
    public void testToByte() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 12, NumberUtils.toByte("12"));
        assertEquals((byte) 7, NumberUtils.toByte("invalid", (byte) 7));
        assertEquals((byte) 8, NumberUtils.toByte("8", (byte) 7));
    }

    @Test(timeout = 4000)
    public void testToShort() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 1234, NumberUtils.toShort("1234"));
        assertEquals((short) 50, NumberUtils.toShort("invalid", (short) 50));
        assertEquals((short) 100, NumberUtils.toShort("100", (short) 50));
    }

    @Test(timeout = 4000)
    public void testCreateFloatDoubleIntegerLong() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));

        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(1.5d), NumberUtils.createDouble("1.5"));

        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        assertEquals(Integer.valueOf(0x1a), NumberUtils.createInteger("0x1a"));
        assertEquals(Integer.valueOf(012), NumberUtils.createInteger("012"));

        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(12345678901L), NumberUtils.createLong("12345678901"));
        assertEquals(Long.valueOf(0x1aL), NumberUtils.createLong("0x1a"));
        assertEquals(Long.valueOf(012L), NumberUtils.createLong("012"));
    }

    @Test(timeout = 4000)
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
        assertEquals(new BigInteger("-12345678901234567890"), NumberUtils.createBigInteger("-12345678901234567890"));

        // Hex formats: 0x, #, with and without negation
        assertEquals(new BigInteger("ff", 16), NumberUtils.createBigInteger("0xff"));
        assertEquals(new BigInteger("-ff", 16), NumberUtils.createBigInteger("-0xff"));
        assertEquals(new BigInteger("7f", 16), NumberUtils.createBigInteger("#7f"));
        assertEquals(new BigInteger("-7f", 16), NumberUtils.createBigInteger("-#7f"));

        // Octal format: leading 0 with additional digits
        assertEquals(new BigInteger("77", 8), NumberUtils.createBigInteger("077"));
        assertEquals(new BigInteger("-77", 8), NumberUtils.createBigInteger("-077"));
        assertEquals(BigInteger.ZERO, NumberUtils.createBigInteger("0"));
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("12345.67890"), NumberUtils.createBigDecimal("12345.67890"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberHexPrefixes() {
        // Hex: length - prefix <= 8 -> Integer
        assertEquals(Integer.valueOf(0x1234), NumberUtils.createNumber("0x1234"));
        assertEquals(Integer.valueOf(0x1234), NumberUtils.createNumber("0X1234"));
        assertEquals(Integer.valueOf(-0x1234), NumberUtils.createNumber("-0x1234"));
        assertEquals(Integer.valueOf(-0x1234), NumberUtils.createNumber("-0X1234"));
        assertEquals(Integer.valueOf(0x1234), NumberUtils.createNumber("#1234"));
        assertEquals(Integer.valueOf(-0x1234), NumberUtils.createNumber("-#1234"));

        // Hex: length - prefix > 8 and <= 16 -> Long
        assertEquals(Long.valueOf(0x123456789L), NumberUtils.createNumber("0x123456789"));

        // Hex: length - prefix > 16 -> BigInteger
        assertEquals(new BigInteger("12345678901234567", 16), NumberUtils.createNumber("0x12345678901234567"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberTypeQualifiers() {
        // Suffix 'l' and 'L'
        assertEquals(Long.valueOf(1234L), NumberUtils.createNumber("1234L"));
        assertEquals(Long.valueOf(-1234L), NumberUtils.createNumber("-1234l"));
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808L"));

        // Suffix 'f' and 'F'
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5f"));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5F"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));

        // Suffix 'd' and 'D'
        assertEquals(Double.valueOf(1.5d), NumberUtils.createNumber("1.5d"));
        assertEquals(Double.valueOf(1.5d), NumberUtils.createNumber("1.5D"));
        assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0.0d"));

        // Exponent qualifier cases
        assertEquals(Float.valueOf(1.2e3f), NumberUtils.createNumber("1.2e3f"));
        assertEquals(Double.valueOf(1.2e300d), NumberUtils.createNumber("1.2e300d"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithoutTypeQualifiers() {
        assertNull(NumberUtils.createNumber(null));

        // Integral values: Integer, Long, BigInteger
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Integer.valueOf(-123), NumberUtils.createNumber("-123"));
        assertEquals(Long.valueOf(2147483648L), NumberUtils.createNumber("2147483648"));
        assertEquals(new BigInteger("9223372036854775808"), NumberUtils.createNumber("9223372036854775808"));

        // All zeros
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.00e0"));

        // Exponential form without decimals
        assertEquals(Float.valueOf(1e2f), NumberUtils.createNumber("1e2"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinMaxScalars() {
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
        assertEquals(1.1d, NumberUtils.min(1.1d, 2.2d, 3.3d), 0.0d);
        assertEquals(1.1d, NumberUtils.min(2.2d, 1.1d, 3.3d), 0.0d);
        assertEquals(1.1d, NumberUtils.min(3.3d, 2.2d, 1.1d), 0.0d);
        assertEquals(3.3d, NumberUtils.max(1.1d, 2.2d, 3.3d), 0.0d);
        assertEquals(3.3d, NumberUtils.max(1.1d, 3.3d, 2.2d), 0.0d);
        assertEquals(3.3d, NumberUtils.max(3.3d, 1.1d, 2.2d), 0.0d);

        // float
        assertEquals(1.1f, NumberUtils.min(1.1f, 2.2f, 3.3f), 0.0f);
        assertEquals(1.1f, NumberUtils.min(2.2f, 1.1f, 3.3f), 0.0f);
        assertEquals(1.1f, NumberUtils.min(3.3f, 2.2f, 1.1f), 0.0f);
        assertEquals(3.3f, NumberUtils.max(1.1f, 2.2f, 3.3f), 0.0f);
        assertEquals(3.3f, NumberUtils.max(1.1f, 3.3f, 2.2f), 0.0f);
        assertEquals(3.3f, NumberUtils.max(3.3f, 1.1f, 2.2f), 0.0f);
    }

    @Test(timeout = 4000)
    public void testMinMaxArrays() {
        // long[]
        assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        assertEquals(3L, NumberUtils.max(new long[]{1L, 3L, 2L}));

        // int[]
        assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
        assertEquals(3, NumberUtils.max(new int[]{1, 3, 2}));

        // short[]
        assertEquals((short) 1, NumberUtils.min(new short[]{(short) 3, (short) 1, (short) 2}));
        assertEquals((short) 3, NumberUtils.max(new short[]{(short) 1, (short) 3, (short) 2}));

        // byte[]
        assertEquals((byte) 1, NumberUtils.min(new byte[]{(byte) 3, (byte) 1, (byte) 2}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{(byte) 1, (byte) 3, (byte) 2}));

        // double[]
        assertEquals(1.0d, NumberUtils.min(new double[]{3.0d, 1.0d, 2.0d}), 0.0d);
        assertEquals(3.0d, NumberUtils.max(new double[]{1.0d, 3.0d, 2.0d}), 0.0d);

        // float[]
        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, 1.0f, 2.0f}), 0.0f);
        assertEquals(3.0f, NumberUtils.max(new float[]{1.0f, 3.0f, 2.0f}), 0.0f);

        // NaN handling in arrays
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.0d, Double.NaN, 2.0d})));
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0d, Double.NaN, 2.0d})));
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.0f, Float.NaN, 2.0f})));
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN, 2.0f})));
    }

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("0"));
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("-123"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertFalse(NumberUtils.isDigits("12a3"));
    }

    @Test(timeout = 4000)
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("   "));

        // Valid integers
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("+123"));

        // Valid hex
        assertTrue(NumberUtils.isNumber("0x123"));
        assertTrue(NumberUtils.isNumber("-0x123"));
        assertTrue(NumberUtils.isNumber("0XABCDEF"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertFalse(NumberUtils.isNumber("0xGHI"));

        // Valid decimals
        assertTrue(NumberUtils.isNumber("123.456"));
        assertTrue(NumberUtils.isNumber(".456"));
        assertTrue(NumberUtils.isNumber("123."));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("1.2.3"));

        // Valid exponents
        assertTrue(NumberUtils.isNumber("1e3"));
        assertTrue(NumberUtils.isNumber("1.2e-3"));
        assertTrue(NumberUtils.isNumber("1.2E+3"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("1ee2"));
        assertFalse(NumberUtils.isNumber("e2"));
        assertFalse(NumberUtils.isNumber("1.2e3.4"));

        // Valid type qualifiers
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertTrue(NumberUtils.isNumber("1.2f"));
        assertTrue(NumberUtils.isNumber("1.2F"));
        assertTrue(NumberUtils.isNumber("1.2d"));
        assertTrue(NumberUtils.isNumber("1.2D"));
        assertFalse(NumberUtils.isNumber("1.2L"));
        assertFalse(NumberUtils.isNumber("1e2L"));
        assertFalse(NumberUtils.isNumber("123a"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinLongNullArray() {
        NumberUtils.min((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinLongEmptyArray() {
        NumberUtils.min(new long[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinIntNullArray() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinIntEmptyArray() {
        NumberUtils.min(new int[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinShortNullArray() {
        NumberUtils.min((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinShortEmptyArray() {
        NumberUtils.min(new short[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinByteNullArray() {
        NumberUtils.min((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinByteEmptyArray() {
        NumberUtils.min(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinDoubleNullArray() {
        NumberUtils.min((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinDoubleEmptyArray() {
        NumberUtils.min(new double[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinFloatNullArray() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinFloatEmptyArray() {
        NumberUtils.min(new float[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxLongNullArray() {
        NumberUtils.max((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxLongEmptyArray() {
        NumberUtils.max(new long[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxIntNullArray() {
        NumberUtils.max((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxIntEmptyArray() {
        NumberUtils.max(new int[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxShortNullArray() {
        NumberUtils.max((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxShortEmptyArray() {
        NumberUtils.max(new short[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxByteNullArray() {
        NumberUtils.max((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxByteEmptyArray() {
        NumberUtils.max(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxDoubleNullArray() {
        NumberUtils.max((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxDoubleEmptyArray() {
        NumberUtils.max(new double[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxFloatNullArray() {
        NumberUtils.max((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxFloatEmptyArray() {
        NumberUtils.max(new float[0]);
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("   ");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("   ");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimalDoubleDashProtection() {
        NumberUtils.createBigDecimal("--123.45");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberDoubleExponent() {
        NumberUtils.createNumber("1e2e3");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberExponentBeforeDecimal() {
        NumberUtils.createNumber("1e2.3");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidLongQualifier() {
        NumberUtils.createNumber("1.2L");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberUnknownQualifier() {
        NumberUtils.createNumber("123q");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
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

        assertEquals(Double.valueOf(0.0d), NumberUtils.DOUBLE_ZERO);
        assertEquals(Double.valueOf(1.0d), NumberUtils.DOUBLE_ONE);
        assertEquals(Double.valueOf(-1.0d), NumberUtils.DOUBLE_MINUS_ONE);

        assertEquals(Float.valueOf(0.0f), NumberUtils.FLOAT_ZERO);
        assertEquals(Float.valueOf(1.0f), NumberUtils.FLOAT_ONE);
        assertEquals(Float.valueOf(-1.0f), NumberUtils.FLOAT_MINUS_ONE);
    }
}