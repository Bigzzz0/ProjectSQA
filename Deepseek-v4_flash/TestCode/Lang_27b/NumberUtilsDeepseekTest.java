package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: NumberUtils.createNumber(String) - known StringIndexOutOfBoundsException
 * 
 * Defect Root Cause: In createNumber(), when the last character is a type qualifier (e.g., 'l', 'L', 'f', 'F', 'd', 'D'),
 * the code computes `String numeric = str.substring(0, str.length() - 1);` and then accesses `numeric.charAt(0)` 
 * without checking if `numeric` is empty. This occurs when the input string is a single character like "l", "L", "f", "F", "d", "D".
 * 
 * Key Branches Targeted:
 * 1. str == null -> return null
 * 2. StringUtils.isBlank(str) -> throw NumberFormatException
 * 3. str.startsWith("--") -> return null
 * 4. str.startsWith("0x") or "-0x" -> createInteger(str)
 * 5. Last char is digit or '.' -> no type qualifier path
 * 6. Last char is type qualifier (l, L, f, F, d, D) -> type-specific path
 *    - For 'l'/'L': check dec==null && exp==null && (numeric starts with '-' and isDigits(substring) or isDigits(numeric))
 *    - For 'f'/'F': try Float, then fall through to 'd'/'D'
 *    - For 'd'/'D': try Double, then BigDecimal
 *    - default: throw NumberFormatException
 * 7. Decimal point present vs absent
 * 8. Exponent present vs absent
 * 9. isAllZeros() logic
 * 10. isDigits() logic
 * 
 * Boundary Conditions:
 * - Empty string after removing type qualifier (e.g., "l", "L", "f", "F", "d", "D")
 * - Hex strings with "0x" prefix
 * - Strings with leading zeros
 * - Strings with exponents and type qualifiers
 * - NaN, Infinity
 * - Maximum/minimum numeric values
 * - Scientific notation
 */
