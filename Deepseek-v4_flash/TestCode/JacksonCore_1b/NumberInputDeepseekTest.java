package com.fasterxml.jackson.core.io;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NumberInputDeepseekTest {

    /**
     * @target parseInt(char[], int, int)
     * @scenario Single digit parsing
     * @defectRisk Edge case for minimal length
     */
    @Test(timeout = 4000)
    public void testParseIntCharArray_SingleDigit() {
        char[] digits = "5".toCharArray();
        assertEquals(5, NumberInput.parseInt(digits, 0, 1));
    }

    /**
     * @target parseInt(char[], int, int)
     * @scenario Two digit parsing
     * @defectRisk Basic multi-digit parsing
     */
    @Test(timeout = 4000)
    public void testParseIntCharArray_TwoDigits() {
        char[] digits = "42".toCharArray();
        assertEquals(42, NumberInput.parseInt(digits, 0, 2));
    }

    /**
     * @target parseInt(char[], int, int)
     * @scenario Nine digit parsing (max for int without overflow)
     * @defectRisk Maximum length for fast path
     */
    @Test(timeout = 4000)
    public void testParseIntCharArray_NineDigits() {
        char[] digits = "123456789".toCharArray();
        assertEquals(123456789, NumberInput.parseInt(digits, 0, 9));
    }

    /**
     * @target parseInt(char[], int, int)
     * @scenario Offset and length parameters
     * @defectRisk Incorrect offset handling
     */
    @Test(timeout = 4000)
    public void testParseIntCharArray_WithOffset() {
        char[] digits = "abc12345xyz".toCharArray();
        assertEquals(12345, NumberInput.parseInt(digits, 3, 5));
    }

    /**
     * @target parseInt(String)
     * @scenario Positive integer within range
     * @defectRisk Basic string parsing
     */
    @Test(timeout = 4000)
    public void testParseIntString_Positive() {
        assertEquals(123, NumberInput.parseInt("123"));
    }

    /**
     * @target parseInt(String)
     * @scenario Negative integer within range
     * @defectRisk Negative sign handling
     */
    @Test(timeout = 4000)
    public void testParseIntString_Negative() {
        assertEquals(-456, NumberInput.parseInt("-456"));
    }

    /**
     * @target parseInt(String)
     * @scenario Integer at boundary of fast path (9 digits)
     * @defectRisk Boundary condition for fast path
     */
    @Test(timeout = 4000)
    public void testParseIntString_MaxFastPath() {
        assertEquals(999999999, NumberInput.parseInt("999999999"));
    }

    /**
     * @target parseInt(String)
     * @scenario Integer exceeding fast path (10 digits) - should delegate to Integer.parseInt
     * @defectRisk Overflow handling
     */
    @Test(timeout = 4000)
    public void testParseIntString_ExceedsFastPath() {
        assertEquals(1000000000, NumberInput.parseInt("1000000000"));
    }

    /**
     * @target parseInt(String)
     * @scenario Negative integer with 10 digits (min int)
     * @defectRisk Negative overflow handling
     */
    @Test(timeout = 4000)
    public void testParseIntString_NegativeMinInt() {
        assertEquals(Integer.MIN_VALUE, NumberInput.parseInt("-2147483648"));
    }

    /**
     * @target parseInt(String)
     * @scenario String with non-digit characters
     * @defectRisk Fallback to Integer.parseInt for invalid format
     */
    @Test(timeout = 4000)
    public void testParseIntString_NonDigitCharacters() {
        assertEquals(123, NumberInput.parseInt("123abc"));
    }

    /**
     * @target parseInt(String)
     * @scenario Empty string after sign
     * @defectRisk Edge case for negative sign only
     */
    @Test(timeout = 4000)
    public void testParseIntString_SignOnly() {
        assertEquals(0, NumberInput.parseInt("-"));
    }

    /**
     * @target parseLong(char[], int, int)
     * @scenario 10-digit long parsing
     * @defectRisk Basic long parsing from char array
     */
    @Test(timeout = 4000)
    public void testParseLongCharArray_TenDigits() {
        char[] digits = "1234567890".toCharArray();
        assertEquals(1234567890L, NumberInput.parseLong(digits, 0, 10));
    }

    /**
     * @target parseLong(char[], int, int)
     * @scenario 18-digit long parsing (max for this method)
     * @defectRisk Maximum length handling
     */
    @Test(timeout = 4000)
    public void testParseLongCharArray_EighteenDigits() {
        char[] digits = "123456789012345678".toCharArray();
        assertEquals(123456789012345678L, NumberInput.parseLong(digits, 0, 18));
    }

    /**
     * @target parseLong(String)
     * @scenario Short string (<=9 chars) - should use int parsing
     * @defectRisk Delegation to parseInt for short strings
     */
    @Test(timeout = 4000)
    public void testParseLongString_ShortString() {
        assertEquals(999999999L, NumberInput.parseLong("999999999"));
    }

    /**
     * @target parseLong(String)
     * @scenario Long string (10+ chars) - should use Long.parseLong
     * @defectRisk Delegation to Long.parseLong for long strings
     */
    @Test(timeout = 4000)
    public void testParseLongString_LongString() {
        assertEquals(1234567890123456789L, NumberInput.parseLong("1234567890123456789"));
    }

    /**
     * @target parseLong(String)
     * @scenario Maximum long value
     * @defectRisk Boundary condition for Long.MAX_VALUE
     */
    @Test(timeout = 4000)
    public void testParseLongString_MaxLong() {
        assertEquals(Long.MAX_VALUE, NumberInput.parseLong("9223372036854775807"));
    }

    /**
     * @target parseLong(String)
     * @scenario Minimum long value
     * @defectRisk Boundary condition for Long.MIN_VALUE
     */
    @Test(timeout = 4000)
    public void testParseLongString_MinLong() {
        assertEquals(Long.MIN_VALUE, NumberInput.parseLong("-9223372036854775808"));
    }

    /**
     * @target inLongRange(char[], int, int, boolean)
     * @scenario Number shorter than comparison string
     * @defectRisk Early return for shorter numbers
     */
    @Test(timeout = 4000)
    public void testInLongRangeCharArray_Shorter() {
        char[] digits = "123".toCharArray();
        assertTrue(NumberInput.inLongRange(digits, 0, 3, false));
    }

    /**
     * @target inLongRange(char[], int, int, boolean)
     * @scenario Number longer than comparison string
     * @defectRisk Early return for longer numbers
     */
    @Test(timeout = 4000)
    public void testInLongRangeCharArray_Longer() {
        char[] digits = "99999999999999999999".toCharArray();
        assertFalse(NumberInput.inLongRange(digits, 0, 20, false));
    }

    /**
     * @target inLongRange(char[], int, int, boolean)
     * @scenario Number equal to MAX_LONG
     * @defectRisk Exact match for positive boundary
     */
    @Test(timeout = 4000)
    public void testInLongRangeCharArray_EqualToMax() {
        char[] digits = "9223372036854775807".toCharArray();
        assertTrue(NumberInput.inLongRange(digits, 0, 19, false));
    }

    /**
     * @target inLongRange(char[], int, int, boolean)
     * @scenario Number equal to MIN_LONG (without sign)
     * @defectRisk Exact match for negative boundary
     */
    @Test(timeout = 4000)
    public void testInLongRangeCharArray_EqualToMin() {
        char[] digits = "9223372036854775808".toCharArray();
        assertTrue(NumberInput.inLongRange(digits, 0, 19, true));
    }

    /**
     * @target inLongRange(char[], int, int, boolean)
     * @scenario Number slightly greater than MAX_LONG
     * @defectRisk Comparison for positive overflow
     */
    @Test(timeout = 4000)
    public void testInLongRangeCharArray_GreaterThanMax() {
        char[] digits = "9223372036854775808".toCharArray();
        assertFalse(NumberInput.inLongRange(digits, 0, 19, false));
    }

    /**
     * @target inLongRange(String, boolean)
     * @scenario Number shorter than comparison string
     * @defectRisk Early return for shorter numbers
     */
    @Test(timeout = 4000)
    public void testInLongRangeString_Shorter() {
        assertTrue(NumberInput.inLongRange("123", false));
    }

    /**
     * @target inLongRange(String, boolean)
     * @scenario Number longer than comparison string
     * @defectRisk Early return for longer numbers
     */
    @Test(timeout = 4000)
    public void testInLongRangeString_Longer() {
        assertFalse(NumberInput.inLongRange("99999999999999999999", false));
    }

    /**
     * @target inLongRange(String, boolean)
     * @scenario Number equal to MAX_LONG
     * @defectRisk Exact match for positive boundary
     */
    @Test(timeout = 4000)
    public void testInLongRangeString_EqualToMax() {
        assertTrue(NumberInput.inLongRange("9223372036854775807", false));
    }

    /**
     * @target inLongRange(String, boolean)
     * @scenario Number equal to MIN_LONG (without sign)
     * @defectRisk Exact match for negative boundary
     */
    @Test(timeout = 4000)
    public void testInLongRangeString_EqualToMin() {
        assertTrue(NumberInput.inLongRange("9223372036854775808", true));
    }

    /**
     * @target parseAsInt
     * @scenario Null input
     * @defectRisk Null handling
     */
    @Test(timeout = 4000)
    public void testParseAsInt_Null() {
        assertEquals(42, NumberInput.parseAsInt(null, 42));
    }

    /**
     * @target parseAsInt
     * @scenario Empty string
     * @defectRisk Empty string handling
     */
    @Test(timeout = 4000)
    public void testParseAsInt_Empty() {
        assertEquals(42, NumberInput.parseAsInt("", 42));
    }

    /**
     * @target parseAsInt
     * @scenario Whitespace string
     * @defectRisk Whitespace trimming
     */
    @Test(timeout = 4000)
    public void testParseAsInt_Whitespace() {
        assertEquals(42, NumberInput.parseAsInt("   ", 42));
    }

    /**
     * @target parseAsInt
     * @scenario Simple integer string
     * @defectRisk Basic integer parsing
     */
    @Test(timeout = 4000)
    public void testParseAsInt_SimpleInteger() {
        assertEquals(123, NumberInput.parseAsInt("123", 0));
    }

    /**
     * @target parseAsInt
     * @scenario Integer with leading plus sign
     * @defectRisk Plus sign handling
     */
    @Test(timeout = 4000)
    public void testParseAsInt_WithPlusSign() {
        assertEquals(456, NumberInput.parseAsInt("+456", 0));
    }

    /**
     * @target parseAsInt
     * @scenario Integer with leading minus sign
     * @defectRisk Minus sign handling
     */
    @Test(timeout = 4000)
    public void testParseAsInt_WithMinusSign() {
        assertEquals(-789, NumberInput.parseAsInt("-789", 0));
    }

    /**
     * @target parseAsInt
     * @scenario String with non-digit characters (should parse as double)
     * @defectRisk Fallback to double parsing
     */
    @Test(timeout = 4000)
    public void testParseAsInt_NonDigitString() {
        assertEquals(12, NumberInput.parseAsInt("12.5", 0));
    }

    /**
     * @target parseAsInt
     * @scenario Invalid number string
     * @defectRisk Exception handling
     */
    @Test(timeout = 4000)
    public void testParseAsInt_InvalidNumber() {
        assertEquals(42, NumberInput.parseAsInt("abc", 42));
    }

    /**
     * @target parseAsLong
     * @scenario Null input
     * @defectRisk Null handling
     */
    @Test(timeout = 4000)
    public void testParseAsLong_Null() {
        assertEquals(42L, NumberInput.parseAsLong(null, 42L));
    }

    /**
     * @target parseAsLong
     * @scenario Empty string
     * @defectRisk Empty string handling
     */
    @Test(timeout = 4000)
    public void testParseAsLong_Empty() {
        assertEquals(42L, NumberInput.parseAsLong("", 42L));
    }

    /**
     * @target parseAsLong
     * @scenario Simple long string
     * @defectRisk Basic long parsing
     */
    @Test(timeout = 4000)
    public void testParseAsLong_SimpleLong() {
        assertEquals(1234567890123L, NumberInput.parseAsLong("1234567890123", 0L));
    }

    /**
     * @target parseAsLong
     * @scenario Long with leading plus sign
     * @defectRisk Plus sign handling
     */
    @Test(timeout = 4000)
    public void testParseAsLong_WithPlusSign() {
        assertEquals(456L, NumberInput.parseAsLong("+456", 0L));
    }

    /**
     * @target parseAsLong
     * @scenario Long with leading minus sign
     * @defectRisk Minus sign handling
     */
    @Test(timeout = 4000)
    public void testParseAsLong_WithMinusSign() {
        assertEquals(-789L, NumberInput.parseAsLong("-789", 0L));
    }

    /**
     * @target parseAsLong
     * @scenario String with non-digit characters (should parse as double)
     * @defectRisk Fallback to double parsing
     */
    @Test(timeout = 4000)
    public void testParseAsLong_NonDigitString() {
        assertEquals(12L, NumberInput.parseAsLong("12.5", 0L));
    }

    /**
     * @target parseAsLong
     * @scenario Invalid number string
     * @defectRisk Exception handling
     */
    @Test(timeout = 4000)
    public void testParseAsLong_InvalidNumber() {
        assertEquals(42L, NumberInput.parseAsLong("abc", 42L));
    }

    /**
     * @target parseAsDouble
     * @scenario Null input
     * @defectRisk Null handling
     */
    @Test(timeout = 4000)
    public void testParseAsDouble_Null() {
        assertEquals(3.14, NumberInput.parseAsDouble(null, 3.14), 0.0);
    }

    /**
     * @target parseAsDouble
     * @scenario Empty string
     * @defectRisk Empty string handling
     */
    @Test(timeout = 4000)
    public void testParseAsDouble_Empty() {
        assertEquals(3.14, NumberInput.parseAsDouble("", 3.14), 0.0);
    }

    /**
     * @target parseAsDouble
     * @scenario Valid double string
     * @defectRisk Basic double parsing
     */
    @Test(timeout = 4000)
    public void testParseAsDouble_ValidDouble() {
        assertEquals(3.14159, NumberInput.parseAsDouble("3.14159", 0.0), 0.00001);
    }

    /**
     * @target parseAsDouble
     * @scenario Invalid double string
     * @defectRisk Exception handling
     */
    @Test(timeout = 4000)
    public void testParseAsDouble_InvalidDouble() {
        assertEquals(42.0, NumberInput.parseAsDouble("abc", 42.0), 0.0);
    }

    /**
     * @target parseDouble
     * @scenario Normal double string
     * @defectRisk Basic double parsing
     */
    @Test(timeout = 4000)
    public void testParseDouble_Normal() {
        assertEquals(3.14, NumberInput.parseDouble("3.14"), 0.0);
    }

    /**
     * @target parseDouble
     * @scenario NASTY_SMALL_DOUBLE constant
     * @defectRisk Special handling for problematic double
     */
    @Test(timeout = 4000)
    public void testParseDouble_NastySmallDouble() {
        assertEquals(Double.MIN_VALUE, NumberInput.parseDouble(NumberInput.NASTY_SMALL_DOUBLE), 0.0);
    }

    /**
     * @target parseDouble
     * @scenario Infinity string
     * @defectRisk Special value handling
     */
    @Test(timeout = 4000)
    public void testParseDouble_Infinity() {
        assertEquals(Double.POSITIVE_INFINITY, NumberInput.parseDouble("Infinity"), 0.0);
    }

    /**
     * @target parseDouble
     * @scenario Negative infinity string
     * @defectRisk Special value handling
     */
    @Test(timeout = 4000)
    public void testParseDouble_NegativeInfinity() {
        assertEquals(Double.NEGATIVE_INFINITY, NumberInput.parseDouble("-Infinity"), 0.0);
    }

    /**
     * @target parseDouble
     * @scenario NaN string
     * @defectRisk Special value handling
     */
    @Test(timeout = 4000)
    public void testParseDouble_NaN() {
        assertTrue(Double.isNaN(NumberInput.parseDouble("NaN")));
    }

    /**
     * @target parseBigDecimal(String)
     * @scenario Valid decimal string
     * @defectRisk Basic BigDecimal parsing
     */
    @Test(timeout = 4000)
    public void testParseBigDecimalString_Valid() {
        assertEquals(new BigDecimal("123.456"), NumberInput.parseBigDecimal("123.456"));
    }

    /**
     * @target parseBigDecimal(String)
     * @scenario Integer string
     * @defectRisk Integer to BigDecimal conversion
     */
    @Test(timeout = 4000)
    public void testParseBigDecimalString_Integer() {
        assertEquals(new BigDecimal("789"), NumberInput.parseBigDecimal("789"));
    }

    /**
     * @target parseBigDecimal(char[], int, int)
     * @scenario Valid decimal from char array
     * @defectRisk Basic char array parsing
     */
    @Test(timeout = 4000)
    public void testParseBigDecimalCharArray_Valid() {
        char[] digits = "123.456".toCharArray();
        assertEquals(new BigDecimal("123.456"), NumberInput.parseBigDecimal(digits, 0, 7));
    }

    /**
     * @target parseBigDecimal(char[], int, int)
     * @scenario With offset and length
     * @defectRisk Offset handling
     */
    @Test(timeout = 4000)
    public void testParseBigDecimalCharArray_WithOffset() {
        char[] digits = "abc789.012xyz".toCharArray();
        assertEquals(new BigDecimal("789.012"), NumberInput.parseBigDecimal(digits, 3, 7));
    }

    /**
     * @target parseBigDecimal(char[])
     * @scenario Full buffer parsing
     * @defectRisk Full buffer method
     */
    @Test(timeout = 4000)
    public void testParseBigDecimalCharArray_FullBuffer() {
        char[] digits = "456.789".toCharArray();
        assertEquals(new BigDecimal("456.789"), NumberInput.parseBigDecimal(digits));
    }

    /**
     * @target parseBigDecimal(String) with NaN
     * @scenario NaN string should throw NumberFormatException with descriptive message
     * @defectRisk Defect: NaN parsing produces null message instead of descriptive message
     */
    @Test(timeout = 4000)
    public void testParseBigDecimal_NaN_DescriptiveMessage_Issue98() {
        try {
            NumberInput.parseBigDecimal("NaN");
            fail("Expected NumberFormatException for NaN");
        } catch (NumberFormatException e) {
            assertNotNull("Exception message should not be null", e.getMessage());
            assertTrue("Exception message should contain 'can not be represented as BigDecimal'",
                    e.getMessage().contains("can not be represented as BigDecimal"));
        }
    }

    /**
     * @target parseBigDecimal(char[], int, int) with NaN
     * @scenario NaN char array should throw NumberFormatException with descriptive message
     * @defectRisk Defect: NaN parsing produces null message instead of descriptive message
     */
    @Test(timeout = 4000)
    public void testParseBigDecimalCharArray_NaN_DescriptiveMessage_Issue98() {
        try {
            NumberInput.parseBigDecimal("NaN".toCharArray(), 0, 3);
            fail("Expected NumberFormatException for NaN");
        } catch (NumberFormatException e) {
            assertNotNull("Exception message should not be null", e.getMessage());
            assertTrue("Exception message should contain 'can not be represented as BigDecimal'",
                    e.getMessage().contains("can not be represented as BigDecimal"));
        }
    }

    /**
     * @target parseBigDecimal(String) with Infinity
     * @scenario Infinity string should throw NumberFormatException with descriptive message
     * @defectRisk Defect: Infinity parsing produces null message instead of descriptive message
     */
    @Test(timeout = 4000)
    public void testParseBigDecimal_Infinity_DescriptiveMessage_Issue98() {
        try {
            NumberInput.parseBigDecimal("Infinity");
            fail("Expected NumberFormatException for Infinity");
        } catch (NumberFormatException e) {
            assertNotNull("Exception message should not be null", e.getMessage());
            assertTrue("Exception message should contain 'can not be represented as BigDecimal'",
                    e.getMessage().contains("can not be represented as BigDecimal"));
        }
    }

    /**
     * @target parseBigDecimal(String) with -Infinity
     * @scenario -Infinity string should throw NumberFormatException with descriptive message
     * @defectRisk Defect: -Infinity parsing produces null message instead of descriptive message
     */
    @Test(timeout = 4000)
    public void testParseBigDecimal_NegativeInfinity_DescriptiveMessage_Issue98() {
        try {
            NumberInput.parseBigDecimal("-Infinity");
            fail("Expected NumberFormatException for -Infinity");
        } catch (NumberFormatException e) {
            assertNotNull("Exception message should not be null", e.getMessage());
            assertTrue("Exception message should contain 'can not be represented as BigDecimal'",
                    e.getMessage().contains("can not be represented as BigDecimal"));
        }
    }

    /**
     * @target parseAsInt with overflow
     * @scenario Integer overflow should fall back to double parsing
     * @defectRisk Overflow handling in parseAsInt
     */
    @Test(timeout = 4000)
    public void testParseAsInt_Overflow() {
        assertEquals(Integer.MAX_VALUE, NumberInput.parseAsInt("2147483648", 0));
    }

    /**
     * @target parseAsLong with overflow
     * @scenario Long overflow should fall back to double parsing
     * @defectRisk Overflow handling in parseAsLong
     */
    @Test(timeout = 4000)
    public void testParseAsLong_Overflow() {
        assertEquals(Long.MAX_VALUE, NumberInput.parseAsLong("9223372036854775808", 0L));
    }

    /**
     * @target parseInt(String) with leading zeros
     * @scenario String with leading zeros
     * @defectRisk Leading zero handling
     */
    @Test(timeout = 4000)
    public void testParseIntString_LeadingZeros() {
        assertEquals(123, NumberInput.parseInt("00123"));
    }

    /**
     * @target parseInt(String) with whitespace
     * @scenario String with whitespace should delegate to Integer.parseInt
     * @defectRisk Whitespace handling
     */
    @Test(timeout = 4000)
    public void testParseIntString_WithWhitespace() {
        assertEquals(123, NumberInput.parseInt(" 123"));
    }

    /**
     * @target parseLong(String) with leading zeros
     * @scenario Long string with leading zeros
     * @defectRisk Leading zero handling for longs
     */
    @Test(timeout = 4000)
    public void testParseLongString_LeadingZeros() {
        assertEquals(1234567890L, NumberInput.parseLong("001234567890"));
    }

    /**
     * @target inLongRange with exact boundary comparison
     * @scenario Number one less than MAX_LONG
     * @defectRisk Boundary comparison
     */
    @Test(timeout = 4000)
    public void testInLongRange_OneLessThanMax() {
        assertTrue(NumberInput.inLongRange("9223372036854775806", false));
    }

    /**
     * @target inLongRange with exact boundary comparison
     * @scenario Number one more than MAX_LONG
     * @defectRisk Boundary comparison
     */
    @Test(timeout = 4000)
    public void testInLongRange_OneMoreThanMax() {
        assertFalse(NumberInput.inLongRange("9223372036854775808", false));
    }

    /**
     * @target parseAsDouble with whitespace
     * @scenario Double string with leading/trailing whitespace
     * @defectRisk Whitespace trimming
     */
    @Test(timeout = 4000)
    public void testParseAsDouble_WithWhitespace() {
        assertEquals(3.14, NumberInput.parseAsDouble("  3.14  ", 0.0), 0.0);
    }

    /**
     * @target parseAsDouble with plus sign
     * @scenario Double string with leading plus sign
     * @defectRisk Plus sign handling
     */
    @Test(timeout = 4000)
    public void testParseAsDouble_WithPlusSign() {
        assertEquals(3.14, NumberInput.parseAsDouble("+3.14", 0.0), 0.0);
    }

    /**
     * @target parseAsDouble with minus sign
     * @scenario Double string with leading minus sign
     * @defectRisk Minus sign handling
     */
    @Test(timeout = 4000)
    public void testParseAsDouble_WithMinusSign() {
        assertEquals(-3.14, NumberInput.parseAsDouble("-3.14", 0.0), 0.0);
    }
}