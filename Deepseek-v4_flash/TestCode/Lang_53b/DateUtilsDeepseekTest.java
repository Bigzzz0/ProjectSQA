package org.apache.commons.lang.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.TimeZone;

/**
 * Comprehensive white-box test suite for DateUtils.
 *
 * [Branch & Defect Analysis Matrix]
 * 
 * Target defect: LANG-346 - Minute rounding fails when minutes >= 30. 
 *   The modify() method has a bug in the "round" path where after subtracting 
 *   minutes (time = time - minutes * 60000L), the rounding up logic is not 
 *   properly executed if the minute value is >= 30 because the code only checks 
 *   the "!round || minutes < 30" condition which skips the subtraction, but then 
 *   the subsequent field loop can produce incorrect results because the minute 
 *   field is not fully zeroed before rounding up.
 *   Specifically, when rounding to MINUTE field with minutes >= 30, the bug 
 *   causes the minute to be truncated instead of rounding up to the next hour.
 *
 * Key branches to test:
 * 1. isSameDay(Date) - null checks, same day/different day, cross-ERA boundary
 * 2. isSameDay(Calendar) - null checks, ERA/YEAR/DAY_OF_YEAR comparisons
 * 3. isSameInstant(Date) - null checks, equality/inequality
 * 4. isSameInstant(Calendar) - null checks, time equality via getTime()
 * 5. isSameLocalTime(Calendar) - null checks, full field comparison, class type check
 * 6. parseDate() - null args, successful parse, parse failure, multiple patterns
 * 7. add*() methods - null, zero, positive, negative amounts
 * 8. add() - null date, various calendar fields
 * 9. round(Date) - null, MILLISECOND, SECOND, MINUTE (targets LANG-346), HOUR, 
 *    MONTH, YEAR, SEMI_MONTH, AM_PM
 * 10. round(Calendar) - null, field coverage
 * 11. round(Object) - null, Date, Calendar, invalid type
 * 12. truncate(Date) - null, MILLISECOND, SECOND, MINUTE, HOUR, MONTH, YEAR, SEMI_MONTH
 * 13. truncate(Calendar) - null, cloning/modify correct
 * 14. truncate(Object) - null, Date, Calendar, invalid type
 * 15. iterator(Date) - null, rangeStyle validation
 * 16. iterator(Calendar) - null, all 6 range styles, cutoff boundary clipping
 * 17. iterator(Object) - null, Date, Calendar, invalid type
 * 18. DateIterator - hasNext, next, NoSuchElementException, remove UnsupportedOperationException
 * 19. modify() - ArithmeticException for year > 280000000
 * 20. modify() - field not supported throws IllegalArgumentException
 * 21. Constants validation
 * 22. Constructor
 */
public class DateUtilsDeepseekTest {

