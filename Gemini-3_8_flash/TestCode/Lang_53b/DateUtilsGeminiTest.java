package org.apache.commons.lang.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TimeZone;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * 1. Defect LANG-346 (Defects4J Lang 53/58):
 *    - In DateUtils.modify(Calendar, int, boolean), when rounding to Calendar.SECOND or Calendar.MINUTE,
 *      the 'done' flag was set conditionally inside the truncation block (!round || millisecs < 500)
 *      or (!round || seconds < 30).
 *    - When rounding up with millisecs >= 500 or seconds >= 30, 'done' was never set to true.
 *    - As a result, subsequent units (seconds, minutes) were erroneously subtracted, resulting in
 *      corrupted values (e.g. 08:08:30 rounding to Calendar.MINUTE returned 08:01:00 instead of 08:09:00).
 * 2. Coverage Targets:
 *    - Equality checks: isSameDay, isSameInstant, isSameLocalTime across all fields & class types.
 *    - Add operations: addYears, addMonths, addWeeks, addDays, addHours, addMinutes, addSeconds, addMilliseconds.
 *    - Round / Truncate: modify method paths including SEMI_MONTH, AM_PM, MILLISECOND fast-path,
 *      large year (>280,000,000) ArithmeticException, and unsupported fields.
 *    - Range Iterator: all 6 range styles, startCutoff/endCutoff wraparounds, DateIterator traversal,
 *      and NoSuchElementException / UnsupportedOperationException guards.
 *    - Defensive Null & Type Handling: ClassCastException, IllegalArgumentException on invalid types.
 */
