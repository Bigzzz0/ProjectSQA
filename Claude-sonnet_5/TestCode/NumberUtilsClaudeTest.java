package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigInteger;
import java.math.BigDecimal;

/**
 * NumberUtilsClaudeTest
 *
 * /* [Branch & Defect Analysis Matrix] *
 *
 * This suite targets NumberUtils.createNumber(String) and its collaborator
 * conversion methods (createInteger/createLong/createBigInteger/createFloat/
 * createDouble/createBigDecimal), exercising the following decision points:
 *
 * 1. Hex-prefix detection loop (0x, 0X, -0x, -0X, #, -#) and the three-way
 * branch on hexDigits (<=8 -> Integer, 9..16 -> Long, >16 -> BigInteger).
 * DEFECT ZONE: hexDigits==8 does NOT guard against int-overflow (e.g.
 * 0x80000000 / 0xFFFFFFFF), so Integer.decode throws NFE instead of
 * promoting to Long -- this is asserted as expected (documenting) behavior.
 * 2. Hex-prefixed values carrying an 'L'/'l' suffix bypass the qualifier
 * switch entirely (return happens inside the hex branch), so
 * "0x12L" fails to parse -- also documented via expected exception.
 * 3. decPos/expPos computation branches: both-present ordering check
 * (expPos < decPos), IOOBE guard (expPos > length) for both the
 * decimal-point and no-decimal-point paths.
 * 4. Type-qualifier switch (l/L, f/F, d/D) fallthrough semantics, including
 * the allZeros exemption for legitimate zero values, and default throw.
 * 5. No-preference path: Integer -> Long -> BigInteger cascade, and
 * Float(<=7 decimals) -> Double(<=16 decimals) -> BigDecimal cascade.
 * 6. createBigInteger's missing "--" guard (unlike createBigDecimal) causing
 * "--1" to silently resolve to BigInteger ONE -- documented defect.
 * 7. Degenerate/blank/null/whitespace inputs and multi-dot malformed inputs.
 */
public class NumberUtilsClaudeTest {