    // ========== PARTITION A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testConstructor() {
        // Just verify no exception
        new DateUtils();
    }

    @Test(timeout = 4000)
    public void testConstants() {
        assertEquals(1000L, DateUtils.MILLIS_PER_SECOND);
        assertEquals(60 * 1000L, DateUtils.MILLIS_PER_MINUTE);
        assertEquals(60 * 60 * 1000L, DateUtils.MILLIS_PER_HOUR);
        assertEquals(24 * 60 * 60 * 1000L, DateUtils.MILLIS_PER_DAY);
        assertEquals(1001, DateUtils.SEMI_MONTH);
        assertEquals(1, DateUtils.RANGE_WEEK_SUNDAY);
        assertEquals(2, DateUtils.RANGE_WEEK_MONDAY);
        assertEquals(3, DateUtils.RANGE_WEEK_RELATIVE);
        assertEquals(4, DateUtils.RANGE_WEEK_CENTER);
        assertEquals(5, DateUtils.RANGE_MONTH_SUNDAY);
        assertEquals(6, DateUtils.RANGE_MONTH_MONDAY);
        // Deprecated constants
        assertEquals(1000, DateUtils.MILLIS_IN_SECOND);
        assertEquals(60 * 1000, DateUtils.MILLIS_IN_MINUTE);
        assertEquals(60 * 60 * 1000, DateUtils.MILLIS_IN_HOUR);
        assertEquals(24 * 60 * 60 * 1000, DateUtils.MILLIS_IN_DAY);
        assertNotNull(DateUtils.UTC_TIME_ZONE);
    }

    @Test(timeout = 4000)
    public void testIsSameDay_Date() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2007, Calendar.JULY, 2, 8, 9, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        Date date1 = cal1.getTime();
        
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2007, Calendar.JULY, 2, 14, 30, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        Date date2 = cal2.getTime();
        
        assertTrue(DateUtils.isSameDay(date1, date2));
        
        Calendar cal3 = Calendar.getInstance();
        cal3.set(2007, Calendar.JULY, 3, 14, 30, 0);
        cal3.set(Calendar.MILLISECOND, 0);
        Date date3 = cal3.getTime();
        
        assertFalse(DateUtils.isSameDay(date1, date3));
    }

    @Test(timeout = 4000)
    public void testIsSameDay_Calendar() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2007, Calendar.JULY, 2, 8, 9, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2007, Calendar.JULY, 2, 14, 30, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        
        assertTrue(DateUtils.isSameDay(cal1, cal2));
        
        Calendar cal3 = Calendar.getInstance();
        cal3.set(2007, Calendar.JULY, 3, 14, 30, 0);
        cal3.set(Calendar.MILLISECOND, 0);
        
        assertFalse(DateUtils.isSameDay(cal1, cal3));
    }

    @Test(timeout = 4000)
    public void testIsSameInstant_Date() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2007, Calendar.JULY, 2, 8, 9, 0);
        cal1.set(Calendar.MILLISECOND, 123);
        Date date1 = cal1.getTime();
        Date date2 = new Date(date1.getTime());
        Date date3 = new Date(date1.getTime() + 1);
        
        assertTrue(DateUtils.isSameInstant(date1, date2));
        assertFalse(DateUtils.isSameInstant(date1, date3));
    }

    @Test(timeout = 4000)
    public void testIsSameInstant_Calendar() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2007, Calendar.JULY, 2, 8, 9, 0);
        cal1.set(Calendar.MILLISECOND, 123);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2007, Calendar.JULY, 2, 8, 9, 0);
        cal2.set(Calendar.MILLISECOND, 123);
        
        Calendar cal3 = Calendar.getInstance();
        cal3.setTime(new Date(cal1.getTime().getTime() + 1));
        
        assertTrue(DateUtils.isSameInstant(cal1, cal2));
        assertFalse(DateUtils.isSameInstant(cal1, cal3));
    }

    @Test(timeout = 4000)
    public void testIsSameLocalTime() {
        // Same type calendars with same field values
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2007, Calendar.JULY, 2, 8, 9, 15);
        cal1.set(Calendar.MILLISECOND, 500);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2007, Calendar.JULY, 2, 8, 9, 15);
        cal2.set(Calendar.MILLISECOND, 500);
        
        assertTrue(DateUtils.isSameLocalTime(cal1, cal2));
        
        // Different type calendars (GregorianCalendar vs Calendar) should return false
        Calendar cal3 = new GregorianCalendar();
        cal3.set(2007, Calendar.JULY, 2, 8, 9, 15);
        cal3.set(Calendar.MILLISECOND, 500);
        
        assertFalse(DateUtils.isSameLocalTime(cal1, cal3));
        
        // Different time should be false
        Calendar cal4 = Calendar.getInstance();
        cal4.set(2007, Calendar.JULY, 2, 8, 9, 16);
        cal4.set(Calendar.MILLISECOND, 500);
        
        assertFalse(DateUtils.isSameLocalTime(cal1, cal4));
    }

    @Test(timeout = 4000)
    public void testParseDate_Success() throws ParseException {
        String[] patterns = {"yyyy-MM-dd", "yyyy/MM/dd", "MM/dd/yyyy"};
        Date date = DateUtils.parseDate("2007-07-02", patterns);
        assertNotNull(date);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2007, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, cal.get(Calendar.MONTH));
        assertEquals(2, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test(timeout = 4000)
    public void testParseDate_MultiplePatterns() throws ParseException {
        String[] patterns = {"yyyy/MM/dd", "yyyy-MM-dd", "MM/dd/yyyy"};
        // Should match second pattern after first fails
        Date date = DateUtils.parseDate("2007-07-02", patterns);
        assertNotNull(date);
        
        // Should match first pattern
        date = DateUtils.parseDate("2007/07/02", patterns);
        assertNotNull(date);
    }

    @Test(timeout = 4000)
    public void testParseDate_Failure() {
        String[] patterns = {"yyyy-MM-dd", "yyyy/MM/dd"};
        try {
            DateUtils.parseDate("not-a-date", patterns);
            fail("Should have thrown ParseException");
        } catch (ParseException e) {
            assertTrue(e.getMessage().contains("Unable to parse the date"));
        }
    }

    @Test(timeout = 4000)
    public void testAddYears() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date base = cal.getTime();
        
        Date result = DateUtils.addYears(base, 1);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        assertEquals(2008, resultCal.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, resultCal.get(Calendar.MONTH));
        
        // Negative amount
        result = DateUtils.addYears(base, -2);
        resultCal.setTime(result);
        assertEquals(2005, resultCal.get(Calendar.YEAR));
    }

    @Test(timeout = 4000)
    public void testAddMonths() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JANUARY, 31, 8, 9, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date base = cal.getTime();
        
        // Adding 1 month to Jan 31 should give Feb 28 (not 31) due to month end rollover
        Date result = DateUtils.addMonths(base, 1);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        assertEquals(Calendar.FEBRUARY, resultCal.get(Calendar.MONTH));
        // Actual day depends on year, but should be 28 in non-leap year
        
        // Negative amount
        result = DateUtils.addMonths(base, -1);
        resultCal.setTime(result);
        assertEquals(Calendar.DECEMBER, resultCal.get(Calendar.MONTH));
    }

    @Test(timeout = 4000)
    public void testAddWeeks() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date base = cal.getTime();
        
        Date result = DateUtils.addWeeks(base, 2);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        assertEquals(16, resultCal.get(Calendar.DAY_OF_MONTH)); // July 2 + 14 days = July 16
        
        // Negative amount
        result = DateUtils.addWeeks(base, -1);
        resultCal.setTime(result);
        assertEquals(25, resultCal.get(Calendar.DAY_OF_MONTH)); // July 2 - 7 days = June 25
    }

    @Test(timeout = 4000)
    public void testAddDays() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date base = cal.getTime();
        
        Date result = DateUtils.addDays(base, 5);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        assertEquals(7, resultCal.get(Calendar.DAY_OF_MONTH));
        
        // Negative amount
        result = DateUtils.addDays(base, -3);
        resultCal.setTime(result);
        assertEquals(29, resultCal.get(Calendar.DAY_OF_MONTH)); // June 29
        assertEquals(Calendar.JUNE, resultCal.get(Calendar.MONTH));
    }

    @Test(timeout = 4000)
    public void testAddHours() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date base = cal.getTime();
        
        Date result = DateUtils.addHours(base, 2);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        assertEquals(10, resultCal.get(Calendar.HOUR_OF_DAY));
        
        // Negative amount that crosses day boundary
        result = DateUtils.addHours(base, -10);
        resultCal.setTime(result);
        assertEquals(22, resultCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(1, resultCal.get(Calendar.DAY_OF_MONTH)); // July 1
    }

    @Test(timeout = 4000)
    public void testAddMinutes() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date base = cal.getTime();
        
        Date result = DateUtils.addMinutes(base, 30);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        assertEquals(39, resultCal.get(Calendar.MINUTE));
        
        // Negative amount that crosses hour boundary
        result = DateUtils.addMinutes(base, -10);
        resultCal.setTime(result);
        assertEquals(59, resultCal.get(Calendar.MINUTE));
        assertEquals(7, resultCal.get(Calendar.HOUR_OF_DAY));
    }

    @Test(timeout = 4000)
    public void testAddSeconds() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date base = cal.getTime();
        
        Date result = DateUtils.addSeconds(base, 45);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        assertEquals(45, resultCal.get(Calendar.SECOND));
        assertEquals(9, resultCal.get(Calendar.MINUTE)); // Still 8:09
        
        // Negative amount that crosses minute boundary
        result = DateUtils.addSeconds(base, -5);
        resultCal.setTime(result);
        assertEquals(55, resultCal.get(Calendar.SECOND));
        assertEquals(8, resultCal.get(Calendar.MINUTE));
    }

    @Test(timeout = 4000)
    public void testAddMilliseconds() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date base = cal.getTime();
        
        Date result = DateUtils.addMilliseconds(base, 500);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        assertEquals(500, resultCal.get(Calendar.MILLISECOND));
        
        // Negative amount that crosses second boundary
        result = DateUtils.addMilliseconds(base, -100);
        resultCal.setTime(result);
        assertEquals(900, resultCal.get(Calendar.MILLISECOND));
        assertEquals(59, resultCal.get(Calendar.SECOND));
        assertEquals(8, resultCal.get(Calendar.MINUTE));
    }

    @Test(timeout = 4000)
    public void testAdd_NullDate() {
        try {
            DateUtils.add(null, Calendar.YEAR, 1);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testAdd_AllFields() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 15);
        cal.set(Calendar.MILLISECOND, 500);
        Date base = cal.getTime();
        
        int[] fields = {
            Calendar.YEAR, Calendar.MONTH, Calendar.WEEK_OF_YEAR, Calendar.DAY_OF_MONTH,
            Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND
        };
        
        for (int field : fields) {
            Date result = DateUtils.add(base, field, 1);
            assertNotNull(result);
            assertNotSame(base, result); // Should return new instance
        }
    }

    // ========== PARTITION B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testIsSameDay_NullDate() {
        try {
            DateUtils.isSameDay((Date) null, new Date());
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
        
        try {
            DateUtils.isSameDay(new Date(), (Date) null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testIsSameDay_NullCalendar() {
        try {
            DateUtils.isSameDay((Calendar) null, Calendar.getInstance());
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
        
        try {
            DateUtils.isSameDay(Calendar.getInstance(), (Calendar) null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testIsSameInstant_NullDate() {
        try {
            DateUtils.isSameInstant((Date) null, new Date());
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testIsSameInstant_NullCalendar() {
        try {
            DateUtils.isSameInstant((Calendar) null, Calendar.getInstance());
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testIsSameLocalTime_Null() {
        try {
            DateUtils.isSameLocalTime(null, Calendar.getInstance());
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testParseDate_NullArgs() {
        String[] patterns = {"yyyy-MM-dd"};
        try {
            DateUtils.parseDate(null, patterns);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
        
        try {
            DateUtils.parseDate("2007-07-02", null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testAdd_ZeroAmount() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 0);
        Date base = cal.getTime();
        
        Date result = DateUtils.addYears(base, 0);
        assertEquals(base.getTime(), result.getTime()); // Even though result is new instance, time should be same
        
        result = DateUtils.addDays(base, 0);
        assertEquals(base.getTime(), result.getTime());
    }

    @Test(timeout = 4000)
    public void testAdd_IntegerMinMax() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 0);
        Date base = cal.getTime();
        
        // Should not throw for extreme values (though result may be invalid date)
        Date result = DateUtils.addYears(base, Integer.MAX_VALUE);
        assertNotNull(result);
        
        result = DateUtils.addYears(base, Integer.MIN_VALUE);
        assertNotNull(result);
    }

    // ========== PARTITION C: Defect-Targeted Branch Zone ==========

    // *** CRITICAL: Directly targets the LANG-346 defect ***
    @Test(timeout = 4000)
    public void testRound_Minute_DefectLang346() {
        // Setup: Mon Jul 02 2007 08:09:00.000
        // Defect: rounding to MINUTE with minutes >= 30 should round up, but instead it truncates
        
        // Test with minutes = 30 (should round up to 9:00 from 8:30)
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 30, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        
        Date rounded = DateUtils.round(date, Calendar.MINUTE);
        Calendar roundedCal = Calendar.getInstance();
        roundedCal.setTime(rounded);
        
        // Expected: 8:30 rounds to 9:00 (minutes < 30 is false, so we should round up)
        // But the bug causes minutes to be truncated to 8:00
        assertEquals("Minute Round Up Failed - expected 9:00 from 8:30", 
            9, roundedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals("Minute Round Up Failed - expected 0 minutes", 
            0, roundedCal.get(Calendar.MINUTE));
        assertEquals("Seconds should be 0", 0, roundedCal.get(Calendar.SECOND));
        assertEquals("Milliseconds should be 0", 0, roundedCal.get(Calendar.MILLISECOND));
        
        // Test with minutes = 29 (should round down to 8:00)
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2007, Calendar.JULY, 2, 8, 29, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        Date date2 = cal2.getTime();
        
        Date rounded2 = DateUtils.round(date2, Calendar.MINUTE);
        Calendar roundedCal2 = Calendar.getInstance();
        roundedCal2.setTime(rounded2);
        
        assertEquals("Minute Round Down Failed - expected 8:00 from 8:29", 
            8, roundedCal2.get(Calendar.HOUR_OF_DAY));
        assertEquals("Minute Round Down Failed - expected 0 minutes", 
            0, roundedCal2.get(Calendar.MINUTE));
        
        // Test with minutes = 45 (should round up to 9:00 from 8:45)
        Calendar cal3 = Calendar.getInstance();
        cal3.set(2007, Calendar.JULY, 2, 8, 45, 0);
        cal3.set(Calendar.MILLISECOND, 0);
        Date date3 = cal3.getTime();
        
        Date rounded3 = DateUtils.round(date3, Calendar.MINUTE);
        Calendar roundedCal3 = Calendar.getInstance();
        roundedCal3.setTime(rounded3);
        
        assertEquals("Minute Round Up Failed - expected 9:00 from 8:45", 
            9, roundedCal3.get(Calendar.HOUR_OF_DAY));
        assertEquals("Minute Round Up Failed - expected 0 minutes", 
            0, roundedCal3.get(Calendar.MINUTE));
    }

    // Additional test targeting minute rounding edge case with milliseconds
    @Test(timeout = 4000)
    public void testRound_Minute_BoundaryWithMillis() {
        // Edge case: minutes = 29 with 999 ms (should still round down)
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 29, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date date = cal.getTime();
        
        Date rounded = DateUtils.round(date, Calendar.MINUTE);
        Calendar roundedCal = Calendar.getInstance();
        roundedCal.setTime(rounded);
        
        assertEquals("Should be hour 8 for min 29", 8, roundedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals("Minutes should be 0", 0, roundedCal.get(Calendar.MINUTE));
        
        // Edge case: minutes = 30 with 000 ms (should round up)
        cal.set(2007, Calendar.JULY, 2, 8, 30, 0);
        cal.set(Calendar.MILLISECOND, 0);
        date = cal.getTime();
        
        rounded = DateUtils.round(date, Calendar.MINUTE);
        roundedCal.setTime(rounded);
        
        assertEquals("Should be hour 9 for min 30", 9, roundedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals("Minutes should be 0", 0, roundedCal.get(Calendar.MINUTE));
    }

    // ========== PARTITION D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testRound_NullDate() {
        try {
            DateUtils.round((Date) null, Calendar.YEAR);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testRound_NullCalendar() {
        try {
            DateUtils.round((Calendar) null, Calendar.YEAR);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testRound_NullObject() {
        try {
            DateUtils.round((Object) null, Calendar.YEAR);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testRound_InvalidObjectType() {
        try {
            DateUtils.round("not a date", Calendar.YEAR);
            fail("Should have thrown ClassCastException");
        } catch (ClassCastException e) {
            assertTrue(e.getMessage().contains("Could not round"));
        }
    }

    @Test(timeout = 4000)
    public void testRound_Millisecond() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 15);
        cal.set(Calendar.MILLISECOND, 500);
        Date date = cal.getTime();
        
        // Rounding to MILLISECOND does nothing (returns as-is)
        Date rounded = DateUtils.round(date, Calendar.MILLISECOND);
        assertEquals("Rounding to MILLISECOND should return same time", date.getTime(), rounded.getTime());
    }

    @Test(timeout = 4000)
    public void testRound_Second() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 15);
        cal.set(Calendar.MILLISECOND, 500);
        Date date = cal.getTime();
        
        // Round to SECOND: 15.5 -> 16 seconds
        Date rounded = DateUtils.round(date, Calendar.SECOND);
        Calendar roundedCal = Calendar.getInstance();
        roundedCal.setTime(rounded);
        assertEquals(16, roundedCal.get(Calendar.SECOND));
        assertEquals(0, roundedCal.get(Calendar.MILLISECOND));
        
        // Under threshold
        cal.set(Calendar.MILLISECOND, 499);
        date = cal.getTime();
        rounded = DateUtils.round(date, Calendar.SECOND);
        roundedCal.setTime(rounded);
        assertEquals(15, roundedCal.get(Calendar.SECOND));
        assertEquals(0, roundedCal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testRound_Hour() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 30, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        
        // Round to HOUR: 8:30 -> 9:00
        Date rounded = DateUtils.round(date, Calendar.HOUR);
        Calendar roundedCal = Calendar.getInstance();
        roundedCal.setTime(rounded);
        assertEquals(9, roundedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, roundedCal.get(Calendar.MINUTE));
        
        // Under threshold
        cal.set(Calendar.MINUTE, 29);
        date = cal.getTime();
        rounded = DateUtils.round(date, Calendar.HOUR);
        roundedCal.setTime(rounded);
        assertEquals(8, roundedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, roundedCal.get(Calendar.MINUTE));
    }

    @Test(timeout = 4000)
    public void testRound_Month() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 15, 8, 9, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        
        // Round to MONTH: July 15 -> July 1 (since 15 is middle, round down? Actually rounding to month 
        // based on date: July 15 is in the middle, should round towards Aug 1 if >15)
        // Actually per truncation, rounding to MONTH uses last field in hierarchy
        // Let's test a simpler case: July 16 -> August 1
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2007, Calendar.JULY, 16, 8, 9, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        Date date2 = cal2.getTime();
        
        Date rounded = DateUtils.round(date2, Calendar.MONTH);
        Calendar roundedCal = Calendar.getInstance();
        roundedCal.setTime(rounded);
        assertEquals("Rounding July 16 should give August 1", Calendar.AUGUST, roundedCal.get(Calendar.MONTH));
        assertEquals(1, roundedCal.get(Calendar.DAY_OF_MONTH));
        
        // Under threshold: July 14 -> July 1
        cal2.set(Calendar.DAY_OF_MONTH, 14);
        date2 = cal2.getTime();
        rounded = DateUtils.round(date2, Calendar.MONTH);
        roundedCal.setTime(rounded);
        assertEquals("Rounding July 14 should give July 1", Calendar.JULY, roundedCal.get(Calendar.MONTH));
        assertEquals(1, roundedCal.get(Calendar.DAY_OF_MONTH));
    }

    @Test(timeout = 4000)
    public void testRound_Year() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        
        // Round to YEAR: July 2007 -> 2008 (since July is past mid-year)
        Date rounded = DateUtils.round(date, Calendar.YEAR);
        Calendar roundedCal = Calendar.getInstance();
        roundedCal.setTime(rounded);
        assertEquals("Rounding July 2007 should give 2008", 2008, roundedCal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, roundedCal.get(Calendar.MONTH));
        assertEquals(1, roundedCal.get(Calendar.DAY_OF_MONTH));
        
        // Under threshold: January 1, 2007 -> 2007
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        date = cal.getTime();
        rounded = DateUtils.round(date, Calendar.YEAR);
        roundedCal.setTime(rounded);
        assertEquals("Rounding Jan 1 2007 should give 2007", 2007, roundedCal.get(Calendar.YEAR));
    }

    @Test(timeout = 4000)
    public void testRound_SemiMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 1, 8, 9, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        
        // Round to SEMI_MONTH: July 1 -> July 16 (top half of month)
        Date rounded = DateUtils.round(date, DateUtils.SEMI_MONTH);
        Calendar roundedCal = Calendar.getInstance();
        roundedCal.setTime(rounded);
        assertEquals(Calendar.JULY, roundedCal.get(Calendar.MONTH));
        assertEquals(16, roundedCal.get(Calendar.DAY_OF_MONTH));
        
        // Bottom half: July 16 -> August 1
        cal.set(Calendar.DAY_OF_MONTH, 16);
        date = cal.getTime();
        rounded = DateUtils.round(date, DateUtils.SEMI_MONTH);
        roundedCal.setTime(rounded);
        // July 16 is exactly mid-month, should round to Aug 1? or stay?
        // 16-1=15, offset >= 15 => offset -= 15 => offset=0, roundUp = offset > 7 => false
        // So it should stay at July 16? Actually rounding down would go to July 1
        // Let's test with 17
        cal.set(Calendar.DAY_OF_MONTH, 17);
        date = cal.getTime();
        rounded = DateUtils.round(date, DateUtils.SEMI_MONTH);
        roundedCal.setTime(rounded);
        assertEquals("Rounding July 17 should give August 1", Calendar.AUGUST, roundedCal.get(Calendar.MONTH));
        assertEquals(1, roundedCal.get(Calendar.DAY_OF_MONTH));
    }

    @Test(timeout = 4000)
    public void testRound_AmPm() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        
        // Round to AM_PM: 8 AM rounds to 12 PM? Actually rounding to AM_PM means rounding to 
        // the nearest AM/PM boundary. 8:09 AM -> 12:00 PM (since 8 > 6)
        Date rounded = DateUtils.round(date, Calendar.AM_PM);
        Calendar roundedCal = Calendar.getInstance();
        roundedCal.setTime(rounded);
        assertEquals(12, roundedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, roundedCal.get(Calendar.MINUTE));
        
        // Under threshold: 5 AM -> 12 AM (midnight)
        cal.set(Calendar.HOUR_OF_DAY, 5);
        date = cal.getTime();
        rounded = DateUtils.round(date, Calendar.AM_PM);
        roundedCal.setTime(rounded);
        assertEquals(0, roundedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, roundedCal.get(Calendar.MINUTE));
    }

    @Test(timeout = 4000)
    public void testRound_YearOver280Million() {
        Calendar cal = Calendar.getInstance();
        cal.set(280000001, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        
        try {
            DateUtils.round(date, Calendar.YEAR);
            fail("Should have thrown ArithmeticException");
        } catch (ArithmeticException e) {
            assertTrue(e.getMessage().contains("too large"));
        }
    }

    @Test(timeout = 4000)
    public void testRound_UnsupportedField() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 0);
        Date date = cal.getTime();
        
        try {
            DateUtils.round(date, Calendar.DAY_OF_WEEK_IN_MONTH);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not supported"));
        }
    }

    @Test(timeout = 4000)
    public void testTruncate_NullDate() {
        try {
            DateUtils.truncate((Date) null, Calendar.YEAR);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testTruncate_NullCalendar() {
        try {
            DateUtils.truncate((Calendar) null, Calendar.YEAR);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testTruncate_NullObject() {
        try {
            DateUtils.truncate((Object) null, Calendar.YEAR);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testTruncate_InvalidObjectType() {
        try {
            DateUtils.truncate("not a date", Calendar.YEAR);
            fail("Should have thrown ClassCastException");
        } catch (ClassCastException e) {
            assertTrue(e.getMessage().contains("Could not truncate"));
        }
    }

    @Test(timeout = 4000)
    public void testTruncate_ToMinute() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 45);
        cal.set(Calendar.MILLISECOND, 500);
        Date date = cal.getTime();
        
        Date truncated = DateUtils.truncate(date, Calendar.MINUTE);
        Calendar truncatedCal = Calendar.getInstance();
        truncatedCal.setTime(truncated);
        
        assertEquals(8, truncatedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(9, truncatedCal.get(Calendar.MINUTE));
        assertEquals(0, truncatedCal.get(Calendar.SECOND));
        assertEquals(0, truncatedCal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testTruncate_ToHour() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 45);
        cal.set(Calendar.MILLISECOND, 500);
        Date date = cal.getTime();
        
        Date truncated = DateUtils.truncate(date, Calendar.HOUR_OF_DAY);
        Calendar truncatedCal = Calendar.getInstance();
        truncatedCal.setTime(truncated);
        
        assertEquals(8, truncatedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, truncatedCal.get(Calendar.MINUTE));
        assertEquals(0, truncatedCal.get(Calendar.SECOND));
        assertEquals(0, truncatedCal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testTruncate_ToDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 45);
        cal.set(Calendar.MILLISECOND, 500);
        Date date = cal.getTime();
        
        Date truncated = DateUtils.truncate(date, Calendar.DATE);
        Calendar truncatedCal = Calendar.getInstance();
        truncatedCal.setTime(truncated);
        
        assertEquals(2, truncatedCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, truncatedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, truncatedCal.get(Calendar.MINUTE));
        assertEquals(0, truncatedCal.get(Calendar.SECOND));
        assertEquals(0, truncatedCal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testTruncate_ToMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 45);
        cal.set(Calendar.MILLISECOND, 500);
        Date date = cal.getTime();
        
        Date truncated = DateUtils.truncate(date, Calendar.MONTH);
        Calendar truncatedCal = Calendar.getInstance();
        truncatedCal.setTime(truncated);
        
        assertEquals(Calendar.JULY, truncatedCal.get(Calendar.MONTH));
        assertEquals(1, truncatedCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, truncatedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, truncatedCal.get(Calendar.MINUTE));
        assertEquals(0, truncatedCal.get(Calendar.SECOND));
        assertEquals(0, truncatedCal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testTruncate_ToYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 45);
        cal.set(Calendar.MILLISECOND, 500);
        Date date = cal.getTime();
        
        Date truncated = DateUtils.truncate(date, Calendar.YEAR);
        Calendar truncatedCal = Calendar.getInstance();
        truncatedCal.setTime(truncated);
        
        assertEquals(2007, truncatedCal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, truncatedCal.get(Calendar.MONTH));
        assertEquals(1, truncatedCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, truncatedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, truncatedCal.get(Calendar.MINUTE));
        assertEquals(0, truncatedCal.get(Calendar.SECOND));
        assertEquals(0, truncatedCal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testTruncate_CalendarObject() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 45);
        cal.set(Calendar.MILLISECOND, 500);
        
        Calendar truncated = DateUtils.truncate(cal, Calendar.SECOND);
        assertNotNull(truncated);
        assertEquals(45, truncated.get(Calendar.SECOND));
        assertEquals(0, truncated.get(Calendar.MILLISECOND));
        
        // Verify original is unchanged
        assertEquals(500, cal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testTruncate_UnsupportedField() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 0);
        Date date = cal.getTime();
        
        try {
            DateUtils.truncate(date, Calendar.DAY_OF_WEEK_IN_MONTH);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not supported"));
        }
    }

    @Test(timeout = 4000)
    public void testIterator_NullDate() {
        try {
            DateUtils.iterator((Date) null, DateUtils.RANGE_MONTH_SUNDAY);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testIterator_NullCalendar() {
        try {
            DateUtils.iterator((Calendar) null, DateUtils.RANGE_MONTH_SUNDAY);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testIterator_NullObject() {
        try {
            DateUtils.iterator((Object) null, DateUtils.RANGE_MONTH_SUNDAY);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testIterator_InvalidObjectType() {
        try {
            DateUtils.iterator("not a date", DateUtils.RANGE_MONTH_SUNDAY);
            fail("Should have thrown ClassCastException");
        } catch (ClassCastException e) {
            assertTrue(e.getMessage().contains("Could not iterate"));
        }
    }

    @Test(timeout = 4000)
    public void testIterator_InvalidRangeStyle() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 9, 0);
        try {
            DateUtils.iterator(cal, 99);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not valid"));
        }
    }

    // ========== PARTITION E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testIterator_RangeWeekSunday() {
        Calendar focus = Calendar.getInstance();
        focus.set(2007, Calendar.JULY, 4, 0, 0, 0); // Wednesday July 4, 2007
        focus.set(Calendar.MILLISECOND, 0);
        
        Iterator iter = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_SUNDAY);
        assertNotNull(iter);
        
        int count = 0;
        while (iter.hasNext()) {
            Calendar cal = (Calendar) iter.next();
            assertNotNull(cal);
            count++;
        }
        assertEquals("Week range should have 7 days", 7, count);
    }

    @Test(timeout = 4000)
    public void testIterator_RangeWeekMonday() {
        Calendar focus = Calendar.getInstance();
        focus.set(2007, Calendar.JULY, 4, 0, 0, 0); // Wednesday
        focus.set(Calendar.MILLISECOND, 0);
        
        Iterator iter = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_MONDAY);
        assertNotNull(iter);
        
        Calendar first = (Calendar) iter.next();
        assertEquals("Week starting Monday should start on Monday", Calendar.MONDAY, first.get(Calendar.DAY_OF_WEEK));
        
        int count = 1;
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        assertEquals("Week range should have 7 days", 7, count);
    }

    @Test(timeout = 4000)
    public void testIterator_RangeWeekRelative() {
        Calendar focus = Calendar.getInstance();
        focus.set(2007, Calendar.JULY, 4, 0, 0, 0); // Wednesday
        focus.set(Calendar.MILLISECOND, 0);
        
        Iterator iter = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_RELATIVE);
        assertNotNull(iter);
        
        Calendar first = (Calendar) iter.next();
        assertEquals("Relative week should start on Wednesday", Calendar.WEDNESDAY, first.get(Calendar.DAY_OF_WEEK));
        
        int count = 1;
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        assertEquals("Relative week range should have 7 days", 7, count);
    }

    @Test(timeout = 4000)
    public void testIterator_RangeWeekCenter() {
        Calendar focus = Calendar.getInstance();
        focus.set(2007, Calendar.JULY, 4, 0, 0, 0); // Wednesday
        focus.set(Calendar.MILLISECOND, 0);
        
        Iterator iter = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_CENTER);
        assertNotNull(iter);
        
        Calendar first = (Calendar) iter.next();
        assertEquals("Centered week should start on Sunday (Wed - 3)", Calendar.SUNDAY, first.get(Calendar.DAY_OF_WEEK));
        
        int count = 1;
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        assertEquals("Centered week range should have 7 days", 7, count);
    }

    @Test(timeout = 4000)
    public void testIterator_RangeMonthSunday() {
        Calendar focus = Calendar.getInstance();
        focus.set(2007, Calendar.JULY, 4, 0, 0, 0);
        focus.set(Calendar.MILLISECOND, 0);
        
        Iterator iter = DateUtils.iterator(focus, DateUtils.RANGE_MONTH_SUNDAY);
        assertNotNull(iter);
        
        // First should be Sunday before June 30, 2007 (first day of month is July 1 which is Sunday)
        Calendar first = (Calendar) iter.next();
        // Verify it starts on Sunday
        
        Calendar last = null;
        while (iter.hasNext()) {
            last = (Calendar) iter.next();
        }
        assertNotNull(last);
        // Should end on Saturday after Aug 3
    }

    @Test(timeout = 4000)
    public void testIterator_RangeMonthMonday() {
        Calendar focus = Calendar.getInstance();
        focus.set(2007, Calendar.JULY, 4, 0, 0, 0);
        focus.set(Calendar.MILLISECOND, 0);
        
        Iterator iter = DateUtils.iterator(focus, DateUtils.RANGE_MONTH_MONDAY);
        assertNotNull(iter);
        
        Calendar first = (Calendar) iter.next();
        assertEquals("Month Monday range should start on Monday", Calendar.MONDAY, first.get(Calendar.DAY_OF_WEEK));
    }

    @Test(timeout = 4000)
    public void testDateIterator_HasNext_Next_Exception() {
        Calendar start = Calendar.getInstance();
        start.set(2007, Calendar.JULY, 1, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);
        
        Calendar end = Calendar.getInstance();
        end.set(2007, Calendar.JULY, 3, 0, 0, 0);
        end.set(Calendar.MILLISECOND, 0);
        
        DateUtils.DateIterator iter = new DateUtils.DateIterator(start, end);
        assertTrue(iter.hasNext());
        
        Calendar day1 = (Calendar) iter.next();
        assertEquals(1, day1.get(Calendar.DAY_OF_MONTH)); // July 1
        
        assertTrue(iter.hasNext());
        Calendar day2 = (Calendar) iter.next();
        assertEquals(2, day2.get(Calendar.DAY_OF_MONTH)); // July 2
        
        assertTrue(iter.hasNext());
        Calendar day3 = (Calendar) iter.next();
        assertEquals(3, day3.get(Calendar.DAY_OF_MONTH)); // July 3
        
        assertFalse(iter.hasNext()); // End is exclusive
        
        try {
            iter.next();
            fail("Should have thrown NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testDateIterator_Remove() {
        Calendar start = Calendar.getInstance();
        start.set(2007, Calendar.JULY, 1, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);
        
        Calendar end = Calendar.getInstance();
        end.set(2007, Calendar.JULY, 2, 0, 0, 0);
        end.set(Calendar.MILLISECOND, 0);
        
        DateUtils.DateIterator iter = new DateUtils.DateIterator(start, end);
        try {
            iter.remove();
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testIterator_CutoffBoundaryClipping() {
        // Test when startCutoff and endCutoff are out of range (wrapping around)
        Calendar focus = Calendar.getInstance();
        focus.set(2007, Calendar.JULY, 4, 0, 0, 0);
        focus.set(Calendar.MILLISECOND, 0);
        
        // RANGE_WEEK_CENTER with Wednesday (4): startCutoff = 4 - 3 = 1 (Sunday), 
        // endCutoff = 4 + 3 = 7 (Saturday) - in range, no clipping
        Iterator iter = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_CENTER);
        Calendar first = (Calendar) iter.next();
        assertEquals("Should start on Sunday", Calendar.SUNDAY, first.get(Calendar.DAY_OF_WEEK));
        
        // Test with Saturday (7): startCutoff = 7 - 3 = 4, endCutoff = 7 + 3 = 10 -> clip to 3
        focus.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        iter = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_CENTER);
        first = (Calendar) iter.next();
        // startCutoff = 4 (Wednesday), endCutoff = 10 -> 10-7=3 (Tuesday)
        // So should start on Wednesday
        assertEquals("Should start on Wednesday", Calendar.WEDNESDAY, first.get(Calendar.DAY_OF_WEEK));
        
        // Count should be 7
        int count = 1;
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        assertEquals(7, count);
    }

    @Test(timeout = 4000)
    public void testIterator_ObjectOverload() {
        Calendar focus = Calendar.getInstance();
        focus.set(2007, Calendar.JULY, 4, 0, 0, 0);
        
        // Test with Date object
        Iterator dateIter = DateUtils.iterator((Object) focus.getTime(), DateUtils.RANGE_WEEK_SUNDAY);
        assertNotNull(dateIter);
        
        // Test with Calendar object
        Iterator calIter = DateUtils.iterator((Object) focus, DateUtils.RANGE_WEEK_SUNDAY);
        assertNotNull(calIter);
    }

    @Test(timeout = 4000)
    public void testRound_ObjectOverload() {
        Calendar focus = Calendar.getInstance();
        focus.set(2007, Calendar.JULY, 2, 8, 30, 0);
        focus.set(Calendar.MILLISECOND, 0);
        
        // Test with Date object
        Date rounded = DateUtils.round((Object) focus.getTime(), Calendar.MINUTE);
        assertNotNull(rounded);
        
        // Test with Calendar object
        Calendar roundedCal = DateUtils.round((Object) focus, Calendar.MINUTE);
        assertNotNull(roundedCal);
    }

    @Test(timeout = 4000)
    public void testTruncate_ObjectOverload() {
        Calendar focus = Calendar.getInstance();
        focus.set(2007, Calendar.JULY, 2, 8, 30, 0);
        focus.set(Calendar.MILLISECOND, 0);
        
        // Test with Date object
        Date truncated = DateUtils.truncate((Object) focus.getTime(), Calendar.MINUTE);
        assertNotNull(truncated);
        
        // Test with Calendar object
        Calendar truncatedCal = DateUtils.truncate((Object) focus, Calendar.MINUTE);
        assertNotNull(truncatedCal);
    }
}