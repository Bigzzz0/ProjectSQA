package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Targets:
 * - All conversion methods (toInt, toLong, toFloat, toDouble, toByte, toShort): null, empty, valid, invalid, defaults, boundaries.
 * - createNumber: null, blank, "--", hex (0x, -0x), type qualifiers (L,l,F,f,D,d), integer/long/bigint paths, float/double/bigdecimal paths,
 *   scientific notation, decimal with trailing dot (BUG: "2." should be valid), decimals without digits (e.g., ".5").
 * - createInteger/createLong/createFloat/createDouble/createBigInteger/createBigDecimal: null, blank, valid/invalid.
 * - isDigits: null, empty, all digits, mixed.
 * - isNumber: null, empty, valid/invalid strings, including trailing dot (BUG: "2." should be true).
 * - min/max array methods: null, empty, single/multiple, NaN propagation for float/double.
 * - min/max three-value methods: all integer/float/double variants, NaN handling.
 *
 * Defect-specific tests:
 *   testCreateNumber_TrailingDot -> must NOT throw NumberFormatException for "2."; should return Double.
 *   testIsNumber_TrailingDot    -> must return true for "2.".
 */
public class NumberUtilsDeepseekTest {

    // =================== Partition A: Core conversion methods (toInt, toLong, etc.) ===================

