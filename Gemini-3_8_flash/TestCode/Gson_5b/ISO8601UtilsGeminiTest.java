package com.google.gson.internal.bind.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: com.google.gson.internal.bind.util.ISO8601Utils
 * 
 * Branch & Path Coverage Points:
 * 1. format(Date), format(Date, boolean), format(Date, boolean, TimeZone):
 *    - millis == true / false
 *    - tz with rawOffset == 0 (UTC -> 'Z')
 *    - tz with rawOffset > 0 (e.g., GMT+02:00)
 *    - tz with rawOffset < 0 (e.g., GMT-05:00)
 * 2. parse(String, ParsePosition):
 *    - Date-only formats (no 'T', checkOffset returns false, date.length() <= offset):
 *      with hyphens (yyyy-MM-dd) and without hyphens (yyyyMMdd).
 *    - Date-time with 'T':
 *      - with ':' separators (hh:mm:ss) and without ':' separators (hhmmss).
 *      - optional seconds omitted: hh:mm.
 *      - optional millis omitted or present with 1 digit (.s -> * 100), 2 digits (.ss -> * 10),
 *        3 digits (.sss), and >3 digits (truncated to 3 digits).
 *      - leap seconds handling: seconds > 59 && seconds < 63 truncated to 59.
 *    - TimeZone parsing:
 *      - 'Z' -> TIMEZONE_UTC
 *      - Special cases: "+0000" and "+00:00" -> TIMEZONE_UTC
 *      - Offset with colons ("+02:00", "-05:00")
 *      - Offset without colons ("+0200", "-0500")
 *      - Offset without minutes ("+01", "+02", "-05") -> DEFECT ZONE!
 *      - Missing time zone indicator -> IllegalArgumentException -> ParseException
 *      - Invalid indicator char -> IndexOutOfBoundsException -> ParseException
 *      - Invalid numeric chars -> NumberFormatException -> ParseException
 *      - Calendar non-lenient validation failures (e.g., month 13, day 32) -> IllegalArgumentException -> ParseException
 * 3. Defect-Targeted Branch Zone:
 *    - Defects4J defect: 1-hour timezone offset without minutes (e.g., "1970-01-01T01:00:00+01")
 *      fails due to mismatch check in TimeZone.getTimeZone("GMT+01").getID() returning "GMT+01:00".
 * ----------------------------------------------------------------------------------------------------
 */
