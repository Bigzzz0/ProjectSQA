package com.fasterxml.jackson.databind.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.util.StdDateFormat
 *
 * Decision / Branch Coverage Targets:
 * - looksLikeISO8601: length < 7, length >= 7 with and without digits / '-'
 * - _parseDate: ISO8601 match vs all-digits (positive long, negative long) vs non-digit (RFC-1123)
 * - parseAsISO8601 / _parseAsISO8601:
 *     - dateStr ending with 'Z' vs non-'Z' with custom timezone vs null _timezone
 *     - totalLen <= 10: PATTERN_PLAIN match (yyyy-MM-dd) vs mismatch
 *     - totalLen > 10: PATTERN_ISO8601 match vs mismatch
 *     - Optional time offset in ISO-8601: group 2 null / length 1 ('Z') / '+HH:mm' / '-HHmm'
 *     - Optional seconds in ISO-8601: charAt(16) == ':' vs omitted
 *     - Optional millisecond fractions: 0, 1, 2, 3, 4-9 digits, >9 digits (throws ParseException)
 * - _parseDateFromLong: NumberFormatException for out-of-range 64-bit int
 * - parseAsRFC1123: valid RFC1123 date string vs invalid
 * - format: withColonInTimeZone (true vs false), positive vs negative vs zero timezone offsets
 * - withTimeZone, withLocale, withLenient, withColonInTimeZone: equality short-circuits vs new instance
 * - equals, hashCode, clone, toString, toPattern
 *
 * Defects4J Targeted Defects:
 * 1. databind#2167: Year >= 10000 formatting. pad4(buffer, year) causes integer division error
 *    where `year / 100 >= 10`, leading to pad2 being called with a number >= 10 producing characters
 *    like ':' instead of digits. Expected: "+10204-01-01T00:00:00.000Z" (or "+10204...").
 * 2. BCE / BC Date formatting: Year 1 BCE should be formatted as "+0000-01-01..." but formatted as "0001-...".
 */
public class StdDateFormatGeminiTest {

    private final StdDateFormat std = new StdDateFormat();

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormatDefaultUTC() {
        // Epoch 0: 1970-01-01T00:00:00.000+0000
        Date d = new Date(0L);
        String formatted = std.format(d);
        assertEquals("1970-01-01T00:00:00.000+0000", formatted);
    }

    @Test(timeout = 4000)
    public void testFormatWithColonInTimeZone() {
        StdDateFormat colonFormat = std.withColonInTimeZone(true);
        assertTrue(colonFormat.isColonIncludedInTimeZone());
        assertFalse(std.isColonIncludedInTimeZone());

        Date d = new Date(0L);
        String formatted = colonFormat.format(d);
        assertEquals("1970-01-01T00:00:00.000+00:00", formatted);
    }

    @Test(timeout = 4000)
    public void testFormatWithNonZeroOffsetAndColon() {
        TimeZone tz = TimeZone.getTimeZone("GMT+02:30");
        StdDateFormat df = std.withTimeZone(tz).withColonInTimeZone(true);
        Date d = new Date(0L); // 1970-01-01 02:30:00.000+02:30
        String formatted = df.format(d);
        assertEquals("1970-01-01T02:30:00.000+02:30", formatted);
    }

    @Test(timeout = 4000)
    public void testFormatWithNegativeOffsetNoColon() {
        TimeZone tz = TimeZone.getTimeZone("GMT-05:00");
        StdDateFormat df = std.withTimeZone(tz).withColonInTimeZone(false);
        Date d = new Date(14400000L); // 4 hours after epoch -> 23:00 previous day in GMT-5
        String formatted = df.format(d);
        assertEquals("1969-12-31T23:00:00.000-0500", formatted);
    }

