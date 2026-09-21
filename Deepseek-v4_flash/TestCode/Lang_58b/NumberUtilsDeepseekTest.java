package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * White-box test suite for NumberUtils targeting maximum line/branch coverage
 * and the known defect (testLang300: "1l" should be valid).
 *
 * [Branch & Defect Analysis Matrix]
 * - stringToInt: null, empty, invalid, valid, boundary values (Integer.MAX_VALUE, MIN_VALUE)
 * - createNumber: null, empty, "--", hex (0x, -0x), type suffixes (l, L, f, F, d, D),
 *   decimal point, exponent, allZeros logic, overflow to larger types, invalid formats
 * - createFloat/Double/Integer/Long/BigInteger/BigDecimal: valid/invalid strings
 * - minimum/maximum: int/long, equal values, negative, boundary (Integer.MAX_VALUE, MIN_VALUE)
 * - compare(double/float): NaN, infinity, zero vs negative zero, normal values
 * - isDigits: null, empty, digits, non-digits
 * - isNumber: null, empty, hex, decimal, exponent, type qualifiers, invalid patterns
 * - Defect: "1l" (lowercase L) must be accepted as a valid long number.
 */
public class NumberUtilsDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testStringToIntValid() {
        assertEquals(42, NumberUtils.stringToInt("42"));
        assertEquals(0, NumberUtils.stringToInt("0"));
        assertEquals(-1, NumberUtils.stringToInt("-1"));
        assertEquals(Integer.MAX_VALUE, NumberUtils.stringToInt("2147483647"));
        assertEquals(Integer.MIN_VALUE, NumberUtils.stringToInt("-2147483648"));
    }

    @Test(timeout = 4000)
    public void testStringToIntWithDefault() {
        assertEquals(5, NumberUtils.stringToInt("abc", 5));
        assertEquals(0, NumberUtils.stringToInt(null, 0));
        assertEquals(10, NumberUtils.stringToInt("10", 99));
        assertEquals(99, NumberUtils.stringToInt("", 99));
    }

    @Test(timeout = 4000)
    public void testCreateNumberNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(timeout = 4000)
    public void testCreateNumberEmpty() {
        try {
            NumberUtils.createNumber("");
            fail("Expected NumberFormatException for empty string");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberDoubleDash() {
        assertNull(NumberUtils.createNumber("--1"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberHex() {
        assertEquals(Integer.valueOf(255), NumberUtils.createNumber("0xFF"));
        assertEquals(Integer.valueOf(-255), NumberUtils.createNumber("-0xFF"));
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0x0"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberLongSuffix() {
        // Defect: lowercase 'l' must be accepted
        assertEquals(Long.valueOf(1L), NumberUtils.createNumber("1l"));
        assertEquals(Long.valueOf(1L), NumberUtils.createNumber("1L"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(-456L), NumberUtils.createNumber("-456l"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberFloatSuffix() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5f"));
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5F"));
        assertEquals(Float.valueOf(-2.0f), NumberUtils.createNumber("-2.0f"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberDoubleSuffix() {
        assertEquals(Double.valueOf(3.14), NumberUtils.createNumber("3.14d"));
        assertEquals(Double.valueOf(3.14), NumberUtils.createNumber("3.14D"));
        assertEquals(Double.valueOf(-0.5), NumberUtils.createNumber("-0.5d"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberInteger() {
        assertEquals(Integer.valueOf(42), NumberUtils.createNumber("42"));
        assertEquals(Integer.valueOf(-42), NumberUtils.createNumber("-42"));
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberLong() {
        assertEquals(Long.valueOf(2147483648L), NumberUtils.createNumber("2147483648"));
        assertEquals(Long.valueOf(-2147483649L), NumberUtils.createNumber("-2147483649"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberBigInteger() {
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createNumber("12345678901234567890"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberFloat() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5"));
        assertEquals(Float.valueOf(1e10f), NumberUtils.createNumber("1e10"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberDouble() {
        assertEquals(Double.valueOf(1.2345678901234567), NumberUtils.createNumber("1.2345678901234567"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberBigDecimal() {
        assertEquals(new BigDecimal("1.2345678901234567890123456789"), NumberUtils.createNumber("1.2345678901234567890123456789"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberExponent() {
        assertEquals(Double.valueOf(1.5e2), NumberUtils.createNumber("1.5e2"));
        assertEquals(Double.valueOf(1.5E-2), NumberUtils.createNumber("1.5E-2"));
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
            NumberUtils.createNumber("1.2.3");
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
            NumberUtils.createNumber("1Lg");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testStringToIntBoundary() {
        assertEquals(Integer.MAX_VALUE, NumberUtils.stringToInt("2147483647"));
        assertEquals(Integer.MIN_VALUE, NumberUtils.stringToInt("-2147483648"));
        assertEquals(0, NumberUtils.stringToInt("2147483648")); // overflow -> default 0
        assertEquals(0, NumberUtils.stringToInt("-2147483649"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberBoundary() {
        // Very large number -> BigInteger
        assertEquals(new BigInteger("99999999999999999999999999999999999999"), NumberUtils.createNumber("99999999999999999999999999999999999999"));
        // Very small double
        assertEquals(Double.valueOf(Double.MIN_VALUE), NumberUtils.createNumber(String.valueOf(Double.MIN_VALUE)));
        // Infinity? Not valid as input string, but createFloat returns Infinity for too large
        assertEquals(Float.POSITIVE_INFINITY, NumberUtils.createFloat("1e40"));
        assertEquals(Double.POSITIVE_INFINITY, NumberUtils.createDouble("1e400"));
    }

    @Test(timeout = 4000)
    public void testMinimumMaximumBoundary() {
        assertEquals(Integer.MAX_VALUE, NumberUtils.maximum(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, NumberUtils.minimum(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE));
        assertEquals(Long.MAX_VALUE, NumberUtils.maximum(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, NumberUtils.minimum(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testLang300Defect() {
        // The known defect: "1l" (lowercase L) should be valid and return Long(1)
        Number result = NumberUtils.createNumber("1l");
        assertNotNull("createNumber(\"1l\") should not return null", result);
        assertTrue("Result should be a Long", result instanceof Long);
        assertEquals(1L, result.longValue());
    }

    @Test(timeout = 4000)
    public void testCreateNumberAllZerosEdge() {
        // "0.0f" -> Float 0.0, but allZeros true so should return Float
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));
        // "0.0" -> Float 0.0
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0"));
        // "0.0001f" -> Float 0.0001 (not all zeros)
        assertEquals(Float.valueOf(0.0001f), NumberUtils.createNumber("0.0001f"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberExponentWithSign() {
        assertEquals(Double.valueOf(1.5e+2), NumberUtils.createNumber("1.5e+2"));
        assertEquals(Double.valueOf(1.5e-2), NumberUtils.createNumber("1.5e-2"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberTypeSuffixOverflow() {
        // "1e40" as float -> Infinity, so should fall through to Double
        Number n = NumberUtils.createNumber("1e40");
        assertTrue(n instanceof Double);
        assertEquals(Double.POSITIVE_INFINITY, n.doubleValue());
        // "1e400" as double -> Infinity, fall through to BigDecimal
        Number n2 = NumberUtils.createNumber("1e400");
        assertTrue(n2 instanceof BigDecimal);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumberInvalidHex() {
        NumberUtils.createNumber("0xGG");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumberInvalidExponentPosition() {
        NumberUtils.createNumber("1e2.5");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateNumberInvalidSuffix() {
        NumberUtils.createNumber("1x");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateFloatInvalid() {
        NumberUtils.createFloat("abc");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateDoubleInvalid() {
        NumberUtils.createDouble("abc");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateIntegerInvalid() {
        NumberUtils.createInteger("abc");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateLongInvalid() {
        NumberUtils.createLong("abc");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateBigIntegerInvalid() {
        NumberUtils.createBigInteger("abc");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testCreateBigDecimalInvalid() {
        NumberUtils.createBigDecimal("abc");
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testConstructor() {
        // Constructor is public for JavaBean tools, just ensure it doesn't throw
        new NumberUtils();
    }

    @Test(timeout = 4000)
    public void testMinimumInt() {
        assertEquals(1, NumberUtils.minimum(1, 2, 3));
        assertEquals(1, NumberUtils.minimum(3, 1, 2));
        assertEquals(1, NumberUtils.minimum(2, 3, 1));
        assertEquals(-5, NumberUtils.minimum(-5, 0, 10));
        assertEquals(Integer.MIN_VALUE, NumberUtils.minimum(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testMaximumInt() {
        assertEquals(3, NumberUtils.maximum(1, 2, 3));
        assertEquals(3, NumberUtils.maximum(3, 1, 2));
        assertEquals(3, NumberUtils.maximum(2, 3, 1));
        assertEquals(10, NumberUtils.maximum(-5, 0, 10));
        assertEquals(Integer.MAX_VALUE, NumberUtils.maximum(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testMinimumLong() {
        assertEquals(1L, NumberUtils.minimum(1L, 2L, 3L));
        assertEquals(Long.MIN_VALUE, NumberUtils.minimum(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testMaximumLong() {
        assertEquals(3L, NumberUtils.maximum(1L, 2L, 3L));
        assertEquals(Long.MAX_VALUE, NumberUtils.maximum(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testCompareDouble() {
        assertEquals(0, NumberUtils.compare(0.0, 0.0));
        assertEquals(0, NumberUtils.compare(-0.0, -0.0));
        assertEquals(-1, NumberUtils.compare(-0.0, 0.0));
        assertEquals(1, NumberUtils.compare(0.0, -0.0));
        assertEquals(-1, NumberUtils.compare(1.0, 2.0));
        assertEquals(1, NumberUtils.compare(2.0, 1.0));
        assertEquals(0, NumberUtils.compare(Double.NaN, Double.NaN));
        assertEquals(1, NumberUtils.compare(Double.POSITIVE_INFINITY, Double.MAX_VALUE));
        assertEquals(-1, NumberUtils.compare(Double.NEGATIVE_INFINITY, Double.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testCompareFloat() {
        assertEquals(0, NumberUtils.compare(0.0f, 0.0f));
        assertEquals(0, NumberUtils.compare(-0.0f, -0.0f));
        assertEquals(-1, NumberUtils.compare(-0.0f, 0.0f));
        assertEquals(1, NumberUtils.compare(0.0f, -0.0f));
        assertEquals(-1, NumberUtils.compare(1.0f, 2.0f));
        assertEquals(1, NumberUtils.compare(2.0f, 1.0f));
        assertEquals(0, NumberUtils.compare(Float.NaN, Float.NaN));
        assertEquals(1, NumberUtils.compare(Float.POSITIVE_INFINITY, Float.MAX_VALUE));
        assertEquals(-1, NumberUtils.compare(Float.NEGATIVE_INFINITY, Float.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("123"));
        assertTrue(NumberUtils.isDigits("0"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertFalse(NumberUtils.isDigits("12a"));
    }

    @Test(timeout = 4000)
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("0x1A"));
        assertTrue(NumberUtils.isNumber("-0x1A"));
        assertTrue(NumberUtils.isNumber("1.5"));
        assertTrue(NumberUtils.isNumber("1.5e2"));
        assertTrue(NumberUtils.isNumber("1.5E-2"));
        assertTrue(NumberUtils.isNumber("1L"));
        assertTrue(NumberUtils.isNumber("1l")); // lowercase L should be valid
        assertTrue(NumberUtils.isNumber("1f"));
        assertTrue(NumberUtils.isNumber("1D"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e"));
        assertFalse(NumberUtils.isNumber("1e+"));
        assertFalse(NumberUtils.isNumber("1Lg"));
        assertFalse(NumberUtils.isNumber("--1"));
    }
}