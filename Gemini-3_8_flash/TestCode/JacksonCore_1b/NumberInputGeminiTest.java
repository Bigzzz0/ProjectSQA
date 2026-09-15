package com.fasterxml.jackson.core.io;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.core.io.NumberInput
 *
 * Decision / Condition Branch Coverage:
 * 1. parseInt(char[], int, int):
 *    - Unrolled decision tree for lengths 1 to 9.
 *    - Offsets starting at 0 and > 0.
 *
 * 2. parseInt(String):
 *    - Negative sign branch:
 *      * length == 1 ("-") -> fallback to Integer.parseInt (throws NFE)
 *      * length > 10 ("-12345678901") -> fallback to Integer.parseInt
 *      * length [2..10] -> fast parsing loop
 *    - Non-negative branch:
 *      * length > 9 ("1234567890") -> fallback to Integer.parseInt
 *      * length [1..9] -> fast parsing loop
 *    - Non-digit detection at position 0, 1, 2, and in do-while loop (pos >= 3)
 *      triggering fallback to Integer.parseInt.
 *
 * 3. parseLong(char[], int, int) & parseLong(String):
 *    - parseLong(char[], offset, len) with lengths [10, 18], verifying 9-digit partition and billion multiplier.
 *    - parseLong(String) with length <= 9 (delegates to parseInt) vs > 9 (delegates to Long.parseLong).
 *
 * 4. inLongRange(char[], int, len, neg) & inLongRange(String, neg):
 *    - len < cmpLen (19): return true
 *    - len > cmpLen (19): return false
 *    - len == 19: character-by-character comparison against MIN_LONG_STR_NO_SIGN / MAX_LONG_STR.
 *    - Branches for diff < 0, diff > 0, diff == 0, and boundary edge values (Long.MIN_VALUE, Long.MAX_VALUE, overflow/underflow).
 *
 * 5. parseAsInt / parseAsLong / parseAsDouble:
 *    - null input -> defaultValue
 *    - empty or whitespace-only strings -> defaultValue
 *    - leading '+' sign -> strip and re-evaluate
 *    - leading '-' sign -> skip sign index and keep sign
 *    - non-digit encountered -> coerce via parseDouble
 *    - parseDouble failure -> catch NFE, return defaultValue
 *    - integer / long overflow in Integer.parseInt / Long.parseLong -> catch NFE, return defaultValue
 *
 * 6. parseDouble(String):
 *    - NASTY_SMALL_DOUBLE check: equals returns Double.MIN_VALUE
 *    - standard double parse: regular numeric values
 *
 * 7. Defect-Targeted Branch Zone (JacksonCore-1 / Issue 98):
 *    - parseBigDecimal("NaN"), parseBigDecimal("Infinity"), parseBigDecimal("-Infinity")
 *    - parseBigDecimal(char[], int, len) with "NaN"
 *    - Defect requirement: Exception message must NOT be null and MUST contain "can not be represented as BigDecimal"
 */
public class NumberInputGeminiTest {

    // =========================================================================
    // Partition A: Fast Integer Parsing
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseIntCharArray_AllLengths1To9() {
        char[] digits = "0123456789".toCharArray();
        for (int len = 1; len <= 9; ++len) {
            int expected = Integer.parseInt(new String(digits, 1, len));
            int actual = NumberInput.parseInt(digits, 1, len);
            assertEquals("Mismatch for len " + len, expected, actual);
        }
    }

    @Test(timeout = 4000)
    public void testParseIntCharArray_ZeroOffset() {
        char[] digits = "987654321".toCharArray();
        assertEquals(9, NumberInput.parseInt(digits, 0, 1));
        assertEquals(98, NumberInput.parseInt(digits, 0, 2));
        assertEquals(987, NumberInput.parseInt(digits, 0, 3));
        assertEquals(9876, NumberInput.parseInt(digits, 0, 4));
        assertEquals(98765, NumberInput.parseInt(digits, 0, 5));
        assertEquals(987654, NumberInput.parseInt(digits, 0, 6));
        assertEquals(9876543, NumberInput.parseInt(digits, 0, 7));
        assertEquals(98765432, NumberInput.parseInt(digits, 0, 8));
        assertEquals(987654321, NumberInput.parseInt(digits, 0, 9));
    }

