package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigInteger;
import java.math.BigDecimal;

/* [Branch & Defect Analysis Matrix]
 *
 * 1. LANG-747 / Hexadecimal Representation Defect:
 *    - In createNumber(str): Hex strings with 8 digits such as "0x80000000" or "0xFFFFFFFF"
 *      have hexDigits == 8, causing the method to route to createInteger(str). Integer.decode()
 *      fails with NumberFormatException because 0x80000000 is larger than Integer.MAX_VALUE (2147483647).
 *      Similarly, 16 hex digits starting with high bit >= 8 ("0x8000000000000000") overflow Long.
 *    - Target: testLang747ExposeOverflowInHex() tests "0x80000000" and asserts Long result.
 *
 * 2. createNumber(str) Decision & Type Qualifier Matrix:
 *    - null -> returns null
 *    - blank / empty / " " -> NumberFormatException
 *    - Hex Prefixes: "0x", "0X", "-0x", "-0X", "#", "-#" with lengths <=8, 9..16, >16 digits
 *    - Decimals and Exponents:
 *      * decPos and expPos permutations (dec with exp, dec without exp, exp without dec, neither)
 *      * invalid exp positions (expPos < decPos, expPos > length, double exponents)
 *    - Type qualifiers:
 *      * 'l', 'L' -> Long or BigInteger (too big for Long)
 *      * 'f', 'F' -> Float or falls through to Double/BigDecimal on precision loss/infinity
 *      * 'd', 'D' -> Double or falls through to BigDecimal on precision loss/infinity
 *      * non-digit lastChar not matching qualifiers -> NumberFormatException
 *    - Untyped floating point:
 *      * numDecimals <= 7 -> Float (checking non-zero / allZeros)
 *      * numDecimals <= 16 -> Double (checking non-zero / allZeros)
 *      * > 16 decimals or overflow -> BigDecimal
 *    - Untyped integral:
 *      * fits Integer, fits Long, overflows to BigInteger
 *
 * 3. Primitive Conversions (toInt, toLong, toFloat, toDouble, toByte, toShort):
 *    - null string -> default value
 *    - valid string -> parsed primitive
 *    - malformed string -> default value
 *    - overloaded methods defaulting to 0 / 0L / 0.0f / 0.0d / 0 / (short)0
 *
 * 4. Extrema Methods (min/max arrays and 3-arg primitives):
 *    - null and empty array validation (IllegalArgumentException)
 *    - array min/max for long, int, short, byte, double, float
 *    - NaN handling in float[] and double[] min/max
 *    - 3-arg min and max for long, int, short, byte, double, float (including NaN, -0.0 vs +0.0)
 *
 * 5. String Inspection Methods (isDigits, isNumber):
 *    - isDigits: null, empty, only digits, non-digits
 *    - isNumber: hex (0x...), sign prefixes, exponents (+/-, double exp), decimal point positions,
 *      type qualifiers ('l', 'f', 'd'), degenerate inputs (".", "1e", "1e+", "--1", "e1")
 */
public class NumberUtilsGeminiTest {

    // =========================================================================
    // PARTITION: DEFECT TARGET (LANG-747)
    // =========================================================================

    /**
     * LANG-747: Tests hexadecimal numbers whose parsed integer representation overflows
     * signed 32-bit Integer (>= 0x80000000) or signed 64-bit Long (>= 0x8000000000000000L).
     * createNumber must successfully return Long or BigInteger instead of throwing NumberFormatException.
     */
    @Test(timeout = 4000)
    public void testLang747ExposeOverflowInHex() {
        // 0x80000000 has 8 hex digits, but exceeds Integer.MAX_VALUE (2147483647L)
        final Number result32 = NumberUtils.createNumber("0x80000000");
        assertNotNull("Result should not be null", result32);
        assertTrue("0x80000000 must be a Long", result32 instanceof Long);
        assertEquals(0x80000000L, result32.longValue());

        // 0xFFFFFFFF has 8 hex digits, should also be parsed as Long (4294967295L)
        final Number result32Max = NumberUtils.createNumber("0xFFFFFFFF");
        assertNotNull(result32Max);
        assertTrue("0xFFFFFFFF must be a Long", result32Max instanceof Long);
        assertEquals(4294967295L, result32Max.longValue());

        // 0x8000000000000000 has 16 hex digits, but exceeds Long.MAX_VALUE
        final Number result64 = NumberUtils.createNumber("0x8000000000000000");
        assertNotNull(result64);
        assertTrue("0x8000000000000000 must be a BigInteger", result64 instanceof BigInteger);
        assertEquals(new BigInteger("8000000000000000", 16), result64);
    }

