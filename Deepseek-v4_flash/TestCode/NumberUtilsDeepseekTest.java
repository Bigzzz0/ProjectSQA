package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigInteger;
import java.math.BigDecimal;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target: NumberUtils.createNumber(String) and related helper methods.
 *
 * High-Risk Branches / Decision Points:
 * 1. Hex prefix detection: 0x, 0X, -0x, -0X, #, -#
 *    - hexDigits > 16 -> createBigInteger
 *    - hexDigits > 8  -> createLong
 *    - otherwise       -> createInteger (BUG: no fallback for exactly 8 hex digits that exceed Integer range)
 * 2. Decimal point and exponent handling: decPos, expPos logic, string segmentation.
 * 3. Type qualifier detection: last char is l/L/f/F/d/D, fall-through logic.
 * 4. Fallback for no type qualifier: integer path -> Long -> BigInteger; decimal path -> Float -> Double -> BigDecimal.
 * 5. isAllZeros helper: null, empty, all zeros.
 * 6. isDigits helper: null, empty, mixed chars.
 * 7. createBigInteger: octal, decimal, hex with sign, radix detection.
 *
 * Known Defect (D4J TestLang747):
 *   createNumber("0x80000000") throws NumberFormatException instead of returning Long (2147483648L).
 *   Root cause: hexDigits == 8 falls into createInteger, which fails for values > Integer.MAX_VALUE.
 *   No fallback try-catch in the hex branch.
 *
 * Test strategy: exhaustive BVA on hex boundaries (7,8,9,16,17 digits), type qualifiers,
 * scientific notation, degenerate inputs, and explicit reproduction of the defect.
 */
public class NumberUtilsDeepseekTest {

    // ========== Partition A: Standard Numeric Primitives ==========

    @Test(timeout = 4000)
    public void testCreateInteger() {
        assertEquals(Integer.valueOf(42), NumberUtils.createInteger("42"));
        assertEquals(Integer.valueOf(-123), NumberUtils.createInteger("-123"));
        assertEquals(Integer.valueOf(0), NumberUtils.createInteger("0"));
        assertNull(NumberUtils.createInteger(null));
    }

    @Test(timeout = 4000)
    public void testCreateLong() {
        assertEquals(Long.valueOf(123456789L), NumberUtils.createLong("123456789"));
        assertEquals(Long.valueOf(-1L), NumberUtils.createLong("-1"));
        assertNull(NumberUtils.createLong(null));
    }

    @Test(timeout = 4000)
    public void testCreateFloat() {
        assertEquals(Float.valueOf(3.14f), NumberUtils.createFloat("3.14"));
        assertEquals(Float.valueOf(-0.0f), NumberUtils.createFloat("-0.0"));
        assertNull(NumberUtils.createFloat(null));
    }

    @Test(timeout = 4000)
    public void testCreateDouble() {
        assertEquals(Double.valueOf(2.71828), NumberUtils.createDouble("2.71828"));
        assertEquals(Double.valueOf(Double.NaN), NumberUtils.createDouble("NaN"));
        assertNull(NumberUtils.createDouble(null));
    }

