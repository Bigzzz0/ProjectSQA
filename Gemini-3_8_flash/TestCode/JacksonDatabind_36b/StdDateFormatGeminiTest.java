package com.fasterxml.jackson.databind.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.util.StdDateFormat
 *
 * 1. Defect Coverage (Defects4J Ground Truth):
 *    - testLenient: Calling setLenient(false) on a freshly instantiated StdDateFormat
 *      causes a NullPointerException due to uninitialized internal Calendar instance in
 *      java.text.DateFormat base class.
 *
 * 2. Decision Branches Covered:
 *    - looksLikeISO8601:
 *      * Length < 5 vs >= 5
 *      * Non-digit at char 0 or 3
 *      * Dash vs non-dash at char 4
 *    - parseAsISO8601:
 *      * Plain date (length <= 10, ends with digit)
 *      * Zulu indicator (ends with 'Z'), with missing millis (char len-4 == ':') and explicit millis
 *      * Timezone indicator: hasTimeZone == true (+hh, +hhmm, +hh:mm, -hh, -hhmm, -hh:mm)
 *      * Timezone colon removal (c == ':')
 *      * Timezone missing minutes (c == '+' or '-')
 *      * Timezone timeLen switch (5, 6, 7, 8, 9, 10, 11)
 *      * Plain ISO without timezone: timeLen switch (9, 10, 11, default)
 *      * Non-lenient / unparseable error paths throwing ParseException
 *    - Stringified timestamps & Numeric inputs:
 *      * Pure digits vs negative numbers ('-' prefix)
 *      * Out-of-range long strings fallback
 *    - RFC 1123 parsing:
 *      * Standard RFC-1123 format strings
 *    - Configuration & Caching:
 *      * withTimeZone: same vs null vs different timezone
 *      * withLocale: same vs different locale
 *      * setTimeZone: same (no-op) vs different (clears formats)
 *      * clone: instance isolation and state replication
 *      * toString: with and without explicit timezone
 */
public class StdDateFormatGeminiTest {

