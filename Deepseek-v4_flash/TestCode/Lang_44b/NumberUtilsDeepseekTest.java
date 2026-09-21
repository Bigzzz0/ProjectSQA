package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * White-box test suite for NumberUtils.
 * Targets all public methods, boundary conditions, and the known defect
 * (StringIndexOutOfBoundsException in isNumber for "0x" input).
 *
 * [Branch & Defect Analysis Matrix]
 * - isNumber: hex prefix "0x"/"-0x", empty/null, trailing qualifiers, exponent signs, decimal points.
 * - createNumber: null/empty, "--", hex, type qualifiers (l/L/f/F/d/D), decimal/exponent combinations.
 * - stringToInt: null, empty, invalid, boundary values.
 * - minimum/maximum: equal values, negative, extremes.
 * - compare: NaN, infinities, zero vs negative zero, normal values.
 * - isDigits: null, empty, non-digit chars.
 * - create*: valid/invalid strings, boundary values.
 */
public class NumberUtilsDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testStringToIntDefault() {
        assertEquals(0, NumberUtils.stringToInt(null));
        assertEquals(0, NumberUtils.stringToInt(""));
        assertEquals(0, NumberUtils.stringToInt("abc"));
        assertEquals(123, NumberUtils.stringToInt("123"));
        assertEquals(-456, NumberUtils.stringToInt("-456"));
        assertEquals(Integer.MAX_VALUE, NumberUtils.stringToInt("2147483647"));
        assertEquals(Integer.MIN_VALUE, NumberUtils.stringToInt("-2147483648"));
    }

    @Test(timeout = 4000)
    public void testStringToIntWithDefault() {
        assertEquals(42, NumberUtils.stringToInt(null, 42));
        assertEquals(42, NumberUtils.stringToInt("", 42));
        assertEquals(42, NumberUtils.stringToInt("xyz", 42));
        assertEquals(789, NumberUtils.stringToInt("789", 42));
        assertEquals(-1, NumberUtils.stringToInt("-1", 99));
    }

    @Test(timeout = 4000)
    public void testCreateNumberInteger() {
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0"));
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Integer.valueOf(-456), NumberUtils.createNumber("-456"));
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), NumberUtils.createNumber("2147483647"));
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), NumberUtils.createNumber("-2147483648"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberLong() {
        assertEquals(Long.valueOf(0L), NumberUtils.createNumber("0L"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(Long.MAX_VALUE), NumberUtils.createNumber("9223372036854775807L"));
        assertEquals(Long.valueOf(Long.MIN_VALUE), NumberUtils.createNumber("-9223372036854775808L"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberFloat() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createNumber("1.5f"));
        assertEquals(Float.valueOf(-2.5f), NumberUtils.createNumber("-2.5F"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));
        assertEquals(Float.valueOf(Float.MAX_VALUE), NumberUtils.createNumber("3.4028235E38f"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberDouble() {
        assertEquals(Double.valueOf(3.14), NumberUtils.createNumber("3.14d"));
        assertEquals(Double.valueOf(-0.001), NumberUtils.createNumber("-0.001D"));
        assertEquals(Double.valueOf(0.0), NumberUtils.createNumber("0.0d"));
        assertEquals(Double.valueOf(Double.MAX_VALUE), NumberUtils.createNumber("1.7976931348623157E308d"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberBigInteger() {
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createNumber("12345678901234567890"));
        assertEquals(new BigInteger("-9999999999999999999"), NumberUtils.createNumber("-9999999999999999999"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberBigDecimal() {
        assertEquals(new BigDecimal("3.14159265358979323846"), NumberUtils.createNumber("3.14159265358979323846"));
        assertEquals(new BigDecimal("-0.0000000001"), NumberUtils.createNumber("-0.0000000001"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberHex() {
        assertEquals(Integer.valueOf(0xABCD), NumberUtils.createNumber("0xABCD"));
        assertEquals(Integer.valueOf(-0x1F), NumberUtils.createNumber("-0x1F"));
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0x0"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithExponent() {
        assertEquals(Double.valueOf(1.0E10), NumberUtils.createNumber("1.0E10"));
        assertEquals(Double.valueOf(2.5e-3), NumberUtils.createNumber("2.5e-3"));
        assertEquals(Float.valueOf(1.0e5f), NumberUtils.createNumber("1.0e5f"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithLeadingZeros() {
        assertEquals(Integer.valueOf(5), NumberUtils.createNumber("05"));
        assertEquals(Long.valueOf(5L), NumberUtils.createNumber("05L"));
    }

    @Test(timeout = 4000)
    public void testMinimumLong() {
        assertEquals(1L, NumberUtils.minimum(3L, 2L, 1L));
        assertEquals(1L, NumberUtils.minimum(1L, 2L, 3L));
        assertEquals(Long.MIN_VALUE, NumberUtils.minimum(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
        assertEquals(-5L, NumberUtils.minimum(-5L, -3L, -1L));
    }

    @Test(timeout = 4000)
    public void testMinimumInt() {
        assertEquals(1, NumberUtils.minimum(3, 2, 1));
        assertEquals(1, NumberUtils.minimum(1, 2, 3));
        assertEquals(Integer.MIN_VALUE, NumberUtils.minimum(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
        assertEquals(-5, NumberUtils.minimum(-5, -3, -1));
    }

    @Test(timeout = 4000)
    public void testMaximumLong() {
        assertEquals(3L, NumberUtils.maximum(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.maximum(3L, 2L, 1L));
        assertEquals(Long.MAX_VALUE, NumberUtils.maximum(Long.MIN_VALUE, 0L, Long.MAX_VALUE));
        assertEquals(-1L, NumberUtils.maximum(-5L, -3L, -1L));
    }

    @Test(timeout = 4000)
    public void testMaximumInt() {
        assertEquals(3, NumberUtils.maximum(1, 2, 3));
        assertEquals(3, NumberUtils.maximum(3, 2, 1));
        assertEquals(Integer.MAX_VALUE, NumberUtils.maximum(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));
        assertEquals(-1, NumberUtils.maximum(-5, -3, -1));
    }

    @Test(timeout = 4000)
    public void testCompareDouble() {
        assertEquals(-1, NumberUtils.compare(1.0, 2.0));
        assertEquals(1, NumberUtils.compare(2.0, 1.0));
        assertEquals(0, NumberUtils.compare(1.0, 1.0));
        assertEquals(0, NumberUtils.compare(Double.NaN, Double.NaN));
        assertEquals(1, NumberUtils.compare(Double.NaN, Double.POSITIVE_INFINITY));
        assertEquals(-1, NumberUtils.compare(Double.NEGATIVE_INFINITY, Double.NaN));
        assertEquals(1, NumberUtils.compare(Double.POSITIVE_INFINITY, Double.MAX_VALUE));
        assertEquals(-1, NumberUtils.compare(-0.0, 0.0));
        assertEquals(1, NumberUtils.compare(0.0, -0.0));
    }

    @Test(timeout = 4000)
    public void testCompareFloat() {
        assertEquals(-1, NumberUtils.compare(1.0f, 2.0f));
        assertEquals(1, NumberUtils.compare(2.0f, 1.0f));
        assertEquals(0, NumberUtils.compare(1.0f, 1.0f));
        assertEquals(0, NumberUtils.compare(Float.NaN, Float.NaN));
        assertEquals(1, NumberUtils.compare(Float.NaN, Float.POSITIVE_INFINITY));
        assertEquals(-1, NumberUtils.compare(Float.NEGATIVE_INFINITY, Float.NaN));
        assertEquals(1, NumberUtils.compare(Float.POSITIVE_INFINITY, Float.MAX_VALUE));
        assertEquals(-1, NumberUtils.compare(-0.0f, 0.0f));
        assertEquals(1, NumberUtils.compare(0.0f, -0.0f));
    }

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertFalse(NumberUtils.isDigits("12a"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertTrue(NumberUtils.isDigits("0"));
        assertTrue(NumberUtils.isDigits("1234567890"));
        assertTrue(NumberUtils.isDigits("000"));
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testCreateNumberNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(timeout = 4000)
    public void testCreateNumberEmpty() {
        try {
            NumberUtils.createNumber("");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberDoubleDash() {
        assertNull(NumberUtils.createNumber("--123"));
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
    public void testCreateNumberTooLargeForLong() {
        Number result = NumberUtils.createNumber("99999999999999999999L");
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger("99999999999999999999"), result);
    }

    @Test(timeout = 4000)
    public void testCreateNumberFloatUnderflow() {
        Number result = NumberUtils.createNumber("1.0E-50f");
        assertTrue(result instanceof Float);
        assertEquals(0.0f, result.floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testCreateNumberDoubleUnderflow() {
        Number result = NumberUtils.createNumber("1.0E-350d");
        assertTrue(result instanceof Double);
        assertEquals(0.0, result.doubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateNumberExponentBeforeDecimal() {
        try {
            NumberUtils.createNumber("1e2.3");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIsNumberBoundary() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("e"));
        assertFalse(NumberUtils.isNumber("+"));
        assertFalse(NumberUtils.isNumber("-"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertFalse(NumberUtils.isNumber("0xG"));
        assertFalse(NumberUtils.isNumber("123L"));
        assertFalse(NumberUtils.isNumber("123e"));
        assertFalse(NumberUtils.isNumber("123e-"));
        assertFalse(NumberUtils.isNumber("123.123.123"));
        assertFalse(NumberUtils.isNumber("12e3e4"));
        assertTrue(NumberUtils.isNumber("0"));
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("0xABCD"));
        assertTrue(NumberUtils.isNumber("-0x1F"));
        assertTrue(NumberUtils.isNumber("123.456"));
        assertTrue(NumberUtils.isNumber("1.2e3"));
        assertTrue(NumberUtils.isNumber("1.2E-3"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123f"));
        assertTrue(NumberUtils.isNumber("123D"));
        assertTrue(NumberUtils.isNumber("123.0d"));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Targets the known defect: isNumber("0x") throws StringIndexOutOfBoundsException.
     * On the fixed version, it should return false.
     */
    @Test(timeout = 4000)
    public void testLang457() {
        // This test will fail on the buggy version due to StringIndexOutOfBoundsException
        assertFalse("isNumber('0x') should return false", NumberUtils.isNumber("0x"));
    }

    @Test(timeout = 4000)
    public void testLang457NegativeHex() {
        assertFalse("isNumber('-0x') should return false", NumberUtils.isNumber("-0x"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberHexOnlyPrefix() {
        try {
            NumberUtils.createNumber("0x");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testCreateNumberInvalidExponent() {
        try {
            NumberUtils.createNumber("1e");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberInvalidDecimal() {
        try {
            NumberUtils.createNumber("1.2.3");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberInvalidQualifier() {
        try {
            NumberUtils.createNumber("123k");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberLWithDecimal() {
        try {
            NumberUtils.createNumber("1.2L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberLWithExponent() {
        try {
            NumberUtils.createNumber("1e2L");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberFTooLarge() {
        Number result = NumberUtils.createNumber("1.0E50f");
        assertTrue(result instanceof Double || result instanceof BigDecimal);
    }

    @Test(timeout = 4000)
    public void testCreateNumberDTooLarge() {
        Number result = NumberUtils.createNumber("1.0E500d");
        assertTrue(result instanceof BigDecimal);
    }

    @Test(timeout = 4000)
    public void testStringToIntOverflow() {
        assertEquals(0, NumberUtils.stringToInt("99999999999999999999"));
        assertEquals(0, NumberUtils.stringToInt("-99999999999999999999"));
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testCreateFloat() {
        assertEquals(Float.valueOf(1.5f), NumberUtils.createFloat("1.5"));
        try {
            NumberUtils.createFloat("abc");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateDouble() {
        assertEquals(Double.valueOf(3.14), NumberUtils.createDouble("3.14"));
        try {
            NumberUtils.createDouble("xyz");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateInteger() {
        assertEquals(Integer.valueOf(255), NumberUtils.createInteger("0xFF"));
        assertEquals(Integer.valueOf(10), NumberUtils.createInteger("012"));
        assertEquals(Integer.valueOf(-1), NumberUtils.createInteger("-1"));
        try {
            NumberUtils.createInteger("abc");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateLong() {
        assertEquals(Long.valueOf(123L), NumberUtils.createLong("123"));
        try {
            NumberUtils.createLong("notanumber");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateBigInteger() {
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
        try {
            NumberUtils.createBigInteger("1.2");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateBigDecimal() {
        assertEquals(new BigDecimal("3.14159"), NumberUtils.createBigDecimal("3.14159"));
        try {
            NumberUtils.createBigDecimal("abc");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructor() {
        // Constructor is public for JavaBean compliance
        NumberUtils instance = new NumberUtils();
        assertNotNull(instance);
    }
}