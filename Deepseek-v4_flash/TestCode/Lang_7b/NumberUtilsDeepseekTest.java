package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: NumberUtils (all public methods)
 * 
 * Partition A: Core functional logic & state transitions
 *   - toInt/toLong/toFloat/toDouble/toByte/toShort with valid/invalid strings
 *   - createNumber with valid numeric strings (integer, long, float, double, BigDecimal, BigInteger)
 *   - createFloat/createDouble/createInteger/createLong/createBigInteger/createBigDecimal
 *   - min/max for arrays and three-value variants
 *   - isDigits, isNumber
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - null strings, empty strings, blank strings
 *   - Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE
 *   - Hex strings with 8/9 digits (boundary between Integer and Long)
 *   - Strings with leading zeros, trailing type qualifiers
 *   - Array min/max with single element, negative values, NaN (for double/float)
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Known defect: createNumber("--1") should throw NumberFormatException but returns null
 *   - Additional edge cases that may expose similar defects:
 *     * "0x" (no hex digits) – should throw NumberFormatException
 *     * "1e" (exponent without digits) – should throw NumberFormatException
 *     * "1L" with decimal point – should throw NumberFormatException
 *     * "1.0L" – should throw NumberFormatException
 *     * "1.0e" – should throw NumberFormatException
 *     * "1.0e-" – should throw NumberFormatException
 *     * "+1" (leading plus) – should throw NumberFormatException (not handled)
 *     * "1.0f" with all zeros mantissa/exp – valid
 *     * "0.0f" – valid
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Null/empty arrays for min/max -> IllegalArgumentException
 *   - Invalid number strings for createNumber -> NumberFormatException
 *   - Blank string for createBigDecimal -> NumberFormatException
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Constants (LONG_ZERO, etc.) are not null and have correct values
 *   - Constructor is public (trivial)
 */
