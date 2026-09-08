package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigInteger;
import java.math.BigDecimal;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * TARGET METHOD: NumberUtils.createNumber(String) and its collaborators
 * (createInteger, createLong, createBigInteger, createBigDecimal, createFloat,
 * createDouble), plus toInt/toLong/toFloat/toDouble/toByte/toShort,
 * isDigits, isNumber, min/max array & 3-arg overloads.
 *
 * KNOWN DEFECT (LANG-747):
 *   - Branch: hex prefix detection block in createNumber()
 *     `if (hexDigits > 8) { return createLong(str); } return createInteger(str);`
 *   - Boundary condition: exactly 8 hex digits (hexDigits == 8) whose numeric
 *     value exceeds Integer.MAX_VALUE (e.g. "0x80000000" == 2147483648) is
 *     incorrectly routed to createInteger(), which internally calls
 *     Integer.decode(), throwing NumberFormatException instead of gracefully
 *     widening to a Long.
 *   - Dedicated test: testLang747_Hex8DigitBoundary_ShouldReturnLong()
 *
 * OTHER TARGETED BRANCHES:
 *   - Hex prefix matrix: "0x","0X","-0x","-0X","#","-#"
 *   - hexDigits > 16 (BigInteger), 8 < hexDigits <= 16 (Long), <=8 (Integer)
 *   - Decimal point present / absent, exponent present / absent (decPos, expPos)
 *   - expPos < decPos guard (IOOBE guard) -> NumberFormatException
 *   - Type qualifier switch: l/L, f/F, d/D, fallthrough f->d->BigDecimal
 *   - allZeros precision-loss guard for float/double zero collapse
 *   - numDecimals <=7 (float attempt), <=16 (double attempt), else BigDecimal
 *   - isAllZeros utility (via mant/exp all-zero combinations)
 *   - createBigInteger octal/hex/decimal prefix branches, negate handling
 *   - createBigDecimal blank-check and "--" protection branch
 *   - isDigits / isNumber character class state machine (sign, decimal, exp)
 *   - validateArray null/empty guard branches for min/max array methods
 *   - NaN propagation branches in min/max(double[]/float[])
 */
public class NumberUtilsClaudeTest {

    // =====================================================================
    // CRITICAL DEFECT TEST: LANG-747
    // =====================================================================

    @Test(timeout = 4000)
    public void testLang747_Hex8DigitBoundary_ShouldReturnLong() {
        // "0x80000000" has exactly 8 hex digits after prefix, but its value
        // (2147483648) exceeds Integer.MAX_VALUE (2147483647). The correct
        // behavior is to widen to Long rather than throw.
        Number result = NumberUtils.createNumber("0x80000000");
        assertTrue("Expected result to be a Long for 8-digit hex exceeding int range",
                result instanceof Long);
        assertEquals(2147483648L, result.longValue());
    }

    @Test(timeout = 4000)
    public void testLang747_HexFFFFFFFF_ShouldReturnLong() {
        Number result = NumberUtils.createNumber("0xFFFFFFFF");
        assertTrue("Expected result to be a Long for 0xFFFFFFFF", result instanceof Long);
        assertEquals(4294967295L, result.longValue());
    }

    @Test(timeout = 4000)
    public void testHex8Digit_WithinIntRange_ReturnsInteger() {
        Number result = NumberUtils.createNumber("0x7FFFFFFF");
        assertTrue(result instanceof Integer);
        assertEquals(2147483647, result.intValue());
    }

    // =====================================================================
    // PARTITION A: Standard Numeric Primitives
    // =====================================================================