    @Test(timeout = 4000)
    public void testToInt() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(1, NumberUtils.toInt("1"));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(5, NumberUtils.toInt("5", 0));
        assertEquals(5, NumberUtils.toInt("5", 10));
        assertEquals(10, NumberUtils.toInt(null, 10));
        assertEquals(10, NumberUtils.toInt("", 10));
        assertEquals(Integer.MAX_VALUE, NumberUtils.toInt(String.valueOf(Integer.MAX_VALUE)));
        assertEquals(Integer.MIN_VALUE, NumberUtils.toInt(String.valueOf(Integer.MIN_VALUE)));
    }

    @Test(timeout = 4000)
    public void testToLong() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(1L, NumberUtils.toLong("1"));
        assertEquals(0L, NumberUtils.toLong("xyz"));
        assertEquals(100L, NumberUtils.toLong("100", 0L));
        assertEquals(100L, NumberUtils.toLong("100", 50L));
        assertEquals(50L, NumberUtils.toLong(null, 50L));
        assertEquals(Long.MAX_VALUE, NumberUtils.toLong(String.valueOf(Long.MAX_VALUE)));
        assertEquals(Long.MIN_VALUE, NumberUtils.toLong(String.valueOf(Long.MIN_VALUE)));
    }

    @Test(timeout = 4000)
    public void testToFloat() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0f);
        assertEquals(2.5f, NumberUtils.toFloat("2.5", 0.0f), 0.0f);
        assertEquals(2.5f, NumberUtils.toFloat("2.5", 1.0f), 0.0f);
        assertEquals(1.0f, NumberUtils.toFloat(null, 1.0f), 0.0f);
        assertEquals(Float.MAX_VALUE, NumberUtils.toFloat(String.valueOf(Float.MAX_VALUE)), 0.0f);
        assertEquals(Float.NaN, NumberUtils.toFloat("NaN"), 0.0f);
    }

    @Test(timeout = 4000)
    public void testToDouble() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0d);
        assertEquals(2.5d, NumberUtils.toDouble("2.5", 0.0d), 0.0d);
        assertEquals(2.5d, NumberUtils.toDouble("2.5", 1.0d), 0.0d);
        assertEquals(1.0d, NumberUtils.toDouble(null, 1.0d), 0.0d);
        assertEquals(Double.MAX_VALUE, NumberUtils.toDouble(String.valueOf(Double.MAX_VALUE)), 0.0d);
        assertEquals(Double.NaN, NumberUtils.toDouble("NaN"), 0.0d);
    }

    @Test(timeout = 4000)
    public void testToByte() {
        assertEquals((byte)0, NumberUtils.toByte(null));
        assertEquals((byte)0, NumberUtils.toByte(""));
        assertEquals((byte)1, NumberUtils.toByte("1"));
        assertEquals((byte)0, NumberUtils.toByte("xyz"));
        assertEquals((byte)10, NumberUtils.toByte("10", (byte)0));
        assertEquals((byte)10, NumberUtils.toByte("10", (byte)5));
        assertEquals((byte)5, NumberUtils.toByte(null, (byte)5));
        assertEquals(Byte.MAX_VALUE, NumberUtils.toByte(String.valueOf(Byte.MAX_VALUE)));
        assertEquals(Byte.MIN_VALUE, NumberUtils.toByte(String.valueOf(Byte.MIN_VALUE)));
    }

    @Test(timeout = 4000)
    public void testToShort() {
        assertEquals((short)0, NumberUtils.toShort(null));
        assertEquals((short)0, NumberUtils.toShort(""));
        assertEquals((short)1, NumberUtils.toShort("1"));
        assertEquals((short)0, NumberUtils.toShort("abc"));
        assertEquals((short)100, NumberUtils.toShort("100", (short)0));
        assertEquals((short)100, NumberUtils.toShort("100", (short)5));
        assertEquals((short)5, NumberUtils.toShort(null, (short)5));
        assertEquals(Short.MAX_VALUE, NumberUtils.toShort(String.valueOf(Short.MAX_VALUE)));
        assertEquals(Short.MIN_VALUE, NumberUtils.toShort(String.valueOf(Short.MIN_VALUE)));
    }

    // =================== Partition B: createNumber (core, boundary, defect) ===================

    @Test(timeout = 4000)
    public void testCreateNumber_Null() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_Blank() {
        NumberUtils.createNumber("  ");
    }

    @Test(timeout = 4000)
    public void testCreateNumber_DoubleDash() {
        assertNull(NumberUtils.createNumber("--123"));
    }

    @Test(timeout = 4000)
    public void testCreateNumber_Hex() {
        assertEquals(Integer.valueOf(0x1A), NumberUtils.createNumber("0x1A"));
        assertEquals(Integer.valueOf(-0x1A), NumberUtils.createNumber("-0x1A"));
    }

    @Test(timeout = 4000)
    public void testCreateNumber_Integer() {
        assertEquals(Integer.valueOf(42), NumberUtils.createNumber("42"));
        assertEquals(Integer.valueOf(-42), NumberUtils.createNumber("-42"));
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0"));
    }

    @Test(timeout = 4000)
    public void testCreateNumber_Long() {
        assertEquals(Long.valueOf(1234567890123L), NumberUtils.createNumber("1234567890123"));
        assertEquals(Long.valueOf(-123L), NumberUtils.createNumber("-123"));
    }

    @Test(timeout = 4000)
    public void testCreateNumber_BigInteger() {
        assertEquals(new java.math.BigInteger("99999999999999999999999999999"),
                NumberUtils.createNumber("99999999999999999999999999999"));
    }

    @Test(timeout = 4000)
    public void testCreateNumber_Float() {
        assertEquals(Float.valueOf(3.14f), NumberUtils.createNumber("3.14f"));
        assertEquals(Float.valueOf(3.14f), NumberUtils.createNumber("3.14F"));
        assertEquals(Float.valueOf(3.14f), NumberUtils.createNumber("3.14"));
    }

    @Test(timeout = 4000)
    public void testCreateNumber_Double() {
        assertEquals(Double.valueOf(3.14d), NumberUtils.createNumber("3.14d"));
        assertEquals(Double.valueOf(3.14d), NumberUtils.createNumber("3.14D"));
    }

    @Test(timeout = 4000)
    public void testCreateNumber_BigDecimal() {
        assertEquals(new java.math.BigDecimal("12345678901234567890.1234567890"),
                NumberUtils.createNumber("12345678901234567890.1234567890"));
    }

    @Test(timeout = 4000)
    public void testCreateNumber_WithExponent() {
        assertEquals(Double.valueOf(1e10), NumberUtils.createNumber("1e10"));
        assertEquals(Float.valueOf(1.5e-5f), NumberUtils.createNumber("1.5e-5"));
        assertEquals(Double.valueOf(2E+3), NumberUtils.createNumber("2E+3"));
    }

    @Test(timeout = 4000)
    public void testCreateNumber_TypeQualifierL() {
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123l"));
    }

    @Test(timeout = 4000)
    public void testCreateNumber_InvalidTrailingDot() {
        // BUG: "2." should be valid; currently throws NumberFormatException
        // Test that it does throw (defective behavior); we want to reveal the bug.
        // But since it's known to be a bug, we should test that it fails.
        // However, the requirement says: "write at least one dedicated @Test method that directly targets this specific failure condition.
        // The test MUST assert the expected correct behavior so that it reveals/triggers the bug on the defective version!"
        // That means we need to test the correct behavior and expect it to fail on the buggy version.
        // So we assert that createNumber("2.") returns a Double (2.0). This will fail on the buggy version.
        Number result = NumberUtils.createNumber("2.");
        assertNotNull("createNumber('2.') should not return null", result);
        assertTrue("createNumber('2.') should be a Double", result instanceof Double);
        assertEquals(2.0, ((Double) result).doubleValue(), 0.0);
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_FloatWithTwoDecimals() {
        // ".." is invalid
        NumberUtils.createNumber("1..2");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_InvalidTypeQualifier() {
        NumberUtils.createNumber("123x");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_ExponentWithNoDigits() {
        NumberUtils.createNumber("1e");
    }

    // =================== Partition C: createInteger, createLong, etc. ===================

    @Test(timeout = 4000)
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(0), NumberUtils.createInteger("0"));
        assertEquals(Integer.valueOf(255), NumberUtils.createInteger("255"));
        assertEquals(Integer.valueOf(-12), NumberUtils.createInteger("-12"));
        assertEquals(Integer.valueOf(0xFF), NumberUtils.createInteger("0xFF")); // hex
        assertEquals(Integer.valueOf(077), NumberUtils.createInteger("077")); // octal (Java treats as octal)
    }

    @Test(timeout = 4000)
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(0L), NumberUtils.createLong("0"));
        assertEquals(Long.valueOf(Long.MAX_VALUE), NumberUtils.createLong(String.valueOf(Long.MAX_VALUE)));
    }

    @Test(timeout = 4000)
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(1.0f), NumberUtils.createFloat("1.0"));
    }

    @Test(timeout = 4000)
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(2.5), NumberUtils.createDouble("2.5"));
    }

    @Test(timeout = 4000)
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new java.math.BigInteger("100000000000000000000000000000000000000000000"),
                NumberUtils.createBigInteger("100000000000000000000000000000000000000000000"));
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new java.math.BigDecimal("123.456"), NumberUtils.createBigDecimal("123.456"));
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateBigDecimal_Blank() {
        NumberUtils.createBigDecimal("   ");
    }

    // =================== Partition D: isDigits ===================

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertTrue(NumberUtils.isDigits("123"));
        assertTrue(NumberUtils.isDigits("0"));
        assertFalse(NumberUtils.isDigits("12a"));
    }

    // =================== Partition E: isNumber (including defect) ===================

    @Test(timeout = 4000)
    public void testIsNumber_Simple() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertTrue(NumberUtils.isNumber("1"));
        assertTrue(NumberUtils.isNumber("-1"));
        assertTrue(NumberUtils.isNumber("1.5"));
        assertTrue(NumberUtils.isNumber("1.5e10"));
        assertFalse(NumberUtils.isNumber("1..2"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e-"));
        assertFalse(NumberUtils.isNumber("1ea"));
    }

    @Test(timeout = 4000)
    public void testIsNumber_Hex() {
        assertTrue(NumberUtils.isNumber("0x1A"));
        assertTrue(NumberUtils.isNumber("-0x1A"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("0xG"));
    }

    @Test(timeout = 4000)
    public void testIsNumber_TypeQualifiers() {
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertTrue(NumberUtils.isNumber("1.5f"));
        assertTrue(NumberUtils.isNumber("1.5F"));
        assertTrue(NumberUtils.isNumber("1.5d"));
        assertTrue(NumberUtils.isNumber("1.5D"));
        assertFalse(NumberUtils.isNumber("123Lx")); // extra char
    }

    @Test(timeout = 4000)
    public void testIsNumber_TrailingDot() {
        // BUG: should be true, currently false
        assertTrue("isNumber('2.') should be true", NumberUtils.isNumber("2."));
    }

    @Test(timeout = 4000)
    public void testIsNumber_LeadingDot() {
        assertTrue(NumberUtils.isNumber(".5"));
        assertFalse(NumberUtils.isNumber("."));
    }

    @Test(timeout = 4000)
    public void testIsNumber_ExponentWithSign() {
        assertTrue(NumberUtils.isNumber("1e+2"));
        assertTrue(NumberUtils.isNumber("1e-2"));
    }

    @Test(timeout = 4000)
    public void testIsNumber_DoubleExponent() {
        assertFalse(NumberUtils.isNumber("1e2e3"));
    }

    // =================== Partition F: min/max array methods ===================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMin_LongArray_Null() {
        NumberUtils.min((long[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMin_LongArray_Empty() {
        NumberUtils.min(new long[0]);
    }

    @Test(timeout = 4000)
    public void testMin_LongArray() {
        assertEquals(1L, NumberUtils.min(new long[]{5L, 1L, 3L}));
        assertEquals(5L, NumberUtils.min(new long[]{5L}));
        assertEquals(Long.MIN_VALUE, NumberUtils.min(new long[]{Long.MIN_VALUE, Long.MAX_VALUE}));
    }

    @Test(timeout = 4000)
    public void testMin_IntArray() {
        assertEquals(1, NumberUtils.min(new int[]{5, 1, 3}));
        assertEquals(5, NumberUtils.min(new int[]{5}));
        assertEquals(Integer.MIN_VALUE, NumberUtils.min(new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE}));
    }

    @Test(timeout = 4000)
    public void testMin_ShortArray() {
        assertEquals((short)1, NumberUtils.min(new short[]{(short)5, (short)1, (short)3}));
    }

    @Test(timeout = 4000)
    public void testMin_ByteArray() {
        assertEquals((byte)1, NumberUtils.min(new byte[]{(byte)5, (byte)1, (byte)3}));
    }

    @Test(timeout = 4000)
    public void testMin_DoubleArray() {
        assertEquals(1.5, NumberUtils.min(new double[]{5.0, 1.5, 3.0}), 0.0);
        assertTrue(Double.isNaN(NumberUtils.min(new double[]{1.0, Double.NaN, 2.0})));
    }

    @Test(timeout = 4000)
    public void testMin_FloatArray() {
        assertEquals(1.5f, NumberUtils.min(new float[]{5.0f, 1.5f, 3.0f}), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.min(new float[]{1.0f, Float.NaN, 2.0f})));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMax_LongArray_Null() {
        NumberUtils.max((long[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMax_LongArray_Empty() {
        NumberUtils.max(new long[0]);
    }

    @Test(timeout = 4000)
    public void testMax_LongArray() {
        assertEquals(5L, NumberUtils.max(new long[]{5L, 1L, 3L}));
        assertEquals(Long.MAX_VALUE, NumberUtils.max(new long[]{Long.MIN_VALUE, Long.MAX_VALUE}));
    }

    @Test(timeout = 4000)
    public void testMax_IntArray() {
        assertEquals(5, NumberUtils.max(new int[]{5, 1, 3}));
    }

    @Test(timeout = 4000)
    public void testMax_ShortArray() {
        assertEquals((short)5, NumberUtils.max(new short[]{(short)5, (short)1, (short)3}));
    }

    @Test(timeout = 4000)
    public void testMax_ByteArray() {
        assertEquals((byte)5, NumberUtils.max(new byte[]{(byte)5, (byte)1, (byte)3}));
    }

    @Test(timeout = 4000)
    public void testMax_DoubleArray() {
        assertEquals(5.0, NumberUtils.max(new double[]{5.0, 1.5, 3.0}), 0.0);
        assertTrue(Double.isNaN(NumberUtils.max(new double[]{1.0, Double.NaN, 2.0})));
    }

    @Test(timeout = 4000)
    public void testMax_FloatArray() {
        assertEquals(5.0f, NumberUtils.max(new float[]{5.0f, 1.5f, 3.0f}), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.max(new float[]{1.0f, Float.NaN, 2.0f})));
    }

    // =================== Partition G: min/max three-value methods ===================

    @Test(timeout = 4000)
    public void testMinThree_Long() {
        assertEquals(1L, NumberUtils.min(5L, 1L, 3L));
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));
    }

    @Test(timeout = 4000)
    public void testMinThree_Int() {
        assertEquals(1, NumberUtils.min(5, 1, 3));
    }

    @Test(timeout = 4000)
    public void testMinThree_Short() {
        assertEquals((short)1, NumberUtils.min((short)5, (short)1, (short)3));
    }

    @Test(timeout = 4000)
    public void testMinThree_Byte() {
        assertEquals((byte)1, NumberUtils.min((byte)5, (byte)1, (byte)3));
    }

    @Test(timeout = 4000)
    public void testMinThree_Double() {
        assertEquals(1.0, NumberUtils.min(5.0, 1.0, 3.0), 0.0);
        assertTrue(Double.isNaN(NumberUtils.min(1.0, Double.NaN, 2.0)));
    }

    @Test(timeout = 4000)
    public void testMinThree_Float() {
        assertEquals(1.0f, NumberUtils.min(5.0f, 1.0f, 3.0f), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.min(1.0f, Float.NaN, 2.0f)));
    }

    @Test(timeout = 4000)
    public void testMaxThree_Long() {
        assertEquals(5L, NumberUtils.max(5L, 1L, 3L));
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(3L, 2L, 1L));
    }

    @Test(timeout = 4000)
    public void testMaxThree_Int() {
        assertEquals(5, NumberUtils.max(5, 1, 3));
    }

    @Test(timeout = 4000)
    public void testMaxThree_Short() {
        assertEquals((short)5, NumberUtils.max((short)5, (short)1, (short)3));
    }

    @Test(timeout = 4000)
    public void testMaxThree_Byte() {
        assertEquals((byte)5, NumberUtils.max((byte)5, (byte)1, (byte)3));
    }

    @Test(timeout = 4000)
    public void testMaxThree_Double() {
        assertEquals(5.0, NumberUtils.max(5.0, 1.0, 3.0), 0.0);
        assertTrue(Double.isNaN(NumberUtils.max(1.0, Double.NaN, 2.0)));
    }

    @Test(timeout = 4000)
    public void testMaxThree_Float() {
        assertEquals(5.0f, NumberUtils.max(5.0f, 1.0f, 3.0f), 0.0f);
        assertTrue(Float.isNaN(NumberUtils.max(1.0f, Float.NaN, 2.0f)));
    }

    // =================== Additional edge cases for createNumber ===================

    @Test(timeout = 4000)
    public void testCreateNumber_AllZeroesMantissaAndExponent() {
        // should return Float 0.0 (not BigDecimal) because allZeros = true
        Number result = NumberUtils.createNumber("0.0e10");
        assertTrue(result instanceof Float);
        assertEquals(0.0f, ((Float) result).floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_ExponentWithLeadingPlusSign() {
        Number result = NumberUtils.createNumber("1E+2");
        assertTrue(result instanceof Double); // because exponent and no type specifier
        assertEquals(100.0, ((Double) result).doubleValue(), 0.0);
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_ExponentPositionBeforeDecimal() {
        // "1e2.3" is invalid because exponent must come after decimal
        NumberUtils.createNumber("1e2.3");
    }

    @Test(timeout = 4000)
    public void testCreateNumber_NegativeHexadecimal() {
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_TooLargeForFloatWithQualifier() {
        // "1e40f" should trigger overflow and fall through to Double, then BigDecimal
        // But actually, default for 'f' will try Float, then Double, then BigDecimal.
        // 1e40f is Infinity, so fall through to Double; Double is also Infinity; then BigDecimal succeeds.
        NumberUtils.createNumber("1e40f");
    }
}