    @Test(timeout = 4000)
    public void testParseIntString_PositiveLengths() {
        assertEquals(0, NumberInput.parseInt("0"));
        assertEquals(5, NumberInput.parseInt("5"));
        assertEquals(42, NumberInput.parseInt("42"));
        assertEquals(123, NumberInput.parseInt("123"));
        assertEquals(1234, NumberInput.parseInt("1234"));
        assertEquals(12345, NumberInput.parseInt("12345"));
        assertEquals(123456, NumberInput.parseInt("123456"));
        assertEquals(1234567, NumberInput.parseInt("1234567"));
        assertEquals(12345678, NumberInput.parseInt("12345678"));
        assertEquals(123456789, NumberInput.parseInt("123456789"));
        // Length > 9 (falls back to Integer.parseInt)
        assertEquals(1234567890, NumberInput.parseInt("1234567890"));
    }

    @Test(timeout = 4000)
    public void testParseIntString_NegativeLengths() {
        assertEquals(-1, NumberInput.parseInt("-1"));
        assertEquals(-42, NumberInput.parseInt("-42"));
        assertEquals(-123, NumberInput.parseInt("-123"));
        assertEquals(-1234, NumberInput.parseInt("-1234"));
        assertEquals(-12345, NumberInput.parseInt("-12345"));
        assertEquals(-123456, NumberInput.parseInt("-123456"));
        assertEquals(-1234567, NumberInput.parseInt("-1234567"));
        assertEquals(-12345678, NumberInput.parseInt("-12345678"));
        assertEquals(-123456789, NumberInput.parseInt("-123456789"));
        assertEquals(-1234567890, NumberInput.parseInt("-1234567890"));
    }

    @Test(timeout = 4000)
    public void testParseIntString_EdgeFallbacks() {
        // Negative sign only (length == 1)
        try {
            NumberInput.parseInt("-");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // Success
        }

        // Negative length > 10
        try {
            NumberInput.parseInt("-12345678901");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // Success
        }

        // Non-digit at position 0 (e.g., '+') -> delegates to Integer.parseInt
        assertEquals(123, NumberInput.parseInt("+123"));

        // Non-digit at position 1
        try {
            NumberInput.parseInt("1a");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // Success
        }

        // Non-digit at position 2
        try {
            NumberInput.parseInt("12a");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // Success
        }

        // Non-digit in do-while loop (position >= 3)
        try {
            NumberInput.parseInt("123a");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // Success
        }
        try {
            NumberInput.parseInt("12345a");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // Success
        }

        // Negative numbers with non-digits
        try {
            NumberInput.parseInt("-a");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // Success
        }
        try {
            NumberInput.parseInt("-1a");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // Success
        }
        try {
            NumberInput.parseInt("-12a");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // Success
        }
        try {
            NumberInput.parseInt("-123a");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // Success
        }
    }

    // =========================================================================
    // Partition B: Fast Long Parsing & Range Checks
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseLongCharArray() {
        char[] buffer = "prefix_123456789012345678_suffix".toCharArray();
        // 10 digits
        long val10 = NumberInput.parseLong(buffer, 7, 10);
        assertEquals(1234567890L, val10);

        // 18 digits
        long val18 = NumberInput.parseLong(buffer, 7, 18);
        assertEquals(123456789012345678L, val18);

        // 15 digits
        long val15 = NumberInput.parseLong(buffer, 7, 15);
        assertEquals(123456789012345L, val15);
    }

    @Test(timeout = 4000)
    public void testParseLongString() {
        // Length <= 9 delegates to parseInt
        assertEquals(0L, NumberInput.parseLong("0"));
        assertEquals(123456789L, NumberInput.parseLong("123456789"));
        assertEquals(-12345678L, NumberInput.parseLong("-12345678"));

        // Length > 9 delegates to Long.parseLong
        assertEquals(1000000000L, NumberInput.parseLong("1000000000"));
        assertEquals(Long.MAX_VALUE, NumberInput.parseLong(String.valueOf(Long.MAX_VALUE)));
        assertEquals(Long.MIN_VALUE, NumberInput.parseLong(String.valueOf(Long.MIN_VALUE)));
    }

