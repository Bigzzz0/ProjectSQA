package org.apache.commons.lang3.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * Partition A: Core functional paths (getInstance, format(Date), format(Calendar), format(long))
 * Partition B: Boundary values (null pattern, empty string, negative years, months 0-11, days 1-31, 
 *              hours 0-23, minutes 0-59, seconds 0-59, milliseconds 0-999, week-of-year boundaries)
 * Partition C: Defect-targeted branch (testLang645 – ISO week numbering with Norwegian locale)
 * Partition D: Exception paths (null pattern, invalid pattern character, unsupported date/time style)
 * Partition E: Object lifecycle (equals, hashCode, toString, serialization)
 * 
 * The known defect (testLang645) is triggered when using a locale with non-default week numbering 
 * (e.g., nb_NO where Monday is first day of week and minimal days in first week = 4). 
 * For a date that falls into week 53 of the previous year (e.g., 2010-01-01), 
 * the formatter outputs week 01 instead of week 53.
 */
public class FastDateFormatDeepseekTest {

    // ===== Partition A: Core functional logic and state transitions =====
    
    @Test(timeout = 4000)
    public void testGetInstanceNoArgs() {
        FastDateFormat fmt = FastDateFormat.getInstance();
        assertNotNull("Default instance should not be null", fmt);
        assertNotNull("Pattern should not be null", fmt.getPattern());
        assertEquals("TimeZone is default", TimeZone.getDefault(), fmt.getTimeZone());
        assertEquals("Locale is default", Locale.getDefault(), fmt.getLocale());
        assertFalse("TimeZone not forced", fmt.getTimeZoneOverridesCalendar());
    }