    /*
     * ----------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testFormatAndParseRoundtripDefault() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date original = new Date(1577836800000L); // 2020-01-01T00:00:00.000Z
        String formatted = df.format(original);

        assertEquals("2020-01-01T00:00:00.000+0000", formatted);
        Date parsed = df.parse(formatted);
        assertEquals(original, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatWithFieldPositionAndStringBuffer() {
        StdDateFormat df = new StdDateFormat();
        Date date = new Date(0L);
        StringBuffer buffer = new StringBuffer("Prefix: ");
        FieldPosition pos = new FieldPosition(0);

        StringBuffer result = df.format(date, buffer, pos);
        assertSame(buffer, result);
        assertEquals("Prefix: 1970-01-01T00:00:00.000+0000", result.toString());
    }

    @Test(timeout = 4000)
    public void testParsePlainDateWithoutTime() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date parsed = df.parse("2021-05-17");
        assertNotNull(parsed);

        // Verify caching: second call exercises cached _formatPlain
        Date parsedCached = df.parse("2021-05-18");
        assertNotNull(parsedCached);
        assertTrue(parsedCached.after(parsed));
    }

    @Test(timeout = 4000)
    public void testParseZuluFormats() throws Exception {
        StdDateFormat df = new StdDateFormat();

        // Zulu without milliseconds (len-4 is ':') -> branch inserts .000
        Date d1 = df.parse("2021-05-17T12:30:45Z");
        assertNotNull(d1);

        // Zulu with milliseconds -> cached _formatISO8601_z
        Date d2 = df.parse("2021-05-17T12:30:45.123Z");
        assertNotNull(d2);
        assertTrue(d2.getTime() > d1.getTime());
    }

    @Test(timeout = 4000)
    public void testParseRfc1123Format() throws Exception {
        StdDateFormat df = new StdDateFormat();
        String rfcStr = "Sun, 06 Nov 1994 08:49:37 GMT";
        Date parsed = df.parse(rfcStr);
        assertNotNull(parsed);

        // Verify caching of _formatRFC1123
        Date parsed2 = df.parse("Mon, 07 Nov 1994 08:49:37 GMT");
        assertNotNull(parsed2);
    }

    @Test(timeout = 4000)
    public void testParseTimestampNumbers() throws Exception {
        StdDateFormat df = new StdDateFormat();

        // Positive numeric timestamp
        Date d1 = df.parse("1600000000000");
        assertEquals(1600000000000L, d1.getTime());

        // Negative numeric timestamp
        Date d2 = df.parse("-50000");
        assertEquals(-50000L, d2.getTime());
    }

    /*
     * ----------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testParseIso8601TimezoneVariants() throws Exception {
        StdDateFormat df = new StdDateFormat();

        // Colon in timezone (c == ':') -> delete colon branch
        Date d1 = df.parse("2020-01-01T12:00:00+02:00");
        assertNotNull(d1);

        // Missing timezone minutes (c == '+' or '-') -> appends '00' branch
        Date d2 = df.parse("2020-01-01T12:00:00+02");
        assertNotNull(d2);
        assertEquals(d1.getTime(), d2.getTime());

        // TimeLen switch cases with timezone
        // timeLen 5 (hh:mm)
        Date dLen5 = df.parse("2020-01-01T12:00+0000");
        assertNotNull(dLen5);

        // timeLen 8 (hh:mm:ss without millis)
        Date dLen8 = df.parse("2020-01-01T12:00:00+0000");
        assertNotNull(dLen8);

        // timeLen 10 (.s partial millis)
        Date dLen10 = df.parse("2020-01-01T12:00:00.1+0000");
        assertNotNull(dLen10);

        // timeLen 11 (.ss partial millis)
        Date dLen11 = df.parse("2020-01-01T12:00:00.12+0000");
        assertNotNull(dLen11);

        // timeLen 9 (. partial millis marker)
        Date dLen9 = df.parse("2020-01-01T12:00:00.+0000");
        assertNotNull(dLen9);
    }

    @Test(timeout = 4000)
    public void testParseIso8601NoTimezoneVariants() throws Exception {
        StdDateFormat df = new StdDateFormat();

        // No timezone, missing millis -> default switch appends .000Z
        Date dNoTzDefault = df.parse("2020-01-01T12:00:00");
        assertNotNull(dNoTzDefault);

        // No timezone, partial millis timeLen 11
        Date dNoTz11 = df.parse("2020-01-01T12:00:00.12");
        assertNotNull(dNoTz11);

        // No timezone, partial millis timeLen 10
        Date dNoTz10 = df.parse("2020-01-01T12:00:00.1");
        assertNotNull(dNoTz10);

        // No timezone, partial millis timeLen 9
        Date dNoTz9 = df.parse("2020-01-01T12:00:00.");
        assertNotNull(dNoTz9);
    }

    @Test(timeout = 4000)
    public void testParseWithParsePosition() {
        StdDateFormat df = new StdDateFormat();
        ParsePosition pos = new ParsePosition(0);

        Date dIso = df.parse("2021-01-01T00:00:00.000Z", pos);
        assertNotNull(dIso);
        assertEquals(0, pos.getErrorIndex());

        ParsePosition posNum = new ParsePosition(0);
        Date dNum = df.parse("1234567890", posNum);
        assertNotNull(dNum);

        ParsePosition posNeg = new ParsePosition(0);
        Date dNeg = df.parse("-123456", posNeg);
        assertNotNull(dNeg);

        ParsePosition posRfc = new ParsePosition(0);
        Date dRfc = df.parse("Sun, 06 Nov 1994 08:49:37 GMT", posRfc);
        assertNotNull(dRfc);
    }

    /*
     * ----------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
     * ----------------------------------------------------------------------
     */

    /**
     * Targets Defects4J bug: Calling setLenient(false) on StdDateFormat throws
     * NullPointerException because the superclass DateFormat.setLenient invokes
     * calendar.setLenient(...), but calendar is null in StdDateFormat.
     */
    @Test(timeout = 4000)
    public void testLenientDefect() {
        StdDateFormat df = new StdDateFormat();
        assertTrue("Default leniency should be true", df.isLenient());

        // This call reveals the NullPointerException defect in defective versions
        df.setLenient(false);
        assertFalse("Leniency should have updated to false", df.isLenient());
    }

