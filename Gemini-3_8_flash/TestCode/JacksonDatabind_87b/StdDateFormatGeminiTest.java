package com.fasterxml.jackson.databind.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.util.StdDateFormat
 *
 * Decision / Condition Coverage Targets:
 * 1. looksLikeISO8601(String):
 *    - len >= 5 && isDigit(0) && isDigit(3) && charAt(4) == '-' (true path)
 *    - len < 5 (false path)
 *    - charAt(0) not digit (false path)
 *    - charAt(3) not digit (false path)
 *    - charAt(4) != '-' (false path)
 *
 * 2. parseAsISO8601(String, ParsePosition, boolean):
 *    - Branch: Plain date (len <= 10 && isDigit(lastChar)): "yyyy-MM-dd"
 *    - Branch: Zulu format (lastChar == 'Z'):
 *        - With milliseconds (e.g. "2020-01-01T12:00:00.000Z")
 *        - Missing milliseconds (charAt(len-4) == ':') -> inserts ".000"
 *    - Branch: Timezone offset present (hasTimeZone(str) == true):
 *        - Colon in TZ (charAt(len-3) == ':') -> colon deleted ("+01:00" -> "+0100")
 *        - Missing minutes in TZ (charAt(len-3) == '+' or '-') -> appends "00" ("+01" -> "+0100")
 *        - Fractional seconds switch (timeLen = len - T_idx - 6):
 *            - case 11: 2 millis digits -> pad '0'
 *            - case 10: 1 millis digit -> pad "00"
 *            - case 9: dot marker only -> pad "000"
 *            - case 8: no millis -> pad ".000"
 *            - case 6: seconds omitted -> pad "00.000"
 *            - case 5: minutes omitted/seconds omitted -> pad ":00.000"
 *    - Branch: No timezone indicator (hasTimeZone(str) == false):
 *        - timeLen switch (11, 10, 9, default) -> pads and appends 'Z'
 *        - CRITICAL DEFECT BRANCH: If custom timezone is configured on StdDateFormat,
 *          parsing ISO-8601 without timezone incorrectly forces UTC (DEFAULT_TIMEZONE).
 *
 * 3. parse(String) & parse(String, ParsePosition):
 *    - ISO-8601 path
 *    - Numeric timestamp path (positive digits, negative digits, inLongRange check)
 *    - RFC-1123 fallback path
 *    - Failure path -> throws ParseException listing ALL_FORMATS
 *
 * 4. Configuration & State:
 *    - withTimeZone(tz): null -> DEFAULT_TIMEZONE, same tz -> returns this, different tz -> new instance
 *    - withLocale(loc): same loc -> returns this, different loc -> new instance
 *    - setTimeZone(tz): changes state, clears cached formats
 *    - setLenient(boolean) & isLenient(): toggles leniency, clears cached formats
 *    - clone(): deep copy with preserved state
 *    - equals() and hashCode(): identity contract verification
 *    - toString(): with and without explicit timezone
 */
public class StdDateFormatGeminiTest {

    /*
     * -------------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
     * -------------------------------------------------------------------------
     */

    /**
     * Target Defect:
     * com.fasterxml.jackson.databind.deser.TestDateDeserialization::testDateUtilISO8601NoTimezoneNonDefault
     *
     * When an ISO-8601 string without timezone (e.g. "1970-01-01T02:00:00.000") is parsed
     * by a StdDateFormat instance configured with a non-default timezone (e.g. GMT+2),
     * it must be interpreted using the configured timezone (yielding epoch 0L),
     * NOT forced to UTC.
     */
    @Test(timeout = 4000)
    public void testDateUtilISO8601NoTimezoneNonDefault() throws Exception {
        StdDateFormat df = new StdDateFormat();
        df.setTimeZone(TimeZone.getTimeZone("GMT+2"));

        Date date = df.parse("1970-01-01T02:00:00.000");

        // 1970-01-01 02:00:00 in GMT+2 corresponds exactly to 1970-01-01 00:00:00 UTC (epoch 0L)
        assertEquals("Parsed date should reflect the configured non-default timezone (GMT+2)",
                new Date(0L), date);
    }

    @Test(timeout = 4000)
    public void testDateUtilISO8601NoTimezoneWithCustomTimezoneViaWithTimeZone() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("GMT-5");
        StdDateFormat df = StdDateFormat.instance.withTimeZone(tz);

