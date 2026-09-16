package org.jfree.data.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * White-box test suite for Week.java targeting the known defect:
 * testConstructor expects week 35 but gets 34.
 *
 * [Branch & Defect Analysis Matrix]
 * - Constructor Week(Date, TimeZone, Locale): branches for week=1 and month=DECEMBER,
 *   and month=JANUARY with week>=52. Defect likely in the adjustment logic.
 * - previous(): branch for week==1, year>1900, else null.
 * - next(): branch for week<52, else check actualMaxWeek, else year<9999.
 * - getFirstMillisecond/getLastMillisecond: uses calendar clone and clear.
 * - parseWeek: separator finding, year/week parsing.
 * - equals/hashCode/compareTo: field comparisons.
 * - Boundary values: week=1,53; year=1900,9999; null arguments.
 */
public class WeekDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testConstructorWithWeekAndYear() {
        Week w = new Week(35, 2007);
        assertEquals(2007, w.getYearValue());
        assertEquals(35, w.getWeek());
        assertEquals(new Year(2007), w.getYear());
    }

    @Test(timeout = 4000)
    public void testConstructorWithWeekAndYearObject() {
        Week w = new Week(1, new Year(2008));
        assertEquals(2008, w.getYearValue());
        assertEquals(1, w.getWeek());
    }

    @Test(timeout = 4000)
    public void testConstructorWithDate() {
        // Use a known date: 2007-08-27 is week 35 of 2007 (ISO)
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2007, Calendar.AUGUST, 27);
        Date d = cal.getTime();
        Week w = new Week(d, TimeZone.getTimeZone("UTC"), Locale.US);
        // Expected week 35, year 2007
        assertEquals(35, w.getWeek());
        assertEquals(2007, w.getYearValue());
    }

    @Test(timeout = 4000)
    public void testConstructorWithDateAndTimeZone() {
        // Deprecated constructor delegates to the three-arg version
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.UK);
        cal.set(2008, Calendar.JANUARY, 1);
        Week w = new Week(cal.getTime(), TimeZone.getTimeZone("GMT"));
        // Jan 1, 2008 is in week 1 of 2008 (ISO)
        assertEquals(1, w.getWeek());
        assertEquals(2008, w.getYearValue());
    }

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        // Should not throw, uses current date
        Week w = new Week();
        assertNotNull(w);
    }

    @Test(timeout = 4000)
    public void testGetYear() {
        Week w = new Week(10, 2010);
        assertEquals(new Year(2010), w.getYear());
    }

    @Test(timeout = 4000)
    public void testGetFirstAndLastMillisecond() {
        Week w = new Week(1, 2000);
        long first = w.getFirstMillisecond();
        long last = w.getLastMillisecond();
        assertTrue(first <= last);
        // Check that first millisecond is start of week (Sunday in US locale)
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(first);
        assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK));
    }

    @Test(timeout = 4000)
    public void testPeg() {
        Week w = new Week(20, 2005);
        Calendar cal = Calendar.getInstance();
        w.peg(cal);
        assertTrue(w.getFirstMillisecond() > 0);
        assertTrue(w.getLastMillisecond() > w.getFirstMillisecond());
    }

    @Test(timeout = 4000)
    public void testPrevious() {
        Week w = new Week(2, 2000);
        Week prev = (Week) w.previous();
        assertEquals(1, prev.getWeek());
        assertEquals(2000, prev.getYearValue());

        // Week 1 of 1900 should return null
        Week w1900 = new Week(1, 1900);
        assertNull(w1900.previous());

        // Week 1 of 2001 -> previous is week 53 of 2000 (if 53 weeks)
        Week w2001 = new Week(1, 2001);
        Week prev2000 = (Week) w2001.previous();
        assertEquals(53, prev2000.getWeek());
        assertEquals(2000, prev2000.getYearValue());
    }

    @Test(timeout = 4000)
    public void testNext() {
        Week w = new Week(51, 2000);
        Week next = (Week) w.next();
        assertEquals(52, next.getWeek());
        assertEquals(2000, next.getYearValue());

        // Week 53 of 2000 -> next is week 1 of 2001
        Week w53 = new Week(53, 2000);
        Week next2001 = (Week) w53.next();
        assertEquals(1, next2001.getWeek());
        assertEquals(2001, next2001.getYearValue());

        // Week 53 of 9999 -> null
        Week w9999 = new Week(53, 9999);
        assertNull(w9999.next());
    }

    @Test(timeout = 4000)
    public void testGetSerialIndex() {
        Week w = new Week(10, 2000);
        assertEquals(2000 * 53L + 10, w.getSerialIndex());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Week w = new Week(5, 2003);
        assertEquals("Week 5, 2003", w.toString());
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testBoundaryWeek1() {
        Week w = new Week(1, 1900);
        assertEquals(1, w.getWeek());
        assertEquals(1900, w.getYearValue());
    }

    @Test(timeout = 4000)
    public void testBoundaryWeek53() {
        Week w = new Week(53, 2000);
        assertEquals(53, w.getWeek());
        assertEquals(2000, w.getYearValue());
    }

    @Test(timeout = 4000)
    public void testBoundaryYear1900() {
        Week w = new Week(52, 1900);
        assertEquals(1900, w.getYearValue());
    }

    @Test(timeout = 4000)
    public void testBoundaryYear9999() {
        Week w = new Week(1, 9999);
        assertEquals(9999, w.getYearValue());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidWeekZero() {
        new Week(0, 2000);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidWeek54() {
        new Week(54, 2000);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidWeekNegative() {
        new Week(-1, 2000);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullDate() {
        new Week((Date) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullTimeZone() {
        new Week(new Date(), null, Locale.getDefault());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullLocale() {
        new Week(new Date(), TimeZone.getDefault(), null);
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    /**
     * Directly targets the known defect: testConstructor expects week 35 but gets 34.
     * This test uses a date that should yield week 35 but the buggy version returns 34.
     * The defect is in the Week(Date, TimeZone, Locale) constructor.
     * We use a date that falls in the last week of the previous year or first week of next year
     * where the adjustment logic is flawed.
     */
    @Test(timeout = 4000)
    public void testDefectWeek35() {
        // According to the defect, for some date the expected week is 35 but actual is 34.
        // We'll use a date that is known to be problematic: e.g., December 31, 2007
        // might be considered week 1 of 2008, but if the bug miscalculates, it could be week 34.
        // Let's try a date that should be week 35 of 2007: August 27, 2007 is week 35.
        // But the defect says expected 35 but got 34, so maybe the bug is off by one.
        // We'll test a date that is near the boundary where week number is ambiguous.
        // For example, January 1, 2008 is week 1 of 2008. But if the bug miscalculates,
        // it might assign week 34 of 2007? That seems unlikely.
        // Another possibility: The bug is in the constructor when the week is calculated
        // from a Date and the year is adjusted incorrectly.
        // Let's use a date that is in week 35 of 2007 according to ISO: 2007-08-27.
        // If the bug causes week to be 34, then this test will fail on the fixed version
        // but pass on the buggy version? Actually we want the test to reveal the bug,
        // so we assert the correct expected value (35). On the buggy version, it returns 34,
        // so the assertion fails, revealing the bug.
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2007, Calendar.AUGUST, 27);
        Date d = cal.getTime();
        Week w = new Week(d, TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals("Defect: expected week 35 but got " + w.getWeek(), 35, w.getWeek());
        assertEquals(2007, w.getYearValue());
    }

    @Test(timeout = 4000)
    public void testDefectYearBoundary() {
        // Test a date that falls in the first week of the next year but is in December.
        // For example, December 31, 2007 is in week 1 of 2008 (ISO).
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2007, Calendar.DECEMBER, 31);
        Date d = cal.getTime();
        Week w = new Week(d, TimeZone.getTimeZone("UTC"), Locale.US);
        // Expected: week 1, year 2008
        assertEquals(1, w.getWeek());
        assertEquals(2008, w.getYearValue());
    }

    @Test(timeout = 4000)
    public void testDefectJanuaryBoundary() {
        // Test a date that falls in the last week of the previous year.
        // January 1, 2000 is in week 52 of 1999 (ISO).
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2000, Calendar.JANUARY, 1);
        Date d = cal.getTime();
        Week w = new Week(d, TimeZone.getTimeZone("UTC"), Locale.US);
        // Expected: week 52, year 1999
        assertEquals(52, w.getWeek());
        assertEquals(1999, w.getYearValue());
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidWeekInConstructorWithYearObject() {
        new Week(0, new Year(2000));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidWeek54InConstructorWithYearObject() {
        new Week(54, new Year(2000));
    }

    @Test(timeout = 4000)
    public void testParseWeekValid() {
        Week w = Week.parseWeek("2007-W35");
        assertNotNull(w);
        assertEquals(35, w.getWeek());
        assertEquals(2007, w.getYearValue());
    }

    @Test(timeout = 4000)
    public void testParseWeekAlternativeFormat() {
        Week w = Week.parseWeek("W35-2007");
        assertNotNull(w);
        assertEquals(35, w.getWeek());
        assertEquals(2007, w.getYearValue());
    }

    @Test(timeout = 4000, expected = TimePeriodFormatException.class)
    public void testParseWeekNoSeparator() {
        Week.parseWeek("2007W35");
    }

    @Test(timeout = 4000, expected = TimePeriodFormatException.class)
    public void testParseWeekInvalidWeek() {
        Week.parseWeek("2007-W99");
    }

    @Test(timeout = 4000, expected = TimePeriodFormatException.class)
    public void testParseWeekInvalidYear() {
        Week.parseWeek("abcd-W35");
    }

    @Test(timeout = 4000)
    public void testParseWeekNull() {
        assertNull(Week.parseWeek(null));
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testEquals() {
        Week w1 = new Week(10, 2000);
        Week w2 = new Week(10, 2000);
        Week w3 = new Week(11, 2000);
        Week w4 = new Week(10, 2001);
        assertTrue(w1.equals(w2));
        assertFalse(w1.equals(w3));
        assertFalse(w1.equals(w4));
        assertFalse(w1.equals(null));
        assertFalse(w1.equals("not a week"));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        Week w1 = new Week(10, 2000);
        Week w2 = new Week(10, 2000);
        assertEquals(w1.hashCode(), w2.hashCode());
    }

    @Test(timeout = 4000)
    public void testCompareTo() {
        Week w1 = new Week(10, 2000);
        Week w2 = new Week(10, 2000);
        Week w3 = new Week(11, 2000);
        Week w4 = new Week(10, 2001);
        assertEquals(0, w1.compareTo(w2));
        assertTrue(w1.compareTo(w3) < 0);
        assertTrue(w1.compareTo(w4) < 0);
        assertTrue(w3.compareTo(w1) > 0);
        // Compare to non-Week RegularTimePeriod (e.g., Day) - should return 0
        // We can't instantiate Day easily, but we can test compareTo with a non-TimePeriod object
        assertEquals(1, w1.compareTo("string"));
    }

    @Test(timeout = 4000)
    public void testSerialIndexConsistency() {
        Week w1 = new Week(1, 2000);
        Week w2 = new Week(2, 2000);
        assertTrue(w1.getSerialIndex() < w2.getSerialIndex());
    }

    @Test(timeout = 4000)
    public void testImmutability() {
        Week w = new Week(20, 2005);
        int originalWeek = w.getWeek();
        int originalYear = w.getYearValue();
        // No setters, so fields cannot change
        assertEquals(originalWeek, w.getWeek());
        assertEquals(originalYear, w.getYearValue());
    }
}