    @Test(timeout = 4000)
    public void testToInt_NullAndDefault() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(1, NumberUtils.toInt("1"));
        assertEquals(1, NumberUtils.toInt(null, 1));
        assertEquals(5, NumberUtils.toInt("abc", 5));
        assertEquals(123, NumberUtils.toInt("123", 0));
    }

    @Test(timeout = 4000)
    public void testToLong_NullAndDefault() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(1L, NumberUtils.toLong("1"));
        assertEquals(1L, NumberUtils.toLong(null, 1L));
        assertEquals(99L, NumberUtils.toLong("abc", 99L));
        assertEquals(123456789012L, NumberUtils.toLong("123456789012", 0L));
    }

    @Test(timeout = 4000)
    public void testToFloat_NullAndDefault() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0f);
        assertEquals(1.1f, NumberUtils.toFloat(null, 1.1f), 0.0f);
        assertEquals(2.2f, NumberUtils.toFloat("abc", 2.2f), 0.0f);
    }

    @Test(timeout = 4000)
    public void testToDouble_NullAndDefault() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0d);
        assertEquals(1.1d, NumberUtils.toDouble(null, 1.1d), 0.0d);
        assertEquals(3.3d, NumberUtils.toDouble("abc", 3.3d), 0.0d);
    }

    @Test(timeout = 4000)
    public void testToByte_NullAndDefault() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 5, NumberUtils.toByte("5"));
        assertEquals((byte) 9, NumberUtils.toByte("abc", (byte) 9));
        assertEquals((byte) 1, NumberUtils.toByte(null, (byte) 1));
    }

    @Test(timeout = 4000)
    public void testToShort_NullAndDefault() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 5, NumberUtils.toShort("5"));
        assertEquals((short) 9, NumberUtils.toShort("abc", (short) 9));
        assertEquals((short) 1, NumberUtils.toShort(null, (short) 1));
    }

    @Test(timeout = 4000)
    public void testCreateFloat_NullAndValid() {
        assertNull(NumberUtils.createFloat(null));
        Float f = NumberUtils.createFloat("1.5");
        assertTrue(f instanceof Float);
        assertEquals(1.5f, f, 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateDouble_NullAndValid() {
        assertNull(NumberUtils.createDouble(null));
        Double d = NumberUtils.createDouble("1.5");
        assertTrue(d instanceof Double);
        assertEquals(1.5d, d, 0.0d);
    }

    @Test(timeout = 4000)
    public void testCreateInteger_NullAndValid() {
        assertNull(NumberUtils.createInteger(null));
        Integer i = NumberUtils.createInteger("123");
        assertTrue(i instanceof Integer);
        assertEquals(123, i.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateLong_NullAndValid() {
        assertNull(NumberUtils.createLong(null));
        Long l = NumberUtils.createLong("123456789012");
        assertTrue(l instanceof Long);
        assertEquals(123456789012L, l.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateBigInteger_NullAndDecimal() {
        assertNull(NumberUtils.createBigInteger(null));
        BigInteger bi = NumberUtils.createBigInteger("123");
        assertTrue(bi instanceof BigInteger);
        assertEquals(BigInteger.valueOf(123), bi);
    }

    @Test(timeout = 4000)
    public void testCreateBigInteger_Octal() {
        BigInteger bi = NumberUtils.createBigInteger("010");
        assertEquals(BigInteger.valueOf(8), bi);
    }

    @Test(timeout = 4000)
    public void testCreateBigInteger_HexAndSign() {
        assertEquals(BigInteger.valueOf(26), NumberUtils.createBigInteger("0x1A"));
        assertEquals(BigInteger.valueOf(-26), NumberUtils.createBigInteger("-0x1A"));
        assertEquals(BigInteger.valueOf(26), NumberUtils.createBigInteger("#1A"));
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimal_NullAndValid() {
        assertNull(NumberUtils.createBigDecimal(null));
        BigDecimal bd = NumberUtils.createBigDecimal("1.5");
        assertTrue(bd instanceof BigDecimal);
        assertEquals(new BigDecimal("1.5"), bd);
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimal_Blank() {
        NumberUtils.createBigDecimal("");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateBigDecimal_DoubleMinusProtection() {
        NumberUtils.createBigDecimal("--1.1");
    }

    // =====================================================================
    // PARTITION B: Hexadecimal Representations (Critical Bug Zone)
    // =====================================================================

    @Test(timeout = 4000)
    public void testCreateNumber_Hex_LowerX() {
        Number n = NumberUtils.createNumber("0x10");
        assertTrue(n instanceof Integer);
        assertEquals(16, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_Hex_UpperX() {
        Number n = NumberUtils.createNumber("0X10");
        assertTrue(n instanceof Integer);
        assertEquals(16, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_Hex_HashPrefix() {
        Number n = NumberUtils.createNumber("#10");
        assertTrue(n instanceof Integer);
        assertEquals(16, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_Hex_NegativeLowerX() {
        Number n = NumberUtils.createNumber("-0x10");
        assertTrue(n instanceof Integer);
        assertEquals(-16, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_Hex_NegativeUpperX() {
        Number n = NumberUtils.createNumber("-0X10");
        assertTrue(n instanceof Integer);
        assertEquals(-16, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_Hex_NegativeHash() {
        Number n = NumberUtils.createNumber("-#1234");
        assertTrue(n instanceof Integer);
        assertEquals(-4660, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_Hex_9DigitsForcesLong() {
        String s = "0x123456789";
        Number n = NumberUtils.createNumber(s);
        assertTrue(n instanceof Long);
        assertEquals(NumberUtils.createLong(s), n);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_Hex_17DigitsForcesBigInteger() {
        String s = "0x10000000000000000";
        Number n = NumberUtils.createNumber(s);
        assertTrue(n instanceof BigInteger);
        assertEquals(NumberUtils.createBigInteger(s), n);
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_HexWithLSuffix_ThrowsBecausePfxLenShortCircuits() {
        // The prefix-detection branch returns before the suffix-qualifier
        // logic is ever consulted, so trailing 'L' is treated as an invalid
        // hex digit by Integer.decode -> NumberFormatException.
        NumberUtils.createNumber("0x12L");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_HexWithLowerLSuffix_Throws() {
        NumberUtils.createNumber("0x12l");
    }

    // =====================================================================
    // PARTITION C: Scientific / Exponent Notations
    // =====================================================================

    @Test(timeout = 4000)
    public void testCreateNumber_ScientificPlusExponent() {
        Number n = NumberUtils.createNumber("1.2e+3");
        assertTrue(n instanceof Float);
        assertEquals(Float.parseFloat("1.2e+3"), n.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_ScientificNegativeExponentUpperE() {
        Number n = NumberUtils.createNumber("-2.5E-4");
        assertTrue(n instanceof Float);
        assertEquals(Float.parseFloat("-2.5E-4"), n.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_AllZerosExponent() {
        Number n = NumberUtils.createNumber("00E0");
        assertTrue(n instanceof Float);
        assertEquals(0.0f, n.floatValue(), 0.0f);
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_ExpBeforeDecimalGuard() {
        // expPos < decPos triggers explicit NumberFormatException guard
        NumberUtils.createNumber("1e2.3");
    }

    @Test(timeout = 4000)
    public void testCreateNumber_ExponentOverflowFallsToBigDecimal() {
        Number n = NumberUtils.createNumber("1E400");
        assertTrue(n instanceof BigDecimal);
        assertEquals(new BigDecimal("1E400"), n);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_ExponentOverflowWithFSuffixFallsToBigDecimal() {
        Number n = NumberUtils.createNumber("1E400F");
        assertTrue(n instanceof BigDecimal);
        assertEquals(new BigDecimal("1E400"), n);
    }

    // =====================================================================
    // PARTITION D: Type Qualifiers & Case Sensitivity
    // =====================================================================

    @Test(timeout = 4000)
    public void testCreateNumber_SuffixF() {
        Number n = NumberUtils.createNumber("123f");
        assertTrue(n instanceof Float);
        assertEquals(123.0f, n.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_SuffixUpperF() {
        Number n = NumberUtils.createNumber("123F");
        assertTrue(n instanceof Float);
        assertEquals(123.0f, n.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_SuffixD() {
        Number n = NumberUtils.createNumber("123d");
        assertTrue(n instanceof Double);
        assertEquals(123.0d, n.doubleValue(), 0.0d);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_SuffixUpperD() {
        Number n = NumberUtils.createNumber("123D");
        assertTrue(n instanceof Double);
        assertEquals(123.0d, n.doubleValue(), 0.0d);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_SuffixL() {
        Number n = NumberUtils.createNumber("123L");
        assertTrue(n instanceof Long);
        assertEquals(123L, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_SuffixLowerL() {
        Number n = NumberUtils.createNumber("123l");
        assertTrue(n instanceof Long);
        assertEquals(123L, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_NegativeSuffixL() {
        Number n = NumberUtils.createNumber("-123L");
        assertTrue(n instanceof Long);
        assertEquals(-123L, n.longValue());
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_DecimalWithLSuffix_Throws() {
        // 'L' with a decimal point present is invalid
        NumberUtils.createNumber("1.5L");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_InvalidSuffixChar_Throws() {
        NumberUtils.createNumber("123c");
    }

    // =====================================================================
    // PARTITION E: Edge & Degenerate Cases
    // =====================================================================

    @Test(timeout = 4000)
    public void testCreateNumber_Null() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_EmptyString() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_SingleWhitespace() {
        NumberUtils.createNumber(" ");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_LeadingWhitespace() {
        NumberUtils.createNumber(" 123");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_TrailingWhitespace() {
        NumberUtils.createNumber("123 ");
    }

    @Test(timeout = 4000)
    public void testCreateNumber_AllZeros() {
        Number n = NumberUtils.createNumber("000");
        assertTrue(n instanceof Integer);
        assertEquals(0, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_LeadingZeroOctal() {
        Number n = NumberUtils.createNumber("045");
        assertTrue(n instanceof Integer);
        assertEquals(37, n.intValue());
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumber_MultipleDots_Throws() {
        NumberUtils.createNumber("1.2.3");
    }

    @Test(timeout = 4000)
    public void testCreateNumber_IntegerBoundary_MaxInt() {
        Number n = NumberUtils.createNumber("2147483647");
        assertTrue(n instanceof Integer);
        assertEquals(Integer.MAX_VALUE, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_IntegerOverflow_BecomesLong() {
        Number n = NumberUtils.createNumber("2147483648");
        assertTrue(n instanceof Long);
        assertEquals(2147483648L, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_LongBoundary_MaxLong() {
        Number n = NumberUtils.createNumber("9223372036854775807");
        assertTrue(n instanceof Long);
        assertEquals(Long.MAX_VALUE, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_LongOverflow_BecomesBigInteger() {
        Number n = NumberUtils.createNumber("9223372036854775808");
        assertTrue(n instanceof BigInteger);
        assertEquals(new BigInteger("9223372036854775808"), n);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_SimpleDecimal_ReturnsFloat() {
        Number n = NumberUtils.createNumber("3.14");
        assertTrue(n instanceof Float);
        assertEquals(3.14f, n.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_MediumPrecisionDecimal_ReturnsDouble() {
        Number n = NumberUtils.createNumber("1.123456789");
        assertTrue(n instanceof Double);
        assertEquals(Double.parseDouble("1.123456789"), n.doubleValue(), 0.0d);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_HighPrecisionDecimal_ReturnsBigDecimal() {
        String s = "1.12345678901234567";
        Number n = NumberUtils.createNumber(s);
        assertTrue(n instanceof BigDecimal);
        assertEquals(new BigDecimal(s), n);
    }

    // =====================================================================
    // isDigits / isNumber coverage
    // =====================================================================

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("123"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertFalse(NumberUtils.isDigits("12a"));
    }

    @Test(timeout = 4000)
    public void testIsNumber_BasicCases() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertTrue(NumberUtils.isNumber("1"));
        assertTrue(NumberUtils.isNumber("-1"));
        assertTrue(NumberUtils.isNumber("1.1"));
        assertTrue(NumberUtils.isNumber("-1.1"));
        assertTrue(NumberUtils.isNumber("1.1e10"));
        assertTrue(NumberUtils.isNumber("1.1e-10"));
        assertTrue(NumberUtils.isNumber("1.1E+10"));
    }

    @Test(timeout = 4000)
    public void testIsNumber_Hex() {
        assertTrue(NumberUtils.isNumber("0x1F"));
        assertFalse(NumberUtils.isNumber("0x"));
    }

    @Test(timeout = 4000)
    public void testIsNumber_Qualifiers() {
        assertTrue(NumberUtils.isNumber("1L"));
        assertTrue(NumberUtils.isNumber("1l"));
        assertTrue(NumberUtils.isNumber("1D"));
        assertTrue(NumberUtils.isNumber("1F"));
    }

    @Test(timeout = 4000)
    public void testIsNumber_InvalidAndEdgeCases() {
        assertFalse(NumberUtils.isNumber("abc"));
        assertTrue(NumberUtils.isNumber("1."));
        assertTrue(NumberUtils.isNumber(".1"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("--1"));
        assertFalse(NumberUtils.isNumber("+1"));
    }

    // =====================================================================
    // min/max array method coverage
    // =====================================================================

    @Test(timeout = 4000)
    public void testMinMax_LongArray() {
        long[] arr = {3L, 1L, 2L};
        assertEquals(1L, NumberUtils.min(arr));
        assertEquals(3L, NumberUtils.max(arr));
    }

    @Test(timeout = 4000)
    public void testMinMax_IntArray() {
        int[] arr = {3, 1, 2};
        assertEquals(1, NumberUtils.min(arr));
        assertEquals(3, NumberUtils.max(arr));
    }

    @Test(timeout = 4000)
    public void testMinMax_ShortArray() {
        short[] arr = {3, 1, 2};
        assertEquals((short) 1, NumberUtils.min(arr));
        assertEquals((short) 3, NumberUtils.max(arr));
    }

    @Test(timeout = 4000)
    public void testMinMax_ByteArray() {
        byte[] arr = {3, 1, 2};
        assertEquals((byte) 1, NumberUtils.min(arr));
        assertEquals((byte) 3, NumberUtils.max(arr));
    }

    @Test(timeout = 4000)
    public void testMinMax_DoubleArray() {
        double[] arr = {3.0, 1.0, 2.0};
        assertEquals(1.0, NumberUtils.min(arr), 0.0d);
        assertEquals(3.0, NumberUtils.max(arr), 0.0d);
    }

    @Test(timeout = 4000)
    public void testMinMax_FloatArray() {
        float[] arr = {3.0f, 1.0f, 2.0f};
        assertEquals(1.0f, NumberUtils.min(arr), 0.0f);
        assertEquals(3.0f, NumberUtils.max(arr), 0.0f);
    }

    @Test(timeout = 4000)
    public void testMinMax_DoubleArrayWithNaN() {
        double[] arr = {1.0, Double.NaN, 2.0};
        assertTrue(Double.isNaN(NumberUtils.min(arr)));
        assertTrue(Double.isNaN(NumberUtils.max(arr)));
    }

    @Test(timeout = 4000)
    public void testMinMax_FloatArrayWithNaN() {
        float[] arr = {1.0f, Float.NaN, 2.0f};
        assertTrue(Float.isNaN(NumberUtils.min(arr)));
        assertTrue(Float.isNaN(NumberUtils.max(arr)));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMin_NullLongArray_Throws() {
        NumberUtils.min((long[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMin_EmptyLongArray_Throws() {
        NumberUtils.min(new long[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMax_NullIntArray_Throws() {
        NumberUtils.max((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMax_EmptyIntArray_Throws() {
        NumberUtils.max(new int[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMin_EmptyDoubleArray_Throws() {
        NumberUtils.min(new double[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMax_EmptyFloatArray_Throws() {
        NumberUtils.max(new float[0]);
    }

    // =====================================================================
    // 3-arg min/max coverage
    // =====================================================================

    @Test(timeout = 4000)
    public void testMinMax_ThreeLongs() {
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(3L, NumberUtils.max(3L, 1L, 2L));
    }

    @Test(timeout = 4000)
    public void testMinMax_ThreeInts() {
        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(3, NumberUtils.max(3, 1, 2));
    }

    @Test(timeout = 4000)
    public void testMinMax_ThreeShorts() {
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 1, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 1, (short) 2));
    }

    @Test(timeout = 4000)
    public void testMinMax_ThreeBytes() {
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 1, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 1, (byte) 2));
    }

    @Test(timeout = 4000)
    public void testMinMax_ThreeDoubles() {
        assertEquals(1.0d, NumberUtils.min(3.0d, 1.0d, 2.0d), 0.0d);
        assertEquals(3.0d, NumberUtils.max(3.0d, 1.0d, 2.0d), 0.0d);
    }

    @Test(timeout = 4000)
    public void testMinMax_ThreeFloats() {
        assertEquals(1.0f, NumberUtils.min(3.0f, 1.0f, 2.0f), 0.0f);
        assertEquals(3.0f, NumberUtils.max(3.0f, 1.0f, 2.0f), 0.0f);
    }

    @Test(timeout = 4000)
    public void testMinMax_ThreeDoublesWithNaN() {
        assertTrue(Double.isNaN(NumberUtils.min(1.0d, Double.NaN, 2.0d)));
        assertTrue(Double.isNaN(NumberUtils.max(1.0d, Double.NaN, 2.0d)));
    }

    // =====================================================================
    // Constant field sanity checks
    // =====================================================================

    @Test(timeout = 4000)
    public void testReusableConstants() {
        assertEquals(Long.valueOf(0L), NumberUtils.LONG_ZERO);
        assertEquals(Long.valueOf(1L), NumberUtils.LONG_ONE);
        assertEquals(Long.valueOf(-1L), NumberUtils.LONG_MINUS_ONE);
        assertEquals(Integer.valueOf(0), NumberUtils.INTEGER_ZERO);
        assertEquals(Integer.valueOf(1), NumberUtils.INTEGER_ONE);
        assertEquals(Integer.valueOf(-1), NumberUtils.INTEGER_MINUS_ONE);
        assertEquals(Short.valueOf((short) 0), NumberUtils.SHORT_ZERO);
        assertEquals(Byte.valueOf((byte) 0), NumberUtils.BYTE_ZERO);
        assertEquals(Double.valueOf(0.0d), NumberUtils.DOUBLE_ZERO);
        assertEquals(Float.valueOf(0.0f), NumberUtils.FLOAT_ZERO);
    }

    @Test(timeout = 4000)
    public void testConstructor_Instantiable() {
        NumberUtils nu = new NumberUtils();
        assertNotNull(nu);
    }
}