public class ISO8601UtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions (Formatting & Parsing)
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormatUtcWithoutMillis() {
        Date date = new Date(0L); // 1970-01-01T00:00:00.000Z
        String formatted = ISO8601Utils.format(date);
        assertEquals("1970-01-01T00:00:00Z", formatted);
    }

    @Test(timeout = 4000)
    public void testFormatUtcWithMillis() {
        Date date = new Date(123456789L);
        String formatted = ISO8601Utils.format(date, true);
        assertEquals("1970-01-02T10:17:36.789Z", formatted);
    }

    @Test(timeout = 4000)
    public void testFormatPositiveTimezoneOffset() {
        Date date = new Date(0L);
        TimeZone tz = TimeZone.getTimeZone("GMT+02:30");
        String formatted = ISO8601Utils.format(date, false, tz);
        assertEquals("1970-01-01T02:30:00+02:30", formatted);
    }

    @Test(timeout = 4000)
    public void testFormatNegativeTimezoneOffsetWithMillis() {
        Date date = new Date(123L);
        TimeZone tz = TimeZone.getTimeZone("GMT-05:00");
        String formatted = ISO8601Utils.format(date, true, tz);
        assertEquals("1969-12-31T19:00:00.123-05:00", formatted);
    }

    @Test(timeout = 4000)
    public void testParseStandardIsoUtcWithMillis() throws ParseException {
        String input = "2023-05-18T14:30:15.500Z";
        ParsePosition pos = new ParsePosition(0);
        Date parsed = ISO8601Utils.parse(input, pos);
        assertNotNull(parsed);
        assertEquals(input.length(), pos.getIndex());

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(parsed);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        assertEquals(18, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(15, cal.get(Calendar.SECOND));
        assertEquals(500, cal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testParseDateOnlyWithHyphens() throws ParseException {
        String input = "2023-05-18";
        ParsePosition pos = new ParsePosition(0);
        Date parsed = ISO8601Utils.parse(input, pos);
        assertNotNull(parsed);
        assertEquals(input.length(), pos.getIndex());

        Calendar cal = Calendar.getInstance();
        cal.setTime(parsed);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        assertEquals(18, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test(timeout = 4000)
    public void testParseDateOnlyWithoutHyphens() throws ParseException {
        String input = "20230518";
        ParsePosition pos = new ParsePosition(0);
        Date parsed = ISO8601Utils.parse(input, pos);
        assertNotNull(parsed);
        assertEquals(input.length(), pos.getIndex());

        Calendar cal = Calendar.getInstance();
        cal.setTime(parsed);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        assertEquals(18, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test(timeout = 4000)
    public void testParseCompactDateTimeWithoutSeparators() throws ParseException {
        String input = "20230518T143015Z";
        ParsePosition pos = new ParsePosition(0);
        Date parsed = ISO8601Utils.parse(input, pos);
        assertNotNull(parsed);
        assertEquals(input.length(), pos.getIndex());

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(parsed);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        assertEquals(18, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(15, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testParseTimeWithoutSeconds() throws ParseException {
        String input = "2023-05-18T14:30Z";
        ParsePosition pos = new ParsePosition(0);
        Date parsed = ISO8601Utils.parse(input, pos);
        assertNotNull(parsed);
        assertEquals(input.length(), pos.getIndex());

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(parsed);
        assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
    }

    @Test(timeout = 4000)
    public void testParseTimezoneZeroSpecialCases() throws ParseException {
        // "+0000" and "+00:00" explicitly map to TIMEZONE_UTC
        Date d1 = ISO8601Utils.parse("2020-01-01T00:00:00+0000", new ParsePosition(0));
        Date d2 = ISO8601Utils.parse("2020-01-01T00:00:00+00:00", new ParsePosition(0));
        Date d3 = ISO8601Utils.parse("2020-01-01T00:00:00Z", new ParsePosition(0));

        assertEquals(d3.getTime(), d1.getTime());
        assertEquals(d3.getTime(), d2.getTime());
    }

    @Test(timeout = 4000)
    public void testParseTimezoneWithAndWithoutColons() throws ParseException {
        Date withColon = ISO8601Utils.parse("2020-01-01T12:00:00+02:00", new ParsePosition(0));
        Date withoutColon = ISO8601Utils.parse("2020-01-01T12:00:00+0200", new ParsePosition(0));
        assertEquals(withColon.getTime(), withoutColon.getTime());

        Date negWithColon = ISO8601Utils.parse("2020-01-01T12:00:00-05:00", new ParsePosition(0));
        Date negWithoutColon = ISO8601Utils.parse("2020-01-01T12:00:00-0500", new ParsePosition(0));
        assertEquals(negWithColon.getTime(), negWithoutColon.getTime());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseFractionalSecondsOneDigit() throws ParseException {
        // 1 digit millis: .1 should be 100ms
        Date parsed = ISO8601Utils.parse("2020-01-01T00:00:00.1Z", new ParsePosition(0));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(parsed);
        assertEquals(100, cal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testParseFractionalSecondsTwoDigits() throws ParseException {
        // 2 digits millis: .12 should be 120ms
        Date parsed = ISO8601Utils.parse("2020-01-01T00:00:00.12Z", new ParsePosition(0));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(parsed);
        assertEquals(120, cal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testParseFractionalSecondsThreeDigits() throws ParseException {
        // 3 digits millis: .123 should be 123ms
        Date parsed = ISO8601Utils.parse("2020-01-01T00:00:00.123Z", new ParsePosition(0));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(parsed);
        assertEquals(123, cal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testParseFractionalSecondsMoreThanThreeDigitsTruncation() throws ParseException {
        // 4 digits millis: .1234 should truncate to 3 digits (123ms)
        Date parsed = ISO8601Utils.parse("2020-01-01T00:00:00.1234Z", new ParsePosition(0));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(parsed);
        assertEquals(123, cal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testParseLeapSecondsTruncation() throws ParseException {
        // Leap seconds 60, 61, 62 should be truncated to 59
        Date parsed60 = ISO8601Utils.parse("2020-01-01T23:59:60Z", new ParsePosition(0));
        Calendar cal60 = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal60.setTime(parsed60);
        assertEquals(59, cal60.get(Calendar.SECOND));

        Date parsed61 = ISO8601Utils.parse("2020-01-01T23:59:61Z", new ParsePosition(0));
        Calendar cal61 = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal61.setTime(parsed61);
        assertEquals(59, cal61.get(Calendar.SECOND));

        Date parsed62 = ISO8601Utils.parse("2020-01-01T23:59:62Z", new ParsePosition(0));
        Calendar cal62 = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal62.setTime(parsed62);
        assertEquals(59, cal62.get(Calendar.SECOND));
    }

    @Test(timeout = 4000)
    public void testParseNonZeroInitialParsePosition() throws ParseException {
        String prefix = "PREFIX_DATA_";
        String dateStr = "2020-01-01T00:00:00Z";
        String fullString = prefix + dateStr;
        ParsePosition pos = new ParsePosition(prefix.length());
        Date parsed = ISO8601Utils.parse(fullString, pos);
        assertNotNull(parsed);
        assertEquals(fullString.length(), pos.getIndex());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth Defect)
    // =========================================================================

    /**
     * Targets Defects4J known issue: 2-digit timezone offset without minutes (e.g., "+01", "+02", "-05").
     * The specification permits [Z|[+-]hh[:]mm] where timezone can be "+01" (comment notes: "+00:00, +0000 and +00").
     * On the defective code, TimeZone.getTimeZone("GMT+01").getID() resolves to "GMT+01:00",
     * which fails the strict equality check against "GMT+01" and causes an IndexOutOfBoundsException/ParseException.
     */
    @Test(timeout = 4000)
    public void testParseDateWithTwoDigitTimezoneWithoutMinutes() throws ParseException {
        String input = "1970-01-01T01:00:00+01";
        ParsePosition pos = new ParsePosition(0);
        Date parsed = ISO8601Utils.parse(input, pos);
        assertNotNull("Date should parse successfully for 2-digit timezone offset", parsed);
        assertEquals("Parsed index should match string length", input.length(), pos.getIndex());
        // 1970-01-01T01:00:00+01:00 is exactly UTC epoch 0 ms
        assertEquals(0L, parsed.getTime());
    }

    @Test(timeout = 4000)
    public void testParseDateWithNegativeTwoDigitTimezoneWithoutMinutes() throws ParseException {
        String input = "1969-12-31T23:00:00-01";
        ParsePosition pos = new ParsePosition(0);
        Date parsed = ISO8601Utils.parse(input, pos);
        assertNotNull(parsed);
        assertEquals(input.length(), pos.getIndex());
        assertEquals(0L, parsed.getTime());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseMissingTimezoneIndicatorThrowsParseException() {
        try {
            ISO8601Utils.parse("2020-01-01T12:00:00", new ParsePosition(0));
            fail("Expected ParseException when timezone is missing from datetime");
        } catch (ParseException expected) {
            assertTrue(expected.getMessage().contains("No time zone indicator"));
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test(timeout = 4000)
    public void testParseInvalidTimezoneIndicatorCharThrowsParseException() {
        try {
            ISO8601Utils.parse("2020-01-01T12:00:00X", new ParsePosition(0));
            fail("Expected ParseException when timezone indicator is not Z, +, or -");
        } catch (ParseException expected) {
            assertTrue(expected.getMessage().contains("Invalid time zone indicator 'X'"));
            assertTrue(expected.getCause() instanceof IndexOutOfBoundsException);
        }
    }

    @Test(timeout = 4000)
    public void testParseInvalidNumericCharsInDateThrowsParseException() {
        try {
            ISO8601Utils.parse("2020-XX-01T12:00:00Z", new ParsePosition(0));
            fail("Expected ParseException for non-numeric month");
        } catch (ParseException expected) {
            assertTrue(expected.getMessage().contains("Invalid number: XX"));
            assertTrue(expected.getCause() instanceof NumberFormatException);
        }
    }

    @Test(timeout = 4000)
    public void testParseStringTooShortThrowsParseException() {
        try {
            ISO8601Utils.parse("20", new ParsePosition(0));
            fail("Expected ParseException for string too short to contain year");
        } catch (ParseException expected) {
            assertTrue(expected.getCause() instanceof NumberFormatException);
        }
    }

    @Test(timeout = 4000)
    public void testParseNonLenientCalendarValuesThrowsParseException() {
        // Month 13 is invalid
        try {
            ISO8601Utils.parse("2020-13-01T12:00:00Z", new ParsePosition(0));
            fail("Expected ParseException for invalid calendar month");
        } catch (ParseException expected) {
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }

        // Day 32 is invalid
        try {
            ISO8601Utils.parse("2020-01-32T12:00:00Z", new ParsePosition(0));
            fail("Expected ParseException for invalid calendar day");
        } catch (ParseException expected) {
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }

        // Leap day in non-leap year (2019-02-29) is invalid
        try {
            ISO8601Utils.parse("2019-02-29T12:00:00Z", new ParsePosition(0));
            fail("Expected ParseException for non-existent leap day");
        } catch (ParseException expected) {
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test(timeout = 4000)
    public void testParseNullStringThrowsParseException() {
        try {
            ISO8601Utils.parse(null, new ParsePosition(0));
            fail("Expected NullPointerException or ParseException wrapped");
        } catch (NullPointerException | ParseException expected) {
            // Success: null input is defended against
        }
    }

    @Test(timeout = 4000)
    public void testParseInvalidTimezoneOffsetResolvesMismatchThrowsParseException() {
        // An invalid offset like +99:99 will fail to resolve into a matching TimeZone ID
        try {
            ISO8601Utils.parse("2020-01-01T12:00:00+99:99", new ParsePosition(0));
            fail("Expected ParseException for invalid timezone offset");
        } catch (ParseException expected) {
            assertTrue(expected.getCause() instanceof IndexOutOfBoundsException);
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testInstantiation() {
        ISO8601Utils instance = new ISO8601Utils();
        assertNotNull("Utility class should be instantiable without error", instance);
    }
}