package org.apache.commons.lang.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

/**
 * White-box test suite for DateUtils targeting the known LANG-59 defect
 * (truncate to SECOND fails on DST boundary) and maximizing line/branch coverage.
 *
 * [Branch & Defect Analysis Matrix]
 * - isSameDay(Date): null guard, same day true/false, different day false.
 * - isSameDay(Calendar): null guard, ERA/YEAR/DAY_OF_YEAR equality.
 * - isSameInstant(Date): null guard, millisecond equality.
 * - isSameInstant(Calendar): null guard, getTime().getTime() equality.
 * - isSameLocalTime(Calendar): null guard, all fields + class equality.
 * - parseDate: null guard, successful parse, parse failure (ParseException).
 * - add* methods: null guard, positive/negative amounts, boundary (Integer.MAX_VALUE).
 * - add: null guard, calendarField validity (though not checked), amount.
 * - round(Date/Calendar/Object): null guard, field constants (including SEMI_MONTH, AM_PM),
 *   round up/down logic, large year exception.
 * - truncate(Date/Calendar/Object): null guard, field constants, large year exception,
 *   DST boundary (LANG-59 defect).
 * - modify: internal method tested via round/truncate; covers all field groups,
 *   SEMI_MONTH special case, AM_PM special case, offset calculation, roundUp logic.
 * - iterator: null guard, all range styles, invalid range style exception,
 *   DateIterator hasNext/next/remove.
 * - DateIterator: constructor, hasNext, next, NoSuchElementException, remove throws UnsupportedOperationException.
 *
 * Defect-specific test: testTruncateLang59() reproduces the exact failure.
 */
public class DateUtilsDeepseekTest {