    @Test(timeout = 4000)
    public void testInLongRangeCharArray() {
        String maxLong = String.valueOf(Long.MAX_VALUE); // 9223372036854775807 (len 19)
        String minLongNoSign = String.valueOf(Long.MIN_VALUE).substring(1); // 9223372036854775808 (len 19)

        // len < 19
        assertTrue(NumberInput.inLongRange("123456789012345678".toCharArray(), 0, 18, false));
        assertTrue(NumberInput.inLongRange("123456789012345678".toCharArray(), 0, 18, true));

        // len > 19
        assertFalse(NumberInput.inLongRange("12345678901234567890".toCharArray(), 0, 20, false));
        assertFalse(NumberInput.inLongRange("12345678901234567890".toCharArray(), 0, 20, true));

        // len == 19 exact match
        assertTrue(NumberInput.inLongRange(maxLong.toCharArray(), 0, 19, false));
        assertTrue(NumberInput.inLongRange(minLongNoSign.toCharArray(), 0, 19, true));

        // len == 19 smaller than max
        char[] smaller = "9223372036854775806".toCharArray();
        assertTrue(NumberInput.inLongRange(smaller, 0, 19, false));

        // len == 19 larger than max
        char[] largerThanMax = "9223372036854775808".toCharArray();
        assertFalse(NumberInput.inLongRange(largerThanMax, 0, 19, false));

        // len == 19 larger than min (overflow for negative)
        char[] largerThanMin = "9223372036854775809".toCharArray();
        assertFalse(NumberInput.inLongRange(largerThanMin, 0, 19, true));

        // Leading digit differences
        char[] leadSmaller = "8223372036854775807".toCharArray();
        assertTrue(NumberInput.inLongRange(leadSmaller, 0, 19, false));
        char[] leadLarger = "9923372036854775807".toCharArray();
        assertFalse(NumberInput.inLongRange(leadLarger, 0, 19, false));

        // With offset
        char[] withOffset = ("ABC" + maxLong).toCharArray();
        assertTrue(NumberInput.inLongRange(withOffset, 3, 19, false));
    }