public class NumberUtilsDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testToIntValid() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(1, NumberUtils.toInt("1"));
        assertEquals(-5, NumberUtils.toInt("-5"));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(10, NumberUtils.toInt("10", 0));
        assertEquals(0, NumberUtils.toInt(null, 0));
        assertEquals(5, NumberUtils.toInt("invalid", 5));
    }

    @Test(timeout = 4000)
    public void testToLongValid() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(1L, NumberUtils.toLong("1"));
        assertEquals(-5L, NumberUtils.toLong("-5"));
        assertEquals(0L, NumberUtils.toLong("abc"));
        assertEquals(10L, NumberUtils.toLong("10", 0L));
        assertEquals(0L, NumberUtils.toLong(null, 0L));
        assertEquals(5L, NumberUtils.toLong("invalid", 5L));
    }

    @Test(timeout = 4000)
    public void testToFloatValid() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0f);
        assertEquals(-2.0f, NumberUtils.toFloat("-2"), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0f);
        assertEquals(1.1f, NumberUtils.toFloat(null, 1.1f), 0.0f);
        assertEquals(1.1f, NumberUtils.toFloat("", 1.1f), 0.0f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5", 0.0f), 0.0f);
    }

    @Test(timeout = 4000)
    public void testToDoubleValid() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0d);
        assertEquals(-2.0d, NumberUtils.toDouble("-2"), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0d);
        assertEquals(1.1d, NumberUtils.toDouble(null, 1.1d), 0.0d);
        assertEquals(1.1d, NumberUtils.toDouble("", 1.1d), 0.0d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5", 0.0d), 0.0d);
    }

    @Test(timeout = 4000)
    public void testToByteValid() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 1, NumberUtils.toByte("1"));
        assertEquals((byte) -1, NumberUtils.toByte("-1"));
        assertEquals((byte) 0, NumberUtils.toByte("abc"));
        assertEquals((byte) 10, NumberUtils.toByte("10", (byte) 0));
        assertEquals((byte) 5, NumberUtils.toByte("invalid", (byte) 5));
    }

    @Test(timeout = 4000)
    public void testToShortValid() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 1, NumberUtils.toShort("1"));
        assertEquals((short) -1, NumberUtils.toShort("-1"));
        assertEquals((short) 0, NumberUtils.toShort("abc"));
        assertEquals((short) 10, NumberUtils.toShort("10", (short) 0));
        assertEquals((short) 5, NumberUtils.toShort("invalid", (short) 5));
    }

    @Test(timeout = 4000)
    public void testCreateNumberValidInteger() {
        Number n = NumberUtils.createNumber("123");
        assertTrue(n instanceof Integer);
        assertEquals(123, n.intValue());

        n = NumberUtils.createNumber("-456");
        assertTrue(n instanceof Integer);
        assertEquals(-456, n.intValue());

        n = NumberUtils.createNumber("0");
        assertTrue(n instanceof Integer);
        assertEquals(0, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberValidLong() {
        Number n = NumberUtils.createNumber("1234567890123");
        assertTrue(n instanceof Long);
        assertEquals(1234567890123L, n.longValue());

        n = NumberUtils.createNumber("-1234567890123");
        assertTrue(n instanceof Long);
        assertEquals(-1234567890123L, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberValidBigInteger() {
        Number n = NumberUtils.createNumber("123456789012345678901234567890");
        assertTrue(n instanceof BigInteger);
        assertEquals(new BigInteger("123456789012345678901234567890"), n);
    }

    @Test(timeout = 4000)
    public void testCreateNumberValidFloat() {
        Number n = NumberUtils.createNumber("1.5f");
        assertTrue(n instanceof Float);
        assertEquals(1.5f, n.floatValue(), 0.0f);

        n = NumberUtils.createNumber("-2.0F");
        assertTrue(n instanceof Float);
        assertEquals(-2.0f, n.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberValidDouble() {
        Number n = NumberUtils.createNumber("1.5d");
        assertTrue(n instanceof Double);
        assertEquals(1.5d, n.doubleValue(), 0.0d);

        n = NumberUtils.createNumber("-2.0D");
        assertTrue(n instanceof Double);
        assertEquals(-2.0d, n.doubleValue(), 0.0d);
    }

    @Test(timeout = 4000)
    public void testCreateNumberValidBigDecimal() {
        Number n = NumberUtils.createNumber("1.5");
        assertTrue(n instanceof BigDecimal);
        assertEquals(new BigDecimal("1.5"), n);

        n = NumberUtils.createNumber("-2.0");
        assertTrue(n instanceof BigDecimal);
        assertEquals(new BigDecimal("-2.0"), n);
    }

    @Test(timeout = 4000)
    public void testCreateNumberValidHex() {
        Number n = NumberUtils.createNumber("0x1A");
        assertTrue(n instanceof Integer);
        assertEquals(26, n.intValue());

        n = NumberUtils.createNumber("-0x1A");
        assertTrue(n instanceof Integer);
        assertEquals(-26, n.intValue());

        n = NumberUtils.createNumber("0XFF");
        assertTrue(n instanceof Integer);
        assertEquals(255, n.intValue());

        // 9 hex digits -> Long
        n = NumberUtils.createNumber("0x123456789");
        assertTrue(n instanceof Long);
        assertEquals(0x123456789L, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberValidWithExponent() {
        Number n = NumberUtils.createNumber("1.5e2");
        assertTrue(n instanceof BigDecimal);
        assertEquals(new BigDecimal("150"), n);

        n = NumberUtils.createNumber("1.5E2");
        assertTrue(n instanceof BigDecimal);
        assertEquals(new BigDecimal("150"), n);

        n = NumberUtils.createNumber("1e10");
        assertTrue(n instanceof BigDecimal);
        assertEquals(new BigDecimal("10000000000"), n);
    }

    @Test(timeout = 4000)
    public void testCreateNumberValidWithTypeQualifierAndExponent() {
        Number n = NumberUtils.createNumber("1.5e2f");
        assertTrue(n instanceof Float);
        assertEquals(150.0f, n.floatValue(), 0.0f);

        n = NumberUtils.createNumber("1.5e2d");
        assertTrue(n instanceof Double);
        assertEquals(150.0d, n.doubleValue(), 0.0d);
    }

    @Test(timeout = 4000)
    public void testCreateNumberNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(timeout = 4000)
    public void testCreateFloatNull() {
        assertNull(NumberUtils.createFloat(null));
    }

    @Test(timeout = 4000)
    public void testCreateDoubleNull() {
        assertNull(NumberUtils.createDouble(null));
    }

    @Test(timeout = 4000)
    public void testCreateIntegerNull() {
        assertNull(NumberUtils.createInteger(null));
    }

    @Test(timeout = 4000)
    public void testCreateLongNull() {
        assertNull(NumberUtils.createLong(null));
    }

    @Test(timeout = 4000)
    public void testCreateBigIntegerNull() {
        assertNull(NumberUtils.createBigInteger(null));
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimalNull() {
        assertNull(NumberUtils.createBigDecimal(null));
    }

    @Test(timeout = 4000)
    public void testCreateFloatValid() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));
        assertEquals(Float.valueOf(-2.0f), NumberUtils.createFloat("-2.0"));
    }

    @Test(timeout = 4000)
    public void testCreateDoubleValid() {
        assertEquals(Double.valueOf(1.5d), NumberUtils.createDouble("1.5"));
        assertEquals(Double.valueOf(-2.0d), NumberUtils.createDouble("-2.0"));
    }

    @Test(timeout = 4000)
    public void testCreateIntegerValid() {
        assertEquals(Integer.valueOf(123), NumberUtils.createInteger("123"));
        assertEquals(Integer.valueOf(-456), NumberUtils.createInteger("-456"));
        // hex and octal via decode
        assertEquals(Integer.valueOf(0x1A), NumberUtils.createInteger("0x1A"));
        assertEquals(Integer.valueOf(077), NumberUtils.createInteger("077"));
    }

    @Test(timeout = 4000)
    public void testCreateLongValid() {
        assertEquals(Long.valueOf(123L), NumberUtils.createLong("123"));
        assertEquals(Long.valueOf(-456L), NumberUtils.createLong("-456"));
        assertEquals(Long.valueOf(0x1AL), NumberUtils.createLong("0x1A"));
    }

    @Test(timeout = 4000)
    public void testCreateBigIntegerValid() {
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimalValid() {
        assertEquals(new BigDecimal("123.456"), NumberUtils.createBigDecimal("123.456"));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testToIntBoundary() {
        assertEquals(Integer.MAX_VALUE, NumberUtils.toInt(String.valueOf(Integer.MAX_VALUE)));
        assertEquals(Integer.MIN_VALUE, NumberUtils.toInt(String.valueOf(Integer.MIN_VALUE)));
        assertEquals(0, NumberUtils.toInt("2147483648")); // overflow -> default
        assertEquals(0, NumberUtils.toInt("-2147483649"));
    }

    @Test(timeout = 4000)
    public void testToLongBoundary() {
        assertEquals(Long.MAX_VALUE, NumberUtils.toLong(String.valueOf(Long.MAX_VALUE)));
        assertEquals(Long.MIN_VALUE, NumberUtils.toLong(String.valueOf(Long.MIN_VALUE)));
        assertEquals(0L, NumberUtils.toLong("9223372036854775808")); // overflow
    }

    @Test(timeout = 4000)
    public void testToFloatBoundary() {
        assertEquals(Float.MAX_VALUE, NumberUtils.toFloat(String.valueOf(Float.MAX_VALUE)), 0.0f);
        assertEquals(Float.MIN_VALUE, NumberUtils.toFloat(String.valueOf(Float.MIN_VALUE)), 0.0f);
        assertEquals(Float.NaN, NumberUtils.toFloat("NaN"), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, NumberUtils.toFloat("Infinity"), 0.0f);
        assertEquals(Float.NEGATIVE_INFINITY, NumberUtils.toFloat("-Infinity"), 0.0f);
    }

    @Test(timeout = 4000)
    public void testToDoubleBoundary() {
        assertEquals(Double.MAX_VALUE, NumberUtils.toDouble(String.valueOf(Double.MAX_VALUE)), 0.0d);
        assertEquals(Double.MIN_VALUE, NumberUtils.toDouble(String.valueOf(Double.MIN_VALUE)), 0.0d);
        assertEquals(Double.NaN, NumberUtils.toDouble("NaN"), 0.0d);
        assertEquals(Double.POSITIVE_INFINITY, NumberUtils.toDouble("Infinity"), 0.0d);
        assertEquals(Double.NEGATIVE_INFINITY, NumberUtils.toDouble("-Infinity"), 0.0d);
    }

    @Test(timeout = 4000)
    public void testCreateNumberHexBoundary() {
        // 8 hex digits -> Integer
        Number n = NumberUtils.createNumber("0x12345678");
        assertTrue(n instanceof Integer);
        assertEquals(0x12345678, n.intValue());

        // 9 hex digits -> Long
        n = NumberUtils.createNumber("0x123456789");
        assertTrue(n instanceof Long);
        assertEquals(0x123456789L, n.longValue());

        // 16 hex digits -> Long (max)
        n = NumberUtils.createNumber("0x7FFFFFFFFFFFFFFF");
        assertTrue(n instanceof Long);
        assertEquals(Long.MAX_VALUE, n.longValue());

        // 17 hex digits -> should throw? Actually createLong will throw, but code doesn't handle BigInteger for hex
        // This is a known limitation; we test that it throws NumberFormatException
        try {
            NumberUtils.createNumber("0x1FFFFFFFFFFFFFFFF");
            fail("Expected NumberFormatException for hex string too large for Long");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberAllZeros() {
        Number n = NumberUtils.createNumber("0.0");
        assertTrue(n instanceof Float);
        assertEquals(0.0f, n.floatValue(), 0.0f);

        n = NumberUtils.createNumber("0.0f");
        assertTrue(n instanceof Float);
        assertEquals(0.0f, n.floatValue(), 0.0f);

        n = NumberUtils.createNumber("0.0d");
        assertTrue(n instanceof Double);
        assertEquals(0.0d, n.doubleValue(), 0.0d);

        n = NumberUtils.createNumber("0.0e0");
        assertTrue(n instanceof BigDecimal);
        assertEquals(new BigDecimal("0.0"), n);
    }

    @Test(timeout = 4000)
    public void testMinMaxArrayBoundary() {
        // Single element arrays
        assertEquals(5L, NumberUtils.min(new long[]{5L}));
        assertEquals(5L, NumberUtils.max(new long[]{5L}));
        assertEquals(5, NumberUtils.min(new int[]{5}));
        assertEquals(5, NumberUtils.max(new int[]{5}));
        assertEquals((short)5, NumberUtils.min(new short[]{(short)5}));
        assertEquals((short)5, NumberUtils.max(new short[]{(short)5}));
        assertEquals((byte)5, NumberUtils.min(new byte[]{(byte)5}));
        assertEquals((byte)5, NumberUtils.max(new byte[]{(byte)5}));
        assertEquals(5.0d, NumberUtils.min(new double[]{5.0d}), 0.0d);
        assertEquals(5.0d, NumberUtils.max(new double[]{5.0d}), 0.0d);
        assertEquals(5.0f, NumberUtils.min(new float[]{5.0f}), 0.0f);
        assertEquals(5.0f, NumberUtils.max(new float[]{5.0f}), 0.0f);
    }

    @Test(timeout = 4000)
    public void testMinMaxArrayNaN() {
        // NaN propagation
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.0, Double.NaN, 2.0})));
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0, Double.NaN, 2.0})));
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.0f, Float.NaN, 2.0f})));
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN, 2.0f})));
    }

    @Test(timeout = 4000)
    public void testMinMaxThreeValues() {
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(3L, NumberUtils.max(3L, 1L, 2L));
        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(3, NumberUtils.max(3, 1, 2));
        assertEquals((short)1, NumberUtils.min((short)3, (short)1, (short)2));
        assertEquals((short)3, NumberUtils.max((short)3, (short)1, (short)2));
        assertEquals((byte)1, NumberUtils.min((byte)3, (byte)1, (byte)2));
        assertEquals((byte)3, NumberUtils.max((byte)3, (byte)1, (byte)2));
        assertEquals(1.0d, NumberUtils.min(3.0d, 1.0d, 2.0d), 0.0d);
        assertEquals(3.0d, NumberUtils.max(3.0d, 1.0d, 2.0d), 0.0d);
        assertEquals(1.0f, NumberUtils.min(3.0f, 1.0f, 2.0f), 0.0f);
        assertEquals(3.0f, NumberUtils.max(3.0f, 1.0f, 2.0f), 0.0f);
    }

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("abc"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertTrue(NumberUtils.isDigits("123"));
        assertTrue(NumberUtils.isDigits("0"));
        assertFalse(NumberUtils.isDigits("-123"));
    }

    @Test(timeout = 4000)
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber(" "));
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("123.456"));
        assertTrue(NumberUtils.isNumber("123.456e10"));
        assertTrue(NumberUtils.isNumber("0x1A"));
        assertTrue(NumberUtils.isNumber("-0x1A"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123f"));
        assertTrue(NumberUtils.isNumber("123d"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1L."));
        assertFalse(NumberUtils.isNumber("1.0L"));
        assertFalse(NumberUtils.isNumber("--1"));
        assertFalse(NumberUtils.isNumber("+1"));
        assertFalse(NumberUtils.isNumber("1.0e"));
        assertFalse(NumberUtils.isNumber("1.0e-"));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Known defect: createNumber("--1") should throw NumberFormatException but returns null.
     * This test verifies that NumberFormatException is thrown.
     */
    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberDoubleMinus() {
        NumberUtils.createNumber("--1");
    }

    /**
     * Additional edge: "0x" (no hex digits) should throw NumberFormatException.
     */
    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberHexOnlyPrefix() {
        NumberUtils.createNumber("0x");
    }

    /**
     * Additional edge: "-0x" should throw NumberFormatException.
     */
    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberNegativeHexOnlyPrefix() {
        NumberUtils.createNumber("-0x");
    }

    /**
     * Additional edge: "1e" (exponent without digits) should throw NumberFormatException.
     */
    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberExponentOnly() {
        NumberUtils.createNumber("1e");
    }

    /**
     * Additional edge: "1E" should throw NumberFormatException.
     */
    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberExponentOnlyCapital() {
        NumberUtils.createNumber("1E");
    }

    /**
     * Additional edge: "1L" with decimal point should throw NumberFormatException.
     */
    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberLongWithDecimal() {
        NumberUtils.createNumber("1.0L");
    }

    /**
     * Additional edge: "1.0e" (exponent without digits after e) should throw NumberFormatException.
     */
    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberExponentTrailing() {
        NumberUtils.createNumber("1.0e");
    }

    /**
     * Additional edge: "1.0e-" (exponent with only sign) should throw NumberFormatException.
     */
    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberExponentTrailingSign() {
        NumberUtils.createNumber("1.0e-");
    }

    /**
     * Additional edge: "+1" (leading plus) should throw NumberFormatException (not handled).
     */
    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberLeadingPlus() {
        NumberUtils.createNumber("+1");
    }

    /**
     * Additional edge: blank string should throw NumberFormatException.
     */
    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("  ");
    }

    /**
     * Additional edge: empty string should throw NumberFormatException.
     */
    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberEmpty() {
        NumberUtils.createNumber("");
    }

    /**
     * Additional edge: "1L" with decimal point (already covered) but also "1L" with exponent.
     */
    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberLongWithExponent() {
        NumberUtils.createNumber("1e2L");
    }

    /**
     * Additional edge: "1f" with decimal point (valid) but "1.f" should be valid? Actually "1.f" is valid float.
     * We test that it returns Float.
     */
    @Test(timeout = 4000)
    public void testCreateNumberFloatTrailingDecimal() {
        Number n = NumberUtils.createNumber("1.f");
        assertTrue(n instanceof Float);
        assertEquals(1.0f, n.floatValue(), 0.0f);
    }

    /**
     * Additional edge: "1d" with decimal point (valid).
     */
    @Test(timeout = 4000)
    public void testCreateNumberDoubleTrailingDecimal() {
        Number n = NumberUtils.createNumber("1.d");
        assertTrue(n instanceof Double);
        assertEquals(1.0d, n.doubleValue(), 0.0d);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinLongArrayNull() {
        NumberUtils.min((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinLongArrayEmpty() {
        NumberUtils.min(new long[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinIntArrayNull() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinIntArrayEmpty() {
        NumberUtils.min(new int[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinShortArrayNull() {
        NumberUtils.min((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinShortArrayEmpty() {
        NumberUtils.min(new short[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinByteArrayNull() {
        NumberUtils.min((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinByteArrayEmpty() {
        NumberUtils.min(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinDoubleArrayNull() {
        NumberUtils.min((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinDoubleArrayEmpty() {
        NumberUtils.min(new double[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinFloatArrayNull() {
        NumberUtils.min((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinFloatArrayEmpty() {
        NumberUtils.min(new float[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxLongArrayNull() {
        NumberUtils.max((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxLongArrayEmpty() {
        NumberUtils.max(new long[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxIntArrayNull() {
        NumberUtils.max((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxIntArrayEmpty() {
        NumberUtils.max(new int[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxShortArrayNull() {
        NumberUtils.max((short[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxShortArrayEmpty() {
        NumberUtils.max(new short[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxByteArrayNull() {
        NumberUtils.max((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxByteArrayEmpty() {
        NumberUtils.max(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxDoubleArrayNull() {
        NumberUtils.max((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxDoubleArrayEmpty() {
        NumberUtils.max(new double[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxFloatArrayNull() {
        NumberUtils.max((float[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxFloatArrayEmpty() {
        NumberUtils.max(new float[0]);
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("  ");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimalInvalid() {
        NumberUtils.createBigDecimal("abc");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalid() {
        NumberUtils.createNumber("abc");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidWithType() {
        NumberUtils.createNumber("abcL");
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testConstants() {
        assertNotNull(NumberUtils.LONG_ZERO);
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
    public void testConstructor() {
        // Just ensure it can be instantiated (public constructor)
        new NumberUtils();
    }
}