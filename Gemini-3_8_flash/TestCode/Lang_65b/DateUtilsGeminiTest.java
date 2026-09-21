/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.lang.time.DateUtils
 * Target Defect: LANG-59 / testTruncateLang59 (DST fallback bug when modifying time fields in Calendar)
 * 
 * Branch Coverage Targets:
 * - isSameDay(Date, Date), isSameDay(Calendar, Calendar): null guards, equal/different era, year, day
 * - isSameInstant(Date, Date), isSameInstant(Calendar, Calendar): null guards, equal/different time
 * - isSameLocalTime(Calendar, Calendar): null guards, mismatch in millisecond, second, minute, hour,
 *   day_of_year, year, era, and calendar class type
 * - parseDate(String, String[]): null string/array, multiple patterns, first match, subsequent match,
 *   incomplete parse, parse failure throwing ParseException
 * - add*(Date, int): positive, negative, zero amounts for all date/time units
 * - round & truncate (Date, Calendar, Object): null guards, ClassCastException for invalid types,
 *   ArithmeticException for year > 280,000,000, unsupported field IllegalArgumentException
 * - modify() internal logic:
 *   - roundUp flag condition across fields (<= half, > half)
 *   - SEMI_MONTH: date == 1 vs date != 1, offset >= 15 vs < 15, roundUp offset > 7
 *   - Calendar.AM_PM: offset >= 12 vs < 12, roundUp offset > 6
 *   - fields iteration: MILLISECOND, SECOND, MINUTE, HOUR_OF_DAY/HOUR, DATE/DAY_OF_MONTH/AM_PM,
 *     MONTH/SEMI_MONTH, YEAR, ERA
 * - iterator(Date/Calendar/Object, int): null guards, ClassCastException, invalid rangeStyle guard,
 *   RANGE_MONTH_SUNDAY, RANGE_MONTH_MONDAY, RANGE_WEEK_SUNDAY, RANGE_WEEK_MONDAY,
 *   RANGE_WEEK_RELATIVE, RANGE_WEEK_CENTER, startCutoff / endCutoff wrapping logic (< SUNDAY, > SATURDAY)
 * - DateIterator inner class: hasNext(), next(), NoSuchElementException after exhaustion, remove() (UOE)
 * - Defect Target (LANG-59): Daylight Saving Time transition in "America/Denver" on 2004-10-31. Truncating
 *   Calendar.SECOND, Calendar.MINUTE, etc. across the repeated 1:00-2:00 hour without shifting timezone.
 * ----------------------------------------------------------------------------------------------------
 */
package org.apache.commons.lang.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TimeZone;