    // ------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testIsSameDaySameDate() {
        Date d1 = new Date(1000000L);
        Date d2 = new Date(1000000L);
        assertTrue(DateUtils.isSameDay(d1, d2));
    }

    @Test(timeout = 4000)
    public void testIsSameDayDifferentDay() {
        Date d1 = new Date(1000000L);
        Date d2 = new Date(2000000L);
        assertFalse(DateUtils.isSameDay(d1, d2));
    }

    @Test(timeout = 4000)
    public void testIsSameDayCalendar() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2004, Calendar.OCTOBER, 31, 1, 2, 3);
        Calendar cal2 = (Calendar) cal1.clone();
        assertTrue(DateUtils.isSameDay(cal1, cal2));
        cal2.add(Calendar.DAY_OF_YEAR, 1);
        assertFalse(DateUtils.isSameDay(cal1, cal2));
    }

    @Test(timeout = 4000)
    public void testIsSameInstantDate() {
        Date d1 = new Date(123456789L);
        Date d2 = new Date(123456789L);
        assertTrue(DateUtils.isSameInstant(d1, d2));
        d2 = new Date(987654321L);
        assertFalse(DateUtils.isSameInstant(d1, d2));
    }

    @Test(timeout = 4000)
    public void testIsSameInstantCalendar() {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(123456789L);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(123456789L);
        assertTrue(DateUtils.isSameInstant(cal1, cal2));
        cal2.setTimeInMillis(987654321L);
        assertFalse(DateUtils.isSameInstant(cal1, cal2));
    }

    @Test(timeout = 4000)
    public void testIsSameLocalTime() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2004, Calendar.OCTOBER, 31, 1, 2, 3);
        cal1.set(Calendar.MILLISECOND, 456);
        Calendar cal2 = (Calendar) cal1.clone();
        assertTrue(DateUtils.isSameLocalTime(cal1, cal2));
        cal2.set(Calendar.MILLISECOND, 789);
        assertFalse(DateUtils.isSameLocalTime(cal1, cal2));
        // Different class
        Calendar cal3 = new java.util.GregorianCalendar();
        cal3.setTime(cal1.getTime());
        assertFalse(DateUtils.isSameLocalTime(cal1, cal3));
    }

    @Test(timeout = 4000)
    public void testParseDateSuccess() throws Exception {
        String[] patterns = {"yyyy-MM-dd", "yyyy/MM/dd"};
        Date d = DateUtils.parseDate("2004-10-31", patterns);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        assertEquals(2004, cal.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, cal.get(Calendar.MONTH));
        assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test(timeout = 4000)
    public void testParseDateMultiplePatterns() throws Exception {
        String[] patterns = {"yyyy/MM/dd", "yyyy-MM-dd"};
        Date d = DateUtils.parseDate("2004/10/31", patterns);
        assertNotNull(d);
    }

    @Test(timeout = 4000, expected = ParseException.class)
    public void testParseDateFailure() throws Exception {
        String[] patterns = {"yyyy-MM-dd"};
        DateUtils.parseDate("invalid", patterns);
    }

    @Test(timeout = 4000)
    public void testAddYears() {
        Date base = new Date(0L);
        Date result = DateUtils.addYears(base, 5);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(1975, cal.get(Calendar.YEAR)); // 1970 + 5
    }

    @Test(timeout = 4000)
    public void testAddMonths() {
        Date base = new Date(0L);
        Date result = DateUtils.addMonths(base, 2);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH)); // Jan + 2 = Mar
    }

    @Test(timeout = 4000)
    public void testAddWeeks() {
        Date base = new Date(0L);
        Date result = DateUtils.addWeeks(base, 1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(7, cal.get(Calendar.DAY_OF_YEAR)); // Jan 1 + 7 days = Jan 8
    }

    @Test(timeout = 4000)
    public void testAddDays() {
        Date base = new Date(0L);
        Date result = DateUtils.addDays(base, 10);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(11, cal.get(Calendar.DAY_OF_YEAR));
    }

    @Test(timeout = 4000)
    public void testAddHours() {
        Date base = new Date(0L);
        Date result = DateUtils.addHours(base, 5);
        assertEquals(5 * 3600000L, result.getTime());
    }

    @Test(timeout = 4000)
    public void testAddMinutes() {
        Date base = new Date(0L);
        Date result = DateUtils.addMinutes(base, 30);
        assertEquals(30 * 60000L, result.getTime());
    }

    @Test(timeout = 4000)
    public void testAddSeconds() {
        Date base = new Date(0L);
        Date result = DateUtils.addSeconds(base, 45);
        assertEquals(45 * 1000L, result.getTime());
    }

    @Test(timeout = 4000)
    public void testAddMilliseconds() {
        Date base = new Date(0L);
        Date result = DateUtils.addMilliseconds(base, 123);
        assertEquals(123L, result.getTime());
    }

    @Test(timeout = 4000)
    public void testAddNegativeAmount() {
        Date base = new Date(1000000L);
        Date result = DateUtils.addDays(base, -1);
        assertTrue(result.getTime() < base.getTime());
    }

    // ------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // ------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameDayNullDate1() {
        DateUtils.isSameDay(null, new Date());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameDayNullDate2() {
        DateUtils.isSameDay(new Date(), null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameDayNullCalendar1() {
        DateUtils.isSameDay((Calendar) null, Calendar.getInstance());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameDayNullCalendar2() {
        DateUtils.isSameDay(Calendar.getInstance(), null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameInstantNullDate1() {
        DateUtils.isSameInstant(null, new Date());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameInstantNullCalendar1() {
        DateUtils.isSameInstant((Calendar) null, Calendar.getInstance());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameLocalTimeNull() {
        DateUtils.isSameLocalTime(null, Calendar.getInstance());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseDateNullString() throws Exception {
        DateUtils.parseDate(null, new String[]{"yyyy"});
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseDateNullPatterns() throws Exception {
        DateUtils.parseDate("2004", null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddNullDate() {
        DateUtils.add(null, Calendar.YEAR, 1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRoundNullDate() {
        DateUtils.round((Date) null, Calendar.YEAR);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRoundNullCalendar() {
        DateUtils.round((Calendar) null, Calendar.YEAR);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRoundNullObject() {
        DateUtils.round((Object) null, Calendar.YEAR);
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testRoundInvalidObject() {
        DateUtils.round("string", Calendar.YEAR);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTruncateNullDate() {
        DateUtils.truncate((Date) null, Calendar.YEAR);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTruncateNullCalendar() {
        DateUtils.truncate((Calendar) null, Calendar.YEAR);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTruncateNullObject() {
        DateUtils.truncate((Object) null, Calendar.YEAR);
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testTruncateInvalidObject() {
        DateUtils.truncate(42, Calendar.YEAR);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIteratorNullDate() {
        DateUtils.iterator((Date) null, DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIteratorNullCalendar() {
        DateUtils.iterator((Calendar) null, DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIteratorNullObject() {
        DateUtils.iterator((Object) null, DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testIteratorInvalidObject() {
        DateUtils.iterator(3.14, DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIteratorInvalidRangeStyle() {
        DateUtils.iterator(new Date(), 999);
    }

    @Test(timeout = 4000)
    public void testLargeYearArithmeticException() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 280000001);
        try {
            DateUtils.truncate(cal, Calendar.YEAR);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    // ------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (LANG-59)
    // ------------------------------------------------------------------

    /**
     * Reproduces the exact failure from Defects4J:
     * truncate to Calendar.SECOND on a DST boundary date
     * (Sun Oct 31 01:02:03 MDT 2004) should preserve timezone offset,
     * but bug causes MST.
     */
    @Test(timeout = 4000)
    public void testTruncateLang59() {
        // Use a timezone that observes DST (America/Denver)
        TimeZone tz = TimeZone.getTimeZone("America/Denver");
        Calendar cal = Calendar.getInstance(tz);
        // Set to Oct 31, 2004 01:02:03.123 MDT (MDT = GMT-6)
        cal.set(2004, Calendar.OCTOBER, 31, 1, 2, 3);
        cal.set(Calendar.MILLISECOND, 123);
        // Ensure we are in DST (MDT)
        assertTrue("Expected MDT (DST)", tz.inDaylightTime(cal.getTime()));

        // Truncate to SECOND
        Calendar truncated = DateUtils.truncate(cal, Calendar.SECOND);
        Date truncatedDate = truncated.getTime();

        // Build expected: same instant but with milliseconds zeroed
        Calendar expectedCal = Calendar.getInstance(tz);
        expectedCal.set(2004, Calendar.OCTOBER, 31, 1, 2, 3);
        expectedCal.set(Calendar.MILLISECOND, 0);
        Date expectedDate = expectedCal.getTime();

        // The bug causes the timezone offset to change from MDT to MST.
        // We assert that the truncated date equals the expected date.
        assertEquals("Truncate Calendar.SECOND failed on DST boundary",
                     expectedDate, truncatedDate);
    }

    // Additional DST boundary tests for other fields
    @Test(timeout = 4000)
    public void testTruncateMinuteDST() {
        TimeZone tz = TimeZone.getTimeZone("America/Denver");
        Calendar cal = Calendar.getInstance(tz);
        cal.set(2004, Calendar.OCTOBER, 31, 1, 2, 3);
        cal.set(Calendar.MILLISECOND, 456);
        Calendar truncated = DateUtils.truncate(cal, Calendar.MINUTE);
        Calendar expected = Calendar.getInstance(tz);
        expected.set(2004, Calendar.OCTOBER, 31, 1, 2, 0);
        expected.set(Calendar.MILLISECOND, 0);
        assertEquals(expected.getTime(), truncated.getTime());
    }

    @Test(timeout = 4000)
    public void testRoundSecondDST() {
        TimeZone tz = TimeZone.getTimeZone("America/Denver");
        Calendar cal = Calendar.getInstance(tz);
        cal.set(2004, Calendar.OCTOBER, 31, 1, 2, 3);
        cal.set(Calendar.MILLISECOND, 500);
        Calendar rounded = DateUtils.round(cal, Calendar.SECOND);
        Calendar expected = Calendar.getInstance(tz);
        expected.set(2004, Calendar.OCTOBER, 31, 1, 2, 4);
        expected.set(Calendar.MILLISECOND, 0);
        assertEquals(expected.getTime(), rounded.getTime());
    }

    // ------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // ------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testModifyUnsupportedField() {
        // This will throw because the field is not in the fields array
        DateUtils.truncate(new Date(), Calendar.WEEK_OF_MONTH);
    }

    @Test(timeout = 4000)
    public void testRoundSemiMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.MARCH, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Calendar rounded = DateUtils.round(cal, DateUtils.SEMI_MONTH);
        // March 1 -> round up to March 16 (since day 1 is in bottom half)
        assertEquals(16, rounded.get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.MARCH, rounded.get(Calendar.MONTH));
    }

    @Test(timeout = 4000)
    public void testRoundSemiMonthDay16() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.MARCH, 16, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Calendar rounded = DateUtils.round(cal, DateUtils.SEMI_MONTH);
        // March 16 -> round up to April 1 (since offset = 15, roundUp = offset > 7 -> true)
        assertEquals(1, rounded.get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.APRIL, rounded.get(Calendar.MONTH));
    }

    @Test(timeout = 4000)
    public void testRoundAmPm() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.MARCH, 1, 6, 0, 0); // 6 AM
        Calendar rounded = DateUtils.round(cal, Calendar.AM_PM);
        // 6 AM -> round up to PM? Actually offset = 6, roundUp = offset > 6 -> false, so stays AM
        assertEquals(Calendar.AM, rounded.get(Calendar.AM_PM));
        // Hour should be 0 (midnight) after truncation? Actually rounding to AM_PM truncates to date and sets hour to 0.
        assertEquals(0, rounded.get(Calendar.HOUR_OF_DAY));
    }

    @Test(timeout = 4000)
    public void testRoundAmPmBoundary() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.MARCH, 1, 12, 0, 0); // 12 PM
        Calendar rounded = DateUtils.round(cal, Calendar.AM_PM);
        // offset = 12, after subtracting 12 -> 0, roundUp = 0 > 6? false, so stays PM? Actually rounding to AM_PM truncates to date.
        // The expected behavior: truncate to date, so hour becomes 0.
        assertEquals(0, rounded.get(Calendar.HOUR_OF_DAY));
    }

    @Test(timeout = 4000)
    public void testTruncateSemiMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.MARCH, 10, 0, 0, 0);
        Calendar truncated = DateUtils.truncate(cal, DateUtils.SEMI_MONTH);
        // Truncate to SEMI_MONTH: should set date to 1 (bottom half) or 16 (top half)?
        // Since offset = 9, offset >= 15? no, so stays in bottom half, set date to 1.
        assertEquals(1, truncated.get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.MARCH, truncated.get(Calendar.MONTH));
    }

    // ------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity (Iterator)
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testIteratorWeekSunday() {
        Calendar focus = Calendar.getInstance();
        focus.set(2004, Calendar.OCTOBER, 31, 0, 0, 0); // Sunday
        Iterator it = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_SUNDAY);
        assertTrue(it.hasNext());
        Calendar first = (Calendar) it.next();
        assertEquals(Calendar.SUNDAY, first.get(Calendar.DAY_OF_WEEK));
        // Should iterate through the week
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(6, count); // 7 days total, first already consumed
    }

    @Test(timeout = 4000)
    public void testIteratorWeekMonday() {
        Calendar focus = Calendar.getInstance();
        focus.set(2004, Calendar.NOVEMBER, 1, 0, 0, 0); // Monday
        Iterator it = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_MONDAY);
        Calendar first = (Calendar) it.next();
        assertEquals(Calendar.MONDAY, first.get(Calendar.DAY_OF_WEEK));
    }

    @Test(timeout = 4000)
    public void testIteratorWeekRelative() {
        Calendar focus = Calendar.getInstance();
        focus.set(2004, Calendar.OCTOBER, 31, 0, 0, 0); // Sunday
        Iterator it = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_RELATIVE);
        Calendar first = (Calendar) it.next();
        assertEquals(Calendar.SUNDAY, first.get(Calendar.DAY_OF_WEEK));
    }

    @Test(timeout = 4000)
    public void testIteratorWeekCenter() {
        Calendar focus = Calendar.getInstance();
        focus.set(2004, Calendar.OCTOBER, 31, 0, 0, 0); // Sunday
        Iterator it = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_CENTER);
        Calendar first = (Calendar) it.next();
        // startCutoff = Sunday - 3 = Thursday
        assertEquals(Calendar.THURSDAY, first.get(Calendar.DAY_OF_WEEK));
    }

    @Test(timeout = 4000)
    public void testIteratorMonthSunday() {
        Calendar focus = Calendar.getInstance();
        focus.set(2004, Calendar.OCTOBER, 31, 0, 0, 0);
        Iterator it = DateUtils.iterator(focus, DateUtils.RANGE_MONTH_SUNDAY);
        Calendar first = (Calendar) it.next();
        // Start of month truncated to MONTH, then back to previous Sunday
        // October 1, 2004 is a Friday, so previous Sunday is Sep 26
        assertEquals(Calendar.SEPTEMBER, first.get(Calendar.MONTH));
        assertEquals(26, first.get(Calendar.DAY_OF_MONTH));
    }

    @Test(timeout = 4000)
    public void testIteratorMonthMonday() {
        Calendar focus = Calendar.getInstance();
        focus.set(2004, Calendar.OCTOBER, 31, 0, 0, 0);
        Iterator it = DateUtils.iterator(focus, DateUtils.RANGE_MONTH_MONDAY);
        Calendar first = (Calendar) it.next();
        // Start of month truncated to MONTH, then back to previous Monday
        // October 1, 2004 is Friday, previous Monday is Sep 27
        assertEquals(Calendar.SEPTEMBER, first.get(Calendar.MONTH));
        assertEquals(27, first.get(Calendar.DAY_OF_MONTH));
    }

    @Test(timeout = 4000)
    public void testDateIteratorHasNextAndNext() {
        Calendar start = Calendar.getInstance();
        start.set(2004, Calendar.OCTOBER, 31);
        Calendar end = Calendar.getInstance();
        end.set(2004, Calendar.NOVEMBER, 2);
        Iterator it = new DateUtils.DateIterator(start, end);
        assertTrue(it.hasNext());
        Calendar first = (Calendar) it.next();
        assertEquals(31, first.get(Calendar.DAY_OF_MONTH));
        assertTrue(it.hasNext());
        Calendar second = (Calendar) it.next();
        assertEquals(1, second.get(Calendar.DAY_OF_MONTH));
        assertTrue(it.hasNext());
        Calendar third = (Calendar) it.next();
        assertEquals(2, third.get(Calendar.DAY_OF_MONTH));
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000, expected = java.util.NoSuchElementException.class)
    public void testDateIteratorNextBeyondEnd() {
        Calendar start = Calendar.getInstance();
        start.set(2004, Calendar.OCTOBER, 31);
        Calendar end = Calendar.getInstance();
        end.set(2004, Calendar.OCTOBER, 31);
        Iterator it = new DateUtils.DateIterator(start, end);
        it.next(); // only one element
        it.next(); // should throw
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testDateIteratorRemove() {
        Calendar start = Calendar.getInstance();
        start.set(2004, Calendar.OCTOBER, 31);
        Calendar end = Calendar.getInstance();
        end.set(2004, Calendar.NOVEMBER, 1);
        Iterator it = new DateUtils.DateIterator(start, end);
        it.remove();
    }

    // ------------------------------------------------------------------
    // Additional coverage for round/truncate with various fields
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testRoundMillisecond() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 31, 1, 2, 3);
        cal.set(Calendar.MILLISECOND, 456);
        Calendar rounded = DateUtils.round(cal, Calendar.MILLISECOND);
        // Round to millisecond: no change
        assertEquals(456, rounded.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testTruncateMillisecond() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 31, 1, 2, 3);
        cal.set(Calendar.MILLISECOND, 456);
        Calendar truncated = DateUtils.truncate(cal, Calendar.MILLISECOND);
        assertEquals(456, truncated.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testRoundSecond() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 31, 1, 2, 3);
        cal.set(Calendar.MILLISECOND, 500);
        Calendar rounded = DateUtils.round(cal, Calendar.SECOND);
        assertEquals(4, rounded.get(Calendar.SECOND));
        assertEquals(0, rounded.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testTruncateSecond() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 31, 1, 2, 3);
        cal.set(Calendar.MILLISECOND, 456);
        Calendar truncated = DateUtils.truncate(cal, Calendar.SECOND);
        assertEquals(3, truncated.get(Calendar.SECOND));
        assertEquals(0, truncated.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testRoundMinute() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 31, 1, 2, 30);
        cal.set(Calendar.MILLISECOND, 0);
        Calendar rounded = DateUtils.round(cal, Calendar.MINUTE);
        assertEquals(3, rounded.get(Calendar.MINUTE));
        assertEquals(0, rounded.get(Calendar.SECOND));
    }

    @Test(timeout = 4000)
    public void testTruncateMinute() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 31, 1, 2, 45);
        cal.set(Calendar.MILLISECOND, 123);
        Calendar truncated = DateUtils.truncate(cal, Calendar.MINUTE);
        assertEquals(2, truncated.get(Calendar.MINUTE));
        assertEquals(0, truncated.get(Calendar.SECOND));
        assertEquals(0, truncated.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testRoundHour() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 31, 1, 30, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Calendar rounded = DateUtils.round(cal, Calendar.HOUR_OF_DAY);
        assertEquals(2, rounded.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, rounded.get(Calendar.MINUTE));
    }

    @Test(timeout = 4000)
    public void testTruncateHour() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 31, 1, 45, 30);
        cal.set(Calendar.MILLISECOND, 123);
        Calendar truncated = DateUtils.truncate(cal, Calendar.HOUR_OF_DAY);
        assertEquals(1, truncated.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, truncated.get(Calendar.MINUTE));
        assertEquals(0, truncated.get(Calendar.SECOND));
        assertEquals(0, truncated.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testRoundDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 31, 12, 0, 0);
        Calendar rounded = DateUtils.round(cal, Calendar.DATE);
        // Round to day: if time >= 12:00, round up to next day
        assertEquals(1, rounded.get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.NOVEMBER, rounded.get(Calendar.MONTH));
    }

    @Test(timeout = 4000)
    public void testTruncateDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 31, 12, 30, 45);
        Calendar truncated = DateUtils.truncate(cal, Calendar.DATE);
        assertEquals(31, truncated.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, truncated.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, truncated.get(Calendar.MINUTE));
        assertEquals(0, truncated.get(Calendar.SECOND));
        assertEquals(0, truncated.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testRoundMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 16, 0, 0, 0);
        Calendar rounded = DateUtils.round(cal, Calendar.MONTH);
        // October 16 -> round up to November 1
        assertEquals(Calendar.NOVEMBER, rounded.get(Calendar.MONTH));
        assertEquals(1, rounded.get(Calendar.DAY_OF_MONTH));
    }

    @Test(timeout = 4000)
    public void testTruncateMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 31, 12, 0, 0);
        Calendar truncated = DateUtils.truncate(cal, Calendar.MONTH);
        assertEquals(Calendar.OCTOBER, truncated.get(Calendar.MONTH));
        assertEquals(1, truncated.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, truncated.get(Calendar.HOUR_OF_DAY));
    }

    @Test(timeout = 4000)
    public void testRoundYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.JULY, 1, 0, 0, 0);
        Calendar rounded = DateUtils.round(cal, Calendar.YEAR);
        // July 1 -> round up to next year? Actually half year is July 2? Let's compute:
        // min = 1, max = 12, offset = 6 (July is month 6), roundUp = offset > (12-1)/2 = 5.5 -> true
        // So round up to next year
        assertEquals(2005, rounded.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, rounded.get(Calendar.MONTH));
        assertEquals(1, rounded.get(Calendar.DAY_OF_MONTH));
    }

    @Test(timeout = 4000)
    public void testTruncateYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 31, 12, 0, 0);
        Calendar truncated = DateUtils.truncate(cal, Calendar.YEAR);
        assertEquals(2004, truncated.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, truncated.get(Calendar.MONTH));
        assertEquals(1, truncated.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, truncated.get(Calendar.HOUR_OF_DAY));
    }

    @Test(timeout = 4000)
    public void testRoundEra() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 31, 0, 0, 0);
        Calendar rounded = DateUtils.round(cal, Calendar.ERA);
        // Round to ERA: should truncate to beginning of era (year 1)
        assertEquals(1, rounded.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, rounded.get(Calendar.MONTH));
        assertEquals(1, rounded.get(Calendar.DAY_OF_MONTH));
    }

    @Test(timeout = 4000)
    public void testTruncateEra() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 31, 12, 0, 0);
        Calendar truncated = DateUtils.truncate(cal, Calendar.ERA);
        assertEquals(1, truncated.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, truncated.get(Calendar.MONTH));
        assertEquals(1, truncated.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, truncated.get(Calendar.HOUR_OF_DAY));
    }

    // ------------------------------------------------------------------
    // Object overloads for round/truncate/iterator
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testRoundObjectDate() {
        Date d = new Date();
        Date rounded = DateUtils.round((Object) d, Calendar.YEAR);
        assertNotNull(rounded);
    }

    @Test(timeout = 4000)
    public void testRoundObjectCalendar() {
        Calendar cal = Calendar.getInstance();
        Date rounded = DateUtils.round((Object) cal, Calendar.YEAR);
        assertNotNull(rounded);
    }

    @Test(timeout = 4000)
    public void testTruncateObjectDate() {
        Date d = new Date();
        Date truncated = DateUtils.truncate((Object) d, Calendar.YEAR);
        assertNotNull(truncated);
    }

    @Test(timeout = 4000)
    public void testTruncateObjectCalendar() {
        Calendar cal = Calendar.getInstance();
        Date truncated = DateUtils.truncate((Object) cal, Calendar.YEAR);
        assertNotNull(truncated);
    }

    @Test(timeout = 4000)
    public void testIteratorObjectDate() {
        Date d = new Date();
        Iterator it = DateUtils.iterator((Object) d, DateUtils.RANGE_WEEK_SUNDAY);
        assertTrue(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testIteratorObjectCalendar() {
        Calendar cal = Calendar.getInstance();
        Iterator it = DateUtils.iterator((Object) cal, DateUtils.RANGE_WEEK_SUNDAY);
        assertTrue(it.hasNext());
    }
}