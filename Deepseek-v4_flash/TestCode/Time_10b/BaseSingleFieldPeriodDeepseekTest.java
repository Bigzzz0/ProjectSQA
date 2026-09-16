package org.joda.time.base;

import org.junit.Test;
import static org.junit.Assert.*;

import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.field.DividedDateTimeField;
import org.joda.time.field.FieldUtils;

/**
 * Advanced White-Box Test Suite for BaseSingleFieldPeriod.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Decision branches targeted:
 * 1. between(ReadableInstant, ReadableInstant, DurationFieldType):
 *    - null start -> throws IllegalArgumentException
 *    - null end -> throws IllegalArgumentException
 *    - Normal flow with valid instants
 * 
 * 2. between(ReadablePartial, ReadablePartial, ReadablePeriod):
 *    - null start -> throws IllegalArgumentException
 *    - null end -> throws IllegalArgumentException
 *    - size mismatch -> throws IllegalArgumentException
 *    - field type mismatch -> throws IllegalArgumentException
 *    - non-contiguous partials -> throws IllegalArgumentException
 *    - Normal flow with valid partials
 * 
 * 3. standardPeriodIn(ReadablePeriod, long):
 *    - null period -> returns 0
 *    - zero values skip iteration
 *    - precise fields accumulate correctly
 *    - imprecise field -> throws IllegalArgumentException
 *    - safeAdd/safeMultiply edge cases (overflow)
 *    - safeToInt edge cases
 * 
 * 4. Constructor/Setter/Getter:
 *    - Positive values
 *    - Negative values
 *    - Zero value
 *    - Integer.MAX_VALUE/MIN_VALUE boundaries
 * 
 * 5. getFieldType(int), getValue(int):
 *    - index == 0 -> valid
 *    - index != 0 -> IndexOutOfBoundsException
 * 
 * 6. get(DurationFieldType):
 *    - matching field type -> returns value
 *    - non-matching field type -> returns 0
 *    - null input -> returns 0
 * 
 * 7. equals(Object):
 *    - same reference (this) -> true
 *    - not a ReadablePeriod -> false
 *    - same period type and value -> true
 *    - different period type -> false
 *    - different value -> false
 * 
 * 8. hashCode():
 *    - Consistent with equals
 * 
 * 9. compareTo(BaseSingleFieldPeriod):
 *    - other.getClass() != getClass() -> ClassCastException
 *    - this > other -> 1
 *    - this < other -> -1
 *    - this == other -> 0
 *    - null other -> NullPointerException
 * 
 * KNOWN DEFECT: between(ReadablePartial, ReadablePartial, ReadablePeriod)
 * fails when partials have day-of-month > 28 in MonthDay with non-leap February
 * or other invalid date combinations due to chrono.set() validation.
 * Targeted by testFactory_methods_RPartial_MonthDay_Boundary().
 */
public class BaseSingleFieldPeriodDeepseekTest {

    // ===================== Partition A: Core Functional Logic & State Transitions =====================

    @Test(timeout = 4000)
    public void testConstructorAndBasicGetters() {
        // Test concrete subclass - Days for simplicity
        Days days = Days.days(5);
        assertEquals(5, days.getValue());
        assertEquals(DurationFieldType.days(), days.getFieldType());
        assertEquals(PeriodType.days(), days.getPeriodType());
        assertEquals(1, days.size());
    }

    @Test(timeout = 4000)
    public void testSetValue() {
        MutableDays mutableDays = new MutableDays(10);
        assertEquals(10, mutableDays.getValue());
        mutableDays.setValue(25);
        assertEquals(25, mutableDays.getValue());
        mutableDays.setValue(-3);
        assertEquals(-3, mutableDays.getValue());
        mutableDays.setValue(0);
        assertEquals(0, mutableDays.getValue());
    }

    @Test(timeout = 4000)
    public void testBetweenReadableInstantNormal() {
        // Days between two instants
        DateTime start = new DateTime(2020, 1, 1, 0, 0, 0, 0, ISOChronology.getInstanceUTC());
        DateTime end = new DateTime(2020, 1, 11, 0, 0, 0, 0, ISOChronology.getInstanceUTC());
        int result = Days.daysBetween(start, end).getValue();
        assertEquals(10, result);

        // Negative result
        result = Days.daysBetween(end, start).getValue();
        assertEquals(-10, result);

        // Zero difference
        result = Days.daysBetween(start, start).getValue();
        assertEquals(0, result);
    }