    // =========================================================================
    // PARTITION A: createNumber - Standard Numeric Primitives & Type Fallbacks
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumberBasicPrimitives() {
        assertNull(NumberUtils.createNumber(null));

        final Number i = NumberUtils.createNumber("12345");
        assertTrue(i instanceof Integer);
        assertEquals(12345, i.intValue());

        final Number l = NumberUtils.createNumber("2147483648"); // Integer.MAX_VALUE + 1
        assertTrue(l instanceof Long);
        assertEquals(2147483648L, l.longValue());

        final Number bi = NumberUtils.createNumber("9223372036854775808"); // Long.MAX_VALUE + 1
        assertTrue(bi instanceof BigInteger);
        assertEquals(new BigInteger("9223372036854775808"), bi);
    }

    @Test(timeout = 4000)
    public void testCreateNumberFloatingPointPrecisionDecisions() {
        // numDecimals <= 7 -> Float
        final Number f1 = NumberUtils.createNumber("1.123456");
        assertTrue(f1 instanceof Float);
        assertEquals(1.123456f, f1.floatValue(), 0.00001f);

        // numDecimals between 8 and 16 -> Double
        final Number d1 = NumberUtils.createNumber("1.12345678901");
        assertTrue(d1 instanceof Double);
        assertEquals(1.12345678901d, d1.doubleValue(), 0.000000001d);

        // numDecimals > 16 -> BigDecimal
        final Number bd = NumberUtils.createNumber("1.12345678901234567890");
        assertTrue(bd instanceof BigDecimal);
        assertEquals(new BigDecimal("1.12345678901234567890"), bd);
    }

    @Test(timeout = 4000)
    public void testCreateNumberAllZerosHandling() {
        final Number fZero = NumberUtils.createNumber("0.0");
        assertTrue(fZero instanceof Float);
        assertEquals(0.0f, fZero.floatValue(), 0.0f);

        final Number dZero = NumberUtils.createNumber("0.0000000000");
        assertTrue(dZero instanceof Double);
        assertEquals(0.0d, dZero.doubleValue(), 0.0d);

        // Underflow to float 0.0 with non-zero string should fall through to Double or BigDecimal
        final Number underflowFloat = NumberUtils.createNumber("1e-45");
        assertNotNull(underflowFloat);

        final Number underflowDouble = NumberUtils.createNumber("1e-324");
        assertNotNull(underflowDouble);
        assertTrue(underflowDouble instanceof BigDecimal);
    }

    // =========================================================================
    // PARTITION B: createNumber - Hexadecimal Representations
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumberHex() {
        // 0x prefix
        final Number hex1 = NumberUtils.createNumber("0x1234");
        assertTrue(hex1 instanceof Integer);
        assertEquals(0x1234, hex1.intValue());

        // 0X prefix
        final Number hex2 = NumberUtils.createNumber("0XABC");
        assertTrue(hex2 instanceof Integer);
        assertEquals(0xABC, hex2.intValue());

        // -0x prefix
        final Number hex3 = NumberUtils.createNumber("-0x10");
        assertTrue(hex3 instanceof Integer);
        assertEquals(-16, hex3.intValue());

        // -0X prefix
        final Number hex4 = NumberUtils.createNumber("-0X20");
        assertTrue(hex4 instanceof Integer);
        assertEquals(-32, hex4.intValue());

        // # prefix
        final Number hex5 = NumberUtils.createNumber("#12");
        assertTrue(hex5 instanceof Integer);
        assertEquals(0x12, hex5.intValue());

        // -# prefix
        final Number hex6 = NumberUtils.createNumber("-#12");
        assertTrue(hex6 instanceof Integer);
        assertEquals(-0x12, hex6.intValue());

        // > 8 hex digits -> Long
        final Number hexLong = NumberUtils.createNumber("0x100000000");
        assertTrue(hexLong instanceof Long);
        assertEquals(0x100000000L, hexLong.longValue());

        // > 16 hex digits -> BigInteger
        final Number hexBig = NumberUtils.createNumber("0x10000000000000000");
        assertTrue(hexBig instanceof BigInteger);
        assertEquals(new BigInteger("10000000000000000", 16), hexBig);
    }

