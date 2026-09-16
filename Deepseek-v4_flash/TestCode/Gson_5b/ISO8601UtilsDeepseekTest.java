package com.google.gson.internal.bind.util;

import org.junit.Test;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for ISO8601Utils.
 * Targets line/branch coverage and the known defect (parsing "+01" timezone offset).
 *
 * [Branch & Defect Analysis Matrix]
 * - Format: UTC vs non-UTC, millis on/off, positive/negative offsets.
 * - Parse: year/month/day with/without dashes, T presence, hours:minutes:seconds.millis,
 *   timezone Z, +hh:mm, +hhmm, +hh (defect), -hh:mm, -hhmm, -hh.
 * - Edge: leap seconds (60,61,62 -> 59), missing timezone, invalid chars, empty string.
 * - Defect: "1970-01-01T01:00:00+01" must parse successfully to 1970-01-01T00:00:00Z.
 */
public class ISO8601UtilsDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testFormatDateDefault() {
        Date date = new Date(0L); // 1970-01-01 00:00:00 UTC
        String result = ISO8601Utils.format(date);
        assertEquals("1970-01-01T00:00:00Z", result);
    }

    @Test(timeout = 4000)
    public void testFormatDateWithMillis() {
        Date date = new Date(123456789L); // 1970-01-02 10:17:36.789 UTC
        String result = ISO8601Utils.format(date, true);
        assertEquals("1970-01-02T10:17:36.789Z", result);
    }

    @Test(timeout = 4000)
    public void testFormatDateWithoutMillis() {
        Date date = new Date(123456789L);
        String result = ISO8601Utils.format(date, false);
        assertEquals("1970-01-02T10:17:36Z", result);
    }

    @Test(timeout = 4000)
    public void testFormatDateWithTimeZonePositiveOffset() {
        Date date = new Date(0L);
        TimeZone tz = TimeZone.getTimeZone("GMT+02:00");
        String result = ISO8601Utils.format(date, false, tz);
        assertEquals("1970-01-01T02:00:00+02:00", result);
    }

    @Test(timeout = 4000)
    public void testFormatDateWithTimeZoneNegativeOffset() {
        Date date = new Date(0L);
        TimeZone tz = TimeZone.getTimeZone("GMT-05:30");
        String result = ISO8601Utils.format(date, false, tz);
        assertEquals("1969-12-31T18:30:00-05:30", result);
    }

    @Test(timeout = 4000)
    public void testFormatDateWithMillisAndTimeZone() {
        Date date = new Date(123456789L);
        TimeZone tz = TimeZone.getTimeZone("GMT+01:00");
        String result = ISO8601Utils.format(date, true, tz);
        assertEquals("1970-01-02T11:17:36.789+01:00", result);
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testParseDateWithDashes() throws Exception {
        String input = "1970-01-01T00:00:00Z";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(0L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateWithoutDashes() throws Exception {
        String input = "19700101T000000Z";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(0L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateOnly() throws Exception {
        String input = "1970-01-01";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(0L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateWithMillis() throws Exception {
        String input = "1970-01-01T00:00:00.123Z";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(123L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateWithOneDigitMillis() throws Exception {
        String input = "1970-01-01T00:00:00.1Z";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(100L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateWithTwoDigitMillis() throws Exception {
        String input = "1970-01-01T00:00:00.12Z";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(120L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateWithLeapSecond() throws Exception {
        // 60 seconds should be truncated to 59
        String input = "1970-01-01T00:00:60Z";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        // 59 seconds after epoch
        assertEquals(59000L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateWithLeapSecond62() throws Exception {
        String input = "1970-01-01T00:00:62Z";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(59000L, result.getTime());
    }

    @Test(timeout = 4000)
    public void testParseDateWithPositiveOffsetColon() throws Exception {
        String input = "1970-01-01T01:00:00+01:00";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(0L, result.getTime()); // 01:00:00+01:00 = 00:00:00 UTC
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateWithPositiveOffsetNoColon() throws Exception {
        String input = "1970-01-01T01:00:00+0100";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(0L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateWithNegativeOffsetColon() throws Exception {
        String input = "1970-01-01T00:00:00-05:00";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(5 * 3600 * 1000L, result.getTime()); // 1970-01-01 05:00:00 UTC
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateWithNegativeOffsetNoColon() throws Exception {
        String input = "1970-01-01T00:00:00-0500";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(5 * 3600 * 1000L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateWithZeroOffset() throws Exception {
        String input = "1970-01-01T00:00:00+0000";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(0L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateWithZeroOffsetColon() throws Exception {
        String input = "1970-01-01T00:00:00+00:00";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(0L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Defect: parsing "1970-01-01T01:00:00+01" (timezone with only hours, no minutes)
     * should succeed and return 1970-01-01T00:00:00Z.
     * Known to throw an exception in the defective version.
     */
    @Test(timeout = 4000)
    public void testParseDateWithPositiveOffsetHoursOnly() throws Exception {
        String input = "1970-01-01T01:00:00+01";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(0L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateWithNegativeOffsetHoursOnly() throws Exception {
        String input = "1970-01-01T00:00:00-05";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(5 * 3600 * 1000L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseInvalidDateEmptyString() throws Exception {
        ParsePosition pos = new ParsePosition(0);
        ISO8601Utils.parse("", pos);
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseInvalidDateNoTimezone() throws Exception {
        ParsePosition pos = new ParsePosition(0);
        ISO8601Utils.parse("1970-01-01T00:00:00", pos);
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseInvalidDateBadTimezoneChar() throws Exception {
        ParsePosition pos = new ParsePosition(0);
        ISO8601Utils.parse("1970-01-01T00:00:00X", pos);
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseInvalidDateNonDigitInYear() throws Exception {
        ParsePosition pos = new ParsePosition(0);
        ISO8601Utils.parse("197A-01-01T00:00:00Z", pos);
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseInvalidDateNegativeYear() throws Exception {
        // parseInt does not support negative values, but the code uses negative internal logic.
        // Actually, the code uses negative accumulation and then negates, so negative year would be parsed as positive.
        // This test is for invalid format like "-1970-01-01" which would fail at first char.
        ParsePosition pos = new ParsePosition(0);
        ISO8601Utils.parse("-1970-01-01T00:00:00Z", pos);
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseInvalidDateMismatchedTimezone() throws Exception {
        // This triggers the IndexOutOfBoundsException branch when timezone ID doesn't match
        // Use a non-existent offset like +99:99
        ParsePosition pos = new ParsePosition(0);
        ISO8601Utils.parse("1970-01-01T00:00:00+99:99", pos);
    }

    @Test(timeout = 4000)
    public void testParseWithPositionUpdate() throws Exception {
        String input = "1970-01-01T00:00:00Z";
        ParsePosition pos = new ParsePosition(0);
        ISO8601Utils.parse(input, pos);
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseWithPositionPartial() throws Exception {
        // ParsePosition index should advance even if parsing fails? Actually exception is thrown.
        // But we can test that index is set correctly on success.
        String input = "1970-01-01";
        ParsePosition pos = new ParsePosition(0);
        ISO8601Utils.parse(input, pos);
        assertEquals(input.length(), pos.getIndex());
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    // Not applicable for utility class with static methods.

    // Additional edge cases for completeness

    @Test(timeout = 4000)
    public void testParseDateWithTimeOnly() throws Exception {
        // No date? Actually format requires date. But we can test with just date and no T.
        String input = "1970-01-01";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(0L, result.getTime());
    }

    @Test(timeout = 4000)
    public void testParseDateWithNegativeOffsetHoursOnlyAndMillis() throws Exception {
        String input = "1970-01-01T00:00:00.500-05";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(5 * 3600 * 1000L + 500L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testFormatDateWithNegativeOffset() {
        Date date = new Date(0L);
        TimeZone tz = TimeZone.getTimeZone("GMT-08:00");
        String result = ISO8601Utils.format(date, false, tz);
        assertEquals("1969-12-31T16:00:00-08:00", result);
    }

    @Test(timeout = 4000)
    public void testFormatDateWithMillisAndNegativeOffset() {
        Date date = new Date(123456789L);
        TimeZone tz = TimeZone.getTimeZone("GMT-08:00");
        String result = ISO8601Utils.format(date, true, tz);
        assertEquals("1969-12-31T18:17:36.789-08:00", result);
    }

    @Test(timeout = 4000)
    public void testParseDateWithLeapSecond61() throws Exception {
        String input = "1970-01-01T00:00:61Z";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(59000L, result.getTime());
    }

    @Test(timeout = 4000)
    public void testParseDateWithSeconds59() throws Exception {
        String input = "1970-01-01T00:00:59Z";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(59000L, result.getTime());
    }

    @Test(timeout = 4000)
    public void testParseDateWithFractionDigitsMoreThan3() throws Exception {
        // indexOfNonDigit will stop at first non-digit, parseEndOffset limited to 3 digits
        String input = "1970-01-01T00:00:00.123456Z";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(123L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateWithNoSeconds() throws Exception {
        // Format: yyyy-MM-ddThh:mm (no seconds)
        String input = "1970-01-01T01:00+01:00";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(0L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateWithNoMinutes() throws Exception {
        // Format: yyyy-MM-ddThh (no minutes) - but minutes are required? Actually code expects minutes after hours.
        // This will fail because after hours it expects either ':' or timezone indicator.
        // Let's test a valid case: yyyyMMddThhmm (no colon)
        String input = "19700101T0100+0100";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(0L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateWithCompactFormat() throws Exception {
        String input = "19700101T010000+0100";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(0L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseDateWithCompactFormatAndMillis() throws Exception {
        String input = "19700101T010000.123+0100";
        ParsePosition pos = new ParsePosition(0);
        Date result = ISO8601Utils.parse(input, pos);
        assertEquals(123L, result.getTime());
        assertEquals(input.length(), pos.getIndex());
    }
}