    @Test(timeout = 4000)
    public void testInLongRangeString() {
        String maxLong = String.valueOf(Long.MAX_VALUE);
        String minLongNoSign = String.valueOf(Long.MIN_VALUE).substring(1);

        // Length < 19
        assertTrue(NumberInput.inLongRange("1234567890", false));
        assertTrue(NumberInput.inLongRange("1234567890", true));

        // Length > 19
        assertFalse(NumberInput.inLongRange("12345678901234567890", false));
        assertFalse(NumberInput.inLongRange("12345678901234567890", true));

        // Exact matches
        assertTrue(NumberInput.inLongRange(maxLong, false));
        assertTrue(NumberInput.inLongRange(minLongNoSign, true));

        // Edge comparisons
        assertFalse(NumberInput.inLongRange("9223372036854775808", false));
        assertTrue(NumberInput.inLongRange("9223372036854775807", true));
        assertFalse(NumberInput.inLongRange("9223372036854775809", true));
        assertFalse(NumberInput.inLongRange("9300000000000000000", false));
        assertTrue(NumberInput.inLongRange("1000000000000000000", false));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (JacksonCore-1 / Issue 98)
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseBigDecimal_NaN_DescriptiveMessage_Issue98() {
        try {
            NumberInput.parseBigDecimal("NaN");
            fail("Expected NumberFormatException for NaN");
        } catch (NumberFormatException e) {
            assertNotNull("Exception message should not be null", e.getMessage());
            assertTrue("Exception message [" + e.getMessage() + "] must contain 'can not be represented as BigDecimal'",
                    e.getMessage().contains("can not be represented as BigDecimal"));
        }

        try {
            char[] chars = "NaN".toCharArray();
            NumberInput.parseBigDecimal(chars, 0, chars.length);
            fail("Expected NumberFormatException for NaN char[]");
        } catch (NumberFormatException e) {
            assertNotNull("Exception message should not be null", e.getMessage());
            assertTrue("Exception message [" + e.getMessage() + "] must contain 'can not be represented as BigDecimal'",
                    e.getMessage().contains("can not be represented as BigDecimal"));
        }
    }

    @Test(timeout = 4000)
    public void testParseBigDecimal_Infinities_DescriptiveMessage_Issue98() {
        String[] infinities = new String[] { "Infinity", "+Infinity", "-Infinity" };
        for (String inf : infinities) {
            try {
                NumberInput.parseBigDecimal(inf);
                fail("Expected NumberFormatException for " + inf);
            } catch (NumberFormatException e) {
                assertNotNull("Exception message should not be null for " + inf, e.getMessage());
                assertTrue("Exception message [" + e.getMessage() + "] must contain 'can not be represented as BigDecimal'",
                        e.getMessage().contains("can not be represented as BigDecimal"));
            }

            try {
                char[] chars = inf.toCharArray();
                NumberInput.parseBigDecimal(chars, 0, chars.length);
                fail("Expected NumberFormatException for char[] " + inf);
            } catch (NumberFormatException e) {
                assertNotNull("Exception message should not be null for char[] " + inf, e.getMessage());
                assertTrue("Exception message [" + e.getMessage() + "] must contain 'can not be represented as BigDecimal'",
                        e.getMessage().contains("can not be represented as BigDecimal"));
            }
        }
    }

    @Test(timeout = 4000)
    public void testParseBigDecimal_Valid() {
        assertEquals(new BigDecimal("123.456"), NumberInput.parseBigDecimal("123.456"));
        assertEquals(new BigDecimal("-0.001"), NumberInput.parseBigDecimal("-0.001"));
        assertEquals(new BigDecimal("1e10"), NumberInput.parseBigDecimal("1e10"));

        char[] chars = "prefix123.456suffix".toCharArray();
        assertEquals(new BigDecimal("123.456"), NumberInput.parseBigDecimal(chars, 6, 7));
        assertEquals(new BigDecimal("987.65"), NumberInput.parseBigDecimal("987.65".toCharArray()));
    }

    // =========================================================================
    // Partition D: Floating-Point & Double Parsing
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseDouble_NastySmallDouble() {
        double val = NumberInput.parseDouble(NumberInput.NASTY_SMALL_DOUBLE);
        assertEquals(Double.MIN_VALUE, val, 0.0);
    }

    @Test(timeout = 4000)
    public void testParseDouble_RegularValues() {
        assertEquals(0.0, NumberInput.parseDouble("0.0"), 0.000001);
        assertEquals(-123.456, NumberInput.parseDouble("-123.456"), 0.000001);
        assertEquals(1.23e4, NumberInput.parseDouble("1.23e4"), 0.000001);
    }

    @Test(timeout = 4000)
    public void testParseAsDouble() {
        assertEquals(1.5, NumberInput.parseAsDouble(null, 1.5), 0.0);
        assertEquals(2.5, NumberInput.parseAsDouble("", 2.5), 0.0);
        assertEquals(3.5, NumberInput.parseAsDouble("   ", 3.5), 0.0);
        assertEquals(12.34, NumberInput.parseAsDouble("  12.34 ", 9.9), 0.00001);
        assertEquals(9.9, NumberInput.parseAsDouble("not_a_double", 9.9), 0.0);
    }

    // =========================================================================
    // Partition E: Fallback & Coercion Parsers
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseAsInt() {
        // null, empty, whitespace
        assertEquals(-99, NumberInput.parseAsInt(null, -99));
        assertEquals(-99, NumberInput.parseAsInt("", -99));
        assertEquals(-99, NumberInput.parseAsInt("    ", -99));

        // Normal integers
        assertEquals(1234, NumberInput.parseAsInt("1234", 0));
        assertEquals(1234, NumberInput.parseAsInt("  1234  ", 0));
        assertEquals(-567, NumberInput.parseAsInt("-567", 0));
        assertEquals(789, NumberInput.parseAsInt("+789", 0));

        // Floating point coerced to int
        assertEquals(123, NumberInput.parseAsInt("123.75", 0));
        assertEquals(-123, NumberInput.parseAsInt("-123.75", 0));
        assertEquals(45, NumberInput.parseAsInt("+45.2", 0));

        // Overflow of integer falls back to default
        assertEquals(77, NumberInput.parseAsInt("999999999999999999999999", 77));

        // Invalid string falls back to default
        assertEquals(77, NumberInput.parseAsInt("not_a_number", 77));
        assertEquals(77, NumberInput.parseAsInt("+", 77));
        assertEquals(77, NumberInput.parseAsInt("-", 77));
    }

    @Test(timeout = 4000)
    public void testParseAsLong() {
        // null, empty, whitespace
        assertEquals(-99L, NumberInput.parseAsLong(null, -99L));
        assertEquals(-99L, NumberInput.parseAsLong("", -99L));
        assertEquals(-99L, NumberInput.parseAsLong("    ", -99L));

        // Normal longs
        assertEquals(1234567890123L, NumberInput.parseAsLong("1234567890123", 0L));
        assertEquals(1234567890123L, NumberInput.parseAsLong("  1234567890123  ", 0L));
        assertEquals(-1234567890123L, NumberInput.parseAsLong("-1234567890123", 0L));
        assertEquals(1234567890123L, NumberInput.parseAsLong("+1234567890123", 0L));

        // Floating point coerced to long
        assertEquals(1234567890123L, NumberInput.parseAsLong("1234567890123.85", 0L));
        assertEquals(-1234567890123L, NumberInput.parseAsLong("-1234567890123.85", 0L));
        assertEquals(987654L, NumberInput.parseAsLong("+987654.12", 0L));

        // Overflow of long falls back to default
        assertEquals(88L, NumberInput.parseAsLong("99999999999999999999999999999999", 88L));

        // Invalid string falls back to default
        assertEquals(88L, NumberInput.parseAsLong("invalid_long", 88L));
        assertEquals(88L, NumberInput.parseAsLong("+", 88L));
        assertEquals(88L, NumberInput.parseAsLong("-", 88L));
    }

    @Test(timeout = 4000)
    public void testConstructorCoverage() {
        // NumberInput is a utility class with static methods, verify instantiation
        NumberInput instance = new NumberInput();
        assertNotNull(instance);
    }
}