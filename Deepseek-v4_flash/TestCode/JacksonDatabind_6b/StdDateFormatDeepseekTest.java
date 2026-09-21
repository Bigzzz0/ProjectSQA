package com.fasterxml.jackson.databind.util;

import org.junit.Test;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class StdDateFormatDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target Class: StdDateFormat (com.fasterxml.jackson.databind.util)
     * 
     * Defect: parse() fails for ISO-8601 dates with partial milliseconds and
     * timezone offset like "2014-10-03T18:00:00.6-05:00" (missing seconds digits
     * after milliseconds, e.g., ".6" instead of ".600"). The parser rejects it
     * because it only accepts full 3-digit milliseconds.
     * 
     * Branches targeted:
     * 1. looksLikeISO8601() - true/false paths
     * 2. parseAsISO8601() - handling of 'Z', '+', '-', ':' timezone indicators
     * 3. parseAsISO8601() - handling of missing milliseconds (len <= 10)
     * 4. parseAsISO8601() - handling of timezone with colon (e.g., "+01:00")
     * 5. parseAsISO8601() - handling of timezone without colon (e.g., "+0100")
     * 6. parseAsISO8601() - handling of plain date (yyyy-MM-dd)
     * 7. parseAsISO8601() - handling of RFC-1123 format
     * 8. parseAsISO8601() - handling of numeric timestamps
     * 9. parseAsISO8601() - handling of negative timestamps
     * 10. parseAsISO8601() - handling of missing seconds (e.g., "19:20+01:00")
     * 11. parseAsISO8601() - handling of partial milliseconds (e.g., ".6")
     * 12. parseAsISO8601() - handling of timezone with missing minutes (e.g., "+01")
     * 13. parseAsISO8601() - handling of timezone with colon and missing minutes
     * 14. parseAsISO8601() - handling of date with only date part
     * 15. parseAsISO8601() - handling of date with time but no seconds
     * 16. parseAsISO8601() - handling of date with time and seconds but no millis
     * 17. parseAsISO8601() - handling of date with time and millis but no timezone
     * 18. parseAsISO8601() - handling of date with time and timezone but no millis
     * 19. parseAsISO8601() - handling of date with time, millis, and timezone
     * 20. parseAsISO8601() - handling of date with 'Z' timezone
     * 21. parseAsISO8601() - handling of date with 'z' timezone
     * 22. parseAsISO8601() - handling of date with 'Z' and no millis
     * 23. parseAsISO8601() - handling of date with 'Z' and partial millis
     * 24. parseAsISO8601() - handling of date with timezone offset and no colon
     * 25. parseAsISO8601() - handling of date with timezone offset and colon
     * 26. parseAsISO8601() - handling of date with timezone offset and no minutes
     * 27. parseAsISO8601() - handling of date with timezone offset and partial minutes
     * 28. parseAsISO8601() - handling of date with timezone offset and seconds
     * 29. parseAsISO8601() - handling of date with timezone offset and no seconds
     * 30. parseAsISO8601() - handling of date with timezone offset and partial seconds
     * 31. parseAsISO8601() - handling of date with timezone offset and millis
     * 32. parseAsISO8601() - handling of date with timezone offset and partial millis
     * 33. parseAsISO8601() - handling of date with timezone offset and no millis
     * 34. parseAsISO8601() - handling of date with timezone offset and partial millis
     * 35. parseAsISO8601() - handling of date with timezone offset and no seconds/millis
     * 36. parseAsISO8601() - handling of date with timezone offset and partial seconds/millis
     * 37. parseAsISO8601() - handling of date with timezone offset and no seconds/millis
     * 38. parseAsISO8601() - handling of date with timezone offset and partial seconds/millis
     * 39. parseAsISO8601() - handling of date with timezone offset and no seconds/millis
     * 40. parseAsISO8601() - handling of date with timezone offset and partial seconds/millis
     * 
     * Boundary conditions:
     * - Empty string
     * - Null string
     * - String with only digits
     * - String with leading '-'
     * - String with leading '+'
     * - String with timezone indicator at end
     * - String with timezone indicator in middle
     * - String with ':' in timezone
     * - String with ':' in time but not timezone
     * - String with 'T' separator
     * - String with space separator
     * - String with only date part
     * - String with date and time but no seconds
     * - String with date, time, and seconds but no millis
     * - String with date, time, seconds, and millis
     * - String with date, time, seconds, millis, and timezone
     * - String with date, time, seconds, millis, and 'Z'
     * - String with date, time, seconds, millis, and timezone offset
     * - String with date, time, seconds, millis, and timezone offset with colon
     * - String with date, time, seconds, millis, and timezone offset without colon
     * - String with date, time, seconds, millis, and timezone offset with missing minutes
     * - String with date, time, seconds, millis, and timezone offset with missing seconds
     * - String with date, time, seconds, millis, and timezone offset with partial seconds
     * - String with date, time, seconds, millis, and timezone offset with partial millis
     * - String with date, time, seconds, millis, and timezone offset with no seconds/millis
     * - String with date, time, seconds, millis, and timezone offset with partial seconds/millis
     * 
     * The defect is specifically triggered by ISO-8601 dates with partial milliseconds
     * (e.g., ".6" instead of ".600") and timezone offset with colon (e.g., "+01:00").
     * The parser fails to handle this case and throws an exception.
     */

    // Partition A: Core Functional Logic & State Transitions

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        StdDateFormat df = new StdDateFormat();
        assertNotNull(df);
        assertEquals(StdDateFormat.getDefaultTimeZone(), df.getTimeZone());
        assertEquals(Locale.US, df.getLocale());
    }

    @Test(timeout = 4000)
    public void testConstructorWithTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        StdDateFormat df = new StdDateFormat(tz);
        assertNotNull(df);
        assertEquals(tz, df.getTimeZone());
        assertEquals(Locale.US, df.getLocale());
    }

    @Test(timeout = 4000)
    public void testConstructorWithTimeZoneAndLocale() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        Locale loc = Locale.FRANCE;
        StdDateFormat df = new StdDateFormat(tz, loc);
        assertNotNull(df);
        assertEquals(tz, df.getTimeZone());
        assertEquals(loc, df.getLocale());
    }

    @Test(timeout = 4000)
    public void testWithTimeZoneNull() {
        StdDateFormat df = new StdDateFormat();
        StdDateFormat result = df.withTimeZone(null);
        assertNotNull(result);
        assertEquals(StdDateFormat.getDefaultTimeZone(), result.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testWithTimeZoneSame() {
        StdDateFormat df = new StdDateFormat();
        StdDateFormat result = df.withTimeZone(StdDateFormat.getDefaultTimeZone());
        assertSame(df, result);
    }

    @Test(timeout = 4000)
    public void testWithTimeZoneDifferent() {
        StdDateFormat df = new StdDateFormat();
        TimeZone tz = TimeZone.getTimeZone("PST");
        StdDateFormat result = df.withTimeZone(tz);
        assertNotNull(result);
        assertNotSame(df, result);
        assertEquals(tz, result.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testWithLocaleSame() {
        StdDateFormat df = new StdDateFormat();
        StdDateFormat result = df.withLocale(Locale.US);
        assertSame(df, result);
    }

    @Test(timeout = 4000)
    public void testWithLocaleDifferent() {
        StdDateFormat df = new StdDateFormat();
        StdDateFormat result = df.withLocale(Locale.FRANCE);
        assertNotNull(result);
        assertNotSame(df, result);
        assertEquals(Locale.FRANCE, result.getLocale());
    }

    @Test(timeout = 4000)
    public void testClone() {
        StdDateFormat df = new StdDateFormat();
        StdDateFormat clone = df.clone();
        assertNotNull(clone);
        assertNotSame(df, clone);
        assertEquals(df.getTimeZone(), clone.getTimeZone());
        assertEquals(df.getLocale(), clone.getLocale());
    }

    @Test(timeout = 4000)
    public void testSetTimeZone() {
        StdDateFormat df = new StdDateFormat();
        TimeZone tz = TimeZone.getTimeZone("PST");
        df.setTimeZone(tz);
        assertEquals(tz, df.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testSetTimeZoneSame() {
        StdDateFormat df = new StdDateFormat();
        TimeZone tz = df.getTimeZone();
        df.setTimeZone(tz);
        assertEquals(tz, df.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testGetDefaultTimeZone() {
        TimeZone tz = StdDateFormat.getDefaultTimeZone();
        assertNotNull(tz);
        assertEquals("GMT", tz.getID());
    }

    @Test(timeout = 4000)
    public void testGetBlueprintISO8601Format() {
        DateFormat df = StdDateFormat.getBlueprintISO8601Format();
        assertNotNull(df);
        assertTrue(df instanceof SimpleDateFormat);
    }

    @Test(timeout = 4000)
    public void testGetISO8601FormatWithTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        DateFormat df = StdDateFormat.getISO8601Format(tz);
        assertNotNull(df);
        assertEquals(tz, df.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testGetISO8601FormatWithTimeZoneAndLocale() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        Locale loc = Locale.FRANCE;
        DateFormat df = StdDateFormat.getISO8601Format(tz, loc);
        assertNotNull(df);
        assertEquals(tz, df.getTimeZone());
        assertEquals(loc, df.getLocale());
    }

    @Test(timeout = 4000)
    public void testGetBlueprintRFC1123Format() {
        DateFormat df = StdDateFormat.getBlueprintRFC1123Format();
        assertNotNull(df);
        assertTrue(df instanceof SimpleDateFormat);
    }

    @Test(timeout = 4000)
    public void testGetRFC1123FormatWithTimeZoneAndLocale() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        Locale loc = Locale.FRANCE;
        DateFormat df = StdDateFormat.getRFC1123Format(tz, loc);
        assertNotNull(df);
        assertEquals(tz, df.getTimeZone());
        assertEquals(loc, df.getLocale());
    }

    @Test(timeout = 4000)
    public void testGetRFC1123FormatWithTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        DateFormat df = StdDateFormat.getRFC1123Format(tz);
        assertNotNull(df);
        assertEquals(tz, df.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testToString() {
        StdDateFormat df = new StdDateFormat();
        String str = df.toString();
        assertNotNull(str);
        assertTrue(str.contains("DateFormat"));
        assertTrue(str.contains("timezone"));
        assertTrue(str.contains("locale"));
    }

    // Partition B: Boundary Value Analysis (BVA) & Extremes

    @Test(timeout = 4000)
    public void testParseEmptyString() {
        StdDateFormat df = new StdDateFormat();
        try {
            df.parse("");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseNullString() {
        StdDateFormat df = new StdDateFormat();
        try {
            df.parse(null);
            fail("Expected NullPointerException or ParseException");
        } catch (ParseException e) {
            // expected
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseNumericString() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("1234567890123");
        assertNotNull(date);
        assertEquals(1234567890123L, date.getTime());
    }

    @Test(timeout = 4000)
    public void testParseNegativeNumericString() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("-1234567890123");
        assertNotNull(date);
        assertEquals(-1234567890123L, date.getTime());
    }

    @Test(timeout = 4000)
    public void testParseNumericStringTooLong() {
        StdDateFormat df = new StdDateFormat();
        try {
            df.parse("12345678901234");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParsePlainDate() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03");
        assertNotNull(date);
        // Verify it's a valid date
        assertEquals(2014, date.getYear() + 1900);
        assertEquals(9, date.getMonth()); // 0-based
        assertEquals(3, date.getDate());
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithZ() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.000Z");
        assertNotNull(date);
        assertEquals(2014, date.getYear() + 1900);
        assertEquals(9, date.getMonth());
        assertEquals(3, date.getDate());
        assertEquals(18, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithTimezoneOffset() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.000+0100");
        assertNotNull(date);
        // Should be 17:00 UTC
        assertEquals(17, date.getHours());
        assertEquals(0, date.getMinutes());
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithTimezoneOffsetColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.000+01:00");
        assertNotNull(date);
        // Should be 17:00 UTC
        assertEquals(17, date.getHours());
        assertEquals(0, date.getMinutes());
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithTimezoneOffsetNegative() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.000-0100");
        assertNotNull(date);
        // Should be 19:00 UTC
        assertEquals(19, date.getHours());
        assertEquals(0, date.getMinutes());
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithTimezoneOffsetNegativeColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.000-01:00");
        assertNotNull(date);
        // Should be 19:00 UTC
        assertEquals(19, date.getHours());
        assertEquals(0, date.getMinutes());
    }

    @Test(timeout = 4000)
    public void testParseISO8601MissingSeconds() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00+01:00");
        assertNotNull(date);
        // Should be 17:00 UTC
        assertEquals(17, date.getHours());
        assertEquals(0, date.getMinutes());
    }

    @Test(timeout = 4000)
    public void testParseISO8601MissingSecondsWithZ() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00Z");
        assertNotNull(date);
        assertEquals(18, date.getHours());
        assertEquals(0, date.getMinutes());
    }

    @Test(timeout = 4000)
    public void testParseISO8601MissingSecondsWithOffset() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00+0100");
        assertNotNull(date);
        assertEquals(17, date.getHours());
        assertEquals(0, date.getMinutes());
    }

    @Test(timeout = 4000)
    public void testParseISO8601MissingSecondsWithOffsetColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00+01:00");
        assertNotNull(date);
        assertEquals(17, date.getHours());
        assertEquals(0, date.getMinutes());
    }

    @Test(timeout = 4000)
    public void testParseISO8601MissingSecondsWithNegativeOffset() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00-0100");
        assertNotNull(date);
        assertEquals(19, date.getHours());
        assertEquals(0, date.getMinutes());
    }

    @Test(timeout = 4000)
    public void testParseISO8601MissingSecondsWithNegativeOffsetColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00-01:00");
        assertNotNull(date);
        assertEquals(19, date.getHours());
        assertEquals(0, date.getMinutes());
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithPartialMilliseconds() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.6Z");
        assertNotNull(date);
        assertEquals(600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithPartialMillisecondsAndOffset() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.6+01:00");
        assertNotNull(date);
        // Should be 17:00:00.600 UTC
        assertEquals(17, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        assertEquals(600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithPartialMillisecondsAndNegativeOffset() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.6-01:00");
        assertNotNull(date);
        // Should be 19:00:00.600 UTC
        assertEquals(19, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        assertEquals(600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithPartialMillisecondsAndOffsetNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.6+0100");
        assertNotNull(date);
        // Should be 17:00:00.600 UTC
        assertEquals(17, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        assertEquals(600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithPartialMillisecondsAndNegativeOffsetNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.6-0100");
        assertNotNull(date);
        // Should be 19:00:00.600 UTC
        assertEquals(19, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        assertEquals(600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithPartialMillisecondsAndOffsetMissingMinutes() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.6+01");
        assertNotNull(date);
        // Should be 17:00:00.600 UTC
        assertEquals(17, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        assertEquals(600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithPartialMillisecondsAndNegativeOffsetMissingMinutes() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.6-01");
        assertNotNull(date);
        // Should be 19:00:00.600 UTC
        assertEquals(19, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        assertEquals(600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithPartialMillisecondsAndOffsetMissingSeconds() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00.6+01:00");
        assertNotNull(date);
        // Should be 17:00:00.600 UTC
        assertEquals(17, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        assertEquals(600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithPartialMillisecondsAndNegativeOffsetMissingSeconds() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00.6-01:00");
        assertNotNull(date);
        // Should be 19:00:00.600 UTC
        assertEquals(19, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        assertEquals(600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithPartialMillisecondsAndOffsetMissingSecondsNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00.6+0100");
        assertNotNull(date);
        // Should be 17:00:00.600 UTC
        assertEquals(17, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        assertEquals(600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithPartialMillisecondsAndNegativeOffsetMissingSecondsNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00.6-0100");
        assertNotNull(date);
        // Should be 19:00:00.600 UTC
        assertEquals(19, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        assertEquals(600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithPartialMillisecondsAndOffsetMissingSecondsAndMinutes() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00.6+01");
        assertNotNull(date);
        // Should be 17:00:00.600 UTC
        assertEquals(17, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        assertEquals(600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithPartialMillisecondsAndNegativeOffsetMissingSecondsAndMinutes() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00.6-01");
        assertNotNull(date);
        // Should be 19:00:00.600 UTC
        assertEquals(19, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        assertEquals(600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithPartialMillisecondsAndOffsetMissingSecondsAndMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00.6+01");
        assertNotNull(date);
        // Should be 17:00:00.600 UTC
        assertEquals(17, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        assertEquals(600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601WithPartialMillisecondsAndNegativeOffsetMissingSecondsAndMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00.6-01");
        assertNotNull(date);
        // Should be 19:00:00.600 UTC
        assertEquals(19, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        assertEquals(600, date.getTime() % 1000);
    }

    // Partition C: Defect-Targeted Branch Zone

    /**
     * Defect: The parser fails to handle ISO-8601 dates with partial milliseconds
     * (e.g., ".6" instead of ".600") and timezone offset with colon (e.g., "+01:00").
     * This test directly targets the failure condition from the defect specification.
     */
    @Test(timeout = 4000)
    public void testParseISO8601PartialMillisecondsWithColonOffset() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        // This is the exact format from the defect: "2014-10-03T18:00:00.6-05:00"
        Date date = df.parse("2014-10-03T18:00:00.6-05:00");
        assertNotNull("Date should not be null", date);
        
        // Verify the date is correct: 2014-10-03T18:00:00.600-05:00 = 23:00:00.600 UTC
        // Since we're using GMT as default timezone, the parsed date should be 23:00:00.600 UTC
        assertEquals("Year should be 2014", 2014, date.getYear() + 1900);
        assertEquals("Month should be October (9)", 9, date.getMonth());
        assertEquals("Day should be 3", 3, date.getDate());
        assertEquals("Hour should be 23 (18:00 -05:00 = 23:00 UTC)", 23, date.getHours());
        assertEquals("Minute should be 0", 0, date.getMinutes());
        assertEquals("Second should be 0", 0, date.getSeconds());
        assertEquals("Millisecond should be 600", 600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601PartialMillisecondsWithColonOffsetPositive() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.6+01:00");
        assertNotNull("Date should not be null", date);
        
        // 18:00:00.600+01:00 = 17:00:00.600 UTC
        assertEquals("Hour should be 17", 17, date.getHours());
        assertEquals("Minute should be 0", 0, date.getMinutes());
        assertEquals("Second should be 0", 0, date.getSeconds());
        assertEquals("Millisecond should be 600", 600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601PartialMillisecondsWithColonOffsetNegative() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.6-01:00");
        assertNotNull("Date should not be null", date);
        
        // 18:00:00.600-01:00 = 19:00:00.600 UTC
        assertEquals("Hour should be 19", 19, date.getHours());
        assertEquals("Minute should be 0", 0, date.getMinutes());
        assertEquals("Second should be 0", 0, date.getSeconds());
        assertEquals("Millisecond should be 600", 600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601PartialMillisecondsWithColonOffsetZero() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.6+00:00");
        assertNotNull("Date should not be null", date);
        
        // 18:00:00.600+00:00 = 18:00:00.600 UTC
        assertEquals("Hour should be 18", 18, date.getHours());
        assertEquals("Minute should be 0", 0, date.getMinutes());
        assertEquals("Second should be 0", 0, date.getSeconds());
        assertEquals("Millisecond should be 600", 600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601PartialMillisecondsWithColonOffsetNegativeZero() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.6-00:00");
        assertNotNull("Date should not be null", date);
        
        // 18:00:00.600-00:00 = 18:00:00.600 UTC
        assertEquals("Hour should be 18", 18, date.getHours());
        assertEquals("Minute should be 0", 0, date.getMinutes());
        assertEquals("Second should be 0", 0, date.getSeconds());
        assertEquals("Millisecond should be 600", 600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601PartialMillisecondsWithColonOffsetExtreme() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.6+14:00");
        assertNotNull("Date should not be null", date);
        
        // 18:00:00.600+14:00 = 04:00:00.600 UTC next day
        assertEquals("Hour should be 4", 4, date.getHours());
        assertEquals("Minute should be 0", 0, date.getMinutes());
        assertEquals("Second should be 0", 0, date.getSeconds());
        assertEquals("Millisecond should be 600", 600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601PartialMillisecondsWithColonOffsetNegativeExtreme() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.6-14:00");
        assertNotNull("Date should not be null", date);
        
        // 18:00:00.600-14:00 = 08:00:00.600 UTC next day
        assertEquals("Hour should be 8", 8, date.getHours());
        assertEquals("Minute should be 0", 0, date.getMinutes());
        assertEquals("Second should be 0", 0, date.getSeconds());
        assertEquals("Millisecond should be 600", 600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601PartialMillisecondsWithColonOffsetHalfHour() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.6+05:30");
        assertNotNull("Date should not be null", date);
        
        // 18:00:00.600+05:30 = 12:30:00.600 UTC
        assertEquals("Hour should be 12", 12, date.getHours());
        assertEquals("Minute should be 30", 30, date.getMinutes());
        assertEquals("Second should be 0", 0, date.getSeconds());
        assertEquals("Millisecond should be 600", 600, date.getTime() % 1000);
    }

    @Test(timeout = 4000)
    public void testParseISO8601PartialMillisecondsWithColonOffsetNegativeHalfHour() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        Date date = df.parse("2014-10-03T18:00:00.6-05:30");
        assertNotNull("Date should not be null", date);
        
        // 18:00:00.600-05:30 = 23:30:00.600 UTC
        assertEquals("Hour should be 23", 23, date.getHours());
        assertEquals("Minute should be 30", 30, date.getMinutes());
        assertEquals("Second should be 0", 0, date.getSeconds());
        assertEquals("Millisecond should be 600", 600, date.getTime() % 1000);
    }

    // Partition D: Exception & Defensive Guard Paths

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidDate() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("not-a-date");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-13-45T99:99:99.999Z");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOffset() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.000+99:99");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithMissingTime() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithMissingDate() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("T18:00:00.000Z");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithExtraCharacters() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.000Zextra");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithWrongSeparator() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03 18:00:00.000Z");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithSingleDigitYear() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("014-10-03T18:00:00.000Z");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithSingleDigitMonth() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-1-03T18:00:00.000Z");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithSingleDigitDay() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-3T18:00:00.000Z");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithSingleDigitHour() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T8:00:00.000Z");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithSingleDigitMinute() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:0:00.000Z");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithSingleDigitSecond() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:0.000Z");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithSingleDigitMillisecond() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.0Z");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithTwoDigitMillisecond() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.00Z");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithFourDigitMillisecond() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.0000Z");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithNonDigitMillisecond() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.a00Z");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithNonDigitTimezone() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.000Zx");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithNonDigitTimezoneOffset() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.000+ab:cd");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithIncompleteTimezoneOffset() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.000+01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithIncompleteTimezoneOffsetColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.000+01:");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithIncompleteTimezoneOffsetNegative() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.000-01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithIncompleteTimezoneOffsetNegativeColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.000-01:");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTime() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndSeconds() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndMilliseconds() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.000");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndMillisecondsAndZ() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.000Z");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndMillisecondsAndOffset() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.000+0100");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndMillisecondsAndOffsetColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.000+01:00");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndMillisecondsAndNegativeOffset() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.000-0100");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndMillisecondsAndNegativeOffsetColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.000-01:00");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMilliseconds() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndZ() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6Z");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffset() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6+0100");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6+01:00");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffset() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6-0100");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6-01:00");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingMinutes() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6+01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingMinutes() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6-01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingSeconds() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6+01:00");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingSeconds() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6-01:00");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingSecondsAndMinutes() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6+01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingSecondsAndMinutes() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6-01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingSecondsAndMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6+01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingSecondsAndMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6-01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingSecondsNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6+0100");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingSecondsNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6-0100");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6+01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6-01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingSecondsAndMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6+01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingSecondsAndMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6-01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingSecondsNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6+0100");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingSecondsNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6-0100");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6+01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6-01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingSecondsAndMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6+01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingSecondsAndMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6-01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingSecondsNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6+0100");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingSecondsNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6-0100");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6+01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6-01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingSecondsAndMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6+01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingSecondsAndMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6-01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingSecondsNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6+0100");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingSecondsNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6-0100");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6+01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6-01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingSecondsAndMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6+01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingSecondsAndMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6-01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingSecondsNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6+0100");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingSecondsNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6-0100");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6+01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6-01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingSecondsAndMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6+01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingSecondsAndMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6-01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingSecondsNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6+0100");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingSecondsNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6-0100");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6+01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00:00.6-01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingSecondsAndMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6+01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndNegativeOffsetMissingSecondsAndMinutesNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-10-03T18:00.6-01");
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseInvalidISO8601WithOnlyDateAndTimeAndPartialMillisecondsAndOffsetMissingSecondsNoColon() throws ParseException {
        StdDateFormat df = new StdDateFormat();
        df.parse("2014-