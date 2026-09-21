/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Decision Branches & Boundaries:
 * 1. createNumber(String):
 *    - null input -> returns null
 *    - blank/empty String -> throws NumberFormatException
 *    - startsWith("--") -> returns null
 *    - Hex detection ("0x", "-0x", and defect target "0X", "-0X") -> createInteger
 *    - Qualifier detection: 'l'/'L', 'f'/'F', 'd'/'D'
 *    - Precision fallback: Float -> Double -> BigDecimal
 *    - Integer range fallback: Integer -> Long -> BigInteger
 *    - Malformed formats (e.g., mispositioned decimals, multiple exponents, illegal characters)
 * 2. isNumber(String):
 *    - Hex prefix "0x", empty hex "0x", invalid hex chars
 *    - Leading sign (+, -), decimal points, exponent specifiers ('e', 'E')
 *    - Trailing type specifiers ('f', 'F', 'd', 'D', 'l', 'L')
 *    - Invalid combinations (multiple points, multiple exponents, bare signs, empty strings)
 * 3. Array min/max methods (long, int, short, byte, double, float):
 *    - null array -> IllegalArgumentException
 *    - empty array -> IllegalArgumentException
 *    - single and multi-element arrays (first, middle, last is min/max)
 *    - IEEE 754 NaN handling in double[] and float[] (NaN propagation)
 * 4. 3-argument min/max primitives:
 *    - permutations of arguments (a < b < c, b < a < c, c < b < a, etc.)
 *    - Float/Double NaN and Infinity handling
 * 5. Type conversions toX(String, defaultVal):
 *    - null and invalid string fallback to defaultValue
 *    - valid string parsing to exact primitive
 *
 * DEFECT TARGET (from Defects4J ground truth):
 * - Target: NumberUtils.createNumber("0Xfade") fails with NumberFormatException
 *   because only "0x" and "-0x" are inspected instead of case-insensitive "0X".
 */