public class DateUtilsGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (LANG-346)
    // =========================================================================

    /**
     * Directly tests the defect reported in LANG-346 / Defects4J.
     * Rounding a date with seconds >= 30 to Calendar.MINUTE must round up to the next minute,
     * not corrupt the minute field by erroneously subtracting minutes.
     */
    @Test(timeout = 4000)
    public void testRoundLang346() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date input = sdf.parse("2007-07-02 08:08:30.000");
        Date rounded = DateUtils.round(input, Calendar.MINUTE);
        Date expected = sdf.parse("2007-07-02 08:09:00.000");
        assertEquals("Minute Round Up Failed", expected, rounded);
    }

    /**
     * Companion defect check for LANG-346 on Calendar.SECOND boundary when millis >= 500.
     */
    @Test(timeout = 4000)
    public void testRoundLang346Seconds() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date input = sdf.parse("2007-07-02 08:08:15.500");
        Date rounded = DateUtils.round(input, Calendar.SECOND);
        Date expected = sdf.parse("2007-07-02 08:08:16.000");
        assertEquals("Second Round Up Failed", expected, rounded);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsSameDay_Date() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.set(2023, Calendar.JANUARY, 10, 10, 30, 0);
        cal2.set(2023, Calendar.JANUARY, 10, 23, 59, 59);

        assertTrue(DateUtils.isSameDay(cal1.getTime(), cal2.getTime()));

        cal2.set(2023, Calendar.JANUARY, 11, 0, 0, 0);
        assertFalse(DateUtils.isSameDay(cal1.getTime(), cal2.getTime()));
    }

    @Test(timeout = 4000)
    public void testIsSameDay_CalendarBranches() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.set(2023, Calendar.MARCH, 15, 12, 0, 0);
        cal2.set(2023, Calendar.MARCH, 15, 18, 0, 0);

        assertTrue(DateUtils.isSameDay(cal1, cal2));

        // Different ERA
        cal2.set(Calendar.ERA, GregorianCalendar.BC);
        assertFalse(DateUtils.isSameDay(cal1, cal2));
        cal2.set(Calendar.ERA, GregorianCalendar.AD);

        // Different YEAR
        cal2.set(Calendar.YEAR, 2022);
        assertFalse(DateUtils.isSameDay(cal1, cal2));
        cal2.set(Calendar.YEAR, 2023);

        // Different DAY_OF_YEAR
        cal2.set(Calendar.DAY_OF_YEAR, cal1.get(Calendar.DAY_OF_YEAR) + 1);
        assertFalse(DateUtils.isSameDay(cal1, cal2));
    }

    @Test(timeout = 4000)
    public void testIsSameInstant() {
        Date date1 = new Date(1600000000000L);
        Date date2 = new Date(1600000000000L);
        Date date3 = new Date(1600000000001L);

        assertTrue(DateUtils.isSameInstant(date1, date2));
        assertFalse(DateUtils.isSameInstant(date1, date3));

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(date1);
        c2.setTime(date2);
        assertTrue(DateUtils.isSameInstant(c1, c2));

        c2.setTime(date3);
        assertFalse(DateUtils.isSameInstant(c1, c2));
    }

    @Test(timeout = 4000)
    public void testIsSameLocalTime_AllFields() {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = (Calendar) c1.clone();
        assertTrue(DateUtils.isSameLocalTime(c1, c2));

        // Subclass comparison branch
        Calendar customCal = new GregorianCalendar() {};
        customCal.setTime(c1.getTime());
        assertFalse(DateUtils.isSameLocalTime(c1, customCal));

        // Millisecond difference
        c2 = (Calendar) c1.clone();
        c2.add(Calendar.MILLISECOND, 1);
        assertFalse(DateUtils.isSameLocalTime(c1, c2));

        // Second difference
        c2 = (Calendar) c1.clone();
        c2.add(Calendar.SECOND, 1);
        assertFalse(DateUtils.isSameLocalTime(c1, c2));

        // Minute difference
        c2 = (Calendar) c1.clone();
        c2.add(Calendar.MINUTE, 1);
        assertFalse(DateUtils.isSameLocalTime(c1, c2));

        // Hour difference
        c2 = (Calendar) c1.clone();
        c2.add(Calendar.HOUR, 1);
        assertFalse(DateUtils.isSameLocalTime(c1, c2));

        // Day of year difference
        c2 = (Calendar) c1.clone();
        c2.add(Calendar.DAY_OF_YEAR, 1);
        assertFalse(DateUtils.isSameLocalTime(c1, c2));

        // Year difference
        c2 = (Calendar) c1.clone();
        c2.add(Calendar.YEAR, 1);
        assertFalse(DateUtils.isSameLocalTime(c1, c2));

        // Era difference
        c2 = (Calendar) c1.clone();
        c2.set(Calendar.ERA, GregorianCalendar.BC);
        assertFalse(DateUtils.isSameLocalTime(c1, c2));
    }

    @Test(timeout = 4000)
    public void testParseDate_Success() throws ParseException {
        String[] patterns = new Date[] {}.length == 0 ? new String[] {"yyyy-MM-dd", "yyyy/MM/dd HH:mm"} : null;
        Date parsed = DateUtils.parseDate("2023/05/20 14:15", patterns);
        assertNotNull(parsed);

        Calendar cal = Calendar.getInstance();
        cal.setTime(parsed);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        assertEquals(20, cal.get(Calendar.DATE));
        assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(15, cal.get(Calendar.MINUTE));
    }

    @Test(timeout = 4000)
    public void testAddMethods() {
        Date base = new Date(1000000000000L); // Sun Sep 09 01:46:40 UTC 2001

        Calendar expected = Calendar.getInstance();
        expected.setTime(base);
        expected.add(Calendar.YEAR, 2);
        assertEquals(expected.getTime(), DateUtils.addYears(base, 2));

        expected.setTime(base);
        expected.add(Calendar.MONTH, -3);
        assertEquals(expected.getTime(), DateUtils.addMonths(base, -3));

        expected.setTime(base);
        expected.add(Calendar.WEEK_OF_YEAR, 4);
        assertEquals(expected.getTime(), DateUtils.addWeeks(base, 4));

        expected.setTime(base);
        expected.add(Calendar.DAY_OF_MONTH, -5);
        assertEquals(expected.getTime(), DateUtils.addDays(base, -5));

        expected.setTime(base);
        expected.add(Calendar.HOUR_OF_DAY, 6);
        assertEquals(expected.getTime(), DateUtils.addHours(base, 6));

        expected.setTime(base);
        expected.add(Calendar.MINUTE, -7);
        assertEquals(expected.getTime(), DateUtils.addMinutes(base, -7));

        expected.setTime(base);
        expected.add(Calendar.SECOND, 8);
        assertEquals(expected.getTime(), DateUtils.addSeconds(base, 8));

        expected.setTime(base);
        expected.add(Calendar.MILLISECOND, -9);
        assertEquals(expected.getTime(), DateUtils.addMilliseconds(base, -9));
    }

    @Test(timeout = 4000)
    public void testRoundAndTruncate_ObjectSignature() {
        Date d = new Date(1000000000000L);
        Calendar c = Calendar.getInstance();
        c.setTime(d);

        Date roundedD = DateUtils.round((Object) d, Calendar.DATE);
        Date roundedC = DateUtils.round((Object) c, Calendar.DATE);
        assertEquals(roundedD, roundedC);

        Date truncD = DateUtils.truncate((Object) d, Calendar.DATE);
        Date truncC = DateUtils.truncate((Object) c, Calendar.DATE);
        assertEquals(truncD, truncC);
    }

    @Test(timeout = 4000)
    public void testRoundAndTruncate_CalendarSignature() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.MARCH, 15, 14, 45, 35);
        cal.set(Calendar.MILLISECOND, 600);

        Calendar rounded = DateUtils.round(cal, Calendar.HOUR);
        assertEquals(15, rounded.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, rounded.get(Calendar.MINUTE));

        Calendar truncated = DateUtils.truncate(cal, Calendar.HOUR);
        assertEquals(14, truncated.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, truncated.get(Calendar.MINUTE));
    }

    @Test(timeout = 4000)
    public void testRoundAndTruncate_SemiMonth() {
        Calendar cal = Calendar.getInstance();
        // Day 1: truncate -> 1, round -> 1
        cal.set(2023, Calendar.FEBRUARY, 1, 0, 0, 0);
        assertEquals(1, DateUtils.truncate(cal, DateUtils.SEMI_MONTH).get(Calendar.DATE));
        assertEquals(1, DateUtils.round(cal, DateUtils.SEMI_MONTH).get(Calendar.DATE));

        // Day 8: offset is 7 (not > 7), round down to 1
        cal.set(2023, Calendar.FEBRUARY, 8, 0, 0, 0);
        assertEquals(1, DateUtils.round(cal, DateUtils.SEMI_MONTH).get(Calendar.DATE));

        // Day 9: offset is 8 (> 7), round up to 16
        cal.set(2023, Calendar.FEBRUARY, 9, 0, 0, 0);
        assertEquals(16, DateUtils.round(cal, DateUtils.SEMI_MONTH).get(Calendar.DATE));

        // Day 15: truncate -> 1
        cal.set(2023, Calendar.FEBRUARY, 15, 0, 0, 0);
        assertEquals(1, DateUtils.truncate(cal, DateUtils.SEMI_MONTH).get(Calendar.DATE));

        // Day 16: truncate -> 16
        cal.set(2023, Calendar.FEBRUARY, 16, 0, 0, 0);
        assertEquals(16, DateUtils.truncate(cal, DateUtils.SEMI_MONTH).get(Calendar.DATE));

        // Day 23: offset is 22 - 15 = 7 (not > 7), round down to 16
        cal.set(2023, Calendar.FEBRUARY, 23, 0, 0, 0);
        assertEquals(16, DateUtils.round(cal, DateUtils.SEMI_MONTH).get(Calendar.DATE));

        // Day 24: offset is 23 - 15 = 8 (> 7), round up to 1st of next month
        cal.set(2023, Calendar.FEBRUARY, 24, 0, 0, 0);
        Calendar result = DateUtils.round(cal, DateUtils.SEMI_MONTH);
        assertEquals(1, result.get(Calendar.DATE));
        assertEquals(Calendar.MARCH, result.get(Calendar.MONTH));
    }

    @Test(timeout = 4000)
    public void testRoundAndTruncate_AmPm() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.JANUARY, 10, 6, 0, 0);
        // Truncate AM -> 0
        assertEquals(0, DateUtils.truncate(cal, Calendar.AM_PM).get(Calendar.HOUR_OF_DAY));
        // Round AM (6 <= 6) -> 0
        assertEquals(0, DateUtils.round(cal, Calendar.AM_PM).get(Calendar.HOUR_OF_DAY));

        // Round AM (7 > 6) -> 12
        cal.set(Calendar.HOUR_OF_DAY, 7);
        assertEquals(12, DateUtils.round(cal, Calendar.AM_PM).get(Calendar.HOUR_OF_DAY));

        // Truncate PM -> 12
        cal.set(Calendar.HOUR_OF_DAY, 18);
        assertEquals(12, DateUtils.truncate(cal, Calendar.AM_PM).get(Calendar.HOUR_OF_DAY));
        // Round PM (18 - 12 = 6 <= 6) -> 12
        assertEquals(12, DateUtils.round(cal, Calendar.AM_PM).get(Calendar.HOUR_OF_DAY));

        // Round PM (19 - 12 = 7 > 6) -> 0 next day
        cal.set(Calendar.HOUR_OF_DAY, 19);
        Calendar res = DateUtils.round(cal, Calendar.AM_PM);
        assertEquals(0, res.get(Calendar.HOUR_OF_DAY));
        assertEquals(11, res.get(Calendar.DATE));
    }

    @Test(timeout = 4000)
    public void testRoundAndTruncate_MillisecondFastPath() {
        Date d = new Date(123456789L);
        assertEquals(d, DateUtils.round(d, Calendar.MILLISECOND));
        assertEquals(d, DateUtils.truncate(d, Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testIterator_AllStyles() {
        Calendar focus = Calendar.getInstance();
        focus.set(2023, Calendar.JULY, 15); // Saturday

        int[] styles = {
            DateUtils.RANGE_MONTH_SUNDAY,
            DateUtils.RANGE_MONTH_MONDAY,
            DateUtils.RANGE_WEEK_SUNDAY,
            DateUtils.RANGE_WEEK_MONDAY,
            DateUtils.RANGE_WEEK_RELATIVE,
            DateUtils.RANGE_WEEK_CENTER
        };

        for (int style : styles) {
            Iterator it = DateUtils.iterator(focus, style);
            assertNotNull(it);
            assertTrue(it.hasNext());
            int count = 0;
            while (it.hasNext()) {
                Object obj = it.next();
                assertTrue(obj instanceof Calendar);
                count++;
            }
            assertTrue("Expected iterator to yield days, got " + count, count >= 7);
        }
    }

    @Test(timeout = 4000)
    public void testIterator_BoundaryAdjustments() {
        // Focus on Sunday (startCutoff < Calendar.SUNDAY in RANGE_WEEK_CENTER)
        Calendar sundayFocus = Calendar.getInstance();
        sundayFocus.set(2023, Calendar.JULY, 16); // Sunday
        assertEquals(Calendar.SUNDAY, sundayFocus.get(Calendar.DAY_OF_WEEK));
        Iterator itCenter = DateUtils.iterator(sundayFocus, DateUtils.RANGE_WEEK_CENTER);
        assertNotNull(itCenter);
        assertTrue(itCenter.hasNext());

        // Focus on Saturday (endCutoff > Calendar.SATURDAY in RANGE_WEEK_CENTER)
        Calendar satFocus = Calendar.getInstance();
        satFocus.set(2023, Calendar.JULY, 22); // Saturday
        assertEquals(Calendar.SATURDAY, satFocus.get(Calendar.DAY_OF_WEEK));
        Iterator itCenterSat = DateUtils.iterator(satFocus, DateUtils.RANGE_WEEK_CENTER);
        assertNotNull(itCenterSat);

        // Focus on Sunday with RANGE_WEEK_RELATIVE (endCutoff < Calendar.SUNDAY)
        Iterator itRelSun = DateUtils.iterator(sundayFocus, DateUtils.RANGE_WEEK_RELATIVE);
        assertNotNull(itRelSun);

        // Object iterator methods
        Iterator itFromDate = DateUtils.iterator((Object) sundayFocus.getTime(), DateUtils.RANGE_WEEK_SUNDAY);
        assertNotNull(itFromDate);
        Iterator itFromCal = DateUtils.iterator((Object) sundayFocus, DateUtils.RANGE_WEEK_SUNDAY);
        assertNotNull(itFromCal);
    }

    @Test(timeout = 4000)
    public void testDateIterator_ExhaustionAndContract() {
        Calendar start = Calendar.getInstance();
        start.set(2023, Calendar.JANUARY, 1);
        Calendar end = Calendar.getInstance();
        end.set(2023, Calendar.JANUARY, 2);

        Iterator it = DateUtils.iterator(start, DateUtils.RANGE_WEEK_SUNDAY);
        while (it.hasNext()) {
            it.next();
        }

        try {
            it.next();
            fail("Expected NoSuchElementException when iterator exhausted");
        } catch (NoSuchElementException expected) {
            // expected
        }

        try {
            it.remove();
            fail("Expected UnsupportedOperationException on remove()");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseDate_IncompleteMatchThrowsParseException() {
        try {
            DateUtils.parseDate("2023-01-01 extra", new String[] {"yyyy-MM-dd"});
            fail("Expected ParseException when input has unparsed trailing characters");
        } catch (ParseException expected) {
            assertTrue(expected.getMessage().contains("Unable to parse the date"));
        }
    }

    @Test(timeout = 4000)
    public void testParseDate_NoMatchThrowsParseException() {
        try {
            DateUtils.parseDate("invalid-date", new String[] {"yyyy-MM-dd", "yyyy/MM/dd"});
            fail("Expected ParseException when no patterns match");
        } catch (ParseException expected) {
            assertEquals(-1, expected.getErrorOffset());
        }
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testModify_YearTooLarge() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 280000001);
        DateUtils.round(cal, Calendar.MONTH);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameDay_NullDate1() {
        DateUtils.isSameDay((Date) null, new Date());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameDay_NullDate2() {
        DateUtils.isSameDay(new Date(), (Date) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameDay_NullCal1() {
        DateUtils.isSameDay((Calendar) null, Calendar.getInstance());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameDay_NullCal2() {
        DateUtils.isSameDay(Calendar.getInstance(), (Calendar) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameInstant_NullDate1() {
        DateUtils.isSameInstant((Date) null, new Date());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameInstant_NullDate2() {
        DateUtils.isSameInstant(new Date(), (Date) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameInstant_NullCal1() {
        DateUtils.isSameInstant((Calendar) null, Calendar.getInstance());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameInstant_NullCal2() {
        DateUtils.isSameInstant(Calendar.getInstance(), (Calendar) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameLocalTime_NullCal1() {
        DateUtils.isSameLocalTime(null, Calendar.getInstance());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameLocalTime_NullCal2() {
        DateUtils.isSameLocalTime(Calendar.getInstance(), null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseDate_NullString() throws ParseException {
        DateUtils.parseDate(null, new String[] {"yyyy-MM-dd"});
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseDate_NullPatterns() throws ParseException {
        DateUtils.parseDate("2023-01-01", null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAdd_NullDate() {
        DateUtils.add(null, Calendar.DAY_OF_MONTH, 1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRound_NullDate() {
        DateUtils.round((Date) null, Calendar.DATE);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRound_NullCalendar() {
        DateUtils.round((Calendar) null, Calendar.DATE);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRound_NullObject() {
        DateUtils.round((Object) null, Calendar.DATE);
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testRound_InvalidObject() {
        DateUtils.round("Not a date", Calendar.DATE);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTruncate_NullDate() {
        DateUtils.truncate((Date) null, Calendar.DATE);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTruncate_NullCalendar() {
        DateUtils.truncate((Calendar) null, Calendar.DATE);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTruncate_NullObject() {
        DateUtils.truncate((Object) null, Calendar.DATE);
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testTruncate_InvalidObject() {
        DateUtils.truncate("Not a date", Calendar.DATE);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testModify_UnsupportedField() {
        DateUtils.round(new Date(), 999999);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIterator_NullDate() {
        DateUtils.iterator((Date) null, DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIterator_NullCalendar() {
        DateUtils.iterator((Calendar) null, DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIterator_NullObject() {
        DateUtils.iterator((Object) null, DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testIterator_InvalidObject() {
        DateUtils.iterator("Not a date", DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIterator_InvalidRangeStyle() {
        DateUtils.iterator(Calendar.getInstance(), 99999);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAndConstants() {
        DateUtils utils = new DateUtils();
        assertNotNull(utils);

        assertEquals(TimeZone.getTimeZone("GMT"), DateUtils.UTC_TIME_ZONE);
        assertEquals(1000L, DateUtils.MILLIS_PER_SECOND);
        assertEquals(60000L, DateUtils.MILLIS_PER_MINUTE);
        assertEquals(3600000L, DateUtils.MILLIS_PER_HOUR);
        assertEquals(86400000L, DateUtils.MILLIS_PER_DAY);

        @SuppressWarnings("deprecation")
        int mis = DateUtils.MILLIS_IN_SECOND;
        @SuppressWarnings("deprecation")
        int mim = DateUtils.MILLIS_IN_MINUTE;
        @SuppressWarnings("deprecation")
        int mih = DateUtils.MILLIS_IN_HOUR;
        @SuppressWarnings("deprecation")
        int mid = DateUtils.MILLIS_IN_DAY;

        assertEquals(1000, mis);
        assertEquals(60000, mim);
        assertEquals(3600000, mih);
        assertEquals(86400000, mid);
    }
}