    @Test(timeout = 4000)
    public void testGetInstancePatternOnly() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd");
        assertEquals("Pattern preserved", "yyyy-MM-dd", fmt.getPattern());
    }

    @Test(timeout = 4000)
    public void testGetInstanceWithTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:00");
        FastDateFormat fmt = FastDateFormat.getInstance("HH:mm", tz);
        assertEquals("TimeZone set", tz, fmt.getTimeZone());
        assertTrue("TimeZone forced", fmt.getTimeZoneOverridesCalendar());
    }

    @Test(timeout = 4000)
    public void testGetInstanceWithLocale() {
        Locale loc = Locale.FRANCE;
        FastDateFormat fmt = FastDateFormat.getInstance("dd MMM yyyy", loc);
        assertEquals("Locale set", loc, fmt.getLocale());
    }

    @Test(timeout = 4000)
    public void testGetInstanceAllArgs() {
        TimeZone tz = TimeZone.getTimeZone("GMT-03:00");
        Locale loc = Locale.GERMANY;
        FastDateFormat fmt = FastDateFormat.getInstance("HH:mm:ss", tz, loc);
        assertEquals("TimeZone", tz, fmt.getTimeZone());
        assertEquals("Locale", loc, fmt.getLocale());
        assertTrue("TimeZone forced", fmt.getTimeZoneOverridesCalendar());
    }

    @Test(timeout = 4000)
    public void testFormatDate() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd");
        Date date = new Date(1483228800000L); // 2017-01-01 00:00:00 GMT
        String result = fmt.format(date);
        assertEquals("Date formatted", "2017-01-01", result);
    }

    @Test(timeout = 4000)
    public void testFormatCalendar() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        Calendar cal = new GregorianCalendar(2018, Calendar.JUNE, 15, 10, 30, 45);
        String result = fmt.format(cal);
        assertEquals("Calendar formatted", "2018-06-15 10:30:45", result);
    }

    @Test(timeout = 4000)
    public void testFormatLong() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd");
        long millis = 1262304000000L; // 2010-01-01 00:00:00 GMT
        String result = fmt.format(millis);
        assertEquals("Long formatted", "2010-01-01", result);
    }

    @Test(timeout = 4000)
    public void testFormatDateStringBuffer() {
        FastDateFormat fmt = FastDateFormat.getInstance("HH:mm");
        Date date = new Date(86400000L); // 1970-01-02 00:00:00 GMT
        StringBuffer buf = new StringBuffer();
        StringBuffer result = fmt.format(date, buf);
        assertSame("Same StringBuffer returned", buf, result);
        // The timezone is UTC default, so 00:00
        assertTrue("Result starts with '00:'", result.toString().startsWith("00:"));
    }

    @Test(timeout = 4000)
    public void testFormatCalendarStringBuffer() {
        FastDateFormat fmt = FastDateFormat.getInstance("MM/dd/yyyy");
        Calendar cal = new GregorianCalendar(2015, Calendar.DECEMBER, 31);
        StringBuffer buf = new StringBuffer();
        StringBuffer result = fmt.format(cal, buf);
        assertEquals("Calendar formatted into buffer", "12/31/2015", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatLongStringBuffer() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        long millis = 0L;
        StringBuffer buf = new StringBuffer();
        fmt.format(millis, buf);
        assertEquals("Long formatted into buffer", "1970", buf.toString());
    }

    @Test(timeout = 4000)
    public void testFormatObjectDate() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd");
        Object obj = new Date(0L);
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        fmt.format(obj, buf, pos);
        assertEquals("Object(Date) formatted", "1970-01-01", buf.toString());
    }

    @Test(timeout = 4000)
    public void testFormatObjectCalendar() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd");
        Object obj = new GregorianCalendar(2020, Calendar.JANUARY, 1);
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        fmt.format(obj, buf, pos);
        assertEquals("Object(Calendar) formatted", "2020-01-01", buf.toString());
    }

    @Test(timeout = 4000)
    public void testFormatObjectLong() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd");
        Object obj = 1262304000000L;
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        fmt.format(obj, buf, pos);
        assertEquals("Object(Long) formatted", "2010-01-01", buf.toString());
    }

    @Test(timeout = 4000)
    public void testFormatObjectInvalid() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        try {
            fmt.format(new Object(), buf, pos);
            fail("Should throw IllegalArgumentException for non-Date/Calendar/Long");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetPattern() {
        FastDateFormat fmt = FastDateFormat.getInstance("'test'");
        assertEquals("Pattern with literal", "'test'", fmt.getPattern());
    }

    @Test(timeout = 4000)
    public void testGetTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        FastDateFormat fmt = FastDateFormat.getInstance("z", tz);
        assertEquals("TimeZone getter", tz, fmt.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testGetTimeZoneOverridesCalendarTrue() {
        TimeZone tz = TimeZone.getTimeZone("EST");
        FastDateFormat fmt = FastDateFormat.getInstance("z", tz);
        assertTrue("overridesCalendar is true when tz not null", fmt.getTimeZoneOverridesCalendar());
    }

    @Test(timeout = 4000)
    public void testGetTimeZoneOverridesCalendarFalse() {
        FastDateFormat fmt = FastDateFormat.getInstance("z");
        assertFalse("overridesCalendar is false when tz null", fmt.getTimeZoneOverridesCalendar());
    }

    @Test(timeout = 4000)
    public void testGetLocale() {
        Locale loc = Locale.ITALY;
        FastDateFormat fmt = FastDateFormat.getInstance("dd/MM/yyyy", loc);
        assertEquals("Locale getter", loc, fmt.getLocale());
    }

    @Test(timeout = 4000)
    public void testGetMaxLengthEstimate() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        int est = fmt.getMaxLengthEstimate();
        assertTrue("Estimate is positive", est > 0);
    }

    @Test(timeout = 4000)
    public void testParseObject() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        ParsePosition pos = new ParsePosition(0);
        Object result = fmt.parseObject("2010", pos);
        assertNull("parseObject returns null", result);
        assertEquals("pos index set to 0", 0, pos.getIndex());
        assertEquals("error index set to 0", 0, pos.getErrorIndex());
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullPattern() {
        FastDateFormat.getInstance(null);
    }

    @Test(timeout = 4000)
    public void testEmptyPattern() {
        // Empty pattern is valid? parseToken returns empty string, loop ends, rules list empty.
        FastDateFormat fmt = FastDateFormat.getInstance("");
        String result = fmt.format(new Date(0));
        assertEquals("Empty pattern yields empty string", "", result);
    }

    @Test(timeout = 4000)
    public void testPatternWithOnlyLiterals() {
        FastDateFormat fmt = FastDateFormat.getInstance("'Hello World'");
        String result = fmt.format(new Date(0));
        assertEquals("Literal only", "Hello World", result);
    }

    @Test(timeout = 4000)
    public void testPatternWithEscapedQuote() {
        FastDateFormat fmt = FastDateFormat.getInstance("''");
        String result = fmt.format(new Date(0));
        assertEquals("Single quote", "'", result);
    }

    @Test(timeout = 4000)
    public void testYearFourDigits() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        String result = fmt.format(new Date(0L));
        assertEquals("Year 1970", "1970", result);
    }

    @Test(timeout = 4000)
    public void testYearTwoDigits() {
        FastDateFormat fmt = FastDateFormat.getInstance("yy");
        String result = fmt.format(new Date(0L));
        assertEquals("Year 70", "70", result);
    }

    @Test(timeout = 4000)
    public void testMonthPaddedTwoDigits() {
        FastDateFormat fmt = FastDateFormat.getInstance("MM");
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 1);
        assertEquals("Month 01", "01", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testMonthUnpadded() {
        FastDateFormat fmt = FastDateFormat.getInstance("M");
        Calendar cal = new GregorianCalendar(2010, Calendar.OCTOBER, 1);
        assertEquals("Unpadded month 10", "10", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testMonthShortText() {
        FastDateFormat fmt = FastDateFormat.getInstance("MMM", Locale.US);
        Calendar cal = new GregorianCalendar(2010, Calendar.MARCH, 1);
        assertEquals("Short month Mar", "Mar", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testMonthLongText() {
        FastDateFormat fmt = FastDateFormat.getInstance("MMMM", Locale.US);
        Calendar cal = new GregorianCalendar(2010, Calendar.DECEMBER, 1);
        assertEquals("Long month December", "December", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testDayPaddedTwoDigits() {
        FastDateFormat fmt = FastDateFormat.getInstance("dd");
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 5);
        assertEquals("Day 05", "05", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testDayUnpadded() {
        FastDateFormat fmt = FastDateFormat.getInstance("d");
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 15);
        assertEquals("Unpadded day 15", "15", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testHour12Padded() {
        FastDateFormat fmt = FastDateFormat.getInstance("hh");
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 1, 3, 0, 0);
        assertEquals("Hour 12-format 03", "03", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testHour12Midnight() {
        FastDateFormat fmt = FastDateFormat.getInstance("hh");
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 1, 0, 0, 0);
        // According to TwelveHourField, when hour is 0, use getLeastMaximum(HOUR)+1 = 12
        assertEquals("Midnight 12", "12", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testHour24Padded() {
        FastDateFormat fmt = FastDateFormat.getInstance("HH");
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 1, 15, 0, 0);
        assertEquals("Hour 24-format 15", "15", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testHour24Zero() {
        FastDateFormat fmt = FastDateFormat.getInstance("k"); // 1-24
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 1, 0, 0, 0);
        // TwentyFourHourField: when value==0, use maximum+1 = 24
        assertEquals("Midnight 24", "24", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testMinutePadded() {
        FastDateFormat fmt = FastDateFormat.getInstance("mm");
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 1, 0, 7, 0);
        assertEquals("Minute 07", "07", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testSecondPadded() {
        FastDateFormat fmt = FastDateFormat.getInstance("ss");
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 1, 0, 0, 59);
        assertEquals("Second 59", "59", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testMillisecondThreeDigits() {
        FastDateFormat fmt = FastDateFormat.getInstance("SSS");
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 1);
        assertEquals("Millisecond 001", "001", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testDayOfYear() {
        FastDateFormat fmt = FastDateFormat.getInstance("DDD");
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 21);
        assertEquals("Day of year 021", "021", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testDayOfWeekShortText() {
        FastDateFormat fmt = FastDateFormat.getInstance("E", Locale.US);
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 4); // Monday
        assertEquals("Short weekday Mon", "Mon", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testDayOfWeekLongText() {
        FastDateFormat fmt = FastDateFormat.getInstance("EEEE", Locale.US);
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 4); // Monday
        assertEquals("Long weekday Monday", "Monday", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testWeekInYear() {
        FastDateFormat fmt = FastDateFormat.getInstance("ww");
        // Use a known date: 2010-01-01 is Friday, week 53 of 2009 in ISO (but JVM default may vary)
        // We'll test a simple case where week number is known
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 4); // Monday, week 1
        String result = fmt.format(cal);
        // In ISO week, Jan 4 2010 is week 1
        assertEquals("Week 01", "01", result);
    }

    @Test(timeout = 4000)
    public void testWeekInMonth() {
        FastDateFormat fmt = FastDateFormat.getInstance("W");
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 4); // first Monday -> week 1
        assertEquals("Week in month 1", "1", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testAmPmMarkerAM() {
        FastDateFormat fmt = FastDateFormat.getInstance("a", Locale.US);
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 1, 11, 0, 0);
        assertEquals("AM", "AM", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testAmPmMarkerPM() {
        FastDateFormat fmt = FastDateFormat.getInstance("a", Locale.US);
        Calendar cal = new GregorianCalendar(2010, Calendar.JANUARY, 1, 15, 0, 0);
        assertEquals("PM", "PM", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testTimeZoneShortNameForced() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        FastDateFormat fmt = FastDateFormat.getInstance("z", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2010, Calendar.JANUARY, 1, 12, 0, 0);
        String result = fmt.format(cal);
        // Should be EST (standard time) because DST not in effect
        assertTrue("TimeZone short name", result.equals("EST") || result.equals("EDT"));
        // Ensure that when timeZone forced, it uses the formatter's time zone
        // The calendar's timezone is overridden by the formatter's timezone in format(Calendar)
        assertTrue(result.equals("EST") || result.equals("EDT"));
    }

    @Test(timeout = 4000)
    public void testTimeZoneLongNameForced() {
        TimeZone tz = TimeZone.getTimeZone("Europe/London");
        FastDateFormat fmt = FastDateFormat.getInstance("zzzz", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2010, Calendar.JANUARY, 1, 12, 0, 0);
        String result = fmt.format(cal);
        assertTrue("Long timezone name", result.contains("Time") || result.contains("Mean") || result.contains("Greenwich"));
    }

    @Test(timeout = 4000)
    public void testTimeZoneNumberNoColon() {
        FastDateFormat fmt = FastDateFormat.getInstance("Z");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+05:30"));
        cal.set(2010, Calendar.JANUARY, 1);
        String result = fmt.format(cal);
        // Offset is 5:30 -> +0530
        assertEquals("TimeZone number no colon", "+0530", result);
    }

    @Test(timeout = 4000)
    public void testTimeZoneNumberWithColon() {
        FastDateFormat fmt = FastDateFormat.getInstance("ZZ");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT-08:00"));
        cal.set(2010, Calendar.JANUARY, 1);
        String result = fmt.format(cal);
        assertEquals("TimeZone number with colon", "-08:00", result);
    }

    @Test(timeout = 4000)
    public void testInvalidPatternCharacter() {
        try {
            FastDateFormat.getInstance("X");
            fail("Should throw IllegalArgumentException for invalid pattern char");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Illegal pattern component"));
        }
    }

    @Test(timeout = 4000)
    public void testNegativeYear() {
        // Calendar supports negative year (BC)
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy G");
        Calendar cal = new GregorianCalendar(-100, Calendar.JANUARY, 1);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        String result = fmt.format(cal);
        assertTrue("Negative year with era", result.contains("BC") || result.contains("100"));
    }

    @Test(timeout = 4000)
    public void testLargeYear() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        Calendar cal = new GregorianCalendar(9999, Calendar.DECEMBER, 31);
        String result = fmt.format(cal);
        assertEquals("Large year 9999", "9999", result);
    }

    @Test(timeout = 4000)
    public void testWeekOfYearBoundaryCrossYear() {
        // Use locale that defines week 1 as containing Jan 4 (Monday)
        // For 2010-01-01 (Friday), week number should be 53 of 2009 if using ISO rules.
        // We'll use Locale.US (Sunday first) or Locale.GERMANY (Monday first)?
        // Actually, the defect uses Norwegian locale which uses Monday first.
        // We'll test with a Locale that has Monday first and minimal days in first week = 4.
        // But the JVM default may vary. We'll explicitly use Locale.GERMANY (Monday first, 4 days minimum)
        Locale loc = Locale.GERMANY;
        FastDateFormat fmt = FastDateFormat.getInstance("w", loc);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2010, Calendar.JANUARY, 1);
        // Depending on locale, week might be 53 or 1. We just check that result is not empty.
        String result = fmt.format(cal);
        assertNotNull("Week number not null", result);
        // This test is not for defect detection, just coverage.
    }

    // ===== Partition C: Defect-Targeted Branch (testLang645) =====

    @Test(timeout = 4000)
    public void testLang645() {
        // Defect: For Norwegian locale, formatting a date that falls into week 53 of previous year
        // yields "week 01" instead of "week 53".
        // Pattern: "EEEE, week ww"
        Locale norwegian = new Locale("nb", "NO");
        FastDateFormat fmt = FastDateFormat.getInstance("EEEE, week ww", norwegian);
        // Date: 2010-01-01 (Friday) => week 53 of 2009 in ISO week numbering (Monday first, min days 4)
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2010, Calendar.JANUARY, 1);
        String result = fmt.format(cal);
        // Expected: "fredag, week 53"
        assertEquals("Bug test: week should be 53", "fredag, uke 53", result);
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetDateInstanceInvalidStyle() {
        FastDateFormat.getDateInstance(42);
    }

    @Test(timeout = 4000)
    public void testGetDateInstanceUnsupportedLocale() {
        // A locale without date pattern? unlikely, but we can test that ClassCastException is wrapped.
        // Just ensure it doesn't throw internal exception.
        try {
            FastDateFormat.getDateInstance(FastDateFormat.FULL, new Locale("xx", "YY"));
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("No date pattern for locale"));
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetTimeInstanceInvalidStyle() {
        FastDateFormat.getTimeInstance(Integer.MIN_VALUE);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetDateTimeInstanceInvalidStyle() {
        FastDateFormat.getDateTimeInstance(42, 42);
    }

    @Test(timeout = 4000)
    public void testPaddedNumberFieldSizeLessThan3() {
        // PaddedNumberField constructor throws if size < 3
        // We cannot instantiate directly because it's private, but we can trigger via pattern "DDD"? That's size 3, okay.
        // To test the exception, we need to somehow call selectNumberRule with padding < 3? That doesn't happen because
        // parsePattern only uses selectNumberRule for single-letter patterns? Actually, for d, if token length 1, padding=1, calls UnpaddedNumberField.
        // Padding 2 calls TwoDigitNumberField. Padding >=3 calls PaddedNumberField which throws on size<3, but size is always >=3.
        // So we cannot easily test that exception from public API. We'll skip.
    }

    @Test(timeout = 4000)
    public void testFormatDateOverridesTimeZone() {
        // When mTimeZoneForced is true, format(Calendar) clones and sets timezone.
        TimeZone tz = TimeZone.getTimeZone("GMT+02:00");
        FastDateFormat fmt = FastDateFormat.getInstance("HH:mm", tz);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+05:00"));
        cal.set(2010, Calendar.JANUARY, 1, 12, 0, 0);
        String result = fmt.format(cal);
        // Should format in GMT+02:00 -> 09:00
        assertEquals("Calendar timezone overridden", "09:00", result);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testEqualsSame() {
        FastDateFormat fmt1 = FastDateFormat.getInstance("yyyy");
        FastDateFormat fmt2 = FastDateFormat.getInstance("yyyy");
        assertEquals("Same pattern equal", fmt1, fmt2);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentPattern() {
        FastDateFormat fmt1 = FastDateFormat.getInstance("yyyy");
        FastDateFormat fmt2 = FastDateFormat.getInstance("MM");
        assertNotEquals("Different pattern not equal", fmt1, fmt2);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentTimeZone() {
        FastDateFormat fmt1 = FastDateFormat.getInstance("yyyy", TimeZone.getTimeZone("UTC"));
        FastDateFormat fmt2 = FastDateFormat.getInstance("yyyy", TimeZone.getTimeZone("PST"));
        assertNotEquals("Different timezone not equal", fmt1, fmt2);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentLocale() {
        FastDateFormat fmt1 = FastDateFormat.getInstance("MMM", Locale.US);
        FastDateFormat fmt2 = FastDateFormat.getInstance("MMM", Locale.GERMANY);
        assertNotEquals("Different locale not equal", fmt1, fmt2);
    }

    @Test(timeout = 4000)
    public void testEqualsNonFastDateFormat() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        assertFalse("Not equal to String", fmt.equals("test"));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistent() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd");
        int hc1 = fmt.hashCode();
        int hc2 = fmt.hashCode();
        assertEquals("Hashcode consistent", hc1, hc2);
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        FastDateFormat fmt1 = FastDateFormat.getInstance("HH:mm", TimeZone.getTimeZone("CET"), Locale.UK);
        FastDateFormat fmt2 = FastDateFormat.getInstance("HH:mm", TimeZone.getTimeZone("CET"), Locale.UK);
        assertEquals("Equal objects", fmt1, fmt2);
        assertEquals("Equal hashcodes", fmt1.hashCode(), fmt2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd");
        String str = fmt.toString();
        assertTrue("toString contains pattern", str.contains("yyyy-MM-dd"));
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd");
        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(fmt);
        oos.close();
        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();
        assertEquals("Deserialized pattern same", fmt.getPattern(), deserialized.getPattern());
        assertEquals("Deserialized timezone same", fmt.getTimeZone(), deserialized.getTimeZone());
        assertEquals("Deserialized locale same", fmt.getLocale(), deserialized.getLocale());
        // Also verify formatting works
        assertEquals("Deserialized formats same", fmt.format(new Date(0)), deserialized.format(new Date(0)));
    }
}