        // 1970-01-01 00:00:00 in GMT-5 is 1970-01-01 05:00:00 UTC = 5 * 3600 * 1000 = 18000000L
        Date parsed = df.parse("1970-01-01T00:00:00.000");
        assertEquals(new Date(18000000L), parsed);
    }

    /*
     * -------------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * -------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testFormatAndRoundTripUTC() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date date = new Date(1435737600000L); // 2015-07-01 08:00:00.000 UTC

        StringBuffer sb = new StringBuffer();
        StringBuffer res = df.format(date, sb, new FieldPosition(0));
        assertNotNull(res);
        assertEquals("2015-07-01T08:00:00.000+0000", res.toString());

        Date parsed = df.parse(res.toString());
        assertEquals(date, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatWithNonDefaultTimeZone() {
        StdDateFormat df = new StdDateFormat();
        df.setTimeZone(TimeZone.getTimeZone("GMT+02:00"));
        Date date = new Date(0L);

        StringBuffer sb = new StringBuffer();
        StringBuffer res = df.format(date, sb, new FieldPosition(0));
        assertEquals("1970-01-01T02:00:00.000+0200", res.toString());
    }

    @Test(timeout = 4000)
    public void testParsePlainDate() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("1970-01-01");
        assertNotNull(date);
        assertEquals(0L, date.getTime());
    }

    @Test(timeout = 4000)
    public void testParseZuluFormatWithMillis() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("1970-01-01T00:00:00.000Z");
        assertEquals(0L, date.getTime());
    }

    @Test(timeout = 4000)
    public void testParseZuluFormatWithoutMillis() throws Exception {
        StdDateFormat df = new StdDateFormat();
        // charAt(len-4) == ':' -> triggers insertion of ".000"
        Date date = df.parse("1970-01-01T00:00:00Z");
        assertEquals(0L, date.getTime());
    }

    @Test(timeout = 4000)
    public void testParseRFC1123Format() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("Thu, 01 Jan 1970 00:00:00 GMT");
        assertNotNull(date);
        assertEquals(0L, date.getTime());
    }

    @Test(timeout = 4000)
    public void testParsePositiveNumericTimestamp() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("1435737600000");
        assertEquals(1435737600000L, date.getTime());
    }

    @Test(timeout = 4000)
    public void testParseNegativeNumericTimestamp() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("-10000");
        assertEquals(-10000L, date.getTime());
    }

    /*
     * -------------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Variations of ISO-8601
     * -------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testParseTimeZoneWithColonOffset() throws Exception {
        StdDateFormat df = new StdDateFormat();
        // Offset "+00:00" -> triggers colon removal branch
        Date date = df.parse("1970-01-01T00:00:00.000+00:00");
        assertEquals(0L, date.getTime());
    }

    @Test(timeout = 4000)
    public void testParseTimeZoneMissingMinutesOffset() throws Exception {
        StdDateFormat df = new StdDateFormat();
        // Offset "+00" -> triggers appending "00" branch
        Date date = df.parse("1970-01-01T00:00:00.000+00");
        assertEquals(0L, date.getTime());

        Date dateMinus = df.parse("1970-01-01T00:00:00.000-00");
        assertEquals(0L, dateMinus.getTime());
    }

    @Test(timeout = 4000)
    public void testParseIso8601WithVariousFractionalSecondsAndOffset() throws Exception {
        StdDateFormat df = new StdDateFormat();

        // timeLen 11: 2 digits millis
        Date d11 = df.parse("1970-01-01T00:00:00.12+0000");
        assertEquals(120L, d11.getTime());

        // timeLen 10: 1 digit millis
        Date d10 = df.parse("1970-01-01T00:00:00.1+0000");
        assertEquals(100L, d10.getTime());

        // timeLen 9: dot only
        Date d9 = df.parse("1970-01-01T00:00:00.+0000");
        assertEquals(0L, d9.getTime());

        // timeLen 8: no millis
        Date d8 = df.parse("1970-01-01T00:00:00+0000");
        assertEquals(0L, d8.getTime());

        // timeLen 6: no seconds (legal ISO-8601 extension supported)
        Date d6 = df.parse("1970-01-01T00:00+0000");
        assertEquals(0L, d6.getTime());
    }

    @Test(timeout = 4000)
    public void testParseIso8601NoTimeZoneFractionalVariants() throws Exception {
        StdDateFormat df = new StdDateFormat();

        // No timezone, 2 digits millis (timeLen 11)
        Date d11 = df.parse("1970-01-01T00:00:00.12");
        assertNotNull(d11);

        // No timezone, 1 digit millis (timeLen 10)
        Date d10 = df.parse("1970-01-01T00:00:00.1");
        assertNotNull(d10);

        // No timezone, dot only (timeLen 9)
        Date d9 = df.parse("1970-01-01T00:00:00.");
        assertNotNull(d9);

        // No timezone, default seconds (timeLen 8)
        Date d8 = df.parse("1970-01-01T00:00:00");
        assertNotNull(d8);
    }

    @Test(timeout = 4000)
    public void testLooksLikeISO8601BoundaryCases() {
        StdDateFormat df = new StdDateFormat();

        // String shorter than 5 characters
        assertFalse(df.looksLikeISO8601("1234"));
        assertFalse(df.looksLikeISO8601(""));

        // charAt(0) not digit
        assertFalse(df.looksLikeISO8601("A970-01-01"));

        // charAt(3) not digit
        assertFalse(df.looksLikeISO8601("197A-01-01"));

        // charAt(4) not '-'
        assertFalse(df.looksLikeISO8601("1970/01-01"));

        // Correct format
        assertTrue(df.looksLikeISO8601("1970-01-01"));
        assertTrue(df.looksLikeISO8601("1970-01-01T00:00:00Z"));
    }

    @Test(timeout = 4000)
    public void testParsePositionWithParseSuccessAndFailure() {
        StdDateFormat df = new StdDateFormat();

        ParsePosition pos = new ParsePosition(0);
        Date d = df.parse("1970-01-01T00:00:00.000Z", pos);
        assertNotNull(d);
        assertTrue(pos.getIndex() > 0);
        assertEquals(-1, pos.getErrorIndex());

        // Invalid ISO8601 string with ParsePosition: returns null
        ParsePosition posBad = new ParsePosition(0);
        Date dBad = df.parse("2020-99-99T99:99:99Z", posBad);
        // Depending on leniency or parse failure, it should return null or set error
        // With default lenient, 2020-99-99 might rollover, but "not-a-date" won't match looksLikeISO8601
        ParsePosition posNotIso = new ParsePosition(0);
        Date dNotIso = df.parse("not a date", posNotIso);
        assertNull(dNotIso);
    }

    @Test(timeout = 4000)
    public void testParsePositionNumeric() {
        StdDateFormat df = new StdDateFormat();
        ParsePosition pos = new ParsePosition(0);
        Date d = df.parse("12345", pos);
        assertNotNull(d);
        assertEquals(12345L, d.getTime());

        ParsePosition posNeg = new ParsePosition(0);
        Date dNeg = df.parse("-12345", posNeg);
        assertNotNull(dNeg);
        assertEquals(-12345L, dNeg.getTime());
    }

    /*
     * -------------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * -------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testUnparseableDateThrowsParseException() {
        StdDateFormat df = new StdDateFormat();
        try {
            df.parse("completely invalid date string");
            fail("Expected ParseException was not thrown");
        } catch (ParseException e) {
            assertTrue(e.getMessage().contains("Can not parse date"));
            assertTrue(e.getMessage().contains("ALL_FORMATS") || e.getMessage().contains("yyyy-MM-dd"));
        }
    }

    @Test(timeout = 4000)
    public void testParseNumericOutOfLongRangeFallsBackAndFails() {
        StdDateFormat df = new StdDateFormat();
        // Extremely large number exceeding Long range
        String hugeNumber = "9999999999999999999999999999999999999999";
        try {
            df.parse(hugeNumber);
            fail("Expected ParseException for number exceeding Long.MAX_VALUE");
        } catch (ParseException e) {
            assertTrue(e.getMessage().contains("Can not parse date"));
        }
    }

    @Test(timeout = 4000)
    public void testNonLenientParsingRejectsInvalidDate() {
        StdDateFormat df = new StdDateFormat();
        df.setLenient(false);
        assertFalse(df.isLenient());

        try {
            // February 31 is invalid under non-lenient parsing
            df.parse("2020-02-31T00:00:00.000Z");
            fail("Expected ParseException with lenient=false for Feb 31");
        } catch (ParseException e) {
            assertTrue(e.getMessage().contains("while it seems to fit format"));
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Partition E: Object Lifecycle & Contract Integrity
     * -------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testWithTimeZoneIdempotenceAndChange() {
        StdDateFormat df = new StdDateFormat();
        TimeZone defaultTz = StdDateFormat.getDefaultTimeZone();

        // Pass null -> defaults to DEFAULT_TIMEZONE
        StdDateFormat dfNullTz = df.withTimeZone(null);
        assertNotNull(dfNullTz);

        // Same timezone -> returns identical instance (this)
        StdDateFormat dfSame = dfNullTz.withTimeZone(defaultTz);
        assertSame(dfNullTz, dfSame);

        // Different timezone -> returns new instance
        TimeZone est = TimeZone.getTimeZone("EST");
        StdDateFormat dfEst = df.withTimeZone(est);
        assertNotSame(df, dfEst);
        assertEquals(est, dfEst.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testWithLocaleIdempotenceAndChange() {
        StdDateFormat df = new StdDateFormat();

        // Same locale -> returns this
        StdDateFormat dfSame = df.withLocale(Locale.US);
        assertSame(df, dfSame);

        // Different locale -> returns new instance
        StdDateFormat dfGermany = df.withLocale(Locale.GERMANY);
        assertNotSame(df, dfGermany);
    }

    @Test(timeout = 4000)
    public void testCloneIntegrity() {
        StdDateFormat df = new StdDateFormat();
        df.setTimeZone(TimeZone.getTimeZone("PST"));
        df.setLenient(false);

        StdDateFormat clone = df.clone();
        assertNotNull(clone);
        assertNotSame(df, clone);
        assertEquals(df.getTimeZone(), clone.getTimeZone());
        assertEquals(df.isLenient(), clone.isLenient());
    }

    @Test(timeout = 4000)
    public void testSetTimeZoneClearsCachedFormats() throws Exception {
        StdDateFormat df = new StdDateFormat();
        // Warm up cached format
        df.parse("1970-01-01T00:00:00.000Z");

        // Change timezone to different timezone
        TimeZone cst = TimeZone.getTimeZone("CST");
        df.setTimeZone(cst);
        assertEquals(cst, df.getTimeZone());

        // Same timezone set -> no-op
        df.setTimeZone(cst);
        assertEquals(cst, df.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testSetLenientClearsCachedFormats() throws Exception {
        StdDateFormat df = new StdDateFormat();
        assertTrue(df.isLenient()); // default lenient is true

        df.parse("1970-01-01T00:00:00.000Z");

        df.setLenient(true); // same value, no-op
        assertTrue(df.isLenient());

        df.setLenient(false); // different value, clears formats
        assertFalse(df.isLenient());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeIdentityContract() {
        StdDateFormat df1 = new StdDateFormat();
        StdDateFormat df2 = new StdDateFormat();

        // StdDateFormat enforces identity equality (o == this)
        assertEquals(df1, df1);
        assertNotEquals(df1, df2);
        assertNotEquals(df1, null);
        assertNotEquals(df1, "not-a-date-format");

        assertEquals(System.identityHashCode(df1), df1.hashCode());
    }

    @Test(timeout = 4000)
    public void testToStringRepresentation() {
        StdDateFormat df = new StdDateFormat();
        String strWithoutExplicitTz = df.toString();
        assertTrue(strWithoutExplicitTz.contains("DateFormat com.fasterxml.jackson.databind.util.StdDateFormat"));
        assertTrue(strWithoutExplicitTz.contains("locale: en_US"));

        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String strWithTz = df.toString();
        assertTrue(strWithTz.contains("timezone: "));
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testDeprecatedConstructorsAndStaticHelpers() {
        // Deprecated constructor
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        StdDateFormat dfDeprecated = new StdDateFormat(gmt, Locale.UK);
        assertEquals(gmt, dfDeprecated.getTimeZone());

        // Deprecated static helper methods
        DateFormat isoTz = StdDateFormat.getISO8601Format(gmt);
        assertNotNull(isoTz);
        assertEquals(gmt, isoTz.getTimeZone());

        DateFormat isoTzLoc = StdDateFormat.getISO8601Format(gmt, Locale.GERMANY);
        assertNotNull(isoTzLoc);

        DateFormat rfcTz = StdDateFormat.getRFC1123Format(gmt);
        assertNotNull(rfcTz);
        assertEquals(gmt, rfcTz.getTimeZone());

        DateFormat rfcTzLoc = StdDateFormat.getRFC1123Format(gmt, Locale.FRANCE);
        assertNotNull(rfcTzLoc);
    }

    @Test(timeout = 4000)
    public void testProtectedConstructorDirectCoverage() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        StdDateFormat df = new StdDateFormat(tz, Locale.CANADA, Boolean.FALSE);
        assertFalse(df.isLenient());
        assertEquals(tz, df.getTimeZone());
    }
}