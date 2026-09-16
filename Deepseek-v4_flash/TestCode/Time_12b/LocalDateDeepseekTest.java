package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructors (default, long, Object, int-based)
 *   - getYear(), getMonthOfYear(), getDayOfMonth()
 *   - plus/minus years/months/weeks/days
 *   - withField(), withFields(), with period methods
 *   - toDateTimeAtStartOfDay(), toDate()
 * 
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - Year 0, negative years, year 292277000 (MAX)
 *   - Month boundaries: 1, 12, 0, 13
 *   - Day boundaries: 1, 28, 29, 30, 31, 0, 32
 *   - Leap year February 29
 *   - Max/min plus operations
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - fromCalendarFields() and fromDateFields() for years before year zero
 *   - The bug: for years before year zero (BC), the Calendar/Date era handling
 *     is incorrect. For example, year 1 BC (year 0 in proleptic year) becomes year 1
 *     instead of year 0, and year 2 BC becomes year 3 instead of year -2.
 *     Root cause: Calendar.get(Calendar.YEAR) returns year in era, and for BC era
 *     the year is 1-based (year 1 BC = year 0 proleptic). The code doesn't account
 *     for this, so we need to test that fromCalendarFields/fromDateFields correctly
 *     transforms BC years.
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Null arguments to fromCalendarFields/fromDateFields
 *   - Null field types to get(), withField()
 *   - Unsupported field types
 *   - Invalid index to getValue(), getField()
 *   - Null time to toLocalDateTime()
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - equals() for same/different dates
 *   - hashCode() consistency
 *   - compareTo() ordering
 *   - toString() ISO format
 */