    @Test(timeout = 4000)
    public void testParsePlainDate() throws ParseException {
        Date d = std.parse("2020-05-17");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(d);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        assertEquals(17, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithZ() throws ParseException {
        Date d = std.parse("2021-12-31T23:59:59.123Z");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(d);
        assertEquals(2021, cal.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
        assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(23, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, cal.get(Calendar.MINUTE));
        assertEquals(59, cal.get(Calendar.SECOND));
        assertEquals(123, cal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithoutSeconds() throws ParseException {
        Date d = std.parse("2021-12-31T23:59Z");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(d);
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithOffsetPositiveAndNegative() throws ParseException {
        Date d1 = std.parse("2020-01-01T12:00:00+02:00");
        Date d2 = std.parse("2020-01-01T08:00:00-02:00");
        assertEquals(d1.getTime(), d2.getTime());

        Date d3 = std.parse("2020-01-01T12:00:00+0200");
        assertEquals(d1.getTime(), d3.getTime());

        Date d4 = std.parse("2020-01-01T12:00:00+02");
        assertEquals(d1.getTime(), d4.getTime());
    }

    @Test(timeout = 4000)
    public void testParseRFC1123() throws ParseException {
        String rfc = "Thu, 01 Jan 1970 00:00:00 GMT";
        Date d = std.parse(rfc);
        assertEquals(0L, d.getTime());
    }

    @Test(timeout = 4000)
    public void testParseTimestampStrings() throws ParseException {
        Date dPositive = std.parse("1234567890");
        assertEquals(1234567890L, dPositive.getTime());

        Date dZero = std.parse("0");
        assertEquals(0L, dZero.getTime());

        Date dNegative = std.parse("-5000");
        assertEquals(-5000L, dNegative.getTime());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseFractionsDifferentLengths() throws ParseException {
        // 1 digit millis -> 100ms
        Date d1 = std.parse("2020-01-01T00:00:00.1Z");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(d1);
        assertEquals(100, cal.get(Calendar.MILLISECOND));

        // 2 digits millis -> 120ms
        Date d2 = std.parse("2020-01-01T00:00:00.12Z");
        cal.setTime(d2);
        assertEquals(120, cal.get(Calendar.MILLISECOND));

        // 3 digits millis -> 123ms
        Date d3 = std.parse("2020-01-01T00:00:00.123Z");
        cal.setTime(d3);
        assertEquals(123, cal.get(Calendar.MILLISECOND));

        // 4 to 9 digits millis (allowed, capped at nanos, millis parsed from first 3 digits)
        Date d6 = std.parse("2020-01-01T00:00:00.123456Z");
        cal.setTime(d6);
        assertEquals(123, cal.get(Calendar.MILLISECOND));

        Date d9 = std.parse("2020-01-01T00:00:00.987654321Z");
        cal.setTime(d9);
        assertEquals(987, cal.get(Calendar.MILLISECOND));
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseFractionsExceeding9Digits() throws ParseException {
        // > 9 digits of sub-second fraction must throw ParseException
        std.parse("2020-01-01T00:00:00.1234567890Z");
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseInvalidNumericTimestampRange() throws ParseException {
        // Exceeds 64-bit Long max value
        std.parse("999999999999999999999999999999999");
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseInvalidStringFormat() throws ParseException {
        std.parse("not-a-valid-date");
    }

    @Test(timeout = 4000)
    public void testLooksLikeISO8601EdgeCases() throws ParseException {
        // Short length < 7 cannot be ISO8601
        ParsePosition pos = new ParsePosition(0);
        Date d = std.parse("123", pos);
        assertNotNull(d);
        assertEquals(123L, d.getTime());

        // String >= 7 but not matching digit/dash pattern
        ParsePosition pos2 = new ParsePosition(0);
        Date d2 = std.parse("abcdefghijk", pos2);
        assertNull(d2);
    }

    @Test(timeout = 4000)
    public void testParseWithParsePosition() {
        ParsePosition pos = new ParsePosition(0);
        Date d = std.parse("2021-05-01", pos);
        assertNotNull(d);

        // Parsing invalid input using parse(String, ParsePosition) should return null without throwing
        ParsePosition posInvalid = new ParsePosition(0);
        Date dInvalid = std.parse("completely invalid input", posInvalid);
        assertNull(dInvalid);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDateISO8601_10k() {
        // Targets Defects4J bug: pad4() fails for 5-digit years >= 10000.
        // GregorianCalendar year 10204: pad4() computed (10204/100) = 102,
        // then pad2(102) produced ':' because '0' + 10 = ':'.
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(10204, Calendar.JANUARY, 1, 0, 0, 0);
        Date date = cal.getTime();

        String formatted = std.format(date);
        // Correct ISO-8601 representation requires '+10204' or at minimum does not output ':'
        assertTrue("Formatted year >= 10000 should start with +10204: was " + formatted,
                formatted.startsWith("+10204"));
    }

    @Test(timeout = 4000)
    public void testDateISO8601_BCE() {
        // Targets Defects4J bug: handling BCE dates (e.g. 1 BCE).
        // Year 1 BCE in GregorianCalendar is represented as ERA = GregorianCalendar.BC, YEAR = 1.
        // It must be serialized conforming to ISO-8601 BCE standard (+0000 or -0001 depending on standard).
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 1);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date date = cal.getTime();

        String formatted = std.format(date);
        assertTrue("Year 1 BCE should be formatted starting with '+0000': was " + formatted,
                formatted.startsWith("+0000"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseExceptionOnEmptyString() throws ParseException {
        std.parse("");
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseExceptionOnWhitespaceOnly() throws ParseException {
        std.parse("   ");
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseISO8601MalformedMonth() throws ParseException {
        // Fits length <= 10, but not PATTERN_PLAIN
        std.parse("2020-99-99");
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseISO8601MalformedTime() throws ParseException {
        // Fits ISO8601 prefix, but malformed time part
        std.parse("2020-01-01T99:99:99");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testMutantFactories() {
        // withTimeZone
        StdDateFormat sameTz = std.withTimeZone(TimeZone.getTimeZone("UTC"));
        assertSame(std, sameTz);

        StdDateFormat diffTz = std.withTimeZone(TimeZone.getTimeZone("PST"));
        assertNotSame(std, diffTz);
        assertEquals(TimeZone.getTimeZone("PST"), diffTz.getTimeZone());

        StdDateFormat nullTz = diffTz.withTimeZone(null);
        assertEquals(StdDateFormat.getDefaultTimeZone(), nullTz.getTimeZone());

        // withLocale
        StdDateFormat sameLoc = std.withLocale(Locale.US);
        assertSame(std, sameLoc);

        StdDateFormat diffLoc = std.withLocale(Locale.GERMANY);
        assertNotSame(std, diffLoc);

        // withLenient
        assertTrue(std.isLenient());
        StdDateFormat strict = std.withLenient(Boolean.FALSE);
        assertNotSame(std, strict);
        assertFalse(strict.isLenient());

        StdDateFormat sameStrict = strict.withLenient(Boolean.FALSE);
        assertSame(strict, sameStrict);

        // withColonInTimeZone
        StdDateFormat sameColon = std.withColonInTimeZone(false);
        assertSame(std, sameColon);

        StdDateFormat diffColon = std.withColonInTimeZone(true);
        assertNotSame(std, diffColon);
        assertTrue(diffColon.isColonIncludedInTimeZone());
    }

    @Test(timeout = 4000)
    public void testSetTimeZoneAndSetLenientStateChanges() {
        StdDateFormat df = std.clone();
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        df.setTimeZone(gmt);
        assertEquals(gmt, df.getTimeZone());

        // Setting same timezone should be no-op
        df.setTimeZone(gmt);
        assertEquals(gmt, df.getTimeZone());

        df.setLenient(false);
        assertFalse(df.isLenient());

        df.setLenient(true);
        assertTrue(df.isLenient());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        // StdDateFormat defines equals as reference identity: o == this
        assertEquals(std, std);
        assertNotEquals(std, std.clone());
        assertNotEquals(std, null);
        assertNotEquals(std, "some string");

        assertEquals(System.identityHashCode(std), std.hashCode());
    }

    @Test(timeout = 4000)
    public void testToStringAndToPattern() {
        String str = std.toString();
        assertNotNull(str);
        assertTrue(str.contains("DateFormat"));

        String pattern = std.toPattern();
        assertNotNull(pattern);
        assertTrue(pattern.contains(StdDateFormat.DATE_FORMAT_STR_ISO8601));
        assertTrue(pattern.contains("lenient"));

        StdDateFormat strict = std.withLenient(Boolean.FALSE);
        assertTrue(strict.toPattern().contains("strict"));
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testDeprecatedFactoryMethods() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale loc = Locale.FRANCE;

        DateFormat isoDf = StdDateFormat.getISO8601Format(tz, loc);
        assertNotNull(isoDf);
        assertEquals(tz, isoDf.getTimeZone());

        DateFormat rfcDf = StdDateFormat.getRFC1123Format(tz, loc);
        assertNotNull(rfcDf);
        assertEquals(tz, rfcDf.getTimeZone());

        StdDateFormat custom = new StdDateFormat(tz, loc);
        assertEquals(tz, custom.getTimeZone());
    }
}