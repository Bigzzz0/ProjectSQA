/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.util.StdDateFormat
 *
 * Defect Analysis (Defects4J):
 * 1. ISO-8601 parsing with missing seconds (e.g. "1997-07-16T19:20+01:00"):
 *    - The defective implementation assumes seconds and milliseconds are either complete or
 *      attempts naive offset-based insertion (charAt(len-9)), failing to handle dates without seconds.
 * 2. ISO-8601 parsing with partial milliseconds (e.g. "2014-10-03T18:00:00.6-05:00"):
 *    - The defective implementation assumes full milliseconds (.SSS) or no milliseconds at all.
 *      When given a 1- or 2-digit millisecond fraction, len-9 indexing misidentifies the separator,
 *      resulting in erroneous insertion of ".000" into invalid positions.
 *
 * Branch & Coverage Matrix:
 * - looksLikeISO8601:
 *   - len >= 5 & charAt(0) digit & charAt(3) digit & charAt(4) == '-' -> true
 *   - non-matching lengths / patterns -> false (RFC 1123 or timestamp fallback)
 * - parseAsISO8601:
 *   - Plain date format: len <= 10 and ends with digit (e.g. "2020-01-01")
 *   - Zulu format ('Z' suffix):
 *     - with milliseconds ("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
 *     - missing milliseconds ("yyyy-MM-dd'T'HH:mm:ssZ") -> inserts ".000"
 *   - Timezone offset variants:
 *     - hasTimeZone: "+hh", "+hhmm", "+hh:mm" (and "-" variants)
 *     - Colon in timezone offset (len-3 == ':') -> stripped
 *     - Missing minutes in timezone (len-3 == '+' or '-') -> appends "00"
 *     - Missing milliseconds with timezone -> inserts ".000"
 *   - No timezone indicator (non-Zulu, not plain):
 *     - appends ".000" if timeLen <= 8
 *     - appends 'Z'
 * - parse:
 *   - numeric timestamps (positive long, negative long, out of long range)
 *   - whitespace trimming
 *   - invalid formats -> ParseException listing ALL_FORMATS
 * - format:
 *   - default timezone (GMT) vs custom timezones
 *   - custom locales
 * - State and Lifecycle:
 *   - constructors: default, with TimeZone, with TimeZone and Locale
 *   - withTimeZone / withLocale (identity check vs new instance)
 *   - setTimeZone (invalidation of cached formats)
 *   - clone / toString / blueprint getters
 */

package com.fasterxml.jackson.databind.util;

import org.junit.Test;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class StdDateFormatGeminiTest {

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    /**********************************************************
     */

    /**
     * Targets known defect: ISO8601 string missing seconds with timezone offset.
     * Defective StdDateFormat fails to parse "1997-07-16T19:20+01:00" because it fails to correctly
     * detect missing seconds before inserting milliseconds and timezone offsets.
     */
    @Test(timeout = 4000)
    public void testDefectISO8601MissingSeconds() throws Exception {
        StdDateFormat df = new StdDateFormat();
        String dateStr = "1997-07-16T19:20+01:00";
        Date parsed = df.parse(dateStr);
        assertNotNull("Date should be successfully parsed even when seconds are omitted", parsed);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(parsed);
        assertEquals(1997, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, cal.get(Calendar.MONTH));
        assertEquals(16, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(18, cal.get(Calendar.HOUR_OF_DAY)); // 19:20 +01:00 == 18:20 GMT
        assertEquals(20, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
    }

    /**
     * Targets known defect: ISO8601 string with partial milliseconds (1 or 2 digits).
     * Defective StdDateFormat assumes milliseconds are either absent or exactly 3 digits,
     * corrupting strings like "2014-10-03T18:00:00.6-05:00".
     */
    @Test(timeout = 4000)
    public void testDefectISO8601PartialMilliseconds() throws Exception {
        StdDateFormat df = new StdDateFormat();
        String dateStr = "2014-10-03T18:00:00.6-05:00";
        Date parsed = df.parse(dateStr);
        assertNotNull("Date should be successfully parsed with partial milliseconds", parsed);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(parsed);
        assertEquals(2014, cal.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, cal.get(Calendar.MONTH));
        assertEquals(3, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(23, cal.get(Calendar.HOUR_OF_DAY)); // 18:00 -05:00 == 23:00 GMT
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(600, cal.get(Calendar.MILLISECOND));
    }

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testFormatAndParseRoundtripDefaultGMT() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date now = new Date(1600000000000L); // 2020-09-13T12:26:40.000Z
        String formatted = df.format(now);
        assertEquals("2020-09-13T12:26:40.000+0000", formatted);

        Date parsed = df.parse(formatted);
        assertEquals(now.getTime(), parsed.getTime());
    }

    @Test(timeout = 4000)
    public void testFormatWithCustomTimeZoneAndFieldPosition() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        StdDateFormat df = new StdDateFormat(tz, Locale.US);
        Date now = new Date(0L); // 1970-01-01T00:00:00.000Z -> 1969-12-31T16:00:00.000-0800 in PST
        StringBuffer sb = new StringBuffer("Timestamp: ");
        StringBuffer res = df.format(now, sb, new FieldPosition(0));
        assertEquals("Timestamp: 1969-12-31T16:00:00.000-0800", res.toString());
    }

    @Test(timeout = 4000)
    public void testParsePlainDateFormat() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2021-05-18");
        assertNotNull(date);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        assertEquals(2021, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        assertEquals(18, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithZuluAndMissingMillis() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2021-05-18T14:30:15Z");
        assertNotNull(date);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(15, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithZuluAndFullMillis() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2021-05-18T14:30:15.123Z");
        assertNotNull(date);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        assertEquals(123, cal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithColonedTimezone() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2021-05-18T14:30:15.000+02:00");
        assertNotNull(date);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY)); // 14:30 +02:00 == 12:30 GMT
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithMissingMinutesTimezone() throws Exception {
        StdDateFormat df = new StdDateFormat();
        // Missing minutes, e.g. "+02" -> appended "00" internally
        Date date = df.parse("2021-05-18T14:30:15.000+02");
        assertNotNull(date);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithoutTimezone() throws Exception {
        StdDateFormat df = new StdDateFormat();
        // Plain date-time without timezone: gets 'Z' appended
        Date date = df.parse("2021-05-18T14:30:15");
        assertNotNull(date);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(15, cal.get(Calendar.SECOND));
    }

    @Test(timeout = 4000)
    public void testParseRFC1123() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("Tue, 18 May 2021 14:30:15 GMT");
        assertNotNull(date);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        assertEquals(2021, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        assertEquals(18, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(15, cal.get(Calendar.SECOND));
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Timestamps
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testParseNumericTimestampPositive() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("1500000000");
        assertEquals(1500000000L, date.getTime());
    }

    @Test(timeout = 4000)
    public void testParseNumericTimestampNegative() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("-1500000000");
        assertEquals(-1500000000L, date.getTime());
    }

    @Test(timeout = 4000)
    public void testParseNumericTimestampZero() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("0");
        assertEquals(0L, date.getTime());
    }

    @Test(timeout = 4000)
    public void testParseNumericTimestampMaxLong() throws Exception {
        StdDateFormat df = new StdDateFormat();
        String maxLong = String.valueOf(Long.MAX_VALUE);
        Date date = df.parse(maxLong);
        assertEquals(Long.MAX_VALUE, date.getTime());
    }

    @Test(timeout = 4000)
    public void testParseNumericTimestampMinLong() throws Exception {
        StdDateFormat df = new StdDateFormat();
        String minLong = String.valueOf(Long.MIN_VALUE);
        Date date = df.parse(minLong);
        assertEquals(Long.MIN_VALUE, date.getTime());
    }

    @Test(timeout = 4000)
    public void testParseNumericTimestampOutOfLongRangeFallsBackToRFC1123() {
        StdDateFormat df = new StdDateFormat();
        // Number of digits exceeds Long.MAX_VALUE length, so inLongRange is false, falls back to RFC1123 and fails
        String tooLarge = "999999999999999999999999999";
        try {
            df.parse(tooLarge);
            fail("Expected ParseException for out of range numeric string");
        } catch (ParseException e) {
            assertTrue(e.getMessage().contains("Can not parse date"));
        }
    }

    @Test(timeout = 4000)
    public void testParseWithLeadingAndTrailingWhitespace() throws Exception {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("   2021-05-18   ");
        assertNotNull(date);
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testParseInvalidDateStringThrowsParseException() {
        StdDateFormat df = new StdDateFormat();
        try {
            df.parse("not-a-valid-date");
            fail("Expected ParseException for invalid date string");
        } catch (ParseException e) {
            assertTrue(e.getMessage().contains("not-a-valid-date"));
            assertTrue(e.getMessage().contains("not compatible with any of standard forms"));
        }
    }

    @Test(timeout = 4000)
    public void testParseEmptyStringThrowsParseException() {
        StdDateFormat df = new StdDateFormat();
        try {
            df.parse("");
            fail("Expected ParseException for empty date string");
        } catch (ParseException e) {
            assertTrue(e.getMessage().contains("Can not parse date"));
        }
    }

    @Test(timeout = 4000)
    public void testLooksLikeISO8601EdgeCases() {
        StdDateFormat df = new StdDateFormat();
        // Length < 5
        assertFalse(df.looksLikeISO8601("202"));
        assertFalse(df.looksLikeISO8601(""));
        // 5 chars but first is not digit
        assertFalse(df.looksLikeISO8601("a020-"));
        // 5 chars but 4th is not digit
        assertFalse(df.looksLikeISO8601("202a-"));
        // 5 chars but 5th is not '-'
        assertFalse(df.looksLikeISO8601("20200"));
        // Exactly valid prefix
        assertTrue(df.looksLikeISO8601("2020-"));
    }

    @Test(timeout = 4000)
    public void testParsePositionWithErrorIndex() {
        StdDateFormat df = new StdDateFormat();
        ParsePosition pos = new ParsePosition(0);
        Date date = df.parse("invalid", pos);
        assertNull(date);
        assertTrue(pos.getErrorIndex() >= 0);
    }

    /*
    /**********************************************************
    /* Partition E: Object Lifecycle & Contract Integrity
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testDefaultConstructorAndSingleton() {
        assertNotNull(StdDateFormat.instance);
        assertEquals(TimeZone.getTimeZone("GMT"), StdDateFormat.getDefaultTimeZone());
    }

    @SuppressWarnings("deprecation")
    @Test(timeout = 4000)
    public void testDeprecatedConstructorsAndGetters() {
        TimeZone tz = TimeZone.getTimeZone("EST");
        StdDateFormat df = new StdDateFormat(tz);
        assertNotNull(df);

        DateFormat bpISO = StdDateFormat.getBlueprintISO8601Format();
        assertNotNull(bpISO);

        DateFormat bpRFC = StdDateFormat.getBlueprintRFC1123Format();
        assertNotNull(bpRFC);

        DateFormat iso = StdDateFormat.getISO8601Format(tz);
        assertNotNull(iso);
        assertEquals(tz, iso.getTimeZone());

        DateFormat rfc = StdDateFormat.getRFC1123Format(tz);
        assertNotNull(rfc);
        assertEquals(tz, rfc.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testWithTimeZone() {
        StdDateFormat df = new StdDateFormat();
        TimeZone tz = TimeZone.getTimeZone("PST");
        StdDateFormat withTz = df.withTimeZone(tz);
        assertNotSame(df, withTz);

        // Same timezone returns self
        assertSame(withTz, withTz.withTimeZone(tz));

        // Passing null defaults to DEFAULT_TIMEZONE (GMT)
        StdDateFormat gmtDf = withTz.withTimeZone(null);
        assertNotNull(gmtDf);
    }

    @Test(timeout = 4000)
    public void testWithLocale() {
        StdDateFormat df = new StdDateFormat();
        Locale locale = Locale.GERMANY;
        StdDateFormat withLoc = df.withLocale(locale);
        assertNotSame(df, withLoc);

        // Same locale returns self
        assertSame(withLoc, withLoc.withLocale(locale));
    }

    @Test(timeout = 4000)
    public void testSetTimeZone() {
        StdDateFormat df = new StdDateFormat();
        // Warm up cached formatters
        df.format(new Date());

        TimeZone tz = TimeZone.getTimeZone("CST");
        df.setTimeZone(tz);
        // Setting same timezone should be no-op
        df.setTimeZone(tz);

        String formatted = df.format(new Date(0L));
        assertTrue(formatted.endsWith("-0600"));
    }

    @Test(timeout = 4000)
    public void testClone() {
        StdDateFormat df = new StdDateFormat(TimeZone.getTimeZone("GMT"), Locale.FRENCH);
        StdDateFormat copy = df.clone();
        assertNotSame(df, copy);
        assertEquals(df.toString(), copy.toString());
    }

    @Test(timeout = 4000)
    public void testToString() {
        StdDateFormat df = new StdDateFormat(TimeZone.getTimeZone("GMT"), Locale.US);
        String str = df.toString();
        assertTrue(str.contains("DateFormat"));
        assertTrue(str.contains("GMT"));
        assertTrue(str.contains("en_US") || str.contains("en"));
    }

    @Test(timeout = 4000)
    public void testGetISO8601AndRFC1123FormatWithNonDefaultLocale() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat isoFormat = StdDateFormat.getISO8601Format(tz, Locale.GERMAN);
        assertNotNull(isoFormat);

        DateFormat rfcFormat = StdDateFormat.getRFC1123Format(tz, Locale.GERMAN);
        assertNotNull(rfcFormat);
    }
}