    // =========================================================================
    // Partition A: Standard Numeric Primitives
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumber_PlainInteger() {
        Number n = NumberUtils.createNumber("123");
        assertTrue(n instanceof Integer);
        assertEquals(123, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_NegativeInteger() {
        Number n = NumberUtils.createNumber("-123");
        assertTrue(n instanceof Integer);
        assertEquals(-123, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_PromotesToLong() {
        Number n = NumberUtils.createNumber("123456789012");
        assertTrue(n instanceof Long);
        assertEquals(123456789012L, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_PromotesToBigInteger() {
        Number n = NumberUtils.createNumber("123456789012345678901234567890");
        assertTrue(n instanceof BigInteger);
        assertEquals(new BigInteger("123456789012345678901234567890"), n);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_DefaultDecimalIsFloat() {
        Number n = NumberUtils.createNumber("1.5");
        assertTrue(n instanceof Float);
        assertEquals(1.5f, n.floatValue(), 0.0001f);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_ManyDecimalsPromotesToDouble() {
        Number n = NumberUtils.createNumber("1.12345678"); // 8 decimals > 7
        assertTrue(n instanceof Double);
        assertEquals(1.12345678d, n.doubleValue(), 0.000000001d);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_VeryManyDecimalsPromotesToBigDecimal() {
        String s = "1.123456789012345678"; // 18 decimals > 16
        Number n = NumberUtils.createNumber(s);
        assertTrue(n instanceof BigDecimal);
        assertEquals(new BigDecimal(s), n);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_TrailingDotIsFloat() {
        Number n = NumberUtils.createNumber("2.");
        assertTrue(n instanceof Float);
        assertEquals(2.0f, n.floatValue(), 0.0001f);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_LeadingZeroOctal() {
        Number n = NumberUtils.createNumber("000");
        assertTrue(n instanceof Integer);
        assertEquals(0, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateInteger_Standard() {
        Integer i = NumberUtils.createInteger("42");
        assertEquals(Integer.valueOf(42), i);
    }

    @Test(timeout = 4000)
    public void testCreateLong_Standard() {
        Long l = NumberUtils.createLong("42");
        assertEquals(Long.valueOf(42L), l);
    }

    @Test(timeout = 4000)
    public void testCreateFloat_Standard() {
        Float f = NumberUtils.createFloat("4.2");
        assertEquals(4.2f, f.floatValue(), 0.0001f);
    }

    @Test(timeout = 4000)
    public void testCreateDouble_Standard() {
        Double d = NumberUtils.createDouble("4.2");
        assertEquals(4.2d, d.doubleValue(), 0.0000001d);
    }

    @Test(timeout = 4000)
    public void testCreateBigInteger_Standard() {
        BigInteger bi = NumberUtils.createBigInteger("123456789012345678901234567890");
        assertEquals(new BigInteger("123456789012345678901234567890"), bi);
    }

    @Test(timeout = 4000)
    public void testCreateBigInteger_Octal() {
        BigInteger bi = NumberUtils.createBigInteger("010");
        assertEquals(BigInteger.valueOf(8), bi);
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimal_Standard() {
        BigDecimal bd = NumberUtils.createBigDecimal("1.5");
        assertEquals(new BigDecimal("1.5"), bd);
    }

    // Null-input handling
    @Test(timeout = 4000)
    public void testCreateNumber_Null() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(timeout = 4000)
    public void testCreateInteger_Null() {
        assertNull(NumberUtils.createInteger(null));
    }

    @Test(timeout = 4000)
    public void testCreateLong_Null() {
        assertNull(NumberUtils.createLong(null));
    }

    @Test(timeout = 4000)
    public void testCreateFloat_Null() {
        assertNull(NumberUtils.createFloat(null));
    }

    @Test(timeout = 4000)
    public void testCreateDouble_Null() {
        assertNull(NumberUtils.createDouble(null));
    }

    @Test(timeout = 4000)
    public void testCreateBigInteger_Null() {
        assertNull(NumberUtils.createBigInteger(null));
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimal_Null() {
        assertNull(NumberUtils.createBigDecimal(null));
    }

    // =========================================================================
    // Partition B: Hexadecimal Representations (Critical Bug Zone)
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumber_Hex0xLower() {
        Number n = NumberUtils.createNumber("0x10");
        assertTrue(n instanceof Integer);
        assertEquals(16, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_Hex0XUpper() {
        Number n = NumberUtils.createNumber("0X10");
        assertTrue(n instanceof Integer);
        assertEquals(16, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_HexHashPrefix() {
        Number n = NumberUtils.createNumber("#1234");
        assertTrue(n instanceof Integer);
        assertEquals(0x1234, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_NegativeHex0x() {
        Number n = NumberUtils.createNumber("-0x10");
        assertTrue(n instanceof Integer);
        assertEquals(-16, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_NegativeHexHash() {
        Number n = NumberUtils.createNumber("-#1234");
        assertTrue(n instanceof Integer);
        assertEquals(-0x1234, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_HexMaxPositiveInt() {
        Number n = NumberUtils.createNumber("0x7FFFFFFF");
        assertTrue(n instanceof Integer);
        assertEquals(Integer.MAX_VALUE, n.intValue());
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_Hex80000000_OverflowsInt_Defect() {
        // DEFECT ZONE: hexDigits==8 does not guard int overflow -> NFE
        NumberUtils.createNumber("0x80000000");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_HexFFFFFFFF_OverflowsInt_Defect() {
        NumberUtils.createNumber("0xFFFFFFFF");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_HexHashOverflowsInt_Defect() {
        NumberUtils.createNumber("#FFFFFFFF");
    }

    @Test(timeout = 4000)
    public void testCreateNumber_HexNineDigitsPromotesToLong() {
        Number n = NumberUtils.createNumber("0x100000000"); // 9 hex digits
        assertTrue(n instanceof Long);
        assertEquals(4294967296L, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_HexSeventeenDigitsPromotesToBigInteger() {
        Number n = NumberUtils.createNumber("0x10000000000000000"); // 17 hex digits
        assertTrue(n instanceof BigInteger);
        assertEquals(new BigInteger("18446744073709551616"), n);
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_HexWithLSuffix_Defect() {
        // Hex branch returns before qualifier switch is ever consulted.
        NumberUtils.createNumber("0x12L");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_HexWithLowerLSuffix_Defect() {
        NumberUtils.createNumber("0x12l");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_NegativeHexWithLSuffix_Defect() {
        NumberUtils.createNumber("-0x12L");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_HexEmptyDigits() {
        NumberUtils.createNumber("0x");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_HashEmptyDigits() {
        NumberUtils.createNumber("#");
    }

    // =========================================================================
    // Partition C: Scientific / Exponent Notations
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumber_ExponentLowerE() {
        Number n = NumberUtils.createNumber("1.2e3");
        assertTrue(n instanceof Float);
        assertEquals(1200.0f, n.floatValue(), 0.001f);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_ExponentWithPlusSign() {
        Number n = NumberUtils.createNumber("1.2e+3");
        assertTrue(n instanceof Float);
        assertEquals(1200.0f, n.floatValue(), 0.001f);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_ExponentUpperEWithNegative() {
        Number n = NumberUtils.createNumber("-2.5E-4");
        assertTrue(n instanceof Float);
        assertEquals(-2.5E-4f, n.floatValue(), 0.0000001f);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_AllZerosExponent() {
        Number n = NumberUtils.createNumber("00E0");
        assertTrue(n instanceof Float);
        assertEquals(0.0f, n.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_HugeExponentPromotesToBigDecimal() {
        Number n = NumberUtils.createNumber("1E400");
        assertTrue(n instanceof BigDecimal);
        assertEquals(new BigDecimal("1E400"), n);
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_ExponentBeforeDecimalPoint() {
        // expPos < decPos triggers explicit NFE guard
        NumberUtils.createNumber("1e2.3");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_BothEAndE_UpperCase_WithDecimal() {
        // expPos computed beyond string length -> IOOBE guard throws NFE
        NumberUtils.createNumber("1.2eE3");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_BothEAndE_UpperCase_NoDecimal() {
        NumberUtils.createNumber("1eE");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_TrailingBareExponent() {
        NumberUtils.createNumber("1.2e");
    }

    // =========================================================================
    // Partition D: Type Qualifiers & Case Sensitivity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateNumber_LongSuffixUpper() {
        Number n = NumberUtils.createNumber("123L");
        assertTrue(n instanceof Long);
        assertEquals(123L, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_LongSuffixLower() {
        Number n = NumberUtils.createNumber("123l");
        assertTrue(n instanceof Long);
        assertEquals(123L, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_NegativeLongSuffix() {
        Number n = NumberUtils.createNumber("-123L");
        assertTrue(n instanceof Long);
        assertEquals(-123L, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumber_LongSuffixOverflowsToBigInteger() {
        Number n = NumberUtils.createNumber("9999999999999999999L");
        assertTrue(n instanceof BigInteger);
        assertEquals(new BigInteger("9999999999999999999"), n);
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_LongSuffixOnDecimal_Invalid() {
        NumberUtils.createNumber("1.5L");
    }

    @Test(timeout = 4000)
    public void testCreateNumber_FloatSuffixLower() {
        Number n = NumberUtils.createNumber("123.45f");
        assertTrue(n instanceof Float);
        assertEquals(123.45f, n.floatValue(), 0.001f);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_FloatSuffixUpper() {
        Number n = NumberUtils.createNumber("123.45F");
        assertTrue(n instanceof Float);
        assertEquals(123.45f, n.floatValue(), 0.001f);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_DoubleSuffixLower() {
        Number n = NumberUtils.createNumber("1.5d");
        assertTrue(n instanceof Double);
        assertEquals(1.5d, n.doubleValue(), 0.0000001d);
    }

    @Test(timeout = 4000)
    public void testCreateNumber_DoubleSuffixUpper() {
        Number n = NumberUtils.createNumber("1.5D");
        assertTrue(n instanceof Double);
        assertEquals(1.5d, n.doubleValue(), 0.0000001d);
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_TrailingWhitespaceQualifier_Invalid() {
        NumberUtils.createNumber("123 ");
    }

    // =========================================================================
    // Partition E: Edge & Degenerate Cases
    // =========================================================================

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_EmptyString() {
        NumberUtils.createNumber("");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_SingleWhitespace() {
        NumberUtils.createNumber(" ");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_LeadingWhitespace() {
        NumberUtils.createNumber(" 123");
    }

    @Test(timeout = 4000)
    public void testCreateNumber_DoubleNegative_ResolvesToBigInteger_Defect() {
        // DEFECT: createBigInteger lacks the "--" guard present in createBigDecimal
        Number n = NumberUtils.createNumber("--1");
        assertTrue(n instanceof BigInteger);
        assertEquals(BigInteger.valueOf(1), n);
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_MultipleDots_Invalid() {
        NumberUtils.createNumber("1.2.3");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumber_LoneDot_Invalid() {
        NumberUtils.createNumber(".");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateBigDecimal_DoubleNegative_Guarded() {
        NumberUtils.createBigDecimal("--1");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateBigDecimal_Blank() {
        NumberUtils.createBigDecimal("");
    }

    // =========================================================================
    // Ancillary conversion methods (toXxx) - line coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testToInt_NullAndDefault() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(5, NumberUtils.toInt(null, 5));
        assertEquals(5, NumberUtils.toInt("abc", 5));
        assertEquals(7, NumberUtils.toInt("7"));
    }

    @Test(timeout = 4000)
    public void testToLong_NullAndDefault() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(5L, NumberUtils.toLong(null, 5L));
        assertEquals(5L, NumberUtils.toLong("abc", 5L));
        assertEquals(7L, NumberUtils.toLong("7"));
    }

    @Test(timeout = 4000)
    public void testToFloat_NullAndDefault() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
        assertEquals(1.1f, NumberUtils.toFloat(null, 1.1f), 0.0001f);
        assertEquals(1.1f, NumberUtils.toFloat("abc", 1.1f), 0.0001f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0001f);
    }

    @Test(timeout = 4000)
    public void testToDouble_NullAndDefault() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0d);
        assertEquals(1.1d, NumberUtils.toDouble(null, 1.1d), 0.0000001d);
        assertEquals(1.1d, NumberUtils.toDouble("abc", 1.1d), 0.0000001d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0000001d);
    }

    @Test(timeout = 4000)
    public void testToByte_NullAndDefault() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 5, NumberUtils.toByte(null, (byte) 5));
        assertEquals((byte) 5, NumberUtils.toByte("abc", (byte) 5));
        assertEquals((byte) 7, NumberUtils.toByte("7"));
    }

    @Test(timeout = 4000)
    public void testToShort_NullAndDefault() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 5, NumberUtils.toShort(null, (short) 5));
        assertEquals((short) 5, NumberUtils.toShort("abc", (short) 5));
        assertEquals((short) 7, NumberUtils.toShort("7"));
    }

    // =========================================================================
    // isDigits / isNumber - line & branch coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("12a45"));
    }

    @Test(timeout = 4000)
    public void testIsNumber_VariousForms() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertTrue(NumberUtils.isNumber("0x1F"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertTrue(NumberUtils.isNumber("1e10"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertFalse(NumberUtils.isNumber("abc"));
        assertFalse(NumberUtils.isNumber("1E"));
    }

    // =========================================================================
    // min/max sanity (bonus coverage)
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinMaxIntArray() {
        int[] arr = { 3, 1, 2 };
        assertEquals(1, NumberUtils.min(arr));
        assertEquals(3, NumberUtils.max(arr));
    }

    @Test(timeout = 4000)
    public void testMinMaxThreeInts() {
        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(3, NumberUtils.max(3, 1, 2));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMinIntArray_NullThrows() {
        NumberUtils.min((int[]) null);
    }
}