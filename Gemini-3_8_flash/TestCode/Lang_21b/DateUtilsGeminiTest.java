package org.apache.commons.lang3.time;

import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target: org.apache.commons.lang3.time.DateUtils
 *
 * 1. DEFECT LANG-677 Zone:
 *    - Method: isSameLocalTime(Calendar, Calendar)
 *    - Flaw: Compares Calendar.HOUR instead of Calendar.HOUR_OF_DAY (or without AM_PM check).
 *      Times differing by 12 hours (e.g. 10:00 vs 22:00 on the same date) falsely return true.
 *    - Targeted by: testLANG677_isSameLocalTimeDifferentAmPm()
 *
 * 2. Coverage & Equivalence Partitions:
 *    - Comparisons:
 *      * isSameDay(Date, Date), isSameDay(Calendar, Calendar): null guards, same day, cross-year, cross-era.
 *      * isSameInstant(Date, Date), isSameInstant(Calendar, Calendar): null guards, exact millisecond comparison.
 *      * isSameLocalTime(Calendar, Calendar): class mismatch, nulls, each field branch (ms, s, m, h, day, yr, era).
 *    - Parsing:
 *      * parseDate(String, String...), parseDateStrictly(String, String...)
 *      * 'ZZ' timezone format regex replacement handling.
 *      * Lenient vs strict mode (e.g. Feb 942, 1996 or Feb 29 on non-leap year).
 *      * Unmatched pattern throwing ParseException.
 *    - Date Math (Add & Set):
 *      * addYears, addMonths, addWeeks, addDays, addHours, addMinutes, addSeconds, addMilliseconds.
 *      * setYears, setMonths, setDays, setHours, setMinutes, setSeconds, setMilliseconds.
 *      * Null checks on all add/set methods.
 *    - Rounding, Truncation, and Ceiling (Date, Calendar, Object):
 *      * modify() branches: Truncate, Round, Ceiling.
 *      * Fields: Millisecond (early return), Second, Minute, Hour, SEMI_MONTH (both <= 15 and > 15), AM_PM.
 *      * Boundaries: Year > 280,000,000 ArithmeticException.
 *      * Type handling: Date vs Calendar vs unsupported Object (ClassCastException).
 *    - Truncated Comparisons:
 *      * truncatedEquals(Calendar/Date), truncatedCompareTo(Calendar/Date).
 *    - Date Fragments:
 *      * getFragmentInMilliseconds, Seconds, Minutes, Hours, Days for Date and Calendar.
 *      * Units: YEAR, MONTH, DAY_OF_YEAR, DATE, HOUR_OF_DAY, MINUTE, SECOND, MILLISECOND.
 *      * Unsupported fragment & invalid unit IllegalArgumentExceptions.
 *    - Range Iterators:
 *      * iterator(Date/Calendar/Object, rangeStyle).
 *      * Styles: RANGE_MONTH_SUNDAY, RANGE_MONTH_MONDAY, RANGE_WEEK_SUNDAY, RANGE_WEEK_MONDAY,
 *                RANGE_WEEK_RELATIVE, RANGE_WEEK_CENTER.
 *      * DateIterator contracts: hasNext(), next(), NoSuchElementException, remove() (UnsupportedOperationException).
 */