public class NumberUtilsDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testCreateNumberNull() {
        assertNull("Null input should return null", NumberUtils.createNumber(null));
    }

    @Test(timeout = 4000)
    public void testCreateNumberBlank() {
        try {
            NumberUtils.createNumber("");
            fail("Expected NumberFormatException for blank string");
        } catch (NumberFormatException e) {
            // expected
        }
        try {
            NumberUtils.createNumber("   ");
            fail("Expected NumberFormatException for whitespace string");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberDoubleDash() {
        assertNull("String starting with '--' should return null", NumberUtils.createNumber("--123"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberHex() {
        assertEquals("0x1A should be 26", Integer.valueOf(26), NumberUtils.createNumber("0x1A"));
        assertEquals("-0x1A should be -26", Integer.valueOf(-26), NumberUtils.createNumber("-0x1A"));
        assertEquals("0x0 should be 0", Integer.valueOf(0), NumberUtils.createNumber("0x0"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberInteger() {
        assertEquals("123 should be Integer 123", Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals("-123 should be Integer -123", Integer.valueOf(-123), NumberUtils.createNumber("-123"));
        assertEquals("0 should be Integer 0", Integer.valueOf(0), NumberUtils.createNumber("0"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberLong() {
        assertEquals("123L should be Long 123", Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals("-123L should be Long -123", Long.valueOf(-123L), NumberUtils.createNumber("-123L"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberBigInteger() {
        assertEquals("99999999999999999999 should be BigInteger", 
                     new BigInteger("99999999999999999999"), 
                     NumberUtils.createNumber("99999999999999999999"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberFloat() {
        assertEquals("1.5f should be Float 1.5", Float.valueOf(1.5f), NumberUtils.createNumber("1.5f"));
        assertEquals("-1.5F should be Float -1.5", Float.valueOf(-1.5f), NumberUtils.createNumber("-1.5F"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberDouble() {
        assertEquals("1.5d should be Double 1.5", Double.valueOf(1.5d), NumberUtils.createNumber("1.5d"));
        assertEquals("-1.5D should be Double -1.5", Double.valueOf(-1.5d), NumberUtils.createNumber("-1.5D"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberBigDecimal() {
        assertEquals("1.5 should be BigDecimal", new BigDecimal("1.5"), NumberUtils.createNumber("1.5"));
        assertEquals("-1.5 should be BigDecimal", new BigDecimal("-1.5"), NumberUtils.createNumber("-1.5"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberScientificNotation() {
        assertEquals("1.5E2 should be Double 150.0", Double.valueOf(150.0d), NumberUtils.createNumber("1.5E2"));
        assertEquals("1.5e2 should be Double 150.0", Double.valueOf(150.0d), NumberUtils.createNumber("1.5e2"));
    }

    // ==================== Partition B: Boundary Value Analysis (BVA) & Extremes ====================

    @Test(timeout = 4000)
    public void testCreateNumberMaxInt() {
        assertEquals("2147483647 should be Integer MAX_VALUE", 
                     Integer.valueOf(Integer.MAX_VALUE), 
                     NumberUtils.createNumber("2147483647"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberMinInt() {
        assertEquals("-2147483648 should be Integer MIN_VALUE", 
                     Integer.valueOf(Integer.MIN_VALUE), 
                     NumberUtils.createNumber("-2147483648"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberMaxLong() {
        assertEquals("9223372036854775807 should be Long MAX_VALUE", 
                     Long.valueOf(Long.MAX_VALUE), 
                     NumberUtils.createNumber("9223372036854775807"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberMinLong() {
        assertEquals("-9223372036854775808 should be Long MIN_VALUE", 
                     Long.valueOf(Long.MIN_VALUE), 
                     NumberUtils.createNumber("-9223372036854775808"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberZero() {
        assertEquals("0 should be Integer 0", Integer.valueOf(0), NumberUtils.createNumber("0"));
        assertEquals("0.0 should be Double 0.0", Double.valueOf(0.0d), NumberUtils.createNumber("0.0"));
        assertEquals("0.0d should be Double 0.0", Double.valueOf(0.0d), NumberUtils.createNumber("0.0d"));
        assertEquals("0.0f should be Float 0.0", Float.valueOf(0.0f), NumberUtils.createNumber("0.0f"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberNegativeZero() {
        assertEquals("-0 should be Integer 0", Integer.valueOf(0), NumberUtils.createNumber("-0"));
        assertEquals("-0.0 should be Double -0.0", Double.valueOf(-0.0d), NumberUtils.createNumber("-0.0"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberLeadingZeros() {
        assertEquals("007 should be Integer 7", Integer.valueOf(7), NumberUtils.createNumber("007"));
        assertEquals("00.5 should be Double 0.5", Double.valueOf(0.5d), NumberUtils.createNumber("00.5"));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * CRITICAL DEFECT TEST: Targets the StringIndexOutOfBoundsException bug.
     * When the input is a single character type qualifier like "l", "L", "f", "F", "d", "D",
     * the code does `String numeric = str.substring(0, str.length() - 1);` which produces an empty string,
     * then accesses `numeric.charAt(0)` without checking if numeric is empty.
     */
    @Test(timeout = 4000)
    public void testCreateNumberSingleTypeQualifier() {
        // These should all throw NumberFormatException, not StringIndexOutOfBoundsException
        try {
            NumberUtils.createNumber("l");
            fail("Expected NumberFormatException for single 'l'");
        } catch (NumberFormatException e) {
            // expected - this is the correct behavior
        }
        
        try {
            NumberUtils.createNumber("L");
            fail("Expected NumberFormatException for single 'L'");
        } catch (NumberFormatException e) {
            // expected
        }
        
        try {
            NumberUtils.createNumber("f");
            fail("Expected NumberFormatException for single 'f'");
        } catch (NumberFormatException e) {
            // expected
        }
        
        try {
            NumberUtils.createNumber("F");
            fail("Expected NumberFormatException for single 'F'");
        } catch (NumberFormatException e) {
            // expected
        }
        
        try {
            NumberUtils.createNumber("d");
            fail("Expected NumberFormatException for single 'd'");
        } catch (NumberFormatException e) {
            // expected
        }
        
        try {
            NumberUtils.createNumber("D");
            fail("Expected NumberFormatException for single 'D'");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberTypeQualifierWithDecimal() {
        // These should also not cause StringIndexOutOfBoundsException
        try {
            NumberUtils.createNumber(".l");
            fail("Expected NumberFormatException for '.l'");
        } catch (NumberFormatException e) {
            // expected
        }
        
        try {
            NumberUtils.createNumber(".L");
            fail("Expected NumberFormatException for '.L'");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberTypeQualifierWithExponent() {
        // These should also not cause StringIndexOutOfBoundsException
        try {
            NumberUtils.createNumber("1eL");
            fail("Expected NumberFormatException for '1eL'");
        } catch (NumberFormatException e) {
            // expected
        }
        
        try {
            NumberUtils.createNumber("1eD");
            fail("Expected NumberFormatException for '1eD'");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberNegativeTypeQualifier() {
        // These should also not cause StringIndexOutOfBoundsException
        try {
            NumberUtils.createNumber("-l");
            fail("Expected NumberFormatException for '-l'");
        } catch (NumberFormatException e) {
            // expected
        }
        
        try {
            NumberUtils.createNumber("-L");
            fail("Expected NumberFormatException for '-L'");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testCreateNumberInvalidFormat() {
        try {
            NumberUtils.createNumber("abc");
            fail("Expected NumberFormatException for 'abc'");
        } catch (NumberFormatException e) {
            // expected
        }
        
        try {
            NumberUtils.createNumber("1.2.3");
            fail("Expected NumberFormatException for '1.2.3'");
        } catch (NumberFormatException e) {
            // expected
        }
        
        try {
            NumberUtils.createNumber("1e2e3");
            fail("Expected NumberFormatException for '1e2e3'");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberInvalidTypeQualifier() {
        try {
            NumberUtils.createNumber("123x");
            fail("Expected NumberFormatException for '123x'");
        } catch (NumberFormatException e) {
            // expected
        }
        
        try {
            NumberUtils.createNumber("123z");
            fail("Expected NumberFormatException for '123z'");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberExponentWithoutDigit() {
        try {
            NumberUtils.createNumber("e5");
            fail("Expected NumberFormatException for 'e5'");
        } catch (NumberFormatException e) {
            // expected
        }
        
        try {
            NumberUtils.createNumber("1e");
            fail("Expected NumberFormatException for '1e'");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberTrailingDecimal() {
        assertEquals("1. should be Double 1.0", Double.valueOf(1.0d), NumberUtils.createNumber("1."));
        assertEquals("-1. should be Double -1.0", Double.valueOf(-1.0d), NumberUtils.createNumber("-1."));
    }

    @Test(timeout = 4000)
    public void testCreateNumberLeadingDecimal() {
        assertEquals(".5 should be Double 0.5", Double.valueOf(0.5d), NumberUtils.createNumber(".5"));
        assertEquals("-.5 should be Double -0.5", Double.valueOf(-0.5d), NumberUtils.createNumber("-.5"));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testToInt() {
        assertEquals(0, NumberUtils.toInt(null));
        assertEquals(0, NumberUtils.toInt(""));
        assertEquals(1, NumberUtils.toInt("1"));
        assertEquals(-1, NumberUtils.toInt("-1"));
        assertEquals(0, NumberUtils.toInt("abc"));
        assertEquals(5, NumberUtils.toInt(null, 5));
        assertEquals(5, NumberUtils.toInt("", 5));
        assertEquals(1, NumberUtils.toInt("1", 0));
    }

    @Test(timeout = 4000)
    public void testToLong() {
        assertEquals(0L, NumberUtils.toLong(null));
        assertEquals(0L, NumberUtils.toLong(""));
        assertEquals(1L, NumberUtils.toLong("1"));
        assertEquals(-1L, NumberUtils.toLong("-1"));
        assertEquals(5L, NumberUtils.toLong(null, 5L));
        assertEquals(5L, NumberUtils.toLong("", 5L));
    }

    @Test(timeout = 4000)
    public void testToFloat() {
        assertEquals(0.0f, NumberUtils.toFloat(null), 0.0f);
        assertEquals(0.0f, NumberUtils.toFloat(""), 0.0f);
        assertEquals(1.5f, NumberUtils.toFloat("1.5"), 0.0f);
        assertEquals(1.1f, NumberUtils.toFloat(null, 1.1f), 0.0f);
    }

    @Test(timeout = 4000)
    public void testToDouble() {
        assertEquals(0.0d, NumberUtils.toDouble(null), 0.0d);
        assertEquals(0.0d, NumberUtils.toDouble(""), 0.0d);
        assertEquals(1.5d, NumberUtils.toDouble("1.5"), 0.0d);
        assertEquals(1.1d, NumberUtils.toDouble(null, 1.1d), 0.0d);
    }

    @Test(timeout = 4000)
    public void testToByte() {
        assertEquals((byte) 0, NumberUtils.toByte(null));
        assertEquals((byte) 0, NumberUtils.toByte(""));
        assertEquals((byte) 1, NumberUtils.toByte("1"));
        assertEquals((byte) 5, NumberUtils.toByte(null, (byte) 5));
    }

    @Test(timeout = 4000)
    public void testToShort() {
        assertEquals((short) 0, NumberUtils.toShort(null));
        assertEquals((short) 0, NumberUtils.toShort(""));
        assertEquals((short) 1, NumberUtils.toShort("1"));
        assertEquals((short) 5, NumberUtils.toShort(null, (short) 5));
    }

    @Test(timeout = 4000)
    public void testMinMaxArrays() {
        assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        assertEquals(3L, NumberUtils.max(new long[]{3L, 1L, 2L}));
        
        assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
        assertEquals(3, NumberUtils.max(new int[]{3, 1, 2}));
        
        assertEquals((short) 1, NumberUtils.min(new short[]{(short) 3, (short) 1, (short) 2}));
        assertEquals((short) 3, NumberUtils.max(new short[]{(short) 3, (short) 1, (short) 2}));
        
        assertEquals((byte) 1, NumberUtils.min(new byte[]{(byte) 3, (byte) 1, (byte) 2}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{(byte) 3, (byte) 1, (byte) 2}));
        
        assertEquals(1.0d, NumberUtils.min(new double[]{3.0d, 1.0d, 2.0d}), 0.0d);
        assertEquals(3.0d, NumberUtils.max(new double[]{3.0d, 1.0d, 2.0d}), 0.0d);
        
        assertEquals(1.0f, NumberUtils.min(new float[]{3.0f, 1.0f, 2.0f}), 0.0f);
        assertEquals(3.0f, NumberUtils.max(new float[]{3.0f, 1.0f, 2.0f}), 0.0f);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMinNullArray() {
        NumberUtils.min((long[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMinEmptyArray() {
        NumberUtils.min(new long[0]);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMaxNullArray() {
        NumberUtils.max((long[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMaxEmptyArray() {
        NumberUtils.max(new long[0]);
    }

    @Test(timeout = 4000)
    public void testMinMaxThreeValues() {
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(3L, NumberUtils.max(3L, 1L, 2L));
        
        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(3, NumberUtils.max(3, 1, 2));
        
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 1, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 1, (short) 2));
        
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 1, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 1, (byte) 2));
        
        assertEquals(1.0d, NumberUtils.min(3.0d, 1.0d, 2.0d), 0.0d);
        assertEquals(3.0d, NumberUtils.max(3.0d, 1.0d, 2.0d), 0.0d);
        
        assertEquals(1.0f, NumberUtils.min(3.0f, 1.0f, 2.0f), 0.0f);
        assertEquals(3.0f, NumberUtils.max(3.0f, 1.0f, 2.0f), 0.0f);
    }

    @Test(timeout = 4000)
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("123"));
        assertFalse(NumberUtils.isDigits("12.3"));
        assertFalse(NumberUtils.isDigits("abc"));
    }

    @Test(timeout = 4000)
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber("123L"));
        assertTrue(NumberUtils.isNumber("123f"));
        assertTrue(NumberUtils.isNumber("123D"));
        assertTrue(NumberUtils.isNumber("1.5E2"));
        assertTrue(NumberUtils.isNumber("0x1A"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("1e2e3"));
        assertFalse(NumberUtils.isNumber("abc"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberWithExponentAndTypeQualifier() {
        assertEquals("1.5E2f should be Float 150.0", Float.valueOf(150.0f), NumberUtils.createNumber("1.5E2f"));
        assertEquals("1.5E2d should be Double 150.0", Double.valueOf(150.0d), NumberUtils.createNumber("1.5E2d"));
    }

    @Test(timeout = 4000)
    public void testCreateNumberInfinity() {
        try {
            NumberUtils.createNumber("Infinity");
            fail("Expected NumberFormatException for 'Infinity'");
        } catch (NumberFormatException e) {
            // expected
        }
        
        try {
            NumberUtils.createNumber("-Infinity");
            fail("Expected NumberFormatException for '-Infinity'");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberNaN() {
        try {
            NumberUtils.createNumber("NaN");
            fail("Expected NumberFormatException for 'NaN'");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateNumberVeryLargeFloat() {
        // This should return Double or BigDecimal, not Float (since Float would be infinite)
        Number result = NumberUtils.createNumber("1e40f");
        assertTrue("1e40f should not be Float", result instanceof Double || result instanceof BigDecimal);
    }

    @Test(timeout = 4000)
    public void testCreateNumberVerySmallFloat() {
        // This should return Double or BigDecimal, not Float (since Float would be 0.0 but string has non-zeros)
        Number result = NumberUtils.createNumber("1e-50f");
        assertTrue("1e-50f should not be Float", result instanceof Double || result instanceof BigDecimal);
    }
}