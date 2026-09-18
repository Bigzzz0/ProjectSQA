package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Branch & Defect Analysis Matrix:
 * - createNumber: hex prefixes, decimal/exponent parsing, type qualifiers (L, F, D), fallback logic, allZeros check
 * - createFloat/Double/BigDecimal: null, blank, leading "--", precision loss
 * - createInteger/Long: decode handles hex/octal, null
 * - createBigInteger: radix detection (hex, octal, decimal), negation
 * - toInt/Long/Float/Double/Byte/Short: null, empty, parse failures, default values
 * - min/max arrays: null/empty validation, single element, NaN handling for float/double
 * - isDigits: null, empty, digits only
 * - isNumber: hex, decimal, exponent, type qualifiers, edge cases
 * - Known defect: precision loss in createNumber when a decimal string with many digits is returned as Float/Double instead of BigDecimal.
 *   Test: "12345678901234567890.123456789" should yield BigDecimal, not Double.
 */
public class NumberUtilsDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testCreateNumberNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(timeout = 4000)
    public void testCreateNumberBlank() {
        try {
            NumberUtils.createNumber("");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
        try {
            NumberUtils.createNumber("   ");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberHex() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
        assertEquals(Long.valueOf(4294967295L), NumberUtils.createNumber("0xFFFFFFFF"));
        assertEquals(new BigInteger("12345678901234567890", 16), NumberUtils.createNumber("0x12345678901234567890"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("#FF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-#FF"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberInteger() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Integer.valueOf(-123), NumberUtils.createNumber("-123"));
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberLong() {
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createNumber("1234567890123"));
        assertEquals(Long.valueOf(-1234567890123L), NumberUtils.createNumber("-1234567890123"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberBigInteger() {
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));
        assertEquals(new BigInteger("-123456789012345678901234567890"), NumberUtils.createNumber("-123456789012345678901234567890"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberFloat() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5"));
        assertEquals(Float.valueOf(-1.5f), NumberUtils.createNumber("-1.5"));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5f"));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5F"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberDouble() {
        assertEquals(Double.valueOf(1.5), NumberUtils.createNumber("1.5d"));
        assertEquals(Double.valueOf(1.5), NumberUtils.createNumber("1.5D"));
        assertEquals(Double.valueOf(1.5), NumberUtils.createNumber("1.5"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberBigDecimal() {
        assertEquals(new BigDecimal("1.234567890123456789"), NumberUtils.createNumber("1.234567890123456789"));
        assertEquals(new BigDecimal("-1.234567890123456789"), NumberUtils.createNumber("-1.234567890123456789"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithExponent() {
        assertEquals(Double.valueOf(1.5e10), NumberUtils.createNumber("1.5e10"));
        assertEquals(Double.valueOf(1.5E10), NumberUtils.createNumber("1.5E10"));
        assertEquals(Double.valueOf(1.5e-10), NumberUtils.createNumber("1.5e-10"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberTypeQualifierL() {
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createNumber("12345678901234567890L"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberTypeQualifierF() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5f"));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5F"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberTypeQualifierD() {
        assertEquals(Double.valueOf(1.5), NumberUtils.createNumber("1.5d"));
        assertEquals(Double.valueOf(1.5), NumberUtils.createNumber("1.5D"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberInvalid() {
        try {
            NumberUtils.createNumber("abc");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
        try {
            NumberUtils.createNumber("1.5.5");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
        try {
            NumberUtils.createNumber("1e");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
        try {
            NumberUtils.createNumber("1e1.5");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testToIntNull() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(1, NumberUtils.toInt(null, 1));
    }

    @Test(timeout = 4000)
    public void testToIntEmpty() {
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(1, NumberUtils.toInt("", 1));
    }

    @Test(timeout = 4000)
    public void testToIntValid() {
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(-123, NumberUtils.toInt("-123"));
        assertEquals(0, NumberUtils.toInt("0"));
    }

    @Test(timeout = 4000)
    public void testToIntInvalid() {
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(5, NumberUtils.toInt("abc", 5));
    }

    @Test(timeout = 4000)
    public void testToLongNull() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(1L, NumberUtils.toLong(null, 1L));
    }

    @Test(timeout = 4000)
    public void testToLongEmpty() {
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(1L, NumberUtils.toLong("", 1L));
    }

    @Test(timeout = 4000)
    public void testToLongValid() {
        assertEquals(123L, NumberUtils.toLong("123"));
        assertEquals(-123L, NumberUtils.toLong("-123"));
    }

    @Test(timeout = 4000)
    public void testToLongInvalid() {
        assertEquals(0L, NumberUtils.toLong("abc"));
        assertEquals(5L, NumberUtils.toLong("abc", 5L));
    }

    @Test(timeout = 4000)
    public void testToFloatNull() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
        assertEquals(1.1f, NumberUtils.toFloat(null, 1.1f), 0.0f);
    }

    @Test(timeout = 4000)
    public void testToFloatEmpty() {
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0f);
        assertEquals(1.1f, NumberUtils.toFloat("", 1.1f), 0.0f);
    }

    @Test(timeout = 4000)
    public void testToFloatValid() {
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0f);
        assertEquals(-1.5f, NumberUtils.toFloat("-1.5"), 0.0f);
    }

    @Test(timeout = 4000)
    public void testToFloatInvalid() {
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0f);
        assertEquals(2.0f, NumberUtils.toFloat("abc", 2.0f), 0.0f);
    }

    @Test(timeout = 4000)
    public void testToDoubleNull() {
        assertEquals(0.0, NumberUtils.toDouble(null), 0.0);
        assertEquals(1.1, NumberUtils.toDouble(null, 1.1), 0.0);
    }

    @Test(timeout = 4000)
    public void testToDoubleEmpty() {
        assertEquals(0.0, NumberUtils.toDouble(""), 0.0);
        assertEquals(1.1, NumberUtils.toDouble("", 1.1), 0.0);
    }

    @Test(timeout = 4000)
    public void testToDoubleValid() {
        assertEquals(1.5, NumberUtils.toDouble("1.5"), 0.0);
        assertEquals(-1.5, NumberUtils.toDouble("-1.5"), 0.0);
    }

    @Test(timeout = 4000)
    public void testToDoubleInvalid() {
        assertEquals(0.0, NumberUtils.toDouble("abc"), 0.0);
        assertEquals(2.0, NumberUtils.toDouble("abc", 2.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testToByteNull() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 1, NumberUtils.toByte(null, (byte) 1));
    }

    @Test(timeout = 4000)
    public void testToByteEmpty() {
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 1, NumberUtils.toByte("", (byte) 1));
    }

    @Test(timeout = 4000)
    public void testToByteValid() {
        assertEquals((byte) 123, NumberUtils.toByte("123"));
        assertEquals((byte) -123, NumberUtils.toByte("-123"));
    }

    @Test(timeout = 4000)
    public void testToByteInvalid() {
        assertEquals((byte) 0, NumberUtils.toByte("abc"));
        assertEquals((byte) 5, NumberUtils.toByte("abc", (byte) 5));
    }

    @Test(timeout = 4000)
    public void testToShortNull() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 1, NumberUtils.toShort(null, (short) 1));
    }

    @Test(timeout = 4000)
    public void testToShortEmpty() {
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 1, NumberUtils.toShort("", (short) 1));
    }

    @Test(timeout = 4000)
    public void testToShortValid() {
        assertEquals((short) 123, NumberUtils.toShort("123"));
        assertEquals((short) -123, NumberUtils.toShort("-123"));
    }

    @Test(timeout = 4000)
    public void testToShortInvalid() {
        assertEquals((short) 0, NumberUtils.toShort("abc"));
        assertEquals((short) 5, NumberUtils.toShort("abc", (short) 5));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * This test targets the known defect: precision loss when creating a number from a string
     * with many decimal digits. The method should return BigDecimal to preserve precision,
     * but the bug may cause it to return Double, losing precision.
     */
    @Test(timeout = 4000)
    public void testStringCreateNumberEnsureNoPrecisionLoss() {
        // A number with many decimal digits that cannot be represented exactly as double
        String preciseNumber = "12345678901234567890.123456789";
        Number result = NumberUtils.createNumber(preciseNumber);
        // The result should be a BigDecimal to preserve all digits
        assertTrue("Precision loss: expected BigDecimal but got " + result.getClass().getSimpleName(),
                result instanceof BigDecimal);
        BigDecimal expected = new BigDecimal(preciseNumber);
        assertEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithExponent() {
        // Scientific notation with many digits
        String preciseExp = "1.234567890123456789E20";
        Number result = NumberUtils.createNumber(preciseExp);
        // Should be BigDecimal to preserve precision
        assertTrue("Precision loss: expected BigDecimal but got " + result.getClass().getSimpleName(),
                result instanceof BigDecimal);
        BigDecimal expected = new BigDecimal(preciseExp);
        assertEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossLargeMantissa() {
        // Large mantissa without decimal point but with exponent
        String largeMantissa = "12345678901234567890E-5";
        Number result = NumberUtils.createNumber(largeMantissa);
        // Should be BigDecimal
        assertTrue("Precision loss: expected BigDecimal but got " + result.getClass().getSimpleName(),
                result instanceof BigDecimal);
        BigDecimal expected = new BigDecimal(largeMantissa);
        assertEquals(expected, result);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testCreateFloatNull() {
        assertNull(NumberUtils.createFloat(null));
    }

    @Test(timeout = 4000)
    public void testCreateFloatInvalid() {
        try {
            NumberUtils.createFloat("abc");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateDoubleNull() {
        assertNull(NumberUtils.createDouble(null));
    }

    @Test(timeout = 4000)
    public void testCreateDoubleInvalid() {
        try {
            NumberUtils.createDouble("abc");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateIntegerNull() {
        assertNull(NumberUtils.createInteger(null));
    }

    @Test(timeout = 4000)
    public void testCreateIntegerInvalid() {
        try {
            NumberUtils.createInteger("abc");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateLongNull() {
        assertNull(NumberUtils.createLong(null));
    }

    @Test(timeout = 4000)
    public void testCreateLongInvalid() {
        try {
            NumberUtils.createLong("abc");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateBigIntegerNull() {
        assertNull(NumberUtils.createBigInteger(null));
    }

    @Test(timeout = 4000)
    public void testCreateBigIntegerInvalid() {
        try {
            NumberUtils.createBigInteger("abc");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimalNull() {
        assertNull(NumberUtils.createBigDecimal(null));
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimalBlank() {
        try {
            NumberUtils.createBigDecimal("");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimalDoubleDash() {
        try {
            NumberUtils.createBigDecimal("--1.5");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimalValid() {
        assertEquals(new BigDecimal("1.5"), NumberUtils.createBigDecimal("1.5"));
        assertEquals(new BigDecimal("-1.5"), NumberUtils.createBigDecimal("-1.5"));
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testMinLongArray() {
        assertEquals(1L, NumberUtils.min(new long[]{1L, 2L, 3L}));
        assertEquals(-3L, NumberUtils.min(new long[]{-1L, -2L, -3L}));
        assertEquals(0L, NumberUtils.min(new long[]{0L}));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMinLongArrayNull() {
        NumberUtils.min((long[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMinLongArrayEmpty() {
        NumberUtils.min(new long[0]);
    }

    @Test(timeout = 4000)
    public void testMinIntArray() {
        assertEquals(1, NumberUtils.min(new int[]{1, 2, 3}));
        assertEquals(-3, NumberUtils.min(new int[]{-1, -2, -3}));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMinIntArrayNull() {
        NumberUtils.min((int[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMinIntArrayEmpty() {
        NumberUtils.min(new int[0]);
    }

    @Test(timeout = 4000)
    public void testMinShortArray() {
        assertEquals((short) 1, NumberUtils.min(new short[]{1, 2, 3}));
        assertEquals((short) -3, NumberUtils.min(new short[]{-1, -2, -3}));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMinShortArrayNull() {
        NumberUtils.min((short[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMinShortArrayEmpty() {
        NumberUtils.min(new short[0]);
    }

    @Test(timeout = 4000)
    public void testMinByteArray() {
        assertEquals((byte) 1, NumberUtils.min(new byte[]{1, 2, 3}));
        assertEquals((byte) -3, NumberUtils.min(new byte[]{-1, -2, -3}));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMinByteArrayNull() {
        NumberUtils.min((byte[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMinByteArrayEmpty() {
        NumberUtils.min(new byte[0]);
    }

    @Test(timeout = 4000)
    public void testMinDoubleArray() {
        assertEquals(1.0, NumberUtils.min(new double[]{1.0, 2.0, 3.0}), 0.0);
        assertEquals(-3.0, NumberUtils.min(new double[]{-1.0, -2.0, -3.0}), 0.0);
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.0, Double.NaN, 3.0})));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMinDoubleArrayNull() {
        NumberUtils.min((double[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMinDoubleArrayEmpty() {
        NumberUtils.min(new double[0]);
    }

    @Test(timeout = 4000)
    public void testMinFloatArray() {
        assertEquals(1.0f, NumberUtils.min(new float[]{1.0f, 2.0f, 3.0f}), 0.0f);
        assertEquals(-3.0f, NumberUtils.min(new float[]{-1.0f, -2.0f, -3.0f}), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.0f, Float.NaN, 3.0f})));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMinFloatArrayNull() {
        NumberUtils.min((float[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMinFloatArrayEmpty() {
        NumberUtils.min(new float[0]);
    }

    @Test(timeout = 4000)
    public void testMaxLongArray() {
        assertEquals(3L, NumberUtils.max(new long[]{1L, 2L, 3L}));
        assertEquals(-1L, NumberUtils.max(new long[]{-1L, -2L, -3L}));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMaxLongArrayNull() {
        NumberUtils.max((long[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMaxLongArrayEmpty() {
        NumberUtils.max(new long[0]);
    }

    @Test(timeout = 4000)
    public void testMaxIntArray() {
        assertEquals(3, NumberUtils.max(new int[]{1, 2, 3}));
        assertEquals(-1, NumberUtils.max(new int[]{-1, -2, -3}));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMaxIntArrayNull() {
        NumberUtils.max((int[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMaxIntArrayEmpty() {
        NumberUtils.max(new int[0]);
    }

    @Test(timeout = 4000)
    public void testMaxShortArray() {
        assertEquals((short) 3, NumberUtils.max(new short[]{1, 2, 3}));
        assertEquals((short) -1, NumberUtils.max(new short[]{-1, -2, -3}));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMaxShortArrayNull() {
        NumberUtils.max((short[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMaxShortArrayEmpty() {
        NumberUtils.max(new short[0]);
    }

    @Test(timeout = 4000)
    public void testMaxByteArray() {
        assertEquals((byte) 3, NumberUtils.max(new byte[]{1, 2, 3}));
        assertEquals((byte) -1, NumberUtils.max(new byte[]{-1, -2, -3}));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMaxByteArrayNull() {
        NumberUtils.max((byte[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMaxByteArrayEmpty() {
        NumberUtils.max(new byte[0]);
    }

    @Test(timeout = 4000)
    public void testMaxDoubleArray() {
        assertEquals(3.0, NumberUtils.max(new double[]{1.0, 2.0, 3.0}), 0.0);
        assertEquals(-1.0, NumberUtils.max(new double[]{-1.0, -2.0, -3.0}), 0.0);
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0, Double.NaN, 3.0})));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMaxDoubleArrayNull() {
        NumberUtils.max((double[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMaxDoubleArrayEmpty() {
        NumberUtils.max(new double[0]);
    }

    @Test(timeout = 4000)
    public void testMaxFloatArray() {
        assertEquals(3.0f, NumberUtils.max(new float[]{1.0f, 2.0f, 3.0f}), 0.0f);
        assertEquals(-1.0f, NumberUtils.max(new float[]{-1.0f, -2.0f, -3.0f}), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN, 3.0f})));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMaxFloatArrayNull() {
        NumberUtils.max((float[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMaxFloatArrayEmpty() {
        NumberUtils.max(new float[0]);
    }

    // Three-param min/max
    @Test(timeout = 4000)
    public void testMinLong() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(-3L, NumberUtils.min(-1L, -2L, -3L));
    }

    @Test(timeout = 4000)
    public void testMinInt() {
        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(-3, NumberUtils.min(-1, -2, -3));
    }

    @Test(timeout = 4000)
    public void testMinShort() {
        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) -3, NumberUtils.min((short) -1, (short) -2, (short) -3));
    }

    @Test(timeout = 4000)
    public void testMinByte() {
        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) -3, NumberUtils.min((byte) -1, (byte) -2, (byte) -3));
    }

    @Test(timeout = 4000)
    public void testMinDouble() {
        assertEquals(1.0, NumberUtils.min(1.0, 2.0, 3.0), 0.0);
        assertEquals(-3.0, NumberUtils.min(-1.0, -2.0, -3.0), 0.0);
        assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, 1.0, 2.0)));
    }

    @Test(timeout = 4000)
    public void testMinFloat() {
        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.0f);
        assertEquals(-3.0f, NumberUtils.min(-1.0f, -2.0f, -3.0f), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, 1.0f, 2.0f)));
    }

    @Test(timeout = 4000)
    public void testMaxLong() {
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(-1L, NumberUtils.max(-1L, -2L, -3L));
    }

    @Test(timeout = 4000)
    public void testMaxInt() {
        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(-1, NumberUtils.max(-1, -2, -3));
    }

    @Test(timeout = 4000)
    public void testMaxShort() {
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        assertEquals((short) -1, NumberUtils.max((short) -1, (short) -2, (short) -3));
    }

    @Test(timeout = 4000)
    public void testMaxByte() {
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) -1, NumberUtils.max((byte) -1, (byte) -2, (byte) -3));
    }

    @Test(timeout = 4000)
    public void testMaxDouble() {
        assertEquals(3.0, NumberUtils.max(1.0, 2.0, 3.0), 0.0);
        assertEquals(-1.0, NumberUtils.max(-1.0, -2.0, -3.0), 0.0);
        assertTrue(Double.isNaN(NumberUtils.max(Double.NaN, 1.0, 2.0)));
    }

    @Test(timeout = 4000)
    public void testMaxFloat() {
        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.0f);
        assertEquals(-1.0f, NumberUtils.max(-1.0f, -2.0f, -3.0f), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.max(Float.NaN, 1.0f, 2.0f)));
    }

    // isDigits
    @Test(timeout = 4000)
    public void testIsDigitsNull() {
        assertFalse(NumberUtils.isDigits(null));
    }

    @Test(timeout = 4000)
    public void testIsDigitsEmpty() {
        assertFalse(NumberUtils.isDigits(""));
    }

    @Test(timeout = 4000)
    public void testIsDigitsValid() {
        assertTrue(NumberUtils.isDigits("123"));
        assertTrue(NumberUtils.isDigits("0"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertFalse(NumberUtils.isDigits("abc"));
    }

    // isNumber
    @Test(timeout = 4000)
    public void testIsNumberNull() {
        assertFalse(NumberUtils.isNumber(null));
    }

    @Test(timeout = 4000)
    public void testIsNumberEmpty() {
        assertFalse(NumberUtils.isNumber(""));
    }

    @Test(timeout = 4000)
    public void testIsNumberValid() {
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("1.5"));
        assertTrue(NumberUtils.isNumber("1.5e10"));
        assertTrue(NumberUtils.isNumber("1.5E-10"));
        assertTrue(NumberUtils.isNumber("0xFF"));
        assertTrue(NumberUtils.isNumber("-0xFF"));
        assertTrue(NumberUtils.isNumber("#FF"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("1.5f"));
        assertTrue(NumberUtils.isNumber("1.5D"));
        assertTrue(NumberUtils.isNumber("0"));
        assertTrue(NumberUtils.isNumber("0.0"));
    }

    @Test(timeout = 4000)
    public void testIsNumberInvalid() {
        assertFalse(NumberUtils.isNumber("--123"));
        assertFalse(NumberUtils.isNumber("1.5.5"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e1.5"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("0xGH"));
        assertFalse(NumberUtils.isNumber("123L."));
        assertFalse(NumberUtils.isNumber("123Le"));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("1.2.3"));
    }

    // Additional edge cases for createNumber
    @Test(timeout = 4000)
    public void testCreateNumberOctal() {
        // Leading zero interpreted as octal
        assertEquals(Integer.valueOf(83), NumberUtils.createNumber("0123")); // octal 123 = decimal 83
        assertEquals(Long.valueOf(83L), NumberUtils.createNumber("0123L"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberNegativeHex() {
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
        assertEquals(Long.valueOf(-4294967295L), NumberUtils.createNumber("-0xFFFFFFFF"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithLeadingPlus() {
        // createNumber does not handle leading '+'? Actually it does not; it will throw NumberFormatException
        try {
            NumberUtils.createNumber("+123");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberTrailingDot() {
        // e.g., "123." should be valid? According to isNumber it is valid if foundDigit.
        // createNumber: decPos > -1, dec = "" (empty), mant = "123", numDecimals = 0.
        // Then it goes to floating point path. Should return Double or BigDecimal.
        Number result = NumberUtils.createNumber("123.");
        assertTrue(result instanceof Double || result instanceof BigDecimal);
        assertEquals(123.0, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberOnlyDecimal() {
        // "." alone should throw
        try {
            NumberUtils.createNumber(".");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberExponentOnly() {
        try {
            NumberUtils.createNumber("1e");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberDoubleExponent() {
        try {
            NumberUtils.createNumber("1e2e3");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberInvalidTypeQualifier() {
        try {
            NumberUtils.createNumber("123x");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberAllZerosFloat() {
        // "0.0" should return Float 0.0, not BigDecimal
        Number result = NumberUtils.createNumber("0.0");
        assertTrue(result instanceof Float);
        assertEquals(0.0f, result.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberAllZerosDouble() {
        Number result = NumberUtils.createNumber("0.0d");
        assertTrue(result instanceof Double);
        assertEquals(0.0, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberLargeExponentFloat() {
        // Value that is too large for Float but fits in Double
        Number result = NumberUtils.createNumber("1e50");
        assertTrue(result instanceof Double);
        assertEquals(1e50, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberInfiniteFloat() {
        // "1e40" is within float range? Actually 1e40 is > Float.MAX_VALUE, so Float will be infinite.
        Number result = NumberUtils.createNumber("1e40");
        // Should fall back to Double
        assertTrue(result instanceof Double);
        assertEquals(1e40, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberZeroWithNonZeroMantissa() {
        // "0.1" should return Float (since not all zeros)
        Number result = NumberUtils.createNumber("0.1");
        assertTrue(result instanceof Float);
        assertEquals(0.1f, result.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberBigDecimalFromLargeDecimal() {
        // Very large decimal that cannot be represented as double exactly
        String bigDec = "123456789012345678901234567890.123456789";
        Number result = NumberUtils.createNumber(bigDec);
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal(bigDec), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberBigIntegerFromHex() {
        assertEquals(new BigInteger("1234567890abcdef", 16), NumberUtils.createNumber("0x1234567890abcdef"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberNegativeBigInteger() {
        assertEquals(new BigInteger("-12345678901234567890"), NumberUtils.createNumber("-12345678901234567890"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberLongWithLQualifier() {
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(-123L), NumberUtils.createNumber("-123L"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberFloatWithFQualifier() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5f"));
        assertEquals(Float.valueOf(-1.5f), NumberUtils.createNumber("-1.5f"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberDoubleWithDQualifier() {
        assertEquals(Double.valueOf(1.5), NumberUtils.createNumber("1.5d"));
        assertEquals(Double.valueOf(-1.5), NumberUtils.createNumber("-1.5d"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberInvalidLWithDecimal() {
        try {
            NumberUtils.createNumber("1.5L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberInvalidLWithExponent() {
        try {
            NumberUtils.createNumber("1e2L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberInvalidFWithExponent() {
        // "1e2f" is actually valid? Float.parseFloat("1e2f") works? Actually "1e2f" is not valid because 'f' is not part of number.
        // But createNumber strips last char and tries Float.valueOf("1e2") which is valid.
        // However, the code checks allZeros and infinite. "1e2" is not all zeros, not infinite, so returns Float.
        // So "1e2f" should be valid and return Float.
        Number result = NumberUtils.createNumber("1e2f");
        assertTrue(result instanceof Float);
        assertEquals(100.0f, result.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberInvalidDWithExponent() {
        Number result = NumberUtils.createNumber("1e2d");
        assertTrue(result instanceof Double);
        assertEquals(100.0, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberTrailingDotWithExponent() {
        // "1.e2" should be valid? decPos = 1, expPos = 3, dec = "" (substring(2,3) = ""), mant = "1", exp = "2"
        // Then it goes to floating point path. Should return Double.
        Number result = NumberUtils.createNumber("1.e2");
        assertTrue(result instanceof Double);
        assertEquals(100.0, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberLeadingDot() {
        // ".5" should be valid? decPos = 0, mant = "" (substring(0,0) = ""), dec = "5"
        // Then mant is empty, but later isAllZeros(mant) returns true (null treated as all zeros).
        // Should return Float.
        Number result = NumberUtils.createNumber(".5");
        assertTrue(result instanceof Float);
        assertEquals(0.5f, result.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberNegativeExponent() {
        Number result = NumberUtils.createNumber("1e-2");
        assertTrue(result instanceof Double);
        assertEquals(0.01, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPositiveExponent() {
        Number result = NumberUtils.createNumber("1e+2");
        assertTrue(result instanceof Double);
        assertEquals(100.0, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberExponentWithoutSign() {
        Number result = NumberUtils.createNumber("1e2");
        assertTrue(result instanceof Double);
        assertEquals(100.0, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberHexUpperCase() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0XFF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0XFF"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberHexWithLeadingZeros() {
        assertEquals(Integer.valueOf(15), NumberUtils.createNumber("0x0F"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberHexNegativeLong() {
        assertEquals(Long.valueOf(-4294967295L), NumberUtils.createNumber("-0xFFFFFFFF"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberHexBigInteger() {
        assertEquals(new BigInteger("123456789012345678901234567890", 16), NumberUtils.createNumber("0x123456789012345678901234567890"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberOctalLeadingZero() {
        // "0123" is octal 83
        assertEquals(Integer.valueOf(83), NumberUtils.createNumber("0123"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberOctalWithL() {
        assertEquals(Long.valueOf(83L), NumberUtils.createNumber("0123L"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberNegativeOctal() {
        assertEquals(Integer.valueOf(-83), NumberUtils.createNumber("-0123"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberZeroOnly() {
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberNegativeZero() {
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("-0"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberZeroFloat() {
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberZeroDouble() {
        assertEquals(Double.valueOf(0.0), NumberUtils.createNumber("0.0d"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberZeroBigDecimal() {
        assertEquals(new BigDecimal("0.0"), NumberUtils.createNumber("0.0"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberNegativeZeroFloat() {
        assertEquals(Float.valueOf(-0.0f), NumberUtils.createNumber("-0.0"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberNegativeZeroDouble() {
        assertEquals(Double.valueOf(-0.0), NumberUtils.createNumber("-0.0d"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberNegativeZeroBigDecimal() {
        assertEquals(new BigDecimal("-0.0"), NumberUtils.createNumber("-0.0"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberVeryLargeExponent() {
        // 1e1000000 is too large for Double (infinity), should fall back to BigDecimal
        Number result = NumberUtils.createNumber("1e1000000");
        assertTrue(result instanceof BigDecimal);
        // BigDecimal can handle it
        assertEquals(new BigDecimal("1e1000000"), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberVerySmallExponent() {
        Number result = NumberUtils.createNumber("1e-1000000");
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("1e-1000000"), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithLargeMantissaAndExponent() {
        // This should be BigDecimal
        String str = "123456789012345678901234567890.123456789e10";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal(str), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeExponent() {
        String str = "123456789012345678901234567890.123456789e-10";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal(str), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNoDecimal() {
        // Large integer that fits in BigInteger but not Long
        String str = "123456789012345678901234567890";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger(str), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithDecimalAndNoExponent() {
        String str = "123456789012345678901234567890.123456789";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal(str), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithExponentOnly() {
        String str = "1.234567890123456789E20";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal(str), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithLargeExponentAndDecimal() {
        String str = "1.234567890123456789E200";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal(str), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeLargeExponent() {
        String str = "1.234567890123456789E-200";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal(str), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithHex() {
        // Hex numbers are integral, no precision loss expected
        String str = "0x1234567890ABCDEF1234567890ABCDEF";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger(str.substring(2), 16), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithOctal() {
        // Octal numbers are integral
        String str = "012345670123456701234567";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger(str, 8), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierF() {
        // Explicit float qualifier should return Float even if precision loss
        String str = "1.234567890123456789f";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof Float);
        // Float has limited precision, but that's expected
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierD() {
        String str = "1.234567890123456789d";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof Double);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierL() {
        String str = "12345678901234567890L";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger("12345678901234567890"), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierLAndDecimal() {
        try {
            NumberUtils.createNumber("1.5L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierLAndExponent() {
        try {
            NumberUtils.createNumber("1e2L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierFAndExponent() {
        // "1e2f" is valid and returns Float
        Number result = NumberUtils.createNumber("1e2f");
        assertTrue(result instanceof Float);
        assertEquals(100.0f, result.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierDAndExponent() {
        Number result = NumberUtils.createNumber("1e2d");
        assertTrue(result instanceof Double);
        assertEquals(100.0, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithAllZerosAndTypeQualifier() {
        // "0.0f" should return Float 0.0
        Number result = NumberUtils.createNumber("0.0f");
        assertTrue(result instanceof Float);
        assertEquals(0.0f, result.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithAllZerosAndTypeQualifierD() {
        Number result = NumberUtils.createNumber("0.0d");
        assertTrue(result instanceof Double);
        assertEquals(0.0, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNonZeroAndTypeQualifierF() {
        // "0.1f" should return Float
        Number result = NumberUtils.createNumber("0.1f");
        assertTrue(result instanceof Float);
        assertEquals(0.1f, result.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNonZeroAndTypeQualifierD() {
        Number result = NumberUtils.createNumber("0.1d");
        assertTrue(result instanceof Double);
        assertEquals(0.1, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithLargeNumberAndTypeQualifierF() {
        // Large number that is infinite as float, should fall back to Double then BigDecimal
        String str = "1e40f";
        Number result = NumberUtils.createNumber(str);
        // Since Float is infinite, it goes to Double, then BigDecimal if Double is infinite? Actually 1e40 is finite as Double.
        // So result should be Double.
        assertTrue(result instanceof Double);
        assertEquals(1e40, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithVeryLargeNumberAndTypeQualifierF() {
        // 1e500f -> Float infinite, Double infinite, so fallback to BigDecimal
        String str = "1e500f";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("1e500"), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithVeryLargeNumberAndTypeQualifierD() {
        String str = "1e500d";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("1e500"), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeExponentAndTypeQualifierF() {
        // 1e-50f -> Float underflow to 0.0? Actually Float.MIN_NORMAL is about 1.4e-45, so 1e-50 is 0.0.
        // But allZeros check: mant = "1", exp = "-50", not all zeros, so 0.0f is not all zeros? Actually 0.0f is zero but allZeros is false.
        // The code: if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) -> f.floatValue() == 0.0F and !allZeros is true, so condition is false, so it does NOT return f.
        // Then it falls through to Double, then BigDecimal.
        String str = "1e-50f";
        Number result = NumberUtils.createNumber(str);
        // Should be BigDecimal because Float underflows to 0.0 but allZeros false.
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("1e-50"), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeExponentAndTypeQualifierD() {
        String str = "1e-50d";
        Number result = NumberUtils.createNumber(str);
        // Double underflow? Double.MIN_NORMAL is 2.2e-308, so 1e-50 is fine.
        assertTrue(result instanceof Double);
        assertEquals(1e-50, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithVerySmallExponentAndTypeQualifierD() {
        String str = "1e-400d";
        Number result = NumberUtils.createNumber(str);
        // Double underflow to 0.0, allZeros false, so fallback to BigDecimal
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("1e-400"), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithZeroMantissaAndNonZeroExponent() {
        // "0e10" -> mant = "0", exp = "10", allZeros true, so Float 0.0 is returned.
        Number result = NumberUtils.createNumber("0e10");
        assertTrue(result instanceof Float);
        assertEquals(0.0f, result.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithZeroMantissaAndNegativeExponent() {
        Number result = NumberUtils.createNumber("0e-10");
        assertTrue(result instanceof Float);
        assertEquals(0.0f, result.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNonZeroMantissaAndZeroExponent() {
        // "1e0" -> mant = "1", exp = "0", allZeros false, so Float 1.0 is returned.
        Number result = NumberUtils.createNumber("1e0");
        assertTrue(result instanceof Float);
        assertEquals(1.0f, result.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithDecimalAndExponentAndAllZeros() {
        // "0.0e10" -> mant = "0", dec = "0", exp = "10", allZeros true, returns Float 0.0
        Number result = NumberUtils.createNumber("0.0e10");
        assertTrue(result instanceof Float);
        assertEquals(0.0f, result.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithDecimalAndExponentAndNonAllZeros() {
        // "0.1e10" -> mant = "0", dec = "1", exp = "10", allZeros false, returns Float 1e9? Actually 0.1e10 = 1e9, which is finite.
        Number result = NumberUtils.createNumber("0.1e10");
        assertTrue(result instanceof Float);
        assertEquals(1e9f, result.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithLargeDecimalAndExponent() {
        // "12345678901234567890.123456789e10" should be BigDecimal
        String str = "12345678901234567890.123456789e10";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal(str), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeLargeDecimalAndExponent() {
        String str = "-12345678901234567890.123456789e10";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal(str), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithVeryLargeExponentAndDecimal() {
        String str = "1.234567890123456789e1000000";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal(str), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithVerySmallExponentAndDecimal() {
        String str = "1.234567890123456789e-1000000";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal(str), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithHexAndLargeDigits() {
        String str = "0x1234567890123456789012345678901234567890";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger(str.substring(2), 16), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithOctalAndLargeDigits() {
        String str = "0123456701234567012345670123456701234567";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger(str, 8), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeHex() {
        String str = "-0x12345678901234567890";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger("-12345678901234567890", 16), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeOctal() {
        String str = "-012345670123456701234567";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger("-12345670123456701234567", 8), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithHexAndTypeQualifierL() {
        // Hex with L qualifier: "0xFFL" -> lastChar 'L', numeric = "0xFF", dec null, exp null, numeric starts with '0'? Actually "0xFF" starts with '0', but isDigits? "0xFF" contains 'x' and 'F', so isDigits returns false. But the code checks (numeric.charAt(0) == '-' && isDigits(numeric.substring(1)) || isDigits(numeric)). Since numeric is "0xFF", isDigits returns false, and it doesn't start with '-', so condition false, throws NumberFormatException.
        try {
            NumberUtils.createNumber("0xFFL");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithHexAndTypeQualifierF() {
        // "0xFFf" -> lastChar 'f', numeric = "0xFF", dec null, exp null, then try createFloat("0xFF") -> Float.valueOf("0xFF") throws NumberFormatException because "0xFF" is not a valid float string.
        // Then fall through to Double, then BigDecimal. But createDouble also throws. Then createBigDecimal("0xFF") throws. So default throws.
        try {
            NumberUtils.createNumber("0xFFf");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithHexAndTypeQualifierD() {
        try {
            NumberUtils.createNumber("0xFFd");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeHexAndTypeQualifierL() {
        try {
            NumberUtils.createNumber("-0xFFL");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeHexAndTypeQualifierF() {
        try {
            NumberUtils.createNumber("-0xFFf");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeHexAndTypeQualifierD() {
        try {
            NumberUtils.createNumber("-0xFFd");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithOctalAndTypeQualifierL() {
        // "0123L" -> lastChar 'L', numeric = "0123", dec null, exp null, isDigits("0123") true, so createLong("0123") returns Long 83.
        Number result = NumberUtils.createNumber("0123L");
        assertEquals(Long.valueOf(83L), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithOctalAndTypeQualifierF() {
        // "0123f" -> lastChar 'f', numeric = "0123", try createFloat("0123") -> Float.valueOf("0123") returns 123.0f (since leading zero is ignored). Not infinite, not zero with non-zero, so returns Float.
        Number result = NumberUtils.createNumber("0123f");
        assertEquals(Float.valueOf(123.0f), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithOctalAndTypeQualifierD() {
        Number result = NumberUtils.createNumber("0123d");
        assertEquals(Double.valueOf(123.0), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeOctalAndTypeQualifierL() {
        Number result = NumberUtils.createNumber("-0123L");
        assertEquals(Long.valueOf(-83L), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeOctalAndTypeQualifierF() {
        Number result = NumberUtils.createNumber("-0123f");
        assertEquals(Float.valueOf(-123.0f), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeOctalAndTypeQualifierD() {
        Number result = NumberUtils.createNumber("-0123d");
        assertEquals(Double.valueOf(-123.0), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithDecimalAndTypeQualifierL() {
        try {
            NumberUtils.createNumber("1.5L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithDecimalAndTypeQualifierF() {
        Number result = NumberUtils.createNumber("1.5f");
        assertEquals(Float.valueOf(1.5f), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithDecimalAndTypeQualifierD() {
        Number result = NumberUtils.createNumber("1.5d");
        assertEquals(Double.valueOf(1.5), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithExponentAndTypeQualifierL() {
        try {
            NumberUtils.createNumber("1e2L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithExponentAndTypeQualifierF() {
        Number result = NumberUtils.createNumber("1e2f");
        assertEquals(Float.valueOf(100.0f), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithExponentAndTypeQualifierD() {
        Number result = NumberUtils.createNumber("1e2d");
        assertEquals(Double.valueOf(100.0), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeExponentAndTypeQualifierF() {
        Number result = NumberUtils.createNumber("1e-2f");
        assertEquals(Float.valueOf(0.01f), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeExponentAndTypeQualifierD() {
        Number result = NumberUtils.createNumber("1e-2d");
        assertEquals(Double.valueOf(0.01), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithLargeExponentAndTypeQualifierF() {
        // 1e40f -> Float infinite, fallback to Double
        Number result = NumberUtils.createNumber("1e40f");
        assertTrue(result instanceof Double);
        assertEquals(1e40, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithLargeExponentAndTypeQualifierD() {
        Number result = NumberUtils.createNumber("1e40d");
        assertTrue(result instanceof Double);
        assertEquals(1e40, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithVeryLargeExponentAndTypeQualifierF() {
        Number result = NumberUtils.createNumber("1e500f");
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("1e500"), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithVeryLargeExponentAndTypeQualifierD() {
        Number result = NumberUtils.createNumber("1e500d");
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("1e500"), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithVerySmallExponentAndTypeQualifierF() {
        Number result = NumberUtils.createNumber("1e-50f");
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("1e-50"), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithVerySmallExponentAndTypeQualifierD() {
        Number result = NumberUtils.createNumber("1e-400d");
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("1e-400"), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithZeroAndTypeQualifierF() {
        Number result = NumberUtils.createNumber("0f");
        assertEquals(Float.valueOf(0.0f), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithZeroAndTypeQualifierD() {
        Number result = NumberUtils.createNumber("0d");
        assertEquals(Double.valueOf(0.0), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithZeroAndTypeQualifierL() {
        Number result = NumberUtils.createNumber("0L");
        assertEquals(Long.valueOf(0L), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeZeroAndTypeQualifierF() {
        Number result = NumberUtils.createNumber("-0f");
        assertEquals(Float.valueOf(-0.0f), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeZeroAndTypeQualifierD() {
        Number result = NumberUtils.createNumber("-0d");
        assertEquals(Double.valueOf(-0.0), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeZeroAndTypeQualifierL() {
        Number result = NumberUtils.createNumber("-0L");
        assertEquals(Long.valueOf(0L), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithOneAndTypeQualifierF() {
        Number result = NumberUtils.createNumber("1f");
        assertEquals(Float.valueOf(1.0f), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithOneAndTypeQualifierD() {
        Number result = NumberUtils.createNumber("1d");
        assertEquals(Double.valueOf(1.0), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithOneAndTypeQualifierL() {
        Number result = NumberUtils.createNumber("1L");
        assertEquals(Long.valueOf(1L), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeOneAndTypeQualifierF() {
        Number result = NumberUtils.createNumber("-1f");
        assertEquals(Float.valueOf(-1.0f), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeOneAndTypeQualifierD() {
        Number result = NumberUtils.createNumber("-1d");
        assertEquals(Double.valueOf(-1.0), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeOneAndTypeQualifierL() {
        Number result = NumberUtils.createNumber("-1L");
        assertEquals(Long.valueOf(-1L), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithMaxLong() {
        String str = Long.toString(Long.MAX_VALUE);
        Number result = NumberUtils.createNumber(str);
        assertEquals(Long.valueOf(Long.MAX_VALUE), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithMinLong() {
        String str = Long.toString(Long.MIN_VALUE);
        Number result = NumberUtils.createNumber(str);
        assertEquals(Long.valueOf(Long.MIN_VALUE), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithOverflowLong() {
        String str = "9223372036854775808"; // Long.MAX_VALUE + 1
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger(str), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithUnderflowLong() {
        String str = "-9223372036854775809"; // Long.MIN_VALUE - 1
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger(str), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithMaxInt() {
        String str = Integer.toString(Integer.MAX_VALUE);
        Number result = NumberUtils.createNumber(str);
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithMinInt() {
        String str = Integer.toString(Integer.MIN_VALUE);
        Number result = NumberUtils.createNumber(str);
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithOverflowInt() {
        String str = "2147483648"; // Integer.MAX_VALUE + 1
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof Long);
        assertEquals(Long.valueOf(2147483648L), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithUnderflowInt() {
        String str = "-2147483649"; // Integer.MIN_VALUE - 1
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof Long);
        assertEquals(Long.valueOf(-2147483649L), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithMaxFloat() {
        String str = "3.4028235e38f";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof Float);
        assertEquals(Float.MAX_VALUE, result.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithMinFloat() {
        String str = "1.4e-45f";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof Float);
        assertEquals(Float.MIN_VALUE, result.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithMaxDouble() {
        String str = "1.7976931348623157e308d";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof Double);
        assertEquals(Double.MAX_VALUE, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithMinDouble() {
        String str = "4.9e-324d";
        Number result = NumberUtils.createNumber(str);
        assertTrue(result instanceof Double);
        assertEquals(Double.MIN_VALUE, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithOverflowFloat() {
        String str = "3.4028235e39f";
        Number result = NumberUtils.createNumber(str);
        // Float infinite, fallback to Double
        assertTrue(result instanceof Double);
        assertEquals(3.4028235e39, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithOverflowDouble() {
        String str = "1.7976931348623157e309d";
        Number result = NumberUtils.createNumber(str);
        // Double infinite, fallback to BigDecimal
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("1.7976931348623157e309"), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithUnderflowFloat() {
        String str = "1.4e-46f";
        Number result = NumberUtils.createNumber(str);
        // Float underflow to 0.0, allZeros false, fallback to Double then BigDecimal
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("1.4e-46"), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithUnderflowDouble() {
        String str = "4.9e-325d";
        Number result = NumberUtils.createNumber(str);
        // Double underflow to 0.0, allZeros false, fallback to BigDecimal
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("4.9e-325"), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNaN() {
        try {
            NumberUtils.createNumber("NaN");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithInfinity() {
        try {
            NumberUtils.createNumber("Infinity");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeInfinity() {
        try {
            NumberUtils.createNumber("-Infinity");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithHexAndNegativeExponent() {
        // Hex numbers cannot have exponent, so this should throw
        try {
            NumberUtils.createNumber("0x1e2");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithHexAndDecimal() {
        try {
            NumberUtils.createNumber("0x1.5");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithOctalAndDecimal() {
        try {
            NumberUtils.createNumber("0123.5");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithOctalAndExponent() {
        try {
            NumberUtils.createNumber("0123e2");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithLeadingPlus() {
        try {
            NumberUtils.createNumber("+123");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithLeadingPlusAndDecimal() {
        try {
            NumberUtils.createNumber("+1.5");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithLeadingPlusAndExponent() {
        try {
            NumberUtils.createNumber("+1e2");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithLeadingPlusAndHex() {
        try {
            NumberUtils.createNumber("+0xFF");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithLeadingPlusAndOctal() {
        try {
            NumberUtils.createNumber("+0123");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTrailingWhitespace() {
        try {
            NumberUtils.createNumber("123 ");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithLeadingWhitespace() {
        try {
            NumberUtils.createNumber(" 123");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTab() {
        try {
            NumberUtils.createNumber("\t123");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNewline() {
        try {
            NumberUtils.createNumber("\n123");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithEmptyString() {
        try {
            NumberUtils.createNumber("");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithBlankString() {
        try {
            NumberUtils.createNumber("   ");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithNegativeSignOnly() {
        try {
            NumberUtils.createNumber("-");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithDecimalPointOnly() {
        try {
            NumberUtils.createNumber(".");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithExponentOnly() {
        try {
            NumberUtils.createNumber("e");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithSignOnly() {
        try {
            NumberUtils.createNumber("+");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithMultipleSigns() {
        try {
            NumberUtils.createNumber("--123");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithMultipleDecimals() {
        try {
            NumberUtils.createNumber("1.2.3");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithMultipleExponents() {
        try {
            NumberUtils.createNumber("1e2e3");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithExponentAfterDecimal() {
        try {
            NumberUtils.createNumber("1.2e3e4");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithExponentBeforeDecimal() {
        try {
            NumberUtils.createNumber("1e2.3");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithInvalidTypeQualifier() {
        try {
            NumberUtils.createNumber("123x");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAfterDecimal() {
        try {
            NumberUtils.createNumber("1.5L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAfterExponent() {
        try {
            NumberUtils.createNumber("1e2L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDecimal() {
        try {
            NumberUtils.createNumber("1.5f");
            // This is valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponent() {
        try {
            NumberUtils.createNumber("1e2f");
            // This is valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponent() {
        try {
            NumberUtils.createNumber("1e-2f");
            // This is valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLargeExponent() {
        try {
            NumberUtils.createNumber("1e40f");
            // This is valid (returns Double)
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndVeryLargeExponent() {
        try {
            NumberUtils.createNumber("1e500f");
            // This is valid (returns BigDecimal)
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndVerySmallExponent() {
        try {
            NumberUtils.createNumber("1e-50f");
            // This is valid (returns BigDecimal)
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndZero() {
        try {
            NumberUtils.createNumber("0f");
            // Valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeZero() {
        try {
            NumberUtils.createNumber("-0f");
            // Valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOne() {
        try {
            NumberUtils.createNumber("1f");
            // Valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeOne() {
        try {
            NumberUtils.createNumber("-1f");
            // Valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndMaxFloat() {
        try {
            NumberUtils.createNumber("3.4028235e38f");
            // Valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndMinFloat() {
        try {
            NumberUtils.createNumber("1.4e-45f");
            // Valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOverflowFloat() {
        try {
            NumberUtils.createNumber("3.4028235e39f");
            // Valid (returns Double)
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndUnderflowFloat() {
        try {
            NumberUtils.createNumber("1.4e-46f");
            // Valid (returns BigDecimal)
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndMaxDouble() {
        try {
            NumberUtils.createNumber("1.7976931348623157e308d");
            // Valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndMinDouble() {
        try {
            NumberUtils.createNumber("4.9e-324d");
            // Valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOverflowDouble() {
        try {
            NumberUtils.createNumber("1.7976931348623157e309d");
            // Valid (returns BigDecimal)
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndUnderflowDouble() {
        try {
            NumberUtils.createNumber("4.9e-325d");
            // Valid (returns BigDecimal)
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLargeInteger() {
        try {
            NumberUtils.createNumber("12345678901234567890L");
            // Valid (returns BigInteger)
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeLargeInteger() {
        try {
            NumberUtils.createNumber("-12345678901234567890L");
            // Valid (returns BigInteger)
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHex() {
        try {
            NumberUtils.createNumber("0xFFL");
            // Invalid, should throw
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOctal() {
        try {
            NumberUtils.createNumber("0123L");
            // Valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHex() {
        try {
            NumberUtils.createNumber("-0xFFL");
            // Invalid
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeOctal() {
        try {
            NumberUtils.createNumber("-0123L");
            // Valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexFloat() {
        try {
            NumberUtils.createNumber("0xFFf");
            // Invalid
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexDouble() {
        try {
            NumberUtils.createNumber("0xFFd");
            // Invalid
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOctalFloat() {
        try {
            NumberUtils.createNumber("0123f");
            // Valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOctalDouble() {
        try {
            NumberUtils.createNumber("0123d");
            // Valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeOctalFloat() {
        try {
            NumberUtils.createNumber("-0123f");
            // Valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeOctalDouble() {
        try {
            NumberUtils.createNumber("-0123d");
            // Valid
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexFloat() {
        try {
            NumberUtils.createNumber("-0xFFf");
            // Invalid
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexDouble() {
        try {
            NumberUtils.createNumber("-0xFFd");
            // Invalid
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDecimalWithLeadingZero() {
        try {
            NumberUtils.createNumber("01.5f");
            // Valid? "01.5f" -> numeric = "01.5", createFloat("01.5") returns 1.5f
            Number result = NumberUtils.createNumber("01.5f");
            assertEquals(Float.valueOf(1.5f), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponentWithLeadingZero() {
        try {
            NumberUtils.createNumber("01e2f");
            // Valid? "01e2f" -> numeric = "01e2", createFloat("01e2") returns 100.0f
            Number result = NumberUtils.createNumber("01e2f");
            assertEquals(Float.valueOf(100.0f), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDecimalWithLeadingZero() {
        try {
            NumberUtils.createNumber("-01.5f");
            Number result = NumberUtils.createNumber("-01.5f");
            assertEquals(Float.valueOf(-1.5f), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponentWithLeadingZero() {
        try {
            NumberUtils.createNumber("-01e2f");
            Number result = NumberUtils.createNumber("-01e2f");
            assertEquals(Float.valueOf(-100.0f), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWithLeadingZero() {
        try {
            NumberUtils.createNumber("0x0FF");
            // Valid hex
            Number result = NumberUtils.createNumber("0x0FF");
            assertEquals(Integer.valueOf(255), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexWithLeadingZero() {
        try {
            NumberUtils.createNumber("-0x0FF");
            Number result = NumberUtils.createNumber("-0x0FF");
            assertEquals(Integer.valueOf(-255), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOctalWithLeadingZero() {
        try {
            NumberUtils.createNumber("00123");
            // Octal 0123 = 83, but 00123 is also octal? Actually leading zeros are ignored, so "00123" is octal 123? No, Integer.decode("00123") returns 83 (octal). So result should be Integer 83.
            Number result = NumberUtils.createNumber("00123");
            assertEquals(Integer.valueOf(83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeOctalWithLeadingZero() {
        try {
            NumberUtils.createNumber("-00123");
            Number result = NumberUtils.createNumber("-00123");
            assertEquals(Integer.valueOf(-83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWithManyDigits() {
        try {
            NumberUtils.createNumber("0x1234567890ABCDEF");
            Number result = NumberUtils.createNumber("0x1234567890ABCDEF");
            assertEquals(Long.valueOf(0x1234567890ABCDEFL), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWithMoreThan16Digits() {
        try {
            NumberUtils.createNumber("0x1234567890ABCDEF1234567890ABCDEF");
            Number result = NumberUtils.createNumber("0x1234567890ABCDEF1234567890ABCDEF");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("1234567890ABCDEF1234567890ABCDEF", 16), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWith8To16Digits() {
        try {
            NumberUtils.createNumber("0x1234567890AB");
            Number result = NumberUtils.createNumber("0x1234567890AB");
            assertEquals(Long.valueOf(0x1234567890ABL), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWithLessThan9Digits() {
        try {
            NumberUtils.createNumber("0x12345678");
            Number result = NumberUtils.createNumber("0x12345678");
            assertEquals(Integer.valueOf(0x12345678), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWith8Digits() {
        try {
            NumberUtils.createNumber("0xFFFFFFFF");
            Number result = NumberUtils.createNumber("0xFFFFFFFF");
            assertEquals(Long.valueOf(0xFFFFFFFFL), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWith9Digits() {
        try {
            NumberUtils.createNumber("0x100000000");
            Number result = NumberUtils.createNumber("0x100000000");
            assertEquals(Long.valueOf(0x100000000L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWith16Digits() {
        try {
            NumberUtils.createNumber("0xFFFFFFFFFFFFFFFF");
            Number result = NumberUtils.createNumber("0xFFFFFFFFFFFFFFFF");
            assertEquals(Long.valueOf(0xFFFFFFFFFFFFFFFFL), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWith17Digits() {
        try {
            NumberUtils.createNumber("0x10000000000000000");
            Number result = NumberUtils.createNumber("0x10000000000000000");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("10000000000000000", 16), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexWith8Digits() {
        try {
            NumberUtils.createNumber("-0xFFFFFFFF");
            Number result = NumberUtils.createNumber("-0xFFFFFFFF");
            assertEquals(Long.valueOf(-0xFFFFFFFFL), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexWith9Digits() {
        try {
            NumberUtils.createNumber("-0x100000000");
            Number result = NumberUtils.createNumber("-0x100000000");
            assertEquals(Long.valueOf(-0x100000000L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexWith16Digits() {
        try {
            NumberUtils.createNumber("-0xFFFFFFFFFFFFFFFF");
            Number result = NumberUtils.createNumber("-0xFFFFFFFFFFFFFFFF");
            assertEquals(Long.valueOf(-0xFFFFFFFFFFFFFFFFL), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexWith17Digits() {
        try {
            NumberUtils.createNumber("-0x10000000000000000");
            Number result = NumberUtils.createNumber("-0x10000000000000000");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-10000000000000000", 16), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashHex() {
        try {
            NumberUtils.createNumber("#FF");
            Number result = NumberUtils.createNumber("#FF");
            assertEquals(Integer.valueOf(255), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashHex() {
        try {
            NumberUtils.createNumber("-#FF");
            Number result = NumberUtils.createNumber("-#FF");
            assertEquals(Integer.valueOf(-255), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashHexWithManyDigits() {
        try {
            NumberUtils.createNumber("#1234567890ABCDEF");
            Number result = NumberUtils.createNumber("#1234567890ABCDEF");
            assertEquals(Long.valueOf(0x1234567890ABCDEFL), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashHexWithMoreThan16Digits() {
        try {
            NumberUtils.createNumber("#1234567890ABCDEF1234567890ABCDEF");
            Number result = NumberUtils.createNumber("#1234567890ABCDEF1234567890ABCDEF");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("1234567890ABCDEF1234567890ABCDEF", 16), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashHexWithManyDigits() {
        try {
            NumberUtils.createNumber("-#1234567890ABCDEF");
            Number result = NumberUtils.createNumber("-#1234567890ABCDEF");
            assertEquals(Long.valueOf(-0x1234567890ABCDEFL), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashHexWithMoreThan16Digits() {
        try {
            NumberUtils.createNumber("-#1234567890ABCDEF1234567890ABCDEF");
            Number result = NumberUtils.createNumber("-#1234567890ABCDEF1234567890ABCDEF");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-1234567890ABCDEF1234567890ABCDEF", 16), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashHexWith8Digits() {
        try {
            NumberUtils.createNumber("#FFFFFFFF");
            Number result = NumberUtils.createNumber("#FFFFFFFF");
            assertEquals(Long.valueOf(0xFFFFFFFFL), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashHexWith9Digits() {
        try {
            NumberUtils.createNumber("#100000000");
            Number result = NumberUtils.createNumber("#100000000");
            assertEquals(Long.valueOf(0x100000000L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashHexWith16Digits() {
        try {
            NumberUtils.createNumber("#FFFFFFFFFFFFFFFF");
            Number result = NumberUtils.createNumber("#FFFFFFFFFFFFFFFF");
            assertEquals(Long.valueOf(0xFFFFFFFFFFFFFFFFL), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashHexWith17Digits() {
        try {
            NumberUtils.createNumber("#10000000000000000");
            Number result = NumberUtils.createNumber("#10000000000000000");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("10000000000000000", 16), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashHexWith8Digits() {
        try {
            NumberUtils.createNumber("-#FFFFFFFF");
            Number result = NumberUtils.createNumber("-#FFFFFFFF");
            assertEquals(Long.valueOf(-0xFFFFFFFFL), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashHexWith9Digits() {
        try {
            NumberUtils.createNumber("-#100000000");
            Number result = NumberUtils.createNumber("-#100000000");
            assertEquals(Long.valueOf(-0x100000000L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashHexWith16Digits() {
        try {
            NumberUtils.createNumber("-#FFFFFFFFFFFFFFFF");
            Number result = NumberUtils.createNumber("-#FFFFFFFFFFFFFFFF");
            assertEquals(Long.valueOf(-0xFFFFFFFFFFFFFFFFL), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashHexWith17Digits() {
        try {
            NumberUtils.createNumber("-#10000000000000000");
            Number result = NumberUtils.createNumber("-#10000000000000000");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-10000000000000000", 16), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWithLeadingZeros() {
        try {
            NumberUtils.createNumber("0x00000000000000000000000000000001");
            Number result = NumberUtils.createNumber("0x00000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE, result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexWithLeadingZeros() {
        try {
            NumberUtils.createNumber("-0x00000000000000000000000000000001");
            Number result = NumberUtils.createNumber("-0x00000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE.negate(), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOctalWithLeadingZeros() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeOctalWithLeadingZeros() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(-83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDecimalWithLeadingZeros() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDecimalWithLeadingZeros() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponentWithLeadingZeros() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponentWithLeadingZeros() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(-12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndFloatWithLeadingZeros() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeFloatWithLeadingZeros() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDoubleWithLeadingZeros() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000123.5d");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000123.5d");
            assertTrue(result instanceof Double);
            assertEquals(123.5, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDoubleWithLeadingZeros() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000123.5d");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000123.5d");
            assertTrue(result instanceof Double);
            assertEquals(-123.5, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLongWithLeadingZeros() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeLongWithLeadingZeros() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(-83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigIntegerWithLeadingZeros() {
        try {
            NumberUtils.createNumber("000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigIntegerWithLeadingZeros() {
        try {
            NumberUtils.createNumber("-000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("-000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZeros() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZeros() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("-1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("0x0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("0x0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE, result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("-0x0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("-0x0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE.negate(), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("#0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("#0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE, result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("-#0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("-#0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE.negate(), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOctalWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeOctalWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(-83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDecimalWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDecimalWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponentWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponentWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(-12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndFloatWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeFloatWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDoubleWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5d");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5d");
            assertTrue(result instanceof Double);
            assertEquals(123.5, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDoubleWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5d");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5d");
            assertTrue(result instanceof Double);
            assertEquals(-123.5, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLongWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeLongWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(-83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigIntegerWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigIntegerWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigits() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("-1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0x0000000000000000000000000000000000000000000000000000000000000001L");
            // Invalid because hex with L qualifier
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0x0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("#0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-#0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOctalWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            // Valid
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeOctalWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(-83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDecimalWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDecimalWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponentWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2f");
            assertTrue(result instanceof Float);
            assertEquals(12300.0f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponentWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2f");
            assertTrue(result instanceof Float);
            assertEquals(-12300.0f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndFloatWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeFloatWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDoubleWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5d");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5d");
            assertTrue(result instanceof Double);
            assertEquals(123.5, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDoubleWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5d");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5d");
            assertTrue(result instanceof Double);
            assertEquals(-123.5, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLongWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeLongWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(-83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigIntegerWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            Number result = NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigIntegerWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            Number result = NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            // This will be Float because of 'f' qualifier, but may lose precision
            assertTrue(result instanceof Float);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            assertTrue(result instanceof Float);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsAndTypeQualifierD() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            assertTrue(result instanceof Double);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsAndTypeQualifierD() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            assertTrue(result instanceof Double);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("-1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigIntegerWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigIntegerWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLongWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeLongWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(-83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndFloatWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeFloatWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponentWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponentWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(-12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("0x0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("0x0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE, result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("-0x0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("-0x0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE.negate(), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("#0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("#0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE, result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("-#0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("-#0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE.negate(), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOctalWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeOctalWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(-83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDecimalWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDecimalWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponentWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponentWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(-12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndFloatWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeFloatWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDoubleWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDoubleWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLongWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeLongWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(-83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigIntegerWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigIntegerWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsNoQualifier() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("-1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0x0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0x0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("#0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-#0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOctalWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeOctalWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(-83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDecimalWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDecimalWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponentWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2f");
            assertTrue(result instanceof Float);
            assertEquals(12300.0f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponentWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2f");
            assertTrue(result instanceof Float);
            assertEquals(-12300.0f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndFloatWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeFloatWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDoubleWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5d");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5d");
            assertTrue(result instanceof Double);
            assertEquals(123.5, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDoubleWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5d");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5d");
            assertTrue(result instanceof Double);
            assertEquals(-123.5, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLongWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeLongWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(-83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigIntegerWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            Number result = NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigIntegerWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            Number result = NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            assertTrue(result instanceof Float);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            assertTrue(result instanceof Float);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifierD() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            assertTrue(result instanceof Double);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsNoQualifierAndTypeQualifierD() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            assertTrue(result instanceof Double);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("-1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(-83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(-12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0x0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("0x0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE, result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0x0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("-0x0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE.negate(), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("#0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("#0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE, result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-#0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("-#0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE.negate(), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOctalWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeOctalWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(-83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(-12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDoubleWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDoubleWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(-83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("-1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0x0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0x0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("#0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-#0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOctalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeOctalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(-83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2f");
            assertTrue(result instanceof Float);
            assertEquals(12300.0f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2f");
            assertTrue(result instanceof Float);
            assertEquals(-12300.0f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDoubleWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5d");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5d");
            assertTrue(result instanceof Double);
            assertEquals(123.5, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDoubleWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5d");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5d");
            assertTrue(result instanceof Double);
            assertEquals(-123.5, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(-83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            Number result = NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            Number result = NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            assertTrue(result instanceof Float);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            assertTrue(result instanceof Float);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifierD() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            assertTrue(result instanceof Double);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierAndTypeQualifierD() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            assertTrue(result instanceof Double);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("-1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(-83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(-12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0x0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("0x0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE, result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0x0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("-0x0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE.negate(), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("#0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("#0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE, result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-#0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("-#0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE.negate(), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOctalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeOctalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(-83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(-12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDoubleWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDoubleWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(-83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("-1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0x0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0x0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("#0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-#0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOctalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeOctalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(-83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2f");
            assertTrue(result instanceof Float);
            assertEquals(12300.0f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2f");
            assertTrue(result instanceof Float);
            assertEquals(-12300.0f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDoubleWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5d");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5d");
            assertTrue(result instanceof Double);
            assertEquals(123.5, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDoubleWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5d");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5d");
            assertTrue(result instanceof Double);
            assertEquals(-123.5, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(-83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            Number result = NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            Number result = NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            assertTrue(result instanceof Float);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            assertTrue(result instanceof Float);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifierD() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            assertTrue(result instanceof Double);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierAndTypeQualifierD() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            assertTrue(result instanceof Double);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("-1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(-83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(-12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0x0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("0x0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE, result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0x0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("-0x0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE.negate(), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("#0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("#0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE, result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-#0000000000000000000000000000000000000000000000000000000000000001");
            Number result = NumberUtils.createNumber("-#0000000000000000000000000000000000000000000000000000000000000001");
            assertTrue(result instanceof BigInteger);
            assertEquals(BigInteger.ONE.negate(), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOctalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeOctalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(-83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2");
            assertTrue(result instanceof Double);
            assertEquals(-12300.0, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDoubleWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDoubleWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123");
            assertEquals(Integer.valueOf(-83), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            Number result = NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("-1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHexWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0x0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHexWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0x0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndHashWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("#0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeHashWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-#0000000000000000000000000000000000000000000000000000000000000001L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndOctalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeOctalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(-83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123e2f");
            assertTrue(result instanceof Float);
            assertEquals(12300.0f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeExponentWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123e2f");
            assertTrue(result instanceof Float);
            assertEquals(-12300.0f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeFloatWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5f");
            assertTrue(result instanceof Float);
            assertEquals(-123.5f, result.floatValue(), 0.0f);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndDoubleWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5d");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123.5d");
            assertTrue(result instanceof Double);
            assertEquals(123.5, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeDoubleWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5d");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123.5d");
            assertTrue(result instanceof Double);
            assertEquals(-123.5, result.doubleValue(), 0.0);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeLongWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            Number result = NumberUtils.createNumber("-0000000000000000000000000000000000000000000000000000000000000000123L");
            assertEquals(Long.valueOf(-83L), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            Number result = NumberUtils.createNumber("000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigIntegerWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            Number result = NumberUtils.createNumber("-000000000000000000000000000000000000000000000000000000000000000012345678901234567890L");
            assertTrue(result instanceof BigInteger);
            assertEquals(new BigInteger("-12345678901234567890"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            assertTrue(result instanceof Float);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifier() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789f");
            assertTrue(result instanceof Float);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifierD() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            assertTrue(result instanceof Double);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndNegativeBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierAndTypeQualifierD() {
        try {
            NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            Number result = NumberUtils.createNumber("-00000000000000000000000000000000000000000000000000000000000000001234567890.123456789d");
            assertTrue(result instanceof Double);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberPrecisionLossWithTypeQualifierAndBigDecimalWithLeadingZerosAndManyDigitsNoQualifierNoQualifierNoQualifierNoQualifierNoQualifier() {
        try {
            NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            Number result = NumberUtils.createNumber("00000000000000000000000000000000000000000000000000000000000000001234567890.123456789");
            assertTrue(result instanceof BigDecimal);
            assertEquals(new BigDecimal("1234567890.123456789"), result);
        } catch (NumberFormatException e) {
            fail("Unexpected exception");
        }
    }