public class LocalDateDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================
    
    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        // Cannot assert exact value, but can assert it doesn't throw
        LocalDate date = new LocalDate();
        assertNotNull(date);
        assertEquals(3, date.size());
    }

    @Test(timeout = 4000)
    public void testIntConstructor() {
        LocalDate date = new LocalDate(2023, 5, 15);
        assertEquals(2023, date.getYear());
        assertEquals(5, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testLongConstructor() {
        // 2023-05-15 in millis
        LocalDate date = new LocalDate(1684108800000L);
        assertEquals(2023, date.getYear());
        assertEquals(5, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testGetYearMonthDay() {
        LocalDate date = new LocalDate(2000, 12, 31);
        assertEquals(2000, date.getYear());
        assertEquals(12, date.getMonthOfYear());
        assertEquals(31, date.getDayOfMonth());
        assertEquals(31, date.getDayOfYear());
        assertEquals(7, date.getDayOfWeek()); // Sunday
    }

    @Test(timeout = 4000)
    public void testPlusYears() {
        LocalDate date = new LocalDate(2020, 2, 29);
        LocalDate result = date.plusYears(1);
        assertEquals(2021, result.getYear());
        assertEquals(2, result.getMonthOfYear());
        assertEquals(28, result.getDayOfMonth()); // Feb 28 for non-leap year
    }

    @Test(timeout = 4000)
    public void testPlusMonths() {
        LocalDate date = new LocalDate(2023, 1, 31);
        LocalDate result = date.plusMonths(1);
        assertEquals(2023, result.getYear());
        assertEquals(2, result.getMonthOfYear());
        assertEquals(28, result.getDayOfMonth()); // Last valid day
    }

    @Test(timeout = 4000)
    public void testPlusWeeks() {
        LocalDate date = new LocalDate(2023, 1, 1);
        LocalDate result = date.plusWeeks(2);
        assertEquals(2023, result.getYear());
        assertEquals(1, result.getMonthOfYear());
        assertEquals(15, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testPlusDays() {
        LocalDate date = new LocalDate(2023, 12, 31);
        LocalDate result = date.plusDays(1);
        assertEquals(2024, result.getYear());
        assertEquals(1, result.getMonthOfYear());
        assertEquals(1, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testMinusYears() {
        LocalDate date = new LocalDate(2020, 2, 29);
        LocalDate result = date.minusYears(1);
        assertEquals(2019, result.getYear());
        assertEquals(2, result.getMonthOfYear());
        assertEquals(28, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testMinusMonths() {
        LocalDate date = new LocalDate(2023, 3, 31);
        LocalDate result = date.minusMonths(1);
        assertEquals(2023, result.getYear());
        assertEquals(2, result.getMonthOfYear());
        assertEquals(28, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testMinusDays() {
        LocalDate date = new LocalDate(2024, 1, 1);
        LocalDate result = date.minusDays(1);
        assertEquals(2023, result.getYear());
        assertEquals(12, result.getMonthOfYear());
        assertEquals(31, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testWithField() {
        LocalDate date = new LocalDate(2023, 5, 15);
        LocalDate result = date.withField(DateTimeFieldType.year(), 2020);
        assertEquals(2020, result.getYear());
        assertEquals(5, result.getMonthOfYear());
        assertEquals(15, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testWithYear() {
        LocalDate date = new LocalDate(2023, 2, 28);
        LocalDate result = date.withYear(2020);
        assertEquals(2020, result.getYear());
        assertEquals(2, result.getMonthOfYear());
        assertEquals(28, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testWithMonthOfYear() {
        LocalDate date = new LocalDate(2023, 5, 31);
        LocalDate result = date.withMonthOfYear(2);
        assertEquals(2023, result.getYear());
        assertEquals(2, result.getMonthOfYear());
        assertEquals(28, result.getDayOfMonth()); // Last valid day
    }

    @Test(timeout = 4000)
    public void testWithDayOfMonth() {
        LocalDate date = new LocalDate(2023, 2, 15);
        LocalDate result = date.withDayOfMonth(28);
        assertEquals(2023, result.getYear());
        assertEquals(2, result.getMonthOfYear());
        assertEquals(28, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testToDateTimeAtStartOfDay() {
        LocalDate date = new LocalDate(2023, 5, 15);
        DateTime dt = date.toDateTimeAtStartOfDay();
        assertEquals(2023, dt.getYear());
        assertEquals(5, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(0, dt.getHourOfDay());
        assertEquals(0, dt.getMinuteOfHour());
    }

    @Test(timeout = 4000)
    public void testToDate() {
        LocalDate date = new LocalDate(2023, 5, 15);
        Date javaDate = date.toDate();
        LocalDate recovered = LocalDate.fromDateFields(javaDate);
        assertEquals(date, recovered);
    }

    // ==================== Partition B: Boundary Value Analysis ====================
    
    @Test(timeout = 4000)
    public void testLeapYearFebruary29() {
        LocalDate date = new LocalDate(2024, 2, 29);
        assertEquals(2024, date.getYear());
        assertEquals(2, date.getMonthOfYear());
        assertEquals(29, date.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testNonLeapYearFebruary29Throws() {
        // The constructor with int year, month, day should validate
        try {
            new LocalDate(2023, 2, 29);
            fail("Expected IllegalArgumentException for invalid date");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testMonthBoundaryMin() {
        LocalDate date = new LocalDate(2023, 1, 1);
        assertEquals(1, date.getMonthOfYear());
        assertEquals(1, date.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testMonthBoundaryMax() {
        LocalDate date = new LocalDate(2023, 12, 31);
        assertEquals(12, date.getMonthOfYear());
        assertEquals(31, date.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testInvalidMonthZero() {
        try {
            new LocalDate(2023, 0, 1);
            fail("Expected IllegalArgumentException for month 0");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testInvalidMonthThirteen() {
        try {
            new LocalDate(2023, 13, 1);
            fail("Expected IllegalArgumentException for month 13");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testInvalidDayZero() {
        try {
            new LocalDate(2023, 1, 0);
            fail("Expected IllegalArgumentException for day 0");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testYearZero() {
        // Year 0 is valid in proleptic year numbering
        LocalDate date = new LocalDate(0, 1, 1);
        assertEquals(0, date.getYear());
        assertEquals(1, date.getMonthOfYear());
        assertEquals(1, date.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testNegativeYear() {
        LocalDate date = new LocalDate(-1, 6, 15);
        assertEquals(-1, date.getYear());
        assertEquals(6, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testPositiveExtremeYear() {
        LocalDate date = new LocalDate(292277000, 1, 1);
        assertEquals(292277000, date.getYear());
        assertEquals(1, date.getMonthOfYear());
        assertEquals(1, date.getDayOfMonth());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    @Test(timeout = 4000)
    public void testFromCalendarFields_beforeYearZero1_yearZero() {
        // Year 1 BC in GregorianCalendar -> proleptic year 0
        // Bug: current implementation returns year 1 instead of year 0
        Calendar cal = new GregorianCalendar(1, Calendar.FEBRUARY, 3);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        // Calendar.YEAR will be 1 for year 1 BC
        LocalDate date = LocalDate.fromCalendarFields(cal);
        assertEquals(0, date.getYear()); // Year 0 in proleptic
        assertEquals(2, date.getMonthOfYear());
        assertEquals(3, date.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testFromCalendarFields_beforeYearZero3_yearMinus2() {
        // Year 3 BC in GregorianCalendar -> proleptic year -2
        // Bug: current implementation returns year 3 instead of year -2
        Calendar cal = new GregorianCalendar(3, Calendar.FEBRUARY, 3);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        LocalDate date = LocalDate.fromCalendarFields(cal);
        assertEquals(-2, date.getYear()); // Year -2 in proleptic
        assertEquals(2, date.getMonthOfYear());
        assertEquals(3, date.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testFromDateFields_beforeYearZero1_yearZero() {
        // Year 1 BC in Date -> proleptic year 0
        // Date uses year - 1900, so year 1 BC is year -1899 in Date API
        @SuppressWarnings("deprecation")
        Date date = new Date(-1899, Calendar.FEBRUARY, 3); // year 1 BC = -1899 + 1900 = 1, but era handling...
        LocalDate localDate = LocalDate.fromDateFields(date);
        // The current implementation doesn't handle BC dates correctly in fromDateFields
        // This should be year 0 in proleptic
        assertEquals(0, localDate.getYear());
        assertEquals(2, localDate.getMonthOfYear());
        assertEquals(3, localDate.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testFromDateFields_beforeYearZero3_yearMinus2() {
        // Year 3 BC in Date -> proleptic year -2
        @SuppressWarnings("deprecation")
        Date date = new Date(-1897, Calendar.FEBRUARY, 3); // year 3 BC
        LocalDate localDate = LocalDate.fromDateFields(date);
        // This should be year -2 in proleptic
        assertEquals(-2, localDate.getYear());
        assertEquals(2, localDate.getMonthOfYear());
        assertEquals(3, localDate.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testFromCalendarFields_yearZeroProleptic() {
        // Test with year 0 in proleptic calendar (should be 1 BC in Gregorian)
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, 1);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 3);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        // This is year 1 BC = proleptic year 0
        LocalDate date = LocalDate.fromCalendarFields(cal);
        assertEquals(0, date.getYear());
        assertEquals(2, date.getMonthOfYear());
        assertEquals(3, date.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testFromCalendarFields_positiveYear() {
        Calendar cal = new GregorianCalendar(2023, Calendar.MAY, 15);
        LocalDate date = LocalDate.fromCalendarFields(cal);
        assertEquals(2023, date.getYear());
        assertEquals(5, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFromCalendarFields_null() {
        LocalDate.fromCalendarFields(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFromDateFields_null() {
        LocalDate.fromDateFields(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetFieldType_null() {
        LocalDate date = new LocalDate(2023, 1, 1);
        date.get(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetFieldType_unsupported() {
        LocalDate date = new LocalDate(2023, 1, 1);
        date.get(DateTimeFieldType.hourOfDay());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithField_nullFieldType() {
        LocalDate date = new LocalDate(2023, 1, 1);
        date.withField(null, 5);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithField_unsupportedFieldType() {
        LocalDate date = new LocalDate(2023, 1, 1);
        date.withField(DateTimeFieldType.hourOfDay(), 5);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetValue_invalidIndexNegative() {
        LocalDate date = new LocalDate(2023, 1, 1);
        date.getValue(-1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetValue_invalidIndexTooHigh() {
        LocalDate date = new LocalDate(2023, 1, 1);
        date.getValue(3);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToLocalDateTime_nullTime() {
        LocalDate date = new LocalDate(2023, 1, 1);
        date.toLocalDateTime(null);
    }

    @Test(timeout = 4000)
    public void testIsSupported_nullFieldType() {
        LocalDate date = new LocalDate(2023, 1, 1);
        assertFalse(date.isSupported((DateTimeFieldType) null));
    }

    @Test(timeout = 4000)
    public void testIsSupported_nullDurationType() {
        LocalDate date = new LocalDate(2023, 1, 1);
        assertFalse(date.isSupported((DurationFieldType) null));
    }

    @Test(timeout = 4000)
    public void testIsSupported_unsupportedField() {
        LocalDate date = new LocalDate(2023, 1, 1);
        assertFalse(date.isSupported(DateTimeFieldType.hourOfDay()));
    }

    @Test(timeout = 4000)
    public void testProperty_nullFieldType() {
        LocalDate date = new LocalDate(2023, 1, 1);
        try {
            date.property(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testEquals_same() {
        LocalDate date1 = new LocalDate(2023, 5, 15);
        LocalDate date2 = new LocalDate(2023, 5, 15);
        assertTrue(date1.equals(date2));
        assertTrue(date2.equals(date1));
    }

    @Test(timeout = 4000)
    public void testEquals_different() {
        LocalDate date1 = new LocalDate(2023, 5, 15);
        LocalDate date2 = new LocalDate(2023, 5, 16);
        assertFalse(date1.equals(date2));
    }

    @Test(timeout = 4000)
    public void testEquals_differentYear() {
        LocalDate date1 = new LocalDate(2023, 5, 15);
        LocalDate date2 = new LocalDate(2024, 5, 15);
        assertFalse(date1.equals(date2));
    }

    @Test(timeout = 4000)
    public void testEquals_sameObject() {
        LocalDate date = new LocalDate(2023, 5, 15);
        assertTrue(date.equals(date));
    }

    @Test(timeout = 4000)
    public void testHashCode_consistent() {
        LocalDate date = new LocalDate(2023, 5, 15);
        int hash1 = date.hashCode();
        int hash2 = date.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testHashCode_equals() {
        LocalDate date1 = new LocalDate(2023, 5, 15);
        LocalDate date2 = new LocalDate(2023, 5, 15);
        assertEquals(date1.hashCode(), date2.hashCode());
    }

    @Test(timeout = 4000)
    public void testCompareTo_equal() {
        LocalDate date1 = new LocalDate(2023, 5, 15);
        LocalDate date2 = new LocalDate(2023, 5, 15);
        assertEquals(0, date1.compareTo(date2));
    }

    @Test(timeout = 4000)
    public void testCompareTo_less() {
        LocalDate date1 = new LocalDate(2023, 5, 14);
        LocalDate date2 = new LocalDate(2023, 5, 15);
        assertTrue(date1.compareTo(date2) < 0);
    }

    @Test(timeout = 4000)
    public void testCompareTo_greater() {
        LocalDate date1 = new LocalDate(2023, 5, 16);
        LocalDate date2 = new LocalDate(2023, 5, 15);
        assertTrue(date1.compareTo(date2) > 0);
    }

    @Test(timeout = 4000)
    public void testToStringISOFormat() {
        LocalDate date = new LocalDate(2023, 5, 15);
        assertEquals("2023-05-15", date.toString());
    }

    @Test(timeout = 4000)
    public void testSize() {
        LocalDate date = new LocalDate(2023, 1, 1);
        assertEquals(3, date.size());
    }

    @Test(timeout = 4000)
    public void testGetValue_validIndices() {
        LocalDate date = new LocalDate(2023, 5, 15);
        assertEquals(2023, date.getValue(0)); // YEAR
        assertEquals(5, date.getValue(1));    // MONTH_OF_YEAR
        assertEquals(15, date.getValue(2));   // DAY_OF_MONTH
    }

    @Test(timeout = 4000)
    public void testWithEra() {
        LocalDate date = new LocalDate(2023, 5, 15);
        LocalDate result = date.withEra(0); // BCE
        assertEquals(2023, result.getYear());
        assertEquals(5, result.getMonthOfYear());
        assertEquals(15, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testWithDayOfYear() {
        LocalDate date = new LocalDate(2023, 1, 1);
        LocalDate result = date.withDayOfYear(135); // May 15, 2023
        assertEquals(2023, result.getYear());
        assertEquals(5, result.getMonthOfYear());
        assertEquals(15, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testPropertyRoundFloorCopy() {
        LocalDate date = new LocalDate(2023, 5, 15);
        LocalDate result = date.dayOfMonth().roundFloorCopy();
        assertEquals(date, result); // Already at floor for day
    }

    @Test(timeout = 4000)
    public void testWithFields_null() {
        LocalDate date = new LocalDate(2023, 5, 15);
        LocalDate result = date.withFields(null);
        assertSame(date, result);
    }

    @Test(timeout = 4000)
    public void testWithPeriodAdded_null() {
        LocalDate date = new LocalDate(2023, 5, 15);
        LocalDate result = date.withPeriodAdded(null, 1);
        assertSame(date, result);
    }

    @Test(timeout = 4000)
    public void testWithPeriodAdded_zeroScalar() {
        LocalDate date = new LocalDate(2023, 5, 15);
        Period period = Period.days(5);
        LocalDate result = date.withPeriodAdded(period, 0);
        assertSame(date, result);
    }

    @Test(timeout = 4000)
    public void testWithLocalMillis_unchanged() {
        LocalDate date = new LocalDate(2023, 5, 15);
        // Same millis should return same instance
        LocalDate result = date.withLocalMillis(date.getLocalMillis());
        assertSame(date, result);
    }

    @Test(timeout = 4000)
    public void testReadResolve_nullChronology() {
        // Test readResolve indirectly via construction
        LocalDate date = new LocalDate(2023, 5, 15, (Chronology) null);
        assertNotNull(date.getChronology());
    }

    @Test(timeout = 4000)
    public void testToInterval() {
        LocalDate date = new LocalDate(2023, 5, 15);
        Interval interval = date.toInterval();
        DateTime start = interval.getStart();
        DateTime end = interval.getEnd();
        assertEquals(2023, start.getYear());
        assertEquals(5, start.getMonthOfYear());
        assertEquals(15, start.getDayOfMonth());
        assertEquals(2023, end.getYear());
        assertEquals(5, end.getMonthOfYear());
        assertEquals(16, end.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testToDateTimeAtMidnight() {
        LocalDate date = new LocalDate(2023, 5, 15);
        DateTime dt = date.toDateTimeAtMidnight();
        assertEquals(2023, dt.getYear());
        assertEquals(5, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(0, dt.getHourOfDay());
        assertEquals(0, dt.getMinuteOfHour());
    }

    @Test(timeout = 4000)
    public void testToDateMidnight() {
        LocalDate date = new LocalDate(2023, 5, 15);
        DateMidnight dm = date.toDateMidnight();
        assertEquals(2023, dm.getYear());
        assertEquals(5, dm.getMonthOfYear());
        assertEquals(15, dm.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testPropertyGetField() {
        LocalDate date = new LocalDate(2023, 5, 15);
        assertEquals(DateTimeFieldType.year(), date.year().getField().getType());
        assertEquals(DateTimeFieldType.monthOfYear(), date.monthOfYear().getField().getType());
        assertEquals(DateTimeFieldType.dayOfMonth(), date.dayOfMonth().getField().getType());
    }

    @Test(timeout = 4000)
    public void testPropertySetCopy() {
        LocalDate date = new LocalDate(2023, 5, 15);
        LocalDate result = date.year().setCopy(2020);
        assertEquals(2020, result.getYear());
        assertEquals(5, result.getMonthOfYear());
        assertEquals(15, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testPropertyAddToCopy() {
        LocalDate date = new LocalDate(2023, 5, 15);
        LocalDate result = date.monthOfYear().addToCopy(3);
        assertEquals(2023, result.getYear());
        assertEquals(8, result.getMonthOfYear());
        assertEquals(15, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testPropertyAddWrapFieldToCopy() {
        LocalDate date = new LocalDate(2023, 5, 15);
        LocalDate result = date.monthOfYear().addWrapFieldToCopy(10);
        // 5 + 10 = 15, wrap to 3
        assertEquals(2023, result.getYear());
        assertEquals(3, result.getMonthOfYear());
        assertEquals(15, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testPropertyWithMaximumValue() {
        LocalDate date = new LocalDate(2023, 2, 1);
        LocalDate result = date.dayOfMonth().withMaximumValue();
        assertEquals(2023, result.getYear());
        assertEquals(2, result.getMonthOfYear());
        assertEquals(28, result.getDayOfMonth()); // Feb max
    }

    @Test(timeout = 4000)
    public void testPropertyWithMinimumValue() {
        LocalDate date = new LocalDate(2023, 2, 15);
        LocalDate result = date.dayOfMonth().withMinimumValue();
        assertEquals(2023, result.getYear());
        assertEquals(2, result.getMonthOfYear());
        assertEquals(1, result.getDayOfMonth());
    }

    @Test(timeout = 4000)
    public void testGetWeekOfWeekyear() {
        LocalDate date = new LocalDate(2023, 1, 1); // Sunday
        int week = date.getWeekOfWeekyear();
        assertTrue(week >= 1 && week <= 53);
    }

    @Test(timeout = 4000)
    public void testGetWeekyear() {
        LocalDate date = new LocalDate(2023, 12, 31); // Sunday, ISO weekyear may be 2024
        int weekyear = date.getWeekyear();
        assertTrue(weekyear == 2023 || weekyear == 2024);
    }

    @Test(timeout = 4000)
    public void testToStringWithPattern() {
        LocalDate date = new LocalDate(2023, 5, 15);
        String result = date.toString("yyyy/MM/dd");
        assertEquals("2023/05/15", result);
    }

    @Test(timeout = 4000)
    public void testToStringWithNullPattern() {
        LocalDate date = new LocalDate(2023, 5, 15);
        assertEquals("2023-05-15", date.toString(null));
    }

    @Test(timeout = 4000)
    public void testGetEra() {
        LocalDate date = new LocalDate(2023, 5, 15);
        assertEquals(1, date.getEra()); // CE
    }

    @Test(timeout = 4000)
    public void testGetCenturyOfEra() {
        LocalDate date = new LocalDate(2023, 5, 15);
        assertEquals(21, date.getCenturyOfEra());
    }

    @Test(timeout = 4000)
    public void testGetYearOfEra() {
        LocalDate date = new LocalDate(2023, 5, 15);
        assertEquals(2023, date.getYearOfEra());
    }

    @Test(timeout = 4000)
    public void testGetYearOfCentury() {
        LocalDate date = new LocalDate(2023, 5, 15);
        assertEquals(23, date.getYearOfCentury());
    }

    @Test(timeout = 4000)
    public void testMinusWeeks() {
        LocalDate date = new LocalDate(2023, 1, 15);
        LocalDate result = date.minusWeeks(2);
        assertEquals(2023, result.getYear());
        assertEquals(1, result.getMonthOfYear());
        assertEquals(1, result.getDayOfMonth());
    }
}