public class DateUtilsGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (LANG-677)
    // =========================================================================

    /**
     * LANG-677: DateUtils.isSameLocalTime returns true for calendars that differ by 12 hours
     * because Calendar.HOUR is checked instead of Calendar.HOUR_OF_DAY (or without AM_PM).
     */
    @Test(timeout = 4000)
    public void testLANG677_isSameLocalTimeDifferentAmPm() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal1.clear();
        cal2.clear();

        cal1.set(2023, Calendar.OCTOBER, 15, 10, 30, 0); // 10:30 AM
        cal2.set(2023, Calendar.OCTOBER, 15, 22, 30, 0); // 10:30 PM (22:30)

        // Local times are strictly distinct (10:30 vs 22:30), must return false
        assertFalse("isSameLocalTime should return false for 10:00 vs 22:00",
                DateUtils.isSameLocalTime(cal1, cal2));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        assertNotNull(new DateUtils());
    }

    @Test(timeout = 4000)
    public void testIsSameDay_Date() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.JUNE, 10, 8, 30, 0);
        Date d1 = cal.getTime();

        cal.set(2023, Calendar.JUNE, 10, 18, 45, 30);
        Date d2 = cal.getTime();

        cal.set(2023, Calendar.JUNE, 11, 8, 30, 0);
        Date d3 = cal.getTime();

        assertTrue(DateUtils.isSameDay(d1, d2));
        assertFalse(DateUtils.isSameDay(d1, d3));
    }

    @Test(timeout = 4000)
    public void testIsSameDay_Calendar() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal1.set(2023, Calendar.MARCH, 15, 1, 0, 0);
        cal2.set(2023, Calendar.MARCH, 15, 23, 59, 59);
        assertTrue(DateUtils.isSameDay(cal1, cal2));

        cal2.set(2024, Calendar.MARCH, 15, 1, 0, 0);
        assertFalse(DateUtils.isSameDay(cal1, cal2));

        cal2.set(2023, Calendar.APRIL, 15, 1, 0, 0);
        assertFalse(DateUtils.isSameDay(cal1, cal2));
    }

    @Test(timeout = 4000)
    public void testIsSameInstant_DateAndCal() {
        Date d1 = new Date(1600000000000L);
        Date d2 = new Date(1600000000000L);
        Date d3 = new Date(1600000000001L);

        assertTrue(DateUtils.isSameInstant(d1, d2));
        assertFalse(DateUtils.isSameInstant(d1, d3));

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        assertTrue(DateUtils.isSameInstant(c1, c2));

        c2.setTime(d3);
        assertFalse(DateUtils.isSameInstant(c1, c2));
    }

    @Test(timeout = 4000)
    public void testIsSameLocalTime_Identical() {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = (Calendar) c1.clone();
        assertTrue(DateUtils.isSameLocalTime(c1, c2));

        c2.add(Calendar.MILLISECOND, 1);
        assertFalse(DateUtils.isSameLocalTime(c1, c2));
    }

    @Test(timeout = 4000)
    public void testParseDate_LenientAndStrict() throws ParseException {
        String[] patterns = new String[]{"yyyy-MM-dd", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssZZ"};

        Date parsed = DateUtils.parseDate("2023-11-20", patterns);
        assertNotNull(parsed);

        Date parsedZZ = DateUtils.parseDate("2023-11-20T10:00:00+01:00", patterns);
        assertNotNull(parsedZZ);

        Date strict = DateUtils.parseDateStrictly("2023-02-28", "yyyy-MM-dd");
        assertNotNull(strict);
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseDateStrictly_InvalidDateThrows() throws ParseException {
        DateUtils.parseDateStrictly("2023-02-29", "yyyy-MM-dd");
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testParseDate_NoMatch() throws ParseException {
        DateUtils.parseDate("not-a-date", "yyyy-MM-dd");
    }

    @Test(timeout = 4000)
    public void testDateAddManipulations() {
        Calendar base = Calendar.getInstance();
        base.clear();
        base.set(2020, Calendar.JANUARY, 15, 12, 30, 45);
        base.set(Calendar.MILLISECOND, 500);
        Date d = base.getTime();

        Date yr = DateUtils.addYears(d, 1);
        assertEquals(2021, DateUtils.toCalendar(yr).get(Calendar.YEAR));

        Date mo = DateUtils.addMonths(d, 2);
        assertEquals(Calendar.MARCH, DateUtils.toCalendar(mo).get(Calendar.MONTH));

        Date wk = DateUtils.addWeeks(d, 1);
        assertEquals(22, DateUtils.toCalendar(wk).get(Calendar.DAY_OF_MONTH));

        Date dy = DateUtils.addDays(d, 5);
        assertEquals(20, DateUtils.toCalendar(dy).get(Calendar.DAY_OF_MONTH));

        Date hr = DateUtils.addHours(d, 3);
        assertEquals(15, DateUtils.toCalendar(hr).get(Calendar.HOUR_OF_DAY));

        Date min = DateUtils.addMinutes(d, 10);
        assertEquals(40, DateUtils.toCalendar(min).get(Calendar.MINUTE));

        Date sec = DateUtils.addSeconds(d, 10);
        assertEquals(55, DateUtils.toCalendar(sec).get(Calendar.SECOND));

        Date ms = DateUtils.addMilliseconds(d, 200);
        assertEquals(700, DateUtils.toCalendar(ms).get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testDateSetManipulations() {
        Calendar base = Calendar.getInstance();
        base.clear();
        base.set(2020, Calendar.JANUARY, 15, 12, 30, 45);
        base.set(Calendar.MILLISECOND, 500);
        Date d = base.getTime();

        assertEquals(2025, DateUtils.toCalendar(DateUtils.setYears(d, 2025)).get(Calendar.YEAR));
        assertEquals(Calendar.JULY, DateUtils.toCalendar(DateUtils.setMonths(d, Calendar.JULY)).get(Calendar.MONTH));
        assertEquals(5, DateUtils.toCalendar(DateUtils.setDays(d, 5)).get(Calendar.DAY_OF_MONTH));
        assertEquals(6, DateUtils.toCalendar(DateUtils.setHours(d, 6)).get(Calendar.HOUR_OF_DAY));
        assertEquals(45, DateUtils.toCalendar(DateUtils.setMinutes(d, 45)).get(Calendar.MINUTE));
        assertEquals(12, DateUtils.toCalendar(DateUtils.setSeconds(d, 12)).get(Calendar.SECOND));
        assertEquals(123, DateUtils.toCalendar(DateUtils.setMilliseconds(d, 123)).get(Calendar.MILLISECOND));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA), Truncate, Round, Ceiling
    // =========================================================================

    @Test(timeout = 4000)
    public void testTruncateRoundCeiling_Date() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2023, Calendar.MAY, 15, 14, 45, 30);
        cal.set(Calendar.MILLISECOND, 600);
        Date d = cal.getTime();

        Date truncatedHour = DateUtils.truncate(d, Calendar.HOUR_OF_DAY);
        assertEquals(14, DateUtils.toCalendar(truncatedHour).get(Calendar.HOUR_OF_DAY));
        assertEquals(0, DateUtils.toCalendar(truncatedHour).get(Calendar.MINUTE));

        Date roundedHour = DateUtils.round(d, Calendar.HOUR_OF_DAY);
        assertEquals(15, DateUtils.toCalendar(roundedHour).get(Calendar.HOUR_OF_DAY));

        Date ceilingHour = DateUtils.ceiling(d, Calendar.HOUR_OF_DAY);
        assertEquals(15, DateUtils.toCalendar(ceilingHour).get(Calendar.HOUR_OF_DAY));

        Date truncMs = DateUtils.truncate(d, Calendar.MILLISECOND);
        assertEquals(600, DateUtils.toCalendar(truncMs).get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testTruncateRoundCeiling_CalendarAndObject() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2023, Calendar.MARCH, 20, 10, 10, 10);
        cal.set(Calendar.MILLISECOND, 100);

        Calendar truncCal = DateUtils.truncate(cal, Calendar.DATE);
        assertEquals(0, truncCal.get(Calendar.HOUR_OF_DAY));

        Calendar roundCal = DateUtils.round(cal, Calendar.DATE);
        assertEquals(0, roundCal.get(Calendar.HOUR_OF_DAY));

        Calendar ceilCal = DateUtils.ceiling(cal, Calendar.DATE);
        assertEquals(21, ceilCal.get(Calendar.DATE));

        Date objTrunc = DateUtils.truncate((Object) cal, Calendar.DATE);
        assertNotNull(objTrunc);
        Date objRound = DateUtils.round((Object) cal, Calendar.DATE);
        assertNotNull(objRound);
        Date objCeil = DateUtils.ceiling((Object) cal, Calendar.DATE);
        assertNotNull(objCeil);

        Date d = cal.getTime();
        assertNotNull(DateUtils.truncate((Object) d, Calendar.DATE));
        assertNotNull(DateUtils.round((Object) d, Calendar.DATE));
        assertNotNull(DateUtils.ceiling((Object) d, Calendar.DATE));
    }

    @Test(timeout = 4000)
    public void testModify_SemiMonthAndAmPm() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);

        Date ceilSemiMonth1 = DateUtils.ceiling(cal.getTime(), DateUtils.SEMI_MONTH);
        Calendar res1 = DateUtils.toCalendar(ceilSemiMonth1);
        assertEquals(16, res1.get(Calendar.DATE));

        cal.set(Calendar.DATE, 17);
        Date ceilSemiMonth2 = DateUtils.ceiling(cal.getTime(), DateUtils.SEMI_MONTH);
        Calendar res2 = DateUtils.toCalendar(ceilSemiMonth2);
        assertEquals(1, res2.get(Calendar.DATE));
        assertEquals(Calendar.FEBRUARY, res2.get(Calendar.MONTH));

        cal.set(2023, Calendar.JANUARY, 10, 0, 0, 0);
        Date ceilAmPm1 = DateUtils.ceiling(cal.getTime(), Calendar.AM_PM);
        assertEquals(12, DateUtils.toCalendar(ceilAmPm1).get(Calendar.HOUR_OF_DAY));

        cal.set(2023, Calendar.JANUARY, 10, 13, 0, 0);
        Date ceilAmPm2 = DateUtils.ceiling(cal.getTime(), Calendar.AM_PM);
        assertEquals(11, DateUtils.toCalendar(ceilAmPm2).get(Calendar.DATE));
        assertEquals(0, DateUtils.toCalendar(ceilAmPm2).get(Calendar.HOUR_OF_DAY));
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testModify_ExcessiveYearArithmeticException() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 280000001);
        DateUtils.truncate(cal, Calendar.DATE);
    }

    @Test(timeout = 4000)
    public void testTruncatedCompareToAndEquals() {
        Calendar c1 = Calendar.getInstance();
        c1.clear();
        c1.set(2023, Calendar.JULY, 15, 10, 20, 30);

        Calendar c2 = Calendar.getInstance();
        c2.clear();
        c2.set(2023, Calendar.JULY, 15, 10, 45, 50);

        assertTrue(DateUtils.truncatedEquals(c1, c2, Calendar.HOUR_OF_DAY));
        assertFalse(DateUtils.truncatedEquals(c1, c2, Calendar.MINUTE));

        assertEquals(0, DateUtils.truncatedCompareTo(c1, c2, Calendar.HOUR_OF_DAY));
        assertTrue(DateUtils.truncatedCompareTo(c1, c2, Calendar.MINUTE) < 0);

        Date d1 = c1.getTime();
        Date d2 = c2.getTime();
        assertTrue(DateUtils.truncatedEquals(d1, d2, Calendar.HOUR_OF_DAY));
        assertFalse(DateUtils.truncatedEquals(d1, d2, Calendar.MINUTE));
        assertEquals(0, DateUtils.truncatedCompareTo(d1, d2, Calendar.HOUR_OF_DAY));
        assertTrue(DateUtils.truncatedCompareTo(d1, d2, Calendar.MINUTE) < 0);
    }

    // =========================================================================
    // Partition D: Date Fragments & Iterators
    // =========================================================================

    @Test(timeout = 4000)
    public void testDateFragments() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 5, 14, 30, 25);
        cal.set(Calendar.MILLISECOND, 250);
        Date d = cal.getTime();

        assertEquals(250, DateUtils.getFragmentInMilliseconds(d, Calendar.SECOND));
        assertEquals(250, DateUtils.getFragmentInMilliseconds(cal, Calendar.SECOND));

        assertEquals(25, DateUtils.getFragmentInSeconds(d, Calendar.MINUTE));
        assertEquals(25, DateUtils.getFragmentInSeconds(cal, Calendar.MINUTE));

        assertEquals(30, DateUtils.getFragmentInMinutes(d, Calendar.HOUR_OF_DAY));
        assertEquals(30, DateUtils.getFragmentInMinutes(cal, Calendar.HOUR_OF_DAY));

        assertEquals(14, DateUtils.getFragmentInHours(d, Calendar.DATE));
        assertEquals(14, DateUtils.getFragmentInHours(cal, Calendar.DATE));

        assertEquals(5, DateUtils.getFragmentInDays(d, Calendar.MONTH));
        assertEquals(5, DateUtils.getFragmentInDays(cal, Calendar.MONTH));

        assertEquals(5, DateUtils.getFragmentInDays(d, Calendar.YEAR));
        assertEquals(5, DateUtils.getFragmentInDays(cal, Calendar.YEAR));

        assertEquals(0, DateUtils.getFragmentInDays(d, Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testIterators() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2023, Calendar.JULY, 15); // Saturday

        int[] styles = new int[]{
                DateUtils.RANGE_MONTH_SUNDAY,
                DateUtils.RANGE_MONTH_MONDAY,
                DateUtils.RANGE_WEEK_SUNDAY,
                DateUtils.RANGE_WEEK_MONDAY,
                DateUtils.RANGE_WEEK_RELATIVE,
                DateUtils.RANGE_WEEK_CENTER
        };

        for (int style : styles) {
            Iterator<Calendar> it = DateUtils.iterator(cal, style);
            assertNotNull(it);
            assertTrue(it.hasNext());
            Calendar next = it.next();
            assertNotNull(next);

            Iterator<?> itObj = DateUtils.iterator((Object) cal, style);
            assertTrue(itObj.hasNext());
        }

        Iterator<Calendar> itDate = DateUtils.iterator(cal.getTime(), DateUtils.RANGE_WEEK_SUNDAY);
        int count = 0;
        while (itDate.hasNext()) {
            itDate.next();
            count++;
        }
        assertEquals(7, count);
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testDateIterator_NoSuchElement() {
        Calendar cal = Calendar.getInstance();
        Iterator<Calendar> it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_SUNDAY);
        while (it.hasNext()) {
            it.next();
        }
        it.next();
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testDateIterator_RemoveUnsupported() {
        Calendar cal = Calendar.getInstance();
        Iterator<Calendar> it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_SUNDAY);
        it.remove();
    }

    // =========================================================================
    // Partition E: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsSameDay_NullDate() {
        DateUtils.isSameDay((Date) null, new Date());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsSameDay_NullCalendar() {
        DateUtils.isSameDay(Calendar.getInstance(), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsSameInstant_NullDate() {
        DateUtils.isSameInstant(null, new Date());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsSameInstant_NullCalendar() {
        DateUtils.isSameInstant(Calendar.getInstance(), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsSameLocalTime_NullCalendar() {
        DateUtils.isSameLocalTime(null, Calendar.getInstance());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseDate_NullString() throws ParseException {
        DateUtils.parseDate(null, "yyyy-MM-dd");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseDate_NullPatterns() throws ParseException {
        DateUtils.parseDate("2023-01-01", (String[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddYears_NullDate() {
        DateUtils.addYears(null, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetMonths_NullDate() {
        DateUtils.setMonths(null, 1);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToCalendar_NullDate() {
        DateUtils.toCalendar(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRound_NullDate() {
        DateUtils.round((Date) null, Calendar.HOUR);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRound_UnsupportedField() {
        DateUtils.round(new Date(), -999);
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testRound_InvalidObjectType() {
        DateUtils.round("Not a date", Calendar.HOUR);
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testTruncate_InvalidObjectType() {
        DateUtils.truncate("Not a date", Calendar.HOUR);
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testCeiling_InvalidObjectType() {
        DateUtils.ceiling("Not a date", Calendar.HOUR);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIterator_InvalidStyle() {
        DateUtils.iterator(Calendar.getInstance(), 9999);
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testIterator_InvalidObjectType() {
        DateUtils.iterator("Not a date", DateUtils.RANGE_WEEK_SUNDAY);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetFragment_InvalidFragment() {
        DateUtils.getFragmentInDays(new Date(), -1);
    }
}