    /*
     * ----------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testParseInvalidDateStringThrowsParseException() {
        StdDateFormat df = new StdDateFormat();
        try {
            df.parse("not-a-valid-date");
            fail("Expected ParseException was not thrown");
        } catch (ParseException pe) {
            assertTrue(pe.getMessage().contains("Can not parse date"));
            assertTrue(pe.getMessage().contains("not compatible with any of standard forms"));
        }
    }

    @Test(timeout = 4000)
    public void testParseNumericOutOfRangeFallsBackToRfcAndFails() {
        StdDateFormat df = new StdDateFormat();
        // Number too large for Long: NumberInput.inLongRange returns false -> RFC-1123 -> ParseException
        String overflowNumber = "999999999999999999999999999999";
        try {
            df.parse(overflowNumber);
            fail("Expected ParseException for huge numeric value");
        } catch (ParseException pe) {
            assertTrue(pe.getMessage().contains("Can not parse date"));
        }
    }

    @Test(timeout = 4000)
    public void testParseLooksLikeIsoMalformedThrowsParseException() {
        StdDateFormat df = new StdDateFormat();
        // Starts like ISO (digits, dash at 4) but is malformed
        try {
            df.parse("2020-xx-yy");
            fail("Expected ParseException for malformed ISO");
        } catch (ParseException pe) {
            assertTrue(pe.getMessage().contains("Can not parse date"));
        }
    }

    /*
     * ----------------------------------------------------------------------
     * Partition E: Object Lifecycle & Contract Integrity
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testWithTimeZoneAndSetTimeZone() throws Exception {
        StdDateFormat df = new StdDateFormat();
        TimeZone tzPst = TimeZone.getTimeZone("PST");

        // withTimeZone creates new instance
        StdDateFormat dfPst = df.withTimeZone(tzPst);
        assertNotSame(df, dfPst);
        assertEquals(tzPst, dfPst.getTimeZone());

        // withTimeZone same returns this
        assertSame(dfPst, dfPst.withTimeZone(tzPst));

        // withTimeZone null defaults to UTC
        StdDateFormat dfUtc = dfPst.withTimeZone(null);
        assertEquals(TimeZone.getTimeZone("UTC"), dfUtc.getTimeZone());

        // setTimeZone mutation & format clearing
        dfPst.format(new Date()); // Populates internal cached format
        TimeZone tzEst = TimeZone.getTimeZone("EST");
        dfPst.setTimeZone(tzEst); // Clears formats and updates _timezone
        assertEquals(tzEst, dfPst.getTimeZone());

        // setTimeZone same is a no-op
        dfPst.setTimeZone(tzEst);
        assertEquals(tzEst, dfPst.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testWithLocale() {
        StdDateFormat df = new StdDateFormat();

        // withLocale same returns this
        assertSame(df, df.withLocale(Locale.US));

        // withLocale different returns new instance
        StdDateFormat dfDe = df.withLocale(Locale.GERMANY);
        assertNotSame(df, dfDe);
        assertTrue(dfDe.toString().contains("de_DE"));
    }

    @Test(timeout = 4000)
    public void testCloneIntegrity() {
        StdDateFormat df = new StdDateFormat(TimeZone.getTimeZone("GMT+2"), Locale.FRANCE);
        StdDateFormat cloned = df.clone();

        assertNotSame(df, cloned);
        assertEquals(df.getTimeZone(), cloned.getTimeZone());
        assertEquals(df.isLenient(), cloned.isLenient());
        assertEquals(df.toString(), cloned.toString());
    }

    @Test(timeout = 4000)
    public void testToStringRepresentation() {
        StdDateFormat dfNoTz = new StdDateFormat();
        String str1 = dfNoTz.toString();
        assertTrue(str1.startsWith("DateFormat com.fasterxml.jackson.databind.util.StdDateFormat"));
        assertTrue(str1.contains("(locale: en_US)"));
        assertFalse(str1.contains("(timezone:"));

        StdDateFormat dfWithTz = dfNoTz.withTimeZone(TimeZone.getTimeZone("UTC"));
        String str2 = dfWithTz.toString();
        assertTrue(str2.contains("(timezone: "));
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testStaticHelperMethods() {
        assertEquals(TimeZone.getTimeZone("UTC"), StdDateFormat.getDefaultTimeZone());
        assertNotNull(StdDateFormat.instance);

        TimeZone tz = TimeZone.getTimeZone("GMT+1");
        DateFormat isoFmt = StdDateFormat.getISO8601Format(tz);
        assertNotNull(isoFmt);
        assertEquals(tz, isoFmt.getTimeZone());

        DateFormat rfcFmt = StdDateFormat.getRFC1123Format(tz);
        assertNotNull(rfcFmt);
        assertEquals(tz, rfcFmt.getTimeZone());

        DateFormat isoFmtLocale = StdDateFormat.getISO8601Format(tz, Locale.GERMANY);
        assertNotNull(isoFmtLocale);

        DateFormat rfcFmtLocale = StdDateFormat.getRFC1123Format(tz, Locale.GERMANY);
        assertNotNull(rfcFmtLocale);
    }
}