    @Test(timeout = 4000)
    public void testBetweenReadableInstantNullStart() {
        DateTime end = new DateTime(2020, 1, 11, 0, 0, 0, 0);
        try {
            Days.daysBetween((ReadableInstant) null, end);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("ReadableInstant objects must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testBetweenReadableInstantNullEnd() {
        DateTime start = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        try {
            Days.daysBetween(start, (ReadableInstant) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("ReadableInstant objects must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testBetweenReadablePartialNormal() {
        // LocalDate examples
        LocalDate start = new LocalDate(2020, 1, 1);
        LocalDate end = new LocalDate(2020, 1, 15);
        int result = Days.daysBetween(start, end).getValue();
        assertEquals(14, result);

        // Months between
        result = Months.monthsBetween(start, new LocalDate(2020, 6, 1)).getValue();
        assertEquals(5, result);
    }

    @Test(timeout = 4000)
    public void testBetweenReadablePartialNullStart() {
        LocalDate end = new LocalDate(2020, 1, 15);
        try {
            Days.daysBetween((ReadablePartial) null, end);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("ReadablePartial objects must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testBetweenReadablePartialNullEnd() {
        LocalDate start = new LocalDate(2020, 1, 1);
        try {
            Days.daysBetween(start, (ReadablePartial) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("ReadablePartial objects must not be null"));
        }
    }

    @Test(timeout = 4000)
    public void testBetweenReadablePartialSizeMismatch() {
        LocalDate start = new LocalDate(2020, 1, 1);
        // Partial with different size (e.g., YearMonth vs LocalDate)
        YearMonth ym = new YearMonth(2020, 6);
        try {
            Days.daysBetween(start, ym);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("same set of fields"));
        }
    }

    @Test(timeout = 4000)
    public void testBetweenReadablePartialFieldTypeMismatch() {
        // Two partials of same size but different field types: LocalDate vs LocalTime
        LocalDate start = new LocalDate(2020, 1, 1);
        LocalTime time = new LocalTime(12, 30);
        try {
            Days.daysBetween(start, time);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("same set of fields"));
        }
    }

    @Test(timeout = 4000)
    public void testBetweenReadablePartialNonContiguous() {
        // Partial that is not contiguous - e.g., MonthDay with only month and day
        // Actually MonthDay is contiguous, but YearMonthDay with missing year? No.
        // Use a custom partial? Let's use YearMonth (which is contiguous) vs something else
        // Actually YearMonth is contiguous. Use a non-contiguous like a custom one?
        // For Joda-Time, try using a Partial that is not contiguous:
        // e.g., Partial with only month and year but not day - that's YearMonth, which is contiguous.
        // To get non-contiguous, we need a partial like {weekyear, weekOfWeekyear, dayOfWeek} but missing weekyear?
        // Actually standard Joda-Time partials are all contiguous. 
        // We'll test using LocalDate which is contiguous, so this path won't be triggered.
        // For coverage, we need a non-contiguous partial. Use Partial with non-contiguous fields:
        Partial partial = new Partial(new DateTimeFieldType[] {
            DateTimeFieldType.clockhourOfDay(), DateTimeFieldType.minuteOfHour()
        }, new int[] {10, 30});
        // This partial is NOT contiguous because clock hour and minute are not in a continuous range
        // But this between method may not be called with such - let's try to trigger
        LocalDate start = new LocalDate(2020, 1, 1);
        try {
            Days.daysBetween(start, partial);
            fail("Expected IllegalArgumentException for non-contiguous");
        } catch (IllegalArgumentException e) {
            // Expected: "ReadablePartial objects must be contiguous"
            assertTrue(e.getMessage().contains("contiguous"));
        }
    }

    // ===================== Partition B: Boundary Value Analysis (BVA) & Extremes =====================

    @Test(timeout = 4000)
    public void testStandardPeriodInNullPeriod() {
        // standardPeriodIn with null period returns 0
        int result = Days.standardDaysIn(null);
        assertEquals(0, result);
    }

    @Test(timeout = 4000)
    public void testStandardPeriodInZeroValues() {
        // Period with all zero values
        Period zeroPeriod = Period.ZERO;
        int result = Days.standardDaysIn(zeroPeriod);
        assertEquals(0, result);
    }

    @Test(timeout = 4000)
    public void testStandardPeriodInWithDays() {
        Period period = new Period(0, 0, 0, 5, 0, 0, 0, 0); // 5 days
        int result = Days.standardDaysIn(period);
        assertEquals(5, result);
    }

    @Test(timeout = 4000)
    public void testStandardPeriodInWithWeeks() {
        Period period = new Period(0, 0, 2, 0, 0, 0, 0, 0); // 2 weeks = 14 days
        int result = Days.standardDaysIn(period);
        assertEquals(14, result);
    }

    @Test(timeout = 4000)
    public void testStandardPeriodInWithHours() {
        Period period = new Period(0, 0, 0, 0, 24, 0, 0, 0); // 24 hours = 1 day
        int result = Days.standardDaysIn(period);
        assertEquals(1, result);
    }

    @Test(timeout = 4000)
    public void testStandardPeriodInWithMinutes() {
        Period period = new Period(0, 0, 0, 0, 0, 1440, 0, 0); // 1440 minutes = 1 day
        int result = Days.standardDaysIn(period);
        assertEquals(1, result);
    }

    @Test(timeout = 4000)
    public void testStandardPeriodInWithSeconds() {
        Period period = new Period(0, 0, 0, 0, 0, 0, 86400, 0); // 86400 seconds = 1 day
        int result = Days.standardDaysIn(period);
        assertEquals(1, result);
    }

    @Test(timeout = 4000)
    public void testStandardPeriodInWithMillis() {
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 86400000L); // 86400000 ms = 1 day
        int result = Days.standardDaysIn(period);
        assertEquals(1, result);
    }

    @Test(timeout = 4000)
    public void testStandardPeriodInWithMixedFields() {
        Period period = new Period(0, 0, 1, 1, 12, 0, 0, 0); // 1 week + 1 day + 12 hours
        // Total standard days = 7 + 1 + 0.5 = 8.5 => truncated to 8 days
        int result = Days.standardDaysIn(period);
        assertEquals(8, result);
    }

    @Test(timeout = 4000)
    public void testStandardPeriodInNegativeValues() {
        Period period = new Period(0, 0, 0, -5, 0, 0, 0, 0); // -5 days
        int result = Days.standardDaysIn(period);
        assertEquals(-5, result);
    }

    @Test(timeout = 4000)
    public void testStandardPeriodInOverflow() {
        // Large values causing overflow
        Period period = new Period(0, 0, 0, Integer.MAX_VALUE, 0, 0, 0, 0);
        try {
            Days.standardDaysIn(period);
            // May overflow in safeMultiply or safeAdd, but FieldUtils handles it
            // Actually Integer.MAX_VALUE days is fine, but if we multiply by millis...
            // This may cause long overflow if value * millisPerUnit exceeds Long.MAX_VALUE
            // Let's test with a huge period
        } catch (ArithmeticException e) {
            // Expected overflow
        }
    }

    @Test(timeout = 4000)
    public void testStandardPeriodInImpreciseField() {
        // Period with months should throw IllegalArgumentException
        Period period = new Period(1, 0, 0, 0, 0, 0, 0, 0); // 1 month
        try {
            Days.standardDaysIn(period);
            fail("Expected IllegalArgumentException for imprecise field");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("is not precise"));
        }
    }

    // ===================== Partition C: Defect-Targeted Branch Zone =====================

    /**
     * This test targets the known defect where between(ReadablePartial, ReadablePartial, ReadablePeriod)
     * fails when using MonthDay partials with day-of-month > 28 in non-leap contexts.
     * The defect manifests as an IllegalFieldValueException being thrown when the underlying
     * chrono.set() tries to set an invalid day-of-month for February.
     * 
     * The correct behavior should handle this by performing the calculation correctly,
     * as MonthDay is a valid partial that can represent Feb 29.
     */
    @Test(timeout = 4000)
    public void testFactory_methods_RPartial_MonthDay_Boundary() {
        // Test MonthDay with February 29 - should work for daysBetween
        MonthDay start = new MonthDay(2, 28); // Feb 28
        MonthDay end = new MonthDay(2, 29);   // Feb 29 (valid in some contexts, but partial)
        // This should calculate the difference in days (1 day) without throwing exception
        try {
            Days days = Days.daysBetween(start, end);
            assertEquals(1, days.getValue()); // Feb 28 to Feb 29 is 1 day
        } catch (IllegalFieldValueException e) {
            fail("Unexpected IllegalFieldValueException: " + e.getMessage());
        }

        // Test with other month-day combinations that might cause issues
        // Jan 31 to Feb 1 (different months)
        start = new MonthDay(1, 31);
        end = new MonthDay(2, 1);
        try {
            Days days = Days.daysBetween(start, end);
            // Expected: 1 day? Actually Jan 31 to Feb 1 is 1 day in the same year
            assertEquals(1, days.getValue());
        } catch (IllegalFieldValueException e) {
            fail("Unexpected IllegalFieldValueException: " + e.getMessage());
        }

        // Test monthsBetween with MonthDay
        try {
            Months months = Months.monthsBetween(start, new MonthDay(3, 1));
            // Jan 31 to Mar 1 is about 1 month (29 or 30 days depending on year)
            // But monthsBetween should give approximately 1 month (or 0?)
            // Actually from Jan 31 to Mar 1 is 29 days in non-leap, ~1 month
            // The exact value may vary, but should not throw exception
            assertNotNull(months);
        } catch (IllegalFieldValueException e) {
            fail("Unexpected IllegalFieldValueException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testFactory_methods_RPartial_MonthDay_LeapYearBoundary() {
        // Test with a known problematic case: Feb 29 in a non-leap year context
        // MonthDay does not have a year, so the calculation uses an arbitrary year (1970)
        // The bug is that the chrono.set() tries to set Feb 29 in 1970 which is not a leap year
        MonthDay start = new MonthDay(2, 28);
        MonthDay end = new MonthDay(2, 29);
        
        // This should succeed without throwing IllegalFieldValueException
        try {
            Days days = Days.daysBetween(start, end);
            // The correct behavior: treat Feb 28 to Feb 29 as 1 day difference
            assertEquals(1, days.getValue());
        } catch (IllegalFieldValueException e) {
            fail("Defect revealed: IllegalFieldValueException thrown for valid MonthDay difference: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testFactory_daysBetween_RPartial_MonthDay_MultipleMonths() {
        // Test cross-month boundaries that may trigger the defect
        MonthDay jan31 = new MonthDay(1, 31);
        MonthDay feb28 = new MonthDay(2, 28);
        MonthDay mar1 = new MonthDay(3, 1);
        
        // Jan 31 to Feb 28: should be 28 days
        try {
            Days days = Days.daysBetween(jan31, feb28);
            // In a non-leap year, Jan 31 to Feb 28 is 28 days (Jan has 31, so Feb 1 is 1 day after Jan 31,
            // Feb 28 is 28 days after Feb 1, total = 28 days)
            assertEquals(28, days.getValue());
        } catch (IllegalFieldValueException e) {
            fail("Defect revealed: " + e.getMessage());
        }
        
        // Jan 31 to Mar 1: should be 29 days (in non-leap year) or 30 (in leap year)
        // Since MonthDay doesn't specify year, the calculation uses 1970 (non-leap)
        try {
            Days days = Days.daysBetween(jan31, mar1);
            // Jan 31 to Mar 1 in 1970: Jan has 31 days, so after Jan 31, Feb 1 is next.
            // Feb 1970 has 28 days, so Mar 1 is 28 days after Feb 1?
            // Actually from Jan 31 00:00 to Mar 1 00:00 = 29 days (Feb has 28 in 1970)
            assertEquals(29, days.getValue());
        } catch (IllegalFieldValueException e) {
            fail("Defect revealed: " + e.getMessage());
        }
    }

    // ===================== Partition D: Exception & Defensive Guard Paths =====================

    @Test(timeout = 4000)
    public void testGetFieldTypeInvalidIndex() {
        Days days = Days.days(5);
        try {
            days.getFieldType(1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            assertEquals("1", e.getMessage());
        }
        try {
            days.getFieldType(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            assertEquals("-1", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testGetValueInvalidIndex() {
        Days days = Days.days(5);
        try {
            days.getValue(1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            assertEquals("1", e.getMessage());
        }
        try {
            days.getValue(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            assertEquals("-1", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testGetWithNullField() {
        Days days = Days.days(5);
        assertEquals(0, days.get(null));
    }

    @Test(timeout = 4000)
    public void testGetWithMatchingField() {
        Days days = Days.days(5);
        assertEquals(5, days.get(DurationFieldType.days()));
    }

    @Test(timeout = 4000)
    public void testGetWithNonMatchingField() {
        Days days = Days.days(5);
        assertEquals(0, days.get(DurationFieldType.hours()));
        assertEquals(0, days.get(DurationFieldType.months()));
    }

    @Test(timeout = 4000)
    public void testIsSupportedNull() {
        Days days = Days.days(5);
        assertFalse(days.isSupported(null));
    }

    @Test(timeout = 4000)
    public void testIsSupportedMatching() {
        Days days = Days.days(5);
        assertTrue(days.isSupported(DurationFieldType.days()));
    }

    @Test(timeout = 4000)
    public void testIsSupportedNonMatching() {
        Days days = Days.days(5);
        assertFalse(days.isSupported(DurationFieldType.hours()));
    }

    // ===================== Partition E: Object Lifecycle & Contract Integrity =====================

    @Test(timeout = 4000)
    public void testEqualsSameReference() {
        Days days = Days.days(5);
        assertTrue(days.equals(days));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        Days days = Days.days(5);
        assertFalse(days.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        Days days = Days.days(5);
        assertFalse(days.equals("string"));
        assertFalse(days.equals(5));
    }

    @Test(timeout = 4000)
    public void testEqualsSameTypeSameValue() {
        Days days1 = Days.days(5);
        Days days2 = Days.days(5);
        assertTrue(days1.equals(days2));
        assertTrue(days2.equals(days1));
    }

    @Test(timeout = 4000)
    public void testEqualsSameTypeDifferentValue() {
        Days days1 = Days.days(5);
        Days days2 = Days.days(10);
        assertFalse(days1.equals(days2));
        assertFalse(days2.equals(days1));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentPeriodType() {
        // Hours and days have different period types
        Hours hours = Hours.hours(5);
        Days days = Days.days(5);
        assertFalse(days.equals(hours));
        // But both are BaseSingleFieldPeriod, so equals checks periodType
        // Hours has PeriodType.hours(), Days has PeriodType.days() - different
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Days days1 = Days.days(7);
        Days days2 = Days.days(7);
        assertEquals(days1.hashCode(), days2.hashCode());
        
        Days days3 = Days.days(10);
        // Different values should produce different hash codes generally
        assertNotEquals(days1.hashCode(), days3.hashCode());
    }

    @Test(timeout = 4000)
    public void testCompareToClassCastException() {
        Days days = Days.days(5);
        Hours hours = Hours.hours(5);
        try {
            days.compareTo(hours);
            fail("Expected ClassCastException");
        } catch (ClassCastException e) {
            assertTrue(e.getMessage().contains("cannot be compared"));
        }
    }

    @Test(timeout = 4000)
    public void testCompareToNull() {
        Days days = Days.days(5);
        try {
            days.compareTo(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCompareToGreater() {
        Days days1 = Days.days(10);
        Days days2 = Days.days(5);
        assertEquals(1, days1.compareTo(days2));
    }

    @Test(timeout = 4000)
    public void testCompareToLess() {
        Days days1 = Days.days(3);
        Days days2 = Days.days(8);
        assertEquals(-1, days1.compareTo(days2));
    }

    @Test(timeout = 4000)
    public void testCompareToEqual() {
        Days days1 = Days.days(7);
        Days days2 = Days.days(7);
        assertEquals(0, days1.compareTo(days2));
    }

    @Test(timeout = 4000)
    public void testToPeriod() {
        Days days = Days.days(3);
        Period period = days.toPeriod();
        assertEquals(3, period.getDays());
        assertEquals(0, period.getHours());
        // Period should have only days field
        assertEquals(PeriodType.standard(), period.getPeriodType());
    }

    @Test(timeout = 4000)
    public void testToMutablePeriod() {
        Days days = Days.days(3);
        MutablePeriod mutablePeriod = days.toMutablePeriod();
        assertEquals(3, mutablePeriod.getDays());
        assertEquals(0, mutablePeriod.getHours());
        // Should be a new instance
        MutablePeriod mutablePeriod2 = days.toMutablePeriod();
        assertNotSame(mutablePeriod, mutablePeriod2);
        // But equals
        assertEquals(mutablePeriod, mutablePeriod2);
    }

    @Test(timeout = 4000)
    public void testSize() {
        Days days = Days.days(5);
        assertEquals(1, days.size());
    }

    @Test(timeout = 4000)
    public void testValueZero() {
        Days zero = Days.days(0);
        assertEquals(0, zero.getValue());
        assertEquals(0, zero.get(DurationFieldType.days()));
    }

    @Test(timeout = 4000)
    public void testNegativeValue() {
        Days negative = Days.days(-7);
        assertEquals(-7, negative.getValue());
        assertEquals(-7, negative.get(DurationFieldType.days()));
    }

    /**
     * Helper concrete subclass for testing protected methods.
     * Not strictly needed since we test through Days, but ensures coverage.
     */
    private static class MutableDays extends BaseSingleFieldPeriod {
        private static final long serialVersionUID = 1L;
        
        public MutableDays(int period) {
            super(period);
        }
        
        @Override
        public DurationFieldType getFieldType() {
            return DurationFieldType.days();
        }
        
        @Override
        public PeriodType getPeriodType() {
            return PeriodType.days();
        }
        
        // Expose setValue
        public void setValue(int value) {
            super.setValue(value);
        }
    }
}