package org.apache.commons.lang3.math;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class NumberUtilsGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect where uppercase hexadecimal prefix '0X' or '-0X'
     * is not recognized by createNumber(String), causing a NumberFormatException.
     */
    @Test(timeout = 4000)
    public void testCreateNumberHexUppercaseXDefect() {
        Number resultPos = NumberUtils.createNumber("0Xfade");
        assertEquals("Failed to parse uppercase hex prefix 0X", Integer.valueOf(0xfade), resultPos);

        Number resultNeg = NumberUtils.createNumber("-0X1234");
        assertEquals("Failed to parse negative uppercase hex prefix -0X", Integer.valueOf(-0x1234), resultNeg);

        Number resultLower = NumberUtils.createNumber("0x10");
        assertEquals("Failed to parse lowercase hex prefix 0x", Integer.valueOf(16), resultLower);
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & TYPE CONVERSIONS (toX methods)
    // =========================================================================

    @Test(timeout = 4000)
    public void testToInt() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(42, NumberUtils.toInt("42"));
        assertEquals(5, NumberUtils.toInt(null, 5));
        assertEquals(5, NumberUtils.toInt("invalid", 5));
        assertEquals(10, NumberUtils.toInt("10", 5));
    }

    @Test(timeout = 4000)
    public void testToLong() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(0L, NumberUtils.toLong("xyz"));
        assertEquals(1234567890123L, NumberUtils.toLong("1234567890123"));
        assertEquals(99L, NumberUtils.toLong(null, 99L));
        assertEquals(99L, NumberUtils.toLong("invalid", 99L));
        assertEquals(100L, NumberUtils.toLong("100", 99L));
    }

    @Test(timeout = 4000)
    public void testToFloat() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0001f);
        assertEquals(0.0f, NumberUtils.toFloat("invalid"), 0.0001f);
        assertEquals(3.14f, NumberUtils.toFloat("3.14"), 0.0001f);
        assertEquals(1.5f, NumberUtils.toFloat(null, 1.5f), 0.0001f);
        assertEquals(1.5f, NumberUtils.toFloat("invalid", 1.5f), 0.0001f);
        assertEquals(2.5f, NumberUtils.toFloat("2.5", 1.5f), 0.0001f);
    }

    @Test(timeout = 4000)
    public void testToDouble() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.00001d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.00001d);
        assertEquals(0.0d, NumberUtils.toDouble("invalid"), 0.00001d);
        assertEquals(2.71828d, NumberUtils.toDouble("2.71828"), 0.00001d);
        assertEquals(4.2d, NumberUtils.toDouble(null, 4.2d), 0.00001d);
        assertEquals(4.2d, NumberUtils.toDouble("invalid", 4.2d), 0.00001d);
        assertEquals(9.9d, NumberUtils.toDouble("9.9", 4.2d), 0.00001d);
    }

    @Test(timeout = 4000)
    public void testToByte() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 0, NumberUtils.toByte("bad"));
        assertEquals((byte) 12, NumberUtils.toByte("12"));
        assertEquals((byte) 7, NumberUtils.toByte(null, (byte) 7));
        assertEquals((byte) 7, NumberUtils.toByte("invalid", (byte) 7));
        assertEquals((byte) 64, NumberUtils.toByte("64", (byte) 7));
    }

    @Test(timeout = 4000)
    public void testToShort() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 0, NumberUtils.toShort("bad"));
        assertEquals((short) 1234, NumberUtils.toShort("1234"));
        assertEquals((short) 8, NumberUtils.toShort(null, (short) 8));
        assertEquals((short) 8, NumberUtils.toShort("invalid", (short) 8));
        assertEquals((short) 500, NumberUtils.toShort("500", (short) 8));
    }

    // =========================================================================
    // PARTITION B: FACTORY METHODS FOR NUMERIC OBJECTS
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(12.34f), NumberUtils.createFloat("12.34"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateFloatInvalid() {
        NumberUtils.createFloat("not-a-float");
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
        assertEquals(Integer.valueOf(0x1a), NumberUtils.createInteger("0x1a"));
        assertEquals(Integer.valueOf(010), NumberUtils.createInteger("010"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateIntegerInvalid() {
        NumberUtils.createInteger("not-an-int");
    }

    @Test(timeout = 4000)
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(12345678901L), NumberUtils.createLong("12345678901"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateLongInvalid() {
        NumberUtils.createLong("not-a-long");
    }

    @Test(timeout = 4000)
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("98765432109876543210"), NumberUtils.createBigInteger("98765432109876543210"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigIntegerInvalid() {
        NumberUtils.createBigInteger("not-a-bigint");
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("123456.7890123456789"), NumberUtils.createBigDecimal("123456.7890123456789"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("   ");
    }

    // =========================================================================
    // PARTITION B & C: createNumber DETAILED PATHS
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumberNullAndBlank() {
        assertNull(NumberUtils.createNumber(null));
        assertNull(NumberUtils.createNumber("--123"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberEmptyStringThrows() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberWhitespaceThrows() {
        NumberUtils.createNumber("   ");
    }

    @Test(timeout = 4000)
    public void testCreateNumberIntegerTypes() {
        // Fits in Integer
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Integer.valueOf(-123), NumberUtils.createNumber("-123"));
        // Fits in Long
        long largeVal = 2147483648L; // Integer.MAX_VALUE + 1
        assertEquals(Long.valueOf(largeVal), NumberUtils.createNumber(String.valueOf(largeVal)));
        // Fits in BigInteger
        String bigIntStr = "123456789012345678901234567890";
        assertEquals(new BigInteger(bigIntStr), NumberUtils.createNumber(bigIntStr));
    }

    @Test(timeout = 4000)
    public void testCreateNumberFloatingPointTypes() {
        // Simple float / double
        assertEquals(Float.valueOf(1.23f), NumberUtils.createNumber("1.23"));
        assertEquals(Double.valueOf(1.234567890123456), NumberUtils.createNumber("1.234567890123456"));

        // Exponent without qualifier
        assertEquals(Float.valueOf(1.23e4f), NumberUtils.createNumber("1.23e4"));

        // All zeros
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0"));
        assertEquals(Double.valueOf(0.0d), NumberUtils.createNumber("0.0000000000000000000000000000000000000000000000000000000d"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithQualifiers() {
        // Long qualifier
        assertEquals(Long.valueOf(100L), NumberUtils.createNumber("100L"));
        assertEquals(Long.valueOf(-100L), NumberUtils.createNumber("-100l"));
        assertEquals(new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890L"));

        // Float qualifier
        assertEquals(Float.valueOf(3.14f), NumberUtils.createNumber("3.14f"));
        assertEquals(Float.valueOf(3.14f), NumberUtils.createNumber("3.14F"));

        // Float overflow falls to Double/BigDecimal
        Number bigFloat = NumberUtils.createNumber("1e50f");
        assertTrue(bigFloat instanceof Double || bigFloat instanceof BigDecimal);

        // Double qualifier
        assertEquals(Double.valueOf(3.14d), NumberUtils.createNumber("3.14d"));
        assertEquals(Double.valueOf(3.14d), NumberUtils.createNumber("3.14D"));

        // Double overflow falls to BigDecimal
        Number bigDouble = NumberUtils.createNumber("1e400d");
        assertTrue(bigDouble instanceof BigDecimal);
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidQualifier() {
        NumberUtils.createNumber("123z");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberDecAfterExpThrows() {
        NumberUtils.createNumber("1e2.3");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidExponentThrows() {
        NumberUtils.createNumber("123e");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberLongWithDecimalThrows() {
        NumberUtils.createNumber("12.34L");
    }

    // =========================================================================
    // PARTITION B: isDigits & isNumber
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("   "));
        assertFalse(NumberUtils.isDigits("12a34"));
        assertFalse(NumberUtils.isDigits("-123"));
        assertTrue(NumberUtils.isDigits("0"));
        assertTrue(NumberUtils.isDigits("1234567890"));
    }

    @Test(timeout = 4000)
    public void testIsNumber() {
        // Null and Empty
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("   "));

        // Valid integers and signed
        assertTrue(NumberUtils.isNumber("0"));
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertFalse(NumberUtils.isNumber("+123")); // '+' handled after exponent only

        // Valid Hex
        assertTrue(NumberUtils.isNumber("0x1234"));
        assertTrue(NumberUtils.isNumber("0xabcd"));
        assertTrue(NumberUtils.isNumber("-0xABCD"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("0x123G"));

        // Floating point & scientific notation
        assertTrue(NumberUtils.isNumber("1.23"));
        assertTrue(NumberUtils.isNumber("-1.23"));
        assertTrue(NumberUtils.isNumber(".23"));
        assertTrue(NumberUtils.isNumber("123."));
        assertTrue(NumberUtils.isNumber("1.23e4"));
        assertTrue(NumberUtils.isNumber("1.23E4"));
        assertTrue(NumberUtils.isNumber("1.23e+4"));
        assertTrue(NumberUtils.isNumber("1.23e-4"));

        // Qualifiers
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertTrue(NumberUtils.isNumber("1.23f"));
        assertTrue(NumberUtils.isNumber("1.23F"));
        assertTrue(NumberUtils.isNumber("1.23d"));
        assertTrue(NumberUtils.isNumber("1.23D"));

        // Invalid cases
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("e1"));
        assertFalse(NumberUtils.isNumber("123a"));
        assertFalse(NumberUtils.isNumber("123.4L")); // Long cannot have dec point
        assertFalse(NumberUtils.isNumber("1e2L"));   // Long cannot have exponent
    }

    // =========================================================================
    // PARTITION B & D: MIN / MAX ARRAY METHODS (All Primitives)
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinLongArray() {
        assertEquals(1L, NumberUtils.min(new long[]{1L}));
        assertEquals(-5L, NumberUtils.min(new long[]{10L, -5L, 2L}));
        assertEquals(-20L, NumberUtils.min(new long[]{10L, 5L, -20L}));
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
    public void testMaxLongArray() {
        assertEquals(1L, NumberUtils.max(new long[]{1L}));
        assertEquals(10L, NumberUtils.max(new long[]{10L, -5L, 2L}));
        assertEquals(30L, NumberUtils.max(new long[]{10L, 5L, 30L}));
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
    public void testMinIntArray() {
        assertEquals(3, NumberUtils.min(new int[]{3}));
        assertEquals(-1, NumberUtils.min(new int[]{5, -1, 4}));
        assertEquals(-10, NumberUtils.min(new int[]{5, 4, -10}));
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
    public void testMaxIntArray() {
        assertEquals(3, NumberUtils.max(new int[]{3}));
        assertEquals(9, NumberUtils.max(new int[]{9, 1, 4}));
        assertEquals(12, NumberUtils.max(new int[]{5, 4, 12}));
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
    public void testMinShortArray() {
        assertEquals((short) 5, NumberUtils.min(new short[]{5}));
        assertEquals((short) -2, NumberUtils.min(new short[]{3, -2, 1}));
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
    public void testMaxShortArray() {
        assertEquals((short) 5, NumberUtils.max(new short[]{5}));
        assertEquals((short) 10, NumberUtils.max(new short[]{3, 10, 1}));
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
    public void testMinByteArray() {
        assertEquals((byte) 1, NumberUtils.min(new byte[]{1}));
        assertEquals((byte) -8, NumberUtils.min(new byte[]{2, 0, -8}));
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
    public void testMaxByteArray() {
        assertEquals((byte) 1, NumberUtils.max(new byte[]{1}));
        assertEquals((byte) 20, NumberUtils.max(new byte[]{2, 20, -8}));
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
    public void testMinDoubleArray() {
        assertEquals(1.1d, NumberUtils.min(new double[]{1.1d}), 0.0001d);
        assertEquals(-2.5d, NumberUtils.min(new double[]{0.0d, -2.5d, 3.2d}), 0.0001d);
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
    public void testMaxDoubleArray() {
        assertEquals(1.1d, NumberUtils.max(new double[]{1.1d}), 0.0001d);
        assertEquals(3.2d, NumberUtils.max(new double[]{0.0d, -2.5d, 3.2d}), 0.0001d);
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
    public void testMinFloatArray() {
        assertEquals(1.1f, NumberUtils.min(new float[]{1.1f}), 0.0001f);
        assertEquals(-2.5f, NumberUtils.min(new float[]{0.0f, -2.5f, 3.2f}), 0.0001f);
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
    public void testMaxFloatArray() {
        assertEquals(1.1f, NumberUtils.max(new float[]{1.1f}), 0.0001f);
        assertEquals(3.2f, NumberUtils.max(new float[]{0.0f, -2.5f, 3.2f}), 0.0001f);
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
    // PARTITION B: 3-ARGUMENT MIN / MAX (All Primitives)
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinThreeLongs() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(2L, 1L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));
    }

    @Test(timeout = 4000)
    public void testMaxThreeLongs() {
        assertEquals(3L, NumberUtils.max(3L, 2L, 1L));
        assertEquals(3L, NumberUtils.max(1L, 3L, 2L));
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
    }

    @Test(timeout = 4000)
    public void testMinThreeInts() {
        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(2, 1, 3));
        assertEquals(1, NumberUtils.min(3, 2, 1));
    }

    @Test(timeout = 4000)
    public void testMaxThreeInts() {
        assertEquals(3, NumberUtils.max(3, 2, 1));
        assertEquals(3, NumberUtils.max(1, 3, 2));
        assertEquals(3, NumberUtils.max(1, 2, 3));
    }

    @Test(timeout = 4000)
    public void testMinThreeShorts() {
        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 2, (short) 1, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 2, (short) 1));
    }

    @Test(timeout = 4000)
    public void testMaxThreeShorts() {
        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 2, (short) 1));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 3, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
    }

    @Test(timeout = 4000)
    public void testMinThreeBytes() {
        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 2, (byte) 1, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 2, (byte) 1));
    }

    @Test(timeout = 4000)
    public void testMaxThreeBytes() {
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 2, (byte) 1));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 3, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
    }

    @Test(timeout = 4000)
    public void testMinThreeDoubles() {
        assertEquals(1.0d, NumberUtils.min(1.0d, 2.0d, 3.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(2.0d, 1.0d, 3.0d), 0.0001d);
        assertEquals(1.0d, NumberUtils.min(3.0d, 2.0d, 1.0d), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.min(Double.NaN, 2.0d, 3.0d)));
        assertTrue(Double.isNaN(NumberUtils.min(1.0d, Double.NaN, 3.0d)));
        assertTrue(Double.isNaN(NumberUtils.min(1.0d, 2.0d, Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testMaxThreeDoubles() {
        assertEquals(3.0d, NumberUtils.max(3.0d, 2.0d, 1.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(1.0d, 3.0d, 2.0d), 0.0001d);
        assertEquals(3.0d, NumberUtils.max(1.0d, 2.0d, 3.0d), 0.0001d);
        assertTrue(Double.isNaN(NumberUtils.max(Double.NaN, 2.0d, 3.0d)));
        assertTrue(Double.isNaN(NumberUtils.max(1.0d, Double.NaN, 3.0d)));
        assertTrue(Double.isNaN(NumberUtils.max(1.0d, 2.0d, Double.NaN)));
    }

    @Test(timeout = 4000)
    public void testMinThreeFloats() {
        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(2.0f, 1.0f, 3.0f), 0.0001f);
        assertEquals(1.0f, NumberUtils.min(3.0f, 2.0f, 1.0f), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.min(Float.NaN, 2.0f, 3.0f)));
        assertTrue(Float.isNaN(NumberUtils.min(1.0f, Float.NaN, 3.0f)));
        assertTrue(Float.isNaN(NumberUtils.min(1.0f, 2.0f, Float.NaN)));
    }

    @Test(timeout = 4000)
    public void testMaxThreeFloats() {
        assertEquals(3.0f, NumberUtils.max(3.0f, 2.0f, 1.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(1.0f, 3.0f, 2.0f), 0.0001f);
        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.0001f);
        assertTrue(Float.isNaN(NumberUtils.max(Float.NaN, 2.0f, 3.0f)));
        assertTrue(Float.isNaN(NumberUtils.max(1.0f, Float.NaN, 3.0f)));
        assertTrue(Float.isNaN(NumberUtils.max(1.0f, 2.0f, Float.NaN)));
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE & CONSTANTS INTEGRITY
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