public class DateUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsSameDay_Date_TrueAndFalse() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2023, Calendar.MARCH, 15, 10, 30, 0);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2023, Calendar.MARCH, 15, 23, 59, 59);
        Calendar cal3 = Calendar.getInstance();
        cal3.set(2023, Calendar.MARCH, 16, 10, 30, 0);

        assertTrue(DateUtils.isSameDay(cal1.getTime(), cal2.getTime()));
        assertFalse(DateUtils.isSameDay(cal1.getTime(), cal3.getTime()));
    }

    @Test(timeout = 4000)
    public void testIsSameDay_Calendar_AllFieldComparisons() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2023, Calendar.DECEMBER, 25, 8, 0, 0);
        Calendar cal2 = (Calendar) cal1.clone();
        cal2.set(Calendar.HOUR_OF_DAY, 20);

        assertTrue(DateUtils.isSameDay(cal1, cal2));

        // Different day of year
        Calendar calDiffDay = (Calendar) cal1.clone();
        calDiffDay.set(Calendar.DAY_OF_MONTH, 26);
        assertFalse(DateUtils.isSameDay(cal1, calDiffDay));

        // Different year
        Calendar calDiffYear = (Calendar) cal1.clone();
        calDiffYear.set(Calendar.YEAR, 2024);
        assertFalse(DateUtils.isSameDay(cal1, calDiffYear));

        // Different era
        Calendar calDiffEra = (Calendar) cal1.clone();
        calDiffEra.set(Calendar.ERA, GregorianCalendar.BC);
        assertFalse(DateUtils.isSameDay(cal1, calDiffEra));
    }

    @Test(timeout = 4000)
    public void testIsSameInstant_DateAndCalendar() {
        Date d1 = new Date(1672531199000L);
        Date d2 = new Date(1672531199000L);
        Date d3 = new Date(1672531199001L);

        assertTrue(DateUtils.isSameInstant(d1, d2));
        assertFalse(DateUtils.isSameInstant(d1, d3));

        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        Calendar c3 = Calendar.getInstance();
        c3.setTime(d3);

        assertTrue(DateUtils.isSameInstant(c1, c2));
        assertFalse(DateUtils.isSameInstant(c1, c3));
    }

    @Test(timeout = 4000)
    public void testIsSameLocalTime_AllFields() {
        Calendar c1 = Calendar.getInstance();
        c1.set(2023, Calendar.JULY, 4, 15, 30, 45);
        c1.set(Calendar.MILLISECOND, 500);

        Calendar c2 = (Calendar) c1.clone();
        assertTrue(DateUtils.isSameLocalTime(c1, c2));

        // Mismatches per field
        Calendar cDiffMs = (Calendar) c1.clone();
        cDiffMs.set(Calendar.MILLISECOND, 501);
        assertFalse(DateUtils.isSameLocalTime(c1, cDiffMs));

        Calendar cDiffSec = (Calendar) c1.clone();
        cDiffSec.set(Calendar.SECOND, 46);
        assertFalse(DateUtils.isSameLocalTime(c1, cDiffSec));

        Calendar cDiffMin = (Calendar) c1.clone();
        cDiffMin.set(Calendar.MINUTE, 31);
        assertFalse(DateUtils.isSameLocalTime(c1, cDiffMin));

        Calendar cDiffHour = (Calendar) c1.clone();
        cDiffHour.set(Calendar.HOUR, (c1.get(Calendar.HOUR) + 1) % 12);
        assertFalse(DateUtils.isSameLocalTime(c1, cDiffHour));

        Calendar cDiffDoy = (Calendar) c1.clone();
        cDiffDoy.add(Calendar.DAY_OF_YEAR, 1);
        assertFalse(DateUtils.isSameLocalTime(c1, cDiffDoy));

        Calendar cDiffYear = (Calendar) c1.clone();
        cDiffYear.add(Calendar.YEAR, 1);
        assertFalse(DateUtils.isSameLocalTime(c1, cDiffYear));

        Calendar cDiffEra = (Calendar) c1.clone();
        cDiffEra.set(Calendar.ERA, GregorianCalendar.BC);
        assertFalse(DateUtils.isSameLocalTime(c1, cDiffEra));

        // Different calendar implementation
        Calendar anonymousCal = new GregorianCalendar() {};
        anonymousCal.setTime(c1.getTime());
        assertFalse(DateUtils.isSameLocalTime(c1, anonymousCal));
    }

    @Test(timeout = 4000)
    public void testParseDate_SuccessPaths() throws ParseException {
        String[] patterns = new String[] { "yyyy-MM-dd", "yyyy/MM/dd HH:mm:ss", "yyyy.MM.dd" };

        Date date1 = DateUtils.parseDate("2023-05-12", patterns);
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        assertEquals(2023, cal1.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, cal1.get(Calendar.MONTH));
        assertEquals(12, cal1.get(Calendar.DAY_OF_MONTH));

        Date date2 = DateUtils.parseDate("2023/05/12 14:20:30", patterns);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        assertEquals(14, cal2.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, cal2.get(Calendar.MINUTE));
        assertEquals(30, cal2.get(Calendar.SECOND));
    }

    @Test(timeout = 4000)
    public void testAddMethods() {
        Calendar base = Calendar.getInstance();
        base.clear();
        base.set(2020, Calendar.JANUARY, 15, 12, 30, 40);
        base.set(Calendar.MILLISECOND, 500);
        Date original = base.getTime();

        assertEquals(2021, getField(DateUtils.addYears(original, 1), Calendar.YEAR));
        assertEquals(2019, getField(DateUtils.addYears(original, -1), Calendar.YEAR));

        assertEquals(Calendar.MARCH, getField(DateUtils.addMonths(original, 2), Calendar.MONTH));
        assertEquals(Calendar.NOVEMBER, getField(DateUtils.addMonths(original, -2), Calendar.MONTH));

        assertEquals(22, getField(DateUtils.addWeeks(original, 1), Calendar.DAY_OF_MONTH));
        assertEquals(8, getField(DateUtils.addWeeks(original, -1), Calendar.DAY_OF_MONTH));

        assertEquals(16, getField(DateUtils.addDays(original, 1), Calendar.DAY_OF_MONTH));
        assertEquals(14, getField(DateUtils.addDays(original, -1), Calendar.DAY_OF_MONTH));

        assertEquals(15, getField(DateUtils.addHours(original, 3), Calendar.HOUR_OF_DAY));
        assertEquals(9, getField(DateUtils.addHours(original, -3), Calendar.HOUR_OF_DAY));

        assertEquals(45, getField(DateUtils.addMinutes(original, 15), Calendar.MINUTE));
        assertEquals(15, getField(DateUtils.addMinutes(original, -15), Calendar.MINUTE));

        assertEquals(50, getField(DateUtils.addSeconds(original, 10), Calendar.SECOND));
        assertEquals(30, getField(DateUtils.addSeconds(original, -10), Calendar.SECOND));

        assertEquals(700, getField(DateUtils.addMilliseconds(original, 200), Calendar.MILLISECOND));
        assertEquals(300, getField(DateUtils.addMilliseconds(original, -200), Calendar.MILLISECOND));

        // Original date was unaltered
        assertEquals(original, base.getTime());
    }

    @Test(timeout = 4000)
    public void testRoundAndTruncate_StandardFields() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2023, Calendar.JUNE, 15, 12, 30, 40);
        cal.set(Calendar.MILLISECOND, 600);
        Date d = cal.getTime();

        // Truncate to second: drops milliseconds
        Date truncSec = DateUtils.truncate(d, Calendar.SECOND);
        assertEquals(0, getField(truncSec, Calendar.MILLISECOND));
        assertEquals(40, getField(truncSec, Calendar.SECOND));

        // Round to second: 600ms rounds second up to 41
        Date roundSec = DateUtils.round(d, Calendar.SECOND);
        assertEquals(0, getField(roundSec, Calendar.MILLISECOND));
        assertEquals(41, getField(roundSec, Calendar.SECOND));

        // Truncate to minute: drops seconds and ms
        Date truncMin = DateUtils.truncate(d, Calendar.MINUTE);
        assertEquals(30, getField(truncMin, Calendar.MINUTE));
        assertEquals(0, getField(truncMin, Calendar.SECOND));

        // Round to minute: 40 seconds rounds minute up to 31
        Date roundMin = DateUtils.round(d, Calendar.MINUTE);
        assertEquals(31, getField(roundMin, Calendar.MINUTE));

        // Truncate to hour
        Date truncHour = DateUtils.truncate(d, Calendar.HOUR_OF_DAY);
        assertEquals(12, getField(truncHour, Calendar.HOUR_OF_DAY));
        assertEquals(0, getField(truncHour, Calendar.MINUTE));

        // Round to hour: 30 minutes rounds hour up to 13 (offset > (59-0)/2 => 30 > 29)
        Date roundHour = DateUtils.round(d, Calendar.HOUR_OF_DAY);
        assertEquals(13, getField(roundHour, Calendar.HOUR_OF_DAY));

        // Round Date, Calendar, and Object polymorphic forms
        Calendar calObj = (Calendar) cal.clone();
        assertEquals(truncSec, DateUtils.truncate(calObj, Calendar.SECOND).getTime());
        assertEquals(roundSec, DateUtils.round(calObj, Calendar.SECOND).getTime());

        assertEquals(truncSec, DateUtils.truncate((Object) d, Calendar.SECOND));
        assertEquals(roundSec, DateUtils.round((Object) d, Calendar.SECOND));
        assertEquals(truncSec, DateUtils.truncate((Object) calObj, Calendar.SECOND));
        assertEquals(roundSec, DateUtils.round((Object) calObj, Calendar.SECOND));
    }

    @Test(timeout = 4000)
    public void testRoundAndTruncate_SemiMonth() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2023, Calendar.APRIL, 1, 0, 0, 0);

        // Date 1 truncated to SEMI_MONTH remains 1
        assertEquals(1, getField(DateUtils.truncate(cal.getTime(), DateUtils.SEMI_MONTH), Calendar.DAY_OF_MONTH));

        // Date 1 rounded to SEMI_MONTH: offset 0 <= 7, remains 1
        assertEquals(1, getField(DateUtils.round(cal.getTime(), DateUtils.SEMI_MONTH), Calendar.DAY_OF_MONTH));

        // Date 9 rounded to SEMI_MONTH: offset = 9 - 1 = 8 > 7 => roundUp -> add 15 to 1 => 16
        cal.set(Calendar.DAY_OF_MONTH, 9);
        assertEquals(16, getField(DateUtils.round(cal.getTime(), DateUtils.SEMI_MONTH), Calendar.DAY_OF_MONTH));

        // Date 16 truncated: offset = 15 >= 15 -> offset becomes 0 -> day 16
        cal.set(Calendar.DAY_OF_MONTH, 16);
        assertEquals(16, getField(DateUtils.truncate(cal.getTime(), DateUtils.SEMI_MONTH), Calendar.DAY_OF_MONTH));

        // Date 24: offset = 23 -> offset - 15 = 8 > 7 => roundUp -> subtract 15 days, add 1 month => May 1
        cal.set(Calendar.DAY_OF_MONTH, 24);
        Date rounded24 = DateUtils.round(cal.getTime(), DateUtils.SEMI_MONTH);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(rounded24);
        assertEquals(1, resultCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.MAY, resultCal.get(Calendar.MONTH));
    }

    @Test(timeout = 4000)
    public void testRoundAndTruncate_AmPm() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 10, 4, 0, 0); // AM

        Date truncAm = DateUtils.truncate(cal.getTime(), Calendar.AM_PM);
        assertEquals(0, getField(truncAm, Calendar.HOUR_OF_DAY));

        cal.set(Calendar.HOUR_OF_DAY, 8); // 8 AM: offset 8 > 6 => round up to 12 PM
        Date roundAm = DateUtils.round(cal.getTime(), Calendar.AM_PM);
        assertEquals(12, getField(roundAm, Calendar.HOUR_OF_DAY));

        cal.set(Calendar.HOUR_OF_DAY, 14); // 2 PM: offset 14 - 12 = 2 <= 6 => round down to 12 PM
        Date roundPmDown = DateUtils.round(cal.getTime(), Calendar.AM_PM);
        assertEquals(12, getField(roundPmDown, Calendar.HOUR_OF_DAY));

        cal.set(Calendar.HOUR_OF_DAY, 20); // 8 PM: offset 20 - 12 = 8 > 6 => round up to next day 0 AM
        Date roundPmUp = DateUtils.round(cal.getTime(), Calendar.AM_PM);
        Calendar rCal = Calendar.getInstance();
        rCal.setTime(roundPmUp);
        assertEquals(11, rCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, rCal.get(Calendar.HOUR_OF_DAY));
    }

    @Test(timeout = 4000)
    public void testIterator_AllRangeStyles() {
        Calendar focus = Calendar.getInstance();
        focus.clear();
        focus.set(2002, Calendar.JULY, 4); // Thursday, July 4, 2002

        int[] rangeStyles = new int[] {
            DateUtils.RANGE_MONTH_SUNDAY,
            DateUtils.RANGE_MONTH_MONDAY,
            DateUtils.RANGE_WEEK_SUNDAY,
            DateUtils.RANGE_WEEK_MONDAY,
            DateUtils.RANGE_WEEK_RELATIVE,
            DateUtils.RANGE_WEEK_CENTER
        };

        for (int style : rangeStyles) {
            Iterator it = DateUtils.iterator(focus, style);
            assertNotNull(it);
            assertTrue(it.hasNext());
            int count = 0;
            Calendar prev = null;
            while (it.hasNext()) {
                Calendar current = (Calendar) it.next();
                assertNotNull(current);
                if (prev != null) {
                    assertTrue(current.after(prev));
                }
                prev = current;
                count++;
            }
            assertTrue("Expected at least a week of days", count >= 7);
        }

        // Test polymorphic Date and Object invocations
        Iterator itDate = DateUtils.iterator(focus.getTime(), DateUtils.RANGE_WEEK_SUNDAY);
        assertTrue(itDate.hasNext());
        Iterator itObj = DateUtils.iterator((Object) focus, DateUtils.RANGE_WEEK_SUNDAY);
        assertTrue(itObj.hasNext());
        Iterator itObjDate = DateUtils.iterator((Object) focus.getTime(), DateUtils.RANGE_WEEK_SUNDAY);
        assertTrue(itObjDate.hasNext());
    }

    @Test(timeout = 4000)
    public void testDateIterator_ExhaustionAndContract() {
        Calendar focus = Calendar.getInstance();
        focus.clear();
        focus.set(2023, Calendar.JANUARY, 1);

        Iterator it = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_SUNDAY);
        while (it.hasNext()) {
            it.next();
        }
        assertFalse(it.hasNext());

        try {
            it.next();
            fail("Expected NoSuchElementException after iterator exhaustion");
        } catch (NoSuchElementException expected) {
            // Success
        }

        try {
            it.remove();
            fail("Expected UnsupportedOperationException on remove");
        } catch (UnsupportedOperationException expected) {
            // Success
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullInputs_ThrowIllegalArgumentException() throws ParseException {
        // isSameDay
        try {
            DateUtils.isSameDay((Date) null, new Date());
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            DateUtils.isSameDay(new Date(), (Date) null);
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            DateUtils.isSameDay((Calendar) null, Calendar.getInstance());
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            DateUtils.isSameDay(Calendar.getInstance(), (Calendar) null);
            fail();
        } catch (IllegalArgumentException expected) {}

        // isSameInstant
        try {
            DateUtils.isSameInstant((Date) null, new Date());
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            DateUtils.isSameInstant(new Date(), (Date) null);
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            DateUtils.isSameInstant((Calendar) null, Calendar.getInstance());
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            DateUtils.isSameInstant(Calendar.getInstance(), (Calendar) null);
            fail();
        } catch (IllegalArgumentException expected) {}

        // isSameLocalTime
        try {
            DateUtils.isSameLocalTime((Calendar) null, Calendar.getInstance());
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            DateUtils.isSameLocalTime(Calendar.getInstance(), (Calendar) null);
            fail();
        } catch (IllegalArgumentException expected) {}

        // parseDate
        try {
            DateUtils.parseDate(null, new String[] { "yyyy" });
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            DateUtils.parseDate("2023", null);
            fail();
        } catch (IllegalArgumentException expected) {}

        // add
        try {
            DateUtils.add(null, Calendar.YEAR, 1);
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            DateUtils.addYears(null, 1);
            fail();
        } catch (IllegalArgumentException expected) {}

        // round / truncate
        try {
            DateUtils.round((Date) null, Calendar.DATE);
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            DateUtils.round((Calendar) null, Calendar.DATE);
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            DateUtils.round((Object) null, Calendar.DATE);
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            DateUtils.truncate((Date) null, Calendar.DATE);
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            DateUtils.truncate((Calendar) null, Calendar.DATE);
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            DateUtils.truncate((Object) null, Calendar.DATE);
            fail();
        } catch (IllegalArgumentException expected) {}

        // iterator
        try {
            DateUtils.iterator((Date) null, DateUtils.RANGE_WEEK_SUNDAY);
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            DateUtils.iterator((Calendar) null, DateUtils.RANGE_WEEK_SUNDAY);
            fail();
        } catch (IllegalArgumentException expected) {}
        try {
            DateUtils.iterator((Object) null, DateUtils.RANGE_WEEK_SUNDAY);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    @Test(timeout = 4000)
    public void testRoundAndTruncate_YearLimitArithmeticException() {
        Calendar largeCal = Calendar.getInstance();
        largeCal.set(Calendar.YEAR, 280000001);
        Date largeDate = largeCal.getTime();

        try {
            DateUtils.truncate(largeDate, Calendar.DAY_OF_MONTH);
            fail("Expected ArithmeticException when year > 280,000,000");
        } catch (ArithmeticException expected) {
            // Success
        }

        try {
            DateUtils.round(largeDate, Calendar.DAY_OF_MONTH);
            fail("Expected ArithmeticException when year > 280,000,000");
        } catch (ArithmeticException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testRoundAndTruncate_UnsupportedField() {
        Date now = new Date();
        try {
            DateUtils.truncate(now, -9999);
            fail("Expected IllegalArgumentException for unsupported field");
        } catch (IllegalArgumentException expected) {
            // Success
        }

        try {
            DateUtils.round(now, -9999);
            fail("Expected IllegalArgumentException for unsupported field");
        } catch (IllegalArgumentException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testInvalidRangeStyle_ThrowsIllegalArgumentException() {
        Calendar cal = Calendar.getInstance();
        try {
            DateUtils.iterator(cal, 99999);
            fail("Expected IllegalArgumentException for invalid rangeStyle");
        } catch (IllegalArgumentException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testInvalidObjectType_ThrowsClassCastException() {
        String invalidObj = "Not A Date Or Calendar";
        try {
            DateUtils.round(invalidObj, Calendar.DATE);
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {}

        try {
            DateUtils.truncate(invalidObj, Calendar.DATE);
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {}

        try {
            DateUtils.iterator(invalidObj, DateUtils.RANGE_WEEK_SUNDAY);
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {}
    }

    @Test(timeout = 4000)
    public void testParseDate_NoMatchThrowsParseException() {
        try {
            DateUtils.parseDate("not-a-date", new String[] { "yyyy-MM-dd", "yyyy/MM/dd" });
            fail("Expected ParseException");
        } catch (ParseException expected) {
            assertEquals(-1, expected.getErrorOffset());
        }
    }

    @Test(timeout = 4000)
    public void testParseDate_IncompleteMatchThrowsParseException() {
        // String has trailing characters that do not match the pattern completely
        try {
            DateUtils.parseDate("2023-01-01 extra", new String[] { "yyyy-MM-dd" });
            fail("Expected ParseException for unparsed suffix");
        } catch (ParseException expected) {
            assertEquals(-1, expected.getErrorOffset());
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (LANG-59)
    // =========================================================================

    /**
     * LANG-59: Truncating a date near daylight saving time transition in America/Denver.
     * On 2004-10-31 01:02:03.004 MDT, daylight saving time shifts back 1 hour at 02:00 MDT -> 01:00 MST.
     * In defective versions, modifying lower fields through Calendar.set causes an unexpected timezone
     * shift (MDT -> MST), altering the millisecond instant.
     */
    @Test(timeout = 4000)
    public void testTruncateLang59() {
        TimeZone defaultZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Denver"));

            Date oct31_01_02_03_04_mdt = new Date(1099206123004L);
            Date oct31_01_02_03_mdt    = new Date(1099206123000L);
            Date oct31_01_02_mdt       = new Date(1099206120000L);
            Date oct31_01_mdt          = new Date(1099206000000L);
            Date oct31_00_mdt          = new Date(1099202400000L);

            assertEquals("Truncate Calendar.SECOND", oct31_01_02_03_mdt,
                    DateUtils.truncate(oct31_01_02_03_04_mdt, Calendar.SECOND));
            assertEquals("Truncate Calendar.MINUTE", oct31_01_02_mdt,
                    DateUtils.truncate(oct31_01_02_03_04_mdt, Calendar.MINUTE));
            assertEquals("Truncate Calendar.HOUR_OF_DAY", oct31_01_mdt,
                    DateUtils.truncate(oct31_01_02_03_04_mdt, Calendar.HOUR_OF_DAY));
            assertEquals("Truncate Calendar.HOUR", oct31_01_mdt,
                    DateUtils.truncate(oct31_01_02_03_04_mdt, Calendar.HOUR));
            assertEquals("Truncate Calendar.DATE", oct31_00_mdt,
                    DateUtils.truncate(oct31_01_02_03_04_mdt, Calendar.DATE));
        } finally {
            TimeZone.setDefault(defaultZone);
        }
    }

    // =========================================================================
    // Partition D: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAndConstants() {
        DateUtils utils = new DateUtils();
        assertNotNull(utils);

        assertEquals(1000L, DateUtils.MILLIS_PER_SECOND);
        assertEquals(60000L, DateUtils.MILLIS_PER_MINUTE);
        assertEquals(3600000L, DateUtils.MILLIS_PER_HOUR);
        assertEquals(86400000L, DateUtils.MILLIS_PER_DAY);

        assertEquals(1000, DateUtils.MILLIS_IN_SECOND);
        assertEquals(60000, DateUtils.MILLIS_IN_MINUTE);
        assertEquals(3600000, DateUtils.MILLIS_IN_HOUR);
        assertEquals(86400000, DateUtils.MILLIS_IN_DAY);

        assertEquals(1001, DateUtils.SEMI_MONTH);
        assertEquals("GMT", DateUtils.UTC_TIME_ZONE.getID());
    }

    // -------------------------------------------------------------------------
    // Helper method
    // -------------------------------------------------------------------------
    private static int getField(Date date, int field) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(field);
    }
}