    // =========================================================================
    // PARTITION C: createNumber - Scientific & Exponent Notations
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumberScientific() {
        final Number sciF = NumberUtils.createNumber("1.23e2");
        assertTrue(sciF instanceof Float);
        assertEquals(123.0f, sciF.floatValue(), 0.001f);

        final Number sciD = NumberUtils.createNumber("1.2345678901E2");
        assertTrue(sciD instanceof Double);
        assertEquals(123.45678901d, sciD.doubleValue(), 0.000001d);

        final Number expNoDec = NumberUtils.createNumber("2e10");
        assertTrue(expNoDec instanceof Float);
        assertEquals(2e10f, expNoDec.floatValue(), 100f);
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidExponentPositionDecPosAfter() {
        NumberUtils.createNumber("1.2e3.4");
    }

    // =========================================================================
    // PARTITION D: createNumber - Type Qualifiers ('l', 'f', 'd')
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumberWithTypeQualifiers() {
        // 'l' / 'L'
        final Number l1 = NumberUtils.createNumber("1234L");
        assertTrue(l1 instanceof Long);
        assertEquals(1234L, l1.longValue());

        final Number l2 = NumberUtils.createNumber("-1234l");
        assertTrue(l2 instanceof Long);
        assertEquals(-1234L, l2.longValue());

        // Large number with 'L' overflowing Long -> BigInteger
        final Number lOverflow = NumberUtils.createNumber("9223372036854775808L");
        assertTrue(lOverflow instanceof BigInteger);
        assertEquals(new BigInteger("9223372036854775808"), lOverflow);

        // 'f' / 'F'
        final Number f1 = NumberUtils.createNumber("12.34f");
        assertTrue(f1 instanceof Float);
        assertEquals(12.34f, f1.floatValue(), 0.001f);

        final Number f2 = NumberUtils.createNumber("-56.78F");
        assertTrue(f2 instanceof Float);
        assertEquals(-56.78f, f2.floatValue(), 0.001f);

        // 'f' overflow -> Double
        final Number fOverflow = NumberUtils.createNumber("3.4028236e+39f");
        assertTrue(fOverflow instanceof Double);

        // 'd' / 'D'
        final Number d1 = NumberUtils.createNumber("12.34d");
        assertTrue(d1 instanceof Double);
        assertEquals(12.34d, d1.doubleValue(), 0.001d);

        final Number d2 = NumberUtils.createNumber("-56.78D");
        assertTrue(d2 instanceof Double);
        assertEquals(-56.78d, d2.doubleValue(), 0.001d);

        // 'd' overflow -> BigDecimal
        final Number dOverflow = NumberUtils.createNumber("1.7976931348623159e+309d");
        assertTrue(dOverflow instanceof BigDecimal);
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidQualifierLWithDecimal() {
        NumberUtils.createNumber("12.34L");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberInvalidQualifierLWithExponent() {
        NumberUtils.createNumber("12e3L");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberUnknownQualifier() {
        NumberUtils.createNumber("1234q");
    }

    // =========================================================================
    // PARTITION E: Edge & Degenerate Cases for createNumber & create*
    // =========================================================================

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("   ");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberEmpty() {
        NumberUtils.createNumber("");
    }

    @Test(timeout = 4000)
    public void testSpecificCreateMethods() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));

        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(1.5d), NumberUtils.createDouble("1.5"));

        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(15), NumberUtils.createInteger("15"));
        assertEquals(Integer.valueOf(16), NumberUtils.createInteger("0x10"));
        assertEquals(Integer.valueOf(8), NumberUtils.createInteger("010"));

        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(15L), NumberUtils.createLong("15"));
        assertEquals(Long.valueOf(16L), NumberUtils.createLong("0x10"));

        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
        assertEquals(new BigInteger("16"), NumberUtils.createBigInteger("0x10"));
        assertEquals(new BigInteger("-16"), NumberUtils.createBigInteger("-0x10"));
        assertEquals(new BigInteger("16"), NumberUtils.createBigInteger("#10"));
        assertEquals(new BigInteger("-16"), NumberUtils.createBigInteger("-#10"));
        assertEquals(new BigInteger("8"), NumberUtils.createBigInteger("010"));
        assertEquals(new BigInteger("-8"), NumberUtils.createBigInteger("-010"));

        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("123.456"), NumberUtils.createBigDecimal("123.456"));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimalBlank() {
        NumberUtils.createBigDecimal("");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimalDoubleMinus() {
        NumberUtils.createBigDecimal("--1.23");
    }

    // =========================================================================
    // PARTITION F: Primitive Safe Conversion Helpers (toX)
    // =========================================================================

    @Test(timeout = 4000)
    public void testToInt() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(123, NumberUtils.toInt("123"));
        assertEquals(42, NumberUtils.toInt("invalid", 42));
        assertEquals(5, NumberUtils.toInt(null, 5));
    }

    @Test(timeout = 4000)
    public void testToLong() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(0L, NumberUtils.toLong("abc"));
        assertEquals(123L, NumberUtils.toLong("123"));
        assertEquals(42L, NumberUtils.toLong("invalid", 42L));
        assertEquals(5L, NumberUtils.toLong(null, 5L));
    }

    @Test(timeout = 4000)
    public void testToFloat() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat("abc"), 0.0f);
        assertEquals(1.25f, NumberUtils.toFloat("1.25"), 0.001f);
        assertEquals(4.2f, NumberUtils.toFloat("invalid", 4.2f), 0.001f);
        assertEquals(5.5f, NumberUtils.toFloat(null, 5.5f), 0.001f);
    }

    @Test(timeout = 4000)
    public void testToDouble() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble("abc"), 0.0d);
        assertEquals(1.25d, NumberUtils.toDouble("1.25"), 0.001d);
        assertEquals(4.2d, NumberUtils.toDouble("invalid", 4.2d), 0.001d);
        assertEquals(5.5d, NumberUtils.toDouble(null, 5.5d), 0.001d);
    }

    @Test(timeout = 4000)
    public void testToByte() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 0, NumberUtils.toByte("abc"));
        assertEquals((byte) 12, NumberUtils.toByte("12"));
        assertEquals((byte) 42, NumberUtils.toByte("invalid", (byte) 42));
        assertEquals((byte) 5, NumberUtils.toByte(null, (byte) 5));
    }

    @Test(timeout = 4000)
    public void testToShort() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 0, NumberUtils.toShort("abc"));
        assertEquals((short) 123, NumberUtils.toShort("123"));
        assertEquals((short) 42, NumberUtils.toShort("invalid", (short) 42));
        assertEquals((short) 5, NumberUtils.toShort(null, (short) 5));
    }

    // =========================================================================
    // PARTITION G: Array Extrema Methods (min/max)
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinMaxLongArray() {
        final long[] arr = { 5L, -2L, 10L, 0L };
        assertEquals(-2L, NumberUtils.min(arr));
        assertEquals(10L, NumberUtils.max(arr));
    }

    @Test(timeout = 4000)
    public void testMinMaxIntArray() {
        final int[] arr = { 5, -2, 10, 0 };
        assertEquals(-2, NumberUtils.min(arr));
        assertEquals(10, NumberUtils.max(arr));
    }

    @Test(timeout = 4000)
    public void testMinMaxShortArray() {
        final short[] arr = { (short) 5, (short) -2, (short) 10, (short) 0 };
        assertEquals((short) -2, NumberUtils.min(arr));
        assertEquals((short) 10, NumberUtils.max(arr));
    }

    @Test(timeout = 4000)
    public void testMinMaxByteArray() {
        final byte[] arr = { (byte) 5, (byte) -2, (byte) 10, (byte) 0 };
        assertEquals((byte) -2, NumberUtils.min(arr));
        assertEquals((byte) 10, NumberUtils.max(arr));
    }

    @Test(timeout = 4000)
    public void testMinMaxDoubleArray() {
        final double[] arr = { 5.5, -2.2, 10.1, 0.0 };
        assertEquals(-2.2, NumberUtils.min(arr), 0.001);
        assertEquals(10.1, NumberUtils.max(arr), 0.001);

        final double[] arrWithNaN = { 1.0, Double.NaN, 2.0 };
        assertTrue(Double.isNaN(NumberUtils.min(arrWithNaN)));
        assertTrue(Double.isNaN(NumberUtils.max(arrWithNaN)));
    }

    @Test(timeout = 4000)
    public void testMinMaxFloatArray() {
        final float[] arr = { 5.5f, -2.2f, 10.1f, 0.0f };
        assertEquals(-2.2f, NumberUtils.min(arr), 0.001f);
        assertEquals(10.1f, NumberUtils.max(arr), 0.001f);

        final float[] arrWithNaN = { 1.0f, Float.NaN, 2.0f };
        assertTrue(Float.isNaN(NumberUtils.min(arrWithNaN)));
        assertTrue(Float.isNaN(NumberUtils.max(arrWithNaN)));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMinNullArrayValidation() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMaxEmptyArrayValidation() {
        NumberUtils.max(new long[0]);
    }

    // =========================================================================
    // PARTITION H: Three-Parameter Extrema Methods (min/max)
    // =========================================================================

    @Test(timeout = 4000)
    public void testThreeParamMin() {
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));

        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(1, NumberUtils.min(3, 2, 1));

        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 1, (short) 2));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 2, (short) 1));

        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 1, (byte) 2));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 2, (byte) 1));

        assertEquals(1.0d, NumberUtils.min(1.0d, 2.0d, 3.0d), 0.001d);
        assertEquals(1.0d, NumberUtils.min(3.0d, 1.0d, 2.0d), 0.001d);
        assertEquals(1.0d, NumberUtils.min(3.0d, 2.0d, 1.0d), 0.001d);

        assertEquals(1.0f, NumberUtils.min(1.0f, 2.0f, 3.0f), 0.001f);
        assertEquals(1.0f, NumberUtils.min(3.0f, 1.0f, 2.0f), 0.001f);
        assertEquals(1.0f, NumberUtils.min(3.0f, 2.0f, 1.0f), 0.001f);
    }

    @Test(timeout = 4000)
    public void testThreeParamMax() {
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(1L, 3L, 2L));
        assertEquals(3L, NumberUtils.max(3L, 2L, 1L));

        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(3, NumberUtils.max(1, 3, 2));
        assertEquals(3, NumberUtils.max(3, 2, 1));

        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 3, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 2, (short) 1));

        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 3, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 2, (byte) 1));

        assertEquals(3.0d, NumberUtils.max(1.0d, 2.0d, 3.0d), 0.001d);
        assertEquals(3.0d, NumberUtils.max(1.0d, 3.0d, 2.0d), 0.001d);
        assertEquals(3.0d, NumberUtils.max(3.0d, 2.0d, 1.0d), 0.001d);

        assertEquals(3.0f, NumberUtils.max(1.0f, 2.0f, 3.0f), 0.001f);
        assertEquals(3.0f, NumberUtils.max(1.0f, 3.0f, 2.0f), 0.001f);
        assertEquals(3.0f, NumberUtils.max(3.0f, 2.0f, 1.0f), 0.001f);
    }

    // =========================================================================
    // PARTITION I: isDigits and isNumber
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("12a3"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertFalse(NumberUtils.isDigits("-123"));
        assertTrue(NumberUtils.isDigits("12345"));
    }

    @Test(timeout = 4000)
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("   "));
        assertFalse(NumberUtils.isNumber("abc"));

        // Hex
        assertTrue(NumberUtils.isNumber("0x1234"));
        assertTrue(NumberUtils.isNumber("0xABCD"));
        assertTrue(NumberUtils.isNumber("-0x1234"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertFalse(NumberUtils.isNumber("0xGHI"));

        // Standard integer & decimal
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("+123"));
        assertTrue(NumberUtils.isNumber("123.456"));
        assertTrue(NumberUtils.isNumber("-123.456"));
        assertTrue(NumberUtils.isNumber(".5"));
        assertTrue(NumberUtils.isNumber("5."));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("1.2.3"));

        // Exponents
        assertTrue(NumberUtils.isNumber("1e5"));
        assertTrue(NumberUtils.isNumber("1E5"));
        assertTrue(NumberUtils.isNumber("1.2e+5"));
        assertTrue(NumberUtils.isNumber("1.2E-5"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("1e-"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("e1"));

        // Type qualifiers
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123l"));
        assertFalse(NumberUtils.isNumber("12.3L"));
        assertFalse(NumberUtils.isNumber("1e3L"));
        assertTrue(NumberUtils.isNumber("123f"));
        assertTrue(NumberUtils.isNumber("123F"));
        assertTrue(NumberUtils.isNumber("123.4f"));
        assertTrue(NumberUtils.isNumber("123d"));
        assertTrue(NumberUtils.isNumber("123D"));
        assertTrue(NumberUtils.isNumber("123.4d"));
        assertFalse(NumberUtils.isNumber("123q"));
        assertFalse(NumberUtils.isNumber("--123"));
    }

    // =========================================================================
    // PARTITION J: Constants and Constructor Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstantsAndConstructor() {
        assertNotNull(new NumberUtils());

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