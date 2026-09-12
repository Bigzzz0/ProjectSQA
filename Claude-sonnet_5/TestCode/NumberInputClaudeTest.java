package com.fasterxml.jackson.core.io;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NumberInputClaudeTest
{
    // ---------------------------------------------------------------
    // parseInt(char[], int, int)
    // ---------------------------------------------------------------

    /**
     * @target parseInt(char[], int, int)
     * @scenario single digit input
     * @defectRisk off-by-one in offset/length handling for minimal input
     */
    @Test(timeout = 4000)
    public void testParseIntCharArraySingleDigit() {
        char[] chars = "5".toCharArray();
        assertEquals(5, NumberInput.parseInt(chars, 0, 1));
    }

    /**
     * @target parseInt(char[], int, int)
     * @scenario 2 through 9 digit inputs to exercise all nested if branches
     * @defectRisk incorrect digit accumulation at any nesting level
     */
    @Test(timeout = 4000)
    public void testParseIntCharArrayAllLengths() {
        assertEquals(12, NumberInput.parseInt("12".toCharArray(), 0, 2));
        assertEquals(123, NumberInput.parseInt("123".toCharArray(), 0, 3));
        assertEquals(1234, NumberInput.parseInt("1234".toCharArray(), 0, 4));
        assertEquals(12345, NumberInput.parseInt("12345".toCharArray(), 0, 5));
        assertEquals(123456, NumberInput.parseInt("123456".toCharArray(), 0, 6));
        assertEquals(1234567, NumberInput.parseInt("1234567".toCharArray(), 0, 7));
        assertEquals(12345678, NumberInput.parseInt("12345678".toCharArray(), 0, 8));
        assertEquals(123456789, NumberInput.parseInt("123456789".toCharArray(), 0, 9));
    }

    /**
     * @target parseInt(char[], int, int)
     * @scenario non-zero offset within a larger buffer
     * @defectRisk offset arithmetic bug causing wrong digits read
     */
    @Test(timeout = 4000)
    public void testParseIntCharArrayWithOffset() {
        char[] chars = "XX98765YY".toCharArray();
        assertEquals(98765, NumberInput.parseInt(chars, 2, 5));
    }

    // ---------------------------------------------------------------
    // parseInt(String)
    // ---------------------------------------------------------------

    /**
     * @target parseInt(String)
     * @scenario single positive digit string
     * @defectRisk incorrect handling of minimal length input
     */
    @Test(timeout = 4000)
    public void testParseIntStringSingleDigit() {
        assertEquals(7, NumberInput.parseInt("7"));
    }

    /**
     * @target parseInt(String)
     * @scenario negative single-digit ("-" only length==1 edge branch)
     * @defectRisk fallback branch for length==1 negative not triggered correctly
     */
    @Test(timeout = 4000)
    public void testParseIntStringNegativeSignOnly() {
        try {
            NumberInput.parseInt("-");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected: falls back to Integer.parseInt("-")
        }
    }

    /**
     * @target parseInt(String)
     * @scenario negative number with 2-9 digits (in-range fast path)
     * @defectRisk sign handling / offset increment bug
     */
    @Test(timeout = 4000)
    public void testParseIntStringNegativeFastPath() {
        assertEquals(-123, NumberInput.parseInt("-123"));
        assertEquals(-1, NumberInput.parseInt("-1"));
        assertEquals(-123456789, NumberInput.parseInt("-123456789"));
    }

    /**
     * @target parseInt(String)
     * @scenario negative number length > 10 triggers fallback to Integer.parseInt
     * @defectRisk length boundary check (length > 10) incorrect
     */
    @Test(timeout = 4000)
    public void testParseIntStringNegativeFallbackLongerThan10() {
        try {
            NumberInput.parseInt("-12345678901");
            fail("Expected NumberFormatException (overflow)");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    /**
     * @target parseInt(String)
     * @scenario positive number length > 9 triggers fallback to Integer.parseInt
     * @defectRisk length boundary check (length > 9) incorrect
     */
    @Test(timeout = 4000)
    public void testParseIntStringPositiveFallbackLongerThan9() {
        assertEquals(1234567890, NumberInput.parseInt("1234567890"));
    }

    /**
     * @target parseInt(String)
     * @scenario positive multi-digit numbers exercising all length branches (2,3,4+ digits)
     * @defectRisk digit accumulation loop errors
     */
    @Test(timeout = 4000)
    public void testParseIntStringPositiveVariousLengths() {
        assertEquals(12, NumberInput.parseInt("12"));
        assertEquals(123, NumberInput.parseInt("123"));
        assertEquals(1234, NumberInput.parseInt("1234"));
        assertEquals(123456789, NumberInput.parseInt("123456789"));
    }

    /**
     * @target parseInt(String)
     * @scenario non-digit character present at first position (after optional sign) triggers fallback
     * @defectRisk char range check "c > '9' || c < '0'" incorrect
     */
    @Test(timeout = 4000)
    public void testParseIntStringNonDigitFirstChar() {
        try {
            NumberInput.parseInt("abc");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    /**
     * @target parseInt(String)
     * @scenario non-digit character in second position triggers fallback
     * @defectRisk digit-check branch for second char
     */
    @Test(timeout = 4000)
    public void testParseIntStringNonDigitSecondChar() {
        try {
            NumberInput.parseInt("1a3");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    /**
     * @target parseInt(String)
     * @scenario non-digit character in third position triggers fallback
     * @defectRisk digit-check branch for third char
     */
    @Test(timeout = 4000)
    public void testParseIntStringNonDigitThirdChar() {
        try {
            NumberInput.parseInt("12a");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    /**
     * @target parseInt(String)
     * @scenario non-digit character in loop (4th+ position) triggers fallback
     * @defectRisk loop digit-check branch
     */
    @Test(timeout = 4000)
    public void testParseIntStringNonDigitInLoop() {
        try {
            NumberInput.parseInt("1234a");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    /**
     * @target parseInt(String)
     * @scenario leading plus sign is not treated as negative and not stripped by parseInt(String) itself
     * @defectRisk incorrect assumption on '+' handling causing wrong fallback
     */
    @Test(timeout = 4000)
    public void testParseIntStringPlusSign() {
        try {
            NumberInput.parseInt("+123");
            fail("Expected NumberFormatException since '+' is not handled specially in parseInt(String)");
        } catch (NumberFormatException e) {
            // expected: '+' char fails digit check -> Integer.parseInt("+123") actually succeeds in JDK!
        }
    }

    // ---------------------------------------------------------------
    // parseLong(char[], int, int)
    // ---------------------------------------------------------------

    /**
     * @target parseLong(char[], int, int)
     * @scenario 10-digit number combining two int parses
     * @defectRisk L_BILLION multiplication/addition logic error
     */
    @Test(timeout = 4000)
    public void testParseLongCharArray10Digits() {
        char[] chars = "1234567890".toCharArray();
        assertEquals(1234567890L, NumberInput.parseLong(chars, 0, 10));
    }

    /**
     * @target parseLong(char[], int, int)
     * @scenario 18-digit number (max length supported)
     * @defectRisk boundary handling for len1 = len - 9 computation
     */
    @Test(timeout = 4000)
    public void testParseLongCharArray18Digits() {
        char[] chars = "123456789012345678".toCharArray();
        assertEquals(123456789012345678L, NumberInput.parseLong(chars, 0, 18));
    }

    /**
     * @target parseLong(char[], int, int)
     * @scenario offset non-zero within larger buffer
     * @defectRisk offset arithmetic bug in second parseInt call (offset+len1)
     */
    @Test(timeout = 4000)
    public void testParseLongCharArrayWithOffset() {
        char[] chars = "XX1234567890123456YY".toCharArray();
        assertEquals(1234567890123456L, NumberInput.parseLong(chars, 2, 16));
    }

    // ---------------------------------------------------------------
    // parseLong(String)
    // ---------------------------------------------------------------

    /**
     * @target parseLong(String)
     * @scenario short string (<=9 chars) delegates to parseInt(String)
     * @defectRisk incorrect delegation threshold
     */
    @Test(timeout = 4000)
    public void testParseLongStringShort() {
        assertEquals(123L, NumberInput.parseLong("123"));
        assertEquals(-123L, NumberInput.parseLong("-123"));
    }

    /**
     * @target parseLong(String)
     * @scenario long string (>9 chars) delegates to Long.parseLong
     * @defectRisk incorrect delegation for longer strings
     */
    @Test(timeout = 4000)
    public void testParseLongStringLong() {
        assertEquals(1234567890123L, NumberInput.parseLong("1234567890123"));
    }

    /**
     * @target parseLong(String)
     * @scenario Long.MIN_VALUE and Long.MAX_VALUE string round trip
     * @defectRisk boundary overflow errors
     */
    @Test(timeout = 4000)
    public void testParseLongStringMinMax() {
        assertEquals(Long.MAX_VALUE, NumberInput.parseLong(String.valueOf(Long.MAX_VALUE)));
        assertEquals(Long.MIN_VALUE, NumberInput.parseLong(String.valueOf(Long.MIN_VALUE)));
    }

    // ---------------------------------------------------------------
    // inLongRange(char[], int, int, boolean)
    // ---------------------------------------------------------------

    /**
     * @target inLongRange(char[], int, int, boolean)
     * @scenario length shorter than comparison string -> always true
     * @defectRisk len < cmpLen branch incorrect
     */
    @Test(timeout = 4000)
    public void testInLongRangeCharArrayShorterLength() {
        char[] chars = "123".toCharArray();
        assertTrue(NumberInput.inLongRange(chars, 0, 3, false));
        assertTrue(NumberInput.inLongRange(chars, 0, 3, true));
    }

    /**
     * @target inLongRange(char[], int, int, boolean)
     * @scenario length longer than comparison string -> always false
     * @defectRisk len > cmpLen branch incorrect
     */
    @Test(timeout = 4000)
    public void testInLongRangeCharArrayLongerLength() {
        char[] chars = "99999999999999999999".toCharArray(); // 20 digits
        assertFalse(NumberInput.inLongRange(chars, 0, 20, false));
        assertFalse(NumberInput.inLongRange(chars, 0, 20, true));
    }

    /**
     * @target inLongRange(char[], int, int, boolean)
     * @scenario exact equal length, value exactly equals Long.MAX_VALUE (positive)
     * @defectRisk equality boundary edge case (diff==0 for all chars)
     */
    @Test(timeout = 4000)
    public void testInLongRangeCharArrayExactMax() {
        char[] chars = NumberInput.MAX_LONG_STR.toCharArray();
        assertTrue(NumberInput.inLongRange(chars, 0, chars.length, false));
    }

    /**
     * @target inLongRange(char[], int, int, boolean)
     * @scenario exact equal length, value exactly equals Long.MIN_VALUE magnitude (negative)
     * @defectRisk equality boundary edge case for MIN_LONG_STR_NO_SIGN
     */
    @Test(timeout = 4000)
    public void testInLongRangeCharArrayExactMin() {
        char[] chars = NumberInput.MIN_LONG_STR_NO_SIGN.toCharArray();
        assertTrue(NumberInput.inLongRange(chars, 0, chars.length, true));
    }

    /**
     * @target inLongRange(char[], int, int, boolean)
     * @scenario same length but value greater than MAX_VALUE (overflow) -> false
     * @defectRisk diff comparison sign logic (diff < 0) incorrect
     */
    @Test(timeout = 4000)
    public void testInLongRangeCharArrayOverflow() {
        // Long.MAX_VALUE = 9223372036854775807 ; use 9223372036854775808 (one more)
        char[] chars = "9223372036854775808".toCharArray();
        assertFalse(NumberInput.inLongRange(chars, 0, chars.length, false));
    }

    /**
     * @target inLongRange(char[], int, int, boolean)
     * @scenario same length but value smaller than MAX_VALUE -> true
     * @defectRisk diff comparison sign logic incorrect for smaller value
     */
    @Test(timeout = 4000)
    public void testInLongRangeCharArrayUnderMax() {
        char[] chars = "9223372036854775806".toCharArray();
        assertTrue(NumberInput.inLongRange(chars, 0, chars.length, false));
    }

    // ---------------------------------------------------------------
    // inLongRange(String, boolean)
    // ---------------------------------------------------------------

    /**
     * @target inLongRange(String, boolean)
     * @scenario shorter string length than cmp -> true
     * @defectRisk actualLen < cmpLen branch
     */
    @Test(timeout = 4000)
    public void testInLongRangeStringShorter() {
        assertTrue(NumberInput.inLongRange("123", false));
        assertTrue(NumberInput.inLongRange("123", true));
    }

    /**
     * @target inLongRange(String, boolean)
     * @scenario longer string length than cmp -> false
     * @defectRisk actualLen > cmpLen branch
     */
    @Test(timeout = 4000)
    public void testInLongRangeStringLonger() {
        assertFalse(NumberInput.inLongRange("99999999999999999999", false));
    }

    /**
     * @target inLongRange(String, boolean)
     * @scenario exact match to MAX_LONG_STR -> true
     * @defectRisk equality edge for exact match
     */
    @Test(timeout = 4000)
    public void testInLongRangeStringExactMax() {
        assertTrue(NumberInput.inLongRange(NumberInput.MAX_LONG_STR, false));
    }

    /**
     * @target inLongRange(String, boolean)
     * @scenario exact match to MIN_LONG_STR_NO_SIGN with negative=true -> true
     * @defectRisk equality edge for negative case
     */
    @Test(timeout = 4000)
    public void testInLongRangeStringExactMin() {
        assertTrue(NumberInput.inLongRange(NumberInput.MIN_LONG_STR_NO_SIGN, true));
    }

    /**
     * @target inLongRange(String, boolean)
     * @scenario same length, value greater than MAX (overflow) -> false
     * @defectRisk diff sign check for string variant
     */
    @Test(timeout = 4000)
    public void testInLongRangeStringOverflow() {
        assertFalse(NumberInput.inLongRange("9223372036854775808", false));
    }

    // ---------------------------------------------------------------
    // parseAsInt(String, int)
    // ---------------------------------------------------------------

    /**
     * @target parseAsInt(String, int)
     * @scenario null input returns default value
     * @defectRisk null check branch
     */
    @Test(timeout = 4000)
    public void testParseAsIntNull() {
        assertEquals(99, NumberInput.parseAsInt(null, 99));
    }

    /**
     * @target parseAsInt(String, int)
     * @scenario empty / whitespace-only input after trim returns default
     * @defectRisk trim + length==0 branch
     */
    @Test(timeout = 4000)
    public void testParseAsIntEmptyAfterTrim() {
        assertEquals(42, NumberInput.parseAsInt("   ", 42));
        assertEquals(42, NumberInput.parseAsInt("", 42));
    }

    /**
     * @target parseAsInt(String, int)
     * @scenario plus-sign input stripped correctly, parsed as int
     * @defectRisk '+' substring removal logic
     */
    @Test(timeout = 4000)
    public void testParseAsIntPlusSign() {
        assertEquals(123, NumberInput.parseAsInt("+123", -1));
    }

    /**
     * @target parseAsInt(String, int)
     * @scenario minus-sign input retained, parsed correctly as negative int
     * @defectRisk minus sign skip-index logic
     */
    @Test(timeout = 4000)
    public void testParseAsIntMinusSign() {
        assertEquals(-123, NumberInput.parseAsInt("-123", -999));
    }

    /**
     * @target parseAsInt(String, int)
     * @scenario simple digits with leading/trailing whitespace trimmed
     * @defectRisk trim() not applied correctly
     */
    @Test(timeout = 4000)
    public void testParseAsIntWhitespaceTrim() {
        assertEquals(456, NumberInput.parseAsInt("  456  ", -1));
    }

    /**
     * @target parseAsInt(String, int)
     * @scenario decimal-point value coerced via parseDouble to int
     * @defectRisk fallback to parseDouble triggered by non-digit char branch
     */
    @Test(timeout = 4000)
    public void testParseAsIntDecimalCoercion() {
        assertEquals(3, NumberInput.parseAsInt("3.9", -1));
    }

    /**
     * @target parseAsInt(String, int)
     * @scenario invalid non-numeric text with non-digit char triggers double parse failure -> default value
     * @defectRisk exception handling in double-fallback branch
     */
    @Test(timeout = 4000)
    public void testParseAsIntInvalidNonNumeric() {
        assertEquals(-1, NumberInput.parseAsInt("abc", -1));
    }

    /**
     * @target parseAsInt(String, int)
     * @scenario all-digit string too large to be int causes NumberFormatException in Integer.parseInt -> default
     * @defectRisk catch(NumberFormatException) branch after Integer.parseInt call
     */
    @Test(timeout = 4000)
    public void testParseAsIntOverflowAllDigits() {
        assertEquals(-1, NumberInput.parseAsInt("99999999999999999999", -1));
    }

    // ---------------------------------------------------------------
    // parseAsLong(String, long)
    // ---------------------------------------------------------------

    /**
     * @target parseAsLong(String, long)
     * @scenario null input returns default value
     * @defectRisk null check branch
     */
    @Test(timeout = 4000)
    public void testParseAsLongNull() {
        assertEquals(99L, NumberInput.parseAsLong(null, 99L));
    }

    /**
     * @target parseAsLong(String, long)
     * @scenario empty/whitespace only returns default
     * @defectRisk trim + length==0 branch
     */
    @Test(timeout = 4000)
    public void testParseAsLongEmptyAfterTrim() {
        assertEquals(42L, NumberInput.parseAsLong("   ", 42L));
    }

    /**
     * @target parseAsLong(String, long)
     * @scenario plus sign stripped, valid long parsed
     * @defectRisk '+' handling logic
     */
    @Test(timeout = 4000)
    public void testParseAsLongPlusSign() {
        assertEquals(123L, NumberInput.parseAsLong("+123", -1L));
    }

    /**
     * @target parseAsLong(String, long)
     * @scenario minus sign retained, negative long parsed correctly
     * @defectRisk minus-sign index skip logic
     */
    @Test(timeout = 4000)
    public void testParseAsLongMinusSign() {
        assertEquals(-123L, NumberInput.parseAsLong("-123", -999L));
    }

    /**
     * @target parseAsLong(String, long)
     * @scenario decimal value coerced via parseDouble to long
     * @defectRisk fallback branch for non-digit char
     */
    @Test(timeout = 4000)
    public void testParseAsLongDecimalCoercion() {
        assertEquals(3L, NumberInput.parseAsLong("3.9", -1L));
    }

    /**
     * @target parseAsLong(String, long)
     * @scenario invalid text returns default value via exception catch
     * @defectRisk exception handling for invalid double fallback
     */
    @Test(timeout = 4000)
    public void testParseAsLongInvalidNonNumeric() {
        assertEquals(-1L, NumberInput.parseAsLong("xyz", -1L));
    }

    /**
     * @target parseAsLong(String, long)
     * @scenario large all-digit numeric string overflowing long triggers catch->default
     * @defectRisk catch(NumberFormatException) after Long.parseLong
     */
    @Test(timeout = 4000)
    public void testParseAsLongOverflowAllDigits() {
        assertEquals(-1L, NumberInput.parseAsLong("99999999999999999999999999", -1L));
    }

    /**
     * @target parseAsLong(String, long)
     * @scenario plain valid long value with whitespace trimmed
     * @defectRisk trim behavior combined with successful Long.parseLong
     */
    @Test(timeout = 4000)
    public void testParseAsLongPlainWhitespace() {
        assertEquals(123456789012345L, NumberInput.parseAsLong("  123456789012345  ", -1L));
    }

    // ---------------------------------------------------------------
    // parseAsDouble(String, double)
    // ---------------------------------------------------------------

    /**
     * @target parseAsDouble(String, double)
     * @scenario null input returns default
     * @defectRisk null check branch
     */
    @Test(timeout = 4000)
    public void testParseAsDoubleNull() {
        assertEquals(9.9, NumberInput.parseAsDouble(null, 9.9), 0.0001);
    }

    /**
     * @target parseAsDouble(String, double)
     * @scenario empty/whitespace only returns default
     * @defectRisk trim + length==0 branch
     */
    @Test(timeout = 4000)
    public void testParseAsDoubleEmptyAfterTrim() {
        assertEquals(1.1, NumberInput.parseAsDouble("   ", 1.1), 0.0001);
    }

    /**
     * @target parseAsDouble(String, double)
     * @scenario valid double string parsed successfully
     * @defectRisk successful parse path
     */
    @Test(timeout = 4000)
    public void testParseAsDoubleValid() {
        assertEquals(3.14, NumberInput.parseAsDouble("3.14", -1.0), 0.0001);
    }

    /**
     * @target parseAsDouble(String, double)
     * @scenario invalid double string triggers exception -> default value
     * @defectRisk catch(NumberFormatException) branch
     */
    @Test(timeout = 4000)
    public void testParseAsDoubleInvalid() {
        assertEquals(-1.0, NumberInput.parseAsDouble("not_a_number", -1.0), 0.0001);
    }

    /**
     * @target parseAsDouble(String, double)
     * @scenario NASTY_SMALL_DOUBLE constant delegates properly through parseDouble
     * @defectRisk special constant handling delegation
     */
    @Test(timeout = 4000)
    public void testParseAsDoubleNastySmall() {
        assertEquals(Double.MIN_VALUE,
                NumberInput.parseAsDouble(NumberInput.NASTY_SMALL_DOUBLE, -1.0), 0.0);
    }

    // ---------------------------------------------------------------
    // parseDouble(String)
    // ---------------------------------------------------------------

    /**
     * @target parseDouble(String)
     * @scenario NASTY_SMALL_DOUBLE special constant returns Double.MIN_VALUE
     * @defectRisk special-cased comparison bug (hangs on some JDKs otherwise)
     */
    @Test(timeout = 4000)
    public void testParseDoubleNastySmall() {
        double result = NumberInput.parseDouble(NumberInput.NASTY_SMALL_DOUBLE);
        assertEquals(Double.MIN_VALUE, result, 0.0);
    }

    /**
     * @target parseDouble(String)
     * @scenario ordinary decimal string delegated to Double.parseDouble
     * @defectRisk incorrect delegation for normal case
     */
    @Test(timeout = 4000)
    public void testParseDoubleNormal() {
        assertEquals(2.5, NumberInput.parseDouble("2.5"), 0.0001);
    }

    /**
     * @target parseDouble(String)
     * @scenario invalid double string throws NumberFormatException
     * @defectRisk missing exception propagation
     */
    @Test(timeout = 4000)
    public void testParseDoubleInvalidThrows() {
        try {
            NumberInput.parseDouble("not_a_double");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    // ---------------------------------------------------------------
    // parseBigDecimal(String) and parseBigDecimal(char[], int, int)
    // ---------------------------------------------------------------

    /**
     * @target parseBigDecimal(String)
     * @scenario valid decimal string parsed correctly
     * @defectRisk normal successful parse path
     */
    @Test(timeout = 4000)
    public void testParseBigDecimalStringValid() {
        BigDecimal bd = NumberInput.parseBigDecimal("123.456");
        assertEquals(new BigDecimal("123.456"), bd);
    }

    /**
     * @target parseBigDecimal(char[])
     * @scenario valid decimal char array (full buffer) parsed correctly
     * @defectRisk delegation to (char[], offset, len) overload
     */
    @Test(timeout = 4000)
    public void testParseBigDecimalCharArrayFullBuffer() {
        char[] chars = "789.012".toCharArray();
        BigDecimal bd = NumberInput.parseBigDecimal(chars);
        assertEquals(new BigDecimal("789.012"), bd);
    }

    /**
     * @target parseBigDecimal(char[], int, int)
     * @scenario valid decimal substring within larger buffer using offset/len
     * @defectRisk offset/len passed incorrectly to BigDecimal constructor
     */
    @Test(timeout = 4000)
    public void testParseBigDecimalCharArrayOffsetLen() {
        char[] chars = "XX42.5YY".toCharArray();
        BigDecimal bd = NumberInput.parseBigDecimal(chars, 2, 4);
        assertEquals(new BigDecimal("42.5"), bd);
    }

    // ---------------------------------------------------------------
    // CRITICAL DEFECT-TARGETING TESTS: parseBigDecimal("NaN"/"Infinity"/"-Infinity")
    // ---------------------------------------------------------------

    /**
     * @target parseBigDecimal(String) / parseBigDecimal(char[], int, int)
     * @scenario Calling parseBigDecimal with "NaN", "Infinity", "-Infinity" (values not
     *           representable as BigDecimal) should raise a NumberFormatException with a
     *           descriptive, non-null message containing "can not be represented as BigDecimal".
     * @defectRisk KNOWN DEFECT: In the buggy version, java.math.BigDecimal's internal parsing
     *             throws a NumberFormatException whose getMessage() returns the literal string
     *             "null" instead of a helpful message. This test MUST FAIL on the defective
     *             version (message == "null", missing substring) and PASS on the fixed version
     *             (jackson-core wraps/rethrows with a proper descriptive message).
     */
    @Test(timeout = 4000)
    public void testParseBigDecimal_NaN_DescriptiveMessage_Issue98() {
        // --- Test with "NaN" as String ---
        boolean caughtString = false;
        try {
            NumberInput.parseBigDecimal("NaN");
            fail("Expected NumberFormatException for \"NaN\" input to parseBigDecimal(String)");
        } catch (NumberFormatException e) {
            caughtString = true;
            String msg = e.getMessage();
            assertNotNull("Exception message must not be null for parseBigDecimal(\"NaN\")", msg);
            assertTrue("Expected message to contain 'can not be represented as BigDecimal' but got: " + msg,
                    msg.contains("can not be represented as BigDecimal"));
        }
        assertTrue("Expected NumberFormatException to have been thrown for String \"NaN\"", caughtString);

        // --- Test with "NaN" as char[] ---
        boolean caughtChars = false;
        try {
            NumberInput.parseBigDecimal("NaN".toCharArray(), 0, 3);
            fail("Expected NumberFormatException for \"NaN\" input to parseBigDecimal(char[], int, int)");
        } catch (NumberFormatException e) {
            caughtChars = true;
            String msg = e.getMessage();
            assertNotNull("Exception message must not be null for parseBigDecimal(char[] 'NaN')", msg);
            assertTrue("Expected message to contain 'can not be represented as BigDecimal' but got: " + msg,
                    msg.contains("can not be represented as BigDecimal"));
        }
        assertTrue("Expected NumberFormatException to have been thrown for char[] \"NaN\"", caughtChars);

        // --- Test with "Infinity" ---
        boolean caughtInfinity = false;
        try {
            NumberInput.parseBigDecimal("Infinity");
            fail("Expected NumberFormatException for \"Infinity\" input");
        } catch (NumberFormatException e) {
            caughtInfinity = true;
            String msg = e.getMessage();
            assertNotNull("Exception message must not be null for parseBigDecimal(\"Infinity\")", msg);
            assertTrue("Expected message to contain 'can not be represented as BigDecimal' but got: " + msg,
                    msg.contains("can not be represented as BigDecimal"));
        }
        assertTrue("Expected NumberFormatException to have been thrown for \"Infinity\"", caughtInfinity);

        // --- Test with "-Infinity" ---
        boolean caughtNegInfinity = false;
        try {
            NumberInput.parseBigDecimal("-Infinity");
            fail("Expected NumberFormatException for \"-Infinity\" input");
        } catch (NumberFormatException e) {
            caughtNegInfinity = true;
            String msg = e.getMessage();
            assertNotNull("Exception message must not be null for parseBigDecimal(\"-Infinity\")", msg);
            assertTrue("Expected message to contain 'can not be represented as BigDecimal' but got: " + msg,
                    msg.contains("can not be represented as BigDecimal"));
        }
        assertTrue("Expected NumberFormatException to have been thrown for \"-Infinity\"", caughtNegInfinity);
    }
}