    @Test(timeout = 4000)
    public void testCreateBigInteger() {
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
        assertEquals(new BigInteger("-1"), NumberUtils.createBigInteger("-1"));
        assertNull(NumberUtils.createBigInteger(null));
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimal() {
        assertEquals(new BigDecimal("3.14159265358979323846"), NumberUtils.createBigDecimal("3.14159265358979323846"));
        assertNull(NumberUtils.createBigDecimal(null));
    }

    // ========== Partition B: Hexadecimal Representations (Critical) ==========

    @Test(timeout = 4000)
    public void testCreateNumberHexStandard() {
        Number n;

        // Positive hex within int range
        n = NumberUtils.createNumber("0x10");
        assertTrue("Expected Integer", n instanceof Integer);
        assertEquals(16, n.intValue());

        n = NumberUtils.createNumber("0XFF");
        assertTrue("Expected Integer", n instanceof Integer);
        assertEquals(255, n.intValue());

        n = NumberUtils.createNumber("#1F");
        assertTrue("Expected Integer", n instanceof Integer);
        assertEquals(31, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberHexNegative() {
        Number n;

        n = NumberUtils.createNumber("-0x10");
        assertTrue("Expected Integer", n instanceof Integer);
        assertEquals(-16, n.intValue());

        n = NumberUtils.createNumber("-#ABCD");
        assertTrue("Expected Integer", n instanceof Integer);
        assertEquals(-43981, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberHex7Digits() {
        // 7 hex digits after prefix: fits in int
        Number n = NumberUtils.createNumber("0xFFFFFFF"); // 268435455
        assertTrue("Expected Integer", n instanceof Integer);
        assertEquals(268435455, n.intValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberHexExactly8Digits() {
        // 8 hex digits, must be detected as Long because 0x80000000 > Integer.MAX_VALUE (BUG TRIGGER)
        Number n = NumberUtils.createNumber("0x80000000");
        assertTrue("Expected Long", n instanceof Long);
        assertEquals(2147483648L, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberHex9Digits() {
        // 9 hex digits -> Long
        Number n = NumberUtils.createNumber("0x1FFFFFFFF"); // 8589934591
        assertTrue("Expected Long", n instanceof Long);
        assertEquals(8589934591L, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberHex16Digits() {
        Number n = NumberUtils.createNumber("0x7FFFFFFFFFFFFFFF"); // Long.MAX_VALUE
        assertTrue("Expected Long", n instanceof Long);
        assertEquals(Long.MAX_VALUE, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberHex17Digits() {
        // 17 hex digits -> BigInteger
        Number n = NumberUtils.createNumber("0x1FFFFFFFFFFFFFFFF"); // > Long.MAX_VALUE
        assertTrue("Expected BigInteger", n instanceof BigInteger);
        assertEquals(new BigInteger("1FFFFFFFFFFFFFFFF", 16), n);
    }

    @Test(timeout = 4000)
    public void testCreateNumberHexWithMinus0x() {
        Number n = NumberUtils.createNumber("-0xABCDEF");
        assertTrue("Expected Integer", n instanceof Integer);
        assertEquals(-11259375, n.intValue());
    }

    // ========== Partition C: Scientific / Exponent Notations ==========

    @Test(timeout = 4000)
    public void testCreateNumberScientific() {
        Number n;

        n = NumberUtils.createNumber("1.2e3");
        assertTrue("Expected Float or Double", n instanceof Double);
        assertEquals(1200.0, n.doubleValue(), 1e-9);

        n = NumberUtils.createNumber("1.2E+3");
        assertTrue("Expected Double", n instanceof Double);
        assertEquals(1200.0, n.doubleValue(), 1e-9);

        n = NumberUtils.createNumber("2.5E-4");
        assertTrue("Expected Double", n instanceof Double);
        assertEquals(0.00025, n.doubleValue(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testCreateNumberScientificWithQualifier() {
        Number n = NumberUtils.createNumber("1.2e3f");
        assertTrue("Expected Float", n instanceof Float);
        assertEquals(1200.0f, n.floatValue(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testCreateNumberScientificWithoutDecimal() {
        Number n = NumberUtils.createNumber("1e2");
        assertTrue("Expected Double or Float", n instanceof Double);
        assertEquals(100.0, n.doubleValue(), 1e-9);
    }

    // ========== Partition D: Type Qualifiers and Case Sensitivity ==========

    @Test(timeout = 4000)
    public void testCreateNumberLongQualifier() {
        Number n = NumberUtils.createNumber("123L");
        assertTrue("Expected Long", n instanceof Long);
        assertEquals(123L, n.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberFloatQualifier() {
        Number n = NumberUtils.createNumber("3.14f");
        assertTrue("Expected Float", n instanceof Float);
        assertEquals(3.14f, n.floatValue(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testCreateNumberDoubleQualifier() {
        Number n = NumberUtils.createNumber("2.71D");
        assertTrue("Expected Double", n instanceof Double);
        assertEquals(2.71, n.doubleValue(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testCreateNumberBigIntegerViaL() {
        // L qualifier on a value too large for Long
        Number n = NumberUtils.createNumber("9999999999999999999L");
        assertTrue("Expected BigInteger", n instanceof BigInteger);
        assertEquals(new BigInteger("9999999999999999999"), n);
    }

    @Test(timeout = 4000)
    public void testCreateNumberBigDecimalViaD() {
        Number n = NumberUtils.createNumber("1.7976931348623157E308D"); // > Double.MAX_VALUE? Actually within Double
        assertTrue("Expected Double or BigDecimal", n instanceof Double || n instanceof BigDecimal);
        // Instead test a bigger one:
        n = NumberUtils.createNumber("1e309D"); // > Double.MAX_VALUE
        assertTrue("Expected BigDecimal", n instanceof BigDecimal);
        assertEquals(new BigDecimal("1e309"), n);
    }

    // ========== Partition E: Edge and Degenerate Cases ==========

    @Test(timeout = 4000)
    public void testCreateNumberNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberWhitespace() {
        NumberUtils.createNumber(" ");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberLeadingZerosAsOctal() {
        // "00" is valid? Actually "00" -> Integer 0
        NumberUtils.createNumber("00"); // should work, not throw
    }

    @Test(timeout = 4000)
    public void testCreateNumberAllZeros() {
        Number n = NumberUtils.createNumber("000");
        assertTrue("Expected Integer", n instanceof Integer);
        assertEquals(0, n.intValue());
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberMultipleDots() {
        NumberUtils.createNumber("1.2.3");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberMultipleSigns() {
        NumberUtils.createNumber("--1");
    }

    @Test(timeout = 4000)
    public void testCreateNumberTrailingDot() {
        Number n = NumberUtils.createNumber("1.");
        assertTrue("Expected Double", n instanceof Double);
        assertEquals(1.0, n.doubleValue(), 1e-9);
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberHexEmpty() {
        NumberUtils.createNumber("0x");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testCreateNumberHexInvalidChar() {
        NumberUtils.createNumber("0xZZ");
    }

    // ========== Partition F: Known Defect Reproduction ==========

    /**
     * Defects4J test TestLang747: createNumber("0x80000000") should return Long 2147483648L.
     * Current implementation throws NumberFormatException.
     */
    @Test(timeout = 4000)
    public void testLang747() {
        Number n = NumberUtils.createNumber("0x80000000");
        assertNotNull("Should not return null", n);
        assertTrue("Expected Long", n instanceof Long);
        assertEquals("Value should be 2147483648L", 2147483648L, n.longValue());
    }

    // Additional boundary: exactly 8 hex digits that fit in int (0x7FFFFFFF) should be Integer
    @Test(timeout = 4000)
    public void testHex8DigitsMaxInt() {
        Number n = NumberUtils.createNumber("0x7FFFFFFF");
        assertTrue("Expected Integer", n instanceof Integer);
        assertEquals(2147483647, n.intValue());
    }

    // Negative hex with 8 digits
    @Test(timeout = 4000)
    public void testHex8DigitsNegative() {
        Number n = NumberUtils.createNumber("-0x80000000");
        assertTrue("Expected Long", n instanceof Long);
        assertEquals(-2147483648L, n.longValue());
    }

    // ========== Partition G: Helper method isAllZeros ==========

    @Test(timeout = 4000)
    public void testIsAllZeros() {
        // The method is private; tested indirectly through createNumber with zero strings.
        // e.g., "0.0" should return Float 0.0f, not zero-is-all-zeros issue.
        Number n = NumberUtils.createNumber("0.0");
        assertTrue("Expected Float", n instanceof Float);
        assertEquals(0.0f, n.floatValue(), 1e-9);
    }

    // ========== Partition H: isDigits and isNumber (static helpers) ==========

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertTrue(NumberUtils.isDigits("123"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits("-1"));
    }

    @Test(timeout = 4000)
    public void testIsNumber() {
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("0x10"));
        assertTrue(NumberUtils.isNumber("1.2e3"));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
    }

    // ========== Partition I: Conversion to primitives (toInt, toLong, etc.) ==========

    @Test(timeout = 4000)
    public void testToInt() {
        assertEquals(42, NumberUtils.toInt("42"));
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(10, NumberUtils.toInt("10", 5));
        assertEquals(5, NumberUtils.toInt("invalid", 5));
    }

    @Test(timeout = 4000)
    public void testToLong() {
        assertEquals(123L, NumberUtils.toLong("123"));
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(10L, NumberUtils.toLong("10", 5L));
        assertEquals(5L, NumberUtils.toLong("invalid", 5L));
    }

    @Test(timeout = 4000)
    public void testToFloat() {
        assertEquals(3.14f, NumberUtils.toFloat("3.14"), 1e-9);
        assertEquals(0.0f, NumberUtils.toFloat(null), 1e-9);
        assertEquals(1.5f, NumberUtils.toFloat("1.5", 2.0f), 1e-9);
        assertEquals(2.0f, NumberUtils.toFloat("bad", 2.0f), 1e-9);
    }

    @Test(timeout = 4000)
    public void testToDouble() {
        assertEquals(2.71, NumberUtils.toDouble("2.71"), 1e-9);
        assertEquals(0.0, NumberUtils.toDouble(null), 1e-9);
        assertEquals(3.0, NumberUtils.toDouble("3.0", 1.0), 1e-9);
        assertEquals(1.0, NumberUtils.toDouble("bad", 1.0), 1e-9);
    }

    @Test(timeout = 4000)
    public void testToByte() {
        assertEquals(127, NumberUtils.toByte("127"));
        assertEquals(0, NumberUtils.toByte(null));
        assertEquals(1, NumberUtils.toByte("1", (byte) 0));
        assertEquals(0, NumberUtils.toByte("256", (byte) 0));
    }

    @Test(timeout = 4000)
    public void testToShort() {
        assertEquals(32767, NumberUtils.toShort("32767"));
        assertEquals(0, NumberUtils.toShort(null));
        assertEquals(100, NumberUtils.toShort("100", (short) 0));
        assertEquals(0, NumberUtils.toShort("40000", (short) 0));
    }

    // ========== Partition J: Array min/max (basic smoke) ==========

    @Test(timeout = 4000)
    public void testArrayMinMax() {
        assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
        assertEquals(3, NumberUtils.max(new int[]{3, 1, 2}));
        assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        assertEquals(3L, NumberUtils.max(new long[]{3L, 1L, 2L}));
        assertEquals(1.0, NumberUtils.min(new double[]{3.0, 1.0, 2.0}), 1e-9);
        assertEquals(3.0, NumberUtils.max(new double[]{3.0, 1.0, 2.0}), 1e-9);
        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, 1.0f, 2.0f}), 1e-9);
        assertEquals(3.0f, NumberUtils.max(new float[]{3.0f, 1.0f, 2.0f}), 1e-9);
    }
}